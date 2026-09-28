package com.elhajri.noor.prayer

import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.data.City
import com.elhajri.noor.data.Prefs
import com.elhajri.noor.notification.AdhanScheduler
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.Navy
import com.elhajri.noor.ui.NavyCard
import com.elhajri.noor.ui.NavyLight
import kotlinx.coroutines.delay
import java.util.Calendar
import java.util.Locale
import com.elhajri.noor.ui.NoorGradients

data class PrayerItem(
    val name: String,
    val time: String,
    val icon: String,
    val isNext: Boolean = false,
    val isSunrise: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerScreen() {
    val context = LocalContext.current
    val city = remember { Prefs.getCity(context) ?: City("مكة المكرمة", 21.4225, 39.8262, "السعودية") }

    var timings by remember { mutableStateOf<PrayerTimings?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isAdhanPlaying by remember { mutableStateOf(false) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var isAdhanEnabled by remember { mutableStateOf(Prefs.getAdhanEnabled(context)) }

    var nextPrayerName by remember { mutableStateOf("...") }
    var nextPrayerTime by remember { mutableStateOf("") }
    var countdownText by remember { mutableStateOf("00:00:00") }

    LaunchedEffect(city) {
        isLoading = true
        timings = PrayerRepository.getTimings(context, lat = city.lat, lng = city.lng)
        isLoading = false
    }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        }
    }

    LaunchedEffect(timings) {
        while (true) {
            timings?.let { t ->
                val now = Calendar.getInstance()
                val currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)

                val prayerTimes = listOf(
                    "الفجر" to t.fajr,
                    "الظهر" to t.dhuhr,
                    "العصر" to t.asr,
                    "المغرب" to t.maghrib,
                    "العشاء" to t.isha
                )

                var foundNext = false
                var nextCal: Calendar? = null

                for ((pName, pTimeStr) in prayerTimes) {
                    val parts = pTimeStr.split(":")
                    if (parts.size >= 2) {
                        val h = parts[0].trim().toIntOrNull() ?: 0
                        val m = parts[1].trim().toIntOrNull() ?: 0
                        val pMinutes = h * 60 + m

                        if (pMinutes > currentMinutes) {
                            nextPrayerName = pName
                            nextPrayerTime = format12Hour(pTimeStr)
                            val cal = Calendar.getInstance().apply {
                                set(Calendar.HOUR_OF_DAY, h)
                                set(Calendar.MINUTE, m)
                                set(Calendar.SECOND, 0)
                            }
                            nextCal = cal
                            foundNext = true
                            break
                        }
                    }
                }

                if (!foundNext) {
                    nextPrayerName = "الفجر"
                    val parts = t.fajr.split(":")
                    val h = parts.getOrNull(0)?.trim()?.toIntOrNull() ?: 5
                    val m = parts.getOrNull(1)?.trim()?.toIntOrNull() ?: 0
                    nextPrayerTime = format12Hour(t.fajr)
                    nextCal = Calendar.getInstance().apply {
                        add(Calendar.DAY_OF_YEAR, 1)
                        set(Calendar.HOUR_OF_DAY, h)
                        set(Calendar.MINUTE, m)
                        set(Calendar.SECOND, 0)
                    }
                }

                nextCal?.let { target ->
                    val diffMillis = target.timeInMillis - now.timeInMillis
                    if (diffMillis > 0) {
                        val hours = (diffMillis / (1000 * 60 * 60)) % 24
                        val minutes = (diffMillis / (1000 * 60)) % 60
                        val seconds = (diffMillis / 1000) % 60
                        countdownText = String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
                    } else {
                        countdownText = "00:00:00"
                    }
                }
            }
            delay(1000)
        }
    }

    val playAdhanUrl = "https://cdn.islamic.network/cdn/azan/audio/adhan_makkah.mp3"

    fun toggleAdhanAudio() {
        if (isAdhanPlaying) {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            isAdhanPlaying = false
        } else {
            try {
                val mp = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                    setDataSource(playAdhanUrl)
                    setOnPreparedListener {
                        start()
                        isAdhanPlaying = true
                    }
                    setOnCompletionListener {
                        isAdhanPlaying = false
                        release()
                        mediaPlayer = null
                    }
                    prepareAsync()
                }
                mediaPlayer = mp
            } catch (_: Exception) {
                isAdhanPlaying = false
            }
        }
    }

    Scaffold(
        containerColor = Color(0xFF070B14)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(NoorGradients.ScreenBackground)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = Gold,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${city.name}, ${city.country}",
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isAdhanEnabled) "التنبيه مفعل" else "التنبيه معطل",
                        color = GoldSoft,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    IconButton(
                        onClick = {
                            val newState = !isAdhanEnabled
                            isAdhanEnabled = newState
                            Prefs.setAdhanEnabled(context, newState)
                            if (newState) {
                                AdhanScheduler.scheduleNextAdhan(context)
                            } else {
                                AdhanScheduler.cancel(context)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (isAdhanEnabled) Icons.Filled.Notifications else Icons.Filled.NotificationsOff,
                            contentDescription = "تفعيل الأذان",
                            tint = Gold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF070B14)Card),
                border = androidx.compose.foundation.BorderStroke(1.dp, Gold.copy(alpha = 0.3f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(NavyLight, NavyCard)
                            )
                        )
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "مواقيت الصلاة",
                                color = GoldSoft,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = timings?.hijriDate ?: "جاري التحميل...",
                                color = Gold,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text(
                                    text = "الوقت المتبقي",
                                    color = GoldSoft.copy(alpha = 0.8f),
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = countdownText,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "الصلاة القادمة",
                                    color = GoldSoft.copy(alpha = 0.8f),
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = nextPrayerName,
                                    color = Gold,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (nextPrayerTime.isNotEmpty()) {
                                    Text(
                                        text = nextPrayerTime,
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f),
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { toggleAdhanAudio() },
                                color = Gold.copy(alpha = 0.15f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (isAdhanPlaying) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                                        contentDescription = null,
                                        tint = Gold,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isAdhanPlaying) "إيقاف الأذان" else "استماع للأذان",
                                        color = Gold,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Gold)
                }
            } else {
                timings?.let { t ->
                    val prayerList = listOf(
                        PrayerItem("الفجر", format12Hour(t.fajr), "🌅", isNext = nextPrayerName == "الفجر"),
                        PrayerItem("الشروق", format12Hour(t.sunrise), "☀️", isSunrise = true),
                        PrayerItem("الظهر", format12Hour(t.dhuhr), "☀️", isNext = nextPrayerName == "الظهر"),
                        PrayerItem("العصر", format12Hour(t.asr), "🌤️", isNext = nextPrayerName == "العصر"),
                        PrayerItem("المغرب", format12Hour(t.maghrib), "🌇", isNext = nextPrayerName == "المغرب"),
                        PrayerItem("العشاء", format12Hour(t.isha), "🌙", isNext = nextPrayerName == "العشاء")
                    )

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(prayerList) { p ->
                            PrayerCard(item = p)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PrayerCard(item: PrayerItem) {
    val bgColor = if (item.isNext) NavyLight else NavyCard
    val borderColor = if (item.isNext) Gold else Gold.copy(alpha = 0.15f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = androidx.compose.foundation.BorderStroke(if (item.isNext) 1.5.dp else 1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = item.icon, fontSize = 20.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = item.name,
                    color = if (item.isNext) Gold else MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (item.isNext) FontWeight.Bold else FontWeight.Medium
                )
                if (item.isSunrise) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "(شروق)",
                        color = GoldSoft.copy(alpha = 0.6f),
                        fontSize = 11.sp
                    )
                }
            }

            Text(
                text = item.time,
                color = if (item.isNext) Gold else GoldSoft,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun format12Hour(time24: String): String {
    return try {
        val parts = time24.split(":")
        if (parts.size < 2) return time24
        val h = parts[0].trim().toInt()
        val m = parts[1].trim().toInt()
        val amPm = if (h >= 12) "م" else "ص"
        val h12 = if (h % 12 == 0) 12 else h % 12
        String.format(Locale.US, "%02d:%02d %s", h12, m, amPm)
    } catch (_: Exception) {
        time24
    }
}
