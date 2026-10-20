package com.elhajri.noor.games

import com.elhajri.noor.settings.t

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.Navy
import com.elhajri.noor.ui.NavyCard
import com.elhajri.noor.ui.TextMain
import com.elhajri.noor.ui.TextSecondaryLinen
import com.elhajri.noor.ui.noorGlassCard
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs

/**
 * Data structure representing today's rotating daily challenge game.
 */
data class DailyChallengeInfo(
    val gameRoute: String,
    val title: String,
    val desc: String,
    val icon: ImageVector,
    val bonusStars: Int = 20,
    val isCompletedToday: Boolean
)

/**
 * DailyChallengeManager: Handles the 24-hour rotating daily challenge,
 * tracking completion state, awarding +20 bonus stars, and providing countdown timers.
 */
object DailyChallengeManager {

    private const val PREFS_NAME = "noor_daily_challenge_v1"
    private const val KEY_LAST_COMPLETED_DATE = "last_completed_date"
    private const val BONUS_STARS = 20

    // Curated catalog of candidate games for daily challenge rotation
    val DAILY_GAME_CATALOG = listOf(
        DailyChallengeInfo("game_kalimat", t("كلمة من الذكر", "Dhikr Word"), t("تخمين اسم الله الحسنى لليوم", "Guess today's Divine Name"), Icons.Default.WbTwilight, BONUS_STARS, false),
        DailyChallengeInfo("game_trivia", t("ميدان المعرفة", "Knowledge Field"), t("السلم الذهبي ذو 10 أسئلة إيمانية", "Golden ladder with 10 faith questions"), Icons.Default.EmojiEvents, BONUS_STARS, false),
        DailyChallengeInfo("game_ayat", t("سلسلة الآيات", "Verse Chain"), t("ترتيب آيات السورة واختبار الحفظ", "Arrange surah verses and test memorization"), Icons.Default.Timer, BONUS_STARS, false),
        DailyChallengeInfo("game_match", t("رباط معاني الله", "Names of Allah Meanings"), t("طابق أسماء الله الحسنى بمعانيها", "Match Names of Allah with their meanings"), Icons.Default.Flip, BONUS_STARS, false),
        DailyChallengeInfo("game_timeline", t("خط الزمن النبوي", "Prophetic Timeline"), t("ترتيب أحداث السيرة زمنياً", "Order Seerah events chronologically"), Icons.Default.Psychology, BONUS_STARS, false),
        DailyChallengeInfo("game_tartil", t("مرآة الترتيل", "Tartil Mirror"), t("إكمال الكلمة المغطاة في الآية", "Complete the covered word in the verse"), Icons.Default.GridOn, BONUS_STARS, false),
        DailyChallengeInfo("game_prophets_order", t("ترتيب الأنبياء", "Order of Prophets"), t("ترتيب الأنبياء حسب التسلسل الزمني", "Order prophets chronologically"), Icons.Default.FormatListNumbered, BONUS_STARS, false),
        DailyChallengeInfo("game_true_false", t("صح أم خطأ", "True or False"), t("اختبار التحدي السريع لأسئلة الدين", "Quick religion trivia quiz"), Icons.Default.Check, BONUS_STARS, false),
        DailyChallengeInfo("game_quran_mem", t("حفظ القرآن", "Qur'an Memorization"), t("اختبار متقدم لحفظ سور المصحف", "Advanced surah memorization test"), Icons.Default.MenuBook, BONUS_STARS, false),
        DailyChallengeInfo("game_wudu", t("تحدي الوضوء", "Wudu Challenge"), t("ترتيب خطوات الوضوء الصحيحة", "Order correct Wudu steps"), Icons.Default.WaterDrop, BONUS_STARS, false),
        DailyChallengeInfo("game_tawaf", t("ترتيب الطواف", "Tawaf Order"), t("ترتيب مناسك الطواف والعمرة", "Order Tawaf and Umrah rituals"), Icons.Default.Mosque, BONUS_STARS, false),
        DailyChallengeInfo("game_deeds_balance", t("ميزان الحسنات", "Scale of Good Deeds"), t("اختيار الأعمال الأعظم أجراً", "Choose deeds with higher reward"), Icons.Default.Balance, BONUS_STARS, false)
    )

