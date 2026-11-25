package com.snb.inspect.screens.service.cwCalibration

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.snb.inspect.calibrationViewModels.CalibrationCheckweigherViewModel
import com.snb.inspect.formModules.CalibrationHeader
import com.snb.inspect.ui.theme.AppCard
import com.snb.inspect.ui.theme.ScrollableWithScrollbar
import com.snb.inspect.util.DynamicEccentricityTestResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalCwDynamicEccentricityTest(viewModel: CalibrationCheckweigherViewModel) {
    val frontReading = viewModel.dynamicEccentricityFrontReadingAsFound.value
    val backReading = viewModel.dynamicEccentricityBackReadingAsFound.value
    val testLoadInput = viewModel.dynamicEccentricityTestWeightAsFound.value
    val staticRefWeight = viewModel.staticScaleWeightAsFound.value
    val grossWeight = viewModel.grossWeight.value

    val limits by viewModel.evaluatedToleranceLimits.collectAsState()
    val calcResult by viewModel.evaluatedDynamicEccentricityResultAsFound.collectAsState()

    val isNextStepEnabled = frontReading.isNotBlank() && backReading.isNotBlank()

    LaunchedEffect(isNextStepEnabled) {
        viewModel.setCurrentScreenNextEnabled(isNextStepEnabled)
    }

    val maxCapVal = viewModel.maxCapacity.value.toDoubleOrNull()
    val tareVal = viewModel.tareWeight.value.toDoubleOrNull() ?: 0.0
    val suggestedLoad = maxCapVal?.let { "%.1f".format((it / 3.0) + tareVal) }

    Column(modifier = Modifier.fillMaxSize()) {
        CalibrationHeader(
            label = "Dynamic Eccentricity (Off-Centre Weighing)",
            isValid = isNextStepEnabled
        )

        ScrollableWithScrollbar(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Spacer(Modifier.height(4.dp))

                // Procedure Guidance Card
                AppCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Dynamic Eccentricity Procedure",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text(
                            "1. Test load: Representative pack (~1/3 Max capacity + additive tare).\n" +
                            "2. Front position pass: Pass load dynamically towards the front edge of the conveyor (halfway between centre and front edge).\n" +
                            "3. Back position pass: Repeat test towards the back edge (halfway between centre and back edge).\n" +
                            "4. Assessment: Evaluated against global tolerance class.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Global Calibration Tolerance Class Card
                AppCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Global Calibration Tolerance Class",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Text(
                            limits.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        val eccLimitDisplay = if (limits.allowedStaticMpe >= 1e9) "Advisory (None)" else "±%.2fg".format(limits.allowedStaticMpe)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ValueBox(title = "Tolerance Mode", value = limits.mode.label, modifier = Modifier.weight(1f))
                            ValueBox(title = "Eccentricity Limit", value = eccLimitDisplay, modifier = Modifier.weight(1f))
                        }
                    }
                }

                // Test Parameters Card
                AppCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Test Load & Baseline Setup",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(Icons.Default.CompassCalibration, contentDescription = null)
                        }

                        if (suggestedLoad != null) {
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                                shape = MaterialTheme.shapes.small,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "💡 Recommended Test Load (1/3 Max + Tare): ${suggestedLoad}g",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(8.dp),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = testLoadInput,
                                onValueChange = viewModel::setDynamicEccentricityTestWeightAsFound,
                                label = { Text("Representative Pack Load (g)") },
                                placeholder = { Text(staticRefWeight.ifBlank { grossWeight.ifBlank { "e.g. 500" } }) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = viewModel.dynamicEccentricityCentreReadingAsFound.value,
                                onValueChange = viewModel::setDynamicEccentricityCentreReadingAsFound,
                                label = { Text("Centre Reading (g, Optional)") },
                                placeholder = { Text("Defaults to Test Load") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Off-Centre Dynamic Pass Inputs Card
                AppCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            "Off-Centre Dynamic Passes",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = frontReading,
                                onValueChange = viewModel::setDynamicEccentricityFrontReadingAsFound,
                                label = { Text("Front Edge Reading (g)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = backReading,
                                onValueChange = viewModel::setDynamicEccentricityBackReadingAsFound,
                                label = { Text("Back Edge Reading (g)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Evaluation Summary Card
                if (calcResult != null) {
                    EvaluationSummaryCard(calcResult!!)
                }

                Spacer(Modifier.height(60.dp))
            }
        }
    }
}

@Composable
private fun EvaluationSummaryCard(res: DynamicEccentricityTestResult) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Permissible Error Assessment",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    color = if (res.isPass) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = if (res.isPass) "PASS" else "FAIL",
                        color = if (res.isPass) Color(0xFF2E7D32) else Color(0xFFC62828),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ValueBox(title = "Test Load Baseline", value = "%.2fg".format(res.referenceWeight), modifier = Modifier.weight(1f))
                ValueBox(title = "Front Error", value = "%+.2fg".format(res.frontError), modifier = Modifier.weight(1f))
                ValueBox(title = "Back Error", value = "%+.2fg".format(res.backError), modifier = Modifier.weight(1f))
            }

            val isAdvisory = res.mpe >= 1e9
            val mpeDisplay = if (isAdvisory) "Advisory" else "±%.2fg".format(res.mpe)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ValueBox(title = "Max Deviation", value = "%.2fg".format(res.maxError), modifier = Modifier.weight(1f))
                ValueBox(title = "Allowed Limit", value = mpeDisplay, modifier = Modifier.weight(1f))
                ValueBox(
                    title = "Tolerance Rule",
                    value = res.toleranceDescription.ifBlank { "Global Class" },
                    modifier = Modifier.weight(1f)
                )
            }

            Surface(
                color = if (res.isPass) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                val summaryText = when {
                    isAdvisory -> "ℹ️ Advisory Mode: Max position error (${"%.2f".format(res.maxError)}g) reported for advisory purposes only (no limits enforced)."
                    res.isPass -> "✅ Compliant: Max position error (${"%.2f".format(res.maxError)}g) is within allowable limit (±${"%.2f".format(res.mpe)}g) per ${res.toleranceDescription}."
                    else -> "❌ Exceeds limit: Max position error (${"%.2f".format(res.maxError)}g) exceeds allowable limit (±${"%.2f".format(res.mpe)}g) per ${res.toleranceDescription}."
                }
                Text(
                    text = summaryText,
                    color = if (res.isPass) Color(0xFF2E7D32) else Color(0xFFC62828),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }
    }
}

@Composable
private fun ValueBox(title: String, value: String, modifier: Modifier = Modifier) {
    AppCard(shape = MaterialTheme.shapes.medium, modifier = modifier) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(title, style = MaterialTheme.typography.labelSmall)
            Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        }
    }
}
