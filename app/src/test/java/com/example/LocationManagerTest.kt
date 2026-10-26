package com.example

import com.example.data.location.RideLocation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LocationManagerTest {

    @Test
    fun testRideLocationSpeedConversion() {
        val speedMps = 27.7778 // ~100 km/h
        val calculatedKmH = speedMps * 3.6
        assertEquals(100.0, calculatedKmH, 0.1)

        val loc = RideLocation(
            latitude = 32.5512,
            longitude = 77.1724,
            speedKmH = calculatedKmH,
            altitudeM = 4280.0,
            bearingDeg = 45f,
            accuracyM = 3.5f,
            distanceDeltaM = 27.7f
        )

        assertEquals(32.5512, loc.latitude, 0.0001)
        assertEquals(77.1724, loc.longitude, 0.0001)
        assertEquals(100.0, loc.speedKmH, 0.1)
        assertEquals(4280.0, loc.altitudeM, 0.1)
        assertEquals(45f, loc.bearingDeg)
        assertTrue(loc.distanceDeltaM > 0)
    }

    @Test
    fun testRideTelemetryWithGpsCoordinates() {
        val repo = com.example.data.repository.AdventurerRepository()
        repo.startRide()

        val loc = RideLocation(
            latitude = 34.1526,
            longitude = 77.5771,
            speedKmH = 65.5,
            altitudeM = 5359.0,
            bearingDeg = 120f,
            accuracyM = 2.0f,
            distanceDeltaM = 150f
        )

        repo.updateFromGpsLocation(loc)
        val telemetry = repo.telemetry.value

        assertEquals(65.5, telemetry.currentSpeedKmH, 0.1)
        assertEquals(34.1526, telemetry.latitude, 0.0001)
        assertEquals(77.5771, telemetry.longitude, 0.0001)
        assertEquals(5359, telemetry.altitudeM)
        assertTrue(telemetry.hasGpsFix)
    }
}
