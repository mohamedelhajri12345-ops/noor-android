package com.elhajri.noor.videos

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.elhajri.noor.data.Prefs
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.toArabicDigits
import com.elhajri.noor.ui.formatPlayerTime
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * JS Bridge interface for forwarding YouTube IFrame events to Compose.
 */
class VideoPlayerJsBridge(
    private val onStateChange: (Int) -> Unit,
    private val onProgress: (Float, Float, Boolean) -> Unit,
    private val onEnded: () -> Unit
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
}

/**
 * Custom Video Player Screen for Kids Content.
 * Embeds YouTube IFrame Player API inside a WebView with all native controls hidden,
 * wrapped in our own Compose Glass Overlay UI.
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

    // Auto-hide controls overlay after 4s
    var areControlsVisible by remember { mutableStateOf(true) }

    var webViewInstance by remember { mutableStateOf<WebView?>(null) }

    // Record video watch and save initial position
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

    fun playNextVideo() {
        val next = KidsVideoStore.getNextVideo(context, currentVideoId)
        if (next != null) {
            currentVideoId = next.id
            currentTime = 0f
            isEndedOverlayVisible = false
            webViewInstance?.evaluateJavascript("if(player && player.loadVideoById) player.loadVideoById('${next.id}');", null)
        }
    }

    fun playPrevVideo() {
        val prev = KidsVideoStore.getPrevVideo(context, currentVideoId)
        if (prev != null) {
            currentVideoId = prev.id
            currentTime = 0f
            isEndedOverlayVisible = false
            webViewInstance?.evaluateJavascript("if(player && player.loadVideoById) player.loadVideoById('${prev.id}');", null)
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

    // Handle Hardware Back Button
    BackHandler {
        webViewInstance?.evaluateJavascript("pauseVideo();", null)
        onBack()
    }

    // Clean up WebView on screen exit
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
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                areControlsVisible = !areControlsVisible
            }
    ) {
        // YouTube IFrame WebView Container
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
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                        }
                    }

                    val bridge = VideoPlayerJsBridge(
                        onStateChange = { state ->
                            // 1 = PLAYING, 2 = PAUSED, 0 = ENDED
                            isPlaying = (state == 1)
                        },
                        onProgress = { cur, dur, playing ->
                            currentTime = cur
                            duration = dur
                            isPlaying = playing
                        },
                        onEnded = {
                            isPlaying = false
                            isEndedOverlayVisible = true
                            // Auto advance after 1.5 seconds if user doesn't interact
                            coroutineScope.launch {
                                delay(1500)
                                if (isEndedOverlayVisible) {
                                    playNextVideo()
                                }
                            }
                        }
                    )

                    addJavascriptInterface(bridge, "AndroidBridge")

                    val htmlContent = buildYouTubeHtml(currentVideoId)
                    loadDataWithBaseURL("https://www.youtube.com", htmlContent, "text/html", "UTF-8", null)

                    webViewInstance = this
                }
            },
            update = { wv ->
                webViewInstance = wv
            },
            modifier = Modifier.fillMaxSize()
        )

        // Custom Compose Glass Controls Overlay
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
                        Brush.verticalGradient(
                            listOf(
                                Color.Black.copy(alpha = 0.8f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.85f)
                            )
                        )
                    )
            ) {
                // Top Bar: Back Button & Video Information
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            webViewInstance?.evaluateJavascript("pauseVideo();", null)
                            onBack()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع",
                            tint = Gold,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                    ) {
                        Text(
                            text = currentVideo?.title ?: "مشغل الفيديو",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = currentVideo?.channel ?: "",
                                color = GoldSoft,
                                fontSize = 11.sp,
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
                                    Text(cat, color = Gold, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Center Playback Controls (Prev - Play/Pause - Next)
                Row(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(bottom = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Previous Video
                    IconButton(
                        onClick = { playPrevVideo() },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.4f))
                            .border(1.dp, Gold.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "السابق",
                            tint = Gold,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Main Play / Pause Button
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(listOf(Gold, Color(0xFFC5A028)))
                            )
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

                    // Next Video
                    IconButton(
                        onClick = { playNextVideo() },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.4f))
                            .border(1.dp, Gold.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "التالي",
                            tint = Gold,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Bottom Control Bar: Seek Slider & Arabic Time Labels
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
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
                }
            }
        }

        // ENDED Custom Overlay ("التالي" Next Video Overlay)
        if (isEndedOverlayVisible) {
            val nextVideo = KidsVideoStore.getNextVideo(context, currentVideoId)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.92f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Gold),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.88f)
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "انتهى الفيديو",
                            color = Gold,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        if (nextVideo != null) {
                            Text(
                                text = "التالي: ${nextVideo.title}",
                                color = Color.White,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = { playNextVideo() },
                                colors = ButtonDefaults.buttonColors(containerColor = Gold),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.SkipNext, contentDescription = null, tint = Color(0xFF0B1120))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("تشغيل الفيديو التالي", color = Color(0xFF0B1120), fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Text(
                                text = "وصلت لنهاية القائمة",
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = {
                            isEndedOverlayVisible = false
                            seekTo(0f)
                            webViewInstance?.evaluateJavascript("playVideo();", null)
                        }) {
                            Text("إعادة تشغيل الفيديو الحالي", color = GoldSoft)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Builds custom HTML with YouTube IFrame Player API.
 * Disables YouTube controls, keyboard, related suggestions, and enforces inline playback.
 */
