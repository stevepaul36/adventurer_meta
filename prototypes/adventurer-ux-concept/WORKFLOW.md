# Adventurer — Original UI/UX Direction & Workflow

**Status:** Design reference and interactive concept only. This does not replace the native Android app or enable any backend integrations.

## Design intent

Adventurer is a multi-activity adventure companion. The main product loop is **Discover → Plan → Prepare → Go & record → Reflect → Share by choice**. Social features should support the adventure rather than dictate the navigation or visual identity. The concept uses a dark field-notes palette, warm orange action colour, quiet mint state colour, terrain-inspired line patterns and original vector landscape illustrations. These are broad visual cues for outdoor utility, not a reproduction of any social network interface.

## Screens and purpose

1. **Basecamp / Home** — concise launchpad: create an adventure, enter field mode, or shape a trip memory. A resume card exposes saved-plan progress and incomplete prep steps.
2. **Trip Builder** — guided selection of activity, starting area, time available and preferred pace; carries selected activity into discovery.
3. **Field Atlas / Explore** — search, activity filters, map candidates and route cards. Map lines are illustrative and labelled until a trusted provider returns actual routing data.
4. **Route Brief** — route chapters/stops, estimates, prep checklist and visible unknowns. Users can save the plan or enter field mode.
5. **Field Mode** — Ready, Active, Paused and Finished states, glanceable controls and clear disclosure of what data is real. No implied SOS or sharing integrations when disconnected.
6. **Story Studio** — choose route journal, short film, photo essay or trip guide; set tone, edit title/caption, choose audience, mask route start/end and delay location.
7. **Final Review** — preview current edits and summarise audience, precise-route treatment and media permissions. Final action stays disabled until three explicit confirmations.
8. **Trail Settings** — multi-activity preferences, voice behaviour, quiet hours, feed mood and plain-language privacy controls.

## Primary workflows

### A. Discover an adventure
`Basecamp → Trip Builder → Field Atlas → Search / Filter → Route card → Route Brief → Save trip or Use plan → Field Mode`

### B. Record and make a story
`Basecamp → Field Mode → Start → Pause / Resume → Finish → Story Studio → Edit → Final Review → Confirm demo`

### C. Share deliberately
`Story Studio → format/tone → title/caption → audience → route masking → delayed location → Final Review → three required confirmations → save/publish`

### D. Find people and coordinate
`Community → Clubs / Messages / Challenges → inspect context → join or open a thread → send a demo message / join a sample challenge → return to the relevant trip or club`

### E. Use the voice companion
`Tap Ask Adventurer → Talk Now or type → review recognised/request text → run a supported demo command → receive visible response → optional spoken answer → close companion`

Supported sample commands cover demo navigation to Discover, Clubs, Messages, Challenges or Story Studio, plus start, pause, resume, finish and mark moment for the sample Field Mode. Typed/recognised text is interpreted locally in the UI concept; it is not sent to an AI backend.

## Design tokens

- **Surface:** near-black navy `#0A1119`, slate panels `#111D28` / `#162532`, border `#263844`.
- **Action:** warm orange `#FF8B43`; soft mint `#A8DFC4` for completion/privacy-protective state.
- **Typography:** readable sans-serif with strong headline hierarchy; uppercase micro-labels only for secondary metadata.
- **Shapes:** compact functional cards, restrained outlines, 44 dp+ native touch targets.
- **Imagery:** original/appropriately licensed adventure photography and original terrain diagrams. Do not extract competitor screenshots, icons, branding or branded layouts.
- **Navigation:** Basecamp, Discover, Community, Go/Field, Me. Story Studio is contextual after a trip or from Basecamp, not a sixth persistent tab. Voice Companion is an app-wide action, not a tab.

## Safety, privacy and accessibility

- Do not fabricate speed, altitude, GPS, road conditions, dispatch, contact alerts, routing, booking or public publishing.
- Label source, freshness, access unknowns and estimated values; a completed checklist is not a safety guarantee.
- Keep complex edits outside active motion. Do not make destructive actions depend on a swipe-only gesture.
- Support TalkBack labels/content descriptions, focus order, scalable text and sufficient contrast in native Android.
- Let users control coordinate exposure, route-history retention, audience, microphone activation and media upload consent.
- Production sharing requires authenticated backend enforcement, clear data retention and explicit success/error outcomes.

## Prototype boundary

The HTML is a standalone browser concept with illustrative sample data. It does not access GPS, call route APIs, upload media, message a contact, publish content, or activate SOS. It is not a Kotlin/Jetpack Compose implementation.

## Suggested usability test

Test with 3–5 people, without coaching: find and save a route; spot sample/unverified data; start, pause and finish a demo session; make a playful story; mask route start/end and delay location; complete the review gate; open a club, join/leave it; open a conversation and send a demo message; join a challenge; use a typed voice command; grant or deny browser microphone access and recover via typing. Record task success, hesitation, confusing labels, privacy controls missed and mistaken assumptions that a demo function is connected.

## Implementation order

1. Compose theme/tokens, typography, controls, navigation, state models and accessibility.
2. Connect Basecamp and planning views to existing Explore & Plan / Room data.
3. Implement Field Mode as a real state machine against the recorder, with sensor validity and lifecycle tests.
4. Connect selected trip segments to Story Studio; preserve edits and upload consent/local export.
5. Implement typed share draft, preview, confirmation gate, coordinate masking, audience policy and backend enforcement.
6. Run usability tests, accessibility checks, physical-device field validation and release gates.


## Expanded feature structure (October 2026)

The detailed inventory and phased scope now live in [ADVENTURER_FEATURE_CATALOG.md](../../ADVENTURER_FEATURE_CATALOG.md). The experience groups discovery/planning, field operation, community/coordination, achievements, story creation, profile/privacy and a persistent voice companion. This is deliberate: feature depth belongs behind context-specific destinations and secondary tabs, not as an ever-growing Basecamp dashboard.

**Voice UX safety:** Talk Now is explicit and one-shot in this browser prototype. Text input is always available, spoken responses can be muted/stopped, and the current command resolver supports only a finite set of demo intents. No passive recording, connected AI model, real GPS, live maps, actual message delivery or emergency dispatch is provided by this HTML concept.
