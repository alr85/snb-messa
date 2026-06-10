package com.snb.inspect.calibrationLogic.metalDetectorConveyor

import com.snb.inspect.calibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import com.snb.inspect.formModules.PvRule
import com.snb.inspect.formModules.PvRuleStatus
import com.snb.inspect.formModules.YesNoState

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

//private fun String.normalisedSize(): String = trim().replace(",", ".")

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

        value.isBlank() || value == "None" ->
            setDetectionSettingPvResult("Fail")

        else ->
            setDetectionSettingPvResult("Pass")
    }
}

// ---------------------------------------------------------
// Sensitivity PV Logic (Ferrous, Non-Ferrous, Stainless)
// ---------------------------------------------------------

private fun CalibrationMetalDetectorConveyorViewModel.getSensitivityRules(
    reqStr: String,
    leftStr: String,
    drLeading: YesNoState,
    drMiddle: YesNoState,
    drTrailing: YesNoState,
    cert: String,
    prefix: String,
    drLeadingSignal: String,
    drMiddleSignal: String,
    drTrailingSignal: String
): List<PvRule> {
    if (!pvRequired.value) return emptyList()

    val rules = mutableListOf<PvRule>()


    // 1. Compliance (Warning if left > req)
    val req = reqStr.replace(",", ".").toDoubleOrNull()
    val left = leftStr.replace(",", ".").toDoubleOrNull()

    when {
        leftStr == "N/A" -> {
            rules.add(PvRule("Sensitivity requirement compliance.", PvRuleStatus.NA, "${prefix}_SENSITIVITY_COMPLIANCE"))
        }
        req != null && left != null -> {
            rules.add(PvRule(
                description = "As Left sensitivity ($left mm) vs Requirement ($req mm).",
                status = if (left <= req) PvRuleStatus.Pass else PvRuleStatus.Warning,
                ruleId = "${prefix}_SENSITIVITY_COMPLIANCE"
            ))
        }
        else -> {
            // This keeps the rule visible in the summary card even when the box is empty
            rules.add(PvRule("Sensitivity requirement compliance.", PvRuleStatus.Incomplete, "${prefix}_SENSITIVITY_COMPLIANCE"))
        }
    }

    rules.add(PvRule(
        description = "Sample certificate number must be recorded.",
        status = when {
            leftStr == "N/A" -> PvRuleStatus.NA // If size is N/A, Cert requirement is N/A
            cert.isNotBlank() -> PvRuleStatus.Pass
            else -> PvRuleStatus.Fail
        },
        ruleId = "${prefix}_CERT"
    ))

    if(isConveyor.value) {

        // --- Leading Edge Rules ---
        rules.add(
            PvRule(
                description = "Leading edge detection and rejection.",
                status = when (drLeading) {
                    YesNoState.YES -> PvRuleStatus.Pass
                    YesNoState.NA -> PvRuleStatus.NA
                    else -> PvRuleStatus.Fail
                },
                ruleId = "${prefix}_DR_LEADING"
            )
        )

        rules.add(
            PvRule(
                description = "Leading edge signal strength must be recorded.",
                status = when {
                    drLeading == YesNoState.NA -> PvRuleStatus.NA // Follows the toggle
                    drLeadingSignal.isNotBlank() -> PvRuleStatus.Pass
                    else -> PvRuleStatus.Fail
                },
                ruleId = "${prefix}_DR_LEADING_SIGNAL"
            )
        )

// --- Middle Rules ---
        rules.add(
            PvRule(
                description = "Middle detection and rejection.",
                status = when (drMiddle) {
                    YesNoState.YES -> PvRuleStatus.Pass
                    YesNoState.NA -> PvRuleStatus.NA
                    else -> PvRuleStatus.Fail
                },
                ruleId = "${prefix}_DR_MIDDLE"
            )
        )

        rules.add(
            PvRule(
                description = "Middle signal strength must be recorded.",
                status = when {
                    drMiddle == YesNoState.NA -> PvRuleStatus.NA
                    drMiddleSignal.isNotBlank() -> PvRuleStatus.Pass
                    else -> PvRuleStatus.Fail
                },
                ruleId = "${prefix}_DR_MIDDLE_SIGNAL"
            )
        )

// --- Trailing Rules ---
        rules.add(
            PvRule(
                description = "Trailing edge detection and rejection.",
                status = when (drTrailing) {
                    YesNoState.YES -> PvRuleStatus.Pass
                    YesNoState.NA -> PvRuleStatus.NA
                    else -> PvRuleStatus.Fail
                },
                ruleId = "${prefix}_DR_TRAILING"
            )
        )

        rules.add(
            PvRule(
                description = "Trailing signal strength must be recorded.",
                status = when {
                    drTrailing == YesNoState.NA -> PvRuleStatus.NA
                    drTrailingSignal.isNotBlank() -> PvRuleStatus.Pass
                    else -> PvRuleStatus.Fail
                },
                ruleId = "${prefix}_DR_TRAILING_SIGNAL"
            )
        )
    }
    else {
        // Single test pass for non-conveyor systems
        rules.add(
            PvRule(
                description = "Detection and rejection.",
                status = when (drLeading) {
                    YesNoState.YES -> PvRuleStatus.Pass
                    YesNoState.NA -> PvRuleStatus.NA
                    else -> PvRuleStatus.Fail
                },
                ruleId = "${prefix}_DR_LEADING"
            )
        )

        rules.add(
            PvRule(
                description = "Signal strength must be recorded.",
                status = when {
                    drLeading == YesNoState.NA -> PvRuleStatus.NA
                    drLeadingSignal.isNotBlank() -> PvRuleStatus.Pass
                    else -> PvRuleStatus.Fail
                },
                ruleId = "${prefix}_DR_LEADING_SIGNAL"
            )
        )
    }

    return rules
}


