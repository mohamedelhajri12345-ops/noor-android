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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
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
 * Game 35: Caravan Zuma Stream (قطار القافلة)
 * Category: متعة
 * Mechanic: Rolling chain marble shooter. Aim central cannon at spiral camel chain and match 3 colors.
 */
private data class ZumaMarble(
    val id: Int,
    var color: Color,
    var distance: Float // 0.0 (start) to 1.0 (center pit)
)

private data class ZumaProjectile(
    var center: Offset,
    var velocity: Offset,
    val color: Color
)

@Composable
fun SpiralZumaGame(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val theme = NoorThemeState.active
    val engineState = rememberGameEngineState(
        initialLevel = 1,
        initialLives = 3,
        targetScore = 150
    )

    val marbleRadius = 14f
    val cannonCenter = Offset(200f, 250f)

    val colors = remember {
        listOf(
            Color(0xFFFFD700), // Gold
            Color(0xFF64FFDA), // Turquoise
            Color(0xFF00E676), // Emerald
            Color(0xFFFF5252), // Crimson
            Color(0xFFB388FF)  // Amethyst
        )
    }

    var aimAngle by remember { mutableFloatStateOf(-PI.toFloat() / 2f) }
    var currentAmmoColor by remember { mutableStateOf(colors[0]) }
    var nextAmmoColor by remember { mutableStateOf(colors[1]) }
    var activeProjectile by remember { mutableStateOf<ZumaProjectile?>(null) }

    val marbleChain = remember { mutableStateListOf<ZumaMarble>() }
    val particles = remember { ParticleSystem() }
    var marbleIdCounter by remember { mutableIntStateOf(1) }

    // Parametric Archimedean Spiral Path: t from 0.0f (outer) to 1.0f (center)
    fun getSpiralPoint(t: Float): Offset {
        val clampedT = t.coerceIn(0f, 1f)
        val turns = 2.5f
        val angle = (1f - clampedT) * turns * 2f * PI.toFloat()
        val radius = 180f * clampedT + 20f
        val x = cannonCenter.x + radius * cos(angle)
        val y = cannonCenter.y + radius * sin(angle)
        return Offset(x, y)
    }

    fun spawnInitialChain() {
        marbleChain.clear()
        val initialCount = 18
        val spacing = 0.032f
        for (i in 0 until initialCount) {
            val dist = 0.05f + (initialCount - 1 - i) * spacing
            marbleChain.add(
                ZumaMarble(
                    id = marbleIdCounter++,
                    color = colors.random(),
                    distance = dist
                )
            )
        }
    }

    fun initGame() {
        activeProjectile = null
        currentAmmoColor = colors.random()
        nextAmmoColor = colors.random()
        aimAngle = -PI.toFloat() / 2f
        spawnInitialChain()
        engineState.startGame()
    }

    LaunchedEffect(Unit) {
        initGame()
    }

    fun fireCannon() {
        if (activeProjectile != null || engineState.playState != GamePlayState.PLAYING) return

        val speed = 750f
        val vx = cos(aimAngle) * speed
        val vy = sin(aimAngle) * speed

        activeProjectile = ZumaProjectile(
            center = cannonCenter,
            velocity = Offset(vx, vy),
            color = currentAmmoColor
        )

        currentAmmoColor = nextAmmoColor
        nextAmmoColor = colors.random()

        triggerGameHaptic(context, GameHapticType.MEDIUM)
        SfxManager.playTap(context)
    }

    fun checkAndPopMatches(insertedIndex: Int) {
        if (insertedIndex !in marbleChain.indices) return
        val targetColor = marbleChain[insertedIndex].color

        var left = insertedIndex
        while (left > 0 && marbleChain[left - 1].color == targetColor) {
            left--
        }

        var right = insertedIndex
        while (right < marbleChain.size - 1 && marbleChain[right + 1].color == targetColor) {
            right++
        }

        val matchCount = right - left + 1
        if (matchCount >= 3) {
            // Burst particles for matched marbles
            for (i in left..right) {
                if (i in marbleChain.indices) {
                    val pos = getSpiralPoint(marbleChain[i].distance)
                    particles.spawnBurst(pos, count = 15)
                }
            }

            // Remove matched marbles
            val removeList = marbleChain.subList(left, right + 1).toList()
            marbleChain.removeAll(removeList)

            val points = matchCount * 10 + (matchCount - 3) * 15
            engineState.addScore(points)
            triggerGameHaptic(context, GameHapticType.SUCCESS)
            SfxManager.playSuccess(context)

            if (marbleChain.isEmpty()) {
                engineState.completeLevel(context, 3)
            } else if (engineState.score >= engineState.targetScore) {
                engineState.completeLevel(context, 3)
            }
        }
    }

    CanvasGameLoop(engineState = engineState) { deltaSec ->
        particles.update(deltaSec)

        if (engineState.playState == GamePlayState.PLAYING) {
            // 1. Advance Marble Chain along spiral path
            val chainSpeed = 0.015f * deltaSec
            var overflowPit = false

            for (i in marbleChain.indices) {
                marbleChain[i] = marbleChain[i].copy(distance = marbleChain[i].distance - chainSpeed)
                if (marbleChain[i].distance <= 0.01f) {
                    overflowPit = true
                }
            }

            // If chain reaches pit center
            if (overflowPit) {
                triggerGameHaptic(context, GameHapticType.ERROR)
                SfxManager.playWrong(context)
                if (engineState.loseLife()) {
                    engineState.failLevel(context)
                } else {
                    spawnInitialChain()
                }
            }

            // 2. Update Active Projectile
            val proj = activeProjectile
            if (proj != null) {
                val newPos = proj.center + proj.velocity * deltaSec

                // Check collision with chain marbles
                var hitIndex = -1
                var minHitDist = Float.MAX_VALUE

                for (idx in marbleChain.indices) {
                    val marblePos = getSpiralPoint(marbleChain[idx].distance)
                    val dist = (newPos - marblePos).getDistance()
                    if (dist < marbleRadius * 2f && dist < minHitDist) {
                        minHitDist = dist
                        hitIndex = idx
                    }
                }

                if (hitIndex != -1) {
                    // Collision with chain marble!
                    val hitMarble = marbleChain[hitIndex]
                    val insertedDistance = hitMarble.distance + 0.025f
                    val newMarble = ZumaMarble(
                        id = marbleIdCounter++,
                        color = proj.color,
                        distance = insertedDistance
                    )

                    marbleChain.add(hitIndex, newMarble)
                    activeProjectile = null

                    // Adjust downstream chain distances
                    for (i in 0 until hitIndex) {
                        marbleChain[i] = marbleChain[i].copy(distance = marbleChain[i].distance + 0.028f)
                    }

                    checkAndPopMatches(hitIndex)
                } else if (newPos.x < 0f || newPos.x > 400f || newPos.y < 0f || newPos.y > 500f) {
                    // Off screen
                    activeProjectile = null
                } else {
                    activeProjectile = proj.copy(center = newPos)
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
                title = t("قطار القافلة", "Caravan Zuma Stream"),
                level = engineState.level,
                score = engineState.score,
                lives = engineState.lives,
                onBack = onBack,
                onPause = { engineState.pauseGame() }
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = t("وجه المدفع واطلق التمر لمطابقة ٣ ألوان متتالية قبل وصول القافلة للمنتصف!", "Aim cannon and fire dates to match 3 colors before chain reaches pit!"),
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
                        detectDragGestures(
                            onDrag = { change, _ ->
                                change.consume()
                                val dx = change.position.x - size.width / 2f
                                val dy = change.position.y - size.height / 2f
                                aimAngle = atan2(dy, dx)
                            },
                            onDragEnd = {
                                fireCannon()
                            }
                        )
                    }
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { offset ->
                                val dx = offset.x - size.width / 2f
                                val dy = offset.y - size.height / 2f
                                aimAngle = atan2(dy, dx)
                                fireCannon()
                            }
                        )
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val boardWidth = size.width
                    val boardHeight = size.height
                    val scaleX = boardWidth / 400f
                    val scaleY = boardHeight / 500f

                    // Draw Spiral Track Path
                    val steps = 120
                    var prevPt = getSpiralPoint(0f)
                    for (step in 1..steps) {
                        val t = step / steps.toFloat()
                        val currPt = getSpiralPoint(t)
                        drawLine(
                            color = Color(0xFF2A1B0E),
                            start = Offset(prevPt.x * scaleX, prevPt.y * scaleY),
                            end = Offset(currPt.x * scaleX, currPt.y * scaleY),
                            strokeWidth = marbleRadius * 2.2f * scaleX
                        )
                        prevPt = currPt
                    }

                    // Pit in Center
                    val centerDraw = Offset(cannonCenter.x * scaleX, cannonCenter.y * scaleY)
                    drawCircle(
                        color = Color.Black,
                        radius = 28f * scaleX,
                        center = centerDraw
                    )
                    drawCircle(
                        color = theme.accent,
                        radius = 28f * scaleX,
                        center = centerDraw,
                        style = Stroke(width = 3f * scaleX)
                    )

                    // Draw Chain Marbles
                    marbleChain.forEach { marble ->
                        val pos = getSpiralPoint(marble.distance)
                        val drawPos = Offset(pos.x * scaleX, pos.y * scaleY)
                        drawCircle(
                            color = marble.color,
                            radius = marbleRadius * scaleX,
                            center = drawPos
                        )
                        drawCircle(
                            color = Color.White.copy(alpha = 0.5f),
                            radius = marbleRadius * 0.4f * scaleX,
                            center = drawPos - Offset(3f * scaleX, 3f * scaleY)
                        )
                    }

                    // Draw Central Cannon Base
                    drawCircle(
                        color = Color(0xFF1E1408),
                        radius = 22f * scaleX,
                        center = centerDraw
                    )

                    // Aim Barrel Line
                    val barrelLen = 35f * scaleX
                    val barrelTip = Offset(
                        centerDraw.x + cos(aimAngle) * barrelLen,
                        centerDraw.y + sin(aimAngle) * barrelLen
                    )
                    drawLine(
                        color = theme.accent,
                        start = centerDraw,
                        end = barrelTip,
                        strokeWidth = 8f * scaleX
                    )

                    // Current Ammo Marble inside Cannon
                    drawCircle(
                        color = currentAmmoColor,
                        radius = marbleRadius * 0.9f * scaleX,
                        center = centerDraw
                    )

                    // Next Ammo Marble Preview near Cannon
                    drawCircle(
                        color = nextAmmoColor,
                        radius = marbleRadius * 0.5f * scaleX,
                        center = centerDraw + Offset(25f * scaleX, 25f * scaleY)
                    )

                    // Draw Active Fired Projectile
                    activeProjectile?.let { proj ->
                        val projDraw = Offset(proj.center.x * scaleX, proj.center.y * scaleY)
                        drawCircle(
                            color = proj.color,
                            radius = marbleRadius * scaleX,
                            center = projDraw
                        )
                        drawCircle(
                            color = Color.White,
                            radius = marbleRadius * 0.4f * scaleX,
                            center = projDraw - Offset(3f * scaleX, 3f * scaleY)
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
