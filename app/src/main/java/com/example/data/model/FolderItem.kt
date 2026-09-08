package com.example.data.model

data class FolderItem(
    val name: String,
    val videoCount: Int,
    val totalSizeBytes: Long,
    val firstVideoUri: String?
) {
    fun formattedTotalSize(): String {
        val mb = totalSizeBytes / (1024.0 * 1024.0)
        val gb = mb / 1024.0
        return if (gb >= 1.0) {
            String.format(java.util.Locale.US, "%.1f GB", gb)
        } else {
            String.format(java.util.Locale.US, "%.1f MB", mb)
        }
    }
}
