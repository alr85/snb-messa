package com.example.mecca.formModules.inputs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.mecca.formModules.YesNoState
import com.example.mecca.ui.theme.FormInputDisabledBorderColor
import com.example.mecca.ui.theme.FormInputDisabledContainerColor
import com.example.mecca.ui.theme.FormInputDisabledLabelColor
import com.example.mecca.ui.theme.FormInputDisabledPlaceholderColor
import com.example.mecca.ui.theme.FormInputDisabledTextColor
import com.example.mecca.ui.theme.FormInputFocusedBorderColor
import com.example.mecca.ui.theme.FormInputFocusedContainerColor
import com.example.mecca.ui.theme.FormInputFocusedLabelColor
import com.example.mecca.ui.theme.FormInputFocusedPlaceholderColor
import com.example.mecca.ui.theme.FormInputFocusedTextColor
import com.example.mecca.ui.theme.FormInputUnfocusedBorderColor
import com.example.mecca.ui.theme.FormInputUnfocusedContainerColor
import com.example.mecca.ui.theme.FormInputUnfocusedLabelColor
import com.example.mecca.ui.theme.FormInputUnfocusedPlaceholderColor
import com.example.mecca.ui.theme.FormInputUnfocusedTextColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YesNoSegmented(
    currentState: YesNoState = YesNoState.UNSPECIFIED,
    onStateChange: (YesNoState) -> Unit,
    inputLabel: String,
    inputValue: String,
    onInputValueChange: (String) -> Unit,
    inputKeyboardType: KeyboardType = KeyboardType.Text,
    isDisabled: Boolean,
    inputMaxLength: Int? = null,
    inputTransform: ((String) -> String)? = null,


    ) {
    val fieldShape = RoundedCornerShape(14.dp)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // YES / NO segmented control
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.weight(0.8f)
        ) {
            val items = listOf(
                "Yes" to YesNoState.YES,
                "No" to YesNoState.NO
            )

            items.forEachIndexed { index, (label, state) ->
                val selected = currentState == state

                SegmentedButton(
                    selected = selected,
                    onClick = { if (!isDisabled) onStateChange(state) },
                    enabled = !isDisabled,
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = items.size
                    ),
                    icon = {
                        if (selected) {
                            Icon(
                                imageVector = Icons.Outlined.Check,
                                contentDescription = null
                            )
                        }
                    }
                ) {
                    Text(label)
                }
            }
        }


        SimpleTextInput(
            value = inputValue,
            onValueChange = onInputValueChange,
            label = inputLabel,
            keyboardType = inputKeyboardType,
            isDisabled = isDisabled,
            maxLength = inputMaxLength,
            transformInput = inputTransform,
            modifier = Modifier.weight(1.2f),
            singleLine = true
        )

    }
}
