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
// Sensitivity PV Logic
// ---------------------------------------------------------

fun CalibrationMetalDetectorConveyorViewModel.autoUpdateFerrousPvResult() {

    val result = evaluatePvResult(
        achievedSensitivityString = sensitivityAsLeftFerrous.value.trim(),
        maxSensitivity = sensitivityData.value?.FerrousMaxMM,
        leadingState = detectRejectFerrousLeading.value,
        middleState = detectRejectFerrousMiddle.value,
        trailingState = detectRejectFerrousTrailing.value,
        leadingPeak = peakSignalFerrousLeading.value,
        middlePeak = peakSignalFerrousMiddle.value,
        trailingPeak = peakSignalFerrousTrailing.value,
        certificate = sampleCertificateNumberFerrous.value.trim()
    )

    setFerrousTestPvResult(result)
}


fun CalibrationMetalDetectorConveyorViewModel.autoUpdateNonFerrousPvResult() {

    val result = evaluatePvResult(
        achievedSensitivityString = sensitivityAsLeftNonFerrous.value.trim(),
        maxSensitivity = sensitivityData.value?.NonFerrousMaxMM,
        leadingState = detectRejectNonFerrousLeading.value,
        middleState = detectRejectNonFerrousMiddle.value,
        trailingState = detectRejectNonFerrousTrailing.value,
        leadingPeak = peakSignalNonFerrousLeading.value,
        middlePeak = peakSignalNonFerrousMiddle.value,
        trailingPeak = peakSignalNonFerrousTrailing.value,
        certificate = sampleCertificateNumberNonFerrous.value.trim()
    )

    setNonFerrousTestPvResult(result)
}

fun CalibrationMetalDetectorConveyorViewModel.autoUpdateStainlessPvResult() {

    val result = evaluatePvResult(
        achievedSensitivityString = sensitivityAsLeftStainless.value.trim(),
        maxSensitivity = sensitivityData.value?.Stainless316MaxMM,
        leadingState = detectRejectStainlessLeading.value,
        middleState = detectRejectStainlessMiddle.value,
        trailingState = detectRejectStainlessTrailing.value,
        leadingPeak = peakSignalStainlessLeading.value,
        middlePeak = peakSignalStainlessMiddle.value,
        trailingPeak = peakSignalStainlessTrailing.value,
        certificate = sampleCertificateNumberStainless.value.trim()
    )

    setStainlessTestPvResult(result)
}

fun CalibrationMetalDetectorConveyorViewModel.autoUpdateInfeedSensorPvResult() {

    if (!pvRequired.value) {
        setInfeedSensorTestPvResult("N/A")
        return
    }

    if (infeedSensorFitted.value == YesNoState.NO) {
        setInfeedSensorTestPvResult("Not Fitted")
        return
    }

    if (infeedSensorFitted.value == YesNoState.NA) {
        setInfeedSensorTestPvResult("N/A")
        return
    }

    val passes =
        infeedSensorDetail.value.isNotBlank() &&
                infeedSensorTestMethod.value.isNotBlank() &&
                infeedSensorTestResult.value.isNotEmpty() &&
                "No Result" !in infeedSensorTestResult.value &&
                infeedSensorLatched.value == YesNoState.YES &&
                infeedSensorCR.value == YesNoState.YES &&
                (infeedSensorTestMethod.value != "Other" ||
                        infeedSensorTestMethodOther.value.isNotBlank())

    setInfeedSensorTestPvResult(
        if (passes) "Pass" else "Fail"
    )


}

fun CalibrationMetalDetectorConveyorViewModel.autoUpdateRejectConfirmSensorPvResult() {

    // If PV isn't required for this calibration, make it N/A and stop.
    if (!pvRequired.value) {
        setRejectConfirmSensorPvResult("N/A")
        return
    }

    if (rejectConfirmSensorFitted.value == YesNoState.NO) {
        setRejectConfirmSensorPvResult("Not Fitted")
        return
    }

    if (rejectConfirmSensorFitted.value == YesNoState.NA) {
        setRejectConfirmSensorPvResult("N/A")
        return
    }


    val passes =
        rejectConfirmSensorDetail.value.isNotBlank() &&
                rejectConfirmSensorTestMethod.value.isNotBlank() &&
                rejectConfirmSensorTestResult.value.isNotEmpty() &&
                "No Result" !in rejectConfirmSensorTestResult.value &&
                rejectConfirmSensorLatched.value != YesNoState.NA &&
                rejectConfirmSensorLatched.value != YesNoState.NO &&
                rejectConfirmSensorCR.value != YesNoState.NA &&
                rejectConfirmSensorCR.value != YesNoState.NO &&
                rejectConfirmSensorStopPosition.value.isNotBlank() &&
                rejectConfirmSensorStopPosition.value != "Out-feed Belt (Uncontrolled)" &&
                (rejectConfirmSensorTestMethod.value != "Other" ||
                        rejectConfirmSensorTestMethodOther.value.isNotBlank())

    setRejectConfirmSensorPvResult(
        if (passes) "Pass" else "Fail"
    )
}
fun evaluatePvResult(
    achievedSensitivityString: String,
    maxSensitivity: Double?,
    leadingState: YesNoState,
    middleState: YesNoState,
    trailingState: YesNoState,
    leadingPeak: String,
    middlePeak: String,
    trailingPeak: String,
    certificate: String
): String {

    // Handle "N/A" early
    if (achievedSensitivityString.equals("N/A", ignoreCase = true)) {
        return "N/A"
    }

    val achievedDouble = achievedSensitivityString.toDoubleOrNull()
        ?: return "Fail"   // invalid number means fail

    val retailerSensitivityAchieved =
        maxSensitivity?.let { achievedDouble <= it } ?: false

    // Passed?
    val allPassed =
        leadingState == YesNoState.YES &&
                middleState == YesNoState.YES &&
                trailingState == YesNoState.YES &&
                leadingPeak.isNotBlank() &&
                middlePeak.isNotBlank() &&
                trailingPeak.isNotBlank() &&
                certificate.isNotBlank() &&
                retailerSensitivityAchieved

    // Any failures?
    val anyFailed =
        leadingState == YesNoState.NO || leadingState == YesNoState.NA ||
                middleState == YesNoState.NO || middleState == YesNoState.NA ||
                trailingState == YesNoState.NO || trailingState == YesNoState.NA ||
                (leadingState == YesNoState.YES && leadingPeak.isBlank()) ||
                (middleState == YesNoState.YES && middlePeak.isBlank()) ||
                (trailingState == YesNoState.YES && trailingPeak.isBlank()) ||
                certificate.isBlank() ||
                !retailerSensitivityAchieved

    return when {
        allPassed -> "Pass"
        anyFailed -> "Fail"
        else -> "Fail"  // fallback
    }
}

// ---------------------------------------------------------
// Set all PV results to N/A
// ---------------------------------------------------------
fun CalibrationMetalDetectorConveyorViewModel.setAllPvResultsNa() {
    setFerrousTestPvResult("N/A")
    setDetectionSettingPvResult("N/A")
}