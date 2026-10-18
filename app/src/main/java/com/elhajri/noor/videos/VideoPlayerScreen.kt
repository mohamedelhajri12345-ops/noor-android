package com.elhajri.noor.videos

import android.annotation.SuppressLint
import android.app.Activity
import android.content.res.Configuration
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import com.elhajri.noor.data.Prefs
import com.elhajri.noor.theme.noorGlassCard
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.formatPlayerTime
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.elhajri.noor.ui.TextMain

/**
 * The player HTML is hosted on a real https origin (GitHub Pages). YouTube rejects embeds
 * whose host page has no valid origin (errors 152/153/2/5) — this was the root cause of
 * "video unavailable" for every video when the HTML was loaded via loadDataWithBaseURL.
 */
private const val PLAYER_PAGE_URL = "https://mohamedelhajri12345-ops.github.io/noor-android/player.html"

/** Hosts the player may navigate to; everything else is blocked to keep the user in-app. */
private val ALLOWED_HOSTS = listOf(
    "mohamedelhajri12345-ops.github.io",
    "youtube.com", "www.youtube.com", "www.youtube-nocookie.com",
    "ytimg.com", "i.ytimg.com", "googlevideo.com", "gstatic.com", "googleapis.com",
    // direct MP4 sources (public-domain / CC content)
    "archive.org", "www.archive.org", "us.archive.org", "ia801402.us.archive.org", "ia601402.us.archive.org",
    "archive.org:443"
)

/**
 * JS Bridge interface for forwarding YouTube IFrame events from the hosted player page to Compose.
 */
