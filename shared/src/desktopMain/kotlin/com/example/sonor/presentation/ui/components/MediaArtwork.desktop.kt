package com.example.sonor.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import com.example.sonor.domain.model.MediaType
import com.example.sonor.ui.theme.DeepSpace
import com.example.sonor.ui.theme.OnyxSurface
import com.example.sonor.ui.theme.WhiteDim

@Composable
actual fun MediaArtwork(
    artworkUri: String?,
    contentDescription: String?,
    modifier: Modifier,
    placeholderIconSize: Dp,
    mediaType: MediaType,
    songUri: String?
) {
    // Desktop simple fallback implementation
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(listOf(DeepSpace, OnyxSurface))
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (mediaType == MediaType.VIDEO) Icons.Rounded.PlayCircle else Icons.Rounded.MusicNote,
            contentDescription = null,
            tint = WhiteDim,
            modifier = Modifier.size(placeholderIconSize)
        )
    }
}
