# Adventurer Differentiators & Experiment Plan

## Executive direction

Adventurer should not compete with Instagram, Snapchat, Facebook, X or TikTok by making another infinite feed. Its core promise should be **plan a real adventure, stay prepared when connectivity is poor, and turn the journey into a useful memory**. Social sharing, badges and creator features should reinforce this utility—not replace it.

Mainstream social platforms are strong at audience reach, editing, publishing, recommendations and creator monetization. Adventurer's differentiation is the workflow around real-world trips: activity-specific plans, route and place provenance, offline-ready trip details, safety preparation, multi-day records, domain-aware stories and trusted local knowledge.

The comparison below is strategic at the capability level; competitor product names, policies, monetization and feature availability change over time and should be re-checked before external publication.

## Capability comparison (strategic, not a current feature audit)

| Capability | Mainstream social platforms | Adventurer's intended differentiation |
|---|---|---|
| Short videos / stories | Highly polished editing, effects, feeds and broad distribution | Activity-specific narrative templates, chapters tied to trip segments, route context and editable AI-assisted highlights |
| Discovery | Interest and social-graph recommendations; content-first ranking | Place/activity/time/budget/difficulty discovery, with original-source links and data-freshness cues |
| Travel planning | Posts and groups can inspire, but plans typically live elsewhere | Multi-stop, multi-day plans with activity per stage, gear prep, return times and fallback options |
| Offline use | Social content may cache, but is not a dependable backcountry route package | Locally saved trip plans/checklists first; licensed map/route downloads when an appropriate provider is connected |
| Safety | General privacy, reporting and location-sharing tools vary by service | Explicit trip check-ins, expected return, offline caveats and activity-specific pre-departure checklists |
| Community | Large audiences and sharing/engagement systems | Focused condition reports, route guides, local events, expert reputation, source provenance and moderation |
| Creator economy | Large distribution and mature advertising/subscription/gifting ecosystems | Later-stage paid guides and route packs with verified provenance, transparent fees and safety-policy enforcement |
| Personal history | Photos, memories, bookmarks and activity posts | A persistent trip journal that combines day stages, tracks, notes, stops, gear and generated story chapters |
| Device and sensor data | Platform-specific health/creator integrations | Optional sensors matched to the activity: cycling BLE, vehicle data adapters, and supported dive-computer import |

## 8 experiments to run

Do not use unsupported global benchmarks as acceptance criteria. Establish a baseline with a small cohort, pre-register the hypothesis and primary metric, choose a fixed experiment window, and report confidence intervals alongside sample sizes. Do not claim causality from a simple correlation between feature use and retention.

| # | Experiment | Hypothesis / variant | Primary metric | Guardrails |
|---|---|---|---|---|
| 1 | Activity-specific story templates | Template picker vs. a single generic story flow | Story creation completion rate | Story edit rate, unsupported-fact reports, export failure |
| 2 | Offline-ready trip pack | Trip checklist + export/download preparation vs. no guided preparation | Plans successfully used in low-connectivity trips | Stale-map incidents, storage size, failed navigation |
| 3 | Multi-modal itinerary builder | Multi-day activity segments + saved stops vs. single activity record | Plan-created → activity-started conversion | Route correctness, cancelled plans, planning time |
| 4 | Safety check-in setup | Optional check-in plan presented during trip creation vs. later prompt | Check-in plans completed and on-time manual check-ins | False sense of security, contact consent, notification-delivery failures |
| 5 | Paid guide prototype | Preview + provenance + transparent proposed price vs. free guide preview | Guide detail → qualified purchase intent | Refunds, unsafe-content reports, creator quality; do not take payment in draft phase |
| 6 | Community place reports | Time-stamped reports + moderation + source state vs. unlabelled notes | Helpful report rate and saved-place conversion | Spam, outdated reports, sensitive location leakage |
| 7 | Multi-day trip history | Daily stages and notes vs. separate activity records | Multi-day trip completion and next-session continuation | Sync conflict rate, route privacy, deletion failures |
| 8 | Hardware compatibility | Optional device setup education and later supported pairing vs. no setup prompt | Successfully connected supported devices | Dropouts, battery impact, invalid sensor data |

## Funnel and event definitions

Use the core funnel:

adventure_app_open → trip_plan_created → activity_started → story_exported → story_shared

Suggested definitions:
- **Open:** app launches into the foreground. De-duplicate if measuring daily active users.
- **Plan created:** a valid, named trip draft has been successfully persisted.
- **Activity started:** a session begins; retain activity_type, but not destination or coordinates.
- **Story exported:** a new rendered reel has been successfully completed and saved, not merely when rendering starts.
- **Story shared:** distinguish the in-app feed from the Android share sheet; opening the share sheet is not proof that another platform published it.

Supplementary events: place_saved, safety_check_in_completed, creator_guide_draft_created, community_event_draft_created. Draft creation is not the same as a published guide/event or a completed sale.

Do not attach destination names, full route geometry, precise coordinates, sensitive wildlife/fishing/diving locations, trusted-contact labels, private notes or uploaded-media identifiers to analytics. Firebase Analytics may be complemented by BigQuery for cohort queries after Firebase Analytics is configured. For consent and retention requirements, review the deployment jurisdiction and Play Console disclosures.

## Current code vs. roadmap

Implemented in the current branch as a first vertical slice:
- Room-backed local trip drafts, multi-day stop lists, checklists, saved stop ideas and local creator guide/event drafts.
- External maps/search handoff for destination discovery.
- Local manual check-in timestamp and an exportable JSON plan pack, with clear notices that no contact is notified and map tiles/live conditions are not included.
- Activity-aware Story Studio template selection and offline/AI story prompts.
- Local funnel counters and Firebase Analytics hooks for meaningful actions.

Not yet live and must not be presented as already delivered:
- In-app verified POI feeds, route calculations, weather/tide/closure/permit sources or offline map tiles.
- Scheduled/background safety alerts, outbound emergency contacts or automatic SOS dispatch.
- Shared/verified community content, moderation, RSVP events or paid guides/payments/KYC.
- A/B assignment, statistical experiment dashboards or cohort-level Day-7/Day-30 retention analysis.
- Cycling sensors, OBD-II, satellite messengers or certified dive-computer integrations.

## Rollout and definition of done

1. Validate the local-first experience on real Android devices, including process death/relaunch, Room upgrade path, low storage, GPS denial, no network and export/import.
2. Choose and license the map/places provider before marketing offline maps or verified route data.
3. Create a server-authorized community model and moderation queue before publishing user-submitted reports.
4. Implement consent-aware check-in delivery and test notification permissions, doze/background limits, message failures and expiry.
5. Instrument and validate events in the analytics console before starting experiments.
6. Run each experiment with a primary metric, guardrails and stop conditions; ship only if user value improves without weakening safety or privacy.
