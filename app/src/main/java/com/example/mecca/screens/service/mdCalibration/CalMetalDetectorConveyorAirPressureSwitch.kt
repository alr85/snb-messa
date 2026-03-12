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
import androidx.compose.ui.unit.dp
import com.example.mecca.calibrationLogic.metalDetectorConveyor.autoUpdateAirPressureSensorPvResult
import com.example.mecca.calibrationLogic.metalDetectorConveyor.getAirPressureSensorPvRules
import com.example.mecca.calibrationViewModels.CalibrationMetalDetectorConveyorViewModel
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
fun CalMetalDetectorConveyorAirPressureSensor(
    viewModel: CalibrationMetalDetectorConveyorViewModel
) {
    val fitted by viewModel.airPressureSensorFitted
    val testMethod by viewModel.airPressureSensorTestMethod
    val testMethodOther by viewModel.airPressureSensorTestMethodOther
    val testResult by viewModel.airPressureSensorTestResult.collectAsState()
    val notes by viewModel.airPressureSensorEngineerNotes
    val latched by viewModel.airPressureSensorLatched
    val controlledRestart by viewModel.airPressureSensorCR

    val pvRequired = viewModel.pvRequired.value

    val testMethodOptions = remember {
        listOf("Dump Valve", "Air Disconnection", "Other")
    }

    val testResultOptions = remember {
        listOf(
            "No Result",
            "Audible Notification",
            "Visual Notification",
            "On-Screen Notification",
            "System Belt Stops",
            "In-feed Belt Stops",
            "Out-feed Belt Stops",
            "Other"
        )
    }

    val isNextStepEnabled = when (fitted) {
        YesNoState.NO, YesNoState.NA -> true
        YesNoState.YES -> {
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

    val rules = remember(
        fitted,
        testMethod,
        testMethodOther,
        testResult,
        latched,
        controlledRestart
    ) {
        viewModel.getAirPressureSensorPvRules()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        CalibrationHeader("Failsafe Tests - Air Pressure Sensor")

        ScrollableWithScrollbar(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
        ) {
            Column {
                LabeledYesNoNaSegmentedSwitchWithHelp(
                    label = "Air pressure sensor fitted?",
                    currentState = fitted,
                    onStateChange = { newState ->
                        viewModel.setAirPressureSensorFitted(newState)
                        if (newState == YesNoState.NO || newState == YesNoState.NA) {
                            viewModel.setAirPressureSensorDetail("")
                            viewModel.setAirPressureSensorTestMethod("N/A")
                            viewModel.setAirPressureSensorTestMethodOther("")
                            viewModel.setAirPressureSensorTestResult(emptyList())
                            viewModel.setAirPressureSensorLatched(YesNoState.NA)
                            viewModel.setAirPressureSensorCR(YesNoState.NA)
                        } else if (newState == YesNoState.YES) {
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
                    onInputValueChange = {
                    },
                    pvStatus = if (pvRequired) rules.find { it.ruleId == "AIR_FITTED" }?.status?.name else null,
                    pvRules = if (pvRequired) rules.filter { it.ruleId == "AIR_FITTED" } else emptyList()
                )

                FormSpacer()

                if (fitted == YesNoState.YES) {
                    LabeledDropdownWithHelp(
                        label = "Test Method",
                        options = testMethodOptions,
                        selectedOption = testMethod,
                        onSelectionChange = {
                            if (it != "Other") viewModel.setAirPressureSensorTestMethodOther("")
                            viewModel.setAirPressureSensorTestMethod(it)
                            viewModel.autoUpdateAirPressureSensorPvResult()
                        },
                        helpText = "Select the method used to trigger the air fault.",
                        isNAToggleEnabled = false,
                        pvStatus = if (pvRequired) rules.find { it.ruleId == "AIR_METHOD" }?.status?.name else null,
                        pvRules = if (pvRequired) rules.filter { it.ruleId == "AIR_METHOD" } else emptyList()
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
                        onSelectionChange = { newSelection ->
                            val cleaned = if ("No Result" in newSelection) {
                                listOf("No Result")
                            } else {
                                newSelection.filterNot { it == "No Result" }
                            }

                            viewModel.setAirPressureSensorTestResult(cleaned)
                            viewModel.autoUpdateAirPressureSensorPvResult()
                        },
                        helpText = "Observed outcome of the air pressure failsafe test.",
                        isNAToggleEnabled = false,
                        pvStatus = if (pvRequired) rules.find { it.ruleId == "AIR_RESULT" }?.status?.name else null,
                        pvRules = if (pvRequired) rules.filter { it.ruleId == "AIR_RESULT" } else emptyList()
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
                        pvStatus = if (pvRequired) rules.find { it.ruleId == "AIR_LATCHED" }?.status?.name else null,
                        pvRules = if (pvRequired) rules.filter { it.ruleId == "AIR_LATCHED" } else emptyList()
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
                        pvStatus = if (pvRequired) rules.find { it.ruleId == "AIR_CR" }?.status?.name else null,
                        pvRules = if (pvRequired) rules.filter { it.ruleId == "AIR_CR" } else emptyList()
                    )

                    if (!pvRequired) FormSpacer()
                }

                if (pvRequired) {
                    PvSectionSummaryCard(
                        title = "Air pressure test P.V. Summary",
                        rules = rules
                    )
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