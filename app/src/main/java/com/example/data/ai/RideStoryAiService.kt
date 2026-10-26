package com.example.data.ai

import android.content.Context
import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.util.UUID

/**
 * Calls authenticated, App-Check-enforced Cloud Functions rather than calling a model
 * directly from the APK. Subscription verification and monthly AI limits are enforced
 * by the backend, so a modified client cannot unlock paid inference by changing its UI.
 */
object RideStoryAiService {
    private const val MAX_VIDEO_BYTES = 14 * 1024 * 1024
    private const val FUNCTIONS_REGION = "us-central1"

    data class ClipContext(
        val timeRange: String,
        val title: String,
        val tag: String,
        val speedKmh: Int?,
        val altitudeM: Int?
    )

    data class SuggestedHighlight(
        val startSeconds: Int,
        val endSeconds: Int,
        val title: String,
        val tag: String
    ) {
        val timeRange: String
            get() = "${formatTimestamp(startSeconds)} - ${formatTimestamp(endSeconds)}"
    }

    suspend fun analyzeVideoAndGenerateCaption(
        context: Context,
        videoUri: Uri,
        region: String,
        targetLength: String,
        narrationLanguage: String,
        clips: List<ClipContext>
    ): String {
        require(clips.isNotEmpty()) { "Select at least one highlight." }
        val storagePath = uploadTemporaryVideo(context, videoUri)
        return try {
            val result = callFunction(
                "generateRideCaption",
                mapOf(
                    "storagePath" to storagePath,
                    "region" to region,
                    "targetLength" to targetLength,
                    "narrationLanguage" to narrationLanguage,
                    "clips" to clips.map(::toWireClip)
                )
            )
            result["caption"] as? String
                ?: throw IllegalStateException("The AI service returned an empty caption.")
        } finally {
            deleteTemporaryVideo(storagePath)
        }
    }

    suspend fun analyzeVideoForHighlights(
        context: Context,
        videoUri: Uri,
        targetLength: String,
        narrationLanguage: String,
        activityType: String = "Motorcycling"
    ): List<SuggestedHighlight> {
        val storagePath = uploadTemporaryVideo(context, videoUri)
        return try {
            val result = callFunction(
                "analyzeRideHighlights",
                mapOf(
                    "storagePath" to storagePath,
                    "targetLength" to targetLength,
                    "narrationLanguage" to narrationLanguage,
                    "activityType" to activityType.take(64)
                )
            )
            val items = result["highlights"] as? List<*> ?: emptyList<Any>()
            items.mapNotNull { raw ->
                val item = raw as? Map<*, *> ?: return@mapNotNull null
                val start = (item["start_seconds"] as? Number)?.toInt() ?: return@mapNotNull null
                val end = (item["end_seconds"] as? Number)?.toInt() ?: return@mapNotNull null
                val title = (item["title"] as? String)?.trim().orEmpty()
                val tag = (item["tag"] as? String)?.trim().orEmpty().ifBlank { "Adventure" }
                if (start < 0 || end <= start || end - start > 30 || title.isBlank()) null
                else SuggestedHighlight(start, end, title.take(80), tag.take(30))
            }.take(8)
        } finally {
            deleteTemporaryVideo(storagePath)
        }
    }

    suspend fun generateCaption(
        region: String,
        targetLength: String,
        narrationLanguage: String,
        clips: List<ClipContext>
    ): String {
        require(clips.isNotEmpty()) { "Select at least one highlight." }
        val result = callFunction(
            "generateRideCaption",
            mapOf(
                "region" to region,
                "targetLength" to targetLength,
                "narrationLanguage" to narrationLanguage,
                "clips" to clips.map(::toWireClip)
            )
        )
        return result["caption"] as? String
            ?: throw IllegalStateException("The AI service returned an empty caption.")
    }

    private fun toWireClip(clip: ClipContext): Map<String, Any> = buildMap {
        put("timeRange", clip.timeRange.take(40))
        put("title", clip.title.take(100))
        put("tag", clip.tag.take(40))
        clip.speedKmh?.takeIf { it in 0..400 }?.let { put("speedKmh", it) }
        clip.altitudeM?.takeIf { it in -500..9000 }?.let { put("altitudeM", it) }
    }

    private suspend fun callFunction(name: String, payload: Map<String, Any>): Map<*, *> {
        val result = try {
            FirebaseFunctions.getInstance(FUNCTIONS_REGION)
                .getHttpsCallable(name)
                .call(payload)
                .await()
        } catch (error: Exception) {
            val message = error.message?.takeIf(String::isNotBlank)
            throw IllegalStateException(
                message ?: "Secure AI request failed. Check your connection and subscription."
            )
        }
        return result.data as? Map<*, *>
            ?: throw IllegalStateException("The AI service returned an invalid response.")
    }

    private suspend fun uploadTemporaryVideo(context: Context, uri: Uri): String {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
            ?: throw IllegalStateException("A secure session is required. Check Firebase Authentication setup.")
        val resolver = context.contentResolver
        val declaredSize = resolver.openAssetFileDescriptor(uri, "r")?.use { it.length } ?: -1L
        require(declaredSize < 0L || declaredSize <= MAX_VIDEO_BYTES) {
            "For this release, choose a video smaller than 14 MB."
        }

        val bytes = resolver.openInputStream(uri)?.use { input ->
            val buffer = ByteArray(16 * 1024)
            val output = ByteArrayOutputStream()
            var total = 0
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                total += count
                require(total <= MAX_VIDEO_BYTES) {
                    "For this release, choose a video smaller than 14 MB."
                }
                output.write(buffer, 0, count)
            }
            output.toByteArray()
        } ?: throw IllegalArgumentException("Unable to read the selected video.")

        require(bytes.isNotEmpty()) { "The selected video is empty." }
        val mimeType = resolver.getType(uri)?.takeIf { it.startsWith("video/") } ?: "video/mp4"
        val path = "ride-videos/$uid/${UUID.randomUUID()}.mp4"
        val reference = FirebaseStorage.getInstance().reference.child(path)
        val metadata = StorageMetadata.Builder().setContentType(mimeType).build()
        try {
            reference.putBytes(bytes, metadata).await()
        } catch (error: Exception) {
            throw IllegalStateException(
                error.message ?: "Video upload failed. Check Firebase Storage setup and retry."
            )
        }
        return path
    }

    private suspend fun deleteTemporaryVideo(path: String) {
        runCatching {
            FirebaseStorage.getInstance().reference.child(path).delete().await()
        }
    }

    private fun formatTimestamp(seconds: Int): String {
        val safe = seconds.coerceAtLeast(0)
        return String.format(java.util.Locale.US, "%02d:%02d", safe / 60, safe % 60)
    }
}
