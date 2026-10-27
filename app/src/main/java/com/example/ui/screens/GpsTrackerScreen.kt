package com.example.ui.screens

import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AdventureOrange
import com.example.ui.theme.AmberGold
import com.example.ui.theme.DangerRed
import com.example.ui.theme.HighAltitudeCyan
import com.example.analytics.AdventureFunnelAnalytics
import com.example.viewmodel.AdventurerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GpsTrackerScreen(
    viewModel: AdventurerViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler { viewModel.navigateBack() }

    val context = LocalContext.current
    val telemetry by viewModel.telemetry.collectAsState()
    val selectedActivityMode by viewModel.activityMode.collectAsState()
    val activityType = if (telemetry.isRecording) telemetry.activityType else selectedActivityMode
    var hasLocationPermission by remember { mutableStateOf(viewModel.locationManager?.hasLocationPermission() == true) }
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        hasLocationPermission = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (hasLocationPermission) viewModel.onLocationPermissionGranted()
    }
    val requestLocationPermission = {
        locationPermissionLauncher.launch(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        )
    }

    var showWaypointDialog by remember { mutableStateOf(false) }
    var waypointLabel by remember { mutableStateOf("") }
    var waypointType by remember { mutableStateOf("Photo") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = if (telemetry.isRecording) Color(0xFF10B981) else AmberGold,
                            modifier = Modifier.size(12.dp)
                        ) {}
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (telemetry.isRecording) "LIVE ${activityType.displayName.uppercase()} TRACKER" else "START ${activityType.displayName.uppercase()} SESSION",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = activityType.trackerSubtitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = AmberGold
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.navigateTo(com.example.viewmodel.Screen.RADAR) },
                        modifier = Modifier.testTag("gps_radar_sos_shortcut")
                    ) {
                        Icon(Icons.Default.Emergency, contentDescription = "SOS & Roadside Radar", tint = DangerRed)
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
            if (!hasLocationPermission) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2637))
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Enable location for activity tracking", color = Color.White, fontWeight = FontWeight.Bold)
                            Text(
                                "Without location permission, the app can show demo motion. Demo speed/distance/lean are simulated, not GPS-recorded.",
                                color = Color(0xFFCBD5E1), style = MaterialTheme.typography.bodySmall
                            )
                            Button(onClick = requestLocationPermission, colors = ButtonDefaults.buttonColors(containerColor = AdventureOrange)) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Black)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Enable GPS", color = Color.Black)
                            }
                        }
                    }
                }
            }
            if (telemetry.isDemo && telemetry.isRecording) {
                item {
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFF3A2A17))) {
                        Text(
                            "DEMO MODE — movement and telemetry are simulated. Grant location permission to record actual GPS data.",
                            color = Color(0xFFFDE68A), modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            if (telemetry.isRecording && !telemetry.isDemo && !telemetry.hasGpsFix) {
                item {
                    Text("Waiting for the first accurate GPS fix…", color = AmberGold, style = MaterialTheme.typography.bodySmall)
                }
            }

            // Speedometer HUD Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("speedometer_hud_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2A)),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.verticalGradient(
                            listOf(AdventureOrange.copy(alpha = 0.6f), Color(0xFF1E293B))
                        )
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Analog / Digital Speed Dial
                        val speedAngle = (telemetry.currentSpeedKmH / 160.0).coerceIn(0.0, 1.0).toFloat()
                        val animatedSpeedAngle by animateFloatAsState(
                            targetValue = speedAngle,
                            animationSpec = tween(durationMillis = 200),
                            label = "speed_arc"
                        )

                        Box(
                            modifier = Modifier.size(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val strokeWidth = 12.dp.toPx()
                                // Background Arc (240 deg sweep from 150 deg)
                                drawArc(
                                    color = Color(0xFF1E293B),
                                    startAngle = 150f,
                                    sweepAngle = 240f,
                                    useCenter = false,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                )
                                // Active Speed Arc
                                drawArc(
                                    brush = Brush.sweepGradient(
                                        listOf(HighAltitudeCyan, AmberGold, AdventureOrange)
                                    ),
                                    startAngle = 150f,
                                    sweepAngle = animatedSpeedAngle * 240f,
                                    useCenter = false,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${telemetry.currentSpeedKmH.toInt()}",
                                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 54.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "KM / H",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = AmberGold,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Avg: ${telemetry.averageSpeedKmH} km/h • Max: ${telemetry.topSpeedKmH.toInt()} km/h",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Stats Grid: Distance, Time, Altitude, Calories
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            HudStatBox("DISTANCE", "${telemetry.distanceKm} km", AmberGold)
                            val minutes = telemetry.elapsedSeconds / 60
                            val seconds = telemetry.elapsedSeconds % 60
                            HudStatBox("ELAPSED", "${minutes}m ${seconds}s", Color.White)
                            HudStatBox("ALTITUDE", "${telemetry.altitudeM} m", HighAltitudeCyan)
                            HudStatBox("CALORIES", "${telemetry.caloriesBurned} kcal", AdventureOrange)
                        }
                    }
                }
            }

            // Lean-angle telemetry is a motorcycle-only sensor estimate.
            if (activityType.supportsLeanAngle) {
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Corner Lean Angle Meter",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = AmberGold.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "MAX LEAN: ${telemetry.maxLeanAngleDeg}°",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AmberGold,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Motorcycle Lean Indicator Canvas
                        val leanDeg = telemetry.leanAngleDeg.toFloat()
                        val animatedLean by animateFloatAsState(
                            targetValue = leanDeg,
                            animationSpec = tween(durationMillis = 150),
                            label = "lean_angle"
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val centerX = size.width / 2
                                val groundY = size.height * 0.75f

                                // Horizon ground line
                                drawLine(
                                    color = Color(0xFF334155),
                                    start = Offset(20f, groundY),
                                    end = Offset(size.width - 20f, groundY),
                                    strokeWidth = 2.dp.toPx()
                                )

                                // Center reference line
                                drawLine(
                                    color = Color(0x66FFFFFF),
                                    start = Offset(centerX, groundY - 50.dp.toPx()),
                                    end = Offset(centerX, groundY + 10.dp.toPx()),
                                    strokeWidth = 1.dp.toPx()
                                )

                                // Tilted bike axis indicator
                                rotate(degrees = animatedLean, pivot = Offset(centerX, groundY)) {
                                    drawLine(
                                        brush = Brush.verticalGradient(
                                            listOf(
                                                if (kotlin.math.abs(animatedLean) > 35f) DangerRed else AdventureOrange,
                                                AmberGold
                                            )
                                        ),
                                        start = Offset(centerX, groundY - 60.dp.toPx()),
                                        end = Offset(centerX, groundY),
                                        strokeWidth = 6.dp.toPx(),
                                        cap = StrokeCap.Round
                                    )
                                    // Pointer head
                                    drawCircle(
                                        color = Color.White,
                                        radius = 6.dp.toPx(),
                                        center = Offset(centerX, groundY - 60.dp.toPx())
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("◀ 45° LEFT", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                                Text(
                                    text = "${kotlin.math.abs(telemetry.leanAngleDeg)}° ${if (telemetry.leanAngleDeg < 0) "LEFT" else if (telemetry.leanAngleDeg > 0) "RIGHT" else "UPRIGHT"}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (kotlin.math.abs(telemetry.leanAngleDeg) > 35) DangerRed else AdventureOrange
                                )
                                Text("RIGHT 45° ▶", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                            }
                        }
                    }
                }
            }
            }

            if (activityType.usesSurfaceLocationOnly) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF3A2A17))
                    ) {
                        Text(
                            "Surface GPS only: this prototype cannot track your underwater position and is not a dive computer. Use certified dive equipment and training for depth, gas, ascent and decompression decisions.",
                            color = Color(0xFFFDE68A),
                            modifier = Modifier.padding(14.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            if (!telemetry.isRecording) {
                item {
                    Button(
                        onClick = {
                            AdventureFunnelAnalytics.log(context, "activity_started", mapOf("activity_type" to selectedActivityMode.name))
                            viewModel.startRide()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = AdventureOrange),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Start ${activityType.displayName.lowercase()}", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { showWaypointDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.AddLocation, contentDescription = null, tint = HighAltitudeCyan, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Drop waypoint", color = Color.White)
                            }
                            Button(
                                onClick = { if (telemetry.isPaused) viewModel.resumeRide() else viewModel.pauseRide() },
                                colors = ButtonDefaults.buttonColors(containerColor = AmberGold),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(if (telemetry.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (telemetry.isPaused) "Resume" else "Pause", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                        Button(
                            onClick = { viewModel.finishRide() },
                            colors = ButtonDefaults.buttonColors(containerColor = AdventureOrange),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Flag, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Finish & Post", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = { viewModel.discardRide() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Discard session")
                        }
                    }
                }
            }

            // Marked Waypoints History
            if (telemetry.markedWaypoints.isNotEmpty()) {
                item {
                    Text(
                        text = "Session Waypoints (${telemetry.markedWaypoints.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                items(telemetry.markedWaypoints.size) { i ->
                    val wp = telemetry.markedWaypoints[i]
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF131B2A),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = when (wp.type) {
                                    "Fuel" -> Icons.Default.LocalGasStation
                                    "Chai" -> Icons.Default.Coffee
                                    "Photo" -> Icons.Default.CameraAlt
                                    else -> Icons.Default.Map
                                },
                                contentDescription = null,
                                tint = AmberGold,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(wp.label, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("${wp.timestamp} • Alt: ${wp.altitudeM}m • Speed: ${wp.speedKmH.toInt()} km/h", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showWaypointDialog) {
        AlertDialog(
            onDismissRequest = { showWaypointDialog = false },
            title = { Text("Mark Trail Waypoint", fontWeight = FontWeight.Bold, color = Color.White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = waypointLabel,
                        onValueChange = { waypointLabel = it },
                        label = { Text("Waypoint Name") },
                        placeholder = { Text("e.g. Rohtang Chai Stop, Fuel point") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = AdventureOrange
                        )
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Photo", "Chai", "Fuel", "Pass").forEach { t ->
                            FilterChip(
                                selected = waypointType == t,
                                onClick = { waypointType = t },
                                label = { Text(t) },
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
            },
            confirmButton = {
                Button(
                    onClick = {
                        val name = if (waypointLabel.isBlank()) "$waypointType Stop" else waypointLabel
                        viewModel.addWaypoint(name, waypointType)
                        waypointLabel = ""
                        showWaypointDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AdventureOrange)
                ) {
                    Text("Save Waypoint", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showWaypointDialog = false }) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            },
            containerColor = Color(0xFF131B2A),
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun HudStatBox(label: String, value: String, valueColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = valueColor)
    }
}
