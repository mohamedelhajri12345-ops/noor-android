package com.elhajri.noor.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * مكون Composable لرسم البصمة البصرية المخصصة للثيمة.
 */
@Composable
fun ThemeMotif(
    modifier: Modifier = Modifier,
    def: NoorThemeDef = NoorThemeState.active
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        drawThemeMotif(def)
    }
}

/**
 * Modifier يُضيف طبقة البصمة البصرية خلف أي مكون.
 */
fun Modifier.themeMotif(def: NoorThemeDef = NoorThemeState.active): Modifier = this.drawBehind {
    drawThemeMotif(def)
}

/**
 * دالة الرسم الرئيسية للبصمات البصرية (100% Canvas Code — دون أي صور أو زخارف هندسية متكررة).
 */
fun DrawScope.drawThemeMotif(def: NoorThemeDef) {
    val color = def.motifColor
    val w = size.width
    val h = size.height
    if (w <= 0f || h <= 0f) return

    when (def.motifType) {
        ThemeMotifType.NOOR_FLAGSHIP -> drawNoorFlagshipMotif(color, w, h)
        ThemeMotifType.FAJR_DAWN -> drawFajrDawnMotif(color, w, h)
        ThemeMotifType.EMERALD_DOME -> drawEmeraldDomeMotif(color, w, h)
        ThemeMotifType.GOLDEN_MOSQUE -> drawGoldenMosqueMotif(color, w, h)
        ThemeMotifType.MAKKAH_NIGHT -> drawMakkahNightMotif(color, w, h)
        ThemeMotifType.MADINAH_CALM -> drawMadinahCalmMotif(color, w, h)
        ThemeMotifType.ROYAL_KAABA -> drawRoyalKaabaMotif(color, w, h)
        ThemeMotifType.BLUE_SKY -> drawBlueSkyMotif(color, w, h)
        ThemeMotifType.SILVER_CRESCENT -> drawSilverCrescentMotif(color, w, h)
        ThemeMotifType.GOLDEN_DESERT -> drawGoldenDesertMotif(color, w, h)
        ThemeMotifType.OLIVE_GARDEN -> drawOliveGardenMotif(color, w, h)
        ThemeMotifType.TURQUOISE_ARCH -> drawTurquoiseArchMotif(color, w, h)
        ThemeMotifType.SPIRITUAL_VIOLET -> drawSpiritualVioletMotif(color, w, h)
        ThemeMotifType.NIGHT_EMERALD -> drawNightEmeraldMotif(color, w, h)
        ThemeMotifType.PINK_DAWN -> drawPinkDawnMotif(color, w, h)
        ThemeMotifType.ISLAMIC_SEA -> drawIslamicSeaMotif(color, w, h)
        ThemeMotifType.MANUSCRIPTS -> drawManuscriptsMotif(color, w, h)
        ThemeMotifType.EMERALD_GEM -> drawEmeraldGemMotif(color, w, h)
        ThemeMotifType.RAMADAN_LANTERN -> drawRamadanLanternMotif(color, w, h)
        ThemeMotifType.NIGHT_CRESCENT -> drawNightCrescentMotif(color, w, h)
        ThemeMotifType.NOOR_PREMIUM -> drawNoorPremiumMotif(color, w, h)
    }
}

// 00 — نور الأساسي: هالة زيتونية ذهبية مع هلال دقيق وشعاع ضوء
private fun DrawScope.drawNoorFlagshipMotif(color: Color, w: Float, h: Float) {
    val cx = w * 0.82f
    val cy = h * 0.08f
    val radius = w * 0.35f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color.copy(alpha = 0.16f), Color.Transparent),
            center = Offset(cx, cy),
            radius = radius
        ),
        center = Offset(cx, cy),
        radius = radius
    )
    val outerPath = Path().apply { addOval(Rect(cx - 24f, cy - 24f, cx + 24f, cy + 24f)) }
    val innerPath = Path().apply { addOval(Rect(cx - 16f, cy - 28f, cx + 28f, cy + 20f)) }
    val crescentPath = Path.combine(PathOperation.Difference, outerPath, innerPath)
    drawPath(crescentPath, color.copy(alpha = 0.22f))

    val rayPath = Path().apply {
        moveTo(cx, cy)
        lineTo(w * 0.20f, h * 0.45f)
        lineTo(w * 0.05f, h * 0.45f)
        close()
    }
    drawPath(
        rayPath,
        brush = Brush.linearGradient(
            colors = listOf(color.copy(alpha = 0.08f), Color.Transparent),
            start = Offset(cx, cy),
            end = Offset(w * 0.1f, h * 0.45f)
        )
    )
}

