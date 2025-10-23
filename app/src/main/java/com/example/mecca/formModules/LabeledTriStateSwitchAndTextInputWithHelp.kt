package com.example.mecca.formModules

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import com.example.mecca.formModules.inputs.TriStateSwitchWithInput

//
//@Composable
//fun LabeledTriStateSwitchAndTextInputWithHelp(
//    label: String,
//    currentState: YesNoState = YesNoState.NO,
//    onStateChange: (YesNoState) -> Unit,
//    helpText: String,
//    inputLabel: String,
//    inputValue: String,
//    onInputValueChange: (String) -> Unit,
//    inputKeyboardType: KeyboardType = KeyboardType.Text
//) {
//    var showHelpDialog by remember { mutableStateOf(false) }
//    var isDisabled by remember { mutableStateOf(false) }
//    var localCurrentState by remember {
//        mutableStateOf(if (currentState == YesNoState.UNSPECIFIED) YesNoState.NO else currentState)
//    }
//
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = 6.dp, horizontal = 8.dp),
//        verticalAlignment = Alignment.CenterVertically,
//        horizontalArrangement = Arrangement.SpaceBetween
//    ) {
//        //Label
//        Text(
//            text = label,
//            style = MaterialTheme.typography.labelLarge,
//            modifier = Modifier.width(175.dp)
//        )
//
//        // Switch + Text
//        Row(
//            verticalAlignment = Alignment.CenterVertically,
//            modifier = Modifier.width(200.dp)
//        ) {
//            Switch(
//                checked = localCurrentState == YesNoState.YES,
//                onCheckedChange = {
//                    if (!isDisabled) {
//                        localCurrentState = if (it) YesNoState.YES else YesNoState.NO
//                        onStateChange(localCurrentState)
//                    }
//                },
//                enabled = !isDisabled
//            )
//            Text(
//                text = when (localCurrentState) {
//                    YesNoState.YES -> "Yes"
//                    YesNoState.NO -> "No"
//                    YesNoState.NA -> "N/A"
//                    YesNoState.UNSPECIFIED -> "Unspecified"
//                },
//                modifier = Modifier.padding(start = 4.dp)
//            )
//            // Input field always visible, disabled only if N/A
//            OutlinedTextField(
//                value = inputValue,
//                onValueChange = {
//                    if (!isDisabled) onInputValueChange(it)
//                },
//                label = { Text(inputLabel) },
//                enabled = !isDisabled,
//                //modifier = Modifier.width(160.dp),
//                singleLine = true,
//                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = inputKeyboardType),
//                colors = OutlinedTextFieldDefaults.colors(
//                    disabledTextColor = Color.Gray
//                )
//            )
//        }
//
//
//
//        // N/A / Edit button
//        TextButton(
//            onClick = {
//                if (isDisabled) {
//                    localCurrentState = YesNoState.NO
//                    onStateChange(localCurrentState)
//                    onInputValueChange("")
//                    isDisabled = false
//                } else {
//                    localCurrentState = YesNoState.NA
//                    onStateChange(localCurrentState)
//                    onInputValueChange("N/A")
//                    isDisabled = true
//                }
//            },
//            modifier = Modifier.width(60.dp)
//        ) {
//            Text(if (isDisabled) "Edit" else "N/A")
//        }
//
//        // Help icon
//        IconButton(
//            onClick = { showHelpDialog = true },
//            modifier = Modifier.width(40.dp)
//        ) {
//            Icon(
//                imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
//                contentDescription = "Help for $label"
//            )
//        }
//    }
//
//    if (showHelpDialog) {
//        AlertDialog(
//            onDismissRequest = { showHelpDialog = false },
//            title = { Text(text = label) },
//            text = { Text(text = helpText) },
//            confirmButton = {
//                TextButton(onClick = { showHelpDialog = false }) {
//                    Text("OK")
//                }
//            }
//        )
//    }
//}

@Composable
fun LabeledTriStateSwitchAndTextInputWithHelp(
    label: String,
    currentState: YesNoState = YesNoState.NO,
    onStateChange: (YesNoState) -> Unit,
    helpText: String,
    inputLabel: String,
    inputValue: String,
    onInputValueChange: (String) -> Unit,
    inputKeyboardType: KeyboardType = KeyboardType.Text
) {
    var showHelpDialog by remember { mutableStateOf(false) }
    var isDisabled by remember { mutableStateOf(false) }
    var localCurrentState by remember {
        mutableStateOf(if (currentState == YesNoState.UNSPECIFIED) YesNoState.NO else currentState)
    }

    FormRowWrapper(
        label = label,
        naButtonText = if (isDisabled) "Edit" else "N/A",
        isDisabled = isDisabled,
        onNaClick = {
            isDisabled = !isDisabled
            localCurrentState = if (isDisabled) YesNoState.NA else YesNoState.NO
            onStateChange(localCurrentState)
            onInputValueChange(if (isDisabled) "N/A" else "")
        },
        onHelpClick = { showHelpDialog = true }
    ) { disabled ->
        TriStateSwitchWithInput(
            currentState = localCurrentState,
            onStateChange = onStateChange,
            inputLabel = inputLabel,
            inputValue = inputValue,
            onInputValueChange = onInputValueChange,
            inputKeyboardType = inputKeyboardType,
            isDisabled = disabled
        )
    }

    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = { Text(text = label) },
            text = { Text(text = helpText) },
            confirmButton = {
                TextButton(onClick = { showHelpDialog = false }) { Text("OK") }
            }
        )
    }
}
