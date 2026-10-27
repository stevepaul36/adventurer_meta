package com.example.data.planning

/**
 * Conservative, transparent fuel-range calculation.
 *
 * This is a planning aid, not a vehicle sensor or guarantee. The fuel level is
 * explicitly supplied by the rider; range depends on real-world conditions.
 */
enum class FuelAdviceLevel {
    UNKNOWN,
    ESTIMATE_STALE,
    COMFORTABLE,
    REFUEL_SOON,
    STOP_AND_REPLAN,
}

data class FuelProfile(
    val tankCapacityLitres: Double,
    val kilometresPerLitre: Double,
    val reserveLitres: Double,
    val safetyBufferKm: Double = 25.0,
) {
    fun isValid(): Boolean =
        tankCapacityLitres.isFinite() &&
            tankCapacityLitres > 0.0 &&
            kilometresPerLitre.isFinite() &&
            kilometresPerLitre > 0.0 &&
            reserveLitres.isFinite() &&
            reserveLitres >= 0.0 &&
            reserveLitres < tankCapacityLitres &&
            safetyBufferKm.isFinite() &&
            safetyBufferKm >= 0.0
}

data class FuelAdvice(
    val level: FuelAdviceLevel,
    val estimatedTotalRangeKm: Double? = null,
    val conservativeUsableRangeKm: Double? = null,
    val distanceToNextStopKm: Double? = null,
    val remainingAfterNextStopLitres: Double? = null,
    val message: String,
)

object FuelRangePlanner {
    /** Inputs older than this are not used to produce a confident recommendation. */
    const val MAX_INPUT_AGE_MILLIS: Long = 12 * 60 * 60 * 1000L

    fun evaluate(
        profile: FuelProfile?,
        remainingFuelLitres: Double?,
        recordedAtMillis: Long?,
        nowMillis: Long = System.currentTimeMillis(),
        distanceToNextFuelStopKm: Double? = null,
    ): FuelAdvice {
        if (profile == null || !profile.isValid()) {
            return FuelAdvice(
                level = FuelAdviceLevel.UNKNOWN,
                message = "Fuel profile is incomplete. Add tank size, expected mileage and reserve.",
            )
        }

        val fuel = remainingFuelLitres
        if (fuel == null || !fuel.isFinite() || fuel < 0.0 || fuel > profile.tankCapacityLitres) {
            return FuelAdvice(
                level = FuelAdviceLevel.UNKNOWN,
                message = "I need a valid fuel estimate from you before calculating range.",
            )
        }

        val recorded = recordedAtMillis
        if (recorded == null || recorded > nowMillis || nowMillis - recorded > MAX_INPUT_AGE_MILLIS) {
            return FuelAdvice(
                level = FuelAdviceLevel.ESTIMATE_STALE,
                estimatedTotalRangeKm = fuel * profile.kilometresPerLitre,
                conservativeUsableRangeKm = (fuel - profile.reserveLitres)
                    .coerceAtLeast(0.0) * profile.kilometresPerLitre,
                message = "Your fuel estimate is missing or old. Please update it before relying on this range.",
            )
        }

        val totalRange = fuel * profile.kilometresPerLitre
        val usableRange = (fuel - profile.reserveLitres).coerceAtLeast(0.0) *
            profile.kilometresPerLitre

        val stopDistance = distanceToNextFuelStopKm
        if (stopDistance != null && (!stopDistance.isFinite() || stopDistance < 0.0)) {
            return FuelAdvice(
                level = FuelAdviceLevel.UNKNOWN,
                estimatedTotalRangeKm = totalRange,
                conservativeUsableRangeKm = usableRange,
                message = "The next fuel-stop distance is invalid. Recheck the route.",
            )
        }

        val remainingAfterStop = stopDistance?.let {
            (fuel - it / profile.kilometresPerLitre).coerceAtLeast(0.0)
        }

        if (stopDistance != null && stopDistance + profile.safetyBufferKm > usableRange) {
            return FuelAdvice(
                level = FuelAdviceLevel.STOP_AND_REPLAN,
                estimatedTotalRangeKm = totalRange,
                conservativeUsableRangeKm = usableRange,
                distanceToNextStopKm = stopDistance,
                remainingAfterNextStopLitres = remainingAfterStop,
                message = "The next known fuel stop is beyond your conservative range with buffer. Stop safely and find a closer option or re-plan.",
            )
        }

        val nearReserve = usableRange <= profile.safetyBufferKm * 2.0 ||
            (stopDistance != null && usableRange - stopDistance <= profile.safetyBufferKm * 2.0)

        return FuelAdvice(
            level = if (nearReserve) FuelAdviceLevel.REFUEL_SOON else FuelAdviceLevel.COMFORTABLE,
            estimatedTotalRangeKm = totalRange,
            conservativeUsableRangeKm = usableRange,
            distanceToNextStopKm = stopDistance,
            remainingAfterNextStopLitres = remainingAfterStop,
            message = if (nearReserve) {
                "Plan a fuel stop soon. This is an estimate based on the fuel level and mileage you provided."
            } else {
                "Your estimate leaves a conservative range for now. Recheck fuel and route conditions regularly."
            },
        )
    }
}