fun CalibrationMetalDetectorConveyorViewModel.getFerrousPvRules() = getSensitivityRules(
    sensitivityRequirementFerrous.value, sensitivityAsLeftFerrous.value,
    detectRejectFerrousLeading.value, detectRejectFerrousMiddle.value, detectRejectFerrousTrailing.value,
    sampleCertificateNumberFerrous.value, "FERROUS",
    peakSignalFerrousLeading.value, peakSignalFerrousMiddle.value, peakSignalFerrousTrailing.value
)

fun CalibrationMetalDetectorConveyorViewModel.getNonFerrousPvRules() = getSensitivityRules(
    sensitivityRequirementNonFerrous.value, sensitivityAsLeftNonFerrous.value,
    detectRejectNonFerrousLeading.value, detectRejectNonFerrousMiddle.value, detectRejectNonFerrousTrailing.value,
    sampleCertificateNumberNonFerrous.value, "NON_FERROUS",
    peakSignalNonFerrousLeading.value, peakSignalNonFerrousMiddle.value, peakSignalNonFerrousTrailing.value

)

fun CalibrationMetalDetectorConveyorViewModel.getStainlessPvRules() = getSensitivityRules(
    sensitivityRequirementStainless.value, sensitivityAsLeftStainless.value,
    detectRejectStainlessLeading.value, detectRejectStainlessMiddle.value, detectRejectStainlessTrailing.value,
    sampleCertificateNumberStainless.value, "STAINLESS",
    peakSignalStainlessLeading.value, peakSignalStainlessMiddle.value, peakSignalStainlessTrailing.value
)

// Result Updaters
fun CalibrationMetalDetectorConveyorViewModel.autoUpdateFerrousPvResult() {
    setFerrousTestPvResult(if (!pvRequired.value) "N/A" else getFerrousPvRules().calculateOverallStatus())
}
fun CalibrationMetalDetectorConveyorViewModel.autoUpdateNonFerrousPvResult() {
    setNonFerrousTestPvResult(if (!pvRequired.value) "N/A" else getNonFerrousPvRules().calculateOverallStatus())
}
fun CalibrationMetalDetectorConveyorViewModel.autoUpdateStainlessPvResult() {
    setStainlessTestPvResult(if (!pvRequired.value) "N/A" else getStainlessPvRules().calculateOverallStatus())
}



