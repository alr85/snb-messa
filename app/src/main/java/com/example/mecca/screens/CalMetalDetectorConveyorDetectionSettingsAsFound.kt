package com.example.mecca.screens

import CalibrationBanner
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.mecca.CalibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import com.example.mecca.CalibrationViewModels.CalibrationNavigationButtons
import com.example.mecca.formModules.CalibrationHeader
import com.example.mecca.formModules.LabeledTextFieldWithHelp
import com.example.mecca.formModules.LabeledTextFieldWithHelpEdit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalMetalDetectorConveyorDetectionSettingsAsFound(
    navController: NavHostController,
    viewModel: CalibrationMetalDetectorConveyorViewModel = viewModel()
) {
    // Stops the next button from being pressed until the screen is rendered
    LaunchedEffect(Unit) {
        viewModel.finishNavigation()
    }

    val progress = viewModel.progress
    val scrollState = rememberScrollState()

// Get and update data in the ViewModel
    val detectionSettingAsFound1 by viewModel.detectionSettingAsFound1
    val detectionSettingAsFound2 by viewModel.detectionSettingAsFound2
    val detectionSettingAsFound3 by viewModel.detectionSettingAsFound3
    val detectionSettingAsFound4 by viewModel.detectionSettingAsFound4
    val detectionSettingAsFound5 by viewModel.detectionSettingAsFound5
    val detectionSettingAsFound6 by viewModel.detectionSettingAsFound6
    val detectionSettingAsFound7 by viewModel.detectionSettingAsFound7
    val detectionSettingAsFound8 by viewModel.detectionSettingAsFound8
    val sensitivityAccessRestriction by viewModel.sensitivityAccessRestriction
    val detectionSetting1label by viewModel.detectionSetting1label
    val detectionSetting2label by viewModel.detectionSetting2label
    val detectionSetting3label by viewModel.detectionSetting3label
    val detectionSetting4label by viewModel.detectionSetting4label
    val detectionSetting5label by viewModel.detectionSetting5label
    val detectionSetting6label by viewModel.detectionSetting6label
    val detectionSetting7label by viewModel.detectionSetting7label
    val detectionSetting8label by viewModel.detectionSetting8label


    //Determine if "Next Step" button should be enabled
    val isNextStepEnabled =
        detectionSettingAsFound1.isNotBlank() &&
                detectionSettingAsFound2.isNotBlank() &&
                detectionSettingAsFound3.isNotBlank() &&
                detectionSettingAsFound4.isNotBlank() &&
                detectionSettingAsFound5.isNotBlank() &&
                detectionSettingAsFound6.isNotBlank() &&
                detectionSettingAsFound7.isNotBlank() &&
                detectionSettingAsFound8.isNotBlank() &&
                sensitivityAccessRestriction.isNotBlank()


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        // Display the consistent banner at the top
        CalibrationBanner(
            progress = progress,
            viewModel = viewModel
        )

        // Navigation Buttons
        CalibrationNavigationButtons(
            onPreviousClick = { viewModel.updateDetectionSettingsAsFound()
                              viewModel.updateDetectionSettingLabels()},
            onCancelClick = { viewModel.updateDetectionSettingsAsFound()
                             viewModel.updateDetectionSettingLabels()},
            onNextClick = {
                viewModel.updateDetectionSettingLabels()
                viewModel.updateDetectionSettingsAsFound()
                navController.navigate("CalMetalDetectorConveyorSensitivityAsFound")},
            isNextEnabled = isNextStepEnabled,
            isFirstStep = false, // Indicates this is the first step and disables the Previous button
            navController = navController,
            viewModel = viewModel,
            onSaveAndExitClick = {
                //viewModel.saveCalibrationData() // Custom save logic here
            },
        )

        Spacer(modifier = Modifier.height(16.dp))

        CalibrationHeader("Detection Settings (As Found)")

        Spacer(modifier = Modifier.height(20.dp))

        LabeledTextFieldWithHelp(
            label = "Access Restriction:",
            value = sensitivityAccessRestriction,
            onValueChange = { newValue -> viewModel.setSensitivityAccessRestriction(newValue) },
            helpText = "Enter details about how the sensitivity settings are restricted e.g. 'Password Protected'",
        )

        LabeledTextFieldWithHelpEdit(
            label = detectionSetting1label,
            onLabelChange = { newLabel -> viewModel.setDetectionSetting1Label(newLabel) },
            value = detectionSettingAsFound1,
            onValueChange = { newValue -> viewModel.setDetectionSettingAsFound1(newValue) },
            helpText = "Enter the value of this detection setting. To change the label, click on the label text."
        )

        LabeledTextFieldWithHelpEdit(
            label = detectionSetting2label,
            onLabelChange = { newLabel -> viewModel.setDetectionSetting2Label(newLabel) },
            value = detectionSettingAsFound2,
            onValueChange = { newValue -> viewModel.setDetectionSettingAsFound2(newValue) },
            helpText = "Enter the value of this detection setting. To change the label, click on the label text."
        )

        LabeledTextFieldWithHelpEdit(
            label = detectionSetting3label,
            onLabelChange = { newLabel -> viewModel.setDetectionSetting3Label(newLabel) },
            value = detectionSettingAsFound3,
            onValueChange = { newValue -> viewModel.setDetectionSettingAsFound3(newValue) },
            helpText = "Enter the value of this detection setting. To change the label, click on the label text."
        )

        LabeledTextFieldWithHelpEdit(
            label = detectionSetting4label,
            onLabelChange = { newLabel -> viewModel.setDetectionSetting4Label(newLabel) },
            value = detectionSettingAsFound4,
            onValueChange = { newValue -> viewModel.setDetectionSettingAsFound4(newValue) },
            helpText = "Enter the value of this detection setting. To change the label, click on the label text."
        )

        LabeledTextFieldWithHelpEdit(
            label = detectionSetting5label,
            onLabelChange = { newLabel -> viewModel.setDetectionSetting5Label(newLabel) },
            value = detectionSettingAsFound5,
            onValueChange = { newValue -> viewModel.setDetectionSettingAsFound5(newValue) },
            helpText = "Enter the value of this detection setting. To change the label, click on the label text."
        )

        LabeledTextFieldWithHelpEdit(
            label = detectionSetting6label,
            onLabelChange = { newLabel -> viewModel.setDetectionSetting6Label(newLabel) },
            value = detectionSettingAsFound6,
            onValueChange = { newValue -> viewModel.setDetectionSettingAsFound6(newValue) },
            helpText = "Enter the value of this detection setting. To change the label, click on the label text."
        )

        LabeledTextFieldWithHelpEdit(
            label = detectionSetting7label,
            onLabelChange = { newLabel -> viewModel.setDetectionSetting7Label(newLabel) },
            value = detectionSettingAsFound7,
            onValueChange = { newValue -> viewModel.setDetectionSettingAsFound7(newValue) },
            helpText = "Enter the value of this detection setting. To change the label, click on the label text."
        )

        LabeledTextFieldWithHelpEdit(
            label = detectionSetting8label,
            onLabelChange = { newLabel -> viewModel.setDetectionSetting8Label(newLabel) },
            value = detectionSettingAsFound8,
            onValueChange = { newValue -> viewModel.setDetectionSettingAsFound8(newValue) },
            helpText = "Enter the value of this detection setting. To change the label, click on the label text."
        )



        Spacer(modifier = Modifier.height(16.dp))

        LabeledTextFieldWithHelp(
            label = "Engineer Notes",
            value = viewModel.detectionSettingAsFoundEngineerNotes.value,
            onValueChange = { newValue -> viewModel.setDetectionSettingAsFoundEngineerNotes(newValue) },
            helpText = "Enter any notes relevant to this section",
            isNAToggleEnabled = false
        )


    }
}
