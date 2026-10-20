package com.elhajri.noor.games

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
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

@Composable
fun WordScrambleGameScreen(
    gameTitle: String,
    words: List<String>,
    hints: List<String>,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val totalStages = words.size

    var progressData by remember(gameTitle, totalStages) {
        mutableStateOf(StageProgressStore.loadProgress(context, gameTitle, totalStages))
    }
    var selectedStageIdx by remember { mutableStateOf<Int?>(null) }
    var showCelebration by remember { mutableStateOf(false) }

    if (showCelebration) {
        WordScrambleCelebrationScreen(
            gameTitle = gameTitle,
            totalStages = totalStages,
            totalStars = progressData.starsMap.values.sum(),
            onBackToMap = {
                showCelebration = false
                selectedStageIdx = null
            },
            onReplayAll = {
                showCelebration = false
                selectedStageIdx = 0
            }
        )
    } else if (selectedStageIdx == null) {
        StageMapScreen(
            gameTitle = gameTitle,
            gameSubtitle = "${totalStages.toArabicDigits()} مرحلة لتحدي تكوين الكلمات",
            totalStages = totalStages,
            progressData = progressData,
            onSelectStage = { stageIdx ->
                selectedStageIdx = stageIdx
            },
            onBack = onBack
        )
    } else {
        val stageIdx = selectedStageIdx!!
        val targetWord = words.getOrElse(stageIdx) { "" }
        val targetHint = hints.getOrElse(stageIdx) { "" }

        WordScrambleStagePlayScreen(
            gameTitle = gameTitle,
            stageIdx = stageIdx,
            totalStages = totalStages,
            targetWord = targetWord,
            targetHint = targetHint,
            progressData = progressData,
            onBackToMap = {
                progressData = StageProgressStore.loadProgress(context, gameTitle, totalStages)
                selectedStageIdx = null
            },
            onCompleteStage = { stars ->
                StageProgressStore.saveProgress(context, gameTitle, stageIdx, stars, totalStages)
                progressData = StageProgressStore.loadProgress(context, gameTitle, totalStages)
            },
            onNextStage = {
                if (stageIdx + 1 < totalStages) {
                    selectedStageIdx = stageIdx + 1
                } else {
                    selectedStageIdx = null
                    showCelebration = true
                }
            },
            onShowCelebration = {
                selectedStageIdx = null
                showCelebration = true
            }
        )
    }
}

