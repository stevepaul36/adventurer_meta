package com.example

import com.example.data.providers.AdventureMarket
import com.example.data.providers.RegionalProviderCatalog
import com.example.data.providers.RegionalServiceCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RegionalProviderCatalogTest {
    @Test
    fun suggestsMarketFromCountryCodeWithoutRequiringLocationPermission() {
        assertEquals(AdventureMarket.INDIA, AdventureMarket.suggestedFromCountryCode("in"))
        assertEquals(AdventureMarket.CANADA, AdventureMarket.suggestedFromCountryCode("CA"))
        assertEquals(AdventureMarket.UNITED_STATES, AdventureMarket.suggestedFromCountryCode("us"))
        assertEquals(null, AdventureMarket.suggestedFromCountryCode("GB"))
        assertEquals(null, AdventureMarket.suggestedFromCountryCode(null))
    }

    @Test
    fun indiaFoodProvidersIncludeCommonExternalHandoffs() {
        val names = RegionalProviderCatalog
            .forCategory(AdventureMarket.INDIA, RegionalServiceCategory.FOOD)
            .map { it.name }
        assertTrue(names.contains("Swiggy"))
        assertTrue(names.contains("Zomato"))
    }

    @Test
    fun canadaOutdoorProvidersIncludeOfficialAndTrailDiscoverySources() {
        val providers = RegionalProviderCatalog.forCategory(
            AdventureMarket.CANADA,
            RegionalServiceCategory.OUTDOORS,
        )
        assertTrue(providers.any { it.name == "Parks Canada" })
        assertTrue(providers.any { it.name == "AllTrails" })
        assertTrue(providers.all { it.websiteUrl.startsWith("https://") })
    }

    @Test
    fun catalogKeepsMarketsDistinctAndProviderIdsUnique() {
        val india = RegionalProviderCatalog.forMarket(AdventureMarket.INDIA)
        val canada = RegionalProviderCatalog.forMarket(AdventureMarket.CANADA)
        val unitedStates = RegionalProviderCatalog.forMarket(AdventureMarket.UNITED_STATES)
        assertTrue(india.any { it.id == "irctc" })
        assertFalse(canada.any { it.id == "irctc" })
        assertTrue(india.none { it.id == "parks-canada" })
        assertTrue(unitedStates.any { it.id == "nps-us" })
        assertTrue(unitedStates.any { it.id == "recreation-gov-us" })
        assertTrue(unitedStates.none { it.id == "parks-canada" })
        val allProviders = india + canada + unitedStates
        assertEquals(allProviders.size, allProviders.map { it.id }.distinct().size)
        assertTrue(allProviders.all { it.websiteUrl.startsWith("https://") })
        assertNotNull(RegionalProviderCatalog.find("parks-reservations-ca"))
    }

    @Test
    fun mapsSearchUrlEncodesInputAndNamesTheSelectedMarket() {
        val url = RegionalProviderCatalog.googleMapsSearchUrl(
            query = "EV charger & coffee",
            market = AdventureMarket.CANADA,
        )
        assertTrue(url.startsWith("https://www.google.com/maps/search/?api=1&query="))
        assertTrue(url.contains("EV+charger+%26+coffee+in+Canada"))
        assertFalse(url.contains(" "))
    }

    @Test
    fun directionsUrlSupportsOriginAndRejectsBlankDestination() {
        val url = RegionalProviderCatalog.googleMapsDirectionsUrl(
            destination = "Banff National Park",
            market = AdventureMarket.CANADA,
            origin = "Toronto, ON",
        )
        assertTrue(url.contains("api=1"))
        assertTrue(url.contains("destination=Banff+National+Park%2C+Canada"))
        assertTrue(url.contains("origin=Toronto%2C+ON"))

        try {
            RegionalProviderCatalog.googleMapsDirectionsUrl(" ", AdventureMarket.INDIA)
            throw AssertionError("A blank destination must be rejected.")
        } catch (_: IllegalArgumentException) {
            // Expected: never create a misleading empty Maps handoff.
        }
    }

}
