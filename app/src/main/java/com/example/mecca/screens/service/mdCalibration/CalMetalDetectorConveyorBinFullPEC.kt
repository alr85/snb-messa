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

    // Options: remember so Compose doesn’t rebuild them constantly
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

        CalibrationHeader("Failsafe Tests - Bin Full Sensor")

        ScrollableWithScrollbar(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
        ) {

            Column {

                LabeledYesNoSegmentedSwitchAndTextInputWithHelp(
                    label = "Sensor fitted?",
                    currentState = fitted,
                    onStateChange = { newState ->
                        viewModel.setBinFullSensorFitted(newState)

                        if (newState == YesNoState.NO || newState == YesNoState.NA) {
                            // Set all relevant fields to N/A
                            viewModel.setBinFullSensorDetail("N/A")
                            viewModel.setBinFullSensorTestMethod("N/A")
                            viewModel.setBinFullSensorTestMethodOther("N/A")
                            viewModel.setBinFullSensorTestResult(emptyList())
                            viewModel.setBinFullSensorLatched(YesNoState.NA)
                            viewModel.setBinFullSensorCR(YesNoState.NA)
                        } else if (newState == YesNoState.YES) {
                            // Clear fields for proper entry
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
                    inputMaxLength = 12
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
                        helpText = "Select one option from the dropdown.",
                        isNAToggleEnabled = false
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
                    }

                    FormSpacer()

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

                            viewModel.setBinFullSensorTestResult(cleaned)

                            if (cleaned == listOf("No Result")) {
                                viewModel.setBinFullSensorLatched(YesNoState.NO)
                                viewModel.setBinFullSensorCR(YesNoState.NO)
                            }

                            viewModel.autoUpdateBinFullSensorPvResult()
                        },
                        helpText = "Select one or more items from the dropdown.",
                        isNAToggleEnabled = false
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
                        isNAToggleEnabled = false
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
                        value = viewModel.binFullSensorTestPvResult.value,
                        onValueChange = viewModel::setBinFullSensorTestPvResult,
                        helpText = """
                    Auto-Pass rules (when PV required):
                      • Sensor fitted = Yes
                      • Detail entered
                      • Test method selected (and 'Other' described if chosen)
                      • At least one test result selected
                      • Fault Latched is YES
                      • Controlled Restart is YES

                    If sensor is NO → PV = N/F.
                    If sensor is N/A → PV = N/A.
                    Otherwise auto-fail. You may override manually.
                    """.trimIndent()
                    )
                }

                FormSpacer()

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