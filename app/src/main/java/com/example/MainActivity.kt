package com.example

import android.Manifest
import android.animation.ObjectAnimator
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.data.model.VideoItem
import com.example.ui.components.PermissionRequestCard
import com.example.ui.screens.FoldersScreen
import com.example.ui.screens.PlayerScreen
import com.example.ui.screens.PlaylistsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.VideosScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.VidooBlack
import com.example.ui.theme.VidooBorder
import com.example.ui.theme.VidooDarkCharcoal
import com.example.ui.theme.VidooOrange
import com.example.ui.theme.VidooOrangeGlow
import com.example.ui.theme.VidooSurface
import com.example.ui.theme.VidooTextPrimary
import com.example.ui.theme.VidooTextSecondary
import com.example.ui.theme.VidooTextTertiary
import com.example.ui.viewmodel.VideoPlayerViewModel
import com.example.util.AppStrings
import com.example.util.LocalAppStrings

enum class NavigationTab(val icon: ImageVector) {
    VIDEOS(Icons.Default.VideoLibrary),
    FOLDERS(Icons.Default.Folder),
    PLAYLISTS(Icons.Default.PlaylistPlay),
    SETTINGS(Icons.Default.Settings)
}

class MainActivity : ComponentActivity() {

    private val viewModel: VideoPlayerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Smooth transition animation when moving from splash -> main content
        splashScreen.setOnExitAnimationListener { splashScreenViewProvider ->
            val fadeOut = ObjectAnimator.ofFloat(
                splashScreenViewProvider.view,
                View.ALPHA,
                1f,
                0f
            ).apply {
                interpolator = AccelerateDecelerateInterpolator()
                duration = 350L
                doOnEnd { splashScreenViewProvider.remove() }
            }
            fadeOut.start()
        }

        // Synchronously verify permission before UI composition to eliminate any screen flash
        val hasPermission = VideoPlayerViewModel.hasStoragePermission(this)
        viewModel.updatePermissionState(hasPermission)

