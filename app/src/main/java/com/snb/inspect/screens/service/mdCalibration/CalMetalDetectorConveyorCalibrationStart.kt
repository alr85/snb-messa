package com.snb.inspect.screens.service.mdCalibration

//import com.snb.inspect.screens.getAppVersion
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
import com.snb.inspect.calibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import com.snb.inspect.formModules.CalibrationHeader
import com.snb.inspect.formModules.LabeledDropdownWithHelp
import com.snb.inspect.formModules.LabeledRadioButtonWithHelp
import com.snb.inspect.formModules.LabeledReadOnlyField
import com.snb.inspect.formModules.LabeledTextFieldWithHelp
import com.snb.inspect.ui.theme.FormSpacer
import com.snb.inspect.ui.theme.ScrollableWithScrollbar


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
                maxLength = 30,
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
                helpText = "Are you able to complete a calibration/PV procedure today?"
            )

            FormSpacer()

            if (!canPerformCalibration) {
                Spacer(Modifier.height(8.dp))

                val commonReasons = remember {
                    listOf(
                        "No product available",
                        "No power/air supply",
                        "Unable to locate",
                        "System unsafe",
                        "System faulty/inoperative",
                        "Other"
                    )
                }

                var isOtherSelected by remember {
                    mutableStateOf(reasonForNotCalibrating.isNotEmpty() && reasonForNotCalibrating !in commonReasons.dropLast(1))
                }

                LabeledDropdownWithHelp(
                    label = "Reason for not calibrating",
                    options = commonReasons,
                    selectedOption = if (isOtherSelected) "Other" else reasonForNotCalibrating,
                    onSelectionChange = { selection ->
                        if (selection == "Other") {
                            isOtherSelected = true
                            if (reasonForNotCalibrating in commonReasons.dropLast(1)) {
                                viewModel.setReasonForNotCalibrating("")
                            }
                        } else {
                            isOtherSelected = false
                            viewModel.setReasonForNotCalibrating(selection)
                        }
                    },
                    helpText = "Select a reason why calibration cannot be performed.",
                    isNAToggleEnabled = false
                )

                FormSpacer()

                if (isOtherSelected) {
                    LabeledTextFieldWithHelp(
                        label = "Other reason",
                        value = reasonForNotCalibrating,
                        onValueChange = viewModel::setReasonForNotCalibrating,
                        helpText = "Explain why calibration cannot be performed.",
                        isNAToggleEnabled = false,
                        maxLength = 50,
                        showInputLabel = false
                    )

                    FormSpacer()
                }
            }



            if (canPerformCalibration) {
                LabeledRadioButtonWithHelp(
                    label = "M&S Performance Verification (PV) Required?",
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
