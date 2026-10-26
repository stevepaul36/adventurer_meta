package com.example.data.model

data class ExpeditionCrew(
    val leader: String,
    val safetyOfficer: String,
    val mechanic: String,
    val photographer: String
)

data class ExpeditionRoute(
    val id: String,
    val title: String,
    val region: String,
    val state: String,
    val distanceKm: Int,
    val durationDays: String,
    val highestPass: String,
    val altitudeFt: Int,
    val difficulty: String, // "Moderate", "Challenging", "Extreme"
    val isWomenFocused: Boolean = false,
    val planA: String,
    val planB: String,
    val safetyScore: Double,
    val terrain: String,
    val landmarks: List<String>,
    val recommendedBike: String,
    val oxygenAlert: Boolean = false,
    val monsoonAlert: Boolean = false,
    val imageUrl: String,
    val roles: ExpeditionCrew,
    val isBookmarked: Boolean = false
)

enum class StationType(val label: String) {
    MECHANIC("Certified Mechanic"),
    PUNCTURE("24/7 Tyre Puncture"),
    FUEL_STASH("High-Altitude Fuel"),
    HOSPITAL("Medical Clinic / Oxygen"),
    DHABA("Highway Food / Shelter")
}

data class EmergencyStation(
    val id: String,
    val name: String,
    val type: StationType,
    val location: String,
    val distanceKm: Double,
    val phone: String,
    val rating: Double,
    val isOpen24x7: Boolean,
    val specialNotes: String
)

data class MountainSafetyTip(
    val id: String,
    val title: String,
    val category: String, // "AMS", "Cold Weather", "River Crossing", "Breakdown"
    val shortAdvice: String,
    val fullInstructions: String
)

data class TripBudgetEstimate(
    val distanceKm: Int,
    val bikeMileageKmpl: Double = 28.0,
    val fuelRatePerLiterInr: Double = 104.0,
    val stayPerNightInr: Double = 1800.0,
    val foodPerDayInr: Double = 750.0,
    val permitCostInr: Double = 1200.0,
    val sparePartsBufferInr: Double = 4500.0,
    val daysCount: Int = 8
) {
    val totalFuelLiters: Double get() = distanceKm / bikeMileageKmpl
    val totalFuelCostInr: Double get() = totalFuelLiters * fuelRatePerLiterInr
    val totalStayCostInr: Double get() = stayPerNightInr * daysCount
    val totalFoodCostInr: Double get() = foodPerDayInr * daysCount
    val totalEstimateInr: Double
        get() = totalFuelCostInr + totalStayCostInr + totalFoodCostInr + permitCostInr + sparePartsBufferInr
}
