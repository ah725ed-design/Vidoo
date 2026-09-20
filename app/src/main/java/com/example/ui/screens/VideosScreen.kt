package com.example.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VideoItem
import com.example.ui.components.AddToPlaylistDialog
import com.example.ui.components.CreatePlaylistDialog
import com.example.ui.components.SortDialog
import com.example.ui.components.VideoDetailsDialog
import com.example.ui.components.VideoGridItem
import com.example.ui.components.VideoListItem
import com.example.ui.components.VideoGridSkeleton
import com.example.ui.components.VideoListSkeleton
import com.example.ui.theme.VidooBlack
import com.example.ui.theme.VidooBorder
import com.example.ui.theme.VidooOrange
import com.example.ui.theme.VidooSurface
import com.example.ui.theme.VidooSurfaceVariant
import com.example.ui.theme.VidooTextPrimary
import com.example.ui.theme.VidooTextSecondary
import com.example.ui.theme.VidooTextTertiary
import com.example.ui.viewmodel.VideoPlayerViewModel
import com.example.util.LocalAppStrings

@Composable
fun VideosScreen(
    viewModel: VideoPlayerViewModel,
    onPlayVideo: (VideoItem) -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    gridState: LazyGridState = rememberLazyGridState()
) {
    val uiState by viewModel.uiState.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val strings = LocalAppStrings.current

    // Trigger deferred media scanning asynchronously after first frame renders
    LaunchedEffect(Unit) {
        viewModel.loadVideosAfterFirstFrame()
    }

    var showSortDialog by remember { mutableStateOf(false) }
    var videoForPlaylist by remember { mutableStateOf<VideoItem?>(null) }
    var videoForDetails by remember { mutableStateOf<VideoItem?>(null) }
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(VidooBlack)
    ) {
        // Search & Controls Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Search bar (Capsule design with perfectly aligned and centered text)
                var isSearchFocused by remember { mutableStateOf(false) }
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(CircleShape)
                        .background(VidooSurface)
                        .border(
                            width = 1.dp,
                            color = if (isSearchFocused) VidooOrange else VidooBorder,
                            shape = CircleShape
                        )
                        .padding(start = 7.dp, end = 12.dp)
                        .testTag("video_search_input"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Circular Search icon badge
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(VidooOrange.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = strings.searchVideosPlaceholder,
                            tint = VidooOrange,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Text & Placeholder container perfectly centered vertically
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (uiState.searchQuery.isEmpty()) {
                            Text(
                                text = strings.searchVideosPlaceholder,
                                color = VidooTextTertiary,
                                fontSize = 14.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        BasicTextField(
                            value = uiState.searchQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            singleLine = true,
                            textStyle = TextStyle(
                                color = VidooTextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal
                            ),
                            cursorBrush = SolidColor(VidooOrange),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { isSearchFocused = it.isFocused }
                        )
                    }

                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { viewModel.setSearchQuery("") },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = strings.clear,
                                tint = VidooTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // Sort Button
                IconButton(
                    onClick = { showSortDialog = true },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(VidooSurface)
                        .testTag("sort_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Sort,
                        contentDescription = strings.sortVideos,
                        tint = VidooOrange
                    )
                }

                // Grid / List Toggle
                IconButton(
                    onClick = { viewModel.toggleGridView() },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(VidooSurface)
                        .testTag("toggle_view_button")
                ) {
                    Icon(
                        imageVector = if (uiState.isGridView) Icons.Default.ViewList else Icons.Default.GridView,
                        contentDescription = strings.toggleView,
                        tint = VidooTextPrimary
                    )
                }
            }

            // Active folder filter chip (if selected)
            if (uiState.selectedFolder != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    InputChip(
                        selected = true,
                        onClick = { viewModel.selectFolder(null) },
                        label = { Text("${strings.folderPrefix}: ${uiState.selectedFolder}", fontSize = 12.sp) },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = strings.clear,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = InputChipDefaults.inputChipColors(
                            selectedContainerColor = VidooOrange.copy(alpha = 0.15f),
                            selectedLabelColor = VidooOrange,
                            selectedTrailingIconColor = VidooOrange
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "(${uiState.filteredVideos.size} ${if (uiState.filteredVideos.size == 1) strings.videoCountSingle else strings.videoCountPlural})",
                        color = VidooTextTertiary,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Content Area with smooth transition from skeleton loading to video list
        Box(modifier = Modifier.fillMaxSize()) {
            Crossfade(
                targetState = uiState.isLoading,
                animationSpec = tween(durationMillis = 280),
                label = "videos_content_fade"
            ) { loading ->
                if (loading) {
                    if (uiState.isGridView) {
                        VideoGridSkeleton()
                    } else {
                        VideoListSkeleton()
                    }
                } else if (uiState.filteredVideos.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(VidooSurfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VideoLibrary,
                                    contentDescription = null,
                                    tint = VidooTextTertiary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (uiState.searchQuery.isNotEmpty()) strings.noMatchingVideos else strings.noVideosFound,
                                color = VidooTextPrimary,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (uiState.searchQuery.isNotEmpty()) strings.noMatchingVideosDesc else strings.noVideosFoundDesc,
                                color = VidooTextSecondary,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    if (uiState.isGridView) {
                        LazyVerticalGrid(
                            state = gridState,
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(uiState.filteredVideos, key = { it.id }) { video ->
                                VideoGridItem(
                                    video = video,
                                    onVideoClick = {
                                        viewModel.setPlaybackQueue(uiState.filteredVideos)
                                        viewModel.recordVideoPlayed(video.id)
                                        onPlayVideo(video)
                                    },
                                    onAddToPlaylistClick = { videoForPlaylist = it },
                                    onDetailsClick = { videoForDetails = it }
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            state = listState,
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(uiState.filteredVideos, key = { it.id }) { video ->
                                VideoListItem(
                                    video = video,
                                    onVideoClick = {
                                        viewModel.setPlaybackQueue(uiState.filteredVideos)
                                        viewModel.recordVideoPlayed(video.id)
                                        onPlayVideo(video)
                                    },
                                    onAddToPlaylistClick = { videoForPlaylist = it },
                                    onDetailsClick = { videoForDetails = it }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Sort Dialog
    if (showSortDialog) {
        SortDialog(
            currentSort = uiState.sortOption,
            onSortSelected = { viewModel.setSortOption(it) },
            onDismiss = { showSortDialog = false }
        )
    }

    // Add to Playlist Dialog
    videoForPlaylist?.let { video ->
        AddToPlaylistDialog(
            video = video,
            playlists = playlists,
            onPlaylistSelected = { playlist ->
                viewModel.addVideoToPlaylist(playlist.id, video)
            },
            onCreateNewClick = {
                showCreatePlaylistDialog = true
            },
            onDismiss = { videoForPlaylist = null }
        )
    }

    // Create Playlist Dialog (from Add to Playlist)
    if (showCreatePlaylistDialog) {
        CreatePlaylistDialog(
            onConfirm = { name ->
                viewModel.createPlaylist(name)
                showCreatePlaylistDialog = false
            },
            onDismiss = { showCreatePlaylistDialog = false }
        )
    }

    // Details Dialog
    videoForDetails?.let { video ->
        VideoDetailsDialog(
            video = video,
            onDismiss = { videoForDetails = null }
        )
    }
}
