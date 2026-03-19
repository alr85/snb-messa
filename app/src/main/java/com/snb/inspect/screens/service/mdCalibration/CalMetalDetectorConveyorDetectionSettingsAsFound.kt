package com.snb.inspect.screens.service.mdCalibration

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
import com.snb.inspect.calibrationLogic.metalDetectorConveyor.autoUpdateDetectionSettingPvResult
import com.snb.inspect.calibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import com.snb.inspect.formModules.CalibrationHeader
import com.snb.inspect.formModules.LabeledDropdownWithHelp
import com.snb.inspect.formModules.LabeledTextFieldWithHelp
import com.snb.inspect.formModules.LabeledTextFieldWithHelpEdit
import com.snb.inspect.formModules.PvRule
import com.snb.inspect.formModules.PvRuleStatus
import com.snb.inspect.ui.theme.FormSpacer
import com.snb.inspect.ui.theme.ScrollableWithScrollbar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalMetalDetectorConveyorDetectionSettingsAsFound(
    viewModel: CalibrationMetalDetectorConveyorViewModel
) {
    // Pull state from ViewModel
    val sensitivityAccessRestriction by viewModel.sensitivityAccessRestriction

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

                LabeledDropdownWithHelp(
                    label = "Access Restriction:",
                    options = listOf("Yes (Password)", "Yes (Locked door)", "Yes (Key switch)", "Yes (RFID Card)", "None"),
                    selectedOption = sensitivityAccessRestriction,
                    onSelectionChange = {
                        viewModel.setSensitivityAccessRestriction(it)
                        viewModel.autoUpdateDetectionSettingPvResult()
                    },
                    pvStatus = if (viewModel.pvRequired.value) {
                        when (sensitivityAccessRestriction) {
                            "Yes (Password)", "Yes (Locked door)", "Yes (Key switch)", "Yes (RFID Card)" -> "Pass"
                            "None", "" -> "Fail"
                            else -> "Fail"
                        }
                    } else null,
                    pvRules = if (viewModel.pvRequired.value) {
                        listOf(
                            PvRule(
                                description = "Access restriction must be documented.",
                                status = if (sensitivityAccessRestriction.isNotBlank()) PvRuleStatus.Pass else PvRuleStatus.Incomplete
                            ),
                            PvRule(
                                description = "Sensitivities must be protected to meet retailer safety standards.",
                                status = when (sensitivityAccessRestriction) {
                                    "Yes (Password)", "Yes (Locked door)", "Yes (Key switch)" -> PvRuleStatus.Pass
                                    "None" -> PvRuleStatus.Fail
                                    else -> PvRuleStatus.Incomplete
                                }
                            )
                        )
                    } else emptyList(),
                    helpText = "Select the level of access restriction on the metal detector.",
                    isNAToggleEnabled = false
                )

                FormSpacer()

                // Render settings 1–8 dynamically
                labels.indices.forEach { index ->
                    val labelState = labels[index]
                    val valueState = values[index]

                    LabeledTextFieldWithHelpEdit(
                        label = labelState.value,
                        onLabelChange = { labelSetters[index](it) },
                        value = valueState.value,
                        onValueChange = { valueSetters[index](it) },
                        helpText = "Enter the detection setting value. Tap the label to rename.",
                        maxLength = 20
                    )

                    FormSpacer()

                }
                LabeledTextFieldWithHelp(
                    label = "Product Peak Signal (As Found)",
                    value = viewModel.productPeakSignalAsFound.value,
                    onValueChange = viewModel::setProductPeakSignalAsFound,
                    helpText = "Enter the product peak signal value at the 'As Found' settings",
                    isNAToggleEnabled = false,
                    maxLength = 10
                )

                FormSpacer()



                LabeledTextFieldWithHelp(
                    label = "Engineer Notes",
                    value = viewModel.detectionSettingAsFoundEngineerNotes.value,
                    onValueChange = viewModel::setDetectionSettingAsFoundEngineerNotes,
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
