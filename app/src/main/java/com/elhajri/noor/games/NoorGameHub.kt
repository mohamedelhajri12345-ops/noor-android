package com.elhajri.noor.games

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.BookOpen
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Hash
import androidx.compose.material.icons.filled.ListOrdered
import androidx.compose.material.icons.filled.MapPin
import androidx.compose.material.icons.filled.Sparkles
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.Navy
import com.elhajri.noor.ui.NavyCard
import com.elhajri.noor.ui.themeScreenBackground

private data class GameEntry(
    val route: String,
    val title: String,
    val desc: String,
    val icon: ImageVector,
    val isNew: Boolean
)

private val games = listOf(
    GameEntry("game_kalimat", "كلمة من الذكر", "لغز اليوم: تخمّن اسم الله الحسنى في ٦ محاولات", Icons.Default.WbTwilight, true),
    GameEntry("game_trivia", "ميدان المعرفة", "سلم ذهبي من ١٠ أسئلة بوسائل مساعدة إيمانية", Icons.Default.EmojiEvents, true),
    GameEntry("game_ayat", "سلسلة الآيات", "رتّب آيات السورة في مواضعها واستمع للتلاوة", Icons.Default.Timer, true),
    GameEntry("game_match", "رباط معاني الله", "طابق أسماء الله الحسنى بمعانيها — ذاكرة ومطابقة", Icons.Default.Flip, true),
    GameEntry("game_timeline", "خط الزمن النبوي", "رتّب أحداث السيرة والتاريخ الإسلامي زمنياً", Icons.Default.Psychology, true),
    GameEntry("game_tartil", "مرآة الترتيل", "أكمل الكلمة المغطاة وثبّت حفظك بالنجوم", Icons.Default.GridOn, true)
    GameEntry("game_prophets_order", "ترتيب الأنبياء", "رتّب الأنبياء حسب الترتيب الزمني عبر ٢٥ مستوى", Icons.Default.ListOrdered, true),
    GameEntry("game_true_false", "صح أم خطأ", "١٢٠+ سؤالاً إيمانياً مع نظام القلوب والمستويات", Icons.Default.Check, true),
    GameEntry("game_numbers_quiz", "اختبار الأرقام", "٥٠+ سؤالاً عن الأعداد في القرآن والسنة", Icons.Default.Hash, true),
    GameEntry("game_quran_mem", "حفظ القرآن", "تابع حفظ ١١٤ سورة واختبر معلوماتك عبر ١٥ مستوى", Icons.Default.BookOpen, true),
    GameEntry("game_names_allah", "الأسماء الحسنى", "دليل ٩٩ اسماً واختبارات الإتقان والربط", Icons.Default.Sparkles, true),
    GameEntry("game_prophets_journey", "رحلة الأنبياء", "١٥ محطة وقصة عن أنبياء الله ورسله", Icons.Default.MapPin, true)
)

/**
 * "ميدان نور الإيماني" — بوابة الألعاب الجديدة الست، بنفس هوية التطبيق.
 */
@Composable
fun NoorGameHubScreen(onBack: () -> Unit, onNavigate: (String) -> Unit) {
    Box(Modifier.fillMaxSize().themeScreenBackground()) {
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = Gold)
                }
                Column {
                    Text("ميدان نور الإيماني", color = Gold, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text("ألعاب الذكر والحكمة — ست محطات إيمانية جديدة", color = GoldSoft, fontSize = 11.sp)
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(games) { _, g ->
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .height(168.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(NavyCard.copy(alpha = 0.85f))
                            .border(1.dp, Gold.copy(alpha = 0.28f), RoundedCornerShape(18.dp))
                            .clickable { onNavigate(g.route) }
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(26.dp))
                                .background(Gold.copy(alpha = 0.14f))
                                .border(1.dp, Gold.copy(alpha = 0.45f), RoundedCornerShape(26.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(g.icon, contentDescription = null, tint = Gold, modifier = Modifier.size(26.dp))
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(g.title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(4.dp))
                        Text(g.desc, color = GoldSoft, fontSize = 10.sp, lineHeight = 14.sp, textAlign = TextAlign.Center)
                        if (g.isNew) {
                            Spacer(Modifier.height(6.dp))
                            Box(
                                Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Gold)
                                    .padding(horizontal = 10.dp, vertical = 2.dp)
                            ) {
                                Text("جديد", color = Navy, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
