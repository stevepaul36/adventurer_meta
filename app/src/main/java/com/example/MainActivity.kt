package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
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
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory

class MainActivity : ComponentActivity() {

    private val viewModel: AdventurerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Install App Check before the first Firebase AI Logic request. Firebase is optional
        // for local UI previews; live AI requires a configured google-services.json.
        if (FirebaseApp.getApps(this).isNotEmpty()) {
            FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
                if (BuildConfig.DEBUG) {
                    DebugAppCheckProviderFactory.getInstance()
                } else {
                    PlayIntegrityAppCheckProviderFactory.getInstance()
                }
            )
        }

        enableEdgeToEdge()
        setContent {
            AdventurerTheme(darkTheme = true) {
                AdventurerApp(viewModel = viewModel)
            }
        }
    }
}

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

    Scaffold(
        bottomBar = {
            // Main bottom navigation bar for top-level tabs
            NavigationBar(
                containerColor = Color(0xFF0F172A),
                contentColor = Color.White,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("main_bottom_nav")
            ) {
                NavigationBarItem(
                    selected = currentScreen == Screen.FEED,
                    onClick = { viewModel.navigateTo(Screen.FEED) },
                    icon = {
                        Icon(
                            imageVector = if (currentScreen == Screen.FEED) Icons.Filled.Home else Icons.Outlined.Home,
                            contentDescription = "Feed"
                        )
                    },
                    label = { Text("Feed") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = AdventureOrange,
                        indicatorColor = AdventureOrange,
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF94A3B8)
                    ),
                    modifier = Modifier.testTag("nav_feed")
                )

                NavigationBarItem(
                    selected = currentScreen == Screen.EXPEDITIONS,
                    onClick = { viewModel.navigateTo(Screen.EXPEDITIONS) },
                    icon = {
                        Icon(
                            imageVector = if (currentScreen == Screen.EXPEDITIONS) Icons.Filled.Terrain else Icons.Outlined.Terrain,
                            contentDescription = "Passes"
                        )
                    },
                    label = { Text("Passes") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = AdventureOrange,
                        indicatorColor = AdventureOrange,
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF94A3B8)
                    ),
                    modifier = Modifier.testTag("nav_expeditions")
                )

                NavigationBarItem(
                    selected = currentScreen == Screen.LIVE_TRACKER,
                    onClick = { viewModel.navigateTo(Screen.LIVE_TRACKER) },
                    icon = {
                        Icon(
                            imageVector = if (currentScreen == Screen.LIVE_TRACKER) Icons.Filled.Speed else Icons.Outlined.Speed,
                            contentDescription = "HUD"
                        )
                    },
                    label = { Text("HUD") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = AdventureOrange,
                        indicatorColor = AdventureOrange,
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF94A3B8)
                    ),
                    modifier = Modifier.testTag("nav_telemetry")
                )

                NavigationBarItem(
                    selected = currentScreen == Screen.RADAR,
                    onClick = { viewModel.navigateTo(Screen.RADAR) },
                    icon = {
                        Icon(
                            imageVector = if (currentScreen == Screen.RADAR) Icons.Filled.Emergency else Icons.Outlined.Emergency,
                            contentDescription = "Radar"
                        )
                    },
                    label = { Text("Radar") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = Color(0xFFEF4444),
                        indicatorColor = Color(0xFFEF4444),
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF94A3B8)
                    ),
                    modifier = Modifier.testTag("nav_radar")
                )

                NavigationBarItem(
                    selected = currentScreen == Screen.PROFILE,
                    onClick = { viewModel.navigateTo(Screen.PROFILE) },
                    icon = {
                        Icon(
                            imageVector = if (currentScreen == Screen.PROFILE) Icons.Filled.Person else Icons.Outlined.Person,
                            contentDescription = "Rider"
                        )
                    },
                    label = { Text("Rider") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = AmberGold,
                        indicatorColor = AmberGold,
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF94A3B8)
                    ),
                    modifier = Modifier.testTag("nav_profile")
                )
            }
        },
        containerColor = Color(0xFF0B0F17)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(targetState = currentScreen, label = "screen_crossfade") { screen ->
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
                    Screen.PRO_PAYWALL -> ProPaywallScreen(viewModel = viewModel)
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
        SafetySosDialog(onDismiss = { viewModel.setShowSosAlert(false) })
    }
}
