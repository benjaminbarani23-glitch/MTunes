package com.mtunes.app.data.repository

import com.mtunes.app.data.local.dao.FriendDao
import com.mtunes.app.data.local.entity.FriendEntity
import com.mtunes.app.domain.model.Friend
import com.mtunes.app.domain.model.FriendStatus
import com.mtunes.app.network.FriendNetwork
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FriendRepository @Inject constructor(
    private val friendDao: FriendDao,
    private val friendNetwork: FriendNetwork
) {
    fun getAllFriends(): Flow<List<Friend>> {
        return friendDao.getAllFriends().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getOnlineFriends(): Flow<List<Friend>> {
        return friendDao.getOnlineFriends().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun addFriend(friendCode: String): Friend? {
        val friend = friendNetwork.addFriend(friendCode)
        if (friend != null) {
            friendDao.insertFriend(friend.toEntity())
        }
        return friend
    }

    suspend fun removeFriend(friendId: String) {
        friendNetwork.removeFriend(friendId)
        val entity = friendDao.getFriendById(friendId)
        if (entity != null) {
            friendDao.deleteFriend(entity)
        }
    }

    fun getMyFriendCode(): Flow<String> = friendNetwork.myFriendCode

    private fun FriendEntity.toDomain(): Friend {
        return Friend(
            id = id,
            name = name,
            friendCode = friendCode,
            status = FriendStatus.valueOf(status),
            sharedSongCount = sharedSongCount,
            avatarUrl = avatarUrl
        )
    }

    private fun Friend.toEntity(): FriendEntity {
        return FriendEntity(
            id = id,
            name = name,
            friendCode = friendCode,
            status = status.name,
            sharedSongCount = sharedSongCount,
            avatarUrl = avatarUrl
        )
    }
}
