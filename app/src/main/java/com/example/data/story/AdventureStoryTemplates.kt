package com.example.data.story

import com.example.data.model.ActivityType

data class AdventureStoryTemplate(
    val id: String,
    val title: String,
    val description: String,
    val promptGuidance: String,
    val supportedActivities: Set<ActivityType>,
)

object AdventureStoryTemplates {
    private val allActivities = ActivityType.values().toSet()

    val all: List<AdventureStoryTemplate> = listOf(
        AdventureStoryTemplate(
            "epic_journey",
            "Epic Journey",
            "A beginning, a memorable middle, and a reflective ending.",
            "Use a clear narrative arc: anticipation, two or three real moments, and a personal reflection. Avoid inventing events.",
            allActivities,
        ),
        AdventureStoryTemplate(
            "road_trip",
            "Road Trip Chapters",
            "Stops, detours, scenery and the feeling of the journey.",
            "Structure the story as journey chapters with actual stops or visible transitions. Never invent road names, distances, traffic or detours.",
            setOf(ActivityType.Motorcycling, ActivityType.CarTrips, ActivityType.Cycling, ActivityType.IslandExploration, ActivityType.DestinationExploration),
        ),
        AdventureStoryTemplate(
            "trail_journal",
            "Trail Journal",
            "A grounded field diary for walks, hikes, climbs and rides.",
            "Focus on terrain and observed moments. Do not infer trail difficulty, altitude, distance, wildlife or whether a route is safe unless supplied facts support it.",
            setOf(ActivityType.Cycling, ActivityType.Walking, ActivityType.Trekking, ActivityType.ClimbingCaving, ActivityType.Motorcycling),
        ),
        AdventureStoryTemplate(
            "campfire_diary",
            "Campfire Diary",
            "Camp setup, small discoveries, and the quiet end of the day.",
            "Use an intimate journal tone around the moments supplied. Do not claim a location was an authorized campsite or assert weather/fire conditions.",
            setOf(ActivityType.Camping, ActivityType.Hybrid, ActivityType.CarTrips, ActivityType.Trekking),
        ),
        AdventureStoryTemplate(
            "island_escape",
            "Island & Coast Escape",
            "Coastlines, ferry transitions, shore stops and atmosphere.",
            "Highlight visible coastal or island scenes, transitions and personal reflections. Do not invent ferry schedules, water conditions, named beaches or opening times.",
            setOf(ActivityType.IslandExploration, ActivityType.Snorkeling, ActivityType.ScubaDiving, ActivityType.BoatingSailing, ActivityType.KayakingPaddling),
        ),
        AdventureStoryTemplate(
            "water_log",
            "Water Adventure Log",
            "A careful recap for paddling, snorkeling or dive-trip memories.",
            "Describe only observed or user-supplied surface moments. Never infer underwater depth, species, decompression status, currents or water safety from ordinary footage.",
            setOf(ActivityType.ScubaDiving, ActivityType.Snorkeling, ActivityType.KayakingPaddling, ActivityType.BoatingSailing, ActivityType.Fishing),
        ),
        AdventureStoryTemplate(
            "wildlife_fieldnotes",
            "Wildlife Field Notes",
            "A respectful nature-observation journal.",
            "Use neutral observation language. Do not name species unless confidently supplied/verified, and never expose sensitive wildlife coordinates.",
            setOf(ActivityType.WildlifeExploration, ActivityType.Hunting, ActivityType.Fishing, ActivityType.Trekking, ActivityType.Camping),
        ),
        AdventureStoryTemplate(
            "fishing_day",
            "Fishing Day",
            "The setting, patience, notes and catch details the user confirms.",
            "Only mention a catch or species if the user explicitly supplied it or it is unmistakably supported by inputs. Do not offer claims about legal limits or current fishing rules.",
            setOf(ActivityType.Fishing),
        ),
        AdventureStoryTemplate(
            "creator_reel",
            "Creator Reel",
            "A fast, shareable sequence with a strong opening and concise ending.",
            "Write for a short social reel with an immediate hook, short chapter titles and a concise ending. Avoid clickbait and unsupported factual claims.",
            allActivities,
        ),
    )

    fun forActivity(activity: ActivityType): List<AdventureStoryTemplate> =
        all.filter { activity in it.supportedActivities }

    fun byId(id: String): AdventureStoryTemplate =
        all.firstOrNull { it.id == id } ?: all.first()
}
