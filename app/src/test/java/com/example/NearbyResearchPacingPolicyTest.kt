package com.example

import com.example.data.voice.NearbyResearchPacingPolicy
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NearbyResearchPacingPolicyTest {
    @Test
    fun researchesTheFirstUsableAreaAfterTheCooldown() {
        assertTrue(NearbyResearchPacingPolicy.shouldRefresh(false, 0.0, 0L, 60_000L))
        assertFalse(NearbyResearchPacingPolicy.shouldRefresh(false, 0.0, 0L, 10_000L))
    }

    @Test
    fun movementRefreshesContextButGpsJitterDoesNot() {
        assertFalse(NearbyResearchPacingPolicy.shouldRefresh(true, 45.0, 60_000L, 90_000L))
        assertTrue(NearbyResearchPacingPolicy.shouldRefresh(true, 160.0, 60_000L, 90_000L))
    }

    @Test
    fun staleContextRefreshesEvenWhenTheUserHasPaused() {
        assertTrue(NearbyResearchPacingPolicy.shouldRefresh(true, 0.0, 180_000L, 180_000L))
        assertFalse(NearbyResearchPacingPolicy.shouldRefresh(true, 0.0, 90_000L, 90_000L))
    }
}
