package com.elhajri.noor.games

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.audio.SfxManager
import com.elhajri.noor.games.engine.*
import com.elhajri.noor.settings.t
import com.elhajri.noor.theme.NoorThemeState
import com.elhajri.noor.ui.noorGlassCard
import kotlin.math.*

/**
 * Game 34: Minaret Steel Pinball (مغناطيس المنار)
 * Category: متعة
 * Mechanic: Pinball arcade game with spring plunger launcher, twin minaret flippers, and dome bumpers.
 */
private data class PinballBumper(
    val id: Int,
    val center: Offset,
    val radius: Float,
    val scoreValue: Int,
    val color: Color,
    var flashTimer: Float = 0f
)

@Composable
fun PinballMinaretGame(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val theme = NoorThemeState.active
    val engineState = rememberGameEngineState(
        initialLevel = 1,
        initialLives = 3,
        targetScore = 300
    )

    // Ball physics state
    var ballPos by remember { mutableStateOf(Offset(380f, 520f)) }
    var ballVel by remember { mutableStateOf(Offset(0f, 0f)) }
    val ballRadius = 11f

    // Plunger state
    var plungerPull by remember { mutableFloatStateOf(0f) } // 0f to 80f
    var isDraggingPlunger by remember { mutableStateOf(false) }
    var ballInPlungerLane by remember { mutableStateOf(true) }

    // Flipper states
    var leftFlipperActive by remember { mutableStateOf(false) }
    var rightFlipperActive by remember { mutableStateOf(false) }
    var leftFlipperAngle by remember { mutableFloatStateOf(25f) } // degrees
    var rightFlipperAngle by remember { mutableFloatStateOf(155f) } // degrees

    val particles = remember { ParticleSystem() }

    // Table bumpers definition
    val bumpers = remember {
        mutableStateListOf(
            PinballBumper(1, Offset(180f, 150f), 28f, 50, Color(0xFFFFD700)), // Main Dome
            PinballBumper(2, Offset(100f, 230f), 22f, 30, Color(0xFF00E676)), // Left Pillar
            PinballBumper(3, Offset(260f, 230f), 22f, 30, Color(0xFF00E676)), // Right Pillar
            PinballBumper(4, Offset(180f, 300f), 20f, 40, Color(0xFF80D8FF)), // Crescent Star
            PinballBumper(5, Offset(80f, 130f), 18f, 25, Color(0xFFFF80AB)), // Upper Left
            PinballBumper(6, Offset(280f, 130f), 18f, 25, Color(0xFFFF80AB))  // Upper Right
        )
    }

    fun initGame() {
        ballPos = Offset(380f, 520f)
        ballVel = Offset(0f, 0f)
        plungerPull = 0f
        isDraggingPlunger = false
        ballInPlungerLane = true
        leftFlipperActive = false
        rightFlipperActive = false
        leftFlipperAngle = 25f
        rightFlipperAngle = 155f
        engineState.startGame()
    }

    LaunchedEffect(Unit) {
        initGame()
    }

    fun launchBall() {
        if (!ballInPlungerLane) return
        val launchSpeed = 500f + plungerPull * 10f
        ballVel = Offset(0f, -launchSpeed)
        ballInPlungerLane = false
        plungerPull = 0f
        triggerGameHaptic(context, GameHapticType.MEDIUM)
        SfxManager.playTap(context)
    }

    fun resetBallToPlunger() {
        ballPos = Offset(380f, 520f)
        ballVel = Offset(0f, 0f)
        ballInPlungerLane = true
    }

    CanvasGameLoop(engineState = engineState) { deltaSec ->
        particles.update(deltaSec)

        // Update flipper angles towards target
        val targetLeftAngle = if (leftFlipperActive) -25f else 25f
        val targetRightAngle = if (rightFlipperActive) 205f else 155f
        val flipperSpeed = 800f * deltaSec // deg/sec

        val isLeftSwingingUp = leftFlipperActive && leftFlipperAngle > targetLeftAngle
        val isRightSwingingUp = rightFlipperActive && rightFlipperAngle < targetRightAngle

        leftFlipperAngle = if (leftFlipperAngle < targetLeftAngle) {
            min(targetLeftAngle, leftFlipperAngle + flipperSpeed)
        } else {
            max(targetLeftAngle, leftFlipperAngle - flipperSpeed)
        }

        rightFlipperAngle = if (rightFlipperAngle < targetRightAngle) {
            min(targetRightAngle, rightFlipperAngle + flipperSpeed)
        } else {
            max(targetRightAngle, rightFlipperAngle - flipperSpeed)
        }

        // Update bumper flash timers
        bumpers.forEach { b ->
            if (b.flashTimer > 0f) {
                b.flashTimer = max(0f, b.flashTimer - deltaSec * 3f)
            }
        }

        if (engineState.playState == GamePlayState.PLAYING && !ballInPlungerLane) {
            // Physics integration (Gravity + Velocity)
            val gravity = 650f * deltaSec
            var vx = ballVel.x
            var vy = ballVel.y + gravity

            // Damping / Friction
            vx *= (1f - 0.1f * deltaSec)
            vy *= (1f - 0.1f * deltaSec)

            var newX = ballPos.x + vx * deltaSec
            var newY = ballPos.y + vy * deltaSec

            // Plunger lane curved ceiling entry (x > 350, y < 100)
            if (newX > 350f && newY < 80f) {
                newX = 345f
                vx = -abs(vx) - 80f
                vy = abs(vy) + 50f
            }

            // Wall Collisions
            // Left Wall
            if (newX < 15f + ballRadius) {
                newX = 15f + ballRadius
                vx = abs(vx) * 0.85f
            }
            // Right Wall (Main board wall at 350f, plunger lane wall at 395f)
            val rightLimit = if (newY > 100f && ballInPlungerLane) 395f else if (newY > 100f) 350f else 395f
            if (newX > rightLimit - ballRadius) {
                newX = rightLimit - ballRadius
                vx = -abs(vx) * 0.85f
            }
            // Top Arch Ceiling
            if (newY < 15f + ballRadius) {
                newY = 15f + ballRadius
                vy = abs(vy) * 0.85f
            }

            // Side Slanted Guides
            // Left Slanted Guide: line from (15, 400) to (110, 500)
            if (newX < 110f && newY > 400f && newY < 510f) {
                val lineSlope = (500f - 400f) / (110f - 15f)
                val expectedY = 400f + (newX - 15f) * lineSlope
                if (newY > expectedY - ballRadius) {
                    newY = expectedY - ballRadius
                    vx = abs(vx) + 100f
                    vy = -abs(vy) * 0.7f
                }
            }
            // Right Slanted Guide: line from (350, 400) to (250, 500)
            if (newX > 250f && newX < 350f && newY > 400f && newY < 510f) {
                val lineSlope = (500f - 400f) / (250f - 350f)
                val expectedY = 400f + (newX - 350f) * lineSlope
                if (newY > expectedY - ballRadius) {
                    newY = expectedY - ballRadius
                    vx = -abs(vx) - 100f
                    vy = -abs(vy) * 0.7f
                }
            }

            // Bumper Collisions
            bumpers.forEach { bumper ->
                val dx = newX - bumper.center.x
                val dy = newY - bumper.center.y
                val distSq = dx * dx + dy * dy
                val minDist = bumper.radius + ballRadius
                if (distSq < minDist * minDist && distSq > 0.001f) {
                    val dist = sqrt(distSq)
                    val nx = dx / dist
                    val ny = dy / dist

                    // Reposition outside bumper
                    newX = bumper.center.x + nx * (minDist + 1f)
                    newY = bumper.center.y + ny * (minDist + 1f)

                    // Bounce velocity reflection + impulse boost
                    val dot = vx * nx + vy * ny
                    if (dot < 0) {
                        val boost = 350f
                        vx = (vx - 2 * dot * nx) + nx * boost
                        vy = (vy - 2 * dot * ny) + ny * boost
                    }

                    bumper.flashTimer = 1f
                    particles.spawnBurst(bumper.center, count = 20)
                    triggerGameHaptic(context, GameHapticType.SUCCESS)
                    SfxManager.playSuccess(context)
                    engineState.addScore(bumper.scoreValue)

                    if (engineState.score >= engineState.targetScore) {
                        engineState.completeLevel(context, 3)
                    }
                }
            }

            // Flipper Collisions
            // Left Flipper
            val leftPivot = Offset(110f, 500f)
            val leftRad = leftFlipperAngle * PI.toFloat() / 180f
            val leftTip = Offset(leftPivot.x + 75f * cos(leftRad), leftPivot.y + 75f * sin(leftRad))
            val distToLeft = distToSegment(Offset(newX, newY), leftPivot, leftTip)

            if (distToLeft < ballRadius + 6f) {
                val nx = -sin(leftRad)
                val ny = cos(leftRad)
                val boost = if (isLeftSwingingUp) 600f else 150f
                vy = -abs(vy) - boost
                vx = vx + nx * boost * 0.5f
                newY = leftPivot.y - 15f
                triggerGameHaptic(context, GameHapticType.LIGHT)
                SfxManager.playTap(context)
            }

            // Right Flipper
            val rightPivot = Offset(250f, 500f)
            val rightRad = rightFlipperAngle * PI.toFloat() / 180f
            val rightTip = Offset(rightPivot.x + 75f * cos(rightRad), rightPivot.y + 75f * sin(rightRad))
            val distToRight = distToSegment(Offset(newX, newY), rightPivot, rightTip)

            if (distToRight < ballRadius + 6f) {
                val nx = -sin(rightRad)
                val ny = cos(rightRad)
                val boost = if (isRightSwingingUp) 600f else 150f
                vy = -abs(vy) - boost
                vx = vx + nx * boost * 0.5f
                newY = rightPivot.y - 15f
                triggerGameHaptic(context, GameHapticType.LIGHT)
                SfxManager.playTap(context)
            }

            // Drain Check (Ball dropped below flippers)
            if (newY > 580f) {
                triggerGameHaptic(context, GameHapticType.ERROR)
                SfxManager.playWrong(context)
                if (engineState.loseLife()) {
                    engineState.failLevel(context)
                } else {
                    resetBallToPlunger()
                }
            } else {
                ballPos = Offset(newX, newY)
                ballVel = Offset(vx, vy)
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
                title = t("مغناطيس المنار", "Minaret Steel Pinball"),
                level = engineState.level,
                score = engineState.score,
                lives = engineState.lives,
                onBack = onBack,
                onPause = { engineState.pauseGame() }
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = t(
                    "اسحب الضاغط لإطلاق الكرة، وانقر يميناً ويساراً لتحريك المضربين!",
                    "Pull plunger to launch steel ball, tap left/right screen for flippers!"
                ),
                color = theme.textSecondary,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(12.dp)
                    .noorGlassCard(cornerRadius = 24.dp)
                    .border(2.dp, theme.accent.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = { offset ->
                                if (offset.x < size.width * 0.5f) {
                                    leftFlipperActive = true
                                    tryAwaitRelease()
                                    leftFlipperActive = false
                                } else if (offset.x < size.width * 0.88f) {
                                    rightFlipperActive = true
                                    tryAwaitRelease()
                                    rightFlipperActive = false
                                }
                            }
                        )
                    }
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                if (ballInPlungerLane && offset.x > size.width * 0.85f) {
                                    isDraggingPlunger = true
                                }
                            },
                            onDrag = { change, dragAmount ->
                                if (isDraggingPlunger) {
                                    change.consume()
                                    plungerPull = (plungerPull + dragAmount.y).coerceIn(0f, 80f)
                                }
                            },
                            onDragEnd = {
                                if (isDraggingPlunger) {
                                    isDraggingPlunger = false
                                    launchBall()
                                }
                            }
                        )
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val boardWidth = size.width
                    val boardHeight = size.height
                    val scaleX = boardWidth / 400f
                    val scaleY = boardHeight / 600f

                    // Draw Arch & Minaret Pinball Board Outline
                    val archPath = Path().apply {
                        moveTo(15f * scaleX, 100f * scaleY)
                        cubicTo(
                            15f * scaleX, 10f * scaleY,
                            350f * scaleX, 10f * scaleY,
                            350f * scaleX, 100f * scaleY
                        )
                        lineTo(350f * scaleX, 580f * scaleY)
                        lineTo(15f * scaleX, 580f * scaleY)
                        close()
                    }
                    drawPath(
                        path = archPath,
                        color = Color(0xFF131024).copy(alpha = 0.6f)
                    )
                    drawPath(
                        path = archPath,
                        color = theme.accent.copy(alpha = 0.4f),
                        style = Stroke(width = 3f * scaleX)
                    )

                    // Plunger Lane Separator Line
                    drawLine(
                        color = Color.White.copy(alpha = 0.3f),
                        start = Offset(350f * scaleX, 80f * scaleY),
                        end = Offset(350f * scaleX, 580f * scaleY),
                        strokeWidth = 3f * scaleX
                    )

                    // Slanted Side Guides
                    drawLine(
                        color = theme.accent,
                        start = Offset(15f * scaleX, 400f * scaleY),
                        end = Offset(110f * scaleX, 500f * scaleY),
                        strokeWidth = 4f * scaleX
                    )
                    drawLine(
                        color = theme.accent,
                        start = Offset(350f * scaleX, 400f * scaleY),
                        end = Offset(250f * scaleX, 500f * scaleY),
                        strokeWidth = 4f * scaleX
                    )

                    // Draw Bumpers
                    bumpers.forEach { bumper ->
                        val center = Offset(bumper.center.x * scaleX, bumper.center.y * scaleY)
                        val radius = bumper.radius * scaleX
                        val color = if (bumper.flashTimer > 0f) Color.White else bumper.color

                        drawCircle(
                            color = color,
                            radius = radius,
                            center = center
                        )
                        drawCircle(
                            color = Color.White.copy(alpha = 0.6f),
                            radius = radius * 0.7f,
                            center = center,
                            style = Stroke(width = 3f * scaleX)
                        )
                    }

                    // Draw Flippers
                    // Left Flipper
                    val leftPivot = Offset(110f * scaleX, 500f * scaleY)
                    val leftRad = leftFlipperAngle * PI.toFloat() / 180f
                    val leftTip = Offset(
                        leftPivot.x + 75f * scaleX * cos(leftRad),
                        leftPivot.y + 75f * scaleY * sin(leftRad)
                    )
                    drawLine(
                        color = Color(0xFFFFD700),
                        start = leftPivot,
                        end = leftTip,
                        strokeWidth = 12f * scaleX
                    )
                    drawCircle(color = theme.accent, radius = 8f * scaleX, center = leftPivot)

                    // Right Flipper
                    val rightPivot = Offset(250f * scaleX, 500f * scaleY)
                    val rightRad = rightFlipperAngle * PI.toFloat() / 180f
                    val rightTip = Offset(
                        rightPivot.x + 75f * scaleX * cos(rightRad),
                        rightPivot.y + 75f * scaleY * sin(rightRad)
                    )
                    drawLine(
                        color = Color(0xFFFFD700),
                        start = rightPivot,
                        end = rightTip,
                        strokeWidth = 12f * scaleX
                    )
                    drawCircle(color = theme.accent, radius = 8f * scaleX, center = rightPivot)

                    // Plunger Spring
                    val plungerY = (520f + plungerPull) * scaleY
                    drawLine(
                        color = Color.Gray,
                        start = Offset(375f * scaleX, 580f * scaleY),
                        end = Offset(375f * scaleX, plungerY),
                        strokeWidth = 8f * scaleX
                    )
                    drawRoundRect(
                        color = theme.accent,
                        topLeft = Offset(365f * scaleX, plungerY - 10f * scaleY),
                        size = Size(20f * scaleX, 15f * scaleY),
                        cornerRadius = CornerRadius(4f * scaleX)
                    )

                    // Steel Ball
                    val drawBallPos = Offset(ballPos.x * scaleX, ballPos.y * scaleY)
                    drawCircle(
                        color = Color(0xFFE0E0E0),
                        radius = ballRadius * scaleX,
                        center = drawBallPos
                    )
                    drawCircle(
                        color = Color.White,
                        radius = ballRadius * 0.4f * scaleX,
                        center = drawBallPos - Offset(3f * scaleX, 3f * scaleY)
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

private fun distToSegment(pt: Offset, p1: Offset, p2: Offset): Float {
    val dx = p2.x - p1.x
    val dy = p2.y - p1.y
    val lenSq = dx * dx + dy * dy
    if (lenSq == 0f) return (pt - p1).getDistance()
    val t = ((pt.x - p1.x) * dx + (pt.y - p1.y) * dy) / lenSq
    val clampedT = t.coerceIn(0f, 1f)
    val proj = Offset(p1.x + clampedT * dx, p1.y + clampedT * dy)
    return (pt - proj).getDistance()
}
