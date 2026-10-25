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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.audio.SfxManager
import com.elhajri.noor.games.engine.*
import com.elhajri.noor.settings.t
import com.elhajri.noor.theme.NoorThemeState
import com.elhajri.noor.ui.noorGlassCard
import kotlin.random.Random

data class MergeItem(
    val id: Int,
    var level: Int, // 1 to 6
    val type: Int,  // 1: Flora, 2: Water
    var gridX: Int,
    var gridY: Int
)

@Composable
fun OasisMergerGame(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val theme = NoorThemeState.active
    val engineState = rememberGameEngineState(initialLevel = 1, targetScore = 150)

    val gridRows = 4
    val gridCols = 4

    val gridItems = remember { mutableStateListOf<MergeItem>() }
    var draggedItem by remember { mutableStateOf<MergeItem?>(null) }
    var dragPos by remember { mutableStateOf(Offset.Zero) }
    val particles = remember { ParticleSystem() }

    val itemColors = listOf(
        Color(0xFF80D8FF), // L1 Water
        Color(0xFF00E676), // L2 Sapling
        Color(0xFF64FFDA), // L3 Well
        Color(0xFFFFD700), // L4 Palm Tree
        Color(0xFFFFAB00), // L5 Date Oasis
        Color(0xFFFF5252)  // L6 Royal Palace
    )

    val itemNamesAr = listOf(
        "قطرة ماء", "شتلة نخل", "عين ماء", "نخلة باسقة", "واحة تمور", "قصر الواحة"
    )

    fun spawnItem() {
        val emptySlots = mutableListOf<Pair<Int, Int>>()
        for (r in 0 until gridRows) {
            for (c in 0 until gridCols) {
                if (gridItems.none { it.gridX == c && it.gridY == r }) {
                    emptySlots.add(Pair(c, r))
                }
            }
        }

        if (emptySlots.isNotEmpty()) {
            val (c, r) = emptySlots.random()
            val newType = if (Random.nextBoolean()) 1 else 2
            gridItems.add(
                MergeItem(
                    id = Random.nextInt(1000000),
                    level = 1,
                    type = newType,
                    gridX = c,
                    gridY = r
                )
            )
            SfxManager.playTap(context)
            triggerGameHaptic(context, GameHapticType.LIGHT)
        }
    }

    fun initGame() {
        gridItems.clear()
        draggedItem = null
        repeat(4) { spawnItem() }
        engineState.startGame()
    }

    LaunchedEffect(Unit) {
        initGame()
    }

    fun handleDragEnd(cellSizePx: Float) {
        val dragged = draggedItem ?: return

        val targetCol = (dragPos.x / cellSizePx).toInt().coerceIn(0, gridCols - 1)
        val targetRow = (dragPos.y / cellSizePx).toInt().coerceIn(0, gridRows - 1)

        val targetItem = gridItems.find { it.id != dragged.id && it.gridX == targetCol && it.gridY == targetRow }

        if (targetItem != null) {
            if (targetItem.level == dragged.level && targetItem.level < 6) {
                // Merge!
                targetItem.level += 1
                gridItems.remove(dragged)
                SfxManager.playSuccess(context)
                triggerGameHaptic(context, GameHapticType.MEDIUM)

                val centerPos = Offset((targetCol + 0.5f) * cellSizePx, (targetRow + 0.5f) * cellSizePx)
                particles.spawnBurst(centerPos, count = 20)

                val pts = targetItem.level * 15
                engineState.addScore(pts)

                if (engineState.score >= engineState.targetScore) {
                    engineState.completeLevel(context, 3)
                }
            } else {
                // Swap position
                targetItem.gridX = dragged.gridX
                targetItem.gridY = dragged.gridY
                dragged.gridX = targetCol
                dragged.gridY = targetRow
            }
        } else {
            // Move to empty cell
            dragged.gridX = targetCol
            dragged.gridY = targetRow
        }

        draggedItem = null
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
                title = t("واحة السلام", "Oasis Merge"),
                level = engineState.level,
                score = engineState.score,
                lives = engineState.lives,
                onBack = onBack,
                onPause = { engineState.pauseGame() }
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = t("اسحب العناصر المتطابقة وادمجهما لتطوير واحتك!", "Drag and merge identical items to evolve your oasis!"),
                color = theme.textSecondary,
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(Modifier.height(12.dp))

            // Generator Button
            Button(
                onClick = { spawnItem() },
                colors = ButtonDefaults.buttonColors(containerColor = theme.accent),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(
                    text = t("إنتاج عنصر جديد 🌿", "Produce New Item 🌿"),
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(12.dp))

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp)
                    .noorGlassCard(cornerRadius = 24.dp)
                    .border(2.dp, theme.accent.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
            ) {
                val boardSize = minOf(constraints.maxWidth, constraints.maxHeight).toFloat()
                val cellSize = boardSize / 4f

                Canvas(
                    modifier = Modifier
                        .size(boardSize.dp)
                        .gameGestureDetector(
                            onDragStart = { offset ->
                                val col = (offset.x / cellSize).toInt().coerceIn(0, 3)
                                val row = (offset.y / cellSize).toInt().coerceIn(0, 3)
                                val item = gridItems.find { it.gridX == col && it.gridY == row }
                                if (item != null) {
                                    draggedItem = item
                                    dragPos = offset
                                }
                            },
                            onDragMove = { offset, _ ->
                                dragPos = offset
                            },
                            onDragEnd = {
                                handleDragEnd(cellSize)
                            }
                        )
                ) {
                    // Draw grid background
                    for (r in 0 until gridRows) {
                        for (c in 0 until gridCols) {
                            drawRoundRect(
                                color = Color.White.copy(alpha = 0.1f),
                                topLeft = Offset(c * cellSize + 6f, r * cellSize + 6f),
                                size = Size(cellSize - 12f, cellSize - 12f),
                                cornerRadius = CornerRadius(12f)
                            )
                        }
                    }

                    // Draw grid items
                    gridItems.forEach { item ->
                        if (item.id != draggedItem?.id) {
                            val colorIdx = (item.level - 1).coerceIn(0, itemColors.size - 1)
                            val itemColor = itemColors[colorIdx]

                            drawRoundRect(
                                color = itemColor,
                                topLeft = Offset(item.gridX * cellSize + 10f, item.gridY * cellSize + 10f),
                                size = Size(cellSize - 20f, cellSize - 20f),
                                cornerRadius = CornerRadius(16f)
                            )
                        }
                    }

                    // Draw dragged item at finger
                    draggedItem?.let { item ->
                        val colorIdx = (item.level - 1).coerceIn(0, itemColors.size - 1)
                        val itemColor = itemColors[colorIdx]

                        drawRoundRect(
                            color = itemColor.copy(alpha = 0.85f),
                            topLeft = Offset(dragPos.x - cellSize / 2f, dragPos.y - cellSize / 2f),
                            size = Size(cellSize - 10f, cellSize - 10f),
                            cornerRadius = CornerRadius(16f)
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
