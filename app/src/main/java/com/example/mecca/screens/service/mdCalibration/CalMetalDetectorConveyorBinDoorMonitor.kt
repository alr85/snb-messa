package com.example.mecca.screens.service.mdCalibration

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.mecca.calibrationLogic.metalDetectorConveyor.autoUpdateBinDoorMonitorPvResult
import com.example.mecca.calibrationLogic.metalDetectorConveyor.getBinDoorMonitorPvRules
import com.example.mecca.calibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import com.example.mecca.core.InputTransforms
import com.example.mecca.formModules.CalibrationHeader
import com.example.mecca.formModules.LabeledDropdownWithHelp
import com.example.mecca.formModules.LabeledMultiSelectDropdownWithHelp
import com.example.mecca.formModules.LabeledTextFieldWithHelp
import com.example.mecca.formModules.LabeledTriStateSwitchWithHelp
import com.example.mecca.formModules.LabeledYesNoNaSegmentedSwitchWithHelp
import com.example.mecca.formModules.PvSectionSummaryCard
import com.example.mecca.formModules.YesNoState
import com.example.mecca.ui.theme.FormSpacer
import com.example.mecca.ui.theme.ScrollableWithScrollbar

@Composable
fun CalMetalDetectorConveyorBinDoorMonitor(
    viewModel: CalibrationMetalDetectorConveyorViewModel
) {
    val fitted by viewModel.binDoorMonitorFitted
    val statusAsFound by viewModel.binDoorStatusAsFound
    val unlockedIndication by viewModel.binDoorUnlockedIndication.collectAsState()
    val openIndication by viewModel.binDoorOpenIndication.collectAsState()
    val timeoutTimer by viewModel.binDoorTimeoutTimer
    val timeoutResult by viewModel.binDoorTimeoutResult.collectAsState()
    val latched by viewModel.binDoorLatched
    val controlledRestart by viewModel.binDoorCR
    val notes by viewModel.binDoorEngineerNotes

    val pvRequired = viewModel.pvRequired.value

    val asFoundOptions = remember {
        listOf("Open and Unlocked", "Open and Locked", "Closed and Unlocked", "Closed and Locked")
    }

    val indicationOptions = remember {
        listOf("No Result", "Audible Notification", "Visual Notification", "On-Screen Notification")
    }

    val timeoutResultOptions = remember {
        listOf(
            "No Result",
            "Audible Notification",
            "Visual Notification",
            "On-Screen Notification",
            "System Belt Stops",
            "In-feed Belt Stops",
            "Out-feed Belt Stops"
        )
    }

    val isNextStepEnabled = when (fitted) {
        YesNoState.NO, YesNoState.NA -> true
        YesNoState.YES -> {
            statusAsFound.isNotBlank() &&
                    unlockedIndication.isNotEmpty() &&
                    openIndication.isNotEmpty() &&
                    timeoutTimer.isNotBlank() &&
                    timeoutResult.isNotEmpty() &&
                    latched != YesNoState.NA &&
                    controlledRestart != YesNoState.NA
        }
        else -> false
    }

    LaunchedEffect(isNextStepEnabled) {
        viewModel.setCurrentScreenNextEnabled(isNextStepEnabled)
    }

    val rules = remember(
        fitted,
        statusAsFound,
        unlockedIndication,
        openIndication,
        timeoutTimer,
        timeoutResult,
        latched,
        controlledRestart
    ) {
        viewModel.getBinDoorMonitorPvRules()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        CalibrationHeader("Failsafe Tests - Bin Door Monitor")

        ScrollableWithScrollbar(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
        ) {
            Column {
                LabeledYesNoNaSegmentedSwitchWithHelp(
                    label = "Bin Door Monitor fitted?",
                    currentState = fitted,
                    onStateChange = { newState ->
                        viewModel.setBinDoorMonitorFitted(newState)
                        if (newState == YesNoState.NO || newState == YesNoState.NA) {
                            viewModel.setBinDoorMonitorDetail("")
                            viewModel.setBinDoorStatusAsFound("N/A")
                            viewModel.setBinDoorOpenIndication(emptyList())
                            viewModel.setBinDoorUnlockedIndication(emptyList())
                            viewModel.setBinDoorTimeoutTimer("")
                            viewModel.setBinDoorTimeoutResult(emptyList())
                            viewModel.setBinDoorLatched(YesNoState.NA)
                            viewModel.setBinDoorCR(YesNoState.NA)
                        } else if (newState == YesNoState.YES) {
                            viewModel.setBinDoorMonitorDetail("")
                            viewModel.setBinDoorStatusAsFound("")
                            viewModel.setBinDoorOpenIndication(emptyList())
                            viewModel.setBinDoorUnlockedIndication(emptyList())
                            viewModel.setBinDoorTimeoutTimer("")
                            viewModel.setBinDoorTimeoutResult(emptyList())
                            viewModel.setBinDoorLatched(YesNoState.NO)
                            viewModel.setBinDoorCR(YesNoState.NO)
                        }
                        viewModel.autoUpdateBinDoorMonitorPvResult()
                    },
                    helpText = "Select if a bin door monitor is fitted.",
                    onInputValueChange = {
                    },
                    pvStatus = if (pvRequired) rules.find { it.ruleId == "BIN_DOOR_FITTED" }?.status?.name else null,
                    pvRules = if (pvRequired) rules.filter { it.ruleId == "BIN_DOOR_FITTED" } else emptyList()
                )

                FormSpacer()

                if (fitted == YesNoState.YES) {
                    LabeledDropdownWithHelp(
                        label = "Bin Door Status As Found",
                        options = asFoundOptions,
                        selectedOption = statusAsFound,
                        onSelectionChange = {
                            viewModel.setBinDoorStatusAsFound(it)
                            viewModel.autoUpdateBinDoorMonitorPvResult()
                        },
                        helpText = "Select the initial state of the door.",
                        isNAToggleEnabled = false,
                        pvStatus = if (pvRequired) rules.find { it.ruleId == "BIN_DOOR_FOUND" }?.status?.name else null,
                        pvRules = if (pvRequired) rules.filter { it.ruleId == "BIN_DOOR_FOUND" } else emptyList()
                    )

                    FormSpacer()

                    LabeledMultiSelectDropdownWithHelp(
                        label = "Unlocked Indication",
                        options = indicationOptions,
                        value = unlockedIndication.joinToString(", "),
                        selectedOptions = unlockedIndication,
                        onSelectionChange = { newSelection ->
                            val cleaned = if ("No Result" in newSelection) {
                                listOf("No Result")
                            } else {
                                newSelection.filterNot { it == "No Result" }
                            }

                            viewModel.setBinDoorUnlockedIndication(cleaned)
                            viewModel.autoUpdateBinDoorMonitorPvResult()
                        },
                        helpText = "Select the indication shown when the door is unlocked.",
                        isNAToggleEnabled = false,
                        pvStatus = if (pvRequired) rules.find { it.ruleId == "BIN_DOOR_UNLOCKED_INDICATION" }?.status?.name else null,
                        pvRules = if (pvRequired) rules.filter { it.ruleId == "BIN_DOOR_UNLOCKED_INDICATION" } else emptyList()
                    )

                    FormSpacer()

                    LabeledMultiSelectDropdownWithHelp(
                        label = "Open Indication",
                        options = indicationOptions,
                        value = openIndication.joinToString(", "),
                        selectedOptions = openIndication,
                        onSelectionChange = { newSelection ->
                            val cleaned = if ("No Result" in newSelection) {
                                listOf("No Result")
                            } else {
                                newSelection.filterNot { it == "No Result" }
                            }

                            viewModel.setBinDoorOpenIndication(cleaned)
                            viewModel.autoUpdateBinDoorMonitorPvResult()
                        },
                        helpText = "Select the indication shown when the door is open.",
                        isNAToggleEnabled = false,
                        pvStatus = if (pvRequired) rules.find { it.ruleId == "BIN_DOOR_OPEN_INDICATION" }?.status?.name else null,
                        pvRules = if (pvRequired) rules.filter { it.ruleId == "BIN_DOOR_OPEN_INDICATION" } else emptyList()
                    )

                    FormSpacer()

                    LabeledTextFieldWithHelp(
                        label = "Bin Door Timeout (secs)",
                        value = timeoutTimer,
                        onValueChange = {
                            viewModel.setBinDoorTimeoutTimer(it)
                            viewModel.autoUpdateBinDoorMonitorPvResult()
                        },
                        helpText = "Enter the timeout value in seconds.",
                        isNAToggleEnabled = false,
                        maxLength = 3,
                        keyboardType = KeyboardType.Number,
                        transformInput = { input -> input.filter { it.isDigit() } },
                        pvStatus = if (pvRequired) rules.find { it.ruleId == "BIN_DOOR_TIMEOUT" }?.status?.name else null,
                        pvRules = if (pvRequired) rules.filter { it.ruleId == "BIN_DOOR_TIMEOUT" } else emptyList()
                    )

                    FormSpacer()

                    LabeledMultiSelectDropdownWithHelp(
                        label = "Timeout Result",
                        options = timeoutResultOptions,
                        value = timeoutResult.joinToString(", "),
                        selectedOptions = timeoutResult,
                        onSelectionChange = { newSelection ->
                            val cleaned = if ("No Result" in newSelection) {
                                listOf("No Result")
                            } else {
                                newSelection.filterNot { it == "No Result" }
                            }

                            viewModel.setBinDoorTimeoutResult(cleaned)
                            viewModel.autoUpdateBinDoorMonitorPvResult()
                        },
                        helpText = "Select the observed result when the timeout occurs.",
                        isNAToggleEnabled = false,
                        pvStatus = if (pvRequired) rules.find { it.ruleId == "BIN_DOOR_TIMEOUT_RESULT" }?.status?.name else null,
                        pvRules = if (pvRequired) rules.filter { it.ruleId == "BIN_DOOR_TIMEOUT_RESULT" } else emptyList()
                    )

                    FormSpacer()

                    LabeledTriStateSwitchWithHelp(
                        label = "Fault Latched?",
                        currentState = latched,
                        onStateChange = {
                            viewModel.setBinDoorLatched(it)
                            viewModel.autoUpdateBinDoorMonitorPvResult()
                        },
                        helpText = "Is the fault output latched, or does it clear automatically?",
                        isNAToggleEnabled = false,
                        pvStatus = if (pvRequired) rules.find { it.ruleId == "BIN_DOOR_LATCHED" }?.status?.name else null,
                        pvRules = if (pvRequired) rules.filter { it.ruleId == "BIN_DOOR_LATCHED" } else emptyList()
                    )

                    FormSpacer()

                    LabeledTriStateSwitchWithHelp(
                        label = "Controlled Restart?",
                        currentState = controlledRestart,
                        onStateChange = {
                            viewModel.setBinDoorCR(it)
                            viewModel.autoUpdateBinDoorMonitorPvResult()
                        },
                        helpText = "Fault must be latched and require a controlled restart.",
                        isNAToggleEnabled = false,
                        pvStatus = if (pvRequired) rules.find { it.ruleId == "BIN_DOOR_CR" }?.status?.name else null,
                        pvRules = if (pvRequired) rules.filter { it.ruleId == "BIN_DOOR_CR" } else emptyList()
                    )

                    if (!pvRequired) FormSpacer()
                }

                if (pvRequired) {
                    PvSectionSummaryCard(
                        title = "Bin door monitor test P.V. Summary",
                        rules = rules
                    )
                }

                LabeledTextFieldWithHelp(
                    label = "Engineer Comments",
                    value = notes,
                    onValueChange = viewModel::setBinDoorEngineerNotes,
                    helpText = "Optional notes for this section.",
                    isNAToggleEnabled = false,
                    maxLength = 50
                )

                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}