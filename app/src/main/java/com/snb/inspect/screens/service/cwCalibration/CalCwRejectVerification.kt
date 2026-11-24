package com.snb.inspect.screens.service.cwCalibration

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.snb.inspect.calibrationViewModels.CalibrationCheckweigherViewModel
import com.snb.inspect.formModules.CalibrationHeader
import com.snb.inspect.formModules.LabeledDropdownWithHelp
import com.snb.inspect.formModules.LabeledTextFieldWithHelp
import com.snb.inspect.ui.theme.AppCard
import com.snb.inspect.ui.theme.ScrollableWithScrollbar

@Composable
fun CalCwRejectVerification(viewModel: CalibrationCheckweigherViewModel) {
    val genResult = viewModel.generalRejectTestResult.value
    val isNextStepEnabled = genResult.isNotBlank()

    LaunchedEffect(isNextStepEnabled) {
        viewModel.setCurrentScreenNextEnabled(isNextStepEnabled)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        CalibrationHeader(label = "Reject Mechanism Verification", isValid = isNextStepEnabled)

        ScrollableWithScrollbar(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Spacer(Modifier.height(4.dp))

                AppCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            "Mandatory General Reject Test",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        LabeledDropdownWithHelp(
                            label = "General Reject Function Test",
                            options = listOf("PASS", "FAIL", "N/A"),
                            selectedOption = genResult,
                            onSelectionChange = viewModel::setGeneralRejectTestResult,
                            helpText = "General reject mechanism functionality check."
                        )

                        LabeledTextFieldWithHelp(
                            label = "General Reject Notes",
                            value = viewModel.generalRejectNotes.value,
                            onValueChange = viewModel::setGeneralRejectNotes,
                            helpText = "Engineer notes on general reject test."
                        )
                    }
                }

                AppCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            "Optional Threshold & Set-Point Rejection Tests",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "Use customer operating requirements and test blocks if provided.",
                            style = MaterialTheme.typography.bodySmall
                        )

                        RejectTestRow(
                            testName = "T0 / Nominal Weight Test",
                            refWeight = viewModel.t0TestRefWeight.value,
                            onRefChange = viewModel::setT0TestRefWeight,
                            expected = viewModel.t0TestExpected.value,
                            onExpChange = viewModel::setT0TestExpected,
                            actual = viewModel.t0TestActual.value,
                            onActChange = viewModel::setT0TestActual,
                            result = viewModel.t0TestResult.value,
                            onResultChange = viewModel::setT0TestResult
                        )

                        RejectTestRow(
                            testName = "T1 Threshold Test",
                            refWeight = viewModel.t1TestRefWeight.value,
                            onRefChange = viewModel::setT1TestRefWeight,
                            expected = viewModel.t1TestExpected.value,
                            onExpChange = viewModel::setT1TestExpected,
                            actual = viewModel.t1TestActual.value,
                            onActChange = viewModel::setT1TestActual,
                            result = viewModel.t1TestResult.value,
                            onResultChange = viewModel::setT1TestResult
                        )

                        RejectTestRow(
                            testName = "T2 Threshold Test",
                            refWeight = viewModel.t2TestRefWeight.value,
                            onRefChange = viewModel::setT2TestRefWeight,
                            expected = viewModel.t2TestExpected.value,
                            onExpChange = viewModel::setT2TestExpected,
                            actual = viewModel.t2TestActual.value,
                            onActChange = viewModel::setT2TestActual,
                            result = viewModel.t2TestResult.value,
                            onResultChange = viewModel::setT2TestResult
                        )

                        RejectTestRow(
                            testName = "Overweight Rejection Test",
                            refWeight = viewModel.overweightTestRefWeight.value,
                            onRefChange = viewModel::setOverweightTestRefWeight,
                            expected = viewModel.overweightTestExpected.value,
                            onExpChange = viewModel::setOverweightTestExpected,
                            actual = viewModel.overweightTestActual.value,
                            onActChange = viewModel::setOverweightTestActual,
                            result = viewModel.overweightTestResult.value,
                            onResultChange = viewModel::setOverweightTestResult
                        )
                    }
                }

                Spacer(Modifier.height(60.dp))
            }
        }
    }
}

@Composable
private fun RejectTestRow(
    testName: String,
    refWeight: String,
    onRefChange: (String) -> Unit,
    expected: String,
    onExpChange: (String) -> Unit,
    actual: String,
    onActChange: (String) -> Unit,
    result: String,
    onResultChange: (String) -> Unit
) {
    AppCard(shape = MaterialTheme.shapes.medium) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(testName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = refWeight,
                    onValueChange = onRefChange,
                    label = { Text("Ref Weight (g)") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = expected,
                    onValueChange = onExpChange,
                    label = { Text("Expected Behavior") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = actual,
                    onValueChange = onActChange,
                    label = { Text("Actual Behavior") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )

                LabeledDropdownWithHelp(
                    label = "Result",
                    options = listOf("PASS", "FAIL", "N/A"),
                    selectedOption = result.ifBlank { "N/A" },
                    onSelectionChange = onResultChange,
                    helpText = "Test result for $testName"
                )
            }
        }
    }
}
