package com.elhajri.noor.calendar

import com.elhajri.noor.ui.themeScreenBackground
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.Navy
import com.elhajri.noor.ui.NavyCard
import com.elhajri.noor.ui.TextMain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit
import com.elhajri.noor.ui.NoorGradients

private fun toArabicNumber(number: Any): String {
    val map = mapOf('0' to '٠', '1' to '١', '2' to '٢', '3' to '٣', '4' to '٤', '5' to '٥', '6' to '٦', '7' to '٧', '8' to '٨', '9' to '٩')
    return number.toString().map { map[it] ?: it }.joinToString("")
}

data class HijriDateData(
    val day: Int,
    val monthIndex: Int,
    val monthName: String,
    val year: Int,
    val weekdayName: String,
    val fullText: String
)

data class CalendarOccasion(
    val id: Int,
    val name: String,
    val subtitle: String,
    val hijriDate: String,
    val gregorianApprox: String,
    val icon: String,
    val monthIndex: Int,
    val dayNumber: Int
)

private fun computeFallbackHijri(cal: Calendar, months: List<String>, monthDays: List<Int>): HijriDateData {
    val anchorCal = Calendar.getInstance().apply {
        set(Calendar.YEAR, 2026)
        set(Calendar.MONTH, Calendar.JANUARY)
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val diffMillis = cal.timeInMillis - anchorCal.timeInMillis
    val diffDays = (diffMillis / (1000 * 60 * 60 * 24)).toInt()

    var currentDay = 7 + diffDays
    var currentMonthIdx = 6 // Rajab 1447 (index 6)
    var currentYear = 1447

    while (currentDay > (monthDays.getOrNull(currentMonthIdx) ?: 29)) {
        val daysInCurrent = monthDays.getOrNull(currentMonthIdx) ?: 29
        currentDay -= daysInCurrent
        currentMonthIdx++
        if (currentMonthIdx > 11) {
            currentMonthIdx = 0
            currentYear++
        }
    }

    while (currentDay < 1) {
        currentMonthIdx--
        if (currentMonthIdx < 0) {
            currentMonthIdx = 11
            currentYear--
        }
        val daysInPrev = monthDays.getOrNull(currentMonthIdx) ?: 29
        currentDay += daysInPrev
    }

    val weekdays = listOf("الأحد", "الإثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة", "السبت")
    val dayOfWeekIdx = (cal.get(Calendar.DAY_OF_WEEK) - 1 + 7) % 7
    val weekdayName = weekdays.getOrElse(dayOfWeekIdx) { "" }
    val mName = months.getOrElse(currentMonthIdx) { "" }
    val full = "${toArabicNumber(currentDay)} $mName ${toArabicNumber(currentYear)} هـ"

    return HijriDateData(
        day = currentDay,
        monthIndex = currentMonthIdx,
        monthName = mName,
        year = currentYear,
        weekdayName = weekdayName,
        fullText = full
    )
}

private suspend fun fetchHijriDate(months: List<String>, monthDays: List<Int>): HijriDateData {
    return withContext(Dispatchers.IO) {
        val cal = Calendar.getInstance()
        val dd = String.format(Locale.US, "%02d", cal.get(Calendar.DAY_OF_MONTH))
        val mm = String.format(Locale.US, "%02d", cal.get(Calendar.MONTH) + 1)
        val yyyy = cal.get(Calendar.YEAR)
        val url = "https://api.aladhan.com/v1/gToH/$dd-$mm-$yyyy"

        try {
            val client = OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .build()
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string()
                if (body != null) {
                    val json = JSONObject(body)
                    val data = json.optJSONObject("data")
                    val hijri = data?.optJSONObject("hijri")
                    val gregorian = data?.optJSONObject("gregorian")
                    if (hijri != null) {
                        val dayStr = hijri.optString("day", "1")
                        val day = dayStr.toIntOrNull() ?: 1
                        val monthAr = hijri.optJSONObject("month")?.optString("ar", "") ?: ""
                        val yearStr = hijri.optString("year", "1447")
                        val year = yearStr.toIntOrNull() ?: 1447
                        val weekdayAr = gregorian?.optJSONObject("weekday")?.optString("ar", "") ?: ""

                        val mIdx = months.indexOfFirst { monthAr.contains(it) }.let { if (it == -1) 0 else it }
                        val full = "${toArabicNumber(day)} $monthAr ${toArabicNumber(year)} هـ"

                        return@withContext HijriDateData(
                            day = day,
                            monthIndex = mIdx,
                            monthName = if (monthAr.isNotBlank()) monthAr else months.getOrElse(mIdx) { "" },
                            year = year,
                            weekdayName = weekdayAr,
                            fullText = full
                        )
                    }
                }
            }
        } catch (_: Exception) {}
        return@withContext computeFallbackHijri(cal, months, monthDays)
    }
}

