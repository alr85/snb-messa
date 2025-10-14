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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.mecca.CalibrationBanner
import com.example.mecca.calibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import com.example.mecca.calibrationViewModels.CalibrationNavigationButtons
import com.example.mecca.formModules.CalibrationHeader
import com.example.mecca.formModules.LabeledTextFieldWithHelp
import com.example.mecca.formModules.LabeledTriStateSwitchAndTextInputWithHelp
import com.example.mecca.formModules.YesNoState

//@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalMetalDetectorConveyorFerrousTest(
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
    val sensitivityAsLeftFerrous by viewModel.sensitivityAsLeftFerrous
    val sampleCertificateNumberFerrous by viewModel.sampleCertificateNumberFerrous
    val peakSignalFerrousLeading by viewModel.peakSignalFerrousLeading
    val peakSignalFerrousMiddle by viewModel.peakSignalFerrousMiddle
    val peakSignalFerrousTrailing by viewModel.peakSignalFerrousTrailing
    val detectRejectFerrousLeading by viewModel.detectRejectFerrousLeading
    val detectRejectFerrousMiddle by viewModel.detectRejectFerrousMiddle
    val detectRejectFerrousTrailing by viewModel.detectRejectFerrousTrailing
    val ferrousTestEngineerNotes by viewModel.ferrousTestEngineerNotes


    //Determine if "Next Step" button should be enabled
    val isNextStepEnabled =
        sensitivityAsLeftFerrous.isNotBlank() &&
                sampleCertificateNumberFerrous.isNotBlank() &&
                (
                        detectRejectFerrousLeading != YesNoState.YES || peakSignalFerrousLeading.isNotBlank()
                        ) &&
                (
                        detectRejectFerrousMiddle != YesNoState.YES || peakSignalFerrousMiddle.isNotBlank()
                        ) &&
                (
                        detectRejectFerrousTrailing != YesNoState.YES || peakSignalFerrousTrailing.isNotBlank()
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
            onPreviousClick = { viewModel.updateFerrousResult() },
            onCancelClick = { viewModel.updateFerrousResult() },
            onNextClick = {
                navController.navigate("CalMetalDetectorConveyorNonFerrousTest")
                viewModel.updateFerrousResult()
                          },
            isNextEnabled = isNextStepEnabled,
            isFirstStep = false, // Indicates this is the first step and disables the Previous button
            navController = navController,
            viewModel = viewModel,
            onSaveAndExitClick = { viewModel.updateFerrousResult() },
        )

        Spacer(modifier = Modifier.height(16.dp))

        CalibrationHeader("Ferrous Sensitivity (As Left)")

        Spacer(modifier = Modifier.height(20.dp))


        LabeledTextFieldWithHelp(
            label = "Achieved Sensitivity (mm)",
            value = sensitivityAsLeftFerrous,
            onValueChange = { newValue -> viewModel.setSensitivityAsLeftFerrous(newValue) },
            helpText = "Enter the achieved Ferrous sensitivity e.g '2.0'",
            keyboardType = KeyboardType.Number
        )

        LabeledTextFieldWithHelp(
            label = "Sample Certificate No.",
            value = sampleCertificateNumberFerrous,
            onValueChange = { newValue -> viewModel.setSampleCertificateNumberFerrous(newValue) },
            helpText = "Enter the metal test sample certificate number, usually located on the test piece",
        )


        LabeledTriStateSwitchAndTextInputWithHelp(
            label = "D&R (Leading)",
            currentState = detectRejectFerrousLeading,
            onStateChange = { newState -> viewModel.setDetectRejectFerrousLeading(newState) },
            helpText = "Select if there was satisfactory Detection and Rejection of the pack with the metal sample placed in the leading edge. Note down the peak signal.",
            inputLabel = "Produced Signal",
            inputValue = peakSignalFerrousLeading,
            onInputValueChange = { newValue -> viewModel.setPeakSignalFerrousLeading(newValue) },
            //inputKeyboardType = KeyboardType.Number
        )

        LabeledTriStateSwitchAndTextInputWithHelp(
            label = "D&R (Middle)",
            currentState = detectRejectFerrousMiddle,
            onStateChange = { newState -> viewModel.setDetectRejectFerrousMiddle(newState) },
            helpText = "Select if there was satisfactory Detection and Rejection of the pack with the metal sample placed in the middle. Note down the peak signal.",
            inputLabel = "Produced Signal",
            inputValue = peakSignalFerrousMiddle,
            onInputValueChange = { newValue -> viewModel.setPeakSignalFerrousMiddle(newValue) },
            //inputKeyboardType = KeyboardType.Number
        )

        LabeledTriStateSwitchAndTextInputWithHelp(
            label = "D&R (Trailing)",
            currentState = detectRejectFerrousTrailing,
            onStateChange = { newState -> viewModel.setDetectRejectFerrousTrailing(newState) },
            helpText = "Select if there was satisfactory Detection and Rejection of the pack with the metal sample placed in the trailing edge. Note down the peak signal.",
            inputLabel = "Produced Signal",
            inputValue = peakSignalFerrousTrailing,
            onInputValueChange = { newValue -> viewModel.setPeakSignalFerrousTrailing(newValue) },
            //inputKeyboardType = KeyboardType.Number
        )

        Spacer(modifier = Modifier.height(16.dp))

        LabeledTextFieldWithHelp(
            label = "Engineer Notes",
            value = ferrousTestEngineerNotes,
            onValueChange = { newValue -> viewModel.setFerrousTestEngineerNotes(newValue) },
            helpText = "Enter any notes relevant to this section",
            isNAToggleEnabled = false
        )


        Spacer(modifier = Modifier.height(16.dp))


    }
}
