package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.*
import com.example.ui.components.BroadcastSosDialog
import com.example.ui.components.DistressDetailSheet
import com.example.ui.components.EmergencyActions
import com.example.ui.components.RoadsideDistressMapComponent
import com.example.ui.theme.AdventureOrange
import com.example.ui.theme.AmberGold
import com.example.ui.theme.DangerRed
import com.example.ui.theme.HighAltitudeCyan
import com.example.viewmodel.AdventurerViewModel

enum class RadarViewMode {
    MAP_RADAR,
    DIRECTORY_LIST
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoadsideRadarScreen(
    viewModel: AdventurerViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler { viewModel.navigateBack() }

    val context = LocalContext.current
    var viewMode by remember { mutableStateOf(RadarViewMode.MAP_RADAR) }

    val distressSignals by viewModel.distressSignals.collectAsState()
    val roadsideServices by viewModel.roadsideServices.collectAsState()
    val stations by viewModel.emergencyStations.collectAsState()
    val safetyTips = viewModel.safetyTips

    val radarFilter by viewModel.radarFilter.collectAsState()
    val selectedMarker by viewModel.selectedMarker.collectAsState()
    val showBroadcastModal by viewModel.showBroadcastModal.collectAsState()

    val myActiveSos = distressSignals.firstOrNull { it.isMySignal }

    // Filtered signals for the Map
    val visibleDistressSignals = remember(distressSignals, radarFilter) {
        when (radarFilter) {
            RadarFilter.ALL -> distressSignals
            RadarFilter.DISTRESS_ONLY -> distressSignals
            RadarFilter.MECHANIC_TOW -> distressSignals.filter {
                it.distressType == DistressType.MECHANICAL_BREAKDOWN || it.distressType == DistressType.CRASH_ALERT
            }
            RadarFilter.FUEL_PUNCTURE -> distressSignals.filter {
                it.distressType == DistressType.OUT_OF_FUEL || it.distressType == DistressType.PUNCTURE_BLOWOUT
            }
            RadarFilter.MEDICAL_SHELTER -> distressSignals.filter {
                it.distressType == DistressType.MEDICAL_AMS
            }
        }
    }

    val visibleRoadsideServices = remember(roadsideServices, radarFilter) {
        when (radarFilter) {
            RadarFilter.ALL -> roadsideServices
            RadarFilter.DISTRESS_ONLY -> emptyList()
            RadarFilter.MECHANIC_TOW -> roadsideServices.filter { it.type == StationType.MECHANIC }
            RadarFilter.FUEL_PUNCTURE -> roadsideServices.filter {
                it.type == StationType.FUEL_STASH || it.type == StationType.PUNCTURE
            }
            RadarFilter.MEDICAL_SHELTER -> roadsideServices.filter {
                it.type == StationType.HOSPITAL || it.type == StationType.DHABA
            }
        }
    }

    var selectedDirectoryType by remember { mutableStateOf<StationType?>(null) }
    val filteredDirectoryStations = remember(stations, selectedDirectoryType) {
        if (selectedDirectoryType == null) stations else stations.filter { it.type == selectedDirectoryType }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("SOS & Roadside Radar", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = DangerRed.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "LIVE",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = DangerRed,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Nearby rescue services & riders in distress",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.setShowBroadcastModal(true) },
                        modifier = Modifier.testTag("top_broadcast_sos_btn")
                    ) {
                        Icon(Icons.Default.Emergency, contentDescription = "Broadcast SOS", tint = DangerRed)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // View Mode Segmented Tab Selector [ Radar Map | Directory & Safety ]
            ViewModeSegmentedSelector(
                selectedMode = viewMode,
                onModeSelected = { viewMode = it },
                distressCount = distressSignals.size,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            )

            // Active User SOS Alert Banner (if user has active beacon)
            if (myActiveSos != null) {
                ActiveSosBanner(
                    signal = myActiveSos,
                    onCancel = { viewModel.cancelMySos() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            when (viewMode) {
                RadarViewMode.MAP_RADAR -> {
                    // Map View Container
                    Box(modifier = Modifier.fillMaxSize()) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Filter row for Map View
                            RadarFilterChipsRow(
                                selectedFilter = radarFilter,
                                onFilterSelected = { viewModel.setRadarFilter(it) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp)
                            )

                            // Main Map Component
                            RoadsideDistressMapComponent(
                                distressSignals = visibleDistressSignals,
                                roadsideServices = visibleRoadsideServices,
                                selectedMarker = selectedMarker,
                                onMarkerSelected = { viewModel.selectMarker(it) },
                                onBroadcastSosClick = { viewModel.setShowBroadcastModal(true) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }

                        // Docked Distress Detail Bottom Sheet
                        if (selectedMarker != null) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                            ) {
                                DistressDetailSheet(
                                    marker = selectedMarker!!,
                                    onClose = { viewModel.selectMarker(null) },
                                    onToggleRespond = { viewModel.toggleRespondToDistress(it) },
                                    onCancelMySos = { viewModel.cancelMySos() }
                                )
                            }
                        }
                    }
                }

                RadarViewMode.DIRECTORY_LIST -> {
                    // Directory List & Safety Protocols View
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Quick Emergency Call Card
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
                                            Text("In trouble?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = DangerRed)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Call 112 or 108, or broadcast your exact GPS distress signal to nearby riders.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFFCBD5E1)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Button(
                                        onClick = { viewModel.setShowBroadcastModal(true) },
                                        colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier.height(52.dp).testTag("open_sos_button")
                                    ) {
                                        Icon(Icons.Default.Sos, contentDescription = null, tint = Color.White)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("SOS", color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // Real nearby help via maps app
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth().testTag("find_nearby_card"),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline)
                                )
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text("Find help near you", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    Text("Opens your maps app with real places around your current location.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    val options = listOf(
                                        Triple(StationType.MECHANIC, "Mechanic", Icons.Default.Build),
                                        Triple(StationType.PUNCTURE, "Puncture", Icons.Default.TireRepair),
                                        Triple(StationType.FUEL_STASH, "Fuel", Icons.Default.LocalGasStation),
                                        Triple(StationType.HOSPITAL, "Hospital", Icons.Default.LocalHospital),
                                        Triple(StationType.DHABA, "Food & stay", Icons.Default.Restaurant),
                                    )
                                    options.chunked(3).forEach { row ->
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                            row.forEach { (type, label, icon) ->
                                                OutlinedButton(
                                                    onClick = { EmergencyActions.findNearby(context, EmergencyActions.searchQuery(type)) },
                                                    shape = RoundedCornerShape(12.dp),
                                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 10.dp),
                                                    modifier = Modifier.weight(1f).heightIn(min = 52.dp)
                                                ) {
                                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                        Icon(icon, contentDescription = null, tint = AmberGold, modifier = Modifier.size(20.dp))
                                                        Text(label, color = Color.White, style = MaterialTheme.typography.labelMedium, maxLines = 1)
                                                    }
                                                }
                                            }
                                            repeat(3 - row.size) { Spacer(modifier = Modifier.weight(1f)) }
                                        }
                                    }
                                }
                            }
                        }

                        // Filter Tabs for Directory
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = selectedDirectoryType == null,
                                    onClick = { selectedDirectoryType = null },
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
                                        selected = selectedDirectoryType == type,
                                        onClick = { selectedDirectoryType = type },
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

                        // Directory Stations
                        item {
                            Text(
                                text = "Emergency Assistance Stations",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        items(filteredDirectoryStations) { station ->
                            StationCard(
                                station = station,
                                onCallClick = { EmergencyActions.findNearby(context, EmergencyActions.searchQuery(station.type)) }
                            )
                        }

                        // Safety Protocols Checklist
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
            }
        }
    }

