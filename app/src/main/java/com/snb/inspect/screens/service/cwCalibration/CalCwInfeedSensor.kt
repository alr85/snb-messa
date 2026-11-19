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
fun CalCwInfeedSensor(viewModel: CalibrationCheckweigherViewModel) {
    val fitted = viewModel.infeedSensorFitted.value
    val testMethod = viewModel.infeedSensorTestMethod.value
    val testMethodOther = viewModel.infeedSensorTestMethodOther.value
    val testResult = viewModel.infeedSensorTestResult.value
    val latched = viewModel.infeedSensorLatched.value
    val cr = viewModel.infeedSensorCR.value
    val notes = viewModel.infeedSensorEngineerNotes.value

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
        listOf("Manual Block", "Device Block", "Other")
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
        viewModel.setScreenValidity("CwInfeedSensor", isNextStepEnabled)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        CalibrationHeader(label = "Failsafe Tests - Infeed Sensor", isValid = isNextStepEnabled)

        ScrollableWithScrollbar(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {
            Column {
                LabeledYesNoNaSegmentedSwitchWithHelp(
                    label = "Infeed Sensor Fitted?",
                    currentState = fitted,
                    onStateChange = viewModel::setInfeedSensorFitted,
                    onInputValueChange = {},
                    helpText = "Is the infeed sensor fitted to the system?"
                )

                AnimatedVisibility(visible = fitted == YesNoState.YES) {
                    Column {
                        FormSpacer()
                        LabeledDropdownWithHelp(
                            label = "Test Method",
                            options = testMethodOptions,
                            selectedOption = testMethod,
                            onSelectionChange = {
                                if (it != "Other") viewModel.setInfeedSensorTestMethodOther("")
                                viewModel.setInfeedSensorTestMethod(it)
                            },
                            helpText = "Method used to test the sensor."
                        )
                        FormSpacer()

                        if (testMethod == "Other") {
                            LabeledTextFieldWithHelp(
                                label = "Other Test Method",
                                value = testMethodOther,
                                onValueChange = viewModel::setInfeedSensorTestMethodOther,
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
                                viewModel.setInfeedSensorTestResult(cleaned)
                            },
                            value = testResult.joinToString(", "),
                            helpText = "Outcome of the test.",
                            isNAToggleEnabled = false
                        )
                        FormSpacer()

                        LabeledRadioButtonWithHelp(
                            label = "Is Latched?",
                            value = latched == YesNoState.YES,
                            onValueChange = { viewModel.setInfeedSensorLatched(if (it) YesNoState.YES else YesNoState.NO) },
                            helpText = "Does the alarm latch?"
                        )
                        FormSpacer()

                        LabeledRadioButtonWithHelp(
                            label = "Control Reset (CR)?",
                            value = cr == YesNoState.YES,
                            onValueChange = { viewModel.setInfeedSensorCR(if (it) YesNoState.YES else YesNoState.NO) },
                            helpText = "Does it require a manual reset?"
                        )
                        FormSpacer()
                    }
                }

                LabeledTextFieldWithHelp(
                    label = "Engineer Comments",
                    value = notes,
                    onValueChange = viewModel::setInfeedSensorEngineerNotes,
                    helpText = "Enter any notes relevant to this section.",
                    isNAToggleEnabled = false,
                    maxLength = 50
                )

                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}
