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
fun CalMetalDetectorConveyorNonFerrousTest(
    viewModel: CalibrationMetalDetectorConveyorViewModel
) {
    // Pull state from VM
    val sensitivityAsLeftNonFerrous by viewModel.sensitivityAsLeftNonFerrous
    val sampleCertificateNumberNonFerrous by viewModel.sampleCertificateNumberNonFerrous

    val peakSignalLeading by viewModel.peakSignalNonFerrousLeading
    val peakSignalMiddle by viewModel.peakSignalNonFerrousMiddle
    val peakSignalTrailing by viewModel.peakSignalNonFerrousTrailing

    val detectLeading by viewModel.detectRejectNonFerrousLeading
    val detectMiddle by viewModel.detectRejectNonFerrousMiddle
    val detectTrailing by viewModel.detectRejectNonFerrousTrailing

    val engineerNotes by viewModel.nonFerrousTestEngineerNotes

    val isConveyor by viewModel.isConveyor
    val pvRequired by viewModel.pvRequired


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
    val achieved = sensitivityAsLeftNonFerrous.replace(",", ".").toDoubleOrNull() ?: 0.0
    val isSensitivityWarning = achieved > customerReq && achieved > 0.0 && customerReq > 0.0

    // Validation
    val isNextStepEnabled =
        sensitivityAsLeftNonFerrous.isNotBlank() &&
                sampleCertificateNumberNonFerrous.isNotBlank() &&
                (detectLeading != YesNoState.YES || peakSignalLeading.isNotBlank()) &&
                (!isConveyor || (
                    (detectMiddle != YesNoState.YES || peakSignalMiddle.isNotBlank()) &&
                    (detectTrailing != YesNoState.YES || peakSignalTrailing.isNotBlank())
                ))

    LaunchedEffect(isNextStepEnabled) {
        viewModel.setCurrentScreenNextEnabled(isNextStepEnabled)
    }

    // PV Rules Calculation
    val rules = viewModel.getNonFerrousPvRules()

    val sensitivityAndCertStatus = when {
        // If the sensitivity value is explicitly "N/A", the indicator must be "N/A" (Grey)
        sensitivityAsLeftNonFerrous == "N/A" -> "N/A"

        // Otherwise, evaluate the specific rules for Compliance and Cert
        rules.filter { it.ruleId.contains("SENSITIVITY") || it.ruleId.contains("CERT") }
            .any { it.status == PvRuleStatus.Fail } -> "Fail"

        rules.filter { it.ruleId.contains("SENSITIVITY") || it.ruleId.contains("CERT") }
            .any { it.status == PvRuleStatus.Incomplete || it.status == PvRuleStatus.Warning } -> "Warning"

        else -> "Pass"
    }

    Column(Modifier.fillMaxSize()) {

        CalibrationHeader("Non-Ferrous Sensitivity (As Left)")

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
                        onClick = { copyNonFerrousTestAsFoundToAsLeft(viewModel) }
                    )
                }

                Spacer(Modifier.height(6.dp))

                //-----------------------------------------------------
                //  Achieved Sensitivity + Certificate
                //-----------------------------------------------------
                LabeledTwoTextInputsWithHelp(
                    label = "Achieved Sensitivity & Certificate",
                    firstInputLabel = "Size",
                    firstInputValue = sensitivityAsLeftNonFerrous,
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
                    secondInputValue = sampleCertificateNumberNonFerrous,
                    onSecondInputValueChange = { newValue ->
                        viewModel.setSampleCertificateNumberNonFerrous(newValue.uppercase())
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
                if (sensitivityAsLeftNonFerrous != "N/A") {

                    val leadingRules = rules.filter { it.ruleId.startsWith("NON_FERROUS_DR_LEADING") }

                    LabeledYesNoSegmentedSwitchAndTextInputWithHelp(
                        label = if(isConveyor){ "Detected & Rejected (Leading)" } else {"Detected & Rejected"},
                        currentState = detectLeading,
                        onStateChange = {
                            viewModel.setDetectRejectNonFerrousLeading(it)
                            viewModel.autoUpdateNonFerrousPvResult()
                        },
                        helpText = if(isConveyor){ "Leading edge test result & signal." } else {"Test result & signal."},
                        inputLabel = "Produced Signal",
                        inputValue = peakSignalLeading,
                        onInputValueChange = {
                            viewModel.setPeakSignalNonFerrousLeading(it)
                            viewModel.autoUpdateNonFerrousPvResult()
                        },
                        inputMaxLength = 12,
                        pvStatus = if (viewModel.pvRequired.value) leadingRules.calculateOverallStatus() else null,
                        pvRules = leadingRules
                    )

                    FormSpacer()

                    if (isConveyor){

                        val middleRules = rules.filter { it.ruleId.startsWith("NON_FERROUS_DR_MIDDLE") }

                        LabeledYesNoSegmentedSwitchAndTextInputWithHelp(
                            label = "Detected & Rejected (Middle)",
                            currentState = detectMiddle,
                            onStateChange = {
                                viewModel.setDetectRejectNonFerrousMiddle(it)
                                viewModel.autoUpdateNonFerrousPvResult()
                            },
                            helpText = "Middle test result & signal.",
                            inputLabel = "Produced Signal",
                            inputValue = peakSignalMiddle,
                            onInputValueChange = {
                                viewModel.setPeakSignalNonFerrousMiddle(it)
                                viewModel.autoUpdateNonFerrousPvResult()
                            },
                            inputMaxLength = 12,
                            pvStatus = if (viewModel.pvRequired.value) middleRules.calculateOverallStatus() else null,
                            pvRules = middleRules
                        )

                        FormSpacer()

                        val trailingRules = rules.filter { it.ruleId.startsWith("NON_FERROUS_DR_TRAILING") }

                        LabeledYesNoSegmentedSwitchAndTextInputWithHelp(
                            label = "Detected & Rejected (Trailing)",
                            currentState = detectTrailing,
                            onStateChange = {
                                viewModel.setDetectRejectNonFerrousTrailing(it)
                                viewModel.autoUpdateNonFerrousPvResult()
                            },
                            helpText = "Trailing-edge test result & signal.",
                            inputLabel = "Produced Signal",
                            inputValue = peakSignalTrailing,
                            onInputValueChange = {
                                viewModel.setPeakSignalNonFerrousTrailing(it)
                                viewModel.autoUpdateNonFerrousPvResult()
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
                        title = "Non-Ferrous Test P.V. Summary",
                        rules = rules
                    )

                }



                //-----------------------------------------------------
                //  Engineer Notes
                //-----------------------------------------------------
                LabeledTextFieldWithHelp(
                    label = "Engineer Notes",
                    value = engineerNotes,
                    onValueChange = viewModel::setNonFerrousTestEngineerNotes,
                    helpText = "Relevant notes for this section.",
                    isNAToggleEnabled = false,
                    maxLength = 50,
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
    viewModel.autoUpdateNonFerrousPvResult()
}
