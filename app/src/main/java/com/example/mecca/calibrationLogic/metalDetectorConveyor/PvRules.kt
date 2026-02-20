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
// Detect Notify PV Logic
// ---------------------------------------------------------
fun CalibrationMetalDetectorConveyorViewModel.autoUpdateDetectNotificationTestPvResult() {

    val passes =
        detectNotificationTestPvResult.value.isNotBlank() && detectNotificationTestPvResult.value != "No Result"


    setDetectNotificationTestPvResult(
        if (passes) "Pass" else "Fail"
    )
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
// Infeed Sensor PV Logic
// ---------------------------------------------------------

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

// ---------------------------------------------------------
// Reject Confirm PV Logic
// ---------------------------------------------------------

fun CalibrationMetalDetectorConveyorViewModel.autoUpdateRejectConfirmSensorPvResult() {

    // If PV isn't required for this calibration, make it N/A and stop.
    if (!pvRequired.value) {
        setRejectConfirmSensorTestPvResult("N/A")
        return
    }

    if (rejectConfirmSensorFitted.value == YesNoState.NO) {
        setRejectConfirmSensorTestPvResult("Not Fitted")
        return
    }

    if (rejectConfirmSensorFitted.value == YesNoState.NA) {
        setRejectConfirmSensorTestPvResult("N/A")
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

    setRejectConfirmSensorTestPvResult(
        if (passes) "Pass" else "Fail"
    )
}

// ---------------------------------------------------------
// Bin Full Sensor PV Logic
// ---------------------------------------------------------

fun CalibrationMetalDetectorConveyorViewModel.autoUpdateBinFullSensorPvResult() {

    // If PV isn't required for this calibration, make it N/A and stop.
    if (!pvRequired.value) {
        setBinFullSensorTestPvResult("N/A")
        return
    }

    // If the sensor isn't fitted, PV result should be N/F
    if (binFullSensorFitted.value == YesNoState.NO) {
        setBinFullSensorTestPvResult("Not Fitted")
        return
    }

    // If it’s not applicable, PV result should be N/A
    if (binFullSensorFitted.value == YesNoState.NA) {
        setBinFullSensorTestPvResult("N/A")
        return
    }

    val passes =
        binFullSensorDetail.value.isNotBlank() &&
                binFullSensorTestMethod.value.isNotBlank() &&
                binFullSensorTestResult.value.isNotEmpty() &&
                "No Result" !in binFullSensorTestResult.value &&
                binFullSensorLatched.value == YesNoState.YES &&
                binFullSensorCR.value == YesNoState.YES &&
                (binFullSensorTestMethod.value != "Other" ||
                        binFullSensorTestMethodOther.value.isNotBlank())

    setBinFullSensorTestPvResult(
        if (passes) "Pass" else "Fail"
    )
}

// ---------------------------------------------------------
// Bin Full Sensor PV Logic
// ---------------------------------------------------------

fun CalibrationMetalDetectorConveyorViewModel.autoUpdateBackupSensorPvResult() {

    // If PV isn't required for this calibration, make it N/A and stop.
    if (!pvRequired.value) {
        setBackupSensorTestPvResult("N/A")
        return
    }

    // If the sensor isn't fitted, PV result should be N/F
    if (backupSensorFitted.value == YesNoState.NO) {
        setBackupSensorTestPvResult("Not Fitted")
        return
    }

    // If it’s not applicable, PV result should be N/A
    if (backupSensorFitted.value == YesNoState.NA) {
        setBackupSensorTestPvResult("N/A")
        return
    }

    val passes =
        backupSensorDetail.value.isNotBlank() &&
                backupSensorTestMethod.value.isNotBlank() &&
                backupSensorTestResult.value.isNotEmpty() &&
                "No Result" !in backupSensorTestResult.value &&
                backupSensorLatched.value == YesNoState.YES &&
                backupSensorCR.value == YesNoState.YES &&
                (backupSensorTestMethod.value != "Other" ||
                        backupSensorTestMethodOther.value.isNotBlank())

    setBackupSensorTestPvResult(
        if (passes) "Pass" else "Fail"
    )
}

// ---------------------------------------------------------
// Air Pressure Sensor PV Logic
// ---------------------------------------------------------


fun CalibrationMetalDetectorConveyorViewModel.autoUpdateAirPressureSensorPvResult() {

    // If PV isn't required for this calibration, make it N/A and stop.
    if (!pvRequired.value) {
        setAirPressureSensorTestPvResult("N/A")
        return
    }

    // If the sensor isn't fitted, PV result should be N/F
    if (airPressureSensorFitted.value == YesNoState.NO) {
        setAirPressureSensorTestPvResult("Not Fitted")
        return
    }

    // If it’s not applicable, PV result should be N/A
    if (airPressureSensorFitted.value == YesNoState.NA) {
        setAirPressureSensorTestPvResult("N/A")
        return
    }

    val passes =
        airPressureSensorDetail.value.isNotBlank() &&
                airPressureSensorTestMethod.value.isNotBlank() &&
                airPressureSensorTestResult.value.isNotEmpty() &&
                "No Result" !in airPressureSensorTestResult.value &&
                airPressureSensorLatched.value == YesNoState.YES &&
                airPressureSensorCR.value == YesNoState.YES &&
                (airPressureSensorTestMethod.value != "Other" ||
                        airPressureSensorTestMethodOther.value.isNotBlank())

    setAirPressureSensorTestPvResult(
        if (passes) "Pass" else "Fail"
    )
}

// ---------------------------------------------------------
// Air Pressure Sensor PV Logic
// ---------------------------------------------------------

fun CalibrationMetalDetectorConveyorViewModel.autoUpdatePackCheckSensorPvResult() {

    // If PV isn't required for this calibration, make it N/A and stop.
    if (!pvRequired.value) {
        setPackCheckSensorTestPvResult("N/A")
        return
    }

    // If the sensor isn't fitted, PV result should be N/F
    if (packCheckSensorFitted.value == YesNoState.NO) {
        setPackCheckSensorTestPvResult("Not Fitted")
        return
    }

    // If it’s not applicable, PV result should be N/A
    if (packCheckSensorFitted.value == YesNoState.NA) {
        setPackCheckSensorTestPvResult("N/A")
        return
    }

    val passes =
        packCheckSensorDetail.value.isNotBlank() &&
                packCheckSensorTestMethod.value.isNotBlank() &&
                packCheckSensorTestResult.value.isNotEmpty() &&
                "No Result" !in packCheckSensorTestResult.value &&
                packCheckSensorLatched.value == YesNoState.YES &&
                packCheckSensorCR.value == YesNoState.YES &&
                (packCheckSensorTestMethod.value != "Other" ||
                        packCheckSensorTestMethodOther.value.isNotBlank())

    setPackCheckSensorTestPvResult(
        if (passes) "Pass" else "Fail"
    )
}

// ---------------------------------------------------------
// Air Pressure Sensor PV Logic
// ---------------------------------------------------------


fun CalibrationMetalDetectorConveyorViewModel.autoUpdateSpeedSensorPvResult() {

    // If PV isn't required for this calibration, make it N/A and stop.
    if (!pvRequired.value) {
        setSpeedSensorTestPvResult("N/A")
        return
    }

    // Not fitted
    if (speedSensorFitted.value == YesNoState.NO) {
        setSpeedSensorTestPvResult("Not Fitted")
        return
    }

    // Not applicable
    if (speedSensorFitted.value == YesNoState.NA) {
        setSpeedSensorTestPvResult("N/A")
        return
    }

    val passes =
        speedSensorDetail.value.isNotBlank() &&
                speedSensorTestMethod.value.isNotBlank() &&
                speedSensorTestResult.value.isNotEmpty() &&
                "No Result" !in speedSensorTestResult.value &&
                speedSensorLatched.value == YesNoState.YES &&
                speedSensorCR.value == YesNoState.YES &&
                (speedSensorTestMethod.value != "Other" ||
                        speedSensorTestMethodOther.value.isNotBlank())

    setSpeedSensorTestPvResult(
        if (passes) "Pass" else "Fail"
    )
}

// ---------------------------------------------------------
// Air Pressure Sensor PV Logic
// ---------------------------------------------------------



fun CalibrationMetalDetectorConveyorViewModel.autoUpdateBinDoorMonitorPvResult() {

    if (!pvRequired.value) {
        setBinDoorMonitorTestPvResult("N/A")
        return
    }

    when (binDoorMonitorFitted.value) {
        YesNoState.NO -> {
            setBinDoorMonitorTestPvResult("Not Fitted")
            return
        }
        YesNoState.NA -> {
            setBinDoorMonitorTestPvResult("N/A")
            return
        }
        YesNoState.YES -> { /* continue */ }
        else -> {
            setBinDoorMonitorTestPvResult("Fail")
            return
        }
    }

    val passes =
        binDoorMonitorDetail.value.isNotBlank() &&
                binDoorStatusAsFound.value.isNotBlank() &&
                binDoorUnlockedIndication.value.isNotEmpty() &&
                "No Result" !in binDoorUnlockedIndication.value &&
                binDoorOpenIndication.value.isNotEmpty() &&
                "No Result" !in binDoorOpenIndication.value &&
                binDoorTimeoutTimer.value.isNotBlank() &&
                binDoorTimeoutResult.value.isNotEmpty() &&
                "No Result" !in binDoorTimeoutResult.value &&
                binDoorLatched.value == YesNoState.YES &&
                binDoorCR.value == YesNoState.YES

    setBinDoorMonitorTestPvResult(if (passes) "Pass" else "Fail")
}

private fun String.normalisedSize(): String =
    trim().replace(",", ".") // so "2,5" == "2.5"

fun CalibrationMetalDetectorConveyorViewModel.autoUpdateSmePvResult() {

    if (!pvRequired.value) {
        setSmeTestPvResult("N/A")
        return
    }

    when (operatorTestWitnessed.value) {
        YesNoState.NA -> {
            setSmeTestPvResult("N/A")
            return
        }
        YesNoState.YES -> { /* continue */ }
        else -> {
            setSmeTestPvResult("Fail")
            return
        }
    }

    // --- Engineer sizes (swap these to your actual fields) ---
    val engFerrous = sensitivityAsLeftFerrous.value.normalisedSize()       // OR sensitivityAsLeftFerrous.value
    val engNonFerrous = sensitivityAsLeftNonFerrous.value.normalisedSize() // OR sensitivityAsLeftNonFerrous.value
    val engStainless = sensitivityAsLeftStainless.value.normalisedSize()   // OR sensitivityAsLeftStainless.value


    // --- Operator sizes ---
    val opFerrous = operatorTestResultFerrous.value.normalisedSize()
    val opNonFerrous = operatorTestResultNonFerrous.value.normalisedSize()
    val opStainless = operatorTestResultStainless.value.normalisedSize()


    val allFieldsPresent =
        operatorName.value.isNotBlank() &&
                smeName.value.isNotBlank() &&
                operatorTestResultFerrous.value.isNotBlank() &&
                operatorTestResultNonFerrous.value.isNotBlank() &&
                operatorTestResultStainless.value.isNotBlank() &&
                operatorTestResultLargeMetal.value.isNotBlank() &&
                operatorTestResultCertNumberFerrous.value.isNotBlank() &&
                operatorTestResultCertNumberNonFerrous.value.isNotBlank() &&
                operatorTestResultCertNumberStainless.value.isNotBlank() &&
                operatorTestResultCertNumberLargeMetal.value.isNotBlank()

    val sizesMatch =
        opFerrous >= engFerrous &&
                opNonFerrous >= engNonFerrous &&
                opStainless >= engStainless

    setSmeTestPvResult(if (allFieldsPresent && sizesMatch) "Pass" else "Fail")
}




// ---------------------------------------------------------
// Set all PV results to N/A
// ---------------------------------------------------------
fun CalibrationMetalDetectorConveyorViewModel.setAllPvResultsNa() {
    setDetectionSettingPvResult("N/A")
    setFerrousTestPvResult("N/A")
    setNonFerrousTestPvResult("N/A")
    setStainlessTestPvResult("N/A")
    setSmeTestPvResult("N/A")
    setInfeedSensorTestPvResult("N/A")
    setRejectConfirmSensorTestPvResult("N/A")
    setBinFullSensorTestPvResult("N/A")
    setLargeMetalTestPvResult("N/A")
    setBackupSensorTestPvResult("N/A")
    setAirPressureSensorTestPvResult("N/A")
    setPackCheckSensorTestPvResult("N/A")
    setSpeedSensorTestPvResult("N/A")
    setBinDoorMonitorTestPvResult("N/A")
    setDetectNotificationTestPvResult("N/A")
}