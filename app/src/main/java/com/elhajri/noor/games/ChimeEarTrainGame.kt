package com.elhajri.noor.games

import androidx.compose.animation.core.*
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.audio.SfxManager
import com.elhajri.noor.games.engine.*
import com.elhajri.noor.settings.t
import com.elhajri.noor.settings.toArabicDigits
import com.elhajri.noor.theme.NoorThemeState
import com.elhajri.noor.ui.noorGlassCard
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Game 54: Minaret Melodic Chimes / Chime Ear Train (أنغام المنار)
 * ID: game_54_chime_ear_train
 * Category: تربوية (EDUCATIONAL)
 * Mechanic: Audio pitch match & chime sequence. Listen to played pentatonic chime notes from minaret bells, then tap correct bells on screen to match the melodic tune sequence.
 */
data class MinaretBellNote(
    val id: Int, // 0..4
    val nameAr: String,
    val nameEn: String,
    val pitchLabel: String,
    val frequencyHz: Float,
    val color: Color
)

enum class ChimeRoundState {
    IDLE,
    SHOWING_SEQUENCE,
    USER_INPUT,
    ROUND_SUCCESS,
    ROUND_FAILED
}

@Composable
fun ChimeEarTrainGame(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val theme = NoorThemeState.active
    val coroutineScope = rememberCoroutineScope()
    val engineState = rememberGameEngineState(initialLevel = 1, initialLives = 3, timeLimitMs = 0L, targetScore = 150)

    val bells = remember {
        listOf(
            MinaretBellNote(0, "دو", "Do", "C4", 261.63f, Color(0xFFFFD700)),
            MinaretBellNote(1, "ري", "Re", "D4", 293.66f, Color(0xFF00E676)),
            MinaretBellNote(2, "مي", "Mi", "E4", 329.63f, Color(0xFF80D8FF)),
            MinaretBellNote(3, "صول", "Sol", "G4", 392.00f, Color(0xFFB388FF)),
            MinaretBellNote(4, "لا", "La", "A4", 440.00f, Color(0xFFFF80AB))
        )
    }

    val targetSequence = remember { mutableStateListOf<Int>() }
    val userSequence = remember { mutableStateListOf<Int>() }
    val particles = remember { ParticleSystem() }

    var roundState by remember { mutableStateOf(ChimeRoundState.IDLE) }
    var activeHighlightedBell by remember { mutableStateOf<Int?>(null) }
    var currentRound by remember { mutableIntStateOf(1) }

    fun playSequence() {
        coroutineScope.launch {
            roundState = ChimeRoundState.SHOWING_SEQUENCE
            userSequence.clear()
            delay(500)

            for (noteIndex in targetSequence) {
                activeHighlightedBell = noteIndex
                triggerGameHaptic(context, GameHapticType.LIGHT)
                SfxManager.playTap(context)

                // TODO: Synthesize pentatonic minaret chime tone frequency (e.g. ${bells[noteIndex].frequencyHz} Hz)

                delay(550)
                activeHighlightedBell = null
                delay(200)
            }

            roundState = ChimeRoundState.USER_INPUT
        }
    }

    fun startNextRound() {
        if (targetSequence.isEmpty()) {
            targetSequence.addAll(listOf(Random.nextInt(5), Random.nextInt(5), Random.nextInt(5)))
        } else {
            targetSequence.add(Random.nextInt(5))
        }
        playSequence()
    }

    fun initGame() {
        targetSequence.clear()
        userSequence.clear()
        currentRound = 1
        activeHighlightedBell = null
        engineState.startGame()
        startNextRound()
    }

    LaunchedEffect(Unit) {
        initGame()
    }

    fun handleBellTap(bellId: Int) {
        if (roundState != ChimeRoundState.USER_INPUT || engineState.playState != GamePlayState.PLAYING) return

        userSequence.add(bellId)
        activeHighlightedBell = bellId
        triggerGameHaptic(context, GameHapticType.LIGHT)
        SfxManager.playTap(context)

        // TODO: Synthesize pentatonic minaret chime tone frequency for bell $bellId (${bells[bellId].frequencyHz} Hz)

        coroutineScope.launch {
            delay(250)
            if (activeHighlightedBell == bellId) {
                activeHighlightedBell = null
            }
        }

        val currentIndex = userSequence.size - 1

        // Check if correct note tapped
        if (userSequence[currentIndex] != targetSequence[currentIndex]) {
            // WRONG Note
            roundState = ChimeRoundState.ROUND_FAILED
            triggerGameHaptic(context, GameHapticType.ERROR)
            SfxManager.playWrong(context)

            coroutineScope.launch {
                delay(800)
                if (engineState.loseLife()) {
                    engineState.failLevel(context)
                } else {
                    // Replay sequence after error
                    playSequence()
                }
            }
            return
        }

        // Check if full sequence completed
        if (userSequence.size == targetSequence.size) {
            roundState = ChimeRoundState.ROUND_SUCCESS
            val roundPts = 20 + targetSequence.size * 10
            engineState.addScore(roundPts)

            particles.spawnBurst(Offset(250f, 300f), count = 30)
            triggerGameHaptic(context, GameHapticType.SUCCESS)
            SfxManager.playLevelUp(context)

            coroutineScope.launch {
                delay(1000)
                if (engineState.score >= engineState.targetScore || targetSequence.size >= 8) {
                    engineState.completeLevel(context, 3)
                } else {
                    currentRound++
                    startNextRound()
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
                title = t("أنغام المنار", "Minaret Melodic Chimes"),
                level = engineState.level,
                score = engineState.score,
                lives = engineState.lives,
                timeRemainingMs = engineState.timeRemainingMs,
                onBack = onBack,
                onPause = { engineState.pauseGame() }
            )

            Spacer(Modifier.height(8.dp))

            // Status Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .noorGlassCard(cornerRadius = 16.dp)
                    .border(1.dp, theme.accent.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    .padding(vertical = 10.dp, horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                val statusText = when (roundState) {
                    ChimeRoundState.SHOWING_SEQUENCE -> t("🎼 استمع بتركيز إلى أنغام أجراس المنار...", "🎼 Listen closely to minaret chime notes...")
                    ChimeRoundState.USER_INPUT -> t("✨ دورك الآن! اضغط على الأجراس حسب الترتيب (${userSequence.size.toArabicDigits()}/${targetSequence.size.toArabicDigits()})", "✨ Your turn! Tap bells in sequence (${userSequence.size}/${targetSequence.size})")
                    ChimeRoundState.ROUND_SUCCESS -> t("🎉 ممتاز! أتقنت متتالية الأنغام!", "🎉 Excellent! Matched tune sequence!")
                    ChimeRoundState.ROUND_FAILED -> t("❌ نغم خاطئ! أعد المحاولة...", "❌ Wrong note! Try again...")
                    else -> t("استعد...", "Get Ready...")
                }
                val statusColor = when (roundState) {
                    ChimeRoundState.SHOWING_SEQUENCE -> Color(0xFFFFD700)
                    ChimeRoundState.USER_INPUT -> theme.textPrimary
                    ChimeRoundState.ROUND_SUCCESS -> Color(0xFF00E676)
                    ChimeRoundState.ROUND_FAILED -> Color(0xFFFF5252)
                    else -> theme.textSecondary
                }

                Text(
                    text = statusText,
                    color = statusColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(12.dp))

            // Minaret Arch & Bells Display
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp)
                    .noorGlassCard(cornerRadius = 24.dp)
                    .border(2.dp, theme.accent.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
            ) {
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val widthPx = constraints.maxWidth.toFloat()
                    val heightPx = constraints.maxHeight.toFloat()

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // Minaret Background Silhouettes & Crescent
                        val minaretCenter = Offset(widthPx / 2f, heightPx * 0.25f)
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFFFFD700).copy(alpha = 0.15f), Color.Transparent),
                                center = minaretCenter,
                                radius = 220f
                            ),
                            radius = 220f,
                            center = minaretCenter
                        )

                        // Crescent Moon Icon
                        drawCircle(
                            color = Color(0xFFFFD700).copy(alpha = 0.8f),
                            radius = 28f,
                            center = minaretCenter
                        )
                        drawCircle(
                            color = Color(0xFF0B1D19),
                            radius = 24f,
                            center = Offset(minaretCenter.x + 10f, minaretCenter.y - 6f)
                        )

                        // Glowing Connecting Arch Line for Bells
                        drawCircle(
                            color = theme.accent.copy(alpha = 0.3f),
                            radius = widthPx * 0.38f,
                            center = Offset(widthPx / 2f, heightPx * 0.55f),
                            style = Stroke(width = 3f)
                        )

                        // Highlight aura for active bell
                        activeHighlightedBell?.let { bellIdx ->
                            val arcAngle = Math.PI * (0.2 + bellIdx * 0.15)
                            val bx = widthPx / 2f + (widthPx * 0.38f) * kotlin.math.cos(arcAngle).toFloat()
                            val by = heightPx * 0.55f + (widthPx * 0.38f) * kotlin.math.sin(arcAngle).toFloat()

                            drawCircle(
                                color = bells[bellIdx].color.copy(alpha = 0.5f),
                                radius = 60f,
                                center = Offset(bx, by)
                            )
                        }

                        // Particles Overlay
                        particles.draw(this)
                    }

                    // 5 Minaret Bells arranged in arch
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Bottom,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Sequence dots progress
                        Row(
                            modifier = Modifier.padding(bottom = 20.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            targetSequence.forEachIndexed { idx, _ ->
                                val isDone = idx < userSequence.size
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 4.dp)
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isDone) Color(0xFF00E676)
                                            else if (idx == userSequence.size && roundState == ChimeRoundState.USER_INPUT) Color(0xFFFFD700)
                                            else Color.White.copy(alpha = 0.25f)
                                        )
                                )
                            }
                        }

                        // Bells buttons grid/row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            bells.forEach { bell ->
                                val isActive = activeHighlightedBell == bell.id
                                val bellScale = if (isActive) 1.15f else 1.0f

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(
                                            if (isActive) bell.color.copy(alpha = 0.35f)
                                            else Color.White.copy(alpha = 0.08f)
                                        )
                                        .border(
                                            width = if (isActive) 3.dp else 1.5.dp,
                                            color = if (isActive) bell.color else bell.color.copy(alpha = 0.5f),
                                            shape = RoundedCornerShape(20.dp)
                                        )
                                        .clickable {
                                            handleBellTap(bell.id)
                                        }
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = "🔔",
                                        fontSize = if (isActive) 32.sp else 26.sp
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = bell.nameAr,
                                        color = bell.color,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = bell.pitchLabel,
                                        color = theme.textSecondary,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                    }
                }
            }
        }

        // Overlays
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
