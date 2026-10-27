# Adventurer production release checklist

## Firebase and AI security

- [ ] Set the final, owned application ID; register it in Firebase and add the matching `app/google-services.json` locally.
- [ ] Enable Firebase Anonymous Authentication, Cloud Firestore, Cloud Storage, Cloud Functions and App Check.
- [ ] Configure App Check: debug provider only for local development; Play Integrity for production.
- [ ] Set server secrets `GEMINI_API_KEY` and `REVENUECAT_SECRET_API_KEY` with Firebase CLI, never Android build properties.
- [ ] Deploy callable Functions and strict Firestore/Storage rules; test unauthorized requests and object access are rejected.
- [ ] Test active, expired, refunded, restored and unverifiable subscriptions. Verify the backend fails closed if RevenueCat cannot be reached.
- [ ] Test monthly quota concurrency and failure rollback; confirm the counter only charges successful results.
- [ ] Add budgets/alerts, model/request caps, retention rules and monitoring. Verify abandoned temporary video cleanup.

## RevenueCat and Google Play

- [ ] Create entitlement `adventurer_pro`, map Google Play monthly/annual products and configure a current RevenueCat offering.
- [ ] Set public SDK keys as environment variables/Gradle properties. Use Test Store key only for Test Store tests; use the Android public key for Google Play internal tests and release.
- [ ] On a Play internal track, test purchase, cancellation, pending payment, restore, grace period, expiry and refund/revocation using license testers.
- [ ] Verify localized price and subscription period/renewal disclosure, cancellation guidance, restore purchases and support path.
- [ ] Adjust the 30-successful-generation monthly AI limit only after estimating real inference costs.

## Safety-critical feature status

- [ ] Do not advertise Roadside Radar as live until verified provider data and a tested backend dispatch/acknowledgement workflow are deployed.
- [ ] Keep fabricated SOS reports, responder identities, phone numbers and roadside-service listings out of the runtime data source.
- [ ] Verify the UI clearly states that local-only SOS actions do not contact responders and directs users to local emergency services for urgent help.

## Android and legal release

- [ ] Configure upload signing key and passwords through secure CI secrets; keep signing material out of Git.
- [ ] Update version code/name, final package ID, icons, screenshots, support email and Play listing.
- [ ] Publish a privacy policy, terms and support page that match actual data flows; provide a user data/account deletion workflow appropriate to the chosen account system.
- [ ] Complete Play Data Safety, content rating, target audience and related declarations accurately.
- [ ] Replace mock feed/repository, demo rider statistics and seeded service locations before marketing them as live data.
- [ ] Test physical devices for permissions, offline mode, video size, low storage, interrupted uploads/exports, app resume and crashes.
- [ ] Build a signed AAB, run Play pre-launch report, stage rollout, monitor crashes/ANRs and prepare rollback.


## UI/UX and user-trust release gates

- [ ] Complete every applicable item in [PRODUCTION_UX_ACCEPTANCE.md](PRODUCTION_UX_ACCEPTANCE.md) on the actual release candidate.
- [ ] Audit all primary journeys for loading, empty, success, pending, offline, permission-denied and recoverable-error states.
- [ ] Verify no demo/local-only action is presented as a live service; remove or clearly label mock feed, seeded locations, and sample statistics.
- [ ] Verify GPS, voice, video export, subscription, and safety screens report success only after the underlying operation succeeds.
- [ ] Validate Material 3 light/dark themes, accessibility font scaling, TalkBack, touch target sizes, contrast, keyboard/system-bar insets, and small-screen layouts.
- [ ] Test physical devices for lifecycle cleanup, background/foreground transitions, network changes, Bluetooth audio routing, low storage and process recreation.
- [ ] Confirm analytics/logs contain no API keys, tokens, precise coordinates, transcripts/audio, route geometry, or contact labels.
- [ ] Record release-candidate build number, CI links, known issues, test evidence, rollback owner, and go/no-go decision.

## Implementation status

This branch adds a RevenueCat purchase/restore adapter and paywall, client Pro gates, callable Gemini endpoints with Auth/App Check enforcement, live RevenueCat entitlement verification, transactional monthly AI quota, private video Storage rules, temporary-video cleanup, and backend tests. These controls are operational only after a real Firebase/RevenueCat setup, secrets and Functions deployment. The feed/repository remains mock-backed; anonymous Auth is a per-install identity, not full account linking; production legal pages, Play listing, final signing, account/data deletion and physical-device testing remain outstanding.
