package com.snb.inspect.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun MyAppTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = lightColorScheme(
        primary = SnbRed,  // Use your defined red primary color
        secondary = SnbDarkGrey,
        surfaceContainerLow = Color.White, // Background for ElevatedCards
        surface = Color.White
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MaterialTheme.typography, // Use the default Material3 Typography
        content = content
    )


}

