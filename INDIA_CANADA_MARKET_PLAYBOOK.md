# Adventurer — India + Canada Market Playbook

**Research cut-off:** 27 October 2026  
**Purpose:** Prioritize market-specific apps, preferences, product features and integration work for India and Canada. This is desk research and a product recommendation—not evidence of a signed provider partnership or completed user research.

## Executive decision

Build one trip-and-memory product with region-specific service adapters, not two separate apps and not a replacement for Google Maps. The user can explicitly select India or Canada; the device country is only a suggested starting value and must never be treated as permission to track the user. Keep Google Maps (and, for India, a possible Mappls SDK/API integration) responsible for maps and navigation. Adventurer should own trip context, user preferences, checklists, user-entered fuel estimates, reminders, saved stops, action-state truth, and trip memories.

The first shippable regional feature is an external provider handoff catalogue. It helps users open the specialist service they already trust while Adventurer preserves the trip plan. Live data, cart reading, booking, payment, order status and receipts require approved official integration access and end-to-end verification.

## What changes by market

### India: value-conscious, intercity and highway journeys

**Core jobs**
- Plan multi-day motorcycle and car routes that may combine bus or rail legs.
- Find food, water/rest breaks, fuel, tyre/repair help and overnight options while keeping the trip plan in one place.
- Mix regional food, viewpoints, heritage, nature and outdoor activities.
- Support users who switch between English and Indian languages, and provide low-data fallbacks for long road trips.
- Keep money clear in INR, distance in kilometres and temperature in Celsius; make local dietary preferences and budgets editable.

**Provider shortlist for handoff-first**
- **Maps:** Google Maps; evaluate Mappls for a later licensed search/routing SDK or REST API integration. Mappls documents Android SDKs and search/routing APIs; it is a candidate, not already connected.
- **Food:** Swiggy and Zomato. First release opens the selected provider; Adventurer does not imply menu availability, delivery coverage, cart contents or order completion.
- **Rail and bus:** IRCTC Rail Connect and redBus. Use official booking page/app handoffs; do not scrape ticketing, seat availability or private account screens.
- **Stays:** MakeMyTrip, Booking.com and Airbnb as optional search/booking handoffs.
- **Local mobility:** Rapido and Uber as optional ride handoffs. Opening a page does not request a ride or assign a driver.
- **Destination research:** Incredible India for inspiration, plus map-search handoffs for specific places.
- **Fuel/roadside:** start with a query handoff to Maps. Add station-network partnerships only when station identity, operating hours and availability can be verified.

**Initial segment hypotheses**
1. Motorcycle and car tourers planning multi-day routes.
2. Weekend explorers combining food, nature and heritage.
3. Budget travellers coordinating bus, rail and accommodation.
4. Travel vloggers who want a private trip timeline and a faster story workflow.

**Localization requirements**
- Treat language as a user preference. Support English first with a roadmap for Hindi, Tamil, Malayalam, Telugu, Kannada, Bengali and Marathi voice/text. Do not infer language from a name or location.
- Let users choose cuisine, vegetarian/vegan/Jain/halal preferences and optional allergy notes. Keep personal dietary/allergy data local by default; share only after a clear user action.
- Store budgets as amount + ISO currency code. Never reinterpret INR as CAD or use a hard-coded exchange rate.
- Fuel advice must say “based on your estimate”. The current calculator is not connected to route distances, fuel stock, payment or station availability.

### Canada: outdoors, seasonal access and longer distances

**Core jobs**
- Build trips around hikes, parks, campsites, lakes, scenic drives, ferries, rail legs and EV charging.
- Keep official access, closure and reservation links visible because availability and seasonality change.
- Prepare for gaps in mobile coverage with saved plans, checklists and user-owned notes. Do not market offline maps until map data can actually be downloaded and used offline.
- Default to CAD, kilometres and Celsius, allow a miles preference, and expand English/French localization.

