# Adventurer Roadmap — One Adventure Platform, Many Ways to Explore

## Product vision

Adventurer helps people **discover, plan, experience, document, and share outdoor adventures and travel**—whether they are cycling a gravel route, driving to an unfamiliar town, trekking into the mountains, camping overnight, fishing, diving, or exploring an island.

Motorcycling remains a strong initial use case, not the product boundary. The long-term experience shares a single trip, route, media and safety foundation while each activity gets its own truthful metrics and domain-specific safeguards.

## Current branch delivery slice

- [x] Add a dedicated Explore & Plan navigation destination.
- [x] Persist private trip drafts, multi-day stop lists, checklists, safety-plan fields, saved-place ideas and creator guide/event drafts in local Room storage.
- [x] Export a saved plan and its safety/checklist fields to a JSON trip pack, explicitly excluding map tiles and live conditions.
- [x] Record a manual check-in timestamp locally and show that it does not send a notification.
- [x] Add activity-specific Story Studio templates and generalize AI/offline copy by activity.
- [x] Instrument app-open, plan-created, activity-started, story-exported/shared, place-saved, local check-in and creator-draft events.
- [ ] Connect a real place/routing provider and show source + freshness for every route/POI result.
- [ ] Package lawful, licensed offline map regions and test full navigation without network.
- [ ] Add background check-in reminders and explicit, consent-based outbound contact workflows.
- [ ] Back community place reports/guides/events with authorized server data, moderation and verification.
- [ ] Implement A/B assignment and aggregate cohort reporting before interpreting conversion/retention experiments.
- [ ] Add sensor integrations only through supported, documented device APIs.

## Activity coverage

| Activity / journey | Core user need | Domain-specific features |
|---|---|---|
| Motorcycle touring | Confident rides, mountain passes and road support | Road-surface notes, fuel/range planning, lean-angle estimate, breakdown help, high-altitude cautions |
| Cycling | Road, gravel, MTB and bikepacking | Surface-aware routes, climb/elevation profile, bike repair/water POIs, optional cadence/heart-rate sensors |
| Car road trips | Discover a new place and enjoy the drive | Scenic routes, stop sequencing, fuel/EV charging, parking, road closures, tolls, overnight stages |
| 4x4 / overlanding | Remote routes and self-supported trips | Terrain/clearance suitability, water/fuel range, campsite waypoints, recovery/communications checklist |
| Walking / hiking / trekking | Trail discovery and off-grid navigation | Topographic maps, difficulty, grade, elevation gain, water/refuge points, turnaround times |
| Camping / backpacking | Find a safe, suitable place to stay | Campsite details, reservations/fees/permits, amenities, weather/fire restrictions, packing and food checklists |
| Fishing / angling | Find legal, appropriate fishing locations | Tide/wind/water conditions, season and licence links, catch log, access rules, sensitive-location privacy |
| Hunting / wildlife tracking | Plan responsible, lawful outings | Season/licence/land-access source links, protected-zone warnings, offline plans, observation log and strict location privacy |
| Scuba / snorkelling | Find sites and keep trip/dive records | Entry/exit points, current/visibility/tide conditions, buddy checklist, logbook and certified dive-computer import |
| Kayaking / boating / sailing | Plan water routes and returns | Launch/landing points, wind/tide/current, marine forecast, vessel checklist and no-go notices |
| Island / new-place exploration | Build a rewarding multi-stop itinerary | Ferry/transit dependencies, viewpoints, beaches, food, culture, hikes, local tours, permits, weather windows and time/budget |
| Vlogging / travel creator | Turn any adventure into a story | Timestamped media, AI highlights, map overlays, subtitles, multilingual narration and social-ready exports |

## Product principles

1. **A journey can contain multiple segments.** One trip may include a car transfer, cycling, a hike, a campsite and a snorkel session; the product should retain one itinerary and attach activity-specific segments.
2. **Share the foundation, not the wrong metric.** Routes, duration, waypoints, media, packing, budgets, groups and stories are common. Lean angle, cycling cadence, hiking grade, fuel range, catches and dive depth are specific.
3. **Plan for offline before leaving coverage.** Download route/map areas, save contacts and essential trip notes, cache waypoints and make the recording experience resilient to disconnected service.
4. **Keep discovery explainable.** AI suggestions should show the source and freshness of closures, weather, permits and local rules. If information cannot be verified, say so rather than inventing availability or legality.
5. **Safety beats engagement.** Large field controls, minimal interaction while moving, check-ins, emergency contacts and warnings should be more prominent than badges or social prompts.
6. **Protect people and place.** Sharing routes and live locations is opt-in. Allow trip-level privacy and delayed publishing; keep rare wildlife, dive, fishing and hunting locations private by default where disclosure creates risk.
7. **Never overclaim emergency or technical safety.** A tap-to-call or prepared SMS is not a dispatch service. A consumer app is not a dive computer, medical device, weather authority or legal regulator.

## Core experience and navigation

