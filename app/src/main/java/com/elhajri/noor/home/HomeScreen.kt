package com.elhajri.noor.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.elhajri.noor.prayer.PrayerRepository
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.Navy
import com.elhajri.noor.ui.NavyCard
import com.elhajri.noor.ui.NavyLight
import java.util.Calendar
import kotlinx.coroutines.delay
import com.elhajri.noor.ui.NoorGradients

private const val MOSQUE_IMAGE = "https://media.base44.com/images/public/6a9ec57e3a8cd5ed957a8641/8bf461ac2_generated_image.png"

private fun toArabicDigits(value: Any): String {
    val map = mapOf('0' to '٠', '1' to '١', '2' to '٢', '3' to '٣', '4' to '٤', '5' to '٥', '6' to '٦', '7' to '٧', '8' to '٨', '9' to '٩')
    return value.toString().map { map[it] ?: it }.joinToString("")
}

/** بطاقة الوصول السريع — أيقونة ملوّنة مميزة (على نمط تطبيقات إسلامية عصرية) + عنوان + وصف */
private data class GridFeature(
    val route: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val accent: Color
)

/** محتوى مقترح — بطاقة أفقية بصورة/تدرج + تسمية سفلية */
private data class ContentSpot(val route: String, val label: String, val gradient: List<Color>, val icon: ImageVector)

