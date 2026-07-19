package com.example.sonor

import android.Manifest
import android.app.PictureInPictureParams
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import com.example.sonor.presentation.ui.components.MediaArtwork
import com.example.sonor.presentation.ui.components.SongActionSheet
import com.example.sonor.presentation.ui.components.SongAction
import com.example.sonor.domain.model.Song
import com.example.sonor.domain.model.PlaylistEntity
import com.example.sonor.domain.model.Album
import com.example.sonor.domain.model.Artist
import com.example.sonor.presentation.ui.screens.HomeScreen
import com.example.sonor.presentation.viewmodel.HomeViewModel
import com.example.sonor.presentation.ui.navigation.Screen
import com.example.sonor.ui.theme.*
import com.example.sonor.presentation.ui.screens.NowPlayingScreen
import com.example.sonor.presentation.ui.screens.EqualizerScreen
import com.example.sonor.presentation.ui.screens.LibraryScreen
import com.example.sonor.presentation.ui.screens.SearchScreen
import com.example.sonor.presentation.ui.screens.SettingsScreen
import com.example.sonor.presentation.ui.screens.LyricsScreen
import com.example.sonor.presentation.ui.screens.AlbumDetailScreen
import com.example.sonor.presentation.ui.screens.ArtistDetailScreen
import com.example.sonor.presentation.ui.screens.PlaylistDetailScreen
import com.example.sonor.presentation.folders.FolderExplorerScreen
import com.example.sonor.presentation.ui.components.MiniPlayer
import com.example.sonor.presentation.viewmodel.AuthViewModel
import com.example.sonor.presentation.ui.screens.LoginScreen
import com.example.sonor.ui.screens.SplashScreen
import org.koin.compose.viewmodel.koinViewModel

class MainActivity : ComponentActivity() {

    private val isPipMode = mutableStateOf(false)

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        checkAndRequestPermissions()

