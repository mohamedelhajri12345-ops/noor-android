package com.elhajri.noor.audio.player

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * نقل حرفي 1:1 لـ src/lib/audioManager.js من الكود المصدري:
 * مشغّل واحد (MediaPlayer) يخدم المصحف والأناشيد معاً، بصف واحد،
 * مع خلط/تكرار (none|all|one)/سرعة/مؤقت نوم/روابط بديلة عند فشل السيرفر.
 * لا ExoPlayer ولا MediaSession — نفس سلوك الويب حرفياً وبثبات تام.
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
    val sleepTimerActive: Boolean = false,
    val lastError: String? = null
)

interface PlayerFacade {
    val state: StateFlow<PlayerState>
    fun ensure(context: Context)
    fun ensurePlayer(context: Context) = ensure(context)
    fun playQueue(tracks: List<PlayerTrack>, startIndex: Int = 0)
    fun play(track: PlayerTrack)
    fun toggle()
    fun stop()
    fun next()
    fun prev()
    fun seek(seconds: Float)
    fun setRate(rate: Float)
    fun toggleShuffle()
    fun toggleRepeat()
    fun setSleepTimer(minutes: Int)
    fun clearSleepTimer()
}

object PlayerCore : PlayerFacade {

    private val handler = Handler(Looper.getMainLooper())
    private var appContext: Context? = null
    private var player: MediaPlayer? = null

    private var queue: List<PlayerTrack> = emptyList()
    private var currentIndex = -1
    private var fallbackIndex = 0
    private var shuffle = false
    private var repeatMode = "none"
    private var rate = 1f
    private var isBuffering = false
    private var isLoading = false
    private var sleepUntilEnd = false
    private var sleepRunnable: Runnable? = null
    private var gapRunnable: Runnable? = null
    private var tickRunnable: Runnable? = null

    private val _state = MutableStateFlow(PlayerState())
    override val state: StateFlow<PlayerState> = _state.asStateFlow()

    override fun ensure(context: Context) {
        if (appContext == null) appContext = context.applicationContext
    }

    // ==================== playQueue (الويب: playQueue) ====================
    override fun playQueue(tracks: List<PlayerTrack>, startIndex: Int) {
        if (tracks.isEmpty()) return
        queue = tracks
        currentIndex = startIndex.coerceIn(0, tracks.size - 1)
        playCurrent()
    }

    // ==================== play (الويب: play — نفس المعرف = تبديل) ====================
    override fun play(track: PlayerTrack) {
        if (state.value.currentId == track.id) {
            toggle()
            return
        }
        queue = listOf(track)
        currentIndex = 0
        playCurrent()
    }

    // ==================== _playCurrent (الويب) ====================
    private fun playCurrent() {
        val track = queue.getOrNull(currentIndex) ?: return
        val ctx = appContext ?: return
        gapRunnable?.let { handler.removeCallbacks(it) }; gapRunnable = null

        isLoading = true
        isBuffering = true
        fallbackIndex = 0
        publish()
        try {
            val mp = player ?: MediaPlayer().also { created -> wire(created); player = created }
            mp.reset()
            mp.setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            mp.setDataSource(track.url)
            mp.prepareAsync()
            PlaybackService.start(ctx)
            startTicking()
        } catch (_: Exception) {
            // رابط ميت أو تهيئة فاشلة → جرب الروابط البديلة كما في الويب
            if (!retryWithFallback()) {
                isLoading = false
                isBuffering = false
                publish(error = "تعذّر تشغيل الملف")
            }
        }
    }

    /** ربط أحداث MediaPlayer — مقابل مستمعات audio.addEventListener في الويب */
    private fun wire(mp: MediaPlayer) {
        mp.setOnPreparedListener { p ->
            // مقابل حدث 'playing' في الويب: ابدأ التشغيل وأوقف حالة التحميل
            isBuffering = false
            isLoading = false
            try {
                if (Build.VERSION.SDK_INT >= 23) {
                    p.playbackParams = p.playbackParams.setSpeed(rate)
                }
                p.start()
            } catch (_: Exception) {}
            publish()
        }
        mp.setOnCompletionListener {
            // مقابل حدث 'ended' → _onTrackEnd
            onTrackEnd()
        }
        mp.setOnErrorListener { p, _, _ ->
            try { p.reset() } catch (_: Exception) {}
            if (!retryWithFallback()) {
                isLoading = false
                isBuffering = false
                publish(error = "تعذّر تشغيل الملف")
            }
            true
        }
        mp.setOnBufferingUpdateListener { _, percent ->
            // مقابل 'waiting'/'stalled' في الويب
            isBuffering = percent < 95
            publish()
        }
    }