- **Explore:** Search a country, region, island, town, trail or point of interest; browse activity-filtered routes; view verified information, community notes and conditions.
- **Plan:** Assemble a multi-day, multi-stop itinerary; set activity per leg, distance/time constraints, difficulty, accessibility, transport dependencies, budget and fallback options.
- **Record:** Start a named activity session with real-versus-demo state, GPS track when available, elapsed time, waypoints, photos/notes and activity-appropriate metrics.
- **Trips:** Save upcoming, active and completed adventures with day stages, checklists, route files, permits and emergency plans.
- **Stories:** Convert selected media and approved trip data into an editable story, highlight timeline, captions, voice-over, map recap and export.
- **Community:** Discover public trips, clubs, events, guides, campground/dive/route reports and fresh trail/road-condition updates—with provenance and moderation.
- **Profile / Pro:** Activity preferences, owned equipment/vehicles, saved places, units/accessibility, privacy, download storage, subscription and data deletion.

## Shared feature requirements

### Destination discovery and trip planning
- Search by destination, coordinates, activity, time available, season, difficulty, budget, access needs and group composition.
- Discover points of interest by intent: viewpoints, trails, campgrounds, water, mechanics, fuel, EV chargers, food, parking, ferry terminals, toilets, boat ramps, dive entries and emergency services.
- Plan ordered stops and day stages; show approximate distance, elevation/terrain, transit/ferry dependencies and time buffers.
- Offer safe alternatives and return routes. Surface the age/source of closures and condition reports.
- Support GPX import/export first; add KML/GeoJSON where useful. Use clear attribution and comply with map-data licences.
- Store route, itinerary and saved-place data locally so a saved trip remains usable without network access.

### Recording and trip journal
- A common session stores the route/track, start/end, elapsed time, waypoints, notes, media references and privacy state.
- Metric adapters supply only relevant values: speed/distance/elevation for movement; grade/climb and optional sensor data for cycling; walking pace/elevation; vehicle/route notes; camp arrival/departure and equipment; manually entered catch/observation logs; dive log imported from a compatible device.
- Save sessions locally and sync later. Make GPS accuracy and simulated/demo data obvious; never blend simulated telemetry with a real trip.
- Add a one-tap photo, scenic stop, hazard, water/fuel/camp stop, fish/observation, or emergency waypoint, depending on activity.
- Allow manual notes even when no GPS or sensor is available.

### Universal AI Explore Copilot
- Ask natural questions such as “Find a quiet 3-hour cycling loop near this town”, “What can we do on this island over two days?”, “Find a safe campsite before sunset”, or “Suggest scenic stops on this drive”.
- Generate alternative itineraries for time, weather, transport, difficulty, mobility and cost constraints.
- Summarize review themes and route reports with links to original sources and report dates.
- Explain what to prepare: packing list, permits/licence links, ferry timing, seasonal closures and weather-dependent alternatives.
- Create captions and stories from actual user-selected media and recorded trip facts. Never fabricate a GPS path, a landmark visit, wildlife identification, legal permission or an event.
- Use server-side secrets and entitlement checks for paid AI. Provide useful manual/offline fallbacks and user control over media uploads.

### Safety and responsible exploration
- Trusted-contact live sharing when connectivity exists; visible start/expiry and explicit consent.
- Check-in timer and expected-return time. Make it clear when a notification can or cannot be delivered.
- SOS calls/messages through supported device capabilities; no claim of guaranteed emergency dispatch without a contracted, tested service.
- Route-deviation and weather/closure warnings based on real sources, with the timestamp and data-quality limits shown.
- Pre-trip checks tailored to motorbike/car/bike, hike, camp, water sport, fishing/hunting and diving.
- Protected area, fire, wildlife and access-rule warnings sourced from relevant local authorities.
- Diving UI must explicitly defer depth, gas, ascent-rate and decompression decisions to certified equipment/training; do not implement improvised decompression calculations.
- Hunting/fishing flows must prioritize legal seasons, licenses, land access, bag/catch limits and conservation; don't present a user post as official regulation.
- In motion, default to voice/large targets and make nonessential content unavailable while navigation or a safety prompt is active.

## Release phases

### Phase 0 — Reliability and data foundation
- [ ] Keep Android/Functions CI green on the merged main tree.
- [ ] Fix model/merge regressions before adding mode features.
- [ ] Introduce a typed activity taxonomy and persist the activity mode on each session.
- [ ] Define shared trip, itinerary segment, route, waypoint, POI, track point, media asset and privacy models.
- [ ] Remove hard-coded single-region assumptions from story generation and support custom destination text.
- [ ] Label each screen/metric that is demo, stubbed or backed by live data.

### Phase 1 — Cross-activity MVP
- [ ] Motorcycle, cycling, car/road-trip, walking and trekking flows using the common GPS recorder where appropriate.
- [ ] Activity-aware titles, summaries, waypoint types and session recap; motorcycle lean-angle UI only appears for motorcycling.
- [ ] New-place discovery with saved places, multi-stop route drafts and source-attributed place information.
- [ ] GPX track export/import and trip media linked to timestamped waypoints.
- [ ] Generic multi-activity story generation, not a ride-only story prompt.
- [ ] Unit, instrumentation and real-device tests for GPS permissions, no signal, pause/resume, background behavior and privacy.

