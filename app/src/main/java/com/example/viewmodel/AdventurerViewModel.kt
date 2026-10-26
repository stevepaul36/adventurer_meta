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
    EXPEDITIONS("Expeditions", isTopLevel = true),
    RADAR("Roadside Radar", isTopLevel = true),
    LIVE_TRACKER("Live Telemetry", isTopLevel = true),
    STORY_STUDIO("AI Story Studio"),
    APEX_MOTO("Apex Moto 3D"),
    GAMIFICATION("Gamification Hub"),
    CLUBS("Riding Clubs"),
    PROFILE("Rider Profile", isTopLevel = true)
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
    val safetyTips = repository.safetyTips
    val gamification = repository.gamification
    val clubs = repository.clubs
    val telemetry = repository.telemetry
    val currentUser = repository.currentUser

    init {
        // Ride clock (1 s) and demo-mode simulation (2 s) while a ride is recording.
        viewModelScope.launch {
            var tick = 0
            while (true) {
                delay(1000)
                repository.tickClock()
                if (++tick % 2 == 0) repository.updateTelemetryTick()
            }
        }
    }

    /**
     * Bottom-bar tabs replace each other (back from any tab returns to the Feed);
     * detail screens (Studio, Clubs, Trophies, Apex Moto) stack on top of the current tab.
     */
    fun navigateTo(screen: Screen) {
        val current = _currentScreen.value
        if (current == screen) return
        if (screen.isTopLevel) {
            screenStack.clear()
        } else {
            screenStack.add(current)
        }
        _currentScreen.value = screen
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
        this.locationManager = manager
        if (telemetry.value.isRecording) {
            startGpsTracking()
        }
    }

    fun startGpsTracking() {
        val manager = locationManager ?: return
        if (!manager.hasLocationPermission()) return
        locationJob?.cancel()
        locationJob = viewModelScope.launch {
            try {
                manager.getLocationUpdates().collect { rideLoc ->
                    repository.updateFromGpsLocation(rideLoc)
                }
            } catch (_: Exception) {
                // Handled gracefully
            }
        }
    }

    fun stopGpsTracking() {
        locationJob?.cancel()
        locationJob = null
        locationManager?.stopTracking()
    }

    fun startRide() {
        val hasLocation = locationManager?.hasLocationPermission() == true
        repository.startRide(demo = !hasLocation)
        startGpsTracking()
        navigateTo(Screen.LIVE_TRACKER)
    }

    /** Called when the rider grants location permission from the HUD. */
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
        stopGpsTracking()
        val stats = repository.stopAndSaveRide()
        repository.createPost(
            caption = "🏁 Completed ride! ${stats.distanceKm} km in ${stats.duration} · top ${stats.topSpeedKmH.toInt()} km/h · max lean ${stats.maxLeanAngleDeg}° · +${stats.elevationGainM} m climbed.",
            rideStats = stats
        )
        navigateTo(Screen.FEED)
    }

    fun recordApexRun(score: Int, topSpeed: Double, dist: Double) {
        repository.recordApexMotoRun(score, topSpeed, dist)
        navigateTo(Screen.FEED)
    }
}
