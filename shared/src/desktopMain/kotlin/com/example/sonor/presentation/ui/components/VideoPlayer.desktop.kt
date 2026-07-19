package com.example.sonor.presentation.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sonor.audio.MusicController
import com.example.sonor.ui.theme.*
import kotlin.math.sin

/**
 * Desktop actual: since Desktop JVM cannot use ExoPlayer/PlayerView,
 * we render a premium animated waveform visualizer as a cinematic placeholder.
 */
@Composable
actual fun VideoPlayer(
    modifier: Modifier,
    musicController: MusicController,
    isPlaying: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "desktop_video_visualizer")

    val timeMs by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(12_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    val amplitude by animateFloatAsState(
        targetValue = if (isPlaying) 1f else 0.1f,
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label = "amplitude"
    )

    Box(
        modifier = modifier
            .background(
                brush = Brush.verticalGradient(listOf(DeepSpace, MidnightBlack)),
                shape = RoundedCornerShape(16.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        // Animated waveform
        Canvas(modifier = Modifier.fillMaxSize()) {
            val barCount    = 48
            val barW        = (size.width / barCount) * 0.60f
            val gap         = (size.width / barCount) * 0.40f
            val centerY     = size.height / 2f
            val maxHalfH    = size.height * 0.42f * amplitude
            val t           = timeMs / 1000f * 2 * Math.PI.toFloat()

            for (i in 0 until barCount) {
                val phase = (i.toFloat() / barCount) * 2 * Math.PI.toFloat()
                val rawH = (
                    0.5f  * sin(t * 1.5f + phase) +
                    0.3f  * sin(t * 3.1f + phase * 1.3f) +
                    0.2f  * sin(t * 0.8f + phase * 0.6f)
                )
                val halfH    = maxHalfH * ((rawH + 1f) / 2f).coerceIn(0.05f, 1f)
                val xOffset  = i * (barW + gap)

                // Upper bar
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        listOf(LarkAccent.copy(alpha = 0.9f), LarkAccent.copy(alpha = 0.1f)),
                        startY = centerY - halfH, endY = centerY
                    ),
                    topLeft      = Offset(xOffset, centerY - halfH),
                    size         = Size(barW, halfH),
                    cornerRadius = CornerRadius(3.dp.toPx())
                )
                // Mirror lower bar
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        listOf(SonorGoldLiquid.copy(alpha = 0.5f), Color.Transparent),
                        startY = centerY, endY = centerY + halfH
                    ),
                    topLeft      = Offset(xOffset, centerY),
                    size         = Size(barW, halfH),
                    cornerRadius = CornerRadius(3.dp.toPx())
                )
            }
        }

        // Overlay label
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Rounded.PlayArrow,
                contentDescription = null,
                tint = WhitePure.copy(alpha = 0.3f),
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = "Lecteur Vidéo — Desktop",
                style = androidx.compose.material3.MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 2.sp
                ),
                color = WhiteDim.copy(alpha = 0.5f)
            )
        }
    }
}