### Phase 2 — Multi-day travel and camping
- [ ] Multi-day itinerary planner with day-stage split, overnight stops and editable alternatives.
- [ ] Campsite/hostel/lodging and points-of-interest source integration; show verification time and link to booking/permit source.
- [ ] Personal and activity-specific packing lists, budget/fuel/food calculator, weather windows and return-by reminders.
- [ ] Car-trip and overlanding POI classes (fuel/charging, parking/tolls, repairs, recovery, clearance/terrain caveats).
- [ ] Offline region download/cache, route files and conflict-safe sync.

### Phase 3 — Water, nature and regulated activities
- [ ] Fishing log, tide/wind/marine conditions, season/licence source links and private catch/spot logs.
- [ ] Lawful hunting/wildlife observation checklist, source-based access/season restrictions and precise-location privacy.
- [ ] Scuba/snorkelling site explorer, current/visibility/weather data, buddy checklist and basic logbook.
- [ ] Dive-computer import only through documented partner/data formats; all decompression and dive-safety decisions remain with certified devices and training.
- [ ] Kayaking/boating launch points, tides/currents, weather limits and trip-return check-ins.

### Phase 4 — Creator, community and intelligence
- [ ] Story Studio templates for bike, cycle, road trip, trekking, camping, fishing, diving and destination travel.
- [ ] Route map overlays, chapters, geotagged media, multilingual captions and portrait/landscape exports.
- [ ] Community routes/events, moderation, verified condition updates and club goals across activities.
- [ ] AI itinerary ranking based on stated preferences, report freshness, accessibility, budget and actual constraints.
- [ ] Creator export workflows and any monetization integration only after permission, API policy and privacy review.

## Common data model proposal

- **AdventureTrip:** owner, title, trip dates, destination, participants, stages, budget, packing list, safety plan and privacy.
- **TripSegment:** segment order, activity type, route/transport, start/end, duration, difficulty, planned/actual state and activity-specific extension.
- **Route:** geometry/GPX reference, surface/terrain, elevation profile, accessibility, provenance, update time and restrictions.
- **Waypoint / Place:** typed location with notes, hours/season, source, confidence, last verified time, privacy and media.
- **ActivitySession:** actual recording source, GPS accuracy, timestamps, metrics available for that mode, demo flag and local-sync state.
- **MediaAsset:** owner, consent, file reference, timestamp, optional location, activity/segment link, derived captions and retention policy.
- **ConditionReport:** source type, report time, expiry, moderation/reliability state and visibility.
- **SafetyPlan:** contacts, check-in interval, expected return, fallback route, known risks and delivery status.

Persist a versioned shared envelope and keep specialized metrics in typed extensions; don't put every activity-specific value into one enormous nullable telemetry object. Preserve units and source timestamps.

## Architecture guardrails

- Android/Kotlin UI and local persistence first; remote sync must not be required to stop/save a session.
- Add Room-backed trip/session data and a map provider with explicit offline cache/licensing before promising offline maps.
- Firebase should hold authenticated user-owned content and callable AI services under least-privilege rules; validate uploads, owner, size and quota on the server.
- External place, route, weather, tide, permit and reservation information must have a data-source adapter so a provider can be replaced without redesigning activity models.
- Keep provider API keys/secrets on the server. App Check helps protect backend access but is not a substitute for authorization.
- Use test doubles for provider outages, stale data, unavailable connectivity and quota failures.

## Validation and launch gates

- Run automated Android compilation/unit tests and Cloud Functions compilation/tests for every proposed mode addition.
- Test recording in foreground/background, low battery, denied permissions, GPS jumps, tunnels, no signal and local save/retry.
- Verify map/data licences, authoritative source coverage, update cadence and region-specific legal links before showing a status as authoritative.
- For real users, provide privacy policy, consent and deletion flows for location, media and account data; private trips must not leak into public feed/analytics.
- Test Pro purchase, restore, expiry/refund, entitlement revocation and usage quota on Google Play test tracks.
- Interview motorcyclists, cyclists, road-trippers, hikers, campers, anglers, divers and international travelers separately; one generic satisfaction score is not enough.

## Success metrics
- Successful saved sessions by activity; percent of trips usable offline; route/POI data freshness; trip-plan completion and alternative acceptance; GPS/session loss rate; story export success; safety check-in reliability; reports confirmed/expired; privacy incidents; and paid AI cost per useful story.

## Product references
- [Komoot route planner and waypoints](https://www.komoot.com/help/routeplanner)
- [Komoot sport-specific routing, offline navigation and sharing](https://www.komoot.com/features)
- [AllTrails offline map help](https://support.alltrails.com/hc/en-us/articles/37213318235028-How-to-download-maps-to-your-phone-for-offline-use)
- [Garmin Dive timeline and dive logs](https://support.garmin.com/en-GB/?faq=kEMry7X1uCA6U2SkkKpIP6&partNumber=010-GDIVE-00&tab=topics&topicTag=region_divelogs)

These references establish useful patterns, not a requirement to copy another app. Validate regional rules and data coverage before choosing providers.
