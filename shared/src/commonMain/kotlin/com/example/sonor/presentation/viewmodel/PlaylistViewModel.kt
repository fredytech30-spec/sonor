package com.example.sonor.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sonor.domain.model.PlaylistEntity
import com.example.sonor.domain.model.Song
import com.example.sonor.domain.repository.SongRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlaylistViewModel(
    private val songRepository: SongRepository
) : ViewModel() {

    val playlists = songRepository.getAllPlaylists()

    private val _selectedPlaylistSongs = MutableStateFlow<List<Song>>(emptyList())
    val selectedPlaylistSongs: StateFlow<List<Song>> = _selectedPlaylistSongs.asStateFlow()

    fun createPlaylist(name: String) {
        viewModelScope.launch {
            songRepository.createPlaylist(name)
        }
    }

    fun deletePlaylist(playlist: PlaylistEntity) {
        viewModelScope.launch {
            songRepository.deletePlaylist(playlist)
        }
    }

    fun loadSongsForPlaylist(playlistId: Long) {
        viewModelScope.launch {
            songRepository.getSongsForPlaylist(playlistId).collect {
                _selectedPlaylistSongs.value = it
            }
        }
    }

    fun addSongToPlaylist(songId: Long, playlistId: Long) {
        viewModelScope.launch {
            songRepository.addSongToPlaylist(songId, playlistId)
        }
    }
}
