# Walkthrough - Checkweigher Calibration Fixes

This document summarizes the changes made to the Checkweigher calibration flow, including UI improvements and a critical crash fix.

## 1. Banner Title Fix

The `CalibrationBanner` was incorrectly displaying "Metal Detector Calibration" for all calibration types. I updated the banner logic to dynamically show the correct title based on the active ViewModel.

### Changes:
- Modified [CalibrationBanner.kt](file:///C:/Users/44782/StudioProjects/snb-messa0/app/src/main/java/com/snb/inspect/CalibrationBanner.kt) to use a `when` expression for title selection.

```kotlin
val type = when (viewModel) {
    is CalibrationMetalDetectorConveyorViewModel -> {
        if (pvRequired) "Metal Detector Performance Verification"
        else "Metal Detector Calibration"
    }
    is CalibrationCheckweigherViewModel -> "Checkweigher Calibration"
    else -> "Calibration"
}
```

## 2. Split Failsafe Tests

To improve clarity and match the Metal Detector workflow, I split the combined "Failsafes" screen into five separate screens.

### Changes:
- **ViewModel Update**: Updated [CalibrationCheckweigherViewModel.kt](file:///C:/Users/44782/StudioProjects/snb-messa0/app/src/main/java/com/snb/inspect/calibrationViewModels/CalibrationCheckweigherViewModel.kt) with new state fields and validation logic for each failsafe section.
- **Navigation Update**: Updated the [NavGraphContent](file:///C:/Users/44782/StudioProjects/snb-messa0/app/src/main/java/com/snb/inspect/ui/theme/CheckweigherCalibrationNavGraphContent.kt) and [ScreenWrapper](file:///C:/Users/44782/StudioProjects/snb-messa0/app/src/main/java/com/snb/inspect/screens/service/cwCalibration/CheckweigherCalibrationScreenWrapper.kt) to support the new multi-step flow.
- **New Screens**: Created dedicated screens for Infeed, Reject Confirm, Bin Full, Air Pressure, and Bin Door Monitor tests.

## 3. Critical Crash Fix (IndexOutOfBoundsException)

Fixed a crash that occurred when navigating to the "Dynamic Test" screen for a new or incomplete calibration. The screen expected exactly 10 data points, but the ViewModel was providing an empty list if no data was saved.

### Changes:
- Added a `parsePasses` helper function to [CalibrationCheckweigherViewModel.kt](file:///C:/Users/44782/StudioProjects/snb-messa0/app/src/main/java/com/snb/inspect/calibrationViewModels/CalibrationCheckweigherViewModel.kt).
- Updated `loadCalibration` to use this helper, ensuring that the dynamic passes list always contains exactly 10 elements, padded with empty strings if necessary.

```kotlin
private fun parsePasses(passesString: String): List<String> {
    if (passesString.isBlank()) return List(10) { "" }
    val list = passesString.split(",").map { it.trim() }
    return List(10) { i -> list.getOrNull(i) ?: "" }
}
```

## Verification Results

### Automated Tests
- Successfully performed a full Gradle build (`app:assembleDebug`) to ensure no syntax errors or unresolved references were introduced.

### Manual Verification (Expected)
- The Checkweigher Calibration banner now correctly displays "Checkweigher Calibration".
- Navigating past the "Test Weight" screen no longer causes a crash.
- The failsafe section is now split into multiple focused steps with proper validation.
