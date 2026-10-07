package com.elhajri.noor.games

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
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
import com.elhajri.noor.data.Surah
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.Navy
import com.elhajri.noor.ui.NavyCard
import com.elhajri.noor.ui.noorGlassCard
import com.elhajri.noor.ui.themeScreenBackground
import kotlinx.coroutines.delay
import org.json.JSONObject

private fun Int.toArabicDigits(): String {
    val arDigits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
    val str = this.toString()
    val builder = StringBuilder()
    for (ch in str) {
        if (ch in '0'..'9') builder.append(arDigits[ch - '0']) else builder.append(ch)
    }
    return builder.toString()
}

// Memorization status: 0 = NOT_STARTED, 1 = IN_PROGRESS, 2 = MEMORIZED
enum class MemStatus { NOT_STARTED, IN_PROGRESS, MEMORIZED }

@Composable
fun QuranMemorizationGameScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    // Lock orientation
    DisposableEffect(Unit) {
        val activity = context as? Activity
        val orig = activity?.requestedOrientation
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        onDispose {
            orig?.let { activity.requestedOrientation = it }
        }
    }

    val surahsList = remember { DataLoader.surahs(context) }
    var activeTab by remember { mutableStateOf(0) } // 0 = Surah Tracker, 1 = Level Quiz

    // Persistent status map stored as custom JSON string in NewGameProgress
    var progressData by remember { mutableStateOf(NewGameProgress.load(context, "game_quran_mem")) }

    // Map of surahNumber -> MemStatus
    val surahStatusMap = remember {
        mutableStateMapOf<Int, MemStatus>().apply {
            try {
                if (progressData.customData.isNotEmpty()) {
                    val obj = JSONObject(progressData.customData)
                    val keys = obj.keys()
                    while (keys.hasNext()) {
                        val k = keys.next()
                        val num = k.toIntOrNull()
                        val status = obj.getInt(k)
                        if (num != null) {
                            this[num] = when (status) {
                                1 -> MemStatus.IN_PROGRESS
                                2 -> MemStatus.MEMORIZED
                                else -> MemStatus.NOT_STARTED
                            }
                        }
                    }
                }
            } catch (_: Exception) {}
        }
    }

    fun saveSurahProgress() {
        try {
            val obj = JSONObject()
            surahStatusMap.forEach { (num, status) ->
                val code = when (status) {
                    MemStatus.IN_PROGRESS -> 1
                    MemStatus.MEMORIZED -> 2
                    MemStatus.NOT_STARTED -> 0
                }
                obj.put(num.toString(), code)
            }
            NewGameProgress.save(
                context,
                gameId = "game_quran_mem",
                level = progressData.level,
                score = progressData.score,
                stars = progressData.stars,
                customData = obj.toString()
            )
            progressData = progressData.copy(customData = obj.toString())
        } catch (_: Exception) {}
    }

    Box(Modifier.fillMaxSize().themeScreenBackground()) {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            // Header bar
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = Gold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("حفظ القرآن الكريم", color = Gold, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("تابع رحلة حفظك وتحدّ اختبارات الآيات", color = GoldSoft, fontSize = 11.sp)
                }
                Box(Modifier.size(24.dp))
            }

            Spacer(Modifier.height(12.dp))

            // Navigation Tabs
            Row(Modifier.fillMaxWidth().noorGlassCard(cornerRadius = 14.dp).padding(4.dp)) {
                Box(
                    Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (activeTab == 0) Gold else Color.Transparent)
                        .clickable { activeTab = 0 },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Book, contentDescription = null, tint = if (activeTab == 0) Navy else Gold, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("متابع الحفظ", color = if (activeTab == 0) Navy else Gold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Box(
                    Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (activeTab == 1) Gold else Color.Transparent)
                        .clickable { activeTab = 1 },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Quiz, contentDescription = null, tint = if (activeTab == 1) Navy else Gold, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("اختبارات الحفظ", color = if (activeTab == 1) Navy else Gold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            if (activeTab == 0) {
                // SURAH TRACKER MODE
                val memorizedCount = surahStatusMap.values.count { it == MemStatus.MEMORIZED }
                val inProgressCount = surahStatusMap.values.count { it == MemStatus.IN_PROGRESS }
                val overallPercent = ((memorizedCount.toFloat() / surahsList.size) * 100).toInt()

                var searchQuery by remember { mutableStateOf("") }
                var selectedJuzFilter by remember { mutableStateOf(0) } // 0 = All, 1 = Amma, 2 = Tabarak

                val filteredSurahs = remember(searchQuery, selectedJuzFilter, surahsList) {
                    surahsList.filter { s ->
                        val matchesSearch = s.name.contains(searchQuery) || s.number.toString().contains(searchQuery)
                        val matchesJuz = when (selectedJuzFilter) {
                            1 -> s.number in 78..114
                            2 -> s.number in 67..77
                            else -> true
                        }
                        matchesSearch && matchesJuz
                    }
                }

                // Summary Stats
                Card(
                    Modifier.fillMaxWidth().noorGlassCard(cornerRadius = 16.dp).padding(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Column(Modifier.fillMaxWidth()) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("التقدم الكلي في الحفظ", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("$overallPercent٪", color = Gold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { overallPercent / 100f },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = Gold,
                            trackColor = NavyCard
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(memorizedCount.toArabicDigits(), color = Gold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Text("محفوظة", color = GoldSoft, fontSize = 10.sp)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(inProgressCount.toArabicDigits(), color = Color.Yellow, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Text("قيد الحفظ", color = GoldSoft, fontSize = 10.sp)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text((114 - memorizedCount - inProgressCount).toArabicDigits(), color = Color.Gray, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Text("لم تبدأ", color = GoldSoft, fontSize = 10.sp)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                // Search & Filters
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.weight(1f).height(48.dp),
                        placeholder = { Text("بحث عن سورة...", color = Color.Gray, fontSize = 12.sp) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = NavyCard,
                            unfocusedContainerColor = NavyCard,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(Modifier.height(10.dp))

                // Surahs Scroll List
                LazyColumn(
                    Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredSurahs) { surah ->
                        val currentStatus = surahStatusMap[surah.number] ?: MemStatus.NOT_STARTED

                        Box(
                            Modifier
                                .fillMaxWidth()
                                .noorGlassCard(cornerRadius = 12.dp)
                                .clickable {
                                    val nextStatus = when (currentStatus) {
                                        MemStatus.NOT_STARTED -> MemStatus.IN_PROGRESS
                                        MemStatus.IN_PROGRESS -> MemStatus.MEMORIZED
                                        MemStatus.MEMORIZED -> MemStatus.NOT_STARTED
                                    }
                                    surahStatusMap[surah.number] = nextStatus
                                    saveSurahProgress()
                                }
                                .padding(12.dp)
                        ) {
                            Row(
                                Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        Modifier.size(36.dp).clip(CircleShape).background(Gold.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(surah.number.toArabicDigits(), color = Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(Modifier.width(10.dp))
                                    Column {
                                        Text("سورة ${surah.name}", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                        Text("${surah.type} · ${surah.ayahs.toArabicDigits()} آية", color = GoldSoft, fontSize = 11.sp)
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    when (currentStatus) {
                                        MemStatus.MEMORIZED -> {
                                            Box(
                                                Modifier.clip(RoundedCornerShape(8.dp)).background(Gold.copy(alpha = 0.2f)).padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Gold, modifier = Modifier.size(14.dp))
                                                    Spacer(Modifier.width(4.dp))
                                                    Text("تم الحفظ", color = Gold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                        MemStatus.IN_PROGRESS -> {
                                            Box(
                                                Modifier.clip(RoundedCornerShape(8.dp)).background(Color.Yellow.copy(alpha = 0.2f)).padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.Schedule, contentDescription = null, tint = Color.Yellow, modifier = Modifier.size(14.dp))
                                                    Spacer(Modifier.width(4.dp))
                                                    Text("قيد الحفظ", color = Color.Yellow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                        MemStatus.NOT_STARTED -> {
                                            Text("اضغط للتسجيل", color = Color.Gray, fontSize = 10.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // LEVEL QUIZ MODE (15 Levels)
                var currentQuizLevel by remember { mutableStateOf<Int?>(null) }

                if (currentQuizLevel == null) {
                    Text("اختبارات أجزاء وسور القرآن (١٥ مستواً):", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(15) { idx ->
                            val lvlNum = idx + 1
                            val isUnlocked = lvlNum <= progressData.level
                            Box(
                                Modifier
                                    .height(76.dp)
                                    .noorGlassCard(cornerRadius = 14.dp)
                                    .clickable(enabled = isUnlocked) { currentQuizLevel = lvlNum },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    if (isUnlocked) {
                                        Text("المستوى", color = GoldSoft, fontSize = 10.sp)
                                        Text(lvlNum.toArabicDigits(), color = Gold, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                    } else {
                                        Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                                        Text(lvlNum.toArabicDigits(), color = Color.Gray, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    val lvl = currentQuizLevel!!
                    var qIndex by remember(lvl) { mutableStateOf(0) }
                    var score by remember(lvl) { mutableStateOf(0) }
                    var isCompleted by remember(lvl) { mutableStateOf(false) }

                    val sampleQuestions = remember(lvl) {
                        listOf(
                            Triple("أي سورة تسمى عروس القرآن؟", listOf("سورة الرحمن", "سورة يس", "سورة الملك", "سورة الواقعة"), 0),
                            Triple("ما هي أطول سورة في القرآن الكريم؟", listOf("سورة آل عمران", "سورة البقرة", "سورة النساء", "سورة المائدة"), 1),
                            Triple("ما هي أقصر سورة في القرآن الكريم؟", listOf("سورة الإخلاص", "سورة الكوثر", "سورة النصر", "سورة الفلق"), 1),
                            Triple("كم عدد سور القرآن الكريم؟", listOf("١١٠", "١١٢", "١١٤", "١٢٠"), 2),
                            Triple("في أي سورة توجد آية الكرسي؟", listOf("سورة البقرة", "سورة آل عمران", "سورة النساء", "سورة المائدة"), 0)
                        )
                    }

                    val currentQ = sampleQuestions.getOrNull(qIndex)

                    if (currentQ != null && !isCompleted) {
                        var selectedOpt by remember(lvl, qIndex) { mutableStateOf<Int?>(null) }

                        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("اختبار المستوى ${lvl.toArabicDigits()}", color = Gold, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(16.dp))

                            Card(
                                Modifier.fillMaxWidth().noorGlassCard(cornerRadius = 18.dp).padding(20.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                            ) {
                                Text(currentQ.first, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                            }

                            Spacer(Modifier.height(16.dp))

                            currentQ.second.forEachIndexed { optIdx, optText ->
                                val isCorrect = optIdx == currentQ.third
                                val isSel = selectedOpt == optIdx

                                Box(
                                    Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            when {
                                                selectedOpt != null && isCorrect -> Gold.copy(alpha = 0.3f)
                                                isSel && !isCorrect -> Color.Red.copy(alpha = 0.3f)
                                                else -> NavyCard.copy(alpha = 0.8f)
                                            }
                                        )
                                        .clickable(enabled = selectedOpt == null) {
                                            selectedOpt = optIdx
                                            if (isCorrect) score += 1
                                        }
                                        .padding(horizontal = 14.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Text(optText, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(Modifier.height(8.dp))
                            }

                            LaunchedEffect(selectedOpt) {
                                if (selectedOpt != null) {
                                    delay(1000)
                                    if (qIndex + 1 < sampleQuestions.size) {
                                        qIndex += 1
                                        selectedOpt = null
                                    } else {
                                        isCompleted = true
                                        val newUnlocked = if (score >= 3 && lvl >= progressData.level) lvl + 1 else progressData.level
                                        val newScore = progressData.score + (score * 20)
                                        progressData = progressData.copy(level = newUnlocked, score = newScore)
                                        NewGameProgress.save(
                                            context,
                                            gameId = "game_quran_mem",
                                            level = newUnlocked,
                                            score = newScore,
                                            stars = newUnlocked * 3,
                                            customData = progressData.customData
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Gold, modifier = Modifier.size(52.dp))
                                Spacer(Modifier.height(10.dp))
                                Text("انتهى الاختبار!", color = Gold, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(6.dp))
                                Text("النتيجة: ${score.toArabicDigits()}/٥", color = Color.White, fontSize = 16.sp)
                                Spacer(Modifier.height(16.dp))
                                Button(
                                    onClick = { currentQuizLevel = null },
                                    colors = ButtonDefaults.buttonColors(containerColor = Gold)
                                ) {
                                    Text("عودة للمستويات", color = Navy, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
