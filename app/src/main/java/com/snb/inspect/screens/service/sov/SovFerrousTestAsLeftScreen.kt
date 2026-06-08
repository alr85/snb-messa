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
    val detectLeading by viewModel.detectRejectAsLeftFerrousLeading
    val peakLeading by viewModel.peakSignalAsLeftFerrousLeading
    val detectMiddle by viewModel.detectRejectAsLeftFerrousMiddle
    val peakMiddle by viewModel.peakSignalAsLeftFerrousMiddle
    val detectTrailing by viewModel.detectRejectAsLeftFerrousTrailing
    val peakTrailing by viewModel.peakSignalAsLeftFerrousTrailing
    val notes by viewModel.notesAsLeftFerrous
    
    val leadingSuccesses by viewModel.val1LeadingSuccesses
    val middleSuccesses by viewModel.val1MiddleSuccesses
    val trailingSuccesses by viewModel.val1TrailingSuccesses

    val isConveyor = viewModel.system.systemTypeId == 1

    // Validation logic
    val isNextStepEnabled = sensitivity.isNotBlank() &&
            sampleCert.isNotBlank() &&
            (sensitivity == "N/A" || (
                (detectLeading != YesNoState.YES || peakLeading.isNotBlank()) &&
                (!isConveyor || (
                    (detectMiddle != YesNoState.YES || peakMiddle.isNotBlank()) &&
                    (detectTrailing != YesNoState.YES || peakTrailing.isNotBlank())
                )) &&
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
        CalibrationHeader("Ferrous Sensitivity (As Left)")

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
                    LabeledYesNoSegmentedSwitchAndTextInputWithHelp(
                        label = if (isConveyor) "Detected & Rejected (Leading)" else "Detected & Rejected",
                        currentState = detectLeading,
                        onStateChange = { viewModel.detectRejectAsLeftFerrousLeading.value = it },
                        helpText = if (isConveyor) "Leading edge test result & signal." else "Test result & signal.",
                        inputLabel = "Produced Signal",
                        inputValue = peakLeading,
                        onInputValueChange = { viewModel.peakSignalAsLeftFerrousLeading.value = it },
                        inputMaxLength = 12,
                    )

                    if (isConveyor) {
                        FormSpacer()
                        LabeledYesNoSegmentedSwitchAndTextInputWithHelp(
                            label = "Detected & Rejected (Middle)",
                            currentState = detectMiddle,
                            onStateChange = { viewModel.detectRejectAsLeftFerrousMiddle.value = it },
                            helpText = "Middle test result & signal.",
                            inputLabel = "Produced Signal",
                            inputValue = peakMiddle,
                            onInputValueChange = { viewModel.peakSignalAsLeftFerrousMiddle.value = it },
                            inputMaxLength = 12,
                        )

                        FormSpacer()
                        LabeledYesNoSegmentedSwitchAndTextInputWithHelp(
                            label = "Detected & Rejected (Trailing)",
                            currentState = detectTrailing,
                            onStateChange = { viewModel.detectRejectAsLeftFerrousTrailing.value = it },
                            helpText = "Trailing-edge test result & signal.",
                            inputLabel = "Produced Signal",
                            inputValue = peakTrailing,
                            onInputValueChange = { viewModel.peakSignalAsLeftFerrousTrailing.value = it },
                            inputMaxLength = 12,
                        )
                        FormSpacer()
                    }

                    CalibrationHeader(if (isConveyor) "30 Pass Validation (10x per edge)" else "30 Pass Validation")
                    FormSpacer()

                    ValidationSuccessInput(
                        label = if (isConveyor) "Leading Edge" else "Results",
                        successes = leadingSuccesses,
                        onSuccessesChange = { viewModel.val1LeadingSuccesses.value = it },
                        minSuccesses = if (isConveyor) 10 else 30
                    )

                    if (isConveyor) {
                        FormSpacer()
                        ValidationSuccessInput(
                            label = "Middle",
                            successes = middleSuccesses,
                            onSuccessesChange = { viewModel.val1MiddleSuccesses.value = it },
                            minSuccesses = 10
                        )

                        FormSpacer()
                        ValidationSuccessInput(
                            label = "Trailing Edge",
                            successes = trailingSuccesses,
                            onSuccessesChange = { viewModel.val1TrailingSuccesses.value = it },
                            minSuccesses = 10
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
                )

                Spacer(Modifier.height(60.dp))
            }
        }
    }
}

