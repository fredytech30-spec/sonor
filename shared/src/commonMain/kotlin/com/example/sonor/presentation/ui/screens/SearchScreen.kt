package com.example.sonor.presentation.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sonor.domain.model.Song
import com.example.sonor.presentation.viewmodel.SearchViewModel
import com.example.sonor.ui.components.AnimatedSongItem
import com.example.sonor.ui.theme.*
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SearchScreen(
    viewModel: SearchViewModel = koinViewModel(),
    onSongClick: (Song) -> Unit = {}
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.filteredSongs.collectAsState()
    val recentSearches by viewModel.recentSearches.collectAsState()
    val recentlyPlayed by viewModel.recentlyPlayed.collectAsState()
    
    val keyboardController = LocalSoftwareKeyboardController.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightBlack)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "SONOR EXPLORE",
                style = Typography.displayLarge.copy(
                    fontSize = 32.sp,
                    letterSpacing = 6.sp,
                    brush = Brush.linearGradient(LuxuryGoldGradient)
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            if (searchQuery.isNotEmpty()) LuxuryGoldGradient 
                            else listOf(Color.White.copy(alpha = 0.1f), Color.White.copy(alpha = 0.1f))
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ),
                color = OnyxSurface.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.Search, contentDescription = null, tint = SonorGoldLiquid)
                    Spacer(modifier = Modifier.width(12.dp))
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.onSearchQueryChange(it) },
                        modifier = Modifier.weight(1f),
                        textStyle = Typography.bodyLarge.copy(color = Color.White),
                        cursorBrush = SolidColor(SonorGoldLiquid),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = {
                            viewModel.onSearchSubmitted(searchQuery)
                            keyboardController?.hide()
                        }),
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) {
                                Text("Artistes, titres ou albums...", color = WhiteDim)
                            }
                            innerTextField()
                        }
                    )
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.clearSearch() }) {
                            Icon(Icons.Rounded.Close, contentDescription = "Clear", tint = WhiteMuted)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (searchQuery.isEmpty()) {
                DefaultSearchContent(
                    recentSearches = recentSearches.map { it.query },
                    recentlyPlayed = recentlyPlayed,
                    onRecentSearchClick = { viewModel.onSearchQueryChange(it) },
                    onSongClick = { song ->
                        viewModel.onSongClicked(song)
                        onSongClick(song)
                    }
                )
            } else {
                SearchResultsContent(
                    songs = searchResults,
                    onSongClick = { song ->
                        viewModel.onSongClicked(song)
                        onSongClick(song)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DefaultSearchContent(
    recentSearches: List<String>,
    recentlyPlayed: List<Song>,
    onRecentSearchClick: (String) -> Unit,
    onSongClick: (Song) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(32.dp),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        if (recentSearches.isNotEmpty()) {
            item {
                Column {
                    SectionHeader("RECHERCHES RÉCENTES")
                    Spacer(modifier = Modifier.height(16.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        recentSearches.take(5).forEach { query ->
                            RecentSearchChip(query, onClick = { onRecentSearchClick(query) })
                        }
                    }
                }
            }
        }

        if (recentlyPlayed.isNotEmpty()) {
            item {
                Column {
                    SectionHeader("REPRENDRE LA LECTURE")
                    Spacer(modifier = Modifier.height(16.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(recentlyPlayed) { song ->
                            RecentlyPlayedCard(song, onClick = { onSongClick(song) })
                        }
                    }
                }
            }
        }

        item {
            Column {
                SectionHeader("TOUT PARCOURIR")
                Spacer(modifier = Modifier.height(20.dp))
                val genres = listOf(
                    "Spatial Audio" to AuroraGradient,
                    "Chill Gold" to LuxuryGoldGradient,
                    "Deep Bass" to listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0)),
                    "Classical" to listOf(Color(0xFF3E5151), Color(0xFFDECBA4)),
                    "Electronic" to SunsetGradient,
                    "Jazz Noir" to listOf(Color(0xFF141E30), Color(0xFF243B55))
                )

                genres.chunked(2).forEach { rowGenres ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        rowGenres.forEach { (name, gradient) ->
                            Box(modifier = Modifier.weight(1f)) {
                                GenreCard(name, gradient)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = Typography.labelMedium.copy(letterSpacing = 2.sp, fontWeight = FontWeight.Bold),
        color = WhiteMuted
    )
}

@Composable
fun RecentSearchChip(query: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        color = OnyxSurface,
        shape = CircleShape,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.History, 
                contentDescription = null, 
                modifier = Modifier.size(16.dp), 
                tint = WhiteDim
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(query, style = Typography.bodySmall, color = Color.White)
        }
    }
}

@Composable
fun RecentlyPlayedCard(song: Song, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(120.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(OnyxSurface)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.linearGradient(listOf(Color(0xFF2C2C2C), Color(0xFF1A1A1A))))
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = song.title,
            style = Typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            color = Color.White,
            maxLines = 1
        )
        Text(
            text = song.artist,
            style = Typography.bodySmall.copy(fontSize = 10.sp),
            color = WhiteMuted,
            maxLines = 1
        )
    }
}

@Composable
fun SearchResultsContent(
    songs: List<Song>,
    onSongClick: (Song) -> Unit
) {
    if (songs.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Aucun résultat trouvé", color = WhiteMuted)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            itemsIndexed(songs) { index, song ->
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
fun GenreCard(name: String, gradient: List<Color>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.6f)
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.linearGradient(gradient))
            .clickable { /* Navigate */ }
            .padding(16.dp)
    ) {
        Text(
            text = name,
            style = Typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = if (gradient == LuxuryGoldGradient) Color.Black else Color.White
            ),
            modifier = Modifier.align(Alignment.BottomStart)
        )
    }
}
