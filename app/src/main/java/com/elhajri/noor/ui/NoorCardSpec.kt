package com.elhajri.noor.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.theme.noorGlassCard

/**
 * UNIFORM CARD SIZING SPECIFICATION (NOOR v7.0)
 *
 * Guarantees zero size scatter or layout jumping caused by text length variations.
 * English and Arabic locales retain identical card dimensions, paddings, and alignment.
 */
object NoorCardSpec {
    // Shared Fixed Height Metrics
    val HeightCompactTile: Dp = 80.dp
    val HeightStandardGridCard: Dp = 135.dp
    val HeightFeatureCard: Dp = 160.dp
    val HeightListRow: Dp = 76.dp
    val HeightHeroBanner: Dp = 200.dp
    val HeightBookCoverPortrait: Dp = 185.dp
    val HeightGameCard: Dp = 145.dp

    val CornerRadiusDefault: Dp = 18.dp
    val BorderWidthDefault: Dp = 1.dp
}

/**
 * A uniform container modifier applying fixed height, glass styling, clipping, and optional click listener.
 */
fun Modifier.uniformCardSpec(
    fixedHeight: Dp = NoorCardSpec.HeightStandardGridCard,
    cornerRadius: Dp = NoorCardSpec.CornerRadiusDefault,
    borderWidth: Dp = NoorCardSpec.BorderWidthDefault,
    shape: Shape = RoundedCornerShape(cornerRadius)
): Modifier = this
    .fillMaxWidth()
    .height(fixedHeight)
    .noorGlassCard(cornerRadius = cornerRadius, borderWidth = borderWidth, shape = shape)

/**
 * Uniform Grid Card Composable with guaranteed height and text ellipsis truncation.
 */
@Composable
fun NoorUniformGridCard(
    title: String,
    subtitle: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = NoorCardSpec.HeightGridCard,
    badgeText: String? = null
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .noorGlassCard(cornerRadius = NoorCardSpec.CornerRadiusDefault)
            .clickable { onClick() },
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.Start
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Gold.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        icon()
                    }

                    if (!badgeText.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Gold.copy(alpha = 0.20f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = badgeText,
                                color = Gold,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Start
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        color = GoldSoft.copy(alpha = 0.75f),
                        fontSize = 11.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Start
                    )
                }
            }
        }
    }
}

// Alias helper
val NoorCardSpec.HeightGridCard get() = HeightStandardGridCard
