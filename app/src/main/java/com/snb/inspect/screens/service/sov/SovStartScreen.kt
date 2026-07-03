package com.snb.inspect.screens.service.sov

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.KeyboardType
import com.snb.inspect.calibrationViewModels.SensitivityOptimisationValidationViewModel
import com.snb.inspect.formModules.CalibrationHeader
import com.snb.inspect.formModules.LabeledReadOnlyField
import com.snb.inspect.formModules.LabeledTextFieldWithHelp
import com.snb.inspect.ui.theme.FormSpacer
import com.snb.inspect.ui.theme.ScrollableWithScrollbar

@Composable
fun SovStartScreen(viewModel: SensitivityOptimisationValidationViewModel) {
    val isNextStepEnabled = viewModel.newLocation.value.isNotBlank() && 
                           viewModel.beltSpeed.value.isNotBlank()
    
    LaunchedEffect(isNextStepEnabled) {
        viewModel.setCurrentScreenNextEnabled(isNextStepEnabled)
    }

    ScrollableWithScrollbar(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(Modifier.height(16.dp))
            CalibrationHeader(label = "System Details", showStatusIcon = false)
            Spacer(Modifier.height(16.dp))

            Text(
                text = "Please create and use a new product library page titled 'SNB Test'",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            LabeledReadOnlyField(
                label = "Serial Number",
                value = viewModel.system.serialNumber,
                helpText = "Unique identifier for the system."
            )
            FormSpacer()

            LabeledReadOnlyField(
                label = "Customer",
                value = viewModel.system.customerName,
                helpText = "Customer name."
            )
            FormSpacer()

            LabeledReadOnlyField(
                label = "Make/Model",
                value = viewModel.system.modelDescription,
                helpText = "Machine model."
            )
            FormSpacer()

            LabeledReadOnlyField(
                label = "System Type",
                value = viewModel.system.systemType,
                helpText = "Type of system."
            )
            FormSpacer()

            LabeledTextFieldWithHelp(
                label = "Location",
                value = viewModel.newLocation.value,
                onValueChange = viewModel::setNewLocation,
                helpText = "Verify or update the system location reference."
            )
            FormSpacer()

            LabeledTextFieldWithHelp(
                label = "Belt Speed (m/m)",
                value = viewModel.beltSpeed.value,
                onValueChange = { input ->
                    val filtered = input.filter { it.isDigit() }
                    if (filtered.length <= 3) {
                        viewModel.beltSpeed.value = filtered
                    }
                },
                helpText = "Operating speed of the belt/system.",
                keyboardType = KeyboardType.Number,
                maxLength = 3
            )
            FormSpacer()

            LabeledTextFieldWithHelp(
                label = "System Comments",
                value = viewModel.systemComments.value,
                onValueChange = { viewModel.systemComments.value = it },
                helpText = "Comments on system details, application and condition with respect to any possible performance restrictions: Examples include, insufficient metal free area, excessive vibration, low level belt contamination, earth loops, evident RF or mains interference or other environmental influences. Be as descriptive as you can.",
                singleLine = false,
                showHelpOnFocusIfEmpty = true
            )
            FormSpacer()



            Spacer(Modifier.height(60.dp))
        }
    }
}
