# Adventurer production UX acceptance gates

This checklist is a release gate, not a claim that the listed behavior already exists. Run it on the release candidate and record evidence for each item. Do not market a demo, cached result, local-only action, or unconnected service as live.

## 1. Product truth and trust

- [ ] Every feed, group, service listing, rider statistic, route, and place result is labelled accurately as live, cached, sample, or local-only.
- [ ] Every primary action ends in a visible success, pending, empty, or recoverable error state. No browser/native alert is the only feedback for a core flow.
- [ ] SOS never says a contact or emergency service was notified unless the server confirms delivery and the recipient path is configured. If dispatch is unavailable, say so plainly and provide the device's emergency-call option where appropriate.
- [ ] GPS shows permission, acquisition, accuracy/staleness, paused, and unavailable states. A location permission grant is not represented as a reliable fix.
- [ ] AI responses disclose provider/network dependency and recover gracefully from missing secrets, auth/App Check rejection, rate limits, expired tokens, no network, and provider outage.
- [ ] Subscription UI shows localized price, billing period, renewal/cancellation terms, restore purchases, and a support route. Entitlement is enforced server-side, not only by hiding UI.
- [ ] Voice and tracking have clear active indicators and a one-tap stop/pause control. No microphone or location session survives a lifecycle transition that is supposed to stop it.

## 2. Visual system and interaction consistency

- [ ] Use Material 3 theme tokens for surfaces, content, typography, shapes, and semantic states; avoid hard-coded per-screen colors that break light/dark themes.
- [ ] Apply a consistent spacing scale (4/8/12/16/24/32 dp), card radius, icon sizing, button height, and heading hierarchy.
- [ ] Every screen has one visually dominant primary action; destructive and emergency actions are visually distinct and require confirmation when appropriate.
- [ ] Interactive controls expose pressed, disabled, loading, selected, success, and error states. Disable duplicate submissions while an operation is in flight.
- [ ] Forms preserve entered data after recoverable failures and show field-level validation next to the field that needs attention.
- [ ] Empty states explain what the user can do next. Error states explain recovery in plain language; never show stack traces, raw API payloads, or secret/configuration values.
- [ ] Long content scrolls without clipping; keyboard, system bars, display cutouts, font scaling, and landscape layouts do not hide primary controls.
- [ ] Icons are paired with accessible labels where meaning is not obvious. Color is never the only signal for status.

## 3. Accessibility and localization

- [ ] Test Android font/display scaling at 1.0x and the largest supported accessibility setting; no clipped labels or inaccessible controls.
- [ ] Touch targets are at least 48 x 48 dp wherever practical.
- [ ] TalkBack announces screen titles, button purpose, selected state, GPS/recording state changes, errors, and completion feedback.
- [ ] Text/background contrast meets WCAG AA targets for normal and large text, including disabled/status colors where readable text is presented.
- [ ] Strings are resource-backed; no user-facing strings are accidentally hard-coded into feature logic.
- [ ] Verify long translated strings and right-to-left layouts before claiming localization support.

## 4. Core journey tests

### First launch and permissions
- [ ] First launch explains why location/microphone access is needed before requesting it.
- [ ] Denial, “don't ask again,” approximate-only location, revoked permission, and system settings return are handled without a dead end.
- [ ] User can explore non-location features without granting optional permissions.

### Explore & Plan
- [ ] Search/provider hand-off clearly indicates when the app opens an external service.
- [ ] Draft plan, checklist, expected return, activity selection, and saved state survive app restart as designed.
- [ ] Save/export failures retain the plan and offer retry; exported data does not include fields the user did not expect.
- [ ] No suggested place is represented as verified access, current opening hours, legal permission, or a safe route without an authoritative source.

### Tracker and safety
- [ ] Test no GPS fix, stale/low-accuracy fix, location permission loss, app backgrounding, process death, pause/resume, and duplicate start/stop.
- [ ] Distance and elapsed time remain plausible across pauses and GPS jitter; explain any known limits.
- [ ] Safety Help communicates which actions are informational versus actually transmitted. Verify server acknowledgement and delivery failures before claiming dispatch.

### Voice Companion
- [ ] Permission denial, token minting failure, expired/rejected token, network loss, Bluetooth/headset change, incoming call, backgrounding, and repeated start/stop all clean up microphone/audio resources.
- [ ] Interrupting speech suppresses queued/unplayed audio and does not leave stale assistant context.
- [ ] Vehicle-motion guard is tested on physical devices and described as a limited safeguard, not proof that interaction is safe while moving.
- [ ] Nearby place summaries identify their sources and are not framed as official safety, access, road, trail, tide, weather, or emergency data.

### Story Studio and Pro
- [ ] Test invalid/corrupt/oversized video, insufficient storage, cancellation, export interruption, app recreation, and share failure.
- [ ] Progress is determinate when possible; cancellation is real; completed files are validated before success is announced.
- [ ] Test purchase, pending payment, cancel, restore, expiry, refund/revocation, offline entitlement check, and quota exhaustion on Google Play internal testing.

## 5. Device and release validation

- [ ] Run Android unit tests and instrumentation tests where available; attach CI run URLs to the release record.
- [ ] Test at least one lower-memory Android device and one current Android version on physical hardware.
- [ ] Test cold start, slow network, offline launch, low storage, process recreation, rotation, and app upgrade from the previous release.
- [ ] Inspect Logcat and analytics for access tokens, API keys, precise coordinates, route geometry, audio/transcripts, contact labels, or other sensitive payloads.
- [ ] Verify backup/data-extraction rules match the product's privacy commitments and do not unintentionally export sensitive trip data.
- [ ] Confirm final application ID, version, signing key, Play declarations, privacy policy, terms, support channel, data deletion flow, crash monitoring, budgets, and rollback plan.
- [ ] Build and inspect a signed Android App Bundle; run Play pre-launch checks and staged rollout before public launch.

## 6. Release decision

**Do not ship publicly** while any of these are unresolved:
- credentials exposed or shipped in the client;
- safety/emergency UI claims delivery without confirmed delivery;
- account/data deletion and required privacy disclosures are absent;
- purchase entitlement can be bypassed client-side;
- core journeys crash, lose user data, or report false success;
- production services are still demo-backed but presented as live.

For each failed gate, record owner, severity, reproducible steps, target fix, and evidence of retest. A passing build is necessary but does not prove UX, privacy, billing, safety, or production-service readiness.
