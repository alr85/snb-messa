package com.example.mecca.screens.service.mdCalibration

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mecca.calibrationLogic.metalDetectorConveyor.autoUpdateBinDoorMonitorPvResult
import com.example.mecca.calibrationLogic.metalDetectorConveyor.getBinDoorMonitorPvRules
import com.example.mecca.calibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import com.example.mecca.core.InputTransforms
import com.example.mecca.formModules.*
import com.example.mecca.ui.theme.FormSpacer
import com.example.mecca.ui.theme.ScrollableWithScrollbar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalMetalDetectorConveyorBinDoorMonitor(
    viewModel: CalibrationMetalDetectorConveyorViewModel
) {

    val fitted by viewModel.binDoorMonitorFitted
    val detail by viewModel.binDoorMonitorDetail
    val statusAsFound by viewModel.binDoorStatusAsFound
    val unlockedIndication by viewModel.binDoorUnlockedIndication.collectAsState()
    val openIndication by viewModel.binDoorOpenIndication.collectAsState()
    val timeoutTimer by viewModel.binDoorTimeoutTimer
    val timeoutResult by viewModel.binDoorTimeoutResult.collectAsState()
    val latched by viewModel.binDoorLatched
    val controlledRestart by viewModel.binDoorCR
    val notes by viewModel.binDoorEngineerNotes

    val pvRequired = viewModel.pvRequired.value

    // Options
    val asFoundOptions = remember {
        listOf("Open and Unlocked", "Open and Locked", "Closed and Unlocked", "Closed and Locked")
    }
    val indicationOptions = remember {
        listOf("No Result", "Audible Notification", "Visual Notification", "On-Screen Notification")
    }
    val timeoutResultOptions = remember {
        listOf("No Result", "Audible Notification", "Visual Notification", "On-Screen Notification", "Belt Stops", "In-feed Belt Stops", "Out-feed Belt Stops", "Other")
    }

    // Validation for Next
    val isNextStepEnabled = when (fitted) {
        YesNoState.NO, YesNoState.NA -> true
        YesNoState.YES -> {
            detail.isNotBlank() &&
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

    val rules = viewModel.getBinDoorMonitorPvRules()

    Column(modifier = Modifier.fillMaxSize()) {
        CalibrationHeader("Failsafe Tests - Bin Door Monitor")

        ScrollableWithScrollbar(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
        ) {
            Column {
                LabeledYesNoSegmentedSwitchAndTextInputWithHelp(
                    label = "Bin Door Monitor fitted?",
                    currentState = fitted,
                    onStateChange = { newState ->
                        viewModel.setBinDoorMonitorFitted(newState)
                        if (newState != YesNoState.YES) {
                            viewModel.setBinDoorMonitorDetail("N/A")
                            viewModel.setBinDoorStatusAsFound("N/A")
                            viewModel.setBinDoorOpenIndication(emptyList())
                            viewModel.setBinDoorUnlockedIndication(emptyList())
                            viewModel.setBinDoorTimeoutTimer("N/A")
                            viewModel.setBinDoorTimeoutResult(emptyList())
                            viewModel.setBinDoorLatched(YesNoState.NA)
                            viewModel.setBinDoorCR(YesNoState.NA)
                        } else {
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
                    inputLabel = "Detail",
                    inputValue = detail,
                    onInputValueChange = {
                        viewModel.setBinDoorMonitorDetail(it)
                        viewModel.autoUpdateBinDoorMonitorPvResult()
                    },
                    pvStatus = if (pvRequired) {
                        if (fitted == YesNoState.YES) rules.getOrNull(0)?.status?.name else "N/A"
                    } else null,
                    pvRules = if (pvRequired) {
                        if (fitted == YesNoState.YES) listOfNotNull(rules.getOrNull(0)) else rules
                    } else emptyList(),
                    inputMaxLength = 12
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
                        pvStatus = if (pvRequired) rules.getOrNull(1)?.status?.name else null,
                        pvRules = if (pvRequired) listOfNotNull(rules.getOrNull(1)) else emptyList()
                    )

                    FormSpacer()

                    LabeledMultiSelectDropdownWithHelp(
                        label = "Bin Door Indication",
                        options = indicationOptions,
                        value = (openIndication + unlockedIndication).distinct().joinToString(", "),
                        selectedOptions = openIndication, // Matches logic in getBinDoorMonitorPvRules
                        onSelectionChange = { 
                            viewModel.setBinDoorOpenIndication(it)
                            viewModel.setBinDoorUnlockedIndication(it)
                            viewModel.autoUpdateBinDoorMonitorPvResult()
                        },
                        helpText = "Unlocked and Open indication results.",
                        isNAToggleEnabled = false,
                        pvStatus = if (pvRequired) rules.getOrNull(2)?.status?.name else null,
                        pvRules = if (pvRequired) listOfNotNull(rules.getOrNull(2)) else emptyList()
                    )

                    FormSpacer()

                    LabeledTextFieldWithHelp(
                        label = "Bin Door Timeout (secs)",
                        value = timeoutTimer,
                        onValueChange = {
                            viewModel.setBinDoorTimeoutTimer(it)
                            viewModel.autoUpdateBinDoorMonitorPvResult()
                        },
                        helpText = "Timer value and result recorded.",
                        isNAToggleEnabled = false,
                        pvStatus = if (pvRequired) rules.getOrNull(3)?.status?.name else null,
                        pvRules = if (pvRequired) listOfNotNull(rules.getOrNull(3)) else emptyList()
                    )

                    FormSpacer()

                    LabeledTriStateSwitchWithHelp(
                        label = "Latch & Restart",
                        currentState = latched,
                        onStateChange = {
                            viewModel.setBinDoorLatched(it)
                            viewModel.setBinDoorCR(it)
                            viewModel.autoUpdateBinDoorMonitorPvResult()
                        },
                        helpText = "Fault must be latched and require a controlled restart.",
                        isNAToggleEnabled = false,
                        pvStatus = if (pvRequired) rules.getOrNull(4)?.status?.name else null,
                        pvRules = if (pvRequired) listOfNotNull(rules.getOrNull(4)) else emptyList()
                    )

                    FormSpacer()
                }

                if (pvRequired) {
                    LabeledFourOptionRadioWithHelp(
                        label = "P.V. Result",
                        value = viewModel.binDoorMonitorTestPvResult.value,
                        onValueChange = viewModel::setBinDoorMonitorTestPvResult,
                        helpText = "Overall status for Bin Door Monitor failsafe validation."
                    )
                    FormSpacer()
                    PvSectionSummaryCard(title = "Bin door monitor test P.V. Summary", rules = rules)
                    FormSpacer()
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
