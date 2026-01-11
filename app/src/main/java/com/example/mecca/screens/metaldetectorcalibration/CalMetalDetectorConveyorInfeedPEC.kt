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
import androidx.navigation.NavHostController
import com.example.mecca.calibrationLogic.metalDetectorConveyor.autoUpdateInfeedSensorPvResult
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
fun CalMetalDetectorConveyorInfeedPEC(
    navController: NavHostController,
    viewModel: CalibrationMetalDetectorConveyorViewModel
) {

    val fitted by viewModel.infeedSensorFitted
    val detail by viewModel.infeedSensorDetail
    val testMethod by viewModel.infeedSensorTestMethod
    val testMethodOther by viewModel.infeedSensorTestMethodOther
    val testResult by viewModel.infeedSensorTestResult.collectAsState()
    val notes by viewModel.infeedSensorEngineerNotes
    val latched by viewModel.infeedSensorLatched
    val controlledRestart by viewModel.infeedSensorCR

    // Options (remembered so Compose doesn’t rebuild them)
    val testMethodOptions = remember {
        listOf(
            "Large Metal Test",
            "Manual Block",
            "Device Block",
            "Other"
        )
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

    // -----------------------------
    // Next enabled logic
    // -----------------------------
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

        CalibrationHeader("Failsafe Tests - Photogating/Infeed Sensor")

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
                        viewModel.setInfeedSensorFitted(newState)

                        if (newState == YesNoState.NA || newState == YesNoState.NO) {
                            viewModel.setInfeedSensorDetail("N/A")
                            viewModel.setInfeedSensorTestMethod("N/A")
                            viewModel.setInfeedSensorTestMethodOther("N/A")
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
                    inputLabel = "Detail",
                    inputValue = detail,
                    onInputValueChange = {
                        viewModel.setInfeedSensorDetail(it)
                        viewModel.autoUpdateInfeedSensorPvResult()
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (fitted == YesNoState.YES) {

                    LabeledDropdownWithHelp(
                        label = "Test Method",
                        options = testMethodOptions,
                        selectedOption = testMethod,
                        onSelectionChange = {
                            viewModel.setInfeedSensorTestMethod(it)
                            viewModel.autoUpdateInfeedSensorPvResult()
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
                                viewModel.setInfeedSensorTestMethodOther(it)
                                viewModel.autoUpdateInfeedSensorPvResult()
                            },
                            helpText = "Enter the custom test method.",
                            isNAToggleEnabled = false
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    LabeledMultiSelectDropdownWithHelp(
                        label = "Test Result",
                        value = testResult.joinToString(", "),
                        options = testResultOptions,
                        selectedOptions = testResult,
                        onSelectionChange = { newSelection ->

                            val cleaned = when {
                                // If "No Result" is selected, it becomes the ONLY selection
                                "No Result" in newSelection -> listOf("No Result")

                                // Otherwise, ensure "No Result" isn't hanging around
                                else -> newSelection.filterNot { it == "No Result" }
                            }

                            viewModel.setInfeedSensorTestResult(cleaned)

                            if (cleaned == listOf("No Result")) {
                                viewModel.setInfeedSensorLatched(YesNoState.NO)
                                viewModel.setInfeedSensorCR(YesNoState.NO)
                            }

                            viewModel.autoUpdateInfeedSensorPvResult()
                        },
                        helpText = "Select one or more items from the dropdown.",
                        isNAToggleEnabled = false
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    LabeledTriStateSwitchWithHelp(
                        label = "Fault Latched?",
                        currentState = latched,
                        onStateChange = {
                            viewModel.setInfeedSensorLatched(it)
                            viewModel.autoUpdateInfeedSensorPvResult()
                        },
                        helpText = "Is the fault output latched, or does it clear automatically?",
                        isNAToggleEnabled = false
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    LabeledTriStateSwitchWithHelp(
                        label = "Controlled Restart?",
                        currentState = controlledRestart,
                        onStateChange = {
                            viewModel.setInfeedSensorCR(it)
                            viewModel.autoUpdateInfeedSensorPvResult()
                        },
                        helpText = "Is a controlled restart required after a fault?",
                        isNAToggleEnabled = false
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // -----------------------------------------------------
                // ⭐ PV RESULT (only when required)
                // -----------------------------------------------------
                if (viewModel.pvRequired.value) {
                    LabeledFourOptionRadioWithHelp(
                        label = "P.V. Result",
                        value = viewModel.infeedSensorTestPvResult.value,
                        onValueChange = viewModel::setInfeedSensorTestPvResult,
                        helpText = """
                        Auto-Pass rules (when PV required):
                          • Sensor fitted = Yes
                          • Detail entered
                          • Test method selected (and 'Other' described if chosen)
                          • At least one test result selected
                          • Fault Latched is not N/A
                          • Controlled Restart is not N/A

                        Otherwise auto-fail. You may override manually.
                    """.trimIndent()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                LabeledTextFieldWithHelp(
                    label = "Engineer Comments",
                    value = notes,
                    onValueChange = viewModel::setInfeedSensorEngineerNotes,
                    helpText = "Enter any notes relevant to this section.",
                    isNAToggleEnabled = false
                )

                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}