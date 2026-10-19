package com.elhajri.noor.games

import android.content.Context
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
import kotlinx.coroutines.delay
import kotlin.random.Random

data class MatchCard(
    val id: Int,
    val pairId: Int,
    val text: String,
    val categoryLabel: String
)


@Composable
fun MemoryMatchEngine(
    gameTitle: String,
    items: List<Pair<String, String>>,
    frontLabel: String,
    backLabel: String,
    onBack: () -> Unit
) {
    ForcePortraitOrientation()

    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("noor_games", Context.MODE_PRIVATE) }
    val totalStages = 20

    // Level progress state
    var bestStage by remember { mutableIntStateOf(1) }
    var starsList by remember { mutableStateOf(List(totalStages) { 0 }) }

    // Screen state: 0 = STAGE_MAP, 1 = GAMEPLAY, 2 = CELEBRATION
    var currentScreen by remember { mutableIntStateOf(0) }
    var currentStageIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(gameTitle) {
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

    fun getStagePairCount(stageIdx: Int): Int {
        if (items.isEmpty()) return 1
        val target = when {
            stageIdx < 5 -> 6
            stageIdx < 12 -> 8
            stageIdx < 16 -> 10
            else -> 12
        }
        return target.coerceAtMost(items.size)
    }

    fun createCardsForStage(stageIdx: Int): List<MatchCard> {
        val pairCount = getStagePairCount(stageIdx)
        if (items.isEmpty()) return emptyList()
        val rng = Random(stageIdx * 31 + gameTitle.hashCode())
        val selectedPairs = items.shuffled(rng).take(pairCount)
        val list = mutableListOf<MatchCard>()
        selectedPairs.forEachIndexed { pIdx, pair ->
            list.add(MatchCard(id = pIdx * 2, pairId = pIdx, text = pair.first, categoryLabel = frontLabel))
            list.add(MatchCard(id = pIdx * 2 + 1, pairId = pIdx, text = pair.second, categoryLabel = backLabel))
        }
        return list.shuffled(Random(stageIdx * 17 + 42))
    }

    // Gameplay states
    var cards by remember(currentStageIndex, currentScreen) { mutableStateOf(createCardsForStage(currentStageIndex)) }
    var flippedCardIds by remember(currentStageIndex, currentScreen) { mutableStateOf<List<Int>>(emptyList()) }
    var matchedCardIds by remember(currentStageIndex, currentScreen) { mutableStateOf<Set<Int>>(emptySet()) }
    var moves by remember(currentStageIndex, currentScreen) { mutableIntStateOf(0) }
    var isChecking by remember(currentStageIndex, currentScreen) { mutableStateOf(false) }

    fun startStage(stageIdx: Int) {
        currentStageIndex = stageIdx
        cards = createCardsForStage(stageIdx)
        flippedCardIds = emptyList()
        matchedCardIds = emptySet()
        moves = 0
        isChecking = false
        currentScreen = 1
    }

    val pairCount = getStagePairCount(currentStageIndex)
    val totalCardCount = pairCount * 2
    val isStageCompleted = matchedCardIds.size == totalCardCount && totalCardCount > 0

    val earnedStars = remember(moves, pairCount, isStageCompleted) {
        if (!isStageCompleted) 0
        else when {
            moves <= pairCount + 2 -> 3
            moves <= pairCount + 6 -> 2
            else -> 1
        }
    }

    LaunchedEffect(isStageCompleted) {
        if (isStageCompleted && currentScreen == 1) {
            val updatedStars = starsList.toMutableList()
            updatedStars[currentStageIndex] = maxOf(updatedStars[currentStageIndex], earnedStars)
            val updatedBest = maxOf(bestStage, currentStageIndex + 2).coerceAtMost(totalStages + 1)
            saveProgress(updatedBest, updatedStars)
        }
    }

    LaunchedEffect(flippedCardIds) {
        if (flippedCardIds.size == 2 && currentScreen == 1) {
            isChecking = true
            moves += 1
            val card1 = cards.firstOrNull { it.id == flippedCardIds[0] }
            val card2 = cards.firstOrNull { it.id == flippedCardIds[1] }

            if (card1 != null && card2 != null && card1.pairId == card2.pairId) {
                matchedCardIds = matchedCardIds + setOf(card1.id, card2.id)
                flippedCardIds = emptyList()
                isChecking = false
            } else {
                delay(900)
                flippedCardIds = emptyList()
                isChecking = false
            }
        }
    }

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
                    fontSize = 18.sp,
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
                        items(totalStages) { index ->
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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .noorGlassCard(cornerRadius = 14.dp)
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "المحاولات: ${moves.toArabicDigits()}",
                            color = TextMain,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "المتطابقة: ${(matchedCardIds.size / 2).toArabicDigits()} / ${pairCount.toArabicDigits()}",
                            color = GoldSoft,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (isStageCompleted) {
                        // Stage Success Card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .noorGlassCard(cornerRadius = 18.dp)
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "مبروك! تمت المطابقة بنجاح ✨",
                                    color = Gold,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Row(horizontalArrangement = Arrangement.Center) {
                                    for (i in 1..3) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = if (i <= earnedStars) Gold else Color.Gray.copy(alpha = 0.4f),
                                            modifier = Modifier.size(36.dp).padding(horizontal = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = "عدد المحاولات: ${moves.toArabicDigits()}",
                                    color = TextMain,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(24.dp))

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
                                            text = if (currentStageIndex + 1 < totalStages) "المرحلة التالية" else "إنهاء التحدي",
                                            color = Navy,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // Cards Grid
                        val columnsCount = if (cards.size <= 12) 3 else 4

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(columnsCount),
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(cards) { card ->
                                val isFlipped = flippedCardIds.contains(card.id)
                                val isMatched = matchedCardIds.contains(card.id)

                                val rotation by animateFloatAsState(
                                    targetValue = if (isFlipped || isMatched) 180f else 0f,
                                    animationSpec = tween(durationMillis = 400),
                                    label = "cardFlip"
                                )

                                Box(
                                    modifier = Modifier
                                        .aspectRatio(0.82f)
                                        .graphicsLayer {
                                            rotationY = rotation
                                            cameraDistance = 12f * density
                                        }
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            when {
                                                isMatched -> Color(0xFF2E7D32).copy(alpha = 0.8f)
                                                isFlipped -> NavyCard
                                                else -> Gold.copy(alpha = 0.25f)
                                            }
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = when {
                                                isMatched -> Color(0xFF4CAF50)
                                                isFlipped -> Gold
                                                else -> GoldSoft.copy(alpha = 0.4f)
                                            },
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable(
                                            enabled = !isFlipped && !isMatched && !isChecking && flippedCardIds.size < 2
                                        ) {
                                            flippedCardIds = flippedCardIds + card.id
                                        }
                                        .padding(4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (rotation > 90f) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .graphicsLayer { rotationY = 180f },
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = card.categoryLabel,
                                                color = GoldSoft,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Normal
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = card.text,
                                                color = TextMain,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.Center,
                                                lineHeight = 15.sp
                                            )
                                        }
                                    } else {
                                        Text(
                                            text = "📖",
                                            fontSize = 20.sp,
                                            textAlign = TextAlign.Center
                                        )
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
                                    text = "تهانينا! أتممت جميع مراحل المطابقة بنجاح!",
                                    color = Gold,
                                    fontSize = 20.sp,
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
