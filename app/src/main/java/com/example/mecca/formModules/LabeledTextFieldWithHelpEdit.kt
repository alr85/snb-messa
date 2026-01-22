@file:Suppress("SameParameterValue", "SameParameterValue", "SameParameterValue",
    "SameParameterValue", "SameParameterValue", "SameParameterValue"
)

package com.example.mecca.formModules

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.mecca.ui.theme.FormInputDisabledBorderColor
import com.example.mecca.ui.theme.FormInputDisabledLabelColor
import com.example.mecca.ui.theme.FormInputDisabledTextColor
import com.example.mecca.ui.theme.FormInputFocusedBorderColor
import com.example.mecca.ui.theme.FormInputFocusedLabelColor
import com.example.mecca.ui.theme.FormInputFocusedTextColor
import com.example.mecca.ui.theme.FormInputUnfocusedBorderColor
import com.example.mecca.ui.theme.FormInputUnfocusedLabelColor
import com.example.mecca.ui.theme.FormInputUnfocusedTextColor

@Composable
fun LabeledTextFieldWithHelpEdit(
    label: String,
    onLabelChange: (String) -> Unit,
    value: String,
    onValueChange: (String) -> Unit,
    helpText: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    isNAToggleEnabled: Boolean = true
) {
    var showHelpDialog by remember { mutableStateOf(false) }

    val fieldShape = RoundedCornerShape(14.dp)

    // Single source of truth: disabled iff value == "N/A"
    var isDisabled by remember { mutableStateOf(value == "N/A") }
    LaunchedEffect(value) { isDisabled = value == "N/A" }

    FormRowWrapperEditableLabel(
        label = label,
        onLabelChange = onLabelChange,
        naButtonText = if (isDisabled) "Edit" else "N/A",
        isDisabled = isDisabled,
        onNaClick = if (isNAToggleEnabled) {
            {
                val next = !isDisabled
                isDisabled = next
                onValueChange(if (next) "N/A" else "")
            }
        } else null,
        onHelpClick = { showHelpDialog = true }
    ) { disabled ->
        OutlinedTextField(
            value = value,
            onValueChange = { raw ->
                if (!disabled) {
                    var cleaned = raw.replace(',', '.')
                    if (keyboardType == KeyboardType.Text) {
                        cleaned = cleaned.replaceFirstChar {
                            if (it.isLowerCase()) it.titlecase() else it.toString()
                        }
                    }
                    onValueChange(cleaned)
                }
            },
            label = { Text("Value") },
            singleLine = true,
            enabled = !disabled,
            shape = fieldShape,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                capitalization = when (keyboardType) {
                    KeyboardType.Text -> KeyboardCapitalization.Sentences
                    else -> KeyboardCapitalization.None
                }
            ),
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
            modifier = Modifier.fillMaxWidth()
        )
    }

    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = { Text(text = label) },
            text = { Text(helpText) },
            confirmButton = { TextButton(onClick = { showHelpDialog = false }) { Text("OK") } }
        )
    }
}
