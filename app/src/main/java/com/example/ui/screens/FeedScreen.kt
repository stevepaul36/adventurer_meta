package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.tween
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.analytics.AdventureFunnelAnalytics
import com.example.data.model.*
import com.example.data.repository.formatRideDuration
import com.example.ui.Str
import com.example.ui.components.*
import com.example.ui.theme.AdventureOrange
import com.example.ui.theme.AmberGold
import com.example.ui.theme.DangerRed
import com.example.ui.theme.FieldMuted
import com.example.ui.theme.FieldNavy
import com.example.ui.theme.HighAltitudeCyan
import com.example.ui.theme.SafeMint
import com.example.ui.theme.SlatePanel
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TrailOrange
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
    var activeViewerShort by remember { mutableStateOf<AdventureShort?>(null) }
    var activePhotoViewer by remember { mutableStateOf<Pair<String, String>?>(null) }
    var activeViewerFilter by remember { mutableStateOf("All Media") }
    var selectedFeedTab by remember { mutableStateOf("For you") }
    val followedAdventurerIds by viewModel.followedAdventurerIds.collectAsState()
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
            matched
        }
    }

    val audienceFeedItems = remember(filteredFeedItems, selectedFeedTab, followedAdventurerIds) {
        if (selectedFeedTab == "Following") {
            FeedAudiencePolicy.followingItems(filteredFeedItems, followedAdventurerIds)
        } else {
            filteredFeedItems
        }
    }
    val displayFeedItems = remember(audienceFeedItems, activeViewerFilter) {
        when (activeViewerFilter) {
            "🔥 Trail Shorts" -> emptyList()
            "📸 Trail Photos" -> audienceFeedItems.filter { item ->
                when (item) {
                    is FeedItem.PostItem -> !item.post.imageUrl.isNullOrBlank()
                    is FeedItem.StoryItem -> item.story.coverImageUrl.isNotBlank()
                    is FeedItem.RideShareItem -> false
                }
            }
            "🏔️ High Pass" -> audienceFeedItems.filter { item ->
                when (item) {
                    is FeedItem.PostItem -> (item.post.caption + " " + item.post.user.location).contains("pass", true)
                    is FeedItem.StoryItem -> (item.story.title + " " + item.story.description).contains("pass", true)
                    is FeedItem.RideShareItem -> (item.rideShare.title + " " + item.rideShare.user.location).contains("pass", true)
                }
            }
            else -> audienceFeedItems
        }
    }
    val followedAdventurerNames = remember(viewModel.friendUsers, followedAdventurerIds) {
        viewModel.friendUsers.filter { it.id in followedAdventurerIds }.map { it.name }.toSet()
    }
    val audienceShorts = remember(selectedFeedTab, followedAdventurerNames) {
        if (selectedFeedTab == "Following") {
            defaultAdventureShorts.filter { it.riderName in followedAdventurerNames }
        } else {
            defaultAdventureShorts
        }
    }
    val availableShorts = remember(audienceShorts, activeViewerFilter) {
        when (activeViewerFilter) {
            "📸 Trail Photos" -> emptyList()
            "🏔️ High Pass" -> audienceShorts.filter {
                it.location.contains("pass", true) || it.tags.any { tag -> tag.contains("highpass", true) }
            }
            else -> audienceShorts
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

                    // All Modules & Features Navigator
                    IconButton(
                        onClick = { viewModel.setShowFeatureOrganizer(true) },
                        modifier = Modifier.testTag("open_feature_organizer_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Widgets,
                            contentDescription = "Adventure Modules Directory",
                            tint = SafeMint
                        )
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
            // 1. Follower-first stories rail: familiar horizontal story bubbles at the top of Home.
            item {
                AdventurerStoriesRail(
                    currentUser = viewModel.currentUser,
                    adventurers = viewModel.friendUsers,
                    followedIds = followedAdventurerIds,
                    onAddStoryClick = { viewModel.setShowCreatePost(true) },
                    onStoryClick = { adventurer ->
                        val short = defaultAdventureShorts.firstOrNull { it.riderName == adventurer.name }
                        if (short != null) {
                            activeViewerShort = short
                        } else {
                            activePhotoViewer = Pair(
                                adventurerStoryCovers[adventurer.id] ?: adventurer.avatarUrl,
                                "A recent moment from ${adventurer.name} · ${adventurer.location}"
                            )
                        }
                    }
                )
            }

            // 2. For-you vs Following: viewers control whose updates fill the feed.
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "YOUR ADVENTURE FEED",
                            color = AdventureOrange,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            if (selectedFeedTab == "Following") "Moments from Adventurers you follow" else "Discover journeys worth following",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("For you", "Following").forEach { tab ->
                            FilterChip(
                                selected = selectedFeedTab == tab,
                                onClick = { selectedFeedTab = tab },
                                label = { Text(tab, fontWeight = if (selectedFeedTab == tab) FontWeight.Bold else FontWeight.Medium) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AdventureOrange,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.testTag("feed_audience_${tab.lowercase().replace(" ", "_")}")
                            )
                        }
                    }
                }
            }

            // 3. Follow discovery connects creator identity with one-tap following.
            item {
                SuggestedAdventurersRail(
                    adventurers = viewModel.friendUsers,
                    followedIds = followedAdventurerIds,
                    onFollowToggle = { viewModel.toggleFollowAdventurer(it) }
                )
            }

            // 4. Immersive shorts respond to the selected audience and media view.
            if (availableShorts.isNotEmpty()) {
            item {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(animationSpec = tween(420, delayMillis = 60)) +
                        slideInVertically(
                            animationSpec = tween(420, delayMillis = 60),
                            initialOffsetY = { it / 14 }
                        ),
                    label = "feed_hero_entrance"
                ) {
                    ExpeditionShortsReel(
                        shorts = availableShorts,
                        onShortClick = { shortItem ->
                            activeViewerShort = shortItem
                        },
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }


            }
            // 5. Media filters now change the actual feed content
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf("All Media", "🔥 Trail Shorts", "📸 Trail Photos", "🏔️ High Pass").forEach { filterName ->
                        val isSelected = activeViewerFilter == filterName
                        FilterChip(
                            selected = isSelected,
                            onClick = { activeViewerFilter = filterName },
                            label = {
                                Text(
                                    text = filterName,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AdventureOrange,
                                selectedLabelColor = Color.White,
                                containerColor = MaterialTheme.colorScheme.surface,
                                labelColor = MaterialTheme.colorScheme.onSurface
                            ),
                            shape = RoundedCornerShape(10.dp),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) AdventureOrange else MaterialTheme.colorScheme.outline
                            ),
                            modifier = Modifier.testTag("viewer_filter_${filterName.replace(" ", "_").lowercase()}")
                        )
                    }
                }
            }

            if (activeViewerFilter != "🔥 Trail Shorts") {
            // 6. Social posts and stories, directly below the viewer shelves
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ADVENTURER UPDATES & MOMENTS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = AdventureOrange
                    )
                    Text(
                        text = "Stories and field notes",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (displayFeedItems.isEmpty() && activeViewerFilter != "🔥 Trail Shorts") {
                item {
                    Text(
                        if (selectedFeedTab == "Following") "Nothing from your followed Adventurers matches this view yet." else Str.FEED_EMPTY.get(currentLang),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth().padding(32.dp)
                    )
                }
            }

            // 5. Active Viewer Photos & Post Cards (Elevated to top)
            itemsIndexed(displayFeedItems) { _, item ->
                val sourceIndex = feedItems.indexOfFirst { it == item }.coerceAtLeast(0)
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    when (item) {
                        is FeedItem.PostItem -> {
                            PostCard(
                                post = item.post,
                                onLikeClick = { viewModel.toggleLike(sourceIndex) },
                                onCommentClick = { viewModel.openCommentSheet(sourceIndex) },
                                onPhotoClick = { photoUrl, caption ->
                                    activePhotoViewer = Pair(photoUrl, caption)
                                }
                            )
                        }
                        is FeedItem.RideShareItem -> {
                            RideShareCard(
                                rideShare = item.rideShare,
                                onLikeClick = { viewModel.toggleLike(sourceIndex) },
                                onCommentClick = { viewModel.openCommentSheet(sourceIndex) }
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

            if (activeViewerFilter == "🔥 Trail Shorts" && availableShorts.isEmpty()) {
                item {
                    Text(
                        if (selectedFeedTab == "Following") "No shorts from the Adventurers you follow yet." else "No short videos match this view yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 18.dp)
                    )
                }
            }

            // 3. Basecamp Active Plan & Preparation Resume Card (Screen 01)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clickable { viewModel.navigateTo(Screen.ROUTE_BRIEF) }
                        .testTag("basecamp_resume_plan_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SlatePanel),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(listOf(TrailOrange.copy(alpha = 0.8f), SafeMint.copy(alpha = 0.5f)))
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = SafeMint.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        "6/8 PREP COMPLETE",
                                        color = SafeMint,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "SAVED TRIP CANDIDATE",
                                    color = FieldMuted,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Ridge & River Loop · 74 km",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            "4 stops · North overlook, Creek crossing · Expected return 6:30 PM",
                            style = MaterialTheme.typography.bodySmall,
                            color = FieldMuted
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.navigateTo(Screen.ROUTE_BRIEF) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                modifier = Modifier.weight(1f).height(36.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Text("Route Brief", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { viewModel.navigateTo(Screen.LIVE_TRACKER) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = TrailOrange),
                                modifier = Modifier.weight(1f).height(36.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Text("Field Mode", color = FieldNavy, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }
                }
            }

            // 4. Adventure Modules & Expedition Tools Hub
            item {
                AdventureModulesHub(
                    currentScreen = Screen.FEED,
                    onNavigate = { viewModel.navigateTo(it) },
                    onOpenAllModules = { viewModel.setShowFeatureOrganizer(true) },
                    lang = currentLang
                )
            }

            // 6. Section Divider: Adventure Hub & Telemetry
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Explore,
                                contentDescription = null,
                                tint = HighAltitudeCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ADVENTURE CONTROLS & TELEMETRY",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "Sensors Active",
                            style = MaterialTheme.typography.labelSmall,
                            color = SuccessGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // 7. Alpine Weather & Telemetry Bar
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    AlpineWeatherTelemetryBar()
                }
            }

            // 8. Interactive Activity Mode Switcher Strip
            item {
                Column(
                    modifier = Modifier.fillMaxWidth()
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

            // 9. Season Altitude Profile & Sparkline Card
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    ExpeditionElevationSparklineCard()
                }
            }

            // 10. Hero: start an adventure, or jump back into active recording
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

            // 11. Curated shortcuts to contextual tools; the full directory is one tap away.
            item {
                QuickActionsStrip(
                    currentScreen = currentScreen,
                    onNavigate = { viewModel.navigateTo(it) },
                    lang = currentLang,
                    onOpenAllModules = { viewModel.setShowFeatureOrganizer(true) }
                )
            }

            // 12. Terrain & Trail Filter Chips
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
        }

        // Active Shorts Viewer Modal Overlay
        activeViewerShort?.let { currentShort ->
            val shortsList = defaultAdventureShorts
            val currentIndex = remember(currentShort) {
                shortsList.indexOfFirst { it.id == currentShort.id }.coerceAtLeast(0)
            }
            ActiveShortsViewerModal(
                short = currentShort,
                onDismiss = { activeViewerShort = null },
                onNext = {
                    val nextIndex = (currentIndex + 1) % shortsList.size
                    activeViewerShort = shortsList[nextIndex]
                },
                onPrevious = {
                    val prevIndex = (currentIndex - 1 + shortsList.size) % shortsList.size
                    activeViewerShort = shortsList[prevIndex]
                }
            )
        }

        // Active Photo Viewer Dialog Overlay
        activePhotoViewer?.let { (photoUrl, caption) ->
            ActivePhotoViewerDialog(
                photoUrl = photoUrl,
                caption = caption,
                onDismiss = { activePhotoViewer = null }
            )
        }
    }
}



private val adventurerStoryCovers = mapOf(
    "u2" to "https://images.unsplash.com/photo-1558981403-c5f9899a28bc?auto=format&fit=crop&w=900&q=80",
    "u3" to "https://images.unsplash.com/photo-1506744038136-46273834b3fb?auto=format&fit=crop&w=900&q=80",
    "u4" to "https://images.unsplash.com/photo-1568772585407-9361f9bf3a87?auto=format&fit=crop&w=900&q=80",
    "u5" to "https://images.unsplash.com/photo-1544735716-392fe2489ffa?auto=format&fit=crop&w=900&q=80",
    "u6" to "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?auto=format&fit=crop&w=900&q=80",
    "u7" to "https://images.unsplash.com/photo-1500375592092-40eb2168fd21?auto=format&fit=crop&w=900&q=80",
    "u8" to "https://images.unsplash.com/photo-1478131143081-80f7f84ca84d?auto=format&fit=crop&w=900&q=80",
    "u9" to "https://images.unsplash.com/photo-1517649763962-0c623066013b?auto=format&fit=crop&w=900&q=80",
    "u10" to "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=900&q=80",
    "u11" to "https://images.unsplash.com/photo-1544551763-46a013bb70d5?auto=format&fit=crop&w=900&q=80",
    "u12" to "https://images.unsplash.com/photo-1498654200943-1088dd4438ae?auto=format&fit=crop&w=900&q=80",
    "u13" to "https://images.unsplash.com/photo-1544735716-392fe2489ffa?auto=format&fit=crop&w=900&q=80",
    "u14" to "https://images.unsplash.com/photo-1568772585407-9361f9bf3a87?auto=format&fit=crop&w=900&q=80",
    "u15" to "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?auto=format&fit=crop&w=900&q=80"
)

/** Stories are surfaced first, like a familiar social viewer, while using Adventurer terminology. */
@Composable
private fun AdventurerStoriesRail(
    currentUser: User,
    adventurers: List<User>,
    followedIds: Set<String>,
    onAddStoryClick: () -> Unit,
    onStoryClick: (User) -> Unit
) {
    val followedAdventurers = adventurers.filter { it.id in followedIds }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "ADVENTURER STORIES",
                    color = AdventureOrange,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    "Fresh moments from people you follow",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                "${followedAdventurers.size} following",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall
            )
        }
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.width(66.dp).clickable(onClick = onAddStoryClick),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(62.dp).clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    AsyncImage(
                        model = currentUser.avatarUrl,
                        contentDescription = "Your Adventurer profile",
                        modifier = Modifier.fillMaxSize().padding(3.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Surface(
                        modifier = Modifier.align(Alignment.BottomEnd).size(23.dp)
                            .border(2.dp, MaterialTheme.colorScheme.background, CircleShape),
                        shape = CircleShape,
                        color = AdventureOrange
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add a story",
                            tint = Color.White,
                            modifier = Modifier.padding(3.dp)
                        )
                    }
                }
                Spacer(Modifier.height(5.dp))
                Text(
                    "Your story",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1
                )
            }

            followedAdventurers.forEach { adventurer ->
                Column(
                    modifier = Modifier.width(70.dp).clickable { onStoryClick(adventurer) },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier.size(62.dp).clip(CircleShape)
                            .background(Brush.sweepGradient(listOf(AdventureOrange, AmberGold, SafeMint, AdventureOrange)))
                    ) {
                        AsyncImage(
                            model = adventurer.avatarUrl,
                            contentDescription = "Open story from Adventurer ${adventurer.name}",
                            modifier = Modifier.fillMaxSize().padding(3.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(Modifier.height(5.dp))
                    Text(
                        adventurer.name.substringBefore(" "),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1
                    )
                    Text(
                        "ADVENTURER",
                        color = AdventureOrange,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 8.sp,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

/** A horizontal discovery shelf with explicit Follow/Following actions for every profile. */
@Composable
private fun SuggestedAdventurersRail(
    adventurers: List<User>,
    followedIds: Set<String>,
    onFollowToggle: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "MEET ADVENTURERS",
                color = AdventureOrange,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                "Follow their next journey",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            adventurers.forEach { adventurer ->
                val isFollowing = adventurer.id in followedIds
                Card(
                    modifier = Modifier.width(224.dp).testTag("adventurer_card_${adventurer.id}"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = adventurer.avatarUrl,
                                contentDescription = null,
                                modifier = Modifier.size(44.dp).clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(Modifier.width(9.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    adventurer.name,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                Text(
                                    adventurer.primaryActivity,
                                    color = AdventureOrange,
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1
                                )
                                Text(
                                    adventurer.location,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1
                                )
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        if (isFollowing) {
                            OutlinedButton(
                                onClick = { onFollowToggle(adventurer.id) },
                                modifier = Modifier.fillMaxWidth().height(36.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(Modifier.width(5.dp))
                                Text("Following")
                            }
                        } else {
                            Button(
                                onClick = { onFollowToggle(adventurer.id) },
                                modifier = Modifier.fillMaxWidth().height(36.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AdventureOrange)
                            ) {
                                Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(Modifier.width(5.dp))
                                Text("Follow Adventurer")
                            }
                        }
                    }
                }
            }
        }
    }
}
