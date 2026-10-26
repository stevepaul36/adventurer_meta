package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
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
import com.example.data.model.FeedItem
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
        setContent {
            AdventurerTheme(darkTheme = true) {
                AdventurerApp(viewModel = viewModel)
            }
        }
    }
}

private data class NavTab(val screen: Screen, val label: Str, val selected: ImageVector, val unselected: ImageVector, val accent: Color)

private val NavTabs = listOf(
    NavTab(Screen.FEED, Str.NAV_FEED, Icons.Filled.Home, Icons.Outlined.Home, AdventureOrange),
    NavTab(Screen.EXPEDITIONS, Str.NAV_PASSES, Icons.Filled.Terrain, Icons.Outlined.Terrain, AdventureOrange),
    NavTab(Screen.LIVE_TRACKER, Str.NAV_RIDE, Icons.Filled.Speed, Icons.Outlined.Speed, AdventureOrange),
    NavTab(Screen.RADAR, Str.NAV_SOS, Icons.Filled.Emergency, Icons.Outlined.Emergency, Color(0xFFEF4444)),
    NavTab(Screen.PROFILE, Str.NAV_RIDER, Icons.Filled.Person, Icons.Outlined.Person, AmberGold),
)

@Composable
fun AdventurerApp(viewModel: AdventurerViewModel) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val locationManager = remember { com.example.data.location.LocationManager(context.applicationContext) }

    LaunchedEffect(locationManager) {
        viewModel.attachLocationManager(locationManager)
    }

    val currentScreen by viewModel.currentScreen.collectAsState()
    val showCreatePost by viewModel.showCreatePost.collectAsState()
    val activeCommentIndex by viewModel.activeCommentPostIndex.collectAsState()
    val showSosAlert by viewModel.showSosAlert.collectAsState()
    val feedItems by viewModel.feed.collectAsState()
    val telemetry by viewModel.telemetry.collectAsState()
    val lang by viewModel.currentLang.collectAsState()

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
                        title = Str.RIDE_ACTIVE.get(lang),
                        action = Str.OPEN_HUD.get(lang),
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
                                label = { Text(tab.label.get(lang), maxLines = 1) },
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
                    Screen.EXPEDITIONS -> ExpeditionsScreen(viewModel = viewModel)
                    Screen.RADAR -> RoadsideRadarScreen(viewModel = viewModel)
                    Screen.LIVE_TRACKER -> GpsTrackerScreen(viewModel = viewModel)
                    Screen.STORY_STUDIO -> AiStoryStudioScreen(viewModel = viewModel)
                    Screen.APEX_MOTO -> ApexMotoGameScreen(viewModel = viewModel)
                    Screen.GAMIFICATION -> GamificationScreen(viewModel = viewModel)
                    Screen.CLUBS -> ClubsScreen(viewModel = viewModel)
                    Screen.PROFILE -> ProfileScreen(viewModel = viewModel)
                }
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
