package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.AdventurerRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class Screen(val title: String, val isTopLevel: Boolean = false) {
    FEED("Basecamp", isTopLevel = true),
    JOURNEY("Journey"),
    EXPEDITIONS("Expeditions"),
    EXPLORE_PLAN("Field Atlas", isTopLevel = true),
    ROUTE_BRIEF("Route Brief"),
    VOICE_COMPANION("Voice Companion"),
    RADAR("Safety & Help"),
    LIVE_TRACKER("Go / Field Mode", isTopLevel = true),
    STORY_STUDIO("Story Studio"),
    FINAL_REVIEW("Final Review"),
    GAMIFICATION("Achievements & Milestones"),
    CLUBS("Community", isTopLevel = true),
    PROFILE("Trail Settings", isTopLevel = true),
    PRO_PAYWALL("Adventurer Pro")
}

enum class SupportedLang(val code: String, val displayName: String, val localName: String) {
    ENGLISH("en", "English", "English"),
    HINDI("hi", "Hindi", "हिन्दी"),
    MARATHI("mr", "Marathi", "मराठी"),
    KANNADA("kn", "Kannada", "ಕನ್ನಡ"),
    TAMIL("ta", "Tamil", "தமிழ்")
}

class AdventurerViewModel(
    val repository: AdventurerRepository = AdventurerRepository()
) : ViewModel() {

    private val screenStack = mutableListOf<Screen>()

    private val _currentScreen = MutableStateFlow(Screen.FEED)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _activityMode = MutableStateFlow(ActivityType.Motorcycling)
    val activityMode: StateFlow<ActivityType> = _activityMode.asStateFlow()

    private val _currentLang = MutableStateFlow(SupportedLang.ENGLISH)
    val currentLang: StateFlow<SupportedLang> = _currentLang.asStateFlow()

    private val _selectedExpedition = MutableStateFlow<ExpeditionRoute?>(null)
    val selectedExpedition: StateFlow<ExpeditionRoute?> = _selectedExpedition.asStateFlow()

    private val _showBudgetModal = MutableStateFlow(false)
    val showBudgetModal: StateFlow<Boolean> = _showBudgetModal.asStateFlow()

    private val _showSosAlert = MutableStateFlow(false)
    val showSosAlert: StateFlow<Boolean> = _showSosAlert.asStateFlow()

    private val _showCreatePost = MutableStateFlow(false)
    val showCreatePost: StateFlow<Boolean> = _showCreatePost.asStateFlow()

    private val _activeCommentPostIndex = MutableStateFlow<Int?>(null)
    val activeCommentPostIndex: StateFlow<Int?> = _activeCommentPostIndex.asStateFlow()

    private val _showFeatureOrganizer = MutableStateFlow(false)
    val showFeatureOrganizer: StateFlow<Boolean> = _showFeatureOrganizer.asStateFlow()

    fun setShowFeatureOrganizer(show: Boolean) {
        _showFeatureOrganizer.value = show
    }

    val feed = repository.feed
    val friendUsers = repository.friendUsers
    val followedAdventurerIds = repository.followedAdventurerIds

    fun toggleFollowAdventurer(adventurerId: String) {
        repository.toggleFollowAdventurer(adventurerId)
    }
    val expeditions = repository.expeditions
    val emergencyStations = repository.emergencyStations
    val distressSignals = repository.distressSignals
    val roadsideServices = repository.roadsideServices
    val safetyTips = repository.safetyTips
    val gamification = repository.gamification
    val clubs = repository.clubs
    val telemetry = repository.telemetry
    val currentUser = repository.currentUser

    private val _radarFilter = MutableStateFlow(RadarFilter.ALL)
    val radarFilter: StateFlow<RadarFilter> = _radarFilter.asStateFlow()

    private val _selectedMarker = MutableStateFlow<MapMarkerItem?>(null)
    val selectedMarker: StateFlow<MapMarkerItem?> = _selectedMarker.asStateFlow()

    private val _showBroadcastModal = MutableStateFlow(false)
    val showBroadcastModal: StateFlow<Boolean> = _showBroadcastModal.asStateFlow()

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    fun setDarkMode(dark: Boolean) {
        _isDarkMode.value = dark
    }

    init {
        // Wall-clock time runs independently of GPS cadence. Simulation is demo-only.
        viewModelScope.launch {
            var tick = 0
            while (true) {
                delay(1000)
                repository.tickClock()
                if (++tick % 2 == 0) repository.updateTelemetryTick()
            }
        }
    }

    fun navigateTo(screen: Screen) {
        if (_currentScreen.value != screen) {
            screenStack.add(_currentScreen.value)
            _currentScreen.value = screen
        }
    }

    fun navigateBack(): Boolean {
        if (screenStack.isNotEmpty()) {
            _currentScreen.value = screenStack.removeAt(screenStack.size - 1)
            return true
        } else if (_currentScreen.value != Screen.FEED) {
            _currentScreen.value = Screen.FEED
            return true
        }
        return false
    }

    private val voiceTopLevelScreens = listOf(
        Screen.FEED,
        Screen.EXPLORE_PLAN,
        Screen.CLUBS,
        Screen.LIVE_TRACKER,
        Screen.PROFILE,
    )

    fun navigateToNextTopLevel() {
        val index = voiceTopLevelScreens.indexOf(_currentScreen.value)
        navigateTo(voiceTopLevelScreens[(index + 1).mod(voiceTopLevelScreens.size)])
    }

    fun navigateToPreviousTopLevel() {
        val index = voiceTopLevelScreens.indexOf(_currentScreen.value)
        val normalized = if (index < 0) 0 else index
        navigateTo(voiceTopLevelScreens[(normalized - 1 + voiceTopLevelScreens.size).mod(voiceTopLevelScreens.size)])
    }

    fun setActivityMode(mode: ActivityType) {
        _activityMode.value = mode
    }

    fun setLanguage(lang: SupportedLang) {
        _currentLang.value = lang
    }

    fun selectExpedition(route: ExpeditionRoute?) {
        _selectedExpedition.value = route
    }

    fun setShowBudgetModal(show: Boolean) {
        _showBudgetModal.value = show
    }

    fun setShowSosAlert(show: Boolean) {
        _showSosAlert.value = show
    }

    fun setShowCreatePost(show: Boolean) {
        _showCreatePost.value = show
    }

    fun openCommentSheet(postIndex: Int?) {
        _activeCommentPostIndex.value = postIndex
    }

    fun toggleLike(index: Int) {
        repository.toggleLike(index)
    }

    fun addComment(index: Int, text: String) {
        repository.addComment(index, text)
        _activeCommentPostIndex.value = null
    }

    fun createPost(caption: String, imageUrl: String?) {
        repository.createPost(caption, imageUrl)
        _showCreatePost.value = false
    }

    fun toggleBookmarkExpedition(id: String) {
        repository.toggleBookmarkExpedition(id)
    }

    fun toggleJoinClub(id: String) {
        repository.toggleJoinClub(id)
    }

    fun sendClubMessage(id: String, text: String) {
        repository.sendClubMessage(id, text)
    }

    var locationManager: com.example.data.location.LocationManager? = null
        private set

    private var locationJob: kotlinx.coroutines.Job? = null

    fun attachLocationManager(manager: com.example.data.location.LocationManager) {
        locationManager = manager
        if (telemetry.value.isRecording) startGpsTracking()
    }

    fun startGpsTracking() {
        val manager = locationManager ?: return
        if (!manager.hasLocationPermission()) return
        locationJob?.cancel()
        locationJob = viewModelScope.launch {
            try {
                manager.getLocationUpdates().collect { rideLocation ->
                    repository.updateFromGpsLocation(rideLocation)
                }
            } catch (_: Exception) {
                // The HUD remains available if permission is revoked or GPS is unavailable.
            }
        }
    }

    fun stopGpsTracking() {
        locationJob?.cancel()
        locationJob = null
        locationManager?.stopTracking()
    }

    fun startRide() {
        val hasPermission = locationManager?.hasLocationPermission() == true
        repository.startRide(demo = !hasPermission, activityType = activityMode.value)
        if (hasPermission) startGpsTracking()
        navigateTo(Screen.LIVE_TRACKER)
    }

    fun onLocationPermissionGranted() {
        repository.leaveDemoMode()
        if (telemetry.value.isRecording) startGpsTracking()
    }

    fun pauseRide() = repository.setPaused(true)
    fun resumeRide() = repository.setPaused(false)

    fun discardRide() {
        stopGpsTracking()
        repository.discardRide()
    }

    fun addWaypoint(label: String, type: String) {
        repository.addTelemetryWaypoint(label, type)
    }

    fun finishRide() {
        val completedSession = telemetry.value
        val completedMode = completedSession.activityType
        val wasDemo = completedSession.isDemo
        stopGpsTracking()
        val stats = repository.stopAndSaveRide()
        val caption = if (wasDemo) {
            "🏁 Demo ${completedMode.displayName.lowercase()} session completed. Movement values were simulated and are not a recorded GPS route."
        } else when (completedMode) {
            ActivityType.Motorcycling ->
                "🏁 Motorcycle ride completed! Distance: ${stats.distanceKm} km · Duration: ${stats.duration} · Top speed: ${stats.topSpeedKmH.toInt()} km/h · Max lean estimate: ${stats.maxLeanAngleDeg}° · Elevation gain: +${stats.elevationGainM} m."
            ActivityType.Cycling ->
                "🚴 Cycling session completed! Distance: ${stats.distanceKm} km · Duration: ${stats.duration} · Elevation gain: +${stats.elevationGainM} m."
            ActivityType.Walking, ActivityType.Trekking ->
                "🥾 ${completedMode.displayName} session completed! Distance: ${stats.distanceKm} km · Duration: ${stats.duration} · Elevation gain: +${stats.elevationGainM} m."
            ActivityType.CarTrips ->
                "🚗 Road-trip session completed! Recorded distance: ${stats.distanceKm} km · Duration: ${stats.duration}. Review the stops before sharing."
            else ->
                "🧭 ${completedMode.displayName} session completed. Recorded distance: ${stats.distanceKm} km · Duration: ${stats.duration}. Add notes and verify activity-specific details before sharing."
        }
        repository.createPost(caption = caption, rideStats = stats)
        navigateTo(Screen.FEED)
    }

    fun setRadarFilter(filter: RadarFilter) {
        _radarFilter.value = filter
    }

    fun selectMarker(marker: MapMarkerItem?) {
        _selectedMarker.value = marker
    }

    fun setShowBroadcastModal(show: Boolean) {
        _showBroadcastModal.value = show
    }

    fun toggleRespondToDistress(signalId: String) {
        repository.toggleRespondToDistress(signalId)
        // Keep selected marker updated if it's the one being toggled
        val currentSelected = _selectedMarker.value
        if (currentSelected is MapMarkerItem.RiderDistress && currentSelected.id == signalId) {
            val updatedSignal = repository.distressSignals.value.find { it.id == signalId }
            if (updatedSignal != null) {
                _selectedMarker.value = MapMarkerItem.RiderDistress(updatedSignal)
            }
        }
    }

    fun broadcastMySos(distressType: DistressType, description: String, phone: String) {
        val t = telemetry.value
        if (t.hasGpsFix) {
            repository.broadcastMySos(distressType, description, phone, t.latitude, t.longitude, t.altitudeM)
        } else {
            repository.broadcastMySos(distressType, description, phone)
            locationManager?.takeIf { it.hasLocationPermission() }?.getLastKnownLocation { loc ->
                if (loc != null && (loc.latitude != 0.0 || loc.longitude != 0.0)) {
                    repository.updateMySosLocation(loc.latitude, loc.longitude, loc.altitudeM.toInt().takeIf { it > 0 })
                    val selected = _selectedMarker.value
                    if (selected is MapMarkerItem.RiderDistress && selected.signal.isMySignal) {
                        repository.distressSignals.value.firstOrNull { it.isMySignal }?.let { _selectedMarker.value = MapMarkerItem.RiderDistress(it) }
                    }
                }
            }
        }
        _showBroadcastModal.value = false
        // Automatically select own broadcast marker
        val mySignal = repository.distressSignals.value.firstOrNull { it.isMySignal }
        if (mySignal != null) {
            _selectedMarker.value = MapMarkerItem.RiderDistress(mySignal)
        }
    }

    fun cancelMySos() {
        repository.cancelMySos()
        if (_selectedMarker.value is MapMarkerItem.RiderDistress && (_selectedMarker.value as MapMarkerItem.RiderDistress).signal.isMySignal) {
            _selectedMarker.value = null
        }
    }

    // ==========================================
    // JOURNEY COMMAND CENTER STATE & ACTIONS
    // ==========================================

    private val _fuelState = MutableStateFlow(FuelState())
    val fuelState: StateFlow<FuelState> = _fuelState.asStateFlow()

    private val _journeyStops = MutableStateFlow(
        listOf(
            JourneyStop(
                id = "stop-1",
                name = "Jispa Riverside Basecamp",
                distanceFromStartKm = 0.0,
                etaMinutes = 0,
                altitudeM = 3200,
                reminderNote = "Check tire pressure & top up engine oil before ascent",
                isVisited = true,
                status = ActionStatus.CHECKED_OFF
            ),
            JourneyStop(
                id = "stop-2",
                name = "Zingzingbar High Army Halt",
                distanceFromStartKm = 32.4,
                etaMinutes = 45,
                altitudeM = 4270,
                reminderNote = "Acclimatize 20m, sip hot ginger tea, check AMS symptoms",
                isVisited = false,
                status = ActionStatus.SUGGESTED
            ),
            JourneyStop(
                id = "stop-3",
                name = "Baralacha La Mountain Pass",
                distanceFromStartKm = 54.8,
                etaMinutes = 80,
                altitudeM = 4890,
                reminderNote = "Caution: water crossing and possible black ice on north face",
                isVisited = false,
                status = ActionStatus.SUGGESTED
            ),
            JourneyStop(
                id = "stop-4",
                name = "Sarchu Alpine Transition Camp",
                distanceFromStartKm = 84.2,
                etaMinutes = 135,
                altitudeM = 4290,
                reminderNote = "Check oxygen levels, register checkpost, pitch camp before twilight",
                isVisited = false,
                status = ActionStatus.SUGGESTED
            ),
            JourneyStop(
                id = "stop-5",
                name = "Nakee La & 21 Gata Loops",
                distanceFromStartKm = 128.0,
                etaMinutes = 210,
                altitudeM = 4739,
                reminderNote = "Maintain 2nd gear engine braking on switchbacks; leave water offering",
                isVisited = false,
                status = ActionStatus.SUGGESTED
            )
        )
    )
    val journeyStops: StateFlow<List<JourneyStop>> = _journeyStops.asStateFlow()

    private val _journeyMemories = MutableStateFlow(
        listOf(
            JourneyMemory(
                id = "mem-1",
                title = "Sunrise Reflection on Deepak Taal",
                locationName = "Deepak Taal · 3,750m",
                altitudeM = 3750,
                timestamp = "07:15 AM · Day 3",
                reflection = "The dawn reflection of snow peaks in the still turquoise water was motionless. Engine ticking as it cooled in the crisp morning air, steam rising from our tin chai mugs.",
                moodTag = "Golden Serenity ☀️",
                gradientColorsHex = listOf(0xFFF97316, 0xFFEAB308)
            ),
            JourneyMemory(
                id = "mem-2",
                title = "Prayer Flags in Baralacha Wind",
                locationName = "Baralacha La Summit · 4,890m",
                altitudeM = 4890,
                timestamp = "11:40 AM · Day 3",
                reflection = "Fierce alpine gusts whipped prayer flags into a blur of color against deep indigo sky. Thin, deliberate breaths. We tied our club ribbon to the stone cairn and saluted the mountain gods.",
                moodTag = "High Altitude Reverence 🏔️",
                gradientColorsHex = listOf(0xFF38BDF8, 0xFF6366F1)
            ),
            JourneyMemory(
                id = "mem-3",
                title = "Warm Hearth at Batal Refuge",
                locationName = "Batal Chandra Taal Bridge · 3,960m",
                altitudeM = 3960,
                timestamp = "06:30 PM · Yesterday",
                reflection = "After wading two axle-deep freezing snowmelt streams, the warmth of Chacha's kerosene stove and a steaming plate of hot Rajma Chawal felt like the greatest luxury on Earth.",
                moodTag = "Trail Brotherhood 🤝",
                gradientColorsHex = listOf(0xFFEC4899, 0xFF8B5CF6)
            )
        )
    )
    val journeyMemories: StateFlow<List<JourneyMemory>> = _journeyMemories.asStateFlow()

    private val _discoveryVenues = MutableStateFlow(
        listOf(
            // Food
            DiscoveryVenue(
                id = "ven-1",
                name = "Chandra-Bhaga High Altitude Dhaba",
                category = DiscoveryCategory.RESTAURANTS,
                distanceKm = 3.2,
                rating = 4.9,
                altitudeM = 3350,
                description = "World-famous piping hot Himalayan thukpa, mutton/veg momos & ginger lemon honey tea with heated hearth seating.",
                amenities = listOf("Warm Hearth Seating", "Pure Spring Water", "Fast Trailside Service", "Biker Parking"),
                menuItems = listOf(
                    MenuItem("m1", "Steaming Himalayan Veg Thukpa", "Hand-pulled mountain noodles in herbal broth", 140, isVeg = true),
                    MenuItem("m2", "Tibetan Steamed Momos (8 pcs)", "Stuffed with farm cabbage, paneer, and garlic dip", 120, isVeg = true),
                    MenuItem("m3", "Butter Tea & Roasted Barley Tsampa", "Traditional Tibetan energy fuel for cold passes", 80, isVeg = true),
                    MenuItem("m4", "Mountain Maggi with Crunchy Veggies", "Hot spicy comfort food with fried onions", 90, isVeg = true),
                    MenuItem("m5", "Trail Electrolyte Pack & Dark Choc", "High-altitude hydration and quick glucose", 70, isVeg = true)
                ),
                locationDescription = "NH-3 Jispa Bypass"
            ),
            DiscoveryVenue(
                id = "ven-2",
                name = "Zingzingbar Army & Biker Refuge",
                category = DiscoveryCategory.RESTAURANTS,
                distanceKm = 32.0,
                rating = 4.8,
                altitudeM = 4270,
                description = "High-mountain comfort station: piping hot dal tadka, tandoori rotis, and oxygen replenishment stop.",
                amenities = listOf("Oxygen Cylinder Onsite", "Kerosene Heater", "Emergency Satellite Link"),
                menuItems = listOf(
                    MenuItem("m6", "Dal Tadka with Steaming Rice", "Slow-cooked yellow lentils with cumin and ghee", 160, isVeg = true),
                    MenuItem("m7", "Hot Tawa Butter Roti (4 pcs)", "Freshly toasted whole wheat flatbreads", 80, isVeg = true),
                    MenuItem("m8", "Ginger Honey Lemon Kahwa", "Kashmiri saffron and almond herbal brew", 75, isVeg = true),
                    MenuItem("m9", "Boiled Farm Eggs with Black Pepper (2)", "Quick clean protein for mountain riding", 60, isVeg = false)
                ),
                locationDescription = "Zingzingbar Checkpoint"
            ),
            // Fuel
            DiscoveryVenue(
                id = "ven-3",
                name = "Tandi Final IndianOil Pump",
                category = DiscoveryCategory.FUEL,
                distanceKm = 38.0,
                rating = 4.9,
                altitudeM = 3160,
                description = "CRITICAL: Last official fuel station for the next 365 km until Karu/Leh. Fill tank and spare jerry cans.",
                amenities = listOf("91 Octane Petrol", "Diesel", "Jerry Can Fill Allowed", "Air Pressure Check"),
                locationDescription = "Confluence of Chandra & Bhaga rivers"
            ),
            DiscoveryVenue(
                id = "ven-4",
                name = "Jispa Emergency Fuel & Recovery Depot",
                category = DiscoveryCategory.FUEL,
                distanceKm = 4.1,
                rating = 4.7,
                altitudeM = 3250,
                description = "Sealed 5L/10L petrol cans, jump start battery packs, tire vulcanizing, winch tow assistance.",
                amenities = listOf("Sealed 5L Fuel Cans", "Jump Battery Packs", "Heavy Tow Straps", "24/7 Assistance"),
                locationDescription = "Opposite Jispa Helipad"
            ),
            // Stays
            DiscoveryVenue(
                id = "ven-5",
                name = "Padma Lodge & Jispa Riverside Glamping",
                category = DiscoveryCategory.STAYS,
                distanceKm = 2.8,
                rating = 4.9,
                altitudeM = 3200,
                description = "Geodesic heated luxury domes by the river, 24/7 pressurized hot water, oxygen concentrators, and enclosed motorcycle parking.",
                amenities = listOf("Heated Geodesic Domes", "Hot Water 24/7", "Oxygen Concentrator", "Secure Bike Bays"),
                locationDescription = "Jispa Riverside Meadow"
            ),
            DiscoveryVenue(
                id = "ven-6",
                name = "Sarchu Wind Whispers Luxury Camp",
                category = DiscoveryCategory.STAYS,
                distanceKm = 84.0,
                rating = 4.6,
                altitudeM = 4290,
                description = "Triple-layered insulated Swiss alpine tents with attached bath, heated blankets, and on-call mountain paramedic.",
                amenities = listOf("Insulated Swiss Tents", "Electric Heating Blankets", "Medical Paramedic", "Dining Hall"),
                locationDescription = "Sarchu Plateau Boundary"
            ),
            // Viewpoints
            DiscoveryVenue(
                id = "ven-7",
                name = "Deepak Taal Emerald Mirror Lake",
                category = DiscoveryCategory.VIEWPOINTS,
                distanceKm = 18.4,
                rating = 4.9,
                altitudeM = 3750,
                description = "Pristine high-altitude mirror lake reflecting glacier peaks. Perfect rest stop and photo overlook.",
                amenities = listOf("Rest Area", "Scenic Overlook", "Photo Point", "Chai Shack"),
                locationDescription = "Between Darcha & Patseo"
            ),
            DiscoveryVenue(
                id = "ven-8",
                name = "Suraj Taal (Lake of Sun God)",
                category = DiscoveryCategory.VIEWPOINTS,
                distanceKm = 51.2,
                rating = 5.0,
                altitudeM = 4883,
                description = "Third highest lake in India, source of Bhaga river, jaw-dropping panoramic pass vista below Baralacha summit.",
                amenities = listOf("Panoramic Vista", "Historical Landmark", "Prayer Flag Site"),
                locationDescription = "3km below Baralacha La"
            )
        )
    )
    val discoveryVenues: StateFlow<List<DiscoveryVenue>> = _discoveryVenues.asStateFlow()

    private val _selectedDiscoveryCategory = MutableStateFlow(DiscoveryCategory.RESTAURANTS)
    val selectedDiscoveryCategory: StateFlow<DiscoveryCategory> = _selectedDiscoveryCategory.asStateFlow()

    private val _venueSearchQuery = MutableStateFlow("")
    val venueSearchQuery: StateFlow<String> = _venueSearchQuery.asStateFlow()

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _activeDemoOrder = MutableStateFlow<DemoFoodOrder?>(null)
    val activeDemoOrder: StateFlow<DemoFoodOrder?> = _activeDemoOrder.asStateFlow()

    private val _voiceDockStateText = MutableStateFlow("All checkpoints loaded. Fuel safe margin is +112 km.")
    val voiceDockStateText: StateFlow<String> = _voiceDockStateText.asStateFlow()

    private val _voiceDockIsSpeaking = MutableStateFlow(false)
    val voiceDockIsSpeaking: StateFlow<Boolean> = _voiceDockIsSpeaking.asStateFlow()

    private val _voiceDockActionStatus = MutableStateFlow(ActionStatus.SUGGESTED)
    val voiceDockActionStatus: StateFlow<ActionStatus> = _voiceDockActionStatus.asStateFlow()

    fun setDiscoveryCategory(category: DiscoveryCategory) {
        _selectedDiscoveryCategory.value = category
    }

    fun setVenueSearchQuery(query: String) {
        _venueSearchQuery.value = query
    }

    fun updateFuelCalculator(
        tankLiters: Double,
        mileage: Double,
        reserve: Double,
        bufferPercent: Double,
        distanceToNext: Double
    ) {
        _fuelState.value = FuelState(
            fuelInTankLiters = tankLiters,
            mileageKmPerLiter = mileage,
            reserveCapacityLiters = reserve,
            safetyBufferPercent = bufferPercent,
            distanceToNextFuelKm = distanceToNext
        )
    }

    fun addJourneyStop(name: String, km: Double, etaMin: Int, altitudeM: Int, reminder: String) {
        val newStop = JourneyStop(
            name = name.ifBlank { "Checkpoint" },
            distanceFromStartKm = km,
            etaMinutes = etaMin,
            altitudeM = altitudeM,
            reminderNote = reminder,
            isVisited = false,
            status = ActionStatus.SUGGESTED
        )
        _journeyStops.value = _journeyStops.value + newStop
        _voiceDockStateText.value = "Added checkpoint: ${newStop.name}. Reminder set."
        _voiceDockActionStatus.value = ActionStatus.SAVED_ON_DEVICE
    }

    fun editJourneyStop(id: String, name: String, km: Double, etaMin: Int, altitudeM: Int, reminder: String) {
        _journeyStops.value = _journeyStops.value.map { stop ->
            if (stop.id == id) {
                stop.copy(
                    name = name,
                    distanceFromStartKm = km,
                    etaMinutes = etaMin,
                    altitudeM = altitudeM,
                    reminderNote = reminder
                )
            } else stop
        }
        _voiceDockStateText.value = "Updated checkpoint $name."
        _voiceDockActionStatus.value = ActionStatus.SAVED_ON_DEVICE
    }

    fun deleteJourneyStop(id: String) {
        _journeyStops.value = _journeyStops.value.filterNot { it.id == id }
    }

    fun toggleStopVisited(id: String) {
        _journeyStops.value = _journeyStops.value.map { stop ->
            if (stop.id == id) {
                val newVisited = !stop.isVisited
                stop.copy(
                    isVisited = newVisited,
                    status = if (newVisited) ActionStatus.CHECKED_OFF else ActionStatus.SUGGESTED
                )
            } else stop
        }
    }

    fun addJourneyMemory(title: String, location: String, altitudeM: Int, reflection: String, moodTag: String) {
        val newMemory = JourneyMemory(
            title = title.ifBlank { "Trail Moment" },
            locationName = location.ifBlank { "High Mountain Road" },
            altitudeM = altitudeM,
            timestamp = "Today · Just logged",
            reflection = reflection.ifBlank { "Spectacular mountain view." },
            moodTag = moodTag.ifBlank { "Trail Joy ✨" },
            gradientColorsHex = listOf(0xFFF97316, 0xFFEAB308)
        )
        _journeyMemories.value = listOf(newMemory) + _journeyMemories.value
        _voiceDockStateText.value = "Logged memory: ${newMemory.title}. Stored in your travel journal."
        _voiceDockActionStatus.value = ActionStatus.SAVED_ON_DEVICE
    }

    fun deleteJourneyMemory(id: String) {
        _journeyMemories.value = _journeyMemories.value.filterNot { it.id == id }
    }

    fun addToCart(menuItem: MenuItem) {
        val existing = _cartItems.value.find { it.menuItem.id == menuItem.id }
        _cartItems.value = if (existing != null) {
            _cartItems.value.map {
                if (it.menuItem.id == menuItem.id) it.copy(quantity = it.quantity + 1) else it
            }
        } else {
            _cartItems.value + CartItem(menuItem, 1)
        }
    }

    fun updateCartQuantity(menuItemId: String, delta: Int) {
        val updated = _cartItems.value.mapNotNull {
            if (it.menuItem.id == menuItemId) {
                val newQ = it.quantity + delta
                if (newQ > 0) it.copy(quantity = newQ) else null
            } else it
        }
        _cartItems.value = updated
    }

    fun clearCart() {
        _cartItems.value = emptyList()
    }

    fun submitDemoOrder(venueName: String, deliveryHalt: String) {
        val items = _cartItems.value
        if (items.isEmpty()) return
        val total = items.sumOf { it.menuItem.priceInr * it.quantity }
        _activeDemoOrder.value = DemoFoodOrder(
            venueName = venueName,
            items = items,
            subtotalInr = total,
            deliveryHalt = deliveryHalt,
            status = ActionStatus.AWAITING_CONFIRMATION,
            isSimulation = true
        )
        _voiceDockStateText.value = "Cart ready for $venueName (₹$total). Please review and explicitly confirm the demo order."
        _voiceDockActionStatus.value = ActionStatus.AWAITING_CONFIRMATION
    }

    fun confirmDemoOrder() {
        _activeDemoOrder.value?.let { order ->
            _activeDemoOrder.value = order.copy(
                status = ActionStatus.SIMULATED
            )
            _cartItems.value = emptyList()
            _voiceDockStateText.value = "Demo only: nothing was ordered from ${order.venueName} and no payment was made."
            _voiceDockActionStatus.value = ActionStatus.SIMULATED
        }
    }

    fun cancelDemoOrder() {
        _activeDemoOrder.value = null
        _voiceDockStateText.value = "Demo order cancelled. No charges or dispatch created."
        _voiceDockActionStatus.value = ActionStatus.SUGGESTED
    }

    fun triggerVoiceDockPrompt(prompt: String, status: ActionStatus = ActionStatus.SUGGESTED) {
        _voiceDockIsSpeaking.value = true
        _voiceDockStateText.value = prompt
        _voiceDockActionStatus.value = status
    }

    fun stopVoiceDockSpeaking() {
        _voiceDockIsSpeaking.value = false
    }
}
