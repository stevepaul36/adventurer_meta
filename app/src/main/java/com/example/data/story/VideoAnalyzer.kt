package com.example.data.story

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * Samples frames from a ride video with MediaMetadataRetriever and produces
 * [FrameMetrics] for [MomentPicker], plus JPEG keyframes for Meta AI labelling.
 */
class VideoAnalyzer(private val context: Context) {

    data class VideoInfo(
        val durationSec: Double,
        val width: Int,
        val height: Int,
        val rotation: Int,
    )

    fun probe(uri: Uri): VideoInfo {
        val r = MediaMetadataRetriever()
        try {
            r.setDataSource(context, uri)
            val durationMs = r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
            val w = r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull() ?: 0
            val h = r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull() ?: 0
            val rot = r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)?.toIntOrNull() ?: 0
            return VideoInfo(durationMs / 1000.0, w, h, rot)
        } finally {
            runCatching { r.release() }
        }
    }

    suspend fun analyze(
        uri: Uri,
        targetSec: Double,
        onProgress: (Float, String) -> Unit = { _, _ -> },
    ): AnalysisResult = withContext(Dispatchers.IO) {
        val info = probe(uri)
        require(info.durationSec > 0.5) { "Video is too short or unreadable" }
        val times = MomentPicker.sampleTimes(info.durationSec)
        val keyDir = File(context.cacheDir, "ridestory/keyframes").apply {
            if (exists()) listFiles()?.forEach { it.delete() }
            mkdirs()
        }

        val r = MediaMetadataRetriever()
        r.setDataSource(context, uri)
        try {
            var prevLuma: FloatArray? = null
            val frames = mutableListOf<FrameMetrics>()
            val sampleW = 96
            val sampleH = 54
            times.forEachIndexed { idx, t ->
                onProgress(idx.toFloat() / times.size, "Sampling frame ${idx + 1}/${times.size}")
                val us = (t * 1_000_000).toLong()
                val bmp = r.getFrameAtTime(us, MediaMetadataRetriever.OPTION_CLOSEST_SYNC) ?: return@forEachIndexed
                val scaled = Bitmap.createScaledBitmap(bmp, sampleW, sampleH, true)
                if (scaled !== bmp) bmp.recycle()
                val pixels = IntArray(sampleW * sampleH)
                scaled.getPixels(pixels, 0, sampleW, 0, 0, sampleW, sampleH)
                val (metrics, luma) = FrameMeasurer.measure(t, pixels, sampleW, sampleH, prevLuma)
                prevLuma = luma
                frames += metrics

                // Save a larger keyframe for AI labelling (every few samples)
                if (idx % maxOf(1, times.size / 16) == 0 || idx == times.size / 2) {
                    val key = r.getFrameAtTime(us, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                    if (key != null) {
                        val out = File(keyDir, "k_${(t * 10).toInt()}.jpg")
                        FileOutputStream(out).use { fos ->
                            key.compress(Bitmap.CompressFormat.JPEG, 75, fos)
                        }
                        key.recycle()
                    }
                }
                scaled.recycle()
            }
            require(frames.isNotEmpty()) { "Could not sample any frames" }
            onProgress(0.95f, "Selecting highlight moments")
            val result = MomentPicker.pick(frames, info.durationSec, targetSec)

            // Attach keyframe paths to nearest moments
            val keys = keyDir.listFiles()?.filter { it.extension == "jpg" }?.sortedBy { it.name } ?: emptyList()
            val moments = result.moments.map { m ->
                val nearest = keys.minByOrNull { f ->
                    val ft = f.nameWithoutExtension.removePrefix("k_").toDoubleOrNull()?.div(10.0) ?: 0.0
                    kotlin.math.abs(ft - m.peakSec)
                }
                m.copy(thumbnailPath = nearest?.absolutePath)
            }
            onProgress(1f, "Done")
            result.copy(moments = moments)
        } finally {
            runCatching { r.release() }
        }
    }
}
