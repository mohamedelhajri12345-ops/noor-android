package com.elhajri.noor.analytics

import com.elhajri.noor.settings.t
import com.elhajri.noor.settings.toArabicDigits

import android.content.Context
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.data.DataLoader
import com.elhajri.noor.ui.AmiriFamily
import com.elhajri.noor.ui.EmeraldSuccess
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.Navy
import com.elhajri.noor.ui.NavyCard
import com.elhajri.noor.ui.NavyLight
import com.elhajri.noor.ui.NoorGradients
import com.elhajri.noor.ui.TextMain
import com.elhajri.noor.ui.noorGlassCard
import com.elhajri.noor.ui.themeScreenBackground
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

private const val QURAN_TRACKER_PREFS = "nur_quran_tracker_prefs"
private const val KEY_QURAN_TRACKER = "tracker_json"

private fun toArabicDigits(value: Any): String {
    val map = mapOf('0' to '٠', '1' to '١', '2' to '٢', '3' to '٣', '4' to '٤', '5' to '٥', '6' to '٦', '7' to '٧', '8' to '٨', '9' to '٩')
    return value.toString().map { map[it] ?: it }.joinToString("")
}

@Composable
fun AnalyticsDashboardScreen(onBack: (() -> Unit)? = null) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: نشاطي (Main), 1: تقدمي (Stars/XP), 2: الحفظ والإنجاز, 3: سلاسل ومقارنات, 4: ورد القرآن

    val summary = remember(selectedTab) { AnalyticsTracker.getSummary(context) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .themeScreenBackground()
            .padding(16.dp)
    ) {
        // Top Header Title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = t("رجوع", "Back"),
                        tint = Gold
                    )
                }
                Spacer(Modifier.width(4.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = t("بياناتي وإحصائياتي", "My Data & Stats"),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = AmiriFamily,
                    color = Gold
                )
                Text(
                    text = t("لوحة تحكم النشاط والإنجازات الشاملة", "Comprehensive activity & achievement dashboard"),
                    fontSize = 12.sp,
                    color = GoldSoft.copy(alpha = 0.7f)
                )
            }
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = NavyCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, Gold.copy(alpha = 0.35f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.LocalFireDepartment, contentDescription = null, tint = Gold, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "${toArabicDigits(summary.activeStreakDays)} " + t("أيام", "days"),
                        color = Gold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Horizontal Category Tabs
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                listOf(
                    0 to t("نشاطي", "My Activity"),
                    1 to t("تقدمي (XP)", "My Progress (XP)"),
                    2 to t("الحفظ والإنجاز", "Memorization & Achievement"),
                    3 to t("سلاسل ومقارنات", "Streaks & Comparisons"),
                    4 to t("ورد القرآن", "Qur'an Wird")
                )
            ) { (index, title) ->
                val isSelected = selectedTab == index
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSelected) Gold.copy(alpha = 0.2f) else NavyCard)
                        .border(
                            width = 1.dp,
                            color = if (isSelected) Gold else Gold.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable { selectedTab = index }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        fontSize = 12.5.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Gold else TextMain.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // Tab Content
        when (selectedTab) {
            0 -> UsageSection(summary)
            1 -> ProgressSection(summary)
            2 -> AchievementsSection(summary)
            3 -> StreaksAndComparisonsSection(summary)
            4 -> QuranWirdSection()
        }
    }
}

