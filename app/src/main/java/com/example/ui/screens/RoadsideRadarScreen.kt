package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.EmergencyStation
import com.example.data.model.StationType
import com.example.ui.components.SafetySosDialog
import com.example.ui.theme.AdventureOrange
import com.example.ui.theme.AmberGold
import com.example.ui.theme.DangerRed
import com.example.ui.theme.HighAltitudeCyan
import com.example.viewmodel.AdventurerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoadsideRadarScreen(
    viewModel: AdventurerViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler { viewModel.navigateBack() }

    val stations by viewModel.emergencyStations.collectAsState()
    val safetyTips = viewModel.safetyTips
    val showSosAlert by viewModel.showSosAlert.collectAsState()
    val context = LocalContext.current

    var selectedType by remember { mutableStateOf<StationType?>(null) }

    val filteredStations = remember(stations, selectedType) {
        if (selectedType == null) stations else stations.filter { it.type == selectedType }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Roadside Assistance Radar", fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Mechanics, Puncture, Fuel & High-Pass Medical", style = MaterialTheme.typography.labelSmall, color = DangerRed)
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
            // SOS Emergency Action Hero Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sos_emergency_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DangerRed.copy(alpha = 0.15f)),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(DangerRed)
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = DangerRed, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("EMERGENCY RADAR ACTIVE", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = DangerRed)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Stranded or facing high-altitude emergency? Tap to transmit GPS fix to nearby riders & ITBP rescue.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFCBD5E1)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Button(
                            onClick = { viewModel.setShowSosAlert(true) },
                            colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("TRIGGER SOS", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Filter Tabs
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedType == null,
                        onClick = { selectedType = null },
                        label = { Text("All Stations (${stations.size})") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AdventureOrange,
                            selectedLabelColor = Color.Black,
                            containerColor = Color(0xFF1E293B),
                            labelColor = Color.White
                        )
                    )
                    StationType.values().forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(type.label) },
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

            // Station list
            item {
                Text(
                    text = "Nearby Verified Stations",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            items(filteredStations) { station ->
                StationCard(
                    station = station,
                    onCallClick = {
                        try {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${station.phone.replace(" ", "")}"))
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            // Fallback simulation
                        }
                    }
                )
            }

            // Mountain Survival Checklist
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Trans-Himalayan Safety Protocols",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AmberGold
                )
            }

            items(safetyTips) { tip ->
                SafetyTipCard(tip = tip)
            }
        }
    }

    if (showSosAlert) {
        SafetySosDialog(onDismiss = { viewModel.setShowSosAlert(false) })
    }
}

@Composable
fun StationCard(
    station: EmergencyStation,
    onCallClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("station_card_${station.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2A)),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF334155))
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = when (station.type) {
                        StationType.MECHANIC -> AdventureOrange.copy(alpha = 0.2f)
                        StationType.HOSPITAL -> DangerRed.copy(alpha = 0.2f)
                        StationType.PUNCTURE -> AmberGold.copy(alpha = 0.2f)
                        StationType.FUEL_STASH -> HighAltitudeCyan.copy(alpha = 0.2f)
                        StationType.DHABA -> Color(0xFF10B981).copy(alpha = 0.2f)
                    }
                ) {
                    Text(
                        text = station.type.label.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = when (station.type) {
                            StationType.MECHANIC -> AdventureOrange
                            StationType.HOSPITAL -> DangerRed
                            StationType.PUNCTURE -> AmberGold
                            StationType.FUEL_STASH -> HighAltitudeCyan
                            StationType.DHABA -> Color(0xFF10B981)
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }

                if (station.isOpen24x7) {
                    Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFF10B981).copy(alpha = 0.2f)) {
                        Text(
                            text = "24/7 OPEN",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF10B981),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = station.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "📍 ${station.location} (${station.distanceKm} km away)",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF94A3B8)
            )

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = station.specialNotes,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFCBD5E1)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "★ ${station.rating} User Verified",
                    style = MaterialTheme.typography.labelMedium,
                    color = AmberGold
                )

                Button(
                    onClick = onCallClick,
                    colors = ButtonDefaults.buttonColors(containerColor = AdventureOrange),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Phone, contentDescription = "Call", tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(station.phone, color = Color.Black, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
fun SafetyTipCard(tip: com.example.data.model.MountainSafetyTip) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2A)),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF334155))
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = tip.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Expand",
                        tint = AmberGold
                    )
                }
            }

            Text(
                text = tip.shortAdvice,
                style = MaterialTheme.typography.bodySmall,
                color = AmberGold
            )

            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Divider(color = Color(0xFF1E293B))
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = tip.fullInstructions,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFCBD5E1)
                )
            }
        }
    }
}
