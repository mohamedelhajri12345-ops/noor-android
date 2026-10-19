package com.elhajri.noor.videos

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.net.Uri
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import coil.compose.AsyncImage
import com.elhajri.noor.data.Prefs
import com.elhajri.noor.theme.noorGlassCard
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.formatPlayerTime
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Hosts the player may navigate to; everything else is blocked to keep the user in-app. */
private val ALLOWED_HOSTS = listOf(
    "youtube.com", "www.youtube.com", "www.youtube-nocookie.com",
    "ytimg.com", "i.ytimg.com", "googlevideo.com", "gstatic.com", "googleapis.com",
    "archive.org", "www.archive.org", "us.archive.org", "ia801402.us.archive.org", "ia601402.us.archive.org",
    "archive.org:443"
)

/**
 * JS Bridge interface for forwarding YouTube IFrame events from the local player page to Compose.
 */
class VideoPlayerJsBridge(
    private val onStateChange: (Int) -> Unit,
    private val onProgress: (Float, Float, Boolean) -> Unit,
    private val onEnded: () -> Unit,
    private val onPlayerReady: () -> Unit = {},
    private val onVideoError: (Int) -> Unit = {}
) {
    @JavascriptInterface
    fun sendState(state: Int) {
        onStateChange(state)
    }

    @JavascriptInterface
    fun sendStateChange(state: Int) {
        onStateChange(state)
    }

    @JavascriptInterface
    fun sendProgress(cur: Double, dur: Double, playing: Boolean) {
        onProgress(cur.toFloat(), dur.toFloat(), playing)
    }

    @JavascriptInterface
    fun sendVideoEnded() {
        onEnded()
    }

    @JavascriptInterface
    fun sendPlayerReady() {
        onPlayerReady()
    }

    @JavascriptInterface
    fun sendVideoError(code: Int) {
        onVideoError(code)
    }
}

