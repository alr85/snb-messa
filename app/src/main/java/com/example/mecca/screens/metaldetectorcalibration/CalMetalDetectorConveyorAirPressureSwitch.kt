package com.example.mecca.screens.metaldetectorcalibration

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
import com.example.mecca.calibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import com.example.mecca.formModules.CalibrationHeader
import com.example.mecca.formModules.LabeledDropdownWithHelp
import com.example.mecca.formModules.LabeledFourOptionRadioWithHelp
import com.example.mecca.formModules.LabeledMultiSelectDropdownWithHelp
import com.example.mecca.formModules.LabeledTextFieldWithHelp
import com.example.mecca.formModules.LabeledTriStateSwitchWithHelp
import com.example.mecca.formModules.LabeledYesNoSegmentedSwitchAndTextInputWithHelp
import com.example.mecca.formModules.YesNoState
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

    // Options: remember so Compose doesn’t rebuild them constantly
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

    // Tell wrapper
    LaunchedEffect(isNextStepEnabled) {
        viewModel.setCurrentScreenNextEnabled(isNextStepEnabled)
    }

    Column(modifier = Modifier.fillMaxSize()) {

        CalibrationHeader("Failsafe Tests - Air Pressure Sensor")

        ScrollableWithScrollbar(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
        ) {

            Column {

                LabeledYesNoSegmentedSwitchAndTextInputWithHelp(
                    label = "Air pressure sensor fitted?",
                    currentState = fitted,
                    onStateChange = { newState ->
                        viewModel.setAirPressureSensorFitted(newState)

                        if (newState == YesNoState.NA || newState == YesNoState.NO) {
                            viewModel.setAirPressureSensorDetail("N/A")
                            viewModel.setAirPressureSensorTestMethod("N/A")
                            viewModel.setAirPressureSensorTestMethodOther("N/A")
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
                    inputLabel = "Detail",
                    inputValue = detail,
                    onInputValueChange = {
                        viewModel.setAirPressureSensorDetail(it)
                        viewModel.autoUpdateAirPressureSensorPvResult()
                    }
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
                        helpText = "Select one option from the dropdown.",
                        isNAToggleEnabled = false
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
                            isNAToggleEnabled = false
                        )
                    }

                    FormSpacer()

                    LabeledMultiSelectDropdownWithHelp(
                        label = "Test Result",
                        value = testResult.joinToString(", "),
                        options = testResultOptions,
                        selectedOptions = testResult,
                        onSelectionChange = { newSelection ->

                            val cleaned = when {
                                "No Result" in newSelection -> listOf("No Result")
                                else -> newSelection.filterNot { it == "No Result" }
                            }
                            viewModel.setAirPressureSensorTestResult(cleaned)

                            if (cleaned == listOf("No Result")) {
                                viewModel.setAirPressureSensorLatched(YesNoState.NO)
                                viewModel.setAirPressureSensorCR(YesNoState.NO)
                            }

                            viewModel.autoUpdateAirPressureSensorPvResult()
                        },
                        helpText = "Select one or more items from the dropdown.",
                        isNAToggleEnabled = false
                    )

                    FormSpacer()

                    LabeledTriStateSwitchWithHelp(
                        label = "Fault Latched?",
                        currentState = latched,
                        onStateChange = {
                            viewModel.setAirPressureSensorLatched(it)
                            viewModel.autoUpdateAirPressureSensorPvResult()
                        },
                        helpText = "Is the fault output latched, or does it clear automatically?",
                        isNAToggleEnabled = false
                    )

                    FormSpacer()

                    LabeledTriStateSwitchWithHelp(
                        label = "Controlled Restart?",
                        currentState = controlledRestart,
                        onStateChange = {
                            viewModel.setAirPressureSensorCR(it)
                            viewModel.autoUpdateAirPressureSensorPvResult()
                        },
                        helpText = "Is a controlled restart required after the fault condition?",
                        isNAToggleEnabled = false
                    )
                }

                FormSpacer()

                //-----------------------------------------------------
                // ⭐ PV RESULT (only when required)
                //-----------------------------------------------------
                if (viewModel.pvRequired.value) {
                    LabeledFourOptionRadioWithHelp(
                        label = "P.V. Result",
                        value = viewModel.airPressureSensorTestPvResult.value,
                        onValueChange = viewModel::setAirPressureSensorTestPvResult,
                        helpText = """
                        Auto-Pass rules (when PV required):
                          • Sensor fitted = Yes
                          • Detail entered
                          • Test method selected (and 'Other' described if chosen)
                          • At least one test result selected (and not "No Result")
                          • Fault Latched = Yes
                          • Controlled Restart = Yes

                        If sensor is No → PV = N/F.
                        If sensor is N/A → PV = N/A.
                        Otherwise auto-fail. You may override manually.
                    """.trimIndent()
                    )
                }

                FormSpacer()

                LabeledTextFieldWithHelp(
                    label = "Engineer Comments",
                    value = notes,
                    onValueChange = viewModel::setAirPressureSensorEngineerNotes,
                    helpText = "Enter any notes relevant to this section.",
                    isNAToggleEnabled = false
                )

                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}