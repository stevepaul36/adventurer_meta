package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AdventureOrange
import com.example.ui.theme.AmberGold
import com.example.viewmodel.AdventurerViewModel
import com.example.viewmodel.Screen

/**
 * One Community destination that groups club discovery, joined-club conversations,
 * and personal/group progress instead of adding separate bottom-navigation tabs.
 *
 * Group chat reuses the existing club repository. One-to-one messaging is explicitly
 * described as not connected until a real member/message service is added.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityHubScreen(
    viewModel: AdventurerViewModel,
    modifier: Modifier = Modifier
) {
    val clubs by viewModel.clubs.collectAsState()
    val progress by viewModel.gamification.collectAsState()
    var selectedTab by rememberSaveable { mutableStateOf(0) }
    var expandedClubId by rememberSaveable { mutableStateOf<String?>(null) }

    Scaffold(
        modifier = modifier,
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Column {
                            Text("Community", fontWeight = FontWeight.Bold)
                            Text(
                                "Clubs, conversations and shared milestones",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
                TabRow(selectedTabIndex = selectedTab) {
                    listOf("Clubs", "Messages", "Challenges").forEachIndexed { index, label ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(label, maxLines = 1) }
                        )
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        when (selectedTab) {
            0 -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            "Find your people. Go further together.",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            "Join by activity and pace. Use club chat to coordinate around a real trip plan.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (clubs.isEmpty()) {
                    item { EmptyCommunityCard("No clubs are available in this local dataset yet.") }
                } else {
                    items(clubs, key = { it.id }) { club ->
                        ClubCard(
                            club = club,
                            onJoinToggle = { viewModel.toggleJoinClub(club.id) },
                            onSendMessage = { text -> viewModel.sendClubMessage(club.id, text) },
                            initialChatExpanded = expandedClubId == club.id
                        )
                    }
                }
            }
            1 -> {
                val joinedClubs = clubs.filter { it.isJoined }
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Your adventure conversations", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                            Text(
                                "Club group chat is available here. Direct one-to-one messaging requires an account-backed member inbox and is not connected in this build.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (joinedClubs.isEmpty()) {
                        item {
                            EmptyCommunityCard("Join a club first. Its group chat will appear here when membership is active.")
                            OutlinedButton(onClick = { selectedTab = 0 }) { Text("Browse clubs") }
                        }
                    } else {
                        items(joinedClubs, key = { it.id }) { club ->
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Column(Modifier.weight(1f)) {
                                            Text(club.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                            Text(
                                                "${club.chatMessages.size} messages · ${club.city}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = AmberGold
                                            )
                                        }
                                        OutlinedButton(onClick = {
                                            expandedClubId = club.id
                                            selectedTab = 0
                                        }) { Text("Open chat") }
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    val latest = club.chatMessages.lastOrNull()
                                    Text(
                                        latest?.let { "${it.user.name}: ${it.message}" }
                                            ?: "No messages yet — start the conversation.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                Text("Direct messages", fontWeight = FontWeight.Bold)
                                Text(
                                    "This inbox is not connected to real friend accounts yet. Club conversations are available now; private DMs need a secure persistent messaging service.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Progress that brings people together", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                        Text(
                            "Celebrate discovery, preparation and shared contribution — not speed or unsafe risk-taking.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Your current trail level", style = MaterialTheme.typography.labelMedium, color = AmberGold)
                            Text("Level ${progress.level}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                ProgressStat("Trips", progress.totalRides.toString(), Modifier.weight(1f))
                                ProgressStat("Distance", "${progress.totalDistanceKm.toInt()} km", Modifier.weight(1f))
                                ProgressStat("Streak", "${progress.currentStreakDays} days", Modifier.weight(1f))
                            }
                            LinearProgressIndicator(
                                progress = { (progress.currentStreakDays.coerceAtMost(7) / 7f).coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxWidth(),
                                color = AdventureOrange
                            )
                            Text(
                                "Streak display is a personal summary, not a requirement to travel daily. Rest days are normal.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Button(
                                onClick = { viewModel.navigateTo(Screen.GAMIFICATION) },
                                colors = ButtonDefaults.buttonColors(containerColor = AdventureOrange)
                            ) { Text("Open achievements & milestones") }
                        }
                    }
                }
                item {
                    CommunityChallengeCard(
                        title = "Discover five new places",
                        description = "Log distinct destinations and save a field note. Keep exact coordinates private unless you choose otherwise."
                    )
                }
                item {
                    CommunityChallengeCard(
                        title = "Get the club trip-ready",
                        description = "Help complete gear, water, transport and return-plan checks before the next group adventure."
                    )
                }
                item {
                    CommunityChallengeCard(
                        title = "Leave the place better",
                        description = "Record a cleanup or stewardship contribution. Participation matters more than competitive distance."
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyCommunityCard(message: String) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(message, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun ProgressStat(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun CommunityChallengeCard(title: String, description: String) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(title, fontWeight = FontWeight.Bold)
                Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.width(8.dp))
            Text("→", color = AdventureOrange, fontWeight = FontWeight.Bold)
        }
    }
}
