package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.analytics.AdventureFunnelAnalytics
import com.example.data.model.*
import com.example.data.repository.formatRideDuration
import com.example.ui.Str
import com.example.ui.components.*
import com.example.ui.theme.AdventureOrange
import com.example.ui.theme.AmberGold
import com.example.ui.theme.DangerRed
import com.example.ui.theme.HighAltitudeCyan
import com.example.viewmodel.AdventurerViewModel
import com.example.viewmodel.Screen
import com.example.viewmodel.SupportedLang

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    viewModel: AdventurerViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val feedItems by viewModel.feed.collectAsState()
    val activityMode by viewModel.activityMode.collectAsState()
    val currentLang by viewModel.currentLang.collectAsState()
    val currentScreen by viewModel.currentScreen.collectAsState()
    val telemetry by viewModel.telemetry.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    var showLangDropdown by remember { mutableStateOf(false) }
    var selectedTerrain by remember { mutableStateOf("All Terrains") }
    val listState = rememberLazyListState()

    val rotationAngle by animateFloatAsState(
        targetValue = if (isDarkMode) 180f else 0f,
        animationSpec = spring(),
        label = "theme_rotation"
    )

    val filteredFeedItems = remember(feedItems, selectedTerrain) {
        if (selectedTerrain == "All Terrains") feedItems
        else {
            val keyword = when (selectedTerrain) {
                "🏔️ High Pass" -> "pass"
                "🌲 Forest & Gravel" -> "trail"
                "🌊 Coastal Highway" -> "coast"
                "🏜️ Desert Dunes" -> "desert"
                "🪨 Singletrack" -> "ridge"
                else -> ""
            }
            val matched = feedItems.filter { item ->
                when (item) {
                    is FeedItem.PostItem -> item.post.caption.contains(keyword, true) || item.post.user.name.contains(keyword, true)
                    is FeedItem.StoryItem -> item.story.title.contains(keyword, true) || item.story.description.contains(keyword, true)
                    is FeedItem.RideShareItem -> item.rideShare.title.contains(keyword, true) || item.rideShare.user.name.contains(keyword, true)
                }
            }
            if (matched.isEmpty()) feedItems else matched
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF0F172A),
                            modifier = Modifier.size(36.dp)
                        ) {
                            androidx.compose.foundation.Image(
                                painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.adventurer_app_icon_1793110761210),
                                contentDescription = "Adventurer Logo",
                                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(10.dp)),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Adventurer",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = Str.TAGLINE.get(currentLang),
                                style = MaterialTheme.typography.labelSmall,
                                color = AdventureOrange
                            )
                        }
                    }
                },
                actions = {
                    // Bright Daylight / Tactical Dark Mode Toggle
                    IconButton(
                        onClick = { viewModel.toggleDarkMode() },
                        modifier = Modifier
                            .rotate(rotationAngle)
                            .testTag("theme_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = if (isDarkMode) "Switch to Bright Daylight mode" else "Switch to Night mode",
                            tint = if (isDarkMode) AmberGold else Color(0xFFD97706)
                        )
                    }

                    // Language switcher button
                    Box {
                        TextButton(
                            onClick = { showLangDropdown = true },
                            modifier = Modifier.testTag("language_switch_button")
                        ) {
                            Icon(Icons.Default.Translate, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                currentLang.localName,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.semantics { contentDescription = "Language: ${currentLang.displayName}. Change language" }
                            )
                        }
                        DropdownMenu(
                            expanded = showLangDropdown,
                            onDismissRequest = { showLangDropdown = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                        ) {
                            SupportedLang.values().forEach { lang ->
                                DropdownMenuItem(
                                    text = { Text("${lang.localName} · ${lang.displayName}", color = MaterialTheme.colorScheme.onSurface) },
                                    trailingIcon = { if (lang == currentLang) Icon(Icons.Default.Check, contentDescription = "Selected", tint = AdventureOrange) },
                                    onClick = {
                                        viewModel.setLanguage(lang)
                                        showLangDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    // Create Post quick top action
                    IconButton(
                        onClick = { viewModel.setShowCreatePost(true) },
                        modifier = Modifier.testTag("create_post_top_action")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PostAdd,
                            contentDescription = Str.POST.get(currentLang),
                            tint = AdventureOrange
                        )
                    }

                    // Emergency SOS quick icon
                    IconButton(
                        onClick = { viewModel.setShowSosAlert(true) },
                        modifier = Modifier.testTag("sos_top_action")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sos,
                            contentDescription = "Emergency SOS",
                            tint = DangerRed
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.setShowCreatePost(true) },
                containerColor = AdventureOrange,
                contentColor = Color.White,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                modifier = Modifier
                    .size(56.dp)
                    .testTag("create_post_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = Str.POST.get(currentLang),
                    modifier = Modifier.size(26.dp)
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Expedition Live Stories Strip
            item {
                ExpeditionStoriesStrip(
                    onAddStoryClick = { viewModel.setShowCreatePost(true) },
                    onStoryClick = { story ->
                        viewModel.navigateTo(Screen.STORY_STUDIO)
                    },
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Alpine Weather & Telemetry Bar
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    AlpineWeatherTelemetryBar()
                }
            }

            // Interactive Activity Mode Switcher Strip
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "EXPLORE BY ACTIVITY",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = AdventureOrange,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ActivityType.values().forEach { mode ->
                            val isSelected = activityMode == mode
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setActivityMode(mode) },
                                label = {
                                    Text(
                                        text = mode.displayName,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                leadingIcon = {
                                    val icon = when (mode) {
                                        ActivityType.Motorcycling -> Icons.Default.TwoWheeler
                                        ActivityType.Cycling -> Icons.Default.DirectionsBike
                                        ActivityType.Walking, ActivityType.Trekking -> Icons.Default.Hiking
                                        ActivityType.CarTrips -> Icons.Default.DirectionsCar
                                        ActivityType.Camping -> Icons.Default.Cabin
                                        ActivityType.Fishing -> Icons.Default.Water
                                        ActivityType.ScubaDiving, ActivityType.Snorkeling -> Icons.Default.ScubaDiving
                                        ActivityType.KayakingPaddling, ActivityType.BoatingSailing -> Icons.Default.Kayaking
                                        else -> Icons.Default.Explore
                                    }
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AdventureOrange,
                                    selectedLabelColor = Color.White,
                                    selectedLeadingIconColor = Color.White,
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    labelColor = MaterialTheme.colorScheme.onSurface,
                                    iconColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                shape = RoundedCornerShape(12.dp),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = if (isSelected) AdventureOrange else MaterialTheme.colorScheme.outline
                                ),
                                modifier = Modifier.testTag("mode_chip_${mode.name.lowercase()}")
                            )
                        }
                    }
                }
            }

            // Season Altitude Profile & Interactive Sparkline Card
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    ExpeditionElevationSparklineCard()
                }
            }

            // Hero: start an adventure, or jump back into active recording
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(
                            listOf(AdventureOrange.copy(alpha = 0.8f), AmberGold.copy(alpha = 0.5f))
                        )
                    )
                ) {
                    if (telemetry.isRecording) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    Str.RIDE_ACTIVE.get(currentLang),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "%.1f km · %s%s".format(telemetry.distanceKm, formatRideDuration(telemetry.elapsedSeconds), if (telemetry.isPaused) " · paused" else ""),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = AdventureOrange,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Button(
                                onClick = { viewModel.navigateTo(Screen.LIVE_TRACKER) },
                                colors = ButtonDefaults.buttonColors(containerColor = AdventureOrange),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.height(52.dp)
                            ) {
                                Icon(Icons.Default.Speed, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    if (telemetry.activityType == ActivityType.Motorcycling) Str.OPEN_HUD.get(currentLang) else "Open tracker",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (activityMode == ActivityType.Motorcycling) Str.HERO_TITLE.get(currentLang) else activityMode.homeTitle,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (activityMode == ActivityType.Motorcycling) Str.HERO_BODY.get(currentLang) else activityMode.homeHint,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            HoldToStartRideButton(onRideStarted = {
                                AdventureFunnelAnalytics.log(context, "activity_started", mapOf("activity_type" to activityMode.name))
                                viewModel.startRide()
                                viewModel.navigateTo(Screen.JOURNEY)
                            })
                        }
                    }
                }
            }

            // Shortcuts to the non-tab screens
            item {
                Text(
                    Str.EXPLORE.get(currentLang),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            item {
                QuickActionsStrip(
                    currentScreen = currentScreen,
                    onNavigate = { viewModel.navigateTo(it) },
                    lang = currentLang
                )
            }

            // Terrain & Trail Filter Chips
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = "TERRAIN & TRAIL SURFACE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = HighAltitudeCyan,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    FeedTerrainFilterRow(
                        selectedTerrain = selectedTerrain,
                        onTerrainSelected = { selectedTerrain = it }
                    )
                }
            }

            if (filteredFeedItems.isEmpty()) {
                item {
                    Text(
                        Str.FEED_EMPTY.get(currentLang),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth().padding(32.dp)
                    )
                }
            }

            // Feed items
            itemsIndexed(filteredFeedItems) { index, item ->
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    when (item) {
                        is FeedItem.PostItem -> {
                            PostCard(
                                post = item.post,
                                onLikeClick = { viewModel.toggleLike(index) },
                                onCommentClick = { viewModel.openCommentSheet(index) }
                            )
                        }
                        is FeedItem.RideShareItem -> {
                            RideShareCard(
                                rideShare = item.rideShare,
                                onLikeClick = { viewModel.toggleLike(index) },
                                onCommentClick = { viewModel.openCommentSheet(index) }
                            )
                        }
                        is FeedItem.StoryItem -> {
                            RideStoryCard(
                                story = item.story,
                                onClick = { viewModel.navigateTo(Screen.STORY_STUDIO) }
                            )
                        }
                    }
                }
            }
        }
    }
}
