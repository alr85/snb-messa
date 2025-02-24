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
fun CalMetalDetectorConveyorInfeedPEC(
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
    val infeedSensorFitted by viewModel.infeedSensorFitted
    val infeedSensorDetail by viewModel.infeedSensorDetail
    val infeedSensorTestMethod by viewModel.infeedSensorTestMethod
    val infeedSensorTestMethodOther by viewModel.infeedSensorTestMethodOther
    val infeedSensorTestResult by viewModel.infeedSensorTestResult.collectAsState()
    val infeedSensorEngineerNotes by viewModel.infeedSensorEngineerNotes
    val infeedSensorLatched by viewModel.infeedSensorLatched
    val infeedSensorCR by viewModel.infeedSensorCR

    // Test options
    val infeedSensorTestOptions = listOf(
        "Large Metal Test",
        "Manual Block",
        "Device Block",
        "Other"
    )

    val infeedSensorTestResults = listOf(
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
    val isNextStepEnabled = when (infeedSensorFitted) {
        YesNoState.NO, YesNoState.NA -> true // Button enabled for NO or NA
        YesNoState.YES -> {
            // Button enabled only if all other fields are valid
            infeedSensorDetail.isNotBlank() &&
                    infeedSensorTestMethod.isNotBlank() &&
                    infeedSensorTestResult.isNotEmpty() &&
                    infeedSensorLatched != YesNoState.NA &&
                    infeedSensorCR != YesNoState.NA
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
            onPreviousClick = { viewModel.updateInfeedSensor() },
            onCancelClick = { viewModel.updateInfeedSensor() },
            onNextClick = {
                viewModel.updateInfeedSensor()
                navController.navigate("CalMetalDetectorConveyorRejectConfirmPEC") },
            isNextEnabled = isNextStepEnabled,
            isFirstStep = false,
            navController = navController,
            viewModel = viewModel,
            onSaveAndExitClick = {
                viewModel.updateInfeedSensor()
            },
        )

        Spacer(modifier = Modifier.height(16.dp))

        CalibrationHeader("Compliance Checks - Infeed Sensor")

        Spacer(modifier = Modifier.height(20.dp))

        LabeledTriStateSwitchAndTextInputWithHelp(
            label = "In-feed sensor fitted?",
            currentState = infeedSensorFitted,
            onStateChange = { newState ->
                viewModel.setInfeedSensorFitted(newState)
                if (newState == YesNoState.NA) {
                    // Set all relevant fields to N/A
                    viewModel.setInfeedSensorDetail("N/A")
                    viewModel.setInfeedSensorTestMethod("N/A")
                    viewModel.setInfeedSensorTestMethodOther("N/A")
                    viewModel.setInfeedSensorTestResult(emptyList())
                    viewModel.setInfeedSensorLatched(YesNoState.NA)
                    viewModel.setInfeedSensorCR(YesNoState.NA)
                    selectedOptions = listOf("N/A")
                } else if (newState == YesNoState.YES) {
                    // Clear N/A from selected options when switching back to YES
                    selectedOptions = emptyList() // Clear any selected options
                    viewModel.setInfeedSensorTestResult(emptyList()) // Clear the result in the ViewModel as well
                }
            },
            helpText = "Select if there is an in-feed/gated timer sensor fitted",
            inputLabel = "Detail",
            inputValue = infeedSensorDetail,
            onInputValueChange = { newValue -> viewModel.setInfeedSensorDetail(newValue) }
        )


        // Conditionally display remaining fields if "Yes" is selected for In-feed sensor fitted
        if (infeedSensorFitted == YesNoState.YES) {
            LabeledDropdownWithHelp(
                label = "Test Method",
                options = infeedSensorTestOptions,
                selectedOption = infeedSensorTestMethod,
                onSelectionChange = { newSelection ->
                    viewModel.setInfeedSensorTestMethod(newSelection)
                },
                helpText = "Select one option from the dropdown.",
                isNAToggleEnabled = false
            )

            if (infeedSensorTestMethod == "Other") {
                LabeledTextFieldWithHelp(
                    label = "Other Test Method",
                    value = infeedSensorTestMethodOther,
                    onValueChange = { newValue -> viewModel.setInfeedSensorTestMethodOther(newValue) },
                    helpText = "Enter the custom test method",
                    isNAToggleEnabled = false
                )
            }

            val selectedOptions by viewModel.infeedSensorTestResult.collectAsState()

            LabeledMultiSelectDropdownWithHelp(
                label = "Test Result",
                value = selectedOptions.joinToString(", "), // Display selected options as string
                options = infeedSensorTestResults,
                selectedOptions = selectedOptions,
                onSelectionChange =  { newSelectedOptions ->
                    viewModel.setInfeedSensorTestResult(newSelectedOptions) },
                helpText = "Select one or more items from the dropdown.",
                isNAToggleEnabled = false
            )

            LabeledTriStateSwitchWithHelp(
                label = "Fault Latched?",
                currentState = infeedSensorLatched,
                onStateChange = { newState -> viewModel.setInfeedSensorLatched(newState) },
                helpText = "Is the fault output latched, or does it clear automatically?",
                isNAToggleEnabled = false
            )

            LabeledTriStateSwitchWithHelp(
                label = "Fault Controlled Restart?",
                currentState = infeedSensorCR,
                onStateChange = { newState -> viewModel.setInfeedSensorCR(newState) },
                helpText = "Is the fault output latched, or does it clear automatically?",
                isNAToggleEnabled = false
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        LabeledTextFieldWithHelp(
            label = "Engineer Comments",
            value = infeedSensorEngineerNotes,
            onValueChange = { newValue -> viewModel.setInfeedSensorEngineerNotes(newValue) },
            helpText = "Enter any notes relevant to this section",
            isNAToggleEnabled = false
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}
