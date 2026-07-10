package com.snb.inspect.screens.service.cwCalibration

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.snb.inspect.calibrationViewModels.CalibrationCheckweigherViewModel
import com.snb.inspect.formModules.CalibrationHeader
import com.snb.inspect.formModules.LabeledDropdownWithHelp
import com.snb.inspect.formModules.LabeledTextFieldWithHelp
import com.snb.inspect.ui.theme.FormSpacer
import com.snb.inspect.ui.theme.ScrollableWithScrollbar

@Composable
fun CalCwSystemDetails(viewModel: CalibrationCheckweigherViewModel) {
    val isNextStepEnabled = viewModel.beltWidth.value.isNotBlank() &&
                           viewModel.weighConveyorLength.value.isNotBlank() &&
                           viewModel.rejectType.value.isNotBlank()

    LaunchedEffect(isNextStepEnabled) {
        viewModel.setCurrentScreenNextEnabled(isNextStepEnabled)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        CalibrationHeader(label = "System Details", isValid = isNextStepEnabled)

        ScrollableWithScrollbar(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Spacer(Modifier.height(16.dp))

                LabeledTextFieldWithHelp(
                    label = "Belt Width",
                    value = viewModel.beltWidth.value,
                    onValueChange = viewModel::setBeltWidth,
                    helpText = "Width of the weigh belt."
                )
                FormSpacer()

                LabeledTextFieldWithHelp(
                    label = "Weigh Conveyor Length",
                    value = viewModel.weighConveyorLength.value,
                    onValueChange = viewModel::setWeighConveyorLength,
                    helpText = "Length of the weigh conveyor."
                )
                FormSpacer()

                val rejectTypes = remember { listOf("Pusher", "Air Blast", "Diverter Arm", "Drop Flap", "Retracting Belt", "Carriage Tilt", "Other") }
                LabeledDropdownWithHelp(
                    label = "Reject Type",
                    options = rejectTypes,
                    selectedOption = viewModel.rejectType.value,
                    onSelectionChange = viewModel::setRejectType,
                    helpText = "Select the rejection mechanism."
                )
                FormSpacer()

                val captureOptions = remember { listOf("Printer", "USB", "Network", "N/A") }
                LabeledDropdownWithHelp(
                    label = "Printer/Data Capture",
                    options = captureOptions,
                    selectedOption = viewModel.printerDataCapture.value,
                    onSelectionChange = viewModel::setPrinterDataCapture,
                    helpText = "Available data capture method."
                )
                FormSpacer()

                val rejectModes = remember { listOf("E mark", "Minimum weight") }
                LabeledDropdownWithHelp(
                    label = "Reject Mode",
                    options = rejectModes,
                    selectedOption = viewModel.rejectMode.value,
                    onSelectionChange = viewModel::setRejectMode,
                    helpText = "Operating mode for rejection."
                )
                FormSpacer()

                Spacer(Modifier.height(60.dp))
            }
        }
    }
}
