package com.elhajri.noor.games

import androidx.compose.animation.core.*
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
import androidx.compose.ui.geometry.Rect
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

private data class MazeCell(
    val row: Int,
    val col: Int,
    var wallNorth: Boolean = true,
    var wallSouth: Boolean = true,
    var wallEast: Boolean = true,
    var wallWest: Boolean = true,
    var visited: Boolean = false
)

private data class LanternDrop(
    val id: Int,
    val cellRow: Int,
    val cellCol: Int,
    var collected: Boolean = false
)

private data class WallSegment(
    val start: Offset,
    val end: Offset
)

/**
 * Game 09: Medina Labyrinth (متاهة المدينة)
 * Category: ألعاب ذكاء (Intelligence)
 * Mechanic: 2D Maze pathfinding. Drag glowing traveler orb through ancient city corridors
 * to reach the central mosque with fog-of-war vision mode and lantern light drops.
 */
@Composable
fun MedinaMazeGame(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val theme = NoorThemeState.active
    val engineState = rememberGameEngineState(initialLevel = 1, initialLives = 3)

    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    val mazeGridSize = when (engineState.level) {
        1 -> 6
        2 -> 7
        else -> 8
    }

    var mazeCells by remember(engineState.level) {
        mutableStateOf(generateMazeGrid(mazeGridSize, mazeGridSize))
    }

    var lanterns by remember(engineState.level) {
        val dropCount = 2 + engineState.level
        val drops = mutableListOf<LanternDrop>()
        val rnd = Random(engineState.level * 31)
        var id = 0
        while (drops.size < dropCount) {
            val r = rnd.nextInt(mazeGridSize)
            val c = rnd.nextInt(mazeGridSize)
            if ((r != 0 || c != 0) && (r != mazeGridSize - 1 || c != mazeGridSize - 1)) {
                if (drops.none { it.cellRow == r && it.cellCol == c }) {
                    drops.add(LanternDrop(id++, r, c))
                }
            }
        }
        mutableStateOf(drops.toList())
    }

    var playerPos by remember(engineState.level) { mutableStateOf(Offset.Zero) }
    var targetTouchPos by remember(engineState.level) { mutableStateOf(Offset.Zero) }
    var visionRadiusBonus by remember(engineState.level) { mutableFloatStateOf(0f) }

    val particles = remember { ParticleSystem() }

    fun resetGame() {
        mazeCells = generateMazeGrid(mazeGridSize, mazeGridSize)
        val dropCount = 2 + engineState.level
        val drops = mutableListOf<LanternDrop>()
        val rnd = Random(System.currentTimeMillis())
        var id = 0
        while (drops.size < dropCount) {
            val r = rnd.nextInt(mazeGridSize)
            val c = rnd.nextInt(mazeGridSize)
            if ((r != 0 || c != 0) && (r != mazeGridSize - 1 || c != mazeGridSize - 1)) {
                if (drops.none { it.cellRow == r && it.cellCol == c }) {
                    drops.add(LanternDrop(id++, r, c))
                }
            }
        }
        lanterns = drops
        visionRadiusBonus = 0f
        playerPos = Offset.Zero
        targetTouchPos = Offset.Zero
        particles.clear()
        engineState.startGame()
    }

    LaunchedEffect(engineState.level) {
        resetGame()
    }

    CanvasGameLoop(engineState) { deltaSec ->
        if (engineState.playState != GamePlayState.PLAYING) return@CanvasGameLoop

        particles.update(deltaSec)

        if (canvasSize.width > 0 && canvasSize.height > 0) {
            val w = canvasSize.width.toFloat()
            val h = canvasSize.height.toFloat()
            val mazeDim = min(w, h) * 0.88f
            val gridLeft = (w - mazeDim) / 2f
            val gridTop = (h - mazeDim) / 2f
            val cellSize = mazeDim / mazeGridSize

            // Initialize player position at cell (0,0) center if at Zero
            if (playerPos == Offset.Zero) {
                val initPos = Offset(gridLeft + cellSize / 2f, gridTop + cellSize / 2f)
                playerPos = initPos
                targetTouchPos = initPos
            }

            // Smooth movement toward target touch point with wall collision constraint
            if (targetTouchPos != Offset.Zero && playerPos != targetTouchPos) {
                val speed = 350f * deltaSec
                val dir = targetTouchPos - playerPos
                val dist = dir.length()
                if (dist > 2f) {
                    val normDir = dir.normalize()
                    val nextPos = playerPos + normDir * min(speed, dist)

                    val wallSegments = extractWallSegments(mazeCells, mazeGridSize, gridLeft, gridTop, cellSize)
                    val clampedPos = resolveWallCollision(playerPos, nextPos, wallSegments, playerRadius = cellSize * 0.25f)
                    playerPos = clampedPos
                } else {
                    playerPos = targetTouchPos
                }
            }

            // Check lantern pickups
            val updatedLanterns = lanterns.map { lantern ->
                if (!lantern.collected) {
                    val lanternX = gridLeft + lantern.cellCol * cellSize + cellSize / 2f
                    val lanternY = gridTop + lantern.cellRow * cellSize + cellSize / 2f
                    val lanternCenter = Offset(lanternX, lanternY)
                    val distToLantern = (playerPos - lanternCenter).length()

                    if (distToLantern < cellSize * 0.4f) {
                        triggerGameHaptic(context, GameHapticType.LIGHT)
                        SfxManager.playTap(context)
                        engineState.addScore(25)
                        visionRadiusBonus += 35f
                        particles.spawnBurst(lanternCenter, count = 18, colors = listOf(Color(0xFFFFD700), Color(0xFFFFAB00), Color.White))
                        lantern.copy(collected = true)
                    } else lantern
                } else lantern
            }
            if (updatedLanterns != lanterns) {
                lanterns = updatedLanterns
            }

            // Check goal mosque collision at cell (mazeGridSize-1, mazeGridSize-1)
            val mosqueX = gridLeft + (mazeGridSize - 1) * cellSize + cellSize / 2f
            val mosqueY = gridTop + (mazeGridSize - 1) * cellSize + cellSize / 2f
            val mosqueCenter = Offset(mosqueX, mosqueY)
            val distToMosque = (playerPos - mosqueCenter).length()

            if (distToMosque < cellSize * 0.45f) {
                triggerGameHaptic(context, GameHapticType.SUCCESS)
                particles.spawnBurst(mosqueCenter, count = 40, colors = listOf(Color(0xFF00E676), Color(0xFFFFD700), Color.White))
                engineState.completeLevel(context, 3)
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
                title = t("متاهة المدينة", "Medina Labyrinth"),
                level = engineState.level,
                score = engineState.score,
                onBack = onBack,
                onPause = { engineState.pauseGame() }
            )

            Text(
                text = t("وجّه قنديل النور عبر أزقة المدينة للوصول إلى المسجد", "Guide the lantern orb through Medina corridors to reach the mosque"),
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
                                targetTouchPos = offset
                            },
                            onDragMove = { offset, _ ->
                                targetTouchPos = offset
                            },
                            onTap = { offset ->
                                targetTouchPos = offset
                            }
                        )
                ) {
                    val w = size.width
                    val h = size.height
                    val mazeDim = min(w, h) * 0.88f
                    val gridLeft = (w - mazeDim) / 2f
                    val gridTop = (h - mazeDim) / 2f
                    val cellSize = mazeDim / mazeGridSize

                    // Draw maze base floor
                    drawRoundRect(
                        color = Color(0xFF131D24),
                        topLeft = Offset(gridLeft, gridTop),
                        size = Size(mazeDim, mazeDim),
                        cornerRadius = CornerRadius(16f)
                    )

                    // Draw corridor grid background accents
                    for (r in 0 until mazeGridSize) {
                        for (c in 0 until mazeGridSize) {
                            val cx = gridLeft + c * cellSize
                            val cy = gridTop + r * cellSize
                            drawRoundRect(
                                color = Color(0xFF1E2C35),
                                topLeft = Offset(cx + 2f, cy + 2f),
                                size = Size(cellSize - 4f, cellSize - 4f),
                                cornerRadius = CornerRadius(8f)
                            )
                        }
                    }

                    // Draw Lantern drops
                    lanterns.forEach { lantern ->
                        if (!lantern.collected) {
                            val lx = gridLeft + lantern.cellCol * cellSize + cellSize / 2f
                            val ly = gridTop + lantern.cellRow * cellSize + cellSize / 2f

                            drawCircle(
                                color = Color(0xFFFFD700).copy(alpha = 0.3f),
                                radius = cellSize * 0.35f,
                                center = Offset(lx, ly)
                            )
                            drawCircle(
                                color = Color(0xFFFFAB00),
                                radius = cellSize * 0.18f,
                                center = Offset(lx, ly)
                            )
                            drawCircle(
                                color = Color.White,
                                radius = cellSize * 0.08f,
                                center = Offset(lx, ly)
                            )
                        }
                    }

                    // Draw Goal Mosque
                    val mosqueX = gridLeft + (mazeGridSize - 1) * cellSize + cellSize / 2f
                    val mosqueY = gridTop + (mazeGridSize - 1) * cellSize + cellSize / 2f
                    val mosqueCenter = Offset(mosqueX, mosqueY)

                    drawCircle(
                        color = Color(0xFF00E676).copy(alpha = 0.35f),
                        radius = cellSize * 0.42f,
                        center = mosqueCenter
                    )
                    drawCircle(
                        color = Color(0xFF00E676),
                        radius = cellSize * 0.28f,
                        center = mosqueCenter,
                        style = Stroke(width = 3f)
                    )
                    // Draw Mosque Crescent Dome Symbol
                    val domePath = Path().apply {
                        addArc(
                            oval = Rect(
                                mosqueCenter.x - cellSize * 0.2f,
                                mosqueCenter.y - cellSize * 0.15f,
                                mosqueCenter.x + cellSize * 0.2f,
                                mosqueCenter.y + cellSize * 0.25f
                            ),
                            startAngleDegrees = 180f,
                            sweepAngleDegrees = 180f
                        )
                    }
                    drawPath(domePath, color = Color(0xFF00E676))
                    drawCircle(
                        color = Color(0xFFFFD700),
                        radius = cellSize * 0.08f,
                        center = Offset(mosqueCenter.x, mosqueCenter.y - cellSize * 0.22f)
                    )

                    // Draw Walls
                    val wallStrokeWidth = 8f
                    val wallColor = Color(0xFFFFD54F)

                    for (r in 0 until mazeGridSize) {
                        for (c in 0 until mazeGridSize) {
                            val cell = mazeCells[r][c]
                            val x1 = gridLeft + c * cellSize
                            val y1 = gridTop + r * cellSize
                            val x2 = x1 + cellSize
                            val y2 = y1 + cellSize

                            if (cell.wallNorth) {
                                drawLine(wallColor, Offset(x1, y1), Offset(x2, y1), wallStrokeWidth, StrokeCap.Round)
                            }
                            if (cell.wallSouth) {
                                drawLine(wallColor, Offset(x1, y2), Offset(x2, y2), wallStrokeWidth, StrokeCap.Round)
                            }
                            if (cell.wallEast) {
                                drawLine(wallColor, Offset(x2, y1), Offset(x2, y2), wallStrokeWidth, StrokeCap.Round)
                            }
                            if (cell.wallWest) {
                                drawLine(wallColor, Offset(x1, y1), Offset(x1, y2), wallStrokeWidth, StrokeCap.Round)
                            }
                        }
                    }

                    // Render Fog of War Overlay
                    val baseVisionRad = cellSize * 1.5f + visionRadiusBonus

                    // Dark ambient fog tint over maze except illuminated circles
                    drawRoundRect(
                        color = Color.Black.copy(alpha = 0.55f),
                        topLeft = Offset(gridLeft, gridTop),
                        size = Size(mazeDim, mazeDim),
                        cornerRadius = CornerRadius(16f)
                    )

                    // Player illuminated aura
                    val currentPos = if (playerPos == Offset.Zero) Offset(gridLeft + cellSize / 2f, gridTop + cellSize / 2f) else playerPos

                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFF80D8FF).copy(alpha = 0.45f), Color.Transparent),
                            center = currentPos,
                            radius = baseVisionRad
                        ),
                        radius = baseVisionRad,
                        center = currentPos
                    )

                    // Player Traveler Orb
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color.White, Color(0xFF80D8FF), Color(0xFF0288D1)),
                            center = currentPos,
                            radius = cellSize * 0.28f
                        ),
                        radius = cellSize * 0.22f,
                        center = currentPos
                    )
                    drawCircle(
                        color = Color.White,
                        radius = cellSize * 0.12f,
                        center = currentPos
                    )

                    particles.draw(this)
                }
            }
        }

        when (engineState.playState) {
            GamePlayState.PAUSED -> PauseOverlay(
                onResume = { engineState.resumeGame() },
                onRestart = { resetGame() },
                onQuit = onBack
            )
            GamePlayState.WON -> GameWinOverlay(
                score = engineState.score,
                stars = engineState.starsEarned,
                onRestart = { resetGame() },
                onQuit = onBack
            )
            GamePlayState.LOST -> GameLoseOverlay(
                score = engineState.score,
                onRestart = { resetGame() },
                onQuit = onBack
            )
            else -> {}
        }
    }
}

