package com.example.mecca.screens.metaldetectorcalibration

import com.example.mecca.CalibrationBanner
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
import com.example.mecca.calibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import com.example.mecca.calibrationViewModels.CalibrationNavigationButtons
import com.example.mecca.formModules.CalibrationHeader
import com.example.mecca.formModules.LabeledTextFieldWithHelp
import com.example.mecca.formModules.LabeledTwoTextInputsWithHelp

//@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalMetalDetectorConveyorSensitivityAsFound(
    navController: NavHostController,
    viewModel: CalibrationMetalDetectorConveyorViewModel = viewModel()
) {
    // Stops the next button from being pressed until the screen is rendered
    LaunchedEffect(Unit) {
        viewModel.finishNavigation()
    }

    val progress = viewModel.progress
    val scrollState = rememberScrollState()

// Get and update data in the ViewModel
    val sensitivityAsFoundFerrous by viewModel.sensitivityAsFoundFerrous
    val sensitivityAsFoundFerrousPeakSignal by viewModel.sensitivityAsFoundFerrousPeakSignal
    val sensitivityAsFoundNonFerrous by viewModel.sensitivityAsFoundNonFerrous
    val sensitivityAsFoundNonFerrousPeakSignal by viewModel.sensitivityAsFoundNonFerrousPeakSignal
    val sensitivityAsFoundStainless by viewModel.sensitivityAsFoundStainless
    val sensitivityAsFoundStainlessPeakSignal by viewModel.sensitivityAsFoundStainlessPeakSignal
    val productPeakSignalAsFound by viewModel.productPeakSignalAsFound



    //Determine if "Next Step" button should be enabled
    val isNextStepEnabled =
        sensitivityAsFoundFerrous.isNotBlank() &&
                sensitivityAsFoundFerrousPeakSignal.isNotBlank() &&
                sensitivityAsFoundNonFerrous.isNotBlank() &&
                sensitivityAsFoundFerrousPeakSignal.isNotBlank() &&
                sensitivityAsFoundStainless.isNotBlank() &&
                sensitivityAsFoundStainlessPeakSignal.isNotBlank() &&
                productPeakSignalAsFound.isNotBlank()



    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        // Display the consistent banner at the top
        CalibrationBanner(
            progress = progress,
            viewModel = viewModel

        )

        // Navigation Buttons
        CalibrationNavigationButtons(
            onPreviousClick = { viewModel.updateSensitivitiesAsFound() },
            onCancelClick = { viewModel.updateSensitivitiesAsFound() },
            onNextClick = {
                navController.navigate("CalMetalDetectorConveyorFerrousTest")
                viewModel.updateSensitivitiesAsFound()
                          },
            isNextEnabled = isNextStepEnabled,
            isFirstStep = false, // Indicates this is the first step and disables the Previous button
            navController = navController,
            viewModel = viewModel,
            onSaveAndExitClick = {
                //viewModel.saveCalibrationData() // Custom save logic here
            },
        )

        Spacer(modifier = Modifier.height(16.dp))

        CalibrationHeader("Sensitivities (As Found)")

        Spacer(modifier = Modifier.height(20.dp))


//        LabeledTextFieldWithHelp(
//            label = "Ferrous (mm)",
//            value = sensitivityAsFoundFerrous,
//            onValueChange = { newValue -> viewModel.setSensitivityAsFoundFerrous(newValue) },
//            helpText = "Enter the 'As Found' Ferrous sensitivity",
//            keyboardType = KeyboardType.Number
//        )

//        LabeledTextFieldWithHelp(
//            label = "Non Ferrous (mm)",
//            value = sensitivityAsFoundNonFerrous,
//            onValueChange = { newValue -> viewModel.setSensitivityAsFoundNonFerrous(newValue) },
//            helpText = "Enter the 'As Found' Non Ferrous sensitivity",
//            keyboardType = KeyboardType.Number
//        )
//
//        LabeledTextFieldWithHelp(
//            label = "Stainless Steel (mm)",
//            value = sensitivityAsFoundStainless,
//            onValueChange = { newValue -> viewModel.setSensitivityAsFoundStainless(newValue) },
//            helpText = "Enter the 'As Found' Stainless Steel sensitivity",
//            keyboardType = KeyboardType.Number
//        )
//
        LabeledTextFieldWithHelp(
            label = "Product Peak Signal",
            value = productPeakSignalAsFound,
            onValueChange = { newValue -> viewModel.setProductPeakSignalAsFound(newValue) },
            helpText = "Enter the 'As Found' peak signal from the product alone",
        )

        LabeledTwoTextInputsWithHelp(
            label = "Ferrous (mm)",
            firstInputLabel = "Fe",
            firstInputValue = sensitivityAsFoundFerrous,
            onFirstInputValueChange = { newValue -> viewModel.setSensitivityAsFoundFerrous(newValue) },
            secondInputLabel = "Peak Signal",
            secondInputValue = sensitivityAsFoundFerrousPeakSignal,
            onSecondInputValueChange = { newValue -> viewModel.setSensitivityAsFoundFerrousPeakSignal(newValue) },
            helpText = "Enter the 'As Found' Ferrous sensitivity and peak signal",
            firstInputKeyboardType = KeyboardType.Text,
            secondInputKeyboardType = KeyboardType.Text,
            isNAToggleEnabled = true


        )

        LabeledTwoTextInputsWithHelp(
            label = "Non-Ferrous (mm)",
            firstInputLabel = "Non-Fe",
            firstInputValue = sensitivityAsFoundNonFerrous,
            onFirstInputValueChange = { newValue -> viewModel.setSensitivityAsFoundNonFerrous(newValue) },
            secondInputLabel = "Peak Signal",
            secondInputValue = sensitivityAsFoundNonFerrousPeakSignal,
            onSecondInputValueChange = { newValue -> viewModel.setSensitivityAsFoundNonFerrousPeakSignal(newValue) },
            helpText = "Enter the 'As Found' Non-Ferrous sensitivity and peak signal",
            firstInputKeyboardType = KeyboardType.Text,
            secondInputKeyboardType = KeyboardType.Text,
            isNAToggleEnabled = true

        )

        LabeledTwoTextInputsWithHelp(
            label = "Stainless Steel (mm)",
            firstInputLabel = "S/Steel",
            firstInputValue = sensitivityAsFoundStainless,
            onFirstInputValueChange = { newValue -> viewModel.setSensitivityAsFoundStainless(newValue) },
            secondInputLabel = "Peak Signal",
            secondInputValue = sensitivityAsFoundStainlessPeakSignal,
            onSecondInputValueChange = { newValue -> viewModel.setSensitivityAsFoundStainlessPeakSignal(newValue) },
            helpText = "Enter the 'As Found' Stainless Steel sensitivity and peak signal",
            firstInputKeyboardType = KeyboardType.Text,
            secondInputKeyboardType = KeyboardType.Text,
            isNAToggleEnabled = true

        )



        Spacer(modifier = Modifier.height(16.dp))

        LabeledTextFieldWithHelp(
            label = "Engineer Notes",
            value = viewModel.sensitivityAsFoundEngineerNotes.value,
            onValueChange = { newValue -> viewModel.setSensitivityAsFoundEngineerNotes(newValue) },
            helpText = "Enter any notes relevant to this section",
            isNAToggleEnabled = false
        )




        Spacer(modifier = Modifier.height(16.dp))


    }
}
