package com.example.sonor.presentation.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sonor.domain.model.PlayerState
import com.example.sonor.ui.theme.*

@Composable
fun NowPlayingScreen(
    playerState: PlayerState,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    onBackClick: () -> Unit,
    onEqualizerClick: () -> Unit,
    onLyricsClick: () -> Unit,
    onPlaylistClick: () -> Unit,
    onMoreOptions: () -> Unit = {}
) {
    val currentSong = playerState.currentSong

    // Pulsating animation for the artwork when playing
    val infiniteTransition = rememberInfiniteTransition(label = "artwork_pulse")
    val artworkScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (playerState.isPlaying) 1.03f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "artwork_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(OnyxSurface, MidnightBlack, MidnightBlack))
            )
    ) {
        // Blurred background glow
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            LarkAccent.copy(alpha = if (playerState.isPlaying) 0.08f else 0.03f),
                            Color.Transparent
                        ),
                        radius = 600f
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            // ── Top Bar ────────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        Icons.Rounded.KeyboardArrowDown,
                        contentDescription = "Réduire",
                        tint = WhitePure,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "EN LECTURE",
                        style = Typography.labelSmall.copy(letterSpacing = 4.sp),
                        color = WhiteMuted
                    )
                }
                IconButton(onClick = onMoreOptions) {
                    Icon(Icons.Rounded.MoreVert, contentDescription = "Options", tint = WhiteMuted)
                }
            }

            // ── Center Section ─────────────────────────────────────────────────
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(28.dp)
            ) {

                // ── Album Artwork ──────────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .aspectRatio(1f)
                        .graphicsLayer {
                            scaleX = artworkScale
                            scaleY = artworkScale
                        }
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(DeepSpace, OnyxSurface)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Glow ring when playing
                    if (playerState.isPlaying) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.radialGradient(
                                        listOf(
                                            LarkAccent.copy(alpha = 0.15f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )
                    }
                    Icon(
                        imageVector = Icons.Rounded.MusicNote,
                        contentDescription = null,
                        tint = WhiteDim,
                        modifier = Modifier.size(72.dp)
                    )
                }

                // ── Title, Artist & Favorite ───────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = currentSong?.title ?: "Titre inconnu",
                            style = Typography.displayLarge.copy(
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = WhitePure,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = currentSong?.artist ?: "Artiste inconnu",
                            style = Typography.titleMedium,
                            color = WhiteMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(onClick = onToggleFavorite) {
                        AnimatedContent(
                            targetState = isFavorite,
                            transitionSpec = { scaleIn() + fadeIn() togetherWith scaleOut() + fadeOut() },
                            label = "favorite"
                        ) { fav ->
                            Icon(
                                imageVector = if (fav) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                contentDescription = "Favoris",
                                tint = if (fav) FavoriteRed else WhiteMuted,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                }

                // ── Progress Bar & Time ────────────────────────────────────────
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Slider(
                        value = playerState.currentPosition.toFloat(),
                        onValueChange = { onSeekTo(it.toLong()) },
                        valueRange = 0f..playerState.duration.toFloat().coerceAtLeast(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = WhitePure,
                            activeTrackColor = LarkAccent,
                            inactiveTrackColor = WhiteDim
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            formatPlayerTime(playerState.currentPosition),
                            style = Typography.labelMedium,
                            color = WhiteMuted
                        )
                        Text(
                            formatPlayerTime(playerState.duration),
                            style = Typography.labelMedium,
                            color = WhiteMuted
                        )
                    }
                }

                // ── Playback Controls ──────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    IconButton(onClick = onToggleShuffle) {
                        Icon(
                            imageVector = Icons.Rounded.Shuffle,
                            contentDescription = "Aléatoire",
                            tint = if (playerState.shuffleModeEnabled) LarkAccent else WhiteMuted,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    IconButton(onClick = onSkipPrevious) {
                        Icon(
                            Icons.Rounded.SkipPrevious,
                            contentDescription = "Précédent",
                            tint = WhitePure,
                            modifier = Modifier.size(44.dp)
                        )
                    }

                    // Central Play / Pause button with glow
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    listOf(LarkAccent.copy(alpha = 0.3f), Color.Transparent)
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            modifier = Modifier.size(70.dp),
                            color = WhitePure,
                            shape = CircleShape,
                            tonalElevation = 16.dp
                        ) {
                            IconButton(
                                onClick = onTogglePlayPause,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                AnimatedContent(
                                    targetState = playerState.isPlaying,
                                    transitionSpec = { scaleIn() + fadeIn() togetherWith scaleOut() + fadeOut() },
                                    label = "play_pause"
                                ) { playing ->
                                    Icon(
                                        imageVector = if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                        contentDescription = if (playing) "Pause" else "Lecture",
                                        tint = MidnightBlack,
                                        modifier = Modifier.size(44.dp)
                                    )
                                }
                            }
                        }
                    }

                    IconButton(onClick = onSkipNext) {
                        Icon(
                            Icons.Rounded.SkipNext,
                            contentDescription = "Suivant",
                            tint = WhitePure,
                            modifier = Modifier.size(44.dp)
                        )
                    }

                    IconButton(onClick = onToggleRepeat) {
                        Icon(
                            imageVector = when (playerState.repeatMode) {
                                PlayerState.REPEAT_MODE_ONE -> Icons.Rounded.RepeatOne
                                PlayerState.REPEAT_MODE_ALL -> Icons.Rounded.Repeat
                                else                        -> Icons.Rounded.Repeat
                            },
                            contentDescription = "Répéter",
                            tint = if (playerState.repeatMode != PlayerState.REPEAT_MODE_OFF) LarkAccent else WhiteMuted,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                // ── Bottom Action Bar ──────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Equalizer
                    IconButton(onClick = onEqualizerClick) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(
                                Icons.Rounded.Tune,
                                contentDescription = "Égaliseur",
                                tint = if (playerState.equalizerEnabled) LarkAccent else WhiteMuted,
                                modifier = Modifier.size(28.dp)
                            )
                            AnimatedVisibility(visible = playerState.equalizerEnabled) {
                                Text("ON", style = Typography.labelSmall, color = LarkAccent)
                            }
                        }
                    }

                    // Lyrics
                    Surface(
                        onClick = onLyricsClick,
                        color = OnyxSurface,
                        shape = RoundedCornerShape(24.dp),
                        tonalElevation = 4.dp
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                        ) {
                            Icon(
                                Icons.Rounded.Lyrics,
                                contentDescription = "Paroles",
                                tint = WhiteMuted,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "Paroles",
                                style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = WhiteMuted
                            )
                        }
                    }

                    // Playlist queue
                    IconButton(onClick = onPlaylistClick) {
                        Icon(
                            Icons.Rounded.QueueMusic,
                            contentDescription = "File de lecture",
                            tint = WhiteMuted,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    }
}

// ─── Time Formatter ────────────────────────────────────────────────────────────

internal fun formatPlayerTime(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}
