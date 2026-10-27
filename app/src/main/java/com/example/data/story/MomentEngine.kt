package com.example.data.story

import com.example.data.model.ActivityType
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt

/*
 * RideStory moment engine — pure Kotlin (no Android imports) so it is unit-testable on the JVM.
 *
 * Given small RGB frames sampled across a ride video, it measures what makes footage worth
 * watching (visual change/motion, colourfulness, sharpness, open sky, greenery, golden-hour
 * warmth), detects scene changes, penalises dark / idle / shaky stretches and picks the best
 * non-overlapping clip windows to fill a target reel length.
 */

/** Per-frame measurements. All values are normalised to roughly 0..1. */
data class FrameMetrics(
    val timeSec: Double,
    val brightness: Double,
    val colorfulness: Double,
    val sharpness: Double,
    val change: Double,
    val warmth: Double,
    val sky: Double,
    val green: Double,
)

data class ScoredSample(val metrics: FrameMetrics, val score: Double)

data class StudioMoment(
    val id: String,
    val startSec: Double,
    val endSec: Double,
    val peakSec: Double,
    val score: Double,
    val tags: List<String>,
    val label: String,
    val description: String,
    val selected: Boolean,
    val thumbnailPath: String? = null,
    val aiLabeled: Boolean = false,
    val privacyFaces: Boolean = false,
    val privacyPlates: Boolean = false,
) {
    val durationSec: Double get() = endSec - startSec
}

data class AnalysisResult(
    val durationSec: Double,
    val sampleIntervalSec: Double,
    val samples: List<ScoredSample>,
    val sceneCutsSec: List<Double>,
    val moments: List<StudioMoment>,
    val targetSec: Double,
)

object FrameMeasurer {
    /**
     * Measures one frame. [argb] is packed ARGB (as from Bitmap.getPixels), [w]×[h] pixels.
     * Returns the metrics plus this frame's luma so the next call can compute [FrameMetrics.change].
     */
    fun measure(timeSec: Double, argb: IntArray, w: Int, h: Int, previousLuma: FloatArray?): Pair<FrameMetrics, FloatArray> {
        val n = w * h
        require(argb.size >= n) { "pixel buffer too small" }
        val luma = FloatArray(n)
        var sumL = 0.0; var sumR = 0.0; var sumB = 0.0
        var sumRg = 0.0; var sumYb = 0.0; var sumRg2 = 0.0; var sumYb2 = 0.0
        var skyCount = 0; var greenCount = 0
        val topPixels = (h / 3) * w

        for (i in 0 until n) {
            val p = argb[i]
            val r = (p shr 16) and 0xFF
            val g = (p shr 8) and 0xFF
            val b = p and 0xFF
            val l = 0.299 * r + 0.587 * g + 0.114 * b
            luma[i] = l.toFloat()
            sumL += l; sumR += r; sumB += b
            val rg = (r - g).toDouble()
            val yb = 0.5 * (r + g) - b
            sumRg += rg; sumYb += yb; sumRg2 += rg * rg; sumYb2 += yb * yb
            if (g > r * 1.08 && g > b * 1.05 && g > 50) greenCount++
            if (i < topPixels && ((b > r * 1.05 && b > 110) || l > 200)) skyCount++
        }

        val meanRg = sumRg / n; val meanYb = sumYb / n
        val stdRg = sqrt(max(0.0, sumRg2 / n - meanRg * meanRg))
        val stdYb = sqrt(max(0.0, sumYb2 / n - meanYb * meanYb))
        val colorfulness = min(1.0, (hypot(stdRg, stdYb) + 0.3 * hypot(meanRg, meanYb)) / 110.0)

        var lap = 0.0
        for (y in 1 until h - 1) for (x in 1 until w - 1) {
            val i = y * w + x
            lap += abs(4 * luma[i] - luma[i - 1] - luma[i + 1] - luma[i - w] - luma[i + w])
        }
        val inner = max(1, (w - 2) * (h - 2))
        val sharpness = min(1.0, lap / inner / 40.0)

        var change = 0.0
        if (previousLuma != null && previousLuma.size == n) {
            var d = 0.0
            for (i in 0 until n) d += abs(luma[i] - previousLuma[i])
            change = min(1.0, d / n / 255.0 * 2.2)
        }

        val metrics = FrameMetrics(
            timeSec = timeSec,
            brightness = sumL / n / 255.0,
            colorfulness = colorfulness,
            sharpness = sharpness,
            change = change,
            warmth = (sumR - sumB) / n / 255.0,
            sky = if (topPixels > 0) skyCount.toDouble() / topPixels else 0.0,
            green = greenCount.toDouble() / n,
        )
        return metrics to luma
    }
}

