package com.example.data.model

import androidx.compose.ui.graphics.Color

enum class DistressType(
    val title: String,
    val subtitle: String,
    val badgeColorHex: Long
) {
    CRASH_ALERT("Crash / Fall Detected", "Immediate assistance requested", 0xFFEF4444),
    PUNCTURE_BLOWOUT("Tyre Blowout / Flat", "Needs tubeless plug or 21\" tube", 0xFFF59E0B),
    MECHANICAL_BREAKDOWN("Engine / Chain Breakdown", "Chain snapped or clutch cable cut", 0xFFFB923C),
    OUT_OF_FUEL("Out of Fuel", "Stranded with empty tank", 0xFF06B6D4),
    MEDICAL_AMS("Medical / AMS / Hypoxia", "Acute mountain sickness or oxygen needed", 0xFFDC2626),
    BATTERY_DEAD("Sub-Zero Battery Drain", "Engine won't crank, needs jump start", 0xFF60A5FA)
}

enum class DistressStatus {
    ACTIVE_SOS,
    RESPONDER_EN_ROUTE,
    RESOLVED
}

data class RiderDistressSignal(
    val id: String,
    val riderName: String,
    val bikeModel: String,
    val distressType: DistressType,
    val status: DistressStatus = DistressStatus.ACTIVE_SOS,
    val latitude: Double,
    val longitude: Double,
    val distanceKm: Double,
    val bearingDeg: Float,
    val altitudeM: Int,
    val batteryPercent: Int,
    val timeAgo: String,
    val description: String,
    val phoneNumber: String,
    val responderCount: Int = 0,
    val isMySignal: Boolean = false,
    val isAcknowledgedByMe: Boolean = false
)

data class RoadsideServiceEntity(
    val id: String,
    val name: String,
    val type: StationType,
    val latitude: Double,
    val longitude: Double,
    val distanceKm: Double,
    val bearingDeg: Float,
    val locationName: String,
    val phone: String,
    val rating: Double,
    val reviewCount: Int,
    val isOpen24x7: Boolean,
    val etaMinutes: Int,
    val specialties: List<String>,
    val description: String
)

sealed interface MapMarkerItem {
    val id: String
    val distanceKm: Double
    val bearingDeg: Float
    val latitude: Double
    val longitude: Double

    data class RiderDistress(val signal: RiderDistressSignal) : MapMarkerItem {
        override val id: String get() = signal.id
        override val distanceKm: Double get() = signal.distanceKm
        override val bearingDeg: Float get() = signal.bearingDeg
        override val latitude: Double get() = signal.latitude
        override val longitude: Double get() = signal.longitude
    }

    data class RoadsideService(val service: RoadsideServiceEntity) : MapMarkerItem {
        override val id: String get() = service.id
        override val distanceKm: Double get() = service.distanceKm
        override val bearingDeg: Float get() = service.bearingDeg
        override val latitude: Double get() = service.latitude
        override val longitude: Double get() = service.longitude
    }
}

enum class RadarFilter(val label: String) {
    ALL("All Assistance & SOS"),
    DISTRESS_ONLY("Riders in Distress"),
    MECHANIC_TOW("Mechanics & Towing"),
    FUEL_PUNCTURE("Fuel & Tyres"),
    MEDICAL_SHELTER("Medical & Shelter")
}
