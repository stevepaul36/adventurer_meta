package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.model.ExpeditionRoute
import com.example.ui.components.TripBudgetModal
import com.example.ui.theme.AdventureOrange
import com.example.ui.theme.AmberGold
import com.example.ui.theme.DangerRed
import com.example.ui.theme.HighAltitudeCyan
import com.example.viewmodel.AdventurerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpeditionsScreen(
    viewModel: AdventurerViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler { viewModel.navigateBack() }

    val expeditions by viewModel.expeditions.collectAsState()
    val showBudgetModal by viewModel.showBudgetModal.collectAsState()
    val selectedExpedition by viewModel.selectedExpedition.collectAsState()

    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "North Himalayas", "Inner Himalayas", "Sahyadri", "Desert", "Extreme", "Women-Focused")

    val filteredList = remember(expeditions, selectedFilter) {
        when (selectedFilter) {
            "All" -> expeditions
            "North Himalayas" -> expeditions.filter { it.region.contains("North", true) }
            "Inner Himalayas" -> expeditions.filter { it.region.contains("Inner", true) }
            "Sahyadri" -> expeditions.filter { it.region.contains("Sahyadri", true) }
            "Desert" -> expeditions.filter { it.region.contains("Desert", true) }
            "Extreme" -> expeditions.filter { it.difficulty.equals("Extreme", true) }
            "Women-Focused" -> expeditions.filter { it.isWomenFocused }
            else -> expeditions
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("India Expeditions", fontWeight = FontWeight.Bold, color = Color.White)
                        Text("High passes, remote trails & contingencies", style = MaterialTheme.typography.labelSmall, color = AmberGold)
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
            // Filter chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    filters.forEach { f ->
                        val isSelected = selectedFilter == f
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedFilter = f },
                            label = { Text(f) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AdventureOrange,
                                selectedLabelColor = Color.Black,
                                containerColor = Color(0xFF1E293B),
                                labelColor = Color.White
                            )
                        )
                    }
                }
            }

            items(filteredList) { route ->
                ExpeditionCard(
                    route = route,
                    onBookmarkClick = { viewModel.toggleBookmarkExpedition(route.id) },
                    onCalculateBudget = {
                        viewModel.selectExpedition(route)
                        viewModel.setShowBudgetModal(true)
                    }
                )
            }
        }
    }

    if (showBudgetModal && selectedExpedition != null) {
        TripBudgetModal(
            distanceKm = selectedExpedition!!.distanceKm,
            onDismiss = { viewModel.setShowBudgetModal(false) }
        )
    }
}

@Composable
fun ExpeditionCard(
    route: ExpeditionRoute,
    onBookmarkClick: () -> Unit,
    onCalculateBudget: () -> Unit
) {
    var expandedPlans by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("expedition_card_${route.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2A)),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF334155))
        )
    ) {
        Column {
            // Hero Image with Badges
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                AsyncImage(
                    model = route.imageUrl,
                    contentDescription = route.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0x66000000), Color(0xDD0B0F17))
                            )
                        )
                )

                // Top right bookmark
                IconButton(
                    onClick = onBookmarkClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = if (route.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Bookmark",
                        tint = if (route.isBookmarked) AmberGold else Color.White
                    )
                }

                // Bottom tags
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (route.difficulty == "Extreme") DangerRed else AdventureOrange
                    ) {
                        Text(
                            text = route.difficulty.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }

                    if (route.oxygenAlert) {
                        Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFF0284C7)) {
                            Text(
                                text = "O2 ALERT (>14K FT)",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (route.monsoonAlert) {
                        Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFF0D9488)) {
                            Text(
                                text = "MONSOON RIDGE",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = route.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${route.region} • ${route.state}",
                    style = MaterialTheme.typography.labelMedium,
                    color = AmberGold
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Stats row
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF1E293B),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Distance", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                            Text("${route.distanceKm} km", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Column {
                            Text("Duration", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                            Text(route.durationDays, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Column {
                            Text("Peak Pass", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                            Text("${route.altitudeFt} ft", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = HighAltitudeCyan)
                        }
                        Column {
                            Text("Safety Score", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                            Text("★ ${route.safetyScore}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = AmberGold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Terrain: ${route.terrain}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFCBD5E1)
                )

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Recommended Bike: ${route.recommendedBike}",
                    style = MaterialTheme.typography.bodySmall,
                    color = AmberGold
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Plan A & Plan B Accordion
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF162033),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Route Contingency Plans",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            TextButton(onClick = { expandedPlans = !expandedPlans }) {
                                Text(if (expandedPlans) "Hide" else "Show Plan A & B", color = AdventureOrange)
                            }
                        }

                        if (expandedPlans) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("🟢 Plan A (Primary Route):", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                            Text(route.planA, style = MaterialTheme.typography.bodySmall, color = Color.White)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("🟡 Plan B (Emergency Contingency):", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = AmberGold)
                            Text(route.planB, style = MaterialTheme.typography.bodySmall, color = Color.White)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Expedition Crew Roster:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
                            Text("• Leader: ${route.roles.leader}\n• Safety/Medic: ${route.roles.safetyOfficer}\n• Mechanic: ${route.roles.mechanic}\n• Media: ${route.roles.photographer}", style = MaterialTheme.typography.bodySmall, color = Color(0xFFCBD5E1))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onCalculateBudget,
                    colors = ButtonDefaults.buttonColors(containerColor = AmberGold),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Calculate, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Calculate Expedition Budget", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
