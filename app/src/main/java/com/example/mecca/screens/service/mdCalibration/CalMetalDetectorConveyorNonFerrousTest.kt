package com.example.mecca.screens.service.mdCalibration

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentPasteGo
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
import com.example.mecca.calibrationLogic.metalDetectorConveyor.autoUpdateNonFerrousPvResult
import com.example.mecca.calibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import com.example.mecca.formModules.AnimatedActionPill
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
fun CalMetalDetectorConveyorNonFerrousTest(
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
    
    val isConveyor by viewModel.isConveyor

    // Default hidden values to safe state if not a conveyor
    LaunchedEffect(isConveyor) {
        if (!isConveyor) {
            viewModel.setDetectRejectNonFerrousMiddle(YesNoState.NA)
            viewModel.setPeakSignalNonFerrousMiddle("N/A")
            viewModel.setDetectRejectNonFerrousTrailing(YesNoState.NA)
            viewModel.setPeakSignalNonFerrousTrailing("N/A")
        }
    }

    // Sensitivity Warning Logic
    val customerReq = viewModel.sensitivityRequirementNonFerrous.value.replace(",", ".").toDoubleOrNull() ?: 0.0
    val achieved = sensitivity.replace(",", ".").toDoubleOrNull() ?: 0.0
    val isSensitivityWarning = achieved > customerReq && achieved > 0.0 && customerReq > 0.0

    // Next validation
    val isNextStepEnabled =
        sensitivity.isNotBlank() &&
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

        CalibrationHeader("Non-Ferrous Sensitivity (As Left)")

        ScrollableWithScrollbar(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
        ) {
            Column {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    AnimatedActionPill(
                        text = "Copy from ‘As Found’",
                        icon = Icons.Outlined.ContentPasteGo,
                        onClick = { copyNonFerrousTestAsFoundToAsLeft(viewModel) }
                    )
                }

                Spacer(Modifier.height(6.dp))

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

                // Skip the D&R tests if N/A
                if (sensitivity != "N/A") {

                    LabeledYesNoSegmentedSwitchAndTextInputWithHelp(
                        label = if(isConveyor){ "Detected & Rejected (Leading)" } else {"Detected & Rejected"},
                        currentState = detectLeading,
                        onStateChange = {
                            viewModel.setDetectRejectNonFerrousLeading(it)
                            viewModel.autoUpdateNonFerrousPvResult()
                        },
                        helpText = if(isConveyor){ "Leading-edge test result & peak signal." } else {"Test result & peak signal."},
                        inputLabel = "Produced Signal",
                        inputValue = peakLeading,
                        onInputValueChange = {
                            viewModel.setPeakSignalNonFerrousLeading(it)
                            viewModel.autoUpdateNonFerrousPvResult()
                        },
                        inputMaxLength = 12
                    )

                    FormSpacer()

                    if (isConveyor) {
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
                            },
                            inputMaxLength = 12
                        )

                        FormSpacer()

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
                            },
                            inputMaxLength = 12
                        )

                        FormSpacer()
                    }

                }



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
                              • All required D&R = Yes
                              • All peak signals entered

                            Otherwise auto-fail.
                            You may override manually.
                        """.trimIndent(),
                        showNotFittedOption = false,
                        notFittedEnabled = false
                    )

                    FormSpacer()
                }



                LabeledTextFieldWithHelp(
                    label = "Engineer Notes",
                    value = notes,
                    onValueChange = viewModel::setNonFerrousTestEngineerNotes,
                    helpText = "Enter any notes relevant to this section",
                    isNAToggleEnabled = false,
                    maxLength = 50
                )

                Spacer(Modifier.height(60.dp))
            }
        }
    }
}

fun copyNonFerrousTestAsFoundToAsLeft(viewModel: CalibrationMetalDetectorConveyorViewModel) {
    viewModel.setSensitivityAsLeftNonFerrous(viewModel.sensitivityAsFoundNonFerrous.value)
    viewModel.setSampleCertificateNumberNonFerrous(viewModel.sampleCertificateNumberAsFoundNonFerrous.value)
    viewModel.setDetectRejectNonFerrousLeading(viewModel.detectRejectAsFoundNonFerrousLeading.value)
    viewModel.setPeakSignalNonFerrousLeading(viewModel.peakSignalAsFoundNonFerrousLeading.value)
    viewModel.setDetectRejectNonFerrousMiddle(viewModel.detectRejectAsFoundNonFerrousMiddle.value)
    viewModel.setPeakSignalNonFerrousMiddle(viewModel.peakSignalAsFoundNonFerrousMiddle.value)
    viewModel.setDetectRejectNonFerrousTrailing(viewModel.detectRejectAsFoundNonFerrousTrailing.value)
    viewModel.setPeakSignalNonFerrousTrailing(viewModel.peakSignalAsFoundNonFerrousTrailing.value)


}
