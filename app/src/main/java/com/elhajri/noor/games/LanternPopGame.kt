package com.elhajri.noor.games

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.audio.SfxManager
import com.elhajri.noor.games.engine.*
import com.elhajri.noor.settings.t
import com.elhajri.noor.theme.NoorThemeState
import com.elhajri.noor.ui.noorGlassCard
import kotlin.random.Random

/**
 * Game 10: Ramadan Lantern Pop (فوانيس رمضان)
 * Category: متعة
 * Mechanic: Fast tap reaction popping rising sky lanterns before they exit.
 */
data class RisingLantern(
    var body: CircleBody,
    var color: Color,
    val speed: Float,
    val isGolden: Boolean = false,
    var isPopped: Boolean = false
)

@Composable
fun LanternPopGame(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val theme = NoorThemeState.active
    val engineState = rememberGameEngineState(initialLevel = 1, initialLives = 3, timeLimitMs = 30000L, targetScore = 150)

    val lanterns = remember { mutableStateListOf<RisingLantern>() }
    val particles = remember { ParticleSystem() }

    fun spawnLantern() {
        val cx = 40f + Random.nextFloat() * 400f
        val cy = 680f
        val isGold = Random.nextFloat() < 0.2f
        lanterns.add(
            RisingLantern(
                body = CircleBody(center = Offset(cx, cy), radius = 26f),
                color = if (isGold) Color(0xFFFFD700) else theme.accent,
                speed = 120f + Random.nextFloat() * 150f,
                isGolden = isGold
            )
        )
    }

    fun initGame() {
        lanterns.clear()
        repeat(4) { spawnLantern() }
        engineState.startGame()
    }

    LaunchedEffect(Unit) {
        initGame()
    }

    fun handleTap(tapPos: Offset) {
        if (engineState.playState != GamePlayState.PLAYING) return

        var popped = false
        val iterator = lanterns.iterator()
        while (iterator.hasNext()) {
            val l = iterator.next()
            if (!l.isPopped && l.body.containsPoint(tapPos)) {
                l.isPopped = true
                popped = true
                particles.spawnBurst(l.body.center, count = 25)
                triggerGameHaptic(context, GameHapticType.LIGHT)
                SfxManager.playSuccess(context)

                val pts = if (l.isGolden) 20 else 10
                engineState.addScore(pts)

                if (engineState.score >= engineState.targetScore) {
                    engineState.completeLevel(context, 3)
                }
                break
            }
        }

        if (popped) {
            spawnLantern()
        }
    }

    CanvasGameLoop(engineState = engineState) { deltaSec ->
        particles.update(deltaSec)

        if (engineState.playState == GamePlayState.PLAYING) {
            val iterator = lanterns.iterator()
            while (iterator.hasNext()) {
                val l = iterator.next()
                if (!l.isPopped) {
                    l.body.center = Offset(l.body.center.x, l.body.center.y - l.speed * deltaSec)

                    // Missed lantern exit top
                    if (l.body.center.y < -30f) {
                        l.isPopped = true
                        if (engineState.loseLife()) {
                            engineState.failLevel(context)
                        }
                        spawnLantern()
                    }
                }
            }

            // Keep minimum lanterns
            if (lanterns.count { !it.isPopped } < 4) {
                spawnLantern()
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
                title = t("فوانيس رمضان", "Ramadan Lantern Pop"),
                level = engineState.level,
                score = engineState.score,
                lives = engineState.lives,
                timeRemainingMs = engineState.timeRemainingMs,
                onBack = onBack,
                onPause = { engineState.pauseGame() }
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = t("انقر بسرعة لفرقعة الفوانيس الصاعدة قبل أن تتعدى الشاشة!", "Fast tap rising lanterns before they escape the screen!"),
                color = theme.textSecondary,
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(Modifier.height(12.dp))

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
                        .gameGestureDetector(
                            onTap = { offset -> handleTap(offset) }
                        )
                ) {
                    lanterns.forEach { l ->
                        if (!l.isPopped) {
                            // Lantern body
                            drawRoundRect(
                                color = l.color,
                                topLeft = Offset(l.body.center.x - l.body.radius, l.body.center.y - l.body.radius),
                                size = Size(l.body.radius * 2f, l.body.radius * 2.2f),
                                cornerRadius = CornerRadius(12f)
                            )
                            // Flame center
                            drawCircle(
                                color = Color.White,
                                radius = 8f,
                                center = l.body.center
                            )
                        }
                    }

                    particles.draw(this)
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

private fun CircleBody.containsPoint(point: Offset): Boolean {
    val dx = center.x - point.x
    val dy = center.y - point.y
    return (dx * dx + dy * dy) <= (radius * radius)
}
