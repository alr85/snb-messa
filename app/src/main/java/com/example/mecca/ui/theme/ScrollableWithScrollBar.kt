package com.example.mecca.ui.theme


import android.R.attr.maxHeight
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt


@Composable
fun ScrollableWithScrollbar(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    scrollbarWidth: Dp = 4.dp,
    thumbHeight: Dp = 48.dp,
    scrollbarColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.55f),
    content: @Composable ColumnScope.() -> Unit
) {
    val scrollState = rememberScrollState()
    val density = LocalDensity.current

    Box(modifier = modifier) {

        // Scrollable content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(end = 12.dp) // space for the thumb
                .verticalScroll(scrollState)
        ) {
            content()
        }

        // Only draw scrollbar if we can actually scroll
        val maxScroll = scrollState.maxValue
        if (maxScroll > 0) {
            BoxWithConstraints(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .fillMaxHeight()
                    .width(scrollbarWidth)
                    .padding(vertical = 8.dp)
            ) {
                val availablePx = with(density) {
                    (maxHeight - thumbHeight).toPx().coerceAtLeast(0f)
                }

                // Safe fraction (no divide-by-zero, no NaN)

                val scrollFraction by remember(scrollState, maxScroll) {
                    derivedStateOf {
                        if (maxScroll > 0) {
                            (scrollState.value.toFloat() / maxScroll.toFloat()).coerceIn(0f, 1f)
                        } else {
                            0f
                        }
                    }
                }
                val fraction = scrollFraction


                val offsetPx = (availablePx * fraction).coerceIn(0f, availablePx)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(thumbHeight)
                        .offset { IntOffset(0, offsetPx.roundToInt()) }
                        .background(scrollbarColor, RoundedCornerShape(50))
                )
            }
        }
    }
}


@Composable
fun LazyColumnWithScrollbar(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    scrollbarWidth: Dp = 4.dp,
    thumbHeight: Dp = 48.dp,
    scrollbarColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.55f),
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: LazyListScope.() -> Unit
) {
    val listState = rememberLazyListState()
    val density = LocalDensity.current

    Box(modifier = modifier) {

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(end = 12.dp),
            verticalArrangement = verticalArrangement,
        ) {
            content()
        }

        // Scrollbar
        BoxWithConstraints(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .fillMaxHeight()
                .width(scrollbarWidth)
                .padding(vertical = 8.dp)
        ) {
            val availableHeightPx = with(density) {
                (maxHeight - thumbHeight).toPx().coerceAtLeast(0f)
            }

            // Derive scroll fraction from frequently-changing layoutInfo
            val scrollFraction by remember(listState) {
                derivedStateOf {
                    val layoutInfo = listState.layoutInfo
                    val totalItems = layoutInfo.totalItemsCount
                    val visibleItems = layoutInfo.visibleItemsInfo.size

                    if (totalItems <= 0 || visibleItems <= 0 || visibleItems >= totalItems) {
                        null // no scrollbar needed
                    } else {
                        val firstIndex = layoutInfo.visibleItemsInfo.firstOrNull()?.index ?: 0
                        val maxIndex = (totalItems - visibleItems).coerceAtLeast(1)
                        (firstIndex.toFloat() / maxIndex.toFloat()).coerceIn(0f, 1f)
                    }
                }
            }

            val fraction = scrollFraction
            if (fraction != null) {
                val thumbOffsetPx = (availableHeightPx * fraction).coerceIn(0f, availableHeightPx)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(thumbHeight)
                        .offset { IntOffset(0, thumbOffsetPx.roundToInt()) }
                        .background(scrollbarColor, RoundedCornerShape(50))
                )
            }
        }

    }
}
