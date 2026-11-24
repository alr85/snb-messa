package com.snb.inspect.screens.service.cwCalibration

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.snb.inspect.calibrationViewModels.CalibrationCheckweigherViewModel
import com.snb.inspect.formModules.CalibrationHeader
import com.snb.inspect.formModules.FormRowWrapper
import com.snb.inspect.formModules.LabeledDropdownWithHelp
import com.snb.inspect.formModules.LabeledReadOnlyField
import com.snb.inspect.formModules.LabeledTextFieldWithHelp
import com.snb.inspect.ui.theme.*
import java.util.Calendar
import java.util.Locale

@Composable
fun CalCwStaticScaleReference(viewModel: CalibrationCheckweigherViewModel) {
    val context = LocalContext.current
    val resStr = viewModel.staticScaleResolution.value
    val resolutionVal = resStr.toDoubleOrNull()
    val isResolutionValid = resStr.isNotBlank() && resolutionVal != null && resolutionVal > 0.0

    val maxCapStr = viewModel.staticScaleMaxCapacity.value
    val minCapStr = viewModel.staticScaleMinCapacity.value
    val maxCapVal = maxCapStr.toDoubleOrNull()
    val minCapVal = minCapStr.toDoubleOrNull()

    val isMaxCapValid = maxCapStr.isNotBlank() && maxCapVal != null && maxCapVal > 0.0
    val isMinCapValid = minCapStr.isNotBlank() && minCapVal != null && minCapVal >= 0.0 && (maxCapVal == null || minCapVal <= maxCapVal)

    val serialStr = viewModel.staticScaleSerial.value
    val certRefStr = viewModel.staticScaleCertRef.value
    val isSerialValid = serialStr.isNotBlank() && serialStr != "N/A"
    val isCertRefValid = certRefStr.isNotBlank()

    val calDate = viewModel.staticScaleCalDate.value
    val calStatus = viewModel.staticScaleCalStatus.value
    val suitability = viewModel.staticScaleSuitability.value
    val functionalCheck = viewModel.staticScaleFunctionalCheck.value

    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val calendar = Calendar.getInstance()
        if (calDate.isNotBlank()) {
            val parts = calDate.split("-", "/")
            if (parts.size == 3) {
                if (parts[0].length == 4) { // YYYY-MM-DD
                    val y = parts[0].toIntOrNull()
                    val m = parts[1].toIntOrNull()?.minus(1)
                    val d = parts[2].toIntOrNull()
                    if (y != null && m != null && d != null) {
                        calendar.set(y, m, d)
                    }
                } else if (parts[2].length == 4) { // DD/MM/YYYY
                    val d = parts[0].toIntOrNull()
                    val m = parts[1].toIntOrNull()?.minus(1)
                    val y = parts[2].toIntOrNull()
                    if (y != null && m != null && d != null) {
                        calendar.set(y, m, d)
                    }
                }
            }
        }

        DisposableEffect(Unit) {
            val datePickerDialog = DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    val formattedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth)
                    viewModel.setStaticScaleCalDate(formattedDate)
                    showDatePicker = false
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).apply {
                setOnDismissListener { showDatePicker = false }
            }
            datePickerDialog.show()

            onDispose {
                datePickerDialog.dismiss()
            }
        }
    }

    // Auto-evaluate WELMEC 6.4 / OIML R 51-1 assessment
    val assessment = viewModel.evaluateStaticScaleAssessment()

    // Sync suitability automatically if auto-evaluated
    LaunchedEffect(assessment.suitability) {
        if (viewModel.staticScaleSuitability.value != assessment.suitability) {
            viewModel.setStaticScaleSuitability(assessment.suitability)
        }
        if (assessment.suitability != "SUITABLE" && viewModel.staticScaleLimitationFlags.value.isBlank()) {
            viewModel.setStaticScaleLimitationFlags(assessment.detailedNotes)
        }
    }

    val isUnsuitableOrLimited = suitability == "UNSUITABLE" || suitability == "LIMITATIONS" || calStatus.contains("EXPIRED") || calStatus.contains("MISSING")
    val isNextStepEnabled = (viewModel.staticScaleManufacturer.value.isNotBlank() || viewModel.staticScaleMakeModel.value.isNotBlank()) &&
            isSerialValid &&
            isCertRefValid &&
            isResolutionValid &&
            isMaxCapValid &&
            isMinCapValid &&
            calDate.isNotBlank() && calDate != "N/A"

    LaunchedEffect(isNextStepEnabled) {
        viewModel.setCurrentScreenNextEnabled(isNextStepEnabled)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        CalibrationHeader(label = "Static Reference Scale", isValid = isNextStepEnabled)

        ScrollableWithScrollbar(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Spacer(Modifier.height(16.dp))

                LabeledTextFieldWithHelp(
                    label = "Manufacturer",
                    value = viewModel.staticScaleManufacturer.value,
                    onValueChange = viewModel::setStaticScaleManufacturer,
                    isNAToggleEnabled = false,
                    helpText = "Manufacturer of reference static scale."
                )
                FormSpacer()

                LabeledTextFieldWithHelp(
                    label = "Model",
                    value = viewModel.staticScaleModel.value,
                    onValueChange = {
                        viewModel.setStaticScaleModel(it)
                        viewModel.setStaticScaleMakeModel("${viewModel.staticScaleManufacturer.value} $it".trim())
                    },
                    isNAToggleEnabled = false,
                    helpText = "Model number of reference scale."
                )
                FormSpacer()

                LabeledTextFieldWithHelp(
                    label = "Serial Number",
                    value = serialStr,
                    onValueChange = viewModel::setStaticScaleSerial,
                    isNAToggleEnabled = false,
                    helpText = "Serial number of reference static scale. Required for equipment traceability."
                )
                if (!isSerialValid) {
                    Text(
                        text = "⚠️ Serial number is required for equipment traceability.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
                FormSpacer()

                LabeledTextFieldWithHelp(
                    label = "Calibration / Verification Evidence",
                    value = certRefStr,
                    onValueChange = viewModel::setStaticScaleCertRef,
                    isNAToggleEnabled = true,
                    helpText = "Calibration certificate reference or legal verification stamp identifier. Toggle N/A if unavailable in the field."
                )
                FormSpacer()

                var showDateHelpDialog by remember { mutableStateOf(false) }

                FormRowWrapper(
                    label = "Calibration Date",
                    naButtonText = "N/A",
                    isDisabled = false,
                    onNaClick = null,
                    onHelpClick = { showDateHelpDialog = true },
                ) {
                    OutlinedTextField(
                        value = calDate,
                        onValueChange = viewModel::setStaticScaleCalDate,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("YYYY-MM-DD") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = "Pick Date"
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FormInputFocusedBorderColor,
                            unfocusedBorderColor = FormInputUnfocusedBorderColor,
                            focusedContainerColor = FormInputFocusedContainerColor,
                            unfocusedContainerColor = FormInputUnfocusedContainerColor,
                            focusedLabelColor = FormInputFocusedLabelColor,
                            unfocusedLabelColor = FormInputUnfocusedLabelColor,
                            focusedTextColor = FormInputFocusedTextColor,
                            unfocusedTextColor = FormInputUnfocusedTextColor
                        )
                    )
                }

                if (showDateHelpDialog) {
                    AlertDialog(
                        onDismissRequest = { showDateHelpDialog = false },
                        title = { Text(text = "Calibration Date") },
                        text = { Text(text = "Date static scale was last calibrated. Use the calendar icon to select a date or type YYYY-MM-DD.") },
                        confirmButton = {
                            TextButton(onClick = { showDateHelpDialog = false }) { Text("OK") }
                        }
                    )
                }
                FormSpacer()

                LabeledReadOnlyField(
                    label = "Calibration Status",
                    value = calStatus,
                    helpText = "Automatically calculated from the calibration date relative to today's date."
                )
                FormSpacer()

                LabeledTextFieldWithHelp(
                    label = "Verification Scale Interval (e in grams)",
                    value = resStr,
                    onValueChange = viewModel::setStaticScaleResolution,
                    keyboardType = KeyboardType.Decimal,
                    isNAToggleEnabled = false,
                    helpText = "Enter verification scale interval 'e' from data plate (not display resolution 'd'). Required e ≤ 1/10th Table 3 for WELMEC Annex 5."
                )
                if (resStr.isNotBlank() && !isResolutionValid) {
                    Text(
                        text = "⚠️ Resolution must be a valid positive decimal number (e.g. 0.1 or 0.01).",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
                FormSpacer()

                LabeledTextFieldWithHelp(
                    label = "Maximum Capacity (Max in grams)",
                    value = maxCapStr,
                    onValueChange = viewModel::setStaticScaleMaxCapacity,
                    keyboardType = KeyboardType.Decimal,
                    isNAToggleEnabled = false,
                    helpText = "Maximum weighing range (Max) from scale data plate. Must be greater than 0."
                )
                if (maxCapStr.isNotBlank() && !isMaxCapValid) {
                    Text(
                        text = "⚠️ Maximum capacity must be a positive number greater than 0.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
                FormSpacer()

                LabeledTextFieldWithHelp(
                    label = "Minimum Capacity (Min in grams)",
                    value = minCapStr,
                    onValueChange = viewModel::setStaticScaleMinCapacity,
                    keyboardType = KeyboardType.Decimal,
                    isNAToggleEnabled = false,
                    helpText = "Minimum weighing range (Min) from scale data plate. Must be greater than or equal to 0 and ≤ Max."
                )
                if (minCapStr.isNotBlank() && !isMinCapValid) {
                    Text(
                        text = "⚠️ Minimum capacity must be greater than or equal to 0 and less than or equal to Max capacity.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
                FormSpacer()

                var showFunctionalHelpDialog by remember { mutableStateOf(false) }

                FormRowWrapper(
                    label = "Functional Check (3-Repeatability Pack Check)",
                    naButtonText = "N/A",
                    isDisabled = false,
                    onNaClick = null,
                    onHelpClick = { showFunctionalHelpDialog = true },
                ) {
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        SegmentedButton(
                            selected = functionalCheck == "SATISFACTORY",
                            onClick = { viewModel.setStaticScaleFunctionalCheck("SATISFACTORY") },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                        ) {
                            Text("Satisfactory")
                        }
                        SegmentedButton(
                            selected = functionalCheck == "UNSATISFACTORY",
                            onClick = { viewModel.setStaticScaleFunctionalCheck("UNSATISFACTORY") },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                        ) {
                            Text("Unsatisfactory")
                        }
                    }
                }

                if (showFunctionalHelpDialog) {
                    AlertDialog(
                        onDismissRequest = { showFunctionalHelpDialog = false },
                        title = { Text(text = "Functional Check (SNB 3-Repeatability Check)") },
                        text = { Text(text = "1. Ensure scale is level and zeroed.\n2. Place test pack on scale, record reading, remove.\n3. Repeat 3 times.\n4. Confirm readings are consistent and zero returns stably.") },
                        confirmButton = {
                            TextButton(onClick = { showFunctionalHelpDialog = false }) { Text("OK") }
                        }
                    )
                }
                FormSpacer()

                // WELMEC 6.4 / OIML R 51-1 Auto-Assessment Card
                AppCard(
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = when (assessment.suitability) {
                            "SUITABLE" -> Color(0xFFE8F5E9)
                            "LIMITATIONS" -> Color(0xFFFFF3E0)
                            else -> MaterialTheme.colorScheme.errorContainer
                        }
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = assessment.summaryMessage,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = when (assessment.suitability) {
                                "SUITABLE" -> Color(0xFF2E7D32)
                                "LIMITATIONS" -> Color(0xFFE65100)
                                else -> MaterialTheme.colorScheme.onErrorContainer
                            }
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = assessment.detailedNotes,
                            style = MaterialTheme.typography.bodyMedium,
                            color = when (assessment.suitability) {
                                "SUITABLE" -> Color(0xFF1B5E20)
                                "LIMITATIONS" -> Color(0xFFBF360C)
                                else -> MaterialTheme.colorScheme.onErrorContainer
                            }
                        )
                        if (assessment.requiredE != null && assessment.actualE != null) {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "Scale Verification Interval (e): ${assessment.actualE}g | Required for WELMEC Annex 5: e ≤ ${assessment.requiredE}g",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
                FormSpacer()

                LabeledDropdownWithHelp(
                    label = "Equipment Suitability Assessment",
                    options = listOf("SUITABLE", "LIMITATIONS", "UNSUITABLE"),
                    selectedOption = suitability,
                    onSelectionChange = viewModel::setStaticScaleSuitability,
                    helpText = "Suitability classification for calibration reporting."
                )
                FormSpacer()

                if (isUnsuitableOrLimited) {
                    LabeledTextFieldWithHelp(
                        label = "Limitation Notes",
                        value = viewModel.staticScaleLimitationFlags.value,
                        onValueChange = viewModel::setStaticScaleLimitationFlags,
                        helpText = "Detail reasons for traceability or suitability limitations."
                    )
                    FormSpacer()
                }

                Spacer(Modifier.height(60.dp))
            }
        }
    }
}
