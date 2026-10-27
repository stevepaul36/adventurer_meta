package com.example

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
    fun testNoUnverifiedRescueDataIsLoadedAtStartup() {
        assertTrue("No fabricated SOS reports should appear before a live provider is connected", repository.distressSignals.value.isEmpty())
        assertTrue("No unverified roadside services should appear before a live provider is connected", repository.roadsideServices.value.isEmpty())
        assertTrue("No unverified emergency stations should appear before a live provider is connected", repository.emergencyStations.value.isEmpty())
    }

    @Test
    fun testResponderToggleDoesNotInventIncidentWhenNoLiveSignalExists() {
        viewModel.toggleRespondToDistress("sig-2")
        assertTrue(repository.distressSignals.value.isEmpty())
    }

    @Test
    fun testUserCreatesAndCancelsLocalSosNote() {
        assertNull(repository.distressSignals.value.find { it.isMySignal })

        viewModel.broadcastMySos(
            distressType = DistressType.OUT_OF_FUEL,
            description = "Stalled before pass summit, fuel empty",
            phone = "+91 98888 77777"
        )

        val signals = repository.distressSignals.value
        val mySignal = signals.find { it.isMySignal }
        assertNotNull("Local SOS note must be present", mySignal)
        assertEquals(DistressType.OUT_OF_FUEL, mySignal?.distressType)
        assertEquals("Stalled before pass summit, fuel empty", mySignal?.description)
        assertEquals(0.0, mySignal?.distanceKm ?: 10.0, 0.001)

        val selected = viewModel.selectedMarker.value
        assertTrue(selected is MapMarkerItem.RiderDistress)
        assertTrue((selected as MapMarkerItem.RiderDistress).signal.isMySignal)

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
        viewModel.broadcastMySos(
            distressType = DistressType.PUNCTURE_BLOWOUT,
            description = "Test marker",
            phone = ""
        )
        val signal = repository.distressSignals.value.first()
        val marker = MapMarkerItem.RiderDistress(signal)

        viewModel.selectMarker(marker)
        assertEquals(marker, viewModel.selectedMarker.value)

        viewModel.selectMarker(null)
        assertNull(viewModel.selectedMarker.value)
    }
}
