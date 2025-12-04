package com.example.mecca.screens.metaldetectorcalibration

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
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
import com.example.mecca.calibrationLogic.metalDetectorConveyor.autoUpdateFerrousPvResult
import com.example.mecca.calibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import com.example.mecca.calibrationViewModels.CalibrationNavigationButtons
import com.example.mecca.formModules.CalibrationHeader
import com.example.mecca.formModules.LabeledTextFieldWithHelp
import com.example.mecca.formModules.LabeledThreeOptionRadioWithHelp
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




    Column(modifier = Modifier.fillMaxSize()) {

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
        CalibrationHeader("Ferrous Sensitivity (As Left)")


        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState)
                .imePadding()
        ) {

            LabeledTextFieldWithHelp(
                label = "Achieved Sensitivity (mm)",
                value = sensitivityAsLeftFerrous,
                onValueChange = { newValue ->
                    viewModel.setSensitivityAsLeftFerrous(newValue)

                    if (newValue == "N/A") {
                        viewModel.disableFerrousTest()
                    } else {
                        //viewModel.enableFerrousTest()
                    }
                    viewModel.autoUpdateFerrousPvResult()
                },
                helpText = """Enter the achieved Ferrous sensitivity e.g '2.0'
                    
                    M&S Target Sensitivity: ${viewModel.sensitivityData.value?.FerrousTargetMM}mm (Max: ${viewModel.sensitivityData.value?.FerrousMaxMM}mm)
                """.trimIndent(),
                keyboardType = KeyboardType.Number
            )

            if (sensitivityAsLeftFerrous != "N/A") {


                LabeledTextFieldWithHelp(
                    label = "Sample Certificate No.",
                    value = sampleCertificateNumberFerrous,
                    onValueChange = { newValue ->
                        viewModel.setSampleCertificateNumberFerrous(
                            newValue
                        )
                    },
                    helpText = "Enter the metal test sample certificate number, usually located on the test piece",
                )


                LabeledTriStateSwitchAndTextInputWithHelp(
                    label = "Detected & Rejected (Leading)",
                    currentState = detectRejectFerrousLeading,
                    onStateChange = { newState ->
                        viewModel.setDetectRejectFerrousLeading(newState)
                        viewModel.autoUpdateFerrousPvResult()
                    },
                    helpText = "Select if there was satisfactory Detection and Rejection of the pack with the metal sample placed in the leading edge. Note down the peak signal.",
                    inputLabel = "Produced Signal",
                    inputValue = peakSignalFerrousLeading,
                    onInputValueChange = { newValue ->
                        viewModel.setPeakSignalFerrousLeading(newValue)
                        viewModel.autoUpdateFerrousPvResult()
                    },
                    //inputKeyboardType = KeyboardType.Number
                )


                LabeledTriStateSwitchAndTextInputWithHelp(
                    label = "Detected & Rejected (Middle)",
                    currentState = detectRejectFerrousMiddle,
                    onStateChange = { newState ->
                        viewModel.setDetectRejectFerrousMiddle(newState)
                        viewModel.autoUpdateFerrousPvResult()
                    },
                    helpText = "Select if there was satisfactory Detection and Rejection of the pack with the metal sample placed in the middle. Note down the peak signal.",
                    inputLabel = "Produced Signal",
                    inputValue = peakSignalFerrousMiddle,
                    onInputValueChange = { newValue ->
                        viewModel.setPeakSignalFerrousMiddle(newValue)
                        viewModel.autoUpdateFerrousPvResult()
                    },
                    //inputKeyboardType = KeyboardType.Number
                )

                LabeledTriStateSwitchAndTextInputWithHelp(
                    label = "Detected & Rejected (Trailing)",
                    currentState = detectRejectFerrousTrailing,
                    onStateChange = { newState ->
                        viewModel.setDetectRejectFerrousTrailing(newState)
                        viewModel.autoUpdateFerrousPvResult()
                    },
                    helpText = "Select if there was satisfactory Detection and Rejection of the pack with the metal sample placed in the trailing edge. Note down the peak signal.",
                    inputLabel = "Produced Signal",
                    inputValue = peakSignalFerrousTrailing,
                    onInputValueChange = { newValue ->
                        viewModel.setPeakSignalFerrousTrailing(newValue)
                        viewModel.autoUpdateFerrousPvResult()
                    },
                    //inputKeyboardType = KeyboardType.Number
                )


            }

            Spacer(modifier = Modifier.height(16.dp))

            if (viewModel.pvRequired.value) {
                LabeledThreeOptionRadioWithHelp(
                    label = "P.V. Result",
                    value = viewModel.ferrousTestPvResult.value,
                    onValueChange = { viewModel.setFerrousTestPvResult(it) },
                    helpText = """
                        
                        This will automatically set to 'Pass' if:
                            1. The 'Achieved Sensitivity' entered is less than or equal to the M&S Max sensitivity
                            2. The 'Sample Certificate Number' is entered
                            3. All three 'Detected and Rejected' switches are set to 'Yes'
                            4. All 'Produced Signal' fields are entered.
                        
                        Otherwise, it will automatically set to 'Fail'
                        
                        You can manually select Pass, Fail, or N/A as appropriate.
                        
                        """.trimIndent()
                )

            }

            Spacer(modifier = Modifier.height(16.dp))



            LabeledTextFieldWithHelp(
                label = "Engineer Notes",
                value = ferrousTestEngineerNotes,
                onValueChange = { newValue -> viewModel.setFerrousTestEngineerNotes(newValue) },
                helpText = "Enter any notes relevant to this section",
                isNAToggleEnabled = false
            )

            Spacer(modifier = Modifier.height(60.dp))

        }

    }
}
