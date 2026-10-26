package com.example.data.story

import android.util.Base64
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Meta Model API client (OpenAI-compatible chat completions — https://dev.meta.ai/docs).
 * Also works with any OpenAI-compatible host serving Meta open-weights models
 * (Fireworks, Baseten, Modal…) by changing MODEL_API_BASE_URL / MODEL_TEXT / MODEL_VISION in .env.
 */
class MetaAiClient(
    private val apiKey: String = BuildConfig.MODEL_API_KEY,
    baseUrl: String = BuildConfig.MODEL_API_BASE_URL,
    val textModel: String = BuildConfig.MODEL_TEXT,
    val visionModel: String = BuildConfig.MODEL_VISION,
) {
    private val endpoint = baseUrl.trimEnd('/') + "/chat/completions"
    private val http = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val isConfigured: Boolean get() = apiKey.isNotBlank() && !apiKey.startsWith("MY_")

    data class MomentLabel(
        val id: String,
        val label: String,
        val description: String,
        val score: Double,
        val tags: List<String>,
        val faces: Boolean,
        val plates: Boolean,
    )

    suspend fun labelMoments(moments: List<StudioMoment>, location: String): List<MomentLabel> = withContext(Dispatchers.IO) {
        val batch = moments.filter { it.thumbnailPath != null }.take(12)
        require(batch.isNotEmpty()) { "No keyframes to send" }
        val content = JSONArray().put(
            JSONObject().put("type", "text").put(
                "text",
                "You are the editor for RideStory AI, which turns long motorcycle ride footage into short highlight reels. " +
                    "The ride took place around: ${location.ifBlank { \"unknown\" }}. Below are ${batch.size} keyframes, one per candidate moment. " +
                    "For every image return an object with: \"index\" (1-based), \"label\" (3-6 vivid words, no emojis), " +
                    "\"description\" (one sentence of what is visible), \"score\" (0-10: how good this moment is for a highlight reel), " +
                    "\"tags\" (up to 4 lowercase words), " +
                    "\"privacy\" ({\"faces\": boolean, \"plates\": boolean}). Return ONLY a JSON array."
            )
        )
        batch.forEachIndexed { i, m ->
            val b64 = Base64.encodeToString(File(m.thumbnailPath!!).readBytes(), Base64.NO_WRAP)
            content.put(JSONObject().put("type", "text").put("text", "Image ${i + 1} — t=${m.peakSec.toInt()}s"))
            content.put(JSONObject().put("type", "image_url").put("image_url", JSONObject().put("url", "data:image/jpeg;base64,$b64")))
        }
        val reply = chat(visionModel, JSONArray().put(JSONObject().put("role", "user").put("content", content)), 0.3, 2000)
        val json = AiJson.extract(reply)
        val arr = if (json.trimStart().startsWith("[")) JSONArray(json) else JSONObject(json).optJSONArray("moments") ?: JSONArray()
        (0 until arr.length()).mapNotNull { k ->
            val o = arr.optJSONObject(k) ?: return@mapNotNull null
            val source = batch.getOrNull(o.optInt("index", 0) - 1) ?: return@mapNotNull null
            val privacy = o.optJSONObject("privacy")
            MomentLabel(
                id = source.id,
                label = o.optString("label").take(60),
                description = o.optString("description").take(240),
                score = o.optDouble("score", 0.0).coerceIn(0.0, 10.0),
                tags = o.optJSONArray("tags")?.let { t -> (0 until t.length()).map { t.optString(it).take(20) } }?.take(4) ?: emptyList(),
                faces = privacy?.optBoolean("faces") ?: false,
                plates = privacy?.optBoolean("plates") ?: false,
            )
        }
    }

    suspend fun writeStory(title: String, location: String, language: String, moments: List<StudioMoment>, sourceSec: Double): StoryCopy =
        withContext(Dispatchers.IO) {
            val reelSec = moments.sumOf { it.durationSec }
            val prompt = buildString {
                appendLine("Write social copy for a ride highlight reel.")
                appendLine("Title: $title")
                appendLine("Location: $location")
                appendLine("Source footage: ${sourceSec.toInt()} s; reel: ${reelSec.toInt()} s; clips: ${moments.size}.")
                appendLine("Moments in order:")
                moments.forEach { m -> appendLine("- ${m.peakSec.toInt()}s: ${m.label}${if (m.aiLabeled) " — ${m.description}" else ""}") }
                appendLine()
                append("Write everything in $language. Be warm and specific; do not invent facts. ")
                append("Return ONLY JSON: {\"caption\": string, \"diary\": string, \"hashtags\": string[], ")
                append("\"chapters\": string[], \"narration\": string}")
            }
            val messages = JSONArray()
                .put(JSONObject().put("role", "system").put("content", "You are a travel storyteller for adventure riders in India. Respond with ONLY valid JSON."))
                .put(JSONObject().put("role", "user").put("content", prompt))
            val o = JSONObject(AiJson.extract(chat(textModel, messages, 0.8, 1500)))
            fun strings(key: String) = o.optJSONArray(key)?.let { a -> (0 until a.length()).map { a.optString(it) } } ?: emptyList()
            val chapters = strings("chapters").map { it.take(80) }
            StoryCopy(
                caption = o.optString("caption").take(600),
                diary = o.optString("diary").take(4000),
                hashtags = strings("hashtags").take(10).map { if (it.startsWith("#")) it else "#$it" },
                chapterTitles = moments.indices.map { i -> chapters.getOrNull(i)?.takeIf { it.isNotBlank() } ?: moments[i].label },
                narration = o.optString("narration").take(2000),
                fromAi = true,
            )
        }

    private fun chat(model: String, messages: JSONArray, temperature: Double, maxTokens: Int): String {
        check(isConfigured) { "MODEL_API_KEY is not set in .env" }
        val body = JSONObject()
            .put("model", model)
            .put("messages", messages)
            .put("temperature", temperature)
            .put("max_tokens", maxTokens)
            .toString()
            .toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(endpoint)
            .header("Authorization", "Bearer $apiKey")
            .post(body)
            .build()
        val text = http.newCall(request).execute().use { res ->
            val raw = res.body?.string().orEmpty()
            if (!res.isSuccessful) throw IllegalStateException("Model API ${res.code}: ${raw.take(300)}")
            raw
        }
        val message = JSONObject(text).getJSONArray("choices").getJSONObject(0).getJSONObject("message")
        return when (val content = message.opt("content")) {
            is String -> content
            is JSONArray -> (0 until content.length()).joinToString("") { content.optJSONObject(it)?.optString("text").orEmpty() }
            else -> ""
        }
    }
}
