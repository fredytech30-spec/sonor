package com.example.sonor.presentation.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.sonor.audio.MusicController

/**
 * Cross-platform video player component.
 *
 * - **Android**: renders an ExoPlayer [PlayerView] directly connected to [MusicController].
 * - **Desktop**: renders an animated audio visualizer as a placeholder.
 */
@Composable
expect fun VideoPlayer(
    modifier: Modifier = Modifier,
    musicController: MusicController,
    isPlaying: Boolean = false
)
