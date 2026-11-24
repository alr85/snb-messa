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
import com.snb.inspect.formModules.LabeledDropdownWithHelp
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

    val toleranceSource = viewModel.dynamicEccentricityToleranceSource.value
    val manualType = viewModel.dynamicEccentricityManualType.value
    val manualValue = viewModel.dynamicEccentricityManualValue.value
    val divisionMult = viewModel.dynamicEccentricityDivisionMultiplier.value
    val tnePackWeight = viewModel.dynamicEccentricityTnePackWeight.value
    val oimlClass = viewModel.dynamicEccentricityOimlClass.value

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
                            "4. Assessment: Evaluated against selected tolerance source.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Tolerance Assessment Source Card
                AppCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Tolerance Assessment Source",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(Icons.Default.Tune, contentDescription = null)
                        }

                        val toleranceOptions = listOf(
                            "OIML R 51 Benchmark",
                            "Manual / Customer Spec",
                            "Division-Based (±Xd)",
                            "Packaged Goods (TNE Rule)"
                        )

                        val currentSelectedLabel = when (toleranceSource) {
                            "MANUAL_SPEC" -> "Manual / Customer Spec"
                            "DIVISION_BASED" -> "Division-Based (±Xd)"
                            "PACKAGED_GOODS" -> "Packaged Goods (TNE Rule)"
                            else -> "OIML R 51 Benchmark"
                        }

                        LabeledDropdownWithHelp(
                            label = "Tolerance Rule / Source",
                            options = toleranceOptions,
                            selectedOption = currentSelectedLabel,
                            onSelectionChange = { sel ->
                                val code = when (sel) {
                                    "Manual / Customer Spec" -> "MANUAL_SPEC"
                                    "Division-Based (±Xd)" -> "DIVISION_BASED"
                                    "Packaged Goods (TNE Rule)" -> "PACKAGED_GOODS"
                                    else -> "OIML_R51"
                                }
                                viewModel.setDynamicEccentricityToleranceSource(code)
                            },
                            helpText = "Select rule to evaluate pass/fail limit when machine specs or e values are unknown."
                        )

                        // Sub-options based on selected tolerance source
                        when (toleranceSource) {
                            "MANUAL_SPEC" -> {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Manual Spec Unit:", style = MaterialTheme.typography.labelMedium)
                                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                                        SegmentedButton(
                                            selected = manualType == "GRAMS",
                                            onClick = { viewModel.setDynamicEccentricityManualType("GRAMS") },
                                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                                        ) {
                                            Text("± Grams (±g)")
                                        }
                                        SegmentedButton(
                                            selected = manualType == "PERCENT",
                                            onClick = { viewModel.setDynamicEccentricityManualType("PERCENT") },
                                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                                        ) {
                                            Text("± Percent (±%)")
                                        }
                                    }

                                    OutlinedTextField(
                                        value = manualValue,
                                        onValueChange = viewModel::setDynamicEccentricityManualValue,
                                        label = { Text(if (manualType == "PERCENT") "Allowed Limit (±%)" else "Allowed Limit (±g)") },
                                        placeholder = { Text(if (manualType == "PERCENT") "e.g. 1.0" else "e.g. 1.5") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }

                            "DIVISION_BASED" -> {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Division Multiplier (Scale Interval d):", style = MaterialTheme.typography.labelMedium)
                                    val multipliers = listOf("1", "2", "3", "5")
                                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                                        multipliers.forEachIndexed { index, m ->
                                            SegmentedButton(
                                                selected = divisionMult == m,
                                                onClick = { viewModel.setDynamicEccentricityDivisionMultiplier(m) },
                                                shape = SegmentedButtonDefaults.itemShape(index = index, count = multipliers.size)
                                            ) {
                                                Text("±${m}d")
                                            }
                                        }
                                    }
                                }
                            }

                            "PACKAGED_GOODS" -> {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = tnePackWeight,
                                        onValueChange = viewModel::setDynamicEccentricityTnePackWeight,
                                        label = { Text("Target Pack Weight (g)") },
                                        placeholder = { Text(testLoadInput.ifBlank { staticRefWeight.ifBlank { grossWeight.ifBlank { "500" } } }) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Text(
                                        "App automatically calculates 10% of TNE as the permissible error limit.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            else -> { // "OIML_R51"
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("OIML R 51 Class Benchmark:", style = MaterialTheme.typography.labelMedium)
                                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                                        SegmentedButton(
                                            selected = oimlClass.contains("XIII(1)"),
                                            onClick = { viewModel.setDynamicEccentricityOimlClass("XIII(1)") },
                                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                                        ) {
                                            Text("Class XIII(1)")
                                        }
                                        SegmentedButton(
                                            selected = oimlClass.contains("XIII(2)"),
                                            onClick = { viewModel.setDynamicEccentricityOimlClass("XIII(2)") },
                                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                                        ) {
                                            Text("Class XIII(2) In-Service")
                                        }
                                    }
                                }
                            }
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

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ValueBox(title = "Max Deviation", value = "%.2fg".format(res.maxError), modifier = Modifier.weight(1f))
                ValueBox(title = "Allowed Limit", value = "±%.2fg".format(res.mpe), modifier = Modifier.weight(1f))
                ValueBox(
                    title = "Tolerance Rule",
                    value = res.toleranceDescription.ifBlank { "OIML R 51" },
                    modifier = Modifier.weight(1f)
                )
            }

            Surface(
                color = if (res.isPass) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (res.isPass) {
                        "✅ Compliant: Max position error (${"%.2f".format(res.maxError)}g) is within allowable limit (±${"%.2f".format(res.mpe)}g) per ${res.toleranceDescription}."
                    } else {
                        "❌ Exceeds limit: Max position error (${"%.2f".format(res.maxError)}g) exceeds allowable limit (±${"%.2f".format(res.mpe)}g) per ${res.toleranceDescription}."
                    },
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
