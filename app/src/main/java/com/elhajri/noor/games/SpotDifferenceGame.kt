package com.elhajri.noor.games
import androidx.compose.ui.layout.onSizeChanged

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
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
import kotlin.math.hypot

/**
 * Game 40: Spot the Difference (افحص الصورة)
 * Category: أطفال
 * Mechanic: Side-by-side visual difference search. Compare two adjacent cartoon illustrations
 * of mosque courtyards and tap 5 missing/altered details.
 */
private data class SpotDifferenceItem(
    val id: Int,
    val xRatio: Float,
    val yRatio: Float,
    val radiusRatio: Float = 0.08f,
    var isFound: Boolean = false,
    val descriptionAr: String
)

@Composable
fun SpotDifferenceGame(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val theme = NoorThemeState.active
    val engineState = rememberGameEngineState(initialLevel = 1, initialLives = 3)
    var canvasSize by remember { mutableStateOf(Size.Zero) }
    val particles = remember { ParticleSystem() }

    var differences by remember(engineState.level) {
        val diffList = listOf(
            SpotDifferenceItem(1, 0.20f, 0.18f, 0.08f, false, "هلال المئذنة"),
            SpotDifferenceItem(2, 0.50f, 0.28f, 0.08f, false, "طائر القبة"),
            SpotDifferenceItem(3, 0.80f, 0.45f, 0.08f, false, "شعلة الفانوس"),
            SpotDifferenceItem(4, 0.35f, 0.65f, 0.08f, false, "نجمة القوس"),
            SpotDifferenceItem(5, 0.75f, 0.78f, 0.08f, false, "ثمار النخلة")
        )
        mutableStateOf(diffList)
    }

    var hintActiveId by remember { mutableStateOf<Int?>(null) }

    fun initGame() {
        differences = differences.map { it.copy(isFound = false) }
        hintActiveId = null
        particles.clear()
        engineState.startGame()
    }

    LaunchedEffect(engineState.level) {
        initGame()
    }

    val foundCount = differences.count { it.isFound }
    val totalCount = differences.size

    fun triggerHint() {
        val unfound = differences.filter { !it.isFound }
        if (unfound.isNotEmpty()) {
            hintActiveId = unfound.random().id
            triggerGameHaptic(context, GameHapticType.LIGHT)
            SfxManager.playTap(context)
        }
    }

    fun handleAlteredTap(tapOffset: Offset, canvasSize: Size) {
        if (engineState.playState != GamePlayState.PLAYING) return

        val w = canvasSize.width
        val h = canvasSize.height

        var hit = false
        differences = differences.map { diff ->
            if (!diff.isFound) {
                val targetX = diff.xRatio * w
                val targetY = diff.yRatio * h
                val targetR = diff.radiusRatio * w

                val dist = hypot(tapOffset.x - targetX, tapOffset.y - targetY)
                if (dist <= targetR * 1.5f) {
                    hit = true
                    particles.spawnBurst(Offset(targetX, targetY), count = 20, colors = listOf(Color(0xFFFFD700), Color.Red, Color.White))
                    triggerGameHaptic(context, GameHapticType.SUCCESS)
                    SfxManager.playSuccess(context)
                    engineState.addScore(20)
                    if (hintActiveId == diff.id) hintActiveId = null
                    diff.copy(isFound = true)
                } else diff
            } else diff
        }

        if (!hit) {
            triggerGameHaptic(context, GameHapticType.LIGHT)
        }

        if (differences.all { it.isFound }) {
            engineState.completeLevel(context, 3)
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
                title = t("افحص الصورة", "Spot the Difference"),
                level = engineState.level,
                score = engineState.score,
                lives = engineState.lives,
                onBack = onBack,
                onPause = { engineState.pauseGame() }
            )

            // Status bar & hint button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = t("الفروقات: ${foundCount.toArabicDigits()} / ${totalCount.toArabicDigits()}", "Found: $foundCount / $totalCount"),
                    color = theme.accent,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Button(
                    onClick = { triggerHint() },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.accent.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Lightbulb, contentDescription = null, tint = theme.accent, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(t("تلميح", "Hint"), color = theme.textPrimary, fontSize = 12.sp)
                }
            }

            // Split View: Top (Original Scene)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .noorGlassCard(cornerRadius = 20.dp)
                    .border(1.5.dp, theme.accent.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Background sky & mosque
                    drawRoundRect(Color(0xFF1B263B), cornerRadius = CornerRadius(20f))
                    // Dome
                    drawArc(
                        color = Color(0xFFD4AF37),
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = true,
                        topLeft = Offset(w * 0.35f, h * 0.25f),
                        size = Size(w * 0.3f, h * 0.35f)
                    )
                    // Minaret 1 (left)
                    drawRect(Color(0xFF415A77), topLeft = Offset(w * 0.15f, h * 0.15f), size = Size(w * 0.1f, h * 0.6f))
                    // Minaret Crescent
                    drawCircle(Color(0xFFFFD700), radius = w * 0.03f, center = Offset(w * 0.20f, h * 0.18f))

                    // Bird on Dome
                    drawCircle(Color.White, radius = w * 0.02f, center = Offset(w * 0.50f, h * 0.28f))

                    // Lantern right
                    drawRect(Color(0xFFE53935), topLeft = Offset(w * 0.78f, h * 0.42f), size = Size(w * 0.05f, h * 0.08f))
                    drawCircle(Color(0xFFFFEA00), radius = w * 0.02f, center = Offset(w * 0.80f, h * 0.45f))

                    // Arch star center
                    drawCircle(Color(0xFF00E676), radius = w * 0.025f, center = Offset(w * 0.35f, h * 0.65f))

                    // Palm tree dates
                    drawCircle(Color(0xFFFF8F00), radius = w * 0.03f, center = Offset(w * 0.75f, h * 0.78f))

                    // Found highlight indicators on original
                    differences.forEach { diff ->
                        if (diff.isFound) {
                            val cx = diff.xRatio * w
                            val cy = diff.yRatio * h
                            drawCircle(
                                color = Color.Red,
                                radius = diff.radiusRatio * w,
                                center = Offset(cx, cy),
                                style = Stroke(width = 3f)
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                        .align(Alignment.TopStart)
                ) {
                    Text(t("الصورة الأصلية", "Original Image"), color = Color.White, fontSize = 11.sp)
                }
            }

            // Split View: Bottom (Altered Scene)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .noorGlassCard(cornerRadius = 20.dp)
                    .border(1.5.dp, theme.accent.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .onSizeChanged { canvasSize = Size(it.width.toFloat(), it.height.toFloat()) }
                        .gameGestureDetector(
                            onTap = { offset ->
                                handleAlteredTap(offset, canvasSize = canvasSize)
                            }
                        )
                ) {
                    val w = size.width
                    val h = size.height

                    // Background sky & mosque
                    drawRoundRect(Color(0xFF1B263B), cornerRadius = CornerRadius(20f))
                    // Dome
                    drawArc(
                        color = Color(0xFFD4AF37),
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = true,
                        topLeft = Offset(w * 0.35f, h * 0.25f),
                        size = Size(w * 0.3f, h * 0.35f)
                    )
                    // Minaret 1 (left)
                    drawRect(Color(0xFF415A77), topLeft = Offset(w * 0.15f, h * 0.15f), size = Size(w * 0.1f, h * 0.6f))

                    // Diff 1: Missing Crescent in Altered if NOT found
                    val diff1 = differences.find { it.id == 1 }
                    if (diff1?.isFound == true) {
                        drawCircle(Color(0xFFFFD700), radius = w * 0.03f, center = Offset(w * 0.20f, h * 0.18f))
                    }

                    // Diff 2: Missing Bird on Dome in Altered if NOT found
                    val diff2 = differences.find { it.id == 2 }
                    if (diff2?.isFound == true) {
                        drawCircle(Color.White, radius = w * 0.02f, center = Offset(w * 0.50f, h * 0.28f))
                    }

                    // Lantern right
                    drawRect(Color(0xFFE53935), topLeft = Offset(w * 0.78f, h * 0.42f), size = Size(w * 0.05f, h * 0.08f))
                    // Diff 3: Missing Flame light in Altered if NOT found
                    val diff3 = differences.find { it.id == 3 }
                    if (diff3?.isFound == true) {
                        drawCircle(Color(0xFFFFEA00), radius = w * 0.02f, center = Offset(w * 0.80f, h * 0.45f))
                    }

                    // Diff 4: Missing Arch star in Altered if NOT found
                    val diff4 = differences.find { it.id == 4 }
                    if (diff4?.isFound == true) {
                        drawCircle(Color(0xFF00E676), radius = w * 0.025f, center = Offset(w * 0.35f, h * 0.65f))
                    }

                    // Diff 5: Missing Dates in Altered if NOT found
                    val diff5 = differences.find { it.id == 5 }
                    if (diff5?.isFound == true) {
                        drawCircle(Color(0xFFFF8F00), radius = w * 0.03f, center = Offset(w * 0.75f, h * 0.78f))
                    }

                    // Found highlight circles & hint ring
                    differences.forEach { diff ->
                        val cx = diff.xRatio * w
                        val cy = diff.yRatio * h
                        if (diff.isFound) {
                            drawCircle(
                                color = Color.Red,
                                radius = diff.radiusRatio * w,
                                center = Offset(cx, cy),
                                style = Stroke(width = 3.5f)
                            )
                        } else if (hintActiveId == diff.id) {
                            drawCircle(
                                color = Color(0xFFFFD700),
                                radius = diff.radiusRatio * w * 1.3f,
                                center = Offset(cx, cy),
                                style = Stroke(width = 4f)
                            )
                        }
                    }

                    particles.draw(this)
                }

                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .background(theme.accent.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                        .align(Alignment.TopStart)
                ) {
                    Text(t("ابحث وانقر الاختلاف هنا", "Find & Tap Differences Here"), color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(8.dp))
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
