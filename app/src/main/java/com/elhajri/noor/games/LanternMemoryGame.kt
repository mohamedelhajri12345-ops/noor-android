package com.elhajri.noor.games

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PlayArrow
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
import kotlin.random.Random

private const val GAME_NAME = "LanternMemoryGame"
private const val TOTAL_STAGES = 25

private data class LanternInfo(
    val id: Int,
    val text: String,
    val color: Color
)

private val LANTERNS = listOf(
    LanternInfo(0, "سبحان الله", Color(0xFFE53935)), // Red
    LanternInfo(1, "الحمد لله", Color(0xFF43A047)), // Green
    LanternInfo(2, "لا إله إلا الله", Color(0xFF1E88E5)), // Blue
    LanternInfo(3, "الله أكبر", Color(0xFFFFB300)) // Gold / Amber
)

private enum class LanternGameState {
    READY, SHOWING_SEQUENCE, USER_INPUT, STAGE_WON
}

@Composable
fun LanternMemoryGame(onBack: () -> Unit) {
    val context = LocalContext.current
    var progressData by remember {
        mutableStateOf(StageProgressStore.loadProgress(context, GAME_NAME, TOTAL_STAGES))
    }
    var selectedStageIdx by remember { mutableStateOf<Int?>(null) }

    if (selectedStageIdx == null) {
        StageMapScreen(
            gameTitle = "فوانيس الذاكرة",
            gameSubtitle = "٢٥ مرحلة لتحدي ذاكرة الأذكار التتابعية",
            totalStages = TOTAL_STAGES,
            progressData = progressData,
            onSelectStage = { selectedStageIdx = it },
            onBack = onBack
        )
    } else {
        val stageIdx = selectedStageIdx!!
        LanternMemoryPlayScreen(
            stageIdx = stageIdx,
            totalStages = TOTAL_STAGES,
            onStageComplete = { stars ->
                StageProgressStore.saveProgress(context, GAME_NAME, stageIdx, stars, TOTAL_STAGES)
                progressData = StageProgressStore.loadProgress(context, GAME_NAME, TOTAL_STAGES)
            },
            onNextStage = {
                if (stageIdx + 1 < TOTAL_STAGES) {
                    selectedStageIdx = stageIdx + 1
                } else {
                    selectedStageIdx = null
                }
            },
            onBackToMap = { selectedStageIdx = null }
        )
    }
}

