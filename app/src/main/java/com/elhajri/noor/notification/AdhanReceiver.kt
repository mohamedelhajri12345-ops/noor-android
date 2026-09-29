package com.elhajri.noor.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.elhajri.noor.MainActivity

/**
 * يُطلق عند موعد الصلاة: يشغّل الأذان *من ملفات صوتية مدمجة داخل التطبيق*
 * (لا يعتمد على الإنترنت إطلاقاً)، مع إشعار عالٍ الأولوية.
 * قبل هذا الإصلاح كان يبثّ الأذان من رابط خارجي محجوب (403) فلا يُسمع أي صوت.
 */
class AdhanReceiver : BroadcastReceiver() {

    companion object {
        @Volatile private var player: MediaPlayer? = null
        const val ACTION_STOP_ADHAN = "com.elhajri.noor.STOP_ADHAN"
    }

    override fun onReceive(context: Context, intent: Intent) {
        onReceiveGuard(context)
        if (intent.action == ACTION_STOP_ADHAN) {
            stopAdhan()
            return
        }

        Channels.ensure(context)
        val prayerName = intent.getStringExtra("prayer_name") ?: "الصلاة"
        val prayerTime = intent.getStringExtra("prayer_time") ?: ""

        playAdhan(context, prayerName)

        val activityIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            activityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(context, AdhanReceiver::class.java).apply {
            action = ACTION_STOP_ADHAN
        }
        val stopPending = PendingIntent.getBroadcast(
            context,
            1002,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, Channels.ADHAN)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("حان الآن موعد صلاة $prayerName")
            .setContentText(
                if (prayerTime.isNotBlank()) "الوقت: $prayerTime — حي على الصلاة، حي على الفلاح"
                else "حي على الصلاة، حي على الفلاح"
            )
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_media_pause, "إيقاف الأذان", stopPending)
            .setContentIntent(pendingIntent)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val allowed = if (Build.VERSION.SDK_INT >= 33)
            androidx.core.content.ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        else true
        if (allowed) {
            try { manager.notify(2001, notification) } catch (_: SecurityException) {}
        }

        AdhanScheduler.scheduleNextAdhan(context)
    }

    /** يشغّل الأذان من الأصول المدمجة — أذان الفجر له ملفه الخاص كما في السنة */
    private fun playAdhan(context: Context, prayerName: String) {
        // إعدادات المستخدم: الاهتزاز مع الأذان، والصوت قابل للإطفاء
        if (com.elhajri.noor.data.Prefs.getAdhanVibrate(context)) {
            try {
                val vib = context.getSystemService(Context.VIBRATOR_SERVICE) as? android.os.Vibrator
                if (vib?.hasVibrator() == true) {
                    val pattern = longArrayOf(0, 500, 300, 500, 300, 800)
                    @Suppress("DEPRECATION")
                    if (android.os.Build.VERSION.SDK_INT >= 26)
                        vib.vibrate(android.os.VibrationEffect.createWaveform(pattern, -1))
                    else vib.vibrate(pattern, -1)
                }
            } catch (_: Exception) {}
        }
        if (!com.elhajri.noor.data.Prefs.getAdhanSound(context)) return
        try {
            stopAdhan()
            val fileName = if (prayerName == "الفجر") "audio/adhan_fajr.mp3" else "audio/adhan.mp3"
            val afd = context.assets.openFd(fileName)
            val mp = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_ALARM) // صوت الأذان يتبع مستوى المنبّه
                        .build()
                )
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()
                setOnCompletionListener {
                    it.release()
                    if (player == it) player = null
                }
                setOnErrorListener { p, _, _ ->
                    p.release()
                    if (player == p) player = null
                    true
                }
                prepare()
                start()
            }
            player = mp
        } catch (_: Exception) {
            // حتى لو تعذّر الصوت تبقى الإشاعة والتأجيل يعملان
        }
    }

    private fun stopAdhan() {
        try {
            player?.let {
                if (it.isPlaying) it.stop()
                it.release()
            }
        } catch (_: Exception) {}
        player = null
        // إزالة إشعار الأذان الجاري
        try {
            val nm = lastContext?.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            nm?.cancel(2001)
        } catch (_: Exception) {}
    }

    private fun onReceiveGuard(context: Context) { lastContext = context.applicationContext }

    private var lastContext: Context? = null
}
