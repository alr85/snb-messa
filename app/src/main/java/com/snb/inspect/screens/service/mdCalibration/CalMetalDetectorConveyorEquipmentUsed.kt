package com.snb.inspect.screens.service.mdCalibration

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.snb.inspect.calibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import com.snb.inspect.dataClasses.MeasuringEquipmentLocal
import com.snb.inspect.formModules.CalibrationHeader
import com.snb.inspect.formModules.LabeledDropdownWithHelp
import com.snb.inspect.ui.theme.FormSpacer
import com.snb.inspect.ui.theme.ScrollableWithScrollbar

@Composable
fun CalMetalDetectorConveyorEquipmentUsed(
    viewModel: CalibrationMetalDetectorConveyorViewModel
) {
    val userFusionId by viewModel.userFusionId.collectAsState()

    // Collecting flows/states for the 3 pieces of equipment
    val selectedOscilloscopeId by viewModel.equipmentOscilloscopeId
    val selectedMultimeterId by viewModel.equipmentMultimeterId
    val selectedTachometerId by viewModel.equipmentTachometerId

    // Observe equipment lists from DB/Repo via VM
    val oscilloscopeList by viewModel.oscilloscopes.collectAsState(initial = emptyList())
    val multimeterList by viewModel.multimeters.collectAsState(initial = emptyList())
    val tachometerList by viewModel.tachometers.collectAsState(initial = emptyList())

    fun formatLabel(item: MeasuringEquipmentLocal): String {
        val isMine = userFusionId != null && item.employeeID == userFusionId
        val prefix = if (isMine) "⭐ " else ""
        val suffix = if (isMine) " (Assigned to me)" else ""
        return "$prefix${item.manufacturer} ${item.model} (${item.serialNumber})$suffix"
    }

    // Convert objects to display strings for dropdowns
    val oscilloscopeOptions = oscilloscopeList.map { formatLabel(it) }
    val multimeterOptions = multimeterList.map { formatLabel(it) }
    val tachometerOptions = tachometerList.map { formatLabel(it) }

    // Find current display strings based on saved IDs
    val selectedOscilloscopeStr = oscilloscopeList.find { it.id == selectedOscilloscopeId }?.let { formatLabel(it) } ?: ""
    val selectedMultimeterStr = multimeterList.find { it.id == selectedMultimeterId }?.let { formatLabel(it) } ?: ""
    val selectedTachometerStr = tachometerList.find { it.id == selectedTachometerId }?.let { formatLabel(it) } ?: ""

    // Screen is always "ready" as fields are optional/have N/A toggles
    LaunchedEffect(Unit) {
        viewModel.setCurrentScreenNextEnabled(true)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        CalibrationHeader(
            label = "Measuring Equipment Used",
            isValid = true
        )

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
                        val id = oscilloscopeList.find { formatLabel(it) == selection }?.id
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
                        val id = multimeterList.find { formatLabel(it) == selection }?.id
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
                        val id = tachometerList.find { formatLabel(it) == selection }?.id
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
