package com.example.data.market

import android.content.Context
import com.example.data.providers.AdventureMarket
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

data class AdventureMarketContext(
    val market: AdventureMarket = AdventureMarket.CANADA,
    val region: String = "Ontario",
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
            "Andhra Pradesh", "Arunachal Pradesh", "Assam", "Bihar", "Chhattisgarh",
            "Goa", "Gujarat", "Haryana", "Himachal Pradesh", "Jharkhand", "Karnataka",
            "Kerala", "Madhya Pradesh", "Maharashtra", "Manipur", "Meghalaya", "Mizoram",
            "Nagaland", "Odisha", "Punjab", "Rajasthan", "Sikkim", "Tamil Nadu",
            "Telangana", "Tripura", "Uttar Pradesh", "Uttarakhand", "West Bengal",
            "Andaman and Nicobar Islands", "Chandigarh", "Dadra and Nagar Haveli and Daman and Diu",
            "Delhi", "Jammu and Kashmir", "Ladakh", "Lakshadweep", "Puducherry", "Other / not listed"
        )
        AdventureMarket.CANADA -> listOf(
            "Ontario", "Quebec", "British Columbia", "Alberta", "Manitoba",
            "Saskatchewan", "Nova Scotia", "New Brunswick", "Prince Edward Island",
            "Newfoundland and Labrador", "Yukon", "Northwest Territories", "Nunavut",
            "Other / not listed"
        )
        AdventureMarket.UNITED_STATES -> listOf(
            "Alabama", "Alaska", "Arizona", "Arkansas", "California", "Colorado",
            "Connecticut", "Delaware", "District of Columbia", "Florida", "Georgia",
            "Hawaii", "Idaho", "Illinois", "Indiana", "Iowa", "Kansas", "Kentucky",
            "Louisiana", "Maine", "Maryland", "Massachusetts", "Michigan", "Minnesota",
            "Mississippi", "Missouri", "Montana", "Nebraska", "Nevada", "New Hampshire",
            "New Jersey", "New Mexico", "New York", "North Carolina", "North Dakota",
            "Ohio", "Oklahoma", "Oregon", "Pennsylvania", "Rhode Island", "South Carolina",
            "South Dakota", "Tennessee", "Texas", "Utah", "Vermont", "Virginia",
            "Washington", "West Virginia", "Wisconsin", "Wyoming", "American Samoa",
            "Guam", "Northern Mariana Islands", "Puerto Rico", "U.S. Virgin Islands",
            "Other / not listed"
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
