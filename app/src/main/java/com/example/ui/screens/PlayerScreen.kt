package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.media.AudioManager
import android.net.Uri
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
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
import com.example.ui.viewmodel.VideoPlayerViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

enum class SpeedGestureMode {
    FORWARD_2X,
    REWIND_2X
}

enum class AspectRatioMode(val displayName: String, val mode: Int) {
    FIT("Fit", AspectRatioFrameLayout.RESIZE_MODE_FIT),
    FILL("Crop / Fill", AspectRatioFrameLayout.RESIZE_MODE_ZOOM),
    STRETCH("Stretch", AspectRatioFrameLayout.RESIZE_MODE_FILL)
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
    val activity = context as? Activity
    val lifecycleOwner = LocalLifecycleOwner.current
    val settings by viewModel.settings.collectAsState()

    // ExoPlayer instance configured with software decoder fallback and proper audio attributes
    val exoPlayer = remember {
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Mobile Safari/537.36")
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(15000)
            .setReadTimeoutMs(20000)

        val dataSourceFactory = DefaultDataSource.Factory(context, httpDataSourceFactory)
        val mediaSourceFactory = DefaultMediaSourceFactory(dataSourceFactory)

        // Enable fallback to software decoders if hardware decoder component interface queries fail
        val renderersFactory = DefaultRenderersFactory(context).apply {
            setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF)
            setEnableDecoderFallback(true)
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

    // Gesture-based Speed & Rewind Hold state (Long-press left/right)
    var speedGestureActive by remember { mutableStateOf<SpeedGestureMode?>(null) }
    var wasPlayingBeforeSpeedGesture by remember { mutableStateOf(false) }
    val hapticFeedback = LocalHapticFeedback.current

    // Subtitles state
    var subtitles by remember { mutableStateOf<List<SubtitleItem>>(emptyList()) }
    var activeSubtitleText by remember { mutableStateOf("") }
    var subtitleFileName by remember { mutableStateOf<String?>(null) }
    var showSubtitleInfo by remember { mutableStateOf(false) }

    // Resume pill
    var resumeNotification by remember { mutableStateOf<String?>(null) }

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
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            // Restore portrait orientation upon exit
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            exoPlayer.stop()
            exoPlayer.clearMediaItems()
            exoPlayer.release()
        }
    }

    // Back handler: unlock first if screen is locked, otherwise save and exit
    BackHandler {
        if (isLocked) {
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
        playerErrorMessage = null
        autoRetryCount = 0
        subtitles = emptyList()
        activeSubtitleText = ""
        subtitleFileName = null
        showSubtitleInfo = false
        resumeNotification = null

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
                val timeStr = formatTime(savedPos)
                resumeNotification = "Resumed from $timeStr"
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
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }

            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY) {
                    duration = exoPlayer.duration.coerceAtLeast(0L)
                    playerErrorMessage = null
                } else if (state == Player.STATE_ENDED) {
                    isPlaying = false
                    showControls = true
                    viewModel.saveProgress(video.contentUri, 0L, duration)
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                val cause = error.cause
                val isDecoderReclaimed = error.errorCode == PlaybackException.ERROR_CODE_DECODING_RESOURCES_RECLAIMED ||
                        error.errorCode == PlaybackException.ERROR_CODE_DECODER_INIT_FAILED

                if (isDecoderReclaimed && autoRetryCount < 2) {
                    autoRetryCount++
                    val savedPos = exoPlayer.currentPosition
                    coroutineScope.launch {
                        delay(350L)
                        try {
                            exoPlayer.prepare()
                            if (savedPos > 0L) {
                                exoPlayer.seekTo(savedPos)
                            }
                            exoPlayer.play()
                            playerErrorMessage = null
                        } catch (_: Exception) {
                            playerErrorMessage = "Video decoder was reclaimed by the system. Tap Retry to reload."
                        }
                    }
                    return
                }

                playerErrorMessage = when {
                    isDecoderReclaimed ->
                        "Video decoder was reclaimed by the system. Tap Retry to reload."
                    cause is HttpDataSource.InvalidResponseCodeException ->
                        "Server responded with error (HTTP ${cause.responseCode})"
                    cause is java.net.UnknownHostException ->
                        "Network unavailable. Please check your internet connection."
                    cause is java.lang.SecurityException ->
                        "Access error: ${cause.message}"
                    error.errorCode == PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED ->
                        "Network connection failed"
                    else -> error.localizedMessage ?: "Unable to play video"
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

    // Auto-dismiss resume notification
    LaunchedEffect(resumeNotification) {
        if (resumeNotification != null) {
            delay(3000)
            resumeNotification = null
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

    Box(
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
                            onDrag = { change, dragAmount ->
                                change.consume()
                                val isRightSide = change.position.x > size.width / 2
                                val deltaY = -dragAmount.y // upward swipe is positive

                                if (isRightSide) {
                                    // Volume gesture
                                    val currentVol = try {
                                        audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: 0
                                    } catch (_: Exception) {
                                        0
                                    }
                                    val step = (deltaY / size.height * maxVolume * 2).roundToInt()
                                    val newVol = (currentVol + step).coerceIn(0, maxVolume)
                                    try {
                                        audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, newVol, 0)
                                    } catch (_: Exception) {}
                                    val pct = (newVol.toFloat() / maxVolume)
                                    hudIcon = when {
                                        newVol == 0 -> Icons.Default.VolumeMute
                                        pct < 0.5f -> Icons.Default.VolumeDown
                                        else -> Icons.Default.VolumeUp
                                    }
                                    hudPercentage = pct
                                    hudText = "Volume ${(pct * 100).roundToInt()}%"
                                    showHud = true
                                } else {
                                    // Brightness gesture
                                    val currentBrightness = activity?.window?.attributes?.screenBrightness ?: 0.5f
                                    val safeCurrent = if (currentBrightness < 0f) 0.5f else currentBrightness
                                    val step = deltaY / size.height * 1.5f
                                    val newBrightness = (safeCurrent + step).coerceIn(0.01f, 1.0f)
                                    activity?.let { act ->
                                        val lp = act.window.attributes
                                        lp.screenBrightness = newBrightness
                                        act.window.attributes = lp
                                    }
                                    hudIcon = Icons.Default.BrightnessMedium
                                    hudPercentage = newBrightness
                                    hudText = "Brightness ${(newBrightness * 100).roundToInt()}%"
                                    showHud = true
                                }
                                lastInteractionTime = System.currentTimeMillis()
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

        // Resume playback notification pill
        resumeNotification?.let { msg ->
            Surface(
                color = VidooSurface,
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, VidooOrange),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 70.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = msg,
                        color = VidooTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Restart",
                        color = VidooOrange,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {
                            exoPlayer.seekTo(0L)
                            resumeNotification = null
                        }
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
                        contentDescription = "Playback Error",
                        tint = Color(0xFFEF5350),
                        modifier = Modifier.size(52.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Playback Error",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = playerErrorMessage ?: "An error occurred while loading this video.",
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
                            Text("Go Back", color = Color.White)
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
                            Text("Retry", color = VidooOrange, fontWeight = FontWeight.Bold)
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

        // Controls Overlay (Top bar, Center buttons, Bottom controls)
        AnimatedVisibility(
            visible = showControls && !isLocked,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f))
                    .statusBarsPadding()
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
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
                            contentDescription = "Close player",
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
                }

                // Center Playback Action Buttons (Strictly centered in the middle of the screen)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Previous Video Track (|◁)
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
                                .clip(CircleShape)
                                .background(if (hasPrevious) Color.Black.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.2f))
                                .testTag("player_previous_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipPrevious,
                                contentDescription = "Previous video",
                                tint = if (hasPrevious) Color.White else Color.White.copy(alpha = 0.3f),
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        // Rewind -10s
                        IconButton(
                            onClick = {
                                val newPos = (exoPlayer.currentPosition - 10000L).coerceAtLeast(0L)
                                exoPlayer.seekTo(newPos)
                                currentPosition = newPos
                                lastInteractionTime = System.currentTimeMillis()
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.6f))
                                .testTag("player_rewind_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Replay10,
                                contentDescription = "Rewind 10s",
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        // Large Play / Pause / Replay button
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
                                .size(68.dp)
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
                                modifier = Modifier.size(40.dp)
                            )
                        }

                        // Forward +10s
                        IconButton(
                            onClick = {
                                val newPos = (exoPlayer.currentPosition + 10000L).coerceAtMost(duration)
                                exoPlayer.seekTo(newPos)
                                currentPosition = newPos
                                lastInteractionTime = System.currentTimeMillis()
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.6f))
                                .testTag("player_forward_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Forward10,
                                contentDescription = "Forward 10s",
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        // Next Video Track (▷|)
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
                                .clip(CircleShape)
                                .background(if (hasNext) Color.Black.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.2f))
                                .testTag("player_next_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipNext,
                                contentDescription = "Next video",
                                tint = if (hasNext) Color.White else Color.White.copy(alpha = 0.3f),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }

                // Bottom Controls Bar (Single cohesive connected block: Time display & Resolution, Seek bar, Icon row)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    val effectivePosition = if (isSeeking) seekPosition.toLong() else currentPosition
                    val safeDuration = duration.coerceAtLeast(1L)

                    // 1. Time display & Resolution / Speed badges
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${formatTime(effectivePosition)} / ${formatTime(duration)}",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (playbackSpeed != 1.0f) {
                                Surface(
                                    color = VidooOrange.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "${playbackSpeed}x",
                                        color = VidooOrange,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            video.resolutionText()?.let { res ->
                                Surface(
                                    color = Color.White.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = res,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    // 2. Seek/progress bar — right below the time display
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
                            inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("player_seek_bar")
                    )

                    // 3. Icon row (Subtitles, Speed, Aspect Ratio, Rotation, Lock) — right below the seek bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("player_secondary_controls_row"),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Subtitle Button
                        IconButton(
                            onClick = {
                                srtPickerLauncher.launch(arrayOf("*/*"))
                                lastInteractionTime = System.currentTimeMillis()
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .testTag("player_subtitles_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Subtitles,
                                contentDescription = "Load Subtitle",
                                tint = if (subtitles.isNotEmpty()) VidooOrange else Color.White
                            )
                        }

                        // Playback Speed Button
                        IconButton(
                            onClick = {
                                showSpeedDialog = true
                                lastInteractionTime = System.currentTimeMillis()
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .testTag("player_speed_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = "Speed",
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
                                hudText = "Aspect: ${currentAspectRatio.displayName}"
                                hudIcon = Icons.Default.AspectRatio
                                hudPercentage = 1f
                                showHud = true
                                lastInteractionTime = System.currentTimeMillis()
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .testTag("player_aspect_ratio_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AspectRatio,
                                contentDescription = "Aspect Ratio",
                                tint = Color.White
                            )
                        }

                        // Orientation / Rotate Button
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
                                .size(44.dp)
                                .testTag("player_rotate_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ScreenRotation,
                                contentDescription = "Rotate screen",
                                tint = Color.White
                            )
                        }

                        // Screen Lock Button
                        IconButton(
                            onClick = {
                                isLocked = true
                                showControls = false
                                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .testTag("player_lock_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.LockOpen,
                                contentDescription = "Lock Screen Orientation and Controls",
                                tint = Color.White
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
                    text = "Playback Speed",
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
                                text = if (speed == 1.0f) "Normal (1.0x)" else "${speed}x",
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
                    Text("Close", color = VidooOrange)
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
                Text("Subtitle Loaded", color = VidooTextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    text = "Loaded ${subtitles.size} subtitle lines from external file. Subtitles are now synchronized with video playback.",
                    color = VidooTextSecondary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { showSubtitleInfo = false }) {
                    Text("OK", color = VidooOrange)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    subtitles = emptyList()
                    activeSubtitleText = ""
                    showSubtitleInfo = false
                }) {
                    Text("Remove Subtitle", color = Color(0xFFFF5252))
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
