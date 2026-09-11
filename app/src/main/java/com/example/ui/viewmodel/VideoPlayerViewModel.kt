package com.example.ui.viewmodel

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.PlaylistEntity
import com.example.data.db.PlaylistItemEntity
import com.example.data.model.FolderItem
import com.example.data.model.VideoItem
import com.example.data.repository.AppSettings
import com.example.data.repository.SettingsManager
import com.example.data.repository.VideoRepository
import com.example.data.repository.VideoSortOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class VideoUiState(
    val isLoading: Boolean = true,
    val allVideos: List<VideoItem> = emptyList(),
    val filteredVideos: List<VideoItem> = emptyList(),
    val folders: List<FolderItem> = emptyList(),
    val selectedFolder: String? = null,
    val searchQuery: String = "",
    val sortOption: VideoSortOption = VideoSortOption.DATE_DESC,
    val isGridView: Boolean = false,
    val permissionGranted: Boolean = false,
    val isPermissionChecked: Boolean = true,
    val errorMessage: String? = null
)

class VideoPlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = VideoRepository(application)
    private val settingsManager = SettingsManager(application)

    // Synchronous initial permission check so UI never flashes a false permission card
    private val initialPermissionGranted = hasStoragePermission(application)

    private val _uiState = MutableStateFlow(
        VideoUiState(
            permissionGranted = initialPermissionGranted,
            isPermissionChecked = true,
            isLoading = initialPermissionGranted
        )
    )
    val uiState: StateFlow<VideoUiState> = _uiState.asStateFlow()

    private var initialScanScheduled = false

    var lastPlayedVideoId: Long? = null
        private set

    fun recordVideoPlayed(videoId: Long) {
        lastPlayedVideoId = videoId
    }

    /**
     * Defer full media scanning until after the first frame renders,
     * ensuring immediate first-frame drawing without blocking UI thread.
     */
    fun loadVideosAfterFirstFrame() {
        if (!initialScanScheduled && _uiState.value.permissionGranted) {
            initialScanScheduled = true
            refreshVideos()
        }
    }

    val playlists: StateFlow<List<PlaylistEntity>> = repository.getAllPlaylists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val settings: StateFlow<AppSettings> = settingsManager.settings

    private val _selectedPlaylist = MutableStateFlow<PlaylistEntity?>(null)
    val selectedPlaylist: StateFlow<PlaylistEntity?> = _selectedPlaylist.asStateFlow()

    private val _playlistItems = MutableStateFlow<List<PlaylistItemEntity>>(emptyList())
    val playlistItems: StateFlow<List<PlaylistItemEntity>> = _playlistItems.asStateFlow()

    init {
        viewModelScope.launch {
            settingsManager.settings.collect {
                applyFilterAndSort()
            }
        }
    }

    // Active playback queue (based on active folder/sort/search or playlist)
    private val _playbackQueue = MutableStateFlow<List<VideoItem>>(emptyList())
    val playbackQueue: StateFlow<List<VideoItem>> = _playbackQueue.asStateFlow()

    fun setPlaybackQueue(queue: List<VideoItem>) {
        _playbackQueue.value = queue
    }

    fun updatePermissionState(granted: Boolean) {
        val wasGranted = _uiState.value.permissionGranted
        _uiState.value = _uiState.value.copy(
            permissionGranted = granted,
            isPermissionChecked = true
        )
        if (granted) {
            if (!wasGranted || _uiState.value.allVideos.isEmpty()) {
                refreshVideos()
            }
        } else {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                allVideos = emptyList(),
                filteredVideos = emptyList(),
                folders = emptyList()
            )
        }
    }

    fun refreshVideos() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val videos = repository.queryLocalVideos()
            withContext(Dispatchers.Default) {
                val folders = repository.groupVideosByFolders(videos)
                val sorted = repository.sortVideos(videos, _uiState.value.sortOption)
                val filtered = filterAndSort(sorted, _uiState.value.searchQuery, _uiState.value.selectedFolder)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    allVideos = videos,
                    folders = folders,
                    filteredVideos = filtered
                )
            }
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        applyFilterAndSort()
    }

    fun setSortOption(sortOption: VideoSortOption) {
        _uiState.value = _uiState.value.copy(sortOption = sortOption)
        applyFilterAndSort()
    }

    fun selectFolder(folderName: String?) {
        _uiState.value = _uiState.value.copy(selectedFolder = folderName)
        applyFilterAndSort()
    }

    fun toggleGridView() {
        _uiState.value = _uiState.value.copy(isGridView = !_uiState.value.isGridView)
    }

    private fun applyFilterAndSort() {
        val sorted = repository.sortVideos(_uiState.value.allVideos, _uiState.value.sortOption)
        val filtered = filterAndSort(sorted, _uiState.value.searchQuery, _uiState.value.selectedFolder)
        _uiState.value = _uiState.value.copy(filteredVideos = filtered)
    }

    private fun filterAndSort(
        videos: List<VideoItem>,
        query: String,
        folder: String?
    ): List<VideoItem> {
        var list = videos
        if (folder != null) {
            list = list.filter { it.folderName.equals(folder, ignoreCase = true) }
        }
        if (settings.value.hideShortVideos) {
            val minDurationMs = settings.value.shortVideoThresholdSec * 1000L
            list = list.filter { it.durationMs >= minDurationMs }
        }
        if (query.isNotBlank()) {
            list = list.filter {
                it.displayName.contains(query, ignoreCase = true) ||
                        it.title.contains(query, ignoreCase = true) ||
                        it.folderName.contains(query, ignoreCase = true)
            }
        }
        return list
    }

    // Playlist operations
    fun createPlaylist(name: String) {
        viewModelScope.launch {
            repository.createPlaylist(name)
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            repository.deletePlaylist(playlistId)
            if (_selectedPlaylist.value?.id == playlistId) {
                _selectedPlaylist.value = null
            }
        }
    }

    fun selectPlaylist(playlist: PlaylistEntity?) {
        _selectedPlaylist.value = playlist
        if (playlist != null) {
            viewModelScope.launch {
                repository.getPlaylistItems(playlist.id).collect { items ->
                    _playlistItems.value = items
                }
            }
        } else {
            _playlistItems.value = emptyList()
        }
    }

    fun addVideoToPlaylist(playlistId: Long, video: VideoItem) {
        viewModelScope.launch {
            repository.addVideoToPlaylist(playlistId, video)
        }
    }

    fun removePlaylistItem(itemId: Long) {
        viewModelScope.launch {
            repository.removePlaylistItemById(itemId)
        }
    }

    // Settings
    fun setResumePlayback(enabled: Boolean) = settingsManager.setResumePlayback(enabled)
    fun setSubtitleFontSize(sizeSp: Float) = settingsManager.setSubtitleFontSize(sizeSp)
    fun setSubtitleColor(color: String) = settingsManager.setSubtitleColor(color)
    fun setSubtitleBackground(enabled: Boolean) = settingsManager.setSubtitleBackground(enabled)
    fun setAmoledBlack(enabled: Boolean) = settingsManager.setAmoledBlack(enabled)
    fun setDefaultPlaybackSpeed(speed: Float) = settingsManager.setDefaultPlaybackSpeed(speed)
    fun setAutoLock(enabled: Boolean) = settingsManager.setAutoLock(enabled)
    fun setAutoLockTimeoutSec(seconds: Int) = settingsManager.setAutoLockTimeoutSec(seconds)
    fun setHideShortVideos(enabled: Boolean) = settingsManager.setHideShortVideos(enabled)
    fun setShortVideoThresholdSec(seconds: Int) = settingsManager.setShortVideoThresholdSec(seconds)
    fun setAppLanguage(langCode: String) = settingsManager.setAppLanguage(langCode)
    fun setHasSeenGestureGuide(seen: Boolean) = settingsManager.setHasSeenGestureGuide(seen)

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAllHistory()
        }
    }

    suspend fun getSavedPosition(videoUri: String): Long {
        if (!settings.value.resumePlayback) return 0L
        return repository.getPlaybackProgress(videoUri)?.lastPositionMs ?: 0L
    }

    fun saveProgress(videoUri: String, positionMs: Long, durationMs: Long) {
        if (!settings.value.resumePlayback) return
        viewModelScope.launch {
            repository.savePlaybackPosition(videoUri, positionMs, durationMs)
        }
    }

    companion object {
        fun getRequiredPermission(): String {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Manifest.permission.READ_MEDIA_VIDEO
            } else {
                Manifest.permission.READ_EXTERNAL_STORAGE
            }
        }

        fun hasStoragePermission(context: Context): Boolean {
            // Check Android 13+ granular media video permission
            val hasMediaVideo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_MEDIA_VIDEO
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                false
            }

            // Check standard READ_EXTERNAL_STORAGE permission
            val hasExternalStorage = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED

            // Check Android 14+ partial visual user selected permission
            val hasPartialVisual = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ContextCompat.checkSelfPermission(
                    context,
                    "android.permission.READ_MEDIA_VISUAL_USER_SELECTED"
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                false
            }

            return hasMediaVideo || hasExternalStorage || hasPartialVisual
        }
    }
}
