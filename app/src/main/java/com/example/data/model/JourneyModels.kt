package com.example.data.model

import java.util.UUID

/**
 * Explicit action state for the Adventure Copilot and Command Center.
 * Distinguishes suggestions, unconfirmed actions, handoffs, and verifications.
 */
enum class ActionStatus(val label: String, val badgeColorHex: Long) {
    SUGGESTED("Suggested", 0xFF38BDF8), // Cyan/Blue
    AWAITING_CONFIRMATION("Awaiting Confirmation", 0xFFF59E0B), // Amber
    HANDED_OFF("Handed Off", 0xFFA855F7), // Purple
    /** Stored locally on this phone (stops, memories, notes). Not confirmed by any provider. */
    SAVED_ON_DEVICE("Saved on device", 0xFF94A3B8), // Slate
    /** The rider ticked this off themselves. */
    CHECKED_OFF("Checked off by you", 0xFF22C55E), // Light green
    /** Prototype flow — nothing real happened (no order, booking or dispatch). */
    SIMULATED("Simulation only", 0xFF64748B), // Grey
    /** Only for outcomes proven by a provider receipt/status. Nothing in this build produces it yet. */
    VERIFIED_COMPLETE("Verified Complete", 0xFF10B981), // Green
    UNCERTAIN("Uncertain", 0xFFEF4444) // Red
}

enum class DiscoveryCategory(val displayName: String, val icon: String) {
    RESTAURANTS("Food & Chai", "restaurant"),
    FUEL("Fuel Stations", "local_gas_station"),
    STAYS("Alpine Stays", "hotel"),
    VIEWPOINTS("Vistas & Passes", "landscape")
}

data class MenuItem(
    val id: String,
    val name: String,
    val description: String,
    val priceInr: Int,
    val isVeg: Boolean = true,
    val prepTimeMin: Int = 15
)

data class DiscoveryVenue(
    val id: String,
    val name: String,
    val category: DiscoveryCategory,
    val distanceKm: Double,
    val rating: Double,
    val altitudeM: Int,
    val description: String,
    val amenities: List<String>,
    val menuItems: List<MenuItem> = emptyList(),
    val isVerifiedOpen: Boolean = true,
    val locationDescription: String = "NH-3 Manali-Leh Highway"
)

data class CartItem(
    val menuItem: MenuItem,
    val quantity: Int = 1
)

data class DemoFoodOrder(
    val id: String = UUID.randomUUID().toString(),
    val venueName: String,
    val items: List<CartItem>,
    val subtotalInr: Int,
    val deliveryHalt: String,
    val status: ActionStatus = ActionStatus.AWAITING_CONFIRMATION,
    val orderTime: String = "Just now",
    val isSimulation: Boolean = true
)

data class FuelState(
    val fuelInTankLiters: Double = 11.5,
    val mileageKmPerLiter: Double = 26.0,
    val reserveCapacityLiters: Double = 2.5,
    val safetyBufferPercent: Double = 15.0,
    val distanceToNextFuelKm: Double = 142.0
) {
    val usableFuel: Double get() = (fuelInTankLiters - reserveCapacityLiters).coerceAtLeast(0.0)
    val usableRangeKm: Double get() = usableFuel * mileageKmPerLiter
    val safeRangeKm: Double get() = usableRangeKm * (1.0 - (safetyBufferPercent / 100.0).coerceIn(0.0, 0.5))
    val marginKm: Double get() = safeRangeKm - distanceToNextFuelKm
    val isSafe: Boolean get() = marginKm >= 0.0
}

data class JourneyStop(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val distanceFromStartKm: Double,
    val etaMinutes: Int,
    val altitudeM: Int,
    val reminderNote: String,
    val isVisited: Boolean = false,
    val status: ActionStatus = ActionStatus.SUGGESTED
)

data class JourneyMemory(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val locationName: String,
    val altitudeM: Int,
    val timestamp: String,
    val reflection: String,
    val moodTag: String,
    val gradientColorsHex: List<Long> = listOf(0xFFF97316, 0xFFEAB308)
)
