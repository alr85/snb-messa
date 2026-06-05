package com.snb.inspect.screens.service.sov

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.snb.inspect.calibrationViewModels.SensitivityOptimisationValidationViewModel
import com.snb.inspect.formModules.*
import com.snb.inspect.ui.theme.FormSpacer
import com.snb.inspect.ui.theme.ScrollableWithScrollbar

@Composable
fun SovFerrousTestAsFoundScreen(viewModel: SensitivityOptimisationValidationViewModel) {
    val sensitivity by viewModel.sensitivityAsFoundFerrous
    val sampleCert by viewModel.sampleCertAsFoundFerrous
    val detectLeading by viewModel.detectRejectAsFoundFerrousLeading
    val peakLeading by viewModel.peakAsFoundFerrousLeading
    val detectMiddle by viewModel.detectRejectAsFoundFerrousMiddle
    val peakMiddle by viewModel.peakAsFoundFerrousMiddle
    val detectTrailing by viewModel.detectRejectAsFoundFerrousTrailing
    val peakTrailing by viewModel.peakAsFoundFerrousTrailing
    val notes by viewModel.notesAsFoundFerrous
    
    val isConveyor = viewModel.system.systemTypeId == 1

    // Validation logic (similar to MD calibration)
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
        CalibrationHeader("Ferrous Sensitivity (As Found)")

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
                        viewModel.sensitivityAsFoundFerrous.value = it
                        if (it == "N/A") viewModel.disableFerrousAsFound() else viewModel.enableFerrousAsFound()
                    },
                    secondInputLabel = "Cert No.",
                    secondInputValue = sampleCert,
                    onSecondInputValueChange = { viewModel.sampleCertAsFoundFerrous.value = it.uppercase() },
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
                        onStateChange = { viewModel.detectRejectAsFoundFerrousLeading.value = it },
                        helpText = if (isConveyor) "Leading edge test result & signal." else "Test result & signal.",
                        inputLabel = "Produced Signal",
                        inputValue = peakLeading,
                        onInputValueChange = { viewModel.peakAsFoundFerrousLeading.value = it },
                        inputMaxLength = 12,
                    )

                    if (isConveyor) {
                        FormSpacer()
                        LabeledYesNoSegmentedSwitchAndTextInputWithHelp(
                            label = "Detected & Rejected (Middle)",
                            currentState = detectMiddle,
                            onStateChange = { viewModel.detectRejectAsFoundFerrousMiddle.value = it },
                            helpText = "Middle test result & signal.",
                            inputLabel = "Produced Signal",
                            inputValue = peakMiddle,
                            onInputValueChange = { viewModel.peakAsFoundFerrousMiddle.value = it },
                            inputMaxLength = 12,
                        )

                        FormSpacer()
                        LabeledYesNoSegmentedSwitchAndTextInputWithHelp(
                            label = "Detected & Rejected (Trailing)",
                            currentState = detectTrailing,
                            onStateChange = { viewModel.detectRejectAsFoundFerrousTrailing.value = it },
                            helpText = "Trailing-edge test result & signal.",
                            inputLabel = "Produced Signal",
                            inputValue = peakTrailing,
                            onInputValueChange = { viewModel.peakAsFoundFerrousTrailing.value = it },
                            inputMaxLength = 12,
                        )
                        FormSpacer()
                    }
                }

                LabeledTextFieldWithHelp(
                    label = "Engineer Notes",
                    value = notes,
                    onValueChange = { viewModel.notesAsFoundFerrous.value = it },
                    helpText = "Relevant notes for this section.",
                    maxLength = 50,
                )

                Spacer(Modifier.height(60.dp))
            }
        }
    }
}