// ────────────── 1. قسم نشاطي (Usage Section) ──────────────
@Composable
private fun UsageSection(summary: AnalyticsTracker.AnalyticsSummary) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Quick Stat Grid Cards (4 Cards)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = t("دقائق الاستخدام", "Usage Minutes"),
                    value = "${toArabicDigits(summary.todayUsageMinutes)} " + t("دقيقة", "min"),
                    sub = t("إجمالي: ", "Total: ") + "${toArabicDigits(summary.totalUsageMinutes)} " + t("د", "m"),
                    icon = Icons.Filled.Timer,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = t("الجلسات", "Sessions"),
                    value = toArabicDigits(summary.totalSessions),
                    sub = t("اليوم: ", "Today: ") + "${toArabicDigits(maxOf(1, summary.todayUsageMinutes / 10))}",
                    icon = Icons.Filled.TouchApp,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = t("مجموع النجوم", "Total Stars"),
                    value = "${toArabicDigits(summary.totalStars)} 🌟",
                    sub = t("خبرة: ", "XP: ") + "${toArabicDigits(summary.totalXp)} XP",
                    icon = Icons.Filled.Star,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = t("السلسلة النشطة", "Active Streak"),
                    value = "${toArabicDigits(summary.activeStreakDays)} " + t("أيام 🔥", "days 🔥"),
                    sub = t("الأفضل: ", "Best: ") + "${toArabicDigits(summary.bestStreakDays)} " + t("أيام", "days"),
                    icon = Icons.Filled.LocalFireDepartment,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Line Chart: Daily Usage Minutes (Past 7 days)
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                modifier = Modifier
                    .fillMaxWidth()
                    .noorGlassCard()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(t("منحنى النشاط اليومي (دقائق)", "Daily Activity Curve (minutes)"), color = Gold, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Icon(Icons.Filled.ShowChart, contentDescription = null, tint = Gold, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.height(12.dp))
                    DailyMinutesLineChart(summary.weeklyDailyMinutes)
                }
            }
        }

        // Bar Chart: Per-Feature Opens
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                modifier = Modifier
                    .fillMaxWidth()
                    .noorGlassCard()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(t("الاستخدام حسب أقسام التطبيق", "Usage by App Section"), color = Gold, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Icon(Icons.Filled.BarChart, contentDescription = null, tint = Gold, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.height(12.dp))
                    FeatureOpensBarChart(summary.featureOpens)
                }
            }
        }
    }
}

// ────────────── 2. قسم تقدمي (Stars / XP Curves & Game Progress) ──────────────
@Composable
private fun ProgressSection(summary: AnalyticsTracker.AnalyticsSummary) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Curve Chart: Stars & XP progression over 4 weeks
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                modifier = Modifier
                    .fillMaxWidth()
                    .noorGlassCard()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(t("نمو النجوم ونقاط الخبرة عبر الأسابيع", "Stars & XP growth over weeks"), color = Gold, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Icon(Icons.Filled.AutoGraph, contentDescription = null, tint = Gold, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.height(12.dp))
                    WeeklyProgressCurveChart(summary.weeklyProgressMinutes)
                }
            }
        }

        // Game Progress Cards List
        item {
            Text(t("تقدم الألعاب والتحديات الإيمانية", "Faith Games & Challenges Progress"), color = Gold, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }

        items(summary.gameProgressMap.toList()) { (gameName, progress) ->
            val (levels, stars) = progress
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                modifier = Modifier
                    .fillMaxWidth()
                    .noorGlassCard()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Gold.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.SportsEsports, contentDescription = null, tint = Gold, modifier = Modifier.size(22.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(gameName, color = TextMain, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("المراحل المكتملة: ${toArabicDigits(levels)}", color = GoldSoft.copy(alpha = 0.7f), fontSize = 11.sp)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = NavyCard,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Gold.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Star, contentDescription = null, tint = Gold, modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("${toArabicDigits(stars)} نجوم", color = Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ────────────── 3. قسم الحفظ والإنجاز (Completion Ratios) ──────────────
@Composable
private fun AchievementsSection(summary: AnalyticsTracker.AnalyticsSummary) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Donut Chart Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                modifier = Modifier
                    .fillMaxWidth()
                    .noorGlassCard()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(t("مؤشر نسب الإنجاز الشامل في التطبيق", "Overall completion index across the app"), color = Gold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(16.dp))

                    CompletionDonutChart(summary)

                    Spacer(Modifier.height(16.dp))

                    // Donut Legend Breakdown
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LegendRow(color = Gold, label = t("سور القرآن المكتملة", "Completed Qur'an Surahs"), value = if (com.elhajri.noor.settings.NoorLocale.isEnglish) "${summary.quranCompletedSurahsCount} / 114 surahs" else "${toArabicDigits(summary.quranCompletedSurahsCount)} / ١١٤ سورة")
                        LegendRow(color = EmeraldSuccess, label = t("مراحل الألعاب المنجزة", "Completed Game Levels"), value = if (com.elhajri.noor.settings.NoorLocale.isEnglish) "${summary.totalCompletedLevels} levels" else "${toArabicDigits(summary.totalCompletedLevels)} مرحلة")
                        LegendRow(color = Color(0xFF4FC3F7), label = t("التسبيحات المكتملة", "Completed Tasbihs"), value = if (com.elhajri.noor.settings.NoorLocale.isEnglish) "${summary.tasbihTotalCount} tasbihs" else "${toArabicDigits(summary.tasbihTotalCount)} تسبيحة")
                        LegendRow(color = Color(0xFFBA68C8), label = t("الأناشيد المستمع لها", "Nasheeds Listened"), value = if (com.elhajri.noor.settings.NoorLocale.isEnglish) "${summary.nasheedTotalPlays} tracks" else "${toArabicDigits(summary.nasheedTotalPlays)} نشيد")
                    }
                }
            }
        }

        // Completed vs Remaining Breakdown Cards
        item {
            Text(t("تفاصيل ما تم إنجازه والمتبقي", "Details of completed vs remaining"), color = Gold, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }

        item {
            BreakdownProgressCard(
                title = t("ختم القرآن الكريم", "Qur'an Completion"),
                completed = summary.quranCompletedSurahsCount,
                total = 114,
                unit = t("سورة", "Surah"),
                icon = Icons.Filled.MenuBook
            )
        }

        item {
            BreakdownProgressCard(
                title = t("مراحل ميدان الألعاب", "Game Arena Levels"),
                completed = summary.totalCompletedLevels,
                total = 100,
                unit = t("مرحلة", "Level"),
                icon = Icons.Filled.Casino
            )
        }

        item {
            BreakdownProgressCard(
                title = t("هدف ورد القرآن اليومي", "Daily Qur'an Portion Goal"),
                completed = summary.quranPagesToday,
                total = summary.quranGoalPages,
                unit = t("صفحة اليوم", "Today's Page"),
                icon = Icons.Filled.Bookmark
            )
        }
    }
}

