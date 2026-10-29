package com.example.data.providers

import java.net.URLEncoder

/**
 * User-selected market context. This is a preference, not a location fix.
 * Users can always override the device-locale suggestion.
 */
enum class AdventureMarket(
    val countryName: String,
    val countryCode: String,
    val currencyCode: String,
    val distanceUnit: String,
    val temperatureUnit: String,
) {
    INDIA("India", "IN", "INR", "km", "°C"),
    CANADA("Canada", "CA", "CAD", "km", "°C"),
    UNITED_STATES("United States", "US", "USD", "mi", "°F");

    companion object {
        fun suggestedFromCountryCode(countryCode: String?): AdventureMarket? =
            when (countryCode?.uppercase()) {
                "IN" -> INDIA
                "CA" -> CANADA
                "US" -> UNITED_STATES
                else -> null
            }
    }
}

enum class RegionalServiceCategory(val label: String) {
    NAVIGATION("Maps"),
    FOOD("Food"),
    TRANSPORT("Transport"),
    STAYS("Stays"),
    OUTDOORS("Outdoors"),
    FUEL_AND_CHARGING("Fuel & charging"),
}

enum class ProviderIntegrationState {
    /** Opens a provider website/app; Adventurer cannot read its private state or confirm an action. */
    EXTERNAL_HANDOFF_ONLY,
    /** Requires separate official API approval, credentials, policy review and end-to-end verification. */
    OFFICIAL_INTEGRATION_NOT_CONFIGURED,
}

data class RegionalProvider(
    val id: String,
    val name: String,
    val market: AdventureMarket,
    val category: RegionalServiceCategory,
    val websiteUrl: String,
    val summary: String,
    val integrationState: ProviderIntegrationState = ProviderIntegrationState.EXTERNAL_HANDOFF_ONLY,
)

