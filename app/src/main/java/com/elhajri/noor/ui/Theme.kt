package com.elhajri.noor.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.R
import com.elhajri.noor.theme.NoorThemeState

val TajawalFamily = FontFamily(
    Font(R.font.tajawal_regular, FontWeight.Normal),
    Font(R.font.tajawal_bold, FontWeight.Bold)
)

val AmiriFamily = FontFamily(
    Font(R.font.amiri_regular, FontWeight.Normal),
    Font(R.font.amiri_bold, FontWeight.Bold)
)

// Dynamic theme colors deriving from NoorThemeState
val Navy get() = NoorThemeState.active.background
val NavyLight get() = NoorThemeState.active.surfaceVariant.copy(alpha = 0.18f)
val NavyCard get() = NoorThemeState.active.surfaceVariant.copy(alpha = 0.12f)
val Gold get() = NoorThemeState.active.accent
val GoldSoft get() = NoorThemeState.active.accentSoft
val TextMain get() = NoorThemeState.active.textPrimary
val ChampagneGold get() = NoorThemeState.active.accentSoft
val ImperialGold get() = NoorThemeState.active.accent
val AntiqueGoldDeep get() = NoorThemeState.active.accentDeep
val GoldHighlight get() = NoorThemeState.active.accentSoft
val RoseGoldAccent get() = NoorThemeState.active.accentDeep
val TextSecondaryLinen get() = NoorThemeState.active.textSecondary
val EmeraldSuccess get() = NoorThemeState.active.success
val RubyError get() = NoorThemeState.active.error

val NoorColors get() = darkColorScheme(
    primary = Gold,
    onPrimary = Navy,
    secondary = GoldSoft,
    onSecondary = Navy,
    background = Navy,
    onBackground = TextMain,
    surface = NavyCard,
    onSurface = TextMain,
    surfaceVariant = NavyLight,
    onSurfaceVariant = GoldSoft,
    outline = Gold.copy(alpha = 0.4f)
)

private fun tajawal(size: Int, weight: FontWeight, line: Int? = null): TextStyle {
    val lh = line?.toFloat() ?: (size * 1.7f)
    return TextStyle(
        fontWeight = weight,
        fontSize = size.sp,
        lineHeight = lh.sp,
        fontFamily = TajawalFamily
    )
}

private val NoorTypography = Typography(
    displayLarge = tajawal(34, FontWeight.Bold),
    displayMedium = tajawal(30, FontWeight.Bold),
    displaySmall = tajawal(26, FontWeight.Bold),
    headlineLarge = tajawal(28, FontWeight.Bold),
    headlineMedium = tajawal(22, FontWeight.Bold),
    headlineSmall = tajawal(18, FontWeight.Bold),
    titleLarge = tajawal(18, FontWeight.Bold),
    titleMedium = tajawal(16, FontWeight.SemiBold),
    titleSmall = tajawal(14, FontWeight.SemiBold),
    bodyLarge = tajawal(16, FontWeight.Normal, 28),
    bodyMedium = tajawal(14, FontWeight.Normal, 24),
    bodySmall = tajawal(12, FontWeight.Normal, 20),
    labelLarge = tajawal(14, FontWeight.Medium),
    labelMedium = tajawal(12, FontWeight.Medium),
    labelSmall = tajawal(11, FontWeight.Medium)
)

@Composable
fun NoorTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NoorColors,
        typography = NoorTypography,
        shapes = Shapes(
            extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
            small = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
            medium = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
            large = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
            extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(28.dp)
        ),
        content = content
    )
}
