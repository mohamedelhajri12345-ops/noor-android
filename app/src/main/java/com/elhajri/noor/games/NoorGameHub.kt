package com.elhajri.noor.games

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.OndemandVideo
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.elhajri.noor.ui.TextMain
import com.elhajri.noor.ui.TextSecondaryLinen
import com.elhajri.noor.theme.noorGlassCard

private data class GameEntry(
    val route: String,
    val title: String,
    val desc: String,
    val icon: ImageVector,
    val isNew: Boolean,
    /** Distinct accent per game, chosen from the app's green/gold identity palette. */
    val accent: Long,
    /** Category filter: knowledge, memory, order, letters, skill, content */
    val category: String = "knowledge"
)

private val games = listOf(
    GameEntry("videos", "مكتبة الفيديو للأطفال", "قصص الأنبياء، الأناشيد، وسير القرآن للأطفال", Icons.Default.OndemandVideo, true, 0xffd9a943, "content"),
    GameEntry("game_kalimat", "كلمة من الذكر", "لغز اليوم: تخمّن اسم الله الحسنى في ٦ محاولات", Icons.Default.WbTwilight, true, 0xff6fbf8b, "letters"),
    GameEntry("game_trivia", "ميدان المعرفة", "سلم ذهبي من ١٠ أسئلة بوسائل مساعدة إيمانية", Icons.Default.EmojiEvents, true, 0xffc5a028, "knowledge"),
    GameEntry("game_ayat", "سلسلة الآيات", "رتّب آيات السورة في مواضعها واستمع للتلاوة", Icons.Default.Timer, true, 0xff8ecfc4, "order"),
    GameEntry("game_match", "رباط معاني الله", "طابق أسماء الله الحسنى بمعانيها — ذاكرة ومطابقة", Icons.Default.Flip, true, 0xff9dbf6e, "memory"),
    GameEntry("game_timeline", "خط الزمن النبوي", "رتّب أحداث السيرة والتاريخ الإسلامي زمنياً", Icons.Default.Psychology, true, 0xffc98b5e, "order"),
    GameEntry("game_tartil", "مرآة الترتيل", "أكمل الكلمة المغطاة وثبّت حفظك بالنجوم", Icons.Default.GridOn, true, 0xff7fb2a6, "knowledge"),
    GameEntry("game_prophets_order", "ترتيب الأنبياء", "رتّب الأنبياء حسب الترتيب الزمني عبر ٢٥ مستوى", Icons.Default.FormatListNumbered, true, 0xffd4b85c, "order"),
    GameEntry("game_true_false", "صح أم خطأ", "١٢٠+ سؤالاً إيمانياً مع نظام القلوب والمستويات", Icons.Default.Check, true, 0xff86b79b, "knowledge"),
    GameEntry("game_numbers_quiz", "اختبار الأرقام", "٥٠+ سؤالاً عن الأعداد في القرآن والسنة", Icons.Default.Calculate, true, 0xffb39a55, "knowledge"),
    GameEntry("game_quran_mem", "حفظ القرآن", "تابع حفظ ١١٤ سورة واختبر معلوماتك عبر ١٥ مستوى", Icons.Default.MenuBook, true, 0xffa3c9a0, "knowledge"),
    GameEntry("game_names_allah", "الأسماء الحسنى", "دليل ٩٩ اسماً واختبارات الإتقان والربط", Icons.Default.AutoAwesome, true, 0xffc7a85b, "knowledge"),
    GameEntry("game_prophets_journey", "رحلة الأنبياء", "١٥ محطة وقصة عن أنبياء الله ورسله", Icons.Default.LocationOn, true, 0xffd0a93f, "knowledge"),
    // ===== Batch 2: interactive & knowledge games =====
    GameEntry("game_word_search", "صيد الكلمات", "ابحث عن كلمات الذكر داخل شبكة الحروف", Icons.Default.Search, true, 0xff8ecfc4, "skill"),
    GameEntry("game_tawaf", "ترتيب الطواف", "رتّب خطوات الطواف والعمرة بترتيبها الصحيح", Icons.Default.Mosque, true, 0xffd9a943, "order"),
    GameEntry("game_wudu", "تحدي الوضوء", "رتّب خطوات الوضوء قبل انتهاء الوقت", Icons.Default.WaterDrop, true, 0xff7fb2a6, "order"),
    GameEntry("game_prayer_order", "ترتيب الصلاة", "رتّب أركان الصلاة خطوة بخطوة", Icons.Default.Schedule, true, 0xffc5a028, "order"),
    GameEntry("game_duas_chain", "سلسلة الأدعية", "أكمل أدعية النبي ﷺ من بين الخيارات", Icons.Default.Star, true, 0xff9dbf6e, "knowledge"),
    GameEntry("game_deeds_balance", "ميزان الحسنات", "اختر العمل الأعظم أجراً بدليله", Icons.Default.Balance, true, 0xffc98b5e, "knowledge"),
    GameEntry("game_hijri_months", "سنة هجرية", "رتّب الأشهر الهجرية في مواضعها", Icons.Default.CalendarMonth, true, 0xff86b79b, "order"),
    GameEntry("game_juz_order", "ترتيب الأجزاء", "رتّب السور حسب موضعها في المصحف", Icons.Default.MenuBook, true, 0xffa3c9a0, "order"),
    GameEntry("game_lantern_memory", "فوانيس الذاكرة", "تذكر تتابع الفوانيس وكرّره", Icons.Default.DarkMode, true, 0xffd4b85c, "memory"),
    GameEntry("game_isra", "رحلة الإسراء والمعراج", "اجتز محطات الرحلة المباركة بترتيبها", Icons.Default.LocationOn, true, 0xffb39a55, "order"),
    GameEntry("game_wisdom_pearls", "لؤلؤة الحكمة", "طابق الحديث النبوي ببابه وموضوعه", Icons.Default.Psychology, true, 0xff7fb2a6, "knowledge"),
    GameEntry("game_zakat_calc", "عداد الزكاة", "احسب زكاة المال والذهب بنسبة صحيحة", Icons.Default.Money, true, 0xffc7a85b, "knowledge"),
    GameEntry("game_hijri_dates", "لغز التواريخ", "طابق المناسبة الإسلامية بشهرها الهجري", Icons.Default.Event, true, 0xff8ecfc4, "knowledge"),
    GameEntry("game_angels_unseen", "عالم الغيب", "أسئلة عن الملائكة والآخرة وعالم الغيب", Icons.Default.Visibility, true, 0xff9dbf6e, "knowledge"),
    GameEntry("game_fasting_rules", "فقه الصيام", "أحكام الصيام: صح أم خطأ بدليلها", Icons.Default.LocalFireDepartment, true, 0xffc98b5e, "knowledge"),
    GameEntry("game_kindness_tap", "قطاف الأخلاق", "اختر الخلق النبوي قبل انتهاء الوقت", Icons.Default.TouchApp, true, 0xffd0a93f, "skill"),
    // ===== Batch 3: engine-based games =====
    GameEntry("game_quiz_doctrine", "اختبار العقيدة", "٣٠ سؤالاً في العقيدة والتوحيد", Icons.Default.MenuBook, true, 0xff6fbf8b, "knowledge"),
    GameEntry("game_quiz_fiqh", "اختبار الفقه", "أسئلة في فقه العبادات والمعاملات", Icons.Default.MenuBook, true, 0xffc5a028, "knowledge"),
    GameEntry("game_quiz_sirah", "اختبار السيرة", "وقفات وعبر من سيرة الحبيب ﷺ", Icons.Default.Psychology, true, 0xff9dbf6e, "knowledge"),
    GameEntry("game_quiz_companions", "اختبار الصحابة", "٣٠ سؤالاً عن جيل الصحابة الكرام", Icons.Default.EmojiEvents, true, 0xff8ecfc4, "knowledge"),
    GameEntry("game_quiz_ramadan", "اختبار رمضان", "كل ما يخص شهر رمضان المبارك", Icons.Default.LocalFireDepartment, true, 0xffd9a943, "knowledge"),
    GameEntry("game_quiz_hajj", "اختبار الحج والعمرة", "مناسك الحج والعمرة خطوة بخطوة", Icons.Default.Mosque, true, 0xffb39a55, "knowledge"),
    GameEntry("game_match_names_meaning", "مطابقة الأسماء ومعانيها", "طابق الاسم بمعاه من ١٥ زوجاً", Icons.Default.Flip, true, 0xff7fb2a6, "memory"),
    GameEntry("game_match_surah_topics", "مطابقة السور ومقاصدها", "طابق السورة بمقصدها الأساسي", Icons.Default.MenuBook, true, 0xffa3c9a0, "memory"),
    GameEntry("game_match_worship", "مطابقة العبادات والأركان", "أركان الإسلام والعبادات وأزواجها", Icons.Default.Star, true, 0xffd4b85c, "memory"),
    GameEntry("game_match_athkar_times", "مطابقة الأذكار وأوقاتها", "ذكر الصباح والمساء وأوقاته", Icons.Default.WbTwilight, true, 0xff86b79b, "memory"),
    GameEntry("game_match_mosque_cities", "مطابقة المساجد ومدنها", "المساجد العريقة ومدنها حول العالم", Icons.Default.LocationOn, true, 0xffc98b5e, "memory"),
    GameEntry("game_match_islamic_months", "مطابقة الأشهر ومناسباتها", "طابق الشهر الهجري بمناسبته", Icons.Default.CalendarMonth, true, 0xff8ecfc4, "memory"),
    GameEntry("game_scramble_prophets", "حروف الأنبياء", "رتّب حروف أسماء الأنبياء عليهم السلام", Icons.Default.MenuBook, true, 0xffc7a85b, "letters"),
    GameEntry("game_scramble_surahs", "حروف السور", "رتّب حروف أسماء سور القرآن", Icons.Default.MenuBook, true, 0xff6fbf8b, "letters"),
    GameEntry("game_scramble_names_allah", "حروف الأسماء الحسنى", "رتّب حروف أسماء الله الحسنى", Icons.Default.AutoAwesome, true, 0xffd0a93f, "letters"),
    GameEntry("game_scramble_ramadan", "حروف رمضان", "كلمات رمضانية تنتظر ترتيبك", Icons.Default.LocalFireDepartment, true, 0xff9dbf6e, "letters"),
    GameEntry("game_quiz_prophets", "اختبار الأنبياء", "٣٠ سؤالاً عن قصص الأنبياء ورسائلهم", Icons.Default.MenuBook, true, 0xffd0a93f, "knowledge"),
    GameEntry("game_quiz_quran_stories", "اختبار قصص القرآن", "أسئلة عن قصص القرآن الكريم وعبرها", Icons.Default.MenuBook, true, 0xff7fb2a6, "knowledge"),
    GameEntry("game_quiz_manners", "اختبار الأخلاق والآداب", "آداب الإسلام وأخلاقيات اليوم", Icons.Default.EmojiEvents, true, 0xffc5a028, "knowledge"),
    GameEntry("game_quiz_prayer", "اختبار الصلاة", "٣٠ سؤالاً في أحكام الصلاة ووقتها", Icons.Default.Schedule, true, 0xff6fbf8b, "knowledge"),
    GameEntry("game_quiz_athkar", "اختبار الأذكار", "٣٠ سؤالاً في أذكار اليوم والليلة", Icons.Default.WbTwilight, true, 0xffd4b85c, "knowledge"),
    GameEntry("game_quiz_hadith", "اختبار الحديث النبوي", "أكمل الأحاديث النبوية الشهيرة", Icons.AutoMirrored.Filled.MenuBook, true, 0xffc98b5e, "knowledge")
)

