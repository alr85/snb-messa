# Implementation Plan - Flawless Data Recovery with Persisted Permissions

The goal is to make the data recovery system "flawless" by persisting the user's manual folder selection so that the app can automatically scan for backups on every launch after the first manual link.

## User Review Required

> [!IMPORTANT]
> Once you select the `SNB_Inspect_Recovery` folder via the manual "Recover Data" button, the app will **permanently remember** that link. It will automatically scan that folder for missing data every time the app opens from then on, without you having to click the button again for that installation.

## Proposed Changes

### [Component Name] util

#### [MODIFY] [SyncPreferences.kt](file:///C:/Users/44782/StudioProjects/snb-messa0/app/src/main/java/com/snb/inspect/util/SyncPreferences.kt)
- Add `getRecoveryFolderUri()` and `setRecoveryFolderUri(uri: String?)`.

#### [MODIFY] [DataBackupManager.kt](file:///C:/Users/44782/StudioProjects/snb-messa0/app/src/main/java/com/snb/inspect/util/DataBackupManager.kt)
- Update `importFromTreeUri` to:
    1. Call `takePersistableUriPermission` so the app can access it after restart.
    2. Save the URI to `SyncPreferences`.
- Update `checkAndRestore` to:
    1. Check `SyncPreferences` for a saved folder URI.
    2. If found, automatically scan it using `importFromTreeUri` logic.

### [Component Name] screens

#### [MODIFY] [MyCalibrations.kt](file:///C:/Users/44782/StudioProjects/snb-messa0/app/src/main/java/com/snb/inspect/screens/menu/MyCalibrations.kt)
- Update the manual recovery button logic to handle the new persisted workflow.
- Ensure the button remains available in case the user wants to point to a different folder.

## Verification Plan

### Automated Tests
- Verify build success.

### Manual Verification
1. Open "My Calibrations".
2. Click **"Recover Data from Folder"**.
3. Select the folder and allow access.
4. Verify data is restored.
5. Close and restart the app.
6. Verify the logs show an **automatic** scan of the persisted folder on launch.
