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
 * خلفية الشاشة الموضوعية — مرسومة بالكود بالكامل (تدرّج رأسي من ألوان الثيم
 * + توهّج قطري ناعم بلون التمييز) دون أي صور ويب ودون زخارف هندسية.
 * النتيجة: نصوص واضحة تماماً في كل الثيمات وهوية تتبدل كاملة مع كل ثيمة.
 */
@Composable
fun Modifier.themeScreenBackground(): Modifier {
    val def = com.elhajri.noor.theme.NoorThemeState.active
    return this.drawBehind {
        // 1) التدرّج الأساسي الرأسي من ألوان الثيم
        drawRect(
            brush = Brush.verticalGradient(listOf(def.background, def.backgroundEnd))
        )
        // 2) توهّج قطري ناعم بلون التمييز — عمق ثلاثي الأبعاد بلا صور
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    def.accent.copy(alpha = 0.10f),
                    Color.Transparent
                ),
                center = Offset(x = size.width * 0.80f, y = size.height * 0.06f),
                radius = size.width.coerceAtLeast(size.height) * 1.10f
            )
        )
        // 3) إضاءة سفلية خفيفة تمنح البطاقات تبايناً أعلى وخطاً أوضح
        drawRect(
            brush = Brush.verticalGradient(
                0f to Color.Transparent,
                0.72f to def.backgroundEnd.copy(alpha = 0.55f),
                1f to def.backgroundEnd
            )
        )
    }
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
