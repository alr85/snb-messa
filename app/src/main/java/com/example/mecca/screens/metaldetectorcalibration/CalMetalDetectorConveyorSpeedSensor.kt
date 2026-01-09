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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.mecca.calibrationLogic.metalDetectorConveyor.autoUpdateSpeedSensorPvResult
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalMetalDetectorConveyorSpeedSensor(
    navController: NavHostController,
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

    // Options: remember so Compose doesn’t rebuild them constantly
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

        CalibrationHeader("Failsafe Tests - Speed Sensor")

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
                        viewModel.setSpeedSensorFitted(newState)

                        if (newState == YesNoState.NA || newState == YesNoState.NO) {
                            viewModel.setSpeedSensorDetail("N/A")
                            viewModel.setSpeedSensorTestMethod("N/A")
                            viewModel.setSpeedSensorTestMethodOther("N/A")
                            viewModel.setSpeedSensorTestResult(emptyList())
                            viewModel.setSpeedSensorLatched(YesNoState.NA)
                            viewModel.setSpeedSensorCR(YesNoState.NA)
                        } else if (newState == YesNoState.YES) {
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
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (fitted == YesNoState.YES) {

                    LabeledDropdownWithHelp(
                        label = "Test Method",
                        options = testMethodOptions,
                        selectedOption = testMethod,
                        onSelectionChange = {
                            viewModel.setSpeedSensorTestMethod(it)
                            viewModel.autoUpdateSpeedSensorPvResult()
                        },
                        helpText = "Select one option from the dropdown.",
                        isNAToggleEnabled = false
                    )



                    if (testMethod == "Other") {

                        Spacer(modifier = Modifier.height(16.dp))

                        LabeledTextFieldWithHelp(
                            label = "Other Test Method",
                            value = testMethodOther,
                            onValueChange = {
                                viewModel.setSpeedSensorTestMethodOther(it)
                                viewModel.autoUpdateSpeedSensorPvResult()
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
                                "No Result" in newSelection -> listOf("No Result")
                                else -> newSelection.filterNot { it == "No Result" }
                            }
                            viewModel.setSpeedSensorTestResult(cleaned)

                            if (cleaned == listOf("No Result")) {
                                viewModel.setSpeedSensorLatched(YesNoState.NO)
                                viewModel.setSpeedSensorCR(YesNoState.NO)
                            }

                            viewModel.autoUpdateSpeedSensorPvResult()
                        },
                        helpText = "Select one or more items from the dropdown.",
                        isNAToggleEnabled = false
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    LabeledTriStateSwitchWithHelp(
                        label = "Fault Latched?",
                        currentState = latched,
                        onStateChange = {
                            viewModel.setSpeedSensorLatched(it)
                            viewModel.autoUpdateSpeedSensorPvResult()
                        },
                        helpText = "Is the fault output latched, or does it clear automatically?",
                        isNAToggleEnabled = false
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    LabeledTriStateSwitchWithHelp(
                        label = "Controlled Restart?",
                        currentState = controlledRestart,
                        onStateChange = {
                            viewModel.setSpeedSensorCR(it)
                            viewModel.autoUpdateSpeedSensorPvResult()
                        },
                        helpText = "Is a controlled restart required after a fault?",
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
                        value = viewModel.speedSensorTestPvResult.value,
                        onValueChange = viewModel::setSpeedSensorTestPvResult,
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
                    onValueChange = viewModel::setSpeedSensorEngineerNotes,
                    helpText = "Enter any notes relevant to this section.",
                    isNAToggleEnabled = false
                )

                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}