package com.elhajri.noor.web

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.elhajri.noor.R
import com.elhajri.noor.audio.AudioSessionManager
import com.elhajri.noor.audio.AudioSource
import java.lang.ref.WeakReference

/**
 * WebPlayerBus — وسيط حالة مشغّل الويب.
 */
object WebPlayerBus {
    @Volatile var isPlaying: Boolean = false
    @Volatile var url: String? = null
    @Volatile var positionSec: Float = 0f
    @Volatile var title: String = "القرآن الكريم"

    @Volatile private var webRef: WeakReference<android.webkit.WebView>? = null

    fun bind(webView: android.webkit.WebView) { webRef = WeakReference(webView) }
    fun unbind(webView: android.webkit.WebView) {
        if (webRef?.get() === webView) webRef = null
    }

    fun injectJs(js: String) {
        val wv = webRef?.get() ?: return
        wv.post { try { wv.evaluateJavascript(js, null) } catch (_: Exception) {} }
    }
}

/**
 * WebPlayerService — خدمة أمامية لمشغّل الويب متكاملة مع AudioSessionManager.
 */
class WebPlayerService : Service() {

    companion object {
        const val ACTION_WEB_SYNC = "com.elhajri.noor.web.WEB_SYNC"
        const val ACTION_TAKEOVER = "com.elhajri.noor.web.TAKEOVER"
        const val ACTION_TOGGLE = "com.elhajri.noor.web.TOGGLE"
        const val ACTION_STOP = "com.elhajri.noor.web.STOP"

        fun sync(context: Context) {
            launch(context, ACTION_WEB_SYNC)
        }

        fun takeover(context: Context) {
            launch(context, ACTION_TAKEOVER)
        }

        fun stop(context: Context) {
            launch(context, ACTION_STOP)
        }

        private fun launch(context: Context, action: String) {
            val ctx = context.applicationContext
            try {
                AudioSessionManager.ensureChannel(ctx)
                val i = Intent(ctx, WebPlayerService::class.java).apply { this.action = action }
                androidx.core.content.ContextCompat.startForegroundService(ctx, i)
            } catch (_: Exception) {}
        }
    }

    private var mediaPlayer: MediaPlayer? = null
    private var nativeMode = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        AudioSessionManager.ensureChannel(this)
        val notifId = AudioSessionManager.MEDIA_NOTIFICATION_ID

        when (intent?.action) {
            ACTION_WEB_SYNC -> {
                if (WebPlayerBus.isPlaying) {
                    AudioSessionManager.onPlaybackStarted(this, AudioSource.WEB)
                }
                if (nativeMode) {
                    stopNativePlayback()
                    nativeMode = false
                }
                startForeground(notifId, buildNotification(WebPlayerBus.isPlaying))
            }
            ACTION_TAKEOVER -> {
                if (!nativeMode && WebPlayerBus.isPlaying && !WebPlayerBus.url.isNullOrBlank()) {
                    AudioSessionManager.onPlaybackStarted(this, AudioSource.WEB)
                    startNativePlayback()
                }
                if (mediaPlayer != null || !WebPlayerBus.isPlaying) {
                    startForeground(notifId, buildNotification(WebPlayerBus.isPlaying && nativeMode))
                }
            }
            ACTION_TOGGLE -> {
                if (nativeMode) {
                    val mp = mediaPlayer
                    if (mp != null) {
                        if (mp.isPlaying) {
                            mp.pause()
                            AudioSessionManager.onPlaybackStopped(this, AudioSource.WEB)
                        } else {
                            AudioSessionManager.onPlaybackStarted(this, AudioSource.WEB)
                            mp.start()
                        }
                        startForeground(notifId, buildNotification(mp.isPlaying))
                    }
                } else {
                    val js = if (WebPlayerBus.isPlaying)
                        "(function(){document.querySelectorAll('audio,video').forEach(function(a){try{a.pause()}catch(e){}})})()"
                    else
                        "(function(){document.querySelectorAll('audio,video').forEach(function(a){try{a.play()}catch(e){}})})()"
                    WebPlayerBus.injectJs(js)
                }
            }
            ACTION_STOP -> {
                startForeground(notifId, buildNotification(false))
                if (nativeMode) stopNativePlayback()
                else WebPlayerBus.injectJs(
                    "(function(){document.querySelectorAll('audio,video').forEach(function(a){try{a.pause()}catch(e){}})})()"
                )
                WebPlayerBus.isPlaying = false
                AudioSessionManager.onPlaybackStopped(this, AudioSource.WEB)
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun startNativePlayback() {
        try {
            val url = WebPlayerBus.url ?: return
            val mp = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(url)
                setOnPreparedListener { player ->
                    val targetMs = (WebPlayerBus.positionSec * 1000).toInt()
                    if (targetMs in 0..(player.duration.coerceAtLeast(0))) player.seekTo(targetMs)
                    player.start()
                    nativeMode = true
                    startForeground(AudioSessionManager.MEDIA_NOTIFICATION_ID, buildNotification(true))
                }
                setOnCompletionListener {
                    WebPlayerBus.isPlaying = false
                    AudioSessionManager.onPlaybackStopped(this@WebPlayerService, AudioSource.WEB)
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }
                setOnErrorListener { _, _, _ ->
                    AudioSessionManager.onPlaybackStopped(this@WebPlayerService, AudioSource.WEB)
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                    true
                }
                prepareAsync()
            }
            mediaPlayer = mp
        } catch (_: Exception) {
            stopSelf()
        }
    }

    private fun stopNativePlayback() {
        try { mediaPlayer?.stop() } catch (_: Exception) {}
        try { mediaPlayer?.release() } catch (_: Exception) {}
        mediaPlayer = null
    }

    override fun onDestroy() {
        stopNativePlayback()
        AudioSessionManager.onPlaybackStopped(this, AudioSource.WEB)
        super.onDestroy()
    }

    private fun buildNotification(playing: Boolean): Notification {
        val openApp = PendingIntent.getActivity(
            this, 0,
            packageManager.getLaunchIntentForPackage(packageName) ?: Intent(this, com.elhajri.noor.MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        fun action(intentAction: String, code: Int): PendingIntent {
            val i = Intent(this, WebPlayerService::class.java).apply { action = intentAction }
            return PendingIntent.getService(
                this, code, i,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        val largeIcon = try {
            BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)
        } catch (_: Exception) { null }

        return NotificationCompat.Builder(this, AudioSessionManager.MEDIA_CHANNEL_ID)
            .setColor(0xFF38BDF8.toInt())      // أزرق سماوي - Sky Blue Accent
            .setColorized(true)
            .setContentTitle(WebPlayerBus.title.ifBlank { "القرآن الكريم" })
            .setContentText(if (playing) "التلاوة تعمل الآن" else "متوقف مؤقتاً")
            .setSmallIcon(R.drawable.ic_notification)
            .setLargeIcon(largeIcon)
            .setContentIntent(openApp)
            .setOngoing(playing)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .addAction(
                if (playing) R.drawable.ic_notif_pause else R.drawable.ic_notif_play,
                if (playing) "إيقاف مؤقت" else "تشغيل",
                action(ACTION_TOGGLE, 11)
            )
            .addAction(
                R.drawable.ic_notif_close,
                "إغلاق",
                action(ACTION_STOP, 12)
            )
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(0, 1)
            )
            .build()
    }
}
