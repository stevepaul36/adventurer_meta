package com.example.analytics

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

/**
 * Privacy-conscious product funnel instrumentation.
 * Never send destination names, exact coordinates, contact details or route geometry as analytics.
 * Local counters remain available on an unconfigured developer build; Firebase receives events
 * only when the Firebase app/Analytics service is configured.
 */
object AdventureFunnelAnalytics {
    private const val PREFS = "adventure_funnel_metrics"
    private val trackedEvents = listOf(
        "adventure_app_open",
        "trip_plan_created",
        "activity_started",
        "story_exported",
        "story_shared",
        "story_share_sheet_opened",
        "place_saved",
        "safety_check_in_completed",
        "creator_guide_draft_created",
        "community_event_draft_created",
        "voice_assistant_open",
        "voice_push_to_talk_started",
        "voice_hands_free_enabled",
        "voice_command_executed",
        "live_voice_connect_requested",
        "live_voice_connected",
        "live_voice_disconnected",
        "live_voice_nearby_prefetch",
        "voice_guide_preferences_saved",
        "voice_place_fact_requested",
    )

    fun log(context: Context, eventName: String, params: Map<String, Any> = emptyMap()) {
        val safeName = eventName.filter { it.isLetterOrDigit() || it == '_' }.take(40)
        if (safeName.isBlank()) return

        val preferences = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val countKey = "count_$safeName"
        preferences.edit()
            .putInt(countKey, preferences.getInt(countKey, 0) + 1)
            .putLong("last_$safeName", System.currentTimeMillis())
            .apply()

        val bundle = Bundle()
        params.forEach { (key, value) ->
            val safeKey = key.filter { it.isLetterOrDigit() || it == '_' }.take(40)
            if (safeKey.isNotBlank()) {
                when (value) {
                    is String -> bundle.putString(safeKey, value.take(100))
                    is Int -> bundle.putInt(safeKey, value)
                    is Long -> bundle.putLong(safeKey, value)
                    is Float -> bundle.putDouble(safeKey, value.toDouble())
                    is Double -> bundle.putDouble(safeKey, value)
                    is Boolean -> bundle.putString(safeKey, value.toString())
                }
            }
        }
        runCatching {
            FirebaseAnalytics.getInstance(context.applicationContext).logEvent(safeName, bundle)
        }
    }

    fun localSnapshot(context: Context): Map<String, Int> {
        val preferences = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return trackedEvents.associateWith { preferences.getInt("count_$it", 0) }
    }
}
