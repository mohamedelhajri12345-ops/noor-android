package com.elhajri.noor.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.res.painterResource

/**
 * التدرّجات والمكوّنات الفاخرة — نظام "الأبنوس والذهب الإمبراطوري"
 * من مواصفات فريق التصميم (design_spec.md).
 */
/**
 * خلفية الشاشة الموضوعية — صورة الثيم الحقيقية عالية الجودة (من الويب)
 * مغطاة بطبقة لونية من ألوان الثيم نفسه (شفافية مدروسة) بحيث تبقى النصوص
 * واضحة تماماً وتتغيّر هوية التطبيق كاملة مع كل ثيم: خلفية + ألوان معاً.
 */
@Composable
fun Modifier.themeScreenBackground(): Modifier {
    val def = com.elhajri.noor.theme.NoorThemeState.active
    val res = def.backgroundRes
    val painter = if (res != 0) painterResource(res) else null
    return this.drawBehind {
        var usedImage = false
        if (painter != null) {
            // 1) صورة الثيم بقصّ مركزي يملأ الشاشة (ContentScale.Crop)
            val intrinsic = painter.intrinsicSize
            if (intrinsic != Size.Unspecified && intrinsic.width > 0f && intrinsic.height > 0f) {
                val scale = maxOf(size.width / intrinsic.width, size.height / intrinsic.height)
                val drawW = intrinsic.width * scale
                val drawH = intrinsic.height * scale
                translate(
                    left = (size.width - drawW) / 2f,
                    top = (size.height - drawH) / 2f
                ) {
                    with(painter) { draw(size = Size(drawW, drawH), alpha = 1f) }
                }
                usedImage = true
            }
        }
        if (!usedImage) {
            // 2) تدرّج ألوان الثيم
            drawRect(
                brush = Brush.verticalGradient(listOf(def.background, def.backgroundEnd))
            )
        } else {
            // طبقة ألوان الثيم فوق الصورة
            drawRect(
                brush = Brush.verticalGradient(
                    listOf(
                        def.background.copy(alpha = 0.84f),
                        def.backgroundEnd.copy(alpha = 0.90f)
                    )
                )
            )
        }
        // 3) نمط الزخرفة المميز — هوية كل ثيم فوق الخلفية بشفافية ناعمة
        drawThemePattern(def.pattern, def.accent, def.background)
    }
}

/**
 * رسم النمط الهندسي المميز لكل ثيم — هوية بصرية كاملة لا مجرد ألوان.
 * يُرسم بلون التمييز بشفافية خافتة جداً حتى يعطي العمق دون تشتيت.
 */
