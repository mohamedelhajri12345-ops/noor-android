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
import com.elhajri.noor.settings.toArabicDigits
import com.elhajri.noor.theme.NoorThemeState
import com.elhajri.noor.ui.noorGlassCard
import kotlin.math.hypot

/**
 * Game 28: Word Chain Link (وصلة الكلمات)
 * Category: كلمات
 * Mechanic: Word connection chain puzzle. Drag connecting lines between word nodes where the last letter of Word A matches the first letter of Word B.
 */
data class ChainNode(
    val id: Int,
    val word: String,
    val normPos: Offset // Normalized 0..1 position on canvas
)

private data class WordChainLevel(
    val id: Int,
    val nodes: List<ChainNode>,
    val startNodeId: Int
)

@Composable
fun WordChainGame(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val theme = NoorThemeState.active
    val engineState = rememberGameEngineState(initialLevel = 1, initialLives = 3, timeLimitMs = 120000L, targetScore = 300)

    val levels = remember {
        listOf(
            WordChainLevel(
                id = 1,
                startNodeId = 0,
                nodes = listOf(
                    ChainNode(0, "شمس", Offset(0.25f, 0.25f)),
                    ChainNode(1, "سحاب", Offset(0.75f, 0.25f)),
                    ChainNode(2, "برق", Offset(0.75f, 0.72f)),
                    ChainNode(3, "قمر", Offset(0.25f, 0.72f))
                )
            ),
            WordChainLevel(
                id = 2,
                startNodeId = 0,
                nodes = listOf(
                    ChainNode(0, "سلام", Offset(0.2f, 0.2f)),
                    ChainNode(1, "منبر", Offset(0.8f, 0.2f)),
                    ChainNode(2, "ربيع", Offset(0.8f, 0.75f)),
                    ChainNode(3, "عبادة", Offset(0.2f, 0.75f))
                )
            ),
            WordChainLevel(
                id = 3,
                startNodeId = 0,
                nodes = listOf(
                    ChainNode(0, "قرآن", Offset(0.5f, 0.15f)),
                    ChainNode(1, "نبي", Offset(0.85f, 0.45f)),
                    ChainNode(2, "يسر", Offset(0.68f, 0.82f)),
                    ChainNode(3, "رحمة", Offset(0.32f, 0.82f)),
                    ChainNode(4, "تقوى", Offset(0.15f, 0.45f))
                )
            )
        )
    }

    var currentLevelIdx by remember { mutableIntStateOf(0) }
    val currentLevel = levels[currentLevelIdx.coerceIn(0, levels.size - 1)]

    val activeChain = remember { mutableStateListOf<Int>() }
    var dragTouchOffset by remember { mutableStateOf<Offset?>(null) }
    val particles = remember { ParticleSystem() }

    fun initLevel() {
        activeChain.clear()
        activeChain.add(currentLevel.startNodeId)
        dragTouchOffset = null
        engineState.startGame()
    }

    LaunchedEffect(currentLevelIdx) {
        initLevel()
    }

    fun isMatch(lastWord: String, nextWord: String): Boolean {
        if (lastWord.isEmpty() || nextWord.isEmpty()) return false
        val lastChar = lastWord.last()
        val firstChar = nextWord.first()
        if (lastChar == firstChar) return true
        // Handle Arabic letter variants (e.g., 'ة'/'ت', 'أ'/'ا')
        val normLast = when (lastChar) {
            'ة' -> 'ت'
            'أ', 'إ', 'آ' -> 'ا'
            else -> lastChar
        }
        val normFirst = when (firstChar) {
            'ة' -> 'ت'
            'أ', 'إ', 'آ' -> 'ا'
            else -> firstChar
        }
        return normLast == normFirst
    }

    fun getNodePixelPos(node: ChainNode, canvasSize: Size): Offset {
        return Offset(
            x = node.normPos.x * canvasSize.width,
            y = node.normPos.y * canvasSize.height
        )
    }

    fun handleDragStart(offset: Offset, canvasSize: Size) {
        if (engineState.playState != GamePlayState.PLAYING) return
        val tailNodeId = activeChain.lastOrNull() ?: return
        val tailNode = currentLevel.nodes.firstOrNull { it.id == tailNodeId } ?: return
        val tailPos = getNodePixelPos(tailNode, canvasSize)

        if (hypot(offset.x - tailPos.x, offset.y - tailPos.y) <= 90f) {
            dragTouchOffset = offset
            triggerGameHaptic(context, GameHapticType.LIGHT)
        }
    }

    fun handleDragMove(offset: Offset, canvasSize: Size) {
        if (engineState.playState != GamePlayState.PLAYING) return
        if (dragTouchOffset == null) return
        dragTouchOffset = offset

        val tailNodeId = activeChain.lastOrNull() ?: return
        val tailNode = currentLevel.nodes.firstOrNull { it.id == tailNodeId } ?: return

        currentLevel.nodes.forEach { candidateNode ->
            if (candidateNode.id !in activeChain) {
                val candidatePos = getNodePixelPos(candidateNode, canvasSize)
                if (hypot(offset.x - candidatePos.x, offset.y - candidatePos.y) <= 70f) {
                    if (isMatch(tailNode.word, candidateNode.word)) {
                        activeChain.add(candidateNode.id)
                        particles.spawnBurst(candidatePos, count = 20)
                        SfxManager.playSuccess(context)
                        triggerGameHaptic(context, GameHapticType.SUCCESS)

                        if (activeChain.size >= currentLevel.nodes.size) {
                            engineState.addScore(150)
                            if (currentLevelIdx < levels.size - 1) {
                                currentLevelIdx++
                            } else {
                                engineState.completeLevel(context, 3)
                            }
                        }
                    } else {
                        SfxManager.playWrong(context)
                        triggerGameHaptic(context, GameHapticType.ERROR)
                    }
                    dragTouchOffset = null
                }
            }
        }
    }

    fun handleDragEnd() {
        dragTouchOffset = null
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
                title = t("وصلة الكلمات", "Word Chain Link"),
                level = currentLevelIdx + 1,
                score = engineState.score,
                lives = engineState.lives,
                timeRemainingMs = engineState.timeRemainingMs,
                onBack = onBack,
                onPause = { engineState.pauseGame() }
            )

            Spacer(Modifier.height(8.dp))

            // Instruction Banner & Reset Button Row
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .noorGlassCard(cornerRadius = 16.dp),
                color = Color.Transparent
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = t("القاعدة: ", "Rule: ") + t("حرف نهاية الكلمة = بداية الكلمة التالية", "Last letter of word = start of next word"),
                            color = theme.accent,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = t("اسحب خطاً من نهاية السلسلة للكلمة المطابقة", "Drag line from chain tail to matching word"),
                            color = theme.textSecondary,
                            fontSize = 11.sp
                        )
                    }

                    IconButton(
                        onClick = { initLevel() },
                        modifier = Modifier
                            .background(theme.surface.copy(alpha = 0.8f), CircleShape)
                            .border(1.dp, theme.accent.copy(alpha = 0.4f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset Chain",
                            tint = theme.accent
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Active Chain Nodes Sequence Preview
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                activeChain.forEachIndexed { index, nodeId ->
                    val node = currentLevel.nodes.firstOrNull { it.id == nodeId }
                    if (node != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = theme.accent.copy(alpha = 0.3f),
                            border = BorderStroke(1.dp, theme.accent)
                        ) {
                            Text(
                                text = node.word,
                                color = theme.accent,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                        if (index < activeChain.size - 1) {
                            Text(
                                text = " ➔ ",
                                color = theme.textSecondary,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Word Chain Graph Canvas
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

                    // 1. Draw Existing Chain Connections
                    if (activeChain.size > 1) {
                        val path = Path()
                        activeChain.forEachIndexed { idx, nodeId ->
                            val node = currentLevel.nodes.first { it.id == nodeId }
                            val pos = getNodePixelPos(node, size)
                            if (idx == 0) path.moveTo(pos.x, pos.y) else path.lineTo(pos.x, pos.y)
                        }
                        drawPath(
                            path = path,
                            color = theme.accent,
                            style = Stroke(width = 8f, cap = StrokeCap.Round)
                        )
                    }

                    // 2. Draw Active Elastic Drag Line
                    val tailNodeId = activeChain.lastOrNull()
                    if (tailNodeId != null && dragTouchOffset != null) {
                        val tailNode = currentLevel.nodes.first { it.id == tailNodeId }
                        val tailPos = getNodePixelPos(tailNode, size)
                        dragTouchOffset?.let { touchPt ->
                            drawLine(
                                color = theme.accent.copy(alpha = 0.8f),
                                start = tailPos,
                                end = touchPt,
                                strokeWidth = 6f,
                                cap = StrokeCap.Round
                            )
                        }
                    }

                    // 3. Draw Nodes Background Circles
                    currentLevel.nodes.forEach { node ->
                        val pos = getNodePixelPos(node, size)
                        val isInChain = node.id in activeChain
                        val isTail = node.id == activeChain.lastOrNull()

                        val nodeBg = when {
                            isTail -> theme.accent
                            isInChain -> theme.accent.copy(alpha = 0.4f)
                            else -> theme.surface
                        }

                        val nodeBorder = when {
                            isTail -> Color.White
                            isInChain -> theme.accent
                            else -> theme.surfaceVariant
                        }

                        drawCircle(
                            color = nodeBg,
                            radius = 42f,
                            center = pos
                        )
                        drawCircle(
                            color = nodeBorder,
                            radius = 42f,
                            center = pos,
                            style = Stroke(width = if (isTail || isInChain) 4f else 2f)
                        )
                    }

                    particles.draw(this)
                }

                // Overlay Text Labels for Nodes
                if (canvasSize != Size.Zero) {
                    currentLevel.nodes.forEach { node ->
                        val pos = getNodePixelPos(node, canvasSize)
                        val isInChain = node.id in activeChain
                        val isTail = node.id == activeChain.lastOrNull()

                        val density = LocalContext.current.resources.displayMetrics.density
                        val xDp = (pos.x / density - 40).dp
                        val yDp = (pos.y / density - 40).dp

                        Box(
                            modifier = Modifier
                                .offset(x = xDp, y = yDp)
                                .size(80.dp)
                                .clickable {
                                    // Tapping tail node clears last if needed
                                    if (isInChain && !isTail && activeChain.size > 1) {
                                        val idx = activeChain.indexOf(node.id)
                                        if (idx >= 0) {
                                            while (activeChain.size > idx + 1) {
                                                activeChain.removeAt(activeChain.size - 1)
                                            }
                                            SfxManager.playTap(context)
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = node.word,
                                    color = if (isTail) Color.White else if (isInChain) theme.accent else theme.textPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                // Highlight start & end letters
                                if (node.word.length >= 2) {
                                    Text(
                                        text = "${node.word.first()}...${node.word.last()}",
                                        color = if (isTail) Color.White.copy(alpha = 0.8f) else theme.textSecondary,
                                        fontSize = 10.sp
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
