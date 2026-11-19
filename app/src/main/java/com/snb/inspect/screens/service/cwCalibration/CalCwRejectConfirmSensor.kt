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
    val testMethodOther = viewModel.rejectConfirmSensorTestMethodOther.value
    val testResult = viewModel.rejectConfirmSensorTestResult.value
    val latched = viewModel.rejectConfirmSensorLatched.value
    val cr = viewModel.rejectConfirmSensorCR.value
    val notes = viewModel.rejectConfirmSensorEngineerNotes.value

    val ind1label = viewModel.indicator1label.value
    val ind1colour = viewModel.indicator1colour.value
    val ind2label = viewModel.indicator2label.value
    val ind2colour = viewModel.indicator2colour.value
    val ind3label = viewModel.indicator3label.value
    val ind3colour = viewModel.indicator3colour.value
    val ind4label = viewModel.indicator4label.value
    val ind4colour = viewModel.indicator4colour.value
    val ind5label = viewModel.indicator5label.value
    val ind5colour = viewModel.indicator5colour.value
    val ind6label = viewModel.indicator6label.value
    val ind6colour = viewModel.indicator6colour.value

    val testMethodOptions = remember {
        listOf("Reject Override Switch/Button", "Remove Pack", "Other")
    }

    val testResultOptions = remember(
        ind1label, ind1colour,
        ind2label, ind2colour,
        ind3label, ind3colour,
        ind4label, ind4colour,
        ind5label, ind5colour,
        ind6label, ind6colour
    ) {
        buildList {
            add("No Result")
            if (ind6label.isNotBlank() && ind6label != "N/A") add("Indicator 6 ($ind6colour)")
            if (ind5label.isNotBlank() && ind5label != "N/A") add("Indicator 5 ($ind5colour)")
            if (ind4label.isNotBlank() && ind4label != "N/A") add("Indicator 4 ($ind4colour)")
            if (ind3label.isNotBlank() && ind3label != "N/A") add("Indicator 3 ($ind3colour)")
            if (ind2label.isNotBlank() && ind2label != "N/A") add("Indicator 2 ($ind2colour)")
            if (ind1label.isNotBlank() && ind1label != "N/A") add("Indicator 1 ($ind1colour)")
            addAll(
                listOf(
                    "On-Screen Notification",
                    "System Belt Stops",
                    "In-feed Belt Stops",
                    "Out-feed Belt Stops",
                    "Other"
                )
            )
        }
    }

    val isNextStepEnabled = when (fitted) {
        YesNoState.NO, YesNoState.NA -> true
        YesNoState.YES -> testMethod.isNotBlank() && testResult.isNotEmpty() && latched != YesNoState.UNSPECIFIED && cr != YesNoState.UNSPECIFIED && (testMethod != "Other" || testMethodOther.isNotBlank())
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
                        LabeledDropdownWithHelp(
                            label = "Test Method",
                            options = testMethodOptions,
                            selectedOption = testMethod,
                            onSelectionChange = {
                                if (it != "Other") viewModel.setRejectConfirmSensorTestMethodOther("")
                                viewModel.setRejectConfirmSensorTestMethod(it)
                            },
                            helpText = "Method used to test the sensor."
                        )
                        FormSpacer()

                        if (testMethod == "Other") {
                            LabeledTextFieldWithHelp(
                                label = "Other Test Method",
                                value = testMethodOther,
                                onValueChange = viewModel::setRejectConfirmSensorTestMethodOther,
                                helpText = "Enter the custom test method.",
                                isNAToggleEnabled = false,
                                maxLength = 25
                            )
                            FormSpacer()
                        }

                        LabeledMultiSelectDropdownWithHelp(
                            label = "Test Result",
                            options = testResultOptions,
                            selectedOptions = testResult,
                            onSelectionChange = { newSelection ->
                                val cleaned = if ("No Result" in newSelection) listOf("No Result")
                                              else newSelection.filterNot { it == "No Result" }
                                viewModel.setRejectConfirmSensorTestResult(cleaned)
                            },
                            value = testResult.joinToString(", "),
                            helpText = "Outcome of the test.",
                            isNAToggleEnabled = false
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