// 01 — نور الفجر: أشعة فجر سماوية متصاعدة وأفق دافئ
private fun DrawScope.drawFajrDawnMotif(color: Color, w: Float, h: Float) {
    val horizonY = h * 0.85f
    val originX = w * 0.5f
    val numRays = 7
    for (i in 0 until numRays) {
        val angle = (PI / (numRays + 1) * (i + 1)).toFloat()
        val rayPath = Path().apply {
            moveTo(originX, horizonY)
            val rx1 = originX + cos(angle - 0.10f) * w * 1.2f
            val ry1 = horizonY - sin(angle - 0.10f) * h * 0.9f
            val rx2 = originX + cos(angle + 0.10f) * w * 1.2f
            val ry2 = horizonY - sin(angle + 0.10f) * h * 0.9f
            lineTo(rx1, ry1)
            lineTo(rx2, ry2)
            close()
        }
        drawPath(
            rayPath,
            brush = Brush.verticalGradient(
                colors = listOf(color.copy(alpha = 0.07f), Color.Transparent),
                startY = horizonY - h * 0.5f,
                endY = horizonY
            )
        )
    }
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color.Transparent, color.copy(alpha = 0.12f)),
            startY = horizonY - 60f,
            endY = horizonY
        ),
        topLeft = Offset(0f, horizonY - 60f),
        size = Size(w, 120f)
    )
}

// 02 — زمرد القرآن: قبة مسجد زمردية أنيقة مع توهج ناعم
private fun DrawScope.drawEmeraldDomeMotif(color: Color, w: Float, h: Float) {
    val cx = w * 0.5f
    val domeTop = h * 0.04f
    val domeWidth = w * 0.55f
    val domeHeight = h * 0.14f

    val path = Path().apply {
        moveTo(cx - domeWidth / 2, domeTop + domeHeight)
        cubicTo(
            cx - domeWidth / 2, domeTop + domeHeight * 0.3f,
            cx - domeWidth * 0.15f, domeTop,
            cx, domeTop
        )
        cubicTo(
            cx + domeWidth * 0.15f, domeTop,
            cx + domeWidth / 2, domeTop + domeHeight * 0.3f,
            cx + domeWidth / 2, domeTop + domeHeight
        )
        close()
    }
    drawPath(
        path,
        brush = Brush.radialGradient(
            colors = listOf(color.copy(alpha = 0.15f), Color.Transparent),
            center = Offset(cx, domeTop + domeHeight / 2),
            radius = domeWidth / 2
        )
    )
    drawPath(path, color.copy(alpha = 0.22f), style = Stroke(width = 2.5f))
    drawCircle(color.copy(alpha = 0.30f), radius = 6f, center = Offset(cx, domeTop - 12f))
}

// 03 — المسجد الذهبي: ظلال مآذن وقبة ذهبية فاخرة
private fun DrawScope.drawGoldenMosqueMotif(color: Color, w: Float, h: Float) {
    val topY = h * 0.03f
    val mWidth = 20f
    val mHeight = h * 0.18f

    val leftM = Path().apply {
        moveTo(w * 0.12f - mWidth / 2, topY + mHeight)
        lineTo(w * 0.12f - mWidth / 2, topY + 20f)
        lineTo(w * 0.12f, topY)
        lineTo(w * 0.12f + mWidth / 2, topY + 20f)
        lineTo(w * 0.12f + mWidth / 2, topY + mHeight)
        close()
    }
    val rightM = Path().apply {
        moveTo(w * 0.88f - mWidth / 2, topY + mHeight)
        lineTo(w * 0.88f - mWidth / 2, topY + 20f)
        lineTo(w * 0.88f, topY)
        lineTo(w * 0.88f + mWidth / 2, topY + 20f)
        lineTo(w * 0.88f + mWidth / 2, topY + mHeight)
        close()
    }
    drawPath(leftM, color.copy(alpha = 0.18f), style = Stroke(width = 2f))
    drawPath(rightM, color.copy(alpha = 0.18f), style = Stroke(width = 2f))

    val domePath = Path().apply {
        val cx = w * 0.5f
        val dw = w * 0.40f
        moveTo(cx - dw / 2, topY + mHeight)
        cubicTo(cx - dw / 2, topY + 30f, cx + dw / 2, topY + 30f, cx + dw / 2, topY + mHeight)
    }
    drawPath(domePath, color.copy(alpha = 0.20f), style = Stroke(width = 2.5f))
}

