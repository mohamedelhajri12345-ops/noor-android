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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.audio.SfxManager
import com.elhajri.noor.games.engine.*
import com.elhajri.noor.settings.t
import com.elhajri.noor.theme.NoorThemeState
import com.elhajri.noor.ui.noorGlassCard
import kotlin.math.hypot

data class TracingLetter(
    val symbol: String,
    val nameAr: String,
    val points: List<Offset> // Normalized 0..1 coordinates
)

@Composable
fun CalligraphyTraceGame(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val theme = NoorThemeState.active
    val engineState = rememberGameEngineState(initialLevel = 1, initialLives = 3, targetScore = 100)

    val letters = remember {
        listOf(
            TracingLetter(
                symbol = "أ",
                nameAr = "حرف الألف",
                points = listOf(
                    Offset(0.5f, 0.2f), Offset(0.5f, 0.4f), Offset(0.5f, 0.6f), Offset(0.5f, 0.8f)
                )
            ),
            TracingLetter(
                symbol = "ب",
                nameAr = "حرف الباء",
                points = listOf(
                    Offset(0.2f, 0.5f), Offset(0.35f, 0.7f), Offset(0.65f, 0.7f), Offset(0.8f, 0.5f)
                )
            ),
            TracingLetter(
                symbol = "ن",
                nameAr = "حرف النون",
                points = listOf(
                    Offset(0.25f, 0.45f), Offset(0.35f, 0.75f), Offset(0.65f, 0.75f), Offset(0.75f, 0.45f)
                )
            ),
            TracingLetter(
                symbol = "م",
                nameAr = "حرف الميم",
                points = listOf(
                    Offset(0.6f, 0.4f), Offset(0.4f, 0.4f), Offset(0.4f, 0.55f), Offset(0.6f, 0.55f), Offset(0.3f, 0.85f)
                )
            ),
            TracingLetter(
                symbol = "نور",
                nameAr = "كلمة نور",
                points = listOf(
                    Offset(0.2f, 0.4f), Offset(0.3f, 0.7f), Offset(0.5f, 0.7f), Offset(0.7f, 0.4f), Offset(0.85f, 0.7f)
                )
            )
        )
    }

    var currentLetterIdx by remember { mutableIntStateOf(0) }
    var currentWaypointsHit by remember { mutableIntStateOf(0) }
    val userDrawnPoints = remember { mutableStateListOf<Offset>() }
    val particles = remember { ParticleSystem() }

    val currentLetter = letters[currentLetterIdx % letters.size]

    fun initGame() {
        currentLetterIdx = 0
        currentWaypointsHit = 0
        userDrawnPoints.clear()
        engineState.startGame()
    }

    LaunchedEffect(Unit) {
        initGame()
    }

    fun handleDrag(offset: Offset, boxWidth: Float, boxHeight: Float) {
        if (engineState.playState != GamePlayState.PLAYING) return
        userDrawnPoints.add(offset)

        val targetPoints = currentLetter.points.map {
            Offset(it.x * boxWidth, it.y * boxHeight)
        }

        if (currentWaypointsHit < targetPoints.size) {
            val nextWaypoint = targetPoints[currentWaypointsHit]
            val dist = hypot(offset.x - nextWaypoint.x, offset.y - nextWaypoint.y)

            if (dist < 60f) {
                currentWaypointsHit++
                triggerGameHaptic(context, GameHapticType.LIGHT)
                SfxManager.playTap(context)
                particles.spawnBurst(nextWaypoint, count = 15)

                if (currentWaypointsHit >= targetPoints.size) {
                    SfxManager.playSuccess(context)
                    engineState.addScore(20)

                    if (currentLetterIdx + 1 < letters.size) {
                        currentLetterIdx++
                        currentWaypointsHit = 0
                        userDrawnPoints.clear()
                    } else {
                        engineState.completeLevel(context, 3)
                    }
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
                title = t("خطاط النور", "Calligraphy Trace"),
                level = engineState.level,
                score = engineState.score,
                lives = engineState.lives,
                onBack = onBack,
                onPause = { engineState.pauseGame() }
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = "${currentLetter.nameAr} - ${t("تتبع النقاط الذهبية لتخطيط الحرف", "Trace golden waypoints to draw the letter")}",
                color = theme.textSecondary,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(Modifier.height(12.dp))

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp)
                    .noorGlassCard(cornerRadius = 24.dp)
                    .border(2.dp, theme.accent.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
            ) {
                val boxWidth = constraints.maxWidth.toFloat()
                val boxHeight = constraints.maxHeight.toFloat()

                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .gameGestureDetector(
                            onTap = { userDrawnPoints.clear() },
                            onDragStart = { offset ->
                                userDrawnPoints.clear()
                                handleDrag(offset, boxWidth, boxHeight)
                            },
                            onDragMove = { offset, _ ->
                                handleDrag(offset, boxWidth, boxHeight)
                            }
                        )
                ) {
                    val targetPoints = currentLetter.points.map { Offset(it.x * boxWidth, it.y * boxHeight) }

                    // Render guided stroke path
                    if (targetPoints.size >= 2) {
                        val guidedPath = Path().apply {
                            moveTo(targetPoints.first().x, targetPoints.first().y)
                            for (i in 1 until targetPoints.size) {
                                lineTo(targetPoints[i].x, targetPoints[i].y)
                            }
                        }

                        drawPath(
                            path = guidedPath,
                            color = Color.White.copy(alpha = 0.25f),
                            style = Stroke(width = 24f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )
                    }

                    // Render waypoint dots
                    targetPoints.forEachIndexed { idx, point ->
                        val isHit = idx < currentWaypointsHit
                        drawCircle(
                            color = if (isHit) Color(0xFF00E676) else theme.accent,
                            radius = if (isHit) 18f else 14f,
                            center = point
                        )
                    }

                    // Render user stroke
                    if (userDrawnPoints.size >= 2) {
                        val userPath = Path().apply {
                            moveTo(userDrawnPoints.first().x, userDrawnPoints.first().y)
                            for (i in 1 until userDrawnPoints.size) {
                                lineTo(userDrawnPoints[i].x, userDrawnPoints[i].y)
                            }
                        }

                        drawPath(
                            path = userPath,
                            color = Color(0xFFFFD700),
                            style = Stroke(width = 18f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )
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
