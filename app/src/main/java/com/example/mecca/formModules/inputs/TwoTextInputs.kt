package com.example.mecca.formModules.inputs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    firstMaxLength: Int? = null,
    secondMaxLength: Int? = null,
    firstTransform: ((String) -> String)? = null,
    secondTransform: ((String) -> String)? = null,
    isDisabled: Boolean
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top, // VERY important
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        SimpleTextInput(
            value = firstValue,
            onValueChange = onFirstChange,
            label = firstLabel,
            keyboardType = firstKeyboard,
            isDisabled = isDisabled,
            maxLength = firstMaxLength,
            transformInput = firstTransform,
            modifier = Modifier.weight(1f)
        )

        SimpleTextInput(
            value = secondValue,
            onValueChange = onSecondChange,
            label = secondLabel,
            keyboardType = secondKeyboard,
            isDisabled = isDisabled,
            maxLength = secondMaxLength,
            transformInput = secondTransform,
            modifier = Modifier.weight(1f)
        )
    }
}
