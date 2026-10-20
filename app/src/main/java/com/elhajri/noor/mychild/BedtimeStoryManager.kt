package com.elhajri.noor.mychild

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.elhajri.noor.library.LibraryCatalogLoader
import com.elhajri.noor.mychild.BedtimeStoryItem
import org.json.JSONArray
import java.io.InputStreamReader
import java.util.Calendar

object BedtimeStoryManager {
    private const val CHANNEL_ID = "noor_bedtime_story_channel"
    private const val CHANNEL_NAME = "قصة قبل النوم"

    fun loadBedtimeStories(context: Context): List<BedtimeStoryItem> {
        val stories = mutableListOf<BedtimeStoryItem>()

        // 1. Load from fallback bundled assets
        try {
            val isStream = context.assets.open("data/bedtime_stories_fallback.json")
            val reader = InputStreamReader(isStream, "UTF-8")
            val jsonText = reader.readText()
            reader.close()
            isStream.close()

            val array = JSONArray(jsonText)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                stories.add(
                    BedtimeStoryItem(
                        id = obj.getString("id"),
                        title = obj.getString("title"),
                        summary = obj.getString("summary"),
                        content = obj.getString("content"),
                        category = "prophets_kids",
                        isBundled = true
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 2. Also load from Library catalog if available
        try {
            val catalog = LibraryCatalogLoader.loadCatalog(context)
            val kidsBooks = catalog.filter { it.category == "prophets_kids" || it.category == "kids_stories" }
            for (book in kidsBooks) {
                stories.add(
                    BedtimeStoryItem(
                        id = book.id,
                        title = book.title,
                        summary = "من كتب قصص الأطفال والمكتبة الإسلامية: " + book.author,
                        content = "قصة ملهمة من كتاب " + book.title + ".\n\nتفضل بقراءتها مع طفلك وتأمل دروسها وأخلاقها الكريمة.",
                        category = book.category,
                        coverUrl = book.coverUrl,
                        isBundled = false
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return stories
    }

    fun getTodayStory(context: Context): BedtimeStoryItem {
        val stories = loadBedtimeStories(context)
        if (stories.isEmpty()) {
            return BedtimeStoryItem(
                id = "default",
                title = "قصة اليوم قبل النوم",
                summary = "قصة جميلة ومفيدة",
                content = "كان نبي الله حليماً ورحيماً، علمنا مكارم الأخلاق والصدق والأمانة."
            )
        }
        val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        val index = dayOfYear % stories.size
        return stories[index]
    }

    fun scheduleBedtimeNotification(context: Context, childProfile: ChildProfile) {
        if (!childProfile.bedtimeNotificationEnabled) return

        createNotificationChannel(context)

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, BedtimeStoryReceiver::class.java).apply {
            putExtra("childName", childProfile.name)
            putExtra("childId", childProfile.id)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            childProfile.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, childProfile.bedtimeHour)
            set(Calendar.MINUTE, childProfile.bedtimeMinute)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        try {
            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "تنبيهات قصة قبل النوم اليومية للأطفال"
            }
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }
}

class BedtimeStoryReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val childName = intent.getStringExtra("childName") ?: "طفلك"
        val todayStory = BedtimeStoryManager.getTodayStory(context)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            ?: return

        val builder = NotificationCompat.Builder(context, "noor_bedtime_story_channel")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("🌙 وقت قصة قبل النوم لـ $childName")
            .setContentText("قصة الليلة: ${todayStory.title}")
            .setStyle(NotificationCompat.BigTextStyle().bigText("قصة الليلة: ${todayStory.title}\n${todayStory.summary}"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        notificationManager.notify(1001 + childName.hashCode(), builder.build())
    }
}
