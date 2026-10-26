# Adventurer — Multi-Adventure Product Blueprint

## Executive direction

Adventurer should evolve from a motorcycle-first Android prototype into a unified **explore → plan → go → record → reflect → share** platform. The shared core serves riders, cyclists, road-trippers, overlanders, walkers, trekkers, campers, anglers, lawful hunters, divers, paddlers, island explorers and travel vloggers. The differentiator is not merely a route map: one itinerary ties places, activity segments, conditions, people, safety planning and story-making together.

This blueprint consolidates the user's earlier AdventureX/outdoor-adventure research and the later RideStory AI hackathon plan. It expands the existing repository; it is not a request to replace the app with a fresh starter.

**Status rule:** motorcycle/GPS and RideStory work are the strongest existing foundations. Mode names, prototype UI, and this roadmap do not mean every specialist workflow, live data provider or safety integration has already shipped.

## 1. Product promise and primary journeys

1. **Discover a new place:** enter a town, region, island, trailhead or shared location; explore what is nearby and what is worth doing in the time available.
2. **Build the adventure:** choose activity and difficulty; add scenic stops, routes, ferries/transfers, food, overnight stays and alternatives; calculate time and rough budget.
3. **Prepare properly:** activity-specific gear list, permits/licence source links, forecast windows, access restrictions, trusted contacts and expected-return time.
4. **Go with resilience:** use saved maps/routes and notes where possible; record only actual sensor/location data; show GPS quality and any demo state clearly.
5. **Capture the moments:** mark viewpoints, wildlife, campsites, catches, dives or notes; link photos/videos to the time and trip segment.
6. **Make a story:** choose clips, let AI suggest timestamps and captions, edit the timeline, add map/route context, choose format/language, then export.
7. **Share by choice:** publish a recap or route summary, keep the full route private, mask sensitive coordinates, and share check-in/live location only with explicit consent.

## 2. Activity-by-activity requirements

### A. Motorcycle touring
- Road and surface notes, mountain-pass stages, fuel/range estimates entered by rider, refuel/repair/tyre POIs and weather/closure alerts.
- Generic GPS recording plus motorcycle-only lean-angle estimate; never imply that a phone estimate is a calibrated instrument or tells the rider what lean is safe.
- High-altitude acclimatization and turnaround prompts must link to reliable guidance and cannot diagnose altitude illness.
- Breakdown/SOS UI must show whether it opens a dialer/SMS or reaches an actual staffed provider; don't imply dispatch where none exists.

### B. Cycling
- Explicit route profiles: road cycling, gravel, MTB, e-bike and bikepacking.
- Route fitness filters: surface, road traffic exposure where reliable, gradient, elevation gain, distance and technical difficulty.
- Bike-specific POIs: repair shops, water, toilets, safe parking, charging for e-bikes, food and bailout/rail-transfer options.
- Optional BLE cadence/heart-rate/power sensors later; record the source/device and don't fabricate metrics when not connected.
- Multi-day bikepacking: resupply stops, camping/lodging, luggage/gear list and daily distance/climb targets.

### C. Car road trips and overlanding
- A scenic-route builder for unfamiliar places: viewpoints, waterfalls, small towns, food, local experiences, cultural sites and overnight stops.
- Multi-stop itinerary order, day-stage planning, detours and Plan A/Plan B alternatives.
- Roadtrip POIs: fuel, EV chargers, rest stops, parking, toll/border crossings, service stations, accommodation and road restrictions.
- Overlanding adds surface/clearance caveats, water/fuel range entered by user, remote camp access, recovery/communications checklists and expected-return schedule.
- Don't claim direct vehicle telemetry or OBD integration until supported hardware/API has been implemented and tested.

### D. Walking, hiking and trekking
- Walks for city/coast/parks and hikes for trail-based routes should have distinct route labels and difficulty cues.
- Trail-aware routing, topographic/contour maps, elevation profiles, grade, distance, estimated duration and trailhead/exit points.
- Water, shelter, toilets, first-aid/access points and known hazards. Trail difficulty should come from published or community-verified data with date/source.
- Pre-trip checklists, check-in schedule, weather windows, turnaround time and offline breadcrumb return.
- Accessibility information (surface, stairs, steep gradients, rest stops, mobility constraints) where the data exists.

