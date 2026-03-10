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
import com.example.mecca.calibrationLogic.metalDetectorConveyor.autoUpdateAirPressureSensorPvResult
import com.example.mecca.calibrationLogic.metalDetectorConveyor.getAirPressureSensorPvRules
import com.example.mecca.calibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import com.example.mecca.formModules.*
import com.example.mecca.ui.theme.FormSpacer
import com.example.mecca.ui.theme.ScrollableWithScrollbar

@Composable
fun CalMetalDetectorConveyorAirPressureSensor(
    viewModel: CalibrationMetalDetectorConveyorViewModel
) {
    val fitted by viewModel.airPressureSensorFitted
    val detail by viewModel.airPressureSensorDetail
    val testMethod by viewModel.airPressureSensorTestMethod
    val testMethodOther by viewModel.airPressureSensorTestMethodOther
    val testResult by viewModel.airPressureSensorTestResult.collectAsState()
    val notes by viewModel.airPressureSensorEngineerNotes
    val latched by viewModel.airPressureSensorLatched
    val controlledRestart by viewModel.airPressureSensorCR

    val pvRequired = viewModel.pvRequired.value

    // Options
    val testMethodOptions = remember {
        listOf("Dump Valve", "Air Disconnection", "Other")
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

    // Next enabled
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

    val rules = viewModel.getAirPressureSensorPvRules()

    Column(modifier = Modifier.fillMaxSize()) {
        CalibrationHeader("Failsafe Tests - Air Pressure Sensor")

        ScrollableWithScrollbar(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
        ) {
            Column {
                LabeledYesNoSegmentedSwitchAndTextInputWithHelp(
                    label = "Air pressure sensor fitted?",
                    currentState = fitted,
                    onStateChange = { newState ->
                        viewModel.setAirPressureSensorFitted(newState)
                        if (newState != YesNoState.YES) {
                            viewModel.setAirPressureSensorDetail("N/A")
                            viewModel.setAirPressureSensorTestMethod("N/A")
                            viewModel.setAirPressureSensorTestMethodOther("N/A")
                            viewModel.setAirPressureSensorTestResult(emptyList())
                            viewModel.setAirPressureSensorLatched(YesNoState.NA)
                            viewModel.setAirPressureSensorCR(YesNoState.NA)
                        } else {
                            viewModel.setAirPressureSensorDetail("")
                            viewModel.setAirPressureSensorTestMethod("")
                            viewModel.setAirPressureSensorTestMethodOther("")
                            viewModel.setAirPressureSensorTestResult(emptyList())
                            viewModel.setAirPressureSensorLatched(YesNoState.NO)
                            viewModel.setAirPressureSensorCR(YesNoState.NO)
                        }
                        viewModel.autoUpdateAirPressureSensorPvResult()
                    },
                    helpText = "Select if an air pressure monitoring device is fitted to the reject air supply.",
                    inputLabel = "Detail",
                    inputValue = detail,
                    onInputValueChange = {
                        viewModel.setAirPressureSensorDetail(it)
                        viewModel.autoUpdateAirPressureSensorPvResult()
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
                            viewModel.setAirPressureSensorTestMethod(it)
                            viewModel.autoUpdateAirPressureSensorPvResult()
                        },
                        helpText = "Select the method used to trigger the air fault.",
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
                                viewModel.setAirPressureSensorTestMethodOther(it)
                                viewModel.autoUpdateAirPressureSensorPvResult()
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
                            viewModel.setAirPressureSensorTestResult(it)
                            viewModel.autoUpdateAirPressureSensorPvResult()
                        },
                        helpText = "Observed outcome of the air pressure failsafe test.",
                        isNAToggleEnabled = false,
                        pvStatus = if (pvRequired) rules.getOrNull(2)?.status?.name else null,
                        pvRules = if (pvRequired) listOfNotNull(rules.getOrNull(2)) else emptyList()
                    )

                    FormSpacer()

                    LabeledTriStateSwitchWithHelp(
                        label = "Fault Latched?",
                        currentState = latched,
                        onStateChange = {
                            viewModel.setAirPressureSensorLatched(it)
                            viewModel.autoUpdateAirPressureSensorPvResult()
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
                            viewModel.setAirPressureSensorCR(it)
                            viewModel.autoUpdateAirPressureSensorPvResult()
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
                        value = viewModel.airPressureSensorTestPvResult.value,
                        onValueChange = viewModel::setAirPressureSensorTestPvResult,
                        helpText = "Overall status for Air Pressure sensor failsafe validation."
                    )
                    FormSpacer()
                    PvSectionSummaryCard(title = "Air pressure test P.V. Summary", rules = rules)
                    FormSpacer()
                }

                LabeledTextFieldWithHelp(
                    label = "Engineer Comments",
                    value = notes,
                    onValueChange = viewModel::setAirPressureSensorEngineerNotes,
                    helpText = "Optional notes for this section.",
                    isNAToggleEnabled = false,
                    maxLength = 50
                )

                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}
