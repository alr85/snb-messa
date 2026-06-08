package com.snb.inspect.screens.service.sov

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.snb.inspect.calibrationViewModels.SensitivityOptimisationValidationViewModel
import com.snb.inspect.formModules.*
import com.snb.inspect.ui.theme.FormSpacer
import com.snb.inspect.ui.theme.ScrollableWithScrollbar

@Composable
fun SovNonFerrousTestAsLeftScreen(viewModel: SensitivityOptimisationValidationViewModel) {
    val sensitivity by viewModel.sensitivityAsLeftNonFerrous
    val sampleCert by viewModel.sampleCertAsLeftNonFerrous
    val detectLeading by viewModel.detectRejectAsLeftNonFerrousLeading
    val peakLeading by viewModel.peakSignalAsLeftNonFerrousLeading
    val detectMiddle by viewModel.detectRejectAsLeftNonFerrousMiddle
    val peakMiddle by viewModel.peakSignalAsLeftNonFerrousMiddle
    val detectTrailing by viewModel.detectRejectAsLeftNonFerrousTrailing
    val peakTrailing by viewModel.peakSignalAsLeftNonFerrousTrailing
    val notes by viewModel.notesAsLeftNonFerrous
    
    val leadingSuccesses by viewModel.val2LeadingSuccesses
    val middleSuccesses by viewModel.val2MiddleSuccesses
    val trailingSuccesses by viewModel.val2TrailingSuccesses

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
        CalibrationHeader("Non-Ferrous Sensitivity (As Left)")

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
                        viewModel.sensitivityAsLeftNonFerrous.value = it
                        if (it == "N/A") viewModel.disableNonFerrousAsLeft() else viewModel.enableNonFerrousAsLeft()
                    },
                    secondInputLabel = "Cert No.",
                    secondInputValue = sampleCert,
                    onSecondInputValueChange = { viewModel.sampleCertAsLeftNonFerrous.value = it.uppercase() },
                    helpText = "Enter the achieved Non-Ferrous sensitivity and the certificate number.",
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
                        onStateChange = { viewModel.detectRejectAsLeftNonFerrousLeading.value = it },
                        helpText = if (isConveyor) "Leading edge test result & signal." else "Test result & signal.",
                        inputLabel = "Produced Signal",
                        inputValue = peakLeading,
                        onInputValueChange = { viewModel.peakSignalAsLeftNonFerrousLeading.value = it },
                        inputMaxLength = 12,
                    )

                    if (isConveyor) {
                        FormSpacer()
                        LabeledYesNoSegmentedSwitchAndTextInputWithHelp(
                            label = "Detected & Rejected (Middle)",
                            currentState = detectMiddle,
                            onStateChange = { viewModel.detectRejectAsLeftNonFerrousMiddle.value = it },
                            helpText = "Middle test result & signal.",
                            inputLabel = "Produced Signal",
                            inputValue = peakMiddle,
                            onInputValueChange = { viewModel.peakSignalAsLeftNonFerrousMiddle.value = it },
                            inputMaxLength = 12,
                        )

                        FormSpacer()
                        LabeledYesNoSegmentedSwitchAndTextInputWithHelp(
                            label = "Detected & Rejected (Trailing)",
                            currentState = detectTrailing,
                            onStateChange = { viewModel.detectRejectAsLeftNonFerrousTrailing.value = it },
                            helpText = "Trailing-edge test result & signal.",
                            inputLabel = "Produced Signal",
                            inputValue = peakTrailing,
                            onInputValueChange = { viewModel.peakSignalAsLeftNonFerrousTrailing.value = it },
                            inputMaxLength = 12,
                        )
                        FormSpacer()
                    }

                    CalibrationHeader(if (isConveyor) "30 Pass Validation (10x per edge)" else "30 Pass Validation")
                    FormSpacer()

                    ValidationSuccessInput(
                        label = if (isConveyor) "Leading Edge" else "Results",
                        successes = leadingSuccesses,
                        onSuccessesChange = { viewModel.val2LeadingSuccesses.value = it },
                        minSuccesses = if (isConveyor) 10 else 30
                    )

                    if (isConveyor) {
                        FormSpacer()
                        ValidationSuccessInput(
                            label = "Middle",
                            successes = middleSuccesses,
                            onSuccessesChange = { viewModel.val2MiddleSuccesses.value = it },
                            minSuccesses = 10
                        )

                        FormSpacer()
                        ValidationSuccessInput(
                            label = "Trailing Edge",
                            successes = trailingSuccesses,
                            onSuccessesChange = { viewModel.val2TrailingSuccesses.value = it },
                            minSuccesses = 10
                        )
                    }
                    FormSpacer()
                }

                LabeledTextFieldWithHelp(
                    label = "Engineer Notes",
                    value = notes,
                    onValueChange = { viewModel.notesAsLeftNonFerrous.value = it },
                    helpText = "Relevant notes for this section.",
                    maxLength = 50,
                )

                Spacer(Modifier.height(60.dp))
            }
        }
    }
}


