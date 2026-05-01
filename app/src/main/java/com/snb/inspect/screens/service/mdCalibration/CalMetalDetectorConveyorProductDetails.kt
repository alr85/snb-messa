package com.snb.inspect.screens.service.mdCalibration

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.snb.inspect.calibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import com.snb.inspect.formModules.CalibrationHeader
import com.snb.inspect.formModules.LabeledTextFieldWithHelp
import com.snb.inspect.formModules.PvRule
import com.snb.inspect.formModules.PvRuleStatus
import com.snb.inspect.ui.theme.FormSpacer
import com.snb.inspect.ui.theme.ScrollableWithScrollbar

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
    val isConveyor by viewModel.isConveyor


    val isNextStepEnabled = if (isConveyor) {
        description.isNotBlank() &&
                libraryRef.isNotBlank() &&
                libraryNumber.isNotBlank() &&
                length.isNotBlank() &&
                width.isNotBlank() &&
                height.isNotBlank()
    } else {
        description.isNotBlank() &&
                libraryRef.isNotBlank() &&
                libraryNumber.isNotBlank()
    }

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
                    helpText = "Enter the details of the product. This should be 'Bags of sweets' or 'Fresh pork'",
                    maxLength = 25,
                    showInputLabel = false
                )

                FormSpacer()

                LabeledTextFieldWithHelp(
                    label = "Product Library Reference",
                    value = libraryRef,
                    onValueChange = viewModel::setProductLibraryReference,
                    helpText = "There is usually a 'Product Name' in the metal detector library. Enter it here.",
                    maxLength = 15,
                    showInputLabel = false
                )

                FormSpacer()

                LabeledTextFieldWithHelp(
                    label = "Product Library Number",
                    value = libraryNumber,
                    onValueChange = viewModel::setProductLibraryNumber,
                    helpText = "Enter the library number / program number used on the metal detector.",
                    maxLength = 4,
                    showInputLabel = false
                )

                FormSpacer()

                if (isConveyor) {
                    LabeledTextFieldWithHelp(
                        label = "Product Length (mm)",
                        value = length,
                        onValueChange = viewModel::setProductLength,
                        helpText = "Enter the length of the product in mm.",
                        keyboardType = KeyboardType.Number,
                        maxLength = 4,
                        showInputLabel = false
                    )

                    FormSpacer()

                    LabeledTextFieldWithHelp(
                        label = "Product Width (mm)",
                        value = width,
                        onValueChange = viewModel::setProductWidth,
                        helpText = "Enter the width of the product in mm.",
                        keyboardType = KeyboardType.Number,
                        maxLength = 4,
                        showInputLabel = false
                    )

                    FormSpacer()

                    LabeledTextFieldWithHelp(
                        label = "Product Height (mm)",
                        value = height,
                        onValueChange = viewModel::setProductHeight,
                        helpText = "Enter the height of the product in mm. Required for PV calibration.",
                        keyboardType = KeyboardType.Number,
                        // PV required => height is mandatory => do NOT allow N/A toggle
                        isNAToggleEnabled = !viewModel.pvRequired.value,
                        pvStatus = if (viewModel.pvRequired.value) {
                            val hInt = height.toIntOrNull()
                            when {
                                hInt == null -> "Fail"
                                viewModel.systemTypeId.value == 1 && hInt in 1..175 -> "Pass"
                                viewModel.systemTypeId.value == 1 && hInt > 175 -> "Warning"
                                else -> "Fail"
                            }
                        } else null,
                        pvRules = if (viewModel.pvRequired.value) {
                            val hInt = height.toIntOrNull()
                            listOf(
                                PvRule(
                                    description = "Product height must be a valid number.",
                                    status = if (hInt != null) PvRuleStatus.Pass else PvRuleStatus.Fail
                                ),
                                PvRule(
                                    description = "Height must be between 1mm and 175mm to be considered for Performance Validation.",
                                    status = when {
                                        hInt == null -> PvRuleStatus.Incomplete
                                        viewModel.systemTypeId.value == 1 && hInt in 1..175 -> PvRuleStatus.Pass
                                        viewModel.systemTypeId.value == 1 && hInt > 175 -> PvRuleStatus.Incomplete
                                        else -> PvRuleStatus.Fail
                                    }
                                )
                            )
                        } else emptyList(),
                        maxLength = 4,
                        showInputLabel = false
                    )

                    FormSpacer()
                }



                LabeledTextFieldWithHelp(
                    label = "Engineer Comments",
                    value = notes,
                    onValueChange = viewModel::setProductDetailsEngineerNotes,
                    helpText = "Enter any notes relevant to this section.",
                    isNAToggleEnabled = false,
                    maxLength = 25,
                    showInputLabel = false
                )

                Spacer(Modifier.height(60.dp))
            }
        }
    }
}
