package com.mtunes.app.torrent

import com.mtunes.app.domain.model.AudioQuality
import com.mtunes.app.domain.model.Song
import com.mtunes.app.domain.model.SongSource
import com.mtunes.app.domain.model.TorrentHealth
import com.mtunes.app.domain.model.TorrentInfo
import com.mtunes.app.security.FileValidator
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TorrentEngine @Inject constructor(
    private val fileValidator: FileValidator
) {
    private val _downloadProgress = MutableStateFlow<Map<String, Float>>(emptyMap())
    val downloadProgress: Flow<Map<String, Float>> = _downloadProgress.asStateFlow()

    private val _activeTorrents = MutableStateFlow<List<String>>(emptyList())
    val activeTorrents: Flow<List<String>> = _activeTorrents.asStateFlow()

    fun rankTorrents(torrents: List<TorrentInfo>): List<TorrentInfo> {
        return torrents.map { torrent ->
            val seederScore = (torrent.seeders.coerceAtMost(100) / 100f) * 30f
            val healthScore = when (torrent.health) {
                TorrentHealth.EXCELLENT -> 25f
                TorrentHealth.GOOD -> 20f
                TorrentHealth.FAIR -> 12f
                TorrentHealth.POOR -> 5f
                TorrentHealth.UNKNOWN -> 0f
            }
            val qualityScore = when (torrent.quality) {
                AudioQuality.LOSSLESS -> 25f
                AudioQuality.HIGH -> 20f
                AudioQuality.MEDIUM -> 12f
                AudioQuality.LOW -> 5f
            }
            val trustScore = torrent.trustedScore * 20f
            val totalRank = seederScore + healthScore + qualityScore + trustScore
            torrent.copy(rank = totalRank)
        }.sortedByDescending { it.rank }
    }

    suspend fun startDownload(torrentInfo: TorrentInfo, songId: String): Boolean {
        val currentActive = _activeTorrents.value.toMutableList()
        currentActive.add(songId)
        _activeTorrents.value = currentActive

        // Simulate sequential download for streaming
        simulateSequentialDownload(songId)
        return true
    }

    private suspend fun simulateSequentialDownload(songId: String) {
        // First 5-10 MB downloaded quickly for playback start
        for (i in 1..10) {
            delay(200)
            updateProgress(songId, i / 100f)
        }
        // Rest downloads in background
        for (i in 11..100) {
            delay(100)
            updateProgress(songId, i / 100f)
        }
        val currentActive = _activeTorrents.value.toMutableList()
        currentActive.remove(songId)
        _activeTorrents.value = currentActive
    }

    private fun updateProgress(songId: String, progress: Float) {
        val current = _downloadProgress.value.toMutableMap()
        current[songId] = progress
        _downloadProgress.value = current
    }

    fun stopDownload(songId: String) {
        val currentActive = _activeTorrents.value.toMutableList()
        currentActive.remove(songId)
        _activeTorrents.value = currentActive
    }

    fun validateAndSelect(torrents: List<TorrentInfo>): TorrentInfo? {
        val ranked = rankTorrents(torrents)
        return ranked.firstOrNull()
    }

    fun generateMockSearchResults(query: String): List<Song> {
        // This would be replaced with actual torrent search API calls
        return listOf(
            Song(
                id = UUID.randomUUID().toString(),
                title = query,
                artist = "Unknown Artist",
                album = "Unknown Album",
                duration = 210000L,
                source = SongSource.PUBLIC,
                quality = AudioQuality.HIGH
            )
        )
    }
}
