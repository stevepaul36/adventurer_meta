package com.example.data.repository

import com.example.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class AdventurerRepository {

    val currentUser = User(
        id = "u1",
        name = "Alex 'Torque' Rider",
        avatarUrl = "https://images.unsplash.com/photo-1568602471122-7832951cc4c5?auto=format&fit=crop&w=300&q=80",
        bikeModel = "Royal Enfield Himalayan 450 (Kamet White)",
        location = "Bengaluru, India"
    )

    val friendUsers = listOf(
        User("u2", "Mia Torque", "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=300&q=80", "KTM 390 Adventure", "Pune, Maharashtra"),
        User("u3", "Clutch Cargo", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=300&q=80", "BMW R1250 GS Adventure", "Shimla, HP"),
        User("u4", "Vance Velocity", "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=300&q=80", "Triumph Tiger 900 Rally Pro", "Mumbai, Maharashtra", "Motorcycling"),
        User("u5", "Dr. Tanya Sen", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=300&q=80", "Wilderness medicine kit", "Leh, Ladakh", "Wilderness medicine"),
        User("u6", "Leah Ridge", "https://images.unsplash.com/photo-1531123897727-8f129e1688ce?auto=format&fit=crop&w=300&q=80", "Alpine trekking kit", "Canmore, Alberta", "Trekking"),
        User("u7", "Noah Tide", "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?auto=format&fit=crop&w=300&q=80", "Sea kayak", "Squamish, British Columbia", "Kayaking"),
        User("u8", "Sage Wilds", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=300&q=80", "Backcountry camp kit", "Algonquin, Ontario", "Camping & wildlife")
    )

    // Demo follow graph; state lives in the repository so it survives navigation between screens.
    private val _followedAdventurerIds = MutableStateFlow(setOf("u2", "u3"))
    val followedAdventurerIds: StateFlow<Set<String>> = _followedAdventurerIds.asStateFlow()

    private val initialExpeditions = listOf(
        ExpeditionRoute(
            id = "leh-ladakh",
            title = "Leh-Ladakh Trans-Himalayan Circuit",
            region = "North Himalayas",
            state = "Ladakh / Himachal Pradesh",
            distanceKm = 1240,
            durationDays = "10 Days",
            highestPass = "Khardung La (17,982 ft) & Umling La (19,024 ft)",
            altitudeFt = 19024,
            difficulty = "Extreme",
            planA = "Manali - Rohtang - Jispa - Sarchu - Tanglang La - Leh - Nubra - Pangong",
            planB = "Srinagar - Sonamarg - Zoji La - Kargil - Leh (Lower elevation acclimation)",
            safetyScore = 9.4,
            terrain = "Glacial river crossings, high mountain passes, gravel shale, zero network valleys",
            landmarks = listOf("Magnetic Hill", "Diskit Monastery", "Pangong Tso Lake", "Khardung La Summit"),
            recommendedBike = "Royal Enfield Himalayan 450 / KTM 390 Adventure / BMW GS",
            oxygenAlert = true,
            imageUrl = "https://images.unsplash.com/photo-1544735716-392fe2489ffa?auto=format&fit=crop&w=1000&q=80",
            roles = ExpeditionCrew("Vikram 'Khar' Rathore", "Dr. Tanya Sen (Wilderness Medic)", "Rajinder Bullet Works", "Aarav FPV Drone"),
            isBookmarked = true
        ),
        ExpeditionRoute(
            id = "spiti-valley",
            title = "Spiti Valley Cold Desert & Monasteries",
            region = "Inner Himalayas",
            state = "Himachal Pradesh",
            distanceKm = 890,
            durationDays = "7 Days",
            highestPass = "Kunzum La (14,931 ft) & Chicham Bridge",
            altitudeFt = 14931,
            difficulty = "Extreme",
            planA = "Shimla - Kinnaur - Tabo - Kaza - Hikkim - Kunzum Pass - Chandra Taal",
            planB = "Manali reverse exit via Atal Tunnel in case of river mallam water log",
            safetyScore = 9.1,
            terrain = "Loose scree switchbacks, unpaved canyon gorge, high-altitude arid desert",
            landmarks = listOf("Hikkim Highest Post Office", "Key Monastery", "Chandra Taal Lake", "Chicham Gorge"),
            recommendedBike = "Dual Sport 250cc-650cc / High Ground Clearance",
            oxygenAlert = true,
            imageUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?auto=format&fit=crop&w=1000&q=80",
            roles = ExpeditionCrew("Kabir Thapa", "Neha Verma", "Manoj Singh", "Siddharth Rao")
        ),
        ExpeditionRoute(
            id = "western-ghats",
            title = "Western Ghats Monsoon Rain & Ridge Odyssey",
            region = "Sahyadri & Malabar",
            state = "Maharashtra / Karnataka / Goa",
            distanceKm = 760,
            durationDays = "5 Days",
            highestPass = "Amboli Ghat & Varandha Pass",
            altitudeFt = 4600,
            difficulty = "Moderate",
            isWomenFocused = true,
            planA = "Pune - Bhor - Varandha Ghat - Mahabaleshwar - Amboli - Chorla - Goa",
            planB = "NH-66 Coastal detour if heavy monsoon flash floods occur in Tamhini",
            safetyScore = 9.7,
            terrain = "Lush rainforest hairpins, cascading waterfalls, dense mist asphalt",
            landmarks = listOf("Thoseghar Waterfalls", "Kaas Plateau of Flowers", "Chorla Mist Pass"),
            recommendedBike = "Adventure Tourer / Scrambler / Wet Grip Tyres",
            monsoonAlert = true,
            imageUrl = "https://images.unsplash.com/photo-1519681393784-d120267933ba?auto=format&fit=crop&w=1000&q=80",
            roles = ExpeditionCrew("Ananya Kulkarni", "Pooja Hegde", "Suresh Deshmukh", "Priya Nair"),
            isBookmarked = true
        ),
        ExpeditionRoute(
            id = "rann-of-kutch",
            title = "Rann of Kutch & Dholavira Flamingo Road",
            region = "West Desert",
            state = "Gujarat",
            distanceKm = 680,
            durationDays = "4 Days",
            highestPass = "Kala Dungar (Black Hill - 1,510 ft)",
            altitudeFt = 1510,
            difficulty = "Moderate",
            planA = "Bhuj - Khavda - Kala Dungar - White Rann of Kutch - Flamingo Road - Dholavira",
            planB = "State Highway 42 detour if salt marshes remain waterlogged",
            safetyScore = 9.6,
            terrain = "High-speed endless straightaways, crystalline white salt flats, desert crosswinds",
            landmarks = listOf("White Rann Sunset Point", "Dholavira Harappan Citadel", "Road to Heaven"),
            recommendedBike = "Any Cruiser / Adventure Tourer with tubeless tyres",
            imageUrl = "https://images.unsplash.com/photo-1509316975850-ff9c5deb0cd9?auto=format&fit=crop&w=1000&q=80",
            roles = ExpeditionCrew("Harshil Patel", "Deepak Solanki", "Ketan Bhai (Mechanic)", "Aditya Shah")
        )
    )


    private val initialSafetyTips = listOf(
        MountainSafetyTip(
            id = "tip1",
            title = "Acute Mountain Sickness (AMS) Protocol",
            category = "AMS",
            shortAdvice = "Ascend gradually, hydrate with ORS/garlic soup, never sleep higher than planned.",
            fullInstructions = "Above 10,000 ft, your body needs 24-48 hours to acclimatize. Watch for headaches, nausea, or dizziness. If symptoms persist or worsen into breathlessness at rest, immediately descend at least 1,500 ft. Carry Diamox as advised by your physician."
        ),
        MountainSafetyTip(
            id = "tip2",
            title = "Glacial Water Crossing Technique (Mallams)",
            category = "River Crossing",
            shortAdvice = "Cross early in the morning before glacial melt surges; walk the line first.",
            fullInstructions = "In Ladakh and Spiti, streams surge after 1 PM due to solar melting. Inspect the rocks on foot first. Keep momentum in 1st or 2nd gear with steady throttle. Never dump the clutch mid-water. If bike stalls, do NOT push start immediately—check airbox for water ingestion."
        ),
        MountainSafetyTip(
            id = "tip3",
            title = "Sub-Zero Cold Weather Cold Starts",
            category = "Cold Weather",
            shortAdvice = "Keep battery warm overnight; turn ignition on 3 times to prime EFI pump.",
            fullInstructions = "Freezing temperatures sap 40% of lead-acid and lithium battery cranking power. Disconnect battery negative terminal overnight if camping in sub-zero Jispa/Sarchu. Let bike idle for 3 minutes before hitting throttle."
        )
    )



    private val initialPosts = listOf(
        Post(
            id = "p1",
            user = friendUsers[0], // Mia Torque
            timestamp = "2 hours ago",
            caption = "Conquered the switchbacks of Varandha Ghat during heavy Sahyadri mist! The new rain tyres gripped like glue. Coffee break at the ridge waterfall ☕️🌧️",
            imageUrl = "https://images.unsplash.com/photo-1558981403-c5f9899a28bc?auto=format&fit=crop&w=1000&q=80",
            rideStats = RideStats(142.5, "3h 40m", 1180, 104.0, 38),
            likes = 142,
            comments = listOf(
                Comment("c1", friendUsers[1], "Epic line through the hairpin! Did you get rain gear on time?"),
                Comment("c2", currentUser, "Looking solid Mia! That Varandha pass view is unbeatable.")
            ),
            isLikedByMe = true
        ),
        Post(
            id = "p2",
            user = currentUser,
            timestamp = "Yesterday",
            caption = "Early morning sprint up Nandi Hills before dawn broke. Chilly 14°C at the summit, bike breathing crisp air! Ready for the weekend Western Ghats circuit. 🏍️💨",
            imageUrl = "https://images.unsplash.com/photo-1568772585407-9361f9bf3a87?auto=format&fit=crop&w=1000&q=80",
            rideStats = RideStats(68.0, "1h 45m", 480, 96.0, 34),
            likes = 289,
            comments = listOf(
                Comment("c3", friendUsers[2], "Clean lines Alex! Next stop Chikmagalur coffee trails?")
            ),
            isLikedByMe = true
        )
    )

    private val initialRideShares = listOf(
        RideShare(
            id = "rs1",
            user = friendUsers[1], // Clutch Cargo
            timestamp = "4 hours ago",
            title = "Kunzum La to Chandra Taal Gravel Blast",
            mapImageUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?auto=format&fit=crop&w=1000&q=80",
            rideStats = RideStats(94.0, "2h 55m", 1450, 88.0, 31),
            likes = 312,
            comments = listOf(
                Comment("c4", currentUser, "That water crossing near Batal is always gnarly! Great ride Clutch.")
            )
        )
    )

    private val initialRideStory = RideStory(
        id = "story-ladakh",
        user = currentUser,
        title = "Pangong to Umling La: The 19,000 ft Odyssey",
        description = "Three riders pushing single-cylinder dual-sports through oxygen-starved shale, icy wind tunnels, and the world's highest motorable pass.",
        coverImageUrl = "https://images.unsplash.com/photo-1544735716-392fe2489ffa?auto=format&fit=crop&w=1000&q=80",
        date = "Oct 2026",
        waypoints = listOf(
            StoryWaypoint("Pangong Tso Blue Lake", 14270, "Crisp morning start along the saltwater shore"),
            StoryWaypoint("Chushul Military Pass", 14400, "Permit checks and high altitude fuel replenishment"),
            StoryWaypoint("Umling La Summit", 19024, "Sub-zero windchill, zero engine hesitation at the roof of the world")
        ),
        totalDistanceKm = 248.0,
        highestAltitudeM = 5798
    )

    private val _feed = MutableStateFlow<List<FeedItem>>(
        listOf(
            FeedItem.StoryItem(initialRideStory),
            FeedItem.PostItem(initialPosts[0]),
            FeedItem.RideShareItem(initialRideShares[0]),
            FeedItem.PostItem(initialPosts[1])
        )
    )
    val feed: StateFlow<List<FeedItem>> = _feed.asStateFlow()

    private val _expeditions = MutableStateFlow(initialExpeditions)
    val expeditions: StateFlow<List<ExpeditionRoute>> = _expeditions.asStateFlow()

    private val _emergencyStations = MutableStateFlow(emptyList<EmergencyStation>())
    val emergencyStations: StateFlow<List<EmergencyStation>> = _emergencyStations.asStateFlow()

    private val _distressSignals = MutableStateFlow(emptyList<RiderDistressSignal>())
    val distressSignals: StateFlow<List<RiderDistressSignal>> = _distressSignals.asStateFlow()

    private val _roadsideServices = MutableStateFlow(emptyList<RoadsideServiceEntity>())
    val roadsideServices: StateFlow<List<RoadsideServiceEntity>> = _roadsideServices.asStateFlow()

    val safetyTips = initialSafetyTips

    private val _gamification = MutableStateFlow(
        RiderGamificationState(
            level = 14,
            currentXp = 4850,
            nextLevelXp = 6000,
            currentStreakDays = 18,
            totalRides = 94,
            totalDistanceKm = 14320.0,
            totalElevationM = 42800,
            unlockedBadgesCount = 6,
            trophies = listOf(
                Trophy("tr1", "Golden Piston of Umling La", "19,024 ft Summit Master", 0xFFFFD700, BadgeTier.Platinum, "Oct 2026", "Conquered the world's highest asphalt pass with zero recovery incidents."),
                Trophy("tr2", "Silver Compass of the Sahyadris", "Monsoon Ridge Veteran", 0xFFC0C0C0, BadgeTier.Gold, "Sep 2026", "Clocked over 2,500 km of heavy monsoon mountain passes without a slip."),
                Trophy("tr3", "Bronze Carburetor of Rohtang", "Trans-Himalayan Trailblazer", 0xFFCD7F32, BadgeTier.Silver, "Aug 2026", "First major alpine crossing exceeding 13,000 ft elevation gain.")
            ),
            badges = listOf(
                Badge("b1", ActivityType.Motorcycling, "Himalayan Summit Master", BadgeTier.Platinum, "Ride past 18,000 ft altitude in Ladakh", true, 1.0f),
                Badge("b2", ActivityType.Motorcycling, "Ghats Corner Carver", BadgeTier.Gold, "Clock 35°+ lean angles on 500+ hairpins", true, 1.0f),
                Badge("b3", ActivityType.Motorcycling, "Monsoon Mud Specialist", BadgeTier.Gold, "Complete 300+ km under continuous monsoon rain", true, 1.0f),
                Badge("b4", ActivityType.Motorcycling, "Iron Butt 600", BadgeTier.Silver, "Ride 600 km within a single daylight window", true, 1.0f),
                Badge("b5", ActivityType.Trekking, "Spiti Scree Scrambler", BadgeTier.Bronze, "Hike to Chandra Taal viewpoint from basecamp", true, 1.0f),
                Badge("b6", ActivityType.Cycling, "Western Ridge Climber", BadgeTier.Bronze, "Bicycle up 1,000m vertical gradient in Kudremukh", false, 0.65f)
            )
        )
    )
    val gamification: StateFlow<RiderGamificationState> = _gamification.asStateFlow()

    private val _clubs = MutableStateFlow(
        listOf(
            Club(
                id = "c1",
                name = "Himalayan Wanderers MC",
                tagLine = "High-altitude dual sport riders exploring Ladakh & Spiti",
                badgeUrl = "https://images.unsplash.com/photo-1544735716-392fe2489ffa?auto=format&fit=crop&w=300&q=80",
                memberCount = 1480,
                city = "Manali / Leh",
                isJoined = true,
                members = listOf(
                    ClubMember(currentUser, "Road Captain"),
                    ClubMember(friendUsers[0], "Navigator"),
                    ClubMember(friendUsers[1], "Tail Gunner")
                ),
                goals = listOf(
                    ClubGoal("g1", "October Trans-Himalayan Mileage", "Collectively log 50,000 km across mountain routes", 50000, 38420, "km"),
                    ClubGoal("g2", "Pass Climb Elevation", "Total ascent across all member passes", 150000, 128500, "m")
                ),
                chatMessages = listOf(
                    ClubChatMessage("m1", friendUsers[1], "Heads up team: Baralacha La has fresh black ice this morning! Keep tyre pressure at 24 PSI.", "10:14 AM"),
                    ClubChatMessage("m2", currentUser, "Noted Clutch! Will carry extra snow chains and tow straps.", "10:20 AM")
                )
            ),
            Club(
                id = "c2",
                name = "Sahyadri Canyon Riders",
                tagLine = "Twisty road enthusiasts and monsoon waterfall tourers",
                badgeUrl = "https://images.unsplash.com/photo-1558981403-c5f9899a28bc?auto=format&fit=crop&w=300&q=80",
                memberCount = 890,
                city = "Pune / Mumbai",
                isJoined = false
            ),
            Club(
                id = "c3",
                name = "Bengaluru Bullet & Adventure Guild",
                tagLine = "Dawn coffee rides, coffee estate trails, and long endurance tours",
                badgeUrl = "https://images.unsplash.com/photo-1568772585407-9361f9bf3a87?auto=format&fit=crop&w=300&q=80",
                memberCount = 2340,
                city = "Bengaluru",
                isJoined = false
            )
        )
    )
    val clubs: StateFlow<List<Club>> = _clubs.asStateFlow()

    private val _telemetry = MutableStateFlow(LiveRideTelemetry())
    val telemetry: StateFlow<LiveRideTelemetry> = _telemetry.asStateFlow()

    // Helper functions
    fun toggleFollowAdventurer(adventurerId: String) {
        if (adventurerId.isBlank() || adventurerId == currentUser.id) return
        _followedAdventurerIds.value = _followedAdventurerIds.value.toMutableSet().apply {
            if (!add(adventurerId)) remove(adventurerId)
        }.toSet()
    }

    fun toggleLike(feedIndex: Int) {
        val currentList = _feed.value.toMutableList()
        if (feedIndex in currentList.indices) {
            when (val item = currentList[feedIndex]) {
                is FeedItem.PostItem -> {
                    val p = item.post
                    val newPost = p.copy(
                        likes = if (p.isLikedByMe) p.likes - 1 else p.likes + 1,
                        isLikedByMe = !p.isLikedByMe
                    )
                    currentList[feedIndex] = FeedItem.PostItem(newPost)
                }
                is FeedItem.RideShareItem -> {
                    val rs = item.rideShare
                    val newRs = rs.copy(
                        likes = if (rs.isLikedByMe) rs.likes - 1 else rs.likes + 1,
                        isLikedByMe = !rs.isLikedByMe
                    )
                    currentList[feedIndex] = FeedItem.RideShareItem(newRs)
                }
                else -> Unit
            }
            _feed.value = currentList
        }
    }

    fun addComment(feedIndex: Int, text: String) {
        if (text.isBlank()) return
        val currentList = _feed.value.toMutableList()
        if (feedIndex in currentList.indices) {
            val newComment = Comment(user = currentUser, text = text)
            when (val item = currentList[feedIndex]) {
                is FeedItem.PostItem -> {
                    val p = item.post
                    currentList[feedIndex] = FeedItem.PostItem(p.copy(comments = p.comments + newComment))
                }
                is FeedItem.RideShareItem -> {
                    val rs = item.rideShare
                    currentList[feedIndex] = FeedItem.RideShareItem(rs.copy(comments = rs.comments + newComment))
                }
                else -> Unit
            }
            _feed.value = currentList
        }
    }

    fun createPost(caption: String, imageUrl: String? = null, rideStats: RideStats? = null) {
        val newPost = Post(
            user = currentUser,
            timestamp = "Just now",
            caption = caption,
            imageUrl = imageUrl,
            rideStats = rideStats,
            likes = 1,
            isLikedByMe = true
        )
        _feed.value = listOf(FeedItem.PostItem(newPost)) + _feed.value
    }

    fun toggleBookmarkExpedition(expeditionId: String) {
        _expeditions.value = _expeditions.value.map {
            if (it.id == expeditionId) it.copy(isBookmarked = !it.isBookmarked) else it
        }
    }

    fun toggleJoinClub(clubId: String) {
        _clubs.value = _clubs.value.map {
            if (it.id == clubId) it.copy(isJoined = !it.isJoined) else it
        }
    }

    fun sendClubMessage(clubId: String, text: String) {
        if (text.isBlank()) return
        val msg = ClubChatMessage(user = currentUser, message = text, timestamp = "Just now")
        _clubs.value = _clubs.value.map {
            if (it.id == clubId) it.copy(chatMessages = it.chatMessages + msg) else it
        }
    }

    fun startRide(
        demo: Boolean = false,
        activityType: ActivityType = ActivityType.Motorcycling
    ) {
        _telemetry.value = LiveRideTelemetry(
            isRecording = true,
            isPaused = false,
            isDemo = demo,
            activityType = activityType
        )
    }

    fun discardRide() {
        _telemetry.value = LiveRideTelemetry()
    }

    fun leaveDemoMode() {
        val current = _telemetry.value
        if (current.isDemo) {
            _telemetry.value = current.copy(isDemo = false, currentSpeedKmH = 0.0, leanAngleDeg = 0)
        }
    }

    fun setPaused(paused: Boolean) {
        val current = _telemetry.value
        if (current.isRecording) {
            _telemetry.value = current.copy(
                isPaused = paused,
                currentSpeedKmH = if (paused) 0.0 else current.currentSpeedKmH
            )
        }
    }

    fun tickClock() {
        val current = _telemetry.value
        if (!current.isRecording || current.isPaused) return
        val nextSeconds = current.elapsedSeconds + 1
        val averageSpeed = if (nextSeconds > 0) {
            kotlin.math.round(current.distanceKm / (nextSeconds / 3600.0) * 10.0) / 10.0
        } else 0.0
        _telemetry.value = current.copy(
            elapsedSeconds = nextSeconds,
            averageSpeedKmH = averageSpeed,
            caloriesBurned = (nextSeconds * 0.4).toInt()
        )
    }

    fun updateFromGpsLocation(location: com.example.data.location.RideLocation) {
        val current = _telemetry.value
        if (!current.isRecording || current.isPaused) return
        val usableFix = location.accuracyM in 0f..35f
        val usableDistance = usableFix && location.distanceDeltaM in 0f..50f
        val nextDistance = current.distanceKm +
            if (usableDistance) location.distanceDeltaM / 1000.0 else 0.0
        val newAltitude = if (location.altitudeM > 0.0) location.altitudeM.toInt() else current.altitudeM
        val climb = if (current.hasGpsFix && newAltitude - current.altitudeM >= 3) {
            newAltitude - current.altitudeM
        } else 0
        val nextLean = if (current.activityType.supportsLeanAngle) {
            kotlin.math.round(location.leanAngleDeg).toInt()
        } else 0
        _telemetry.value = current.copy(
            distanceKm = kotlin.math.round(nextDistance * 100.0) / 100.0,
            currentSpeedKmH = if (usableFix) location.speedKmH else current.currentSpeedKmH,
            topSpeedKmH = maxOf(current.topSpeedKmH, if (usableFix) location.speedKmH else 0.0),
            altitudeM = newAltitude,
            elevationGainM = current.elevationGainM + climb,
            latitude = location.latitude,
            longitude = location.longitude,
            bearingDeg = location.bearingDeg,
            hasGpsFix = true,
            leanAngleDeg = nextLean,
            maxLeanAngleDeg = if (current.activityType.supportsLeanAngle) {
                maxOf(current.maxLeanAngleDeg, kotlin.math.abs(nextLean))
            } else 0
        )
    }

    /** Simulated movement is only used when location permission is not available. */
    fun updateTelemetryTick() {
        val current = _telemetry.value
        if (!current.isRecording || current.isPaused || !current.isDemo) return
        val nextDistance = current.distanceKm + if (
            current.activityType in setOf(
                ActivityType.Motorcycling,
                ActivityType.Cycling,
                ActivityType.Walking,
                ActivityType.Trekking,
                ActivityType.CarTrips
            )
        ) 0.01 else 0.0
        val nextSpeed = when (current.activityType) {
            ActivityType.Motorcycling -> (15 + (System.currentTimeMillis() % 35)).toDouble()
            ActivityType.Cycling -> (8 + (System.currentTimeMillis() % 18)).toDouble()
            ActivityType.CarTrips -> (20 + (System.currentTimeMillis() % 35)).toDouble()
            ActivityType.Walking, ActivityType.Trekking -> (1 + (System.currentTimeMillis() % 5)).toDouble()
            else -> 0.0
        }
        val nextLean = if (current.activityType.supportsLeanAngle) {
            ((System.currentTimeMillis() % 25) - 12).toInt()
        } else 0
        _telemetry.value = current.copy(
            distanceKm = kotlin.math.round(nextDistance * 100.0) / 100.0,
            currentSpeedKmH = nextSpeed,
            averageSpeedKmH = nextSpeed,
            topSpeedKmH = maxOf(current.topSpeedKmH, nextSpeed),
            leanAngleDeg = nextLean,
            maxLeanAngleDeg = maxOf(current.maxLeanAngleDeg, kotlin.math.abs(nextLean))
        )
    }

    fun addTelemetryWaypoint(label: String, type: String) {
        val current = _telemetry.value
        val waypoint = TelemetryWaypoint(
            label = label,
            type = type,
            timestamp = "${formatRideDuration(current.elapsedSeconds)} · ${current.distanceKm} km",
            speedKmH = current.currentSpeedKmH,
            altitudeM = current.altitudeM
        )
        _telemetry.value = current.copy(markedWaypoints = current.markedWaypoints + waypoint)
    }

    fun stopAndSaveRide(): RideStats {
        val current = _telemetry.value
        val stats = RideStats(
            distanceKm = current.distanceKm,
            duration = formatRideDuration(current.elapsedSeconds),
            elevationGainM = current.elevationGainM,
            topSpeedKmH = current.topSpeedKmH,
            maxLeanAngleDeg = current.maxLeanAngleDeg
        )
        _telemetry.value = LiveRideTelemetry()
        val gamification = _gamification.value
        val gainedXp = (stats.distanceKm * 20).toInt() + 150
        _gamification.value = gamification.copy(
            currentXp = gamification.currentXp + gainedXp,
            totalRides = gamification.totalRides + 1,
            totalDistanceKm = gamification.totalDistanceKm + stats.distanceKm,
            totalElevationM = gamification.totalElevationM + stats.elevationGainM
        )
        return stats
    }

    fun toggleRespondToDistress(signalId: String) {
        val current = _distressSignals.value
        _distressSignals.value = current.map { sig ->
            if (sig.id == signalId) {
                val nextAck = !sig.isAcknowledgedByMe
                val nextCount = if (nextAck) sig.responderCount + 1 else maxOf(0, sig.responderCount - 1)
                val nextStatus = if (nextCount > 0) DistressStatus.RESPONDER_EN_ROUTE else DistressStatus.ACTIVE_SOS
                sig.copy(
                    isAcknowledgedByMe = nextAck,
                    responderCount = nextCount,
                    status = nextStatus
                )
            } else sig
        }
    }

    /**
     * Saves the rider's own SOS note on this phone. Nothing is transmitted to anyone.
     * Location fields come from a real GPS fix when available; otherwise they are marked unknown.
     */
    fun broadcastMySos(
        distressType: DistressType,
        description: String,
        phone: String,
        latitude: Double? = null,
        longitude: Double? = null,
        altitudeM: Int? = null,
    ) {
        val hasLocation = latitude != null && longitude != null
        val mySignal = RiderDistressSignal(
            id = "my-sos-${UUID.randomUUID().toString().take(6)}",
            riderName = "${currentUser.name} (YOU)",
            bikeModel = currentUser.bikeModel,
            distressType = distressType,
            status = DistressStatus.ACTIVE_SOS,
            latitude = latitude ?: 0.0,
            longitude = longitude ?: 0.0,
            distanceKm = 0.0,
            bearingDeg = 0f,
            altitudeM = altitudeM ?: 0,
            batteryPercent = -1, // not measured
            timeAgo = "Just now",
            description = description.ifBlank { "SOS note saved on this phone. Nobody has been notified automatically." },
            phoneNumber = phone.trim(),
            responderCount = 0,
            isMySignal = true,
            hasLocation = hasLocation
        )
        _distressSignals.value = listOf(mySignal) + _distressSignals.value.filterNot { it.isMySignal }
    }

    /** Fills in the rider's real coordinates once a last-known location arrives. */
    fun updateMySosLocation(latitude: Double, longitude: Double, altitudeM: Int?) {
        _distressSignals.value = _distressSignals.value.map { sig ->
            if (sig.isMySignal) sig.copy(latitude = latitude, longitude = longitude, altitudeM = altitudeM ?: sig.altitudeM, hasLocation = true) else sig
        }
    }

    fun cancelMySos() {
        _distressSignals.value = _distressSignals.value.filterNot { it.isMySignal }
    }
}

/** Format seconds as m:ss or h:mm:ss. */
fun formatRideDuration(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val remainingSeconds = seconds % 60
    return if (hours > 0) "%d:%02d:%02d".format(hours, minutes, remainingSeconds)
    else "%d:%02d".format(minutes, remainingSeconds)
}
