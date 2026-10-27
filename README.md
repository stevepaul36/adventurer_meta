# Adventurer — Multi-Activity Adventure, Exploration & Storytelling

**Discover a place. Plan the experience. Go explore. Bring the story home.**

Adventurer is evolving from a motorcycle-first prototype into a multi-activity adventure companion for cycling, car road trips, overlanding, walking, trekking, camping, fishing, lawful hunting and wildlife observation, scuba/snorkelling, island trips, and discovering unfamiliar places. Its shared foundation is destination discovery, trip planning, offline-ready routes, safety planning, GPS activity records where appropriate, and AI-assisted storytelling. The current Android app remains a prototype: motorcycle telemetry is the most developed mode, and specialized route, environmental, underwater, and regulatory features described in the product blueprint are roadmap items, not all live services.

## Product features

- **Adventure Voice Companion:** one-shot Talk Now, optional foreground “Hey Adventurer” commands, spoken app navigation, activity switching, tracker start/pause/resume/status, and Android TTS fallback.
- **Live AI Guide (optional backend feature):** native 24 kHz duplex audio over a Boson Higgs Realtime WebSocket, barge-in cancellation/playback truncation, safe application tools, nearby OSM context prefetch and movement-based quiet refresh (never unsolicited speech), exact-title Wikipedia summaries for named places, and locally saved guide preferences (interests, narrative style, suggestion level and live narration tone). The user starts the microphone explicitly; no provider key is packaged in the APK.
- **Voice safety and limits:** Live Guide requires a configured Firebase backend secret and Auth/App Check, runs only in the foreground, and stops for a GPS-confirmed moving motorcycle/car session. The basic wake phrase also runs only in the foreground; this is not an OS-level always-on assistant. OSM/Wikipedia are reference sources, not verified navigation or current safety/access feeds. Automatic place-arrival narration, offline map packs and background SOS are not enabled.

Setup details and a device-test checklist are in [VOICE_COMPANION_IMPLEMENTATION.md](VOICE_COMPANION_IMPLEMENTATION.md). Live Guide requires deploying Cloud Functions and running `firebase functions:secrets:set BOSON_API_KEY` in your Firebase project.


- **Ride telemetry HUD:** speed, altitude, elapsed time, distance and sensor-based lean-angle foundation.
- **Expedition concepts:** routes, contingency plans, high-altitude guidance and trip-budget tools.
- **Roadside radar/SOS UI:** prototype interface only. The app does not connect to a live rescue network, notify responders, or verify roadside contacts. Seeded fake incidents and service listings are disabled; the UI explicitly warns users to call local emergency services directly.
- **AI Ride Story Studio:** select a video, ask AI for candidate highlights, edit timestamps, generate captions and export a local MP4 using Media3 Transformer or save EDL JSON.
- **Adventurer Pro:** RevenueCat subscription paywall, store-priced purchase flow, restore purchases, and Pro gating for AI generation.

## Current implementation: Explore & Plan experiment

The current feature branch adds an initial **Explore & Plan** screen and a useful local-first workflow:

- Search a destination or interest by opening an external maps/search provider. This is not an in-app verified-place API yet.
- Build a multi-day draft, choose multiple activity types, add activity-relevant stop ideas, and save it locally with Room.
- Store a preparation checklist, expected return and trusted-contact label with the draft.
- Record a manual local check-in, export the plan/checklist as JSON, and create local route-guide/event drafts.
- Choose activity-specific Story Studio templates; generate an editable story from user-selected moments using the configured model or an offline template.
- Record privacy-conscious funnel events locally and, when Firebase Analytics is configured, send aggregate action events without destinations, exact coordinates, contact labels or route geometry.

**Important limits:** this first slice does not yet include downloadable offline map tiles, authoritative trail/road/weather/tide/permit feeds, background SOS/check-in delivery, publicly shared/verifiable community places, publishing/RSVPs for events, a paid guide marketplace, payments/KYC, or bike-computer/OBD/dive-computer hardware integrations. It labels plan data as local and provider-dependent rather than implying those services are live.

## Multi-adventure product scope

The app should share one trip and story system across activities instead of treating everything as a motorcycle ride. The profile's preferred mode list is being expanded, but each activity-specific workflow must still be implemented and validated before it is advertised as supported.

