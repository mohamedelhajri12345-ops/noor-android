package com.elhajri.noor.games

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.elhajri.noor.ui.TextMain
import com.elhajri.noor.ui.TextSecondaryLinen
import com.elhajri.noor.ui.noorGlassCard
import com.elhajri.noor.ui.themeScreenBackground

data class QuizItem(
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val fact: String
)


@Composable
fun GenericQuizGameScreen(
    gameTitle: String,
    questions: List<QuizItem>,
    onBack: () -> Unit
) {
    ForcePortraitOrientation()

    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("noor_games", Context.MODE_PRIVATE) }
    val totalStages = questions.size.coerceAtLeast(1)

    // Stage level state
    var bestStage by remember { mutableIntStateOf(1) }
    var starsList by remember { mutableStateOf(List(totalStages) { 0 }) }

    // Navigation screen state: 0 = STAGE_MAP, 1 = GAMEPLAY, 2 = CELEBRATION
    var currentScreen by remember { mutableIntStateOf(0) }
    var currentStageIndex by remember { mutableIntStateOf(0) }

    // Load progress from SharedPreferences
    LaunchedEffect(gameTitle, totalStages) {
        val key = "levels_$gameTitle"
        val raw = prefs.getString(key, null)
        if (raw != null) {
            val parts = raw.split(";")
            val savedBest = parts.getOrNull(0)?.toIntOrNull()?.coerceIn(1, totalStages + 1) ?: 1
            val savedStarsStr = parts.getOrNull(1)?.split(",") ?: emptyList()
            val savedStars = List(totalStages) { i ->
                savedStarsStr.getOrNull(i)?.toIntOrNull()?.coerceIn(0, 3) ?: 0
            }
            bestStage = savedBest
            starsList = savedStars
        } else {
            bestStage = 1
            starsList = List(totalStages) { 0 }
        }
    }

    fun saveProgress(newBestStage: Int, newStars: List<Int>) {
        bestStage = newBestStage
        starsList = newStars
        val key = "levels_$gameTitle"
        val raw = "$newBestStage;${newStars.joinToString(",")}"
        prefs.edit().putString(key, raw).apply()
    }

    // Stage gameplay state
    var selectedOptionIndex by remember(currentStageIndex, currentScreen) { mutableStateOf<Int?>(null) }
    var wrongAttempts by remember(currentStageIndex, currentScreen) { mutableStateOf<Set<Int>>(emptySet()) }
    var isStageCompleted by remember(currentStageIndex, currentScreen) { mutableStateOf(false) }
    var isStageFailed by remember(currentStageIndex, currentScreen) { mutableStateOf(false) }
    var earnedStars by remember(currentStageIndex, currentScreen) { mutableIntStateOf(0) }

    fun startStage(index: Int) {
        currentStageIndex = index
        selectedOptionIndex = null
        wrongAttempts = emptySet()
        isStageCompleted = false
        isStageFailed = false
        earnedStars = 0
        currentScreen = 1
    }

    val currentQ = questions.getOrNull(currentStageIndex)
    val totalStarsEarned = starsList.sum()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .themeScreenBackground()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = {
                    if (currentScreen != 0) {
                        currentScreen = 0
                    } else {
                        onBack()
                    }
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "رجوع",
                        tint = Gold
                    )
                }

                Text(
                    text = if (currentScreen == 1) "المرحلة ${(currentStageIndex + 1).toArabicDigits()}" else gameTitle,
                    color = Gold,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Gold,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = totalStarsEarned.toArabicDigits(),
                        color = Gold,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (currentScreen) {
                0 -> {
                    // Stage Map View
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .noorGlassCard(cornerRadius = 14.dp),
                        color = Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "خريطة المراحل (${totalStages.toArabicDigits()} مرحلة)",
                                color = TextMain,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "المفتوحة: ${(bestStage - 1).coerceAtMost(totalStages).toArabicDigits()} / ${totalStages.toArabicDigits()}",
                                color = GoldSoft,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(5),
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        itemsIndexed(questions) { index, _ ->
                            val stageNum = index + 1
                            val isUnlocked = stageNum <= bestStage
                            val stageStars = starsList.getOrElse(index) { 0 }

                            val cardBg = if (isUnlocked) NavyCard.copy(alpha = 0.8f) else NavyCard.copy(alpha = 0.3f)
                            val borderColor = if (isUnlocked) Gold else Color.Gray.copy(alpha = 0.3f)

                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(cardBg)
                                    .border(1.5.dp, borderColor, RoundedCornerShape(14.dp))
                                    .clickable(enabled = isUnlocked) {
                                        startStage(index)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    if (isUnlocked) {
                                        Text(
                                            text = stageNum.toArabicDigits(),
                                            color = Gold,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            for (s in 1..3) {
                                                Icon(
                                                    imageVector = Icons.Default.Star,
                                                    contentDescription = null,
                                                    tint = if (s <= stageStars) Gold else Color.Gray.copy(alpha = 0.4f),
                                                    modifier = Modifier.size(10.dp)
                                                )
                                            }
                                        }
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = null,
                                            tint = Color.Gray.copy(alpha = 0.6f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = stageNum.toArabicDigits(),
                                            color = Color.Gray,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (bestStage > totalStages) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { currentScreen = 2 },
                            colors = ButtonDefaults.buttonColors(containerColor = Gold),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text(
                                text = "عرض شاشة الاحتفال والإنجاز",
                                color = Navy,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                1 -> {
                    // Gameplay View
                    if (currentQ != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                        ) {
                            // Question Card
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .noorGlassCard(cornerRadius = 18.dp)
                                    .padding(20.dp)
                            ) {
                                Text(
                                    text = currentQ.question,
                                    color = TextMain,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            // Options
                            currentQ.options.forEachIndexed { index, optionText ->
                                val isWrong = wrongAttempts.contains(index)
                                val isCorrect = index == currentQ.correctIndex
                                val isSelected = selectedOptionIndex == index

                                val containerColor = when {
                                    isStageCompleted && isCorrect -> Color(0xFF1B5E20).copy(alpha = 0.8f)
                                    isWrong -> Color(0xFFB71C1C).copy(alpha = 0.7f)
                                    isSelected -> NavyCard
                                    else -> NavyCard.copy(alpha = 0.5f)
                                }

                                val borderColor = when {
                                    isStageCompleted && isCorrect -> Color(0xFF4CAF50)
                                    isWrong -> Color(0xFFEF5350)
                                    isSelected -> Gold
                                    else -> GoldSoft.copy(alpha = 0.2f)
                                }

                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 5.dp)
                                        .clickable(enabled = !isStageCompleted && !isStageFailed && !isWrong) {
                                            selectedOptionIndex = index
                                            if (index == currentQ.correctIndex) {
                                                isStageCompleted = true
                                                earnedStars = when (wrongAttempts.size) {
                                                    0 -> 3
                                                    1 -> 2
                                                    else -> 1
                                                }
                                                val updatedStars = starsList.toMutableList()
                                                updatedStars[currentStageIndex] = maxOf(
                                                    updatedStars[currentStageIndex],
                                                    earnedStars
                                                )
                                                val updatedBest = maxOf(bestStage, currentStageIndex + 2)
                                                    .coerceAtMost(totalStages + 1)
                                                saveProgress(updatedBest, updatedStars)
                                            } else {
                                                val newWrong = wrongAttempts + index
                                                wrongAttempts = newWrong
                                                if (newWrong.size >= 2) {
                                                    isStageFailed = true
                                                }
                                            }
                                        },
                                    shape = RoundedCornerShape(14.dp),
                                    color = containerColor,
                                    border = androidx.compose.foundation.BorderStroke(1.5.dp, borderColor)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = optionText,
                                            color = TextMain,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.weight(1f)
                                        )

                                        if (isStageCompleted && isCorrect) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = Color(0xFF4CAF50),
                                                modifier = Modifier.size(22.dp)
                                            )
                                        } else if (isWrong) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = null,
                                                tint = Color(0xFFEF5350),
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Fact / Explanation Box
                            if (selectedOptionIndex != null || wrongAttempts.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(16.dp))

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .noorGlassCard(cornerRadius = 14.dp)
                                        .border(
                                            1.dp,
                                            if (isStageFailed) Color(0xFFFFB74D).copy(alpha = 0.5f) else GoldSoft.copy(alpha = 0.3f),
                                            RoundedCornerShape(14.dp)
                                        )
                                        .padding(16.dp)
                                ) {
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Info,
                                                contentDescription = null,
                                                tint = if (isStageFailed) Color(0xFFFFB74D) else Gold,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = if (isStageFailed) "توضيح وفائدة:" else "معلومة ذات صلة:",
                                                color = if (isStageFailed) Color(0xFFFFB74D) else Gold,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = currentQ.fact,
                                            color = TextSecondaryLinen,
                                            fontSize = 13.sp,
                                            lineHeight = 18.sp
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            // Action Area
                            if (isStageCompleted) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .noorGlassCard(cornerRadius = 14.dp)
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "إجابة صحيحة! أحسنت ✨",
                                            color = Gold,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(horizontalArrangement = Arrangement.Center) {
                                            for (s in 1..3) {
                                                Icon(
                                                    imageVector = Icons.Default.Star,
                                                    contentDescription = null,
                                                    tint = if (s <= earnedStars) Gold else Color.Gray.copy(alpha = 0.4f),
                                                    modifier = Modifier.size(28.dp).padding(horizontal = 2.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Button(
                                                onClick = { currentScreen = 0 },
                                                colors = ButtonDefaults.buttonColors(containerColor = NavyCard),
                                                shape = RoundedCornerShape(12.dp),
                                                modifier = Modifier.weight(1f).height(46.dp)
                                            ) {
                                                Text("الخريطة", color = TextMain, fontSize = 14.sp)
                                            }

                                            Button(
                                                onClick = {
                                                    if (currentStageIndex + 1 < totalStages) {
                                                        startStage(currentStageIndex + 1)
                                                    } else {
                                                        currentScreen = 2
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Gold),
                                                shape = RoundedCornerShape(12.dp),
                                                modifier = Modifier.weight(1.5f).height(46.dp)
                                            ) {
                                                Text(
                                                    text = if (currentStageIndex + 1 < totalStages) "المرحلة التالية" else "إنهاء الاختبار",
                                                    color = Navy,
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            } else if (isStageFailed) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .noorGlassCard(cornerRadius = 14.dp)
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "لم تتجاوز المرحلة!",
                                            color = Color(0xFFEF5350),
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "استفد من المعلومة وحاول مرة أخرى",
                                            color = TextSecondaryLinen,
                                            fontSize = 13.sp
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Button(
                                                onClick = { currentScreen = 0 },
                                                colors = ButtonDefaults.buttonColors(containerColor = NavyCard),
                                                shape = RoundedCornerShape(12.dp),
                                                modifier = Modifier.weight(1f).height(46.dp)
                                            ) {
                                                Text("الخريطة", color = TextMain, fontSize = 14.sp)
                                            }

                                            Button(
                                                onClick = { startStage(currentStageIndex) },
                                                colors = ButtonDefaults.buttonColors(containerColor = Gold),
                                                shape = RoundedCornerShape(12.dp),
                                                modifier = Modifier.weight(1.5f).height(46.dp)
                                            ) {
                                                Text(
                                                    text = "إعادة المحاولة",
                                                    color = Navy,
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // Celebration Screen
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .noorGlassCard(cornerRadius = 22.dp)
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = Gold,
                                    modifier = Modifier.size(60.dp)
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "تهانينا! أكملت جميع المراحل بنجاح!",
                                    color = Gold,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Surface(
                                    color = NavyCard.copy(alpha = 0.6f),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, GoldSoft.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "مجموع النجوم المكتسبة:",
                                            color = TextSecondaryLinen,
                                            fontSize = 14.sp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "${totalStarsEarned.toArabicDigits()} / ${(totalStages * 3).toArabicDigits()}",
                                            color = Gold,
                                            fontSize = 26.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                Button(
                                    onClick = {
                                        saveProgress(1, List(totalStages) { 0 })
                                        currentScreen = 0
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Gold),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = null,
                                            tint = Navy,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "إعادة من جديد",
                                            color = Navy,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                TextButton(onClick = { currentScreen = 0 }) {
                                    Text("العودة إلى خريطة المراحل", color = GoldSoft, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
