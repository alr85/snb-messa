package com.snb.inspect.screens.service.cwCalibration

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.snb.inspect.calibrationViewModels.CalibrationCheckweigherViewModel
import com.snb.inspect.formModules.CalibrationHeader
import com.snb.inspect.formModules.LabeledTextFieldWithHelp
import com.snb.inspect.ui.theme.FormSpacer
import com.snb.inspect.ui.theme.ScrollableWithScrollbar

@Composable
fun CalCwStaticTestAsFound(viewModel: CalibrationCheckweigherViewModel) {
    val isNextStepEnabled = true

    LaunchedEffect(isNextStepEnabled) {
        viewModel.setCurrentScreenNextEnabled(isNextStepEnabled)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        CalibrationHeader(label = "Static Test (As Found)", isValid = isNextStepEnabled)

        ScrollableWithScrollbar(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Spacer(Modifier.height(16.dp))

                LabeledTextFieldWithHelp(
                    label = "Off Centre Loading Test",
                    value = viewModel.offCentreLoadingTestResultAsFound.value,
                    onValueChange = viewModel::setOffCentreLoadingTestResultAsFound,
                    helpText = "Enter results for off centre loading test."
                )
                FormSpacer()

                LabeledTextFieldWithHelp(
                    label = "Repeatability Test",
                    value = viewModel.repeatabilityTestResultAsFound.value,
                    onValueChange = viewModel::setRepeatabilityTestResultAsFound,
                    helpText = "Enter results for repeatability test."
                )
                FormSpacer()

                Spacer(Modifier.height(60.dp))
            }
        }
    }
}
