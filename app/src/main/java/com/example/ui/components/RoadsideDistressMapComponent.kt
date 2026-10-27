package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.AdventureOrange
import com.example.ui.theme.AmberGold
import com.example.ui.theme.DangerRed
import com.example.ui.theme.HighAltitudeCyan
import kotlin.math.*

@Composable
fun RoadsideDistressMapComponent(
    distressSignals: List<RiderDistressSignal>,
    roadsideServices: List<RoadsideServiceEntity>,
    selectedMarker: MapMarkerItem?,
    onMarkerSelected: (MapMarkerItem?) -> Unit,
    onBroadcastSosClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDarkMode: Boolean = MaterialTheme.colorScheme.background.luminance() < 0.5f
) {
    val isDark = isDarkMode
    // Map Viewport Transformations
    var panOffsetX by remember { mutableStateOf(0f) }
    var panOffsetY by remember { mutableStateOf(0f) }
    var zoomScale by remember { mutableStateOf(1.0f) }

    // Tactical Radar Sweep & Pulse Animations
    val infiniteTransition = rememberInfiniteTransition(label = "map_radar_transition")

    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar_sweep_angle"
    )

    val pulseRadiusScale by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sos_pulse_radius"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sos_pulse_alpha"
    )

    var isSweepEnabled by remember { mutableStateOf(true) }
    var mapStyleMode by remember { mutableStateOf(0) } // 0 = Tactical Radar, 1 = Topo Contours

    val textMeasurer = rememberTextMeasurer()

    // Calculated marker positions for hit-testing and tap selection
    val markerScreenPositions = remember { mutableStateMapOf<String, Pair<Offset, MapMarkerItem>>() }

    val mapBgColor = if (isDark) Color(0xFF080D1A) else Color(0xFFF8FAFC)
    val mapBorderColor = if (isDark) Color(0xFF1E293B) else Color(0xFFCBD5E1)

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .background(mapBgColor)
            .border(1.dp, mapBorderColor, RoundedCornerShape(16.dp))
            .testTag("roadside_distress_map_canvas")
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        zoomScale = (zoomScale * zoom).coerceIn(0.55f, 3.2f)
                        panOffsetX += pan.x
                        panOffsetY += pan.y
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures { tapOffset ->
                        // Check hit test for markers within 40dp threshold
                        val hitMarker = markerScreenPositions.values.find { (pos, _) ->
                            val dx = tapOffset.x - pos.x
                            val dy = tapOffset.y - pos.y
                            sqrt(dx * dx + dy * dy) <= 80f
                        }?.second

                        onMarkerSelected(hitMarker)
                    }
                }
        ) {
            val center = Offset(size.width / 2f + panOffsetX, size.height / 2f + panOffsetY)
            // Scale: 20 km fits comfortably in 70% of min dimension at zoom 1.0
            val baseRadius = (minOf(size.width, size.height) * 0.42f) * zoomScale
            val kmToPx = baseRadius / 20.0f

            // 1. Draw Background Grid & Terrain Contours
            drawMapBackgroundGrid(
                center = center,
                baseRadius = baseRadius,
                kmToPx = kmToPx,
                mapStyleMode = mapStyleMode,
                textMeasurer = textMeasurer,
                isDark = isDark
            )

            // 2. Draw Highway Route Path (Trans-Himalayan Trail vector)
            drawHighwayTrailVector(center = center, kmToPx = kmToPx)

            // 3. Draw Radar Sweep Beam if enabled
            if (isSweepEnabled) {
                drawRadarSweepBeam(center = center, maxRadius = baseRadius, sweepAngle = sweepAngle)
            }

            // 4. Update screen positions & Draw Roadside Assistance Service Pins
            markerScreenPositions.clear()

            roadsideServices.forEach { service ->
                val angleRad = Math.toRadians((service.bearingDeg - 90.0)).toFloat()
                val distPx = (service.distanceKm * kmToPx).toFloat()
                val markerPos = Offset(
                    x = center.x + cos(angleRad) * distPx,
                    y = center.y + sin(angleRad) * distPx
                )
                val item = MapMarkerItem.RoadsideService(service)
                markerScreenPositions[service.id] = Pair(markerPos, item)

                val isSelected = selectedMarker?.id == service.id
                drawRoadsideServicePin(
                    position = markerPos,
                    service = service,
                    isSelected = isSelected,
                    textMeasurer = textMeasurer
                )
            }

            // 5. Draw Fellow Riders in Distress Pins (with pulsing radar waves)
            distressSignals.forEach { signal ->
                val angleRad = Math.toRadians((signal.bearingDeg - 90.0)).toFloat()
                val distPx = (signal.distanceKm * kmToPx).toFloat()
                val markerPos = Offset(
                    x = center.x + cos(angleRad) * distPx,
                    y = center.y + sin(angleRad) * distPx
                )
                val item = MapMarkerItem.RiderDistress(signal)
                markerScreenPositions[signal.id] = Pair(markerPos, item)

                val isSelected = selectedMarker?.id == signal.id

                drawDistressSignalPin(
                    position = markerPos,
                    signal = signal,
                    isSelected = isSelected,
                    pulseRadiusScale = pulseRadiusScale,
                    pulseAlpha = pulseAlpha,
                    textMeasurer = textMeasurer
                )
            }

            // 6. Draw User Location Pin ("You are here" center beacon)
            drawUserLocationCenter(center = center, pulseRadiusScale = pulseRadiusScale)
        }

        // Top Status Header Overlay
        MapTopStatusBanner(
            distressCount = distressSignals.size,
            servicesCount = roadsideServices.size,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 12.dp)
        )

        // Floating Tactical Controls (Right-aligned)
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Zoom In
            TacticalMapButton(
                icon = Icons.Default.Add,
                contentDescription = "Zoom In",
                onClick = { zoomScale = (zoomScale * 1.25f).coerceAtMost(3.2f) }
            )

            // Zoom Out
            TacticalMapButton(
                icon = Icons.Default.Remove,
                contentDescription = "Zoom Out",
                onClick = { zoomScale = (zoomScale / 1.25f).coerceAtLeast(0.55f) }
            )

            // Recenter to My Location
            TacticalMapButton(
                icon = Icons.Default.MyLocation,
                contentDescription = "Recenter Map",
                tint = AdventureOrange,
                onClick = {
                    panOffsetX = 0f
                    panOffsetY = 0f
                    zoomScale = 1.0f
                }
            )

            // Radar Sweep Toggle
            TacticalMapButton(
                icon = if (isSweepEnabled) Icons.Default.Radar else Icons.Default.SensorsOff,
                contentDescription = "Toggle Radar Sweep",
                tint = if (isSweepEnabled) HighAltitudeCyan else Color(0xFF64748B),
                onClick = { isSweepEnabled = !isSweepEnabled }
            )

            // Layer Style Toggle
            TacticalMapButton(
                icon = Icons.Default.Layers,
                contentDescription = "Map Style",
                tint = if (mapStyleMode == 1) AmberGold else Color.White,
                onClick = { mapStyleMode = (mapStyleMode + 1) % 2 }
            )
        }

        // Floating SOS-note button (bottom right). Creates a local note only; nothing is transmitted.
        FloatingActionButton(
            onClick = onBroadcastSosClick,
            containerColor = DangerRed,
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .height(48.dp)
                .testTag("broadcast_sos_fab")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Sos, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("SOS note", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

// -------------------------------------------------------------
// Canvas Drawing Helpers
// -------------------------------------------------------------

private fun DrawScope.drawMapBackgroundGrid(
    center: Offset,
    baseRadius: Float,
    kmToPx: Float,
    mapStyleMode: Int,
    textMeasurer: TextMeasurer,
    isDark: Boolean = true
) {
    val radarRingColor = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)
    val axisColor = if (isDark) Color(0xFF19253B) else Color(0xFFCBD5E1)
    val distanceLabelColor = if (isDark) Color(0xFF64748B) else Color(0xFF475569)

    // Concentric Range Rings (2.5km, 5km, 10km, 15km, 20km)
    val rangesKm = listOf(2.5f, 5.0f, 10.0f, 15.0f, 20.0f)
    rangesKm.forEach { km ->
        val r = km * kmToPx
        drawCircle(
            color = radarRingColor,
            radius = r,
            center = center,
            style = Stroke(width = if (km == 10f || km == 20f) 1.5f else 1.0f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f))
        )

        // Draw distance tag on positive Y axis
        val textLayout = textMeasurer.measure(
            text = "${km.toInt()} KM",
            style = TextStyle(color = distanceLabelColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        )
        drawText(
            textLayoutResult = textLayout,
            topLeft = Offset(center.x + 6f, center.y - r - 12f)
        )
    }

    // Compass Crosshair Axes
    drawLine(
        color = axisColor,
        start = Offset(center.x, center.y - baseRadius),
        end = Offset(center.x, center.y + baseRadius),
        strokeWidth = 1.2f
    )
    drawLine(
        color = axisColor,
        start = Offset(center.x - baseRadius, center.y),
        end = Offset(center.x + baseRadius, center.y),
        strokeWidth = 1.2f
    )

    // Cardinal directions
    val cardinalStyle = TextStyle(color = if (isDark) Color(0xFF94A3B8) else Color(0xFF334155), fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
    drawText(textMeasurer.measure("N", cardinalStyle), topLeft = Offset(center.x - 5f, center.y - baseRadius - 20f))
    drawText(textMeasurer.measure("S", cardinalStyle), topLeft = Offset(center.x - 4f, center.y + baseRadius + 4f))
    drawText(textMeasurer.measure("E", cardinalStyle), topLeft = Offset(center.x + baseRadius + 6f, center.y - 7f))
    drawText(textMeasurer.measure("W", cardinalStyle), topLeft = Offset(center.x - baseRadius - 18f, center.y - 7f))

    // Topographic Elevation Contours (if mode == 1 or subtle in mode == 0)
    val topoAlpha = if (mapStyleMode == 1) 0.35f else 0.12f
    val topoPath1 = Path().apply {
        moveTo(center.x - 70f * (kmToPx / 15f), center.y - 120f * (kmToPx / 15f))
        cubicTo(
            center.x - 10f, center.y - 160f * (kmToPx / 15f),
            center.x + 80f, center.y - 100f * (kmToPx / 15f),
            center.x + 50f, center.y - 40f * (kmToPx / 15f)
        )
        cubicTo(
            center.x + 30f, center.y + 20f,
            center.x - 50f, center.y - 30f,
            center.x - 70f * (kmToPx / 15f), center.y - 120f * (kmToPx / 15f)
        )
        close()
    }
    drawPath(
        path = topoPath1,
        color = AmberGold.copy(alpha = topoAlpha),
        style = Stroke(width = 1.2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f))
    )

    val topoPath2 = Path().apply {
        moveTo(center.x - 140f * (kmToPx / 15f), center.y + 60f * (kmToPx / 15f))
        cubicTo(
            center.x - 60f, center.y + 110f,
            center.x - 10f, center.y + 80f,
            center.x - 40f, center.y + 30f
        )
        cubicTo(
            center.x - 80f, center.y + 10f,
            center.x - 120f, center.y + 20f,
            center.x - 140f * (kmToPx / 15f), center.y + 60f * (kmToPx / 15f)
        )
        close()
    }
    drawPath(
        path = topoPath2,
        color = HighAltitudeCyan.copy(alpha = topoAlpha),
        style = Stroke(width = 1.0f)
    )
}

private fun DrawScope.drawHighwayTrailVector(center: Offset, kmToPx: Float) {
    val highwayPath = Path().apply {
        moveTo(center.x - 180f * (kmToPx / 15f), center.y + 190f * (kmToPx / 15f))
        quadraticBezierTo(
            center.x - 80f * (kmToPx / 15f), center.y + 90f * (kmToPx / 15f),
            center.x, center.y
        )
        quadraticBezierTo(
            center.x + 60f * (kmToPx / 15f), center.y - 70f * (kmToPx / 15f),
            center.x + 160f * (kmToPx / 15f), center.y - 180f * (kmToPx / 15f)
        )
    }

    // Outer glow for trail
    drawPath(
        path = highwayPath,
        color = Color(0xFF38BDF8).copy(alpha = 0.15f),
        style = Stroke(width = 6f, cap = StrokeCap.Round)
    )
    // Main trail line
    drawPath(
        path = highwayPath,
        color = Color(0xFF38BDF8).copy(alpha = 0.55f),
        style = Stroke(
            width = 2.5f,
            cap = StrokeCap.Round,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 6f), 0f)
        )
    )
}

