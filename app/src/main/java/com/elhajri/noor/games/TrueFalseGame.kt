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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
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

data class TFQuestion(val q: String, val a: Boolean)

@Composable
fun TrueFalseGameScreen(onBack: () -> Unit) {
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
            val arr = JSONObject(jsonStr).getJSONArray("trueFalseQuestions")
            List(arr.length()) { i ->
                val o = arr.getJSONObject(i)
                TFQuestion(o.getString("q"), o.getBoolean("a"))
            }
        } catch (_: Exception) {
            listOf(
                TFQuestion("عدد سور القرآن الكريم ١١٤ سورة", true),
                TFQuestion("عدد الصلوات المفروضة ٦ صلوات", false),
                TFQuestion("عدد أركان الإسلام ٥", true),
                TFQuestion("صوم رمضان ركن من أركان الإسلام", true),
                TFQuestion("الحج ركن من أركان الإيمان", false)
            )
        }
    }

    val totalLevels = (questions.size / 5).coerceAtLeast(20)

    var progressData by remember { mutableStateOf(NewGameProgress.load(context, "game_true_false")) }
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
                    Text("صح أم خطأ", color = Gold, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("اختبر معلوماتك الإيمانية مع ٢٤ مستواً", color = GoldSoft, fontSize = 11.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Gold, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(totalScore.toArabicDigits(), color = Gold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(12.dp))

            if (currentLevel == null) {
                // Level Map (24 levels)
                Text("المستويات والمحطات:", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(totalLevels) { idx ->
                        val lvlNum = idx + 1
                        val isUnlocked = lvlNum <= highestUnlocked
                        val isCompleted = lvlNum < highestUnlocked

                        Box(
                            Modifier
                                .height(72.dp)
                                .noorGlassCard(cornerRadius = 14.dp)
                                .clickable(enabled = isUnlocked) { currentLevel = lvlNum },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                if (isUnlocked) {
                                    Text("المستوى", color = GoldSoft, fontSize = 9.sp)
                                    Text(lvlNum.toArabicDigits(), color = Gold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    if (isCompleted) {
                                        Row {
                                            repeat(3) {
                                                Icon(Icons.Default.Star, contentDescription = null, tint = Gold, modifier = Modifier.size(9.dp))
                                            }
                                        }
                                    }
                                } else {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                                    Text(lvlNum.toArabicDigits(), color = Color.Gray, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            } else {
                // Gameplay for current level (5 questions per level)
                val lvl = currentLevel!!
                val levelQuestions = remember(lvl) {
                    val start = ((lvl - 1) * 5) % questions.size
                    val end = (start + 5).coerceAtMost(questions.size)
                    questions.subList(start, end)
                }

                var qIndex by remember(lvl) { mutableStateOf(0) }
                var levelScore by remember(lvl) { mutableStateOf(0) }
                var lives by remember(lvl) { mutableStateOf(3) }
                var selectedAnswer by remember(lvl, qIndex) { mutableStateOf<Boolean?>(null) }
                var isCompleted by remember(lvl) { mutableStateOf(false) }

                val currentQ = levelQuestions.getOrNull(qIndex)

                if (currentQ != null && !isCompleted) {
                    Column(
                        Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Level status bar
                        Row(
                            Modifier.fillMaxWidth().noorGlassCard(cornerRadius = 14.dp).padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("السؤال ${(qIndex + 1).toArabicDigits()}/٥", color = Gold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                repeat(lives) {
                                    Icon(Icons.Default.Favorite, contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp))
                                }
                            }
                            Text("النتيجة: ${(levelScore * 10).toArabicDigits()}", color = GoldSoft, fontSize = 12.sp)
                        }

                        Spacer(Modifier.height(24.dp))

                        // Question card
                        Card(
                            Modifier.fillMaxWidth().weight(1f).noorGlassCard(cornerRadius = 20.dp).padding(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                        ) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    currentQ.q,
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 32.sp
                                )
                            }
                        }

                        Spacer(Modifier.height(24.dp))

                        // True / False Buttons
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            // True Button
                            Button(
                                onClick = {
                                    if (selectedAnswer != null) return@Button
                                    selectedAnswer = true
                                    val isCorrect = (currentQ.a == true)
                                    if (isCorrect) levelScore += 1 else lives -= 1
                                },
                                modifier = Modifier.weight(1f).height(64.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selectedAnswer == true) {
                                        if (currentQ.a == true) Gold else Color.Red
                                    } else NavyCard
                                )
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = if (selectedAnswer == true) Navy else Gold)
                                Spacer(Modifier.width(8.dp))
                                Text("صـــح", color = if (selectedAnswer == true) Navy else Gold, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }

                            // False Button
                            Button(
                                onClick = {
                                    if (selectedAnswer != null) return@Button
                                    selectedAnswer = false
                                    val isCorrect = (currentQ.a == false)
                                    if (isCorrect) levelScore += 1 else lives -= 1
                                },
                                modifier = Modifier.weight(1f).height(64.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selectedAnswer == false) {
                                        if (currentQ.a == false) Gold else Color.Red
                                    } else NavyCard
                                )
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null, tint = if (selectedAnswer == false) Navy else Color.Red)
                                Spacer(Modifier.width(8.dp))
                                Text("خــطـأ", color = if (selectedAnswer == false) Navy else Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Transition to next question
                        LaunchedEffect(selectedAnswer) {
                            if (selectedAnswer != null) {
                                delay(1000)
                                if (qIndex + 1 < levelQuestions.size && lives > 0) {
                                    qIndex += 1
                                    selectedAnswer = null
                                } else {
                                    isCompleted = true
                                    val earnedPoints = levelScore * 20
                                    totalScore += earnedPoints
                                    if (levelScore >= 3 && lvl >= highestUnlocked) highestUnlocked = lvl + 1
                                    NewGameProgress.save(
                                        context,
                                        gameId = "game_true_false",
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
                                Text("اكتمل المستوى ${lvl.toArabicDigits()}!", color = Gold, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(8.dp))
                                Text("إجابات صحيحة: ${levelScore.toArabicDigits()}/٥", color = Color.White, fontSize = 16.sp)
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
