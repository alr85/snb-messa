# Implementation Plan - Persist Wiped Calibration Data

Investigation revealed that "UNSPECIFIED" values persist in the calibration certificate because the ViewModel state (which is reset to `NA` during the "Unable to calibrate" flow) is not being written to the database. When the calibration is finished, it pulls the existing (empty) values from the database, which are interpreted as `UNSPECIFIED`.

## User Review Required

> [!IMPORTANT]
> This change will cause a one-time database write of all calibration fields as "N/A" when an engineer selects "Unable to calibrate". This ensures the report correctly reflects that no tests were performed.

## Proposed Changes

### [Component] ViewModels

#### [MODIFY] [CalibrationMetalDetectorConveyorViewModel.kt](file:///C:/Users/Adam Robson/StudioProjects/snb-messa/app/src/main/java/com/snb/inspect/calibrationViewModels/CalibrationMetalDetectorConveyorViewModel.kt)
- Add `persistAllSections()` to call all update functions (updateInfeedSensor, updateBackupSensor, etc.).

#### [MODIFY] [CalibrationCheckweigherViewModel.kt](file:///C:/Users/Adam Robson/StudioProjects/snb-messa/app/src/main/java/com/snb/inspect/calibrationViewModels/CalibrationCheckweigherViewModel.kt)
- Add `persistAllSections()` to save the entire state to the database.

### [Component] Screens

#### [MODIFY] [CalMetalDetectorConveyorCalibrationStart.kt](file:///C:/Users/Adam Robson/StudioProjects/snb-messa/app/src/main/java/com/snb/inspect/screens/service/mdCalibration/CalMetalDetectorConveyorCalibrationStart.kt)
- Call `viewModel.persistAllSections()` in the "Yes, wipe data" confirmation button.

#### [MODIFY] [CalCheckweigherCalibrationStart.kt](file:///C:/Users/Adam Robson/StudioProjects/snb-messa/app/src/main/java/com/snb/inspect/screens/service/cwCalibration/CalCheckweigherCalibrationStart.kt)
- Call `viewModel.persistAllSections()` in the "Yes, wipe data" confirmation button.

## Verification Plan

### Manual Verification
1. Start a new calibration.
2. Select "No" for "Able to calibrate?".
3. Confirm the wipe.
4. Finish the calibration.
5. Verify the certificate shows "N/A" for all sensor fields.
