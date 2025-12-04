package com.example.mecca.calibrationLogic.metalDetectorConveyor

import com.example.mecca.calibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import com.example.mecca.formModules.YesNoState

/**
 * ---------------------------------------------------------------
 *  Calibration PV Auto-Evaluation Rules
 * ---------------------------------------------------------------
 *
 * This file contains the logic that automatically determines the
 * Pass / Fail / N/A status of various PV (Performance Validation)
 * checks during calibration.
 *
 * These rules used to sit inside the main ViewModel, mixed in
 * with state variables, database calls, UI triggers, and general
 * chaos. Now they live here, where:
 *
 *   • The logic is easy to find
 *   • The rules are easy to modify
 *   • The ViewModel isn’t clogged with decision trees
 *   • Unit testing is far simpler
 *
 * Each function here is an *extension function* on the
 * CalibrationMetalDetectorConveyorViewModel. This allows the
 * logic to behave as if it were part of the ViewModel, without
 * actually bloating the ViewModel file itself.
 *
 * In short:
 *   The ViewModel notifies *when* a PV result needs updating.
 *   This file decides *what* the result should be.
 *
 * If you’re debugging:
 *   – Check that the state values feeding into these rules
 *     (e.g., YES/NO/NA or peak signals) are correct
 *   – Check that the ViewModel is actually calling the rule
 *   – Remember these rules won't override manual user choices
 *     unless explicitly written to do so
 *
 * ---------------------------------------------------------------
 */


// ---------------------------------------------------------
// Detection Setting PV Logic
// ---------------------------------------------------------
fun CalibrationMetalDetectorConveyorViewModel.autoUpdateDetectionSettingPvResult() {
    val value = sensitivityAccessRestriction.value.trim()

    when {
        value.equals("N/A", ignoreCase = true) ->
            setDetectionSettingPvResult("N/A")

        value.isBlank() ->
            setDetectionSettingPvResult("Fail")

        else ->
            setDetectionSettingPvResult("Pass")
    }
}

// ---------------------------------------------------------
// Ferrous PV Logic
// ---------------------------------------------------------
fun CalibrationMetalDetectorConveyorViewModel.autoUpdateFerrousPvResult() {
    val leading = detectRejectFerrousLeading.value
    val middle = detectRejectFerrousMiddle.value
    val trailing = detectRejectFerrousTrailing.value

    val leadingPeak = peakSignalFerrousLeading.value
    val middlePeak = peakSignalFerrousMiddle.value
    val trailingPeak = peakSignalFerrousTrailing.value

    val sampleCertificate = sampleCertificateNumberFerrous.value.trim()

    val achievedSensitivityString = sensitivityAsLeftFerrous.value.trim()
    val achievedSensitivity = achievedSensitivityString.toDoubleOrNull()
    val maxSensitivity = sensitivityData.value?.FerrousMaxMM ?: 0.0

    val retailerSensitivityAchieved =
        achievedSensitivity?.let { it <= maxSensitivity } ?: false

    // Handle "N/A"
    if (achievedSensitivityString.equals("N/A", ignoreCase = true)) {
        setFerrousTestPvResult("N/A")
        return
    }

    val allPassed =
        leading == YesNoState.YES &&
                middle == YesNoState.YES &&
                trailing == YesNoState.YES &&
                leadingPeak.isNotBlank() &&
                middlePeak.isNotBlank() &&
                trailingPeak.isNotBlank() &&
                sampleCertificate.isNotBlank() &&
                retailerSensitivityAchieved

    val anyFailed =
        leading == YesNoState.NO ||
                leading == YesNoState.NA ||
                middle == YesNoState.NO ||
                middle == YesNoState.NA ||
                trailing == YesNoState.NO ||
                trailing == YesNoState.NA ||
                (leading == YesNoState.YES && leadingPeak.isBlank()) ||
                (middle == YesNoState.YES && middlePeak.isBlank()) ||
                (trailing == YesNoState.YES && trailingPeak.isBlank()) ||
                sampleCertificate.isBlank() ||
                !retailerSensitivityAchieved

    when {
        allPassed -> setFerrousTestPvResult("Pass")
        anyFailed -> setFerrousTestPvResult("Fail")
        else -> { /* no auto change */ }
    }
}


// ---------------------------------------------------------
// Set all PV results to N/A
// ---------------------------------------------------------
fun CalibrationMetalDetectorConveyorViewModel.setAllPvResultsNa() {
    setFerrousTestPvResult("N/A")
    setDetectionSettingPvResult("N/A")
}