// 04 — ليل مكة: أفق الكعبة المشرفة تحت سماء كحلية بنجوم متلألئة
private fun DrawScope.drawMakkahNightMotif(color: Color, w: Float, h: Float) {
    val stars = listOf(
        Offset(w * 0.15f, h * 0.05f), Offset(w * 0.35f, h * 0.03f),
        Offset(w * 0.70f, h * 0.07f), Offset(w * 0.85f, h * 0.04f),
        Offset(w * 0.50f, h * 0.09f), Offset(w * 0.22f, h * 0.12f)
    )
    for (st in stars) {
        drawCircle(color.copy(alpha = 0.35f), radius = 2.5f, center = st)
        drawLine(color.copy(alpha = 0.20f), Offset(st.x - 6f, st.y), Offset(st.x + 6f, st.y), strokeWidth = 1f)
        drawLine(color.copy(alpha = 0.20f), Offset(st.x, st.y - 6f), Offset(st.x, st.y + 6f), strokeWidth = 1f)
    }

    val kw = w * 0.28f
    val kh = kw * 1.1f
    val kx = w * 0.5f - kw / 2
    val ky = h * 0.80f
    drawRect(color.copy(alpha = 0.12f), topLeft = Offset(kx, ky), size = Size(kw, kh))
    drawRect(color.copy(alpha = 0.28f), topLeft = Offset(kx, ky + kh * 0.2f), size = Size(kw, kh * 0.08f))
}

// 05 — المدينة الهادئة: منحنى القبة الخضراء مع هالة هادئة
private fun DrawScope.drawMadinahCalmMotif(color: Color, w: Float, h: Float) {
    val cx = w * 0.5f
    val cy = h * 0.10f
    val r = w * 0.30f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color.copy(alpha = 0.18f), Color.Transparent),
            center = Offset(cx, cy),
            radius = r
        ),
        center = Offset(cx, cy),
        radius = r
    )
    val domePath = Path().apply {
        moveTo(cx - r * 0.6f, cy + r * 0.5f)
        cubicTo(cx - r * 0.6f, cy - r * 0.3f, cx + r * 0.6f, cy - r * 0.3f, cx + r * 0.6f, cy + r * 0.5f)
    }
    drawPath(domePath, color.copy(alpha = 0.25f), style = Stroke(width = 2.5f))
}

// 06 — الكعبة الملكية: مجسم الكعبة الملكي بحزام كسوة ذهبي متوهج
private fun DrawScope.drawRoyalKaabaMotif(color: Color, w: Float, h: Float) {
    val kw = w * 0.32f
    val kh = kw * 1.15f
    val kx = w * 0.5f - kw / 2
    val ky = h * 0.76f

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color.copy(alpha = 0.20f), Color.Transparent),
            center = Offset(w * 0.5f, ky + kh / 2),
            radius = kw * 1.5f
        ),
        center = Offset(w * 0.5f, ky + kh / 2),
        radius = kw * 1.5f
    )
    drawRect(color.copy(alpha = 0.16f), topLeft = Offset(kx, ky), size = Size(kw, kh))
    drawRect(
        brush = Brush.horizontalGradient(listOf(color.copy(alpha = 0.50f), color, color.copy(alpha = 0.50f))),
        topLeft = Offset(kx, ky + kh * 0.18f),
        size = Size(kw, kh * 0.09f)
    )
    drawRect(color.copy(alpha = 0.35f), topLeft = Offset(kx, ky), size = Size(kw, kh), style = Stroke(width = 2f))
}

