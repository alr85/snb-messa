package com.example.mecca.screens.service.mdCalibration

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mecca.calibrationLogic.metalDetectorConveyor.autoUpdateRejectConfirmSensorPvResult
import com.example.mecca.calibrationLogic.metalDetectorConveyor.getRejectConfirmSensorPvRules
import com.example.mecca.calibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import com.example.mecca.formModules.*
import com.example.mecca.ui.theme.FormSpacer
import com.example.mecca.ui.theme.ScrollableWithScrollbar

@Composable
fun CalMetalDetectorConveyorRejectConfirmPEC(
    viewModel: CalibrationMetalDetectorConveyorViewModel
) {
    val fitted by viewModel.rejectConfirmSensorFitted
    val detail by viewModel.rejectConfirmSensorDetail
    val testMethod by viewModel.rejectConfirmSensorTestMethod
    val testMethodOther by viewModel.rejectConfirmSensorTestMethodOther
    val testResult by viewModel.rejectConfirmSensorTestResult.collectAsState()
    val stopPosition by viewModel.rejectConfirmSensorStopPosition
    val latched by viewModel.rejectConfirmSensorLatched
    val controlledRestart by viewModel.rejectConfirmSensorCR
    val notes by viewModel.rejectConfirmSensorEngineerNotes

    val pvRequired = viewModel.pvRequired.value

    val testMethodOptions = remember {
        listOf("Timed Internal Test", "External Trigger", "Other")
    }

    val testResultOptions = remember {
        listOf("No Result", "Audible Notification", "Visual Notification", "On-Screen Notification", "Belt Stops", "In-feed Belt Stops", "Out-feed Belt Stops", "Other")
    }

    val stopPositionOptions = remember {
        listOf("Weigh Platform", "In-feed Belt", "Out-feed Belt (Uncontrolled)", "Other")
    }

    val isNextStepEnabled = when (fitted) {
        YesNoState.NO, YesNoState.NA -> true
        YesNoState.YES -> {
            detail.isNotBlank() &&
                    testMethod.isNotBlank() &&
                    testResult.isNotEmpty() &&
                    stopPosition.isNotBlank() &&
                    latched != YesNoState.NA &&
                    controlledRestart != YesNoState.NA
        }
        else -> false
    }

    LaunchedEffect(isNextStepEnabled) {
        viewModel.setCurrentScreenNextEnabled(isNextStepEnabled)
    }

    val rules = viewModel.getRejectConfirmSensorPvRules()

    Column(modifier = Modifier.fillMaxSize()) {
        CalibrationHeader("Failsafe Tests - Reject Confirm Sensor")

        ScrollableWithScrollbar(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
        ) {
            Column {
                LabeledYesNoSegmentedSwitchAndTextInputWithHelp(
                    label = "Sensor fitted?",
                    currentState = fitted,
                    onStateChange = { newState ->
                        viewModel.setRejectConfirmSensorFitted(newState)
                        if (newState != YesNoState.YES) {
                            viewModel.setRejectConfirmSensorDetail("N/A")
                            viewModel.setRejectConfirmSensorTestMethod("N/A")
                            viewModel.setRejectConfirmSensorStopPosition("N/A")
                            viewModel.setRejectConfirmSensorLatched(YesNoState.NA)
                            viewModel.setRejectConfirmSensorCR(YesNoState.NA)
                        } else {
                            viewModel.setRejectConfirmSensorDetail("")
                            viewModel.setRejectConfirmSensorTestMethod("")
                            viewModel.setRejectConfirmSensorStopPosition("")
                            viewModel.setRejectConfirmSensorLatched(YesNoState.NO)
                            viewModel.setRejectConfirmSensorCR(YesNoState.NO)
                        }
                        viewModel.autoUpdateRejectConfirmSensorPvResult()
                    },
                    helpText = "Is a secondary sensor fitted to confirm the pack was correctly rejected?",
                    inputLabel = "Detail",
                    inputValue = detail,
                    onInputValueChange = {
                        viewModel.setRejectConfirmSensorDetail(it)
                        viewModel.autoUpdateRejectConfirmSensorPvResult()
                    },
                    pvStatus = if (pvRequired) {
                        if (fitted == YesNoState.YES) rules.getOrNull(0)?.status?.name else "N/A"
                    } else null,
                    pvRules = if (pvRequired) {
                        if (fitted == YesNoState.YES) listOfNotNull(rules.getOrNull(0)) else rules
                    } else emptyList()
                )

                FormSpacer()

                if (fitted == YesNoState.YES) {
                    LabeledDropdownWithHelp(
                        label = "Test Method",
                        options = testMethodOptions,
                        selectedOption = testMethod,
                        onSelectionChange = {
                            viewModel.setRejectConfirmSensorTestMethod(it)
                            viewModel.autoUpdateRejectConfirmSensorPvResult()
                        },
                        helpText = "Method used to trigger the reject confirmation fault.",
                        isNAToggleEnabled = false,
                        pvStatus = if (pvRequired) rules.getOrNull(1)?.status?.name else null,
                        pvRules = if (pvRequired) listOfNotNull(rules.getOrNull(1)) else emptyList()
                    )

                    FormSpacer()

                    LabeledMultiSelectDropdownWithHelp(
                        label = "Test Result",
                        value = testResult.joinToString(", "),
                        options = testResultOptions,
                        selectedOptions = testResult,
                        onSelectionChange = {
                            viewModel.setRejectConfirmSensorTestResult(it)
                            viewModel.autoUpdateRejectConfirmSensorPvResult()
                        },
                        helpText = "Observed outcome of the failsafe test.",
                        isNAToggleEnabled = false,
                        pvStatus = if (pvRequired) rules.getOrNull(2)?.status?.name else null,
                        pvRules = if (pvRequired) listOfNotNull(rules.getOrNull(2)) else emptyList()
                    )

                    FormSpacer()

                    LabeledTriStateSwitchWithHelp(
                        label = "Fault Latched?",
                        currentState = latched,
                        onStateChange = {
                            viewModel.setRejectConfirmSensorLatched(it)
                            viewModel.autoUpdateRejectConfirmSensorPvResult()
                        },
                        helpText = "Does the fault remain active until manually cleared?",
                        isNAToggleEnabled = false,
                        pvStatus = if (pvRequired) rules.getOrNull(3)?.status?.name else null,
                        pvRules = if (pvRequired) listOfNotNull(rules.getOrNull(3)) else emptyList()
                    )

                    FormSpacer()

                    LabeledTriStateSwitchWithHelp(
                        label = "Controlled Restart?",
                        currentState = controlledRestart,
                        onStateChange = {
                            viewModel.setRejectConfirmSensorCR(it)
                            viewModel.autoUpdateRejectConfirmSensorPvResult()
                        },
                        helpText = "Is a manual reset required to restart the system?",
                        isNAToggleEnabled = false,
                        pvStatus = if (pvRequired) rules.getOrNull(4)?.status?.name else null,
                        pvRules = if (pvRequired) listOfNotNull(rules.getOrNull(4)) else emptyList()
                    )

                    FormSpacer()

                    LabeledDropdownWithHelp(
                        label = "Pack Stop Position",
                        options = stopPositionOptions,
                        selectedOption = stopPosition,
                        onSelectionChange = {
                            viewModel.setRejectConfirmSensorStopPosition(it)
                            viewModel.autoUpdateRejectConfirmSensorPvResult()
                        },
                        helpText = "Where does the pack stop when this fault occurs?",
                        isNAToggleEnabled = false,
                        pvStatus = if (pvRequired) rules.getOrNull(5)?.status?.name else null,
                        pvRules = if (pvRequired) listOfNotNull(rules.getOrNull(5)) else emptyList()
                    )

                    FormSpacer()
                }

                if (pvRequired) {
                    LabeledFourOptionRadioWithHelp(
                        label = "P.V. Result",
                        value = viewModel.rejectConfirmSensorTestPvResult.value,
                        onValueChange = viewModel::setRejectConfirmSensorTestPvResult,
                        helpText = "Overall status for Reject Confirm failsafe validation."
                    )
                    FormSpacer()
                    PvSectionSummaryCard(title = "Reject confirm test P.V. Summary", rules = rules)
                    FormSpacer()
                }

                LabeledTextFieldWithHelp(
                    label = "Engineer Comments",
                    value = notes,
                    onValueChange = viewModel::setRejectConfirmSensorEngineerNotes,
                    helpText = "Optional notes for this section.",
                    isNAToggleEnabled = false,
                    maxLength = 50
                )

                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}
