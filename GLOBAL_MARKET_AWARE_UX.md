# Adventurer — Global Market-Aware UX

## Design decision

Keep one media and creator experience across India, Canada and the United States. Adapt actions to the adventure's actual context, not to a country flag alone.

## Shared everywhere

- Full-screen video/photo viewer, playback, creator identity, captions, likes, comments, sharing and saving. The browser concept opens the same responsive full-screen viewer on mobile/desktop, with Escape/backdrop/close-button exit; its video is illustrative until a real media stream/feed is connected.
- Discovery, profile, saved memories, accessibility patterns and primary navigation.
- Common trip/memory data model and transparent action states.

## Context-aware by market and place

- Provider catalogues, local transport and map handoffs.
- Units, date/number formats, currency display and language preferences.
- Outdoor access, land-manager requirements, park reservations, fishing/hunting rules, permits, seasonal closures and road restrictions.
- Emergency information and roadside support.
- Payment, commerce, marketplace and community features where product availability and applicable requirements differ.

## Context model

Use a layered context object, not a single country boolean:

1. **User preference:** preferred market, language, units and currency. Editable; device locale is a suggestion only.
2. **Trip context:** selected destination and planned activity.
3. **Geographic context:** country plus state/province/territory, municipality and land/park authority where relevant.
4. **Policy result:** activity, jurisdiction, status (allowed / restricted / permit required / unknown), official source URL, checked-at timestamp, expiry and short explanation.
5. **Action gate:** allow, warn, require explicit acknowledgement, require eligibility/permit evidence, or disable when reliable policy data says it is prohibited. Unknown must never be shown as allowed.

Location permission should be requested only when a feature genuinely needs it. Allow manual region selection. Do not infer legal eligibility or permission solely from GPS or market selection.

## Interaction rules

- Changing market updates regional provider/action cards, terminology and defaults. It does not alter the media viewer.
- A trip can override home-market defaults. For travel across borders, context follows the destination/activity being planned.
- Display the relevant authority and last-checked timestamp for safety/legal claims.
- When official data is unavailable or stale, show **“Could not verify — check the official source”** rather than guessing.
- A provider handoff is not a completed booking, permit, dispatch or transaction. Show state honestly.
- For emergencies, surface applicable local emergency instructions and a direct call option where implemented; never pretend a demo button has contacted responders.
- While movement is detected, minimize distraction and queue non-urgent interactions for a safe stop.
- Store exact location history privately by default and obtain clear consent before sharing it.

## Initial regional examples

- **India:** INR, kilometres, Celsius; state and protected-area context; provider handoffs such as Maps/Mappls candidates, Swiggy/Zomato and IRCTC/redBus.
- **Canada:** CAD, kilometres and Celsius defaults; provincial/territorial and park-operator context; official Parks Canada and provincial/territorial sources, trails, transit and EV-charging handoffs.
- **United States:** USD, miles and Fahrenheit defaults with editable units; state/local and federal/state land-manager context; relevant National Park Service, state-park, road and activity authorities.

Provider examples are candidates only, not proof of partnership, coverage or live data. Regulatory copy and source links require legal/product review before release.

## Prototype

Open [index.html](index.html) directly in a modern browser. It is a self-contained responsive interaction concept with a market selector for India/Canada/USA, regional selection, shared media-viewer mock, market-specific action/provider cards and explicit policy-data limitations. Buttons demonstrate UX states only; no map, official policy API, booking, emergency dispatch or real video is connected.

## Acceptance checklist

- [ ] The same media viewer and creator controls remain consistent across all three markets.
- [ ] Market switching changes relevant providers, units and action labels without changing saved content.
- [ ] Region and activity selection affect policy context.
- [ ] The interface never claims live compliance when no authoritative feed is connected.
- [ ] Unknown/stale policy data is visible and does not imply permission.
- [ ] Market selection does not itself request GPS.
- [ ] Mobile and desktop layouts remain usable with keyboard focus states.
- [ ] Every external handoff has a truthful state and never implies verified completion.
