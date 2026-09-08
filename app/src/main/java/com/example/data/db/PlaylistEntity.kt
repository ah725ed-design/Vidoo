package com.example.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "playlist_items",
    foreignKeys = [
        ForeignKey(
            entity = PlaylistEntity::class,
            parentColumns = ["id"],
            childColumns = ["playlistId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("playlistId"),
        Index(value = ["playlistId", "videoUri"], unique = true)
    ]
)
data class PlaylistItemEntity(
    @PrimaryKey(autoGenerate = true)
    val itemId: Long = 0,
    val playlistId: Long,
    val videoUri: String,
    val videoTitle: String,
    val durationMs: Long,
    val sizeBytes: Long,
    val dateAdded: Long = System.currentTimeMillis()
)
