package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.VidooBlack
import com.example.ui.theme.VidooBorder
import com.example.ui.theme.VidooCardBg
import com.example.ui.theme.VidooOrange
import com.example.ui.theme.VidooOrangeGlow
import com.example.ui.theme.VidooSurface
import com.example.ui.theme.VidooTextPrimary
import com.example.ui.theme.VidooTextSecondary
import com.example.ui.theme.VidooTextTertiary
import com.example.ui.viewmodel.VideoPlayerViewModel

@Composable
fun SettingsScreen(
    viewModel: VideoPlayerViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(VidooBlack)
            .verticalScroll(scrollState)
            .padding(bottom = 32.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null,
                tint = VidooOrange,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Settings",
                color = VidooTextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Section: Playback
        SettingsSectionTitle(title = "Playback", icon = Icons.Default.PlayCircle)

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = VidooCardBg)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Resume playback toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Resume playback",
                            color = VidooTextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Remember and prompt to restore last watched position",
                            color = VidooTextTertiary,
                            fontSize = 12.sp
                        )
                    }

                    Switch(
                        checked = settings.resumePlayback,
                        onCheckedChange = { viewModel.setResumePlayback(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = VidooOrange,
                            uncheckedThumbColor = VidooTextSecondary,
                            uncheckedTrackColor = VidooSurface
                        ),
                        modifier = Modifier.testTag("resume_playback_switch")
                    )
                }

                HorizontalDivider(color = VidooBorder, modifier = Modifier.padding(vertical = 12.dp))

                // Clear history button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Clear playback history",
                            color = VidooTextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Reset saved progress for all videos",
                            color = VidooTextTertiary,
                            fontSize = 12.sp
                        )
                    }

                    OutlinedButton(
                        onClick = { viewModel.clearAllHistory() },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CleaningServices,
                            contentDescription = null,
                            tint = VidooOrange,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reset", color = VidooOrange, fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Section: Subtitle Appearance
        SettingsSectionTitle(title = "Subtitle Appearance", icon = Icons.Default.Subtitles)

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = VidooCardBg)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Live subtitle preview
                Text(
                    text = "Preview",
                    color = VidooTextTertiary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF000000))
                        .border(1.dp, VidooBorder, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    val textColor = when (settings.subtitleColor) {
                        "Orange" -> VidooOrange
                        "Yellow" -> Color(0xFFFFEB3B)
                        else -> Color.White
                    }

                    Surface(
                        color = if (settings.subtitleBackground) Color.Black.copy(alpha = 0.75f) else Color.Transparent,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "Sample Subtitle Text (Vidoo)",
                            color = textColor,
                            fontSize = settings.subtitleFontSizeSp.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Font size options
                Text(
                    text = "Font Size",
                    color = VidooTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val sizes = listOf(14f to "Small", 18f to "Medium", 24f to "Large")
                    sizes.forEach { (sizeSp, label) ->
                        val isSelected = settings.subtitleFontSizeSp == sizeSp
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setSubtitleFontSize(sizeSp) },
                            label = { Text(label, fontSize = 13.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VidooOrange,
                                selectedLabelColor = Color.Black,
                                containerColor = VidooSurface,
                                labelColor = VidooTextPrimary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) VidooOrange else VidooBorder
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Text Color options
                Text(
                    text = "Text Color",
                    color = VidooTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val colors = listOf("White", "Orange", "Yellow")
                    colors.forEach { colorName ->
                        val isSelected = settings.subtitleColor == colorName
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setSubtitleColor(colorName) },
                            label = { Text(colorName, fontSize = 13.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VidooOrange,
                                selectedLabelColor = Color.Black,
                                containerColor = VidooSurface,
                                labelColor = VidooTextPrimary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) VidooOrange else VidooBorder
                            )
                        )
                    }
                }

                HorizontalDivider(color = VidooBorder, modifier = Modifier.padding(vertical = 12.dp))

                // Background box toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Semi-transparent background",
                        color = VidooTextPrimary,
                        fontSize = 14.sp
                    )

                    Switch(
                        checked = settings.subtitleBackground,
                        onCheckedChange = { viewModel.setSubtitleBackground(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = VidooOrange,
                            uncheckedThumbColor = VidooTextSecondary,
                            uncheckedTrackColor = VidooSurface
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionTitle(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = VidooOrange,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            color = VidooOrange,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
