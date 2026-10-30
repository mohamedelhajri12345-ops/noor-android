package com.elhajri.noor.games

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.audio.SfxManager
import com.elhajri.noor.games.engine.*
import com.elhajri.noor.settings.t
import com.elhajri.noor.settings.toArabicDigits
import com.elhajri.noor.theme.NoorThemeState
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.noorGlassCard
import com.elhajri.noor.ui.themeScreenBackground
import kotlin.math.hypot
import kotlin.random.Random

/**
 * Game 43: Good Seed Whack (بستان الخير)
 * Category: أطفال
 * Mechanic: Whack-a-mole reaction tap. Tap bright sprouting dates and flowers popping out of soil holes; avoid thorn weeds.
 */
private enum class PlantType(val nameAr: String, val points: Int, val isHarmful: Boolean) {
    DATE_SPROUT("تمرة", 10, false),
    FLOWER("زهرة", 15, false),
    GOLDEN_FLOWER("زهرة ذهبية", 30, false),
    THORN_WEED("شوك", -10, true)
}

private data class GardenHole(
    val id: Int,
    val row: Int,
    val col: Int,
    var plantType: PlantType? = null,
    var timerSec: Float = 0f,
    var maxDurationSec: Float = 2.0f,
    var isHit: Boolean = false
)

