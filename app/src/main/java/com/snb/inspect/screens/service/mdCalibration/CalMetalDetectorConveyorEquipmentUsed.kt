package com.snb.inspect.screens.service.mdCalibration

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.snb.inspect.calibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import com.snb.inspect.formModules.CalibrationHeader
import com.snb.inspect.formModules.LabeledDropdownWithHelp
import com.snb.inspect.ui.theme.FormSpacer
import com.snb.inspect.ui.theme.ScrollableWithScrollbar

@Composable
fun CalMetalDetectorConveyorEquipmentUsed(
    viewModel: CalibrationMetalDetectorConveyorViewModel
) {
    // Collecting flows/states for the 3 pieces of equipment
    val selectedOscilloscopeId by viewModel.equipmentOscilloscopeId
    val selectedMultimeterId by viewModel.equipmentMultimeterId
    val selectedTachometerId by viewModel.equipmentTachometerId

    // Observe equipment lists from DB/Repo via VM
    val oscilloscopeList by viewModel.oscilloscopes.collectAsState(initial = emptyList())
    val multimeterList by viewModel.multimeters.collectAsState(initial = emptyList())
    val tachometerList by viewModel.tachometers.collectAsState(initial = emptyList())

    // Convert objects to display strings for dropdowns
    val oscilloscopeOptions = oscilloscopeList.map { "${it.manufacturer} ${it.model} (${it.serialNumber})" }
    val multimeterOptions = multimeterList.map { "${it.manufacturer} ${it.model} (${it.serialNumber})" }
    val tachometerOptions = tachometerList.map { "${it.manufacturer} ${it.model} (${it.serialNumber})" }

    // Find current display strings based on saved IDs
    val selectedOscilloscopeStr = oscilloscopeList.find { it.id == selectedOscilloscopeId }?.let { "${it.manufacturer} ${it.model} (${it.serialNumber})" } ?: ""
    val selectedMultimeterStr = multimeterList.find { it.id == selectedMultimeterId }?.let { "${it.manufacturer} ${it.model} (${it.serialNumber})" } ?: ""
    val selectedTachometerStr = tachometerList.find { it.id == selectedTachometerId }?.let { "${it.manufacturer} ${it.model} (${it.serialNumber})" } ?: ""

    // Screen is always "ready" as fields are optional/have N/A toggles
    LaunchedEffect(Unit) {
        viewModel.setCurrentScreenNextEnabled(true)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        CalibrationHeader("Measuring Equipment Used")

        ScrollableWithScrollbar(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
        ) {
            Column {
                
                // 1. Oscilloscope
                LabeledDropdownWithHelp(
                    label = "Oscilloscope",
                    options = oscilloscopeOptions,
                    selectedOption = selectedOscilloscopeStr,
                    onSelectionChange = { selection ->
                        val id = oscilloscopeList.find { "${it.manufacturer} ${it.model} (${it.serialNumber})" == selection }?.id
                        viewModel.setEquipmentOscilloscopeId(id)
                    },
                    helpText = "Select the oscilloscope used during calibration.",
                    isNAToggleEnabled = true,
                    onNAChange = { wantNA ->
                        viewModel.setEquipmentOscilloscopeId(if (wantNA) null else -1)
                    },
                    isNA = selectedOscilloscopeId == null
                )

                FormSpacer()

                // 2. Multimeter
                LabeledDropdownWithHelp(
                    label = "Multimeter",
                    options = multimeterOptions,
                    selectedOption = selectedMultimeterStr,
                    onSelectionChange = { selection ->
                        val id = multimeterList.find { "${it.manufacturer} ${it.model} (${it.serialNumber})" == selection }?.id
                        viewModel.setEquipmentMultimeterId(id)
                    },
                    helpText = "Select the multimeter used during calibration.",
                    isNAToggleEnabled = true,
                    onNAChange = { wantNA ->
                        viewModel.setEquipmentMultimeterId(if (wantNA) null else -1)
                    },
                    isNA = selectedMultimeterId == null
                )

                FormSpacer()

                // 3. Tachometer
                LabeledDropdownWithHelp(
                    label = "Tachometer",
                    options = tachometerOptions,
                    selectedOption = selectedTachometerStr,
                    onSelectionChange = { selection ->
                        val id = tachometerList.find { "${it.manufacturer} ${it.model} (${it.serialNumber})" == selection }?.id
                        viewModel.setEquipmentTachometerId(id)
                    },
                    helpText = "Select the tachometer used during calibration.",
                    isNAToggleEnabled = true,
                    onNAChange = { wantNA ->
                        viewModel.setEquipmentTachometerId(if (wantNA) null else -1)
                    },
                    isNA = selectedTachometerId == null
                )

                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}
