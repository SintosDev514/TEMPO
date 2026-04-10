package com.campusconnectplus.core.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Canvas-drawn circular "engagement" ring for the Home screen.
 * Shows progress (0f..1f) as an animated arc; useful for MCO 2 animation/canvas UI idea.
 */
@Composable
fun StatRingCanvas(
    progress: Float,
    modifier: Modifier = Modifier,
    ringSize: Dp = 56.dp,
    strokeWidth: Dp = 5.dp,
    trackColor: Color = Color.White.copy(alpha = 0.2f),
    progressColor: Color = Color(0xFF8BE9FF)
) {
    val animatedProgress = remember { Animatable(0f) }
    LaunchedEffect(progress) {
        animatedProgress.animateTo(
            targetValue = progress.coerceIn(0f, 1f),
            animationSpec = tween(durationMillis = 800)
        )
    }

    Canvas(modifier = modifier.size(ringSize)) {
        val strokeWidthPx = strokeWidth.toPx()
        val diameter = size.minDimension - strokeWidthPx
        val radius = diameter / 2f
        val c = center

        // Track (background ring)
        drawCircle(
            color = trackColor,
            radius = radius,
            center = c,
            style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
        )

        // Progress arc (sweep from top, clockwise)
        val topLeft = Offset(c.x - radius, c.y - radius)
        val arcSize = Size(radius * 2, radius * 2)
        drawArc(
            color = progressColor,
            startAngle = 270f, // 12 o'clock
            sweepAngle = 360f * animatedProgress.value,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
        )
    }
}
