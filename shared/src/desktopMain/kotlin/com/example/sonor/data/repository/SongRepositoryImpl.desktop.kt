package com.example.sonor.data.repository

import com.example.sonor.data.DatabaseHelper
import com.example.sonor.domain.model.*
import com.example.sonor.domain.repository.SongRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import java.io.File

class SongRepositoryImpl(
    private val databaseHelper: DatabaseHelper
) : SongRepository {

    override fun getAllSongs(): Flow<List<Song>> = combine(
        getAllMedia(),
        databaseHelper.getExcludedFolders()
    ) { media, excludedPaths ->
        media.filter { item ->
            excludedPaths.none { excluded -> item.path.startsWith(excluded) }
        }
    }.flowOn(Dispatchers.IO)

    private fun getAllMedia(): Flow<List<Song>> = flow {
        val songs = mutableListOf<Song>()
        
        // Scan common music directories on desktop
        val musicDirs = listOf(
            File(System.getProperty("user.home"), "Music"),
            File(System.getProperty("user.home"), "Downloads"),
            File(System.getProperty("user.home"), "Videos")
        )
        
        musicDirs.forEach { dir ->
            if (dir.exists()) {
                scanDirectory(dir, songs)
            }
        }
        
        emit(songs.sortedBy { it.title })
    }

    private fun scanDirectory(dir: File, songs: MutableList<Song>) {
        dir.walkTopDown()
            .filter { it.isFile }
            .filter { it.extension.lowercase() in SUPPORTED_AUDIO_EXTENSIONS || it.extension.lowercase() in SUPPORTED_VIDEO_EXTENSIONS }
            .forEach { file ->
                val isAudio = file.extension.lowercase() in SUPPORTED_AUDIO_EXTENSIONS
                songs.add(Song(
                    id = file.absolutePath.hashCode().toLong(),
                    title = file.nameWithoutExtension,
                    artist = extractArtist(file),
                    album = extractAlbum(file),
                    duration = 0, // Will need metadata extraction
                    uri = file.toURI().toString(),
                    albumArtUri = null,
                    path = file.absolutePath,
                    type = if (isAudio) MediaType.AUDIO else MediaType.VIDEO
                ))
            }
    }

    private fun extractArtist(file: File): String {
        // Simple extraction from file path or use metadata library
        val parentDir = file.parentFile?.name ?: "Unknown Artist"
        return if (parentDir.matches(Regex("\\d{4} - .*"))) {
            parentDir.substringAfter(" - ")
        } else {
            parentDir
        }
    }

    private fun extractAlbum(file: File): String {
        // Simple extraction from file path
        return file.parentFile?.parentFile?.name ?: "Unknown Album"
    }

    override fun getFavoriteSongs(): Flow<List<Song>> = combine(
        getAllSongs(), 
        databaseHelper.getAllFavorites()
    ) { all, favs ->
        val ids = favs.map { it.id }.toSet()
        all.filter { it.id in ids }
    }

    override suspend fun toggleFavorite(songId: Long, title: String, artist: String, albumArtUri: String) {
        databaseHelper.toggleFavorite(FavoriteSongEntity(songId, title, artist, albumArtUri))
    }

    override fun isFavorite(songId: Long): Flow<Boolean> = databaseHelper.isFavorite(songId)

    override suspend fun deleteSong(song: Song) {
        withContext(Dispatchers.IO) {
            val file = File(song.path)
            if (file.exists()) {
                file.delete()
            }
        }
    }

    override fun getAllPlaylists(): Flow<List<PlaylistEntity>> = databaseHelper.getAllPlaylists()

    override suspend fun createPlaylist(name: String, description: String?) {
        databaseHelper.createPlaylist(PlaylistEntity(id = 0L, name = name, description = description))
    }

    override suspend fun deletePlaylist(playlist: PlaylistEntity) = databaseHelper.deletePlaylist(playlist)

    override suspend fun addSongToPlaylist(songId: Long, playlistId: Long) = 
        databaseHelper.addSongToPlaylist(PlaylistSongCrossRef(playlistId, songId))

    override suspend fun removeSongFromPlaylist(songId: Long, playlistId: Long) = 
        databaseHelper.removeSongFromPlaylist(playlistId, songId)

    override fun getSongsForPlaylist(playlistId: Long): Flow<List<Song>> = combine(
        getAllSongs(), 
        databaseHelper.getSongsForPlaylist(playlistId)
    ) { all, p -> 
        val ids = p.map { it.id }.toSet()
        all.filter { it.id in ids }
    }

    override fun getFolders(): Flow<List<PlatformFile>> = getAllSongs().map { songs -> 
        songs.map { File(it.path).parentFile?.toPlatformFile() }.filterNotNull().distinctBy { it.path }.sortedBy { it.name }
    }

    override fun getSongsInFolder(folderPath: String): Flow<List<Song>> = 
        getAllSongs().map { it.filter { File(it.path).parent == folderPath } }

    override suspend fun excludeFolder(path: String) = databaseHelper.excludeFolder(ExcludedFolderEntity(path))
    
    override suspend fun includeFolder(path: String) = databaseHelper.includeFolder(ExcludedFolderEntity(path))
    
    override fun getExcludedFolders(): Flow<List<String>> = databaseHelper.getExcludedFolders()

    override fun getArtists(): Flow<List<Artist>> = getAllSongs().map { songs ->
        songs.filter { it.type == MediaType.AUDIO }
            .groupBy { it.artist }
            .map { (name, artistSongs) ->
                Artist(
                    name = name,
                    songCount = artistSongs.size,
                    albumCount = artistSongs.map { it.album }.distinct().size
                )
            }.sortedBy { it.name }
    }

    override fun getAlbums(): Flow<List<Album>> = getAllSongs().map { songs ->
        songs.filter { it.type == MediaType.AUDIO }
            .groupBy { it.album }
            .map { (name, albumSongs) ->
                val firstSong = albumSongs.first()
                Album(
                    id = 0,
                    name = name,
                    artist = firstSong.artist,
                    songCount = albumSongs.size,
                    artworkUri = firstSong.albumArtUri
                )
            }.sortedBy { it.name }
    }

    override fun getSongsByArtist(artistName: String): Flow<List<Song>> = 
        getAllSongs().map { it.filter { it.artist == artistName } }
    
    override fun getSongsByAlbum(albumId: Long): Flow<List<Song>> = getAllSongs()

    override suspend fun incrementPlayCount(songId: Long) = withContext(Dispatchers.IO) {
        databaseHelper.incrementPlayCount(songId)
    }

    override suspend fun addToHistory(songId: Long) = databaseHelper.addToHistory(songId)

    override fun getRecentlyPlayed(): Flow<List<Song>> = combine(
        getAllSongs(), 
        databaseHelper.getRecentlyPlayed()
    ) { all, st -> 
        val ids = st.map { it.songId }
        all.filter { it.id in ids }.sortedByDescending { s -> st.find { it.songId == s.id }?.lastPlayedTimestamp }
    }

    override fun getMostPlayed(): Flow<List<Song>> = combine(
        getAllSongs(), 
        databaseHelper.getMostPlayed()
    ) { all, st -> 
        val ids = st.map { it.songId }
        all.filter { it.id in ids }.sortedByDescending { s -> st.find { it.songId == s.id }?.playCount }
    }

    override suspend fun saveLyrics(songId: Long, lyrics: String, isSynced: Boolean) = 
        databaseHelper.saveLyrics(LyricsEntity(songId, lyrics, isSynced))
    
    override suspend fun getLyrics(songId: Long): LyricsEntity? = databaseHelper.getLyricsForSong(songId)

    override suspend fun saveEqPreset(name: String, gains: List<Float>) = 
        databaseHelper.saveEqPreset(EqPresetEntity(name, gains.joinToString(",")))
    
    override fun getEqPresets(): Flow<List<EqPresetEntity>> = databaseHelper.getAllEqPresets()

    override suspend fun addRecentSearch(query: String) = databaseHelper.addRecentSearch(RecentSearchEntity(query))
    
    override fun getRecentSearches(): Flow<List<RecentSearchEntity>> = databaseHelper.getRecentSearches()

    companion object {
        private val SUPPORTED_AUDIO_EXTENSIONS = setOf("mp3", "wav", "flac", "m4a", "aac", "ogg", "wma")
        private val SUPPORTED_VIDEO_EXTENSIONS = setOf("mp4", "mkv", "avi", "mov", "wmv", "flv")
    }
}
