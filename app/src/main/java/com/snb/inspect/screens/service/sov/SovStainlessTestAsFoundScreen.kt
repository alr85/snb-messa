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
fun SovStainlessTestAsFoundScreen(viewModel: SensitivityOptimisationValidationViewModel) {
    val sensitivity by viewModel.sensitivityAsFoundStainless
    val sampleCert by viewModel.sampleCertAsFoundStainless
    val detectLeading by viewModel.detectRejectAsFoundStainlessLeading
    val peakLeading by viewModel.peakSignalAsFoundStainlessLeading
    val detectMiddle by viewModel.detectRejectAsFoundStainlessMiddle
    val peakMiddle by viewModel.peakSignalAsFoundStainlessMiddle
    val detectTrailing by viewModel.detectRejectAsFoundStainlessTrailing
    val peakTrailing by viewModel.peakSignalAsFoundStainlessTrailing
    val notes by viewModel.notesAsFoundStainless
    
    val isConveyor = viewModel.system.systemTypeId == 1

    // Validation logic
    val isNextStepEnabled = sensitivity.isNotBlank() &&
            sampleCert.isNotBlank() &&
            (detectLeading != YesNoState.YES || peakLeading.isNotBlank()) &&
            (!isConveyor || (
                (detectMiddle != YesNoState.YES || peakMiddle.isNotBlank()) &&
                (detectTrailing != YesNoState.YES || peakTrailing.isNotBlank())
            ))

    LaunchedEffect(isNextStepEnabled) {
        viewModel.setCurrentScreenNextEnabled(isNextStepEnabled)
    }

    Column(Modifier.fillMaxSize()) {
        CalibrationHeader("Stainless Sensitivity (As Found)")

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
                        viewModel.sensitivityAsFoundStainless.value = it
                        if (it == "N/A") viewModel.disableStainlessAsFound() else viewModel.enableStainlessAsFound()
                    },
                    secondInputLabel = "Cert No.",
                    secondInputValue = sampleCert,
                    onSecondInputValueChange = { viewModel.sampleCertAsFoundStainless.value = it.uppercase() },
                    helpText = "Enter the achieved Stainless sensitivity and the certificate number.",
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
                        onStateChange = { viewModel.detectRejectAsFoundStainlessLeading.value = it },
                        helpText = if (isConveyor) "Leading edge test result & signal." else "Test result & signal.",
                        inputLabel = "Produced Signal",
                        inputValue = peakLeading,
                        onInputValueChange = { viewModel.peakSignalAsFoundStainlessLeading.value = it },
                        inputMaxLength = 12,
                    )

                    if (isConveyor) {
                        FormSpacer()
                        LabeledYesNoSegmentedSwitchAndTextInputWithHelp(
                            label = "Detected & Rejected (Middle)",
                            currentState = detectMiddle,
                            onStateChange = { viewModel.detectRejectAsFoundStainlessMiddle.value = it },
                            helpText = "Middle test result & signal.",
                            inputLabel = "Produced Signal",
                            inputValue = peakMiddle,
                            onInputValueChange = { viewModel.peakSignalAsFoundStainlessMiddle.value = it },
                            inputMaxLength = 12,
                        )

                        FormSpacer()
                        LabeledYesNoSegmentedSwitchAndTextInputWithHelp(
                            label = "Detected & Rejected (Trailing)",
                            currentState = detectTrailing,
                            onStateChange = { viewModel.detectRejectAsFoundStainlessTrailing.value = it },
                            helpText = "Trailing-edge test result & signal.",
                            inputLabel = "Produced Signal",
                            inputValue = peakTrailing,
                            onInputValueChange = { viewModel.peakSignalAsFoundStainlessTrailing.value = it },
                            inputMaxLength = 12,
                        )
                        FormSpacer()
                    }
                }

                LabeledTextFieldWithHelp(
                    label = "Engineer Notes",
                    value = notes,
                    onValueChange = { viewModel.notesAsFoundStainless.value = it },
                    helpText = "Relevant notes for this section.",
                    maxLength = 50,
                )

                Spacer(Modifier.height(60.dp))
            }
        }
    }
}
