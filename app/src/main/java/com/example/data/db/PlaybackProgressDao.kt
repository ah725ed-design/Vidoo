package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaybackProgressDao {
    @Query("SELECT * FROM playback_progress WHERE videoUri = :videoUri LIMIT 1")
    suspend fun getProgress(videoUri: String): PlaybackProgressEntity?

    @Query("SELECT * FROM playback_progress WHERE videoUri = :videoUri LIMIT 1")
    fun observeProgress(videoUri: String): Flow<PlaybackProgressEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProgress(progress: PlaybackProgressEntity)

    @Query("DELETE FROM playback_progress WHERE videoUri = :videoUri")
    suspend fun deleteProgress(videoUri: String)

    @Query("DELETE FROM playback_progress")
    suspend fun clearAllProgress()
}
