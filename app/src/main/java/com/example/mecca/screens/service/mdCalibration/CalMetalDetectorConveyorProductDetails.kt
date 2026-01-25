package com.example.mecca.screens.service.mdCalibration

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.mecca.calibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import com.example.mecca.formModules.CalibrationHeader
import com.example.mecca.formModules.LabeledTextFieldWithHelp
import com.example.mecca.ui.theme.FormSpacer
import com.example.mecca.ui.theme.ScrollableWithScrollbar

@Composable
fun CalMetalDetectorConveyorProductDetails(
    viewModel: CalibrationMetalDetectorConveyorViewModel
) {
    val description by viewModel.productDescription
    val libraryRef by viewModel.productLibraryReference
    val libraryNumber by viewModel.productLibraryNumber
    val length by viewModel.productLength
    val width by viewModel.productWidth
    val height by viewModel.productHeight
    val notes by viewModel.productDetailsEngineerNotes

    val isNextStepEnabled =
        description.isNotBlank() &&
                libraryRef.isNotBlank() &&
                libraryNumber.isNotBlank() &&
                length.isNotBlank() &&
                width.isNotBlank() &&
                height.isNotBlank()

    LaunchedEffect(isNextStepEnabled) {
        viewModel.setCurrentScreenNextEnabled(isNextStepEnabled)
    }

    Column(modifier = Modifier.fillMaxSize()) {

        CalibrationHeader("Product Details")

        ScrollableWithScrollbar(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),

        ) {
            Column {

                LabeledTextFieldWithHelp(
                    label = "Product Description",
                    value = description,
                    onValueChange = viewModel::setProductDescription,
                    helpText = "Enter the details of the product (e.g., 'GOLD BARS')."
                )

                FormSpacer()

                LabeledTextFieldWithHelp(
                    label = "Product Library Reference",
                    value = libraryRef,
                    onValueChange = viewModel::setProductLibraryReference,
                    helpText = "There is usually a 'Product Name' in the metal detector library. Enter it here."
                )

                FormSpacer()

                LabeledTextFieldWithHelp(
                    label = "Product Library Number",
                    value = libraryNumber,
                    onValueChange = viewModel::setProductLibraryNumber,
                    helpText = "Enter the library number / program number used on the metal detector."
                )

                FormSpacer()

                LabeledTextFieldWithHelp(
                    label = "Product Length (mm)",
                    value = length,
                    onValueChange = viewModel::setProductLength,
                    helpText = "Enter the length of the product in mm.",
                    keyboardType = KeyboardType.Number
                )

                FormSpacer()

                LabeledTextFieldWithHelp(
                    label = "Product Width (mm)",
                    value = width,
                    onValueChange = viewModel::setProductWidth,
                    helpText = "Enter the width of the product in mm.",
                    keyboardType = KeyboardType.Number
                )

                FormSpacer()

                LabeledTextFieldWithHelp(
                    label = "Product Height (mm)",
                    value = height,
                    onValueChange = viewModel::setProductHeight,
                    helpText = "Enter the height of the product in mm. Required for PV calibration.",
                    keyboardType = KeyboardType.Number,
                    // PV required => height is mandatory => do NOT allow N/A toggle
                    isNAToggleEnabled = !viewModel.pvRequired.value
                )

                FormSpacer()

                LabeledTextFieldWithHelp(
                    label = "Engineer Comments",
                    value = notes,
                    onValueChange = viewModel::setProductDetailsEngineerNotes,
                    helpText = "Enter any notes relevant to this section.",
                    isNAToggleEnabled = false
                )

                Spacer(Modifier.height(60.dp))
            }
        }
    }
}
