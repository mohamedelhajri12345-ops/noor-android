package com.elhajri.noor.games

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.elhajri.noor.theme.NoorThemeState
import com.elhajri.noor.ui.noorGlassCard
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

/**
 * Game 24: Letter Petal Wheel (زهرة الحروف)
 * Category: كلمات
 * Mechanic: Circular letter wheel word builder. Drag continuous line across letters on a flower wheel to assemble words in top crossword grid.
 */
private data class PetalWheelLevel(
    val id: Int,
    val hint: String,
    val letters: List<Char>,
    val targetWords: List<String>
)

@Composable
fun PetalWordWheelGame(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val theme = NoorThemeState.active
    val engineState = rememberGameEngineState(initialLevel = 1, initialLives = 3, timeLimitMs = 120000L, targetScore = 400)

    val levels = remember {
        listOf(
            PetalWheelLevel(
                id = 1,
                hint = "رموز وأدوات",
                letters = listOf('ق', 'ل', 'م', 'ر', 'ح'),
                targetWords = listOf("قلم", "رمل", "رحم", "قمر")
            ),
            PetalWheelLevel(
                id = 2,
                hint = "سكينة وطمأنينة",
                letters = listOf('س', 'ل', 'ا', 'م', 'ح'),
                targetWords = listOf("سلام", "سلاح", "ملح", "حلم")
            ),
            PetalWheelLevel(
                id = 3,
                hint = "علم ومعرفة",
                letters = listOf('ك', 'ت', 'ا', 'ب', 'ر'),
                targetWords = listOf("كتاب", "تارك", "رباك", "بارك")
            )
        )
    }

    var currentLevelIdx by remember { mutableIntStateOf(0) }
    val currentLevel = levels[currentLevelIdx.coerceIn(0, levels.size - 1)]

    val foundWords = remember { mutableStateListOf<String>() }
    val selectedIndices = remember { mutableStateListOf<Int>() }
    var currentTouchOffset by remember { mutableStateOf<Offset?>(null) }
    val particles = remember { ParticleSystem() }

    fun initLevel() {
        foundWords.clear()
        selectedIndices.clear()
        currentTouchOffset = null
        engineState.startGame()
    }

    LaunchedEffect(currentLevelIdx) {
        initLevel()
    }

    fun getPetalPositions(center: Offset, wheelRadius: Float): List<Offset> {
        val count = currentLevel.letters.size
        return currentLevel.letters.indices.map { i ->
            val angle = (i * 2 * PI / count) - (PI / 2)
            Offset(
                x = center.x + wheelRadius * cos(angle).toFloat(),
                y = center.y + wheelRadius * sin(angle).toFloat()
            )
        }
    }

    fun handleDragStart(startOffset: Offset, center: Offset, wheelRadius: Float) {
        if (engineState.playState != GamePlayState.PLAYING) return
        val petalPositions = getPetalPositions(center, wheelRadius)
        selectedIndices.clear()

        petalPositions.forEachIndexed { idx, pos ->
            if (hypot(startOffset.x - pos.x, startOffset.y - pos.y) <= 80f) {
                selectedIndices.add(idx)
                currentTouchOffset = startOffset
                triggerGameHaptic(context, GameHapticType.LIGHT)
                SfxManager.playTap(context)
            }
        }
    }

    fun handleDragMove(touchOffset: Offset, center: Offset, wheelRadius: Float) {
        if (engineState.playState != GamePlayState.PLAYING) return
        if (selectedIndices.isEmpty()) return

        currentTouchOffset = touchOffset
        val petalPositions = getPetalPositions(center, wheelRadius)

        petalPositions.forEachIndexed { idx, pos ->
            if (idx !in selectedIndices && hypot(touchOffset.x - pos.x, touchOffset.y - pos.y) <= 70f) {
                selectedIndices.add(idx)
                triggerGameHaptic(context, GameHapticType.LIGHT)
                SfxManager.playTap(context)
            }
        }
    }

    fun handleDragEnd() {
        if (engineState.playState != GamePlayState.PLAYING) return
        if (selectedIndices.isNotEmpty()) {
            val word = selectedIndices.map { currentLevel.letters[it] }.joinToString("")
            if (word in currentLevel.targetWords && word !in foundWords) {
                foundWords.add(word)
                engineState.addScore(100)
                particles.spawnBurst(currentTouchOffset ?: Offset(200f, 200f), count = 25)
                SfxManager.playSuccess(context)
                triggerGameHaptic(context, GameHapticType.SUCCESS)

                if (foundWords.size >= currentLevel.targetWords.size) {
                    if (currentLevelIdx < levels.size - 1) {
                        currentLevelIdx++
                    } else {
                        engineState.completeLevel(context, 3)
                    }
                }
            } else if (selectedIndices.size > 1) {
                triggerGameHaptic(context, GameHapticType.ERROR)
            }
        }

        selectedIndices.clear()
        currentTouchOffset = null
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
                title = t("زهرة الحروف", "Letter Petal Wheel"),
                level = currentLevelIdx + 1,
                score = engineState.score,
                lives = engineState.lives,
                timeRemainingMs = engineState.timeRemainingMs,
                onBack = onBack,
                onPause = { engineState.pauseGame() }
            )

            Spacer(Modifier.height(8.dp))

            // Instruction Banner
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
                        text = t("التصنيف: ", "Category: ") + currentLevel.hint,
                        color = theme.accent,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = t("صل بين أوراق الزهرة لتكوين الكلمات المطلوبة", "Connect petal letters to reveal target words"),
                        color = theme.textSecondary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Crossword Target Words Display
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                currentLevel.targetWords.forEach { targetWord ->
                    val isFound = targetWord in foundWords
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        targetWord.forEach { char ->
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(
                                        color = if (isFound) theme.accent.copy(alpha = 0.3f) else theme.surface.copy(alpha = 0.6f),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .border(
                                        width = 1.5.dp,
                                        color = if (isFound) theme.accent else theme.surfaceVariant,
                                        shape = RoundedCornerShape(10.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isFound) char.toString() else "؟",
                                    color = if (isFound) theme.accent else theme.textSecondary,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Live Assembling Word Preview Pill
            Box(
                modifier = Modifier
                    .height(40.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                val currentSpelled = selectedIndices.map { currentLevel.letters[it] }.joinToString("")
                if (currentSpelled.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = theme.accent,
                        tonalElevation = 6.dp
                    ) {
                        Text(
                            text = currentSpelled,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Petal Wheel Interactive Canvas Area
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
                                val center = Offset(canvasSize.width / 2f, canvasSize.height / 2f)
                                val wheelRadius = (canvasSize.width.coerceAtMost(canvasSize.height) / 2f) - 60f
                                handleDragStart(offset, center, wheelRadius)
                            },
                            onDragMove = { offset, _ ->
                                val center = Offset(canvasSize.width / 2f, canvasSize.height / 2f)
                                val wheelRadius = (canvasSize.width.coerceAtMost(canvasSize.height) / 2f) - 60f
                                handleDragMove(offset, center, wheelRadius)
                            },
                            onDragEnd = {
                                handleDragEnd()
                            }
                        )
                ) {
                    canvasSize = size
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val wheelRadius = (size.width.coerceAtMost(size.height) / 2f) - 60f
                    val petalPositions = getPetalPositions(center, wheelRadius)

                    // 1. Center Flower Hub Circle
                    drawCircle(
                        color = theme.surface.copy(alpha = 0.4f),
                        radius = wheelRadius + 40f,
                        center = center
                    )
                    drawCircle(
                        color = theme.accent.copy(alpha = 0.2f),
                        radius = wheelRadius + 40f,
                        center = center,
                        style = Stroke(width = 2f)
                    )

                    // 2. Draw Swipe Path
                    if (selectedIndices.isNotEmpty()) {
                        val path = Path()
                        selectedIndices.forEachIndexed { i, idx ->
                            val pt = petalPositions[idx]
                            if (i == 0) path.moveTo(pt.x, pt.y) else path.lineTo(pt.x, pt.y)
                        }
                        currentTouchOffset?.let { touchPt ->
                            path.lineTo(touchPt.x, touchPt.y)
                        }
                        drawPath(
                            path = path,
                            color = theme.accent,
                            style = Stroke(width = 8f, cap = StrokeCap.Round)
                        )
                    }

                    // 3. Draw Petal Nodes
                    petalPositions.forEachIndexed { idx, pos ->
                        val isSelected = idx in selectedIndices
                        val nodeColor = if (isSelected) theme.accent else theme.surface
                        val nodeBorder = if (isSelected) Color.White else theme.accent

                        drawCircle(
                            color = nodeColor,
                            radius = 32f,
                            center = pos
                        )
                        drawCircle(
                            color = nodeBorder,
                            radius = 32f,
                            center = pos,
                            style = Stroke(width = if (isSelected) 4f else 2f)
                        )
                    }

                    particles.draw(this)
                }

                // Render Letter Text Labels on top of Petal Nodes
                if (canvasSize != Size.Zero) {
                    val center = Offset(canvasSize.width / 2f, canvasSize.height / 2f)
                    val wheelRadius = (canvasSize.width.coerceAtMost(canvasSize.height) / 2f) - 60f
                    val petalPositions = getPetalPositions(center, wheelRadius)

                    petalPositions.forEachIndexed { idx, pos ->
                        val char = currentLevel.letters[idx]
                        val isSelected = idx in selectedIndices

                        Box(
                            modifier = Modifier
                                .offset(
                                    x = (pos.x / (LocalContext.current.resources.displayMetrics.density) - 24).dp,
                                    y = (pos.y / (LocalContext.current.resources.displayMetrics.density) - 24).dp
                                )
                                .size(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = char.toString(),
                                color = if (isSelected) Color.White else theme.textPrimary,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
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
