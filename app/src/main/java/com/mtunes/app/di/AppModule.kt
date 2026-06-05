package com.mtunes.app.di

import android.content.Context
import androidx.room.Room
import com.mtunes.app.data.local.MTunesDatabase
import com.mtunes.app.data.local.dao.FriendDao
import com.mtunes.app.data.local.dao.SongDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MTunesDatabase {
        return Room.databaseBuilder(
            context,
            MTunesDatabase::class.java,
            "mtunes_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideSongDao(database: MTunesDatabase): SongDao {
        return database.songDao()
    }

    @Provides
    @Singleton
    fun provideFriendDao(database: MTunesDatabase): FriendDao {
        return database.friendDao()
    }
}
