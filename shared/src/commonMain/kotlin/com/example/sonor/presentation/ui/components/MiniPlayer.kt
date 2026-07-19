package com.example.sonor.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.sonor.domain.model.PlayerState
import com.example.sonor.ui.theme.LarkAccent
import com.example.sonor.ui.theme.OnyxSurface
import com.example.sonor.ui.theme.WhiteMuted
import com.example.sonor.ui.theme.WhitePure

/**
 * Mini player bar shown at the bottom of the screen when a song is playing.
 * Shows artwork, title/artist, skip previous/next and play-pause controls,
 * and a thin progress bar at the bottom edge.
 */
@Composable
fun MiniPlayer(
    playerState: PlayerState,
    onTogglePlayPause: () -> Unit,
    onSkipPrevious: () -> Unit = {},
    onSkipNext: () -> Unit = {},
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentSong = playerState.currentSong

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(72.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        color = OnyxSurface,
        tonalElevation = 12.dp
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                // ── Album art ──────────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF3A3A3A)),
                    contentAlignment = Alignment.Center
                ) {
                    MediaArtwork(
                        artworkUri = currentSong?.albumArtUri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        placeholderIconSize = 26.dp,
                        mediaType = currentSong?.type ?: com.example.sonor.domain.model.MediaType.AUDIO,
                        songUri = currentSong?.uri
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // ── Title & Artist ─────────────────────────────────────────
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = currentSong?.title ?: "Aucune lecture",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = WhitePure,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = currentSong?.artist ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = WhiteMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // ── Controls ───────────────────────────────────────────────
                // Skip Previous
                IconButton(
                    onClick = onSkipPrevious,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SkipPrevious,
                        contentDescription = "Précédent",
                        tint = WhiteMuted,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Play / Pause
                IconButton(
                    onClick = onTogglePlayPause,
                    modifier = Modifier.size(42.dp)
                ) {
                    Icon(
                        imageVector = if (playerState.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = if (playerState.isPlaying) "Pause" else "Lecture",
                        tint = WhitePure,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Skip Next
                IconButton(
                    onClick = onSkipNext,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SkipNext,
                        contentDescription = "Suivant",
                        tint = WhiteMuted,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // ── Progress bar at bottom ─────────────────────────────────────
            val progress = if (playerState.duration > 0) {
                (playerState.currentPosition.toFloat() / playerState.duration.toFloat()).coerceIn(0f, 1f)
            } else {
                0f
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .align(Alignment.BottomCenter),
                color = LarkAccent,
                trackColor = Color.White.copy(alpha = 0.1f)
            )
        }
    }
}
