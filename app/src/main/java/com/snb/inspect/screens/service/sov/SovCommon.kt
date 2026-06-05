package com.snb.inspect.screens.service.sov

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.snb.inspect.formModules.LabeledTextFieldWithHelp

@Composable
fun ValidationSuccessInput(
    label: String,
    successes: String,
    onSuccessesChange: (String) -> Unit,
    minSuccesses: Int = 10
) {
    Column {
        Text(text = label, style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(8.dp))
        LabeledTextFieldWithHelp(
            label = "Successful Detections & Rejections",
            value = successes,
            onValueChange = onSuccessesChange,
            helpText = "Enter the number of times the test sample was successfully detected and rejected (min $minSuccesses).",
            keyboardType = KeyboardType.Number
        )
    }
}
