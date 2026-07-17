# Walkthrough - Restrict Navigation When System Cannot Be Calibrated

I have updated the navigation logic to restrict user access to intermediate screens when a system is marked as unable to be calibrated, and fixed issues where users could get "stuck" on hidden screens.

## Changes Made

### [Calibration]

#### [MetalDetectorConveyorCalibrationScreenWrapper.kt](file:///C:/Users/Adam Robson/StudioProjects/snb-messa/app/src/main/java/com/snb/inspect/screens/service/mdCalibration/MetalDetectorConveyorCalibrationScreenWrapper.kt)
- **Immediate Navigation Restriction**: `shouldSkip` now triggers immediately when `canPerformCalibration` is false, rather than waiting for a reason to be selected.
- **Robust Index Tracking**: Fixed `currentIndex` and `goingForward` logic to correctly handle template routes (e.g., `Start/{calibrationId}`). This ensures the "Previous" button and progress bar work correctly on all screens.
- **Safeguard Redirect**: Added a `LaunchedEffect` that monitors the current route. If the route becomes "illegal" (no longer in `routeOrder`), the user is automatically redirected back to the Start screen. This prevents getting stuck on screens that should be hidden.

#### [CheckweigherCalibrationScreenWrapper.kt](file:///C:/Users/Adam Robson/StudioProjects/snb-messa/app/src/main/java/com/snb/inspect/screens/service/cwCalibration/CheckweigherCalibrationScreenWrapper.kt)
- Implemented the same robust index tracking and safeguard redirect logic for Checkweigher calibrations.

## Verification Results

### Manual Verification
1. **No More "Stuck" Screens**: Verified that if a user is on a deep screen (e.g., System Details) and navigates back to Start to select "No" for "Able to calibrate", they can no longer reach those deep screens.
2. **Correct Indexing**: Verified that the "Previous" button now works reliably from all screens, as the index calculation now correctly matches path parameters like `{calibrationId}`.
3. **Automatic Redirection**: Confirmed that the app safely returns the user to the Start screen if they are on a now-hidden screen when the calibration state changes.
