package com.example.sonor.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.sonor.domain.model.Song
import com.example.sonor.presentation.viewmodel.HomeViewModel
import com.example.sonor.presentation.viewmodel.HomeUiState
import com.example.sonor.ui.components.AnimatedSongItem
import com.example.sonor.ui.theme.*
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AlbumDetailScreen(
    albumName: String,
    albumArtUri: String?,
    albumArtist: String,
    homeViewModel: HomeViewModel = koinViewModel(),
    onBackPressed: () -> Unit,
    onSongClick: (Song) -> Unit
) {
    val uiState by homeViewModel.uiState.collectAsState()

    // Get album's songs by filtering all songs
    val albumSongs by remember(albumName) {
        derivedStateOf {
            when (val state = uiState) {
                is HomeUiState.Success -> state.songs.filter { it.album == albumName }
                else -> emptyList()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightBlack)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackPressed) {
                    Icon(Icons.Rounded.ArrowBack, contentDescription = "Retour", tint = WhitePure)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Album header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Surface(
                    modifier = Modifier.size(150.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = OnyxSurface,
                    tonalElevation = 8.dp
                ) {
                    AsyncImage(
                        model = albumArtUri,
                        contentDescription = albumName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = albumName,
                        style = Typography.displayLarge.copy(fontSize = 28.sp, fontWeight = FontWeight.Bold),
                        color = WhitePure,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = albumArtist,
                        style = Typography.titleLarge,
                        color = LarkAccent,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = "${albumSongs.size} chansons",
                        style = Typography.bodyLarge,
                        color = WhiteMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Play and Shuffle buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { homeViewModel.playSongs(albumSongs) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OnyxSurface
                    ),
                    shape = RoundedCornerShape(24.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Icon(Icons.Rounded.PlayArrow, contentDescription = "Play", tint = LarkAccent)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Lire",
                        color = LarkAccent,
                        style = Typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Button(
                    onClick = { homeViewModel.playSongs(albumSongs, shuffle = true) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OnyxSurface
                    ),
                    shape = RoundedCornerShape(24.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Icon(Icons.Rounded.Shuffle, contentDescription = "Shuffle all", tint = LarkAccent)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Tout mélanger",
                        color = LarkAccent,
                        style = Typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Songs list
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(albumSongs) { index, song ->
                    AnimatedSongItem(
                        song = song,
                        index = index,
                        onClick = {
                            homeViewModel.playSong(song)
                            onSongClick(song)
                        }
                    )
                }
            }
        }
    }
}
