# Adventurer — Production credentials and service configuration

This inventory is based on the current repository code, not a generic shopping list. Create only the credentials for features you intend to enable. Do not paste keys into chat, GitHub issues, source files, or this public repository.

## A. Required for the currently implemented backend features

| Secret / configuration | Provider / location | Used for | Required when |
|---|---|---|---|
| `GEMINI_API_KEY` | Google AI Studio / Gemini Developer API; Firebase Functions Secret Manager | Server-side AI story/highlight generation | AI Story Studio generation is enabled |
| `BOSON_API_KEY` | Boson provider; Firebase Functions Secret Manager | Mints short-lived realtime voice credentials for Live AI Guide | Live AI Guide is enabled |
| `REVENUECAT_SECRET_API_KEY` | RevenueCat **secret** API key; Firebase Functions Secret Manager | Server-side subscription/entitlement verification | Pro-gated AI calls are enabled |
| `REVENUECAT_API_KEY` | RevenueCat **public Android SDK** key; release-build input / GitHub Actions secret | Android billing SDK configuration | Building the production Android release with RevenueCat |
| `REVENUECAT_TEST_API_KEY` | RevenueCat Test Store public SDK key; local Gradle property or environment variable | Debug build billing tests against RevenueCat Test Store | Only when using RevenueCat Test Store in debug builds |
| `app/google-services.json` | Firebase Console, registered for the exact Android application ID | Firebase Android client configuration (not a secret API key) | Local Android build/runtime with Firebase |
| `GOOGLE_SERVICES_JSON_BASE64` | GitHub Actions repository secret containing Base64 of the above file | Supplies Firebase config to signed-release CI | Building the signed release in GitHub Actions |

The current Functions source defines `GEMINI_MODEL` as a deploy-time string parameter (currently default `gemini-3.8-flash`). It is configuration, not a secret. Confirm that the model name is available to the Google AI project before deploying.

Set backend secrets from the repository root using the Firebase CLI. The CLI prompts for each value:

```powershell
firebase functions:secrets:set GEMINI_API_KEY
firebase functions:secrets:set BOSON_API_KEY
firebase functions:secrets:set REVENUECAT_SECRET_API_KEY
```

Then deploy the functions and security rules:

```powershell
firebase deploy --only functions,firestore:rules,storage
```

A Firebase project with the required billing plan, Authentication, Firestore, Storage, Cloud Functions, and App Check configuration is needed. Enable Anonymous Authentication for the current sign-in implementation. Use Play Integrity for release App Check; use a debug provider only for local development. App Check does **not** require you to paste an extra API secret into Kotlin.

## B. GitHub Actions secrets needed to build a signed Android App Bundle

These are build/signing inputs, not third-party API keys.

| GitHub Actions secret | Value |
|---|---|
| `ANDROID_KEYSTORE_BASE64` | Base64 of your persistent Play upload keystore (`.jks`) |
| `ANDROID_KEYSTORE_PASSWORD` | Keystore password |
| `ANDROID_KEY_ALIAS` | Alias in the keystore (the release workflow default guidance uses `upload`) |
| `ANDROID_KEY_PASSWORD` | Key password |
| `GOOGLE_SERVICES_JSON_BASE64` | Base64 of Firebase `google-services.json` registered for the exact application ID |
| `REVENUECAT_API_KEY` | RevenueCat **public Android SDK** key for the production app |

The release workflow checks these names and package-ID alignment before producing a signed AAB. It does not upload the AAB to Play Console.

PowerShell encoding commands (run locally; don't put output into your shell history or source files):

```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("my-upload-key.jks"))
[Convert]::ToBase64String([IO.File]::ReadAllBytes("app/google-services.json"))
```

Store the keystore and passwords in a secure password manager and encrypted backup. Losing an upload key can complicate future releases. Never commit the raw keystore, passwords, encoded strings, or production Firebase config.

## C. Keys that are **not required for the current handoff UX**

| Integration | Current status | Credential needed now? |
|---|---|---|
| Google Maps | Current examples use an external Maps URL/search handoff; no Maps SDK, Places API or Routes API client is currently integrated by the new global-market preview | **No** for the existing handoff. If you later embed maps/places/routes, create a Google Maps Platform key restricted by Android app/package + signing SHA-256 and only enable the APIs actually used. Use separate restricted keys/projects as appropriate for client versus server. |
| Mappls | India integration is a future candidate in the playbook, not an implemented API client | **No** until you choose and implement its SDK/API; request the exact credentials required for that selected Mappls product. |
| Swiggy, Zomato, IRCTC, redBus, Uber Eats, DoorDash, Skip, Parks Canada, VIA Rail, PlugShare, AllTrails | Shown as external provider handoffs/examples, not authenticated API integrations | **No** for opening provider links. Partner/API credentials and commercial approval would be needed only for supported live data or transactions. |
| Weather, road closures, trail conditions, park permits and official local regulations | No authoritative policy/conditions provider has been selected or wired | **No single key exists yet.** Choose the official/licensed feed per intended launch region, review its terms, then store its credential server-side if it is a secret. |
| OpenStreetMap Overpass and Wikipedia | Existing Live Guide server code uses public endpoints; no app-owned provider key is currently defined for these calls | No key in current code. Observe attribution, service policies, rate limits, reliability and privacy requirements before public-scale use. |

Do not obtain every possible key on this list. The only provider API secrets explicitly consumed by the current Firebase Functions source are `GEMINI_API_KEY`, `BOSON_API_KEY`, and `REVENUECAT_SECRET_API_KEY`; the app/build pipeline additionally needs RevenueCat's public SDK key and Firebase config.

## D. Production blockers that API keys do not solve

1. **Finalize the Android application ID before the first Play upload.** The current source declares `com.aistudio.adventurer.kxmpzq`. Register that exact package with Firebase for test builds, or change the ID first and then update Firebase/RevenueCat/release configuration together. Once an app is published, changing the application ID means publishing a different app.
2. Connect the global market UX to the native product flows and a real market preference/context store. The new global-market screen is a browser UX concept, not a replacement for the Android application.
3. A live policy engine needs official jurisdiction/activity-specific sources, timestamps, expiry rules, error states, and a fail-closed **unknown** status. A country selector or LLM alone must never assert legal permission.
4. Review the product's claims against actual capabilities: the app does not currently perform verified emergency dispatch, authoritative routing/closures, or provider bookings just because those controls are visible.
5. Publish the real privacy policy, terms, support route, data-deletion pathway and Google Play declarations. Test location/microphone consent, billing, restores, backend failure, security rules and App Check on real devices.
6. Configure Firebase/RevenueCat/Boson/Gemini accounts, budgets, quotas, monitoring and production source links. A green CI run cannot verify credentials you have not configured.

## E. Safe configuration checklist

- [ ] Create a separate development/test Firebase project and production Firebase project.
- [ ] Register the final Android package ID in Firebase.
- [ ] Enable Anonymous Auth, Firestore, Storage, Functions and Play Integrity App Check.
- [ ] Configure RevenueCat Android app, Play service-account connection, product(s), offering, and exact entitlement ID `adventurer_pro`.
- [ ] Set the three server-only Firebase secrets required for the features you enable.
- [ ] Use the public RevenueCat SDK key in Gradle/build configuration; never use the RevenueCat secret key in the app.
- [ ] Configure GitHub signing/config secrets only for the signed release workflow.
- [ ] Set cost/budget alerts, model quotas and rate limits before inviting testers.
- [ ] Complete the physical-device acceptance checklist in `PRODUCTION_RELEASE_RUNBOOK.md`.