@Composable
fun CalendarScreen() {
    val context = LocalContext.current

    val (hijriMonths, hijriMonthDays, occasions) = remember {
        try {
            val jsonStr = context.assets.open("data/occasions.json").bufferedReader().use { it.readText() }
            val obj = JSONObject(jsonStr)

            val monthsArr = obj.getJSONArray("hijriMonths")
            val months = List(monthsArr.length()) { i -> monthsArr.getString(i) }

            val daysArr = obj.getJSONArray("hijriMonthDays")
            val days = List(daysArr.length()) { i -> daysArr.getInt(i) }

            val occArr = obj.getJSONArray("occasions")
            val occList = List(occArr.length()) { i ->
                val o = occArr.getJSONObject(i)
                val hDate = o.optString("hijriDate", "")
                val mIdx = months.indexOfFirst { hDate.contains(it) }.let { if (it == -1) 0 else it }
                val dayNum = hDate.filter { it.isDigit() }.toIntOrNull() ?: 1
                CalendarOccasion(
                    id = o.optInt("id", i),
                    name = o.optString("name", ""),
                    subtitle = o.optString("subtitle", ""),
                    hijriDate = hDate,
                    gregorianApprox = o.optString("gregorianApprox", ""),
                    icon = o.optString("icon", ""),
                    monthIndex = mIdx,
                    dayNumber = dayNum
                )
            }
            Triple(months, days, occList)
        } catch (_: Exception) {
            Triple(
                listOf("محرم", "صفر", "ربيع الأول", "ربيع الثاني", "جمادى الأولى", "جمادى الآخرة", "رجب", "شعبان", "رمضان", "شوّال", "ذو القعدة", "ذو الحجة"),
                listOf(30, 29, 30, 29, 30, 29, 30, 29, 30, 29, 30, 29),
                emptyList()
            )
        }
    }

    var hijriDate by remember { mutableStateOf<HijriDateData?>(null) }
    var displayMonthIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        val res = fetchHijriDate(hijriMonths, hijriMonthDays)
        hijriDate = res
        displayMonthIndex = res.monthIndex
    }

    val currentYear = hijriDate?.year ?: 1448
    val daysInMonth = hijriMonthDays.getOrElse(displayMonthIndex) { 29 }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .themeScreenBackground()
            .padding(16.dp)
    ) {
        Text(
            text = "التقويم الهجري",
            color = Gold,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        // Today's Hijri Card
        hijriDate?.let { date ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = NavyCard),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "اليوم",
                        color = GoldSoft,
                        fontSize = 12.sp
                    )
                    Text(
                        text = date.fullText,
                        color = Gold,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    if (date.weekdayName.isNotBlank()) {
                        Text(
                            text = date.weekdayName,
                            color = TextMain,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // Month Grid View Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = NavyCard),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Navigation Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = {
                            displayMonthIndex = if (displayMonthIndex > 0) displayMonthIndex - 1 else 11
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowRight,
                            contentDescription = "الشهر السابق",
                            tint = Gold
                        )
                    }

                    Text(
                        text = "${hijriMonths.getOrElse(displayMonthIndex) { "" }} ${toArabicNumber(currentYear)} هـ",
                        color = TextMain,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(
                        onClick = {
                            displayMonthIndex = if (displayMonthIndex < 11) displayMonthIndex + 1 else 0
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowLeft,
                            contentDescription = "الشهر التالي",
                            tint = Gold
                        )
                    }
                }

                // Weekday Labels
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf("الأحد", "الإثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة", "السبت").forEach { d ->
                        Text(
                            text = d,
                            color = GoldSoft,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Days Grid (7 columns)
                val rows = (daysInMonth + 6) / 7
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (r in 0 until rows) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            for (c in 0 until 7) {
                                val dayNum = r * 7 + c + 1
                                if (dayNum <= daysInMonth) {
                                    val isToday = (displayMonthIndex == hijriDate?.monthIndex && dayNum == hijriDate?.day)
                                    val hasOccasion = occasions.any { it.monthIndex == displayMonthIndex && it.dayNumber == dayNum }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                when {
                                                    isToday -> Gold
                                                    hasOccasion -> Gold.copy(alpha = 0.25f)
                                                    else -> Navy.copy(alpha = 0.4f)
                                                }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = toArabicNumber(dayNum),
                                            color = when {
                                                isToday -> Navy
                                                hasOccasion -> Gold
                                                else -> TextMain
                                            },
                                            fontSize = 12.sp,
                                            fontWeight = if (isToday || hasOccasion) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Occasions Section
        Text(
            text = "المناسبات الدينية",
            color = TextMain,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        val monthOccasions = occasions.filter { it.monthIndex == displayMonthIndex }
        val displayOccasions = if (monthOccasions.isNotEmpty()) monthOccasions else occasions

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(displayOccasions) { occ ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = NavyCard),
                    shape = RoundedCornerShape(16.dp)
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
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Gold.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Gold,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = occ.name,
                                    color = TextMain,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = occ.subtitle,
                                    color = GoldSoft.copy(alpha = 0.7f),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = occ.hijriDate,
                                color = Gold,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = occ.gregorianApprox,
                                color = GoldSoft.copy(alpha = 0.6f),
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
