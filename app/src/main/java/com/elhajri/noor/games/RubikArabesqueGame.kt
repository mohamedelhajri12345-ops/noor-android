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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.audio.SfxManager
import com.elhajri.noor.games.engine.*
import com.elhajri.noor.settings.t
import com.elhajri.noor.theme.NoorThemeState
import com.elhajri.noor.ui.noorGlassCard
import kotlin.random.Random

/**
 * Game 06: Rubik Arabesque (مكعب المقرنصات)
 * Category: ألعاب ذكاء
 * Mechanic: Tap tile rings to rotate arabesque pattern segments into alignment.
 */
private data class ArabesqueTile(
    val row: Int,
    val col: Int,
    var currentAngle: Float, // 0, 90, 180, 270
    val targetAngle: Float = 0f
)

@Composable
fun RubikArabesqueGame(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val theme = NoorThemeState.active
    val engineState = rememberGameEngineState(initialLevel = 1, initialLives = 3, timeLimitMs = 60000L, targetScore = 1)

    val gridSide = 3
    val tiles = remember { mutableStateListOf<ArabesqueTile>() }
    var movesCount by remember { mutableStateOf(0) }
    val particles = remember { ParticleSystem() }

    fun initPuzzle() {
        movesCount = 0
        tiles.clear()
        val angles = listOf(90f, 180f, 270f)
        for (r in 0 until gridSide) {
            for (c in 0 until gridSide) {
                val initAngle = angles[Random.nextInt(angles.size)]
                tiles.add(ArabesqueTile(row = r, col = c, currentAngle = initAngle))
            }
        }
        engineState.startGame()
    }

    LaunchedEffect(Unit) {
        initPuzzle()
    }

    fun handleTileTap(tapPos: Offset, sizePx: Float) {
        if (engineState.playState != GamePlayState.PLAYING) return

        val cellSize = sizePx / gridSide
        val col = (tapPos.x / cellSize).toInt().coerceIn(0, gridSide - 1)
        val row = (tapPos.y / cellSize).toInt().coerceIn(0, gridSide - 1)

        val tile = tiles.find { it.row == row && it.col == col } ?: return
        tile.currentAngle = (tile.currentAngle + 90f) % 360f
        movesCount++

        triggerGameHaptic(context, GameHapticType.LIGHT)
        SfxManager.playTap(context)

        // Check if all tiles aligned to targetAngle (0f)
        if (tiles.all { it.currentAngle == it.targetAngle }) {
            particles.spawnBurst(Offset(sizePx / 2f, sizePx / 2f), count = 35)
            SfxManager.playSuccess(context)
            engineState.addScore(100)
            engineState.completeLevel(context, if (movesCount <= 10) 3 else 2)
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
                title = t("مكعب المقرنصات", "Rubik Arabesque"),
                level = engineState.level,
                score = movesCount,
                lives = engineState.lives,
                timeRemainingMs = engineState.timeRemainingMs,
                onBack = onBack,
                onPause = { engineState.pauseGame() }
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = t("انقر على البلاطات لتدوير النقوش الإسلامية ومحاذاتها لتكتمل اللوحة!", "Tap tiles to rotate arabesque patterns and align the master ornament!"),
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
                    val density = LocalDensity.current
                    val canvasSize = minOf(maxWidth, maxHeight)
                    val canvasSizePx = with(density) { canvasSize.toPx() }

                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .gameGestureDetector(
                                onTap = { pos -> handleTileTap(pos, canvasSizePx) }
                            )
                    ) {
                        val sidePx = canvasSizePx
                        val cellPx = sidePx / gridSide

                        tiles.forEach { tile ->
                            val left = tile.col * cellPx
                            val top = tile.row * cellPx
                            val center = Offset(left + cellPx / 2f, top + cellPx / 2f)

                            rotate(degrees = tile.currentAngle, pivot = center) {
                                // Draw tile box
                                drawRoundRect(
                                    color = theme.accent.copy(alpha = 0.15f),
                                    topLeft = Offset(left + 4f, top + 4f),
                                    size = Size(cellPx - 8f, cellPx - 8f),
                                    cornerRadius = CornerRadius(16f)
                                )

                                // Draw arabesque pattern motif (diagonal arc and star line)
                                drawCircle(
                                    color = if (tile.currentAngle == 0f) Color(0xFFFFD700) else theme.accent,
                                    radius = cellPx * 0.28f,
                                    center = center,
                                    style = Stroke(width = 6f)
                                )

                                drawLine(
                                    color = theme.accent,
                                    start = Offset(center.x - cellPx * 0.3f, center.y),
                                    end = Offset(center.x + cellPx * 0.3f, center.y),
                                    strokeWidth = 5f
                                )
                            }
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
