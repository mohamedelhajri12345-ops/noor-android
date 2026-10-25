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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.audio.SfxManager
import com.elhajri.noor.games.engine.*
import com.elhajri.noor.settings.t
import com.elhajri.noor.theme.NoorThemeState
import com.elhajri.noor.ui.noorGlassCard

/**
 * Game 04: Caravan Path Unblock (مسار القافلة)
 * Category: ألعاب ذكاء
 * Mechanic: Sliding block puzzle freeing the lead Kaaba caravan block.
 */
private data class UnblockBlock(
    val id: Int,
    var x: Int,
    var y: Int,
    val length: Int,
    val isVertical: Boolean,
    val isTarget: Boolean = false
)

@Composable
fun CaravanUnblockGame(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val theme = NoorThemeState.active
    val engineState = rememberGameEngineState(initialLevel = 1, initialLives = 3, timeLimitMs = 90000L, targetScore = 1)

    val gridCols = 6
    val gridRows = 6

    var movesCount by remember { mutableStateOf(0) }
    val blocks = remember { mutableStateListOf<UnblockBlock>() }
    val particles = remember { ParticleSystem() }

    fun initPuzzle() {
        movesCount = 0
        blocks.clear()
        // Level 1 layout: 6x6 grid
        // Target block at row 2, x=0, length=2 (horizontal)
        blocks.add(UnblockBlock(1, x = 0, y = 2, length = 2, isVertical = false, isTarget = true))
        // Obstructing blocks
        blocks.add(UnblockBlock(2, x = 2, y = 1, length = 3, isVertical = true))
        blocks.add(UnblockBlock(3, x = 0, y = 0, length = 2, isVertical = false))
        blocks.add(UnblockBlock(4, x = 3, y = 0, length = 2, isVertical = true))
        blocks.add(UnblockBlock(5, x = 4, y = 2, length = 2, isVertical = true))
        blocks.add(UnblockBlock(6, x = 1, y = 4, length = 3, isVertical = false))
        blocks.add(UnblockBlock(7, x = 4, y = 4, length = 2, isVertical = true))
        engineState.startGame()
    }

    LaunchedEffect(Unit) {
        initPuzzle()
    }

    var selectedBlockId by remember { mutableStateOf<Int?>(null) }

    fun isOccupied(x: Int, y: Int, ignoreBlockId: Int): Boolean {
        if (x !in 0 until gridCols || y !in 0 until gridRows) return true
        for (b in blocks) {
            if (b.id == ignoreBlockId) continue
            for (i in 0 until b.length) {
                val bx = if (b.isVertical) b.x else b.x + i
                val by = if (b.isVertical) b.y + i else b.y
                if (bx == x && by == y) return true
            }
        }
        return false
    }

    fun tryMoveBlock(block: UnblockBlock, dx: Int, dy: Int) {
        if (engineState.playState != GamePlayState.PLAYING) return
        if (dx == 0 && dy == 0) return

        if (block.isVertical && dx != 0) return
        if (!block.isVertical && dy != 0) return

        val stepX = dx.coerceIn(-1, 1)
        val stepY = dy.coerceIn(-1, 1)

        val newX = block.x + stepX
        val newY = block.y + stepY

        // Check bounds and collision
        var canMove = true
        for (i in 0 until block.length) {
            val checkX = if (block.isVertical) newX else newX + i
            val checkY = if (block.isVertical) newY + i else newY
            if (isOccupied(checkX, checkY, block.id)) {
                canMove = false
                break
            }
        }

        if (canMove) {
            block.x = newX
            block.y = newY
            movesCount++
            triggerGameHaptic(context, GameHapticType.LIGHT)
            SfxManager.playTap(context)

            // Check win condition (Target block reaches x = 4)
            if (block.isTarget && block.x == 4) {
                particles.spawnBurst(Offset(300f, 300f), count = 40)
                SfxManager.playSuccess(context)
                engineState.addScore(100)
                engineState.completeLevel(context, if (movesCount <= 12) 3 else 2)
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
                title = t("مسار القافلة", "Caravan Unblock"),
                level = engineState.level,
                score = movesCount,
                lives = engineState.lives,
                timeRemainingMs = engineState.timeRemainingMs,
                onBack = onBack,
                onPause = { engineState.pauseGame() }
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = t("حرك الكتل الخشبية لإفساح الطريق أمام قافلة الكعبة للوصول للمخرج!", "Slide wooden blocks to clear the path for the Kaaba caravan to exit!"),
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
                    .border(2.dp, theme.accent.copy(alpha = 0.3f), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                BoxWithConstraints(
                    modifier = Modifier.aspectRatio(1f).padding(12.dp)
                ) {
                    val cellSize = minOf(maxWidth, maxHeight) / gridCols

                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDragStart = { offset ->
                                        val col = (offset.x / cellSize.toPx()).toInt()
                                        val row = (offset.y / cellSize.toPx()).toInt()
                                        for (b in blocks) {
                                            for (i in 0 until b.length) {
                                                val bx = if (b.isVertical) b.x else b.x + i
                                                val by = if (b.isVertical) b.y + i else b.y
                                                if (bx == col && by == row) {
                                                    selectedBlockId = b.id
                                                    break
                                                }
                                            }
                                        }
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        val targetId = selectedBlockId ?: return@detectDragGestures
                                        val b = blocks.find { it.id == targetId } ?: return@detectDragGestures
                                        val thresh = cellSize.toPx() * 0.35f
                                        if (b.isVertical) {
                                            if (dragAmount.y > thresh) {
                                                tryMoveBlock(b, 0, 1)
                                            } else if (dragAmount.y < -thresh) {
                                                tryMoveBlock(b, 0, -1)
                                            }
                                        } else {
                                            if (dragAmount.x > thresh) {
                                                tryMoveBlock(b, 1, 0)
                                            } else if (dragAmount.x < -thresh) {
                                                tryMoveBlock(b, -1, 0)
                                            }
                                        }
                                    },
                                    onDragEnd = { selectedBlockId = null }
                                )
                            }
                    ) {
                        val cellPx = cellSize.toPx()

                        // Grid background lines
                        for (r in 0..gridRows) {
                            drawLine(
                                color = theme.textSecondary.copy(alpha = 0.15f),
                                start = Offset(0f, r * cellPx),
                                end = Offset(gridCols * cellPx, r * cellPx),
                                strokeWidth = 2f
                            )
                        }
                        for (c in 0..gridCols) {
                            drawLine(
                                color = theme.textSecondary.copy(alpha = 0.15f),
                                start = Offset(c * cellPx, 0f),
                                end = Offset(c * cellPx, gridRows * cellPx),
                                strokeWidth = 2f
                            )
                        }

                        // Exit gate indicator at row 2 right edge
                        drawRect(
                            color = Color(0xFFFFD700).copy(alpha = 0.5f),
                            topLeft = Offset(gridCols * cellPx - 8f, 2 * cellPx),
                            size = Size(8f, cellPx)
                        )

                        // Draw blocks
                        blocks.forEach { b ->
                            val blockWidth = if (b.isVertical) cellPx * 0.9f else (cellPx * b.length) - cellPx * 0.1f
                            val blockHeight = if (b.isVertical) (cellPx * b.length) - cellPx * 0.1f else cellPx * 0.9f
                            val left = b.x * cellPx + cellPx * 0.05f
                            val top = b.y * cellPx + cellPx * 0.05f

                            val blockColor = if (b.isTarget) Color(0xFFFFD700) else theme.accent.copy(alpha = 0.85f)

                            drawRoundRect(
                                color = blockColor,
                                topLeft = Offset(left, top),
                                size = Size(blockWidth, blockHeight),
                                cornerRadius = CornerRadius(16f)
                            )
                        }

                        particles.draw(this)
                    }
                }
            }
        }

        when (engineState.playState) {
            GamePlayState.PAUSED -> PauseOverlay(
                onResume = { engineState.resumeGame() },
                onRestart = { initPuzzle() },
                onQuit = onBack
            )
            GamePlayState.WON -> GameWinOverlay(
                score = engineState.score,
                stars = engineState.starsEarned,
                onRestart = { initPuzzle() },
                onQuit = onBack
            )
            GamePlayState.LOST -> GameLoseOverlay(
                score = engineState.score,
                onRestart = { initPuzzle() },
                onQuit = onBack
            )
            else -> {}
        }
    }
}
