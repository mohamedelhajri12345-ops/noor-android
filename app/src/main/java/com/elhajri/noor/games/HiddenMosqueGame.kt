package com.elhajri.noor.games
import androidx.compose.ui.layout.onSizeChanged

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
import kotlin.math.hypot

/**
 * Game 41: Hidden Mosque Objects (البحث عن المعالم)
 * Category: أطفال
 * Mechanic: Hidden object discovery. Explore a detailed warm illustration of a mosque library/courtyard;
 * find and tap items listed in the bottom icon bar.
 */
private data class HiddenItem(
    val id: Int,
    val nameAr: String,
    val nameEn: String,
    val xRatio: Float,
    val yRatio: Float,
    val radiusRatio: Float = 0.08f,
    val color: Color,
    var isFound: Boolean = false
)

@Composable
fun HiddenMosqueGame(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val theme = NoorThemeState.active
    val engineState = rememberGameEngineState(initialLevel = 1, initialLives = 3)
    var canvasSize by remember { mutableStateOf(Size.Zero) }
    val particles = remember { ParticleSystem() }

    var items by remember(engineState.level) {
        val list = listOf(
            HiddenItem(1, "مخطوطة", "Manuscript", 0.25f, 0.35f, 0.08f, Color(0xFFFFD700)),
            HiddenItem(2, "مسبحة", "Rosary", 0.72f, 0.65f, 0.08f, Color(0xFF00E676)),
            HiddenItem(3, "بوصلة", "Compass", 0.48f, 0.22f, 0.08f, Color(0xFF8E24AA)),
            HiddenItem(4, "مفتاح ذهبي", "Golden Key", 0.18f, 0.78f, 0.08f, Color(0xFFFFAB00)),
            HiddenItem(5, "فانوس", "Lantern", 0.82f, 0.30f, 0.08f, Color(0xFFFF5252))
        )
        mutableStateOf(list)
    }

    var hintActiveId by remember { mutableStateOf<Int?>(null) }

    fun initGame() {
        items = items.map { it.copy(isFound = false) }
        hintActiveId = null
        particles.clear()
        engineState.startGame()
    }

    LaunchedEffect(engineState.level) {
        initGame()
    }

    val foundCount = items.count { it.isFound }
    val totalCount = items.size

    fun triggerHint() {
        val unfound = items.filter { !it.isFound }
        if (unfound.isNotEmpty()) {
            hintActiveId = unfound.random().id
            triggerGameHaptic(context, GameHapticType.LIGHT)
            SfxManager.playTap(context)
        }
    }

    fun handleSceneTap(tapOffset: Offset, canvasSize: Size) {
        if (engineState.playState != GamePlayState.PLAYING) return

        val w = canvasSize.width
        val h = canvasSize.height

        var hit = false
        items = items.map { item ->
            if (!item.isFound) {
                val cx = item.xRatio * w
                val cy = item.yRatio * h
                val targetR = item.radiusRatio * w

                val dist = hypot(tapOffset.x - cx, tapOffset.y - cy)
                if (dist <= targetR * 1.5f) {
                    hit = true
                    particles.spawnBurst(Offset(cx, cy), count = 25, colors = listOf(item.color, Color.White, Color(0xFFFFD700)))
                    triggerGameHaptic(context, GameHapticType.SUCCESS)
                    SfxManager.playSuccess(context)
                    engineState.addScore(20)
                    if (hintActiveId == item.id) hintActiveId = null
                    item.copy(isFound = true)
                } else item
            } else item
        }

        if (!hit) {
            triggerGameHaptic(context, GameHapticType.LIGHT)
        }

        if (items.all { it.isFound }) {
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
                title = t("البحث عن المعالم", "Hidden Mosque Objects"),
                level = engineState.level,
                score = engineState.score,
                lives = engineState.lives,
                onBack = onBack,
                onPause = { engineState.pauseGame() }
            )

            // Header status & hint button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = t("العناصر المكتشفة: ${foundCount.toArabicDigits()} / ${totalCount.toArabicDigits()}", "Discovered: $foundCount / $totalCount"),
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

            // Main Interactive Scene Canvas
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
                        .onSizeChanged { canvasSize = Size(it.width.toFloat(), it.height.toFloat()) }
                        .gameGestureDetector(
                            onTap = { offset ->
                                handleSceneTap(offset, canvasSize = canvasSize)
                            }
                        )
                ) {
                    val w = size.width
                    val h = size.height

                    // Mosque library interior background
                    drawRoundRect(Color(0xFF2C1B18), cornerRadius = CornerRadius(24f))

                    // Arch & bookshelves
                    drawArc(
                        color = Color(0xFF4A2810),
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = true,
                        topLeft = Offset(w * 0.1f, h * 0.05f),
                        size = Size(w * 0.8f, h * 0.4f)
                    )

                    // Bookshelves left and right
                    drawRect(Color(0xFF3E2723), topLeft = Offset(w * 0.05f, h * 0.25f), size = Size(w * 0.25f, h * 0.5f))
                    drawRect(Color(0xFF3E2723), topLeft = Offset(w * 0.70f, h * 0.25f), size = Size(w * 0.25f, h * 0.5f))

                    // Carpet bottom
                    drawRoundRect(
                        color = Color(0xFFB71C1C),
                        topLeft = Offset(w * 0.1f, h * 0.70f),
                        size = Size(w * 0.8f, h * 0.25f),
                        cornerRadius = CornerRadius(16f)
                    )

                    // Render hidden objects on scene
                    items.forEach { item ->
                        val cx = item.xRatio * w
                        val cy = item.yRatio * h
                        val r = item.radiusRatio * w

                        if (item.isFound) {
                            // Found state: Glowing halo
                            drawCircle(color = item.color.copy(alpha = 0.8f), radius = r, center = Offset(cx, cy))
                            drawCircle(color = Color.White, radius = r * 0.5f, center = Offset(cx, cy))
                            drawCircle(color = theme.accent, radius = r, center = Offset(cx, cy), style = Stroke(width = 3f))
                        } else {
                            // Unfound state: Subtle hidden objects embedded in scene
                            when (item.id) {
                                1 -> { // Scroll
                                    drawRoundRect(item.color, topLeft = Offset(cx - r, cy - r * 0.4f), size = Size(r * 2f, r * 0.8f), cornerRadius = CornerRadius(6f))
                                }
                                2 -> { // Rosary
                                    drawCircle(item.color, radius = r * 0.7f, center = Offset(cx, cy), style = Stroke(width = 4f))
                                }
                                3 -> { // Compass
                                    drawCircle(item.color, radius = r * 0.8f, center = Offset(cx, cy))
                                    drawCircle(Color.White, radius = r * 0.3f, center = Offset(cx, cy))
                                }
                                4 -> { // Golden Key
                                    drawRect(item.color, topLeft = Offset(cx - r * 0.6f, cy - r * 0.2f), size = Size(r * 1.2f, r * 0.4f))
                                    drawCircle(item.color, radius = r * 0.4f, center = Offset(cx - r * 0.6f, cy))
                                }
                                5 -> { // Lantern
                                    drawRoundRect(item.color, topLeft = Offset(cx - r * 0.5f, cy - r * 0.8f), size = Size(r, r * 1.6f), cornerRadius = CornerRadius(8f))
                                }
                            }

                            // Hint highlight
                            if (hintActiveId == item.id) {
                                drawCircle(
                                    color = Color(0xFFFFD700),
                                    radius = r * 1.4f,
                                    center = Offset(cx, cy),
                                    style = Stroke(width = 4f)
                                )
                            }
                        }
                    }

                    particles.draw(this)
                }
            }

            // Bottom Target Items Checklist
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .noorGlassCard(cornerRadius = 20.dp),
                color = Color.Transparent
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items.forEach { item ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(
                                        color = if (item.isFound) item.color.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .border(
                                        width = if (item.isFound) 2.dp else 1.dp,
                                        color = if (item.isFound) item.color else Color.White.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (item.isFound) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = item.color, modifier = Modifier.size(22.dp))
                                } else {
                                    Icon(Icons.Default.Search, contentDescription = null, tint = theme.textSecondary, modifier = Modifier.size(18.dp))
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = t(item.nameAr, item.nameEn),
                                color = if (item.isFound) theme.accent else theme.textSecondary,
                                fontSize = 11.sp,
                                fontWeight = if (item.isFound) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
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
