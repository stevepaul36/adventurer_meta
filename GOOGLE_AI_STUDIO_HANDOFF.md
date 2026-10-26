# Google AI Studio finishing handoff

Use this document when continuing the current repository in Google AI Studio or Android Studio. Do not regenerate the app from scratch or discard this branch.

- Branch: feat/adventurer-pro-release-foundation
- Draft PR: https://github.com/stevepaul36/adventurer_meta/pull/4
- Android app: app/
- Cloud Functions backend: functions/

## Preserve these decisions

1. Keep the existing Kotlin + Jetpack Compose app and local Media3 ride-story exporter.
2. Keep RevenueCat for Google Play subscriptions and the entitlement identifier exactly adventurer_pro.
3. Keep Gemini requests on the server in functions/src/index.ts. Never embed a Gemini API key or RevenueCat secret API key in Android.
4. Keep Firebase Authentication + App Check on callable Functions; server-side entitlement verification and the monthly AI quota remain authoritative.
5. Keep selected ride videos private and scoped to ride-videos/<Firebase UID>/...; preserve client deletion and scheduled cleanup.
6. Never commit production credentials, google-services.json, .env, service-account JSON, keystores or signing passwords.

## Copy/paste prompt for Google AI Studio

Continue the existing Adventurer Android application from branch feat/adventurer-pro-release-foundation. Do not regenerate it or replace files wholesale. Inspect the repository and current Git diff first.

Finish the current code integration in place. Preserve the Kotlin/Jetpack Compose app, Media3 video exporter, RevenueCat purchase/restore flow, Firebase Auth/App Check setup, private Storage rules, server-side Firebase Cloud Functions, server-only Gemini and RevenueCat secrets, server-side entitlement validation, monthly AI quota and temporary-video cleanup.

Do not add real credentials or commit google-services.json, .env, secret API keys, service-account JSON, keystores or signing passwords. Production secrets belong in Firebase Functions secrets. Only the public RevenueCat Android SDK key may be supplied to the Android build.

Required checks:
1. Run ./gradlew :app:assembleDebug and ./gradlew :app:testDebugUnitTest (or equivalent Gradle tasks from the project root).
2. From functions/, run npm install and npm test.
3. Fix compile/import/lint/test failures and lifecycle or UI-state problems you can actually verify. Do not disable tests or claim a failed check passed.
4. Verify selecting a new source video clears previous highlights/captions. Do not ship sample route names, timestamps or telemetry as genuine user data; export only AI-suggested timestamps for the selected source video.
5. Verify AI callables reject unauthenticated, invalid-App-Check, non-Pro and quota-exhausted callers.
6. Verify the Google Play paywall uses store-localized prices, supports purchase restoration, and never hard-codes fake prices.
7. Push code fixes to feat/adventurer-pro-release-foundation. Keep PR #4 as a draft until the latest Android CI passes and Firebase/RevenueCat/Play Console have been configured. Do not merge into main.

Report the exact commands run and their exit statuses. If a check is blocked by credentials, Firebase setup or store configuration, say so clearly.

## External setup after the code checks

### Firebase
- Choose the final Android package ID. The current generated ID is com.aistudio.adventurer.kxmpzq; decide before release.
- Register that exact package in Firebase and place the matching google-services.json locally (do not commit it).
- Enable Anonymous Authentication, Cloud Firestore, Cloud Storage, Cloud Functions and App Check.
- Configure Play Integrity for release and use the debug App Check provider only for development.
- Deploy the included Firestore/Storage rules and Cloud Functions after selecting your project.

### Server-side secrets
Run these using the Firebase CLI; never put them into Android config:

    firebase functions:secrets:set GEMINI_API_KEY
    firebase functions:secrets:set REVENUECAT_SECRET_API_KEY
    firebase deploy --only functions,firestore:rules,storage

Use a Gemini Developer API key for GEMINI_API_KEY and a RevenueCat secret API key with subscriber-read access for REVENUECAT_SECRET_API_KEY.

### RevenueCat and Google Play
- Connect a RevenueCat Android app to Google Play Console.
- Create entitlement ID exactly adventurer_pro.
- Create real monthly/annual subscription products, map them to the entitlement, and create a current RevenueCat offering.
- Set REVENUECAT_TEST_API_KEY for development/Test Store use and REVENUECAT_API_KEY for Google Play internal testing/release builds.
- Test purchase, cancellation, pending payments, restore, grace period, expiry, refunds/revocation and backend denial for inactive subscribers.

### Release readiness
- Pass the latest Android assembly/unit tests and the Cloud Functions tests.
- Configure signing credentials securely and build a signed Android App Bundle.
- Provide hosted privacy policy, terms, support and an account/data deletion workflow appropriate to the final account system.
- Replace mock feed/repository, seeded profile/stats and mock club/roadside listings before marketing them as live features.
- Complete Play Console privacy/Data Safety and other declarations, then test purchase restoration, video processing, export, offline/error recovery and screen resumes on physical devices.

## Honest current scope

This branch is a release foundation, not a published live product. Social/feed and several community views remain mock-backed; anonymous Firebase Auth is a per-install identity, not complete account linking. Firebase/RevenueCat setup and signed store distribution must be completed and verified in your own projects.
