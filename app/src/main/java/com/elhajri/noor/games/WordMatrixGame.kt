@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.elhajri.noor.games
import androidx.compose.foundation.BorderStroke

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.audio.SfxManager
import com.elhajri.noor.games.engine.*
import com.elhajri.noor.settings.t
import com.elhajri.noor.settings.toArabicDigits
import com.elhajri.noor.theme.NoorThemeState
import com.elhajri.noor.ui.noorGlassCard
import kotlin.math.abs

/**
 * Game 21: Quranic Word Matrix (مسار الكلمات)
 * Category: كلمات
 * Mechanic: Drag finger across adjacent letter tiles on a 5x5 grid in any direction to trace valid Quranic words.
 */
data class MatrixGridPos(val row: Int, val col: Int)

private data class WordMatrixLevel(
    val id: Int,
    val themeTitle: String,
    val grid: List<List<Char>>,
    val targetWords: List<String>
)

@Composable
fun WordMatrixGame(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val theme = NoorThemeState.active
    val engineState = rememberGameEngineState(initialLevel = 1, initialLives = 3, timeLimitMs = 120000L, targetScore = 300)

    val levels = remember {
        listOf(
            WordMatrixLevel(
                id = 1,
                themeTitle = "كلمات قرآنية",
                grid = listOf(
                    listOf('س', 'ل', 'ا', 'م', 'ن'),
                    listOf('ر', 'ح', 'م', 'ة', 'و'),
                    listOf('إ', 'ي', 'م', 'ا', 'ر'),
                    listOf('ج', 'ن', 'ة', 'ن', 'ع'),
                    listOf('ص', 'ب', 'ر', 'ح', 'ق')
                ),
                targetWords = listOf("سلام", "رحمة", "إيمان", "جنة", "صبر", "حق", "نور")
            ),
            WordMatrixLevel(
                id = 2,
                themeTitle = "أخلاق وهداية",
                grid = listOf(
                    listOf('ت', 'ق', 'و', 'ى', 'ه'),
                    listOf('إ', 'ح', 'س', 'ا', 'د'),
                    listOf('ف', 'ر', 'ق', 'ا', 'ى'),
                    listOf('ك', 'و', 'ث', 'ر', 'ن'),
                    listOf('ط', 'ا', 'ع', 'ة', 'م')
                ),
                targetWords = listOf("تقوى", "هدى", "إحسان", "فرقان", "كوثر", "طاعة")
            ),
            WordMatrixLevel(
                id = 3,
                themeTitle = "أسماء الأنبياء",
                grid = listOf(
                    listOf('م', 'و', 'س', 'ى', 'ع'),
                    listOf('ع', 'ي', 'س', 'ى', 'ي'),
                    listOf('ي', 'و', 'س', 'ف', 'س'),
                    listOf('أ', 'ي', 'و', 'ب', 'ى'),
                    listOf('إ', 'ب', 'ر', 'ا', 'ه')
                ),
                targetWords = listOf("موسى", "عيسى", "يوسف", "أيوب")
            )
        )
    }

    var currentLevelIdx by remember { mutableIntStateOf(0) }
    val currentLevel = levels[currentLevelIdx.coerceIn(0, levels.size - 1)]

    val foundWords = remember { mutableStateListOf<String>() }
    val foundCellsMap = remember { mutableStateMapOf<MatrixGridPos, Color>() }
    val activeSelection = remember { mutableStateListOf<MatrixGridPos>() }
    val particles = remember { ParticleSystem() }

    val wordColors = remember {
        listOf(
            Color(0xFF4CAF50), Color(0xFF2196F3), Color(0xFFFF9800),
            Color(0xFFE91E63), Color(0xFF9C27B0), Color(0xFF00BCD4), Color(0xFFFFC107)
        )
    }

    fun initLevel() {
        foundWords.clear()
        foundCellsMap.clear()
        activeSelection.clear()
        engineState.startGame()
    }

    LaunchedEffect(currentLevelIdx) {
        initLevel()
    }

    fun getCellFromOffset(offset: Offset, size: Size): MatrixGridPos? {
        val cellWidth = size.width / 5f
        val cellHeight = size.height / 5f
        val col = (offset.x / cellWidth).toInt().coerceIn(0, 4)
        val row = (offset.y / cellHeight).toInt().coerceIn(0, 4)
        return MatrixGridPos(row, col)
    }

    fun isAdjacent(pos1: MatrixGridPos, pos2: MatrixGridPos): Boolean {
        val rowDiff = abs(pos1.row - pos2.row)
        val colDiff = abs(pos1.col - pos2.col)
        return rowDiff <= 1 && colDiff <= 1 && !(rowDiff == 0 && colDiff == 0)
    }

    fun handleDragStart(offset: Offset, canvasSize: Size) {
        if (engineState.playState != GamePlayState.PLAYING) return
        val pos = getCellFromOffset(offset, canvasSize) ?: return
        activeSelection.clear()
        activeSelection.add(pos)
        triggerGameHaptic(context, GameHapticType.LIGHT)
    }

    fun handleDragMove(offset: Offset, canvasSize: Size) {
        if (engineState.playState != GamePlayState.PLAYING) return
        val pos = getCellFromOffset(offset, canvasSize) ?: return
        val lastPos = activeSelection.lastOrNull() ?: return

        if (pos !in activeSelection && isAdjacent(lastPos, pos)) {
            activeSelection.add(pos)
            triggerGameHaptic(context, GameHapticType.LIGHT)
        }
    }

    fun handleDragEnd() {
        if (engineState.playState != GamePlayState.PLAYING) return
        if (activeSelection.isEmpty()) return

        val formedWord = activeSelection.map { currentLevel.grid[it.row][it.col] }.joinToString("")
        val reversedWord = formedWord.reversed()

        val matchedWord = when {
            formedWord in currentLevel.targetWords && formedWord !in foundWords -> formedWord
            reversedWord in currentLevel.targetWords && reversedWord !in foundWords -> reversedWord
            else -> null
        }

        if (matchedWord != null) {
            foundWords.add(matchedWord)
            val assignedColor = wordColors[(foundWords.size - 1) % wordColors.size]
            activeSelection.forEach { cell ->
                foundCellsMap[cell] = assignedColor
            }

            engineState.addScore(50)
            SfxManager.playSuccess(context)
            triggerGameHaptic(context, GameHapticType.SUCCESS)

            if (foundWords.size >= currentLevel.targetWords.size) {
                if (currentLevelIdx < levels.size - 1) {
                    currentLevelIdx++
                } else {
                    engineState.completeLevel(context, 3)
                }
            }
        } else {
            if (activeSelection.size > 1) {
                triggerGameHaptic(context, GameHapticType.ERROR)
            }
        }

        activeSelection.clear()
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
                title = t("مسار الكلمات", "Quranic Word Matrix"),
                level = currentLevelIdx + 1,
                score = engineState.score,
                lives = engineState.lives,
                timeRemainingMs = engineState.timeRemainingMs,
                onBack = onBack,
                onPause = { engineState.pauseGame() }
            )

            Spacer(Modifier.height(8.dp))

            // Category Title & Instruction Banner
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .noorGlassCard(cornerRadius = 16.dp),
                color = Color.Transparent
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = t("الموضوع: ", "Theme: ") + currentLevel.themeTitle,
                        color = theme.accent,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = t("اسحب أصبعك بين الحروف المجاورة لتشكيل الكلمات", "Drag across adjacent letters to form words"),
                        color = theme.textSecondary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // Target Words Chips Display
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.Center,
                maxItemsInEachRow = 4
            ) {
                currentLevel.targetWords.forEach { word ->
                    val isFound = word in foundWords
                    Surface(
                        modifier = Modifier.padding(4.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = if (isFound) theme.accent.copy(alpha = 0.25f) else theme.surface.copy(alpha = 0.6f),
                        border = if (isFound) BorderStroke(1.dp, theme.accent) else BorderStroke(1.dp, theme.surfaceVariant)
                    ) {
                        Text(
                            text = word,
                            color = if (isFound) theme.accent else theme.textPrimary,
                            fontSize = 13.sp,
                            fontWeight = if (isFound) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Current Active Selection Badge
            val activeWordPreview = activeSelection.map { currentLevel.grid[it.row][it.col] }.joinToString(" - ")
            Box(
                modifier = Modifier
                    .height(36.dp)
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (activeWordPreview.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = theme.accent,
                        tonalElevation = 4.dp
                    ) {
                        Text(
                            text = activeWordPreview,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // 5x5 Matrix Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp)
                    .noorGlassCard(cornerRadius = 24.dp)
                    .border(2.dp, theme.accent.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
            ) {
                var canvasSize by remember { mutableStateOf(Size.Zero) }

                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .gameGestureDetector(
                            onDragStart = { offset ->
                                handleDragStart(offset, canvasSize)
                            },
                            onDragMove = { offset, _ ->
                                handleDragMove(offset, canvasSize)
                            },
                            onDragEnd = {
                                handleDragEnd()
                            }
                        )
                ) {
                    canvasSize = size
                    val cellW = size.width / 5f
                    val cellH = size.height / 5f

                    // 1. Draw Grid Tiles
                    for (r in 0 until 5) {
                        for (c in 0 until 5) {
                            val pos = MatrixGridPos(r, c)
                            val left = c * cellW + 4f
                            val top = r * cellH + 4f
                            val w = cellW - 8f
                            val h = cellH - 8f

                            val isSelected = pos in activeSelection
                            val foundColor = foundCellsMap[pos]

                            val tileBg = when {
                                isSelected -> theme.accent.copy(alpha = 0.4f)
                                foundColor != null -> foundColor.copy(alpha = 0.35f)
                                else -> theme.surface.copy(alpha = 0.5f)
                            }

                            val tileBorderColor = when {
                                isSelected -> theme.accent
                                foundColor != null -> foundColor
                                else -> theme.surfaceVariant.copy(alpha = 0.4f)
                            }

                            drawRoundRect(
                                color = tileBg,
                                topLeft = Offset(left, top),
                                size = Size(w, h),
                                cornerRadius = CornerRadius(16f)
                            )
                            drawRoundRect(
                                color = tileBorderColor,
                                topLeft = Offset(left, top),
                                size = Size(w, h),
                                cornerRadius = CornerRadius(16f),
                                style = Stroke(width = if (isSelected || foundColor != null) 3f else 1f)
                            )
                        }
                    }

                    // 2. Draw Connecting Active Trace Path
                    if (activeSelection.size > 1) {
                        val path = Path()
                        activeSelection.forEachIndexed { index, gridPos ->
                            val cx = gridPos.col * cellW + cellW / 2f
                            val cy = gridPos.row * cellH + cellH / 2f
                            if (index == 0) {
                                path.moveTo(cx, cy)
                            } else {
                                path.lineTo(cx, cy)
                            }
                        }
                        drawPath(
                            path = path,
                            color = theme.accent,
                            style = Stroke(width = 8f, cap = StrokeCap.Round)
                        )
                    }

                    // 3. Render Particles
                    particles.draw(this)
                }

                // Overlay Text Characters on Top of Canvas
                Column(modifier = Modifier.fillMaxSize()) {
                    for (r in 0 until 5) {
                        Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            for (c in 0 until 5) {
                                val char = currentLevel.grid[r][c]
                                val pos = MatrixGridPos(r, c)
                                val isSelected = pos in activeSelection
                                val foundColor = foundCellsMap[pos]

                                Box(
                                    modifier = Modifier.weight(1f).fillMaxHeight(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = char.toString(),
                                        color = when {
                                            isSelected -> Color.White
                                            foundColor != null -> foundColor
                                            else -> theme.textPrimary
                                        },
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        when (engineState.playState) {
            GamePlayState.PAUSED -> PauseOverlay(
                onResume = { engineState.resumeGame() },
                onRestart = { initLevel() },
                onQuit = onBack
            )
            GamePlayState.WON -> GameWinOverlay(
                score = engineState.score,
                stars = engineState.starsEarned,
                onRestart = { initLevel() },
                onQuit = onBack
            )
            GamePlayState.LOST -> GameLoseOverlay(
                score = engineState.score,
                onRestart = { initLevel() },
                onQuit = onBack
            )
            else -> {}
        }
    }
}
