package com.elhajri.noor.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.Navy
import com.elhajri.noor.ui.NavyCard
import com.elhajri.noor.ui.NavyLight

private data class QuickItem(val route: String, val title: String, val subtitle: String, val emoji: String)

@Composable
fun HomeScreen(onNavigate: (String) -> Unit) {
    val items = listOf(
        QuickItem("prayer", "مواقيت الصلاة", "أوقات الصلوات الخمس", "🕌"),
        QuickItem("quran", "المصحف الشريف", "تلاوة وحفظ واستماع", "📖"),
        QuickItem("athkar", "الأذكار", "أذكار الصباح والمساء", "📿"),
        QuickItem("tasbih", "المسبحة", "عدّاد التسبيح", "🔢"),
        QuickItem("qibla", "القبلة", "بوصلة اتجاه الكعبة", "🧭"),
        QuickItem("names", "أسماء الله الحسنى", "٩٩ اسماً ومعانيها", "✨"),
        QuickItem("quiz", "اختبار المعرفة", "أسئلة إسلامية", "🎯"),
        QuickItem("stories", "قصص الأنبياء", "قصص عبرة وهداية", "🌙"),
        QuickItem("settings", "الإعدادات", "تفضيلات التطبيق", "⚙️")
    )
    Column(modifier = Modifier.fillMaxSize().background(Navy).padding(16.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(NavyLight, NavyCard)), RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Column {
                Text("نور", style = MaterialTheme.typography.headlineLarge, color = Gold, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text("نور قلبك.. يقينك.. عبادتك", style = MaterialTheme.typography.bodyLarge, color = GoldSoft)
                Spacer(Modifier.height(4.dp))
                Text("نسخة أندرويد الأصلية", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.6f))
            }
        }
        Spacer(Modifier.height(16.dp))
        Text("الأقسام", style = MaterialTheme.typography.titleLarge, color = Gold)
        Spacer(Modifier.height(8.dp))
        LazyVerticalGrid(columns = GridCells.Fixed(3), verticalArrangement = Arrangement.spacedBy(10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(items) { item ->
                Card(
                    modifier = Modifier.aspectRatio(0.85f).clickable { onNavigate(item.route) },
                    colors = CardDefaults.cardColors(containerColor = NavyCard),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(item.emoji, style = MaterialTheme.typography.headlineMedium)
                        Spacer(Modifier.height(6.dp))
                        Text(item.title, style = MaterialTheme.typography.bodyMedium, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
