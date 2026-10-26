# Adventurer — Motorcycle Adventure Social, Telemetry & Expeditions

**Motorcycle-first. Offline-capable. Built for the high passes.**

Adventurer is a production-grade native Android application for motorcycle adventure enthusiasts, dual-sport riders, and high-pass explorers. It combines live ride telemetry (including real lean-angle estimation), curated Indian high-pass expeditions with Plan A/B contingency routing, roadside assistance radar, AI-powered ride story generation, social feed, clubs, and gamification.

Built with modern Kotlin and Jetpack Compose.

## Core Philosophy

- **Local-first**: Tracks, maps, and expeditions work offline. Sync when online.
- **Motorcycle-native**: Lean angle, high-altitude oxygen warnings, cold-engine protocols, puncture/mechanic radar, crew roles.
- **India high-pass focused**: Ladakh, Spiti, Western Ghats, Rann of Kutch with real contingency logic.
- **Safety-critical**: One-tap SOS, AMS alerts, geo-fences, medical profile support.
- **Content-friendly**: AI Story Studio generates multilingual narrations and EDLs from telemetry + photos/video.

## Key Features

### Live Ride Telemetry HUD
- High-contrast outdoor heads-up display.
- Analog + digital speedometer, top/average speed.
- **Real Motorcycle Corner Lean Angle Gauge** (accelerometer + gyroscope fusion foundation).
- Barometric altitude, elapsed timer, distance, calories.
- One-tap waypoints (Photo, Chai, Fuel, Scenic Pass).
- Hold-to-Start with progressive haptics + auto-publish on finish.

### Indian High-Pass Expeditions
- Curated routes:
  - Leh-Ladakh Trans-Himalayan Circuit (Umling La 19,024 ft)
  - Spiti Valley Cold Desert & Monasteries (Kunzum La)
  - Western Ghats Monsoon Rain & Ridge Odyssey
  - Rann of Kutch & Flamingo Road
- Plan A (primary) + Plan B (emergency contingency) with crew roster (Wilderness Medic, Certified Mechanic, Lead Navigator).
- High-altitude oxygen warnings (>14,000 ft) and monsoon flood advisories.
- Expedition Budget Calculator (INR) — fuel, lodging, meals, wildlife permits, emergency spares.

### Roadside Assistance Radar & SOS
- Nearby certified mechanics, puncture hubs, high-altitude fuel stashes, oxygen clinics.
- One-tap SOS broadcasting GPS, altitude, battery, and medical profile (when configured).
- Trans-Himalayan protocols for AMS, glacial river crossings (mallams), sub-zero cold starts.

### AI Ride Story Studio
- Selectable ride-footage input, AI-detected candidate moments with suggested source timestamps, and an editable highlight timeline.
- Firebase AI Logic caption generation grounded in the selected clip metadata; when a video is selected, the model also receives the video for multimodal captioning (demo limit: 14 MB).
- English, Hindi हिन्दी, and Marathi मराठी caption styles.
- Share generated captions to the local community feed.
- Export a structured EDL JSON file with selected clip metadata, target length, language, and source-video selection state.
- Render selected time ranges from a chosen source video into a single local MP4 highlight reel with Android Media3 Transformer (capped to 15s/30s/60s target).
- EDL JSON remains available as a separate editing manifest for external editors.
- Automatic AI narration audio, music licensing, and generated title-card overlays are not included yet.

### Social, Clubs & Gamification
- Telemetry-rich posts, route sharing, GPX export.
- Riding Clubs with collective mileage/ascent goals and trail radio.
- Rider Level, XP, daily streaks, 3D Trophy Showcase (Golden Piston of Umling La, etc.).
- Multi-discipline badges (Motorcycling primary).

### Rider Profile & Garage
- Bike specs (e.g. Royal Enfield Himalayan 450).
- Lifetime stats, multi-sport mode preference.
- Multilingual UI (English, Hindi, Marathi, Kannada, Tamil).

## Tech Stack

