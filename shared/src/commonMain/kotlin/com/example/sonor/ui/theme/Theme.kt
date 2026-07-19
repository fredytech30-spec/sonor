package com.example.sonor.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val SonorUltimateColorScheme = darkColorScheme(
    primary = LarkAccent,
    onPrimary = MidnightBlack,
    secondary = LarkTeal,
    onSecondary = MidnightBlack,
    tertiary = SonorPurpleCosmic,
    background = MidnightBlack,
    surface = OnyxSurface,
    onBackground = WhitePure,
    onSurface = WhitePure,
    surfaceVariant = DeepSpace,
    onSurfaceVariant = WhiteMuted
)

@Composable
fun SonorTheme(
    darkTheme: Boolean = true, // Sonor is an experience best served dark
    content: @Composable () -> Unit
) {
    val colorScheme = SonorUltimateColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
