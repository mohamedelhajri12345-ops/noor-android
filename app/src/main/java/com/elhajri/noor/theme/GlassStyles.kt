package com.elhajri.noor.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Canonical Glass Recipe for the NOOR Android app visual identity.
 *
 * All cards across all 21 themes (and default) derive from this single unified formula:
 * - Translucent fill with simulated top-light vertical gradient (~0.07 - 0.14 alpha)
 * - Thin 1dp gold/accent border using theme accent with reduced alpha
 * - Soft elevation shadow with ambient dark & accent spot lighting
 * - Rounded 20-28dp corners
 */
object GlassRecipe {
    val activeTheme: NoorThemeDef get() = NoorThemeState.active

    /** Top-light vertical gradient for simulated glass lighting */
    val surfaceGradient: Brush
        get() {
            val T = activeTheme
            return Brush.verticalGradient(
                colors = listOf(
                    T.accentSoft.copy(alpha = 0.14f),
                    T.surfaceVariant.copy(alpha = 0.10f),
                    T.surface.copy(alpha = 0.07f)
                )
            )
        }

    /** Thin border gradient using theme accent */
    val borderGradient: Brush
        get() {
            val T = activeTheme
            return Brush.linearGradient(
                colors = listOf(
                    T.accentSoft.copy(alpha = 0.40f),
                    T.accent.copy(alpha = 0.15f),
                    T.accentSoft.copy(alpha = 0.25f)
                )
            )
        }

    @Composable
    fun cardColors() = CardDefaults.cardColors(
        containerColor = Color.Transparent,
        contentColor = activeTheme.textPrimary
    )
}

/**
 * Unified Modifier for glass card styling.
 */
fun Modifier.noorGlassCard(
    cornerRadius: Dp = 20.dp,
    borderWidth: Dp = 1.dp,
    shape: Shape = RoundedCornerShape(cornerRadius),
    elevation: Dp = 0.dp
): Modifier {
    val T = NoorThemeState.active
    // NOTE: no shadow() on purpose. A shadow drawn under a translucent fill shows through
    // the glass as a dark rectangular patch (visible "squares" with hard corners) and its
    // tone changes with whatever sits behind each card. Depth comes from a uniform fill,
    // a soft top highlight and a thin gradient border instead, so every card looks identical.
    return this
        .clip(shape)
        .background(GLASS_BASE_FILL)
        .background(
            brush = Brush.verticalGradient(
                colors = listOf(
                    T.accentSoft.copy(alpha = 0.10f),
                    Color.Transparent
                )
            )
        )
        .border(
            width = borderWidth,
            brush = Brush.linearGradient(
                colors = listOf(
                    T.accentSoft.copy(alpha = 0.38f),
                    T.accent.copy(alpha = 0.16f),
                    T.accentSoft.copy(alpha = 0.28f)
                )
            ),
            shape = shape
        )
}

/** One single uniform glass tint used by every card in the app (no per-card variation). */
private val GLASS_BASE_FILL: Color
    get() = Color.White.copy(alpha = 0.055f)

/**
 * Extension for applying glass style with explicit corner radius in 20-28dp range.
 */
fun Modifier.noorGlassStyle(
    cornerRadius: Dp = 24.dp,
    borderWidth: Dp = 1.dp,
    elevation: Dp = 0.dp
): Modifier = noorGlassCard(
    cornerRadius = cornerRadius,
    borderWidth = borderWidth,
    elevation = elevation
)

/**
 * Reusable Composable for wrapping content inside a canonical glass card.
 */
@Composable
fun NoorGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    borderWidth: Dp = 1.dp,
    elevation: Dp = 0.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val cardModifier = modifier.noorGlassCard(
        cornerRadius = cornerRadius,
        borderWidth = borderWidth,
        elevation = elevation
    )
    val finalModifier = if (onClick != null) {
        cardModifier.clickable(onClick = onClick)
    } else {
        cardModifier
    }

    Box(modifier = finalModifier) {
        Column {
            content()
        }
    }
}
