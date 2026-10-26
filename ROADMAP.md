# Adventurer Roadmap — Improvised Motorcycle-First Plan

Based on deep research into outdoor adventure apps (AllTrails, Komoot, Gaia, Strava, OSMAnd) and the specific needs of high-pass motorcycle riders.

## Product Vision

Adventurer is the definitive companion for dual-sport and adventure motorcycle riders exploring India’s high passes and beyond. It prioritizes:

- Real-time lean-angle and telemetry HUD that works offline.
- Contingency-aware expedition planning (Plan A / Plan B).
- Safety (SOS, AMS, roadside radar).
- Effortless content creation (AI Story Studio).
- Community and clubs that feel local to the riding culture.

## Personas (Sharpened)

1. **Solo High-Pass Rider** — Needs reliable offline maps, lean telemetry, oxygen warnings, one-tap SOS.
2. **Expedition Group Leader / Crew Chief** — Crew roster, shared location, contingency routes, trail radio.
3. **Adventure Vlogger / Content Rider** — Auto-geotagged media, multilingual AI narration, EDL export.
4. **Riding Club Admin** — Collective goals, events, chat.
5. **Safety / Support Rider** — Group monitoring, muster alerts.

## Feature Prioritization

### Phase 1 — Foundation (Current Branch Focus)
- [x] Live Telemetry HUD with simulated lean angle
- [x] Hold-to-Start + finish flow → auto post
- [x] Curated Indian expeditions with Plan A/B and budget calculator
- [x] Roadside Radar + SOS dialog
- [x] AI Story Studio (mock)
- [x] Clubs, Gamification, Profile
- [ ] Real accelerometer + gyroscope lean-angle fusion (foundation added in LocationManager / sensors)
- [ ] Proper GPX export of recorded rides
- [ ] Navigation Compose migration

### Phase 2 — Offline & Safety Hardening
- Offline vector/raster map downloads (MapLibre / similar)
- Room persistence for rides, waypoints, expeditions
- Real barometer altitude fusion
- Medical profile + SOS payload
- Satellite messenger pairing stubs (Garmin inReach style)
- Mesh / P2P crew location sharing (Bluetooth / Wi-Fi Direct)

### Phase 3 — AI & Content
- Production Gemini multimodal Story Studio (photos + telemetry + voice → narration + EDL)
- Landmark / pass recognition
- Multilingual captions that actually localize UI strings
- Highlight reel auto-selection based on lean/speed/altitude spikes

### Phase 4 — Social & Scale
- Real backend (feed, clubs, real-time chat)
- Club collective goals that unlock trophies
- Wear OS companion for HUD glance
- Advanced analytics (lean distribution, best corners)

## Technical Debt & Improvements Already Started

- LocationManager now documents the path to sensor fusion for lean angle.
- ViewModel has cleaner GPS attachment lifecycle.
- README rewritten with motorcycle-first philosophy and architecture notes.

## Success Metrics (Future)

- % of rides recorded fully offline
- Average max lean angle logged per user
- Stories generated & shared
- Expedition budgets calculated
- SOS / radar usage (safety value)

## Guiding Principles

1. Never require network to finish a ride or see critical safety info.
2. Lean angle and altitude are first-class citizens, not afterthoughts.
3. AI should reduce friction for content, never invent unsafe advice.
4. Respect privacy: location history and medical data are opt-in and encrypted.
5. Celebrate the culture of Indian high-pass riding.

---

Next concrete implementation targets on this branch:
1. Sensor fusion module for lean angle.
2. GPX writer for finished rides.
3. Improved AI Story Studio prompt structure ready for Gemini.
