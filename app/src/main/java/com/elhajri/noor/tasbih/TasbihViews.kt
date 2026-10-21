package com.elhajri.noor.tasbih
import com.elhajri.noor.settings.t

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.ui.AmiriFamily
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.Navy
import com.elhajri.noor.ui.NavyCard
import com.elhajri.noor.ui.NavyLight
import com.elhajri.noor.ui.TextMain
import com.elhajri.noor.theme.noorGlassCard
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

// ============================================================================
// Style 1: المسبحة الإلكترونية (Polished Digital Counter)
// ============================================================================
@Composable
fun ElectronicTasbihView(
    dhikrText: String,
    count: Int,
    target: Int,
    rounds: Int,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = if (target > 0) ((count % target).toFloat() / target.toFloat()).coerceIn(0f, 1f) else 0f
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessHigh),
        label = "electronicPulse"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .noorGlassCard(cornerRadius = 24.dp)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = dhikrText,
                    color = Gold,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = AmiriFamily,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                // Circular Counter Gauge
                Box(
                    modifier = Modifier
                        .size(210.dp)
                        .scale(scale)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(Gold.copy(alpha = 0.12f), Color.Transparent)
                            )
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            isPressed = true
                            onTap()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    LaunchedEffect(isPressed) {
                        if (isPressed) {
                            kotlinx.coroutines.delay(100)
                            isPressed = false
                        }
                    }

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 12.dp.toPx()
                        val radius = (size.minDimension - strokeWidth) / 2
                        val topLeft = Offset((size.width - radius * 2) / 2, (size.height - radius * 2) / 2)
                        val arcSize = Size(radius * 2, radius * 2)

                        // Outer track
                        drawArc(
                            color = NavyLight.copy(alpha = 0.6f),
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth)
                        )

                        // Active progress arc
                        if (progress > 0f) {
                            drawArc(
                                brush = Brush.sweepGradient(
                                    listOf(GoldSoft, Gold, Color(0xFFFFD54F), GoldSoft)
                                ),
                                startAngle = -90f,
                                sweepAngle = progress * 360f,
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcSize,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = toArabicNumber(count),
                            color = Color.White,
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "من ${toArabicNumber(target)}",
                            color = GoldSoft,
                            fontSize = 14.sp
                        )
                        if (rounds > 0) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "الجولة ${toArabicNumber(rounds)}",
                                color = Gold.copy(alpha = 0.8f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Interactive Tap Button
                Button(
                    onClick = {
                        isPressed = true
                        onTap()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Gold,
                        contentColor = Navy
                    ),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text(
                        text = t("اضغط للسبحة", "Tap for Tasbih"),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ============================================================================
// Style 2: مسبحة الخيط (Physics-like Sliding Thread Bead)
// ============================================================================
@Composable
fun ThreadTasbihView(
    dhikrText: String,
    count: Int,
    target: Int,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val slideAnim = remember { Animatable(0f) }
    var stackedBeadsCount by remember { mutableIntStateOf((count % target).coerceAtLeast(0)) }

    LaunchedEffect(count) {
        stackedBeadsCount = (count % target)
    }

    val triggerTap = {
        onTap()
        coroutineScope.launch {
            slideAnim.snapTo(0f)
            slideAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
            )
            slideAnim.snapTo(0f)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .noorGlassCard(cornerRadius = 24.dp)
                .padding(16.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { triggerTap() },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = dhikrText,
                    color = Gold,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = AmiriFamily
                )
                Text(
                    text = "${toArabicNumber(count)} / ${toArabicNumber(target)}",
                    color = GoldSoft,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Vertical Thread & Sliding Bead Canvas
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                ) {
                    val cx = size.width / 2f
                    val topY = 20.dp.toPx()
                    val bottomY = size.height - 30.dp.toPx()

                    // Draw Thread Line
                    drawLine(
                        brush = Brush.verticalGradient(
                            colors = listOf(Gold.copy(alpha = 0.3f), Gold, Gold.copy(alpha = 0.3f))
                        ),
                        start = Offset(cx, topY),
                        end = Offset(cx, bottomY),
                        strokeWidth = 3.dp.toPx()
                    )

                    // Draw Top Bead Dispenser Ring
                    drawCircle(
                        color = Gold,
                        radius = 16.dp.toPx(),
                        center = Offset(cx, topY),
                        style = Stroke(width = 2.dp.toPx())
                    )

                    // Draw Bottom Bead Tray
                    drawRoundRect(
                        brush = Brush.verticalGradient(listOf(NavyLight, NavyCard)),
                        topLeft = Offset(cx - 60.dp.toPx(), bottomY - 10.dp.toPx()),
                        size = Size(120.dp.toPx(), 20.dp.toPx()),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(10.dp.toPx())
                    )

                    // Draw Stacked Beads at Bottom
                    val beadRadius = 14.dp.toPx()
                    val maxVisibleStacked = 10
                    val visibleBeads = stackedBeadsCount.coerceAtMost(maxVisibleStacked)

                    for (i in 0 until visibleBeads) {
                        val beadY = bottomY - 15.dp.toPx() - (i * (beadRadius * 1.5f))
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFFFFF176), Gold, Color(0xFFB78103)),
                                center = Offset(cx - beadRadius * 0.3f, beadY - beadRadius * 0.3f),
                                radius = beadRadius * 1.2f
                            ),
                            radius = beadRadius,
                            center = Offset(cx, beadY)
                        )
                    }

                    // Draw Active Sliding Bead
                    if (slideAnim.value > 0f && slideAnim.value < 1f) {
                        val currentBeadY = topY + (slideAnim.value * (bottomY - topY - (visibleBeads * beadRadius * 1.5f)))
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFFFFFFFF), Color(0xFFFFD54F), Gold),
                                center = Offset(cx - beadRadius * 0.3f, currentBeadY - beadRadius * 0.3f),
                                radius = beadRadius * 1.4f
                            ),
                            radius = beadRadius * 1.1f,
                            center = Offset(cx, currentBeadY)
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = { triggerTap() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Navy),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("اسحب خرزة (اضغط للسبحة)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

// ============================================================================
// Style 3: مسبحة الكرات الدوارة (3D Golden Beads Ring)
// ============================================================================
@Composable
fun RotatingBeadsTasbihView(
    dhikrText: String,
    count: Int,
    target: Int,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    var rotationAngle by remember { mutableFloatStateOf(0f) }

    val animatedAngle by animateFloatAsState(
        targetValue = rotationAngle,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium),
        label = "beadRingRotation"
    )

    val handleTap = {
        rotationAngle -= 360f / 33f
        onTap()
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .noorGlassCard(cornerRadius = 24.dp)
                .padding(20.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { handleTap() },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = dhikrText,
                    color = Gold,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = AmiriFamily
                )
                Text(
                    text = "${toArabicNumber(count)} / ${toArabicNumber(target)}",
                    color = GoldSoft,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // 3D Rotating Bead Ring Canvas
                Box(
                    modifier = Modifier.size(240.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val cx = size.width / 2f
                        val cy = size.height / 2f
                        val ringRadius = 90.dp.toPx()
                        val beadCount = 33

                        // Draw Ring Support
                        drawCircle(
                            color = Gold.copy(alpha = 0.25f),
                            radius = ringRadius,
                            center = Offset(cx, cy),
                            style = Stroke(width = 2.dp.toPx())
                        )

                        // Draw 33 Golden Beads around the circle
                        for (i in 0 until beadCount) {
                            val baseAngle = (i * (360f / beadCount)) + animatedAngle
                            val rad = Math.toRadians(baseAngle.toDouble())
                            val bx = cx + (ringRadius * cos(rad)).toFloat()
                            val by = cy + (ringRadius * sin(rad)).toFloat()

                            val isTopActive = ((baseAngle % 360 + 360) % 360) in 260f..280f
                            val bRadius = if (isTopActive) 11.dp.toPx() else 8.dp.toPx()

                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = if (isTopActive)
                                        listOf(Color(0xFFFFFFFF), Color(0xFFFFD700), Color(0xFFB78103))
                                    else
                                        listOf(Color(0xFFFFECB3), Gold, Color(0xFF795548)),
                                    center = Offset(bx - bRadius * 0.3f, by - bRadius * 0.3f),
                                    radius = bRadius * 1.3f
                                ),
                                radius = bRadius,
                                center = Offset(bx, by)
                            )

                            if (isTopActive) {
                                drawCircle(
                                    color = Gold.copy(alpha = 0.4f),
                                    radius = bRadius * 1.6f,
                                    center = Offset(bx, by),
                                    style = Stroke(width = 1.5.dp.toPx())
                                )
                            }
                        }

                        // Top Pointer Indicator Arrow
                        val pointerPath = Path().apply {
                            moveTo(cx, cy - ringRadius - 18.dp.toPx())
                            lineTo(cx - 8.dp.toPx(), cy - ringRadius - 30.dp.toPx())
                            lineTo(cx + 8.dp.toPx(), cy - ringRadius - 30.dp.toPx())
                            close()
                        }
                        drawPath(pointerPath, color = Gold)
                    }

                    // Center Counter Display inside ring
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = toArabicNumber(count),
                            color = Color.White,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = t("أدر الحلقة", "Rotate Ring"),
                            color = GoldSoft.copy(alpha = 0.7f),
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = { handleTap() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Navy),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("تدوير الخرز الذهبي", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

// ============================================================================
// Style 4: مسبحة البستان (Night Garden Growing Flowers)
// ============================================================================
@Composable
fun NightGardenTasbihView(
    dhikrText: String,
    count: Int,
    target: Int,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gardenFlowers = (count % target).coerceAtLeast(0)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .noorGlassCard(cornerRadius = 24.dp)
                .padding(20.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onTap() },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = t("بستان الذكر", "Dhikr Garden"),
                    color = Gold,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = AmiriFamily
                )
                Text(
                    text = "$dhikrText — ${toArabicNumber(count)} وردة",
                    color = GoldSoft,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                // Night Garden Canvas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF07101C), Color(0xFF0D211A), Color(0xFF04120D))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height

                        // Draw Crescent Moon in Sky
                        drawCircle(
                            color = Color(0xFFFFF59D),
                            radius = 16.dp.toPx(),
                            center = Offset(w - 40.dp.toPx(), 40.dp.toPx())
                        )
                        drawCircle(
                            color = Color(0xFF07101C),
                            radius = 14.dp.toPx(),
                            center = Offset(w - 46.dp.toPx(), 36.dp.toPx())
                        )

                        // Draw Lawn Ground
                        drawRect(
                            brush = Brush.verticalGradient(
                                listOf(Color(0xFF1B4332), Color(0xFF081C15))
                            ),
                            topLeft = Offset(0f, h - 50.dp.toPx()),
                            size = Size(w, 50.dp.toPx())
                        )

                        // Draw Blooming Flowers in Garden Grid
                        val columns = 7
                        val rows = 5
                        val paddingX = 25.dp.toPx()
                        val paddingY = 20.dp.toPx()
                        val cellW = (w - (paddingX * 2)) / columns
                        val cellH = (h - 70.dp.toPx() - paddingY) / rows

                        for (i in 0 until gardenFlowers) {
                            val col = i % columns
                            val row = i / columns
                            val fx = paddingX + (col * cellW) + (cellW / 2)
                            val fy = h - 60.dp.toPx() - (row * cellH)

                            // Stem
                            drawLine(
                                color = Color(0xFF40916C),
                                start = Offset(fx, fy + 12.dp.toPx()),
                                end = Offset(fx, fy),
                                strokeWidth = 2.dp.toPx()
                            )

                            // 5 Flower Petals
                            val petalRadius = 5.dp.toPx()
                            for (p in 0 until 5) {
                                val pAngle = p * 72f
                                val prad = Math.toRadians(pAngle.toDouble())
                                val px = fx + (8.dp.toPx() * cos(prad)).toFloat()
                                val py = fy + (8.dp.toPx() * sin(prad)).toFloat()

                                drawCircle(
                                    color = if (i % 3 == 0) Color(0xFFFFD54F) else if (i % 3 == 1) Color(0xFF81C784) else Color(0xFFF48FB1),
                                    radius = petalRadius,
                                    center = Offset(px, py)
                                )
                            }
                            // Gold Center
                            drawCircle(
                                color = Color(0xFFFFFFFF),
                                radius = 3.dp.toPx(),
                                center = Offset(fx, fy)
                            )
                        }
                    }

                    if (gardenFlowers == 0) {
                        Text(
                            text = "اضغط لإنبات زهرة في البستان...",
                            color = GoldSoft.copy(alpha = 0.6f),
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = { onTap() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Navy),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("أنبت زهرة (اضغط للسبحة)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

// ============================================================================
// Style 5: مسبحة النور (Glowing Light Orb)
// ============================================================================
@Composable
fun LightOrbTasbihView(
    dhikrText: String,
    count: Int,
    target: Int,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    var pulseTrigger by remember { mutableStateOf(false) }

    val orbScale by animateFloatAsState(
        targetValue = if (pulseTrigger) 1.22f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessHigh),
        label = "orbPulse"
    )

    val activeNodes = (count % target).coerceAtLeast(0)

    val triggerTap = {
        pulseTrigger = true
        onTap()
    }

    LaunchedEffect(pulseTrigger) {
        if (pulseTrigger) {
            kotlinx.coroutines.delay(120)
            pulseTrigger = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .noorGlassCard(cornerRadius = 24.dp)
                .padding(20.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { triggerTap() },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = t("مسبحة النور", "Light Tasbih"),
                    color = Gold,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = AmiriFamily
                )
                Text(
                    text = "$dhikrText — ${toArabicNumber(count)} / ${toArabicNumber(target)}",
                    color = GoldSoft,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Glowing Cosmic Canvas
                Box(
                    modifier = Modifier.size(240.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val cx = size.width / 2f
                        val cy = size.height / 2f
                        val orbitRadius = 90.dp.toPx()
                        val totalNodes = 33

                        // Draw Orbit Ring Path
                        drawCircle(
                            color = Gold.copy(alpha = 0.2f),
                            radius = orbitRadius,
                            center = Offset(cx, cy),
                            style = Stroke(width = 1.5.dp.toPx())
                        )

                        // Draw Orbit Nodes
                        for (i in 0 until totalNodes) {
                            val angle = (i * (360f / totalNodes)) - 90f
                            val rad = Math.toRadians(angle.toDouble())
                            val nx = cx + (orbitRadius * cos(rad)).toFloat()
                            val ny = cy + (orbitRadius * sin(rad)).toFloat()

                            val isLit = i < activeNodes
                            val nodeRadius = if (isLit) 6.dp.toPx() else 3.5.dp.toPx()

                            if (isLit) {
                                drawCircle(
                                    color = Gold.copy(alpha = 0.35f),
                                    radius = nodeRadius * 2.2f,
                                    center = Offset(nx, ny)
                                )
                            }

                            drawCircle(
                                color = if (isLit) Color(0xFFFFF59D) else GoldSoft.copy(alpha = 0.3f),
                                radius = nodeRadius,
                                center = Offset(nx, ny)
                            )
                        }

                        // Draw Central Glowing Orb
                        val orbRadius = 45.dp.toPx() * orbScale

                        // Outer Glow
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Gold.copy(alpha = 0.45f), Color.Transparent),
                                center = Offset(cx, cy),
                                radius = orbRadius * 1.8f
                            ),
                            radius = orbRadius * 1.8f,
                            center = Offset(cx, cy)
                        )

                        // Core Orb
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFFFFFFFF), Color(0xFFFFD54F), Gold),
                                center = Offset(cx - orbRadius * 0.2f, cy - orbRadius * 0.2f),
                                radius = orbRadius
                            ),
                            radius = orbRadius,
                            center = Offset(cx, cy)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = toArabicNumber(count),
                            color = Navy,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = { triggerTap() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Navy),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("إضاءة النور (اضغط للسبحة)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}