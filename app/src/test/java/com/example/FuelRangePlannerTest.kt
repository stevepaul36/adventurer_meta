package com.example

import com.example.data.planning.FuelAdviceLevel
import com.example.data.planning.FuelProfile
import com.example.data.planning.FuelRangePlanner
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class FuelRangePlannerTest {
    private val now = 1_800_000_000_000L
    private val profile = FuelProfile(
        tankCapacityLitres = 15.0,
        kilometresPerLitre = 30.0,
        reserveLitres = 2.0,
        safetyBufferKm = 25.0,
    )

    @Test
    fun usesReserveAndBufferForConservativeRange() {
        val advice = FuelRangePlanner.evaluate(profile, 5.0, now, now, 50.0)
        assertEquals(150.0, advice.estimatedTotalRangeKm!!, 0.001)
        assertEquals(90.0, advice.conservativeUsableRangeKm!!, 0.001)
        assertEquals(FuelAdviceLevel.COMFORTABLE, advice.level)
    }

    @Test
    fun recommendsReplanningWhenStopExceedsUsableRangeWithBuffer() {
        val advice = FuelRangePlanner.evaluate(profile, 4.0, now, now, 70.0)
        assertEquals(FuelAdviceLevel.STOP_AND_REPLAN, advice.level)
        assertNotNull(advice.message)
    }

    @Test
    fun requestsFreshEstimateWhenInputIsOld() {
        val advice = FuelRangePlanner.evaluate(
            profile = profile,
            remainingFuelLitres = 5.0,
            recordedAtMillis = now - FuelRangePlanner.MAX_INPUT_AGE_MILLIS - 1,
            nowMillis = now,
            distanceToNextFuelStopKm = 10.0,
        )
        assertEquals(FuelAdviceLevel.ESTIMATE_STALE, advice.level)
    }

    @Test
    fun rejectsFuelLevelAboveTankCapacity() {
        val advice = FuelRangePlanner.evaluate(profile, 16.0, now, now)
        assertEquals(FuelAdviceLevel.UNKNOWN, advice.level)
    }

    @Test
    fun missingRouteDistanceDoesNotInventStationAvailabilityOrEta() {
        val advice = FuelRangePlanner.evaluate(profile, 5.0, now, now)
        assertEquals(FuelAdviceLevel.COMFORTABLE, advice.level)
        assertEquals(null, advice.distanceToNextStopKm)
    }

    @Test
    fun invalidProfileIsRejected() {
        val advice = FuelRangePlanner.evaluate(
            profile = FuelProfile(10.0, 0.0, 1.0),
            remainingFuelLitres = 4.0,
            recordedAtMillis = now,
            nowMillis = now,
        )
        assertEquals(FuelAdviceLevel.UNKNOWN, advice.level)
    }
}
