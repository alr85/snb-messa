package com.example.mecca.screens.service.mdCalibration

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mecca.calibrationLogic.metalDetectorConveyor.autoUpdateSpeedSensorPvResult
import com.example.mecca.calibrationLogic.metalDetectorConveyor.getSpeedSensorPvRules
import com.example.mecca.calibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import com.example.mecca.formModules.*
import com.example.mecca.ui.theme.FormSpacer
import com.example.mecca.ui.theme.ScrollableWithScrollbar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalMetalDetectorConveyorSpeedSensor(
    viewModel: CalibrationMetalDetectorConveyorViewModel
) {
    val fitted by viewModel.speedSensorFitted
    val detail by viewModel.speedSensorDetail
    val testMethod by viewModel.speedSensorTestMethod
    val testMethodOther by viewModel.speedSensorTestMethodOther
    val testResult by viewModel.speedSensorTestResult.collectAsState()
    val notes by viewModel.speedSensorEngineerNotes
    val latched by viewModel.speedSensorLatched
    val controlledRestart by viewModel.speedSensorCR

    val pvRequired = viewModel.pvRequired.value

    // Options
    val testMethodOptions = remember {
        listOf("Stop Belt After Detection", "Other")
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
            "Test Pack Rejects OK",
            "Other"
        )
    }

    // Next enabled logic
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

    val rules = viewModel.getSpeedSensorPvRules()

    Column(modifier = Modifier.fillMaxSize()) {
        CalibrationHeader("Failsafe Tests - Speed Sensor")

        ScrollableWithScrollbar(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
        ) {
            Column {
                LabeledYesNoSegmentedSwitchAndTextInputWithHelp(
                    label = "Sensor fitted?",
                    currentState = fitted,
                    onStateChange = { newState ->
                        viewModel.setSpeedSensorFitted(newState)
                        if (newState != YesNoState.YES) {
                            viewModel.setSpeedSensorDetail("N/A")
                            viewModel.setSpeedSensorTestMethod("N/A")
                            viewModel.setSpeedSensorTestMethodOther("N/A")
                            viewModel.setSpeedSensorTestResult(emptyList())
                            viewModel.setSpeedSensorLatched(YesNoState.NA)
                            viewModel.setSpeedSensorCR(YesNoState.NA)
                        } else {
                            viewModel.setSpeedSensorDetail("")
                            viewModel.setSpeedSensorTestMethod("")
                            viewModel.setSpeedSensorTestMethodOther("")
                            viewModel.setSpeedSensorTestResult(emptyList())
                            viewModel.setSpeedSensorLatched(YesNoState.NO)
                            viewModel.setSpeedSensorCR(YesNoState.NO)
                        }
                        viewModel.autoUpdateSpeedSensorPvResult()
                    },
                    helpText = "Select if a speed sensor is fitted and used for belt speed monitoring / failsafe operation.",
                    inputLabel = "Detail",
                    inputValue = detail,
                    onInputValueChange = {
                        viewModel.setSpeedSensorDetail(it)
                        viewModel.autoUpdateSpeedSensorPvResult()
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
                            viewModel.setSpeedSensorTestMethod(it)
                            viewModel.autoUpdateSpeedSensorPvResult()
                        },
                        helpText = "Method used to trigger the speed sensor fault.",
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
                                viewModel.setSpeedSensorTestMethodOther(it)
                                viewModel.autoUpdateSpeedSensorPvResult()
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
                            viewModel.setSpeedSensorTestResult(it)
                            viewModel.autoUpdateSpeedSensorPvResult()
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
                            viewModel.setSpeedSensorLatched(it)
                            viewModel.autoUpdateSpeedSensorPvResult()
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
                            viewModel.setSpeedSensorCR(it)
                            viewModel.autoUpdateSpeedSensorPvResult()
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
                        value = viewModel.speedSensorTestPvResult.value,
                        onValueChange = viewModel::setSpeedSensorTestPvResult,
                        helpText = "Overall status for Speed Sensor failsafe validation."
                    )
                    FormSpacer()
                    PvSectionSummaryCard(title = "Speed sensor test P.V. Summary", rules = rules)
                    FormSpacer()
                }

                LabeledTextFieldWithHelp(
                    label = "Engineer Comments",
                    value = notes,
                    onValueChange = viewModel::setSpeedSensorEngineerNotes,
                    helpText = "Enter any notes relevant to this section.",
                    isNAToggleEnabled = false,
                    maxLength = 50
                )

                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}
