package com.example.mecca.formModules.inputs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.mecca.formModules.YesNoState

@Composable
fun TriStateSwitchWithInput(
    currentState: YesNoState = YesNoState.NO,
    onStateChange: (YesNoState) -> Unit,
    inputLabel: String,
    inputValue: String,
    onInputValueChange: (String) -> Unit,
    inputKeyboardType: KeyboardType = KeyboardType.Text,
    isDisabled: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Switch only does YES/NO and is disabled when NA
        Switch(
            checked = currentState == YesNoState.YES,
            onCheckedChange = { checked ->
                if (!isDisabled) {
                    onStateChange(if (checked) YesNoState.YES else YesNoState.NO)
                }
            },
            enabled = !isDisabled
        )

        Text(
            text = when (currentState) {
                YesNoState.YES -> "Yes"
                YesNoState.NO -> "No"
                YesNoState.NA -> "N/A"
                YesNoState.UNSPECIFIED -> "Unspecified"
            },
            modifier = Modifier.padding(end = 8.dp)
        )

        OutlinedTextField(
            value = inputValue,
            onValueChange = { raw ->
                if (!isDisabled) {
                    var cleaned = raw.replace(',', '.')
                    if (inputKeyboardType == KeyboardType.Text) {
                        cleaned = cleaned.replaceFirstChar {
                            if (it.isLowerCase()) it.titlecase() else it.toString()
                        }
                    }
                    onInputValueChange(cleaned)
                }
            },
            label = { Text(inputLabel) },
            enabled = !isDisabled,
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = inputKeyboardType,
                capitalization = when (inputKeyboardType) {
                    KeyboardType.Text -> KeyboardCapitalization.Sentences
                    else -> KeyboardCapitalization.None
                }
            ),
            colors = OutlinedTextFieldDefaults.colors(disabledTextColor = Color.Gray),
            modifier = Modifier.weight(1f)
        )
    }
}

