package com.elhajri.noor.games

import android.content.Context
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.audio.SfxManager
import com.elhajri.noor.games.engine.*
import com.elhajri.noor.settings.t
import com.elhajri.noor.settings.toArabicDigits
import com.elhajri.noor.theme.NoorThemeState
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.Navy
import com.elhajri.noor.ui.noorGlassCard
import com.elhajri.noor.ui.themeScreenBackground
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.sin

/**
 * Game 55: Camel Jug Balance (توازن القافلة)
 * Category: تربوية (Educational)
 * Mechanic: Balance slider / tilt equilibrium simulator. Keep a tall stack of water jugs centered on a walking camel over bumpy desert dunes.
 */

private data class CamelJourneyConfig(
    val levelNumber: Int,
    val jugCount: Int = 4,
    val targetDistance: Float = 300f, // in meters
    val bumpiness: Float = 1.0f,
    val windGusts: Boolean = false
)

private val CAMEL_BALANCE_LEVELS = listOf(
    CamelJourneyConfig(levelNumber = 1, jugCount = 3, targetDistance = 250f, bumpiness = 0.8f),
    CamelJourneyConfig(levelNumber = 2, jugCount = 4, targetDistance = 350f, bumpiness = 1.2f),
    CamelJourneyConfig(levelNumber = 3, jugCount = 5, targetDistance = 450f, bumpiness = 1.5f, windGusts = true),
    CamelJourneyConfig(levelNumber = 4, jugCount = 6, targetDistance = 550f, bumpiness = 1.8f, windGusts = true)
)

@Composable
fun CamelBalanceTiltGame(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val theme = NoorThemeState.active
    val engineState = rememberGameEngineState(initialLevel = 1, initialLives = 3, targetScore = 100)

    val currentConfig = remember(engineState.level) {
        val idx = (engineState.level - 1) % CAMEL_BALANCE_LEVELS.size
        CAMEL_BALANCE_LEVELS[idx]
    }

    // Physics State
    var tiltAngleRad by remember { mutableFloatStateOf(0f) } // Stack tilt angle in radians
    var angularVelocity by remember { mutableFloatStateOf(0f) }
    var currentDistance by remember { mutableFloatStateOf(0f) }
    var userCorrectionImpulse by remember { mutableFloatStateOf(0f) }

    var sliderValue by remember { mutableFloatStateOf(0.5f) } // 0.0 (left) to 1.0 (right)
    val particles = remember { ParticleSystem() }

    fun initJourney() {
        tiltAngleRad = 0f
        angularVelocity = 0f
        currentDistance = 0f
        userCorrectionImpulse = 0f
        sliderValue = 0.5f
        engineState.startGame()
    }

    LaunchedEffect(engineState.level) {
        initJourney()
    }

    // 60 FPS Real-time physics engine loop
    CanvasGameLoop(engineState) { deltaSec ->
        particles.update(deltaSec)

        if (engineState.playState == GamePlayState.PLAYING) {
            // Distance progression
            val speed = 25f // meters / sec
            currentDistance += speed * deltaSec

            // Terrain bump torque force
            val terrainFrequency = 0.05f
            val bumpTorque = sin(currentDistance * terrainFrequency) * 0.8f * currentConfig.bumpiness

            // Gravity torque pulling stack further down if tilted
            val gravityTorque = 9.8f * sin(tiltAngleRad) * (currentConfig.jugCount * 0.25f)

            // User balance correction torque from slider
            val targetImpulse = (sliderValue - 0.5f) * -12.0f
            userCorrectionImpulse += (targetImpulse - userCorrectionImpulse) * (deltaSec * 10f)

            // Angular acceleration equation
            val damping = 2.5f
            val angularAcc = gravityTorque + bumpTorque + userCorrectionImpulse - (damping * angularVelocity)

            angularVelocity += angularAcc * deltaSec
            tiltAngleRad += angularVelocity * deltaSec

            // Critical tilt angle check
            val maxTiltRad = 0.55f // ~32 degrees
            if (abs(tiltAngleRad) > maxTiltRad) {
                // Jugs fall!
                particles.spawnBurst(Offset(240f, 300f), count = 30)
                SfxManager.playWrong(context)
                triggerGameHaptic(context, GameHapticType.ERROR)

                val lost = engineState.loseLife()
                if (!lost) {
                    // Reset stack position
                    tiltAngleRad = 0f
                    angularVelocity = 0f
                    sliderValue = 0.5f
                }
            }

            // Check goal destination
            if (currentDistance >= currentConfig.targetDistance) {
                particles.spawnBurst(Offset(240f, 250f), count = 50)
                SfxManager.playSuccess(context)
                engineState.addScore(100 + (engineState.lives * 20))
                engineState.completeLevel(context, stars = engineState.lives)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .themeScreenBackground()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            GameHeaderBar(
                title = t("توازن القافلة", "Camel Jug Balance"),
                level = engineState.level,
                score = engineState.score,
                lives = engineState.lives,
                onBack = onBack,
                onPause = { engineState.pauseGame() }
            )

            // Distance Progress & Balance Indicator Gauge
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .noorGlassCard(cornerRadius = 12.dp),
                color = Color.Transparent
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = t("مسافة المسار نحو الواحة", "Journey to Oasis"),
                            color = theme.textSecondary,
                            fontSize = 12.sp
                        )
                        Text(
                            text = t(
                                "${currentDistance.toInt().toString().toArabicDigits()} / ${currentConfig.targetDistance.toInt().toString().toArabicDigits()} م",
                                "${currentDistance.toInt()} / ${currentConfig.targetDistance.toInt()} m"
                            ),
                            color = theme.accent,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { (currentDistance / currentConfig.targetDistance).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Gold,
                        trackColor = Color.DarkGray
                    )
                }
            }

            // Main Desert & Camel Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(12.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val camelCenterX = width * 0.5f
                    val camelBaseY = height * 0.65f

                    // 1. Draw Dunes & Desert Background
                    drawDesertDunes(width, height, currentDistance, theme.accent)

                    // 2. Draw Camel
                    drawWalkingCamel(Offset(camelCenterX, camelBaseY), Gold)

                    // 3. Draw Stack of Water Jugs with Rotation tiltAngleRad
                    val tiltDeg = Math.toDegrees(tiltAngleRad.toDouble()).toFloat()
                    val stackBase = Offset(camelCenterX, camelBaseY - 50f)

                    rotate(degrees = tiltDeg, pivot = stackBase) {
                        drawStackedWaterJugs(stackBase, currentConfig.jugCount, GoldSoft)
                    }

                    // 4. Draw Particles
                    particles.draw(this)
                }
            }

            // Balance Slider Bar Control
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .noorGlassCard(cornerRadius = 16.dp),
                color = Color.Transparent
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = t("تعديل اتزان الجمال والأباريق", "Adjust Camel Balance"),
                        color = theme.textPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = t("يسار", "Left"),
                            color = theme.textSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Slider(
                            value = sliderValue,
                            onValueChange = { newValue ->
                                sliderValue = newValue
                                triggerGameHaptic(context, GameHapticType.LIGHT)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp),
                            colors = SliderDefaults.colors(
                                thumbColor = Gold,
                                activeTrackColor = Gold,
                                inactiveTrackColor = Color.Gray
                            )
                        )
                        Text(
                            text = t("يمين", "Right"),
                            color = theme.textSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Win and Pause Overlays
        if (engineState.playState == GamePlayState.PAUSED) {
            PauseOverlay(
                onResume = { engineState.resumeGame() },
                onRestart = { initJourney() },
                onQuit = onBack
            )
        }

        if (engineState.playState == GamePlayState.WON) {
            GameWinOverlay(
                score = engineState.score,
                stars = engineState.starsEarned,
                onNextLevel = {
                    if (engineState.level < CAMEL_BALANCE_LEVELS.size) {
                        engineState.nextLevel()
                    } else {
                        onBack()
                    }
                },
                onRestart = { initJourney() },
                onQuit = onBack
            )
        }
    }
}

