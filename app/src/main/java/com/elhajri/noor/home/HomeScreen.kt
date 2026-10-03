package com.elhajri.noor.home

import android.icu.util.IslamicCalendar
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.Image
import com.elhajri.noor.R
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.elhajri.noor.prayer.PrayerRepository
import com.elhajri.noor.ui.AmiriFamily
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.Navy
import com.elhajri.noor.ui.NavyLight
import com.elhajri.noor.ui.TextMain
import java.util.Calendar
import kotlinx.coroutines.delay

// صور الموقع الأصلية نفسها — طبق الأصل
private const val HERO_IMG = "https://media.base44.com/images/public/6a833faeb9e42cca9a6576fa/edd00f95f_generated_image.png"
private const val EMBLEM_IMG = "https://media.base44.com/images/public/6a833faeb9e42cca9a6576fa/f6fc7be6c_generated_image.png"

private fun toArabicDigits(value: Any): String {
    val map = mapOf('0' to '٠', '1' to '١', '2' to '٢', '3' to '٣', '4' to '٤', '5' to '٥', '6' to '٦', '7' to '٧', '8' to '٨', '9' to '٩')
    return value.toString().map { map[it] ?: it }.joinToString("")
}

// آيات اليوم — من الكود المصدري للموقع حرفياً
private data class DailyVerse(val text: String, val ref: String, val ayahNumber: String)
private val VERSES = listOf(
    DailyVerse("أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ", "سورة الرعد — الآية ٢٨", "٢٨"),
    DailyVerse("وَمَن يَتَّقِ اللَّهَ يَجْعَل لَّهُ مَخْرَجًا", "سورة الطلاق — الآية ٢", "٢"),
    DailyVerse("إِنَّ مَعَ الْعُسْرِ يُسْرًا", "سورة الشرح — الآية ٦", "٦"),
    DailyVerse("فَاذْكُرُونِي أَذْكُرْكُمْ وَاشْكُرُوا لِي وَلَا تَكْفُرُونِ", "سورة البقرة — الآية ١٥٢", "١٥٢"),
    DailyVerse("وَهُوَ مَعَكُمْ أَيْنَ مَا كُنتُمْ", "سورة الحديد — الآية ٤", "٤"),
    DailyVerse("رَبِّ اشْرَحْ لِي صَدْرِي وَيَسِّرْ لِي أَمْرِي", "سورة طه — الآيتان ٢٥-٢٦", "٢٥"),
    DailyVerse("لَا يُكَلِّفُ اللَّهُ نَفْسًا إِلَّا وُسْعَهَا", "سورة البقرة — الآية ٢٨٦", "٢٨٦"),
    DailyVerse("وَبَشِّرِ الصَّابِرِينَ", "سورة البقرة — الآية ١٥٥", "١٥٥"),
)

// أحاديث اليوم — من الكود المصدري للموقع حرفياً
private data class DailyHadith(val text: String, val ref: String)
private val HADITHS = listOf(
    DailyHadith("إنَّ اللهَ تعالى يقول: أنا عندَ ظنِّ عبدي بي، وأنا معَه إذا ذكَرَني", "رواه البخاري ومسلم"),
    DailyHadith("مَن كان يؤمن بالله واليوم الآخر فليقل خيرًا أو ليصمت", "متفق عليه"),
    DailyHadith("الطُّهُورُ شَطْرُ الإِيمَانِ", "رواه مسلم"),
    DailyHadith("لَا يُؤْمِنُ أَحَدُكُمْ حَتَّى يُحِبَّ لِأَخِيهِ مَا يُحِبُّ لِنَفْسِهِ", "متفق عليه"),
    DailyHadith("مَن سَلَكَ طَرِيقًا يَلْتَمِسُ فِيهِ عِلْمًا سَهَّلَ اللَّهُ لَهُ طَرِيقًا إِلَى الجَنَّة", "رواه مسلم"),
    DailyHadith("الكَلِمَةُ الطَّيِّبَةُ صَدَقَةٌ", "متفق عليه"),
    DailyHadith("تَبَسُّمُكَ فِي وَجْهِ أَخِيكَ لَكَ صَدَقَةٌ", "رواه الترمذي"),
)

