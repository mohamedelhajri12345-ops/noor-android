package com.elhajri.noor.home

import android.icu.util.IslamicCalendar
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.elhajri.noor.R
import com.elhajri.noor.data.Prefs
import com.elhajri.noor.prayer.PrayerRepository
import com.elhajri.noor.prayer.PrayerTimings
import com.elhajri.noor.settings.NoorLocale
import com.elhajri.noor.settings.t
import com.elhajri.noor.theme.ThemeStore
import com.elhajri.noor.theme.noorGlassCard
import com.elhajri.noor.ui.AmiriFamily
import com.elhajri.noor.ui.TextMain
import com.elhajri.noor.ui.themeScreenBackground
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.delay

// ألوان الشاشة تتبع الثيم النشط حرفياً — تغيير الثيم يغيّرها فوراً
private val Gold8 get() = com.elhajri.noor.ui.Gold
private val Gold8Soft get() = com.elhajri.noor.ui.GoldSoft
private val TextSoft get() = com.elhajri.noor.ui.TextMain

// صورة الموقع الأصلية نفسها — من كود PrayerWidget.jsx حرفياً
private const val HERO_IMG = "https://media.base44.com/images/public/6a833faeb9e42cca9a6576fa/edd00f95f_generated_image.png"

private fun toArabicDigits(value: Any): String = NoorLocale.formatDigits(value)

// آيات اليوم — القرآن الكريم يبقى بالعربية حرفياً حسب الضوابط الشرعية
private data class DailyVerse(val text: String, val refAr: String, val refEn: String)
private val VERSES = listOf(
    DailyVerse("أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ", "سورة الرعد — الآية ٢٨", "Surah Ar-Ra'd — Verse 28"),
    DailyVerse("وَمَن يَتَّقِ اللَّهَ يَجْعَل لَّهُ مَخْرَجًا", "سورة الطلاق — الآية ٢", "Surah At-Talaq — Verse 2"),
    DailyVerse("إِنَّ مَعَ الْعُسْرِ يُسْرًا", "سورة الشرح — الآية ٦", "Surah Ash-Sharh — Verse 6"),
    DailyVerse("فَاذْكُرُونِي أَذْكُرْكُمْ وَاشْكُرُوا لِي وَلَا تَكْفُرُونِ", "سورة البقرة — الآية ١٥٢", "Surah Al-Baqarah — Verse 152"),
    DailyVerse("وَهُوَ مَعَكُمْ أَيْنَ مَا كُنتُمْ", "سورة الحديد — الآية ٤", "Surah Al-Hadid — Verse 4"),
    DailyVerse("رَبِّ اشْرَحْ لِي صَدْرِي وَيَسِّرْ لِي أَمْرِي", "سورة طه — الآيتان ٢٥-٢٦", "Surah Ta-Ha — Verses 25-26"),
    DailyVerse("لَا يُكَلِّفُ اللَّهُ نَفْسًا إِلَّا وُسْعَهَا", "سورة البقرة — الآية ٢٨٦", "Surah Al-Baqarah — Verse 286"),
    DailyVerse("وَبَشِّرِ الصَّابِرِينَ", "سورة البقرة — الآية ١٥٥", "Surah Al-Baqarah — Verse 155"),
)

private val HIJRI_MONTHS_AR = listOf(
    "محرم", "صفر", "ربيع الأول", "ربيع الثاني", "جمادى الأولى", "جمادى الآخرة",
    "رجب", "شعبان", "رمضان", "شوّال", "ذو القعدة", "ذو الحجة"
)
private val HIJRI_MONTHS_EN = listOf(
    "Muharram", "Safar", "Rabi' al-Awwal", "Rabi' al-Thani", "Jumada al-Awwal", "Jumada al-Thani",
    "Rajab", "Sha'ban", "Ramadan", "Shawwal", "Dhu al-Qi'dah", "Dhu al-Hijjah"
)

private fun hijriDateLabel(): String {
    return try {
        val cal = IslamicCalendar()
        val day = cal.get(IslamicCalendar.DAY_OF_MONTH)
        val monthIdx = cal.get(IslamicCalendar.MONTH)
        val month = if (NoorLocale.isEnglish) HIJRI_MONTHS_EN.getOrNull(monthIdx) ?: "" else HIJRI_MONTHS_AR.getOrNull(monthIdx) ?: ""
        val year = cal.get(IslamicCalendar.YEAR)
        if (NoorLocale.isEnglish) {
            "$day $month $year AH"
        } else {
            "${toArabicDigits(day)} $month ${toArabicDigits(year)}هـ"
        }
    } catch (_: Exception) { "" }
}

