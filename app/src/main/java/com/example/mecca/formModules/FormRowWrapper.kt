package com.example.mecca.formModules

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mecca.ui.theme.FormInputDisabledBorderColor
import com.example.mecca.ui.theme.FormInputDisabledLabelColor
import com.example.mecca.ui.theme.FormInputDisabledTextColor
import com.example.mecca.ui.theme.FormInputFocusedBorderColor
import com.example.mecca.ui.theme.FormInputFocusedLabelColor
import com.example.mecca.ui.theme.FormInputFocusedTextColor
import com.example.mecca.ui.theme.FormInputUnfocusedBorderColor
import com.example.mecca.ui.theme.FormInputUnfocusedLabelColor
import com.example.mecca.ui.theme.FormInputUnfocusedTextColor
import com.example.mecca.ui.theme.FormWrapperContent
import com.example.mecca.ui.theme.FormWrapperSurface
import com.example.mecca.ui.theme.SnbDarkGrey
import com.example.mecca.ui.theme.SnbRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormRowWrapper(
    label: String,
    modifier: Modifier = Modifier,
    naButtonText: String = "N/A",
    isDisabled: Boolean = false,
    pvStatus: String? = null, 
    pvRules: List<PvRule> = emptyList(),
    onNaClick: (() -> Unit)? = null,
    onHelpClick: (() -> Unit)? = null,
    content: @Composable RowScope.(Boolean) -> Unit
) {
    Surface(
        color = FormWrapperSurface,
        contentColor = FormWrapperContent,
        modifier = modifier
            .fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (onHelpClick != null) {
                    IconButton(
                        onClick = { if (!isDisabled) onHelpClick() },
                        enabled = !isDisabled,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                            contentDescription = "Help for $label",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }



                if (onNaClick != null) {
                    val isApplicable = naButtonText != "Edit"

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Switch(
                            checked = isApplicable,
                            onCheckedChange = { onNaClick() },
                            modifier = Modifier.scale(0.7f),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = SnbDarkGrey,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color.LightGray
                            )
                        )
                    }
                }

                if (pvStatus != null) {
                    PvIndicator(status = pvStatus, rules = pvRules)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                content(isDisabled)
            }
        }
    }
}

@Composable
fun PvIndicator(status: String, rules: List<PvRule> = emptyList()) {
    var showHelp by remember { mutableStateOf(false) }
    
    val backgroundColor = when (status) {
        "Pass" -> Color(0xFF4CAF50) // Green
        "Fail" -> SnbRed
        "Warning", "Incomplete" -> Color(0xFFFFA000) // Amber
        "N/A" -> Color.Gray
        else -> Color.Gray
    }

    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
            .clickable { showHelp = true },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "PV",
            color = Color.White,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }

    if (showHelp) {
        AlertDialog(
            onDismissRequest = { showHelp = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, null, tint = SnbDarkGrey)
                    Spacer(Modifier.width(8.dp))
                    Text("Performance Validation")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (rules.isEmpty()) {
                        Text(
                            "Performance Validation (PV) rules automatically check your data against SNB standards and retailer requirements.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        PvHelpLegendItem(
                            color = Color(0xFF4CAF50),
                            label = "PASSED",
                            description = "This section meets all required validation standards."
                        )
                        PvHelpLegendItem(
                            color = Color(0xFFFFA000),
                            label = "INCOMPLETE / WARNING",
                            description = "Mandatory information is missing. Please complete the fields to enable validation."
                        )
                        PvHelpLegendItem(
                            color = SnbRed,
                            label = "FAILED",
                            description = "Data entered does not meet the required sensitivity or safety criteria."
                        )
                        PvHelpLegendItem(
                            color = Color.Gray,
                            label = "NOT APPLICABLE",
                            description = "Validation is not required for this section."
                        )
                    } else {
                        Text(
                            "The following rules were evaluated for this section:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            rules.forEach { rule ->
                                PvRuleItem(rule)
                            }
                        }
                        
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        
                        Text(
                            "Overall status: ${status.uppercase()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showHelp = false }) { Text("OK") }
            }
        )
    }
}