- **Land mobility:** motorcycle touring, road/gravel/MTB cycling, car road trips, 4x4 overlanding, walking, hiking and trekking.
- **Outdoor stays:** camping, backpacking, campsite discovery, gear and food checklists, and multi-day stages.
- **Nature and water:** fishing, lawful hunting/wildlife tracking, wildlife watching, kayaking/boating, snorkeling and scuba logbooks.
- **Destination exploration:** new-city discovery, hidden gems, viewpoints, food stops, national parks, coastlines and island itineraries.
- **Creator workflow:** geotagged photos/video, cross-activity highlight reels, route overlays, multilingual narration, and shareable trip stories.

The detailed capability matrix, safety boundaries, common data model and rollout plan live in [MULTI_ADVENTURE_PRODUCT_BLUEPRINT.md](MULTI_ADVENTURE_PRODUCT_BLUEPRINT.md). The current generic GPS recorder does **not** replace an underwater dive computer, certify hunting/fishing regulations, or guarantee off-grid emergency dispatch.

## Voice-first Adventure Copilot

The product strategy, provider boundaries, confirmation policy, UX requirements and phased roadmap are documented in [ADVENTURE_COPILOT_PRODUCT_PLAN.md](ADVENTURE_COPILOT_PRODUCT_PLAN.md). The [2019–2026 voice-first product research](ADVENTURER_VOICE_FIRST_PRODUCT_RESEARCH_2019_2026.md) explains the low-data architecture, short speech UX, fuel planning, camera permissions and staged roadmap. A deterministic fuel-range calculator is now present with unit tests; it is not yet wired to live route data, automatic reminders or vehicle telemetry. The current implementation connects the existing voice command for nearby-place questions to the authenticated GPS/OSM lookup and reports the results as map candidates with explicit limitations. It does not perform visual recognition, live traffic/ETA calculation, restaurant booking, food ordering or arbitrary cross-app tapping.

## Interactive India/Canada UI preview

A full-screen responsive product prototype is available at [`prototypes/india-canada-market-preview/index.html`](prototypes/india-canada-market-preview/index.html). Open the file directly in a modern browser—no build, package install, API key or server is required. The companion [preview README](prototypes/india-canada-market-preview/README.md) lists the flows to try and their limitations.

The preview includes market switching, regional provider catalogue, search filters, local trip stop editing/export, reminders/checklists, fuel estimation, a food cart/review/confirmation simulation, a private memory timeline and a deterministic voice-demo workspace. It is a design/interaction sandbox, not a deployed service and not the Kotlin app. Run `node scripts/validate-market-preview.mjs` from the repository root to check the prototype's JavaScript syntax and required interactions.

## India and Canada market support

Explore & Plan now includes an explicit India/Canada service selector, category filters, regional provider cards and market-aware Google Maps search. The selection is user-editable and seeded from the device country only; it does not use GPS to choose a market. Provider cards are **external website handoffs only**—Adventurer does not read their private screens, live inventory, cart/booking data, payment state or receipts.

The current catalogue includes examples such as Google Maps/Mappls, Swiggy/Zomato, IRCTC/redBus and MakeMyTrip for India, plus Google Maps, Uber Eats/DoorDash/Skip, Parks Canada, AllTrails, VIA Rail and PlugShare for Canada. It is a starting catalogue, not a signed partnership or a promise of market-wide availability. See [India + Canada Market Playbook](INDIA_CANADA_MARKET_PLAYBOOK.md) for the integration ladder, user preference model, privacy guardrails, roadmap and research sources.

**Currency handling:** trip budgets and creator-draft suggested prices now store an explicit amount plus ISO currency code (INR or CAD). Room migration v1→v2 preserves legacy amounts as CAD without conversion; the planner lets users choose budget currency and formats totals accordingly. This is currency-aware storage and formatting, not live FX conversion or full translation/localization.

## Technology

- Kotlin, Jetpack Compose, Android lifecycle/ViewModel/StateFlow.
- Media3 Transformer for local video trimming/concatenation.
- Firebase Auth, App Check, Cloud Functions, private Cloud Storage and Firestore security rules.
- Server-side Gemini API and RevenueCat subscriber verification for paid AI.
- RevenueCat Android SDK for Google Play subscriptions.

## Secure AI request flow

