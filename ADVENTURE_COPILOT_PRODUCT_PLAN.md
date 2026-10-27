# Adventure Copilot — voice-first adventure product plan

## Product promise

**Tell Adventurer what you want to do next. It researches, organizes and guides the adventure while showing its sources and asking before consequential actions.**

Adventurer is not a generic phone-automation clone. Its advantage is the shared context across destination discovery, trip planning, live activity records, roadside preparation and post-trip storytelling. The voice assistant should be the consistent front door to these flows.

## Interaction model

1. **Speak or type**: explicit Talk Now remains the default. Foreground hands-free is opt-in and visibly armed.
2. **Understand**: preserve the user's original words, resolve references only when enough context exists, and ask a short clarification otherwise.
3. **Research**: call the narrowest available provider/tool and attach provider, retrieval time and confidence/freshness to the result. Do not silently substitute mock data.
4. **Propose**: show a compact visual task card with goal, source(s), key facts and intended action.
5. **Act**: use app navigation and external deep links first; use accessibility-driven cross-app automation only with explicit permissions and robust Android support.
6. **Verify**: re-read observable state after an action. A tap accepted by Android is not proof that a booking, order or setting succeeded.
7. **Remember the useful outcome**: save an editable trip/place/packing-list/narrative preference locally, with clear controls to inspect and delete memory.

## Architecture compatible with this repository

- **Voice interface**: keep the existing Kotlin SpeechRecognizer/TTS controller for quick commands and the existing Boson duplex session for interruptible conversation. Do not add a second audio engine without a device-tested reason.
- **Orchestrator**: create typed, narrow tools (place research, trip draft, route intent handoff, activity status, story creation) behind a central policy. Tools return typed observations and source/freshness metadata, not arbitrary model-authored actions.
- **Research providers**: start with the existing authenticated Firebase callable backed by OpenStreetMap/Overpass and exact-title Wikipedia summaries. Later integrate Google Places/Routes only after enabling the specific APIs, billing and credentials. Do not scrape other apps' private UI to replicate commercial data.
- **Maps and navigation**: use app intents/deep links for Google Maps. Treat map handoff as opening an external app, not proof that navigation began. Keep an in-app source card so the user can compare places and return to the plan.
- **Data model**: separate `PlaceCandidate`, `TripDraft`, `TripStop`, `RouteLeg`, `SafetyCheck`, `ActionProposal`, and `VerificationEvidence`. Store only the minimum location history the user chooses to keep.
- **Activity safety**: while a verified motorcycle/car session is moving, block cross-app automation and lengthy voice tasks; provide short status only. Never run a continuous hotword listener in the background using APIs not designed for it.
- **Consequential actions**: food delivery, booking, messaging, payments, posting and deletion require an explicit, fresh confirmation that names the destination/vendor, item/quantity, recipient or exact change. A correction withdraws confirmation. No action from “yes” if the pending proposal changed.

## High-value feature sequence

### V1 — Adventure Copilot (this slice)
- Voice command for nearby places now uses the existing server-authenticated, permission-authorized GPS lookup.
- Search a named destination and open an external map search, with the limitation stated aloud.
- Speak current trip state from the recorder and activity type.
- Keep all responses grounded; show source/freshness/caveat when map data is used.
- Improve the voice screen with a recent transcript, sample commands and an at-a-glance connection/listening indicator.

### V1.1 — Plans users can actually use
- Build multi-stop trip drafts from researched places; distinguish candidate stops from confirmed bookings.
- Add trip time/budget/distance ranges only when a route provider returns those values; do not infer traffic or delivery ETAs.
- Save checklists and expected-return reminders locally first; make sharing opt-in.

### V2 — Cross-app assistant with safe handoff
- Maintain a single pending action object and a step-by-step visual timeline.
- Use Android deep links first; only adopt an accessibility operator after a separate permission, security and device test.
- Implement cancel/pause/interruption generation IDs so stale model decisions cannot dispatch new actions.
- Require confirmation before placing an order, sending a message, paying, booking, publishing, or deleting.
- Verify the actual destination screen/state after returning from another app; otherwise report “opened, not verified.”

### V3 — Trustworthy place intelligence and gamification
- Provider-backed opening hours, operating status, route duration and availability only when API access allows it.
- “Adventure Passport”: users save a visited place after their own check-in, with GPS precision controls and manual correction.
- Achievements for completing a planned route, recording a trip, leaving a useful review or finishing a checklist—not for entering dangerous areas or chasing speeds.
- Community places and meetups need moderation, reporting, privacy-preserving location sharing and controls for sensitive sites.

## UX direction

- A single obvious primary microphone button, separate Stop and keyboard/text fallback.
- Always show one clear state: Ready, Listening, Thinking, Speaking, Checking source, Waiting for confirmation, Paused for safety, or Couldn't verify.
- Stream the transcript and current step into an accessible activity timeline; keep the last result visible after speech stops.
- Make the source explicit in a tappable card: provider, last checked, approximate distance, and what the provider cannot verify.
- Avoid pretending the screen sees the world: this build has no camera-based visual understanding.
- Use large typography, 48dp-class touch targets, excellent contrast, TalkBack labels, predictable back navigation and reduced motion.
- Let users interrupt speech. Clearly state that an already-dispatched Android gesture or external action may still complete.

## Metrics and release gates

- Task success judged by observed final state, not model self-report.
- Clarification rate, wrong-place rate, stale-result rate, cancellation latency, unverified-action rate, and user-confirmation abandonment.
- Test speech in noisy environments and languages separately, with opt-in device sessions.
- Automated parser/tool contract tests; Firebase callable tests; Android build and unit tests; then physical device tests for microphone permissions, barge-in, maps handoff and process/lifecycle changes.
- No public claims of emergency dispatch, offline operation, visual identification, live ETA, booking/order completion or always-on background voice until each is implemented and measured.

## Current limits

Nearby discovery is a community-map lookup over a narrow radius and is not a Google Maps Places/Routes substitute. It does not establish opening hours, reservation availability, safe/legal access, closures, weather, road/trail conditions or travel time. The current command opens Google Maps for named searches; it does not control another app or order deliveries. Cross-app automation and real delivery integration are future work, not implied by this feature.
