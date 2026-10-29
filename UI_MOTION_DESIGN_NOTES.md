# Adventurer — Frontend & Motion Design Notes

## Direction
An outdoors-first adventure companion: confident, cinematic, highly legible in daylight, with warm trail-orange as the action accent and deep field-navy surfaces for night riding. Existing feed/media viewing remains consistent across markets. Location-sensitive actions, safety language, and provider workflows should adapt to the selected country or region.

## Motion principles
- Motion should confirm hierarchy and state—not delay reaching a destination.
- Top-level navigation: short fade cross-transition (220 ms in / 140 ms out).
- Detail destinations: directional slide and fade, with modest travel distance to avoid a heavy page-swipe feel.
- Feed hero: one-time entrance fade and small upward movement, staggered by 60 ms.
- Controls: use spring-based pressed/selected feedback; avoid perpetual animations in high-attention riding surfaces.
- Respect reduced-motion settings where feasible; emergency actions should never depend on animation finishing.

## Interaction priorities
1. One obvious primary action per screen.
2. Strong contrast and large hit areas for outdoor and daylight usage.
3. Preserve immediate access to SOS and active ride state.
4. Keep bottom navigation stable; hide it during active HUD recording as currently intended.
5. Keep media viewing immersive and consistent across regions.
6. Localize policy-sensitive actions and third-party providers through market settings rather than duplicating the visual experience per country.

## First implementation pass
- Refined Jetpack Compose `AnimatedContent` timing and directional offsets in `MainActivity.kt`.
- Added a subtle entrance animation to the feed's expedition-short hero in `FeedScreen.kt`.
- Existing Material 3 color tokens and screen architecture remain unchanged.

## Manual QA checklist
- Navigate repeatedly between the five primary tabs; verify no flashing or layout jump.
- Open a detail screen and navigate back; verify directional animation feels predictable.
- Verify active ride banner and HUD bottom-bar safeguards remain intact.
- Test light and dark themes, English and other supported languages, and narrow devices with larger font scale.
- Test on lower-end Android hardware; motion should remain brief and must not block taps.
- Confirm SOS can be opened immediately and is not hidden behind transitional overlays.
