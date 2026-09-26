package com.elhajri.noor.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.elhajri.noor.prayer.PrayerRepository
import java.util.Calendar

object AdhanScheduler {
    private const val REQUEST_CODE = 1001

    fun scheduleNextAdhan(context: Context) {
        val timings = PrayerRepository.getCachedTimings(context) ?: return

        val now = Calendar.getInstance()
        val currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)

        val prayerTimes = listOf(
            timings.fajr,
            timings.dhuhr,
            timings.asr,
            timings.maghrib,
            timings.isha
        )

        var nextCal: Calendar? = null

        for (timeStr in prayerTimes) {
            val parts = timeStr.split(":")
            if (parts.size >= 2) {
                val h = parts[0].trim().toIntOrNull() ?: continue
                val m = parts[1].trim().toIntOrNull() ?: continue
                val pMinutes = h * 60 + m

                if (pMinutes > currentMinutes) {
                    val cal = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, h)
                        set(Calendar.MINUTE, m)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    nextCal = cal
                    break
                }
            }
        }

        if (nextCal == null) {
            val parts = timings.fajr.split(":")
            val h = parts.getOrNull(0)?.trim()?.toIntOrNull() ?: 5
            val m = parts.getOrNull(1)?.trim()?.toIntOrNull() ?: 0
            nextCal = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, 1)
                set(Calendar.HOUR_OF_DAY, h)
                set(Calendar.MINUTE, m)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
        }

        val triggerAtMillis = nextCal.timeInMillis
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AdhanReceiver::class.java)
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val pendingIntent = PendingIntent.getBroadcast(context, REQUEST_CODE, intent, flags)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                } else {
                    alarmManager.setWindow(AlarmManager.RTC_WAKEUP, triggerAtMillis, 60000L, pendingIntent)
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            }
        } catch (_: SecurityException) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }

    fun cancel(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AdhanReceiver::class.java)
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val pendingIntent = PendingIntent.getBroadcast(context, REQUEST_CODE, intent, flags)
        alarmManager.cancel(pendingIntent)
    }
}
