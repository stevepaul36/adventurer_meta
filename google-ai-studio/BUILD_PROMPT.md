# Google AI Studio Build Prompt — Adventurer Voice Co-pilot

Paste this into Google AI Studio → Build, generate the app, and inspect it in Preview.

Build a polished, responsive, full-screen React + TypeScript app called ADVENTURER: a voice-first adventure co-pilot for road trips, motorcycle touring, hiking, camping and travel. It coordinates specialist apps and services instead of recreating all of Google Maps. It helps plan a journey, hand off navigation to Google Maps, track user-entered fuel estimates, discover restaurants, prepare food orders for explicit confirmation, remember trip moments, capture approved photos, and manage stops and reminders.

## Output requirement
Create a working multi-screen app with interactive UI and client-side local mock data. Do not return only a landing page, wireframe or explanation. No API keys are needed for Preview. Do not invent successful live bookings, orders, GPS fixes, navigation, or camera capture. Clearly label simulated actions.

## Design references and visual direction
Use the two reference projects as inspiration for principles, not as code to copy:
- **Tandem:** interruptible voice interaction, short progress updates, visible task states, correction/pause controls, and a clear difference between a handoff and verified completion.
- **Every Joule:** editorial storytelling, tactile visual detail, a sense of discovery, and memorable visual transitions. Borrow that sense of craft for the Memories and trip-story areas; do not copy its physics-lesson content or require heavy 3D rendering.
- Combine them into a distinct Adventurer identity: a practical trip command center during planning, and a beautiful expedition journal when revisiting memories.

- Premium adventure-tech, calm and cinematic; not a generic chatbot or dense admin dashboard.
- Full-screen responsive app. Desktop: spacious three-column workspace. Tablet: two columns. Mobile: one column with bottom navigation.
- Journey and Explore use deep charcoal/navy, warm amber accents, muted teal for connected/verified states, high-contrast off-white typography, generous spacing, restrained borders, rounded cards, subtle topographic contour patterns and map-like route lines. Memories and trip-story views may use a warmer paper/editorial treatment with serif headlines and carefully chosen photographic layouts.
- Modern readable sans-serif. Avoid giant hero gradients, excessive glow, overuse of glassmorphism, emoji-only navigation and tiny grey text.
- Use inline SVG icons or a reliable icon library. Provide fallbacks for remote images.
- Accessible keyboard focus, semantic controls, clear labels, visible state changes and touch-friendly targets.
- Global banner: INTERACTIVE PREVIEW · SAMPLE DATA · NO LIVE BOOKINGS OR ORDERS.

## App shell
Brand: ADVENTURER with a compass/route mark and tagline “Your journey, intelligently guided”.

Navigation:
1. Journey — current trip overview
2. Explore — food, fuel, stays, viewpoints and services
3. Trip plan — route legs, stops, checklists and reminders
4. Memories — trip timeline, photos and milestones
5. Voice — conversation and assistant settings

Show a top status strip with trip state, demo/offline indicator, activity mode and data-conscious label. Do not fake a real GPS fix; say “Demo location” when showing sample context.

## Journey screen (default)
- Headline “The road is yours.” Subtitle “One co-pilot for the whole adventure.”
- Trip card: Chennai → Ladakh, Demo trip, stage Trip planning, button Plan journey. Milestones Chennai, Bengaluru, Hyderabad, Manali and Leh are sample planning stops, not a live route.
- Navigation handoff card: “Let Google Maps handle turn-by-turn directions.” Buttons Open Google Maps and Route options. In demo, show a simulated handoff state; never claim navigation is running.
- Co-pilot briefing with one short sample sentence and Play briefing / Stop buttons. Browser speech synthesis only starts after the user clicks. If unavailable, show a fallback.
- Next smart stop cards for fuel, food and rest/viewpoint. Label all as demo data and show source/freshness.
- Trip readiness checklist: fuel estimate, route selected, food preferences and camera permission; initial state Not set.
- Sticky compact voice dock with mic button, text input, send button, status “Demo assistant · local preview”, and quick suggestions Check fuel, Find food, Plan a stop, Capture a memory. Local deterministic responses only; no claim of real-time AI or continuous background listening.

