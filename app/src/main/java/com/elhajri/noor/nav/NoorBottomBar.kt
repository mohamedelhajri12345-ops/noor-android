package com.elhajri.noor.nav

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.Navy
import com.elhajri.noor.ui.NavyCard
import com.elhajri.noor.ui.NoorGradients
import com.elhajri.noor.ui.TajawalFamily

data class BottomBarTab(
    val route: String,
    val label: String,
    val activeIcon: ImageVector,
    val inactiveIcon: ImageVector
)

val bottomBarTabs = listOf(
    BottomBarTab("home", "الرئيسية", Icons.Filled.Home, Icons.Outlined.Home),
    BottomBarTab("quran", "القرآن", Icons.Filled.MenuBook, Icons.Outlined.MenuBook),
    BottomBarTab("tasbih", "السبحة", Icons.Filled.TouchApp, Icons.Outlined.TouchApp),
    BottomBarTab("games", "الألعاب", Icons.Filled.SportsEsports, Icons.Outlined.SportsEsports),
    BottomBarTab("more", "المزيد", Icons.Filled.GridView, Icons.Outlined.GridView)
)

/**
 * 2026 Standard Bottom Bar for NOOR App:
 * - Glass background with translucent dark gradient and subtle gold border.
 * - Smooth animated selection pill + glowing active indicator dot.
 * - Theme-aware icon tints (selected = active accent 'Gold', unselected = muted).
 * - Native Material ripple feedback on press.
 * - Proper safe-area navigation bar padding.
 */
@Composable
fun NoorBottomBar(
    currentRoute: String?,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 16.dp, ambientColor = Color.Black, spotColor = Gold.copy(alpha = 0.20f))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        NavyCard.copy(alpha = 0.6f),
                        Navy.copy(alpha = 0.96f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = NoorGradients.GlassBorderGold,
                shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp)
            )
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            bottomBarTabs.forEach { tab ->
                val selected = currentRoute == tab.route

                val iconScale by animateFloatAsState(
                    targetValue = if (selected) 1.12f else 1.0f,
                    animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
                    label = "iconScale"
                )

                val activePillAlpha by animateFloatAsState(
                    targetValue = if (selected) 1.0f else 0.0f,
                    animationSpec = tween(durationMillis = 200),
                    label = "activePillAlpha"
                )

                val activeColor = Gold
                val inactiveColor = GoldSoft.copy(alpha = 0.48f)

                val iconTint by animateColorAsState(
                    targetValue = if (selected) activeColor else inactiveColor,
                    animationSpec = tween(durationMillis = 180),
                    label = "iconTint"
                )

                val interactionSource = remember { MutableInteractionSource() }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(
                            interactionSource = interactionSource,
                            indication = rememberRipple(bounded = true, color = Gold.copy(alpha = 0.25f)),
                            onClick = { onTabSelected(tab.route) }
                        )
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Icon container with active glowing pill background
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(width = 44.dp, height = 28.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (selected) Gold.copy(alpha = 0.16f * activePillAlpha) else Color.Transparent
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (selected) Gold.copy(alpha = 0.35f * activePillAlpha) else Color.Transparent,
                                    shape = RoundedCornerShape(14.dp)
                                )
                        ) {
                            Icon(
                                imageVector = if (selected) tab.activeIcon else tab.inactiveIcon,
                                contentDescription = tab.label,
                                tint = iconTint,
                                modifier = Modifier
                                    .size(20.dp)
                                    .graphicsLayer {
                                        scaleX = iconScale
                                        scaleY = iconScale
                                    }
                            )
                        }

                        Spacer(Modifier.height(2.dp))

                        Text(
                            text = tab.label,
                            color = iconTint,
                            fontSize = 10.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            fontFamily = TajawalFamily,
                            maxLines = 1
                        )

                        Spacer(Modifier.height(2.dp))

                        // Animated gold dot under the active tab
                        Box(
                            modifier = Modifier
                                .size(width = if (selected) 12.dp else 0.dp, height = 3.dp)
                                .clip(CircleShape)
                                .background(Gold.copy(alpha = activePillAlpha))
                        )
                    }
                }
            }
        }
    }
}
