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
fun SovOptimisationScreen(viewModel: SensitivityOptimisationValidationViewModel) {
    ScrollableWithScrollbar(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(Modifier.height(16.dp))
            CalibrationHeader("Optimisation")
            Spacer(Modifier.height(16.dp))

            // Guidance text
            // Text("Optimise settings to find smallest test sample size. Max 1 in 10,000 false rejects. Use max operating line speeds.")
            // FormSpacer()

            LabeledTextFieldWithHelp(
                label = "Optimisation Notes",
                value = viewModel.optimisationNotes.value,
                onValueChange = { viewModel.optimisationNotes.value = it },
                helpText = "Document the optimisation process, settings changed, and false reject rate observed."
            )
            FormSpacer()

            Spacer(Modifier.height(60.dp))
        }
    }
}
