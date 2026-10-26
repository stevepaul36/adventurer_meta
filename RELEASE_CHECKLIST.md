# Adventurer production release checklist

This checklist separates code that exists from external launch configuration that is still required.

## Billing and monetization

- [ ] Create a RevenueCat project and connect the Android app to Google Play Console.
- [ ] Create entitlement ID `adventurer_pro` (case-sensitive).
- [ ] Create monthly and annual subscriptions in Play Console. Set the actual price, trial terms, and regional availability there; do not hard-code prices in the app.
- [ ] Import the subscription products into RevenueCat, attach them to `adventurer_pro`, and create a current offering (e.g. `default`).
- [ ] Set `REVENUECAT_TEST_API_KEY` for debug builds and `REVENUECAT_API_KEY` for release builds via Gradle project properties or environment variables. These are public SDK keys; do not use a RevenueCat secret API key in the app.
- [ ] Test successful purchase, cancel, pending/declined payment, expired subscription, refund/revocation, and restore on a Google Play internal testing track with license testers.
- [ ] Verify the paywall displays localized Google Play prices and accurate plan/renewal disclosures.

## Before selling AI usage

- [ ] Move AI model calls behind an authenticated backend with App Check enforcement.
- [ ] Verify subscription entitlements server-side and apply per-user quotas/rate limits before each costly AI call.
- [ ] Do not accept a client-supplied `isPro` flag as proof of entitlement.
- [ ] Track subscription changes without logging raw video, health details, or exact route histories unnecessarily.
- [ ] Define retention/deletion policy for uploaded footage and ensure user-owned content is deleted when expected.

## Android release

- [ ] Add production Firebase configuration and register Play Integrity/App Check; never rely on the debug App Check provider in release.
- [ ] Add a production upload signing key through release build variables; keep the keystore and passwords out of Git.
- [ ] Verify the production application ID, version code/name, app icon, screenshots, support email, privacy-policy URL, and terms URL.
- [ ] Complete Google Play Data safety, ads declaration (if applicable), content rating, target-audience, and account/data-deletion declarations as applicable to the actual features.
- [ ] Test permissions, offline ride recording, interrupted media export, low storage, background/foreground behavior, and crashes on physical devices.
- [ ] Confirm no fabricated rider stats, route names, medical/safety claims, or demo content is shown as a real user record.
- [ ] Create signed AAB, run pre-launch report, staged rollout, crash/ANR monitoring, and rollback procedure.

## Current implementation status

The branch adds RevenueCat initialization, store-backed packages/prices, purchase/restore flows, entitlement-derived Pro state, and app-screen gating for AI highlights/captions. Firebase AI calls are still initiated in the Android client, social/feed data is still mock-backed, and there is not yet a production account/backend, deletion workflow, subscription webhook, or AI quota enforcement. Do not advertise those pieces as complete until implemented and tested.
