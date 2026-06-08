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
fun SovNonFerrousTestAsFoundScreen(viewModel: SensitivityOptimisationValidationViewModel) {
    val sensitivity by viewModel.sensitivityAsFoundNonFerrous
    val sampleCert by viewModel.sampleCertAsFoundNonFerrous
    val detectLeading by viewModel.detectRejectAsFoundNonFerrousLeading
    val peakLeading by viewModel.peakSignalAsFoundNonFerrousLeading
    val detectMiddle by viewModel.detectRejectAsFoundNonFerrousMiddle
    val peakMiddle by viewModel.peakSignalAsFoundNonFerrousMiddle
    val detectTrailing by viewModel.detectRejectAsFoundNonFerrousTrailing
    val peakTrailing by viewModel.peakSignalAsFoundNonFerrousTrailing
    val notes by viewModel.notesAsFoundNonFerrous
    
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
        CalibrationHeader("Non-Ferrous Sensitivity (As Found)")

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
                        viewModel.sensitivityAsFoundNonFerrous.value = it
                        if (it == "N/A") viewModel.disableNonFerrousAsFound() else viewModel.enableNonFerrousAsFound()
                    },
                    secondInputLabel = "Cert No.",
                    secondInputValue = sampleCert,
                    onSecondInputValueChange = { viewModel.sampleCertAsFoundNonFerrous.value = it.uppercase() },
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
                        onStateChange = { viewModel.detectRejectAsFoundNonFerrousLeading.value = it },
                        helpText = if (isConveyor) "Leading edge test result & signal." else "Test result & signal.",
                        inputLabel = "Produced Signal",
                        inputValue = peakLeading,
                        onInputValueChange = { viewModel.peakSignalAsFoundNonFerrousLeading.value = it },
                        inputMaxLength = 12,
                    )

                    if (isConveyor) {
                        FormSpacer()
                        LabeledYesNoSegmentedSwitchAndTextInputWithHelp(
                            label = "Detected & Rejected (Middle)",
                            currentState = detectMiddle,
                            onStateChange = { viewModel.detectRejectAsFoundNonFerrousMiddle.value = it },
                            helpText = "Middle test result & signal.",
                            inputLabel = "Produced Signal",
                            inputValue = peakMiddle,
                            onInputValueChange = { viewModel.peakSignalAsFoundNonFerrousMiddle.value = it },
                            inputMaxLength = 12,
                        )

                        FormSpacer()
                        LabeledYesNoSegmentedSwitchAndTextInputWithHelp(
                            label = "Detected & Rejected (Trailing)",
                            currentState = detectTrailing,
                            onStateChange = { viewModel.detectRejectAsFoundNonFerrousTrailing.value = it },
                            helpText = "Trailing-edge test result & signal.",
                            inputLabel = "Produced Signal",
                            inputValue = peakTrailing,
                            onInputValueChange = { viewModel.peakSignalAsFoundNonFerrousTrailing.value = it },
                            inputMaxLength = 12,
                        )
                        FormSpacer()
                    }
                }

                LabeledTextFieldWithHelp(
                    label = "Engineer Notes",
                    value = notes,
                    onValueChange = { viewModel.notesAsFoundNonFerrous.value = it },
                    helpText = "Relevant notes for this section.",
                    maxLength = 50,
                )

                Spacer(Modifier.height(60.dp))
            }
        }
    }
}