private fun translatePrayerName(name: String): String = when (name) {
    "الفجر" -> t("الفجر", "Fajr")
    "الظهر" -> t("الظهر", "Dhuhr")
    "العصر" -> t("العصر", "Asr")
    "المغرب" -> t("المغرب", "Maghrib")
    "العشاء" -> t("العشاء", "Isha")
    else -> name
}

private fun translateCityName(name: String): String = when (name) {
    "مكة المكرمة" -> t("مكة المكرمة", "Makkah Al-Mukarramah")
    "المدينة المنورة" -> t("المدينة المنورة", "Madinah Al-Munawwarah")
    "الرياض" -> t("الرياض", "Riyadh")
    "جدة" -> t("جدة", "Jeddah")
    "القاهرة" -> t("القاهرة", "Cairo")
    "الدار البيضاء" -> t("الدار البيضاء", "Casablanca")
    "الرباط" -> t("الدار البيضاء", "Rabat")
    "دبي" -> t("دبي", "Dubai")
    "أبوظبي" -> t("أبوظبي", "Abu Dhabi")
    "القدس" -> t("القدس", "Jerusalem")
    "عمان" -> t("عمان", "Amman")
    "الكويت" -> t("الكويت", "Kuwait City")
    "الدوحة" -> t("الدوحة", "Doha")
    "تونس" -> t("تونس", "Tunis")
    "الجزائر" -> t("الجزائر", "Algiers")
    "بغداد" -> t("بغداد", "Baghdad")
    "دمشق" -> t("دمشق", "Damascus")
    else -> name
}

@Composable
private fun pressScale(interactionSource: MutableInteractionSource): Float {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = androidx.compose.animation.core.tween(120),
        label = "pressScale"
    )
    return scale
}

