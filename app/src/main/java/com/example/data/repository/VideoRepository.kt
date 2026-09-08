package com.example.data.repository

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import com.example.data.db.AppDatabase
import com.example.data.db.PlaybackProgressEntity
import com.example.data.db.PlaylistEntity
import com.example.data.db.PlaylistItemEntity
import com.example.data.model.FolderItem
import com.example.data.model.VideoItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

enum class VideoSortOption(val displayName: String) {
    DATE_DESC("Newest first"),
    DATE_ASC("Oldest first"),
    NAME_ASC("Name (A–Z)"),
    NAME_DESC("Name (Z–A)"),
    SIZE_DESC("Size (Largest)"),
    SIZE_ASC("Size (Smallest)")
}

class VideoRepository(private val context: Context) {

    private val db = AppDatabase.getDatabase(context)
    private val playlistDao = db.playlistDao()
    private val playbackProgressDao = db.playbackProgressDao()

    suspend fun queryLocalVideos(): List<VideoItem> = withContext(Dispatchers.IO) {
        val videoList = mutableListOf<VideoItem>()
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DATE_MODIFIED,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Video.Media.BUCKET_DISPLAY_NAME
            } else {
                MediaStore.Video.Media.DATA
            },
            MediaStore.Video.Media.WIDTH,
            MediaStore.Video.Media.HEIGHT,
            MediaStore.Video.Media.MIME_TYPE
        )

        val sortOrder = "${MediaStore.Video.Media.DATE_MODIFIED} DESC"

        try {
            val queryUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            context.contentResolver.query(
                queryUri,
                projection,
                null,
                null,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val displayNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                val titleColumn = cursor.getColumnIndex(MediaStore.Video.Media.TITLE)
                val durationColumn = cursor.getColumnIndex(MediaStore.Video.Media.DURATION)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
                val dateModifiedColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED)
                val bucketColumn = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)
                } else {
                    cursor.getColumnIndex(MediaStore.Video.Media.DATA)
                }
                val widthColumn = cursor.getColumnIndex(MediaStore.Video.Media.WIDTH)
                val heightColumn = cursor.getColumnIndex(MediaStore.Video.Media.HEIGHT)
                val mimeColumn = cursor.getColumnIndex(MediaStore.Video.Media.MIME_TYPE)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val displayName = cursor.getString(displayNameColumn) ?: "Video_$id"
                    val title = if (titleColumn >= 0) cursor.getString(titleColumn) ?: displayName else displayName
                    val duration = if (durationColumn >= 0) cursor.getLong(durationColumn) else 0L
                    val size = cursor.getLong(sizeColumn)
                    val dateModified = cursor.getLong(dateModifiedColumn)
                    val folderName = if (bucketColumn >= 0) {
                        val rawBucket = cursor.getString(bucketColumn)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            rawBucket ?: "Internal Storage"
                        } else {
                            rawBucket?.substringBeforeLast('/')?.substringAfterLast('/') ?: "Internal Storage"
                        }
                    } else {
                        "Internal Storage"
                    }
                    val width = if (widthColumn >= 0) cursor.getInt(widthColumn) else 0
                    val height = if (heightColumn >= 0) cursor.getInt(heightColumn) else 0
                    val mime = if (mimeColumn >= 0) cursor.getString(mimeColumn) ?: "video/mp4" else "video/mp4"

                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        id
                    ).toString()

                    videoList.add(
                        VideoItem(
                            id = id,
                            contentUri = contentUri,
                            title = title,
                            displayName = displayName,
                            durationMs = duration,
                            sizeBytes = size,
                            dateModified = dateModified,
                            folderName = folderName,
                            width = width,
                            height = height,
                            mimeType = mime
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("VideoRepository", "Error querying MediaStore", e)
        }

        // If no videos exist on this fresh test device/emulator, provide sample local/playable video items
        if (videoList.isEmpty()) {
            videoList.addAll(getDemoSampleVideos())
        }

        videoList
    }

    private fun getDemoSampleVideos(): List<VideoItem> {
        return listOf(
            VideoItem(
                id = 1001L,
                contentUri = "https://raw.githubusercontent.com/mediaelement/mediaelement-files/master/big_buck_bunny.mp4",
                title = "Big Buck Bunny (Sample)",
                displayName = "BigBuckBunny.mp4",
                durationMs = 60000L,
                sizeBytes = 5510872L,
                dateModified = System.currentTimeMillis() / 1000 - 3600,
                folderName = "Demo Videos",
                width = 1280,
                height = 720,
                mimeType = "video/mp4"
            ),
            VideoItem(
                id = 1002L,
                contentUri = "https://raw.githubusercontent.com/mediaelement/mediaelement-files/master/echo-hereweare.mp4",
                title = "Echo - Here We Are (Sample)",
                displayName = "echo-hereweare.mp4",
                durationMs = 45000L,
                sizeBytes = 5360323L,
                dateModified = System.currentTimeMillis() / 1000 - 86400,
                folderName = "Demo Videos",
                width = 1280,
                height = 720,
                mimeType = "video/mp4"
            ),
            VideoItem(
                id = 1003L,
                contentUri = "https://interactive-examples.mdn.mozilla.net/media/cc0-videos/flower.mp4",
                title = "Flower Blooming (Sample)",
                displayName = "flower.mp4",
                durationMs = 10000L,
                sizeBytes = 1128375L,
                dateModified = System.currentTimeMillis() / 1000 - 172800,
                folderName = "Nature Clips",
                width = 640,
                height = 360,
                mimeType = "video/mp4"
            ),
            VideoItem(
                id = 1004L,
                contentUri = "https://interactive-examples.mdn.mozilla.net/media/cc0-videos/friday.mp4",
                title = "Friday Groove (Sample)",
                displayName = "friday.mp4",
                durationMs = 15000L,
                sizeBytes = 515198L,
                dateModified = System.currentTimeMillis() / 1000 - 300000,
                folderName = "Short Clips",
                width = 640,
                height = 360,
                mimeType = "video/mp4"
            )
        )
    }

    fun sortVideos(videos: List<VideoItem>, sortOption: VideoSortOption): List<VideoItem> {
        return when (sortOption) {
            VideoSortOption.DATE_DESC -> videos.sortedByDescending { it.dateModified }
            VideoSortOption.DATE_ASC -> videos.sortedBy { it.dateModified }
            VideoSortOption.NAME_ASC -> videos.sortedBy { it.displayName.lowercase() }
            VideoSortOption.NAME_DESC -> videos.sortedByDescending { it.displayName.lowercase() }
            VideoSortOption.SIZE_DESC -> videos.sortedByDescending { it.sizeBytes }
            VideoSortOption.SIZE_ASC -> videos.sortedBy { it.sizeBytes }
        }
    }

    fun groupVideosByFolders(videos: List<VideoItem>): List<FolderItem> {
        return videos.groupBy { it.folderName }
            .map { (folderName, items) ->
                FolderItem(
                    name = folderName,
                    videoCount = items.size,
                    totalSizeBytes = items.sumOf { it.sizeBytes },
                    firstVideoUri = items.firstOrNull()?.contentUri
                )
            }.sortedByDescending { it.videoCount }
    }

    // Playlists
    fun getAllPlaylists(): Flow<List<PlaylistEntity>> = playlistDao.getAllPlaylists()

    fun getPlaylistItems(playlistId: Long): Flow<List<PlaylistItemEntity>> =
        playlistDao.getItemsForPlaylist(playlistId)

    suspend fun createPlaylist(name: String): Long {
        return playlistDao.insertPlaylist(PlaylistEntity(name = name))
    }

    suspend fun deletePlaylist(playlistId: Long) {
        playlistDao.deletePlaylist(playlistId)
    }

    suspend fun addVideoToPlaylist(playlistId: Long, video: VideoItem) {
        playlistDao.insertPlaylistItem(
            PlaylistItemEntity(
                playlistId = playlistId,
                videoUri = video.contentUri,
                videoTitle = video.displayName,
                durationMs = video.durationMs,
                sizeBytes = video.sizeBytes
            )
        )
    }

    suspend fun removeVideoFromPlaylist(playlistId: Long, videoUri: String) {
        playlistDao.removePlaylistItem(playlistId, videoUri)
    }

    suspend fun removePlaylistItemById(itemId: Long) {
        playlistDao.removePlaylistItemById(itemId)
    }

    // Playback Progress & Resume
    suspend fun savePlaybackPosition(videoUri: String, positionMs: Long, durationMs: Long) {
        if (positionMs <= 0 || durationMs <= 0) return
        playbackProgressDao.saveProgress(
            PlaybackProgressEntity(
                videoUri = videoUri,
                lastPositionMs = positionMs,
                durationMs = durationMs
            )
        )
    }

    suspend fun getPlaybackProgress(videoUri: String): PlaybackProgressEntity? {
        return playbackProgressDao.getProgress(videoUri)
    }

    suspend fun clearProgress(videoUri: String) {
        playbackProgressDao.deleteProgress(videoUri)
    }

    suspend fun clearAllHistory() {
        playbackProgressDao.clearAllProgress()
    }
}
