package com.elhajri.noor.games

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import kotlin.random.Random

/**
 * Game 38: ZigZag Caravan Trail (طريق الجبل)
 * Category: متعة
 * Mechanic: One-tap direction switcher runner along a narrow isometric zigzag mountain wall.
 */
private data class ZigzagTile(
    val gx: Int,
    val gy: Int,
    var hasRuby: Boolean = false,
    var rubyCollected: Boolean = false
)

@Composable
fun ZigzagCaravanGame(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val theme = NoorThemeState.active
    val engineState = rememberGameEngineState(
        initialLevel = 1,
        initialLives = 3,
        targetScore = 120
    )

    // Player position in continuous grid space
    var camelGx by remember { mutableFloatStateOf(0f) }
    var camelGy by remember { mutableFloatStateOf(0f) }
    var moveDirection by remember { mutableIntStateOf(0) } // 0 = +X axis, 1 = +Y axis
    var moveSpeed by remember { mutableFloatStateOf(2.5f) } // grid units / sec

    var isFalling by remember { mutableStateOf(false) }
    var fallAnimProgress by remember { mutableFloatStateOf(0f) }

    val pathTiles = remember { mutableStateListOf<ZigzagTile>() }
    val particles = remember { ParticleSystem() }

    fun generatePathAhead(minCount: Int = 50) {
        var lastGx = if (pathTiles.isNotEmpty()) pathTiles.last().gx else 0
        var lastGy = if (pathTiles.isNotEmpty()) pathTiles.last().gy else 0

        if (pathTiles.isEmpty()) {
            pathTiles.add(ZigzagTile(0, 0, hasRuby = false))
        }

        var currentDir = Random.nextInt(2)
        while (pathTiles.size < minCount) {
            val segmentLength = Random.nextInt(2, 5)
            for (step in 0 until segmentLength) {
                if (currentDir == 0) lastGx++ else lastGy++
                val spawnRuby = Random.nextFloat() < 0.35f && pathTiles.size > 3
                pathTiles.add(ZigzagTile(lastGx, lastGy, hasRuby = spawnRuby))
            }
            currentDir = 1 - currentDir // alternate turn
        }
    }

    fun initGame() {
        pathTiles.clear()
        camelGx = 0f
        camelGy = 0f
        moveDirection = 0
        moveSpeed = 2.5f
        isFalling = false
        fallAnimProgress = 0f
        generatePathAhead(60)
        engineState.startGame()
    }

    LaunchedEffect(Unit) {
        initGame()
    }

    fun handleTapTurn() {
        if (engineState.playState != GamePlayState.PLAYING || isFalling) return

        moveDirection = 1 - moveDirection
        triggerGameHaptic(context, GameHapticType.LIGHT)
        SfxManager.playTap(context)
    }

    fun isPositionOnPath(gx: Float, gy: Float): Boolean {
        for (tile in pathTiles) {
            val dx = abs(gx - tile.gx)
            val dy = abs(gy - tile.gy)
            if (dx < 0.65f && dy < 0.65f) {
                return true
            }
        }
        return false
    }

    CanvasGameLoop(engineState = engineState) { deltaSec ->
        particles.update(deltaSec)

        if (engineState.playState == GamePlayState.PLAYING) {
            if (isFalling) {
                fallAnimProgress += deltaSec * 3f
                if (fallAnimProgress >= 1f) {
                    triggerGameHaptic(context, GameHapticType.ERROR)
                    SfxManager.playWrong(context)
                    if (engineState.loseLife()) {
                        engineState.failLevel(context)
                    } else {
                        initGame()
                    }
                }
            } else {
                // Advance camel continuous position
                if (moveDirection == 0) {
                    camelGx += moveSpeed * deltaSec
                } else {
                    camelGy += moveSpeed * deltaSec
                }

                // Check ruby collections
                pathTiles.forEach { tile ->
                    if (tile.hasRuby && !tile.rubyCollected) {
                        val dx = camelGx - tile.gx
                        val dy = camelGy - tile.gy
                        if (dx * dx + dy * dy < 0.3f) {
                            tile.rubyCollected = true
                            engineState.addScore(10)
                            triggerGameHaptic(context, GameHapticType.SUCCESS)
                            SfxManager.playSuccess(context)
                            moveSpeed += 0.05f

                            if (engineState.score >= engineState.targetScore) {
                                engineState.completeLevel(context, 3)
                            }
                        }
                    }
                }

                // Check if fallen off path cliff
                if (!isPositionOnPath(camelGx, camelGy)) {
                    isFalling = true
                    fallAnimProgress = 0f
                }

                // Keep path generated ahead
                if (pathTiles.size - (camelGx + camelGy).toInt() < 25) {
                    generatePathAhead(pathTiles.size + 30)
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
                title = t("طريق الجبل", "ZigZag Caravan Trail"),
                level = engineState.level,
                score = engineState.score,
                lives = engineState.lives,
                onBack = onBack,
                onPause = { engineState.pauseGame() }
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = t(
                    "انقر في أي مكان بالشاشة لتغيير اتجاه القافلة والالتفاف على الجبل!",
                    "Tap anywhere to switch camel direction and stay on the mountain wall!"
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
                            onTap = {
                                handleTapTurn()
                            }
                        )
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height

                    val tileWidth = 60f
                    val tileHeight = 35f

                    // Camera tracking center offset
                    val centerX = canvasWidth / 2f
                    val centerY = canvasHeight / 2f + 50f

                    fun toScreen(gx: Float, gy: Float): Offset {
                        val relGx = gx - camelGx
                        val relGy = gy - camelGy
                        val sx = centerX + (relGx - relGy) * (tileWidth * 0.866f)
                        val sy = centerY + (relGx + relGy) * (tileHeight * 0.6f)
                        return Offset(sx, sy)
                    }

                    // Render Path Wall Tiles
                    pathTiles.forEach { tile ->
                        val pos = toScreen(tile.gx.toFloat(), tile.gy.toFloat())

                        // Render only visible tiles on screen
                        if (pos.x in -100f..(canvasWidth + 100f) && pos.y in -100f..(canvasHeight + 100f)) {
                            val topFace = Path().apply {
                                moveTo(pos.x, pos.y - tileHeight * 0.5f)
                                lineTo(pos.x + tileWidth * 0.866f, pos.y)
                                lineTo(pos.x, pos.y + tileHeight * 0.5f)
                                lineTo(pos.x - tileWidth * 0.866f, pos.y)
                                close()
                            }

                            val sideFaceLeft = Path().apply {
                                moveTo(pos.x - tileWidth * 0.866f, pos.y)
                                lineTo(pos.x, pos.y + tileHeight * 0.5f)
                                lineTo(pos.x, pos.y + tileHeight * 0.5f + 20f)
                                lineTo(pos.x - tileWidth * 0.866f, pos.y + 20f)
                                close()
                            }

                            val sideFaceRight = Path().apply {
                                moveTo(pos.x, pos.y + tileHeight * 0.5f)
                                lineTo(pos.x + tileWidth * 0.866f, pos.y)
                                lineTo(pos.x + tileWidth * 0.866f, pos.y + 20f)
                                lineTo(pos.x, pos.y + tileHeight * 0.5f + 20f)
                                close()
                            }

                            // Wall 3D Block Faces
                            drawPath(sideFaceLeft, Color(0xFF3E2723))
                            drawPath(sideFaceRight, Color(0xFF271C19))
                            drawPath(topFace, Color(0xFFD7CCC8))
                            drawPath(topFace, theme.accent.copy(alpha = 0.3f), style = Stroke(width = 2f))

                            // Draw Collectible Ruby
                            if (tile.hasRuby && !tile.rubyCollected) {
                                val rubyPos = Offset(pos.x, pos.y - 15f)
                                drawCircle(
                                    color = Color(0xFFFFD700),
                                    radius = 10f,
                                    center = rubyPos
                                )
                                drawCircle(
                                    color = Color.White,
                                    radius = 4f,
                                    center = rubyPos - Offset(2f, 2f)
                                )
                            }
                        }
                    }

                    // Render Camel Player
                    val camelScreenPos = toScreen(camelGx, camelGy)
                    val fallYOffset = if (isFalling) fallAnimProgress * 150f else 0f
                    val camelScale = if (isFalling) max(0.2f, 1f - fallAnimProgress) else 1f
                    val drawCamelPos = Offset(camelScreenPos.x, camelScreenPos.y - 12f + fallYOffset)

                    drawCircle(
                        color = Color(0xFFFFD700),
                        radius = 16f * camelScale,
                        center = drawCamelPos
                    )
                    drawCircle(
                        color = Color(0xFF795548),
                        radius = 10f * camelScale,
                        center = drawCamelPos - Offset(0f, 12f * camelScale)
                    )

                    // Direction Indicator Arrow
                    val dirVector = if (moveDirection == 0) {
                        Offset(tileWidth * 0.5f, tileHeight * 0.3f)
                    } else {
                        Offset(-tileWidth * 0.5f, tileHeight * 0.3f)
                    }
                    drawLine(
                        color = theme.accent,
                        start = drawCamelPos,
                        end = drawCamelPos + dirVector * camelScale,
                        strokeWidth = 4f * camelScale
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
