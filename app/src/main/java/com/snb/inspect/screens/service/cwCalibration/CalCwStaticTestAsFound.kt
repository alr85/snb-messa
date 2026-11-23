package com.snb.inspect.screens.service.cwCalibration

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.snb.inspect.calibrationViewModels.CalibrationCheckweigherViewModel
import com.snb.inspect.dataClasses.MeasuringEquipmentLocal
import com.snb.inspect.formModules.CalibrationHeader
import com.snb.inspect.formModules.LabeledObjectDropdownWithHelp
import com.snb.inspect.ui.theme.ScrollableWithScrollbar
import com.snb.inspect.util.EvaluatedStaticTestSet
import com.snb.inspect.util.OffCentreTestResult
import com.snb.inspect.util.RepeatabilityTestResult
import com.snb.inspect.util.StaticTestSet

@Composable
fun CalCwStaticTestAsFound(viewModel: CalibrationCheckweigherViewModel) {
    val equipmentList by viewModel.equipmentList.collectAsState(initial = emptyList())
    val userFusionId by viewModel.userFusionId.collectAsState()

    val testCount = viewModel.staticTestCountAsFound.value
    val testSets = viewModel.staticTestSetsAsFound.value
    val evaluatedSets by viewModel.evaluatedStaticTestSetsAsFound.collectAsState()

    val activeSets = testSets.take(testCount)
    val isNextStepEnabled = activeSets.isNotEmpty() && activeSets.all { s ->
        s.centre.isNotBlank() &&
        s.corner1.isNotBlank() &&
        s.corner2.isNotBlank() &&
        s.corner3.isNotBlank() &&
        s.corner4.isNotBlank() &&
        s.repeatabilityReading1.isNotBlank() &&
        s.repeatabilityZero1.isNotBlank() &&
        s.repeatabilityReading2.isNotBlank() &&
        s.repeatabilityZero2.isNotBlank() &&
        s.repeatabilityReading3.isNotBlank() &&
        s.repeatabilityZero3.isNotBlank()
    }

    LaunchedEffect(isNextStepEnabled) {
        viewModel.setCurrentScreenNextEnabled(isNextStepEnabled)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        CalibrationHeader(
            label = "Static Weighing Tests (As Found)",
            isValid = isNextStepEnabled
        )

        ScrollableWithScrollbar(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Spacer(Modifier.height(4.dp))

                // Card 0: Test Count Selector Configuration
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Static Test Configuration",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(Icons.Default.Tune, contentDescription = null)
                        }

                        Text(
                            "Select how many static weighing tests to perform:",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            for (c in 1..5) {
                                SegmentedButton(
                                    selected = testCount == c,
                                    onClick = { viewModel.setStaticTestCountAsFound(c) },
                                    shape = SegmentedButtonDefaults.itemShape(index = c - 1, count = 5)
                                ) {
                                    Text("$c")
                                }
                            }
                        }
                    }
                }

                // Render test cards for each selected static test
                for (idx in 0 until testCount) {
                    val set = testSets.getOrNull(idx) ?: StaticTestSet(id = idx + 1)
                    val evalSet = evaluatedSets.find { it.testSet.id == set.id }
                    val selectedEq = equipmentList.find { it.id == set.equipmentId }

                    StaticTestSetCard(
                        testIndex = idx,
                        set = set,
                        evalSet = evalSet,
                        equipmentList = equipmentList,
                        selectedEquipment = selectedEq,
                        userFusionId = userFusionId,
                        viewModel = viewModel
                    )
                }

                Spacer(Modifier.height(60.dp))
            }
        }
    }
}

