package com.example.sonor.audio

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.sonor.domain.model.PlayerState
import com.example.sonor.domain.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

public class MusicControllerImpl(context: Context) : MusicController {

    public val exoPlayer: ExoPlayer = ExoPlayer.Builder(context).build()

    private val _playerState = MutableStateFlow(PlayerState())
    override val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    // Coroutine scope pour le polling de position (dure autant que le controller)
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var positionPollingJob: Job? = null

    private val listener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _playerState.value = _playerState.value.copy(
                isPlaying = isPlaying,
                playbackState = if (isPlaying) PlayerState.STATE_READY else PlayerState.STATE_IDLE,
                duration = exoPlayer.duration.coerceAtLeast(0),
                currentPosition = exoPlayer.currentPosition.coerceAtLeast(0)
            )
            // Démarrer/arrêter le polling selon l'état de lecture
            if (isPlaying) startPositionPolling() else stopPositionPolling()
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            _playerState.value = _playerState.value.copy(
                playbackState = when (playbackState) {
                    Player.STATE_IDLE -> PlayerState.STATE_IDLE
                    Player.STATE_BUFFERING -> PlayerState.STATE_BUFFERING
                    Player.STATE_READY -> PlayerState.STATE_READY
                    Player.STATE_ENDED -> PlayerState.STATE_ENDED
                    else -> PlayerState.STATE_IDLE
                },
                duration = exoPlayer.duration.coerceAtLeast(0)
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
                currentSong = _playerState.value.currentSong?.takeIf { it.id == nextId },
                currentPosition = 0L,
                duration = exoPlayer.duration.coerceAtLeast(0)
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

    /**
     * Démarre un polling de position toutes les 500ms pour mettre à jour
     * currentPosition et duration en temps réel pendant la lecture.
     */
    private fun startPositionPolling() {
        positionPollingJob?.cancel()
        positionPollingJob = scope.launch {
            while (isActive) {
                val pos = exoPlayer.currentPosition.coerceAtLeast(0)
                val dur = exoPlayer.duration.coerceAtLeast(0)
                _playerState.value = _playerState.value.copy(
                    currentPosition = pos,
                    duration = dur
                )
                delay(500)
            }
        }
    }

    private fun stopPositionPolling() {
        positionPollingJob?.cancel()
        positionPollingJob = null
        // Snapshot final position when paused
        _playerState.value = _playerState.value.copy(
            currentPosition = exoPlayer.currentPosition.coerceAtLeast(0),
            duration = exoPlayer.duration.coerceAtLeast(0)
        )
    }

    private fun updatePlayerState() {
        _playerState.value = _playerState.value.copy(
            duration = exoPlayer.duration.coerceAtLeast(0),
            currentPosition = exoPlayer.currentPosition.coerceAtLeast(0),
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
            currentQueue = listOf(song),
            currentPosition = 0L
        )
        startPositionPolling()
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
            currentQueue = songs,
            currentPosition = 0L
        )
        startPositionPolling()
    }

    override fun pause() {
        exoPlayer.pause()
        _playerState.value = _playerState.value.copy(isPlaying = false)
        stopPositionPolling()
    }

    override fun resume() {
        exoPlayer.play()
        _playerState.value = _playerState.value.copy(isPlaying = true)
        startPositionPolling()
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

    private var sleepTimerJob: kotlinx.coroutines.Job? = null

    override fun setSleepTimer(minutes: Int) {
        cancelSleepTimer()
        sleepTimerJob = scope.launch {
            delay(minutes * 60 * 1000L)
            pause()
        }
    }

    override fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
    }

    // Platform-specific cleanup API (kept local for now; controller lifecycle should call this)
    fun release() {
        positionPollingJob?.cancel()
        sleepTimerJob?.cancel()
        exoPlayer.removeListener(listener)
        exoPlayer.release()
    }
}
