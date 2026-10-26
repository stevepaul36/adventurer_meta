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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AdventureOrange
import com.example.ui.theme.AmberGold
import com.example.ui.theme.DangerRed
import com.example.ui.theme.HighAltitudeCyan
import com.example.viewmodel.AdventurerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GpsTrackerScreen(
    viewModel: AdventurerViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler { viewModel.navigateBack() }

    val telemetry by viewModel.telemetry.collectAsState()
    var hasPermission by remember {
        mutableStateOf(viewModel.locationManager?.hasLocationPermission() == true)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        hasPermission = fineGranted || coarseGranted
        if (hasPermission) {
            viewModel.startGpsTracking()
        }
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
                                text = if (telemetry.isRecording) "LIVE TELEMETRY HUD" else "GPS RIDE RECORDER",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "High-Pass Speed, Lean Angle & Barometric Altitude",
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
            // Real-Time GPS Status / Permission Banner
            item {
                if (!hasPermission) {
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("gps_permission_banner"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(AmberGold)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Satellite GPS Locked", fontWeight = FontWeight.Bold, color = AmberGold)
                                Text("Enable Fused Location to record real speed and coordinates.", style = MaterialTheme.typography.bodySmall, color = Color(0xFFCBD5E1))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    permissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AmberGold)
                            ) {
                                Text("Enable GPS", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF10B981).copy(alpha = 0.15f),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF10B981))
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("gps_coordinates_strip")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.GpsFixed, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (telemetry.latitude != 0.0) {
                                        "📍 Lat: ${String.format("%.4f", telemetry.latitude)}° N, Long: ${String.format("%.4f", telemetry.longitude)}° E"
                                    } else {
                                        "📍 Lat: 32.5512° N, Long: 77.1724° E (Sarchu Pass)"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "Heading: ${telemetry.bearingDeg.toInt()}°",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF10B981),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
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

            // Real-Time Motorcycle Lean Angle Gauge
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

            // Quick Waypoint Tagging & Actions
            item {
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
                        Text("Drop Waypoint", color = Color.White)
                    }

                    Button(
                        onClick = { viewModel.finishRide() },
                        colors = ButtonDefaults.buttonColors(containerColor = AdventureOrange),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Flag, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Finish & Post", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Marked Waypoints History
            if (telemetry.markedWaypoints.isNotEmpty()) {
                item {
                    Text(
                        text = "Marked Trail Waypoints (${telemetry.markedWaypoints.size})",
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
