package com.elhajri.noor.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Skeleton Shimmer Loading Modifier & Placeholders for NOOR v7.0
 * Replaces infinite spinners with smooth linear metallic shimmer waves.
 */
fun Modifier.shimmerLoading(
    shape: RoundedCornerShape = RoundedCornerShape(16.dp)
): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_anim"
    )

    val activeTheme = com.elhajri.noor.theme.NoorThemeState.active
    val baseColor = activeTheme.surfaceVariant.copy(alpha = 0.35f)
    val highlightColor = activeTheme.accent.copy(alpha = 0.25f)

    val shimmerBrush = Brush.linearGradient(
        colors = listOf(baseColor, highlightColor, baseColor),
        start = Offset(translateAnim.value - 300f, translateAnim.value - 300f),
        end = Offset(translateAnim.value, translateAnim.value)
    )

    this
        .clip(shape)
        .background(shimmerBrush)
}

@Composable
fun SkeletonCard(
    height: Dp = 120.dp,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .shimmerLoading(shape = RoundedCornerShape(cornerRadius))
    )
}

@Composable
fun SkeletonListColumn(
    count: Int = 4,
    cardHeight: Dp = 80.dp,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        repeat(count) {
            SkeletonCard(height = cardHeight)
        }
    }
}
