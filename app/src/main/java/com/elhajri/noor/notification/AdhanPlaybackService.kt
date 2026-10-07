package com.elhajri.noor.notification

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.elhajri.noor.R
import com.elhajri.noor.audio.AudioSessionManager
import com.elhajri.noor.audio.AudioSource

/**
 * AdhanPlaybackService — تشغيل الأذان بأعلى جودة احترافية مع التكامل الكامل
 * مع مدير الجلسات الصوتية الموحد (AudioSessionManager).
 */
class AdhanPlaybackService : Service() {

    companion object {
        const val ACTION_PLAY = "com.elhajri.noor.adhan.PLAY"
        const val ACTION_STOP = "com.elhajri.noor.adhan.STOP"

        @Volatile
        var isPlaying: Boolean = false
            private set

        fun play(context: Context) {
            launch(context, ACTION_PLAY)
        }

        fun stop(context: Context) {
            launch(context, ACTION_STOP)
        }

        private fun launch(context: Context, action: String) {
            val ctx = context.applicationContext
            try {
                AudioSessionManager.ensureChannel(ctx)
                val i = Intent(ctx, AdhanPlaybackService::class.java).apply { this.action = action }
                ContextCompat.startForegroundService(ctx, i)
            } catch (_: Exception) {}
        }
    }

    private var mediaPlayer: MediaPlayer? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        AudioSessionManager.ensureChannel(this)
        val notifId = AudioSessionManager.MEDIA_NOTIFICATION_ID

        when (intent?.action) {
            ACTION_PLAY -> {
                AudioSessionManager.onPlaybackStarted(this, AudioSource.ADHAN)
                releasePlayer()
                try {
                    val mp = MediaPlayer().apply {
                        setAudioAttributes(
                            AudioAttributes.Builder()
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .build()
                        )
                        val afd = resources.openRawResourceFd(R.raw.adhan_makkah)
                        setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                        afd.close()
                        setOnPreparedListener { player ->
                            player.start()
                            AdhanPlaybackService.isPlaying = true
                            startForeground(notifId, buildNotification())
                        }
                        setOnCompletionListener {
                            AdhanPlaybackService.isPlaying = false
                            AudioSessionManager.onPlaybackStopped(this@AdhanPlaybackService, AudioSource.ADHAN)
                            stopForeground(STOP_FOREGROUND_REMOVE)
                            stopSelf()
                        }
                        setOnErrorListener { _, _, _ ->
                            AdhanPlaybackService.isPlaying = false
                            AudioSessionManager.onPlaybackStopped(this@AdhanPlaybackService, AudioSource.ADHAN)
                            stopForeground(STOP_FOREGROUND_REMOVE)
                            stopSelf()
                            true
                        }
                        prepareAsync()
                    }
                    mediaPlayer = mp
                } catch (_: Exception) {
                    AdhanPlaybackService.isPlaying = false
                    AudioSessionManager.onPlaybackStopped(this, AudioSource.ADHAN)
                    stopSelf()
                }
                startForeground(notifId, buildNotification())
            }
            ACTION_STOP -> {
                startForeground(notifId, buildNotification())
                releasePlayer()
                isPlaying = false
                AudioSessionManager.onPlaybackStopped(this, AudioSource.ADHAN)
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun releasePlayer() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) it.stop()
                it.release()
            }
        } catch (_: Exception) {}
        mediaPlayer = null
    }

    override fun onDestroy() {
        releasePlayer()
        isPlaying = false
        AudioSessionManager.onPlaybackStopped(this, AudioSource.ADHAN)
        super.onDestroy()
    }

    private fun buildNotification(): Notification {
        val openApp = PendingIntent.getActivity(
            this, 0,
            packageManager.getLaunchIntentForPackage(packageName)
                ?: Intent(this, com.elhajri.noor.MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        val stopIntent = Intent(this, AdhanPlaybackService::class.java).apply { action = ACTION_STOP }
        val stopPending = PendingIntent.getService(
            this, 1001, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val largeIcon = try {
            BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)
        } catch (_: Exception) { null }

        return NotificationCompat.Builder(this, AudioSessionManager.MEDIA_CHANNEL_ID)
            .setColor(0xFFE9C46A.toInt())      // ذهبي دافئ - Gold Accent (تعليمة ثابتة)
            .setColorized(true)
            .setContentTitle("الأذان الشريف")
            .setContentText("أذان مكة المكرمة — يعمل الآن")
            .setSmallIcon(R.drawable.ic_notification)
            .setLargeIcon(largeIcon)
            .setContentIntent(openApp)
            .setOngoing(isPlaying)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .addAction(R.drawable.ic_notif_close, "إيقاف", stopPending)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(0)
            )
            .build()
    }
}
