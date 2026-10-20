package com.elhajri.noor.games

import com.elhajri.noor.settings.t
import com.elhajri.noor.settings.toArabicDigits

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
    val titleAr: String,
    val titleEn: String,
    val descAr: String,
    val descEn: String,
    val icon: ImageVector,
    val isNew: Boolean,
    /** Distinct accent per game, chosen from the app's green/gold identity palette. */
    val accent: Long,
    /** Category filter: knowledge, memory, order, letters, skill, content */
    val category: String = "knowledge"
) {
    val title: String get() = t(titleAr, titleEn)
    val desc: String get() = t(descAr, descEn)
}

private val games = listOf(
    GameEntry("videos", "مكتبة الفيديو للأطفال", "Kids Video Library", "قصص الأنبياء، الأناشيد، وسير القرآن للأطفال", "Prophets' stories, nasheeds, and Quranic stories for kids", Icons.Default.OndemandVideo, true, 0xffd9a943, "content"),
    GameEntry("game_kalimat", "كلمة من الذكر", "Dhikr Word", "لغز اليوم: تخمّن اسم الله الحسنى في ٦ محاولات", "Daily puzzle: Guess Allah's Name in 6 attempts", Icons.Default.WbTwilight, true, 0xff6fbf8b, "letters"),
    GameEntry("game_trivia", "ميدان المعرفة", "Knowledge Arena", "سلم ذهبي من ١٠ أسئلة بوسائل مساعدة إيمانية", "Golden ladder of 10 questions with lifelines", Icons.Default.EmojiEvents, true, 0xffc5a028, "knowledge"),
    GameEntry("game_ayat", "سلسلة الآيات", "Ayah Chain", "رتّب آيات السورة في مواضعها واستمع للتلاوة", "Arrange surah verses in order and listen to recitation", Icons.Default.Timer, true, 0xff8ecfc4, "order"),
    GameEntry("game_match", "رباط معاني الله", "Allah's Names & Meanings", "طابق أسماء الله الحسنى بمعانيها — ذاكرة ومطابقة", "Match Allah's Names with their meanings", Icons.Default.Flip, true, 0xff9dbf6e, "memory"),
    GameEntry("game_timeline", "خط الزمن النبوي", "Prophetic Timeline", "رتّب أحداث السيرة والتاريخ الإسلامي زمنياً", "Order Seerah & Islamic history events chronologically", Icons.Default.Psychology, true, 0xffc98b5e, "order"),
    GameEntry("game_tartil", "مرآة الترتيل", "Tartil Mirror", "أكمل الكلمة المغطاة وثبّت حفظك بالنجوم", "Fill in covered words and consolidate memorization", Icons.Default.GridOn, true, 0xff7fb2a6, "knowledge"),
    GameEntry("game_prophets_order", "ترتيب الأنبياء", "Prophets Order", "رتّب الأنبياء حسب الترتيب الزمني عبر ٢٥ مستوى", "Order prophets chronologically across 25 levels", Icons.Default.FormatListNumbered, true, 0xffd4b85c, "order"),
    GameEntry("game_true_false", "صح أم خطأ", "True or False", "١٢٠+ سؤالاً إيمانياً مع نظام القلوب والمستويات", "120+ faith questions with hearts & level system", Icons.Default.Check, true, 0xff86b79b, "knowledge"),
    GameEntry("game_numbers_quiz", "اختبار الأرقام", "Numbers Quiz", "٥٠+ سؤالاً عن الأعداد في القرآن والسنة", "50+ questions on numbers in Quran and Sunnah", Icons.Default.Calculate, true, 0xffb39a55, "knowledge"),
    GameEntry("game_quran_mem", "حفظ القرآن", "Quran Memorization", "تابع حفظ ١١٤ سورة واختبر معلوماتك عبر ١٥ مستوى", "Track 114 surahs and test knowledge across 15 levels", Icons.Default.MenuBook, true, 0xffa3c9a0, "knowledge"),
    GameEntry("game_names_allah", "الأسماء الحسنى", "Names of Allah", "دليل ٩٩ اسماً واختبارات الإتقان والربط", "Guide to 99 Names with mastery tests", Icons.Default.AutoAwesome, true, 0xffc7a85b, "knowledge"),
    GameEntry("game_prophets_journey", "رحلة الأنبياء", "Prophets' Journey", "١٥ محطة وقصة عن أنبياء الله ورسله", "15 stations and stories of Allah's Prophets", Icons.Default.LocationOn, true, 0xffd0a93f, "knowledge"),
    // ===== Batch 2: interactive & knowledge games =====
    GameEntry("game_word_search", "صيد الكلمات", "Word Search", "ابحث عن كلمات الذكر داخل شبكة الحروف", "Find Dhikr words inside the letter grid", Icons.Default.Search, true, 0xff8ecfc4, "skill"),
    GameEntry("game_tawaf", "ترتيب الطواف", "Tawaf Order", "رتّب خطوات الطواف والعمرة بترتيبها الصحيح", "Arrange Tawaf and Umrah steps in order", Icons.Default.Mosque, true, 0xffd9a943, "order"),
    GameEntry("game_wudu", "تحدي الوضوء", "Wudu Challenge", "رتّب خطوات الوضوء قبل انتهاء الوقت", "Order Wudu steps before time runs out", Icons.Default.WaterDrop, true, 0xff7fb2a6, "order"),
    GameEntry("game_prayer_order", "ترتيب الصلاة", "Prayer Order", "رتّب أركان الصلاة خطوة بخطوة", "Arrange prayer pillars step by step", Icons.Default.Schedule, true, 0xffc5a028, "order"),
    GameEntry("game_duas_chain", "سلسلة الأدعية", "Dua Chain", "أكمل أدعية النبي ﷺ من بين الخيارات", "Complete Prophet's duas from options", Icons.Default.Star, true, 0xff9dbf6e, "knowledge"),
    GameEntry("game_deeds_balance", "ميزان الحسنات", "Deeds Balance", "اختر العمل الأعظم أجراً بدليله", "Choose the greater reward deed with evidence", Icons.Default.Balance, true, 0xffc98b5e, "knowledge"),
    GameEntry("game_hijri_months", "سنة هجرية", "Hijri Year", "رتّب الأشهر الهجرية في مواضعها", "Order Hijri months in their positions", Icons.Default.CalendarMonth, true, 0xff86b79b, "order"),
    GameEntry("game_juz_order", "ترتيب الأجزاء", "Juz Order", "رتّب السور حسب موضعها في المصحف", "Arrange surahs by position in Quran", Icons.Default.MenuBook, true, 0xffa3c9a0, "order"),
    GameEntry("game_lantern_memory", "فوانيس الذاكرة", "Lantern Memory", "تذكر تتابع الفوانيس وكرّره", "Remember the lantern sequence and repeat it", Icons.Default.DarkMode, true, 0xffd4b85c, "memory"),
    GameEntry("game_isra", "رحلة الإسراء والمعراج", "Isra & Mi'raj Journey", "اجتز محطات الرحلة المباركة بترتيبها", "Pass stations of the blessed journey in order", Icons.Default.LocationOn, true, 0xffb39a55, "order"),
    GameEntry("game_wisdom_pearls", "لؤلؤة الحكمة", "Wisdom Pearls", "طابق الحديث النبوي ببابه وموضوعه", "Match Hadith with its chapter and topic", Icons.Default.Psychology, true, 0xff7fb2a6, "knowledge"),
    GameEntry("game_zakat_calc", "عداد الزكاة", "Zakat Calculator", "احسب زكاة المال والذهب بنسبة صحيحة", "Calculate Zakat on wealth and gold accurately", Icons.Default.Money, true, 0xffc7a85b, "knowledge"),
    GameEntry("game_hijri_dates", "لغز التواريخ", "Islamic Dates Puzzle", "طابق المناسبة الإسلامية بشهرها الهجري", "Match Islamic occasion with Hijri month", Icons.Default.Event, true, 0xff8ecfc4, "knowledge"),
    GameEntry("game_angels_unseen", "عالم الغيب", "Realm of the Unseen", "أسئلة عن الملائكة والآخرة وعالم الغيب", "Questions on angels, hereafter & the unseen", Icons.Default.Visibility, true, 0xff9dbf6e, "knowledge"),
    GameEntry("game_fasting_rules", "فقه الصيام", "Fasting Rules", "أحكام الصيام: صح أم خطأ بدليلها", "Fasting rules: True/False with evidence", Icons.Default.LocalFireDepartment, true, 0xffc98b5e, "knowledge"),
    GameEntry("game_kindness_tap", "قطاف الأخلاق", "Prophetic Manners", "اختر الخلق النبوي قبل انتهاء الوقت", "Select Prophetic manners before time ends", Icons.Default.TouchApp, true, 0xffd0a93f, "skill"),
    // ===== Batch 3: engine-based games =====
    GameEntry("game_quiz_doctrine", "اختبار العقيدة", "Aqeedah Quiz", "٣٠ سؤالاً في العقيدة والتوحيد", "30 questions on Aqeedah and Tawheed", Icons.Default.MenuBook, true, 0xff6fbf8b, "knowledge"),
    GameEntry("game_quiz_fiqh", "اختبار الفقه", "Fiqh Quiz", "أسئلة في فقه العبادات والمعاملات", "Questions on Fiqh of worship and transactions", Icons.Default.MenuBook, true, 0xffc5a028, "knowledge"),
    GameEntry("game_quiz_sirah", "اختبار السيرة", "Seerah Quiz", "وقفات وعبر من سيرة الحبيب ﷺ", "Lessons from the life of Prophet Muhammad ﷺ", Icons.Default.Psychology, true, 0xff9dbf6e, "knowledge"),
    GameEntry("game_quiz_companions", "اختبار الصحابة", "Companions Quiz", "٣٠ سؤالاً عن جيل الصحابة الكرام", "30 questions on the noble Companions", Icons.Default.EmojiEvents, true, 0xff8ecfc4, "knowledge"),
    GameEntry("game_quiz_ramadan", "اختبار رمضان", "Ramadan Quiz", "كل ما يخص شهر رمضان المبارك", "Everything about the blessed month of Ramadan", Icons.Default.LocalFireDepartment, true, 0xffd9a943, "knowledge"),
    GameEntry("game_quiz_hajj", "اختبار الحج والعمرة", "Hajj & Umrah Quiz", "مناسك الحج والعمرة خطوة بخطوة", "Rites of Hajj and Umrah step by step", Icons.Default.Mosque, true, 0xffb39a55, "knowledge"),
    GameEntry("game_match_names_meaning", "مطابقة الأسماء ومعانيها", "Names & Meanings Match", "طابق الاسم بمعناه من ١٥ زوجاً", "Match Allah's Name with meaning across 15 pairs", Icons.Default.Flip, true, 0xff7fb2a6, "memory"),
    GameEntry("game_match_surah_topics", "مطابقة السور ومقاصدها", "Surah Topics Match", "طابق السورة بمقصدها الأساسي", "Match surah with its main theme", Icons.Default.MenuBook, true, 0xffa3c9a0, "memory"),
    GameEntry("game_match_worship", "مطابقة العبادات والأركان", "Worship Pillars Match", "أركان الإسلام والعبادات وأزواجها", "Pillars of Islam and worship pairs", Icons.Default.Star, true, 0xffd4b85c, "memory"),
    GameEntry("game_match_athkar_times", "مطابقة الأذكار وأوقاتها", "Adhkar Times Match", "ذكر الصباح والمساء وأوقاته", "Morning & Evening Adhkar and times", Icons.Default.WbTwilight, true, 0xff86b79b, "memory"),
    GameEntry("game_match_mosque_cities", "مطابقة المساجد ومدنها", "Mosques & Cities Match", "المساجد العريقة ومدنها حول العالم", "Historic mosques and their cities worldwide", Icons.Default.LocationOn, true, 0xffc98b5e, "memory"),
    GameEntry("game_match_islamic_months", "مطابقة الأشهر ومناسباتها", "Islamic Months Match", "طابق الشهر الهجري بمناسبته", "Match Hijri month with its occasion", Icons.Default.CalendarMonth, true, 0xff8ecfc4, "memory"),
    GameEntry("game_scramble_prophets", "حروف الأنبياء", "Prophets Scramble", "رتّب حروف أسماء الأنبياء عليهم السلام", "Unscramble names of the Prophets", Icons.Default.MenuBook, true, 0xffc7a85b, "letters"),
    GameEntry("game_scramble_surahs", "حروف السور", "Surah Names Scramble", "رتّب حروف أسماء سور القرآن", "Unscramble names of Quran Surahs", Icons.Default.MenuBook, true, 0xff6fbf8b, "letters"),
    GameEntry("game_scramble_names_allah", "حروف الأسماء الحسنى", "Allah's Names Scramble", "رتّب حروف أسماء الله الحسنى", "Unscramble Allah's Beautiful Names", Icons.Default.AutoAwesome, true, 0xffd4b85c, "letters"),
    GameEntry("game_scramble_ramadan", "حروف رمضان", "Ramadan Terms Scramble", "رتّب مصطلحات وأحكام شهر الصيام", "Unscramble Ramadan terms & rules", Icons.Default.LocalFireDepartment, true, 0xffc98b5e, "letters"),
    GameEntry("game_quiz_athkar", "اختبار الأذكار", "Adhkar Quiz", "أذكار الصباح والمساء والاستيقاظ", "Morning, evening and waking Adhkar", Icons.Default.WbTwilight, true, 0xff86b79b, "knowledge"),
    GameEntry("game_quiz_manners", "اختبار الآداب", "Islamic Manners Quiz", "آداب الطعام والشراب والسلام والاستئذان", "Manners of eating, greeting and seeking permission", Icons.Default.TouchApp, true, 0xffd0a93f, "knowledge"),
    GameEntry("game_quiz_mosques", "اختبار المساجد", "Mosques Quiz", "تاريخ ومعالم المساجد المزارة عبر التاريخ", "History and landmarks of sacred mosques", Icons.Default.Mosque, true, 0xffb39a55, "knowledge"),
    GameEntry("game_quiz_prophets", "اختبار قصص الأنبياء", "Prophets Stories Quiz", "معجزات ومواقف أنبياء الله ورسله", "Miracles and stories of Allah's Prophets", Icons.Default.LocationOn, true, 0xffd0a93f, "knowledge"),
    GameEntry("game_quiz_quran_stories", "اختبار قصص القرآن", "Quran Stories Quiz", "أصحاب الكهف، ذو القرنين، وقصص العبرة", "People of the Cave, Dhul-Qarnayn & lessons", Icons.Default.MenuBook, true, 0xffa3c9a0, "knowledge")
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
        t("الكل", "All") to "",
        t("معرفة", "Knowledge") to "knowledge",
        t("ذاكرة ومطابقة", "Memory") to "memory",
        t("ترتيب", "Ordering") to "order",
        t("حروف", "Letters") to "letters",
        t("مهارة", "Skill") to "skill",
        t("فيديو", "Video") to "content"
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
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = t("رجوع", "Back"), tint = Gold)
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(t("ميدان نور الإيماني", "NOOR Faith Games"), color = Gold, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Text(t("ألعاب الذكر والحكمة والتحدي التفاعلي", "Interactive Dhikr, Wisdom & Challenge Games"), color = GoldSoft, fontSize = 11.sp)
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
                        Text(t("متجر النجوم وتفعيل إطارات البطاقات 👑", "Star Shop & Frame Customization 👑"), color = Navy, fontSize = 14.sp, fontWeight = FontWeight.Bold)
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
                            Text(t("جديد", "NEW"), color = Color(0xFF0B1120), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