@Composable
fun WhackSeedsGame(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val theme = NoorThemeState.active
    val textMeasurer = rememberTextMeasurer()

    val engineState = rememberGameEngineState(
        initialLevel = 1,
        initialLives = 3,
        timeLimitMs = 45000L,
        targetScore = 180
    )

    val particles = remember { ParticleSystem() }

    // 3x3 Garden Holes
    val holes = remember {
        mutableStateListOf<GardenHole>().apply {
            var id = 0
            for (r in 0 until 3) {
                for (c in 0 until 3) {
                    add(GardenHole(id = id++, row = r, col = c))
                }
            }
        }
    }

    var spawnTimerSec by remember { mutableFloatStateOf(0f) }
    var statusText by remember { mutableStateOf("") }

    fun initGame() {
        engineState.startGame()
        statusText = t("اضغط الثمار والأزهار فور ظهورها! احذر الشوك", "Tap dates and flowers quickly! Avoid thorns")
        holes.forEach {
            it.plantType = null
            it.timerSec = 0f
            it.isHit = false
        }
        spawnTimerSec = 0f
    }

    LaunchedEffect(Unit) {
        initGame()
    }

    // Main Game Loop Logic
    CanvasGameLoop(engineState) { deltaSec ->
        particles.update(deltaSec)

        if (engineState.playState != GamePlayState.PLAYING) return@CanvasGameLoop

        // Spawn timer
        spawnTimerSec += deltaSec
        val spawnInterval = maxOf(0.4f, 1.2f - (engineState.level * 0.1f))

        if (spawnTimerSec >= spawnInterval) {
            spawnTimerSec = 0f
            val emptyHoles = holes.filter { it.plantType == null }
            if (emptyHoles.isNotEmpty()) {
                val targetHole = emptyHoles.random()
                val rand = Random.nextFloat()
                val type = when {
                    rand < 0.45f -> PlantType.DATE_SPROUT
                    rand < 0.70f -> PlantType.FLOWER
                    rand < 0.85f -> PlantType.GOLDEN_FLOWER
                    else -> PlantType.THORN_WEED
                }
                val duration = maxOf(0.9f, 2.2f - (engineState.level * 0.12f))

                targetHole.plantType = type
                targetHole.timerSec = duration
                targetHole.maxDurationSec = duration
                targetHole.isHit = false
            }
        }

        // Update active holes
        holes.forEach { hole ->
            if (hole.plantType != null && !hole.isHit) {
                hole.timerSec -= deltaSec
                if (hole.timerSec <= 0f) {
                    hole.plantType = null
                }
            }
        }

        // Check level win / target score
        if (engineState.score >= engineState.targetScore) {
            engineState.completeLevel(context, 3)
        }
    }

    fun handleHoleTap(hole: GardenHole, tapOffset: Offset) {
        if (engineState.playState != GamePlayState.PLAYING) return

        val plant = hole.plantType
        if (plant != null && !hole.isHit) {
            hole.isHit = true
            hole.plantType = null

            if (plant.isHarmful) {
                // Thorn weed tapped
                SfxManager.playWrong(context)
                triggerGameHaptic(context, GameHapticType.ERROR)
                engineState.loseLife()
                engineState.addScore(-10)
                statusText = t("آخ! احذر الشوك 🌵", "Ouch! Watch out for thorns 🌵")

                if (engineState.lives <= 0) {
                    engineState.loseLife()
                }
            } else {
                // Good plant tapped
                SfxManager.playSuccess(context)
                triggerGameHaptic(context, GameHapticType.SUCCESS)
                engineState.addScore(plant.points)

                val burstColors = when (plant) {
                    PlantType.GOLDEN_FLOWER -> listOf(Gold, GoldSoft, Color.White, Color(0xFFFFD700))
                    PlantType.FLOWER -> listOf(Color(0xFFE91E63), Color(0xFFF48FB1), Color.White)
                    PlantType.DATE_SPROUT -> listOf(Color(0xFF8D6E63), Color(0xFFFFB74D), Color(0xFF81C784))
                    else -> listOf(Gold, Color.White)
                }

                particles.spawnBurst(
                    origin = tapOffset,
                    count = if (plant == PlantType.GOLDEN_FLOWER) 25 else 12,
                    colors = burstColors
                )

                statusText = when (plant) {
                    PlantType.GOLDEN_FLOWER -> t("زهرة ذهبية فائقة! +٣٠ 🌟", "Golden Flower! +30 🌟")
                    PlantType.FLOWER -> t("أحسنت! زهرة البستان +١٥ 🌸", "Well done! Flower +15 🌸")
                    else -> t("ممتاز! تمرة طيبة +١٠ 🌴", "Great! Date +10 🌴")
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .themeScreenBackground()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            GameHeaderBar(
                title = t("بستان الخير", "Good Seed Whack"),
                level = engineState.level,
                score = engineState.score,
                lives = engineState.lives,
                timeRemainingMs = engineState.timeRemainingMs,
                onBack = onBack,
                onPause = { engineState.pauseGame() }
            )

            // Status message
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .noorGlassCard(cornerRadius = 12.dp),
                color = Color.Transparent
            ) {
                Text(
                    text = statusText,
                    color = Gold,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(8.dp)
                )
            }

            // Garden Grid Canvas Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp)
                    .noorGlassCard(cornerRadius = 24.dp)
            ) {
                var boxSize by remember { mutableStateOf(Size.Zero) }

                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .gameGestureDetector(
                            onTap = { tapOffset ->
                                if (boxSize.width > 0f && boxSize.height > 0f) {
                                    val cellW = boxSize.width / 3f
                                    val cellH = boxSize.height / 3f

                                    holes.forEach { h ->
                                        val holeCenterX = h.col * cellW + cellW / 2f
                                        val holeCenterY = h.row * cellH + cellH / 2f

                                        if (hypot(tapOffset.x - holeCenterX, tapOffset.y - holeCenterY) < cellW * 0.42f) {
                                            handleHoleTap(h, tapOffset)
                                        }
                                    }
                                }
                            }
                        )
                ) {
                    boxSize = size
                    val w = size.width
                    val h = size.height
                    val cellW = w / 3f
                    val cellH = h / 3f

                    // Soil Background Texture
                    drawRoundRect(
                        color = Color(0xFF3E2723).copy(alpha = 0.4f),
                        topLeft = Offset(0f, 0f),
                        size = size,
                        cornerRadius = CornerRadius(20f)
                    )

                    // Draw 9 Soil Holes & Plants
                    holes.forEach { hole ->
                        val cx = hole.col * cellW + cellW / 2f
                        val cy = hole.row * cellH + cellH / 2f
                        val holeRadiusX = cellW * 0.38f
                        val holeRadiusY = cellH * 0.28f

                        // Outer Hole Rim
                        drawOval(
                            color = Color(0xFF1B0E07),
                            topLeft = Offset(cx - holeRadiusX, cy - holeRadiusY),
                            size = Size(holeRadiusX * 2f, holeRadiusY * 2f)
                        )
                        drawOval(
                            color = Color(0xFF5D4037),
                            topLeft = Offset(cx - holeRadiusX + 4f, cy - holeRadiusY + 4f),
                            size = Size((holeRadiusX - 4f) * 2f, (holeRadiusY - 4f) * 2f),
                            style = Stroke(width = 4f)
                        )

                        // Plant popping out
                        val plant = hole.plantType
                        if (plant != null && !hole.isHit) {
                            val popRatio = (hole.timerSec / hole.maxDurationSec).coerceIn(0f, 1f)
                            val offsetY = cy - (holeRadiusY * 0.8f * (1f - (1f - popRatio) * 0.2f))

                            when (plant) {
                                PlantType.DATE_SPROUT -> {
                                    // Sprout Stem
                                    drawLine(
                                        color = Color(0xFF4CAF50),
                                        start = Offset(cx, cy),
                                        end = Offset(cx, offsetY - 30f),
                                        strokeWidth = 8f
                                    )
                                    // Green Leaves
                                    drawCircle(
                                        color = Color(0xFF66BB6A),
                                        radius = 22f,
                                        center = Offset(cx - 18f, offsetY - 35f)
                                    )
                                    drawCircle(
                                        color = Color(0xFF66BB6A),
                                        radius = 22f,
                                        center = Offset(cx + 18f, offsetY - 35f)
                                    )
                                    // Ripe Dates
                                    drawOval(
                                        color = Color(0xFFFFB74D),
                                        topLeft = Offset(cx - 12f, offsetY - 25f),
                                        size = Size(24f, 32f)
                                    )
                                }

                                PlantType.FLOWER -> {
                                    // Flower Stem & Petals
                                    drawLine(
                                        color = Color(0xFF43A047),
                                        start = Offset(cx, cy),
                                        end = Offset(cx, offsetY - 20f),
                                        strokeWidth = 8f
                                    )
                                    val petalColors = listOf(
                                        Color(0xFFE91E63), Color(0xFFF48FB1), Color(0xFFAB47BC), Color(0xFF8E24AA)
                                    )
                                    repeat(5) { i ->
                                        val angle = i * (Math.PI * 2 / 5)
                                        val px = cx + (Math.cos(angle) * 22.0).toFloat()
                                        val py = offsetY - 30f + (Math.sin(angle) * 22.0).toFloat()
                                        drawCircle(
                                            color = petalColors[i % petalColors.size],
                                            radius = 16f,
                                            center = Offset(px, py)
                                        )
                                    }
                                    // Center Gold Core
                                    drawCircle(
                                        color = Gold,
                                        radius = 14f,
                                        center = Offset(cx, offsetY - 30f)
                                    )
                                }

                                PlantType.GOLDEN_FLOWER -> {
                                    // Golden Flower with Glowing Aura
                                    drawCircle(
                                        color = GoldSoft.copy(alpha = 0.5f),
                                        radius = 45f,
                                        center = Offset(cx, offsetY - 30f)
                                    )
                                    drawLine(
                                        color = Gold,
                                        start = Offset(cx, cy),
                                        end = Offset(cx, offsetY - 20f),
                                        strokeWidth = 10f
                                    )
                                    repeat(6) { i ->
                                        val angle = i * (Math.PI * 2 / 6)
                                        val px = cx + (Math.cos(angle) * 24.0).toFloat()
                                        val py = offsetY - 30f + (Math.sin(angle) * 24.0).toFloat()
                                        drawCircle(
                                            color = Color(0xFFFFD700),
                                            radius = 18f,
                                            center = Offset(px, py)
                                        )
                                    }
                                    drawCircle(
                                        color = Color.White,
                                        radius = 16f,
                                        center = Offset(cx, offsetY - 30f)
                                    )
                                }

                                PlantType.THORN_WEED -> {
                                    // Dark Prickly Thorn Weed
                                    val thornPath = Path().apply {
                                        moveTo(cx, cy)
                                        lineTo(cx - 25f, offsetY - 40f)
                                        lineTo(cx - 10f, offsetY - 15f)
                                        lineTo(cx, offsetY - 50f)
                                        lineTo(cx + 10f, offsetY - 15f)
                                        lineTo(cx + 25f, offsetY - 40f)
                                        close()
                                    }
                                    drawPath(thornPath, color = Color(0xFF263238))
                                    drawPath(thornPath, color = Color(0xFFFF5252), style = Stroke(width = 3f))
                                }
                            }

                            // Label text below plant
                            val nameLayout = textMeasurer.measure(
                                text = plant.nameAr,
                                style = TextStyle(
                                    color = if (plant.isHarmful) Color(0xFFFF5252) else Gold,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            drawText(
                                textLayoutResult = nameLayout,
                                topLeft = Offset(cx - nameLayout.size.width / 2f, cy + holeRadiusY + 4f)
                            )
                        }
                    }

                    // Render particles
                    particles.draw(this)
                }
            }
        }

        // Standard Game Overlays
        if (engineState.playState == GamePlayState.PAUSED) {
            PauseOverlay(
                onResume = { engineState.resumeGame() },
                onRestart = { initGame() },
                onQuit = onBack
            )
        }

        if (engineState.playState == GamePlayState.WON) {
            GameWinOverlay(
                score = engineState.score,
                stars = engineState.starsEarned,
                onNextLevel = {
                    engineState.nextLevel()
                    initGame()
                },
                onRestart = { initGame() },
                onQuit = onBack
            )
        }

        if (engineState.playState == GamePlayState.LOST) {
            GameLoseOverlay(
                score = engineState.score,
                onRestart = { initGame() },
                onQuit = onBack
            )
        }
    }
}
