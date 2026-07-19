package com.example.sonor.presentation.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Search : Screen("search", "Search", Icons.Default.Search)
    object Folders : Screen("folders", "Folders", Icons.Default.Folder)
    object Library : Screen("library", "Library", Icons.Default.LibraryMusic)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    
    object ArtistDetail : Screen("artist/{artistName}", "Artist", Icons.Default.LibraryMusic)
    object AlbumDetail : Screen("album/{albumName}/{albumArtUri}/{albumArtist}", "Album", Icons.Default.LibraryMusic)
    object PlaylistDetail : Screen("playlist/{playlistId}/{playlistName}/{playlistDescription}", "Playlist", Icons.Default.LibraryMusic)
    object Lyrics : Screen("lyrics", "Lyrics", Icons.Default.LibraryMusic)
    
    companion object {
        fun artistDetailRoute(artistName: String) = "artist/$artistName"
        fun albumDetailRoute(albumName: String, albumArtUri: String?, albumArtist: String) = 
            "album/$albumName/${albumArtUri ?: "null"}/$albumArtist"
        fun playlistDetailRoute(playlistId: Long, playlistName: String, description: String?) = 
            "playlist/$playlistId/$playlistName/${description ?: "null"}"
    }
}
