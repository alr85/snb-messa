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
        any { it.status == PvRuleStatus.Incomplete || it.status == PvRuleStatus.Warning } -> "Warning"
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
    if (achieved == "N/A") return listOf(PvRule("Test marked as N/A", PvRuleStatus.NA, "FERROUS_NA"))

    val fVal = achieved.replace(",", ".").toDoubleOrNull()
    val cReq = sensitivityRequirementFerrous.value.replace(",", ".").toDoubleOrNull()
    
    val rules = mutableListOf<PvRule>()
    
    rules.add(PvRule(
        description = "Sensitivity must be ${cReq ?: 0.0}mm or better.",
        status = when {
            fVal == null || cReq == null -> PvRuleStatus.Incomplete
            fVal <= cReq -> PvRuleStatus.Pass
            else -> PvRuleStatus.Warning
        },
        ruleId = "FERROUS_SENSITIVITY"
    ))
    
    rules.add(PvRule(
        description = "Certificate number must be recorded.",
        status = if (sampleCertificateNumberFerrous.value.isNotBlank()) PvRuleStatus.Pass else PvRuleStatus.Fail,
        ruleId = "FERROUS_CERT"
    ))
    
    rules.add(createDetectionRule("INFEED_LEADING", "Leading edge", detectRejectFerrousLeading.value, peakSignalFerrousLeading.value))
    if (isConveyor.value) {
        rules.add(createDetectionRule("INFEED_MIDDLE", "Middle", detectRejectFerrousMiddle.value, peakSignalFerrousMiddle.value))
        rules.add(createDetectionRule("INFEED_TRAILING", "Trailing edge", detectRejectFerrousTrailing.value, peakSignalFerrousTrailing.value))
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
    if (achieved == "N/A") return listOf(PvRule("Test marked as N/A", PvRuleStatus.NA, "NONFERROUS_NA"))

    val fVal = achieved.replace(",", ".").toDoubleOrNull()
    val cReq = sensitivityRequirementNonFerrous.value.replace(",", ".").toDoubleOrNull()
    
    val rules = mutableListOf<PvRule>()
    
    rules.add(PvRule(
        description = "Sensitivity must be ${cReq ?: 0.0}mm or better.",
        status = when {
            fVal == null || cReq == null -> PvRuleStatus.Incomplete
            fVal <= cReq -> PvRuleStatus.Pass
            else -> PvRuleStatus.Warning
        },
        ruleId = "NONFERROUS_SENSITIVITY"
    ))
    
    rules.add(PvRule(
        description = "Certificate number must be recorded.",
        status = if (sampleCertificateNumberNonFerrous.value.isNotBlank()) PvRuleStatus.Pass else PvRuleStatus.Fail,
        ruleId = "NONFERROUS_CERT"
    ))
    
    rules.add(createDetectionRule("NONFERROUS_LEADING", "Leading edge", detectRejectNonFerrousLeading.value, peakSignalNonFerrousLeading.value))
    if (isConveyor.value) {
        rules.add(createDetectionRule("NONFERROUS_MIDDLE", "Middle", detectRejectNonFerrousMiddle.value, peakSignalNonFerrousMiddle.value))
        rules.add(createDetectionRule("NONFERROUS_TRAILING", "Trailing edge", detectRejectNonFerrousTrailing.value, peakSignalNonFerrousTrailing.value))
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
    if (achieved == "N/A") return listOf(PvRule("Test marked as N/A", PvRuleStatus.NA, "STAINLESS_NA"))

    val fVal = achieved.replace(",", ".").toDoubleOrNull()
    val cReq = sensitivityRequirementStainless.value.replace(",", ".").toDoubleOrNull()
    
    val rules = mutableListOf<PvRule>()
    
    rules.add(PvRule(
        description = "Sensitivity must be ${cReq ?: 0.0}mm or better.",
        status = when {
            fVal == null || cReq == null -> PvRuleStatus.Incomplete
            fVal <= cReq -> PvRuleStatus.Pass
            else -> PvRuleStatus.Warning
        },
        ruleId = "STAINLESS_SENSITIVITY"
    ))
    
    rules.add(PvRule(
        description = "Certificate number must be recorded.",
        status = if (sampleCertificateNumberStainless.value.isNotBlank()) PvRuleStatus.Pass else PvRuleStatus.Fail,
        ruleId = "STAINLESS_CERT"
    ))
    
    rules.add(createDetectionRule("STAINLESS_LEADING", "Leading edge", detectRejectStainlessLeading.value, peakSignalStainlessLeading.value))
    if (isConveyor.value) {
        rules.add(createDetectionRule("STAINLESS_MIDDLE", "Middle", detectRejectStainlessMiddle.value, peakSignalStainlessMiddle.value))
        rules.add(createDetectionRule("STAINLESS_TRAILING", "Trailing edge", detectRejectStainlessTrailing.value, peakSignalStainlessTrailing.value))
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

private fun createDetectionRule(ruleId: String, label: String, state: YesNoState, signal: String): PvRule {
    return PvRule(
        description = "$label test must pass with recorded signal.",
        status = when {
            state == YesNoState.NA -> PvRuleStatus.NA
            state == YesNoState.YES && signal.isNotBlank() -> PvRuleStatus.Pass
            state == YesNoState.NO -> PvRuleStatus.Fail
            else -> PvRuleStatus.Incomplete
        },
        ruleId = ruleId
    )
}

// ---------------------------------------------------------
// General Failsafe Rule Creator
// ---------------------------------------------------------
private fun getFailsafeRules(
    prefix: String,
    fitted: YesNoState,
    detail: String,
    method: String,
    results: List<String>,
    latched: YesNoState,
    cr: YesNoState
): List<PvRule> {
    val rules = mutableListOf<PvRule>()

    // 1. Sensor Fitted
    rules.add(PvRule(
        description = "Sensor must be fitted to meet retailer safety standards.",
        status = when(fitted) {
            YesNoState.YES -> PvRuleStatus.Pass
            YesNoState.NO -> PvRuleStatus.Fail
            YesNoState.NA -> PvRuleStatus.NA
            else -> PvRuleStatus.Incomplete
        },
        ruleId = "${prefix}_FITTED"
    ))

    if (fitted == YesNoState.NO || fitted == YesNoState.NA) {
        val st = if (fitted == YesNoState.NA) PvRuleStatus.NA else PvRuleStatus.NA
        rules.add(PvRule("Detail entered.", st, "${prefix}_DETAIL"))
        rules.add(PvRule("Test method selected.", st, "${prefix}_METHOD"))
        rules.add(PvRule("Test result recorded.", st, "${prefix}_RESULT"))
        rules.add(PvRule("Fault condition latched.", st, "${prefix}_LATCHED"))
        rules.add(PvRule("Controlled restart required.", st, "${prefix}_CR"))
        return rules
    }

    // 2. Detail
    rules.add(PvRule(
        description = "A description of the sensor location/purpose must be entered.",
        status = if (detail.isNotBlank() && detail != "N/A") PvRuleStatus.Pass else PvRuleStatus.Incomplete,
        ruleId = "${prefix}_DETAIL"
    ))
    
    // 3. Test method
    rules.add(PvRule(
        description = "A valid test method must be selected.",
        status = if (method.isNotBlank() && method != "N/A") PvRuleStatus.Pass else PvRuleStatus.Incomplete,
        ruleId = "${prefix}_METHOD"
    ))
    
    // 4. Test result
    rules.add(PvRule(
        description = "The belt must stop, and an audible or visual notification must be recorded.",
        status = if (results.isNotEmpty() &&
            "No Result" !in results &&
            results.any { it.contains("Notification", ignoreCase = true) } &&
            "Belt Stops" in results) PvRuleStatus.Pass else PvRuleStatus.Fail,
        ruleId = "${prefix}_RESULT"
    ))
    
    // 5. Fault latched
    rules.add(PvRule(
        description = "The failsafe condition must be latched (Stop/Alarm).",
        status = when(latched) {
            YesNoState.YES -> PvRuleStatus.Pass
            YesNoState.NO -> PvRuleStatus.Fail
            else -> PvRuleStatus.Incomplete
        },
        ruleId = "${prefix}_LATCHED"
    ))
    
    // 6. Controlled restart
    rules.add(PvRule(
        description = "A manual reset/restart must be required to resume operation.",
        status = when(cr) {
            YesNoState.YES -> PvRuleStatus.Pass
            YesNoState.NO -> PvRuleStatus.Fail
            else -> PvRuleStatus.Incomplete
        },
        ruleId = "${prefix}_CR"
    ))

    return rules
}

// ---------------------------------------------------------
// Infeed Sensor PV Logic
// ---------------------------------------------------------
fun CalibrationMetalDetectorConveyorViewModel.getInfeedSensorPvRules(): List<PvRule> {
    return getFailsafeRules(
        "INFEED",
        infeedSensorFitted.value,
        infeedSensorDetail.value,
        infeedSensorTestMethod.value,
        infeedSensorTestResult.value,
        infeedSensorLatched.value,
        infeedSensorCR.value
    )
}

fun CalibrationMetalDetectorConveyorViewModel.autoUpdateInfeedSensorPvResult() {
    if (!pvRequired.value) { setInfeedSensorTestPvResult("N/A"); return }
    setInfeedSensorTestPvResult(getInfeedSensorPvRules().calculateOverallStatus())
}

// ---------------------------------------------------------
// Reject Confirm PV Logic
// ---------------------------------------------------------
fun CalibrationMetalDetectorConveyorViewModel.getRejectConfirmSensorPvRules(): List<PvRule> {
    val rules = getFailsafeRules(
        "REJECT_CONFIRM",
        rejectConfirmSensorFitted.value,
        rejectConfirmSensorDetail.value,
        rejectConfirmSensorTestMethod.value,
        rejectConfirmSensorTestResult.value,
        rejectConfirmSensorLatched.value,
        rejectConfirmSensorCR.value
    ).toMutableList()

    if (rejectConfirmSensorFitted.value == YesNoState.YES) {
        rules.add(PvRule(
            description = "The stop position must be controlled (not uncontrolled belt).",
            status = if (rejectConfirmSensorStopPosition.value.isNotBlank() && 
                        rejectConfirmSensorStopPosition.value != "Out-feed Belt (Uncontrolled)") 
                        PvRuleStatus.Pass else PvRuleStatus.Fail,
            ruleId = "REJECT_CONFIRM_STOP_POS"
        ))
    }
    return rules
}

fun CalibrationMetalDetectorConveyorViewModel.autoUpdateRejectConfirmSensorPvResult() {
    if (!pvRequired.value) { setRejectConfirmSensorTestPvResult("N/A"); return }
    setRejectConfirmSensorTestPvResult(getRejectConfirmSensorPvRules().calculateOverallStatus())
}

// ---------------------------------------------------------
// Bin Full Sensor PV Logic
// ---------------------------------------------------------
fun CalibrationMetalDetectorConveyorViewModel.getBinFullSensorPvRules(): List<PvRule> {
    return getFailsafeRules(
        "BIN_FULL",
        binFullSensorFitted.value,
        binFullSensorDetail.value,
        binFullSensorTestMethod.value,
        binFullSensorTestResult.value,
        binFullSensorLatched.value,
        binFullSensorCR.value
    )
}

fun CalibrationMetalDetectorConveyorViewModel.autoUpdateBinFullSensorPvResult() {
    if (!pvRequired.value) { setBinFullSensorTestPvResult("N/A"); return }
    setBinFullSensorTestPvResult(getBinFullSensorPvRules().calculateOverallStatus())
}

// ---------------------------------------------------------
// Backup Sensor PV Logic
// ---------------------------------------------------------
fun CalibrationMetalDetectorConveyorViewModel.getBackupSensorPvRules(): List<PvRule> {
    return getFailsafeRules(
        "BACKUP",
        backupSensorFitted.value,
        backupSensorDetail.value,
        backupSensorTestMethod.value,
        backupSensorTestResult.value,
        backupSensorLatched.value,
        backupSensorCR.value
    )
}

fun CalibrationMetalDetectorConveyorViewModel.autoUpdateBackupSensorPvResult() {
    if (!pvRequired.value) { setBackupSensorTestPvResult("N/A"); return }
    setBackupSensorTestPvResult(getBackupSensorPvRules().calculateOverallStatus())
}

// ---------------------------------------------------------
// Air Pressure Sensor PV Logic
// ---------------------------------------------------------
fun CalibrationMetalDetectorConveyorViewModel.getAirPressureSensorPvRules(): List<PvRule> {
    return getFailsafeRules(
        "AIR",
        airPressureSensorFitted.value,
        airPressureSensorDetail.value,
        airPressureSensorTestMethod.value,
        airPressureSensorTestResult.value,
        airPressureSensorLatched.value,
        airPressureSensorCR.value
    )
}

fun CalibrationMetalDetectorConveyorViewModel.autoUpdateAirPressureSensorPvResult() {
    if (!pvRequired.value) { setAirPressureSensorTestPvResult("N/A"); return }
    setAirPressureSensorTestPvResult(getAirPressureSensorPvRules().calculateOverallStatus())
}

// ---------------------------------------------------------
// Pack Check Sensor PV Logic
// ---------------------------------------------------------
fun CalibrationMetalDetectorConveyorViewModel.getPackCheckSensorPvRules(): List<PvRule> {
    return getFailsafeRules(
        "PACK",
        packCheckSensorFitted.value,
        packCheckSensorDetail.value,
        packCheckSensorTestMethod.value,
        packCheckSensorTestResult.value,
        packCheckSensorLatched.value,
        packCheckSensorCR.value
    )
}

fun CalibrationMetalDetectorConveyorViewModel.autoUpdatePackCheckSensorPvResult() {
    if (!pvRequired.value) { setPackCheckSensorTestPvResult("N/A"); return }
    setPackCheckSensorTestPvResult(getPackCheckSensorPvRules().calculateOverallStatus())
}

// ---------------------------------------------------------
// Speed Sensor PV Logic
// ---------------------------------------------------------
fun CalibrationMetalDetectorConveyorViewModel.getSpeedSensorPvRules(): List<PvRule> {
    return getFailsafeRules(
        "SPEED",
        speedSensorFitted.value,
        speedSensorDetail.value,
        speedSensorTestMethod.value,
        speedSensorTestResult.value,
        speedSensorLatched.value,
        speedSensorCR.value
    )
}

fun CalibrationMetalDetectorConveyorViewModel.autoUpdateSpeedSensorPvResult() {
    if (!pvRequired.value) { setSpeedSensorTestPvResult("N/A"); return }
    setSpeedSensorTestPvResult(getSpeedSensorPvRules().calculateOverallStatus())
}

// ---------------------------------------------------------
// Bin Door Monitor PV Logic
// ---------------------------------------------------------
fun CalibrationMetalDetectorConveyorViewModel.getBinDoorMonitorPvRules(): List<PvRule> {
    val prefix = "BIN_DOOR"
    if (binDoorMonitorFitted.value == YesNoState.NO || binDoorMonitorFitted.value == YesNoState.NA) {
        val st = if (binDoorMonitorFitted.value == YesNoState.NO) PvRuleStatus.Fail else PvRuleStatus.NA
        return listOf(PvRule(description = "Sensor fitted.", status = st, ruleId = "${prefix}_FITTED"))
    }

    val rules = mutableListOf<PvRule>()
    rules.add(PvRule(description = "Sensor fitted.", status = PvRuleStatus.Pass, ruleId = "${prefix}_FITTED"))
    
    rules.add(PvRule(
        description = "A description of the bin door monitor location must be entered.",
        status = if (binDoorMonitorDetail.value.isNotBlank() && binDoorMonitorDetail.value != "N/A") PvRuleStatus.Pass else PvRuleStatus.Incomplete,
        ruleId = "${prefix}_DETAIL"
    ))
    
    rules.add(PvRule(
        description = "Initial door status (As Found) must be recorded.",
        status = if (binDoorStatusAsFound.value.isNotBlank()) PvRuleStatus.Pass else PvRuleStatus.Incomplete,
        ruleId = "${prefix}_FOUND"
    ))

    rules.add(PvRule(
        description = "Unlocked and Open indication results must be recorded.",
        status = if (binDoorUnlockedIndication.value.isNotEmpty() && "No Result" !in binDoorUnlockedIndication.value &&
                    binDoorOpenIndication.value.isNotEmpty() && "No Result" !in binDoorOpenIndication.value) 
                    PvRuleStatus.Pass else PvRuleStatus.Incomplete,
        ruleId = "${prefix}_INDICATION"
    ))

    rules.add(PvRule(
        description = "Timeout timer and result must be recorded.",
        status = if (binDoorTimeoutTimer.value.isNotBlank() && binDoorTimeoutResult.value.isNotEmpty() && "No Result" !in binDoorTimeoutResult.value) 
                    PvRuleStatus.Pass else PvRuleStatus.Incomplete,
        ruleId = "${prefix}_TIMEOUT"
    ))

    rules.add(PvRule(
        description = "Fault must be latched and require a controlled restart.",
        status = if (binDoorLatched.value == YesNoState.YES && binDoorCR.value == YesNoState.YES) PvRuleStatus.Pass else PvRuleStatus.Fail,
        ruleId = "${prefix}_CR"
    ))

    return rules
}

fun CalibrationMetalDetectorConveyorViewModel.autoUpdateBinDoorMonitorPvResult() {
    if (!pvRequired.value) { setBinDoorMonitorTestPvResult("N/A"); return }
    setBinDoorMonitorTestPvResult(getBinDoorMonitorPvRules().calculateOverallStatus())
}

// ---------------------------------------------------------
// SME PV Logic
// ---------------------------------------------------------

fun CalibrationMetalDetectorConveyorViewModel.getSmePvRules(): List<PvRule> {
    if (!pvRequired.value) return emptyList()
    
    val witnessed = operatorTestWitnessed.value
    if (witnessed == YesNoState.NA) return listOf(PvRule(description = "Test marked as N/A", status = PvRuleStatus.NA, ruleId = "SME_NA"))

    val infeed = operatorTestWitnessedInfeed.value
    val rejectConfirm = operatorTestWitnessedRejectConfirm.value
    val binFull = operatorTestWitnessedBinFull.value
    val binDoor = operatorTestWitnessedBinDoor.value
    val airFail = operatorTestWitnessedAirFail.value
    val packCheck = operatorTestWitnessedPackCheck.value

    val engF = sensitivityAsLeftFerrous.value.normalisedSize().toDoubleOrNull() ?: 99.0
    val engNF = sensitivityAsLeftNonFerrous.value.normalisedSize().toDoubleOrNull() ?: 99.0
    val engS = sensitivityAsLeftStainless.value.normalisedSize().toDoubleOrNull() ?: 99.0
    val opF = operatorTestResultFerrous.value.normalisedSize().toDoubleOrNull() ?: 99.0
    val opNF = operatorTestResultNonFerrous.value.normalisedSize().toDoubleOrNull() ?: 99.0
    val opS = operatorTestResultStainless.value.normalisedSize().toDoubleOrNull() ?: 99.0
    val opLM = operatorTestResultLargeMetal.value.normalisedSize().toDoubleOrNull() ?: 99.0

    val rules = mutableListOf<PvRule>()
    
    rules.add(PvRule(
        description = "Operator test must be witnessed.",
        status = if (witnessed == YesNoState.YES) PvRuleStatus.Pass else PvRuleStatus.Fail,
        ruleId = "OPERATOR_TEST_WITNESSED"
    ))

    rules.add(PvRule(
        description = "The name of the operator performing the test must be recorded.",
        status = if (witnessed == YesNoState.YES && operatorName.value.isNotBlank()) PvRuleStatus.Pass else PvRuleStatus.Fail,
        ruleId = "OPERATOR_NAME"
    ))
    
    rules.add(PvRule(
        description = "Operator Ferrous test size must be <= engineer 'As Left' (${engF}mm) + cert.",
        status = if (opF <= engF && operatorTestResultCertNumberFerrous.value.isNotBlank()) PvRuleStatus.Pass else PvRuleStatus.Fail,
        ruleId = "OPERATOR_TEST_FERR"
    ))

    rules.add(PvRule(
        description = "Operator Non-Ferrous test size must be <= engineer 'As Left' (${engNF}mm) + cert.",
        status = if (opNF <= engNF && operatorTestResultCertNumberNonFerrous.value.isNotBlank()) PvRuleStatus.Pass else PvRuleStatus.Fail,
        ruleId = "OPERATOR_TEST_NON_FERR"
    ))

    rules.add(PvRule(
        description = "Operator Stainless test size must be <= engineer 'As Left' (${engS}mm) + cert.",
        status = if (opS <= engS && operatorTestResultCertNumberStainless.value.isNotBlank()) PvRuleStatus.Pass else PvRuleStatus.Fail,
        ruleId = "OPERATOR_TEST_STAINLESS"
    ))
    
    rules.add(PvRule(
        description = "Large metal test must use a 20mm ferrous sample + cert.",
        status = if (opLM <= 20.0 && operatorTestResultCertNumberLargeMetal.value.isNotBlank()) PvRuleStatus.Pass else PvRuleStatus.Fail,
        ruleId = "OPERATOR_TEST_LM"
    ))

    rules.add(PvRule(
        description = "Infeed Sensor test must be witnessed.",
        status = if (infeed == YesNoState.YES) PvRuleStatus.Pass else if (infeed == YesNoState.NA) PvRuleStatus.NA else PvRuleStatus.Fail,
        ruleId = "OPERATOR_TEST_INFEED"
    ))

    rules.add(PvRule(
        description = "Reject Confirm test must be witnessed.",
        status = if (rejectConfirm == YesNoState.YES) PvRuleStatus.Pass else if (rejectConfirm == YesNoState.NA) PvRuleStatus.NA else PvRuleStatus.Fail,
        ruleId = "OPERATOR_TEST_REJECT_CONFIRM"
    ))

    rules.add(PvRule(
        description = "Bin Full test must be witnessed.",
        status = if (binFull == YesNoState.YES) PvRuleStatus.Pass else if (binFull == YesNoState.NA) PvRuleStatus.NA else PvRuleStatus.Fail,
        ruleId = "OPERATOR_TEST_BIN_FULL"
    ))

    rules.add(PvRule(
        description = "Bin Door test must be witnessed.",
        status = if (binDoor == YesNoState.YES) PvRuleStatus.Pass else if (binDoor == YesNoState.NA) PvRuleStatus.NA else PvRuleStatus.Fail,
        ruleId = "OPERATOR_TEST_BIN_DOOR"
    ))

    rules.add(PvRule(
        description = "Air Fail test must be witnessed.",
        status = if (airFail == YesNoState.YES) PvRuleStatus.Pass else if (airFail == YesNoState.NA) PvRuleStatus.NA else PvRuleStatus.Fail,
        ruleId = "OPERATOR_TEST_AIR_FAIL"
    ))

    rules.add(PvRule(
        description = "Pack Check test must be witnessed.",
        status = if (packCheck == YesNoState.YES) PvRuleStatus.Pass else if (packCheck == YesNoState.NA) PvRuleStatus.NA else PvRuleStatus.Fail,
        ruleId = "OPERATOR_TEST_PACK_CHECK"
    ))

    rules.add(PvRule(
        description = "Site must have an SME on site, and their name must be recorded.",
        status = if (smeName.value.isNotBlank()) PvRuleStatus.Pass else PvRuleStatus.Fail,
        ruleId = "SME_NAME"
    ))

    return rules
}

fun CalibrationMetalDetectorConveyorViewModel.autoUpdateSmePvResult() {
    if (!pvRequired.value) { setSmeTestPvResult("N/A"); return }
    setSmeTestPvResult(getSmePvRules().calculateOverallStatus())
}

// ---------------------------------------------------------
// Detect Notify PV Logic
// ---------------------------------------------------------

fun CalibrationMetalDetectorConveyorViewModel.autoUpdateDetectNotificationTestPvResult() {
    if (!pvRequired.value) { setDetectNotificationTestPvResult("N/A"); return }
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
