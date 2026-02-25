package com.example.mecca.formModules

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
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
import com.example.mecca.ui.theme.FormWrapperContent
import com.example.mecca.ui.theme.FormWrapperSurface
import com.example.mecca.ui.theme.SnbRed


@OptIn(ExperimentalMaterial3Api::class)
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
    Surface(
        color = FormWrapperSurface,
        contentColor = FormWrapperContent,
        modifier = modifier
            .fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (onNaClick != null) {
                    val isNa = naButtonText == "Edit"

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "N/A",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isNa) SnbRed else Color.Gray
                        )
                        Switch(
                            checked = isNa,
                            onCheckedChange = { onNaClick() },
                            modifier = Modifier.scale(0.8f),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = SnbRed,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color.LightGray
                            )
                        )
                    }
                }

                if (onHelpClick != null) {
                    IconButton(
                        onClick = { if (!isDisabled) onHelpClick() },
                        enabled = !isDisabled,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                            contentDescription = "Help for $label"
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                content(isDisabled)
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
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
    val fieldShape = RoundedCornerShape(14.dp)

    Surface(
        color = FormWrapperSurface,
        contentColor = FormWrapperContent,
        modifier = modifier
            .fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isEditingLabel) {
                    OutlinedTextField(
                        value = label,
                        onValueChange = onLabelChange,
                        singleLine = true,
                        enabled = !isDisabled,
                        shape = fieldShape,
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
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Label") },
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { isEditingLabel = false }
                        )
                    )

                    IconButton(
                        onClick = { isEditingLabel = false },
                        enabled = !isDisabled
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Check,
                            contentDescription = "Done editing label"
                        )
                    }
                } else {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier
                            .weight(1f)
                            .clickable(enabled = !isDisabled) { isEditingLabel = true },
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    IconButton(
                        onClick = { isEditingLabel = true },
                        enabled = !isDisabled,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "Edit label"
                        )
                    }
                }

                if (onNaClick != null) {
                    val isNa = naButtonText == "Edit"

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "N/A",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isNa) SnbRed else Color.Gray
                        )
                        Switch(
                            checked = isNa,
                            onCheckedChange = { onNaClick() },
                            modifier = Modifier.scale(0.8f),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = SnbRed,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color.LightGray
                            )
                        )
                    }
                }

                if (onHelpClick != null) {
                    IconButton(
                        onClick = { if (!isDisabled) onHelpClick() },
                        enabled = !isDisabled,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                            contentDescription = "Help for $label"
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                content(isDisabled)
            }

            Spacer(Modifier.height(2.dp))
        }
    }
}
