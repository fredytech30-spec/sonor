import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.compose.ui.unit.DpSize
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.sonor.di.desktopModule
import com.example.sonor.di.sharedModule
import com.example.sonor.domain.model.PlayerState
import com.example.sonor.presentation.ui.screens.*
import com.example.sonor.presentation.viewmodel.AuthViewModel
import com.example.sonor.presentation.viewmodel.HomeViewModel
import com.example.sonor.ui.screens.SplashScreen
import com.example.sonor.ui.theme.*
import org.koin.core.context.startKoin
import org.koin.compose.koinInject

fun main() = application {
    // Initialize Koin
    startKoin {
        modules(sharedModule, desktopModule)
    }

    val windowState = rememberWindowState(
        size = DpSize(width = 1280.dp, height = 820.dp)
    )

    Window(
        title       = "Sonor — Lecteur Audio & Vidéo Premium",
        state       = windowState,
        onCloseRequest = ::exitApplication
    ) {
        SonorTheme {
            var showSplash by remember { mutableStateOf(true) }
            val authViewModel: AuthViewModel = koinInject()
            val isAuthenticated by authViewModel.isAuthenticated.collectAsState(initial = false)

            if (showSplash) {
                SplashScreen(onAnimationFinished = { showSplash = false })
            } else {
                Crossfade(
                    targetState  = isAuthenticated,
                    animationSpec = tween(800, easing = FastOutSlowInEasing),
                    label        = "auth_crossfade"
                ) { auth ->
                    if (auth) {
                        DesktopMainApp(onLogout = { authViewModel.logout() })
                    } else {
                        LoginScreen(
                            viewModel      = authViewModel,
                            onLoginSuccess = {}
                        )
                    }
                }
            }
        }
    }
}

// ─── Desktop Main App with full Navigation ─────────────────────────────────────

@Composable
fun DesktopMainApp(onLogout: () -> Unit) {
    val homeViewModel: HomeViewModel = koinInject()
    val sharedPlayerState by homeViewModel.sharedPlayerState.collectAsState()
    val navController = rememberNavController()

    var isPlayerExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightBlack)
    ) {
        NavHost(
            navController    = navController,
            startDestination = "home",
            modifier         = Modifier.fillMaxSize()
        ) {
            composable("home") {
                HomeScreen(
                    viewModel      = homeViewModel,
                    onSongClick    = { song -> homeViewModel.playSong(song) },
                    onSearchClick  = { navController.navigate("search") },
                    onArtistClick  = { artist -> navController.navigate("artist/${artist.name}") },
                    onAlbumClick   = { album  -> navController.navigate("album/${album.name}") },
                    onPlaylistClick = { playlist -> navController.navigate("playlist/${playlist.id}") }
                )
            }

            composable("search") {
                SearchScreen(
                    onSongClick = { song -> homeViewModel.playSong(song) }
                )
            }

            composable("library") {
                LibraryScreen(
                    viewModel   = homeViewModel,
                    onSongClick = { song -> homeViewModel.playSong(song) }
                )
            }

            composable("settings") {
                SettingsScreen(
                    onLogout        = onLogout,
                    onEqualizerClick = { navController.navigate("equalizer") }
                )
            }

            composable("equalizer") {
                EqualizerScreen(
                    playerState       = sharedPlayerState,
                    onToggleEqualizer = { enabled -> homeViewModel.toggleEqualizer(enabled) },
                    onBack            = { navController.popBackStack() }
                )
            }

            composable("lyrics") {
                LyricsScreen(
                    currentSong    = sharedPlayerState.currentSong,
                    onBackPressed  = { navController.popBackStack() },
                    onSearchLyrics = { /* TODO */ },
                    onAddLyrics    = { /* TODO */ }
                )
            }
        }

        // ── Floating Now Playing Overlay (Desktop) ──────────────────────────────
        if (sharedPlayerState.currentSong != null && isPlayerExpanded) {
            NowPlayingScreen(
                playerState       = sharedPlayerState,
                isFavorite        = false,
                onToggleFavorite  = { /* TODO */ },
                onTogglePlayPause = { homeViewModel.togglePlayPause() },
                onSkipNext        = { homeViewModel.skipNext() },
                onSkipPrevious    = { homeViewModel.skipPrevious() },
                onSeekTo          = { homeViewModel.seekTo(it) },
                onToggleShuffle   = { homeViewModel.toggleShuffle() },
                onToggleRepeat    = { homeViewModel.toggleRepeat() },
                onBackClick       = { isPlayerExpanded = false },
                onEqualizerClick  = { navController.navigate("equalizer") },
                onLyricsClick     = { navController.navigate("lyrics") },
                onPlaylistClick   = { /* TODO */ }
            )
        }

        // ── Mini player bar at bottom ────────────────────────────────────────────
        if (sharedPlayerState.currentSong != null && !isPlayerExpanded) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Surface(
                    shape        = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                    color        = OnyxSurface.copy(alpha = 0.95f),
                    tonalElevation = 12.dp
                ) {
                    Row(
                        modifier              = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 12.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text  = sharedPlayerState.currentSong?.title ?: "",
                                style = Typography.titleMedium,
                                color = WhitePure
                            )
                            Text(
                                text  = sharedPlayerState.currentSong?.artist ?: "",
                                style = Typography.bodySmall,
                                color = WhiteMuted
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(onClick = { homeViewModel.skipPrevious() }) {
                                Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Rounded.SkipPrevious,
                                    contentDescription = "Précédent",
                                    tint = WhitePure
                                )
                            }
                            IconButton(onClick = { homeViewModel.togglePlayPause() }) {
                                Icon(
                                    imageVector = if (sharedPlayerState.isPlaying)
                                        androidx.compose.material.icons.Icons.Rounded.Pause
                                    else
                                        androidx.compose.material.icons.Icons.Rounded.PlayArrow,
                                    contentDescription = "Lecture/Pause",
                                    tint = WhitePure
                                )
                            }
                            IconButton(onClick = { homeViewModel.skipNext() }) {
                                Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Rounded.SkipNext,
                                    contentDescription = "Suivant",
                                    tint = WhitePure
                                )
                            }
                            IconButton(onClick = { isPlayerExpanded = true }) {
                                Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Rounded.OpenInFull,
                                    contentDescription = "Ouvrir le lecteur",
                                    tint = WhiteMuted
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
