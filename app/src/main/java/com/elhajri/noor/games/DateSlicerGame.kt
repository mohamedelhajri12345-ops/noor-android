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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
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
 * Game 19: Date Slicer / Fruit Harvest Slicer (حصاد الثمار / تقطيع التمر)
 * Category: مهارة
 * Mechanic: Fruit Ninja style swipe slicing game.
 * Swipe finger continuously across flying dates, olives, and pomegranates. Avoid thorn bombs!
 */

private enum class HarvestFruitType {
    DATE,        // التمر (Brown)
    OLIVE,       // الزيتون (Emerald Green)
    POMEGRANATE, // الرمان (Ruby Red)
    THORN_BOMB   // قنبلة الشوك (Spiked Black Bomb)
}

private data class FlyingFruit(
    val id: Int,
    var position: Offset,
    var velocity: Vector22D,
    val type: HarvestFruitType,
    val radius: Float,
    var angle: Float = 0f,
    val rotationSpeed: Float,
    var isSliced: Boolean = false,
    var isExploded: Boolean = false
)

private data class HalfFruitPiece(
    var position: Offset,
    var velocity: Vector22D,
    val color: Color,
    val radius: Float,
    var angle: Float = 0f,
    val rotationSpeed: Float = 4f,
    var alpha: Float = 1.0f,
    var ageSec: Float = 0f
)

private data class SwipeTrailPoint(
    val position: Offset,
    val timeMs: Long = System.currentTimeMillis()
)

