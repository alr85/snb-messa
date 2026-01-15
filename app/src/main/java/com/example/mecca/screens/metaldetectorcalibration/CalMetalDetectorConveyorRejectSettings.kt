package com.example.mecca.screens.metaldetectorcalibration

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.mecca.calibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import com.example.mecca.formModules.CalibrationHeader
import com.example.mecca.formModules.LabeledDropdownWithTextInput
import com.example.mecca.formModules.LabeledTextFieldWithHelp
import com.example.mecca.formModules.LabeledTriStateSwitchAndTextInputWithHelp
import com.example.mecca.formModules.LabeledYesNoSegmentedSwitchAndTextInputWithHelp
import com.example.mecca.formModules.YesNoState
import com.example.mecca.ui.theme.FormSpacer
import com.example.mecca.ui.theme.ScrollableWithScrollbar

@Composable
fun CalMetalDetectorConveyorRejectSettings(
    navController: NavHostController,
    viewModel: CalibrationMetalDetectorConveyorViewModel
) {

    val rejectSynchronisationSetting by viewModel.rejectSynchronisationSetting
    val rejectSynchronisationDetail by viewModel.rejectSynchronisationDetail

    val rejectDelaySetting by viewModel.rejectDelaySetting
    val rejectDelayUnits by viewModel.rejectDelayUnits

    val rejectDurationSetting by viewModel.rejectDurationSetting
    val rejectDurationUnits by viewModel.rejectDurationUnits

    val rejectConfirmWindowSetting by viewModel.rejectConfirmWindowSetting
    val rejectConfirmWindowUnits by viewModel.rejectConfirmWindowUnits

    val rejectSettingsEngineerNotes by viewModel.rejectSettingsEngineerNotes

    // Don’t rebuild this list every time Compose sneezes
    val rejectTimerUnitOptions = remember {
        listOf("Secs", "mSecs", "mm", "pulses")
    }

    // Next enabled
    val isNextStepEnabled =
        rejectDelaySetting.isNotBlank() &&
                rejectDurationSetting.isNotBlank() &&
                rejectConfirmWindowSetting.isNotBlank() &&
                rejectDelayUnits.isNotBlank() &&
                rejectDurationUnits.isNotBlank() &&
                rejectConfirmWindowUnits.isNotBlank() &&
                (rejectSynchronisationSetting != YesNoState.YES || rejectSynchronisationDetail.isNotBlank())

    // Tell wrapper
    LaunchedEffect(isNextStepEnabled) {
        viewModel.setCurrentScreenNextEnabled(isNextStepEnabled)
    }

    Column(modifier = Modifier.fillMaxSize()) {

        CalibrationHeader("Reject Settings")

        ScrollableWithScrollbar(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
        ) {

            Column {

                LabeledYesNoSegmentedSwitchAndTextInputWithHelp(
                    label = "Synchronisation",
                    currentState = rejectSynchronisationSetting,
                    onStateChange = viewModel::setRejectSynchronisationSetting,
                    helpText = "Select if there is a method of reject synchronisation.",
                    inputLabel = "Detail",
                    inputValue = rejectSynchronisationDetail,
                    onInputValueChange = viewModel::setRejectSynchronisationDetail
                )

                FormSpacer()

                LabeledDropdownWithTextInput(
                    label = "Reject Duration",
                    dropdownLabel = "Units",
                    options = rejectTimerUnitOptions,
                    selectedOption = rejectDurationUnits,
                    onOptionChange = viewModel::setRejectDurationUnits,
                    helpText = "Select the units for the reject duration.",
                    inputLabel = "Duration",
                    inputValue = rejectDurationSetting,
                    onInputValueChange = viewModel::setRejectDurationSetting,
                    inputKeyboardType = KeyboardType.Number
                )

                FormSpacer()

                LabeledDropdownWithTextInput(
                    label = "Reject Delay",
                    dropdownLabel = "Units",
                    options = rejectTimerUnitOptions,
                    selectedOption = rejectDelayUnits,
                    onOptionChange = viewModel::setRejectDelayUnits,
                    helpText = "Select the units for the reject delay.",
                    inputLabel = "Delay",
                    inputValue = rejectDelaySetting,
                    onInputValueChange = viewModel::setRejectDelaySetting,
                    inputKeyboardType = KeyboardType.Number
                )

                FormSpacer()

                LabeledDropdownWithTextInput(
                    label = "Reject Confirm Window",
                    dropdownLabel = "Units",
                    options = rejectTimerUnitOptions,
                    selectedOption = rejectConfirmWindowUnits,
                    onOptionChange = viewModel::setRejectConfirmWindowUnits,
                    helpText = "Select the units for the reject confirm window.",
                    inputLabel = "Conf. Window",
                    inputValue = rejectConfirmWindowSetting,
                    onInputValueChange = viewModel::setRejectConfirmWindowSetting,
                    inputKeyboardType = KeyboardType.Number
                )

                FormSpacer()

                LabeledTextFieldWithHelp(
                    label = "Engineer Notes",
                    value = rejectSettingsEngineerNotes,
                    onValueChange = viewModel::setRejectSettingsEngineerNotes,
                    helpText = "Enter any notes relevant to this section.",
                    isNAToggleEnabled = false
                )

                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}