private fun generateMazeGrid(rows: Int, cols: Int): List<List<MazeCell>> {
    val grid = List(rows) { r -> List(cols) { c -> MazeCell(r, c) } }
    val stack = mutableListOf<MazeCell>()

    var current = grid[0][0]
    current.visited = true
    var visitedCount = 1
    val totalCells = rows * cols

    while (visitedCount < totalCells) {
        val neighbors = mutableListOf<Pair<MazeCell, String>>()
        val r = current.row
        val c = current.col

        if (r > 0 && !grid[r - 1][c].visited) neighbors.add(grid[r - 1][c] to "N")
        if (r < rows - 1 && !grid[r + 1][c].visited) neighbors.add(grid[r + 1][c] to "S")
        if (c < cols - 1 && !grid[r][c + 1].visited) neighbors.add(grid[r][c + 1] to "E")
        if (c > 0 && !grid[r][c - 1].visited) neighbors.add(grid[r][c - 1] to "W")

        if (neighbors.isNotEmpty()) {
            val (nextCell, dir) = neighbors.random()
            when (dir) {
                "N" -> { current.wallNorth = false; nextCell.wallSouth = false }
                "S" -> { current.wallSouth = false; nextCell.wallNorth = false }
                "E" -> { current.wallEast = false; nextCell.wallWest = false }
                "W" -> { current.wallWest = false; nextCell.wallEast = false }
            }
            stack.add(current)
            current = nextCell
            current.visited = true
            visitedCount++
        } else if (stack.isNotEmpty()) {
            current = stack.removeAt(stack.size - 1)
        } else {
            break
        }
    }
    return grid
}

