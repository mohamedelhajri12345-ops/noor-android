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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.audio.SfxManager
import com.elhajri.noor.games.engine.*
import com.elhajri.noor.settings.t
import com.elhajri.noor.theme.NoorThemeState
import com.elhajri.noor.ui.noorGlassCard
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Game 5: Crescent Cannon (مدفع الهلال)
 * Category: مهارة
 * Mechanic: Aim trajectory slingshot shooter hitting floating crescents.
 */
data class TargetCrescent(
    var body: CircleBody,
    var color: Color,
    var isHit: Boolean = false
)

@Composable
fun CrescentCannonGame(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val theme = NoorThemeState.active
    val engineState = rememberGameEngineState(initialLevel = 1, initialLives = 5, targetScore = 80)

    val cannonPos = Offset(240f, 650f)
    var dragStartPos by remember { mutableStateOf<Offset?>(null) }
    var currentDragPos by remember { mutableStateOf<Offset?>(null) }

    var activeProjectile by remember { mutableStateOf<CircleBody?>(null) }
    val targets = remember { mutableStateListOf<TargetCrescent>() }
    val particles = remember { ParticleSystem() }

    fun spawnTargets() {
        targets.clear()
        for (i in 0 until 6) {
            val cx = 60f + Random.nextFloat() * 360f
            val cy = 100f + Random.nextFloat() * 300f
            targets.add(
                TargetCrescent(
                    body = CircleBody(center = Offset(cx, cy), radius = 28f),
                    color = if (i % 2 == 0) theme.accent else Color(0xFF00E676)
                )
            )
        }
    }

    fun initGame() {
        activeProjectile = null
        dragStartPos = null
        currentDragPos = null
        spawnTargets()
        engineState.startGame()
    }

    LaunchedEffect(Unit) {
        initGame()
    }

    fun fireCannon(velocity: Vector22D) {
        if (engineState.playState != GamePlayState.PLAYING) return
        if (activeProjectile != null) return

        triggerGameHaptic(context, GameHapticType.MEDIUM)
        SfxManager.playTap(context)

        activeProjectile = CircleBody(
            center = cannonPos,
            radius = 12f,
            velocity = velocity
        )
    }

    CanvasGameLoop(engineState = engineState) { deltaSec ->
        particles.update(deltaSec)

        val proj = activeProjectile
        if (proj != null && engineState.playState == GamePlayState.PLAYING) {
            // Move projectile with gravity
            proj.center = Offset(
                proj.center.x + proj.velocity.x * deltaSec,
                proj.center.y + proj.velocity.y * deltaSec
            )
            proj.velocity.y += 400f * deltaSec // gravity

            // Check collision with targets
            val iterator = targets.iterator()
            while (iterator.hasNext()) {
                val t = iterator.next()
                if (!t.isHit && proj.intersectsCircle(t.body)) {
                    t.isHit = true
                    particles.spawnBurst(t.body.center, count = 20)
                    triggerGameHaptic(context, GameHapticType.SUCCESS)
                    SfxManager.playSuccess(context)
                    engineState.addScore(20)

                    if (engineState.score >= engineState.targetScore) {
                        engineState.completeLevel(context, 3)
                    }
                }
            }

            // Screen bounds or off-screen
            if (proj.center.x < 0 || proj.center.x > 480 || proj.center.y > 720 || proj.center.y < 0) {
                activeProjectile = null
                if (engineState.loseLife()) {
                    engineState.failLevel(context)
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
                title = t("مدفع الهلال", "Crescent Aim Cannon"),
                level = engineState.level,
                score = engineState.score,
                lives = engineState.lives,
                timeRemainingMs = -1L,
                onBack = onBack,
                onPause = { engineState.pauseGame() }
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = t("اسحب للخلف لتحديد زاوية وإطلاق القذيفة نحو الأهلة!", "Drag back to aim trajectory and shoot at crescents!"),
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
                    .gameGestureDetector(
                        onDragStart = { offset ->
                            if (activeProjectile == null) {
                                dragStartPos = offset
                                currentDragPos = offset
                            }
                        },
                        onDragMove = { pos, _ ->
                            if (activeProjectile == null) {
                                currentDragPos = pos
                            }
                        },
                        onDragEnd = {
                            val start = dragStartPos
                            val curr = currentDragPos
                            if (start != null && curr != null && activeProjectile == null) {
                                val dx = start.x - curr.x
                                val dy = start.y - curr.y
                                val power = 3.0f
                                fireCannon(Vector22D(dx * power, dy * power))
                            }
                            dragStartPos = null
                            currentDragPos = null
                        }
                    )
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Draw Targets
                    targets.forEach { target ->
                        if (!target.isHit) {
                            drawCircle(
                                color = target.color,
                                radius = target.body.radius,
                                center = target.body.center
                            )
                            drawCircle(
                                color = Color.White.copy(alpha = 0.4f),
                                radius = target.body.radius,
                                center = target.body.center,
                                style = Stroke(width = 3f)
                            )
                        }
                    }

                    // Draw Cannon base
                    drawCircle(
                        color = theme.accent,
                        radius = 24f,
                        center = cannonPos
                    )

                    // Draw Trajectory line while dragging
                    val start = dragStartPos
                    val curr = currentDragPos
                    if (start != null && curr != null && activeProjectile == null) {
                        val dx = start.x - curr.x
                        val dy = start.y - curr.y
                        val traj = PhysicsEngine.computeTrajectory(
                            startPos = cannonPos,
                            velocity = Vector22D(dx * 3.0f, dy * 3.0f),
                            gravity = 400f,
                            steps = 15
                        )
                        traj.forEach { pt ->
                            drawCircle(
                                color = Color.White.copy(alpha = 0.7f),
                                radius = 4f,
                                center = pt
                            )
                        }
                    }

                    // Draw Active projectile
                    activeProjectile?.let { proj ->
                        drawCircle(
                            color = Color.Yellow,
                            radius = proj.radius,
                            center = proj.center
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
