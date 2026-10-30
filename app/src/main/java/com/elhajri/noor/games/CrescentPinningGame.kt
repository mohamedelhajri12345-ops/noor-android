package com.elhajri.noor.games

import androidx.compose.animation.core.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.audio.SfxManager
import com.elhajri.noor.games.engine.*
import com.elhajri.noor.settings.t
import com.elhajri.noor.settings.toArabicDigits
import com.elhajri.noor.theme.NoorThemeState
import com.elhajri.noor.ui.noorGlassCard
import kotlin.math.*

/**
 * Game 17: Crescent Pin Spinner (دائرة الهلال)
 * ID: game_17_crescent_pinning
 * Category: مهارة
 * Mechanic: Rotating wheel pin insertion (aa style). Tap the screen to shoot numbered pins
 * into a central rotating crescent wheel without letting pins touch.
 */
private data class PinOnWheel(
    val id: Int,
    val attachedAngleDeg: Float,
    val number: Int
)

@Composable
fun CrescentPinningGame(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val theme = NoorThemeState.active
    val textMeasurer = rememberTextMeasurer()

    val engineState = rememberGameEngineState(
        initialLevel = 1,
        initialLives = 3,
        targetScore = 80
    )

    var wheelAngle by remember { mutableFloatStateOf(0f) }
    var directionMultiplier by remember { mutableFloatStateOf(1f) }
    var directionTimer by remember { mutableFloatStateOf(0f) }

    val attachedPins = remember { mutableStateListOf<PinOnWheel>() }
    var remainingPinsToLaunch by remember { mutableIntStateOf(8) }
    var currentPinNumber by remember { mutableIntStateOf(1) }

    var isLaunching by remember { mutableStateOf(false) }
    var launchProgressY by remember { mutableFloatStateOf(0f) }

    val particles = remember { ParticleSystem() }

    fun normalizeAngle(angle: Float): Float {
        var a = angle % 360f
        if (a < 0f) a += 360f
        return a
    }

    fun angularDifference(a1: Float, a2: Float): Float {
        val diff = abs(normalizeAngle(a1) - normalizeAngle(a2))
        return if (diff > 180f) 360f - diff else diff
    }

    fun initGame() {
        wheelAngle = 0f
        directionMultiplier = 1f
        directionTimer = 0f
        isLaunching = false
        launchProgressY = 0f

        val totalPinsForLevel = 8 + (engineState.level * 2)
        remainingPinsToLaunch = totalPinsForLevel
        currentPinNumber = 1

        attachedPins.clear()
        // Add pre-existing initial pins on wheel for challenge
        val initialCount = min(2 + engineState.level, 5)
        val step = 360f / initialCount
        for (i in 0 until initialCount) {
            attachedPins.add(
                PinOnWheel(
                    id = -1 - i,
                    attachedAngleDeg = i * step,
                    number = 0
                )
            )
        }

        engineState.startGame()
    }

    LaunchedEffect(engineState.level) {
        initGame()
    }

    fun launchPin() {
        if (engineState.playState != GamePlayState.PLAYING) return
        if (isLaunching || remainingPinsToLaunch <= 0) return

        isLaunching = true
        launchProgressY = 1.0f // 1.0 = bottom start, 0.0 = wheel contact point
        triggerGameHaptic(context, GameHapticType.LIGHT)
        SfxManager.playTap(context)
    }

    CanvasGameLoop(engineState = engineState) { deltaSec ->
        particles.update(deltaSec)

        if (engineState.playState == GamePlayState.PLAYING) {
            // Rotation speed scales with level
            val rotationSpeed = 80f + (engineState.level * 25f)

            // Periodically reverse direction on higher levels
            if (engineState.level >= 2) {
                directionTimer += deltaSec
                if (directionTimer >= 3.5f - (engineState.level * 0.3f)) {
                    directionTimer = 0f
                    directionMultiplier *= -1f
                }
            }

            wheelAngle = (wheelAngle + rotationSpeed * directionMultiplier * deltaSec) % 360f

            // Handle active pin launch motion
            if (isLaunching) {
                launchProgressY -= 5.0f * deltaSec // Fast travel upward

                if (launchProgressY <= 0f) {
                    launchProgressY = 0f

                    // Calculate impact angle on central wheel relative to current rotation
                    // Bottom point in Canvas world space is 90° (0° = right, 90° = down)
                    val hitAngle = normalizeAngle(90f - wheelAngle)

                    // Check collision with all attached pins
                    var collision = false
                    val minAngleThreshold = 14f // degrees

                    for (pin in attachedPins) {
                        if (angularDifference(hitAngle, pin.attachedAngleDeg) < minAngleThreshold) {
                            collision = true
                            break
                        }
                    }

                    val wheelCenter = Offset(240f, 260f) // Canvas relative coordinates
                    val impactPoint = Offset(
                        wheelCenter.x + (90f + 65f) * cos(Math.toRadians((wheelAngle + hitAngle).toDouble())).toFloat(),
                        wheelCenter.y + (90f + 65f) * sin(Math.toRadians((wheelAngle + hitAngle).toDouble())).toFloat()
                    )

                    if (collision) {
                        isLaunching = false
                        particles.spawnBurst(impactPoint, count = 25)
                        triggerGameHaptic(context, GameHapticType.ERROR)
                        SfxManager.playWrong(context)

                        if (engineState.loseLife()) {
                            engineState.failLevel(context)
                        }
                    } else {
                        // Pin attached successfully
                        attachedPins.add(
                            PinOnWheel(
                                id = currentPinNumber,
                                attachedAngleDeg = hitAngle,
                                number = currentPinNumber
                            )
                        )

                        particles.spawnBurst(impactPoint, count = 12)
                        triggerGameHaptic(context, GameHapticType.SUCCESS)
                        SfxManager.playSuccess(context)

                        engineState.addScore(10)
                        currentPinNumber++
                        remainingPinsToLaunch--
                        isLaunching = false

                        if (remainingPinsToLaunch <= 0) {
                            engineState.completeLevel(context, 3)
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
                title = t("دائرة الهلال", "Crescent Pin Spinner"),
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
                    "انقر لإطلاق الدبوس في عجلة الهلال دون تصادم الدبابيس!",
                    "Tap to launch numbered pins into the spinning crescent wheel without touching existing pins!"
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
                        launchPin()
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height

                    val wheelCenter = Offset(canvasWidth / 2f, canvasHeight * 0.38f)
                    val wheelRadius = 80f
                    val stemLength = 65f
                    val pinHeadRadius = 14f

                    val launchStartY = canvasHeight * 0.85f
                    val launchTargetY = wheelCenter.y + wheelRadius + stemLength + pinHeadRadius
                    val currentLaunchY = launchTargetY + (launchStartY - launchTargetY) * launchProgressY

                    // Draw Central Wheel Outer Glow & Ring
                    drawCircle(
                        color = theme.accent.copy(alpha = 0.2f),
                        radius = wheelRadius + 12f,
                        center = wheelCenter
                    )

                    drawCircle(
                        color = theme.accent,
                        radius = wheelRadius,
                        center = wheelCenter
                    )

                    drawCircle(
                        color = Color.White.copy(alpha = 0.8f),
                        radius = wheelRadius,
                        center = wheelCenter,
                        style = Stroke(width = 4f)
                    )

                    // Draw Crescent Moon Motif inside Wheel
                    val moonPath = Path().apply {
                        addOval(
                            androidx.compose.ui.geometry.Rect(
                                center = wheelCenter + Offset(-8f, -4f),
                                radius = wheelRadius * 0.55f
                            )
                        )
                    }
                    val cutoutPath = Path().apply {
                        addOval(
                            androidx.compose.ui.geometry.Rect(
                                center = wheelCenter + Offset(12f, -12f),
                                radius = wheelRadius * 0.50f
                            )
                        )
                    }

                    drawPath(
                        path = moonPath,
                        color = Color(0xFFFFD700)
                    )
                    drawPath(
                        path = cutoutPath,
                        color = theme.accent
                    )

                    // Wheel Center Hub
                    drawCircle(
                        color = Color.White,
                        radius = 16f,
                        center = wheelCenter
                    )

                    // Draw Attached Pins on Wheel
                    attachedPins.forEach { pin ->
                        val currentWorldAngleDeg = (pin.attachedAngleDeg + wheelAngle) % 360f
                        val rad = Math.toRadians(currentWorldAngleDeg.toDouble())

                        val contactPoint = Offset(
                            wheelCenter.x + wheelRadius * cos(rad).toFloat(),
                            wheelCenter.y + wheelRadius * sin(rad).toFloat()
                        )

                        val pinHeadCenter = Offset(
                            wheelCenter.x + (wheelRadius + stemLength) * cos(rad).toFloat(),
                            wheelCenter.y + (wheelRadius + stemLength) * sin(rad).toFloat()
                        )

                        // Stem line
                        drawLine(
                            color = Color.White,
                            start = contactPoint,
                            end = pinHeadCenter,
                            strokeWidth = 3.5f
                        )

                        // Pin head
                        val isInitialPin = pin.number == 0
                        val headColor = if (isInitialPin) Color(0xFFFF5252) else theme.accent

                        drawCircle(
                            color = headColor,
                            radius = pinHeadRadius,
                            center = pinHeadCenter
                        )
                        drawCircle(
                            color = Color.White,
                            radius = pinHeadRadius,
                            center = pinHeadCenter,
                            style = Stroke(width = 2f)
                        )

                        if (!isInitialPin) {
                            val numStr = pin.number.toArabicDigits()
                            val textLayout = textMeasurer.measure(
                                text = numStr,
                                style = TextStyle(
                                    color = Color.Black,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            drawText(
                                textLayoutResult = textLayout,
                                topLeft = Offset(
                                    pinHeadCenter.x - textLayout.size.width / 2f,
                                    pinHeadCenter.y - textLayout.size.height / 2f
                                )
                            )
                        }
                    }

                    // Draw Pin in Launch Flight
                    if (isLaunching) {
                        val launchHeadCenter = Offset(wheelCenter.x, currentLaunchY)
                        val launchStemBottom = Offset(wheelCenter.x, currentLaunchY + stemLength)

                        drawLine(
                            color = Color.White,
                            start = launchHeadCenter,
                            end = launchStemBottom,
                            strokeWidth = 3.5f
                        )

                        drawCircle(
                            color = Color(0xFFFFD700),
                            radius = pinHeadRadius,
                            center = launchHeadCenter
                        )

                        val numStr = currentPinNumber.toArabicDigits()
                        val textLayout = textMeasurer.measure(
                            text = numStr,
                            style = TextStyle(
                                color = Color.Black,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        drawText(
                            textLayoutResult = textLayout,
                            topLeft = Offset(
                                launchHeadCenter.x - textLayout.size.width / 2f,
                                launchHeadCenter.y - textLayout.size.height / 2f
                            )
                        )
                    }

                    // Draw Launch Launcher Station at Bottom
                    val launcherCenter = Offset(wheelCenter.x, launchStartY)
                    if (!isLaunching && remainingPinsToLaunch > 0) {
                        drawLine(
                            color = Color.White.copy(alpha = 0.6f),
                            start = launcherCenter,
                            end = launcherCenter + Offset(0f, stemLength),
                            strokeWidth = 3.5f
                        )

                        drawCircle(
                            color = theme.accent,
                            radius = pinHeadRadius,
                            center = launcherCenter
                        )

                        val numStr = currentPinNumber.toArabicDigits()
                        val textLayout = textMeasurer.measure(
                            text = numStr,
                            style = TextStyle(
                                color = Color.Black,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        drawText(
                            textLayoutResult = textLayout,
                            topLeft = Offset(
                                launcherCenter.x - textLayout.size.width / 2f,
                                launcherCenter.y - textLayout.size.height / 2f
                            )
                        )
                    }

                    // Draw Remaining Pins Counter Queue below launcher
                    val queueText = t(
                        "المتبقي: ${remainingPinsToLaunch.toArabicDigits()}",
                        "Remaining: $remainingPinsToLaunch"
                    )
                    val queueLayout = textMeasurer.measure(
                        text = queueText,
                        style = TextStyle(
                            color = theme.textPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    drawText(
                        textLayoutResult = queueLayout,
                        topLeft = Offset(
                            wheelCenter.x - queueLayout.size.width / 2f,
                            launchStartY + stemLength + 16f
                        )
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
