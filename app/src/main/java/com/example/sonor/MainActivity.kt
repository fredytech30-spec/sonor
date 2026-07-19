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
import com.example.sonor.ui.components.MiniPlayer
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
                        onPlaylistClick   = { },
                        onMoreOptions     = { }
                    )
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
}
