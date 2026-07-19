package com.example.sonor.presentation.ui.components

import android.view.ViewGroup
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.ui.PlayerView
import com.example.sonor.audio.MusicController
import com.example.sonor.audio.MusicControllerImpl

/**
 * Android actual: wraps ExoPlayer's [PlayerView] and connects it to
 * the shared [MusicController] ExoPlayer instance.
 */
@Composable
actual fun VideoPlayer(
    modifier: Modifier,
    musicController: MusicController,
    isPlaying: Boolean
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            PlayerView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                useController = true
            }
        },
        update = { playerView ->
            if (musicController is MusicControllerImpl) {
                // Attach player deterministically on every update to avoid stale attachments.
                if (playerView.player != musicController.exoPlayer) {
                    playerView.player = musicController.exoPlayer
                }
            }
        }
    )

    // Keep playback in sync with UI intent when possible.
    LaunchedEffect(isPlaying, musicController) {
        if (isPlaying) musicController.resume() else musicController.pause()
    }

    // Cleanup ExoPlayer listener/resources when this composable leaves composition.
    DisposableEffect(musicController) {
        onDispose {
            // MusicControllerImpl.android has a release() method; dispose only when applicable.
            (musicController as? com.example.sonor.audio.MusicControllerImpl)?.release()
        }
    }
}
