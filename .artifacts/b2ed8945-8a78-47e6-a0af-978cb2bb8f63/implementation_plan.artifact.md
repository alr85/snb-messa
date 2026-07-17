# Implementation Plan - Revert Styling & Fix Logic for Calibration Viewer

The goal is to revert the styling of the `CalibrationDetailViewer.kt` to use the SNB Red brand colors and the initial `DetailSection`/`DetailRow` components, while maintaining the comprehensive data structure from the MD summary screen. I will also fix the logic that determines if detailed data should be shown.

## User Review Required

> [!IMPORTANT]
> The "Able to calibrate" logic will be updated to handle both `"true"` and `"Yes"` values, as the current implementation stores booleans as strings in some cases.

## Proposed Changes

### [Menu & UI]

#### [MODIFY] [CalibrationDetailViewer.kt](file:///C:/Users/Adam Robson/StudioProjects/snb-messa/app/src/main/java/com/snb/inspect/screens/menu/CalibrationDetailViewer.kt)
- Reintroduce `DetailSection`, `DetailSubSection`, and `DetailRow` components with the original styling (SNB Red headers, white cards).
- Update `MetalDetectorCalibrationDetailsView` to use these components.
- Fix the visibility logic: replace `if (cal.canPerformCalibration == "Yes")` with a check that handles `"true"`, `"false"`, `"Yes"`, and `"No"`.
- Ensure all sections from the MD summary screen are included but rendered with the preferred styling.

## Verification Plan

### Manual Verification
1. Open the app and navigate to "My Calibrations".
2. View a completed MD calibration where "Able to calibrate" was true.
3. Verify that the rest of the details (Conveyor, Checklist, etc.) are now visible.
4. Verify that section headers are back to SNB Red and the layout matches the first iteration's aesthetic.
