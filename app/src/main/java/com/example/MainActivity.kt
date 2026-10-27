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
import com.example.data.voice.AdventureGuideMode
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
    val locationManager = remember { com.example.data.location.LocationManager(context.applicationContext) }
    val voiceController = remember(context) { AdventureVoiceController(context) }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner, voiceController) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> voiceController.setForegroundActive(true)
                Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP -> voiceController.setForegroundActive(false)
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        voiceController.setForegroundActive(lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED))
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            voiceController.shutdown()
        }
    }

    var pendingVoiceStart by remember { mutableStateOf(VoiceStartRequest.PUSH_TO_TALK) }
    val voicePermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            when (pendingVoiceStart) {
                VoiceStartRequest.PUSH_TO_TALK -> voiceController.startOneShot()
                VoiceStartRequest.HANDS_FREE -> voiceController.enableHandsFree()
            }
        } else voiceController.permissionDenied()
    }
    val requestVoice: (VoiceStartRequest) -> Unit = { request ->
        pendingVoiceStart = request
        AdventureFunnelAnalytics.log(
            context,
            if (request == VoiceStartRequest.HANDS_FREE) "voice_hands_free_enabled" else "voice_push_to_talk_started"
        )
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            when (request) {
                VoiceStartRequest.PUSH_TO_TALK -> voiceController.startOneShot()
                VoiceStartRequest.HANDS_FREE -> voiceController.enableHandsFree()
            }
        } else voicePermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    LaunchedEffect(voiceController, viewModel) {
        voiceController.updateLanguage(viewModel.currentLang.value.code)
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
                    val detail = if (voiceController.state.value.guideMode == AdventureGuideMode.TOUR) {
                        "${activity.displayName}. ${activity.homeHint} ${activity.trackerSubtitle}"
                    } else "${activity.displayName}. ${activity.homeHint}"
                    VoiceCommandReply("Activity briefing. ${detail.take(360)}")
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
                    if (query.isBlank()) VoiceCommandReply("Please say the place name after “search for”.")
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
                    Screen.VOICE_COMPANION -> AdventureVoiceCompanionScreen(viewModel = viewModel, controller = voiceController, onRequestVoice = requestVoice)
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
