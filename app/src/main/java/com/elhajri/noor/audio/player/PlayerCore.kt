package com.elhajri.noor.audio.player

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.elhajri.noor.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * محرك تشغيل صوتي مستقل بالكامل (MediaPlayer خاص به + صف تشغيل خاص + إشعار خاص).
 * كل من "مشغّل القرآن" و "مشغّل الأناشيد" له نسخة مستقلة تماماً من هذا المحرك —
 * لا يتشاركان أي حالة، ولا أي MediaPlayer. عند تشغيل أحدهما يتوقف الآخر تلقائياً
 * (تجربة طبيعية كباقي التطبيقات الاحترافية، بلا تصادم صوت).
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

/** يبقي مشغلاً واحداً فقط يعزف صوتاً في نفس اللحظة — يوقف الآخر تلقائياً عند بدء أحدهما */
private object AudioFocusCoordinator {
    private val engines = mutableListOf<BasePlayerEngine>()
    fun register(e: BasePlayerEngine) { if (!engines.contains(e)) engines.add(e) }
    fun pauseOthers(current: BasePlayerEngine) {
        engines.forEach { if (it !== current) it.pauseIfPlaying() }
    }
}

/**
 * المحرك الأساسي: نقل حرفي لسلوك src/lib/audioManager.js (خلط/تكرار/سرعة/مؤقت نوم/روابط بديلة)
 * لكن كنسخة قابلة للتكرار — كل استدعاء "BasePlayerEngine()" ينشئ محركاً مستقلاً تماماً.
 */
abstract class BasePlayerEngine(private val defaultTitle: String) : PlayerFacade {

    init { AudioFocusCoordinator.register(this) }

    private val handler = Handler(Looper.getMainLooper())
    protected var appContext: Context? = null
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

    protected abstract fun startForegroundService(context: Context)
    protected abstract fun stopForegroundService(context: Context)

    override fun ensure(context: Context) {
        if (appContext == null) appContext = context.applicationContext
    }

    override fun playQueue(tracks: List<PlayerTrack>, startIndex: Int) {
        if (tracks.isEmpty()) return
        queue = tracks
        currentIndex = startIndex.coerceIn(0, tracks.size - 1)
        playCurrent()
    }

    override fun play(track: PlayerTrack) {
        if (state.value.currentId == track.id) {
            toggle()
            return
        }
        queue = listOf(track)
        currentIndex = 0
        playCurrent()
    }

