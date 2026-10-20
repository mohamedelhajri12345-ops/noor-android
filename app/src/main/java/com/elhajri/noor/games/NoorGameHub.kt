package com.elhajri.noor.games

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.theme.noorGlassCard
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.Navy
import com.elhajri.noor.ui.NavyCard
import com.elhajri.noor.ui.TextMain
import com.elhajri.noor.ui.TextSecondaryLinen
import com.elhajri.noor.ui.themeScreenBackground

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
    GameEntry("game_match_names_meaning", "مطابقة الأسماء ومعانيها", "طابق الاسم بمعناه من ١٥ زوجاً", Icons.Default.Flip, true, 0xff7fb2a6, "memory"),
    GameEntry("game_match_surah_topics", "مطابقة السور ومقاصدها", "طابق السورة بمقصدها الأساسي", Icons.Default.MenuBook, true, 0xffa3c9a0, "memory"),
    GameEntry("game_match_worship", "مطابقة العبادات والأركان", "أركان الإسلام والعبادات وأزواجها", Icons.Default.Star, true, 0xffd4b85c, "memory"),
    GameEntry("game_match_athkar_times", "مطابقة الأذكار وأوقاتها", "ذكر الصباح والمساء وأوقاته", Icons.Default.WbTwilight, true, 0xff86b79b, "memory"),
    GameEntry("game_match_mosque_cities", "مطابقة المساجد ومدنها", "المساجد العريقة ومدنها حول العالم", Icons.Default.LocationOn, true, 0xffc98b5e, "memory"),
    GameEntry("game_match_islamic_months", "مطابقة الأشهر ومناسباتها", "طابق الشهر الهجري بمناسبته", Icons.Default.CalendarMonth, true, 0xff8ecfc4, "memory"),
    GameEntry("game_scramble_prophets", "حروف الأنبياء", "رتّب حروف أسماء الأنبياء عليهم السلام", Icons.Default.MenuBook, true, 0xffc7a85b, "letters"),
    GameEntry("game_scramble_surahs", "حروف السور", "رتّب حروف أسماء سور القرآن", Icons.Default.MenuBook, true, 0xff6fbf8b, "letters"),
    GameEntry("game_scramble_names_allah", "حروف الأسماء الحسنى", "رتّب حروف أسماء الله الحسنى", Icons.Default.AutoAwesome, true, 0xffd4b85c, "letters"),
    GameEntry("game_scramble_ramadan", "حروف رمضان", "رتّب مصطلحات وأحكام شهر الصيام", Icons.Default.LocalFireDepartment, true, 0xffc98b5e, "letters"),
    GameEntry("game_quiz_athkar", "اختبار الأذكار", "أذكار الصباح والمساء والاستيقاظ", Icons.Default.WbTwilight, true, 0xff86b79b, "knowledge"),
    GameEntry("game_quiz_manners", "اختبار الآداب", "آداب الطعام والشراب والسلام والاستئذان", Icons.Default.TouchApp, true, 0xffd0a93f, "knowledge"),
    GameEntry("game_quiz_mosques", "اختبار المساجد", "تاريخ ومعالم المساجد المزارة عبر التاريخ", Icons.Default.Mosque, true, 0xffb39a55, "knowledge"),
    GameEntry("game_quiz_prophets", "اختبار قصص الأنبياء", "معجزات ومواقف أنبياء الله ورسله", Icons.Default.LocationOn, true, 0xffd0a93f, "knowledge"),
    GameEntry("game_quiz_quran_stories", "اختبار قصص القرآن", "أصحاب الكهف، ذو القرنين، وقصص العبرة", Icons.Default.MenuBook, true, 0xffa3c9a0, "knowledge")
)

@Composable
fun NoorGameHubScreen(
    onBack: () -> Unit = {},
    onNavigate: (String) -> Unit = {}
) {
    NoorGameHub(onNavigate = onNavigate, onBack = onBack)
}

@Composable
fun NoorGameHub(
    onNavigate: (String) -> Unit,
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    var showStarShop by remember { mutableStateOf(false) }
    val starBalance = remember(showStarShop) { StarEconomyStore.getStarBalance(context) }
    val hintCount = remember(showStarShop) { StarEconomyStore.getHintCount(context) }

    val categories = listOf(
        "الكل" to "", "معرفة" to "knowledge", "ذاكرة ومطابقة" to "memory",
        "ترتيب" to "order", "حروف" to "letters", "مهارة" to "skill", "فيديو" to "content"
    )
    var selectedCategory by remember { mutableStateOf("") }
    val visibleGames = if (selectedCategory.isEmpty()) games else games.filter { it.category == selectedCategory }

    if (showStarShop) {
        StarShopDialog(onDismiss = { showStarShop = false })
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .themeScreenBackground()
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Header Section
            item(span = { GridItemSpan(2) }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    // Top Bar with Currency Pill & Back
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = Gold)
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("ميدان نور الإيماني", color = Gold, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Text("ألعاب الذكر والحكمة والتحدي التفاعلي", color = GoldSoft, fontSize = 11.sp)
                        }

                        // Currency Pill (Stars & Hints)
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = NavyCard,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Gold),
                            modifier = Modifier.clickable { showStarShop = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = Gold, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(starBalance.toArabicDigits(), color = Gold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.width(8.dp))
                                Text("•", color = GoldSoft)
                                Spacer(Modifier.width(8.dp))
                                Icon(Icons.Default.Lightbulb, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(15.dp))
                                Spacer(Modifier.width(2.dp))
                                Text(hintCount.toArabicDigits(), color = TextMain, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    // XP Rank Progress Bar Card
                    XpRankProgressBarCard()

                    Spacer(Modifier.height(12.dp))

                    // Daily Challenge Rotating Game Card
                    DailyChallengeHubCard(onPlayChallenge = { route -> onNavigate(route) })

                    Spacer(Modifier.height(12.dp))

                    // Shop & Frames Quick Access Banner
                    Button(
                        onClick = { showStarShop = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Gold),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = Navy, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("متجر النجوم وتفعيل إطارات البطاقات 👑", color = Navy, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(Modifier.height(12.dp))

                    // Daily Challenge Notification Settings Toggle Card
                    DailyChallengeNotificationSettingsCard()

                    Spacer(Modifier.height(16.dp))

                    // Categories Strip
                    LazyRow(
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
                                Text(
                                    label,
                                    color = if (selected) Color(0xFF0B1120) else GoldSoft,
                                    fontSize = 12.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }

            // Games Grid Items with Equipped Card Frames
            itemsIndexed(visibleGames) { _, g ->
                val accent = Color(g.accent)
                Column(
                    Modifier
                        .fillMaxWidth()
                        .height(168.dp)
                        .padding(horizontal = 8.dp)
                        .noorEquippedCardFrame(context, cornerRadius = 18.dp)
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
