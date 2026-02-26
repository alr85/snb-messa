package com.example.mecca.screens.service.mdCalibration


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.mecca.calibrationViewModels.CalibrationMetalDetectorConveyorViewModel
import com.example.mecca.core.FieldLimits
import com.example.mecca.formModules.CalibrationHeader
import com.example.mecca.formModules.ConditionState
import com.example.mecca.formModules.inputs.SimpleTextInput
import com.example.mecca.ui.theme.FormSpacer
import com.example.mecca.ui.theme.FormWrapperSurface
import com.example.mecca.ui.theme.LazyColumnWithScrollbar

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
fun CalMetalDetectorConveyorSystemChecklist(
    viewModel: CalibrationMetalDetectorConveyorViewModel
) {
    // Bind ViewModel state
    val beltCondition by viewModel.beltCondition
    val beltConditionComments by viewModel.beltConditionComments

    val guardCondition by viewModel.guardCondition
    val guardConditionComments by viewModel.guardConditionComments

    val safetyCircuitCondition by viewModel.safetyCircuitCondition
    val safetyCircuitConditionComments by viewModel.safetyCircuitConditionComments

    val linerCondition by viewModel.linerCondition
    val linerConditionComments by viewModel.linerConditionComments

    val cablesCondition by viewModel.cablesCondition
    val cablesConditionComments by viewModel.cablesConditionComments

    val screwsCondition by viewModel.screwsCondition
    val screwsConditionComments by viewModel.screwsConditionComments

    // Build UI models
    val items = remember(
        beltCondition, beltConditionComments,
        guardCondition, guardConditionComments,
        safetyCircuitCondition, safetyCircuitConditionComments,
        linerCondition, linerConditionComments,
        cablesCondition, cablesConditionComments,
        screwsCondition, screwsConditionComments
    ) {
        listOf(
            ChecklistCardModel(
                key = "belt",
                title = "Conveyor Belt",
                hint = "Clean, intact, tracking correctly",
                helpText = "Enter the condition and any extra comments regarding the Conveyor Belt. Comments should include belt type and any defects that need to be fixed.",
                condition = beltCondition,
                onConditionChange = viewModel::setBeltCondition,
                comments = beltConditionComments,
                onCommentsChange = viewModel::setBeltConditionComments
            ),
            ChecklistCardModel(
                key = "guarding",
                title = "Guarding",
                hint = "Intact, covers risk areas, hatches interlocked",
                helpText = "Enter the condition and any extra comments regarding the Guarding. Comments should include guarding type and any defects that need to be fixed.",
                condition = guardCondition,
                onConditionChange = viewModel::setGuardCondition,
                comments = guardConditionComments,
                onCommentsChange = viewModel::setGuardConditionComments
            ),
            ChecklistCardModel(
                key = "safety",
                title = "Safety Circuit",
                hint = "E-stops, interlocks, safety devices operate correctly",
                helpText = "Enter the condition and any extra comments regarding the Safety Circuit. Include any defects that need to be fixed.",
                condition = safetyCircuitCondition,
                onConditionChange = viewModel::setSafetyCircuitCondition,
                comments = safetyCircuitConditionComments,
                onCommentsChange = viewModel::setSafetyCircuitConditionComments
            ),
            ChecklistCardModel(
                key = "liner",
                title = "Detector Liner, Gaskets & Seals",
                hint = "No wear, damage or missing seals",
                helpText = "Enter the condition and any extra comments regarding the Detector Liner, Gaskets and Seals. Include any defects that need to be fixed.",
                condition = linerCondition,
                onConditionChange = viewModel::setLinerCondition,
                comments = linerConditionComments,
                onCommentsChange = viewModel::setLinerConditionComments
            ),
            ChecklistCardModel(
                key = "cables",
                title = "Cable Fittings",
                hint = "No damaged cables, glands secure, strain relief OK",
                helpText = "Enter the condition and any extra comments regarding Cable Fittings. Include any defects that need to be fixed.",
                condition = cablesCondition,
                onConditionChange = viewModel::setCablesCondition,
                comments = cablesConditionComments,
                onCommentsChange = viewModel::setCablesConditionComments
            ),
            ChecklistCardModel(
                key = "screws",
                title = "Screws & Fittings",
                hint = "Secure, tight, nothing missing",
                helpText = "Enter the condition and any extra comments regarding Screws and Fittings. Include any defects that need to be fixed.",
                condition = screwsCondition,
                onConditionChange = viewModel::setScrewsCondition,
                comments = screwsConditionComments,
                onCommentsChange = viewModel::setScrewsConditionComments
            )
        )
    }

    // Next enable logic
    val isNextStepEnabled = items.all { model ->
        val conditionSelected = model.condition != ConditionState.UNSPECIFIED
        val poorNeedsComment = model.condition == ConditionState.POOR && model.comments.trim().isEmpty()
        conditionSelected && !poorNeedsComment
    }

    LaunchedEffect(isNextStepEnabled) {
        viewModel.setCurrentScreenNextEnabled(isNextStepEnabled)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        CalibrationHeader("System Checklist")

        LazyColumnWithScrollbar(
            modifier = Modifier
                .fillMaxSize(),

            contentPadding = PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            // Want it explicitly red? Uncomment:
            // scrollbarColor = Color(0xFFB00020).copy(alpha = 0.55f)
        ) {
            item { Spacer(Modifier.height(8.dp)) }

            items(items, key = { it.key }) { model ->
                ModernChecklistCard(model)
            }



            // breathing room above your bottom nav buttons
            item { Spacer(Modifier.height(60.dp)) }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModernChecklistCard(
    model: ChecklistCardModel
) {
    var showHelp by remember { mutableStateOf(false) }

    // NEW: local expansion state so "Add comment" actually does something
    var commentsExpanded by remember { mutableStateOf(false) }

    val commentsRequired = model.condition == ConditionState.POOR

    // Show comments if required, if user expanded, or if there is existing text
    val showComments = commentsRequired || commentsExpanded || model.comments.isNotBlank()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = FormWrapperSurface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = model.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = model.hint,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(onClick = { showHelp = true }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                        contentDescription = "Help"
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            ConditionSegmented(
                value = model.condition,
                onValueChange = { newState ->
                    model.onConditionChange(newState)

                    // Optional: collapse comments when switching away from POOR
                    // (keeps UI tidy, doesn't delete text)
                    if (newState != ConditionState.POOR && model.comments.isBlank()) {
                        commentsExpanded = false
                    }
                }
            )

            Spacer(Modifier.height(12.dp))

            AnimatedVisibility(visible = showComments) {
                Column {
                    SimpleTextInput(
                        value = model.comments,
                        onValueChange = model.onCommentsChange,
                        label = if (commentsRequired) "Comments (required)" else "Comments",
                        singleLine = false,
                        maxLength = FieldLimits.CHECKLIST_COMMENTS,
                        transformInput = null,
                        isDisabled = false,
                        minLines = 2
                    )


                    if (!commentsRequired && model.comments.isBlank()) {
                        Spacer(Modifier.height(6.dp))
                        TextButton(onClick = { commentsExpanded = false }) {
                            Text("Hide comment")
                        }
                    }
                }
            }

            // Show Add Comment only when comments are hidden and not required
            if (!showComments && !commentsRequired) {
                TextButton(onClick = { commentsExpanded = true }) {
                    Text("Add comment")
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            FormSpacer()
        }
    }

    if (showHelp) {  //title = { Text(text = label) },

        AlertDialog(
            onDismissRequest = { showHelp = false },
            title = { Text (text = model.title) },
            text = { Text(text = model.helpText) },
            confirmButton = {
                TextButton(onClick = { showHelp = false }) { Text("OK") }
            }
        )

    }
}





@Composable
private fun ConditionSegmented(
    value: ConditionState,
    onValueChange: (ConditionState) -> Unit
) {
    val options = listOf(
        ConditionState.GOOD to "Good",
        ConditionState.POOR to "Poor",
        ConditionState.NA to "N/A"
    )

    Column {
        Text(
            text = "Condition",
            style = MaterialTheme.typography.labelMedium
        )
        Spacer(Modifier.height(6.dp))

        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            options.forEachIndexed { index, (state, label) ->
                SegmentedButton(
                    selected = value == state,
                    onClick = { onValueChange(state) },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = options.size
                    ),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        inactiveContainerColor = Color.White,
                        activeContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        inactiveContentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Text(label)
                }
            }
        }

        if (value == ConditionState.UNSPECIFIED) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Select a condition to continue.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}


