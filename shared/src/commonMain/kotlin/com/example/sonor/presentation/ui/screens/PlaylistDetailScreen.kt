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
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sonor.domain.model.PlaylistEntity
import com.example.sonor.domain.model.Song
import com.example.sonor.presentation.viewmodel.HomeViewModel
import com.example.sonor.presentation.viewmodel.PlaylistViewModel
import com.example.sonor.ui.components.AnimatedSongItem
import com.example.sonor.ui.theme.*
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PlaylistDetailScreen(
    playlist: PlaylistEntity,
    homeViewModel: HomeViewModel = koinViewModel(),
    playlistViewModel: PlaylistViewModel = koinViewModel(),
    onBackPressed: () -> Unit,
    onSongClick: (Song) -> Unit
) {
    LaunchedEffect(playlist.id) {
        playlistViewModel.loadSongsForPlaylist(playlist.id)
    }

    val playlistSongs by playlistViewModel.selectedPlaylistSongs.collectAsState()

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

            // Playlist header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Surface(
                    modifier = Modifier.size(150.dp),
                    color = OnyxSurface,
                    shape = RoundedCornerShape(24.dp),
                    tonalElevation = 8.dp
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.QueueMusic,
                            contentDescription = playlist.name,
                            tint = WhiteMuted,
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = playlist.name,
                        style = Typography.displayLarge.copy(fontSize = 32.sp, fontWeight = FontWeight.Bold),
                        color = WhitePure,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    playlist.description?.let { desc ->
                        Text(
                            text = desc,
                            style = Typography.bodyLarge,
                            color = WhiteMuted,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Text(
                        text = "${playlistSongs.size} chansons",
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
                    onClick = { homeViewModel.playSongs(playlistSongs) },
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
                    onClick = { homeViewModel.playSongs(playlistSongs, shuffle = true) },
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
                itemsIndexed(playlistSongs) { index, song ->
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