## Explore screen
- Search field and category chips: All, Fuel, Food, Coffee, Stay, Viewpoints, Repair, Pharmacy.
- Selector: Along my trip / Near demo location. Add sort/filter controls.
- 3–5 sample result cards with category, name, sample distance, price band when relevant, reason, source label and action.
- Label each item “Demo data — not live availability”.
- Food actions View menu and Add to order. Fuel action Plan stop. Other results Add stop.
- List-first layout and a compact stylized route panel, not a huge full map. Explain that live maps come from the chosen provider. Do not confuse straight-line proximity with route distance.

## Food and order flow
Implement the whole local demo state flow:
1. Select a restaurant.
2. Show restaurant detail, demo menu, cuisine, price, dietary labels and sample ETA clearly marked as an estimate.
3. Add/remove menu items and change quantities.
4. Order review with items, quantity, pickup/delivery choice, destination field using a fake demo destination, sample fees and total.
5. Review order opens a distinct confirmation sheet summarizing restaurant, items, quantities, destination and total.
6. Only after clicking Confirm demo order show “Demo order placed” with persistent notice “SIMULATION ONLY — no restaurant was contacted and no payment was made”.
7. Cancel/back is available at every stage.

Never auto-place a real order, request/store real payment details or treat a vague “yes” as transaction approval.

## Fuel planner
- Inputs: tank capacity in litres, expected mileage in km/litre, reserve in litres, safety buffer in km, current fuel estimate, timestamp and optional distance to a known next station (label the distance as demo input).
- Show estimated total range and conservative usable range after reserve. Compare next-stop distance to usable range after safety buffer.
- Validate missing, negative and invalid values and fuel above tank capacity.
- Say “Estimate based on your input—not a fuel sensor or guarantee.” Note that terrain, traffic, load, weather and riding style affect mileage.
- Do not invent station availability, exact depletion time or live station distance.
- Buttons Save estimate and Remind me at next safe stop have visible demo behavior.

## Trip plan screen
- User-managed trip legs and stops; create/edit/reorder/remove locally.
- Reminder cards for refuel, food break, stretch, accommodation check-in and sunset/photo opportunity. Mark done or snooze.
- Checklist for documents, charger, water, first aid and riding gear.
- Distinguish user-planned times from live provider ETAs.

## Memories screen
- Timeline grouped by day with sample stop, note, photo placeholder and milestone.
- Add note, Save place and Add photo actions. Add photo should use a user-triggered file input or show an explicitly simulated capture state; do not claim camera permission is granted.
- Privacy controls: save location with memory toggle, cloud backup off by default, delete memory.
- Create trip story opens a preview from selected timeline items.
- Label all sample memories as examples.

## Voice screen
- Conversation history with timestamps and distinct user/assistant messages.
- Modes Concise, Tour guide and Hands-free (demo).
- Listening never starts automatically. Mic is a simulated toggle unless browser speech recognition is genuinely supported and explicitly enabled.
- Stop speaking cancels browser speech.
- Show: “Actions that spend money, send messages, book or publish always require a final review and confirmation.”
- Action statuses: Suggested, Waiting for confirmation, Handed off, Verified complete, Outcome unknown. Opening another app is not completion.

## Cross-cutting behavior
- All navigation works without reload; every visible primary control has a useful click action.
- Use local state and sample data; persist only benign demo state if needed and include Reset demo.
- Search and filters actually filter sample results.
- Keep one cart state and show cart count.
- On mobile use bottom nav and a sticky voice dock that never covers important actions.
- Avoid constant location tracking, background recording, continuous audio/video streaming and heavy external map tiles.
- Gracefully represent offline, network, permission and provider-integration placeholders.

## Definition of done
- Preview opens on a polished Journey screen.
- All five sections navigate.
- Explore filters work.
- Fuel calculator updates and validates.
- Food cart can be built, reviewed, confirmed as simulated, or cancelled.
- Trip stops/reminders can be edited and completed.
- Memories can be added and deleted.
- Voice dock accepts text and responds with deterministic local demo logic; speech can be played/stopped by click.
- Desktop and mobile layouts are polished.
- No secrets, fake live data or claims of production integrations.