    private fun playCurrent() {
        val track = queue.getOrNull(currentIndex) ?: return
        val ctx = appContext ?: return
        AudioFocusCoordinator.pauseOthers(this)
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
            startForegroundService(ctx)
        } catch (_: Exception) {
            if (!retryWithFallback()) {
                isLoading = false
                isBuffering = false
                publish(error = "تعذّر تشغيل الملف")
            }
        }
    }

    private fun wire(mp: MediaPlayer) {
        mp.setOnPreparedListener { p ->
            if (!isLoading && !isBuffering) return@setOnPreparedListener
            isBuffering = false
            isLoading = false
            try {
                if (Build.VERSION.SDK_INT >= 23) {
                    p.playbackParams = p.playbackParams.setSpeed(rate)
                }
                p.start()
                startTicking()
            } catch (_: Exception) {}
            publish()
        }
        mp.setOnCompletionListener { onTrackEnd() }
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
            isBuffering = percent < 95
            publish()
        }
    }

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
            mp.setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            mp.setDataSource(url)
            mp.prepareAsync()
            true
        } catch (_: Exception) {
            false
        }
    }

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
        gapRunnable = Runnable { next() }.also { handler.postDelayed(it, 300L) }
    }

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

    override fun toggle() {
        val mp = player ?: return
        if (state.value.currentId == null) return
        try {
            if (mp.isPlaying) mp.pause() else {
                if (Build.VERSION.SDK_INT >= 23) {
                    mp.playbackParams = mp.playbackParams.setSpeed(rate)
                }
                mp.start()
                startTicking()
                AudioFocusCoordinator.pauseOthers(this)
            }
        } catch (_: Exception) {}
        publish()
    }

    /** يستدعى من المنسّق فقط: أوقف مؤقتاً إن كان هذا المحرك يعزف الآن (لا يمسح الصف) */
    fun pauseIfPlaying() {
        try {
            player?.let { if (it.isPlaying) it.pause() }
        } catch (_: Exception) {}
        publish()
    }

    override fun stop() {
        stopAudio()
        clearSleepTimer()
        queue = emptyList()
        currentIndex = -1
        publish(clearTrack = true)
        appContext?.let { stopForegroundService(it) }
    }

    private fun stopAudio() {
        gapRunnable?.let { handler.removeCallbacks(it) }; gapRunnable = null
        try {
            player?.let { if (it.isPlaying) it.stop() }
        } catch (_: Exception) {}
        isBuffering = false
        isLoading = false
    }

    override fun seek(seconds: Float) {
        val mp = player ?: return
        try {
            val durMs = mp.duration
            if (durMs > 0) mp.seekTo((seconds.coerceIn(0f, durMs / 1000f) * 1000).toInt())
        } catch (_: Exception) {}
        publish()
    }

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

    override fun toggleRepeat() {
        repeatMode = when (repeatMode) {
            "none" -> "all"; "all" -> "one"; else -> "none"
        }
        publish()
    }

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
                appContext?.let { stopForegroundService(it) }
            }.also { handler.postDelayed(it, minutes * 60_000L) }
            publish()
        }
    }

    override fun clearSleepTimer() {
        sleepRunnable?.let { handler.removeCallbacks(it) }; sleepRunnable = null
        sleepUntilEnd = false
    }

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

    fun isPlayingNow(): Boolean = try { player?.isPlaying == true } catch (_: Exception) { false }

    private fun publish(clearTrack: Boolean = false, error: String? = null) {
        val track = if (clearTrack) null else queue.getOrNull(currentIndex)
        val mp = player
        val playing = try { mp?.isPlaying == true } catch (_: Exception) { false }
        val cur = try { (mp?.currentPosition ?: 0) / 1000f } catch (_: Exception) { 0f }
        val dur = try { (mp?.duration ?: 0).let { if (it > 0) it / 1000f else 0f } } catch (_: Exception) { 0f }
        _state.value = PlayerState(
            isPlaying = playing,
            currentId = if (clearTrack) null else track?.id ?: _state.value.currentId,
            currentTime = cur,
            duration = dur,
            title = track?.title ?: (if (clearTrack) "" else _state.value.title),
            artist = track?.artist ?: (if (clearTrack) "" else _state.value.artist),
            queueLength = queue.size,
            shuffle = shuffle,
            repeatMode = repeatMode,
            rate = rate,
            isBuffering = isBuffering,
            isLoading = isLoading,
            sleepTimerActive = sleepRunnable != null || sleepUntilEnd,
            lastError = error
        )
    }
}

/** مشغّل الأناشيد — محرك مستقل تماماً بذاكرته وإشعاره الخاص */
object PlayerCore : BasePlayerEngine("الأناشيد") {
    override fun startForegroundService(context: Context) = NasheedPlaybackService.start(context)
    override fun stopForegroundService(context: Context) = NasheedPlaybackService.stop(context)
}

/** مشغّل القرآن الكريم — محرك مستقل تماماً بذاكرته وإشعاره الخاص، منفصل كلياً عن مشغل الأناشيد */
object QuranPlayerManager : BasePlayerEngine("القرآن الكريم") {
    override fun startForegroundService(context: Context) = QuranPlaybackService.start(context)
    override fun stopForegroundService(context: Context) = QuranPlaybackService.stop(context)
}

object NasheedPlayerManager : PlayerFacade by PlayerCore

/** إشعار احترافي بأزرار حقيقية (سابق/تشغيل-إيقاف/تالي/إيقاف كامل) بأسلوب MediaStyle */
private object NotificationActions {
    const val ACTION_TOGGLE = "com.elhajri.noor.action.TOGGLE"
    const val ACTION_NEXT = "com.elhajri.noor.action.NEXT"
    const val ACTION_PREV = "com.elhajri.noor.action.PREV"
    const val ACTION_STOP = "com.elhajri.noor.action.STOP"
    const val EXTRA_TARGET = "target"
    const val TARGET_QURAN = "quran"
    const val TARGET_NASHEED = "nasheed"
}

