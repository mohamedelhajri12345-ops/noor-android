package com.elhajri.noor.games

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.hypot
import kotlin.random.Random

/**
 * Game 42: Simon Lantern Sequence (أنوار الفوانيس)
 * Category: أطفال
 * Mechanic: Watch 4 colorful hanging lanterns flash and chime in sequence, then repeat the sequence by tapping the lanterns.
 */
private data class LanternSpec(
    val id: Int,
    val nameAr: String,
    val color: Color,
    val glowColor: Color
)

private val LANTERN_SPECS = listOf(
    LanternSpec(0, "ياقوتي", Color(0xFFE53935), Color(0xFFFF8A80)), // Crimson Red
    LanternSpec(1, "زمردي", Color(0xFF43A047), Color(0xFFB9F6CA)), // Emerald Green
    LanternSpec(2, "سماوي", Color(0xFF1E88E5), Color(0xFF80D8FF)), // Sapphire Blue
    LanternSpec(3, "ذهبي", Color(0xFFFFB300), Color(0xFFFFE57F))  // Gold Amber
)

private enum class SimonState {
    SHOWING_SEQUENCE,
    USER_INPUT,
    ROUND_SUCCESS,
    WAITING_NEXT
}

@Composable
fun SimonLanternsGame(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val theme = NoorThemeState.active
    val coroutineScope = rememberCoroutineScope()
    val textMeasurer = rememberTextMeasurer()

    val engineState = rememberGameEngineState(
        initialLevel = 1,
        initialLives = 3,
        targetScore = 150
    )

    val particles = remember { ParticleSystem() }

    // Game Sequence State
    var sequence by remember { mutableStateOf(listOf<Int>()) }
    var userIndex by remember { mutableIntStateOf(0) }
    var simonState by remember { mutableStateOf(SimonState.SHOWING_SEQUENCE) }
    var activeLanternId by remember { mutableStateOf<Int?>(null) }
    var statusText by remember { mutableStateOf("") }

    // Sound chime helper
    fun playLanternChime(id: Int) {
        when (id) {
            0 -> SfxManager.playTap(context)
            1 -> SfxManager.playButton(context)
            2 -> SfxManager.playPageTurn(context)
            3 -> SfxManager.playTasbih(context)
            else -> SfxManager.playTap(context)
        }
    }

    // Play sequence coroutine loop
    fun playSequence() {
        simonState = SimonState.SHOWING_SEQUENCE
        userIndex = 0
        statusText = t("انتظر وشاهد ترتيب الأنوار...", "Watch the lantern sequence...")

        coroutineScope.launch {
            delay(800)
            val speedDelay = maxOf(300L, 700L - (engineState.level * 30L))
            for (lanternId in sequence) {
                if (engineState.playState != GamePlayState.PLAYING) return@launch
                activeLanternId = lanternId
                triggerGameHaptic(context, GameHapticType.LIGHT)
                playLanternChime(lanternId)
                delay(speedDelay)
                activeLanternId = null
                delay(180L)
            }
            if (engineState.playState == GamePlayState.PLAYING) {
                simonState = SimonState.USER_INPUT
                statusText = t("دورك الآن! اضغط الفوانيس بالترتيب", "Your turn! Tap lanterns in sequence")
            }
        }
    }

    // Start / Restart game round
    fun startNewGame() {
        engineState.startGame()
        val initialLength = 2 + engineState.level
        val newSeq = List(initialLength) { Random.nextInt(4) }
        sequence = newSeq
        playSequence()
    }

    LaunchedEffect(Unit) {
        startNewGame()
    }

    // Game loop for particles
    CanvasGameLoop(engineState) { deltaSec ->
        particles.update(deltaSec)
    }

    fun handleLanternTap(id: Int, position: Offset) {
        if (engineState.playState != GamePlayState.PLAYING || simonState != SimonState.USER_INPUT) return

        activeLanternId = id
        playLanternChime(id)
        triggerGameHaptic(context, GameHapticType.MEDIUM)

        coroutineScope.launch {
            delay(150)
            if (activeLanternId == id) activeLanternId = null
        }

        if (sequence[userIndex] == id) {
            // Correct step
            engineState.addScore(10)
            particles.spawnBurst(
                origin = position,
                count = 8,
                colors = listOf(LANTERN_SPECS[id].glowColor, Gold, Color.White)
            )

            userIndex++
            if (userIndex >= sequence.size) {
                // Round completed!
                simonState = SimonState.ROUND_SUCCESS
                SfxManager.playSuccess(context)
                triggerGameHaptic(context, GameHapticType.SUCCESS)
                engineState.addScore(30)
                statusText = t("ممتاز! إجابة صحيحة 🎉", "Excellent! Correct sequence 🎉")

                particles.spawnBurst(
                    origin = Offset(400f, 300f),
                    count = 35,
                    colors = listOf(Gold, GoldSoft, Color(0xFF00E676), Color(0xFF40C4FF))
                )

                coroutineScope.launch {
                    delay(1200)
                    if (sequence.size >= 12 || engineState.score >= engineState.targetScore) {
                        engineState.completeLevel(context, 3)
                    } else {
                        // Advance to next step in sequence
                        sequence = sequence + Random.nextInt(4)
                        playSequence()
                    }
                }
            }
        } else {
            // Wrong step
            SfxManager.playWrong(context)
            triggerGameHaptic(context, GameHapticType.ERROR)
            engineState.loseLife()
            statusText = t("حاول مرة أخرى!", "Try again!")

            if (engineState.lives <= 0) {
                engineState.loseLife()
            } else {
                simonState = SimonState.WAITING_NEXT
                coroutineScope.launch {
                    delay(1200)
                    playSequence()
                }
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
                title = t("أنوار الفوانيس", "Simon Lanterns"),
                level = engineState.level,
                score = engineState.score,
                lives = engineState.lives,
                onBack = onBack,
                onPause = { engineState.pauseGame() }
            )

            // Instruction Banner
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .noorGlassCard(cornerRadius = 12.dp),
                color = Color.Transparent
            ) {
                Text(
                    text = statusText,
                    color = Gold,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(10.dp)
                )
            }

            // Interactive Canvas Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp)
                    .noorGlassCard(cornerRadius = 24.dp)
                    .gameGestureDetector(
                        onTap = { tapOffset ->
                            if (simonState == SimonState.USER_INPUT) {
                                // Find tapped lantern
                                val canvasWidth = 800f // reference coordinate system
                                val centers = listOf(
                                    Offset(180f, 320f),
                                    Offset(340f, 320f),
                                    Offset(500f, 320f),
                                    Offset(660f, 320f)
                                )
                                // Convert tapOffset based on actual view size if needed or check distance
                            }
                        }
                    )
            ) {
                var boxSize by remember { mutableStateOf(Size.Zero) }

                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .gameGestureDetector(
                            onTap = { tapOffset ->
                                if (simonState == SimonState.USER_INPUT && boxSize.width > 0f) {
                                    val colWidth = boxSize.width / 4f
                                    val idx = (tapOffset.x / colWidth).toInt().coerceIn(0, 3)
                                    handleLanternTap(idx, tapOffset)
                                }
                            }
                        )
                ) {
                    boxSize = size
                    val w = size.width
                    val h = size.height

                    // Draw ceiling hanging wire
                    drawLine(
                        color = Gold.copy(alpha = 0.6f),
                        start = Offset(0f, 40f),
                        end = Offset(w, 40f),
                        strokeWidth = 4f
                    )

                    val spacing = w / 4f
                    val lanternPositions = List(4) { i ->
                        Offset(spacing * i + spacing / 2f, h * 0.45f)
                    }

                    // Draw Lanterns
                    lanternPositions.forEachIndexed { idx, centerPos ->
                        val spec = LANTERN_SPECS[idx]
                        val isActive = activeLanternId == idx
                        val scale = if (isActive) 1.15f else 1.0f

                        // Hanging string
                        drawLine(
                            color = GoldSoft,
                            start = Offset(centerPos.x, 40f),
                            end = Offset(centerPos.x, centerPos.y - 70f * scale),
                            strokeWidth = 3f
                        )

                        // Glow background when active
                        if (isActive) {
                            drawCircle(
                                color = spec.glowColor.copy(alpha = 0.45f),
                                radius = 90f * scale,
                                center = centerPos
                            )
                            drawCircle(
                                color = Color.White.copy(alpha = 0.3f),
                                radius = 60f * scale,
                                center = centerPos
                            )
                        }

                        // Lantern Top Cap / Dome
                        val capPath = Path().apply {
                            moveTo(centerPos.x - 30f * scale, centerPos.y - 50f * scale)
                            lineTo(centerPos.x + 30f * scale, centerPos.y - 50f * scale)
                            lineTo(centerPos.x + 15f * scale, centerPos.y - 70f * scale)
                            lineTo(centerPos.x - 15f * scale, centerPos.y - 70f * scale)
                            close()
                        }
                        drawPath(capPath, color = Gold)

                        // Lantern Glass Body
                        val bodyPath = Path().apply {
                            moveTo(centerPos.x - 35f * scale, centerPos.y - 45f * scale)
                            lineTo(centerPos.x + 35f * scale, centerPos.y - 45f * scale)
                            lineTo(centerPos.x + 45f * scale, centerPos.y)
                            lineTo(centerPos.x + 25f * scale, centerPos.y + 45f * scale)
                            lineTo(centerPos.x - 25f * scale, centerPos.y + 45f * scale)
                            lineTo(centerPos.x - 45f * scale, centerPos.y)
                            close()
                        }
                        drawPath(
                            path = bodyPath,
                            color = if (isActive) spec.glowColor else spec.color.copy(alpha = 0.85f)
                        )
                        drawPath(
                            path = bodyPath,
                            color = Gold,
                            style = Stroke(width = 3f * scale)
                        )

                        // Inner Flame / Light
                        drawCircle(
                            color = if (isActive) Color.White else Gold,
                            radius = if (isActive) 18f * scale else 10f * scale,
                            center = centerPos
                        )

                        // Bottom Base & Tassel
                        drawRect(
                            color = Gold,
                            topLeft = Offset(centerPos.x - 20f * scale, centerPos.y + 45f * scale),
                            size = Size(40f * scale, 10f * scale)
                        )
                        drawLine(
                            color = spec.color,
                            start = Offset(centerPos.x, centerPos.y + 55f * scale),
                            end = Offset(centerPos.x, centerPos.y + 80f * scale),
                            strokeWidth = 4f * scale
                        )

                        // Label
                        val labelLayout = textMeasurer.measure(
                            text = spec.nameAr,
                            style = TextStyle(
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        drawText(
                            textLayoutResult = labelLayout,
                            topLeft = Offset(
                                centerPos.x - labelLayout.size.width / 2f,
                                centerPos.y + 90f * scale
                            )
                        )
                    }

                    // Render particles
                    particles.draw(this)
                }
            }

            // Sequence Step Progress Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = t("الخطوات: ", "Steps: ") + "${userIndex.toArabicDigits()} / ${sequence.size.toArabicDigits()}",
                    color = GoldSoft,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Standard Game Overlays
        if (engineState.playState == GamePlayState.PAUSED) {
            PauseOverlay(
                onResume = { engineState.resumeGame() },
                onRestart = { startNewGame() },
                onQuit = onBack
            )
        }

        if (engineState.playState == GamePlayState.WON) {
            GameWinOverlay(
                score = engineState.score,
                stars = engineState.starsEarned,
                onNextLevel = {
                    engineState.nextLevel()
                    startNewGame()
                },
                onRestart = { startNewGame() },
                onQuit = onBack
            )
        }

        if (engineState.playState == GamePlayState.LOST) {
            GameLoseOverlay(
                score = engineState.score,
                onRestart = { startNewGame() },
                onQuit = onBack
            )
        }
    }
}