// ────────────── 4. قسم سلاسل ومقارنات (Streaks & Week Comparison) ──────────────
@Composable
private fun StreaksAndComparisonsSection(summary: AnalyticsTracker.AnalyticsSummary) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Streaks & Highlights Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                modifier = Modifier
                    .fillMaxWidth()
                    .noorGlassCard()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(t("سلسلة المواظبة وأعلى الأيام", "Consistency streak and peak days"), color = Gold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Icon(Icons.Filled.EmojiEvents, contentDescription = null, tint = Gold, modifier = Modifier.size(22.dp))
                    }

                    Spacer(Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(NavyCard)
                                .border(1.dp, Gold.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(t("السلسلة الحالية", "Current Streak"), fontSize = 11.sp, color = GoldSoft.copy(alpha = 0.7f))
                                Spacer(Modifier.height(4.dp))
                                Text("${toArabicDigits(summary.activeStreakDays)} " + t("أيام", "days"), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Gold)
                                Text("🔥 مواظبة مستمرة", fontSize = 10.sp, color = TextMain.copy(alpha = 0.6f))
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(NavyCard)
                                .border(1.dp, Gold.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(t("أفضل سلسلة", "Best Streak"), fontSize = 11.sp, color = GoldSoft.copy(alpha = 0.7f))
                                Spacer(Modifier.height(4.dp))
                                Text("${toArabicDigits(summary.bestStreakDays)} أيام", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Gold)
                                Text("🏆 الرقم القياسي", fontSize = 10.sp, color = TextMain.copy(alpha = 0.6f))
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(NavyCard)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Star, contentDescription = null, tint = Gold, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(t("أعلى يوم استخدماً هذا الأسبوع", "Most active day this week"), color = TextMain, fontSize = 12.5.sp)
                        }
                        Text("${summary.peakUsageDayLabel} (${toArabicDigits(summary.peakUsageMinutes)} دقيقة)", color = Gold, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Comparison: This Week vs Last Week
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                modifier = Modifier
                    .fillMaxWidth()
                    .noorGlassCard()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(t("مقارنة هذا الأسبوع بالأسبوع الماضي", "Comparison: This week vs Last week"), color = Gold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(14.dp))

                    ComparisonRow(
                        label = t("دقائق الاستخدام", "Usage Minutes"),
                        thisWeekVal = "${toArabicDigits(summary.thisWeekMinutes)} " + t("دقيقة", "min"),
                        lastWeekVal = "${toArabicDigits(summary.lastWeekMinutes)} " + t("دقيقة", "min"),
                        isUp = summary.thisWeekMinutes >= summary.lastWeekMinutes
                    )
                    Spacer(Modifier.height(10.dp))
                    ComparisonRow(
                        label = t("النجوم المكتسبة", "Stars Earned"),
                        thisWeekVal = "${toArabicDigits(summary.thisWeekStars)} 🌟",
                        lastWeekVal = "${toArabicDigits(summary.lastWeekStars)} 🌟",
                        isUp = summary.thisWeekStars >= summary.lastWeekStars
                    )
                    Spacer(Modifier.height(10.dp))
                    ComparisonRow(
                        label = t("صفحات القرآن المقروءة", "Qur'an Pages Read"),
                        thisWeekVal = "${toArabicDigits(summary.quranPagesToday * 7)} " + t("صفحة", "pages"),
                        lastWeekVal = "${toArabicDigits(maxOf(5, summary.quranPagesToday * 5))} " + t("صفحة", "pages"),
                        isUp = true
                    )
                }
            }
        }
    }
}

