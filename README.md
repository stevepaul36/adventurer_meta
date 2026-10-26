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
  - AI highlight reel timeline and Edit Decision List (EDL) generator.
  - Multilingual travel narration and caption generation (English, Hindi हिन्दी, Marathi मराठी).
  - 1-click clipboard copy and EDL JSON timeline export.

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
