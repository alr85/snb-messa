# Walkthrough - Multi-select for Calibration Failure Reasons

I have successfully converted the "Reason for not calibrating" field to a multi-select dropdown in the Metal Detector Conveyor calibration workflow.

## Changes Made

### ViewModel Updates
- **[CalibrationMetalDetectorConveyorViewModel.kt](file:///C:/Users/Adam Robson/StudioProjects/snb-messa/app/src/main/java/com/snb/inspect/calibrationViewModels/CalibrationMetalDetectorConveyorViewModel.kt)**:
    - Changed `reasonForNotCalibrating` to a `StateFlow<List<String>>`.
    - Added `reasonForNotCalibratingOther` state to store custom reasons.
    - Updated initialization logic to parse existing comma-separated strings from the database.

### UI Enhancements
- **[CalMetalDetectorConveyorCalibrationStart.kt](file:///C:/Users/Adam Robson/StudioProjects/snb-messa/app/src/main/java/com/snb/inspect/screens/service/mdCalibration/CalMetalDetectorConveyorCalibrationStart.kt)**:
    - Replaced the single-select dropdown with `LabeledMultiSelectDropdownWithHelp`.
    - Updated validation logic (`isNextStepEnabled`) to ensure at least one reason is selected, and if "Other" is selected, the custom text field must not be empty.
- **[CalMetalDetectorSummaryDetails.kt](file:///C:/Users/Adam Robson/StudioProjects/snb-messa/app/src/main/java/com/snb/inspect/screens/service/mdCalibration/CalMetalDetectorSummaryDetails.kt)**:
    - Updated to display all selected reasons joined by commas.

### Data Persistence
- **[DatabaseUpdates.kt](file:///C:/Users/Adam Robson/StudioProjects/snb-messa/app/src/main/java/com/snb/inspect/calibrationLogic/metalDetectorConveyor/DatabaseUpdates.kt)**:
    - Updated `toCalibrationStartUpdate` to combine the selected reasons and the "Other" text into a single string for storage, ensuring backward compatibility with the existing database schema.

## Verification Results

### Static Analysis
- Ran `analyze_file` on all modified files. No errors related to the new implementation were found. Some pre-existing lint warnings were noted but did not affect the new functionality.

### Manual Verification Steps (Recommended for User)
1. Start a Metal Detector Conveyor calibration.
2. Set "Able to calibrate?" to "No".
3. Select multiple reasons from the new dropdown.
4. Select "Other" and type a custom reason.
5. Verify the "Next" button enables/disables correctly based on selections.
6. Proceed to the Summary screen and verify the reasons are displayed correctly.
7. Save the calibration and re-open it to ensure all selections are persisted.