object MomentPicker {
    const val MIN_AUTO_SELECT_SCORE = 0.32

    /** How often to sample a video of [durationSec] (≤ ~320 frames, 0.5–10 s apart). */
    fun sampleInterval(durationSec: Double): Double = min(10.0, max(0.5, durationSec / 320.0))

    fun sampleTimes(durationSec: Double): List<Double> {
        val interval = sampleInterval(durationSec)
        val count = max(2, (durationSec / interval).toInt())
        return (0 until count).map { k -> min(durationSec - 0.05, (k + 0.5) * interval) }
    }

    fun pick(
        rawFrames: List<FrameMetrics>,
        durationSec: Double,
        targetSec: Double,
        bookmarksSec: List<Double> = emptyList(),
    ): AnalysisResult {
        require(rawFrames.isNotEmpty()) { "no frames" }
        val interval = sampleInterval(durationSec)
        val frames = if (rawFrames.size > 1) listOf(rawFrames[0].copy(change = rawFrames[1].change)) + rawFrames.drop(1) else rawFrames

        // Scene cuts: change spikes well above the typical level.
        val changes = frames.map { it.change }
        val sorted = changes.sorted()
        val median = sorted[sorted.size / 2]
        val p90 = sorted[min(sorted.size - 1, (sorted.size * 0.9).toInt())]
        val cutThreshold = maxOf(0.18, median * 3, p90)
        val sceneCuts = frames.filter { it.change >= cutThreshold }.map { it.timeSec }

        val colorRank = percentileRanks(frames.map { it.colorfulness })
        val changeRank = percentileRanks(changes)
        val sharpRank = percentileRanks(frames.map { it.sharpness })
        fun nearBookmark(t: Double) = bookmarksSec.any { abs(it - t) <= max(4.0, interval * 1.5) }
        fun golden(f: FrameMetrics) = f.warmth > 0.1 && f.brightness > 0.25 && f.brightness < 0.8

        val raw = frames.mapIndexed { i, f ->
            var s = 0.28 * colorRank[i] +
                0.24 * changeRank[i] +
                0.16 * sharpRank[i] +
                0.12 * min(1.0, max(f.sky, f.green) * 1.6) +
                0.10 * (if (golden(f)) 1.0 else 0.0) +
                0.10 * min(1.0, f.brightness * 2)
            if (f.brightness < 0.12) s -= 0.35                         // too dark to watch
            if (changeRank[i] < 0.12) s -= 0.08                        // parked / idle
            if (changeRank[i] > 0.9 && sharpRank[i] < 0.2) s -= 0.12  // shaky blur
            if (nearBookmark(f.timeSec)) s += 0.5                      // rider marked it
            s
        }
        val smooth = raw.indices.map { i ->
            val a = raw[max(0, i - 1)]; val b = raw[i]; val c = raw[min(raw.size - 1, i + 1)]
            (0.25 * a + 0.5 * b + 0.25 * c).coerceIn(0.0, 1.0)
        }
        val samples = frames.mapIndexed { i, f -> ScoredSample(f, smooth[i]) }

        // Choose windows greedily by score, keeping them apart.
        val target = min(targetSec, durationSec)
        val wholeVideo = durationSec <= target
        val clipCount = if (wholeVideo) 1 else (target / 8).roundToInt().coerceIn(3, 20)
        val clipLen = if (wholeVideo) durationSec else (target / clipCount).coerceIn(3.0, 15.0)
        val minGap = max(clipLen * 0.5, durationSec / (clipCount * 4))
        data class Window(val start: Double, val end: Double, val peak: Int)
        val windows = mutableListOf<Window>()
        for (idx in samples.indices.sortedByDescending { samples[it].score }) {
            if (windows.size >= clipCount + 4) break
            val t = samples[idx].metrics.timeSec
            var start = (t - clipLen / 2).coerceIn(0.0, max(0.0, durationSec - clipLen))
            sceneCuts.firstOrNull { it > start && it < start + clipLen / 3 }?.let { cut ->
                start = min(cut + 0.1, max(0.0, durationSec - clipLen))   // start on the cut
            }
            var end = min(durationSec, start + clipLen)
            sceneCuts.lastOrNull { it > end - clipLen / 3 && it < end }?.let { cut ->
                end = cut - 0.1                                            // stop before the next scene
            }
            val clash = windows.any { start < it.end + minGap && end > it.start - minGap }
            if (!clash) windows += Window(start, end, idx)
        }

        var total = 0.0
        val moments = windows.mapIndexed { rank, w ->
            val peak = samples[w.peak]
            val isCut = sceneCuts.any { it in w.start..w.end }
            val tags = tagsFor(peak.metrics, isCut, changeRank[w.peak], nearBookmark(peak.metrics.timeSec))
            // Don't pad the reel with dull footage: weak windows stay as alternates.
            val selected = total < target - 0.5 && rank < clipCount && (rank == 0 || peak.score >= MIN_AUTO_SELECT_SCORE)
            if (selected) total += w.end - w.start
            StudioMoment(
                id = "m${(w.start * 10).roundToInt()}",
                startSec = round1(w.start),
                endSec = round1(w.end),
                peakSec = round1(peak.metrics.timeSec),
                score = peak.score,
                tags = tags,
                label = labelFor(tags),
                description = "Score ${pct(peak.score)} · colour ${pct(peak.metrics.colorfulness)} · motion ${pct(peak.metrics.change)} · sharpness ${pct(peak.metrics.sharpness)}",
                selected = selected,
            )
        }.sortedBy { it.startSec }

        return AnalysisResult(durationSec, interval, samples, sceneCuts, moments, target)
    }

