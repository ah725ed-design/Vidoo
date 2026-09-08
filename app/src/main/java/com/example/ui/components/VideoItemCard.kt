package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VideoItem
import com.example.ui.theme.VidooBorder
import com.example.ui.theme.VidooCardBg
import com.example.ui.theme.VidooOrange
import com.example.ui.theme.VidooSurface
import com.example.ui.theme.VidooTextPrimary
import com.example.ui.theme.VidooTextSecondary
import com.example.ui.theme.VidooTextTertiary
import com.example.util.VideoThumbnailHelper

@Composable
fun VideoListItem(
    video: VideoItem,
    onVideoClick: (VideoItem) -> Unit,
    onAddToPlaylistClick: (VideoItem) -> Unit,
    onDetailsClick: (VideoItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var thumbnail by remember(video.id) { mutableStateOf<Bitmap?>(null) }
    var showMenu by remember { mutableStateOf(false) }

    LaunchedEffect(video.id) {
        thumbnail = VideoThumbnailHelper.getThumbnail(context, video.id, video.contentUri)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("video_card_${video.id}")
            .clickable { onVideoClick(video) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = VidooCardBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail container with duration badge
            Box(
                modifier = Modifier
                    .width(116.dp)
                    .height(72.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(VidooSurface),
                contentAlignment = Alignment.Center
            ) {
                if (thumbnail != null) {
                    Image(
                        bitmap = thumbnail!!.asImageBitmap(),
                        contentDescription = video.displayName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF1B1B20)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = VidooOrange.copy(alpha = 0.6f),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                // Duration badge
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp),
                    color = Color.Black.copy(alpha = 0.75f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = video.formattedDuration(),
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Video Details
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = video.displayName,
                    color = VidooTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = null,
                        tint = VidooTextTertiary,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = video.folderName,
                        color = VidooTextTertiary,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = video.formattedSize(),
                        color = VidooTextSecondary,
                        fontSize = 12.sp
                    )
                    video.resolutionText()?.let { res ->
                        Surface(
                            color = VidooOrange.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(3.dp)
                        ) {
                            Text(
                                text = res,
                                color = VidooOrange,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
            }

            // More Menu
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Video options",
                        tint = VidooTextSecondary
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(VidooSurface)
                ) {
                    DropdownMenuItem(
                        text = { Text("Play Video", color = VidooTextPrimary) },
                        leadingIcon = {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = VidooOrange)
                        },
                        onClick = {
                            showMenu = false
                            onVideoClick(video)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Add to Playlist", color = VidooTextPrimary) },
                        leadingIcon = {
                            Icon(Icons.Default.PlaylistAdd, contentDescription = null, tint = VidooOrange)
                        },
                        onClick = {
                            showMenu = false
                            onAddToPlaylistClick(video)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Video Details", color = VidooTextPrimary) },
                        leadingIcon = {
                            Icon(Icons.Outlined.Info, contentDescription = null, tint = VidooTextSecondary)
                        },
                        onClick = {
                            showMenu = false
                            onDetailsClick(video)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun VideoGridItem(
    video: VideoItem,
    onVideoClick: (VideoItem) -> Unit,
    onAddToPlaylistClick: (VideoItem) -> Unit,
    onDetailsClick: (VideoItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var thumbnail by remember(video.id) { mutableStateOf<Bitmap?>(null) }
    var showMenu by remember { mutableStateOf(false) }

    LaunchedEffect(video.id) {
        thumbnail = VideoThumbnailHelper.getThumbnail(context, video.id, video.contentUri)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("video_grid_card_${video.id}")
            .clickable { onVideoClick(video) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = VidooCardBg)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(VidooSurface),
                contentAlignment = Alignment.Center
            ) {
                if (thumbnail != null) {
                    Image(
                        bitmap = thumbnail!!.asImageBitmap(),
                        contentDescription = video.displayName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF1B1B20)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = VidooOrange.copy(alpha = 0.6f),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp),
                    color = Color.Black.copy(alpha = 0.75f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = video.formattedDuration(),
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = video.displayName,
                        color = VidooTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Options",
                                tint = VidooTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier.background(VidooSurface)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Play", color = VidooTextPrimary) },
                                leadingIcon = { Icon(Icons.Default.PlayArrow, null, tint = VidooOrange) },
                                onClick = {
                                    showMenu = false
                                    onVideoClick(video)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Add to Playlist", color = VidooTextPrimary) },
                                leadingIcon = { Icon(Icons.Default.PlaylistAdd, null, tint = VidooOrange) },
                                onClick = {
                                    showMenu = false
                                    onAddToPlaylistClick(video)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Details", color = VidooTextPrimary) },
                                leadingIcon = { Icon(Icons.Outlined.Info, null, tint = VidooTextSecondary) },
                                onClick = {
                                    showMenu = false
                                    onDetailsClick(video)
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = video.formattedSize(),
                        color = VidooTextTertiary,
                        fontSize = 11.sp
                    )

                    video.resolutionText()?.let { res ->
                        Text(
                            text = res,
                            color = VidooOrange,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
