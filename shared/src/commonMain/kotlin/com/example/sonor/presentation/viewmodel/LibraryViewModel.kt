package com.example.sonor.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sonor.domain.model.Song
import com.example.sonor.domain.model.PlaylistEntity
import com.example.sonor.domain.repository.SongRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class LibraryViewModel(
    private val repository: SongRepository
) : ViewModel() {

    val artists = repository.getArtists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val albums = repository.getAlbums()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playlists = repository.getAllPlaylists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val folders = repository.getFolders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createPlaylist(name: String, description: String? = null) = viewModelScope.launch {
        repository.createPlaylist(name, description)
    }

    fun deletePlaylist(playlist: PlaylistEntity) = viewModelScope.launch {
        repository.deletePlaylist(playlist)
    }

    fun addToPlaylist(songId: Long, playlistId: Long) = viewModelScope.launch {
        repository.addSongToPlaylist(songId, playlistId)
    }

    fun deleteSong(song: Song) = viewModelScope.launch {
        repository.deleteSong(song)
    }
    
    fun updateSongTags(songId: Long, title: String, artist: String) = viewModelScope.launch {
        // Feature: Tag Editor logic in repository
    }
}
