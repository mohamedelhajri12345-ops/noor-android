package com.elhajri.noor.games

import android.content.Context
import androidx.compose.animation.animateContentSize
import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
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
import com.elhajri.noor.data.DataLoader
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.Navy
import com.elhajri.noor.ui.NavyCard
import kotlinx.coroutines.delay
import org.json.JSONObject

/**
 * "سلسلة الآيات" — ترتيب آيات السور في مواضعها الصحيحة (20 مستوى من قصار السور إلى أطولها).
 * لمس بطاقتين لتبديلهما؛ عند الترتيب تلمع البطاقات ذهبياً ويمكن الاستماع لتلاوة السورة.
 */
private data class AyatSurah(val num: Int, val name: String)

private val chainSurahs = listOf(
    AyatSurah(108, "الكوثر"), AyatSurah(103, "العصر"), AyatSurah(112, "الإخلاص"),
    AyatSurah(113, "الفلق"), AyatSurah(114, "الناس"), AyatSurah(105, "الفيل"),
    AyatSurah(106, "قريش"), AyatSurah(107, "الماعون"), AyatSurah(109, "الكافرون"),
    AyatSurah(110, "النصر"), AyatSurah(111, "المسد"), AyatSurah(101, "القارعة"),
    AyatSurah(102, "التكاثر"), AyatSurah(104, "الهمزة"), AyatSurah(100, "العاديات"),
    AyatSurah(99, "الزلزلة"), AyatSurah(98, "البينة"), AyatSurah(97, "القدر"),
    AyatSurah(96, "العلق"), AyatSurah(95, "التين")
)

private fun loadAyahs(context: Context, num: Int): List<String> {
    return try {
        val json = context.assets.open("data/quran_full.json").bufferedReader().use { it.readText() }
        val surahs = JSONObject(json).getJSONArray("surahs")
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
    ForcePortraitOrientation()

    val context = LocalContext.current
    var levelIdx by remember { mutableIntStateOf(0) }
    var inPlay by remember { mutableStateOf(false) }
    var refreshKey by remember { mutableIntStateOf(0) }

    val highestLevel = remember(refreshKey) { GameProgressStore.getHighestLevel(context, GameProgressStore.GAME_AYAT) }
    val totalStars = remember(refreshKey) { GameProgressStore.getTotalStars(context, GameProgressStore.GAME_AYAT, chainSurahs.size) }
    val totalScore = remember(refreshKey) { GameProgressStore.getTotalScore(context, GameProgressStore.GAME_AYAT) }
    val streak = remember(refreshKey) { GameProgressStore.getStreak(context, GameProgressStore.GAME_AYAT) }

    Box(Modifier.fillMaxSize().background(Navy)) {
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { if (inPlay) inPlay = false else onBack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = Gold)
                }
                Column(Modifier.weight(1f)) {
                    Text("سلسلة الآيات", color = Gold, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("المستوى ${levelIdx + 1} / ${chainSurahs.size} | ⭐ $totalStars | 🌙 $totalScore | 🔥 $streak أعياد", color = GoldSoft, fontSize = 11.sp)
                }
            }

            if (!inPlay) {
                Text(
                    "خريطة السور القرآنية",
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
                    itemsIndexed(chainSurahs) { index, s ->
                        val isUnlocked = index <= highestLevel
                        val starsEarned = GameProgressStore.getLevelStars(context, GameProgressStore.GAME_AYAT, index)

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
                                    levelIdx = index
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
                AyatChainPlay(
                    context = context,
                    surah = chainSurahs[levelIdx],
                    levelIdx = levelIdx,
                    onDone = { movesTaken ->
                        val stars = when {
                            movesTaken <= 2 -> 3
                            movesTaken <= 5 -> 2
                            else -> 1
                        }
                        val pts = (10 - movesTaken.coerceAtMost(8)) * 15 + 50
                        GameProgressStore.rewardPoints(context, GameProgressStore.GAME_AYAT, levelIdx, stars, pts)
                        refreshKey++
                        if (levelIdx + 1 < chainSurahs.size) {
                            levelIdx++
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
private fun AyatChainPlay(
    context: Context,
    surah: AyatSurah,
    levelIdx: Int,
    onDone: (movesTaken: Int) -> Unit,
    onExit: () -> Unit
) {
    val ayahs = remember(surah.num) { loadAyahs(context, surah.num) }
    var order by remember(surah.num) { mutableStateOf(ayahs.indices.shuffled().toMutableList()) }
    var selected by remember(surah.num) { mutableStateOf<Int?>(null) }
    var solved by remember(surah.num) { mutableStateOf(false) }
    var moves by remember(surah.num) { mutableIntStateOf(0) }

    val isCorrect = order == ayahs.indices.toList()

    LaunchedEffect(isCorrect) {
        if (isCorrect && ayahs.isNotEmpty() && !solved) {
            solved = true
            delay(800)
            playSurahReward(context, surah)
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            "سورة ${surah.name}",
            color = Gold, fontSize = 18.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center
        )
        Text(
            "المس البطاقتين لتبديل موضعهما حتى يصح ترتيب السورة",
            color = GoldSoft, fontSize = 11.sp,
            modifier = Modifier.fillMaxWidth().padding(top = 2.dp), textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))

        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(order.size) { i ->
                val selectedNow = selected == i
                Box(
                    Modifier
                        .fillMaxWidth()
                        .animateContentSize()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            when {
                                isCorrect -> Gold
                                selectedNow -> Gold.copy(alpha = 0.25f)
                                else -> NavyCard
                            }
                        )
                        .border(
                            1.dp,
                            when {
                                isCorrect -> Gold
                                selectedNow -> Gold
                                else -> Gold.copy(alpha = 0.18f)
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
                            color = if (isCorrect) Navy else Gold, fontWeight = FontWeight.Bold, fontSize = 13.sp
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
                Text("ما شاء الله! ترتيب صحيح 🌙", color = Gold, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(NavyCard)
                        .border(1.dp, Gold, RoundedCornerShape(12.dp))
                        .clickable { playSurahReward(context, surah) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "استماع", tint = Gold, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("التلاوة", color = Gold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
                Box(
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Gold)
                        .clickable { onDone(moves) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("السورة التالية", color = Navy, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        } else {
            Spacer(Modifier.height(6.dp))
            Text("المحاولات: $moves", color = GoldSoft, fontSize = 11.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        }
    }
}

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
