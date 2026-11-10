package com.snb.inspect.screens.service.cwCalibration

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
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
import com.snb.inspect.ui.theme.ScrollableWithScrollbar
import com.snb.inspect.util.CheckweigherCalculationResult

@Composable
fun CalCwDynamicTestAsFound(viewModel: CalibrationCheckweigherViewModel) {
    val passes = viewModel.dynamicPassesAsFound.value
    val targetPasses = viewModel.requiredPassesCount.value
    var inputPassesText by remember(targetPasses) { mutableStateOf(targetPasses.toString()) }

    val calcResult by viewModel.evaluatedCalculationResultAsFound.collectAsState()

    val validCount = passes.take(targetPasses).count { it.isNotBlank() }
    val isNextStepEnabled = viewModel.nominalQuantityAsFound.value.isNotBlank() && validCount >= targetPasses

    LaunchedEffect(isNextStepEnabled) {
        viewModel.setCurrentScreenNextEnabled(isNextStepEnabled)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        CalibrationHeader(label = "Dynamic Weighing Test ($targetPasses Passes)", isValid = isNextStepEnabled)

        ScrollableWithScrollbar(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Spacer(Modifier.height(4.dp))

                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Dynamic Test Configuration",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(Icons.Default.Calculate, contentDescription = null)
                        }

                        OutlinedTextField(
                            value = inputPassesText,
                            onValueChange = { raw ->
                                inputPassesText = raw.filter { it.isDigit() }
                                val parsed = inputPassesText.toIntOrNull()
                                if (parsed != null && parsed >= 10) {
                                    viewModel.setRequiredPassesCountInt(parsed.coerceAtMost(30))
                                }
                            },
                            label = { Text("Required Test Passes (Min 10, Max 30)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            isError = (inputPassesText.toIntOrNull() ?: 0) < 10,
                            modifier = Modifier.fillMaxWidth()
                        )
                        if ((inputPassesText.toIntOrNull() ?: 0) < 10 && inputPassesText.isNotBlank()) {
                            Text(
                                "⚠️ Required test passes must be a minimum of 10.",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            "Test Pack Parameters",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = viewModel.nominalQuantityAsFound.value,
                                onValueChange = viewModel::setNominalQuantityAsFound,
                                label = { Text("Nominal Qnom (g)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = viewModel.staticScaleWeightAsFound.value,
                                onValueChange = viewModel::setStaticScaleWeightAsFound,
                                label = { Text("Static Ref Pack (g)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = viewModel.beltSpeed.value,
                                onValueChange = viewModel::setBeltSpeed,
                                label = { Text("Belt Speed (m/min)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = viewModel.tareConfiguration.value,
                                onValueChange = viewModel::setTareConfiguration,
                                label = { Text("Tare Config (g)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Dynamic Passes (g)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f)
                            )
                            Surface(
                                color = if (validCount >= targetPasses) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    text = "$validCount / $targetPasses",
                                    color = if (validCount >= targetPasses) Color(0xFF2E7D32) else Color(0xFFE65100),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }

                        val rowCount = (targetPasses + 2) / 3
                        for (row in 0 until rowCount) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                for (col in 0 until 3) {
                                    val index = row * 3 + col
                                    if (index < targetPasses) {
                                        OutlinedTextField(
                                            value = passes.getOrElse(index) { "" },
                                            onValueChange = { viewModel.setDynamicPassAsFound(index, it) },
                                            label = { Text("P${index + 1}") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                            singleLine = true,
                                            modifier = Modifier.weight(1f)
                                        )
                                    } else {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }

                if (calcResult != null) {
                    CalculatedStatisticsSummaryCard(calcResult!!)
                }

                Spacer(Modifier.height(60.dp))
            }
        }
    }
}

@Composable
private fun CalculatedStatisticsSummaryCard(res: CheckweigherCalculationResult) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                "Dynamic Accuracy Statistics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ValueBox(title = "Mean (x̄)", value = res.mean?.let { "%.2fg".format(it) } ?: "—", modifier = Modifier.weight(1f))
                ValueBox(title = "Mean Error", value = res.meanDynamicError?.let { "%.2fg".format(it) } ?: "—", modifier = Modifier.weight(1f))
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ValueBox(title = "Sample SD (s)", value = res.sampleStdDev?.let { "%.3f".format(it) } ?: "—", modifier = Modifier.weight(1f))
                ValueBox(title = "Six Sigma (6s)", value = res.sixSigmaZoi?.let { "%.2f".format(it) } ?: "—", modifier = Modifier.weight(1f))
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ValueBox(title = "TNE", value = "%.2fg".format(res.tne), modifier = Modifier.weight(1f))
                ValueBox(title = "20% TNE Limit", value = "%.2fg".format(res.benchmark20PercentTne), modifier = Modifier.weight(1f))
                ValueBox(title = "25% TNE Threshold", value = "%.2fg".format(res.benchmark25PercentTne), modifier = Modifier.weight(1f))
            }

            if (res.exceeds25PercentTneLimit) {
                Surface(
                    color = Color(0xFFFFF3E0),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "⚠️ 6σ exceeds 25% of TNE (${"%.2f".format(res.sixSigmaZoi)} > ${"%.2f".format(res.benchmark25PercentTne)})",
                            color = Color(0xFFE65100),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            "Further assessment is required on the next step. Additional set-point allowance: ${res.additionalSetPointAllowance?.let { "%.2fg".format(it) } ?: "—"}",
                            color = Color(0xFFE65100),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ValueBox(title: String, value: String, modifier: Modifier = Modifier) {
    Surface(tonalElevation = 2.dp, shape = MaterialTheme.shapes.medium, modifier = modifier) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(title, style = MaterialTheme.typography.labelSmall)
            Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        }
    }
}
