package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.HttpDataSource
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.data.model.SrtParser
import com.example.data.model.SubtitleItem
import com.example.data.model.VideoItem
import com.example.ui.theme.VidooBorder
import com.example.ui.theme.VidooOrange
import com.example.ui.theme.VidooSurface
import com.example.ui.theme.VidooTextPrimary
import com.example.ui.theme.VidooTextSecondary
import com.example.ui.theme.VidooTextTertiary
import com.example.ui.viewmodel.VideoPlayerViewModel
import com.example.util.LocalAppStrings
import com.example.util.VideoThumbnailHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.roundToInt

enum class DragGestureType {
    NONE,
    HORIZONTAL_SEEK,
    VERTICAL_VOLUME,
    VERTICAL_BRIGHTNESS
}

enum class SpeedGestureMode {
    FORWARD_2X,
    REWIND_2X
}

@OptIn(UnstableApi::class)
enum class AspectRatioMode(val displayName: String, val mode: Int) {
    FIT("Fit", AspectRatioFrameLayout.RESIZE_MODE_FIT),
    FILL("Crop / Fill", AspectRatioFrameLayout.RESIZE_MODE_ZOOM),
    STRETCH("Stretch", AspectRatioFrameLayout.RESIZE_MODE_FILL)
}

private fun isRunningOnEmulator(): Boolean {
    val fingerprint = Build.FINGERPRINT ?: ""
    val model = Build.MODEL ?: ""
    val manufacturer = Build.MANUFACTURER ?: ""
    val hardware = Build.HARDWARE ?: ""
    val product = Build.PRODUCT ?: ""
    val brand = Build.BRAND ?: ""
    val device = Build.DEVICE ?: ""
    return fingerprint.startsWith("generic")
            || fingerprint.startsWith("unknown")
            || model.contains("google_sdk", ignoreCase = true)
            || model.contains("Emulator", ignoreCase = true)
            || model.contains("Android SDK built for", ignoreCase = true)
            || manufacturer.contains("Genymotion", ignoreCase = true)
            || (brand.startsWith("generic") && device.startsWith("generic"))
            || "google_sdk" == product
            || hardware.contains("goldfish", ignoreCase = true)
            || hardware.contains("ranchu", ignoreCase = true)
            || product.contains("sdk_gphone", ignoreCase = true)
            || product.contains("vbox86p", ignoreCase = true)
            || product.contains("emulator", ignoreCase = true)
            || product.contains("simulator", ignoreCase = true)
}

