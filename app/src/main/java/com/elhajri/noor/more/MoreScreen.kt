package com.elhajri.noor.more

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.ui.AmiriFamily
import com.elhajri.noor.ui.noorGlassCard
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.NavyCard

/**
 * Exact port of the web More.jsx: 16 feature cards in a 2-column grid,
 * each with a gold icon chip, bold label and muted description.
 * 100% native Compose — zero WebView.
 */
private data class MoreFeature(
    val route: String,
    val label: String,
    val desc: String,
    val icon: ImageVector
)

private val features = listOf(
    MoreFeature("favorites", "المفضلة", "السور والقصص المحفوظة", Icons.Filled.Favorite),
    MoreFeature("community", "المجتمع", "دردشة الأعضاء", Icons.Filled.Groups),
    MoreFeature("prayer", "الصلاة", "أوقات الصلاة", Icons.Filled.Schedule),
    MoreFeature("qibla", "القبلة", "اتجاه القبلة", Icons.Filled.Explore),
    MoreFeature("library", "المكتبة", "الأناشيد", Icons.Filled.MusicNote),
    MoreFeature("journal", "المفكرة", "تدوين الخواطر", Icons.Filled.EditNote),
    MoreFeature("calendar", "التقويم", "المناسبات الإسلامية", Icons.Filled.EditCalendar),
    MoreFeature("stories", "قصص الأنبياء", "قصص ملهمة", Icons.Filled.HistoryEdu),
    MoreFeature("quiz", "الاختبار الديني", "أسئلة وأجوبة", Icons.Filled.Help),
    MoreFeature("tasbih", "السبحة", "عدّاد الذكر", Icons.Filled.Repeat),
    MoreFeature("games", "الألعاب", "ألعاب إسلامية", Icons.Filled.Casino),
    MoreFeature("ai", "المساعد الذكي", "ذكاء اصطناعي إسلامي", Icons.Filled.AutoAwesome),
    MoreFeature("zakat", "حاسبة الزكاة", "احسب زكاتك", Icons.Filled.Paid),
    MoreFeature("hajj", "الحج والعمرة", "دليل المصطافين", Icons.Filled.Bookmark),
    MoreFeature("tracker", "ورد القرآن", "متتبع التلاوة", Icons.Filled.TaskAlt),
    MoreFeature("themes", "متجر الثيمات", "ثيمات إسلامية بالنقاط", Icons.Filled.Palette),
    MoreFeature("settings", "الإعدادات", "إعدادات التطبيق", Icons.Filled.Settings)
)

@Composable
fun MoreScreen(onNavigate: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp)
    ) {
        Text(
            "الأقسام",
            color = Gold,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = AmiriFamily,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(features) { f ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .noorGlassCard(cornerRadius = 16.dp)
                        .clickable { onNavigate(f.route) }
                        .padding(14.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Gold.copy(alpha = 0.1f))
                    ) {
                        Icon(
                            f.icon,
                            contentDescription = f.label,
                            tint = Gold,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        f.label,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        f.desc,
                        color = GoldSoft.copy(alpha = 0.55f),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // إعلان سفلي آمن — تحت الشبكة داخل التخطيط، لا يغطي أي زر أو محتوى
        com.elhajri.noor.ui.NoorBannerAd()
        Spacer(Modifier.height(10.dp))
    }
}
