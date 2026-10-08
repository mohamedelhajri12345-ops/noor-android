package com.elhajri.noor.home

import android.icu.util.IslamicCalendar
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.elhajri.noor.prayer.PrayerRepository
import com.elhajri.noor.theme.ThemeStore
import com.elhajri.noor.ui.AmiriFamily
import com.elhajri.noor.ui.themeScreenBackground
import java.util.Calendar
import kotlinx.coroutines.delay

// ألوان الشاشة تتبع الثيم النشط حرفياً — تغيير الثيم يغيّرها فوراً
private val Gold8 get() = com.elhajri.noor.ui.Gold
private val Gold8Soft get() = com.elhajri.noor.ui.GoldSoft
private val TextSoft get() = com.elhajri.noor.ui.TextMain

// صورة الموقع الأصلية نفسها — من كود PrayerWidget.jsx حرفياً
private const val HERO_IMG = "https://media.base44.com/images/public/6a833faeb9e42cca9a6576fa/edd00f95f_generated_image.png"

private fun toArabicDigits(value: Any): String {
    val map = mapOf('0' to '٠', '1' to '١', '2' to '٢', '3' to '٣', '4' to '٤', '5' to '٥', '6' to '٦', '7' to '٧', '8' to '٨', '9' to '٩')
    return value.toString().map { map[it] ?: it }.joinToString("")
}

// آيات اليوم — من الكود المصدري للموقع حرفياً
private data class DailyVerse(val text: String, val ref: String)
private val VERSES = listOf(
    DailyVerse("أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ", "سورة الرعد — الآية ٢٨"),
    DailyVerse("وَمَن يَتَّقِ اللَّهَ يَجْعَل لَّهُ مَخْرَجًا", "سورة الطلاق — الآية ٢"),
    DailyVerse("إِنَّ مَعَ الْعُسْرِ يُسْرًا", "سورة الشرح — الآية ٦"),
    DailyVerse("فَاذْكُرُونِي أَذْكُرْكُمْ وَاشْكُرُوا لِي وَلَا تَكْفُرُونِ", "سورة البقرة — الآية ١٥٢"),
    DailyVerse("وَهُوَ مَعَكُمْ أَيْنَ مَا كُنتُمْ", "سورة الحديد — الآية ٤"),
    DailyVerse("رَبِّ اشْرَحْ لِي صَدْرِي وَيَسِّرْ لِي أَمْرِي", "سورة طه — الآيتان ٢٥-٢٦"),
    DailyVerse("لَا يُكَلِّفُ اللَّهُ نَفْسًا إِلَّا وُسْعَهَا", "سورة البقرة — الآية ٢٨٦"),
    DailyVerse("وَبَشِّرِ الصَّابِرِينَ", "سورة البقرة — الآية ١٥٥"),
)

private val HIJRI_MONTHS = listOf(
    "محرم", "صفر", "ربيع الأول", "ربيع الثاني", "جمادى الأولى", "جمادى الآخرة",
    "رجب", "شعبان", "رمضان", "شوّال", "ذو القعدة", "ذو الحجة"
)

