package com.example

import com.example.data.model.ActivityType
import com.example.data.story.AdventureStoryTemplates
import com.example.data.story.AiJson
import com.example.data.story.FrameMeasurer
import com.example.data.story.FrameMetrics
import com.example.data.story.MomentPicker
import com.example.data.story.StoryTemplate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class MomentEngineTest {

    private val w = 64
    private val h = 36

    /** Synthetic 60 s ride: colourful moving 0–15 s, near-black 15–30 s, green scenery 30–45 s, grey static 45–60 s. */
    private fun syntheticRide(): Pair<List<FrameMetrics>, Double> {
        val duration = 60.0
        val rnd = Random(7)
        var prev: FloatArray? = null
        val frames = MomentPicker.sampleTimes(duration).map { t ->
            val px = IntArray(w * h) { i ->
                val x = i % w
                when {
                    t < 15 -> { // moving colour bars
                        val bar = ((x + (t * 8).toInt()) / 8) % 6
                        intArrayOf(0xE53935, 0x43A047, 0xFDD835, 0x1E88E5, 0x8E24AA, 0x00ACC1)[bar] or (0xFF shl 24)
                    }
                    t < 30 -> (0xFF shl 24) or (rnd.nextInt(6) * 0x010101)
                    t < 45 -> { val g = 120 + rnd.nextInt(100); (0xFF shl 24) or (40 shl 16) or (g shl 8) or 50 }
                    else -> (0xFF shl 24) or 0x808080
                }
            }
            val (m, luma) = FrameMeasurer.measure(t, px, w, h, prev)
            prev = luma
            m
        }
        return frames to duration
    }

    @Test
    fun skipsDarkFootageAndKeepsClipsApart() {
        val (frames, duration) = syntheticRide()
        val result = MomentPicker.pick(frames, duration, targetSec = 30.0)
        val selected = result.moments.filter { it.selected }.sortedBy { it.startSec }

        assertTrue(selected.isNotEmpty())
        assertTrue("dark stretch must not be selected", selected.none { it.peakSec in 16.0..29.0 })
        selected.zipWithNext().forEach { (a, b) -> assertTrue("clips overlap", a.endSec <= b.startSec) }
        assertTrue(selected.all { it.startSec >= 0 && it.endSec <= duration })
        assertTrue(selected.sumOf { it.durationSec } <= 30.0 + 15.0)
    }

    @Test
    fun riderBookmarkIsHonoured() {
        val (frames, duration) = syntheticRide()
        val result = MomentPicker.pick(frames, duration, targetSec = 30.0, bookmarksSec = listOf(52.0))
        assertTrue(result.moments.any { it.selected && "bookmarked" in it.tags && it.peakSec in 47.0..57.0 })
    }

    @Test
    fun shortVideoBecomesSingleClip() {
        val (frames, _) = syntheticRide()
        val result = MomentPicker.pick(frames.take(8), durationSec = 4.0, targetSec = 30.0)
        assertEquals(1, result.moments.size)
        assertTrue(result.moments[0].selected)
    }

    @Test
    fun reselectFollowsNewScores() {
        val (frames, duration) = syntheticRide()
        val moments = MomentPicker.pick(frames, duration, 30.0).moments
        val target = moments.maxByOrNull { it.startSec }!!
        val boosted = moments.map { if (it.id == target.id) it.copy(score = 0.99) else it.copy(score = 0.4) }
        val re = MomentPicker.reselect(boosted, targetSec = target.durationSec)
        assertTrue(re.first { it.id == target.id }.selected)
        assertEquals(1, re.count { it.selected })
    }

    @Test
    fun templateStoryUsesOnlyDetectedMoments() {
        val (frames, duration) = syntheticRide()
        val selected = MomentPicker.pick(frames, duration, 30.0).moments.filter { it.selected }
        val story = StoryTemplate.build("Ghat run", "Varandha Ghat", selected, duration)
        assertFalse(story.fromAi)
        assertEquals(selected.size, story.chapterTitles.size)
        assertTrue(story.caption.startsWith("Ghat run"))
        assertEquals(selected.size, StoryTemplate.reelChapterTimes(selected).size)
    }

    @Test
    fun storyTemplatesAndOfflineCopyAreActivitySpecific() {
        val (frames, duration) = syntheticRide()
        val selected = MomentPicker.pick(frames, duration, 30.0).moments.filter { it.selected }
        val roadTripTemplate = AdventureStoryTemplates.byId("road_trip")

        assertTrue(AdventureStoryTemplates.forActivity(ActivityType.CarTrips).any { it.id == roadTripTemplate.id })
        assertTrue(AdventureStoryTemplates.forActivity(ActivityType.ScubaDiving).any { it.id == "water_log" })

        val roadTripStory = StoryTemplate.build(
            title = "Coastal escape",
            location = "Vancouver Island",
            moments = selected,
            sourceSec = duration,
            activityType = ActivityType.CarTrips,
            template = roadTripTemplate,
        )

        assertTrue(roadTripStory.caption.contains("road trip"))
        assertTrue(roadTripStory.caption.contains("Vancouver Island"))
        assertFalse(roadTripStory.caption.contains("riding through"))
        assertTrue(roadTripStory.hashtags.contains("#Adventurer"))
    }

    @Test
    fun extractsJsonFromChattyModelReplies() {
        assertEquals("[{\"a\":\"x ] y\"}]", AiJson.extract("Here you go:\n```json\n[{\"a\":\"x ] y\"}]\n```"))
        assertEquals("{\"c\":{\"d\":1}}", AiJson.extract("{\"c\":{\"d\":1}} thanks"))
    }
}
