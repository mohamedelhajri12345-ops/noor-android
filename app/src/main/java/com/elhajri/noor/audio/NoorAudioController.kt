package com.elhajri.noor.audio

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import android.os.Handler
import android.os.Looper

/**
 * Single shared, professional audio controller for the whole app
 * (Quran recitation + Library / nasheeds), built on Media3/ExoPlayer.
 * Backed by NoorPlaybackService, so playback survives backgrounding,
 * screen-off, and app minimizing, with a real media notification.
 */
@UnstableApi
object NoorAudioController {
    /** Application context set once at startup so player screens never need to pass it. */
    @Volatile var appContext: android.content.Context? = null

    private var controller: MediaController? = null
    private val handler = Handler(Looper.getMainLooper())

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress.asStateFlow()

    private val _currentTitle = MutableStateFlow("")
    val currentTitle: StateFlow<String> = _currentTitle.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _positionMs = MutableStateFlow(0L)
    val positionMs: StateFlow<Long> = _positionMs.asStateFlow()

    var onAutoAdvance: (() -> Unit)? = null

    private var fallbackUrls: List<String> = emptyList()
    private var fallbackIndex = 0
    private var fallbackTitle = ""

    fun ensureConnected(context: Context) {
        if (controller != null) return
        val token = SessionToken(context, ComponentName(context, NoorPlaybackService::class.java))
        val future = MediaController.Builder(context, token).buildAsync()
        future.addListener({
            controller = future.get()
            controller?.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(playing: Boolean) {
                    _isPlaying.value = playing
                }
                override fun onPlaybackStateChanged(state: Int) {
                    _isLoading.value = state == Player.STATE_BUFFERING
                    if (state == Player.STATE_ENDED) {
                        onAutoAdvance?.invoke()
                    }
                    if (state == Player.STATE_READY) {
                        _durationMs.value = controller?.duration?.coerceAtLeast(0) ?: 0L
                    }
                }
                override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                    playFallback()
                }
            })
            startProgressLoop()
        }, java.util.concurrent.Executor { it.run() })
    }

    private fun startProgressLoop() {
        handler.post(object : Runnable {
            override fun run() {
                val c = controller
                if (c != null) {
                    val dur = c.duration.coerceAtLeast(0)
                    val pos = c.currentPosition.coerceAtLeast(0)
                    _durationMs.value = dur
                    _positionMs.value = pos
                    _progress.value = if (dur > 0) (pos.toFloat() / dur.toFloat()).coerceIn(0f, 1f) else 0f
                }
                handler.postDelayed(this, 400)
            }
        })
    }

    fun play(context: Context, url: String, title: String, fallbacks: List<String> = emptyList()) {
        ensureConnected(context)
        fallbackUrls = fallbacks
        fallbackIndex = 0
        fallbackTitle = title
        _currentTitle.value = title
        _isLoading.value = true
        val runnable = Runnable {
            val c = controller ?: return@Runnable
            val item = MediaItem.Builder()
                .setUri(url)
                .setMediaMetadata(MediaMetadata.Builder().setTitle(title).build())
                .build()
            c.setMediaItem(item)
            c.prepare()
            c.playWhenReady = true
        }
        if (controller == null) handler.postDelayed(runnable, 250) else runnable.run()
    }

    private fun playFallback() {
        if (fallbackIndex < fallbackUrls.size) {
            val url = fallbackUrls[fallbackIndex]
            fallbackIndex++
            val c = controller ?: return
            val item = MediaItem.Builder()
                .setUri(url)
                .setMediaMetadata(MediaMetadata.Builder().setTitle(fallbackTitle).build())
                .build()
            c.setMediaItem(item)
            c.prepare()
            c.playWhenReady = true
        } else {
            _isLoading.value = false
            _isPlaying.value = false
        }
    }

    fun toggle() {
        val c = controller ?: return
        if (c.isPlaying) c.pause() else c.play()
    }

    fun pause() { controller?.pause() }

    fun seekTo(fraction: Float) {
        val c = controller ?: return
        val dur = c.duration.coerceAtLeast(0)
        if (dur > 0) c.seekTo((dur * fraction).toLong())
    }

    fun stop() {
        controller?.stop()
        _isPlaying.value = false
        _isLoading.value = false
        _progress.value = 0f
        _currentTitle.value = ""
    }
}
