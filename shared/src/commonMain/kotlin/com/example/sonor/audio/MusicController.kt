package com.example.sonor.audio

import com.example.sonor.domain.model.PlayerState
import com.example.sonor.domain.model.Song
import kotlinx.coroutines.flow.StateFlow

interface MusicController {
    val playerState: StateFlow<PlayerState>

    fun play(song: Song)
    fun play(songs: List<Song>, startIndex: Int = 0)

    fun pause()
    fun resume()

    fun skipNext()
    fun skipPrevious()

    fun seekTo(position: Long)

    fun setShuffleMode(enabled: Boolean)
    fun setRepeatMode(repeatMode: Int)

    // Professional Features
    fun setPlaybackSpeed(speed: Float)
    fun setPitch(pitch: Float)
    fun setEqualizerEnabled(enabled: Boolean)
    fun setEqualizerBandLevel(band: Int, level: Int)

    // Sleep Timer
    fun setSleepTimer(minutes: Int)
    fun cancelSleepTimer()
}