/**
 * "ميدان نور الإيماني" — بوابة الألعاب والأنشطة، بنفس هوية التطبيق.
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
                    Text("ألعاب الذكر والحكمة والمحتوى التفاعلي للإسلام الصغير", color = GoldSoft, fontSize = 11.sp)
                }
            }

            val categories = listOf(
                "الكل" to "", "معرفة" to "knowledge", "ذاكرة ومطابقة" to "memory",
                "ترتيب" to "order", "حروف" to "letters", "مهارة" to "skill", "فيديو" to "content"
            )
            var selectedCategory by remember { mutableStateOf("") }
            val visibleGames = if (selectedCategory.isEmpty()) games else games.filter { it.category == selectedCategory }
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(categories) { _, (label, key) ->
                    val selected = selectedCategory == key
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (selected) Gold else Color.White.copy(alpha = 0.06f))
                            .border(1.dp, if (selected) Gold else Color.White.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
                            .clickable { selectedCategory = key }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Text(label, color = if (selected) Color(0xFF0B1120) else GoldSoft, fontSize = 12.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(visibleGames) { _, g ->
                    val accent = Color(g.accent)
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .height(168.dp)
                            .noorGlassCard(cornerRadius = 18.dp)
                            .border(1.dp, accent.copy(alpha = 0.32f), RoundedCornerShape(18.dp))
                            .clickable { onNavigate(g.route) }
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(26.dp))
                                .background(accent.copy(alpha = 0.16f))
                                .border(1.dp, accent.copy(alpha = 0.45f), RoundedCornerShape(26.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(g.icon, contentDescription = null, tint = accent, modifier = Modifier.size(26.dp))
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(g.title, color = TextMain, fontSize = 15.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(4.dp))
                        Text(g.desc, color = TextSecondaryLinen, fontSize = 10.sp, lineHeight = 14.sp, textAlign = TextAlign.Center)
                        if (g.isNew) {
                            Spacer(Modifier.height(6.dp))
                            Box(
                                Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(accent)
                                    .padding(horizontal = 10.dp, vertical = 2.dp)
                            ) {
                                Text("جديد", color = Color(0xFF0B1120), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
