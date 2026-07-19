package com.example.sonor.audio

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.sonor.domain.model.PlayerState
import com.example.sonor.domain.model.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

public class MusicControllerImpl(context: Context) : MusicController {

    public val exoPlayer: ExoPlayer = ExoPlayer.Builder(context).build()

    private val _playerState = MutableStateFlow(PlayerState())
    override val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private val listener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _playerState.value = _playerState.value.copy(
                isPlaying = isPlaying,
                playbackState = if (isPlaying) PlayerState.STATE_READY else PlayerState.STATE_IDLE
            )
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            _playerState.value = _playerState.value.copy(
                playbackState = when (playbackState) {
                    Player.STATE_IDLE -> PlayerState.STATE_IDLE
                    Player.STATE_BUFFERING -> PlayerState.STATE_BUFFERING
                    Player.STATE_READY -> PlayerState.STATE_READY
                    Player.STATE_ENDED -> PlayerState.STATE_ENDED
                    else -> PlayerState.STATE_IDLE
                }
            )
        }

        override fun onPlaybackParametersChanged(playbackParameters: androidx.media3.common.PlaybackParameters) {
            _playerState.value = _playerState.value.copy(
                playbackSpeed = playbackParameters.speed
            )
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            val nextId = mediaItem?.mediaId?.toLongOrNull()
            _playerState.value = _playerState.value.copy(
                currentSongId = nextId,
                // currentSong will be enriched by HomeViewModel using song list
                currentSong = _playerState.value.currentSong?.takeIf { it.id == nextId }
            )
            updatePlayerState()
        }
    }

    init {
        exoPlayer.addListener(listener)
        // initialize from current player state
        updatePlayerState()
        _playerState.value = _playerState.value.copy(
            currentSongId = exoPlayer.currentMediaItem?.mediaId?.toLongOrNull()
        )
    }

    private fun updatePlayerState() {
        _playerState.value = _playerState.value.copy(
            duration = exoPlayer.duration,
            currentPosition = exoPlayer.currentPosition,
            shuffleModeEnabled = exoPlayer.shuffleModeEnabled,
            repeatMode = when (exoPlayer.repeatMode) {
                Player.REPEAT_MODE_OFF -> PlayerState.REPEAT_MODE_OFF
                Player.REPEAT_MODE_ONE -> PlayerState.REPEAT_MODE_ONE
                Player.REPEAT_MODE_ALL -> PlayerState.REPEAT_MODE_ALL
                else -> PlayerState.REPEAT_MODE_OFF
            }
        )
    }

    private fun mediaItemFor(song: Song): MediaItem =
        MediaItem.Builder()
            .setUri(song.uri)
            // store Song.id for later mapping
            .setMediaId(song.id.toString())
            .build()

    override fun play(song: Song) {
        val mediaItem = mediaItemFor(song)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.play()

        _playerState.value = _playerState.value.copy(
            currentSong = song,
            currentSongId = song.id,
            isPlaying = true,
            currentQueue = listOf(song)
        )
    }

    override fun play(songs: List<Song>, startIndex: Int) {
        val mediaItems = songs.map { mediaItemFor(it) }
        exoPlayer.setMediaItems(mediaItems, startIndex, 0)
        exoPlayer.prepare()
        exoPlayer.play()

        val current = songs.getOrNull(startIndex)
        _playerState.value = _playerState.value.copy(
            currentSong = current,
            currentSongId = current?.id,
            isPlaying = true,
            currentQueue = songs
        )
    }

    override fun pause() {
        exoPlayer.pause()
        _playerState.value = _playerState.value.copy(isPlaying = false)
    }

    override fun resume() {
        exoPlayer.play()
        _playerState.value = _playerState.value.copy(isPlaying = true)
    }

    override fun skipNext() {
        exoPlayer.seekToNext()
        // currentSongId updated via onMediaItemTransition
    }

    override fun skipPrevious() {
        exoPlayer.seekToPrevious()
        // currentSongId updated via onMediaItemTransition
    }

    override fun seekTo(position: Long) {
        exoPlayer.seekTo(position)
        _playerState.value = _playerState.value.copy(currentPosition = position)
    }

    override fun setShuffleMode(enabled: Boolean) {
        exoPlayer.shuffleModeEnabled = enabled
        updatePlayerState()
    }

    override fun setRepeatMode(repeatMode: Int) {
        exoPlayer.repeatMode = when (repeatMode) {
            PlayerState.REPEAT_MODE_OFF -> Player.REPEAT_MODE_OFF
            PlayerState.REPEAT_MODE_ONE -> Player.REPEAT_MODE_ONE
            PlayerState.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ALL
            else -> Player.REPEAT_MODE_OFF
        }
        updatePlayerState()
    }

    override fun setPlaybackSpeed(speed: Float) {
        exoPlayer.setPlaybackSpeed(speed)
        _playerState.value = _playerState.value.copy(playbackSpeed = speed)
    }

    override fun setPitch(pitch: Float) {
        exoPlayer.setPlaybackParameters(
            androidx.media3.common.PlaybackParameters(exoPlayer.playbackParameters.speed, pitch)
        )
    }

    override fun setEqualizerEnabled(enabled: Boolean) {
        _playerState.value = _playerState.value.copy(equalizerEnabled = enabled)
        // TODO: Implement actual equalizer control
    }

    override fun setEqualizerBandLevel(band: Int, level: Int) {
        // TODO: Implement actual equalizer band control
    }

    override fun setSleepTimer(minutes: Int) {
        // TODO: Implement sleep timer
    }

    override fun cancelSleepTimer() {
        // TODO: Cancel sleep timer
    }

    // Platform-specific cleanup API (kept local for now; controller lifecycle should call this)
    fun release() {
        exoPlayer.removeListener(listener)
        exoPlayer.release()
    }
}
