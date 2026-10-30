package com.elhajri.noor.games
import androidx.compose.ui.layout.onSizeChanged

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import kotlin.math.hypot
import kotlin.random.Random

/**
 * Game 46: Constellation Connect (نجوم السماء)
 * Category: أطفال
 * Mechanic: Connect-the-dots star tracer. Drag finger from star 1 to star 2, 3...
 * in numbered order to outline and light up ancient sky constellations.
 */
private data class StarNode(
    val id: Int,
    val sequenceNum: Int,
    val xRatio: Float,
    val yRatio: Float,
    var isConnected: Boolean = false
)

private data class ConstellationInfo(
    val titleAr: String,
    val titleEn: String,
    val stars: List<StarNode>
)

@Composable
fun ConnectConstellationGame(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val theme = NoorThemeState.active
    val engineState = rememberGameEngineState(initialLevel = 1, initialLives = 3)
    var canvasSize by remember { mutableStateOf(Size.Zero) }
    val textMeasurer = rememberTextMeasurer()
    val particles = remember { ParticleSystem() }

    val constellations = remember {
        listOf(
            ConstellationInfo(
                "كوكبة الهلال", "Crescent Constellation",
                listOf(
                    StarNode(1, 1, 0.50f, 0.20f, true),
                    StarNode(2, 2, 0.25f, 0.35f, false),
                    StarNode(3, 3, 0.20f, 0.60f, false),
                    StarNode(4, 4, 0.45f, 0.80f, false),
                    StarNode(5, 5, 0.70f, 0.65f, false)
                )
            ),
            ConstellationInfo(
                "كوكبة الفانوس", "Lantern Constellation",
                listOf(
                    StarNode(1, 1, 0.50f, 0.15f, true),
                    StarNode(2, 2, 0.25f, 0.35f, false),
                    StarNode(3, 3, 0.25f, 0.70f, false),
                    StarNode(4, 4, 0.50f, 0.85f, false),
                    StarNode(5, 5, 0.75f, 0.70f, false),
                    StarNode(6, 6, 0.75f, 0.35f, false)
                )
            ),
            ConstellationInfo(
                "كوكبة الجمل", "Camel Constellation",
                listOf(
                    StarNode(1, 1, 0.15f, 0.60f, true),
                    StarNode(2, 2, 0.30f, 0.30f, false),
                    StarNode(3, 3, 0.45f, 0.20f, false),
                    StarNode(4, 4, 0.60f, 0.35f, false),
                    StarNode(5, 5, 0.85f, 0.50f, false),
                    StarNode(6, 6, 0.70f, 0.80f, false),
                    StarNode(7, 7, 0.35f, 0.80f, false)
                )
            )
        )
    }

    val currentConstellation = constellations[(engineState.level - 1) % constellations.size]
    var nodes by remember(engineState.level) {
        mutableStateOf(currentConstellation.stars.map { it.copy() })
    }

    var activeDragPos by remember { mutableStateOf<Offset?>(null) }
    var currentNextIndex by remember(engineState.level) { mutableIntStateOf(2) }

    fun initGame() {
        nodes = currentConstellation.stars.map { it.copy() }
        currentNextIndex = 2
        activeDragPos = null
        particles.clear()
        engineState.startGame()
    }

    LaunchedEffect(engineState.level) {
        initGame()
    }

    fun handleDrag(currentPos: Offset, canvasSize: Size) {
        if (engineState.playState != GamePlayState.PLAYING) return
        activeDragPos = currentPos

        val w = canvasSize.width
        val h = canvasSize.height

        val targetNode = nodes.find { it.sequenceNum == currentNextIndex } ?: return
        val targetX = targetNode.xRatio * w
        val targetY = targetNode.yRatio * h

        val dist = hypot(currentPos.x - targetX, currentPos.y - targetY)
        if (dist <= 60f) { // Snap threshold
            nodes = nodes.map {
                if (it.sequenceNum == currentNextIndex) it.copy(isConnected = true) else it
            }
            triggerGameHaptic(context, GameHapticType.SUCCESS)
            SfxManager.playSuccess(context)
            particles.spawnBurst(Offset(targetX, targetY), count = 20, colors = listOf(Color(0xFFFFD700), Color.White, Color(0xFF00E676)))
            engineState.addScore(25)

            currentNextIndex++

            if (nodes.all { it.isConnected }) {
                activeDragPos = null
                engineState.completeLevel(context, 3)
            }
        }
    }

    CanvasGameLoop(engineState = engineState) { deltaSec ->
        particles.update(deltaSec)
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
                title = t("نجوم السماء", "Constellation Connect"),
                level = engineState.level,
                score = engineState.score,
                lives = engineState.lives,
                onBack = onBack,
                onPause = { engineState.pauseGame() }
            )

            // Constellation title bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .noorGlassCard(cornerRadius = 16.dp),
                color = Color.Transparent
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = t(currentConstellation.titleAr, currentConstellation.titleEn),
                        color = theme.accent,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = t("صل النجوم بالترتيب 1 ⬅ 2 ⬅ 3", "Connect stars in order 1 ➡ 2 ➡ 3"),
                        color = theme.textSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            // Sky Canvas Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp)
                    .noorGlassCard(cornerRadius = 24.dp)
                    .border(2.dp, theme.accent.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .onSizeChanged { canvasSize = Size(it.width.toFloat(), it.height.toFloat()) }
                        .gameGestureDetector(
                            onDragMove = { currentPos, _ ->
                                handleDrag(currentPos, canvasSize = canvasSize)
                            },
                            onDragEnd = {
                                activeDragPos = null
                            }
                        )
                ) {
                    val w = size.width
                    val h = size.height

                    // Dark night sky canvas background
                    drawRoundRect(Color(0xFF0B132B), cornerRadius = CornerRadius(24f))

                    // Draw background ambient twinkling stars
                    repeat(30) { i ->
                        val sx = (i * 37) % w.toInt()
                        val sy = (i * 71) % h.toInt()
                        drawCircle(Color.White.copy(alpha = 0.3f), radius = 2f, center = Offset(sx.toFloat(), sy.toFloat()))
                    }

                    // Draw connected constellation lines
                    val connectedNodes = nodes.filter { it.isConnected }.sortedBy { it.sequenceNum }
                    if (connectedNodes.size > 1) {
                        val path = Path()
                        val first = connectedNodes.first()
                        path.moveTo(first.xRatio * w, first.yRatio * h)
                        for (i in 1 until connectedNodes.size) {
                            val node = connectedNodes[i]
                            path.lineTo(node.xRatio * w, node.yRatio * h)
                        }
                        drawPath(
                            path = path,
                            color = Color(0xFFFFD700),
                            style = Stroke(width = 5f)
                        )
                    }

                    // Draw active drag line to touch position
                    val lastConnected = connectedNodes.lastOrNull()
                    val dragPos = activeDragPos
                    if (lastConnected != null && dragPos != null && !nodes.all { it.isConnected }) {
                        drawLine(
                            color = Color.White.copy(alpha = 0.8f),
                            start = Offset(lastConnected.xRatio * w, lastConnected.yRatio * h),
                            end = dragPos,
                            strokeWidth = 3f
                        )
                    }

                    // Draw Star Nodes
                    nodes.forEach { star ->
                        val cx = star.xRatio * w
                        val cy = star.yRatio * h
                        val isTarget = star.sequenceNum == currentNextIndex

                        if (star.isConnected) {
                            // Glowing golden star
                            drawCircle(Color(0xFFFFD700), radius = 16f, center = Offset(cx, cy))
                            drawCircle(Color.White, radius = 8f, center = Offset(cx, cy))
                        } else if (isTarget) {
                            // Pulsing target star
                            drawCircle(theme.accent.copy(alpha = 0.4f), radius = 24f, center = Offset(cx, cy))
                            drawCircle(theme.accent, radius = 14f, center = Offset(cx, cy))
                            drawCircle(Color.White, radius = 6f, center = Offset(cx, cy))
                        } else {
                            // Inactive star
                            drawCircle(Color.White.copy(alpha = 0.4f), radius = 12f, center = Offset(cx, cy))
                        }

                        // Star sequence number label
                        val numStr = star.sequenceNum.toArabicDigits()
                        val textLayout = textMeasurer.measure(
                            text = numStr,
                            style = TextStyle(
                                color = if (star.isConnected) Color.Black else Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        drawText(
                            textLayoutResult = textLayout,
                            topLeft = Offset(cx - textLayout.size.width / 2f, cy - textLayout.size.height / 2f)
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
