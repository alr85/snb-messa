# Implementation Plan - Fix Checkweigher Calibration Crash

The user reported a crash when navigating to the screen following the Test Weight screen, which is the "Dynamic Test (As Found)" screen. The cause is an `IndexOutOfBoundsException` in the `CalCwDynamicTestAsFound` composable.

## Problem Analysis

The `CalCwDynamicTestAsFound` composable assumes that `viewModel.dynamicPassesAsFound.value` always contains 10 elements. However, in `CalibrationCheckweigherViewModel.loadCalibration`, the list is populated by splitting a comma-separated string and filtering out blank values:

```kotlin
_dynamicPassesAsFound.value = cal.dynamicPassesAsFound.split(",").filter { it.isNotBlank() }
```

When a calibration is first created or has no dynamic passes saved, `cal.dynamicPassesAsFound` is an empty string. `"".split(",").filter { it.isNotBlank() }` results in an empty list. When the UI tries to access `passTexts[i]` for `i` from 0 to 9, it crashes.

## Proposed Changes

### ViewModel

#### [MODIFY] [CalibrationCheckweigherViewModel.kt](file:///C:/Users/44782/StudioProjects/snb-messa0/app/src/main/java/com/snb/inspect/calibrationViewModels/CalibrationCheckweigherViewModel.kt)

1.  Create a helper function to parse the comma-separated passes string into a list of exactly 10 strings, padding with empty strings if necessary.
2.  Use this helper in `loadCalibration` for both `dynamicPassesAsFound` and `dynamicPassesAsLeft`.
3.  (Optional but recommended) Update the `nominalQuantityAsFound` and `nominalQuantityAsLeft` initialization to handle empty strings safely in calculations if needed, though `toDoubleOrNull` already handles this in the UI.

Proposed Helper:
```kotlin
private fun parsePasses(passesString: String): List<String> {
    val list = passesString.split(",").map { it.trim() }
    return List(10) { i -> list.getOrNull(i) ?: "" }
}
```

## Verification Plan

### Automated Tests
- I will check if there are unit tests for `CalibrationCheckweigherViewModel` and add a test case for loading an empty calibration if possible.

### Manual Verification
1. Start a Checkweigher Calibration.
2. Navigate to the "Dynamic Test (As Found)" screen.
3. Verify that the screen loads without crashing.
4. Enter some values, navigate away and back, and verify the values are preserved and the screen still doesn't crash.
