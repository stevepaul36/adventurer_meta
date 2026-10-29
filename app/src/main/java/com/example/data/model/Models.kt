package com.example.data.model

import java.util.UUID

data class User(
    val id: String,
    val name: String,
    val avatarUrl: String,
    val bikeModel: String = "Royal Enfield Himalayan 450",
    val location: String = "Bengaluru, India",
    val primaryActivity: String = "Motorcycling"
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

enum class ActivityType(
    val displayName: String,
    val iconName: String,
    val homeTitle: String,
    val homeHint: String,
    val trackerSubtitle: String,
    val supportsLeanAngle: Boolean = false
) {
    Motorcycling(
        "Motorcycling", "two_wheeler",
        "Start a motorcycle ride",
        "Record your GPS route, distance, speed, elevation and motorcycle-specific telemetry.",
        "Motorcycle GPS session · lean-angle estimate is informational, not a riding-safety system.",
        supportsLeanAngle = true
    ),
    Cycling(
        "Cycling", "pedal_bike",
        "Start a cycling adventure",
        "Record a road, gravel, mountain-bike or bikepacking route with distance and elevation.",
        "Cycling GPS session · cadence and heart-rate sensors are not connected yet."
    ),
    Walking(
        "Walking", "directions_walk",
        "Explore on foot",
        "Discover local paths, waterfront walks, viewpoints and hidden neighbourhood places.",
        "Walking GPS session · use downloaded maps and plan for areas without mobile coverage."
    ),
    Trekking(
        "Trekking / Hiking", "hiking",
        "Start a hike or trek",
        "Record your trail, time, waypoints and elevation; plan water, weather and turnaround points.",
        "Hiking GPS session · offline topographic map packs and authoritative trail conditions are roadmap items."
    ),
    CarTrips(
        "Car road trips", "directions_car",
        "Start a road trip",
        "Explore a new place with scenic drives, viewpoints, food stops and a flexible multi-stop itinerary.",
        "Road-trip GPS session · live traffic, EV/fuel integrations and road-closure feeds are not wired yet."
    ),
    Hybrid(
        "Multi-activity adventure", "explore",
        "Start a multi-activity adventure",
        "Connect transfers, rides, walks, camps and points of interest into one trip story.",
        "Basic GPS session · multi-segment trip planning is a product roadmap item."
    ),
    Camping(
        "Camping & overlanding", "camping",
        "Plan a camp or overland trip",
        "Collect campsites, overnight stops, gear and food checklists, water points and fallback plans.",
        "Basic GPS session only · campsite availability, booking and permit data require provider integration."
    ),
    Fishing(
        "Fishing & angling", "fishing",
        "Plan a fishing outing",
        "Save access points and private catch notes; check local licences, seasons and conditions.",
        "Basic location log only · tide, catch-rule and fishing-forecast feeds are not integrated."
    ),
    Hunting(
        "Hunting & wildlife tracking", "wildlife",
        "Plan a responsible nature outing",
        "Keep a private plan and observation log; verify licences, seasons, land access and protected areas with official sources.",
        "Basic location log only · no legal status is verified by this prototype."
    ),
    ScubaDiving(
        "Scuba diving", "scuba_diving",
        "Plan a dive-site trip",
        "Save dive sites, entry/exit notes and buddy checklists; keep sensitive sites private.",
        "Surface GPS only in this prototype. Use certified dive equipment/training for depth, gas, ascent and decompression decisions."
    ),
    Snorkeling(
        "Snorkelling", "snorkeling",
        "Explore a snorkelling spot",
        "Plan accessible shore entries, weather windows and reef-friendly visits.",
        "Surface location logging only · currents, visibility and marine advisories are not integrated."
    ),
    KayakingPaddling(
        "Kayaking & paddling", "kayaking",
        "Plan a paddle adventure",
        "Save launch/landing points, portages, take-out options and expected-return check-ins.",
        "Basic GPS session · marine forecasts, current and water-level sources are roadmap items."
    ),
    BoatingSailing(
        "Boating & sailing", "sailing",
        "Plan a boating trip",
        "Plan waterway stops and return timing with a vessel-specific pre-departure checklist.",
        "Basic GPS session only · this is not a marine chartplotter or navigation-safety system."
    ),
    IslandExploration(
        "Island & coastal exploring", "island",
        "Explore a coast or island",
        "Build around beaches, viewpoints, ferries, local food, walks, snorkelling and weather windows.",
        "Basic GPS session · ferry schedules and island service data require live source integration."
    ),
    DestinationExploration(
        "New-place exploring", "explore",
        "Discover somewhere new",
        "Find landmarks, local favourites, food, viewpoints and short adventures that fit your time and budget.",
        "Basic GPS session · place results and opening hours must be verified through connected data sources."
    ),
    WildlifeExploration(
        "Wildlife & nature watching", "nature",
        "Explore the outdoors",
        "Record wildlife observations while protecting exact locations of sensitive species and habitats.",
        "Basic location log only · species identification is assistive and needs human/source verification."
    ),
    ClimbingCaving(
        "Climbing & caving", "terrain",
        "Plan an exploration session",
        "Keep access notes, equipment checks, entry/exit plans and expected-return timing.",
        "Basic session only · this prototype is not a climbing, cave-survey or technical-rescue system."
    );

    val usesSurfaceLocationOnly: Boolean
        get() = this == ScubaDiving || this == Snorkeling
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
