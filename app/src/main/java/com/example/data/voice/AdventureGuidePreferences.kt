package com.example.data.voice

import android.content.Context

data class AdventureGuidePreferences(
    val interests: Set<String> = AdventureGuidePreferenceOptions.DEFAULT_INTERESTS,
    val storyStyle: String = "local_storyteller",
    val proactivity: String = "on_demand",
    val voicePreset: String = "mia",
)

object AdventureGuidePreferenceOptions {
    val interestLabels = linkedMapOf(
        "local_history" to "Local history",
        "architecture_engineering" to "Architecture & engineering",
        "nature_ecology" to "Nature & ecology",
        "wildlife_observation" to "Wildlife watching",
        "geology_landforms" to "Geology & landforms",
        "local_food_culture" to "Food & local culture",
        "photography_viewpoints" to "Photo spots & viewpoints",
        "gear_campcraft" to "Gear & campcraft",
        "marine_underwater" to "Marine & underwater",
        "road_travel" to "Road trips & routes",
    )

    val storyStyleLabels = linkedMapOf(
        "local_storyteller" to "Local storyteller",
        "field_guide" to "Field guide",
        "technical_explainer" to "Technical explainer",
        "cinematic_journal" to "Cinematic journal",
        "wildlife_observer" to "Nature observer",
    )

    val proactivityLabels = linkedMapOf(
        "on_demand" to "On demand",
        "gentle_suggestions" to "Gentle suggestions",
        "frequent_suggestions" to "More suggestions",
    )

    val voiceLabels = linkedMapOf(
        "mia" to "Mia",
        "chloe" to "Chloe",
        "marcus" to "Marcus",
        "jake" to "Jake",
        "nora" to "Nora",
        "eleanor" to "Eleanor",
        "oliver" to "Oliver",
        "berlinda" to "Berlinda",
        "default" to "Default",
    )

    val DEFAULT_INTERESTS = setOf("local_history", "nature_ecology", "local_food_culture")
}

/** Local-only guide preferences; never uploaded as analytics or attached to exact location data. */
class AdventureGuidePreferencesStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("adventurer_guide_preferences", Context.MODE_PRIVATE)

    fun load(): AdventureGuidePreferences {
        val interests = prefs.getStringSet("interests", null)?.toSet()
            ?.filter { it in AdventureGuidePreferenceOptions.interestLabels.keys }
            ?.toSet()
            ?.takeIf { it.isNotEmpty() }
            ?: AdventureGuidePreferenceOptions.DEFAULT_INTERESTS
        val style = prefs.getString("story_style", "local_storyteller")
            ?.takeIf { it in AdventureGuidePreferenceOptions.storyStyleLabels }
            ?: "local_storyteller"
        val proactivity = prefs.getString("proactivity", "on_demand")
            ?.takeIf { it in AdventureGuidePreferenceOptions.proactivityLabels }
            ?: "on_demand"
        val voice = prefs.getString("voice_preset", "mia")
            ?.takeIf { it in AdventureGuidePreferenceOptions.voiceLabels }
            ?: "mia"
        return AdventureGuidePreferences(interests, style, proactivity, voice)
    }

    fun save(value: AdventureGuidePreferences): AdventureGuidePreferences {
        val safeInterests = value.interests
            .filter { it in AdventureGuidePreferenceOptions.interestLabels.keys }
            .toSet()
            .ifEmpty { AdventureGuidePreferenceOptions.DEFAULT_INTERESTS }
        val safeStyle = value.storyStyle.takeIf { it in AdventureGuidePreferenceOptions.storyStyleLabels } ?: "local_storyteller"
        val safeProactivity = value.proactivity.takeIf { it in AdventureGuidePreferenceOptions.proactivityLabels } ?: "on_demand"
        val safeVoice = value.voicePreset.takeIf { it in AdventureGuidePreferenceOptions.voiceLabels } ?: "mia"
        prefs.edit()
            .putStringSet("interests", safeInterests)
            .putString("story_style", safeStyle)
            .putString("proactivity", safeProactivity)
            .putString("voice_preset", safeVoice)
            .apply()
        return load()
    }

    fun spokenSummary(value: AdventureGuidePreferences = load()): String {
        val interests = value.interests
            .mapNotNull { AdventureGuidePreferenceOptions.interestLabels[it] }
            .joinToString(", ")
        val style = AdventureGuidePreferenceOptions.storyStyleLabels[value.storyStyle] ?: "Local storyteller"
        val proactivity = AdventureGuidePreferenceOptions.proactivityLabels[value.proactivity] ?: "On demand"
        val voice = AdventureGuidePreferenceOptions.voiceLabels[value.voicePreset] ?: "Default"
        return "Interests: $interests. Story style: $style. Suggestion level: $proactivity. Realtime voice: $voice."
    }
}
