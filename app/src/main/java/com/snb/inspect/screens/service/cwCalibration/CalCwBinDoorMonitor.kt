package com.snb.inspect.screens.service.cwCalibration

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.snb.inspect.calibrationViewModels.CalibrationCheckweigherViewModel
import com.snb.inspect.formModules.*
import com.snb.inspect.ui.theme.FormSpacer
import com.snb.inspect.ui.theme.ScrollableWithScrollbar

@Composable
fun CalCwBinDoorMonitor(viewModel: CalibrationCheckweigherViewModel) {
    val fitted = viewModel.binDoorMonitorFitted.value
    val unlockedIndication = viewModel.binDoorUnlockedIndication.value
    val openIndication = viewModel.binDoorOpenIndication.value
    val timeoutTimer = viewModel.binDoorTimeoutTimer.value
    val timeoutResult = viewModel.binDoorTimeoutResult.value
    val latched = viewModel.binDoorLatched.value
    val cr = viewModel.binDoorCR.value
    val notes = viewModel.binDoorEngineerNotes.value

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

    val isNextStepEnabled = when (fitted) {
        YesNoState.NO, YesNoState.NA -> true
        YesNoState.YES -> unlockedIndication.isNotEmpty() && openIndication.isNotEmpty() && timeoutTimer.isNotBlank() && timeoutResult.isNotEmpty() && latched != YesNoState.UNSPECIFIED && cr != YesNoState.UNSPECIFIED
        else -> false
    }

    LaunchedEffect(isNextStepEnabled) {
        viewModel.setCurrentScreenNextEnabled(isNextStepEnabled)
        viewModel.setScreenValidity("CwBinDoorMonitor", isNextStepEnabled)
    }

    val indicationOptions = remember {
        listOf("No Result", "Audible Notification", "Visual Notification", "On-Screen Notification")
    }

    val timeoutResultOptions = remember(
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

    Column(modifier = Modifier.fillMaxSize()) {
        CalibrationHeader(label = "Failsafe Tests - Bin Door Monitor", isValid = isNextStepEnabled)

        ScrollableWithScrollbar(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {
            Column {
                LabeledYesNoNaSegmentedSwitchWithHelp(
                    label = "Bin Door Monitor Fitted?",
                    currentState = fitted,
                    onStateChange = viewModel::setBinDoorMonitorFitted,
                    onInputValueChange = {},
                    helpText = "Is a bin door monitor fitted to the system?"
                )

                AnimatedVisibility(visible = fitted == YesNoState.YES) {
                    Column {
                        FormSpacer()
                        LabeledMultiSelectDropdownWithHelp(
                            label = "Unlocked Indication",
                            options = indicationOptions,
                            value = unlockedIndication.joinToString(", "),
                            selectedOptions = unlockedIndication,
                            onSelectionChange = { newSelection ->
                                val cleaned = if ("No Result" in newSelection) listOf("No Result") else newSelection.filterNot { it == "No Result" }
                                viewModel.setBinDoorUnlockedIndication(cleaned)
                            },
                            helpText = "Select the indication shown when the door is unlocked.",
                            isNAToggleEnabled = false
                        )
                        FormSpacer()
                        LabeledMultiSelectDropdownWithHelp(
                            label = "Open Indication",
                            options = indicationOptions,
                            value = openIndication.joinToString(", "),
                            selectedOptions = openIndication,
                            onSelectionChange = { newSelection ->
                                val cleaned = if ("No Result" in newSelection) listOf("No Result") else newSelection.filterNot { it == "No Result" }
                                viewModel.setBinDoorOpenIndication(cleaned)
                            },
                            helpText = "Select the indication shown when the door is open.",
                            isNAToggleEnabled = false
                        )
                        FormSpacer()
                        LabeledTextFieldWithHelp(
                            label = "Bin Door Timeout (secs)",
                            value = timeoutTimer,
                            onValueChange = viewModel::setBinDoorTimeoutTimer,
                            helpText = "Enter the timeout value in seconds.",
                            isNAToggleEnabled = false,
                            maxLength = 3,
                            keyboardType = KeyboardType.Number,
                            transformInput = { input -> input.filter { it.isDigit() } }
                        )
                        FormSpacer()
                        LabeledMultiSelectDropdownWithHelp(
                            label = "Timeout Result",
                            options = timeoutResultOptions,
                            value = timeoutResult.joinToString(", "),
                            selectedOptions = timeoutResult,
                            onSelectionChange = { newSelection ->
                                val cleaned = if ("No Result" in newSelection) listOf("No Result") else newSelection.filterNot { it == "No Result" }
                                viewModel.setBinDoorTimeoutResult(cleaned)
                            },
                            helpText = "Select the observed result when the timeout occurs.",
                            isNAToggleEnabled = false
                        )
                        FormSpacer()
                        LabeledRadioButtonWithHelp(
                            label = "Is Latched?",
                            value = latched == YesNoState.YES,
                            onValueChange = { viewModel.setBinDoorLatched(if (it) YesNoState.YES else YesNoState.NO) },
                            helpText = "Does the alarm latch?"
                        )
                        FormSpacer()
                        LabeledRadioButtonWithHelp(
                            label = "Control Reset (CR)?",
                            value = cr == YesNoState.YES,
                            onValueChange = { viewModel.setBinDoorCR(if (it) YesNoState.YES else YesNoState.NO) },
                            helpText = "Does it require a manual reset?"
                        )
                        FormSpacer()
                    }
                }

                LabeledTextFieldWithHelp(
                    label = "Engineer Comments",
                    value = notes,
                    onValueChange = viewModel::setBinDoorEngineerNotes,
                    helpText = "Enter any notes relevant to this section.",
                    isNAToggleEnabled = false,
                    maxLength = 50
                )

                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}
