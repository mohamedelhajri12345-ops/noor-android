package com.elhajri.noor.games

import android.content.Context
import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.Navy
import com.elhajri.noor.ui.NavyCard
import org.json.JSONObject
import kotlin.random.Random

/**
 * "مرآة الترتيل" — استرجاع الحفظ النشط: أكمل الكلمة المغطاة في الآية عبر 20 مرحلة إيمانية.
 */
private data class MirrorSurah(val num: Int, val name: String)

private val mirrorSurahs = listOf(
    MirrorSurah(112, "الإخلاص"), MirrorSurah(113, "الفلق"), MirrorSurah(114, "الناس"),
    MirrorSurah(108, "الكوثر"), MirrorSurah(103, "العصر"), MirrorSurah(105, "الفيل"),
    MirrorSurah(107, "الماعون"), MirrorSurah(109, "الكافرون"), MirrorSurah(110, "النصر"),
    MirrorSurah(101, "القارعة"), MirrorSurah(102, "التكاثر"), MirrorSurah(99, "الزلزلة"),
    MirrorSurah(106, "قريش"), MirrorSurah(111, "المسد"), MirrorSurah(104, "الهمزة"),
    MirrorSurah(100, "العاديات"), MirrorSurah(97, "القدر"), MirrorSurah(96, "العلق"),
    MirrorSurah(95, "التين"), MirrorSurah(94, "الشرح")
)

private data class BlankAyah(val ayah: String, val blanked: String, val correct: String, val options: List<String>)

private fun loadMirrorAyahs(context: Context, num: Int): List<BlankAyah> {
    return try {
        val json = context.assets.open("data/quran_full.json").bufferedReader().use { it.readText() }
        val surahs = JSONObject(json).getJSONArray("surahs")
        for (i in 0 until surahs.length()) {
            val s = surahs.getJSONObject(i)
            if (s.getInt("number") == num) {
                val texts = List(s.getJSONArray("ayahs").length()) { j ->
                    s.getJSONArray("ayahs").getJSONObject(j).getString("t").trim()
                }
                return texts.mapIndexedNotNull { idx, text ->
                    val words = text.split(" ").filter { it.isNotBlank() }
                    if (words.size < 3) return@mapIndexedNotNull null
                    val candidates = words.withIndex()
                        .filter { (wi, w) -> w.length >= 3 && words.count { it == w } == 1 }
                    val target = if (candidates.isEmpty()) return@mapIndexedNotNull null
                    else candidates[Random(num * 100 + idx).nextInt(candidates.size)]
                    val blanked = words.mapIndexed { wi, w -> if (wi == target.index) "▢" else w }.joinToString(" ")

                    val distractors = texts.flatMap { it.split(" ") }
                        .filter { it != target.value && it.length >= 3 }
                        .distinct()
                        .shuffled(Random(idx * 31 + num))
                        .take(3)
                    val options = (distractors + target.value).shuffled(Random(idx * 17 + 3))
                    BlankAyah(text, blanked, target.value, options)
                }
            }
        }
        emptyList()
    } catch (_: Exception) { emptyList() }
}

