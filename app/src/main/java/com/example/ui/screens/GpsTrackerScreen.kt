package com.example.ui.screens

import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LiveRideTelemetry
import com.example.data.repository.formatRideDuration
import com.example.ui.components.HoldToStartRideButton
import com.example.ui.theme.AdventureOrange
import com.example.ui.theme.AmberGold
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.HighAltitudeCyan
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.AdventurerViewModel
import kotlin.math.abs

private enum class RideStatus { READY, RECORDING, PAUSED }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GpsTrackerScreen(
    viewModel: AdventurerViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler { viewModel.navigateBack() }

    val telemetry by viewModel.telemetry.collectAsState()
    val status = when {
        !telemetry.isRecording -> RideStatus.READY
        telemetry.isPaused -> RideStatus.PAUSED
        else -> RideStatus.RECORDING
    }
    var hasPermission by remember { mutableStateOf(viewModel.locationManager?.hasLocationPermission() == true) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        hasPermission = result[Manifest.permission.ACCESS_FINE_LOCATION] == true || result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (hasPermission) viewModel.onLocationPermissionGranted()
    }
    val requestLocation = {
        permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
    }

    var showWaypointDialog by remember { mutableStateOf(false) }
    var showFinishDialog by remember { mutableStateOf(false) }

    // Riders glance at the HUD — don't let the screen sleep mid-ride.
    val view = LocalView.current
    DisposableEffect(telemetry.isRecording) {
        view.keepScreenOn = telemetry.isRecording
        onDispose { view.keepScreenOn = false }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(if (status == RideStatus.READY) "Ride" else "Live ride", fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Speed · lean angle · altitude", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    }
                },
                actions = { StatusPill(status, telemetry.isDemo) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        bottomBar = {
            if (status != RideStatus.READY) {
                RideControls(
                    paused = status == RideStatus.PAUSED,
                    onPauseToggle = { if (status == RideStatus.PAUSED) viewModel.resumeRide() else viewModel.pauseRide() },
                    onWaypoint = { showWaypointDialog = true },
                    onFinish = { showFinishDialog = true },
                )
            }
        },
        containerColor = DarkBackground,
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { GpsStatusCard(hasPermission, telemetry, onEnable = requestLocation) }

            if (status == RideStatus.READY) {
                item { ReadyCard(hasPermission, onStart = { viewModel.startRide() }) }
            } else {
                item { SpeedCard(telemetry, paused = status == RideStatus.PAUSED) }
                item { LeanCard(telemetry) }
                if (telemetry.markedWaypoints.isNotEmpty()) {
                    item {
                        Text("Waypoints (${telemetry.markedWaypoints.size})", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    items(telemetry.markedWaypoints.reversed()) { wp ->
                        Surface(shape = RoundedCornerShape(12.dp), color = DarkSurface, modifier = Modifier.fillMaxWidth()) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(waypointIcon(wp.type), contentDescription = wp.type, tint = AmberGold, modifier = Modifier.size(24.dp))
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(wp.label, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text("${wp.timestamp} · ${wp.altitudeM} m · ${wp.speedKmH.toInt()} km/h", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showWaypointDialog) {
        WaypointDialog(
            onDismiss = { showWaypointDialog = false },
            onSave = { label, type -> viewModel.addWaypoint(label, type); showWaypointDialog = false },
        )
    }

    if (showFinishDialog) {
        AlertDialog(
            onDismissRequest = { showFinishDialog = false },
            icon = { Icon(Icons.Default.Flag, contentDescription = null, tint = AdventureOrange) },
            title = { Text("Finish this ride?", fontWeight = FontWeight.Bold, color = Color.White) },
            text = {
                Text(
                    "${telemetry.distanceKm} km · ${formatRideDuration(telemetry.elapsedSeconds)} · max lean ${telemetry.maxLeanAngleDeg}°" +
                        if (telemetry.isDemo) "\n\nThis ride used demo data because location was off." else "",
                    color = Color(0xFFCBD5E1)
                )
            },
            confirmButton = {
                Button(
                    onClick = { showFinishDialog = false; viewModel.finishRide() },
                    colors = ButtonDefaults.buttonColors(containerColor = AdventureOrange),
                    modifier = Modifier.testTag("confirm_finish_ride")
                ) { Text("Save & post", color = Color.Black, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = { showFinishDialog = false; viewModel.discardRide() }) { Text("Discard", color = DangerRed) }
                    TextButton(onClick = { showFinishDialog = false }) { Text("Keep riding", color = TextSecondary) }
                }
            },
            containerColor = DarkSurface,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

// ----------------------------------------------------------------------------

@Composable
private fun StatusPill(status: RideStatus, demo: Boolean) {
    val (label, color) = when {
        status == RideStatus.READY -> "READY" to TextSecondary
        status == RideStatus.PAUSED -> "PAUSED" to AmberGold
        demo -> "DEMO" to HighAltitudeCyan
        else -> "REC" to DangerRed
    }
    Surface(shape = RoundedCornerShape(50), color = color.copy(alpha = 0.18f), modifier = Modifier.padding(end = 12.dp)) {
        Row(Modifier.padding(horizontal = 10.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = color, modifier = Modifier.size(8.dp)) {}
            Spacer(Modifier.width(6.dp))
            Text(label, color = color, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun GpsStatusCard(hasPermission: Boolean, t: LiveRideTelemetry, onEnable: () -> Unit) {
    when {
        !hasPermission -> Card(
            modifier = Modifier.fillMaxWidth().testTag("gps_permission_banner"),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
            border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(AmberGold))
        ) {
            Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOff, contentDescription = null, tint = AmberGold)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Location is off", fontWeight = FontWeight.Bold, color = AmberGold)
                    Text(
                        if (t.isRecording) "Showing demo data. Turn on location to record this ride for real."
                        else "Needed for real speed, distance and route. Without it the HUD runs in demo mode.",
                        style = MaterialTheme.typography.bodySmall, color = Color(0xFFCBD5E1)
                    )
                }
                Spacer(Modifier.width(8.dp))
                Button(onClick = onEnable, colors = ButtonDefaults.buttonColors(containerColor = AmberGold)) {
                    Text("Turn on", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
        t.isRecording && !t.hasGpsFix && !t.isDemo -> Surface(
            shape = RoundedCornerShape(12.dp), color = DarkSurfaceVariant, modifier = Modifier.fillMaxWidth()
        ) {
            Row(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(color = AmberGold, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(10.dp))
                Text("Searching for GPS… open sky helps", color = Color.White, style = MaterialTheme.typography.bodySmall)
            }
        }
        t.hasGpsFix -> Surface(
            shape = RoundedCornerShape(12.dp),
            color = SuccessGreen.copy(alpha = 0.12f),
            border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(SuccessGreen.copy(alpha = 0.6f))),
            modifier = Modifier.fillMaxWidth().testTag("gps_coordinates_strip")
        ) {
            Row(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.GpsFixed, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(formatCoordinates(t.latitude, t.longitude), color = Color.White, style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f))
                Text("${compassPoint(t.bearingDeg)} ${t.bearingDeg.toInt()}°", color = SuccessGreen, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            }
        }
        else -> Surface(shape = RoundedCornerShape(12.dp), color = SuccessGreen.copy(alpha = 0.12f), modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.GpsFixed, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("Location ready", color = Color.White, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun ReadyCard(hasPermission: Boolean, onStart: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.verticalGradient(listOf(AdventureOrange.copy(alpha = 0.6f), DarkSurfaceVariant)))
    ) {
        Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("Ready when you are", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
            HoldToStartRideButton(onRideStarted = onStart)
            Text(
                "Press and hold to start recording. Holding prevents accidental starts in a pocket or tank bag.",
                style = MaterialTheme.typography.bodySmall, color = TextSecondary, textAlign = TextAlign.Center
            )
            if (!hasPermission) {
                Text("Location is off — this ride will use demo data.", style = MaterialTheme.typography.labelMedium, color = AmberGold)
            }
        }
    }
}

@Composable
private fun SpeedCard(t: LiveRideTelemetry, paused: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("speedometer_hud_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.verticalGradient(listOf(AdventureOrange.copy(alpha = 0.6f), DarkSurfaceVariant)))
    ) {
        Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            val fraction by animateFloatAsState((t.currentSpeedKmH / 160.0).coerceIn(0.0, 1.0).toFloat(), tween(250), label = "speed")
            Box(
                Modifier.size(220.dp).semantics { contentDescription = "Speed ${t.currentSpeedKmH.toInt()} kilometres per hour" },
                contentAlignment = Alignment.Center
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    val stroke = 14.dp.toPx()
                    drawArc(DarkSurfaceVariant, 150f, 240f, false, style = Stroke(stroke, cap = StrokeCap.Round))
                    drawArc(
                        brush = Brush.sweepGradient(listOf(HighAltitudeCyan, AmberGold, AdventureOrange)),
                        startAngle = 150f, sweepAngle = fraction * 240f, useCenter = false,
                        style = Stroke(stroke, cap = StrokeCap.Round)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        if (paused) "—" else "${t.currentSpeedKmH.toInt()}",
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 72.sp, lineHeight = 72.sp),
                        fontWeight = FontWeight.Bold, color = Color.White
                    )
                    Text("km/h", style = MaterialTheme.typography.titleSmall, color = AmberGold, fontWeight = FontWeight.Bold)
                    if (t.isDemo) Text("demo data", style = MaterialTheme.typography.labelSmall, color = HighAltitudeCyan)
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth()) {
                HudStat("AVG", "${t.averageSpeedKmH.toInt()} km/h", Color.White, Modifier.weight(1f))
                HudStat("TOP", "${t.topSpeedKmH.toInt()} km/h", AdventureOrange, Modifier.weight(1f))
            }
            HorizontalDivider(color = DarkSurfaceVariant, modifier = Modifier.padding(vertical = 14.dp))
            Row(Modifier.fillMaxWidth()) {
                HudStat("DISTANCE", "%.2f km".format(t.distanceKm), AmberGold, Modifier.weight(1f))
                HudStat("RIDE TIME", formatRideDuration(t.elapsedSeconds), Color.White, Modifier.weight(1f))
            }
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth()) {
                HudStat("ALTITUDE", if (t.hasGpsFix || t.isDemo) "${t.altitudeM} m" else "—", HighAltitudeCyan, Modifier.weight(1f))
                HudStat("CLIMBED", "+${t.elevationGainM} m", SuccessGreen, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun LeanCard(t: LiveRideTelemetry) {
    val lean = t.leanAngleDeg
    val danger = abs(lean) > 40
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(DarkSurfaceBorder))
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Lean angle", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(1f))
                Text("max ${t.maxLeanAngleDeg}°", style = MaterialTheme.typography.labelLarge, color = AmberGold, fontWeight = FontWeight.Bold)
            }
            val animated by animateFloatAsState(lean.toFloat(), tween(150), label = "lean")
            Box(
                Modifier.fillMaxWidth().height(110.dp).semantics {
                    contentDescription = "Lean ${abs(lean)} degrees ${if (lean < 0) "left" else if (lean > 0) "right" else "upright"}"
                },
                contentAlignment = Alignment.BottomCenter
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    val cx = size.width / 2
                    val gy = size.height * 0.85f
                    val len = size.height * 0.7f
                    // ±45° guide marks
                    listOf(-45f, -30f, -15f, 0f, 15f, 30f, 45f).forEach { a ->
                        rotate(a, Offset(cx, gy)) {
                            drawLine(if (a == 0f) Color(0x66FFFFFF) else DarkSurfaceBorder, Offset(cx, gy - len), Offset(cx, gy - len + 10.dp.toPx()), 2.dp.toPx())
                        }
                    }
                    drawLine(DarkSurfaceBorder, Offset(16f, gy), Offset(size.width - 16f, gy), 2.dp.toPx())
                    rotate(animated, Offset(cx, gy)) {
                        drawLine(
                            brush = Brush.verticalGradient(listOf(if (danger) DangerRed else AdventureOrange, AmberGold)),
                            start = Offset(cx, gy - len + 14.dp.toPx()), end = Offset(cx, gy),
                            strokeWidth = 7.dp.toPx(), cap = StrokeCap.Round
                        )
                        drawCircle(Color.White, 7.dp.toPx(), Offset(cx, gy - len + 14.dp.toPx()))
                    }
                }
            }
            Text(
                "${abs(lean)}° ${if (lean < 0) "LEFT" else if (lean > 0) "RIGHT" else "UPRIGHT"}",
                style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold,
                color = if (danger) DangerRed else AdventureOrange,
                modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center
            )
        }
    }
}

/** Large, glove-friendly controls pinned to the bottom while a ride is active. */
@Composable
private fun RideControls(paused: Boolean, onPauseToggle: () -> Unit, onWaypoint: () -> Unit, onFinish: () -> Unit) {
    Surface(color = DarkSurface, tonalElevation = 4.dp) {
        Row(
            Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onPauseToggle,
                colors = ButtonDefaults.buttonColors(containerColor = if (paused) SuccessGreen else DarkSurfaceVariant),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.weight(1f).height(56.dp).testTag("pause_ride_button")
            ) {
                Icon(if (paused) Icons.Default.PlayArrow else Icons.Default.Pause, contentDescription = null, tint = if (paused) Color.Black else Color.White)
                Spacer(Modifier.width(6.dp))
                Text(if (paused) "Resume" else "Pause", color = if (paused) Color.Black else Color.White, fontWeight = FontWeight.Bold)
            }
            FilledTonalIconButton(
                onClick = onWaypoint,
                colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = DarkSurfaceVariant),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.size(56.dp)
            ) {
                Icon(Icons.Default.AddLocationAlt, contentDescription = "Drop waypoint", tint = HighAltitudeCyan)
            }
            Button(
                onClick = onFinish,
                colors = ButtonDefaults.buttonColors(containerColor = AdventureOrange),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.weight(1f).height(56.dp).testTag("finish_ride_button")
            ) {
                Icon(Icons.Default.Flag, contentDescription = null, tint = Color.Black)
                Spacer(Modifier.width(6.dp))
                Text("Finish", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WaypointDialog(onDismiss: () -> Unit, onSave: (String, String) -> Unit) {
    var label by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Photo") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Drop a waypoint", fontWeight = FontWeight.Bold, color = Color.White) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Photo", "Chai", "Fuel", "Pass").forEach { t ->
                        FilterChip(
                            selected = type == t, onClick = { type = t }, label = { Text(t) },
                            leadingIcon = { Icon(waypointIcon(t), contentDescription = null, modifier = Modifier.size(16.dp)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AdventureOrange, selectedLabelColor = Color.Black, selectedLeadingIconColor = Color.Black,
                                containerColor = DarkSurfaceVariant, labelColor = Color.White, iconColor = Color.White
                            )
                        )
                    }
                }
                OutlinedTextField(
                    value = label, onValueChange = { label = it.take(60) },
                    label = { Text("Name (optional)") }, placeholder = { Text("e.g. Rohtang chai stop") },
                    singleLine = true, modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = AdventureOrange)
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSave(label.ifBlank { "$type stop" }, type) }, colors = ButtonDefaults.buttonColors(containerColor = AdventureOrange)) {
                Text("Save", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) } },
        containerColor = DarkSurface,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun HudStat(label: String, value: String, valueColor: Color, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary, letterSpacing = 1.sp)
        Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = valueColor)
    }
}

private fun waypointIcon(type: String) = when (type) {
    "Fuel" -> Icons.Default.LocalGasStation
    "Chai" -> Icons.Default.Coffee
    "Photo" -> Icons.Default.CameraAlt
    else -> Icons.Default.Terrain
}

private fun formatCoordinates(lat: Double, lon: Double): String =
    "%.4f° %s, %.4f° %s".format(abs(lat), if (lat >= 0) "N" else "S", abs(lon), if (lon >= 0) "E" else "W")

private fun compassPoint(bearing: Float): String {
    val points = listOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
    return points[(((bearing % 360) + 360) % 360 / 45f + 0.5f).toInt() % 8]
}
