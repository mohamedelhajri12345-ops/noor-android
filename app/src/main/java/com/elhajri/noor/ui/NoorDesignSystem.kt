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

/**
 * التدرّجات والمكوّنات الفاخرة — نظام "الأبنوس والذهب الإمبراطوري"
 * من مواصفات فريق التصميم (design_spec.md).
 */
object NoorGradients {

    /** خلفية الشاشات: من الأعلى بلمسة ليلية فاخرة إلى القاع الأبنيسي الداكن */
    val ScreenBackground = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0A0E14),
            Color(0xFF0A0E14)
        )
    )

    /** الذهب المعدني الإمبراطوري للأزرار والعناوين الكبرى */
    val ImperialGoldMetallic = Brush.linearGradient(
        colors = listOf(
            GoldHighlight,
            ImperialGold,
            Gold,
            AntiqueGoldDeep
        )
    )

    /** تدرّج أزرار الذهب الناعم */
    val GoldButton = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFFE8C96A),
            Color(0xFFD4AF37),
            Color(0xFFB8941F)
        )
    )

    /** حدود البطاقات الزجاجية الذهبية */
    val GlassBorderGold = Brush.linearGradient(
        colors = listOf(
            ChampagneGold.copy(alpha = 0.50f),
            Gold.copy(alpha = 0.15f),
            ChampagneGold.copy(alpha = 0.35f)
        )
    )

    /** تعبئة البطاقات الزجاجية شبه الشفافة */
    val GlassSurface = Brush.linearGradient(
        colors = listOf(
            Color(0x2B24334C),
            Color(0x1A141F33)
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
