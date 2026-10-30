package com.elhajri.noor.games

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import kotlin.random.Random

/**
 * Game 16: Anachid Rhythm Tiles / Piano Anachid (نغمات الروح)
 * ID: game_16_piano_anachid
 * Category: مهارة (SKILL)
 * Mechanic: Falling lane rhythm tap (Piano Tiles style). Tap glowing light tiles as they descend along 4 vertical lanes in sync with rhythmic melodies.
 */
enum class AnachidRating {
    NONE, PERFECT, GREAT, MISS
}

data class FallingAnachidTile(
    val id: Long,
    val lane: Int, // 0..3
    var yRatio: Float, // 0.0 (top) to 1.1 (bottom)
    val lengthRatio: Float = 0.15f,
    val isGolden: Boolean = false,
    var hitState: AnachidRating = AnachidRating.NONE
)

data class RhythmTapFeedback(
    val text: String,
    val color: Color,
    val position: Offset,
    var alpha: Float = 1.0f
)

@Composable
fun PianoAnachidGame(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val theme = NoorThemeState.active
    val engineState = rememberGameEngineState(initialLevel = 1, initialLives = 3, timeLimitMs = 45000L, targetScore = 200)

    val tiles = remember { mutableStateListOf<FallingAnachidTile>() }
    val particles = remember { ParticleSystem() }
    var spawnTimer by remember { mutableFloatStateOf(0f) }
    var currentStreak by remember { mutableIntStateOf(0) }
    var feedback by remember { mutableStateOf<RhythmTapFeedback?>(null) }
    var nextTileId by remember { mutableLongStateOf(1L) }

    fun spawnTile() {
        val lane = Random.nextInt(4)
        val isGold = Random.nextFloat() < 0.25f
        tiles.add(
            FallingAnachidTile(
                id = nextTileId++,
                lane = lane,
                yRatio = -0.15f,
                isGolden = isGold
            )
        )
    }

    fun initGame() {
        tiles.clear()
        currentStreak = 0
        feedback = null
        spawnTimer = 0f
        repeat(3) { spawnTile() }
        engineState.startGame()
    }

    LaunchedEffect(Unit) {
        initGame()
    }

    fun handleLaneTap(laneIndex: Int, canvasWidth: Float, canvasHeight: Float) {
        if (engineState.playState != GamePlayState.PLAYING) return

        // Target hit line is at yRatio = 0.80f
        val targetYRatio = 0.80f
        val targetY = canvasHeight * targetYRatio

        // Find unhit tile in this lane closest to target line
        val candidate = tiles
            .filter { it.lane == laneIndex && it.hitState == AnachidRating.NONE }
            .minByOrNull { kotlin.math.abs(it.yRatio - targetYRatio) }

        val laneCenterX = (canvasWidth / 4f) * (laneIndex + 0.5f)

        if (candidate != null) {
            val diff = kotlin.math.abs(candidate.yRatio - targetYRatio)
            if (diff <= 0.08f) {
                // PERFECT Hit
                candidate.hitState = AnachidRating.PERFECT
                currentStreak++
                val bonus = if (candidate.isGolden) 30 else 20
                val pts = bonus + (currentStreak / 5) * 5
                engineState.addScore(pts)

                particles.spawnBurst(Offset(laneCenterX, targetY), count = 20)
                triggerGameHaptic(context, GameHapticType.LIGHT)
                SfxManager.playSuccess(context)

                // TODO: Play piano/anachid pitch tone corresponding to lane index (0..3)
                feedback = RhythmTapFeedback(
                    text = t("ممتاز! ✨", "PERFECT!"),
                    color = Color(0xFFFFD700),
                    position = Offset(laneCenterX, targetY - 40f)
                )

                if (engineState.score >= engineState.targetScore) {
                    engineState.completeLevel(context, 3)
                }
                return
            } else if (diff <= 0.15f) {
                // GREAT Hit
                candidate.hitState = AnachidRating.GREAT
                currentStreak++
                val pts = 10 + (currentStreak / 5) * 5
                engineState.addScore(pts)

                particles.spawnBurst(Offset(laneCenterX, targetY), count = 12)
                triggerGameHaptic(context, GameHapticType.LIGHT)
                SfxManager.playTap(context)

                // TODO: Play piano/anachid pitch tone corresponding to lane index (0..3)
                feedback = RhythmTapFeedback(
                    text = t("جيد! 🎵", "GREAT!"),
                    color = Color(0xFF00E676),
                    position = Offset(laneCenterX, targetY - 40f)
                )

                if (engineState.score >= engineState.targetScore) {
                    engineState.completeLevel(context, 3)
                }
                return
            }
        }

        // Tap on empty lane or miss timing
        currentStreak = 0
        triggerGameHaptic(context, GameHapticType.ERROR)
        SfxManager.playWrong(context)
        feedback = RhythmTapFeedback(
            text = t("تجاوز! ❌", "MISS!"),
            color = Color(0xFFFF5252),
            position = Offset(laneCenterX, targetY - 40f)
        )
        if (engineState.loseLife()) {
            engineState.failLevel(context)
        }
    }

    CanvasGameLoop(engineState = engineState) { deltaSec ->
        particles.update(deltaSec)

        // Fade feedback
        feedback?.let { fb ->
            fb.alpha -= deltaSec * 1.8f
            if (fb.alpha <= 0f) feedback = null
        }

        if (engineState.playState == GamePlayState.PLAYING) {
            val speed = 0.35f + (engineState.level * 0.05f) + (engineState.score / 200f) * 0.1f

            // Move tiles
            val iterator = tiles.iterator()
            while (iterator.hasNext()) {
                val t = iterator.next()
                if (t.hitState == AnachidRating.NONE) {
                    t.yRatio += speed * deltaSec

                    // Check if passed target zone unhit
                    if (t.yRatio > 0.95f) {
                        t.hitState = AnachidRating.MISS
                        currentStreak = 0
                        SfxManager.playWrong(context)
                        triggerGameHaptic(context, GameHapticType.ERROR)
                        if (engineState.loseLife()) {
                            engineState.failLevel(context)
                        }
                    }
                }
            }

            // Clean old tiles
            tiles.removeAll { it.yRatio > 1.2f || it.hitState == AnachidRating.PERFECT || it.hitState == AnachidRating.GREAT }

            // Spawning ticker
            spawnTimer += deltaSec
            val spawnInterval = (0.85f - (engineState.level * 0.08f)).coerceAtLeast(0.40f)
            if (spawnTimer >= spawnInterval) {
                spawnTimer = 0f
                if (tiles.count { it.hitState == AnachidRating.NONE } < 5) {
                    spawnTile()
                }
            }
        }
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
                title = t("نغمات الروح", "Anachid Rhythm Tiles"),
                level = engineState.level,
                score = engineState.score,
                lives = engineState.lives,
                timeRemainingMs = engineState.timeRemainingMs,
                onBack = onBack,
                onPause = { engineState.pauseGame() }
            )

            Spacer(Modifier.height(8.dp))

            // Instructions + Streak display
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = t("المس المربعات النورانية الهابطة فور وصولها لخط النغم!", "Tap falling light tiles when they reach the target line!"),
                    color = theme.textSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.weight(1f)
                )

                if (currentStreak > 1) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFFD700).copy(alpha = 0.2f))
                            .border(1.dp, Color(0xFFFFD700), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = t("تتابع: ${currentStreak.toArabicDigits()}🔥", "Combo: $currentStreak🔥"),
                            color = Color(0xFFFFD700),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Canvas Game Area
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

                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .gameGestureDetector(
                                onTap = { tapOffset ->
                                    val laneWidth = widthPx / 4f
                                    val tappedLane = (tapOffset.x / laneWidth).toInt().coerceIn(0, 3)
                                    handleLaneTap(tappedLane, widthPx, heightPx)
                                }
                            )
                    ) {
                        val laneWidth = widthPx / 4f
                        val targetY = heightPx * 0.80f

                        // Draw 4 vertical lane columns
                        for (i in 0 until 4) {
                            val x = i * laneWidth
                            // Divider lines
                            if (i > 0) {
                                drawLine(
                                    color = Color.White.copy(alpha = 0.12f),
                                    start = Offset(x, 0f),
                                    end = Offset(x, heightPx),
                                    strokeWidth = 2f
                                )
                            }
                        }

                        // Target hit zone bar
                        drawRect(
                            color = Color(0xFFFFD700).copy(alpha = 0.15f),
                            topLeft = Offset(0f, targetY - 25f),
                            size = Size(widthPx, 50f)
                        )
                        drawLine(
                            color = Color(0xFFFFD700).copy(alpha = 0.8f),
                            start = Offset(0f, targetY),
                            end = Offset(widthPx, targetY),
                            strokeWidth = 4f
                        )

                        // Draw falling tiles
                        tiles.forEach { tile ->
                            if (tile.hitState == AnachidRating.NONE) {
                                val laneX = tile.lane * laneWidth
                                val tileTop = tile.yRatio * heightPx
                                val tileHeight = tile.lengthRatio * heightPx

                                val tileColor = if (tile.isGolden) Color(0xFFFFD700) else theme.accent

                                drawRoundRect(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(tileColor, tileColor.copy(alpha = 0.7f))
                                    ),
                                    topLeft = Offset(laneX + 8f, tileTop),
                                    size = Size(laneWidth - 16f, tileHeight),
                                    cornerRadius = CornerRadius(16f)
                                )

                                // Glowing inner core
                                drawCircle(
                                    color = Color.White.copy(alpha = 0.9f),
                                    radius = 10f,
                                    center = Offset(laneX + laneWidth / 2f, tileTop + tileHeight / 2f)
                                )
                            }
                        }

                        // Particles overlay
                        particles.draw(this)
                    }

                    // Interactive touch targets at bottom of lanes
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .align(Alignment.BottomCenter)
                    ) {
                        val laneLabels = listOf("🎵 1", "🎶 2", "🎵 3", "🎶 4")
                        laneLabels.forEachIndexed { index, label ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .padding(4.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(theme.accent.copy(alpha = 0.2f))
                                    .border(1.5.dp, theme.accent.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                    .clickable {
                                        handleLaneTap(index, widthPx, heightPx)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = theme.textPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
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