object RegionalProviderCatalog {
    private val providers = listOf(
        // India: discovery, transport and transaction handoffs.
        RegionalProvider("google-maps-in", "Google Maps", AdventureMarket.INDIA, RegionalServiceCategory.NAVIGATION, "https://maps.google.com/", "Open external maps for place search or turn-by-turn directions."),
        RegionalProvider("mappls", "Mappls", AdventureMarket.INDIA, RegionalServiceCategory.NAVIGATION, "https://www.mappls.com/", "India-focused mapping option; SDK/API use needs a separate integration and credentials review."),
        RegionalProvider("swiggy", "Swiggy", AdventureMarket.INDIA, RegionalServiceCategory.FOOD, "https://www.swiggy.com/", "Restaurant and food discovery; cart, delivery fees and order status remain in Swiggy."),
        RegionalProvider("zomato", "Zomato", AdventureMarket.INDIA, RegionalServiceCategory.FOOD, "https://www.zomato.com/", "Restaurant discovery and food ordering remain in Zomato."),
        RegionalProvider("irctc", "IRCTC Rail Connect", AdventureMarket.INDIA, RegionalServiceCategory.TRANSPORT, "https://www.irctc.co.in/nget/", "Official rail booking handoff; train availability and ticket confirmation are not read by Adventurer."),
        RegionalProvider("redbus", "redBus", AdventureMarket.INDIA, RegionalServiceCategory.TRANSPORT, "https://www.redbus.in/", "Intercity bus search and booking handoff."),
        RegionalProvider("rapido", "Rapido", AdventureMarket.INDIA, RegionalServiceCategory.TRANSPORT, "https://www.rapido.bike/", "Ride handoff; live driver availability and trip state are not visible to Adventurer."),
        RegionalProvider("makemytrip", "MakeMyTrip", AdventureMarket.INDIA, RegionalServiceCategory.STAYS, "https://www.makemytrip.com/", "Travel and accommodation search/booking handoff."),
        RegionalProvider("booking-india", "Booking.com", AdventureMarket.INDIA, RegionalServiceCategory.STAYS, "https://www.booking.com/", "Accommodation handoff; price and availability must be rechecked with the provider."),
        RegionalProvider("airbnb-india", "Airbnb", AdventureMarket.INDIA, RegionalServiceCategory.STAYS, "https://www.airbnb.com/", "Stays and experiences handoff; no booking confirmation is returned to Adventurer."),
        RegionalProvider("incredible-india", "Incredible India", AdventureMarket.INDIA, RegionalServiceCategory.OUTDOORS, "https://www.incredibleindia.gov.in/", "Official tourism inspiration; verify local access, closures, permits and operating hours."),
        RegionalProvider("maps-fuel-in", "Google Maps fuel search", AdventureMarket.INDIA, RegionalServiceCategory.FUEL_AND_CHARGING, "https://maps.google.com/", "Use Maps to look for fuel or charging. Availability, opening hours and fuel stock are not verified."),
        // United States: external discovery handoffs only; no provider APIs are connected.
        RegionalProvider("google-maps-us", "Google Maps", AdventureMarket.UNITED_STATES, RegionalServiceCategory.NAVIGATION, "https://maps.google.com/", "External place search and directions handoff; no live route state is returned to Adventurer."),
        RegionalProvider("uber-eats-us", "Uber Eats", AdventureMarket.UNITED_STATES, RegionalServiceCategory.FOOD, "https://www.ubereats.com/", "Food-provider handoff; menus, delivery coverage, payments and order status remain with Uber Eats."),
        RegionalProvider("doordash-us", "DoorDash", AdventureMarket.UNITED_STATES, RegionalServiceCategory.FOOD, "https://www.doordash.com/", "Food-provider handoff; no cart or order confirmation is returned to Adventurer."),
        RegionalProvider("amtrak-us", "Amtrak", AdventureMarket.UNITED_STATES, RegionalServiceCategory.TRANSPORT, "https://www.amtrak.com/", "Rail search/booking handoff; schedules and ticket confirmation remain with Amtrak."),
        RegionalProvider("uber-us", "Uber", AdventureMarket.UNITED_STATES, RegionalServiceCategory.TRANSPORT, "https://www.uber.com/", "Mobility handoff; opening the provider does not request or confirm a ride."),
        RegionalProvider("recreation-gov-us", "Recreation.gov", AdventureMarket.UNITED_STATES, RegionalServiceCategory.STAYS, "https://www.recreation.gov/", "Official federal recreation reservation handoff; availability and access conditions must be checked at the source."),
        RegionalProvider("nps-us", "National Park Service", AdventureMarket.UNITED_STATES, RegionalServiceCategory.OUTDOORS, "https://www.nps.gov/", "Official US national-park information; it does not cover every state, local, tribal or private-land rule."),
        RegionalProvider("alltrails-us", "AllTrails", AdventureMarket.UNITED_STATES, RegionalServiceCategory.OUTDOORS, "https://www.alltrails.com/us", "Trail discovery handoff; confirm current conditions and land-manager restrictions."),
        RegionalProvider("plugshare-us", "PlugShare", AdventureMarket.UNITED_STATES, RegionalServiceCategory.FUEL_AND_CHARGING, "https://www.plugshare.com/", "EV charging discovery handoff; live connector availability is not connected to Adventurer."),
        // Canada: delivery, outdoor access, camping, transit and EV charging.
        RegionalProvider("google-maps-ca", "Google Maps", AdventureMarket.CANADA, RegionalServiceCategory.NAVIGATION, "https://maps.google.com/", "Open external maps for place search or turn-by-turn directions."),
        RegionalProvider("uber-eats-ca", "Uber Eats", AdventureMarket.CANADA, RegionalServiceCategory.FOOD, "https://www.ubereats.com/ca", "Restaurant delivery handoff; cart, fees, payment and order status stay with Uber Eats."),
        RegionalProvider("doordash-ca", "DoorDash", AdventureMarket.CANADA, RegionalServiceCategory.FOOD, "https://www.doordash.com/", "Restaurant delivery handoff; menus and service coverage vary by location."),
        RegionalProvider("skip-ca", "Skip", AdventureMarket.CANADA, RegionalServiceCategory.FOOD, "https://www.skipthedishes.com/", "Canadian food delivery handoff; no order is placed by opening this link."),
        RegionalProvider("via-rail", "VIA Rail", AdventureMarket.CANADA, RegionalServiceCategory.TRANSPORT, "https://www.viarail.ca/", "Rail search and booking handoff; schedules and ticket confirmation remain with VIA Rail."),
        RegionalProvider("uber-ca", "Uber", AdventureMarket.CANADA, RegionalServiceCategory.TRANSPORT, "https://www.uber.com/ca/en/", "Ride request handoff; no pickup is requested by opening this link."),
        RegionalProvider("lyft-ca", "Lyft", AdventureMarket.CANADA, RegionalServiceCategory.TRANSPORT, "https://www.lyft.com/", "Ride request handoff; coverage varies by city."),
        RegionalProvider("parks-reservations-ca", "Parks Canada reservations", AdventureMarket.CANADA, RegionalServiceCategory.STAYS, "https://reservation.pc.gc.ca/", "Official campground and accommodation reservation service; capacity is time-sensitive."),
        RegionalProvider("booking-ca", "Booking.com", AdventureMarket.CANADA, RegionalServiceCategory.STAYS, "https://www.booking.com/", "Accommodation handoff; price and availability must be rechecked with the provider."),
        RegionalProvider("airbnb-ca", "Airbnb", AdventureMarket.CANADA, RegionalServiceCategory.STAYS, "https://www.airbnb.com/", "Stays and experiences handoff; no booking confirmation is returned to Adventurer."),
        RegionalProvider("alltrails-ca", "AllTrails", AdventureMarket.CANADA, RegionalServiceCategory.OUTDOORS, "https://www.alltrails.com/canada", "Trail discovery handoff; check current trail conditions, access notices and offline-map requirements."),
        RegionalProvider("parks-canada", "Parks Canada", AdventureMarket.CANADA, RegionalServiceCategory.OUTDOORS, "https://parks.canada.ca/", "Official national-park information; it does not cover every provincial, territorial or municipal park."),
        RegionalProvider("plugshare-ca", "PlugShare", AdventureMarket.CANADA, RegionalServiceCategory.FUEL_AND_CHARGING, "https://www.plugshare.com/", "EV charging discovery handoff; connector status and operational availability must be checked live."),
        RegionalProvider("chargepoint-ca", "ChargePoint", AdventureMarket.CANADA, RegionalServiceCategory.FUEL_AND_CHARGING, "https://www.chargepoint.com/", "Charging-network handoff; live charger state is not connected to Adventurer."),
    )