// ---------------------------------------------------------
// General Failsafe Rule Creator
// ---------------------------------------------------------
private fun getFailsafeRules(
    prefix: String,
    fitted: YesNoState,
    method: String,
    methodOther: String,
    results: List<String>,
    latched: YesNoState,
    cr: YesNoState,
    requireBeltStop: Boolean = true,
    includeLatchedCr: Boolean = true
): List<PvRule> {
    val rules = mutableListOf<PvRule>()

    val fittedStatus = when(fitted) {
        YesNoState.YES -> PvRuleStatus.Pass
        YesNoState.NO -> PvRuleStatus.Fail
        YesNoState.NA -> PvRuleStatus.NA
        else -> PvRuleStatus.Incomplete
    }

    rules.add(PvRule("Sensor must be fitted to meet retailer safety standards.", fittedStatus, "${prefix}_FITTED"))

    // If not fitted or NA, subsequent rules are NA
    if (fitted != YesNoState.YES) {
        val st = if (fitted == YesNoState.NA) PvRuleStatus.NA else PvRuleStatus.Incomplete
        rules.add(PvRule("Test method selected.", st, "${prefix}_METHOD"))
        rules.add(PvRule("Test result recorded.", st, "${prefix}_RESULT"))
        if (includeLatchedCr) {
            rules.add(PvRule("Fault condition latched.", st, "${prefix}_LATCHED"))
            rules.add(PvRule("Controlled restart required.", st, "${prefix}_CR"))
        }
        return rules
    }

    // 2. Test method
    val methodPass = method.isNotBlank() && method != "N/A" && (method != "Other" || methodOther.isNotBlank())
    rules.add(PvRule("A valid test method must be selected.", if (methodPass) PvRuleStatus.Pass else PvRuleStatus.Incomplete, "${prefix}_METHOD"))

    // 3. Test result
    val resultPass = results.isNotEmpty() && "No Result" !in results &&
            results.any { it.contains("Indicator", ignoreCase = true) || it.contains("Notification", ignoreCase = true) } &&
            (!requireBeltStop || "System Belt Stops" in results)

    rules.add(PvRule(
        description = if (requireBeltStop) "The belt must stop, and an audible or visual notification must be recorded." else "An audible or visual notification must be recorded.",
        status = if (resultPass) PvRuleStatus.Pass else PvRuleStatus.Fail,
        ruleId = "${prefix}_RESULT"
    ))

    if (includeLatchedCr) {
        // 4. Fault latched
        rules.add(PvRule("The failsafe condition must be latched (Stop/Alarm).", if (latched == YesNoState.YES) PvRuleStatus.Pass else if (latched == YesNoState.NO) PvRuleStatus.Fail else PvRuleStatus.Incomplete, "${prefix}_LATCHED"))

        // 5. Controlled restart
        rules.add(PvRule("A manual reset/restart must be required to resume operation.", if (cr == YesNoState.YES) PvRuleStatus.Pass else if (cr == YesNoState.NO) PvRuleStatus.Fail else PvRuleStatus.Incomplete, "${prefix}_CR"))
    }

    return rules
}

// ---------------------------------------------------------
// Infeed Sensor PV Logic
// ---------------------------------------------------------
fun CalibrationMetalDetectorConveyorViewModel.getInfeedSensorPvRules(): List<PvRule> =
    getFailsafeRules("INFEED", infeedSensorFitted.value, infeedSensorTestMethod.value,
        infeedSensorTestMethodOther.value, infeedSensorTestResult.value,
        infeedSensorLatched.value, infeedSensorCR.value,
        requireBeltStop = isConveyor.value)

fun CalibrationMetalDetectorConveyorViewModel.autoUpdateInfeedSensorPvResult() {
    val result = if (!pvRequired.value) "N/A" else getInfeedSensorPvRules().calculateOverallStatus()
    setInfeedSensorTestPvResult(result)
}

// ---------------------------------------------------------
// Reject Confirm PV Logic
// ---------------------------------------------------------
fun CalibrationMetalDetectorConveyorViewModel.getRejectConfirmSensorPvRules(): List<PvRule> {
    val rules = getFailsafeRules(
        "REJECT_CONFIRM",
        rejectConfirmSensorFitted.value,
        rejectConfirmSensorTestMethod.value,
        rejectConfirmSensorTestMethodOther.value,
        rejectConfirmSensorTestResult.value,
        rejectConfirmSensorLatched.value,
        rejectConfirmSensorCR.value,
        requireBeltStop = isConveyor.value
    ).toMutableList()

    // Add specific check for stop position only if fitted
    if (rejectConfirmSensorFitted.value == YesNoState.YES) {
        val stopOk = rejectConfirmSensorStopPosition.value.isNotBlank() &&
                rejectConfirmSensorStopPosition.value != "Uncontrolled"

        rules.add(PvRule(
            description = "The stop position must be controlled.",
            status = if (stopOk) PvRuleStatus.Pass else PvRuleStatus.Fail,
            ruleId = "REJECT_CONFIRM_STOP_POS"
        ))
    }
    return rules
}

