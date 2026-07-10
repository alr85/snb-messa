package com.snb.inspect.screens.service.cwCalibration

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.snb.inspect.calibrationViewModels.CalibrationCheckweigherViewModel
import com.snb.inspect.formModules.CalibrationHeader
import com.snb.inspect.formModules.ConditionState
import com.snb.inspect.formModules.inputs.SimpleTextInput
import com.snb.inspect.ui.theme.FormSpacer
import com.snb.inspect.ui.theme.FormWrapperSurface
import com.snb.inspect.ui.theme.LazyColumnWithScrollbar

private data class ChecklistCardModel(
    val key: String,
    val title: String,
    val hint: String,
    val helpText: String,
    val condition: ConditionState,
    val onConditionChange: (ConditionState) -> Unit,
    val comments: String,
    val onCommentsChange: (String) -> Unit
)

@Composable
fun CalCwSystemChecklist(viewModel: CalibrationCheckweigherViewModel) {
    val items = remember(
        viewModel.beltCondition.value, viewModel.beltConditionComments.value,
        viewModel.safetyCircuitCondition.value, viewModel.safetyCircuitConditionComments.value,
        viewModel.guardCondition.value, viewModel.guardConditionComments.value,
        viewModel.vibrationCondition.value, viewModel.vibrationConditionComments.value,
        viewModel.weighTableObstruction.value, viewModel.weighTableObstructionComments.value,
        viewModel.productTransferCondition.value, viewModel.productTransferConditionComments.value,
        viewModel.machineStabilityCondition.value, viewModel.machineStabilityConditionComments.value
    ) {
        listOf(
            ChecklistCardModel(
                key = "belt",
                title = "Conveyor Belts",
                hint = "Clean, intact, tracking correctly",
                helpText = "Condition of the conveyor belts.",
                condition = viewModel.beltCondition.value,
                onConditionChange = viewModel::setBeltCondition,
                comments = viewModel.beltConditionComments.value,
                onCommentsChange = viewModel::setBeltConditionComments
            ),
            ChecklistCardModel(
                key = "safety",
                title = "Safety Circuit",
                hint = "E-stops, interlocks operate correctly",
                helpText = "Condition of the safety circuit.",
                condition = viewModel.safetyCircuitCondition.value,
                onConditionChange = viewModel::setSafetyCircuitCondition,
                comments = viewModel.safetyCircuitConditionComments.value,
                onCommentsChange = viewModel::setSafetyCircuitConditionComments
            ),
            ChecklistCardModel(
                key = "guarding",
                title = "Guarding",
                hint = "Intact, covers risk areas",
                helpText = "Condition of the guarding.",
                condition = viewModel.guardCondition.value,
                onConditionChange = viewModel::setGuardCondition,
                comments = viewModel.guardConditionComments.value,
                onCommentsChange = viewModel::setGuardConditionComments
            ),
            ChecklistCardModel(
                key = "vibration",
                title = "Vibration",
                hint = "No excessive vibration affecting weight",
                helpText = "Is there vibration affecting the scale?",
                condition = viewModel.vibrationCondition.value,
                onConditionChange = viewModel::setVibrationCondition,
                comments = viewModel.vibrationConditionComments.value,
                onCommentsChange = viewModel::setVibrationConditionComments
            ),
            ChecklistCardModel(
                key = "obstruction",
                title = "Weigh Table Obstruction",
                hint = "Table free from debris or contact",
                helpText = "Is the weigh table free from obstructions?",
                condition = viewModel.weighTableObstruction.value,
                onConditionChange = viewModel::setWeighTableObstruction,
                comments = viewModel.weighTableObstructionComments.value,
                onCommentsChange = viewModel::setWeighTableObstructionComments
            ),
            ChecklistCardModel(
                key = "transfer",
                title = "Product Transfer",
                hint = "Smooth transfer onto/off weigh belt",
                helpText = "Condition of product transfer.",
                condition = viewModel.productTransferCondition.value,
                onConditionChange = viewModel::setProductTransferCondition,
                comments = viewModel.productTransferConditionComments.value,
                onCommentsChange = viewModel::setProductTransferConditionComments
            ),
            ChecklistCardModel(
                key = "stable",
                title = "Machine Stable",
                hint = "Level and secure on floor",
                helpText = "Is the machine stable?",
                condition = viewModel.machineStabilityCondition.value,
                onConditionChange = viewModel::setMachineStabilityCondition,
                comments = viewModel.machineStabilityConditionComments.value,
                onCommentsChange = viewModel::setMachineStabilityConditionComments
            )
        )
    }

    val isNextStepEnabled = items.all { model ->
        val conditionSelected = model.condition != ConditionState.UNSPECIFIED
        val poorNeedsComment = model.condition == ConditionState.POOR && model.comments.trim().isEmpty()
        conditionSelected && !poorNeedsComment
    }

    LaunchedEffect(isNextStepEnabled) {
        viewModel.setCurrentScreenNextEnabled(isNextStepEnabled)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        CalibrationHeader(label = "System Checklist", isValid = isNextStepEnabled)
        LazyColumnWithScrollbar(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(Modifier.height(8.dp)) }
            items(items, key = { it.key }) { model -> ModernChecklistCard(model) }
            item { Spacer(Modifier.height(60.dp)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModernChecklistCard(model: ChecklistCardModel) {
    var showHelp by remember { mutableStateOf(false) }
    var commentsExpanded by remember { mutableStateOf(false) }
    val commentsRequired = model.condition == ConditionState.POOR
    val showComments = commentsRequired || commentsExpanded || model.comments.isNotBlank()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = FormWrapperSurface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = model.title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(4.dp))
                    Text(text = model.hint, style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
                IconButton(onClick = { showHelp = true }) {
                    Icon(imageVector = Icons.AutoMirrored.Outlined.HelpOutline, contentDescription = "Help")
                }
            }
            Spacer(Modifier.height(12.dp))
            ConditionSegmented(value = model.condition, onValueChange = { newState ->
                model.onConditionChange(newState)
                if (newState != ConditionState.POOR && model.comments.isBlank()) commentsExpanded = false
            })
            Spacer(Modifier.height(12.dp))
            AnimatedVisibility(visible = showComments) {
                Column {
                    SimpleTextInput(
                        value = model.comments,
                        onValueChange = model.onCommentsChange,
                        label = if (commentsRequired) "Comments (required)" else "Comments",
                        singleLine = false,
                        maxLength = 40,
                        minLines = 2
                    )
                    if (!commentsRequired && model.comments.isBlank()) {
                        Spacer(Modifier.height(6.dp))
                        TextButton(onClick = { commentsExpanded = false }) { Text("Hide comment") }
                    }
                }
            }
            if (!showComments && !commentsRequired) {
                TextButton(onClick = { commentsExpanded = true }) { Text("Add comment") }
            }
            Spacer(modifier = Modifier.height(6.dp))
            FormSpacer()
        }
    }
    if (showHelp) {
        AlertDialog(
            onDismissRequest = { showHelp = false },
            title = { Text(text = model.title) },
            text = { Text(text = model.helpText) },
            confirmButton = { TextButton(onClick = { showHelp = false }) { Text("OK") } }
        )
    }
}

@Composable
private fun ConditionSegmented(value: ConditionState, onValueChange: (ConditionState) -> Unit) {
    val options = listOf(
        ConditionState.GOOD to "Good",
        ConditionState.OK to "OK",
        ConditionState.POOR to "Poor",
        ConditionState.NA to "N/A"
    )
    Column {
        Text(text = "Condition", style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(6.dp))
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            options.forEachIndexed { index, (state, label) ->
                SegmentedButton(
                    selected = value == state,
                    onClick = { onValueChange(state) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        inactiveContainerColor = Color.White,
                        activeContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        inactiveContentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) { Text(label) }
            }
        }
    }
}
