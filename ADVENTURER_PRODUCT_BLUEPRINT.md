# Adventurer product blueprint: beyond fuel stops

## Product promise
Adventurer is a voice-first journey co-pilot coordinating navigation, trip planning, food, fuel, stays, memories and adventure milestones. It is not a replacement map, delivery marketplace or unrestricted phone operator. Specialist providers handle live navigation and transactions; Adventurer owns trip context, personal preferences, reminders, decision support and a unified history.

## Feature domains
- Journey: current trip, next planned stop, route progress when supplied by an approved source, concise briefing.
- Navigation handoff: open a selected map provider with a destination or directions; distinguish handoff from verified navigation.
- Explore: fuel, food, coffee, stays, viewpoints, repair, pharmacy, toilets and rest stops, with provider and freshness.
- Food: dietary preferences, menu browsing, cart, pickup/delivery, destination, fee/total review, explicit order confirmation and provider receipt verification.
- Fuel: user-entered fuel level, tank size, mileage, reserve, buffer, route distance to a candidate stop, stale-input and uncertainty handling.
- Trip plan: route legs, user-defined stops, reminders, checklist and editable schedule.
- Memories: user-approved photo/video, notes, location/time metadata controls, trip timeline and story export.
- Voice: concise answers, interrupt/cancel, repeat, pause, confirmation and action-state history.
- Adventure passport: visited places, activity milestones, challenge cards, badges, progress and optional sharing.
- Profile/settings: activity/vehicle profiles, language, voice style, data saver, privacy, offline status and provider connections.

## Interaction loop
1. Understand request and trip context.
2. Use local calculations or a narrow provider query only if needed.
3. Present a concise recommendation with source, freshness and uncertainty.
4. Ask before consequential actions.
5. Execute via supported app intent or official API.
6. Verify outcome using provider confirmation or observed state.
7. Save only useful, consented information to the trip journal.

## Low-data principles
- Do not redraw/stream a full map when an external navigation app can do the route.
- Local fuel math, reminders, checklists, notes and memories should work offline.
- Query nearby data on demand; cache only what provider terms allow.
- Prefer route/geofence events over high-frequency GPS polling when supported.
- Never continuously upload audio/video by default. Camera starts visibly and explicitly.
- Show whether a feature is local, online, demo or unavailable offline.

## Transaction and safety rules
- Food/order: review exact items, quantities, delivery location, fees and total; confirm freshly; verify provider receipt. Prototype confirmation is simulation only.
- Maps: opening a map app is a handoff, not proof navigation started.
- Fuel: rider-provided estimate is not a sensor reading. Include reserve, buffer and uncertainty; don't promise a station is open or stocked.
- Moving vehicle: no typing or visual interaction prompts; concise safety-critical audio only, defer noncritical conversation until stopped.
- Camera: explicit start/stop and permission, local-first storage, user review and clear retention/delete controls.
- SOS: never claim help was dispatched unless a real, tested emergency integration confirms it.

## UI hierarchy
1. Journey home
2. Explore / search
3. Trip plan
4. Memories
5. Voice and settings

Persistent compact voice dock; do not make a full-screen chatbot the only interface. Use rich cards for next actions and a compact status strip for provider, permission and freshness.

## Delivery sequence
1. Prototype full UI in Google AI Studio with deterministic sample data and explicit simulation labels.
2. Test flows with users and adjust hierarchy and voice brevity.
3. Integrate shared domain models and local persistence into the existing Kotlin app.
4. Connect approved route/place providers and protect keys server-side; add attribution, quotas and billing controls.
5. Add camera flow and memory privacy controls.
6. Add food provider integration only after API/commercial access, address/fees, payment and confirmation requirements are defined.
7. Device test, privacy/security review, accessibility review and store release gates.

## Important boundary
The Google AI Studio build is a design and interaction prototype. It does not automatically become the existing Android app. Reuse its design system and screen behavior when implementing in Jetpack Compose, or deliberately choose a shared web architecture later.
