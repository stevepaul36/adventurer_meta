package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.model.ActivityType
import com.example.ui.theme.AdventureOrange
import com.example.ui.theme.AmberGold
import com.example.ui.theme.HighAltitudeCyan
import com.example.viewmodel.AdventurerViewModel
import com.example.viewmodel.SupportedLang
import com.example.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: AdventurerViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler { viewModel.navigateBack() }

    val user = viewModel.currentUser
    val gamification by viewModel.gamification.collectAsState()
    val activityMode by viewModel.activityMode.collectAsState()
    val currentLang by viewModel.currentLang.collectAsState()
    var showActivitySettings by rememberSaveable { mutableStateOf(false) }
    var showLanguageSettings by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Rider Profile & Garage", fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Optional personalisation · not required to start a trip", style = MaterialTheme.typography.labelSmall, color = AmberGold)
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
            // Profile Header Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2A)),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(
                            listOf(AdventureOrange.copy(alpha = 0.6f), AmberGold.copy(alpha = 0.3f))
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
                                .size(90.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(user.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Sample profile and trip stats · you can personalize later", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
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
                            color = Color(0xFF1E293B),
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
                                ProfileStatCol("ASCENT", "${gamification.totalElevationM} m")
                                ProfileStatCol("STREAK", "${gamification.currentStreakDays} days")
                            }
                        }
                    }
                }
            }

            // Keep safety discoverable after moving SOS/help out of the primary bottom navigation.
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("profile_safety_help_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF20191B)),
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
                                "Roadside options, safety guidance and emergency-call entry points. Availability depends on device and region.",
                                color = Color(0xFFD9C8C6),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        OutlinedButton(onClick = { viewModel.navigateTo(Screen.RADAR) }) {
                            Text("Open", color = Color(0xFFFFB4A6))
                        }
                    }
                }
            }

            // Monetization entry point. Product prices come from Google Play via RevenueCat.
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("profile_pro_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A2E)),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(
                            listOf(AdventureOrange.copy(alpha = 0.85f), AmberGold.copy(alpha = 0.65f))
                        )
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = AmberGold, modifier = Modifier.size(28.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Adventurer Pro", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text(
                                "AI highlight discovery and AI-generated ride captions. Manage or restore your subscription anytime.",
                                color = Color(0xFFCBD5E1),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Button(
                            onClick = { viewModel.navigateTo(Screen.PRO_PAYWALL) },
                            colors = ButtonDefaults.buttonColors(containerColor = AdventureOrange)
                        ) {
                            Text("Explore", color = Color.Black)
                        }
                    }
                }
            }

            // Rider Garage Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2A)),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF334155))
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.TwoWheeler, contentDescription = null, tint = AdventureOrange)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Example vehicle setup", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(user.bikeModel, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = AmberGold)
                        Text("Replace these example specifications before relying on vehicle-specific estimates.", style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "• Displacement: 452cc Sherpa DOHC Liquid-Cooled\n• Power / Torque: 40.02 PS / 40 Nm @ 5500 RPM\n• Suspension: 43mm USD Showa Forks (200mm travel)\n• Gear: Cross-spoke tubeless wheels, aluminum engine guard, 45L hard panniers, Garmin GPS bar mount",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFCBD5E1)
                        )
                    }
                }
            }

            // Optional activity preferences are collapsed until the user chooses to personalize.
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2A)),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF334155))
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("Preferred adventure mode", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                                Text(activityMode.displayName, style = MaterialTheme.typography.bodySmall, color = AmberGold)
                            }
                            TextButton(onClick = { showActivitySettings = !showActivitySettings }) {
                                Text(if (showActivitySettings) "Done" else "Change")
                            }
                        }
                        if (showActivitySettings) {
                            Text(
                                "Optional. You can change this anytime; specialist trail, regulation and dive-computer feeds are not connected yet.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF94A3B8)
                            )
                            ActivityType.values().forEach { mode ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(mode.displayName, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                                    RadioButton(
                                        selected = activityMode == mode,
                                        onClick = { viewModel.setActivityMode(mode) },
                                        colors = RadioButtonDefaults.colors(selectedColor = AdventureOrange)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Language is optional too: show the current choice and expand only on request.
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2A)),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF334155))
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("App language", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("${currentLang.displayName} (${currentLang.localName})", style = MaterialTheme.typography.bodySmall, color = AmberGold)
                            }
                            TextButton(onClick = { showLanguageSettings = !showLanguageSettings }) {
                                Text(if (showLanguageSettings) "Done" else "Change")
                            }
                        }
                        if (showLanguageSettings) {
                            SupportedLang.values().forEach { lang ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("${lang.displayName} (${lang.localName})", color = Color.White, style = MaterialTheme.typography.bodyMedium)
                                    RadioButton(
                                        selected = currentLang == lang,
                                        onClick = { viewModel.setLanguage(lang) },
                                        colors = RadioButtonDefaults.colors(selectedColor = AmberGold)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileStatCol(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
    }
}
