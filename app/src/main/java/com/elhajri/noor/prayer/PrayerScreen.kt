package com.elhajri.noor.prayer

import com.elhajri.noor.settings.NoorLocale
import com.elhajri.noor.settings.t
import com.elhajri.noor.settings.toArabicDigits

import com.elhajri.noor.ui.themeScreenBackground
import android.media.AudioAttributes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
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
import androidx.compose.ui.graphics.Color
import com.elhajri.noor.theme.noorGlassCard

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
    var city by remember { mutableStateOf(Prefs.getCity(context) ?: City("مكة المكرمة", 21.4225, 39.8262, "السعودية", "Asia/Riyadh")) }
    var isAutoLocation by remember { mutableStateOf(Prefs.isAutoLocation(context)) }
    var showCityPicker by remember { mutableStateOf(false) }
    var citySearch by remember { mutableStateOf("") }
    val allCities = remember { com.elhajri.noor.data.DataLoader.cities(context) }

    var timings by remember { mutableStateOf<PrayerTimings?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isAdhanPlaying by remember { mutableStateOf(false) }
    var isAdhanEnabled by remember { mutableStateOf(Prefs.getAdhanEnabled(context)) }

    var nextPrayerName by remember { mutableStateOf("...") }
    var nextPrayerTime by remember { mutableStateOf("") }
    var countdownText by remember { mutableStateOf("00:00:00") }

    LaunchedEffect(city) {
        isLoading = true
        timings = PrayerRepository.getTimings(
            context,
            lat = city.lat,
            lng = city.lng,
            country = city.country,
            timezone = city.timezone
        )
        isLoading = false
        // Reschedule adhan now that the cache holds THIS city's fresh times
        if (isAdhanEnabled) {
            com.elhajri.noor.notification.AdhanScheduler.scheduleNextAdhan(context)
        }
    }

    DisposableEffect(Unit) {
        onDispose { }
    }

    LaunchedEffect(timings) {
        while (true) {
            // مزامنة الواجهة مع الخدمة: اكتمل الأذان في الخلفية → يعود الزر لطبيعته
            if (isAdhanPlaying && !com.elhajri.noor.notification.AdhanPlaybackService.isPlaying) {
                isAdhanPlaying = false
            }
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

    fun toggleAdhanAudio() {
        if (isAdhanPlaying) {
            com.elhajri.noor.notification.AdhanPlaybackService.stop(context)
            isAdhanPlaying = false
        } else {
            com.elhajri.noor.notification.AdhanPlaybackService.play(context)
            isAdhanPlaying = true
        }
    }

    // ═══ اختيار المدينة والتحديد التلقائي ═══
    if (showCityPicker) {
        AlertDialog(
            onDismissRequest = { showCityPicker = false },
            title = { Text("اختر مدينتك أو الموقع التلقائي", color = Gold, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    // خيار التحديد التلقائي حسب GPS
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isAutoLocation) Gold else NavyLight)
                            .clickable {
                                isAutoLocation = true
                                Prefs.setAutoLocation(context, true)
                                NoorLocationManager.updateAutoLocation(context, force = true) { newCity ->
                                    if (newCity != null) {
                                        city = newCity
                                    }
                                    showCityPicker = false
                                }
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.MyLocation,
                            contentDescription = null,
                            tint = if (isAutoLocation) Navy else Gold,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                t("الموقع التلقائي (GPS)", "Auto Location (GPS)"),
                                color = if (isAutoLocation) Navy else Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                t("انتقال تلقائي حسب موقعك الجغرافي", "Auto switch based on location"),
                                color = if (isAutoLocation) Navy.copy(alpha = 0.7f) else GoldSoft.copy(alpha = 0.7f),
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = citySearch,
                        onValueChange = { citySearch = it },
                        placeholder = { Text("ابحث عن مدينة (300+ مدينة عالمية)...", fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Gold,
                            focusedBorderColor = Gold,
                            unfocusedBorderColor = NavyLight
                        )
                    )

                    LazyColumn(modifier = Modifier.height(300.dp)) {
                        val q = citySearch.trim()
                        val filtered = if (q.isBlank()) allCities else allCities.filter {
                            it.name.contains(q) || it.country.contains(q)
                        }
                        items(filtered) { c ->
                            val selected = !isAutoLocation && c.name == city.name && c.country == city.country
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (selected) Gold else Color.Transparent)
                                    .clickable {
                                        isAutoLocation = false
                                        Prefs.setAutoLocation(context, false)
                                        city = c
                                        Prefs.setCity(context, c)
                                        showCityPicker = false
                                    }
                                    .padding(horizontal = 10.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Filled.LocationOn,
                                    contentDescription = null,
                                    tint = if (selected) Navy else Gold,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(c.name, color = if (selected) Navy else Color.White, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(c.country, color = if (selected) Navy.copy(alpha = 0.7f) else GoldSoft.copy(alpha = 0.6f), fontSize = 11.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCityPicker = false }) { Text("إغلاق", color = GoldSoft) }
            },
            containerColor = NavyCard
        )
    }

    Scaffold(
        containerColor = Color(0xFF070B14)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .themeScreenBackground()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { showCityPicker = true }
                ) {
                    Icon(
                        imageVector = if (isAutoLocation) Icons.Filled.MyLocation else Icons.Filled.LocationOn,
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
                    if (isAutoLocation) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Gold.copy(alpha = 0.2f)
                        ) {
                            Text(
                                t("تلقائي", "Auto"),
                                color = Gold,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isAdhanEnabled) t("التنبيه مفعل", "Adhan Enabled") else t("التنبيه معطل", "Adhan Disabled"),
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
                            contentDescription = t("تفعيل الأذان", "Enable Adhan"),
                            tint = Gold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp).noorGlassCard(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
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
                                text = t("مواقيت الصلاة", "Prayer Times"),
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
                                    text = t("الوقت المتبقي", "Time Remaining"),
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
                                    text = t("الصلاة القادمة", "Next Prayer"),
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
                                        text = if (isAdhanPlaying) t("إيقاف الأذان", "Stop Adhan") else t("استماع للأذان", "Listen to Adhan"),
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
                        PrayerItem(t("الفجر", "Fajr"), format12Hour(t.fajr), "🌅", isNext = nextPrayerName == "الفجر"),
                        PrayerItem(t("الشروق", "Sunrise"), format12Hour(t.sunrise), "☀️", isSunrise = true),
                        PrayerItem(t("الظهر", "Dhuhr"), format12Hour(t.dhuhr), "☀️", isNext = nextPrayerName == "الظهر"),
                        PrayerItem(t("العصر", "Asr"), format12Hour(t.asr), "🌤️", isNext = nextPrayerName == "العصر"),
                        PrayerItem(t("المغرب", "Maghrib"), format12Hour(t.maghrib), "🌇", isNext = nextPrayerName == "المغرب"),
                        PrayerItem(t("العشاء", "Isha"), format12Hour(t.isha), "🌙", isNext = nextPrayerName == "العشاء")
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
private fun PrayerCard(item: PrayerItem) {
    val backgroundColor = when {
        item.isNext -> Gold.copy(alpha = 0.2f)
        item.isSunrise -> NavyLight.copy(alpha = 0.3f)
        else -> NavyLight.copy(alpha = 0.6f)
    }

    val borderColor = if (item.isNext) Gold else Color.Transparent

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = item.icon, fontSize = 20.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = item.name,
                    color = if (item.isNext) Gold else MaterialTheme.colorScheme.onBackground,
                    fontWeight = if (item.isNext) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 16.sp
                )
                if (item.isNext) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Gold.copy(alpha = 0.3f)
                    ) {
                        Text(
                            text = t("القادمة", "Next"),
                            color = Gold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Text(
                text = item.time,
                color = if (item.isNext) Gold else MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

private fun format12Hour(time24: String): String {
    return try {
        val parts = time24.split(":")
        if (parts.size < 2) return time24
        var hour = parts[0].trim().toInt()
        val minute = parts[1].trim().toInt()
        val period = if (hour >= 12) t("م", "PM") else t("ص", "AM")
        if (hour == 0) hour = 12
        else if (hour > 12) hour -= 12
        String.format(Locale.US, "%d:%02d %s", hour, minute, period)
    } catch (_: Exception) {
        time24
    }
}
