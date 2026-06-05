package com.snb.inspect.screens.service.sov

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.snb.inspect.calibrationViewModels.SensitivityOptimisationValidationViewModel
import com.snb.inspect.formModules.CalibrationHeader
import com.snb.inspect.formModules.LabeledTextFieldWithHelp
import com.snb.inspect.ui.theme.FormSpacer
import com.snb.inspect.ui.theme.ScrollableWithScrollbar

@Composable
fun SovValidationScreen(viewModel: SensitivityOptimisationValidationViewModel) {
    ScrollableWithScrollbar(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(Modifier.height(16.dp))
            CalibrationHeader("Validation (30 Passes)")
            Spacer(Modifier.height(16.dp))

            ValidationTestRow(
                description = viewModel.validationTest1Description.value,
                passes = viewModel.validationTest1Passes.value,
                onPassesChange = { viewModel.validationTest1Passes.value = it },
                successes = viewModel.validationTest1Successes.value,
                onSuccessesChange = { viewModel.validationTest1Successes.value = it }
            )
            FormSpacer()

            ValidationTestRow(
                description = viewModel.validationTest2Description.value,
                passes = viewModel.validationTest2Passes.value,
                onPassesChange = { viewModel.validationTest2Passes.value = it },
                successes = viewModel.validationTest2Successes.value,
                onSuccessesChange = { viewModel.validationTest2Successes.value = it }
            )
            FormSpacer()

            ValidationTestRow(
                description = viewModel.validationTest3Description.value,
                passes = viewModel.validationTest3Passes.value,
                onPassesChange = { viewModel.validationTest3Passes.value = it },
                successes = viewModel.validationTest3Successes.value,
                onSuccessesChange = { viewModel.validationTest3Successes.value = it }
            )
            FormSpacer()

            Spacer(Modifier.height(60.dp))
        }
    }
}

@Composable
fun ValidationTestRow(
    description: String,
    passes: String,
    onPassesChange: (String) -> Unit,
    successes: String,
    onSuccessesChange: (String) -> Unit
) {
    Column {
        Text(text = "Test: $description", modifier = Modifier.padding(bottom = 8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(Modifier.weight(1f)) {
                LabeledTextFieldWithHelp(
                    label = "Passes",
                    value = passes,
                    onValueChange = onPassesChange,
                    helpText = "Number of passes (min 30)."
                )
            }
            Box(Modifier.weight(1f)) {
                LabeledTextFieldWithHelp(
                    label = "Successes",
                    value = successes,
                    onValueChange = onSuccessesChange,
                    helpText = "Number of successful detections (must be 30/30)."
                )
            }
        }
    }
}
