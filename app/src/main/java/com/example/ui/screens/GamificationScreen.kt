package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.Badge
import com.example.data.model.BadgeTier
import com.example.data.model.Trophy
import com.example.ui.theme.AdventureOrange
import com.example.ui.theme.AmberGold
import com.example.ui.theme.HighAltitudeCyan
import com.example.viewmodel.AdventurerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamificationScreen(
    viewModel: AdventurerViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler { viewModel.navigateBack() }

    val gamification by viewModel.gamification.collectAsState()
    var selectedTrophyIndex by remember { mutableStateOf(0) }
    var trophyRotationAngle by remember { mutableStateOf(0f) }

    val animatedRotation by animateFloatAsState(
        targetValue = trophyRotationAngle,
        animationSpec = tween(durationMillis = 80),
        label = "trophy_rot"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Rider Gamification Hub", fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Level, Streak, 3D Trophy Pedestal & Badges", style = MaterialTheme.typography.labelSmall, color = AmberGold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0B0F17))
            )
        },
        containerColor = Color(0xFF0B0F17),
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Level & XP Hero Strip
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2A)),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(
                            listOf(AdventureOrange.copy(alpha = 0.5f), AmberGold.copy(alpha = 0.4f))
                        )
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = AdventureOrange,
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "${gamification.level}",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("ADVENTURE CAPTAIN", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text("${gamification.currentXp} / ${gamification.nextLevelXp} XP to Level ${gamification.level + 1}", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                                }
                            }

                            // Streak
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF1E293B)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = AdventureOrange, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("${gamification.currentStreakDays}d STREAK", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // XP Progress Bar
                        val progress = (gamification.currentXp.toFloat() / gamification.nextLevelXp).coerceIn(0f, 1f)
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = AmberGold,
                            trackColor = Color(0xFF1E293B)
                        )
                    }
                }
            }

            // Interactive 3D Trophy Showcase
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2A)),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF334155))
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "3D Adventure Trophy Showcase",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Drag horizontally across pedestal to inspect in 360°",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF94A3B8)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // 3D Trophy Canvas
                        val currentTrophy = gamification.trophies.getOrNull(selectedTrophyIndex) ?: gamification.trophies.firstOrNull()

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .pointerInput(Unit) {
                                    detectDragGestures { _, dragAmount ->
                                        trophyRotationAngle += dragAmount.x * 0.8f
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val cx = size.width / 2
                                val cy = size.height * 0.75f

                                // Pedestal Base (metallic oval)
                                drawOval(
                                    brush = Brush.radialGradient(
                                        listOf(Color(0xFF475569), Color(0xFF1E293B)),
                                        center = Offset(cx, cy),
                                        radius = 80.dp.toPx()
                                    ),
                                    topLeft = Offset(cx - 70.dp.toPx(), cy - 15.dp.toPx()),
                                    size = Size(140.dp.toPx(), 30.dp.toPx())
                                )

                                // Trophy Stem & Cup rotating
                                rotate(degrees = animatedRotation, pivot = Offset(cx, cy)) {
                                    // Piston / Trophy Stem
                                    drawRoundRect(
                                        brush = Brush.verticalGradient(
                                            listOf(AmberGold, Color(0xFFB45309))
                                        ),
                                        topLeft = Offset(cx - 10.dp.toPx(), cy - 50.dp.toPx()),
                                        size = Size(20.dp.toPx(), 45.dp.toPx()),
                                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
                                    )

                                    // Crown Cup
                                    drawOval(
                                        brush = Brush.horizontalGradient(
                                            listOf(Color(0xFFFFE54C), AmberGold, Color(0xFFD97706))
                                        ),
                                        topLeft = Offset(cx - 36.dp.toPx(), cy - 80.dp.toPx()),
                                        size = Size(72.dp.toPx(), 40.dp.toPx())
                                    )

                                    // Laurel / Stars emblem
                                    drawCircle(
                                        color = Color.White,
                                        radius = 6.dp.toPx(),
                                        center = Offset(cx, cy - 60.dp.toPx())
                                    )
                                }
                            }
                        }

                        currentTrophy?.let { tr ->
                            Text(
                                text = tr.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = AmberGold
                            )
                            Text(
                                text = "${tr.subtitle} • Unlocked ${tr.unlockedDate ?: "Recently"}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFCBD5E1)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = tr.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF94A3B8),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Trophy switcher tabs
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            gamification.trophies.forEachIndexed { idx, tr ->
                                FilterChip(
                                    selected = selectedTrophyIndex == idx,
                                    onClick = { selectedTrophyIndex = idx },
                                    label = { Text(tr.tier.name) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = AmberGold,
                                        selectedLabelColor = Color.Black,
                                        containerColor = Color(0xFF1E293B),
                                        labelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Badges Grid
            item {
                Text(
                    text = "Adventure Badges (${gamification.unlockedBadgesCount} Unlocked)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            items(gamification.badges.chunked(2)) { pair ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    pair.forEach { badge ->
                        BadgeCard(badge = badge, modifier = Modifier.weight(1f))
                    }
                    if (pair.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun BadgeCard(badge: Badge, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.testTag("badge_card_${badge.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2A)),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (badge.isUnlocked) AmberGold.copy(alpha = 0.6f) else Color(0xFF1E293B)
            )
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = if (badge.isUnlocked) AmberGold.copy(alpha = 0.2f) else Color(0xFF1E293B),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (badge.isUnlocked) Icons.Default.MilitaryTech else Icons.Default.Lock,
                        contentDescription = null,
                        tint = if (badge.isUnlocked) AmberGold else Color(0xFF64748B),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = badge.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (badge.isUnlocked) Color.White else Color(0xFF94A3B8),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "${badge.tier.name} • ${badge.activityType.displayName}",
                style = MaterialTheme.typography.labelSmall,
                color = if (badge.isUnlocked) AmberGold else Color(0xFF64748B)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = badge.description,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFCBD5E1),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
