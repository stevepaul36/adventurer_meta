package com.example.ui.components

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.model.Comment
import com.example.data.model.Post
import com.example.data.model.TripBudgetEstimate
import com.example.ui.theme.AdventureOrange
import com.example.ui.theme.AmberGold
import com.example.ui.theme.BorderNavy
import com.example.ui.theme.DangerRed
import com.example.ui.theme.FieldMuted
import com.example.ui.theme.FieldNavy
import com.example.ui.theme.HighAltitudeCyan
import com.example.ui.theme.SafeMint
import com.example.ui.theme.SlatePanel
import com.example.ui.theme.SlatePanelAlt
import com.example.ui.theme.TrailOrange
import com.example.viewmodel.Screen

@Composable
fun CreatePostDialog(
    onDismiss: () -> Unit,
    onSubmit: (caption: String, imageUrl: String?) -> Unit
) {
    var caption by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Create Adventure Post", fontWeight = FontWeight.Bold, color = Color.White)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = caption,
                    onValueChange = { caption = it },
                    label = { Text("What happened on the trail?") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("post_caption_input"),
                    minLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AdventureOrange,
                        unfocusedBorderColor = Color(0xFF334155)
                    )
                )

                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    label = { Text("Photo URL (Optional)") },
                    placeholder = { Text("https://...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("post_image_url_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AdventureOrange,
                        unfocusedBorderColor = Color(0xFF334155)
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (caption.isNotBlank()) {
                        onSubmit(caption, if (imageUrl.isBlank()) null else imageUrl)
                    }
                },
                enabled = caption.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = AdventureOrange),
                modifier = Modifier.testTag("submit_post_button")
            ) {
                Text("Publish", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF94A3B8))
            }
        },
        containerColor = Color(0xFF131B2A),
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun CommentsBottomSheet(
    comments: List<Comment>,
    onDismiss: () -> Unit,
    onAddComment: (String) -> Unit
) {
    var newCommentText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Trail Comments (${comments.size})", fontWeight = FontWeight.Bold, color = Color.White)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 340.dp)
            ) {
                if (comments.isEmpty()) {
                    Text(
                        text = "No comments yet. Start the conversation!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(comments) { comment ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF1E293B), RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                AsyncImage(
                                    model = comment.user.avatarUrl,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = comment.user.name,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = AmberGold
                                    )
                                    Text(
                                        text = comment.text,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = newCommentText,
                        onValueChange = { newCommentText = it },
                        placeholder = { Text("Write a comment...") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("comment_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = AdventureOrange,
                            unfocusedBorderColor = Color(0xFF334155)
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (newCommentText.isNotBlank()) {
                                onAddComment(newCommentText)
                                newCommentText = ""
                            }
                        },
                        modifier = Modifier.testTag("send_comment_button")
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send", tint = AdventureOrange)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = Color(0xFF94A3B8))
            }
        },
        containerColor = Color(0xFF131B2A),
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun TripBudgetModal(
    distanceKm: Int,
    onDismiss: () -> Unit
) {
    var days by remember { mutableStateOf(8) }
    var mileage by remember { mutableStateOf(28.0) }
    var stayPerNight by remember { mutableStateOf(1800.0) }
    val estimate = remember(distanceKm, days, mileage, stayPerNight) {
        TripBudgetEstimate(
            distanceKm = distanceKm,
            bikeMileageKmpl = mileage,
            stayPerNightInr = stayPerNight,
            daysCount = days
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CurrencyRupee, contentDescription = null, tint = AmberGold)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Expedition Budget Calculator", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Total Expedition Route: $distanceKm km",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = HighAltitudeCyan
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Duration (Days): $days", color = Color.White, style = MaterialTheme.typography.bodyMedium)
                    Row {
                        IconButton(onClick = { if (days > 2) days-- }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = Color.White)
                        }
                        IconButton(onClick = { if (days < 30) days++ }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Add, contentDescription = "Increase", tint = Color.White)
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF1E293B),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        BudgetLineItem("Fuel (${estimate.totalFuelLiters.toInt()} L @ ₹104/L)", "₹${estimate.totalFuelCostInr.toInt()}")
                        BudgetLineItem("Stay ($days nights @ ₹${stayPerNight.toInt()})", "₹${estimate.totalStayCostInr.toInt()}")
                        BudgetLineItem("Food & Hydration ($days days)", "₹${estimate.totalFoodCostInr.toInt()}")
                        BudgetLineItem("Inner Line & Wildlife Permits", "₹${estimate.permitCostInr.toInt()}")
                        BudgetLineItem("Emergency Spares & Medical Buffer", "₹${estimate.sparePartsBufferInr.toInt()}")
                        Divider(color = Color(0xFF334155), modifier = Modifier.padding(vertical = 4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Estimated Total", fontWeight = FontWeight.Bold, color = AmberGold)
                            Text("₹${estimate.totalEstimateInr.toInt()}", fontWeight = FontWeight.Bold, color = AmberGold, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = AmberGold)
            ) {
                Text("Done", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = Color(0xFF131B2A),
        shape = RoundedCornerShape(16.dp)
    )
}

/**
 * Emergency sheet. Honest by design: the app does not alert anyone by itself, so every action
 * hands off to the dialer or share sheet, and the rider's real location is shown when known.
 */
@Composable
fun SafetySosDialog(
    onDismiss: () -> Unit,
    locationManager: com.example.data.location.LocationManager? = null,
    liveLatitude: Double? = null,
    liveLongitude: Double? = null,
) {
    val context = LocalContext.current
    var lat by remember { mutableStateOf(liveLatitude) }
    var lon by remember { mutableStateOf(liveLongitude) }
    var locating by remember { mutableStateOf(lat == null && locationManager?.hasLocationPermission() == true) }
    LaunchedEffect(Unit) {
        if (lat == null && locationManager?.hasLocationPermission() == true) {
            locationManager.getLastKnownLocation { loc ->
                if (loc != null && (loc.latitude != 0.0 || loc.longitude != 0.0)) { lat = loc.latitude; lon = loc.longitude }
                locating = false
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Sos, contentDescription = null, tint = DangerRed, modifier = Modifier.size(36.dp)) },
        title = { Text("Emergency help", fontWeight = FontWeight.Bold, color = Color.White) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Adventurer can't contact anyone for you. Call for help or send your location to someone you trust.",
                    style = MaterialTheme.typography.bodySmall, color = Color(0xFFCBD5E1)
                )
                Button(
                    onClick = { EmergencyActions.dial(context, "112") },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().height(56.dp).testTag("sos_call_112")
                ) {
                    Icon(Icons.Default.Call, contentDescription = null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("Call 112 · Emergency", color = Color.White, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = { EmergencyActions.dial(context, "108") },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Icon(Icons.Default.LocalHospital, contentDescription = null, tint = DangerRed)
                    Spacer(Modifier.width(8.dp))
                    Text("Call 108 · Ambulance", color = Color.White, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = { EmergencyActions.shareLocation(context, lat, lon) },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().height(52.dp).testTag("sos_share_location")
                ) {
                    Icon(Icons.Default.ShareLocation, contentDescription = null, tint = AmberGold)
                    Spacer(Modifier.width(8.dp))
                    Text("Share my location", color = Color.White, fontWeight = FontWeight.Bold)
                }
                Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFF1E293B), modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(if (lat != null) Icons.Default.GpsFixed else Icons.Default.LocationOff, contentDescription = null, tint = if (lat != null) Color(0xFF10B981) else Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            when {
                                lat != null && lon != null -> "Your location: %.5f, %.5f".format(lat, lon)
                                locating -> "Finding your location…"
                                else -> "Location unavailable — turn on location to include it."
                            },
                            style = MaterialTheme.typography.labelMedium, color = Color.White
                        )
                    }
                }
                Text("112 and 108 work in India. Elsewhere, dial your local emergency number.", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close", color = Color(0xFF94A3B8)) } },
        containerColor = Color(0xFF131B2A),
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun BudgetLineItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
        Text(value, style = MaterialTheme.typography.bodySmall, color = Color.White, fontWeight = FontWeight.SemiBold)
    }
}

private data class OrganizerFeature(
    val screen: Screen,
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val accentColor: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdventureFeatureOrganizerSheet(
    onDismiss: () -> Unit,
    onNavigate: (Screen) -> Unit
) {
    val operationalGroup = listOf(
        OrganizerFeature(Screen.FEED, "Basecamp Launchpad", "Stories, active shorts reel & community feed", Icons.Default.Home, TrailOrange),
        OrganizerFeature(Screen.JOURNEY, "Journey Command Center", "Nearby POIs, food cart simulation, fuel calc & voice dock", Icons.Default.Explore, TrailOrange),
        OrganizerFeature(Screen.LIVE_TRACKER, "Field Mode (HUD)", "4-stage GPS telemetry, breadcrumb & check-in plan", Icons.Default.Speed, SafeMint),
        OrganizerFeature(Screen.ROUTE_BRIEF, "Route Brief & Checklist", "74 km route, 8-point checklist & elevation graph", Icons.Default.Checklist, SafeMint),
        OrganizerFeature(Screen.VOICE_COMPANION, "Voice Companion", "Hands-free realtime Gemini audio travel guide", Icons.Default.Mic, Color(0xFF818CF8))
    )

    val explorationGroup = listOf(
        OrganizerFeature(Screen.EXPLORE_PLAN, "Field Atlas", "Map candidates, gear packing list & regional catalog", Icons.Default.Map, TrailOrange),
        OrganizerFeature(Screen.EXPEDITIONS, "India Expeditions", "Curated routes across Ladakh, Spiti & Western Ghats", Icons.Default.Terrain, HighAltitudeCyan),
        OrganizerFeature(Screen.RADAR, "Roadside Radar & SOS", "Live roadside assistance map & distress beacons", Icons.Default.Radar, DangerRed)
    )

    val storyGroup = listOf(
        OrganizerFeature(Screen.STORY_STUDIO, "AI Story Studio", "Multimodal story templates, reels & audio narration", Icons.Default.AutoAwesome, Color(0xFFC084FC)),
        OrganizerFeature(Screen.FINAL_REVIEW, "Final Review", "3-point privacy safety gating check before posting", Icons.Default.VerifiedUser, SafeMint)
    )

    val communityGroup = listOf(
        OrganizerFeature(Screen.CLUBS, "Riding Clubs", "Active chapters, meetups & scheduled group rides", Icons.Default.Groups, Color(0xFF38BDF8)),
        OrganizerFeature(Screen.GAMIFICATION, "Gamification Hub", "Rider XP levels, streaks & 16 unlockable badges", Icons.Default.EmojiEvents, AmberGold),
        OrganizerFeature(Screen.PROFILE, "Trail Settings (\"Me\")", "Multi-activity identity, privacy check-up & offline cache", Icons.Default.Settings, AmberGold),
        OrganizerFeature(Screen.PRO_PAYWALL, "Adventurer Pro", "Offline 3D topo maps, satellite beacon sync & unlimited AI", Icons.Default.WorkspacePremium, Color(0xFFFB7185))
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SlatePanel,
        scrimColor = Color.Black.copy(alpha = 0.65f),
        dragHandle = { BottomSheetDefaults.DragHandle(color = BorderNavy) },
        modifier = Modifier.testTag("feature_organizer_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .padding(bottom = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ADVENTURE MODULES DIRECTORY",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = TrailOrange
                    )
                    Text(
                        text = "All 14 features neatly organized by domain",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(36.dp).testTag("close_feature_organizer_button")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = FieldMuted)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LazyColumn(
                modifier = Modifier.fillMaxWidth().heightIn(max = 520.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    OrganizerGroupSection("🧭 TRIP & FIELD OPERATIONS", operationalGroup, onNavigate)
                }
                item {
                    OrganizerGroupSection("🗺️ EXPLORATION & SAFETY", explorationGroup, onNavigate)
                }
                item {
                    OrganizerGroupSection("🎬 STORY & SHARING", storyGroup, onNavigate)
                }
                item {
                    OrganizerGroupSection("👥 COMMUNITY & PROGRESSION", communityGroup, onNavigate)
                }
            }
        }
    }
}

@Composable
private fun OrganizerGroupSection(
    title: String,
    features: List<OrganizerFeature>,
    onNavigate: (Screen) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = FieldMuted,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SlatePanelAlt, RoundedCornerShape(12.dp))
                .padding(vertical = 4.dp)
        ) {
            features.forEachIndexed { index, feature ->
                Surface(
                    onClick = { onNavigate(feature.screen) },
                    color = Color.Transparent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp)
                        .testTag("organizer_nav_${feature.screen.name.lowercase()}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = feature.accentColor.copy(alpha = 0.15f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    feature.icon,
                                    contentDescription = null,
                                    tint = feature.accentColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = feature.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = feature.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = FieldMuted,
                                maxLines = 1
                            )
                        }

                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = BorderNavy,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                if (index < features.size - 1) {
                    Divider(color = BorderNavy.copy(alpha = 0.4f), modifier = Modifier.padding(horizontal = 12.dp))
                }
            }
        }
    }
}

