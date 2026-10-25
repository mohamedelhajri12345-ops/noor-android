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
import kotlin.math.abs

/**
 * Game 4: Minaret Stacker (برج المئذنة)
 * Category: مهارة
 * Mechanic: Timing tap to drop moving blocks and stack a minaret tower.
 */
data class MinaretBlock(
    var x: Float,
    val y: Float,
    var width: Float,
    val height: Float = 40f,
    val color: Color
)

@Composable
fun MinaretStackerGame(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val theme = NoorThemeState.active
    val engineState = rememberGameEngineState(initialLevel = 1, initialLives = 3, targetScore = 10)

    val stackedBlocks = remember { mutableStateListOf<MinaretBlock>() }
    var currentX by remember { mutableFloatStateOf(50f) }
    var currentWidth by remember { mutableFloatStateOf(200f) }
    var speed by remember { mutableFloatStateOf(300f) }
    var direction by remember { mutableIntStateOf(1) }
    val particles = remember { ParticleSystem() }

    fun initGame() {
        stackedBlocks.clear()
        // Base block at bottom
        stackedBlocks.add(
            MinaretBlock(
                x = 100f,
                y = 600f,
                width = 240f,
                height = 50f,
                color = theme.accent
            )
        )
        currentWidth = 240f
        currentX = 50f
        speed = 300f
        direction = 1
        engineState.startGame()
    }

    LaunchedEffect(Unit) {
        initGame()
    }

    fun handleTap() {
        if (engineState.playState != GamePlayState.PLAYING) return

        val prevBlock = stackedBlocks.last()
        val currX = currentX
        val prevX = prevBlock.x

        val diff = currX - prevX
        val overlap = currentWidth - abs(diff)

        if (overlap <= 0f) {
            // Completely missed!
            triggerGameHaptic(context, GameHapticType.ERROR)
            SfxManager.playWrong(context)
            if (engineState.loseLife()) {
                engineState.failLevel(context)
            }
        } else {
            // Stack successfully!
            triggerGameHaptic(context, GameHapticType.SUCCESS)
            SfxManager.playSuccess(context)
            engineState.addScore(1)

            val newX = if (diff > 0) currX else prevX
            val newY = prevBlock.y - 45f

            stackedBlocks.add(
                MinaretBlock(
                    x = newX,
                    y = newY,
                    width = overlap,
                    height = 40f,
                    color = if (stackedBlocks.size % 2 == 0) theme.accent else Color(0xFF00E676)
                )
            )

            // Spawn particle burst at junction
            particles.spawnBurst(Offset(newX + overlap / 2f, newY), count = 15)

            currentWidth = overlap
            speed += 20f

            if (engineState.score >= engineState.targetScore) {
                engineState.completeLevel(context, 3)
            }
        }
    }

    CanvasGameLoop(engineState = engineState) { deltaSec ->
        particles.update(deltaSec)

        if (engineState.playState == GamePlayState.PLAYING) {
            currentX += direction * speed * deltaSec
            if (currentX <= 10f) {
                currentX = 10f
                direction = 1
            } else if (currentX + currentWidth >= 480f) {
                currentX = 480f - currentWidth
                direction = -1
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
                title = t("برج المئذنة", "Minaret Stacker"),
                level = engineState.level,
                score = engineState.score,
                lives = engineState.lives,
                timeRemainingMs = -1L,
                onBack = onBack,
                onPause = { engineState.pauseGame() }
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = t("انقر بالشاشة لأسقاط الطبقة وبناء المئذنة بدقة!", "Tap to drop the minaret layer and build the tower!"),
                color = theme.textSecondary,
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp)
                    .noorGlassCard(cornerRadius = 24.dp)
                    .border(2.dp, theme.accent.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                    .gameGestureDetector(
                        onTap = { handleTap() }
                    )
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Draw stacked blocks
                    stackedBlocks.forEach { block ->
                        drawRoundRect(
                            color = block.color,
                            topLeft = Offset(block.x, block.y),
                            size = Size(block.width, block.height),
                            cornerRadius = CornerRadius(8f)
                        )
                    }

                    // Draw active moving block
                    if (engineState.playState == GamePlayState.PLAYING && stackedBlocks.isNotEmpty()) {
                        val activeY = stackedBlocks.last().y - 45f
                        drawRoundRect(
                            color = Color.White,
                            topLeft = Offset(currentX, activeY),
                            size = Size(currentWidth, 40f),
                            cornerRadius = CornerRadius(8f)
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
