package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.AdventureOrange
import com.example.ui.theme.AmberGold
import com.example.ui.theme.DangerRed
import com.example.ui.theme.HighAltitudeCyan
import com.example.ui.theme.SuccessGreen
import com.example.viewmodel.AdventurerViewModel
import com.example.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JourneyScreen(
    viewModel: AdventurerViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler { viewModel.navigateBack() }
    val context = LocalContext.current

    val fuelState by viewModel.fuelState.collectAsState()
    val stops by viewModel.journeyStops.collectAsState()
    val memories by viewModel.journeyMemories.collectAsState()
    val venues by viewModel.discoveryVenues.collectAsState()
    val selectedCategory by viewModel.selectedDiscoveryCategory.collectAsState()
    val searchQuery by viewModel.venueSearchQuery.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()
    val activeOrder by viewModel.activeDemoOrder.collectAsState()
    val voiceDockText by viewModel.voiceDockStateText.collectAsState()
    val isSpeaking by viewModel.voiceDockIsSpeaking.collectAsState()
    val actionStatus by viewModel.voiceDockActionStatus.collectAsState()
    val currentLang by viewModel.currentLang.collectAsState()

    // Dialog & sheet states
    var showAddStopDialog by remember { mutableStateOf(false) }
    var editingStop by remember { mutableStateOf<JourneyStop?>(null) }
    var showAddMemoryDialog by remember { mutableStateOf(false) }
    var selectedVenueForMenu by remember { mutableStateOf<DiscoveryVenue?>(null) }
    var showCartSheet by remember { mutableStateOf(false) }
    var showOrderConfirmDialog by remember { mutableStateOf(false) }

    val nextUpcomingStop = remember(stops) { stops.firstOrNull { !it.isVisited } ?: stops.lastOrNull() }

    val filteredVenues = remember(venues, selectedCategory, searchQuery) {
        venues.filter { venue ->
            venue.category == selectedCategory &&
                (searchQuery.isBlank() ||
                    venue.name.contains(searchQuery, ignoreCase = true) ||
                    venue.description.contains(searchQuery, ignoreCase = true) ||
                    venue.amenities.any { it.contains(searchQuery, ignoreCase = true) })
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        val isWideScreen = maxWidth >= 760.dp

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
                                    contentDescription = "Adventurer Icon",
                                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(10.dp)),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Journey Command Center",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Manali → Leh Highway · High Pass Protocol",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AdventureOrange
                                )
                            }
                        }
                    },
                    actions = {
                        // Switch to full-screen telemetry HUD
                        OutlinedButton(
                            onClick = { viewModel.navigateTo(Screen.LIVE_TRACKER) },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, AdventureOrange),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Icon(Icons.Default.Speed, contentDescription = null, tint = AdventureOrange, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("HUD", color = AdventureOrange, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        // Food cart badge button
                        if (cartItems.isNotEmpty() || activeOrder != null) {
                            IconButton(onClick = { showCartSheet = true }) {
                                BadgedBox(
                                    badge = {
                                        Badge(containerColor = AdventureOrange) {
                                            Text("${cartItems.sumOf { it.quantity }}")
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.ShoppingCart, contentDescription = "Trail food cart", tint = AdventureOrange)
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            if (isWideScreen) {
                // Tablet / Desktop 2-Column Split
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Left Column: Nav, Telemetry, Voice Dock & Fuel Calculator
                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 32.dp)
                    ) {
                        item {
                            PersistentVoiceCopilotDock(
                                text = voiceDockText,
                                isSpeaking = isSpeaking,
                                actionStatus = actionStatus,
                                onStopSpeaking = { viewModel.stopVoiceDockSpeaking() },
                                onPromptChip = { prompt ->
                                    viewModel.triggerVoiceDockPrompt(prompt, ActionStatus.SUGGESTED)
                                }
                            )
                        }
                        item {
                            CompactTurnByTurnCard(
                                onOpenGoogleMaps = {
                                    val gmmIntentUri = Uri.parse("google.navigation:q=32.5512,77.1724")
                                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                                        setPackage("com.google.android.apps.maps")
                                    }
                                    runCatching { context.startActivity(mapIntent) }.onFailure {
                                        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.google.com/?q=Jispa+Himachal+Pradesh"))
                                        runCatching { context.startActivity(webIntent) }
                                    }
                                }
                            )
                        }
                        item {
                            FuelRangeCalculatorCard(
                                state = fuelState,
                                onUpdate = { t, m, r, b, d ->
                                    viewModel.updateFuelCalculator(t, m, r, b, d)
                                }
                            )
                        }
                        item {
                            ContextualNextStopCard(
                                nextStop = nextUpcomingStop,
                                allStops = stops,
                                onToggleVisited = { viewModel.toggleStopVisited(it) },
                                onAddStop = { showAddStopDialog = true },
                                onEditStop = { editingStop = it }
                            )
                        }
                    }

                    // Right Column: Discovery, Food Orders & Editorial Memories
                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 32.dp)
                    ) {
                        item {
                            DiscoveryAndTrailServicesCard(
                                venues = filteredVenues,
                                selectedCategory = selectedCategory,
                                searchQuery = searchQuery,
                                onCategorySelect = { viewModel.setDiscoveryCategory(it) },
                                onSearchChange = { viewModel.setVenueSearchQuery(it) },
                                onOrderFoodClick = { venue ->
                                    selectedVenueForMenu = venue
                                },
                                onRouteToVenue = { venue ->
                                    viewModel.triggerVoiceDockPrompt(
                                        "Setting course to ${venue.name} (${venue.distanceKm} km away). Confirm turn-by-turn route.",
                                        ActionStatus.AWAITING_CONFIRMATION
                                    )
                                }
                            )
                        }
                        item {
                            EditorialMemoriesCard(
                                memories = memories,
                                onAddMemory = { showAddMemoryDialog = true },
                                onDeleteMemory = { viewModel.deleteJourneyMemory(it) }
                            )
                        }
                    }
                }
            } else {
                // Mobile Single Column Stack
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Pinned persistent Copilot Dock
                    item {
                        PersistentVoiceCopilotDock(
                            text = voiceDockText,
                            isSpeaking = isSpeaking,
                            actionStatus = actionStatus,
                            onStopSpeaking = { viewModel.stopVoiceDockSpeaking() },
                            onPromptChip = { prompt ->
                                viewModel.triggerVoiceDockPrompt(prompt, ActionStatus.SUGGESTED)
                            }
                        )
                    }

                    // Compact Google Maps Turn-by-Turn Card
                    item {
                        CompactTurnByTurnCard(
                            onOpenGoogleMaps = {
                                val gmmIntentUri = Uri.parse("google.navigation:q=32.5512,77.1724")
                                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                                    setPackage("com.google.android.apps.maps")
                                }
                                runCatching { context.startActivity(mapIntent) }.onFailure {
                                    val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.google.com/?q=Jispa+Himachal+Pradesh"))
                                    runCatching { context.startActivity(webIntent) }
                                }
                            }
                        )
                    }

                    // Contextual Next-Stop Card & Checkpoint Manager
                    item {
                        ContextualNextStopCard(
                            nextStop = nextUpcomingStop,
                            allStops = stops,
                            onToggleVisited = { viewModel.toggleStopVisited(it) },
                            onAddStop = { showAddStopDialog = true },
                            onEditStop = { editingStop = it }
                        )
                    }

                    // Fuel Range & Reserve Calculator
                    item {
                        FuelRangeCalculatorCard(
                            state = fuelState,
                            onUpdate = { t, m, r, b, d ->
                                viewModel.updateFuelCalculator(t, m, r, b, d)
                            }
                        )
                    }

                    // Trail Discovery (Food, Fuel, Stays, Passes)
                    item {
                        DiscoveryAndTrailServicesCard(
                            venues = filteredVenues,
                            selectedCategory = selectedCategory,
                            searchQuery = searchQuery,
                            onCategorySelect = { viewModel.setDiscoveryCategory(it) },
                            onSearchChange = { viewModel.setVenueSearchQuery(it) },
                            onOrderFoodClick = { venue ->
                                selectedVenueForMenu = venue
                            },
                            onRouteToVenue = { venue ->
                                viewModel.triggerVoiceDockPrompt(
                                    "Route suggested to ${venue.name} (${venue.distanceKm} km). Ready for handoff.",
                                    ActionStatus.AWAITING_CONFIRMATION
                                )
                            }
                        )
                    }

                    // Editorial Memories Timeline
                    item {
                        EditorialMemoriesCard(
                            memories = memories,
                            onAddMemory = { showAddMemoryDialog = true },
                            onDeleteMemory = { viewModel.deleteJourneyMemory(it) }
                        )
                    }
                }
            }
        }
    }

    // Modal: Restaurant Food Menu
    selectedVenueForMenu?.let { venue ->
        RestaurantMenuDialog(
            venue = venue,
            onDismiss = { selectedVenueForMenu = null },
            onAddToCart = { menuItem ->
                viewModel.addToCart(menuItem)
            },
            onOpenCart = {
                selectedVenueForMenu = null
                showCartSheet = true
            }
        )
    }

    // Modal: Food Cart & Destination Review
    if (showCartSheet) {
        FoodCartBottomSheet(
            cartItems = cartItems,
            activeOrder = activeOrder,
            onDismiss = { showCartSheet = false },
            onQuantityChange = { id, delta -> viewModel.updateCartQuantity(id, delta) },
            onProceedToConfirmation = { venueName, halt ->
                viewModel.submitDemoOrder(venueName, halt)
                showOrderConfirmDialog = true
            },
            onCancelOrder = { viewModel.cancelDemoOrder() }
        )
    }

    // Modal: Explicit Food Order Confirmation Step
    if (showOrderConfirmDialog && activeOrder != null) {
        ExplicitOrderConfirmationDialog(
            order = activeOrder!!,
            onDismiss = { showOrderConfirmDialog = false },
            onConfirmOrder = {
                viewModel.confirmDemoOrder()
                showOrderConfirmDialog = false
            },
            onCancelOrder = {
                viewModel.cancelDemoOrder()
                showOrderConfirmDialog = false
            }
        )
    }

    // Modal: Add or Edit Stop
    if (showAddStopDialog) {
        StopEditDialog(
            stop = null,
            onDismiss = { showAddStopDialog = false },
            onSave = { name, km, eta, alt, rem ->
                viewModel.addJourneyStop(name, km, eta, alt, rem)
                showAddStopDialog = false
            }
        )
    }

    editingStop?.let { stop ->
        StopEditDialog(
            stop = stop,
            onDismiss = { editingStop = null },
            onSave = { name, km, eta, alt, rem ->
                viewModel.editJourneyStop(stop.id, name, km, eta, alt, rem)
                editingStop = null
            },
            onDelete = {
                viewModel.deleteJourneyStop(stop.id)
                editingStop = null
            }
        )
    }

    // Modal: Add Editorial Memory
    if (showAddMemoryDialog) {
        AddMemoryDialog(
            onDismiss = { showAddMemoryDialog = false },
            onSave = { title, loc, alt, note, mood ->
                viewModel.addJourneyMemory(title, loc, alt, note, mood)
                showAddMemoryDialog = false
            }
        )
    }
}

