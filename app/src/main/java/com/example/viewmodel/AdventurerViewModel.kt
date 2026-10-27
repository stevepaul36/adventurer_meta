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
    FEED("Feed", isTopLevel = true),
    EXPEDITIONS("Expeditions"),
    EXPLORE_PLAN("Explore & Plan", isTopLevel = true),
    RADAR("Roadside Radar", isTopLevel = true),
    LIVE_TRACKER("Live Telemetry", isTopLevel = true),
    STORY_STUDIO("AI Story Studio"),
    GAMIFICATION("Gamification Hub"),
    CLUBS("Riding Clubs"),
    PROFILE("Rider Profile", isTopLevel = true),
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

    val feed = repository.feed
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
        repository.broadcastMySos(distressType, description, phone)
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
}
