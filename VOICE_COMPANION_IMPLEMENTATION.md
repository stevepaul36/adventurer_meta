# Adventurer Voice Companion & Live Guide — implementation handoff

## Product result

Adventurer now has two voice paths:

1. **Voice commands / fallback** — tap Talk Now for a single speech-recognition turn, or explicitly enable “Hey Adventurer” while the app is foregrounded. Speech commands route to deterministic actions and Android TextToSpeech replies.
2. **Live AI Guide** — explicitly start a realtime full-duplex audio session. Audio is captured as PCM16 mono at 24 kHz and sent over a WebSocket to Boson Higgs Realtime using an ephemeral, short-lived client token minted by Firebase Cloud Functions. Returned audio is played through an Android AudioTrack. The session handles interruption events by cancelling a response, flushing pending playback, and truncating conversation audio to the amount that actually played.

Live Guide runs separately from the basic recognizer. It supports natural follow-up questions, interruption while the guide speaks, source-backed nearby map context, exact-title Wikipedia introductory summaries for named place questions, spoken app actions, and locally persisted guide preferences.

## Reference architecture adapted

The supplied RoamAlong repository separates the audio transport, realtime model, tools, geospatial research and pacing. Adventurer carries that separation into the native Android/Cloud Functions stack:

- `AdventureRealtimeVoiceSession.kt` owns PCM capture/playback, WebSocket lifecycle, VAD configuration, event processing, tool execution, interruption and playback truncation.
- `AdventureVoiceController.kt` owns the lightweight one-shot/foreground wake phrase experience and local TTS fallback.
- `AdventureGuidePreferences.kt` persists topic interests, narrative style, suggestion level and live narration tone locally.
- `MainActivity.kt` wires realtime model tools to real app navigation, selected activity, tracker status and user-triggered place lookups.
- `functions/src/index.ts` mints a short-lived Boson credential, calls OpenStreetMap Overpass for nearby named map features, and uses Wikipedia's API for exact-title introductory summaries.
- `functions/src/nearbyPlaces.ts` rounds spatial cells, normalizes and ranks nearby candidates by distance and, when movement provides a useful heading, approximate relative direction.

The model cannot execute arbitrary Kotlin. It can request a small explicit tool allowlist, and the app returns whether the action succeeded. It cannot dispatch SOS or end/delete an activity by voice.

## Shipped voice actions

- Navigate: back, next/previous main screen, Feed, Explore & Plan, tracker, Story Studio, Safety Help and profile.
- Activity changes: motorcycling, cycling, walking, trekking, car trips, camping, fishing, hunting, wildlife watching, scuba, snorkelling, paddling, boating, islands/coasts, and other supported activity modes.
- Tracker: start a real session when location permissions are available, pause/resume, and read current distance/time/GPS-fix state.
- Place questions: silently prefetch a small nearby context list after Live Guide starts when location access and a fresh usable fix are available. When the user asks about a nearby/unnamed object, the guide can consult that candidate list. For a specific named place, it can query an exact-title Wikipedia summary. Source details are returned alongside summary text.
- Personalization: locally saved topic interests, narrative style, suggestion level and one of the supported live narration tones. The user can change these with the UI or explicitly ask Live Guide to save a preference.
- Tour/Wander: updates the realtime instruction context. Suggestion level controls whether an answer includes a follow-up idea; it does not enable unsolicited GPS-triggered speech.
- Voice quality of life: full-duplex voice interrupt handling, playback flush/truncation, speech stop on the basic path, language following Adventurer’s selected app language for fallback TTS, local voice preference, and coarse action analytics.

## Server setup required before Live Guide works

The Android app does **not** contain a Boson API key. Live Guide requires your own configured Boson key and a deployed Firebase backend:

1. Set the server secret using the Firebase CLI: `firebase functions:secrets:set BOSON_API_KEY`.
2. Deploy Cloud Functions from this repository: `firebase deploy --only functions`.
3. Confirm that Firebase Auth and App Check are configured for the Android app and that the Firebase project is configured for the same deployment.
4. In Android, open Voice, grant microphone permission, and tap **Start Live AI Guide**. If the secret/backend is unavailable, the status displays a failure and Talk Now remains a separate fallback.

The callable that mints a token is authenticated and App-Check protected. It requests a limited-lifetime ephemeral token (10 minutes), and only that ephemeral token is sent to the Android WebSocket connection. Live Guide sends the microphone audio stream to the configured Boson Realtime service while the session is active.

