package com.example.sonor.presentation.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sonor.domain.model.MediaType
import com.example.sonor.domain.model.Song
import com.example.sonor.domain.model.Artist
import com.example.sonor.domain.model.Album
import com.example.sonor.domain.model.PlaylistEntity
import com.example.sonor.domain.model.PlatformFile
import com.example.sonor.presentation.viewmodel.HomeViewModel
import com.example.sonor.presentation.viewmodel.HomeUiState
import com.example.sonor.presentation.ui.components.MediaArtwork
import com.example.sonor.ui.theme.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onSongClick: (Song) -> Unit,
    onSearchClick: () -> Unit,
    onArtistClick: (Artist) -> Unit,
    onAlbumClick: (Album) -> Unit,
    onPlaylistClick: (PlaylistEntity) -> Unit,
    onSettingsClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val mostPlayed by viewModel.mostPlayed.collectAsState()
    val recentlyPlayed by viewModel.recentlyPlayed.collectAsState()
    val artists by viewModel.artists.collectAsState()
    val albums by viewModel.albums.collectAsState()
    val playlists by viewModel.playlists.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightBlack)
    ) {
        // Luxury Background Gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(LarkAccent.copy(alpha = 0.2f), Color.Transparent)
                    )
                )
        )

        when (val state = uiState) {
            is HomeUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = LarkAccent)
                }
            }
            is HomeUiState.Success -> {
                HomeContent(
                    viewModel = viewModel,
                    state = state,
                    mostPlayed = mostPlayed,
                    recentlyPlayed = recentlyPlayed,
                    artists = artists,
                    albums = albums,
                    playlists = playlists,
                    onSongClick = { song ->
                        viewModel.playSong(song)
                        onSongClick(song)
                    },
                    onSearchClick = onSearchClick,
                    onArtistClick = onArtistClick,
                    onAlbumClick = onAlbumClick,
                    onPlaylistClick = onPlaylistClick,
                    onSettingsClick = onSettingsClick
                )
            }
            is HomeUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = state.message, color = FavoriteRed)
                }
            }
        }
    }
}

@Composable
fun HomeContent(
    viewModel: HomeViewModel,
    state: HomeUiState.Success,
    mostPlayed: List<Song>,
    recentlyPlayed: List<Song>,
    artists: List<Artist>,
    albums: List<Album>,
    playlists: List<PlaylistEntity>,
    onSongClick: (Song) -> Unit,
    onSearchClick: () -> Unit,
    onArtistClick: (Artist) -> Unit,
    onAlbumClick: (Album) -> Unit,
    onPlaylistClick: (PlaylistEntity) -> Unit,
    onSettingsClick: () -> Unit
) {
    val tabs = listOf("Vidéos", "Chansons", "Playlists", "Dossiers", "Artistes", "Albums")
    val pagerState = rememberPagerState(pageCount = { tabs.size })

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 100.dp)
        ) { pageIndex ->
            when (pageIndex) {
                0 -> { // Vidéos
                    VideosTabContent(
                        songs = state.songs.filter { it.type == MediaType.VIDEO },
                        onSongClick = onSongClick
                    )
                }
                1 -> { // Chansons
                    ChansonsTabContent(
                        state = state,
                        mostPlayed = mostPlayed,
                        recentlyPlayed = recentlyPlayed,
                        onSongClick = onSongClick
                    )
                }
                2 -> { // Playlists
                    PlaylistsTabContent(playlists = playlists, onPlaylistClick = onPlaylistClick)
                }
                3 -> { // Dossiers
                    FoldersTabContent(
                        viewModel = viewModel,
                        onSongClick = onSongClick
                    )
                }
                4 -> { // Artistes
                    ArtistsTabContent(artists = artists, onArtistClick = onArtistClick)
                }
                5 -> { // Albums
                    AlbumsTabContent(albums = albums, onAlbumClick = onAlbumClick)
                }
            }
        }

        // Floating Glass Header with Tabs
        Column {
            GlassHeader(tabs, pagerState, onSearchClick, onSettingsClick)
        }
    }
}

