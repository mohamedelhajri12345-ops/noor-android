package com.elhajri.noor.games

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.audio.SfxManager
import com.elhajri.noor.games.engine.*
import com.elhajri.noor.settings.t
import com.elhajri.noor.settings.toArabicDigits
import com.elhajri.noor.theme.NoorThemeState
import com.elhajri.noor.ui.noorGlassCard
import com.elhajri.noor.ui.themeScreenBackground
import kotlin.math.sin
import kotlin.random.Random

/**
 * Pipe Node for Water Saver grid
 */
private data class WaterNode(
    val id: Int,
    val row: Int,
    val col: Int,
    var isValve: Boolean = false,
    var isLeaking: Boolean = false,
    var leakRate: Float = 0.5f, // Water loss per sec
    var center: Offset = Offset.Zero
)

/**
 * Game 51: Oasis Water Saver (ترشيد الماء)
 * Category: تربوية
 * Mechanic: Timed valve control puzzle tapping leaky pipe joints and turning valve handles to stop leaks and fill village reservoir.
 */
@Composable
fun WaterSaverGame(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val theme = NoorThemeState.active
    val textMeasurer = rememberTextMeasurer()
    val engineState = rememberGameEngineState(initialLevel = 1, initialLives = -1, timeLimitMs = 40000L, targetScore = 100)

    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    val particles = remember { ParticleSystem() }

    val gridRows = 4
    val gridCols = 4
    val nodes = remember { mutableStateListOf<WaterNode>() }

    var reservoirWaterPct by remember { mutableFloatStateOf(100f) } // 0% to 100%
    var totalLeakPressure by remember { mutableFloatStateOf(0f) }
    var leakSpawnTimerSec by remember { mutableFloatStateOf(0f) }

    fun initGrid() {
        nodes.clear()
        var nodeCount = 0
        for (r in 0 until gridRows) {
            for (c in 0 until gridCols) {
                val isV = (r + c) % 2 == 1
                nodes.add(
                    WaterNode(
                        id = ++nodeCount,
                        row = r,
                        col = c,
                        isValve = isV,
                        isLeaking = false,
                        leakRate = if (isV) 1.5f else 1.0f
                    )
                )
            }
        }
        // Start with 2 initial leaks
        nodes.shuffled().take(2).forEach { it.isLeaking = true }
    }

    fun resetGame() {
        reservoirWaterPct = 100f
        totalLeakPressure = 0f
        leakSpawnTimerSec = 0f
        initGrid()
        engineState.startGame()
    }

    LaunchedEffect(Unit) {
        resetGame()
    }

    // 60 FPS Game Loop
    CanvasGameLoop(engineState = engineState) { deltaSec ->
        particles.update(deltaSec)

        if (engineState.playState != GamePlayState.PLAYING) return@CanvasGameLoop

        // Calculate total active leaks
        val activeLeaks = nodes.filter { it.isLeaking }
        totalLeakPressure = activeLeaks.sumOf { it.leakRate.toDouble() }.toFloat()

        // Drain reservoir based on leak rate
        if (activeLeaks.isNotEmpty()) {
            reservoirWaterPct = (reservoirWaterPct - totalLeakPressure * 1.8f * deltaSec).coerceIn(0f, 100f)

            // Spawn water spray particles from active leaking nodes
            activeLeaks.forEach { node ->
                if (node.center != Offset.Zero && Random.nextFloat() < 0.3f) {
                    particles.spawnBurst(
                        origin = node.center,
                        count = 3,
                        colors = listOf(Color(0xFF29B6F6), Color(0xFF80D8FF), Color.White),
                        minSpeed = 50f,
                        maxSpeed = 150f
                    )
                }
            }
        } else {
            // Reward bonus points when zero leaks exist
            engineState.addScore(1)
        }

        // Fail game if water reservoir drains completely
        if (reservoirWaterPct <= 0f) {
            engineState.failLevel(context)
            return@CanvasGameLoop
        }

        // Periodic new leak spring
        leakSpawnTimerSec += deltaSec
        if (leakSpawnTimerSec >= 2.5f) {
            leakSpawnTimerSec = 0f
            val nonLeaking = nodes.filter { !it.isLeaking }
            if (nonLeaking.isNotEmpty()) {
                nonLeaking.random().isLeaking = true
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar
            GameHeaderBar(
                title = t("ترشيد الماء", "Oasis Water Saver"),
                level = engineState.level,
                score = engineState.score,
                timeRemainingMs = engineState.timeRemainingMs,
                onBack = onBack,
                onPause = { engineState.pauseGame() }
            )

            // Reservoir Water Level Bar & Pressure Gauge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .noorGlassCard(cornerRadius = 12.dp)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Water Reservoir Gauge
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = t("مخزون القرية من الماء", "Village Water Reservoir"),
                            color = theme.textPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${reservoirWaterPct.toInt().toArabicDigits()}%",
                            color = if (reservoirWaterPct > 30f) Color(0xFF29B6F6) else Color(0xFFFF5252),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { reservoirWaterPct / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp),
                        color = if (reservoirWaterPct > 30f) Color(0xFF0288D1) else Color(0xFFFF5252),
                        trackColor = Color.DarkGray
                    )
                }

                Spacer(Modifier.width(16.dp))

                // Pressure Gauge Warning Badge
                Box(
                    modifier = Modifier
                        .background(
                            if (totalLeakPressure > 3f) Color.Red.copy(alpha = 0.2f) else Color.Green.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (totalLeakPressure > 3f) t("ضغط عالٍ!", "High Pressure") else t("مستقر", "Stable"),
                        color = if (totalLeakPressure > 3f) Color(0xFFFF5252) else Color(0xFF66BB6A),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Main Pipe Network Canvas Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .noorGlassCard(cornerRadius = 16.dp)
                    .onSizeChanged { canvasSize = it }
                    .gameGestureDetector(
                        onTap = { tapPos ->
                            if (engineState.playState == GamePlayState.PLAYING) {
                                // Find tapped node
                                val tappedNode = nodes.minByOrNull {
                                    (it.center - tapPos).getDistanceSquared()
                                }
                                if (tappedNode != null && (tappedNode.center - tapPos).getDistance() < 70f) {
                                    if (tappedNode.isLeaking) {
                                        tappedNode.isLeaking = false
                                        engineState.addScore(15)
                                        SfxManager.playSuccess(context)
                                        triggerGameHaptic(context, GameHapticType.LIGHT)

                                        particles.spawnBurst(
                                            origin = tappedNode.center,
                                            count = 15,
                                            colors = listOf(Color(0xFF80D8FF), Color.White, Color(0xFFFFD700))
                                        )
                                    } else {
                                        SfxManager.playTap(context)
                                    }
                                }
                            }
                        }
                    )
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    if (canvasSize.width <= 0) return@Canvas

                    val width = size.width
                    val height = size.height

                    val cellW = width / gridCols
                    val cellH = height / gridRows

                    // Compute Node Centers
                    nodes.forEach { node ->
                        node.center = Offset(
                            node.col * cellW + cellW / 2f,
                            node.row * cellH + cellH / 2f
                        )
                    }

                    // Draw Pipe Lines Connecting Grid Nodes
                    nodes.forEach { node ->
                        // Connect Right
                        val rightNode = nodes.firstOrNull { it.row == node.row && it.col == node.col + 1 }
                        if (rightNode != null) {
                            drawLine(
                                color = Color(0xFF0288D1),
                                start = node.center,
                                end = rightNode.center,
                                strokeWidth = 12f,
                                cap = StrokeCap.Round
                            )
                        }
                        // Connect Down
                        val downNode = nodes.firstOrNull { it.row == node.row + 1 && it.col == node.col }
                        if (downNode != null) {
                            drawLine(
                                color = Color(0xFF0288D1),
                                start = node.center,
                                end = downNode.center,
                                strokeWidth = 12f,
                                cap = StrokeCap.Round
                            )
                        }
                    }

                    // Draw Valves & Joint Nodes
                    nodes.forEach { node ->
                        val radius = if (node.isValve) 28f else 22f

                        // Draw Base Node Circle
                        drawCircle(
                            color = if (node.isValve) Color(0xFFFFA726) else Color(0xFF78909C),
                            radius = radius,
                            center = node.center
                        )
                        drawCircle(
                            color = Color.White,
                            radius = radius,
                            center = node.center,
                            style = Stroke(width = 3f)
                        )

                        if (node.isValve) {
                            // Draw Valve Wheel Cross Detail
                            drawLine(
                                color = Color.Black,
                                start = Offset(node.center.x - 14f, node.center.y),
                                end = Offset(node.center.x + 14f, node.center.y),
                                strokeWidth = 4f
                            )
                            drawLine(
                                color = Color.Black,
                                start = Offset(node.center.x, node.center.y - 14f),
                                end = Offset(node.center.x, node.center.y + 14f),
                                strokeWidth = 4f
                            )
                        }

                        // Draw Water Leak Burst Warning Indicator
                        if (node.isLeaking) {
                            drawCircle(
                                color = Color.Red.copy(alpha = 0.4f),
                                radius = radius + 14f,
                                center = node.center
                            )
                            drawCircle(
                                color = Color.Red,
                                radius = radius + 14f,
                                center = node.center,
                                style = Stroke(width = 3f)
                            )

                            // Leak Text Label in Arabic
                            val textResult = textMeasurer.measure(
                                text = t("تسريب!", "Leak!"),
                                style = TextStyle(
                                    color = Color.Red,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            drawText(
                                textLayoutResult = textResult,
                                topLeft = Offset(
                                    node.center.x - textResult.size.width / 2f,
                                    node.center.y - radius - 22f
                                )
                            )
                        }
                    }

                    // Draw Particles
                    particles.draw(this)
                }
            }

            // Bottom Instructions
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = t("اضغط على الصمامات والوصلات لإغلاق التسريبات وحماية مخزون الماء", "Tap valves and joints to stop water leaks"),
                    color = theme.textSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Overlays
        when (engineState.playState) {
            GamePlayState.PAUSED -> {
                PauseOverlay(
                    onResume = { engineState.resumeGame() },
                    onRestart = { resetGame() },
                    onQuit = onBack
                )
            }
            GamePlayState.WON -> {
                GameWinOverlay(
                    score = engineState.score,
                    stars = engineState.starsEarned,
                    onNextLevel = {
                        engineState.nextLevel()
                        resetGame()
                    },
                    onRestart = { resetGame() },
                    onQuit = onBack
                )
            }
            GamePlayState.LOST -> {
                GameLoseOverlay(
                    score = engineState.score,
                    onRestart = { resetGame() },
                    onQuit = onBack
                )
            }
            else -> {}
        }
    }
}
