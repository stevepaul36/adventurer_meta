# Adventurer Voice Companion — implementation and handoff

## What this branch implements

This is a foreground Android voice-control slice built on the existing Jetpack Compose app and existing ActivityType, GPS tracker, Explore & Plan and Story Studio modules.

- **Talk now:** one-shot speech recognition starts only after the user taps the microphone and grants RECORD_AUDIO.
- **Hey Adventurer (opt-in):** once explicitly enabled, a lightweight wake-phrase loop runs while the Android Activity is foregrounded. The controller pauses listening when the Activity pauses/stops. It does not register an OS-level wake-word service and is not available while the app is closed.
- **Hands-free navigation:** go back, next screen, previous tab, open Feed, Explore, tracker, Voice, Story Studio, Roadside Radar, or profile.
- **Activity commands:** switch between supported adventure types; read the selected activity's existing in-app briefing; start, pause and resume the current tracker session; read local session distance/time/GPS-fix status.
- **Place intents:** “search for [place]” opens an external Google Maps search after spoken acknowledgement. “What's that?” is not guessed: this slice explicitly tells the user that verified place context is not connected yet.
- **Voice output:** Android TextToSpeech, calm/balanced/energetic delivery presets, language following Adventurer's current selected language, stop-speaking button, remembered Tour/Wander preference.
- **Privacy-conscious analytics:** coarse command action only; no transcript, raw audio, destination query, exact coordinate or route geometry is logged.
- **Voice UI:** a global voice bubble remains visible on app screens; a dedicated screen explains the supported command vocabulary and limitations.

## Reference repository ideas adopted

The attached RoamAlong reference separates realtime conversation, tools, location context, place research and proactive pacing. This Android slice preserves that separation at the product boundary:

1. the recognizer turns speech into text;
2. a deterministic command parser maps only a small allowlist into app actions;
3. the application executes actual navigation/session actions and gives a spoken response;
4. existing activity/tracker state provides local context; and
5. UI copy makes unsupported actions visible instead of allowing the model to pretend that work happened.

The current slice does **not** copy the reference's React/Expo/WebSocket implementation into the Kotlin app, and does not embed provider credentials in the APK.

## Platform limits and safety rules

Android's regular SpeechRecognizer is not intended for endless background recognition. Therefore hands-free is explicitly opt-in and foreground-only. Depending on installed Android speech services and language packs, speech recognition may require a network. Text-to-speech uses the installed device engine; a suitable local voice must be downloaded for offline narration.

When speaking “SOS” or “emergency,” the app only opens Safety/Roadside Radar and asks the user to confirm any emergency action. It never dispatches an alert based solely on speech. “End trip” opens the tracker for confirmation instead of deleting or stopping a session with a potentially misrecognized command. For a confirmed live GPS session on a motorcycle or in a car at 5 km/h or more, longer activity briefings and external map-app launches are blocked until stopped; short navigation acknowledgements remain available. This guard is intentionally conservative and is not a distracted-driving detector.

The app must not proactively speak while a motorcycle/car user is in motion. There is no automatic narration of unknown landmarks: until a source-backed place provider, route context and confidence checks are connected, location-based narration is intentionally withheld. GPS coordinates and direction alone cannot reliably infer exactly what someone is looking at.

## What remains unconnected

- **True always-on wake-word engine:** requires a supported native hotword SDK/service and licensing/configuration. This branch does not keep a microphone open after the app backgrounds or closes.
- **Realtime duplex voice agent and barge-in:** the current recognizer+TTS flow is turn-based. A provider-backed WebSocket session with short-lived server-issued credentials is required for natural interruption during speaking. Never ship a long-lived AI key in Android resources or BuildConfig.
- **Verified nearby place / landmark context:** connect a provider with explicit attribution, allowed caching, rate limiting and freshness metadata. Label AI-generated interpretation separately from retrieved facts and keep the source URL/provider provenance with each story.
- **Automatic Tour-mode place narration:** add only once place search and route/distance context are authoritative enough; make proactive narration opt-in, apply cooldowns and skip when speech, recording controls or movement safety gates say it is a bad time.
- **Offline place research:** store small, user-selected place/story packs with source timestamps and storage limits. Do not bulk-download public OSM tiles in violation of the tile service policy, or depend on the public Nominatim service for production autocomplete.
- **AI TTS/music export:** a separate Story Studio export capability using a licensed TTS provider and Android Media3 Transformer is not part of this voice-control slice.

## Before release

1. Test on real Android devices with and without an offline speech pack; speech recognition and TTS support vary across manufacturers.
2. Validate microphone permission denial, revoked permission, no recognition service, network loss, Android app backgrounding, Bluetooth/headset routing and interruptions by phone calls.
3. Check notification/foreground-service policies before any future background mic mode; avoid continuous recording unless a supported, declared and compliant design justifies it.
4. Add server-authenticated, short-lived credentials before using a realtime voice API.
5. Do not market Tour mode as a live local expert until source-backed place research is wired in and its claims are tested.
6. Run Gradle unit tests and the Android GitHub Actions workflow on this branch before merging.