/**
 * In-app YouTube & Direct MP4 Video Player Screen with NOOR Gold Custom Overlay Controls.
 * Supports:
 * - Local HTML asset player driving YouTube IFrame Player API & HTML5 video.
 * - Custom gold controls overlay: play/pause, skip +/-10s, speed 0.75x-2x, seekbar, and TV-like fullscreen button.
 * - Non-embeddable videos error view with fallback button to open in YouTube app.
 * - Suggested videos grid from app's curated catalog under player (never YouTube feed).
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun VideoPlayerScreen(
    initialVideoId: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val coroutineScope = rememberCoroutineScope()

    var currentVideoId by remember { mutableStateOf(initialVideoId) }
    val currentVideo = remember(currentVideoId) {
        KidsVideoStore.getVideoById(context, currentVideoId) ?: KidsVideo(
            id = currentVideoId,
            title = "فيديو يوتيوب",
            category = "عام",
            channel = "يوتيوب"
        )
    }

    var isPlaying by remember { mutableStateOf(false) }
    var currentTime by remember { mutableStateOf(0f) }
    var duration by remember { mutableStateOf(0f) }
    var isEndedOverlayVisible by remember { mutableStateOf(false) }
    var playbackRate by remember { mutableStateOf(1f) }
    var speedMenuExpanded by remember { mutableStateOf(false) }
    var isPlayerReady by remember { mutableStateOf(false) }
    var isErrorState by remember { mutableStateOf(false) }
    var areControlsVisible by remember { mutableStateOf(true) }
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }

    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    // Load local player.html asset
    val playerHtml = remember {
        try {
            context.assets.open("player.html").bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            ""
        }
    }

    fun launchYouTubeAppFallback() {
        val watchUrl = "https://www.youtube.com/watch?v=$currentVideoId"
        try {
            val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:$currentVideoId")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(appIntent)
            return
        } catch (_: Exception) { }
        try {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(watchUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(webIntent)
        } catch (_: Exception) {
            Toast.makeText(context, "تعذر فتح رابط يوتيوب", Toast.LENGTH_SHORT).show()
        }
    }

    fun loadVideoInWebView(vId: String) {
        val videoObj = KidsVideoStore.getVideoById(context, vId)
        if (videoObj?.mp4Url != null) {
            webViewInstance?.evaluateJavascript("loadNewVideo(null, '${videoObj.mp4Url}');", null)
        } else {
            webViewInstance?.evaluateJavascript("loadNewVideo('$vId');", null)
        }
    }

    fun playNextVideo() {
        val next = KidsVideoStore.getNextVideo(context, currentVideoId)
        if (next != null) {
            currentVideoId = next.id
            currentTime = 0f
            isEndedOverlayVisible = false
            isErrorState = false
            isPlayerReady = false
            loadVideoInWebView(next.id)
        }
    }

    fun playPrevVideo() {
        val prev = KidsVideoStore.getPrevVideo(context, currentVideoId)
        if (prev != null) {
            currentVideoId = prev.id
            currentTime = 0f
            isEndedOverlayVisible = false
            isErrorState = false
            isPlayerReady = false
            loadVideoInWebView(prev.id)
        }
    }

    fun togglePlayPause() {
        if (isPlaying) {
            webViewInstance?.evaluateJavascript("pauseVideo();", null)
        } else {
            if (isEndedOverlayVisible) {
                isEndedOverlayVisible = false
                webViewInstance?.evaluateJavascript("seekTo(0); playVideo();", null)
            } else {
                webViewInstance?.evaluateJavascript("playVideo();", null)
            }
        }
    }

    fun seekTo(seconds: Float) {
        currentTime = seconds
        webViewInstance?.evaluateJavascript("seekTo($seconds);", null)
    }

    fun setRate(rate: Float) {
        playbackRate = rate
        webViewInstance?.evaluateJavascript("setRate($rate);", null)
    }

    fun skipBy(delta: Float) {
        val target = (currentTime + delta).coerceIn(0f, if (duration > 0f) duration else 0f)
        seekTo(target)
    }

    fun toggleFullscreen() {
        if (activity != null) {
            if (isLandscape) {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            } else {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }
        }
    }

    // Save watch progress
    LaunchedEffect(currentVideoId) {
        VideoGatingStore.recordVideoWatched(context, currentVideoId)
        Prefs.saveLastWatchedVideo(context, currentVideo.id, currentVideo.title, currentTime.toLong())
        isEndedOverlayVisible = false
        isErrorState = false
    }

    // Auto-hide controls in landscape after 4 seconds of playing
    LaunchedEffect(areControlsVisible, isPlaying, isLandscape) {
        if (isLandscape && areControlsVisible && isPlaying) {
            delay(4000)
            areControlsVisible = false
        }
    }

    // Fullscreen TV Mode system bars control
    DisposableEffect(isLandscape) {
        val window = activity?.window
        if (window != null) {
            val controller = WindowCompat.getInsetsController(window, window.decorView)
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            if (isLandscape) {
                controller.hide(WindowInsetsCompat.Type.systemBars())
            } else {
                controller.show(WindowInsetsCompat.Type.systemBars())
            }
        }
        onDispose { }
    }

    // Restore orientation when screen exits
    DisposableEffect(Unit) {
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            webViewInstance?.let { wv ->
                wv.evaluateJavascript("pauseVideo();", null)
                wv.stopLoading()
                wv.destroy()
            }
        }
    }

    BackHandler {
        if (isLandscape) {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } else {
            webViewInstance?.evaluateJavascript("pauseVideo();", null)
            onBack()
        }
    }

    val suggestedVideos = remember(currentVideoId) {
        KidsVideoStore.loadVideos(context)
            .filter { it.id != currentVideoId }
            .shuffled()
            .take(12)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ==================== PLAYER VIEW AREA ====================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (isLandscape) Modifier.weight(1f) else Modifier.aspectRatio(16f / 9f))
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                if (isErrorState) {
                    // Non-embeddable video Arabic error view + fallback button
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF0D1322))
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.WarningAmber,
                            contentDescription = null,
                            tint = Gold,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "هذا الفيديو غير متاح للتشغيل المباشر داخل التطبيق بناءً على قيود الناشر",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "يمكنك فتح الفيديو مباشرة في تطبيق يوتيوب للاستمتاع بالمشاهدة",
                            color = GoldSoft,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = { launchYouTubeAppFallback() },
                            colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Color(0xFF0D1322)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("فتح في تطبيق يوتيوب", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                } else {
                    AndroidView(
                        factory = { ctx ->
                            WebView(ctx).apply {
                                layoutParams = android.view.ViewGroup.LayoutParams(
                                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                                    android.view.ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                settings.apply {
                                    javaScriptEnabled = true
                                    domStorageEnabled = true
                                    mediaPlaybackRequiresUserGesture = false
                                    cacheMode = WebSettings.LOAD_NO_CACHE
                                    allowFileAccess = true
                                }
                                webChromeClient = WebChromeClient()
                                webViewClient = object : WebViewClient() {
                                    override fun shouldOverrideUrlLoading(
                                        view: WebView?,
                                        request: android.webkit.WebResourceRequest?
                                    ): Boolean {
                                        val host = request?.url?.host ?: return true
                                        return ALLOWED_HOSTS.none { host == it || host.endsWith(".$it") }
                                    }
                                }

                                val bridge = VideoPlayerJsBridge(
                                    onStateChange = { state ->
                                        // 1 = PLAYING, 2 = PAUSED, 0 = ENDED
                                        isPlaying = (state == 1)
                                        if (state == 1) isErrorState = false
                                    },
                                    onProgress = { cur, dur, _ ->
                                        currentTime = cur
                                        duration = dur
                                    },
                                    onEnded = {
                                        isPlaying = false
                                        isEndedOverlayVisible = true
                                        coroutineScope.launch {
                                            delay(2000)
                                            if (isEndedOverlayVisible) playNextVideo()
                                        }
                                    },
                                    onPlayerReady = {
                                        isPlayerReady = true
                                    },
                                    onVideoError = { _ ->
                                        isPlaying = false
                                        isErrorState = true
                                    }
                                )

                                addJavascriptInterface(bridge, "AndroidBridge")

                                val vObj = KidsVideoStore.getVideoById(context, currentVideoId)
                                val baseUrl = if (vObj?.mp4Url != null) {
                                    "https://www.youtube.com/player.html?mode=mp4&src=" + java.net.URLEncoder.encode(vObj.mp4Url, "UTF-8")
                                } else {
                                    "https://www.youtube.com/player.html?v=$currentVideoId"
                                }
                                loadDataWithBaseURL(baseUrl, playerHtml, "text/html", "UTF-8", null)
                                webViewInstance = this
                            }
                        },
                        update = { wv -> webViewInstance = wv },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Loading indicator before player is ready
                    if (!isPlayerReady) {
                        CircularProgressIndicator(
                            color = Gold,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    // Tappable overlay for controls toggle in TV/fullscreen mode
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { areControlsVisible = !areControlsVisible }
                    )

                    // Embedded Gold Control Bar Overlay on video player
                    androidx.compose.animation.AnimatedVisibility(
                        visible = areControlsVisible || !isPlaying || isEndedOverlayVisible,
                        enter = fadeIn(),
                        exit = fadeOut(),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Color.Black.copy(alpha = 0.7f),
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.8f)
                                        )
                                    )
                                )
                        ) {
                            // Top overlay: back button & title
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .statusBarsPadding()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = {
                                    if (isLandscape) {
                                        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                                    } else {
                                        webViewInstance?.evaluateJavascript("pauseVideo();", null)
                                        onBack()
                                    }
                                }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "رجوع",
                                        tint = Gold,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                                Text(
                                    text = currentVideo.title,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            // Center overlay controls: play/pause & skip +/-10s
                            Row(
                                modifier = Modifier.align(Alignment.Center),
                                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                GlassIconButton(onClick = { playPrevVideo() }, icon = Icons.Default.SkipPrevious, desc = "السابق")
                                GlassIconButton(onClick = { skipBy(-10f) }, icon = Icons.Default.Replay10, desc = "تراجع 10 ثوان")

                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(Brush.linearGradient(listOf(Gold, Color(0xFFC5A028))))
                                        .clickable { togglePlayPause() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = if (isPlaying) "إيقاف مؤقت" else "تشغيل",
                                        tint = Color(0xFF0B1120),
                                        modifier = Modifier.size(32.dp)
                                    )
                                }

                                GlassIconButton(onClick = { skipBy(10f) }, icon = Icons.Default.Forward10, desc = "تقديم 10 ثوان")
                                GlassIconButton(onClick = { playNextVideo() }, icon = Icons.Default.SkipNext, desc = "التالي")
                            }

                            // Bottom overlay bar: progress slider, times, speed & fullscreen button
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .navigationBarsPadding()
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${formatPlayerTime(currentTime)} / ${formatPlayerTime(duration)}",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        // Speed button
                                        Box {
                                            TextButton(
                                                onClick = { speedMenuExpanded = true },
                                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Icon(Icons.Default.Speed, contentDescription = null, tint = Gold, modifier = Modifier.size(16.dp))
                                                Spacer(Modifier.width(4.dp))
                                                Text(if (playbackRate == 1f) "1x" else "${playbackRate}x", color = Gold, fontSize = 11.sp)
                                            }
                                            DropdownMenu(
                                                expanded = speedMenuExpanded,
                                                onDismissRequest = { speedMenuExpanded = false },
                                                modifier = Modifier.background(Color(0xFF0B1120))
                                            ) {
                                                listOf(0.75f, 1f, 1.25f, 1.5f, 2f).forEach { r ->
                                                    DropdownMenuItem(
                                                        text = {
                                                            Text(
                                                                if (r == 1f) "عادي" else "×$r",
                                                                color = if (r == playbackRate) Gold else Color.White,
                                                                fontWeight = if (r == playbackRate) FontWeight.Bold else FontWeight.Normal
                                                            )
                                                        },
                                                        onClick = {
                                                            setRate(r)
                                                            speedMenuExpanded = false
                                                        }
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(Modifier.width(8.dp))

                                        // Fullscreen button
                                        IconButton(onClick = { toggleFullscreen() }) {
                                            Icon(
                                                imageVector = if (isLandscape) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                                contentDescription = "ملء الشاشة",
                                                tint = Gold,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                }

                                Slider(
                                    value = if (duration > 0f) currentTime.coerceIn(0f, duration) else 0f,
                                    onValueChange = { seekTo(it) },
                                    valueRange = 0f..(if (duration > 0f) duration else 1f),
                                    colors = SliderDefaults.colors(
                                        thumbColor = Gold,
                                        activeTrackColor = Gold,
                                        inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(24.dp)
                                )
                            }
                        }
                    }
                }
            }

            // ==================== PORTRAIT CONTENT BELOW PLAYER ====================
            if (!isLandscape) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(12.dp)
                ) {
                    // Video Info Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .noorGlassCard(cornerRadius = 16.dp, borderWidth = 1.dp)
                            .padding(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Column {
                            Text(
                                text = currentVideo.title,
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = currentVideo.channel,
                                    color = GoldSoft,
                                    fontSize = 12.sp
                                )
                                Spacer(Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Gold.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = currentVideo.category,
                                        color = Gold,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    // Suggested Videos Header
                    Text(
                        text = "فيديوهات مقترحة",
                        color = Gold,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                    )

                    // Suggested Videos Grid (populated from app's curated catalog)
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(suggestedVideos, key = { it.id }) { item ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .noorGlassCard(cornerRadius = 12.dp, borderWidth = 1.dp)
                                    .clickable {
                                        currentVideoId = item.id
                                        currentTime = 0f
                                        isEndedOverlayVisible = false
                                        isErrorState = false
                                        isPlayerReady = false
                                        loadVideoInWebView(item.id)
                                    },
                                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                            ) {
                                Column {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(84.dp)
                                    ) {
                                        AsyncImage(
                                            model = item.thumbnailUrl,
                                            contentDescription = item.title,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(Color.Black.copy(alpha = 0.25f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PlayArrow,
                                                contentDescription = null,
                                                tint = Gold,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                    PaddingValues(6.dp).let {
                                        Column(modifier = Modifier.padding(6.dp)) {
                                            Text(
                                                text = item.title,
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(Modifier.height(2.dp))
                                            Text(
                                                text = item.channel,
                                                color = GoldSoft,
                                                fontSize = 9.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
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
    }
}

@Composable
private fun GlassIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    desc: String
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.15f))
            .border(1.dp, Gold.copy(alpha = 0.4f), CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = desc,
            tint = Color.White,
            modifier = Modifier.size(22.dp)
        )
    }
}
