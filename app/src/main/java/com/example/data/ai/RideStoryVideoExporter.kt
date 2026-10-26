package com.example.data.ai

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.EditedMediaItemSequence
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.Transformer
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Exports selected timestamp ranges from one source video into a single MP4 highlight reel.
 * Timestamp labels are interpreted as mm:ss - mm:ss. The export is local to the device.
 */
object RideStoryVideoExporter {
    data class Segment(val timeRange: String, val title: String)

    data class TimeRange(val startMs: Long, val endMs: Long) {
        val durationMs: Long get() = endMs - startMs
    }

    fun parseTimeRange(label: String): TimeRange {
        val match = Regex("""(\d{1,2}):(\d{2})\s*[-–]\s*(\d{1,2}):(\d{2})""")
            .matchEntire(label.trim())
            ?: throw IllegalArgumentException("Unsupported clip time range: $label. Use mm:ss - mm:ss.")
        val (startMin, startSec, endMin, endSec) = match.destructured
        val start = (startMin.toLong() * 60 + startSec.toLong()) * 1000
        val end = (endMin.toLong() * 60 + endSec.toLong()) * 1000
        require(start >= 0 && end > start) { "Clip end time must be after its start time." }
        return TimeRange(start, end)
    }

    fun limitSegments(segments: List<Segment>, targetLength: String): List<TimeRange> {
        val targetMs = when {
            targetLength.startsWith("15") -> 15_000L
            targetLength.startsWith("30") -> 30_000L
            else -> 60_000L
        }
        var remaining = targetMs
        return segments.mapNotNull { segment ->
            if (remaining <= 0) return@mapNotNull null
            val range = parseTimeRange(segment.timeRange)
            val takeMs = minOf(range.durationMs, remaining)
            if (takeMs <= 0) null else {
                remaining -= takeMs
                TimeRange(range.startMs, range.startMs + takeMs)
            }
        }
    }

    @OptIn(UnstableApi::class)
    suspend fun export(
        context: Context,
        sourceUri: Uri,
        segments: List<Segment>,
        targetLength: String
    ): File = suspendCancellableCoroutine { continuation ->
        val ranges = try {
            limitSegments(segments, targetLength)
        } catch (error: Exception) {
            continuation.resumeWithException(error)
            return@suspendCancellableCoroutine
        }
        if (ranges.isEmpty()) {
            continuation.resumeWithException(IllegalArgumentException("Select at least one valid highlight."))
            return@suspendCancellableCoroutine
        }

        val outputFile = File(context.cacheDir, "adventurer-ride-story-${System.currentTimeMillis()}.mp4")
        val items = ranges.map { range ->
            val mediaItem = MediaItem.Builder()
                .setUri(sourceUri)
                .setClippingConfiguration(
                    MediaItem.ClippingConfiguration.Builder()
                        .setStartPositionMs(range.startMs)
                        .setEndPositionMs(range.endMs)
                        .build()
                )
                .build()
            EditedMediaItem.Builder(mediaItem).build()
        }
        // Media3 Transformer 1.8.0 exposes the sequence through its Builder API.
        val sequence = EditedMediaItemSequence.Builder(items).build()
        val composition = Composition.Builder(listOf(sequence)).build()
        val transformer = Transformer.Builder(context)
            .addListener(object : Transformer.Listener {
                override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                    if (continuation.isActive) continuation.resume(outputFile)
                }

                override fun onError(
                    composition: Composition,
                    exportResult: ExportResult,
                    exportException: ExportException
                ) {
                    outputFile.delete()
                    if (continuation.isActive) continuation.resumeWithException(exportException)
                }
            })
            .build()

        continuation.invokeOnCancellation {
            transformer.cancel()
            outputFile.delete()
        }
        try {
            transformer.start(composition, outputFile.absolutePath)
        } catch (error: Exception) {
            outputFile.delete()
            if (continuation.isActive) continuation.resumeWithException(error)
        }
    }
}
