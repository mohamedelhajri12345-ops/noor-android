package com.elhajri.noor.games

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.audio.SfxManager
import com.elhajri.noor.games.engine.*
import com.elhajri.noor.settings.t
import com.elhajri.noor.theme.NoorThemeState
import com.elhajri.noor.ui.noorGlassCard
import kotlin.math.*
import kotlin.random.Random

/**
 * Game 18: Peace Dove Flight (حمامة السلام)
 * ID: game_18_flying_dove
 * Category: مهارة
 * Mechanic: Flappy altitude control game. Tap the screen to give a peace dove an upward flap
 * impulse as it flies through gaps in olive branch obstacles.
 */
private data class OliveObstacle(
    val id: Int,
    var x: Float,
    var gapY: Float,
    val gapHeight: Float = 190f,
    val width: Float = 60f,
    var passed: Boolean = false,
    var starCollected: Boolean = false,
    var starOffsetY: Float = 0f
)

@Composable
fun FlyingDoveGame(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val theme = NoorThemeState.active

    val engineState = rememberGameEngineState(
        initialLevel = 1,
        initialLives = 3,
        targetScore = 100
    )

    var doveY by remember { mutableFloatStateOf(300f) }
    var doveVelocityY by remember { mutableFloatStateOf(0f) }
    var wingFlapPhase by remember { mutableFloatStateOf(0f) }
    var invincibilityTimer by remember { mutableFloatStateOf(0f) }

    val obstacles = remember { mutableStateListOf<OliveObstacle>() }
    val particles = remember { ParticleSystem() }

    val gravity = 1100f
    val flapImpulse = -380f

    fun initGame() {
        doveY = 300f
        doveVelocityY = 0f
        wingFlapPhase = 0f
        invincibilityTimer = 0f

        obstacles.clear()
        val gapHeight = max(150f, 210f - engineState.level * 15f)
        val spacing = 260f

        for (i in 0..2) {
            val startX = 400f + i * spacing
            val randomGapY = 180f + Random.nextFloat() * 260f
            val starOffset = (Random.nextFloat() - 0.5f) * 60f
            obstacles.add(
                OliveObstacle(
                    id = i,
                    x = startX,
                    gapY = randomGapY,
                    gapHeight = gapHeight,
                    starOffsetY = starOffset
                )
            )
        }

        engineState.startGame()
    }

    LaunchedEffect(engineState.level) {
        initGame()
    }

    fun flap() {
        if (engineState.playState != GamePlayState.PLAYING) return

        doveVelocityY = flapImpulse
        wingFlapPhase = 1.0f // Trigger wing flap animation
        triggerGameHaptic(context, GameHapticType.LIGHT)
        SfxManager.playTap(context)

        // Spawn soft wind particles behind dove
        particles.spawnBurst(
            origin = Offset(100f, doveY + 10f),
            count = 6
        )
    }

    CanvasGameLoop(engineState = engineState) { deltaSec ->
        particles.update(deltaSec)

        if (engineState.playState == GamePlayState.PLAYING) {
            // Apply physics
            doveVelocityY += gravity * deltaSec
            doveVelocityY = doveVelocityY.coerceIn(-500f, 650f)
            doveY += doveVelocityY * deltaSec

            // Decay wing flap animation & invincibility
            if (wingFlapPhase > 0f) {
                wingFlapPhase = max(0f, wingFlapPhase - deltaSec * 5f)
            }
            if (invincibilityTimer > 0f) {
                invincibilityTimer = max(0f, invincibilityTimer - deltaSec)
            }

            val speed = 180f + (engineState.level * 25f)
            val doveX = 120f
            val doveRadius = 18f

            // Move and check obstacles
            var maxX = 0f
            obstacles.forEach { obs ->
                if (obs.x > maxX) maxX = obs.x
            }

            for (obs in obstacles) {
                obs.x -= speed * deltaSec

                // Score for passing obstacle gap
                if (!obs.passed && obs.x + obs.width < doveX) {
                    obs.passed = true
                    engineState.addScore(10)
                    SfxManager.playSuccess(context)

                    if (engineState.score >= engineState.targetScore) {
                        engineState.completeLevel(context, 3)
                    }
                }

                // Star pickup collision
                if (!obs.starCollected) {
                    val starCenter = Offset(obs.x + obs.width / 2f, obs.gapY + obs.starOffsetY)
                    val dx = doveX - starCenter.x
                    val dy = doveY - starCenter.y
                    if (dx * dx + dy * dy <= (doveRadius + 18f) * (doveRadius + 18f)) {
                        obs.starCollected = true
                        engineState.addScore(15)
                        particles.spawnBurst(starCenter, count = 18)
                        triggerGameHaptic(context, GameHapticType.SUCCESS)
                        SfxManager.playSuccess(context)

                        if (engineState.score >= engineState.targetScore) {
                            engineState.completeLevel(context, 3)
                        }
                    }
                }

                // Recycle offscreen obstacles
                if (obs.x + obs.width < -20f) {
                    obs.x = maxX + 260f
                    obs.gapY = 180f + Random.nextFloat() * 260f
                    obs.passed = false
                    obs.starCollected = false
                    obs.starOffsetY = (Random.nextFloat() - 0.5f) * 60f
                }

                // Collision detection with olive branch obstacles
                if (invincibilityTimer <= 0f) {
                    val doveBox = CircleBody(center = Offset(doveX, doveY), radius = doveRadius)

                    val topBranch = AABBBox(
                        left = obs.x,
                        top = -100f,
                        right = obs.x + obs.width,
                        bottom = obs.gapY - obs.gapHeight / 2f
                    )

                    val bottomBranch = AABBBox(
                        left = obs.x,
                        top = obs.gapY + obs.gapHeight / 2f,
                        right = obs.x + obs.width,
                        bottom = 1200f
                    )

                    val hitBoundary = doveY < 20f || doveY > 680f
                    val hitBranch = doveBox.intersectsBox(topBranch) || doveBox.intersectsBox(bottomBranch)

                    if (hitBoundary || hitBranch) {
                        invincibilityTimer = 1.8f
                        doveVelocityY = -200f
                        particles.spawnBurst(Offset(doveX, doveY), count = 25)
                        triggerGameHaptic(context, GameHapticType.ERROR)
                        SfxManager.playWrong(context)

                        if (engineState.loseLife()) {
                            engineState.failLevel(context)
                        }
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
                title = t("حمامة السلام", "Peace Dove Flight"),
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
                    "انقر للتحليق وتفادي أغصان الزيتون وجمع النجوم!",
                    "Tap to flap upwards and fly safely through gaps in olive branches!"
                ),
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
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        flap()
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    val doveX = 120f

                    // Sky Background Elements (Soft Minaret Silhouettes at bottom)
                    drawRoundRect(
                        color = theme.accent.copy(alpha = 0.05f),
                        size = Size(canvasWidth, canvasHeight),
                        cornerRadius = CornerRadius(24f, 24f)
                    )

                    val silhouettePath = Path().apply {
                        moveTo(0f, canvasHeight)
                        lineTo(canvasWidth * 0.15f, canvasHeight - 40f)
                        lineTo(canvasWidth * 0.20f, canvasHeight - 110f) // Minaret
                        lineTo(canvasWidth * 0.25f, canvasHeight - 40f)
                        lineTo(canvasWidth * 0.50f, canvasHeight - 70f) // Dome
                        lineTo(canvasWidth * 0.75f, canvasHeight - 40f)
                        lineTo(canvasWidth * 0.80f, canvasHeight - 120f) // Minaret 2
                        lineTo(canvasWidth * 0.85f, canvasHeight - 40f)
                        lineTo(canvasWidth, canvasHeight)
                        close()
                    }
                    drawPath(
                        path = silhouettePath,
                        color = theme.accent.copy(alpha = 0.08f)
                    )

                    // Draw Olive Branch Obstacles
                    obstacles.forEach { obs ->
                        val gapTop = obs.gapY - obs.gapHeight / 2f
                        val gapBottom = obs.gapY + obs.gapHeight / 2f

                        // Top Olive Branch Trunk
                        drawRoundRect(
                            color = Color(0xFF5D4037),
                            topLeft = Offset(obs.x, 0f),
                            size = Size(obs.width, gapTop),
                            cornerRadius = CornerRadius(8f, 8f)
                        )
                        // Top Leaf Tip Cluster
                        drawCircle(
                            color = Color(0xFF2E7D32),
                            radius = obs.width * 0.75f,
                            center = Offset(obs.x + obs.width / 2f, gapTop)
                        )
                        drawCircle(
                            color = Color(0xFF4CAF50),
                            radius = obs.width * 0.45f,
                            center = Offset(obs.x + obs.width / 2f, gapTop - 10f)
                        )

                        // Bottom Olive Branch Trunk
                        drawRoundRect(
                            color = Color(0xFF5D4037),
                            topLeft = Offset(obs.x, gapBottom),
                            size = Size(obs.width, canvasHeight - gapBottom),
                            cornerRadius = CornerRadius(8f, 8f)
                        )
                        // Bottom Leaf Tip Cluster
                        drawCircle(
                            color = Color(0xFF2E7D32),
                            radius = obs.width * 0.75f,
                            center = Offset(obs.x + obs.width / 2f, gapBottom)
                        )
                        drawCircle(
                            color = Color(0xFF4CAF50),
                            radius = obs.width * 0.45f,
                            center = Offset(obs.x + obs.width / 2f, gapBottom + 10f)
                        )

                        // Draw Collectible Star inside Gap
                        if (!obs.starCollected) {
                            val starCenter = Offset(obs.x + obs.width / 2f, obs.gapY + obs.starOffsetY)

                            drawCircle(
                                color = Color(0xFFFFD700).copy(alpha = 0.3f),
                                radius = 22f,
                                center = starCenter
                            )
                            drawCircle(
                                color = Color(0xFFFFD700),
                                radius = 14f,
                                center = starCenter
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 8f,
                                center = starCenter
                            )
                        }
                    }

                    // Draw Peace Dove
                    val doveAlpha = if (invincibilityTimer > 0f && (invincibilityTimer * 10).toInt() % 2 == 0) 0.4f else 1.0f

                    val pitchAngle = (doveVelocityY * 0.08f).coerceIn(-30f, 45f)

                    rotate(degrees = pitchAngle, pivot = Offset(doveX, doveY)) {
                        // Tail Feathers
                        val tailPath = Path().apply {
                            moveTo(doveX - 16f, doveY)
                            lineTo(doveX - 32f, doveY - 10f)
                            lineTo(doveX - 30f, doveY + 6f)
                            close()
                        }
                        drawPath(path = tailPath, color = Color.White.copy(alpha = doveAlpha))

                        // Body (White Dove)
                        drawOval(
                            color = Color.White.copy(alpha = doveAlpha),
                            topLeft = Offset(doveX - 22f, doveY - 16f),
                            size = Size(42f, 30f)
                        )

                        // Flapping Wing
                        val wingYOffset = -12f - (wingFlapPhase * 16f)
                        val wingPath = Path().apply {
                            moveTo(doveX - 6f, doveY - 4f)
                            lineTo(doveX - 14f, doveY + wingYOffset)
                            lineTo(doveX + 10f, doveY - 8f)
                            close()
                        }
                        drawPath(
                            path = wingPath,
                            color = Color(0xFFECEFF1).copy(alpha = doveAlpha)
                        )
                        drawPath(
                            path = wingPath,
                            color = theme.accent.copy(alpha = doveAlpha * 0.5f),
                            style = Stroke(width = 1.5f)
                        )

                        // Eye
                        drawCircle(
                            color = Color(0xFF263238).copy(alpha = doveAlpha),
                            radius = 3f,
                            center = Offset(doveX + 12f, doveY - 6f)
                        )

                        // Beak (Small Golden Orange Triangle)
                        val beakPath = Path().apply {
                            moveTo(doveX + 18f, doveY - 4f)
                            lineTo(doveX + 28f, doveY - 1f)
                            lineTo(doveX + 18f, doveY + 4f)
                            close()
                        }
                        drawPath(
                            path = beakPath,
                            color = Color(0xFFFF9800).copy(alpha = doveAlpha)
                        )

                        // Olive Twig in Beak
                        val twigPath = Path().apply {
                            moveTo(doveX + 24f, doveY)
                            lineTo(doveX + 34f, doveY + 8f)
                        }
                        drawPath(
                            path = twigPath,
                            color = Color(0xFF2E7D32).copy(alpha = doveAlpha),
                            style = Stroke(width = 2.5f)
                        )
                        drawCircle(
                            color = Color(0xFF4CAF50).copy(alpha = doveAlpha),
                            radius = 4f,
                            center = Offset(doveX + 34f, doveY + 8f)
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