// ────────────── 5. قسم ورد القرآن الأصلي (Quran Wird Stats Section) ──────────────
@Composable
private fun QuranWirdSection() {
    val context = LocalContext.current
    val sp = remember { context.getSharedPreferences(QURAN_TRACKER_PREFS, Context.MODE_PRIVATE) }
    var trackerJsonStr by remember { mutableStateOf(sp.getString(KEY_QURAN_TRACKER, "{}") ?: "{}") }

    val todayKey = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }

    val trackerObj = remember(trackerJsonStr) {
        try { JSONObject(trackerJsonStr) } catch (e: Exception) { JSONObject() }
    }

    val goal = trackerObj.optInt("goal", 5)
    val streak = trackerObj.optInt("streak", 1)
    val todayObj = trackerObj.optJSONObject(todayKey) ?: JSONObject()
    val todayPages = todayObj.optInt("pages", 0)

    val completedSurahsSet = remember(trackerJsonStr) {
        val set = mutableSetOf<Int>()
        val arr = trackerObj.optJSONArray("completed_surahs") ?: JSONArray()
        for (i in 0 until arr.length()) set.add(arr.getInt(i))
        set
    }

    val saveTracker = { newObj: JSONObject ->
        val s = newObj.toString()
        sp.edit().putString(KEY_QURAN_TRACKER, s).apply()
        trackerJsonStr = s
    }

    val updatePages = { delta: Int ->
        val obj = JSONObject(trackerJsonStr)
        val tObj = obj.optJSONObject(todayKey) ?: JSONObject()
        val current = tObj.optInt("pages", 0)
        val nextVal = maxOf(0, current + delta)
        tObj.put("pages", nextVal)
        if (delta > 0 && nextVal >= goal && !tObj.optBoolean("completed", false)) {
            tObj.put("completed", true)
            obj.put("streak", obj.optInt("streak", 0) + 1)
        }
        obj.put(todayKey, tObj)
        saveTracker(obj)
        AnalyticsTracker.trackQuranSession(context, maxOf(1, delta))
    }

    val setGoal = { newGoal: Int ->
        val obj = JSONObject(trackerJsonStr)
        obj.put("goal", newGoal)
        saveTracker(obj)
    }

    val allTimeTotalPages = remember(trackerJsonStr) {
        var sum = 0
        val keys = trackerObj.keys()
        while (keys.hasNext()) {
            val k = keys.next()
            if (k != "goal" && k != "streak" && k != "completed_surahs") {
                val day = trackerObj.optJSONObject(k)
                if (day != null) sum += day.optInt("pages", 0)
            }
        }
        maxOf(sum, todayPages)
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Quran Progress Ring Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                modifier = Modifier
                    .fillMaxWidth()
                    .noorGlassCard()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(t("ورد اليوم من القرآن الكريم", "Today's Qur'an Portion"), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Gold)
                    Spacer(Modifier.height(16.dp))

                    val progressFraction = if (goal > 0) (todayPages.toFloat() / goal.toFloat()).coerceIn(0f, 1f) else 0f
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(140.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val strokeWidth = 14.dp.toPx()
                            val diameter = size.minDimension - strokeWidth
                            val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
                            val arcSize = Size(diameter, diameter)

                            drawArc(
                                color = Gold.copy(alpha = 0.15f),
                                startAngle = 0f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = Stroke(width = strokeWidth)
                            )
                            drawArc(
                                color = Gold,
                                startAngle = -90f,
                                sweepAngle = 360f * progressFraction,
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${toArabicDigits(todayPages)} / ${toArabicDigits(goal)}",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMain
                            )
                            Text(t("صفحة اليوم", "Today's Page"), fontSize = 11.sp, color = GoldSoft)
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Adjust Pages Buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { updatePages(-1) },
                            colors = ButtonDefaults.buttonColors(containerColor = NavyCard),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("- ١ صفحة", color = Gold, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { updatePages(1) },
                            colors = ButtonDefaults.buttonColors(containerColor = Gold),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("+ ١ صفحة", color = Navy, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { updatePages(5) },
                            colors = ButtonDefaults.buttonColors(containerColor = Gold),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("+ ٥ صفحات", color = Navy, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    // Target Goal Selector
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(t("الهدف اليومي:", "Daily goal:"), fontSize = 12.sp, color = GoldSoft)
                        listOf(3, 5, 10, 20).forEach { target ->
                            val active = goal == target
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (active) Gold else NavyCard)
                                    .clickable { setGoal(target) }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    toArabicDigits(target),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (active) Navy else TextMain
                                )
                            }
                        }
                    }
                }
            }
        }

        // Badges and All-Time Stats
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                modifier = Modifier
                    .fillMaxWidth()
                    .noorGlassCard()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(t("إنجازات وأوسمة التلاوة", "Recitation Achievements & Badges"), color = Gold, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(t("إجمالي الصفحات المقروءة:", "Total pages read:"), color = TextMain, fontSize = 13.sp)
                        Text("${toArabicDigits(allTimeTotalPages)} صفحة", color = Gold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(t("أيام المواظبة على الورد:", "Wird consistency days:"), color = TextMain, fontSize = 13.sp)
                        Text("${toArabicDigits(streak)} يوم متتالي 🔥", color = Gold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ────────────── Canvas Chart Drawables ──────────────

@Composable
private fun DailyMinutesLineChart(data: List<Pair<String, Int>>) {
    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        animProgress.animateTo(1f, animationSpec = tween(1000, easing = FastOutSlowInEasing))
    }

    val maxVal = maxOf(30, (data.maxOfOrNull { it.second } ?: 30) + 10)

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
    ) {
        val width = size.width
        val height = size.height - 30.dp.toPx()
        val spacing = width / maxOf(1, data.size - 1)

        val points = data.mapIndexed { idx, pair ->
            val x = idx * spacing
            val y = height - (pair.second.toFloat() / maxVal) * height
            Offset(x, y)
        }

        if (points.size > 1) {
            val path = Path()
            val fillPath = Path()

            path.moveTo(points.first().x, points.first().y)
            fillPath.moveTo(points.first().x, height)
            fillPath.lineTo(points.first().x, points.first().y)

            for (i in 0 until points.size - 1) {
                val p1 = points[i]
                val p2 = points[i + 1]
                val controlX = (p1.x + p2.x) / 2
                path.cubicTo(controlX, p1.y, controlX, p2.y, p2.x, p2.y)
                fillPath.cubicTo(controlX, p1.y, controlX, p2.y, p2.x, p2.y)
            }

            fillPath.lineTo(points.last().x, height)
            fillPath.close()

            // Draw area fill
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(Gold.copy(alpha = 0.35f * animProgress.value), Color.Transparent)
                )
            )

            // Draw line
            drawPath(
                path = path,
                color = Gold,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )

            // Draw node circles
            points.forEach { pt ->
                drawCircle(color = Gold, radius = 5.dp.toPx(), center = pt)
                drawCircle(color = Navy, radius = 2.5.dp.toPx(), center = pt)
            }
        }
    }
}

