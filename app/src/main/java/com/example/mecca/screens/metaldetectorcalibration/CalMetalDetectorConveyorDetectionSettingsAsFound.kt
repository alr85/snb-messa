package com.example.mecca.screens.metaldetectorcalibration

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
import androidx.compose.ui.unit.dp
import com.example.mecca.calibrationLogic.metalDetectorConveyor.autoUpdateDetectionSettingPvResult
import com.example.mecca.calibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import com.example.mecca.formModules.CalibrationHeader
import com.example.mecca.formModules.LabeledFourOptionRadioWithHelp
import com.example.mecca.formModules.LabeledTextFieldWithHelp
import com.example.mecca.formModules.LabeledTextFieldWithHelpEdit
import com.example.mecca.ui.theme.FormSpacer
import com.example.mecca.ui.theme.ScrollableWithScrollbar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalMetalDetectorConveyorDetectionSettingsAsFound(
    viewModel: CalibrationMetalDetectorConveyorViewModel
) {
    // Pull state from ViewModel
    val sensitivityAccessRestriction by viewModel.sensitivityAccessRestriction
    val pvRequired by viewModel.pvRequired
    val pvResult by viewModel.detectionSettingPvResult

    // Labels and values (1–8)
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

    val labelSetters = listOf(
        viewModel::setDetectionSetting1Label,
        viewModel::setDetectionSetting2Label,
        viewModel::setDetectionSetting3Label,
        viewModel::setDetectionSetting4Label,
        viewModel::setDetectionSetting5Label,
        viewModel::setDetectionSetting6Label,
        viewModel::setDetectionSetting7Label,
        viewModel::setDetectionSetting8Label
    )

    val valueSetters = listOf(
        viewModel::setDetectionSettingAsFound1,
        viewModel::setDetectionSettingAsFound2,
        viewModel::setDetectionSettingAsFound3,
        viewModel::setDetectionSettingAsFound4,
        viewModel::setDetectionSettingAsFound5,
        viewModel::setDetectionSettingAsFound6,
        viewModel::setDetectionSettingAsFound7,
        viewModel::setDetectionSettingAsFound8
    )

    // Validation
    val isNextStepEnabled =
        labels.all { it.value.isNotBlank() } &&
                values.all { it.value.isNotBlank() } &&
                sensitivityAccessRestriction.isNotBlank()

    LaunchedEffect(isNextStepEnabled) {
        viewModel.setCurrentScreenNextEnabled(isNextStepEnabled)
    }

    Column(modifier = Modifier.fillMaxSize()) {

        CalibrationHeader("Detection Settings (As Found)")

        ScrollableWithScrollbar(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp),
        ) {
            Column {

                Spacer(Modifier.height(6.dp))

                // Render settings 1–8 dynamically
                labels.indices.forEach { index ->
                    val labelState = labels[index]
                    val valueState = values[index]

                    LabeledTextFieldWithHelpEdit(
                        label = labelState.value,
                        onLabelChange = { labelSetters[index](it) },
                        value = valueState.value,
                        onValueChange = { valueSetters[index](it) },
                        helpText = "Enter the detection setting value. Tap the label to rename."
                    )

                    FormSpacer()
                }

                LabeledTextFieldWithHelp(
                    label = "Access Restriction:",
                    value = sensitivityAccessRestriction,
                    onValueChange = {
                        viewModel.setSensitivityAccessRestriction(it)
                        viewModel.autoUpdateDetectionSettingPvResult()
                    },
                    helpText = "Eg: 'Password protected', 'Key switch', etc."
                )

                FormSpacer()

                if (pvRequired) {
                    LabeledFourOptionRadioWithHelp(
                        label = "P.V. Result",
                        showNotFittedOption = false,
                        value = pvResult,
                        onValueChange = { viewModel.setDetectionSettingPvResult(it) },
                        helpText = """
                            If Access Restriction has a value, P.V. automatically becomes Pass.
                            If Access Restriction is blank, P.V. automatically becomes Fail.
                            If Access Restriction is 'N/A', P.V. becomes N/A.
                            You may override manually if required.
                        """.trimIndent()
                    )

                    FormSpacer()
                }

                LabeledTextFieldWithHelp(
                    label = "Engineer Notes",
                    value = viewModel.detectionSettingAsFoundEngineerNotes.value,
                    onValueChange = viewModel::setDetectionSettingAsFoundEngineerNotes,
                    helpText = "Enter any notes relevant to this section.",
                    isNAToggleEnabled = false
                )

                Spacer(Modifier.height(60.dp))
            }
        }
    }
}
