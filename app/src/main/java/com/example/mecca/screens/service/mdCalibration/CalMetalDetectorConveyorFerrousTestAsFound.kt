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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.mecca.calibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import com.example.mecca.formModules.CalibrationHeader
import com.example.mecca.formModules.LabeledTextFieldWithHelp
import com.example.mecca.formModules.LabeledTwoTextInputsWithHelp
import com.example.mecca.formModules.LabeledYesNoSegmentedSwitchAndTextInputWithHelp
import com.example.mecca.formModules.YesNoState
import com.example.mecca.ui.theme.FormSpacer
import com.example.mecca.ui.theme.ScrollableWithScrollbar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalMetalDetectorConveyorFerrousTestAsFound(
    viewModel: CalibrationMetalDetectorConveyorViewModel
) {
    // Pull state from VM
    val sensitivityAsFoundFerrous by viewModel.sensitivityAsFoundFerrous
    val sampleCert by viewModel.sampleCertificateNumberAsFoundFerrous

    val peakLeading by viewModel.peakSignalAsFoundFerrousLeading
    val peakMiddle by viewModel.peakSignalAsFoundFerrousMiddle
    val peakTrailing by viewModel.peakSignalAsFoundFerrousTrailing

    val detectLeading by viewModel.detectRejectAsFoundFerrousLeading
    val detectMiddle by viewModel.detectRejectAsFoundFerrousMiddle
    val detectTrailing by viewModel.detectRejectAsFoundFerrousTrailing

    val notes by viewModel.ferrousTestAsFoundEngineerNotes
    
    val isConveyor by viewModel.isConveyor

    // Default hidden values to safe state if not a conveyor
    LaunchedEffect(isConveyor) {
        if (!isConveyor) {
            viewModel.setDetectRejectAsFoundFerrousMiddle(YesNoState.NA)
            viewModel.setPeakSignalAsFoundFerrousMiddle("N/A")
            viewModel.setDetectRejectAsFoundFerrousTrailing(YesNoState.NA)
            viewModel.setPeakSignalAsFoundFerrousTrailing("N/A")
        }
    }

    // Sensitivity Warning Logic
    val customerReq = viewModel.sensitivityRequirementFerrous.value.replace(",", ".").toDoubleOrNull() ?: 0.0
    val achieved = sensitivityAsFoundFerrous.replace(",", ".").toDoubleOrNull() ?: 0.0
    val isSensitivityWarning = achieved > customerReq && achieved > 0.0 && customerReq > 0.0

    // Validation - only check middle/trailing if it's a conveyor
    val isNextStepEnabled =
        sensitivityAsFoundFerrous.isNotBlank() &&
                sampleCert.isNotBlank() &&
                (detectLeading != YesNoState.YES || peakLeading.isNotBlank()) &&
                (!isConveyor || (
                    (detectMiddle != YesNoState.YES || peakMiddle.isNotBlank()) &&
                    (detectTrailing != YesNoState.YES || peakTrailing.isNotBlank())
                ))

    LaunchedEffect(isNextStepEnabled) {
        viewModel.setCurrentScreenNextEnabled(isNextStepEnabled)
    }

    Column(Modifier.fillMaxSize()) {

        CalibrationHeader("Ferrous Sensitivity (As Found)")

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
                    firstInputValue = sensitivityAsFoundFerrous,
                    onFirstInputValueChange = {
                        viewModel.setSensitivityAsFoundFerrous(it)

                        if (it == "N/A") {
                            viewModel.disableFerrousAsFound()
                        } else {
                            viewModel.enableFerrousAsFound()
                        }

                    },
                    secondInputLabel = "Cert No.",
                    secondInputValue = sampleCert,
                    onSecondInputValueChange = { newValue ->
                        viewModel.setSampleCertificateNumberAsFoundFerrous(newValue)
                    },
                    helpText = """
                        Enter the achieved Ferrous sensitivity and the certificate number.
                        
                        Customer Requirement: ${viewModel.sensitivityRequirementFerrous.value}mm
                        M&S Target: ${viewModel.sensitivityData.value?.ferrousTargetMM}mm  
                        Max Allowed: ${viewModel.sensitivityData.value?.ferrousMaxMM}mm
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
                if (sensitivityAsFoundFerrous != "N/A") {

                    LabeledYesNoSegmentedSwitchAndTextInputWithHelp(
                        label = if(isConveyor){ "Detected & Rejected (Leading)" } else {"Detected & Rejected"},
                        currentState = detectLeading,
                        onStateChange = {
                            viewModel.setDetectRejectAsFoundFerrousLeading(it)
                        },
                        helpText = if(isConveyor){ "Leading edge test result & signal." } else {"Test result & signal."},
                        inputLabel = "Produced Signal",
                        inputValue = peakLeading,
                        onInputValueChange = {
                            viewModel.setPeakSignalAsFoundFerrousLeading(it)
                        },
                        inputMaxLength = 12,
                    )

                    FormSpacer()
                    
                    if (isConveyor){
                        LabeledYesNoSegmentedSwitchAndTextInputWithHelp(
                            label = "Detected & Rejected (Middle)",
                            currentState = detectMiddle,
                            onStateChange = {
                                viewModel.setDetectRejectAsFoundFerrousMiddle(it)
                            },
                            helpText = "Middle test result & signal.",
                            inputLabel = "Produced Signal",
                            inputValue = peakMiddle,
                            onInputValueChange = {
                                viewModel.setPeakSignalAsFoundFerrousMiddle(it)
                            },
                            inputMaxLength = 12,
                        )

                        FormSpacer()

                        LabeledYesNoSegmentedSwitchAndTextInputWithHelp(
                            label = "Detected & Rejected (Trailing)",
                            currentState = detectTrailing,
                            onStateChange = {
                                viewModel.setDetectRejectAsFoundFerrousTrailing(it)
                            },
                            helpText = "Trailing-edge test result & signal.",
                            inputLabel = "Produced Signal",
                            inputValue = peakTrailing,
                            onInputValueChange = {
                                viewModel.setPeakSignalAsFoundFerrousTrailing(it)
                            },
                            inputMaxLength = 12,
                        )

                        FormSpacer()

                    }
                }

                //-----------------------------------------------------
                //  Engineer Notes
                //-----------------------------------------------------
                LabeledTextFieldWithHelp(
                    label = "Engineer Notes",
                    value = notes,
                    onValueChange = viewModel::setFerrousTestAsFoundEngineerNotes,
                    helpText = "Relevant notes for this section.",
                    isNAToggleEnabled = false,
                    maxLength = 50,
                )

                Spacer(Modifier.height(60.dp))
            }
        }
    }
}