**Provider shortlist for handoff-first**
- **Navigation:** Google Maps URL handoff; preserve the Adventurer trip plan when the user returns.
- **Food delivery:** Uber Eats, DoorDash and Skip.
- **Outdoors:** AllTrails for trail discovery and Parks Canada for official national-park information. Provincial and territorial parks need their own official sources.
- **Camping/stays:** Parks Canada reservations, Booking.com and Airbnb. Parks Canada notes that reservations and operating windows vary by destination; the official system remains the source of truth.
- **Rail/urban mobility:** VIA Rail plus Uber/Lyft where available. Add local transit providers after choosing a launch city.
- **EV charging:** PlugShare and ChargePoint for discovery handoffs. Connector compatibility, price and live availability require the relevant feed.
- **Weather/road/park conditions:** add official, region-aware feeds before promising condition-aware advice. Show source and last-checked time; use “unknown” rather than inventing a condition.

**Initial segment hypotheses**
1. Ontario/Quebec/BC/Alberta weekend hikers and road-trippers.
2. Campers and national/provincial park visitors.
3. EV road-trippers planning charging stops.
4. Students/new residents exploring nearby places on a budget.
5. Outdoor creators who want privacy-controlled trip memories and recaps.

**Localization requirements**
- Preferences for vehicle type, EV connector, mobility/accessibility needs, hiking difficulty, camping style, pets, food, accommodation budget, group size, quiet hours and offline/data-saving preferences.
- Distinguish national park information from provincial/territorial/municipal rules.
- Expire stale trail/park/community reports and show whether a point is official, user-submitted or only a search suggestion.

## Provider integration ladder

| Level | What ships | What Adventurer can truthfully say |
|---|---|---|
| 0. Curated link | Maintained official URL and a handoff button | “Opening the provider.” |
| 1. Search handoff | Encoded Maps/provider search URL and market context | “Opening search results.” |
| 2. Official read API | Approved place/route/conditions data with attribution, quotas and freshness | “Provider data checked at [time].” |
| 3. Authenticated workflow | Provider-approved account connection and typed transaction/request API | “Request submitted,” only if the provider confirms it. |
| 4. Verified outcome | Provider receipt/status or auditable confirmation record | “Booking/order confirmed,” only when evidence proves it. |

Do not use screen scraping or arbitrary tapping as the default integration strategy. A provider needs suitable access terms, stable capabilities, test accounts, cancellation/error paths and a way to verify outcomes. Payment credentials should remain in the provider/payment flow; do not store card data in Adventurer.

## Personalization: the user should set the trip rules

Make preferences structured, editable and optionally overridden per trip. Defaults should be conservative and recommendations explainable.

### Traveller profile
- Home-market preference (India/Canada/other), language and voice style.
- Units, currency, number/date format, budget range and spending style.
- Activity modes: motorcycle, car, cycling, walking/hiking, trekking, camping, water activities, fishing/wildlife watching and destination exploration.
- Vehicle/equipment: tank size and user-entered efficiency, vehicle type, EV connector/range and gear.
- Accessibility preferences, difficulty and terrain comfort.
- Food: cuisine, budget, dietary preferences and optional allergy notes.
- Stay: campsite/hotel/hostel/rental, comfort level, group size and price ceiling.
- Route: tolls, highways, unpaved roads, ferries, scenic detours, fuel/charging interval.
- Notifications: concise while moving, details queued for safe stops, quiet hours and reminder style.
- Privacy: exact vs approximate location in memories, local-only/cloud backup, history retention, photo metadata and sharing audience.

### Per-trip overrides
- Destination, date/time window, days available and return deadline.
- Travellers, activity per segment, expected spend and buffer.
- Planned stops and fallback/exit points.
- Offline preparation state and what was downloaded, if actual offline support exists.
- Trusted contact and check-in interval, with visible sharing on/off state.
- Preferred provider for each category; preferences must not force a provider where it has no coverage.

### Preference-learning rules
- Ask before saving a preference; show what was saved and provide edit/delete.
- Learn from intentional actions (saved stop, preferred cuisine, dismissed category), not hidden cross-app scraping.
- Keep precise location history private by default; never expose home/work or sensitive outdoor coordinates as a public achievement.
- Do not personalize safety-critical rankings using commissions, streak pressure or engagement incentives.
- Do not silently send dietary, accessibility, location or contact details to a provider.

## India + Canada feature matrix

