package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AdventureOrange
import com.example.ui.theme.AmberGold
import com.example.ui.theme.HighAltitudeCyan
import com.example.ui.theme.SuccessGreen

/**
 * Story Bubble data item representing active rider moments on the trail.
 */
data class ExpeditionStoryItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val isLive: Boolean = false,
    val accentColor: Color = AdventureOrange,
    val icon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Default.Terrain
)

/**
 * Modern Instagram / Strava style Expedition Stories strip with glowing animated rings.
 */
@Composable
fun ExpeditionStoriesStrip(
    onAddStoryClick: () -> Unit,
    onStoryClick: (ExpeditionStoryItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val stories = remember {
        listOf(
            ExpeditionStoryItem("khardung", "Khardung La", "17,982 ft", isLive = true, accentColor = AdventureOrange, icon = Icons.Default.Terrain),
            ExpeditionStoryItem("banff", "Banff Ridge", "3,400 m", accentColor = HighAltitudeCyan, icon = Icons.Default.FilterHdr),
            ExpeditionStoryItem("zanskar", "Zanskar Ice", "Cold Alert", accentColor = Color(0xFF8B5CF6), icon = Icons.Default.AcUnit),
            ExpeditionStoryItem("spiti", "Spiti Valley", "Lunar Loop", accentColor = AmberGold, icon = Icons.Default.Explore),
            ExpeditionStoryItem("coastal", "Pacific Coast", "Ocean Hwy", accentColor = SuccessGreen, icon = Icons.Default.Waves)
        )
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "EXPEDITION STORIES",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = AdventureOrange
            )
            Text(
                "Live Pass Updates",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // "Your Story" / Add Story Button
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable { onAddStoryClick() }
                    .testTag("add_story_bubble")
            ) {
                Box(
                    modifier = Modifier
                        .size(62.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.5.dp, MaterialTheme.colorScheme.outline, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AddAPhoto,
                        contentDescription = "Share trail moment",
                        tint = AdventureOrange,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Add Story",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "+ Moment",
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // High Pass Active Expedition Stories
            stories.forEach { story ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { onStoryClick(story) }
                        .testTag("story_bubble_${story.id}")
                ) {
                    Box(
                        modifier = Modifier
                            .size(62.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.sweepGradient(
                                    listOf(
                                        story.accentColor,
                                        AmberGold,
                                        story.accentColor
                                    )
                                )
                            )
                            .padding(2.5.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = story.icon,
                            contentDescription = story.title,
                            tint = story.accentColor,
                            modifier = Modifier.size(26.dp)
                        )
                        if (story.isLive) {
                            Surface(
                                shape = RoundedCornerShape(3.dp),
                                color = Color(0xFFEF4444),
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .offset(y = 4.dp)
                            ) {
                                Text(
                                    text = "LIVE",
                                    fontSize = 7.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 0.5.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = story.title,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = story.subtitle,
                        fontSize = 9.sp,
                        color = story.accentColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

/**
 * Compact, modern Alpine Weather & Telemetry Capsule.
 */
@Composable
fun AlpineWeatherTelemetryBar(
    modifier: Modifier = Modifier
) {
    var isMetric by remember { mutableStateOf(true) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { isMetric = !isMetric }
            .testTag("alpine_weather_telemetry_bar"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = SolidColor(MaterialTheme.colorScheme.outline)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Weather Condition
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.WbSunny,
                    contentDescription = null,
                    tint = AmberGold,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = if (isMetric) "24°C Alpine Clear" else "75°F Alpine Clear",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "UV 6 · Wind 14 km/h W",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Altitude & Compass
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Terrain,
                    contentDescription = null,
                    tint = HighAltitudeCyan,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (isMetric) "3,420 m Elevation" else "11,220 ft Elevation",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = HighAltitudeCyan
                    )
                    Text(
                        text = "Azimuth 328° NW · Tap for ${if (isMetric) "Imperial" else "Metric"}",
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Interactive Season Mileage & Elevation Profiler Sparkline Card.
 */
@Composable
fun ExpeditionElevationSparklineCard(
    modifier: Modifier = Modifier
) {
    var touchedIndex by remember { mutableStateOf<Int?>(null) }

    // Elevation Waypoints (Leh -> South Pullu -> Khardung La -> North Pullu -> Nubra Valley)
    val waypoints = remember {
        listOf(
            Triple("Leh Base", 11500f, "100% O2"),
            Triple("S. Pullu", 14300f, "82% O2"),
            Triple("K-Top Peak", 17582f, "53% O2 AMS Alert"),
            Triple("N. Pullu", 15200f, "74% O2"),
            Triple("Diskit Dunes", 10200f, "100% O2"),
            Triple("Hunder Camel", 9800f, "100% O2"),
            Triple("Turtuk Ridge", 9200f, "100% O2")
        )
    }

    val textMeasurer = rememberTextMeasurer()
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val outline = MaterialTheme.colorScheme.outline

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("expedition_stats_elevation_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                listOf(AdventureOrange.copy(alpha = 0.7f), HighAltitudeCyan.copy(alpha = 0.5f))
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SEASON EXPEDITIONS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = AdventureOrange
                    )
                    Text(
                        text = "1,420 km Conquered",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AmberGold.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AmberGold.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🔥 7-Day Streak", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AmberGold)
                    }
                }
            }

            // Interactive Elevation Profiler Canvas with Scrubber
            val activeWp = touchedIndex?.let { waypoints.getOrNull(it) } ?: waypoints[2]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Scrubber: ${activeWp.first} • ${"%.0f".format(activeWp.second)} ft",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = HighAltitudeCyan
                )
                Text(
                    text = activeWp.third,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (activeWp.second > 15000f) Color(0xFFEF4444) else SuccessGreen
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val segWidth = size.width / (waypoints.size - 1).toFloat()
                            val idx = (offset.x / segWidth).toInt().coerceIn(0, waypoints.size - 1)
                            touchedIndex = idx
                        }
                    }
                    .pointerInput(Unit) {
                        detectDragGestures { change, _ ->
                            val segWidth = size.width / (waypoints.size - 1).toFloat()
                            val idx = (change.position.x / segWidth).toInt().coerceIn(0, waypoints.size - 1)
                            touchedIndex = idx
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val minAlt = 8000f
                    val maxAlt = 19000f

                    val points = waypoints.mapIndexed { i, wp ->
                        val x = (w / (waypoints.size - 1).toFloat()) * i
                        val normY = (wp.second - minAlt) / (maxAlt - minAlt)
                        val y = h - (normY * (h - 20f)) - 10f
                        Offset(x, y)
                    }

                    // Fill Gradient Path
                    val fillPath = Path().apply {
                        moveTo(0f, h)
                        points.forEachIndexed { i, pt ->
                            if (i == 0) lineTo(pt.x, pt.y)
                            else lineTo(pt.x, pt.y)
                        }
                        lineTo(w, h)
                        close()
                    }
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            listOf(AdventureOrange.copy(alpha = 0.35f), Color.Transparent)
                        )
                    )

                    // Stroke Path
                    val strokePath = Path().apply {
                        points.forEachIndexed { i, pt ->
                            if (i == 0) moveTo(pt.x, pt.y) else lineTo(pt.x, pt.y)
                        }
                    }
                    drawPath(
                        path = strokePath,
                        color = AdventureOrange,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Draw Node Points
                    points.forEachIndexed { i, pt ->
                        val isSelected = touchedIndex == i || (touchedIndex == null && i == 2)
                        drawCircle(
                            color = if (isSelected) Color.White else AdventureOrange,
                            radius = if (isSelected) 5.dp.toPx() else 3.dp.toPx(),
                            center = pt
                        )
                        if (isSelected) {
                            drawCircle(
                                color = AdventureOrange,
                                radius = 7.dp.toPx(),
                                center = pt,
                                style = Stroke(width = 2.dp.toPx())
                            )
                        }
                    }
                }
            }

            // Quick Telemetry Metric Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatPill("Elevation Climb", "+9,850 m", HighAltitudeCyan)
                StatPill("Total Time", "34h 12m", AmberGold)
                StatPill("Max Lean", "38° Right", AdventureOrange)
            }
        }
    }
}

@Composable
private fun StatPill(label: String, value: String, accent: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = accent)
    }
}

/**
 * Filter Strip for Terrains & Trail Surfaces.
 */
@Composable
fun FeedTerrainFilterRow(
    selectedTerrain: String,
    onTerrainSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val terrains = listOf(
        "All Terrains",
        "🏔️ High Pass",
        "🌲 Forest & Gravel",
        "🌊 Coastal Highway",
        "🏜️ Desert Dunes",
        "🪨 Singletrack"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        terrains.forEach { terrain ->
            val isSelected = selectedTerrain == terrain
            FilterChip(
                selected = isSelected,
                onClick = { onTerrainSelected(terrain) },
                label = { Text(terrain, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AdventureOrange,
                    selectedLabelColor = Color.White,
                    containerColor = MaterialTheme.colorScheme.surface,
                    labelColor = MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(10.dp),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = if (isSelected) AdventureOrange else MaterialTheme.colorScheme.outline
                )
            )
        }
    }
}

data class GearItem(val name: String, val weightKg: Float, val isChecked: Boolean = false)

/**
 * Interactive Smart Gear & Pack Checklist with categorical tabs and weight budgeting.
 */
@Composable
fun ExpeditionGearPackListCard(
    modifier: Modifier = Modifier
) {
    val categories = remember {
        listOf("🏍️ Spares", "⛺ Campsite", "🦺 Survival", "🔋 Tech")
    }
    var selectedCategory by remember { mutableStateOf("🏍️ Spares") }

    val sparesItems = remember {
        mutableStateListOf(
            GearItem("Chain Lube & Master Link", 0.4f, true),
            GearItem("Tubeless Repair Kit & Mini Pump", 0.8f, true),
            GearItem("Compact Multi-Tool & Wrenches", 0.6f, false),
            GearItem("Spare Clutch & Throttle Cables", 0.3f, false)
        )
    }
    val campItems = remember {
        mutableStateListOf(
            GearItem("Ultralight Bivy / Alpine Tent", 1.8f, true),
            GearItem("Down Sleeping Bag (-5°C)", 1.2f, true),
            GearItem("Compact Gas Stove & Pot", 0.5f, false),
            GearItem("Micro Water Filtration Pump", 0.3f, false)
        )
    }
    val survivalItems = remember {
        mutableStateListOf(
            GearItem("AMS Diamox & High Altitude Kit", 0.1f, true),
            GearItem("Trauma Dressing & Splint", 0.4f, true),
            GearItem("Emergency Thermal Foil Blanket", 0.1f, true),
            GearItem("Satellite SOS Beacon / InReach", 0.2f, false)
        )
    }
    val techItems = remember {
        mutableStateListOf(
            GearItem("Offline GPX Maps Cached", 0.0f, true),
            GearItem("20,000mAh Rugged Powerbank", 0.4f, true),
            GearItem("Rechargeable Headlamp (350lm)", 0.2f, true),
            GearItem("Spare Action Cam Batteries", 0.3f, false)
        )
    }

    val currentList = when (selectedCategory) {
        "🏍️ Spares" -> sparesItems
        "⛺ Campsite" -> campItems
        "🦺 Survival" -> survivalItems
        else -> techItems
    }

    val allItems = sparesItems + campItems + survivalItems + techItems
    val checkedCount = allItems.count { it.isChecked }
    val totalWeight = allItems.filter { it.isChecked }.sumOf { it.weightKg.toDouble() }

    var customGearName by remember { mutableStateOf("") }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = SolidColor(MaterialTheme.colorScheme.outline)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Backpack, contentDescription = null, tint = AdventureOrange, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Smart Gear & Pack List",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = HighAltitudeCyan.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "Pack: ${"%.1f".format(totalWeight)} kg",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = HighAltitudeCyan,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Text(
                "Categorized expedition gear with weight budgeting ($checkedCount/${allItems.size} packed).",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Category Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                categories.forEach { cat ->
                    val isSel = selectedCategory == cat
                    FilterChip(
                        selected = isSel,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat, fontSize = 11.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AdventureOrange,
                            selectedLabelColor = Color.White,
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = MaterialTheme.colorScheme.onSurface
                        ),
                        shape = RoundedCornerShape(8.dp),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSel,
                            borderColor = if (isSel) AdventureOrange else MaterialTheme.colorScheme.outline
                        )
                    )
                }
            }

            // Gear Items List
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                currentList.forEachIndexed { idx, item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                currentList[idx] = item.copy(isChecked = !item.isChecked)
                            }
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = item.isChecked,
                            onCheckedChange = { checked ->
                                currentList[idx] = item.copy(isChecked = checked)
                            }
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.name,
                                fontSize = 13.sp,
                                fontWeight = if (item.isChecked) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (item.isChecked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "${"%.1f".format(item.weightKg)} kg",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (item.isChecked) AdventureOrange else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Add Custom Item Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = customGearName,
                    onValueChange = { customGearName = it },
                    placeholder = { Text("Add custom gear item...", fontSize = 11.sp) },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Button(
                    onClick = {
                        val trimmed = customGearName.trim()
                        if (trimmed.isNotBlank()) {
                            currentList.add(GearItem(trimmed, 0.3f, isChecked = true))
                            customGearName = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AdventureOrange),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                    Spacer(Modifier.width(2.dp))
                    Text("Add", fontSize = 11.sp, color = Color.White)
                }
            }
        }
    }
}
