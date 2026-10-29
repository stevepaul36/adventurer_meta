package com.example.data.market

import android.content.Context
import com.example.data.providers.AdventureMarket
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

data class AdventureMarketContext(
    val market: AdventureMarket = AdventureMarket.CANADA,
    val region: String = AdventureMarketSettings.defaultRegionFor(AdventureMarket.CANADA),
)

object AdventureMarketSettings {
    private const val PREFS = "adventurer_market_preferences"
    private const val KEY_MARKET = "selected_market"
    private const val KEY_REGION = "selected_region"

    private val mutableState = MutableStateFlow(AdventureMarketContext())
    val state: StateFlow<AdventureMarketContext> = mutableState.asStateFlow()
    @Volatile private var initialized = false

    fun regionsFor(market: AdventureMarket): List<String> = when (market) {
        AdventureMarket.INDIA -> listOf(
            "Tamil Nadu", "Kerala", "Karnataka", "Maharashtra", "Himachal Pradesh",
            "Uttarakhand", "Goa", "Delhi", "Rajasthan", "Other / not listed"
        )
        AdventureMarket.CANADA -> listOf(
            "Ontario", "Quebec", "British Columbia", "Alberta", "Manitoba",
            "Saskatchewan", "Nova Scotia", "New Brunswick", "Prince Edward Island",
            "Newfoundland and Labrador", "Yukon", "Northwest Territories", "Nunavut",
            "Other / not listed"
        )
        AdventureMarket.UNITED_STATES -> listOf(
            "California", "New York", "Colorado", "Texas", "Washington", "Utah",
            "Alaska", "Hawaii", "Other / not listed"
        )
    }

    fun defaultRegionFor(market: AdventureMarket): String =
        when (market) {
            AdventureMarket.INDIA -> "Tamil Nadu"
            AdventureMarket.CANADA -> "Ontario"
            AdventureMarket.UNITED_STATES -> "California"
        }

    fun defaultsLabelFor(market: AdventureMarket): String = when (market) {
        AdventureMarket.INDIA -> "INR · km · °C"
        AdventureMarket.CANADA -> "CAD · km · °C"
        AdventureMarket.UNITED_STATES -> "USD · mi · °F"
    }

    @Synchronized
    fun initialize(context: Context) {
        if (initialized) return
        val preferences = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val market = runCatching {
            preferences.getString(KEY_MARKET, null)?.let(AdventureMarket::valueOf)
                ?: AdventureMarket.suggestedFromCountryCode(Locale.getDefault().country)
                ?: AdventureMarket.CANADA
        }.getOrDefault(AdventureMarket.CANADA)
        val savedRegion = preferences.getString(KEY_REGION, null)
        val region = savedRegion?.takeIf { it in regionsFor(market) } ?: defaultRegionFor(market)
        mutableState.value = AdventureMarketContext(market, region)
        initialized = true
    }

    @Synchronized
    fun setMarket(context: Context, market: AdventureMarket) {
        val currentRegion = mutableState.value.region
        val region = currentRegion.takeIf { it in regionsFor(market) } ?: defaultRegionFor(market)
        persist(context, market, region)
    }

    @Synchronized
    fun setRegion(context: Context, region: String) {
        val market = mutableState.value.market
        if (region !in regionsFor(market)) return
        persist(context, market, region)
    }

    private fun persist(context: Context, market: AdventureMarket, region: String) {
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_MARKET, market.name)
            .putString(KEY_REGION, region)
            .apply()
        mutableState.value = AdventureMarketContext(market, region)
        initialized = true
    }
}