@Composable
fun HomeScreen(onNavigate: (String) -> Unit) {
    val context = LocalContext.current
    val timings = remember { PrayerRepository.getCachedTimings(context) }

    var nextPrayerName by remember { mutableStateOf("...") }
    var nextPrayerTimeLabel by remember { mutableStateOf("") }
    var countdown by remember { mutableStateOf("٠٠:٠٠:٠٠") }
    var khatmaPercent by remember { mutableStateOf(0) }
    var streakCount by remember { mutableStateOf(0) }
    var lastReadName by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val sp = context.getSharedPreferences("noor_prefs", android.content.Context.MODE_PRIVATE)
        val readCount = (sp.getStringSet("khatma_read_surahs", emptySet()) ?: emptySet()).size
        khatmaPercent = ((readCount / 114f) * 100).toInt().coerceIn(0, 100)
        streakCount = sp.getInt("streak_count", 0)
        lastReadName = sp.getString("last_read_surah_name", "") ?: ""
    }

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
                nextPrayerTimeLabel = toArabicDigits(String.format("%02d", h12)) + ":" + toArabicDigits(String.format("%02d", tm)) + " " + ampm

                val target = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, th); set(Calendar.MINUTE, tm); set(Calendar.SECOND, 0)
                    if (timeInMillis <= now.timeInMillis) add(Calendar.DAY_OF_YEAR, 1)
                }
                val diff = ((target.timeInMillis - now.timeInMillis) / 1000).coerceAtLeast(0)
                countdown = toArabicDigits(String.format("%02d", diff / 3600)) + ":" +
                    toArabicDigits(String.format("%02d", (diff % 3600) / 60)) + ":" +
                    toArabicDigits(String.format("%02d", diff % 60))
            }
            delay(1000)
        }
    }

    // شبكة الوصول السريع — 12 خاصية بأيقونات ملوّنة مميّزة (نمط تطبيقات إسلامية عصرية عالمية)
    val grid = listOf(
        GridFeature("quran", "القرآن الكريم", "تلاوة . قراءة . تفسير", Icons.Filled.MenuBook, Color(0xFF4A90E2)),
        GridFeature("athkar", "الأذكار", "أذكار الصباح والمساء", Icons.Filled.Spa, Color(0xFF3FBF7F)),
        GridFeature("tasbih", "السبحة", "عدّاد التسبيح", Icons.Filled.RadioButtonUnchecked, Color(0xFF16A5A5)),
        GridFeature("tracker", "ورد القرآن", "متتبع الختمة", Icons.Filled.TaskAlt, Color(0xFFD9A441)),
        GridFeature("community", "المجتمع", "تواصل ومشاركة", Icons.Filled.Groups, Color(0xFFE0607E)),
        GridFeature("ai", "الذكاء الاصطناعي", "اسأل عن دينك", Icons.Filled.AutoAwesome, Color(0xFF7B6FE0)),
        GridFeature("calendar", "التقويم", "المناسبات الإسلامية", Icons.Filled.EditCalendar, Color(0xFFE0954E)),
        GridFeature("library", "الأناشيد", "موسيقى إسلامية", Icons.Filled.MusicNote, Color(0xFFB05FD9)),
        GridFeature("stories", "قصص الأنبياء", "عبر ودروس", Icons.Filled.HistoryEdu, Color(0xFF4EACD9)),
        GridFeature("quiz", "الاختبار الديني", "أسئلة وأجوبة", Icons.Filled.EmojiEvents, Color(0xFFD9C24E)),
        GridFeature("qibla", "القبلة", "اتجاه القبلة", Icons.Filled.Explore, Color(0xFF52C97A)),
        GridFeature("more", "المزيد", "خيارات أخرى", Icons.Filled.GridView, Color(0xFF9AA5B8))
    )

    val contentSpots = listOf(
        ContentSpot("athkar", "فضل الأذكار", listOf(Color(0xFF1B2B4B), Color(0xFF0B1424)), Icons.Filled.NightsStay),
        ContentSpot("ai", "كيف تكون أقرب إلى الله؟", listOf(Color(0xFF2B1B4B), Color(0xFF120B24)), Icons.Filled.AutoAwesome),
        ContentSpot("quran", "تفسير سورة الفاتحة", listOf(Color(0xFF1B3B3B), Color(0xFF0B1F1F)), Icons.Filled.MenuBook)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NoorGradients.ScreenBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // ============= البانر الرئيسي: المسجد + العنوان + آية + جرس =============
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(215.dp)
                .clip(RoundedCornerShape(24.dp))
        ) {
            AsyncImage(
                model = MOSQUE_IMAGE,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Black.copy(alpha = 0.30f), Color.Black.copy(alpha = 0.78f))
                        )
                    )
            )
            // جرس الإشعارات
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(14.dp)
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.35f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.NotificationsNone, contentDescription = "الإشعارات", tint = GoldSoft, modifier = Modifier.size(18.dp))
            }
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 14.dp, end = 16.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text("القرآن الكريم", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("رفيقك في طريق الهداية", color = GoldSoft, fontSize = 11.sp)
            }
            Text(
                "﴿ هُوَ الَّذِي أَنزَلَ عَلَيْكَ الْكِتَابَ مِنْهُ آيَاتٌ مُّحْكَمَاتٌ ﴾",
                color = Color.White.copy(alpha = 0.92f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.Center).padding(horizontal = 26.dp)
            )
            Text(
                "( سورة آل عمران — الآية ٧ )",
                color = GoldSoft.copy(alpha = 0.75f),
                fontSize = 9.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.Center).padding(top = 26.dp)
            )
        }

        Spacer(Modifier.height(14.dp))

        // ============= شريط مواقيت الصلاة =============
        Card(
            colors = CardDefaults.cardColors(containerColor = NavyCard),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.fillMaxWidth().clickable { onNavigate("prayer") }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Mosque, contentDescription = null, tint = Gold, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text("مواقيت الصلاة", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("المغرب ⚲", color = GoldSoft.copy(alpha = 0.7f), fontSize = 10.sp)
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(nextPrayerName, color = Gold, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text(nextPrayerTimeLabel, color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("يتبقى " + countdown, color = GoldSoft, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.Filled.ChevronLeft, contentDescription = null, tint = GoldSoft.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // ============= شبكة الوصول السريع 4×3 بأيقونات ملوّنة =============
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier.fillMaxWidth().height(320.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            gridItems(grid) { feature ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(NavyCard)
                        .clickable { onNavigate(feature.route) }
                        .padding(vertical = 12.dp, horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(
                                Brush.verticalGradient(listOf(feature.accent, feature.accent.copy(alpha = 0.65f)))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(feature.icon, contentDescription = feature.title, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        feature.title, color = Color.White, fontSize = 10.5.sp, fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center, maxLines = 1
                    )
                    Text(
                        feature.subtitle, color = GoldSoft.copy(alpha = 0.55f), fontSize = 8.5.sp,
                        textAlign = TextAlign.Center, maxLines = 1
                    )
                }
            }
        }

        Spacer(Modifier.height(18.dp))

        // ============= بطاقة استمرار التلاوة =============
        Card(
            colors = CardDefaults.cardColors(containerColor = NavyCard),
            shape = RoundedCornerShape(22.dp),
            modifier = Modifier.fillMaxWidth().clickable { onNavigate("quran") }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Brush.verticalGradient(listOf(Gold, Color(0xFFB8941F)))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.MenuBook, contentDescription = null, tint = Navy, modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("استمرار التلاوة", color = Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        if (lastReadName.isNotBlank()) "آخر قراءة: سورة $lastReadName" else "لم تبدأ القراءة بعد",
                        color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium, maxLines = 1
                    )
                    Spacer(Modifier.height(8.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)).background(NavyLight)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth(khatmaPercent / 100f).fillMaxHeight()
                                .clip(RoundedCornerShape(3.dp))
                                .background(Brush.horizontalGradient(listOf(Gold, GoldSoft)))
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text("أكملت " + toArabicDigits(khatmaPercent) + "٪" + if (streakCount > 0) "  🔥 " + toArabicDigits(streakCount) + " يوم متتالي" else "", color = GoldSoft.copy(alpha = 0.7f), fontSize = 10.sp)
                }
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier.size(38.dp).clip(CircleShape).background(Gold),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = "استمرار", tint = Navy, modifier = Modifier.size(20.dp))
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // ============= أحدث المحتويات — شريط أفقي =============
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.width(3.dp).height(16.dp).background(Gold, RoundedCornerShape(2.dp)))
            Spacer(Modifier.width(8.dp))
            Text("أحدث المحتويات", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(10.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(contentSpots) { spot ->
                Box(
                    modifier = Modifier
                        .width(150.dp)
                        .height(96.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Brush.verticalGradient(spot.gradient))
                        .clickable { onNavigate(spot.route) }
                ) {
                    Icon(
                        spot.icon, contentDescription = null, tint = Gold.copy(alpha = 0.35f),
                        modifier = Modifier.align(Alignment.TopEnd).padding(10.dp).size(28.dp)
                    )
                    Text(
                        spot.label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.BottomStart).padding(10.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(90.dp))
    }
}