fun CalibrationMetalDetectorConveyorViewModel.autoUpdateRejectConfirmSensorPvResult() {
    val result = if (!pvRequired.value) "N/A" else getRejectConfirmSensorPvRules().calculateOverallStatus()
    setRejectConfirmSensorTestPvResult(result)
}

// ---------------------------------------------------------
// Bin Full Sensor PV Logic
// ---------------------------------------------------------
fun CalibrationMetalDetectorConveyorViewModel.getBinFullSensorPvRules(): List<PvRule> =
    getFailsafeRules("BIN_FULL", binFullSensorFitted.value, binFullSensorTestMethod.value,
        binFullSensorTestMethodOther.value, binFullSensorTestResult.value,
        binFullSensorLatched.value, binFullSensorCR.value,
        requireBeltStop = isConveyor.value)

fun CalibrationMetalDetectorConveyorViewModel.autoUpdateBinFullSensorPvResult() {
    val result = if (!pvRequired.value) "N/A" else getBinFullSensorPvRules().calculateOverallStatus()
    setBinFullSensorTestPvResult(result)
}

// ---------------------------------------------------------
// Backup Sensor PV Logic
// ---------------------------------------------------------
fun CalibrationMetalDetectorConveyorViewModel.getBackupSensorPvRules(): List<PvRule> =
    getFailsafeRules("BACKUP", backupSensorFitted.value, backupSensorTestMethod.value,
        backupSensorTestMethodOther.value, backupSensorTestResult.value,
        backupSensorLatched.value, backupSensorCR.value,
        requireBeltStop = isConveyor.value)

fun CalibrationMetalDetectorConveyorViewModel.autoUpdateBackupSensorPvResult() {
    val result = if (!pvRequired.value) "N/A" else getBackupSensorPvRules().calculateOverallStatus()
    setBackupSensorTestPvResult(result)
}

// ---------------------------------------------------------
// Air Pressure Sensor PV Logic
// ---------------------------------------------------------
fun CalibrationMetalDetectorConveyorViewModel.getAirPressureSensorPvRules(): List<PvRule> =
    getFailsafeRules("AIR", airPressureSensorFitted.value, airPressureSensorTestMethod.value,
        airPressureSensorTestMethodOther.value, airPressureSensorTestResult.value,
        airPressureSensorLatched.value, airPressureSensorCR.value,
        requireBeltStop = isConveyor.value)

fun CalibrationMetalDetectorConveyorViewModel.autoUpdateAirPressureSensorPvResult() {
    val result = if (!pvRequired.value) "N/A" else getAirPressureSensorPvRules().calculateOverallStatus()
    setAirPressureSensorTestPvResult(result)
}

// ---------------------------------------------------------
// Pack Check Sensor PV Logic
// ---------------------------------------------------------
fun CalibrationMetalDetectorConveyorViewModel.getPackCheckSensorPvRules(): List<PvRule> =
    getFailsafeRules("PACK", packCheckSensorFitted.value, packCheckSensorTestMethod.value,
        packCheckSensorTestMethodOther.value, packCheckSensorTestResult.value,
        packCheckSensorLatched.value, packCheckSensorCR.value,
        requireBeltStop = isConveyor.value)

fun CalibrationMetalDetectorConveyorViewModel.autoUpdatePackCheckSensorPvResult() {
    val result = if (!pvRequired.value) "N/A" else getPackCheckSensorPvRules().calculateOverallStatus()
    setPackCheckSensorTestPvResult(result)
}

// ---------------------------------------------------------
// Speed Sensor PV Logic (Note: requireBeltStop = false)
// ---------------------------------------------------------
fun CalibrationMetalDetectorConveyorViewModel.getSpeedSensorPvRules(): List<PvRule> =
    getFailsafeRules("SPEED", speedSensorFitted.value, speedSensorTestMethod.value,
        speedSensorTestMethodOther.value, speedSensorTestResult.value,
        speedSensorLatched.value, speedSensorCR.value,
        requireBeltStop = false,
        includeLatchedCr = false)

