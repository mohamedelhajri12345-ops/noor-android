package com.elhajri.noor.audio.player

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.elhajri.noor.R
import com.elhajri.noor.audio.AudioSessionManager
import com.elhajri.noor.audio.AudioSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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

    private fun getAudioSource(): AudioSource =
        if (this === QuranPlayerManager) AudioSource.QURAN else AudioSource.NASHEED

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

        // Notify central AudioSessionManager to stop all competing audio sources
        AudioSessionManager.onPlaybackStarted(ctx, getAudioSource())
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
                    appContext?.let {
                        stopForegroundService(it)
                        AudioSessionManager.onPlaybackStopped(it, getAudioSource())
                    }
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
        val ctx = appContext
        try {
            if (mp.isPlaying) {
                mp.pause()
                if (ctx != null) AudioSessionManager.onPlaybackStopped(ctx, getAudioSource())
            } else {
                if (ctx != null) AudioSessionManager.onPlaybackStarted(ctx, getAudioSource())
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
        appContext?.let {
            stopForegroundService(it)
            AudioSessionManager.onPlaybackStopped(it, getAudioSource())
        }
    }

    private fun stopAudio() {
        tickRunnable?.let { handler.removeCallbacks(it) }; tickRunnable = null
        try { player?.stop(); player?.release() } catch (_: Exception) {}
        player = null
    }

    override fun seek(seconds: Float) {
        val mp = player ?: return
        try {
            mp.seekTo((seconds * 1000).toInt())
            publish()
        } catch (_: Exception) {}
    }

    override fun setRate(rate: Float) {
        this.rate = rate.coerceIn(0.5f, 2.0f)
        try {
            if (Build.VERSION.SDK_INT >= 23) {
                player?.let { if (it.isPlaying) it.playbackParams = it.playbackParams.setSpeed(this.rate) }
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
            "none" -> "all"
            "all" -> "one"
            else -> "none"
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
                appContext?.let {
                    stopForegroundService(it)
                    AudioSessionManager.onPlaybackStopped(it, getAudioSource())
                }
            }.also { handler.postDelayed(it, minutes * 60_000L) }
            publish()
        }
    }

    override fun clearSleepTimer() {
        sleepUntilEnd = false
        sleepRunnable?.let { handler.removeCallbacks(it) }
        sleepRunnable = null
        publish()
    }

    private fun startTicking() {
        tickRunnable?.let { handler.removeCallbacks(it) }
        tickRunnable = object : Runnable {
            override fun run() {
                if (player?.isPlaying == true) {
                    publish()
                    handler.postDelayed(this, 500L)
                }
            }
        }.also { handler.post(it) }
    }

    protected fun publish(clearTrack: Boolean = false, error: String? = null) {
        val track = if (clearTrack) null else queue.getOrNull(currentIndex)
        val isPlaying = player?.isPlaying == true
        val pos = try { (player?.currentPosition ?: 0) / 1000f } catch (_: Exception) { 0f }
        val dur = try { (player?.duration ?: 0) / 1000f } catch (_: Exception) { 0f }

        _state.value = PlayerState(
            isPlaying = isPlaying,
            currentId = track?.id,
            currentTime = pos,
            duration = dur,
            title = track?.title ?: if (isPlaying) defaultTitle else "",
            artist = track?.artist ?: "",
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

    fun isPlayingNow(): Boolean = try { player?.isPlaying == true } catch (_: Exception) { false }
}

/** مشغّل الأناشيد — محرك مستقل */
object PlayerCore : BasePlayerEngine("الأناشيد") {
    override fun startForegroundService(context: Context) = NasheedPlaybackService.start(context)
    override fun stopForegroundService(context: Context) = NasheedPlaybackService.stop(context)
}

/** مشغّل القرآن الكريم — محرك مستقل */
object QuranPlayerManager : BasePlayerEngine("القرآن الكريم") {
    override fun startForegroundService(context: Context) = QuranPlaybackService.start(context)
    override fun stopForegroundService(context: Context) = QuranPlaybackService.stop(context)
}

/** alias لسهولة الاستخدام */
object NasheedPlayerManager : PlayerFacade by PlayerCore

private object NotificationActions {
    const val ACTION_TOGGLE = "com.elhajri.noor.action.TOGGLE"
    const val ACTION_NEXT = "com.elhajri.noor.action.NEXT"
    const val ACTION_PREV = "com.elhajri.noor.action.PREV"
    const val ACTION_STOP = "com.elhajri.noor.action.STOP"
    const val EXTRA_TARGET = "target"
}

private fun buildPlaybackNotification(
    context: Context,
    channelId: String,
    notificationId: Int,
    engine: BasePlayerEngine,
    targetName: String,
    targetServiceClass: Class<*>,
    fallbackTitle: String
): Notification {
    val s = engine.state.value
    fun actionIntent(action: String): PendingIntent {
        val intent = Intent(context, targetServiceClass).apply {
            this.action = action
            putExtra(NotificationActions.EXTRA_TARGET, targetName)
        }
        return PendingIntent.getService(
            context,
            action.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    val openAppIntent = PendingIntent.getActivity(
        context, 0,
        context.packageManager.getLaunchIntentForPackage(context.packageName)
            ?: Intent(context, com.elhajri.noor.MainActivity::class.java),
        PendingIntent.FLAG_IMMUTABLE
    )
    val playPauseIcon = if (s.isPlaying) R.drawable.ic_notif_pause else R.drawable.ic_notif_play
    val largeIcon = try {
        BitmapFactory.decodeResource(context.resources, com.elhajri.noor.R.mipmap.ic_launcher)
    } catch (_: Exception) { null }

    return NotificationCompat.Builder(context, channelId)
        .setContentTitle(s.title.ifBlank { fallbackTitle })
        .setContentText(s.artist.ifBlank { "تطبيق نور - القرآن الكريم" })
        .setSmallIcon(R.drawable.ic_notification)
        .setLargeIcon(largeIcon)
        .setContentIntent(openAppIntent)
        .setOngoing(s.isPlaying)
        .setOnlyAlertOnce(true)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
        .setColor(0xFF38BDF8.toInt())      // أزرق سماوي - Sky Blue Accent
        .setColorized(true)
        .addAction(R.drawable.ic_notif_prev, "السابق", actionIntent(NotificationActions.ACTION_PREV))
        .addAction(playPauseIcon, if (s.isPlaying) "إيقاف مؤقت" else "تشغيل", actionIntent(NotificationActions.ACTION_TOGGLE))
        .addAction(R.drawable.ic_notif_next, "التالي", actionIntent(NotificationActions.ACTION_NEXT))
        .addAction(R.drawable.ic_notif_close, "إغلاق", actionIntent(NotificationActions.ACTION_STOP))
        .setStyle(
            androidx.media.app.NotificationCompat.MediaStyle()
                .setShowActionsInCompactView(0, 1, 2)
        )
        .build()
}

/** خدمة إشعار مستقلة لمشغّل القرآن */
class QuranPlaybackService : Service() {
    private var observeScope: CoroutineScope? = null
    private var lastStateKey = ""

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        AudioSessionManager.ensureChannel(this)
        val notifId = AudioSessionManager.MEDIA_NOTIFICATION_ID
        val channelId = AudioSessionManager.MEDIA_CHANNEL_ID

        when (intent?.action) {
            NotificationActions.ACTION_TOGGLE -> QuranPlayerManager.toggle()
            NotificationActions.ACTION_NEXT -> QuranPlayerManager.next()
            NotificationActions.ACTION_PREV -> QuranPlayerManager.prev()
            NotificationActions.ACTION_STOP -> {
                QuranPlayerManager.stop()
                AudioSessionManager.onPlaybackStopped(this, AudioSource.QURAN)
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
        }
        startForeground(notifId, buildPlaybackNotification(this, channelId, notifId, QuranPlayerManager, "quran", QuranPlaybackService::class.java, "القرآن الكريم"))
        if (observeScope == null) {
            observeScope = CoroutineScope(Dispatchers.Main + Job()).also { sc ->
                sc.launch {
                    QuranPlayerManager.state.collect { st ->
                        val key = "${st.isPlaying}|${st.currentId}|${st.title}"
                        if (key != lastStateKey) {
                            lastStateKey = key
                            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                            nm.notify(notifId, buildPlaybackNotification(this@QuranPlaybackService, channelId, notifId, QuranPlayerManager, "quran", QuranPlaybackService::class.java, "القرآن الكريم"))
                        }
                    }
                }
            }
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        observeScope?.cancel(); observeScope = null
        try { QuranPlayerManager.stop() } catch (_: Exception) {}
        AudioSessionManager.onPlaybackStopped(this, AudioSource.QURAN)
        super.onDestroy()
    }

    companion object {
        fun start(context: Context) {
            val ctx = context.applicationContext
            try {
                AudioSessionManager.ensureChannel(ctx)
                ContextCompat.startForegroundService(ctx, Intent(ctx, QuranPlaybackService::class.java))
            } catch (_: Exception) {}
        }

        fun stop(context: Context) {
            try { context.applicationContext.stopService(Intent(context, QuranPlaybackService::class.java)) } catch (_: Exception) {}
        }
    }
}

/** خدمة إشعار لمشغّل الأناشيد */
class NasheedPlaybackService : Service() {
    private var observeScope: CoroutineScope? = null
    private var lastStateKey = ""

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        AudioSessionManager.ensureChannel(this)
        val notifId = AudioSessionManager.MEDIA_NOTIFICATION_ID
        val channelId = AudioSessionManager.MEDIA_CHANNEL_ID

        when (intent?.action) {
            NotificationActions.ACTION_TOGGLE -> PlayerCore.toggle()
            NotificationActions.ACTION_NEXT -> PlayerCore.next()
            NotificationActions.ACTION_PREV -> PlayerCore.prev()
            NotificationActions.ACTION_STOP -> {
                PlayerCore.stop()
                AudioSessionManager.onPlaybackStopped(this, AudioSource.NASHEED)
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
        }
        startForeground(notifId, buildPlaybackNotification(this, channelId, notifId, PlayerCore, "nasheed", NasheedPlaybackService::class.java, "الأناشيد الإسلامية"))
        if (observeScope == null) {
            observeScope = CoroutineScope(Dispatchers.Main + Job()).also { sc ->
                sc.launch {
                    PlayerCore.state.collect { st ->
                        val key = "${st.isPlaying}|${st.currentId}|${st.title}"
                        if (key != lastStateKey) {
                            lastStateKey = key
                            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                            nm.notify(notifId, buildPlaybackNotification(this@NasheedPlaybackService, channelId, notifId, PlayerCore, "nasheed", NasheedPlaybackService::class.java, "الأناشيد الإسلامية"))
                        }
                    }
                }
            }
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        observeScope?.cancel(); observeScope = null
        try { PlayerCore.stop() } catch (_: Exception) {}
        AudioSessionManager.onPlaybackStopped(this, AudioSource.NASHEED)
        super.onDestroy()
    }

    companion object {
        fun start(context: Context) {
            val ctx = context.applicationContext
            try {
                AudioSessionManager.ensureChannel(ctx)
                ContextCompat.startForegroundService(ctx, Intent(ctx, NasheedPlaybackService::class.java))
            } catch (_: Exception) {}
        }

        fun stop(context: Context) {
            try { context.applicationContext.stopService(Intent(context, NasheedPlaybackService::class.java)) } catch (_: Exception) {}
        }
    }
}
