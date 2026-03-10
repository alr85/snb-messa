package com.example.mecca.calibrationLogic.metalDetectorConveyor

import com.example.mecca.calibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import com.example.mecca.formModules.PvRule
import com.example.mecca.formModules.PvRuleStatus
import com.example.mecca.formModules.YesNoState

/**
 * ---------------------------------------------------------------
 *  Calibration PV Models & Rules
 * ---------------------------------------------------------------
 */

fun List<PvRule>.calculateOverallStatus(): String {
    if (isEmpty()) return "N/A"
    return when {
        any { it.status == PvRuleStatus.Fail } -> "Fail"
        any { it.status == PvRuleStatus.Warning || it.status == PvRuleStatus.Incomplete } -> "Warning"
        all { it.status == PvRuleStatus.NA } -> "N/A"
        all { it.status == PvRuleStatus.Pass || it.status == PvRuleStatus.NA } -> "Pass"
        else -> "N/A"
    }
}

private fun String.normalisedSize(): String = trim().replace(",", ".")

// ---------------------------------------------------------
// Detection Setting PV Logic
// ---------------------------------------------------------
fun CalibrationMetalDetectorConveyorViewModel.autoUpdateDetectionSettingPvResult() {
    if (!pvRequired.value) {
        setDetectionSettingPvResult("N/A")
        return
    }

    val value = sensitivityAccessRestriction.value.trim()

    when {
        value.equals("N/A", ignoreCase = true) ->
            setDetectionSettingPvResult("N/A")

        value.isBlank() ->
            setDetectionSettingPvResult("Fail")

        value == "None" -> 
            setDetectionSettingPvResult("Fail")

        else ->
            setDetectionSettingPvResult("Pass")
    }
}

// ---------------------------------------------------------
// Sensitivity PV Logic (Ferrous)
// ---------------------------------------------------------

fun CalibrationMetalDetectorConveyorViewModel.getFerrousPvRules(): List<PvRule> {
    if (!pvRequired.value) return emptyList()
    
    val achieved = sensitivityAsLeftFerrous.value.trim()
    if (achieved == "N/A") return listOf(PvRule("Test marked as N/A", PvRuleStatus.NA))

    val fVal = achieved.replace(",", ".").toDoubleOrNull()
    val cReq = sensitivityRequirementFerrous.value.replace(",", ".").toDoubleOrNull()
    
    val rules = mutableListOf<PvRule>()
    
    rules.add(PvRule(
        description = "Sensitivity must be ${cReq ?: 0.0}mm or better.",
        status = when {
            fVal == null || cReq == null -> PvRuleStatus.Incomplete
            fVal <= cReq -> PvRuleStatus.Pass
            else -> PvRuleStatus.Warning
        }
    ))
    
    rules.add(PvRule(
        description = "Certificate number must be recorded.",
        status = if (sampleCertificateNumberFerrous.value.isNotBlank()) PvRuleStatus.Pass else PvRuleStatus.Fail
    ))
    
    rules.add(createDetectionRule("Leading edge", detectRejectFerrousLeading.value, peakSignalFerrousLeading.value))
    if (isConveyor.value) {
        rules.add(createDetectionRule("Middle", detectRejectFerrousMiddle.value, peakSignalFerrousMiddle.value))
        rules.add(createDetectionRule("Trailing edge", detectRejectFerrousTrailing.value, peakSignalFerrousTrailing.value))
    }
    
    return rules
}

fun CalibrationMetalDetectorConveyorViewModel.autoUpdateFerrousPvResult() {
    if (!pvRequired.value) {
        setFerrousTestPvResult("N/A")
        return
    }
    setFerrousTestPvResult(getFerrousPvRules().calculateOverallStatus())
}

// ---------------------------------------------------------
// Sensitivity PV Logic (Non-Ferrous)
// ---------------------------------------------------------

