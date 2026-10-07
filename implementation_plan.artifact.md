# Implement Forced In-App Updates with Pre-Sync

We will implement an automated, forced update flow using the Google Play Core In-App Updates API. To protect against data loss during an update, the app will intercept any pending update, force a silent background sync of all offline data to your servers, and only *then* trigger the un-dismissible Play Store update screen.

## User Review Required

> [!WARNING]
> **Offline Edge Case:** If the device has no internet connection (or a poor signal), the Google Play Store update check will inherently fail or timeout, meaning the update will be bypassed and the user can continue using the app normally (offline).
>
> However, if the Play Store *does* detect an update, but your internal sync fails due to a poor connection or server timeout, we must decide how to handle it.

## Open Questions

**How should we handle a failed pre-sync?**
If an update is available, but the data fails to upload (e.g., poor signal, Azure is waking up, API error), what should happen?
- **Option A (Safest):** Block the update and block the app. Show a screen saying "Update required, but we couldn't sync your offline data. Please move to a better signal area and try again." (The engineer cannot work until they get signal and update).
- **Option B (Data First, Delay Update):** Cancel the update flow entirely and let them into the main app to continue working offline. The update will try again on the next app launch.
- **Option C (Update Anyway):** Proceed with the Play Store update, knowing the data is saved locally and will (likely) survive the update process, syncing later.

*Please let me know which option you prefer!*

## Proposed Changes

### Gradle Configuration
Add the Google Play Core App Update library.

#### [MODIFY] [libs.versions.toml](file:///C:/Users/Adam Robson/StudioProjects/snb-messa/gradle/libs.versions.toml)
- Add version `playAppUpdate = "2.1.0"`
- Add library `google-play-app-update = { module = "com.google.android.play:app-update-ktx", version.ref = "playAppUpdate" }`

#### [MODIFY] [build.gradle.kts (app)](file:///C:/Users/Adam Robson/StudioProjects/snb-messa/app/build.gradle.kts)
- Add the `google-play-app-update` dependency.

### Main Activity & UI States
We will modify `MainActivity` to check for updates *before* checking the user login state.

#### [MODIFY] [MainActivity.kt](file:///C:/Users/Adam Robson/StudioProjects/snb-messa/app/src/main/java/com/snb/inspect/MainActivity.kt)
- **State Management:** Introduce an `AppInitState` enum (`CHECKING_UPDATES`, `SYNCING_BEFORE_UPDATE`, `READY_TO_LAUNCH`).
- **AppUpdateManager:** Use `AppUpdateManagerFactory.create(this)` to query for an `AppUpdateType.IMMEDIATE` update.
- **Sync Interceptor:** If an update is found, launch a coroutine that calls:
  - `mdSystemsRepository.uploadUnsyncedSystems()`
  - `cwSystemsRepository.uploadUnsyncedSystems()`
  - `calibrationRepository.uploadUnsyncedCalibrations()`
  - `cwCalibrationRepository.uploadUnsyncedCalibrations()`
  - `mdSystemNotesRepository.syncAllUnsyncedNotes()`
  - `cwSystemNotesRepository.syncAllUnsyncedNotes()`
- **New UI Screen:** Add a simple `@Composable fun UpdateSyncScreen()` to show a loading spinner with text: *"Important update required. Syncing your offline data safely to the server first..."* while the data uploads.
- **ActivityResultLauncher:** Register the intent launcher to trigger the immediate update UI. If the user cancels it (somehow), we will loop it back or close the app to enforce the update.

## Verification Plan

### Automated Tests
- Gradle Sync and build project to ensure Play Core dependency resolves correctly.

### Manual Verification
- You can test the in-app update flow locally using Play Console's "Internal App Sharing" or by pushing a newer version code to the Internal Test Track and waiting for the Play Store to cache the availability on your test tablet.
- Disconnect from Wi-Fi, create a dummy calibration/machine in the app (so it's unsynced), then trigger an update scenario to ensure the "Syncing offline data..." screen successfully blocks the update until the upload passes.