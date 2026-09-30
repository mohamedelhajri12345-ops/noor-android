package com.elhajri.noor.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.IBinder
import androidx.core.content.ContextCompat
import androidx.core.app.NotificationCompat
import com.elhajri.noor.R

/**
 * AdhanPlaybackService — تشغيل الأذان بأعلى جودة احترافية.
 *
 * 1. الأذان ملف صوتي محلي داخل التطبيق (res/raw/adhan_makkah.mp3):
 *    يعمل فوراً بلا انتظار تحميل من الإنترنت، ويعمل حتى بلا اتصال.
 * 2. إشعار MediaStyle أنيق أثناء الأذان: أيقونة التطبيق الرسمية عريضة،
 *    وزر إيقاف حقيقي يعمل من الإشعار نفسه.
 * 3. خدمة أمامية: الأذان لا ينقطع حتى لو خرج المستخدم من التطبيق.
 */
class AdhanPlaybackService : Service() {

    companion object {
        private const val CHANNEL_ID = "noor_adhan_playback"
        private const val NOTIF_ID = 4211
        const val ACTION_PLAY = "com.elhajri.noor.adhan.PLAY"
        const val ACTION_STOP = "com.elhajri.noor.adhan.STOP"

        @Volatile var isPlaying: Boolean = false

        fun play(context: Context) {
            launch(context, ACTION_PLAY)
        }

        fun stop(context: Context) {
            launch(context, ACTION_STOP)
        }

        private fun launch(context: Context, action: String) {
            val ctx = context.applicationContext
            try {
                if (android.os.Build.VERSION.SDK_INT >= 26) {
                    val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    nm.createNotificationChannel(
                        NotificationChannel(
                            CHANNEL_ID, "تشغيل الأذان", NotificationManager.IMPORTANCE_LOW
                        )
                    )
                }
                val i = Intent(ctx, AdhanPlaybackService::class.java).apply { this.action = action }
                ContextCompat.startForegroundService(ctx, i)
            } catch (_: Exception) {}
        }
    }

    private var mediaPlayer: MediaPlayer? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> {
                // إيقاف أي أذان سابق ثم تشغيل جديد من البداية — فوري ومحلي
                releasePlayer()
                try {
                    val mp = MediaPlayer().apply {
                        setAudioAttributes(
                            AudioAttributes.Builder()
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .build()
                        )
                        // الملف المحلي: تشغيل فوري بلا شبكة
                        val afd = resources.openRawResourceFd(R.raw.adhan_makkah)
                        setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                        afd.close()
                        setOnPreparedListener { player ->
                            player.start()
                            isPlaying = true
                            startForeground(NOTIF_ID, buildNotification())
                        }
                        setOnCompletionListener {
                            isPlaying = false
                            stopForeground(STOP_FOREGROUND_REMOVE)
                            stopSelf()
                        }
                        setOnErrorListener { _, _, _ ->
                            isPlaying = false
                            stopForeground(STOP_FOREGROUND_REMOVE)
                            stopSelf()
                            true
                        }
                        prepareAsync()
                    }
                    mediaPlayer = mp
                } catch (_: Exception) {
                    isPlaying = false
                    stopSelf()
                }
                // إن تأخر التجهيز: التزام شرط startForegroundService
                startForeground(NOTIF_ID, buildNotification())
            }
            ACTION_STOP -> {
                startForeground(NOTIF_ID, buildNotification()) // التزام شرط الخدمة الأمامية
                releasePlayer()
                isPlaying = false
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun releasePlayer() {
        try { mediaPlayer?.stop() } catch (_: Exception) {}
        try { mediaPlayer?.release() } catch (_: Exception) {}
        mediaPlayer = null
    }

    override fun onDestroy() {
        releasePlayer()
        isPlaying = false
        super.onDestroy()
    }

    private fun buildNotification(): Notification {
        val openApp = PendingIntent.getActivity(
            this, 0,
            packageManager.getLaunchIntentForPackage(packageName),
            PendingIntent.FLAG_IMMUTABLE
        )
        val stopIntent = Intent(this, AdhanPlaybackService::class.java).apply { action = ACTION_STOP }
        val stopPending = PendingIntent.getService(
            this, 21, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // أيقونة التطبيق الرسمية عريضة في الإشعار — مظهر احترافي
        val largeIcon = try {
            BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)
        } catch (_: Exception) { null }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("الأذان")
            .setContentText("أذان مكة المكرمة — يعمل الآن")
            .setSmallIcon(R.drawable.ic_notification)
            .setLargeIcon(largeIcon)
            .setContentIntent(openApp)
            .setOngoing(isPlaying)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "إيقاف", stopPending)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(0)
            )
            .build()
    }
}
