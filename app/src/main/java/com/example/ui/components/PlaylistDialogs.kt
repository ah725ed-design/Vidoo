package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.PlaylistEntity
import com.example.data.model.VideoItem
import com.example.ui.theme.VidooBorder
import com.example.ui.theme.VidooOrange
import com.example.ui.theme.VidooSurface
import com.example.ui.theme.VidooTextPrimary
import com.example.ui.theme.VidooTextSecondary
import com.example.ui.theme.VidooTextTertiary

@Composable
fun CreatePlaylistDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = VidooSurface,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.PlaylistAdd, contentDescription = null, tint = VidooOrange)
                Text(
                    text = "New Playlist",
                    color = VidooTextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column {
                Text(
                    text = "Enter a name for your custom playlist:",
                    color = VidooTextSecondary,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (it.isNotBlank()) isError = false
                    },
                    placeholder = { Text("e.g., Favorites, Watch Later", color = VidooTextTertiary) },
                    singleLine = true,
                    isError = isError,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = VidooTextPrimary,
                        unfocusedTextColor = VidooTextPrimary,
                        focusedBorderColor = VidooOrange,
                        unfocusedBorderColor = VidooBorder,
                        cursorColor = VidooOrange
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("create_playlist_input")
                )
                if (isError) {
                    Text(
                        text = "Playlist name cannot be empty",
                        color = VidooOrange,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.trim().isNotEmpty()) {
                        onConfirm(name.trim())
                    } else {
                        isError = true
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = VidooOrange,
                    contentColor = androidx.compose.ui.graphics.Color.Black
                ),
                modifier = Modifier.testTag("create_playlist_confirm_button")
            ) {
                Text("Create", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = VidooTextSecondary)
            }
        }
    )
}

@Composable
fun AddToPlaylistDialog(
    video: VideoItem,
    playlists: List<PlaylistEntity>,
    onPlaylistSelected: (PlaylistEntity) -> Unit,
    onCreateNewClick: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = VidooSurface,
        title = {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.PlaylistAdd, contentDescription = null, tint = VidooOrange)
                    Text(
                        text = "Add to Playlist",
                        color = VidooTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = video.displayName,
                    color = VidooTextSecondary,
                    fontSize = 13.sp,
                    maxLines = 1
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Button to create new playlist
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCreateNewClick() }
                        .padding(vertical = 10.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = VidooOrange,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Create new playlist",
                        color = VidooOrange,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                HorizontalDivider(
                    color = VidooBorder,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                if (playlists.isEmpty()) {
                    Text(
                        text = "No custom playlists yet. Create one above!",
                        color = VidooTextTertiary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    LazyColumn(modifier = Modifier.height(200.dp)) {
                        items(playlists) { playlist ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onPlaylistSelected(playlist)
                                        onDismiss()
                                    }
                                    .padding(vertical = 10.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlaylistPlay,
                                    contentDescription = null,
                                    tint = VidooTextSecondary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = playlist.name,
                                    color = VidooTextPrimary,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = VidooTextSecondary)
            }
        }
    )
}

@Composable
fun VideoDetailsDialog(
    video: VideoItem,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = VidooSurface,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Info, contentDescription = null, tint = VidooOrange)
                Text(
                    text = "Video Details",
                    color = VidooTextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DetailItem(label = "File Name", value = video.displayName)
                DetailItem(label = "Folder", value = video.folderName)
                DetailItem(label = "Duration", value = video.formattedDuration())
                DetailItem(label = "File Size", value = video.formattedSize())
                video.resolutionText()?.let {
                    DetailItem(label = "Resolution", value = it)
                }
                DetailItem(label = "MIME Type", value = video.mimeType)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = VidooOrange)
            }
        }
    )
}

@Composable
private fun DetailItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            color = VidooTextTertiary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            color = VidooTextPrimary,
            fontSize = 14.sp
        )
    }
}
