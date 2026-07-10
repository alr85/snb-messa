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
fun CalCwStaticScaleReference(viewModel: CalibrationCheckweigherViewModel) {
    val isNextStepEnabled = true

    LaunchedEffect(isNextStepEnabled) {
        viewModel.setCurrentScreenNextEnabled(isNextStepEnabled)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        CalibrationHeader(label = "Static Scale Reference", isValid = isNextStepEnabled)

        ScrollableWithScrollbar(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Spacer(Modifier.height(16.dp))

                LabeledTextFieldWithHelp(
                    label = "Make/Model",
                    value = viewModel.staticScaleMakeModel.value,
                    onValueChange = viewModel::setStaticScaleMakeModel,
                    helpText = "Static scale make and model."
                )
                FormSpacer()

                LabeledTextFieldWithHelp(
                    label = "Certificate/Ref",
                    value = viewModel.staticScaleCertRef.value,
                    onValueChange = viewModel::setStaticScaleCertRef,
                    helpText = "Calibration reference."
                )
                FormSpacer()

                LabeledTextFieldWithHelp(
                    label = "Expiry Date",
                    value = viewModel.staticScaleExpiryDate.value,
                    onValueChange = viewModel::setStaticScaleExpiryDate,
                    helpText = "Calibration expiry date."
                )
                FormSpacer()

                Spacer(Modifier.height(60.dp))
            }
        }
    }
}
