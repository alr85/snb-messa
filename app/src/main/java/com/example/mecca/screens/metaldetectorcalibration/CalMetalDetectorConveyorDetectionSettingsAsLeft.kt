package com.example.mecca.screens.metaldetectorcalibration

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentPasteGo
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.mecca.calibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import com.example.mecca.formModules.AnimatedActionPill
import com.example.mecca.formModules.CalibrationHeader
import com.example.mecca.formModules.LabeledTextFieldWithHelp
import com.example.mecca.ui.theme.ScrollableWithScrollbar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalMetalDetectorConveyorDetectionSettingsAsLeft(
    navController: NavHostController,
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

    // Validation
    val isNextStepEnabled = asLeftStates.all { it.value.isNotBlank() }

    LaunchedEffect(isNextStepEnabled) {
        viewModel.setCurrentScreenNextEnabled(isNextStepEnabled)
    }

    Column(Modifier.fillMaxSize()) {

        CalibrationHeader("Detection Settings (As Left)")

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

                Spacer(Modifier.height(20.dp))

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
                        helpText = ""
                    )

                    Spacer(Modifier.height(16.dp))
                }

                LabeledTextFieldWithHelp(
                    label = "Engineer Notes",
                    value = engineerNotes,
                    onValueChange = viewModel::setDetectionSettingAsLeftEngineerNotes,
                    helpText = "Enter any notes relevant to this section",
                    isNAToggleEnabled = false
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
}


