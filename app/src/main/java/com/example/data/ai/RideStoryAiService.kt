package com.example.data.ai

import android.content.Context
import android.net.Uri
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.Firebase
import com.google.firebase.ai.type.content
import org.json.JSONArray
import org.json.JSONObject

/**
 * Firebase AI Logic wrapper for turning ride metadata into a social-ready story.
 *
 * Firebase must be configured for this Android app and the Gemini Developer API
 * enabled in Firebase AI Logic before requests can succeed.
 */
object RideStoryAiService {
    private const val MAX_VIDEO_BYTES = 14 * 1024 * 1024
    data class ClipContext(
        val timeRange: String,
        val title: String,
        val tag: String,
        val speedKmh: Int?,
        val altitudeM: Int?
    )

    suspend fun analyzeVideoAndGenerateCaption(
        context: Context,
        videoUri: Uri,
        region: String,
        targetLength: String,
        narrationLanguage: String,
        clips: List<ClipContext>
    ): String {
        require(clips.isNotEmpty()) { "Select at least one highlight." }
        val resolver = context.contentResolver
        val mimeType = resolver.getType(videoUri)?.takeIf { it.startsWith("video/") }
            ?: "video/mp4"
        val declaredSize = resolver.openAssetFileDescriptor(videoUri, "r")?.use { it.length } ?: -1L
        require(declaredSize <= MAX_VIDEO_BYTES || declaredSize < 0L) {
            "For this demo, choose a video smaller than 14 MB."
        }
        val bytes = resolver.openInputStream(videoUri)?.use { stream ->
            val buffer = ByteArray(16 * 1024)
            val output = java.io.ByteArrayOutputStream()
            var total = 0
            while (true) {
                val count = stream.read(buffer)
                if (count < 0) break
                total += count
                require(total <= MAX_VIDEO_BYTES) {
                    "For this demo, choose a video smaller than 14 MB."
                }
                output.write(buffer, 0, count)
            }
            output.toByteArray()
        } ?: throw IllegalArgumentException("Unable to read the selected video.")

        val clipDetails = clips.joinToString(separator = "\n") { clip ->
            "- ${clip.timeRange}: ${clip.title} (tag: ${clip.tag}${clip.speedKmh?.let { "; speed metadata: $it km/h" } ?: ""}${clip.altitudeM?.let { "; altitude metadata: $it m" } ?: ""})"
        }
        val prompt = """
            Create a social-ready ride story caption based on the actual video content and the
            supplied rider metadata. Use the requested language/style: $narrationLanguage.
            Region: $region. Target reel length: $targetLength.
            Candidate highlights:
            $clipDetails
            Describe only scenes you can actually infer from the video. Treat speed and altitude
            as user-provided metadata, not visual facts. Do not invent locations or safety claims.
            Never encourage speeding or dangerous riding. Use at most four hashtags.
            Return only the caption.
        """.trimIndent()
        val model = Firebase.ai(backend = GenerativeBackend.googleAI(), useLimitedUseAppCheckTokens = true)
            .generativeModel("gemini-3.8-flash")
        val request = content {
            inlineData(bytes, mimeType)
            text(prompt)
        }
        val response = model.generateContent(request)
        return response.text?.trim()?.takeIf { it.isNotEmpty() }
            ?: throw IllegalStateException("The AI model returned an empty caption.")
    }

    data class SuggestedHighlight(
        val startSeconds: Int,
        val endSeconds: Int,
        val title: String,
        val tag: String
    ) {
        val timeRange: String
            get() = "${formatTimestamp(startSeconds)} - ${formatTimestamp(endSeconds)}"
    }

    private fun formatTimestamp(seconds: Int): String {
        val safe = seconds.coerceAtLeast(0)
        return String.format(java.util.Locale.US, "%02d:%02d", safe / 60, safe % 60)
    }

