package com.example.mecca.formModules.inputs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun TwoTextInputs(
    firstLabel: String,
    firstValue: String,
    onFirstChange: (String) -> Unit,
    secondLabel: String,
    secondValue: String,
    onSecondChange: (String) -> Unit,
    firstKeyboard: KeyboardType = KeyboardType.Text,
    secondKeyboard: KeyboardType = KeyboardType.Text,
    isDisabled: Boolean
) {
    fun sanitize(value: String, keyboard: KeyboardType): String {
        var v = value.replace(',', '.')
        if (keyboard == KeyboardType.Text) {
            v = v.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
        return v
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = firstValue,
            onValueChange = { raw ->
                if (!isDisabled) onFirstChange(sanitize(raw, firstKeyboard))
            },
            label = { Text(firstLabel) },
            enabled = !isDisabled,
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = firstKeyboard,
                capitalization = when (firstKeyboard) {
                    KeyboardType.Text -> KeyboardCapitalization.Sentences
                    else -> KeyboardCapitalization.None
                }
            ),
            colors = OutlinedTextFieldDefaults.colors(disabledTextColor = Color.Gray),
            modifier = Modifier.weight(1f)
        )

        OutlinedTextField(
            value = secondValue,
            onValueChange = { raw ->
                if (!isDisabled) onSecondChange(sanitize(raw, secondKeyboard))
            },
            label = { Text(secondLabel) },
            enabled = !isDisabled,
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = secondKeyboard,
                capitalization = when (secondKeyboard) {
                    KeyboardType.Text -> KeyboardCapitalization.Sentences
                    else -> KeyboardCapitalization.None
                }
            ),
            colors = OutlinedTextFieldDefaults.colors(disabledTextColor = Color.Gray),
            modifier = Modifier.weight(1f)
        )
    }
}
