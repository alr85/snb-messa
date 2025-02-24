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
import com.example.mecca.formModules.LabeledTextFieldWithHelp

//@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalMetalDetectorConveyorDetectNotification(
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
    val detectNotificationResult by viewModel.detectNotificationResult
    val detectNotificationEngineerNotes by viewModel.detectNotificationEngineerNotes

    val detectNotificationTestResults = listOf(
        "No Result",
        "Audible Notification",
        "Visual Notification",
        "On-Screen Notification",
    )

    var selectedOptions by remember { mutableStateOf(listOf<String>()) }

    //Determine if "Next Step" button should be enabled
    val isNextStepEnabled = detectNotificationResult.isNotEmpty()

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
            onPreviousClick = { viewModel.updateDetectNotification() },
            onCancelClick = { viewModel.updateDetectNotification() },
            onNextClick = {
                viewModel.updateDetectNotification()
                navController.navigate("CalMetalDetectorConveyorBinDoorMonitor") },
            isNextEnabled = isNextStepEnabled,
            isFirstStep = false,
            navController = navController,
            viewModel = viewModel,
            onSaveAndExitClick = {
                viewModel.updateDetectNotification()
            },
        )

        Spacer(modifier = Modifier.height(16.dp))

        CalibrationHeader("Compliance Checks - Detect Notification")

        Spacer(modifier = Modifier.height(20.dp))

        LabeledDropdownWithHelp(
            label = "Detect Notification Result",
            options = detectNotificationTestResults,
            selectedOption = detectNotificationResult,
            onSelectionChange = { newSelection ->
                viewModel.setDetectNotificationResult(newSelection)
            },
            helpText = "Select one option from the dropdown.",
            isNAToggleEnabled = false
        )

        Spacer(modifier = Modifier.height(16.dp))

        LabeledTextFieldWithHelp(
            label = "Engineer Comments",
            value = detectNotificationEngineerNotes,
            onValueChange = { newValue -> viewModel.setDetectNotificationEngineerNotes(newValue) },
            helpText = "Enter any notes relevant to this section",
            isNAToggleEnabled = false
        )

        Spacer(modifier = Modifier.height(16.dp))
        }


    }



