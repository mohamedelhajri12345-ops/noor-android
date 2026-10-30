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

/**
 * Game 45: Duff Drum Beat / Duff Rhythm (إيقاع الدف)
 * ID: game_45_duff_rhythm
 * Category: أطفال (KIDS)
 * Mechanic: Single-touch frame drum rhythm pulse. Tap the central Duff drum visualizer exactly when expanding glowing light rings hit the outer drum rim.
 */
data class DuffPulseRing(
    val id: Long,
    var currentRadius: Float,
    val speed: Float,
    var isHit: Boolean = false,
    var isMissed: Boolean = false
)

data class DuffRatingFeedback(
    val text: String,
    val color: Color,
    var alpha: Float = 1.0f,
    var yOffset: Float = 0f
)

@Composable
fun DuffRhythmGame(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val theme = NoorThemeState.active
    val engineState = rememberGameEngineState(initialLevel = 1, initialLives = 3, timeLimitMs = 40000L, targetScore = 180)

    val rings = remember { mutableStateListOf<DuffPulseRing>() }
    val particles = remember { ParticleSystem() }
    var currentStreak by remember { mutableIntStateOf(0) }
    var feedback by remember { mutableStateOf<DuffRatingFeedback?>(null) }
    var spawnTimer by remember { mutableFloatStateOf(0f) }
    var nextRingId by remember { mutableLongStateOf(1L) }

    fun spawnRing(baseSpeed: Float) {
        rings.add(
            DuffPulseRing(
                id = nextRingId++,
                currentRadius = 15f,
                speed = baseSpeed
            )
        )
    }

    fun initGame() {
        rings.clear()
        currentStreak = 0
        feedback = null
        spawnTimer = 0f
        engineState.startGame()
    }

    LaunchedEffect(Unit) {
        initGame()
    }

    fun handleDrumTap(center: Offset, rimRadius: Float) {
        if (engineState.playState != GamePlayState.PLAYING) return

        // Find active unhit ring closest to drumRimRadius
        val candidate = rings
            .filter { !it.isHit && !it.isMissed }
            .minByOrNull { kotlin.math.abs(it.currentRadius - rimRadius) }

        if (candidate != null) {
            val diff = kotlin.math.abs(candidate.currentRadius - rimRadius)

            if (diff <= 25f) {
                // PERFECT Hit
                candidate.isHit = true
                currentStreak++
                val pts = 20 + (currentStreak / 3) * 5
                engineState.addScore(pts)

                particles.spawnBurst(center, count = 24)
                triggerGameHaptic(context, GameHapticType.SUCCESS)
                SfxManager.playSuccess(context)

                // TODO: Play authentic duff frame drum percussion sample on beat hit
                feedback = DuffRatingFeedback(
                    text = t("ممتاز! 🥁✨", "PERFECT! 🥁✨"),
                    color = Color(0xFFFFD700)
                )

                if (engineState.score >= engineState.targetScore) {
                    engineState.completeLevel(context, 3)
                }
                return
            } else if (diff <= 55f) {
                // GOOD Hit
                candidate.isHit = true
                currentStreak++
                val pts = 10 + (currentStreak / 3) * 5
                engineState.addScore(pts)

                particles.spawnBurst(center, count = 12)
                triggerGameHaptic(context, GameHapticType.LIGHT)
                SfxManager.playTasbih(context)

                // TODO: Play authentic duff frame drum percussion sample on beat hit
                feedback = DuffRatingFeedback(
                    text = t("جيد! 🥁", "GOOD! 🥁"),
                    color = Color(0xFF00E676)
                )

                if (engineState.score >= engineState.targetScore) {
                    engineState.completeLevel(context, 3)
                }
                return
            }
        }

        // Tap off timing or missed rhythm
        currentStreak = 0
        triggerGameHaptic(context, GameHapticType.ERROR)
        SfxManager.playWrong(context)
        feedback = DuffRatingFeedback(
            text = t("حاول ثانية! ❌", "TRY AGAIN! ❌"),
            color = Color(0xFFFF5252)
        )
        if (engineState.loseLife()) {
            engineState.failLevel(context)
        }
    }

    CanvasGameLoop(engineState = engineState) { deltaSec ->
        particles.update(deltaSec)

        // Feedback animation
        feedback?.let { fb ->
            fb.alpha -= deltaSec * 1.5f
            fb.yOffset -= deltaSec * 30f
            if (fb.alpha <= 0f) feedback = null
        }

        if (engineState.playState == GamePlayState.PLAYING) {
            val ringSpeed = 160f + (engineState.level * 30f) + (engineState.score / 20f) * 10f

            // Update pulse rings
            val iterator = rings.iterator()
            while (iterator.hasNext()) {
                val r = iterator.next()
                if (!r.isHit && !r.isMissed) {
                    r.currentRadius += r.speed * deltaSec

                    // Missed ring exceeded rim by margin
                    if (r.currentRadius > 260f) { // threshold will be validated against drum rim in canvas
                        r.isMissed = true
                        currentStreak = 0
                        SfxManager.playWrong(context)
                        triggerGameHaptic(context, GameHapticType.ERROR)
                        feedback = DuffRatingFeedback(
                            text = t("فاتك الإيقاع! ❌", "MISSED BEAT! ❌"),
                            color = Color(0xFFFF5252)
                        )
                        if (engineState.loseLife()) {
                            engineState.failLevel(context)
                        }
                    }
                }
            }

            // Cleanup inactive rings
            rings.removeAll { it.isHit || it.isMissed || it.currentRadius > 350f }

            // Spawning rhythm beats
            spawnTimer += deltaSec
            val spawnInterval = (1.4f - (engineState.level * 0.12f)).coerceAtLeast(0.65f)
            if (spawnTimer >= spawnInterval) {
                spawnTimer = 0f
                if (rings.count { !it.isHit && !it.isMissed } < 3) {
                    spawnRing(ringSpeed)
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
                title = t("إيقاع الدف", "Duff Drum Beat"),
                level = engineState.level,
                score = engineState.score,
                lives = engineState.lives,
                timeRemainingMs = engineState.timeRemainingMs,
                onBack = onBack,
                onPause = { engineState.pauseGame() }
            )

            Spacer(Modifier.height(8.dp))

            // Instructions & Combo bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = t("انقر على الدف تماماً عند ملامسة الحلقة المضيئة لإطاره الخارجي!", "Tap the drum right as expanding light rings touch the outer rim!"),
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
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = t("تتابع: ${currentStreak.toArabicDigits()}🔥", "Streak: $currentStreak🔥"),
                            color = Color(0xFFFFD700),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Main Drum Canvas Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp)
                    .noorGlassCard(cornerRadius = 24.dp)
                    .border(2.dp, theme.accent.copy(alpha = 0.4f), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val widthPx = constraints.maxWidth.toFloat()
                    val heightPx = constraints.maxHeight.toFloat()
                    val center = Offset(widthPx / 2f, heightPx / 2f)
                    val drumRimRadius = (kotlin.math.min(widthPx, heightPx) * 0.33f).coerceAtLeast(100f)

                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .gameGestureDetector(
                                onTap = { handleDrumTap(center, drumRimRadius) }
                            )
                    ) {
                        // Background arabesque aura
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(theme.accent.copy(alpha = 0.15f), Color.Transparent),
                                center = center,
                                radius = drumRimRadius * 1.6f
                            ),
                            radius = drumRimRadius * 1.6f,
                            center = center
                        )

                        // Outer Wooden Frame
                        drawCircle(
                            color = Color(0xFF8D6E63),
                            radius = drumRimRadius + 14f,
                            center = center
                        )
                        drawCircle(
                            color = Color(0xFF5D4037),
                            radius = drumRimRadius + 14f,
                            center = center,
                            style = Stroke(width = 8f)
                        )

                        // Inner Parchment Skin
                        drawCircle(
                            color = Color(0xFFFFF8E1),
                            radius = drumRimRadius,
                            center = center
                        )
                        drawCircle(
                            color = Color(0xFFD7CCC8),
                            radius = drumRimRadius,
                            center = center,
                            style = Stroke(width = 3f)
                        )

                        // Decorative inner pattern rings
                        drawCircle(
                            color = Color(0xFFFFD700).copy(alpha = 0.4f),
                            radius = drumRimRadius * 0.65f,
                            center = center,
                            style = Stroke(width = 2f)
                        )

                        // Brass cymbals / zils around rim
                        for (i in 0 until 8) {
                            val angle = (i * Math.PI / 4.0).toFloat()
                            val cx = center.x + (drumRimRadius + 7f) * kotlin.math.cos(angle)
                            val cy = center.y + (drumRimRadius + 7f) * kotlin.math.sin(angle)
                            drawCircle(
                                color = Color(0xFFFFD700),
                                radius = 7f,
                                center = Offset(cx, cy)
                            )
                        }

                        // Target Rim Ring (Glowing alignment target)
                        drawCircle(
                            color = Color(0xFFFFD700),
                            radius = drumRimRadius,
                            center = center,
                            style = Stroke(width = 4f)
                        )

                        // Expanding Light Pulse Rings
                        rings.forEach { pulse ->
                            if (!pulse.isHit && !pulse.isMissed) {
                                val ringAlpha = (1.0f - (pulse.currentRadius / (drumRimRadius * 1.4f))).coerceIn(0.1f, 1.0f)
                                drawCircle(
                                    color = theme.accent.copy(alpha = ringAlpha),
                                    radius = pulse.currentRadius,
                                    center = center,
                                    style = Stroke(width = 6f)
                                )
                                drawCircle(
                                    color = Color.White.copy(alpha = ringAlpha * 0.8f),
                                    radius = pulse.currentRadius,
                                    center = center,
                                    style = Stroke(width = 2f)
                                )
                            }
                        }

                        // Particles Overlay
                        particles.draw(this)
                    }

                    // Rating Feedback Text Overlay
                    feedback?.let { fb ->
                        Text(
                            text = fb.text,
                            color = fb.color.copy(alpha = fb.alpha.coerceIn(0f, 1f)),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .offset(y = (-60 + fb.yOffset).dp)
                        )
                    }

                    // Center Drum Touch Button
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .align(Alignment.Center)
                            .clip(CircleShape)
                            .clickable {
                                handleDrumTap(center, drumRimRadius)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🥁",
                            fontSize = 36.sp
                        )
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
