package com.elhajri.noor.games

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
 * "مرآة الترتيل" — استرجاع الحفظ النشط: كلمة مغطاة داخل الآية،
 * والمس الكلمة الصحيحة من الخيارات. تقييم بالنجوم حسب الدقة.
 */
private data class MirrorSurah(val num: Int, val name: String)

private val mirrorSurahs = listOf(
    MirrorSurah(112, "الإخلاص"), MirrorSurah(113, "الفلق"), MirrorSurah(114, "الناس"),
    MirrorSurah(108, "الكوثر"), MirrorSurah(103, "العصر"), MirrorSurah(105, "الفيل"),
    MirrorSurah(107, "الماعون"), MirrorSurah(109, "الكافرون"), MirrorSurah(110, "النصر"),
    MirrorSurah(101, "القارعة"), MirrorSurah(102, "التكاثر"), MirrorSurah(99, "الزلزلة")
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
                    if (words.size < 4) return@mapIndexedNotNull null
                    // كلمة مميزة قابلة للتخمين (طويلة نسبياً وغير مكررة في الآية)
                    val candidates = words.withIndex()
                        .filter { (wi, w) -> w.length >= 4 && words.count { it == w } == 1 && wi != 0 }
                    val target = if (candidates.isEmpty()) return@mapIndexedNotNull null
                        else candidates[Random(num * 100 + idx).nextInt(candidates.size)]
                    val blanked = words.mapIndexed { wi, w -> if (wi == target.index) "▢" else w }.joinToString(" ")
                    // خيارات مشتتة من كلمات آيات أخرى في السورة
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
    val context = LocalContext.current
    var surahIdx by remember { mutableIntStateOf(0) }
    var inPlay by remember { mutableStateOf(false) }
    var completed by remember { mutableStateOf(setOf<Int>()) }

    val current = mirrorSurahs[surahIdx]

    Box(Modifier.fillMaxSize().background(Navy)) {
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { if (inPlay) inPlay = false else onBack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = Gold)
                }
                Column {
                    Text("مرآة الترتيل", color = Gold, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("أكمل الكلمة المغطاة في الآية", color = GoldSoft, fontSize = 11.sp)
                }
            }

            if (!inPlay) {
                Text("اختر السورة", color = GoldSoft, fontSize = 14.sp, modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp))
                LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
                    items(mirrorSurahs.size) { i ->
                        val done = i in completed
                        val locked = i > (completed.maxOrNull()?.plus(1) ?: 0)
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (done) Gold.copy(alpha = 0.12f) else NavyCard)
                                .border(1.dp, if (done) Gold.copy(alpha = 0.5f) else Gold.copy(alpha = 0.18f), RoundedCornerShape(14.dp))
                                .clickable(enabled = !locked) { surahIdx = i; inPlay = true }
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(if (done) "✓" else "﴿${mirrorSurahs[i].num}﴾", color = Gold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.width(12.dp))
                            Text("سورة ${mirrorSurahs[i].name}", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.weight(1f))
                            Text(if (locked) "🔒" else "العب", color = GoldSoft, fontSize = 12.sp)
                        }
                    }
                }
            } else {
                MirrorPlay(
                    context = context,
                    surah = current,
                    onDone = { completed = completed + surahIdx; if (surahIdx + 1 < mirrorSurahs.size) surahIdx++ },
                    onExit = { inPlay = false }
                )
            }
        }
    }
}

@Composable
private fun MirrorPlay(context: Context, surah: MirrorSurah, onDone: () -> Unit, onExit: () -> Unit) {
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
                "سورة ${surah.name} — الآية ${idx + 1}",
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
                    fontSize = 22.sp,
                    lineHeight = 40.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(16.dp))

            if (revealed) {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("✓ صحيح — «${current!!.correct}»", color = Gold, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(10.dp))
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Gold)
                            .clickable {
                                if (idx + 1 < blanks.size) { idx++; revealed = false }
                                else finished = true
                            }
                            .padding(horizontal = 30.dp, vertical = 10.dp)
                    ) {
                        Text(if (idx + 1 < blanks.size) "الآية التالية" else "إتمام السورة", color = Navy, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            } else {
                Text("اختر الكلمة الناقصة:", color = GoldSoft, fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
            Column(Modifier.fillMaxSize().padding(top = 60.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                val stars = when {
                    errors == 0 -> 3
                    errors <= 2 -> 2
                    else -> 1
                }
                Text("⭐".repeat(stars) + "☆".repeat(3 - stars), fontSize = 30.sp)
                Text("ما شاء الله!", color = Gold, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                Text("أكملت سورة ${surah.name} بـ $errors أخطاء", color = GoldSoft, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
                Spacer(Modifier.height(20.dp))
                Box(
                    Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Gold)
                        .clickable { onDone(); onExit() }
                        .padding(horizontal = 30.dp, vertical = 10.dp)
                ) {
                    Text("السورة التالية", color = Navy, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}
