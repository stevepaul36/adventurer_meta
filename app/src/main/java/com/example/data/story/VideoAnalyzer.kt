package com.example.data.story

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * On-device analysis of a ride video: samples frames with [MediaMetadataRetriever], measures them
 * with [FrameMeasurer], picks moments with [MomentPicker] and saves a JPEG keyframe per moment.
 * Nothing is uploaded.
 */
class VideoAnalyzer(private val context: Context) {

    data class VideoInfo(val durationSec: Double, val width: Int, val height: Int, val sizeBytes: Long?)

    fun probe(uri: Uri): VideoInfo {
        val r = MediaMetadataRetriever()
        try {
            r.setDataSource(context, uri)
            val durationMs = r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
            var w = r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull() ?: 0
            var h = r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull() ?: 0
            val rotation = r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)?.toIntOrNull() ?: 0
            if (rotation == 90 || rotation == 270) { val t = w; w = h; h = t }
            val size = runCatching {
                context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { it.length }
            }.getOrNull()?.takeIf { it > 0 }
            return VideoInfo(durationMs / 1000.0, w, h, size)
        } finally {
            r.release()
        }
    }

    suspend fun analyze(
        uri: Uri,
        targetSec: Double,
        bookmarksSec: List<Double> = emptyList(),
        onProgress: (Float, String) -> Unit,
    ): AnalysisResult = withContext(Dispatchers.Default) {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, uri)
            val durationSec = (retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L) / 1000.0
            require(durationSec > 0.5) { "Could not read this video's duration." }

            val times = MomentPicker.sampleTimes(durationSec)
            val frames = ArrayList<FrameMetrics>(times.size)
            var previousLuma: FloatArray? = null
            var lastGood: FrameMetrics? = null

            times.forEachIndexed { k, t ->
                ensureActive()
                // CLOSEST_SYNC is fast (nearest keyframe) — plenty for scoring.
                val bmp = frameAt(retriever, t, MediaMetadataRetriever.OPTION_CLOSEST_SYNC, SAMPLE_WIDTH)
                if (bmp != null) {
                    val w = bmp.width; val h = bmp.height
                    val px = IntArray(w * h)
                    bmp.getPixels(px, 0, w, 0, 0, w, h)
                    bmp.recycle()
                    val (m, luma) = FrameMeasurer.measure(t, px, w, h, previousLuma)
                    previousLuma = luma
                    frames += m
                    lastGood = m
                } else {
                    lastGood?.let { frames += it.copy(timeSec = t) } // undecodable frame: repeat previous
                }
                if (k % 4 == 0) onProgress(0.8f * k / times.size, "Scanning ${formatTime(t)} / ${formatTime(durationSec)}")
            }
            require(frames.isNotEmpty()) { "No frames could be decoded from this video." }

            onProgress(0.82f, "Choosing the best moments")
            val picked = MomentPicker.pick(frames, durationSec, targetSec, bookmarksSec)

            // Keyframes for the UI, the feed post and the vision model.
            val dir = File(context.cacheDir, "ridestory/keyframes").apply { mkdirs() }
            dir.listFiles()?.forEach { it.delete() }
            val withThumbs = picked.moments.mapIndexed { i, m ->
                ensureActive()
                onProgress(0.85f + 0.15f * (i + 1) / picked.moments.size, "Capturing keyframes")
                val bmp = frameAt(retriever, m.peakSec, MediaMetadataRetriever.OPTION_CLOSEST, THUMB_WIDTH)
                val path = bmp?.let { b ->
                    val file = File(dir, "${m.id}_${System.currentTimeMillis()}.jpg")
                    FileOutputStream(file).use { out -> b.compress(Bitmap.CompressFormat.JPEG, 82, out) }
                    b.recycle()
                    file.absolutePath
                }
                m.copy(thumbnailPath = path)
            }
            picked.copy(moments = withThumbs)
        } finally {
            retriever.release()
        }
    }

    private fun frameAt(r: MediaMetadataRetriever, timeSec: Double, option: Int, width: Int): Bitmap? {
        val us = (timeSec * 1_000_000).toLong()
        return runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                r.getScaledFrameAtTime(us, option, width, width * 4) // keeps aspect ratio within bounds
            } else {
                r.getFrameAtTime(us, option)?.let { full ->
                    val h = (width * full.height / full.width.coerceAtLeast(1)).coerceAtLeast(8)
                    Bitmap.createScaledBitmap(full, width, h, true).also { if (it !== full) full.recycle() }
                }
            }
        }.getOrNull()
    }

    companion object {
        private const val SAMPLE_WIDTH = 96
        private const val THUMB_WIDTH = 480
    }
}
