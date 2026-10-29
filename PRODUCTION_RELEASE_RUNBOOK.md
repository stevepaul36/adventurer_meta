# Adventurer — Production Release Runbook

This runbook prepares a **signed Android App Bundle (AAB)** for controlled testing and eventual Google Play release. A successful CI build is not proof that Firebase services, microphone/audio hardware, payments, privacy controls or safety flows are production-ready.

## Release scope and present constraints

Adventurer has a broad product vision, but the public launch must describe only the features that are implemented and verified. In particular, the current app does not dispatch SOS to emergency responders, provide authoritative safety/navigation data, support every activity-specific hardware workflow, or provide an always-on background assistant.

Before the first public release, treat these as blocking decisions:

- Confirm the final Android application ID. It currently is `com.aistudio.adventurer.kxmpzq`. If you want a different package, change it **before** the first Play upload and register the same ID in Firebase and RevenueCat. The application ID is the identity of the Play app.
- Publish a real privacy policy and support contact; complete Play Console Data safety, content rating, audience/ads/app access declarations, and data-deletion disclosures that match actual behaviour.
- Build and test a user-accessible account/data deletion path or request route appropriate to the shipped account model. The current Android app uses Firebase anonymous authentication and is not a full sign-up/account-linking experience.
- Finish the manual device acceptance checklist in [PRODUCTION_UX_ACCEPTANCE.md](PRODUCTION_UX_ACCEPTANCE.md), covering permissions, location, voice interruption, app lifecycle, billing, offline use, export/share and recovery from backend failure.
- Configure Google Play internal testing, Play App Signing, subscriptions, and provider credentials before advertising paid features as available.

Refer to [PRODUCTION_API_KEYS.md](PRODUCTION_API_KEYS.md) for the credentials matrix, which credentials are optional, and the distinction between public SDK keys, server secrets, Firebase app configuration, and signing material.

## 1. Firebase project and backend

1. Install/use the Firebase CLI and select the intended project from the repository root:

   ```powershell
   firebase login
   firebase projects:list
   firebase use --add
   ```

2. Register an Android app whose package exactly matches `applicationId` in `app/build.gradle.kts`. Download its `google-services.json` into `app/google-services.json` for local development. This file is excluded from Git and must not be committed.

3. In Firebase Console, enable the authentication provider used by this version (anonymous authentication), enable billing as required for Cloud Functions deployment, confirm Firestore and Storage exist, and configure App Check for Play Integrity. Register the Android app and complete App Check's production setup before enforcing it for real users.

4. Add server-only secrets. Run each command and paste the provider credential only at the secure prompt:

   ```powershell
   firebase functions:secrets:set BOSON_API_KEY
   firebase functions:secrets:set GEMINI_API_KEY
   firebase functions:secrets:set REVENUECAT_SECRET_API_KEY
   ```

   `BOSON_API_KEY` mints short-lived Live AI Guide tokens; `GEMINI_API_KEY` powers the server-side story/highlight workflows; `REVENUECAT_SECRET_API_KEY` lets Cloud Functions verify the Pro entitlement via RevenueCat's subscriber API. These are **not** Android SDK keys. Never place them in app source, Gradle properties committed to Git, GitHub files, or an APK.

5. Confirm the model selected by the configured `GEMINI_MODEL` (currently defaulted in `functions/src/index.ts`) is available to your Google AI account and supported by the API. Set the `GEMINI_MODEL` Functions parameter to a supported production model if needed.

6. Deploy the functions and access rules from the root:

   ```powershell
   firebase deploy --only functions,firestore:rules,storage
   ```

7. Review Firebase logs and usage/billing; set a budget alert before testing with multiple users. Test functions only after App Check, Auth, and all required secrets are configured. Changing a secret requires redeploying functions that consume it.

## 2. RevenueCat / paid features

In RevenueCat:

- Configure the matching Android app and Google Play service account/integration.
- Create the store product(s), attach them to a current Offering, and map active products to the exact entitlement `adventurer_pro`.
- Use the RevenueCat **public Android SDK key** in the app build; use the **server secret API key** only as `REVENUECAT_SECRET_API_KEY` in Firebase.
- Test purchase, pending/failed payment, cancellation, restore, expiry/revocation and no-network behaviour through Play internal testing. Do not use a RevenueCat secret API key as the Android SDK key.
- AI story and highlight generation currently require active Pro status and use a monthly server-side allowance of 30 successful generations. Ensure paywall wording and price/renewal details match the actual Play products.

## 3. Create the GitHub release secrets

