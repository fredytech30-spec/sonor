package com.example.sonor.presentation.folders

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.compose.viewmodel.koinViewModel
import com.example.sonor.domain.model.Song
import com.example.sonor.domain.model.PlatformFile
import com.example.sonor.presentation.viewmodel.HomeViewModel
import com.example.sonor.ui.theme.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@Composable
fun FolderExplorerScreen(
    homeViewModel: HomeViewModel = koinViewModel(),
    onSongClick: (Song) -> Unit
) {
    var currentFolderPath by remember { mutableStateOf<String?>(null) }
    val folders by homeViewModel.folders.collectAsState()
    
    val songsInFolder by remember(currentFolderPath) {
        if (currentFolderPath != null) {
            homeViewModel.getSongsInFolder(currentFolderPath!!)
        } else {
            flowOf(emptyList<Song>())
        }
    }.collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightBlack)
            .statusBarsPadding()
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        
        val currentFolder = folders.find { it.path == currentFolderPath }
        Text(
            text = if (currentFolderPath == null) "DOSSIERS" else currentFolder?.name?.uppercase() ?: "DOSSIER",
            style = Typography.displayLarge.copy(
                fontSize = 28.sp,
                letterSpacing = 4.sp,
                brush = Brush.linearGradient(LuxuryGoldGradient)
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (currentFolderPath == null) {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(folders) { folder ->
                    FolderItem(folder) { currentFolderPath = folder.path }
                }
            }
        } else {
            TextButton(onClick = { currentFolderPath = null }) {
                Text("< Retour aux dossiers", color = SonorGoldLiquid)
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(songsInFolder) { song ->
                    SongItemSimple(song) { onSongClick(song) }
                }
            }
        }
    }
}

@Composable
fun FolderItem(folder: PlatformFile, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Rounded.Folder, contentDescription = null, tint = SonorGoldLiquid, modifier = Modifier.size(32.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(folder.name, style = Typography.bodyLarge, color = WhitePure)
            Text(folder.path, style = Typography.labelSmall, color = WhiteDim, maxLines = 1)
        }
    }
}

@Composable
fun SongItemSimple(song: Song, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Rounded.MusicNote, contentDescription = null, tint = WhiteDim)
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(song.title, style = Typography.bodyLarge, color = WhitePure, maxLines = 1)
            Text(song.artist, style = Typography.labelMedium, color = WhiteMuted)
        }
    }
}