@Composable
fun ArtistsTabContent(artists: List<Artist>, onArtistClick: (Artist) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp, start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (artists.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("Aucun artiste trouvé", style = MaterialTheme.typography.bodyLarge, color = WhiteMuted)
                }
            }
        } else {
            items(artists) { artist ->
                ArtistCard(artist = artist, onArtistClick = { onArtistClick(artist) })
            }
        }
    }
}

@Composable
fun ArtistCard(artist: Artist, onArtistClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onArtistClick),
        shape = RoundedCornerShape(24.dp),
        color = OnyxSurface,
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(GlassSurface),
                contentAlignment = Alignment.Center
            ) {
                // Show first song artwork for artist if available, else person icon
                Icon(
                    imageVector = Icons.Rounded.Person,
                    contentDescription = artist.name,
                    tint = WhiteMuted,
                    modifier = Modifier.size(32.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = artist.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = WhitePure,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${artist.songCount} ${if (artist.songCount == 1) "chanson" else "chansons"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = WhiteMuted
                )
            }
        }
    }
}

@Composable
fun AlbumsTabContent(albums: List<Album>, onAlbumClick: (Album) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp, start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (albums.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("Aucun album trouvé", style = MaterialTheme.typography.bodyLarge, color = WhiteMuted)
                }
            }
        } else {
            items(albums) { album ->
                AlbumCard(album = album, onAlbumClick = { onAlbumClick(album) })
            }
        }
    }
}

@Composable
fun AlbumCard(album: Album, onAlbumClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onAlbumClick),
        shape = RoundedCornerShape(24.dp),
        color = OnyxSurface,
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(GlassSurface)
            ) {
                // Album art via MediaArtwork (Coil) — falls back to album icon
                MediaArtwork(
                    artworkUri = album.artworkUri,
                    contentDescription = album.name,
                    modifier = Modifier.fillMaxSize(),
                    placeholderIconSize = 32.dp,
                    mediaType = MediaType.AUDIO,
                    songUri = null
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = album.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = WhitePure,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = album.artist,
                    style = MaterialTheme.typography.bodyMedium,
                    color = WhiteMuted
                )
                Text(
                    text = "${album.songCount} ${if (album.songCount == 1) "chanson" else "chansons"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = WhiteMuted
                )
            }
        }
    }
}

@Composable
fun PlaylistsTabContent(playlists: List<PlaylistEntity>, onPlaylistClick: (PlaylistEntity) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp, start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (playlists.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("Aucune playlist trouvée", style = MaterialTheme.typography.bodyLarge, color = WhiteMuted)
                }
            }
        } else {
            items(playlists) { playlist ->
                PlaylistCard(playlist = playlist, onPlaylistClick = { onPlaylistClick(playlist) })
            }
        }
    }
}

@Composable
fun PlaylistCard(playlist: PlaylistEntity, onPlaylistClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onPlaylistClick),
        shape = RoundedCornerShape(24.dp),
        color = OnyxSurface,
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                modifier = Modifier.size(64.dp),
                color = GlassSurface,
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.QueueMusic,
                        contentDescription = playlist.name,
                        tint = WhiteMuted,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = playlist.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = WhitePure,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Playlist",
                    style = MaterialTheme.typography.bodyMedium,
                    color = WhiteMuted
                )
            }
        }
    }
}

