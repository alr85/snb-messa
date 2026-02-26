package com.example.mecca.screens.service.mdCalibration

//import com.example.mecca.screens.getAppVersion
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
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
import com.example.mecca.calibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import com.example.mecca.formModules.CalibrationHeader
import com.example.mecca.formModules.LabeledRadioButtonWithHelp
import com.example.mecca.formModules.LabeledReadOnlyField
import com.example.mecca.formModules.LabeledTextFieldWithHelp
import com.example.mecca.formModules.YesNoState
import com.example.mecca.ui.theme.FormSpacer
import com.example.mecca.ui.theme.ScrollableWithScrollbar


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalMetalDetectorConveyorCalibrationStart(
    viewModel: CalibrationMetalDetectorConveyorViewModel
) {
    val lastLocation by viewModel.lastLocation
    val canPerformCalibration by viewModel.canPerformCalibration
    val reasonForNotCalibrating by viewModel.reasonForNotCalibrating
    val pvRequired by viewModel.pvRequired
    var showConfirmDialog by remember { mutableStateOf(false) }
    var pendingValue by remember { mutableStateOf<Boolean?>(null) }

    // Pre-fill location once per calibration
    LaunchedEffect(Unit) {
        if (viewModel.newLocation.value.isBlank()) {
            viewModel.setNewLocation(lastLocation)
        }
    }

    val isNextStepEnabled =
        canPerformCalibration || reasonForNotCalibrating.isNotBlank()

    LaunchedEffect(isNextStepEnabled) {
        viewModel.setCurrentScreenNextEnabled(isNextStepEnabled)
    }

    ScrollableWithScrollbar(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            Spacer(Modifier.height(16.dp))
            CalibrationHeader("Calibration Start")
            Spacer(Modifier.height(16.dp))

            LabeledReadOnlyField(
                label = "Serial Number",
                value = viewModel.serialNumber.value,
                helpText = "This is the unique identifier for the system."
            )

            FormSpacer()

            LabeledReadOnlyField(
                label = "Make/Model",
                value = viewModel.modelDescription.value,
                helpText = "This cannot be edited."
            )

            FormSpacer()

            LabeledReadOnlyField(
                label = "System Type",
                value = viewModel.systemTypeDescription.value,
                helpText = "This cannot be edited."
            )

            FormSpacer()

            LabeledTextFieldWithHelp(
                label = "Location",
                value = viewModel.newLocation.value,
                onValueChange = viewModel::setNewLocation,
                helpText = "Edit if the system has moved.",
                isNAToggleEnabled = false,
                maxLength = 12,
                showInputLabel = false
            )

            FormSpacer()

            LabeledRadioButtonWithHelp(
                label = "Able to calibrate?",
                value = canPerformCalibration,
                onValueChange = { newValue ->

                    // Only confirm when changing true -> false
                    if (canPerformCalibration && !newValue) {
                        pendingValue = newValue
                        showConfirmDialog = true
                    } else {
                        viewModel.setCanPerformCalibration(newValue)
                    }
                },
                helpText = "..."
            )

            if (!canPerformCalibration) {
                Spacer(Modifier.height(8.dp))

                LabeledTextFieldWithHelp(
                    label = "Reason for not calibrating",
                    value = reasonForNotCalibrating,
                    onValueChange = viewModel::setReasonForNotCalibrating,
                    helpText = "Explain why calibration cannot be performed.",
                    isNAToggleEnabled = false,
                    maxLength = 50,
                    showInputLabel = false
                )
            }

            FormSpacer()

            if (canPerformCalibration) {
                LabeledRadioButtonWithHelp(
                    label = "P.V. Required?",
                    value = pvRequired,
                    onValueChange = viewModel::setPvRequired,
                    helpText = "Select 'Yes' if machine runs M&S products."
                )
            }

            Spacer(Modifier.height(60.dp))
        }
    }
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = {
                showConfirmDialog = false
                pendingValue = null
            },
            title = { Text("Confirm change") },
            text = {
                Text("All calibration data will be wiped. Continue?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingValue?.let {
                            viewModel.setCanPerformCalibration(it)
                            viewModel.setAllResultsUtc()
                        }
                        showConfirmDialog = false
                        pendingValue = null
                    }
                ) {
                    Text("Yes, wipe data")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showConfirmDialog = false
                        pendingValue = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

}
