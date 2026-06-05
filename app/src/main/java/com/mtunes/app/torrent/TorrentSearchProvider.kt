package com.mtunes.app.torrent

import com.mtunes.app.domain.model.AudioQuality
import com.mtunes.app.domain.model.Song
import com.mtunes.app.domain.model.SongSource
import com.mtunes.app.domain.model.TorrentHealth
import com.mtunes.app.domain.model.TorrentInfo
import kotlinx.coroutines.delay
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TorrentSearchProvider @Inject constructor() {

    suspend fun searchTorrents(query: String): List<TorrentInfo> {
        delay(500) // Simulate network delay
        // In production, this would call actual torrent search APIs
        return generateMockResults(query)
    }

    suspend fun searchForSong(title: String, artist: String): List<Song> {
        delay(300)
        val query = "$artist - $title"
        return listOf(
            Song(
                id = UUID.randomUUID().toString(),
                title = title,
                artist = artist,
                album = "Album",
                albumArtUrl = "",
                duration = (180_000L..300_000L).random(),
                source = SongSource.PUBLIC,
                quality = AudioQuality.HIGH
            ),
            Song(
                id = UUID.randomUUID().toString(),
                title = title,
                artist = artist,
                album = "Album (Deluxe)",
                albumArtUrl = "",
                duration = (180_000L..300_000L).random(),
                source = SongSource.PUBLIC,
                quality = AudioQuality.LOSSLESS
            )
        )
    }

    private fun generateMockResults(query: String): List<TorrentInfo> {
        return listOf(
            TorrentInfo(
                magnetLink = "magnet:?xt=urn:btih:${UUID.randomUUID()}",
                name = "$query [FLAC]",
                seeders = 150,
                leechers = 20,
                size = 35_000_000L,
                health = TorrentHealth.EXCELLENT,
                quality = AudioQuality.LOSSLESS,
                trustedScore = 0.9f
            ),
            TorrentInfo(
                magnetLink = "magnet:?xt=urn:btih:${UUID.randomUUID()}",
                name = "$query [320kbps]",
                seeders = 85,
                leechers = 15,
                size = 10_000_000L,
                health = TorrentHealth.GOOD,
                quality = AudioQuality.HIGH,
                trustedScore = 0.8f
            ),
            TorrentInfo(
                magnetLink = "magnet:?xt=urn:btih:${UUID.randomUUID()}",
                name = "$query [MP3]",
                seeders = 45,
                leechers = 8,
                size = 7_000_000L,
                health = TorrentHealth.FAIR,
                quality = AudioQuality.MEDIUM,
                trustedScore = 0.6f
            )
        )
    }
}