1. Firebase Anonymous Authentication provides a per-install UID.
2. RevenueCat is initialized with that UID, keeping purchase identity aligned with the server.
3. Videos upload to `ride-videos/<uid>/<random-file>.mp4`; Storage rules cap them at 14 MiB, restrict uploads to the authenticated owner and deny client reads.
4. The app calls a Firebase callable Function using Auth and App Check.
5. The server checks the live `adventurer_pro` entitlement through RevenueCat's subscriber API, reserves quota transactionally, then requests Gemini output.
6. The app deletes its temporary video after the call. A scheduled backend cleanup deletes abandoned video uploads older than 24 hours.
7. AI allowance is currently capped at 30 successful generations per user per UTC month. Keep or adjust that limit only after reviewing model costs.

## Configure Firebase, RevenueCat and server secrets

The repository intentionally does **not** contain production `google-services.json`, a Gemini key, a RevenueCat secret key, or signing material.

1. Register the Android app in Firebase using the application ID in `app/build.gradle.kts`. Add the matching `app/google-services.json` locally.
2. Enable **Anonymous** sign-in in Firebase Authentication, plus Cloud Firestore, Cloud Storage and Cloud Functions.
3. Configure App Check for the Android app: Debug provider for local development; Play Integrity for production. Do not use debug tokens in production.
4. Create a RevenueCat Android app connected to Google Play. Create entitlement ID exactly `adventurer_pro`, attach monthly/annual subscription products and configure a current offering (for example `default`).
5. Provide `REVENUECAT_TEST_API_KEY` for debug builds and `REVENUECAT_API_KEY` for Google Play test/release builds through Gradle properties or environment variables. A RevenueCat Test Store key is for Test Store testing only; Google Play billing tests need the public Android SDK key.
6. Configure server-only secrets:
   ```bash
   firebase functions:secrets:set GEMINI_API_KEY
   firebase functions:secrets:set REVENUECAT_SECRET_API_KEY
   ```
   Use a Gemini Developer API key and RevenueCat **secret** API key with subscriber read access. Never put those secrets in the Android app, `.env`, Git, or Gradle properties.
7. Review the `GEMINI_MODEL` default in `functions/src/index.ts`, select your Firebase project, and deploy:
   ```bash
   firebase use <your-project-id>
   firebase deploy --only functions,firestore:rules,storage
   ```
8. Use a Google Play internal testing track and license testers to test purchase, cancel, restore, pending payment, expiry/refund, quota exhaustion, App Check rejection and offline/error recovery.

More detail: [functions/README.md](functions/README.md), [RELEASE_CHECKLIST.md](RELEASE_CHECKLIST.md), and the [Voice, Chat & Story Studio Reliability Plan](VOICE_AND_STORY_RELIABILITY_PLAN.md), including the physical-device acceptance checklist.

## Build and tests

- Android: GitHub Actions workflow `.github/workflows/android-build.yml` assembles the debug APK, runs unit tests, and uploads `adventurer-debug-apk` as a 14-day CI artifact when tests pass. Download it from the successful [Android build workflow](https://github.com/stevepaul36/adventurer_meta/actions/workflows/android-build.yml); this is a debug/test build, not a Play Store release.
- Functions: `cd functions && npm install && npm test` runs TypeScript compilation and entitlement/path unit tests.
- Local MP4 rendering happens on device; server-side Gemini requests need network access, deployed Functions, valid secrets, Auth and App Check.

A green CI workflow verifies compilation and automated tests only. It does not configure Firebase/RevenueCat, verify Play Billing on a physical device, or make the app production-ready by itself.

## Before public release

- Select an application ID you own; the current one is a generated `com.aistudio.*` ID.
- Implement real account linking, persistent user-owned profiles/feed data and appropriate account/data deletion.
- Host a privacy policy, terms and support page that match actual route/location/video data flows.
- Complete Play Console declarations, configure signing secrets, test all purchase and restoration paths, set budgets/alerts, and stage rollout.
- Replace mock rider stats, feed posts, groups and service listings before marketing them as live community features.

## Google AI Studio handoff

Use [MULTI_ADVENTURE_PRODUCT_BLUEPRINT.md](MULTI_ADVENTURE_PRODUCT_BLUEPRINT.md) for the cross-activity vision and implementation backlog. Use [GOOGLE_AI_STUDIO_HANDOFF.md](GOOGLE_AI_STUDIO_HANDOFF.md) for the repository-aware coding prompt and exact Firebase/RevenueCat/Google Play setup steps. Extend this repository in place; do not replace it with a new starter.

**Ride safe. Lean deeper. Tell better stories.**
