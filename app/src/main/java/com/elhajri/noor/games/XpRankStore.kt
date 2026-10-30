package com.elhajri.noor.games
import androidx.compose.foundation.BorderStroke

import com.elhajri.noor.settings.t
import com.elhajri.noor.settings.toArabicDigits

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.analytics.AnalyticsTracker
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.Navy
import com.elhajri.noor.ui.NavyCard
import com.elhajri.noor.ui.TextMain
import com.elhajri.noor.ui.TextSecondaryLinen
import com.elhajri.noor.ui.noorGlassCard

/**
 * XP Rank tier definition.
 */
data class XpRankTierData(
    val level: Int,
    val titleAr: String,
    val titleEn: String,
    val minXp: Int,
    val iconSymbol: String,
    val descAr: String,
    val descEn: String
) {
    val titleArVal: String get() = titleAr
    val title: String get() = t(titleAr, titleEn)
    val desc: String get() = t(descAr, descEn)
}

/**
 * XpRankStore: Manages XP calculation, level win rewards, and rank progression.
 * Reuses the 9-tier XP system consistent with ProfileScreen.
 */
object XpRankStore {

    val XP_TIERS = listOf(
        XpRankTierData(1, "مبتدئ", "Beginner", 0, "🌱", "بداية الرحلة الإيمانية المباركة", "Start of the blessed faith journey"),
        XpRankTierData(2, "متدرب", "Trainee", 100, "📜", "الاستمرار في التعلم والذكر", "Continuing learning and dhikr"),
        XpRankTierData(3, "قارئ", "Reciter", 300, "📖", "المواظبة على تلاوة القرآن وتحديات الألعاب", "Regular Quran recitation and game challenges"),
        XpRankTierData(4, "باحث", "Researcher", 600, "🔍", "التوسع في المعرفة والفقه والسيرة", "Expanding knowledge in Fiqh and Seerah"),
        XpRankTierData(5, "مجتهد", "Diligent", 1000, "⚡", "الاجتهاد والمنافسة في ميدان الخير", "Diligence in good deeds"),
        XpRankTierData(6, "متميز", "Distinguished", 1500, "🌟", "التميز والإتقان في العلوم الشرعية", "Excellence in Islamic knowledge"),
        XpRankTierData(7, "متقن", "Master", 2100, "💎", "درجة عالية من الإتقان والتحصيل", "High level of mastery and achievement"),
        XpRankTierData(8, "حافظ", "Hafiz", 3000, "👑", "الارتقاء إلى مرتبة الحفاظ والذاكرين", "Elevated rank of Hafiz and rememberers"),
        XpRankTierData(9, "عالم", "Scholar", 4000, "⚜️", "أعلى مراتب العلم والإنجاز في نور", "Highest rank of knowledge in NOOR")
    )

    fun getRankForXp(xp: Int): XpRankTierData {
        return XP_TIERS.lastOrNull { xp >= it.minXp } ?: XP_TIERS.first()
    }

    fun getNextRankForXp(xp: Int): XpRankTierData? {
        return XP_TIERS.firstOrNull { xp < it.minXp }
    }

    /**
     * Helper to convert numbers to Arabic digits.
     */
    fun toArDigits(value: Any): String {
        val map = mapOf('0' to '٠', '1' to '١', '2' to '٢', '3' to '٣', '4' to '٤', '5' to '٥', '6' to '٦', '7' to '٧', '8' to '٨', '9' to '٩')
        return value.toString().map { map[it] ?: it }.joinToString("")
    }

    /**
     * Award level completion XP and stars.
     */
    fun rewardLevelCompletion(context: Context, gameName: String, levelIdx: Int, starsEarned: Int): Int {
        val baseXp = 20 + (starsEarned * 5)
        AnalyticsTracker.trackLevelCompleted(context, gameName, levelIdx, starsEarned, baseXp)
        StarEconomyStore.addStars(context, starsEarned)
        return baseXp
    }
}

/**
 * Composable: XP Rank progress bar & current tier status card for Game Hub and Profile.
 */
@Composable
fun XpRankProgressBarCard(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val summary = remember { AnalyticsTracker.getSummary(context) }
    val totalXp = summary.totalXp

    val currentRank = XpRankStore.getRankForXp(totalXp)
    val nextRank = XpRankStore.getNextRankForXp(totalXp)

    val currentBase = currentRank.minXp
    val nextTarget = nextRank?.minXp ?: (currentBase + 1000)
    val span = (nextTarget - currentBase).coerceAtLeast(1)
    val currentProgress = (totalXp - currentBase).coerceAtLeast(0)
    val progressFraction = (currentProgress.toFloat() / span.toFloat()).coerceIn(0f, 1f)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .noorGlassCard(cornerRadius = 18.dp)
            .border(1.dp, Gold.copy(alpha = 0.4f), RoundedCornerShape(18.dp)),
        color = Color.Transparent
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Gold.copy(alpha = 0.2f))
                            .border(1.dp, Gold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(currentRank.iconSymbol, fontSize = 18.sp)
                    }
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(t("الرتبة: ", "Rank: ") + currentRank.title, color = Gold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(currentRank.desc, color = TextSecondaryLinen, fontSize = 10.sp)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = NavyCard,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Gold)
                ) {
                    Text(
                        text = "${XpRankStore.toArDigits(totalXp)} XP",
                        color = Gold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(Color.White.copy(alpha = 0.1f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fraction = progressFraction)
                        .clip(RoundedCornerShape(5.dp))
                        .background(Brush.horizontalGradient(listOf(GoldSoft, Gold, Color(0xFF50C878))))
                )
            }

            Spacer(Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = t("الرتبة ", "Rank ") + currentRank.level.toArabicDigits() + ": " + currentRank.title,
                    color = TextSecondaryLinen,
                    fontSize = 10.sp
                )
                if (nextRank != null) {
                    Text(
                        text = t("الرتبة التالية: ", "Next Rank: ") + nextRank.title + " (" + nextRank.minXp.toArabicDigits() + " XP)",
                        color = GoldSoft,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                } else {
                    Text(t("أعلى رتبة إيمانية! 👑", "Highest Faith Rank! 👑"), color = Gold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
