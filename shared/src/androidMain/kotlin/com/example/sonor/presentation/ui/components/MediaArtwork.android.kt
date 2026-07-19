package com.example.sonor.presentation.ui.components

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.util.Size
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.example.sonor.domain.model.MediaType
import com.example.sonor.ui.theme.DeepSpace
import com.example.sonor.ui.theme.LarkAccent
import com.example.sonor.ui.theme.OnyxSurface
import com.example.sonor.ui.theme.WhiteDim
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
actual fun MediaArtwork(
    artworkUri: String?,
    contentDescription: String?,
    modifier: Modifier,
    placeholderIconSize: Dp,
    mediaType: MediaType,
    songUri: String?
) {
    val context = LocalContext.current

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if (!artworkUri.isNullOrBlank()) {
            // Load artwork from URL or content Uri
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(Uri.parse(artworkUri))
                    .build(),
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Generate local thumbnail (video frame or embedded album art)
            val localThumbnail by produceState<Bitmap?>(initialValue = null, songUri, mediaType) {
                value = withContext(Dispatchers.IO) {
                    if (songUri.isNullOrBlank()) return@withContext null
                    
                    val retriever = MediaMetadataRetriever()
                    try {
                        val uri = Uri.parse(songUri)
                        retriever.setDataSource(context, uri)
                        if (mediaType == MediaType.VIDEO) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                try {
                                    context.contentResolver.loadThumbnail(uri, Size(512, 512), null)
                                } catch (e: Exception) {
                                    retriever.getFrameAtTime(1000000)
                                }
                            } else {
                                retriever.getFrameAtTime(1000000)
                            }
                        } else {
                            val artBytes = retriever.embeddedPicture
                            if (artBytes != null) {
                                android.graphics.BitmapFactory.decodeByteArray(artBytes, 0, artBytes.size)
                            } else null
                        }
                    } catch (e: Exception) {
                        // Fallback using direct file path if Uri parsing failed
                        try {
                            retriever.setDataSource(songUri)
                            if (mediaType == MediaType.VIDEO) {
                                retriever.getFrameAtTime(1000000)
                            } else {
                                val artBytes = retriever.embeddedPicture
                                if (artBytes != null) {
                                    android.graphics.BitmapFactory.decodeByteArray(artBytes, 0, artBytes.size)
                                } else null
                            }
                        } catch (ex: Exception) {
                            null
                        }
                    } finally {
                        try {
                            retriever.release()
                        } catch (e: Exception) {}
                    }
                }
            }

            if (localThumbnail != null) {
                Image(
                    bitmap = localThumbnail!!.asImageBitmap(),
                    contentDescription = contentDescription,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // High-End Gradient Placeholder if no thumbnail is available
                Box(
                    modifier = Modifier
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
        }
    }
}
