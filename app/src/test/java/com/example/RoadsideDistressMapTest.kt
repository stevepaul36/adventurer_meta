package com.example

import com.example.data.model.DistressStatus
import com.example.data.model.DistressType
import com.example.data.model.MapMarkerItem
import com.example.data.model.RadarFilter
import com.example.data.repository.AdventurerRepository
import com.example.viewmodel.AdventurerViewModel
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class RoadsideDistressMapTest {

    private lateinit var repository: AdventurerRepository
    private lateinit var viewModel: AdventurerViewModel

    @Before
    fun setUp() {
        repository = AdventurerRepository()
        viewModel = AdventurerViewModel(repository)
    }

    @Test
    fun testInitialDistressSignalsAndRoadsideServicesLoaded() {
        val signals = repository.distressSignals.value
        val services = repository.roadsideServices.value

        assertTrue("Distress signals should be initialized", signals.isNotEmpty())
        assertEquals(4, signals.size)

        assertTrue("Roadside services should be initialized", services.isNotEmpty())
        assertEquals(5, services.size)

        // Check first signal details
        val firstSignal = signals[0]
        assertEquals("sig-1", firstSignal.id)
        assertEquals(DistressType.PUNCTURE_BLOWOUT, firstSignal.distressType)
        assertTrue(firstSignal.distanceKm > 0)
        assertFalse(firstSignal.isMySignal)
    }

    @Test
    fun testVolunteerResponderToggle() {
        val signalId = "sig-2"
        val originalSignal = repository.distressSignals.value.find { it.id == signalId }!!
        assertEquals(0, originalSignal.responderCount)
        assertFalse(originalSignal.isAcknowledgedByMe)
        assertEquals(DistressStatus.ACTIVE_SOS, originalSignal.status)

        // Toggle to acknowledge en route
        viewModel.toggleRespondToDistress(signalId)

        val updatedSignal = repository.distressSignals.value.find { it.id == signalId }!!
        assertTrue(updatedSignal.isAcknowledgedByMe)
        assertEquals(1, updatedSignal.responderCount)
        assertEquals(DistressStatus.RESPONDER_EN_ROUTE, updatedSignal.status)

        // Toggle again to undo
        viewModel.toggleRespondToDistress(signalId)

        val revertedSignal = repository.distressSignals.value.find { it.id == signalId }!!
        assertFalse(revertedSignal.isAcknowledgedByMe)
        assertEquals(0, revertedSignal.responderCount)
        assertEquals(DistressStatus.ACTIVE_SOS, revertedSignal.status)
    }

    @Test
    fun testUserBroadcastMySosAndCancel() {
        // Initial state: no own signal
        assertNull(repository.distressSignals.value.find { it.isMySignal })

        // Broadcast SOS
        viewModel.broadcastMySos(
            distressType = DistressType.OUT_OF_FUEL,
            description = "Stalled before pass summit, fuel empty",
            phone = "+91 98888 77777"
        )

        val signals = repository.distressSignals.value
        val mySignal = signals.find { it.isMySignal }
        assertNotNull("User SOS signal must be present", mySignal)
        assertEquals(DistressType.OUT_OF_FUEL, mySignal?.distressType)
        assertEquals("Stalled before pass summit, fuel empty", mySignal?.description)
        assertEquals(0.0, mySignal?.distanceKm ?: 10.0, 0.001)

        // Selected marker should be automatically set to user signal
        val selected = viewModel.selectedMarker.value
        assertTrue(selected is MapMarkerItem.RiderDistress)
        assertTrue((selected as MapMarkerItem.RiderDistress).signal.isMySignal)

        // Cancel SOS
        viewModel.cancelMySos()
        assertNull(repository.distressSignals.value.find { it.isMySignal })
        assertNull(viewModel.selectedMarker.value)
    }

    @Test
    fun testRadarFilterCategories() {
        viewModel.setRadarFilter(RadarFilter.DISTRESS_ONLY)
        assertEquals(RadarFilter.DISTRESS_ONLY, viewModel.radarFilter.value)

        viewModel.setRadarFilter(RadarFilter.MECHANIC_TOW)
        assertEquals(RadarFilter.MECHANIC_TOW, viewModel.radarFilter.value)

        viewModel.setRadarFilter(RadarFilter.FUEL_PUNCTURE)
        assertEquals(RadarFilter.FUEL_PUNCTURE, viewModel.radarFilter.value)

        viewModel.setRadarFilter(RadarFilter.ALL)
        assertEquals(RadarFilter.ALL, viewModel.radarFilter.value)
    }

    @Test
    fun testMarkerSelectionState() {
        val signal = repository.distressSignals.value.first()
        val marker = MapMarkerItem.RiderDistress(signal)

        viewModel.selectMarker(marker)
        assertEquals(marker, viewModel.selectedMarker.value)

        viewModel.selectMarker(null)
        assertNull(viewModel.selectedMarker.value)
    }
}
