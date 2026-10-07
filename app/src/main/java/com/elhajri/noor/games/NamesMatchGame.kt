package com.elhajri.noor.games

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
import com.elhajri.noor.data.DataLoader
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.Navy
import com.elhajri.noor.ui.NavyCard
import kotlin.random.Random

/**
 * "رباط معاني الله" — ذاكرة ومطابقة: طابق اسم الله الحسنى مع معناه الإيماني.
 * 20 مرحلة بتزايد في عدد الأزواج والشبكة من 4 أزواج حتى 10 أزواج.
 */
private data class MatchCard(val pairId: Int, val label: String, val isMeaning: Boolean)

@Composable
fun NamesMatchGameScreen(onBack: () -> Unit) {
    ForcePortraitOrientation()

    val context = LocalContext.current
    var currentLevelIdx by remember { mutableIntStateOf(0) }
    var inPlay by remember { mutableStateOf(false) }
    var refreshKey by remember { mutableIntStateOf(0) }

    val totalLevels = 20
    val highestLevel = remember(refreshKey) { GameProgressStore.getHighestLevel(context, GameProgressStore.GAME_MATCH) }
    val totalStars = remember(refreshKey) { GameProgressStore.getTotalStars(context, GameProgressStore.GAME_MATCH, totalLevels) }
    val totalScore = remember(refreshKey) { GameProgressStore.getTotalScore(context, GameProgressStore.GAME_MATCH) }
    val streak = remember(refreshKey) { GameProgressStore.getStreak(context, GameProgressStore.GAME_MATCH) }

    Box(Modifier.fillMaxSize().background(Navy)) {
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { if (inPlay) inPlay = false else onBack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = Gold)
                }
                Column(Modifier.weight(1f)) {
                    Text("رباط معاني الله", color = Gold, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("المستوى ${currentLevelIdx + 1} / $totalLevels | ⭐ $totalStars | 🌙 $totalScore | 🔥 $streak أعياد", color = GoldSoft, fontSize = 11.sp)
                }
            }

            if (!inPlay) {
                Text(
                    "خريطة مراحل الرباط",
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
                        val starsEarned = GameProgressStore.getLevelStars(context, GameProgressStore.GAME_MATCH, index)

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
                NamesMatchPlayScreen(
                    levelIdx = currentLevelIdx,
                    onLevelComplete = { moves, pairCount ->
                        val ratio = moves.toFloat() / pairCount
                        val stars = when {
                            ratio <= 1.5f -> 3
                            ratio <= 2.3f -> 2
                            else -> 1
                        }
                        val scoreGained = (pairCount * 25) + (30 - moves).coerceAtLeast(10)
                        GameProgressStore.rewardPoints(context, GameProgressStore.GAME_MATCH, currentLevelIdx, stars, scoreGained)
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
private fun NamesMatchPlayScreen(
    levelIdx: Int,
    onLevelComplete: (moves: Int, pairCount: Int) -> Unit,
    onNextLevel: () -> Unit,
    onExit: () -> Unit
) {
    val context = LocalContext.current

    val pairCount = when {
        levelIdx < 5 -> 4
        levelIdx < 10 -> 6
        levelIdx < 15 -> 8
        else -> 10
    }

    var cards by remember(levelIdx) { mutableStateOf(listOf<MatchCard>()) }
    var flipped by remember(levelIdx) { mutableStateOf(setOf<Int>()) }
    var matched by remember(levelIdx) { mutableStateOf(setOf<Int>()) }
    var moves by remember(levelIdx) { mutableIntStateOf(0) }
    var wrongPair by remember(levelIdx) { mutableStateOf(setOf<Int>()) }

    fun setup(l: Int) {
        val names = DataLoader.namesOfAllah(context)
            .filter { it.meaning.isNotBlank() && it.name != "الله" }
            .distinctBy { it.name }
        val picks = names.shuffled(Random((l + 1) * 17 + 3)).take(pairCount)
        val deck = picks.flatMapIndexed { i, n ->
            listOf(
                MatchCard(i, n.name, isMeaning = false),
                MatchCard(i, n.meaning, isMeaning = true)
            )
        }.shuffled(Random((l + 1) * 29 + 11))
        cards = deck
        flipped = emptySet()
        matched = emptySet()
        moves = 0
        wrongPair = emptySet()
    }

    LaunchedEffect(levelIdx) { setup(levelIdx) }

    val allMatched = cards.isNotEmpty() && matched.size == cards.size

    LaunchedEffect(allMatched) {
        if (allMatched) {
            onLevelComplete(moves, pairCount)
        }
    }

    fun flip(idx: Int) {
        if (allMatched || idx in matched || idx in flipped || flipped.size >= 2) return
        flipped = flipped + idx
        if (flipped.size == 2) {
            moves++
            val (a, b) = flipped.toList()
            val ca = cards[a]; val cb = cards[b]
            if (ca.pairId == cb.pairId && ca.isMeaning != cb.isMeaning) {
                matched = matched + setOf(a, b)
                flipped = emptySet()
            } else {
                wrongPair = setOf(a, b)
            }
        }
    }

    LaunchedEffect(wrongPair) {
        if (wrongPair.isNotEmpty()) {
            kotlinx.coroutines.delay(700)
            flipped = flipped - wrongPair
            wrongPair = emptySet()
        }
    }

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.padding(horizontal = 18.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("المحاولات: $moves", color = GoldSoft, fontSize = 12.sp)
            Spacer(Modifier.weight(1f))
            IconButton(onClick = { setup(levelIdx) }) {
                Icon(Icons.Default.Replay, contentDescription = "إعادة", tint = GoldSoft)
            }
        }

        val columns = if (pairCount <= 6) 2 else 3

        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            itemsIndexed(cards) { idx, card ->
                val isOpen = idx in flipped || idx in matched
                val isWrong = idx in wrongPair
                val pop by animateFloatAsState(
                    targetValue = if (isOpen) 1f else 0.94f,
                    animationSpec = tween(300), label = "pop"
                )
                val isGold = idx in matched

                Box(
                    Modifier
                        .height(if (pairCount <= 6) 100.dp else 84.dp)
                        .graphicsLayer { scaleX = pop; scaleY = pop }
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            when {
                                isGold -> Gold
                                isWrong -> Color(0xFF7F1D1D).copy(alpha = 0.5f)
                                isOpen -> NavyCard
                                else -> NavyCard.copy(alpha = 0.7f)
                            }
                        )
                        .border(
                            1.dp,
                            if (isGold) Gold else Gold.copy(alpha = if (isOpen) 0.5f else 0.18f),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { flip(idx) },
                    contentAlignment = Alignment.Center
                ) {
                    if (isOpen) {
                        Text(
                            card.label,
                            color = if (isGold) Navy else Color.White,
                            fontSize = if (card.isMeaning) 11.sp else 15.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(8.dp)
                        )
                    } else {
                        Text("﷽", color = Gold.copy(alpha = 0.35f), fontSize = 16.sp)
                    }
                }
            }
        }

        if (allMatched) {
            Column(
                Modifier.fillMaxWidth().padding(bottom = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("ما شاء الله! أتممت الرباط 🌙", color = Gold, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(Modifier.height(10.dp))
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
                            .clickable { onNextLevel() }
                            .padding(horizontal = 26.dp, vertical = 10.dp)
                    ) {
                        Text("المستوى التالي", color = Navy, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
