package com.example.data.story

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.text.Spannable
import android.text.SpannableString
import android.text.style.AbsoluteSizeSpan
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import androidx.annotation.OptIn
import androidx.media3.common.Effect
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.OverlayEffect
import androidx.media3.effect.Presentation
import androidx.media3.effect.StaticOverlaySettings
import androidx.media3.effect.TextOverlay
import androidx.media3.effect.TextureOverlay
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.EditedMediaItemSequence
import androidx.media3.transformer.Effects
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.ProgressHolder
import androidx.media3.transformer.Transformer
import com.google.common.collect.ImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Renders the selected moments into one H.264/AAC MP4 on the device with Media3 Transformer:
 * clips are cut losslessly from the source, cropped to 16:9 or 9:16, labelled with a lower-third,
 * and the first clip carries the ride title.
 */
@OptIn(UnstableApi::class)
class ReelExporter(private val context: Context) {

    enum class Aspect(val width: Int, val height: Int, val label: String) {
        LANDSCAPE(1920, 1080, "16:9 YouTube"),
        VERTICAL(1080, 1920, "9:16 Reels / Shorts"),
    }

    suspend fun export(
        source: Uri,
        clips: List<StudioMoment>,
        labels: List<String>,
        title: String,
        location: String,
        aspect: Aspect,
        onProgress: (Float) -> Unit,
    ): File = withContext(Dispatchers.Main) {
        require(clips.isNotEmpty()) { "Select at least one moment" }
        val dir = File(context.cacheDir, "ridestory/reels").apply { mkdirs() }
        dir.listFiles()?.filter { it.lastModified() < System.currentTimeMillis() - 86_400_000 }?.forEach { it.delete() }
        val out = File(dir, "reel-${System.currentTimeMillis()}.mp4")

        suspendCancellableCoroutine { cont ->
            val handler = Handler(Looper.getMainLooper())
            val items = clips.mapIndexed { i, clip ->
                val startUs = (clip.startSec * 1_000_000).toLong()
                val endUs = (clip.endSec * 1_000_000).toLong()
                val mediaItem = MediaItem.Builder()
                    .setUri(source)
                    .setClippingConfiguration(
                        MediaItem.ClippingConfiguration.Builder()
                            .setStartPositionUs(startUs)
                            .setEndPositionUs(endUs)
                            .build()
                    )
                    .build()
                val overlays = mutableListOf<TextureOverlay>()
                if (i == 0) overlays += titleCard(title, location, aspect)
                val label = labels.getOrNull(i)?.takeIf { it.isNotBlank() } ?: clip.label
                overlays += lowerThird(label, aspect)
                val effects = Effects(
                    emptyList(),
                    listOf(
                        Presentation.createForWidthAndHeight(aspect.width, aspect.height, Presentation.LAYOUT_SCALE_TO_FIT_WITH_CROP),
                        OverlayEffect(ImmutableList.copyOf(overlays)),
                    ),
                )
                EditedMediaItem.Builder(mediaItem).setEffects(effects).build()
            }
            val composition = Composition.Builder(EditedMediaItemSequence(items)).build()
            val transformer = Transformer.Builder(context)
                .setVideoMimeType(MimeTypes.VIDEO_H264)
                .setAudioMimeType(MimeTypes.AUDIO_AAC)
                .addListener(object : Transformer.Listener {
                    override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                        handler.removeCallbacksAndMessages(null)
                        onProgress(1f)
                        if (cont.isActive) cont.resume(out)
                    }
                    override fun onError(composition: Composition, exportResult: ExportResult, exportException: ExportException) {
                        handler.removeCallbacksAndMessages(null)
                        out.delete()
                        if (cont.isActive) cont.resumeWithException(exportException)
                    }
                })
                .build()

            val holder = ProgressHolder()
            val poll = object : Runnable {
                override fun run() {
                    if (!cont.isActive) return
                    if (transformer.getProgress(holder) == Transformer.PROGRESS_STATE_AVAILABLE) onProgress(holder.progress / 100f)
                    handler.postDelayed(this, 300)
                }
            }
            cont.invokeOnCancellation {
                handler.post {
                    transformer.cancel()
                    out.delete()
                }
            }
            transformer.start(composition, out.absolutePath)
            handler.post(poll)
        }
    }

    private fun styled(text: String, sizePx: Int, color: Int, background: Int?): SpannableString =
        SpannableString(text).apply {
            val flags = Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            setSpan(ForegroundColorSpan(color), 0, length, flags)
            setSpan(AbsoluteSizeSpan(sizePx), 0, length, flags)
            setSpan(StyleSpan(Typeface.BOLD), 0, length, flags)
            background?.let { setSpan(BackgroundColorSpan(it), 0, length, flags) }
        }

    private fun lowerThird(text: String, aspect: Aspect): TextureOverlay {
        val size = if (aspect == Aspect.VERTICAL) 54 else 46
        val settings = StaticOverlaySettings.Builder()
            .setOverlayFrameAnchor(-1f, -1f)
            .setBackgroundFrameAnchor(-0.92f, if (aspect == Aspect.VERTICAL) -0.88f else -0.84f)
            .build()
        return TextOverlay.createStaticTextOverlay(styled("  $text  ", size, Color.WHITE, 0xB3020617.toInt()), settings)
    }

    private fun titleCard(title: String, location: String, aspect: Aspect): TextureOverlay {
        val size = if (aspect == Aspect.VERTICAL) 76 else 68
        val line = if (location.isBlank()) title else "$title\n$location"
        val settings = StaticOverlaySettings.Builder()
            .setOverlayFrameAnchor(0f, 1f)
            .setBackgroundFrameAnchor(0f, if (aspect == Aspect.VERTICAL) 0.8f else 0.85f)
            .build()
        return TextOverlay.createStaticTextOverlay(styled("  ${line.take(70)}  ", size, 0xFFFBBF24.toInt(), 0x99000000.toInt()), settings)
    }
}
