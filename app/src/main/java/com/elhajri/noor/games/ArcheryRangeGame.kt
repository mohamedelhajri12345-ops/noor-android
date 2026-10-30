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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
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
import kotlin.math.*
import kotlin.random.Random

/**
 * Game 20: Desert Archery Range (رماية السهام)
 * ID: game_20_archery_range
 * Category: مهارة
 * Mechanic: Archery precision target shooting. Pull back bowstring, move aim sight over target rings,
 * compensate for wind direction/speed, and release to shoot arrow.
 */
private enum class ArrowState {
    READY,
    AIMING,
    FLYING,
    HIT
}

private data class StuckArrow(
    val offset: Offset, // Impact offset relative to target center
    val scoreEarned: Int
)

private data class WindVector(
    val vx: Float,
    val vy: Float,
    val speedMps: Float
)

@Composable
fun ArcheryRangeGame(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val theme = NoorThemeState.active
    val textMeasurer = rememberTextMeasurer()

    val engineState = rememberGameEngineState(
        initialLevel = 1,
        initialLives = 3,
        targetScore = 250
    )

    var arrowState by remember { mutableStateOf(ArrowState.READY) }
    var arrowsRemaining by remember { mutableIntStateOf(5) }

    var dragStartPos by remember { mutableStateOf<Offset?>(null) }
    var currentDragPos by remember { mutableStateOf<Offset?>(null) }

    var sightOffset by remember { mutableStateOf(Offset.Zero) }
    var flightProgress by remember { mutableFloatStateOf(0f) }
    var targetOscillationTime by remember { mutableFloatStateOf(0f) }

    var currentWind by remember {
        mutableStateOf(
            WindVector(
                vx = (Random.nextFloat() - 0.5f) * 50f,
                vy = (Random.nextFloat() - 0.5f) * 30f,
                speedMps = (1.5f + Random.nextFloat() * 5f)
            )
        )
    }

    val stuckArrows = remember { mutableStateListOf<StuckArrow>() }
    val particles = remember { ParticleSystem() }
    var lastHitMessage by remember { mutableStateOf("") }

    fun generateNewWind() {
        val speed = 2f + engineState.level * 1.5f + Random.nextFloat() * 3f
        val angleRad = Random.nextFloat() * 2f * PI.toFloat()
        currentWind = WindVector(
            vx = cos(angleRad) * (speed * 8f),
            vy = sin(angleRad) * (speed * 6f),
            speedMps = (speed * 10f).roundToInt() / 10f
        )
    }

    fun initGame() {
        arrowState = ArrowState.READY
        arrowsRemaining = 5
        dragStartPos = null
        currentDragPos = null
        sightOffset = Offset.Zero
        flightProgress = 0f
        targetOscillationTime = 0f
        lastHitMessage = ""
        stuckArrows.clear()

        generateNewWind()
        engineState.startGame()
    }

    LaunchedEffect(engineState.level) {
        initGame()
    }

    CanvasGameLoop(engineState = engineState) { deltaSec ->
        particles.update(deltaSec)

        if (engineState.playState == GamePlayState.PLAYING) {
            targetOscillationTime += deltaSec

            if (arrowState == ArrowState.FLYING) {
                flightProgress += 2.5f * deltaSec // Arrow flight time ~0.4s

                if (flightProgress >= 1.0f) {
                    flightProgress = 1.0f
                    arrowState = ArrowState.HIT

                    val canvasWidth = 480f
                    val canvasHeight = 720f
                    val baseTargetCenter = Offset(canvasWidth / 2f, canvasHeight * 0.36f)

                    val oscillationX = if (engineState.level >= 2) sin(targetOscillationTime * 2f) * 45f else 0f
                    val currentTargetCenter = baseTargetCenter + Offset(oscillationX, 0f)

                    // Final impact position = sight location + wind drift
                    val finalImpactPos = currentTargetCenter + sightOffset + Offset(currentWind.vx, currentWind.vy)
                    val distToCenter = (finalImpactPos - currentTargetCenter).getDistance()

                    val baseRadius = max(60f, 110f - engineState.level * 10f)

                    val earnedScore = when {
                        distToCenter <= baseRadius * 0.18f -> 100 // Bullseye
                        distToCenter <= baseRadius * 0.38f -> 80  // Ring 4 Gold
                        distToCenter <= baseRadius * 0.58f -> 60  // Ring 3 Red
                        distToCenter <= baseRadius * 0.78f -> 40  // Ring 2 Blue
                        distToCenter <= baseRadius * 1.00f -> 20  // Ring 1 Outer
                        else -> 0                                 // Miss
                    }

                    lastHitMessage = when (earnedScore) {
                        100 -> "🎯 " + t("سهم في المنتصف! (١٠٠ نقطة)", "Bulls-eye! (100 pts)")
                        80 -> "🌟 " + t("إصابة ذهبية! (٨٠ نقطة)", "Gold Ring! (80 pts)")
                        60 -> "✨ " + t("إصابة ممتازة! (٦٠ نقطة)", "Great Hit! (60 pts)")
                        40 -> "👍 " + t("إصابة جيدة! (٤٠ نقطة)", "Good Hit! (40 pts)")
                        20 -> "🎯 " + t("إصابة طرفية! (٢٠ نقطة)", "Outer Ring! (20 pts)")
                        else -> "❌ " + t("خارج الهدف!", "Missed target!")
                    }

                    if (earnedScore > 0) {
                        particles.spawnBurst(finalImpactPos, count = 20)
                        triggerGameHaptic(context, GameHapticType.SUCCESS)
                        SfxManager.playSuccess(context)
                        stuckArrows.add(StuckArrow(offset = finalImpactPos - currentTargetCenter, scoreEarned = earnedScore))
                    } else {
                        triggerGameHaptic(context, GameHapticType.ERROR)
                        SfxManager.playWrong(context)
                    }

                    engineState.addScore(earnedScore)
                    arrowsRemaining--

                    // Check win/lose conditions
                    if (engineState.score >= engineState.targetScore) {
                        engineState.completeLevel(context, 3)
                    } else if (arrowsRemaining <= 0) {
                        if (engineState.score >= engineState.targetScore * 0.7) {
                            engineState.completeLevel(context, 2)
                        } else {
                            engineState.failLevel(context)
                        }
                    } else {
                        // Prepare next arrow
                        generateNewWind()
                        arrowState = ArrowState.READY
                        sightOffset = Offset.Zero
                    }
                }
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
                title = t("رماية السهام", "Desert Archery Range"),
                level = engineState.level,
                score = engineState.score,
                lives = engineState.lives,
                timeRemainingMs = -1L,
                onBack = onBack,
                onPause = { engineState.pauseGame() }
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = t(
                    "اسحب القوس للخلف واضبط الهدف مع مراعاة اتجاه وسرعة الرياح!",
                    "Pull back bowstring and compensate for wind direction & speed to hit the bullseye!"
                ),
                color = theme.textSecondary,
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp)
                    .noorGlassCard(cornerRadius = 24.dp)
                    .border(2.dp, theme.accent.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                    .gameGestureDetector(
                        onDragStart = { offset ->
                            if (arrowState == ArrowState.READY && arrowsRemaining > 0) {
                                arrowState = ArrowState.AIMING
                                dragStartPos = offset
                                currentDragPos = offset
                                triggerGameHaptic(context, GameHapticType.LIGHT)
                            }
                        },
                        onDragMove = { pos, _ ->
                            if (arrowState == ArrowState.AIMING) {
                                currentDragPos = pos
                                val start = dragStartPos ?: pos
                                // Map drag movement to sight movement on target
                                val dx = (pos.x - start.x) * 0.85f
                                val dy = (pos.y - start.y) * 0.85f
                                sightOffset = Offset(dx, dy)
                            }
                        },
                        onDragEnd = {
                            if (arrowState == ArrowState.AIMING) {
                                arrowState = ArrowState.FLYING
                                flightProgress = 0f
                                dragStartPos = null
                                currentDragPos = null
                                triggerGameHaptic(context, GameHapticType.MEDIUM)
                                SfxManager.playTap(context)
                            }
                        }
                    )
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height

                    val baseTargetCenter = Offset(canvasWidth / 2f, canvasHeight * 0.36f)
                    val oscillationX = if (engineState.level >= 2) sin(targetOscillationTime * 2f) * 45f else 0f
                    val targetCenter = baseTargetCenter + Offset(oscillationX, 0f)

                    val targetRadius = max(60f, 110f - engineState.level * 10f)

                    // Desert Stand & Target Easel Legs
                    drawLine(
                        color = Color(0xFF5D4037),
                        start = targetCenter,
                        end = targetCenter + Offset(-50f, targetRadius + 90f),
                        strokeWidth = 8f
                    )
                    drawLine(
                        color = Color(0xFF5D4037),
                        start = targetCenter,
                        end = targetCenter + Offset(50f, targetRadius + 90f),
                        strokeWidth = 8f
                    )
                    drawLine(
                        color = Color(0xFF4E342E),
                        start = targetCenter,
                        end = targetCenter + Offset(0f, targetRadius + 110f),
                        strokeWidth = 10f
                    )

                    // Concentric Target Rings
                    // Ring 1 (Outer White)
                    drawCircle(color = Color.White, radius = targetRadius, center = targetCenter)
                    drawCircle(color = Color.Black, radius = targetRadius, center = targetCenter, style = Stroke(width = 2f))

                    // Ring 2 (Blue)
                    drawCircle(color = Color(0xFF1E88E5), radius = targetRadius * 0.78f, center = targetCenter)

                    // Ring 3 (Red)
                    drawCircle(color = Color(0xFFE53935), radius = targetRadius * 0.58f, center = targetCenter)

                    // Ring 4 (Gold / Yellow)
                    drawCircle(color = Color(0xFFFFD54F), radius = targetRadius * 0.38f, center = targetCenter)

                    // Ring 5 (Bullseye Inner Center)
                    drawCircle(color = Color(0xFFD81B60), radius = targetRadius * 0.18f, center = targetCenter)

                    // Draw Previously Stuck Arrows
                    stuckArrows.forEach { arrow ->
                        val arrowPos = targetCenter + arrow.offset
                        drawCircle(
                            color = Color(0xFF3E2723),
                            radius = 4f,
                            center = arrowPos
                        )
                        drawLine(
                            color = Color(0xFFFFB300),
                            start = arrowPos,
                            end = arrowPos + Offset(-12f, -18f),
                            strokeWidth = 3f
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 3f,
                            center = arrowPos + Offset(-12f, -18f)
                        )
                    }

                    // Draw Wind Gauge Indicator Box in Top Left
                    val windBoxTopLeft = Offset(16f, 16f)
                    drawRoundRect(
                        color = Color.Black.copy(alpha = 0.5f),
                        topLeft = windBoxTopLeft,
                        size = Size(140f, 50f),
                        cornerRadius = CornerRadius(12f, 12f)
                    )

                    // Wind Direction Arrow
                    val windAngleRad = atan2(currentWind.vy, currentWind.vx)
                    val arrowCenter = windBoxTopLeft + Offset(30f, 25f)
                    val arrowLength = 16f
                    val arrowEnd = arrowCenter + Offset(cos(windAngleRad) * arrowLength, sin(windAngleRad) * arrowLength)

                    drawLine(
                        color = Color(0xFFFFD700),
                        start = arrowCenter,
                        end = arrowEnd,
                        strokeWidth = 4f
                    )
                    drawCircle(
                        color = Color(0xFFFFD700),
                        radius = 4f,
                        center = arrowEnd
                    )

                    val windText = "${currentWind.speedMps.toString().toArabicDigits()} م/ث"
                    val windLayout = textMeasurer.measure(
                        text = windText,
                        style = TextStyle(
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    drawText(
                        textLayoutResult = windLayout,
                        topLeft = Offset(windBoxTopLeft.x + 55f, windBoxTopLeft.y + 14f)
                    )

                    // Remaining Arrows Counter in Top Right
                    val arrowsText = t(
                        "السهام: ${arrowsRemaining.toArabicDigits()}",
                        "Arrows: $arrowsRemaining"
                    )
                    val arrowsLayout = textMeasurer.measure(
                        text = arrowsText,
                        style = TextStyle(
                            color = theme.accent,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    drawText(
                        textLayoutResult = arrowsLayout,
                        topLeft = Offset(canvasWidth - arrowsLayout.size.width - 24f, 24f)
                    )

                    // Bow & Aiming Mechanics
                    val bowPos = Offset(canvasWidth / 2f, canvasHeight * 0.82f)

                    if (arrowState == ArrowState.AIMING) {
                        // Draw Aim Crosshair over Target
                        val aimCenter = targetCenter + sightOffset
                        drawCircle(
                            color = Color.Red.copy(alpha = 0.6f),
                            radius = 18f,
                            center = aimCenter,
                            style = Stroke(width = 2f)
                        )
                        drawLine(
                            color = Color.Red,
                            start = aimCenter + Offset(-24f, 0f),
                            end = aimCenter + Offset(24f, 0f),
                            strokeWidth = 2f
                        )
                        drawLine(
                            color = Color.Red,
                            start = aimCenter + Offset(0f, -24f),
                            end = aimCenter + Offset(0f, 24f),
                            strokeWidth = 2f
                        )

                        // Draw Wind Drift Prediction Dot
                        val predictedHit = aimCenter + Offset(currentWind.vx, currentWind.vy)
                        drawCircle(
                            color = Color(0xFFFFD700).copy(alpha = 0.8f),
                            radius = 5f,
                            center = predictedHit
                        )
                    }

                    // Draw Bow Arc at Bottom
                    val bowPath = Path().apply {
                        moveTo(bowPos.x - 80f, bowPos.y - 20f)
                        quadraticBezierTo(bowPos.x, bowPos.y + 40f, bowPos.x + 80f, bowPos.y - 20f)
                    }
                    drawPath(
                        path = bowPath,
                        color = Color(0xFF8D6E63),
                        style = Stroke(width = 8f)
                    )

                    // Draw String Tension
                    val stringPullOffset = if (arrowState == ArrowState.AIMING) Offset(0f, 35f) else Offset.Zero
                    drawLine(
                        color = Color.White,
                        start = Offset(bowPos.x - 80f, bowPos.y - 20f),
                        end = bowPos + stringPullOffset,
                        strokeWidth = 2.5f
                    )
                    drawLine(
                        color = Color.White,
                        start = Offset(bowPos.x + 80f, bowPos.y - 20f),
                        end = bowPos + stringPullOffset,
                        strokeWidth = 2.5f
                    )

                    // Draw Flying Arrow Animation
                    if (arrowState == ArrowState.FLYING) {
                        val finalImpactPos = targetCenter + sightOffset + Offset(currentWind.vx, currentWind.vy)
                        val currentArrowPos = bowPos + (finalImpactPos - bowPos) * flightProgress
                        val scale = 1.0f - (flightProgress * 0.65f)

                        drawCircle(
                            color = Color(0xFFFFD700),
                            radius = 12f * scale,
                            center = currentArrowPos
                        )
                        drawLine(
                            color = Color(0xFF3E2723),
                            start = currentArrowPos,
                            end = currentArrowPos + Offset(0f, 40f * scale),
                            strokeWidth = 4f * scale
                        )
                    }

                    // Draw Hit Feedback Message Overlay
                    if (lastHitMessage.isNotEmpty()) {
                        val msgLayout = textMeasurer.measure(
                            text = lastHitMessage,
                            style = TextStyle(
                                color = theme.textPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        drawText(
                            textLayoutResult = msgLayout,
                            topLeft = Offset(
                                canvasWidth / 2f - msgLayout.size.width / 2f,
                                canvasHeight * 0.66f
                            )
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