fun CalibrationMetalDetectorConveyorViewModel.getNonFerrousPvRules(): List<PvRule> {
    if (!pvRequired.value) return emptyList()
    
    val achieved = sensitivityAsLeftNonFerrous.value.trim()
    if (achieved == "N/A") return listOf(PvRule("Test marked as N/A", PvRuleStatus.NA))

    val fVal = achieved.replace(",", ".").toDoubleOrNull()
    val cReq = sensitivityRequirementNonFerrous.value.replace(",", ".").toDoubleOrNull()
    
    val rules = mutableListOf<PvRule>()
    
    rules.add(PvRule(
        description = "Sensitivity must be ${cReq ?: 0.0}mm or better.",
        status = when {
            fVal == null || cReq == null -> PvRuleStatus.Incomplete
            fVal <= cReq -> PvRuleStatus.Pass
            else -> PvRuleStatus.Warning
        }
    ))
    
    rules.add(PvRule(
        description = "Certificate number must be recorded.",
        status = if (sampleCertificateNumberNonFerrous.value.isNotBlank()) PvRuleStatus.Pass else PvRuleStatus.Fail
    ))
    
    rules.add(createDetectionRule("Leading edge", detectRejectNonFerrousLeading.value, peakSignalNonFerrousLeading.value))
    if (isConveyor.value) {
        rules.add(createDetectionRule("Middle", detectRejectNonFerrousMiddle.value, peakSignalNonFerrousMiddle.value))
        rules.add(createDetectionRule("Trailing edge", detectRejectNonFerrousTrailing.value, peakSignalNonFerrousTrailing.value))
    }
    
    return rules
}

fun CalibrationMetalDetectorConveyorViewModel.autoUpdateNonFerrousPvResult() {
    if (!pvRequired.value) {
        setNonFerrousTestPvResult("N/A")
        return
    }
    setNonFerrousTestPvResult(getNonFerrousPvRules().calculateOverallStatus())
}

// ---------------------------------------------------------
// Sensitivity PV Logic (Stainless)
// ---------------------------------------------------------

fun CalibrationMetalDetectorConveyorViewModel.getStainlessPvRules(): List<PvRule> {
    if (!pvRequired.value) return emptyList()
    
    val achieved = sensitivityAsLeftStainless.value.trim()
    if (achieved == "N/A") return listOf(PvRule("Test marked as N/A", PvRuleStatus.NA))

    val fVal = achieved.replace(",", ".").toDoubleOrNull()
    val cReq = sensitivityRequirementStainless.value.replace(",", ".").toDoubleOrNull()
    
    val rules = mutableListOf<PvRule>()
    
    rules.add(PvRule(
        description = "Sensitivity must be ${cReq ?: 0.0}mm or better.",
        status = when {
            fVal == null || cReq == null -> PvRuleStatus.Incomplete
            fVal <= cReq -> PvRuleStatus.Pass
            else -> PvRuleStatus.Warning
        }
    ))
    
    rules.add(PvRule(
        description = "Certificate number must be recorded.",
        status = if (sampleCertificateNumberStainless.value.isNotBlank()) PvRuleStatus.Pass else PvRuleStatus.Fail
    ))
    
    rules.add(createDetectionRule("Leading edge", detectRejectStainlessLeading.value, peakSignalStainlessLeading.value))
    if (isConveyor.value) {
        rules.add(createDetectionRule("Middle", detectRejectStainlessMiddle.value, peakSignalStainlessMiddle.value))
        rules.add(createDetectionRule("Trailing edge", detectRejectStainlessTrailing.value, peakSignalStainlessTrailing.value))
    }
    
    return rules
}

fun CalibrationMetalDetectorConveyorViewModel.autoUpdateStainlessPvResult() {
    if (!pvRequired.value) {
        setStainlessTestPvResult("N/A")
        return
    }
    setStainlessTestPvResult(getStainlessPvRules().calculateOverallStatus())
}

private fun createDetectionRule(label: String, state: YesNoState, signal: String): PvRule {
    return PvRule(
        description = "$label test must pass with recorded signal.",
        status = when {
            state == YesNoState.NA -> PvRuleStatus.NA
            state == YesNoState.YES && signal.isNotBlank() -> PvRuleStatus.Pass
            state == YesNoState.NO -> PvRuleStatus.Fail
            else -> PvRuleStatus.Incomplete
        }
    )
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
    val passes = infeedSensorDetail.value.isNotBlank() &&
                infeedSensorTestMethod.value.isNotBlank() &&
                infeedSensorTestResult.value.isNotEmpty() &&
                "No Result" !in infeedSensorTestResult.value &&
                infeedSensorLatched.value == YesNoState.YES &&
                infeedSensorCR.value == YesNoState.YES &&
                (infeedSensorTestMethod.value != "Other" || infeedSensorTestMethodOther.value.isNotBlank())
    setInfeedSensorTestPvResult(if (passes) "Pass" else "Fail")
}

// ---------------------------------------------------------
// Reject Confirm PV Logic
// ---------------------------------------------------------

