package com.example.sonor.domain.model

// Entities for database/storage (platform-specific implementations will use these)
data class PlaylistEntity(
    val id: Long,
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
    val description: String? = null,
    val customCoverUri: String? = null
)

data class FavoriteSongEntity(
    val id: Long,
    val title: String,
    val artist: String,
    val albumArtUri: String
)

data class SongStatsEntity(
    val songId: Long,
    val playCount: Int = 0,
    val lastPlayedTimestamp: Long = 0,
    val totalTimePlayed: Long = 0
)

data class PlaybackHistoryEntity(
    val id: Long = 0,
    val songId: Long,
    val timestamp: Long = System.currentTimeMillis()
)

data class PlaylistSongCrossRef(
    val playlistId: Long,
    val songId: Long,
    val addedAt: Long = System.currentTimeMillis()
)

data class ExcludedFolderEntity(
    val path: String
)

data class LyricsEntity(
    val songId: Long,
    val lyricsText: String,
    val isSynced: Boolean,
    val source: String? = null
)

data class EqPresetEntity(
    val name: String,
    val gains: String, // Stored as comma-separated floats
    val isSystem: Boolean = false
)

data class RecentSearchEntity(
    val query: String,
    val timestamp: Long = System.currentTimeMillis()
)
