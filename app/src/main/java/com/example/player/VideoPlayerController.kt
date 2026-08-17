package com.example.player

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.SeekParameters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class PlayerUiState(
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val isBuffering: Boolean = false,
    val isEnded: Boolean = false,
    val playbackSpeed: Float = 1.0f,
    val videoWidth: Int = 1920,
    val videoHeight: Int = 1080,
    val isMuted: Boolean = false,
    val isLooping: Boolean = false
) {
    val aspectRatio: Float get() = if (videoHeight > 0) videoWidth.toFloat() / videoHeight.toFloat() else 16f / 9f
}

@OptIn(UnstableApi::class)
class VideoPlayerController(
    private val context: Context,
    private val coroutineScope: CoroutineScope
) {
    private var exoPlayer: ExoPlayer? = null
    private var progressJob: Job? = null

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    fun getPlayer(): ExoPlayer {
        if (exoPlayer == null) {
            initPlayer()
        }
        return exoPlayer!!
    }

    private fun initPlayer() {
        val player = ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_OFF
            setSeekParameters(SeekParameters.EXACT)
            addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _uiState.value = _uiState.value.copy(isPlaying = isPlaying)
                    if (isPlaying) {
                        startProgressTracker()
                    } else {
                        stopProgressTracker()
                        updatePosition()
                    }
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        Player.STATE_BUFFERING -> {
                            _uiState.value = _uiState.value.copy(isBuffering = true, isEnded = false)
                        }
                        Player.STATE_READY -> {
                            val duration = duration.coerceAtLeast(0L)
                            _uiState.value = _uiState.value.copy(
                                isBuffering = false,
                                isEnded = false,
                                durationMs = duration
                            )
                        }
                        Player.STATE_ENDED -> {
                            _uiState.value = _uiState.value.copy(
                                isPlaying = false,
                                isEnded = true,
                                currentPositionMs = duration.coerceAtLeast(0L)
                            )
                        }
                        Player.STATE_IDLE -> {
                            _uiState.value = _uiState.value.copy(isBuffering = false)
                        }
                    }
                }

                override fun onVideoSizeChanged(videoSize: VideoSize) {
                    if (videoSize.width > 0 && videoSize.height > 0) {
                        _uiState.value = _uiState.value.copy(
                            videoWidth = videoSize.width,
                            videoHeight = videoSize.height
                        )
                    }
                }
            })
        }
        exoPlayer = player
    }

    fun loadMedia(uri: Uri) {
        val player = getPlayer()
        val mediaItem = MediaItem.fromUri(uri)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.pause()
        _uiState.value = _uiState.value.copy(
            currentPositionMs = 0L,
            isEnded = false
        )
    }

    fun unloadMedia() {
        stopProgressTracker()
        exoPlayer?.stop()
        exoPlayer?.clearMediaItems()
        _uiState.value = PlayerUiState()
    }

    fun play() {
        getPlayer().play()
    }

    fun pause() {
        getPlayer().pause()
    }

    fun togglePlayPause() {
        val player = getPlayer()
        if (player.isPlaying) {
            player.pause()
        } else {
            if (_uiState.value.isEnded) {
                player.seekTo(0L)
            }
            player.play()
        }
    }

    fun seekTo(positionMs: Long) {
        val player = getPlayer()
        val maxDuration = _uiState.value.durationMs.coerceAtLeast(1L)
        val targetMs = positionMs.coerceIn(0L, maxDuration)
        player.seekTo(targetMs)
        _uiState.value = _uiState.value.copy(
            currentPositionMs = targetMs,
            isEnded = targetMs >= maxDuration
        )
    }

    fun seekBy(deltaMs: Long) {
        val current = getPlayer().currentPosition
        seekTo(current + deltaMs)
    }

    fun stepFrame(deltaFrames: Int) {
        // ~33.3ms per frame (approx 30fps)
        val frameDurationMs = 33L
        val current = getPlayer().currentPosition
        seekTo(current + (deltaFrames * frameDurationMs))
    }

    fun setPlaybackSpeed(speed: Float) {
        getPlayer().playbackParameters = PlaybackParameters(speed)
        _uiState.value = _uiState.value.copy(playbackSpeed = speed)
    }

    fun toggleMute() {
        val player = getPlayer()
        val newMuted = !_uiState.value.isMuted
        player.volume = if (newMuted) 0.0f else 1.0f
        _uiState.value = _uiState.value.copy(isMuted = newMuted)
    }

    fun toggleLooping() {
        val player = getPlayer()
        val newLoop = !_uiState.value.isLooping
        player.repeatMode = if (newLoop) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
        _uiState.value = _uiState.value.copy(isLooping = newLoop)
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = coroutineScope.launch(Dispatchers.Main) {
            while (isActive) {
                updatePosition()
                delay(40) // ~25fps UI sync - optimal for smooth timeline performance
            }
        }
    }

    private fun stopProgressTracker() {
        progressJob?.cancel()
        progressJob = null
    }

    private fun updatePosition() {
        exoPlayer?.let { player ->
            val pos = player.currentPosition.coerceAtLeast(0L)
            val dur = player.duration.coerceAtLeast(0L)
            val currentDur = if (dur > 0) dur else _uiState.value.durationMs
            val delta = kotlin.math.abs(pos - _uiState.value.currentPositionMs)
            // Only emit state update if position changed by at least 15ms or duration changed or player is paused
            if (!player.isPlaying || delta >= 15L || currentDur != _uiState.value.durationMs) {
                _uiState.value = _uiState.value.copy(
                    currentPositionMs = pos,
                    durationMs = currentDur
                )
            }
        }
    }

    fun release() {
        stopProgressTracker()
        exoPlayer?.release()
        exoPlayer = null
    }
}
