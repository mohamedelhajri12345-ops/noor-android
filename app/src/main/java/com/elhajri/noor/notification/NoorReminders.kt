package com.elhajri.noor.notification

import android.app.AlarmManager
import android.app.Notification
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.elhajri.noor.MainActivity
import com.elhajri.noor.R

/**
 * Daily engaging reminders (Quran reading, memorization streak, dhikr).
 * Rotating friendly messages that pull the user back into the app.
 */
object NoorReminders {

    private const val REQUEST_CODE = 4701

    /** Message pool for the daily Quran reminder (shown ~20:00 local time). */
    private val quranMessages = listOf(
        "📖 لم تقرأ وردك اليوم — دقائق قليلة مع كتاب الله تنير ليلتك",
        "🌙 اقرأ القرآن الآن وأكمل ما وصلت إليه من الحفظ",
        "✨ آية واحدة تكفي.. افتح المصحف وداوم على وردك",
        "🕌 جلسة قراءة قصيرة اليوم تحفظ استمراريتك — لا تكسر السلسلة!",
        "🌱 ﴿وَرَتِّلِ الْقُرْآنَ تَرْتِيلًا﴾ — وردك بانتظارك",
        "💡 أصدقاؤك يقرؤون الآن.. انضم لهم ووسّع صدرك بالقرآن"
    )

    private val morningMessages = listOf(
        "☀️ ابدأ يومك بذكر الله — أذكار الصباح تنتظرك",
        "🌸 صلاة الفجر نور لكل يوم، وأذكار الصباح طمأنينة لقلبك",
        "🕊️ دقيقة واحدة من الأذكار = حسنات لا تنقطع"
    )

    fun schedule(context: Context) {
        Channels.ensure(context)
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val receiver = Intent(context, NoorReminderReceiver::class.java)

        // evening Quran reminder at 20:00
        val quranIntent = PendingIntent.getBroadcast(
            context, REQUEST_CODE,
            receiver.putExtra("slot", "quran"),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val cal = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 20)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(java.util.Calendar.DAY_OF_YEAR, 1)
        }
        scheduleExact(am, cal.timeInMillis, quranIntent)

        // morning dhikr reminder at 07:30
        val morningIntent = PendingIntent.getBroadcast(
            context, REQUEST_CODE + 1,
            receiver.putExtra("slot", "morning"),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val cal2 = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 7)
            set(java.util.Calendar.MINUTE, 30)
            set(java.util.Calendar.SECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(java.util.Calendar.DAY_OF_YEAR, 1)
        }
        scheduleExact(am, cal2.timeInMillis, morningIntent)
    }

    private fun scheduleExact(am: AlarmManager, at: Long, pi: PendingIntent) {
        try {
            if (Build.VERSION.SDK_INT >= 31 && !am.canScheduleExactAlarms()) {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
            } else {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
            }
        } catch (_: SecurityException) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
        }
    }

    fun build(context: Context, slot: String): Notification {
        val (title, text) = if (slot == "quran") {
            val day = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_YEAR)
            "ورد القرآن بانتظارك" to quranMessages[day % quranMessages.size]
        } else {
            val day = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_YEAR)
            "أذكار الصباح" to morningMessages[day % morningMessages.size]
        }

        val open = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java).apply {
                putExtra("open_route", if (slot == "quran") "quran" else "athkar")
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, Channels.GENERAL)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setAutoCancel(true)
            .setContentIntent(open)
            .build()
    }
}

/** Fires the daily reminders and reschedules itself for the next day. */
class NoorReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val slot = intent.getStringExtra("slot") ?: "quran"
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        try {
            nm.notify(9100 + slot.hashCode() % 100, NoorReminders.build(context, slot))
        } catch (_: SecurityException) {
        }
        // reschedule for tomorrow
        NoorReminders.schedule(context)
    }
}
