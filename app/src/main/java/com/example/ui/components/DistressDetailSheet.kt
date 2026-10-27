package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.*
import com.example.ui.theme.AdventureOrange
import com.example.ui.theme.AmberGold
import com.example.ui.theme.DangerRed
import com.example.ui.theme.HighAltitudeCyan

@Composable
fun DistressDetailSheet(
    marker: MapMarkerItem,
    onClose: () -> Unit,
    onToggleRespond: (String) -> Unit,
    onCancelMySos: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .testTag("distress_detail_sheet"),
        color = Color(0xFF0F172A),
        tonalElevation = 12.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Drag handle & Close row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .width(44.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFF475569))
                )

                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(28.dp).testTag("close_sheet_btn")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF94A3B8))
                }
            }

            when (marker) {
                is MapMarkerItem.RiderDistress -> {
                    RiderDistressDetailContent(
                        signal = marker.signal,
                        onToggleRespond = { onToggleRespond(marker.signal.id) },
                        onCancelMySos = onCancelMySos,
                        onCall = { EmergencyActions.dial(context, marker.signal.phoneNumber) },
                        onNavigate = {
                            EmergencyActions.navigateToCoordinates(
                                context = context,
                                latitude = marker.signal.latitude,
                                longitude = marker.signal.longitude,
                                label = "Distressed Rider: ${marker.signal.riderName}"
                            )
                        }
                    )
                }
                is MapMarkerItem.RoadsideService -> {
                    RoadsideServiceDetailContent(
                        service = marker.service,
                        onCall = { EmergencyActions.dial(context, marker.service.phone) },
                        onNavigate = {
                            EmergencyActions.navigateToCoordinates(
                                context = context,
                                latitude = marker.service.latitude,
                                longitude = marker.service.longitude,
                                label = marker.service.name
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun RiderDistressDetailContent(
    signal: RiderDistressSignal,
    onToggleRespond: () -> Unit,
    onCancelMySos: () -> Unit,
    onCall: () -> Unit,
    onNavigate: () -> Unit
) {
    val typeColor = Color(signal.distressType.badgeColorHex)

    // Header badge
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = typeColor.copy(alpha = 0.2f),
            border = androidx.compose.foundation.BorderStroke(1.dp, typeColor.copy(alpha = 0.6f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = typeColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = signal.distressType.title.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = typeColor
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(6.dp),
            color = if (signal.status == DistressStatus.RESPONDER_EN_ROUTE) Color(0xFF10B981).copy(alpha = 0.2f) else DangerRed.copy(alpha = 0.2f)
        ) {
            Text(
                text = if (signal.status == DistressStatus.RESPONDER_EN_ROUTE) "${signal.responderCount} RESPONDER EN ROUTE" else "ACTIVE SOS · ${signal.timeAgo}",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (signal.status == DistressStatus.RESPONDER_EN_ROUTE) Color(0xFF10B981) else DangerRed,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }

    // Rider name and bike
    Column {
        Text(
            text = signal.riderName,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "🏍️ ${signal.bikeModel}",
            style = MaterialTheme.typography.bodyMedium,
            color = AmberGold
        )
    }

    // Metric telemetry chips row
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TelemetryBadge(
            label = "DISTANCE",
            value = "%.1f km".format(signal.distanceKm),
            icon = Icons.Default.Navigation,
            modifier = Modifier.weight(1f)
        )
        TelemetryBadge(
            label = "BEARING",
            value = "${signal.bearingDeg.toInt()}° (${compassDirection(signal.bearingDeg)})",
            icon = Icons.Default.Explore,
            modifier = Modifier.weight(1f)
        )
        TelemetryBadge(
            label = "PHONE BATTERY",
            value = "${signal.batteryPercent}%",
            icon = Icons.Default.BatteryChargingFull,
            modifier = Modifier.weight(1f)
        )
        TelemetryBadge(
            label = "ALTITUDE",
            value = "${signal.altitudeM} m",
            icon = Icons.Default.Landscape,
            modifier = Modifier.weight(1f)
        )
    }

    // Description Box
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF131B2E),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "RIDER REPORT:",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF94A3B8)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = signal.description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFE2E8F0)
            )
        }
    }

    // If this is the user's own broadcast
    if (signal.isMySignal) {
        Button(
            onClick = onCancelMySos,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("cancel_my_sos_button")
        ) {
            Icon(Icons.Default.Cancel, contentDescription = null, tint = DangerRed)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Cancel My SOS Beacon", color = Color.White, fontWeight = FontWeight.Bold)
        }
    } else {
        // Volunteer response button
        val respondBtnColor by animateColorAsState(
            targetValue = if (signal.isAcknowledgedByMe) Color(0xFF10B981) else AdventureOrange,
            label = "respond_btn_color"
        )

        Button(
            onClick = onToggleRespond,
            colors = ButtonDefaults.buttonColors(containerColor = respondBtnColor),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("volunteer_respond_btn")
        ) {
            Icon(
                imageVector = if (signal.isAcknowledgedByMe) Icons.Default.CheckCircle else Icons.Default.TwoWheeler,
                contentDescription = null,
                tint = Color.Black
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (signal.isAcknowledgedByMe) "YOU'RE EN ROUTE TO HELP (ETA ~${maxOf(5, (signal.distanceKm * 2.5).toInt())} MIN)" else "I'm En Route to Help Rider",
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall
            )
        }

        // Action buttons: Direct Call & Navigate
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onCall,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("call_rider_btn")
            ) {
                Icon(Icons.Default.Phone, contentDescription = null, tint = AmberGold)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Call Rider", fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onNavigate,
                colors = ButtonDefaults.buttonColors(containerColor = HighAltitudeCyan),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("navigate_to_rider_btn")
            ) {
                Icon(Icons.Default.Directions, contentDescription = null, tint = Color.Black)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Navigate", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun RoadsideServiceDetailContent(
    service: RoadsideServiceEntity,
    onCall: () -> Unit,
    onNavigate: () -> Unit
) {
    val serviceColor = when (service.type) {
        StationType.MECHANIC -> AdventureOrange
        StationType.HOSPITAL -> DangerRed
        StationType.PUNCTURE -> AmberGold
        StationType.FUEL_STASH -> HighAltitudeCyan
        StationType.DHABA -> Color(0xFF10B981)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = serviceColor.copy(alpha = 0.2f),
            border = androidx.compose.foundation.BorderStroke(1.dp, serviceColor.copy(alpha = 0.6f))
        ) {
            Text(
                text = service.type.label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = serviceColor,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }

        if (service.isOpen24x7) {
            Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFF10B981).copy(alpha = 0.2f)) {
                Text(
                    text = "24/7 OPEN",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF10B981),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }

    Column {
        Text(
            text = service.name,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "📍 ${service.locationName}",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF94A3B8)
        )
    }

    // Metric telemetry chips
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TelemetryBadge(
            label = "DISTANCE",
            value = "%.1f km".format(service.distanceKm),
            icon = Icons.Default.Navigation,
            modifier = Modifier.weight(1f)
        )
        TelemetryBadge(
            label = "EST. ARRIVAL",
            value = "~${service.etaMinutes} mins",
            icon = Icons.Default.AccessTime,
            modifier = Modifier.weight(1f)
        )
        TelemetryBadge(
            label = "RATING",
            value = "★ ${service.rating} (${service.reviewCount})",
            icon = Icons.Default.Star,
            modifier = Modifier.weight(1f)
        )
    }

    // Specialties pills
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "SERVICES & EQUIPMENT:",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = AmberGold
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            service.specialties.take(3).forEach { spec ->
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF1E293B)
                ) {
                    Text(
                        text = spec,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFCBD5E1),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
        Text(
            text = service.description,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF94A3B8)
        )
    }

    // Action buttons: Direct Call & Navigate
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OutlinedButton(
            onClick = onCall,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .testTag("call_service_btn")
        ) {
            Icon(Icons.Default.Phone, contentDescription = null, tint = AmberGold)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Call Help", fontWeight = FontWeight.Bold)
        }

        Button(
            onClick = onNavigate,
            colors = ButtonDefaults.buttonColors(containerColor = AdventureOrange),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .testTag("navigate_to_service_btn")
        ) {
            Icon(Icons.Default.Directions, contentDescription = null, tint = Color.Black)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Navigate", color = Color.Black, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun TelemetryBadge(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF131B2E),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = AmberGold, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1)
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color(0xFF64748B), maxLines = 1)
        }
    }
}

private fun compassDirection(bearing: Float): String {
    val b = (bearing % 360 + 360) % 360
    return when {
        b >= 337.5 || b < 22.5 -> "N"
        b < 67.5 -> "NE"
        b < 112.5 -> "E"
        b < 157.5 -> "SE"
        b < 202.5 -> "S"
        b < 247.5 -> "SW"
        b < 292.5 -> "W"
        else -> "NW"
    }
}
