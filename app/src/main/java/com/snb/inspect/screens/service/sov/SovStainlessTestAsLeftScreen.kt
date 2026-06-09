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
fun SovStainlessTestAsLeftScreen(viewModel: SensitivityOptimisationValidationViewModel) {
    val sensitivity by viewModel.sensitivityAsLeftStainless
    val sampleCert by viewModel.sampleCertAsLeftStainless
    val minSignalLeading by viewModel.minSignalAsLeftStainlessLeading
    val minSignalMiddle by viewModel.minSignalAsLeftStainlessMiddle
    val minSignalTrailing by viewModel.minSignalAsLeftStainlessTrailing
    val notes by viewModel.notesAsLeftStainless
    
    val leadingSuccesses by viewModel.val3LeadingSuccesses
    val middleSuccesses by viewModel.val3MiddleSuccesses
    val trailingSuccesses by viewModel.val3TrailingSuccesses

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
        CalibrationHeader("Stainless Sensitivity (As Left)")

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
                        viewModel.sensitivityAsLeftStainless.value = it
                        if (it == "N/A") viewModel.disableStainlessAsLeft() else viewModel.enableStainlessAsLeft()
                    },
                    secondInputLabel = "Cert No.",
                    secondInputValue = sampleCert,
                    onSecondInputValueChange = { viewModel.sampleCertAsLeftStainless.value = it.uppercase() },
                    helpText = "Enter the achieved Stainless sensitivity and the certificate number.",
                    firstInputKeyboardType = KeyboardType.Decimal,
                    secondInputKeyboardType = KeyboardType.Text,
                    isNAToggleEnabled = true,
                    firstMaxLength = 4,
                    secondMaxLength = 12
                )

                FormSpacer()

                if (sensitivity != "N/A") {
                    CalibrationHeader(if (isConveyor) "30 Pass Validation (10x per edge)" else "30 Pass Validation")
                    FormSpacer()

                    LabeledTwoTextInputsWithHelp(
                        label = if (isConveyor) "Leading Edge Results" else "Validation Results",
                        firstInputLabel = "Successes",
                        firstInputValue = leadingSuccesses,
                        onFirstInputValueChange = { viewModel.val3LeadingSuccesses.value = it },
                        secondInputLabel = "Min Signal",
                        secondInputValue = minSignalLeading,
                        onSecondInputValueChange = { viewModel.minSignalAsLeftStainlessLeading.value = it },
                        helpText = "Enter the number of successful detections (min ${if (isConveyor) 10 else 30}) and the lowest signal observed.",
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
                            onFirstInputValueChange = { viewModel.val3MiddleSuccesses.value = it },
                            secondInputLabel = "Min Signal",
                            secondInputValue = minSignalMiddle,
                            onSecondInputValueChange = { viewModel.minSignalAsLeftStainlessMiddle.value = it },
                            helpText = "Enter the number of successful detections (min 10) and the lowest signal observed.",
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
                            onFirstInputValueChange = { viewModel.val3TrailingSuccesses.value = it },
                            secondInputLabel = "Min Signal",
                            secondInputValue = minSignalTrailing,
                            onSecondInputValueChange = { viewModel.minSignalAsLeftStainlessTrailing.value = it },
                            helpText = "Enter the number of successful detections (min 10) and the lowest signal observed.",
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
                    onValueChange = { viewModel.notesAsLeftStainless.value = it },
                    helpText = "Relevant notes for this section.",
                    maxLength = 50,
                )

                Spacer(Modifier.height(60.dp))
            }
        }
    }
}