// 07 — السماء الزرقاء: غيوم انسيابية وأشعة ناصعة
private fun DrawScope.drawBlueSkyMotif(color: Color, w: Float, h: Float) {
    val cloudPath = Path().apply {
        moveTo(w * 0.1f, h * 0.08f)
        cubicTo(w * 0.2f, h * 0.04f, w * 0.35f, h * 0.05f, w * 0.45f, h * 0.08f)
        cubicTo(w * 0.55f, h * 0.03f, w * 0.75f, h * 0.04f, w * 0.85f, h * 0.09f)
        cubicTo(w * 0.95f, h * 0.14f, w * 0.8f, h * 0.18f, w * 0.6f, h * 0.16f)
        cubicTo(w * 0.4f, h * 0.18f, w * 0.15f, h * 0.15f, w * 0.1f, h * 0.08f)
        close()
    }
    drawPath(cloudPath, color.copy(alpha = 0.12f))

    val sunCenter = Offset(w * 0.85f, h * 0.06f)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color.copy(alpha = 0.20f), Color.Transparent),
            center = sunCenter,
            radius = w * 0.4f
        ),
        center = sunCenter,
        radius = w * 0.4f
    )
}

// 08 — الهلال الفضي: هلال فضي ناعم مع نجوم دقيقة
private fun DrawScope.drawSilverCrescentMotif(color: Color, w: Float, h: Float) {
    val cx = w * 0.82f
    val cy = h * 0.08f
    val radius = 32f

    val outer = Path().apply { addOval(Rect(cx - radius, cy - radius, cx + radius, cy + radius)) }
    val inner = Path().apply { addOval(Rect(cx - radius * 0.7f, cy - radius * 1.2f, cx + radius * 1.3f, cy + radius * 0.8f)) }
    val crescent = Path.combine(PathOperation.Difference, outer, inner)
    drawPath(crescent, color.copy(alpha = 0.28f))

    val dots = listOf(Offset(cx - 50f, cy + 10f), Offset(cx - 30f, cy + 40f), Offset(cx + 20f, cy + 50f))
    for (d in dots) {
        drawCircle(color.copy(alpha = 0.35f), radius = 2f, center = d)
    }
}

// 09 — الصحراء الذهبية: كثبان رملية انسيابية وأفق دافئ
private fun DrawScope.drawGoldenDesertMotif(color: Color, w: Float, h: Float) {
    val dune1 = Path().apply {
        moveTo(0f, h * 0.88f)
        cubicTo(w * 0.3f, h * 0.82f, w * 0.6f, h * 0.92f, w, h * 0.85f)
        lineTo(w, h)
        lineTo(0f, h)
        close()
    }
    val dune2 = Path().apply {
        moveTo(0f, h * 0.92f)
        cubicTo(w * 0.4f, h * 0.96f, w * 0.7f, h * 0.88f, w, h * 0.93f)
        lineTo(w, h)
        lineTo(0f, h)
        close()
    }
    drawPath(dune1, color.copy(alpha = 0.12f))
    drawPath(dune2, color.copy(alpha = 0.18f))
}

// 10 — الزيتون: أغصان وأوراق زيتون انسيابية
private fun DrawScope.drawOliveGardenMotif(color: Color, w: Float, h: Float) {
    val stem = Path().apply {
        moveTo(0f, h * 0.05f)
        cubicTo(w * 0.15f, h * 0.08f, w * 0.25f, h * 0.04f, w * 0.35f, h * 0.10f)
    }
    drawPath(stem, color.copy(alpha = 0.25f), style = Stroke(width = 2.5f, cap = StrokeCap.Round))

    val leafCenters = listOf(
        Offset(w * 0.10f, h * 0.065f), Offset(w * 0.20f, h * 0.06f), Offset(w * 0.30f, h * 0.08f)
    )
    for (lc in leafCenters) {
        val leaf = Path().apply {
            moveTo(lc.x - 12f, lc.y)
            cubicTo(lc.x - 4f, lc.y - 10f, lc.x + 4f, lc.y - 10f, lc.x + 12f, lc.y)
            cubicTo(lc.x + 4f, lc.y + 10f, lc.x - 4f, lc.y + 10f, lc.x - 12f, lc.y)
            close()
        }
        drawPath(leaf, color.copy(alpha = 0.20f))
    }
}

// 11 — الفيروز الإسلامي: أقواس أندلسية فيروذية متوهجة
private fun DrawScope.drawTurquoiseArchMotif(color: Color, w: Float, h: Float) {
    val archPath = Path().apply {
        val top = h * 0.02f
        val bottom = h * 0.16f
        moveTo(w * 0.1f, bottom)
        lineTo(w * 0.1f, top + 40f)
        cubicTo(w * 0.1f, top, w * 0.5f, top - 10f, w * 0.5f, top)
        cubicTo(w * 0.5f, top - 10f, w * 0.9f, top, w * 0.9f, top + 40f)
        lineTo(w * 0.9f, bottom)
    }
    drawPath(archPath, color.copy(alpha = 0.22f), style = Stroke(width = 2.5f))
}