    private fun prefs(context: Context): SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun todayDateString(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    fun isCompletedToday(context: Context): Boolean {
        val lastDate = prefs(context).getString(KEY_LAST_COMPLETED_DATE, "")
        return lastDate == todayDateString()
    }

    /**
     * Gets today's rotating game deterministically based on date.
     */
    fun getTodayChallenge(context: Context): DailyChallengeInfo {
        val dateStr = todayDateString()
        val index = abs(dateStr.hashCode()) % DAILY_GAME_CATALOG.size
        val base = DAILY_GAME_CATALOG[index]
        return base.copy(isCompletedToday = isCompletedToday(context))
    }

    /**
     * Complete today's daily challenge and award +20 bonus stars!
     * Returns the awarded bonus stars if first time today, else 0.
     */
    fun completeDailyChallenge(context: Context): Int {
        if (isCompletedToday(context)) return 0
        prefs(context).edit().putString(KEY_LAST_COMPLETED_DATE, todayDateString()).apply()
        // Credit bonus stars
        StarEconomyStore.addStars(context, BONUS_STARS)
        return BONUS_STARS
    }

    /**
     * Calculates remaining time until midnight (HH:mm:ss format).
     */
    fun getRemainingTimeFormatted(): String {
        val cal = Calendar.getInstance()
        val now = cal.timeInMillis
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        val midnight = cal.timeInMillis
        val diffMs = (midnight - now).coerceAtLeast(0)

        val hours = diffMs / (1000 * 60 * 60)
        val minutes = (diffMs / (1000 * 60)) % 60
        val seconds = (diffMs / 1000) % 60

        val map = mapOf('0' to '٠', '1' to '١', '2' to '٢', '3' to '٣', '4' to '٤', '5' to '٥', '6' to '٦', '7' to '٧', '8' to '٨', '9' to '٩')
        val raw = String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
        return raw.map { map[it] ?: it }.joinToString("")
    }
}

/**
 * Composable: Daily Challenge Hub Card with live countdown timer and direct play action.
 */
@Composable
fun DailyChallengeHubCard(
    onPlayChallenge: (String) -> Unit
) {
    val context = LocalContext.current
    var challengeInfo by remember { mutableStateOf(DailyChallengeManager.getTodayChallenge(context)) }
    var remainingTime by remember { mutableStateOf(DailyChallengeManager.getRemainingTimeFormatted()) }

    // Live countdown timer loop
    LaunchedEffect(Unit) {
        while (true) {
            remainingTime = DailyChallengeManager.getRemainingTimeFormatted()
            challengeInfo = DailyChallengeManager.getTodayChallenge(context)
            delay(1000L)
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .noorGlassCard(cornerRadius = 20.dp)
            .border(
                2.dp,
                Brush.horizontalGradient(listOf(Gold, Color(0xFF50C878), Gold)),
                RoundedCornerShape(20.dp)
            )
            .clickable { onPlayChallenge(challengeInfo.gameRoute) },
        color = Color.Transparent
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Gold.copy(alpha = 0.2f))
                            .border(1.dp, Gold, RoundedCornerShape(10.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = Gold, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(t("تحدي اليوم", "Daily Challenge"), color = Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF50C878).copy(alpha = 0.2f))
                            .border(1.dp, Color(0xFF50C878), RoundedCornerShape(10.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(t("+٢٠ 🌟 بونوس", "+20 🌟 Bonus"), color = Color(0xFF50C878), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Timer Pill
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, contentDescription = null, tint = GoldSoft, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(remainingTime, color = GoldSoft, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Gold.copy(alpha = 0.15f))
                        .border(1.dp, Gold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(challengeInfo.icon, contentDescription = null, tint = Gold, modifier = Modifier.size(26.dp))
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(challengeInfo.title, color = TextMain, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(challengeInfo.desc, color = TextSecondaryLinen, fontSize = 11.sp)
                }

                Spacer(Modifier.width(8.dp))

                if (challengeInfo.isCompletedToday) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF50C878))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Navy, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(t("مكتمل ✅", "Completed ✅"), color = Navy, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Button(
                        onClick = { onPlayChallenge(challengeInfo.gameRoute) },
                        colors = ButtonDefaults.buttonColors(containerColor = Gold),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(t("العب الآن ⚔️", "Play Now ⚔️"), color = Navy, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Compact Daily Challenge Home Card suitable for embedding in main Home screen.
 */
@Composable
fun DailyChallengeHomeCard(
    onPlayChallenge: (String) -> Unit
) {
    val context = LocalContext.current
    var challengeInfo by remember { mutableStateOf(DailyChallengeManager.getTodayChallenge(context)) }
    var remainingTime by remember { mutableStateOf(DailyChallengeManager.getRemainingTimeFormatted()) }

    LaunchedEffect(Unit) {
        while (true) {
            remainingTime = DailyChallengeManager.getRemainingTimeFormatted()
            challengeInfo = DailyChallengeManager.getTodayChallenge(context)
            delay(1000L)
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .noorGlassCard(cornerRadius = 16.dp)
            .border(1.dp, Gold, RoundedCornerShape(16.dp))
            .clickable { onPlayChallenge(challengeInfo.gameRoute) },
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Gold.copy(alpha = 0.2f))
                        .border(1.dp, Gold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(challengeInfo.icon, contentDescription = null, tint = Gold, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("تحدي اليوم 🌙", color = Gold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(6.dp))
                        Text(remainingTime, color = GoldSoft, fontSize = 10.sp)
                    }
                    Text(challengeInfo.title, color = TextMain, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (challengeInfo.isCompletedToday) Color(0xFF50C878) else Gold)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    if (challengeInfo.isCompletedToday) t("مكتمل ✅", "Completed ✅") else t("ابدأ التحدي", "Start Challenge"),
                    color = Navy,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
