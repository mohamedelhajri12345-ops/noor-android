package com.elhajri.noor.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object Channels {
    const val ADHAN = "adhan_channel"
    const val GENERAL = "general_channel"

    fun ensure(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val adhanChannel = NotificationChannel(
                ADHAN,
                "تنبيهات الأذان",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "تنبيهات وإشعارات أوقات الصلاة"
            }

            val generalChannel = NotificationChannel(
                GENERAL,
                "تنبيهات عامة",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "إشعارات وأذكار عامة"
            }

            notificationManager.createNotificationChannel(adhanChannel)
            notificationManager.createNotificationChannel(generalChannel)
        }
    }
}