        setContent {
            val inPip by isPipMode
            SonorTheme {
                if (inPip) {
                    PipContent()
                } else {
                    RootAppFlow()
                }
            }
        }
    }

    @Composable
    fun RootAppFlow() {
        var showSplash by remember { mutableStateOf(true) }
        val authViewModel: AuthViewModel = koinViewModel()
        val isAuthenticated by authViewModel.isAuthenticated.collectAsState()

        AnimatedContent(
            targetState = showSplash,
            transitionSpec = {
                fadeIn(animationSpec = tween(700)) togetherWith fadeOut(animationSpec = tween(700))
            },
            label = "splash_transition"
        ) { isSplashing ->
            if (isSplashing) {
                SplashScreen(onAnimationFinished = { showSplash = false })
            } else {
                // Seamless Crossfade between Auth and Main App
                Crossfade(
                    targetState = isAuthenticated,
                    animationSpec = tween(1000, easing = EaseInOutQuart),
                    label = "auth_crossfade"
                ) { authenticated ->
                    if (authenticated) {
                        SonorMainApp()
                    } else {
                        LoginScreen(
                            viewModel = authViewModel,
                            onLoginSuccess = { /* State update handles this */ }
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun PipContent() {
        val homeViewModel: HomeViewModel = koinViewModel()
        val sharedPlayerState by homeViewModel.sharedPlayerState.collectAsState()
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MidnightBlack), 
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = sharedPlayerState.currentSong?.title ?: "SONOR",
                    color = SonorGoldLiquid,
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = sharedPlayerState.currentSong?.artist ?: "",
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: Configuration) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        isPipMode.value = isInPictureInPictureMode
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)) {
                enterPictureInPictureMode(
                    PictureInPictureParams.Builder()
                        .setAspectRatio(Rational(16, 9))
                        .build()
                )
            }
        }
    }

    @Composable
    fun SonorMainApp() {
        val homeViewModel: HomeViewModel = koinViewModel()
        val sharedPlayerState by homeViewModel.sharedPlayerState.collectAsState()
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination
        
        var isPlayerExpanded by remember { mutableStateOf(false) }
        var showQueueBottomSheet by remember { mutableStateOf(false) }
        var showActionSheetForSong by remember { mutableStateOf<Song?>(null) }
        var showSpeedDialog by remember { mutableStateOf(false) }
        var showTimerDialog by remember { mutableStateOf(false) }

        val currentSongId = sharedPlayerState.currentSong?.id
        val isCurrentFavorite by if (currentSongId != null) {
            homeViewModel.isFavorite(currentSongId).collectAsState(initial = false)
        } else {
            remember { mutableStateOf(false) }
        }

        BackHandler(enabled = isPlayerExpanded) {
            isPlayerExpanded = false
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MidnightBlack,
            bottomBar = {
                AnimatedVisibility(
                    visible = !isPlayerExpanded && currentDestination?.route != "equalizer",
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .navigationBarsPadding()
                            .padding(bottom = 8.dp)
                    ) {
                        if (sharedPlayerState.currentSong != null) {
                            MiniPlayer(
                                playerState = sharedPlayerState,
                                onTogglePlayPause = { homeViewModel.togglePlayPause() },
                                onSkipPrevious = { homeViewModel.skipPrevious() },
                                onSkipNext = { homeViewModel.skipNext() },
                                onClick = { isPlayerExpanded = true }
                            )
                        }
                        
                        // Ultra-Premium Custom Bottom Navigation
                        Surface(
                            modifier = Modifier
                                .padding(horizontal = 24.dp)
                                .height(68.dp)
                                .clip(RoundedCornerShape(34.dp))
                                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(34.dp)),
                            color = OnyxSurface.copy(alpha = 0.85f),
                            tonalElevation = 12.dp
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val screens = listOf(Screen.Home, Screen.Search, Screen.Folders, Screen.Library, Screen.Settings)
                                screens.forEach { screen ->
                                    val isSelected = currentDestination?.route == screen.route
                                    
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .clickable {
                                                if (!isSelected) {
                                                    navController.navigate(screen.route) {
                                                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                                                        launchSingleTop = true
                                                        restoreState = true
                                                    }
                                                }
                                            }
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(
                                                imageVector = screen.icon,
                                                contentDescription = screen.title,
                                                tint = if (isSelected) SonorGoldLiquid else WhiteMuted,
                                                modifier = Modifier.size(if (isSelected) 28.dp else 24.dp)
                                            )
                                            AnimatedVisibility(visible = isSelected) {
                                                Box(
                                                    modifier = Modifier
                                                        .padding(top = 4.dp)
                                                        .size(4.dp)
                                                        .clip(CircleShape)
                                                        .background(SonorGoldLiquid)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                NavHost(
                    navController = navController, 
                    startDestination = Screen.Home.route,
                    modifier = Modifier.fillMaxSize()
                ) {
                    composable(Screen.Home.route) {
                        HomeScreen(
                            viewModel = homeViewModel,
                            onSongClick = { song -> homeViewModel.playSong(song) },
                            onSearchClick = { navController.navigate(Screen.Search.route) },
                            onArtistClick = { artist ->
                                navController.navigate(Screen.artistDetailRoute(artist.name))
                            },
                            onAlbumClick = { album ->
                                navController.navigate(Screen.albumDetailRoute(album.name, album.artworkUri, album.artist))
                            },
                            onPlaylistClick = { playlist ->
                                navController.navigate(Screen.playlistDetailRoute(playlist.id, playlist.name, playlist.description))
                            },
                            onSettingsClick = {
                                navController.navigate(Screen.Settings.route)
                            }
                        )
                    }
                    composable(Screen.ArtistDetail.route) { backStackEntry ->
                        val artistName = backStackEntry.arguments?.getString("artistName") ?: return@composable
                        ArtistDetailScreen(
                            artistName = artistName,
                            onBackPressed = { navController.popBackStack() },
                            onSongClick = { song -> homeViewModel.playSong(song) }
                        )
                    }
                    composable(Screen.AlbumDetail.route) { backStackEntry ->
                        val albumName = backStackEntry.arguments?.getString("albumName") ?: return@composable
                        val albumArtUri = backStackEntry.arguments?.getString("albumArtUri")
                        val albumArtist = backStackEntry.arguments?.getString("albumArtist") ?: return@composable
                        AlbumDetailScreen(
                            albumName = albumName,
                            albumArtUri = albumArtUri,
                            albumArtist = albumArtist,
                            onBackPressed = { navController.popBackStack() },
                            onSongClick = { song -> homeViewModel.playSong(song) }
                        )
                    }
                    composable(Screen.PlaylistDetail.route) { backStackEntry ->
                        val playlistId = backStackEntry.arguments?.getString("playlistId")?.toLongOrNull() ?: return@composable
                        val playlistName = backStackEntry.arguments?.getString("playlistName") ?: return@composable
                        val playlistDescription = backStackEntry.arguments?.getString("playlistDescription")
                        PlaylistDetailScreen(
                            playlist = PlaylistEntity(
                                id = playlistId,
                                name = playlistName,
                                description = playlistDescription
                            ),
                            onBackPressed = { navController.popBackStack() },
                            onSongClick = { song -> homeViewModel.playSong(song) }
                        )
                    }
                    composable(Screen.Search.route) {
                        SearchScreen(
                            onSongClick = { song -> homeViewModel.playSong(song) }
                        )
                    }
                    composable(Screen.Folders.route) {
                        FolderExplorerScreen(
                            homeViewModel = homeViewModel,
                            onSongClick = { song -> homeViewModel.playSong(song) }
                        )
                    }
                    composable(Screen.Library.route) {
                        LibraryScreen(
                            onSongClick = { song -> homeViewModel.playSong(song) }
                        )
                    }
                    composable(Screen.Settings.route) {
                        SettingsScreen(
                            onLogout = { homeViewModel.logout() },
                            onEqualizerClick = { navController.navigate("equalizer") }
                        )
                    }
                    composable("equalizer") {
                        EqualizerScreen(
                            playerState    = sharedPlayerState,
                            onToggleEqualizer = { enabled -> homeViewModel.toggleEqualizer(enabled) },
                            onBandLevelChange = { band, level -> homeViewModel.setEqualizerBandLevel(band, level) },
                            onBack         = { navController.popBackStack() }
                        )
                    }
                    composable(Screen.Lyrics.route) {
                        LyricsScreen(
                            currentSong = sharedPlayerState.currentSong,
                            onBackPressed = { navController.popBackStack() },
                            onSearchLyrics = { },
                            onAddLyrics = { }
                        )
                    }
                }

                // Immersive Full Screen Player with Cinematic Transitions
                AnimatedVisibility(
                    visible = isPlayerExpanded,
                    enter = slideInVertically(
                        animationSpec = tween(700, easing = EaseInOutQuart),
                        initialOffsetY = { it }
                    ) + fadeIn(),
                    exit = slideOutVertically(
                        animationSpec = tween(700, easing = EaseInOutQuart),
                        targetOffsetY = { it }
                    ) + fadeOut()
                ) {
                    NowPlayingScreen(
                        playerState       = sharedPlayerState,
                        isFavorite        = isCurrentFavorite,
                        musicController   = homeViewModel.musicController,
                        onToggleFavorite  = {
                            sharedPlayerState.currentSong?.let {
                                homeViewModel.toggleFavorite(it)
                            }
                        },
                        onTogglePlayPause = { homeViewModel.togglePlayPause() },
                        onSkipNext        = { homeViewModel.skipNext() },
                        onSkipPrevious    = { homeViewModel.skipPrevious() },
                        onSeekTo          = { homeViewModel.seekTo(it) },
                        onToggleShuffle   = { homeViewModel.toggleShuffle() },
                        onToggleRepeat    = { homeViewModel.toggleRepeat() },
                        onBackClick       = { isPlayerExpanded = false },
                        onEqualizerClick  = { navController.navigate("equalizer") },
                        onLyricsClick     = { navController.navigate(Screen.Lyrics.route) },
                        onPlaylistClick   = { showQueueBottomSheet = true },
                        onMoreOptions     = { showActionSheetForSong = sharedPlayerState.currentSong }
                    )
                }

                @OptIn(ExperimentalMaterial3Api::class)
                if (showActionSheetForSong != null) {
                    SongActionSheet(
                        song = showActionSheetForSong!!,
                        isFavorite = isCurrentFavorite,
                        onDismiss = { showActionSheetForSong = null },
                        onAction = { action ->
                            val activeSong = showActionSheetForSong!!
                            showActionSheetForSong = null
                            when (action) {
                                is SongAction.ToggleFavorite -> {
                                    homeViewModel.toggleFavorite(activeSong)
                                }
                                is SongAction.PlaybackSpeed -> {
                                    showSpeedDialog = true
                                }
                                is SongAction.SleepTimer -> {
                                    showTimerDialog = true
                                }
                                is SongAction.Share -> {
                                    shareSong(activeSong)
                                }
                                else -> { /* other actions */ }
                            }
                        }
                    )
                }

                if (showSpeedDialog) {
                    var speed by remember { mutableFloatStateOf(sharedPlayerState.playbackSpeed) }
                    AlertDialog(
                        onDismissRequest = { showSpeedDialog = false },
                        title = { Text("Vitesse de lecture", color = WhitePure) },
                        text = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${"%.2f".format(speed)}x", style = Typography.titleLarge, color = LarkAccent)
                                Slider(
                                    value = speed,
                                    onValueChange = { speed = it },
                                    valueRange = 0.5f..2.0f,
                                    steps = 15,
                                    colors = SliderDefaults.colors(
                                        thumbColor = WhitePure,
                                        activeTrackColor = LarkAccent,
                                        inactiveTrackColor = WhiteDim
                                    )
                                )
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                homeViewModel.setPlaybackSpeed(speed)
                                showSpeedDialog = false
                            }) {
                                Text("Confirmer", color = LarkAccent)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showSpeedDialog = false }) {
                                Text("Annuler", color = WhiteMuted)
                            }
                        },
                        containerColor = OnyxSurface
                    )
                }

                if (showTimerDialog) {
                    AlertDialog(
                        onDismissRequest = { showTimerDialog = false },
                        title = { Text("Minuterie de veille", color = WhitePure) },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf(5, 10, 15, 30, 45, 60).forEach { minutes ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                homeViewModel.setSleepTimer(minutes)
                                                showTimerDialog = false
                                            }
                                            .padding(vertical = 12.dp, horizontal = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("$minutes minutes", color = WhitePure)
                                        Icon(Icons.Rounded.Schedule, contentDescription = null, tint = LarkAccent)
                                    }
                                }
                            }
                        },
                        confirmButton = {},
                        dismissButton = {
                            TextButton(onClick = { showTimerDialog = false }) {
                                Text("Annuler", color = WhiteMuted)
                            }
                        },
                        containerColor = OnyxSurface
                    )
                }

                @OptIn(ExperimentalMaterial3Api::class)
                if (showQueueBottomSheet) {
                    ModalBottomSheet(
                        onDismissRequest = { showQueueBottomSheet = false },
                        containerColor = OnyxSurface,
                        dragHandle = { BottomSheetDefaults.DragHandle(color = WhiteDim) }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .navigationBarsPadding()
                                .padding(horizontal = 24.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "File d'attente",
                                style = Typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = WhitePure,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            
                            val queue = sharedPlayerState.currentQueue
                            if (queue.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("La file d'attente est vide", color = WhiteMuted)
                                }
                            } else {
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxHeight(0.6f)
                                ) {
                                    items(queue) { song ->
                                        val isCurrent = song.id == sharedPlayerState.currentSongId
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isCurrent) LarkAccent.copy(alpha = 0.1f) else Color.Transparent)
                                                .clickable {
                                                    homeViewModel.playSong(song)
                                                }
                                                .padding(8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(OnyxSurface),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                MediaArtwork(
                                                    artworkUri = song.albumArtUri,
                                                    contentDescription = null,
                                                    modifier = Modifier.fillMaxSize(),
                                                    placeholderIconSize = 20.dp,
                                                    mediaType = song.type,
                                                    songUri = song.uri
                                                )
                                            }
                                            
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = song.title,
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                                                    ),
                                                    color = if (isCurrent) LarkAccent else WhitePure,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = song.artist,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = WhiteMuted,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                            
                                            if (isCurrent) {
                                                Icon(
                                                    imageVector = Icons.Rounded.VolumeUp,
                                                    contentDescription = "En lecture",
                                                    tint = LarkAccent,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun checkAndRequestPermissions() {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.READ_MEDIA_AUDIO)
            permissions.add(Manifest.permission.READ_MEDIA_VIDEO)
        } else {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        requestPermissionLauncher.launch(permissions.toTypedArray())
    }

    private fun shareSong(song: Song) {
        val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "audio/*"
            putExtra(android.content.Intent.EXTRA_STREAM, android.net.Uri.parse(song.uri))
            putExtra(android.content.Intent.EXTRA_TITLE, song.title)
            putExtra(android.content.Intent.EXTRA_SUBJECT, "${song.title} - ${song.artist}")
        }
        startActivity(android.content.Intent.createChooser(shareIntent, "Partager avec"))
    }
}
