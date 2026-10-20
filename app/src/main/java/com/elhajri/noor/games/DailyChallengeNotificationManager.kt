package com.elhajri.noor.games

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.elhajri.noor.MainActivity
import com.elhajri.noor.R
import com.elhajri.noor.notification.Channels
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.Navy
import com.elhajri.noor.ui.TextMain
import com.elhajri.noor.ui.TextSecondaryLinen
import com.elhajri.noor.ui.noorGlassCard
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * WorkManager Worker for periodic daily challenge reminder backup.
 */
class DailyChallengeWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : Worker(appContext, workerParams) {

    override fun doWork(): Result {
        return try {
            if (DailyChallengeNotificationManager.isNotificationEnabled(appContext) &&
                !DailyChallengeManager.isCompletedToday(appContext)
            ) {
                DailyChallengeNotificationManager.sendNotification(appContext)
            }
            Result.success()
        } catch (_: Exception) {
            Result.failure()
        }
    }
}

/**
 * BroadcastReceiver for Exact Alarm trigger of Daily Challenge Notification.
 */
class DailyChallengeNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (DailyChallengeNotificationManager.isNotificationEnabled(context) &&
            !DailyChallengeManager.isCompletedToday(context)
        ) {
            DailyChallengeNotificationManager.sendNotification(context)
        }
        // Reschedule alarm for tomorrow
        DailyChallengeNotificationManager.scheduleNotification(context)
    }
}

/**
 * DailyChallengeNotificationManager: Manages opt-in smart daily challenge reminders
 * via WorkManager periodic backup + AlarmManager exact alarms with Arabic texts and settings toggle.
 */
object DailyChallengeNotificationManager {

    private const val PREFS_NAME = "noor_daily_notif_prefs_v1"
    private const val KEY_ENABLED = "daily_notif_enabled"
    private const val KEY_HOUR = "daily_notif_hour"
    private const val KEY_MINUTE = "daily_notif_minute"
    private const val ALARM_REQUEST_CODE = 8801
    private const val WORK_TAG = "noor_daily_challenge_work"
    private const val NOTIFICATION_ID = 9912

    private val notificationMessages = listOf(
        "⚔️ تحدي اليوم الإيماني بانتظارك! اختبر معلوماتك واكسب +٢٠ نجمة بونوس 🌟",
        "🌙 لعبة اليوم جديدة وممتعة! شارك الآن لترتقي في رتبة الخبرة وتجمع النجوم",
        "🏆 فرصة اليوم: تحدي إيماني متجدد بمكافآت مضاعفة بانتظارك الآن في نور!"
    )

    private fun prefs(context: Context): SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isNotificationEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_ENABLED, true)

    fun setNotificationEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_ENABLED, enabled).apply()
        if (enabled) {
            scheduleNotification(context)
        } else {
            cancelNotification(context)
        }
    }

    fun getNotificationHour(context: Context): Int =
        prefs(context).getInt(KEY_HOUR, 20) // default 20:00 (8 PM)

    fun getNotificationMinute(context: Context): Int =
        prefs(context).getInt(KEY_MINUTE, 0)

    fun setNotificationTime(context: Context, hour: Int, minute: Int) {
        prefs(context).edit().putInt(KEY_HOUR, hour).putInt(KEY_MINUTE, minute).apply()
        if (isNotificationEnabled(context)) {
            scheduleNotification(context)
        }
    }

    fun scheduleNotification(context: Context) {
        if (!isNotificationEnabled(context)) return

        val hour = getNotificationHour(context)
        val minute = getNotificationMinute(context)

        // 1. Schedule Exact Alarm via AlarmManager
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, DailyChallengeNotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pendingIntent)
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pendingIntent)
            }
        } catch (_: SecurityException) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pendingIntent)
        }

        // 2. Schedule WorkManager Periodic Request as fallback
        try {
            val workRequest = PeriodicWorkRequestBuilder<DailyChallengeWorker>(24, TimeUnit.HOURS)
                .addTag(WORK_TAG)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiresBatteryNotLow(true)
                        .build()
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_TAG,
                ExistingPeriodicWorkPolicy.UPDATE,
                workRequest
            )
        } catch (_: Exception) {
            // WorkManager fallback
        }
    }

    fun cancelNotification(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, DailyChallengeNotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)

        try {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_TAG)
        } catch (_: Exception) {
        }
    }

    fun sendNotification(context: Context) {
        Channels.ensure(context)
        val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        val text = notificationMessages[dayOfYear % notificationMessages.size]

        val openIntent = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java).apply {
                putExtra("open_route", "games")
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, Channels.GENERAL)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("تحدي اليوم الإيماني 🌙")
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(openIntent)
            .build()

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        try {
            nm.notify(NOTIFICATION_ID, notification)
        } catch (_: SecurityException) {
        }
    }
}

/**
 * UI Component: Notification Settings Row Card for Daily Challenge.
 */
@Composable
fun DailyChallengeNotificationSettingsCard(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isEnabled by remember { mutableStateOf(DailyChallengeNotificationManager.isNotificationEnabled(context)) }
    val hour = remember { DailyChallengeNotificationManager.getNotificationHour(context) }
    val minute = remember { DailyChallengeNotificationManager.getNotificationMinute(context) }

    val timeFormatted = remember(hour, minute) {
        val map = mapOf('0' to '٠', '1' to '١', '2' to '٢', '3' to '٣', '4' to '٤', '5' to '٥', '6' to '٦', '7' to '٧', '8' to '٨', '9' to '٩')
        val raw = String.format("%02d:%02d", hour, minute)
        raw.map { map[it] ?: it }.joinToString("")
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .noorGlassCard(cornerRadius = 16.dp)
            .border(1.dp, Gold.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Gold.copy(alpha = 0.2f))
                        .border(1.dp, Gold, RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isEnabled) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                        contentDescription = null,
                        tint = Gold,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("تنبيهات تحدي اليوم 🌙", color = TextMain, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("تذكير يومي مهذب بالتحدي ($timeFormatted)", color = TextSecondaryLinen, fontSize = 10.sp)
                }
            }

            Switch(
                checked = isEnabled,
                onCheckedChange = { checked ->
                    isEnabled = checked
                    DailyChallengeNotificationManager.setNotificationEnabled(context, checked)
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Navy,
                    checkedTrackColor = Gold,
                    uncheckedThumbColor = GoldSoft,
                    uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
                )
            )
        }
    }
}
