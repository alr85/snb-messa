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
import com.example.mecca.CalibrationBanner
import com.example.mecca.calibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import com.example.mecca.calibrationViewModels.CalibrationNavigationButtons
import com.example.mecca.formModules.CalibrationHeader
import com.example.mecca.formModules.LabeledDropdownWithHelp
import com.example.mecca.formModules.LabeledMultiSelectDropdownWithHelp
import com.example.mecca.formModules.LabeledTextFieldWithHelp
import com.example.mecca.formModules.LabeledTriStateSwitchAndTextInputWithHelp
import com.example.mecca.formModules.LabeledTriStateSwitchWithHelp
import com.example.mecca.formModules.YesNoState

//@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalMetalDetectorConveyorBinFullPEC(
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
    val binFullSensorFitted by viewModel.binFullSensorFitted
    val binFullSensorDetail by viewModel.binFullSensorDetail
    val binFullSensorTestMethod by viewModel.binFullSensorTestMethod
    val binFullSensorTestMethodOther by viewModel.binFullSensorTestMethodOther
    val binFullSensorTestResult by viewModel.binFullSensorTestResult.collectAsState()
    val binFullSensorEngineerNotes by viewModel.binFullSensorEngineerNotes
    val binFullSensorLatched by viewModel.binFullSensorLatched
    val binFullSensorCR by viewModel.binFullSensorCR

    // Test options
    val binFullSensorTestOptions = listOf(
        "Manual Block",
        "Device Block",
        "Other"
    )

    val binFullSensorTestResults = listOf(
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
    val isNextStepEnabled = when (binFullSensorFitted) {
        YesNoState.NO, YesNoState.NA -> true // Button enabled for NO or NA
        YesNoState.YES -> {
            // Button enabled only if all other fields are valid
            binFullSensorDetail.isNotBlank() &&
                    binFullSensorTestMethod.isNotBlank() &&
                    binFullSensorTestResult.isNotEmpty() &&
                    binFullSensorLatched != YesNoState.NA &&
                    binFullSensorCR != YesNoState.NA
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
            onPreviousClick = { viewModel.updateBinFullSensor() },
            onCancelClick = {
                viewModel.updateBinFullSensor()
            },
            onNextClick = { navController.navigate("CalMetalDetectorConveyorBackupPEC")
                viewModel.updateBinFullSensor()},
            isNextEnabled = isNextStepEnabled,
            isFirstStep = false,
            navController = navController,
            viewModel = viewModel,
            onSaveAndExitClick = {
                viewModel.updateBinFullSensor()
            },
        )

        Spacer(modifier = Modifier.height(16.dp))

        CalibrationHeader("Compliance Checks - Bin Full")

        Spacer(modifier = Modifier.height(20.dp))

        LabeledTriStateSwitchAndTextInputWithHelp(
            label = "Bin Full sensor fitted?",
            currentState = binFullSensorFitted,
            onStateChange = { newState ->
                viewModel.setBinFullSensorFitted(newState)
                if (newState == YesNoState.NA  || newState == YesNoState.NO) {
                    // Set all relevant fields to N/A
                    viewModel.setBinFullSensorDetail("N/A")
                    viewModel.setBinFullSensorTestMethod("N/A")
                    viewModel.setBinFullSensorTestMethodOther("N/A")
                    viewModel.setBinFullSensorTestResult(emptyList())
                    viewModel.setBinFullSensorLatched(YesNoState.NA)
                    viewModel.setBinFullSensorCR(YesNoState.NA)
                    selectedOptions = listOf("N/A")
                } else if (newState == YesNoState.YES) {
                    // Clear N/A from selected options when switching back to YES
                    selectedOptions = emptyList() // Clear any selected options
                    viewModel.setBinFullSensorTestResult(emptyList()) // Clear the result in the ViewModel as well
                }
            },
            helpText = "Select if there is a bin full sensor fitted",
            inputLabel = "Detail",
            inputValue = binFullSensorDetail,
            onInputValueChange = { newValue -> viewModel.setBinFullSensorDetail(newValue) }
        )


        // Conditionally display remaining fields if "Yes" is selected for In-feed sensor fitted
        if (binFullSensorFitted == YesNoState.YES) {
            LabeledDropdownWithHelp(
                label = "Test Method",
                options = binFullSensorTestOptions,
                selectedOption = binFullSensorTestMethod,
                onSelectionChange = { newSelection ->
                    viewModel.setBinFullSensorTestMethod(newSelection)
                },
                helpText = "Select one option from the dropdown.",
                isNAToggleEnabled = false
            )

            if (binFullSensorTestMethod == "Other") {
                LabeledTextFieldWithHelp(
                    label = "Other Test Method",
                    value = binFullSensorTestMethodOther,
                    onValueChange = { newValue -> viewModel.setBinFullSensorTestMethodOther(newValue) },
                    helpText = "Enter the custom test method",
                    isNAToggleEnabled = false
                )
            }
            val selectedOptions by viewModel.binFullSensorTestResult.collectAsState()

            LabeledMultiSelectDropdownWithHelp(
                label = "Test Result",
                value = selectedOptions.joinToString(", "),
                options = binFullSensorTestResults,
                selectedOptions = selectedOptions,
                onSelectionChange =  { newSelectedOptions -> viewModel.setBinFullSensorTestResult(newSelectedOptions) },
                helpText = "Select one or more items from the dropdown.",
                isNAToggleEnabled = false
            )

            LabeledTriStateSwitchWithHelp(
                label = "Fault Latched?",
                currentState = binFullSensorLatched,
                onStateChange = { newState -> viewModel.setBinFullSensorLatched(newState) },
                helpText = "Is the fault output latched, or does it clear automatically?",
                isNAToggleEnabled = false
            )

            LabeledTriStateSwitchWithHelp(
                label = "Fault Controlled Restart?",
                currentState = binFullSensorCR,
                onStateChange = { newState -> viewModel.setBinFullSensorCR(newState) },
                helpText = "Is the fault output latched, or does it clear automatically?",
                isNAToggleEnabled = false
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        LabeledTextFieldWithHelp(
            label = "Engineer Comments",
            value = binFullSensorEngineerNotes,
            onValueChange = { newValue -> viewModel.setBinFullSensorEngineerNotes(newValue) },
            helpText = "Enter any notes relevant to this section",
            isNAToggleEnabled = false
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}
