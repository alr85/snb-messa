package com.snb.inspect.screens.service.mdCalibration

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.KeyboardType
import com.snb.inspect.calibrationLogic.metalDetectorConveyor.*
import com.snb.inspect.calibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import com.snb.inspect.formModules.*
import com.snb.inspect.ui.theme.FormSpacer
import com.snb.inspect.ui.theme.ScrollableWithScrollbar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalMetalDetectorConveyorSmeDetails(
    viewModel: CalibrationMetalDetectorConveyorViewModel
) {
    val operatorTestWitnessed by viewModel.operatorTestWitnessed
    val operatorName by viewModel.operatorName

    val ferrousSize by viewModel.operatorTestResultFerrous
    val nonFerrousSize by viewModel.operatorTestResultNonFerrous
    val stainlessSize by viewModel.operatorTestResultStainless
    val largeMetalSize by viewModel.operatorTestResultLargeMetal

    val ferrousCert by viewModel.operatorTestResultCertNumberFerrous
    val nonFerrousCert by viewModel.operatorTestResultCertNumberNonFerrous
    val stainlessCert by viewModel.operatorTestResultCertNumberStainless
    val largeMetalCert by viewModel.operatorTestResultCertNumberLargeMetal

    val infeed by viewModel.operatorTestWitnessedInfeed
    val rejectConfirm by viewModel.operatorTestWitnessedRejectConfirm
    val binFull by viewModel.operatorTestWitnessedBinFull
    val binDoor by viewModel.operatorTestWitnessedBinDoor
    val airFail by viewModel.operatorTestWitnessedAirFail
    val packCheck by viewModel.operatorTestWitnessedPackCheck
    val speedSensor by viewModel.operatorTestWitnessedSpeedSensor
    val backup by viewModel.operatorTestWitnessedBackup


    val smeName by viewModel.smeName
    val notes by viewModel.smeEngineerNotes

    // Next enabled logic
    val isNextStepEnabled = when (operatorTestWitnessed) {
        YesNoState.NO, YesNoState.NA -> true
        YesNoState.YES -> {
            operatorName.isNotBlank() &&
                    ferrousSize.isNotBlank() &&
                    nonFerrousSize.isNotBlank() &&
                    stainlessSize.isNotBlank() &&
                    largeMetalSize.isNotBlank() &&
                    ferrousCert.isNotBlank() &&
                    nonFerrousCert.isNotBlank() &&
                    stainlessCert.isNotBlank() &&
                    largeMetalCert.isNotBlank() &&
                    smeName.isNotBlank()
        }
        else -> false
    }

    LaunchedEffect(isNextStepEnabled) {
        viewModel.setCurrentScreenNextEnabled(isNextStepEnabled)
    }

    // PV Rules Calculation
    val rules = viewModel.getSmePvRules()

    // Sensitivity Warning Logic
    val customerReqFe = viewModel.sensitivityRequirementFerrous.value.replace(",", ".").toDoubleOrNull() ?: 0.0
    val achievedFe = ferrousSize.replace(",", ".").toDoubleOrNull() ?: 0.0
    val isSensitivityWarningFe = achievedFe > customerReqFe && achievedFe > 0.0 && customerReqFe > 0.0

    val customerReqNonFe = viewModel.sensitivityRequirementNonFerrous.value.replace(",", ".").toDoubleOrNull() ?: 0.0
    val achievedNonFe = nonFerrousSize.replace(",", ".").toDoubleOrNull() ?: 0.0
    val isSensitivityWarningNonFe = achievedNonFe > customerReqNonFe && achievedNonFe > 0.0 && customerReqNonFe > 0.0

    val customerReqStainless = viewModel.sensitivityRequirementStainless.value.replace(",", ".").toDoubleOrNull() ?: 0.0
    val achievedStainless = nonFerrousSize.replace(",", ".").toDoubleOrNull() ?: 0.0
    val isSensitivityWarningStainless = achievedStainless > customerReqStainless && achievedStainless > 0.0 && customerReqStainless > 0.0

    Column(modifier = Modifier.fillMaxSize()) {

        CalibrationHeader("Operator Test")

        ScrollableWithScrollbar(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
        ) {
            Column {

                LabeledTriStateSwitchWithHelp(
                    label = "Operator Test Witnessed?",
                    currentState = operatorTestWitnessed,
                    onStateChange = { newState ->
                        viewModel.setOperatorTestWitnessed(newState)

                        if (newState == YesNoState.NA || newState == YesNoState.NO) {
                            val na = "N/A"
                            viewModel.setOperatorName(na)
                            viewModel.setOperatorTestResultFerrous(na)
                            viewModel.setOperatorTestResultNonFerrous(na)
                            viewModel.setOperatorTestResultStainless(na)
                            viewModel.setOperatorTestResultLargeMetal(na)
                            viewModel.setOperatorTestResultCertNumberFerrous(na)
                            viewModel.setOperatorTestResultCertNumberNonFerrous(na)
                            viewModel.setOperatorTestResultCertNumberStainless(na)
                            viewModel.setOperatorTestResultCertNumberLargeMetal(na)
                            viewModel.setOperatorTestWitnessedInfeed(YesNoState.NA)
                            viewModel.setOperatorTestWitnessedRejectConfirm(YesNoState.NA)
                            viewModel.setOperatorTestWitnessedBinFull(YesNoState.NA)
                            viewModel.setOperatorTestWitnessedBinDoor(YesNoState.NA)
                            viewModel.setOperatorTestWitnessedAirFail(YesNoState.NA)
                            viewModel.setOperatorTestWitnessedPackCheck(YesNoState.NA)
                            viewModel.setSmeName(na)
                        } else if (newState == YesNoState.YES) {
                            viewModel.setOperatorName("")
                            viewModel.setOperatorTestResultFerrous("")
                            viewModel.setOperatorTestResultNonFerrous("")
                            viewModel.setOperatorTestResultStainless("")
                            viewModel.setOperatorTestResultLargeMetal("20.0") 
                            viewModel.setOperatorTestResultCertNumberFerrous("")
                            viewModel.setOperatorTestResultCertNumberNonFerrous("")
                            viewModel.setOperatorTestResultCertNumberStainless("")
                            viewModel.setOperatorTestResultCertNumberLargeMetal("")
                            if (viewModel.operatorTestWitnessedInfeed.value == YesNoState.NA) {
                                viewModel.setOperatorTestWitnessedInfeed(YesNoState.NA)
                            }
                            else {
                                viewModel.setOperatorTestWitnessedInfeed(YesNoState.NO)
                            }
                            if (viewModel.operatorTestWitnessedRejectConfirm.value == YesNoState.NA) {
                                viewModel.setOperatorTestWitnessedRejectConfirm(YesNoState.NA)
                            }
                            else {
                                viewModel.setOperatorTestWitnessedRejectConfirm(YesNoState.NO)
                            }
                            if (viewModel.operatorTestWitnessedBinFull.value == YesNoState.NA) {
                                viewModel.setOperatorTestWitnessedBinFull(YesNoState.NA)
                            }
                            else {
                                viewModel.setOperatorTestWitnessedBinFull(YesNoState.NO)
                            }
                            if (viewModel.operatorTestWitnessedBinDoor.value == YesNoState.NA) {
                                viewModel.setOperatorTestWitnessedBinDoor(YesNoState.NA)
                            }
                            else {
                                viewModel.setOperatorTestWitnessedBinDoor(YesNoState.NO)
                            }
                            if (viewModel.operatorTestWitnessedAirFail.value == YesNoState.NA) {
                                viewModel.setOperatorTestWitnessedAirFail(YesNoState.NA)
                            }
                            else {
                                viewModel.setOperatorTestWitnessedAirFail(YesNoState.NO)
                            }
                            if (viewModel.operatorTestWitnessedPackCheck.value == YesNoState.NA) {
                                viewModel.setOperatorTestWitnessedPackCheck(YesNoState.NA)
                            }
                            else {
                                viewModel.setOperatorTestWitnessedPackCheck(YesNoState.NO)
                            }

                            viewModel.setSmeName("")
                        }
                        viewModel.autoUpdateSmePvResult()
                    },
                    helpText = "Confirm if you witnessed an operator perform a successful sensitivity check.",
                    pvStatus = if (viewModel.pvRequired.value) rules.find { it.ruleId == "OPERATOR_TEST_WITNESSED" }?.status?.name else null,
                    pvRules = rules.filter { it.ruleId == "OPERATOR_TEST_WITNESSED" }
                )

                FormSpacer()

                if (operatorTestWitnessed == YesNoState.YES) {

                    LabeledTextFieldWithHelp(
                        label = "Operator Name",
                        value = operatorName,
                        onValueChange = { viewModel.setOperatorName(it); viewModel.autoUpdateSmePvResult() },
                        helpText = "Enter the operator's name.",
                        isNAToggleEnabled = false,
                        pvStatus = if (viewModel.pvRequired.value) rules.find { it.ruleId == "OPERATOR_NAME" }?.status?.name else null,
                        pvRules = rules.filter { it.ruleId == "OPERATOR_NAME" },
                        maxLength = 25
                    )

                    FormSpacer()

                    LabeledTwoTextInputsWithHelp(
                        label = "Ferrous Test",
                        firstInputLabel = "Size (mm)",
                        firstInputValue = ferrousSize,
                        onFirstInputValueChange = { viewModel.setOperatorTestResultFerrous(it); viewModel.autoUpdateSmePvResult() },
                        secondInputLabel = "Cert No.",
                        secondInputValue = ferrousCert,
                        onSecondInputValueChange = { viewModel.setOperatorTestResultCertNumberFerrous(it.uppercase()); viewModel.autoUpdateSmePvResult() },
                        helpText = "Enter Ferrous size and certificate number.",
                        firstInputKeyboardType = KeyboardType.Decimal,
                        isNAToggleEnabled = true,
                        pvStatus = if (viewModel.pvRequired.value) rules.find { it.ruleId == "OPERATOR_TEST_FERR" }?.status?.name else null,
                        pvRules = rules.filter { it.ruleId == "OPERATOR_TEST_FERR" }
                    )

                    if (isSensitivityWarningFe) {
                        Spacer(Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.errorContainer, MaterialTheme.shapes.small)
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "⚠️ Achieved sensitivity ($achievedFe mm) is worse than Customer Requirement ($customerReqFe mm).",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    FormSpacer()

                    LabeledTwoTextInputsWithHelp(
                        label = "Non-Ferrous Test",
                        firstInputLabel = "Size (mm)",
                        firstInputValue = nonFerrousSize,
                        onFirstInputValueChange = { viewModel.setOperatorTestResultNonFerrous(it); viewModel.autoUpdateSmePvResult() },
                        secondInputLabel = "Cert No.",
                        secondInputValue = nonFerrousCert,
                        onSecondInputValueChange = { viewModel.setOperatorTestResultCertNumberNonFerrous(it.uppercase()); viewModel.autoUpdateSmePvResult() },
                        helpText = "Enter Non-Ferrous size and certificate number.",
                        firstInputKeyboardType = KeyboardType.Decimal,
                        isNAToggleEnabled = true,
                        pvStatus = if (viewModel.pvRequired.value) rules.find { it.ruleId == "OPERATOR_TEST_NON_FERR" }?.status?.name else null,
                        pvRules = rules.filter { it.ruleId == "OPERATOR_TEST_NON_FERR" }
                    )

                    if (isSensitivityWarningNonFe) {
                        Spacer(Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.errorContainer, MaterialTheme.shapes.small)
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "⚠️ Achieved sensitivity ($achievedNonFe mm) is worse than Customer Requirement ($customerReqNonFe mm).",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    FormSpacer()

                    LabeledTwoTextInputsWithHelp(
                        label = "Stainless Test",
                        firstInputLabel = "Size (mm)",
                        firstInputValue = stainlessSize,
                        onFirstInputValueChange = { viewModel.setOperatorTestResultStainless(it); viewModel.autoUpdateSmePvResult() },
                        secondInputLabel = "Cert No.",
                        secondInputValue = stainlessCert,
                        onSecondInputValueChange = { viewModel.setOperatorTestResultCertNumberStainless(it.uppercase()); viewModel.autoUpdateSmePvResult() },
                        helpText = "Enter Stainless size and certificate number.",
                        firstInputKeyboardType = KeyboardType.Decimal,
                        isNAToggleEnabled = true,
                        pvStatus = if (viewModel.pvRequired.value) rules.find { it.ruleId == "OPERATOR_TEST_STAINLESS" }?.status?.name else null,
                        pvRules = rules.filter { it.ruleId == "OPERATOR_TEST_STAINLESS" }
                    )

                    if (isSensitivityWarningStainless) {
                        Spacer(Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.errorContainer, MaterialTheme.shapes.small)
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "⚠️ Achieved sensitivity ($achievedStainless mm) is worse than Customer Requirement ($customerReqStainless mm).",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    FormSpacer()

                    LabeledTwoTextInputsWithHelp(
                        label = "Large Metal",
                        firstInputLabel = "Size (mm)",
                        firstInputValue = largeMetalSize,
                        onFirstInputValueChange = { viewModel.setOperatorTestResultLargeMetal(it); viewModel.autoUpdateSmePvResult() },
                        secondInputLabel = "Cert No.",
                        secondInputValue = largeMetalCert,
                        onSecondInputValueChange = { viewModel.setOperatorTestResultCertNumberLargeMetal(it.uppercase()); viewModel.autoUpdateSmePvResult() },
                        helpText = "Enter Large Metal size and certificate number.",
                        firstInputKeyboardType = KeyboardType.Decimal,
                        isNAToggleEnabled = true,
                        pvStatus = if (viewModel.pvRequired.value) rules.find { it.ruleId == "OPERATOR_TEST_LM" }?.status?.name else null,
                        pvRules = rules.filter { it.ruleId == "OPERATOR_TEST_LM" }
                    )

                    FormSpacer()

                    LabeledTriStateSwitchWithHelp(
                        label = "Infeed Sensor Test",
                        currentState = infeed,
                        onStateChange = { viewModel.setOperatorTestWitnessedInfeed(it); viewModel.autoUpdateSmePvResult() },
                        helpText = "Was the Infeed sensor test performed by the operator?",
                        isNAToggleEnabled = true,
                        pvStatus = if (viewModel.pvRequired.value) rules.find { it.ruleId == "OPERATOR_TEST_INFEED" }?.status?.name else null,
                        pvRules = rules.filter { it.ruleId == "OPERATOR_TEST_INFEED" }
                    )

                    FormSpacer()

                    LabeledTriStateSwitchWithHelp(
                        label = "Reject Confirm Test",
                        currentState = rejectConfirm,
                        onStateChange = { viewModel.setOperatorTestWitnessedRejectConfirm(it); viewModel.autoUpdateSmePvResult() },
                        helpText = "Was the Reject Confirm sensor test performed by the operator?",
                        isNAToggleEnabled = true,
                        pvStatus = if (viewModel.pvRequired.value) rules.find { it.ruleId == "OPERATOR_TEST_REJECT_CONFIRM" }?.status?.name else null,
                        pvRules = rules.filter { it.ruleId == "OPERATOR_TEST_REJECT_CONFIRM" }
                    )

                    FormSpacer()

                    LabeledTriStateSwitchWithHelp(
                        label = "Bin Full Test",
                        currentState = binFull,
                        onStateChange = { viewModel.setOperatorTestWitnessedBinFull(it); viewModel.autoUpdateSmePvResult() },
                        helpText = "Was the Bin Full sensor test performed by the operator?",
                        isNAToggleEnabled = true,
                        pvStatus = if (viewModel.pvRequired.value) rules.find { it.ruleId == "OPERATOR_TEST_BIN_FULL" }?.status?.name else null,
                        pvRules = rules.filter { it.ruleId == "OPERATOR_TEST_BIN_FULL" }
                    )

                    FormSpacer()

                    LabeledTriStateSwitchWithHelp(
                        label = "Reject Bin Door Test",
                        currentState = binDoor,
                        onStateChange = { viewModel.setOperatorTestWitnessedBinDoor(it); viewModel.autoUpdateSmePvResult() },
                        helpText = "Was the Bin Door sensor test performed by the operator?",
                        isNAToggleEnabled = true,
                        pvStatus = if (viewModel.pvRequired.value) rules.find { it.ruleId == "OPERATOR_TEST_BIN_DOOR" }?.status?.name else null,
                        pvRules = rules.filter { it.ruleId == "OPERATOR_TEST_BIN_DOOR" }
                    )

                    FormSpacer()

                    LabeledTriStateSwitchWithHelp(
                        label = "Air Failure Test",
                        currentState = airFail,
                        onStateChange = { viewModel.setOperatorTestWitnessedAirFail(it); viewModel.autoUpdateSmePvResult() },
                        helpText = "Was the Air Failure test performed by the operator?",
                        isNAToggleEnabled = true,
                        pvStatus = if (viewModel.pvRequired.value) rules.find { it.ruleId == "OPERATOR_TEST_AIR_FAIL" }?.status?.name else null,
                        pvRules = rules.filter { it.ruleId == "OPERATOR_TEST_AIR_FAIL" }
                    )

                    FormSpacer()

                    LabeledTriStateSwitchWithHelp(
                        label = "Pack Check Test",
                        currentState = packCheck,
                        onStateChange = { viewModel.setOperatorTestWitnessedPackCheck(it); viewModel.autoUpdateSmePvResult() },
                        helpText = "Was the Pack Check test performed by the operator?",
                        isNAToggleEnabled = true,
                        pvStatus = if (viewModel.pvRequired.value) rules.find { it.ruleId == "OPERATOR_TEST_PACK_CHECK" }?.status?.name else null,
                        pvRules = rules.filter { it.ruleId == "OPERATOR_TEST_PACK_CHECK" }
                    )

                    FormSpacer()

                    LabeledTriStateSwitchWithHelp(
                        label = "Speed Sensor Test",
                        currentState = speedSensor,
                        onStateChange = { viewModel.setOperatorTestWitnessedSpeedSensor(it); viewModel.autoUpdateSmePvResult() },
                        helpText = "Was the Speed Sensor test performed by the operator?",
                        isNAToggleEnabled = true,
                        pvStatus = if (viewModel.pvRequired.value) rules.find { it.ruleId == "OPERATOR_TEST_SPEED_SENSOR" }?.status?.name else null,
                        pvRules = rules.filter { it.ruleId == "OPERATOR_TEST_SPEED_SENSOR" }
                    )

                    FormSpacer()

                    LabeledTriStateSwitchWithHelp(
                        label = "Back Up Sensor Test",
                        currentState = backup,
                        onStateChange = { viewModel.setOperatorTestWitnessedBackup(it); viewModel.autoUpdateSmePvResult() },
                        helpText = "Was the Back Up test performed by the operator?",
                        isNAToggleEnabled = true,
                        pvStatus = if (viewModel.pvRequired.value) rules.find { it.ruleId == "OPERATOR_TEST_BACKUP" }?.status?.name else null,
                        pvRules = rules.filter { it.ruleId == "OPERATOR_TEST_BACKUP" }
                    )

                    FormSpacer()

                    LabeledTextFieldWithHelp(
                        label = "On Site SME Name",
                        value = smeName,
                        onValueChange = { viewModel.setSmeName(it); viewModel.autoUpdateSmePvResult() },
                        helpText = "Enter the name of the SME on site.",
                        isNAToggleEnabled = false,
                        pvStatus = if (viewModel.pvRequired.value) rules.find { it.ruleId == "SME_NAME" }?.status?.name else null,
                        pvRules = rules.filter { it.ruleId == "SME_NAME" },
                        maxLength = 25
                    )

                    FormSpacer()
                }

                if (viewModel.pvRequired.value) {
                    PvSectionSummaryCard(title = "Operator Test P.V. Summary", rules = rules)
                    FormSpacer()
                }

                LabeledTextFieldWithHelp(
                    label = "Engineer Comments",
                    value = notes,
                    onValueChange = viewModel::setSmeEngineerNotes,
                    helpText = "Relevant notes for this section.",
                    isNAToggleEnabled = false,
                    maxLength = 50
                )

                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}
