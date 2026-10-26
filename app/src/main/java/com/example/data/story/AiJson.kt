package com.example.data.story

/** Pulls the first complete JSON object/array out of a model reply (handles ```json fences and chatter). */
object AiJson {
    fun extract(text: String): String {
        val fenced = Regex("```(?:json)?\\s*([\\s\\S]*?)```", RegexOption.IGNORE_CASE).find(text)?.groupValues?.get(1)
        val candidate = fenced ?: text
        val start = candidate.indexOfFirst { it == '{' || it == '[' }
        require(start >= 0) { "No JSON found in model response" }
        val open = candidate[start]
        val close = if (open == '{') '}' else ']'
        var depth = 0
        var inString = false
        var escaped = false
        for (i in start until candidate.length) {
            val ch = candidate[i]
            if (inString) {
                when {
                    escaped -> escaped = false
                    ch == '\\' -> escaped = true
                    ch == '"' -> inString = false
                }
                continue
            }
            when (ch) {
                '"' -> inString = true
                open -> depth++
                close -> if (--depth == 0) return candidate.substring(start, i + 1)
            }
        }
        throw IllegalArgumentException("Unterminated JSON in model response")
    }
}
