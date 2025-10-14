package com.example.mecca.screens.metaldetectorcalibration

import com.example.mecca.CalibrationBanner
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.mecca.calibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import com.example.mecca.calibrationViewModels.CalibrationNavigationButtons
import com.example.mecca.formModules.*

@Composable
fun CalMetalDetectorConveyorPackCheckSensor(
    navController: NavHostController,
    viewModel: CalibrationMetalDetectorConveyorViewModel = viewModel()
) {
    // Disable navigation until the screen is rendered
    LaunchedEffect(Unit) {
        viewModel.finishNavigation()
    }

    val progress = viewModel.progress
    val scrollState = rememberScrollState()

    // --- ViewModel data ---
    val packCheckSensorFitted by viewModel.packCheckSensorFitted
    val packCheckSensorDetail by viewModel.packCheckSensorDetail
    val packCheckSensorTestMethod by viewModel.packCheckSensorTestMethod
    val packCheckSensorTestMethodOther by viewModel.packCheckSensorTestMethodOther
    val packCheckSensorTestResult by viewModel.packCheckSensorTestResult.collectAsState()
    val packCheckSensorEngineerNotes by viewModel.packCheckSensorEngineerNotes
    val packCheckSensorLatched by viewModel.packCheckSensorLatched
    val packCheckSensorCR by viewModel.packCheckSensorCR

    // --- Dropdown options ---
    val packCheckSensorTestOptions = listOf("Sensor Block", "Remove Pack", "Other")

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

    // --- Logic for enabling the Next button ---
    val isNextStepEnabled = when (packCheckSensorFitted) {
        YesNoState.NO, YesNoState.NA -> true
        YesNoState.YES -> {
            packCheckSensorDetail.isNotBlank() &&
                    packCheckSensorTestMethod.isNotBlank() &&
                    packCheckSensorTestResult.isNotEmpty() &&
                    packCheckSensorLatched != YesNoState.NA &&
                    packCheckSensorCR != YesNoState.NA
        }
        else -> false
    }

    // --- UI Layout ---
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

        CalibrationNavigationButtons(
            onPreviousClick = { viewModel.updatePackCheckSensor() },
            onCancelClick = { viewModel.updatePackCheckSensor() },
            onNextClick = {
                viewModel.updatePackCheckSensor()
                navController.navigate("CalMetalDetectorConveyorSpeedSensor")
            },
            isNextEnabled = isNextStepEnabled,
            isFirstStep = false,
            navController = navController,
            viewModel = viewModel,
            onSaveAndExitClick = {
                viewModel.updatePackCheckSensor()
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        CalibrationHeader("Compliance Checks - Pack Check")

        Spacer(modifier = Modifier.height(20.dp))

        // --- Sensor Fitted ---
        LabeledTriStateSwitchAndTextInputWithHelp(
            label = "Pack Check Sensor Fitted?",
            currentState = packCheckSensorFitted,
            onStateChange = { newState ->
                viewModel.setPackCheckSensorFitted(newState)
                if (newState == YesNoState.NA  || newState == YesNoState.NO) {
                    viewModel.setPackCheckSensorDetail("N/A")
                    viewModel.setPackCheckSensorTestMethod("N/A")
                    viewModel.setPackCheckSensorTestMethodOther("N/A")
                    viewModel.setPackCheckSensorTestResult(emptyList())
                    viewModel.setPackCheckSensorLatched(YesNoState.NA)
                    viewModel.setPackCheckSensorCR(YesNoState.NA)
                } else if (newState == YesNoState.YES) {
                    viewModel.setPackCheckSensorTestResult(emptyList())
                }
            },
            helpText = "Select if there is a pack check sensor fitted",
            inputLabel = "Detail",
            inputValue = packCheckSensorDetail,
            onInputValueChange = { newValue ->
                viewModel.setPackCheckSensorDetail(newValue)
            }
        )

        // --- Conditional fields (only if YES) ---
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
                    onValueChange = { newValue ->
                        viewModel.setPackCheckSensorTestMethodOther(newValue)
                    },
                    helpText = "Enter the custom test method",
                    isNAToggleEnabled = false
                )
            }

            LabeledMultiSelectDropdownWithHelp(
                label = "Test Result",
                options = packCheckSensorTestResults,
                selectedOptions = packCheckSensorTestResult,
                value = packCheckSensorTestResult.joinToString(", "),
                onSelectionChange = { newSelectedOptions ->
                    viewModel.setPackCheckSensorTestResult(newSelectedOptions)
                },
                helpText = "Select one or more items from the dropdown.",
                isNAToggleEnabled = false
            )

            LabeledTriStateSwitchWithHelp(
                label = "Fault Latched?",
                currentState = packCheckSensorLatched,
                onStateChange = { newState ->
                    viewModel.setPackCheckSensorLatched(newState)
                },
                helpText = "Is the fault output latched, or does it clear automatically?",
                isNAToggleEnabled = false
            )

            LabeledTriStateSwitchWithHelp(
                label = "Fault Controlled Restart?",
                currentState = packCheckSensorCR,
                onStateChange = { newState ->
                    viewModel.setPackCheckSensorCR(newState)
                },
                helpText = "Does the fault require a controlled restart after reset?",
                isNAToggleEnabled = false
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Engineer Comments ---
        LabeledTextFieldWithHelp(
            label = "Engineer Comments",
            value = packCheckSensorEngineerNotes,
            onValueChange = { newValue ->
                viewModel.setPackCheckSensorEngineerNotes(newValue)
            },
            helpText = "Enter any notes relevant to this section",
            isNAToggleEnabled = false
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}