fun DrawScope.drawThemePattern(pattern: com.elhajri.noor.theme.ThemePattern, accent: Color, bg: Color) {
    val a = accent.copy(alpha = 0.055f)
    val w = size.width
    val h = size.height
    val pi = PI.toFloat()
    when (pattern) {
        com.elhajri.noor.theme.ThemePattern.NONE -> {}
        // النجمة الثمانية الإسلامية (Girih)
        com.elhajri.noor.theme.ThemePattern.GIRIH_STAR -> {
            val t = 150f
            var y = 0f
            var row = 0
            while (y < h + t) {
                var x = if (row % 2 == 0) 0f else t / 2f
                while (x < w + t) {
                    for (k in 0 until 2) {
                        val rot = k * pi / 4f
                        val r = t * 0.32f
                        val path = Path()
                        path.moveTo(x + r * cos(rot), y + r * sin(rot))
                        path.lineTo(x + r * cos(rot + pi / 2f), y + r * sin(rot + pi / 2f))
                        path.lineTo(x + r * cos(rot + pi), y + r * sin(rot + pi))
                        path.lineTo(x + r * cos(rot + 3f * pi / 2f), y + r * sin(rot + 3f * pi / 2f))
                        path.close()
                        drawPath(path, a, style = Stroke(width = 2f))
                    }
                    x += t
                }
                y += t * 0.75f
                row++
            }
        }
        // أشعة الشمس من أعلى المنتصف
        com.elhajri.noor.theme.ThemePattern.SUN_RAYS -> {
            val cx = w / 2f
            val cy = -h * 0.15f
            for (i in 0 until 24) {
                val ang = i * pi / 24f
                val len = h * 1.3f
                drawLine(a, Offset(cx, cy), Offset(cx + len * cos(ang), cy + len * sin(ang)), strokeWidth = 2f)
            }
        }
        // نمور متوالجة
        com.elhajri.noor.theme.ThemePattern.VINE -> {
            val t = 130f
            var y = t / 2f
            while (y < h + t) {
                val path = Path()
                path.moveTo(0f, y)
                var x = 0f
                while (x < w) {
                    path.cubicTo(x + t * 0.25f, y - 26f, x + t * 0.75f, y + 26f, x + t, y)
                    x += t
                }
                drawPath(path, a, style = Stroke(width = 2f))
                y += t
            }
        }
        // صفوف أقواس
        com.elhajri.noor.theme.ThemePattern.ARCHES -> {
            val t = 110f
            var x = 0f
            while (x < w) {
                val path = Path()
                path.moveTo(x, h)
                path.lineTo(x, h - t * 0.45f)
                path.addArc(Rect(x, h - t * 0.9f, x + t * 0.7f, h - t * 0.2f), 180f, 180f)
                path.lineTo(x + t * 0.7f, h)
                drawPath(path, a.copy(alpha = 0.08f), style = Stroke(width = 2f))
                x += t * 0.72f
            }
        }
        // سُدُم متناثرة
        com.elhajri.noor.theme.ThemePattern.STARDUST -> {
            val t = 120f
            var y = 40f
            var row = 0
            while (y < h) {
                var x = if (row % 2 == 0) 60f else t / 2f
                while (x < w) {
                    val r = 5f
                    val path = Path()
                    path.moveTo(x - r, y); path.lineTo(x + r, y)
                    path.moveTo(x, y - r); path.lineTo(x, y + r)
                    drawPath(path, a)
                    drawCircle(a, radius = 1.6f, center = Offset(x, y))
                    x += t
                }
                y += t * 0.9f
                row++
            }
        }
        // سُحُب ناعمة
        com.elhajri.noor.theme.ThemePattern.CLOUDS -> {
            var y = 90f
            var row = 0
            while (y < h * 0.5f) {
                var x = if (row % 2 == 0) 50f else 190f
                while (x < w) {
                    drawCircle(a.copy(alpha = 0.04f), radius = 46f, center = Offset(x, y))
                    drawCircle(a.copy(alpha = 0.035f), radius = 34f, center = Offset(x + 42f, y + 10f))
                    drawCircle(a.copy(alpha = 0.03f), radius = 30f, center = Offset(x - 38f, y + 8f))
                    x += 230f
                }
                y += 120f
                row++
            }
        }
        // أشرطة ذهبية عمودية على الحواف
        com.elhajri.noor.theme.ThemePattern.GOLD_BANDS -> {
            drawRect(a, topLeft = Offset(0f, 0f), size = Size(10f, h))
            drawRect(a, topLeft = Offset(w - 10f, 0f), size = Size(10f, h))
            drawRect(a.copy(alpha = 0.028f), topLeft = Offset(26f, 0f), size = Size(3f, h))
            drawRect(a.copy(alpha = 0.028f), topLeft = Offset(w - 29f, 0f), size = Size(3f, h))
        }
        // أهلّة متناثرة (دائرة ذهبية تُقتطع بدائرة بلون الخلفية)
        com.elhajri.noor.theme.ThemePattern.CRESCENTS -> {
            val t = 150f
            val bgA = bg.copy(alpha = 0.055f)
            var y = 60f
            var row = 0
            while (y < h) {
                var x = if (row % 2 == 0) 80f else t / 2f
                while (x < w) {
                    drawCircle(a, radius = 14f, center = Offset(x, y))
                    drawCircle(bgA, radius = 12f, center = Offset(x + 7f, y - 3f))
                    x += t
                }
                y += t * 0.8f
                row++
            }
        }
        // كثبان متوالجة
        com.elhajri.noor.theme.ThemePattern.DUNES -> {
            var y = h * 0.25f
            var row = 0
            while (y < h + 60f) {
                val path = Path()
                path.moveTo(0f, y)
                var x = 0f
                val amp = if (row % 2 == 0) 34f else -34f
                while (x < w) {
                    path.cubicTo(x + w * 0.17f, y - amp, x + w * 0.5f, y - amp, x + w * 0.66f, y)
                    x += w * 0.66f
                }
                drawPath(path, a, style = Stroke(width = 2.5f))
                y += 150f
                row++
            }
        }
        // زليج مغربي — معينات متراكبة
        com.elhajri.noor.theme.ThemePattern.ZELLIGE -> {
            val t = 90f
            var y = 0f
            var row = 0
            while (y < h + t) {
                var x = if (row % 2 == 0) 0f else t / 2f
                while (x < w + t) {
                    val path = Path()
                    path.moveTo(x, y - t * 0.35f)
                    path.lineTo(x + t * 0.35f, y)
                    path.lineTo(x, y + t * 0.35f)
                    path.lineTo(x - t * 0.35f, y)
                    path.close()
                    drawPath(path, a, style = Stroke(width = 2f))
                    x += t
                }
                y += t * 0.7f
                row++
            }
        }
        // أزهار متكررة
        com.elhajri.noor.theme.ThemePattern.FLOWERS -> {
            val t = 140f
            var y = 70f
            var row = 0
            while (y < h + t) {
                var x = if (row % 2 == 0) 70f else t / 2f
                while (x < w + t) {
                    for (p in 0 until 4) {
                        val ang = p * pi / 2f
                        drawCircle(a, radius = 13f, center = Offset(x + 16f * cos(ang), y + 16f * sin(ang)))
                    }
                    drawCircle(a, radius = 5f, center = Offset(x, y))
                    x += t
                }
                y += t * 0.85f
                row++
            }
        }
        // أمواج بحرية
        com.elhajri.noor.theme.ThemePattern.WAVES -> {
            var y = h * 0.2f
            while (y < h) {
                val path = Path()
                path.moveTo(0f, y)
                var x = 0f
                while (x < w) {
                    path.cubicTo(x + 40f, y - 16f, x + 80f, y + 16f, x + 120f, y)
                    x += 120f
                }
                drawPath(path, a, style = Stroke(width = 2f))
                y += 95f
            }
        }
        // شبكة كوفية دقيقة
        com.elhajri.noor.theme.ThemePattern.KUFIC -> {
            val t = 80f
            var y = 0f
            while (y < h) { drawLine(a, Offset(0f, y), Offset(w, y), strokeWidth = 1f); y += t }
            var x = 0f
            while (x < w) { drawLine(a, Offset(x, 0f), Offset(x, h), strokeWidth = 1f); x += t }
        }
        // صفوف قباب علوية
        com.elhajri.noor.theme.ThemePattern.DOMES -> {
            val t = 160f
            var x = -t * 0.25f
            while (x < w + t) {
                val path = Path()
                path.addArc(Rect(x, 20f, x + t * 0.8f, 170f), 180f, 180f)
                drawPath(path, a.copy(alpha = 0.07f), style = Stroke(width = 2f))
                drawLine(a, Offset(x + t * 0.4f, 14f), Offset(x + t * 0.4f, 20f), strokeWidth = 3f)
                x += t * 0.66f
            }
        }
        // فوانيس معلقة
        com.elhajri.noor.theme.ThemePattern.LANTERNS -> {
            val t = 170f
            var y = 30f
            var row = 0
            while (y < h * 0.55f) {
                var x = if (row % 2 == 0) 90f else t / 2f
                while (x < w + t) {
                    drawLine(a, Offset(x, (y - 30f).coerceAtLeast(0f)), Offset(x, y), strokeWidth = 1f)
                    val path = Path()
                    path.moveTo(x, y)
                    path.lineTo(x + 13f, y + 10f)
                    path.lineTo(x + 13f, y + 42f)
                    path.lineTo(x, y + 52f)
                    path.lineTo(x - 13f, y + 42f)
                    path.lineTo(x - 13f, y + 10f)
                    path.close()
                    drawPath(path, a.copy(alpha = 0.07f), style = Stroke(width = 1.8f))
                    drawCircle(a, radius = 2f, center = Offset(x, y + 58f))
                    x += t
                }
                y += 200f
                row++
            }
        }
    }
}


