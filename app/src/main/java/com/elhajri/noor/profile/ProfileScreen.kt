package com.elhajri.noor.profile

import com.elhajri.noor.settings.t
import com.elhajri.noor.settings.toArabicDigits

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.analytics.AnalyticsTracker
import com.elhajri.noor.theme.ThemeStore
import com.elhajri.noor.ui.AmiriFamily
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.Navy
import com.elhajri.noor.ui.NavyCard
import com.elhajri.noor.ui.TextMain
import com.elhajri.noor.ui.TextSecondaryLinen
import com.elhajri.noor.ui.noorGlassCard
import com.elhajri.noor.ui.themeScreenBackground
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * SharedPreferences storage helper for local user profile details.
 */
object UserProfileStore {
    private const val PREFS_NAME = "noor_user_profile"
    private const val KEY_NAME = "user_name"
    private const val KEY_BIO = "user_bio"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getUserName(context: Context): String =
        prefs(context).getString(KEY_NAME, t(t(t("مستخدم نور المميز", "NOOR Distinguished User"), "NOOR Distinguished User"), "NOOR Distinguished User")) ?: t(t(t("مستخدم نور المميز", "NOOR Distinguished User"), "NOOR Distinguished User"), "NOOR Distinguished User")

    fun setUserName(context: Context, name: String) {
        prefs(context).edit().putString(KEY_NAME, name.ifBlank { t(t(t("مستخدم نور المميز", "NOOR Distinguished User"), "NOOR Distinguished User"), "NOOR Distinguished User") }).apply()
    }

    fun getUserBio(context: Context): String =
        prefs(context).getString(KEY_BIO, t(t(t("حافظ لكتاب الله وذاكر للمولى 🌙", "Memorizer of Allah's Book & Rememberer 🌙"), "Memorizer of Allah's Book & Rememberer 🌙"), "Memorizer of Allah's Book & Rememberer 🌙")) ?: t(t(t("حافظ لكتاب الله وذاكر للمولى 🌙", "Memorizer of Allah's Book & Rememberer 🌙"), "Memorizer of Allah's Book & Rememberer 🌙"), "Memorizer of Allah's Book & Rememberer 🌙")

    fun setUserBio(context: Context, bio: String) {
        prefs(context).edit().putString(KEY_BIO, bio).apply()
    }
}

/**
 * XP Rank tier definition.
 */
data class XpRankTier(
    val level: Int,
    val title: String,
    val minXp: Int,
    val icon: String,
    val description: String
)

val XP_TIERS = listOf(
    XpRankTier(1, t("مبتدئ", "Beginner"), 0, "🌱", "بداية الرحلة الإيمانية المباركة"),
    XpRankTier(2, t("متدرب", "Trainee"), 100, "📜", t("الاستمرار في التعلم والذكر", "Continuing learning and remembrance")),
    XpRankTier(3, t("قارئ", "Reciter"), 300, "📖", "المواظبة على تلاوة القرآن وتحديات الألعاب"),
    XpRankTier(4, t("باحث", "Seeker"), 600, "🔍", "التوسع في المعرفة والفقه والسيرة"),
    XpRankTier(5, t("مجتهد", "Diligent"), 1000, "⚡", t("الاجتهاد والمنافسة في ميدان الخير", "Striving and competing in goodness")),
    XpRankTier(6, t("متميز", "Distinguished"), 1500, "🌟", "التميز والإتقان في العلوم الشرعية"),
    XpRankTier(7, t("متقن", "Master"), 2100, "💎", "درجة عالية من الإتقان والتحصيل"),
    XpRankTier(8, t("حافظ", "Memorizer"), 3000, "👑", t("الارتقاء إلى مرتبة الحفاظ والذاكرين", "Ascending to the rank of memorizers and remembrancers")),
    XpRankTier(9, t("عالم", "Scholar"), 4000, "⚜️", "أعلى مراتب العلم والإنجاز في نور")
)

fun getRankForXp(xp: Int): XpRankTier {
    return XP_TIERS.lastOrNull { xp >= it.minXp } ?: XP_TIERS.first()
}

fun getNextRankForXp(xp: Int): XpRankTier? {
    return XP_TIERS.firstOrNull { xp < it.minXp }
}

