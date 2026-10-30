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
import kotlin.random.Random

private fun Offset.length(): Float = getDistance()
private fun Offset.normalize(): Offset {
    val d = getDistance()
    return if (d > 0f) Offset(x / d, y / d) else Offset.Zero
}

private data class CrescentBubble(
    val row: Int,
    val col: Int,
    val color: Color,
    val colorIndex: Int
)

private data class ProjectileBubble(
    var position: Offset,
    var velocity: Offset,
    val color: Color,
    val colorIndex: Int,
    var isFlying: Boolean = false
)

private val BubbleColors = listOf(
    Color(0xFFFFD700), // Amber Gold
    Color(0xFF00E676), // Emerald Green
    Color(0xFF80D8FF), // Turquoise Blue
    Color(0xFFB388FF), // Amethyst Purple
    Color(0xFFFF5252)  // Crimson Red
)

/**
 * Game 32: Crescent Bubble Popper (فقاعات النور)
 * Category: متعة (Fun)
 * Mechanic: Aim crescent cannon at hanging colored geometric bubbles; match 3 or more of
 * the same color to pop the cluster with wall reflections and floating cluster drops.
 */
@Composable
fun BubbleCrescentGame(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val theme = NoorThemeState.active
    val engineState = rememberGameEngineState(initialLevel = 1, initialLives = 3)

    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    val maxGridRows = 10
    val maxGridCols = 8

    var bubbleGrid by remember(engineState.level) {
        val initialRows = 4 + engineState.level.coerceAtMost(3)
        val list = mutableListOf<CrescentBubble>()
        val rnd = Random(engineState.level * 47)
        for (r in 0 until initialRows) {
            val cols = if (r % 2 == 1) maxGridCols - 1 else maxGridCols
            for (c in 0 until cols) {
                val colorIdx = rnd.nextInt(BubbleColors.size)
                list.add(CrescentBubble(r, c, BubbleColors[colorIdx], colorIdx))
            }
        }
        mutableStateOf(list.toList())
    }

    var launcherColorIndex by remember { mutableIntStateOf(0) }
    var nextColorIndex by remember { mutableIntStateOf(1) }
    var aimAngleRad by remember { mutableFloatStateOf(-Math.PI.toFloat() / 2f) }
    var isAiming by remember { mutableStateOf(false) }

    var projectile by remember {
        mutableStateOf<ProjectileBubble?>(null)
    }

    val particles = remember { ParticleSystem() }

    fun initGame() {
        val initialRows = 4 + engineState.level.coerceAtMost(3)
        val list = mutableListOf<CrescentBubble>()
        val rnd = Random(System.currentTimeMillis())
        for (r in 0 until initialRows) {
            val cols = if (r % 2 == 1) maxGridCols - 1 else maxGridCols
            for (c in 0 until cols) {
                val colorIdx = rnd.nextInt(BubbleColors.size)
                list.add(CrescentBubble(r, c, BubbleColors[colorIdx], colorIdx))
            }
        }
        bubbleGrid = list
        launcherColorIndex = rnd.nextInt(BubbleColors.size)
        nextColorIndex = rnd.nextInt(BubbleColors.size)
        projectile = null
        isAiming = false
        particles.clear()
        engineState.startGame()
    }

    LaunchedEffect(engineState.level) {
        initGame()
    }

    CanvasGameLoop(engineState) { deltaSec ->
        if (engineState.playState != GamePlayState.PLAYING) return@CanvasGameLoop

        particles.update(deltaSec)

        val activeProj = projectile
        if (activeProj != null && activeProj.isFlying && canvasSize.width > 0) {
            val w = canvasSize.width.toFloat()
            val h = canvasSize.height.toFloat()
            val playWidth = w * 0.9f
            val gridLeft = (w - playWidth) / 2f
            val gridRight = gridLeft + playWidth
            val gridTop = 20f
            val cellSize = playWidth / maxGridCols
            val radius = cellSize * 0.45f

            var nextPos = activeProj.position + activeProj.velocity * deltaSec
            var nextVel = activeProj.velocity

            // Wall bounce off left boundary
            if (nextPos.x - radius <= gridLeft) {
                nextPos = Offset(gridLeft + radius, nextPos.y)
                nextVel = Offset(-nextVel.x, nextVel.y)
                SfxManager.playTap(context)
            }
            // Wall bounce off right boundary
            if (nextPos.x + radius >= gridRight) {
                nextPos = Offset(gridRight - radius, nextPos.y)
                nextVel = Offset(-nextVel.x, nextVel.y)
                SfxManager.playTap(context)
            }

            activeProj.position = nextPos
            activeProj.velocity = nextVel

            // Check collision with top ceiling or grid bubbles
            var hitOccurred = false
            var targetCell: Pair<Int, Int>? = null

            if (nextPos.y - radius <= gridTop) {
                hitOccurred = true
                targetCell = findClosestGridCell(nextPos, gridLeft, gridTop, cellSize, maxGridCols, maxGridRows, bubbleGrid)
            } else {
                for (b in bubbleGrid) {
                    val bx = getBubbleX(b.row, b.col, gridLeft, cellSize)
                    val by = getBubbleY(b.row, gridTop, cellSize)
                    val dist = (nextPos - Offset(bx, by)).length()
                    if (dist <= radius * 2f) {
                        hitOccurred = true
                        targetCell = findClosestGridCell(nextPos, gridLeft, gridTop, cellSize, maxGridCols, maxGridRows, bubbleGrid)
                        break
                    }
                }
            }

            if (hitOccurred && targetCell != null) {
                val (snapRow, snapCol) = targetCell
                val newBubble = CrescentBubble(snapRow, snapCol, activeProj.color, activeProj.colorIndex)
                val updatedGrid = bubbleGrid + newBubble

                // Match 3 cluster search
                val cluster = findMatchingCluster(updatedGrid, newBubble, maxGridCols)

                if (cluster.size >= 3) {
                    triggerGameHaptic(context, GameHapticType.SUCCESS)
                    SfxManager.playTap(context)

                    val remainingGrid = updatedGrid.filterNot { b -> cluster.contains(b) }

                    // Find and pop floating unattached bubbles
                    val anchoredGrid = filterAnchoredBubbles(remainingGrid, maxGridCols)
                    val droppedBubbles = remainingGrid.filterNot { b -> anchoredGrid.contains(b) }

                    val totalPopped = cluster.size + droppedBubbles.size
                    engineState.addScore(totalPopped * 15)

                    // Spawn particle bursts for popped cluster
                    cluster.forEach { b ->
                        val bx = getBubbleX(b.row, b.col, gridLeft, cellSize)
                        val by = getBubbleY(b.row, gridTop, cellSize)
                        particles.spawnBurst(Offset(bx, by), count = 12, colors = listOf(b.color, Color.White))
                    }

                    bubbleGrid = anchoredGrid

                    if (anchoredGrid.isEmpty()) {
                        engineState.completeLevel(context, 3)
                    }
                } else {
                    bubbleGrid = updatedGrid

                    // Check if bubbles reached bottom lose line
                    if (snapRow >= maxGridRows - 2) {
                        engineState.failLevel(context)
                    }
                }

                // Reset projectile for next shot
                projectile = null
                launcherColorIndex = nextColorIndex
                nextColorIndex = Random.nextInt(BubbleColors.size)
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
                title = t("فقاعات النور", "Crescent Bubble Popper"),
                level = engineState.level,
                score = engineState.score,
                onBack = onBack,
                onPause = { engineState.pauseGame() }
            )

            Text(
                text = t("وجّه مدفع الهلال وطابق 3 فقاعات من نفس اللون للفرقعة", "Aim the crescent cannon and match 3 bubbles of the same color"),
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
                            onDragStart = { touch ->
                                val w = canvasSize.width.toFloat()
                                val h = canvasSize.height.toFloat()
                                val cannonPos = Offset(w / 2f, h - 60f)
                                val dir = touch - cannonPos
                                aimAngleRad = atan2(dir.y, dir.x).coerceIn(-Math.PI.toFloat() + 0.2f, -0.2f)
                                isAiming = true
                            },
                            onDragMove = { touch, _ ->
                                val w = canvasSize.width.toFloat()
                                val h = canvasSize.height.toFloat()
                                val cannonPos = Offset(w / 2f, h - 60f)
                                val dir = touch - cannonPos
                                aimAngleRad = atan2(dir.y, dir.x).coerceIn(-Math.PI.toFloat() + 0.2f, -0.2f)
                            },
                            onDragEnd = {
                                if (projectile == null && isAiming) {
                                    val w = canvasSize.width.toFloat()
                                    val h = canvasSize.height.toFloat()
                                    val cannonPos = Offset(w / 2f, h - 60f)
                                    val speed = 1200f
                                    val vx = cos(aimAngleRad) * speed
                                    val vy = sin(aimAngleRad) * speed

                                    projectile = ProjectileBubble(
                                        position = cannonPos,
                                        velocity = Offset(vx, vy),
                                        color = BubbleColors[launcherColorIndex],
                                        colorIndex = launcherColorIndex,
                                        isFlying = true
                                    )
                                    triggerGameHaptic(context, GameHapticType.MEDIUM)
                                    SfxManager.playTap(context)
                                }
                                isAiming = false
                            },
                            onTap = { touch ->
                                if (projectile == null) {
                                    val w = canvasSize.width.toFloat()
                                    val h = canvasSize.height.toFloat()
                                    val cannonPos = Offset(w / 2f, h - 60f)
                                    val dir = touch - cannonPos
                                    val angle = atan2(dir.y, dir.x).coerceIn(-Math.PI.toFloat() + 0.2f, -0.2f)
                                    val speed = 1200f

                                    projectile = ProjectileBubble(
                                        position = cannonPos,
                                        velocity = Offset(cos(angle) * speed, sin(angle) * speed),
                                        color = BubbleColors[launcherColorIndex],
                                        colorIndex = launcherColorIndex,
                                        isFlying = true
                                    )
                                    triggerGameHaptic(context, GameHapticType.MEDIUM)
                                    SfxManager.playTap(context)
                                }
                            }
                        )
                ) {
                    val w = size.width
                    val h = size.height
                    val playWidth = w * 0.9f
                    val gridLeft = (w - playWidth) / 2f
                    val gridRight = gridLeft + playWidth
                    val gridTop = 20f
                    val cellSize = playWidth / maxGridCols
                    val radius = cellSize * 0.45f

                    // Draw frame play area border
                    drawRoundRect(
                        color = Color(0xFF1B2A32),
                        topLeft = Offset(gridLeft, gridTop),
                        size = Size(playWidth, h - 40f),
                        cornerRadius = CornerRadius(16f)
                    )

                    // Draw hanging bubble grid
                    bubbleGrid.forEach { b ->
                        val bx = getBubbleX(b.row, b.col, gridLeft, cellSize)
                        val by = getBubbleY(b.row, gridTop, cellSize)

                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color.White, b.color, b.color.copy(alpha = 0.7f)),
                                center = Offset(bx - radius * 0.3f, by - radius * 0.3f),
                                radius = radius * 1.2f
                            ),
                            radius = radius,
                            center = Offset(bx, by)
                        )
                        drawCircle(
                            color = Color.White.copy(alpha = 0.6f),
                            radius = radius * 0.25f,
                            center = Offset(bx - radius * 0.3f, by - radius * 0.3f)
                        )
                    }

                    // Cannon position at bottom center
                    val cannonPos = Offset(w / 2f, h - 60f)

                    // Draw aiming trajectory dotted line with wall bounces
                    if (isAiming && projectile == null) {
                        var curr = cannonPos
                        var dirX = cos(aimAngleRad)
                        var dirY = sin(aimAngleRad)

                        for (step in 0 until 40) {
                            val next = Offset(curr.x + dirX * 18f, curr.y + dirY * 18f)
                            if (next.x - radius <= gridLeft || next.x + radius >= gridRight) {
                                dirX = -dirX
                            }
                            if (next.y <= gridTop) break
                            if (step % 2 == 0) {
                                drawCircle(
                                    color = BubbleColors[launcherColorIndex].copy(alpha = 0.8f),
                                    radius = 5f,
                                    center = next
                                )
                            }
                            curr = next
                        }
                    }

                    // Draw Cannon Crescent Pedestal
                    val crescentPath = Path().apply {
                        addArc(
                            oval = androidx.compose.ui.geometry.Rect(
                                cannonPos.x - 36f, cannonPos.y - 18f,
                                cannonPos.x + 36f, cannonPos.y + 36f
                            ),
                            startAngleDegrees = 0f,
                            sweepAngleDegrees = 180f
                        )
                    }
                    drawPath(crescentPath, color = Color(0xFFFFD700))

                    // Draw launcher current bubble
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color.White, BubbleColors[launcherColorIndex]),
                            center = Offset(cannonPos.x - 6f, cannonPos.y - 6f),
                            radius = radius
                        ),
                        radius = radius,
                        center = cannonPos
                    )

                    // Draw next bubble preview at side
                    val previewPos = Offset(cannonPos.x + 80f, cannonPos.y)
                    drawCircle(
                        color = BubbleColors[nextColorIndex].copy(alpha = 0.6f),
                        radius = radius * 0.7f,
                        center = previewPos
                    )

                    // Draw active flying projectile
                    val activeProj = projectile
                    if (activeProj != null && activeProj.isFlying) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color.White, activeProj.color),
                                center = Offset(activeProj.position.x - 6f, activeProj.position.y - 6f),
                                radius = radius
                            ),
                            radius = radius,
                            center = activeProj.position
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

