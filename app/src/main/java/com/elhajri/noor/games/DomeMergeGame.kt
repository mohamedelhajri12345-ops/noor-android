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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.audio.SfxManager
import com.elhajri.noor.games.engine.*
import com.elhajri.noor.settings.t
import com.elhajri.noor.settings.toArabicDigits
import com.elhajri.noor.theme.NoorThemeState
import com.elhajri.noor.ui.noorGlassCard
import kotlin.random.Random

/**
 * Game 1: Dome Merge 2048 (دمج المآذن)
 * Category: ألعاب ذكاء
 * Mechanic: 4x4 grid tile merger using swipe gestures.
 */
@Composable
fun DomeMergeGame(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val theme = NoorThemeState.active
    val textMeasurer = rememberTextMeasurer()
    val engineState = rememberGameEngineState(initialLevel = 1, initialLives = -1, targetScore = 1000)

    // 4x4 Grid State
    var grid by remember { mutableStateOf(Array(4) { IntArray(4) { 0 } }) }
    val particles = remember { ParticleSystem() }

    fun spawnNewTile(g: Array<IntArray>): Boolean {
        val emptyCells = mutableListOf<Pair<Int, Int>>()
        for (r in 0 until 4) {
            for (c in 0 until 4) {
                if (g[r][c] == 0) emptyCells.add(r to c)
            }
        }
        if (emptyCells.isEmpty()) return false
        val (r, c) = emptyCells[Random.nextInt(emptyCells.size)]
        g[r][c] = if (Random.nextFloat() < 0.9f) 2 else 4
        return true
    }

    fun initGame() {
        val newGrid = Array(4) { IntArray(4) { 0 } }
        spawnNewTile(newGrid)
        spawnNewTile(newGrid)
        grid = newGrid
        engineState.startGame()
    }

    LaunchedEffect(Unit) {
        initGame()
    }

    fun canMove(g: Array<IntArray>): Boolean {
        for (r in 0 until 4) {
            for (c in 0 until 4) {
                if (g[r][c] == 0) return true
                if (r < 3 && g[r][c] == g[r + 1][c]) return true
                if (c < 3 && g[r][c] == g[r][c + 1]) return true
            }
        }
        return false
    }

    fun handleSwipe(direction: SwipeDirection) {
        if (engineState.playState != GamePlayState.PLAYING) return

        val newGrid = Array(4) { r -> grid[r].clone() }
        var moved = false
        var mergedPoints = 0

        when (direction) {
            SwipeDirection.LEFT -> {
                for (r in 0 until 4) {
                    val row = newGrid[r].filter { it != 0 }.toMutableList()
                    var c = 0
                    while (c < row.size - 1) {
                        if (row[c] == row[c + 1]) {
                            row[c] *= 2
                            mergedPoints += row[c]
                            row.removeAt(c + 1)
                        }
                        c++
                    }
                    while (row.size < 4) row.add(0)
                    for (cIdx in 0 until 4) {
                        if (newGrid[r][cIdx] != row[cIdx]) moved = true
                        newGrid[r][cIdx] = row[cIdx]
                    }
                }
            }
            SwipeDirection.RIGHT -> {
                for (r in 0 until 4) {
                    val row = newGrid[r].filter { it != 0 }.toMutableList()
                    var c = row.size - 1
                    while (c > 0) {
                        if (row[c] == row[c - 1]) {
                            row[c] *= 2
                            mergedPoints += row[c]
                            row.removeAt(c - 1)
                            c--
                        }
                        c--
                    }
                    while (row.size < 4) row.add(0, 0)
                    for (cIdx in 0 until 4) {
                        if (newGrid[r][cIdx] != row[cIdx]) moved = true
                        newGrid[r][cIdx] = row[cIdx]
                    }
                }
            }
            SwipeDirection.UP -> {
                for (c in 0 until 4) {
                    val col = (0 until 4).map { newGrid[it][c] }.filter { it != 0 }.toMutableList()
                    var r = 0
                    while (r < col.size - 1) {
                        if (col[r] == col[r + 1]) {
                            col[r] *= 2
                            mergedPoints += col[r]
                            col.removeAt(r + 1)
                        }
                        r++
                    }
                    while (col.size < 4) col.add(0)
                    for (rIdx in 0 until 4) {
                        if (newGrid[rIdx][c] != col[rIdx]) moved = true
                        newGrid[rIdx][c] = col[rIdx]
                    }
                }
            }
            SwipeDirection.DOWN -> {
                for (c in 0 until 4) {
                    val col = (0 until 4).map { newGrid[it][c] }.filter { it != 0 }.toMutableList()
                    var r = col.size - 1
                    while (r > 0) {
                        if (col[r] == col[r - 1]) {
                            col[r] *= 2
                            mergedPoints += col[r]
                            col.removeAt(r - 1)
                            r--
                        }
                        r--
                    }
                    while (col.size < 4) col.add(0, 0)
                    for (rIdx in 0 until 4) {
                        if (newGrid[rIdx][c] != col[rIdx]) moved = true
                        newGrid[rIdx][c] = col[rIdx]
                    }
                }
            }
        }

        if (moved) {
            triggerGameHaptic(context, GameHapticType.LIGHT)
            SfxManager.playTap(context)
            if (mergedPoints > 0) {
                engineState.addScore(mergedPoints)
                SfxManager.playSuccess(context)
            }
            spawnNewTile(newGrid)
            grid = newGrid

            if (engineState.score >= engineState.targetScore) {
                engineState.completeLevel(context, 3)
            } else if (!canMove(grid)) {
                engineState.failLevel(context)
            }
        }
    }

    CanvasGameLoop(engineState = engineState) { deltaSec ->
        particles.update(deltaSec)
    }

    fun getTileColor(value: Int): Color {
        return when (value) {
            2 -> Color(0xFF1E293B)
            4 -> Color(0xFF0F766E)
            8 -> Color(0xFF0284C7)
            16 -> Color(0xFF7C3AED)
            32 -> Color(0xFFC026D3)
            64 -> Color(0xFFE11D48)
            128 -> Color(0xFFD97706)
            256 -> Color(0xFF059669)
            512 -> Color(0xFF2563EB)
            1024 -> Color(0xFF7E22CE)
            2048 -> Color(0xFFFFD700)
            else -> theme.surface
        }
    }

    fun getTileName(value: Int): String {
        return when (value) {
            2 -> "قبة 1"
            4 -> "قبة 2"
            8 -> "مئذنة"
            16 -> "مسجد"
            32 -> "جامع"
            64 -> "الجامع الكبير"
            128 -> "الأقصى"
            256 -> "النبوي"
            512 -> "الحرام"
            1024 -> "الكعبة"
            2048 -> "نور"
            else -> ""
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
                title = t("دمج المآذن", "Dome Merge 2048"),
                level = engineState.level,
                score = engineState.score,
                lives = -1,
                timeRemainingMs = -1L,
                onBack = onBack,
                onPause = { engineState.pauseGame() }
            )

            Spacer(Modifier.height(16.dp))

            // Game Instructions
            Text(
                text = t("اسحب لإدماج المآذن المتشابهة والوصول لقبة النور!", "Swipe to merge identical domes and reach Noor Dome!"),
                color = theme.textSecondary,
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(Modifier.height(20.dp))

            // Canvas Board
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .padding(16.dp)
                    .noorGlassCard(cornerRadius = 24.dp)
                    .border(2.dp, theme.accent.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                    .gameGestureDetector(
                        onSwipe = { direction -> handleSwipe(direction) }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val boardWidth = size.width
                    val padding = 16f
                    val cellSize = (boardWidth - padding * 5) / 4f

                    // Draw Background cells & Tiles
                    for (r in 0 until 4) {
                        for (c in 0 until 4) {
                            val left = padding + c * (cellSize + padding)
                            val top = padding + r * (cellSize + padding)
                            val cellValue = grid[r][c]

                            // Cell background
                            drawRoundRect(
                                color = Color.White.copy(alpha = 0.05f),
                                topLeft = Offset(left, top),
                                size = Size(cellSize, cellSize),
                                cornerRadius = CornerRadius(16f)
                            )

                            if (cellValue > 0) {
                                val tileColor = getTileColor(cellValue)
                                drawRoundRect(
                                    color = tileColor,
                                    topLeft = Offset(left, top),
                                    size = Size(cellSize, cellSize),
                                    cornerRadius = CornerRadius(16f)
                                )
                                drawRoundRect(
                                    color = Color.White.copy(alpha = 0.2f),
                                    topLeft = Offset(left, top),
                                    size = Size(cellSize, cellSize),
                                    cornerRadius = CornerRadius(16f),
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
                                )

                                // Draw Tile Number & Name
                                val numText = cellValue.toArabicDigits()
                                val nameText = getTileName(cellValue)

                                val numMeasured = textMeasurer.measure(
                                    text = numText,
                                    style = TextStyle(
                                        color = Color.White,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                val nameMeasured = textMeasurer.measure(
                                    text = nameText,
                                    style = TextStyle(
                                        color = Color.White.copy(alpha = 0.85f),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Normal
                                    )
                                )

                                drawText(
                                    textLayoutResult = numMeasured,
                                    topLeft = Offset(
                                        left + (cellSize - numMeasured.size.width) / 2f,
                                        top + (cellSize - numMeasured.size.height) / 2f - 8f
                                    )
                                )
                                drawText(
                                    textLayoutResult = nameMeasured,
                                    topLeft = Offset(
                                        left + (cellSize - nameMeasured.size.width) / 2f,
                                        top + (cellSize - nameMeasured.size.height) / 2f + 16f
                                    )
                                )
                            }
                        }
                    }

                    // Draw Particles
                    particles.draw(this)
                }
            }
        }

        // Overlays
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
