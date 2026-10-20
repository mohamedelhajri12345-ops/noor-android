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

    private data class ScheduledAdhanEvent(
        val timeMillis: Long,
        val type: String, // "ADHAN" or "PRE_ADHAN"
        val prayerName: String,
        val prayerTime: String,
        val preMinutes: Int
    )

    /** جدول التنبيهات القادمة (الأذان والتذكير المسبق) حسَب تفضيلات المستخدم */
    fun scheduleNextAdhan(context: Context) {
        if (!AdhanSettingsStore.isAdhanEnabled(context)) {
            cancel(context)
            return
        }

        val timings = PrayerRepository.getCachedTimings(context) ?: return
        val nowMillis = System.currentTimeMillis()

        val prayerTimes = listOf(
            Pair("الفجر", timings.fajr),
            Pair("الظهر", timings.dhuhr),
            Pair("العصر", timings.asr),
            Pair("المغرب", timings.maghrib),
            Pair("العشاء", timings.isha)
        )

        val preReminderEnabled = AdhanSettingsStore.isPreAdhanReminderEnabled(context)
        val preMinutes = AdhanSettingsStore.getPreAdhanMinutes(context)

        val candidateEvents = mutableListOf<ScheduledAdhanEvent>()

        for (dayOffset in 0..1) {
            for ((name, timeStr) in prayerTimes) {
                if (!AdhanSettingsStore.isPrayerEnabled(context, name)) continue

                val parts = timeStr.split(":")
                if (parts.size >= 2) {
                    val h = parts[0].trim().toIntOrNull() ?: continue
                    val m = parts[1].trim().toIntOrNull() ?: continue

                    val prayerCal = Calendar.getInstance().apply {
                        if (dayOffset > 0) add(Calendar.DAY_OF_YEAR, dayOffset)
                        set(Calendar.HOUR_OF_DAY, h)
                        set(Calendar.MINUTE, m)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }

                    val pMillis = prayerCal.timeInMillis

                    // 1. Pre-Adhan reminder event
                    if (preReminderEnabled && preMinutes > 0) {
                        val preMillis = pMillis - (preMinutes * 60 * 1000L)
                        if (preMillis > nowMillis) {
                            candidateEvents.add(
                                ScheduledAdhanEvent(
                                    timeMillis = preMillis,
                                    type = "PRE_ADHAN",
                                    prayerName = name,
                                    prayerTime = timeStr,
                                    preMinutes = preMinutes
                                )
                            )
                        }
                    }

                    // 2. Main Adhan event
                    if (pMillis > nowMillis) {
                        candidateEvents.add(
                            ScheduledAdhanEvent(
                                timeMillis = pMillis,
                                type = "ADHAN",
                                prayerName = name,
                                prayerTime = timeStr,
                                preMinutes = 0
                            )
                        )
                    }
                }
            }
        }

        val nextEvent = candidateEvents.minByOrNull { it.timeMillis } ?: return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AdhanReceiver::class.java).apply {
            putExtra("event_type", nextEvent.type)
            putExtra("prayer_name", nextEvent.prayerName)
            putExtra("prayer_time", nextEvent.prayerTime)
            putExtra("pre_minutes", nextEvent.preMinutes)
            putExtra("sound_id", AdhanSettingsStore.getSelectedAdhanSound(context))
        }

        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val pendingIntent = PendingIntent.getBroadcast(context, REQUEST_CODE, intent, flags)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextEvent.timeMillis, pendingIntent)
                } else {
                    alarmManager.setWindow(AlarmManager.RTC_WAKEUP, nextEvent.timeMillis, 60000L, pendingIntent)
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextEvent.timeMillis, pendingIntent)
            }
        } catch (_: SecurityException) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, nextEvent.timeMillis, pendingIntent)
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
