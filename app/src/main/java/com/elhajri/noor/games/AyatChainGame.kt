package com.elhajri.noor.games

import android.content.Context
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
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
import com.elhajri.noor.data.DataLoader
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.Navy
import com.elhajri.noor.ui.NavyCard
import kotlinx.coroutines.delay
import org.json.JSONObject

/**
 * "سلسلة الآيات" — ترتيب آيات السورة القصيرة في مواضعها الصحيحة.
 * لمس بطاقتين لتبديلهما؛ عند اكتمال الترتيب تلمع البطاقات ذهبياً
 * ويمكن سماع تلاوة السورة مكافأةً لتثبيت الحفظ.
 */
private data class AyatSurah(val num: Int, val name: String)

private val chainSurahs = listOf(
    AyatSurah(108, "الكوثر"), AyatSurah(103, "العصر"), AyatSurah(112, "الإخلاص"),
    AyatSurah(113, "الفلق"), AyatSurah(114, "الناس"), AyatSurah(105, "الفيل"),
    AyatSurah(106, "قريش"), AyatSurah(107, "الماعون"), AyatSurah(109, "الكافرون"),
    AyatSurah(110, "النصر"), AyatSurah(111, "المسد")
)

private fun loadAyahs(context: Context, num: Int): List<String> {
    return try {
        val json = context.assets.open("data/quran_full.json").bufferedReader().use { it.readText() }
        val surahs = org.json.JSONObject(json).getJSONArray("surahs")
        for (i in 0 until surahs.length()) {
            val s = surahs.getJSONObject(i)
            if (s.getInt("number") == num) {
                val ayahs = s.getJSONArray("ayahs")
                return List(ayahs.length()) { j -> ayahs.getJSONObject(j).getString("t").trim() }
            }
        }
        emptyList()
    } catch (_: Exception) { emptyList() }
}

@Composable
fun AyatChainGameScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var levelIdx by remember { mutableIntStateOf(0) }
    var completed by remember { mutableStateOf(setOf<Int>()) }
    var inPlay by remember { mutableStateOf(false) }

    val current = chainSurahs[levelIdx]

    Box(Modifier.fillMaxSize().background(Navy)) {
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { if (inPlay) inPlay = false else onBack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = Gold)
                }
                Column {
                    Text("سلسلة الآيات", color = Gold, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("رتّب الآيات في مواضعها الصحيحة", color = GoldSoft, fontSize = 11.sp)
                }
            }

            if (!inPlay) {
                // خريطة المراحل
                Text(
                    "اختر السورة",
                    color = GoldSoft, fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)
                )
                LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
                    items(chainSurahs.size) { i ->
                        val s = chainSurahs[i]
                        val done = i in completed
                        val locked = i > (completed.maxOrNull()?.plus(1) ?: 0)
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (done) Gold.copy(alpha = 0.12f) else NavyCard)
                                .border(1.dp, if (done) Gold.copy(alpha = 0.5f) else Gold.copy(alpha = 0.18f), RoundedCornerShape(14.dp))
                                .clickable(enabled = !locked) { levelIdx = i; inPlay = true }
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(if (done) "✓" else "﴿${s.num}﴾", color = Gold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.width(12.dp))
                            Text("سورة ${s.name}", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.weight(1f))
                            Text(if (locked) "🔒" else "العب", color = GoldSoft, fontSize = 12.sp)
                        }
                    }
                }
            } else {
                AyatChainPlay(
                    context = context,
                    surah = current,
                    onDone = {
                        completed = completed + levelIdx
                        if (levelIdx + 1 < chainSurahs.size) levelIdx = levelIdx + 1
                    },
                    onExit = { inPlay = false }
                )
            }
        }
    }
}

