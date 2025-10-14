package com.example.mecca.screens.metaldetectorcalibration

import com.example.mecca.CalibrationBanner
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.mecca.calibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import com.example.mecca.calibrationViewModels.CalibrationNavigationButtons
import com.example.mecca.formModules.CalibrationHeader
import com.example.mecca.formModules.LabeledTextFieldWithHelp
import com.example.mecca.formModules.LabeledTriStateSwitchAndTextInputWithHelp
import com.example.mecca.formModules.YesNoState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalMetalDetectorConveyorNonFerrousTest(
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
    val sensitivityAsLeftNonFerrous by viewModel.sensitivityAsLeftNonFerrous
    val peakSignalNonFerrousLeading by viewModel.peakSignalNonFerrousLeading
    val peakSignalNonFerrousMiddle by viewModel.peakSignalNonFerrousMiddle
    val peakSignalNonFerrousTrailing by viewModel.peakSignalNonFerrousTrailing

    val detectRejectNonFerrousLeading by viewModel.detectRejectNonFerrousLeading
    val detectRejectNonFerrousMiddle by viewModel.detectRejectNonFerrousMiddle
    val detectRejectNonFerrousTrailing by viewModel.detectRejectNonFerrousTrailing

    val sampleCertificateNumberNonFerrous by viewModel.sampleCertificateNumberNonFerrous

    val nonFerrousTestEngineerNotes by viewModel.nonFerrousTestEngineerNotes


    //Determine if "Next Step" button should be enabled
    val isNextStepEnabled =
        sensitivityAsLeftNonFerrous.isNotBlank() &&
                sampleCertificateNumberNonFerrous.isNotBlank() &&
                (
                        detectRejectNonFerrousLeading != YesNoState.YES || peakSignalNonFerrousLeading.isNotBlank()
                        ) &&
                (
                        detectRejectNonFerrousMiddle != YesNoState.YES || peakSignalNonFerrousMiddle.isNotBlank()
                        ) &&
                (
                        detectRejectNonFerrousTrailing != YesNoState.YES || peakSignalNonFerrousTrailing.isNotBlank()
                        )


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState) // Add scrolling to the whole column
    ) {
        CalibrationBanner(
            progress = progress,
            viewModel = viewModel

        )

        // Navigation Buttons
        CalibrationNavigationButtons(
            onPreviousClick = { viewModel.updateNonFerrousResult() },
            onCancelClick = { viewModel.updateNonFerrousResult() },
            onNextClick = { navController.navigate("CalMetalDetectorConveyorStainlessTest")
                viewModel.updateNonFerrousResult()},
            isNextEnabled = isNextStepEnabled,
            isFirstStep = false, // Indicates this is the first step and disables the Previous button
            navController = navController,
            viewModel = viewModel,
            onSaveAndExitClick = {
                viewModel.updateNonFerrousResult()
            },
        )

        Spacer(modifier = Modifier.height(16.dp))

        CalibrationHeader("Non-Ferrous Sensitivity (As Left)")

        Spacer(modifier = Modifier.height(20.dp))


        LabeledTextFieldWithHelp(
            label = "Achieved Sensitivity (mm)",
            value = sensitivityAsLeftNonFerrous,
            onValueChange = { newValue -> viewModel.setSensitivityAsLeftNonFerrous(newValue) },
            helpText = "Enter the achieved Non-Ferrous sensitivity e.g '2.0'",
            keyboardType = KeyboardType.Number
        )

        LabeledTextFieldWithHelp(
            label = "Sample Certificate No.",
            value = sampleCertificateNumberNonFerrous,
            onValueChange = { newValue -> viewModel.setSampleCertificateNumberNonFerrous(newValue) },
            helpText = "Enter the metal test sample certificate number, usually located on the test piece",
        )

        LabeledTriStateSwitchAndTextInputWithHelp(
            label = "D&R (Leading)",
            currentState = detectRejectNonFerrousLeading,
            onStateChange = { newState -> viewModel.setDetectRejectNonFerrousLeading(newState) },
            helpText = "Select if there was satisfactory Detection and Rejection of the pack with the metal sample placed in the leading edge. Note down the peak signal.",
            inputLabel = "Produced Signal",
            inputValue = peakSignalNonFerrousLeading,
            onInputValueChange = { newValue -> viewModel.setPeakSignalNonFerrousLeading(newValue) },
            //inputKeyboardType = KeyboardType.Number
        )

        LabeledTriStateSwitchAndTextInputWithHelp(
            label = "D&R (Middle)",
            currentState = detectRejectNonFerrousMiddle,
            onStateChange = { newState -> viewModel.setDetectRejectNonFerrousMiddle(newState) },
            helpText = "Select if there was satisfactory Detection and Dejection of the pack with the metal sample placed in the middle. Note down the peak signal.",
            inputLabel = "Produced Signal",
            inputValue = peakSignalNonFerrousMiddle,
            onInputValueChange = { newValue -> viewModel.setPeakSignalNonFerrousMiddle(newValue) },
            //inputKeyboardType = KeyboardType.Number
        )

        LabeledTriStateSwitchAndTextInputWithHelp(
            label = "D&R (Trailing)",
            currentState = detectRejectNonFerrousTrailing,
            onStateChange = { newState -> viewModel.setDetectRejectNonFerrousTrailing(newState) },
            helpText = "Select if there was satisfactory Detection and Rejection of the pack with the metal sample placed in the trailing edge. Note down the peak signal.",
            inputLabel = "Produced Signal",
            inputValue = peakSignalNonFerrousTrailing,
            onInputValueChange = { newValue -> viewModel.setPeakSignalNonFerrousTrailing(newValue) },
            //inputKeyboardType = KeyboardType.Number
        )

        Spacer(modifier = Modifier.height(16.dp))

        LabeledTextFieldWithHelp(
            label = "Engineer Notes",
            value = nonFerrousTestEngineerNotes,
            onValueChange = { newValue -> viewModel.setNonFerrousTestEngineerNotes(newValue) },
            helpText = "Enter any notes relevant to this section",
            isNAToggleEnabled = false
        )



        Spacer(modifier = Modifier.height(16.dp))


    }
}
