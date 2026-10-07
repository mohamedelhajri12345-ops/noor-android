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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
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
import com.elhajri.noor.data.QuizQuestion
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.Navy
import com.elhajri.noor.ui.NavyCard
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * "ميدان المعرفة" — السلم الذهبي التفاعلي المكون من 20 مرحلة إيمانية.
 * متدرج الصعوبة مع وسائل مساعدة إيمانية ونقاط مكافأة لمتاجر الثيمات.
 */
@Composable
fun TriviaLadderGameScreen(onBack: () -> Unit) {
    ForcePortraitOrientation()

    val context = LocalContext.current
    val allQuestions = remember { DataLoader.quizQuestions(context) }

    var currentLevelIdx by remember { mutableIntStateOf(0) }
    var inPlay by remember { mutableStateOf(false) }
    var refreshKey by remember { mutableIntStateOf(0) }

    val highestLevel = remember(refreshKey) { GameProgressStore.getHighestLevel(context, GameProgressStore.GAME_TRIVIA) }
    val totalStars = remember(refreshKey) { GameProgressStore.getTotalStars(context, GameProgressStore.GAME_TRIVIA, 20) }
    val totalScore = remember(refreshKey) { GameProgressStore.getTotalScore(context, GameProgressStore.GAME_TRIVIA) }
    val streak = remember(refreshKey) { GameProgressStore.getStreak(context, GameProgressStore.GAME_TRIVIA) }

    val totalLevels = 20

    Box(Modifier.fillMaxSize().background(Navy)) {
        Column(Modifier.fillMaxSize()) {
            // Header
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    if (inPlay) inPlay = false else onBack()
                }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = Gold)
                }
                Column(Modifier.weight(1f)) {
                    Text("ميدان المعرفة", color = Gold, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("المستوى ${currentLevelIdx + 1} / $totalLevels | ⭐ $totalStars | 🌙 $totalScore | 🔥 $streak أعياد", color = GoldSoft, fontSize = 11.sp)
                }
            }

            if (!inPlay) {
                // خريطة المراحل
                Text(
                    "درجات المعرفة السامية",
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
                    itemsIndexed(List(totalLevels) { it }) { index, _ ->
                        val isUnlocked = index <= highestLevel
                        val starsEarned = GameProgressStore.getLevelStars(context, GameProgressStore.GAME_TRIVIA, index)

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
                                    currentLevelIdx = index
                                    inPlay = true
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                if (isUnlocked) {
                                    Text("${index + 1}", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.height(2.dp))
                                    Row {
                                        for (s in 1..3) {
                                            Icon(
                                                Icons.Default.Star,
                                                contentDescription = null,
                                                tint = if (s <= starsEarned) Gold else Color.White.copy(alpha = 0.2f),
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
                TriviaLadderPlayScreen(
                    levelIdx = currentLevelIdx,
                    questionsPool = allQuestions,
                    onLevelComplete = { scoreGained, mistakes ->
                        val stars = when {
                            mistakes == 0 -> 3
                            mistakes == 1 -> 2
                            else -> 1
                        }
                        GameProgressStore.rewardPoints(context, GameProgressStore.GAME_TRIVIA, currentLevelIdx, stars, scoreGained)
                        refreshKey++
                    },
                    onNextLevel = {
                        if (currentLevelIdx + 1 < totalLevels) {
                            currentLevelIdx++
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
private fun TriviaLadderPlayScreen(
    levelIdx: Int,
    questionsPool: List<QuizQuestion>,
    onLevelComplete: (scoreGained: Int, mistakes: Int) -> Unit,
    onNextLevel: () -> Unit,
    onExit: () -> Unit
) {
    // Number of questions increases with level: 5 questions for lvl 0-4, 7 for 5-9, 10 for 10-19
    val questionCount = when {
        levelIdx < 5 -> 5
        levelIdx < 10 -> 7
        else -> 10
    }

    val ladderQuestions = remember(levelIdx) {
        val seed = (levelIdx * 37 + 13).toLong()
        questionsPool.shuffled(Random(seed)).take(questionCount)
    }

    var step by remember(levelIdx) { mutableIntStateOf(0) }
    var eliminated by remember(levelIdx) { mutableStateOf(setOf<Int>()) }
    var timeLeft by remember(levelIdx) { mutableIntStateOf(15) }
    var combo by remember(levelIdx) { mutableIntStateOf(1) }
    var score by remember(levelIdx) { mutableIntStateOf(0) }
    var mistakes by remember(levelIdx) { mutableIntStateOf(0) }
    var state by remember(levelIdx) { mutableStateOf("playing") } // playing | answered | done | fail
    var pickedIdx by remember(levelIdx) { mutableStateOf<Int?>(null) }
    var helpsUsed by remember(levelIdx) { mutableStateOf(setOf<String>()) }
    var wrongFlash by remember(levelIdx) { mutableStateOf<Int?>(null) }

    val q = ladderQuestions.getOrNull(step)

    LaunchedEffect(step, state) {
        if (state == "playing" && q != null) {
            timeLeft = 15
            while (timeLeft > 0 && state == "playing") {
                delay(1000)
                if (state == "playing") timeLeft--
            }
            if (state == "playing" && timeLeft <= 0) {
                mistakes++
                state = "fail"
            }
        }
    }

    fun answer(i: Int) {
        if (state != "playing" || i in eliminated) return
        pickedIdx = i
        if (i == q!!.correct) {
            state = "answered"
            val pointsAwarded = 100 * combo
            score += pointsAwarded
            combo = if (combo < 3) combo + 1 else 3
        } else {
            wrongFlash = i
            mistakes++
            state = "fail"
        }
    }

    fun useHelp(h: String) {
        if (h in helpsUsed || state != "playing") return
        helpsUsed = helpsUsed + h
        when (h) {
            "wisdom" -> {
                val wrongs = q!!.options.indices.filter { it != q.correct }.shuffled().take(2)
                eliminated = wrongs.toSet()
            }
            "light" -> timeLeft += 15
            "swap" -> {
                val pool = questionsPool.filterNot { it in ladderQuestions }
                if (pool.isNotEmpty()) {
                    val replacement = pool.random()
                    eliminated = emptySet()
                    timeLeft = 15
                }
            }
        }
    }

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        // Ladder progress indicator
        Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            for (s in 0 until questionCount) {
                Box(
                    Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (s < step) Gold else if (s == step) GoldSoft else Gold.copy(alpha = 0.15f))
                )
            }
        }

        if (q == null || state == "done") {
            Column(
                Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("🎉", fontSize = 44.sp)
                Text("ما شاء الله! أتممت السلم الذهبي 🌙", color = Gold, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("مجموع النقاط: $score | الأخطاء: $mistakes", color = GoldSoft, fontSize = 14.sp, modifier = Modifier.padding(top = 6.dp))
                Spacer(Modifier.height(20.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Gold.copy(alpha = 0.2f))
                            .border(1.dp, Gold, RoundedCornerShape(12.dp))
                            .clickable { onExit() }
                            .padding(horizontal = 24.dp, vertical = 10.dp)
                    ) {
                        Text("القائمة", color = Gold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Gold)
                            .clickable {
                                onLevelComplete(score, mistakes)
                                onNextLevel()
                            }
                            .padding(horizontal = 28.dp, vertical = 10.dp)
                    ) {
                        Text("المستوى التالي", color = Navy, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        } else {
            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(38.dp).clip(CircleShape).border(2.dp, if (timeLeft <= 5) Color(0xFFEF4444) else Gold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("$timeLeft", color = Gold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Spacer(Modifier.width(10.dp))
                Text("السؤال ${step + 1} من $questionCount", color = Gold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                HelpIconButton("wisdom", Icons.Default.AutoAwesome, "حذف خيارين", helpsUsed, enabled = state == "playing") { useHelp("wisdom") }
                HelpIconButton("light", Icons.Default.Lightbulb, "وقت إضافي", helpsUsed, enabled = state == "playing") { useHelp("light") }
                HelpIconButton("swap", Icons.Default.Refresh, "تبديل", helpsUsed, enabled = state == "playing") { useHelp("swap") }
            }

            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(NavyCard)
                    .border(1.dp, Gold.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    .padding(18.dp)
            ) {
                Text(
                    q.q, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()
                )
                if (q.ref.isNotBlank()) {
                    Text("﴿${q.ref}﴾", color = GoldSoft.copy(alpha = 0.75f), fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 6.dp))
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                q.options.forEachIndexed { i, opt ->
                    val isCorrect = i == q.correct
                    val bg = when {
                        state == "answered" && isCorrect -> Gold
                        state == "fail" && isCorrect -> Gold.copy(alpha = 0.6f)
                        wrongFlash == i -> Color(0xFF7F1D1D).copy(alpha = 0.5f)
                        i in eliminated -> Color.Transparent
                        else -> NavyCard
                    }
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(bg)
                            .border(1.dp, if (bg == Color.Transparent) Color.Transparent else Gold.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            .clickable(enabled = state == "playing" && i !in eliminated) { answer(i) }
                            .padding(14.dp)
                    ) {
                        Text(
                            opt,
                            color = when {
                                bg == Gold -> Navy
                                bg == Color.Transparent -> Color.White.copy(alpha = 0.15f)
                                else -> Color.White
                            },
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            if (state == "answered") {
                Spacer(Modifier.height(14.dp))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Gold)
                        .clickable {
                            if (step + 1 < ladderQuestions.size) {
                                step++
                                eliminated = emptySet()
                                state = "playing"
                                pickedIdx = null
                            } else {
                                state = "done"
                                onLevelComplete(score, mistakes)
                            }
                        }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(if (step + 1 < ladderQuestions.size) "السؤال التالي" else "إتمام السلم 🌙", color = Navy, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }

            if (state == "fail") {
                Spacer(Modifier.height(14.dp))
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("إجابة خاطئة — حاول مجدداً", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                    Spacer(Modifier.height(8.dp))
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, Gold, RoundedCornerShape(12.dp))
                            .clickable {
                                if (step + 1 < ladderQuestions.size) {
                                    step++
                                    eliminated = emptySet()
                                    state = "playing"
                                    pickedIdx = null
                                    wrongFlash = null
                                } else {
                                    state = "done"
                                    onLevelComplete(score, mistakes)
                                }
                            }
                            .padding(horizontal = 26.dp, vertical = 8.dp)
                    ) {
                        Text("متابعة", color = Gold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun HelpIconButton(
    id: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    used: Set<String>,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val usedUp = id in used
    IconButton(onClick = onClick, enabled = enabled && !usedUp) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = label, tint = if (usedUp) Color.White.copy(alpha = 0.2f) else Gold, modifier = Modifier.size(18.dp))
            Text(label, color = if (usedUp) Color.White.copy(alpha = 0.2f) else GoldSoft, fontSize = 8.sp)
        }
    }
}
