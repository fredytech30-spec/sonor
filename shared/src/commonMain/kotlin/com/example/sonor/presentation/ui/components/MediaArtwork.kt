package com.example.sonor.presentation.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

import com.example.sonor.domain.model.MediaType

/**
 * Affiche la pochette d'album depuis [artworkUri] ou génère une vignette locale (vidéo/audio).
 * - Android : chargé via Coil AsyncImage ou extraction locale
 * - Desktop  : fallback vers icône musicale colorée
 */
@Composable
expect fun MediaArtwork(
    artworkUri: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    placeholderIconSize: Dp = 48.dp,
    mediaType: MediaType = MediaType.AUDIO,
    songUri: String? = null
)
