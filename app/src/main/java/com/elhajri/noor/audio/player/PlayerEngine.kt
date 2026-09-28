package com.elhajri.noor.audio.player

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * One audio engine = ONE web audioManager.js port (src/lib/audioManager.js).
 * Queue lives natively inside ExoPlayer (lock-screen next/prev work out of
 * the box); repeat / shuffle / rate / sleep-timer / fallback servers are
 * ported faithfully from the web source. 100% native, zero WebView.
 */
data class PlayerTrack(
    val id: String,
    val url: String,
    val title: String = "",
    val artist: String = "",
    val fallbackUrls: List<String> = emptyList()
)

data class PlayerState(
    val isPlaying: Boolean = false,
    val currentId: String? = null,
    val currentTime: Float = 0f,
    val duration: Float = 0f,
    val title: String = "",
    val artist: String = "",
    val queueLength: Int = 0,
    val shuffle: Boolean = false,
    val repeatMode: String = "none", // none | all | one
    val rate: Float = 1f,
    val isBuffering: Boolean = false,
    val isLoading: Boolean = false,
    val sleepTimerActive: Boolean = false
)

class PlayerEngine(
    private val serviceClass: Class<*>
) {
    /**
     * درع حماية: أي استثناء داخل المشغّل يُبتلع بدل إسقاط التطبيق كله.
     * (المستخدم تأكد أن الخروج المفاجئ بسبب المشغل — لن يحدث مجدداً.)
     */
    private inline fun safe(block: () -> Unit) {
        try { block() } catch (_: Exception) {}
    }

    /** نسخة ترجع قيمة أو null عند الفشل — لا ينهار التطبيق أبداً */
    private inline fun <T : Any> safeOrNull(block: () -> T?): T? {
        return try { block() } catch (_: Exception) { null }
    }
    private val handler = Handler(Looper.getMainLooper())
    private var appContext: Context? = null
    private var player: ExoPlayer? = null
    private var queue: List<PlayerTrack> = emptyList()
    private var fallbackIndex = 0
    private var sleepRunnable: Runnable? = null
    private var sleepUntilEnd = false

    private val _state = MutableStateFlow(PlayerState())
    val state: StateFlow<PlayerState> = _state.asStateFlow()

    private val listener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_ENDED && repeatMode() != "one") {
                maybeSleepEnd()
                stopService()
            }
            publish()
        }
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            if (isPlaying) positionLoop()
            publish()
        }
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            fallbackIndex = 0
            sleepUntilEnd = false
            publish()
        }
        override fun onPlayerError(error: PlaybackException) {
            retryWithFallback()
        }
    }

    fun ensurePlayer(context: Context): ExoPlayer {
        appContext = context.applicationContext
        player?.let { return it }
        val p = ExoPlayer.Builder(context)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                /* handleAudioFocus = */ true
            )
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_NETWORK)
            .build()
        p.addListener(listener)
        player = p
        return p
    }

    /** Raw ExoPlayer for the MediaSessionService to attach to. */
    fun exposedPlayer(): ExoPlayer? = player

    /**
     * Safely tears down the player AND forgets the stale reference.
     * Before this fix the service released the shared ExoPlayer on onDestroy()
     * (e.g. when the user swiped the app away) but the manager kept pointing at
     * that now-released instance — the next play() call then threw on a released
     * player, which is exactly what caused the sudden app close and the "reciters
     * don't play" bug. Now ensurePlayer() always gets a fresh, valid instance.
     */
    fun releasePlayer() {
        player?.release()
        player = null
    }

    // ---------------------------------------------------------------- web API

    fun playQueue(tracks: List<PlayerTrack>, startIndex: Int = 0) {
        if (tracks.isEmpty()) return
        val ctx = appContext ?: return
        PlayerInterop.pauseOthers(this)
        val p = safeOrNull { ensurePlayer(ctx) } ?: return
        queue = tracks
        fallbackIndex = 0
        val items = tracks.map { t ->
            MediaItem.Builder()
                .setMediaId(t.id)
                .setUri(t.url)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(t.title)
                        .setArtist(t.artist)
                        .build()
                )
                .build()
        }
        p.setMediaItems(items, startIndex.coerceIn(0, items.size - 1), 0)
        p.prepare()
        p.play()
        startService(ctx)
        publish()
    }

    fun play(track: PlayerTrack) {
        if (state.value.currentId == track.id) {
            toggle()
            return
        }
        playQueue(listOf(track), 0)
    }

    fun toggle() {
        val p = player ?: return
        safe { if (p.isPlaying) p.pause() else p.play() }
        safe { publish() }
    }

    fun stop() {
        val p = player ?: return
        p.stop()
        p.clearMediaItems()
        queue = emptyList()
        clearSleepTimer(false)
        stopService()
        publish()
    }

    fun next() {
        val p = player ?: return
        safe { if (p.hasNextMediaItem()) p.seekToNextMediaItem() }
    }

    fun prev() {
        val p = player ?: return
        if (p.currentPosition > 3000) {
            p.seekTo(0) // like the web: restart current if we're past 3s
        } else if (p.hasPreviousMediaItem()) {
            p.seekToPreviousMediaItem()
        } else {
            p.seekTo(0)
        }
    }

    fun seek(seconds: Float) {
        val p = player ?: return
        safe {
            val durMs = p.duration
            if (durMs > 0) p.seekTo((seconds.coerceIn(0f, durMs / 1000f) * 1000).toLong())
        }
        safe { publish() }
    }

    fun setRate(rate: Float) {
        player?.setPlaybackSpeed(rate)
        publish()
    }

    fun toggleShuffle() {
        val p = player ?: return
        p.shuffleModeEnabled = !p.shuffleModeEnabled
        publish()
    }

    fun toggleRepeat() {
        val p = player ?: return
        p.repeatMode = when (p.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        publish()
    }

    /** minutes: 0 = off, -1 = until end of current track, >0 = countdown */
    fun setSleepTimer(minutes: Int) {
        clearSleepTimer(false)
        if (minutes == -1) {
            sleepUntilEnd = true
            publish()
            return
        }
        if (minutes > 0) {
            val r = Runnable {
                sleepRunnable = null
                val p = player
                if (p != null) {
                    p.pause()
                    stopService()
                }
                publish()
            }
            sleepRunnable = r
            handler.postDelayed(r, minutes * 60_000L)
            publish()
        }
    }

    fun clearSleepTimer(publishState: Boolean = true) {
        sleepRunnable?.let { handler.removeCallbacks(it) }
        sleepRunnable = null
        sleepUntilEnd = false
        if (publishState) publish()
    }

    // ------------------------------------------------------------- internals

    private fun repeatMode(): String = when (player?.repeatMode) {
        Player.REPEAT_MODE_ALL -> "all"
        Player.REPEAT_MODE_ONE -> "one"
        else -> "none"
    }

    private fun maybeSleepEnd() {
        if (sleepUntilEnd) {
            sleepUntilEnd = false
            player?.pause()
            stopService()
            publish()
        }
    }

    private fun retryWithFallback() {
        val p = player ?: return
        val idx = p.currentMediaItemIndex
        val track = queue.getOrNull(idx) ?: return
        if (fallbackIndex >= track.fallbackUrls.size) {
            if (p.hasNextMediaItem()) {
                fallbackIndex = 0
                p.seekToNextMediaItem()
            } else {
                p.pause()
                stopService()
            }
            publish()
            return
        }
        val url = track.fallbackUrls[fallbackIndex]
        fallbackIndex++
        val item = MediaItem.Builder()
            .setMediaId(track.id)
            .setUri(url)
            .setMediaMetadata(
                MediaMetadata.Builder().setTitle(track.title).setArtist(track.artist).build()
            )
            .build()
        p.replaceMediaItem(idx, item)
        p.prepare()
        p.play()
        publish()
    }

    private fun startService(context: Context) {
        safe {
            val intent = Intent(context, serviceClass)
            ContextCompat.startForegroundService(context, intent)
        }
    }

    private fun stopService() {
        val ctx = appContext ?: return
        ctx.stopService(Intent(ctx, serviceClass))
    }

    fun publish() {
        val p = player
        if (p == null) {
            _state.value = PlayerState()
            return
        }
        val durMs = if (p.duration > 0) p.duration else 0
        _state.value = PlayerState(
            isPlaying = p.isPlaying,
            currentId = p.currentMediaItem?.mediaId,
            currentTime = p.currentPosition / 1000f,
            duration = durMs / 1000f,
            title = p.mediaMetadata.title?.toString() ?: "",
            artist = p.mediaMetadata.artist?.toString() ?: "",
            queueLength = p.mediaItemCount,
            shuffle = p.shuffleModeEnabled,
            repeatMode = repeatMode(),
            rate = p.playbackParameters.speed,
            isBuffering = p.playbackState == Player.STATE_BUFFERING,
            isLoading = p.playbackState == Player.STATE_IDLE || p.playbackState == Player.STATE_BUFFERING,
            sleepTimerActive = sleepRunnable != null || sleepUntilEnd
        )
    }

    fun positionLoop() {
        val p = player ?: return
        safe {
            if (p.isPlaying) {
                publish()
                handler.postDelayed({ positionLoop() }, 300)
            }
        }
    }
}
