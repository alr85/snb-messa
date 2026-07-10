package com.snb.inspect.screens.service.cwCalibration

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.snb.inspect.calibrationViewModels.CalibrationCheckweigherViewModel
import com.snb.inspect.formModules.CalibrationHeader
import com.snb.inspect.formModules.LabeledMultiSelectDropdownWithHelp
import com.snb.inspect.formModules.LabeledRadioButtonWithHelp
import com.snb.inspect.formModules.LabeledReadOnlyField
import com.snb.inspect.formModules.LabeledTextFieldWithHelp
import com.snb.inspect.ui.theme.FormSpacer
import com.snb.inspect.ui.theme.ScrollableWithScrollbar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalCheckweigherCalibrationStart(
    viewModel: CalibrationCheckweigherViewModel
) {
    val lastLocation by viewModel.system.lastLocation.let { mutableStateOf(it) } // Should be from VM state
    val canPerformCalibration by viewModel.canPerformCalibration
    val reasonForNotCalibrating by viewModel.reasonForNotCalibrating
    var showConfirmDialog by remember { mutableStateOf(false) }
    var pendingValue by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(Unit) {
        if (viewModel.newLocation.value.isBlank()) {
            viewModel.setNewLocation(viewModel.system.lastLocation)
        }
    }

    val isNextStepEnabled = canPerformCalibration || reasonForNotCalibrating.isNotEmpty()

    LaunchedEffect(isNextStepEnabled) {
        viewModel.setCurrentScreenNextEnabled(isNextStepEnabled)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        CalibrationHeader(
            label = "Calibration Start",
            isValid = isNextStepEnabled
        )

        ScrollableWithScrollbar(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Spacer(Modifier.height(16.dp))

                LabeledReadOnlyField(
                    label = "Serial Number",
                    value = viewModel.serialNumber.value,
                    helpText = "Unique identifier for the system."
                )
                FormSpacer()

                LabeledReadOnlyField(
                    label = "Make/Model",
                    value = viewModel.modelDescription.value,
                    helpText = "Retrieved from database."
                )
                FormSpacer()

                LabeledTextFieldWithHelp(
                    label = "Location",
                    value = viewModel.newLocation.value,
                    onValueChange = viewModel::setNewLocation,
                    helpText = "Edit if the system has moved.",
                    isNAToggleEnabled = false,
                    maxLength = 30
                )
                FormSpacer()

                LabeledRadioButtonWithHelp(
                    label = "Able to calibrate?",
                    value = canPerformCalibration,
                    onValueChange = { newValue ->
                        if (canPerformCalibration && !newValue) {
                            pendingValue = newValue
                            showConfirmDialog = true
                        } else {
                            viewModel.setCanPerformCalibration(newValue)
                        }
                    },
                    helpText = "Are you able to complete a calibration today?"
                )
                FormSpacer()

                if (!canPerformCalibration) {
                    val commonReasons = listOf(
                        "No product available",
                        "No power/air supply",
                        "Unable to locate",
                        "System unsafe",
                        "System faulty/inoperative",
                        "Other"
                    )
                    LabeledMultiSelectDropdownWithHelp(
                        label = "Reason for not calibrating",
                        options = commonReasons,
                        value = reasonForNotCalibrating.joinToString(", "),
                        selectedOptions = reasonForNotCalibrating,
                        onSelectionChange = { viewModel.setReasonForNotCalibrating(it) },
                        helpText = "Select reasons why calibration cannot be performed.",
                        isNAToggleEnabled = false
                    )
                    FormSpacer()
                }

                Spacer(Modifier.height(60.dp))
            }
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Confirm change") },
            text = { Text("All calibration data will be wiped. Continue?") },
            confirmButton = {
                TextButton(onClick = {
                    pendingValue?.let { viewModel.setCanPerformCalibration(it) }
                    showConfirmDialog = false
                }) { Text("Yes, wipe data") }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) { Text("Cancel") }
            }
        )
    }
}