| Feature | India v1 | Canada v1 | Later official integration |
|---|---|---|---|
| Map search/directions | Google Maps handoff; Mappls candidate | Google Maps handoff | Routes/Places API with secured keys, quotas, attribution and terms review |
| Food discovery/order | Swiggy, Zomato handoff | Uber Eats, DoorDash, Skip handoff | Partner menu/cart/order API and verified receipt |
| Rail/intercity transport | IRCTC, redBus handoff | VIA Rail and regional transit handoff | Authorised schedules and transaction APIs |
| Accommodation | MakeMyTrip, Booking.com, Airbnb handoff | Parks Canada, Booking.com, Airbnb handoff | Search/availability/booking partnership |
| Trails/parks | Tourism/Maps handoff; source per destination | AllTrails discovery + Parks Canada/provincial sources | Licensed trail feeds, official closures/permits |
| Fuel/charging | Maps search; manual tank/mileage | Maps + PlugShare/charging handoff | Route-aware station/charger state and compatibility |
| Offline readiness | Saved plans, notes/checklists; no map tiles implied | Saved plans, notes/checklists; no map tiles implied | Licensed downloadable maps and sync conflict handling |
| Memories | Local-first notes and approved media | Local-first notes and approved media | Optional, opt-in cloud backup/story generation |
| Language/units | INR, km, Celsius; user-selected Indian-language roadmap | CAD, km default with miles option, Celsius, English/French roadmap | Full localization and locale-aware voice evaluation |

## Architecture changes

1. **Market context:** explicit AdventureMarket preference; device locale is only a suggestion. Do not request location permission merely to select a country.
2. **Provider catalogue:** provider ID, market, category, official URL, access status, limitations and review date. Separate app/web handoff from API-backed data.
3. **Typed adapters:** searchPlaces, getRoute, getConditions, getAvailability, prepareOrder, submitOrder and verifyOrder. Every response includes source, checked-at timestamp, region coverage and confidence.
4. **Common preferences:** keep a typed profile with explicit sync consent and versioned migrations. Monetary values require amount + ISO currency code; do not reuse CAD-only storage for another currency.
5. **Local-first trip:** persist trips/stops/reminders/memories locally; cloud sync is opt-in and conflict-aware. Build real map downloads before marketing offline navigation.
6. **Voice policy:** short spoken output while moving, no typing or cross-app browsing prompts during detected motion, and noncritical items queued for safe stops. Support stop/cancel/repeat/correction.
7. **Action state machine:** SUGGESTED → AWAITING_CONFIRMATION → HANDED_OFF / SUBMITTED → VERIFIED / FAILED / OUTCOME_UNKNOWN. Opening an external app is HANDED_OFF, never VERIFIED.
8. **Observability:** record provider errors, coarse handoff outcomes and corrections without raw audio, full query text, route geometry or precise coordinates by default.

## Release sequence

### Sprint 0 — reliable foundation
- Fix the fuel-range test expectation: reserve-aware logic marks a next stop with a small remaining margin REFUEL_SOON.
- Keep Android and Functions CI green.
- Add provider-catalogue tests for market separation, URL encoding and rejection of blank map queries.
- Remove misleading live wording, mock booking claims and fixed-country assumptions.

### Sprint 1 — regional discovery UX
- Add an editable India/Canada choice to Explore & Plan; device country only suggests a starting value.
- Add category filters and external handoff cards.
- Make Google Maps search market-aware.
- Show that a handoff is not a booking; log coarse provider/category events only.
- Collect language, activity, travel style and provider preference without unnecessary permissions.

### Sprint 2 — preferences and money correctness
- **Implemented in this native branch:** trip budget and creator-draft amounts store amount + ISO currency code; Room v1→v2 migration preserves legacy CAD amounts without conversion; trip-pack export records both fields.
- Localize units, number/date formats and languages; never silently convert currency.
- Still required: wider locale-aware date/number/measurement formatting, multilingual UI/speech QA, and a durable profile model beyond the current provider/market preferences.
- Persist provider, accessibility and dietary preferences with edit/delete controls.

### Sprint 3 — live place/route data
- Select one provider for a controlled pilot; complete approvals, secure keys server-side, attribution, quotas/billing alerts and data-retention review.
- Use route-aware stops only when a routing provider supplies route distance; straight-line distance is not route distance.
- Add source timestamps, offline/error states and stale-data expiry.

