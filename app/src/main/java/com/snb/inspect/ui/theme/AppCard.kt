package com.snb.inspect.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.ElevatedCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * Custom reusable elevated card component for the SNB Inspect app.
 * Applies consistent elevation (6.dp shadow) and container styling app-wide.
 */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    shape: Shape = CardDefaults.elevatedShape,
    colors: CardColors = CardDefaults.elevatedCardColors(
        containerColor = Color.White
    ),
    elevation: CardElevation = CardDefaults.elevatedCardElevation(
        defaultElevation = 6.dp
    ),
    border: BorderStroke? = null,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    if (border != null) {
        if (onClick != null) {
            Card(
                onClick = onClick,
                modifier = modifier,
                enabled = enabled,
                shape = shape,
                colors = colors,
                elevation = elevation,
                border = border,
                content = content
            )
        } else {
            Card(
                modifier = modifier,
                shape = shape,
                colors = colors,
                elevation = elevation,
                border = border,
                content = content
            )
        }
    } else {
        if (onClick != null) {
            ElevatedCard(
                onClick = onClick,
                modifier = modifier,
                enabled = enabled,
                shape = shape,
                colors = colors,
                elevation = elevation,
                content = content
            )
        } else {
            ElevatedCard(
                modifier = modifier,
                shape = shape,
                colors = colors,
                elevation = elevation,
                content = content
            )
        }
    }
}
