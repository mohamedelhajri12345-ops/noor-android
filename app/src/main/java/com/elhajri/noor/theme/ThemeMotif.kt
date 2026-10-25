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
 * مكون Composable لرسم البصمة البصرية المستقبليّة للثيمة النشطة.
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
 * Modifier يُضيف طبقة البصمة البصرية المستقبليّة خلف أي مكون.
 */
fun Modifier.themeMotif(def: NoorThemeDef = NoorThemeState.active): Modifier = this.drawBehind {
    drawThemeMotif(def)
}

/**
 * دالة الرسم الرئيسية للبصمات البصرية الـ20 المستقبليّة (100% Canvas Code دون صور خارجية).
 */
fun DrawScope.drawThemeMotif(def: NoorThemeDef) {
    val color = def.motifColor
    val w = size.width
    val h = size.height
    if (w <= 0f || h <= 0f) return

    when (def.motifType) {
        ThemeMotifType.CYBER_NOOR_CORE -> drawCyberNoorCore(color, w, h)
        ThemeMotifType.QUANTUM_EMERALD_GRID -> drawQuantumEmeraldGrid(color, w, h)
        ThemeMotifType.OBSIDIAN_KAABA_NEON -> drawObsidianKaabaNeon(color, w, h)
        ThemeMotifType.NEON_KUFIC_GOLD -> drawNeonKuficGold(color, w, h)
        ThemeMotifType.CELESTIAL_MADINAH_VIOLET -> drawCelestialMadinahViolet(color, w, h)
        ThemeMotifType.DIWANI_CYBER_ROSE -> drawDiwaniCyberRose(color, w, h)
        ThemeMotifType.HOLOGRAPHIC_ZELLIGE -> drawHolographicZellige(color, w, h)
        ThemeMotifType.ANDALUSIAN_CRYSTAL_GLASS -> drawAndalusiaCrystalGlass(color, w, h)
        ThemeMotifType.SOLAR_FAJR_GLOWING -> drawSolarFajrGlowing(color, w, h)
        ThemeMotifType.DEEP_SPACE_CRESCENT -> drawDeepSpaceCrescent(color, w, h)
        ThemeMotifType.CELESTIAL_SAPPHIRE -> drawCelestialSapphire(color, w, h)
        ThemeMotifType.ALHAMBRA_RUBY_PULSE -> drawAlhambraRubyPulse(color, w, h)
        ThemeMotifType.AURORA_MINARET -> drawAuroraMinaret(color, w, h)
        ThemeMotifType.AMBER_DESERT_HORIZON -> drawAmberDesertHorizon(color, w, h)
        ThemeMotifType.STARLIGHT_MUSHAF -> drawStarlightMushaf(color, w, h)
        ThemeMotifType.TITANIUM_OLIVE -> drawTitaniumOlive(color, w, h)
        ThemeMotifType.HOLOGRAPHIC_LANTERN -> drawHolographicLantern(color, w, h)
        ThemeMotifType.ZENITH_TURQUOISE -> drawZenithTurquoise(color, w, h)
        ThemeMotifType.INFINITE_INDIGO_TASBIH -> drawInfiniteIndigoTasbih(color, w, h)
        ThemeMotifType.QUANTUM_QIBLA_EMERALD -> drawQuantumQiblaEmerald(color, w, h)
    }
}

// 00 — نور السيبراني 2027
private fun DrawScope.drawCyberNoorCore(color: Color, w: Float, h: Float) {
    val cx = w * 0.85f
    val cy = h * 0.08f
    val r = w * 0.40f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color.copy(alpha = 0.22f), Color.Transparent),
            center = Offset(cx, cy),
            radius = r
        ),
        center = Offset(cx, cy),
        radius = r
    )
    val outer = Path().apply { addOval(Rect(cx - 30f, cy - 30f, cx + 30f, cy + 30f)) }
    val inner = Path().apply { addOval(Rect(cx - 20f, cy - 35f, cx + 35f, cy + 25f)) }
    val crescent = Path.combine(PathOperation.Difference, outer, inner)
    drawPath(crescent, color.copy(alpha = 0.35f))
    drawCircle(color.copy(alpha = 0.25f), radius = 45f, center = Offset(cx, cy), style = Stroke(width = 1.5f))
}

// 01 — الزمرد الكوانتي
private fun DrawScope.drawQuantumEmeraldGrid(color: Color, w: Float, h: Float) {
    val gridStep = 40f
    var x = 0f
    while (x < w) {
        drawLine(color.copy(alpha = 0.04f), Offset(x, 0f), Offset(x, h), strokeWidth = 1f)
        x += gridStep
    }
    val cx = w * 0.5f
    val domeTop = h * 0.05f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color.copy(alpha = 0.20f), Color.Transparent),
            center = Offset(cx, domeTop + 60f),
            radius = w * 0.35f
        ),
        center = Offset(cx, domeTop + 60f),
        radius = w * 0.35f
    )
}

