package com.mtunes.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val albumArtUrl: String,
    val duration: Long,
    val filePath: String,
    val magnetLink: String,
    val source: String,
    val isDownloaded: Boolean,
    val quality: String,
    val addedAt: Long = System.currentTimeMillis()
)
