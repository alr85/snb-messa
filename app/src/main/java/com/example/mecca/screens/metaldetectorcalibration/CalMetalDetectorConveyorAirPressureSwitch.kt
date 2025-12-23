package com.example.mecca.screens.metaldetectorcalibration

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.mecca.calibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import com.example.mecca.formModules.CalibrationHeader
import com.example.mecca.formModules.LabeledDropdownWithHelp
import com.example.mecca.formModules.LabeledMultiSelectDropdownWithHelp
import com.example.mecca.formModules.LabeledTextFieldWithHelp
import com.example.mecca.formModules.LabeledTriStateSwitchAndTextInputWithHelp
import com.example.mecca.formModules.LabeledTriStateSwitchWithHelp
import com.example.mecca.formModules.YesNoState

//@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalMetalDetectorConveyorAirPressureSensor(
    navController: NavHostController,
    viewModel: CalibrationMetalDetectorConveyorViewModel = viewModel()
) {

    // Stops the next button from being pressed until the screen is rendered
    LaunchedEffect(Unit) {
        viewModel.finishNavigation()
    }

    //val progress = viewModel.progress
    val scrollState = rememberScrollState() // Scroll state to control the scroll behavior

    // Get and update data in the ViewModel
    val airPressureSensorFitted by viewModel.airPressureSensorFitted
    val airPressureSensorDetail by viewModel.airPressureSensorDetail
    val airPressureSensorTestMethod by viewModel.airPressureSensorTestMethod
    val airPressureSensorTestMethodOther by viewModel.airPressureSensorTestMethodOther
    val airPressureSensorTestResult by viewModel.airPressureSensorTestResult.collectAsState()
    val airPressureSensorEngineerNotes by viewModel.airPressureSensorEngineerNotes
    val airPressureSensorLatched by viewModel.airPressureSensorLatched
    val airPressureSensorCR by viewModel.airPressureSensorCR

    // Test options
    val airPressureSensorTestOptions = listOf(
        "Dump Valve",
        "Air Disconnection",
        "Other"
    )

    val airPressureSensorTestResults = listOf(
        "No Result",
        "Audible Notification",
        "Visual Notification",
        "On-Screen Notification",
        "Belt Stops",
        "In-feed Belt Stops",
        "Out-feed Belt Stops",
        "Other"
    )

    var selectedOptions by remember { mutableStateOf(listOf<String>()) }

    //Determine if "Next Step" button should be enabled
    val isNextStepEnabled = when (airPressureSensorFitted) {
        YesNoState.NO, YesNoState.NA -> true // Button enabled for NO or NA
        YesNoState.YES -> {
            // Button enabled only if all other fields are valid
            airPressureSensorDetail.isNotBlank() &&
                    airPressureSensorTestMethod.isNotBlank() &&
                    airPressureSensorTestResult.isNotEmpty() &&
                    airPressureSensorLatched != YesNoState.NA &&
                    airPressureSensorCR != YesNoState.NA
        }

        else -> false // Default to false for safety
    }
    Column(modifier = Modifier.fillMaxSize()) {
//        CalibrationBanner(
//            progress = progress,
//            viewModel = viewModel
//        )

        // Navigation Buttons
//        CalibrationNavigationButtons(
//            onPreviousClick = { viewModel.updateAirPressureSensor() },
//            onCancelClick = {
//                viewModel.updateAirPressureSensor()
//            },
//            onNextClick = {
//                viewModel.updateAirPressureSensor()
//                navController.navigate("CalMetalDetectorConveyorPackCheckSensor")
//            },
//            isNextEnabled = isNextStepEnabled,
//            isFirstStep = false,
//            navController = navController,
//            viewModel = viewModel,
//            onSaveAndExitClick = {
//                viewModel.updateAirPressureSensor()
//            },
//
//            )

        CalibrationHeader("Compliance Checks - Air Pressure")
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {


            LabeledTriStateSwitchAndTextInputWithHelp(
                label = "Air Pressure Sensor Fitted?",
                currentState = airPressureSensorFitted,
                onStateChange = { newState ->
                    viewModel.setAirPressureSensorFitted(newState)
                    if (newState == YesNoState.NA || newState == YesNoState.NO) {
                        // Set all relevant fields to N/A
                        viewModel.setAirPressureSensorDetail("N/A")
                        viewModel.setAirPressureSensorTestMethod("N/A")
                        viewModel.setAirPressureSensorTestMethodOther("N/A")
                        viewModel.setAirPressureSensorTestResult(emptyList())
                        viewModel.setAirPressureSensorLatched(YesNoState.NA)
                        viewModel.setAirPressureSensorCR(YesNoState.NA)
                        selectedOptions = listOf("N/A")
                    } else if (newState == YesNoState.YES) {
                        // Clear N/A from selected options when switching back to YES
                        selectedOptions = emptyList() // Clear any selected options
                        viewModel.setAirPressureSensorDetail("")
                        viewModel.setAirPressureSensorTestMethod("")
                        viewModel.setAirPressureSensorTestMethodOther("")
                        viewModel.setAirPressureSensorTestResult(emptyList())
                        viewModel.setAirPressureSensorLatched(YesNoState.NO)
                        viewModel.setAirPressureSensorCR(YesNoState.NO)
                    }
                },
                helpText = "Select if there is a reject confirm sensor fitted",
                inputLabel = "Detail",
                inputValue = airPressureSensorDetail,
                onInputValueChange = { newValue -> viewModel.setAirPressureSensorDetail(newValue) }
            )


            // Conditionally display remaining fields if "Yes" is selected for In-feed sensor fitted
            if (airPressureSensorFitted == YesNoState.YES) {
                LabeledDropdownWithHelp(
                    label = "Test Method",
                    options = airPressureSensorTestOptions,
                    selectedOption = airPressureSensorTestMethod,
                    onSelectionChange = { newSelection ->
                        viewModel.setAirPressureSensorTestMethod(newSelection)
                    },
                    helpText = "Select one option from the dropdown.",
                    isNAToggleEnabled = false
                )

                if (airPressureSensorTestMethod == "Other") {
                    LabeledTextFieldWithHelp(
                        label = "Other Test Method",
                        value = airPressureSensorTestMethodOther,
                        onValueChange = { newValue ->
                            viewModel.setAirPressureSensorTestMethodOther(
                                newValue
                            )
                        },
                        helpText = "Enter the custom test method",
                        isNAToggleEnabled = false
                    )
                }

                val selectedOptions by viewModel.airPressureSensorTestResult.collectAsState()

                LabeledMultiSelectDropdownWithHelp(
                    label = "Test Result",
                    options = airPressureSensorTestResults,
                    value = selectedOptions.joinToString(", "),
                    selectedOptions = selectedOptions,
                    onSelectionChange = { newSelectedOptions ->
                        viewModel.setAirPressureSensorTestResult(
                            newSelectedOptions
                        )
                    },
                    helpText = "Select one or more items from the dropdown.",
                    isNAToggleEnabled = false
                )

                LabeledTriStateSwitchWithHelp(
                    label = "Fault Latched?",
                    currentState = airPressureSensorLatched,
                    onStateChange = { newState -> viewModel.setAirPressureSensorLatched(newState) },
                    helpText = "Is the fault output latched, or does it clear automatically?",
                    isNAToggleEnabled = false
                )

                LabeledTriStateSwitchWithHelp(
                    label = "Fault Controlled Restart?",
                    currentState = airPressureSensorCR,
                    onStateChange = { newState -> viewModel.setAirPressureSensorCR(newState) },
                    helpText = "Is the fault output latched, or does it clear automatically?",
                    isNAToggleEnabled = false
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            LabeledTextFieldWithHelp(
                label = "Engineer Comments",
                value = airPressureSensorEngineerNotes,
                onValueChange = { newValue -> viewModel.setAirPressureSensorEngineerNotes(newValue) },
                helpText = "Enter any notes relevant to this section",
                isNAToggleEnabled = false
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}