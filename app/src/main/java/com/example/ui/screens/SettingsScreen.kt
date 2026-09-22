package com.example.ui.screens

import android.app.LocaleManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.LocaleList
import android.os.StatFs
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.repository.VideoSortOption
import com.example.ui.theme.VidooBlack
import com.example.ui.theme.VidooBorder
import com.example.ui.theme.VidooCardBg
import com.example.ui.theme.VidooDarkCharcoal
import com.example.ui.theme.VidooOrange
import com.example.ui.theme.VidooOrangeGlow
import com.example.ui.theme.VidooSurface
import com.example.ui.theme.VidooSurfaceVariant
import com.example.ui.theme.VidooTextPrimary
import com.example.ui.theme.VidooTextSecondary
import com.example.ui.theme.VidooTextTertiary
import com.example.ui.viewmodel.VideoPlayerViewModel
import com.example.util.LocalAppStrings
import java.util.Locale

@Composable
fun SettingsScreen(
    viewModel: VideoPlayerViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val strings = LocalAppStrings.current
    val settings by viewModel.settings.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    var showClearHistoryDialog by remember { mutableStateOf(false) }

    // Read device internal storage statistics
    val storageStats = remember { getStorageStats() }

    // Total detected video stats
    val totalVideosSize = remember(uiState.allVideos) {
        uiState.allVideos.sumOf { it.sizeBytes }
    }
    val totalVideosCount = uiState.allVideos.size

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(VidooBlack)
            .verticalScroll(scrollState)
            .padding(bottom = 48.dp)
    ) {
        // ==========================================
        // Top App Bar / Title Header
        // ==========================================
        SettingsHeader(title = strings.settingsTitle)

        Spacer(modifier = Modifier.height(12.dp))

        // ==========================================
        // 1. PLAYBACK SECTION
        // ==========================================
        SettingsSectionTitle(
            title = strings.sectionPlayback,
            icon = Icons.Default.PlayCircle
        )

        SettingsCardContainer {
            // A. Resume playback
            SettingsToggleRow(
                title = strings.resumePlaybackTitle,
                subtitle = strings.resumePlaybackSubtitle,
                checked = settings.resumePlayback,
                onCheckedChange = { viewModel.setResumePlayback(it) },
                testTag = "resume_playback_switch"
            )

            SettingsDivider()

            // B. Clear playback history
            SettingsActionRow(
                title = strings.clearHistoryTitle,
                subtitle = strings.clearHistorySubtitle,
                actionText = strings.clear,
                actionIcon = Icons.Default.CleaningServices,
                onClick = { showClearHistoryDialog = true },
                testTag = "clear_history_button"
            )

            SettingsDivider()

            // C. Default playback speed
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = strings.defaultSpeedTitle,
                    color = VidooTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = strings.defaultSpeedSubtitle,
                    color = VidooTextTertiary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Scrollable chips to ensure never clipping on small screens
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val speeds = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
                    speeds.forEach { speed ->
                        val isSelected = settings.defaultPlaybackSpeed == speed
                        OptionPill(
                            text = "${speed}x",
                            isSelected = isSelected,
                            onClick = { viewModel.setDefaultPlaybackSpeed(speed) }
                        )
                    }
                }
            }

            SettingsDivider()

            // D. Auto-lock screen
            SettingsToggleRow(
                title = strings.autoLockTitle,
                subtitle = strings.autoLockSubtitle,
                checked = settings.autoLock,
                onCheckedChange = { viewModel.setAutoLock(it) },
                testTag = "auto_lock_switch"
            )

            AnimatedVisibility(
                visible = settings.autoLock,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .background(VidooSurface, RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = VidooOrange,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = strings.autoLockTimeoutTitle,
                            color = VidooOrange,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val timeouts = listOf(15 to "15s", 30 to "30s", 60 to "1 min", 120 to "2 min")
                        timeouts.forEach { (sec, label) ->
                            val isSelected = settings.autoLockTimeoutSec == sec
                            OptionPill(
                                text = label,
                                isSelected = isSelected,
                                onClick = { viewModel.setAutoLockTimeoutSec(sec) }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ==========================================
        // 2. LIBRARY SECTION
        // ==========================================
        SettingsSectionTitle(
            title = strings.sectionLibrary,
            icon = Icons.Default.VideoLibrary
        )

        SettingsCardContainer {
            // A. Sort order
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = strings.sortOrderTitle,
                    color = VidooTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = strings.sortOrderSubtitle,
                    color = VidooTextTertiary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                val currentSort = uiState.sortOption
                val sortOptions = listOf(
                    VideoSortOption.DATE_DESC to strings.sortNewest,
                    VideoSortOption.NAME_ASC to strings.sortNameAsc,
                    VideoSortOption.SIZE_DESC to strings.sortSizeLargest
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    sortOptions.forEach { (option, label) ->
                        val isSelected = currentSort == option
                        OptionPill(
                            text = label,
                            isSelected = isSelected,
                            onClick = { viewModel.setSortOption(option) }
                        )
                    }
                }
            }

            SettingsDivider()

            // B. Hide short videos
            SettingsToggleRow(
                title = strings.hideShortVideosTitle,
                subtitle = strings.hideShortVideosSubtitle,
                checked = settings.hideShortVideos,
                onCheckedChange = { viewModel.setHideShortVideos(it) },
                testTag = "hide_short_videos_switch"
            )

            AnimatedVisibility(
                visible = settings.hideShortVideos,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .background(VidooSurface, RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = strings.shortDurationThresholdTitle,
                        color = VidooOrange,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val thresholds = listOf(30 to "< 30s", 60 to "< 60s", 120 to "< 2 min")
                        thresholds.forEach { (sec, label) ->
                            val isSelected = settings.shortVideoThresholdSec == sec
                            OptionPill(
                                text = label,
                                isSelected = isSelected,
                                onClick = { viewModel.setShortVideoThresholdSec(sec) }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ==========================================
        // 4. STORAGE SECTION
        // ==========================================
        SettingsSectionTitle(
            title = strings.sectionStorage,
            icon = Icons.Default.Storage
        )

        SettingsCardContainer {
            // A. Storage Progress & Stats
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.storageUsageTitle,
                        color = VidooTextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = VidooOrange.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, VidooOrange.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = "${(storageStats.usedRatio * 100).toInt()}% ${strings.storageUsed}",
                            color = VidooOrange,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "${storageStats.usedFormatted} / ${storageStats.totalFormatted} • ${storageStats.availableFormatted} ${strings.storageFree}",
                    color = VidooTextSecondary,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                LinearProgressIndicator(
                    progress = { storageStats.usedRatio },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = VidooOrange,
                    trackColor = VidooSurface
                )
            }

            SettingsDivider()

            // B. Total detected video media
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(VidooSurface),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = null,
                        tint = VidooOrange,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = strings.storageTotalVideosSizeTitle,
                        color = VidooTextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    val formattedVideoSize = formatByteSize(totalVideosSize)
                    Text(
                        text = "$formattedVideoSize • $totalVideosCount ${if (totalVideosCount == 1) strings.videoCountSingle else strings.videoCountPlural}",
                        color = VidooOrange,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ==========================================
        // 5. LANGUAGE SECTION
        // ==========================================
        SettingsSectionTitle(
            title = strings.sectionLanguage,
            icon = Icons.Default.Language
        )

        SettingsCardContainer {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = strings.appLanguageTitle,
                    color = VidooTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = strings.appLanguageSubtitle,
                    color = VidooTextTertiary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(14.dp))

                val isEnglish = settings.appLanguage == "en"
                val isArabic = settings.appLanguage == "ar"

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LanguageSelectionButton(
                        text = strings.langEnglish,
                        subText = "English",
                        isSelected = isEnglish,
                        onClick = { applyAppLanguage(context, "en", viewModel) },
                        modifier = Modifier.weight(1f)
                    )

                    LanguageSelectionButton(
                        text = strings.langArabic,
                        subText = "العربية",
                        isSelected = isArabic,
                        onClick = { applyAppLanguage(context, "ar", viewModel) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ==========================================
        // 6. ABOUT SECTION
        // ==========================================
        SettingsSectionTitle(
            title = strings.sectionAbout,
            icon = Icons.Default.Info
        )

        SettingsCardContainer {
            // App Brand Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(VidooOrange.copy(alpha = 0.12f))
                        .border(1.dp, VidooOrange.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_vidoo_symbol),
                        contentDescription = "Vidoo Logo",
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Vidoo",
                        color = VidooTextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${strings.versionLabel} 1.0 (Build 1)",
                        color = VidooOrange,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // App Offline Tag
            Surface(
                color = VidooSurface,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, VidooBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = strings.appDescription,
                    color = VidooOrange,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                )
            }

            SettingsDivider()

            // Developer Information
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(VidooSurface, RoundedCornerShape(12.dp))
                    .border(1.dp, VidooBorder, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Text(
                    text = strings.developerInfoTitle,
                    color = VidooOrange,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.3.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Developer Name: Ahmad Asaad
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(VidooDarkCharcoal),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = strings.developerLabel,
                            tint = VidooOrange,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = strings.developerLabel,
                            color = VidooTextTertiary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(1.dp))
                        Text(
                            text = strings.developerName,
                            color = VidooTextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Phone Contact with Call Intent
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(VidooDarkCharcoal)
                        .clickable {
                            try {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${strings.developerContact}"))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "${strings.contactLabel}: ${strings.developerContact}", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(VidooOrange.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = strings.contactLabel,
                            tint = VidooOrange,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = strings.contactLabel,
                            color = VidooTextTertiary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = strings.developerContact,
                            color = VidooOrange,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    // Confirmation dialog for clearing playback history
    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = {
                Text(
                    text = strings.clearHistoryDialogTitle,
                    color = VidooTextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = strings.clearHistoryDialogDesc,
                    color = VidooTextSecondary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllHistory()
                        showClearHistoryDialog = false
                        Toast.makeText(context, strings.clearHistorySubtitle, Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text(strings.clear, color = VidooOrange, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text(strings.cancel, color = VidooTextTertiary)
                }
            },
            containerColor = VidooCardBg,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

// ==========================================
// Reusable Layout Components
// ==========================================

@Composable
private fun SettingsHeader(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(VidooOrange.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = title,
                tint = VidooOrange,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(
                text = title,
                color = VidooTextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SettingsSectionTitle(title: String, icon: ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
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
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SettingsCardContainer(
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = VidooCardBg),
        border = BorderStroke(1.dp, VidooBorder.copy(alpha = 0.7f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        color = VidooBorder.copy(alpha = 0.6f),
        thickness = 1.dp
    )
}

@Composable
private fun SettingsToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    badgeText: String? = null,
    badgeHighlight: Boolean = false,
    testTag: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = title,
                    color = VidooTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (badgeText != null) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (badgeHighlight) VidooOrange.copy(alpha = 0.2f) else VidooSurface,
                        border = BorderStroke(1.dp, if (badgeHighlight) VidooOrange else VidooBorder)
                    ) {
                        Text(
                            text = badgeText,
                            color = if (badgeHighlight) VidooOrange else VidooTextTertiary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = subtitle,
                color = VidooTextTertiary,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = vidooSwitchColors(),
            modifier = Modifier.testTag(testTag)
        )
    }
}

@Composable
private fun SettingsActionRow(
    title: String,
    subtitle: String,
    actionText: String,
    actionIcon: ImageVector,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp)
        ) {
            Text(
                text = title,
                color = VidooTextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = subtitle,
                color = VidooTextTertiary,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
        OutlinedButton(
            onClick = onClick,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, VidooOrange.copy(alpha = 0.7f)),
            modifier = Modifier.testTag(testTag)
        ) {
            Icon(
                imageVector = actionIcon,
                contentDescription = null,
                tint = VidooOrange,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = actionText,
                color = VidooOrange,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun OptionPill(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) VidooOrange else VidooSurface,
        border = BorderStroke(1.dp, if (isSelected) VidooOrange else VidooBorder)
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.Black else VidooTextPrimary,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
        )
    }
}

@Composable
private fun LanguageSelectionButton(
    text: String,
    subText: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) VidooOrange.copy(alpha = 0.15f) else VidooSurface,
        border = BorderStroke(
            1.dp,
            if (isSelected) VidooOrange else VidooBorder
        ),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = text,
                    color = if (isSelected) VidooOrange else VidooTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subText,
                    color = VidooTextTertiary,
                    fontSize = 11.sp
                )
            }
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(VidooOrange),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun vidooSwitchColors() = SwitchDefaults.colors(
    checkedThumbColor = Color.Black,
    checkedTrackColor = VidooOrange,
    uncheckedThumbColor = VidooTextSecondary,
    uncheckedTrackColor = VidooSurface
)

private data class DeviceStorageStats(
    val totalBytes: Long,
    val usedBytes: Long,
    val availableBytes: Long,
    val usedFormatted: String,
    val totalFormatted: String,
    val availableFormatted: String,
    val usedRatio: Float
)

private fun getStorageStats(): DeviceStorageStats {
    return try {
        val path = Environment.getDataDirectory()
        val stat = StatFs(path.path)
        val blockSize = stat.blockSizeLong
        val totalBlocks = stat.blockCountLong
        val availableBlocks = stat.availableBlocksLong
        val totalBytes = (totalBlocks * blockSize).coerceAtLeast(1L)
        val availableBytes = availableBlocks * blockSize
        val usedBytes = (totalBytes - availableBytes).coerceAtLeast(0L)
        val ratio = (usedBytes.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f)
        DeviceStorageStats(
            totalBytes = totalBytes,
            usedBytes = usedBytes,
            availableBytes = availableBytes,
            usedFormatted = formatByteSize(usedBytes),
            totalFormatted = formatByteSize(totalBytes),
            availableFormatted = formatByteSize(availableBytes),
            usedRatio = ratio
        )
    } catch (e: Exception) {
        DeviceStorageStats(
            totalBytes = 0L,
            usedBytes = 0L,
            availableBytes = 0L,
            usedFormatted = "0 GB",
            totalFormatted = "0 GB",
            availableFormatted = "0 GB",
            usedRatio = 0f
        )
    }
}

private fun formatByteSize(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0
    val tb = gb / 1024.0
    return when {
        tb >= 1.0 -> String.format(Locale.US, "%.1f TB", tb)
        gb >= 1.0 -> String.format(Locale.US, "%.1f GB", gb)
        mb >= 1.0 -> String.format(Locale.US, "%.1f MB", mb)
        else -> String.format(Locale.US, "%.0f KB", kb)
    }
}

private fun applyAppLanguage(context: Context, langCode: String, viewModel: VideoPlayerViewModel) {
    viewModel.setAppLanguage(langCode)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val localeManager = context.getSystemService(LocaleManager::class.java)
        localeManager?.applicationLocales = LocaleList.forLanguageTags(langCode)
    } else {
        val locale = Locale(langCode)
        Locale.setDefault(locale)
        val config = context.resources.configuration
        config.setLocale(locale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }
    val msg = if (langCode == "ar") "تم تفعيل اللغة العربية" else "Language set to English"
    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
}
