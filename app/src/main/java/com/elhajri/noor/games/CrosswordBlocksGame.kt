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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
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
import kotlin.math.hypot

/**
 * Game 26: Mini Arabic Crossword Blocks (شبكة الكلمات)
 * Category: كلمات
 * Mechanic: Block fitting crossword grid. Drag multi-letter horizontal or vertical
 * word blocks onto a blank crossword grid canvas to fit all pieces together.
 */
private data class CrosswordBlock(
    val id: Int,
    val word: String,
    val isVertical: Boolean,
    val targetRow: Int,
    val targetCol: Int,
    val blockColor: Color,
    var currentGridRow: Int = -1,
    var currentGridCol: Int = -1,
    var trayPos: Offset = Offset.Zero,
    var currentPos: Offset = Offset.Zero,
    var isDragging: Boolean = false,
    var isPlaced: Boolean = false
)

@Composable
fun CrosswordBlocksGame(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val theme = NoorThemeState.active
    val textMeasurer = rememberTextMeasurer()
    val engineState = rememberGameEngineState(initialLevel = 1, initialLives = 3, timeLimitMs = 90000L, targetScore = 3)

    val gridRows = 6
    val gridCols = 6

    val blocks = remember { mutableStateListOf<CrosswordBlock>() }
    val particles = remember { ParticleSystem() }
    var activeBlockId by remember { mutableStateOf<Int?>(null) }

    fun setupLevel(level: Int) {
        blocks.clear()
        when (level) {
            1 -> {
                blocks.add(CrosswordBlock(1, "نور", false, 1, 1, Color(0xFFFFD700)))
                blocks.add(CrosswordBlock(2, "رحمة", true, 1, 3, Color(0xFF64FFDA)))
                blocks.add(CrosswordBlock(3, "مسجد", false, 3, 3, Color(0xFFFFAB00)))
            }
            2 -> {
                blocks.add(CrosswordBlock(1, "إيمان", false, 1, 0, Color(0xFF80D8FF)))
                blocks.add(CrosswordBlock(2, "مسلم", true, 1, 2, Color(0xFFFF80AB)))
                blocks.add(CrosswordBlock(3, "سلام", false, 2, 2, Color(0xFF00E676)))
                blocks.add(CrosswordBlock(4, "مكة", true, 2, 5, Color(0xFFFFD700)))
            }
            else -> {
                blocks.add(CrosswordBlock(1, "القرآن", false, 1, 0, Color(0xFFFFD700)))
                blocks.add(CrosswordBlock(2, "رحمة", true, 1, 3, Color(0xFF64FFDA)))
                blocks.add(CrosswordBlock(3, "صلاة", false, 3, 3, Color(0xFFFFAB00)))
                blocks.add(CrosswordBlock(4, "توبة", true, 2, 5, Color(0xFFB388FF)))
            }
        }
    }

    fun initGame() {
        setupLevel(1)
        engineState.startGame()
    }

    LaunchedEffect(Unit) {
        initGame()
    }

    fun checkWinCondition() {
        if (blocks.all { it.isPlaced && it.currentGridRow == it.targetRow && it.currentGridCol == it.targetCol }) {
            particles.spawnBurst(Offset(350f, 300f), count = 50)
            triggerGameHaptic(context, GameHapticType.SUCCESS)
            SfxManager.playSuccess(context)
            engineState.addScore(1)

            if (engineState.score >= engineState.targetScore) {
                engineState.completeLevel(context, 3)
            } else {
                setupLevel(engineState.score + 1)
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
                title = t("شبكة الكلمات", "Mini Arabic Crossword"),
                level = engineState.score + 1,
                score = engineState.score,
                lives = engineState.lives,
                timeRemainingMs = engineState.timeRemainingMs,
                onBack = onBack,
                onPause = { engineState.pauseGame() }
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = t("اسحب كتل الكلمات الجاهزة وطابقها في الشبكة المتقاطعة!", "Drag word blocks and fit them into the crossword grid!"),
                color = theme.textSecondary,
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

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
                                    val b = blocks.find { block ->
                                        hypot(offset.x - block.currentPos.x, offset.y - block.currentPos.y) < 60f
                                    }
                                    if (b != null) {
                                        activeBlockId = b.id
                                        b.isDragging = true
                                        b.isPlaced = false
                                        triggerGameHaptic(context, GameHapticType.LIGHT)
                                    }
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    val id = activeBlockId ?: return@detectDragGestures
                                    val b = blocks.find { it.id == id } ?: return@detectDragGestures
                                    b.currentPos += dragAmount
                                },
                                onDragEnd = {
                                    val id = activeBlockId ?: return@detectDragGestures
                                    val b = blocks.find { it.id == id } ?: return@detectDragGestures
                                    b.isDragging = false
                                    activeBlockId = null

                                    // Grid dimensions
                                    val gridMarginX = 40f
                                    val gridMarginY = 40f
                                    val gridWidth = size.width - 2 * gridMarginX
                                    val cellSize = gridWidth / gridCols

                                    // Calculate grid row/col
                                    val col = ((b.currentPos.x - gridMarginX) / cellSize).toInt()
                                    val row = ((b.currentPos.y - gridMarginY) / cellSize).toInt()

                                    if (row == b.targetRow && col == b.targetCol) {
                                        b.isPlaced = true
                                        b.currentGridRow = row
                                        b.currentGridCol = col
                                        val cx = gridMarginX + col * cellSize + (if (b.isVertical) cellSize / 2f else (b.word.length * cellSize) / 2f)
                                        val cy = gridMarginY + row * cellSize + (if (b.isVertical) (b.word.length * cellSize) / 2f else cellSize / 2f)
                                        b.currentPos = Offset(cx, cy)

                                        SfxManager.playTap(context)
                                        triggerGameHaptic(context, GameHapticType.LIGHT)
                                        checkWinCondition()
                                    } else {
                                        // Snap back to tray
                                        b.isPlaced = false
                                        b.currentPos = b.trayPos
                                    }
                                }
                            )
                        }
                ) {
                    val canvasW = size.width
                    val canvasH = size.height

                    val gridMarginX = 40f
                    val gridMarginY = 40f
                    val gridWidth = canvasW - 2 * gridMarginX
                    val cellSize = gridWidth / gridCols

                    // 1. Draw Grid Background cells
                    for (r in 0 until gridRows) {
                        for (c in 0 until gridCols) {
                            val rx = gridMarginX + c * cellSize
                            val ry = gridMarginY + r * cellSize

                            // Check if cell is active target cell
                            var isTargetCell = false
                            blocks.forEach { b ->
                                for (i in b.word.indices) {
                                    val tr = if (b.isVertical) b.targetRow + i else b.targetRow
                                    val tc = if (b.isVertical) b.targetCol else b.targetCol + i
                                    if (tr == r && tc == c) isTargetCell = true
                                }
                            }

                            val cellColor = if (isTargetCell) theme.accent.copy(alpha = 0.12f) else Color.DarkGray.copy(alpha = 0.15f)
                            drawRoundRect(
                                color = cellColor,
                                topLeft = Offset(rx + 2f, ry + 2f),
                                size = Size(cellSize - 4f, cellSize - 4f),
                                cornerRadius = CornerRadius(10f)
                            )
                            if (isTargetCell) {
                                drawRoundRect(
                                    color = theme.accent.copy(alpha = 0.35f),
                                    topLeft = Offset(rx + 2f, ry + 2f),
                                    size = Size(cellSize - 4f, cellSize - 4f),
                                    cornerRadius = CornerRadius(10f),
                                    style = Stroke(width = 1.5f)
                                )
                            }
                        }
                    }

                    // 2. Tray Layout
                    val trayY = canvasH - 160f
                    val trayH = 120f
                    drawRoundRect(
                        color = Color(0xFF263238).copy(alpha = 0.5f),
                        topLeft = Offset(20f, trayY),
                        size = Size(canvasW - 40f, trayH),
                        cornerRadius = CornerRadius(20f)
                    )

                    // Position tray blocks
                    val trayStep = (canvasW - 60f) / blocks.size
                    blocks.forEachIndexed { idx, b ->
                        val tx = 30f + (idx + 0.5f) * trayStep
                        val ty = trayY + trayH / 2f
                        b.trayPos = Offset(tx, ty)

                        if (!b.isPlaced && !b.isDragging) {
                            b.currentPos = b.trayPos
                        }
                    }

                    // 3. Draw Word Blocks
                    blocks.forEach { b ->
                        val len = b.word.length
                        val blockW = if (b.isVertical) cellSize else len * cellSize
                        val blockH = if (b.isVertical) len * cellSize else cellSize

                        val scale = if (b.isDragging) 1.08f else 1.0f
                        val bw = blockW * scale
                        val bh = blockH * scale
                        val topLeft = Offset(b.currentPos.x - bw / 2f, b.currentPos.y - bh / 2f)

                        // Main Block Container
                        drawRoundRect(
                            color = b.blockColor,
                            topLeft = topLeft,
                            size = Size(bw, bh),
                            cornerRadius = CornerRadius(16f * scale)
                        )
                        drawRoundRect(
                            color = Color.White.copy(alpha = 0.7f),
                            topLeft = topLeft,
                            size = Size(bw, bh),
                            cornerRadius = CornerRadius(16f * scale),
                            style = Stroke(width = 2.5f * scale)
                        )

                        // Individual Letters inside block
                        for (i in b.word.indices) {
                            val lx = if (b.isVertical) topLeft.x + bw / 2f else topLeft.x + (i + 0.5f) * (cellSize * scale)
                            val ly = if (b.isVertical) topLeft.y + (i + 0.5f) * (cellSize * scale) else topLeft.y + bh / 2f

                            val charText = b.word[i].toString()
                            val textResult = textMeasurer.measure(
                                text = charText,
                                style = TextStyle(
                                    color = Color(0xFF1B5E20),
                                    fontSize = (22 * scale).sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            drawText(
                                textLayoutResult = textResult,
                                topLeft = Offset(lx - textResult.size.width / 2f, ly - textResult.size.height / 2f)
                            )
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
