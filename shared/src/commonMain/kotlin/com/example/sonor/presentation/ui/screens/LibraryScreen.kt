package com.example.sonor.presentation.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sonor.domain.model.Song
import com.example.sonor.ui.components.AnimatedSongItem
import com.example.sonor.presentation.viewmodel.HomeUiState
import com.example.sonor.presentation.viewmodel.HomeViewModel
import com.example.sonor.ui.theme.*
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LibraryScreen(
    viewModel: HomeViewModel = koinViewModel(),
    onSongClick: (Song) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val favoriteSongs by viewModel.favoriteSongs.collectAsState()
    
    var selectedFilter by remember { mutableStateOf("Tous") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightBlack)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Library Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "MA",
                        style = Typography.labelMedium.copy(letterSpacing = 2.sp),
                        color = LarkAccent
                    )
                    Text(
                        text = "COLLECTION",
                        style = Typography.displayLarge.copy(
                            fontSize = 32.sp,
                            letterSpacing = 4.sp,
                            fontWeight = FontWeight.Black
                        ),
                        color = Color.White
                    )
                }
                
                IconButton(
                    onClick = { /* Sort options */ },
                    modifier = Modifier.background(GlassSurface, CircleShape)
                ) {
                    Icon(Icons.Rounded.Sort, contentDescription = null, tint = Color.White)
                }
            }

            // Quick Filters
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val filters = listOf("Tous", "Favoris", "Albums")
                filters.forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        label = filter,
                        onClick = { selectedFilter = filter }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (val state = uiState) {
                is HomeUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = LarkAccent)
                    }
                }
                is HomeUiState.Success -> {
                    val displayedSongs = when (selectedFilter) {
                        "Favoris" -> favoriteSongs
                        else -> state.songs
                    }
                    
                    LibraryContent(
                        songs = displayedSongs,
                        filterName = selectedFilter,
                        onSongClick = onSongClick
                    )
                }
                is HomeUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = state.message, color = Color.Red)
                    }
                }
            }
        }
    }
}

@Composable
fun LibraryContent(
    songs: List<Song>,
    filterName: String,
    onSongClick: (Song) -> Unit
) {
    if (songs.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = if (filterName == "Favoris") Icons.Rounded.Favorite else Icons.Rounded.LibraryMusic,
                    contentDescription = null, 
                    modifier = Modifier.size(64.dp),
                    tint = WhiteDim
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (filterName == "Favoris") "Aucun coup de cœur pour l'instant" else "Votre collection est vide",
                    color = WhiteMuted,
                    style = Typography.bodyLarge
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            itemsIndexed(songs, key = { _, song -> song.id }) { index, song ->
                AnimatedSongItem(
                    song = song,
                    index = index,
                    onClick = { onSongClick(song) }
                )
            }
        }
    }
}

@Composable
fun FilterChip(selected: Boolean, label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (selected) LarkAccent else GlassSurface,
        modifier = Modifier.height(36.dp)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = Typography.labelMedium,
                color = if (selected) Color.Black else Color.White
            )
        }
    }
}