@Composable
fun FoldersTabContent(
    viewModel: HomeViewModel,
    onSongClick: (Song) -> Unit
) {
    var currentFolder by remember { mutableStateOf<PlatformFile?>(null) }
    val folders by viewModel.folders.collectAsState()
    val songsInFolder by remember(currentFolder) {
        if (currentFolder != null) {
            viewModel.getSongsInFolder(currentFolder!!.path)
        } else {
            flowOf(emptyList<Song>())
        }
    }.collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (currentFolder == null) {
            Text(
                text = "DOSSIERS",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = LarkAccent
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(folders) { folder ->
                    FolderCard(folder = folder, onClick = { currentFolder = folder })
                }
            }
        } else {
            TextButton(onClick = { currentFolder = null }) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = null, tint = LarkAccent)
                Spacer(modifier = Modifier.width(8.dp))
                Text("< Retour aux dossiers", color = LarkAccent)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = currentFolder!!.name.uppercase(),
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = WhitePure
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(songsInFolder) { song ->
                    SongItem(
                        song = song,
                        onClick = { onSongClick(song) }
                    )
                }
            }
        }
    }
}

@Composable
fun FolderCard(folder: PlatformFile, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = OnyxSurface,
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                modifier = Modifier.size(64.dp),
                shape = RoundedCornerShape(16.dp),
                color = GlassSurface
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Folder,
                        contentDescription = folder.name,
                        tint = LarkAccent,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = folder.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = WhitePure,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = folder.path,
                    style = MaterialTheme.typography.bodySmall,
                    color = WhiteMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun VideosTabContent(
    songs: List<Song>,
    onSongClick: (Song) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 120.dp, top = 16.dp)
    ) {
        if (songs.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("Aucune vidéo trouvée", style = MaterialTheme.typography.bodyLarge, color = WhiteMuted)
                }
            }
        } else {
            itemsIndexed(songs) { index, song ->
                SongItem(
                    song = song,
                    onClick = { onSongClick(song) }
                )
            }
        }
    }
}

@Composable
fun ChansonsTabContent(
    state: HomeUiState.Success,
    mostPlayed: List<Song>,
    recentlyPlayed: List<Song>,
    onSongClick: (Song) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 120.dp, top = 16.dp)
    ) {
        item {
            HeaderSection(state.audioCount, state.videoCount)
        }

        // Section: Recently Played
        if (recentlyPlayed.isNotEmpty()) {
            item {
                SectionTitle(title = "REPRENDRE LA LECTURE")
                FeaturedRow(recentlyPlayed.take(10), onSongClick)
            }
        }

        // Section: Most Played
        if (mostPlayed.isNotEmpty()) {
            item {
                SectionTitle(title = "VOS FAVORIS DU MOMENT")
                FeaturedRow(mostPlayed.take(10), onSongClick)
            }
        }

        item {
            SectionTitle(title = "TOUTE LA BIBLIOTHÈQUE")
        }

        itemsIndexed(state.songs.filter { it.type == MediaType.AUDIO }) { index, song ->
            SongItem(
                song = song,
                onClick = { onSongClick(song) }
            )
        }
    }
}

@Composable
fun GlassHeader(
    tabs: List<String>,
    pagerState: androidx.compose.foundation.pager.PagerState,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(1) } // Default to "Chansons"
    val scope = rememberCoroutineScope()

    // Sync tab indicator to pager scroll
    LaunchedEffect(pagerState.currentPage) {
        selectedTabIndex = pagerState.currentPage
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth(),
        color = MidnightBlack.copy(alpha = 0.95f),
    ) {
        Column {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Logo and App Name
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Simple Logo
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        color = LarkAccent,
                        tonalElevation = 8.dp
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MusicNote,
                            contentDescription = "Logo",
                            tint = MidnightBlack,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    Text(
                        text = "Sonor",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = WhitePure
                        )
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onSearchClick,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(Icons.Rounded.Search, contentDescription = "Rechercher", tint = WhitePure, modifier = Modifier.size(24.dp))
                    }
                    IconButton(
                        onClick = {},
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(Icons.Rounded.Sort, contentDescription = "Trier", tint = WhitePure, modifier = Modifier.size(24.dp))
                    }
                    IconButton(
                        onClick = onSettingsClick,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(Icons.Rounded.Settings, contentDescription = "Paramètres", tint = WhitePure, modifier = Modifier.size(24.dp))
                    }
                }
            }

            // Tabs Row
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.Transparent,
                contentColor = Color.White,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = LarkAccent
                    )
                },
                divider = {}
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = index == selectedTabIndex,
                        onClick = {
                            selectedTabIndex = index
                            // Scroll pager to the selected tab
                            scope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (index == selectedTabIndex) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        },
                        selectedContentColor = WhitePure,
                        unselectedContentColor = WhiteMuted
                    )
                }
            }
        }
    }
}

