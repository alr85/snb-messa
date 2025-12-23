package com.example.mecca.screens.metaldetectorcalibration


import androidx.compose.foundation.layout.Column
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
import com.example.mecca.calibrationLogic.metalDetectorConveyor.autoUpdateStainlessPvResult
import com.example.mecca.calibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import com.example.mecca.formModules.CalibrationHeader
import com.example.mecca.formModules.LabeledFourOptionRadioWithHelp
import com.example.mecca.formModules.LabeledTextFieldWithHelp
import com.example.mecca.formModules.LabeledTriStateSwitchAndTextInputWithHelp
import com.example.mecca.formModules.LabeledTwoTextInputsWithHelp
import com.example.mecca.formModules.YesNoState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalMetalDetectorConveyorStainlessTest(
    navController: NavHostController,
    viewModel: CalibrationMetalDetectorConveyorViewModel
) {
    val scrollState = rememberScrollState()

    val sensitivity by viewModel.sensitivityAsLeftStainless
    val sampleCert by viewModel.sampleCertificateNumberStainless

    val peakLeading by viewModel.peakSignalStainlessLeading
    val peakMiddle by viewModel.peakSignalStainlessMiddle
    val peakTrailing by viewModel.peakSignalStainlessTrailing

    val detectLeading by viewModel.detectRejectStainlessLeading
    val detectMiddle by viewModel.detectRejectStainlessMiddle
    val detectTrailing by viewModel.detectRejectStainlessTrailing

    val notes by viewModel.stainlessTestEngineerNotes

    // Validation for Next
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

        CalibrationHeader("Stainless Sensitivity (As Left)")

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp)
                .verticalScroll(scrollState)
                .imePadding()
        ) {

            //-----------------------------------------------------
            // Combined: Achieved Sensitivity + Certificate
            //-----------------------------------------------------
            LabeledTwoTextInputsWithHelp(
                label = "Achieved Sensitivity & Certificate",
                firstInputLabel = "Sensitivity",
                firstInputValue = sensitivity,
                onFirstInputValueChange = {
                    viewModel.setSensitivityAsLeftStainless(it)

                    if (it == "N/A") {
                        viewModel.disableStainlessTest()
                    }
                    else {
                        viewModel.enableStainlessTest()
                    }
                    viewModel.autoUpdateStainlessPvResult()
                },
                secondInputLabel = "Cert No.",
                secondInputValue = sampleCert,
                onSecondInputValueChange = {
                    viewModel.setSampleCertificateNumberStainless(it)
                    viewModel.autoUpdateStainlessPvResult()
                },
                helpText = """
                    Enter the achieved Stainless Steel sensitivity and the certificate number.
                    
                    M&S Target: ${viewModel.sensitivityData.value?.Stainless316TargetMM}mm
                    Max Allowed: ${viewModel.sensitivityData.value?.Stainless316MaxMM}mm
                """.trimIndent(),
                firstInputKeyboardType = KeyboardType.Number,
                secondInputKeyboardType = KeyboardType.Text,
                isNAToggleEnabled = true
            )

            //-----------------------------------------------------
            // Skip detection tests entirely if N/A
            //-----------------------------------------------------
            if (sensitivity != "N/A") {

                LabeledTriStateSwitchAndTextInputWithHelp(
                    label = "Detected & Rejected (Leading)",
                    currentState = detectLeading,
                    onStateChange = {
                        viewModel.setDetectRejectStainlessLeading(it)
                        viewModel.autoUpdateStainlessPvResult()
                    },
                    helpText = "Leading-edge test result & peak signal.",
                    inputLabel = "Produced Signal",
                    inputValue = peakLeading,
                    onInputValueChange = {
                        viewModel.setPeakSignalStainlessLeading(it)
                        viewModel.autoUpdateStainlessPvResult()
                    }
                )

                LabeledTriStateSwitchAndTextInputWithHelp(
                    label = "Detected & Rejected (Middle)",
                    currentState = detectMiddle,
                    onStateChange = {
                        viewModel.setDetectRejectStainlessMiddle(it)
                        viewModel.autoUpdateStainlessPvResult()
                    },
                    helpText = "Middle test result & peak signal.",
                    inputLabel = "Produced Signal",
                    inputValue = peakMiddle,
                    onInputValueChange = {
                        viewModel.setPeakSignalStainlessMiddle(it)
                        viewModel.autoUpdateStainlessPvResult()
                    }
                )

                LabeledTriStateSwitchAndTextInputWithHelp(
                    label = "Detected & Rejected (Trailing)",
                    currentState = detectTrailing,
                    onStateChange = {
                        viewModel.setDetectRejectStainlessTrailing(it)
                        viewModel.autoUpdateStainlessPvResult()
                    },
                    helpText = "Trailing test result & peak signal.",
                    inputLabel = "Produced Signal",
                    inputValue = peakTrailing,
                    onInputValueChange = {
                        viewModel.setPeakSignalStainlessTrailing(it)
                        viewModel.autoUpdateStainlessPvResult()
                    }
                )
            }

            Spacer(Modifier.height(16.dp))

            //-----------------------------------------------------
            // ⭐ PV RESULT (only when required)
            //-----------------------------------------------------
            if (viewModel.pvRequired.value) {
                LabeledFourOptionRadioWithHelp(
                    label = "P.V. Result",
                    value = viewModel.stainlessTestPvResult.value,
                    onValueChange = viewModel::setStainlessTestPvResult,
                    helpText = """
                        Auto-Pass rules:
                          • Achieved Sensitivity ≤ M&S Max
                          • Certificate number entered
                          • All three D&R = Yes
                          • All peak signals entered

                        Otherwise auto-fail. You may override manually.
                    """.trimIndent(),
                    showNotFittedOption = true,
                    notFittedEnabled = false
                )
            }

            Spacer(Modifier.height(16.dp))

            //-----------------------------------------------------
            // Notes
            //-----------------------------------------------------
            LabeledTextFieldWithHelp(
                label = "Engineer Notes",
                value = notes,
                onValueChange = viewModel::setStainlessTestEngineerNotes,
                helpText = "Enter any notes relevant to this section",
                isNAToggleEnabled = false
            )

            Spacer(Modifier.height(60.dp))
        }
    }
}
