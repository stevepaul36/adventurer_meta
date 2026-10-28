# Adventurer — Feature Catalogue and Product Information Architecture

**Purpose:** Consolidated scope for the existing Adventurer repository. Expand the current product in place; do not replace it with a new starter app.

**Status labels**
- **Repository foundation:** the README/implementation docs describe a capability or partial implementation. Verify build, permissions, server configuration and tests before calling it production-ready.
- **UX prototype:** now demonstrated as local HTML interactions using sample data.
- **Integration required:** needs a real provider, persistent backend, permission, lifecycle handling or secure policy enforcement.
- **Roadmap:** specialist functionality that should not be marketed as shipped until its acceptance tests pass.

## 1. Product promise

**Discover → Plan → Prepare → Go → Connect → Reflect → Share by choice.**

Adventurer is a multi-activity adventure companion for motorcycle touring, cycling, road trips, overlanding, walking, hiking, trekking, camping, fishing, lawful hunting/nature observation, kayaking/boating, snorkelling/scuba, island trips and travel creators. One trip model should connect places, route segments, people, safety plans, field records and stories.

## 2. Navigation and screen map

| Destination | Main purpose | Features grouped inside |
|---|---|---|
| **Basecamp** | Resume and launch | Current/upcoming trip, recent field log, preparation reminders, saved draft, create trip, quick start, continue story |
| **Discover / Field Atlas** | Find places and route candidates | Global search, map, filters, curated and user-saved places, activity types, source/freshness labels, candidate route details |
| **Community** | People and coordination | Internal views: Clubs, Messages, Challenges; events and group trip links appear in context |
| **Go / Field Mode** | Operate the current adventure | Ready/Active/Paused/Finished state, supported metrics, waypoints, mark moment, local notes, expected return and check-in status |
| **Me** | Identity, progress and control | Profile, preferences, personal milestones, achievements, saved collections, privacy, permissions, notifications, data export/delete |
| **Story Studio (contextual workspace)** | Turn a trip into an artifact | Route journal, recap video, photo essay, trip guide, captions, narration, edit/review/export |
| **Voice Companion (global action)** | Help the user move between functions | Push-to-talk, typed request, spoken response, transcript, supported app actions, stop speaking, visible microphone state |

**Anti-clutter rules**
1. Keep the five primary destinations stable; don't add a permanent tab for each feature.
2. Keep Clubs, Messages and Challenges inside Community, with in-page tabs and links back to the relevant trip or event.
3. Show achievements in Me and surface a relevant milestone after a meaningful event, rather than making rewards a competing home feed.
4. Keep Story Studio contextual from the trip/finished session, while retaining a Basecamp shortcut.
5. Voice Companion remains available from every main destination. It is never a reason to remove visible touch controls.
6. Present only the next relevant choice by default; expose advanced route/safety/privacy settings progressively.
7. When a feature is not connected or is based on sample data, label it at the point of action.

### All-features directory: 14 destinations, five task groups

The in-app directory is the authoritative index for every unique destination. Five destinations remain in the bottom navigation; other destinations are contextual tools launched from Basecamp, the directory, a trip or a finished activity.

| Group | Destinations | Count |
|---|---|---:|
| **Plan & discover** | Basecamp, Field Atlas, Journey Command, India Expeditions, Route Brief & Checklist | 5 |
| **Field & safety** | Go / Field Mode, Voice Companion, Safety & Roadside Help | 3 |
| **Create & share** | Story Studio, Final Review | 2 |
| **Community & progress** | Community Hub, Achievements & Milestones | 2 |
| **Account & upgrade** | Trail Settings & Profile, Adventurer Pro | 2 |
| **Total** | Every unique navigation destination, listed once | **14** |

**Directory behaviour**
- Search matches feature titles, purpose text and task-group names.
- Group filters reduce the list without hiding the “All tools” option.
- Items are clearly marked **TAB** when they are one of the five permanent navigation destinations, and **TOOL** for contextual destinations.
- Basecamp shows six frequently useful secondary shortcuts; **All 14 tools** opens the complete directory rather than navigating to an unrelated screen.
- The directory describes destinations and entry points. It does not imply that provider-backed, social, safety or premium services are enabled merely because a screen exists.

## 3. Feature inventory

### A. Voice Companion and conversational UX