    // ==================== _retryWithFallback (الويب) ====================
    private fun retryWithFallback(): Boolean {
        val track = queue.getOrNull(currentIndex) ?: return false
        if (fallbackIndex >= track.fallbackUrls.size) return false
        val url = track.fallbackUrls[fallbackIndex]
        fallbackIndex++
        isBuffering = true
        publish()
        return try {
            val mp = player ?: return false
            mp.reset()
            mp.setDataSource(url)
            mp.prepareAsync()
            true
        } catch (_: Exception) {
            false
        }
    }

    // ==================== _onTrackEnd (الويب) ====================
    private fun onTrackEnd() {
        if (sleepUntilEnd) {
            sleepUntilEnd = false
            stopAudio()
            publish()
            return
        }
        if (repeatMode == "one") {
            playCurrent()
            return
        }
        // نفس فجوة الـ 300ms في الويب بين المقاطع
        gapRunnable = Runnable { next() }.also { handler.postDelayed(it, 300L) }
    }

    // ==================== next (الويب) ====================
    override fun next() {
        if (queue.isEmpty()) return
        if (shuffle) {
            var idx = currentIndex
            while (idx == currentIndex && queue.size > 1) idx = (0 until queue.size).random()
            currentIndex = idx
        } else {
            currentIndex++
            if (currentIndex >= queue.size) {
                if (repeatMode == "all") currentIndex = 0
                else {
                    stopAudio()
                    publish(clearTrack = true)
                    return
                }
            }
        }
        playCurrent()
    }

    // ==================== prev (الويب: >3 ثوان → أعد المقطع) ====================
    override fun prev() {
        if (queue.isEmpty()) return
        val pos = try { (player?.currentPosition ?: 0) / 1000f } catch (_: Exception) { 0f }
        if (pos > 3f) {
            seek(0f)
            return
        }
        currentIndex--
        if (currentIndex < 0) currentIndex = if (repeatMode == "all") queue.size - 1 else 0
        playCurrent()
    }

    // ==================== toggle (الويب) ====================
    override fun toggle() {
        val mp = player ?: return
        if (state.value.currentId == null) return
        try {
            if (mp.isPlaying) mp.pause() else {
                if (Build.VERSION.SDK_INT >= 23) {
                    mp.playbackParams = mp.playbackParams.setSpeed(rate)
                }
                mp.start()
            }
        } catch (_: Exception) {}
        publish()
    }

    // ==================== stop / _stopAudio (الويب) ====================
    override fun stop() {
        stopAudio()
        clearSleepTimer()
        queue = emptyList()
        currentIndex = -1
        publish(clearTrack = true)
        appContext?.let { PlaybackService.stop(it) }
    }

    private fun stopAudio() {
        gapRunnable?.let { handler.removeCallbacks(it) }; gapRunnable = null
        try {
            player?.let { if (it.isPlaying) it.stop() }
        } catch (_: Exception) {}
        isBuffering = false
        isLoading = false
    }

    // ==================== seek (الويب: داخل مدة الملف) ====================
    override fun seek(seconds: Float) {
        val mp = player ?: return
        try {
            val durMs = mp.duration
            if (durMs > 0) mp.seekTo((seconds.coerceIn(0f, durMs / 1000f) * 1000).toInt())
        } catch (_: Exception) {}
        publish()
    }

    // ==================== setRate (الويب: playbackRate) ====================
    override fun setRate(newRate: Float) {
        rate = newRate
        try {
            val mp = player
            if (mp != null && Build.VERSION.SDK_INT >= 23 && mp.isPlaying) {
                mp.playbackParams = mp.playbackParams.setSpeed(rate)
            }
        } catch (_: Exception) {}
        publish()
    }

    override fun toggleShuffle() {
        shuffle = !shuffle
        publish()
    }

    // all → one → none (نفس دورة الويب: none → all → one)
    override fun toggleRepeat() {
        repeatMode = when (repeatMode) {
            "none" -> "all"; "all" -> "one"; else -> "none"
        }
        publish()
    }

