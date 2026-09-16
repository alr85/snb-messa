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

    val timeoutResultOptions = remember {
        listOf(
            "No Result",
            "On-Screen Notification",
            "System Belt Stops",
            "In-feed Belt Stops",
            "Out-feed Belt Stops",
            "Other"
        )
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