### E. Camping, backpacking and basecamp
- Map layers/search for campsites, backcountry camping zones, RV/vehicle-accessible campgrounds, huts, hostels and permitted overnight places.
- Show source-backed access rules, permits, fees, reservations, amenities, potable water, toilets, fire restrictions and seasonal closure.
- Gear/food/water/fuel/power list with packed/missing state; shared group packing and role assignment.
- Arrival/departure check-in, weather/fire-risk reminders and the next day's route integrated into the same trip.
- Clearly distinguish an official campground from a community-submitted suggested spot and a private-land coordinate.

### F. Fishing and angling
- Site notebook for access, permitted species/rules link, season and licence link, user-entered conditions and catch/photo log.
- Add tide, weather, wind, flow/level and marine conditions only from appropriate data providers; show timestamps and region limitations.
- Keep precise fishing spots private by default or let users publish an approximate area instead of exact coordinates.
- The app must not invent legal catch limits or assume rules for the wrong jurisdiction. Link to the responsible authority and mark unverified information.

### G. Hunting and wildlife tracking
- Keep the workflow conservation-first: check season/licence, land access, protected habitats and official local regulations before a user treats a plan as actionable.
- Private tracks, sightings and harvest/observation logs; no precise rare-species locations published by default.
- Provide gear/readiness and communication checklists, expected-return timer and emergency-contact plan.
- Use the product for lawful trip planning and nature logging, never as authority that a hunt is legal or a protected area is open.

### H. Scuba, snorkelling, kayaking and boating
- Discover dive/launch sites, entry/exit points, shore access, current/wind/visibility/tide notes, local guidance and return/check-in plan.
- Scuba logbook should import or receive actual depth/profile information from compatible certified dive computers where documented support exists.
- The current prototype's GPS cannot track reliable underwater position and is not a dive computer. No improvised depth-based decompression, gas management or ascent-rate advice.
- For paddling/boating: launch/landing, portages/take-outs, route length, weather, wind/current, water-level/river hazards where authoritative, vessel/float-plan checklist and no-go criteria.
- Local protected marine areas, wildlife and reef etiquette; sensitive dive sites use opt-in/approximate location sharing.

### I. Island trips and exploring new places
- Destination home page built around map + discovery cards: landmarks, trails, viewpoints, beaches, food, local culture, tours and adventure activities.
- Account for ferry/boat schedules, limited departure times, weather-dependent transport, island service hours and last-return timing.
- User chooses a time-available window and preferences (nature, history, food, photography, swimming, hikes, family-accessible, low cost, quiet places).
- AI proposes a time-aware route with optional stops, estimated transit and a wet-weather alternative; show data sources/uncertainty and don't promise an opening/reservation unless confirmed.
- Local discovery should work internationally, not only for Ladakh and Indian high passes. Build region/provider adapters instead of a single baked-in list.

### J. Travel vlogging and memory making
- Every activity and trip segment can attach photos, videos, notes and timestamps.
- AI finds highlight candidates based on actual video frames, action/scenery diversity and user intent, then returns editable timestamps/EDL JSON; the user chooses final clips.
- Build templates for road-trip recap, bike ride, cycling climb, hiking summit, camp diary, island day and dive/fishing log.
- Offer subtitles, multilingual narration, route/map overlay, chapter cards, cover images and aspect ratios for common sharing destinations.
- Allow private/local-only editing, explicit consent before media upload, temporary server retention, and face/license-plate/location redaction where feasible.
- The model must not claim that a user visited a landmark or caught/saw an animal unless the input supports that claim.

## 3. Features shared by all modes

### Explore and search
- Global place search and map-first browse.
- Filters for activity, distance/time, season, difficulty, budget, accessibility, terrain/surface and group suitability.
- POI categories and user-saved collections; itinerary/route links; source, last-updated timestamp and freshness indicators.
- Reviews summarized by AI only with provenance and a link to source reports; stale user reports expire or are visually downgraded.

### Trip builder
- Multi-stop and multi-day itineraries, activity per segment, reorderable stops and day boundaries.
- Plan A/B routes, saved offline details and fallback exits.
- Rough budget across fuel/charging, food, lodging/camping, permits, rental/gear and transport; users can edit assumptions.
- Travel-time estimates should come from actual routing data, never invented values.
- Links for permits, bookings, park entry and transport should open the official/known provider source; the app should not claim a booking exists without confirmation.

