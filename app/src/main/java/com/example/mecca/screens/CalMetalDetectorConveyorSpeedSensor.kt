package com.example.mecca.screens

import CalibrationBanner
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.example.mecca.CalibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import com.example.mecca.CalibrationViewModels.CalibrationNavigationButtons
import com.example.mecca.formModules.CalibrationHeader
import com.example.mecca.formModules.LabeledDropdownWithHelp
import com.example.mecca.formModules.LabeledMultiSelectDropdownWithHelp
import com.example.mecca.formModules.LabeledTextFieldWithHelp
import com.example.mecca.formModules.LabeledTriStateSwitchAndTextInputWithHelp
import com.example.mecca.formModules.LabeledTriStateSwitchWithHelp
import com.example.mecca.formModules.YesNoState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalMetalDetectorConveyorSpeedSensor(
    navController: NavHostController,
    viewModel: CalibrationMetalDetectorConveyorViewModel = viewModel()
) {
    // Stops the next button from being pressed until the screen is rendered
    LaunchedEffect(Unit) {
        viewModel.finishNavigation()
    }

    val progress = viewModel.progress
    val scrollState = rememberScrollState() // Scroll state to control the scroll behavior

    // Get and update data in the ViewModel
    val speedSensorFitted by viewModel.speedSensorFitted
    val speedSensorDetail by viewModel.speedSensorDetail
    val speedSensorTestMethod by viewModel.speedSensorTestMethod
    val speedSensorTestMethodOther by viewModel.speedSensorTestMethodOther
    val speedSensorTestResult by viewModel.speedSensorTestResult.collectAsState()
    val speedSensorEngineerNotes by viewModel.speedSensorEngineerNotes
    val speedSensorLatched by viewModel.speedSensorLatched
    val speedSensorCR by viewModel.speedSensorCR

    // Test options
    val speedSensorTestOptions = listOf(
        "Stop Belt After Detection",
        "Other"
    )

    val speedSensorTestResults = listOf(
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

    var selectedOptions by remember { mutableStateOf(listOf<String>()) }

    //Determine if "Next Step" button should be enabled
    val isNextStepEnabled = when (speedSensorFitted) {
        YesNoState.NO, YesNoState.NA -> true // Button enabled for NO or NA
        YesNoState.YES -> {
            // Button enabled only if all other fields are valid
            speedSensorDetail.isNotBlank() &&
                    speedSensorTestMethod.isNotBlank() &&
                    speedSensorTestResult.isNotEmpty() &&
                    speedSensorLatched != YesNoState.NA &&
                    speedSensorCR != YesNoState.NA
        }
        else -> false // Default to false for safety
    }

    // Column layout
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        CalibrationBanner(
            progress = progress,
            viewModel = viewModel
        )

        // Navigation Buttons
        CalibrationNavigationButtons(
            onPreviousClick = { viewModel.updateSpeedSensor() },
            onCancelClick = { viewModel.updateSpeedSensor() },
            onNextClick = {
                viewModel.updateSpeedSensor()
                navController.navigate("CalMetalDetectorConveyorDetectNotification") },
            isNextEnabled = isNextStepEnabled,
            isFirstStep = false,
            navController = navController,
            viewModel = viewModel,
            onSaveAndExitClick = {
                viewModel.updateSpeedSensor()
            },
        )

        Spacer(modifier = Modifier.height(16.dp))

        CalibrationHeader("Compliance Checks - Speed Sensor")

        Spacer(modifier = Modifier.height(20.dp))

        LabeledTriStateSwitchAndTextInputWithHelp(
            label = "Speed Sensor Fitted?",
            currentState = speedSensorFitted,
            onStateChange = { newState ->
                viewModel.setSpeedSensorFitted(newState)
                if (newState == YesNoState.NA) {
                    // Set all relevant fields to N/A
                    viewModel.setSpeedSensorDetail("N/A")
                    viewModel.setSpeedSensorTestMethod("N/A")
                    viewModel.setSpeedSensorTestMethodOther("N/A")
                    viewModel.setSpeedSensorTestResult(emptyList())
                    viewModel.setSpeedSensorLatched(YesNoState.NA)
                    viewModel.setSpeedSensorCR(YesNoState.NA)
                    selectedOptions = listOf("N/A")
                } else if (newState == YesNoState.YES) {
                    // Clear N/A from selected options when switching back to YES
                    selectedOptions = emptyList() // Clear any selected options
                    viewModel.setSpeedSensorTestResult(emptyList()) // Clear the result in the ViewModel as well
                }
            },
            helpText = "Select if there is a reject confirm sensor fitted",
            inputLabel = "Detail",
            inputValue = speedSensorDetail,
            onInputValueChange = { newValue -> viewModel.setSpeedSensorDetail(newValue) }
        )


        // Conditionally display remaining fields if "Yes" is selected for In-feed sensor fitted
        if (speedSensorFitted == YesNoState.YES) {
            LabeledDropdownWithHelp(
                label = "Test Method",
                options = speedSensorTestOptions,
                selectedOption = speedSensorTestMethod,
                onSelectionChange = { newSelection ->
                    viewModel.setSpeedSensorTestMethod(newSelection)
                },
                helpText = "Select one option from the dropdown.",
                isNAToggleEnabled = false
            )

            if (speedSensorTestMethod == "Other") {
                LabeledTextFieldWithHelp(
                    label = "Other Test Method",
                    value = speedSensorTestMethodOther,
                    onValueChange = { newValue -> viewModel.setSpeedSensorTestMethodOther(newValue) },
                    helpText = "Enter the custom test method",
                    isNAToggleEnabled = false
                )
            }

            val selectedOptions by viewModel.speedSensorTestResult.collectAsState()

            LabeledMultiSelectDropdownWithHelp(
                label = "Test Result",
                options = speedSensorTestResults,
                selectedOptions = selectedOptions,
                value = selectedOptions.joinToString(", "),
                onSelectionChange = { newSelectedOptions -> viewModel.setSpeedSensorTestResult(newSelectedOptions) },
                helpText = "Select one or more items from the dropdown.",
                isNAToggleEnabled = false
            )

            LabeledTriStateSwitchWithHelp(
                label = "Fault Latched?",
                currentState = speedSensorLatched,
                onStateChange = { newState -> viewModel.setSpeedSensorLatched(newState) },
                helpText = "Is the fault output latched, or does it clear automatically?",
                isNAToggleEnabled = false
            )

            LabeledTriStateSwitchWithHelp(
                label = "Fault Controlled Restart?",
                currentState = speedSensorCR,
                onStateChange = { newState -> viewModel.setSpeedSensorCR(newState) },
                helpText = "Is the fault output latched, or does it clear automatically?",
                isNAToggleEnabled = false
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        LabeledTextFieldWithHelp(
            label = "Engineer Comments",
            value = speedSensorEngineerNotes,
            onValueChange = { newValue -> viewModel.setSpeedSensorEngineerNotes(newValue) },
            helpText = "Enter any notes relevant to this section",
            isNAToggleEnabled = false
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}