/**
 * Achievement badge representation.
 */
data class ProfileAchievement(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val isUnlocked: Boolean,
    val currentProgress: Int,
    val targetProgress: Int
)

/**
 * Certificate item representation.
 */
data class ProfileCertificate(
    val id: String,
    val title: String,
    val category: String,
    val isUnlocked: Boolean,
    val description: String,
    val issueDate: String,
    val verificationCode: String
)

private fun toArDigits(value: Any): String {
    val map = mapOf('0' to '٠', '1' to '١', '2' to '٢', '3' to '٣', '4' to '٤', '5' to '٥', '6' to '٦', '7' to '٧', '8' to '٨', '9' to '٩')
    return value.toString().map { map[it] ?: it }.joinToString("")
}

/**
 * Main Profile Screen (شاشة ملفي الشخصي)
 */
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onNavigate: (String) -> Unit = {}
) {
    val context = LocalContext.current
    var userName by remember { mutableStateOf(UserProfileStore.getUserName(context)) }
    var userBio by remember { mutableStateOf(UserProfileStore.getUserBio(context)) }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedCertForDialog by remember { mutableStateOf<ProfileCertificate?>(null) }

    // Load analytics summary & points
    val summary = remember { AnalyticsTracker.getSummary(context) }
    val points = remember { ThemeStore.getPoints(context) }

    val totalXp = summary.totalXp
    val totalStars = summary.totalStars
    val completedLevels = summary.totalCompletedLevels
    val streakDays = summary.activeStreakDays
    val tasbihCount = summary.tasbihTotalCount

    val currentRank = getRankForXp(totalXp)
    val nextRank = getNextRankForXp(totalXp)

    // Calculate XP Progress Fraction
    val currentBaseXp = currentRank.minXp
    val nextTargetXp = nextRank?.minXp ?: (currentBaseXp + 1000)
    val xpSpan = (nextTargetXp - currentBaseXp).coerceAtLeast(1)
    val xpProgressInTier = (totalXp - currentBaseXp).coerceAtLeast(0)
    val progressFraction = (xpProgressInTier.toFloat() / xpSpan.toFloat()).coerceIn(0f, 1f)

    // Build dynamic achievements list
    val achievements = listOf(
        ProfileAchievement(
            id = "first_step",
            title = t(t(t("خطوة الهداية", "Step of Guidance"), "Step of Guidance"), "Step of Guidance"),
            description = t("إكمال مرحلة واحدة في الألعاب الإيمانية", "Complete 1 level in Islamic games"),
            icon = Icons.Filled.CheckCircle,
            isUnlocked = completedLevels >= 1,
            currentProgress = minOf(completedLevels, 1),
            targetProgress = 1
        ),
        ProfileAchievement(
            id = "star_gatherer",
            title = t("جامع النجوم", "Star Collector"),
            description = t(t(t("جمع ٣٠ نجمة ذهبية من الألعاب", "Collect 30 golden stars from games"), "Collect 30 golden stars from games"), "Collect 30 golden stars from games"),
            icon = Icons.Filled.Star,
            isUnlocked = totalStars >= 30,
            currentProgress = minOf(totalStars, 30),
            targetProgress = 30
        ),
        ProfileAchievement(
            id = "constant_dhikr",
            title = t(t(t("اللسان الذاكر", "Remembering Tongue"), "Remembering Tongue"), "Remembering Tongue"),
            description = t("إنجاز ١٠٠ تسبيحة في المسبحة الإلكترونية", "Reach 100 tasbih count"),
            icon = Icons.Filled.Favorite,
            isUnlocked = tasbihCount >= 100,
            currentProgress = minOf(tasbihCount, 100),
            targetProgress = 100
        ),
        ProfileAchievement(
            id = "faith_streak",
            title = t("سلسلة الإيمان", "Faith Streak"),
            description = t(t(t("الحفاظ على تتابع نشاط يومي لمدة ٣ أيام", "Maintain a 3-day daily activity streak"), "Maintain a 3-day daily activity streak"), "Maintain a 3-day daily activity streak"),
            icon = Icons.Filled.LocalFireDepartment,
            isUnlocked = streakDays >= 3,
            currentProgress = minOf(streakDays, 3),
            targetProgress = 3
        ),
        ProfileAchievement(
            id = "quran_reader",
            title = t("قارئ القرآن", "Qur'an Reciter"),
            description = t(t(t("قراءة ومتابعة ورد القرآن في التطبيق", "Reading & tracking Quran wird in app"), "Reading & tracking Quran wird in app"), "Reading & tracking Quran wird in app"),
            icon = Icons.AutoMirrored.Filled.MenuBook,
            isUnlocked = summary.quranTotalPagesAllTime >= 5,
            currentProgress = minOf(summary.quranTotalPagesAllTime, 5),
            targetProgress = 5
        ),
        ProfileAchievement(
            id = "noor_champion",
            title = t(t(t("بطل الميدان", "Arena Champion"), "Arena Champion"), "Arena Champion"),
            description = t("إكمال ١٥ مرحلة مختلفة في الألعاب", "Complete 15 different levels in games"),
            icon = Icons.Filled.EmojiEvents,
            isUnlocked = completedLevels >= 15,
            currentProgress = minOf(completedLevels, 15),
            targetProgress = 15
        )
    )

    // Build certificates list
    val todayDateStr = remember { SimpleDateFormat("yyyy/MM/dd", Locale("ar")).format(Date()) }
    val certificates = listOf(
        ProfileCertificate(
            id = "cert_merit_1",
            title = t(t(t("شهادة الجدارة - المستوى الأول", "Merit Certificate - Level 1"), "Merit Certificate - Level 1"), "Merit Certificate - Level 1"),
            category = t("الألعاب والمسابقات الإيمانية", "Faith Games & Quizzes"),
            isUnlocked = totalStars >= 15 || completedLevels >= 5,
            description = t(t(t("تُمنح للتميز في اجتياز المراحل التعليمية والإيمانية بنجاح في تطبيق نور.", "Awarded for excellence in passing educational and faith stages in NOOR."), "Awarded for excellence in passing educational and faith stages in NOOR."), "Awarded for excellence in passing educational and faith stages in NOOR."),
            issueDate = todayDateStr,
            verificationCode = "NOOR-MERIT-${(totalXp + 1000).toString().take(4)}"
        ),
        ProfileCertificate(
            id = "cert_mastery_1",
            title = t(t(t("شهادة التمكن - الثقافة الإسلامية", "Mastery Certificate - Islamic Culture"), "Mastery Certificate - Islamic Culture"), "Mastery Certificate - Islamic Culture"),
            category = t(t(t("المعرفة والعلوم الشرعية", "Islamic Knowledge & Sciences"), "Islamic Knowledge & Sciences"), "Islamic Knowledge & Sciences"),
            isUnlocked = totalXp >= 300 || completedLevels >= 10,
            description = t(t(t("تُمنح لإتقان المفاهيم الفقهية والسيرة النبوية الشريفة واجتياز التحديات.", "Awarded for mastering Fiqh, Seerah, and completing challenges."), "Awarded for mastering Fiqh, Seerah, and completing challenges."), "Awarded for mastering Fiqh, Seerah, and completing challenges."),
            issueDate = todayDateStr,
            verificationCode = "NOOR-MASTERY-${(totalXp + 2500).toString().take(4)}"
        ),
        ProfileCertificate(
            id = "cert_tasbih",
            title = t(t(t("شهادة الذاكرين والذاكرات", "Certificate of Dhikr & Remembrance"), "Certificate of Dhikr & Remembrance"), "Certificate of Dhikr & Remembrance"),
            category = t("الأذكار والتسابيح", "Adhkar & Tasbih"),
            isUnlocked = tasbihCount >= 100,
            description = t(t(t("تُمنح للاعتناء بذكر الله عز وجل والمواظبة على الأذكار والتسابيح.", "Awarded for devotion to Dhikr and regular Tasbih."), "Awarded for devotion to Dhikr and regular Tasbih."), "Awarded for devotion to Dhikr and regular Tasbih."),
            issueDate = todayDateStr,
            verificationCode = "NOOR-TASBIH-9981"
        ),
        ProfileCertificate(
            id = "cert_quran",
            title = t(t(t("شهادة إتمام ختمات وتلاوة القرآن", "Certificate of Qur'an Recitation"), "Certificate of Qur'an Recitation"), "Certificate of Qur'an Recitation"),
            category = t(t(t("تلاوة القرآن الكريم", "Qur'an Recitation"), "Qur'an Recitation"), "Qur'an Recitation"),
            isUnlocked = summary.quranTotalPagesAllTime >= 10,
            description = t(t(t("تُمنح للاعتناء بتلاوة كتاب الله وتتبع الورد اليومي المبارك.", "Awarded for dedication to reciting Allah's Book and daily Wird."), "Awarded for dedication to reciting Allah's Book and daily Wird."), "Awarded for dedication to reciting Allah's Book and daily Wird."),
            issueDate = todayDateStr,
            verificationCode = "NOOR-QURAN-7721"
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .themeScreenBackground()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .border(1.dp, Gold.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "رجوع",
                        tint = Gold
                    )
                }

                Text(
                    text = t(t(t("ملفي الشخصي", "My Profile"), "My Profile"), "My Profile"),
                    color = Gold,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = AmiriFamily
                )

                // Edit Profile Icon
                IconButton(
                    onClick = { showEditDialog = true },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .border(1.dp, Gold.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = t(t(t("تعديل الملف", "Edit Profile"), "Edit Profile"), "Edit Profile"),
                        tint = Gold,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 1. Premium Gold Glass Profile Hero Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .noorGlassCard(cornerRadius = 24.dp)
                    .border(1.5.dp, Gold.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                    .padding(20.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Avatar Ring with Crown
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(86.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(Gold.copy(alpha = 0.35f), Gold.copy(alpha = 0.10f), Color.Transparent)
                                    )
                                )
                                .border(2.dp, Gold, CircleShape)
                        )
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(NavyCard),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = currentRank.icon,
                                fontSize = 34.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = userName,
                        color = TextMain,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = AmiriFamily,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = userBio,
                        color = TextSecondaryLinen,
                        fontSize = 12.5.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Rank Pill Badge
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Navy,
                        border = BorderStroke(1.dp, Gold.copy(alpha = 0.6f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.WorkspacePremium,
                                contentDescription = null,
                                tint = Gold,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = t("الرتبة: ", "Rank: ") + "${currentRank.title} (${currentRank.icon})",
                                color = Gold,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. XP Rank Progress Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .noorGlassCard(cornerRadius = 20.dp)
                    .border(1.dp, Gold.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.MilitaryTech,
                                contentDescription = null,
                                tint = Gold,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = t(t(t("مستوى الخبرة (XP)", "Experience Level (XP)"), "Experience Level (XP)"), "Experience Level (XP)"),
                                color = Gold,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "${toArDigits(totalXp)} XP",
                            color = GoldSoft,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Progress Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Black.copy(alpha = 0.35f))
                            .border(0.5.dp, Gold.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(progressFraction)
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(GoldSoft, Gold)
                                    )
                                )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = t("الرتبة الحالية: ", "Current Rank: ") + "${currentRank.title}",
                            color = TextSecondaryLinen,
                            fontSize = 12.sp
                        )
                        if (nextRank != null) {
                            Text(
                                text = t("التالي: ", "Next: ") + "${nextRank.title} (" + t("تبقي ", "Remaining ") + "${toArDigits(nextTargetXp - totalXp)} XP)",
                                color = GoldSoft,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        } else {
                            Text(
                                text = t(t(t("وصلت لأعلى رتبة! ⚜️", "Reached highest rank! ⚜️"), "Reached highest rank! ⚜️"), "Reached highest rank! ⚜️"),
                                color = Gold,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Stats Grid (الرصيد والإحصائيات)
            Text(
                text = t("الرصيد والإحصائيات", "Balance & Stats"),
                color = Gold,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = AmiriFamily,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatMetricCard(
                    modifier = Modifier.weight(1f),
                    title = t("نجوم الألعاب", "Game Stars"),
                    value = toArDigits(totalStars),
                    icon = Icons.Filled.Star,
                    tint = Gold
                )
                StatMetricCard(
                    modifier = Modifier.weight(1f),
                    title = t(t(t("نقاط نور", "NOOR Points"), "NOOR Points"), "NOOR Points"),
                    value = "${toArDigits(points)} 🌙",
                    icon = Icons.Filled.NightlightRound,
                    tint = GoldSoft
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatMetricCard(
                    modifier = Modifier.weight(1f),
                    title = t(t(t("المراحل المكتملة", "Completed Stages"), "Completed Stages"), "Completed Stages"),
                    value = toArDigits(completedLevels),
                    icon = Icons.Filled.EmojiEvents,
                    tint = Gold
                )
                StatMetricCard(
                    modifier = Modifier.weight(1f),
                    title = t("أيام التتابع", "Streak Days"),
                    value = "${toArDigits(streakDays)} " + t("يوم", "days"),
                    icon = Icons.Filled.LocalFireDepartment,
                    tint = Color(0xFFFF9800)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 4. Achievements Section (شريط الإنجازات والوسامات)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = t("الإنجازات والوسامات", "Achievements & Badges"),
                    color = Gold,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = AmiriFamily
                )
                Text(
                    text = "${toArDigits(achievements.count { it.isUnlocked })} / ${toArDigits(achievements.size)} مكتمل",
                    color = GoldSoft,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                achievements.forEach { achievement ->
                    AchievementItemCard(achievement = achievement) {
                        // Share achievement item on click
                        val shareText = "حزت على وسام «${achievement.title}» في تطبيق نور الإيماني! 🌙✨\n${achievement.description}"
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, shareText)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "مشاركة الإنجاز"))
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 5. Certificates Grid (الشهادات والاعتمادات)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = t("الشهادات والاعتمادات", "Certificates & Credentials"),
                    color = Gold,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = AmiriFamily
                )
                Text(
                    text = "${toArDigits(certificates.count { it.isUnlocked })} مكتسبة",
                    color = GoldSoft,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                certificates.forEach { cert ->
                    CertificateItemCard(cert = cert, onPreview = {
                        if (cert.isUnlocked) {
                            selectedCertForDialog = cert
                        }
                    })
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    // Edit Name Dialog
    if (showEditDialog) {
        var tempName by remember { mutableStateOf(userName) }
        var tempBio by remember { mutableStateOf(userBio) }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            containerColor = Navy,
            title = {
                Text(
                    text = "تعديل البيانات الشخصية",
                    color = Gold,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = AmiriFamily
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = tempName,
                        onValueChange = { tempName = it },
                        label = { Text(t("الاسم الشخصي", "Display Name"), color = GoldSoft) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Gold,
                            unfocusedBorderColor = Gold.copy(alpha = 0.4f),
                            focusedLabelColor = Gold,
                            cursorColor = Gold
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = tempBio,
                        onValueChange = { tempBio = it },
                        label = { Text("العبارة الإيمانية", color = GoldSoft) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Gold,
                            unfocusedBorderColor = Gold.copy(alpha = 0.4f),
                            focusedLabelColor = Gold,
                            cursorColor = Gold
                        ),
                        maxLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        UserProfileStore.setUserName(context, tempName)
                        UserProfileStore.setUserBio(context, tempBio)
                        userName = UserProfileStore.getUserName(context)
                        userBio = UserProfileStore.getUserBio(context)
                        showEditDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Navy)
                ) {
                    Text("حفظ والتحديث", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text(t("إلغاء", "Cancel"), color = TextSecondaryLinen)
                }
            }
        )
    }

    // Certificate Full Preview Dialog
    if (selectedCertForDialog != null) {
        val cert = selectedCertForDialog!!
        AlertDialog(
            onDismissRequest = { selectedCertForDialog = null },
            containerColor = Navy,
            title = null,
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .noorGlassCard(cornerRadius = 20.dp)
                        .border(2.dp, Gold, RoundedCornerShape(20.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // Header Seal
                        Icon(
                            imageVector = Icons.Filled.WorkspacePremium,
                            contentDescription = null,
                            tint = Gold,
                            modifier = Modifier.size(48.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "تطبيق نور الإسلامي",
                            color = GoldSoft,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "شهادة تقدير واستحقاق",
                            color = Gold,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = AmiriFamily
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "تُمنح هذه الشهادة المباركة لـ:",
                            color = TextSecondaryLinen,
                            fontSize = 12.5.sp
                        )

                        Text(
                            text = userName,
                            color = TextMain,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = AmiriFamily,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = cert.description,
                            color = TextMain.copy(alpha = 0.9f),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        HorizontalDivider(color = Gold.copy(alpha = 0.3f), thickness = 1.dp)

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "التاريخ: ${cert.issueDate}",
                                color = GoldSoft,
                                fontSize = 11.sp
                            )
                            Text(
                                text = "الرمز: ${cert.verificationCode}",
                                color = GoldSoft,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val shareText = "حصلت بفضل الله على «${cert.title}» من تطبيق نور الإسلامي! 🌙✨\nرقم الاعتماد: ${cert.verificationCode}"
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, shareText)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "مشاركة الشهادة"))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Navy)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Share,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("مشاركة الشهادة", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedCertForDialog = null }) {
                    Text(t("إغلاق", "Close"), color = TextSecondaryLinen)
                }
            }
        )
    }
}

@Composable
private fun StatMetricCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    tint: Color
) {
    Box(
        modifier = modifier
            .noorGlassCard(cornerRadius = 16.dp)
            .border(1.dp, Gold.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White.copy(alpha = 0.06f))
                    .border(1.dp, tint.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = title,
                    color = TextSecondaryLinen,
                    fontSize = 11.5.sp
                )
                Text(
                    text = value,
                    color = Gold,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun AchievementItemCard(
    achievement: ProfileAchievement,
    onClickShare: () -> Unit
) {
    val isUnlocked = achievement.isUnlocked
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .noorGlassCard(cornerRadius = 16.dp)
            .border(
                1.dp,
                if (isUnlocked) Gold.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.15f),
                RoundedCornerShape(16.dp)
            )
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isUnlocked) Brush.radialGradient(listOf(Gold.copy(alpha = 0.3f), Gold.copy(alpha = 0.1f)))
                        else Brush.linearGradient(listOf(Color.White.copy(alpha = 0.05f), Color.White.copy(alpha = 0.02f)))
                    )
                    .border(
                        1.dp,
                        if (isUnlocked) Gold else Color.White.copy(alpha = 0.2f),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = achievement.icon,
                    contentDescription = null,
                    tint = if (isUnlocked) Gold else Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = achievement.title,
                        color = if (isUnlocked) TextMain else TextSecondaryLinen,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold
                    )

                    if (isUnlocked) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Gold.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "مكتسب ✓",
                                color = Gold,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    } else {
                        Text(
                            text = "${toArDigits(achievement.currentProgress)} / ${toArDigits(achievement.targetProgress)}",
                            color = TextSecondaryLinen,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = achievement.description,
                    color = TextSecondaryLinen,
                    fontSize = 11.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (isUnlocked) {
                Spacer(modifier = Modifier.width(6.dp))
                IconButton(
                    onClick = onClickShare,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Share,
                        contentDescription = "مشاركة",
                        tint = GoldSoft,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CertificateItemCard(
    cert: ProfileCertificate,
    onPreview: () -> Unit
) {
    val isUnlocked = cert.isUnlocked
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .noorGlassCard(cornerRadius = 18.dp)
            .border(
                1.dp,
                if (isUnlocked) Gold.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.15f),
                RoundedCornerShape(18.dp)
            )
            .clickable(enabled = isUnlocked) { onPreview() }
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isUnlocked) Brush.verticalGradient(listOf(Gold.copy(alpha = 0.25f), Gold.copy(alpha = 0.08f)))
                        else Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.04f), Color.White.copy(alpha = 0.02f)))
                    )
                    .border(
                        1.dp,
                        if (isUnlocked) Gold else Color.White.copy(alpha = 0.2f),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isUnlocked) Icons.Filled.WorkspacePremium else Icons.Filled.Lock,
                    contentDescription = null,
                    tint = if (isUnlocked) Gold else Color.Gray,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = cert.title,
                    color = if (isUnlocked) Gold else TextSecondaryLinen,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = cert.category,
                    color = TextSecondaryLinen,
                    fontSize = 11.5.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                if (isUnlocked) {
                    Text(
                        text = t("اضغط للفتح واستعراض الشهادة 📜", "Tap to open and view certificate 📜"),
                        color = GoldSoft,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                } else {
                    Text(
                        text = "قيد الإنجاز — أتم المقتضيات لفتح الشهادة",
                        color = TextSecondaryLinen.copy(alpha = 0.7f),
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
