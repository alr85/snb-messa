package com.snb.inspect.ui.theme

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Provides clean vertical spacing between elevated form card modules without divider lines.
 */
@Composable
fun FormSpacer(height: Dp = 16.dp) {
    Spacer(modifier = Modifier.height(height))
}
