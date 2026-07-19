package com.example.sonor.presentation.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sonor.domain.model.Song
import com.example.sonor.ui.theme.*

@Composable
fun LyricsScreen(
    currentSong: Song?,
    lyrics: String? = null,
    onBackPressed: () -> Unit,
    onSearchLyrics: () -> Unit,
    onAddLyrics: () -> Unit
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(DeepSpace, MidnightBlack, MidnightBlack)
                )
            )
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Top Bar ────────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackPressed) {
                    Icon(
                        Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Retour",
                        tint = WhitePure
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = currentSong?.title ?: "Titre inconnu",
                        style = Typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = WhitePure,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = currentSong?.artist ?: "Artiste inconnu",
                        style = Typography.bodyMedium,
                        color = WhiteMuted,
                        maxLines = 1
                    )
                }
                IconButton(onClick = onSearchLyrics) {
                    Icon(Icons.Rounded.Search, contentDescription = "Rechercher les paroles", tint = WhitePure)
                }
                IconButton(onClick = onAddLyrics) {
                    Icon(Icons.Rounded.Add, contentDescription = "Ajouter des paroles", tint = WhitePure)
                }
            }

            // ── Content ────────────────────────────────────────────────────────
            if (lyrics != null) {
                // Lyrics display with scrollable column
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 32.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = lyrics,
                        style = Typography.bodyLarge.copy(
                            fontSize = 20.sp,
                            lineHeight = 36.sp,
                            textAlign = TextAlign.Center
                        ),
                        color = WhitePure,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(64.dp))
                }
            } else {
                // Empty state with animated music icon
                EmptyLyricsState()
            }
        }
    }
}

// ─── Empty State with Breathing Animation ─────────────────────────────────────

@Composable
private fun EmptyLyricsState() {
    val infiniteTransition = rememberInfiniteTransition(label = "breathe")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Glowing background circle
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .alpha(alpha * 0.3f)
                        .background(
                            brush = Brush.radialGradient(
                                listOf(LarkAccent.copy(alpha = 0.4f), Color.Transparent)
                            ),
                            shape = androidx.compose.foundation.shape.CircleShape
                        )
                )
                Icon(
                    imageVector = Icons.Rounded.MusicNote,
                    contentDescription = null,
                    tint = WhiteDim.copy(alpha = alpha),
                    modifier = Modifier
                        .size(100.dp)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                )
            }

            Text(
                text = "Pas de paroles",
                style = Typography.titleLarge.copy(
                    fontSize = 26.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = WhiteMuted
            )
            Text(
                text = "Aucune parole n'est disponible pour ce morceau.\nRecherchez ou ajoutez-en une manuellement.",
                style = Typography.bodyMedium.copy(
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                ),
                color = WhiteDim,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }
    }
}

