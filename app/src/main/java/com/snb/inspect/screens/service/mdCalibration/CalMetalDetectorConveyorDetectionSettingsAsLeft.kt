package com.snb.inspect.screens.service.mdCalibration

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentPasteGo
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.snb.inspect.calibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import com.snb.inspect.formModules.AnimatedActionPill
import com.snb.inspect.formModules.CalibrationHeader
import com.snb.inspect.formModules.LabeledTextFieldWithHelp
import com.snb.inspect.ui.theme.FormSpacer
import com.snb.inspect.ui.theme.ScrollableWithScrollbar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalMetalDetectorConveyorDetectionSettingsAsLeft(
    viewModel: CalibrationMetalDetectorConveyorViewModel
) {
    // Keep these as State<String> so UI updates properly
    val asLeftStates = listOf(
        viewModel.detectionSettingAsLeft1,
        viewModel.detectionSettingAsLeft2,
        viewModel.detectionSettingAsLeft3,
        viewModel.detectionSettingAsLeft4,
        viewModel.detectionSettingAsLeft5,
        viewModel.detectionSettingAsLeft6,
        viewModel.detectionSettingAsLeft7,
        viewModel.detectionSettingAsLeft8
    )

    val labelStates = listOf(
        viewModel.detectionSetting1label,
        viewModel.detectionSetting2label,
        viewModel.detectionSetting3label,
        viewModel.detectionSetting4label,
        viewModel.detectionSetting5label,
        viewModel.detectionSetting6label,
        viewModel.detectionSetting7label,
        viewModel.detectionSetting8label
    )

    val engineerNotes by viewModel.detectionSettingAsLeftEngineerNotes

    val valueSetters = listOf(
        viewModel::setDetectionSettingAsLeft1,
        viewModel::setDetectionSettingAsLeft2,
        viewModel::setDetectionSettingAsLeft3,
        viewModel::setDetectionSettingAsLeft4,
        viewModel::setDetectionSettingAsLeft5,
        viewModel::setDetectionSettingAsLeft6,
        viewModel::setDetectionSettingAsLeft7,
        viewModel::setDetectionSettingAsLeft8
    )

    // Validation: All rows must have a valid label/value pair, OR be set to N/A
    val isNextStepEnabled = asLeftStates.indices.all { i ->
        val v = asLeftStates[i].value
        val l = labelStates[i].value
        v == "N/A" || (l.isNotBlank() && v.isNotBlank())
    } && viewModel.productPeakSignalAsLeft.value.isNotBlank()

    LaunchedEffect(isNextStepEnabled) {
        viewModel.setCurrentScreenNextEnabled(isNextStepEnabled)
    }

    // Auto-set N/A if label or value indicates an unused or invalid setting
    LaunchedEffect(Unit) {
        labelStates.indices.forEach { index ->
            val label = labelStates[index].value
            val value = asLeftStates[index].value

            if (label.isBlank() || label == "-" || label.lowercase() == "null") {
                // If label is missing, dash, or "null", N/A the module value but leave label as is
                valueSetters[index]("N/A")
            } else if (value == "-" || value.lowercase() == "null") {
                // If label is valid but value is dash or "null", N/A the value
                valueSetters[index]("N/A")
            }
        }
    }

    Column(Modifier.fillMaxSize()) {

        CalibrationHeader(
            label = "Detection Settings (As Left)",
            isValid = isNextStepEnabled
        )

        ScrollableWithScrollbar(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
        ) {
            Column {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    AnimatedActionPill(
                        text = "Copy from ‘As Found’",
                        icon = Icons.Outlined.ContentPasteGo,
                        onClick = { copyAsFoundToAsLeft(viewModel) }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LabeledTextFieldWithHelp(
                    label = "Product Peak Signal (As Left)",
                    value = viewModel.productPeakSignalAsLeft.value,
                    onValueChange = viewModel::setProductPeakSignalAsLeft,
                    helpText = "Enter the product peak signal value at the 'As Left' settings",
                    isNAToggleEnabled = true,
                    maxLength = 25
                )

                FormSpacer()

                // Detection setting fields
                labelStates.indices.forEach { index ->
                    val label = labelStates[index].value
                    val value = asLeftStates[index].value

                    LabeledTextFieldWithHelp(
                        label = label,
                        value = value,
                        onValueChange = { newValue ->
                            when (index) {
                                0 -> viewModel.setDetectionSettingAsLeft1(newValue)
                                1 -> viewModel.setDetectionSettingAsLeft2(newValue)
                                2 -> viewModel.setDetectionSettingAsLeft3(newValue)
                                3 -> viewModel.setDetectionSettingAsLeft4(newValue)
                                4 -> viewModel.setDetectionSettingAsLeft5(newValue)
                                5 -> viewModel.setDetectionSettingAsLeft6(newValue)
                                6 -> viewModel.setDetectionSettingAsLeft7(newValue)
                                7 -> viewModel.setDetectionSettingAsLeft8(newValue)
                            }
                        },
                        helpText = "",
                        maxLength = 20
                    )

                    FormSpacer()
                }




                LabeledTextFieldWithHelp(
                    label = "Engineer Notes",
                    value = engineerNotes,
                    onValueChange = viewModel::setDetectionSettingAsLeftEngineerNotes,
                    helpText = "Enter any notes relevant to this section",
                    isNAToggleEnabled = false,
                    maxLength = 50
                )

                Spacer(Modifier.height(60.dp))
            }
        }
    }
}

fun copyAsFoundToAsLeft(viewModel: CalibrationMetalDetectorConveyorViewModel) {
    viewModel.setDetectionSettingAsLeft1(viewModel.detectionSettingAsFound1.value)
    viewModel.setDetectionSettingAsLeft2(viewModel.detectionSettingAsFound2.value)
    viewModel.setDetectionSettingAsLeft3(viewModel.detectionSettingAsFound3.value)
    viewModel.setDetectionSettingAsLeft4(viewModel.detectionSettingAsFound4.value)
    viewModel.setDetectionSettingAsLeft5(viewModel.detectionSettingAsFound5.value)
    viewModel.setDetectionSettingAsLeft6(viewModel.detectionSettingAsFound6.value)
    viewModel.setDetectionSettingAsLeft7(viewModel.detectionSettingAsFound7.value)
    viewModel.setDetectionSettingAsLeft8(viewModel.detectionSettingAsFound8.value)
    viewModel.setProductPeakSignalAsLeft(viewModel.productPeakSignalAsFound.value)
    viewModel.setDetectionSettingAsLeftEngineerNotes(viewModel.detectionSettingAsFoundEngineerNotes.value)
}