    /** Re-pick the reel after scores change (e.g. after AI labelling), keeping the target length. */
    fun reselect(moments: List<StudioMoment>, targetSec: Double): List<StudioMoment> {
        val chosen = mutableSetOf<String>()
        var total = 0.0
        for (m in moments.sortedByDescending { it.score }) {
            if (total >= targetSec - 0.5) break
            if (chosen.isNotEmpty() && m.score < MIN_AUTO_SELECT_SCORE) break
            chosen += m.id
            total += m.durationSec
        }
        return moments.map { it.copy(selected = it.id in chosen) }
    }

    fun tagsFor(f: FrameMetrics, isCut: Boolean, changeRank: Double, bookmarked: Boolean): List<String> = buildList {
        if (bookmarked) add("bookmarked")
        if (f.warmth > 0.1 && f.brightness > 0.25 && f.brightness < 0.8) add("golden hour")
        if (f.green > 0.3) add("green valley")
        if (f.sky > 0.45) add("open sky")
        if (changeRank > 0.8 && f.sharpness > 0.25) add("action")
        if (isCut) add("new scene")
        if (f.colorfulness > 0.55) add("vivid")
        if (f.brightness < 0.16) add("low light")
    }

    fun labelFor(tags: List<String>): String = when {
        "bookmarked" in tags -> "Rider-marked moment"
        "golden hour" in tags && "open sky" in tags -> "Golden-hour skyline"
        "golden hour" in tags -> "Golden-hour stretch"
        "green valley" in tags && "open sky" in tags -> "Open valley vista"
        "green valley" in tags -> "Green valley run"
        "open sky" in tags -> "Big-sky horizon"
        "action" in tags -> "Action burst"
        "new scene" in tags -> "New stretch of road"
        "vivid" in tags -> "Colourful roadside"
        else -> "On the move"
    }

