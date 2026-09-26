package com.elhajri.noor.quran

import com.elhajri.noor.audio.NoorAudioController
import kotlinx.coroutines.flow.StateFlow

/**
 * Thin delegate over the shared NoorAudioController (Media3 + background service).
 * Keeps the exact API the reader screens already use, but playback now
 * survives minimizing the app and screen-off, with a real media notification.
 */
class SurahAudioPlayer {

    val isPlaying: StateFlow<Boolean> = NoorAudioController.isPlaying
    val isLoading: StateFlow<Boolean> = NoorAudioController.isLoading
    val progress: StateFlow<Float> = NoorAudioController.progress
    val currentPositionMs: StateFlow<Long> = NoorAudioController.positionMs
    val durationMs: StateFlow<Long> = NoorAudioController.durationMs

    var onAutoAdvance: (() -> Unit)? = null
        set(value) {
            field = value
            NoorAudioController.onAutoAdvance = value
        }

    var onCompletion: (() -> Unit)? = null
        set(value) {
            field = value
            NoorAudioController.onAutoAdvance = if (value != null) {
                { value.invoke() }
            } else null
        }

    fun prepare(url: String, fallbackUrls: List<String> = emptyList()) {
        val ctx = NoorAudioController.appContext ?: return
        NoorAudioController.play(ctx, url, "", fallbackUrls)
    }

    fun play() {
        if (!NoorAudioController.isPlaying.value) NoorAudioController.toggle()
    }

    fun pause() = NoorAudioController.pause()

    fun toggle() = NoorAudioController.toggle()

    fun seekTo(fraction: Float) = NoorAudioController.seekTo(fraction)

    fun stop() = NoorAudioController.stop()

    /** Intentionally kept: background playback continues after leaving the reader. */
    fun release() { /* no-op: the shared service keeps playing */ }
}
