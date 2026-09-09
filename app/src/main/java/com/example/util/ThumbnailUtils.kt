package com.example.util

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.provider.MediaStore
import android.util.LruCache
import android.util.Size
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.Collections

object VideoThumbnailHelper {
    private val memoryCache: LruCache<String, Bitmap> = LruCache(100)
    private val failedIds = Collections.synchronizedSet(HashSet<Long>())
    private val thumbnailMutex = Mutex()

    suspend fun getThumbnail(context: Context, videoId: Long, videoUriString: String): Bitmap? =
        withContext(Dispatchers.IO) {
            val cacheKey = "$videoId"
            memoryCache.get(cacheKey)?.let { return@withContext it }

            // If already known to fail or not a MediaStore content URI (e.g. sample or remote streams), skip immediately
            if (failedIds.contains(videoId)) return@withContext null
            if (!videoUriString.startsWith("content://", ignoreCase = true)) {
                failedIds.add(videoId)
                return@withContext null
            }

            // Serialize thumbnail extraction so concurrent queries never exhaust MediaCodec hardware resources
            thumbnailMutex.withLock {
                // Re-check cache inside lock
                memoryCache.get(cacheKey)?.let { return@withLock it }
                if (failedIds.contains(videoId)) return@withLock null

                try {
                    val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val uri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, videoId)
                        context.contentResolver.loadThumbnail(uri, Size(320, 240), null)
                    } else {
                        @Suppress("DEPRECATION")
                        MediaStore.Video.Thumbnails.getThumbnail(
                            context.contentResolver,
                            videoId,
                            MediaStore.Video.Thumbnails.MINI_KIND,
                            null
                        )
                    }
                    if (bitmap != null) {
                        memoryCache.put(cacheKey, bitmap)
                    } else {
                        failedIds.add(videoId)
                    }
                    bitmap
                } catch (_: Throwable) {
                    failedIds.add(videoId)
                    null
                }
            }
        }
}

