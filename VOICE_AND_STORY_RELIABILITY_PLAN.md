# Voice, Chat and Story Studio Reliability Plan

## Purpose

This plan turns the voice companion and Story Studio from a feature checklist into a consistent workflow. Product principles are informed by the attached Tandem and Every Joule reference projects: one owner for action state, short visible progress, interrupt/correct controls, accessible text fallbacks, polished sequential storytelling, and honest verification. Their code, branding and content are not copied.

## The interaction contract

All actions should make it clear what Adventurer is doing and what evidence it has:

1. **Ready** — show which input is available (speech, typed command or live AI) and whether the required service is configured.
2. **Listening / Sending / Working** — show visible progress and retain the user's last submitted text.
3. **Result** — present the transcript or result once, not every partial audio delta as a separate message.
4. **Correction / Stop** — let the user retry, edit, cancel speech, stop the live session or correct a proposed action.
5. **Outcome** — distinguish local result, external handoff, simulated result, failure and unknown outcome. Never say a booking, order, SOS dispatch or navigation session succeeded without evidence.

The default interaction is user-initiated. “Proactive” means helpful next-step chips and context-aware suggestions, not an unconsented background microphone, unsolicited speech while moving, or arbitrary cross-app tapping. Any action involving money, messages, booking, publishing, deletion or emergency contact requires fresh explicit confirmation and a trustworthy result check.

## Voice and chat

- **Talk Now** is the primary speech input. Android speech recognition may be provided by the device/system service; it may need a network or installed language pack.
- If a user says only “Hey Adventurer” in one-shot mode, play the short prompt and open a second capture turn, rather than silently stopping.
- When replacing a recognizer session, ignore stale cancellation callbacks briefly so they do not cancel the next intentional turn.
- The deterministic command parser controls only the app's allow-listed actions. Polite forms and common phrasing are supported; ambiguous place references are not guessed.
- **Typed chat is always visible in the Voice screen**. Without Live AI Guide it can run local app commands. When Live AI Guide is connected it sends text to the same conversation. A bounded FIFO keeps quick successive messages in order while the current response finishes.
- Advanced interests, storytelling style and proactivity settings are optional and hidden until requested; the user can use the main workflow before personalizing it.
- **Live AI Guide is separate from Talk Now.** It requires a configured Firebase project, an authenticated/App-Check-approved request, deployed createAdventureRealtimeToken, and the server-only BOSON_API_KEY. The phone should show an actionable failure state when any prerequisite is missing.
- Microphone access starts only after an explicit user action. Live capture ends when the user stops, the app leaves the foreground, or the movement safety guard activates. The app does not claim an OS-level always-on assistant.

## Story Studio

The consistent flow is **Footage → Highlights → Story**.

1. Pick a video or launch the camera; show the source name, readable duration and any unsupported-file error.
2. Run local frame scoring and show progress. This path remains available offline and does not upload the video.
3. Review and edit/select candidate moments. Local choices must remain intact if cloud AI fails.
4. Choose a template, activity, language and reel format. Do not force optional profile fields to generate a first draft.
5. **Write a story** through the authenticated Firebase callable, sending only the chosen moment metadata, title, optional user-provided destination, language and template. The server enforces App Check, Pro entitlement and quota. If unavailable, generate an editable offline template with a clear explanation rather than silently doing nothing.
6. **Cloud highlight review** is a distinct opt-in action. Only after the user taps it, upload the selected video (max 14 MiB) to owner-scoped temporary storage; delete it after the request, with the backend cleanup job as a fallback. Preserve local highlights on error.
7. Render an MP4 locally, show progress/cancel/error, and expose Save/Share only once the export exists. Posting to the in-app feed must not be described as public external publishing.

Private model provider keys do not belong in BuildConfig or the APK. The model client that compiled a direct provider key into the app has been removed; server-side Gemini requests use Firebase Functions secrets.

## Setup friction and fallback behaviour

- Do not gate the basic trip workflow behind a long preference questionnaire. Ask for required permissions only when a feature is invoked, explain the reason, and offer skip/continue-later wherever a setting is optional.
- The app's Firebase Auth path currently uses anonymous per-install authentication; it is not a full account registration flow. Account linking/profile sync is still a separate product task. Avoid wording that suggests users must complete a long signup to try local features.
- A green CI build cannot prove a device microphone, Bluetooth route, TTS voice pack, Play Billing, Firebase App Check or realtime provider credential is usable. Show configuration status and specific recovery advice instead of a generic “not working” state.

## Automated checks

The repository test suite should cover at least:

- Voice parser: wake phrase, wake phrase + command, one-shot command without wake phrase, polite phrasing, destination extraction, unknown and ambiguous inputs.
- Voice controller: permission denied, speech recognition unavailable, no match, cancellation race, one-shot wake follow-up, TTS not ready and foreground resume.
- Realtime chat: response interruption, queue order/capacity, queue clear on disconnect, duplicate/partial transcript handling, server error and timeout states.
- Story Studio: missing source/selected moments, corrupt video, local analysis fallback, cloud unavailable/non-Pro quota, oversized video, failed upload, invalid AI JSON, cancel/export failure and verified local file.
- App workflows: every main primary button navigates or makes a visible state change, no fake completion, and no external transaction without a fresh review.

## Physical-device acceptance checklist

Run this on each representative Android device before distributing a release APK:

1. Fresh install, no Firebase file, no network: Journey and local planner open; text commands work; Talk Now shows a useful error or works when system speech recognition is available; local Story Studio analysis/export remains available.
2. Grant then deny microphone permission: the UI recovers; typed commands remain usable; no looped permission prompts.
3. Talk Now one turn, “Hey Adventurer” then a second command, natural phrasing, TTS interruption, repeat, no-match and a rapid tap/retry.
4. Hands-free: explicitly arm it, confirm stop control works, background the app and verify listening stops/pauses; verify no unintended recording after leaving the foreground.
5. Live AI with configured Firebase/Auth/App Check/server secret: connect, hear the greeting, ask a voice question, send multiple typed turns quickly, interrupt, disconnect/reconnect, then repeat without stale transcript lines or hangs.
6. Live AI without required backend setup: error names the missing prerequisite and Talk Now/local commands still work.
7. Story Studio: open a known MP4, analyze, select/deselect moments, write a local draft with cloud unavailable, request cloud text story with a valid Pro test account, request cloud highlight analysis with a video under/over 14 MiB, cancel and recover from network/server errors.
8. Render a reel, wait for completion, Save, Share, return to the source video and repeat; confirm a newly chosen source never inherits old highlights/captions.
9. Check large font, TalkBack/keyboard focus, reduced animation, rotation, low battery, network loss and the permission/foreground lifecycle.
10. Confirm that live SOS dispatch, real food orders/bookings, current inventory, live traffic/ETA and always-on/background listening are not claimed unless separately implemented and end-to-end verified.

## Current limits

Code/CI checks can prove parser/queue/metadata/backend validation and Android compilation. This workspace does not control a physical phone, install the APK or configure the user's private Firebase/RevenueCat project. Final audio, Bluetooth, account, billing and real-device lifecycle acceptance therefore remains a manual gate, not a claimed test result.
