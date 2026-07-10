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
fun CalCwTestProductDetails(viewModel: CalibrationCheckweigherViewModel) {
    val isNextStepEnabled = viewModel.productDescription.value.isNotBlank()

    LaunchedEffect(isNextStepEnabled) {
        viewModel.setCurrentScreenNextEnabled(isNextStepEnabled)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        CalibrationHeader(label = "Test Product Details", isValid = isNextStepEnabled)

        ScrollableWithScrollbar(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Spacer(Modifier.height(16.dp))

                LabeledTextFieldWithHelp(
                    label = "Description",
                    value = viewModel.productDescription.value,
                    onValueChange = viewModel::setProductDescription,
                    helpText = "Enter product description."
                )
                FormSpacer()

                LabeledTextFieldWithHelp(
                    label = "Length (mm)",
                    value = viewModel.productLength.value,
                    onValueChange = viewModel::setProductLength,
                    helpText = "Product length."
                )
                FormSpacer()

                LabeledTextFieldWithHelp(
                    label = "Width (mm)",
                    value = viewModel.productWidth.value,
                    onValueChange = viewModel::setProductWidth,
                    helpText = "Product width."
                )
                FormSpacer()

                LabeledTextFieldWithHelp(
                    label = "Height (mm)",
                    value = viewModel.productHeight.value,
                    onValueChange = viewModel::setProductHeight,
                    helpText = "Product height."
                )
                FormSpacer()

                LabeledTextFieldWithHelp(
                    label = "Gross Weight (g)",
                    value = viewModel.grossWeight.value,
                    onValueChange = viewModel::setGrossWeight,
                    helpText = "Enter gross weight."
                )
                FormSpacer()

                LabeledTextFieldWithHelp(
                    label = "Tare Weight (g)",
                    value = viewModel.tareWeight.value,
                    onValueChange = viewModel::setTareWeight,
                    helpText = "Enter tare weight."
                )
                FormSpacer()

                LabeledTextFieldWithHelp(
                    label = "Product Library Reference",
                    value = viewModel.productLibraryReference.value,
                    onValueChange = viewModel::setProductLibraryReference,
                    helpText = "Reference number in the machine's library."
                )
                FormSpacer()

                Spacer(Modifier.height(60.dp))
            }
        }
    }
}
