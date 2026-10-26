package com.example.data.ai

import android.content.Context
import android.net.Uri
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import com.google.firebase.Firebase

/**
 * Firebase AI Logic wrapper for turning ride metadata into a social-ready story.
 *
 * Firebase must be configured for this Android app and the Gemini Developer API
 * enabled in Firebase AI Logic before requests can succeed.
 */
object RideStoryAiService {
    data class ClipContext(
        val timeRange: String,
        val title: String,
        val tag: String,
        val speedKmh: Int,
        val altitudeM: Int
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
        require(declaredSize <= 15L * 1024L * 1024L || declaredSize < 0L) {
            "For this demo, choose a video smaller than 15 MB."
        }
        val bytes = resolver.openInputStream(videoUri)?.use { it.readBytes() }
            ?: throw IllegalArgumentException("Unable to read the selected video.")
        require(bytes.size <= 15 * 1024 * 1024) {
            "For this demo, choose a video smaller than 15 MB."
        }

        val clipDetails = clips.joinToString(separator = "\\n") { clip ->
            "- ${clip.timeRange}: ${clip.title} (tag: ${clip.tag}; speed metadata: ${clip.speedKmh} km/h; altitude metadata: ${clip.altitudeM} m)"
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
        val model = com.google.firebase.Firebase.ai(backend = GenerativeBackend.googleAI())
            .generativeModel("gemini-2.5-flash")
        val request = content {
            inlineData(bytes, mimeType)
            text(prompt)
        }
        val response = model.generateContent(request)
        return response.text?.trim()?.takeIf { it.isNotEmpty() }
            ?: throw IllegalStateException("The AI model returned an empty caption.")
    }

    suspend fun generateCaption(
        region: String,
        targetLength: String,
        narrationLanguage: String,
        clips: List<ClipContext>
    ): String {
        require(clips.isNotEmpty()) { "Select at least one highlight." }

        val clipDetails = clips.joinToString(separator = "\n") { clip ->
            "- ${clip.timeRange}: ${clip.title} (moment: ${clip.tag}; speed: ${clip.speedKmh} km/h; altitude: ${clip.altitudeM} m)"
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