fun CalibrationMetalDetectorConveyorViewModel.autoUpdateSpeedSensorPvResult() {
    val result = if (!pvRequired.value) "N/A" else getSpeedSensorPvRules().calculateOverallStatus()
    setSpeedSensorTestPvResult(result)
}

// ---------------------------------------------------------
// Large Metal PV Logic
// ---------------------------------------------------------
fun CalibrationMetalDetectorConveyorViewModel.getLargeMetalPvRules(): List<PvRule> {
    if (!pvRequired.value) return emptyList()

    val dAndR = detectRejectLargeMetal.value
    val cert = sampleCertificateNumberLargeMetal.value

    // Handle the specific N/A selection
    if (dAndR == YesNoState.NA) {
        return listOf(PvRule("20mm Ferrous Test Sample test skipped (N/A).", PvRuleStatus.NA, "LARGE_METAL_DETECT_REJECT"))
    }

    val rules = mutableListOf<PvRule>()
    rules.add(PvRule(
        description = "20mm Ferrous Test Sample must be detected and rejected. The certificate number must be recorded.",
        status = if (dAndR == YesNoState.YES && cert.isNotBlank()) PvRuleStatus.Pass else PvRuleStatus.Fail,
        ruleId = "LARGE_METAL_DETECT_REJECT"
    ))

    return rules
}

fun CalibrationMetalDetectorConveyorViewModel.autoUpdateLargeMetalPvResult() {
    val result = if (!pvRequired.value) "N/A" else getLargeMetalPvRules().calculateOverallStatus()
    setLargeMetalTestPvResult(result)
}

// ---------------------------------------------------------
// Bin Door Monitor PV Logic
// ---------------------------------------------------------
fun CalibrationMetalDetectorConveyorViewModel.getBinDoorMonitorPvRules(): List<PvRule> {
    val prefix = "BIN_DOOR"
    val fitted = binDoorMonitorFitted.value

    // 1. Handle Not Fitted or N/A
    if (fitted != YesNoState.YES) {
        val st = if (fitted == YesNoState.NA) PvRuleStatus.NA else PvRuleStatus.Fail
        val subSt = if (fitted == YesNoState.NA) PvRuleStatus.NA else PvRuleStatus.Incomplete

        return listOf(
            PvRule("Sensor fitted to meet safety standards.", st, "${prefix}_FITTED"),
            PvRule("Timeout timer recorded.", subSt, "${prefix}_TIMEOUT"),
            PvRule("Timeout result recorded.", subSt, "${prefix}_TIMEOUT_RESULT"),
            PvRule("Condition latched.", subSt, "${prefix}_LATCHED"),
            PvRule("Controlled restart required.", subSt, "${prefix}_CR")
        )
    }

    val rules = mutableListOf<PvRule>()
    rules.add(PvRule("Sensor fitted.", PvRuleStatus.Pass, "${prefix}_FITTED"))

    // 2. Timeout Timer (String check)
    val timeoutValue = binDoorTimeoutTimer.value.toIntOrNull()
    rules.add(PvRule(
        description = "The timeout timer value must be recorded and be 30 seconds or less.",
        status = if (timeoutValue != null && timeoutValue <= 30) PvRuleStatus.Pass else PvRuleStatus.Fail,
        ruleId = "${prefix}_TIMEOUT"
    ))

    // 3. Timeout Result
    val timeoutResultPass = binDoorTimeoutResult.value.any { it.contains("System Belt Stops", ignoreCase = true) }
    rules.add(PvRule(
        description = "The system must stop when the timeout occurs.",
        status = if (timeoutResultPass) PvRuleStatus.Pass else PvRuleStatus.Fail,
        ruleId = "${prefix}_TIMEOUT_RESULT"
    ))

    // 4. Latched & CR
    rules.add(PvRule(
        description = "Fault condition must be latched.",
        status = if (binDoorLatched.value == YesNoState.YES) PvRuleStatus.Pass else PvRuleStatus.Fail,
        ruleId = "${prefix}_LATCHED"
    ))

    rules.add(PvRule(
        description = "Controlled restart must be required.",
        status = if (binDoorCR.value == YesNoState.YES) PvRuleStatus.Pass else PvRuleStatus.Fail,
        ruleId = "${prefix}_CR"
    ))

    return rules
}

