package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdventureFeatureOrganizerSheet(
    onDismiss: () -> Unit,
    onNavigate: (Screen) -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }
    var selectedCategoryName by rememberSaveable { mutableStateOf(ModuleCategory.ALL.name) }
    val selectedCategory = ModuleCategory.values().firstOrNull { it.name == selectedCategoryName }
        ?: ModuleCategory.ALL
    val normalizedQuery = query.trim().lowercase()
    val filteredFeatures = AdventureFeatureCatalog.all.filter { feature ->
        val categoryMatches = selectedCategory == ModuleCategory.ALL || feature.category == selectedCategory
        val queryMatches = normalizedQuery.isBlank() ||
            feature.title.lowercase().contains(normalizedQuery) ||
            feature.subtitle.lowercase().contains(normalizedQuery) ||
            feature.category.label.lowercase().contains(normalizedQuery)
        categoryMatches && queryMatches
    }
    val groupedFeatures = AdventureFeatureCatalog.categories.mapNotNull { category ->
        val features = filteredFeatures.filter { it.category == category }
        if (features.isEmpty()) null else category to features
    }

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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "ADVENTURER DIRECTORY",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = TrailOrange
                    )
                    Text(
                        text = "Find the right tool for your next step",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "14 destinations · 5 main tabs · 5 task groups",
                        style = MaterialTheme.typography.bodySmall,
                        color = FieldMuted
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(36.dp).testTag("close_feature_organizer_button")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = FieldMuted)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = query,
                onValueChange = { query = it.take(80) },
                modifier = Modifier.fillMaxWidth().testTag("feature_directory_search"),
                singleLine = true,
                label = { Text("Search all tools") },
                placeholder = { Text("Try “voice”, “safety” or “story”") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotBlank()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear search")
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TrailOrange,
                    focusedLabelColor = TrailOrange,
                    cursorColor = TrailOrange
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ModuleCategory.values().forEach { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategoryName = category.name },
                        label = { Text(category.label, maxLines = 1) },
                        modifier = Modifier.testTag("directory_filter_${category.name.lowercase()}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (filteredFeatures.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.SearchOff, contentDescription = null, tint = FieldMuted, modifier = Modifier.size(30.dp))
                    Text("No matching tools", color = Color.White, fontWeight = FontWeight.Bold)
                    Text("Try another term or clear the selected category.", color = FieldMuted, style = MaterialTheme.typography.bodySmall)
                    TextButton(onClick = {
                        query = ""
                        selectedCategoryName = ModuleCategory.ALL.name
                    }) { Text("Clear filters", color = TrailOrange) }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 430.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    groupedFeatures.forEach { (category, features) ->
                        item(key = "group_${category.name}") {
                            OrganizerGroupSection(
                                title = category.label,
                                count = features.size,
                                features = features,
                                onNavigate = onNavigate
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "MAIN TAB marks one of the five bottom-navigation destinations. Tools open when needed. Live data, social delivery and premium capabilities may depend on permissions, account setup or connected services.",
                style = MaterialTheme.typography.bodySmall,
                color = FieldMuted
            )
        }
    }
}

@Composable
private fun OrganizerGroupSection(
    title: String,
    count: Int,
    features: List<QuickActionItem>,
    onNavigate: (Screen) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = FieldMuted
            )
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = TrailOrange
            )
        }

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
                        .heightIn(min = 58.dp)
                        .testTag("organizer_nav_${feature.screen.name.lowercase()}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(9.dp),
                            color = feature.accentColor.copy(alpha = 0.15f),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    feature.icon,
                                    contentDescription = null,
                                    tint = feature.accentColor,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = feature.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 2,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                            Text(
                                text = feature.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = FieldMuted,
                                maxLines = 2,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (feature.isPrimaryDestination) feature.accentColor.copy(alpha = 0.16f) else SlatePanel
                        ) {
                            Text(
                                text = if (feature.isPrimaryDestination) "TAB" else "TOOL",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (feature.isPrimaryDestination) feature.accentColor else FieldMuted
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
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
