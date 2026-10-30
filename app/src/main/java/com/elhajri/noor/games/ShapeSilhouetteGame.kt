package com.elhajri.noor.games

import androidx.compose.foundation.Canvas
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
import kotlin.math.hypot

/**
 * Game 44: Minaret Silhouette Match (ظلال المعالم)
 * Category: أطفال
 * Mechanic: Shape silhouette drag & snap puzzle. Drag colorful architectural pieces into matching dark shadow outlines.
 */
private enum class ShapeType(val nameAr: String) {
    CRESCENT("هلال"),
    DOME("قبة"),
    MINARET("مئذنة"),
    ARCH_DOOR("باب قوس"),
    STAR("نجمة"),
    LANTERN("فانوس"),
    MIHRAB("محراب"),
    WINDOW("نافذة")
}

private data class ShapePiece(
    val id: Int,
    val type: ShapeType,
    val color: Color,
    var targetPos: Offset = Offset.Zero,
    var currentPos: Offset = Offset.Zero,
    var isSnapped: Boolean = false
)

@Composable
fun ShapeSilhouetteGame(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val theme = NoorThemeState.active
    val textMeasurer = rememberTextMeasurer()

    val engineState = rememberGameEngineState(
        initialLevel = 1,
        initialLives = 3,
        targetScore = 120
    )

    val particles = remember { ParticleSystem() }

    var activeDragId by remember { mutableStateOf<Int?>(null) }
    var pieces by remember { mutableStateOf(listOf<ShapePiece>()) }
    var statusText by remember { mutableStateOf("") }

    val pieceColors = listOf(
        Color(0xFFFF7043), // Coral Orange
        Color(0xFF42A5F5), // Sky Blue
        Color(0xFF66BB6A), // Leaf Green
        Color(0xFFAB47BC), // Orchid Purple
        Color(0xFFFFCA28), // Golden Yellow
        Color(0xFFEC407A), // Rose Pink
        Color(0xFF26A69A), // Turquoise
        Color(0xFF8D6E63)  // Warm Brown
    )

    fun setupLevel() {
        engineState.startGame()
        statusText = t("اسحب القطع الملونة وطابقها مع ظلال المعالم!", "Drag colorful shapes to match shadow outlines!")

        val count = when (engineState.level) {
            1 -> 4
            2 -> 6
            else -> 8
        }

        val availableTypes = ShapeType.values().take(count)
        pieces = availableTypes.mapIndexed { idx, type ->
            ShapePiece(
                id = idx,
                type = type,
                color = pieceColors[idx % pieceColors.size]
            )
        }
    }

    LaunchedEffect(engineState.level) {
        setupLevel()
    }

    // Canvas Game Loop for particles
    CanvasGameLoop(engineState) { deltaSec ->
        particles.update(deltaSec)
    }

    // Helper drawing routine for architectural shapes
    fun DrawScope.drawArchitecturalShape(
        type: ShapeType,
        center: Offset,
        color: Color,
        scale: Float = 1.0f,
        isOutlineOnly: Boolean = false
    ) {
        val path = Path()
        when (type) {
            ShapeType.CRESCENT -> {
                path.addOval(androidx.compose.ui.geometry.Rect(center.x - 35f * scale, center.y - 35f * scale, center.x + 35f * scale, center.y + 35f * scale))
                val innerPath = Path().apply {
                    addOval(androidx.compose.ui.geometry.Rect(center.x - 20f * scale, center.y - 38f * scale, center.x + 42f * scale, center.y + 32f * scale))
                }
                path.op(path, innerPath, androidx.compose.ui.graphics.PathOperation.Difference)
            }

            ShapeType.DOME -> {
                path.moveTo(center.x - 40f * scale, center.y + 25f * scale)
                path.lineTo(center.x + 40f * scale, center.y + 25f * scale)
                path.quadraticBezierTo(
                    center.x + 45f * scale, center.y - 20f * scale,
                    center.x, center.y - 45f * scale
                )
                path.quadraticBezierTo(
                    center.x - 45f * scale, center.y - 20f * scale,
                    center.x - 40f * scale, center.y + 25f * scale
                )
                path.close()
            }

            ShapeType.MINARET -> {
                path.moveTo(center.x - 18f * scale, center.y + 40f * scale)
                path.lineTo(center.x + 18f * scale, center.y + 40f * scale)
                path.lineTo(center.x + 15f * scale, center.y - 15f * scale)
                path.lineTo(center.x + 22f * scale, center.y - 15f * scale)
                path.lineTo(center.x + 22f * scale, center.y - 25f * scale)
                path.lineTo(center.x, center.y - 50f * scale)
                path.lineTo(center.x - 22f * scale, center.y - 25f * scale)
                path.lineTo(center.x - 22f * scale, center.y - 15f * scale)
                path.lineTo(center.x - 15f * scale, center.y - 15f * scale)
                path.close()
            }

            ShapeType.ARCH_DOOR -> {
                path.moveTo(center.x - 30f * scale, center.y + 35f * scale)
                path.lineTo(center.x + 30f * scale, center.y + 35f * scale)
                path.lineTo(center.x + 30f * scale, center.y - 5f * scale)
                path.quadraticBezierTo(
                    center.x, center.y - 50f * scale,
                    center.x - 30f * scale, center.y - 5f * scale
                )
                path.close()
            }

            ShapeType.STAR -> {
                val numPoints = 8
                val outerRadius = 35f * scale
                val innerRadius = 18f * scale
                repeat(numPoints * 2) { i ->
                    val r = if (i % 2 == 0) outerRadius else innerRadius
                    val angle = i * (Math.PI / numPoints)
                    val x = center.x + (r * Math.cos(angle)).toFloat()
                    val y = center.y + (r * Math.sin(angle)).toFloat()
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                path.close()
            }

            ShapeType.LANTERN -> {
                path.moveTo(center.x - 25f * scale, center.y - 30f * scale)
                path.lineTo(center.x + 25f * scale, center.y - 30f * scale)
                path.lineTo(center.x + 35f * scale, center.y)
                path.lineTo(center.x + 18f * scale, center.y + 35f * scale)
                path.lineTo(center.x - 18f * scale, center.y + 35f * scale)
                path.lineTo(center.x - 35f * scale, center.y)
                path.close()
            }

            ShapeType.MIHRAB -> {
                path.moveTo(center.x - 28f * scale, center.y + 35f * scale)
                path.lineTo(center.x + 28f * scale, center.y + 35f * scale)
                path.lineTo(center.x + 28f * scale, center.y - 10f * scale)
                path.cubicTo(
                    center.x + 28f * scale, center.y - 35f * scale,
                    center.x + 10f * scale, center.y - 45f * scale,
                    center.x, center.y - 50f * scale
                )
                path.cubicTo(
                    center.x - 10f * scale, center.y - 45f * scale,
                    center.x - 28f * scale, center.y - 35f * scale,
                    center.x - 28f * scale, center.y - 10f * scale
                )
                path.close()
            }

            ShapeType.WINDOW -> {
                path.addRoundRect(
                    androidx.compose.ui.geometry.RoundRect(
                        left = center.x - 28f * scale,
                        top = center.y - 35f * scale,
                        right = center.x + 28f * scale,
                        bottom = center.y + 35f * scale,
                        radiusX = 16f * scale,
                        radiusY = 16f * scale
                    )
                )
            }
        }

        if (isOutlineOnly) {
            drawPath(path, color = Color(0xFF102A43))
            drawPath(path, color = Gold, style = Stroke(width = 3f * scale))
        } else {
            drawPath(path, color = color)
            drawPath(path, color = Color.White.copy(alpha = 0.8f), style = Stroke(width = 2.5f * scale))
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
                title = t("ظلال المعالم", "Silhouette Match"),
                level = engineState.level,
                score = engineState.score,
                lives = engineState.lives,
                onBack = onBack,
                onPause = { engineState.pauseGame() }
            )

            // Status message
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
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(8.dp)
                )
            }

            // Interactive Drag Canvas
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp)
                    .noorGlassCard(cornerRadius = 24.dp)
            ) {
                var sizeInitialized by remember { mutableStateOf(false) }

                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .gameGestureDetector(
                            onDragStart = { offset ->
                                val touched = pieces.firstOrNull { !it.isSnapped && hypot(it.currentPos.x - offset.x, it.currentPos.y - offset.y) < 80f }
                                if (touched != null) {
                                    activeDragId = touched.id
                                    triggerGameHaptic(context, GameHapticType.LIGHT)
                                }
                            },
                            onDragMove = { pos, _ ->
                                if (activeDragId != null) {
                                    pieces = pieces.map { p ->
                                        if (p.id == activeDragId) p.copy(currentPos = pos) else p
                                    }
                                }
                            },
                            onDragEnd = {
                                if (activeDragId != null) {
                                    val dragged = pieces.firstOrNull { it.id == activeDragId }
                                    if (dragged != null) {
                                        val dist = hypot(dragged.currentPos.x - dragged.targetPos.x, dragged.currentPos.y - dragged.targetPos.y)
                                        if (dist < 90f) {
                                            // Snap to target!
                                            SfxManager.playSuccess(context)
                                            triggerGameHaptic(context, GameHapticType.SUCCESS)
                                            engineState.addScore(25)

                                            particles.spawnBurst(
                                                origin = dragged.targetPos,
                                                count = 20,
                                                colors = listOf(dragged.color, Gold, Color.White)
                                            )

                                            pieces = pieces.map { p ->
                                                if (p.id == activeDragId) p.copy(
                                                    currentPos = p.targetPos,
                                                    isSnapped = true
                                                ) else p
                                            }

                                            statusText = t("مطابقة رائعة! 🌟", "Awesome match! 🌟")

                                            if (pieces.all { it.isSnapped }) {
                                                engineState.completeLevel(context, 3)
                                            }
                                        }
                                    }
                                    activeDragId = null
                                }
                            }
                        )
                ) {
                    val w = size.width
                    val h = size.height

                    // Setup target & initial positions once per size
                    if (!sizeInitialized && pieces.isNotEmpty()) {
                        sizeInitialized = true
                        val cols = 4
                        val rowsTarget = (pieces.size + cols - 1) / cols

                        val targetMarginX = w / (cols + 1)
                        val targetTop = h * 0.28f
                        val trayTop = h * 0.72f

                        pieces = pieces.mapIndexed { idx, p ->
                            val colTarget = idx % cols
                            val rowTarget = idx / cols
                            val target = Offset(
                                targetMarginX * (colTarget + 1),
                                targetTop + rowTarget * 110f
                            )

                            val colTray = idx % cols
                            val rowTray = idx / cols
                            val trayPos = Offset(
                                targetMarginX * (colTray + 1),
                                trayTop + rowTray * 100f
                            )

                            p.copy(
                                targetPos = target,
                                currentPos = trayPos
                            )
                        }
                    }

                    // Draw Target Shadows
                    pieces.forEach { p ->
                        drawArchitecturalShape(
                            type = p.type,
                            center = p.targetPos,
                            color = Color.Black,
                            scale = 1.0f,
                            isOutlineOnly = true
                        )

                        // Shadow Label
                        val labelLayout = textMeasurer.measure(
                            text = p.type.nameAr,
                            style = TextStyle(
                                color = GoldSoft,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                        drawText(
                            textLayoutResult = labelLayout,
                            topLeft = Offset(p.targetPos.x - labelLayout.size.width / 2f, p.targetPos.y + 48f)
                        )
                    }

                    // Draw Draggable / Snapped Pieces
                    pieces.forEach { p ->
                        drawArchitecturalShape(
                            type = p.type,
                            center = p.currentPos,
                            color = p.color,
                            scale = if (activeDragId == p.id) 1.2f else 1.0f,
                            isOutlineOnly = false
                        )
                    }

                    // Particles
                    particles.draw(this)
                }
            }
        }

        // Standard Game Overlays
        if (engineState.playState == GamePlayState.PAUSED) {
            PauseOverlay(
                onResume = { engineState.resumeGame() },
                onRestart = { setupLevel() },
                onQuit = onBack
            )
        }

        if (engineState.playState == GamePlayState.WON) {
            GameWinOverlay(
                score = engineState.score,
                stars = engineState.starsEarned,
                onNextLevel = {
                    engineState.nextLevel()
                    setupLevel()
                },
                onRestart = { setupLevel() },
                onQuit = onBack
            )
        }

        if (engineState.playState == GamePlayState.LOST) {
            GameLoseOverlay(
                score = engineState.score,
                onRestart = { setupLevel() },
                onQuit = onBack
            )
        }
    }
}