@Composable
private fun WordScrambleStagePlayScreen(
    gameTitle: String,
    stageIdx: Int,
    totalStages: Int,
    targetWord: String,
    targetHint: String,
    progressData: StageProgressData,
    onBackToMap: () -> Unit,
    onCompleteStage: (Int) -> Unit,
    onNextStage: () -> Unit,
    onShowCelebration: () -> Unit
) {
    var replaySeed by remember(stageIdx) { mutableIntStateOf(0) }

    var tiles by remember(stageIdx, replaySeed) {
        val charList = targetWord.toCharArray().toList()
        var shuffled = charList.shuffled()
        if (charList.size > 1) {
            while (shuffled == charList) {
                shuffled = charList.shuffled()
            }
        }
        mutableStateOf(shuffled)
    }

    var selectedTileIndex by remember(stageIdx, replaySeed) { mutableStateOf<Int?>(null) }
    var hintUsedCount by remember(stageIdx, replaySeed) { mutableIntStateOf(0) }
    var showHintDialog by remember(stageIdx, replaySeed) { mutableStateOf(false) }
    var wordSolved by remember(stageIdx, replaySeed) { mutableStateOf(false) }
    var showWinDialog by remember(stageIdx, replaySeed) { mutableStateOf(false) }
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    var shakeTrigger by remember(stageIdx, replaySeed) { mutableLongStateOf(0L) }

    LaunchedEffect(tiles) {
        val currentStr = tiles.joinToString("")
        if (currentStr == targetWord && targetWord.isNotEmpty() && !wordSolved) {
            wordSolved = true
            val stars = when (hintUsedCount) {
                0 -> 3
                1 -> 2
                else -> 1
            }
            onCompleteStage(stars)
            delay(500)
            showWinDialog = true
        }
    }

    fun swapTiles(i: Int, j: Int) {
        if (i == j) return
        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        val list = tiles.toMutableList()
        val temp = list[i]
        list[i] = list[j]
        list[j] = temp
        tiles = list
        if (list.joinToString("") != targetWord) {
            shakeTrigger = System.currentTimeMillis()
        }
    }

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
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBackToMap) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "رجوع",
                        tint = Gold
                    )
                }
                Text(
                    text = gameTitle,
                    color = Gold,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StreakCounterFlameHeader(streak = (stageIdx).coerceAtLeast(0))
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = NavyCard,
                        border = BorderStroke(1.dp, Gold.copy(alpha = 0.4f))
                    ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Gold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = progressData.starsMap.values.sum().toArabicDigits(),
                            color = Gold,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Subtitle & Actions Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .noorGlassCard(cornerRadius = 14.dp)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "المرحلة ${(stageIdx + 1).toArabicDigits()} من ${totalStages.toArabicDigits()}",
                    color = TextMain,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            if (hintUsedCount < 2) {
                                if (StarEconomyStore.consumeHintOrBuyRefill(context)) {
                                    hintUsedCount += 1
                                    showHintDialog = true
                                }
                            }
                        },
                        enabled = hintUsedCount < 2
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = "تلميح",
                            tint = if (hintUsedCount < 2) Gold else GoldSoft.copy(alpha = 0.3f)
                        )
                    }
                    IconButton(onClick = {
                        val charList = targetWord.toCharArray().toList()
                        var shuf = charList.shuffled()
                        if (charList.size > 1) {
                            while (shuf == charList) {
                                shuf = charList.shuffled()
                            }
                        }
                        tiles = shuf
                        selectedTileIndex = null
                    }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "إعادة خلط",
                            tint = GoldSoft
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Hint Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .noorGlassCard(cornerRadius = 18.dp)
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "رتب الحروف لتشكيل الكلمة الصحيحة",
                        color = GoldSoft,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = targetHint,
                        color = TextMain,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Tiles Grid
            Text(
                text = "اضغط على حرفين للتبديل بينهما:",
                color = GoldSoft,
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            val columns = tiles.size.coerceAtMost(6).coerceAtLeast(3)
            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                itemsIndexed(tiles) { index, char ->
                    val isSelected = selectedTileIndex == index
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) Gold else NavyCard)
                            .border(
                                2.dp,
                                if (isSelected) GoldSoft else Gold.copy(alpha = 0.4f),
                                RoundedCornerShape(14.dp)
                            )
                            .clickable {
                                val sel = selectedTileIndex
                                if (sel == null) {
                                    selectedTileIndex = index
                                } else if (sel == index) {
                                    selectedTileIndex = null
                                } else {
                                    swapTiles(sel, index)
                                    selectedTileIndex = null
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = char.toString(),
                            color = if (isSelected) Navy else TextMain,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (wordSolved) {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "إجابة صحيحة! أحسنت ✨",
                    color = Gold,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }

        if (showHintDialog) {
            AlertDialog(
                onDismissRequest = { showHintDialog = false },
                title = {
                    Text("تلميح الكلمة", color = Gold, fontWeight = FontWeight.Bold)
                },
                text = {
                    Text(
                        "الحرف الأول من الكلمة هو: '${targetWord.firstOrNull() ?: ""}'\n\n$targetHint",
                        color = TextMain
                    )
                },
                confirmButton = {
                    TextButton(onClick = { showHintDialog = false }) {
                        Text("حسناً", color = Gold)
                    }
                },
                containerColor = Navy,
                shape = RoundedCornerShape(16.dp)
            )
        }

        if (showWinDialog) {
            val earnedStars = when (hintUsedCount) {
                0 -> 3
                1 -> 2
                else -> 1
            }

            StageWinDialog(
                stageNumber = stageIdx + 1,
                totalStages = totalStages,
                stars = earnedStars,
                stageTitle = "كلمة: $targetWord",
                onNextStage = {
                    showWinDialog = false
                    if (stageIdx + 1 < totalStages) {
                        onNextStage()
                    } else {
                        onShowCelebration()
                    }
                },
                onReplayStage = {
                    showWinDialog = false
                    replaySeed++
                },
                onStageMap = {
                    showWinDialog = false
                    if (stageIdx == totalStages - 1) {
                        onShowCelebration()
                    } else {
                        onBackToMap()
                    }
                }
            )
        }
    }
}

@Composable
private fun WordScrambleCelebrationScreen(
    gameTitle: String,
    totalStages: Int,
    totalStars: Int,
    onBackToMap: () -> Unit,
    onReplayAll: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .themeScreenBackground()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
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
                        text = "تهانينا! أتممت جميع مراحل $gameTitle بنجاح!",
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
                                text = "${totalStars.toArabicDigits()} / ${(totalStages * 3).toArabicDigits()}",
                                color = Gold,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = onReplayAll,
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
                                text = "إعادة اللعب من البداية",
                                color = Navy,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = onBackToMap,
                        border = BorderStroke(1.dp, Gold),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text(
                            text = "خريطة المراحل",
                            color = Gold,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
