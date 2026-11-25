package com.snb.inspect.screens.service.cwCalibration

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
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
import com.snb.inspect.formModules.inputs.SimpleDropdown
import com.snb.inspect.ui.theme.AppCard
import com.snb.inspect.ui.theme.ScrollableWithScrollbar
import com.snb.inspect.util.OimlInspectionType
import com.snb.inspect.util.ToleranceMode

@Composable
fun CalCwTolerancesAndAssessment(viewModel: CalibrationCheckweigherViewModel) {
    val currentMode = viewModel.toleranceMode.value

    val pctStr = viewModel.stdDevPercentageOfTne.value
    val pctVal = pctStr.toDoubleOrNull()
    val isPctError = currentMode == ToleranceMode.PACKAGED_GOODS_TNE && pctStr.isNotBlank() && (pctVal == null || pctVal < 5.0 || pctVal > 20.0)

    val isNextStepEnabled = !isPctError

    LaunchedEffect(isNextStepEnabled) {
        viewModel.setCurrentScreenNextEnabled(isNextStepEnabled)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        CalibrationHeader(label = "Acceptance Tolerances Selection", isValid = isNextStepEnabled)

        ScrollableWithScrollbar(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Spacer(Modifier.height(4.dp))

                // Single Card: Tolerance Mode Selection & Configuration
                AppCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Tolerance Mode",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            var showHelpDialog by remember { mutableStateOf(false) }
                            IconButton(
                                onClick = { showHelpDialog = true },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                                    contentDescription = "Help for Tolerance Mode",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            if (showHelpDialog) {
                                AlertDialog(
                                    onDismissRequest = { showHelpDialog = false },
                                    title = { Text(currentMode.label) },
                                    text = { Text(currentMode.description) },
                                    confirmButton = {
                                        TextButton(onClick = { showHelpDialog = false }) { Text("OK") }
                                    }
                                )
                            }
                        }

                        val modeOptions = ToleranceMode.values().map { it.label }
                        val selectedModeLabel = currentMode.label

                        SimpleDropdown(
                            options = modeOptions,
                            selectedOption = selectedModeLabel,
                            onSelectionChange = { selected ->
                                val mode = ToleranceMode.values().find { it.label == selected } ?: ToleranceMode.PACKAGED_GOODS_TNE
                                viewModel.setToleranceMode(mode)
                            },
                            isDisabled = false,
                            label = "Select Tolerance Mode"
                        )

                        // 1. MANUAL_SPEC Inputs
                        if (currentMode == ToleranceMode.MANUAL_SPEC) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = viewModel.manualMeanErrorValue.value,
                                        onValueChange = viewModel::setManualMeanErrorValue,
                                        label = { Text("Max Mean Error") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        modifier = Modifier.weight(2f),
                                        singleLine = true
                                    )
                                    Box(modifier = Modifier.weight(1f)) {
                                        SimpleDropdown(
                                            options = listOf("GRAMS", "PERCENT"),
                                            selectedOption = viewModel.manualMeanErrorUnit.value,
                                            onSelectionChange = viewModel::setManualMeanErrorUnit,
                                            isDisabled = false,
                                            label = "Unit"
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = viewModel.manualStdDevValue.value,
                                        onValueChange = viewModel::setManualStdDevValue,
                                        label = { Text("Max Std Dev (1σ)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        modifier = Modifier.weight(2f),
                                        singleLine = true
                                    )
                                    Box(modifier = Modifier.weight(1f)) {
                                        SimpleDropdown(
                                            options = listOf("GRAMS", "PERCENT"),
                                            selectedOption = viewModel.manualStdDevUnit.value,
                                            onSelectionChange = viewModel::setManualStdDevUnit,
                                            isDisabled = false,
                                            label = "Unit"
                                        )
                                    }
                                }
                            }
                        }

                        // 2. DIVISION_BASED Inputs
                        if (currentMode == ToleranceMode.DIVISION_BASED) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = viewModel.staticDivisionMultiplier.value,
                                    onValueChange = viewModel::setStaticDivisionMultiplier,
                                    label = { Text("Static Multiplier (e.g. 1d)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = viewModel.dynamicDivisionMultiplier.value,
                                    onValueChange = viewModel::setDynamicDivisionMultiplier,
                                    label = { Text("Dynamic Multiplier (e.g. 2d)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                            }
                        }

                        // 3. PACKAGED_GOODS_TNE Inputs
                        if (currentMode == ToleranceMode.PACKAGED_GOODS_TNE) {
                            OutlinedTextField(
                                value = viewModel.stdDevPercentageOfTne.value,
                                onValueChange = viewModel::setStdDevPercentageOfTne,
                                label = { Text("Max Dynamic Std Dev Limit (% of TNE)") },
                                placeholder = { Text("Default: 10.0") },
                                isError = isPctError,
                                supportingText = {
                                    Text(
                                        text = if (isPctError) {
                                            "⚠️ Must be between 5.0% and 20.0% of TNE (Default: 10.0%)"
                                        } else {
                                            "Allowed range: 5.0% to 20.0% of TNE (Default: 10.0%)"
                                        },
                                        color = if (isPctError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }

                        // 4. OIML_R51_BENCHMARK Inputs
                        if (currentMode == ToleranceMode.OIML_R51_BENCHMARK) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        SimpleDropdown(
                                            options = listOf("XI", "XII", "XIII", "XIIII"),
                                            selectedOption = viewModel.accuracyClass.value,
                                            onSelectionChange = viewModel::setAccuracyClass,
                                            isDisabled = false,
                                            label = "Accuracy Class"
                                        )
                                    }
                                    Box(modifier = Modifier.weight(1f)) {
                                        SimpleDropdown(
                                            options = listOf("0.5", "1.0", "2.0"),
                                            selectedOption = viewModel.designationFactorX.value,
                                            onSelectionChange = viewModel::setDesignationFactorX,
                                            isDisabled = false,
                                            label = "Designation (x)"
                                        )
                                    }
                                }
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    SimpleDropdown(
                                        options = OimlInspectionType.values().map { it.label },
                                        selectedOption = viewModel.inspectionType.value.label,
                                        onSelectionChange = { selected ->
                                            val type = OimlInspectionType.values().find { it.label == selected } ?: OimlInspectionType.INITIAL_VERIFICATION
                                            viewModel.setInspectionType(type)
                                        },
                                        isDisabled = false,
                                        label = "Inspection Type"
                                    )
                                }
                            }
                        }

                        // 0. NONE Advisory Banner
                        if (currentMode == ToleranceMode.NONE) {
                            Surface(
                                color = Color(0xFFE8F5E9),
                                shape = MaterialTheme.shapes.medium,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "ℹ️ Advisory Mode Selected: No tolerance limits will be enforced. All static and dynamic weighing errors will be recorded for advisory purposes without generating failures.",
                                    color = Color(0xFF2E7D32),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(60.dp))
            }
        }
    }
}
