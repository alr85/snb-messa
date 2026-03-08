package com.example.mecca.screens.service.mdCalibration

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.mecca.calibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import com.example.mecca.formModules.AnimatedActionPill
import com.example.mecca.formModules.CalibrationHeader
import com.example.mecca.formModules.LabeledTextFieldWithHelp
import com.example.mecca.formModules.PvRule
import com.example.mecca.formModules.PvRuleStatus
import com.example.mecca.ui.theme.FormSpacer
import com.example.mecca.ui.theme.ScrollableWithScrollbar


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalMetalDetectorConveyorSensitivityRequirements(
    viewModel: CalibrationMetalDetectorConveyorViewModel
) {
    val context = LocalContext.current
    val ferrous by viewModel.sensitivityRequirementFerrous
    val nonFerrous by viewModel.sensitivityRequirementNonFerrous
    val stainless by viewModel.sensitivityRequirementStainless
    val engineerNotes by viewModel.sensitivityRequirementEngineerNotes

    val pvRequired by viewModel.pvRequired
    val sensitivityData = viewModel.sensitivityData.value

    // Next enabled
    val isNextStepEnabled =
        ferrous.isNotBlank() &&
                nonFerrous.isNotBlank() &&
                stainless.isNotBlank()

    LaunchedEffect(isNextStepEnabled) {
        viewModel.setCurrentScreenNextEnabled(isNextStepEnabled)
    }

    Column(modifier = Modifier.fillMaxSize()) {

        CalibrationHeader("Customer Sensitivity Requirements")

        ScrollableWithScrollbar(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
        ) {
            Column {

                // Paste M&S targets button (centred)
                if (pvRequired) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        AnimatedActionPill(
                            text = "Paste M&S Targets",
                            icon = Icons.Outlined.ContentPasteGo,
                            onClick = { pasteMStargetSensitivities(context, viewModel) }
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                }

                LabeledTextFieldWithHelp(
                    label = "Ferrous (mm)",
                    value = ferrous,
                    onValueChange = { viewModel.setSensitivityRequirementFerrous(it.replace(",", ".")) },
                    helpText = """
                        Enter the customer requirement for Ferrous sensitivity
                        
                        M&S Target: ${sensitivityData?.ferrousTargetMM?.toString() ?: "N/A"} mm
                        M&S Max: ${sensitivityData?.ferrousMaxMM?.toString() ?: "N/A"} mm
                    """.trimIndent(),
                    keyboardType = KeyboardType.Decimal,
                    isNAToggleEnabled = true,
                    pvStatus = if (pvRequired) {
                        val fVal = ferrous.toDoubleOrNull()
                        val fMax = sensitivityData?.ferrousMaxMM
                        when {
                            ferrous == "N/A" -> "N/A"
                            ferrous.isBlank() -> "Fail"
                            fVal == null -> "Fail"
                            fMax != null && fVal <= fMax -> "Pass"
                            else -> "Warning"
                        }
                    } else null,
                    pvRules = if (pvRequired) {
                        val fVal = ferrous.toDoubleOrNull()
                        val fMax = sensitivityData?.ferrousMaxMM
                        val list = mutableListOf<PvRule>()
                        
                        if (ferrous == "N/A") {
                            list.add(PvRule("Requirement is marked as Not Applicable.", PvRuleStatus.NA))
                        } else {
                            list.add(PvRule("Entry must be a valid number.", if (fVal != null) PvRuleStatus.Pass else if (ferrous.isBlank()) PvRuleStatus.Fail else PvRuleStatus.Fail))
                            if (fMax != null) {
                                list.add(PvRule("Requirement must be $fMax mm or less (Retailer Standard).", when {
                                    fVal == null -> PvRuleStatus.Incomplete
                                    fVal <= fMax -> PvRuleStatus.Pass
                                    else -> PvRuleStatus.Incomplete
                                }))
                            }
                        }
                        list
                    } else emptyList(),
                    maxLength = 4,
                    showInputLabel = false
                )

                FormSpacer()

                LabeledTextFieldWithHelp(
                    label = "Non-Ferrous (mm)",
                    value = nonFerrous,
                    onValueChange = { viewModel.setSensitivityRequirementNonFerrous(it.replace(",", ".")) },
                    helpText = """
                        Enter the customer requirement for Non-Ferrous sensitivity
                        
                        M&S Target: ${sensitivityData?.nonFerrousTargetMM?.toString() ?: "N/A"} mm
                        M&S Max: ${sensitivityData?.nonFerrousMaxMM?.toString() ?: "N/A"} mm
                    """.trimIndent(),
                    keyboardType = KeyboardType.Decimal,
                    isNAToggleEnabled = true,
                    pvStatus = if (pvRequired) {
                        val nfVal = nonFerrous.toDoubleOrNull()
                        val nfMax = sensitivityData?.nonFerrousMaxMM
                        when {
                            nonFerrous == "N/A" -> "N/A"
                            nonFerrous.isBlank() -> "Fail"
                            nfVal == null -> "Fail"
                            nfMax != null && nfVal <= nfMax -> "Pass"
                            else -> "Warning"
                        }
                    } else null,
                    pvRules = if (pvRequired) {
                        val nfVal = nonFerrous.toDoubleOrNull()
                        val nfMax = sensitivityData?.nonFerrousMaxMM
                        val list = mutableListOf<PvRule>()
                        
                        if (nonFerrous == "N/A") {
                            list.add(PvRule("Requirement is marked as Not Applicable.", PvRuleStatus.NA))
                        } else {
                            list.add(PvRule("Entry must be a valid number.", if (nfVal != null) PvRuleStatus.Pass else if (nonFerrous.isBlank()) PvRuleStatus.Fail else PvRuleStatus.Fail))
                            if (nfMax != null) {
                                list.add(PvRule("Requirement must be $nfMax mm or less (Retailer Standard).", when {
                                    nfVal == null -> PvRuleStatus.Incomplete
                                    nfVal <= nfMax -> PvRuleStatus.Pass
                                    else -> PvRuleStatus.Incomplete
                                }))
                            }
                        }
                        list
                    } else emptyList(),
                    maxLength = 4,
                    showInputLabel = false
                )

                FormSpacer()

                LabeledTextFieldWithHelp(
                    label = "Stainless Steel (mm)",
                    value = stainless,
                    onValueChange = { viewModel.setSensitivityRequirementStainless(it.replace(",", ".")) },
                    helpText = """
                        Enter the customer requirement for Stainless Steel sensitivity
                        
                        M&S Target: ${sensitivityData?.stainless316TargetMM?.toString() ?: "N/A"} mm
                        M&S Max: ${sensitivityData?.stainless316MaxMM?.toString() ?: "N/A"} mm
                    """.trimIndent(),
                    keyboardType = KeyboardType.Decimal,
                    isNAToggleEnabled = true,
                    pvStatus = if (pvRequired) {
                        val sVal = stainless.toDoubleOrNull()
                        val sMax = sensitivityData?.stainless316MaxMM
                        when {
                            stainless == "N/A" -> "N/A"
                            stainless.isBlank() -> "Fail"
                            sVal == null -> "Fail"
                            sMax != null && sVal <= sMax -> "Pass"
                            else -> "Warning"
                        }
                    } else null,
                    pvRules = if (pvRequired) {
                        val sVal = stainless.toDoubleOrNull()
                        val sMax = sensitivityData?.stainless316MaxMM
                        val list = mutableListOf<PvRule>()
                        
                        if (stainless == "N/A") {
                            list.add(PvRule("Requirement is marked as Not Applicable.", PvRuleStatus.NA))
                        } else {
                            list.add(PvRule("Entry must be a valid number.", if (sVal != null) PvRuleStatus.Pass else if (stainless.isBlank()) PvRuleStatus.Fail else PvRuleStatus.Fail))
                            if (sMax != null) {
                                list.add(PvRule("Requirement must be $sMax mm or less (Retailer Standard).", when {
                                    sVal == null -> PvRuleStatus.Incomplete
                                    sVal <= sMax -> PvRuleStatus.Pass
                                    else -> PvRuleStatus.Incomplete
                                }))
                            }
                        }
                        list
                    } else emptyList(),
                    maxLength = 4,
                    showInputLabel = false
                )

                FormSpacer()

                LabeledTextFieldWithHelp(
                    label = "Engineer Notes",
                    value = engineerNotes,
                    onValueChange = viewModel::setSensitivityRequirementEngineerNotes,
                    helpText = "Enter any notes relevant to this section",
                    isNAToggleEnabled = false,
                    maxLength = 50,
                    showInputLabel = false
                )

                Spacer(Modifier.height(60.dp))
            }
        }
    }
}

private fun pasteMStargetSensitivities(context: Context, viewModel: CalibrationMetalDetectorConveyorViewModel) {
    val data = viewModel.sensitivityData.value
    if (data == null) {
        Toast.makeText(context, "⚠️ No M&S sensitivity targets found for this system.", Toast.LENGTH_SHORT).show()
        return
    }

    viewModel.setSensitivityRequirementFerrous(data.ferrousMaxMM.toString())
    viewModel.setSensitivityRequirementNonFerrous(data.nonFerrousMaxMM.toString())
    viewModel.setSensitivityRequirementStainless(data.stainless316MaxMM.toString())
}