        setContent {
            val settings by viewModel.settings.collectAsState()
            val isArabic = settings.appLanguage == "ar"
            val appStrings = if (isArabic) AppStrings.Arabic else AppStrings.English
            val layoutDirection = if (isArabic) LayoutDirection.Rtl else LayoutDirection.Ltr

            androidx.compose.runtime.CompositionLocalProvider(
                LocalLayoutDirection provides layoutDirection,
                LocalAppStrings provides appStrings
            ) {
                MyApplicationTheme {
                    VidooApp(viewModel = viewModel)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Re-check if permission was changed in system settings
        val hasPermission = VideoPlayerViewModel.hasStoragePermission(this)
        if (hasPermission != viewModel.uiState.value.permissionGranted) {
            viewModel.updatePermissionState(hasPermission)
        }
    }
}

@Composable
fun FloatingCapsuleBottomBar(
    tabs: List<NavigationTab>,
    activeTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .shadow(
                    elevation = 20.dp,
                    shape = CircleShape,
                    spotColor = Color.Black.copy(alpha = 0.7f),
                    ambientColor = Color.Black.copy(alpha = 0.5f)
                ),
            shape = CircleShape,
            color = Color(0xF21C1C24),
            border = BorderStroke(1.dp, Color(0xFF323242))
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabs.forEachIndexed { index, tab ->
                    val isSelected = activeTab == index
                    val localizedTitle = when (tab) {
                        NavigationTab.VIDEOS -> strings.navVideos
                        NavigationTab.FOLDERS -> strings.navFolders
                        NavigationTab.PLAYLISTS -> strings.navPlaylists
                        NavigationTab.SETTINGS -> strings.navSettings
                    }

                    val scale by animateFloatAsState(
                        targetValue = if (isSelected) 1.05f else 1.0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "tab_scale"
                    )

                    val bgColor by animateColorAsState(
                        targetValue = if (isSelected) VidooOrange.copy(alpha = 0.22f) else Color.Transparent,
                        animationSpec = tween(220),
                        label = "tab_bg"
                    )
                    val tintColor by animateColorAsState(
                        targetValue = if (isSelected) VidooOrange else VidooTextTertiary,
                        animationSpec = tween(220),
                        label = "tab_tint"
                    )

                    Box(
                        modifier = Modifier
                            .scale(scale)
                            .clip(CircleShape)
                            .background(bgColor)
                            .clickable { onTabSelected(index) }
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                            .testTag("nav_tab_${tab.name.lowercase()}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = localizedTitle,
                                tint = tintColor,
                                modifier = Modifier.size(22.dp)
                            )
                            AnimatedVisibility(
                                visible = isSelected,
                                enter = expandHorizontally(
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioNoBouncy,
                                        stiffness = Spring.StiffnessMediumLow
                                    )
                                ) + fadeIn(animationSpec = tween(180)),
                                exit = shrinkHorizontally(
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioNoBouncy,
                                        stiffness = Spring.StiffnessMediumLow
                                    )
                                ) + fadeOut(animationSpec = tween(150))
                            ) {
                                Row {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = localizedTitle,
                                        color = tintColor,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VidooApp(viewModel: VideoPlayerViewModel) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsState()
    val strings = LocalAppStrings.current

    var activeTab by remember { mutableIntStateOf(0) }
    var playingVideo by remember { mutableStateOf<VideoItem?>(null) }

    // Hoist scroll states so list and grid positions are preserved across playback and tab switching
    val videosListState = rememberLazyListState()
    val videosGridState = rememberLazyGridState()

    // Back handler: if user is on secondary tab and not playing video, return to Videos tab
    BackHandler(enabled = playingVideo == null && activeTab != 0) {
        activeTab = 0
    }

    // Required permission based on Android SDK level
    val storagePermission = remember { VideoPlayerViewModel.getRequiredPermission() }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.updatePermissionState(isGranted)
    }

    // Auto-request permission on launch if not already granted
    androidx.compose.runtime.LaunchedEffect(Unit) {
        if (!uiState.permissionGranted) {
            permissionLauncher.launch(storagePermission)
        }
    }

    // Re-check permission when returning to the app from background or system settings
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val isGranted = VideoPlayerViewModel.hasStoragePermission(context)
                if (isGranted != uiState.permissionGranted) {
                    viewModel.updatePermissionState(isGranted)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Use a Box container so the library Scaffold remains composed beneath the player.
    // This guarantees that the scroll position and list layout are never lost when opening/closing videos.
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_vidoo_symbol),
                                contentDescription = "Vidoo Logo",
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = strings.appName,
                                color = VidooTextPrimary,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    },
                    actions = {
                        if (uiState.permissionGranted && activeTab == 0) {
                            IconButton(
                                onClick = { viewModel.refreshVideos() },
                                modifier = Modifier.testTag("refresh_videos_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = strings.refreshVideos,
                                    tint = VidooOrange
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = VidooBlack,
                        titleContentColor = VidooTextPrimary
                    ),
                    modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
                )
            },
            bottomBar = {
                FloatingCapsuleBottomBar(
                    tabs = NavigationTab.values().toList(),
                    activeTab = activeTab,
                    onTabSelected = { activeTab = it },
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                )
            },
            containerColor = VidooBlack,
            modifier = Modifier.fillMaxSize()
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (!uiState.isPermissionChecked) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = VidooOrange,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                } else if (!uiState.permissionGranted && (activeTab == 0 || activeTab == 1)) {
                    PermissionRequestCard(
                        onRequestPermission = { permissionLauncher.launch(storagePermission) }
                    )
                } else {
                    AnimatedContent(
                        targetState = activeTab,
                        transitionSpec = {
                            if (targetState > initialState) {
                                (slideInHorizontally(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) { it / 3 } + fadeIn(animationSpec = tween(220)))
                                    .togetherWith(slideOutHorizontally(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) { -it / 3 } + fadeOut(animationSpec = tween(180)))
                            } else {
                                (slideInHorizontally(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) { -it / 3 } + fadeIn(animationSpec = tween(220)))
                                    .togetherWith(slideOutHorizontally(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) { it / 3 } + fadeOut(animationSpec = tween(180)))
                            }.using(SizeTransform(clip = false))
                        },
                        label = "tab_content_transition"
                    ) { tabIndex ->
                        when (tabIndex) {
                            0 -> VideosScreen(
                                viewModel = viewModel,
                                onPlayVideo = { playingVideo = it },
                                listState = videosListState,
                                gridState = videosGridState
                            )
                            1 -> FoldersScreen(
                                viewModel = viewModel,
                                onFolderSelected = {
                                    activeTab = 0 // Switch to Videos tab with this folder filtered
                                }
                            )
                            2 -> PlaylistsScreen(
                                viewModel = viewModel,
                                onPlayVideo = { playingVideo = it }
                            )
                            3 -> SettingsScreen(
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }
        }

        // Fullscreen player layer overlaid directly on top of the library without disposing it
        val playbackQueue by viewModel.playbackQueue.collectAsState()
        val effectiveQueue = remember(playbackQueue, uiState.filteredVideos) {
            if (playbackQueue.isNotEmpty()) playbackQueue else uiState.filteredVideos
        }

        val currentVideoIndex = remember(playingVideo, effectiveQueue) {
            if (playingVideo == null) -1
            else effectiveQueue.indexOfFirst { it.id == playingVideo?.id || it.contentUri == playingVideo?.contentUri }
        }

        // Only genuinely disabled when there are no other videos in the library/queue (size <= 1)
        val hasPrevious = effectiveQueue.size > 1 || currentVideoIndex > 0
        val hasNext = effectiveQueue.size > 1 || (currentVideoIndex in 0 until (effectiveQueue.size - 1))

        AnimatedVisibility(
            visible = playingVideo != null,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ) + fadeIn(animationSpec = tween(200)),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(200)
            ) + fadeOut(animationSpec = tween(150))
        ) {
            playingVideo?.let { video ->
                PlayerScreen(
                    video = video,
                    viewModel = viewModel,
                    hasPrevious = hasPrevious,
                    hasNext = hasNext,
                    onPlayPrevious = {
                        if (effectiveQueue.isNotEmpty()) {
                            val prevIndex = if (currentVideoIndex > 0) currentVideoIndex - 1 else effectiveQueue.size - 1
                            val prevVideo = effectiveQueue[prevIndex]
                            viewModel.recordVideoPlayed(prevVideo.id)
                            playingVideo = prevVideo
                        }
                    },
                    onPlayNext = {
                        if (effectiveQueue.isNotEmpty()) {
                            val nextIndex = if (currentVideoIndex in 0 until (effectiveQueue.size - 1)) currentVideoIndex + 1 else 0
                            val nextVideo = effectiveQueue[nextIndex]
                            viewModel.recordVideoPlayed(nextVideo.id)
                            playingVideo = nextVideo
                        }
                    },
                    onBack = { playingVideo = null }
                )
            }
        }
    }
}

