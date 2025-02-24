package com.example.mecca.screens

import CalibrationBanner
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
import com.example.mecca.CalibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import com.example.mecca.CalibrationViewModels.CalibrationNavigationButtons
import com.example.mecca.formModules.CalibrationHeader
import com.example.mecca.formModules.LabeledDropdownWithHelp
import com.example.mecca.formModules.LabeledMultiSelectDropdownWithHelp
import com.example.mecca.formModules.LabeledTextFieldWithHelp
import com.example.mecca.formModules.LabeledTriStateSwitchAndTextInputWithHelp
import com.example.mecca.formModules.LabeledTriStateSwitchWithHelp
import com.example.mecca.formModules.YesNoState

//@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalMetalDetectorConveyorPackCheckSensor(
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
    val packCheckSensorFitted by viewModel.packCheckSensorFitted
    val packCheckSensorDetail by viewModel.packCheckSensorDetail
    val packCheckSensorTestMethod by viewModel.packCheckSensorTestMethod
    val packCheckSensorTestMethodOther by viewModel.packCheckSensorTestMethodOther
    val packCheckSensorTestResult by viewModel.packCheckSensorTestResult.collectAsState()
    val packCheckSensorEngineerNotes by viewModel.packCheckSensorEngineerNotes
    val packCheckSensorLatched by viewModel.packCheckSensorLatched
    val packCheckSensorCR by viewModel.packCheckSensorCR

    // Test options
    val packCheckSensorTestOptions = listOf(
        "Sensor Block",
        "Remove Pack",
        "Other"
    )

    val packCheckSensorTestResults = listOf(
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
    val isNextStepEnabled = when (packCheckSensorFitted) {
        YesNoState.NO, YesNoState.NA -> true // Button enabled for NO or NA
        YesNoState.YES -> {
            // Button enabled only if all other fields are valid
            packCheckSensorDetail.isNotBlank() &&
                    packCheckSensorTestMethod.isNotBlank() &&
                    packCheckSensorTestResult.isNotEmpty() &&
                    packCheckSensorLatched != YesNoState.NA &&
                    packCheckSensorCR != YesNoState.NA
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
            onPreviousClick = { viewModel.updatePackCheckSensor() },
            onCancelClick = { viewModel.updatePackCheckSensor() },
            onNextClick = {
                viewModel.updatePackCheckSensor()
                navController.navigate("CalMetalDetectorConveyorSpeedSensor") },
            isNextEnabled = isNextStepEnabled,
            isFirstStep = false,
            navController = navController,
            viewModel = viewModel,
            onSaveAndExitClick = {
                viewModel.updatePackCheckSensor()
            },
        )

        Spacer(modifier = Modifier.height(16.dp))

        CalibrationHeader("Compliance Checks - Pack Check")

        Spacer(modifier = Modifier.height(20.dp))

        LabeledTriStateSwitchAndTextInputWithHelp(
            label = "Pack Check Sensor Fitted?",
            currentState = packCheckSensorFitted,
            onStateChange = { newState ->
                viewModel.setPackCheckSensorFitted(newState)
                if (newState == YesNoState.NA) {
                    // Set all relevant fields to N/A
                    viewModel.setPackCheckSensorDetail("N/A")
                    viewModel.setPackCheckSensorTestMethod("N/A")
                    viewModel.setPackCheckSensorTestMethodOther("N/A")
                    viewModel.setPackCheckSensorTestResult(emptyList())
                    viewModel.setPackCheckSensorLatched(YesNoState.NA)
                    viewModel.setPackCheckSensorCR(YesNoState.NA)
                    selectedOptions = listOf("N/A")
                } else if (newState == YesNoState.YES) {
                    // Clear N/A from selected options when switching back to YES
                    selectedOptions = emptyList() // Clear any selected options
                    viewModel.setPackCheckSensorTestResult(emptyList()) // Clear the result in the ViewModel as well
                }
            },
            helpText = "Select if there is a reject confirm sensor fitted",
            inputLabel = "Detail",
            inputValue = packCheckSensorDetail,
            onInputValueChange = { newValue -> viewModel.setPackCheckSensorDetail(newValue) }
        )


        // Conditionally display remaining fields if "Yes" is selected for In-feed sensor fitted
        if (packCheckSensorFitted == YesNoState.YES) {
            LabeledDropdownWithHelp(
                label = "Test Method",
                options = packCheckSensorTestOptions,
                selectedOption = packCheckSensorTestMethod,
                onSelectionChange = { newSelection ->
                    viewModel.setPackCheckSensorTestMethod(newSelection)
                },
                helpText = "Select one option from the dropdown.",
                isNAToggleEnabled = false
            )

            if (packCheckSensorTestMethod == "Other") {
                LabeledTextFieldWithHelp(
                    label = "Other Test Method",
                    value = packCheckSensorTestMethodOther,
                    onValueChange = { newValue -> viewModel.setPackCheckSensorTestMethodOther(newValue) },
                    helpText = "Enter the custom test method",
                    isNAToggleEnabled = false
                )
            }
        val selectedOptions by viewModel.packCheckSensorTestResult.collectAsState()

            LabeledMultiSelectDropdownWithHelp(
                label = "Test Result",
                options = packCheckSensorTestResults,
                selectedOptions = selectedOptions,
                value = selectedOptions.joinToString(", "),
                onSelectionChange = { newSelectedOptions -> viewModel.setPackCheckSensorTestResult(newSelectedOptions) },
                helpText = "Select one or more items from the dropdown.",
                isNAToggleEnabled = false
            )

            LabeledTriStateSwitchWithHelp(
                label = "Fault Latched?",
                currentState = packCheckSensorLatched,
                onStateChange = { newState -> viewModel.setPackCheckSensorLatched(newState) },
                helpText = "Is the fault output latched, or does it clear automatically?",
                isNAToggleEnabled = false
            )

            LabeledTriStateSwitchWithHelp(
                label = "Fault Controlled Restart?",
                currentState = packCheckSensorCR,
                onStateChange = { newState -> viewModel.setPackCheckSensorCR(newState) },
                helpText = "Is the fault output latched, or does it clear automatically?",
                isNAToggleEnabled = false
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        LabeledTextFieldWithHelp(
            label = "Engineer Comments",
            value = packCheckSensorEngineerNotes,
            onValueChange = { newValue -> viewModel.setPackCheckSensorEngineerNotes(newValue) },
            helpText = "Enter any notes relevant to this section",
            isNAToggleEnabled = false
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}