    fun forMarket(market: AdventureMarket): List<RegionalProvider> =
        providers.filter { it.market == market }

    fun forCategory(
        market: AdventureMarket,
        category: RegionalServiceCategory,
    ): List<RegionalProvider> =
        providers.filter { it.market == market && it.category == category }

    fun find(id: String): RegionalProvider? = providers.firstOrNull { it.id == id }

    /**
     * Google Maps URLs launch external Maps/browser search without an API key.
     * This opens a provider; it does not reveal live navigation state to Adventurer.
     */
    fun googleMapsSearchUrl(
        query: String,
        market: AdventureMarket,
        region: String? = null,
    ): String {
        val normalizedQuery = query.trim()
        require(normalizedQuery.isNotEmpty()) { "Search query must not be blank." }
        val normalizedRegion = region?.trim()?.takeIf { it.isNotEmpty() }
        val locationContext = if (normalizedRegion == null) {
            market.countryName
        } else {
            "$normalizedRegion, ${market.countryName}"
        }
        val contextualQuery = "$normalizedQuery in $locationContext"
        return "https://www.google.com/maps/search/?api=1&query=${encode(contextualQuery)}"
    }

    fun googleMapsDirectionsUrl(
        destination: String,
        market: AdventureMarket,
        origin: String? = null,
        region: String? = null,
    ): String {
        val normalizedDestination = destination.trim()
        require(normalizedDestination.isNotEmpty()) { "Destination must not be blank." }
        val normalizedRegion = region?.trim()?.takeIf { it.isNotEmpty() }
        val destinationContext = listOfNotNull(
            normalizedDestination,
            normalizedRegion,
            market.countryName,
        ).joinToString(", ")
        val parameters = mutableListOf(
            "api=1",
            "destination=${encode(destinationContext)}",
        )
        if (!origin.isNullOrBlank()) parameters += "origin=${encode(origin.trim())}"
        return "https://www.google.com/maps/dir/?${parameters.joinToString("&")}"
    }

    private fun encode(value: String): String = URLEncoder.encode(value, "UTF-8")
}
