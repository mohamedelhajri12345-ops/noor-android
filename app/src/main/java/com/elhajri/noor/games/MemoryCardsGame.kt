package com.elhajri.noor.games

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.audio.SfxManager
import com.elhajri.noor.games.engine.*
import com.elhajri.noor.settings.t
import com.elhajri.noor.theme.NoorThemeState
import com.elhajri.noor.ui.noorGlassCard
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Game 13: Islamic Memory Canvas (بطاقات الذاكرة)
 * Category: أطفال
 * Mechanic: Tap card flip-and-match visual memory grid.
 */
data class MemoryCardItem(
    val id: Int,
    val iconName: String,
    var isFlipped: Boolean = false,
    var isMatched: Boolean = false
)

@Composable
fun MemoryCardsGame(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val theme = NoorThemeState.active
    val coroutineScope = rememberCoroutineScope()
    val textMeasurer = rememberTextMeasurer()
    val engineState = rememberGameEngineState(initialLevel = 1, initialLives = -1, targetScore = 80)

    val rows = 4
    val cols = 3
    val totalPairs = 6
    val icons = remember { listOf("🕌", "🌙", "⭐", "📿", "📖", "🏮") }

    val cards = remember { mutableStateListOf<MemoryCardItem>() }
    var flippedIndices by remember { mutableStateOf<List<Int>>(emptyList()) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    var isBusy by remember { mutableStateOf(false) }
    val particles = remember { ParticleSystem() }

    fun initGame() {
        cards.clear()
        flippedIndices = emptyList()
        isBusy = false

        val list = mutableListOf<MemoryCardItem>()
        for (i in 0 until totalPairs) {
            val icon = icons[i]
            list.add(MemoryCardItem(id = i * 2, iconName = icon))
            list.add(MemoryCardItem(id = i * 2 + 1, iconName = icon))
        }
        list.shuffle()
        cards.addAll(list)
        engineState.startGame()
    }

    LaunchedEffect(Unit) {
        initGame()
    }

    fun handleCardTap(index: Int) {
        if (engineState.playState != GamePlayState.PLAYING || isBusy) return
        if (cards[index].isFlipped || cards[index].isMatched) return

        triggerGameHaptic(context, GameHapticType.LIGHT)
        SfxManager.playTap(context)

        cards[index] = cards[index].copy(isFlipped = true)
        val currentFlipped = flippedIndices + index
        flippedIndices = currentFlipped

        if (currentFlipped.size == 2) {
            isBusy = true
            val idx1 = currentFlipped[0]
            val idx2 = currentFlipped[1]

            if (cards[idx1].iconName == cards[idx2].iconName) {
                // Match!
                coroutineScope.launch {
                    delay(300)
                    cards[idx1] = cards[idx1].copy(isMatched = true)
                    cards[idx2] = cards[idx2].copy(isMatched = true)
                    flippedIndices = emptyList()
                    isBusy = false

                    triggerGameHaptic(context, GameHapticType.SUCCESS)
                    SfxManager.playSuccess(context)
                    engineState.addScore(15)

                    if (cards.all { it.isMatched }) {
                        engineState.completeLevel(context, 3)
                    }
                }
            } else {
                // Mismatch
                coroutineScope.launch {
                    delay(800)
                    cards[idx1] = cards[idx1].copy(isFlipped = false)
                    cards[idx2] = cards[idx2].copy(isFlipped = false)
                    flippedIndices = emptyList()
                    isBusy = false
                    SfxManager.playWrong(context)
                }
            }
        }
    }

    CanvasGameLoop(engineState = engineState) { deltaSec ->
        particles.update(deltaSec)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            GameHeaderBar(
                title = t("بطاقات الذاكرة", "Islamic Memory Canvas"),
                level = engineState.level,
                score = engineState.score,
                lives = -1,
                timeRemainingMs = -1L,
                onBack = onBack,
                onPause = { engineState.pauseGame() }
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = t("قلّب البطاقات واكتشف الأزواج الإسلامية المتقابلة!", "Flip cards to reveal and match Islamic symbol pairs!"),
                color = theme.textSecondary,
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.8f)
                    .padding(16.dp)
                    .noorGlassCard(cornerRadius = 24.dp)
                    .border(2.dp, theme.accent.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .onSizeChanged { canvasSize = it }
                        .gameGestureDetector(
                            onTap = { offset ->
                                if (canvasSize == IntSize.Zero) return@gameGestureDetector
                                val cellW = canvasSize.width / cols.toFloat()
                                val cellH = canvasSize.height / rows.toFloat()
                                val c = (offset.x / cellW).toInt().coerceIn(0, cols - 1)
                                val r = (offset.y / cellH).toInt().coerceIn(0, rows - 1)
                                val idx = r * cols + c
                                if (idx in cards.indices) {
                                    handleCardTap(idx)
                                }
                            }
                        )
                ) {
                    val cellW = size.width / cols.toFloat()
                    val cellH = size.height / rows.toFloat()

                    for (r in 0 until rows) {
                        for (c in 0 until cols) {
                            val idx = r * cols + c
                            if (idx < cards.size) {
                                val item = cards[idx]
                                val left = c * cellW + 6f
                                val top = r * cellH + 6f
                                val cardW = cellW - 12f
                                val cardH = cellH - 12f

                                val cardColor = when {
                                    item.isMatched -> Color(0xFF00E676).copy(alpha = 0.3f)
                                    item.isFlipped -> theme.accent
                                    else -> theme.surface
                                }

                                drawRoundRect(
                                    color = cardColor,
                                    topLeft = Offset(left, top),
                                    size = Size(cardW, cardH),
                                    cornerRadius = CornerRadius(16f)
                                )

                                if (item.isFlipped || item.isMatched) {
                                    val measured = textMeasurer.measure(
                                        text = item.iconName,
                                        style = TextStyle(fontSize = 28.sp)
                                    )
                                    drawText(
                                        textLayoutResult = measured,
                                        topLeft = Offset(
                                            left + (cardW - measured.size.width) / 2f,
                                            top + (cardH - measured.size.height) / 2f
                                        )
                                    )
                                }
                            }
                        }
                    }

                    particles.draw(this)
                }
            }
        }

        when (engineState.playState) {
            GamePlayState.PAUSED -> PauseOverlay(
                onResume = { engineState.resumeGame() },
                onRestart = { initGame() },
                onQuit = onBack
            )
            GamePlayState.WON -> GameWinOverlay(
                score = engineState.score,
                stars = engineState.starsEarned,
                onRestart = { initGame() },
                onQuit = onBack
            )
            GamePlayState.LOST -> GameLoseOverlay(
                score = engineState.score,
                onRestart = { initGame() },
                onQuit = onBack
            )
            else -> {}
        }
    }
}