The deployed functions must use the Boson endpoint, current model/audio protocol, and secret corresponding to the provider account. Provider availability, account credit/quota and current SDK/server semantics must be confirmed on a real device before a public release. Higgs Realtime currently uses its provider default voice; Natural/Warm/Calm/Energetic/Clear are prompt-style guidance, not selectable named voice IDs or voice cloning.

## Nearby research, sources and privacy

- The initial nearby search uses a single Overpass request around a rounded coordinate cell and caches public OSM elements for six hours (with a limited stale-cache fallback). It does not perform five separate searches for directional sectors.
- Coordinates are sent from Android to Adventurer's authenticated Firebase callable only when Live Guide performs a nearby lookup/prefetch. The server rounds the latitude/longitude before using the public map API and before placing the spatial key in the shared cache; exact coordinates are not intentionally written to analytics or the shared place cache.
- OSM features are **possible map candidates**, not camera recognition, guaranteed POI identity, proof of access, or safety/navigation data. Heading is an approximation, not eye gaze.
- Wikipedia enrichment is an introductory summary only when an exact normalized article title matches. A close but non-exact match is not used as a fact source. Article claims can still be incomplete or outdated.
- The app does not put audio, transcripts, destination queries, exact coordinates, source URLs or map geometry in voice analytics. Analytics records coarse action names/event counts only.
- Topics/style/proactivity/voice preferences are stored in device-local SharedPreferences and passed into the Live Guide prompt. They are not uploaded as analytics.
- The public Overpass server is suitable only for a careful prototype-level request pattern; it can rate-limit or become unavailable. Before broad production rollout, evaluate a provider contract, fair use, attribution, cache rules and reliability. Do not bulk-download public OSM raster tiles or treat the public Nominatim service as unlimited autocomplete.
- A nearby result or Wikipedia story does not verify current opening hours, trail/road closures, permits, weather, tides/currents, wildlife law, emergency services, diving status or safe access. Those require official, current source integrations.

## Safety and platform boundaries

- The basic “Hey Adventurer” recognizer loop is **foreground-only**, explicitly opted into, and pauses when the app backgrounds. Android's standard SpeechRecognizer is not a supported endless background hotword service; depending on the installed Android speech provider, recognition may send audio to a remote recognition service.
- Live Guide needs microphone permission and runs only while Adventurer is foregrounded; moving the app to the background disconnects it. The session does not continue recording with the app closed.
- For a motorcycle/car activity, Live Guide requires a real GPS tracker session to be running while stationary. It disconnects when that GPS-confirmed session passes the conservative movement threshold. This is a guard, not a distraction detector. Do not use it while driving/riding; use local controls/short basic commands safely.
- A voice request for SOS opens Safety Help and asks the user to confirm any emergency action; it never silently dispatches an alert. “End trip” opens tracker controls rather than deleting/stopping data without confirmation.
- The app does not proactively narrate every nearby place. Nearby context is prefetched silently on explicit Live Guide startup and used for answers after user turns. Automatic place-arrival narration, route generation/rerouting, automatic background check-ins, emergency notification delivery and licensed offline map packs are not enabled here.
- The application never claims that GPS heading determines where the user's eyes are pointing.

## Testing and release checks

The Android GitHub Actions workflow builds the debug APK and runs unit tests. The Cloud Functions workflow builds and tests the backend. These automated tests cannot replace actual-device verification.

Before release, test:
1. Secret absent, token rejected/expired, App Check/auth failure, service outage, exhausted provider credits and retries.
2. Fresh GPS fix vs. permission denied, inaccurate/stale fix, no network and no map candidate.
3. Echo cancellation/noise suppression, headset/Bluetooth audio routing, audio interruptions, incoming phone call, app backgrounding, and TTS/microphone cleanup.
4. Barge-in latency and whether unplayed audio is removed from both speaker playback and assistant conversation context.
5. Vehicle guard at standstill, on a confirmed moving GPS session and after pause; ensure no voice session remains active on movement.
6. Preference saving, app restart, dynamic session update and no query/transcript logging.
7. Place/source narration wording: verify that facts are attributed to OSM/Wikipedia and never presented as live safety/access guidance.

## Current non-goals

- OS-level/custom always-on wake-word listening when the app is closed.
- Provider-driven live weather/tide/road/trail/access/permit/condition data.
- Verified directions, map matching, route optimization, or official offline topographic/marine charts.
- Automatic landmark-arrival speech, background monitoring, emergency dispatch or automatic contact notifications.
- Provider-independent speech-model operation; Live Guide is optional and requires a deployed Boson/Firebase backend.