// المناسبات القادمة — من بيانات الموقع
private data class Occasion(val name: String, val subtitle: String, val hijriDate: String, val icon: ImageVector)
private val OCCASIONS = listOf(
    Occasion("المولد النبوي", "ذكرى مولد النبي ﷺ", "١٢ ربيع الأول", Icons.Filled.AutoAwesome),
    Occasion("الإسراء والمعراج", "ذكرى رحلة الإسراء والمعراج", "٢٧ رجب", Icons.Filled.Star),
    Occasion("ليلة النصف من شعبان", "ليلة مباركة يكثر فيها الدعاء", "١٥ شعبان", Icons.Filled.DarkMode),
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

/** بطاقة فاخرة — نفس luxury-card بالموقع: تدرج عمودي بحواف ذهبية خفيفة */
@Composable
private fun LuxuryCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.verticalGradient(listOf(NavyLight.copy(alpha = 0.88f), Color(0xFF11141A).copy(alpha = 0.82f))))
            .border(1.dp, Gold.copy(alpha = 0.10f), RoundedCornerShape(16.dp))
            .padding(20.dp),
        content = content
    )
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

    var nextPrayerName by remember { mutableStateOf("...") }
    var nextPrayerTimeLabel by remember { mutableStateOf("") }
    var countdown by remember { mutableStateOf("--:--:--") }

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
                countdown = String.format("%02d:%02d:%02d", diff / 3600, (diff % 3600) / 60, diff % 60)
                    .let { toArabicDigits(it) }
            }
            delay(1000)
        }
    }

    val dayIndex = remember { Calendar.getInstance().get(Calendar.DAY_OF_MONTH) }
    val verse = VERSES[dayIndex % VERSES.size]
    val hadith = HADITHS[dayIndex % HADITHS.size]

    // الوصول السريع — نفس قائمة الموقع بأيقوناته الذهبية
    data class Quick(val route: String, val label: String, val icon: ImageVector)
    val quickAccess = listOf(
        Quick("prayer", "الصلاة", Icons.Filled.Mosque),
        Quick("tasbih", "السبحة", Icons.Filled.TouchApp),
        Quick("athkar", "الأذكار", Icons.Filled.AutoStories),
        Quick("quran", "القرآن", Icons.Filled.MenuBook),
        Quick("games", "الألعاب", Icons.Filled.SportsEsports),
        Quick("library", "الأناشيد", Icons.Filled.MusicNote),
        Quick("quiz", "الاختبار", Icons.Filled.Quiz),
        Quick("stories", "القصص", Icons.Filled.Book),
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // ============ الخلفية الجديدة: سماء ليلية ومسجد — خلفية كامل الشاشة ============
        Image(
            painter = androidx.compose.ui.res.painterResource(R.drawable.theme_bg_makkah_night),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    0f to Navy.copy(alpha = 0.60f),
                    0.45f to Navy.copy(alpha = 0.74f),
                    1f to Navy.copy(alpha = 0.92f)
                )
            )
        )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // ============ ودجت الصلاة — نص فوق خلفية الشاشة، بلا صورة محلية ولا إطار ============
        val heroInteraction = remember { MutableInteractionSource() }
        val heroScale = pressScale(heroInteraction)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
                .graphicsLayer { scaleX = heroScale; scaleY = heroScale }
                .clickable(interactionSource = heroInteraction, indication = androidx.compose.material3.LocalIndication.current) { onNavigate("prayer") }
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(greeting, color = Gold, fontSize = 20.sp, fontWeight = FontWeight.Bold, fontFamily = AmiriFamily)
                    if (hijri.isNotEmpty()) {
                        Text(hijri, color = Gold.copy(alpha = 0.8f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    // العد التنازلي — جهة البداية كما في الموقع
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(countdown, color = TextMain, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Text("الوقت المتبقي", color = Gold.copy(alpha = 0.8f), fontSize = 10.sp)
                    }
                    // الصلاة القادمة — الجهة الأخرى
                    Column(horizontalAlignment = Alignment.End) {
                        Text("الصلاة القادمة", color = Gold.copy(alpha = 0.8f), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                        Text(nextPrayerName, color = Gold, fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = AmiriFamily)
                        Text(nextPrayerTimeLabel, color = TextMain.copy(alpha = 0.9f), fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // ============ الوصول السريع — شبكة 4×2 بحدود ذهبية وأيقونات ذهبية ============
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.width(4.dp).height(16.dp).clip(RoundedCornerShape(2.dp)).background(Gold))
            Spacer(Modifier.width(8.dp))
            Text("الوصول السريع", color = Gold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(12.dp))
        for (row in 0..1) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                for (col in 0..3) {
                    val item = quickAccess[row * 4 + col]
                    val interaction = remember(item.route) { MutableInteractionSource() }
                    val scale = pressScale(interaction)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .graphicsLayer { scaleX = scale; scaleY = scale }
                            .clip(RoundedCornerShape(16.dp))
                            .background(Brush.verticalGradient(listOf(NavyLight.copy(alpha = 0.6f), Color(0xFF11141A).copy(alpha = 0.5f))))
                            .border(1.dp, Gold.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                            .clickable(interactionSource = interaction, indication = androidx.compose.material3.LocalIndication.current) { onNavigate(item.route) },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(item.icon, contentDescription = item.label, tint = Gold, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.height(6.dp))
                        Text(item.label, color = TextMain, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            if (row == 0) Spacer(Modifier.height(10.dp))
        }

        Spacer(Modifier.height(20.dp))

        // ============ آية اليوم ============
        LuxuryCard {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("آية اليوم", color = Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(verse.text, color = TextMain, fontSize = 18.sp, fontFamily = AmiriFamily, textAlign = TextAlign.Center)
                    Spacer(Modifier.width(6.dp))
                    Box(
                        modifier = Modifier.size(28.dp).clip(RoundedCornerShape(50)).background(Gold.copy(alpha = 0.10f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(verse.ayahNumber, color = Gold, fontSize = 11.sp)
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text(verse.ref, color = TextMain.copy(alpha = 0.5f), fontSize = 12.sp)
                Spacer(Modifier.height(12.dp))
                val shareInteraction = remember { MutableInteractionSource() }
                val shareScale = pressScale(shareInteraction)
                Row(
                    modifier = Modifier
                        .graphicsLayer { scaleX = shareScale; scaleY = shareScale }
                        .clip(RoundedCornerShape(12.dp))
                        .background(Gold.copy(alpha = 0.10f))
                        .clickable(interactionSource = shareInteraction, indication = androidx.compose.material3.LocalIndication.current) {
                            val send = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(android.content.Intent.EXTRA_TEXT, "${verse.text}\n\n${verse.ref}")
                            }
                            context.startActivity(android.content.Intent.createChooser(send, "مشاركة الآية"))
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Share, contentDescription = null, tint = Gold, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("مشاركة الآية", color = Gold, fontSize = 12.sp)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ============ الحديث الشريف — بشعار التطبيق ============
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Brush.verticalGradient(listOf(NavyLight.copy(alpha = 0.88f), Color(0xFF11141A).copy(alpha = 0.82f))))
                .border(1.dp, Gold.copy(alpha = 0.10f), RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("الحديث الشريف", color = Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("قال رسول الله ﷺ: ${hadith.text}", color = TextMain, fontSize = 16.sp, fontFamily = AmiriFamily)
                Spacer(Modifier.height(8.dp))
                Text(hadith.ref, color = TextMain.copy(alpha = 0.5f), fontSize = 12.sp)
            }
            Spacer(Modifier.width(12.dp))
            Image(
                painter = androidx.compose.ui.res.painterResource(R.mipmap.ic_launcher),
                contentDescription = "أيقونة التطبيق",
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, Gold.copy(alpha = 0.35f), RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(Modifier.height(20.dp))

        // ============ المناسبات القادمة ============
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.width(4.dp).height(16.dp).clip(RoundedCornerShape(2.dp)).background(Gold))
                Spacer(Modifier.width(8.dp))
                Text("المناسبات القادمة", color = Gold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            Text(
                "الكل",
                color = Gold, fontSize = 12.sp,
                modifier = Modifier.clickable { onNavigate("calendar") }
            )
        }
        Spacer(Modifier.height(10.dp))
        OCCASIONS.forEach { occ ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(NavyLight.copy(alpha = 0.45f))
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(Gold.copy(alpha = 0.10f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(occ.icon, contentDescription = null, tint = Gold, modifier = Modifier.size(16.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(occ.name, color = TextMain, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(occ.subtitle, color = TextMain.copy(alpha = 0.5f), fontSize = 12.sp)
                }
                Text(occ.hijriDate, color = Gold, fontSize = 12.sp)
            }
        }

        Spacer(Modifier.height(16.dp))
    }
    }
}