### Offline-first field use
- Pre-download a supported map area, route files and trip facts before leaving coverage.
- Local recording and manual notes must survive temporary loss of network or backend availability.
- Queue sync with conflict-aware updates; don't block saving/finishing an activity while offline.
- Cache limitations, downloaded area boundaries and freshness date should be visible; don't claim offline support until actual maps/data are cached.

### Safety and group coordination
- Trusted-contact plan, check-in interval, expected return, explicit live-sharing duration and a visible sharing-active state.
- Emergency call/SMS through native capabilities; report send/delivery status honestly. Satellite/mesh claims require actual compatible hardware and tested pathways.
- Local emergency contacts and provider details must be region-aware and sourced; avoid hard-coding phone numbers across countries.
- Group itinerary/leader roles, rendezvous points, roll-call/check-in and no-signal expectations.
- Route deviation or missed check-in may prompt the user/contacts but should not automatically promise a rescue service.

### Common social and reward layer
- One activity-neutral feed for trip summaries, photos, routes, lessons, conditions and group events.
- Multi-activity clubs and challenges rather than only mileage/speed competition.
- Respect private, followers-only, public and delayed-share trip visibility.
- Moderation for dangerous advice, harassment, false safety reports, wildlife-location leakage and copyrighted material.

## 4. Product architecture

### Shared entities
- **UserProfile:** activity preferences, locale/units, owned equipment/vehicle details, accessibility preferences, privacy defaults and trusted contacts.
- **AdventureTrip:** owner, title, destination, dates, participants, itinerary stages, budget, packing list, safety plan and privacy.
- **TripSegment:** ordered mode, planned and actual route, start/end/time, selected provider, state and metrics extension.
- **Route / TrackPoint:** geometry, timestamp, coordinate accuracy, altitude/grade if known, surface/terrain, GPX reference and source.
- **Place / Waypoint:** type, coordinate, note, photos, source, confidence, verification date, seasonal restrictions and privacy.
- **ConditionReport:** hazard/closure/camp/dive/road/POI status, author/source, observed time, expiry and moderation state.
- **ActivitySession:** selected mode, actual-vs-demo flag, local persistence state, sensor sources and mode-appropriate metrics.
- **MediaAsset / StoryPackage:** owner/consent, upload/retention state, clip timestamps, EDL, caption/narration, track/segment references and export.
- **SafetyPlan:** contacts, expected return, check-in schedule, delivery status, exit plan and user-confirmed risks.

Keep shared entities stable and put specialized metrics into versioned per-mode extensions. Avoid an enormous telemetry object full of unrelated nullable fields.

### Platform architecture
1. **Android client:** Compose UI, Room/local persistence, background recording with explicit permission/battery behavior, GPX import/export, media editing and mode-aware UX.
2. **Maps and place providers:** interface for routing, place search, offline tiles, conditions, weather/tides, official rules and reservation/permit links.
3. **Backend:** Firebase Auth, least-privilege Firestore/Storage rules, callable AI services, per-user quota/entitlement enforcement, cleanup and observable error budgets.
4. **AI:** secure server-side model requests; selected images and minimal necessary metadata; structured JSON outputs; source citations for researched recommendations; no keys in the APK.
5. **Integrations:** optional BLE cycling sensors, vehicle data adapters, dive-computer imports, satellite messengers and external social/export APIs only after documented APIs and user consent.
6. **Privacy/security:** data minimization, encryption in transit/at rest, delete/export, precise-location privacy, no public default for sensitive tracks, retention policy and Play Console disclosures that match reality.

## 5. AI features to prioritize

### Priority 1 — Multi-activity Story Studio
Generalize current RideStory prompts and labels from ride/high-pass to an activity-aware story: retrieve activity, destination, time range, selected media, waypoints and known metrics. Outputs remain editable. Keep highlight extraction and local MP4 render as the familiar hook.

### Priority 2 — Destination and stop planner
Ask for destination, start point, duration, travel style, mobility/fitness, budget, interests and constraints. AI can draft stops, but routing provider, travel times, opening times, ferry schedules, permit/closure data and weather must come from tools/sources and display freshness. User approves every itinerary.

