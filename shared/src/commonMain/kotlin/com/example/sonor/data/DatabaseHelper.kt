package com.example.sonor.data

import com.example.sonor.domain.model.*
import kotlinx.coroutines.flow.Flow

interface DatabaseHelper {
    // Favorites
    fun getAllFavorites(): Flow<List<FavoriteSongEntity>>
    suspend fun toggleFavorite(song: FavoriteSongEntity)
    fun isFavorite(songId: Long): Flow<Boolean>
    
    // Playlists
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>
    suspend fun createPlaylist(playlist: PlaylistEntity)
    suspend fun deletePlaylist(playlist: PlaylistEntity)
    suspend fun addSongToPlaylist(crossRef: PlaylistSongCrossRef)
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long)
    fun getSongsForPlaylist(playlistId: Long): Flow<List<FavoriteSongEntity>>
    
    // Stats
    suspend fun incrementPlayCount(songId: Long)
    suspend fun addToHistory(songId: Long)
    fun getRecentlyPlayed(): Flow<List<SongStatsEntity>>
    fun getMostPlayed(): Flow<List<SongStatsEntity>>
    
    // Library
    suspend fun excludeFolder(folder: ExcludedFolderEntity)
    fun getExcludedFolders(): Flow<List<String>>
    suspend fun includeFolder(folder: ExcludedFolderEntity)
    suspend fun saveLyrics(lyrics: LyricsEntity)
    suspend fun getLyricsForSong(songId: Long): LyricsEntity?
    
    // Prefs
    suspend fun saveEqPreset(preset: EqPresetEntity)
    fun getAllEqPresets(): Flow<List<EqPresetEntity>>
    suspend fun addRecentSearch(search: RecentSearchEntity)
    fun getRecentSearches(): Flow<List<RecentSearchEntity>>
}
