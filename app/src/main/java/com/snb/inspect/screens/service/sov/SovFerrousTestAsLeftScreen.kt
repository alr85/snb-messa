package com.snb.inspect.screens.service.sov

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.snb.inspect.calibrationViewModels.SensitivityOptimisationValidationViewModel
import com.snb.inspect.formModules.*
import com.snb.inspect.ui.theme.FormSpacer
import com.snb.inspect.ui.theme.ScrollableWithScrollbar

@Composable
fun SovFerrousTestAsLeftScreen(viewModel: SensitivityOptimisationValidationViewModel) {
    val sensitivity by viewModel.sensitivityAsLeftFerrous
    val sampleCert by viewModel.sampleCertAsLeftFerrous
    val minSignalLeading by viewModel.minSignalAsLeftFerrousLeading
    val minSignalMiddle by viewModel.minSignalAsLeftFerrousMiddle
    val minSignalTrailing by viewModel.minSignalAsLeftFerrousTrailing
    val notes by viewModel.notesAsLeftFerrous
    
    val leadingSuccesses by viewModel.val1LeadingSuccesses
    val middleSuccesses by viewModel.val1MiddleSuccesses
    val trailingSuccesses by viewModel.val1TrailingSuccesses

    val isConveyor = viewModel.system.systemTypeId == 1

    // Validation logic
    val isNextStepEnabled = sensitivity.isNotBlank() &&
            sampleCert.isNotBlank() &&
            (sensitivity == "N/A" || (
                minSignalLeading.isNotBlank() &&
                (!isConveyor || (minSignalMiddle.isNotBlank() && minSignalTrailing.isNotBlank())) &&
                // 30 Pass Validation
                (leadingSuccesses.toIntOrNull() ?: 0) >= (if (isConveyor) 10 else 30) &&
                (!isConveyor || (
                    (middleSuccesses.toIntOrNull() ?: 0) >= 10 &&
                    (trailingSuccesses.toIntOrNull() ?: 0) >= 10
                ))
            ))

    LaunchedEffect(isNextStepEnabled) {
        viewModel.setCurrentScreenNextEnabled(isNextStepEnabled)
    }

    Column(Modifier.fillMaxSize()) {
        CalibrationHeader(label = "Optimised Ferrous Sensitivity", showStatusIcon = false)

        ScrollableWithScrollbar(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp),
        ) {
            Column {
                Spacer(Modifier.height(6.dp))

                LabeledTwoTextInputsWithHelp(
                    label = "Achieved Sensitivity & Certificate",
                    firstInputLabel = "Size",
                    firstInputValue = sensitivity,
                    onFirstInputValueChange = {
                        viewModel.sensitivityAsLeftFerrous.value = it
                        if (it == "N/A") viewModel.disableFerrousAsLeft() else viewModel.enableFerrousAsLeft()
                    },
                    secondInputLabel = "Cert No.",
                    secondInputValue = sampleCert,
                    onSecondInputValueChange = { viewModel.sampleCertAsLeftFerrous.value = it.uppercase() },
                    helpText = "Enter the achieved Ferrous sensitivity and the certificate number.",
                    firstInputKeyboardType = KeyboardType.Decimal,
                    secondInputKeyboardType = KeyboardType.Text,
                    isNAToggleEnabled = true,
                    firstMaxLength = 4,
                    secondMaxLength = 12
                )

                FormSpacer()

                if (sensitivity != "N/A") {
                    CalibrationHeader(
                        label = if (isConveyor) "30 Pass Validation (10x per edge)" else "30 Pass Validation",
                        showStatusIcon = false
                    )
                    FormSpacer()

                    LabeledTwoTextInputsWithHelp(
                        label = if (isConveyor) "Leading Edge Results" else "Validation Results",
                        firstInputLabel = "Successes",
                        firstInputValue = leadingSuccesses,
                        onFirstInputValueChange = { viewModel.val1LeadingSuccesses.value = it },
                        secondInputLabel = "Min Signal",
                        secondInputValue = minSignalLeading,
                        onSecondInputValueChange = { viewModel.minSignalAsLeftFerrousLeading.value = it },
                        helpText = "Enter the number of successful detections and rejections (min ${if (isConveyor) 10 else 30}) and the lowest signal observed.",
                        firstInputKeyboardType = KeyboardType.Number,
                        secondInputKeyboardType = KeyboardType.Text,
                        isNAToggleEnabled = false,
                        firstMaxLength = 2,
                        secondMaxLength = 12
                    )

                    if (isConveyor) {
                        FormSpacer()
                        LabeledTwoTextInputsWithHelp(
                            label = "Middle Results",
                            firstInputLabel = "Successes",
                            firstInputValue = middleSuccesses,
                            onFirstInputValueChange = { viewModel.val1MiddleSuccesses.value = it },
                            secondInputLabel = "Min Signal",
                            secondInputValue = minSignalMiddle,
                            onSecondInputValueChange = { viewModel.minSignalAsLeftFerrousMiddle.value = it },
                            helpText = "Enter the number of successful detections and rejections (min 10) and the lowest signal observed.",
                            firstInputKeyboardType = KeyboardType.Number,
                            secondInputKeyboardType = KeyboardType.Text,
                            isNAToggleEnabled = false,
                            firstMaxLength = 2,
                            secondMaxLength = 12
                        )

                        FormSpacer()
                        LabeledTwoTextInputsWithHelp(
                            label = "Trailing Edge Results",
                            firstInputLabel = "Successes",
                            firstInputValue = trailingSuccesses,
                            onFirstInputValueChange = { viewModel.val1TrailingSuccesses.value = it },
                            secondInputLabel = "Min Signal",
                            secondInputValue = minSignalTrailing,
                            onSecondInputValueChange = { viewModel.minSignalAsLeftFerrousTrailing.value = it },
                            helpText = "Enter the number of successful detections and rejections (min 10) and the lowest signal observed.",
                            firstInputKeyboardType = KeyboardType.Number,
                            secondInputKeyboardType = KeyboardType.Text,
                            isNAToggleEnabled = false,
                            firstMaxLength = 2,
                            secondMaxLength = 12
                        )
                    }
                    FormSpacer()
                }

                LabeledTextFieldWithHelp(
                    label = "Engineer Notes",
                    value = notes,
                    onValueChange = { viewModel.notesAsLeftFerrous.value = it },
                    helpText = "Relevant notes for this section.",
                    maxLength = 50,
                    singleLine = false
                )

                Spacer(Modifier.height(60.dp))
            }
        }
    }
}

