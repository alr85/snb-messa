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
import com.example.mecca.calibrationLogic.metalDetectorConveyor.autoUpdateBinFullSensorPvResult
import com.example.mecca.calibrationLogic.metalDetectorConveyor.getBinFullSensorPvRules
import com.example.mecca.calibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import com.example.mecca.formModules.*
import com.example.mecca.ui.theme.FormSpacer
import com.example.mecca.ui.theme.ScrollableWithScrollbar

@Composable
fun CalMetalDetectorConveyorBinFullPEC(
    viewModel: CalibrationMetalDetectorConveyorViewModel
) {
    val fitted by viewModel.binFullSensorFitted
    val detail by viewModel.binFullSensorDetail
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

    // PV Rules Calculation
    val rules = viewModel.getBinFullSensorPvRules()

    Column(modifier = Modifier.fillMaxSize()) {

        CalibrationHeader("Failsafe Tests - Bin Full Sensor")

        ScrollableWithScrollbar(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
        ) {
            Column {

                LabeledYesNoSegmentedSwitchAndTextInputWithHelp(
                    label = "Sensor fitted?",
                    currentState = fitted,
                    onStateChange = { newState ->
                        viewModel.setBinFullSensorFitted(newState)
                        if (newState == YesNoState.NO || newState == YesNoState.NA) {
                            viewModel.setBinFullSensorDetail("N/A")
                            viewModel.setBinFullSensorTestMethod("N/A")
                            viewModel.setBinFullSensorTestMethodOther("N/A")
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
                    inputLabel = "Detail",
                    inputValue = detail,
                    onInputValueChange = {
                        viewModel.setBinFullSensorDetail(it)
                        viewModel.autoUpdateBinFullSensorPvResult()
                    },
                    inputMaxLength = 12,
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
                            viewModel.setBinFullSensorTestMethod(it)
                            viewModel.autoUpdateBinFullSensorPvResult()
                        },
                        helpText = "Select the method used to trigger the sensor.",
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
                        pvStatus = if (pvRequired) rules.getOrNull(2)?.status?.name else null,
                        pvRules = if (pvRequired) listOfNotNull(rules.getOrNull(2)) else emptyList()
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
                        pvStatus = if (pvRequired) rules.getOrNull(3)?.status?.name else null,
                        pvRules = if (pvRequired) listOfNotNull(rules.getOrNull(3)) else emptyList()
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
                        pvStatus = if (pvRequired) rules.getOrNull(4)?.status?.name else null,
                        pvRules = if (pvRequired) listOfNotNull(rules.getOrNull(4)) else emptyList()
                    )

                    FormSpacer()
                }

                if (pvRequired) {
                    LabeledFourOptionRadioWithHelp(
                        label = "P.V. Result",
                        value = viewModel.binFullSensorTestPvResult.value,
                        onValueChange = viewModel::setBinFullSensorTestPvResult,
                        helpText = "This section is automatically evaluated. You can manually override if necessary."
                    )
                    FormSpacer()
                    PvSectionSummaryCard(title = "Bin full test P.V. Summary", rules = rules)
                    FormSpacer()
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
