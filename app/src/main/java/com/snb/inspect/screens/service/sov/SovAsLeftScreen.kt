package com.snb.inspect.screens.service.sov

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.snb.inspect.calibrationViewModels.SensitivityOptimisationValidationViewModel
import com.snb.inspect.formModules.CalibrationHeader
import com.snb.inspect.formModules.LabeledTextFieldWithHelp
import com.snb.inspect.formModules.LabeledTextFieldWithHelpEdit
import com.snb.inspect.ui.theme.FormSpacer
import com.snb.inspect.ui.theme.ScrollableWithScrollbar

@Composable
fun SovAsLeftScreen(viewModel: SensitivityOptimisationValidationViewModel) {
    val isXray = viewModel.system.systemType.contains("X-ray", ignoreCase = true)

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
        viewModel.detectionSettingAsLeft1,
        viewModel.detectionSettingAsLeft2,
        viewModel.detectionSettingAsLeft3,
        viewModel.detectionSettingAsLeft4,
        viewModel.detectionSettingAsLeft5,
        viewModel.detectionSettingAsLeft6,
        viewModel.detectionSettingAsLeft7,
        viewModel.detectionSettingAsLeft8
    )

    ScrollableWithScrollbar(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(Modifier.height(16.dp))
            CalibrationHeader("As Left Sensitivities")
            Spacer(Modifier.height(16.dp))

            if (!isXray) {
                LabeledTextFieldWithHelp(
                    label = "Ferrous (mm)",
                    value = viewModel.sensitivityAsLeftFerrous.value,
                    onValueChange = { viewModel.sensitivityAsLeftFerrous.value = it },
                    helpText = "As left ferrous sensitivity."
                )
                FormSpacer()
                LabeledTextFieldWithHelp(
                    label = "Non-Ferrous (mm)",
                    value = viewModel.sensitivityAsLeftNonFerrous.value,
                    onValueChange = { viewModel.sensitivityAsLeftNonFerrous.value = it },
                    helpText = "As left non-ferrous sensitivity."
                )
                FormSpacer()
            }
            
            LabeledTextFieldWithHelp(
                label = "Stainless Steel (mm)",
                value = viewModel.sensitivityAsLeftStainless.value,
                onValueChange = { viewModel.sensitivityAsLeftStainless.value = it },
                helpText = "As left stainless steel sensitivity."
            )
            FormSpacer()

            Spacer(Modifier.height(16.dp))
            CalibrationHeader("As Left Detection Settings")
            Spacer(Modifier.height(16.dp))

            labels.indices.forEach { index ->
                val labelState = labels[index]
                val valueState = values[index]

                LabeledTextFieldWithHelpEdit(
                    label = labelState.value,
                    onLabelChange = { labelState.value = it },
                    value = valueState.value,
                    onValueChange = { valueState.value = it },
                    helpText = "Enter the detection setting value. Tap the label to rename.",
                    maxLength = 20
                )
                FormSpacer()
            }

            Spacer(Modifier.height(60.dp))
        }
    }
}