/**
 * Action Status Pill with color and semantic icon.
 */
@Composable
fun ActionStatusBadge(status: ActionStatus, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(100.dp),
        color = Color(status.badgeColorHex).copy(alpha = 0.16f),
        border = BorderStroke(1.dp, Color(status.badgeColorHex)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(Color(status.badgeColorHex))
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = status.label,
                color = Color(status.badgeColorHex),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Pinned Voice Copilot Dock showing current status, spoken response, and quick prompt chips.
 */
@Composable
private fun PersistentVoiceCopilotDock(
    text: String,
    isSpeaking: Boolean,
    actionStatus: ActionStatus,
    onStopSpeaking: () -> Unit,
    onPromptChip: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = AdventureOrange.copy(alpha = 0.15f),
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Mic, contentDescription = null, tint = AdventureOrange, modifier = Modifier.size(16.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Adventure Copilot Dock",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                ActionStatusBadge(status = actionStatus)
            }

            // Spoken Response Balloon
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                    if (isSpeaking) {
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = onStopSpeaking,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.StopCircle, contentDescription = "Stop voice", tint = DangerRed)
                        }
                    }
                }
            }

            // Quick Copilot Action Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val chips = listOf(
                    "⛽ Safe fuel check" to "Fuel check: 11.5L in tank, safe range is 254 km. Margin +112 km.",
                    "🏔️ Next halt weather" to "Zingzingbar checkpoint is 32 km away. 8°C, gusty winds, oxygen 68%.",
                    "🍽️ Recommend chai stop" to "Chandra-Bhaga Dhaba has fresh hot Himalayan thukpa & ginger tea 3.2 km ahead.",
                    "✍️ Quick journal entry" to "Ready to log a new trail memory. Open the journal dialog below."
                )
                chips.forEach { (label, response) ->
                    SuggestionChip(
                        onClick = { onPromptChip(response) },
                        label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                    )
                }
            }
        }
    }
}

