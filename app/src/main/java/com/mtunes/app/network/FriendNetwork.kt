package com.mtunes.app.network

import com.mtunes.app.domain.model.Friend
import com.mtunes.app.domain.model.FriendStatus
import com.mtunes.app.domain.model.Song
import com.mtunes.app.domain.model.SongSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FriendNetwork @Inject constructor() {

    private val _friends = MutableStateFlow<List<Friend>>(emptyList())
    val friends: Flow<List<Friend>> = _friends.asStateFlow()

    private val _myFriendCode = MutableStateFlow(generateFriendCode())
    val myFriendCode: Flow<String> = _myFriendCode.asStateFlow()

    fun generateFriendCode(): String {
        return "MT-${UUID.randomUUID().toString().take(8).uppercase()}"
    }

    suspend fun addFriend(friendCode: String): Friend? {
        delay(500) // Simulate network
        val friend = Friend(
            id = UUID.randomUUID().toString(),
            name = "Friend_${friendCode.takeLast(4)}",
            friendCode = friendCode,
            status = FriendStatus.ONLINE,
            sharedSongCount = (10..100).random()
        )
        val current = _friends.value.toMutableList()
        current.add(friend)
        _friends.value = current
        return friend
    }

    suspend fun removeFriend(friendId: String) {
        val current = _friends.value.toMutableList()
        current.removeAll { it.id == friendId }
        _friends.value = current
    }

    suspend fun searchFriendsForSong(query: String): List<Song> {
        delay(200)
        val onlineFriends = _friends.value.filter { it.status == FriendStatus.ONLINE }
        if (onlineFriends.isEmpty()) return emptyList()

        // Simulate finding songs from friends
        return if ((0..3).random() > 0) {
            listOf(
                Song(
                    id = UUID.randomUUID().toString(),
                    title = query,
                    artist = "From Friend",
                    source = SongSource.FRIEND,
                    duration = 240000L
                )
            )
        } else {
            emptyList()
        }
    }

    suspend fun streamFromFriend(friendId: String, songId: String): Boolean {
        delay(1000)
        return true
    }

    fun updateFriendStatus(friendId: String, status: FriendStatus) {
        val current = _friends.value.toMutableList()
        val index = current.indexOfFirst { it.id == friendId }
        if (index >= 0) {
            current[index] = current[index].copy(status = status)
            _friends.value = current
        }
    }
}