- Explicit Talk Now / push-to-talk; one request per microphone activation.
- Typed request fallback when speech recognition is unavailable or denied.
- Transcript for user-recognised text and assistant response.
- Spoken answer using system/browser text-to-speech; mute and stop speaking.
- Context-aware command routing for current trip, route, checklist, place, story, club and message.
- Quick actions: open Discover, open Clubs, open Messages, open Challenges, open Story Studio, start/pause/resume/finish sample session and mark a moment.
- Optional hands-free mode in the native app only after explicit opt-in, foreground indicator, clear off switch and device/background constraints review.
- Confirmation for consequential operations: send a message, invite/join a private group, share a coordinate, publish a story, overwrite/delete data or initiate an external transaction.
- Permission denial, recognition timeout, noisy environment, interruption, no-network, unsupported language and fallback states.
- Voice actions must return an observable result. An accepted command is not evidence that an external action succeeded.
- Voice safety: no hidden recording, no always-on claim, no open-ended cross-app automation by implication; minimize voice interaction while driving/riding.

**Current HTML concept:** one-shot browser voice input where supported, typed commands, local finite command resolver and optional speech synthesis. This is not the app's live AI backend. Existing repository voice/backend setup is documented in the root README and voice implementation guides.

### B. Discovery and destinations

- Search for regions, trailheads, viewpoints, parks, campsites, coastlines/islands, beaches, launch/exit points, food and service POIs.
- Activity, available-time, difficulty, surface, gradient, budget, season, accessibility and group-fit filters.
- Map-first browse with candidate routes, collections and saved places.
- Source, last-updated timestamp, verification status, access restrictions and report freshness.
- User place notes, photos, collections, route-linked bookmarks and place discovery records.
- Nearby results only where location permission and a real provider are available; manual location remains available.
- Sensitive places and fishing/wildlife coordinates private by default; public location may be approximate.

### C. Trip building and preparation

- Activity-aware, multi-stop, multi-day trip with segments, day boundaries and reorderable stops.
- Alternatives/Plan B, bailout exits, transport/ferry transfers, lodging/camping, food and service stops.
- Budget assumptions (fuel/charging, transport, food, lodging, permits, rental/gear) with explicit currency.
- Prep checklists, group packing, roles, required equipment and optional notes.
- Expected return, trusted contact, check-in plan, location-sharing duration and last known sync status.
- Provider-backed route distances, road/trail conditions, weather/tides, access/permits and booking status; label source and freshness.
- Local save, import/export and offline pack download only after actual local caching, licensing and boundary display are implemented.
- A plan should never infer that a route is open, legal, safe or booked solely because an LLM suggested it.

### D. Field Mode and records

- Explicit Ready → Active → Paused → Resumed → Finished state machine.
- Start/stop/pause/resume control, elapsed time, distance and activity-supported telemetry with source/accuracy status.
- Waypoints, mark moment, voice note, media capture/attachment, actual timestamp and segment reference.
- GPS availability, lost signal, background/lifecycle recovery and local persistence.
- Native reminders/check-ins, trusted-contact and duration-limited location sharing with observable delivery status.
- Export track/route records in supported formats where implemented.
- Ride metrics (speed, altitude, lean estimate) only from available sensors with truthful calibration/source labels.
- Never use phone GPS as a scuba depth/dive computer or claim connected SOS/rescue without a tested provider.

### E. Friends, messaging and coordination

- Friend/follow requests and audience settings.
- One-to-one messages; group conversations for clubs, trips and events.
- Share a route brief, itinerary, place, event, milestone or story card in chat.
- Draft/attachment states; supported photo/video/file limits, local caching and retry behaviour.
- Message timestamps, send/delivery/read status only if that state is backed by the service.
- Mute, block, report, delete/retention controls, safety notices and account protections.
- Push notification preferences and deep links to a relevant conversation or event.
- Group trip participants, host/leader roles, invitation acceptance, RSVP, rally point, roll-call and shared check-in.
- Production needs authenticated APIs, access rules, rate limiting, anti-spam, encryption in transit, abuse reporting and data deletion.

### F. Clubs and community

