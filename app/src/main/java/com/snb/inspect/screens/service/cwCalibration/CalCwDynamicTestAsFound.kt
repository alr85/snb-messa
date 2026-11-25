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
import com.snb.inspect.ui.theme.AppCard
import com.snb.inspect.ui.theme.ScrollableWithScrollbar
import com.snb.inspect.util.DynamicRepeatabilityResult

@Composable
fun CalCwDynamicTestAsFound(viewModel: CalibrationCheckweigherViewModel) {
    val passes = viewModel.dynamicPassesAsFound.value
    val targetPasses = viewModel.requiredPassesCount.value
    var inputPassesText by remember(targetPasses) { mutableStateOf(targetPasses.toString()) }

    val isOimlCompliance = viewModel.oimlComplianceModeEnabled.value
    val evalLimits by viewModel.evaluatedToleranceLimits.collectAsState()
    val dynamicEval by viewModel.evaluatedDynamicRepeatabilityResult.collectAsState()

    val minRequiredRuns = dynamicEval?.minRequiredRuns ?: if (isOimlCompliance) 30 else 10
    val validCount = passes.take(targetPasses).count { it.isNotBlank() }

    val isNextStepEnabled = viewModel.nominalQuantityAsFound.value.isNotBlank() &&
            validCount >= targetPasses &&
            (!isOimlCompliance || validCount >= minRequiredRuns)

    LaunchedEffect(isNextStepEnabled) {
        viewModel.setCurrentScreenNextEnabled(isNextStepEnabled)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        CalibrationHeader(label = "Dynamic Repeatability (In-Motion Test)", isValid = isNextStepEnabled)

        ScrollableWithScrollbar(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Spacer(Modifier.height(4.dp))

                // OIML Compliance Banner if active
                if (isOimlCompliance) {
                    Surface(
                        color = Color(0xFFE3F2FD),
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "🛡️ OIML R 51 Compliance Mode Active",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1565C0),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                "Enforced Minimum Runs for current test load: $minRequiredRuns runs",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF1565C0)
                            )
                        }
                    }
                }

                AppCard(modifier = Modifier.fillMaxWidth()) {
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
                                    viewModel.setRequiredPassesCountInt(parsed.coerceAtMost(60))
                                }
                            },
                            label = { Text("Required Test Runs (Min $minRequiredRuns, Max 60)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            isError = (inputPassesText.toIntOrNull() ?: 0) < minRequiredRuns,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                AppCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            "Test Pack & Reference Parameters",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = viewModel.nominalQuantityAsFound.value,
                                onValueChange = viewModel::setNominalQuantityAsFound,
                                label = { Text("Reference Weight W_ref (g)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = viewModel.beltSpeed.value,
                                onValueChange = viewModel::setBeltSpeed,
                                label = { Text("Belt Speed (${viewModel.dynamicBeltSpeedUnit.value})") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Dynamic Runs Inputs Grid
                AppCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Dynamic Readings (g)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f)
                            )
                            Surface(
                                color = if (validCount >= minRequiredRuns) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    text = "$validCount / $targetPasses (Min $minRequiredRuns)",
                                    color = if (validCount >= minRequiredRuns) Color(0xFF2E7D32) else Color(0xFFE65100),
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
                                            label = { Text("Run ${index + 1}") },
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

                // Dynamic Statistics Summary
                if (dynamicEval != null) {
                    DynamicStatisticsCard(dynamicEval!!)
                }

                Spacer(Modifier.height(60.dp))
            }
        }
    }
}

@Composable
private fun DynamicStatisticsCard(res: DynamicRepeatabilityResult) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Dynamic Accuracy Calculations",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Surface(
                    color = if (res.isOverallPass) Color(0xFF2E7D32) else Color(0xFFC62828),
                    shape = MaterialTheme.shapes.extraSmall
                ) {
                    Text(
                        text = if (res.isOverallPass) "PASS" else "FAIL",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ValueBox(title = "Mean (W_mean)", value = "%.2fg".format(res.dynamicMean), modifier = Modifier.weight(1f))
                ValueBox(
                    title = "Mean Error (E_mean)",
                    value = "%.2fg".format(res.dynamicMeanError),
                    subtitle = "Limit: ±%.2fg".format(res.allowedMaxMeanError),
                    isPass = res.isMeanErrorPass,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ValueBox(
                    title = "Std Dev (1σ)",
                    value = "%.3fg".format(res.sampleStdDev),
                    subtitle = "Limit: %.3fg".format(res.allowedMaxStdDev),
                    isPass = res.isStdDevPass,
                    modifier = Modifier.weight(1f)
                )
                ValueBox(title = "3-Sigma (3σ)", value = "%.3fg".format(res.threeSigma), modifier = Modifier.weight(1f))
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ValueBox(
                    title = "Dynamic Accuracy Band (|E_mean| + 3σ)",
                    value = "%.2fg".format(res.dynamicAccuracyBand),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ValueBox(
    title: String,
    value: String,
    subtitle: String? = null,
    isPass: Boolean? = null,
    modifier: Modifier = Modifier
) {
    AppCard(
        shape = MaterialTheme.shapes.medium,
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = when (isPass) {
                true -> Color(0xFFE8F5E9)
                false -> Color(0xFFFFEBEE)
                null -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(title, style = MaterialTheme.typography.labelSmall)
            Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
    }
}
