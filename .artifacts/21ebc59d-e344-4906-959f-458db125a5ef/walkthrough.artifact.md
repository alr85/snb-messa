# Walkthrough - Start Calibration Double-Tap Protection ("Double Bounce")

## Overview
Added click debouncing ("double bounce" / double-tap protection) to the "New Calibration" start buttons on both Metal Detector and Checkweigher system screens. This prevents engineers from double-tapping or multi-tapping the start button to accidentally trigger multiple concurrent calibration sessions.

---

## Changes Made

### 1. Button Click Debouncing ("Double Bounce" Protection)
#### [MetalDetectorConveyorSystemScreen.kt](file:///C:/Users/Adam Robson/StudioProjects/snb-messa/app/src/main/java/com/snb/inspect/screens/service/MetalDetectorConveyorSystemScreen.kt) & [CheckweigherSystemScreen.kt](file:///C:/Users/Adam Robson/StudioProjects/snb-messa/app/src/main/java/com/snb/inspect/screens/service/CheckweigherSystemScreen.kt)
- Introduced `isStartingCalibration` state variable.
- Wrapped `startCalibration()` with a guard clause checking `isStartingCalibration` and resetting it after a 1.5-second debounce window (`finally { delay(1500L); isStartingCalibration = false }`).
- Guarded the FloatingActionButton `onClick` lambda with `if (!isStartingCalibration)` to completely prevent double-bouncing/double-tapping.

---

## Verification Results

### Automated Tests
- Successfully built the app using `gradle_build("app:assembleDebug")`.
- **Result:** Build finished successfully with zero errors.
