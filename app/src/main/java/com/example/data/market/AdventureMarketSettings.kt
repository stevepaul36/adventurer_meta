package com.example.data.market

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AdventureMarket(
    val id: String,
    val label: String,
    val defaultsLabel: String,
    val defaultRegion: String,
    val regions: List<String>,
) {
    INDIA("IN", "India", "INR · km · °C", "Tamil Nadu", listOf(
        "Tamil Nadu", "Kerala", "Karnataka", "Maharashtra", "Himachal Pradesh",
        "Uttarakhand", "Goa", "Delhi", "Rajasthan", "Other / not listed"
    )),
    CANADA("CA", "Canada", "CAD · km · °C", "Ontario", listOf(
        "Ontario", "Quebec", "British Columbia", "Alberta", "Manitoba",
        "Saskatchewan", "Nova Scotia", "New Brunswick", "Prince Edward Island",
        "Newfoundland and Labrador", "Yukon", "Northwest Territories", "Nunavut",
        "Other / not listed"
    )),
    UNITED_STATES("US", "United States", "USD · mi · °F", "California", listOf(
        "California", "New York", "Colorado", "Texas", "Washington", "Utah",
        "Alaska", "Hawaii", "Other / not listed"
    ));

    companion object {
        fun fromId(id: String?): AdventureMarket =
            entries.firstOrNull { it.id == id } ?: CANADA
    }
}

data class AdventureMarketContext(
    val market: AdventureMarket = AdventureMarket.CANADA,
    val region: String = AdventureMarket.CANADA.defaultRegion,
)

object AdventureMarketSettings {
    private const val PREFS = "adventurer_market_context"
    private const val KEY_MARKET = "market_id"
    private const val KEY_REGION = "region"
    private val mutableState = MutableStateFlow(AdventureMarketContext())
    val state: StateFlow<AdventureMarketContext> = mutableState.asStateFlow()
    @Volatile private var initialized = false

    @Synchronized
    fun initialize(context: Context) {
        if (initialized) return
        val preferences = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val market = AdventureMarket.fromId(preferences.getString(KEY_MARKET, null))
        val savedRegion = preferences.getString(KEY_REGION, null)
        val region = savedRegion?.takeIf { it in market.regions } ?: market.defaultRegion
        mutableState.value = AdventureMarketContext(market, region)
        initialized = true
    }

    @Synchronized
    fun setMarket(context: Context, market: AdventureMarket) {
        val currentRegion = mutableState.value.region
        val region = currentRegion.takeIf { it in market.regions } ?: market.defaultRegion
        persist(context, market, region)
    }

    @Synchronized
    fun setRegion(context: Context, region: String) {
        val market = mutableState.value.market
        if (region !in market.regions) return
        persist(context, market, region)
    }

    private fun persist(context: Context, market: AdventureMarket, region: String) {
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_MARKET, market.id)
            .putString(KEY_REGION, region)
            .apply()
        mutableState.value = AdventureMarketContext(market, region)
        initialized = true
    }
}