@Composable
fun DateSlicerGame(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val theme = NoorThemeState.active
    val engineState = rememberGameEngineState(initialLevel = 1, initialLives = 3, targetScore = 150)

    val canvasWidth = 440f
    val canvasHeight = 600f

    val flyingFruits = remember { mutableStateListOf<FlyingFruit>() }
    val halfPieces = remember { mutableStateListOf<HalfFruitPiece>() }
    val swipeTrail = remember { mutableStateListOf<SwipeTrailPoint>() }
    val particles = remember { ParticleSystem() }

    var spawnTimerSec by remember { mutableFloatStateOf(0f) }
    var spawnIntervalSec by remember { mutableFloatStateOf(1.2f) }
    var fruitIdCounter by remember { mutableIntStateOf(1) }
    var swipeStreakCount by remember { mutableIntStateOf(0) }

    fun intersectsLineSegment(p1: Offset, p2: Offset, circleCenter: Offset, radius: Float): Boolean {
        val dx = p2.x - p1.x
        val dy = p2.y - p1.y
        val lenSq = dx * dx + dy * dy
        if (lenSq == 0f) {
            val distSq = (circleCenter.x - p1.x) * (circleCenter.x - p1.x) + (circleCenter.y - p1.y) * (circleCenter.y - p1.y)
            return distSq <= radius * radius
        }
        val t = (((circleCenter.x - p1.x) * dx + (circleCenter.y - p1.y) * dy) / lenSq).coerceIn(0f, 1f)
        val projX = p1.x + t * dx
        val projY = p1.y + t * dy
        val distSq = (circleCenter.x - projX) * (circleCenter.x - projX) + (circleCenter.y - projY) * (circleCenter.y - projY)
        return distSq <= radius * radius
    }

    fun spawnWave() {
        val count = Random.nextInt(1, 3)
        for (i in 0 until count) {
            val spawnX = 80f + Random.nextFloat() * (canvasWidth - 160f)
            val spawnY = canvasHeight + 30f
            val vx = (-120f + Random.nextFloat() * 240f)
            val vy = -520f - Random.nextFloat() * 140f

            val isBomb = Random.nextFloat() < 0.22f
            val type = if (isBomb) {
                HarvestFruitType.THORN_BOMB
            } else {
                when (Random.nextInt(3)) {
                    0 -> HarvestFruitType.DATE
                    1 -> HarvestFruitType.OLIVE
                    else -> HarvestFruitType.POMEGRANATE
                }
            }

            val radius = when (type) {
                HarvestFruitType.DATE -> 22f
                HarvestFruitType.OLIVE -> 18f
                HarvestFruitType.POMEGRANATE -> 28f
                HarvestFruitType.THORN_BOMB -> 24f
            }

            val rot = -3f + Random.nextFloat() * 6f

            flyingFruits.add(
                FlyingFruit(
                    id = fruitIdCounter++,
                    position = Offset(spawnX, spawnY),
                    velocity = Vector22D(vx, vy),
                    type = type,
                    radius = radius,
                    rotationSpeed = rot
                )
            )
        }
    }

    fun initGame() {
        flyingFruits.clear()
        halfPieces.clear()
        swipeTrail.clear()
        spawnTimerSec = 0f
        spawnIntervalSec = 1.2f
        fruitIdCounter = 1
        swipeStreakCount = 0
        spawnWave()
        engineState.startGame()
    }

    LaunchedEffect(Unit) {
        initGame()
    }

    CanvasGameLoop(engineState = engineState) { deltaSec ->
        particles.update(deltaSec)

        if (engineState.playState != GamePlayState.PLAYING) return@CanvasGameLoop

        // Clean up old swipe trail points (> 200ms old)
        val now = System.currentTimeMillis()
        swipeTrail.removeAll { now - it.timeMs > 200 }

        // Spawn timer logic
        spawnTimerSec += deltaSec
        if (spawnTimerSec >= spawnIntervalSec) {
            spawnTimerSec = 0f
            spawnIntervalSec = (0.9f + Random.nextFloat() * 0.6f).coerceAtLeast(0.7f)
            spawnWave()
        }

        // Gravity physics for flying fruits
        val gravity = 480f
        val fruitIterator = flyingFruits.iterator()
        while (fruitIterator.hasNext()) {
            val fruit = fruitIterator.next()
            fruit.position = Offset(
                fruit.position.x + fruit.velocity.x * deltaSec,
                fruit.position.y + fruit.velocity.y * deltaSec
            )
            fruit.velocity.y += gravity * deltaSec
            fruit.angle += fruit.rotationSpeed * deltaSec

            // Check swipe intersection against current swipe trail segments
            if (!fruit.isSliced && !fruit.isExploded && swipeTrail.size >= 2) {
                for (i in 0 until swipeTrail.size - 1) {
                    val p1 = swipeTrail[i].position
                    val p2 = swipeTrail[i + 1].position
                    if (intersectsLineSegment(p1, p2, fruit.position, fruit.radius)) {
                        if (fruit.type == HarvestFruitType.THORN_BOMB) {
                            // Bomb exploded!
                            fruit.isExploded = true
                            triggerGameHaptic(context, GameHapticType.ERROR)
                            SfxManager.playWrong(context)
                            particles.spawnBurst(
                                origin = fruit.position,
                                count = 35,
                                colors = listOf(Color.Red, Color.Black, Color(0xFFFF5252))
                            )
                            if (engineState.loseLife()) {
                                engineState.failLevel(context)
                            }
                        } else {
                            // Fruit sliced cleanly!
                            fruit.isSliced = true
                            swipeStreakCount++
                            val pts = when (fruit.type) {
                                HarvestFruitType.DATE -> 10
                                HarvestFruitType.OLIVE -> 15
                                HarvestFruitType.POMEGRANATE -> 20
                                else -> 0
                            } + (if (swipeStreakCount >= 3) 10 else 0)

                            engineState.addScore(pts)
                            triggerGameHaptic(context, GameHapticType.SUCCESS)
                            SfxManager.playSuccess(context)

                            val fruitColor = when (fruit.type) {
                                HarvestFruitType.DATE -> Color(0xFF6D4C41)
                                HarvestFruitType.OLIVE -> Color(0xFF2E7D32)
                                HarvestFruitType.POMEGRANATE -> Color(0xFFC62828)
                                else -> Color.White
                            }

                            // Spawn two half-fruit pieces
                            halfPieces.add(
                                HalfFruitPiece(
                                    position = fruit.position,
                                    velocity = Vector22D(-120f, -180f),
                                    color = fruitColor,
                                    radius = fruit.radius * 0.85f
                                )
                            )
                            halfPieces.add(
                                HalfFruitPiece(
                                    position = fruit.position,
                                    velocity = Vector22D(120f, -180f),
                                    color = fruitColor,
                                    radius = fruit.radius * 0.85f
                                )
                            )

                            particles.spawnBurst(
                                origin = fruit.position,
                                count = 22,
                                colors = listOf(fruitColor, Color.White, Color(0xFFFFD700))
                            )
                        }
                        break
                    }
                }
            }

            // Remove sliced/exploded fruits or fruits that fell off bottom
            if (fruit.isSliced || fruit.isExploded) {
                fruitIterator.remove()
            } else if (fruit.position.y > canvasHeight + 60f && fruit.velocity.y > 0f) {
                // If a non-bomb fruit fell unsliced, lose a life
                if (fruit.type != HarvestFruitType.THORN_BOMB) {
                    triggerGameHaptic(context, GameHapticType.LIGHT)
                    if (engineState.loseLife()) {
                        engineState.failLevel(context)
                    }
                }
                fruitIterator.remove()
            }
        }

        // Update half-fruit pieces physics
        val halfIterator = halfPieces.iterator()
        while (halfIterator.hasNext()) {
            val half = halfIterator.next()
            half.ageSec += deltaSec
            if (half.ageSec >= 1.2f) {
                halfIterator.remove()
                continue
            }
            half.position = Offset(
                half.position.x + half.velocity.x * deltaSec,
                half.position.y + half.velocity.y * deltaSec
            )
            half.velocity.y += gravity * 0.8f * deltaSec
            half.angle += half.rotationSpeed * deltaSec
            half.alpha = (1.0f - half.ageSec / 1.2f).coerceIn(0f, 1f)
        }

        // Check level completion
        if (engineState.score >= engineState.targetScore) {
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
                title = t("حصاد الثمار", "Fruit Harvest Slicer"),
                level = engineState.level,
                score = engineState.score,
                lives = engineState.lives,
                timeRemainingMs = -1L,
                onBack = onBack,
                onPause = { engineState.pauseGame() }
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = t("امسح بإصبعك لتقطيع التمر والزيتون والرمان، واحذر قنابل الشوك!", "Swipe your finger to slice flying dates, olives & pomegranates! Avoid thorn bombs!"),
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
                                swipeTrail.clear()
                                swipeTrail.add(SwipeTrailPoint(offset))
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                swipeTrail.add(SwipeTrailPoint(change.position))
                            },
                            onDragEnd = {
                                swipeStreakCount = 0
                            }
                        )
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val scaleX = size.width / canvasWidth
                    val scaleY = size.height / canvasHeight

                    // Render Flying Fruits & Bombs
                    flyingFruits.forEach { fruit ->
                        val fx = fruit.position.x * scaleX
                        val fy = fruit.position.y * scaleY
                        val r = fruit.radius * scaleX

                        when (fruit.type) {
                            HarvestFruitType.DATE -> {
                                // Date fruit (Brown oval)
                                drawCircle(color = Color(0xFF6D4C41), radius = r, center = Offset(fx, fy))
                                drawCircle(
                                    color = Color(0xFF8D6E63),
                                    radius = r * 0.65f,
                                    center = Offset(fx - 2f, fy - 2f)
                                )
                            }
                            HarvestFruitType.OLIVE -> {
                                // Olive (Emerald green)
                                drawCircle(color = Color(0xFF2E7D32), radius = r, center = Offset(fx, fy))
                                drawCircle(
                                    color = Color(0xFF66BB6A),
                                    radius = r * 0.5f,
                                    center = Offset(fx - 2f, fy - 2f)
                                )
                            }
                            HarvestFruitType.POMEGRANATE -> {
                                // Pomegranate (Ruby red)
                                drawCircle(color = Color(0xFFC62828), radius = r, center = Offset(fx, fy))
                                drawCircle(
                                    color = Color(0xFFFF8A80),
                                    radius = r * 0.6f,
                                    center = Offset(fx - 3f, fy - 3f)
                                )
                                drawCircle(
                                    color = Color(0xFFFFD700),
                                    radius = r * 0.3f,
                                    center = Offset(fx, fy)
                                )
                            }
                            HarvestFruitType.THORN_BOMB -> {
                                // Black Thorn Bomb with gold spikes
                                drawCircle(color = Color(0xFF212121), radius = r, center = Offset(fx, fy))
                                drawCircle(
                                    color = Color.Red,
                                    radius = r * 0.35f,
                                    center = Offset(fx, fy)
                                )
                                // Spikes
                                for (i in 0 until 6) {
                                    val angleRad = Math.toRadians((i * 60.0 + fruit.angle * 20.0)).toFloat()
                                    val sx = fx + cos(angleRad.toDouble()).toFloat() * (r + 8f)
                                    val sy = fy + sin(angleRad.toDouble()).toFloat() * (r + 8f)
                                    drawLine(
                                        color = Color(0xFFFFD700),
                                        start = Offset(fx, fy),
                                        end = Offset(sx, sy),
                                        strokeWidth = 3.5f
                                    )
                                }
                            }
                        }
                    }

                    // Render Half-Fruit Pieces flying apart
                    halfPieces.forEach { half ->
                        val hx = half.position.x * scaleX
                        val hy = half.position.y * scaleY
                        val hr = half.radius * scaleX
                        drawCircle(
                            color = half.color.copy(alpha = half.alpha),
                            radius = hr,
                            center = Offset(hx, hy)
                        )
                    }

                    // Render Blade Swipe Trail
                    if (swipeTrail.size >= 2) {
                        val path = Path()
                        val first = swipeTrail.first()
                        path.moveTo(first.position.x, first.position.y)
                        for (i in 1 until swipeTrail.size) {
                            path.lineTo(swipeTrail[i].position.x, swipeTrail[i].position.y)
                        }
                        drawPath(
                            path = path,
                            color = theme.accent,
                            style = Stroke(width = 8f, cap = StrokeCap.Round)
                        )
                        drawPath(
                            path = path,
                            color = Color.White,
                            style = Stroke(width = 3f, cap = StrokeCap.Round)
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
