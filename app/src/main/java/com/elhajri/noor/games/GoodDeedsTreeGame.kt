package com.elhajri.noor.games

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.audio.SfxManager
import com.elhajri.noor.games.engine.*
import com.elhajri.noor.settings.t
import com.elhajri.noor.settings.toArabicDigits
import com.elhajri.noor.theme.NoorThemeState
import com.elhajri.noor.ui.noorGlassCard
import com.elhajri.noor.ui.themeScreenBackground
import kotlin.math.sin
import kotlin.random.Random

/**
 * Virtue & Thorn Leaf Specs
 */
private data class LeafSpec(
    val titleAr: String,
    val isVirtue: Boolean
)

private val VIRTUE_LEAVES = listOf(
    LeafSpec("الصدق", true),
    LeafSpec("البر", true),
    LeafSpec("الصدقة", true),
    LeafSpec("إفشاء السلام", true),
    LeafSpec("الابتسامة", true),
    LeafSpec("صلة الرحم", true),
    LeafSpec("مساعدة الضعيف", true),
    LeafSpec("الاستغفار", true)
)

private val THORN_LEAVES = listOf(
    LeafSpec("الغيبة", false),
    LeafSpec("الكذب", false),
    LeafSpec("النميمة", false),
    LeafSpec("الأذية", false)
)

/**
 * Active Falling Leaf runtime object
 */
private data class FallingLeaf(
    val id: Long,
    val spec: LeafSpec,
    var position: Offset,
    val speedY: Float,
    var swayPhase: Float = Random.nextFloat() * 6.28f,
    val color: Color
)

/**
 * Game 50: Virtue Tree Leaf Catcher (شجرة الحسنات)
 * Category: تربوية
 * Mechanic: Sliding a wicker basket left/right at bottom of screen to catch falling virtue leaves while dodging brown thorn leaves.
 */
