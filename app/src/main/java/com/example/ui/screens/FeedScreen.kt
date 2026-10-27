package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
    var showLangDropdown by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AdventureOrange,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.TwoWheeler,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Adventurer",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = Str.TAGLINE.get(currentLang),
                                style = MaterialTheme.typography.labelSmall,
                                color = AmberGold
                            )
                        }
                    }
                },
                actions = {
                    // Language switcher button
                    Box {
                        TextButton(
                            onClick = { showLangDropdown = true },
                            modifier = Modifier.testTag("language_switch_button")
                        ) {
                            Icon(Icons.Default.Translate, contentDescription = null, tint = Color(0xFFCBD5E1), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                currentLang.localName,
                                color = Color(0xFFCBD5E1),
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.semantics { contentDescription = "Language: ${currentLang.displayName}. Change language" }
                            )
                        }
                        DropdownMenu(
                            expanded = showLangDropdown,
                            onDismissRequest = { showLangDropdown = false },
                            modifier = Modifier.background(Color(0xFF1E293B))
                        ) {
                            SupportedLang.values().forEach { lang ->
                                DropdownMenuItem(
                                    text = { Text("${lang.localName} · ${lang.displayName}", color = Color.White) },
                                    trailingIcon = { if (lang == currentLang) Icon(Icons.Default.Check, contentDescription = "Selected", tint = AdventureOrange) },
                                    onClick = {
                                        viewModel.setLanguage(lang)
                                        showLangDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    // Emergency SOS quick icon
                    IconButton(
                        onClick = { viewModel.setShowSosAlert(true) },
                        modifier = Modifier.testTag("sos_top_action")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sos,
                            contentDescription = "Emergency SOS",
                            tint = Color(0xFFEF4444)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0B0F17))
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.setShowCreatePost(true) },
                expanded = !listState.canScrollBackward,
                icon = { Icon(Icons.Default.Edit, contentDescription = null) },
                text = { Text(Str.POST.get(currentLang), fontWeight = FontWeight.Bold) },
                containerColor = AdventureOrange,
                contentColor = Color.Black,
                modifier = Modifier.testTag("create_post_fab")
            )
        },
        containerColor = Color(0xFF0B0F17),
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero: start a ride, or jump back into the one in progress
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2A)),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(
                            listOf(AdventureOrange.copy(alpha = 0.6f), AmberGold.copy(alpha = 0.3f))
                        )
                    )
                ) {
                    if (telemetry.isRecording) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(Str.RIDE_ACTIVE.get(currentLang), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "%.1f km · %s%s".format(telemetry.distanceKm, formatRideDuration(telemetry.elapsedSeconds), if (telemetry.isPaused) " · paused" else ""),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = AmberGold
                                )
                            }
                            Button(
                                onClick = { viewModel.navigateTo(Screen.LIVE_TRACKER) },
                                colors = ButtonDefaults.buttonColors(containerColor = AdventureOrange),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.height(52.dp)
                            ) {
                                Icon(Icons.Default.Speed, contentDescription = null, tint = Color.Black)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (telemetry.activityType == ActivityType.Motorcycling) Str.OPEN_HUD.get(currentLang) else "Open tracker", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (activityMode == ActivityType.Motorcycling) Str.HERO_TITLE.get(currentLang) else activityMode.homeTitle,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (activityMode == ActivityType.Motorcycling) Str.HERO_BODY.get(currentLang) else activityMode.homeHint,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            HoldToStartRideButton(onRideStarted = {
                                AdventureFunnelAnalytics.log(context, "activity_started", mapOf("activity_type" to activityMode.name))
                                viewModel.startRide()
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
                    color = Color.White,
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

            if (feedItems.isEmpty()) {
                item {
                    Text(
                        Str.FEED_EMPTY.get(currentLang),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.fillMaxWidth().padding(32.dp)
                    )
                }
            }

            // Feed items
            itemsIndexed(feedItems) { index, item ->
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