fun CalibrationMetalDetectorConveyorViewModel.autoUpdateRejectConfirmSensorPvResult() {
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
    val passes = rejectConfirmSensorDetail.value.isNotBlank() &&
                rejectConfirmSensorTestMethod.value.isNotBlank() &&
                rejectConfirmSensorTestResult.value.isNotEmpty() &&
                "No Result" !in rejectConfirmSensorTestResult.value &&
                rejectConfirmSensorLatched.value != YesNoState.NA &&
                rejectConfirmSensorLatched.value != YesNoState.NO &&
                rejectConfirmSensorCR.value != YesNoState.NA &&
                rejectConfirmSensorCR.value != YesNoState.NO &&
                rejectConfirmSensorStopPosition.value.isNotBlank() &&
                rejectConfirmSensorStopPosition.value != "Out-feed Belt (Uncontrolled)" &&
                (rejectConfirmSensorTestMethod.value != "Other" || rejectConfirmSensorTestMethodOther.value.isNotBlank())
    setRejectConfirmSensorTestPvResult(if (passes) "Pass" else "Fail")
}

// ---------------------------------------------------------
// Bin Full Sensor PV Logic
// ---------------------------------------------------------

fun CalibrationMetalDetectorConveyorViewModel.autoUpdateBinFullSensorPvResult() {
    if (!pvRequired.value) {
        setBinFullSensorTestPvResult("N/A")
        return
    }
    if (binFullSensorFitted.value == YesNoState.NO) {
        setBinFullSensorTestPvResult("Not Fitted")
        return
    }
    if (binFullSensorFitted.value == YesNoState.NA) {
        setBinFullSensorTestPvResult("N/A")
        return
    }
    val passes = binFullSensorDetail.value.isNotBlank() &&
                binFullSensorTestMethod.value.isNotBlank() &&
                binFullSensorTestResult.value.isNotEmpty() &&
                "No Result" !in binFullSensorTestResult.value &&
                binFullSensorLatched.value == YesNoState.YES &&
                binFullSensorCR.value == YesNoState.YES &&
                (binFullSensorTestMethod.value != "Other" || binFullSensorTestMethodOther.value.isNotBlank())
    setBinFullSensorTestPvResult(if (passes) "Pass" else "Fail")
}

// ---------------------------------------------------------
// Backup Sensor PV Logic
// ---------------------------------------------------------

fun CalibrationMetalDetectorConveyorViewModel.autoUpdateBackupSensorPvResult() {
    if (!pvRequired.value) {
        setBackupSensorTestPvResult("N/A")
        return
    }
    if (backupSensorFitted.value == YesNoState.NO) {
        setBackupSensorTestPvResult("Not Fitted")
        return
    }
    if (backupSensorFitted.value == YesNoState.NA) {
        setBackupSensorTestPvResult("N/A")
        return
    }
    val passes = backupSensorDetail.value.isNotBlank() &&
                backupSensorTestMethod.value.isNotBlank() &&
                backupSensorTestResult.value.isNotEmpty() &&
                "No Result" !in backupSensorTestResult.value &&
                backupSensorLatched.value == YesNoState.YES &&
                backupSensorCR.value == YesNoState.YES &&
                (backupSensorTestMethod.value != "Other" || backupSensorTestMethodOther.value.isNotBlank())
    setBackupSensorTestPvResult(if (passes) "Pass" else "Fail")
}

// ---------------------------------------------------------
// Air Pressure Sensor PV Logic
// ---------------------------------------------------------

fun CalibrationMetalDetectorConveyorViewModel.autoUpdateAirPressureSensorPvResult() {
    if (!pvRequired.value) {
        setAirPressureSensorTestPvResult("N/A")
        return
    }
    if (airPressureSensorFitted.value == YesNoState.NO) {
        setAirPressureSensorTestPvResult("Not Fitted")
        return
    }
    if (airPressureSensorFitted.value == YesNoState.NA) {
        setAirPressureSensorTestPvResult("N/A")
        return
    }
    val passes = airPressureSensorDetail.value.isNotBlank() &&
                airPressureSensorTestMethod.value.isNotBlank() &&
                airPressureSensorTestResult.value.isNotEmpty() &&
                "No Result" !in airPressureSensorTestResult.value &&
                airPressureSensorLatched.value == YesNoState.YES &&
                airPressureSensorCR.value == YesNoState.YES &&
                (airPressureSensorTestMethod.value != "Other" || airPressureSensorTestMethodOther.value.isNotBlank())
    setAirPressureSensorTestPvResult(if (passes) "Pass" else "Fail")
}