private fun DrawScope.drawRadarSweepBeam(center: Offset, maxRadius: Float, sweepAngle: Float) {
    val sweepBrush = Brush.sweepGradient(
        colors = listOf(
            Color.Transparent,
            Color.Transparent,
            HighAltitudeCyan.copy(alpha = 0.02f),
            HighAltitudeCyan.copy(alpha = 0.18f)
        ),
        center = center
    )

    rotate(degrees = sweepAngle, pivot = center) {
        drawCircle(
            brush = sweepBrush,
            radius = maxRadius,
            center = center
        )
        // Leading radar scan line
        drawLine(
            color = HighAltitudeCyan.copy(alpha = 0.7f),
            start = center,
            end = Offset(center.x + maxRadius, center.y),
            strokeWidth = 2.0f
        )
    }
}

private fun DrawScope.drawUserLocationCenter(center: Offset, pulseRadiusScale: Float) {
    // Outer breathing aura
    drawCircle(
        color = AdventureOrange.copy(alpha = 0.2f),
        radius = 28f * pulseRadiusScale + 12f,
        center = center
    )
    // Solid base ring
    drawCircle(
        color = AdventureOrange,
        radius = 10f,
        center = center,
        style = Stroke(width = 2.5f)
    )
    // Core dot
    drawCircle(
        color = AdventureOrange,
        radius = 6f,
        center = center
    )
    // User Heading Cone (pointing North-East ~35 deg)
    val conePath = Path().apply {
        moveTo(center.x, center.y - 12f)
        lineTo(center.x + 8f, center.y - 28f)
        lineTo(center.x - 8f, center.y - 28f)
        close()
    }
    drawPath(
        path = conePath,
        color = AdventureOrange.copy(alpha = 0.75f)
    )
}

