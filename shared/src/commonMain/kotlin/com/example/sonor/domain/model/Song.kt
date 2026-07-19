package com.example.sonor.domain.model

enum class MediaType {
    AUDIO, VIDEO
}

data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val uri: String,
    val albumArtUri: String?,
    val path: String,
    val type: MediaType = MediaType.AUDIO
)