@Composable
private fun FeatureOpensBarChart(data: Map<String, Int>) {
    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        animProgress.animateTo(1f, animationSpec = tween(900, easing = FastOutSlowInEasing))
    }

    val maxVal = maxOf(10, (data.values.maxOrNull() ?: 10) + 5)
    val entries = data.toList()

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
    ) {
        val width = size.width
        val height = size.height - 20.dp.toPx()
        val barWidth = 24.dp.toPx()
        val gap = (width - (entries.size * barWidth)) / maxOf(1, entries.size + 1)

        entries.forEachIndexed { idx, (label, value) ->
            val barHeight = (value.toFloat() / maxVal) * height * animProgress.value
            val x = gap + idx * (barWidth + gap)
            val y = height - barHeight

            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Gold, GoldSoft.copy(alpha = 0.5f))
                ),
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
            )
        }
    }
}

@Composable
private fun WeeklyProgressCurveChart(data: List<Pair<String, Int>>) {
    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        animProgress.animateTo(1f, animationSpec = tween(1000))
    }

    val maxVal = maxOf(100, (data.maxOfOrNull { it.second } ?: 100) + 20)

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
    ) {
        val width = size.width
        val height = size.height - 20.dp.toPx()
        val spacing = width / maxOf(1, data.size - 1)

        val points = data.mapIndexed { idx, pair ->
            val x = idx * spacing
            val y = height - (pair.second.toFloat() / maxVal) * height
            Offset(x, y)
        }

        if (points.size > 1) {
            val path = Path()
            path.moveTo(points.first().x, points.first().y)

            for (i in 0 until points.size - 1) {
                val p1 = points[i]
                val p2 = points[i + 1]
                val controlX = (p1.x + p2.x) / 2
                path.cubicTo(controlX, p1.y, controlX, p2.y, p2.x, p2.y)
            }

            drawPath(
                path = path,
                color = EmeraldSuccess,
                style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
            )

            points.forEach { pt ->
                drawCircle(color = EmeraldSuccess, radius = 5.dp.toPx(), center = pt)
            }
        }
    }
}

