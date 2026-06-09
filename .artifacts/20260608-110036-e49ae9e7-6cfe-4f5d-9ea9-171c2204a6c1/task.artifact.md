# Task: Implement Multi-select for Calibration Failure Reasons

- [x] Update `CalibrationMetalDetectorConveyorViewModel`
    - [x] Change `reasonForNotCalibrating` to `List<String>`
    - [x] Add `reasonForNotCalibratingOther`
    - [x] Update loading/saving logic
    - [x] Update `shouldSkipToSummary`
- [x] Update `DatabaseUpdates.kt` to handle string conversion
- [x] Update `CalMetalDetectorConveyorCalibrationStart.kt` UI
    - [x] Use `LabeledMultiSelectDropdownWithHelp`
    - [x] Update validation logic
- [x] Update `CalMetalDetectorSummaryDetails.kt` UI
- [x] Verify implementation
    - [x] Check persistence (via static analysis of DatabaseUpdates)
    - [x] Check summary display (via code review)
