package com.elhajri.noor.games

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
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
import kotlin.math.abs
import kotlin.random.Random

/**
 * Charity Basket Category definitions
 */
private enum class CharityCategory(val titleAr: String, val color: Color) {
    FOOD("طعام", Color(0xFFFFB74D)),       // Warm Orange/Gold
    CLOTHING("كسوة", Color(0xFF64B5F6)),   // Soft Blue
    EDUCATION("كتب", Color(0xFF81C784)),  // Soft Green
    TOYS("ألعاب", Color(0xFFBA68C8))       // Soft Purple
}

/**
 * Item specification template for charity sorting
 */
private data class CharityItemSpec(
    val nameAr: String,
    val category: CharityCategory
)

private val CHARITY_ITEMS_CATALOG = listOf(
    CharityItemSpec("خبز طازج", CharityCategory.FOOD),
    CharityItemSpec("تمر وسكر", CharityCategory.FOOD),
    CharityItemSpec("وجبة إفطار", CharityCategory.FOOD),
    CharityItemSpec("سلة فواكه", CharityCategory.FOOD),
    CharityItemSpec("معطف شتوي", CharityCategory.CLOTHING),
    CharityItemSpec("حذاء دافئ", CharityCategory.CLOTHING),
    CharityItemSpec("ثوب جديد", CharityCategory.CLOTHING),
    CharityItemSpec("وشاح صوف", CharityCategory.CLOTHING),
    CharityItemSpec("كتاب نافع", CharityCategory.EDUCATION),
    CharityItemSpec("دفتر وقلم", CharityCategory.EDUCATION),
    CharityItemSpec("حقيبة مدرسة", CharityCategory.EDUCATION),
    CharityItemSpec("مصاحف شريفة", CharityCategory.EDUCATION),
    CharityItemSpec("لعبة أطفال", CharityCategory.TOYS),
    CharityItemSpec("دمية خشبية", CharityCategory.TOYS),
    CharityItemSpec("كرة قدم", CharityCategory.TOYS),
    CharityItemSpec("مكعبات ملونة", CharityCategory.TOYS)
)

/**
 * Active falling item runtime state
 */
private data class FallingCharityItem(
    val id: Long,
    val spec: CharityItemSpec,
    var position: Offset,
    var velocityY: Float,
    var isDragged: Boolean = false,
    var currentLane: Int = 0
)

/**
 * Game 49: Charity Basket Sort (صندوق الصدقات)
 * Category: تربوية
 * Mechanic: Falling item sorting into matching charity distribution boxes via drag or lane buttons.
 */
