package com.example

import com.example.data.repository.AdventurerRepository
import com.example.data.model.DistressType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdventurerRepositoryTrustTest {

    @Test
    fun rescueListingsAreEmptyUntilLiveProviderIntegrationExists() {
        val repository = AdventurerRepository()

        assertTrue("Seeded SOS reports must not be presented as real incidents", repository.distressSignals.value.isEmpty())
        assertTrue("Unverified rescue stations must not be presented as verified services", repository.emergencyStations.value.isEmpty())
        assertTrue("Unverified roadside providers must not be presented as live listings", repository.roadsideServices.value.isEmpty())
    }

    @Test
    fun ownSosNoteNeverInventsLocationContactOrResponders() {
        val repository = AdventurerRepository()
        repository.broadcastMySos(DistressType.values().first(), description = "", phone = "")

        val note = repository.distressSignals.value.single { it.isMySignal }
        assertFalse("Without a GPS fix the note must not claim a location", note.hasLocation)
        assertEquals("No placeholder phone number may be invented", "", note.phoneNumber)
        assertTrue("Battery level is not measured, so it must read as unknown", note.batteryPercent < 0)
        assertEquals("Nobody is notified, so nobody can be responding", 0, note.responderCount)
        assertTrue(note.description.contains("Nobody has been notified"))

        repository.updateMySosLocation(12.9716, 77.5946, 920)
        val located = repository.distressSignals.value.single { it.isMySignal }
        assertTrue(located.hasLocation)
        assertEquals(12.9716, located.latitude, 1e-9)
        assertEquals(77.5946, located.longitude, 1e-9)
    }
}