/**
 * Compact navigation card deferring turn-by-turn routing to Google Maps.
 */
@Composable
private fun CompactTurnByTurnCard(
    onOpenGoogleMaps: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Navigation, contentDescription = null, tint = HighAltitudeCyan, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ACTIVE PASS ROUTE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = HighAltitudeCyan)
                }
                Text("Leg 3 of 5", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Manali → Leh via Khardung La",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Next Turn: Bear right on NH-3 in 4.2 km (Jispa bypass)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Compact External Navigation Launcher
            Button(
                onClick = onOpenGoogleMaps,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().testTag("open_google_maps_button")
            ) {
                Icon(Icons.Default.Directions, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Open Turn-by-Turn in Google Maps",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontSize = 13.sp
                )
            }

            Text(
                text = "Turn-by-turn directions run in Google Maps. Adventurer provides trail telemetry, high-altitude advisory, fuel safety & roadside recovery.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                lineHeight = 14.sp
            )
        }
    }
}

/**
 * Contextual Next-Stop Card & interactive Checkpoint Manager.
 */
@Composable
private fun ContextualNextStopCard(
    nextStop: JourneyStop?,
    allStops: List<JourneyStop>,
    onToggleVisited: (String) -> Unit,
    onAddStop: () -> Unit,
    onEditStop: (JourneyStop) -> Unit,
    modifier: Modifier = Modifier
) {
    var expandedStops by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Flag, contentDescription = null, tint = AmberGold, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("CONTEXTUAL NEXT STOP", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = AmberGold)
                }
                ActionStatusBadge(status = nextStop?.status ?: ActionStatus.SUGGESTED)
            }

            if (nextStop != null) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = nextStop.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${nextStop.distanceFromStartKm} km from base · ~${nextStop.etaMinutes} min arrival · ${nextStop.altitudeM}m",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AdventureOrange,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            IconButton(onClick = { onEditStop(nextStop) }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit stop", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        // Reminder Tag
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = AmberGold, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = nextStop.reminderNote,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        // Visited toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (nextStop.isVisited) "Status: Checkpoint Cleared" else "Status: Approaching Halt",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (nextStop.isVisited) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            FilledTonalButton(
                                onClick = { onToggleVisited(nextStop.id) },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    if (nextStop.isVisited) Icons.Default.CheckCircle else Icons.Outlined.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (nextStop.isVisited) "Mark Pending" else "Mark Cleared")
                            }
                        }
                    }
                }
            }

            // Accordion toggle for all stops
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandedStops = !expandedStops }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "All Expedition Checkpoints (${allStops.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    imageVector = if (expandedStops) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (expandedStops) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    allStops.forEach { stop ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(
                                        imageVector = if (stop.isVisited) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                        contentDescription = null,
                                        tint = if (stop.isVisited) SuccessGreen else MaterialTheme.colorScheme.outline,
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clickable { onToggleVisited(stop.id) }
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(stop.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                                        Text("${stop.distanceFromStartKm} km · ${stop.altitudeM}m · ${stop.reminderNote}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                }
                                IconButton(onClick = { onEditStop(stop) }, modifier = Modifier.size(28.dp)) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = onAddStop,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.AddLocationAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add New Stop / Checkpoint")
                    }
                }
            }
        }
    }
}

