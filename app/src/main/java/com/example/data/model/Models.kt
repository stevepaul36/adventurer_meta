package com.example.data.model

import java.util.UUID

data class User(
    val id: String,
    val name: String,
    val avatarUrl: String,
    val bikeModel: String = "Royal Enfield Himalayan 450",
    val location: String = "Bengaluru, India"
)

data class RideStats(
    val distanceKm: Double,
    val duration: String,
    val elevationGainM: Int,
    val topSpeedKmH: Double = 98.0,
    val maxLeanAngleDeg: Int = 34
)

data class Comment(
    val id: String = UUID.randomUUID().toString(),
    val user: User,
    val text: String,
    val timestamp: String = "Just now"
)

data class Post(
    val id: String = UUID.randomUUID().toString(),
    val user: User,
    val timestamp: String,
    val caption: String,
    val imageUrl: String? = null,
    val rideStats: RideStats? = null,
    val likes: Int = 0,
    val comments: List<Comment> = emptyList(),
    val isLikedByMe: Boolean = false
)

data class RideShare(
    val id: String = UUID.randomUUID().toString(),
    val user: User,
    val timestamp: String,
    val title: String,
    val mapImageUrl: String,
    val rideStats: RideStats,
    val likes: Int = 0,
    val comments: List<Comment> = emptyList(),
    val isLikedByMe: Boolean = false
)

data class StoryWaypoint(
    val name: String,
    val altitudeM: Int,
    val note: String,
    val photoUrl: String? = null
)

data class RideStory(
    val id: String = UUID.randomUUID().toString(),
    val user: User,
    val title: String,
    val description: String,
    val coverImageUrl: String,
    val date: String,
    val waypoints: List<StoryWaypoint>,
    val totalDistanceKm: Double,
    val highestAltitudeM: Int
)

sealed class FeedItem {
    data class PostItem(val post: Post) : FeedItem()
    data class RideShareItem(val rideShare: RideShare) : FeedItem()
    data class StoryItem(val story: RideStory) : FeedItem()
}

enum class ActivityType(val displayName: String, val iconName: String) {
    Motorcycling("Motorcycling", "two_wheeler"),
    Cycling("Cycling", "pedal_bike"),
    Walking("Walking", "directions_walk"),
    Trekking("Trekking / Hiking", "hiking"),
    CarTrips("Road Trips", "directions_car"),
    Hybrid("Multisport Hybrid", "snowshoeing")
}

enum class BadgeTier {
    Bronze, Silver, Gold, Platinum
}

data class Badge(
    val id: String,
    val activityType: ActivityType,
    val name: String,
    val tier: BadgeTier,
    val description: String,
    val isUnlocked: Boolean = false,
    val progressPercent: Float = 0f
)

data class Trophy(
    val id: String,
    val title: String,
    val subtitle: String,
    val colorHex: Long,
    val tier: BadgeTier,
    val unlockedDate: String? = null,
    val description: String
)

data class RiderGamificationState(
    val level: Int = 14,
    val currentXp: Int = 4850,
    val nextLevelXp: Int = 6000,
    val currentStreakDays: Int = 18,
    val totalRides: Int = 92,
    val totalDistanceKm: Double = 14320.0,
    val totalElevationM: Int = 42800,
    val unlockedBadgesCount: Int = 16,
    val trophies: List<Trophy> = emptyList(),
    val badges: List<Badge> = emptyList()
)

data class FriendRequest(
    val id: String,
    val from: User,
    val timestamp: String
)
