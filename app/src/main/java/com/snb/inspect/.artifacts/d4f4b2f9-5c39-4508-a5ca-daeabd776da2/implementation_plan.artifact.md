# Restrict Navigation When System Cannot Be Calibrated

When a user indicates that a system cannot be calibrated (e.g., "Able to calibrate?" = "No"), they should only be allowed to navigate between the "Calibration Start" screen and the "Summary" screen. This prevents them from accessing or seeing indicators for intermediate steps that are no longer relevant.

## Proposed Changes

### [Calibration]

#### [MODIFY] [MetalDetectorConveyorCalibrationScreenWrapper.kt](file:///C:/Users/Adam Robson/StudioProjects/snb-messa/app/src/main/java/com/snb/inspect/screens/service/mdCalibration/MetalDetectorConveyorCalibrationScreenWrapper.kt)

- Collect `canPerformCalibration` and `reasonForNotCalibrating` from the ViewModel.
- Update `routeOrder` to be dynamic:
    - If `shouldSkipToSummary` is true (Able = No AND reasons selected), `routeOrder` will only contain "Start" and "Summary".
    - Otherwise, it will contain the full list of screens.
- Clean up the `Box` and `CalibrationNavigationButtons` code to remove redundant `shouldSkipToSummary` checks, relying on the dynamic `routeOrder` instead.

#### [MODIFY] [CheckweigherCalibrationScreenWrapper.kt](file:///C:/Users/Adam Robson/StudioProjects/snb-messa/app/src/main/java/com/snb/inspect/screens/service/cwCalibration/CheckweigherCalibrationScreenWrapper.kt)

- Collect `canPerformCalibration` and `reasonForNotCalibrating` from the ViewModel.
- Update `routeOrder` to be dynamic, similar to the Metal Detector wrapper.

## Verification Plan

### Manual Verification
- Start a Metal Detector calibration.
- Set "Able to calibrate?" to "No" and select a reason.
- Verify that:
    - The navigation dropdown (side menu) only shows "Calibration Start" and "Summary".
    - Swiping left or clicking "Next" takes you directly to the Summary screen.
    - Swiping right or clicking "Previous" from the Summary takes you back to Start.
- Repeat for a Checkweigher calibration.
