package com.mtunes.app.domain.model

data class Friend(
    val id: String,
    val name: String,
    val friendCode: String,
    val status: FriendStatus = FriendStatus.OFFLINE,
    val sharedSongCount: Int = 0,
    val avatarUrl: String = ""
)

enum class FriendStatus {
    ONLINE,
    OFFLINE
}
