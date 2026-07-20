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
fun CalCwBinFullSensor(viewModel: CalibrationCheckweigherViewModel) {
    val fitted = viewModel.binFullSensorFitted.value
    val testMethod = viewModel.binFullSensorTestMethod.value
    val testResult = viewModel.binFullSensorTestResult.value
    val latched = viewModel.binFullSensorLatched.value
    val cr = viewModel.binFullSensorCR.value
    val notes = viewModel.binFullSensorEngineerNotes.value

    val isNextStepEnabled = when (fitted) {
        YesNoState.NO, YesNoState.NA -> true
        YesNoState.YES -> testMethod.isNotBlank() && testResult.isNotEmpty() && latched != YesNoState.UNSPECIFIED && cr != YesNoState.UNSPECIFIED
        else -> false
    }

    LaunchedEffect(isNextStepEnabled) {
        viewModel.setCurrentScreenNextEnabled(isNextStepEnabled)
        viewModel.setScreenValidity("CwBinFullSensor", isNextStepEnabled)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        CalibrationHeader(label = "Failsafe Tests - Bin Full Sensor", isValid = isNextStepEnabled)

        ScrollableWithScrollbar(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {
            Column {
                LabeledYesNoNaSegmentedSwitchWithHelp(
                    label = "Bin Full Sensor Fitted?",
                    currentState = fitted,
                    onStateChange = viewModel::setBinFullSensorFitted,
                    onInputValueChange = {},
                    helpText = "Is the bin full sensor fitted to the system?"
                )

                AnimatedVisibility(visible = fitted == YesNoState.YES) {
                    Column {
                        FormSpacer()
                        val methods = remember { listOf("Blocked PEC", "Disconnected Sensor", "Other") }
                        LabeledDropdownWithHelp(
                            label = "Test Method",
                            options = methods,
                            selectedOption = testMethod,
                            onSelectionChange = viewModel::setBinFullSensorTestMethod,
                            helpText = "Method used to test the sensor."
                        )
                        FormSpacer()
                        val results = remember { listOf("System Alarm", "Belt Stop", "Reject Action") }
                        LabeledMultiSelectDropdownWithHelp(
                            label = "Test Result",
                            options = results,
                            selectedOptions = testResult,
                            onSelectionChange = viewModel::setBinFullSensorTestResult,
                            value = testResult.joinToString(", "),
                            helpText = "Outcome of the test."
                        )
                        FormSpacer()
                        LabeledRadioButtonWithHelp(
                            label = "Is Latched?",
                            value = latched == YesNoState.YES,
                            onValueChange = { viewModel.setBinFullSensorLatched(if (it) YesNoState.YES else YesNoState.NO) },
                            helpText = "Does the alarm latch?"
                        )
                        FormSpacer()
                        LabeledRadioButtonWithHelp(
                            label = "Control Reset (CR)?",
                            value = cr == YesNoState.YES,
                            onValueChange = { viewModel.setBinFullSensorCR(if (it) YesNoState.YES else YesNoState.NO) },
                            helpText = "Does it require a manual reset?"
                        )
                        FormSpacer()
                    }
                }

                LabeledTextFieldWithHelp(
                    label = "Engineer Comments",
                    value = notes,
                    onValueChange = viewModel::setBinFullSensorEngineerNotes,
                    helpText = "Enter any notes relevant to this section.",
                    isNAToggleEnabled = false,
                    maxLength = 50
                )

                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}