object NoorGradients {

    private val T get() = com.elhajri.noor.theme.NoorThemeState.active

    /** خلفية الشاشات: تدرّج الثيم الحالي */
    val ScreenBackground get() = Brush.verticalGradient(
        colors = listOf(T.background, T.backgroundEnd)
    )

    /** اللون المعدني للأزرار والعناوين الكبرى — ألوان الثيم */
    val ImperialGoldMetallic get() = Brush.linearGradient(
        colors = listOf(T.accentSoft, T.accent, T.accent, T.accentDeep)
    )

    /** تدرّج الأزرار الناعم — ألوان الثيم */
    val GoldButton get() = Brush.horizontalGradient(
        colors = listOf(T.accentSoft, T.accent, T.accentDeep)
    )

    /** حدود البطاقات الزجاجية بألوان الثيم */
    val GlassBorderGold get() = Brush.linearGradient(
        colors = listOf(
            T.accentSoft.copy(alpha = 0.50f),
            T.accent.copy(alpha = 0.15f),
            T.accentSoft.copy(alpha = 0.35f)
        )
    )

    /** تعبئة البطاقات الزجاجية شبه الشفافة — سطح الثيم */
    val GlassSurface get() = Brush.linearGradient(
        colors = listOf(
            T.surfaceVariant.copy(alpha = 0.17f),
            T.surface.copy(alpha = 0.10f)
        )
    )