@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(
    video: VideoItem,
    viewModel: VideoPlayerViewModel,
    hasPrevious: Boolean = false,
    hasNext: Boolean = false,
    onPlayPrevious: () -> Unit = {},
    onPlayNext: () -> Unit = {},
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val strings = LocalAppStrings.current
    val activity = context as? Activity
    val lifecycleOwner = LocalLifecycleOwner.current
    val settings by viewModel.settings.collectAsState()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // ExoPlayer instance configured with software decoder fallback and proper audio attributes
    val exoPlayer = remember {
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Mobile Safari/537.36")
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(15000)
            .setReadTimeoutMs(20000)

        val dataSourceFactory = DefaultDataSource.Factory(context, httpDataSourceFactory)
        val mediaSourceFactory = DefaultMediaSourceFactory(dataSourceFactory)

        // Use DefaultRenderersFactory with decoder fallback enabled so ExoPlayer queries
        // system codecs using standard platform mechanisms and falls back gracefully if hardware
        // resource interfaces are unavailable.
        val renderersFactory = DefaultRenderersFactory(context).apply {
            setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF)
            setEnableDecoderFallback(true)
            setAllowedVideoJoiningTimeMs(5000)
        }

        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
            .setUsage(C.USAGE_MEDIA)
            .build()

        ExoPlayer.Builder(context, renderersFactory)
            .setMediaSourceFactory(mediaSourceFactory)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .build().apply {
                playWhenReady = true
            }
    }

    val coroutineScope = rememberCoroutineScope()
    var autoRetryCount by remember { mutableIntStateOf(0) }
    var isPlaying by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(video.durationMs) }
    var isSeeking by remember { mutableStateOf(false) }
    var seekPosition by remember { mutableFloatStateOf(0f) }
    var playerErrorMessage by remember { mutableStateOf<String?>(null) }

    // Controls visibility & auto-hide
    var showControls by remember { mutableStateOf(true) }
    var isLocked by remember { mutableStateOf(false) }
    var lastInteractionTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    // HUD state for gesture controls
    var showHud by remember { mutableStateOf(false) }
    var hudIcon by remember { mutableStateOf(Icons.Default.VolumeUp) }
    var hudText by remember { mutableStateOf("") }
    var hudPercentage by remember { mutableFloatStateOf(0f) }

    // Double-tap ripple indication
    var doubleTapRippleText by remember { mutableStateOf<String?>(null) }

    // Aspect ratio & Playback speed
    var currentAspectRatio by remember { mutableStateOf(AspectRatioMode.FIT) }
    var showSpeedDialog by remember { mutableStateOf(false) }
    var playbackSpeed by remember { mutableFloatStateOf(settings.defaultPlaybackSpeed) }

    // Video display frame and overlay control dimensions for precise vertical centering
    val density = LocalDensity.current
    var videoFrameWidth by remember { mutableIntStateOf(if (video.width > 0) video.width else 16) }
    var videoFrameHeight by remember { mutableIntStateOf(if (video.height > 0) video.height else 9) }
    var topBarHeightDp by remember { mutableStateOf(56.dp) }
    var bottomControlsHeightDp by remember { mutableStateOf(100.dp) }

    // Gesture-based Speed & Rewind Hold state (Long-press left/right)
    var speedGestureActive by remember { mutableStateOf<SpeedGestureMode?>(null) }
    var wasPlayingBeforeSpeedGesture by remember { mutableStateOf(false) }
    val hapticFeedback = LocalHapticFeedback.current

    // Subtitles state
    var subtitles by remember { mutableStateOf<List<SubtitleItem>>(emptyList()) }
    var activeSubtitleText by remember { mutableStateOf("") }
    var subtitleFileName by remember { mutableStateOf<String?>(null) }
    var showSubtitleInfo by remember { mutableStateOf(false) }

    // Gesture Help Guide Overlay state
    var showGestureGuide by remember { mutableStateOf(!settings.hasSeenGestureGuide) }

    // System Audio Manager safely retrieved
    val audioManager = remember {
        try {
            context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        } catch (_: Exception) {
            null
        }
    }
    val maxVolume = remember(audioManager) {
        try {
            audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC)?.coerceAtLeast(1) ?: 15
        } catch (_: Exception) {
            15
        }
    }

    // Vertical swipe for Volume overlay state (TikTok/Instagram style)
    var showVolumeOverlay by remember { mutableStateOf(false) }
    var volumePercentage by remember {
        mutableFloatStateOf(
            try {
                ((audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC)?.toFloat() ?: 7f) / maxVolume).coerceIn(0f, 1f)
            } catch (_: Exception) {
                0.5f
            }
        )
    }
    var volumeFractionAccumulator by remember { mutableFloatStateOf(volumePercentage) }
    var volumeOverlayDismissJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }

    // Vertical swipe for Brightness overlay state (matching Volume overlay exactly)
    var showBrightnessOverlay by remember { mutableStateOf(false) }
    var brightnessPercentage by remember {
        mutableFloatStateOf(
            activity?.window?.attributes?.screenBrightness.let { b ->
                if (b == null || b < 0f) 0.5f else b.coerceIn(0.01f, 1.0f)
            }
        )
    }
    var brightnessFractionAccumulator by remember { mutableFloatStateOf(brightnessPercentage) }
    var brightnessOverlayDismissJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }

    // Horizontal swipe for Seek overlay state
    var isHorizontalSeekGesture by remember { mutableStateOf(false) }
    var horizontalSeekDeltaMs by remember { mutableLongStateOf(0L) }
    var horizontalSeekTargetPos by remember { mutableLongStateOf(0L) }
    var horizontalSeekInitialPos by remember { mutableLongStateOf(0L) }
    var seekOverlayDismissJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }

    // Gesture dominant-axis tracking state
    var activeGestureType by remember { mutableStateOf(DragGestureType.NONE) }
    var accumulatedDragX by remember { mutableFloatStateOf(0f) }
    var accumulatedDragY by remember { mutableFloatStateOf(0f) }
    var dragStartOffset by remember { mutableStateOf(Offset.Zero) }

    // Pause playback when app goes to background / lifecycle pause
    DisposableEffect(lifecycleOwner, exoPlayer) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP -> {
                    exoPlayer.pause()
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Keep screen on while playing and properly release player resources
    DisposableEffect(Unit) {
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        VideoThumbnailHelper.isPlaybackActive = true
        onDispose {
            VideoThumbnailHelper.isPlaybackActive = false
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            // Restore portrait orientation upon exit
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            exoPlayer.stop()
            exoPlayer.clearMediaItems()
            exoPlayer.release()
        }
    }

    // Back handler: dismiss gesture guide first, unlock if screen is locked, otherwise save and exit
    BackHandler {
        if (showGestureGuide) {
            showGestureGuide = false
            viewModel.setHasSeenGestureGuide(true)
        } else if (isLocked) {
            isLocked = false
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            showControls = true
        } else {
            viewModel.saveProgress(video.contentUri, exoPlayer.currentPosition, exoPlayer.duration.coerceAtLeast(0L))
            onBack()
        }
    }

    // Initialize Video & Resume position
    LaunchedEffect(video.contentUri) {
        currentPosition = 0L
        duration = video.durationMs
        videoFrameWidth = if (video.width > 0) video.width else 16
        videoFrameHeight = if (video.height > 0) video.height else 9
        playerErrorMessage = null
        autoRetryCount = 0
        subtitles = emptyList()
        activeSubtitleText = ""
        subtitleFileName = null
        showSubtitleInfo = false

        // Stop and clear previous pipeline to ensure codecs are cleanly released before loading next video
        exoPlayer.stop()
        exoPlayer.clearMediaItems()

        val mediaItem = MediaItem.fromUri(Uri.parse(video.contentUri))
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.playbackParameters = PlaybackParameters(playbackSpeed)

        // Check resume position
        var resumed = false
        if (settings.resumePlayback) {
            val savedPos = viewModel.getSavedPosition(video.contentUri)
            // If saved pos is not at the end of the video
            if (savedPos > 5000L && (video.durationMs <= 0L || savedPos < video.durationMs - 2000L)) {
                exoPlayer.seekTo(savedPos)
                currentPosition = savedPos
                resumed = true
            }
        }
        if (!resumed) {
            exoPlayer.seekTo(0L)
            currentPosition = 0L
        }
        exoPlayer.play()
    }

    // Listener for ExoPlayer state changes
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onVideoSizeChanged(videoSize: VideoSize) {
                if (videoSize.width > 0 && videoSize.height > 0) {
                    videoFrameWidth = videoSize.width
                    videoFrameHeight = videoSize.height
                }
            }

            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }

            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY) {
                    duration = exoPlayer.duration.coerceAtLeast(0L)
                    playerErrorMessage = null
                    autoRetryCount = 0
                } else if (state == Player.STATE_ENDED) {
                    isPlaying = false
                    showControls = true
                    viewModel.saveProgress(video.contentUri, 0L, duration)
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                val cause = error.cause
                val errorMsg = error.localizedMessage?.lowercase() ?: ""
                val isDecoderIssue = error.errorCode == PlaybackException.ERROR_CODE_DECODING_RESOURCES_RECLAIMED ||
                        error.errorCode == PlaybackException.ERROR_CODE_DECODER_INIT_FAILED ||
                        error.errorCode == PlaybackException.ERROR_CODE_DECODER_QUERY_FAILED ||
                        error.errorCode == PlaybackException.ERROR_CODE_DECODING_FAILED ||
                        errorMsg.contains("decoder") ||
                        errorMsg.contains("codec") ||
                        errorMsg.contains("resource")

                if (isDecoderIssue && autoRetryCount < 3) {
                    autoRetryCount++
                    val savedPos = exoPlayer.currentPosition
                    coroutineScope.launch {
                        delay(350L)
                        try {
                            exoPlayer.stop()
                            exoPlayer.prepare()
                            if (savedPos > 0L) {
                                exoPlayer.seekTo(savedPos)
                            }
                            exoPlayer.play()
                            playerErrorMessage = null
                        } catch (_: Exception) {
                            playerErrorMessage = strings.playerErrorDecoderReclaimed
                        }
                    }
                    return
                }

                playerErrorMessage = when {
                    isDecoderIssue ->
                        strings.playerErrorDecoderReclaimed
                    cause is HttpDataSource.InvalidResponseCodeException ->
                        "Server responded with error (HTTP ${cause.responseCode})"
                    cause is java.net.UnknownHostException ->
                        "Network unavailable. Please check your internet connection."
                    cause is java.lang.SecurityException ->
                        "Access error: ${cause.message}"
                    error.errorCode == PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED ->
                        "Network connection failed"
                    else -> error.localizedMessage ?: strings.playerErrorGeneral
                }
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
        }
    }

    // Periodic position updater and subtitle sync
    LaunchedEffect(isPlaying) {
        while (true) {
            if (exoPlayer.playbackState == Player.STATE_READY && !isSeeking) {
                currentPosition = exoPlayer.currentPosition
                if (exoPlayer.duration > 0) {
                    duration = exoPlayer.duration
                }

                // Subtitle sync
                if (subtitles.isNotEmpty()) {
                    val matching = subtitles.find {
                        currentPosition in it.startTimeMs..it.endTimeMs
                    }
                    activeSubtitleText = matching?.text ?: ""
                } else {
                    activeSubtitleText = ""
                }

                // Periodic progress save every 3s
                if (currentPosition > 2000L && duration > 0L) {
                    viewModel.saveProgress(video.contentUri, currentPosition, duration)
                }
            }
            delay(250)
        }
    }

    // Continuous rapid rewind seeking when long-pressing left side
    LaunchedEffect(speedGestureActive) {
        if (speedGestureActive == SpeedGestureMode.REWIND_2X) {
            while (isActive && speedGestureActive == SpeedGestureMode.REWIND_2X) {
                val stepMs = 300L
                val target = (exoPlayer.currentPosition - stepMs).coerceAtLeast(0L)
                exoPlayer.seekTo(target)
                currentPosition = target
                if (target <= 0L) {
                    break
                }
                delay(150L)
            }
        }
    }

    // Auto-hide controls timer (3.5 seconds)
    LaunchedEffect(showControls, lastInteractionTime) {
        if (showControls && isPlaying && !isLocked) {
            delay(3500)
            showControls = false
        }
    }

    // Auto-lock screen during playback timer
    LaunchedEffect(isPlaying, isLocked, lastInteractionTime, settings.autoLock, settings.autoLockTimeoutSec) {
        if (settings.autoLock && isPlaying && !isLocked) {
            delay(settings.autoLockTimeoutSec * 1000L)
            isLocked = true
            showControls = false
        }
    }

    // Auto-dismiss HUD
    LaunchedEffect(showHud, hudText) {
        if (showHud) {
            delay(1200)
            showHud = false
        }
    }

    // Auto-dismiss double tap ripple
    LaunchedEffect(doubleTapRippleText) {
        if (doubleTapRippleText != null) {
            delay(600)
            doubleTapRippleText = null
        }
    }

    // Subtitle File Picker
    val srtPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val content = stream.bufferedReader().use { it.readText() }
                    val parsed = SrtParser.parse(content)
                    subtitles = parsed
                    subtitleFileName = uri.lastPathSegment ?: "External Subtitle"
                    showSubtitleInfo = true
                }
            } catch (_: Exception) {
                // Ignore parsing errors
            }
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Player Surface
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false
                    resizeMode = currentAspectRatio.mode
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            update = { playerView ->
                if (playerView.player != exoPlayer) {
                    playerView.player = exoPlayer
                }
                playerView.resizeMode = currentAspectRatio.mode
            },
            onRelease = { playerView ->
                playerView.player = null
            },
            modifier = Modifier.fillMaxSize()
        )

        // Gesture Detection Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(isLocked) {
                    if (isLocked) {
                        detectTapGestures(onTap = { showControls = true })
                    } else {
                        detectTapGestures(
                            onPress = { offset ->
                                try {
                                    tryAwaitRelease()
                                } finally {
                                    if (speedGestureActive != null) {
                                        val wasActive = speedGestureActive
                                        val wasPlaying = wasPlayingBeforeSpeedGesture
                                        speedGestureActive = null
                                        exoPlayer.playbackParameters = PlaybackParameters(playbackSpeed)
                                        if (wasActive == SpeedGestureMode.REWIND_2X && wasPlaying) {
                                            exoPlayer.play()
                                        } else if (wasActive == SpeedGestureMode.FORWARD_2X && !wasPlaying) {
                                            exoPlayer.pause()
                                        }
                                    }
                                }
                            },
                            onDoubleTap = { offset ->
                                val screenWidth = size.width
                                if (offset.x < screenWidth * 0.4f) {
                                    // Rewind 10s
                                    val newPos = (exoPlayer.currentPosition - 10000L).coerceAtLeast(0L)
                                    exoPlayer.seekTo(newPos)
                                    currentPosition = newPos
                                    doubleTapRippleText = "-10s"
                                } else if (offset.x > screenWidth * 0.6f) {
                                    // Forward 10s
                                    val newPos = (exoPlayer.currentPosition + 10000L).coerceAtMost(duration)
                                    exoPlayer.seekTo(newPos)
                                    currentPosition = newPos
                                    doubleTapRippleText = "+10s"
                                } else {
                                    // Center double tap toggles play/pause or replay if at the end
                                    val isAtEnd = exoPlayer.playbackState == Player.STATE_ENDED ||
                                            (duration > 0 && exoPlayer.currentPosition >= duration - 300L)
                                    if (isAtEnd) {
                                        exoPlayer.seekTo(0L)
                                        currentPosition = 0L
                                        exoPlayer.prepare()
                                        exoPlayer.play()
                                    } else if (exoPlayer.isPlaying) {
                                        exoPlayer.pause()
                                    } else {
                                        exoPlayer.play()
                                    }
                                }
                                lastInteractionTime = System.currentTimeMillis()
                            },
                            onLongPress = { offset ->
                                val screenWidth = size.width
                                val isRightSide = offset.x >= screenWidth / 2f
                                wasPlayingBeforeSpeedGesture = exoPlayer.isPlaying
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                if (isRightSide) {
                                    speedGestureActive = SpeedGestureMode.FORWARD_2X
                                    exoPlayer.playbackParameters = PlaybackParameters(2.0f)
                                    if (!exoPlayer.isPlaying) {
                                        exoPlayer.play()
                                    }
                                } else {
                                    speedGestureActive = SpeedGestureMode.REWIND_2X
                                    if (exoPlayer.isPlaying) {
                                        exoPlayer.pause()
                                    }
                                }
                                showControls = false
                                lastInteractionTime = System.currentTimeMillis()
                            },
                            onTap = {
                                showControls = !showControls
                                lastInteractionTime = System.currentTimeMillis()
                            }
                        )
                    }
                }
                .pointerInput(isLocked, speedGestureActive != null) {
                    if (!isLocked && speedGestureActive == null) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                dragStartOffset = offset
                                accumulatedDragX = 0f
                                accumulatedDragY = 0f
                                activeGestureType = DragGestureType.NONE
                                horizontalSeekInitialPos = exoPlayer.currentPosition
                                volumeOverlayDismissJob?.cancel()
                                seekOverlayDismissJob?.cancel()
                                brightnessOverlayDismissJob?.cancel()
                                val currentVol = try {
                                    audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: 0
                                } catch (_: Exception) { 0 }
                                volumePercentage = (currentVol.toFloat() / maxVolume).coerceIn(0f, 1f)
                                volumeFractionAccumulator = volumePercentage
                                val curBrightness = activity?.window?.attributes?.screenBrightness ?: 0.5f
                                brightnessPercentage = if (curBrightness < 0f) 0.5f else curBrightness.coerceIn(0.01f, 1.0f)
                                brightnessFractionAccumulator = brightnessPercentage
                            },
                            onDragEnd = {
                                if (activeGestureType == DragGestureType.HORIZONTAL_SEEK) {
                                    exoPlayer.seekTo(horizontalSeekTargetPos)
                                    currentPosition = horizontalSeekTargetPos
                                    seekOverlayDismissJob?.cancel()
                                    seekOverlayDismissJob = coroutineScope.launch {
                                        delay(800L)
                                        isHorizontalSeekGesture = false
                                    }
                                } else if (activeGestureType == DragGestureType.VERTICAL_VOLUME) {
                                    volumeOverlayDismissJob?.cancel()
                                    volumeOverlayDismissJob = coroutineScope.launch {
                                        delay(1000L)
                                        showVolumeOverlay = false
                                    }
                                } else if (activeGestureType == DragGestureType.VERTICAL_BRIGHTNESS) {
                                    brightnessOverlayDismissJob?.cancel()
                                    brightnessOverlayDismissJob = coroutineScope.launch {
                                        delay(1000L)
                                        showBrightnessOverlay = false
                                    }
                                }
                                activeGestureType = DragGestureType.NONE
                                lastInteractionTime = System.currentTimeMillis()
                            },
                            onDragCancel = {
                                if (activeGestureType == DragGestureType.VERTICAL_VOLUME) {
                                    volumeOverlayDismissJob?.cancel()
                                    volumeOverlayDismissJob = coroutineScope.launch {
                                        delay(1000L)
                                        showVolumeOverlay = false
                                    }
                                } else if (activeGestureType == DragGestureType.VERTICAL_BRIGHTNESS) {
                                    brightnessOverlayDismissJob?.cancel()
                                    brightnessOverlayDismissJob = coroutineScope.launch {
                                        delay(1000L)
                                        showBrightnessOverlay = false
                                    }
                                }
                                isHorizontalSeekGesture = false
                                activeGestureType = DragGestureType.NONE
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                accumulatedDragX += dragAmount.x
                                accumulatedDragY += dragAmount.y

                                val movementSlop = with(density) { 16.dp.toPx() }

                                if (activeGestureType == DragGestureType.NONE) {
                                    val dist = hypot(accumulatedDragX, accumulatedDragY)
                                    if (dist >= movementSlop) {
                                        // Dominant axis detection: if horizontal > vertical -> seek; if vertical > horizontal -> volume
                                        if (abs(accumulatedDragX) > abs(accumulatedDragY)) {
                                            activeGestureType = DragGestureType.HORIZONTAL_SEEK
                                            isHorizontalSeekGesture = true
                                        } else {
                                            // Vertical gesture: right side (or anywhere convention) -> Volume, far left -> Brightness
                                            val isRightSide = dragStartOffset.x >= size.width * 0.40f
                                            if (isRightSide) {
                                                activeGestureType = DragGestureType.VERTICAL_VOLUME
                                                showVolumeOverlay = true
                                            } else {
                                                activeGestureType = DragGestureType.VERTICAL_BRIGHTNESS
                                                showBrightnessOverlay = true
                                            }
                                        }
                                    }
                                }

                                when (activeGestureType) {
                                    DragGestureType.VERTICAL_VOLUME -> {
                                        val deltaY = -dragAmount.y // upward swipe is positive
                                        val volumeDelta = (deltaY / (size.height * 0.70f))
                                        volumeFractionAccumulator = (volumeFractionAccumulator + volumeDelta).coerceIn(0f, 1f)
                                        val newVol = (volumeFractionAccumulator * maxVolume).roundToInt().coerceIn(0, maxVolume)
                                        try {
                                            audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, newVol, 0)
                                        } catch (_: Exception) {}
                                        volumePercentage = (newVol.toFloat() / maxVolume).coerceIn(0f, 1f)
                                        showVolumeOverlay = true
                                        lastInteractionTime = System.currentTimeMillis()
                                    }
                                    DragGestureType.HORIZONTAL_SEEK -> {
                                        val scrubWindowMs = duration.coerceAtLeast(60000L).coerceAtMost(300000L)
                                        val deltaMs = (accumulatedDragX / size.width * scrubWindowMs).toLong()
                                        horizontalSeekDeltaMs = deltaMs
                                        horizontalSeekTargetPos = (horizontalSeekInitialPos + deltaMs).coerceIn(0L, duration)
                                        isHorizontalSeekGesture = true
                                        lastInteractionTime = System.currentTimeMillis()
                                    }
                                    DragGestureType.VERTICAL_BRIGHTNESS -> {
                                        val deltaY = -dragAmount.y
                                        val deltaBrightness = deltaY / (size.height * 0.70f)
                                        brightnessFractionAccumulator = (brightnessFractionAccumulator + deltaBrightness).coerceIn(0.01f, 1.0f)
                                        activity?.let { act ->
                                            val lp = act.window.attributes
                                            lp.screenBrightness = brightnessFractionAccumulator
                                            act.window.attributes = lp
                                        }
                                        brightnessPercentage = brightnessFractionAccumulator
                                        showBrightnessOverlay = true
                                        lastInteractionTime = System.currentTimeMillis()
                                    }
                                    DragGestureType.NONE -> {}
                                }
                            }
                        )
                    }
                }
        )

        // Subtitle Render Box
        if (activeSubtitleText.isNotEmpty()) {
            val subColor = when (settings.subtitleColor) {
                "Yellow" -> Color(0xFFFFEB3B)
                "Cyan" -> Color(0xFF00E5FF)
                "Green" -> Color(0xFF69F0AE)
                "Orange" -> VidooOrange
                else -> Color.White
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = if (showControls) 100.dp else 40.dp)
                    .padding(horizontal = 24.dp)
            ) {
                Surface(
                    color = if (settings.subtitleBackground) Color.Black.copy(alpha = 0.75f) else Color.Transparent,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = activeSubtitleText,
                        color = subColor,
                        fontSize = settings.subtitleFontSizeSp.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Gesture HUD (Volume & Brightness feedback)
        AnimatedVisibility(
            visible = showHud,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Surface(
                color = Color.Black.copy(alpha = 0.85f),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, VidooBorder),
                modifier = Modifier.size(130.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = hudIcon,
                        contentDescription = null,
                        tint = VidooOrange,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = hudText,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color(0xFF333333))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(hudPercentage)
                                .height(4.dp)
                                .background(VidooOrange)
                        )
                    }
                }
            }
        }

        // Vertical Volume Control Overlay (Floating clean slider directly over video, no capsule)
        AnimatedVisibility(
            visible = showVolumeOverlay,
            enter = fadeIn() + scaleIn(initialScale = 0.90f),
            exit = fadeOut() + scaleOut(targetScale = 0.90f),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 24.dp)
                .testTag("player_vertical_volume_overlay")
        ) {
            Column(
                modifier = Modifier
                    .width(36.dp)
                    .height(150.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                val speakerIcon = when {
                    volumePercentage <= 0.01f -> Icons.Default.VolumeMute
                    volumePercentage < 0.5f -> Icons.Default.VolumeDown
                    else -> Icons.Default.VolumeUp
                }
                Icon(
                    imageVector = speakerIcon,
                    contentDescription = strings.volumeLabel,
                    tint = if (volumePercentage > 0.01f) VidooOrange else Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(24.dp)
                )

                // Vertical volume bar track with orange fill
                Box(
                    modifier = Modifier
                        .width(6.dp)
                        .height(86.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color.Black.copy(alpha = 0.45f)),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(volumePercentage.coerceIn(0f, 1f))
                            .clip(RoundedCornerShape(3.dp))
                            .background(VidooOrange)
                    )
                }

                Text(
                    text = "${(volumePercentage * 100).roundToInt()}%",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Vertical Brightness Control Overlay (Floating clean slider directly over video, no capsule)
        AnimatedVisibility(
            visible = showBrightnessOverlay,
            enter = fadeIn() + scaleIn(initialScale = 0.90f),
            exit = fadeOut() + scaleOut(targetScale = 0.90f),
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 24.dp)
                .testTag("player_vertical_brightness_overlay")
        ) {
            Column(
                modifier = Modifier
                    .width(36.dp)
                    .height(150.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    imageVector = Icons.Default.BrightnessMedium,
                    contentDescription = strings.brightnessLabel,
                    tint = VidooOrange,
                    modifier = Modifier.size(24.dp)
                )

                // Vertical brightness bar track with orange fill
                Box(
                    modifier = Modifier
                        .width(6.dp)
                        .height(86.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color.Black.copy(alpha = 0.45f)),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(brightnessPercentage.coerceIn(0f, 1f))
                            .clip(RoundedCornerShape(3.dp))
                            .background(VidooOrange)
                    )
                }

                Text(
                    text = "${(brightnessPercentage * 100).roundToInt()}%",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Horizontal Seek Gesture Overlay
        AnimatedVisibility(
            visible = isHorizontalSeekGesture,
            enter = fadeIn() + scaleIn(initialScale = 0.92f),
            exit = fadeOut() + scaleOut(targetScale = 0.92f),
            modifier = Modifier
                .align(Alignment.Center)
                .testTag("player_horizontal_seek_overlay")
        ) {
            Surface(
                color = Color.Black.copy(alpha = 0.88f),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, VidooBorder),
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    val deltaSeconds = horizontalSeekDeltaMs / 1000
                    val deltaText = if (deltaSeconds >= 0) "+${deltaSeconds}s" else "${deltaSeconds}s"

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (deltaSeconds >= 0) Icons.Default.FastForward else Icons.Default.FastRewind,
                            contentDescription = null,
                            tint = VidooOrange,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = "${formatTime(horizontalSeekTargetPos)} / ${formatTime(duration)}",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "($deltaText)",
                        color = VidooOrange,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .width(160.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color(0xFF333333))
                    ) {
                        val seekPct = if (duration > 0) (horizontalSeekTargetPos.toFloat() / duration).coerceIn(0f, 1f) else 0f
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(seekPct)
                                .height(4.dp)
                                .background(VidooOrange)
                        )
                    }
                }
            }
        }

        // Double-tap indication (Left / Right)
        AnimatedVisibility(
            visible = doubleTapRippleText != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Surface(
                color = Color.Black.copy(alpha = 0.8f),
                shape = CircleShape,
                modifier = Modifier.size(76.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = doubleTapRippleText ?: "",
                        color = VidooOrange,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Gesture-based Speed / Rewind indicator overlay (Minimal floating "2x" with vector icon, no background pill)
        AnimatedVisibility(
            visible = speedGestureActive != null,
            enter = fadeIn() + scaleIn(initialScale = 0.85f),
            exit = fadeOut() + scaleOut(targetScale = 0.85f),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 28.dp)
                .testTag("speed_gesture_indicator")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (speedGestureActive == SpeedGestureMode.REWIND_2X) {
                    Icon(
                        imageVector = Icons.Default.FastRewind,
                        contentDescription = "Rewind 2x",
                        tint = VidooOrange,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "2x",
                        color = VidooOrange,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp,
                        fontFamily = com.example.ui.theme.NotoSansArabic,
                        style = androidx.compose.ui.text.TextStyle(
                            shadow = Shadow(
                                color = Color.Black.copy(alpha = 0.75f),
                                offset = Offset(0f, 2f),
                                blurRadius = 6f
                            )
                        ),
                        modifier = Modifier.testTag("speed_gesture_text")
                    )
                } else if (speedGestureActive == SpeedGestureMode.FORWARD_2X) {
                    Text(
                        text = "2x",
                        color = VidooOrange,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp,
                        fontFamily = com.example.ui.theme.NotoSansArabic,
                        style = androidx.compose.ui.text.TextStyle(
                            shadow = Shadow(
                                color = Color.Black.copy(alpha = 0.75f),
                                offset = Offset(0f, 2f),
                                blurRadius = 6f
                            )
                        ),
                        modifier = Modifier.testTag("speed_gesture_text")
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.FastForward,
                        contentDescription = "Fast Forward 2x",
                        tint = VidooOrange,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Playback Error Overlay
        if (playerErrorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.90f))
                    .clickable { /* absorb taps */ },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = strings.playerErrorGeneral,
                        tint = Color(0xFFEF5350),
                        modifier = Modifier.size(52.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = strings.playerErrorGeneral,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = playerErrorMessage ?: strings.playerErrorGeneral,
                        color = VidooTextSecondary,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        TextButton(
                            onClick = { onBack() }
                        ) {
                            Text(strings.goBack, color = Color.White)
                        }
                        TextButton(
                            onClick = {
                                playerErrorMessage = null
                                autoRetryCount = 0
                                val savedPos = exoPlayer.currentPosition
                                exoPlayer.prepare()
                                if (savedPos > 0L) {
                                    exoPlayer.seekTo(savedPos)
                                }
                                exoPlayer.play()
                            }
                        ) {
                            Text(strings.retry, color = VidooOrange, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Floating Lock Button when locked
        if (isLocked) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(20.dp)
            ) {
                IconButton(
                    onClick = {
                        isLocked = false
                        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                        showControls = true
                        lastInteractionTime = System.currentTimeMillis()
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.7f))
                        .testTag("player_unlock_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Unlock controls",
                        tint = VidooOrange
                    )
                }
            }
        }

        // Calculate visible video frame boundaries and vertical center between top bar and bottom controls
        val containerW = maxWidth
        val containerH = maxHeight
        val vWidth = if (videoFrameWidth > 0) videoFrameWidth else 16
        val vHeight = if (videoFrameHeight > 0) videoFrameHeight else 9
        val videoAspect = vWidth.toFloat() / vHeight.toFloat()

        val (videoTop, videoBottom) = when (currentAspectRatio) {
            AspectRatioMode.FIT -> {
                val containerAspect = if (containerH.value > 0f) containerW.value / containerH.value else 1.77f
                if (containerAspect > videoAspect) {
                    // Container is wider than video (pillarbox) -> video touches top and bottom of screen
                    0.dp to containerH
                } else {
                    // Container is taller than video (letterbox) -> video touches left and right, centered vertically
                    val renderedHeight = containerW / videoAspect
                    val insetY = ((containerH - renderedHeight) / 2f).coerceAtLeast(0.dp)
                    insetY to (insetY + renderedHeight)
                }
            }
            AspectRatioMode.FILL, AspectRatioMode.STRETCH -> {
                0.dp to containerH
            }
        }

        // Exact vertical center of the video content area between the top bar and the bottom seek-bar/controls section
        val effectiveTop = maxOf(videoTop, topBarHeightDp)
        val effectiveBottom = minOf(videoBottom, (containerH - bottomControlsHeightDp).coerceAtLeast(effectiveTop))
        val centerY = if (effectiveBottom > effectiveTop) {
            (effectiveTop + effectiveBottom) / 2f
        } else {
            (videoTop + videoBottom) / 2f
        }

        // Controls Overlay (Top bar, Center buttons, Bottom controls)
        AnimatedVisibility(
            visible = showControls && !isLocked,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                // Top Bar with smooth vertical gradient overlay
                Row(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.75f),
                                    Color.Black.copy(alpha = 0.35f),
                                    Color.Transparent
                                )
                            )
                        )
                        .statusBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .onSizeChanged { size ->
                            topBarHeightDp = with(density) { size.height.toDp() }
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            viewModel.saveProgress(video.contentUri, exoPlayer.currentPosition, exoPlayer.duration.coerceAtLeast(0L))
                            onBack()
                        },
                        modifier = Modifier.testTag("player_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = strings.close,
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = video.displayName,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    // Top toolbar: Speed, Aspect Ratio, Lock icons aligned with video title (always at the top)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Playback Speed Button
                        IconButton(
                            onClick = {
                                showSpeedDialog = true
                                lastInteractionTime = System.currentTimeMillis()
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("player_speed_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = strings.playbackSpeed,
                                tint = if (playbackSpeed != 1.0f) VidooOrange else Color.White
                            )
                        }

                        // Aspect Ratio Button
                        IconButton(
                            onClick = {
                                currentAspectRatio = when (currentAspectRatio) {
                                    AspectRatioMode.FIT -> AspectRatioMode.FILL
                                    AspectRatioMode.FILL -> AspectRatioMode.STRETCH
                                    AspectRatioMode.STRETCH -> AspectRatioMode.FIT
                                }
                                val aspectLabel = when (currentAspectRatio) {
                                    AspectRatioMode.FIT -> strings.aspectRatioFit
                                    AspectRatioMode.FILL -> strings.aspectRatioFill
                                    AspectRatioMode.STRETCH -> strings.aspectRatio16_9
                                }
                                hudText = "${strings.aspectRatio}: $aspectLabel"
                                hudIcon = Icons.Default.AspectRatio
                                hudPercentage = 1f
                                showHud = true
                                lastInteractionTime = System.currentTimeMillis()
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("player_aspect_ratio_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AspectRatio,
                                contentDescription = strings.aspectRatio,
                                tint = Color.White
                            )
                        }

                        // Lock Screen Button
                        IconButton(
                            onClick = {
                                isLocked = true
                                showControls = false
                                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("player_lock_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.LockOpen,
                                contentDescription = strings.lockScreen,
                                tint = Color.White
                            )
                        }
                    }
                }

                // Bottom Controls: Redesigned modern 2-row layout matching exact structure
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.65f),
                                    Color.Black.copy(alpha = 0.92f)
                                )
                            )
                        )
                        .navigationBarsPadding()
                        .padding(start = 16.dp, end = 16.dp, bottom = 12.dp, top = 16.dp)
                        .onSizeChanged { size ->
                            bottomControlsHeightDp = with(density) { size.height.toDp() }
                        }
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val effectivePosition = if (isSeeking) seekPosition.toLong() else currentPosition
                        val safeDuration = duration.coerceAtLeast(1L)

                        // Top row: current time (left) — thin seek/progress bar (center, filling the space) — total duration (right) — rotate/orientation icon (far right, same row)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Current time (left)
                            Text(
                                text = formatTime(effectivePosition),
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.testTag("player_current_time")
                            )

                            // Thin seek/progress bar (center, filling the space)
                            Slider(
                                value = (effectivePosition.toFloat() / safeDuration).coerceIn(0f, 1f),
                                onValueChange = { fraction ->
                                    isSeeking = true
                                    seekPosition = fraction * safeDuration
                                    lastInteractionTime = System.currentTimeMillis()
                                },
                                onValueChangeFinished = {
                                    exoPlayer.seekTo(seekPosition.toLong())
                                    currentPosition = seekPosition.toLong()
                                    isSeeking = false
                                    lastInteractionTime = System.currentTimeMillis()
                                },
                                colors = SliderDefaults.colors(
                                    thumbColor = VidooOrange,
                                    activeTrackColor = VidooOrange,
                                    inactiveTrackColor = Color.White.copy(alpha = 0.30f)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 8.dp)
                                    .height(28.dp)
                                    .testTag("player_seek_bar")
                            )

                            // Total duration (right)
                            Text(
                                text = formatTime(duration),
                                color = Color.White.copy(alpha = 0.75f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.testTag("player_total_duration")
                            )

                            Spacer(modifier = Modifier.width(4.dp))

                            // Rotate / orientation icon (far right, same row)
                            IconButton(
                                onClick = {
                                    activity?.let { act ->
                                        val currentOrientation = act.requestedOrientation
                                        act.requestedOrientation = if (currentOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                                            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                                        } else {
                                            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                                        }
                                    }
                                    lastInteractionTime = System.currentTimeMillis()
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("player_rotate_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ScreenRotation,
                                    contentDescription = strings.rotateScreen,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        // Bottom row: five buttons evenly spaced in one row, in this order:
                        // rewind-10s, previous track, play/pause (large circular button, bigger than the others), next track, forward-10s
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 2.dp, bottom = 4.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 1. Rewind -10s (thin outlined icon, no filled background, large touch target)
                            IconButton(
                                onClick = {
                                    val newPos = (exoPlayer.currentPosition - 10000L).coerceAtLeast(0L)
                                    exoPlayer.seekTo(newPos)
                                    currentPosition = newPos
                                    lastInteractionTime = System.currentTimeMillis()
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("player_rewind_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Replay10,
                                    contentDescription = "Rewind 10s",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            // 2. Previous track (|◁) (thin outlined icon, no filled background, large touch target)
                            IconButton(
                                onClick = {
                                    if (hasPrevious) {
                                        viewModel.saveProgress(video.contentUri, exoPlayer.currentPosition, exoPlayer.duration.coerceAtLeast(0L))
                                        onPlayPrevious()
                                        lastInteractionTime = System.currentTimeMillis()
                                    }
                                },
                                enabled = hasPrevious,
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("player_previous_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SkipPrevious,
                                    contentDescription = "Previous video",
                                    tint = if (hasPrevious) Color.White else Color.White.copy(alpha = 0.35f),
                                    modifier = Modifier.size(30.dp)
                                )
                            }

                            // 3. Play / Pause / Replay (large circular button, bigger than the others, orange accent fill)
                            val isAtEnd = exoPlayer.playbackState == Player.STATE_ENDED ||
                                    (duration > 0 && exoPlayer.currentPosition >= duration - 300L && !isPlaying)
                            IconButton(
                                onClick = {
                                    if (isAtEnd) {
                                        exoPlayer.seekTo(0L)
                                        currentPosition = 0L
                                        exoPlayer.prepare()
                                        exoPlayer.play()
                                    } else if (exoPlayer.isPlaying) {
                                        exoPlayer.pause()
                                    } else {
                                        exoPlayer.play()
                                    }
                                    lastInteractionTime = System.currentTimeMillis()
                                },
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(VidooOrange)
                                    .testTag("player_play_pause_button")
                            ) {
                                Icon(
                                    imageVector = when {
                                        isPlaying -> Icons.Default.Pause
                                        isAtEnd -> Icons.Default.Replay
                                        else -> Icons.Default.PlayArrow
                                    },
                                    contentDescription = when {
                                        isPlaying -> "Pause"
                                        isAtEnd -> "Replay"
                                        else -> "Play"
                                    },
                                    tint = Color.Black,
                                    modifier = Modifier.size(36.dp)
                                )
                            }

                            // 4. Next track (▷|) (thin outlined icon, no filled background, large touch target)
                            IconButton(
                                onClick = {
                                    if (hasNext) {
                                        viewModel.saveProgress(video.contentUri, exoPlayer.currentPosition, exoPlayer.duration.coerceAtLeast(0L))
                                        onPlayNext()
                                        lastInteractionTime = System.currentTimeMillis()
                                    }
                                },
                                enabled = hasNext,
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("player_next_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SkipNext,
                                    contentDescription = "Next video",
                                    tint = if (hasNext) Color.White else Color.White.copy(alpha = 0.35f),
                                    modifier = Modifier.size(30.dp)
                                )
                            }

                            // 5. Forward +10s (thin outlined icon, no filled background, large touch target)
                            IconButton(
                                onClick = {
                                    val newPos = (exoPlayer.currentPosition + 10000L).coerceAtMost(duration)
                                    exoPlayer.seekTo(newPos)
                                    currentPosition = newPos
                                    lastInteractionTime = System.currentTimeMillis()
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("player_forward_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Forward10,
                                    contentDescription = "Forward 10s",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Gesture Guide Overlay
        AnimatedVisibility(
            visible = showGestureGuide,
            enter = fadeIn() + scaleIn(initialScale = 0.96f),
            exit = fadeOut() + scaleOut(targetScale = 0.96f)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f))
                    .clickable {
                        showGestureGuide = false
                        viewModel.setHasSeenGestureGuide(true)
                    }
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = VidooSurface,
                    border = BorderStroke(1.dp, VidooBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .clickable(enabled = false) {}
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(VidooOrange.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.HelpOutline,
                                    contentDescription = null,
                                    tint = VidooOrange,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = strings.gestureGuideTitle,
                                    color = VidooTextPrimary,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = strings.gestureGuideSubtitle,
                                    color = VidooTextTertiary,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        GestureGuideItem(
                            icon = Icons.Default.WbSunny,
                            title = strings.gestureBrightnessTitle,
                            desc = strings.gestureBrightnessDesc
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        GestureGuideItem(
                            icon = Icons.Default.VolumeUp,
                            title = strings.gestureVolumeTitle,
                            desc = strings.gestureVolumeDesc
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        GestureGuideItem(
                            icon = Icons.Default.Forward10,
                            title = strings.gestureSeekingTitle,
                            desc = strings.gestureSeekingDesc
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        GestureGuideItem(
                            icon = Icons.Default.FastForward,
                            title = strings.gestureSpeedTitle,
                            desc = strings.gestureSpeedDesc
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        androidx.compose.material3.Button(
                            onClick = {
                                showGestureGuide = false
                                viewModel.setHasSeenGestureGuide(true)
                            },
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = VidooOrange,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("gesture_guide_got_it_button")
                        ) {
                            Text(
                                text = strings.gestureGotIt,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    // Speed Control Dialog
    if (showSpeedDialog) {
        val speeds = listOf(0.25f, 0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f)
        AlertDialog(
            onDismissRequest = { showSpeedDialog = false },
            containerColor = VidooSurface,
            title = {
                Text(
                    text = strings.playbackSpeed,
                    color = VidooTextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    speeds.forEach { speed ->
                        val isSelected = speed == playbackSpeed
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    playbackSpeed = speed
                                    exoPlayer.playbackParameters = PlaybackParameters(speed)
                                    showSpeedDialog = false
                                }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (speed == 1.0f) "${strings.speedNormal} (1.0x)" else "${speed}x",
                                color = if (isSelected) VidooOrange else VidooTextPrimary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 15.sp
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = VidooOrange,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSpeedDialog = false }) {
                    Text(strings.close, color = VidooOrange)
                }
            }
        )
    }

    // Subtitle loaded notification dialog
    if (showSubtitleInfo) {
        AlertDialog(
            onDismissRequest = { showSubtitleInfo = false },
            containerColor = VidooSurface,
            title = {
                Text(strings.subtitleFilePrompt, color = VidooTextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    text = "${strings.subtitles}: ${subtitles.size}",
                    color = VidooTextSecondary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { showSubtitleInfo = false }) {
                    Text(strings.ok, color = VidooOrange)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    subtitles = emptyList()
                    activeSubtitleText = ""
                    showSubtitleInfo = false
                }) {
                    Text(strings.removeSubtitle, color = Color(0xFFFF5252))
                }
            }
        )
    }
}

private fun formatTime(millis: Long): String {
    val safeMs = millis.coerceAtLeast(0L)
    val hours = TimeUnit.MILLISECONDS.toHours(safeMs)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(safeMs) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(safeMs) % 60
    return if (hours > 0) {
        String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }
}

@Composable
private fun GestureGuideItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    desc: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF141417), RoundedCornerShape(12.dp))
            .border(BorderStroke(1.dp, VidooBorder), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(VidooSurface, CircleShape)
                .border(BorderStroke(1.dp, VidooBorder), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = VidooOrange,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = VidooTextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = desc,
                color = VidooTextSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
    }
}
