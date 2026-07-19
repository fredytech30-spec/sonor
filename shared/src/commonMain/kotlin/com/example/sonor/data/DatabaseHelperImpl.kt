package com.example.sonor.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import com.example.sonor.domain.model.*
import com.example.sonor.sqldelight.SonorDatabase
import com.example.sonor.sqldelight.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DatabaseHelperImpl(database: SonorDatabase) : DatabaseHelper {
    private val q = database.sonorDatabaseQueries

    override fun getAllFavorites(): Flow<List<FavoriteSongEntity>> =
        q.getAllFavorites().asFlow().mapToList(Dispatchers.Default).map { list ->
            list.map { FavoriteSongEntity(it.id, it.title, it.artist, it.albumArtUri) }
        }

    override suspend fun toggleFavorite(song: FavoriteSongEntity) {
        val exists = q.isFavorite(song.id).executeAsOne()
        // SQLDelight returns Boolean for EXISTS in this configuration
        if (exists) {
            q.removeFavorite(song.id)
        } else {
            q.addFavorite(song.id, song.title, song.artist, song.albumArtUri)
        }
    }

    override fun isFavorite(songId: Long): Flow<Boolean> =
        q.isFavorite(songId).asFlow().mapToOne(Dispatchers.Default)

    override fun getAllPlaylists(): Flow<List<PlaylistEntity>> =
        q.getAllPlaylists().asFlow().mapToList(Dispatchers.Default).map { list ->
            list.map { PlaylistEntity(it.id, it.name, it.createdAt, it.description, it.customCoverUri) }
        }

    override suspend fun createPlaylist(playlist: PlaylistEntity) {
        q.createPlaylist(playlist.name, playlist.description, playlist.customCoverUri)
    }

    override suspend fun deletePlaylist(playlist: PlaylistEntity) {
        q.deletePlaylist(playlist.id)
    }

    override suspend fun addSongToPlaylist(crossRef: PlaylistSongCrossRef) {
        q.addSongToPlaylist(crossRef.playlistId, crossRef.songId)
    }

    override suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        q.removeSongFromPlaylist(playlistId, songId)
    }

    override fun getSongsForPlaylist(playlistId: Long): Flow<List<FavoriteSongEntity>> =
        q.getSongsForPlaylist(playlistId).asFlow().mapToList(Dispatchers.Default).map { list ->
            list.map { FavoriteSongEntity(it.id, it.title, it.artist, it.albumArtUri) }
        }

    override suspend fun incrementPlayCount(songId: Long) {
        val stats = q.getStatsForSong(songId).executeAsOneOrNull()
        q.updateStats(
            songId = songId,
            playCount = (stats?.playCount ?: 0L) + 1L,
            lastPlayedTimestamp = System.currentTimeMillis(),
            totalTimePlayed = stats?.totalTimePlayed ?: 0L
        )
    }

    override suspend fun addToHistory(songId: Long) {
        q.addToHistory(songId, System.currentTimeMillis())
    }

    override fun getRecentlyPlayed(): Flow<List<SongStatsEntity>> =
        q.getRecentlyPlayed().asFlow().mapToList(Dispatchers.Default).map { list ->
            list.map { SongStatsEntity(it.songId, it.playCount.toInt(), it.lastPlayedTimestamp, it.totalTimePlayed) }
        }

    override fun getMostPlayed(): Flow<List<SongStatsEntity>> =
        q.getMostPlayed().asFlow().mapToList(Dispatchers.Default).map { list ->
            list.map { SongStatsEntity(it.songId, it.playCount.toInt(), it.lastPlayedTimestamp, it.totalTimePlayed) }
        }

    override suspend fun excludeFolder(folder: ExcludedFolderEntity) {
        q.excludeFolder(folder.path)
    }

    override fun getExcludedFolders(): Flow<List<String>> =
        q.getExcludedFolders().asFlow().mapToList(Dispatchers.Default)

    override suspend fun includeFolder(folder: ExcludedFolderEntity) {
        q.includeFolder(folder.path)
    }

    override suspend fun saveLyrics(lyrics: LyricsEntity) {
        q.saveLyrics(lyrics.songId, lyrics.lyricsText, if (lyrics.isSynced) 1L else 0L, lyrics.source)
    }

    override suspend fun getLyricsForSong(songId: Long): LyricsEntity? =
        q.getLyricsForSong(songId).executeAsOneOrNull()?.let {
            LyricsEntity(it.songId, it.lyricsText, it.isSynced != 0L, it.source)
        }

    override suspend fun saveEqPreset(preset: EqPresetEntity) {
        q.saveEqPreset(preset.name, preset.gains, if (preset.isSystem) 1L else 0L)
    }

    override fun getAllEqPresets(): Flow<List<EqPresetEntity>> =
        q.getAllEqPresets().asFlow().mapToList(Dispatchers.Default).map { list ->
            list.map { EqPresetEntity(it.name, it.gains, it.isSystem != 0L) }
        }

    override suspend fun addRecentSearch(search: RecentSearchEntity) {
        q.addRecentSearch(search.query, search.timestamp)
    }

    override fun getRecentSearches(): Flow<List<RecentSearchEntity>> =
        q.getRecentSearches().asFlow().mapToList(Dispatchers.Default).map { list ->
            list.map { RecentSearchEntity(it.query, it.timestamp) }
        }
}