    // Modal: Broadcast SOS Beacon Dialog
    if (showBroadcastModal) {
        BroadcastSosDialog(
            onDismiss = { viewModel.setShowBroadcastModal(false) },
            onBroadcast = { type, desc, phone ->
                viewModel.broadcastMySos(type, desc, phone)
                viewMode = RadarViewMode.MAP_RADAR
            }
        )
    }
}

// -------------------------------------------------------------
// Subcomponents
// -------------------------------------------------------------

@Composable
private fun ViewModeSegmentedSelector(
    selectedMode: RadarViewMode,
    onModeSelected: (RadarViewMode) -> Unit,
    distressCount: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Map Radar Mode
            val mapSelected = selectedMode == RadarViewMode.MAP_RADAR
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (mapSelected) AdventureOrange else Color.Transparent)
                    .clickable { onModeSelected(RadarViewMode.MAP_RADAR) }
                    .padding(vertical = 8.dp)
                    .testTag("tab_map_radar"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Radar,
                        contentDescription = null,
                        tint = if (mapSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Map Radar",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (mapSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (distressCount > 0) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = CircleShape,
                            color = if (mapSelected) DangerRed else DangerRed.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "$distressCount",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (mapSelected) Color.White else DangerRed,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
            }

            // Directory List Mode
            val listSelected = selectedMode == RadarViewMode.DIRECTORY_LIST
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (listSelected) AdventureOrange else Color.Transparent)
                    .clickable { onModeSelected(RadarViewMode.DIRECTORY_LIST) }
                    .padding(vertical = 8.dp)
                    .testTag("tab_directory_list"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.ListAlt,
                        contentDescription = null,
                        tint = if (listSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Directory & Safety",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (listSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ActiveSosBanner(
    signal: RiderDistressSignal,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = DangerRed.copy(alpha = 0.2f),
        border = androidx.compose.foundation.BorderStroke(1.dp, DangerRed),
        modifier = modifier.testTag("active_sos_banner")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.Emergency, contentDescription = null, tint = DangerRed, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "YOUR SOS BEACON IS ACTIVE",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = DangerRed
                    )
                    Text(
                        text = "${signal.distressType.title} · ${signal.responderCount} Responders alerted",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFCBD5E1)
                    )
                }
            }

            OutlinedButton(
                onClick = onCancel,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, DangerRed),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text("Cancel", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun RadarFilterChipsRow(
    selectedFilter: RadarFilter,
    onFilterSelected: (RadarFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        RadarFilter.values().forEach { filter ->
            val isSelected = selectedFilter == filter
            FilterChip(
                selected = isSelected,
                onClick = { onFilterSelected(filter) },
                label = { Text(filter.label) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = when (filter) {
                        RadarFilter.DISTRESS_ONLY -> DangerRed
                        RadarFilter.MECHANIC_TOW -> AdventureOrange
                        RadarFilter.FUEL_PUNCTURE -> HighAltitudeCyan
                        RadarFilter.MEDICAL_SHELTER -> Color(0xFF10B981)
                        else -> AmberGold
                    },
                    selectedLabelColor = Color.Black,
                    containerColor = Color(0xFF1E293B),
                    labelColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp)
            )
        }
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline)
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
                    text = "★ ${station.rating} · sample",
                    style = MaterialTheme.typography.labelMedium,
                    color = AmberGold
                )

                Button(
                    onClick = onCallClick,
                    colors = ButtonDefaults.buttonColors(containerColor = AdventureOrange),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Map, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Find nearby", color = Color.Black, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline)
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
                    color = MaterialTheme.colorScheme.onSurface,
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