private fun getBubbleX(row: Int, col: Int, gridLeft: Float, cellSize: Float): Float {
    val offsetShift = if (row % 2 == 1) cellSize / 2f else 0f
    return gridLeft + offsetShift + col * cellSize + cellSize / 2f
}

private fun getBubbleY(row: Int, gridTop: Float, cellSize: Float): Float {
    return gridTop + row * (cellSize * 0.866f) + cellSize / 2f
}

private fun findClosestGridCell(
    pos: Offset,
    gridLeft: Float,
    gridTop: Float,
    cellSize: Float,
    maxCols: Int,
    maxRows: Int,
    existingBubbles: List<CrescentBubble>
): Pair<Int, Int> {
    var bestCell = 0 to 0
    var minDist = Float.MAX_VALUE

    for (r in 0 until maxRows) {
        val colsInRow = if (r % 2 == 1) maxCols - 1 else maxCols
        for (c in 0 until colsInRow) {
            if (existingBubbles.none { it.row == r && it.col == c }) {
                val bx = getBubbleX(r, c, gridLeft, cellSize)
                val by = getBubbleY(r, gridTop, cellSize)
                val dist = (pos - Offset(bx, by)).length()
                if (dist < minDist) {
                    minDist = dist
                    bestCell = r to c
                }
            }
        }
    }
    return bestCell
}

