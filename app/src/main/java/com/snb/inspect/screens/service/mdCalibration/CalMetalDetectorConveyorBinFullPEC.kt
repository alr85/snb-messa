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
import com.snb.inspect.calibrationLogic.metalDetectorConveyor.autoUpdateBinFullSensorPvResult
import com.snb.inspect.calibrationLogic.metalDetectorConveyor.getBinFullSensorPvRules
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
fun CalMetalDetectorConveyorBinFullPEC(
    viewModel: CalibrationMetalDetectorConveyorViewModel
) {
    val fitted by viewModel.binFullSensorFitted
    val testMethod by viewModel.binFullSensorTestMethod
    val testMethodOther by viewModel.binFullSensorTestMethodOther
    val testResult by viewModel.binFullSensorTestResult.collectAsState()
    val notes by viewModel.binFullSensorEngineerNotes
    val latched by viewModel.binFullSensorLatched
    val controlledRestart by viewModel.binFullSensorCR
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

    // Options
    val testMethodOptions = remember {
        listOf("Manual Block", "Device Block", "Other")
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
                    "On-Screen Notification",
                    "System Belt Stops",
                    "In-feed Belt Stops",
                    "Out-feed Belt Stops",
                    "Other"
                )
            )
        }
    }

    // Validation for Next button
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

    // PV Rules Calculation
    val rules = remember(
        fitted,
        testMethod,
        testMethodOther,
        testResult,
        latched,
        controlledRestart
    ) {
        viewModel.getBinFullSensorPvRules()
    }

    Column(modifier = Modifier.fillMaxSize()) {

        CalibrationHeader("Failsafe Tests - Bin Full Sensor")

        ScrollableWithScrollbar(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
        ) {
            Column {

                LabeledYesNoNaSegmentedSwitchWithHelp(
                    label = "Sensor fitted?",
                    currentState = fitted,
                    onStateChange = { newState ->
                        viewModel.setBinFullSensorFitted(newState)
                        if (newState == YesNoState.NO || newState == YesNoState.NA) {
                            viewModel.setBinFullSensorDetail("N/A")
                            viewModel.setBinFullSensorTestMethod("N/A")
                            viewModel.setBinFullSensorTestMethodOther("")
                            viewModel.setBinFullSensorTestResult(emptyList())
                            viewModel.setBinFullSensorLatched(YesNoState.NA)
                            viewModel.setBinFullSensorCR(YesNoState.NA)
                            viewModel.setOperatorTestWitnessedBinFull(YesNoState.NA)
                        } else if (newState == YesNoState.YES) {
                            viewModel.setBinFullSensorDetail("")
                            viewModel.setBinFullSensorTestMethod("")
                            viewModel.setBinFullSensorTestMethodOther("")
                            viewModel.setBinFullSensorTestResult(emptyList())
                            viewModel.setBinFullSensorLatched(YesNoState.NO)
                            viewModel.setBinFullSensorCR(YesNoState.NO)
                        }
                        viewModel.autoUpdateBinFullSensorPvResult()
                    },
                    helpText = "Select if there is a bin full sensor fitted.",
                    onInputValueChange = {
                    },
                    pvStatus = if (pvRequired) rules.find { it.ruleId == "BIN_FULL_FITTED" }?.status?.name else null,
                    pvRules = rules.filter { it.ruleId == "BIN_FULL_FITTED" }
                )

                FormSpacer()

                if (fitted == YesNoState.YES) {

                    LabeledDropdownWithHelp(
                        label = "Test Method",
                        options = testMethodOptions,
                        selectedOption = testMethod,
                        onSelectionChange = {
                            if(it != "Other") viewModel.setBinFullSensorTestMethodOther("")
                            viewModel.setBinFullSensorTestMethod(it)
                            viewModel.autoUpdateBinFullSensorPvResult()
                        },
                        helpText = "Select the method used to trigger the bin full fault.",
                        isNAToggleEnabled = false,
                        pvStatus = if (pvRequired) rules.find { it.ruleId == "BIN_FULL_METHOD" }?.status?.name else null,
                        pvRules = rules.filter { it.ruleId == "BIN_FULL_METHOD" }
                    )

                    FormSpacer()

                    if (testMethod == "Other") {
                        LabeledTextFieldWithHelp(
                            label = "Other Test Method",
                            value = testMethodOther,
                            onValueChange = {
                                viewModel.setBinFullSensorTestMethodOther(it)
                                viewModel.autoUpdateBinFullSensorPvResult()
                            },
                            helpText = "Enter the custom test method.",
                            isNAToggleEnabled = false,
                            maxLength = 25
                        )
                        FormSpacer()
                    }

                    LabeledMultiSelectDropdownWithHelp(
                        label = "Test Result",
                        value = testResult.joinToString(", "),
                        options = testResultOptions,
                        selectedOptions = testResult,
                        onSelectionChange = { newSelection ->
                            val cleaned = if ("No Result" in newSelection) listOf("No Result") 
                                          else newSelection.filterNot { it == "No Result" }
                            
                            viewModel.setBinFullSensorTestResult(cleaned)
                            viewModel.autoUpdateBinFullSensorPvResult()
                        },
                        helpText = "Select the outcome of the sensor test.",
                        isNAToggleEnabled = false,
                        pvStatus = if (pvRequired) rules.find { it.ruleId == "BIN_FULL_RESULT" }?.status?.name else null,
                        pvRules = rules.filter { it.ruleId == "BIN_FULL_RESULT" }
                    )

                    FormSpacer()

                    LabeledTriStateSwitchWithHelp(
                        label = "Fault Latched?",
                        currentState = latched,
                        onStateChange = {
                            viewModel.setBinFullSensorLatched(it)
                            viewModel.autoUpdateBinFullSensorPvResult()
                        },
                        helpText = "Is the fault output latched, or does it clear automatically?",
                        isNAToggleEnabled = false,
                        pvStatus = if (pvRequired) rules.find { it.ruleId == "BIN_FULL_LATCHED" }?.status?.name else null,
                        pvRules = rules.filter { it.ruleId == "BIN_FULL_LATCHED" }
                    )

                    FormSpacer()

                    LabeledTriStateSwitchWithHelp(
                        label = "Controlled Restart?",
                        currentState = controlledRestart,
                        onStateChange = {
                            viewModel.setBinFullSensorCR(it)
                            viewModel.autoUpdateBinFullSensorPvResult()
                        },
                        helpText = "Is a controlled restart required after a fault?",
                        isNAToggleEnabled = false,
                        pvStatus = if (pvRequired) rules.find { it.ruleId == "BIN_FULL_CR" }?.status?.name else null,
                        pvRules = rules.filter { it.ruleId == "BIN_FULL_CR" }
                    )

                    if(!pvRequired) FormSpacer()
                }

                if (pvRequired) {

                    PvSectionSummaryCard(
                        title = "Bin full test P.V. Summary",
                        rules = rules
                    )

                }

                LabeledTextFieldWithHelp(
                    label = "Engineer Comments",
                    value = notes,
                    onValueChange = viewModel::setBinFullSensorEngineerNotes,
                    helpText = "Enter any notes relevant to this section.",
                    isNAToggleEnabled = false,
                    maxLength = 50
                )

                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}
