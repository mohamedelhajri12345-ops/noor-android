package com.elhajri.noor.games
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput

import android.content.Context
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import kotlin.math.sqrt

/**
 * Game 53: Oasis Bridge Architect (جسر المحبة)
 * Category: تربوية (Educational)
 * Mechanic: Physics bridge building sandbox. Connect wooden beams and ropes across structural nodes to let the caravan cross the canyon.
 */

private data class BridgeNode(
    val id: Int,
    val posRatio: Offset, // (xRatio, yRatio) relative to canyon area
    val isFixed: Boolean = false,
    val isDeck: Boolean = false
)

private data class BeamMember(
    val nodeAId: Int,
    val nodeBId: Int,
    val cost: Int = 10,
    var stressRatio: Float = 0f, // 0.0 to 1.0+
    var isBroken: Boolean = false
)

private data class BridgeLevelData(
    val levelNumber: Int,
    val maxBudget: Int,
    val nodes: List<BridgeNode>,
    val caravanWeight: Float = 1.0f
)

private val OASIS_BRIDGE_LEVELS = listOf(
    BridgeLevelData(
        levelNumber = 1,
        maxBudget = 100,
        caravanWeight = 1.0f,
        nodes = listOf(
            // Left Cliff
            BridgeNode(0, Offset(0.08f, 0.40f), isFixed = true, isDeck = true),
            BridgeNode(1, Offset(0.08f, 0.65f), isFixed = true),
            // Middle deck nodes
            BridgeNode(2, Offset(0.35f, 0.40f), isDeck = true),
            BridgeNode(3, Offset(0.65f, 0.40f), isDeck = true),
            // Support bottom node
            BridgeNode(4, Offset(0.50f, 0.70f)),
            // Right Cliff
            BridgeNode(5, Offset(0.92f, 0.40f), isFixed = true, isDeck = true),
            BridgeNode(6, Offset(0.92f, 0.65f), isFixed = true)
        )
    ),
    BridgeLevelData(
        levelNumber = 2,
        maxBudget = 120,
        caravanWeight = 1.4f,
        nodes = listOf(
            BridgeNode(0, Offset(0.06f, 0.38f), isFixed = true, isDeck = true),
            BridgeNode(1, Offset(0.06f, 0.62f), isFixed = true),
            BridgeNode(2, Offset(0.28f, 0.38f), isDeck = true),
            BridgeNode(3, Offset(0.50f, 0.38f), isDeck = true),
            BridgeNode(4, Offset(0.72f, 0.38f), isDeck = true),
            BridgeNode(5, Offset(0.39f, 0.68f)),
            BridgeNode(6, Offset(0.61f, 0.68f)),
            BridgeNode(7, Offset(0.94f, 0.38f), isFixed = true, isDeck = true),
            BridgeNode(8, Offset(0.94f, 0.62f), isFixed = true)
        )
    ),
    BridgeLevelData(
        levelNumber = 3,
        maxBudget = 150,
        caravanWeight = 2.0f,
        nodes = listOf(
            BridgeNode(0, Offset(0.05f, 0.35f), isFixed = true, isDeck = true),
            BridgeNode(1, Offset(0.05f, 0.60f), isFixed = true),
            BridgeNode(2, Offset(0.25f, 0.35f), isDeck = true),
            BridgeNode(3, Offset(0.50f, 0.35f), isDeck = true),
            BridgeNode(4, Offset(0.75f, 0.35f), isDeck = true),
            BridgeNode(5, Offset(0.35f, 0.65f)),
            BridgeNode(6, Offset(0.50f, 0.82f), isFixed = true), // Island pillar anchor
            BridgeNode(7, Offset(0.65f, 0.65f)),
            BridgeNode(8, Offset(0.95f, 0.35f), isFixed = true, isDeck = true),
            BridgeNode(9, Offset(0.95f, 0.60f), isFixed = true)
        )
    )
)

