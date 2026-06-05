package com.mtunes.app.ui.screens.nowplaying

import androidx.lifecycle.ViewModel
import com.mtunes.app.domain.model.Song
import com.mtunes.app.service.MusicPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class NowPlayingViewModel @Inject constructor(
    private val musicPlayer: MusicPlayer
) : ViewModel() {

    val currentSong: StateFlow<Song?> = musicPlayer.currentSong
    val isPlaying: StateFlow<Boolean> = musicPlayer.isPlaying
    val progress: StateFlow<Float> = musicPlayer.progress
    val currentPosition: StateFlow<Long> = musicPlayer.currentPosition

    fun togglePlayPause() {
        musicPlayer.togglePlayPause()
    }

    fun seekTo(position: Float) {
        val song = currentSong.value ?: return
        val seekPosition = (position * song.duration).toLong()
        musicPlayer.seekTo(seekPosition)
    }

    fun playSong(song: Song) {
        musicPlayer.playSong(song)
    }
}
