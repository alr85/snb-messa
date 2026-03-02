package com.example.mecca.screens.service.mdCalibration

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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.mecca.calibrationLogic.metalDetectorConveyor.autoUpdateFerrousPvResult
import com.example.mecca.calibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import com.example.mecca.core.InputTransforms
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
fun CalMetalDetectorConveyorNonFerrousTestAsFound(
    viewModel: CalibrationMetalDetectorConveyorViewModel
) {
    // Pull state from VM
    val sensitivityAsFoundNonFerrous by viewModel.sensitivityAsFoundNonFerrous
    val sampleCert by viewModel.sampleCertificateNumberAsFoundNonFerrous

    val peakLeading by viewModel.peakSignalAsFoundNonFerrousLeading
    val peakMiddle by viewModel.peakSignalAsFoundNonFerrousMiddle
    val peakTrailing by viewModel.peakSignalAsFoundNonFerrousTrailing

    val detectLeading by viewModel.detectRejectAsFoundNonFerrousLeading
    val detectMiddle by viewModel.detectRejectAsFoundNonFerrousMiddle
    val detectTrailing by viewModel.detectRejectAsFoundNonFerrousTrailing

    val notes by viewModel.nonFerrousTestAsFoundEngineerNotes




    // Sensitivity Warning Logic
    val customerReq = viewModel.sensitivityRequirementNonFerrous.value.replace(",", ".").toDoubleOrNull() ?: 0.0
    val achieved = sensitivityAsFoundNonFerrous.replace(",", ".").toDoubleOrNull() ?: 0.0
    val isSensitivityWarning = achieved > customerReq && achieved > 0.0 && customerReq > 0.0

    // Validation
    val isNextStepEnabled =
        sensitivityAsFoundNonFerrous.isNotBlank() &&
                sampleCert.isNotBlank() &&
                (detectLeading != YesNoState.YES || peakLeading.isNotBlank()) &&
                (detectMiddle != YesNoState.YES || peakMiddle.isNotBlank()) &&
                (detectTrailing != YesNoState.YES || peakTrailing.isNotBlank())

    LaunchedEffect(isNextStepEnabled) {
        viewModel.setCurrentScreenNextEnabled(isNextStepEnabled)
    }

    Column(Modifier.fillMaxSize()) {

        CalibrationHeader("Non Ferrous Sensitivity (As Found)")

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
                    firstInputValue = sensitivityAsFoundNonFerrous,
                    onFirstInputValueChange = {
                        viewModel.setSensitivityAsFoundNonFerrous(it)

                        if (it == "N/A") {
                            viewModel.disableNonFerrousAsFound()
                        } else {
                            viewModel.enableNonFerrousAsFound()
                        }

                    },
                    secondInputLabel = "Cert No.",
                    secondInputValue = sampleCert,
                    onSecondInputValueChange = { newValue ->
                        viewModel.setSampleCertificateNumberAsFoundNonFerrous(newValue)
                    },
                    helpText = """
                        Enter the achieved Non-Ferrous sensitivity and the certificate number.
                        
                        Customer Requirement: ${viewModel.sensitivityRequirementNonFerrous.value}mm
                        M&S Target: ${viewModel.sensitivityData.value?.nonFerrousTargetMM}mm  
                        Max Allowed: ${viewModel.sensitivityData.value?.nonFerrousMaxMM}mm
                    """.trimIndent(),
                    firstInputKeyboardType = KeyboardType.Decimal,
                    secondInputKeyboardType = KeyboardType.Text,
                    isNAToggleEnabled = true,
                    firstMaxLength = 4,
                    secondMaxLength = 12
                )

                if (isSensitivityWarning) {
                    Spacer(Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.errorContainer, MaterialTheme.shapes.small)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "⚠️ Achieved sensitivity ($achieved mm) is worse than Customer Requirement ($customerReq mm).",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                FormSpacer()

                //-----------------------------------------------------
                //  If N/A – skip test section
                //-----------------------------------------------------
                if (sensitivityAsFoundNonFerrous != "N/A") {

                    LabeledYesNoSegmentedSwitchAndTextInputWithHelp(
                        label = "Detected & Rejected (Leading)",
                        currentState = detectLeading,
                        onStateChange = {
                            viewModel.setDetectRejectAsFoundNonFerrousLeading(it)
                        },
                        helpText = "Leading-edge test result & signal.",
                        inputLabel = "Produced Signal",
                        inputValue = peakLeading,
                        onInputValueChange = {
                            viewModel.setPeakSignalAsFoundNonFerrousLeading(it)
                        },
                        inputMaxLength = 12,
                    )

                    FormSpacer()

                    LabeledYesNoSegmentedSwitchAndTextInputWithHelp(
                        label = "Detected & Rejected (Middle)",
                        currentState = detectMiddle,
                        onStateChange = {
                            viewModel.setDetectRejectAsFoundNonFerrousMiddle(it)
                        },
                        helpText = "Middle test result & signal.",
                        inputLabel = "Produced Signal",
                        inputValue = peakMiddle,
                        onInputValueChange = {
                            viewModel.setPeakSignalAsFoundNonFerrousMiddle(it)
                        },
                        inputMaxLength = 12,
                    )

                    FormSpacer()

                    LabeledYesNoSegmentedSwitchAndTextInputWithHelp(
                        label = "Detected & Rejected (Trailing)",
                        currentState = detectTrailing,
                        onStateChange = {
                            viewModel.setDetectRejectAsFoundNonFerrousTrailing(it)
                        },
                        helpText = "Trailing-edge test result & signal.",
                        inputLabel = "Produced Signal",
                        inputValue = peakTrailing,
                        onInputValueChange = {
                            viewModel.setPeakSignalAsFoundNonFerrousTrailing(it)
                        },
                        inputMaxLength = 12,
                    )

                    FormSpacer()


                }


                //-----------------------------------------------------
                //  Engineer Notes
                //-----------------------------------------------------
                LabeledTextFieldWithHelp(
                    label = "Engineer Notes",
                    value = notes,
                    onValueChange = viewModel::setNonFerrousTestAsFoundEngineerNotes,
                    helpText = "Relevant notes for this section.",
                    isNAToggleEnabled = false,
                    maxLength = 50,
                )

                Spacer(Modifier.height(60.dp))
            }
        }
    }
}
