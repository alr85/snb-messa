# Multi-select for Calibration Failure Reasons

This plan outlines the changes to convert the "Reason for not calibrating" field from a single-selection dropdown to a multi-selection dropdown in the Metal Detector Conveyor calibration start screen. This follows the pattern used for "Test Result" fields in sensor screens.

## Proposed Changes

### ViewModel & Data Layer

#### [CalibrationMetalDetectorConveyorViewModel.kt](file:///C:/Users/Adam Robson/StudioProjects/snb-messa/app/src/main/java/com/snb/inspect/calibrationViewModels/CalibrationMetalDetectorConveyorViewModel.kt)

- Change `_reasonForNotCalibrating` from `mutableStateOf("")` to `MutableStateFlow(listOf<String>())`.
- Add `_reasonForNotCalibratingOther` as a `MutableStateFlow("")`.
- Update `setReasonForNotCalibrating` to accept `List<String>`.
- Add `setReasonForNotCalibratingOther(String)`.
- Update the initialization logic (where `existingCalibration` is loaded) to parse the comma-separated string into a list and extract the "Other" reason if it's not in the common reasons list.
- Update `shouldSkipToSummary()` to account for the new list-based state.

#### [DatabaseUpdates.kt](file:///C:/Users/Adam Robson/StudioProjects/snb-messa/app/src/main/java/com/snb/inspect/calibrationLogic/metalDetectorConveyor/DatabaseUpdates.kt)

- Update `toCalibrationStartUpdate` to combine the `reasonForNotCalibrating` list and the `reasonForNotCalibratingOther` text into a single string for database storage, maintaining compatibility with the existing schema.

---

### UI Components

#### [CalMetalDetectorConveyorCalibrationStart.kt](file:///C:/Users/Adam Robson/StudioProjects/snb-messa/app/src/main/java/com/snb/inspect/screens/service/mdCalibration/CalMetalDetectorConveyorCalibrationStart.kt)

- Replace `LabeledDropdownWithHelp` with `LabeledMultiSelectDropdownWithHelp` for the "Reason for not calibrating" field.
- Update `isNextStepEnabled` logic to handle the list of reasons.
- Show the "Other reason" text field if "Other" is contained in the selected reasons list.
- Move `commonReasons` to the ViewModel as a constant to ensure consistency between UI and data processing.

#### [CalMetalDetectorSummaryDetails.kt](file:///C:/Users/Adam Robson/StudioProjects/snb-messa/app/src/main/java/com/snb/inspect/screens/service/mdCalibration/CalMetalDetectorSummaryDetails.kt)

- Update the display of "Reason for not calibrating" to join the list of reasons into a comma-separated string, including the "Other" reason if applicable.

---

## Verification Plan

### Automated Tests
- I will check for any existing unit tests for `CalibrationMetalDetectorConveyorViewModel` and update them if they test the calibration start logic.
- Run `./gradlew :app:testDebugUnitTest --tests "com.snb.inspect.calibrationViewModels.CalibrationMetalDetectorConveyorViewModelTest"` (if it exists).

### Manual Verification
1. Deploy the app to a device/emulator.
2. Navigate to a Metal Detector Conveyor calibration.
3. On the Start screen, select "No" for "Able to calibrate?".
4. Verify that "Reason for not calibrating" is now a multi-select dropdown.
5. Select multiple reasons, including "Other".
6. Fill in the "Other" reason text field.
7. Proceed to the summary screen and verify that all selected reasons (including the custom one) are displayed correctly.
8. Go back and change the selection, then verify the summary updates correctly.
9. Close the calibration and re-open it to verify the selections are persisted and re-loaded correctly.
