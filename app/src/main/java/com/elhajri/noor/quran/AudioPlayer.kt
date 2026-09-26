package com.elhajri.noor.quran

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SurahAudioPlayer {
    private var mediaPlayer: MediaPlayer? = null
    private val handler = Handler(Looper.getMainLooper())

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    var onAutoAdvance: (() -> Unit)? = null
    var onCompletion: (() -> Unit)? = null

    private var currentUrlsList: List<String> = emptyList()
    private var currentUrlIndex = 0

    private val progressRunnable = object : Runnable {
        override fun run() {
            mediaPlayer?.let { mp ->
                try {
                    if (mp.isPlaying) {
                        val pos = mp.currentPosition.toLong()
                        val dur = mp.duration.toLong()
                        _currentPositionMs.value = pos
                        _durationMs.value = if (dur > 0) dur else 0L
                        _progress.value = if (dur > 0) pos.toFloat() / dur.toFloat() else 0f
                        handler.postDelayed(this, 300)
                    }
                } catch (e: Exception) {
                    // Ignore transient state errors
                }
            }
        }
    }

    fun prepare(url: String, fallbackUrls: List<String> = emptyList()) {
        currentUrlsList = listOf(url) + fallbackUrls
        currentUrlIndex = 0
        loadAndPlayCurrentIndex()
    }

    private fun loadAndPlayCurrentIndex() {
        if (currentUrlIndex >= currentUrlsList.size) {
            _isLoading.value = false
            _isPlaying.value = false
            stopProgressPolling()
            return
        }

        stop()
        _isLoading.value = true

        try {
            val mp = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(currentUrlsList[currentUrlIndex])
                setOnPreparedListener { player ->
                    _isLoading.value = false
                    player.start()
                    _isPlaying.value = true
                    _durationMs.value = player.duration.toLong()
                    startProgressPolling()
                }
                setOnCompletionListener {
                    _isPlaying.value = false
                    stopProgressPolling()
                    _progress.value = 1f
                    onCompletion?.invoke()
                    onAutoAdvance?.invoke()
                }
                setOnErrorListener { _, _, _ ->
                    currentUrlIndex++
                    loadAndPlayCurrentIndex()
                    true
                }
            }
            mediaPlayer = mp
            mp.prepareAsync()
        } catch (e: Exception) {
            currentUrlIndex++
            loadAndPlayCurrentIndex()
        }
    }

    fun play() {
        mediaPlayer?.let { mp ->
            try {
                if (!mp.isPlaying) {
                    mp.start()
                    _isPlaying.value = true
                    startProgressPolling()
                }
            } catch (e: Exception) {}
        }
    }

    fun pause() {
        mediaPlayer?.let { mp ->
            try {
                if (mp.isPlaying) {
                    mp.pause()
                    _isPlaying.value = false
                    stopProgressPolling()
                }
            } catch (e: Exception) {}
        }
    }

    fun toggle() {
        if (_isPlaying.value) pause() else play()
    }

    fun seekTo(fraction: Float) {
        mediaPlayer?.let { mp ->
            try {
                val dur = mp.duration
                if (dur > 0) {
                    val newPos = (dur * fraction).toInt()
                    mp.seekTo(newPos)
                    _currentPositionMs.value = newPos.toLong()
                    _progress.value = fraction
                }
            } catch (e: Exception) {}
        }
    }

    fun stop() {
        stopProgressPolling()
        mediaPlayer?.let { mp ->
            try {
                if (mp.isPlaying) {
                    mp.stop()
                }
                mp.release()
            } catch (e: Exception) {}
        }
        mediaPlayer = null
        _isPlaying.value = false
        _isLoading.value = false
        _progress.value = 0f
        _currentPositionMs.value = 0L
    }

    fun release() {
        stop()
    }

    private fun startProgressPolling() {
        stopProgressPolling()
        handler.post(progressRunnable)
    }

    private fun stopProgressPolling() {
        handler.removeCallbacks(progressRunnable)
    }
}
