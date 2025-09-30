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
fun CalMetalDetectorConveyorRejectConfirmPEC(
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
    val rejectConfirmSensorFitted by viewModel.rejectConfirmSensorFitted
    val rejectConfirmSensorDetail by viewModel.rejectConfirmSensorDetail
    val rejectConfirmSensorTestMethod by viewModel.rejectConfirmSensorTestMethod
    val rejectConfirmSensorTestMethodOther by viewModel.rejectConfirmSensorTestMethodOther
    val rejectConfirmSensorTestResult by viewModel.rejectConfirmSensorTestResult.collectAsState()
    val rejectConfirmSensorEngineerNotes by viewModel.rejectConfirmSensorEngineerNotes
    val rejectConfirmSensorLatched by viewModel.rejectConfirmSensorLatched
    val rejectConfirmSensorCR by viewModel.rejectConfirmSensorCR
    val rejectConfirmSensorStopPosition by viewModel.rejectConfirmSensorStopPosition


    val rejectConfirmSensorTestResults = listOf(
        "No Result",
        "Audible Notification",
        "Visual Notification",
        "On-Screen Notification",
        "Belt Stops",
        "In-feed Belt Stops",
        "Out-feed Belt Stops",
        "Other"
    )

    val rejectConfirmSensorTestOptions = listOf(
        "Reject Override Switch",
        "Product Removal",
        "Other"
    )

    var selectedOptions by remember { mutableStateOf(listOf<String>()) }

    //Determine if "Next Step" button should be enabled
    val isNextStepEnabled = when (rejectConfirmSensorFitted) {
        YesNoState.NO, YesNoState.NA -> true // Button enabled for NO or NA
        YesNoState.YES -> {
            // Button enabled only if all other fields are valid
            rejectConfirmSensorDetail.isNotBlank() &&
                    rejectConfirmSensorTestMethod.isNotBlank() &&
                    rejectConfirmSensorTestResult.isNotEmpty() &&
                    rejectConfirmSensorLatched != YesNoState.NA &&
                    rejectConfirmSensorCR != YesNoState.NA
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
            onPreviousClick = { viewModel.updateRejectConfirmSensor() },
            onCancelClick = { viewModel.updateRejectConfirmSensor() },
            onNextClick = {
                viewModel.updateRejectConfirmSensor()
                navController.navigate("CalMetalDetectorConveyorBinFullPEC") },
            isNextEnabled = isNextStepEnabled,
            isFirstStep = false,
            navController = navController,
            viewModel = viewModel,
            onSaveAndExitClick = {
                viewModel.updateRejectConfirmSensor()
            },
        )

        Spacer(modifier = Modifier.height(16.dp))

        CalibrationHeader("Compliance Checks - Reject Confirm")

        Spacer(modifier = Modifier.height(20.dp))

        LabeledTriStateSwitchAndTextInputWithHelp(
            label = "Reject Confirm sensor fitted?",
            currentState = rejectConfirmSensorFitted,
            onStateChange = { newState ->
                viewModel.setRejectConfirmSensorFitted(newState)
                if (newState == YesNoState.NA) {
                    // Set all relevant fields to N/A
                    viewModel.setRejectConfirmSensorDetail("N/A")
                    viewModel.setRejectConfirmSensorTestMethod("N/A")
                    viewModel.setRejectConfirmSensorTestMethodOther("N/A")
                    viewModel.setRejectConfirmSensorTestResult(emptyList())
                    viewModel.setRejectConfirmSensorLatched(YesNoState.NA)
                    viewModel.setRejectConfirmSensorCR(YesNoState.NA)
                    selectedOptions = listOf("N/A")
                    viewModel.setRejectConfirmSensorStopPosition("N/A")
                } else if (newState == YesNoState.YES) {
                    // Clear N/A from selected options when switching back to YES
                    selectedOptions = emptyList() // Clear any selected options
                    viewModel.setRejectConfirmSensorTestResult(emptyList()) // Clear the result in the ViewModel as well
                }
            },
            helpText = "Select if there is a reject confirm sensor fitted",
            inputLabel = "Detail",
            inputValue = rejectConfirmSensorDetail,
            onInputValueChange = { newValue -> viewModel.setRejectConfirmSensorDetail(newValue) }
        )


        // Conditionally display remaining fields if "Yes" is selected for In-feed sensor fitted
        if (rejectConfirmSensorFitted == YesNoState.YES) {
            LabeledDropdownWithHelp(
                label = "Test Method",
                options = rejectConfirmSensorTestOptions,
                selectedOption = rejectConfirmSensorTestMethod,
                onSelectionChange = { newSelection ->
                    viewModel.setRejectConfirmSensorTestMethod(newSelection)
                },
                helpText = "Select one option from the dropdown.",
                isNAToggleEnabled = false
            )

            if (rejectConfirmSensorTestMethod == "Other") {
                LabeledTextFieldWithHelp(
                    label = "Other Test Method",
                    value = rejectConfirmSensorTestMethodOther,
                    onValueChange = { newValue -> viewModel.setRejectConfirmSensorTestMethodOther(newValue) },
                    helpText = "Enter the custom test method",
                    isNAToggleEnabled = false
                )
            }

            val selectedOptions by viewModel.rejectConfirmSensorTestResult.collectAsState()

            LabeledMultiSelectDropdownWithHelp(
                label = "Test Result",
                value = selectedOptions.joinToString ( ", " ),
                options = rejectConfirmSensorTestResults,
                selectedOptions = selectedOptions,
                onSelectionChange = { newSelectedOptions -> viewModel.setRejectConfirmSensorTestResult(newSelectedOptions) },
                helpText = "Select one or more items from the dropdown.",
                isNAToggleEnabled = false
            )

            LabeledTriStateSwitchWithHelp(
                label = "Fault Latched?",
                currentState = rejectConfirmSensorLatched,
                onStateChange = { newState -> viewModel.setRejectConfirmSensorLatched(newState) },
                helpText = "Is the fault output latched, or does it clear automatically?",
                isNAToggleEnabled = false
            )

            LabeledTriStateSwitchWithHelp(
                label = "Fault Controlled Restart?",
                currentState = rejectConfirmSensorCR,
                onStateChange = { newState -> viewModel.setRejectConfirmSensorCR(newState) },
                helpText = "Is the fault output latched, or does it clear automatically?",
                isNAToggleEnabled = false
            )

            val rejectConfirmStopPositionOptions = listOf(
                "System Belt",
                "Out-feed Belt (Controlled)",
                "Out-feed Belt (Uncontrolled)"
            )

            LabeledDropdownWithHelp(
                label = "Test Pack Stop Position",
                options = rejectConfirmStopPositionOptions,
                selectedOption = rejectConfirmSensorStopPosition,
                onSelectionChange = { newSelection ->
                    viewModel.setRejectConfirmSensorStopPosition(newSelection)
                },
                helpText = "Select one option from the dropdown.",
                isNAToggleEnabled = false
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        LabeledTextFieldWithHelp(
            label = "Engineer Comments",
            value = rejectConfirmSensorEngineerNotes,
            onValueChange = { newValue -> viewModel.setRejectConfirmSensorEngineerNotes(newValue) },
            helpText = "Enter any notes relevant to this section",
            isNAToggleEnabled = false
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}
