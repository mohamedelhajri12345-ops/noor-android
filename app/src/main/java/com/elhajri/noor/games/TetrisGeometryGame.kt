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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.audio.SfxManager
import com.elhajri.noor.games.engine.*
import com.elhajri.noor.settings.t
import com.elhajri.noor.theme.NoorThemeState
import com.elhajri.noor.ui.noorGlassCard

/**
 * Game 08: Wall Brick Tetris / Tetris Geometry (بناء الأسوار)
 * Category: ألعاب ذكاء
 * Mechanic: Falling geometric carved stone blocks, stack and clear full horizontal rows.
 */
private data class TetrisBlock(
    var x: Int,
    var y: Int,
    val shape: List<Pair<Int, Int>>, // relative (dx, dy)
    val color: Color
)

@Composable
fun TetrisGeometryGame(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val theme = NoorThemeState.active
    val engineState = rememberGameEngineState(initialLevel = 1, initialLives = 3, timeLimitMs = 90000L, targetScore = 80)

    val gridCols = 8
    val gridRows = 12

    val grid = remember { Array(gridRows) { IntArray(gridCols) { 0 } } }
    var activeBlock by remember { mutableStateOf<TetrisBlock?>(null) }
    var dropTimer by remember { mutableFloatStateOf(0f) }
    val particles = remember { ParticleSystem() }

    val shapes = remember {
        listOf(
            listOf(Pair(0, 0), Pair(1, 0), Pair(0, 1), Pair(1, 1)), // Square
            listOf(Pair(0, 0), Pair(0, 1), Pair(0, 2), Pair(0, 3)), // Line
            listOf(Pair(0, 0), Pair(1, 0), Pair(2, 0), Pair(1, 1)), // T-shape
            listOf(Pair(0, 0), Pair(1, 0), Pair(1, 1), Pair(2, 1))  // Z-shape
        )
    }

    fun spawnBlock() {
        val shapeIdx = (0 until shapes.size).random()
        activeBlock = TetrisBlock(
            x = gridCols / 2 - 1,
            y = 0,
            shape = shapes[shapeIdx],
            color = if (shapeIdx % 2 == 0) Color(0xFFFFD700) else theme.accent
        )
    }

    fun initGame() {
        for (r in 0 until gridRows) {
            for (c in 0 until gridCols) {
                grid[r][c] = 0
            }
        }
        spawnBlock()
        engineState.startGame()
    }

    LaunchedEffect(Unit) {
        initGame()
    }

    fun canPlace(block: TetrisBlock, newX: Int, newY: Int): Boolean {
        block.shape.forEach { (dx, dy) ->
            val bx = newX + dx
            val by = newY + dy
            if (bx !in 0 until gridCols || by !in 0 until gridRows) return false
            if (grid[by][bx] != 0) return false
        }
        return true
    }

    fun lockBlock() {
        val curr = activeBlock ?: return
        curr.shape.forEach { (dx, dy) ->
            val bx = curr.x + dx
            val by = curr.y + dy
            if (by in 0 until gridRows && bx in 0 until gridCols) {
                grid[by][bx] = 1
            }
        }

        // Check for completed rows
        var clearedRows = 0
        for (r in gridRows - 1 downTo 0) {
            if (grid[r].all { it == 1 }) {
                clearedRows++
                for (moveR in r downTo 1) {
                    grid[moveR] = grid[moveR - 1].clone()
                }
                grid[0] = IntArray(gridCols) { 0 }
            }
        }

        if (clearedRows > 0) {
            val pts = clearedRows * 20
            engineState.addScore(pts)
            triggerGameHaptic(context, GameHapticType.SUCCESS)
            SfxManager.playSuccess(context)
            particles.spawnBurst(Offset(200f, 400f), count = 25 * clearedRows)

            if (engineState.score >= engineState.targetScore) {
                engineState.completeLevel(context, 3)
            }
        }

        spawnBlock()

        // Game over check if newly spawned block collides immediately
        val newB = activeBlock
        if (newB != null && !canPlace(newB, newB.x, newB.y)) {
            if (engineState.loseLife()) {
                engineState.failLevel(context)
            } else {
                initGame()
            }
        }
    }

    fun moveActive(dx: Int) {
        val curr = activeBlock ?: return
        if (canPlace(curr, curr.x + dx, curr.y)) {
            curr.x += dx
            triggerGameHaptic(context, GameHapticType.LIGHT)
            SfxManager.playTap(context)
        }
    }

    fun softDrop() {
        val curr = activeBlock ?: return
        if (canPlace(curr, curr.x, curr.y + 1)) {
            curr.y += 1
        } else {
            lockBlock()
        }
    }

    CanvasGameLoop(engineState = engineState) { deltaSec ->
        particles.update(deltaSec)

        if (engineState.playState == GamePlayState.PLAYING) {
            dropTimer += deltaSec
            if (dropTimer >= 0.7f) {
                dropTimer = 0f
                softDrop()
            }
        }
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
                title = t("بناء الأسوار", "Wall Brick Tetris"),
                level = engineState.level,
                score = engineState.score,
                lives = engineState.lives,
                timeRemainingMs = engineState.timeRemainingMs,
                onBack = onBack,
                onPause = { engineState.pauseGame() }
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = t("رتب الكتل الحجرية المنحوتة لإكمال الصفوف الأفقية وبناء السور!", "Arrange carved stone blocks to complete horizontal rows and build the wall!"),
                color = theme.textSecondary,
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(12.dp)
                    .noorGlassCard(cornerRadius = 24.dp)
                    .border(2.dp, theme.accent.copy(alpha = 0.3f), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                BoxWithConstraints(
                    modifier = Modifier.fillMaxHeight().aspectRatio(gridCols.toFloat() / gridRows.toFloat()).padding(8.dp)
                ) {
                    val cellWidth = maxWidth / gridCols
                    val cellHeight = maxHeight / gridRows

                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .gameGestureDetector(
                                onTap = { pos ->
                                    val midX = constraints.maxWidth / 2f
                                    if (pos.x < midX) moveActive(-1) else moveActive(1)
                                }
                            )
                    ) {
                        val cw = cellWidth.toPx()
                        val ch = cellHeight.toPx()

                        // Grid background
                        for (r in 0 until gridRows) {
                            for (c in 0 until gridCols) {
                                if (grid[r][c] == 1) {
                                    drawRoundRect(
                                        color = Color(0xFFFFD700),
                                        topLeft = Offset(c * cw + 1f, r * ch + 1f),
                                        size = Size(cw - 2f, ch - 2f),
                                        cornerRadius = CornerRadius(6f)
                                    )
                                }
                            }
                        }

                        // Active piece
                        activeBlock?.let { curr ->
                            curr.shape.forEach { (dx, dy) ->
                                val bx = curr.x + dx
                                val by = curr.y + dy
                                if (by in 0 until gridRows && bx in 0 until gridCols) {
                                    drawRoundRect(
                                        color = curr.color,
                                        topLeft = Offset(bx * cw + 1f, by * ch + 1f),
                                        size = Size(cw - 2f, ch - 2f),
                                        cornerRadius = CornerRadius(6f)
                                    )
                                }
                            }
                        }

                        particles.draw(this)
                    }
                }
            }

            // Bottom control buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { moveActive(-1) },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.accent)
                ) {
                    Text("◀", color = Color.Black)
                }

                Button(
                    onClick = { softDrop() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700))
                ) {
                    Text(t("إسقاط", "Drop"), color = Color.Black)
                }

                Button(
                    onClick = { moveActive(1) },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.accent)
                ) {
                    Text("▶", color = Color.Black)
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
