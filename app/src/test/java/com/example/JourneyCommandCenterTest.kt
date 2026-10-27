package com.example

import com.example.data.model.ActionStatus
import com.example.data.model.FuelState
import com.example.data.model.MenuItem
import com.example.viewmodel.AdventurerViewModel
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class JourneyCommandCenterTest {

    private lateinit var viewModel: AdventurerViewModel

    @Before
    fun setup() {
        viewModel = AdventurerViewModel()
    }

    @Test
    fun testFuelRangeCalculationLogic() {
        // Tank: 12L, Mileage: 25 km/L, Reserve: 2L, Buffer: 10%, Next pump: 200 km
        val fuel = FuelState(
            fuelInTankLiters = 12.0,
            mileageKmPerLiter = 25.0,
            reserveCapacityLiters = 2.0,
            safetyBufferPercent = 10.0,
            distanceToNextFuelKm = 200.0
        )

        assertEquals(10.0, fuel.usableFuel, 0.001)
        assertEquals(250.0, fuel.usableRangeKm, 0.001)
        // Safe range: 250 * (1 - 0.10) = 225 km
        assertEquals(225.0, fuel.safeRangeKm, 0.001)
        // Margin: 225 - 200 = 25 km
        assertEquals(25.0, fuel.marginKm, 0.001)
        assertTrue("Range must be safe", fuel.isSafe)

        // When next pump is 250 km (beyond safe range)
        val criticalFuel = fuel.copy(distanceToNextFuelKm = 250.0)
        assertEquals(-25.0, criticalFuel.marginKm, 0.001)
        assertFalse("Should warn unsafe", criticalFuel.isSafe)
    }

    @Test
    fun testUpdateFuelCalculatorViaViewModel() {
        viewModel.updateFuelCalculator(
            tankLiters = 15.0,
            mileage = 20.0,
            reserve = 3.0,
            bufferPercent = 15.0,
            distanceToNext = 150.0
        )

        val updated = viewModel.fuelState.value
        assertEquals(15.0, updated.fuelInTankLiters, 0.001)
        assertEquals(12.0, updated.usableFuel, 0.001)
        // 12 * 20 = 240, safe = 240 * 0.85 = 204
        assertEquals(204.0, updated.safeRangeKm, 0.001)
        assertTrue(updated.isSafe)
    }

    @Test
    fun testAddEditDeleteJourneyStops() {
        val initialCount = viewModel.journeyStops.value.size
        assertTrue(initialCount >= 3)

        // Add stop
        viewModel.addJourneyStop(
            name = "Test Alpine Pass",
            km = 95.0,
            etaMin = 120,
            altitudeM = 4500,
            reminder = "Check thermal layers"
        )

        val afterAdd = viewModel.journeyStops.value
        assertEquals(initialCount + 1, afterAdd.size)
        val added = afterAdd.last()
        assertEquals("Test Alpine Pass", added.name)
        assertFalse(added.isVisited)

        // Edit stop
        viewModel.editJourneyStop(
            id = added.id,
            name = "Test Alpine Pass (Summit)",
            km = 98.0,
            etaMin = 130,
            altitudeM = 4600,
            reminder = "Drink water"
        )
        val edited = viewModel.journeyStops.value.find { it.id == added.id }!!
        assertEquals("Test Alpine Pass (Summit)", edited.name)
        assertEquals(98.0, edited.distanceFromStartKm, 0.001)

        // Toggle visited
        viewModel.toggleStopVisited(added.id)
        val toggled = viewModel.journeyStops.value.find { it.id == added.id }!!
        assertTrue(toggled.isVisited)
        assertEquals(ActionStatus.VERIFIED_COMPLETE, toggled.status)

        // Delete stop
        viewModel.deleteJourneyStop(added.id)
        assertNull(viewModel.journeyStops.value.find { it.id == added.id })
        assertEquals(initialCount, viewModel.journeyStops.value.size)
    }

    @Test
    fun testFoodCartAndSimulationOrderLifecycle() {
        val item1 = MenuItem("test-m1", "Himalayan Thukpa", "Noodle soup", 140)
        val item2 = MenuItem("test-m2", "Butter Tea", "Warm tea", 60)

        // Add to cart
        viewModel.addToCart(item1)
        viewModel.addToCart(item2)
        viewModel.addToCart(item1) // increase quantity of item1

        val cart = viewModel.cartItems.value
        assertEquals(2, cart.size)
        val cartItem1 = cart.find { it.menuItem.id == item1.id }!!
        assertEquals(2, cartItem1.quantity)

        // Quantity decrement
        viewModel.updateCartQuantity(item1.id, -1)
        assertEquals(1, viewModel.cartItems.value.find { it.menuItem.id == item1.id }!!.quantity)

        // Submit demo order
        viewModel.submitDemoOrder("Chandra Dhaba", "Zingzingbar Halt")
        val order = viewModel.activeDemoOrder.value
        assertNotNull(order)
        assertTrue("Must be flagged as simulation", order!!.isSimulation)
        assertEquals(ActionStatus.AWAITING_CONFIRMATION, order.status)
        assertEquals(200, order.subtotalInr) // 140 + 60

        // Explicit confirmation
        viewModel.confirmDemoOrder()
        val confirmedOrder = viewModel.activeDemoOrder.value!!
        assertEquals(ActionStatus.VERIFIED_COMPLETE, confirmedOrder.status)
        assertTrue("Cart should be cleared after confirmation", viewModel.cartItems.value.isEmpty())

        // Cancel order
        viewModel.cancelDemoOrder()
        assertNull(viewModel.activeDemoOrder.value)
    }

    @Test
    fun testAddAndDeleteEditorialMemories() {
        val initialCount = viewModel.journeyMemories.value.size

        viewModel.addJourneyMemory(
            title = "Testing the Glacier Crossing",
            location = "North Face · 4,200m",
            altitudeM = 4200,
            reflection = "The gravel trail narrowed to a single track between ice walls.",
            moodTag = "Pure Adrenaline ⚡"
        )

        val memories = viewModel.journeyMemories.value
        assertEquals(initialCount + 1, memories.size)
        val newMem = memories.first()
        assertEquals("Testing the Glacier Crossing", newMem.title)

        viewModel.deleteJourneyMemory(newMem.id)
        assertEquals(initialCount, viewModel.journeyMemories.value.size)
    }

    @Test
    fun testVoiceDockSpeakingAndPromptState() {
        viewModel.triggerVoiceDockPrompt("Checking road conditions ahead.", ActionStatus.SUGGESTED)
        assertTrue(viewModel.voiceDockIsSpeaking.value)
        assertEquals("Checking road conditions ahead.", viewModel.voiceDockStateText.value)
        assertEquals(ActionStatus.SUGGESTED, viewModel.voiceDockActionStatus.value)

        viewModel.stopVoiceDockSpeaking()
        assertFalse(viewModel.voiceDockIsSpeaking.value)
    }
}
