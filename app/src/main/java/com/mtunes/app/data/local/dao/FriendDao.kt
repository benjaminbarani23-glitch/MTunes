package com.mtunes.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mtunes.app.data.local.entity.FriendEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FriendDao {
    @Query("SELECT * FROM friends ORDER BY name ASC")
    fun getAllFriends(): Flow<List<FriendEntity>>

    @Query("SELECT * FROM friends WHERE status = 'ONLINE'")
    fun getOnlineFriends(): Flow<List<FriendEntity>>

    @Query("SELECT * FROM friends WHERE id = :id")
    suspend fun getFriendById(id: String): FriendEntity?

    @Query("SELECT * FROM friends WHERE friendCode = :code")
    suspend fun getFriendByCode(code: String): FriendEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFriend(friend: FriendEntity)

    @Update
    suspend fun updateFriend(friend: FriendEntity)

    @Delete
    suspend fun deleteFriend(friend: FriendEntity)
}