private fun findMatchingCluster(
    grid: List<CrescentBubble>,
    startBubble: CrescentBubble,
    maxCols: Int
): List<CrescentBubble> {
    val cluster = mutableListOf<CrescentBubble>()
    val queue = mutableListOf(startBubble)
    val visited = mutableSetOf<Pair<Int, Int>>()

    val gridMap = grid.associateBy { it.row to it.col }

    while (queue.isNotEmpty()) {
        val curr = queue.removeAt(0)
        val key = curr.row to curr.col
        if (visited.contains(key)) continue
        visited.add(key)
        cluster.add(curr)

        val neighbors = getNeighbors(curr.row, curr.col, maxCols)
        for ((nr, nc) in neighbors) {
            val neighborBubble = gridMap[nr to nc]
            if (neighborBubble != null && neighborBubble.colorIndex == startBubble.colorIndex && !visited.contains(nr to nc)) {
                queue.add(neighborBubble)
            }
        }
    }
    return cluster
}

private fun filterAnchoredBubbles(
    grid: List<CrescentBubble>,
    maxCols: Int
): List<CrescentBubble> {
    val gridMap = grid.associateBy { it.row to it.col }
    val visited = mutableSetOf<Pair<Int, Int>>()
    val queue = mutableListOf<CrescentBubble>()

    grid.filter { it.row == 0 }.forEach {
        queue.add(it)
    }

    while (queue.isNotEmpty()) {
        val curr = queue.removeAt(0)
        val key = curr.row to curr.col
        if (visited.contains(key)) continue
        visited.add(key)

        val neighbors = getNeighbors(curr.row, curr.col, maxCols)
        for ((nr, nc) in neighbors) {
            val neighbor = gridMap[nr to nc]
            if (neighbor != null && !visited.contains(nr to nc)) {
                queue.add(neighbor)
            }
        }
    }
    return grid.filter { visited.contains(it.row to it.col) }
}

private fun getNeighbors(r: Int, c: Int, maxCols: Int): List<Pair<Int, Int>> {
    val isOdd = r % 2 == 1
    return if (isOdd) {
        listOf(
            r to c - 1, r to c + 1,
            r - 1 to c, r - 1 to c + 1,
            r + 1 to c, r + 1 to c + 1
        )
    } else {
        listOf(
            r to c - 1, r to c + 1,
            r - 1 to c - 1, r - 1 to c,
            r + 1 to c - 1, r + 1 to c
        )
    }
}
