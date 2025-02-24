package com.example.mecca.screens

import CalibrationBanner
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.CopyAll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.mecca.CalibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import com.example.mecca.CalibrationViewModels.CalibrationNavigationButtons
import com.example.mecca.formModules.CalibrationHeader
import com.example.mecca.formModules.LabeledTextFieldWithHelp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalMetalDetectorConveyorDetectionSettingsAsLeft(
    navController: NavHostController,
    viewModel: CalibrationMetalDetectorConveyorViewModel = viewModel()
) {
    // Stops the next button from being pressed until the screen is rendered
    LaunchedEffect(Unit) {
        viewModel.finishNavigation()
    }

    val progress = viewModel.progress

// Get and update data in the ViewModel
    val detectionSettingAsLeft1 by viewModel.detectionSettingAsLeft1
    val detectionSettingAsLeft2 by viewModel.detectionSettingAsLeft2
    val detectionSettingAsLeft3 by viewModel.detectionSettingAsLeft3
    val detectionSettingAsLeft4 by viewModel.detectionSettingAsLeft4
    val detectionSettingAsLeft5 by viewModel.detectionSettingAsLeft5
    val detectionSettingAsLeft6 by viewModel.detectionSettingAsLeft6
    val detectionSettingAsLeftEngineerNotes by viewModel.detectionSettingAsLeftEngineerNotes
    val detectionSetting1label by viewModel.detectionSetting1label
    val detectionSetting2label by viewModel.detectionSetting2label
    val detectionSetting3label by viewModel.detectionSetting3label
    val detectionSetting4label by viewModel.detectionSetting4label
    val detectionSetting5label by viewModel.detectionSetting5label
    val detectionSetting6label by viewModel.detectionSetting6label


    val isNextStepEnabled =
        detectionSettingAsLeft1.isNotBlank() &&
                detectionSettingAsLeft2.isNotBlank() &&
                detectionSettingAsLeft3.isNotBlank() &&
                detectionSettingAsLeft4.isNotBlank() &&
                detectionSettingAsLeft5.isNotBlank() &&
                detectionSettingAsLeft6.isNotBlank()


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Display the consistent banner at the top
        CalibrationBanner(
            progress = progress,
            viewModel = viewModel
        )

        // Navigation Buttons
        CalibrationNavigationButtons(
            onPreviousClick = { viewModel.updateDetectionSettingAsLeft() },
            onCancelClick = { viewModel.updateDetectionSettingAsLeft() },
            onNextClick = {
                viewModel.updateDetectionSettingAsLeft()
                navController.navigate("CalMetalDetectorConveyorRejectSettings") },
            isNextEnabled = isNextStepEnabled,
            isFirstStep = false, // Indicates this is the first step and disables the Previous button
            navController = navController,
            viewModel = viewModel,
            onSaveAndExitClick = {
                viewModel.updateDetectionSettingAsLeft()
            },
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Row for the text and copy button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CalibrationHeader("Detection Settings (As Left)")

            IconButton(
                onClick = {
                    // Copy "As Found" settings to "As Left" inputs
                    copyAsFoundToAsLeft(viewModel)
                },
                modifier = Modifier.weight(0.5f) // Make sure it takes less space to align properly
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Sharp.CopyAll,
                        contentDescription = "Copy 'As Found' settings to 'As Left'"
                    )
                    Spacer(modifier = Modifier.width(4.dp)) // Space between icon and text
                    Text(text = "Copy from 'As Found'", style = MaterialTheme.typography.labelLarge)
                }
            }
        }



        Spacer(modifier = Modifier.height(20.dp))


        LabeledTextFieldWithHelp(
            label = detectionSetting1label,
            value = detectionSettingAsLeft1,
            onValueChange = { newValue -> viewModel.setDetectionSettingAsLeft1(newValue) },
            helpText = ""
        )

        LabeledTextFieldWithHelp(
            label = detectionSetting2label,
            value = detectionSettingAsLeft2,
            onValueChange = { newValue -> viewModel.setDetectionSettingAsLeft2(newValue) },
            helpText = ""
        )

        LabeledTextFieldWithHelp(
            label = detectionSetting3label,
            value = detectionSettingAsLeft3,
            onValueChange = { newValue -> viewModel.setDetectionSettingAsLeft3(newValue) },
            helpText = ""
        )

        LabeledTextFieldWithHelp(
            label = detectionSetting4label,
            value = detectionSettingAsLeft4,
            onValueChange = { newValue -> viewModel.setDetectionSettingAsLeft4(newValue) },
            helpText = ""
        )

        LabeledTextFieldWithHelp(
            label = detectionSetting5label,
            value = detectionSettingAsLeft5,
            onValueChange = { newValue -> viewModel.setDetectionSettingAsLeft5(newValue) },
            helpText = ""
        )

        LabeledTextFieldWithHelp(
            label = detectionSetting6label,
            value = detectionSettingAsLeft6,
            onValueChange = { newValue -> viewModel.setDetectionSettingAsLeft6(newValue) },
            helpText = ""
        )

        Spacer(modifier = Modifier.height(16.dp))

        LabeledTextFieldWithHelp(
            label = "Engineer Notes",
            value = detectionSettingAsLeftEngineerNotes,
            onValueChange = { newValue -> viewModel.setDetectionSettingAsLeftEngineerNotes(newValue) },
            helpText = "Enter any notes relevant to this section",
            isNAToggleEnabled = false
        )


    }
}


fun copyAsFoundToAsLeft(viewModel: CalibrationMetalDetectorConveyorViewModel) {
    viewModel.setDetectionSettingAsLeft1(viewModel.detectionSettingAsFound1.value)
    viewModel.setDetectionSettingAsLeft2(viewModel.detectionSettingAsFound2.value)
    viewModel.setDetectionSettingAsLeft3(viewModel.detectionSettingAsFound3.value)
    viewModel.setDetectionSettingAsLeft4(viewModel.detectionSettingAsFound4.value)
    viewModel.setDetectionSettingAsLeft5(viewModel.detectionSettingAsFound5.value)
    viewModel.setDetectionSettingAsLeft6(viewModel.detectionSettingAsFound6.value)
}