package com.mtunes.app.domain.model

data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val album: String = "",
    val albumArtUrl: String = "",
    val duration: Long = 0L,
    val filePath: String = "",
    val magnetLink: String = "",
    val source: SongSource = SongSource.PUBLIC,
    val isDownloaded: Boolean = false,
    val downloadProgress: Float = 0f,
    val quality: AudioQuality = AudioQuality.HIGH
)

enum class SongSource {
    LOCAL_CACHE,
    FRIEND,
    PRIVATE_SHARED,
    PUBLIC
}

enum class AudioQuality {
    LOW,
    MEDIUM,
    HIGH,
    LOSSLESS
}
