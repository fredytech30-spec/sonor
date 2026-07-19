package com.example.sonor.presentation.ui.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.example.sonor.ui.theme.DeepSpace
import com.example.sonor.ui.theme.LarkAccent
import com.example.sonor.ui.theme.OnyxSurface
import com.example.sonor.ui.theme.WhiteDim

@Composable
actual fun MediaArtwork(
    artworkUri: String?,
    contentDescription: String?,
    modifier: Modifier,
    placeholderIconSize: Dp
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if (!artworkUri.isNullOrBlank()) {
            val context = LocalContext.current
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(Uri.parse(artworkUri))
                    .build(),
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Dégradé coloré premium + icône musicale
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(listOf(DeepSpace, OnyxSurface))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.MusicNote,
                    contentDescription = null,
                    tint = WhiteDim,
                    modifier = Modifier.size(placeholderIconSize)
                )
            }
        }
    }
}