@Composable
fun OasisBridgeGame(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val theme = NoorThemeState.active
    val engineState = rememberGameEngineState(initialLevel = 1, initialLives = -1, targetScore = 100)

    val levelData = remember(engineState.level) {
        val idx = (engineState.level - 1) % OASIS_BRIDGE_LEVELS.size
        OASIS_BRIDGE_LEVELS[idx]
    }

    val beams = remember { mutableStateListOf<BeamMember>() }
    var selectedNodeId by remember { mutableStateOf<Int?>(null) }
    var isTesting by remember { mutableStateOf(false) }
    var caravanProgress by remember { mutableFloatStateOf(0.0f) } // 0.0 to 1.0
    var isCollapsed by remember { mutableStateOf(false) }

    val particles = remember { ParticleSystem() }

    val usedBudget = beams.sumOf { it.cost }
    val remainingBudget = levelData.maxBudget - usedBudget

    fun initLevel() {
        beams.clear()
        selectedNodeId = null
        isTesting = false
        caravanProgress = 0.0f
        isCollapsed = false
        engineState.startGame()
    }

    LaunchedEffect(engineState.level) {
        initLevel()
    }

    // 60 FPS update loop for caravan movement and truss load solver
    CanvasGameLoop(engineState) { deltaSec ->
        particles.update(deltaSec)

        if (isTesting && !isCollapsed) {
            caravanProgress += deltaSec * 0.25f // 4 seconds crossing speed

            // Compute stress on all beams
            var maxMemberStress = 0f
            beams.forEach { beam ->
                val nodeA = levelData.nodes.find { it.id == beam.nodeAId }
                val nodeB = levelData.nodes.find { it.id == beam.nodeBId }

                if (nodeA != null && nodeB != null) {
                    // Distance from caravan position to beam center
                    val caravanX = 0.08f + caravanProgress * 0.84f
                    val beamCenterX = (nodeA.posRatio.x + nodeB.posRatio.x) / 2f
                    val distToCaravan = abs(caravanX - beamCenterX)

                    val loadImpact = (1.0f - distToCaravan.coerceIn(0f, 0.4f) / 0.4f) * levelData.caravanWeight
                    beam.stressRatio = (0.2f + loadImpact * 0.7f).coerceIn(0f, 1.2f)

                    if (beam.stressRatio > maxMemberStress) {
                        maxMemberStress = beam.stressRatio
                    }

                    if (beam.stressRatio > 1.0f && !beam.isBroken) {
                        beam.isBroken = true
                        isCollapsed = true
                        SfxManager.playWrong(context)
                        triggerGameHaptic(context, GameHapticType.ERROR)
                    }
                }
            }

            // Check victory condition
            if (caravanProgress >= 1.0f && !isCollapsed) {
                isTesting = false
                particles.spawnBurst(Offset(300f, 250f), count = 40)
                SfxManager.playSuccess(context)
                engineState.addScore(100 + remainingBudget)
                engineState.completeLevel(context, 3)
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
                title = t("جسر المحبة", "Oasis Bridge Architect"),
                level = engineState.level,
                score = engineState.score,
                onBack = onBack,
                onPause = { engineState.pauseGame() }
            )

            // Control Bar & Budget
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
                    Column {
                        Text(
                            text = t("الميزانية المتبقية", "Remaining Budget"),
                            color = theme.textSecondary,
                            fontSize = 11.sp
                        )
                        Text(
                            text = t("${remainingBudget.toArabicDigits()} عملة", "$remainingBudget coins"),
                            color = if (remainingBudget >= 0) Gold else Color(0xFFFF5252),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row {
                        // Clear Beams Button
                        OutlinedButton(
                            onClick = {
                                beams.clear()
                                isTesting = false
                                caravanProgress = 0f
                                isCollapsed = false
                                SfxManager.playTap(context)
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = theme.textPrimary, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(t("مسح", "Clear"), color = theme.textPrimary, fontSize = 12.sp)
                        }

                        // Test Bridge Button
                        Button(
                            onClick = {
                                if (beams.isNotEmpty() && !isTesting) {
                                    isTesting = true
                                    caravanProgress = 0f
                                    isCollapsed = false
                                    beams.forEach { it.isBroken = false; it.stressRatio = 0f }
                                    SfxManager.playWhoosh(context)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = if (isTesting) Color.Gray else theme.accent),
                            shape = RoundedCornerShape(10.dp),
                            enabled = beams.isNotEmpty() && !isTesting
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(t("اختبار الجسر", "Test"), color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Canyon Interactive Canvas Area
            var canvasSize by remember { mutableStateOf(IntSize.Zero) }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(12.dp)
                    .onSizeChanged { canvasSize = it }
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures { tapOffset ->
                                if (isTesting) return@detectTapGestures

                                // Find clicked node
                                val width = canvasSize.width.toFloat()
                                val height = canvasSize.height.toFloat()

                                val clickedNode = levelData.nodes.find { node ->
                                    val nx = node.posRatio.x * width
                                    val ny = node.posRatio.y * height
                                    val dist = sqrt((tapOffset.x - nx) * (tapOffset.x - nx) + (tapOffset.y - ny) * (tapOffset.y - ny))
                                    dist <= 35f
                                }

                                if (clickedNode != null) {
                                    val firstSelected = selectedNodeId
                                    if (firstSelected == null) {
                                        selectedNodeId = clickedNode.id
                                        SfxManager.playTap(context)
                                    } else {
                                        if (firstSelected != clickedNode.id) {
                                            // Check if beam already exists
                                            val existingIdx = beams.indexOfFirst {
                                                (it.nodeAId == firstSelected && it.nodeBId == clickedNode.id) ||
                                                        (it.nodeAId == clickedNode.id && it.nodeBId == firstSelected)
                                            }

                                            if (existingIdx >= 0) {
                                                beams.removeAt(existingIdx)
                                            } else if (remainingBudget >= 10) {
                                                beams.add(BeamMember(firstSelected, clickedNode.id, cost = 10))
                                                SfxManager.playTap(context)
                                                triggerGameHaptic(context, GameHapticType.LIGHT)
                                            }
                                        }
                                        selectedNodeId = null
                                    }
                                } else {
                                    selectedNodeId = null
                                }
                            }
                        }
                ) {
                    val width = size.width
                    val height = size.height

                    // 1. Draw Canyon Cliffs Background
                    drawCanyonBackground(width, height, theme.accent)

                    // 2. Draw Beams
                    beams.forEach { beam ->
                        val nodeA = levelData.nodes.find { it.id == beam.nodeAId }
                        val nodeB = levelData.nodes.find { it.id == beam.nodeBId }

                        if (nodeA != null && nodeB != null) {
                            val posA = Offset(nodeA.posRatio.x * width, nodeA.posRatio.y * height)
                            val posB = Offset(nodeB.posRatio.x * width, nodeB.posRatio.y * height)

                            val beamColor = when {
                                beam.isBroken -> Color.Transparent
                                beam.stressRatio > 0.8f -> Color(0xFFFF5252)
                                beam.stressRatio > 0.5f -> Color(0xFFFFAB00)
                                isTesting -> Color(0xFF00E676)
                                else -> GoldSoft
                            }

                            if (!beam.isBroken) {
                                drawLine(
                                    color = beamColor,
                                    start = posA,
                                    end = posB,
                                    strokeWidth = 8f,
                                    cap = StrokeCap.Round
                                )
                            }
                        }
                    }

                    // 3. Draw Nodes
                    levelData.nodes.forEach { node ->
                        val nx = node.posRatio.x * width
                        val ny = node.posRatio.y * height
                        val isSelected = selectedNodeId == node.id

                        val nodeColor = when {
                            node.isFixed -> Color(0xFF78909C)
                            isSelected -> Gold
                            else -> Color.White
                        }

                        drawCircle(color = nodeColor, radius = if (isSelected) 14f else 10f, center = Offset(nx, ny))
                        drawCircle(color = Color.Black, radius = if (isSelected) 14f else 10f, center = Offset(nx, ny), style = Stroke(width = 2f))
                    }

                    // 4. Draw Caravan (Camel)
                    if (isTesting) {
                        val caravanX = (0.08f + caravanProgress * 0.84f) * width
                        val caravanY = height * 0.36f + (if (isCollapsed) (caravanProgress * 150f) else 0f)

                        drawCaravanIcon(Offset(caravanX, caravanY), isCollapsed)
                    }

                    // 5. Draw Particles
                    particles.draw(this)
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
                    if (engineState.level < OASIS_BRIDGE_LEVELS.size) {
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

private fun DrawScope.drawCanyonBackground(width: Float, height: Float, accentColor: Color) {
    // Water at bottom
    drawRect(
        color = Color(0xFF0277BD).copy(alpha = 0.5f),
        topLeft = Offset(0f, height * 0.80f),
        size = Size(width, height * 0.20f)
    )

    // Left Cliff Path
    val leftCliff = Path().apply {
        moveTo(0f, 0f)
        lineTo(width * 0.12f, 0f)
        lineTo(width * 0.12f, height * 0.42f)
        lineTo(width * 0.06f, height * 0.85f)
        lineTo(0f, height * 0.85f)
        close()
    }
    drawPath(leftCliff, color = Color(0xFF5D4037))

    // Right Cliff Path
    val rightCliff = Path().apply {
        moveTo(width, 0f)
        lineTo(width * 0.88f, 0f)
        lineTo(width * 0.88f, height * 0.42f)
        lineTo(width * 0.94f, height * 0.85f)
        lineTo(width, height * 0.85f)
        close()
    }
    drawPath(rightCliff, color = Color(0xFF5D4037))
}

private fun DrawScope.drawCaravanIcon(center: Offset, isCollapsed: Boolean) {
    val bodyColor = if (isCollapsed) Color(0xFFFF5252) else Gold

    // Camel hump and body
    drawCircle(color = bodyColor, radius = 14f, center = Offset(center.x - 6f, center.y - 6f))
    drawCircle(color = bodyColor, radius = 10f, center = Offset(center.x + 8f, center.y - 10f))

    // Legs
    drawLine(color = bodyColor, start = Offset(center.x - 10f, center.y), end = Offset(center.x - 10f, center.y + 16f), strokeWidth = 3f)
    drawLine(color = bodyColor, start = Offset(center.x + 10f, center.y), end = Offset(center.x + 10f, center.y + 16f), strokeWidth = 3f)
}
