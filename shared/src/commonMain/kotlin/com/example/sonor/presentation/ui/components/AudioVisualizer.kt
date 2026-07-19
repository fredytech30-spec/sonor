package com.example.sonor.presentation.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.sonor.ui.theme.LarkAccent
import com.example.sonor.ui.theme.SonorGoldLiquid
import kotlin.math.sin

/**
 * A purely animated, cross-platform audio visualizer drawn with Canvas.
 * No platform-specific audio data is required — the animation simulates
 * a realistic spectrum by combining multiple sine waves per bar.
 */
@Composable
fun AudioVisualizer(
    modifier: Modifier = Modifier,
    isPlaying: Boolean = false,
    barCount: Int = 32,
    barHeight: Dp = 60.dp,
    primaryColor: Color = LarkAccent,
    secondaryColor: Color = SonorGoldLiquid
) {
    val infiniteTransition = rememberInfiniteTransition(label = "audio_visualizer")

    // Global time counter — drives all bar animations
    val timeMs by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(10_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    // Animated amplitude multiplier (smooth fade in/out when paused)
    val amplitude by animateFloatAsState(
        targetValue = if (isPlaying) 1f else 0.08f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "amplitude"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(barHeight)
    ) {
        val canvasWidth  = size.width
        val canvasHeight = size.height
        val barW         = (canvasWidth / barCount) * 0.65f
        val gap          = (canvasWidth / barCount) * 0.35f

        for (i in 0 until barCount) {
            // Combine several sine waves to produce organic-looking movement
            val phase   = (i.toFloat() / barCount) * 2 * Math.PI.toFloat()
            val t       = timeMs / 1000f * 2 * Math.PI.toFloat()

            val rawHeight = (
                0.50f * sin(t * 1.3f + phase) +
                0.30f * sin(t * 2.7f + phase * 1.5f) +
                0.20f * sin(t * 0.7f + phase * 0.8f)
            )
            // Normalize to [0.05, 1.0]
            val normalizedHeight = ((rawHeight + 1f) / 2f).coerceIn(0.05f, 1f) * amplitude
            val bH = canvasHeight * normalizedHeight
            val xOffset = i * (barW + gap)

            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(primaryColor, secondaryColor.copy(alpha = 0.4f)),
                    startY = canvasHeight - bH,
                    endY   = canvasHeight
                ),
                topLeft = Offset(xOffset, canvasHeight - bH),
                size    = Size(barW, bH),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )
        }
    }
}
