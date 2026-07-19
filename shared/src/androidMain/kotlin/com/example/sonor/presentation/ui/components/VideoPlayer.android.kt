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
 * 
 * useController = true expose les contrôles natifs ExoPlayer (seek, play/pause, skip).
 * On ne libère PAS le player ici — le lifecycle est géré par MusicControllerImpl.
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
                // Activer les contrôles natifs ExoPlayer (play/pause, seek, skip)
                useController = true
                // Afficher les overlays de contrôle pendant 3 secondes puis disparaître
                controllerShowTimeoutMs = 3000
                controllerHideOnTouch = true
            }
        },
        update = { playerView ->
            if (musicController is MusicControllerImpl) {
                // Attacher le player à chaque update pour éviter les attachements périmés
                if (playerView.player != musicController.exoPlayer) {
                    playerView.player = musicController.exoPlayer
                }
            }
        }
    )

    // Ne PAS libérer les ressources ExoPlayer ici — géré par le cycle de vie de MusicControllerImpl
    DisposableEffect(musicController) {
        onDispose {
            // Détacher uniquement la vue, pas le player
        }
    }
}
