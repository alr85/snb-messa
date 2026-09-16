package com.snb.inspect.screens.service.cwCalibration

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.snb.inspect.calibrationViewModels.CalibrationCheckweigherViewModel
import com.snb.inspect.formModules.*
import com.snb.inspect.ui.theme.FormSpacer
import com.snb.inspect.ui.theme.ScrollableWithScrollbar

@Composable
fun CalCwRejectConfirmSensor(viewModel: CalibrationCheckweigherViewModel) {
    val fitted = viewModel.rejectConfirmSensorFitted.value
    val testMethod = viewModel.rejectConfirmSensorTestMethod.value
    val testResult = viewModel.rejectConfirmSensorTestResult.value
    val latched = viewModel.rejectConfirmSensorLatched.value
    val cr = viewModel.rejectConfirmSensorCR.value
    val notes = viewModel.rejectConfirmSensorEngineerNotes.value

    val isNextStepEnabled = when (fitted) {
        YesNoState.NO, YesNoState.NA -> true
        YesNoState.YES -> testMethod.isNotBlank() && testResult.isNotEmpty() && latched != YesNoState.UNSPECIFIED && cr != YesNoState.UNSPECIFIED
        else -> false
    }

    LaunchedEffect(isNextStepEnabled) {
        viewModel.setCurrentScreenNextEnabled(isNextStepEnabled)
        viewModel.setScreenValidity("CwRejectConfirmSensor", isNextStepEnabled)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        CalibrationHeader(label = "Failsafe Tests - Reject Confirm Sensor", isValid = isNextStepEnabled)

        ScrollableWithScrollbar(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {
            Column {
                LabeledYesNoNaSegmentedSwitchWithHelp(
                    label = "Reject Confirm Sensor Fitted?",
                    currentState = fitted,
                    onStateChange = viewModel::setRejectConfirmSensorFitted,
                    onInputValueChange = {},
                    helpText = "Is the reject confirm sensor fitted to the system?"
                )

                AnimatedVisibility(visible = fitted == YesNoState.YES) {
                    Column {
                        FormSpacer()
                        val methods = remember { listOf("Blocked PEC", "Disconnected Sensor", "Other") }
                        LabeledDropdownWithHelp(
                            label = "Test Method",
                            options = methods,
                            selectedOption = testMethod,
                            onSelectionChange = viewModel::setRejectConfirmSensorTestMethod,
                            helpText = "Method used to test the sensor."
                        )
                        FormSpacer()
                        val results = remember { listOf("System Alarm", "Belt Stop", "Reject Action") }
                        LabeledMultiSelectDropdownWithHelp(
                            label = "Test Result",
                            options = results,
                            selectedOptions = testResult,
                            onSelectionChange = viewModel::setRejectConfirmSensorTestResult,
                            value = testResult.joinToString(", "),
                            helpText = "Outcome of the test."
                        )
                        FormSpacer()
                        LabeledRadioButtonWithHelp(
                            label = "Is Latched?",
                            value = latched == YesNoState.YES,
                            onValueChange = { viewModel.setRejectConfirmSensorLatched(if (it) YesNoState.YES else YesNoState.NO) },
                            helpText = "Does the alarm latch?"
                        )
                        FormSpacer()
                        LabeledRadioButtonWithHelp(
                            label = "Control Reset (CR)?",
                            value = cr == YesNoState.YES,
                            onValueChange = { viewModel.setRejectConfirmSensorCR(if (it) YesNoState.YES else YesNoState.NO) },
                            helpText = "Does it require a manual reset?"
                        )
                        FormSpacer()
                    }
                }

                LabeledTextFieldWithHelp(
                    label = "Engineer Comments",
                    value = notes,
                    onValueChange = viewModel::setRejectConfirmSensorEngineerNotes,
                    helpText = "Enter any notes relevant to this section.",
                    isNAToggleEnabled = false,
                    maxLength = 50
                )

                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}
