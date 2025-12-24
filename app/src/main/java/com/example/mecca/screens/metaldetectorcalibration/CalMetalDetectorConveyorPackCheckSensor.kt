package com.example.mecca.screens.metaldetectorcalibration

import androidx.compose.foundation.layout.Column
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
import com.example.mecca.calibrationLogic.metalDetectorConveyor.autoUpdatePackCheckSensorPvResult
import com.example.mecca.calibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import com.example.mecca.formModules.CalibrationHeader
import com.example.mecca.formModules.LabeledDropdownWithHelp
import com.example.mecca.formModules.LabeledFourOptionRadioWithHelp
import com.example.mecca.formModules.LabeledMultiSelectDropdownWithHelp
import com.example.mecca.formModules.LabeledTextFieldWithHelp
import com.example.mecca.formModules.LabeledTriStateSwitchAndTextInputWithHelp
import com.example.mecca.formModules.LabeledTriStateSwitchWithHelp
import com.example.mecca.formModules.YesNoState

@Composable
fun CalMetalDetectorConveyorPackCheckSensor(
    navController: NavHostController,
    viewModel: CalibrationMetalDetectorConveyorViewModel
) {
    val scrollState = rememberScrollState()

    val fitted by viewModel.packCheckSensorFitted
    val detail by viewModel.packCheckSensorDetail
    val testMethod by viewModel.packCheckSensorTestMethod
    val testMethodOther by viewModel.packCheckSensorTestMethodOther
    val testResult by viewModel.packCheckSensorTestResult.collectAsState()
    val notes by viewModel.packCheckSensorEngineerNotes
    val latched by viewModel.packCheckSensorLatched
    val controlledRestart by viewModel.packCheckSensorCR

    // Options: remember so Compose doesn’t rebuild them constantly
    val testMethodOptions = remember {
        listOf("Sensor Block", "Remove Pack", "Other")
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

        CalibrationHeader("Failsafe Tests - Pack Check Sensor")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState)
                .imePadding()
        ) {

            LabeledTriStateSwitchAndTextInputWithHelp(
                label = "Sensor fitted?",
                currentState = fitted,
                onStateChange = { newState ->
                    viewModel.setPackCheckSensorFitted(newState)

                    if (newState == YesNoState.NA || newState == YesNoState.NO) {
                        viewModel.setPackCheckSensorDetail("N/A")
                        viewModel.setPackCheckSensorTestMethod("N/A")
                        viewModel.setPackCheckSensorTestMethodOther("N/A")
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
                helpText = "Select if a pack check sensor is fitted and used to detect missing product/packs.",
                inputLabel = "Detail",
                inputValue = detail,
                onInputValueChange = {
                    viewModel.setPackCheckSensorDetail(it)
                    viewModel.autoUpdatePackCheckSensorPvResult()
                }
            )

            if (fitted == YesNoState.YES) {

                LabeledDropdownWithHelp(
                    label = "Test Method",
                    options = testMethodOptions,
                    selectedOption = testMethod,
                    onSelectionChange = {
                        viewModel.setPackCheckSensorTestMethod(it)
                        viewModel.autoUpdatePackCheckSensorPvResult()
                    },
                    helpText = "Select one option from the dropdown.",
                    isNAToggleEnabled = false
                )

                if (testMethod == "Other") {
                    LabeledTextFieldWithHelp(
                        label = "Other Test Method",
                        value = testMethodOther,
                        onValueChange = {
                            viewModel.setPackCheckSensorTestMethodOther(it)
                            viewModel.autoUpdatePackCheckSensorPvResult()
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
                            "No Result" in newSelection -> listOf("No Result")
                            else -> newSelection.filterNot { it == "No Result" }
                        }
                        viewModel.setPackCheckSensorTestResult(cleaned)

                        if (cleaned == listOf("No Result")) {
                            viewModel.setPackCheckSensorLatched(YesNoState.NO)
                            viewModel.setPackCheckSensorCR(YesNoState.NO)
                        }

                        viewModel.autoUpdatePackCheckSensorPvResult()
                    },
                    helpText = "Select one or more items from the dropdown.",
                    isNAToggleEnabled = false
                )

                LabeledTriStateSwitchWithHelp(
                    label = "Fault Latched?",
                    currentState = latched,
                    onStateChange = {
                        viewModel.setPackCheckSensorLatched(it)
                        viewModel.autoUpdatePackCheckSensorPvResult()
                    },
                    helpText = "Is the fault output latched, or does it clear automatically?",
                    isNAToggleEnabled = false
                )

                LabeledTriStateSwitchWithHelp(
                    label = "Controlled Restart?",
                    currentState = controlledRestart,
                    onStateChange = {
                        viewModel.setPackCheckSensorCR(it)
                        viewModel.autoUpdatePackCheckSensorPvResult()
                    },
                    helpText = "Does the fault require a controlled restart after reset?",
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
                    value = viewModel.packCheckSensorTestPvResult.value,
                    onValueChange = viewModel::setPackCheckSensorTestPvResult,
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

            Spacer(modifier = Modifier.height(16.dp))

            LabeledTextFieldWithHelp(
                label = "Engineer Comments",
                value = notes,
                onValueChange = viewModel::setPackCheckSensorEngineerNotes,
                helpText = "Enter any notes relevant to this section.",
                isNAToggleEnabled = false
            )

            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}
