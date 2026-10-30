package com.elhajri.noor.games

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.audio.SfxManager
import com.elhajri.noor.games.engine.*
import com.elhajri.noor.settings.t
import com.elhajri.noor.theme.NoorThemeState
import com.elhajri.noor.ui.noorGlassCard
import kotlin.math.abs

/**
 * Game 29: Calligraphy Unscramble (ترتيب المخطوطات)
 * Category: كلمات
 * Mechanic: Horizontal strip unscramble. Drag and swap horizontal sliced image strips
 * of a classical Arabic calligraphy manuscript to restore the phrase.
 */
private data class ManuscriptPhrase(
    val titleAr: String,
    val fullPhrase: String,
    val numStrips: Int
)

private data class CalligraphyStrip(
    val id: Int,
    val originalRow: Int,
    var currentRow: Int
)

@Composable
fun CalligraphyPuzzleGame(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val theme = NoorThemeState.active
    val textMeasurer = rememberTextMeasurer()
    val engineState = rememberGameEngineState(initialLevel = 1, initialLives = 3, timeLimitMs = 90000L, targetScore = 3)

    val phrases = remember {
        listOf(
            ManuscriptPhrase("البسملة الشريفة", "بسم الله الرحمن الرحيم", 4),
            ManuscriptPhrase("الشهادتان", "لا إله إلا الله محمد رسول الله", 5),
            ManuscriptPhrase("التسبيح والمحمدة", "سبحان الله وبحمده سبحان الله العظيم", 6)
        )
    }

    var currentPhrase by remember { mutableStateOf(phrases[0]) }
    val strips = remember { mutableStateListOf<CalligraphyStrip>() }
    val particles = remember { ParticleSystem() }

    var selectedStripId by remember { mutableStateOf<Int?>(null) }
    var dragAccumulatorY by remember { mutableStateOf(0f) }
    var showPreview by remember { mutableStateOf(false) }

    fun setupManuscript(level: Int) {
        val data = phrases[(level - 1) % phrases.size]
        currentPhrase = data
        strips.clear()

        val count = data.numStrips
        val scrambledRows = (0 until count).shuffled()
        for (i in 0 until count) {
            strips.add(
                CalligraphyStrip(
                    id = i,
                    originalRow = i,
                    currentRow = scrambledRows[i]
                )
            )
        }
    }

    fun initGame() {
        setupManuscript(1)
        engineState.startGame()
    }

    LaunchedEffect(Unit) {
        initGame()
    }

    fun checkSolved() {
        if (strips.all { it.currentRow == it.originalRow }) {
            particles.spawnBurst(Offset(350f, 300f), count = 50)
            triggerGameHaptic(context, GameHapticType.SUCCESS)
            SfxManager.playSuccess(context)
            engineState.addScore(1)

            if (engineState.score >= engineState.targetScore) {
                engineState.completeLevel(context, 3)
            } else {
                setupManuscript(engineState.score + 1)
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
                title = t("ترتيب المخطوطات", "Calligraphy Puzzle"),
                level = engineState.score + 1,
                score = engineState.score,
                lives = engineState.lives,
                timeRemainingMs = engineState.timeRemainingMs,
                onBack = onBack,
                onPause = { engineState.pauseGame() }
            )

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = currentPhrase.titleAr,
                        color = theme.accent,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = t("اسحب الشرائط أفقياً لترتيب المخطوطة الكلاسيكية!", "Drag strips vertically to restore the calligraphy manuscript!"),
                        color = theme.textSecondary,
                        fontSize = 12.sp
                    )
                }

                OutlinedButton(
                    onClick = { showPreview = !showPreview },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (showPreview) t("إخفاء", "Hide") else t("معاينة", "Preview"),
                        fontSize = 12.sp,
                        color = theme.accent
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp)
                    .noorGlassCard(cornerRadius = 24.dp)
                    .border(2.dp, theme.accent.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(engineState.score) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    if (engineState.playState != GamePlayState.PLAYING) return@detectDragGestures
                                    val count = currentPhrase.numStrips
                                    val topY = 40f
                                    val manuscriptH = size.height - 80f
                                    val sliceH = manuscriptH / count

                                    val clickedRow = ((offset.y - topY) / sliceH).toInt()
                                    val strip = strips.find { it.currentRow == clickedRow }
                                    if (strip != null) {
                                        selectedStripId = strip.id
                                        dragAccumulatorY = 0f
                                        triggerGameHaptic(context, GameHapticType.LIGHT)
                                    }
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    val id = selectedStripId ?: return@detectDragGestures
                                    val s = strips.find { it.id == id } ?: return@detectDragGestures

                                    dragAccumulatorY += dragAmount.y
                                    val count = currentPhrase.numStrips
                                    val manuscriptH = size.height - 80f
                                    val sliceH = manuscriptH / count

                                    if (abs(dragAccumulatorY) > sliceH * 0.45f) {
                                        val targetRow = if (dragAccumulatorY > 0) s.currentRow + 1 else s.currentRow - 1
                                        if (targetRow in 0 until count) {
                                            val other = strips.find { it.currentRow == targetRow }
                                            if (other != null) {
                                                other.currentRow = s.currentRow
                                                s.currentRow = targetRow
                                                SfxManager.playTap(context)
                                                triggerGameHaptic(context, GameHapticType.LIGHT)
                                            }
                                        }
                                        dragAccumulatorY = 0f
                                    }
                                },
                                onDragEnd = {
                                    selectedStripId = null
                                    dragAccumulatorY = 0f
                                    checkSolved()
                                }
                            )
                        }
                ) {
                    val canvasW = size.width
                    val manuscriptW = canvasW - 40f
                    val topY = 40f
                    val manuscriptH = size.height - 80f

                    val count = currentPhrase.numStrips
                    val sliceH = manuscriptH / count

                    // Helper to draw the complete manuscript
                    fun drawFullManuscript(offsetY: Float) {
                        val leftX = 20f
                        val manuscriptRect = Rect(leftX, topY + offsetY, leftX + manuscriptW, topY + manuscriptH + offsetY)

                        // Parchment Background
                        drawRoundRect(
                            color = Color(0xFFFFF8E1),
                            topLeft = Offset(manuscriptRect.left, manuscriptRect.top),
                            size = Size(manuscriptRect.width, manuscriptRect.height),
                            cornerRadius = CornerRadius(16f)
                        )

                        // Outer Ornate Border
                        drawRoundRect(
                            color = Color(0xFFD4AF37),
                            topLeft = Offset(manuscriptRect.left + 8f, manuscriptRect.top + 8f),
                            size = Size(manuscriptRect.width - 16f, manuscriptRect.height - 16f),
                            cornerRadius = CornerRadius(12f),
                            style = Stroke(width = 3f)
                        )

                        // Inner Thin Border
                        drawRoundRect(
                            color = Color(0xFF8D6E63),
                            topLeft = Offset(manuscriptRect.left + 14f, manuscriptRect.top + 14f),
                            size = Size(manuscriptRect.width - 28f, manuscriptRect.height - 28f),
                            cornerRadius = CornerRadius(10f),
                            style = Stroke(width = 1.5f)
                        )

                        // Calligraphy Text across manuscript height
                        val textResult = textMeasurer.measure(
                            text = currentPhrase.fullPhrase,
                            style = TextStyle(
                                color = Color(0xFF2E7D32),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        drawText(
                            textLayoutResult = textResult,
                            topLeft = Offset(
                                manuscriptRect.left + (manuscriptRect.width - textResult.size.width) / 2f,
                                manuscriptRect.top + (manuscriptRect.height - textResult.size.height) / 2f
                            )
                        )
                    }

                    if (showPreview) {
                        drawFullManuscript(offsetY = 0f)
                    } else {
                        // Draw Strips
                        for (r in 0 until count) {
                            val strip = strips.find { it.currentRow == r } ?: continue
                            val slotTop = topY + r * sliceH

                            clipRect(
                                left = 20f,
                                top = slotTop,
                                right = 20f + manuscriptW,
                                bottom = slotTop + sliceH
                            ) {
                                // Draw full manuscript shifted by difference between current slot and original row
                                val shiftY = (r - strip.originalRow) * sliceH
                                drawFullManuscript(offsetY = shiftY)
                            }

                            // Slice Divider Line
                            if (r < count - 1) {
                                drawLine(
                                    color = Color(0xFFD4AF37).copy(alpha = 0.8f),
                                    start = Offset(20f, slotTop + sliceH),
                                    end = Offset(20f + manuscriptW, slotTop + sliceH),
                                    strokeWidth = 3f
                                )
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
