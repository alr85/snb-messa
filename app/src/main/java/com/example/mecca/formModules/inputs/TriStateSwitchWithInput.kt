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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    var localCurrentState by remember {
        mutableStateOf(if (currentState == YesNoState.UNSPECIFIED) YesNoState.NO else currentState)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Switch(
            checked = localCurrentState == YesNoState.YES,
            onCheckedChange = {
                if (!isDisabled) {
                    localCurrentState = if (it) YesNoState.YES else YesNoState.NO
                    onStateChange(localCurrentState)
                }
            },
            enabled = !isDisabled
        )

        Text(
            text = when (localCurrentState) {
                YesNoState.YES -> "Yes"
                YesNoState.NO -> "No"
                YesNoState.NA -> "N/A"
                YesNoState.UNSPECIFIED -> "Unspecified"
            },
            modifier = Modifier.padding(end = 8.dp)
        )

        OutlinedTextField(
            value = inputValue,
            onValueChange = { if (!isDisabled) onInputValueChange(it) },
            label = { Text(inputLabel) },
            enabled = !isDisabled,
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = inputKeyboardType),
            colors = OutlinedTextFieldDefaults.colors(disabledTextColor = Color.Gray),
            modifier = Modifier.weight(1f)
        )
    }
}