    fun percentileRanks(values: List<Double>): List<Double> {
        val ranks = DoubleArray(values.size) { 0.5 }
        if (values.size > 1) {
            values.indices.sortedBy { values[it] }.forEachIndexed { k, idx -> ranks[idx] = k.toDouble() / (values.size - 1) }
        }
        return ranks.toList()
    }

    private fun round1(v: Double) = (v * 10).roundToInt() / 10.0
    private fun pct(v: Double) = (v * 100).roundToInt()
}

/** Story copy (caption, diary, chapters…). Built by Meta AI or, offline, by [StoryTemplate]. */
data class StoryCopy(
    val caption: String,
    val diary: String,
    val hashtags: List<String>,
    val chapterTitles: List<String>,
    val narration: String,
    val fromAi: Boolean,
)

object StoryTemplate {
    const val TITLE_CARD_SEC = 0.0

    /** Offline story built only from what the analysis actually found — no invented facts. */
    fun build(
        title: String,
        location: String,
        moments: List<StudioMoment>,
        sourceSec: Double,
        activityType: ActivityType = ActivityType.Motorcycling,
        template: AdventureStoryTemplate = AdventureStoryTemplates.byId("epic_journey"),
    ): StoryCopy {
        val place = location.trim().ifEmpty { "the planned destination" }
        val name = title.trim().ifEmpty { "An adventure to remember" }
        val reelSec = moments.sumOf { it.durationSec }
        val highlights = moments.map { it.label.lowercase() }.distinct().take(3)
        val list = when (highlights.size) {
            0 -> "the selected moments"
            1 -> highlights[0]
            else -> highlights.dropLast(1).joinToString(", ") + " and " + highlights.last()
        }
        val activityNoun = when (activityType) {
            ActivityType.Motorcycling -> "motorcycle ride"
            ActivityType.Cycling -> "cycling adventure"
            ActivityType.Walking -> "walk"
            ActivityType.Trekking -> "hike or trek"
            ActivityType.CarTrips -> "road trip"
            ActivityType.Camping -> "camping trip"
            ActivityType.Fishing -> "fishing outing"
            ActivityType.Hunting -> "outdoor field trip"
            ActivityType.ScubaDiving -> "dive trip"
            ActivityType.Snorkeling -> "snorkelling trip"
            ActivityType.KayakingPaddling -> "paddling trip"
            ActivityType.BoatingSailing -> "boating trip"
            ActivityType.IslandExploration -> "island adventure"
            ActivityType.DestinationExploration -> "destination exploration"
            ActivityType.WildlifeExploration -> "nature-watching trip"
            ActivityType.ClimbingCaving -> "climbing or caving trip"
            ActivityType.Hybrid -> "multi-activity adventure"
        }
        val placeTags = place.split(' ', ',').filter { it.isNotBlank() }.take(2)
            .map { "#" + it.filter(Char::isLetterOrDigit) }.filter { it.length > 1 }
        val modeTag = "#" + activityType.displayName.replace(Regex("[^A-Za-z0-9]"), "")
        return StoryCopy(
            caption = "$name — a $activityNoun through $place, cut to ${formatTime(reelSec)}. Featuring $list.",
            diary = "$name.\n\nThis $activityNoun through $place produced ${formatTime(sourceSec)} of footage. " +
                "The selected moments — $list — were assembled into a ${formatTime(reelSec)} reel. " +
                "${template.description}\n\nAdd your own observations, people, stops and details before sharing.",
            hashtags = listOf("#Adventurer", modeTag, "#ExploreResponsibly") + placeTags,
            chapterTitles = moments.map { it.label },
            narration = "$name. $place. " + moments.joinToString(". ") { it.label } + ".",
            fromAi = false,
        )
    }

    /** Chapter start times in the finished reel (clips play back to back). */
    fun reelChapterTimes(moments: List<StudioMoment>): List<Double> {
        var t = TITLE_CARD_SEC
        return moments.map { m -> t.also { t += m.durationSec } }
    }
}

fun formatTime(seconds: Double): String {
    if (!seconds.isFinite() || seconds < 0) return "0:00"
    val total = seconds.toInt()
    val h = total / 3600; val m = (total % 3600) / 60; val s = total % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%d:%02d".format(m, s)
}
