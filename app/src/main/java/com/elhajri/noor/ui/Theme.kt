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

val TajawalFamily = FontFamily(
    Font(R.font.tajawal_regular, FontWeight.Normal),
    Font(R.font.tajawal_bold, FontWeight.Bold)
)

val AmiriFamily = FontFamily(
    Font(R.font.amiri_regular, FontWeight.Normal),
    Font(R.font.amiri_bold, FontWeight.Bold)
)


// ============================================================================
// لوحة "الأبنوس والذهب الإمبراطوري" الفاخرة — تحديث فريق التصميم
// الأسماء نفسها حتى تتجدد كل الشاشات تلقائياً
// ============================================================================
val Navy = Color(0xFF070B14)              // أبنوس مخملي عميق (كان 0B1B3A)
val NavyLight = Color(0xFF152238)         // كحلي ملكي أدفأ
val NavyCard = Color(0xFF1B2B4B)          // بطاقات أفتح وضوحاً
val Gold = Color(0xFFF0C75E)              // ذهب شمبانيا متوهج — تباين أعلى
val GoldSoft = Color(0xFFFFE9B8)          // ذهب فاتح مشع
val TextMain = Color(0xFFFAF8F5)          // أبيض لؤلؤي دافئ
val ChampagneGold = Color(0xFFF3E5AB)
val ImperialGold = Color(0xFFE5C158)
val AntiqueGoldDeep = Color(0xFF997A15)
val GoldHighlight = Color(0xFFFFF2A1)
val RoseGoldAccent = Color(0xFFE5A983)
val TextSecondaryLinen = Color(0xFFC5BAA8)
val EmeraldSuccess = Color(0xFF10B981)
val RubyError = Color(0xFFEF4444)

val NoorColors = darkColorScheme(
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

private val NoorTypography = Typography(
    headlineLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 28.sp, fontFamily = TajawalFamily),
    headlineMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 22.sp, fontFamily = TajawalFamily),
    headlineSmall = TextStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp, fontFamily = TajawalFamily),
    titleLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp, fontFamily = TajawalFamily),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp, fontFamily = TajawalFamily),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 28.sp, fontFamily = TajawalFamily),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 24.sp, fontFamily = TajawalFamily),
    labelLarge = TextStyle(fontWeight = FontWeight.Medium, fontSize = 14.sp, fontFamily = TajawalFamily)
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