- Discover clubs by activity, region, skill/experience, pace and privacy.
- Club creation: name, description, category, rules, public/private visibility, leader/moderator roles and membership approvals.
- Join/leave flows with confirmed backend membership; club chat, pinned info, shared packing lists and route collections.
- Club events: time, place, capacity, route/difficulty, organizer, RSVP/waitlist, gear list, cancellation and update notifications.
- Group trip plans, roles, meetup points, check-ins, attendance and trip recap.
- Moderation queue, report/block, rule acceptance, member removal and appeals where appropriate.
- Club health should not rely exclusively on follower counts; prioritize useful events, safe participation and quality trip information.

### G. Achievements, milestones and fun

- Personal milestones: first activity record, first multi-day trip, new region, new activity, places explored, completed collection, first shared club outing, story/export milestone.
- Place discovery stamps, activity badges, journey timeline, personal stats and optional streaks.
- Club milestones and shared challenges such as discover five new places, complete a camp-preparation checklist or take part in a stewardship/cleanup event.
- Challenge description, duration, eligibility, privacy, progress, credit rules, anti-duplication and withdrawal.
- Distinguish user-confirmed achievements from GPS/provider-verified ones.
- Respect accessibility, rest, weather, fatigue and different activity types; never reward speed or unsafe risk-taking.
- Notification pacing, privacy controls, no forced public leaderboard and opt-out of streak pressure.

### H. Story Studio and creator workflow

- Route journal, trip guide, photo essay, short film and activity-specific recap templates.
- User-selected video/photo/audio; editable highlight suggestions and timestamps.
- Captions, subtitles, narration language/voice style, chapters, map overlay and export aspect ratio.
- Local MP4 editing/export or EDL/JSON output where available; maintain editable source draft.
- Audience and collaboration, asset upload consent, retention, route masking and location delay.
- Face/plate/privacy redaction only when implemented and disclosed; model output remains editable and grounded in supplied media.
- Story review gate summarises audience, route exposure and media rights before publish/export.
- A locally saved preview is not public publishing; publish state must be confirmed from the actual service.

### I. Profile, accessibility and privacy

- Multiple preferred activities, units, locale/language, experience/comfort, accessibility preferences and equipment/vehicle context.
- Saved trips, collections, clubs, friends, achievements and media/story drafts.
- Location and microphone permissions at the point of need; clear indicators while active.
- Route-history visibility, hide start/end, delayed location, live-share expiry, contact access and quiet hours.
- Notification categories, spoken-answer preference, narration level and voice mode.
- Data export, account deletion, local-only options and explicit cloud upload controls.
- Scalable text, screen-reader labels, focus order, adequate contrast, larger touch targets, reduced motion and keyboard operation.

### J. Safety, trust and resilience

- Route/condition/permit information includes provider/source, observed/update time and limitations.
- Local data survives connectivity loss; sync retries and conflicts are explicit. Offline maps only after map tiles/data are actually stored.
- Pre-trip gear/readiness, return time, bailout plan and trusted contact.
- Emergency-call/SMS deep links report handoff/delivery accurately; no fake dispatch or untested satellite claims.
- Community moderation for harassment, dangerous advice, false hazard reports and location leakage.
- Clear demo/sample/unverified labels and no fabricated real-time metrics.
- Do not interrupt active riding with non-essential voice/social prompts.

### K. Activity-specific extensions

| Activity family | Specialized capabilities and constraints |
|---|---|
| Motorcycle touring | Surface/pass notes, fuel-range assumptions, service stops and actual sensor source; no safety claims from estimated lean angle |
| Cycling / bikepacking | Road/gravel/MTB/e-bike profiles, surface, elevation, resupply, repairs, water and optional BLE sensors |
| Car trips / overlanding | Scenic itinerary, fuel/EV charging, parking/tolls/ferries, clearance caveats, water/fuel assumptions and recovery checklist |
| Hiking / trekking | Trailhead/exit, grade/elevation, water/shelter, turnaround/check-in and accessibility cues |
| Camping / backpacking | Legal camping zone/source, permits/fees, amenities, fire restrictions, shared kit/food/water/power checklist |
| Fishing / lawful hunting / wildlife | Official regulations and licence links, season/land-access checks, private coordinates and sensitive-sighting protection |
| Paddling / boating / snorkelling / scuba | Launch/exit, wind/current/tide sources, float plan and dive-computer import only from supported compatible devices; phone GPS is not an underwater safety device |
| Island / general travel | Ferry/transfer windows, last-return planning, time-aware alternatives, food/culture/landmarks and region-specific provider adapters |
| Creator/travel vlogging | Activity-linked media, user-confirmed moments, editable highlight timeline, narration/subtitles, private drafts and location redaction |

