package com.example.sonor.presentation.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Renders the main app logo / banner image.
 * - Android: loads the custom treble clef image (app_banner.jpg) dynamically.
 * - Desktop: falls back to a premium visual.
 */
@Composable
expect fun AppLogo(
    modifier: Modifier = Modifier
)