### Priority 3 — Preparation assistant
Generate mode-specific checklists and alternatives. For regulated/safety-sensitive areas, surface official sources and never substitute model text for a permit or professional training.

### Priority 4 — Field-side interpretation
Summarize a landmark from supplied verified context, explain route/condition data, and draft a voice note. Keep driver/rider interaction minimal while moving. Animal/species recognition should show uncertainty and require review before posting.

## 6. Pricing and sustainability
- Free: basic trip/session records, saved places and limited story creation.
- Pro: expanded offline storage/maps where licensing permits, richer multi-day planning, additional story generations/exports, more cloud sync, group features and device integrations.
- Affiliate/booking partnerships only when the provider is real and the link is valid; disclose sponsored placement and don't let payment rank safety-critical information.
- Keep AI cost bounded with server-side quotas, caching of non-personal results and observability. Don't charge for access to emergency contact controls.

## 7. Implementation sequencing

### Phase 0: repair and model foundation
- Restore a clean compiling main and green CI first.
- Add typed activity definitions and store the chosen mode on every activity session.
- Make UI labels/activity recap aware of mode; motorcycle lean-angle UI appears only for the motorcycle mode.
- Keep current mode labels honest: scuba needs dive-computer data; fishing/hunting require current official sources; off-grid map support requires real local map storage.
- Remove fixed high-pass assumptions in AI Story Studio and allow a custom city, country, island or route context.

### Phase 1: practical cross-mode demo
Ship the common GPS record/replay/waypoint flow first for motorcycling, cycling, road trips, walking and trekking. Implement a global Explore page and basic multi-stop trip draft. Give Story Studio activity-specific prompts for these common workflows. Prove them on real devices.

### Phase 2: itinerary, camping and offline
Implement multi-day stages, lodging/campsite provider, activity-specific checklists, GPX/KML as required, map area downloads, offline save/sync and route alternatives.

### Phase 3: water/nature and specialized providers
Add fish/conditions/official regulations, lawful hunting/wildlife logging, marine sites/tides, dive logs and compatible dive-computer import. Build provider/data-quality tests and privacy controls first.

### Phase 4: community and creator ecosystem
Add moderated trip libraries, events/clubs, creator templates, robust story editing, integrations and selected monetization. Expand regions after localized source coverage and research interviews.

## 8. Definition of done and non-goals

A mode is **not** considered shipped because it appears in a dropdown. A mode reaches beta only when:
- onboarding/preferences identify the activity and unit/metric expectations;
- user can plan/save/record the correct type of session;
- metrics display only when a real supported source exists;
- safety, no-signal, permission denial, privacy, deletion and error states are tested;
- authoritative sources, licence terms and regional constraints are reviewed;
- help/marketing accurately explains limitations.

Non-goals for the current prototype: guaranteeing rescue, acting as a certified dive computer, declaring legal hunting/fishing rights, inventing current travel conditions, promising offline navigation before map-data download support, or publishing precise sensitive locations automatically.

## 9. Research inputs and references

Earlier user research assets located in the Library:
- **AdventureX: Research Blueprint for an Outdoor Adventure App** — multi-persona scope, cross-activity features, privacy/legal work, architecture and staged rollout.
- **Executive Summary — Outdoor Adventure Apps** — offline maps, route planning, GPX/KML, sensor-aware tracking, safety, media and monetization.
- **Executive Summary — Adventurer (RideStory AI)** — AI highlights, narrated diary, hazard-awareness ideas, feature feasibility and a 10-day hackathon-oriented plan.

Current external product references used to validate patterns:
- [Komoot route planner and waypoint sequencing](https://www.komoot.com/help/routeplanner)
- [Komoot routing, offline navigation and sharing](https://www.komoot.com/features)
- [AllTrails offline maps help](https://support.alltrails.com/hc/en-us/articles/37213318235028-How-to-download-maps-to-your-phone-for-offline-use)
- [Garmin Dive timeline and dive logs](https://support.garmin.com/en-GB/?faq=kEMry7X1uCA6U2SkkKpIP6&partNumber=010-GDIVE-00&tab=topics&topicTag=region_divelogs)

The third-party references establish useful design patterns; exact provider choice should follow a region-by-region coverage, pricing, licensing and privacy review.
