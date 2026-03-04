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
import com.example.mecca.calibrationLogic.metalDetectorConveyor.autoUpdateStainlessPvResult
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
fun CalMetalDetectorConveyorStainlessTest(
    viewModel: CalibrationMetalDetectorConveyorViewModel
) {
    val sensitivity by viewModel.sensitivityAsLeftStainless
    val sampleCert by viewModel.sampleCertificateNumberStainless

    val peakLeading by viewModel.peakSignalStainlessLeading
    val peakMiddle by viewModel.peakSignalStainlessMiddle
    val peakTrailing by viewModel.peakSignalStainlessTrailing

    val detectLeading by viewModel.detectRejectStainlessLeading
    val detectMiddle by viewModel.detectRejectStainlessMiddle
    val detectTrailing by viewModel.detectRejectStainlessTrailing

    val notes by viewModel.stainlessTestEngineerNotes
    
    val isConveyor by viewModel.isConveyor

    // Default hidden values to safe state if not a conveyor
    LaunchedEffect(isConveyor) {
        if (!isConveyor) {
            viewModel.setDetectRejectStainlessMiddle(YesNoState.NA)
            viewModel.setPeakSignalStainlessMiddle("N/A")
            viewModel.setDetectRejectStainlessTrailing(YesNoState.NA)
            viewModel.setPeakSignalStainlessTrailing("N/A")
        }
    }

    // Sensitivity Warning Logic
    val customerReq = viewModel.sensitivityRequirementStainless.value.replace(",", ".").toDoubleOrNull() ?: 0.0
    val achieved = sensitivity.replace(",", ".").toDoubleOrNull() ?: 0.0
    val isSensitivityWarning = achieved > customerReq && achieved > 0.0 && customerReq > 0.0

    // Validation for Next
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

        CalibrationHeader("Stainless Sensitivity (As Left)")

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
                        onClick = { copyStainlessTestAsFoundToAsLeft(viewModel) }
                    )
                }

                Spacer(Modifier.height(6.dp))

                //-----------------------------------------------------
                // Combined: Achieved Sensitivity + Certificate
                //-----------------------------------------------------
                LabeledTwoTextInputsWithHelp(
                    label = "Achieved Sensitivity & Certificate",
                    firstInputLabel = "Size",
                    firstInputValue = sensitivity,
                    onFirstInputValueChange = {
                        viewModel.setSensitivityAsLeftStainless(it)

                        if (it == "N/A") {
                            viewModel.disableStainlessTest()
                        } else {
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
                        
                        M&S Target: ${viewModel.sensitivityData.value?.stainless316TargetMM}mm
                        Max Allowed: ${viewModel.sensitivityData.value?.stainless316MaxMM}mm
                    """.trimIndent(),
                    firstInputKeyboardType = KeyboardType.Number,
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
                // Skip detection tests entirely if N/A
                //-----------------------------------------------------
                if (sensitivity != "N/A") {

                    LabeledYesNoSegmentedSwitchAndTextInputWithHelp(
                        label = if(isConveyor){ "Detected & Rejected (Leading)" } else {"Detected & Rejected"},
                        currentState = detectLeading,
                        onStateChange = {
                            viewModel.setDetectRejectStainlessLeading(it)
                            viewModel.autoUpdateStainlessPvResult()
                        },
                        helpText = if(isConveyor){ "Leading-edge test result & peak signal." } else {"Test result & peak signal."},
                        inputLabel = "Produced Signal",
                        inputValue = peakLeading,
                        onInputValueChange = {
                            viewModel.setPeakSignalStainlessLeading(it)
                            viewModel.autoUpdateStainlessPvResult()
                        },
                        inputMaxLength = 12
                    )

                   FormSpacer()

                    if (isConveyor) {
                        LabeledYesNoSegmentedSwitchAndTextInputWithHelp(
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
                            },
                            inputMaxLength = 12
                        )

                        FormSpacer()

                        LabeledYesNoSegmentedSwitchAndTextInputWithHelp(
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
                            },
                            inputMaxLength = 12
                        )

                        FormSpacer()
                    }

                }



                //-----------------------------------------------------
                // PV RESULT (only when required)
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
                              • All required D&R = Yes
                              • All peak signals entered

                            Otherwise auto-fail. You may override manually.
                        """.trimIndent(),
                        showNotFittedOption = false,
                        notFittedEnabled = false
                    )

                    FormSpacer()
                }



                //-----------------------------------------------------
                // Notes
                //-----------------------------------------------------
                LabeledTextFieldWithHelp(
                    label = "Engineer Notes",
                    value = notes,
                    onValueChange = viewModel::setStainlessTestEngineerNotes,
                    helpText = "Enter any notes relevant to this section",
                    isNAToggleEnabled = false,
                    maxLength = 50
                )

                Spacer(Modifier.height(60.dp))
            }
        }
    }
}

fun copyStainlessTestAsFoundToAsLeft(viewModel: CalibrationMetalDetectorConveyorViewModel) {
    viewModel.setSensitivityAsLeftStainless(viewModel.sensitivityAsFoundNonFerrous.value)
    viewModel.setSampleCertificateNumberStainless(viewModel.sampleCertificateNumberAsFoundStainless.value)
    viewModel.setDetectRejectStainlessLeading(viewModel.detectRejectAsFoundStainlessLeading.value)
    viewModel.setPeakSignalStainlessLeading(viewModel.peakSignalAsFoundStainlessLeading.value)
    viewModel.setDetectRejectStainlessMiddle(viewModel.detectRejectAsFoundStainlessMiddle.value)
    viewModel.setPeakSignalStainlessMiddle(viewModel.peakSignalAsFoundStainlessMiddle.value)
    viewModel.setDetectRejectStainlessTrailing(viewModel.detectRejectAsFoundStainlessTrailing.value)
    viewModel.setPeakSignalStainlessTrailing(viewModel.peakSignalAsFoundStainlessTrailing.value)


}
