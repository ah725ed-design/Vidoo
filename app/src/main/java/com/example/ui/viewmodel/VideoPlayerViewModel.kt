package com.example.ui.viewmodel

import android.app.Application
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
    val errorMessage: String? = null
)

class VideoPlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = VideoRepository(application)
    private val settingsManager = SettingsManager(application)

    private val _uiState = MutableStateFlow(VideoUiState())
    val uiState: StateFlow<VideoUiState> = _uiState.asStateFlow()

    val playlists: StateFlow<List<PlaylistEntity>> = repository.getAllPlaylists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val settings: StateFlow<AppSettings> = settingsManager.settings

    private val _selectedPlaylist = MutableStateFlow<PlaylistEntity?>(null)
    val selectedPlaylist: StateFlow<PlaylistEntity?> = _selectedPlaylist.asStateFlow()

    private val _playlistItems = MutableStateFlow<List<PlaylistItemEntity>>(emptyList())
    val playlistItems: StateFlow<List<PlaylistItemEntity>> = _playlistItems.asStateFlow()

    fun updatePermissionState(granted: Boolean) {
        _uiState.value = _uiState.value.copy(permissionGranted = granted)
        if (granted) {
            refreshVideos()
        } else {
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun refreshVideos() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val videos = repository.queryLocalVideos()
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
}
