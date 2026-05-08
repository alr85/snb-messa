package com.snb.inspect.screens.service.mdCalibration

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.snb.inspect.calibrationLogic.metalDetectorConveyor.*
import com.snb.inspect.calibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import com.snb.inspect.formModules.*
import com.snb.inspect.ui.theme.FormSpacer
import com.snb.inspect.ui.theme.ScrollableWithScrollbar

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

    val isConveyor by viewModel.isConveyor
    val pvRequired by viewModel.pvRequired


    // Default hidden values to safe state if not a conveyor
    LaunchedEffect(isConveyor) {
        if (!isConveyor) {
            viewModel.setDetectRejectFerrousMiddle(YesNoState.NA)
            viewModel.setPeakSignalFerrousMiddle("N/A")
            viewModel.setDetectRejectFerrousTrailing(YesNoState.NA)
            viewModel.setPeakSignalFerrousTrailing("N/A")
        }
    }

    // Sensitivity Warning Logic
    val customerReq = viewModel.sensitivityRequirementFerrous.value.replace(",", ".").toDoubleOrNull() ?: 0.0
    val achieved = sensitivityAsLeftFerrous.replace(",", ".").toDoubleOrNull() ?: 0.0
    val isSensitivityWarning = achieved > customerReq && achieved > 0.0 && customerReq > 0.0

    // Validation
    val isNextStepEnabled =
        sensitivityAsLeftFerrous.isNotBlank() &&
                sampleCertificateNumberFerrous.isNotBlank() &&
                (detectLeading != YesNoState.YES || peakSignalLeading.isNotBlank()) &&
                (!isConveyor || (
                    (detectMiddle != YesNoState.YES || peakSignalMiddle.isNotBlank()) &&
                    (detectTrailing != YesNoState.YES || peakSignalTrailing.isNotBlank())
                ))

    LaunchedEffect(isNextStepEnabled) {
        viewModel.setCurrentScreenNextEnabled(isNextStepEnabled)
    }

    // PV Rules Calculation
    val rules = viewModel.getFerrousPvRules()

    val sensitivityAndCertStatus = when {
        // If the sensitivity value is explicitly "N/A", the indicator must be "N/A" (Grey)
        sensitivityAsLeftFerrous == "N/A" -> "N/A"

        // Otherwise, evaluate the specific rules for Compliance and Cert
        rules.filter { it.ruleId.contains("SENSITIVITY") || it.ruleId.contains("CERT") }
            .any { it.status == PvRuleStatus.Fail } -> "Fail"

        rules.filter { it.ruleId.contains("SENSITIVITY") || it.ruleId.contains("CERT") }
            .any { it.status == PvRuleStatus.Incomplete || it.status == PvRuleStatus.Warning } -> "Warning"

        else -> "Pass"
    }

    Column(Modifier.fillMaxSize()) {

        CalibrationHeader("Ferrous Sensitivity (As Left)")

        ScrollableWithScrollbar(
            modifier = Modifier
                .fillMaxSize(),
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
                        onClick = { copyFerrousTestAsFoundToAsLeft(viewModel) }
                    )
                }

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
                        viewModel.setSampleCertificateNumberFerrous(newValue.uppercase())
                        viewModel.autoUpdateFerrousPvResult()
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
                    pvStatus = if (viewModel.pvRequired.value) sensitivityAndCertStatus else null,
                    pvRules = rules.filter {
                        it.ruleId.contains("SENSITIVITY") || it.ruleId.contains("CERT")
                    },
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
                if (sensitivityAsLeftFerrous != "N/A") {

                    val leadingRules = rules.filter { it.ruleId.startsWith("FERROUS_DR_LEADING") }

                    LabeledYesNoSegmentedSwitchAndTextInputWithHelp(
                        label = if(isConveyor){ "Detected & Rejected (Leading)" } else {"Detected & Rejected"},
                        currentState = detectLeading,
                        onStateChange = {
                            viewModel.setDetectRejectFerrousLeading(it)
                            viewModel.autoUpdateFerrousPvResult()
                        },
                        helpText = if(isConveyor){ "Leading edge test result & signal." } else {"Test result & signal."},
                        inputLabel = "Produced Signal",
                        inputValue = peakSignalLeading,
                        onInputValueChange = {
                            viewModel.setPeakSignalFerrousLeading(it)
                            viewModel.autoUpdateFerrousPvResult()
                        },
                        inputMaxLength = 12,
                        pvStatus = if (viewModel.pvRequired.value) leadingRules.calculateOverallStatus() else null,
                        pvRules = leadingRules
                    )

                    FormSpacer()

                    if (isConveyor){

                        val middleRules = rules.filter { it.ruleId.startsWith("FERROUS_DR_MIDDLE") }

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
                            },
                            inputMaxLength = 12,
                            pvStatus = if (viewModel.pvRequired.value) middleRules.calculateOverallStatus() else null,
                            pvRules = middleRules
                        )

                        FormSpacer()

                        val trailingRules = rules.filter { it.ruleId.startsWith("FERROUS_DR_TRAILING") }

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
                            },
                            inputMaxLength = 12,
                            pvStatus = if (viewModel.pvRequired.value) trailingRules.calculateOverallStatus() else null,
                            pvRules = trailingRules
                        )

                        FormSpacer()

                    }

                }



                //-----------------------------------------------------
                //  PV Summary Card (replacing the old radio selector)
                //-----------------------------------------------------
                if (pvRequired) {
                    PvSectionSummaryCard(
                        title = "Ferrous Test P.V. Summary",
                        rules = rules
                    )

                }



                //-----------------------------------------------------
                //  Engineer Notes
                //-----------------------------------------------------
                LabeledTextFieldWithHelp(
                    label = "Engineer Notes",
                    value = engineerNotes,
                    onValueChange = viewModel::setFerrousTestEngineerNotes,
                    helpText = "Relevant notes for this section.",
                    isNAToggleEnabled = false,
                    maxLength = 50,
                )

                Spacer(Modifier.height(60.dp))
            }
        }
    }

}


fun copyFerrousTestAsFoundToAsLeft(viewModel: CalibrationMetalDetectorConveyorViewModel) {
    viewModel.setSensitivityAsLeftFerrous(viewModel.sensitivityAsFoundFerrous.value)
    viewModel.setSampleCertificateNumberFerrous(viewModel.sampleCertificateNumberAsFoundFerrous.value)
    viewModel.setDetectRejectFerrousLeading(viewModel.detectRejectAsFoundFerrousLeading.value)
    viewModel.setPeakSignalFerrousLeading(viewModel.peakSignalAsFoundFerrousLeading.value)
    viewModel.setDetectRejectFerrousMiddle(viewModel.detectRejectAsFoundFerrousMiddle.value)
    viewModel.setPeakSignalFerrousMiddle(viewModel.peakSignalAsFoundFerrousMiddle.value)
    viewModel.setDetectRejectFerrousTrailing(viewModel.detectRejectAsFoundFerrousTrailing.value)
    viewModel.setPeakSignalFerrousTrailing(viewModel.peakSignalAsFoundFerrousTrailing.value)


}
