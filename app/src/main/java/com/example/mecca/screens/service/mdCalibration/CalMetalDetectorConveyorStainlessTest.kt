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
import com.example.mecca.calibrationLogic.metalDetectorConveyor.*
import com.example.mecca.calibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import com.example.mecca.formModules.*
import com.example.mecca.ui.theme.FormSpacer
import com.example.mecca.ui.theme.ScrollableWithScrollbar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalMetalDetectorConveyorStainlessTest(
    viewModel: CalibrationMetalDetectorConveyorViewModel
) {
    // Pull state from VM
    val sensitivityAsLeftStainless by viewModel.sensitivityAsLeftStainless
    val sampleCertificateNumberStainless by viewModel.sampleCertificateNumberStainless

    val peakSignalLeading by viewModel.peakSignalStainlessLeading
    val peakSignalMiddle by viewModel.peakSignalStainlessMiddle
    val peakSignalTrailing by viewModel.peakSignalStainlessTrailing

    val detectLeading by viewModel.detectRejectStainlessLeading
    val detectMiddle by viewModel.detectRejectStainlessMiddle
    val detectTrailing by viewModel.detectRejectStainlessTrailing

    val engineerNotes by viewModel.stainlessTestEngineerNotes

    val isConveyor by viewModel.isConveyor
    val pvRequired by viewModel.pvRequired



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
    val achieved = sensitivityAsLeftStainless.replace(",", ".").toDoubleOrNull() ?: 0.0
    val isSensitivityWarning = achieved > customerReq && achieved > 0.0 && customerReq > 0.0

    // Validation
    val isNextStepEnabled =
        sensitivityAsLeftStainless.isNotBlank() &&
                sampleCertificateNumberStainless.isNotBlank() &&
                (detectLeading != YesNoState.YES || peakSignalLeading.isNotBlank()) &&
                (!isConveyor || (
                    (detectMiddle != YesNoState.YES || peakSignalMiddle.isNotBlank()) &&
                    (detectTrailing != YesNoState.YES || peakSignalTrailing.isNotBlank())
                ))

    LaunchedEffect(isNextStepEnabled) {
        viewModel.setCurrentScreenNextEnabled(isNextStepEnabled)
    }

    // PV Rules Calculation
    val rules = viewModel.getStainlessPvRules()
    
    val sensitivityAndCertStatus = when {
        sensitivityAsLeftStainless == "N/A" -> "N/A"
        sampleCertificateNumberStainless.isBlank() -> "Fail"
        sensitivityAsLeftStainless.isBlank() -> "Fail"
        achieved > customerReq -> "Warning"
        else -> "Pass"
    }

    Column(Modifier.fillMaxSize()) {

        CalibrationHeader("Stainless Steel Sensitivity (As Left)")

        ScrollableWithScrollbar(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp),
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
                //  Achieved Sensitivity + Certificate
                //-----------------------------------------------------
                LabeledTwoTextInputsWithHelp(
                    label = "Achieved Sensitivity & Certificate",
                    firstInputLabel = "Size",
                    firstInputValue = sensitivityAsLeftStainless,
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
                    secondInputValue = sampleCertificateNumberStainless,
                    onSecondInputValueChange = {
                        viewModel.setSampleCertificateNumberStainless(it)
                        viewModel.autoUpdateStainlessPvResult()
                    },
                    helpText = """
                        Enter the achieved Stainless Steel sensitivity and the certificate number.
                        
                        Customer Requirement: ${viewModel.sensitivityRequirementStainless.value}mm
                        M&S Target: ${viewModel.sensitivityData.value?.stainless316TargetMM}mm  
                        Max Allowed: ${viewModel.sensitivityData.value?.stainless316MaxMM}mm
                    """.trimIndent(),
                    firstInputKeyboardType = KeyboardType.Decimal,
                    secondInputKeyboardType = KeyboardType.Text,
                    isNAToggleEnabled = true,
                    pvStatus = if (pvRequired) sensitivityAndCertStatus else null,
                    pvRules = rules.filter { it.description.contains("sensitivity", ignoreCase = true) || it.description.contains("Certificate", ignoreCase = true) },
                    firstMaxLength = 4,
                    secondMaxLength = 12
                )

                if (isSensitivityWarning) {
                    Spacer(Modifier.height(4.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.errorContainer, MaterialTheme.shapes.small).padding(8.dp)
                    ) {
                        Text(
                            text = "⚠️ Achieved sensitivity ($achieved mm) is worse than Customer Requirement ($customerReq mm).",
                            color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold
                        )
                    }
                }

                FormSpacer()

                // Detection tests
                if (sensitivityAsLeftStainless != "N/A") {
                    LabeledYesNoSegmentedSwitchAndTextInputWithHelp(
                        label = if(isConveyor){ "Detected & Rejected (Leading)" } else {"Detected & Rejected"},
                        currentState = detectLeading,
                        onStateChange = { 
                            viewModel.setDetectRejectStainlessLeading(it)
                            viewModel.autoUpdateStainlessPvResult() 
                        },
                        helpText = if(isConveyor){ "Leading edge test result & signal." } else {"Test result & signal."},
                        inputLabel = "Produced Signal",
                        inputValue = peakSignalLeading,
                        onInputValueChange = { 
                            viewModel.setPeakSignalStainlessLeading(it)
                            viewModel.autoUpdateStainlessPvResult() 
                        },
                        inputMaxLength = 12,
                        pvStatus = if (viewModel.pvRequired.value) {
                            rules.firstOrNull { it.description.contains("Leading", ignoreCase = true) }?.status?.name ?: "Incomplete"
                        } else null,
                        pvRules = rules.filter { it.description.contains("Leading", ignoreCase = true) }
                    )

                    FormSpacer()

                    if (isConveyor){
                        LabeledYesNoSegmentedSwitchAndTextInputWithHelp(
                            label = "Detected & Rejected (Middle)",
                            currentState = detectMiddle,
                            onStateChange = { 
                                viewModel.setDetectRejectStainlessMiddle(it)
                                viewModel.autoUpdateStainlessPvResult() 
                            },
                            helpText = "Middle test result & signal.",
                            inputLabel = "Produced Signal",
                            inputValue = peakSignalMiddle,
                            onInputValueChange = { 
                                viewModel.setPeakSignalStainlessMiddle(it)
                                viewModel.autoUpdateStainlessPvResult() 
                            },
                            inputMaxLength = 12,
                            pvStatus = if (pvRequired) {
                                rules.firstOrNull { it.description.contains("Middle", ignoreCase = true) }?.status?.name ?: "Incomplete"
                            } else null,
                            pvRules = rules.filter { it.description.contains("Middle", ignoreCase = true) }
                        )

                        FormSpacer()

                        LabeledYesNoSegmentedSwitchAndTextInputWithHelp(
                            label = "Detected & Rejected (Trailing)",
                            currentState = detectTrailing,
                            onStateChange = { 
                                viewModel.setDetectRejectStainlessTrailing(it)
                                viewModel.autoUpdateStainlessPvResult() 
                            },
                            helpText = "Trailing-edge test result & signal.",
                            inputLabel = "Produced Signal",
                            inputValue = peakSignalTrailing,
                            onInputValueChange = { 
                                viewModel.setPeakSignalStainlessTrailing(it)
                                viewModel.autoUpdateStainlessPvResult() 
                            },
                            inputMaxLength = 12,
                            pvStatus = if (pvRequired) {
                                rules.firstOrNull { it.description.contains("Trailing", ignoreCase = true) }?.status?.name ?: "Incomplete"
                            } else null,
                            pvRules = rules.filter { it.description.contains("Trailing", ignoreCase = true) }
                        )

                        FormSpacer()
                    }
                }

                // PV Summary Card
                if (pvRequired) {
                    PvSectionSummaryCard(
                        title = "Stainless Steel Test P.V. Summary",
                        rules = rules
                    )
                }

                // Engineer Notes
                LabeledTextFieldWithHelp(
                    label = "Engineer Notes",
                    value = engineerNotes,
                    onValueChange = viewModel::setStainlessTestEngineerNotes,
                    helpText = "Relevant notes for this section.",
                    isNAToggleEnabled = false,
                    maxLength = 50,
                )

                Spacer(Modifier.height(60.dp))
            }
        }
    }
}

fun copyStainlessTestAsFoundToAsLeft(viewModel: CalibrationMetalDetectorConveyorViewModel) {
    viewModel.setSensitivityAsLeftStainless(viewModel.sensitivityAsFoundStainless.value)
    viewModel.setSampleCertificateNumberStainless(viewModel.sampleCertificateNumberAsFoundStainless.value)
    viewModel.setDetectRejectStainlessLeading(viewModel.detectRejectAsFoundStainlessLeading.value)
    viewModel.setPeakSignalStainlessLeading(viewModel.peakSignalAsFoundStainlessLeading.value)
    viewModel.setDetectRejectStainlessMiddle(viewModel.detectRejectAsFoundStainlessMiddle.value)
    viewModel.setPeakSignalStainlessMiddle(viewModel.peakSignalAsFoundStainlessMiddle.value)
    viewModel.setDetectRejectStainlessTrailing(viewModel.detectRejectAsFoundStainlessTrailing.value)
    viewModel.setPeakSignalStainlessTrailing(viewModel.peakSignalAsFoundStainlessTrailing.value)
}
