package com.example.mecca.screens.metaldetectorcalibration

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.mecca.calibrationLogic.metalDetectorConveyor.autoUpdateNonFerrousPvResult
import com.example.mecca.calibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import com.example.mecca.formModules.CalibrationHeader
import com.example.mecca.formModules.LabeledFourOptionRadioWithHelp
import com.example.mecca.formModules.LabeledTextFieldWithHelp
import com.example.mecca.formModules.LabeledTriStateSwitchAndTextInputWithHelp
import com.example.mecca.formModules.LabeledTwoTextInputsWithHelp
import com.example.mecca.formModules.LabeledYesNoSegmentedSwitchAndTextInputWithHelp
import com.example.mecca.formModules.YesNoState
import com.example.mecca.ui.theme.ScrollableWithScrollbar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalMetalDetectorConveyorNonFerrousTest(
    navController: NavHostController,
    viewModel: CalibrationMetalDetectorConveyorViewModel
) {
    // VM state
    val sensitivity by viewModel.sensitivityAsLeftNonFerrous
    val sampleCert by viewModel.sampleCertificateNumberNonFerrous

    val peakLeading by viewModel.peakSignalNonFerrousLeading
    val peakMiddle by viewModel.peakSignalNonFerrousMiddle
    val peakTrailing by viewModel.peakSignalNonFerrousTrailing

    val detectLeading by viewModel.detectRejectNonFerrousLeading
    val detectMiddle by viewModel.detectRejectNonFerrousMiddle
    val detectTrailing by viewModel.detectRejectNonFerrousTrailing

    val notes by viewModel.nonFerrousTestEngineerNotes

    // Next validation
    val isNextStepEnabled =
        sensitivity.isNotBlank() &&
                sampleCert.isNotBlank() &&
                (detectLeading != YesNoState.YES || peakLeading.isNotBlank()) &&
                (detectMiddle != YesNoState.YES || peakMiddle.isNotBlank()) &&
                (detectTrailing != YesNoState.YES || peakTrailing.isNotBlank())

    LaunchedEffect(isNextStepEnabled) {
        viewModel.setCurrentScreenNextEnabled(isNextStepEnabled)
    }

    Column(Modifier.fillMaxSize()) {

        CalibrationHeader("Non-Ferrous Sensitivity (As Left)")

        ScrollableWithScrollbar(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
        ) {
            Column {

                // ⭐ Combined row (Achieved + Cert)
                LabeledTwoTextInputsWithHelp(
                    label = "Achieved Sensitivity & Certificate",
                    firstInputLabel = "Size",
                    firstInputValue = sensitivity,
                    onFirstInputValueChange = {
                        viewModel.setSensitivityAsLeftNonFerrous(it)
                        if (it == "N/A") {
                            viewModel.disableNonFerrousTest()
                        } else {
                            viewModel.enableNonFerrousTest()
                        }
                        viewModel.autoUpdateNonFerrousPvResult()
                    },
                    secondInputLabel = "Cert No.",
                    secondInputValue = sampleCert,
                    onSecondInputValueChange = {
                        viewModel.setSampleCertificateNumberNonFerrous(it)
                        viewModel.autoUpdateNonFerrousPvResult()
                    },
                    helpText = """
                        Enter the achieved Non-Ferrous sensitivity and the certificate number.
                        
                        M&S Target: ${viewModel.sensitivityData.value?.NonFerrousTargetMM}mm  
                        Max Allowed: ${viewModel.sensitivityData.value?.NonFerrousMaxMM}mm
                    """.trimIndent(),
                    firstInputKeyboardType = KeyboardType.Number,
                    secondInputKeyboardType = KeyboardType.Text,
                    isNAToggleEnabled = true
                )

                Spacer(Modifier.height(16.dp))

                // Skip the D&R tests if N/A
                if (sensitivity != "N/A") {

                    LabeledYesNoSegmentedSwitchAndTextInputWithHelp(
                        label = "Detected & Rejected (Leading)",
                        currentState = detectLeading,
                        onStateChange = {
                            viewModel.setDetectRejectNonFerrousLeading(it)
                            viewModel.autoUpdateNonFerrousPvResult()
                        },
                        helpText = "Leading-edge test result & peak signal.",
                        inputLabel = "Produced Signal",
                        inputValue = peakLeading,
                        onInputValueChange = {
                            viewModel.setPeakSignalNonFerrousLeading(it)
                            viewModel.autoUpdateNonFerrousPvResult()
                        }
                    )

                    Spacer(Modifier.height(16.dp))

                    LabeledYesNoSegmentedSwitchAndTextInputWithHelp(
                        label = "Detected & Rejected (Middle)",
                        currentState = detectMiddle,
                        onStateChange = {
                            viewModel.setDetectRejectNonFerrousMiddle(it)
                            viewModel.autoUpdateNonFerrousPvResult()
                        },
                        helpText = "Middle test result & peak signal.",
                        inputLabel = "Produced Signal",
                        inputValue = peakMiddle,
                        onInputValueChange = {
                            viewModel.setPeakSignalNonFerrousMiddle(it)
                            viewModel.autoUpdateNonFerrousPvResult()
                        }
                    )

                    Spacer(Modifier.height(16.dp))

                    LabeledYesNoSegmentedSwitchAndTextInputWithHelp(
                        label = "Detected & Rejected (Trailing)",
                        currentState = detectTrailing,
                        onStateChange = {
                            viewModel.setDetectRejectNonFerrousTrailing(it)
                            viewModel.autoUpdateNonFerrousPvResult()
                        },
                        helpText = "Trailing test result & peak signal.",
                        inputLabel = "Produced Signal",
                        inputValue = peakTrailing,
                        onInputValueChange = {
                            viewModel.setPeakSignalNonFerrousTrailing(it)
                            viewModel.autoUpdateNonFerrousPvResult()
                        }
                    )
                }

                Spacer(Modifier.height(16.dp))

                // ⭐ PV Result (only when required)
                if (viewModel.pvRequired.value) {
                    LabeledFourOptionRadioWithHelp(
                        label = "P.V. Result",
                        value = viewModel.nonFerrousTestPvResult.value,
                        onValueChange = viewModel::setNonFerrousTestPvResult,
                        helpText = """
                            Auto-Pass rules:
                              • Achieved Sensitivity ≤ M&S Max
                              • Certificate number entered
                              • All three D&R = Yes
                              • All peak signals entered

                            Otherwise auto-fail.
                            You may override manually.
                        """.trimIndent(),
                        showNotFittedOption = false,
                        notFittedEnabled = false
                    )
                }

                Spacer(Modifier.height(16.dp))

                LabeledTextFieldWithHelp(
                    label = "Engineer Notes",
                    value = notes,
                    onValueChange = viewModel::setNonFerrousTestEngineerNotes,
                    helpText = "Enter any notes relevant to this section",
                    isNAToggleEnabled = false
                )

                Spacer(Modifier.height(60.dp))
            }
        }
    }
}