@Composable
fun GoodDeedsTreeGame(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val theme = NoorThemeState.active
    val textMeasurer = rememberTextMeasurer()
    val engineState = rememberGameEngineState(initialLevel = 1, initialLives = 3, targetScore = 150)

    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    val particles = remember { ParticleSystem() }

    val activeLeaves = remember { mutableStateListOf<FallingLeaf>() }
    var nextLeafId by remember { mutableLongStateOf(0L) }
    var spawnTimerSec by remember { mutableFloatStateOf(0f) }
    var basketCenterX by remember { mutableFloatStateOf(200f) }
    var blossomProgress by remember { mutableFloatStateOf(0f) }

    fun spawnLeaf() {
        if (canvasSize.width <= 0) return
        val isVirtue = Random.nextFloat() < 0.75f
        val spec = if (isVirtue) VIRTUE_LEAVES.random() else THORN_LEAVES.random()
        val startX = 40f + Random.nextFloat() * (canvasSize.width - 80f)
        val leafColor = if (isVirtue) Color(0xFF4CAF50) else Color(0xFF8D6E63)
        val fallSpeed = 150f + (engineState.score / 15f) * 10f

        activeLeaves.add(
            FallingLeaf(
                id = ++nextLeafId,
                spec = spec,
                position = Offset(startX, -40f),
                speedY = fallSpeed,
                color = leafColor
            )
        )
    }

    fun resetGame() {
        activeLeaves.clear()
        spawnTimerSec = 0f
        blossomProgress = 0f
        if (canvasSize.width > 0) {
            basketCenterX = canvasSize.width / 2f
        }
        engineState.startGame()
    }

    LaunchedEffect(Unit) {
        resetGame()
    }

    // 60 FPS Game Loop Ticker
    CanvasGameLoop(engineState = engineState) { deltaSec ->
        particles.update(deltaSec)

        if (engineState.playState != GamePlayState.PLAYING) return@CanvasGameLoop

        // Update blossom progress
        blossomProgress = (engineState.score.toFloat() / engineState.targetScore).coerceIn(0f, 1f)

        // Spawning ticker
        spawnTimerSec += deltaSec
        if (spawnTimerSec >= 1.2f) {
            spawnTimerSec = 0f
            if (activeLeaves.size < 6) {
                spawnLeaf()
            }
        }

        val basketY = canvasSize.height.toFloat() - 110f
        val basketWidth = 140f
        val basketHeight = 45f
        val basketBox = AABBBox(
            left = basketCenterX - basketWidth / 2f,
            top = basketY,
            right = basketCenterX + basketWidth / 2f,
            bottom = basketY + basketHeight
        )

        val iterator = activeLeaves.iterator()
        while (iterator.hasNext()) {
            val leaf = iterator.next()
            leaf.swayPhase += deltaSec * 3f
            val swayX = sin(leaf.swayPhase) * 40f * deltaSec
            leaf.position = Offset(leaf.position.x + swayX, leaf.position.y + leaf.speedY * deltaSec)

            val leafBox = AABBBox(
                left = leaf.position.x - 30f,
                top = leaf.position.y - 20f,
                right = leaf.position.x + 30f,
                bottom = leaf.position.y + 20f
            )

            // Collision check with basket
            if (basketBox.intersects(leafBox)) {
                if (leaf.spec.isVirtue) {
                    // Caught Virtue Leaf!
                    val points = 10 + (engineState.comboCount * 2)
                    engineState.addScore(points)
                    SfxManager.playSuccess(context)
                    triggerGameHaptic(context, GameHapticType.SUCCESS)

                    particles.spawnBurst(
                        origin = leaf.position,
                        count = 18,
                        colors = listOf(Color(0xFF81C784), Color(0xFFFFD700), Color.White)
                    )

                    if (engineState.score >= engineState.targetScore) {
                        engineState.completeLevel(context, 3)
                    }
                } else {
                    // Caught Thorn Leaf!
                    SfxManager.playWrong(context)
                    triggerGameHaptic(context, GameHapticType.ERROR)

                    particles.spawnBurst(
                        origin = leaf.position,
                        count = 15,
                        colors = listOf(Color(0xFFFF5252), Color(0xFFD32F2F))
                    )

                    val lost = engineState.loseLife()
                    if (lost) {
                        engineState.failLevel(context)
                    }
                }
                iterator.remove()
            } else if (leaf.position.y > canvasSize.height.toFloat() + 50f) {
                // Missed leaf hit bottom
                iterator.remove()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar
            GameHeaderBar(
                title = t("شجرة الحسنات", "Virtue Tree Leaf Catcher"),
                level = engineState.level,
                score = engineState.score,
                lives = engineState.lives,
                onBack = onBack,
                onPause = { engineState.pauseGame() }
            )

            // Main Canvas Arcade View
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .noorGlassCard(cornerRadius = 16.dp)
                    .onSizeChanged {
                        canvasSize = it
                        if (basketCenterX == 200f && it.width > 0) {
                            basketCenterX = it.width / 2f
                        }
                    }
                    .gameGestureDetector(
                        onDragStart = { offset ->
                            basketCenterX = offset.x
                        },
                        onDragMove = { currentOffset, _ ->
                            basketCenterX = currentOffset.x.coerceIn(70f, (canvasSize.width - 70).toFloat())
                        }
                    )
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    if (canvasSize.width <= 0) return@Canvas

                    val width = size.width
                    val height = size.height

                    // Draw Background Virtue Tree
                    val trunkX = width / 2f
                    val trunkWidth = 40f

                    // Draw Tree Trunk
                    drawRect(
                        color = Color(0xFF5D4037),
                        topLeft = Offset(trunkX - trunkWidth / 2f, height - 300f),
                        size = Size(trunkWidth, 200f)
                    )

                    // Draw Blossoming Tree Foliage Canopy
                    val canopyRadius = 120f + (blossomProgress * 50f)
                    val canopyCenter = Offset(trunkX, height - 320f)

                    drawCircle(
                        color = Color(0xFF2E7D32).copy(alpha = 0.85f),
                        radius = canopyRadius,
                        center = canopyCenter
                    )

                    // Draw Blossoming Flowers as score grows
                    val numFlowers = (blossomProgress * 12).toInt()
                    for (i in 0 until numFlowers) {
                        val angle = (i * (360f / 12)) * (Math.PI / 180f).toFloat()
                        val fx = canopyCenter.x + (canopyRadius * 0.65f) * sin(angle)
                        val fy = canopyCenter.y + (canopyRadius * 0.65f) * kotlin.math.cos(angle)
                        drawCircle(
                            color = Color(0xFFFF4081),
                            radius = 12f,
                            center = Offset(fx, fy)
                        )
                        drawCircle(
                            color = Color.Yellow,
                            radius = 5f,
                            center = Offset(fx, fy)
                        )
                    }

                    // Draw Falling Leaves
                    activeLeaves.forEach { leaf ->
                        val leafWidth = 70f
                        val leafHeight = 35f
                        val leafOffset = Offset(
                            leaf.position.x - leafWidth / 2f,
                            leaf.position.y - leafHeight / 2f
                        )

                        // Leaf Capsule Shape
                        drawRoundRect(
                            color = leaf.color,
                            topLeft = leafOffset,
                            size = Size(leafWidth, leafHeight),
                            cornerRadius = CornerRadius(16f, 16f)
                        )
                        drawRoundRect(
                            color = Color.White.copy(alpha = 0.7f),
                            topLeft = leafOffset,
                            size = Size(leafWidth, leafHeight),
                            cornerRadius = CornerRadius(16f, 16f),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                        )

                        // Leaf Arabic Label
                        val textResult = textMeasurer.measure(
                            text = leaf.spec.titleAr,
                            style = TextStyle(
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        drawText(
                            textLayoutResult = textResult,
                            topLeft = Offset(
                                leaf.position.x - textResult.size.width / 2f,
                                leaf.position.y - textResult.size.height / 2f
                            )
                        )
                    }

                    // Draw Wicker Catcher Basket at Bottom
                    val basketY = height - 110f
                    val basketWidth = 140f
                    val basketHeight = 45f
                    val basketOffset = Offset(basketCenterX - basketWidth / 2f, basketY)

                    drawRoundRect(
                        color = Color(0xFFFFB74D),
                        topLeft = basketOffset,
                        size = Size(basketWidth, basketHeight),
                        cornerRadius = CornerRadius(12f, 12f)
                    )
                    drawRoundRect(
                        color = Color(0xFFE65100),
                        topLeft = basketOffset,
                        size = Size(basketWidth, basketHeight),
                        cornerRadius = CornerRadius(12f, 12f),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
                    )

                    // Draw Basket Rim handle details
                    drawLine(
                        color = Color.White,
                        start = Offset(basketOffset.x, basketY + 8f),
                        end = Offset(basketOffset.x + basketWidth, basketY + 8f),
                        strokeWidth = 3f
                    )

                    // Draw Particles
                    particles.draw(this)
                }
            }

            // Bottom Drag Helper Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = t("اسحب السلة يميناً ويساراً لجمع أوراق الحسنات وتجنب الأذى", "Drag basket left and right to catch virtue leaves"),
                    color = theme.textSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Overlays
        when (engineState.playState) {
            GamePlayState.PAUSED -> {
                PauseOverlay(
                    onResume = { engineState.resumeGame() },
                    onRestart = { resetGame() },
                    onQuit = onBack
                )
            }
            GamePlayState.WON -> {
                GameWinOverlay(
                    score = engineState.score,
                    stars = engineState.starsEarned,
                    onNextLevel = {
                        engineState.nextLevel()
                        resetGame()
                    },
                    onRestart = { resetGame() },
                    onQuit = onBack
                )
            }
            GamePlayState.LOST -> {
                GameLoseOverlay(
                    score = engineState.score,
                    onRestart = { resetGame() },
                    onQuit = onBack
                )
            }
            else -> {}
        }
    }
}
