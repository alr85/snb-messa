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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.mecca.calibrationLogic.metalDetectorConveyor.autoUpdateFerrousPvResult
import com.example.mecca.calibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import com.example.mecca.formModules.CalibrationHeader
import com.example.mecca.formModules.LabeledFourOptionRadioWithHelp
import com.example.mecca.formModules.LabeledTextFieldWithHelp
import com.example.mecca.formModules.LabeledTwoTextInputsWithHelp
import com.example.mecca.formModules.LabeledYesNoSegmentedSwitchAndTextInputWithHelp
import com.example.mecca.formModules.YesNoState
import com.example.mecca.ui.theme.FormSpacer
import com.example.mecca.ui.theme.ScrollableWithScrollbar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalMetalDetectorConveyorFerrousTest(
    viewModel: CalibrationMetalDetectorConveyorViewModel
) {
    // Pull state from VM
    val sensitivityAsLeftFerrous by viewModel.sensitivityAsLeftFerrous
    val sampleCertificateNumberFerrous by viewModel.sampleCertificateNumberFerrous

    val peakSignalLeading by viewModel.peakSignalFerrousLeading
    val peakSignalMiddle by viewModel.peakSignalFerrousMiddle
    val peakSignalTrailing by viewModel.peakSignalFerrousTrailing

    val detectLeading by viewModel.detectRejectFerrousLeading
    val detectMiddle by viewModel.detectRejectFerrousMiddle
    val detectTrailing by viewModel.detectRejectFerrousTrailing

    val engineerNotes by viewModel.ferrousTestEngineerNotes

    // Validation
    val isNextStepEnabled =
        sensitivityAsLeftFerrous.isNotBlank() &&
                sampleCertificateNumberFerrous.isNotBlank() &&
                (detectLeading != YesNoState.YES || peakSignalLeading.isNotBlank()) &&
                (detectMiddle != YesNoState.YES || peakSignalMiddle.isNotBlank()) &&
                (detectTrailing != YesNoState.YES || peakSignalTrailing.isNotBlank())

    LaunchedEffect(isNextStepEnabled) {
        viewModel.setCurrentScreenNextEnabled(isNextStepEnabled)
    }

    Column(Modifier.fillMaxSize()) {

        CalibrationHeader("Ferrous Sensitivity (As Left)")

        ScrollableWithScrollbar(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp),
        ) {
            Column {

                Spacer(Modifier.height(6.dp))

                //-----------------------------------------------------
                //  Achieved Sensitivity + Certificate
                //-----------------------------------------------------
                LabeledTwoTextInputsWithHelp(
                    label = "Achieved Sensitivity & Certificate",
                    firstInputLabel = "Size",
                    firstInputValue = sensitivityAsLeftFerrous,
                    onFirstInputValueChange = {
                        viewModel.setSensitivityAsLeftFerrous(it)

                        if (it == "N/A") {
                            viewModel.disableFerrousTest()
                        } else {
                            viewModel.enableFerrousTest()
                        }
                        viewModel.autoUpdateFerrousPvResult()
                    },
                    secondInputLabel = "Cert No.",
                    secondInputValue = sampleCertificateNumberFerrous,
                    onSecondInputValueChange = { newValue ->
                        viewModel.setSampleCertificateNumberFerrous(newValue)
                        viewModel.autoUpdateFerrousPvResult()
                    },
                    helpText = """
                        Enter the achieved Ferrous sensitivity and the certificate number.
                        
                        M&S Target: ${viewModel.sensitivityData.value?.FerrousTargetMM}mm  
                        Max Allowed: ${viewModel.sensitivityData.value?.FerrousMaxMM}mm
                    """.trimIndent(),
                    firstInputKeyboardType = KeyboardType.Number,
                    secondInputKeyboardType = KeyboardType.Text,
                    isNAToggleEnabled = true
                )

                FormSpacer()

                //-----------------------------------------------------
                //  If N/A – skip test section
                //-----------------------------------------------------
                if (sensitivityAsLeftFerrous != "N/A") {

                    LabeledYesNoSegmentedSwitchAndTextInputWithHelp(
                        label = "Detected & Rejected (Leading)",
                        currentState = detectLeading,
                        onStateChange = {
                            viewModel.setDetectRejectFerrousLeading(it)
                            viewModel.autoUpdateFerrousPvResult()
                        },
                        helpText = "Leading-edge test result & signal.",
                        inputLabel = "Produced Signal",
                        inputValue = peakSignalLeading,
                        onInputValueChange = {
                            viewModel.setPeakSignalFerrousLeading(it)
                            viewModel.autoUpdateFerrousPvResult()
                        }
                    )

                    FormSpacer()

                    LabeledYesNoSegmentedSwitchAndTextInputWithHelp(
                        label = "Detected & Rejected (Middle)",
                        currentState = detectMiddle,
                        onStateChange = {
                            viewModel.setDetectRejectFerrousMiddle(it)
                            viewModel.autoUpdateFerrousPvResult()
                        },
                        helpText = "Middle test result & signal.",
                        inputLabel = "Produced Signal",
                        inputValue = peakSignalMiddle,
                        onInputValueChange = {
                            viewModel.setPeakSignalFerrousMiddle(it)
                            viewModel.autoUpdateFerrousPvResult()
                        }
                    )

                    FormSpacer()

                    LabeledYesNoSegmentedSwitchAndTextInputWithHelp(
                        label = "Detected & Rejected (Trailing)",
                        currentState = detectTrailing,
                        onStateChange = {
                            viewModel.setDetectRejectFerrousTrailing(it)
                            viewModel.autoUpdateFerrousPvResult()
                        },
                        helpText = "Trailing-edge test result & signal.",
                        inputLabel = "Produced Signal",
                        inputValue = peakSignalTrailing,
                        onInputValueChange = {
                            viewModel.setPeakSignalFerrousTrailing(it)
                            viewModel.autoUpdateFerrousPvResult()
                        }
                    )
                }

                FormSpacer()

                //-----------------------------------------------------
                //  PV Result (if required)
                //-----------------------------------------------------
                if (viewModel.pvRequired.value) {
                    LabeledFourOptionRadioWithHelp(
                        label = "P.V. Result",
                        value = viewModel.ferrousTestPvResult.value,
                        onValueChange = viewModel::setFerrousTestPvResult,
                        helpText = """
                            Auto-Pass rules:
                            • Achieved sensitivity ≤ Max Allowed
                            • Certificate No. entered
                            • All three detection tests = Yes
                            • All produced signals entered
                            
                            Otherwise auto-fail. You may override manually.
                        """.trimIndent(),
                        showNotFittedOption = false,
                        notFittedEnabled = false
                    )
                }

                FormSpacer()

                //-----------------------------------------------------
                //  Engineer Notes
                //-----------------------------------------------------
                LabeledTextFieldWithHelp(
                    label = "Engineer Notes",
                    value = engineerNotes,
                    onValueChange = viewModel::setFerrousTestEngineerNotes,
                    helpText = "Relevant notes for this section.",
                    isNAToggleEnabled = false
                )

                Spacer(Modifier.height(60.dp))
            }
        }
    }
}