The workflow [Signed Android release candidate](.github/workflows/android-release.yml) is deliberately manual and only runs from `main`. It validates the package ID against the Firebase config, runs unit tests, and builds a signed AAB. It **does not upload or publish** to Google Play.

In GitHub, open **Settings → Secrets and variables → Actions → New repository secret** and configure:

| Secret | Value |
|---|---|
| `ANDROID_KEYSTORE_BASE64` | Base64 of your Play upload keystore file |
| `ANDROID_KEYSTORE_PASSWORD` | Keystore password |
| `ANDROID_KEY_ALIAS` | Alias inside that keystore (the local default is `upload`) |
| `ANDROID_KEY_PASSWORD` | Key password |
| `GOOGLE_SERVICES_JSON_BASE64` | Base64 of the correct `app/google-services.json` |
| `REVENUECAT_API_KEY` | Public RevenueCat Android SDK key for the production project |

On Windows PowerShell, encode files without putting their contents into the terminal history:

```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("my-upload-key.jks"))
[Convert]::ToBase64String([IO.File]::ReadAllBytes("app/google-services.json"))
```

Copy the output directly into the matching GitHub secret field. Do not store the Base64 strings or keystore in the repository. Keep at least one encrypted offline backup of the upload keystore and its passwords. If you have not created the upload key yet, create it securely and enroll in Play App Signing; do not generate a new key for every release.

Then open **GitHub → Actions → Signed Android release candidate → Run workflow**, choose `main`, and start the workflow. Download the resulting signed `.aab` artifact and verify its SHA-256 checksum. The artifact expires after 7 days. If any required secret is missing or the Firebase package ID differs, the workflow should fail instead of creating a misleading release artifact.

## 4. Google Play release path

1. Create the app in Play Console only after finalizing its application ID and public product name.
2. Complete store listing, icon/screenshots, support email/website, privacy policy URL, Data safety, content rating, target audience, ads declaration, app access instructions and any data deletion disclosures.
3. Upload the signed AAB to **Internal testing**. Enroll in/complete Play App Signing. Test on real devices installed by Play—not only a sideloaded debug APK.
4. Validate live Firebase/App Check/Boson/Gemini and RevenueCat flows with test accounts. Inspect crash logs and remove any test/demo state from the release experience.
5. Resolve all Play pre-launch report issues. Run a small staged rollout before increasing availability. Have a rollback and support-response plan.
6. Increment `versionCode` for each subsequent release. Never replace the production upload keystore with a newly generated one.

The Android app currently targets API level 36, which meets the Google Play requirement for new apps and updates starting August 31, 2026. Recheck the official policy immediately before submission: [Target API level requirements](https://support.google.com/googleplay/android-developer/answer/11926878).

## 5. Credentials and deployment summary

| Credential/config | Where it belongs |
|---|---|
| `BOSON_API_KEY` | Firebase Functions Secret Manager only |
| `GEMINI_API_KEY` | Firebase Functions Secret Manager only |
| `REVENUECAT_SECRET_API_KEY` | Firebase Functions Secret Manager only |
| RevenueCat public Android SDK key | Release build input `REVENUECAT_API_KEY` |
| Firebase Android config | Locally at `app/google-services.json`; CI as `GOOGLE_SERVICES_JSON_BASE64` |
| Upload keystore + passwords | Encrypted offline backup; GitHub Actions secrets for signing |
| Firebase App Check | Firebase Console + Play Integrity setup; no provider API secret pasted in Kotlin |

Never paste provider credentials into this chat or commit them to the public repository. Rotate a key if it was exposed.

## 6. Go/no-go gate

Do **not** call the app production-ready until all of these are evidenced:

- [ ] Signed AAB built from `main`; CI tests passed; checksum recorded.
- [ ] Correct package ID, production Firebase project, App Check, live cloud secrets and rules validated.
- [ ] Public privacy policy/support route and truthful store declarations completed.
- [ ] Account/data deletion requirement reviewed and an appropriate user-facing path implemented.
- [ ] Subscription catalog/entitlement and restore tested through Play internal testing.
- [ ] Physical-device voice, microphone permission, movement guard, location, offline, Story Studio and file sharing accepted.
- [ ] No false claims for SOS, verified navigation, external publishing or unsupported activity capabilities.
- [ ] Budget alerts, rate limits, crash reporting/monitoring and a support/rollback plan ready.
- [ ] Play Console pre-launch checks resolved and staged rollout approved.

This is a release gate, not a claim that these external account and device steps have already been completed.
