# Implementation Plan - Preventing New Calibration When Incomplete Calibration Exists

## Problem Description
Previously, if a user tried to start a new calibration while an incomplete calibration already existed for that system, the app either resumed it automatically or allowed creating duplicate sessions. The user now requires that **if an incomplete calibration already exists when attempting to start a new calibration, the request must be cancelled/blocked and the user notified**.

## User Review Required
> [!IMPORTANT]
> When `startCalibration()` is invoked on a system:
> 1. The app checks local storage for any existing unfinished calibration (`endDate IS NULL OR endDate = ''`) for that system ID.
> 2. If an incomplete calibration is found, **new calibration creation is cancelled**, and a Snackbar notification alerts the user: *"⚠️ An incomplete calibration already exists for this system. Please complete or delete it first."*
> 3. If no incomplete calibration exists, the new calibration session starts normally.

## Proposed Changes

### Database & DAO Layer
#### [MODIFY] [CheckweigherCalibrationDAO.kt](file:///C:/Users/Adam Robson/StudioProjects/snb-messa/app/src/main/java/com/snb/inspect/daos/CheckweigherCalibrationDAO.kt)
- Add `getUnfinishedCalibrationForSystem(systemId)` query.

### UI & Screen Layer
#### [MODIFY] [MetalDetectorConveyorSystemScreen.kt](file:///C:/Users/Adam Robson/StudioProjects/snb-messa/app/src/main/java/com/snb/inspect/screens/service/MetalDetectorConveyorSystemScreen.kt)
- Update `startCalibration()` to check `dao.getUnfinishedCalibrationForSystem(system.id)`. If present, cancel start and display notification.

#### [MODIFY] [CheckweigherSystemScreen.kt](file:///C:/Users/Adam Robson/StudioProjects/snb-messa/app/src/main/java/com/snb/inspect/screens/service/CheckweigherSystemScreen.kt)
- Update `startCalibration()` to check `cwCalibrationDAO.getUnfinishedCalibrationForSystem(system.id)`. If present, cancel start and display notification.

## Verification Plan

### Automated Tests
- Build the project using `gradle_build("app:assembleDebug")` to ensure compilation and correct syntax.

### Manual Verification
- Start an MD or CW calibration and leave it unfinished.
- Return to the system screen and attempt to start a new calibration.
- Verify that a notification appears and the new calibration is blocked/cancelled.
