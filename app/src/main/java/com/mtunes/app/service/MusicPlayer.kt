package com.mtunes.app.service

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.mtunes.app.domain.model.Song
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicPlayer @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var exoPlayer: ExoPlayer? = null

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private fun getOrCreatePlayer(): ExoPlayer {
        if (exoPlayer == null) {
            exoPlayer = ExoPlayer.Builder(context).build().apply {
                addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(playing: Boolean) {
                        _isPlaying.value = playing
                    }

                    override fun onPlaybackStateChanged(playbackState: Int) {
                        if (playbackState == Player.STATE_ENDED) {
                            _isPlaying.value = false
                            _progress.value = 0f
                        }
                    }
                })
            }
        }
        return exoPlayer!!
    }

    fun playSong(song: Song) {
        _currentSong.value = song
        val player = getOrCreatePlayer()

        if (song.filePath.isNotEmpty()) {
            val mediaItem = MediaItem.fromUri(song.filePath)
            player.setMediaItem(mediaItem)
            player.prepare()
            player.play()
        } else {
            // For streaming, will be connected to torrent engine
            _isPlaying.value = true
        }
    }

    fun togglePlayPause() {
        val player = exoPlayer ?: return
        if (player.isPlaying) {
            player.pause()
        } else {
            player.play()
        }
    }

    fun seekTo(position: Long) {
        exoPlayer?.seekTo(position)
    }

    fun stop() {
        exoPlayer?.stop()
        _isPlaying.value = false
        _currentSong.value = null
    }

    fun release() {
        exoPlayer?.release()
        exoPlayer = null
    }

    fun updateProgress(progress: Float) {
        _progress.value = progress
    }

    fun updatePosition(position: Long) {
        _currentPosition.value = position
    }
}
