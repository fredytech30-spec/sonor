package com.example.sonor.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sonor.audio.MusicController
import com.example.sonor.domain.model.MediaType
import com.example.sonor.domain.model.PlayerState
import com.example.sonor.domain.model.Song
import com.example.sonor.domain.repository.SongRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(
        val songs: List<Song>,
        val audioCount: Int,
        val videoCount: Int
    ) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

class HomeViewModel(
    private val songRepository: SongRepository,
    private val musicController: MusicController
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // Source of truth for playback
    val playerState: StateFlow<PlayerState> = musicController.playerState

    // Enrich: replace currentSong (id) by full Song from library when possible
    val sharedPlayerState: StateFlow<PlayerState> =
        combine(playerState, uiState) { playerState, uiState ->
            val songs = when (uiState) {
                is HomeUiState.Success -> uiState.songs
                else -> emptyList()
            }
            val currentSong = songs.find { it.id == playerState.currentSongId }
            playerState.copy(currentSong = currentSong)
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            PlayerState()
        )

    // Stats & Smart Content
    val mostPlayed = songRepository.getMostPlayed()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentlyPlayed = songRepository.getRecentlyPlayed()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteSongs = songRepository.getFavoriteSongs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val folders = songRepository.getFolders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val artists = songRepository.getArtists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val albums = songRepository.getAlbums()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playlists = songRepository.getAllPlaylists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadSongs()
    }

    private fun loadSongs() {
        viewModelScope.launch {
            songRepository.getAllSongs().collect { songs ->
                _uiState.value = HomeUiState.Success(
                    songs = songs,
                    audioCount = songs.count { it.type == MediaType.AUDIO },
                    videoCount = songs.count { it.type == MediaType.VIDEO }
                )
            }
        }
    }

    fun playSong(song: Song) {
        viewModelScope.launch {
            songRepository.incrementPlayCount(song.id)
            songRepository.addToHistory(song.id)
        }
        musicController.play(song)
    }

    fun playSongs(songs: List<Song>, shuffle: Boolean = false) {
        if (songs.isEmpty()) return

        viewModelScope.launch {
            songRepository.incrementPlayCount(songs.first().id)
            songRepository.addToHistory(songs.first().id)
        }

        musicController.setShuffleMode(shuffle)
        musicController.play(songs, startIndex = 0)
    }

    fun togglePlayPause() {
        if (playerState.value.isPlaying) {
            musicController.pause()
        } else {
            musicController.resume()
        }
    }

    fun skipNext() {
        musicController.skipNext()
    }

    fun skipPrevious() {
        musicController.skipPrevious()
    }

    fun seekTo(position: Long) {
        musicController.seekTo(position)
    }

    fun toggleShuffle() {
        val next = !playerState.value.shuffleModeEnabled
        musicController.setShuffleMode(next)
    }

    fun toggleRepeat() {
        val nextMode = when (playerState.value.repeatMode) {
            PlayerState.REPEAT_MODE_OFF -> PlayerState.REPEAT_MODE_ALL
            PlayerState.REPEAT_MODE_ALL -> PlayerState.REPEAT_MODE_ONE
            else -> PlayerState.REPEAT_MODE_OFF
        }
        musicController.setRepeatMode(nextMode)
    }

    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            songRepository.toggleFavorite(
                song.id,
                song.title,
                song.artist,
                song.albumArtUri ?: ""
            )
        }
    }

    fun isFavorite(songId: Long): Flow<Boolean> = songRepository.isFavorite(songId)

    fun toggleEqualizer(enabled: Boolean) {
        musicController.setEqualizerEnabled(enabled)
    }

    fun getSongsInFolder(folderPath: String): Flow<List<Song>> =
        songRepository.getSongsInFolder(folderPath)

    fun logout() {
        // Logout handled by AuthViewModel; HomeViewModel resets playback
        musicController.pause()
    }

    fun setPlaybackSpeed(speed: Float) {
        musicController.setPlaybackSpeed(speed)
    }

    fun setSleepTimer(minutes: Int) {
        musicController.setSleepTimer(minutes)
    }
}
