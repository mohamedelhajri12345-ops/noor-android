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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.audio.SfxManager
import com.elhajri.noor.games.engine.*
import com.elhajri.noor.settings.t
import com.elhajri.noor.theme.NoorThemeState
import com.elhajri.noor.ui.noorGlassCard
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Game 15: Dome Brick Breaker (كاسر الأشكال / كاسر القباب)
 * Category: مهارة
 * Mechanic: Breakout arcade game destroying geometric Islamic ceiling tiles & domes.
 * Drag a bottom paddle left/right to bounce a crescent orb.
 */

private enum class DomeBrickType {
    NORMAL, HARD_STONE, POWER_UP
}

private data class DomeBrick(
    val id: Int,
    val box: AABBBox,
    val type: DomeBrickType,
    val color: Color,
    var hitsLeft: Int
)

private data class FallingPowerUp(
    val id: Int,
    var center: Offset,
    val type: String, // "WIDEN" or "SCORE"
    val radius: Float = 14f,
    var isCollected: Boolean = false
)

@Composable
fun DomeBreakerGame(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val theme = NoorThemeState.active
    val engineState = rememberGameEngineState(initialLevel = 1, initialLives = 3, targetScore = 150)

    // Canvas coordinate reference (normalized inside 440 x 600 area)
    val canvasWidth = 440f
    val canvasHeight = 600f

    // Paddle parameters
    var paddleWidth by remember { mutableFloatStateOf(110f) }
    val paddleHeight = 18f
    val paddleY = 530f
    var paddleX by remember { mutableFloatStateOf(canvasWidth / 2f) }

    // Orb parameters
    val orbRadius = 12f
    var orbPos by remember { mutableStateOf(Offset(canvasWidth / 2f, paddleY - 20f)) }
    var orbVel by remember { mutableStateOf(Vector22D(0f, 0f)) }
    var isOrbLaunched by remember { mutableStateOf(false) }

    val bricks = remember { mutableStateListOf<DomeBrick>() }
    val powerUps = remember { mutableStateListOf<FallingPowerUp>() }
    val particles = remember { ParticleSystem() }
    var powerUpTimerSec by remember { mutableFloatStateOf(0f) }

    fun buildBrickGrid() {
        bricks.clear()
        powerUps.clear()
        val rows = 5
        val cols = 6
        val brickWidth = 56f
        val brickHeight = 28f
        val startX = (canvasWidth - (cols * (brickWidth + 6f) - 6f)) / 2f
        val startY = 60f

        var brickId = 1
        val colors = listOf(
            theme.accent,
            Color(0xFF00E676),
            Color(0xFF80D8FF),
            Color(0xFFFFB300),
            Color(0xFFB388FF)
        )

        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val left = startX + c * (brickWidth + 6f)
                val top = startY + r * (brickHeight + 6f)
                val box = AABBBox(left, top, left + brickWidth, top + brickHeight)

                val type = when {
                    r == 1 && (c == 2 || c == 3) -> DomeBrickType.HARD_STONE
                    (r == 0 && c == 0) || (r == 2 && c == 5) || (r == 3 && c == 1) -> DomeBrickType.POWER_UP
                    else -> DomeBrickType.NORMAL
                }

                val color = when (type) {
                    DomeBrickType.HARD_STONE -> Color(0xFF8D6E63)
                    DomeBrickType.POWER_UP -> Color(0xFFFFD700)
                    DomeBrickType.NORMAL -> colors[r % colors.size]
                }

                val hits = if (type == DomeBrickType.HARD_STONE) 2 else 1
                bricks.add(DomeBrick(brickId++, box, type, color, hits))
            }
        }
    }

    fun launchOrb() {
        if (!isOrbLaunched && engineState.playState == GamePlayState.PLAYING) {
            isOrbLaunched = true
            val speed = 360f
            orbVel = Vector22D(speed * 0.45f, -speed * 0.89f)
            triggerGameHaptic(context, GameHapticType.MEDIUM)
            SfxManager.playTap(context)
        }
    }

    fun initGame() {
        paddleWidth = 110f
        paddleX = canvasWidth / 2f
        orbPos = Offset(canvasWidth / 2f, paddleY - 20f)
        orbVel = Vector22D(0f, 0f)
        isOrbLaunched = false
        powerUpTimerSec = 0f
        buildBrickGrid()
        engineState.startGame()
    }

    LaunchedEffect(Unit) {
        initGame()
    }

    CanvasGameLoop(engineState = engineState) { deltaSec ->
        particles.update(deltaSec)

        if (engineState.playState != GamePlayState.PLAYING) return@CanvasGameLoop

        // Power-up duration countdown
        if (powerUpTimerSec > 0f) {
            powerUpTimerSec -= deltaSec
            if (powerUpTimerSec <= 0f) {
                paddleWidth = 110f
            }
        }

        if (!isOrbLaunched) {
            // Keep orb sitting on paddle center
            orbPos = Offset(paddleX, paddleY - orbRadius - 2f)
            return@CanvasGameLoop
        }

        // Move Orb
        var nextX = orbPos.x + orbVel.x * deltaSec
        var nextY = orbPos.y + orbVel.y * deltaSec

        // Wall collisions (Left, Right, Top)
        if (nextX - orbRadius <= 0f) {
            nextX = orbRadius
            orbVel.x = -orbVel.x
            SfxManager.playTap(context)
        } else if (nextX + orbRadius >= canvasWidth) {
            nextX = canvasWidth - orbRadius
            orbVel.x = -orbVel.x
            SfxManager.playTap(context)
        }

        if (nextY - orbRadius <= 20f) {
            nextY = 20f + orbRadius
            orbVel.y = -orbVel.y
            SfxManager.playTap(context)
        }

        // Orb falls below bottom screen -> lose life
        if (nextY > canvasHeight + 20f) {
            isOrbLaunched = false
            orbPos = Offset(paddleX, paddleY - orbRadius - 2f)
            triggerGameHaptic(context, GameHapticType.ERROR)
            SfxManager.playWrong(context)
            if (engineState.loseLife()) {
                engineState.failLevel(context)
            }
            return@CanvasGameLoop
        }

        val orbBody = CircleBody(center = Offset(nextX, nextY), radius = orbRadius)

        // Paddle collision
        val paddleBox = AABBBox(
            left = paddleX - paddleWidth / 2f,
            top = paddleY,
            right = paddleX + paddleWidth / 2f,
            bottom = paddleY + paddleHeight
        )

        if (orbVel.y > 0f && orbBody.intersectsBox(paddleBox)) {
            nextY = paddleY - orbRadius
            val hitRatio = ((nextX - (paddleX - paddleWidth / 2f)) / paddleWidth).coerceIn(0.1f, 0.9f)
            // Reflection angle: 210 deg (left) to 330 deg (right)
            val angleRad = Math.toRadians((210.0 + hitRatio * 120.0)).toFloat()
            val speed = orbVel.length().coerceIn(340f, 480f)
            orbVel = Vector22D(speed * cos(angleRad.toDouble()).toFloat(), speed * sin(angleRad.toDouble()).toFloat())
            triggerGameHaptic(context, GameHapticType.LIGHT)
            SfxManager.playTap(context)
        }

        // Brick collisions
        val brickIterator = bricks.iterator()
        while (brickIterator.hasNext()) {
            val brick = brickIterator.next()
            if (orbBody.intersectsBox(brick.box)) {
                brick.hitsLeft--
                particles.spawnBurst(
                    origin = brick.box.center,
                    count = 18,
                    colors = listOf(brick.color, Color.White, Color(0xFFFFD700))
                )

                if (brick.hitsLeft <= 0) {
                    engineState.addScore(10)
                    triggerGameHaptic(context, GameHapticType.SUCCESS)
                    SfxManager.playSuccess(context)

                    if (brick.type == DomeBrickType.POWER_UP) {
                        powerUps.add(
                            FallingPowerUp(
                                id = Random.nextInt(),
                                center = brick.box.center,
                                type = if (Random.nextBoolean()) "WIDEN" else "SCORE"
                            )
                        )
                    }
                    brickIterator.remove()
                } else {
                    engineState.addScore(5)
                    triggerGameHaptic(context, GameHapticType.LIGHT)
                }

                // Bounce orb back
                orbVel.y = -orbVel.y
                break
            }
        }

        // Update orb position
        orbPos = Offset(nextX, nextY)

        // Move & collect falling power-ups
        val powerIterator = powerUps.iterator()
        while (powerIterator.hasNext()) {
            val power = powerIterator.next()
            power.center = Offset(power.center.x, power.center.y + 160f * deltaSec)

            val powerBody = CircleBody(center = power.center, radius = power.radius)
            if (!power.isCollected && powerBody.intersectsBox(paddleBox)) {
                power.isCollected = true
                triggerGameHaptic(context, GameHapticType.MEDIUM)
                SfxManager.playCelebration(context)

                if (power.type == "WIDEN") {
                    paddleWidth = 160f
                    powerUpTimerSec = 10f
                } else {
                    engineState.addScore(30)
                }
                powerIterator.remove()
            } else if (power.center.y > canvasHeight) {
                powerIterator.remove()
            }
        }

        // Check level win
        if (bricks.isEmpty() || engineState.score >= engineState.targetScore) {
            engineState.completeLevel(context, 3)
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
                title = t("كاسر الأشكال", "Dome Brick Breaker"),
                level = engineState.level,
                score = engineState.score,
                lives = engineState.lives,
                timeRemainingMs = -1L,
                onBack = onBack,
                onPause = { engineState.pauseGame() }
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = t("حرّك المضطرب يميناً ويساراً لارتداد الكرة وكسر القباب الفسيفسائية!", "Drag paddle left/right to bounce crescent orb and destroy geometric tiles!"),
                color = theme.textSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .noorGlassCard(cornerRadius = 24.dp)
                    .border(2.dp, theme.accent.copy(alpha = 0.35f), RoundedCornerShape(24.dp))
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                if (!isOrbLaunched) launchOrb()
                                paddleX = offset.x.coerceIn(paddleWidth / 2f, size.width - paddleWidth / 2f)
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                paddleX = change.position.x.coerceIn(paddleWidth / 2f, size.width - paddleWidth / 2f)
                            }
                        )
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val scaleX = size.width / canvasWidth
                    val scaleY = size.height / canvasHeight

                    // Render Bricks
                    bricks.forEach { brick ->
                        val bLeft = brick.box.left * scaleX
                        val bTop = brick.box.top * scaleY
                        val bW = brick.box.width * scaleX
                        val bH = brick.box.height * scaleY

                        // Rounded Brick Tile
                        drawRoundRect(
                            color = brick.color,
                            topLeft = Offset(bLeft, bTop),
                            size = Size(bW, bH),
                            cornerRadius = CornerRadius(8f, 8f)
                        )
                        drawRoundRect(
                            color = Color.White.copy(alpha = 0.35f),
                            topLeft = Offset(bLeft, bTop),
                            size = Size(bW, bH),
                            cornerRadius = CornerRadius(8f, 8f),
                            style = Stroke(width = 2.5f)
                        )

                        // If hard stone brick with 1 hit remaining, draw crack line
                        if (brick.type == DomeBrickType.HARD_STONE && brick.hitsLeft == 1) {
                            drawLine(
                                color = Color.White,
                                start = Offset(bLeft + 6f, bTop + 6f),
                                end = Offset(bLeft + bW - 6f, bTop + bH - 6f),
                                strokeWidth = 3f
                            )
                        }
                    }

                    // Render Falling Power-Ups
                    powerUps.forEach { power ->
                        val px = power.center.x * scaleX
                        val py = power.center.y * scaleY
                        val pColor = if (power.type == "WIDEN") Color(0xFF00E676) else Color(0xFFFFD700)
                        drawCircle(color = pColor, radius = power.radius * scaleX, center = Offset(px, py))
                        drawCircle(
                            color = Color.White,
                            radius = power.radius * scaleX,
                            center = Offset(px, py),
                            style = Stroke(width = 2f)
                        )
                    }

                    // Render Paddle
                    val pLeft = (paddleX - paddleWidth / 2f) * scaleX
                    val pTop = paddleY * scaleY
                    val pW = paddleWidth * scaleX
                    val pH = paddleHeight * scaleY

                    drawRoundRect(
                        color = theme.accent,
                        topLeft = Offset(pLeft, pTop),
                        size = Size(pW, pH),
                        cornerRadius = CornerRadius(10f, 10f)
                    )
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.7f),
                        topLeft = Offset(pLeft, pTop),
                        size = Size(pW, pH),
                        cornerRadius = CornerRadius(10f, 10f),
                        style = Stroke(width = 2f)
                    )

                    // Render Crescent Orb
                    val orbX = orbPos.x * scaleX
                    val orbY = orbPos.y * scaleY
                    val r = orbRadius * scaleX

                    drawCircle(color = Color(0xFFFFD700), radius = r, center = Offset(orbX, orbY))
                    drawCircle(
                        color = Color.White,
                        radius = r,
                        center = Offset(orbX, orbY),
                        style = Stroke(width = 2f)
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
