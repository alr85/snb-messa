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
fun CalMetalDetectorConveyorBackupPEC(
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
    val backupSensorFitted by viewModel.backupSensorFitted
    val backupSensorDetail by viewModel.backupSensorDetail
    val backupSensorTestMethod by viewModel.backupSensorTestMethod
    val backupSensorTestMethodOther by viewModel.backupSensorTestMethodOther
    val backupSensorTestResult by viewModel.backupSensorTestResult.collectAsState()
    val backupSensorEngineerNotes by viewModel.backupSensorEngineerNotes
    val backupSensorLatched by viewModel.backupSensorLatched
    val backupSensorCR by viewModel.backupSensorCR

    // Test options
    val backupSensorTestOptions = listOf(
        "Reject Override Switch",
        "Product Removal",
        "Other"
    )

    val backupSensorTestResults = listOf(
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
    val isNextStepEnabled = when (backupSensorFitted) {
        YesNoState.NO, YesNoState.NA -> true // Button enabled for NO or NA
        YesNoState.YES -> {
            // Button enabled only if all other fields are valid
            backupSensorDetail.isNotBlank() &&
                    backupSensorTestMethod.isNotBlank() &&
                    backupSensorTestResult.isNotEmpty() &&
                    backupSensorLatched != YesNoState.NA &&
                    backupSensorCR != YesNoState.NA
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
            onPreviousClick = { viewModel.updateBackupSensor() },
            onCancelClick = {
                viewModel.updateBackupSensor()
            },
            onNextClick = {
                viewModel.updateBackupSensor()
                navController.navigate("CalMetalDetectorConveyorAirPressureSensor") },
            isNextEnabled = isNextStepEnabled,
            isFirstStep = false,
            navController = navController,
            viewModel = viewModel,
            onSaveAndExitClick = {
                viewModel.updateBackupSensor()
            },
        )

        Spacer(modifier = Modifier.height(16.dp))

        CalibrationHeader("Compliance Checks - Backup")

        Spacer(modifier = Modifier.height(20.dp))

        LabeledTriStateSwitchAndTextInputWithHelp(
            label = "Back-up sensor fitted?",
            currentState = backupSensorFitted,
            onStateChange = { newState ->
                viewModel.setBackupSensorFitted(newState)
                if (newState == YesNoState.NA) {
                    // Set all relevant fields to N/A
                    viewModel.setBackupSensorDetail("N/A")
                    viewModel.setBackupSensorTestMethod("N/A")
                    viewModel.setBackupSensorTestMethodOther("N/A")
                    viewModel.setBackupSensorTestResult(emptyList())
                    viewModel.setBackupSensorLatched(YesNoState.NA)
                    viewModel.setBackupSensorCR(YesNoState.NA)
                    selectedOptions = listOf("N/A")
                } else if (newState == YesNoState.YES) {
                    // Clear N/A from selected options when switching back to YES
                    selectedOptions = emptyList() // Clear any selected options
                    viewModel.setBackupSensorTestResult(emptyList()) // Clear the result in the ViewModel as well
                }
            },
            helpText = "Select if there is a reject confirm sensor fitted",
            inputLabel = "Detail",
            inputValue = backupSensorDetail,
            onInputValueChange = { newValue -> viewModel.setBackupSensorDetail(newValue) }
        )


        // Conditionally display remaining fields if "Yes" is selected for In-feed sensor fitted
        if (backupSensorFitted == YesNoState.YES) {
            LabeledDropdownWithHelp(
                label = "Test Method",
                options = backupSensorTestOptions,
                selectedOption = backupSensorTestMethod,
                onSelectionChange = { newSelection ->
                    viewModel.setBackupSensorTestMethod(newSelection)
                },
                helpText = "Select one option from the dropdown.",
                isNAToggleEnabled = false
            )

            if (backupSensorTestMethod == "Other") {
                LabeledTextFieldWithHelp(
                    label = "Other Test Method",
                    value = backupSensorTestMethodOther,
                    onValueChange = { newValue -> viewModel.setBackupSensorTestMethodOther(newValue) },
                    helpText = "Enter the custom test method",
                    isNAToggleEnabled = false
                )
            }

            val selectedOptions by viewModel.backupSensorTestResult.collectAsState()

            LabeledMultiSelectDropdownWithHelp(
                label = "Test Result",
                options = backupSensorTestResults,
                value = selectedOptions.joinToString(", "),
                selectedOptions = selectedOptions,
                onSelectionChange = { newSelectedOptions -> viewModel.setBackupSensorTestResult(newSelectedOptions) },
                helpText = "Select one or more items from the dropdown.",
                isNAToggleEnabled = false
            )

            LabeledTriStateSwitchWithHelp(
                label = "Fault Latched?",
                currentState = backupSensorLatched,
                onStateChange = { newState -> viewModel.setBackupSensorLatched(newState) },
                helpText = "Is the fault output latched, or does it clear automatically?",
                isNAToggleEnabled = false
            )

            LabeledTriStateSwitchWithHelp(
                label = "Fault Controlled Restart?",
                currentState = backupSensorCR,
                onStateChange = { newState -> viewModel.setBackupSensorCR(newState) },
                helpText = "Is the fault output latched, or does it clear automatically?",
                isNAToggleEnabled = false
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        LabeledTextFieldWithHelp(
            label = "Engineer Comments",
            value = backupSensorEngineerNotes,
            onValueChange = { newValue -> viewModel.setBackupSensorEngineerNotes(newValue) },
            helpText = "Enter any notes relevant to this section",
            isNAToggleEnabled = false
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}
