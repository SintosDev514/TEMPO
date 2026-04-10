package com.campusconnectplus.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.max

@Composable
fun FloatingScrollbar(
    listState: LazyListState,
    modifier: Modifier = Modifier
) {
    val total = max(1, listState.layoutInfo.totalItemsCount)
    val first = listState.firstVisibleItemIndex.coerceAtLeast(0)
    val progress = first.toFloat() / total.toFloat()

    Box(
        modifier = modifier
            .padding(end = 6.dp)
            .width(6.dp)
            .fillMaxHeight()
    ) {
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .offset(y = (progress * 420).dp)
                .height(54.dp)
                .width(4.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(Color.Black.copy(alpha = 0.18f))
        )
    }
}

@Composable
fun FloatingGridScrollbar(
    gridState: LazyGridState,
    modifier: Modifier = Modifier
) {
    val total = max(1, gridState.layoutInfo.totalItemsCount)
    val first = gridState.firstVisibleItemIndex.coerceAtLeast(0)
    val progress = first.toFloat() / total.toFloat()

    Box(
        modifier = modifier
            .padding(end = 6.dp)
            .width(6.dp)
            .fillMaxHeight()
    ) {
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .offset(y = (progress * 420).dp)
                .height(54.dp)
                .width(4.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(Color.Black.copy(alpha = 0.18f))
        )
    }
}