/**
 * Interactive Fuel Range & Reserve Calculator with real-time margin alerts.
 */
@Composable
private fun FuelRangeCalculatorCard(
    state: FuelState,
    onUpdate: (Double, Double, Double, Double, Double) -> Unit,
    modifier: Modifier = Modifier
) {
    var fuelInTankText by remember(state) { mutableStateOf("${state.fuelInTankLiters}") }
    var mileageText by remember(state) { mutableStateOf("${state.mileageKmPerLiter}") }
    var reserveText by remember(state) { mutableStateOf("${state.reserveCapacityLiters}") }
    var bufferText by remember(state) { mutableStateOf("${state.safetyBufferPercent.toInt()}") }
    var nextPumpText by remember(state) { mutableStateOf("${state.distanceToNextFuelKm.toInt()}") }

    fun commitUpdates() {
        val tank = fuelInTankText.toDoubleOrNull() ?: state.fuelInTankLiters
        val mileage = mileageText.toDoubleOrNull() ?: state.mileageKmPerLiter
        val reserve = reserveText.toDoubleOrNull() ?: state.reserveCapacityLiters
        val buffer = bufferText.toDoubleOrNull() ?: state.safetyBufferPercent
        val nextDist = nextPumpText.toDoubleOrNull() ?: state.distanceToNextFuelKm
        onUpdate(tank, mileage, reserve, buffer, nextDist)
    }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocalGasStation, contentDescription = null, tint = AdventureOrange, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("FUEL RANGE & SAFETY BUFFER", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = AdventureOrange)
                }
                val statusText = if (state.isSafe) "SAFE TO PUMP" else "CRITICAL: REFUEL"
                val statusColor = if (state.isSafe) SuccessGreen else DangerRed
                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = statusColor.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, statusColor)
                ) {
                    Text(
                        text = statusText,
                        color = statusColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            // Big Metric Hero Display
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Calculated Safe Range", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "%.0f km".format(state.safeRangeKm),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (state.isSafe) SuccessGreen else DangerRed
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Distance to Next Pump", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "%.0f km".format(state.distanceToNextFuelKm),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (state.marginKm >= 0) "+%.0f km reserve margin".format(state.marginKm) else "Deficit: -%.0f km".format(-state.marginKm),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (state.marginKm >= 0) SuccessGreen else DangerRed
                    )
                }
            }

            // Editable Input Fields
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = fuelInTankText,
                    onValueChange = {
                        fuelInTankText = it
                        commitUpdates()
                    },
                    label = { Text("Tank (L)", fontSize = 11.sp) },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = mileageText,
                    onValueChange = {
                        mileageText = it
                        commitUpdates()
                    },
                    label = { Text("km/L", fontSize = 11.sp) },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = reserveText,
                    onValueChange = {
                        reserveText = it
                        commitUpdates()
                    },
                    label = { Text("Reserve (L)", fontSize = 11.sp) },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = bufferText,
                    onValueChange = {
                        bufferText = it
                        commitUpdates()
                    },
                    label = { Text("Buffer %", fontSize = 11.sp) },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            // Quick bike preset chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = state.fuelInTankLiters == 17.0,
                    onClick = {
                        fuelInTankText = "17.0"
                        mileageText = "26.0"
                        reserveText = "3.0"
                        commitUpdates()
                    },
                    label = { Text("Himalayan 450 (17L)", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = state.fuelInTankLiters == 14.5,
                    onClick = {
                        fuelInTankText = "14.5"
                        mileageText = "28.0"
                        reserveText = "2.5"
                        commitUpdates()
                    },
                    label = { Text("KTM 390 Adv (14.5L)", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = state.fuelInTankLiters == 23.0,
                    onClick = {
                        fuelInTankText = "23.0"
                        mileageText = "21.0"
                        reserveText = "4.0"
                        commitUpdates()
                    },
                    label = { Text("BMW GS 850 (23L)", fontSize = 11.sp) }
                )
            }
        }
    }
}

