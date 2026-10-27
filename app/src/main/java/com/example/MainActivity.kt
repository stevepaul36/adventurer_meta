package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.repository.formatRideDuration
import com.example.ui.Str
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import com.example.analytics.AdventureFunnelAnalytics
import com.example.data.planning.AdventurePlannerRepository
import com.example.data.model.ActivityType
import com.example.data.voice.AdventureVoiceAction
import com.example.data.voice.AdventureVoiceController
import com.example.data.voice.AdventureRealtimeVoiceSession
import com.example.data.voice.RealtimeAdventureContext
import com.example.data.voice.AdventureVoiceSafetyPolicy
import com.example.data.voice.VoiceMotionSnapshot
import com.example.data.voice.AdventureGuideMode
import com.example.data.voice.AdventureGuidePreferences
import com.example.data.voice.AdventureGuidePreferencesStore
import com.example.data.voice.ParsedAdventureVoiceCommand
import com.example.data.voice.VoiceCommandReply
import com.example.data.voice.VoiceStartRequest
import com.example.data.model.FeedItem
import com.example.data.billing.AdventurerProBilling
import com.example.ui.components.AdventureVoiceBubble
import com.example.ui.components.CommentsBottomSheet
import com.example.ui.components.CreatePostDialog
import com.example.ui.components.SafetySosDialog
import com.example.ui.screens.*
import com.example.ui.theme.AdventureOrange
import com.example.ui.theme.AdventurerTheme
import com.example.ui.theme.AmberGold
import com.example.viewmodel.AdventurerViewModel
import com.example.viewmodel.Screen
import com.google.firebase.functions.FirebaseFunctions
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

class MainActivity : ComponentActivity() {