@Composable
fun CharitySortingGame(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val theme = NoorThemeState.active
    val textMeasurer = rememberTextMeasurer()
    val engineState = rememberGameEngineState(initialLevel = 1, initialLives = 3, targetScore = 150)

    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    val particles = remember { ParticleSystem() }

    val categories = CharityCategory.values()
    val fallingItems = remember { mutableStateListOf<FallingCharityItem>() }
    var nextItemId by remember { mutableLongStateOf(0L) }
    var spawnTimerSec by remember { mutableFloatStateOf(0f) }
    var activeItemLane by remember { mutableIntStateOf(0) }
    var isErrorFlashing by remember { mutableStateOf(false) }

    fun spawnItem() {
        if (canvasSize.width <= 0) return
        val spec = CHARITY_ITEMS_CATALOG.random()
        val laneIndex = Random.nextInt(categories.size)
        val laneWidth = canvasSize.width.toFloat() / categories.size
        val startX = laneWidth * laneIndex + laneWidth / 2f
        val baseSpeed = 160f + (engineState.score / 20f) * 15f

        fallingItems.add(
            FallingCharityItem(
                id = ++nextItemId,
                spec = spec,
                position = Offset(startX, -60f),
                velocityY = baseSpeed,
                currentLane = laneIndex
            )
        )
    }

    fun resetGame() {
        fallingItems.clear()
        spawnTimerSec = 0f
        activeItemLane = 0
        isErrorFlashing = false
        engineState.startGame()
    }

    LaunchedEffect(Unit) {
        resetGame()
    }

    // 60 FPS Engine Tick
    CanvasGameLoop(engineState = engineState) { deltaSec ->
        particles.update(deltaSec)

        if (engineState.playState != GamePlayState.PLAYING) return@CanvasGameLoop

        // Item Spawning Loop
        spawnTimerSec += deltaSec
        val currentSpawnInterval = (1.8f - (engineState.score / 250f)).coerceAtLeast(0.85f)
        if (spawnTimerSec >= currentSpawnInterval) {
            spawnTimerSec = 0f
            if (fallingItems.size < 4) {
                spawnItem()
            }
        }

        val basketTopY = canvasSize.height.toFloat() - 150f
        val laneWidth = canvasSize.width.toFloat() / categories.size

        val iterator = fallingItems.iterator()
        while (iterator.hasNext()) {
            val item = iterator.next()

            if (!item.isDragged) {
                item.position = Offset(item.position.x, item.position.y + item.velocityY * deltaSec)
            }

            // Check if item reached basket drop zone
            if (item.position.y >= basketTopY) {
                val droppedLane = (item.position.x / laneWidth).toInt().coerceIn(0, categories.size - 1)
                val targetCat = categories[droppedLane]

                if (item.spec.category == targetCat) {
                    // Correct Sorting!
                    val points = 10 + (engineState.comboCount * 2)
                    engineState.addScore(points)
                    SfxManager.playSuccess(context)
                    triggerGameHaptic(context, GameHapticType.SUCCESS)

                    particles.spawnBurst(
                        origin = item.position,
                        count = 20,
                        colors = listOf(targetCat.color, Color.White, Color(0xFFFFD700))
                    )

                    if (engineState.score >= engineState.targetScore) {
                        engineState.completeLevel(context, 3)
                    }
                } else {
                    // Wrong Sorting Basket
                    SfxManager.playWrong(context)
                    triggerGameHaptic(context, GameHapticType.ERROR)
                    isErrorFlashing = true

                    particles.spawnBurst(
                        origin = item.position,
                        count = 15,
                        colors = listOf(Color.Red, Color(0xFFFF5252))
                    )

                    val lost = engineState.loseLife()
                    if (lost) {
                        engineState.failLevel(context)
                    }
                }

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
                title = t("صندوق الصدقات", "Charity Basket Sort"),
                level = engineState.level,
                score = engineState.score,
                lives = engineState.lives,
                onBack = onBack,
                onPause = { engineState.pauseGame() }
            )

            // Main Interactive Canvas Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .noorGlassCard(cornerRadius = 16.dp)
                    .onSizeChanged { canvasSize = it }
                    .gameGestureDetector(
                        onDragStart = { offset ->
                            if (engineState.playState == GamePlayState.PLAYING) {
                                // Find nearest falling item
                                val closest = fallingItems.minByOrNull {
                                    (it.position - offset).getDistanceSquared()
                                }
                                if (closest != null && (closest.position - offset).getDistance() < 120f) {
                                    closest.isDragged = true
                                    SfxManager.playTap(context)
                                }
                            }
                        },
                        onDragMove = { currentOffset, _ ->
                            if (engineState.playState == GamePlayState.PLAYING) {
                                fallingItems.firstOrNull { it.isDragged }?.let { item ->
                                    item.position = currentOffset
                                }
                            }
                        },
                        onDragEnd = {
                            fallingItems.forEach { it.isDragged = false }
                        }
                    )
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    if (canvasSize.width <= 0) return@Canvas

                    val width = size.width
                    val height = size.height
                    val laneWidth = width / categories.size
                    val basketHeight = 140f
                    val basketTop = height - basketHeight

                    // Draw Category Basket Bins at the bottom
                    categories.forEachIndexed { index, cat ->
                        val binLeft = index * laneWidth
                        val binRect = Size(laneWidth - 8f, basketHeight - 12f)
                        val binOffset = Offset(binLeft + 4f, basketTop + 6f)

                        // Draw Bin Background
                        drawRoundRect(
                            color = cat.color.copy(alpha = 0.25f),
                            topLeft = binOffset,
                            size = binRect,
                            cornerRadius = CornerRadius(16f, 16f)
                        )
                        drawRoundRect(
                            color = cat.color,
                            topLeft = binOffset,
                            size = binRect,
                            cornerRadius = CornerRadius(16f, 16f),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
                        )

                        // Bin Title Text in Arabic
                        val textResult = textMeasurer.measure(
                            text = cat.titleAr,
                            style = TextStyle(
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        drawText(
                            textLayoutResult = textResult,
                            topLeft = Offset(
                                binLeft + (laneWidth - textResult.size.width) / 2f,
                                basketTop + 35f
                            )
                        )
                    }

                    // Draw Falling Charity Items
                    fallingItems.forEach { item ->
                        val cardSize = Size(100f, 60f)
                        val itemOffset = Offset(
                            item.position.x - cardSize.width / 2f,
                            item.position.y - cardSize.height / 2f
                        )

                        // Draw Item Box Card
                        drawRoundRect(
                            color = item.spec.category.color,
                            topLeft = itemOffset,
                            size = cardSize,
                            cornerRadius = CornerRadius(12f, 12f)
                        )
                        drawRoundRect(
                            color = Color.White.copy(alpha = 0.8f),
                            topLeft = itemOffset,
                            size = cardSize,
                            cornerRadius = CornerRadius(12f, 12f),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                        )

                        // Draw Item Text
                        val nameResult = textMeasurer.measure(
                            text = item.spec.nameAr,
                            style = TextStyle(
                                color = Color.Black,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        drawText(
                            textLayoutResult = nameResult,
                            topLeft = Offset(
                                item.position.x - nameResult.size.width / 2f,
                                item.position.y - nameResult.size.height / 2f
                            )
                        )
                    }

                    // Draw Particles
                    particles.draw(this)
                }

                // Error Warning Visual Flash
                if (isErrorFlashing) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Red.copy(alpha = 0.2f))
                    )
                    LaunchedEffect(Unit) {
                        kotlinx.coroutines.delay(200L)
                        isErrorFlashing = false
                    }
                }
            }

            // Quick Lane Shift Control Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        SfxManager.playTap(context)
                        fallingItems.firstOrNull()?.let { item ->
                            val laneWidth = canvasSize.width.toFloat() / categories.size
                            val currentLane = (item.position.x / laneWidth).toInt().coerceIn(0, categories.size - 1)
                            val newLane = (currentLane - 1).coerceAtLeast(0)
                            item.position = Offset(newLane * laneWidth + laneWidth / 2f, item.position.y)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.accent),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.Black)
                    Spacer(Modifier.width(4.dp))
                    Text(t("نقل لليسار", "Move Left"), color = Color.Black, fontWeight = FontWeight.Bold)
                }

                Text(
                    text = t("اسحب المواد إلى السلال المناسبة", "Drag items to matching baskets"),
                    color = theme.textSecondary,
                    fontSize = 12.sp
                )

                Button(
                    onClick = {
                        SfxManager.playTap(context)
                        fallingItems.firstOrNull()?.let { item ->
                            val laneWidth = canvasSize.width.toFloat() / categories.size
                            val currentLane = (item.position.x / laneWidth).toInt().coerceIn(0, categories.size - 1)
                            val newLane = (currentLane + 1).coerceAtMost(categories.size - 1)
                            item.position = Offset(newLane * laneWidth + laneWidth / 2f, item.position.y)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.accent),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(t("نقل لليمين", "Move Right"), color = Color.Black, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.Black)
                }
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
