package com.snb.inspect.screens.service.sov

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.snb.inspect.calibrationViewModels.SensitivityOptimisationValidationViewModel
import com.snb.inspect.formModules.CalibrationHeader
import com.snb.inspect.formModules.LabeledReadOnlyField
import com.snb.inspect.formModules.LabeledTextFieldWithHelp
import com.snb.inspect.ui.theme.FormSpacer
import com.snb.inspect.ui.theme.ScrollableWithScrollbar

@Composable
fun SovSummaryScreen(viewModel: SensitivityOptimisationValidationViewModel) {
    ScrollableWithScrollbar(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(Modifier.height(16.dp))
            CalibrationHeader("SOV Summary")
            Spacer(Modifier.height(16.dp))

            LabeledReadOnlyField(label = "Product", value = viewModel.productDescription.value)
            FormSpacer()
            
            LabeledReadOnlyField(label = "As Found (Fe/NF/SS)", value = "${viewModel.sensitivityAsFoundFerrous.value} / ${viewModel.sensitivityAsFoundNonFerrous.value} / ${viewModel.sensitivityAsFoundStainless.value}")
            FormSpacer()

            LabeledReadOnlyField(label = "As Left (Fe/NF/SS)", value = "${viewModel.sensitivityAsLeftFerrous.value} / ${viewModel.sensitivityAsLeftNonFerrous.value} / ${viewModel.sensitivityAsLeftStainless.value}")
            FormSpacer()

            CalibrationHeader("Validation Results")
            FormSpacer()
            Text("${viewModel.validationTest1Description.value}: ${viewModel.validationTest1Successes.value} / ${viewModel.validationTest1Passes.value}")
            Text("${viewModel.validationTest2Description.value}: ${viewModel.validationTest2Successes.value} / ${viewModel.validationTest2Passes.value}")
            Text("${viewModel.validationTest3Description.value}: ${viewModel.validationTest3Successes.value} / ${viewModel.validationTest3Passes.value}")
            FormSpacer()

            LabeledTextFieldWithHelp(
                label = "Customer Name",
                value = viewModel.customerName.value,
                onValueChange = { viewModel.customerName.value = it },
                helpText = "Name of the customer representative witnessing the test."
            )
            FormSpacer()

            Button(
                onClick = { /* Handle Finalise */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Finalise and Upload SOV")
            }

            Spacer(Modifier.height(60.dp))
        }
    }
}
