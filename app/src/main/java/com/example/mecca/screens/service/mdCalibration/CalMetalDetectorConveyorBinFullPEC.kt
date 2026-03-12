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
import com.example.mecca.calibrationLogic.metalDetectorConveyor.autoUpdateBinFullSensorPvResult
import com.example.mecca.calibrationLogic.metalDetectorConveyor.getBinFullSensorPvRules
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

    val pvRequired = viewModel.pvRequired.value

    // Options
    val testMethodOptions = remember {
        listOf("Manual Block", "Device Block", "Other")
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
