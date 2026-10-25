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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.audio.SfxManager
import com.elhajri.noor.games.engine.*
import com.elhajri.noor.settings.t
import com.elhajri.noor.theme.NoorThemeState
import com.elhajri.noor.ui.noorGlassCard

/**
 * Game 07: Oasis Aqueduct Pipes / Pipe Irrigation (قنوات السقيا)
 * Category: ألعاب ذكاء
 * Mechanic: Rotate pipe tiles 90 degrees to irrigate date palms from the water spring.
 */
private enum class PipeType { STRAIGHT, CORNER }

private data class IrrigationPipe(
    val row: Int,
    val col: Int,
    val type: PipeType,
    var rotation: Float, // 0, 90, 180, 270
    var isConnected: Boolean = false
)

@Composable
fun PipeIrrigationGame(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val theme = NoorThemeState.active
    val engineState = rememberGameEngineState(initialLevel = 1, initialLives = 3, timeLimitMs = 60000L, targetScore = 1)

    val gridSide = 4
    val pipes = remember { mutableStateListOf<IrrigationPipe>() }
    var movesCount by remember { mutableStateOf(0) }
    val particles = remember { ParticleSystem() }

    fun initPuzzle() {
        movesCount = 0
        pipes.clear()

        // 4x4 Grid setup
        for (r in 0 until gridSide) {
            for (c in 0 until gridSide) {
                val pType = if ((r + c) % 2 == 0) PipeType.CORNER else PipeType.STRAIGHT
                val initRot = ((r + c + 1) * 90f) % 360f
                pipes.add(IrrigationPipe(row = r, col = c, type = pType, rotation = initRot))
            }
        }
        engineState.startGame()
    }

    LaunchedEffect(Unit) {
        initPuzzle()
    }

    fun checkFlow() {
        // Simplified flow check: check if key path pipes are aligned
        // For level 1: source (0,0) to target (3,3)
        var connectedCount = 0
        pipes.forEach { p ->
            if (p.row == 0 && p.col == 0 && (p.rotation == 90f || p.rotation == 180f)) p.isConnected = true
            else if (p.row == 3 && p.col == 3 && (p.rotation == 0f || p.rotation == 270f)) p.isConnected = true
            else p.isConnected = (p.rotation == 0f || p.rotation == 90f)

            if (p.isConnected) connectedCount++
        }

        if (connectedCount >= 12) { // 75% of grid connected
            particles.spawnBurst(Offset(300f, 300f), count = 40)
            SfxManager.playSuccess(context)
            engineState.addScore(100)
            engineState.completeLevel(context, 3)
        }
    }

    fun handlePipeTap(tapPos: Offset, sizePx: Float) {
        if (engineState.playState != GamePlayState.PLAYING) return

        val cellSize = sizePx / gridSide
        val col = (tapPos.x / cellSize).toInt().coerceIn(0, gridSide - 1)
        val row = (tapPos.y / cellSize).toInt().coerceIn(0, gridSide - 1)

        val pipe = pipes.find { it.row == row && it.col == col } ?: return
        pipe.rotation = (pipe.rotation + 90f) % 360f
        movesCount++

        triggerGameHaptic(context, GameHapticType.LIGHT)
        SfxManager.playTap(context)

        checkFlow()
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
                title = t("قنوات السقيا", "Oasis Pipe Irrigation"),
                level = engineState.level,
                score = movesCount,
                lives = engineState.lives,
                timeRemainingMs = engineState.timeRemainingMs,
                onBack = onBack,
                onPause = { engineState.pauseGame() }
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = t("أدر الأنابيب لتوصيل المياه من نبع الواحة إلى أشجار النخيل!", "Rotate pipes to connect water from the oasis spring to the date palms!"),
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
                    val canvasSize = minOf(maxWidth, maxHeight)

                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .gameGestureDetector(
                                onTap = { pos -> handlePipeTap(pos, canvasSize.toPx()) }
                            )
                    ) {
                        val sidePx = canvasSize.toPx()
                        val cellPx = sidePx / gridSide

                        pipes.forEach { pipe ->
                            val left = pipe.col * cellPx
                            val top = pipe.row * cellPx
                            val center = Offset(left + cellPx / 2f, top + cellPx / 2f)

                            drawRoundRect(
                                color = theme.accent.copy(alpha = 0.1f),
                                topLeft = Offset(left + 2f, top + 2f),
                                size = Size(cellPx - 4f, cellPx - 4f),
                                cornerRadius = CornerRadius(12f)
                            )

                            rotate(degrees = pipe.rotation, pivot = center) {
                                val pipeColor = if (pipe.isConnected) Color(0xFF00E676) else theme.accent
                                if (pipe.type == PipeType.STRAIGHT) {
                                    drawLine(
                                        color = pipeColor,
                                        start = Offset(center.x, top + 8f),
                                        end = Offset(center.x, top + cellPx - 8f),
                                        strokeWidth = 12f
                                    )
                                } else {
                                    drawLine(
                                        color = pipeColor,
                                        start = Offset(center.x, top + 8f),
                                        end = center,
                                        strokeWidth = 12f
                                    )
                                    drawLine(
                                        color = pipeColor,
                                        start = center,
                                        end = Offset(left + cellPx - 8f, center.y),
                                        strokeWidth = 12f
                                    )
                                }
                            }
                        }

                        // Draw source indicator (top-left) and palm indicator (bottom-right)
                        drawCircle(
                            color = Color(0xFF80D8FF),
                            radius = cellPx * 0.25f,
                            center = Offset(cellPx / 2f, cellPx / 2f)
                        )

                        drawCircle(
                            color = Color(0xFFFFD700),
                            radius = cellPx * 0.25f,
                            center = Offset(sidePx - cellPx / 2f, sidePx - cellPx / 2f)
                        )

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
