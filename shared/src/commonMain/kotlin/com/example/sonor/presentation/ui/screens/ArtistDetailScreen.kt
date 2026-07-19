package com.example.sonor.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Person
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
import com.example.sonor.domain.model.Song
import com.example.sonor.presentation.viewmodel.HomeViewModel
import com.example.sonor.presentation.viewmodel.HomeUiState
import com.example.sonor.ui.components.AnimatedSongItem
import com.example.sonor.ui.theme.*
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ArtistDetailScreen(
    artistName: String,
    homeViewModel: HomeViewModel = koinViewModel(),
    onBackPressed: () -> Unit,
    onSongClick: (Song) -> Unit
) {
    val uiState by homeViewModel.uiState.collectAsState()

    // Get artist's songs by filtering all songs
    val artistSongs by remember(artistName) {
        derivedStateOf {
            when (val state = uiState) {
                is HomeUiState.Success -> state.songs.filter { it.artist == artistName }
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

            // Artist header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Surface(
                    modifier = Modifier.size(120.dp),
                    color = OnyxSurface,
                    shape = RoundedCornerShape(24.dp),
                    tonalElevation = 8.dp
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.Person,
                            contentDescription = null,
                            tint = WhiteMuted,
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = artistName,
                        style = Typography.displayLarge.copy(fontSize = 32.sp, fontWeight = FontWeight.Bold),
                        color = WhitePure,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = "${artistSongs.size} chansons",
                        style = Typography.bodyLarge,
                        color = WhiteMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Songs list
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(artistSongs) { index, song ->
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
