package com.example.util

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.LruCache
import android.util.Size
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object VideoThumbnailHelper {
    private val memoryCache: LruCache<String, Bitmap> = LruCache(100)

    suspend fun getThumbnail(context: Context, videoId: Long, videoUriString: String): Bitmap? =
        withContext(Dispatchers.IO) {
            val cacheKey = "$videoId"
            memoryCache.get(cacheKey)?.let { return@withContext it }

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
                }
                bitmap
            } catch (_: Exception) {
                null
            }
        }
}
