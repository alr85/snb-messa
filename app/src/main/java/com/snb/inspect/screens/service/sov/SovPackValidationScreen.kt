package com.snb.inspect.screens.service.sov

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.snb.inspect.calibrationViewModels.SensitivityOptimisationValidationViewModel
import com.snb.inspect.formModules.CalibrationHeader
import com.snb.inspect.ui.theme.FormSpacer
import com.snb.inspect.ui.theme.ScrollableWithScrollbar

@Composable
fun SovPackValidationScreen(viewModel: SensitivityOptimisationValidationViewModel) {
    val isNextStepEnabled = viewModel.packValidationPassed.value
    
    LaunchedEffect(isNextStepEnabled) {
        viewModel.setCurrentScreenNextEnabled(isNextStepEnabled)
    }

    ScrollableWithScrollbar(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(Modifier.height(16.dp))
            CalibrationHeader("Pack Validation")
            Spacer(Modifier.height(16.dp))

            Text(
                text = "Stability Test",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "To ensure the system is stable and the sensitivity settings are appropriate for this product, please confirm that 30 consecutive good packs have been passed through the system without any false detections.",
                style = MaterialTheme.typography.bodyMedium
            )
            
            FormSpacer()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = viewModel.packValidationPassed.value,
                    onCheckedChange = { viewModel.packValidationPassed.value = it }
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "I confirm 30 good packs passed without false detection",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(Modifier.height(60.dp))
        }
    }
}
