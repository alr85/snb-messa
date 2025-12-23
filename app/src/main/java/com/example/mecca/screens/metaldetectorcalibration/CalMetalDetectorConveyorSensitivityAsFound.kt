package com.example.mecca.screens.metaldetectorcalibration

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.mecca.calibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import com.example.mecca.formModules.CalibrationHeader
import com.example.mecca.formModules.LabeledTextFieldWithHelp
import com.example.mecca.formModules.LabeledTwoTextInputsWithHelp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalMetalDetectorConveyorSensitivityAsFound(
    navController: NavHostController,
    viewModel: CalibrationMetalDetectorConveyorViewModel
) {
    val scrollState = rememberScrollState()

    // Pull state from ViewModel
    val productPeakSignalAsFound by viewModel.productPeakSignalAsFound

    val sensitivityAsFoundFerrous by viewModel.sensitivityAsFoundFerrous
    val sensitivityAsFoundFerrousPeakSignal by viewModel.sensitivityAsFoundFerrousPeakSignal

    val sensitivityAsFoundNonFerrous by viewModel.sensitivityAsFoundNonFerrous
    val sensitivityAsFoundNonFerrousPeakSignal by viewModel.sensitivityAsFoundNonFerrousPeakSignal

    val sensitivityAsFoundStainless by viewModel.sensitivityAsFoundStainless
    val sensitivityAsFoundStainlessPeakSignal by viewModel.sensitivityAsFoundStainlessPeakSignal

    // Validation
    val isNextStepEnabled =
        productPeakSignalAsFound.isNotBlank() &&
                sensitivityAsFoundFerrous.isNotBlank() &&
                sensitivityAsFoundFerrousPeakSignal.isNotBlank() &&
                sensitivityAsFoundNonFerrous.isNotBlank() &&
                sensitivityAsFoundNonFerrousPeakSignal.isNotBlank() &&
                sensitivityAsFoundStainless.isNotBlank() &&
                sensitivityAsFoundStainlessPeakSignal.isNotBlank()

    // Tell wrapper about Next enabled state
    LaunchedEffect(isNextStepEnabled) {
        viewModel.setCurrentScreenNextEnabled(isNextStepEnabled)
    }

    Column(modifier = Modifier.fillMaxSize()) {

        CalibrationHeader("Sensitivities (As Found)")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState)
                .imePadding()
        ) {

            LabeledTextFieldWithHelp(
                label = "Product Peak Signal",
                value = productPeakSignalAsFound,
                onValueChange = viewModel::setProductPeakSignalAsFound,
                helpText = "Enter the 'As Found' peak signal from the product alone."
            )

            LabeledTwoTextInputsWithHelp(
                label = "Ferrous (mm)",
                firstInputLabel = "Fe",
                firstInputValue = sensitivityAsFoundFerrous,
                onFirstInputValueChange = viewModel::setSensitivityAsFoundFerrous,
                secondInputLabel = "Signal L/M/T",
                secondInputValue = sensitivityAsFoundFerrousPeakSignal,
                onSecondInputValueChange = viewModel::setSensitivityAsFoundFerrousPeakSignal,
                helpText = "Enter the Ferrous sensitivity & peak signals (Leading / Middle / Trailing).",
                isNAToggleEnabled = true
            )

            LabeledTwoTextInputsWithHelp(
                label = "Non-Ferrous (mm)",
                firstInputLabel = "Non-Fe",
                firstInputValue = sensitivityAsFoundNonFerrous,
                onFirstInputValueChange = viewModel::setSensitivityAsFoundNonFerrous,
                secondInputLabel = "Signal L/M/T",
                secondInputValue = sensitivityAsFoundNonFerrousPeakSignal,
                onSecondInputValueChange = viewModel::setSensitivityAsFoundNonFerrousPeakSignal,
                helpText = "Enter the Non-Ferrous sensitivity & peak signals.",
                isNAToggleEnabled = true
            )

            LabeledTwoTextInputsWithHelp(
                label = "Stainless Steel (mm)",
                firstInputLabel = "S/Steel",
                firstInputValue = sensitivityAsFoundStainless,
                onFirstInputValueChange = viewModel::setSensitivityAsFoundStainless,
                secondInputLabel = "Signal L/M/T",
                secondInputValue = sensitivityAsFoundStainlessPeakSignal,
                onSecondInputValueChange = viewModel::setSensitivityAsFoundStainlessPeakSignal,
                helpText = "Enter the Stainless Steel sensitivity & peak signals.",
                isNAToggleEnabled = true
            )

            Spacer(Modifier.height(16.dp))

            LabeledTextFieldWithHelp(
                label = "Engineer Notes",
                value = viewModel.sensitivityAsFoundEngineerNotes.value,
                onValueChange = viewModel::setSensitivityAsFoundEngineerNotes,
                helpText = "Enter any notes relevant to this section.",
                isNAToggleEnabled = false
            )

            Spacer(Modifier.height(60.dp))
        }
    }
}