private fun hijriDateLabel(): String {
    return try {
        val cal = IslamicCalendar()
        val day = cal.get(IslamicCalendar.DAY_OF_MONTH)
        val month = HIJRI_MONTHS.getOrNull(cal.get(IslamicCalendar.MONTH)) ?: ""
        val year = cal.get(IslamicCalendar.YEAR)
        "${toArabicDigits(day)} $month ${toArabicDigits(year)}هـ"
    } catch (_: Exception) { "" }
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

/** بطاقة الصلاة — صورة الموقع الأصلية تغطي البطاقة كاملة، تحية وهجري فوقها، إطار ذهبي */
@Composable
private fun HeroPrayerCard(
    greeting: String,
    hijri: String,
    nextPrayerName: String,
    nextPrayerTimeLabel: String,
    countdown: String,
    onNavigate: (String) -> Unit
) {
    val heroInteraction = remember { MutableInteractionSource() }
    val heroScale = pressScale(heroInteraction)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(238.dp)
            .graphicsLayer { scaleX = heroScale; scaleY = heroScale }
            .shadow(elevation = 10.dp, shape = RoundedCornerShape(22.dp))
            .clip(RoundedCornerShape(22.dp))
            .background(Color.Black)
            .border(1.5.dp, Gold8.copy(alpha = 0.35f), RoundedCornerShape(22.dp))
            .clickable(interactionSource = heroInteraction, indication = androidx.compose.foundation.LocalIndication.current) { onNavigate("prayer") }
    ) {
        AsyncImage(
            model = HERO_IMG,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        // تعتيم متدرج يثبّت النصوص فوق الصورة
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    listOf(Color.Black.copy(alpha = 0.25f), Color.Black.copy(alpha = 0.55f), Color.Black.copy(alpha = 0.80f))
                )
            )
        )
        // إضاءة علوية للعمق ثلاثي الأبعاد
        Box(
            Modifier.fillMaxWidth().height(60.dp).background(
                Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.10f), Color.Transparent))
            )
        )
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.End
        ) {
            // التحية + التاريخ الهجري — كما في الموقع
            Column(horizontalAlignment = Alignment.End) {
                Text(greeting, color = Gold8Soft, fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = AmiriFamily)
                if (hijri.isNotEmpty()) {
                    Text(hijri, color = Color.White.copy(alpha = 0.75f), fontSize = 10.sp)
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                // الصلاة القادمة — جهة اليمين
                Column(horizontalAlignment = Alignment.Start) {
                    Text("الصلاة التالية", color = Gold8.copy(alpha = 0.85f), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                    Text(nextPrayerName, color = Gold8Soft, fontSize = 22.sp, fontWeight = FontWeight.Bold, fontFamily = AmiriFamily)
                    Text(nextPrayerTimeLabel, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
                // حلقة العد التنازلي — جهة اليسار مع تسميتها
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .border(2.dp, Gold8.copy(alpha = 0.75f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(countdown, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text("حتى الأذان", color = Color.White.copy(alpha = 0.85f), fontSize = 9.sp)
                }
            }
        }
    }
}

/** بطاقة استكشاف — 3D بإضاءة علوية وحافة ذهبية، مع تموج عند الضغط */
private data class Explore(val route: String, val title: String, val sub: String, val icon: ImageVector)

@Composable
private fun ExploreCard(item: Explore, modifier: Modifier = Modifier, onNavigate: (String) -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val scale = pressScale(interaction)
    Row(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .shadow(elevation = 5.dp, shape = RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        com.elhajri.noor.ui.NavyLight.copy(alpha = 0.78f),
                        com.elhajri.noor.ui.NavyCard.copy(alpha = 0.6f),
                        com.elhajri.noor.ui.Navy.copy(alpha = 0.92f)
                    )
                )
            )
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
    val timings = remember { PrayerRepository.getCachedTimings(context) }

    val hour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val greeting = when {
        hour in 5..11 -> "صباح الخير"
        hour in 12..16 -> "مساء الخير"
        hour in 17..20 -> "مساء الخير"
        else -> "ليلة مباركة"
    }
    val hijri = remember { hijriDateLabel() }
    val points = remember { ThemeStore.getPoints(context) }

    var nextPrayerName by remember { mutableStateOf("...") }
    var nextPrayerTimeLabel by remember { mutableStateOf("") }
    var countdown by remember { mutableStateOf("--:--") }

    LaunchedEffect(Unit) {
        while (true) {
            val t = timings
            if (t != null) {
                val now = Calendar.getInstance()
                val currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
                val prayers = listOf(
                    "الفجر" to t.fajr, "الظهر" to t.dhuhr, "العصر" to t.asr,
                    "المغرب" to t.maghrib, "العشاء" to t.isha
                )
                var chosenName = "الفجر"
                var chosenTime = t.fajr
                for ((name, time) in prayers) {
                    val parts = time.split(":")
                    val h = parts.getOrNull(0)?.trim()?.toIntOrNull() ?: continue
                    val m = parts.getOrNull(1)?.trim()?.toIntOrNull() ?: continue
                    if (h * 60 + m > currentMinutes) { chosenName = name; chosenTime = time; break }
                }
                nextPrayerName = chosenName
                val tp = chosenTime.split(":")
                val th = tp.getOrNull(0)?.trim()?.toIntOrNull() ?: 0
                val tm = tp.getOrNull(1)?.trim()?.toIntOrNull() ?: 0
                val h12 = if (th % 12 == 0) 12 else th % 12
                val ampm = if (th >= 12) "م" else "ص"
                nextPrayerTimeLabel = toArabicDigits(String.format("%02d", h12)) + ":" +
                    toArabicDigits(String.format("%02d", tm)) + " " + ampm

                val target = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, th); set(Calendar.MINUTE, tm); set(Calendar.SECOND, 0)
                    if (timeInMillis <= now.timeInMillis) add(Calendar.DAY_OF_YEAR, 1)
                }
                val diff = ((target.timeInMillis - now.timeInMillis) / 1000).coerceAtLeast(0)
                countdown = String.format("%d:%02d", diff / 3600, (diff % 3600) / 60).let { toArabicDigits(it) }
            }
            delay(1000)
        }
    }

    val dayIndex = remember { Calendar.getInstance().get(Calendar.DAY_OF_MONTH) }
    val verse = VERSES[dayIndex % VERSES.size]

    // استكشف نور — ستة أبواب كما في التصميم المعتمد
    val explore = listOf(
        Explore("quran", "القرآن الكريم", "تلاوة، حفظ، تفسير", Icons.Filled.MenuBook),
        Explore("athkar", "الأذكار", "أذكار اليوم والليلة", Icons.Filled.Favorite),
        Explore("qibla", "القبلة", "اتجاه القبلة بدقة", Icons.Filled.Explore),
        Explore("library", "الأناشيد", "إيقاع بلا آلات", Icons.Filled.MusicNote),
        Explore("games", "ميدان الألعاب", "٦ تحديات إيمانية", Icons.Filled.SportsEsports),
        Explore("ai", "المساعد الذكي", "أسئلتك الفقهية", Icons.Filled.AutoAwesome),
    )

    Box(modifier = Modifier.fillMaxSize().themeScreenBackground()) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // بطاقة الصلاة — صورة الموقع الأصلية
            HeroPrayerCard(greeting, hijri, nextPrayerName, nextPrayerTimeLabel, countdown, onNavigate)

            Spacer(Modifier.height(10.dp))

            // شريط الإعلانات — فوق العناصر مباشرة كما طلب محمد
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 4.dp, shape = RoundedCornerShape(13.dp))
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
                Text("نقاطك: ${toArabicDigits(points)} 🌙", color = Gold8Soft, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                Text(
                    "أكمل ٣ إعلانات واربح ٣٠٠ نقطة ←",
                    color = Gold8Soft.copy(alpha = 0.85f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onNavigate("earn") }
                )
            }

            // استكشف نور — العناصر فوق الآية
            Spacer(Modifier.height(12.dp))
            // عنوان استكشف نور
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.width(4.dp).height(16.dp).clip(RoundedCornerShape(2.dp)).background(Gold8))
                Spacer(Modifier.width(8.dp))
                Text("استكشف نور", color = Gold8, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(10.dp))

            // شبكة الاستكشاف ٢×٣
            for (row in 0..2) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ExploreCard(explore[row * 2], Modifier.weight(1f), onNavigate)
                    ExploreCard(explore[row * 2 + 1], Modifier.weight(1f), onNavigate)
                }
                Spacer(Modifier.height(10.dp))
            }

            // آية اليوم — بطاقة زجاجية بحافة ذهبية وعمق ثلاثي الأبعاد
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 6.dp, shape = RoundedCornerShape(16.dp))
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
                Text("آية اليوم", color = Gold8, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                Spacer(Modifier.height(6.dp))
                Text(verse.text, color = TextSoft, fontSize = 17.sp, fontFamily = AmiriFamily, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(4.dp))
                Text(verse.ref, color = Gold8Soft.copy(alpha = 0.80f), fontSize = 10.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End)
            }

            Spacer(Modifier.height(14.dp))

            Spacer(Modifier.height(16.dp))
        }
    }
}