@Composable
private fun StaticTestSetCard(
    testIndex: Int,
    set: StaticTestSet,
    evalSet: EvaluatedStaticTestSet?,
    equipmentList: List<MeasuringEquipmentLocal>,
    selectedEquipment: MeasuringEquipmentLocal?,
    userFusionId: Int?,
    viewModel: CalibrationCheckweigherViewModel
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Static Test #${testIndex + 1}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )

                if (set.testWeight.isNotBlank()) {
                    Surface(
                        color = Color(0xFFF5F5F5),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = "${set.testWeight}g",
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }

            // Equipment Dropdown Selection
            LabeledObjectDropdownWithHelp(
                label = "Test Weight Equipment (Test #${testIndex + 1})",
                options = equipmentList,
                selectedOption = selectedEquipment,
                onSelectionChange = { selected ->
                    viewModel.setStaticTestEquipmentId(testIndex, selected?.id, equipmentList)
                },
                optionLabel = { item ->
                    formatEquipmentLabel(item, userFusionId)
                },
                helpText = "Select allocated test weight from measuring equipment table."
            )

//            OutlinedTextField(
//                value = set.testWeight,
//                onValueChange = { viewModel.setStaticTestWeight(testIndex, it) },
//                label = { Text("Test Weight Value (g)") },
//                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
//                singleLine = true,
//                modifier = Modifier.fillMaxWidth()
//            )

            HorizontalDivider()

            // 1. Off Centre Loading Test
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Off Centre Loading Test",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Icon(Icons.Default.GridView, contentDescription = null)
            }

            Surface(
                shape = MaterialTheme.shapes.medium,
                color = Color(0xFFF5F5F5),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = set.corner1,
                            onValueChange = { viewModel.setStaticTestCorner1(testIndex, it) },
                            label = { Text("Back Left (g)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = set.corner2,
                            onValueChange = { viewModel.setStaticTestCorner2(testIndex, it) },
                            label = { Text("Back Right (g)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        OutlinedTextField(
                            value = set.centre,
                            onValueChange = { viewModel.setStaticTestCentre(testIndex, it) },
                            label = { Text("Centre (g)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(0.8f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = set.corner3,
                            onValueChange = { viewModel.setStaticTestCorner3(testIndex, it) },
                            label = { Text("Front Left (g)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = set.corner4,
                            onValueChange = { viewModel.setStaticTestCorner4(testIndex, it) },
                            label = { Text("Front Right (g)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            if (evalSet?.offCentreResult != null) {
                OffCentreSummaryCard(evalSet.offCentreResult)
            }

            HorizontalDivider()

            // 2. 3-Time Repeatability Test
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "3-Time Repeatability Test",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Icon(Icons.Default.Repeat, contentDescription = null)
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = set.repeatabilityReading1,
                        onValueChange = { viewModel.setStaticTestRepeatability1(testIndex, it) },
                        label = { Text("Run 1 Loaded (g)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = set.repeatabilityZero1,
                        onValueChange = { viewModel.setStaticTestRepeatabilityZero1(testIndex, it) },
                        label = { Text("Run 1 Removed (g)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = set.repeatabilityReading2,
                        onValueChange = { viewModel.setStaticTestRepeatability2(testIndex, it) },
                        label = { Text("Run 2 Loaded (g)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = set.repeatabilityZero2,
                        onValueChange = { viewModel.setStaticTestRepeatabilityZero2(testIndex, it) },
                        label = { Text("Run 2 Removed (g)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = set.repeatabilityReading3,
                        onValueChange = { viewModel.setStaticTestRepeatability3(testIndex, it) },
                        label = { Text("Run 3 Loaded (g)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = set.repeatabilityZero3,
                        onValueChange = { viewModel.setStaticTestRepeatabilityZero3(testIndex, it) },
                        label = { Text("Run 3 Removed (g)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (evalSet?.repeatabilityResult != null) {
                RepeatabilitySummaryCard(evalSet.repeatabilityResult)
            }
        }
    }
}



private fun formatEquipmentLabel(item: MeasuringEquipmentLocal, userFusionId: Int?): String {
    val isMine = userFusionId != null && item.employeeID == userFusionId
    val prefix = if (isMine) "⭐ " else ""
    val suffix = if (isMine) " (Assigned to me)" else ""
    val weightStr = if (item.weight != null) " - ${item.weight}g" else ""
    return "$prefix${item.manufacturer} ${item.model} (${item.serialNumber})$weightStr$suffix"
}

@Composable
private fun OffCentreSummaryCard(res: OffCentreTestResult) {
    Surface(
        color = Color(0xFFF5F5F5),
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
        color = Color(0xFFF5F5F5),
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

            if (res.maxZeroReturn != null) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ValueBox(title = "Max Zero Return", value = "%.2fg".format(res.maxZeroReturn), modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun DevBadge(label: String, dev: Double, modifier: Modifier = Modifier) {
    Surface(color = Color(0xFFEEEEEE), shape = MaterialTheme.shapes.extraSmall, modifier = modifier) {
        Column(modifier = Modifier.padding(4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = MaterialTheme.typography.labelSmall)
            Text("Δ ${"%.2f".format(dev)}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun ValueBox(title: String, value: String, modifier: Modifier = Modifier) {
    Surface(color = Color(0xFFF5F5F5), shape = MaterialTheme.shapes.medium, modifier = modifier) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(title, style = MaterialTheme.typography.labelSmall)
            Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        }
    }
}
