package com.snb.inspect.screens.service.cwCalibration

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
fun CalCwScaleDetails(viewModel: CalibrationCheckweigherViewModel) {
    val isNextStepEnabled = viewModel.loadcellType.value.isNotBlank() &&
                           viewModel.scaleInterval.value.isNotBlank() &&
                           viewModel.maxCapacity.value.isNotBlank()

    LaunchedEffect(isNextStepEnabled) {
        viewModel.setCurrentScreenNextEnabled(isNextStepEnabled)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        CalibrationHeader(label = "Scale Details", isValid = isNextStepEnabled)

        ScrollableWithScrollbar(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Spacer(Modifier.height(16.dp))

                LabeledTextFieldWithHelp(
                    label = "Loadcell Type",
                    value = viewModel.loadcellType.value,
                    onValueChange = viewModel::setLoadcellType,
                    helpText = "Enter the type/model of the loadcell used."
                )
                FormSpacer()

                LabeledTextFieldWithHelp(
                    label = "Scale Interval (e)",
                    value = viewModel.scaleInterval.value,
                    onValueChange = viewModel::setScaleInterval,
                    helpText = "Enter the scale verification interval (e.g. 1g)."
                )
                FormSpacer()

                LabeledTextFieldWithHelp(
                    label = "Max Capacity",
                    value = viewModel.maxCapacity.value,
                    onValueChange = viewModel::setMaxCapacity,
                    helpText = "Enter the maximum weighing capacity."
                )
                FormSpacer()

                Spacer(Modifier.height(60.dp))
            }
        }
    }
}
