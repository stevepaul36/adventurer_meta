package com.example

import com.example.data.market.AdventureMarketSettings
import com.example.data.providers.AdventureMarket
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AdventureMarketSettingsTest {
    @Test
    fun defaultRegionsMatchEachSupportedMarket() {
        assertEquals("Tamil Nadu", AdventureMarketSettings.defaultRegionFor(AdventureMarket.INDIA))
        assertEquals("Ontario", AdventureMarketSettings.defaultRegionFor(AdventureMarket.CANADA))
        assertEquals("California", AdventureMarketSettings.defaultRegionFor(AdventureMarket.UNITED_STATES))
        AdventureMarket.values().forEach { market ->
            assertTrue(
                "Default region must be included for ${market.countryName}",
                AdventureMarketSettings.regionsFor(market).contains(AdventureMarketSettings.defaultRegionFor(market))
            )
        }
    }

    @Test
    fun marketDefaultsShowCorrectCurrencyAndUnits() {
        assertEquals("INR · km · °C", AdventureMarketSettings.defaultsLabelFor(AdventureMarket.INDIA))
        assertEquals("CAD · km · °C", AdventureMarketSettings.defaultsLabelFor(AdventureMarket.CANADA))
        assertEquals("USD · mi · °F", AdventureMarketSettings.defaultsLabelFor(AdventureMarket.UNITED_STATES))
    }
}
