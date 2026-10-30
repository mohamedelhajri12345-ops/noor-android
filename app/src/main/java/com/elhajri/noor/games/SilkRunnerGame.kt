package com.elhajri.noor.games

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.audio.SfxManager
import com.elhajri.noor.games.engine.*
import com.elhajri.noor.settings.t
import com.elhajri.noor.theme.NoorThemeState
import com.elhajri.noor.ui.noorGlassCard
import kotlin.random.Random

/**
 * Game 14: Silk Road Runner (عداء طريق الحرير)
 * Category: مهارة
 * Mechanic: Endless 2D side-scrolling runner through desert dunes.
 * Swipe UP to jump over cacti/rocks, swipe DOWN to slide under archways. Collect gold dirhams.
 */

private enum class RunnerObstacleType {
    CACTUS, ROCK, ARCHWAY
}

private data class RunnerObstacle(
    val id: Int,
    var x: Float,
    val type: RunnerObstacleType,
    val width: Float,
    val height: Float,
    var isHit: Boolean = false
)

private data class DirhamCoin(
    val id: Int,
    var x: Float,
    val y: Float,
    val radius: Float = 14f,
    var isCollected: Boolean = false
)

@Composable
fun SilkRunnerGame(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val theme = NoorThemeState.active
    val engineState = rememberGameEngineState(initialLevel = 1, initialLives = 3, targetScore = 120)

    // Physics parameters
    val groundY = 380f
    val runnerX = 90f
    var runnerY by remember { mutableFloatStateOf(groundY) }
    var runnerVy by remember { mutableFloatStateOf(0f) }
    var isJumping by remember { mutableStateOf(false) }
    var isSliding by remember { mutableStateOf(false) }
    var slideTimerSec by remember { mutableFloatStateOf(0f) }
    var invulnerableTimerSec by remember { mutableFloatStateOf(0f) }

    // World parameters
    var scrollSpeed by remember { mutableFloatStateOf(280f) }
    var bgOffset by remember { mutableFloatStateOf(0f) }
    var distanceTraveled by remember { mutableFloatStateOf(0f) }
    var nextSpawnX by remember { mutableFloatStateOf(500f) }

    val obstacles = remember { mutableStateListOf<RunnerObstacle>() }
    val coins = remember { mutableStateListOf<DirhamCoin>() }
    val particles = remember { ParticleSystem() }
    var obstacleIdCounter by remember { mutableIntStateOf(1) }
    var coinIdCounter by remember { mutableIntStateOf(1) }

    fun triggerJump() {
        if (!isJumping && !isSliding && engineState.playState == GamePlayState.PLAYING) {
            isJumping = true
            runnerVy = -520f
            triggerGameHaptic(context, GameHapticType.LIGHT)
            SfxManager.playTap(context)
        }
    }

    fun triggerSlide() {
        if (!isJumping && !isSliding && engineState.playState == GamePlayState.PLAYING) {
            isSliding = true
            slideTimerSec = 0.65f
            triggerGameHaptic(context, GameHapticType.LIGHT)
            SfxManager.playTap(context)
        }
    }

    fun spawnNextPattern() {
        val spawnX = 650f + Random.nextFloat() * 150f
        val randVal = Random.nextFloat()
        when {
            randVal < 0.40f -> {
                // Ground obstacle (Cactus or Rock)
                val isCactus = Random.nextBoolean()
                val type = if (isCactus) RunnerObstacleType.CACTUS else RunnerObstacleType.ROCK
                val w = if (isCactus) 36f else 48f
                val h = if (isCactus) 60f else 40f
                obstacles.add(RunnerObstacle(obstacleIdCounter++, spawnX, type, w, h))
            }
            randVal < 0.70f -> {
                // High obstacle (Archway)
                obstacles.add(RunnerObstacle(obstacleIdCounter++, spawnX, RunnerObstacleType.ARCHWAY, 55f, 130f))
            }
            else -> {
                // Double pattern or obstacle with coins
                obstacles.add(RunnerObstacle(obstacleIdCounter++, spawnX, RunnerObstacleType.CACTUS, 36f, 55f))
            }
        }

        // Spawn dirham coins along the path
        val coinY = if (randVal < 0.4f) groundY - 110f else groundY - 40f
        for (i in 0 until 3) {
            coins.add(DirhamCoin(coinIdCounter++, spawnX + i * 45f + 60f, coinY))
        }
    }

    fun initGame() {
        runnerY = groundY
        runnerVy = 0f
        isJumping = false
        isSliding = false
        slideTimerSec = 0f
        invulnerableTimerSec = 0f
        scrollSpeed = 280f
        bgOffset = 0f
        distanceTraveled = 0f
        nextSpawnX = 500f
        obstacles.clear()
        coins.clear()
        obstacleIdCounter = 1
        coinIdCounter = 1
        spawnNextPattern()
        engineState.startGame()
    }

    LaunchedEffect(Unit) {
        initGame()
    }

    CanvasGameLoop(engineState = engineState) { deltaSec ->
        particles.update(deltaSec)

        if (engineState.playState != GamePlayState.PLAYING) return@CanvasGameLoop

        // Increase difficulty/speed gradually
        scrollSpeed = (280f + distanceTraveled * 0.05f).coerceAtMost(520f)
        distanceTraveled += scrollSpeed * deltaSec
        bgOffset = (bgOffset + scrollSpeed * 0.3f * deltaSec) % 800f

        // Distance score
        if ((distanceTraveled.toInt() % 100) < 10) {
            engineState.addScore(1)
        }

        if (engineState.score >= engineState.targetScore) {
            engineState.completeLevel(context, 3)
            return@CanvasGameLoop
        }

        // Runner jump physics
        if (isJumping) {
            val gravity = 1350f
            runnerY += runnerVy * deltaSec
            runnerVy += gravity * deltaSec
            if (runnerY >= groundY) {
                runnerY = groundY
                runnerVy = 0f
                isJumping = false
            }
        }

        // Runner slide timer
        if (isSliding) {
            slideTimerSec -= deltaSec
            if (slideTimerSec <= 0f) {
                isSliding = false
                slideTimerSec = 0f
            }
        }

        // Invulnerability timer
        if (invulnerableTimerSec > 0f) {
            invulnerableTimerSec -= deltaSec
        }

        // Runner collision bounds
        val currentRunnerHeight = if (isSliding) 28f else 62f
        val currentRunnerWidth = if (isSliding) 55f else 38f
        val runnerBox = AABBBox(
            left = runnerX - currentRunnerWidth / 2f,
            top = runnerY - currentRunnerHeight,
            right = runnerX + currentRunnerWidth / 2f,
            bottom = runnerY
        )

        // Move & update obstacles
        var maxObsX = 0f
        val obsIterator = obstacles.iterator()
        while (obsIterator.hasNext()) {
            val obs = obsIterator.next()
            obs.x -= scrollSpeed * deltaSec
            if (obs.x > maxObsX) maxObsX = obs.x

            // Collision check
            if (!obs.isHit && invulnerableTimerSec <= 0f) {
                val obsBox = when (obs.type) {
                    RunnerObstacleType.CACTUS, RunnerObstacleType.ROCK -> AABBBox(
                        left = obs.x - obs.width / 2f,
                        top = groundY - obs.height,
                        right = obs.x + obs.width / 2f,
                        bottom = groundY
                    )
                    RunnerObstacleType.ARCHWAY -> AABBBox(
                        left = obs.x - obs.width / 2f,
                        top = 0f,
                        right = obs.x + obs.width / 2f,
                        bottom = groundY - 45f // archway opening height requires sliding
                    )
                }

                if (runnerBox.intersects(obsBox)) {
                    obs.isHit = true
                    invulnerableTimerSec = 1.2f
                    triggerGameHaptic(context, GameHapticType.ERROR)
                    SfxManager.playWrong(context)
                    particles.spawnBurst(
                        origin = runnerBox.center,
                        count = 25,
                        colors = listOf(Color.Red, Color(0xFFFF9800), Color.White)
                    )
                    if (engineState.loseLife()) {
                        engineState.failLevel(context)
                    }
                }
            }

            if (obs.x < -100f) {
                obsIterator.remove()
            }
        }

        // Move & update coins
        val coinIterator = coins.iterator()
        while (coinIterator.hasNext()) {
            val coin = coinIterator.next()
            coin.x -= scrollSpeed * deltaSec

            if (!coin.isCollected) {
                val coinBody = CircleBody(center = Offset(coin.x, coin.y), radius = coin.radius)
                if (coinBody.intersectsBox(runnerBox)) {
                    coin.isCollected = true
                    engineState.addScore(5)
                    triggerGameHaptic(context, GameHapticType.LIGHT)
                    SfxManager.playSuccess(context)
                    particles.spawnBurst(
                        origin = Offset(coin.x, coin.y),
                        count = 12,
                        colors = listOf(Color(0xFFFFD700), Color.White)
                    )
                }
            }

            if (coin.x < -50f) {
                coinIterator.remove()
            }
        }

        // Spawn new patterns continuously
        if (maxObsX < 450f) {
            spawnNextPattern()
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
                title = t("عداء طريق الحرير", "Silk Road Runner"),
                level = engineState.level,
                score = engineState.score,
                lives = engineState.lives,
                timeRemainingMs = -1L,
                onBack = onBack,
                onPause = { engineState.pauseGame() }
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = t("اسحب للأعلى ⬆️ للقفز فوق الصخور، وللأسفل ⬇️ للانزلاق تحت الأقواس!", "Swipe UP to jump cacti/rocks, swipe DOWN to slide under archways!"),
                color = theme.textSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(Modifier.height(10.dp))

            // Main Canvas Game Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .noorGlassCard(cornerRadius = 24.dp)
                    .border(2.dp, theme.accent.copy(alpha = 0.35f), RoundedCornerShape(24.dp))
                    .gameGestureDetector(
                        onSwipe = { direction ->
                            when (direction) {
                                SwipeDirection.UP -> triggerJump()
                                SwipeDirection.DOWN -> triggerSlide()
                                else -> {}
                            }
                        }
                    )
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val scaleY = height / 500f

                    // Sky / Desert Dunes Parallax Background
                    val dunePath = Path().apply {
                        moveTo(0f, 260f * scaleY)
                        cubicTo(
                            width * 0.25f - (bgOffset % width), 220f * scaleY,
                            width * 0.5f - (bgOffset % width), 280f * scaleY,
                            width, 250f * scaleY
                        )
                        lineTo(width, height)
                        lineTo(0f, height)
                        close()
                    }
                    drawPath(path = dunePath, color = Color(0xFFE0A96D).copy(alpha = 0.35f))

                    // Ground Line
                    drawRect(
                        color = Color(0xFFC28245),
                        topLeft = Offset(0f, groundY * scaleY),
                        size = Size(width, height - groundY * scaleY)
                    )
                    drawLine(
                        color = Color(0xFFFFD700),
                        start = Offset(0f, groundY * scaleY),
                        end = Offset(width, groundY * scaleY),
                        strokeWidth = 4f
                    )

                    // Draw Obstacles
                    obstacles.forEach { obs ->
                        val obsX = obs.x
                        when (obs.type) {
                            RunnerObstacleType.CACTUS -> {
                                val topY = (groundY - obs.height) * scaleY
                                drawRoundRect(
                                    color = Color(0xFF2E7D32),
                                    topLeft = Offset(obsX - obs.width / 2f, topY),
                                    size = Size(obs.width, obs.height * scaleY),
                                    cornerRadius = CornerRadius(8f, 8f)
                                )
                                // Side branches
                                drawRect(
                                    color = Color(0xFF1B5E20),
                                    topLeft = Offset(obsX - obs.width * 0.8f, topY + 12f),
                                    size = Size(obs.width * 0.5f, 8f)
                                )
                                drawRect(
                                    color = Color(0xFF1B5E20),
                                    topLeft = Offset(obsX + obs.width * 0.3f, topY + 24f),
                                    size = Size(obs.width * 0.5f, 8f)
                                )
                            }
                            RunnerObstacleType.ROCK -> {
                                val topY = (groundY - obs.height) * scaleY
                                drawRoundRect(
                                    color = Color(0xFF795548),
                                    topLeft = Offset(obsX - obs.width / 2f, topY),
                                    size = Size(obs.width, obs.height * scaleY),
                                    cornerRadius = CornerRadius(12f, 12f)
                                )
                            }
                            RunnerObstacleType.ARCHWAY -> {
                                // Stone archway obstacle (requires sliding)
                                val archHeight = (groundY - 45f) * scaleY
                                drawRect(
                                    color = Color(0xFFB17032),
                                    topLeft = Offset(obsX - obs.width / 2f, 0f),
                                    size = Size(obs.width, archHeight)
                                )
                                drawRect(
                                    color = Color(0xFFFFD700),
                                    topLeft = Offset(obsX - obs.width / 2f - 4f, archHeight - 8f),
                                    size = Size(obs.width + 8f, 10f)
                                )
                            }
                        }
                    }

                    // Draw Gold Dirham Coins
                    coins.forEach { coin ->
                        if (!coin.isCollected) {
                            drawCircle(
                                color = Color(0xFFFFD700),
                                radius = coin.radius,
                                center = Offset(coin.x, coin.y * scaleY)
                            )
                            drawCircle(
                                color = Color(0xFFFFA000),
                                radius = coin.radius * 0.65f,
                                center = Offset(coin.x, coin.y * scaleY),
                                style = Stroke(width = 3f)
                            )
                        }
                    }

                    // Draw Merchant Runner Avatar
                    val isVisible = invulnerableTimerSec <= 0f || ((invulnerableTimerSec * 15).toInt() % 2 == 0)
                    if (isVisible) {
                        val currentH = if (isSliding) 28f else 62f
                        val currentW = if (isSliding) 55f else 38f
                        val topY = (runnerY - currentH) * scaleY
                        val centerX = runnerX

                        // Robe / Body
                        drawRoundRect(
                            color = theme.accent,
                            topLeft = Offset(centerX - currentW / 2f, topY),
                            size = Size(currentW, currentH * scaleY),
                            cornerRadius = CornerRadius(10f, 10f)
                        )

                        // Head & Turban if standing/jumping
                        if (!isSliding) {
                            drawCircle(
                                color = Color(0xFFD7CCC8),
                                radius = 12f,
                                center = Offset(centerX, topY - 8f)
                            )
                            // Emerald Turban
                            drawCircle(
                                color = Color(0xFF00E676),
                                radius = 13f,
                                center = Offset(centerX, topY - 14f)
                            )
                        }
                    }

                    // Render particles
                    particles.draw(this)
                }
            }

            // Onscreen Touch Controls for Jump & Slide
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Slide Button
                Button(
                    onClick = { triggerSlide() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00897B)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                ) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color.White)
                    Spacer(Modifier.width(6.dp))
                    Text(t("انزلاق ⬇️", "Slide ⬇️"), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Spacer(Modifier.width(16.dp))

                // Jump Button
                Button(
                    onClick = { triggerJump() },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.accent),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                ) {
                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = null, tint = Color.Black)
                    Spacer(Modifier.width(6.dp))
                    Text(t("قفز ⬆️", "Jump ⬆️"), color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }

        // Overlay states
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
