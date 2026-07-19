package com.example.sonor.presentation.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Affiche la pochette d'album depuis [artworkUri].
 * - Android : chargé via Coil AsyncImage
 * - Desktop  : fallback vers icône musicale colorée
 */
@Composable
expect fun MediaArtwork(
    artworkUri: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    placeholderIconSize: Dp = 48.dp
)