- **Platform**: Android (minSdk 24, targetSdk 36)
- **Language**: Kotlin 2.2.10
- **UI**: Jetpack Compose + Material Design 3
- **Architecture**: MVVM + Repository + StateFlow (local-first)
- **Location**: Google Play Services FusedLocationProvider + SensorManager lean-angle fusion
- **Images**: Coil Compose
- **Video / AI**: Firebase AI Logic (Gemini) + Android Media3 Transformer
- **Typography**: Outfit (Google Font)

## Architecture Highlights

```
Mobile Client (Compose)
├── UI Layer (Screens + Components)
├── ViewModel (AdventurerViewModel)
├── Repository (AdventurerRepository — currently mock, ready for Room + remote)
├── LocationManager + SensorFusion (lean angle, barometer)
├── RideStoryAiService + RideStoryVideoExporter
└── Local storage (tracks, offline expeditions)

When online → Backend sync (feed, clubs, stories) + Firebase AI
When offline → Full ride recording + map cache + mesh-ready crew chat
```

## Configure Firebase AI Logic

The repository intentionally does not include a Firebase configuration file or API secrets. To enable live AI caption generation:

1. Create or select a Firebase project and register the Android app using the application ID from `app/build.gradle.kts`.
2. Enable Firebase AI Logic and the Gemini Developer API backend in the Firebase console.
3. Download the project's `google-services.json` and place it in the `app/` directory. Do not commit credentials or configuration from a private production project unless your team has explicitly approved that.
4. Confirm the Google Services Gradle plugin is configured, then build and run the app on a device with network access.
5. Configure App Check for production. Use the debug provider only for local development, never as the production enforcement strategy.

If Firebase is not configured, AI generation will fail and the screen will show an error while preserving the existing caption. The non-AI caption template remains available. Video upload is performed directly from the app to Firebase AI Logic when a video is selected; for this prototype, keep video files under 14 MB and do not select sensitive footage without consent.

## Ride Story Studio workflow

1. Choose an optional video from device storage.
2. Choose the route, target reel length, and narration language.
3. Select the candidate moments to include in the edit decision list.
4. Generate a caption with Gemini. If a video was selected, the model analyzes that video together with the selected metadata.
5. Optionally tap **AI Find Highlights** to ask Gemini to suggest moments and timestamps from the actual video. Review the timeline because AI timestamps can be approximate.
6. Share the caption to the app's local feed, render selected moments into an MP4 reel using **Render & Export MP4 Reel**, or save the selected moments as an EDL JSON file.

Video rendering runs locally on the device and uses the selected timeline timestamps; it does not yet use AI to discover exact scene boundaries automatically. Review timestamps before exporting. The reel keeps the source audio and does not add generated narration or licensed music.

## Roadmap

See [ROADMAP.md](ROADMAP.md) for the full motorcycle-first plan.

**Near-term focus**:
1. Real lean-angle sensor fusion + max lean badges.
2. Stronger offline map / GPX foundation.
3. Production-ready AI Story Studio (already advanced on this branch).
4. Navigation Compose + better back-stack.
5. Room persistence for rides and expeditions.

## Build verification

A GitHub Actions workflow at `.github/workflows/android-build.yml` assembles the debug APK and runs unit tests on pushes and pull requests. The timestamp/target-length tests cover the reel export planner. A green workflow is required before treating the feature as build-verified.

An additional auto-merge workflow (`.github/workflows/auto-merge.yml`) enables squash auto-merge for PRs labeled `automerge`.

## Getting Started

1. Clone the repo.
2. Open in Android Studio.
3. Add Firebase config (`google-services.json`) and/or `GEMINI_API_KEY` as needed.
4. Build & run on a device with location + sensors (emulator works for UI).

## License & Attribution

Generated / evolved from AI Studio templates. Map data should respect ODbL (OpenStreetMap) when offline tiles are added. Always leave no trace on the high passes.

---

**Ride safe. Lean deeper. Tell better stories.**
