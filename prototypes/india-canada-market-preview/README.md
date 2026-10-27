# Adventurer India + Canada Market Preview

A self-contained, responsive product/interaction prototype for the India and Canada market strategy.

## Run it

Open `index.html` directly in a modern browser. There is no package installation, API key, build step or server required.

- Desktop: use the left navigation for Journey, Explore, Trip plan, Memories, and Voice & profile.
- Mobile: use the bottom navigation.
- Use the India/Canada toggle at the top to switch the sample provider catalogue, currency label and route illustration.
- Try Explore filters, preferred-provider settings, the local trip planner/export, the fuel-range calculator, the memory timeline and the food-order simulation.
- Use **Reset demo data** at the bottom of the left navigation to restore the starting state.

## What is real and what is not

- Provider URLs are external sites. Selecting **Open** hands the user to that provider; Adventurer does not know whether a page loaded, an account is signed in, or navigation/order/booking completed.
- Places, distances, menus, prices, fuel inputs, itinerary and memory examples are sample data.
- Food checkout ends in an explicitly simulated confirmation. No restaurant is contacted, no booking is placed, and no money is charged.
- Maps URLs use the documented universal Google Maps search/directions URL patterns. Search results and routing are returned by Google Maps, not this preview. See [Google Maps URLs documentation](https://developers.google.com/maps/documentation/urls/get-started).
- Voice responses use deterministic local demo logic. Browser speech synthesis starts only when the user has enabled it and sends a message. The prototype does not activate microphone, wake-word or camera access.
- Memories and preferences are stored in this browser's localStorage. Optional photo selection is not uploaded or stored by the prototype. Cloud backup is not connected.
- INR/CAD labels are market preferences; this prototype does not convert currencies. Editing the market does not convert the existing sample budget.
- This prototype is not the Kotlin/Jetpack Compose Android application and does not automatically sync state into it.

## Validation

Run the static interaction/syntax checks from the repository root:

```bash
node scripts/validate-market-preview.mjs
```

GitHub Actions runs this check on pull requests and pushes that modify this prototype.
