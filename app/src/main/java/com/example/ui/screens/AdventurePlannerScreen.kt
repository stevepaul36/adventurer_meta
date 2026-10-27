package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.analytics.AdventureFunnelAnalytics
import com.example.data.model.ActivityType
import com.example.data.planning.AdventureCreatorDraftEntity
import com.example.data.planning.AdventurePlaceBookmarkEntity
import com.example.data.planning.AdventurePlannerRepository
import com.example.data.planning.AdventureTripPlanEntity
import com.example.data.providers.AdventureMarket
import com.example.data.providers.RegionalProviderCatalog
import com.example.data.providers.RegionalServiceCategory
import com.example.ui.components.ExpeditionGearPackListCard
import com.example.ui.theme.AdventureOrange
import com.example.ui.theme.AmberGold
import com.example.ui.theme.HighAltitudeCyan
import com.example.ui.theme.SuccessGreen
import com.example.viewmodel.AdventurerViewModel
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

private data class AdventureIdea(
    val id: String,
    val title: String,
    val category: String,
    val description: String,
    val activities: Set<ActivityType>,
)

private data class DraftTripStop(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val category: String,
    val day: Int,
    val notes: String,
)

private val everyActivity = ActivityType.values().toSet()
private val adventureIdeas = listOf(
    AdventureIdea("viewpoint", "Scenic viewpoint / photo stop", "Viewpoint", "Pause for photos; verify access, parking and the actual route.", everyActivity),
    AdventureIdea("trailhead", "Trailhead and return route", "Trail", "Record the trailhead, exit point, difficulty source and turnaround plan.", setOf(ActivityType.Walking, ActivityType.Trekking, ActivityType.Cycling, ActivityType.ClimbingCaving)),
    AdventureIdea("food", "Local food and resupply", "Food & supplies", "Plan a meal or water refill. Verify live opening hours before departing.", setOf(ActivityType.Motorcycling, ActivityType.Cycling, ActivityType.Walking, ActivityType.Trekking, ActivityType.CarTrips, ActivityType.Camping, ActivityType.Hybrid, ActivityType.IslandExploration, ActivityType.DestinationExploration)),
    AdventureIdea("camp", "Campsite / overnight base", "Camping", "Verify camping permission, availability, fees, amenities and fire rules.", setOf(ActivityType.Camping, ActivityType.Trekking, ActivityType.CarTrips, ActivityType.Motorcycling, ActivityType.Cycling, ActivityType.Hybrid)),
    AdventureIdea("fuel-repair", "Fuel, EV charge or bike repair", "Resupply", "Add a vehicle/bike support stop and verify the actual provider.", setOf(ActivityType.Motorcycling, ActivityType.Cycling, ActivityType.CarTrips, ActivityType.Camping, ActivityType.Hybrid)),
    AdventureIdea("water", "Shore entry, boat ramp or launch", "Water access", "Verify weather, tides/currents, access rights and local guidance.", setOf(ActivityType.ScubaDiving, ActivityType.Snorkeling, ActivityType.KayakingPaddling, ActivityType.BoatingSailing, ActivityType.Fishing, ActivityType.IslandExploration)),
    AdventureIdea("wildlife", "Wildlife / nature observation", "Nature", "Plan responsible observation; keep sensitive wildlife coordinates private.", setOf(ActivityType.WildlifeExploration, ActivityType.Hunting, ActivityType.Fishing, ActivityType.Trekking, ActivityType.Camping, ActivityType.Walking)),
    AdventureIdea("transfer", "Ferry, transit or transfer", "Transport", "Add a connection that constrains timing. Verify with the official operator.", setOf(ActivityType.CarTrips, ActivityType.Cycling, ActivityType.Walking, ActivityType.Trekking, ActivityType.IslandExploration, ActivityType.BoatingSailing, ActivityType.Hybrid)),
    AdventureIdea("culture", "Local culture / small-town stop", "Culture", "Make space for community-run tours, local history, food or markets.", setOf(ActivityType.CarTrips, ActivityType.Motorcycling, ActivityType.Cycling, ActivityType.Walking, ActivityType.DestinationExploration, ActivityType.IslandExploration)),
    AdventureIdea("regroup", "Buddy check / regroup point", "Safety", "Choose a rendezvous and fallback if the group splits or loses connectivity.", everyActivity),
)

private val preparationItems = listOf(
    "Check official weather, access, closures and local rules",
    "Share the plan and expected return with a trusted person",
    "Pack water, food, first aid and activity-specific equipment",
    "Confirm your chosen navigation tool covers the intended offline area",
    "Set a turnaround time and a fallback / exit plan",
)

