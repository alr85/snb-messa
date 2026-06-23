package com.snb.inspect.screens.service.sov

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
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
            CalibrationHeader("Product Details")
            Spacer(Modifier.height(16.dp))

            LabeledTextFieldWithHelp(
                label = "Product Description",
                value = viewModel.productDescription.value,
                onValueChange = { viewModel.productDescription.value = it },
                helpText = "Description of the product being tested."
            )
            FormSpacer()

            if (viewModel.productDescription.value.isNotBlank()) {
                Text(
                    text = "Please ensure a new product library page has been created on the detector titled:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "VAL - ${viewModel.productDescription.value}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                FormSpacer()
            }

            LabeledTextFieldWithHelp(
                label = "Product Comments",
                value = viewModel.productComments.value,
                onValueChange = { viewModel.productComments.value = it },
                helpText = "Comments on product details, presentation and fluctuations with respect to any possible performance restrictions, examples include large differences in conductivity, changes in size, random presentation, double stacking, packaging inconsistencies",
                singleLine = false
            )
            FormSpacer()

            Spacer(Modifier.height(60.dp))
        }
    }
}
