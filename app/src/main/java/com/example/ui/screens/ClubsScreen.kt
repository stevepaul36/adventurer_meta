package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.model.Club
import com.example.ui.theme.AdventureOrange
import com.example.ui.theme.AmberGold
import com.example.ui.theme.HighAltitudeCyan
import com.example.viewmodel.AdventurerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClubsScreen(
    viewModel: AdventurerViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler { viewModel.navigateBack() }

    val clubs by viewModel.clubs.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Adventure Riding Clubs",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "Brotherhoods, Collective Mileage & Trail Chats",
                            style = MaterialTheme.typography.labelSmall,
                            color = HighAltitudeCyan
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(clubs) { club ->
                ClubCard(
                    club = club,
                    onJoinToggle = { viewModel.toggleJoinClub(club.id) },
                    onSendMessage = { text -> viewModel.sendClubMessage(club.id, text) }
                )
            }
        }
    }
}

@Composable
fun ClubCard(
    club: Club,
    onJoinToggle: () -> Unit,
    onSendMessage: (String) -> Unit,
    initialChatExpanded: Boolean = false
) {
    var expandedChat by remember(club.id, initialChatExpanded) { mutableStateOf(initialChatExpanded) }
    var chatInput by remember { mutableStateOf("") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("club_card_${club.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = club.badgeUrl,
                    contentDescription = club.name,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        club.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "📍 ${club.city} • ${club.memberCount} Riders",
                        style = MaterialTheme.typography.labelSmall,
                        color = AmberGold
                    )
                }

                Button(
                    onClick = onJoinToggle,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (club.isJoined) MaterialTheme.colorScheme.surfaceVariant else AdventureOrange
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (club.isJoined) "Joined" else "Join Club",
                        color = if (club.isJoined) MaterialTheme.colorScheme.onSurfaceVariant else Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                club.tagLine,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Club Goals
            if (club.goals.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                club.goals.forEach { goal ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    goal.name,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    "${goal.current} / ${goal.target} ${goal.unit}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AmberGold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { goal.progressPercent },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = AdventureOrange,
                                trackColor = MaterialTheme.colorScheme.surface
                            )
                        }
                    }
                }
            }

            // Club Chat Section
            if (club.isJoined) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Club Radio / Trail Chat (${club.chatMessages.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    TextButton(onClick = { expandedChat = !expandedChat }) {
                        Text(if (expandedChat) "Hide" else "Open Chat", color = AdventureOrange)
                    }
                }

                if (expandedChat) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        club.chatMessages.forEach { msg ->
                            Row(verticalAlignment = Alignment.Top) {
                                Text("${msg.user.name}: ", fontWeight = FontWeight.Bold, color = AmberGold, style = MaterialTheme.typography.labelSmall)
                                Text(msg.message, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                                Text(msg.timestamp, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = chatInput,
                                onValueChange = { chatInput = it },
                                placeholder = { Text("Broadcast trail update...") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    focusedBorderColor = AdventureOrange,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = {
                                    if (chatInput.isNotBlank()) {
                                        onSendMessage(chatInput)
                                        chatInput = ""
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Send, contentDescription = "Send", tint = AdventureOrange)
                            }
                        }
                    }
                }
            }
        }
    }
}
