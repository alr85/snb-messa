package com.example.mecca.screens.service.mdCalibration

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.mecca.calibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import com.example.mecca.core.InputTransforms
import com.example.mecca.formModules.CalibrationHeader
import com.example.mecca.formModules.LabeledTextFieldWithHelp
import com.example.mecca.formModules.LabeledTwoTextInputsWithHelp
import com.example.mecca.ui.theme.FormSpacer
import com.example.mecca.ui.theme.ScrollableWithScrollbar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalMetalDetectorConveyorSensitivityAsFound(
    viewModel: CalibrationMetalDetectorConveyorViewModel
) {
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

    LaunchedEffect(isNextStepEnabled) {
        viewModel.setCurrentScreenNextEnabled(isNextStepEnabled)
    }

    Column(modifier = Modifier.fillMaxSize()) {

        CalibrationHeader("Sensitivities (As Found)")

        ScrollableWithScrollbar(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
        ) {
            Column {

                LabeledTextFieldWithHelp(
                    label = "Product Peak Signal",
                    value = productPeakSignalAsFound,
                    onValueChange = viewModel::setProductPeakSignalAsFound,
                    helpText = "Enter the 'As Found' peak signal from the product alone.",
                    maxLength = 15
                )

                FormSpacer()

                LabeledTwoTextInputsWithHelp(
                    label = "Ferrous (mm)",
                    firstInputLabel = "Size",
                    firstInputValue = sensitivityAsFoundFerrous,
                    onFirstInputValueChange = viewModel::setSensitivityAsFoundFerrous,
                    secondInputLabel = "Signal L/M/T",
                    secondInputValue = sensitivityAsFoundFerrousPeakSignal,
                    onSecondInputValueChange = viewModel::setSensitivityAsFoundFerrousPeakSignal,
                    helpText = "Enter the Ferrous sensitivity & peak signals (Leading / Middle / Trailing).",
                    isNAToggleEnabled = true,
                    firstInputKeyboardType = KeyboardType.Decimal,
                    secondInputKeyboardType = KeyboardType.Text,

                    firstMaxLength = 4,
                    secondMaxLength = 25,

                    firstTransform = InputTransforms.decimal,

                )

                FormSpacer()

                LabeledTwoTextInputsWithHelp(
                    label = "Non-Ferrous (mm)",
                    firstInputLabel = "Size",
                    firstInputValue = sensitivityAsFoundNonFerrous,
                    onFirstInputValueChange = viewModel::setSensitivityAsFoundNonFerrous,
                    secondInputLabel = "Signal L/M/T",
                    secondInputValue = sensitivityAsFoundNonFerrousPeakSignal,
                    onSecondInputValueChange = viewModel::setSensitivityAsFoundNonFerrousPeakSignal,
                    helpText = "Enter the Non-Ferrous sensitivity & peak signals.",
                    isNAToggleEnabled = true,
                    firstInputKeyboardType = KeyboardType.Decimal,
                    secondInputKeyboardType = KeyboardType.Text,
                    firstMaxLength = 4,
                    secondMaxLength =20,

                    firstTransform = InputTransforms.decimal,

                )

                FormSpacer()

                LabeledTwoTextInputsWithHelp(
                    label = "Stainless Steel (mm)",
                    firstInputLabel = "Size",
                    firstInputValue = sensitivityAsFoundStainless,
                    onFirstInputValueChange = viewModel::setSensitivityAsFoundStainless,
                    secondInputLabel = "Signal L/M/T",
                    secondInputValue = sensitivityAsFoundStainlessPeakSignal,
                    onSecondInputValueChange = viewModel::setSensitivityAsFoundStainlessPeakSignal,
                    helpText = "Enter the Stainless Steel sensitivity & peak signals.",
                    isNAToggleEnabled = true,
                    firstInputKeyboardType = KeyboardType.Decimal,
                    secondInputKeyboardType = KeyboardType.Text,

                    firstMaxLength = 4,
                    secondMaxLength = 20,

                    firstTransform = InputTransforms.decimal,
                )

                FormSpacer()

                LabeledTextFieldWithHelp(
                    label = "Engineer Notes",
                    value = viewModel.sensitivityAsFoundEngineerNotes.value,
                    onValueChange = viewModel::setSensitivityAsFoundEngineerNotes,
                    helpText = "Enter any notes relevant to this section.",
                    isNAToggleEnabled = false,
                    maxLength = 50
                )

                Spacer(Modifier.height(60.dp))
            }
        }
    }
}