/**
 * Trail Discovery Hub (Food, Fuel Stations, Alpine Stays, Viewpoints)
 */
@Composable
private fun DiscoveryAndTrailServicesCard(
    venues: List<DiscoveryVenue>,
    selectedCategory: DiscoveryCategory,
    searchQuery: String,
    onCategorySelect: (DiscoveryCategory) -> Unit,
    onSearchChange: (String) -> Unit,
    onOrderFoodClick: (DiscoveryVenue) -> Unit,
    onRouteToVenue: (DiscoveryVenue) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Explore, contentDescription = null, tint = HighAltitudeCyan, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("TRAILSIDE DISCOVERY & FOOD", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = HighAltitudeCyan)
                }
                Text("${venues.size} Points Found", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Search dhabas, fuel, homestays, viewpoints…", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear search", modifier = Modifier.size(16.dp))
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Category Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DiscoveryCategory.values().forEach { category ->
                    val isSelected = category == selectedCategory
                    FilterChip(
                        selected = isSelected,
                        onClick = { onCategorySelect(category) },
                        label = { Text(category.displayName, fontSize = 12.sp) },
                        leadingIcon = {
                            val icon = when (category) {
                                DiscoveryCategory.RESTAURANTS -> Icons.Default.Restaurant
                                DiscoveryCategory.FUEL -> Icons.Default.LocalGasStation
                                DiscoveryCategory.STAYS -> Icons.Default.Hotel
                                DiscoveryCategory.VIEWPOINTS -> Icons.Default.Landscape
                            }
                            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    )
                }
            }

            // Venues List
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                venues.forEach { venue ->
                    VenueCardItem(
                        venue = venue,
                        onOrderFoodClick = { onOrderFoodClick(venue) },
                        onRouteClick = { onRouteToVenue(venue) }
                    )
                }
            }
        }
    }
}