private fun buildYouTubeHtml(videoId: String): String {
    return """
        <!DOCTYPE html>
        <html>
        <head>
          <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
          <style>
            * { -webkit-tap-highlight-color: transparent; }
            html, body {
              margin: 0;
              padding: 0;
              width: 100%;
              height: 100%;
              background-color: #000000;
              overflow: hidden;
            }
            #player-container {
              width: 100%;
              height: 100%;
              position: absolute;
              top: 0;
              left: 0;
            }
            iframe {
              width: 100%;
              height: 100%;
              border: none;
              pointer-events: none; /* Block touch interactions inside YouTube iframe */
            }
          </style>
        </head>
        <body>
          <div id="player-container">
            <div id="player"></div>
          </div>
          <script>
            var tag = document.createElement('script');
            tag.src = "https://www.youtube.com/iframe_api";
            var firstScriptTag = document.getElementsByTagName('script')[0];
            firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);

            var player;
            function onYouTubeIframeAPIReady() {
              player = new YT.Player('player', {
                videoId: '$videoId',
                playerVars: {
                  'controls': 0,
                  'rel': 0,
                  'playsinline': 1,
                  'iv_load_policy': 3,
                  'fs': 0,
                  'disablekb': 1,
                  'enablejsapi': 1,
                  'autoplay': 1,
                  'modestbranding': 1
                },
                events: {
                  'onReady': onPlayerReady,
                  'onStateChange': onPlayerStateChange
                }
              });
            }

            function onPlayerReady(event) {
              event.target.playVideo();
              startProgressTimer();
            }

            function onPlayerStateChange(event) {
              if (window.AndroidBridge && window.AndroidBridge.sendStateChange) {
                window.AndroidBridge.sendStateChange(event.data);
              }
              // YT.PlayerState.ENDED is 0
              if (event.data === 0) {
                if (player && player.seekTo) {
                  player.seekTo(0, true);
                  player.pauseVideo();
                }
                if (window.AndroidBridge && window.AndroidBridge.sendVideoEnded) {
                  window.AndroidBridge.sendVideoEnded();
                }
              }
            }

            function startProgressTimer() {
              setInterval(function() {
                if (player && player.getCurrentTime && window.AndroidBridge && window.AndroidBridge.sendProgress) {
                  var cur = player.getCurrentTime();
                  var dur = player.getDuration();
                  var isPlaying = (player.getPlayerState() === 1);
                  window.AndroidBridge.sendProgress(cur, dur, isPlaying);
                }
              }, 1000);
            }

            function playVideo() { if (player && player.playVideo) player.playVideo(); }
            function pauseVideo() { if (player && player.pauseVideo) player.pauseVideo(); }
            function seekTo(sec) { if (player && player.seekTo) player.seekTo(sec, true); }
          </script>
        </body>
        </html>
    """.trimIndent()
}
