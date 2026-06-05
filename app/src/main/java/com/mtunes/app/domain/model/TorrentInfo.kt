package com.mtunes.app.domain.model

data class TorrentInfo(
    val magnetLink: String,
    val name: String,
    val seeders: Int = 0,
    val leechers: Int = 0,
    val size: Long = 0L,
    val health: TorrentHealth = TorrentHealth.UNKNOWN,
    val quality: AudioQuality = AudioQuality.MEDIUM,
    val trustedScore: Float = 0f,
    val rank: Float = 0f
)

enum class TorrentHealth {
    EXCELLENT,
    GOOD,
    FAIR,
    POOR,
    UNKNOWN
}
