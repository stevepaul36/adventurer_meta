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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Rider Profile & Garage", fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Motorcycle Specs & Telemetry History", style = MaterialTheme.typography.labelSmall, color = AmberGold)
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
                            Text("Primary Expedition Steed", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(user.bikeModel, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = AmberGold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "• Displacement: 452cc Sherpa DOHC Liquid-Cooled\n• Power / Torque: 40.02 PS / 40 Nm @ 5500 RPM\n• Suspension: 43mm USD Showa Forks (200mm travel)\n• Gear: Cross-spoke tubeless wheels, aluminum engine guard, 45L hard panniers, Garmin GPS bar mount",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFCBD5E1)
                        )
                    }
                }
            }

            // Activity Mode Preferences
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
                        Text("Active Tracking Mode", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(modifier = Modifier.height(10.dp))
                        ActivityType.values().forEach { mode ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
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

            // Language Selector Card
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
                        Text("App Language", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(modifier = Modifier.height(10.dp))
                        SupportedLang.values().forEach { lang ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
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

@Composable
private fun ProfileStatCol(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
    }
}
