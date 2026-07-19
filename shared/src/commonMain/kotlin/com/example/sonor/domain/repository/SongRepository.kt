package com.example.sonor.domain.repository

import com.example.sonor.domain.model.*
import kotlinx.coroutines.flow.Flow

interface SongRepository {
    // --- Basic Song & Library Management ---
    fun getAllSongs(): Flow<List<Song>>
    fun getFavoriteSongs(): Flow<List<Song>>
    suspend fun toggleFavorite(songId: Long, title: String, artist: String, albumArtUri: String)
    fun isFavorite(songId: Long): Flow<Boolean>
    suspend fun deleteSong(song: Song)
    
    // --- Playlist Management ---
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>
    suspend fun createPlaylist(name: String, description: String? = null)
    suspend fun deletePlaylist(playlist: PlaylistEntity)
    suspend fun addSongToPlaylist(songId: Long, playlistId: Long)
    suspend fun removeSongFromPlaylist(songId: Long, playlistId: Long)
    fun getSongsForPlaylist(playlistId: Long): Flow<List<Song>>

    // --- Folder Explorer & Exclusion ---
    fun getFolders(): Flow<List<PlatformFile>>
    fun getSongsInFolder(folderPath: String): Flow<List<Song>>
    suspend fun excludeFolder(path: String)
    suspend fun includeFolder(path: String)
    fun getExcludedFolders(): Flow<List<String>>

    // --- Artists & Albums ---
    fun getArtists(): Flow<List<Artist>>
    fun getAlbums(): Flow<List<Album>>
    fun getSongsByArtist(artistName: String): Flow<List<Song>>
    fun getSongsByAlbum(albumId: Long): Flow<List<Song>>

    // --- Stats & History ---
    suspend fun incrementPlayCount(songId: Long)
    suspend fun addToHistory(songId: Long)
    fun getRecentlyPlayed(): Flow<List<Song>>
    fun getMostPlayed(): Flow<List<Song>>

    // --- Lyrics & Meta ---
    suspend fun saveLyrics(songId: Long, lyrics: String, isSynced: Boolean)
    suspend fun getLyrics(songId: Long): LyricsEntity?

    // --- Prefs & Search ---
    suspend fun saveEqPreset(name: String, gains: List<Float>)
    fun getEqPresets(): Flow<List<EqPresetEntity>>
    suspend fun addRecentSearch(query: String)
    fun getRecentSearches(): Flow<List<RecentSearchEntity>>
}
