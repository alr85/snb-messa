package com.example.mecca.formModules.inputs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
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
import com.example.mecca.ui.theme.SnbRed
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiSelectDropdown(
    value: String,
    options: List<String>,
    selectedOptions: List<String>,
    onSelectionChange: (List<String>) -> Unit,
    isDisabled: Boolean,
    label: String = "Select options",
    placeholder: String = "Select…"
) {
    var expanded by remember { mutableStateOf(false) }

    val displayText = when {
        isDisabled -> "N/A"
        value.isNotBlank() -> value
        selectedOptions.isNotEmpty() -> selectedOptions.joinToString(" | ")
        else -> ""
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (!isDisabled) expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = displayText,
            onValueChange = {},
            readOnly = true,
            enabled = !isDisabled,
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(
                    type = ExposedDropdownMenuAnchorType.PrimaryEditable,
                    enabled = !isDisabled
                ),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = FormInputFocusedContainerColor,
                unfocusedContainerColor = FormInputUnfocusedContainerColor,
                disabledContainerColor = FormInputDisabledContainerColor,
                focusedBorderColor = FormInputFocusedBorderColor,
                unfocusedBorderColor = FormInputUnfocusedBorderColor,
                disabledBorderColor = FormInputDisabledBorderColor,
                focusedTextColor = FormInputFocusedTextColor,
                unfocusedTextColor = FormInputUnfocusedTextColor,
                disabledTextColor = FormInputDisabledTextColor,
                focusedLabelColor = FormInputFocusedLabelColor,
                unfocusedLabelColor = FormInputUnfocusedLabelColor,
                disabledLabelColor = FormInputDisabledLabelColor,
                focusedPlaceholderColor = FormInputFocusedPlaceholderColor,
                unfocusedPlaceholderColor = FormInputUnfocusedPlaceholderColor,
                disabledPlaceholderColor = FormInputDisabledPlaceholderColor
            )
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.heightIn(max = 400.dp)
        ) {
            val scrollState = rememberScrollState()
            var viewportHeightPx by remember { mutableIntStateOf(0) }

            // 1. Scrollable List Area
            // Use heightIn instead of weight to ensure content is measured correctly in the popup
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp)
                    .onSizeChanged { viewportHeightPx = it.height }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                        .padding(end = 20.dp) // space for scrollbar on right
                ) {
                    options.forEach { option ->
                        val isSelected = option in selectedOptions

                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Outlined.Check,
                                            contentDescription = null,
                                            tint = SnbRed
                                        )
                                    } else {
                                        Spacer(Modifier.size(24.dp))
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Text(
                                        text = option,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            },
                            onClick = {
                                val newSelection =
                                    if (isSelected) selectedOptions - option else selectedOptions + option
                                onSelectionChange(newSelection)
                            },
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                        )
                    }
                }

                // 2. Custom Scrollbar on the RIGHT
                if (scrollState.maxValue > 0 && viewportHeightPx > 0) {
                    val density = LocalDensity.current
                    val maxValue = scrollState.maxValue.toFloat()
                    val totalContentHeight = (viewportHeightPx + maxValue)
                    
                    val thumbHeightPx = (viewportHeightPx.toFloat() / totalContentHeight) * viewportHeightPx.toFloat()
                    val minThumbPx = with(density) { 30.dp.toPx() }
                    val finalThumbHeightPx = thumbHeightPx.coerceAtLeast(minThumbPx)

                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .width(16.dp)
                            .height(with(density) { viewportHeightPx.toDp() })
                            .background(Color.Black.copy(alpha = 0.05f))
                    ) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .width(4.dp)
                                .height(with(density) { finalThumbHeightPx.toDp() })
                                .offset {
                                    val scrollProgress = scrollState.value.toFloat() / maxValue
                                    val availableTrackHeight = viewportHeightPx - finalThumbHeightPx
                                    IntOffset(0, (scrollProgress * availableTrackHeight).roundToInt())
                                }
                                .background(SnbRed, RoundedCornerShape(50))
                        )
                    }
                }
            }

            // 3. Sticky Bottom Buttons
            HorizontalDivider()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { onSelectionChange(emptyList()) },
                    enabled = !isDisabled && selectedOptions.isNotEmpty()
                ) {
                    Text("Clear")
                }
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = { expanded = false },
                    enabled = !isDisabled
                ) {
                    Text("Done")
                }
            }
        }
    }
}