    /** الخط الفاصل الذهبي التلاشي في الطرفين */
    val GoldenDivider = Brush.horizontalGradient(
        colors = listOf(
            Color.Transparent,
            ChampagneGold.copy(alpha = 0.60f),
            Gold.copy(alpha = 0.85f),
            ChampagneGold.copy(alpha = 0.60f),
            Color.Transparent
        )
    )

    /** تدرّج نصوص العناوين والآيات */
    val GoldText = Brush.linearGradient(
        colors = listOf(
            ChampagneGold,
            ImperialGold,
            RoseGoldAccent
        )
    )
}

/**
 * معدّل البطاقة الزجاجية الفاخرة — استخدامه:
 * Modifier.noorGlassCard()  أو  Modifier.noorGlassCard(cornerRadius = 14.dp)
 */
fun Modifier.noorGlassCard(
    cornerRadius: Dp = 20.dp,
    borderWidth: Dp = 1.dp,
    shape: Shape = RoundedCornerShape(cornerRadius)
): Modifier = this
    .shadow(
        elevation = 10.dp,
        shape = shape,
        ambientColor = Color(0x99000000),
        spotColor = Gold.copy(alpha = 0.15f)
    )
    .clip(shape)
    .background(NoorGradients.GlassSurface)
    .border(
        width = borderWidth,
        brush = NoorGradients.GlassBorderGold,
        shape = shape
    )

/** الخط الذهبي الرفيع الفاصل بين الأقسام */
@Composable
fun NoorGoldenDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(NoorGradients.GoldenDivider)
    )
}
