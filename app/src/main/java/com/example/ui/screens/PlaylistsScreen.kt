package com.example.ui.screens

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.PlaylistEntity
import com.example.data.db.PlaylistItemEntity
import com.example.data.model.VideoItem
import com.example.ui.components.CreatePlaylistDialog
import com.example.ui.theme.VidooBlack
import com.example.ui.theme.VidooCardBg
import com.example.ui.theme.VidooOrange
import com.example.ui.theme.VidooOrangeGlow
import com.example.ui.theme.VidooSurface
import com.example.ui.theme.VidooSurfaceVariant
import com.example.ui.theme.VidooTextPrimary
import com.example.ui.theme.VidooTextSecondary
import com.example.ui.theme.VidooTextTertiary
import com.example.ui.viewmodel.VideoPlayerViewModel
import com.example.util.LocalAppStrings

@Composable
fun PlaylistsScreen(
    viewModel: VideoPlayerViewModel,
    onPlayVideo: (VideoItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val playlists by viewModel.playlists.collectAsState()
    val selectedPlaylist by viewModel.selectedPlaylist.collectAsState()
    val playlistItems by viewModel.playlistItems.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val strings = LocalAppStrings.current

    var showCreateDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(VidooBlack)
    ) {
        if (selectedPlaylist != null) {
            // Detailed View of Selected Playlist
            PlaylistDetailView(
                playlist = selectedPlaylist!!,
                items = playlistItems,
                onBack = { viewModel.selectPlaylist(null) },
                onPlayAll = {
                    if (playlistItems.isNotEmpty()) {
                        val queue = playlistItems.map { item ->
                            uiState.allVideos.find { it.contentUri == item.videoUri }
                                ?: VideoItem(
                                    id = 0L,
                                    contentUri = item.videoUri,
                                    title = item.videoTitle,
                                    displayName = item.videoTitle,
                                    durationMs = item.durationMs,
                                    sizeBytes = item.sizeBytes,
                                    dateModified = 0L,
                                    folderName = "Playlist"
                                )
                        }
                        viewModel.setPlaybackQueue(queue)
                        onPlayVideo(queue.first())
                    }
                },
                onPlayItem = { item ->
                    val queue = playlistItems.map { pi ->
                        uiState.allVideos.find { it.contentUri == pi.videoUri }
                            ?: VideoItem(
                                id = 0L,
                                contentUri = pi.videoUri,
                                title = pi.videoTitle,
                                displayName = pi.videoTitle,
                                durationMs = pi.durationMs,
                                sizeBytes = pi.sizeBytes,
                                dateModified = 0L,
                                folderName = "Playlist"
                            )
                    }
                    viewModel.setPlaybackQueue(queue)
                    val targetVideo = queue.find { it.contentUri == item.videoUri } ?: queue.first()
                    onPlayVideo(targetVideo)
                },
                onDeleteItem = { item ->
                    viewModel.removePlaylistItem(item.itemId)
                },
                onDeletePlaylist = {
                    viewModel.deletePlaylist(selectedPlaylist!!.id)
                }
            )
        } else {
            // List of Playlists View
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.PlaylistPlay,
                        contentDescription = null,
                        tint = VidooOrange,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = strings.playlistsTitle,
                        color = VidooTextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (playlists.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(VidooSurfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlaylistPlay,
                                    contentDescription = null,
                                    tint = VidooTextTertiary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = strings.noPlaylistsYet,
                                color = VidooTextPrimary,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = strings.noPlaylistsYetDesc,
                                color = VidooTextSecondary,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(playlists, key = { it.id }) { playlist ->
                            PlaylistItemCard(
                                playlist = playlist,
                                modifier = Modifier.animateItem(
                                    fadeInSpec = tween(220),
                                    fadeOutSpec = tween(180),
                                    placementSpec = tween(250)
                                ),
                                onClick = { viewModel.selectPlaylist(playlist) },
                                onDelete = { viewModel.deletePlaylist(playlist.id) }
                            )
                        }
                    }
                }
            }

            // Floating Action Button to create playlist
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = VidooOrange,
                contentColor = Color.Black,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
                    .testTag("create_playlist_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = strings.createPlaylist)
            }
        }
    }

    if (showCreateDialog) {
        CreatePlaylistDialog(
            onConfirm = { name ->
                viewModel.createPlaylist(name)
                showCreateDialog = false
            },
            onDismiss = { showCreateDialog = false }
        )
    }
}

@Composable
fun PlaylistItemCard(
    playlist: PlaylistEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = VidooCardBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(VidooOrangeGlow),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlaylistPlay,
                    contentDescription = null,
                    tint = VidooOrange,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = playlist.name,
                    color = VidooTextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = strings.customPlaylistSubtitle,
                    color = VidooTextTertiary,
                    fontSize = 12.sp
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = strings.deletePlaylist,
                    tint = VidooTextTertiary
                )
            }
        }
    }
}

@Composable
fun PlaylistDetailView(
    playlist: PlaylistEntity,
    items: List<PlaylistItemEntity>,
    onBack: () -> Unit,
    onPlayAll: () -> Unit,
    onPlayItem: (PlaylistItemEntity) -> Unit,
    onDeleteItem: (PlaylistItemEntity) -> Unit,
    onDeletePlaylist: () -> Unit
) {
    val strings = LocalAppStrings.current

    Column(modifier = Modifier.fillMaxSize()) {
        // Detail Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = strings.close,
                    tint = VidooTextPrimary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = playlist.name,
                    color = VidooTextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${items.size} ${if (items.size == 1) strings.videoCountSingle else strings.videoCountPlural}",
                    color = VidooTextTertiary,
                    fontSize = 12.sp
                )
            }
            IconButton(onClick = onDeletePlaylist) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = strings.deletePlaylist,
                    tint = Color(0xFFFF5252)
                )
            }
        }

        // Action Row
        if (items.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Button(
                    onClick = onPlayAll,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VidooOrange,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("${strings.playAll} (${items.size})", fontWeight = FontWeight.Bold)
                }
            }
        }

        if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = strings.emptyPlaylistDesc,
                        color = VidooTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(items, key = { it.itemId }) { item ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = VidooCardBg),
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItem(
                                fadeInSpec = tween(220),
                                fadeOutSpec = tween(180),
                                placementSpec = tween(250)
                            )
                            .clickable { onPlayItem(item) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(VidooOrangeGlow),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = VidooOrange,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.videoTitle,
                                    color = VidooTextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1
                                )
                            }
                            IconButton(onClick = { onDeleteItem(item) }) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = strings.deletePlaylistItem,
                                    tint = VidooTextTertiary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
