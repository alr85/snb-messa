package com.snb.inspect.screens.service.mdCalibration

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
import com.snb.inspect.calibrationLogic.metalDetectorConveyor.autoUpdatePackCheckSensorPvResult
import com.snb.inspect.calibrationLogic.metalDetectorConveyor.getPackCheckSensorPvRules
import com.snb.inspect.calibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import com.snb.inspect.formModules.CalibrationHeader
import com.snb.inspect.formModules.LabeledDropdownWithHelp
import com.snb.inspect.formModules.LabeledMultiSelectDropdownWithHelp
import com.snb.inspect.formModules.LabeledTextFieldWithHelp
import com.snb.inspect.formModules.LabeledTriStateSwitchWithHelp
import com.snb.inspect.formModules.LabeledYesNoNaSegmentedSwitchWithHelp
import com.snb.inspect.formModules.PvSectionSummaryCard
import com.snb.inspect.formModules.YesNoState
import com.snb.inspect.ui.theme.FormSpacer
import com.snb.inspect.ui.theme.ScrollableWithScrollbar

@Composable
fun CalMetalDetectorConveyorPackCheckSensor(
    viewModel: CalibrationMetalDetectorConveyorViewModel
) {
    val fitted by viewModel.packCheckSensorFitted
    val testMethod by viewModel.packCheckSensorTestMethod
    val testMethodOther by viewModel.packCheckSensorTestMethodOther
    val testResult by viewModel.packCheckSensorTestResult.collectAsState()
    val notes by viewModel.packCheckSensorEngineerNotes
    val latched by viewModel.packCheckSensorLatched
    val controlledRestart by viewModel.packCheckSensorCR
    val ind1label by viewModel.indicator1label
    val ind1colour by viewModel.indicator1colour
    val ind2label by viewModel.indicator2label
    val ind2colour by viewModel.indicator2colour
    val ind3label by viewModel.indicator3label
    val ind3colour by viewModel.indicator3colour
    val ind4label by viewModel.indicator4label
    val ind4colour by viewModel.indicator4colour
    val ind5label by viewModel.indicator5label
    val ind5colour by viewModel.indicator5colour
    val ind6label by viewModel.indicator6label
    val ind6colour by viewModel.indicator6colour

    val pvRequired = viewModel.pvRequired.value

    val testMethodOptions = remember {
        listOf("Timed Internal Test", "Product Block", "Manual Block", "Other")
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
                    "Audible Notification",
                    "Visual Notification",
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
        viewModel.getPackCheckSensorPvRules()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        CalibrationHeader("Failsafe Tests - Pack Check Sensor")

        ScrollableWithScrollbar(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
        ) {
            Column {
                LabeledYesNoNaSegmentedSwitchWithHelp(
                    label = "Pack check sensor fitted?",
                    currentState = fitted,
                    onStateChange = { newState ->
                        viewModel.setPackCheckSensorFitted(newState)
                        if (newState == YesNoState.NO || newState == YesNoState.NA) {
                            viewModel.setPackCheckSensorDetail("")
                            viewModel.setPackCheckSensorTestMethod("N/A")
                            viewModel.setPackCheckSensorTestMethodOther("")
                            viewModel.setPackCheckSensorTestResult(emptyList())
                            viewModel.setPackCheckSensorLatched(YesNoState.NA)
                            viewModel.setPackCheckSensorCR(YesNoState.NA)
                        } else if (newState == YesNoState.YES) {
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
                    onInputValueChange = {
                    },
                    pvStatus = if (pvRequired) rules.find { it.ruleId == "PACK_FITTED" }?.status?.name else null,
                    pvRules = if (pvRequired) rules.filter { it.ruleId == "PACK_FITTED" } else emptyList()
                )

                FormSpacer()

                if (fitted == YesNoState.YES) {
                    LabeledDropdownWithHelp(
                        label = "Test Method",
                        options = testMethodOptions,
                        selectedOption = testMethod,
                        onSelectionChange = {
                            if (it != "Other") viewModel.setPackCheckSensorTestMethodOther("")
                            viewModel.setPackCheckSensorTestMethod(it)
                            viewModel.autoUpdatePackCheckSensorPvResult()
                        },
                        helpText = "Select the method used to test the pack check sensor.",
                        isNAToggleEnabled = false,
                        pvStatus = if (pvRequired) rules.find { it.ruleId == "PACK_METHOD" }?.status?.name else null,
                        pvRules = if (pvRequired) rules.filter { it.ruleId == "PACK_METHOD" } else emptyList()
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
                        onSelectionChange = { newSelection ->
                            val cleaned = if ("No Result" in newSelection) {
                                listOf("No Result")
                            } else {
                                newSelection.filterNot { it == "No Result" }
                            }

                            viewModel.setPackCheckSensorTestResult(cleaned)
                            viewModel.autoUpdatePackCheckSensorPvResult()
                        },
                        helpText = "Select the observed failsafe action.",
                        isNAToggleEnabled = false,
                        pvStatus = if (pvRequired) rules.find { it.ruleId == "PACK_RESULT" }?.status?.name else null,
                        pvRules = if (pvRequired) rules.filter { it.ruleId == "PACK_RESULT" } else emptyList()
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
                        pvStatus = if (pvRequired) rules.find { it.ruleId == "PACK_LATCHED" }?.status?.name else null,
                        pvRules = if (pvRequired) rules.filter { it.ruleId == "PACK_LATCHED" } else emptyList()
                    )

                    FormSpacer()

                    LabeledTriStateSwitchWithHelp(
                        label = "Controlled Restart?",
                        currentState = controlledRestart,
                        onStateChange = {
                            viewModel.setPackCheckSensorCR(it)
                            viewModel.autoUpdatePackCheckSensorPvResult()
                        },
                        helpText = "Is a manual reset required to restart the system?",
                        isNAToggleEnabled = false,
                        pvStatus = if (pvRequired) rules.find { it.ruleId == "PACK_CR" }?.status?.name else null,
                        pvRules = if (pvRequired) rules.filter { it.ruleId == "PACK_CR" } else emptyList()
                    )

                    if (!pvRequired) FormSpacer()
                }

                if (pvRequired) {
                    PvSectionSummaryCard(
                        title = "Pack check test P.V. Summary",
                        rules = rules
                    )
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
