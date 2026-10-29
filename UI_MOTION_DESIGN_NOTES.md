# Adventurer — UI/UX Implementation Blueprint

## Product identity

**Adventurer** is the person creating or sharing an outdoor journey, trip, story, photo, route, or short video. **Followers** are the people who choose to follow Adventurers and see their updates. “Rider” remains appropriate only where the activity is motorcycling; it is not the universal role name.

The interface supports many kinds of outdoor activity: motorcycling, cycling and bikepacking, trekking, walking, camping, scenic road trips, kayaking, scuba diving, fishing, wildlife exploration, and mixed-activity trips. The activity label and relevant equipment/context should change with the Adventurer's chosen activity; motorcycle-only telemetry should not be shown for a hike, dive or campsite.

## Home: follower-first hierarchy

The Home screen is a continuous, vertically scrolling feed, not a dashboard that puts all planning tools before creator content. Its hierarchy is:

1. **Adventurer Stories** — horizontal story bubbles at the very top, including “Your story” and recent moments from followed Adventurers. Tapping a bubble opens that Adventurer's current short or photo.
2. **For you / Following** — an explicit feed switch. “For you” discovers moments; “Following” only shows feed items and shorts by followed profiles.
3. **Adventurer Shorts** — horizontally scrollable vertical-media previews. Selecting a preview opens the immersive viewer with reactions, comments, sharing, progress, and activity-aware context.
4. **Media filters** — All Media, Trail Shorts, Trail Photos, and High Pass change the displayed content instead of acting as decorative chips.
5. **Adventurer updates** — vertically scrolling posts, shared routes, and stories with existing like/comment/media-viewer interactions.
6. **Meet Adventurers** — creator cards show their name, chosen activity and location, with a working Follow/Following action.
7. **Basecamp, trip-planning and field tools** — still available below the social feed, preserving planning, tracking and safety features without obscuring the follower experience.

Feed post likes and comments resolve back to the original repository index, even when the user views the Following or media-filtered subset. The Following policy retains feed ordering and supports posts, route shares, and stories.

## Creator and follower states

- Creator-facing labels use **Adventurer** and activity-specific context.
- Follower-facing language uses **Follow**, **Following**, and “people you follow.”
- Following state is held in the app repository/ViewModel, so it survives navigation while the ViewModel remains alive. Sample follows are seeded for the demo. Server sync and durable account-backed follow storage are not implemented by this UI branch.
- The current-user profile identifies the user's Adventurer profile and explains that shared moments appear to followers.
- The sample discovery shelf includes motorcycle and non-motorcycle creators, so the product identity is not locked to one transport mode.

## Visual system and motion

The existing Material 3 palette remains the base: trail orange for primary actions, strong field-navy surfaces for dark mode, high-contrast text, and warm/cyan/mint secondary accents. Story avatars use a distinctive story ring; creator cards use consistent profile imagery, activity labels, and explicit follow state.

- Top-level navigation: quick fade transitions.
- Detail destinations: a short directional slide and fade.
- Feed hero: one-time fade/slide entrance.
- Selected navigation icon: subtle spring scale.
- Follow controls and filters should update state immediately and should not rely on animation to confirm an action.
- Avoid perpetual motion around safety-critical or live-tracking controls.
- Keep touch targets accessible, preserve content descriptions, and use text labels alongside color to communicate identity and state.

## Market behavior

The media viewer and creator/follower relationship are shared across India, Canada, and the USA. Country-specific safety copy, emergency workflows, local providers, and policy-sensitive actions should depend on the selected market context; do not fork the core viewing layout by country.

## Verification checklist

### Automated
- Feed audience policy: posts, ride shares and stories from followed author IDs only.
- Empty Following state: no items when the follow set is empty.
- Stable ordering: source feed order remains intact.
- Follow/unfollow state transitions and self-follow/blank-ID safeguards.
- Shorts: valid IDs, titles, location, activity, metrics, and wraparound behavior.
- Android CI: validate UX scripts, assemble a debug APK, and run the Gradle task :app:testDebugUnitTest.

### Manual
- Verify stories are the first content section on Home.
- Follow an Adventurer, confirm their story and short appear under Following; unfollow them and confirm the feed changes.
- Test For you, Following, All Media, Trail Shorts, Trail Photos, High Pass, and terrain selection with both matching and non-matching data.
- Confirm like/comment still apply to the intended original item after filtering.
- Open a story, advance/return through eligible shorts, react, comment, share, and close the immersive viewer.
- Verify non-motorcycle short overlays show the activity and location without incorrectly presenting lean angle or speed.
- Verify Light/Dark mode, large font scale, narrow screens, English and supported languages.
- Confirm SOS, active-trip banner, and tracker navigation remain available and are not blocked by feed transitions.

## Scope and honest limitations

This branch adds the follower-first information architecture, local follow/unfollow state, sample Adventurer catalog, activity-diverse short cards, feed audience policy, and tests. The repository still uses sample accounts, sample post data and remote image URLs. This does not turn the demo repository into a deployed backend, add real follower counts, upload media, or integrate a production video stream; those require account, API, storage, and media-provider implementations. Android build/test results must be taken from a successful CI run before the branch is called verified.
