package com.elhajri.noor.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.elhajri.noor.theme.GlassRecipe
import com.elhajri.noor.theme.drawThemeMotif
import com.elhajri.noor.theme.noorGlassCard as themeNoorGlassCard

/**
 * Background for themed screens — code-drawn vertical gradient + radial accent glow + signature motif.
 */
@Composable
fun Modifier.themeScreenBackground(): Modifier {
    val def = com.elhajri.noor.theme.NoorThemeState.active
    return this.drawBehind {
        // 1) Vertical background gradient
        drawRect(
            brush = Brush.verticalGradient(listOf(def.background, def.backgroundEnd))
        )
        // 2) Radial accent glow
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
        // 3) Theme motif
        drawThemeMotif(def)

        // 4) Bottom lighting gradient
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
 * Glass card modifier delegating to the canonical GlassRecipe in theme package.
 */
fun Modifier.noorGlassCard(
    cornerRadius: Dp = 20.dp,
    borderWidth: Dp = 1.dp,
    shape: Shape = RoundedCornerShape(cornerRadius)
): Modifier = this.themeNoorGlassCard(
    cornerRadius = cornerRadius,
    borderWidth = borderWidth,
    shape = shape
)

/** Thin golden divider line */
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

    /** Screen background gradient */
    val ScreenBackground get() = Brush.verticalGradient(
        colors = listOf(T.background, T.backgroundEnd)
    )

    /** Imperial metallic gradient */
    val ImperialGoldMetallic get() = Brush.linearGradient(
        colors = listOf(T.accentSoft, T.accent, T.accent, T.accentDeep)
    )

    /** Button gradient */
    val GoldButton get() = Brush.horizontalGradient(
        colors = listOf(T.accentSoft, T.accent, T.accentDeep)
    )

    /** Glass card border gradient */
    val GlassBorderGold get() = GlassRecipe.borderGradient

    /** Glass surface gradient */
    val GlassSurface get() = GlassRecipe.surfaceGradient

    /** Fading divider gradient */
    val GoldenDivider = Brush.horizontalGradient(
        colors = listOf(
            Color.Transparent,
            ChampagneGold.copy(alpha = 0.60f),
            Gold.copy(alpha = 0.85f),
            ChampagneGold.copy(alpha = 0.60f),
            Color.Transparent
        )
    )

    /** Text gradient */
    val GoldText = Brush.linearGradient(
        colors = listOf(
            ChampagneGold,
            ImperialGold,
            RoseGoldAccent
        )
    )
}