// ---------------------------------------------------------
// Pack Check Sensor PV Logic
// ---------------------------------------------------------

fun CalibrationMetalDetectorConveyorViewModel.autoUpdatePackCheckSensorPvResult() {
    if (!pvRequired.value) {
        setPackCheckSensorTestPvResult("N/A")
        return
    }
    if (packCheckSensorFitted.value == YesNoState.NO) {
        setPackCheckSensorTestPvResult("Not Fitted")
        return
    }
    if (packCheckSensorFitted.value == YesNoState.NA) {
        setPackCheckSensorTestPvResult("N/A")
        return
    }
    val passes = packCheckSensorDetail.value.isNotBlank() &&
                packCheckSensorTestMethod.value.isNotBlank() &&
                packCheckSensorTestResult.value.isNotEmpty() &&
                "No Result" !in packCheckSensorTestResult.value &&
                packCheckSensorLatched.value == YesNoState.YES &&
                packCheckSensorCR.value == YesNoState.YES &&
                (packCheckSensorTestMethod.value != "Other" || packCheckSensorTestMethodOther.value.isNotBlank())
    setPackCheckSensorTestPvResult(if (passes) "Pass" else "Fail")
}

// ---------------------------------------------------------
// Speed Sensor PV Logic
// ---------------------------------------------------------

fun CalibrationMetalDetectorConveyorViewModel.autoUpdateSpeedSensorPvResult() {
    if (!pvRequired.value) {
        setSpeedSensorTestPvResult("N/A")
        return
    }
    if (speedSensorFitted.value == YesNoState.NO) {
        setSpeedSensorTestPvResult("Not Fitted")
        return
    }
    if (speedSensorFitted.value == YesNoState.NA) {
        setSpeedSensorTestPvResult("N/A")
        return
    }
    val passes = speedSensorDetail.value.isNotBlank() &&
                speedSensorTestMethod.value.isNotBlank() &&
                speedSensorTestResult.value.isNotEmpty() &&
                "No Result" !in speedSensorTestResult.value &&
                speedSensorLatched.value == YesNoState.YES &&
                speedSensorCR.value == YesNoState.YES &&
                (speedSensorTestMethod.value != "Other" || speedSensorTestMethodOther.value.isNotBlank())
    setSpeedSensorTestPvResult(if (passes) "Pass" else "Fail")
}

// ---------------------------------------------------------
// Bin Door Monitor PV Logic
// ---------------------------------------------------------

