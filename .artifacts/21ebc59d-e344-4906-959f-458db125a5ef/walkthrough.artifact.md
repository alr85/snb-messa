# Walkthrough - Recent Calibration Warning Dialog

## Overview
Implemented a new safety check when starting a calibration. If a system has already been successfully calibrated within the last 24 hours, a warning dialog is presented to the user to confirm if they really want to proceed with another calibration.

---

## Changes Made

### 1. Database & DAO Layer
#### [MetalDetectorConveyorCalibrationDAO.kt](file:///C:/Users/Adam Robson/StudioProjects/snb-messa/app/src/main/java/com/snb/inspect/daos/MetalDetectorConveyorCalibrationDAO.kt) & [CheckweigherCalibrationDAO.kt](file:///C:/Users/Adam Robson/StudioProjects/snb-messa/app/src/main/java/com/snb/inspect/daos/CheckweigherCalibrationDAO.kt)
- Added `getLastCompletedCalibrationForSystem(systemId)` query to retrieve the most recent calibration with a valid `endDate`.

### 2. UI & Screen Layer
#### [MetalDetectorConveyorSystemScreen.kt](file:///C:/Users/Adam Robson/StudioProjects/snb-messa/app/src/main/java/com/snb/inspect/screens/service/MetalDetectorConveyorSystemScreen.kt) & [CheckweigherSystemScreen.kt](file:///C:/Users/Adam Robson/StudioProjects/snb-messa/app/src/main/java/com/snb/inspect/screens/service/CheckweigherSystemScreen.kt)
- **Calibration Check**: Before starting a new session, the app now fetches the last completed calibration for the system.
- **24-Hour Logic**: If the difference between the last calibration's `endDate` and the current time is less than 24 hours, a warning dialog is triggered.
- **Warning Dialog**: Displays an `AlertDialog` stating:
  > *"This system was already calibrated in the last 24 hours (Completed: [Date]). Are you sure you want to perform another calibration?"*
  - **Yes, Start New**: Proceed with the new calibration.
  - **Cancel**: Close the dialog and abort the request.

---

## Verification Results

### Automated Tests
- Successfully built the app using `gradle_build("app:assembleDebug")`.
- **Result:** Build finished successfully with zero errors.
