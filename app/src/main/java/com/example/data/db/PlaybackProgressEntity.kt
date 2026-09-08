package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playback_progress")
data class PlaybackProgressEntity(
    @PrimaryKey
    val videoUri: String,
    val lastPositionMs: Long,
    val durationMs: Long,
    val updatedAt: Long = System.currentTimeMillis()
)
