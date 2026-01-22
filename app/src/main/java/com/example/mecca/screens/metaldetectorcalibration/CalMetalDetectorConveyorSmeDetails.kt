package com.example.mecca.screens.metaldetectorcalibration

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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.mecca.calibrationLogic.metalDetectorConveyor.autoUpdateSmePvResult
import com.example.mecca.calibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import com.example.mecca.formModules.CalibrationHeader
import com.example.mecca.formModules.LabeledFourOptionRadioWithHelp
import com.example.mecca.formModules.LabeledTextFieldWithHelp
import com.example.mecca.formModules.LabeledTriStateSwitchWithHelp
import com.example.mecca.formModules.LabeledTwoTextInputsWithHelp
import com.example.mecca.formModules.YesNoState
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

    val smeName by viewModel.smeName
    val notes by viewModel.smeEngineerNotes

    // Next enabled
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

    Column(modifier = Modifier.fillMaxSize()) {

        CalibrationHeader("Operator Test")

        ScrollableWithScrollbar(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
        ) {
            Column {

                LabeledTriStateSwitchWithHelp(
                    label = "Operator Test Witnessed?",
                    currentState = operatorTestWitnessed,
                    onStateChange = { newState ->
                        viewModel.setOperatorTestWitnessed(newState)

                        if (newState == YesNoState.NA || newState == YesNoState.NO) {
                            viewModel.setOperatorName("N/A")
                            viewModel.setOperatorTestResultFerrous("N/A")
                            viewModel.setOperatorTestResultNonFerrous("N/A")
                            viewModel.setOperatorTestResultStainless("N/A")
                            viewModel.setOperatorTestResultLargeMetal("N/A")
                            viewModel.setOperatorTestResultCertNumberFerrous("N/A")
                            viewModel.setOperatorTestResultCertNumberNonFerrous("N/A")
                            viewModel.setOperatorTestResultCertNumberStainless("N/A")
                            viewModel.setOperatorTestResultCertNumberLargeMetal("N/A")
                            viewModel.setSmeName("N/A")
                        } else if (newState == YesNoState.YES) {
                            viewModel.setOperatorName("")
                            viewModel.setOperatorTestResultFerrous("")
                            viewModel.setOperatorTestResultNonFerrous("")
                            viewModel.setOperatorTestResultStainless("")
                            viewModel.setOperatorTestResultLargeMetal("")
                            viewModel.setOperatorTestResultCertNumberFerrous("")
                            viewModel.setOperatorTestResultCertNumberNonFerrous("")
                            viewModel.setOperatorTestResultCertNumberStainless("")
                            viewModel.setOperatorTestResultCertNumberLargeMetal("")
                            viewModel.setSmeName("")
                        }

                        viewModel.autoUpdateSmePvResult()
                    },
                    helpText = "If you witnessed an operator perform a successful sensitivity check, select Yes. Otherwise select No or N/A."
                )

                FormSpacer()

                if (operatorTestWitnessed == YesNoState.YES) {

                    LabeledTextFieldWithHelp(
                        label = "Operator Name",
                        value = operatorName,
                        onValueChange = {
                            viewModel.setOperatorName(it)
                            viewModel.autoUpdateSmePvResult()
                        },
                        helpText = "Enter the name of the operator in charge of this system",
                        isNAToggleEnabled = true
                    )

                    FormSpacer()

                    LabeledTwoTextInputsWithHelp(
                        label = "Ferrous Test",
                        firstInputLabel = "Size",
                        firstInputValue = ferrousSize,
                        onFirstInputValueChange = {
                            viewModel.setOperatorTestResultFerrous(it)
                            viewModel.autoUpdateSmePvResult()
                        },
                        secondInputLabel = "Certificate No.",
                        secondInputValue = ferrousCert,
                        onSecondInputValueChange = {
                            viewModel.setOperatorTestResultCertNumberFerrous(it)
                            viewModel.autoUpdateSmePvResult()
                        },
                        helpText = "Enter the operator test size and certificate number for Ferrous.",
                        firstInputKeyboardType = KeyboardType.Number,
                        secondInputKeyboardType = KeyboardType.Text,
                        isNAToggleEnabled = true
                    )

                    FormSpacer()

                    LabeledTwoTextInputsWithHelp(
                        label = "Non Ferrous Test",
                        firstInputLabel = "Size",
                        firstInputValue = nonFerrousSize,
                        onFirstInputValueChange = {
                            viewModel.setOperatorTestResultNonFerrous(it)
                            viewModel.autoUpdateSmePvResult()
                        },
                        secondInputLabel = "Certificate No.",
                        secondInputValue = nonFerrousCert,
                        onSecondInputValueChange = {
                            viewModel.setOperatorTestResultCertNumberNonFerrous(it)
                            viewModel.autoUpdateSmePvResult()
                        },
                        helpText = "Enter the operator test size and certificate number for Non-Ferrous.",
                        firstInputKeyboardType = KeyboardType.Number,
                        secondInputKeyboardType = KeyboardType.Text,
                        isNAToggleEnabled = true
                    )

                    FormSpacer()

                    LabeledTwoTextInputsWithHelp(
                        label = "Stainless Test",
                        firstInputLabel = "Size",
                        firstInputValue = stainlessSize,
                        onFirstInputValueChange = {
                            viewModel.setOperatorTestResultStainless(it)
                            viewModel.autoUpdateSmePvResult()
                        },
                        secondInputLabel = "Certificate No.",
                        secondInputValue = stainlessCert,
                        onSecondInputValueChange = {
                            viewModel.setOperatorTestResultCertNumberStainless(it)
                            viewModel.autoUpdateSmePvResult()
                        },
                        helpText = "Enter the operator test size and certificate number for Stainless.",
                        firstInputKeyboardType = KeyboardType.Number,
                        secondInputKeyboardType = KeyboardType.Text,
                        isNAToggleEnabled = true
                    )

                    FormSpacer()

                    LabeledTwoTextInputsWithHelp(
                        label = "Large Metal",
                        firstInputLabel = "Size",
                        firstInputValue = largeMetalSize,
                        onFirstInputValueChange = {
                            viewModel.setOperatorTestResultLargeMetal(it)
                            viewModel.autoUpdateSmePvResult()
                        },
                        secondInputLabel = "Certificate No.",
                        secondInputValue = largeMetalCert,
                        onSecondInputValueChange = {
                            viewModel.setOperatorTestResultCertNumberLargeMetal(it)
                            viewModel.autoUpdateSmePvResult()
                        },
                        helpText = "Enter the operator test size and certificate number for Large Metal.",
                        firstInputKeyboardType = KeyboardType.Number,
                        secondInputKeyboardType = KeyboardType.Text,
                        isNAToggleEnabled = true
                    )

                    FormSpacer()

                    LabeledTextFieldWithHelp(
                        label = "On Site SME Name",
                        value = smeName,
                        onValueChange = {
                            viewModel.setSmeName(it)
                            viewModel.autoUpdateSmePvResult()
                        },
                        helpText = "Enter the name of the SME currently on site",
                        isNAToggleEnabled = true
                    )
                }

                FormSpacer()

                if (viewModel.pvRequired.value) {
                    LabeledFourOptionRadioWithHelp(
                        label = "P.V. Result",
                        value = viewModel.smeTestPvResult.value,
                        onValueChange = viewModel::setSmeTestPvResult,
                        helpText = """
                            Auto-Pass rules (when PV required):
                              • Operator Test Witnessed = Yes
                              • Operator name entered
                              • SME name entered
                              • All four operator test sizes entered (Fe/NFe/SS/Large)
                              • All four certificate numbers entered
                              • Operator test sizes match engineer test sizes

                            If Witnessed = No / N/A → PV = N/A.
                            Otherwise auto-fail. You may override manually.
                        """.trimIndent(),
                        showNotFittedOption = false,
                        notFittedEnabled = false
                    )
                }

                FormSpacer()

                LabeledTextFieldWithHelp(
                    label = "Engineer Comments",
                    value = notes,
                    onValueChange = viewModel::setSmeEngineerNotes,
                    helpText = "Enter any notes relevant to this section",
                    isNAToggleEnabled = false
                )

                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}
