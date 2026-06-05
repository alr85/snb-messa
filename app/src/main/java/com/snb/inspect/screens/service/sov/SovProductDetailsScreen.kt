package com.snb.inspect.screens.service.sov

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.snb.inspect.calibrationViewModels.SensitivityOptimisationValidationViewModel
import com.snb.inspect.formModules.CalibrationHeader
import com.snb.inspect.formModules.LabeledTextFieldWithHelp
import com.snb.inspect.ui.theme.FormSpacer
import com.snb.inspect.ui.theme.ScrollableWithScrollbar

@Composable
fun SovProductDetailsScreen(viewModel: SensitivityOptimisationValidationViewModel) {
    ScrollableWithScrollbar(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(Modifier.height(16.dp))
            CalibrationHeader("Product Details")
            Spacer(Modifier.height(16.dp))

            LabeledTextFieldWithHelp(
                label = "Product Description",
                value = viewModel.productDescription.value,
                onValueChange = { viewModel.productDescription.value = it },
                helpText = "Description of the product being tested."
            )
            FormSpacer()

            LabeledTextFieldWithHelp(
                label = "Library Reference",
                value = viewModel.productLibraryReference.value,
                onValueChange = { viewModel.productLibraryReference.value = it },
                helpText = "Internal library reference name."
            )
            FormSpacer()

            LabeledTextFieldWithHelp(
                label = "Library Number",
                value = viewModel.productLibraryNumber.value,
                onValueChange = { viewModel.productLibraryNumber.value = it },
                helpText = "Internal library number."
            )
            FormSpacer()

            LabeledTextFieldWithHelp(
                label = "Belt Speed",
                value = viewModel.beltSpeed.value,
                onValueChange = { viewModel.beltSpeed.value = it },
                helpText = "Operating speed of the belt/system."
            )
            FormSpacer()

            Spacer(Modifier.height(60.dp))
        }
    }
}
