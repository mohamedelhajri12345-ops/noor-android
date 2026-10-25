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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.audio.SfxManager
import com.elhajri.noor.games.engine.*
import com.elhajri.noor.settings.t
import com.elhajri.noor.theme.NoorThemeState
import com.elhajri.noor.ui.noorGlassCard
import com.elhajri.noor.ui.themeScreenBackground
import kotlin.math.hypot

private data class JigsawPiece(
    val id: Int,
    val targetRow: Int,
    val targetCol: Int,
    val color: Color,
    val label: String,
    val currentOffset: Offset = Offset.Zero,
    val isSnapped: Boolean = false
)

/**
 * Game 3: Mosque Jigsaw (تركيب صور المساجد)
 * Category: ألعاب ذكاء
 * Mechanic: Drag jigsaw puzzle tiles into grid slots to assemble mosque architecture.
 */
@Composable
fun MosqueJigsawGame(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val theme = NoorThemeState.active
    val engineState = rememberGameEngineState(initialLevel = 1, initialLives = 3)
    val textMeasurer = rememberTextMeasurer()

    val rows = 3
    val cols = 3

    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    var pieces by remember(engineState.level) {
        val colors = listOf(
            Color(0xFF2E7D32), Color(0xFF1565C0), Color(0xFFD84315),
            Color(0xFF8E24AA), Color(0xFFF57F17), Color(0xFF00838F),
            Color(0xFF4E342E), Color(0xFF37474F), Color(0xFFAD1457)
        )
        val labels = listOf("قبة", "مئذنة", "محراب", "صحن", "زليج", "منبر", "قوس", "باب", "هلال")
        val pieceList = mutableListOf<JigsawPiece>()
        var id = 0
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                pieceList.add(
                    JigsawPiece(
                        id = id,
                        targetRow = r,
                        targetCol = c,
                        color = colors[id % colors.size],
                        label = labels[id % labels.size]
                    )
                )
                id++
            }
        }
        val initialShuffled: List<JigsawPiece> = pieceList.shuffled()
        mutableStateOf(initialShuffled)
    }

    var activeDragId by remember { mutableStateOf<Int?>(null) }

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
                title = t("تركيب صور المساجد", "Mosque Jigsaw"),
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
                    .onSizeChanged { sz -> canvasSize = sz }
                    .gameGestureDetector(
                        onDragStart = { offset ->
                            val p = pieces.firstOrNull { !it.isSnapped && hypot(it.currentOffset.x - offset.x, it.currentOffset.y - offset.y) < 120f }
                            if (p != null) {
                                activeDragId = p.id
                                triggerGameHaptic(context, GameHapticType.LIGHT)
                            }
                        },
                        onDragMove = { pos, _ ->
                            if (activeDragId != null) {
                                pieces = pieces.map {
                                    if (it.id == activeDragId) it.copy(currentOffset = pos) else it
                                }
                            }
                        },
                        onDragEnd = {
                            activeDragId = null
                        }
                    )
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val canvasWidth = canvasSize.width.toFloat()
                    val canvasHeight = canvasSize.height.toFloat()

                    val gridWidth = canvasWidth * 0.85f
                    val gridHeight = canvasHeight * 0.5f
                    val gridLeft = (canvasWidth - gridWidth) / 2f
                    val gridTop = 40f

                    val cellW = gridWidth / cols
                    val cellH = gridHeight / rows

                    val trayY = gridTop + gridHeight + 60f
                    pieces.forEachIndexed { idx, p ->
                        if (p.currentOffset == Offset.Zero && !p.isSnapped) {
                            val colInTray = idx % 3
                            val rowInTray = idx / 3
                            val initOffset = Offset(
                                gridLeft + colInTray * (cellW + 15f) + cellW / 2f,
                                trayY + rowInTray * (cellH + 15f) + cellH / 2f
                            )
                            pieces = pieces.map { if (it.id == p.id) it.copy(currentOffset = initOffset) else it }
                        }
                    }

                    for (r in 0 until rows) {
                        for (c in 0 until cols) {
                            val slotX = gridLeft + c * cellW
                            val slotY = gridTop + r * cellH

                            drawRoundRect(
                                color = theme.accent.copy(alpha = 0.2f),
                                topLeft = Offset(slotX, slotY),
                                size = Size(cellW - 6f, cellH - 6f),
                                cornerRadius = CornerRadius(12f)
                            )
                            drawRoundRect(
                                color = theme.accent.copy(alpha = 0.5f),
                                topLeft = Offset(slotX, slotY),
                                size = Size(cellW - 6f, cellH - 6f),
                                cornerRadius = CornerRadius(12f),
                                style = Stroke(width = 2f)
                            )
                        }
                    }

                    val updatedList = pieces.map { p ->
                        if (!p.isSnapped) {
                            val targetCenterX = gridLeft + p.targetCol * cellW + cellW / 2f
                            val targetCenterY = gridTop + p.targetRow * cellH + cellH / 2f
                            val dist = hypot(p.currentOffset.x - targetCenterX, p.currentOffset.y - targetCenterY)

                            if (dist < 80f && activeDragId != p.id) {
                                triggerGameHaptic(context, GameHapticType.SUCCESS)
                                SfxManager.playSuccess(context)
                                engineState.addScore(15)
                                p.copy(
                                    currentOffset = Offset(targetCenterX, targetCenterY),
                                    isSnapped = true
                                )
                            } else p
                        } else p
                    }

                    if (updatedList != pieces) {
                        pieces = updatedList
                        if (updatedList.all { it.isSnapped }) {
                            engineState.completeLevel(context, 3)
                        }
                    }

                    pieces.forEach { p ->
                        val center = if (p.isSnapped) {
                            Offset(
                                gridLeft + p.targetCol * cellW + cellW / 2f,
                                gridTop + p.targetRow * cellH + cellH / 2f
                            )
                        } else p.currentOffset

                        val pLeft = center.x - (cellW - 8f) / 2f
                        val pTop = center.y - (cellH - 8f) / 2f

                        drawRoundRect(
                            color = p.color,
                            topLeft = Offset(pLeft, pTop),
                            size = Size(cellW - 8f, cellH - 8f),
                            cornerRadius = CornerRadius(14f)
                        )
                        drawRoundRect(
                            color = if (p.isSnapped) theme.accent else Color.White.copy(alpha = 0.8f),
                            topLeft = Offset(pLeft, pTop),
                            size = Size(cellW - 8f, cellH - 8f),
                            cornerRadius = CornerRadius(14f),
                            style = Stroke(width = if (p.isSnapped) 4f else 2f)
                        )

                        val layoutResult = textMeasurer.measure(
                            text = p.label,
                            style = TextStyle(
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        drawText(
                            textLayoutResult = layoutResult,
                            topLeft = Offset(
                                center.x - layoutResult.size.width / 2f,
                                center.y - layoutResult.size.height / 2f
                            )
                        )
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
