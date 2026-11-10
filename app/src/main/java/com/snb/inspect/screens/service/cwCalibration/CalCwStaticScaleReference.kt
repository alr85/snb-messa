package com.snb.inspect.screens.service.cwCalibration

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.snb.inspect.calibrationViewModels.CalibrationCheckweigherViewModel
import com.snb.inspect.formModules.CalibrationHeader
import com.snb.inspect.formModules.LabeledDropdownWithHelp
import com.snb.inspect.formModules.LabeledReadOnlyField
import com.snb.inspect.formModules.LabeledTextFieldWithHelp
import com.snb.inspect.ui.theme.FormSpacer
import com.snb.inspect.ui.theme.ScrollableWithScrollbar

@Composable
fun CalCwStaticScaleReference(viewModel: CalibrationCheckweigherViewModel) {
    val resStr = viewModel.staticScaleResolution.value
    val resolutionVal = resStr.toDoubleOrNull()
    val isResolutionValid = resStr.isNotBlank() && resolutionVal != null && resolutionVal > 0.0

    val calDate = viewModel.staticScaleCalDate.value
    val calStatus = viewModel.staticScaleCalStatus.value
    val suitability = viewModel.staticScaleSuitability.value

    val isUnsuitableOrLimited = suitability == "UNSUITABLE" || suitability == "LIMITATIONS" || calStatus.contains("EXPIRED") || calStatus.contains("MISSING")
    val isNextStepEnabled = (viewModel.staticScaleManufacturer.value.isNotBlank() || viewModel.staticScaleMakeModel.value.isNotBlank()) &&
            isResolutionValid && calDate.isNotBlank()

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
                    helpText = "Manufacturer of customer's reference static scale."
                )
                FormSpacer()

                LabeledTextFieldWithHelp(
                    label = "Model",
                    value = viewModel.staticScaleModel.value,
                    onValueChange = {
                        viewModel.setStaticScaleModel(it)
                        viewModel.setStaticScaleMakeModel("${viewModel.staticScaleManufacturer.value} $it".trim())
                    },
                    helpText = "Model number of reference scale."
                )
                FormSpacer()

                LabeledTextFieldWithHelp(
                    label = "Serial Number",
                    value = viewModel.staticScaleSerial.value,
                    onValueChange = viewModel::setStaticScaleSerial,
                    helpText = "Serial number of reference static scale."
                )
                FormSpacer()

                LabeledTextFieldWithHelp(
                    label = "Resolution (g)",
                    value = resStr,
                    onValueChange = viewModel::setStaticScaleResolution,
                    keyboardType = KeyboardType.Decimal,
                    helpText = "Scale division / resolution (e.g. 0.1 or 0.01). Must be a valid positive decimal number."
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
                    label = "Calibration Date",
                    value = calDate,
                    onValueChange = viewModel::setStaticScaleCalDate,
                    helpText = "Date static scale was last calibrated (format: YYYY-MM-DD or DD/MM/YYYY)."
                )
                FormSpacer()

                LabeledTextFieldWithHelp(
                    label = "Certificate Ref",
                    value = viewModel.staticScaleCertRef.value,
                    onValueChange = viewModel::setStaticScaleCertRef,
                    helpText = "Calibration certificate reference."
                )
                FormSpacer()

                LabeledReadOnlyField(
                    label = "Calibration Status",
                    value = calStatus,
                    helpText = "Automatically calculated from the calibration date relative to today's date."
                )
                FormSpacer()

                LabeledDropdownWithHelp(
                    label = "Equipment Suitability",
                    options = listOf("SUITABLE", "UNSUITABLE", "LIMITATIONS"),
                    selectedOption = suitability,
                    onSelectionChange = viewModel::setStaticScaleSuitability,
                    helpText = "Is the scale suitable and traceability established for dynamic accuracy assessment?"
                )
                FormSpacer()

                LabeledTextFieldWithHelp(
                    label = "Reference Mass Check Results",
                    value = viewModel.staticScaleRefMassCheckResult.value,
                    onValueChange = viewModel::setStaticScaleRefMassCheckResult,
                    helpText = "Results of static reference mass checks performed."
                )
                FormSpacer()

                if (isUnsuitableOrLimited) {
                    ElevatedCard(
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "⚠️ Traceability / Calibration Limitation Flagged",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Static scale calibration status is '$calStatus' and suitability is '$suitability'. " +
                                        "Limitations will be flagged on the dynamic accuracy assessment.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                    FormSpacer()

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
