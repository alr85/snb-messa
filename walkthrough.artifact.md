# Walkthrough: Implementing Forced In-App Updates with Pre-Sync

The implementation for the forced update flow with safe data syncing is now complete.

## Changes Made

1. **Gradle Dependencies Added**:
   - Added `com.google.android.play:app-update-ktx` to the project to interface with the Google Play Store update APIs.

2. **MainActivity Pre-Launch State Machine**:
   - Introduced an `AppInitState` enum (`CHECKING_UPDATES`, `SYNCING_BEFORE_UPDATE`, `READY_TO_LAUNCH`).
   - The app now launches into a blank screen temporarily while it queries the Google Play Services layer using `AppUpdateManagerFactory.create(this)`.

3. **Interception & Sync Logic**:
   - If an update is detected, the UI switches to the `UpdateSyncScreen()` which shows a progress spinner explaining to the user that their offline data is being saved.
   - The `syncDataBeforeUpdate()` function fires in the background, utilizing your existing repository upload functions:
     - `mdSystemsRepository.uploadUnsyncedSystems()`
     - `cwSystemsRepository.uploadUnsyncedSystems()`
     - `calibrationRepository.uploadUnsyncedCalibrations()`
     - `cwCalibrationRepository.uploadUnsyncedCalibrations()`
     - `mdSystemNotesRepository.syncAllUnsyncedNotes()`
     - `cwSystemNotesRepository.syncAllUnsyncedNotes()`

4. **Triggering the Update / Option B Fallback**:
   - **Success:** If the sync finishes without throwing any exceptions, it triggers `appUpdateManager.startUpdateFlowForResult`, which overlays the un-dismissible Play Store update UI onto the screen.
   - **Failure (Option B):** If the background sync fails (e.g. they walked out of Wi-Fi range while it was uploading), the `catch` block intercepts the error, bypasses the Play Store update entirely, sets the state to `READY_TO_LAUNCH`, and lets the user proceed into the app to continue their offline work. The update will re-trigger the next time they launch the app.

## How to Test This
Because you are using the Internal Test Track on the Play Store, here is how you can verify this works:

1. Build and install the app on your device (Version A).
2. Create an unsynced record (e.g., add a machine or calibration while disconnected from the network).
3. Push a *newer* version (Version B) to the Google Play Console Internal Test Track.
4. Wait 10-15 minutes, then open the Google Play Store app on the tablet to ensure it has registered the update.
5. Reconnect the tablet to Wi-Fi.
6. Open your app. You should see the "Important Update Required - Syncing your offline data..." screen briefly.
7. Once the sync finishes, the full-screen Play Store UI should appear forcing you to update.
8. To test the fallback (Option B): Disconnect your Wi-Fi *exactly* when the "Syncing offline data..." screen appears. The sync should throw a network exception, and the app will load the login/main screen as normal.