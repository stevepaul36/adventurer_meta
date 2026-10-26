# Adventurer — Motorcycle Adventure Social, Telemetry & Expeditions

Adventurer is a production-grade native Android application crafted for motorcycle adventure enthusiasts, touring riders, and high-pass explorers. Built with modern Kotlin and Jetpack Compose.

## Key Features

- **Social Feed & Community**:
  - Rider posts with telemetry stats (distance, duration, elevation gain, lean angle).
  - Route sharing with map previews and GPX export.
  - Multi-waypoint expedition stories.
  - Real-time likes and trail comments.
  - Interactive **Hold-to-Start Ride** button with circular animated progress and haptic vibration feedback.

- **Indian High-Pass Expeditions**:
  - Curated routes across the Himalayas, Sahyadris, and Western deserts:
    - Leh-Ladakh Trans-Himalayan Circuit (19,024 ft Umling La summit)
    - Spiti Valley Cold Desert & Monasteries (Kunzum La)
    - Western Ghats Monsoon Rain & Ridge Odyssey (Amboli & Varandha Ghats)
    - Rann of Kutch & Flamingo Road (Salt desert flats)
  - Plan A (Primary) and Plan B (Emergency contingency) routing with crew rosters (Wilderness medic, certified mechanic, lead navigator).
  - High-altitude oxygen warnings (>14,000 ft) and monsoon flood advisories.
  - Integrated **Expedition Budget Calculator** in INR (₹) covering fuel, lodging, meals, wildlife permits, and emergency spare parts.

- **Roadside Assistance Radar & SOS**:
  - Nearby emergency stations: Certified motorcycle mechanics, 24/7 puncture repair hubs, high-altitude fuel stashes, and mountain oxygen clinics.
  - One-tap emergency SOS transmitter broadcasting live GPS coordinates, altitude, and battery levels.
  - Trans-Himalayan safety protocols for Acute Mountain Sickness (AMS), glacial river crossings (mallams), and sub-zero cold engine starts.

- **Live Ride Telemetry HUD**:
  - High-contrast outdoor heads-up display.
  - Analog and digital speedometer with top/average speed tracking.
  - Real-time **Motorcycle Corner Lean Angle Gauge** with visual tilt indicator and maximum lean recording.
  - Barometric altitude meter, elapsed timer, distance odometer, and calorie tracking.
  - One-tap trail waypoint tagging (Photo, Chai stop, Fuel, Scenic pass).
  - Finish ride flow with automatic community feed publishing.

- **Apex Moto 3D — Canyon Racer**:
  - Pure Jetpack Compose Canvas 3D-perspective arcade motorcycle runner.
  - Steer through mountain pass canyon switchbacks, collect turbo boosts, and dodge boulders.
  - Real-time scoring, top speed, and distance recording.
  - Direct run posting to community feed and riding clubs.

- **AI Ride Story Studio**:
  - Selectable ride-footage input and editable highlight timeline.
  - Firebase AI Logic caption generation grounded in the selected clip metadata; when a video is selected, the model also receives the video for multimodal captioning (demo limit: 15 MB).
  - English, Hindi हिन्दी, and Marathi मराठी caption styles.
  - Share generated captions to the local community feed.
  - Export a structured EDL JSON file with selected clip metadata, target length, language, and source-video selection state.
  - Render selected time ranges from a chosen source video into a single local MP4 highlight reel with Android Media3 Transformer. The reel is capped to the selected 15s/30s/60s target.
  - EDL JSON remains available as a separate editing manifest for external editors.
  - Automatic AI narration audio, music licensing, and generated title-card overlays are not included yet.

- **Gamification & 3D Trophy Showcase**:
  - Rider Level and XP progress with daily streak counter.
  - Interactive 3D rotatable Trophy Showcase pedestal (Golden Piston of Umling La, Silver Compass of the Sahyadris, Bronze Carburetor of Rohtang).
  - Multi-discipline achievement badges (Motorcycling, Cycling, Trekking) across Bronze, Silver, Gold, and Platinum tiers.

- **Motorcycle Riding Clubs**:
  - Himalayan Wanderers MC, Sahyadri Apex Predators, and South Adventure Guild.
  - Collective club mileage and ascent goals with progress meters.
  - Real-time club trail radio chat.

- **Rider Profile & Garage**:
  - Alex 'Torque' Rider profile with bike specifications (Royal Enfield Himalayan 450 Kamet White).
  - Lifetime ride statistics and multi-sport tracking mode preferences.
  - Multilingual language selection (English, Hindi, Marathi, Kannada, Tamil).

## Tech Stack

- **Platform**: Android (minSdk 24, targetSdk 36)
- **Language**: Kotlin 2.2.10
- **UI Framework**: Jetpack Compose (Material Design 3)
- **Image Loading**: Coil Compose
- **Architecture**: MVVM with Repository pattern and StateFlow
- **Typography**: Local Google Font (Outfit)
- **Icon**: Custom adaptive launcher icon


## Configure Firebase AI Logic

The repository intentionally does not include a Firebase configuration file or API secrets. To enable live AI caption generation:

1. Create or select a Firebase project and register the Android app using the application ID from `app/build.gradle.kts`.
2. Enable Firebase AI Logic and the Gemini Developer API backend in the Firebase console.
3. Download the project's `google-services.json` and place it in the `app/` directory. Do not commit credentials or configuration from a private production project unless your team has explicitly approved that.
4. Confirm the Google Services Gradle plugin is configured, then build and run the app on a device with network access.
5. Configure App Check for production. Use the debug provider only for local development, never as the production enforcement strategy.

If Firebase is not configured, AI generation will fail and the screen will show an error while preserving the existing caption. The non-AI caption template remains available. Video upload is performed directly from the app to Firebase AI Logic when a video is selected; for this prototype, keep video files under 15 MB and do not select sensitive footage without consent.

## Ride Story Studio workflow

1. Choose an optional video from device storage.
2. Choose the route, target reel length, and narration language.
3. Select the candidate moments to include in the edit decision list.
4. Generate a caption with Gemini. If a video was selected, the model analyzes that video together with the selected metadata.
5. Share the caption to the app's local feed, render the selected moments into an MP4 reel using **Render & Export MP4 Reel**, or save the selected moments as an EDL JSON file.

Video rendering runs locally on the device and uses the selected timeline timestamps; it does not yet use AI to discover exact scene boundaries automatically. Review timestamps before exporting. The reel keeps the source audio and does not add generated narration or licensed music.


## Build verification

A GitHub Actions workflow at `.github/workflows/android-build.yml` assembles the debug APK and runs unit tests on pushes and pull requests. The new timestamp/target-length tests cover the reel export planner. A green workflow is required before treating the feature as build-verified.
