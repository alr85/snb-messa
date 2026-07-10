package com.snb.inspect.screens.service.cwCalibration

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.snb.inspect.calibrationViewModels.CalibrationCheckweigherViewModel
import com.snb.inspect.formModules.CalibrationHeader
import com.snb.inspect.formModules.LabeledObjectDropdownWithHelp
import com.snb.inspect.ui.theme.ScrollableWithScrollbar

@Composable
fun CalCwEngineerTestWeight(viewModel: CalibrationCheckweigherViewModel) {
    val equipment by viewModel.equipmentList.collectAsState(initial = emptyList())
    val selectedId = viewModel.engineerTestWeightId.value
    val selectedItem = equipment.find { it.id == selectedId }

    val isNextStepEnabled = selectedId != null

    LaunchedEffect(isNextStepEnabled) {
        viewModel.setCurrentScreenNextEnabled(isNextStepEnabled)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        CalibrationHeader(label = "Engineer Test Weight", isValid = isNextStepEnabled)

        ScrollableWithScrollbar(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Spacer(Modifier.height(16.dp))

                LabeledObjectDropdownWithHelp(
                    label = "Test Weight",
                    options = equipment,
                    selectedOption = selectedItem,
                    onSelectionChange = { viewModel.setEngineerTestWeightId(it?.id) },
                    optionLabel = { "${it.manufacturer} ${it.model} (${it.serialNumber})" },
                    helpText = "Select the test weight used for calibration."
                )

                Spacer(Modifier.height(60.dp))
            }
        }
    }
}
