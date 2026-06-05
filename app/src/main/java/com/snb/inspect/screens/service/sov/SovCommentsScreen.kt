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
fun SovCommentsScreen(viewModel: SensitivityOptimisationValidationViewModel) {
    ScrollableWithScrollbar(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(Modifier.height(16.dp))
            CalibrationHeader("System Details & Condition Comments")
            Spacer(Modifier.height(16.dp))

            LabeledTextFieldWithHelp(
                label = "System Comments",
                value = viewModel.systemComments.value,
                onValueChange = { viewModel.systemComments.value = it },
                helpText = "Comments on system details, application and condition with respect to any possible performance restrictions: Examples include, insufficient metal free area, excessive vibration, low level belt contamination, earth loops, evident RF or mains interference or other environmental influences.",
                singleLine = false
            )
            FormSpacer()

            Spacer(Modifier.height(16.dp))
            CalibrationHeader("Product Details & Presentation Comments")
            Spacer(Modifier.height(16.dp))

            LabeledTextFieldWithHelp(
                label = "Product Comments",
                value = viewModel.productComments.value,
                onValueChange = { viewModel.productComments.value = it },
                helpText = "Comments on product details, presentation and fluctuations with respect to any possible performance restrictions, examples include large diffrences in conductivity, changes in size, ransom presentation, double stacking, packaging inconsistencies",
                singleLine = false
            )
            FormSpacer()

            Spacer(Modifier.height(60.dp))
        }
    }
}
