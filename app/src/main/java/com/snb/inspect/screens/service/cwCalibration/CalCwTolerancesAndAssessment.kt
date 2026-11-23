package com.snb.inspect.screens.service.cwCalibration

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.snb.inspect.calibrationViewModels.CalibrationCheckweigherViewModel
import com.snb.inspect.formModules.CalibrationHeader
import com.snb.inspect.formModules.LabeledDropdownWithHelp
import com.snb.inspect.formModules.LabeledTextFieldWithHelp
import com.snb.inspect.ui.theme.FormSpacer
import com.snb.inspect.ui.theme.ScrollableWithScrollbar
import com.snb.inspect.util.CheckweigherStatus

@Composable
fun CalCwTolerancesAndAssessment(viewModel: CalibrationCheckweigherViewModel) {
    val calcResult by viewModel.evaluatedCalculationResultAsFound.collectAsState()
    val source = viewModel.toleranceSource.value

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

                LabeledDropdownWithHelp(
                    label = "Tolerance Source",
                    options = listOf("SNB Guidance (Packaged Goods Regulations)", "Manufacturer Tolerances", "Customer Tolerances"),
                    selectedOption = when (source) {
                        "MANUFACTURER" -> "Manufacturer Tolerances"
                        "CUSTOMER" -> "Customer Tolerances"
                        else -> "SNB Guidance (Packaged Goods Regulations)"
                    },
                    onSelectionChange = { selected ->
                        val code = when (selected) {
                            "Manufacturer Tolerances" -> "MANUFACTURER"
                            "Customer Tolerances" -> "CUSTOMER"
                            else -> "SNB_GUIDANCE"
                        }
                        viewModel.setToleranceSource(code)
                    },
                    helpText = "Select tolerance source. Manufacturer and Customer tolerances are entered manually."
                )
                FormSpacer()

                if (source == "MANUFACTURER") {
                    LabeledTextFieldWithHelp(
                        label = "Manufacturer TNE (g)",
                        value = viewModel.manufacturerTne.value,
                        onValueChange = viewModel::setManufacturerTne,
                        helpText = "Enter manufacturer-defined TNE limit."
                    )
                    FormSpacer()
                } else if (source == "CUSTOMER") {
                    LabeledTextFieldWithHelp(
                        label = "Customer TNE (g)",
                        value = viewModel.customerTne.value,
                        onValueChange = viewModel::setCustomerTne,
                        helpText = "Enter customer-defined TNE limit."
                    )
                    FormSpacer()
                }

                if (calcResult != null) {
                    val r = calcResult!!
                    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                "Tolerance & Benchmark Evaluation",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                DisplayPill(title = "Effective TNE", value = "%.2fg".format(r.tne), modifier = Modifier.weight(1f))
                                DisplayPill(title = "20% TNE Suitability", value = "%.2fg".format(r.benchmark20PercentTne), modifier = Modifier.weight(1f))
                                DisplayPill(title = "25% TNE Threshold", value = "%.2fg".format(r.benchmark25PercentTne), modifier = Modifier.weight(1f))
                            }

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                DisplayPill(title = "6σ (ZOI)", value = r.sixSigmaZoi?.let { "%.2fg".format(it) } ?: "—", modifier = Modifier.weight(1f))
                                DisplayPill(title = "Status", value = r.overallStatus.name, modifier = Modifier.weight(1f))
                            }
                        }
                    }

                    if (isAssessmentRequired) {
                        ElevatedCard(
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

                                if (r.additionalSetPointAllowance != null) {
                                    Surface(color = Color(0xFFFFE0B2), shape = MaterialTheme.shapes.small) {
                                        Text(
                                            "Calculated Additional Set-Point Allowance: ${"%.2fg".format(r.additionalSetPointAllowance)}",
                                            modifier = Modifier.padding(8.dp),
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }

                                LabeledTextFieldWithHelp(
                                    label = "Engineer Reject Set-Point Assessment",
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
                    } else {
                        Surface(
                            color = Color(0xFFE8F5E9),
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "✅ 6σ is within 25% TNE threshold. Assessment criteria met.",
                                color = Color(0xFF2E7D32),
                                modifier = Modifier.padding(16.dp),
                                fontWeight = FontWeight.Bold
                            )
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
    Surface(tonalElevation = 2.dp, shape = MaterialTheme.shapes.medium, modifier = modifier) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(title, style = MaterialTheme.typography.labelSmall)
            Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        }
    }
}