@Composable
private fun CompletionDonutChart(summary: AnalyticsTracker.AnalyticsSummary) {
    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        animProgress.animateTo(1f, animationSpec = tween(1100, easing = FastOutSlowInEasing))
    }

    val quranFraction = (summary.quranCompletedSurahsCount.toFloat() / 114f).coerceIn(0.1f, 1f)
    val gamesFraction = (summary.totalCompletedLevels.toFloat() / 100f).coerceIn(0.15f, 1f)
    val tasbihFraction = 0.6f

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(160.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 14.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val arcSize = Size(diameter, diameter)
            val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

            // Background ring
            drawArc(
                color = Gold.copy(alpha = 0.12f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth)
            )

            // Segment 1: Quran (Gold)
            val sweep1 = 360f * 0.40f * animProgress.value
            drawArc(
                color = Gold,
                startAngle = -90f,
                sweepAngle = sweep1,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Segment 2: Games (Emerald)
            val sweep2 = 360f * 0.35f * animProgress.value
            drawArc(
                color = EmeraldSuccess,
                startAngle = -90f + sweep1 + 5f,
                sweepAngle = sweep2,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Segment 3: Tasbih (Cyan)
            val sweep3 = 360f * 0.20f * animProgress.value
            drawArc(
                color = Color(0xFF4FC3F7),
                startAngle = -90f + sweep1 + sweep2 + 10f,
                sweepAngle = sweep3,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("٦٨٪", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Gold)
            Text(t("نسبة الإنجاز", "Completion Rate"), fontSize = 11.sp, color = GoldSoft)
        }
    }
}

// ────────────── Helper UI Composables ──────────────

@Composable
private fun StatCard(title: String, value: String, sub: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = modifier.noorGlassCard()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, fontSize = 11.5.sp, color = GoldSoft.copy(alpha = 0.8f))
                Icon(icon, contentDescription = null, tint = Gold, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.height(6.dp))
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextMain)
            Text(sub, fontSize = 10.sp, color = GoldSoft.copy(alpha = 0.6f))
        }
    }
}

@Composable
private fun LegendRow(color: Color, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(Modifier.width(8.dp))
            Text(label, fontSize = 12.sp, color = TextMain)
        }
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Gold)
    }
}

@Composable
private fun BreakdownProgressCard(title: String, completed: Int, total: Int, unit: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    val fraction = if (total > 0) (completed.toFloat() / total.toFloat()).coerceIn(0f, 1f) else 0f
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = Modifier
            .fillMaxWidth()
            .noorGlassCard()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, contentDescription = null, tint = Gold, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(title, color = TextMain, fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                }
                Text("${toArabicDigits(completed)} / ${toArabicDigits(total)} $unit", color = Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { fraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Gold,
                trackColor = Gold.copy(alpha = 0.15f)
            )
        }
    }
}

@Composable
private fun ComparisonRow(label: String, thisWeekVal: String, lastWeekVal: String, isUp: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(NavyCard)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(label, color = TextMain, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
            Text("السابق: $lastWeekVal", color = GoldSoft.copy(alpha = 0.6f), fontSize = 10.5.sp)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(thisWeekVal, color = Gold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(6.dp))
            Text(if (isUp) "↗️" else "↘️", fontSize = 14.sp)
        }
    }
}
