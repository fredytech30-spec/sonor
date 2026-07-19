package com.example.sonor.data.repository

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import com.example.sonor.data.DatabaseHelper
import com.example.sonor.domain.model.*
import com.example.sonor.domain.repository.SongRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext

class SongRepositoryImpl(
    private val context: Context,
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

    private fun getAllMedia(): Flow<List<Song>> = combine(
        getAudioMedia(),
        getVideoMedia()
    ) { audio, video ->
        (audio + video).sortedBy { it.title }
    }

    private fun getAudioMedia(): Flow<List<Song>> = flow {
        val songs = mutableListOf<Song>()
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DATA
        )
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} > 30000"
        
        context.contentResolver.query(collection, projection, selection, null, null)?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val albumId = cursor.getLong(albumIdCol)
                songs.add(Song(
                    id = id,
                    title = cursor.getString(titleCol) ?: "Inconnu",
                    artist = cursor.getString(artistCol) ?: "Artiste Inconnu",
                    album = cursor.getString(albumCol) ?: "Album Inconnu",
                    duration = cursor.getLong(durationCol),
                    uri = ContentUris.withAppendedId(collection, id).toString(),
                    albumArtUri = ContentUris.withAppendedId(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, albumId).toString(),
                    path = cursor.getString(dataCol) ?: "",
                    type = MediaType.AUDIO
                ))
            }
        }
        emit(songs)
    }

    private fun getVideoMedia(): Flow<List<Song>> = flow {
        val videos = mutableListOf<Song>()
        val collection = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.DATA
        )
        
        context.contentResolver.query(collection, projection, null, null, null)?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE)
            val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
            val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                videos.add(Song(
                    id = id,
                    title = cursor.getString(titleCol) ?: "Vidéo Locale",
                    artist = "Vidéo",
                    album = "Galerie",
                    duration = cursor.getLong(durationCol),
                    uri = ContentUris.withAppendedId(collection, id).toString(),
                    albumArtUri = null,
                    path = cursor.getString(dataCol) ?: "",
                    type = MediaType.VIDEO
                ))
            }
        }
        emit(videos)
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
            val file = java.io.File(song.path)
            if (file.exists()) {
                file.delete()
                context.contentResolver.delete(android.net.Uri.parse(song.uri), null, null)
            }
        }
    }

    override fun getAllPlaylists(): Flow<List<PlaylistEntity>> = databaseHelper.getAllPlaylists()

    override suspend fun createPlaylist(name: String, description: String?) {
        databaseHelper.createPlaylist(PlaylistEntity(id = 0, name = name, description = description))
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
        songs.map { java.io.File(it.path).parentFile?.toPlatformFile() }.filterNotNull().distinctBy { it.path }.sortedBy { it.name }
    }

    override fun getSongsInFolder(folderPath: String): Flow<List<Song>> = 
        getAllSongs().map { it.filter { java.io.File(it.path).parent == folderPath } }

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
}
