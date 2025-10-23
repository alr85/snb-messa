package com.example.mecca.formModules

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun FormRowWrapper(
    label: String,
    modifier: Modifier = Modifier,
    naButtonText: String = "N/A",
    isDisabled: Boolean = false,
    onNaClick: (() -> Unit)? = null,
    onHelpClick: (() -> Unit)? = null,
    content: @Composable RowScope.(Boolean) -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier
                .width(150.dp)
                .padding(end = 8.dp)
        )

        Row(
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            content(isDisabled)
        }

        // Only show N/A if it's enabled
        if (onNaClick != null) {
            TextButton(
                onClick = onNaClick,
                modifier = Modifier.width(60.dp)
            ) {
                Text(naButtonText)
            }
        } else {
            Spacer(modifier = Modifier.width(60.dp)) // preserve alignment
        }

        IconButton(
            onClick = { onHelpClick?.invoke() },
            modifier = Modifier.width(40.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                contentDescription = "Help for $label"
            )
        }
    }
}

@Composable
fun FormRowWrapperEditableLabel(
    label: String,
    onLabelChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    naButtonText: String = "N/A",
    isDisabled: Boolean = false,
    onNaClick: (() -> Unit)? = null,
    onHelpClick: (() -> Unit)? = null,
    content: @Composable RowScope.(Boolean) -> Unit
) {
    var isEditingLabel by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // --- Editable or static label (anchored left) ---
        if (isEditingLabel) {
            OutlinedTextField(
                value = label,
                onValueChange = onLabelChange,
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Text,
                    imeAction = androidx.compose.ui.text.input.ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { isEditingLabel = false }),
                placeholder = { Text("Label") },
                modifier = Modifier
                    .width(150.dp)
                    .padding(end = 8.dp)
            )
        } else {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(
                    textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                ),
                modifier = Modifier
                    .width(150.dp)
                    .padding(end = 8.dp)
                    .clickable { isEditingLabel = true },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // --- Middle flexible content ---
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            content(isDisabled)
        }

        // --- Right anchors ---
        if (onNaClick != null) {
            TextButton(onClick = onNaClick, modifier = Modifier.width(60.dp)) {
                Text(naButtonText)
            }
        } else {
            Spacer(modifier = Modifier.width(60.dp))
        }

        IconButton(
            onClick = { onHelpClick?.invoke() },
            modifier = Modifier.width(40.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                contentDescription = "Help for $label"
            )
        }
    }
}

