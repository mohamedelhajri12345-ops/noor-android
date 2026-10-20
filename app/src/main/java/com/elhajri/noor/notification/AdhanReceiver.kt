package com.elhajri.noor.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.elhajri.noor.MainActivity
import com.elhajri.noor.audio.AudioSessionManager
import com.elhajri.noor.audio.AudioSource

/**
 * AdhanReceiver — يستقبل تنبيهات الأذان والتذكيرات المسبقة ويشغّلها عبر AdhanPlaybackService
 * مع إشعارات كاملة بالنصوص العربية الأنيقة والتحكم الكامل.
 */
class AdhanReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_STOP_ADHAN = "com.elhajri.noor.STOP_ADHAN"

        fun stopAdhanStatic() {
            // Handled via AdhanPlaybackService
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        onReceiveGuard(context)
        if (intent.action == ACTION_STOP_ADHAN) {
            stopAdhan()
            return
        }

        Channels.ensure(context)
        val eventType = intent.getStringExtra("event_type") ?: "ADHAN"
        val prayerName = intent.getStringExtra("prayer_name") ?: "الصلاة"
        val prayerTime = intent.getStringExtra("prayer_time") ?: ""
        val preMinutes = intent.getIntExtra("pre_minutes", 10)
        val soundId = intent.getStringExtra("sound_id") ?: AdhanSettingsStore.getSelectedAdhanSound(context)

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val allowed = if (Build.VERSION.SDK_INT >= 33)
            androidx.core.content.ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        else true

        if (eventType == "PRE_ADHAN") {
            // إشعار التذكير المسبق قبل الأذان
            val activityIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                activityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val preText = "باقي $preMinutes دقائق على أذان صلاة $prayerName — استعد للصلاة والوضوء"

            val notification = NotificationCompat.Builder(context, Channels.ADHAN)
                .setColor(0xFFE9C46A.toInt())
                .setSmallIcon(com.elhajri.noor.R.drawable.ic_notification)
                .setContentTitle("اقترب موعد صلاة $prayerName 🕌")
                .setContentText(preText)
                .setStyle(NotificationCompat.BigTextStyle().bigText(preText))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build()

            if (allowed) {
                try { manager.notify(2002, notification) } catch (_: SecurityException) {}
            }

            // جدولة الحدث التالي فوراً
            AdhanScheduler.scheduleNextAdhan(context)
        } else {
            // إشعار ودفق الأذان الرئيسي
            playAdhan(context, prayerName, soundId)

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
                .setColor(0xFFE9C46A.toInt())
                .setSmallIcon(com.elhajri.noor.R.drawable.ic_notification)
                .setContentTitle("حان الآن موعد صلاة $prayerName 🕌")
                .setContentText(
                    if (prayerTime.isNotBlank()) "الوقت: $prayerTime — حي على الصلاة، حي على الفلاح"
                    else "حي على الصلاة، حي على الفلاح"
                )
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setAutoCancel(true)
                .setOngoing(true)
                .addAction(com.elhajri.noor.R.drawable.ic_notif_close, "إيقاف الأذان", stopPending)
                .setContentIntent(pendingIntent)
                .build()

            if (allowed) {
                try { manager.notify(2001, notification) } catch (_: SecurityException) {}
            }

            // جدولة الحدث التالي فوراً
            AdhanScheduler.scheduleNextAdhan(context)
        }
    }

    private fun playAdhan(context: Context, prayerName: String, soundId: String) {
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
        AdhanPlaybackService.play(context, soundId)
    }

    private fun stopAdhan() {
        lastContext?.let {
            AdhanPlaybackService.stop(it)
            AudioSessionManager.onPlaybackStopped(it, AudioSource.ADHAN)
        }
        try {
            val nm = lastContext?.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            nm?.cancel(2001)
        } catch (_: Exception) {}
    }

    private fun onReceiveGuard(context: Context) { lastContext = context.applicationContext }

    private var lastContext: Context? = null
}