private fun buildPlaybackNotification(
    context: Context,
    channelId: String,
    notifId: Int,
    engine: PlayerFacade,
    target: String,
    serviceClass: Class<*>,
    fallbackTitle: String
): Notification {
    val s = engine.state.value
    fun actionIntent(action: String): PendingIntent {
        val i = Intent(context, serviceClass).apply {
            this.action = action
            putExtra(NotificationActions.EXTRA_TARGET, target)
        }
        return PendingIntent.getService(
            context, action.hashCode(), i,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    val openAppIntent = PendingIntent.getActivity(
        context, 0,
        context.packageManager.getLaunchIntentForPackage(context.packageName),
        PendingIntent.FLAG_IMMUTABLE
    )
    val playPauseIcon = if (s.isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
    // أيقونة التطبيق الرسمية عريضة في الإشعار — مظهر احترافي موحد
    val largeIcon = try {
        android.graphics.BitmapFactory.decodeResource(context.resources, com.elhajri.noor.R.mipmap.ic_launcher)
    } catch (_: Exception) { null }
    val builder = NotificationCompat.Builder(context, channelId)
        .setContentTitle(s.title.ifBlank { fallbackTitle })
        .setContentText(s.artist.ifBlank { "تطبيق القرآن الكريم" })
        .setSmallIcon(R.drawable.ic_notification)
        .setLargeIcon(largeIcon)
        .setContentIntent(openAppIntent)
        .setOngoing(s.isPlaying)
        .setOnlyAlertOnce(true)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .addAction(android.R.drawable.ic_media_previous, "السابق", actionIntent(NotificationActions.ACTION_PREV))
        .addAction(playPauseIcon, if (s.isPlaying) "إيقاف" else "تشغيل", actionIntent(NotificationActions.ACTION_TOGGLE))
        .addAction(android.R.drawable.ic_media_next, "التالي", actionIntent(NotificationActions.ACTION_NEXT))
        .addAction(android.R.drawable.ic_menu_close_clear_cancel, "إغلاق", actionIntent(NotificationActions.ACTION_STOP))
        .setStyle(
            androidx.media.app.NotificationCompat.MediaStyle()
                .setShowActionsInCompactView(0, 1, 2)
        )
    return builder.build()
}

/** خدمة إشعار مستقلة لمشغّل القرآن فقط */
class QuranPlaybackService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            NotificationActions.ACTION_TOGGLE -> QuranPlayerManager.toggle()
            NotificationActions.ACTION_NEXT -> QuranPlayerManager.next()
            NotificationActions.ACTION_PREV -> QuranPlayerManager.prev()
            NotificationActions.ACTION_STOP -> { QuranPlayerManager.stop(); stopSelf(); return START_NOT_STICKY }
        }
        startForeground(NOTIF_ID, buildPlaybackNotification(this, CHANNEL, NOTIF_ID, QuranPlayerManager, "quran", QuranPlaybackService::class.java, "القرآن الكريم"))
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        try { QuranPlayerManager.stop() } catch (_: Exception) {}
        super.onDestroy()
    }

    companion object {
        private const val NOTIF_ID = 3002
        private const val CHANNEL = "noor_quran_playback"

        fun start(context: Context) {
            val ctx = context.applicationContext
            try {
                if (Build.VERSION.SDK_INT >= 26) {
                    val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    nm.createNotificationChannel(
                        NotificationChannel(CHANNEL, "تشغيل القرآن الكريم", NotificationManager.IMPORTANCE_LOW)
                    )
                }
                ContextCompat.startForegroundService(ctx, Intent(ctx, QuranPlaybackService::class.java))
            } catch (_: Exception) {}
        }

        fun stop(context: Context) {
            try { context.applicationContext.stopService(Intent(context, QuranPlaybackService::class.java)) } catch (_: Exception) {}
        }
    }
}

/** خدمة إشعار مستقلة لمشغّل الأناشيد فقط */
class NasheedPlaybackService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            NotificationActions.ACTION_TOGGLE -> PlayerCore.toggle()
            NotificationActions.ACTION_NEXT -> PlayerCore.next()
            NotificationActions.ACTION_PREV -> PlayerCore.prev()
            NotificationActions.ACTION_STOP -> { PlayerCore.stop(); stopSelf(); return START_NOT_STICKY }
        }
        startForeground(NOTIF_ID, buildPlaybackNotification(this, CHANNEL, NOTIF_ID, PlayerCore, "nasheed", NasheedPlaybackService::class.java, "الأناشيد الإسلامية"))
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        try { PlayerCore.stop() } catch (_: Exception) {}
        super.onDestroy()
    }

    companion object {
        private const val NOTIF_ID = 3001
        private const val CHANNEL = "noor_nasheed_playback"

        fun start(context: Context) {
            val ctx = context.applicationContext
            try {
                if (Build.VERSION.SDK_INT >= 26) {
                    val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    nm.createNotificationChannel(
                        NotificationChannel(CHANNEL, "تشغيل الأناشيد", NotificationManager.IMPORTANCE_LOW)
                    )
                }
                ContextCompat.startForegroundService(ctx, Intent(ctx, NasheedPlaybackService::class.java))
            } catch (_: Exception) {}
        }

        fun stop(context: Context) {
            try { context.applicationContext.stopService(Intent(context, NasheedPlaybackService::class.java)) } catch (_: Exception) {}
        }
    }
}
