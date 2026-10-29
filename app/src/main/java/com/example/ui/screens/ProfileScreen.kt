package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.model.ActivityType
import com.example.data.market.AdventureMarket
import com.example.data.market.AdventureMarketSettings
import com.example.ui.theme.*
import com.example.viewmodel.AdventurerViewModel
import com.example.viewmodel.Screen
import com.example.viewmodel.SupportedLang

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: AdventurerViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler { viewModel.navigateBack() }

    val context = LocalContext.current
    val user = viewModel.currentUser
    val gamification by viewModel.gamification.collectAsState()
    val activityMode by viewModel.activityMode.collectAsState()
    val currentLang by viewModel.currentLang.collectAsState()
    val marketContext by AdventureMarketSettings.state.collectAsState()
    LaunchedEffect(context) { AdventureMarketSettings.initialize(context) }
    var marketMenuExpanded by rememberSaveable { mutableStateOf(false) }
    var regionMenuExpanded by rememberSaveable { mutableStateOf(false) }

    var showActivitySettings by rememberSaveable { mutableStateOf(false) }
    var showLanguageSettings by rememberSaveable { mutableStateOf(false) }

    // Screen 07: Trail Settings State Tokens
    var voiceGuideExplicit by rememberSaveable { mutableStateOf(false) }
    var quietHours by rememberSaveable { mutableStateOf(true) }
    var keepHistoryPrivate by rememberSaveable { mutableStateOf(true) }
    var selectedMood by rememberSaveable { mutableStateOf("Useful") }
    var showPrivacyDialog by rememberSaveable { mutableStateOf(false) }

    val selectedActivities = remember {
        mutableStateListOf(ActivityType.Motorcycling, ActivityType.Cycling, ActivityType.Trekking)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "TRAIL SETTINGS",
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = AmberGold.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    "07 ME",
                                    color = AmberGold,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            "Preferences shape utility and calm",
                            style = MaterialTheme.typography.labelSmall,
                            color = FieldMuted
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SlatePanel)
            )
        },
        containerColor = FieldNavy,
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile & Rider Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SlatePanel),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(
                            listOf(TrailOrange.copy(alpha = 0.6f), AmberGold.copy(alpha = 0.3f))
                        )
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AsyncImage(
                            model = user.avatarUrl,
                            contentDescription = user.name,
                            modifier = Modifier
                                .size(84.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(user.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("📍 ${user.location} • Level ${gamification.level} Trail Master", style = MaterialTheme.typography.labelMedium, color = AmberGold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "“Two wheels move the soul. Chasing Himalayan snow passes and Sahyadri monsoon ridges.”",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFCBD5E1),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Lifetime Stats
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SlatePanelAlt,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                ProfileStatCol("RIDES", "${gamification.totalRides}")
                                ProfileStatCol("DISTANCE", "${gamification.totalDistanceKm.toInt()} km")
                                ProfileStatCol("ELEVATION", "+${gamification.totalElevationM} m")
                                ProfileStatCol("XP", "${gamification.currentXp}")
                            }
                        }
                    }
                }
            }

            // Keep roadside assistance discoverable with one direct path from Profile.
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("profile_safety_help_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SlatePanel),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF7F3B35))
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.HealthAndSafety, contentDescription = null, tint = Color(0xFFFF8B75), modifier = Modifier.size(26.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Safety & Help", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text(
                                "Roadside options and emergency-call entry points. Availability depends on device and region.",
                                color = FieldMuted,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        OutlinedButton(onClick = { viewModel.navigateTo(Screen.RADAR) }) {
                            Text("Open", color = Color(0xFFFFB4A6))
                        }
                    }
                }
            }

            // User-editable market defaults are preferences, not proof of legal permission.
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("profile_market_context_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SlatePanel),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF315A55))
                    )
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Public, contentDescription = null, tint = SafeMint, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("MARKET & POLICY CONTEXT", color = SafeMint, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                                Text("Regional defaults for trip actions", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            }
                        }
                        Text(
                            "Your photos and videos use the same viewer in every market. Regional providers and activity guidance can differ by destination.",
                            color = FieldMuted,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedButton(
                                    modifier = Modifier.fillMaxWidth().testTag("market_country_selector"),
                                    onClick = { marketMenuExpanded = true }
                                ) {
                                    Text(marketContext.market.label, maxLines = 1)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Default.ExpandMore, contentDescription = "Choose market")
                                }
                                DropdownMenu(
                                    expanded = marketMenuExpanded,
                                    onDismissRequest = { marketMenuExpanded = false }
                                ) {
                                    AdventureMarket.entries.forEach { market ->
                                        DropdownMenuItem(
                                            text = { Text(market.label) },
                                            onClick = {
                                                AdventureMarketSettings.setMarket(context, market)
                                                marketMenuExpanded = false
                                                regionMenuExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                            Box(modifier = Modifier.weight(1.2f)) {
                                OutlinedButton(
                                    modifier = Modifier.fillMaxWidth().testTag("market_region_selector"),
                                    onClick = { regionMenuExpanded = true }
                                ) {
                                    Text(marketContext.region, maxLines = 1, softWrap = false)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Default.ExpandMore, contentDescription = "Choose region")
                                }
                                DropdownMenu(
                                    expanded = regionMenuExpanded,
                                    onDismissRequest = { regionMenuExpanded = false }
                                ) {
                                    marketContext.market.regions.forEach { region ->
                                        DropdownMenuItem(
                                            text = { Text(region) },
                                            onClick = {
                                                AdventureMarketSettings.setRegion(context, region)
                                                regionMenuExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = SlatePanelAlt,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    "Defaults: ${marketContext.market.defaultsLabel}",
                                    color = AmberGold,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelMedium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Policy status: not verified live. Rules can depend on the exact destination, land manager, activity, season and eligibility. Confirm requirements with the official source before acting.",
                                    color = FieldMuted,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }

            // Quick Modules & Progression Launcher
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("profile_modules_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SlatePanel),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(BorderNavy)
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "PROGRESSION & COMMUNITY",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = AmberGold
                            )
                            TextButton(
                                onClick = { viewModel.setShowFeatureOrganizer(true) },
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                            ) {
                                Text("All 14 Modules →", fontSize = 11.sp, color = SafeMint, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                onClick = { viewModel.navigateTo(Screen.GAMIFICATION) },
                                shape = RoundedCornerShape(10.dp),
                                color = SlatePanelAlt,
                                modifier = Modifier.weight(1f).testTag("profile_nav_gamification")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = AmberGold, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text("Trophies", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color.White)
                                        Text("16 Badges", style = MaterialTheme.typography.labelSmall, color = FieldMuted, fontSize = 9.sp)
                                    }
                                }
                            }

                            Surface(
                                onClick = { viewModel.navigateTo(Screen.CLUBS) },
                                shape = RoundedCornerShape(10.dp),
                                color = SlatePanelAlt,
                                modifier = Modifier.weight(1f).testTag("profile_nav_clubs")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Groups, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text("Clubs", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color.White)
                                        Text("4 Chapters", style = MaterialTheme.typography.labelSmall, color = FieldMuted, fontSize = 9.sp)
                                    }
                                }
                            }

                            Surface(
                                onClick = { viewModel.navigateTo(Screen.PRO_PAYWALL) },
                                shape = RoundedCornerShape(10.dp),
                                color = SlatePanelAlt,
                                modifier = Modifier.weight(1f).testTag("profile_nav_pro")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = Color(0xFFFB7185), modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text("Pro", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color.White)
                                        Text("3D Topo", style = MaterialTheme.typography.labelSmall, color = FieldMuted, fontSize = 9.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Screen 07: Multi-Activity Identity
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SlatePanel),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(BorderNavy)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "ADVENTURE MODES",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = TrailOrange
                            )
                            Text(
                                "CHOOSE MORE THAN ONE",
                                style = MaterialTheme.typography.labelSmall,
                                color = FieldMuted,
                                fontSize = 9.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                ActivityType.Motorcycling to "Motorcycle",
                                ActivityType.Cycling to "Cycling",
                                ActivityType.Trekking to "Hiking"
                            ).forEach { (type, label) ->
                                val selected = selectedActivities.contains(type)
                                FilterChip(
                                    selected = selected,
                                    onClick = {
                                        if (selected && selectedActivities.size > 1) selectedActivities.remove(type)
                                        else if (!selected) selectedActivities.add(type)
                                    },
                                    label = { Text(label, fontSize = 11.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = TrailOrange,
                                        selectedLabelColor = FieldNavy,
                                        containerColor = SlatePanelAlt,
                                        labelColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = selected,
                                        borderColor = if (selected) TrailOrange else BorderNavy
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                ActivityType.Camping to "Camping",
                                ActivityType.CarTrips to "Road trips",
                                ActivityType.KayakingPaddling to "Water"
                            ).forEach { (type, label) ->
                                val selected = selectedActivities.contains(type)
                                FilterChip(
                                    selected = selected,
                                    onClick = {
                                        if (selected && selectedActivities.size > 1) selectedActivities.remove(type)
                                        else if (!selected) selectedActivities.add(type)
                                    },
                                    label = { Text(label, fontSize = 11.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = TrailOrange,
                                        selectedLabelColor = FieldNavy,
                                        containerColor = SlatePanelAlt,
                                        labelColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = selected,
                                        borderColor = if (selected) TrailOrange else BorderNavy
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Field & Notification Preferences (Switches)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SlatePanel),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(BorderNavy)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "FIELD & NOTIFICATION PREFERENCES",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = TrailOrange
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        PreferenceSwitchRow(
                            title = "Voice guide",
                            subtitle = "Only speak after explicit start or request.",
                            checked = voiceGuideExplicit,
                            onCheckedChange = { voiceGuideExplicit = it }
                        )

                        HorizontalDivider(color = BorderNavy, modifier = Modifier.padding(vertical = 10.dp))

                        PreferenceSwitchRow(
                            title = "Quiet hours",
                            subtitle = "Pause non-essential suggestions from 10 PM to 7 AM.",
                            checked = quietHours,
                            onCheckedChange = { quietHours = it }
                        )

                        HorizontalDivider(color = BorderNavy, modifier = Modifier.padding(vertical = 10.dp))

                        PreferenceSwitchRow(
                            title = "Keep precise trip history private",
                            subtitle = "New stories do not reveal detailed route by default.",
                            checked = keepHistoryPrivate,
                            onCheckedChange = { keepHistoryPrivate = it }
                        )
                    }
                }
            }

            // Feed Mood Selector
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SlatePanel),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(BorderNavy)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "FEED MOOD",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = TrailOrange
                            )
                            Text(
                                "YOUR CHOICE",
                                style = MaterialTheme.typography.labelSmall,
                                color = FieldMuted,
                                fontSize = 9.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Useful", "Inspiring", "Light & funny", "Quiet").forEach { mood ->
                                val isSelected = selectedMood == mood
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedMood = mood },
                                    label = { Text(mood, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = SafeMint,
                                        selectedLabelColor = FieldNavy,
                                        containerColor = SlatePanelAlt,
                                        labelColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = isSelected,
                                        borderColor = if (isSelected) SafeMint else BorderNavy
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Privacy Check-up Tile
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = SlatePanel,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderNavy),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = SafeMint, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Privacy Check-up",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Review who can see saved trip boards, location permissions, export and account data.",
                            color = FieldMuted,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = { showPrivacyDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Review privacy controls", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Save Preferences Action
            item {
                Button(
                    onClick = {
                        Toast.makeText(context, "Preferences saved in this Adventurer session.", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TrailOrange),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("Save preferences", color = FieldNavy, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }

    // Privacy Controls Dialog Modal
    if (showPrivacyDialog) {
        Dialog(onDismissRequest = { showPrivacyDialog = false }) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = SlatePanel,
                border = androidx.compose.foundation.BorderStroke(1.dp, SafeMint.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = SafeMint)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Privacy Controls", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("• Exact Coordinates: Start/end coordinates are masked by default.", color = Color.White, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("• Route History: Stored device-locally in Room SQL cipher.", color = Color.White, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("• Zero Social Pressure: No follower scoreboards or gamified streaks.", color = Color.White, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("• Audience Gate: Stories require 3-point consent before sharing.", color = Color.White, fontSize = 12.sp)

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { showPrivacyDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = SafeMint),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Close", color = FieldNavy, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun PreferenceSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(subtitle, color = FieldMuted, fontSize = 11.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = FieldNavy,
                checkedTrackColor = SafeMint,
                uncheckedThumbColor = FieldMuted,
                uncheckedTrackColor = SlatePanelAlt
            )
        )
    }
}

@Composable
private fun ProfileStatCol(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = FieldMuted)
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
    }
}
