package com.example.data.voice

/**
 * Controls background research cadence while the user has explicitly started Live Guide.
 * It schedules no speech: refreshed context is only used to answer a later user turn.
 */
internal object NearbyResearchPacingPolicy {
    const val MIN_ATTEMPT_INTERVAL_MS = 45_000L
    const val MIN_MOVEMENT_METERS = 140.0
    const val MAX_CONTEXT_AGE_MS = 180_000L

    fun shouldRefresh(
        hasPreviousResearchFix: Boolean,
        distanceMeters: Double,
        ageSinceSuccessMillis: Long,
        ageSinceAttemptMillis: Long,
    ): Boolean {
        if (ageSinceAttemptMillis < MIN_ATTEMPT_INTERVAL_MS) return false
        if (!hasPreviousResearchFix) return true
        return distanceMeters >= MIN_MOVEMENT_METERS || ageSinceSuccessMillis >= MAX_CONTEXT_AGE_MS
    }
}
