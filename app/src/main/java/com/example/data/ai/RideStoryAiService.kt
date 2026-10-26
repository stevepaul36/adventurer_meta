package com.example.data.ai

import com.google.firebase.ai.FirebaseAI
import com.google.firebase.ai.GenerativeBackend

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

        val model = FirebaseAI.getInstance(GenerativeBackend.googleAI())
            .generativeModel("gemini-2.5-flash")
        val response = model.generateContent(prompt)
        return response.text?.trim()?.takeIf { it.isNotEmpty() }
            ?: throw IllegalStateException("The AI model returned an empty caption.")
    }
}
