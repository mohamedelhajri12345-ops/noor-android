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
 * Game 13: Date Slingshot (مقلاع التمر)
 * Category: مهارة
 * Mechanic: Slingshot physics flinging dates at target blocks and baskets.
 */
private data class TargetBasket(
    val id: Int,
    val center: Offset,
    val radius: Float = 30f,
    var isHit: Boolean = false
)

@Composable
fun DateSlingshotGame(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val theme = NoorThemeState.active
    val engineState = rememberGameEngineState(initialLevel = 1, initialLives = 3, timeLimitMs = 60000L, targetScore = 3)

    val slingAnchor = Offset(120f, 400f)
    var dragPos by remember { mutableStateOf(slingAnchor) }
    var isDragging by remember { mutableStateOf(false) }

    var datePos by remember { mutableStateOf(slingAnchor) }
    var dateVel by remember { mutableStateOf(Offset.Zero) }
    var isFlying by remember { mutableStateOf(false) }

    val targets = remember { mutableStateListOf<TargetBasket>() }
    val particles = remember { ParticleSystem() }

    fun initGame() {
        targets.clear()
        targets.add(TargetBasket(1, Offset(450f, 250f)))
        targets.add(TargetBasket(2, Offset(520f, 380f)))
        targets.add(TargetBasket(3, Offset(480f, 500f)))
        datePos = slingAnchor
        dateVel = Offset.Zero
        isFlying = false
        dragPos = slingAnchor
        engineState.startGame()
    }

    LaunchedEffect(Unit) {
        initGame()
    }

    fun releaseSlingshot() {
        val pullVector = slingAnchor - dragPos
        dateVel = pullVector * 8f
        isFlying = true
        triggerGameHaptic(context, GameHapticType.MEDIUM)
        SfxManager.playTap(context)
    }

    CanvasGameLoop(engineState = engineState) { deltaSec ->
        particles.update(deltaSec)

        if (isFlying && engineState.playState == GamePlayState.PLAYING) {
            val gravity = 400f
            dateVel = Offset(dateVel.x, dateVel.y + gravity * deltaSec)
            datePos += dateVel * deltaSec

            // Check collision with targets
            targets.forEach { target ->
                if (!target.isHit) {
                    val dx = datePos.x - target.center.x
                    val dy = datePos.y - target.center.y
                    if (dx * dx + dy * dy < 2500f) {
                        target.isHit = true
                        particles.spawnBurst(target.center, count = 30)
                        triggerGameHaptic(context, GameHapticType.SUCCESS)
                        SfxManager.playSuccess(context)
                        engineState.addScore(1)

                        if (targets.all { it.isHit }) {
                            engineState.completeLevel(context, 3)
                        }
                    }
                }
            }

            // Reset date if off screen
            if (datePos.x > 800f || datePos.y > 800f || datePos.x < 0f) {
                isFlying = false
                datePos = slingAnchor
                dragPos = slingAnchor
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
                title = t("مقلاع التمر", "Date Slingshot"),
                level = engineState.level,
                score = engineState.score,
                lives = engineState.lives,
                timeRemainingMs = engineState.timeRemainingMs,
                onBack = onBack,
                onPause = { engineState.pauseGame() }
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = t("اسحب المقلاع للخلف وأطلقه لإصابة السلال بالتمر!", "Pull back the elastic slingshot and release to hit target baskets!"),
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
                                    if (!isFlying) {
                                        isDragging = true
                                        dragPos = offset
                                    }
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    if (isDragging) {
                                        dragPos += dragAmount
                                    }
                                },
                                onDragEnd = {
                                    if (isDragging) {
                                        isDragging = false
                                        releaseSlingshot()
                                    }
                                }
                            )
                        }
                ) {
                    // Slingshot base
                    drawLine(
                        color = theme.accent,
                        start = Offset(slingAnchor.x, slingAnchor.y + 80f),
                        end = slingAnchor,
                        strokeWidth = 10f
                    )

                    // Elastic band
                    val currentSlingPos = if (isFlying) datePos else if (isDragging) dragPos else slingAnchor
                    drawLine(
                        color = Color(0xFFFFD700),
                        start = slingAnchor + Offset(-20f, -10f),
                        end = currentSlingPos,
                        strokeWidth = 6f
                    )
                    drawLine(
                        color = Color(0xFFFFD700),
                        start = slingAnchor + Offset(20f, -10f),
                        end = currentSlingPos,
                        strokeWidth = 6f
                    )

                    // Target baskets
                    targets.forEach { target ->
                        if (!target.isHit) {
                            drawCircle(
                                color = theme.accent,
                                radius = target.radius,
                                center = target.center
                            )
                            drawCircle(
                                color = Color(0xFFFFD700),
                                radius = target.radius * 0.5f,
                                center = target.center
                            )
                        }
                    }

                    // Flying / loaded date
                    drawCircle(
                        color = Color(0xFF795548),
                        radius = 16f,
                        center = currentSlingPos
                    )

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
