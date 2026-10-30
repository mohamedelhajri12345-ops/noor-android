package com.elhajri.noor.games
import androidx.compose.ui.layout.onSizeChanged

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
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

/**
 * Game 33: Zellige Color-by-Number (تلوين الزليج)
 * Category: متعة
 * Mechanic: Paint fill by number canvas. Tap a numbered color swatch from the bottom palette,
 * then tap matching numbered cells on an arabesque mosaic grid to paint them.
 */
private data class ColorSwatch(
    val number: Int,
    val color: Color,
    val nameAr: String
)

private data class ZelligeTile(
    val id: Int,
    val row: Int,
    val col: Int,
    val targetNumber: Int,
    val color: Color,
    var isPainted: Boolean = false
)

@Composable
fun ZelligeColorNumberGame(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val theme = NoorThemeState.active
    val engineState = rememberGameEngineState(initialLevel = 1, initialLives = 3)
    var canvasSize by remember { mutableStateOf(Size.Zero) }
    val textMeasurer = rememberTextMeasurer()
    val particles = remember { ParticleSystem() }

    val palette = remember {
        listOf(
            ColorSwatch(1, Color(0xFF1E88E5), "أزرق ملكي"),
            ColorSwatch(2, Color(0xFFFFD700), "ذهبي أندلسي"),
            ColorSwatch(3, Color(0xFF4CAF50), "أخضر زمردي"),
            ColorSwatch(4, Color(0xFFE53935), "أحمر ياقوتي"),
            ColorSwatch(5, Color(0xFF8E24AA), "أرجواني زاهي")
        )
    }

    var selectedSwatchNumber by remember { mutableIntStateOf(1) }
    var tiles by remember(engineState.level) {
        val size = 6 + (engineState.level - 1)
        val list = mutableListOf<ZelligeTile>()
        var id = 0
        for (r in 0 until size) {
            for (c in 0 until size) {
                val distFromCenter = Math.abs(r - size / 2) + Math.abs(c - size / 2)
                val targetNum = (distFromCenter % palette.size) + 1
                val swatch = palette.first { it.number == targetNum }

                list.add(
                    ZelligeTile(
                        id = id++,
                        row = r,
                        col = c,
                        targetNumber = targetNum,
                        color = swatch.color,
                        isPainted = false
                    )
                )
            }
        }
        mutableStateOf(list.toMutableList())
    }

    fun initGame() {
        tiles = tiles.map { it.copy(isPainted = false) }.toMutableList()
        selectedSwatchNumber = 1
        particles.clear()
        engineState.startGame()
    }

    LaunchedEffect(engineState.level) {
        initGame()
    }

    val totalTiles = tiles.size
    val paintedCount = tiles.count { it.isPainted }
    val completionPercentage = if (totalTiles > 0) (paintedCount * 100) / totalTiles else 0

    fun handleCellTap(tapOffset: Offset, canvasSize: Size) {
        if (engineState.playState != GamePlayState.PLAYING) return

        val size = 6 + (engineState.level - 1)
        val padding = 20f
        val availableW = canvasSize.width - padding * 2
        val availableH = canvasSize.height - padding * 2
        val cellW = availableW / size
        val cellH = availableH / size

        val col = ((tapOffset.x - padding) / cellW).toInt()
        val row = ((tapOffset.y - padding) / cellH).toInt()

        if (row in 0 until size && col in 0 until size) {
            val tile = tiles.find { it.row == row && it.col == col } ?: return
            if (!tile.isPainted) {
                if (tile.targetNumber == selectedSwatchNumber) {
                    tile.isPainted = true
                    triggerGameHaptic(context, GameHapticType.SUCCESS)
                    SfxManager.playSuccess(context)

                    val cellCenterX = padding + col * cellW + cellW / 2f
                    val cellCenterY = padding + row * cellH + cellH / 2f
                    particles.spawnBurst(Offset(cellCenterX, cellCenterY), count = 12, colors = listOf(tile.color, Color.White))

                    engineState.addScore(10)

                    if (tiles.all { it.isPainted }) {
                        engineState.completeLevel(context, 3)
                    }
                } else {
                    triggerGameHaptic(context, GameHapticType.ERROR)
                    SfxManager.playWrong(context)
                }
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
                title = t("تلوين الزليج", "Zellige Color-by-Number"),
                level = engineState.level,
                score = engineState.score,
                lives = engineState.lives,
                onBack = onBack,
                onPause = { engineState.pauseGame() }
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = t("نسبة الإكمال: ${completionPercentage.toArabicDigits()}%", "Progress: $completionPercentage%"),
                    color = theme.accent,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = t("اختر اللون ثم انقر المربع المطابق", "Pick a color then tap matching tile"),
                    color = theme.textSecondary,
                    fontSize = 12.sp
                )
            }

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
                            onTap = { offset ->
                                handleCellTap(offset, canvasSize = canvasSize)
                            }
                        )
                ) {
                    val gridSize = 6 + (engineState.level - 1)
                    val padding = 20f
                    val availableW = size.width - padding * 2
                    val availableH = size.height - padding * 2
                    val cellW = availableW / gridSize
                    val cellH = availableH / gridSize

                    tiles.forEach { tile ->
                        val left = padding + tile.col * cellW
                        val top = padding + tile.row * cellH

                        if (tile.isPainted) {
                            drawRoundRect(
                                color = tile.color,
                                topLeft = Offset(left + 2f, top + 2f),
                                size = Size(cellW - 4f, cellH - 4f),
                                cornerRadius = CornerRadius(8f)
                            )
                        } else {
                            drawRoundRect(
                                color = Color.White.copy(alpha = 0.08f),
                                topLeft = Offset(left + 2f, top + 2f),
                                size = Size(cellW - 4f, cellH - 4f),
                                cornerRadius = CornerRadius(8f)
                            )
                            drawRoundRect(
                                color = theme.accent.copy(alpha = 0.3f),
                                topLeft = Offset(left + 2f, top + 2f),
                                size = Size(cellW - 4f, cellH - 4f),
                                cornerRadius = CornerRadius(8f),
                                style = Stroke(width = 1.5f)
                            )

                            val numberStr = tile.targetNumber.toArabicDigits()
                            val textLayout = textMeasurer.measure(
                                text = numberStr,
                                style = TextStyle(
                                    color = theme.textPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            drawText(
                                textLayoutResult = textLayout,
                                topLeft = Offset(
                                    left + (cellW - textLayout.size.width) / 2f,
                                    top + (cellH - textLayout.size.height) / 2f
                                )
                            )
                        }
                    }

                    particles.draw(this)
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .noorGlassCard(cornerRadius = 20.dp),
                color = Color.Transparent
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    palette.forEach { swatch ->
                        val isSelected = swatch.number == selectedSwatchNumber
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(swatch.color)
                                .border(
                                    width = if (isSelected) 3.5.dp else 1.dp,
                                    color = if (isSelected) Color.White else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable {
                                    selectedSwatchNumber = swatch.number
                                    triggerGameHaptic(context, GameHapticType.LIGHT)
                                    SfxManager.playTap(context)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = swatch.number.toArabicDigits(),
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
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