// 12 — البنفسج الروحاني: سماء بنفسجية مع إشعاع نجمي ثماني
private fun DrawScope.drawSpiritualVioletMotif(color: Color, w: Float, h: Float) {
    val cx = w * 0.5f
    val cy = h * 0.08f
    val radius = 30f

    rotate(22.5f, pivot = Offset(cx, cy)) {
        val rect1 = Rect(cx - radius, cy - radius, cx + radius, cy + radius)
        drawRect(color.copy(alpha = 0.18f), topLeft = rect1.topLeft, size = rect1.size, style = Stroke(width = 2f))
        rotate(45f, pivot = Offset(cx, cy)) {
            drawRect(color.copy(alpha = 0.18f), topLeft = rect1.topLeft, size = rect1.size, style = Stroke(width = 2f))
        }
    }
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color.copy(alpha = 0.18f), Color.Transparent),
            center = Offset(cx, cy),
            radius = radius * 3f
        ),
        center = Offset(cx, cy),
        radius = radius * 3f
    )
}

// 13 — زمرد الليل: عناقيد نجوم على سماء زمردية ليليّة
private fun DrawScope.drawNightEmeraldMotif(color: Color, w: Float, h: Float) {
    val cluster = listOf(
        Offset(w * 0.2f, h * 0.06f), Offset(w * 0.25f, h * 0.09f),
        Offset(w * 0.75f, h * 0.05f), Offset(w * 0.8f, h * 0.08f), Offset(w * 0.82f, h * 0.04f)
    )
    for (pt in cluster) {
        drawCircle(color.copy(alpha = 0.32f), radius = 3f, center = pt)
    }
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(color.copy(alpha = 0.12f), Color.Transparent),
            startY = 0f, endY = h * 0.20f
        )
    )
}

// 14 — الفجر الوردي: تموجات ضوئية وردية لأفق الفجر
private fun DrawScope.drawPinkDawnMotif(color: Color, w: Float, h: Float) {
    val wave = Path().apply {
        moveTo(0f, h * 0.06f)
        cubicTo(w * 0.25f, h * 0.02f, w * 0.50f, h * 0.10f, w * 0.75f, h * 0.04f)
        cubicTo(w * 0.88f, h * 0.01f, w * 0.95f, h * 0.07f, w, h * 0.05f)
        lineTo(w, 0f)
        lineTo(0f, 0f)
        close()
    }
    drawPath(wave, color.copy(alpha = 0.16f))
}

// 15 — البحر الإسلامي: أمواج بحرية انسيابية بتألق ذهبي
private fun DrawScope.drawIslamicSeaMotif(color: Color, w: Float, h: Float) {
    val w1 = Path().apply {
        moveTo(0f, h * 0.12f)
        cubicTo(w * 0.3f, h * 0.16f, w * 0.7f, h * 0.08f, w, h * 0.14f)
    }
    val w2 = Path().apply {
        moveTo(0f, h * 0.15f)
        cubicTo(w * 0.4f, h * 0.10f, w * 0.6f, h * 0.18f, w, h * 0.12f)
    }
    drawPath(w1, color.copy(alpha = 0.22f), style = Stroke(width = 2f))
    drawPath(w2, color.copy(alpha = 0.18f), style = Stroke(width = 1.8f))
}

// 16 — المخطوطات: زخارف زوايا المخطوطات الأندلسية العتيقة
private fun DrawScope.drawManuscriptsMotif(color: Color, w: Float, h: Float) {
    val cornerSize = 48f
    val topLeftCorner = Path().apply {
        moveTo(12f, 12f + cornerSize)
        lineTo(12f, 12f)
        lineTo(12f + cornerSize, 12f)
        cubicTo(12f + cornerSize / 2, 12f + cornerSize / 2, 12f + cornerSize / 2, 12f + cornerSize / 2, 12f, 12f + cornerSize)
    }
    val topRightCorner = Path().apply {
        moveTo(w - 12f - cornerSize, 12f)
        lineTo(w - 12f, 12f)
        lineTo(w - 12f, 12f + cornerSize)
        cubicTo(w - 12f - cornerSize / 2, 12f + cornerSize / 2, w - 12f - cornerSize / 2, 12f + cornerSize / 2, w - 12f - cornerSize, 12f)
    }
    drawPath(topLeftCorner, color.copy(alpha = 0.25f), style = Stroke(width = 2f))
    drawPath(topRightCorner, color.copy(alpha = 0.25f), style = Stroke(width = 2f))
}

