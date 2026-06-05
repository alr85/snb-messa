package com.snb.inspect.screens.service.sov

import androidx.compose.foundation.layout.*
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
fun SovStartScreen(viewModel: SensitivityOptimisationValidationViewModel) {
    ScrollableWithScrollbar(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(Modifier.height(16.dp))
            CalibrationHeader("Validation Start")
            Spacer(Modifier.height(16.dp))

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

            Spacer(Modifier.height(60.dp))
        }
    }
}
