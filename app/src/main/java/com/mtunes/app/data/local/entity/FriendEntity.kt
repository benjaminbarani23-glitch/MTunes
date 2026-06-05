package com.mtunes.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "friends")
data class FriendEntity(
    @PrimaryKey val id: String,
    val name: String,
    val friendCode: String,
    val status: String,
    val sharedSongCount: Int,
    val avatarUrl: String,
    val addedAt: Long = System.currentTimeMillis()
)