// 17 — قبة الزمرد: انعكاسات بلورية زمردية فاخرة
private fun DrawScope.drawEmeraldGemMotif(color: Color, w: Float, h: Float) {
    val cx = w * 0.5f
    val cy = h * 0.08f
    val size = 36f

    val gem = Path().apply {
        moveTo(cx, cy - size)
        lineTo(cx + size, cy)
        lineTo(cx, cy + size)
        lineTo(cx - size, cy)
        close()
    }
    drawPath(gem, color.copy(alpha = 0.14f))
    drawPath(gem, color.copy(alpha = 0.28f), style = Stroke(width = 2f))
    drawLine(color.copy(alpha = 0.20f), Offset(cx - size, cy), Offset(cx + size, cy), strokeWidth = 1.5f)
    drawLine(color.copy(alpha = 0.20f), Offset(cx, cy - size), Offset(cx, cy + size), strokeWidth = 1.5f)
}

// 18 — رمضان: فانوس رمضان متوهج بشعاع ضوء وهلال ذهبي
private fun DrawScope.drawRamadanLanternMotif(color: Color, w: Float, h: Float) {
    val lx = w * 0.82f
    val topY = 0f
    val lanternY = h * 0.08f

    drawLine(color.copy(alpha = 0.30f), Offset(lx, topY), Offset(lx, lanternY), strokeWidth = 1.5f)

    val lw = 24f
    val lh = 36f
    val lantern = Path().apply {
        moveTo(lx - lw / 2, lanternY + 6f)
        lineTo(lx - lw, lanternY + lh * 0.4f)
        lineTo(lx - lw / 2, lanternY + lh)
        lineTo(lx + lw / 2, lanternY + lh)
        lineTo(lx + lw, lanternY + lh * 0.4f)
        lineTo(lx + lw / 2, lanternY + 6f)
        close()
    }
    drawPath(lantern, color.copy(alpha = 0.22f))
    drawPath(lantern, color.copy(alpha = 0.38f), style = Stroke(width = 2f))

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color.copy(alpha = 0.25f), Color.Transparent),
            center = Offset(lx, lanternY + lh / 2),
            radius = 60f
        ),
        center = Offset(lx, lanternY + lh / 2),
        radius = 60f
    )
}

// 19 — الهلال الليلي: هلال بارز متوهج في سماء ليليّة حلكة
private fun DrawScope.drawNightCrescentMotif(color: Color, w: Float, h: Float) {
    val cx = w * 0.80f
    val cy = h * 0.08f
    val radius = 38f

    val outer = Path().apply { addOval(Rect(cx - radius, cy - radius, cx + radius, cy + radius)) }
    val inner = Path().apply { addOval(Rect(cx - radius * 0.7f, cy - radius * 1.3f, cx + radius * 1.3f, cy + radius * 0.7f)) }
    val crescent = Path.combine(PathOperation.Difference, outer, inner)

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color.copy(alpha = 0.22f), Color.Transparent),
            center = Offset(cx, cy),
            radius = radius * 2.2f
        ),
        center = Offset(cx, cy),
        radius = radius * 2.2f
    )
    drawPath(crescent, color.copy(alpha = 0.32f))
}

// 20 — نور Premium: أشعة ملكية متداخلة وهالة تاجية ذهبية
private fun DrawScope.drawNoorPremiumMotif(color: Color, w: Float, h: Float) {
    val cx = w * 0.5f
    val cy = h * 0.06f

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color.copy(alpha = 0.24f), Color.Transparent),
            center = Offset(cx, cy),
            radius = w * 0.45f
        ),
        center = Offset(cx, cy),
        radius = w * 0.45f
    )

    val beam1 = Path().apply {
        moveTo(0f, 0f)
        lineTo(w * 0.4f, h * 0.3f)
        lineTo(w * 0.25f, h * 0.3f)
        close()
    }
    val beam2 = Path().apply {
        moveTo(w, 0f)
        lineTo(w * 0.6f, h * 0.3f)
        lineTo(w * 0.75f, h * 0.3f)
        close()
    }
    drawPath(beam1, color.copy(alpha = 0.09f))
    drawPath(beam2, color.copy(alpha = 0.09f))
}