// 02 — الكعبة الأوبسيدية 2027
private fun DrawScope.drawObsidianKaabaNeon(color: Color, w: Float, h: Float) {
    val kw = w * 0.26f
    val kh = kw * 1.1f
    val kx = w * 0.5f - kw / 2
    val ky = h * 0.78f

    drawRect(color.copy(alpha = 0.15f), topLeft = Offset(kx, ky), size = Size(kw, kh))
    drawRect(color.copy(alpha = 0.35f), topLeft = Offset(kx, ky + kh * 0.2f), size = Size(kw, kh * 0.08f))
    drawRect(color.copy(alpha = 0.40f), topLeft = Offset(kx, ky), size = Size(kw, kh), style = Stroke(width = 2f))
}

// 03 — الكوفي النيون
private fun DrawScope.drawNeonKuficGold(color: Color, w: Float, h: Float) {
    val size = 60f
    val ox = w * 0.80f
    val oy = h * 0.06f
    drawRect(color.copy(alpha = 0.18f), topLeft = Offset(ox, oy), size = Size(size, size), style = Stroke(width = 2f))
    drawRect(color.copy(alpha = 0.12f), topLeft = Offset(ox + 10f, oy + 10f), size = Size(size - 20f, size - 20f))
}

// 04 — البنفسج الفلكي
private fun DrawScope.drawCelestialMadinahViolet(color: Color, w: Float, h: Float) {
    val cx = w * 0.5f
    val cy = h * 0.12f
    val r = w * 0.35f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color.copy(alpha = 0.22f), Color.Transparent),
            center = Offset(cx, cy),
            radius = r
        ),
        center = Offset(cx, cy),
        radius = r
    )
}

// 05 — الذهب الفتوري الوردي
private fun DrawScope.drawDiwaniCyberRose(color: Color, w: Float, h: Float) {
    val path = Path().apply {
        moveTo(w * 0.1f, h * 0.15f)
        cubicTo(w * 0.4f, h * 0.05f, w * 0.7f, h * 0.25f, w * 0.9f, h * 0.10f)
    }
    drawPath(path, color.copy(alpha = 0.25f), style = Stroke(width = 2.5f, cap = StrokeCap.Round))
}

// 06 — الزليج الهولوغرامي
private fun DrawScope.drawHolographicZellige(color: Color, w: Float, h: Float) {
    val cx = w * 0.85f
    val cy = h * 0.08f
    val r = 40f
    for (i in 0 until 4) {
        rotate(degrees = i * 45f, pivot = Offset(cx, cy)) {
            drawRect(color.copy(alpha = 0.15f), topLeft = Offset(cx - r, cy - r), size = Size(r * 2, r * 2), style = Stroke(width = 1.5f))
        }
    }
}

// 07 — البلور الأندلسي
private fun DrawScope.drawAndalusiaCrystalGlass(color: Color, w: Float, h: Float) {
    val cx = w * 0.5f
    val topY = h * 0.04f
    val path = Path().apply {
        moveTo(cx - 100f, topY + 120f)
        cubicTo(cx - 100f, topY, cx + 100f, topY, cx + 100f, topY + 120f)
    }
    drawPath(path, color.copy(alpha = 0.22f), style = Stroke(width = 2f))
}

// 08 — فجر السولار
private fun DrawScope.drawSolarFajrGlowing(color: Color, w: Float, h: Float) {
    val horizonY = h * 0.82f
    drawRect(
        brush = Brush.verticalGradient(listOf(Color.Transparent, color.copy(alpha = 0.18f))),
        topLeft = Offset(0f, horizonY - 100f),
        size = Size(w, 100f)
    )
}

// 09 — الهلال السحيق
private fun DrawScope.drawDeepSpaceCrescent(color: Color, w: Float, h: Float) {
    val cx = w * 0.80f
    val cy = h * 0.09f
    drawCircle(color.copy(alpha = 0.30f), radius = 22f, center = Offset(cx, cy))
    drawCircle(Color.Black.copy(alpha = 0.8f), radius = 20f, center = Offset(cx + 8f, cy - 6f))
}

// 10 — الزفير السماوي
private fun DrawScope.drawCelestialSapphire(color: Color, w: Float, h: Float) {
    val cx = w * 0.5f
    val cy = h * 0.08f
    drawCircle(
        brush = Brush.radialGradient(listOf(color.copy(alpha = 0.25f), Color.Transparent)),
        center = Offset(cx, cy),
        radius = w * 0.45f
    )
}

