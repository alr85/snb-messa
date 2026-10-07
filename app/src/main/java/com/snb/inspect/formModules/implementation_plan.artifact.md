# Implementation Plan - Enabling Spell Check and Improving Text Input

The user has reported spelling mistakes made by engineers during calibration. This plan aims to enable the built-in Android spell check/autocorrect features in the app's text input fields and improve the typing experience for free-text fields like "Engineer Notes".

## User Review Required

> [!IMPORTANT]
> The "Engineer Notes" fields are currently limited to 50 characters (and some even 25). If engineers are making many spelling mistakes, it might be partly due to these tight limits forcing abbreviations. I recommend increasing these limits if the database schema allows it.

## Proposed Changes

### Core UI Components

#### [MODIFY] [SimpleTextInput.kt](file:///C:/Users/Adam Robson/StudioProjects/snb-messa/app/src/main/java/com/snb/inspect/formModules/inputs/SimpleTextInput.kt)
- Add `autoCorrect: Boolean = true` parameter to the `SimpleTextInput` composable.
- Explicitly set `autoCorrect = autoCorrect` in the `KeyboardOptions` passed to `OutlinedTextField`.
- This ensures that the system's autocorrect and spell-checking highlights are enabled for text inputs.

#### [MODIFY] [LabeledTextFieldWithHelp.kt](file:///C:/Users/Adam Robson/StudioProjects/snb-messa/app/src/main/java/com/snb/inspect/formModules/LabeledTextFieldWithHelp.kt)
- Add `autoCorrect: Boolean = true` parameter.
- Pass this parameter down to `SimpleTextInput`.

### Calibration Screens

#### [MODIFY] Various Calibration Screens
- Update "Engineer Notes" fields to use `singleLine = false` and `minLines = 3` (or similar) to provide a better typing area.
- Most keyboards provide better spell-check and autocorrect support when the field is multi-line.
- Key files to update:
    - `CalMetalDetectorConveyorFerrousTest.kt`
    - `CalMetalDetectorConveyorNonFerrousTest.kt`
    - `CalMetalDetectorConveyorStainlessTest.kt`
    - (And other screens with "Engineer Notes")

## Verification Plan

### Manual Verification
- Deploy the app to a device.
- Navigate to a calibration screen (e.g., Ferrous Test).
- Type intentionally misspelled words into the "Engineer Notes" field.
- Verify that the keyboard suggests corrections or highlights the misspelled words (depending on the device's keyboard settings).
- Verify that the "Engineer Notes" field is now multi-line and easier to use.
