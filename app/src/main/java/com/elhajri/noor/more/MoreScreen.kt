package com.elhajri.noor.more

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.Navy
import com.elhajri.noor.ui.NavyCard

@Composable
fun MoreScreen(onNavigate: (String) -> Unit) {
    val entries = listOf(
        "favorites" to "❤️ المفضلة",
        "community" to "👥 المجتمع",
        "prayer" to "🕌 الصلاة",
        "qibla" to "🧭 القبلة",
        "library" to "📚 المكتبة",
        "journal" to "📔 المفكرة",
        "calendar" to "📅 التقويم",
        "stories" to "🌙 قصص الأنبياء",
        "quiz" to "🎯 الاختبار الديني",
        "tasbih" to "📿 السبحة",
        "games" to "🎮 الألعاب",
        "ai" to "🤖 المساعد الذكي",
        "zakat" to "💰 حاسبة الزكاة",
        "hajj" to "🕋 الحج والعمرة",
        "tracker" to "📖 ورد القرآن",
        "donation" to "🤲 التبرع",
        "notifications" to "🔔 التنبيهات",
        "login" to "👤 تسجيل الدخول",
        "privacy" to "🔒 سياسة الخصوصية",
        "settings" to "⚙️ الإعدادات"
    )
    Column(modifier = Modifier.fillMaxSize().background(Navy).padding(16.dp)) {
        Text("المزيد", color = Gold, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(entries.size) { i ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onNavigate(entries[i].first) },
                    colors = CardDefaults.cardColors(containerColor = NavyCard),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(entries[i].second, color = GoldSoft, fontSize = 17.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}