private val quickDestinationPresets = listOf(
    Triple("🌲 Banff Passes", "Banff & Jasper Alpine Passes", 3),
    Triple("🏔️ Leh Ridge", "Leh & Khardung La High Pass", 7),
    Triple("🌊 Pacific Rim", "Pacific Rim Coastal Trail", 4),
    Triple("🏜️ White Rann", "Kutch White Desert Ride", 2),
    Triple("🏞️ Moab Canyons", "Moab Red Rock Canyonlands", 5),
    Triple("🌿 Western Ghats", "Western Ghats Monsoon Loop", 3)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdventurePlannerScreen(
    viewModel: AdventurerViewModel,
    plannerRepository: AdventurePlannerRepository,
    modifier: Modifier = Modifier,
) {
    BackHandler { viewModel.navigateBack() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val activityMode by viewModel.activityMode.collectAsState()
    val trips by plannerRepository.observeTrips().collectAsState(initial = emptyList())
    val savedPlaces by plannerRepository.observeSavedPlaces().collectAsState(initial = emptyList())
    val creatorDrafts by plannerRepository.observeCreatorDrafts().collectAsState(initial = emptyList())

    var destinationQuery by rememberSaveable { mutableStateOf("") }
    var ideaQuery by rememberSaveable { mutableStateOf("") }
    var planTitle by rememberSaveable { mutableStateOf("") }
    var destination by rememberSaveable { mutableStateOf("") }
    var startDate by rememberSaveable { mutableStateOf("Flexible dates") }
    var daysText by rememberSaveable { mutableStateOf("2") }
    var budgetText by rememberSaveable { mutableStateOf("450") }
    var selectedActivities by remember { mutableStateOf(setOf(activityMode)) }
    var selectedStopDay by rememberSaveable { mutableStateOf(1) }
    var checkInEnabled by rememberSaveable { mutableStateOf(true) }
    var checkInInterval by rememberSaveable { mutableStateOf(60) }
    var expectedReturn by rememberSaveable { mutableStateOf("") }
    var trustedContact by rememberSaveable { mutableStateOf("") }
    var checkedPrep by remember { mutableStateOf(emptySet<String>()) }
    var formError by remember { mutableStateOf<String?>(null) }
    val draftStops = remember { mutableStateListOf<DraftTripStop>() }
    var pendingExportJson by remember { mutableStateOf<String?>(null) }
    var guideDialog by remember { mutableStateOf(false) }
    var eventDialog by remember { mutableStateOf(false) }
    var guideTitle by rememberSaveable { mutableStateOf("") }
    var guideDestination by rememberSaveable { mutableStateOf("") }
    var guideDescription by rememberSaveable { mutableStateOf("") }
    var guidePrice by rememberSaveable { mutableStateOf("") }
    var eventTitle by rememberSaveable { mutableStateOf("") }
    var eventDestination by rememberSaveable { mutableStateOf("") }
    var eventDate by rememberSaveable { mutableStateOf("") }
    var eventDescription by rememberSaveable { mutableStateOf("") }
    var localMetrics by remember { mutableStateOf(AdventureFunnelAnalytics.localSnapshot(context)) }

    // Market and provider preferences are local, editable settings. Locale only suggests a market;
    // location permission is not needed and country selection is never inferred from GPS.
    val marketPreferences = remember(context) {
        context.getSharedPreferences("adventurer_market_preferences", android.content.Context.MODE_PRIVATE)
    }
    val suggestedMarket = remember {
        AdventureMarket.suggestedFromCountryCode(Locale.getDefault().country) ?: AdventureMarket.CANADA
    }
    var selectedMarketCode by rememberSaveable {
        mutableStateOf(
            runCatching {
                marketPreferences.getString("selected_market", null)
                    ?.let(AdventureMarket::valueOf)?.name ?: suggestedMarket.name
            }.getOrDefault(suggestedMarket.name)
        )
    }
    var selectedRegionalCategoryCode by rememberSaveable {
        mutableStateOf(
            runCatching {
                marketPreferences.getString("selected_service_category", null)
                    ?.let(RegionalServiceCategory::valueOf)?.name ?: RegionalServiceCategory.FOOD.name
            }.getOrDefault(RegionalServiceCategory.FOOD.name)
        )
    }
    val selectedMarket = AdventureMarket.valueOf(selectedMarketCode)
    val selectedRegionalCategory = RegionalServiceCategory.valueOf(selectedRegionalCategoryCode)
    val preferredProviderKey = "preferred_provider_${selectedMarketCode}_${selectedRegionalCategoryCode}"
    var preferredProviderId by rememberSaveable {
        mutableStateOf(marketPreferences.getString(preferredProviderKey, "").orEmpty())
    }
    val regionalProviders = remember(selectedMarket, selectedRegionalCategory) {
        RegionalProviderCatalog.forCategory(selectedMarket, selectedRegionalCategory)
    }

    LaunchedEffect(selectedMarketCode) {
        marketPreferences.edit().putString("selected_market", selectedMarketCode).apply()
    }
    LaunchedEffect(selectedRegionalCategoryCode) {
        marketPreferences.edit().putString("selected_service_category", selectedRegionalCategoryCode).apply()
    }
    LaunchedEffect(preferredProviderKey) {
        preferredProviderId = marketPreferences.getString(preferredProviderKey, "").orEmpty()
    }

    LaunchedEffect(activityMode) {
        if (selectedActivities.isEmpty()) selectedActivities = setOf(activityMode)
    }
    fun refreshMetrics() { localMetrics = AdventureFunnelAnalytics.localSnapshot(context) }

    fun exportTripPack(plan: AdventureTripPlanEntity) {
        pendingExportJson = JSONObject()
            .put("format", "adventurer-trip-pack-v1")
            .put("title", plan.title)
            .put("destination", plan.destination)
            .put("activityTypes", JSONArray(plan.activityTypesCsv.split(",").filter { it.isNotBlank() }))
            .put("startDateLabel", plan.startDateLabel)
            .put("durationDays", plan.durationDays)
            .put("budgetCad", plan.budgetCad)
            .put("itineraryStops", JSONArray(plan.itineraryStopsJson))
            .put("preparationChecklist", JSONArray(plan.prepChecklistJson))
            .put("safetyPlan", JSONObject()
                .put("checkInEnabled", plan.checkInEnabled)
                .put("checkInIntervalMinutes", plan.checkInIntervalMinutes)
                .put("expectedReturnLabel", plan.expectedReturnLabel)
                .put("trustedContactLabel", plan.trustedContactLabel))
            .put("privacy", plan.visibility)
            .put("includesMapTiles", false)
            .put("includesLiveConditions", false)
            .put("note", "Offline trip data export only. Map tiles, live weather/conditions and verified local rules are not included.")
            .toString(2)
    }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        val json = pendingExportJson
        if (uri != null && json != null) {
            runCatching {
                context.contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray(Charsets.UTF_8)) }
                    ?: error("Could not open the selected destination.")
            }.onSuccess {
                Toast.makeText(context, "Trip data exported. Map tiles and live conditions are not included.", Toast.LENGTH_LONG).show()
            }.onFailure {
                Toast.makeText(context, "Could not export this trip pack.", Toast.LENGTH_LONG).show()
            }
        }
        pendingExportJson = null
    }

    val matchingIdeas = remember(ideaQuery, activityMode) {
        adventureIdeas.filter { idea ->
            (activityMode in idea.activities || ActivityType.Hybrid in idea.activities) &&
                (ideaQuery.isBlank() || listOf(idea.title, idea.category, idea.description).any { it.contains(ideaQuery, true) })
        }
    }

    val parsedDays = daysText.toIntOrNull()?.coerceIn(1, 14) ?: 1

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Explore & Plan",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Discover a place · build the trip · prepare safely",
                            color = AmberGold,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier,
    ) { insets ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(insets),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Quick Destination Recommendations & Live Search
            item(key = "destination-discovery") {
                PlannerPanelCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Explore, contentDescription = null, tint = AdventureOrange)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Find a new place",
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Interactive Quick Destination Presets
                    Text(
                        "Popular Expeditions (tap to load):",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        quickDestinationPresets.forEach { (chipLabel, fullTitle, days) ->
                            SuggestionChip(
                                onClick = {
                                    destination = fullTitle
                                    destinationQuery = fullTitle
                                    planTitle = "$fullTitle Tour"
                                    daysText = days.toString()
                                },
                                label = { Text(chipLabel, fontSize = 12.sp, fontWeight = FontWeight.Medium) },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    labelColor = MaterialTheme.colorScheme.onSurface
                                ),
                                border = SuggestionChipDefaults.suggestionChipBorder(
                                    enabled = true,
                                    borderColor = MaterialTheme.colorScheme.outline
                                )
                            )
                        }
                    }

                    OutlinedTextField(
                        value = destinationQuery,
                        onValueChange = { destinationQuery = it },
                        label = { Text("City, island, region or attraction") },
                        placeholder = { Text("e.g. Vancouver Island, Ladakh, Banff") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("destination_search_field"),
                        singleLine = true,
                        colors = plannerFieldColors(),
                    )
                    Button(
                        onClick = {
                            val term = destinationQuery.trim()
                            if (term.isBlank()) Toast.makeText(context, "Enter a destination or interest to search.", Toast.LENGTH_SHORT).show()
                            else {
                                val uri = Uri.parse(RegionalProviderCatalog.googleMapsSearchUrl("$term outdoor places", selectedMarket))
                                runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) }
                                    .onFailure { Toast.makeText(context, "No browser or map app can open this search.", Toast.LENGTH_LONG).show() }
                                AdventureFunnelAnalytics.log(context, "destination_map_search_opened", mapOf("entry_point" to "explore_planner"))
                                refreshMetrics()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = AdventureOrange),
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, tint = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text("Search live places in Maps", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        "Opens external Maps. Trip notes and checklists can be saved locally; map tiles and live conditions are not included in this search handoff.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Market-aware, explicit external provider handoffs.
            item(key = "regional-provider-handoffs") {
                PlannerPanelCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Public, contentDescription = null, tint = AdventureOrange)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Services for your market",
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        "Choose India or Canada to see relevant services. This preference does not use your location.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AdventureMarket.values().forEach { market ->
                            FilterChip(
                                selected = selectedMarket == market,
                                onClick = { selectedMarketCode = market.name },
                                label = { Text("${market.countryName} · ${market.currencyCode}") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AdventureOrange,
                                    selectedLabelColor = Color.White,
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    labelColor = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RegionalServiceCategory.values().forEach { category ->
                            FilterChip(
                                selected = selectedRegionalCategory == category,
                                onClick = { selectedRegionalCategoryCode = category.name },
                                label = { Text(category.label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = HighAltitudeCyan,
                                    selectedLabelColor = Color.White,
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    labelColor = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        regionalProviders.forEach { provider ->
                            Card(
                                modifier = Modifier.width(252.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            provider.name,
                                            modifier = Modifier.weight(1f),
                                            color = MaterialTheme.colorScheme.onSurface,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (preferredProviderId == provider.id) {
                                            Icon(
                                                Icons.Default.Star,
                                                contentDescription = "Preferred provider",
                                                tint = AmberGold,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        provider.summary,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Surface(
                                        color = AmberGold.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            "EXTERNAL HANDOFF ONLY",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            color = AmberGold,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    TextButton(
                                        modifier = Modifier.fillMaxWidth(),
                                        onClick = {
                                            preferredProviderId = provider.id
                                            marketPreferences.edit().putString(preferredProviderKey, provider.id).apply()
                                            Toast.makeText(context, "${provider.name} saved as your preferred provider.", Toast.LENGTH_SHORT).show()
                                        }
                                    ) {
                                        Icon(
                                            Icons.Default.Star,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(if (preferredProviderId == provider.id) "Preferred provider" else "Set as preferred")
                                    }
                                    OutlinedButton(
                                        modifier = Modifier.fillMaxWidth(),
                                        onClick = {
                                            runCatching {
                                                context.startActivity(
                                                    Intent(Intent.ACTION_VIEW, Uri.parse(provider.websiteUrl))
                                                )
                                            }.onSuccess {
                                                AdventureFunnelAnalytics.log(
                                                    context,
                                                    "regional_provider_handoff_opened",
                                                    mapOf(
                                                        "market" to selectedMarket.countryCode,
                                                        "category" to selectedRegionalCategory.name,
                                                        "provider_id" to provider.id
                                                    )
                                                )
                                                refreshMetrics()
                                            }.onFailure {
                                                Toast.makeText(
                                                    context,
                                                    "No browser or compatible app could open this provider.",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                        }
                                    ) {
                                        Text("Open provider")
                                    }
                                }
                            }
                        }
                    }
                    Text(
                        "Opening a provider is not a booking or order. Prices, availability, cart contents and receipts remain with that provider until an approved integration can verify them.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Quick Stop Ideas
            item(key = "quick-ideas") {
                PlannerPanelCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Place, contentDescription = null, tint = HighAltitudeCyan)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Add adventure stops",
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        "Starter ideas adapt to your preferred mode (" + activityMode.displayName + ").",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = ideaQuery,
                        onValueChange = { ideaQuery = it },
                        label = { Text("Filter stop ideas") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = plannerFieldColors(),
                    )
                    if (matchingIdeas.isEmpty()) {
                        Text(
                            "No matching prompts. Try viewpoint, trail, camping, food, transfer or safety.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    matchingIdeas.forEach { idea ->
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(idea.title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                                    Text(idea.category, color = HighAltitudeCyan, style = MaterialTheme.typography.labelSmall)
                                }
                                val isSaved = savedPlaces.any { it.id == idea.id }
                                IconButton(onClick = {
                                    scope.launch {
                                        if (isSaved) plannerRepository.removePlace(idea.id)
                                        else {
                                            plannerRepository.savePlace(
                                                AdventurePlaceBookmarkEntity(
                                                    id = idea.id,
                                                    title = idea.title,
                                                    category = idea.category,
                                                    activityType = activityMode.name,
                                                    destinationContext = destination.ifBlank { destinationQuery },
                                                    description = idea.description,
                                                )
                                            )
                                            AdventureFunnelAnalytics.log(context, "place_saved", mapOf("activity_type" to activityMode.name, "place_category" to idea.category))
                                            refreshMetrics()
                                        }
                                    }
                                }) {
                                    Icon(
                                        if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                        contentDescription = if (isSaved) "Remove saved idea" else "Save place idea",
                                        tint = if (isSaved) AmberGold else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Text(idea.description, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        draftStops.add(
                                            DraftTripStop(
                                                title = idea.title,
                                                category = idea.category,
                                                day = selectedStopDay.coerceIn(1, parsedDays),
                                                notes = idea.description,
                                            )
                                        )
                                        formError = null
                                    },
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                ) {
                                    Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Add to Day $selectedStopDay", color = MaterialTheme.colorScheme.onSurface)
                                }
                                TextButton(onClick = {
                                    val term = (destination.ifBlank { destinationQuery } + " " + idea.title).trim()
                                    if (term.isNotBlank()) runCatching {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(RegionalProviderCatalog.googleMapsSearchUrl(term, selectedMarket))))
                                    }
                                }) { Text("Check provider", color = HighAltitudeCyan) }
                            }
                        }
                    }
                }
            }

            // Interactive Multi-day Itinerary Builder
            item(key = "trip-builder") {
                PlannerPanelCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Timeline, contentDescription = null, tint = AdventureOrange)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Build a multi-day itinerary",
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    OutlinedTextField(
                        value = planTitle,
                        onValueChange = { planTitle = it.take(100) },
                        label = { Text("Trip name") },
                        placeholder = { Text("e.g. Alpine Ridge Discovery") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = plannerFieldColors()
                    )
                    OutlinedTextField(
                        value = destination,
                        onValueChange = { destination = it.take(120) },
                        label = { Text("Destination / region") },
                        placeholder = { Text("Where do you want to explore?") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = plannerFieldColors()
                    )
                    OutlinedTextField(
                        value = startDate,
                        onValueChange = { startDate = it.take(60) },
                        label = { Text("Start date / timing") },
                        placeholder = { Text("e.g. flexible, or next weekend") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = plannerFieldColors()
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = daysText,
                            onValueChange = { daysText = it.filter(Char::isDigit).take(2) },
                            label = { Text("Days (1–14)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = plannerFieldColors()
                        )
                        OutlinedTextField(
                            value = budgetText,
                            onValueChange = { budgetText = it.filter { c -> c.isDigit() || c == '.' }.take(10) },
                            label = { Text("Budget CAD") },
                            modifier = Modifier.weight(1.25f),
                            singleLine = true,
                            colors = plannerFieldColors()
                        )
                    }

                    // Interactive Live Budget Breakdown Calculator
                    val currentBudget = budgetText.toDoubleOrNull() ?: 450.0
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = AmberGold, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    "Live Trip Budget Estimator: CAD $${"%.0f".format(currentBudget)}",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            // Quick Adjustment Pills
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Adjust:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                listOf(-50, 50, 100).forEach { delta ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        modifier = Modifier.clickable {
                                            val updated = (currentBudget + delta).coerceAtLeast(50.0)
                                            budgetText = "%.0f".format(updated)
                                        }
                                    ) {
                                        Text(
                                            text = if (delta > 0) "+$delta" else "$delta",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AdventureOrange,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                            // Allocation rows
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                BudgetPill("⛽ Fuel (35%)", currentBudget * 0.35, AdventureOrange)
                                BudgetPill("🏕️ Stay (30%)", currentBudget * 0.30, HighAltitudeCyan)
                                BudgetPill("🥪 Food (20%)", currentBudget * 0.20, AmberGold)
                                BudgetPill("🧰 Buffer (15%)", currentBudget * 0.15, SuccessGreen)
                            }
                        }
                    }

                    // Activities Selector
                    Text("Activities in this trip", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ActivityType.values().forEach { mode ->
                            FilterChip(
                                selected = mode in selectedActivities,
                                onClick = {
                                    selectedActivities = if (mode in selectedActivities) {
                                        if (selectedActivities.size > 1) selectedActivities - mode else selectedActivities
                                    } else selectedActivities + mode
                                },
                                label = { Text(mode.displayName) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AdventureOrange,
                                    selectedLabelColor = Color.White,
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    labelColor = MaterialTheme.colorScheme.onSurface
                                ),
                            )
                        }
                    }

                    // Interactive Day Tabs Strip
                    Text("Itinerary Day Selector (tap to plan):", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        (1..parsedDays).forEach { day ->
                            val stopCount = draftStops.count { it.day == day }
                            FilterChip(
                                selected = selectedStopDay == day,
                                onClick = { selectedStopDay = day },
                                label = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Day $day")
                                        if (stopCount > 0) {
                                            Spacer(Modifier.width(4.dp))
                                            Surface(
                                                shape = CircleShape,
                                                color = if (selectedStopDay == day) Color.White.copy(alpha = 0.25f) else HighAltitudeCyan.copy(alpha = 0.2f),
                                                modifier = Modifier.size(16.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        stopCount.toString(),
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (selectedStopDay == day) Color.White else HighAltitudeCyan
                                                    )
                                                }
                                            }
                                        }
                                    }
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = HighAltitudeCyan,
                                    selectedLabelColor = Color.White,
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    labelColor = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }

                    // Draft Stops List
                    if (draftStops.isEmpty()) {
                        Text(
                            "Add stop ideas above. Stops will be automatically grouped by the selected day.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    } else {
                        draftStops.groupBy { it.day }.toSortedMap().forEach { (day, stops) ->
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Day $day Plan (${stops.size} stops)", color = AmberGold, fontWeight = FontWeight.Bold)
                                stops.forEach { stop ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Column(Modifier.weight(1f)) {
                                            Text(stop.title, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyMedium)
                                            Text(stop.category, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                                        }
                                        IconButton(onClick = { draftStops.remove(stop) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Remove stop", tint = Color(0xFFEF4444))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Interactive Animated Preparation Checklist
                    val prepProgress = if (preparationItems.isNotEmpty()) (checkedPrep.size.toFloat() / preparationItems.size.toFloat()) else 0f
                    val animatedPrepProgress by animateFloatAsState(targetValue = prepProgress, animationSpec = tween(400), label = "prep_progress")

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Preparation Checklist", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                            Text(
                                "${checkedPrep.size}/${preparationItems.size} (${(prepProgress * 100).toInt()}%)",
                                color = if (prepProgress == 1.0f) SuccessGreen else AmberGold,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                        LinearProgressIndicator(
                            progress = { animatedPrepProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = if (prepProgress == 1.0f) SuccessGreen else AdventureOrange,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        if (prepProgress == 1.0f) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = SuccessGreen.copy(alpha = 0.15f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Ready for Departure 🚀", color = SuccessGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    preparationItems.forEach { label ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = label in checkedPrep,
                                onCheckedChange = { checked -> checkedPrep = if (checked) checkedPrep + label else checkedPrep - label }
                            )
                            Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    // Smart Gear & Pack Checklist Component
                    ExpeditionGearPackListCard()

                    // Safety Plan Card
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.SafetyCheck, contentDescription = null, tint = AmberGold)
                                Spacer(Modifier.width(8.dp))
                                Text("Safety plan & check-ins", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.weight(1f))
                                Switch(checked = checkInEnabled, onCheckedChange = { checkInEnabled = it })
                            }
                            if (checkInEnabled) {
                                Text("Check-in interval", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf(30, 60, 120).forEach { interval ->
                                        FilterChip(
                                            selected = checkInInterval == interval,
                                            onClick = { checkInInterval = interval },
                                            label = { Text("$interval min") },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = AmberGold,
                                                selectedLabelColor = Color.Black,
                                                containerColor = MaterialTheme.colorScheme.surface,
                                                labelColor = MaterialTheme.colorScheme.onSurface
                                            )
                                        )
                                    }
                                }
                                OutlinedTextField(
                                    value = expectedReturn,
                                    onValueChange = { expectedReturn = it.take(80) },
                                    label = { Text("Expected return (time / note)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    colors = plannerFieldColors()
                                )
                                OutlinedTextField(
                                    value = trustedContact,
                                    onValueChange = { trustedContact = it.take(80) },
                                    label = { Text("Trusted contact label") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    colors = plannerFieldColors()
                                )
                            }
                        }
                    }

                    formError?.let { Text(it, color = Color(0xFFEF4444), style = MaterialTheme.typography.bodySmall) }

                    Button(
                        onClick = {
                            val validDays = daysText.toIntOrNull()?.takeIf { it in 1..14 }
                            if (planTitle.isBlank() || destination.isBlank()) formError = "Add a trip name and destination before saving."
                            else if (validDays == null) formError = "Choose a duration between 1 and 14 days."
                            else {
                                val now = System.currentTimeMillis()
                                val stopArray = JSONArray()
                                draftStops.forEach { stop ->
                                    stopArray.put(JSONObject().put("id", stop.id).put("title", stop.title).put("category", stop.category).put("day", stop.day.coerceIn(1, validDays)).put("notes", stop.notes))
                                }
                                val checklist = JSONArray()
                                preparationItems.forEach { label -> checklist.put(JSONObject().put("label", label).put("checked", label in checkedPrep)) }
                                val modes = selectedActivities.ifEmpty { setOf(activityMode) }
                                val plan = AdventureTripPlanEntity(
                                    title = planTitle.trim(),
                                    destination = destination.trim(),
                                    activityTypesCsv = modes.joinToString(",") { it.name },
                                    startDateLabel = startDate.ifBlank { "Flexible dates" },
                                    durationDays = validDays,
                                    budgetCad = budgetText.toDoubleOrNull()?.takeIf { it >= 0.0 },
                                    itineraryStopsJson = stopArray.toString(),
                                    prepChecklistJson = checklist.toString(),
                                    checkInEnabled = checkInEnabled,
                                    checkInIntervalMinutes = checkInInterval,
                                    expectedReturnLabel = expectedReturn.trim(),
                                    trustedContactLabel = trustedContact.trim(),
                                    createdAtMillis = now,
                                    updatedAtMillis = now,
                                )
                                scope.launch {
                                    runCatching { plannerRepository.saveTrip(plan) }
                                        .onSuccess {
                                            AdventureFunnelAnalytics.log(context, "trip_plan_created", mapOf("activity_count" to modes.size, "duration_days" to validDays, "stop_count" to draftStops.size))
                                            refreshMetrics()
                                            Toast.makeText(context, "Trip saved locally on this device.", Toast.LENGTH_LONG).show()
                                            planTitle = ""
                                            draftStops.clear()
                                            formError = null
                                        }
                                        .onFailure { formError = "Trip could not be saved locally. Please try again." }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_trip_plan_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = AdventureOrange),
                    ) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text("Save trip plan on this device", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Saved Plans Section
            item(key = "saved-trips-title") {
                PlannerSectionTitle("Saved trip plans", "${trips.size} on this device")
            }
            if (trips.isEmpty()) {
                item(key = "saved-trips-empty") {
                    PlannerPanelCard {
                        Text(
                            "Your saved plans appear here. Plans stay private and offline on your device.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            items(trips, key = { it.id }) { plan ->
                PlannerPanelCard {
                    Row(verticalAlignment = Alignment.Top) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(plan.title, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(plan.destination, color = HighAltitudeCyan, fontWeight = FontWeight.Medium)
                            val modeNames = plan.activityTypesCsv.split(",").filter { it.isNotBlank() }.joinToString(" + ") { name -> ActivityType.values().firstOrNull { it.name == name }?.displayName ?: name }
                            Text("${plan.durationDays} day(s) · $modeNames", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                            Text("Status: ${plan.status.lowercase()} · Private offline plan", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                        }
                        IconButton(onClick = { scope.launch { plannerRepository.deleteTrip(plan.id) } }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete saved trip", tint = Color(0xFFEF4444))
                        }
                    }
                    val stopTitles = runCatching {
                        val array = JSONArray(plan.itineraryStopsJson)
                        (0 until array.length()).mapNotNull { i -> array.optJSONObject(i)?.optString("title")?.takeIf { it.isNotBlank() } }
                    }.getOrDefault(emptyList())
                    if (stopTitles.isNotEmpty()) {
                        Text("Stops: " + stopTitles.take(5).joinToString(" → ") + if (stopTitles.size > 5) " → +${stopTitles.size - 5} more" else "", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    }
                    val checkedCount = runCatching {
                        val array = JSONArray(plan.prepChecklistJson)
                        (0 until array.length()).count { i -> array.optJSONObject(i)?.optBoolean("checked") == true }
                    }.getOrDefault(0)
                    Text("Preparation: $checkedCount/${preparationItems.size} complete", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                    if (plan.checkInEnabled) {
                        Text("Check-in interval: every ${plan.checkInIntervalMinutes} minutes", color = AmberGold, style = MaterialTheme.typography.labelSmall)
                        plan.lastCheckInAtMillis?.let { last ->
                            val display = runCatching { SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(last)) }.getOrDefault("recorded")
                            Text("Last local check-in: $display", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                        }
                        OutlinedButton(onClick = {
                            scope.launch {
                                plannerRepository.recordCheckIn(plan.id)
                                AdventureFunnelAnalytics.log(context, "safety_check_in_completed", mapOf("check_in_interval_minutes" to plan.checkInIntervalMinutes))
                                refreshMetrics()
                                Toast.makeText(context, "Check-in recorded on this device.", Toast.LENGTH_LONG).show()
                            }
                        }, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Default.SafetyCheck, contentDescription = null, tint = AmberGold)
                            Spacer(Modifier.width(6.dp))
                            Text("Check in now", color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                    OutlinedButton(
                        onClick = { exportTripPack(plan); exportLauncher.launch(plan.title.replace(Regex("[^A-Za-z0-9_-]"), "_").take(48) + "-trip-pack.json") },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, tint = HighAltitudeCyan)
                        Spacer(Modifier.width(6.dp))
                        Text("Export trip data pack (JSON)", color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }

            // Community Guides & Events Drafts
            item(key = "creator-title") { PlannerSectionTitle("Community guides & events", "Draft workspace") }
            item(key = "creator-tools") {
                PlannerPanelCard {
                    Text(
                        "Prepare a route guide or group event draft.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { guideDialog = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = HighAltitudeCyan),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.Verified, contentDescription = null, tint = Color.White)
                            Spacer(Modifier.width(4.dp))
                            Text("Draft a guide", color = Color.White)
                        }
                        OutlinedButton(
                            onClick = { eventDialog = true },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.Event, contentDescription = null, tint = AmberGold)
                            Spacer(Modifier.width(4.dp))
                            Text("Draft an event", color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                    if (creatorDrafts.isEmpty()) {
                        Text("No local guide or event drafts yet.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    }
                    creatorDrafts.forEach { draft ->
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(draft.title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                                Text("${draft.kind} · ${draft.activityType} · ${draft.destination}", color = HighAltitudeCyan, style = MaterialTheme.typography.labelSmall)
                                Text(draft.status.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }, color = AmberGold, style = MaterialTheme.typography.labelSmall)
                                if (draft.description.isNotBlank()) Text(draft.description, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                                if (draft.proposedPriceCad != null) Text("Suggested price: CAD " + "%.2f".format(Locale.CANADA, draft.proposedPriceCad), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                                if (draft.dateLabel.isNotBlank()) Text("Proposed timing: " + draft.dateLabel, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                    TextButton(onClick = { scope.launch { plannerRepository.deleteCreatorDraft(draft.id) } }) { Text("Delete draft", color = Color(0xFFEF4444)) }
                                }
                            }
                        }
                    }
                }
            }

            // Funnel Metrics
            item(key = "analytics-title") { PlannerSectionTitle("Product experiment funnel", "Local Metrics") }
            item(key = "analytics-card") {
                PlannerPanelCard {
                    listOf(
                        "adventure_app_open" to "App opens",
                        "trip_plan_created" to "Trip plans saved",
                        "activity_started" to "Activities started",
                        "story_exported" to "Stories exported",
                        "story_shared" to "Stories shared",
                    ).forEach { (event, title) ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(title, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                            Text((localMetrics[event] ?: 0).toString(), color = AmberGold, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (guideDialog) {
        AlertDialog(
            onDismissRequest = { guideDialog = false },
            title = { Text("Create a route guide draft") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(guideTitle, { guideTitle = it.take(100) }, label = { Text("Guide title") }, singleLine = true)
                    OutlinedTextField(guideDestination, { guideDestination = it.take(120) }, label = { Text("Destination / route") }, singleLine = true)
                    OutlinedTextField(guideDescription, { guideDescription = it.take(1000) }, label = { Text("What should adventurers know?") }, minLines = 3)
                    OutlinedTextField(guidePrice, { guidePrice = it.filter { c -> c.isDigit() || c == '.' }.take(10) }, label = { Text("Suggested price CAD (optional)") }, singleLine = true)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (guideTitle.isBlank() || guideDestination.isBlank()) Toast.makeText(context, "Add a guide title and destination.", Toast.LENGTH_SHORT).show()
                    else scope.launch {
                        plannerRepository.saveCreatorDraft(AdventureCreatorDraftEntity(
                            kind = "GUIDE", title = guideTitle.trim(), destination = guideDestination.trim(),
                            activityType = activityMode.displayName, description = guideDescription.trim(),
                            proposedPriceCad = guidePrice.toDoubleOrNull()?.takeIf { it >= 0.0 },
                        ))
                        AdventureFunnelAnalytics.log(context, "creator_guide_draft_created", mapOf("activity_type" to activityMode.name))
                        refreshMetrics()
                        guideTitle = ""; guideDestination = ""; guideDescription = ""; guidePrice = ""
                        guideDialog = false
                    }
                }) { Text("Save local draft") }
            },
            dismissButton = { TextButton(onClick = { guideDialog = false }) { Text("Cancel") } },
        )
    }

    if (eventDialog) {
        AlertDialog(
            onDismissRequest = { eventDialog = false },
            title = { Text("Draft a community event") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(eventTitle, { eventTitle = it.take(100) }, label = { Text("Event title") }, singleLine = true)
                    OutlinedTextField(eventDestination, { eventDestination = it.take(120) }, label = { Text("Meetup area / destination") }, singleLine = true)
                    OutlinedTextField(eventDate, { eventDate = it.take(100) }, label = { Text("Proposed date / time") }, singleLine = true)
                    OutlinedTextField(eventDescription, { eventDescription = it.take(1000) }, label = { Text("Plan, difficulty, required gear") }, minLines = 3)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (eventTitle.isBlank() || eventDestination.isBlank()) Toast.makeText(context, "Add an event title and meetup area.", Toast.LENGTH_SHORT).show()
                    else scope.launch {
                        plannerRepository.saveCreatorDraft(AdventureCreatorDraftEntity(
                            kind = "EVENT", title = eventTitle.trim(), destination = eventDestination.trim(),
                            activityType = activityMode.displayName, description = eventDescription.trim(), dateLabel = eventDate.trim(),
                        ))
                        AdventureFunnelAnalytics.log(context, "community_event_draft_created", mapOf("activity_type" to activityMode.name))
                        refreshMetrics()
                        eventTitle = ""; eventDestination = ""; eventDate = ""; eventDescription = ""
                        eventDialog = false
                    }
                }) { Text("Save local draft") }
            },
            dismissButton = { TextButton(onClick = { eventDialog = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun BudgetPill(title: String, amount: Double, accent: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            "$${"%.0f".format(amount)}",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = accent
        )
    }
}

@Composable
private fun PlannerSectionTitle(title: String, meta: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            title,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        Text(meta, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun PlannerPanelCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline)
        ),
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
    }
}

@Composable
private fun plannerFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
    focusedBorderColor = AdventureOrange,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
    focusedLabelColor = AdventureOrange,
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    cursorColor = AdventureOrange,
)