    // ==================== مؤقت النوم (الويب: -1 = حتى نهاية المقطع) ====================
    override fun setSleepTimer(minutes: Int) {
        clearSleepTimer()
        if (minutes == -1) {
            sleepUntilEnd = true
            publish()
            return
        }
        if (minutes > 0) {
            sleepRunnable = Runnable {
                stopAudio()
                sleepRunnable = null
                publish(clearTrack = true)
                appContext?.let { PlaybackService.stop(it) }
            }.also { handler.postDelayed(it, minutes * 60_000L) }
            publish()
        }
    }

    override fun clearSleepTimer() {
        sleepRunnable?.let { handler.removeCallbacks(it) }; sleepRunnable = null
        sleepUntilEnd = false
    }

    // ==================== التحديث الدوري (الويب: timeupdate كل إطار) ====================
    private fun startTicking() {
        tickRunnable?.let { handler.removeCallbacks(it) }
        tickRunnable = object : Runnable {
            override fun run() {
                val playing = try { player?.isPlaying == true } catch (_: Exception) { false }
                publish()
                if (playing) handler.postDelayed(this, 300L)
            }
        }.also { handler.postDelayed(it, 100L) }
    }

    private fun publish(clearTrack: Boolean = false, error: String? = null) {
        val track = if (clearTrack) null else queue.getOrNull(currentIndex)
        val mp = player
        val playing = try { mp?.isPlaying == true && mp.isPlaying } catch (_: Exception) { false }
        val cur = try { (mp?.currentPosition ?: 0) / 1000f } catch (_: Exception) { 0f }
        val dur = try { (mp?.duration ?: 0).let { if (it > 0) it / 1000f else 0f } } catch (_: Exception) { 0f }
        _state.value = PlayerState(
            isPlaying = playing,
            currentId = if (clearTrack) null else track?.id ?: _state.value.currentId,
            currentTime = cur,
            duration = dur,
            title = track?.title ?: "",
            artist = track?.artist ?: "",
            queueLength = queue.size,
            shuffle = shuffle,
            repeatMode = repeatMode,
            rate = rate,
            isBuffering = isBuffering,
            isLoading = isLoading,
            sleepTimerActive = sleepRunnable != null || sleepUntilEnd,
            lastError = error ?: _state.value.lastError
        )
    }
}

/**
 * خدمة أمامية بسيطة تحافظ على التشغيل والصوت في شريط الإشعارات —
 * بدون أي مكتبة media3 (مصدر الانهيار السابق). تشبه سلوك الويب:
 * تُطلق عند بدء التشغيل وتتوقف عند الإيقاف.
 */
class PlaybackService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val s = PlayerCore.state.value
        startForeground(NOTIF_ID, buildNotification(this, s.title, s.artist))
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        try { PlayerCore.stop() } catch (_: Exception) {}
        super.onDestroy()
    }

    companion object {
        private const val NOTIF_ID = 3001
        private const val CHANNEL = "noor_playback"

        fun start(context: Context) {
            val ctx = context.applicationContext
            try {
                if (Build.VERSION.SDK_INT >= 26) {
                    val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    nm.createNotificationChannel(
                        NotificationChannel(CHANNEL, "تشغيل التلاوات والأناشيد", NotificationManager.IMPORTANCE_LOW)
                    )
                }
                ContextCompat.startForegroundService(ctx, Intent(ctx, PlaybackService::class.java))
            } catch (_: Exception) {
                // حتى لو تعذّرت الخدمة يستمر التشغيل داخل التطبيق
            }
        }

        fun stop(context: Context) {
            try { context.applicationContext.stopService(Intent(context, PlaybackService::class.java)) } catch (_: Exception) {}
        }

        private fun buildNotification(context: Context, title: String, artist: String): Notification {
            val pi = android.app.PendingIntent.getActivity(
                context, 0,
                context.packageManager.getLaunchIntentForPackage(context.packageName),
                android.app.PendingIntent.FLAG_IMMUTABLE
            )
            return NotificationCompat.Builder(context, CHANNEL)
                .setContentTitle(title.ifBlank { "القرآن الكريم" })
                .setContentText(artist.ifBlank { "تطبيق القرآن الكريم" })
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setContentIntent(pi)
                .setOngoing(true)
                .build()
        }
    }
}

/**
 * نفس الواجهة التي تستعملها الشاشات — كلا المديرين يصبان في مشغّل واحد
 * كما في الويب (audioManager واحد للمصحف والأناشيد معاً).
 */
object QuranPlayerManager : PlayerFacade by PlayerCore
object NasheedPlayerManager : PlayerFacade by PlayerCore
