package com.elhajri.noor.games

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
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

data class NumQuestion(
    val q: String,
    val options: List<String>,
    val correct: Int
)

@Composable
fun NumbersQuizGameScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    // Lock orientation to portrait
    DisposableEffect(Unit) {
        val activity = context as? Activity
        val orig = activity?.requestedOrientation
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        onDispose {
            orig?.let { activity.requestedOrientation = it }
        }
    }

    val questions = remember {
        try {
            val jsonStr = context.assets.open("data/gameQuestions.json").bufferedReader().use { it.readText() }
            val arr = JSONObject(jsonStr).getJSONArray("numberQuestions")
            List(arr.length()) { i ->
                val o = arr.getJSONObject(i)
                val opts = o.getJSONArray("options")
                NumQuestion(
                    o.getString("q"),
                    List(opts.length()) { j -> opts.getString(j) },
                    o.getInt("correct")
                )
            }
        } catch (_: Exception) {
            listOf(
                NumQuestion("عدد الصلوات المفروضة", listOf("٣", "٤", "٥", "٦"), 2),
                NumQuestion("عدد أركان الإسلام", listOf("٤", "٥", "٦", "٧"), 1),
                NumQuestion("عدد أركان الإيمان", listOf("٥", "٦", "٧", "٨"), 1),
                NumQuestion("عدد سور القرآن", listOf("٩٩", "١٠٠", "١١٤", "١٢٠"), 2),
                NumQuestion("عدد أبواب الجنة", listOf("٦", "٧", "٨", "٩"), 2)
            )
        }
    }

    val totalLevels = (questions.size / 5).coerceAtLeast(15)

    var progressData by remember { mutableStateOf(NewGameProgress.load(context, "game_numbers_quiz")) }
    var currentLevel by remember { mutableStateOf<Int?>(null) }
    var totalScore by remember { mutableStateOf(progressData.score) }
    var highestUnlocked by remember { mutableStateOf(progressData.level) }

    Box(Modifier.fillMaxSize().themeScreenBackground()) {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            // Header
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = {
                    if (currentLevel != null) currentLevel = null else onBack()
                }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = Gold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("اختبار الأرقام", color = Gold, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("الأرقام والأعداد الإيمانية في الشريعة", color = GoldSoft, fontSize = 11.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Gold, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(totalScore.toArabicDigits(), color = Gold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(12.dp))

            if (currentLevel == null) {
                // Level Map (15+ levels)
                Text("المستويات والمراحل العدادية:", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(totalLevels) { idx ->
                        val lvlNum = idx + 1
                        val isUnlocked = lvlNum <= highestUnlocked
                        val isCompleted = lvlNum < highestUnlocked

                        Box(
                            Modifier
                                .height(80.dp)
                                .noorGlassCard(cornerRadius = 16.dp)
                                .clickable(enabled = isUnlocked) { currentLevel = lvlNum },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                if (isUnlocked) {
                                    Text("المستوى", color = GoldSoft, fontSize = 10.sp)
                                    Text(lvlNum.toArabicDigits(), color = Gold, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                    if (isCompleted) {
                                        Row {
                                            repeat(3) {
                                                Icon(Icons.Default.Star, contentDescription = null, tint = Gold, modifier = Modifier.size(11.dp))
                                            }
                                        }
                                    }
                                } else {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                                    Text(lvlNum.toArabicDigits(), color = Color.Gray, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            } else {
                // Gameplay for current level
                val lvl = currentLevel!!
                val levelQuestions = remember(lvl) {
                    val start = ((lvl - 1) * 5) % questions.size.coerceAtLeast(1)
                    val end = (start + 5).coerceAtMost(questions.size)
                    questions.subList(start, end)
                }

                var qIndex by remember(lvl) { mutableStateOf(0) }
                var levelScore by remember(lvl) { mutableStateOf(0) }
                var selectedOption by remember(lvl, qIndex) { mutableStateOf<Int?>(null) }
                var isCompleted by remember(lvl) { mutableStateOf(false) }

                val currentQ = levelQuestions.getOrNull(qIndex)

                if (currentQ != null && !isCompleted) {
                    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                        // Level status bar
                        Row(
                            Modifier.fillMaxWidth().noorGlassCard(cornerRadius = 14.dp).padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("المستوى ${lvl.toArabicDigits()}", color = Gold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("السؤال ${(qIndex + 1).toArabicDigits()}/٥", color = Color.White, fontSize = 14.sp)
                            Text("النقاط: ${(levelScore * 20).toArabicDigits()}", color = GoldSoft, fontSize = 12.sp)
                        }

                        Spacer(Modifier.height(18.dp))

                        // Question container
                        Card(
                            Modifier.fillMaxWidth().noorGlassCard(cornerRadius = 18.dp).padding(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                        ) {
                            Box(Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    currentQ.q,
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 28.sp
                                )
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        // Options List (4 choices)
                        Column(
                            Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            currentQ.options.forEachIndexed { optIdx, optText ->
                                val isSelected = selectedOption == optIdx
                                val isCorrect = optIdx == currentQ.correct

                                Box(
                                    Modifier
                                        .fillMaxWidth()
                                        .height(54.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(
                                            when {
                                                selectedOption != null && isCorrect -> Gold.copy(alpha = 0.3f)
                                                isSelected && !isCorrect -> Color.Red.copy(alpha = 0.3f)
                                                else -> NavyCard.copy(alpha = 0.6f)
                                            }
                                        )
                                        .border(
                                            1.dp,
                                            when {
                                                selectedOption != null && isCorrect -> Gold
                                                isSelected && !isCorrect -> Color.Red
                                                else -> Gold.copy(alpha = 0.25f)
                                            },
                                            RoundedCornerShape(14.dp)
                                        )
                                        .clickable(enabled = selectedOption == null) {
                                            selectedOption = optIdx
                                            if (isCorrect) levelScore += 1
                                        }
                                        .padding(horizontal = 16.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Row(
                                        Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(optText, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                        if (selectedOption != null) {
                                            if (isCorrect) {
                                                Icon(Icons.Default.Check, contentDescription = null, tint = Gold)
                                            } else if (isSelected) {
                                                Icon(Icons.Default.Close, contentDescription = null, tint = Color.Red)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Next transition
                        LaunchedEffect(selectedOption) {
                            if (selectedOption != null) {
                                delay(1000)
                                if (qIndex + 1 < levelQuestions.size) {
                                    qIndex += 1
                                    selectedOption = null
                                } else {
                                    isCompleted = true
                                    val pointsEarned = levelScore * 20
                                    totalScore += pointsEarned
                                    if (levelScore >= 3 && lvl >= highestUnlocked) highestUnlocked = lvl + 1
                                    NewGameProgress.save(
                                        context,
                                        gameId = "game_numbers_quiz",
                                        level = highestUnlocked,
                                        score = totalScore,
                                        stars = highestUnlocked * 3
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Level Completed summary
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Card(
                            Modifier.fillMaxWidth().noorGlassCard(cornerRadius = 20.dp).padding(24.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                        ) {
                            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Gold, modifier = Modifier.size(56.dp))
                                Spacer(Modifier.height(12.dp))
                                Text("انتهى المستوى ${lvl.toArabicDigits()}!", color = Gold, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(8.dp))
                                Text("النقاط المتحصلة: ${(levelScore * 20).toArabicDigits()}", color = Color.White, fontSize = 16.sp)
                                Spacer(Modifier.height(16.dp))

                                Button(
                                    onClick = {
                                        if (lvl < totalLevels) currentLevel = lvl + 1 else currentLevel = null
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Gold)
                                ) {
                                    Text("المستوى التالي", color = Navy, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
