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
import com.elhajri.noor.ui.NavyCard
import com.elhajri.noor.ui.noorGlassCard
import com.elhajri.noor.ui.themeScreenBackground
import kotlinx.coroutines.delay
import kotlin.math.abs

/**
 * Game 10: Equilibrium Scale (ميزان التعادل)
 * Category: ألعاب ذكاء (Intelligence)
 * Mechanic: Physics balance scale puzzle. Drag brass weighted objects onto scale pans to balance the beam horizontally.
 */

private data class BrassWeight(
    val id: String,
    val value: Float, // in kg
    val labelAr: String,
    val color: Color = Gold
)

private data class LevelScaleConfig(
    val levelNumber: Int,
    val rightPanWeights: List<BrassWeight>, // Pre-placed weights on right pan
    val availableWeights: List<BrassWeight>, // Tray weights user can place
    val targetTolerance: Float = 0.1f // Max allowed torque difference
)

private val GAME_LEVELS = listOf(
    LevelScaleConfig(
        levelNumber = 1,
        rightPanWeights = listOf(BrassWeight("r1", 5f, "٥ كجم")),
        availableWeights = listOf(
            BrassWeight("w1", 1f, "١ كجم"),
            BrassWeight("w2", 2f, "٢ كجم"),
            BrassWeight("w3", 3f, "٣ كجم"),
            BrassWeight("w4", 5f, "٥ كجم")
        )
    ),
    LevelScaleConfig(
        levelNumber = 2,
        rightPanWeights = listOf(BrassWeight("r2", 7f, "٧ كجم")),
        availableWeights = listOf(
            BrassWeight("w5", 2f, "٢ كجم"),
            BrassWeight("w6", 3f, "٣ كجم"),
            BrassWeight("w7", 4f, "٤ كجم"),
            BrassWeight("w8", 5f, "٥ كجم")
        )
    ),
    LevelScaleConfig(
        levelNumber = 3,
        rightPanWeights = listOf(
            BrassWeight("r3_1", 3f, "٣ كجم"),
            BrassWeight("r3_2", 6f, "٦ كجم")
        ),
        availableWeights = listOf(
            BrassWeight("w9", 1f, "١ كجم"),
            BrassWeight("w10", 2f, "٢ كجم"),
            BrassWeight("w11", 4f, "٤ كجم"),
            BrassWeight("w12", 5f, "٥ كجم")
        )
    ),
    LevelScaleConfig(
        levelNumber = 4,
        rightPanWeights = listOf(BrassWeight("r4", 3.5f, "٣٫٥ كجم")),
        availableWeights = listOf(
            BrassWeight("w13", 0.5f, "٠٫٥ كجم"),
            BrassWeight("w14", 1f, "١ كجم"),
            BrassWeight("w15", 2f, "٢ كجم"),
            BrassWeight("w16", 3f, "٣ كجم")
        )
    ),
    LevelScaleConfig(
        levelNumber = 5,
        rightPanWeights = listOf(
            BrassWeight("r5_1", 2.5f, "٢٫٥ كجم"),
            BrassWeight("r5_2", 8f, "٨ كجم")
        ),
        availableWeights = listOf(
            BrassWeight("w17", 0.5f, "٠٫٥ كجم"),
            BrassWeight("w18", 2f, "٢ كجم"),
            BrassWeight("w19", 3f, "٣ كجم"),
            BrassWeight("w20", 5f, "٥ كجم")
        )
    )
)

