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
import com.example.mecca.calibrationLogic.metalDetectorConveyor.autoUpdateInfeedSensorPvResult
import com.example.mecca.calibrationLogic.metalDetectorConveyor.getInfeedSensorPvRules
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
fun CalMetalDetectorConveyorInfeedPEC(
    viewModel: CalibrationMetalDetectorConveyorViewModel
) {

    val fitted by viewModel.infeedSensorFitted
    val testMethod by viewModel.infeedSensorTestMethod
    val testMethodOther by viewModel.infeedSensorTestMethodOther
    val testResult by viewModel.infeedSensorTestResult.collectAsState()
    val notes by viewModel.infeedSensorEngineerNotes
    val latched by viewModel.infeedSensorLatched
    val controlledRestart by viewModel.infeedSensorCR

    val pvRequired = viewModel.pvRequired.value

    // Options
    val testMethodOptions = remember {
        listOf("Manual Block", "Device Block", "Other")
    }

    val testResultOptions = remember {
        listOf("No Result", "Audible Notification", "Visual Notification", "On-Screen Notification", "System Belt Stops", "In-feed Belt Stops", "Out-feed Belt Stops")
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
    val rules = viewModel.getInfeedSensorPvRules()

    Column(modifier = Modifier.fillMaxSize()) {

        CalibrationHeader("Failsafe Tests - Photogating/Infeed Sensor")

        ScrollableWithScrollbar(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
        ) {
            Column {

                LabeledYesNoNaSegmentedSwitchWithHelp(
                    label = "Sensor fitted?",
                    currentState = fitted,
                    onStateChange = { newState ->
                        viewModel.setInfeedSensorFitted(newState)
                        if (newState == YesNoState.NA || newState == YesNoState.NO) {
                            viewModel.setInfeedSensorDetail("N/A")
                            viewModel.setInfeedSensorTestMethod("N/A")
                            viewModel.setInfeedSensorTestMethodOther("")
                            viewModel.setInfeedSensorTestResult(emptyList())
                            viewModel.setInfeedSensorLatched(YesNoState.NA)
                            viewModel.setInfeedSensorCR(YesNoState.NA)
                        } else if (newState == YesNoState.YES) {
                            viewModel.setInfeedSensorDetail("")
                            viewModel.setInfeedSensorTestMethod("")
                            viewModel.setInfeedSensorTestMethodOther("")
                            viewModel.setInfeedSensorTestResult(emptyList())
                            viewModel.setInfeedSensorLatched(YesNoState.NO)
                            viewModel.setInfeedSensorCR(YesNoState.NO)
                        }
                        viewModel.autoUpdateInfeedSensorPvResult()
                    },
                    helpText = "Select if there is an in-feed/gated timer sensor fitted.",
                    onInputValueChange = {

                    },
                    pvStatus = if (pvRequired) rules.find { it.ruleId == "INFEED_FITTED" }?.status?.name else null,
                    pvRules = rules.filter { it.ruleId == "INFEED_FITTED" }
                )

                FormSpacer()

                if (fitted == YesNoState.YES) {
                    LabeledDropdownWithHelp(
                        label = "Test Method",
                        options = testMethodOptions,
                        selectedOption = testMethod,
                        onSelectionChange = {
                            if(it != "Other") viewModel.setInfeedSensorTestMethodOther("")
                            viewModel.setInfeedSensorTestMethod(it)
                            viewModel.autoUpdateInfeedSensorPvResult()
                        },
                        helpText = "Select the method used to trigger the sensor.",
                        isNAToggleEnabled = false,
                        pvStatus = if (pvRequired) rules.find { it.ruleId == "INFEED_METHOD" }?.status?.name else null,
                        pvRules = rules.filter { it.ruleId == "INFEED_METHOD" }
                    )

                    FormSpacer()

                    if (testMethod == "Other") {
                        LabeledTextFieldWithHelp(
                            label = "Other Test Method",
                            value = testMethodOther,
                            onValueChange = {
                                viewModel.setInfeedSensorTestMethodOther(it)
                                viewModel.autoUpdateInfeedSensorPvResult()
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
                            
                            viewModel.setInfeedSensorTestResult(cleaned)
                            viewModel.autoUpdateInfeedSensorPvResult()
                        },
                        helpText = "Select the outcome of the sensor test.",
                        isNAToggleEnabled = false,
                        pvStatus = if (pvRequired) rules.find { it.ruleId == "INFEED_RESULT" }?.status?.name else null,
                        pvRules = rules.filter { it.ruleId == "INFEED_RESULT" }
                    )

                    FormSpacer()

                    LabeledTriStateSwitchWithHelp(
                        label = "Fault Latched?",
                        currentState = latched,
                        onStateChange = {
                            viewModel.setInfeedSensorLatched(it)
                            viewModel.autoUpdateInfeedSensorPvResult()
                        },
                        helpText = "Is the fault output latched, or does it clear automatically?",
                        isNAToggleEnabled = false,
                        pvStatus = if (pvRequired) rules.find { it.ruleId == "INFEED_LATCHED" }?.status?.name else null,
                        pvRules = rules.filter { it.ruleId == "INFEED_LATCHED" }
                    )

                    FormSpacer()

                    LabeledTriStateSwitchWithHelp(
                        label = "Controlled Restart?",
                        currentState = controlledRestart,
                        onStateChange = {
                            viewModel.setInfeedSensorCR(it)
                            viewModel.autoUpdateInfeedSensorPvResult()
                        },
                        helpText = "Is a manual reset required to restart the system?",
                        isNAToggleEnabled = false,
                        pvStatus = if (pvRequired) rules.find { it.ruleId == "INFEED_CR" }?.status?.name else null,
                        pvRules = rules.filter { it.ruleId == "INFEED_CR" }
                    )

                    if(!pvRequired) FormSpacer()
                }

                if (pvRequired) {
                    PvSectionSummaryCard(
                        title = "Infeed sensor test P.V. Summary",
                        rules = rules
                    )

                }

                LabeledTextFieldWithHelp(
                    label = "Engineer Comments",
                    value = notes,
                    onValueChange = viewModel::setInfeedSensorEngineerNotes,
                    helpText = "Enter any notes relevant to this section.",
                    isNAToggleEnabled = false,
                    maxLength = 50
                )

                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}