@Composable
private fun PvRuleItem(rule: PvRule) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = when (rule.status) {
                PvRuleStatus.Pass -> Icons.Default.CheckCircle
                PvRuleStatus.Fail -> Icons.Default.Cancel
                PvRuleStatus.Incomplete -> Icons.AutoMirrored.Filled.Help
                PvRuleStatus.Warning -> Icons.Default.Warning
                PvRuleStatus.NA -> Icons.Default.RemoveCircle
            },
            contentDescription = null,
            tint = when (rule.status) {
                PvRuleStatus.Pass -> Color(0xFF4CAF50)
                PvRuleStatus.Fail -> SnbRed
                PvRuleStatus.Incomplete -> Color(0xFFFFA000)
                PvRuleStatus.Warning -> Color(0xFFFFA000)
                PvRuleStatus.NA -> Color.Gray
            },
            modifier = Modifier.size(18.dp).padding(top = 2.dp)
        )
        Text(
            text = rule.description,
            style = MaterialTheme.typography.bodySmall,
            color = Color.DarkGray
        )
    }
}

@Composable
private fun PvHelpLegendItem(color: Color, label: String, description: String) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Text("PV", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
        }
        Column {
            Text(label, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
            Text(description, style = MaterialTheme.typography.bodySmall)
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormRowWrapperEditableLabel(
    label: String,
    onLabelChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    naButtonText: String? = null,
    isDisabled: Boolean = false,
    pvStatus: String? = null,
    pvRules: List<PvRule> = emptyList(),
    onNaClick: (() -> Unit)? = null,
    onHelpClick: (() -> Unit)? = null,
    content: @Composable RowScope.(Boolean) -> Unit
) {
    var isEditingLabel by remember { mutableStateOf(false) }
    val fieldShape = RoundedCornerShape(14.dp)

    Surface(
        color = FormWrapperSurface,
        contentColor = FormWrapperContent,
        modifier = modifier
            .fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isEditingLabel) {
                    OutlinedTextField(
                        value = label,
                        onValueChange = onLabelChange,
                        singleLine = true,
                        enabled = !isDisabled,
                        shape = fieldShape,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            disabledContainerColor = Color.White,

                            focusedBorderColor = FormInputFocusedBorderColor,
                            unfocusedBorderColor = FormInputUnfocusedBorderColor,
                            disabledBorderColor = FormInputDisabledBorderColor,

                            focusedTextColor = FormInputFocusedTextColor,
                            unfocusedTextColor = FormInputUnfocusedTextColor,
                            disabledTextColor = FormInputDisabledTextColor,

                            focusedLabelColor = FormInputFocusedLabelColor,
                            unfocusedLabelColor = FormInputUnfocusedLabelColor,
                            disabledLabelColor = FormInputDisabledLabelColor
                        ),
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Label") },
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { isEditingLabel = false }
                        )
                    )

                    IconButton(
                        onClick = { isEditingLabel = false },
                        enabled = !isDisabled
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Check,
                            contentDescription = "Done editing label"
                        )
                    }
                } else {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier
                            .weight(1f)
                            .clickable(enabled = !isDisabled) { isEditingLabel = true },
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (onHelpClick != null) {
                        IconButton(
                            onClick = { if (!isDisabled) onHelpClick() },
                            enabled = !isDisabled,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                                contentDescription = "Help for $label",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = { isEditingLabel = true },
                        enabled = !isDisabled,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "Edit label",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                if (pvStatus != null) {
                    PvIndicator(status = pvStatus, rules = pvRules)
                }

                if (onNaClick != null) {
                    val isApplicable = naButtonText != "Edit"

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Switch(
                            checked = isApplicable,
                            onCheckedChange = { onNaClick() },
                            modifier = Modifier.scale(0.7f),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = SnbDarkGrey,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color.LightGray
                            )
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                content(isDisabled)
            }

            Spacer(Modifier.height(2.dp))
        }
    }
}