private fun extractWallSegments(
    grid: List<List<MazeCell>>,
    size: Int,
    gridLeft: Float,
    gridTop: Float,
    cellSize: Float
): List<WallSegment> {
    val list = mutableListOf<WallSegment>()
    for (r in 0 until size) {
        for (c in 0 until size) {
            val cell = grid[r][c]
            val x1 = gridLeft + c * cellSize
            val y1 = gridTop + r * cellSize
            val x2 = x1 + cellSize
            val y2 = y1 + cellSize

            if (cell.wallNorth) list.add(WallSegment(Offset(x1, y1), Offset(x2, y1)))
            if (cell.wallSouth) list.add(WallSegment(Offset(x1, y2), Offset(x2, y2)))
            if (cell.wallEast) list.add(WallSegment(Offset(x2, y1), Offset(x2, y2)))
            if (cell.wallWest) list.add(WallSegment(Offset(x1, y1), Offset(x1, y2)))
        }
    }
    return list
}

private fun resolveWallCollision(
    currentPos: Offset,
    nextPos: Offset,
    walls: List<WallSegment>,
    playerRadius: Float
): Offset {
    var resolved = nextPos
    for (w in walls) {
        val distToSeg = distancePointToSegment(resolved, w.start, w.end)
        if (distToSeg < playerRadius) {
            val pushDir = (resolved - closestPointOnSegment(resolved, w.start, w.end))
            val len = pushDir.length()
            if (len > 0.001f) {
                val norm = pushDir.normalize()
                resolved = closestPointOnSegment(resolved, w.start, w.end) + norm * (playerRadius + 1f)
            } else {
                resolved = currentPos
            }
        }
    }
    return resolved
}

private fun distancePointToSegment(p: Offset, a: Offset, b: Offset): Float {
    val pt = closestPointOnSegment(p, a, b)
    return (p - pt).length()
}

private fun closestPointOnSegment(p: Offset, a: Offset, b: Offset): Offset {
    val ab = b - a
    val denom = ab.x * ab.x + ab.y * ab.y
    if (denom == 0f) return a
    val t = (((p.x - a.x) * ab.x + (p.y - a.y) * ab.y) / denom).coerceIn(0f, 1f)
    return Offset(a.x + t * ab.x, a.y + t * ab.y)
}
