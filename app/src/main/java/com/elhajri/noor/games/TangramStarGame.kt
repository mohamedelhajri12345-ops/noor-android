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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.audio.SfxManager
import com.elhajri.noor.games.engine.*
import com.elhajri.noor.settings.t
import com.elhajri.noor.theme.NoorThemeState
import com.elhajri.noor.ui.noorGlassCard
import kotlin.math.cos
import kotlin.math.sin

/**
 * Game 05: Islamic Star Tangram (تنجرام النجمة الإسلامية)
 * Category: ألعاب ذكاء
 * Mechanic: Drag and rotate geometric pieces to form an 8-pointed Islamic star.
 */
private data class TangramPiece(
    val id: Int,
    var currentOffset: Offset,
    val targetOffset: Offset,
    var rotationAngle: Float = 0f,
    val targetAngle: Float = 0f,
    var isSnapped: Boolean = false,
    val color: Color
)

@Composable
fun TangramStarGame(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val theme = NoorThemeState.active
    val engineState = rememberGameEngineState(initialLevel = 1, initialLives = 3, timeLimitMs = 60000L, targetScore = 8)

    val pieces = remember { mutableStateListOf<TangramPiece>() }
    val particles = remember { ParticleSystem() }

    fun initGame() {
        pieces.clear()
        val centerX = 250f
        val centerY = 250f
        val radius = 100f

        // 8 points for an 8-pointed star
        for (i in 0 until 8) {
            val angleRad = Math.toRadians((i * 45).toDouble()).toFloat()
            val tx = centerX + radius * cos(angleRad)
            val ty = centerY + radius * sin(angleRad)

            val sx = 80f + (i % 4) * 80f
            val sy = 500f + (i / 4) * 80f

            pieces.add(
                TangramPiece(
                    id = i,
                    currentOffset = Offset(sx, sy),
                    targetOffset = Offset(tx, ty),
                    targetAngle = (i * 45).toFloat(),
                    color = if (i % 2 == 0) Color(0xFFFFD700) else theme.accent
                )
            )
        }
        engineState.startGame()
    }

    LaunchedEffect(Unit) {
        initGame()
    }

    var selectedPieceId by remember { mutableStateOf<Int?>(null) }

    fun checkSnaps() {
        var snappedCount = 0
        pieces.forEach { piece ->
            if (!piece.isSnapped) {
                val dx = piece.currentOffset.x - piece.targetOffset.x
                val dy = piece.currentOffset.y - piece.targetOffset.y
                val distSq = dx * dx + dy * dy
                if (distSq < 1600f) { // Within 40px radius snap
                    piece.currentOffset = piece.targetOffset
                    piece.rotationAngle = piece.targetAngle
                    piece.isSnapped = true
                    triggerGameHaptic(context, GameHapticType.SUCCESS)
                    SfxManager.playSuccess(context)
                    particles.spawnBurst(piece.targetOffset, count = 15)
                    engineState.addScore(1)
                }
            }
            if (piece.isSnapped) snappedCount++
        }

        if (snappedCount == pieces.size) {
            SfxManager.playSuccess(context)
            engineState.completeLevel(context, 3)
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
                title = t("تنجرام النجمة الإسلامية", "Islamic Star Tangram"),
                level = engineState.level,
                score = engineState.score,
                lives = engineState.lives,
                timeRemainingMs = engineState.timeRemainingMs,
                onBack = onBack,
                onPause = { engineState.pauseGame() }
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = t("اسحب القطع الهندسية وأسقطها في مكانها المناسب لتشكيل النجمة الثمانية!", "Drag & drop geometric pieces into position to form the 8-pointed star!"),
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
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    for (p in pieces) {
                                        if (!p.isSnapped) {
                                            val dx = offset.x - p.currentOffset.x
                                            val dy = offset.y - p.currentOffset.y
                                            if (dx * dx + dy * dy < 2500f) { // 50px tap radius
                                                selectedPieceId = p.id
                                                break
                                            }
                                        }
                                    }
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    val targetId = selectedPieceId ?: return@detectDragGestures
                                    val piece = pieces.find { it.id == targetId } ?: return@detectDragGestures
                                    if (!piece.isSnapped) {
                                        piece.currentOffset += dragAmount
                                    }
                                },
                                onDragEnd = {
                                    selectedPieceId = null
                                    checkSnaps()
                                }
                            )
                        }
                ) {
                    // Draw target star silhouette guide
                    pieces.forEach { p ->
                        drawCircle(
                            color = Color.White.copy(alpha = 0.15f),
                            radius = 24f,
                            center = p.targetOffset,
                            style = Stroke(width = 3f)
                        )
                    }

                    // Draw pieces
                    pieces.forEach { p ->
                        val path = Path().apply {
                            val r = 28f
                            moveTo(p.currentOffset.x, p.currentOffset.y - r)
                            lineTo(p.currentOffset.x + r, p.currentOffset.y + r)
                            lineTo(p.currentOffset.x - r, p.currentOffset.y + r)
                            close()
                        }
                        drawPath(
                            path = path,
                            color = if (p.isSnapped) Color(0xFFFFD700) else p.color
                        )
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
