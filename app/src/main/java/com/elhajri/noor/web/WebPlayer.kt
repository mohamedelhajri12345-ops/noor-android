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
import java.lang.ref.WeakReference

/**
 * WebPlayerBus — وسيط حالة مشغّل الويب (قرآن/أناشيد داخل WebView الموقع).
 *
 * يستقبل أحداث الصوت من صفحة الموقع عبر جسر JS، ويحتفظ بأحدث حالة
 * (الرابط، الموضع الحالي، حالة التشغيل) لكي:
 *  1. تُعرض إشعارات MediaStyle احترافية أثناء التشغيل.
 *  2. تُستكمل التلاوة أصلياً عبر MediaPlayer عند إغلاق التطبيق.
 */
object WebPlayerBus {
    @Volatile var isPlaying: Boolean = false
    @Volatile var url: String? = null
    @Volatile var positionSec: Float = 0f
    @Volatile var title: String = "القرآن الكريم"

    // مرجع ضعيف للويب فيو — لا يمنع تحريره من الذاكرة
    @Volatile private var webRef: WeakReference<android.webkit.WebView>? = null

    fun bind(webView: android.webkit.WebView) { webRef = WeakReference(webView) }
    fun unbind(webView: android.webkit.WebView) {
        if (webRef?.get() === webView) webRef = null
    }

    /** تنفيذ أمر JS داخل الصفحة الحالية (إن كانت حية) */
    fun injectJs(js: String) {
        val wv = webRef?.get() ?: return
        wv.post { try { wv.evaluateJavascript(js, null) } catch (_: Exception) {} }
    }
}

/**
 * WebPlayerService — خدمة أمامية لمشغّل الويب.
 *
 * وضعان:
 *  A. وضع الويب (WEB): التلاوة تعمل داخل WebView (المستخدم داخل التطبيق).
 *     الخدمة تعرض إشعار MediaStyle أنيقاً بأيقونة التطبيق الكبيرة،
 *     وأزرارها ترسل الأوامر إلى صفحة الموقع مباشرة.
 *  B. الوضع الأصيل (NATIVE): عند إغلاق التطبيق (onDestroy) تلتقط الخدمة
 *     التلاوة من نفس الرابط ونفس الثانية عبر MediaPlayer — فيستمر الصوت
 *     في العمل حتى بعد إغلاق التطبيق، ويُدار من الإشعارات.
 */
class WebPlayerService : Service() {

    companion object {
        private const val CHANNEL_ID = "noor_web_playback"
        private const val NOTIF_ID = 4210
        const val ACTION_WEB_SYNC = "com.elhajri.noor.web.WEB_SYNC"
        const val ACTION_TAKEOVER = "com.elhajri.noor.web.TAKEOVER"
        const val ACTION_TOGGLE = "com.elhajri.noor.web.TOGGLE"
        const val ACTION_STOP = "com.elhajri.noor.web.STOP"

        fun sync(context: Context) {
            launch(context, ACTION_WEB_SYNC)
        }

        /** التقاط التلاوة أصلياً — يُستدعى لحظة إغلاق التطبيق أثناء تشغيل الصوت */
        fun takeover(context: Context) {
            launch(context, ACTION_TAKEOVER)
        }

        fun stop(context: Context) {
            launch(context, ACTION_STOP)
        }

        private fun launch(context: Context, action: String) {
            val ctx = context.applicationContext
            try {
                if (android.os.Build.VERSION.SDK_INT >= 26) {
                    val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                    nm.createNotificationChannel(
                        android.app.NotificationChannel(
                            CHANNEL_ID, "تشغيل التلاوة والأناشيد", android.app.NotificationManager.IMPORTANCE_LOW
                        )
                    )
                }
                val i = Intent(ctx, WebPlayerService::class.java).apply { this.action = action }
                androidx.core.content.ContextCompat.startForegroundService(ctx, i)
            } catch (_: Exception) {}
        }
    }

    private var mediaPlayer: MediaPlayer? = null
    private var nativeMode = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_WEB_SYNC -> {
                // الصوت يعمل الآن داخل الويب: أوقف أي تشغيل أصلي سابق (المستخدم عاد للتطبيق وشغّل من جديد)
                if (nativeMode) {
                    stopNativePlayback()
                    nativeMode = false
                }
                startForeground(NOTIF_ID, buildNotification(WebPlayerBus.isPlaying))
            }
            ACTION_TAKEOVER -> {
                // التطبيق أُغلق: استكمال أصيل من نفس الثانية
                if (!nativeMode && WebPlayerBus.isPlaying && !WebPlayerBus.url.isNullOrBlank()) {
                    startNativePlayback()
                }
                if (mediaPlayer != null || !WebPlayerBus.isPlaying) {
                    startForeground(NOTIF_ID, buildNotification(WebPlayerBus.isPlaying && nativeMode))
                }
            }
            ACTION_TOGGLE -> {
                if (nativeMode) {
                    val mp = mediaPlayer
                    if (mp != null) {
                        if (mp.isPlaying) mp.pause() else mp.start()
                        startForeground(NOTIF_ID, buildNotification(mp.isPlaying))
                    }
                } else {
                    // إيقاف/تشغيل عبر صفحة الموقع نفسها
                    val js = if (WebPlayerBus.isPlaying)
                        "(function(){document.querySelectorAll('audio,video').forEach(function(a){try{a.pause()}catch(e){}})})()"
                    else
                        "(function(){document.querySelectorAll('audio,video').forEach(function(a){try{a.play()}catch(e){}})})()"
                    WebPlayerBus.injectJs(js)
                }
            }
            ACTION_STOP -> {
                startForeground(NOTIF_ID, buildNotification(false)) // التزام شرط الخدمة الأمامية أولاً
                if (nativeMode) stopNativePlayback()
                else WebPlayerBus.injectJs(
                    "(function(){document.querySelectorAll('audio,video').forEach(function(a){try{a.pause()}catch(e){}})})()"
                )
                WebPlayerBus.isPlaying = false
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
                    startForeground(NOTIF_ID, buildNotification(true))
                }
                setOnCompletionListener {
                    WebPlayerBus.isPlaying = false
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }
                setOnErrorListener { _, _, _ ->
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
        super.onDestroy()
    }

    private fun buildNotification(playing: Boolean): Notification {
        val openApp = PendingIntent.getActivity(
            this, 0,
            packageManager.getLaunchIntentForPackage(packageName),
            PendingIntent.FLAG_IMMUTABLE
        )
        fun action(intentAction: String, code: Int): PendingIntent {
            val i = Intent(this, WebPlayerService::class.java).apply { action = intentAction }
            return PendingIntent.getService(
                this, code, i,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        // أيقونة التطبيق الرسمية عريضة في الإشعار — مظهر احترافي
        val largeIcon = try {
            BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)
        } catch (_: Exception) { null }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setColor(0xFFE9C46A.toInt())
            .setColorized(true)
            .setContentTitle(WebPlayerBus.title.ifBlank { "القرآن الكريم" })
            .setContentText(if (playing) "التلاوة تعمل الآن" else "متوقف مؤقتاً")
            .setSmallIcon(R.drawable.ic_notification)
            .setLargeIcon(largeIcon)
            .setContentIntent(openApp)
            .setOngoing(playing)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .addAction(
                if (playing) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play,
                if (playing) "إيقاف مؤقت" else "تشغيل",
                action(ACTION_TOGGLE, 11)
            )
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "إيقاف",
                action(ACTION_STOP, 12)
            )
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(0, 1)
            )
            .build()
    }
}