/** مربع خانات الوقت الزجاجي للعداد الذهبي */
@Composable
private fun DigitGlassBox(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(width = 36.dp, height = 34.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Gold8.copy(alpha = 0.28f),
                            Gold8.copy(alpha = 0.10f)
                        )
                    )
                )
                .border(1.dp, Gold8.copy(alpha = 0.6f), RoundedCornerShape(9.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = value,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.75f),
            fontSize = 8.5.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/** عداد ذهبي أنيق تنازلي للصلاة القادمة */
@Composable
private fun GoldCountdownTimer(
    hours: String,
    minutes: String,
    seconds: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0x77112B20),
                        Color(0xCC081510)
                    )
                )
            )
            .border(
                width = 1.5.dp,
                brush = Brush.sweepGradient(
                    listOf(
                        Gold8,
                        Gold8Soft,
                        Color(0xFFFFD700),
                        Gold8,
                        Gold8Soft
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = t("الوقت المتبقي للأذان", "Time until Adhan"),
                color = Gold8Soft,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(3.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                DigitGlassBox(value = hours, label = t("ساعة", "hr"))
                Text(
                    text = ":",
                    color = Gold8,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
                DigitGlassBox(value = minutes, label = t("دقيقة", "min"))
                Text(
                    text = ":",
                    color = Gold8,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
                DigitGlassBox(value = seconds, label = t("ثانية", "sec"))
            }
        }
    }
}

private data class HomePrayerDisplayItem(
    val name: String,
    val timeLabel: String,
    val isNext: Boolean
)

/** شريط الأوقات التالية لمواقيت الصلاة اليومية */
@Composable
private fun NextPrayerTimesRow(
    prayers: List<HomePrayerDisplayItem>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.Black.copy(alpha = 0.45f))
            .border(1.dp, Gold8.copy(alpha = 0.30f), RoundedCornerShape(14.dp))
            .padding(vertical = 6.dp, horizontal = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Gold8)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = t("الأوقات التالية", "Next Prayers"),
                    color = Gold8Soft,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = t("مواقيت اليوم", "Today's Timings"),
                color = Color.White.copy(alpha = 0.60f),
                fontSize = 9.sp
            )
        }
        Spacer(Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            prayers.forEach { item ->
                val bg = if (item.isNext) {
                    Brush.verticalGradient(
                        listOf(Gold8.copy(alpha = 0.35f), Gold8.copy(alpha = 0.15f))
                    )
                } else {
                    Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = 0.08f), Color.Transparent)
                    )
                }
                val borderColor = if (item.isNext) Gold8 else Color.White.copy(alpha = 0.12f)
                val textColor = if (item.isNext) Gold8Soft else Color.White.copy(alpha = 0.85f)
                val nameColor = if (item.isNext) Gold8 else Color.White.copy(alpha = 0.65f)

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 2.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(bg)
                        .border(1.dp, borderColor, RoundedCornerShape(10.dp))
                        .padding(vertical = 5.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = translatePrayerName(item.name),
                        color = nameColor,
                        fontSize = 11.sp,
                        fontWeight = if (item.isNext) FontWeight.Bold else FontWeight.Normal,
                        fontFamily = AmiriFamily
                    )
                    Spacer(Modifier.height(1.dp))
                    Text(
                        text = item.timeLabel,
                        color = textColor,
                        fontSize = 9.5.sp,
                        fontWeight = if (item.isNext) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

/** بطاقة الصلاة الرئيسية — خلفية المسجد مع عداد ذهبي أنيق وشريط الأوقات التالية */
@Composable
private fun HeroPrayerCard(
    greeting: String,
    hijri: String,
    cityName: String,
    nextPrayerName: String,
    nextPrayerTimeLabel: String,
    hoursStr: String,
    minutesStr: String,
    secondsStr: String,
    dailyPrayers: List<HomePrayerDisplayItem>,
    onNavigate: (String) -> Unit
) {
    val heroInteraction = remember { MutableInteractionSource() }
    val heroScale = pressScale(heroInteraction)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 280.dp)
            .graphicsLayer { scaleX = heroScale; scaleY = heroScale }
            .clip(RoundedCornerShape(22.dp))
            .background(Color.Black)
            .border(1.5.dp, Gold8.copy(alpha = 0.45f), RoundedCornerShape(22.dp))
            .clickable(interactionSource = heroInteraction, indication = androidx.compose.foundation.LocalIndication.current) { onNavigate("prayer") }
    ) {
        Image(
            painter = painterResource(R.drawable.hero_mosque),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        AsyncImage(
            model = HERO_IMG,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    listOf(
                        Color.Black.copy(alpha = 0.35f),
                        Color.Black.copy(alpha = 0.60f),
                        Color.Black.copy(alpha = 0.88f)
                    )
                )
            )
        )
        Box(
            Modifier.fillMaxWidth().height(60.dp).background(
                Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.12f), Color.Transparent))
            )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.45f))
                        .border(1.dp, Gold8.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = Gold8,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(translateCityName(cityName), color = Color.White.copy(alpha = 0.9f), fontSize = 10.sp)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(greeting, color = Gold8Soft, fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = AmiriFamily)
                    if (hijri.isNotEmpty()) {
                        Text(hijri, color = Color.White.copy(alpha = 0.75f), fontSize = 10.sp)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    Text(t("الصلاة التالية", "Next Prayer"), color = Gold8.copy(alpha = 0.85f), fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold)
                    Text(translatePrayerName(nextPrayerName), color = Gold8Soft, fontSize = 23.sp, fontWeight = FontWeight.Bold, fontFamily = AmiriFamily)
                    Text(nextPrayerTimeLabel, color = TextMain, fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold)
                }

                GoldCountdownTimer(
                    hours = hoursStr,
                    minutes = minutesStr,
                    seconds = secondsStr
                )
            }

            Spacer(Modifier.height(10.dp))

            if (dailyPrayers.isNotEmpty()) {
                NextPrayerTimesRow(prayers = dailyPrayers)
            }
        }
    }
}

private data class Explore(val route: String, val title: String, val sub: String, val icon: ImageVector)

