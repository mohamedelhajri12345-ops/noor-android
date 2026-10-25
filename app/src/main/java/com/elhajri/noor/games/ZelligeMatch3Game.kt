package com.elhajri.noor.games

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.games.engine.*
import com.elhajri.noor.settings.t
import com.elhajri.noor.theme.NoorThemeState
import com.elhajri.noor.theme.noorGlassCard
import com.elhajri.noor.ui.AmiriFamily
import kotlin.random.Random

/**
 * Game 02: نقوش الزليج (Zellige Match-3)
 * Match 3 or more adjacent geometric Zellige tile colors.
 */
@Composable
fun ZelligeMatch3Game(
    onBack: () -> Unit
) {
    val theme = NoorThemeState.active
    val rows = 6
    val cols = 6
    val colors = listOf(
        Color(0xFFFFD700), // Gold
        Color(0xFF00E676), // Emerald
        Color(0xFF00B0FF), // Sapphire
        Color(0xFFFF4081), // Ruby
        Color(0xFFE040FB)  // Violet
    )

    var grid by remember { mutableStateOf(Array(rows) { IntArray(cols) { Random.nextInt(colors.size) } }) }
    var selectedCell by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var score by remember { mutableIntStateOf(0) }
    var movesLeft by remember { mutableIntStateOf(20) }
    var isGameOver by remember { mutableStateOf(false) }

    fun checkMatches(currentGrid: Array<IntArray>): Set<Pair<Int, Int>> {
        val matches = mutableSetOf<Pair<Int, Int>>()
        // Horizontal
        for (r in 0 until rows) {
            for (c in 0 until cols - 2) {
                val color = currentGrid[r][c]
                if (color != -1 && currentGrid[r][c + 1] == color && currentGrid[r][c + 2] == color) {
                    matches.add(r to c)
                    matches.add(r to c + 1)
                    matches.add(r to c + 2)
                }
            }
        }
        // Vertical
        for (r in 0 until rows - 2) {
            for (c in 0 until cols) {
                val color = currentGrid[r][c]
                if (color != -1 && currentGrid[r + 1][c] == color && currentGrid[r + 2][c] == color) {
                    matches.add(r to c)
                    matches.add(r + 1 to c)
                    matches.add(r + 2 to c)
                }
            }
        }
        return matches
    }

    fun handleCellTap(r: Int, c: Int) {
        if (isGameOver || movesLeft <= 0) return

        val sel = selectedCell
        if (sel == null) {
            selectedCell = r to c
        } else {
            val (sr, sc) = sel
            val isAdjacent = (kotlin.math.abs(sr - r) + kotlin.math.abs(sc - c)) == 1
            if (isAdjacent) {
                // Swap
                val newGrid = Array(rows) { row -> grid[row].clone() }
                val temp = newGrid[sr][sc]
                newGrid[sr][sc] = newGrid[r][c]
                newGrid[r][c] = temp

                val matches = checkMatches(newGrid)
                if (matches.isNotEmpty()) {
                    // Apply swap & clear matches
                    matches.forEach { (mr, mc) -> newGrid[mr][mc] = Random.nextInt(colors.size) }
                    grid = newGrid
                    score += matches.size * 10
                    movesLeft -= 1
                    if (movesLeft <= 0) isGameOver = true
                }
            }
            selectedCell = null
        }
    }

    Scaffold(
        containerColor = theme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = theme.accent)
                }
                Text(
                    text = t("نقوش الزليج", "Zellige Match-3"),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = AmiriFamily
                )
                IconButton(onClick = {
                    grid = Array(rows) { IntArray(cols) { Random.nextInt(colors.size) } }
                    score = 0
                    movesLeft = 20
                    isGameOver = false
                }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = theme.accent)
                }
            }

            Spacer(Modifier.height(16.dp))

            // Scoreboard
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .noorGlassCard(cornerRadius = 16.dp)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(t("النقاط", "Score"), color = theme.accentSoft, fontSize = 12.sp)
                    Text("$score", color = theme.accent, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(t("الحركات", "Moves"), color = theme.accentSoft, fontSize = 12.sp)
                    Text("$movesLeft", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(20.dp))

            // Grid Canvas
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .padding(16.dp)
                    .noorGlassCard(cornerRadius = 24.dp)
                    .border(2.dp, theme.accent.copy(alpha = 0.3f), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                val density = LocalDensity.current
                val boxWidthPx = with(density) { maxWidth.toPx() }
                val boxHeightPx = with(density) { maxHeight.toPx() }

                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .gameGestureDetector(
                            onTap = { offset ->
                                val cellW = boxWidthPx / cols.toFloat()
                                val cellH = boxHeightPx / rows.toFloat()
                                if (cellW > 0f && cellH > 0f) {
                                    val c = (offset.x / cellW).toInt().coerceIn(0, cols - 1)
                                    val r = (offset.y / cellH).toInt().coerceIn(0, rows - 1)
                                    handleCellTap(r, c)
                                }
                            }
                        )
                ) {
                    val cellW = size.width / cols.toFloat()
                    val cellH = size.height / rows.toFloat()

                    for (r in 0 until rows) {
                        for (c in 0 until cols) {
                            val colorIdx = grid[r][c]
                            val left = c * cellW + 4f
                            val top = r * cellH + 4f
                            val w = cellW - 8f
                            val h = cellH - 8f

                            val color = if (colorIdx in colors.indices) colors[colorIdx] else Color.Gray

                            drawRoundRect(
                                color = color,
                                topLeft = Offset(left, top),
                                size = Size(w, h),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f)
                            )

                            if (selectedCell == r to c) {
                                drawRoundRect(
                                    color = Color.White,
                                    topLeft = Offset(left, top),
                                    size = Size(w, h),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f),
                                    style = Stroke(width = 4f)
                                )
                            }
                        }
                    }
                }
            }

            if (isGameOver) {
                Spacer(Modifier.height(16.dp))
                Card(colors = CardDefaults.cardColors(containerColor = theme.surface)) {
                    Text(
                        t("انتهت اللعبة! مجموع النقاط: $score", "Game Over! Score: $score"),
                        color = theme.accent,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}
