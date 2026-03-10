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
import com.example.mecca.calibrationLogic.metalDetectorConveyor.autoUpdatePackCheckSensorPvResult
import com.example.mecca.calibrationLogic.metalDetectorConveyor.getPackCheckSensorPvRules
import com.example.mecca.calibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import com.example.mecca.formModules.*
import com.example.mecca.ui.theme.FormSpacer
import com.example.mecca.ui.theme.ScrollableWithScrollbar

@Composable
fun CalMetalDetectorConveyorPackCheckSensor(
    viewModel: CalibrationMetalDetectorConveyorViewModel
) {
    val fitted by viewModel.packCheckSensorFitted
    val detail by viewModel.packCheckSensorDetail
    val testMethod by viewModel.packCheckSensorTestMethod
    val testMethodOther by viewModel.packCheckSensorTestMethodOther
    val testResult by viewModel.packCheckSensorTestResult.collectAsState()
    val notes by viewModel.packCheckSensorEngineerNotes
    val latched by viewModel.packCheckSensorLatched
    val controlledRestart by viewModel.packCheckSensorCR

    val pvRequired = viewModel.pvRequired.value

    // Options
    val testMethodOptions = remember {
        listOf("Timed Internal Test", "Product Block", "Manual Block", "Other")
    }
    val testResultOptions = remember {
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

    // Validation for Next button
    val isNextStepEnabled = when (fitted) {
        YesNoState.NO, YesNoState.NA -> true
        YesNoState.YES -> {
            detail.isNotBlank() &&
                    testMethod.isNotBlank() &&
                    testResult.isNotEmpty() &&
                    latched != YesNoState.NA &&
                    controlledRestart != YesNoState.NA &&
                    (testMethod != "Other" || testMethodOther.isNotBlank())
        }
        else -> false
    }

    LaunchedEffect(isNextStepEnabled) {
        viewModel.setCurrentScreenNextEnabled(isNextStepEnabled)
    }

    val rules = viewModel.getPackCheckSensorPvRules()

    Column(modifier = Modifier.fillMaxSize()) {
        CalibrationHeader("Failsafe Tests - Pack Check Sensor")

        ScrollableWithScrollbar(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
        ) {
            Column {
                LabeledYesNoSegmentedSwitchAndTextInputWithHelp(
                    label = "Pack check sensor fitted?",
                    currentState = fitted,
                    onStateChange = { newState ->
                        viewModel.setPackCheckSensorFitted(newState)
                        if (newState != YesNoState.YES) {
                            viewModel.setPackCheckSensorDetail("N/A")
                            viewModel.setPackCheckSensorTestMethod("N/A")
                            viewModel.setPackCheckSensorTestMethodOther("N/A")
                            viewModel.setPackCheckSensorTestResult(emptyList())
                            viewModel.setPackCheckSensorLatched(YesNoState.NA)
                            viewModel.setPackCheckSensorCR(YesNoState.NA)
                        } else {
                            viewModel.setPackCheckSensorDetail("")
                            viewModel.setPackCheckSensorTestMethod("")
                            viewModel.setPackCheckSensorTestMethodOther("")
                            viewModel.setPackCheckSensorTestResult(emptyList())
                            viewModel.setPackCheckSensorLatched(YesNoState.NO)
                            viewModel.setPackCheckSensorCR(YesNoState.NO)
                        }
                        viewModel.autoUpdatePackCheckSensorPvResult()
                    },
                    helpText = "Is a pack check sensor fitted to the system?",
                    inputLabel = "Detail",
                    inputValue = detail,
                    onInputValueChange = {
                        viewModel.setPackCheckSensorDetail(it)
                        viewModel.autoUpdatePackCheckSensorPvResult()
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
                        label = "Test Method",
                        options = testMethodOptions,
                        selectedOption = testMethod,
                        onSelectionChange = {
                            viewModel.setPackCheckSensorTestMethod(it)
                            viewModel.autoUpdatePackCheckSensorPvResult()
                        },
                        helpText = "Select the method used to test the pack check sensor.",
                        isNAToggleEnabled = false,
                        pvStatus = if (pvRequired) rules.getOrNull(1)?.status?.name else null,
                        pvRules = if (pvRequired) listOfNotNull(rules.getOrNull(1)) else emptyList()
                    )

                    FormSpacer()

                    if (testMethod == "Other") {
                        LabeledTextFieldWithHelp(
                            label = "Other Test Method",
                            value = testMethodOther,
                            onValueChange = {
                                viewModel.setPackCheckSensorTestMethodOther(it)
                                viewModel.autoUpdatePackCheckSensorPvResult()
                            },
                            helpText = "Enter the custom test method.",
                            isNAToggleEnabled = false,
                            maxLength = 12
                        )
                        FormSpacer()
                    }

                    LabeledMultiSelectDropdownWithHelp(
                        label = "Test Result",
                        value = testResult.joinToString(", "),
                        options = testResultOptions,
                        selectedOptions = testResult,
                        onSelectionChange = {
                            viewModel.setPackCheckSensorTestResult(it)
                            viewModel.autoUpdatePackCheckSensorPvResult()
                        },
                        helpText = "Select the observed failsafe action.",
                        isNAToggleEnabled = false,
                        pvStatus = if (pvRequired) rules.getOrNull(2)?.status?.name else null,
                        pvRules = if (pvRequired) listOfNotNull(rules.getOrNull(2)) else emptyList()
                    )

                    FormSpacer()

                    LabeledTriStateSwitchWithHelp(
                        label = "Fault Latched?",
                        currentState = latched,
                        onStateChange = {
                            viewModel.setPackCheckSensorLatched(it)
                            viewModel.autoUpdatePackCheckSensorPvResult()
                        },
                        helpText = "Does the fault remain active until manually cleared?",
                        isNAToggleEnabled = false,
                        pvStatus = if (pvRequired) rules.getOrNull(3)?.status?.name else null,
                        pvRules = if (pvRequired) listOfNotNull(rules.getOrNull(3)) else emptyList()
                    )

                    FormSpacer()

                    LabeledTriStateSwitchWithHelp(
                        label = "Fault Controlled Restart?",
                        currentState = controlledRestart,
                        onStateChange = {
                            viewModel.setPackCheckSensorCR(it)
                            viewModel.autoUpdatePackCheckSensorPvResult()
                        },
                        helpText = "Is a manual reset required to restart the system?",
                        isNAToggleEnabled = false,
                        pvStatus = if (pvRequired) rules.getOrNull(4)?.status?.name else null,
                        pvRules = if (pvRequired) listOfNotNull(rules.getOrNull(4)) else emptyList()
                    )

                    FormSpacer()
                }

                if (pvRequired) {
                    LabeledFourOptionRadioWithHelp(
                        label = "P.V. Result",
                        value = viewModel.packCheckSensorTestPvResult.value,
                        onValueChange = viewModel::setPackCheckSensorTestPvResult,
                        helpText = "Overall status for Pack Check sensor failsafe validation."
                    )
                    FormSpacer()
                    PvSectionSummaryCard(title = "Pack check test P.V. Summary", rules = rules)
                    FormSpacer()
                }

                LabeledTextFieldWithHelp(
                    label = "Engineer Comments",
                    value = notes,
                    onValueChange = viewModel::setPackCheckSensorEngineerNotes,
                    helpText = "Enter any notes relevant to this section.",
                    isNAToggleEnabled = false,
                    maxLength = 50
                )

                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}
