package com.example.mecca.screens.service.mdCalibration

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.KeyboardType
import com.example.mecca.calibrationLogic.metalDetectorConveyor.*
import com.example.mecca.calibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import com.example.mecca.formModules.*
import com.example.mecca.ui.theme.FormSpacer
import com.example.mecca.ui.theme.ScrollableWithScrollbar

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
                            viewModel.setOperatorTestWitnessedInfeed(YesNoState.NO)
                            viewModel.setOperatorTestWitnessedRejectConfirm(YesNoState.NO)
                            viewModel.setOperatorTestWitnessedBinFull(YesNoState.NO)
                            viewModel.setOperatorTestWitnessedBinDoor(YesNoState.NO)
                            viewModel.setOperatorTestWitnessedAirFail(YesNoState.NO)
                            viewModel.setOperatorTestWitnessedPackCheck(YesNoState.NO)
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
                        onSecondInputValueChange = { viewModel.setOperatorTestResultCertNumberFerrous(it); viewModel.autoUpdateSmePvResult() },
                        helpText = "Enter Ferrous size and certificate number.",
                        firstInputKeyboardType = KeyboardType.Decimal,
                        isNAToggleEnabled = false,
                        pvStatus = if (viewModel.pvRequired.value) rules.find { it.ruleId == "OPERATOR_TEST_FERR" }?.status?.name else null,
                        pvRules = rules.filter { it.ruleId == "OPERATOR_TEST_FERR" }
                    )

                    FormSpacer()

                    LabeledTwoTextInputsWithHelp(
                        label = "Non-Ferrous Test",
                        firstInputLabel = "Size (mm)",
                        firstInputValue = nonFerrousSize,
                        onFirstInputValueChange = { viewModel.setOperatorTestResultNonFerrous(it); viewModel.autoUpdateSmePvResult() },
                        secondInputLabel = "Cert No.",
                        secondInputValue = nonFerrousCert,
                        onSecondInputValueChange = { viewModel.setOperatorTestResultCertNumberNonFerrous(it); viewModel.autoUpdateSmePvResult() },
                        helpText = "Enter Non-Ferrous size and certificate number.",
                        firstInputKeyboardType = KeyboardType.Decimal,
                        isNAToggleEnabled = false,
                        pvStatus = if (viewModel.pvRequired.value) rules.find { it.ruleId == "OPERATOR_TEST_NON_FERR" }?.status?.name else null,
                        pvRules = rules.filter { it.ruleId == "OPERATOR_TEST_NON_FERR" }
                    )

                    FormSpacer()

                    LabeledTwoTextInputsWithHelp(
                        label = "Stainless Test",
                        firstInputLabel = "Size (mm)",
                        firstInputValue = stainlessSize,
                        onFirstInputValueChange = { viewModel.setOperatorTestResultStainless(it); viewModel.autoUpdateSmePvResult() },
                        secondInputLabel = "Cert No.",
                        secondInputValue = stainlessCert,
                        onSecondInputValueChange = { viewModel.setOperatorTestResultCertNumberStainless(it); viewModel.autoUpdateSmePvResult() },
                        helpText = "Enter Stainless size and certificate number.",
                        firstInputKeyboardType = KeyboardType.Decimal,
                        isNAToggleEnabled = false,
                        pvStatus = if (viewModel.pvRequired.value) rules.find { it.ruleId == "OPERATOR_TEST_STAINLESS" }?.status?.name else null,
                        pvRules = rules.filter { it.ruleId == "OPERATOR_TEST_STAINLESS" }
                    )

                    FormSpacer()

                    LabeledTwoTextInputsWithHelp(
                        label = "Large Metal",
                        firstInputLabel = "Size (mm)",
                        firstInputValue = largeMetalSize,
                        onFirstInputValueChange = { viewModel.setOperatorTestResultLargeMetal(it); viewModel.autoUpdateSmePvResult() },
                        secondInputLabel = "Cert No.",
                        secondInputValue = largeMetalCert,
                        onSecondInputValueChange = { viewModel.setOperatorTestResultCertNumberLargeMetal(it); viewModel.autoUpdateSmePvResult() },
                        helpText = "Enter Large Metal size and certificate number.",
                        firstInputKeyboardType = KeyboardType.Decimal,
                        isNAToggleEnabled = false,
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
