package com.example.mecca.screens.metaldetectorcalibration

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mecca.calibrationLogic.metalDetectorConveyor.autoUpdateRejectConfirmSensorPvResult
import com.example.mecca.calibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import com.example.mecca.formModules.CalibrationHeader
import com.example.mecca.formModules.LabeledDropdownWithHelp
import com.example.mecca.formModules.LabeledFourOptionRadioWithHelp
import com.example.mecca.formModules.LabeledMultiSelectDropdownWithHelp
import com.example.mecca.formModules.LabeledTextFieldWithHelp
import com.example.mecca.formModules.LabeledTriStateSwitchAndTextInputWithHelp
import com.example.mecca.formModules.LabeledTriStateSwitchWithHelp
import com.example.mecca.formModules.LabeledYesNoSegmentedSwitchAndTextInputWithHelp
import com.example.mecca.formModules.YesNoState
import com.example.mecca.ui.theme.ScrollableWithScrollbar

@Composable
fun CalMetalDetectorConveyorRejectConfirmPEC(
    viewModel: CalibrationMetalDetectorConveyorViewModel
) {

    val fitted by viewModel.rejectConfirmSensorFitted
    val detail by viewModel.rejectConfirmSensorDetail
    val testMethod by viewModel.rejectConfirmSensorTestMethod
    val testMethodOther by viewModel.rejectConfirmSensorTestMethodOther
    val testResult by viewModel.rejectConfirmSensorTestResult.collectAsState()
    val notes by viewModel.rejectConfirmSensorEngineerNotes
    val latched by viewModel.rejectConfirmSensorLatched
    val controlledRestart by viewModel.rejectConfirmSensorCR
    val stopPosition by viewModel.rejectConfirmSensorStopPosition

    // Options: remember so Compose doesn’t rebuild them constantly
    val testMethodOptions = remember {
        listOf("Reject Override Switch", "Product Removal", "Other")
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
        )
    }

    val stopPositionOptions = remember {
        listOf(
            "No Result",
            "System Belt",
            "Out-feed Belt (Controlled)",
            "Out-feed Belt (Uncontrolled)"
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
                    stopPosition.isNotBlank() &&
                    (testMethod != "Other" || testMethodOther.isNotBlank())
        }

        else -> false
    }

    // Tell wrapper
    LaunchedEffect(isNextStepEnabled) {
        viewModel.setCurrentScreenNextEnabled(isNextStepEnabled)
    }

    Column(modifier = Modifier.fillMaxSize()) {

        CalibrationHeader("Failsafe Tests - Reject Confirm Sensor")

        ScrollableWithScrollbar(
            modifier = Modifier
                .fillMaxSize()
                .imePadding(),
            contentPadding = PaddingValues(16.dp),
        ) {

            Column {

                LabeledYesNoSegmentedSwitchAndTextInputWithHelp(
                    label = "Sensor fitted?",
                    currentState = fitted,
                    onStateChange = { newState ->
                        viewModel.setRejectConfirmSensorFitted(newState)

                        if (newState == YesNoState.NO || newState == YesNoState.NA) {
                            // Set all relevant fields to N/A
                            viewModel.setRejectConfirmSensorDetail("N/A")
                            viewModel.setRejectConfirmSensorTestMethod("N/A")
                            viewModel.setRejectConfirmSensorTestMethodOther("N/A")
                            viewModel.setRejectConfirmSensorTestResult(emptyList())
                            viewModel.setRejectConfirmSensorLatched(YesNoState.NA)
                            viewModel.setRejectConfirmSensorCR(YesNoState.NA)
                            viewModel.setRejectConfirmSensorStopPosition("N/A")
                        } else if (newState == YesNoState.YES) {
                            // Clear fields for proper entry
                            viewModel.setRejectConfirmSensorDetail("")
                            viewModel.setRejectConfirmSensorTestMethod("")
                            viewModel.setRejectConfirmSensorTestMethodOther("")
                            viewModel.setRejectConfirmSensorTestResult(emptyList())
                            viewModel.setRejectConfirmSensorLatched(YesNoState.NO)
                            viewModel.setRejectConfirmSensorCR(YesNoState.NO)
                            viewModel.setRejectConfirmSensorStopPosition("")
                        }

                        viewModel.autoUpdateRejectConfirmSensorPvResult()
                    },
                    helpText = "Select if there is a reject confirm sensor fitted.",
                    inputLabel = "Detail",
                    inputValue = detail,
                    onInputValueChange = {
                        viewModel.setRejectConfirmSensorDetail(it)
                        viewModel.autoUpdateRejectConfirmSensorPvResult()
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (fitted == YesNoState.YES) {

                    LabeledDropdownWithHelp(
                        label = "Test Method",
                        options = testMethodOptions,
                        selectedOption = testMethod,
                        onSelectionChange = {
                            viewModel.setRejectConfirmSensorTestMethod(it)
                            viewModel.autoUpdateRejectConfirmSensorPvResult()
                        },
                        helpText = "Select one option from the dropdown.",
                        isNAToggleEnabled = false
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (testMethod == "Other") {
                        LabeledTextFieldWithHelp(
                            label = "Other Test Method",
                            value = testMethodOther,
                            onValueChange = {
                                viewModel.setRejectConfirmSensorTestMethodOther(it)
                                viewModel.autoUpdateRejectConfirmSensorPvResult()
                            },
                            helpText = "Enter the custom test method.",
                            isNAToggleEnabled = false
                        )
                    }

                    LabeledMultiSelectDropdownWithHelp(
                        label = "Test Result",
                        value = testResult.joinToString(", "),
                        options = testResultOptions,
                        selectedOptions = testResult,
                        onSelectionChange = { newSelection ->

                            val cleaned = when {
                                // If "No Result" is selected, it becomes the ONLY selection
                                "No Result" in newSelection -> listOf("No Result")
                                else -> newSelection.filterNot { it == "No Result" }
                            }
                            viewModel.setRejectConfirmSensorTestResult(cleaned)


                            if (cleaned == listOf("No Result")) {
                                viewModel.setRejectConfirmSensorLatched(YesNoState.NO)
                                viewModel.setRejectConfirmSensorCR(YesNoState.NO)
                            }

                            viewModel.autoUpdateRejectConfirmSensorPvResult()
                        },
                        helpText = "Select one or more items from the dropdown.",
                        isNAToggleEnabled = false
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    LabeledTriStateSwitchWithHelp(
                        label = "Fault Latched?",
                        currentState = latched,
                        onStateChange = {
                            viewModel.setRejectConfirmSensorLatched(it)
                            viewModel.autoUpdateRejectConfirmSensorPvResult()
                        },
                        helpText = "Is the fault output latched, or does it clear automatically?",
                        isNAToggleEnabled = false
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    LabeledTriStateSwitchWithHelp(
                        label = "Controlled Restart?",
                        currentState = controlledRestart,
                        onStateChange = {
                            viewModel.setRejectConfirmSensorCR(it)
                            viewModel.autoUpdateRejectConfirmSensorPvResult()
                        },
                        helpText = "Is a controlled restart required after a fault?",
                        isNAToggleEnabled = false
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    LabeledDropdownWithHelp(
                        label = "Test Pack Stop Position",
                        options = stopPositionOptions,
                        selectedOption = stopPosition,
                        onSelectionChange = {
                            viewModel.setRejectConfirmSensorStopPosition(it)
                            viewModel.autoUpdateRejectConfirmSensorPvResult()
                        },
                        helpText = "Select one option from the dropdown.",
                        isNAToggleEnabled = false
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                //-----------------------------------------------------
                // ⭐ PV RESULT (only when required)
                //-----------------------------------------------------
                if (viewModel.pvRequired.value) {
                    LabeledFourOptionRadioWithHelp(
                        label = "P.V. Result",
                        value = viewModel.rejectConfirmSensorTestPvResult.value,
                        onValueChange = viewModel::setRejectConfirmSensorTestPvResult,
                        helpText = """
                    Auto-Pass rules (when PV required):
                      • Sensor fitted = Yes
                      • Detail entered
                      • Test method selected (and 'Other' described if chosen)
                      • At least one test result selected
                      • Fault Latched is not N/A
                      • Controlled Restart is not N/A
                      • Stop position selected
                    
                    If sensor is No / N/A → PV = N/A.
                    Otherwise auto-fail. You may override manually.
                    """.trimIndent()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                LabeledTextFieldWithHelp(
                    label = "Engineer Comments",
                    value = notes,
                    onValueChange = viewModel::setRejectConfirmSensorEngineerNotes,
                    helpText = "Enter any notes relevant to this section.",
                    isNAToggleEnabled = false
                )

                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}