@Composable
private fun AyatChainPlay(context: Context, surah: AyatSurah, onDone: () -> Unit, onExit: () -> Unit) {
    val ayahs = remember(surah.num) { loadAyahs(context, surah.num) }
    var order by remember(surah.num) { mutableStateOf(ayahs.indices.shuffled().toMutableList()) }
    var selected by remember(surah.num) { mutableStateOf<Int?>(null) }
    var solved by remember(surah.num) { mutableStateOf(false) }
    var moves by remember(surah.num) { mutableIntStateOf(0) }

    val isCorrect = order == ayahs.indices.toList()

    LaunchedEffect(isCorrect) {
        if (isCorrect && ayahs.isNotEmpty() && !solved) {
            solved = true
            delay(900)
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            "سورة ${surah.name}",
            color = Gold, fontSize = 18.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center
        )
        Text(
            "المس البطاقتين لتبديلهما — حاول بأقل عدد من المحاولات",
            color = GoldSoft, fontSize = 11.sp,
            modifier = Modifier.fillMaxWidth().padding(top = 2.dp), textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(14.dp))

        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(order.size) { i ->
                val correctHere = order[i] == i && isCorrect
                val selectedNow = selected == i
                Box(
                    Modifier
                        .fillMaxWidth()
                        .animateContentSize()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            when {
                                isCorrect -> Color(0xFFD4AF37)
                                selectedNow -> Gold.copy(alpha = 0.22f)
                                else -> NavyCard
                            }
                        )
                        .border(
                            1.dp,
                            when {
                                isCorrect -> Gold
                                selectedNow -> Gold
                                else -> Gold.copy(alpha = 0.15f)
                            },
                            RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            if (isCorrect) return@clickable
                            val s = selected
                            if (s == null) selected = i
                            else if (s == i) selected = null
                            else {
                                moves++
                                order = order.toMutableList().also {
                                    val tmp = it[s]; it[s] = it[i]; it[i] = tmp
                                }
                                selected = null
                            }
                        }
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "${i + 1}",
                            color = if (isCorrect) Navy else Gold, fontWeight = FontWeight.Bold, fontSize = 12.sp
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            ayahs.getOrNull(order[i]) ?: "",
                            color = if (isCorrect) Navy else Color.White,
                            fontSize = if (ayahs.size > 5) 14.sp else 16.sp,
                            lineHeight = if (ayahs.size > 5) 22.sp else 26.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        if (isCorrect) {
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Text("ما شاء الله! الترتيب صحيح 🌙", color = Gold, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
            Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.Center) {
                Box(
                    Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Gold)
                        .clickable {
                            playSurahReward(context, surah)
                        }
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = Navy, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("استمع للتلاوة", color = Navy, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
                Spacer(Modifier.width(10.dp))
                Box(
                    Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, Gold, RoundedCornerShape(12.dp))
                        .clickable { onDone(); onExit() }
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("السورة التالية", color = Gold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        } else {
            Spacer(Modifier.height(6.dp))
            Text("المحاولات: $moves", color = GoldSoft, fontSize = 11.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        }
    }
}

/** تشغيل تلاوة السورة مكافأة عند إتمام الترتيب */
private fun playSurahReward(context: Context, surah: AyatSurah) {
    try {
        val reciters = DataLoader.reciters(context)
        val reciter = reciters.firstOrNull() ?: return
        val local = com.elhajri.noor.quran.ReciterOffline.localPathIfAny(context, reciter.id, surah.num)
        com.elhajri.noor.audio.player.QuranPlayerManager.playQueue(
            listOf(
                com.elhajri.noor.audio.player.PlayerTrack(
                    id = "quran-${surah.num}",
                    url = local ?: "${reciter.servers.first()}${String.format("%03d", surah.num)}.mp3",
                    title = "سورة ${surah.name}",
                    artist = reciter.name,
                    fallbackUrls = if (local != null) emptyList()
                    else reciter.servers.map { "${it}${String.format("%03d", surah.num)}.mp3" }
                )
            ), 0
        )
    } catch (_: Exception) {}
}