@Composable
private fun LanternMemoryPlayScreen(
    stageIdx: Int,
    totalStages: Int,
    onStageComplete: (Int) -> Unit,
    onNextStage: () -> Unit,
    onBackToMap: () -> Unit
) {
    val stageNumber = stageIdx + 1
    val sequenceLength = 3 + stageIdx // Grows from 3 items at stage 1 to 27 at stage 25

    val sequence = remember(stageIdx) {
        val rand = Random(stageIdx * 7777L + 1234)
        List(sequenceLength) { rand.nextInt(4) }
    }

    var gameState by remember(stageIdx) { mutableStateOf(LanternGameState.READY) }
    var userIndex by remember(stageIdx) { mutableIntStateOf(0) }
    var activeLitLantern by remember(stageIdx) { mutableStateOf<Int?>(null) }
    var mistakesCount by remember(stageIdx) { mutableIntStateOf(0) }
    var statusText by remember(stageIdx) { mutableStateOf("اضغط ابدأ لمشاهدة التسلسل") }
    var showWinDialog by remember(stageIdx) { mutableStateOf(false) }
    var earnedStars by remember(stageIdx) { mutableIntStateOf(3) }
    val haptics = LocalHapticFeedback.current
    var shakeTrigger by remember(stageIdx) { mutableLongStateOf(0L) }

    fun startShowingSequence() {
        gameState = LanternGameState.SHOWING_SEQUENCE
        statusText = "استمع وراقب تسلسل الفوانيس..."
        userIndex = 0
    }

    LaunchedEffect(gameState) {
        if (gameState == LanternGameState.SHOWING_SEQUENCE) {
            delay(400)
            for (idx in sequence) {
                activeLitLantern = idx
                delay(550)
                activeLitLantern = null
                delay(220)
            }
            gameState = LanternGameState.USER_INPUT
            statusText = "دورك الآن! كرر تسلسل الأذكار"
        }
    }

    fun handleLanternClick(lanternId: Int) {
        if (gameState != LanternGameState.USER_INPUT) return

        activeLitLantern = lanternId

        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        val expectedId = sequence[userIndex]
        if (lanternId == expectedId) {
            val nextIdx = userIndex + 1
            userIndex = nextIdx

            if (nextIdx == sequence.size) {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                val stars = when (mistakesCount) {
                    0 -> 3
                    1 -> 2
                    else -> 1
                }
                earnedStars = stars
                gameState = LanternGameState.STAGE_WON
                statusText = "أحسنت! أتممت تسلسل هذه المرحلة بنجاح"
                onStageComplete(stars)
                showWinDialog = true
            }
        } else {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            shakeTrigger = System.currentTimeMillis()
            mistakesCount += 1
            statusText = "خطأ في الترتيب! أعد التركيز واشهد التسلسل مجدداً"
            gameState = LanternGameState.READY
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
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
                        contentDescription = "المراحل",
                        tint = Gold
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "فوانيس الذاكرة",
                        color = Gold,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "المرحلة ${stageNumber.toArabicDigits()} من ${totalStages.toArabicDigits()} (تسلسل من ${sequenceLength.toArabicDigits()} عناصر)",
                        color = GoldSoft,
                        fontSize = 12.sp
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StreakCounterFlameHeader(streak = (stageIdx).coerceAtLeast(0))
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = NavyCard,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Gold.copy(alpha = 0.4f))
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
                            text = "أخطاء: ${mistakesCount.toArabicDigits()}",
                            color = Gold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Status Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .noorGlassCard(cornerRadius = 16.dp)
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = statusText,
                    color = TextMain,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Lanterns 2x2 Grid
            Column(
                modifier = Modifier.wrongAnswerShake(shakeTrigger),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    LanternCard(
                        lantern = LANTERNS[0],
                        isLit = activeLitLantern == 0,
                        enabled = gameState == LanternGameState.USER_INPUT,
                        onClick = { handleLanternClick(0) }
                    )
                    LanternCard(
                        lantern = LANTERNS[1],
                        isLit = activeLitLantern == 1,
                        enabled = gameState == LanternGameState.USER_INPUT,
                        onClick = { handleLanternClick(1) }
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    LanternCard(
                        lantern = LANTERNS[2],
                        isLit = activeLitLantern == 2,
                        enabled = gameState == LanternGameState.USER_INPUT,
                        onClick = { handleLanternClick(2) }
                    )
                    LanternCard(
                        lantern = LANTERNS[3],
                        isLit = activeLitLantern == 3,
                        enabled = gameState == LanternGameState.USER_INPUT,
                        onClick = { handleLanternClick(3) }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Action Button
            if (gameState == LanternGameState.READY) {
                Button(
                    onClick = { startShowingSequence() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Gold),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Navy
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (mistakesCount == 0) "عرض التسلسل" else "إعادة عرض التسلسل",
                        color = Navy,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else if (gameState == LanternGameState.USER_INPUT) {
                Text(
                    text = "تقدمك: ${userIndex.toArabicDigits()} / ${sequenceLength.toArabicDigits()}",
                    color = GoldSoft,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        if (showWinDialog) {
            StageWinDialog(
                stageNumber = stageNumber,
                totalStages = totalStages,
                stars = earnedStars,
                stageTitle = "المرحلة ${stageNumber.toArabicDigits()}",
                onNextStage = {
                    showWinDialog = false
                    onNextStage()
                },
                onReplayStage = {
                    showWinDialog = false
                    gameState = LanternGameState.READY
                    userIndex = 0
                    mistakesCount = 0
                    statusText = "اضغط ابدأ لمشاهدة التسلسل"
                },
                onStageMap = onBackToMap
            )
        }
    }
}

@Composable
private fun LanternCard(
    lantern: LanternInfo,
    isLit: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val cardBg = if (isLit) lantern.color else NavyCard
    val borderCol = if (isLit) Gold else lantern.color.copy(alpha = 0.6f)
    val textCol = if (isLit) Color.White else TextMain

    Box(
        modifier = Modifier
            .size(135.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(cardBg)
            .border(2.dp, borderCol, RoundedCornerShape(20.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Lightbulb,
                contentDescription = null,
                tint = if (isLit) Gold else lantern.color,
                modifier = Modifier.size(38.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = lantern.text,
                color = textCol,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}
