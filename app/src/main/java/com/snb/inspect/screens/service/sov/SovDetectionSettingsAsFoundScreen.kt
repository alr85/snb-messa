package com.snb.inspect.screens.service.sov

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.snb.inspect.calibrationViewModels.SensitivityOptimisationValidationViewModel
import com.snb.inspect.formModules.*
import com.snb.inspect.ui.theme.FormSpacer
import com.snb.inspect.ui.theme.ScrollableWithScrollbar

@Composable
fun SovDetectionSettingsAsFoundScreen(viewModel: SensitivityOptimisationValidationViewModel) {
    val labels = listOf(
        viewModel.detectionSetting1label,
        viewModel.detectionSetting2label,
        viewModel.detectionSetting3label,
        viewModel.detectionSetting4label,
        viewModel.detectionSetting5label,
        viewModel.detectionSetting6label,
        viewModel.detectionSetting7label,
        viewModel.detectionSetting8label
    )

    val values = listOf(
        viewModel.detectionSettingAsFound1,
        viewModel.detectionSettingAsFound2,
        viewModel.detectionSettingAsFound3,
        viewModel.detectionSettingAsFound4,
        viewModel.detectionSettingAsFound5,
        viewModel.detectionSettingAsFound6,
        viewModel.detectionSettingAsFound7,
        viewModel.detectionSettingAsFound8
    )

    // Validation
    val isNextStepEnabled = labels.all { it.value.isNotBlank() } &&
            values.all { it.value.isNotBlank() } &&
            viewModel.productPeakSignalAsFound.value.isNotBlank()

    LaunchedEffect(isNextStepEnabled) {
        viewModel.setCurrentScreenNextEnabled(isNextStepEnabled)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        CalibrationHeader("Detection Settings (As Found)")

        ScrollableWithScrollbar(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp),
        ) {
            Column {
                Spacer(Modifier.height(6.dp))

                LabeledTextFieldWithHelp(
                    label = "Product Peak Signal (As Found)",
                    value = viewModel.productPeakSignalAsFound.value,
                    onValueChange = { viewModel.productPeakSignalAsFound.value = it },
                    helpText = "Enter the product peak signal value at the 'As Found' settings",
                    isNAToggleEnabled = true,
                    maxLength = 25
                )

                FormSpacer()

                labels.indices.forEach { index ->
                    LabeledTextFieldWithHelpEdit(
                        label = labels[index].value,
                        onLabelChange = { labels[index].value = it },
                        value = values[index].value,
                        onValueChange = { values[index].value = it },
                        helpText = "Enter the detection setting value. Tap the label to rename.",
                        maxLength = 20
                    )
                    FormSpacer()
                }

                LabeledTextFieldWithHelp(
                    label = "Engineer Notes",
                    value = viewModel.notesAsFoundDetectionSettings.value,
                    onValueChange = { viewModel.notesAsFoundDetectionSettings.value = it },
                    helpText = "Enter any notes relevant to this section.",
                    isNAToggleEnabled = false,
                    maxLength = 50,
                    showInputLabel = false
                )

                Spacer(Modifier.height(60.dp))
            }
        }
    }
}
