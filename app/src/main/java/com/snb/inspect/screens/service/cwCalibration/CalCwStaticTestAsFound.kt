package com.snb.inspect.screens.service.cwCalibration

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Repeat
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
import com.snb.inspect.util.OffCentreTestResult
import com.snb.inspect.util.RepeatabilityTestResult

@Composable
fun CalCwStaticTestAsFound(viewModel: CalibrationCheckweigherViewModel) {
    val offCentreResult by viewModel.evaluatedOffCentreResultAsFound.collectAsState()
    val repeatabilityResult by viewModel.evaluatedRepeatabilityResultAsFound.collectAsState()

    val offCentreCentre = viewModel.offCentreCentreAsFound.value
    val offCentreC1 = viewModel.offCentreCorner1AsFound.value
    val offCentreC2 = viewModel.offCentreCorner2AsFound.value
    val offCentreC3 = viewModel.offCentreCorner3AsFound.value
    val offCentreC4 = viewModel.offCentreCorner4AsFound.value

    val repR1 = viewModel.repeatabilityReading1AsFound.value
    val repR2 = viewModel.repeatabilityReading2AsFound.value
    val repR3 = viewModel.repeatabilityReading3AsFound.value

    val isNextStepEnabled = offCentreCentre.isNotBlank() &&
            offCentreC1.isNotBlank() &&
            offCentreC2.isNotBlank() &&
            offCentreC3.isNotBlank() &&
            offCentreC4.isNotBlank() &&
            repR1.isNotBlank() &&
            repR2.isNotBlank() &&
            repR3.isNotBlank()

    LaunchedEffect(isNextStepEnabled) {
        viewModel.setCurrentScreenNextEnabled(isNextStepEnabled)
    }

    LaunchedEffect(Unit) {
        val defaultWeight = viewModel.staticScaleWeightAsFound.value.ifBlank {
            viewModel.grossWeight.value.ifBlank { viewModel.checkweigherWeightAsFound.value }
        }
        if (viewModel.offCentreTestWeightAsFound.value.isBlank() && defaultWeight.isNotBlank()) {
            viewModel.setOffCentreTestWeightAsFound(defaultWeight)
        }
        if (viewModel.repeatabilityTestWeightAsFound.value.isBlank() && defaultWeight.isNotBlank()) {
            viewModel.setRepeatabilityTestWeightAsFound(defaultWeight)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        CalibrationHeader(label = "Static Weighing Tests (As Found)", isValid = isNextStepEnabled)

        ScrollableWithScrollbar(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Spacer(Modifier.height(4.dp))

                // Card 1: Off-Centre Loading Test (4 Corners vs Centre)
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Off Centre Loading Test (4 Corners vs Centre)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(Icons.Default.GridView, contentDescription = null)
                        }

                        OutlinedTextField(
                            value = viewModel.offCentreTestWeightAsFound.value,
                            onValueChange = viewModel::setOffCentreTestWeightAsFound,
                            label = { Text("Test Weight (g)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text(
                            "Platform Test Position Readings (g)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            tonalElevation = 1.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    "Top / Infeed End",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                )

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = offCentreC1,
                                        onValueChange = viewModel::setOffCentreCorner1AsFound,
                                        label = { Text("Corner 1 (FL)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = offCentreC2,
                                        onValueChange = viewModel::setOffCentreCorner2AsFound,
                                        label = { Text("Corner 2 (FR)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                                    OutlinedTextField(
                                        value = offCentreCentre,
                                        onValueChange = viewModel::setOffCentreCentreAsFound,
                                        label = { Text("Centre Reading (g)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(0.8f)
                                    )
                                }

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = offCentreC3,
                                        onValueChange = viewModel::setOffCentreCorner3AsFound,
                                        label = { Text("Corner 3 (BL)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = offCentreC4,
                                        onValueChange = viewModel::setOffCentreCorner4AsFound,
                                        label = { Text("Corner 4 (BR)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Text(
                                    "Bottom / Outfeed End",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                )
                            }
                        }

                        if (offCentreResult != null) {
                            OffCentreSummaryCard(offCentreResult!!)
                        }
                    }
                }

                // Card 2: 3-Time Repeatability Test
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "3-Time Repeatability Test",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(Icons.Default.Repeat, contentDescription = null)
                        }

                        OutlinedTextField(
                            value = viewModel.repeatabilityTestWeightAsFound.value,
                            onValueChange = viewModel::setRepeatabilityTestWeightAsFound,
                            label = { Text("Test Weight (g)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = repR1,
                                onValueChange = viewModel::setRepeatabilityReading1AsFound,
                                label = { Text("Reading 1 (g)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = repR2,
                                onValueChange = viewModel::setRepeatabilityReading2AsFound,
                                label = { Text("Reading 2 (g)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = repR3,
                                onValueChange = viewModel::setRepeatabilityReading3AsFound,
                                label = { Text("Reading 3 (g)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        if (repeatabilityResult != null) {
                            RepeatabilitySummaryCard(repeatabilityResult!!)
                        }
                    }
                }

                Spacer(Modifier.height(60.dp))
            }
        }
    }
}

@Composable
private fun OffCentreSummaryCard(res: OffCentreTestResult) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Off Centre Evaluation",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    color = if (res.isPass) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = if (res.isPass) "PASS" else "FAIL",
                        color = if (res.isPass) Color(0xFF2E7D32) else Color(0xFFC62828),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ValueBox(title = "Centre Reading", value = "%.2fg".format(res.centre), modifier = Modifier.weight(1f))
                ValueBox(title = "Max Deviation", value = "%.2fg".format(res.maxDeviation), modifier = Modifier.weight(1f))
            }

            Text(
                "Max Deviation location: ${res.maxCornerName}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                DevBadge(label = "C1", dev = res.devCorner1, modifier = Modifier.weight(1f))
                DevBadge(label = "C2", dev = res.devCorner2, modifier = Modifier.weight(1f))
                DevBadge(label = "C3", dev = res.devCorner3, modifier = Modifier.weight(1f))
                DevBadge(label = "C4", dev = res.devCorner4, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun RepeatabilitySummaryCard(res: RepeatabilityTestResult) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Repeatability Evaluation",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    color = if (res.isPass) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = if (res.isPass) "PASS" else "FAIL",
                        color = if (res.isPass) Color(0xFF2E7D32) else Color(0xFFC62828),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ValueBox(title = "Mean (x̄)", value = "%.2fg".format(res.mean), modifier = Modifier.weight(1f))
                ValueBox(title = "Range (Max - Min)", value = "%.2fg".format(res.range), modifier = Modifier.weight(1f))
                ValueBox(title = "Min / Max", value = "%.2f / %.2f".format(res.minReading, res.maxReading), modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun DevBadge(label: String, dev: Double, modifier: Modifier = Modifier) {
    Surface(tonalElevation = 1.dp, shape = MaterialTheme.shapes.extraSmall, modifier = modifier) {
        Column(modifier = Modifier.padding(4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = MaterialTheme.typography.labelSmall)
            Text("Δ ${"%.2f".format(dev)}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
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
