package com.snb.inspect.screens.service.cwCalibration

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.snb.inspect.calibrationViewModels.CalibrationCheckweigherViewModel
import com.snb.inspect.formModules.*
import com.snb.inspect.ui.theme.FormSpacer
import com.snb.inspect.ui.theme.ScrollableWithScrollbar

@Composable
fun CalCwFailsafes(viewModel: CalibrationCheckweigherViewModel) {
    val isNextStepEnabled = true // Implement logic

    LaunchedEffect(isNextStepEnabled) {
        viewModel.setCurrentScreenNextEnabled(isNextStepEnabled)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        CalibrationHeader(label = "Failsafes", isValid = isNextStepEnabled)

        ScrollableWithScrollbar(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Spacer(Modifier.height(16.dp))

                SensorSection(
                    label = "Infeed Sensor",
                    fitted = viewModel.infeedSensorFitted.value,
                    onFittedChange = viewModel::setInfeedSensorFitted,
                    testMethod = viewModel.infeedSensorTestMethod.value,
                    onTestMethodChange = viewModel::setInfeedSensorTestMethod,
                    testResult = viewModel.infeedSensorTestResult.value,
                    onTestResultChange = viewModel::setInfeedSensorTestResult,
                    latched = viewModel.infeedSensorLatched.value,
                    onLatchedChange = viewModel::setInfeedSensorLatched,
                    cr = viewModel.infeedSensorCR.value,
                    onCrChange = viewModel::setInfeedSensorCR,
                    helpText = "Details for Infeed Sensor."
                )
                FormSpacer()

                SensorSection(
                    label = "Reject Confirm Sensor",
                    fitted = viewModel.rejectConfirmSensorFitted.value,
                    onFittedChange = viewModel::setRejectConfirmSensorFitted,
                    testMethod = viewModel.rejectConfirmSensorTestMethod.value,
                    onTestMethodChange = viewModel::setRejectConfirmSensorTestMethod,
                    testResult = viewModel.rejectConfirmSensorTestResult.value,
                    onTestResultChange = viewModel::setRejectConfirmSensorTestResult,
                    latched = viewModel.rejectConfirmSensorLatched.value,
                    onLatchedChange = viewModel::setRejectConfirmSensorLatched,
                    cr = viewModel.rejectConfirmSensorCR.value,
                    onCrChange = viewModel::setRejectConfirmSensorCR,
                    helpText = "Details for Reject Confirm Sensor."
                )
                FormSpacer()

                SensorSection(
                    label = "Bin Full Sensor",
                    fitted = viewModel.binFullSensorFitted.value,
                    onFittedChange = viewModel::setBinFullSensorFitted,
                    testMethod = viewModel.binFullSensorTestMethod.value,
                    onTestMethodChange = viewModel::setBinFullSensorTestMethod,
                    testResult = viewModel.binFullSensorTestResult.value,
                    onTestResultChange = viewModel::setBinFullSensorTestResult,
                    latched = viewModel.binFullSensorLatched.value,
                    onLatchedChange = viewModel::setBinFullSensorLatched,
                    cr = viewModel.binFullSensorCR.value,
                    onCrChange = viewModel::setBinFullSensorCR,
                    helpText = "Details for Bin Full Sensor."
                )
                FormSpacer()

                SensorSection(
                    label = "Air Fail Sensor",
                    fitted = viewModel.airPressureSensorFitted.value,
                    onFittedChange = viewModel::setAirPressureSensorFitted,
                    testMethod = viewModel.airPressureSensorTestMethod.value,
                    onTestMethodChange = viewModel::setAirPressureSensorTestMethod,
                    testResult = viewModel.airPressureSensorTestResult.value,
                    onTestResultChange = viewModel::setAirPressureSensorTestResult,
                    latched = viewModel.airPressureSensorLatched.value,
                    onLatchedChange = viewModel::setAirPressureSensorLatched,
                    cr = viewModel.airPressureSensorCR.value,
                    onCrChange = viewModel::setAirPressureSensorCR,
                    helpText = "Details for Air Pressure Sensor."
                )
                FormSpacer()

                Spacer(Modifier.height(60.dp))
            }
        }
    }
}

@Composable
private fun SensorSection(
    label: String,
    fitted: YesNoState,
    onFittedChange: (YesNoState) -> Unit,
    testMethod: String,
    onTestMethodChange: (String) -> Unit,
    testResult: List<String>,
    onTestResultChange: (List<String>) -> Unit,
    latched: YesNoState,
    onLatchedChange: (YesNoState) -> Unit,
    cr: YesNoState,
    onCrChange: (YesNoState) -> Unit,
    helpText: String
) {
    LabeledYesNoNaSegmentedSwitchWithHelp(
        label = "$label Fitted?",
        currentState = fitted,
        onStateChange = onFittedChange,
        onInputValueChange = { /* Not needed for fitted state itself if it's separate */ },
        helpText = "Is the $label fitted?"
    )

    AnimatedVisibility(visible = fitted == YesNoState.YES) {
        Column {
            FormSpacer()
            val methods = remember { listOf("Blocked PEC", "Disconnected Sensor", "Other") }
            LabeledDropdownWithHelp(
                label = "Test Method",
                options = methods,
                selectedOption = testMethod,
                onSelectionChange = onTestMethodChange,
                helpText = "Method used to test the sensor."
            )
            FormSpacer()
            val results = remember { listOf("System Alarm", "Belt Stop", "Reject Action") }
            LabeledMultiSelectDropdownWithHelp(
                label = "Test Result",
                options = results,
                selectedOptions = testResult,
                onSelectionChange = onTestResultChange,
                value = testResult.joinToString(", "),
                helpText = "Outcome of the test."
            )
            FormSpacer()
            LabeledRadioButtonWithHelp(
                label = "Is Latched?",
                value = latched == YesNoState.YES,
                onValueChange = { onLatchedChange(if (it) YesNoState.YES else YesNoState.NO) },
                helpText = "Does the alarm latch?"
            )
            FormSpacer()
            LabeledRadioButtonWithHelp(
                label = "Control Reset (CR)?",
                value = cr == YesNoState.YES,
                onValueChange = { onCrChange(if (it) YesNoState.YES else YesNoState.NO) },
                helpText = "Does it require a manual reset?"
            )
        }
    }
}
