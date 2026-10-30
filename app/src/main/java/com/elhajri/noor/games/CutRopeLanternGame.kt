package com.elhajri.noor.games

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
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
import com.elhajri.noor.theme.NoorThemeState
import com.elhajri.noor.ui.noorGlassCard
import com.elhajri.noor.ui.themeScreenBackground
import kotlin.math.*

private fun Offset.length(): Float = getDistance()
private fun Offset.normalize(): Offset {
    val d = getDistance()
    return if (d > 0f) Offset(x / d, y / d) else Offset.Zero
}

private data class RopeNode(
    var pos: Offset,
    var oldPos: Offset
)

private data class GameRope(
    val id: Int,
    val anchorPos: Offset,
    var nodes: List<RopeNode>,
    val restLength: Float,
    var isCut: Boolean = false
)

private data class StarCollectible(
    val id: Int,
    val pos: Offset,
    var collected: Boolean = false
)

/**
 * Game 36: Pendant Rope Cut (مصباح الأمل)
 * Category: متعة (Fun)
 * Mechanic: Swipe finger across supporting ropes to cut them with Verlet physics, dropping
 * the swinging brass lantern past stars onto its pedestal.
 */
@Composable
fun CutRopeLanternGame(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val theme = NoorThemeState.active
    val engineState = rememberGameEngineState(initialLevel = 1, initialLives = 3)

    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    var lanternPos by remember { mutableStateOf(Offset.Zero) }
    var lanternOldPos by remember { mutableStateOf(Offset.Zero) }
    var isLanded by remember { mutableStateOf(false) }

    var ropes by remember { mutableStateOf<List<GameRope>>(emptyList()) }
    var stars by remember { mutableStateOf<List<StarCollectible>>(emptyList()) }
    var collectedStarsCount by remember { mutableIntStateOf(0) }

    val swipeTrail = remember { mutableStateListOf<Offset>() }
    val particles = remember { ParticleSystem() }

    fun initLevel(w: Float, h: Float) {
        if (w <= 0f || h <= 0f) return

        val lanternInit = when (engineState.level) {
            1 -> Offset(w * 0.5f, h * 0.35f)
            2 -> Offset(w * 0.4f, h * 0.35f)
            else -> Offset(w * 0.5f, h * 0.3f)
        }
        lanternPos = lanternInit
        lanternOldPos = lanternInit
        isLanded = false
        collectedStarsCount = 0
        swipeTrail.clear()
        particles.clear()

        // Create level ropes
        val ropeList = mutableListOf<GameRope>()
        when (engineState.level) {
            1 -> {
                val anchor = Offset(w * 0.5f, h * 0.1f)
                ropeList.add(createRope(0, anchor, lanternInit, nodeCount = 8))
            }
            2 -> {
                val anchor1 = Offset(w * 0.2f, h * 0.1f)
                val anchor2 = Offset(w * 0.8f, h * 0.1f)
                ropeList.add(createRope(0, anchor1, lanternInit, nodeCount = 8))
                ropeList.add(createRope(1, anchor2, lanternInit, nodeCount = 8))
            }
            else -> {
                val anchor1 = Offset(w * 0.15f, h * 0.12f)
                val anchor2 = Offset(w * 0.5f, h * 0.08f)
                val anchor3 = Offset(w * 0.85f, h * 0.12f)
                ropeList.add(createRope(0, anchor1, lanternInit, nodeCount = 8))
                ropeList.add(createRope(1, anchor2, lanternInit, nodeCount = 8))
                ropeList.add(createRope(2, anchor3, lanternInit, nodeCount = 8))
            }
        }
        ropes = ropeList

        // Create level collectibles
        val starList = mutableListOf<StarCollectible>()
        when (engineState.level) {
            1 -> {
                starList.add(StarCollectible(0, Offset(w * 0.5f, h * 0.52f)))
                starList.add(StarCollectible(1, Offset(w * 0.5f, h * 0.65f)))
                starList.add(StarCollectible(2, Offset(w * 0.5f, h * 0.78f)))
            }
            2 -> {
                starList.add(StarCollectible(0, Offset(w * 0.5f, h * 0.45f)))
                starList.add(StarCollectible(1, Offset(w * 0.62f, h * 0.62f)))
                starList.add(StarCollectible(2, Offset(w * 0.5f, h * 0.78f)))
            }
            else -> {
                starList.add(StarCollectible(0, Offset(w * 0.35f, h * 0.48f)))
                starList.add(StarCollectible(1, Offset(w * 0.65f, h * 0.58f)))
                starList.add(StarCollectible(2, Offset(w * 0.5f, h * 0.76f)))
            }
        }
        stars = starList

        engineState.startGame()
    }

    LaunchedEffect(engineState.level, canvasSize) {
        if (canvasSize.width > 0 && canvasSize.height > 0) {
            initLevel(canvasSize.width.toFloat(), canvasSize.height.toFloat())
        }
    }

    CanvasGameLoop(engineState) { deltaSec ->
        if (engineState.playState != GamePlayState.PLAYING) return@CanvasGameLoop

        particles.update(deltaSec)

        val w = canvasSize.width.toFloat()
        val h = canvasSize.height.toFloat()

        if (w > 0f && h > 0f && !isLanded) {
            // Verlet Physics step for Lantern body
            val gravity = 900f * deltaSec * deltaSec
            val vel = (lanternPos - lanternOldPos) * 0.98f
            val nextLanternPos = lanternPos + vel + Offset(0f, gravity)

            lanternOldPos = lanternPos
            lanternPos = nextLanternPos

            // Update attached active ropes and apply tension constraints
            ropes.forEach { rope ->
                if (!rope.isCut) {
                    val nodes = rope.nodes
                    // Update node positions under gravity
                    for (i in 1 until nodes.size) {
                        val node = nodes[i]
                        val nVel = (node.pos - node.oldPos) * 0.96f
                        val nextNodePos = node.pos + nVel + Offset(0f, gravity * 0.5f)
                        node.oldPos = node.pos
                        node.pos = nextNodePos
                    }

                    // Enforce node distance constraints
                    nodes[0].pos = rope.anchorPos
                    for (iter in 0 until 5) {
                        for (i in 0 until nodes.size - 1) {
                            val n1 = nodes[i]
                            val n2 = nodes[i + 1]
                            val delta = n2.pos - n1.pos
                            val dist = delta.length()
                            if (dist > rope.restLength) {
                                val norm = delta.normalize()
                                val diff = dist - rope.restLength
                                if (i == 0) {
                                    n2.pos -= norm * diff
                                } else {
                                    n1.pos += norm * (diff * 0.5f)
                                    n2.pos -= norm * (diff * 0.5f)
                                }
                            }
                        }
                    }

                    // Attach last node to lantern
                    val lastNode = nodes.last()
                    val ropeToLantern = lanternPos - lastNode.pos
                    val tensionDist = ropeToLantern.length()
                    if (tensionDist > 10f) {
                        val norm = ropeToLantern.normalize()
                        lanternPos -= norm * (tensionDist * 0.2f)
                    }
                }
            }

            // Check star collectibles collision
            val lanternRadius = 26f
            val updatedStars = stars.map { star ->
                if (!star.collected) {
                    val dist = (lanternPos - star.pos).length()
                    if (dist < lanternRadius + 18f) {
                        triggerGameHaptic(context, GameHapticType.LIGHT)
                        SfxManager.playTap(context)
                        engineState.addScore(30)
                        collectedStarsCount++
                        particles.spawnBurst(star.pos, count = 15, colors = listOf(Color(0xFFFFD700), Color.White))
                        star.copy(collected = true)
                    } else star
                } else star
            }
            if (updatedStars != stars) {
                stars = updatedStars
            }

            // Target Pedestal at bottom center
            val pedestalCenter = Offset(w * 0.5f, h * 0.88f)
            val pedestalBox = AABBBox(
                left = pedestalCenter.x - 50f,
                top = pedestalCenter.y - 15f,
                right = pedestalCenter.x + 50f,
                bottom = pedestalCenter.y + 20f
            )

            // Check landing on pedestal
            if (pedestalBox.contains(lanternPos) && (lanternPos.y - lanternOldPos.y) >= -2f) {
                isLanded = true
                triggerGameHaptic(context, GameHapticType.SUCCESS)
                particles.spawnBurst(pedestalCenter, count = 35, colors = listOf(Color(0xFF00E676), Color(0xFFFFD700), Color.White))

                val earnedStars = max(1, collectedStarsCount.coerceAtMost(3))
                engineState.completeLevel(context, earnedStars)
            }

            // Check fall into abyss
            if (lanternPos.y > h + 60f) {
                triggerGameHaptic(context, GameHapticType.ERROR)
                engineState.failLevel(context)
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
                title = t("مصباح الأمل", "Pendant Rope Cut"),
                level = engineState.level,
                score = engineState.score,
                onBack = onBack,
                onPause = { engineState.pauseGame() }
            )

            Text(
                text = t("اقطع الحبال بسحب الإصبع لإسقاط المصباح نحو القاعدة", "Swipe to cut ropes and guide the lantern onto the pedestal"),
                color = theme.textSecondary,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp)
                    .noorGlassCard(cornerRadius = 20.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .onSizeChanged { sz -> canvasSize = sz }
                        .gameGestureDetector(
                            onDragStart = { offset ->
                                swipeTrail.clear()
                                swipeTrail.add(offset)
                            },
                            onDragMove = { offset, _ ->
                                if (swipeTrail.isNotEmpty()) {
                                    val lastP = swipeTrail.last()
                                    swipeTrail.add(offset)

                                    // Check rope segment cuts
                                    ropes.forEach { rope ->
                                        if (!rope.isCut) {
                                            for (i in 0 until rope.nodes.size - 1) {
                                                val n1 = rope.nodes[i].pos
                                                val n2 = rope.nodes[i + 1].pos
                                                if (lineSegmentsIntersect(lastP, offset, n1, n2)) {
                                                    rope.isCut = true
                                                    triggerGameHaptic(context, GameHapticType.MEDIUM)
                                                    SfxManager.playTap(context)
                                                    particles.spawnBurst(
                                                        origin = Offset((n1.x + n2.x) / 2f, (n1.y + n2.y) / 2f),
                                                        count = 12,
                                                        colors = listOf(Color(0xFFFFD54F), Color.White)
                                                    )
                                                    break
                                                }
                                            }
                                        }
                                    }
                                }
                            },
                            onDragEnd = {
                                swipeTrail.clear()
                            }
                        )
                ) {
                    val w = size.width
                    val h = size.height

                    // Render Rope Anchors & Ropes
                    ropes.forEach { rope ->
                        // Draw Anchor
                        drawCircle(
                            color = Color(0xFF8D6E63),
                            radius = 12f,
                            center = rope.anchorPos
                        )
                        drawCircle(
                            color = Color(0xFFFFD700),
                            radius = 6f,
                            center = rope.anchorPos
                        )

                        if (!rope.isCut) {
                            val ropePath = Path().apply {
                                moveTo(rope.anchorPos.x, rope.anchorPos.y)
                                rope.nodes.forEach { node ->
                                    lineTo(node.pos.x, node.pos.y)
                                }
                                lineTo(lanternPos.x, lanternPos.y)
                            }
                            drawPath(
                                path = ropePath,
                                color = Color(0xFFD7CCC8),
                                style = Stroke(width = 6f, cap = StrokeCap.Round)
                            )
                        }
                    }

                    // Draw Collectible Stars
                    stars.forEach { star ->
                        if (!star.collected) {
                            drawCircle(
                                color = Color(0xFFFFD700).copy(alpha = 0.3f),
                                radius = 22f,
                                center = star.pos
                            )
                            drawCircle(
                                color = Color(0xFFFFD700),
                                radius = 14f,
                                center = star.pos
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 6f,
                                center = star.pos
                            )
                        }
                    }

                    // Draw Target Pedestal at Bottom
                    val pedestalCenter = Offset(w * 0.5f, h * 0.88f)
                    drawRoundRect(
                        color = Color(0xFF00E676).copy(alpha = 0.25f),
                        topLeft = Offset(pedestalCenter.x - 55f, pedestalCenter.y - 15f),
                        size = Size(110f, 30f),
                        cornerRadius = CornerRadius(12f)
                    )
                    drawRoundRect(
                        color = Color(0xFF00E676),
                        topLeft = Offset(pedestalCenter.x - 55f, pedestalCenter.y - 15f),
                        size = Size(110f, 30f),
                        cornerRadius = CornerRadius(12f),
                        style = Stroke(width = 3f)
                    )

                    // Draw Hanging Brass Lantern Body
                    val lanternCenter = if (lanternPos == Offset.Zero) Offset(w * 0.5f, h * 0.35f) else lanternPos

                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFFFFD700), Color(0xFFFF8F00), Color(0xFFBF360C)),
                            center = Offset(lanternCenter.x - 8f, lanternCenter.y - 8f),
                            radius = 32f
                        ),
                        radius = 26f,
                        center = lanternCenter
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.8f),
                        radius = 8f,
                        center = lanternCenter
                    )

                    // Draw Swipe Cutter Trail
                    if (swipeTrail.size >= 2) {
                        val trailPath = Path().apply {
                            moveTo(swipeTrail.first().x, swipeTrail.first().y)
                            for (i in 1 until swipeTrail.size) {
                                lineTo(swipeTrail[i].x, swipeTrail[i].y)
                            }
                        }
                        drawPath(
                            path = trailPath,
                            color = Color(0xFF80D8FF),
                            style = Stroke(width = 8f, cap = StrokeCap.Round)
                        )
                    }

                    particles.draw(this)
                }
            }
        }

        when (engineState.playState) {
            GamePlayState.PAUSED -> PauseOverlay(
                onResume = { engineState.resumeGame() },
                onRestart = {
                    if (canvasSize.width > 0 && canvasSize.height > 0) {
                        initLevel(canvasSize.width.toFloat(), canvasSize.height.toFloat())
                    }
                },
                onQuit = onBack
            )
            GamePlayState.WON -> GameWinOverlay(
                score = engineState.score,
                stars = engineState.starsEarned,
                onRestart = {
                    if (canvasSize.width > 0 && canvasSize.height > 0) {
                        initLevel(canvasSize.width.toFloat(), canvasSize.height.toFloat())
                    }
                },
                onQuit = onBack
            )
            GamePlayState.LOST -> GameLoseOverlay(
                score = engineState.score,
                onRestart = {
                    if (canvasSize.width > 0 && canvasSize.height > 0) {
                        initLevel(canvasSize.width.toFloat(), canvasSize.height.toFloat())
                    }
                },
                onQuit = onBack
            )
            else -> {}
        }
    }
}

private fun createRope(id: Int, anchor: Offset, lanternPos: Offset, nodeCount: Int): GameRope {
    val nodes = mutableListOf<RopeNode>()
    val deltaX = lanternPos.x - anchor.x
    val deltaY = lanternPos.y - anchor.y
    val stepX = deltaX / nodeCount.toFloat()
    val stepY = deltaY / nodeCount.toFloat()
    val totalDist = sqrt(deltaX * deltaX + deltaY * deltaY)
    val segmentRestLength = totalDist / nodeCount.toFloat()

    for (i in 0..nodeCount) {
        val p = Offset(anchor.x + stepX * i.toFloat(), anchor.y + stepY * i.toFloat())
        nodes.add(RopeNode(p, p))
    }
    return GameRope(id, anchor, nodes, segmentRestLength)
}

private fun lineSegmentsIntersect(p1: Offset, p2: Offset, p3: Offset, p4: Offset): Boolean {
    fun ccw(a: Offset, b: Offset, c: Offset): Boolean {
        return (c.y - a.y) * (b.x - a.x) > (b.y - a.y) * (c.x - a.x)
    }
    return (ccw(p1, p3, p4) != ccw(p2, p3, p4)) && (ccw(p1, p2, p3) != ccw(p1, p2, p4))
}
