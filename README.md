# Adventurer — Motorcycle Adventure Social, Telemetry & Expeditions

**Motorcycle-first. Offline-capable. Built for the high passes.**

Adventurer is a native Android prototype for motorcycle adventure riders and explorers. It combines ride telemetry, curated expedition concepts, roadside assistance UI, AI Ride Story Studio, social/feed screens, clubs and gamification. The repository is not yet a public-launch-ready service: profile/feed data and some community behavior remain mock-backed.

## Product features

- **Ride telemetry HUD:** speed, altitude, elapsed time, distance and sensor-based lean-angle foundation.
- **Expedition concepts:** routes, contingency plans, high-altitude guidance and trip-budget tools.
- **Roadside radar/SOS UI:** roadside service concepts and one-tap safety UI; live dispatch workflows require backend integration.
- **AI Ride Story Studio:** select a video, ask AI for candidate highlights, edit timestamps, generate captions and export a local MP4 using Media3 Transformer or save EDL JSON.
- **Adventurer Pro:** RevenueCat subscription paywall, store-priced purchase flow, restore purchases, and Pro gating for AI generation.

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

More detail: [functions/README.md](functions/README.md) and [RELEASE_CHECKLIST.md](RELEASE_CHECKLIST.md).

## Build and tests

- Android: GitHub Actions workflow `.github/workflows/android-build.yml` assembles the debug APK and runs unit tests.
- Functions: `cd functions && npm install && npm test` runs TypeScript compilation and entitlement/path unit tests.
- Local MP4 rendering happens on device; server-side Gemini requests need network access, deployed Functions, valid secrets, Auth and App Check.

A green CI workflow verifies compilation and automated tests only. It does not configure Firebase/RevenueCat, verify Play Billing on a physical device, or make the app production-ready by itself.

## Before public release

- Select an application ID you own; the current one is a generated `com.aistudio.*` ID.
- Implement real account linking, persistent user-owned profiles/feed data and appropriate account/data deletion.
- Host a privacy policy, terms and support page that match actual route/location/video data flows.
- Complete Play Console declarations, configure signing secrets, test all purchase and restoration paths, set budgets/alerts, and stage rollout.
- Replace mock rider stats, feed posts, groups and service listings before marketing them as live community features.

**Ride safe. Lean deeper. Tell better stories.**
