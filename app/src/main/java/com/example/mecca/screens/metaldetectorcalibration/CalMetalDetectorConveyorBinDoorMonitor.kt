package com.example.mecca.screens.metaldetectorcalibration

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.mecca.calibrationLogic.metalDetectorConveyor.autoUpdateBinDoorMonitorPvResult
import com.example.mecca.calibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import com.example.mecca.formModules.CalibrationHeader
import com.example.mecca.formModules.LabeledDropdownWithHelp
import com.example.mecca.formModules.LabeledFourOptionRadioWithHelp
import com.example.mecca.formModules.LabeledMultiSelectDropdownWithHelp
import com.example.mecca.formModules.LabeledTextFieldWithHelp
import com.example.mecca.formModules.LabeledTriStateSwitchAndTextInputWithHelp
import com.example.mecca.formModules.LabeledTriStateSwitchWithHelp
import com.example.mecca.formModules.LabeledYesNoSegmentedSwitchAndTextInputWithHelp
import com.example.mecca.formModules.YesNoState
import com.example.mecca.ui.theme.ScrollableWithScrollbar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalMetalDetectorConveyorBinDoorMonitor(
    navController: NavHostController,
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

    // Options: remember so Compose doesn’t rebuild them constantly
    val asFoundOptions = remember {
        listOf(
            "Open and Unlocked",
            "Open and Locked",
            "Closed and Unlocked",
            "Closed and Locked"
        )
    }

    val indicationOptions = remember {
        listOf(
            "No Result",
            "Audible Notification",
            "Visual Notification",
            "On-Screen Notification",
        )
    }

    val timeoutResultOptions = remember {
        listOf(
            "No Result",
            "Audible Notification",
            "Visual Notification",
            "On-Screen Notification",
            "Belt Stops",
            "In-feed Belt Stops",
            "Out-feed Belt Stops",
            "Other"
        )
    }

    // Next enabled
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

    // Tell wrapper
    LaunchedEffect(isNextStepEnabled) {
        viewModel.setCurrentScreenNextEnabled(isNextStepEnabled)
    }

    Column(modifier = Modifier.fillMaxSize()) {

        CalibrationHeader("Failsafe Tests - Bin Door Monitor")

        ScrollableWithScrollbar(
            modifier = Modifier
                .fillMaxSize()
                .imePadding(),
            contentPadding = PaddingValues(16.dp),
        ) {

            Column {

                LabeledYesNoSegmentedSwitchAndTextInputWithHelp(
                    label = "Bin Door Monitor fitted?",
                    currentState = fitted,
                    onStateChange = { newState ->
                        viewModel.setBinDoorMonitorFitted(newState)

                        if (newState == YesNoState.NA || newState == YesNoState.NO) {
                            viewModel.setBinDoorMonitorDetail("N/A")
                            viewModel.setBinDoorStatusAsFound("N/A")
                            viewModel.setBinDoorOpenIndication(emptyList())
                            viewModel.setBinDoorUnlockedIndication(emptyList())
                            viewModel.setBinDoorTimeoutTimer("N/A")
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
                    inputLabel = "Detail",
                    inputValue = detail,
                    onInputValueChange = {
                        viewModel.setBinDoorMonitorDetail(it)
                        viewModel.autoUpdateBinDoorMonitorPvResult()
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (fitted == YesNoState.YES) {

                    LabeledDropdownWithHelp(
                        label = "Bin Door Status As Found",
                        options = asFoundOptions,
                        selectedOption = statusAsFound,
                        onSelectionChange = {
                            viewModel.setBinDoorStatusAsFound(it)
                            viewModel.autoUpdateBinDoorMonitorPvResult()
                        },
                        helpText = "Select one option from the dropdown.",
                        isNAToggleEnabled = false
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    LabeledMultiSelectDropdownWithHelp(
                        label = "Bin Door Open Indication",
                        options = indicationOptions,
                        value = openIndication.joinToString(" + "),
                        selectedOptions = openIndication,
                        onSelectionChange = { newSelection ->
                            val cleaned = when {
                                "No Result" in newSelection -> listOf("No Result")
                                else -> newSelection.filterNot { it == "No Result" }
                            }
                            viewModel.setBinDoorOpenIndication(cleaned)

                            if (cleaned == listOf("No Result")) {
                                viewModel.setBinDoorLatched(YesNoState.NO)
                                viewModel.setBinDoorCR(YesNoState.NO)
                            }

                            viewModel.autoUpdateBinDoorMonitorPvResult()
                        },
                        helpText = "What indication is shown when the bin door is OPEN?",
                        isNAToggleEnabled = false
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    LabeledMultiSelectDropdownWithHelp(
                        label = "Bin Door Unlocked Indication",
                        options = indicationOptions,
                        value = unlockedIndication.joinToString(" + "),
                        selectedOptions = unlockedIndication,
                        onSelectionChange = { newSelection ->
                            val cleaned = when {
                                "No Result" in newSelection -> listOf("No Result")
                                else -> newSelection.filterNot { it == "No Result" }
                            }
                            viewModel.setBinDoorUnlockedIndication(cleaned)

                            if (cleaned == listOf("No Result")) {
                                viewModel.setBinDoorLatched(YesNoState.NO)
                                viewModel.setBinDoorCR(YesNoState.NO)
                            }

                            viewModel.autoUpdateBinDoorMonitorPvResult()
                        },
                        helpText = "What indication is shown when the bin door is UNLOCKED?",
                        isNAToggleEnabled = false
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    LabeledTextFieldWithHelp(
                        label = "Bin Door Timeout (secs)",
                        value = timeoutTimer,
                        onValueChange = {
                            viewModel.setBinDoorTimeoutTimer(it)
                            viewModel.autoUpdateBinDoorMonitorPvResult()
                        },
                        helpText = "How many seconds until the system acknowledges the bin door has been left open/unlocked?",
                        isNAToggleEnabled = false,
                        keyboardType = KeyboardType.Number
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    LabeledMultiSelectDropdownWithHelp(
                        label = "Bin Door Timeout Result",
                        options = timeoutResultOptions,
                        value = timeoutResult.joinToString(" + "),
                        selectedOptions = timeoutResult,
                        onSelectionChange = { newSelection ->

                            val cleaned = when {
                                "No Result" in newSelection -> listOf("No Result")
                                else -> newSelection.filterNot { it == "No Result" }
                            }
                            viewModel.setBinDoorTimeoutResult(cleaned)

                            if (cleaned == listOf("No Result")) {
                                viewModel.setBinDoorLatched(YesNoState.NO)
                                viewModel.setBinDoorCR(YesNoState.NO)
                            }

                            viewModel.autoUpdateBinDoorMonitorPvResult()
                        },
                        helpText = "What happens after the bin door timer elapses?",
                        isNAToggleEnabled = false
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    LabeledTriStateSwitchWithHelp(
                        label = "Fault Latched?",
                        currentState = latched,
                        onStateChange = {
                            viewModel.setBinDoorLatched(it)
                            viewModel.autoUpdateBinDoorMonitorPvResult()
                        },
                        helpText = "Is the fault output latched, or does it clear automatically?",
                        isNAToggleEnabled = false
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    LabeledTriStateSwitchWithHelp(
                        label = "Controlled Restart?",
                        currentState = controlledRestart,
                        onStateChange = {
                            viewModel.setBinDoorCR(it)
                            viewModel.autoUpdateBinDoorMonitorPvResult()
                        },
                        helpText = "Is a controlled restart required after a fault?",
                        isNAToggleEnabled = false
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                //-----------------------------------------------------
                // ⭐ PV RESULT (only when required)
                //-----------------------------------------------------
                if (viewModel.pvRequired.value) {
                    LabeledFourOptionRadioWithHelp(
                        label = "P.V. Result",
                        value = viewModel.binDoorMonitorTestPvResult.value,
                        onValueChange = viewModel::setBinDoorMonitorTestPvResult,
                        helpText = """
                        Auto-Pass rules (when PV required):
                          • Monitor fitted = Yes
                          • Detail entered
                          • Status-as-found selected
                          • Open indication selected (not "No Result")
                          • Unlocked indication selected (not "No Result")
                          • Timeout value entered
                          • Timeout result selected (not "No Result")
                          • Fault Latched = Yes
                          • Controlled Restart = Yes

                        If monitor is No → PV = N/F.
                        If monitor is N/A → PV = N/A.
                        Otherwise auto-fail. You may override manually.
                    """.trimIndent()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                LabeledTextFieldWithHelp(
                    label = "Engineer Comments",
                    value = notes,
                    onValueChange = viewModel::setBinDoorEngineerNotes,
                    helpText = "Enter any notes relevant to this section.",
                    isNAToggleEnabled = false
                )

                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}