    private val viewModel: AdventurerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        AdventureFunnelAnalytics.log(this, "adventure_app_open")
        setContent {
            AdventurerTheme(darkTheme = true) {
                AdventurerApp(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        AdventurerProBilling.refresh()
    }
}

private data class VoicePlaceFix(
    val latitude: Double,
    val longitude: Double,
    val headingDeg: Double?,
    val speedMps: Double,
    val accuracyM: Double,
    val timestampMillis: Long,
)

private data class NavTab(val screen: Screen, val label: Str, val selected: ImageVector, val unselected: ImageVector, val accent: Color)

private val NavTabs = listOf(
    NavTab(Screen.FEED, Str.NAV_FEED, Icons.Filled.Home, Icons.Outlined.Home, AdventureOrange),
    NavTab(Screen.EXPLORE_PLAN, Str.NAV_EXPLORE, Icons.Filled.Explore, Icons.Outlined.Explore, AdventureOrange),
    NavTab(Screen.VOICE_COMPANION, Str.NAV_VOICE, Icons.Filled.Mic, Icons.Outlined.Mic, AdventureOrange),
    NavTab(Screen.LIVE_TRACKER, Str.NAV_RIDE, Icons.Filled.Speed, Icons.Outlined.Speed, AdventureOrange),
    NavTab(Screen.RADAR, Str.NAV_SOS, Icons.Filled.Emergency, Icons.Outlined.Emergency, Color(0xFFEF4444)),
    NavTab(Screen.PROFILE, Str.NAV_RIDER, Icons.Filled.Person, Icons.Outlined.Person, AmberGold),
)

@Composable
fun AdventurerApp(viewModel: AdventurerViewModel) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val plannerRepository = remember(context) { AdventurePlannerRepository(context.applicationContext) }
    val guidePreferencesStore = remember(context) { AdventureGuidePreferencesStore(context.applicationContext) }
    val locationManager = remember { com.example.data.location.LocationManager(context.applicationContext) }
    val voiceController = remember(context) { AdventureVoiceController(context) }
    val liveGuideSession = remember(context) { AdventureRealtimeVoiceSession(context.applicationContext) }
    val appScope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner, voiceController, liveGuideSession) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> voiceController.setForegroundActive(true)
                Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP -> {
                    voiceController.setForegroundActive(false)
                    liveGuideSession.disconnect("Live Guide paused because Adventurer left the foreground.")
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        voiceController.setForegroundActive(lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED))
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            voiceController.shutdown()
            liveGuideSession.close()
        }
    }

    fun currentVehicleIsMoving(): Boolean {
        val ride = viewModel.telemetry.value
        return AdventureVoiceSafetyPolicy.isVehicleInMotion(
            VoiceMotionSnapshot(
                activityType = ride.activityType,
                isRecording = ride.isRecording,
                isPaused = ride.isPaused,
                hasGpsFix = ride.hasGpsFix,
                isDemo = ride.isDemo,
                speedKmH = ride.currentSpeedKmH,
            )
        )
    }

    fun currentSessionSummary(): String {
        val ride = viewModel.telemetry.value
        if (!ride.isRecording) return "No activity session is recording."
        val minutes = ride.elapsedSeconds / 60
        val gps = if (ride.hasGpsFix && !ride.isDemo) "GPS fix confirmed" else "GPS fix not confirmed"
        return "${ride.activityType.displayName} session ${if (ride.isPaused) "paused" else "recording"}, %.1f kilometres, %d minutes, %s".format(
            java.util.Locale.US, ride.distanceKm, minutes, gps
        )
    }

    fun buildRealtimeGuideContext(): RealtimeAdventureContext {
        val currentActivity = viewModel.activityMode.value
        val preferences = guidePreferencesStore.load()
        return RealtimeAdventureContext(
            activityName = currentActivity.displayName,
            activityGuidance = "${currentActivity.homeHint} ${currentActivity.trackerSubtitle}",
            languageCode = viewModel.currentLang.value.code,
            guideMode = voiceController.state.value.guideMode,
            sessionSummary = currentSessionSummary(),
            preferencesSummary = guidePreferencesStore.spokenSummary(preferences),
            voicePreset = preferences.voicePreset,
            vehicleInMotion = currentVehicleIsMoving(),
        )
    }

    suspend fun getFreshVoicePlaceFix(): VoicePlaceFix? {
        if (!locationManager.hasLocationPermission()) return null
        val now = System.currentTimeMillis()
        val cached = locationManager.currentLocation.value
        if (cached != null && now - cached.timestamp in 0L..120_000L && cached.accuracyM <= 100f) {
            return VoicePlaceFix(
                latitude = cached.latitude,
                longitude = cached.longitude,
                headingDeg = cached.bearingDeg.toDouble(),
                speedMps = (cached.speedKmH / 3.6).coerceAtLeast(0.0),
                accuracyM = cached.accuracyM.toDouble(),
                timestampMillis = cached.timestamp,
            )
        }

        val cancellation = CancellationTokenSource()
        return try {
            withTimeoutOrNull(12_000) {
                val location = LocationServices.getFusedLocationProviderClient(context)
                    .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellation.token)
                    .await() ?: return@withTimeoutOrNull null
                val timestamp = location.time
                val accuracy = if (location.hasAccuracy()) location.accuracy.toDouble() else Double.POSITIVE_INFINITY
                if (System.currentTimeMillis() - timestamp !in 0L..120_000L || accuracy > 100.0) {
                    return@withTimeoutOrNull null
                }
                VoicePlaceFix(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    headingDeg = if (location.hasBearing()) location.bearing.toDouble() else null,
                    speedMps = if (location.hasSpeed()) location.speed.toDouble().coerceIn(0.0, 100.0) else 0.0,
                    accuracyM = accuracy,
                    timestampMillis = timestamp,
                )
            }
        } catch (_: Exception) {
            null
        } finally {
            cancellation.cancel()
        }
    }

    suspend fun requestNearbyContext(): Map<String, Any?> {
        val fix = getFreshVoicePlaceFix()
            ?: return mapOf("ok" to false, "message" to "I need a fresh, permission-authorized GPS fix to look up nearby places. I will not guess your location.")
        return try {
            val request = mutableMapOf<String, Any>(
                "latitude" to fix.latitude,
                "longitude" to fix.longitude,
                "speedMps" to fix.speedMps,
                "radiusMeters" to 250,
            )
            fix.headingDeg?.let { request["headingDeg"] = it }
            val result = FirebaseFunctions.getInstance("us-central1")
                .getHttpsCallable("getNearbyAdventurePlaces")
                .call(request)
                .await()
            val payload = result.data as? Map<*, *>
                ?: return mapOf("ok" to false, "message" to "Nearby research returned an invalid response.")
            val places = (payload["places"] as? List<*>)?.mapNotNull { raw ->
                val place = raw as? Map<*, *> ?: return@mapNotNull null
                val name = place["name"] as? String ?: return@mapNotNull null
                val distance = (place["distanceMeters"] as? Number)?.toInt()
                val direction = listOfNotNull(place["relativeDirection"] as? String, place["relativeClock"] as? String).joinToString(", ")
                mapOf(
                    "name" to name.take(100),
                    "category" to ((place["category"] as? String).orEmpty().take(80)),
                    "distance_meters" to (distance ?: 0),
                    "relative_direction" to direction,
                    "source_url" to ((place["mapUrl"] as? String).orEmpty().take(200)),
                )
            }?.take(5).orEmpty()
            val wiki = payload["wikipedia"] as? Map<*, *>
            val fact = if (wiki != null) mapOf(
                "title" to ((wiki["title"] as? String).orEmpty().take(100)),
                "summary" to ((wiki["summary"] as? String).orEmpty().take(500)),
                "source" to ((wiki["source"] as? String).orEmpty()),
                "url" to ((wiki["url"] as? String).orEmpty().take(300)),
            ) else null
            mapOf(
                "ok" to true,
                "source" to "OpenStreetMap contributors",
                "source_url" to "https://www.openstreetmap.org/copyright",
                "freshness" to ((payload["freshness"] as? String).orEmpty()),
                "places" to places,
                "wiki_fact" to (fact ?: emptyMap<String, String>()),
                "caveat" to ((payload["note"] as? String).orEmpty().take(400)),
            )
        } catch (error: Exception) {
            mapOf(
                "ok" to false,
                "message" to when {
                    error.message.orEmpty().contains("resource-exhausted", true) -> "Nearby research is cooling down. Use the recently prefetched context if available."
                    error.message.orEmpty().contains("unauthenticated", true) -> "Secure sign-in is not ready for nearby research."
                    else -> "Nearby-place research is temporarily unavailable. Do not guess a landmark or current conditions."
                }
            )
        }
    }

    suspend fun requestAdventurePlaceFacts(placeName: String): Map<String, Any?> {
        val query = placeName.trim().take(120)
        if (query.isBlank()) return mapOf("ok" to false, "message" to "A specific place name is required; I won't guess the name of an unnamed object.")
        return try {
            val response = FirebaseFunctions.getInstance("us-central1")
                .getHttpsCallable("lookupAdventurePlaceFacts")
                .call(mapOf("placeName" to query))
                .await()
            val payload = response.data as? Map<*, *>
                ?: return mapOf("ok" to false, "message" to "The place-fact service returned an invalid response.")
            val found = payload["found"] == true
            val fact = payload["fact"] as? Map<*, *>
            if (!found || fact == null) {
                mapOf(
                    "ok" to false,
                    "message" to ((payload["message"] as? String).orEmpty().ifBlank {
                        "No exact-title source-backed summary was found. Please provide more context rather than guessing."
                    }),
                    "source" to "Wikipedia",
                    "source_url" to ((payload["sourceUrl"] as? String).orEmpty().take(240)),
                )
            } else {
                AdventureFunnelAnalytics.log(context, "voice_place_fact_requested")
                mapOf(
                    "ok" to true,
                    "title" to ((fact["title"] as? String).orEmpty().take(120)),
                    "summary" to ((fact["summary"] as? String).orEmpty().take(600)),
                    "source" to ((fact["source"] as? String).orEmpty().ifBlank { "Wikipedia" }),
                    "source_url" to ((fact["url"] as? String).orEmpty().take(280)),
                    "caveat" to ((payload["note"] as? String).orEmpty().take(400)),
                )
            }
        } catch (_: Exception) {
            mapOf("ok" to false, "message" to "Place facts are temporarily unavailable. I don't have a verified summary to use.")
        }
    }

    fun startLiveGuide() {
        voiceController.disableHandsFree()
        voiceController.stopSpeaking()
        if (currentVehicleIsMoving()) {
            liveGuideSession.disconnect("Live Guide is paused for safety. Pull over and stop the vehicle before starting a conversation.")
            return
        }
        val currentActivity = viewModel.activityMode.value
        if (currentActivity == ActivityType.Motorcycling || currentActivity == ActivityType.CarTrips) {
            val ride = viewModel.telemetry.value
            if (!ride.isRecording || ride.isDemo || !ride.hasGpsFix) {
                liveGuideSession.disconnect("For vehicle activities, start a real GPS tracker session while stationary before starting Live Guide. The guide stops automatically when motion is detected.")
                return
            }
        }
        val sessionContext = buildRealtimeGuideContext().copy(vehicleInMotion = false)
        AdventureFunnelAnalytics.log(context, "live_voice_connect_requested", mapOf("activity_type" to currentActivity.name))
        appScope.launch {
            liveGuideSession.connect(sessionContext)
            if (liveGuideSession.state.value.isConnected) {
                // Prefetch a small, user-visible-context-only place list while the user begins the
                // conversation. It is injected as context without forcing an unsolicited response.
                val prefetch = requestNearbyContext()
                if (prefetch["ok"] == true && liveGuideSession.state.value.isConnected) {
                    val note = buildString {
                        append("[PREFETCHED_NEARBY_CONTEXT] Source: OpenStreetMap contributors. ")
                        append("The following are community-map candidates, not visual identification or verified safety/access information. ")
                        append("Candidate data: ")
                        append((prefetch["places"] as? List<*>)?.joinToString("; ") { it.toString().take(180) }.orEmpty().take(1200))
                        val fact = prefetch["wiki_fact"] as? Map<*, *>
                        val wikiSummary = (fact?.get("summary") as? String).orEmpty()
                        if (wikiSummary.isNotBlank()) {
                            append(" Wikipedia source-backed introductory summary: ")
                            append(wikiSummary.take(500))
                            append(" Source URL: ")
                            append(fact["url"].toString().take(250))
                        }
                        append(" Do not mention or describe any candidate unless the user asks; never infer what the user is looking at. These results are not navigation or current conditions.")
                    }
                    liveGuideSession.sendContextNote(note)
                    AdventureFunnelAnalytics.log(context, "live_voice_nearby_prefetch", mapOf("result_count" to ((prefetch["places"] as? List<*>)?.size ?: 0)))
                }
            }
        }
    }

    var pendingVoiceStart by remember { mutableStateOf(VoiceStartRequest.PUSH_TO_TALK) }
    val voicePermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            when (pendingVoiceStart) {
                VoiceStartRequest.PUSH_TO_TALK -> voiceController.startOneShot()
                VoiceStartRequest.HANDS_FREE -> voiceController.enableHandsFree()
                VoiceStartRequest.REALTIME_GUIDE -> startLiveGuide()
            }
        } else voiceController.permissionDenied()
    }
    val requestVoice: (VoiceStartRequest) -> Unit = { request ->
        pendingVoiceStart = request
        when (request) {
            VoiceStartRequest.HANDS_FREE -> AdventureFunnelAnalytics.log(context, "voice_hands_free_enabled")
            VoiceStartRequest.PUSH_TO_TALK -> AdventureFunnelAnalytics.log(context, "voice_push_to_talk_started")
            VoiceStartRequest.REALTIME_GUIDE -> Unit // Recorded after permission is granted and connection is attempted.
        }
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            when (request) {
                VoiceStartRequest.PUSH_TO_TALK -> {
                    liveGuideSession.disconnect("Live Guide paused for Talk Now.")
                    voiceController.startOneShot()
                }
                VoiceStartRequest.HANDS_FREE -> {
                    liveGuideSession.disconnect("Live Guide paused for hands-free voice commands.")
                    voiceController.enableHandsFree()
                }
                VoiceStartRequest.REALTIME_GUIDE -> startLiveGuide()
            }
        } else voicePermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    LaunchedEffect(voiceController, viewModel) {
        voiceController.updateLanguage(viewModel.currentLang.value.code)

        fun vehicleIsMoving(): Boolean {
            val ride = viewModel.telemetry.value
            return AdventureVoiceSafetyPolicy.isVehicleInMotion(
                VoiceMotionSnapshot(
                    activityType = ride.activityType,
                    isRecording = ride.isRecording,
                    isPaused = ride.isPaused,
                    hasGpsFix = ride.hasGpsFix,
                    isDemo = ride.isDemo,
                    speedKmH = ride.currentSpeedKmH,
                )
            )
        }

        voiceController.setCommandHandler { command: ParsedAdventureVoiceCommand ->
            if (command.action != AdventureVoiceAction.IGNORED && command.action != AdventureVoiceAction.UNKNOWN) {
                // Never log the recognized words, query string, or location—only the coarse action.
                AdventureFunnelAnalytics.log(context, "voice_command_executed", mapOf("command_type" to command.action.name))
            }
            when (command.action) {
                AdventureVoiceAction.HELP -> VoiceCommandReply("You can say: open Explore, open Story Studio, go back, next screen, switch to trekking, give me an activity briefing, read my trip status, pause recording, or search for a place.")
                AdventureVoiceAction.BACK -> VoiceCommandReply(if (viewModel.navigateBack()) "Going back." else "You're already at the home screen.")
                AdventureVoiceAction.NEXT_SCREEN -> {
                    viewModel.navigateToNextTopLevel()
                    VoiceCommandReply("Opening the next main screen.")
                }
                AdventureVoiceAction.PREVIOUS_SCREEN -> {
                    viewModel.navigateToPreviousTopLevel()
                    VoiceCommandReply("Opening the previous main screen.")
                }
                AdventureVoiceAction.OPEN_FEED -> {
                    viewModel.navigateTo(Screen.FEED)
                    VoiceCommandReply("Opening your adventure feed.")
                }
                AdventureVoiceAction.OPEN_EXPLORE -> {
                    viewModel.navigateTo(Screen.EXPLORE_PLAN)
                    VoiceCommandReply("Opening Explore and Plan.")
                }
                AdventureVoiceAction.OPEN_TRACKER -> {
                    viewModel.navigateTo(Screen.LIVE_TRACKER)
                    VoiceCommandReply("Opening your activity tracker.")
                }
                AdventureVoiceAction.OPEN_VOICE -> {
                    viewModel.navigateTo(Screen.VOICE_COMPANION)
                    VoiceCommandReply("Opening the Voice Companion.")
                }
                AdventureVoiceAction.OPEN_STORY_STUDIO -> {
                    viewModel.navigateTo(Screen.STORY_STUDIO)
                    VoiceCommandReply("Opening Story Studio. Your existing activity-aware story tools are ready.")
                }
                AdventureVoiceAction.OPEN_RADAR -> {
                    viewModel.navigateTo(Screen.RADAR)
                    VoiceCommandReply("Opening Roadside Radar and Safety Help.")
                }
                AdventureVoiceAction.OPEN_PROFILE -> {
                    viewModel.navigateTo(Screen.PROFILE)
                    VoiceCommandReply("Opening your profile.")
                }
                AdventureVoiceAction.START_ACTIVITY -> {
                    if (viewModel.telemetry.value.isRecording) {
                        VoiceCommandReply("An activity is already recording. Say pause recording, or open tracker for controls.")
                    } else {
                        val hasLocation = viewModel.locationManager?.hasLocationPermission() == true
                        viewModel.startRide()
                        VoiceCommandReply(
                            if (hasLocation) "Activity recording started. Keep your attention on the environment."
                            else "A demo session was started because location permission is unavailable. Enable location before relying on a real GPS track."
                        )
                    }
                }
                AdventureVoiceAction.PAUSE_ACTIVITY -> {
                    val ride = viewModel.telemetry.value
                    if (!ride.isRecording) VoiceCommandReply("There is no active session to pause.")
                    else if (ride.isPaused) VoiceCommandReply("Your session is already paused.")
                    else {
                        viewModel.pauseRide()
                        VoiceCommandReply("Session paused.")
                    }
                }
                AdventureVoiceAction.RESUME_ACTIVITY -> {
                    val ride = viewModel.telemetry.value
                    if (!ride.isRecording) VoiceCommandReply("There is no active session to resume.")
                    else if (!ride.isPaused) VoiceCommandReply("Your session is already running.")
                    else {
                        viewModel.resumeRide()
                        VoiceCommandReply("Session resumed.")
                    }
                }
                AdventureVoiceAction.END_ACTIVITY -> {
                    viewModel.navigateTo(Screen.LIVE_TRACKER)
                    VoiceCommandReply("Opening the tracker so you can finish the session with its controls. I won't discard trip data by voice.")
                }
                AdventureVoiceAction.ACTIVITY_BRIEFING -> {
                    val activity = viewModel.activityMode.value
                    if (vehicleIsMoving()) {
                        VoiceCommandReply("I'll keep it brief while your vehicle is moving. Please pull over safely before requesting a full activity briefing.")
                    } else {
                        val detail = if (voiceController.state.value.guideMode == AdventureGuideMode.TOUR) {
                            "${activity.displayName}. ${activity.homeHint} ${activity.trackerSubtitle}"
                        } else "${activity.displayName}. ${activity.homeHint}"
                        VoiceCommandReply("Activity briefing. ${detail.take(360)}")
                    }
                }
                AdventureVoiceAction.RIDE_STATUS -> {
                    val ride = viewModel.telemetry.value
                    if (!ride.isRecording) VoiceCommandReply("No activity is recording right now. The selected activity is ${viewModel.activityMode.value.displayName}.")
                    else {
                        val minutes = ride.elapsedSeconds / 60
                        val gpsNote = if (ride.hasGpsFix && !ride.isDemo) " GPS is connected." else " GPS fix is not confirmed; don't rely on this as a real route."
                        VoiceCommandReply("Your ${ride.activityType.displayName} session is ${if (ride.isPaused) "paused" else "recording"}. ${String.format(java.util.Locale.US, "%.1f", ride.distanceKm)} kilometres recorded over ${minutes} minutes.${gpsNote}")
                    }
                }
                AdventureVoiceAction.SAFETY_HELP -> {
                    viewModel.navigateTo(Screen.RADAR)
                    VoiceCommandReply("Opening Safety and Help. I will not trigger an SOS from speech alone; confirm emergency actions on screen.")
                }
                AdventureVoiceAction.NEARBY_CONTEXT -> VoiceCommandReply("I don't have a verified live nearby-place feed connected yet, so I can't identify an object reliably from “what's that”. Say “search for” and a place name to open a map search.")
                AdventureVoiceAction.SEARCH_PLACE -> {
                    val query = command.query.orEmpty().trim()
                    if (vehicleIsMoving()) {
                        VoiceCommandReply("I won't switch apps while your vehicle is moving. Please pull over safely, then ask me to search again.")
                    } else if (query.isBlank()) VoiceCommandReply("Please say the place name after “search for”.")
                    else {
                        val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(query)}"))
                        VoiceCommandReply("Opening an external map search for $query. Adventurer hasn't verified these results or current conditions.", afterSpeech = {
                            runCatching { context.startActivity(mapIntent) }
                        })
                    }
                }
                AdventureVoiceAction.SELECT_ACTIVITY -> {
                    val activity = command.activity
                    if (activity == null) VoiceCommandReply("I couldn't match that activity. Try motorcycling, cycling, trekking, camping, fishing, scuba, kayaking, or exploring.")
                    else {
                        viewModel.setActivityMode(activity)
                        VoiceCommandReply("Activity changed to ${activity.displayName}.")
                    }
                }
                AdventureVoiceAction.GUIDED_MODE -> {
                    voiceController.setGuideMode(AdventureGuideMode.TOUR)
                    VoiceCommandReply("Tour mode selected. Ask for an activity briefing whenever you want a longer guide summary.")
                }
                AdventureVoiceAction.WANDER_MODE -> {
                    voiceController.setGuideMode(AdventureGuideMode.WANDER)
                    VoiceCommandReply("Wander mode selected. I'll keep answers concise and wait for you to ask.")
                }
                AdventureVoiceAction.STOP_SPEAKING, AdventureVoiceAction.DISABLE_HANDS_FREE, AdventureVoiceAction.REPEAT_LAST, AdventureVoiceAction.WAKE_ONLY, AdventureVoiceAction.IGNORED -> VoiceCommandReply("", silent = true)
                AdventureVoiceAction.UNKNOWN -> VoiceCommandReply("I didn't understand that. Try open Explore, go back, switch to trekking, give me an activity briefing, or search for a place.")
            }
        }
    }

    LaunchedEffect(locationManager) {
        viewModel.attachLocationManager(locationManager)
    }

    val currentScreen by viewModel.currentScreen.collectAsState()
    val showCreatePost by viewModel.showCreatePost.collectAsState()
    val activeCommentIndex by viewModel.activeCommentPostIndex.collectAsState()
    val showSosAlert by viewModel.showSosAlert.collectAsState()
    val feedItems by viewModel.feed.collectAsState()
    val telemetry by viewModel.telemetry.collectAsState()
    val activityMode by viewModel.activityMode.collectAsState()
    val lang by viewModel.currentLang.collectAsState()

    LaunchedEffect(liveGuideSession, viewModel, locationManager) {
        liveGuideSession.setToolHandler { toolName, arguments ->
            when (toolName) {
                "get_nearby_adventure_places" -> requestNearbyContext()
                "get_adventure_place_facts" -> requestAdventurePlaceFacts(arguments["place_name"] as? String ?: "")
                "save_adventure_guide_preferences" -> {
                    val old = guidePreferencesStore.load()
                    val requestedInterests = (arguments["interests"] as? List<*>)
                        ?.filterIsInstance<String>()
                        ?.toSet()
                        ?: old.interests
                    val updated = guidePreferencesStore.save(
                        old.copy(
                            interests = requestedInterests,
                            storyStyle = arguments["story_style"] as? String ?: old.storyStyle,
                            proactivity = arguments["proactivity"] as? String ?: old.proactivity,
                            voicePreset = arguments["voice_preset"] as? String ?: old.voicePreset,
                        )
                    )
                    liveGuideSession.updateGuidePreferences(buildRealtimeGuideContext())
                    AdventureFunnelAnalytics.log(context, "voice_guide_preferences_saved")
                    mapOf(
                        "ok" to true,
                        "summary" to "Guide preferences were saved on this device.",
                        "preferences" to guidePreferencesStore.spokenSummary(updated),
                    )
                }
                "execute_adventurer_action" -> {
                    val action = arguments["action"] as? String
                    if (action.isNullOrBlank()) {
                        mapOf("ok" to false, "message" to "The requested action is missing.")
                    } else if (currentVehicleIsMoving() && action !in setOf("pause_activity", "open_safety_help")) {
                        mapOf("ok" to false, "message" to "The live guide has a vehicle-motion safety guard. Pull over and stop before continuing.")
                    } else {
                        when (action) {
                            "go_back" -> {
                                val changed = viewModel.navigateBack()
                                mapOf("ok" to true, "summary" to if (changed) "Returned to the previous screen." else "Already at home.")
                            }
                            "next_screen" -> {
                                viewModel.navigateToNextTopLevel()
                                mapOf("ok" to true, "summary" to "Opened the next main app screen.")
                            }
                            "previous_screen" -> {
                                viewModel.navigateToPreviousTopLevel()
                                mapOf("ok" to true, "summary" to "Opened the previous main app screen.")
                            }
                            "open_feed" -> {
                                viewModel.navigateTo(Screen.FEED)
                                mapOf("ok" to true, "summary" to "Opened the adventure feed.")
                            }
                            "open_explore" -> {
                                viewModel.navigateTo(Screen.EXPLORE_PLAN)
                                mapOf("ok" to true, "summary" to "Opened Explore and Plan.")
                            }
                            "open_tracker" -> {
                                viewModel.navigateTo(Screen.LIVE_TRACKER)
                                mapOf("ok" to true, "summary" to "Opened the activity tracker.")
                            }
                            "open_story_studio" -> {
                                viewModel.navigateTo(Screen.STORY_STUDIO)
                                mapOf("ok" to true, "summary" to "Opened Story Studio.")
                            }
                            "open_safety_help" -> {
                                viewModel.navigateTo(Screen.RADAR)
                                mapOf("ok" to true, "summary" to "Opened Safety and Help. No SOS was dispatched.")
                            }
                            "open_profile" -> {
                                viewModel.navigateTo(Screen.PROFILE)
                                mapOf("ok" to true, "summary" to "Opened the profile.")
                            }
                            "set_activity" -> {
                                val activityName = arguments["activity_type"] as? String
                                val requestedActivity = ActivityType.values().firstOrNull { it.name == activityName }
                                if (requestedActivity == null) mapOf("ok" to false, "message" to "That activity is not supported.")
                                else {
                                    viewModel.setActivityMode(requestedActivity)
                                    mapOf("ok" to true, "summary" to "Selected activity ${requestedActivity.displayName}.")
                                }
                            }
                            "start_activity" -> {
                                if (viewModel.telemetry.value.isRecording) mapOf("ok" to false, "message" to "A session is already recording.")
                                else if (viewModel.locationManager?.hasLocationPermission() != true) mapOf("ok" to false, "message" to "Location permission is required. Open Tracker and grant location permission before starting a voice-controlled real GPS session.")
                                else {
                                    viewModel.startRide()
                                    mapOf("ok" to true, "summary" to "The activity tracker started. Keep attention on your environment.")
                                }
                            }
                            "pause_activity" -> {
                                val ride = viewModel.telemetry.value
                                if (!ride.isRecording) mapOf("ok" to false, "message" to "There is no active session to pause.")
                                else if (ride.isPaused) mapOf("ok" to true, "summary" to "The session is already paused.")
                                else {
                                    viewModel.pauseRide()
                                    mapOf("ok" to true, "summary" to "The session was paused.")
                                }
                            }
                            "resume_activity" -> {
                                val ride = viewModel.telemetry.value
                                if (!ride.isRecording) mapOf("ok" to false, "message" to "There is no active session to resume.")
                                else if (!ride.isPaused) mapOf("ok" to true, "summary" to "The session is already running.")
                                else if (currentVehicleIsMoving()) mapOf("ok" to false, "message" to "Pull over and stop before resuming with the voice agent.")
                                else {
                                    viewModel.resumeRide()
                                    mapOf("ok" to true, "summary" to "The session was resumed.")
                                }
                            }
                            "read_status" -> {
                                if (currentVehicleIsMoving()) mapOf("ok" to false, "message" to "Live Guide has been paused for vehicle movement.")
                                else mapOf("ok" to true, "summary" to currentSessionSummary())
                            }
                            "activity_briefing" -> {
                                if (currentVehicleIsMoving()) mapOf("ok" to false, "message" to "A longer briefing is blocked while the vehicle is moving. Pull over safely first.")
                                else {
                                    val activity = viewModel.activityMode.value
                                    mapOf("ok" to true, "summary" to "${activity.displayName}. ${activity.homeHint} ${activity.trackerSubtitle}".take(650))
                                }
                            }
                            else -> mapOf("ok" to false, "message" to "This action is not enabled.")
                        }
                    }
                }
                else -> mapOf("ok" to false, "message" to "That tool is not registered by Adventurer.")
            }
        }
    }

    LaunchedEffect(
        liveGuideSession,
        telemetry.isRecording,
        telemetry.isPaused,
        telemetry.hasGpsFix,
        telemetry.isDemo,
        telemetry.currentSpeedKmH,
        activityMode,
    ) {
        if (currentVehicleIsMoving() && liveGuideSession.state.value.isConnected) {
            liveGuideSession.disconnect("Live Guide stopped because vehicle movement was detected. Stop in a safe place before starting a new conversation.")
        }
    }

    LaunchedEffect(liveGuideSession) {
        var wasConnected = false
        liveGuideSession.state.collect { state ->
            if (!wasConnected && state.isConnected) AdventureFunnelAnalytics.log(context, "live_voice_connected")
            if (wasConnected && !state.isConnected) AdventureFunnelAnalytics.log(context, "live_voice_disconnected")
            wasConnected = state.isConnected
        }
    }

    LaunchedEffect(currentScreen) {
        if (currentScreen == Screen.VOICE_COMPANION) {
            AdventureFunnelAnalytics.log(context, "voice_assistant_open")
        }
    }

    LaunchedEffect(lang) {
        voiceController.updateLanguage(lang.code)
    }

    // Detail screens get the whole screen (they have their own back arrow); the HUD hides the
    // tab bar while a ride is recording so a stray tap can't navigate away mid-corner.
    val ridingOnHud = currentScreen == Screen.LIVE_TRACKER && telemetry.isRecording
    val showBottomBar = currentScreen.isTopLevel && !ridingOnHud

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            Column {
                AnimatedVisibility(visible = telemetry.isRecording && currentScreen != Screen.LIVE_TRACKER) {
                    ActiveRideBanner(
                        distanceKm = telemetry.distanceKm,
                        elapsed = formatRideDuration(telemetry.elapsedSeconds),
                        paused = telemetry.isPaused,
                        title = if (telemetry.activityType == ActivityType.Motorcycling) Str.RIDE_ACTIVE.get(lang) else "${telemetry.activityType.displayName} in progress",
                        action = if (telemetry.activityType == ActivityType.Motorcycling) Str.OPEN_HUD.get(lang) else "Open tracker",
                        padForSystemBar = !showBottomBar,
                        onOpen = { viewModel.navigateTo(Screen.LIVE_TRACKER) }
                    )
                }
                AnimatedVisibility(visible = showBottomBar, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                    NavigationBar(
                        containerColor = Color(0xFF0F172A),
                        contentColor = Color.White,
                        modifier = Modifier.testTag("main_bottom_nav")
                    ) {
                        NavTabs.forEach { tab ->
                            val selected = currentScreen == tab.screen
                            NavigationBarItem(
                                selected = selected,
                                onClick = { viewModel.navigateTo(tab.screen) },
                                icon = {
                                    BadgedBox(badge = {
                                        if (tab.screen == Screen.LIVE_TRACKER && telemetry.isRecording) Badge(containerColor = Color(0xFFEF4444))
                                    }) {
                                        // The label below already names the tab, so the icon is decorative.
                                        Icon(if (selected) tab.selected else tab.unselected, contentDescription = null)
                                    }
                                },
                                label = {
                                    Text(
                                        if (tab.screen == Screen.LIVE_TRACKER && activityMode != ActivityType.Motorcycling) "Track"
                                        else tab.label.get(lang),
                                        maxLines = 1
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.Black,
                                    selectedTextColor = tab.accent,
                                    indicatorColor = tab.accent,
                                    unselectedIconColor = Color(0xFF94A3B8),
                                    unselectedTextColor = Color(0xFF94A3B8)
                                ),
                                modifier = Modifier.testTag("nav_${tab.screen.name.lowercase()}")
                            )
                        }
                    }
                }
            }
        },
        containerColor = Color(0xFF0B0F17)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    when {
                        // Opening a detail screen: slide in from the right.
                        !targetState.isTopLevel && initialState.isTopLevel ->
                            (slideInHorizontally { it / 4 } + fadeIn()) togetherWith fadeOut()
                        // Closing a detail screen: slide it away to the right.
                        targetState.isTopLevel && !initialState.isTopLevel ->
                            fadeIn() togetherWith (slideOutHorizontally { it / 4 } + fadeOut())
                        else -> fadeIn(tween(180)) togetherWith fadeOut(tween(120))
                    }
                },
                label = "screen_transition"
            ) { screen ->
                when (screen) {
                    Screen.FEED -> FeedScreen(viewModel = viewModel)
                    Screen.EXPLORE_PLAN -> AdventurePlannerScreen(viewModel = viewModel, plannerRepository = plannerRepository)
                    Screen.VOICE_COMPANION -> AdventureVoiceCompanionScreen(
                        viewModel = viewModel,
                        controller = voiceController,
                        liveGuideSession = liveGuideSession,
                        preferencesStore = guidePreferencesStore,
                        onGuidePreferencesChanged = { liveGuideSession.updateGuidePreferences(buildRealtimeGuideContext()) },
                        onRequestVoice = requestVoice,
                    )
                    Screen.EXPEDITIONS -> ExpeditionsScreen(viewModel = viewModel)
                    Screen.RADAR -> RoadsideRadarScreen(viewModel = viewModel)
                    Screen.LIVE_TRACKER -> GpsTrackerScreen(viewModel = viewModel)
                    Screen.STORY_STUDIO -> AiStoryStudioScreen(viewModel = viewModel)
                    Screen.GAMIFICATION -> GamificationScreen(viewModel = viewModel)
                    Screen.CLUBS -> ClubsScreen(viewModel = viewModel)
                    Screen.PROFILE -> ProfileScreen(viewModel = viewModel)
                    Screen.PRO_PAYWALL -> ProPaywallScreen(viewModel = viewModel)
                }
            }
            if (currentScreen != Screen.VOICE_COMPANION) {
                AdventureVoiceBubble(
                    controller = voiceController,
                    onRequestVoice = requestVoice,
                    modifier = Modifier.align(Alignment.BottomEnd).padding(end = 14.dp, bottom = 14.dp),
                )
            }
        }
    }

    // Modal Overlays
    if (showCreatePost) {
        CreatePostDialog(
            onDismiss = { viewModel.setShowCreatePost(false) },
            onSubmit = { caption, imageUrl ->
                viewModel.createPost(caption, imageUrl)
            }
        )
    }

    activeCommentIndex?.let { index ->
        val postComments = when (val item = feedItems.getOrNull(index)) {
            is FeedItem.PostItem -> item.post.comments
            is FeedItem.RideShareItem -> item.rideShare.comments
            else -> emptyList()
        }
        CommentsBottomSheet(
            comments = postComments,
            onDismiss = { viewModel.openCommentSheet(null) },
            onAddComment = { text -> viewModel.addComment(index, text) }
        )
    }

    if (showSosAlert) {
        SafetySosDialog(
            onDismiss = { viewModel.setShowSosAlert(false) },
            locationManager = viewModel.locationManager,
            liveLatitude = telemetry.latitude.takeIf { telemetry.hasGpsFix },
            liveLongitude = telemetry.longitude.takeIf { telemetry.hasGpsFix },
        )
    }
}

/** Shown above the tab bar whenever a ride is recording and the rider is on another screen. */
@Composable
private fun ActiveRideBanner(distanceKm: Double, elapsed: String, paused: Boolean, title: String, action: String, padForSystemBar: Boolean, onOpen: () -> Unit) {
    Surface(
        onClick = onOpen,
        color = if (paused) AmberGold else AdventureOrange,
        modifier = Modifier.fillMaxWidth().testTag("active_ride_banner")
    ) {
        Row(
            modifier = Modifier
                .then(if (padForSystemBar) Modifier.navigationBarsPadding() else Modifier)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(if (paused) Icons.Filled.Pause else Icons.Filled.FiberManualRecord, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                text = (if (paused) "⏸ " else "") + title + " · %.1f km · %s".format(distanceKm, elapsed),
                color = Color.Black, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.weight(1f)
            )
            Text(action, color = Color.Black, style = MaterialTheme.typography.labelLarge)
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color.Black)
        }
    }
}
