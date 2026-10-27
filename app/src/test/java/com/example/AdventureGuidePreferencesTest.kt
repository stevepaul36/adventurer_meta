package com.example

import android.content.Context
import com.example.data.voice.AdventureGuidePreferenceOptions
import com.example.data.voice.AdventureGuidePreferences
import com.example.data.voice.AdventureGuidePreferencesStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class AdventureGuidePreferencesTest {
    private val context = RuntimeEnvironment.getApplication()
    private lateinit var store: AdventureGuidePreferencesStore

    @Before
    fun resetPreferences() {
        context.getSharedPreferences("adventurer_guide_preferences", Context.MODE_PRIVATE)
            .edit().clear().commit()
        store = AdventureGuidePreferencesStore(context)
    }

    @Test
    fun defaultsFitMultiActivityOutdoorDomainAndPreferOnDemandSpeech() {
        val defaults = store.load()
        assertTrue(defaults.interests.contains("nature_ecology"))
        assertTrue(defaults.interests.contains("local_history"))
        assertEquals("on_demand", defaults.proactivity)
        assertEquals("mia", defaults.voicePreset)
    }

    @Test
    fun preferencesPersistAcrossStoreInstances() {
        val saved = store.save(
            AdventureGuidePreferences(
                interests = setOf("marine_underwater", "geology_landforms"),
                storyStyle = "technical_explainer",
                proactivity = "gentle_suggestions",
                voicePreset = "nora",
            )
        )
        val reloaded = AdventureGuidePreferencesStore(context).load()
        assertEquals(saved, reloaded)
        assertTrue(reloaded.interests.contains("marine_underwater"))
        assertEquals("technical_explainer", reloaded.storyStyle)
        assertEquals("nora", reloaded.voicePreset)
    }

    @Test
    fun rejectsUnknownPreferenceValuesAndNeverAllowsAnEmptyInterestProfile() {
        val saved = store.save(
            AdventureGuidePreferences(
                interests = setOf("unknown_topic"),
                storyStyle = "injected_prompt",
                proactivity = "always_speak_forever",
                voicePreset = "not_a_supported_voice",
            )
        )
        assertEquals(AdventureGuidePreferenceOptions.DEFAULT_INTERESTS, saved.interests)
        assertEquals("local_storyteller", saved.storyStyle)
        assertEquals("on_demand", saved.proactivity)
        assertEquals("mia", saved.voicePreset)
        assertFalse(saved.interests.isEmpty())
    }

    @Test
    fun spokenSummaryContainsOnlyPreferenceLabelsNotTripLocationData() {
        val summary = store.spokenSummary()
        assertTrue(summary.contains("Interests:"))
        assertTrue(summary.contains("Suggestion level: On demand"))
        assertFalse(summary.contains("latitude"))
        assertFalse(summary.contains("longitude"))
    }
}
