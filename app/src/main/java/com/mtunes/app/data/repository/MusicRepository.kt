package com.mtunes.app.data.repository

import com.mtunes.app.data.local.dao.SongDao
import com.mtunes.app.data.local.entity.SongEntity
import com.mtunes.app.domain.model.AudioQuality
import com.mtunes.app.domain.model.Song
import com.mtunes.app.domain.model.SongSource
import com.mtunes.app.network.FriendNetwork
import com.mtunes.app.torrent.TorrentEngine
import com.mtunes.app.torrent.TorrentSearchProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicRepository @Inject constructor(
    private val songDao: SongDao,
    private val torrentEngine: TorrentEngine,
    private val torrentSearchProvider: TorrentSearchProvider,
    private val friendNetwork: FriendNetwork
) {
    fun getRecentSongs(): Flow<List<Song>> {
        return songDao.getRecentSongs().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getDownloadedSongs(): Flow<List<Song>> {
        return songDao.getDownloadedSongs().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun searchLocalSongs(query: String): Flow<List<Song>> {
        return songDao.searchSongs(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun hybridSearch(query: String): List<Song> {
        val results = mutableListOf<Song>()

        // 1. Check local cache
        // (handled by searchLocalSongs flow)

        // 2. Check friends
        val friendResults = friendNetwork.searchFriendsForSong(query)
        results.addAll(friendResults)

        // 3. Search public torrents
        val torrentResults = torrentSearchProvider.searchForSong(query, "")
        results.addAll(torrentResults)

        return results
    }

    suspend fun saveSong(song: Song) {
        songDao.insertSong(song.toEntity())
    }

    suspend fun startStreaming(song: Song): Boolean {
        val torrents = torrentSearchProvider.searchTorrents("${song.artist} - ${song.title}")
        val bestTorrent = torrentEngine.validateAndSelect(torrents) ?: return false
        return torrentEngine.startDownload(bestTorrent, song.id)
    }

    private fun SongEntity.toDomain(): Song {
        return Song(
            id = id,
            title = title,
            artist = artist,
            album = album,
            albumArtUrl = albumArtUrl,
            duration = duration,
            filePath = filePath,
            magnetLink = magnetLink,
            source = SongSource.valueOf(source),
            isDownloaded = isDownloaded,
            quality = AudioQuality.valueOf(quality)
        )
    }

    private fun Song.toEntity(): SongEntity {
        return SongEntity(
            id = id,
            title = title,
            artist = artist,
            album = album,
            albumArtUrl = albumArtUrl,
            duration = duration,
            filePath = filePath,
            magnetLink = magnetLink,
            source = source.name,
            isDownloaded = isDownloaded,
            quality = quality.name
        )
    }
}