    suspend fun analyzeVideoForHighlights(
        context: Context,
        videoUri: Uri,
        targetLength: String,
        narrationLanguage: String
    ): List<SuggestedHighlight> {
        val resolver = context.contentResolver
        val mimeType = resolver.getType(videoUri)?.takeIf { it.startsWith("video/") }
            ?: "video/mp4"
        val declaredSize = resolver.openAssetFileDescriptor(videoUri, "r")?.use { it.length } ?: -1L
        require(declaredSize <= MAX_VIDEO_BYTES || declaredSize < 0L) {
            "For this demo, choose a video smaller than 14 MB."
        }
        val bytes = resolver.openInputStream(videoUri)?.use { stream ->
            val buffer = ByteArray(16 * 1024)
            val output = java.io.ByteArrayOutputStream()
            var total = 0
            while (true) {
                val count = stream.read(buffer)
                if (count < 0) break
                total += count
                require(total <= MAX_VIDEO_BYTES) {
                    "For this demo, choose a video smaller than 14 MB."
                }
                output.write(buffer, 0, count)
            }
            output.toByteArray()
        } ?: throw IllegalArgumentException("Unable to read the selected video.")

        val prompt = """
            Analyze this motorcycle/adventure travel video and identify up to 8 genuinely interesting
            moments suitable for a short social reel of target length $targetLength.
            Requested language/style for titles: $narrationLanguage.
            Return ONLY valid JSON with this exact shape:
            {"highlights":[{"start_seconds":14,"end_seconds":22,"title":"Short moment title","tag":"Scenic"}]}
            Rules:
            - Use timestamps in seconds relative to the start of this video.
            - Every end_seconds must be greater than start_seconds; clips should be 2 to 12 seconds.
            - Use moments actually visible in the video; never invent locations, speed, altitude, weather,
              safety outcomes, or events. Do not label riding as a drift or high-speed maneuver unless
              that is clearly and safely established by the footage.
            - Prefer scenic views, route transitions, group moments, and memorable but safe riding.
            - Titles should be short and the tag should be 1-3 words.
            - Return an empty highlights array if no reliable moments can be identified.
        """.trimIndent()
        val model = Firebase.ai(
            backend = GenerativeBackend.googleAI(),
            useLimitedUseAppCheckTokens = true
        ).generativeModel("gemini-3.8-flash")
        val request = content {
            inlineData(bytes, mimeType)
            text(prompt)
        }
        val raw = model.generateContent(request).text?.trim()
            ?: throw IllegalStateException("The AI model returned no highlight suggestions.")
        val jsonFence = "`" + "`" + "`"
        val jsonText = raw.removePrefix(jsonFence + "json").removePrefix(jsonFence)
            .removeSuffix(jsonFence).trim()
        val array = JSONObject(jsonText).optJSONArray("highlights") ?: JSONArray()
        return (0 until minOf(array.length(), 8)).mapNotNull { index ->
            val item = array.optJSONObject(index) ?: return@mapNotNull null
            val start = item.optInt("start_seconds", -1)
            val end = item.optInt("end_seconds", -1)
            val title = item.optString("title").trim()
            val tag = item.optString("tag", "Adventure").trim().ifBlank { "Adventure" }
            if (start < 0 || end <= start || end - start > 30 || title.isBlank()) null
            else SuggestedHighlight(start, end, title.take(80), tag.take(30))
        }
    }

    suspend fun generateCaption(
        region: String,
        targetLength: String,
        narrationLanguage: String,
        clips: List<ClipContext>
    ): String {
        require(clips.isNotEmpty()) { "Select at least one highlight." }

        val clipDetails = clips.joinToString(separator = "\n") { clip ->
            "- ${clip.timeRange}: ${clip.title} (moment: ${clip.tag}${clip.speedKmh?.let { "; speed: $it km/h" } ?: ""}${clip.altitudeM?.let { "; altitude: $it m" } ?: ""})"
        }
        val prompt = """
            You are RideStory, a careful and creative motorcycle travel storyteller.
            Write one social-media caption for a rider's adventure.
            Route/region: $region
            Requested reel length: $targetLength
            Requested language/style: $narrationLanguage
            Selected highlight metadata:
            $clipDetails

            Requirements:
            - Write in the requested language; if a style is specified, match it.
            - Keep it vivid, authentic, and ready to paste into a social post.
            - Mention only details supplied above. Do not invent weather, events, route distances,
              road conditions, safety outcomes, or landmarks not in the supplied metadata.
            - Do not encourage speeding or dangerous riding.
            - Add at most four relevant hashtags.
            - Return only the caption, without a heading or explanation.
        """.trimIndent()

        val model = Firebase.ai(backend = GenerativeBackend.googleAI())
            .generativeModel("gemini-2.5-flash")
        val response = model.generateContent(prompt)
        return response.text?.trim()?.takeIf { it.isNotEmpty() }
            ?: throw IllegalStateException("The AI model returned an empty caption.")
    }
}
