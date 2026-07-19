package com.example.sonor.audio.service

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.os.Bundle
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.example.sonor.audio.MusicController
import com.example.sonor.audio.MusicControllerImpl
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import org.koin.android.ext.android.inject

class SonorPlaybackService : MediaSessionService() {

    private val musicController: MusicController by inject()
    private val player: Player by lazy { (musicController as MusicControllerImpl).exoPlayer }

    private var mediaSession: MediaSession? = null
    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var loudnessEnhancer: LoudnessEnhancer? = null

    companion object {
        const val COMMAND_SET_EQ_BAND = "SET_EQ_BAND"
        const val COMMAND_SET_EQ_ENABLED = "SET_EQ_ENABLED"
        const val COMMAND_SET_BASS_BOOST = "SET_BASS_BOOST"
    }

    override fun onCreate() {
        super.onCreate()
        
        val sessionCallback = object : MediaSession.Callback {
            override fun onConnect(
                session: MediaSession,
                controller: MediaSession.ControllerInfo
            ): MediaSession.ConnectionResult {
                val sessionCommands = MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS.buildUpon()
                    .add(SessionCommand(COMMAND_SET_EQ_BAND, Bundle.EMPTY))
                    .add(SessionCommand(COMMAND_SET_EQ_ENABLED, Bundle.EMPTY))
                    .add(SessionCommand(COMMAND_SET_BASS_BOOST, Bundle.EMPTY))
                    .build()
                return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
                    .setAvailableSessionCommands(sessionCommands)
                    .build()
            }

            override fun onCustomCommand(
                session: MediaSession,
                controller: MediaSession.ControllerInfo,
                customCommand: SessionCommand,
                args: Bundle
            ): ListenableFuture<SessionResult> {
                when (customCommand.customAction) {
                    COMMAND_SET_EQ_BAND -> {
                        val band = args.getInt("band")
                        val level = args.getInt("level")
                        equalizer?.setBandLevel(band.toShort(), level.toShort())
                    }
                    COMMAND_SET_EQ_ENABLED -> {
                        val enabled = args.getBoolean("enabled")
                        equalizer?.enabled = enabled
                    }
                    COMMAND_SET_BASS_BOOST -> {
                        val strength = args.getInt("strength")
                        bassBoost?.setStrength(strength.toShort())
                    }
                }
                return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
            }
        }

        mediaSession = MediaSession.Builder(this, player)
            .setCallback(sessionCallback)
            .build()
        
        if (player is ExoPlayer) {
            val exoPlayer = player as ExoPlayer
            exoPlayer.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_READY && equalizer == null) {
                        setupAudioEffects(exoPlayer.audioSessionId)
                    }
                }
            })
        }
    }

    private fun setupAudioEffects(audioSessionId: Int) {
        try {
            equalizer = Equalizer(0, audioSessionId).apply { enabled = true }
            bassBoost = BassBoost(0, audioSessionId).apply { enabled = true }
            loudnessEnhancer = LoudnessEnhancer(audioSessionId).apply { enabled = true }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onDestroy() {
        equalizer?.release()
        bassBoost?.release()
        loudnessEnhancer?.release()
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}