@Composable
fun HeaderSection(audioCount: Int, videoCount: Int) {
    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
        Text(
            text = "Bonjour,",
            style = MaterialTheme.typography.titleLarge,
            color = WhiteMuted
        )
        Text(
            text = "Votre univers Sonor",
            style = MaterialTheme.typography.displayLarge.copy(fontSize = 32.sp),
            color = Color.White
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Media Counter Badges
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MediaBadge(icon = Icons.Rounded.MusicNote, count = audioCount, label = "Audios")
            MediaBadge(icon = Icons.Rounded.Videocam, count = videoCount, label = "Vidéos")
        }
    }
}

@Composable
fun MediaBadge(icon: androidx.compose.ui.graphics.vector.ImageVector, count: Int, label: String) {
    Surface(
        color = OnyxSurface,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = LarkAccent)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "$count $label",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White
            )
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 2.sp, fontWeight = FontWeight.Bold),
        color = WhiteMuted,
        modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 32.dp, bottom = 16.dp)
    )
}

@Composable
fun FeaturedRow(songs: List<Song>, onSongClick: (Song) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        items(songs) { song ->
            FeaturedCard(song, onSongClick)
        }
    }
}

@Composable
fun FeaturedCard(song: Song, onClick: (Song) -> Unit) {
    Column(
        modifier = Modifier
            .width(150.dp)
            .clickable { onClick(song) }
    ) {
        Box(
            modifier = Modifier
                .size(150.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(OnyxSurface)
        ) {
            MediaArtwork(
                artworkUri = song.albumArtUri,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                placeholderIconSize = 48.dp,
                mediaType = song.type,
                songUri = song.uri
            )

            // Video Indicator
            if (song.type == MediaType.VIDEO) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Rounded.Videocam, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.White)
                }
            }

            // Play icon overlay
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp)
                    .size(40.dp)
                    .background(
                        brush = Brush.linearGradient(LarkBlueGradient),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = song.title,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = song.artist,
            style = MaterialTheme.typography.labelSmall,
            color = WhiteMuted,
            maxLines = 1
        )
    }
}

@Composable
fun SongItem(
    song: Song,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Thumbnail 56dp with video/audio badge overlay
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(OnyxSurface)
            ) {
                MediaArtwork(
                    artworkUri = song.albumArtUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    placeholderIconSize = 28.dp,
                    mediaType = song.type,
                    songUri = song.uri
                )
                // Badge VIDEO en haut à droite
                if (song.type == MediaType.VIDEO) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(3.dp)
                            .background(
                                color = LarkAccent.copy(alpha = 0.9f),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 3.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "VID",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                            color = MidnightBlack,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = WhitePure,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = song.artist,
                        style = MaterialTheme.typography.bodySmall,
                        color = WhiteMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (song.duration > 0) {
                        Text(
                            text = "• ${formatSongDuration(song.duration)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = WhiteDim
                        )
                    }
                }
            }

            // More options button
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = "Options",
                tint = WhiteDim,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/** Formats milliseconds as mm:ss */
private fun formatSongDuration(durationMs: Long): String {
    val totalSec = (durationMs / 1000).coerceAtLeast(0)
    val min = totalSec / 60
    val sec = totalSec % 60
    return "%d:%02d".format(min, sec)
}