private fun DrawScope.drawDistressSignalPin(
    position: Offset,
    signal: RiderDistressSignal,
    isSelected: Boolean,
    pulseRadiusScale: Float,
    pulseAlpha: Float,
    textMeasurer: TextMeasurer
) {
    val typeColor = Color(signal.distressType.badgeColorHex)

    // 1. Concentric Animated Pulsing Danger Ripples
    drawCircle(
        color = typeColor.copy(alpha = pulseAlpha * 0.75f),
        radius = 14f + (36f * pulseRadiusScale),
        center = position,
        style = Stroke(width = 2.0f)
    )
    drawCircle(
        color = typeColor.copy(alpha = pulseAlpha * 0.45f),
        radius = 8f + (24f * pulseRadiusScale),
        center = position,
        style = Stroke(width = 1.5f)
    )

    // 2. Selection Target Reticle
    if (isSelected) {
        drawCircle(
            color = Color.White,
            radius = 28f,
            center = position,
            style = Stroke(width = 2.0f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f))
        )
        // Corner brackets / Crosshairs
        drawLine(Color.White, Offset(position.x - 34f, position.y), Offset(position.x - 22f, position.y), 2f)
        drawLine(Color.White, Offset(position.x + 22f, position.y), Offset(position.x + 34f, position.y), 2f)
        drawLine(Color.White, Offset(position.x, position.y - 34f), Offset(position.x, position.y - 22f), 2f)
        drawLine(Color.White, Offset(position.x, position.y + 22f), Offset(position.x, position.y + 34f), 2f)
    }

    // 3. Hexagonal / Shield Pin Body
    val pinRadius = 14f
    drawCircle(
        color = Color(0xFF0F172A),
        radius = pinRadius,
        center = position
    )
    drawCircle(
        color = typeColor,
        radius = pinRadius,
        center = position,
        style = Stroke(width = 3.0f)
    )
    drawCircle(
        color = typeColor,
        radius = 5.5f,
        center = position
    )

    // 4. Floating Rider Name & Distress Badge Tag
    val tagText = "SOS: ${signal.riderName.split(" ").first()}"
    val textLayout = textMeasurer.measure(
        text = tagText,
        style = TextStyle(
            color = Color.White,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            background = typeColor.copy(alpha = 0.85f)
        )
    )
    drawText(
        textLayoutResult = textLayout,
        topLeft = Offset(position.x - (textLayout.size.width / 2f), position.y + pinRadius + 4f)
    )
}