// 11 — الروبي الحمراء
private fun DrawScope.drawAlhambraRubyPulse(color: Color, w: Float, h: Float) {
    val size = 50f
    val cx = w * 0.85f
    val cy = h * 0.08f
    drawRect(color.copy(alpha = 0.20f), topLeft = Offset(cx - size / 2, cy - size / 2), size = Size(size, size), style = Stroke(width = 2f))
}

// 12 — الشفق المئذني
private fun DrawScope.drawAuroraMinaret(color: Color, w: Float, h: Float) {
    val path = Path().apply {
        moveTo(0f, h * 0.15f)
        cubicTo(w * 0.3f, h * 0.05f, w * 0.7f, h * 0.20f, w, h * 0.08f)
    }
    drawPath(path, color.copy(alpha = 0.22f), style = Stroke(width = 3f))
}

// 13 — أفق الصحراء الذهبي
private fun DrawScope.drawAmberDesertHorizon(color: Color, w: Float, h: Float) {
    val path = Path().apply {
        moveTo(0f, h * 0.88f)
        cubicTo(w * 0.35f, h * 0.82f, w * 0.65f, h * 0.92f, w, h * 0.85f)
        lineTo(w, h)
        lineTo(0f, h)
        close()
    }
    drawPath(path, color.copy(alpha = 0.12f))
}

// 14 — المصحف النجمي
private fun DrawScope.drawStarlightMushaf(color: Color, w: Float, h: Float) {
    val cx = w * 0.5f
    val cy = h * 0.08f
    val p1 = Path().apply {
        moveTo(cx, cy + 20f)
        lineTo(cx - 40f, cy)
        lineTo(cx - 40f, cy - 30f)
        lineTo(cx, cy - 10f)
        close()
    }
    val p2 = Path().apply {
        moveTo(cx, cy + 20f)
        lineTo(cx + 40f, cy)
        lineTo(cx + 40f, cy - 30f)
        lineTo(cx, cy - 10f)
        close()
    }
    drawPath(p1, color.copy(alpha = 0.18f))
    drawPath(p2, color.copy(alpha = 0.22f))
}

// 15 — الزيتون التيتانيوم
private fun DrawScope.drawTitaniumOlive(color: Color, w: Float, h: Float) {
    val cx = w * 0.85f
    val cy = h * 0.08f
    drawOval(color.copy(alpha = 0.20f), topLeft = Offset(cx - 15f, cy - 25f), size = Size(30f, 50f), style = Stroke(width = 2f))
}

// 16 — الفانوس الهولوغرامي
private fun DrawScope.drawHolographicLantern(color: Color, w: Float, h: Float) {
    val cx = w * 0.85f
    val cy = h * 0.09f
    val path = Path().apply {
        moveTo(cx, cy - 25f)
        lineTo(cx + 18f, cy - 10f)
        lineTo(cx + 12f, cy + 20f)
        lineTo(cx, cy + 30f)
        lineTo(cx - 12f, cy + 20f)
        lineTo(cx - 18f, cy - 10f)
        close()
    }
    drawPath(path, color.copy(alpha = 0.22f), style = Stroke(width = 2f))
}

// 17 — السمت الفيروزي
private fun DrawScope.drawZenithTurquoise(color: Color, w: Float, h: Float) {
    val cx = w * 0.5f
    val cy = h * 0.08f
    drawCircle(color.copy(alpha = 0.18f), radius = 45f, center = Offset(cx, cy), style = Stroke(width = 2f))
}

// 18 — الانديجو اللانهائي
private fun DrawScope.drawInfiniteIndigoTasbih(color: Color, w: Float, h: Float) {
    val cx = w * 0.85f
    val cy = h * 0.08f
    drawCircle(color.copy(alpha = 0.20f), radius = 30f, center = Offset(cx, cy), style = Stroke(width = 1.5f))
    drawCircle(color.copy(alpha = 0.12f), radius = 45f, center = Offset(cx, cy), style = Stroke(width = 1.5f))
}

// 19 — القبلة الكمومية
private fun DrawScope.drawQuantumQiblaEmerald(color: Color, w: Float, h: Float) {
    val cx = w * 0.85f
    val cy = h * 0.08f
    drawCircle(color.copy(alpha = 0.25f), radius = 32f, center = Offset(cx, cy), style = Stroke(width = 2f))
    drawLine(color.copy(alpha = 0.35f), Offset(cx, cy - 38f), Offset(cx, cy + 38f), strokeWidth = 2f)
    drawLine(color.copy(alpha = 0.35f), Offset(cx - 38f, cy), Offset(cx + 38f, cy), strokeWidth = 2f)
}