### L. Integrations and business model

- Providers for maps/routing, places, weather/conditions, parks/permits, transport, stays and reservations only with valid APIs/links and correct attribution.
- Firebase/Auth/backend, media storage/retention, server-side AI keys, quotas, observability and cost controls.
- Optional hardware integrations only after vendor/API contracts and device validation.
- Free core trip/session/check-in features; optional paid planning, creator quota, backup and licensed offline maps.
- Transparent affiliate links and sponsored placements; monetization cannot outrank safety or accessibility; don't charge for basic emergency-call entry points.

## 4. Key interaction journeys

1. **Plan and go:** Basecamp → Trip Builder → Discover → Route Brief → Prep → Field Mode → Mark moments → Finish → Story Studio.
2. **Find people:** Discover event or Community → inspect club rules/visibility → join or request access → open club/event → RSVP → group trip plan → chat/check-in.
3. **Message about a trip:** Community → Messages → choose or start a thread → share route/place/event card → send → view real delivery result when backed by service.
4. **Voice request:** Tap Voice Companion → Talk Now or type → inspect transcript → resolve intent → show result/action state → optional spoken answer → stop/close. Consequential actions ask confirmation.
5. **Make an achievement:** complete an activity or log a place → validate eligibility → update progress → show earned/pending state → offer private share to a club/story, never auto-post.
6. **Share a story:** selected trip/media → editable draft → audience → precise-route treatment → media-rights review → explicit confirm → verify backend publish/export result.

## 5. Prioritised implementation sequence

### P0 — Interaction and truthfulness foundation
- Stabilize five-destination IA and reusable tokens/components.
- Define typed entities for profile, trip, segment, place, session, club, conversation, message, milestone and story package.
- Normalize navigation, loading/empty/error states, accessibility and telemetry provenance.
- Clearly label actual, estimated, sample and unavailable state; keep safety and privacy controls easy to reach.

### P1 — Functional single-device cross-flow
- Trip builder and saved local trip/stop/checklist.
- Correct Field Mode state machine, local records and moment markers.
- Story Studio edits connected to completed session.
- Global voice companion with explicit one-shot listen, typed fallback, TTS/mute, local command routing and predictable interruption.

### P2 — Community foundations
- Authentication/account linking, profile and consent preferences.
- Persistent clubs, join requests, memberships, roles and events.
- Direct/group messaging, notification delivery, reporting/blocking and retention.
- Backend-enforced access controls and abuse prevention before public launch.

### P3 — Progress/rewards and group experiences
- Place discovery records, achievements and privacy-aware personal progress.
- Club challenges with explicit rules, deduplication and eligibility.
- Group itinerary, RSVP, roll-call and shared checklist; validate with small closed beta.

### P4 — Trusted integrations and offline
- One selected map/place provider pilot with attribution, coverage tests and provider freshness.
- Offline storage/sync, GPX/import-export and field lifecycle recovery.
- Official conditions, closures and permit links in one region before expanding.
- Localised region/provider coverage for India and Canada, followed by other regions.

### P5 — Specialist modes and monetization
- Cycling sensors, vehicle integrations, nature/regulated activity sources, waterway/marine feeds and dive computer integrations as separate supported extensions.
- Paid creator tools, licensed offline maps, cloud quota and verified booking/affiliate providers only after legal/security/business review.

## 6. Definition of done

A feature is not production-ready because a button or mode name exists. For each feature:
- User can reach it through an obvious, keyboard/screen-reader accessible path.
- Primary, empty, permission-denied, offline, timeout, failure and retry states are designed.
- State persists where promised and is scoped to the correct owner/member.
- Consequential operations have explicit confirmation and verifiable completion/failure.
- Logs/analytics avoid precise coordinates, message bodies, contact details and unnecessary media metadata.
- Unit/integration tests cover critical state and permission transitions; device QA validates the actual sensors and lifecycle.
- Privacy and app-store disclosures accurately match the shipped code.
- Demo content is removed or clearly labelled before production release.
