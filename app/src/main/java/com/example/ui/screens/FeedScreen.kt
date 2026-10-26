package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.*
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
    val feedItems by viewModel.feed.collectAsState()
    val activityMode by viewModel.activityMode.collectAsState()
    val currentLang by viewModel.currentLang.collectAsState()
    val currentScreen by viewModel.currentScreen.collectAsState()
    var showLangDropdown by remember { mutableStateOf(false) }

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
                                text = "ADVENTURER",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "RIDE SOCIAL & EXPEDITIONS",
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
                            Icon(Icons.Default.Translate, contentDescription = "Language", tint = Color(0xFFCBD5E1), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(currentLang.code.uppercase(), color = Color(0xFFCBD5E1), style = MaterialTheme.typography.labelMedium)
                        }
                        DropdownMenu(
                            expanded = showLangDropdown,
                            onDismissRequest = { showLangDropdown = false },
                            modifier = Modifier.background(Color(0xFF1E293B))
                        ) {
                            SupportedLang.values().forEach { lang ->
                                DropdownMenuItem(
                                    text = { Text("${lang.displayName} (${lang.localName})", color = Color.White) },
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
                            imageVector = Icons.Default.Warning,
                            contentDescription = "SOS",
                            tint = Color(0xFFEF4444)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0B0F17))
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.setShowCreatePost(true) },
                containerColor = AdventureOrange,
                contentColor = Color.Black,
                modifier = Modifier.testTag("create_post_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Post")
            }
        },
        containerColor = Color(0xFF0B0F17),
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Hold-to-Start Ride Banner
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2A)),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(
                            listOf(AdventureOrange.copy(alpha = 0.5f), AmberGold.copy(alpha = 0.3f))
                        )
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = AmberGold.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "READY FOR THE SADDLE?",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AmberGold,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Start Live Ride Telemetry",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Press & hold the button for 1.5s to lock GPS, lean meter, and trip recording.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF94A3B8)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        HoldToStartRideButton(
                            onRideStarted = { viewModel.startRide() }
                        )
                    }
                }
            }

            // Quick Actions Strip
            item {
                QuickActionsStrip(
                    currentScreen = currentScreen,
                    onNavigate = { viewModel.navigateTo(it) }
                )
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
