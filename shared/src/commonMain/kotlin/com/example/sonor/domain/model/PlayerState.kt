package com.example.sonor.domain.model

data class PlayerState(
    val currentSong: Song? = null,
    val currentSongId: Long? = null,
    val isPlaying: Boolean = false,
    val playbackState: Int = STATE_IDLE,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val shuffleModeEnabled: Boolean = false,
    val repeatMode: Int = REPEAT_MODE_OFF,
    val playbackSpeed: Float = 1f,
    val equalizerEnabled: Boolean = false,
    val sleepTimerRemainingMillis: Long = 0L,
    val currentQueue: List<Song> = emptyList()
) {
    companion object {
        const val STATE_IDLE = 0
        const val STATE_BUFFERING = 1
        const val STATE_READY = 2
        const val STATE_ENDED = 3

        const val REPEAT_MODE_OFF = 0
        const val REPEAT_MODE_ONE = 1
        const val REPEAT_MODE_ALL = 2
    }
}
