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
import androidx.compose.ui.graphics.Color
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
fun CalMetalDetectorConveyorStainlessTestAsFound(
    viewModel: CalibrationMetalDetectorConveyorViewModel
) {
    // Pull state from VM
    val sensitivityAsFoundStainless by viewModel.sensitivityAsFoundStainless
    val sampleCert by viewModel.sampleCertificateNumberAsFoundStainless

    val peakLeading by viewModel.peakSignalAsFoundStainlessLeading
    val peakMiddle by viewModel.peakSignalAsFoundStainlessMiddle
    val peakTrailing by viewModel.peakSignalAsFoundStainlessTrailing

    val detectLeading by viewModel.detectRejectAsFoundStainlessLeading
    val detectMiddle by viewModel.detectRejectAsFoundStainlessMiddle
    val detectTrailing by viewModel.detectRejectAsFoundStainlessTrailing

    val notes by viewModel.stainlessTestAsFoundEngineerNotes
    
    val isConveyor by viewModel.isConveyor

    // Default hidden values to safe state if not a conveyor
    LaunchedEffect(isConveyor) {
        if (!isConveyor) {
            viewModel.setDetectRejectAsFoundStainlessMiddle(YesNoState.NA)
            viewModel.setPeakSignalAsFoundStainlessMiddle("N/A")
            viewModel.setDetectRejectAsFoundStainlessTrailing(YesNoState.NA)
            viewModel.setPeakSignalAsFoundStainlessTrailing("N/A")
        }
    }

    // Sensitivity Warning Logic
    val customerReq = viewModel.sensitivityRequirementStainless.value.replace(",", ".").toDoubleOrNull() ?: 0.0
    val achieved = sensitivityAsFoundStainless.replace(",", ".").toDoubleOrNull() ?: 0.0
    val isSensitivityWarning = achieved > customerReq && achieved > 0.0 && customerReq > 0.0

    // Validation
    val isNextStepEnabled =
        sensitivityAsFoundStainless.isNotBlank() &&
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

        CalibrationHeader("Stainless Sensitivity (As Found)")

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
                    firstInputValue = sensitivityAsFoundStainless,
                    onFirstInputValueChange = {
                        viewModel.setSensitivityAsFoundStainless(it)

                        if (it == "N/A") {
                            viewModel.disableStainlessAsFound()
                        } else {
                            viewModel.enableStainlessAsFound()
                        }

                    },
                    secondInputLabel = "Cert No.",
                    secondInputValue = sampleCert,
                    onSecondInputValueChange = { newValue ->
                        viewModel.setSampleCertificateNumberAsFoundStainless(newValue)
                    },
                    helpText = """
                        Enter the achieved Stainless sensitivity and the certificate number.
                        
                        Customer Requirement: ${viewModel.sensitivityRequirementStainless.value}mm
                        M&S Target: ${viewModel.sensitivityData.value?.stainless316TargetMM}mm  
                        Max Allowed: ${viewModel.sensitivityData.value?.stainless316MaxMM}mm
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
                            text = "⚠️ 'As Found' sensitivity ($achieved mm) is worse than Customer Requirement ($customerReq mm).",
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
                if (sensitivityAsFoundStainless != "N/A") {

                    LabeledYesNoSegmentedSwitchAndTextInputWithHelp(
                        label = if(isConveyor){ "Detected & Rejected (Leading)" } else {"Detected & Rejected"},
                        currentState = detectLeading,
                        onStateChange = {
                            viewModel.setDetectRejectAsFoundStainlessLeading(it)
                        },
                        helpText = if(isConveyor){ "Leading edge test result & signal." } else {"Test result & signal."},
                        inputLabel = "Produced Signal",
                        inputValue = peakLeading,
                        onInputValueChange = {
                            viewModel.setPeakSignalAsFoundStainlessLeading(it)
                        },
                        inputMaxLength = 12,
                    )

                    FormSpacer()
                    
                    if (isConveyor){
                        LabeledYesNoSegmentedSwitchAndTextInputWithHelp(
                            label = "Detected & Rejected (Middle)",
                            currentState = detectMiddle,
                            onStateChange = {
                                viewModel.setDetectRejectAsFoundStainlessMiddle(it)
                            },
                            helpText = "Middle test result & signal.",
                            inputLabel = "Produced Signal",
                            inputValue = peakMiddle,
                            onInputValueChange = {
                                viewModel.setPeakSignalAsFoundStainlessMiddle(it)
                            },
                            inputMaxLength = 12,
                        )

                        FormSpacer()

                        LabeledYesNoSegmentedSwitchAndTextInputWithHelp(
                            label = "Detected & Rejected (Trailing)",
                            currentState = detectTrailing,
                            onStateChange = {
                                viewModel.setDetectRejectAsFoundStainlessTrailing(it)
                            },
                            helpText = "Trailing-edge test result & signal.",
                            inputLabel = "Produced Signal",
                            inputValue = peakTrailing,
                            onInputValueChange = {
                                viewModel.setPeakSignalAsFoundStainlessTrailing(it)
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
                    onValueChange = viewModel::setStainlessTestAsFoundEngineerNotes,
                    helpText = "Relevant notes for this section.",
                    isNAToggleEnabled = false,
                    maxLength = 50,
                )

                Spacer(Modifier.height(60.dp))
            }
        }
    }
}
