package com.snb.inspect.screens.service.sov

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.snb.inspect.calibrationViewModels.SensitivityOptimisationValidationViewModel
import com.snb.inspect.formModules.CalibrationHeader
import com.snb.inspect.formModules.LabeledTextFieldWithHelp
import com.snb.inspect.ui.theme.FormSpacer
import com.snb.inspect.ui.theme.ScrollableWithScrollbar

@Composable
fun SovProductDetailsScreen(viewModel: SensitivityOptimisationValidationViewModel) {
    val isNextStepEnabled = viewModel.productDescription.value.isNotBlank()
    
    LaunchedEffect(isNextStepEnabled) {
        viewModel.setCurrentScreenNextEnabled(isNextStepEnabled)
    }

    ScrollableWithScrollbar(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(Modifier.height(16.dp))
            CalibrationHeader(label = "Product Details", showStatusIcon = false)
            Spacer(Modifier.height(16.dp))

            Text(
                text = "Please create a new product library page titled 'SNB Test'",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            LabeledTextFieldWithHelp(
                label = "Product Description",
                value = viewModel.productDescription.value,
                onValueChange = { viewModel.productDescription.value = it },
                helpText = "Description of the product being tested."
            )
            FormSpacer()


            LabeledTextFieldWithHelp(
                label = "Product Length (mm)",
                value = viewModel.productLength.value,
                onValueChange = { viewModel.productLength.value = it },
                helpText = "Length of the product in mm.",
                keyboardType = KeyboardType.Number
            )
            FormSpacer()

            LabeledTextFieldWithHelp(
                label = "Product Width (mm)",
                value = viewModel.productWidth.value,
                onValueChange = { viewModel.productWidth.value = it },
                helpText = "Width of the product in mm.",
                keyboardType = KeyboardType.Number
            )
            FormSpacer()

            LabeledTextFieldWithHelp(
                label = "Product Height (mm)",
                value = viewModel.productHeight.value,
                onValueChange = { viewModel.productHeight.value = it },
                helpText = "Height of the product in mm.",
                keyboardType = KeyboardType.Number
            )
            FormSpacer()

            LabeledTextFieldWithHelp(
                label = "Product Weight (g)",
                value = viewModel.productWeight.value,
                onValueChange = { viewModel.productWeight.value = it },
                helpText = "Weight of the product in grams.",
                keyboardType = KeyboardType.Number
            )
            FormSpacer()


            LabeledTextFieldWithHelp(
                label = "Product Comments",
                value = viewModel.productComments.value,
                onValueChange = { viewModel.productComments.value = it },
                helpText = "Comments on product details, presentation and fluctuations with respect to any possible performance restrictions, examples include large differences in conductivity, changes in size, random presentation, double stacking, packaging inconsistencies. Be as descriptive as possible.",
                singleLine = false,
                showHelpOnFocusIfEmpty = true
            )
            FormSpacer()

            Spacer(Modifier.height(60.dp))
        }
    }
}
