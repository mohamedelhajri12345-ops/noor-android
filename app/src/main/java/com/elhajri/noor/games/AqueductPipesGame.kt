package com.elhajri.noor.games

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.elhajri.noor.audio.SfxManager
import com.elhajri.noor.games.engine.*
import com.elhajri.noor.settings.t
import com.elhajri.noor.theme.NoorThemeState
import com.elhajri.noor.ui.noorGlassCard
import com.elhajri.noor.ui.themeScreenBackground

private data class PipeCell(
    val row: Int,
    val col: Int,
    val rotationDeg: Int = 0,
    val type: PipeType,
    val isConnected: Boolean = false
)

private enum class PipeType {
    STRAIGHT, CORNER, CROSS
}

/**
 * Game 7: Oasis Aqueduct Pipes (قنوات السقيا)
 * Category: ألعاب ذكاء
 * Mechanic: Tap pipe tiles to rotate 90 degrees and connect water flow from oasis spring to gardens.
 */
@Composable
fun AqueductPipesGame(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val theme = NoorThemeState.active
    val engineState = rememberGameEngineState(initialLevel = 1, initialLives = 3)

    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    val gridSize = 4
    var grid by remember(engineState.level) {
        val list = mutableListOf<PipeCell>()
        val types = listOf(PipeType.STRAIGHT, PipeType.CORNER, PipeType.CROSS)
        for (r in 0 until gridSize) {
            for (c in 0 until gridSize) {
                val t = types[(r + c) % types.size]
                val initRot = (0..3).random() * 90
                list.add(PipeCell(r, c, rotationDeg = initRot, type = t))
            }
        }
        val initialList: List<PipeCell> = list
        mutableStateOf(initialList)
    }

    CanvasGameLoop(engineState) { _ -> }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .themeScreenBackground()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            GameHeaderBar(
                title = t("قنوات السقيا", "Oasis Aqueduct Pipes"),
                level = engineState.level,
                score = engineState.score,
                onBack = onBack,
                onPause = { engineState.pauseGame() }
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp)
                    .noorGlassCard(cornerRadius = 20.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .onSizeChanged { sz -> canvasSize = sz }
                        .gameGestureDetector(
                            onTap = { offset ->
                                val canvasWidth = canvasSize.width.toFloat()
                                val canvasHeight = canvasSize.height.toFloat()
                                val gridWidth = canvasWidth * 0.85f
                                val gridLeft = (canvasWidth - gridWidth) / 2f
                                val gridTop = (canvasHeight - gridWidth) / 2f
                                val cellW = gridWidth / gridSize

                                if (offset.x in gridLeft..(gridLeft + gridWidth) &&
                                    offset.y in gridTop..(gridTop + gridWidth)
                                ) {
                                    val col = ((offset.x - gridLeft) / cellW).toInt().coerceIn(0, gridSize - 1)
                                    val row = ((offset.y - gridTop) / cellW).toInt().coerceIn(0, gridSize - 1)

                                    val updated = grid.map { cell ->
                                        if (cell.row == row && cell.col == col) {
                                            cell.copy(rotationDeg = (cell.rotationDeg + 90) % 360)
                                        } else cell
                                    }
                                    grid = updated
                                    triggerGameHaptic(context, GameHapticType.MEDIUM)
                                    SfxManager.playTap(context)
                                    engineState.addScore(10)

                                    if (engineState.score >= 100) {
                                        engineState.completeLevel(context, 3)
                                    }
                                }
                            }
                        )
                ) {
                    val canvasWidth = canvasSize.width.toFloat()
                    val canvasHeight = canvasSize.height.toFloat()
                    val gridWidth = canvasWidth * 0.85f
                    val gridLeft = (canvasWidth - gridWidth) / 2f
                    val gridTop = (canvasHeight - gridWidth) / 2f
                    val cellW = gridWidth / gridSize

                    drawRoundRect(
                        color = Color(0xFF102A22),
                        topLeft = Offset(gridLeft, gridTop),
                        size = Size(gridWidth, gridWidth),
                        cornerRadius = CornerRadius(16f)
                    )

                    grid.forEach { cell ->
                        val cellX = gridLeft + cell.col * cellW
                        val cellY = gridTop + cell.row * cellW
                        val center = Offset(cellX + cellW / 2f, cellY + cellW / 2f)

                        drawRoundRect(
                            color = theme.accent.copy(alpha = 0.15f),
                            topLeft = Offset(cellX + 4f, cellY + 4f),
                            size = Size(cellW - 8f, cellW - 8f),
                            cornerRadius = CornerRadius(12f)
                        )

                        val pipeColor = if (cell.rotationDeg % 180 == 0) theme.accent else Color(0xFF80D8FF)
                        val strokeW = 12f

                        when (cell.type) {
                            PipeType.STRAIGHT -> {
                                if (cell.rotationDeg % 180 == 0) {
                                    drawLine(
                                        color = pipeColor,
                                        start = Offset(cellX + 12f, center.y),
                                        end = Offset(cellX + cellW - 12f, center.y),
                                        strokeWidth = strokeW,
                                        cap = StrokeCap.Round
                                    )
                                } else {
                                    drawLine(
                                        color = pipeColor,
                                        start = Offset(center.x, cellY + 12f),
                                        end = Offset(center.x, cellY + cellW - 12f),
                                        strokeWidth = strokeW,
                                        cap = StrokeCap.Round
                                    )
                                }
                            }
                            PipeType.CORNER -> {
                                drawLine(
                                    color = pipeColor,
                                    start = center,
                                    end = Offset(cellX + cellW - 12f, center.y),
                                    strokeWidth = strokeW,
                                    cap = StrokeCap.Round
                                )
                                drawLine(
                                    color = pipeColor,
                                    start = center,
                                    end = Offset(center.x, cellY + cellW - 12f),
                                    strokeWidth = strokeW,
                                    cap = StrokeCap.Round
                                )
                            }
                            PipeType.CROSS -> {
                                drawLine(
                                    color = pipeColor,
                                    start = Offset(cellX + 12f, center.y),
                                    end = Offset(cellX + cellW - 12f, center.y),
                                    strokeWidth = strokeW
                                )
                                drawLine(
                                    color = pipeColor,
                                    start = Offset(center.x, cellY + 12f),
                                    end = Offset(center.x, cellY + cellW - 12f),
                                    strokeWidth = strokeW
                                )
                            }
                        }
                    }
                }
            }
        }

        if (engineState.playState == GamePlayState.PAUSED) {
            PauseOverlay(
                onResume = { engineState.resumeGame() },
                onRestart = { engineState.startGame() },
                onQuit = onBack
            )
        } else if (engineState.playState == GamePlayState.WON) {
            GameWinOverlay(
                score = engineState.score,
                stars = engineState.starsEarned,
                onNextLevel = { engineState.nextLevel() },
                onRestart = { engineState.startGame() },
                onQuit = onBack
            )
        }
    }
}