### Sprint 4 — food and stays
- Validate supported API scope and geography with providers.
- Build order review naming restaurant, items, quantities, delivery/pickup location, fees and total.
- Require fresh confirmation for the exact proposal and verify provider receipt/status. Include cancellation, unavailable-item and unknown-outcome paths.

### Sprint 5 — outdoors, safety and memories
- Integrate authoritative park/road/weather/closure data in one region first.
- Build offline trip packs only after map licensing, downloads, storage and sync are implemented.
- Add local-first photo/video notes and explicit metadata/backup controls.
- User-test with riders/hikers while stationary or in simulation before on-road release.

## Success metrics
- Time from intent to a useful sourced shortlist.
- Provider handoffs returning to a saved trip/stop (not claimed order conversions).
- Trip setup completion, saved-stop usage and repeat-trip retention.
- Fuel estimate freshness/correction and overconfident-warning rates.
- Provider freshness and “outcome unknown” rate.
- Crash-free sessions, screen-reader completion, permission-denial recovery, battery/data use.
- User confidence and privacy-control usage. Do not optimize for speed, risky detours or prolonged voice interaction during active travel.

## Revenue hypotheses
- **Free:** trip plans, saved stops, local checklists, voice quick actions and core memories.
- **Adventurer Plus:** richer plans, group planning, larger story quota and opt-in backup; offline maps only when licensed and implemented.
- **Affiliate/referral:** optional transparent links for stays, experiences and gear after terms review. Disclose sponsorship; commissions never outrank safety or accessibility.
- **Provider partnerships:** APIs/bookings only when official access and operational support are viable.
- Do not charge for a native call button or basic trusted-contact/safety checklist.

## Legal, privacy and trust
India's Digital Personal Data Protection Rules, 2025 were notified in November 2025; implementation must follow the applicable commencement timeline and be reviewed by counsel for the specific data flows. In Canada, meaningful consent is a core PIPEDA principle, and provincial laws may also apply. This is a product checklist, not legal advice.

Privacy defaults:
- No always-on background microphone/camera claim.
- No continuous audio/video upload by default.
- Keep precise routes, home/work and sensitive wildlife/fishing coordinates private.
- Request camera, microphone, location and sharing permissions at feature time with plain-language purpose.
- Offer local-only mode and delete/export controls.
- Ensure advertising/affiliate logic cannot influence safety-critical suggestions.

## Sources
- Google Maps Platform, Maps URLs: https://developers.google.com/maps/documentation/urls/get-started
- Google Maps Platform, Navigation SDK policy/attribution: https://developers.google.com/maps/documentation/navigation/android-sdk/policies
- Mappls Android SDK/API documentation: https://developer.mappls.com/documentation/sdk/android/
- IRCTC official Rail Connect app description: https://contents.irctc.co.in/en/IRCTC_andriod_App.html
- India's 2026 App Store Travel chart (a current examples snapshot, not market-share research): https://apps.apple.com/in/iphone/charts/6003
- India's 2026 App Store Food & Drink chart (a current examples snapshot, not market-share research): https://apps.apple.com/in/iphone/charts/6023
- Parks Canada reservation portal: https://parks.canada.ca/voyage-travel/reserve
- Parks Canada reservation instructions: https://parks.canada.ca/voyage-travel/reserve/instructions
- Office of the Privacy Commissioner of Canada, PIPEDA meaningful consent: https://www.priv.gc.ca/en/privacy-topics/privacy-laws-in-canada/the-personal-information-protection-and-electronic-documents-act-pipeda/p_principle/principles/p_consent/
- Government of India Press Information Bureau, DPDP Rules 2025: https://www.pib.gov.in/PressReleasePage.aspx?PRID=2190655&lang=2&reg=48

## Questions to validate with people, not assumptions
- Should market be selected at onboarding, per trip, or both when a user travels abroad?
- Which provider does each persona already prefer, and where is its coverage reliable?
- Is the most frequent request “find options” or “complete a transaction”? The latter needs a commercial/API plan, not only a voice model.
- What is the minimum useful offline pack for an Indian motorcycle trip vs a Canadian hike/campsite trip?
- Which language/speech pairs work in road noise and on lower-end Android devices?
- What level of voice narration is valuable when stopped without becoming a distraction while moving?