fun CalibrationMetalDetectorConveyorViewModel.autoUpdateBinDoorMonitorPvResult() {
    val result = if (!pvRequired.value) "N/A" else getBinDoorMonitorPvRules().calculateOverallStatus()
    setBinDoorMonitorTestPvResult(result)
}

// ---------------------------------------------------------
// Detect Notification PV Logic
// ---------------------------------------------------------
fun CalibrationMetalDetectorConveyorViewModel.getDetectNotificationPvRules(): List<PvRule> {
    if (!pvRequired.value) return emptyList()
    val results = detectNotificationResult.value

    return listOf(
        PvRule(
            description = "At least one visual or audible notification must be recorded.",
            status = if (results.isNotEmpty() && "No Result" !in results) PvRuleStatus.Pass else PvRuleStatus.Fail,
            ruleId = "DETECT_NOTIFY_RESULT"
        )
    )
}

fun CalibrationMetalDetectorConveyorViewModel.autoUpdateDetectNotificationPvResult() {
    val result = if (!pvRequired.value) "N/A" else getDetectNotificationPvRules().calculateOverallStatus()
    setDetectNotificationTestPvResult(result)
}

// ---------------------------------------------------------
// SME Details PV Logic
// ---------------------------------------------------------
fun CalibrationMetalDetectorConveyorViewModel.getSmePvRules(): List<PvRule> {
    if (!pvRequired.value) return emptyList()

    val rules = mutableListOf<PvRule>()

    // 1. Witnessed
    val witnessed = operatorTestWitnessed.value
    rules.add(PvRule(
        description = "The operator test must be witnessed by the engineer.",
        status = if (witnessed == YesNoState.YES) PvRuleStatus.Pass else if (witnessed == YesNoState.NO) PvRuleStatus.Fail else PvRuleStatus.Incomplete,
        ruleId = "OPERATOR_TEST_WITNESSED"
    ))

    if (witnessed != YesNoState.YES) {
        val st = if (witnessed == YesNoState.NA) PvRuleStatus.NA else PvRuleStatus.Incomplete
        rules.add(PvRule("Operator name recorded.", st, "OPERATOR_NAME"))
        rules.add(PvRule("Ferrous test recorded.", st, "OPERATOR_TEST_FERR"))
        rules.add(PvRule("Non-Ferrous test recorded.", st, "OPERATOR_TEST_NON_FERR"))
        rules.add(PvRule("Stainless test recorded.", st, "OPERATOR_TEST_STAINLESS"))
        rules.add(PvRule("Large Metal test recorded.", st, "OPERATOR_TEST_LM"))
        rules.add(PvRule("Infeed sensor test witnessed.", st, "OPERATOR_TEST_INFEED"))
        rules.add(PvRule("Reject confirm sensor test witnessed.", st, "OPERATOR_TEST_REJECT_CONFIRM"))
        rules.add(PvRule("Bin full sensor test witnessed.", st, "OPERATOR_TEST_BIN_FULL"))
        rules.add(PvRule("Bin door monitor test witnessed.", st, "OPERATOR_TEST_BIN_DOOR"))
        rules.add(PvRule("Air pressure sensor test witnessed.", st, "OPERATOR_TEST_AIR_FAIL"))
        rules.add(PvRule("Pack check sensor test witnessed.", st, "OPERATOR_TEST_PACK_CHECK"))
        rules.add(PvRule("Speed sensor test witnessed.", st, "OPERATOR_TEST_SPEED_SENSOR"))
        rules.add(PvRule("Backup sensor test witnessed.", st, "OPERATOR_TEST_BACKUP"))
        rules.add(PvRule("SME name recorded.", st, "SME_NAME"))
        return rules
    }

    // 2. Names
    rules.add(PvRule(
        description = "Operator name must be recorded.",
        status = if (operatorName.value.isNotBlank() && operatorName.value != "N/A") PvRuleStatus.Pass else PvRuleStatus.Fail,
        ruleId = "OPERATOR_NAME"
    ))

    rules.add(PvRule(
        description = "SME name must be recorded.",
        status = if (smeName.value.isNotBlank() && smeName.value != "N/A") PvRuleStatus.Pass else PvRuleStatus.Fail,
        ruleId = "SME_NAME"
    ))

    // 3. Tests
    fun getTestStatus(size: String, cert: String): PvRuleStatus {
        return when {
            size == "N/A" || cert == "N/A" -> PvRuleStatus.NA
            size.isNotBlank() && cert.isNotBlank() -> PvRuleStatus.Pass
            else -> PvRuleStatus.Fail
        }
    }

    rules.add(PvRule("Ferrous size and certificate recorded.", getTestStatus(operatorTestResultFerrous.value, operatorTestResultCertNumberFerrous.value), "OPERATOR_TEST_FERR"))
    rules.add(PvRule("Non-Ferrous size and certificate recorded.", getTestStatus(operatorTestResultNonFerrous.value, operatorTestResultCertNumberNonFerrous.value), "OPERATOR_TEST_NON_FERR"))
    rules.add(PvRule("Stainless size and certificate recorded.", getTestStatus(operatorTestResultStainless.value, operatorTestResultCertNumberStainless.value), "OPERATOR_TEST_STAINLESS"))
    rules.add(PvRule("Large Metal size and certificate recorded.", getTestStatus(operatorTestResultLargeMetal.value, operatorTestResultCertNumberLargeMetal.value), "OPERATOR_TEST_LM"))

    // 4. Failsafe Tests Witnessed
    fun getWitnessStatus(state: YesNoState) = when(state) {
        YesNoState.YES -> PvRuleStatus.Pass
        YesNoState.NA -> PvRuleStatus.NA
        else -> PvRuleStatus.Fail
    }

    rules.add(PvRule("Infeed sensor test witnessed.", getWitnessStatus(operatorTestWitnessedInfeed.value), "OPERATOR_TEST_INFEED"))
    rules.add(PvRule("Reject confirm sensor test witnessed.", getWitnessStatus(operatorTestWitnessedRejectConfirm.value), "OPERATOR_TEST_REJECT_CONFIRM"))
    rules.add(PvRule("Bin full sensor test witnessed.", getWitnessStatus(operatorTestWitnessedBinFull.value), "OPERATOR_TEST_BIN_FULL"))
    rules.add(PvRule("Bin door monitor test witnessed.", getWitnessStatus(operatorTestWitnessedBinDoor.value), "OPERATOR_TEST_BIN_DOOR"))
    rules.add(PvRule("Air pressure sensor test witnessed.", getWitnessStatus(operatorTestWitnessedAirFail.value), "OPERATOR_TEST_AIR_FAIL"))
    rules.add(PvRule("Pack check sensor test witnessed.", getWitnessStatus(operatorTestWitnessedPackCheck.value), "OPERATOR_TEST_PACK_CHECK"))
    rules.add(PvRule("Speed sensor test witnessed.", getWitnessStatus(operatorTestWitnessedSpeedSensor.value), "OPERATOR_TEST_SPEED_SENSOR"))
    rules.add(PvRule("Backup sensor test witnessed.", getWitnessStatus(operatorTestWitnessedBackup.value), "OPERATOR_TEST_BACKUP"))

    return rules
}

fun CalibrationMetalDetectorConveyorViewModel.autoUpdateSmePvResult() {
    val result = if (!pvRequired.value) "N/A" else getSmePvRules().calculateOverallStatus()
    setSmeTestPvResult(result)
}

// ---------------------------------------------------------
// Trigger all PV updates
// ---------------------------------------------------------
fun CalibrationMetalDetectorConveyorViewModel.autoUpdateAllPvResults() {
    autoUpdateDetectionSettingPvResult()
    autoUpdateFerrousPvResult()
    autoUpdateNonFerrousPvResult()
    autoUpdateStainlessPvResult()
    autoUpdateLargeMetalPvResult()
    autoUpdateInfeedSensorPvResult()
    autoUpdateRejectConfirmSensorPvResult()
    autoUpdateBinFullSensorPvResult()
    autoUpdateBackupSensorPvResult()
    autoUpdateAirPressureSensorPvResult()
    autoUpdatePackCheckSensorPvResult()
    autoUpdateSpeedSensorPvResult()
    autoUpdateDetectNotificationPvResult()
    autoUpdateBinDoorMonitorPvResult()
    autoUpdateSmePvResult()
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
