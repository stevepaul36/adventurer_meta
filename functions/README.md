# Adventurer Cloud Functions

These functions enforce server-side Pro entitlement and AI quotas. They require Firebase Authentication, enforced App Check, a server-only Gemini API key, and a server-only RevenueCat secret API key.

## Setup

1. Select your Firebase project and enable **Anonymous** sign-in in Firebase Authentication.
2. Enable Firestore and Cloud Storage; the included rules deny direct client reads/writes to subscription and AI usage documents and deny reads of temporary videos.
3. In RevenueCat, create the exact entitlement `adventurer_pro`, map Google Play subscription products to it, and configure a current offering.
4. Set server secrets using Firebase CLI (never add them to Git or the Android app):

   ```bash
   firebase functions:secrets:set GEMINI_API_KEY
   firebase functions:secrets:set REVENUECAT_SECRET_API_KEY
   ```

   Use a Gemini Developer API key and a RevenueCat **secret** API key with subscriber read access. These are not public SDK keys. Optionally change the `GEMINI_MODEL` parameter default in `src/index.ts`. The current default is `gemini-3.8-flash`; confirm model access/availability in your Google AI Studio project before deploying.
5. Register the Android app with the exact application ID from Gradle; supply its correct `google-services.json` locally. Configure App Check with debug only for local development and Play Integrity for release.

## Deploy

From the repository root:

```bash
firebase use <your-project-id>
firebase deploy --only functions,firestore:rules,storage
```

Cloud Functions/Storage require a Firebase project with billing enabled. Review model availability, service region, budgets, logging, retention, and quotas before handling real users' videos.

## Security behavior

- Callable functions require Firebase Auth plus a valid App Check token.
- Every AI call verifies the active `adventurer_pro` entitlement against RevenueCat's subscriber API; UI state is not authoritative.
- AI calls reserve monthly quota transactionally and count successful results only. Allowance: 30 successful generations per UTC month.
- Videos may only use the caller's `ride-videos/<uid>/<filename>` path, are limited to 14 MiB and video content types, and cannot be read by clients.
- Missing secrets, failed RevenueCat verification, and inactive entitlements fail closed.
- The Android client deletes each uploaded video in a `finally` block. Add a scheduled object-lifecycle cleanup for uploads abandoned after force-closes.


A scheduled cleanup job removes temporary ride-video files older than 24 hours so abandoned uploads do not accumulate. Keep immediate client-side deletion in place as the primary cleanup path.