private fun DrawScope.drawDesertDunes(
    width: Float,
    height: Float,
    distance: Float,
    accentColor: Color
) {
    // Dune path
    val dunePath = Path().apply {
        moveTo(0f, height * 0.70f)
        var x = 0f
        while (x <= width) {
            val y = height * 0.70f + sin((x + distance * 5f) * 0.02f) * 20f
            lineTo(x, y)
            x += 20f
        }
        lineTo(width, height)
        lineTo(0f, height)
        close()
    }
    drawPath(dunePath, color = Color(0xFFC29B38).copy(alpha = 0.6f))
}

private fun DrawScope.drawWalkingCamel(center: Offset, goldColor: Color) {
    // Camel body
    drawRoundRect(
        color = goldColor,
        topLeft = Offset(center.x - 30f, center.y - 20f),
        size = Size(60f, 35f),
        cornerRadius = CornerRadius(10f)
    )

    // Hump
    drawCircle(color = goldColor, radius = 22f, center = Offset(center.x, center.y - 28f))

    // Head and neck
    drawLine(color = goldColor, start = Offset(center.x + 25f, center.y - 10f), end = Offset(center.x + 40f, center.y - 45f), strokeWidth = 8f)
    drawCircle(color = goldColor, radius = 10f, center = Offset(center.x + 42f, center.y - 48f))

    // Legs
    drawLine(color = goldColor, start = Offset(center.x - 20f, center.y + 15f), end = Offset(center.x - 20f, center.y + 45f), strokeWidth = 5f)
    drawLine(color = goldColor, start = Offset(center.x + 20f, center.y + 15f), end = Offset(center.x + 20f, center.y + 45f), strokeWidth = 5f)
}

private fun DrawScope.drawStackedWaterJugs(
    base: Offset,
    count: Int,
    jugColor: Color
) {
    for (i in 0 until count) {
        val jugY = base.y - (i * 28f)
        val radius = 12f

        // Ceramic jug body
        drawCircle(color = jugColor, radius = radius, center = Offset(base.x, jugY))
        drawCircle(color = Gold, radius = radius, center = Offset(base.x, jugY), style = Stroke(width = 2f))

        // Jug top neck
        drawRect(
            color = Gold,
            topLeft = Offset(base.x - 4f, jugY - radius - 6f),
            size = Size(8f, 6f)
        )
    }
}