@Composable
fun TartilMirrorGameScreen(onBack: () -> Unit) {
    ForcePortraitOrientation()

    val context = LocalContext.current
    var surahIdx by remember { mutableIntStateOf(0) }
    var inPlay by remember { mutableStateOf(false) }
    var refreshKey by remember { mutableIntStateOf(0) }

    val totalLevels = mirrorSurahs.size
    val highestLevel = remember(refreshKey) { GameProgressStore.getHighestLevel(context, GameProgressStore.GAME_TARTIL) }
    val totalStars = remember(refreshKey) { GameProgressStore.getTotalStars(context, GameProgressStore.GAME_TARTIL, totalLevels) }
    val totalScore = remember(refreshKey) { GameProgressStore.getTotalScore(context, GameProgressStore.GAME_TARTIL) }
    val streak = remember(refreshKey) { GameProgressStore.getStreak(context, GameProgressStore.GAME_TARTIL) }

    Box(Modifier.fillMaxSize().background(Navy)) {
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { if (inPlay) inPlay = false else onBack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = Gold)
                }
                Column(Modifier.weight(1f)) {
                    Text("مرآة الترتيل", color = Gold, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("المستوى ${surahIdx + 1} / $totalLevels | ⭐ $totalStars | 🌙 $totalScore | 🔥 $streak أعياد", color = GoldSoft, fontSize = 11.sp)
                }
            }

            if (!inPlay) {
                Text(
                    "خريطة السور والتثبيت",
                    color = Gold, fontSize = 15.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    contentPadding = PaddingValues(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    itemsIndexed(mirrorSurahs) { index, s ->
                        val isUnlocked = index <= highestLevel
                        val starsEarned = GameProgressStore.getLevelStars(context, GameProgressStore.GAME_TARTIL, index)

                        Box(
                            Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isUnlocked) NavyCard else NavyCard.copy(alpha = 0.4f))
                                .border(
                                    1.dp,
                                    if (starsEarned > 0) Gold else if (isUnlocked) Gold.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.1f),
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable(enabled = isUnlocked) {
                                    surahIdx = index
                                    inPlay = true
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                if (isUnlocked) {
                                    Text("سورة ${s.name}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                                    Spacer(Modifier.height(2.dp))
                                    Row {
                                        for (st in 1..3) {
                                            Icon(
                                                Icons.Default.Star,
                                                contentDescription = null,
                                                tint = if (st <= starsEarned) Gold else Color.White.copy(alpha = 0.2f),
                                                modifier = Modifier.size(11.dp)
                                            )
                                        }
                                    }
                                } else {
                                    Icon(Icons.Default.Lock, contentDescription = "مقفل", tint = GoldSoft.copy(alpha = 0.4f), modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            } else {
                MirrorPlay(
                    context = context,
                    surah = mirrorSurahs[surahIdx],
                    levelIdx = surahIdx,
                    onDone = { errors ->
                        val stars = when {
                            errors == 0 -> 3
                            errors <= 2 -> 2
                            else -> 1
                        }
                        val scoreGained = (10 - errors).coerceAtLeast(2) * 15 + 30
                        GameProgressStore.rewardPoints(context, GameProgressStore.GAME_TARTIL, surahIdx, stars, scoreGained)
                        refreshKey++
                        if (surahIdx + 1 < mirrorSurahs.size) {
                            surahIdx++
                        } else {
                            inPlay = false
                        }
                    },
                    onExit = { inPlay = false }
                )
            }
        }
    }
}

@Composable
private fun MirrorPlay(
    context: Context,
    surah: MirrorSurah,
    levelIdx: Int,
    onDone: (errors: Int) -> Unit,
    onExit: () -> Unit
) {
    val blanks = remember(surah.num) { loadMirrorAyahs(context, surah.num) }
    var idx by remember(surah.num) { mutableIntStateOf(0) }
    var errors by remember(surah.num) { mutableIntStateOf(0) }
    var revealed by remember(surah.num) { mutableStateOf(false) }
    var finished by remember(surah.num) { mutableStateOf(false) }

    val current = blanks.getOrNull(idx)

    if (blanks.isEmpty()) {
        Text("تعذر تحميل السورة", color = GoldSoft, modifier = Modifier.padding(20.dp))
        return
    }

    fun pick(option: String) {
        if (revealed || finished) return
        if (option == current!!.correct) {
            revealed = true
        } else {
            errors++
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        if (!finished) {
            Text(
                "سورة ${surah.name} — الآية ${idx + 1} من ${blanks.size}",
                color = Gold, fontSize = 15.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center
            )
            Text(
                "الأخطاء: $errors",
                color = GoldSoft, fontSize = 11.sp,
                modifier = Modifier.fillMaxWidth().padding(top = 2.dp), textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))

            // الآية بالكلمة المغطاة
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(NavyCard)
                    .border(1.dp, Gold.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Text(
                    if (revealed) current!!.ayah else current!!.blanked,
                    color = Color.White,
                    fontSize = 21.sp,
                    lineHeight = 38.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(16.dp))

            if (revealed) {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("✓ إجابة صحيحة — «${current!!.correct}»", color = Gold, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Gold)
                            .clickable {
                                if (idx + 1 < blanks.size) {
                                    idx++
                                    revealed = false
                                } else {
                                    finished = true
                                }
                            }
                            .padding(horizontal = 30.dp, vertical = 10.dp)
                    ) {
                        Text(if (idx + 1 < blanks.size) "الآية التالية" else "إتمام السورة 🌙", color = Navy, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    current!!.options.forEach { opt ->
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(NavyCard)
                                .border(1.dp, Gold.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                .clickable { pick(opt) }
                                .padding(13.dp)
                        ) {
                            Text(opt, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            }
        } else {
            // النتيجة بالنجوم
            Column(Modifier.fillMaxSize().padding(top = 50.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                val stars = when {
                    errors == 0 -> 3
                    errors <= 2 -> 2
                    else -> 1
                }
                Text("⭐".repeat(stars) + "☆".repeat(3 - stars), fontSize = 32.sp)
                Spacer(Modifier.height(8.dp))
                Text("ما شاء الله!", color = Gold, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("أكملت سورة ${surah.name} بـ $errors أخطاء", color = GoldSoft, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
                Spacer(Modifier.height(24.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(NavyCard)
                            .border(1.dp, Gold, RoundedCornerShape(12.dp))
                            .clickable { onExit() }
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        Text("القائمة", color = Gold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Gold)
                            .clickable {
                                onDone(errors)
                            }
                            .padding(horizontal = 26.dp, vertical = 10.dp)
                    ) {
                        Text("المستوى التالي", color = Navy, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