@Composable
private fun ExploreCard(item: Explore, modifier: Modifier = Modifier, onNavigate: (String) -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val scale = pressScale(interaction)
    Row(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .noorGlassCard(cornerRadius = 16.dp)
            .border(1.dp, Gold8.copy(alpha = 0.38f), RoundedCornerShape(16.dp))
            .clickable(interactionSource = interaction, indication = androidx.compose.foundation.LocalIndication.current) { onNavigate(item.route) }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.radialGradient(
                        colors = listOf(Gold8.copy(alpha = 0.26f), Gold8.copy(alpha = 0.08f), Gold8.copy(alpha = 0.16f))
                    )
                )
                .border(1.dp, Gold8.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(item.icon, contentDescription = item.title, tint = Gold8, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(10.dp))
        Column {
            Text(item.title, color = TextSoft, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
            Text(item.sub, color = TextSoft.copy(alpha = 0.60f), fontSize = 9.5.sp)
        }
    }
}

@Composable
fun HomeScreen(onNavigate: (String) -> Unit) {
    val context = LocalContext.current
    val city = remember { Prefs.getCity(context) ?: com.elhajri.noor.data.City("مكة المكرمة", 21.4225, 39.8262, "السعودية") }
    var timings by remember { mutableStateOf<PrayerTimings?>(PrayerRepository.getCachedTimings(context)) }

    LaunchedEffect(city) {
        timings = PrayerRepository.getTimings(context, lat = city.lat, lng = city.lng, country = city.country)
    }

    val hour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val greeting = when {
        hour in 5..11 -> t("صباح الخير", "Good Morning")
        hour in 12..16 -> t("مساء الخير", "Good Afternoon")
        hour in 17..20 -> t("مساء الخير", "Good Evening")
        else -> t("ليلة مباركة", "Blessed Night")
    }
    val hijri = remember(NoorLocale.lang) { hijriDateLabel() }
    val points = remember { ThemeStore.getPoints(context) }

    var nextPrayerName by remember { mutableStateOf("...") }
    var nextPrayerTimeLabel by remember { mutableStateOf("") }
    var hoursStr by remember { mutableStateOf(toArabicDigits("00")) }
    var minutesStr by remember { mutableStateOf(toArabicDigits("00")) }
    var secondsStr by remember { mutableStateOf(toArabicDigits("00")) }
    var dailyPrayers by remember { mutableStateOf<List<HomePrayerDisplayItem>>(emptyList()) }

    LaunchedEffect(timings, NoorLocale.lang) {
        while (true) {
            val t = timings ?: PrayerRepository.getCachedTimings(context)
            val now = Calendar.getInstance()
            val currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
            val prayers = listOf(
                "الفجر" to t.fajr,
                "الظهر" to t.dhuhr,
                "العصر" to t.asr,
                "المغرب" to t.maghrib,
                "العشاء" to t.isha
            )
            var chosenName = "الفجر"
            var chosenTime = t.fajr
            var foundNext = false
            var targetCal: Calendar? = null

            for ((name, time) in prayers) {
                val parts = time.split(":")
                val h = parts.getOrNull(0)?.trim()?.toIntOrNull() ?: continue
                val m = parts.getOrNull(1)?.trim()?.toIntOrNull() ?: continue
                if (h * 60 + m > currentMinutes) {
                    chosenName = name
                    chosenTime = time
                    targetCal = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, h)
                        set(Calendar.MINUTE, m)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    foundNext = true
                    break
                }
            }

            if (!foundNext) {
                chosenName = "الفجر"
                chosenTime = t.fajr
                val parts = t.fajr.split(":")
                val h = parts.getOrNull(0)?.trim()?.toIntOrNull() ?: 5
                val m = parts.getOrNull(1)?.trim()?.toIntOrNull() ?: 0
                targetCal = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, 1)
                    set(Calendar.HOUR_OF_DAY, h)
                    set(Calendar.MINUTE, m)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
            }

            nextPrayerName = chosenName
            val tp = chosenTime.split(":")
            val th = tp.getOrNull(0)?.trim()?.toIntOrNull() ?: 0
            val tm = tp.getOrNull(1)?.trim()?.toIntOrNull() ?: 0
            val h12Val = if (th % 12 == 0) 12 else th % 12
            val ampmVal = if (th >= 12) t("م", "PM") else t("ص", "AM")
            nextPrayerTimeLabel = toArabicDigits(String.format("%02d", h12Val)) + ":" +
                toArabicDigits(String.format("%02d", tm)) + " " + ampmVal

            targetCal?.let { target ->
                val diffSec = ((target.timeInMillis - now.timeInMillis) / 1000).coerceAtLeast(0)
                val h = (diffSec / 3600) % 24
                val m = (diffSec % 3600) / 60
                val s = diffSec % 60
                hoursStr = toArabicDigits(String.format(Locale.US, "%02d", h))
                minutesStr = toArabicDigits(String.format(Locale.US, "%02d", m))
                secondsStr = toArabicDigits(String.format(Locale.US, "%02d", s))
            }

            dailyPrayers = prayers.map { (pName, pTime) ->
                val parts = pTime.split(":")
                val h = parts.getOrNull(0)?.trim()?.toIntOrNull() ?: 0
                val m = parts.getOrNull(1)?.trim()?.toIntOrNull() ?: 0
                val itemH12 = if (h % 12 == 0) 12 else h % 12
                val itemAmpm = if (h >= 12) t("م", "PM") else t("ص", "AM")
                val label = "${toArabicDigits(String.format("%02d", itemH12))}:${toArabicDigits(String.format("%02d", m))} $itemAmpm"
                HomePrayerDisplayItem(
                    name = pName,
                    timeLabel = label,
                    isNext = (pName == chosenName)
                )
            }

            delay(1000)
        }
    }

    val dayIndex = remember { Calendar.getInstance().get(Calendar.DAY_OF_MONTH) }
    val verse = VERSES[dayIndex % VERSES.size]

    val explore = listOf(
        Explore("quran", t("القرآن الكريم", "Noble Qur'an"), t("تلاوة، حفظ، تفسير", "Recitation, Memorization, Tafseer"), Icons.AutoMirrored.Filled.MenuBook),
        Explore("athkar", t("الأذكار", "Adhkar"), t("أذكار اليوم والليلة", "Daily Supplications"), Icons.Filled.Favorite),
        Explore("qibla", t("القبلة", "Qibla"), t("اتجاه القبلة بدقة", "Accurate Qibla Direction"), Icons.Filled.Explore),
        Explore("nasheeds", t("الأناشيد", "Nasheeds"), t("إيقاع بلا آلات", "Vocal Melodies"), Icons.Filled.MusicNote),
        Explore("games", t("ميدان الألعاب", "Games Hub"), t("٦ تحديات إيمانية", "Faith Challenges"), Icons.Filled.SportsEsports),
        Explore("ai", t("المساعد الذكي", "Smart AI Helper"), t("أسئلتك الفقهية", "Islamic Q&A"), Icons.Filled.AutoAwesome),
    )

    Box(modifier = Modifier.fillMaxSize().themeScreenBackground()) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            HeroPrayerCard(
                greeting = greeting,
                hijri = hijri,
                cityName = city.name,
                nextPrayerName = nextPrayerName,
                nextPrayerTimeLabel = nextPrayerTimeLabel,
                hoursStr = hoursStr,
                minutesStr = minutesStr,
                secondsStr = secondsStr,
                dailyPrayers = dailyPrayers,
                onNavigate = onNavigate
            )

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(13.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Gold8.copy(alpha = 0.16f), Gold8.copy(alpha = 0.06f), com.elhajri.noor.ui.Navy.copy(alpha = 0.40f))
                        )
                    )
                    .border(1.dp, Gold8.copy(alpha = 0.45f), RoundedCornerShape(13.dp))
                    .padding(horizontal = 13.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(t("نقاطك: ", "Points: ") + "${toArabicDigits(points)} 🌙", color = Gold8Soft, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                Text(
                    t("أكمل ٣ إعلانات واربح ٣٠٠ نقطة ←", "Watch 3 ads & win 300 pts →"),
                    color = Gold8Soft.copy(alpha = 0.85f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onNavigate("earn") }
                )
            }

            Spacer(Modifier.height(10.dp))

            com.elhajri.noor.games.DailyChallengeHubCard(
                onPlayChallenge = { route -> onNavigate(route) }
            )

            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.width(4.dp).height(16.dp).clip(RoundedCornerShape(2.dp)).background(Gold8))
                Spacer(Modifier.width(8.dp))
                Text(t("استكشف نور", "Explore NOOR"), color = Gold8, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(10.dp))

            for (row in 0..2) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ExploreCard(explore[row * 2], Modifier.weight(1f), onNavigate)
                    ExploreCard(explore[row * 2 + 1], Modifier.weight(1f), onNavigate)
                }
                Spacer(Modifier.height(10.dp))
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                com.elhajri.noor.ui.NavyLight.copy(alpha = 0.35f),
                                com.elhajri.noor.ui.NavyCard.copy(alpha = 0.3f),
                                Gold8.copy(alpha = 0.12f)
                            )
                        )
                    )
                    .border(1.dp, Gold8.copy(alpha = 0.40f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 15.dp, vertical = 10.dp)
            ) {
                Text(t("آية اليوم", "Verse of the Day"), color = Gold8, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text(
                    text = verse.text,
                    color = TextMain,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = AmiriFamily,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (NoorLocale.isEnglish) verse.refEn else verse.refAr,
                    color = Gold8Soft,
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}
