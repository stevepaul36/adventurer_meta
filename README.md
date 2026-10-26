# Adventurer — Motorcycle Adventure Social, Telemetry & Expeditions

**Motorcycle-first. Offline-capable. Built for the high passes.**

Adventurer is a production-grade native Android application for motorcycle adventure enthusiasts, dual-sport riders, and high-pass explorers. It combines live ride telemetry (including real lean-angle estimation), curated Indian high-pass expeditions with Plan A/B contingency routing, roadside assistance radar, AI-powered ride story generation, social feed, clubs, and gamification.

Built with modern Kotlin and Jetpack Compose.

> This branch (`improvise/motorcycle-first`) contains focused improvisations based on deep product research: sharper personas, stronger sensor foundation, clearer offline-first architecture, and a prioritized roadmap.

## Core Philosophy

- **Local-first**: Tracks, maps, and expeditions work offline. Sync when online.
- **Motorcycle-native**: Lean angle, high-altitude oxygen warnings, cold-engine protocols, puncture/mechanic radar, crew roles.
- **India high-pass focused**: Ladakh, Spiti, Western Ghats, Rann of Kutch with real contingency logic.
- **Safety-critical**: One-tap SOS, AMS alerts, geo-fences, medical profile support.
- **Content-friendly**: AI Story Studio generates multilingual narrations and EDLs from telemetry + photos.

## Key Features (Current + Improvised Direction)

### Live Ride Telemetry HUD
- High-contrast outdoor heads-up display.
- Analog + digital speedometer, top/average speed.
- **Real Motorcycle Corner Lean Angle Gauge** (accelerometer + gyroscope fusion foundation).
- Altitude, climb, ride timer and distance; ready / recording / paused / demo states.
- Large glove-friendly Pause · Waypoint · Finish bar; Finish asks to save, discard or keep riding.
- Screen stays on while recording; tab bar hides mid-ride; "ride in progress" banner elsewhere.
- One-tap waypoints (Photo, Chai, Fuel, Pass).
- Hold-to-Start with progressive haptics (and a TalkBack long-press action).

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
- SOS sheet: call 112 / 108 through the dialer and share your real location via WhatsApp/SMS. The app never claims to alert anyone on its own.
- "Find help near you": mechanics, puncture repair, fuel, hospitals and dhabas via your maps app.
- Example station listings are sample data (clearly labelled) until a live places source is wired in.
- Trans-Himalayan protocols for AMS, glacial river crossings (mallams), sub-zero cold starts.

### RideStory AI Studio (Meta Global AI Developer Hackathon)
- **Record** a ride with the phone camera or **pick** a long video from the gallery.
- **On-device moment scoring**: samples up to ~320 frames and scores colour, motion, sharpness, open sky, greenery and golden-hour light. It detects scene cuts, skips dark/idle/shaky footage and picks the best clips for a 30 s – 3 min reel. The video never leaves the phone.
- **Enhance with Meta AI**: sends only small keyframes to Muse Spark (vision) to name, re-rank and privacy-check moments (faces / licence plates).
- **Real MP4 export** with Media3 Transformer: H.264/AAC, 16:9 or 9:16 crop, title and lower-third labels; save to *Movies/Adventurer* or share.
- **Travel story**: caption, hashtags, diary, chapters and voice-over in English, Hindi, Marathi, Kannada or Tamil, read aloud with on-device TTS; post to the feed with the reel's cover frame. Works offline with a template built only from detected moments.

#### Setting up Meta AI
1. Copy `.env.example` to `.env` (never commit `.env`) and set `MODEL_API_KEY`.
2. Defaults: `MODEL_API_BASE_URL=https://api.meta.ai/v1`, `MODEL_TEXT` / `MODEL_VISION=muse-spark-1.3` (check the hackathon docs for current IDs). Any OpenAI-compatible host serving Meta open-weights models also works.
3. Without a key the Studio still scores, renders and writes template stories on-device.

The key is compiled into the APK for the hackathon demo; move `MetaAiClient` calls behind a server before a public release.

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
- **Location**: Google Play Services FusedLocationProvider + planned SensorManager lean-angle fusion
- **Images**: Coil Compose
- **Typography**: Outfit (Google Font)
- **AI**: Designed for Gemini (server-side + future on-device)

## Architecture Highlights (Improvised)

```
Mobile Client (Compose)
├── UI Layer (Screens + Components)
├── ViewModel (AdventurerViewModel)
├── Repository (AdventurerRepository — currently mock, ready for Room + remote)
├── LocationManager + future SensorFusion (lean angle, barometer)
└── Local storage (tracks, offline expeditions)

When online → Backend sync (feed, clubs, stories)
When offline → Full ride recording + map cache + mesh-ready crew chat
```

Key principles:
- Sensor data (GNSS, accel, gyro, barometer) flows into LiveRideTelemetry.
- Rides produce GPX-ready tracks + stats for feed and AI Story Studio.
- SOS and medical data are privacy-sensitive and opt-in.

## Roadmap (Prioritized)

See [ROADMAP.md](ROADMAP.md) for the full improvised plan.

**Near-term focus**:
1. Real lean-angle sensor fusion + max lean badges.
2. Stronger offline map / GPX foundation.
3. Production-ready AI Story Studio prompts + Gemini integration.
4. Navigation Compose + better back-stack.
5. Room persistence for rides and expeditions.

## Getting Started

1. Clone the repo.
2. Open in Android Studio.
3. Add your `GEMINI_API_KEY` via the Secrets panel / `.env` (see `.env.example`).
4. Build & run on a device with location + sensors (emulator works for UI).

## License & Attribution

Generated / evolved from AI Studio templates. Map data should respect ODbL (OpenStreetMap) when offline tiles are added. Always leave no trace on the high passes.

---

**Ride safe. Lean deeper. Tell better stories.**