@Composable
fun ScalesOfDeedsGame(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val theme = NoorThemeState.active
    val engineState = rememberGameEngineState(initialLevel = 1, initialLives = -1, targetScore = 100)

    val currentLevelConfig = remember(engineState.level) {
        val idx = (engineState.level - 1) % GAME_LEVELS.size
        GAME_LEVELS[idx]
    }

    // State of placed weights
    val leftPanWeights = remember { mutableStateListOf<BrassWeight>() }
    val rightPanWeights = remember { mutableStateListOf<BrassWeight>() }
    val trayWeights = remember { mutableStateListOf<BrassWeight>() }

    // Currently dragged weight
    var draggedWeight by remember { mutableStateOf<BrassWeight?>(null) }
    var dragPos by remember { mutableStateOf(Offset.Zero) }

    // Smooth animation angle for beam tilt
    var currentBeamAngle by remember { mutableFloatStateOf(0f) }
    val particles = remember { ParticleSystem() }
    var isBalanced by remember { mutableStateOf(false) }
    var balanceTimerMs by remember { mutableLongStateOf(0L) }

    // Calculate total weights and target torque
    val totalLeftWeight = leftPanWeights.sumOf { it.value.toDouble() }.toFloat()
    val totalRightWeight = rightPanWeights.sumOf { it.value.toDouble() }.toFloat()
    val weightDiff = totalRightWeight - totalLeftWeight

    // Target beam angle: clamped between -20 deg and +20 deg
    val targetBeamAngle = (weightDiff * 4f).coerceIn(-22f, 22f)

    // Load initial weights for level
    fun initLevel() {
        leftPanWeights.clear()
        rightPanWeights.clear()
        trayWeights.clear()

        rightPanWeights.addAll(currentLevelConfig.rightPanWeights)
        trayWeights.addAll(currentLevelConfig.availableWeights)

        isBalanced = false
        balanceTimerMs = 0L
        currentBeamAngle = (currentLevelConfig.rightPanWeights.sumOf { it.value.toDouble() } * 4.0).toFloat().coerceIn(-22f, 22f)
        engineState.startGame()
    }

    LaunchedEffect(engineState.level) {
        initLevel()
    }

    // 60 FPS physics loop for smooth tilt and balance check
    CanvasGameLoop(engineState) { deltaSec ->
        // Smoothly interpolate currentBeamAngle toward targetBeamAngle
        val angleStep = (targetBeamAngle - currentBeamAngle) * (deltaSec * 6f)
        currentBeamAngle += angleStep

        // Particle system update
        particles.update(deltaSec)

        // Check equilibrium tolerance
        val isCurrentlyEqual = abs(totalLeftWeight - totalRightWeight) <= currentLevelConfig.targetTolerance && leftPanWeights.isNotEmpty()
        if (isCurrentlyEqual) {
            balanceTimerMs += (deltaSec * 1000).toLong()
            if (!isBalanced && balanceTimerMs >= 800L) {
                isBalanced = true
                particles.spawnBurst(Offset(300f, 250f), count = 45)
                SfxManager.playSuccess(context)
                engineState.addScore(100)
                engineState.completeLevel(context, 3)
            }
        } else {
            balanceTimerMs = 0L
            isBalanced = false
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
                title = t("ميزان التعادل", "Equilibrium Scale"),
                level = engineState.level,
                score = engineState.score,
                onBack = onBack,
                onPause = { engineState.pauseGame() }
            )

            // Instruction Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .noorGlassCard(cornerRadius = 12.dp),
                color = Color.Transparent
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = t("اسحب الأوزان النحاسية إلى كفة الميزان اليسرى لتحقيق التوازن", "Drag brass weights to the left scale pan to balance"),
                            color = theme.textPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = t(
                                "وزن الكفة اليمنى: ${totalRightWeight.toString().toArabicDigits()} كجم | الكفة اليسرى: ${totalLeftWeight.toString().toArabicDigits()} كجم",
                                "Right Pan: $totalRightWeight kg | Left Pan: $totalLeftWeight kg"
                            ),
                            color = theme.accent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Balance status indicator LED
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (abs(totalLeftWeight - totalRightWeight) <= currentLevelConfig.targetTolerance && leftPanWeights.isNotEmpty()) Color(0xFF00E676) else Color(0xFFFF5252))
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = if (isBalanced) Icons.Default.Check else Icons.Default.Scale,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Main Interactive Scale Canvas Area
            var canvasSize by remember { mutableStateOf(IntSize.Zero) }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(12.dp)
                    .onSizeChanged { canvasSize = it }
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { startOffset ->
                                // Find weight clicked in tray or pans
                                val wWidth = 70f
                                val trayY = canvasSize.height - 90f

                                // Check tray items
                                val itemIndex = ((startOffset.x - 20f) / (wWidth + 15f)).toInt()
                                if (startOffset.y >= trayY - 40f && itemIndex in trayWeights.indices) {
                                    draggedWeight = trayWeights.removeAt(itemIndex)
                                    dragPos = startOffset
                                    SfxManager.playTap(context)
                                } else if (startOffset.x < canvasSize.width / 2f && leftPanWeights.isNotEmpty()) {
                                    // Remove top weight from left pan if clicked on left side
                                    draggedWeight = leftPanWeights.removeAt(leftPanWeights.lastIndex)
                                    dragPos = startOffset
                                    SfxManager.playTap(context)
                                }
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                dragPos = dragPos + dragAmount
                            },
                            onDragEnd = {
                                val weight = draggedWeight
                                if (weight != null) {
                                    // Check if dropped near left pan region
                                    val leftPanX = canvasSize.width * 0.22f
                                    val panY = canvasSize.height * 0.48f

                                    if (abs(dragPos.x - leftPanX) < 140f && abs(dragPos.y - panY) < 180f) {
                                        leftPanWeights.add(weight)
                                        SfxManager.playTap(context)
                                        triggerGameHaptic(context, GameHapticType.MEDIUM)
                                    } else {
                                        // Return to tray
                                        trayWeights.add(weight)
                                    }
                                    draggedWeight = null
                                }
                            },
                            onDragCancel = {
                                draggedWeight?.let { trayWeights.add(it) }
                                draggedWeight = null
                            }
                        )
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val centerX = width / 2f
                    val fulcrumY = height * 0.42f
                    val beamLength = width * 0.65f

                    // 1. Draw Stand/Fulcrum Pillars (Islamic Arch Motif)
                    drawFulcrumPillar(centerX, fulcrumY, height * 0.4f, theme.accent, GoldSoft)

                    // 2. Beam rotation around fulcrum (centerX, fulcrumY)
                    rotate(degrees = currentBeamAngle, pivot = Offset(centerX, fulcrumY)) {
                        val leftX = centerX - beamLength / 2f
                        val rightX = centerX + beamLength / 2f

                        // Draw Horizontal Scale Beam
                        drawLine(
                            color = Gold,
                            start = Offset(leftX, fulcrumY),
                            end = Offset(rightX, fulcrumY),
                            strokeWidth = 10f,
                            cap = StrokeCap.Round
                        )

                        // Central Pivot Star Ornament
                        drawCircle(color = GoldSoft, radius = 16f, center = Offset(centerX, fulcrumY))
                        drawCircle(color = theme.accent, radius = 8f, center = Offset(centerX, fulcrumY))

                        // Left Hanging Chains and Pan
                        val panDropHeight = 130f
                        val leftPanCenter = Offset(leftX, fulcrumY + panDropHeight)
                        drawHangingPan(leftX, fulcrumY, leftPanCenter, leftPanWeights, isLeft = true, Gold)

                        // Right Hanging Chains and Pan
                        val rightPanCenter = Offset(rightX, fulcrumY + panDropHeight)
                        drawHangingPan(rightX, fulcrumY, rightPanCenter, rightPanWeights, isLeft = false, Gold)
                    }

                    // 3. Draw Particles
                    particles.draw(this)

                    // 4. Draw Dragged Weight overlay if active
                    draggedWeight?.let { weight ->
                        drawBrassWeightBody(dragPos, weight.value, weight.labelAr, isHighlighted = true)
                    }
                }
            }

            // Weight Tray at Bottom
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .noorGlassCard(cornerRadius = 16.dp),
                color = Color.Transparent
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = t("مخزن الأوزان المتاحة", "Available Brass Weights"),
                        color = theme.textSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (trayWeights.isEmpty()) {
                            Text(
                                text = t("تم وضع جميع الأوزان", "All weights placed"),
                                color = theme.textSecondary,
                                fontSize = 12.sp
                            )
                        } else {
                            trayWeights.forEach { weight ->
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Gold.copy(alpha = 0.2f))
                                        .border(1.5.dp, Gold, RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.Scale, contentDescription = null, tint = Gold, modifier = Modifier.size(16.dp))
                                        Text(
                                            text = weight.labelAr,
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Win and Pause Overlays
        if (engineState.playState == GamePlayState.PAUSED) {
            PauseOverlay(
                onResume = { engineState.resumeGame() },
                onRestart = { initLevel() },
                onQuit = onBack
            )
        }

        if (engineState.playState == GamePlayState.WON) {
            GameWinOverlay(
                score = engineState.score,
                stars = engineState.starsEarned,
                onNextLevel = {
                    if (engineState.level < GAME_LEVELS.size) {
                        engineState.nextLevel()
                    } else {
                        onBack()
                    }
                },
                onRestart = { initLevel() },
                onQuit = onBack
            )
        }
    }
}

