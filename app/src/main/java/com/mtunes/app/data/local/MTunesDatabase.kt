package com.mtunes.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mtunes.app.data.local.dao.FriendDao
import com.mtunes.app.data.local.dao.SongDao
import com.mtunes.app.data.local.entity.FriendEntity
import com.mtunes.app.data.local.entity.SongEntity

@Database(
    entities = [SongEntity::class, FriendEntity::class],
    version = 1,
    exportSchema = false
)
abstract class MTunesDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun friendDao(): FriendDao
}