fun CalibrationMetalDetectorConveyorViewModel.autoUpdateBinDoorMonitorPvResult() {
    if (!pvRequired.value) {
        setBinDoorMonitorTestPvResult("N/A")
        return
    }
    when (binDoorMonitorFitted.value) {
        YesNoState.NO -> { setBinDoorMonitorTestPvResult("Not Fitted"); return }
        YesNoState.NA -> { setBinDoorMonitorTestPvResult("N/A"); return }
        YesNoState.YES -> { /* continue */ }
        else -> { setBinDoorMonitorTestPvResult("Fail"); return }
    }
    val passes = binDoorMonitorDetail.value.isNotBlank() &&
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

// ---------------------------------------------------------
// SME PV Logic
// ---------------------------------------------------------

fun CalibrationMetalDetectorConveyorViewModel.getSmePvRules(): List<PvRule> {
    if (!pvRequired.value) return emptyList()
    
    val witnessed = operatorTestWitnessed.value
    if (witnessed == YesNoState.NA) return listOf(PvRule("Test marked as N/A", PvRuleStatus.NA))

    val infeed = operatorTestWitnessedInfeed.value
    val rejectConfirm = operatorTestWitnessedRejectConfirm.value
    val binFull = operatorTestWitnessedBinFull.value
    val binDoor = operatorTestWitnessedBinDoor.value
    val airFail = operatorTestWitnessedAirFail.value




    val rules = mutableListOf<PvRule>()
    
    // 1. Witnessed and Operator Name
    rules.add(PvRule(
        description = "Operator test must be witnessed and the name of the operator recorded.",
        status = if (witnessed == YesNoState.YES && operatorName.value.isNotBlank()) PvRuleStatus.Pass else PvRuleStatus.Fail
    ))
    
    // 2. Engineer Sensitivities (for comparison)
    val engF = sensitivityAsLeftFerrous.value.normalisedSize().toDoubleOrNull() ?: 99.0
    val engNF = sensitivityAsLeftNonFerrous.value.normalisedSize().toDoubleOrNull() ?: 99.0
    val engS = sensitivityAsLeftStainless.value.normalisedSize().toDoubleOrNull() ?: 99.0
    
    // 3. Ferrous Test Comparison
    val opF = operatorTestResultFerrous.value.normalisedSize().toDoubleOrNull() ?: 99.0
    rules.add(PvRule(
        description = "Operator Ferrous test size must be equal to or smaller than engineer 'As Left' (${engF}mm) and have a cert number.",
        status = if (opF <= engF && operatorTestResultCertNumberFerrous.value.isNotBlank()) PvRuleStatus.Pass else PvRuleStatus.Fail
    ))
    
    // 4. Non-Ferrous Test Comparison
    val opNF = operatorTestResultNonFerrous.value.normalisedSize().toDoubleOrNull() ?: 99.0
    rules.add(PvRule(
        description = "Operator Non-Ferrous test size must be equal to or smaller than engineer 'As Left' (${engNF}mm) and have a cert number.",
        status = if (opNF <= engNF && operatorTestResultCertNumberNonFerrous.value.isNotBlank()) PvRuleStatus.Pass else PvRuleStatus.Fail
    ))
    
    // 5. Stainless Test Comparison
    val opS = operatorTestResultStainless.value.normalisedSize().toDoubleOrNull() ?: 99.0
    rules.add(PvRule(
        description = "Operator Stainless test size must be equal to or smaller than engineer 'As Left' (${engS}mm) and have a cert number.",
        status = if (opS <= engS && operatorTestResultCertNumberStainless.value.isNotBlank()) PvRuleStatus.Pass else PvRuleStatus.Fail
    ))
    
    // 6. Large Metal (20mm Ferrous)
    val opLM = operatorTestResultLargeMetal.value.normalisedSize().toDoubleOrNull() ?: 99.0
    rules.add(PvRule(
        description = "Large metal test must use a 20mm ferrous sample and have a cert number.",
        status = if (opLM <= 20.0 && operatorTestResultCertNumberLargeMetal.value.isNotBlank()) PvRuleStatus.Pass else PvRuleStatus.Fail
    ))
    
    // 7. SME Name
    rules.add(PvRule(
        description = "Site must have an SME on site, and their name must be recorded.",
        status = if (smeName.value.isNotBlank()) PvRuleStatus.Pass else PvRuleStatus.Fail
    ))

    //8. Infeed Test Witnesses

    rules.add(PvRule(
        description = "Infeed Sensor test must be witnessed.",
        status = if (infeed == YesNoState.YES) PvRuleStatus.Pass else PvRuleStatus.Fail
    ))

    //8. Reject Confirm Test Witnesses

    rules.add(PvRule(
        description = "Reject Confirm test must be witnessed.",
        status = if (rejectConfirm == YesNoState.YES) PvRuleStatus.Pass else PvRuleStatus.Fail
    ))

    //9. Bin Full Test Witnesses

    rules.add(PvRule(
        description = "Bin Full test must be witnessed.",
        status = if (binFull == YesNoState.YES) PvRuleStatus.Pass else PvRuleStatus.Fail
    ))

    //10. Bin Door Test Witnesses

    rules.add(PvRule(
        description = "Bin Door test must be witnessed.",
        status = if (binDoor == YesNoState.YES) PvRuleStatus.Pass else PvRuleStatus.Fail
    ))

    //11. Air Fail Test Witnesses

    rules.add(PvRule(
        description = "Air Fail test must be witnessed.",
        status = if (airFail == YesNoState.YES) PvRuleStatus.Pass else PvRuleStatus.Fail
    ))

    return rules

}

fun CalibrationMetalDetectorConveyorViewModel.autoUpdateSmePvResult() {
    if (!pvRequired.value) {
        setSmeTestPvResult("N/A")
        return
    }
    setSmeTestPvResult(getSmePvRules().calculateOverallStatus())
}

// ---------------------------------------------------------
// Detect Notify PV Logic
// ---------------------------------------------------------

fun CalibrationMetalDetectorConveyorViewModel.autoUpdateDetectNotificationTestPvResult() {
    if (!pvRequired.value) {
        setDetectNotificationTestPvResult("N/A")
        return
    }
    val results = detectNotificationResult.value
    val passes = results.isNotEmpty() && "No Result" !in results
    setDetectNotificationTestPvResult(if (passes) "Pass" else "Fail")
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
