package com.elhajri.noor.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WbSunny
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
import java.util.Calendar
import kotlinx.coroutines.delay

private const val MOSQUE_IMAGE = "https://media.base44.com/images/public/6a9ec57e3a8cd5ed957a8641/8bf461ac2_generated_image.png"

private fun toArabicDigits(value: Any): String {
    val map = mapOf('0' to '٠', '1' to '١', '2' to '٢', '3' to '٣', '4' to '٤', '5' to '٥', '6' to '٦', '7' to '٧', '8' to '٨', '9' to '٩')
    return value.toString().map { map[it] ?: it }.joinToString("")
}

private data class QuickItem(val route: String, val title: String, val icon: ImageVector)

@Composable
fun HomeScreen(onNavigate: (String) -> Unit) {
    val context = LocalContext.current
    val timings = remember { PrayerRepository.getCachedTimings(context) }

    var greeting by remember { mutableStateOf("طاب يومك") }
    var nextPrayerName by remember { mutableStateOf("...") }
    var nextPrayerTimeLabel by remember { mutableStateOf("") }
    var countdown by remember { mutableStateOf("٠٠:٠٠:٠٠") }

    LaunchedEffect(Unit) {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        greeting = when {
            hour < 5 -> "طاب مساؤك"
            hour < 12 -> "صباح الخير"
            hour < 18 -> "طاب يومك"
            else -> "مساء الخير"
        }
        while (true) {
            val t = timings
            if (t != null) {
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
                var found = false
                for ((name, time) in prayers) {
                    val parts = time.split(":")
                    val h = parts.getOrNull(0)?.trim()?.toIntOrNull() ?: continue
                    val m = parts.getOrNull(1)?.trim()?.toIntOrNull() ?: continue
                    if (h * 60 + m > currentMinutes) {
                        chosenName = name; chosenTime = time; found = true
                        break
                    }
                }
                nextPrayerName = chosenName
                val tp = chosenTime.split(":")
                val th = tp.getOrNull(0)?.trim()?.toIntOrNull() ?: 0
                val tm = tp.getOrNull(1)?.trim()?.toIntOrNull() ?: 0
                val h12 = if (th % 12 == 0) 12 else th % 12
                val ampm = if (th >= 12) "م" else "ص"
                nextPrayerTimeLabel = toArabicDigits(String.format("%02d", h12)) + ":" + toArabicDigits(String.format("%02d", tm)) + " " + ampm

                val target = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, th)
                    set(Calendar.MINUTE, tm)
                    set(Calendar.SECOND, 0)
                    if (timeInMillis <= now.timeInMillis) add(Calendar.DAY_OF_YEAR, 1)
                }
                val diff = ((target.timeInMillis - now.timeInMillis) / 1000).coerceAtLeast(0)
                val hh = diff / 3600
                val mm = (diff % 3600) / 60
                val ss = diff % 60
                countdown = toArabicDigits(String.format("%02d", hh)) + ":" +
                    toArabicDigits(String.format("%02d", mm)) + ":" +
                    toArabicDigits(String.format("%02d", ss))
            }
            delay(1000)
        }
    }

    val quickItems = listOf(
        QuickItem("quran", "القرآن", Icons.Filled.MenuBook),
        QuickItem("athkar", "الأذكار", Icons.Filled.WbSunny),
        QuickItem("tasbih", "السبحة", Icons.Filled.RadioButtonUnchecked),
        QuickItem("prayer", "الصلاة", Icons.Filled.Schedule),
        QuickItem("stories", "القصص", Icons.Filled.NightsStay),
        QuickItem("quiz", "الاختبار", Icons.Filled.EmojiEvents),
        QuickItem("library", "الأناشيد", Icons.Filled.MusicNote),
        QuickItem("names", "الأسماء", Icons.Filled.Star)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Navy)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Hero mosque banner with greeting, hijri date and next-prayer countdown
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(195.dp)
                .clip(RoundedCornerShape(22.dp))
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
                            listOf(Color.Black.copy(alpha = 0.20f), Color.Black.copy(alpha = 0.68f))
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(greeting, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text(timings?.hijriDate ?: "", color = GoldSoft, fontSize = 12.sp)
            }
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text("الصلاة القادمة", color = GoldSoft, fontSize = 11.sp)
                    Spacer(Modifier.height(2.dp))
                    Text(nextPrayerName, color = Gold, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                    Text(nextPrayerTimeLabel, color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(countdown, color = Gold, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text("الوقت المتبقي", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
                }
            }
        }

        Spacer(Modifier.height(22.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.width(3.dp).height(18.dp).background(Gold, RoundedCornerShape(2.dp)))
            Spacer(Modifier.width(8.dp))
            Text("الوصول السريع", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(12.dp))

        // Compact grid: 4 columns x 2 rows
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.height(190.dp),
            userScrollEnabled = false
        ) {
            items(quickItems) { item ->
                Card(
                    modifier = Modifier
                        .aspectRatio(0.95f)
                        .clickable {
                                                        onNavigate(item.route)
                        },
                    colors = CardDefaults.cardColors(containerColor = NavyCard),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(item.icon, contentDescription = item.title, tint = Gold, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.height(8.dp))
                        Text(item.title, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }

        Spacer(Modifier.height(22.dp))

        // Verse of the day card
        Card(
            colors = CardDefaults.cardColors(containerColor = NavyCard),
            shape = RoundedCornerShape(22.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("آية اليوم", color = Gold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                Text(
                    "لَا يُكَلِّفُ اللَّهُ نَفْسًا إِلَّا وُسْعَهَا",
                    color = Color.White,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(12.dp))
                Text("سورة البقرة — الآية ٢٨٦", color = GoldSoft, fontSize = 12.sp)
            }
        }
        Spacer(Modifier.height(80.dp))
    }
}
