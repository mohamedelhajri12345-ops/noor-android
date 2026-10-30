package com.elhajri.noor.games

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.audio.SfxManager
import com.elhajri.noor.games.engine.*
import com.elhajri.noor.settings.t
import com.elhajri.noor.settings.toArabicDigits
import com.elhajri.noor.theme.NoorThemeState
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.noorGlassCard
import com.elhajri.noor.ui.themeScreenBackground
import kotlin.math.hypot

/**
 * Game 47: Gold Manuscript Scratch (كشف المخطوطة)
 * Category: أطفال
 * Mechanic: Swipe finger back and forth across a dusty screen canvas to brush away dust and reveal brilliant gold manuscript art underneath.
 */
private data class ManuscriptTitle(
    val titleAr: String,
    val subtitleAr: String,
    val primaryColor: Color,
    val accentColor: Color
)

private val MANUSCRIPTS = listOf(
    ManuscriptTitle("مخطوطة النور", "زخرفة إسلامية مذهبة", Color(0xFFFFD700), Color(0xFF1565C0)),
    ManuscriptTitle("المسجد الأقصى", "قبة الصخرة المشرفة", Color(0xFFFFB300), Color(0xFF2E7D32)),
    ManuscriptTitle("الكعبة المشرفة", "الكتابة الذهبية المباركة", Color(0xFFFFD54F), Color(0xFF212121)),
    ManuscriptTitle("زخرفة عريقة", "نقوش هندسية دقيقة", Color(0xFFFF8F00), Color(0xFF6A1B9A)),
    ManuscriptTitle("بسم الله الرحمن الرحيم", "خط عربي مزخرف", Color(0xFFFFC107), Color(0xFF00838F))
)