private fun DrawScope.drawFulcrumPillar(
    centerX: Float,
    fulcrumY: Float,
    pillarHeight: Float,
    accentColor: Color,
    goldColor: Color
) {
    val path = Path().apply {
        moveTo(centerX - 25f, fulcrumY + pillarHeight)
        lineTo(centerX - 10f, fulcrumY)
        lineTo(centerX + 10f, fulcrumY)
        lineTo(centerX + 25f, fulcrumY + pillarHeight)
        close()
    }
    drawPath(path, color = goldColor.copy(alpha = 0.8f))
    drawPath(path, color = accentColor, style = Stroke(width = 3f))

    // Pillar Base
    drawRoundRect(
        color = goldColor,
        topLeft = Offset(centerX - 45f, fulcrumY + pillarHeight - 12f),
        size = Size(90f, 16f),
        cornerRadius = CornerRadius(6f)
    )
}

private fun DrawScope.drawHangingPan(
    anchorX: Float,
    anchorY: Float,
    panCenter: Offset,
    weights: List<BrassWeight>,
    isLeft: Boolean,
    goldColor: Color
) {
    // Chain lines
    drawLine(color = Color.LightGray.copy(alpha = 0.7f), start = Offset(anchorX, anchorY), end = Offset(panCenter.x - 30f, panCenter.y), strokeWidth = 2f)
    drawLine(color = Color.LightGray.copy(alpha = 0.7f), start = Offset(anchorX, anchorY), end = Offset(panCenter.x + 30f, panCenter.y), strokeWidth = 2f)

    // Pan Bowl
    drawArc(
        color = goldColor,
        startAngle = 0f,
        sweepAngle = 180f,
        useCenter = true,
        topLeft = Offset(panCenter.x - 40f, panCenter.y - 10f),
        size = Size(80f, 30f)
    )

    // Stack placed weights inside pan bowl
    weights.forEachIndexed { idx, w ->
        val weightPos = Offset(panCenter.x, panCenter.y - 12f - (idx * 22f))
        drawBrassWeightBody(weightPos, w.value, w.labelAr, isHighlighted = false)
    }
}

private fun DrawScope.drawBrassWeightBody(
    center: Offset,
    value: Float,
    labelAr: String,
    isHighlighted: Boolean
) {
    val radius = 18f + (value * 2f).coerceAtMost(10f)
    val color = if (isHighlighted) Color(0xFFFFD700) else GoldSoft

    drawCircle(color = color, radius = radius, center = center)
    drawCircle(color = Color.Black.copy(alpha = 0.4f), radius = radius, center = center, style = Stroke(width = 2f))
    drawCircle(color = Gold, radius = radius * 0.4f, center = Offset(center.x, center.y - radius * 0.7f))
}