class VideoPlayerJsBridge(
    private val onStateChange: (Int) -> Unit,
    private val onProgress: (Float, Float, Boolean) -> Unit,
    private val onEnded: () -> Unit,
    private val onPlayerReady: () -> Unit = {},
    private val onVideoError: (Int) -> Unit = {}
) {
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
 * Custom Video Player Screen for Kids Content.
 * Portrait: video card on top + full glass control deck below (all buttons always visible).
 * Landscape: immersive fullscreen TV mode with tap-to-show overlay controls.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun VideoPlayerScreen(
    initialVideoId: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var currentVideoId by remember { mutableStateOf(initialVideoId) }
    val currentVideo = remember(currentVideoId) {
        KidsVideoStore.getVideoById(context, currentVideoId)
    }

    var isPlaying by remember { mutableStateOf(false) }
    var currentTime by remember { mutableStateOf(0f) }
    var duration by remember { mutableStateOf(0f) }
    var isEndedOverlayVisible by remember { mutableStateOf(false) }
    var consecutiveErrors by remember { mutableStateOf(0) }
    var playbackRate by remember { mutableStateOf(1f) }
    var speedMenuExpanded by remember { mutableStateOf(false) }
    var isPlayerReady by remember { mutableStateOf(false) }
    var areControlsVisible by remember { mutableStateOf(true) }
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }

    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    /** Builds the JS call for a video: direct MP4 when available, YouTube otherwise. */
    fun jsLoadCall(v: com.elhajri.noor.videos.KidsVideo): String =
        if (v.mp4Url != null) "loadNewVideo(null, '${v.mp4Url}');"
        else "loadNewVideo('${v.id}');"

    fun playNextVideo() {
        val next = KidsVideoStore.getNextVideo(context, currentVideoId)
        if (next != null) {
            currentVideoId = next.id
            currentTime = 0f
            isEndedOverlayVisible = false
            isPlayerReady = false
            webViewInstance?.evaluateJavascript(jsLoadCall(next), null)
        }
    }

    fun playPrevVideo() {
        val prev = KidsVideoStore.getPrevVideo(context, currentVideoId)
        if (prev != null) {
            currentVideoId = prev.id
            currentTime = 0f
            isEndedOverlayVisible = false
            isPlayerReady = false
            webViewInstance?.evaluateJavascript(jsLoadCall(prev), null)
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

    // Record video watch
    LaunchedEffect(currentVideoId) {
        VideoGatingStore.recordVideoWatched(context, currentVideoId)
        currentVideo?.let {
            Prefs.saveLastWatchedVideo(context, it.id, it.title, currentTime.toLong())
        }
        isEndedOverlayVisible = false
    }

    // Save playback position periodically
    LaunchedEffect(currentTime) {
        if (currentTime > 0f && currentVideo != null) {
            Prefs.saveLastWatchedVideo(context, currentVideo.id, currentVideo.title, currentTime.toLong())
        }
    }

    // Auto-hide overlay controls after 4s in landscape while playing
    LaunchedEffect(areControlsVisible, isPlaying, isLandscape) {
        if (isLandscape && areControlsVisible && isPlaying) {
            delay(4000)
            areControlsVisible = false
        }
    }

    // Immersive TV mode: hide system bars while landscape, restore on exit
    DisposableEffect(isLandscape) {
        val window = (context as? Activity)?.window
        if (window != null) {
            val controller = WindowCompat.getInsetsController(window, window.decorView)
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            if (isLandscape) {
                controller.hide(WindowInsetsCompat.Type.systemBars())
            } else {
                controller.show(WindowInsetsCompat.Type.systemBars())
            }
        }
        onDispose { }
    }

    BackHandler {
        webViewInstance?.evaluateJavascript("pauseVideo();", null)
        onBack()
    }

    DisposableEffect(Unit) {
        onDispose {
            webViewInstance?.let { wv ->
                wv.evaluateJavascript("pauseVideo();", null)
                wv.stopLoading()
                wv.destroy()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ==================== VIDEO AREA ====================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (isLandscape) Modifier.weight(1f) else Modifier.aspectRatio(16f / 9f))
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
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
                                cacheMode = WebSettings.LOAD_DEFAULT
                            }
                            webChromeClient = WebChromeClient()
                            webViewClient = object : WebViewClient() {
                                override fun shouldOverrideUrlLoading(
                                    view: WebView?,
                                    request: android.webkit.WebResourceRequest?
                                ): Boolean {
                                    // Keep the user inside the app: allow only the player page and YouTube CDN hosts
                                    val url = request?.url?.host ?: return true
                                    return ALLOWED_HOSTS.none { url == it || url.endsWith(".$it") }
                                }
                            }

                            val bridge = VideoPlayerJsBridge(
                                onStateChange = { state ->
                                    // 1 = PLAYING, 2 = PAUSED, 0 = ENDED
                                    isPlaying = (state == 1)
                                    if (state == 1) consecutiveErrors = 0
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
                                onVideoError = { code ->
                                    // 101/150 = embedding disabled (permanent); 100/2/5 often transient (network/sign-in wall).
                                    // Owner requirement: never auto-skip rapidly — the user decides.
                                    isPlaying = false
                                    if (code == 101 || code == 150) {
                                        Prefs.addBrokenVideo(context, currentVideoId)
                                        KidsVideoStore.invalidate()
                                        Toast.makeText(
                                            context,
                                            "هذا الفيديو لا يسمح بالتشغيل المدمج — اضغط زر التالي لاختيار غيره",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "تعذر تشغيل الفيديو الآن — تحقق من الاتصال أو جرّب فيديو آخر",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            )

                            addJavascriptInterface(bridge, "AndroidBridge")
                            val v = KidsVideoStore.getVideoById(context, currentVideoId)
                            if (v?.mp4Url != null) {
                                loadUrl("$PLAYER_PAGE_URL?mode=mp4&src=" + java.net.URLEncoder.encode(v.mp4Url, "UTF-8"))
                            } else {
                                loadUrl("$PLAYER_PAGE_URL?v=$currentVideoId")
                            }
                            webViewInstance = this
                        }
                    },
                    update = { wv -> webViewInstance = wv },
                    modifier = Modifier.fillMaxSize()
                )

                // Loading spinner until the player reports ready
                if (!isPlayerReady) {
                    CircularProgressIndicator(
                        color = Gold,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            // ==================== PORTRAIT CONTROL DECK (always visible, always tappable) ====================
            if (!isLandscape) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .noorGlassCard(cornerRadius = 22.dp, borderWidth = 0.dp)
                        .padding(16.dp)
                ) {
                    // Title + channel
                    Text(
                        text = currentVideo?.title ?: "مشغل الفيديو",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = currentVideo?.channel ?: "",
                            color = GoldSoft,
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                        currentVideo?.category?.let { cat ->
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Gold.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 1.dp)
                            ) {
                                Text(cat, color = Gold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    // Seek slider + times
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatPlayerTime(currentTime),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = formatPlayerTime(duration),
                            color = GoldSoft,
                            fontSize = 12.sp
                        )
                    }
                    Slider(
                        value = if (duration > 0f) currentTime.coerceIn(0f, duration) else 0f,
                        onValueChange = { seekTo(it) },
                        valueRange = 0f..(if (duration > 0f) duration else 1f),
                        colors = SliderDefaults.colors(
                            thumbColor = Gold,
                            activeTrackColor = Gold,
                            inactiveTrackColor = Color.White.copy(alpha = 0.25f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(6.dp))

                    // Full control row: prev | -10s | play | +10s | next | speed
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GlassIconButton(onClick = { playPrevVideo() }, icon = Icons.Default.SkipPrevious, desc = "السابق")
                        GlassIconButton(onClick = { skipBy(-10f) }, icon = Icons.Default.Replay10, desc = "تراجع 10 ثوان")

                        // Main play/pause
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

                        // Speed menu
                        Box {
                            GlassIconButton(onClick = { speedMenuExpanded = true }, icon = Icons.Default.Speed, desc = "سرعة التشغيل")
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
                    }

                    Spacer(Modifier.height(14.dp))

                    // Related videos (our own list, replaces YouTube suggestions)
                    val related = remember(currentVideoId) {
                        val cat = currentVideo?.category
                        KidsVideoStore.loadVideos(context)
                            .filter { it.category == cat && it.id != currentVideoId }
                            .shuffled()
                            .take(12)
                    }
                    if (related.isNotEmpty()) {
                        Text(
                            "فيديوهات مشابهة",
                            color = Gold,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(related, key = { it.id }) { v ->
                                Column(
                                    modifier = Modifier
                                        .width(136.dp)
                                        .noorGlassCard(cornerRadius = 12.dp)
                                        .clickable {
                                            currentVideoId = v.id
                                            currentTime = 0f
                                            isEndedOverlayVisible = false
                                            isPlayerReady = false
                                            webViewInstance?.evaluateJavascript("loadNewVideo('${v.id}');", null)
                                        }
                                ) {
                                    AsyncImage(
                                        model = v.thumbnailUrl,
                                        contentDescription = v.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxWidth().height(76.dp)
                                    )
                                    Text(
                                        v.title,
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ==================== LANDSCAPE: FULLSCREEN OVERLAY CONTROLS ====================
        if (isLandscape) {
            // Tap catcher: tap anywhere toggles controls
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { areControlsVisible = !areControlsVisible }
            )
            AnimatedVisibility(
                visible = areControlsVisible || !isPlaying || isEndedOverlayVisible,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            if (isEndedOverlayVisible || !isPlaying) {
                                Brush.verticalGradient(
                                    listOf(Color.Black.copy(alpha = 0.97f), Color.Black.copy(alpha = 0.97f))
                                )
                            } else {
                                Brush.verticalGradient(
                                    listOf(
                                        Color.Black.copy(alpha = 0.75f),
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.80f)
                                    )
                                )
                            }
                        )
                ) {
                    // Top bar: back + title
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            webViewInstance?.evaluateJavascript("pauseVideo();", null)
                            onBack()
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "رجوع",
                                tint = Gold,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Text(
                            text = currentVideo?.title ?: "",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Center controls
                    Row(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GlassIconButton(onClick = { playPrevVideo() }, icon = Icons.Default.SkipPrevious, desc = "السابق")
                        GlassIconButton(onClick = { skipBy(-10f) }, icon = Icons.Default.Replay10, desc = "تراجع 10 ثوان")

                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(Gold, Color(0xFFC5A028))))
                                .clickable { togglePlayPause() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "إيقاف مؤقت" else "تشغيل",
                                tint = Color(0xFF0B1120),
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        GlassIconButton(onClick = { skipBy(10f) }, icon = Icons.Default.Forward10, desc = "تقديم 10 ثوان")
                        GlassIconButton(onClick = { playNextVideo() }, icon = Icons.Default.SkipNext, desc = "التالي")

                        Box {
                            GlassIconButton(onClick = { speedMenuExpanded = true }, icon = Icons.Default.Speed, desc = "سرعة التشغيل")
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
                    }

                    // Bottom: slider + times
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(formatPlayerTime(currentTime), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(formatPlayerTime(duration), color = GoldSoft, fontSize = 12.sp)
                        }
                        Slider(
                            value = if (duration > 0f) currentTime.coerceIn(0f, duration) else 0f,
                            onValueChange = { seekTo(it) },
                            valueRange = 0f..(if (duration > 0f) duration else 1f),
                            colors = SliderDefaults.colors(
                                thumbColor = Gold,
                                activeTrackColor = Gold,
                                inactiveTrackColor = Color.White.copy(alpha = 0.25f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // ==================== ENDED OVERLAY (both orientations) ====================
        if (isEndedOverlayVisible) {
            val nextVideo = KidsVideoStore.getNextVideo(context, currentVideoId)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.92f)),
                contentAlignment = Alignment.Center
            ) {
                if (nextVideo != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .noorGlassCard(cornerRadius = 24.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("انتهى الفيديو", color = GoldSoft, fontSize = 13.sp)
                            Spacer(Modifier.height(6.dp))
                            Text(nextVideo.title, color = TextMain, fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            Spacer(Modifier.height(14.dp))
                            Button(
                                onClick = {
                                    currentVideoId = nextVideo.id
                                    currentTime = 0f
                                    isEndedOverlayVisible = false
                                    isPlayerReady = false
                                    webViewInstance?.evaluateJavascript("loadNewVideo('${nextVideo.id}');", null)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Gold)
                            ) {
                                Text("تشغيل الفيديو التالي", color = Color(0xFF0B1120), fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.height(6.dp))
                            TextButton(onClick = {
                                isEndedOverlayVisible = false
                                seekTo(0f)
                                webViewInstance?.evaluateJavascript("playVideo();", null)
                            }) {
                                Text("إعادة تشغيل الفيديو الحالي", color = GoldSoft)
                            }
                        }
                    }
                } else {
                    Text("وصلت لنهاية القائمة", color = TextMain, fontSize = 14.sp)
                }
            }
        }
    }
}

/**
 * Small circular glass button with gold border used across the player.
 */
@Composable
private fun GlassIconButton(
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    desc: String
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.35f))
            .border(1.dp, Gold.copy(alpha = 0.5f), CircleShape)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = desc,
            tint = Gold,
            modifier = Modifier.size(24.dp)
        )
    }
}
