package com.example

import com.example.data.repository.AdventurerRepository
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
}