@Composable
private fun VenueCardItem(
    venue: DiscoveryVenue,
    onOrderFoodClick: () -> Unit,
    onRouteClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(venue.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                    Text(
                        "${venue.distanceKm} km away · ${venue.altitudeM}m alt · ⭐ ${venue.rating}",
                        fontSize = 11.sp,
                        color = AdventureOrange,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = venue.category.displayName,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Text(venue.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 16.sp)

            // Amenities Tags
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                venue.amenities.forEach { amenity ->
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Text(amenity, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onRouteClick,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Target Route", fontSize = 11.sp)
                }

                if (venue.category == DiscoveryCategory.RESTAURANTS) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onOrderFoodClick,
                        colors = ButtonDefaults.buttonColors(containerColor = AdventureOrange),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.RestaurantMenu, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("View Menu & Order", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Editorial Travel Journal Memories Timeline (inspired by Kinfolk / National Geographic).
 */
@Composable
private fun EditorialMemoriesCard(
    memories: List<JourneyMemory>,
    onAddMemory: () -> Unit,
    onDeleteMemory: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoStories, contentDescription = null, tint = AmberGold, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("EDITORIAL MEMORIES JOURNAL", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = AmberGold)
                }
                FilledTonalButton(
                    onClick = onAddMemory,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Memory", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                memories.forEach { memory ->
                    EditorialMemoryItem(
                        memory = memory,
                        onDelete = { onDeleteMemory(memory.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun EditorialMemoryItem(
    memory: JourneyMemory,
    onDelete: () -> Unit
) {
    val gradientBrush = remember(memory.gradientColorsHex) {
        Brush.horizontalGradient(memory.gradientColorsHex.map { Color(it) })
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Editorial Accent Ribbon & Mood Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Text(
                        text = memory.moodTag,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete memory", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                }
            }

            Text(
                text = memory.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 20.sp
            )

            // Location & Altitude Capsule
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Place, contentDescription = null, tint = AdventureOrange, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(memory.locationName, fontSize = 11.sp, color = AdventureOrange, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Text("•", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(8.dp))
                Text(memory.timestamp, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // Narrative Reflection Note
            Text(
                text = "“${memory.reflection}”",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                lineHeight = 18.sp
            )

            // Decorative Gradient Banner Placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(gradientBrush)
            )
        }
    }
}

/**
 * Restaurant Menu Dialog to build a meal cart.
 */
@Composable
private fun RestaurantMenuDialog(
    venue: DiscoveryVenue,
    onDismiss: () -> Unit,
    onAddToCart: (MenuItem) -> Unit,
    onOpenCart: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(venue.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "High-Altitude Kitchen · Fresh Hot Food",
                    style = MaterialTheme.typography.labelSmall,
                    color = AdventureOrange
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().heightIn(max = 380.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(venue.menuItems) { item ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(item.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                                Text("₹${item.priceInr}", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = SuccessGreen)
                            }
                            Button(
                                onClick = { onAddToCart(item) },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text("+ Add", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onOpenCart,
                colors = ButtonDefaults.buttonColors(containerColor = AdventureOrange)
            ) {
                Text("Review Food Cart", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Back to Command")
            }
        }
    )
}

/**
 * Food Cart Bottom Sheet with simulation disclaimers and steppers.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FoodCartBottomSheet(
    cartItems: List<CartItem>,
    activeOrder: DemoFoodOrder?,
    onDismiss: () -> Unit,
    onQuantityChange: (String, Int) -> Unit,
    onProceedToConfirmation: (String, String) -> Unit,
    onCancelOrder: () -> Unit
) {
    var selectedHalt by remember { mutableStateOf("Zingzingbar Army Checkpost (32 km ahead)") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Trailside Food Cart", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = AmberGold.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, AmberGold)
                ) {
                    Text("SIMULATION ONLY", color = AmberGold, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                }
            }

            // Prominent simulation disclaimer banner
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Demo Simulation Flow: This feature demonstrates trailside food planning and timing. No real food is dispatched and no charges will occur.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        lineHeight = 15.sp
                    )
                }
            }

            if (cartItems.isEmpty()) {
                if (activeOrder != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SuccessGreen.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, SuccessGreen),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Active Simulated Order Confirmed", fontWeight = FontWeight.Bold, color = SuccessGreen)
                            Text("Venue: ${activeOrder.venueName} · Total: ₹${activeOrder.subtotalInr}", fontSize = 12.sp)
                            Text("Delivery Halt: ${activeOrder.deliveryHalt}", fontSize = 12.sp)
                            ActionStatusBadge(status = activeOrder.status)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedButton(onClick = onCancelOrder, shape = RoundedCornerShape(8.dp)) {
                                Text("Cancel Demo Order", fontSize = 11.sp)
                            }
                        }
                    }
                } else {
                    Text("Your cart is empty. Tap any restaurant in the Discovery section to add hot food.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    cartItems.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.menuItem.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text("₹${item.menuItem.priceInr} each", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { onQuantityChange(item.menuItem.id, -1) }, modifier = Modifier.size(28.dp)) {
                                    Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Decrease", modifier = Modifier.size(18.dp))
                                }
                                Text("${item.quantity}", fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 6.dp))
                                IconButton(onClick = { onQuantityChange(item.menuItem.id, 1) }, modifier = Modifier.size(28.dp)) {
                                    Icon(Icons.Default.AddCircleOutline, contentDescription = "Increase", modifier = Modifier.size(18.dp))
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("₹${item.menuItem.priceInr * item.quantity}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SuccessGreen)
                            }
                        }
                    }
                }

                Divider()

                // Delivery Halt Destination Selection
                Text("Target Pickup / Delivery Stop:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                listOf(
                    "Zingzingbar Army Checkpost (32 km ahead)",
                    "Chandra-Bhaga Dhaba Counter (Takeaway)",
                    "Darcha Bridge Crossing (12 km ahead)"
                ).forEach { halt ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedHalt = halt }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = selectedHalt == halt, onClick = { selectedHalt = halt })
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(halt, fontSize = 12.sp)
                    }
                }

                val total = cartItems.sumOf { it.menuItem.priceInr * it.quantity }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Total Amount (Simulated):", fontWeight = FontWeight.Bold)
                    Text("₹$total", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = SuccessGreen)
                }

                Button(
                    onClick = {
                        onProceedToConfirmation("Chandra-Bhaga High Altitude Dhaba", selectedHalt)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AdventureOrange),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Review & Explicitly Confirm Order", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

/**
 * Explicit Food Order Confirmation Dialog with safety boundaries.
 */
@Composable
private fun ExplicitOrderConfirmationDialog(
    order: DemoFoodOrder,
    onDismiss: () -> Unit,
    onConfirmOrder: () -> Unit,
    onCancelOrder: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Confirm Demo Food Order", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                ActionStatusBadge(status = ActionStatus.AWAITING_CONFIRMATION)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Venue: ${order.venueName}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Destination: ${order.deliveryHalt}", fontSize = 12.sp)
                        Text("Items: ${order.items.joinToString { "${it.menuItem.name} (x${it.quantity})" }}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Total: ₹${order.subtotalInr}", fontWeight = FontWeight.Bold, color = SuccessGreen)
                    }
                }

                Text(
                    text = "⚠️ EXPLICIT CONFIRMATION NOTICE: This order is a prototype simulation. No funds will be charged and no real restaurant kitchen will be notified. Confirming sets the local action state to 'Verified Complete'.",
                    fontSize = 11.sp,
                    color = AmberGold,
                    lineHeight = 15.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmOrder,
                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
            ) {
                Text("Confirm Simulation", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelOrder) {
                Text("Cancel", color = DangerRed)
            }
        }
    )
}

/**
 * Add / Edit Checkpoint Dialog.
 */
@Composable
private fun StopEditDialog(
    stop: JourneyStop?,
    onDismiss: () -> Unit,
    onSave: (String, Double, Int, Int, String) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var name by remember { mutableStateOf(stop?.name ?: "") }
    var kmText by remember { mutableStateOf(stop?.let { "${it.distanceFromStartKm}" } ?: "45.0") }
    var etaText by remember { mutableStateOf(stop?.let { "${it.etaMinutes}" } ?: "60") }
    var altText by remember { mutableStateOf(stop?.let { "${it.altitudeM}" } ?: "3800") }
    var reminder by remember { mutableStateOf(stop?.reminderNote ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (stop == null) "Add Expedition Checkpoint" else "Edit Checkpoint", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Checkpoint Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = kmText,
                        onValueChange = { kmText = it },
                        label = { Text("Distance (km)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = etaText,
                        onValueChange = { etaText = it },
                        label = { Text("ETA (min)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                OutlinedTextField(
                    value = altText,
                    onValueChange = { altText = it },
                    label = { Text("Altitude (m)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = reminder,
                    onValueChange = { reminder = it },
                    label = { Text("Reminder Note") },
                    placeholder = { Text("e.g. Check water crossings") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val km = kmText.toDoubleOrNull() ?: 0.0
                    val eta = etaText.toIntOrNull() ?: 30
                    val alt = altText.toIntOrNull() ?: 3000
                    onSave(name, km, eta, alt, reminder)
                },
                colors = ButtonDefaults.buttonColors(containerColor = AdventureOrange)
            ) {
                Text("Save Checkpoint", color = Color.White)
            }
        },
        dismissButton = {
            Row {
                if (onDelete != null) {
                    TextButton(onClick = onDelete) {
                        Text("Delete", color = DangerRed)
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}

/**
 * Add Editorial Memory Journal Dialog.
 */
@Composable
private fun AddMemoryDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, Int, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("High Pass Vista") }
    var altitudeText by remember { mutableStateOf("4250") }
    var reflection by remember { mutableStateOf("") }
    var moodTag by remember { mutableStateOf("Golden Hour ☀️") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Editorial Trail Memory", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Memory Title") },
                    placeholder = { Text("e.g. Whispers of the High Pass") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = altitudeText,
                    onValueChange = { altitudeText = it },
                    label = { Text("Altitude (m)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = reflection,
                    onValueChange = { reflection = it },
                    label = { Text("Journal Reflection / Story") },
                    placeholder = { Text("Describe the light, air, sound of the engine, feeling...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                // Mood selection
                Text("Mood / Feeling:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Golden Hour ☀️", "Alpine Reverence 🏔️", "Trail Brotherhood 🤝", "Pure Adrenaline ⚡", "Quiet Solitude 🌌").forEach { mood ->
                        FilterChip(
                            selected = moodTag == mood,
                            onClick = { moodTag = mood },
                            label = { Text(mood, fontSize = 11.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val alt = altitudeText.toIntOrNull() ?: 3500
                    onSave(title, location, alt, reflection, moodTag)
                },
                colors = ButtonDefaults.buttonColors(containerColor = AmberGold)
            ) {
                Text("Save to Journal", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
