package com.example.data.model

import java.util.UUID

data class ClubMember(
    val user: User,
    val role: String // "Admin" or "Road Captain" or "Member"
)

data class ClubGoal(
    val id: String,
    val name: String,
    val description: String,
    val target: Int,
    val current: Int,
    val unit: String
) {
    val progressPercent: Float get() = (current.toFloat() / target.coerceAtLeast(1)).coerceIn(0f, 1f)
}

data class ClubChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val user: User,
    val message: String,
    val timestamp: String
)

data class Club(
    val id: String,
    val name: String,
    val tagLine: String,
    val badgeUrl: String,
    val memberCount: Int,
    val city: String,
    val isJoined: Boolean = false,
    val members: List<ClubMember> = emptyList(),
    val goals: List<ClubGoal> = emptyList(),
    val chatMessages: List<ClubChatMessage> = emptyList()
)

data class LiveRideTelemetry(
    val isRecording: Boolean = false,
    val isPaused: Boolean = false,
    val currentSpeedKmH: Double = 0.0,
    val averageSpeedKmH: Double = 0.0,
    val topSpeedKmH: Double = 0.0,
    val distanceKm: Double = 0.0,
    val elapsedSeconds: Long = 0,
    val altitudeM: Int = 0,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val bearingDeg: Float = 0f,
    val hasGpsFix: Boolean = false,
    val leanAngleDeg: Int = 0, // Negative for left, positive for right
    val maxLeanAngleDeg: Int = 0,
    val caloriesBurned: Int = 0,
    val markedWaypoints: List<TelemetryWaypoint> = emptyList(),
    /** True when location permission is off and the HUD is showing simulated demo data. */
    val isDemo: Boolean = false,
    val elevationGainM: Int = 0
)

data class TelemetryWaypoint(
    val id: String = UUID.randomUUID().toString(),
    val label: String,
    val type: String, // "Fuel", "Photo", "Chai", "Scenic Pass"
    val timestamp: String,
    val speedKmH: Double,
    val altitudeM: Int
)

data class HighlightClip(
    val timestamp: String,
    val title: String,
    val speedKmh: Int,
    val altitudeM: Int,
    val tag: String,
    val isSelected: Boolean = true
)

data class AiGeneratedStory(
    val title: String,
    val region: String,
    val caption: String,
    val suggestedMusic: String,
    val highlightClips: List<HighlightClip>,
    val estimatedDurationSec: Int
)
