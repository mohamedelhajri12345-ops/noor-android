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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.audio.SfxManager
import com.elhajri.noor.games.engine.*
import com.elhajri.noor.settings.t
import com.elhajri.noor.theme.NoorThemeState
import com.elhajri.noor.ui.noorGlassCard
import kotlin.random.Random

data class GridPoint(val x: Int, val y: Int)

@Composable
fun CaravanSnakeGame(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val theme = NoorThemeState.active
    val engineState = rememberGameEngineState(initialLevel = 1, initialLives = 3, targetScore = 100)

    val gridCols = 10
    val gridRows = 10

    val snakeBody = remember { mutableStateListOf<GridPoint>() }
    var direction by remember { mutableStateOf(SwipeDirection.RIGHT) }
    var foodPos by remember { mutableStateOf(GridPoint(5, 5)) }
    var moveTimerSec by remember { mutableFloatStateOf(0f) }
    val moveIntervalSec = 0.22f

    val particles = remember { ParticleSystem() }

    fun spawnFood() {
        val emptyPoints = mutableListOf<GridPoint>()
        for (r in 0 until gridRows) {
            for (c in 0 until gridCols) {
                val pt = GridPoint(c, r)
                if (pt !in snakeBody) {
                    emptyPoints.add(pt)
                }
            }
        }
        if (emptyPoints.isNotEmpty()) {
            foodPos = emptyPoints.random()
        }
    }

    fun initGame() {
        snakeBody.clear()
        snakeBody.add(GridPoint(3, 5))
        snakeBody.add(GridPoint(2, 5))
        snakeBody.add(GridPoint(1, 5))
        direction = SwipeDirection.RIGHT
        moveTimerSec = 0f
        spawnFood()
        engineState.startGame()
    }

    LaunchedEffect(Unit) {
        initGame()
    }

    fun updateSnake() {
        if (engineState.playState != GamePlayState.PLAYING) return

        val head = snakeBody.first()
        val newHead = when (direction) {
            SwipeDirection.UP -> GridPoint(head.x, head.y - 1)
            SwipeDirection.DOWN -> GridPoint(head.x, head.y + 1)
            SwipeDirection.LEFT -> GridPoint(head.x - 1, head.y)
            SwipeDirection.RIGHT -> GridPoint(head.x + 1, head.y)
        }

        // Wall or Self Collision
        if (newHead.x !in 0 until gridCols || newHead.y !in 0 until gridRows || newHead in snakeBody) {
            triggerGameHaptic(context, GameHapticType.HEAVY)
            if (engineState.loseLife()) {
                engineState.failLevel(context)
            } else {
                initGame()
            }
            return
        }

        // Move head
        snakeBody.add(0, newHead)

        // Check Food
        if (newHead == foodPos) {
            SfxManager.playSuccess(context)
            triggerGameHaptic(context, GameHapticType.MEDIUM)
            engineState.addScore(10)

            val particlePos = Offset((newHead.x + 0.5f) * 40f, (newHead.y + 0.5f) * 40f)
            particles.spawnBurst(particlePos, count = 18)

            if (engineState.score >= engineState.targetScore) {
                engineState.completeLevel(context, 3)
            } else {
                spawnFood()
            }
        } else {
            // Remove tail
            snakeBody.removeAt(snakeBody.size - 1)
        }
    }

    CanvasGameLoop(engineState = engineState) { deltaSec ->
        particles.update(deltaSec)

        if (engineState.playState == GamePlayState.PLAYING) {
            moveTimerSec += deltaSec
            if (moveTimerSec >= moveIntervalSec) {
                moveTimerSec = 0f
                updateSnake()
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
                title = t("قافلة الخير", "Caravan Snake"),
                level = engineState.level,
                score = engineState.score,
                lives = engineState.lives,
                onBack = onBack,
                onPause = { engineState.pauseGame() }
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = t("مرر لتوجيه القافلة نحو مؤونة التمر والماء دون الاصطدام!", "Swipe to guide caravan toward food and water without crashing!"),
                color = theme.textSecondary,
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(Modifier.height(12.dp))

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp)
                    .noorGlassCard(cornerRadius = 24.dp)
                    .border(2.dp, theme.accent.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
            ) {
                val boardSize = minOf(constraints.maxWidth, constraints.maxHeight).toFloat()
                val cellSize = boardSize / gridCols.toFloat()

                Canvas(
                    modifier = Modifier
                        .size(boardSize.dp)
                        .gameGestureDetector(
                            onSwipe = { dir ->
                                // Prevent 180-degree reverse turns
                                when (dir) {
                                    SwipeDirection.UP -> if (direction != SwipeDirection.DOWN) direction = dir
                                    SwipeDirection.DOWN -> if (direction != SwipeDirection.UP) direction = dir
                                    SwipeDirection.LEFT -> if (direction != SwipeDirection.RIGHT) direction = dir
                                    SwipeDirection.RIGHT -> if (direction != SwipeDirection.LEFT) direction = dir
                                }
                            }
                        )
                ) {
                    // Desert grid cells background
                    for (r in 0 until gridRows) {
                        for (c in 0 until gridCols) {
                            drawRoundRect(
                                color = Color(0xFF1E1408).copy(alpha = 0.4f),
                                topLeft = Offset(c * cellSize + 2f, r * cellSize + 2f),
                                size = Size(cellSize - 4f, cellSize - 4f),
                                cornerRadius = CornerRadius(6f)
                            )
                        }
                    }

                    // Draw Food (Water / Date Sack)
                    drawCircle(
                        color = Color(0xFF80D8FF),
                        radius = cellSize * 0.35f,
                        center = Offset((foodPos.x + 0.5f) * cellSize, (foodPos.y + 0.5f) * cellSize)
                    )

                    // Draw Snake / Caravan
                    snakeBody.forEachIndexed { idx, pt ->
                        val isHead = idx == 0
                        val color = if (isHead) Color(0xFFFFD700) else theme.accent.copy(alpha = 0.8f)

                        drawRoundRect(
                            color = color,
                            topLeft = Offset(pt.x * cellSize + 4f, pt.y * cellSize + 4f),
                            size = Size(cellSize - 8f, cellSize - 8f),
                            cornerRadius = CornerRadius(if (isHead) 12f else 8f)
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
