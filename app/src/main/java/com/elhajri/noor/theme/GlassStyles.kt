package com.elhajri.noor.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape

/**
 * Glassmorphism surface recipes for NOOR v7.0
 * Fully reactive to NoorThemeState.active.
 */
object GlassRecipe {
    val active get() = NoorThemeState.active

    val surfaceGradient get() = Brush.verticalGradient(
        colors = listOf(
            active.surface.copy(alpha = 0.45f),
            active.surfaceVariant.copy(alpha = 0.28f)
        )
    )

    val borderGradient get() = Brush.linearGradient(
        colors = listOf(
            active.accent.copy(alpha = 0.50f),
            active.accentSoft.copy(alpha = 0.20f),
            active.accentDeep.copy(alpha = 0.40f)
        )
    )
}

fun Modifier.noorGlassCard(
    cornerRadius: Dp = 18.dp,
    borderWidth: Dp = 1.dp,
    shape: Shape = RoundedCornerShape(cornerRadius)
): Modifier {
    val active = NoorThemeState.active
    val cardStyle = active.cardStyle

    val effectiveRadius = if (cornerRadius != 18.dp) cornerRadius else cardStyle.radius
    val effectiveShape = RoundedCornerShape(effectiveRadius)

    return this
        .clip(effectiveShape)
        .background(GlassRecipe.surfaceGradient)
        .border(
            width = borderWidth,
            brush = GlassRecipe.borderGradient,
            shape = effectiveShape
        )
}
