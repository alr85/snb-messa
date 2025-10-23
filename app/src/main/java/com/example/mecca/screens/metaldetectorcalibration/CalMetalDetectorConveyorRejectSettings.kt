package com.example.mecca.screens.metaldetectorcalibration

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.mecca.CalibrationBanner
import com.example.mecca.calibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import com.example.mecca.calibrationViewModels.CalibrationNavigationButtons
import com.example.mecca.formModules.CalibrationHeader
import com.example.mecca.formModules.LabeledDropdownWithTextInput
import com.example.mecca.formModules.LabeledTextFieldWithHelp
import com.example.mecca.formModules.LabeledTriStateSwitchAndTextInputWithHelp
import com.example.mecca.formModules.YesNoState

//@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalMetalDetectorConveyorRejectSettings(
    navController: NavHostController,
    viewModel: CalibrationMetalDetectorConveyorViewModel = viewModel()
) {

    // Stops the next button from being pressed until the screen is rendered
    LaunchedEffect(Unit) {
        viewModel.finishNavigation()
    }

    val progress = viewModel.progress
    val scrollState = rememberScrollState() // Scroll state to control the scroll behavior

// Get and update data in the ViewModel

    val rejectSynchronisationSetting by viewModel.rejectSynchronisationSetting
    val rejectSynchronisationDetail by viewModel.rejectSynchronisationDetail
    val rejectDelaySetting by viewModel.rejectDelaySetting
    val rejectDelayUnits by viewModel.rejectDelayUnits
    val rejectDurationSetting by viewModel.rejectDurationSetting
    val rejectDurationUnits by viewModel.rejectDurationUnits
    val rejectConfirmWindowSetting by viewModel.rejectConfirmWindowSetting
    val rejectConfirmWindowUnits by viewModel.rejectConfirmWindowUnits
    val rejectSettingsEngineerNotes by viewModel.rejectSettingsEngineerNotes

    // Test options
    val rejectTimerUnitOptions = listOf(
        "Secs",
        "mSecs",
        "pulses"
    )

    //Determine if "Next Step" button should be enabled
    val isNextStepEnabled =
        rejectDelaySetting.isNotBlank() &&
                rejectDurationSetting.isNotBlank() &&
                rejectConfirmWindowSetting.isNotBlank() &&
                rejectDelayUnits.isNotBlank() &&
                rejectDurationUnits.isNotBlank() &&
                rejectConfirmWindowUnits.isNotBlank() &&
                (
                        rejectSynchronisationSetting != YesNoState.YES || rejectSynchronisationDetail.isNotBlank()
                        )


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState) // Add scrolling to the whole column
    ) {
        CalibrationBanner(
            progress = progress,
            viewModel = viewModel

        )

        // Navigation Buttons
        CalibrationNavigationButtons(
            onPreviousClick = { viewModel.updateRejectSettings() },
            onCancelClick = { viewModel.updateRejectSettings() },
            onNextClick = {
                viewModel.updateRejectSettings()
                navController.navigate("CalMetalDetectorConveyorConveyorDetails") },
            isNextEnabled = isNextStepEnabled,
            isFirstStep = false, // Indicates this is the first step and disables the Previous button
            navController = navController,
            viewModel = viewModel,
            onSaveAndExitClick = {
                viewModel.updateRejectSettings()
            },
        )

        Spacer(modifier = Modifier.height(16.dp))

        CalibrationHeader("Reject Settings")

        Spacer(modifier = Modifier.height(20.dp))

        LabeledTriStateSwitchAndTextInputWithHelp(
            label = "Synchronisation",
            currentState = rejectSynchronisationSetting,
            onStateChange = { newState -> viewModel.setRejectSynchronisationSetting(newState) },
            helpText = "Select if there is a method of reject synchronisation",
            inputLabel = "Detail",
            inputValue = rejectSynchronisationDetail,
            onInputValueChange = { newValue -> viewModel.setRejectSynchronisationDetail(newValue) },
            //inputKeyboardType = KeyboardType.Number
        )



        LabeledDropdownWithTextInput(
            label = "Reject Duration",
            dropdownLabel = "Units",
            options = rejectTimerUnitOptions,
            selectedOption = rejectDurationUnits,
            onOptionChange = { newValue -> viewModel.setRejectDurationUnits(newValue) },
            helpText = "Select the units for the reject duration",
            inputLabel = "Duration",
            inputValue = rejectDurationSetting,
            onInputValueChange = { newValue -> viewModel.setRejectDurationSetting(newValue) },
            inputKeyboardType = KeyboardType.Number
        )

        LabeledDropdownWithTextInput(
            label = "Reject Delay",
            dropdownLabel = "Units",
            options = rejectTimerUnitOptions,
            selectedOption = rejectDelayUnits,
            onOptionChange = { newValue -> viewModel.setRejectDelayUnits(newValue) },
            helpText = "Select the units for the reject delay",
            inputLabel = "Delay",
            inputValue = rejectDelaySetting,
            onInputValueChange = { newValue -> viewModel.setRejectDelaySetting(newValue) },
            inputKeyboardType = KeyboardType.Number
        )

        LabeledDropdownWithTextInput(
            label = "Rej. Conf. Window",
            dropdownLabel = "Units",
            options = rejectTimerUnitOptions,
            selectedOption = rejectConfirmWindowUnits,
            onOptionChange = { newValue -> viewModel.setRejectConfirmWindowUnits(newValue) },
            helpText = "Select the units for the reject confirm",
            inputLabel = "Conf. Window",
            inputValue = rejectConfirmWindowSetting,
            onInputValueChange = { newValue -> viewModel.setRejectConfirmWindowSetting(newValue) },
            inputKeyboardType = KeyboardType.Number
        )


//        LabeledTextFieldWithHelp(
//            label = "Reject Delay",
//            value = rejectDelaySetting,
//            onValueChange = { newValue -> viewModel.setRejectDelaySetting(newValue) },
//            helpText = "Enter the reject delay setting",
//            keyboardType = KeyboardType.Number
//        )
//
//        LabeledTextFieldWithHelp(
//            label = "Reject Duration",
//            value = rejectDurationSetting,
//            onValueChange = { newValue -> viewModel.setRejectDurationSetting(newValue) },
//            helpText = "Enter the metal test sample certificate number, usually located on the test piece",
//        )
//
//        LabeledTextFieldWithHelp(
//            label = "Reject Confirm Window",
//            value = rejectConfirmWindowSetting,
//            onValueChange = { newValue -> viewModel.setRejectConfirmWindowSetting(newValue) },
//            helpText = "Enter the metal test sample certificate number, usually located on the test piece",
//        )

        Spacer(modifier = Modifier.height(16.dp))

        LabeledTextFieldWithHelp(
            label = "Engineer Notes",
            value = rejectSettingsEngineerNotes,
            onValueChange = { newValue -> viewModel.setRejectSettingsEngineerNotes(newValue) },
            helpText = "Enter any notes relevant to this section",
            isNAToggleEnabled = false
        )

        Spacer(modifier = Modifier.height(16.dp))

    }
}