@Composable
fun ScratchRevealGame(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val theme = NoorThemeState.active
    val textMeasurer = rememberTextMeasurer()

    val engineState = rememberGameEngineState(
        initialLevel = 1,
        initialLives = 3,
        targetScore = 100
    )

    val particles = remember { ParticleSystem() }

    val gridRows = 12
    val gridCols = 12
    val totalCells = gridRows * gridCols

    var scratchGrid by remember { mutableStateOf(BooleanArray(totalCells) { false }) }
    var clearPercentage by remember { mutableFloatStateOf(0f) }
    var isAutoCompleted by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf("") }

    val currentManuscript = remember(engineState.level) {
        MANUSCRIPTS[(engineState.level - 1) % MANUSCRIPTS.size]
    }

    fun initGame() {
        engineState.startGame()
        scratchGrid = BooleanArray(totalCells) { false }
        clearPercentage = 0f
        isAutoCompleted = false
        statusText = t("مرر إصبعك لكشف المخطوطة الذهبية!", "Swipe your finger to reveal the golden manuscript!")
    }

    LaunchedEffect(engineState.level) {
        initGame()
    }

    // Canvas Game Loop for particle updates
    CanvasGameLoop(engineState) { deltaSec ->
        particles.update(deltaSec)
    }

    fun performScratch(touchPos: Offset, canvasSize: Size) {
        if (engineState.playState != GamePlayState.PLAYING || isAutoCompleted) return

        val cellW = canvasSize.width / gridCols
        val cellH = canvasSize.height / gridRows
        val radius = cellW * 1.5f

        var newScratchedCount = 0
        val updatedGrid = scratchGrid.clone()

        for (r in 0 until gridRows) {
            for (c in 0 until gridCols) {
                val idx = r * gridCols + c
                if (updatedGrid[idx]) {
                    newScratchedCount++
                } else {
                    val cx = c * cellW + cellW / 2f
                    val cy = r * cellH + cellH / 2f
                    if (hypot(touchPos.x - cx, touchPos.y - cy) < radius) {
                        updatedGrid[idx] = true
                        newScratchedCount++
                    }
                }
            }
        }

        if (newScratchedCount > scratchGrid.count { it }) {
            scratchGrid = updatedGrid
            clearPercentage = (newScratchedCount.toFloat() / totalCells) * 100f

            // Sparkle particles along scratch path
            particles.spawnBurst(
                origin = touchPos,
                count = 6,
                colors = listOf(Gold, currentManuscript.primaryColor, Color.White)
            )

            triggerGameHaptic(context, GameHapticType.LIGHT)

            // Auto-complete at 80%
            if (clearPercentage >= 80f && !isAutoCompleted) {
                isAutoCompleted = true
                scratchGrid = BooleanArray(totalCells) { true }
                clearPercentage = 100f

                SfxManager.playCelebration(context)
                triggerGameHaptic(context, GameHapticType.SUCCESS)
                engineState.addScore(100)

                particles.spawnBurst(
                    origin = Offset(canvasSize.width / 2f, canvasSize.height / 2f),
                    count = 45,
                    colors = listOf(Gold, GoldSoft, Color.White, currentManuscript.primaryColor)
                )

                statusText = t("مكتملة بنجاح! مخطوطة رائعة 🎉", "Fully Revealed! Beautiful Manuscript 🎉")
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
                title = t("كشف المخطوطة", "Scratch Reveal"),
                level = engineState.level,
                score = engineState.score,
                lives = engineState.lives,
                onBack = onBack,
                onPause = { engineState.pauseGame() }
            )

            // Progress Banner
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .noorGlassCard(cornerRadius = 12.dp),
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
                        text = statusText,
                        color = Gold,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${clearPercentage.toInt().toArabicDigits()}%",
                        color = GoldSoft,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Scratch Canvas
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp)
                    .noorGlassCard(cornerRadius = 24.dp)
            ) {
                var canvasSize by remember { mutableStateOf(Size.Zero) }

                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .gameGestureDetector(
                            onDragStart = { pos -> performScratch(pos, canvasSize) },
                            onDragMove = { pos, _ -> performScratch(pos, canvasSize) }
                        )
                ) {
                    canvasSize = size
                    val w = size.width
                    val h = size.height

                    // 1. UNDERLYING REVEALED ARTWORK (Manuscript)
                    drawRoundRect(
                        color = currentManuscript.accentColor,
                        topLeft = Offset(0f, 0f),
                        size = size,
                        cornerRadius = CornerRadius(20f)
                    )

                    // Gold Border Frame
                    drawRoundRect(
                        color = Gold,
                        topLeft = Offset(16f, 16f),
                        size = Size(w - 32f, h - 32f),
                        cornerRadius = CornerRadius(16f),
                        style = Stroke(width = 6f)
                    )
                    drawRoundRect(
                        color = GoldSoft,
                        topLeft = Offset(24f, 24f),
                        size = Size(w - 48f, h - 48f),
                        cornerRadius = CornerRadius(12f),
                        style = Stroke(width = 2f)
                    )

                    // Central Illuminated Arch
                    val archPath = Path().apply {
                        moveTo(w * 0.2f, h * 0.8f)
                        lineTo(w * 0.8f, h * 0.8f)
                        lineTo(w * 0.8f, h * 0.4f)
                        quadraticBezierTo(w * 0.5f, h * 0.12f, w * 0.2f, h * 0.4f)
                        close()
                    }
                    drawPath(archPath, color = Color.Black.copy(alpha = 0.3f))
                    drawPath(archPath, color = Gold, style = Stroke(width = 4f))

                    // Title Text Calligraphy
                    val titleLayout = textMeasurer.measure(
                        text = currentManuscript.titleAr,
                        style = TextStyle(
                            color = currentManuscript.primaryColor,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    drawText(
                        textLayoutResult = titleLayout,
                        topLeft = Offset(w / 2f - titleLayout.size.width / 2f, h * 0.45f)
                    )

                    val subLayout = textMeasurer.measure(
                        text = currentManuscript.subtitleAr,
                        style = TextStyle(
                            color = GoldSoft,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    drawText(
                        textLayoutResult = subLayout,
                        topLeft = Offset(w / 2f - subLayout.size.width / 2f, h * 0.56f)
                    )

                    // 2. SCRATCHABLE COVER OVERLAY
                    val cellW = w / gridCols
                    val cellH = h / gridRows

                    for (r in 0 until gridRows) {
                        for (c in 0 until gridCols) {
                            val idx = r * gridCols + c
                            if (!scratchGrid[idx]) {
                                val left = c * cellW
                                val top = r * cellH
                                // Dusty Gold Parchment Texture Overlay
                                drawRect(
                                    color = Color(0xFF37474F),
                                    topLeft = Offset(left, top),
                                    size = Size(cellW + 1f, cellH + 1f)
                                )
                                drawRect(
                                    color = Color(0xFF455A64).copy(alpha = 0.6f),
                                    topLeft = Offset(left + 2f, top + 2f),
                                    size = Size(cellW - 4f, cellH - 4f)
                                )
                            }
                        }
                    }

                    // Render particles
                    particles.draw(this)
                }
            }
        }

        // Standard Game Overlays
        if (engineState.playState == GamePlayState.PAUSED) {
            PauseOverlay(
                onResume = { engineState.resumeGame() },
                onRestart = { initGame() },
                onQuit = onBack
            )
        }

        if (engineState.playState == GamePlayState.WON) {
            GameWinOverlay(
                score = engineState.score,
                stars = engineState.starsEarned,
                onNextLevel = {
                    engineState.nextLevel()
                    initGame()
                },
                onRestart = { initGame() },
                onQuit = onBack
            )
        }

        if (engineState.playState == GamePlayState.LOST) {
            GameLoseOverlay(
                score = engineState.score,
                onRestart = { initGame() },
                onQuit = onBack
            )
        }
    }
}