private fun DrawScope.drawRoadsideServicePin(
    position: Offset,
    service: RoadsideServiceEntity,
    isSelected: Boolean,
    textMeasurer: TextMeasurer
) {
    val serviceColor = when (service.type) {
        StationType.MECHANIC -> AdventureOrange
        StationType.HOSPITAL -> DangerRed
        StationType.PUNCTURE -> AmberGold
        StationType.FUEL_STASH -> HighAltitudeCyan
        StationType.DHABA -> Color(0xFF10B981)
    }

    // Selection Ring
    if (isSelected) {
        drawCircle(
            color = serviceColor,
            radius = 24f,
            center = position,
            style = Stroke(width = 2.0f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f), 0f))
        )
    }

    // Pin Body
    val pinRadius = 12f
    drawCircle(
        color = Color(0xFF0F172A),
        radius = pinRadius,
        center = position
    )
    drawCircle(
        color = serviceColor,
        radius = pinRadius,
        center = position,
        style = Stroke(width = 2.5f)
    )
    drawCircle(
        color = serviceColor,
        radius = 4.5f,
        center = position
    )

    // Floating Service Label
    val serviceText = service.name.take(14)
    val textLayout = textMeasurer.measure(
        text = serviceText,
        style = TextStyle(
            color = Color(0xFFCBD5E1),
            fontSize = 8.5.sp,
            fontWeight = FontWeight.SemiBold,
            background = Color(0xFF0F172A).copy(alpha = 0.8f)
        )
    )
    drawText(
        textLayoutResult = textLayout,
        topLeft = Offset(position.x - (textLayout.size.width / 2f), position.y + pinRadius + 3f)
    )
}

// -------------------------------------------------------------
// UI Control Components
// -------------------------------------------------------------

@Composable
private fun MapTopStatusBanner(
    distressCount: Int,
    servicesCount: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(DangerRed)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "$distressCount In Distress",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = DangerRed
                )
            }

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(12.dp)
                    .background(MaterialTheme.colorScheme.outline)
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(HighAltitudeCyan)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "$servicesCount Services Online",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = HighAltitudeCyan
                )
            }
        }
    }
}

@Composable
private fun TacticalMapButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    tint: Color = MaterialTheme.colorScheme.onSurface
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.size(38.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = contentDescription, tint = tint, modifier = Modifier.size(18.dp))
        }
    }
}
