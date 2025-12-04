package com.example.mecca.formModules

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabeledCheckboxWithHelp(
    label: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    helpText: String,
    isDisabled: Boolean = false
) {
    var showHelpDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(3f)
                .padding(end = 8.dp),
            color = if (isDisabled) Color.Gray else Color.Unspecified
        )

        Checkbox(
            checked = isChecked,
            onCheckedChange = { if (!isDisabled) onCheckedChange(it) },
            enabled = !isDisabled,
            colors = CheckboxDefaults.colors(
                checkedColor = Color.Red,
                uncheckedColor = Color.Gray,
                disabledCheckedColor = Color.Gray,
                disabledUncheckedColor = Color.Gray
            ),
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.weight(4f))
        Spacer(modifier = Modifier.weight(1f))

        IconButton(
            onClick = { showHelpDialog = true },
            enabled = !isDisabled,
            modifier = Modifier.weight(0.5f)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                contentDescription = "Help for $label",
                tint = if (isDisabled) Color.Gray else LocalContentColor.current
            )
        }
    }

    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = { Text(text = label) },
            text = { Text(text = helpText) },
            confirmButton = { TextButton(onClick = { showHelpDialog = false }) { Text("OK") } }
        )
    }
}




