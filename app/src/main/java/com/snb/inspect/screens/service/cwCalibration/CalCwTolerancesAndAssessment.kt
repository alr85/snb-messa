package com.snb.inspect.screens.service.cwCalibration

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.snb.inspect.calibrationViewModels.CalibrationCheckweigherViewModel
import com.snb.inspect.formModules.CalibrationHeader
import com.snb.inspect.formModules.LabeledDropdownWithHelp
import com.snb.inspect.formModules.LabeledTextFieldWithHelp
import com.snb.inspect.ui.theme.AppCard
import com.snb.inspect.ui.theme.FormSpacer
import com.snb.inspect.ui.theme.ScrollableWithScrollbar
import com.snb.inspect.util.OimlInspectionType
import com.snb.inspect.util.ToleranceMode

@Composable
fun CalCwTolerancesAndAssessment(viewModel: CalibrationCheckweigherViewModel) {
    val currentMode = viewModel.toleranceMode.value
    val isOimlCompliance = viewModel.oimlComplianceModeEnabled.value
    val limits by viewModel.evaluatedToleranceLimits.collectAsState()

    val calcResult by viewModel.evaluatedCalculationResultAsFound.collectAsState()
    val isAssessmentRequired = calcResult?.exceeds25PercentTneLimit == true
    val isResolved = viewModel.engineerSetPointAssessmentResolved.value
    val notes = viewModel.engineerSetPointAssessmentNotes.value

    val isNextStepEnabled = !isAssessmentRequired || (isResolved && notes.isNotBlank())

    LaunchedEffect(isNextStepEnabled) {
        viewModel.setCurrentScreenNextEnabled(isNextStepEnabled)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        CalibrationHeader(label = "Acceptance Tolerances & Assessment", isValid = isNextStepEnabled)

        ScrollableWithScrollbar(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Spacer(Modifier.height(4.dp))

                // Tier 2: OIML R 51 Compliance Mode Toggle Card
                AppCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "OIML R 51 Compliance Mode (Tier 2)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Enforces OIML R 51 sample run counts and locks tolerances to OIML R 51 Benchmark.",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Switch(
                                checked = isOimlCompliance,
                                onCheckedChange = { viewModel.setOimlComplianceModeEnabled(it) }
                            )
                        }
                    }
                }

                // Section 1: Selectable Presets (ToleranceMode)
                AppCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            "Tolerance Mode Preset",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        val modeOptions = ToleranceMode.values().map { it.label }
                        val selectedModeLabel = currentMode.label

                        LabeledDropdownWithHelp(
                            label = "Select Preset",
                            options = modeOptions,
                            selectedOption = selectedModeLabel,
                            onSelectionChange = { selected ->
                                val mode = ToleranceMode.values().find { it.label == selected } ?: ToleranceMode.PACKAGED_GOODS_TNE
                                viewModel.setToleranceMode(mode)
                            },
                            helpText = currentMode.description
                        )

                        // 1. MANUAL_SPEC Inputs
                        if (currentMode == ToleranceMode.MANUAL_SPEC && !isOimlCompliance) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = viewModel.manualMeanErrorValue.value,
                                    onValueChange = viewModel::setManualMeanErrorValue,
                                    label = { Text("Max Mean Error") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                LabeledDropdownWithHelp(
                                    label = "Unit",
                                    options = listOf("GRAMS", "PERCENT"),
                                    selectedOption = viewModel.manualMeanErrorUnit.value,
                                    onSelectionChange = viewModel::setManualMeanErrorUnit,
                                    helpText = "Unit for max mean error"
                                )
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = viewModel.manualStdDevValue.value,
                                    onValueChange = viewModel::setManualStdDevValue,
                                    label = { Text("Max Std Dev (1σ)") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                LabeledDropdownWithHelp(
                                    label = "Unit",
                                    options = listOf("GRAMS", "PERCENT"),
                                    selectedOption = viewModel.manualStdDevUnit.value,
                                    onSelectionChange = viewModel::setManualStdDevUnit,
                                    helpText = "Unit for max standard deviation"
                                )
                            }
                        }

                        // 2. DIVISION_BASED Inputs
                        if (currentMode == ToleranceMode.DIVISION_BASED && !isOimlCompliance) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = viewModel.staticDivisionMultiplier.value,
                                    onValueChange = viewModel::setStaticDivisionMultiplier,
                                    label = { Text("Static Multiplier (e.g. 1d)") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = viewModel.dynamicDivisionMultiplier.value,
                                    onValueChange = viewModel::setDynamicDivisionMultiplier,
                                    label = { Text("Dynamic Multiplier (e.g. 2d)") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                            }
                        }

                        // 3. PACKAGED_GOODS_TNE Inputs
                        if (currentMode == ToleranceMode.PACKAGED_GOODS_TNE && !isOimlCompliance) {
                            LabeledTextFieldWithHelp(
                                label = "Nominal Package Weight Qn (g)",
                                value = viewModel.nominalPackageWeightQn.value,
                                onValueChange = viewModel::setNominalPackageWeightQn,
                                helpText = "Target package weight used for UK Packaged Goods Regs 2006 TNE calculation."
                            )
                            LabeledTextFieldWithHelp(
                                label = "Max Dynamic Std Dev Limit (% of TNE)",
                                value = viewModel.stdDevPercentageOfTne.value,
                                onValueChange = viewModel::setStdDevPercentageOfTne,
                                helpText = "Default: 10% of TNE (configurable from 5% up to 20% of TNE)."
                            )
                        }

                        // 4. OIML_R51_BENCHMARK Inputs
                        if (currentMode == ToleranceMode.OIML_R51_BENCHMARK || isOimlCompliance) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                LabeledDropdownWithHelp(
                                    label = "Accuracy Class",
                                    options = listOf("XI", "XII", "XIII", "XIIII"),
                                    selectedOption = viewModel.accuracyClass.value,
                                    onSelectionChange = viewModel::setAccuracyClass,
                                    helpText = "OIML accuracy class"
                                )
                                LabeledDropdownWithHelp(
                                    label = "Designation (x)",
                                    options = listOf("0.5", "1.0", "2.0"),
                                    selectedOption = viewModel.designationFactorX.value,
                                    onSelectionChange = viewModel::setDesignationFactorX,
                                    helpText = "Factor x multiplier"
                                )
                            }
                            LabeledDropdownWithHelp(
                                label = "Inspection Type",
                                options = OimlInspectionType.values().map { it.label },
                                selectedOption = viewModel.inspectionType.value.label,
                                onSelectionChange = { selected ->
                                    val type = OimlInspectionType.values().find { it.label == selected } ?: OimlInspectionType.INITIAL_VERIFICATION
                                    viewModel.setInspectionType(type)
                                },
                                helpText = "Initial verification or In-Service inspection"
                            )
                        }
                    }
                }

                // Evaluated Limits Summary Card
                AppCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            "Evaluated Tolerance Limits",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        Text(
                            limits.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            DisplayPill(title = "Allowed Static MPE", value = "±%.2fg".format(limits.allowedStaticMpe), modifier = Modifier.weight(1f))
                            DisplayPill(title = "Allowed Dyn Mean Err", value = "±%.2fg".format(limits.allowedMaxMeanError), modifier = Modifier.weight(1f))
                            DisplayPill(title = "Allowed Max Std Dev", value = "%.2fg".format(limits.allowedMaxStdDev), modifier = Modifier.weight(1f))
                        }
                    }
                }

                if (calcResult != null) {
                    val r = calcResult!!
                    AppCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                "Equipment Suitability & ZOI Evaluation",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                DisplayPill(title = "Effective TNE", value = "%.2fg".format(r.tne), modifier = Modifier.weight(1f))
                                DisplayPill(title = "20% TNE Suitability", value = "%.2fg".format(r.benchmark20PercentTne), modifier = Modifier.weight(1f))
                                DisplayPill(title = "25% TNE ZOI", value = "%.2fg".format(r.benchmark25PercentTne), modifier = Modifier.weight(1f))
                            }

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                DisplayPill(title = "6σ (ZOI Band)", value = r.sixSigmaZoi?.let { "%.2fg".format(it) } ?: "—", modifier = Modifier.weight(1f))
                                DisplayPill(title = "Status", value = r.overallStatus.name, modifier = Modifier.weight(1f))
                            }
                        }
                    }

                    if (isAssessmentRequired) {
                        AppCard(
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = Color(0xFFFFF3E0)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(
                                    "⚠️ FURTHER ASSESSMENT REQUIRED",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE65100)
                                )

                                Text(
                                    "Six Sigma (6σ = ${r.sixSigmaZoi?.let { "%.2f".format(it) }}) exceeds 25% of TNE (${"%.2f".format(r.benchmark25PercentTne)}g). " +
                                            "This does not automatically fail the equipment, but requires assessing and documenting configured reject set-points.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFFE65100)
                                )

                                LabeledTextFieldWithHelp(
                                    label = "Engineer Reject Set-Point Assessment Notes",
                                    value = notes,
                                    onValueChange = viewModel::setEngineerSetPointAssessmentNotes,
                                    helpText = "Document assessment of configured reject thresholds and set-point allowances."
                                )

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = isResolved,
                                        onCheckedChange = viewModel::setEngineerSetPointAssessmentResolved
                                    )
                                    Text(
                                        "Assessment resolved and verified with customer",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(60.dp))
            }
        }
    }
}

@Composable
private fun DisplayPill(title: String, value: String, modifier: Modifier = Modifier) {
    AppCard(shape = MaterialTheme.shapes.medium, modifier = modifier) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(title, style = MaterialTheme.typography.labelSmall)
            Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        }
    }
}
