package com.elhajri.noor.games

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.animation.core.*
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.theme.ThemeStore
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.Navy
import com.elhajri.noor.ui.NavyCard
import com.elhajri.noor.ui.TextMain
import com.elhajri.noor.ui.TextSecondaryLinen
import com.elhajri.noor.ui.noorGlassCard

/**
 * Cosmetic Card Frame representation.
 */
data class CardFrame(
    val id: String,
    val nameAr: String,
    val descAr: String,
    val costStars: Int,
    val borderBrush: Brush,
    val glowColor: Color,
    val icon: ImageVector
)

/**
 * StarEconomyStore: Local storage and currency manager for NOOR star economy.
 * Handles earning/spending stars, hint refills (~10 stars/hint), and cosmetic card frames.
 */
object StarEconomyStore {

    private const val PREFS_NAME = "noor_star_economy_v1"
    private const val KEY_STAR_BALANCE = "star_balance"
    private const val KEY_HINT_COUNT = "hint_count"
    private const val KEY_EQUIPPED_FRAME = "equipped_frame"
    private const val KEY_PURCHASED_FRAMES = "purchased_frames"

    const val HINT_REFILL_COST = 10

    // Frame Identifiers
    const val FRAME_CLASSIC = "frame_classic"
    const val FRAME_GOLD = "frame_gold"
    const val FRAME_ROYAL = "frame_royal"
    const val FRAME_CELESTIAL = "frame_celestial"

    val ALL_FRAMES = listOf(
        CardFrame(
            id = FRAME_CLASSIC,
            nameAr = "الإطار الأصيل",
            descAr = "إطار الزجاج والأرجوان التقليدي المميز",
            costStars = 0,
            borderBrush = Brush.horizontalGradient(listOf(Gold.copy(alpha = 0.4f), GoldSoft.copy(alpha = 0.2f))),
            glowColor = GoldSoft.copy(alpha = 0.2f),
            icon = Icons.Default.Shield
        ),
        CardFrame(
            id = FRAME_GOLD,
            nameAr = "الإطار الذهبي الزمردي",
            descAr = "إطار فاخر بلمعان الزمرد والذهب الخالص",
            costStars = 50,
            borderBrush = Brush.horizontalGradient(listOf(Color(0xFFD4AF37), Color(0xFF50C878), Color(0xFFD4AF37))),
            glowColor = Color(0xFF50C878),
            icon = Icons.Default.AutoAwesome
        ),
        CardFrame(
            id = FRAME_ROYAL,
            nameAr = "إطار الياقوت الملكي",
            descAr = "إطار الياقوت الملكي للتفوق والإنجاز العالي",
            costStars = 100,
            borderBrush = Brush.horizontalGradient(listOf(Color(0xFFE0115F), Color(0xFFFFD700), Color(0xFF990033))),
            glowColor = Color(0xFFE0115F),
            icon = Icons.Default.MilitaryTech
        ),
        CardFrame(
            id = FRAME_CELESTIAL,
            nameAr = "الإطار السماوي الفاخر",
            descAr = "إطار النور المتلألئ بفيض الأضواء والتبر",
            costStars = 200,
            borderBrush = Brush.horizontalGradient(listOf(Color(0xFF00FFFF), Color(0xFFFFD700), Color(0xFF8A2BE2))),
            glowColor = Color(0xFF00FFFF),
            icon = Icons.Default.WorkspacePremium
        )
    )

    private fun prefs(context: Context): SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // ───────────────────────── Balance ─────────────────────────

    fun getStarBalance(context: Context): Int {
        val sp = prefs(context)
        if (!sp.contains(KEY_STAR_BALANCE)) {
            // Seed initial balance from GameProgressStore total stars if first time
            val initialStars = calculateTotalGameStars(context)
            sp.edit().putInt(KEY_STAR_BALANCE, initialStars.coerceAtLeast(15)).apply()
        }
        return prefs(context).getInt(KEY_STAR_BALANCE, 15)
    }

    private fun calculateTotalGameStars(context: Context): Int {
        return try {
            val gamesList = listOf("kalimat", "trivia", "ayat", "match", "timeline", "tartil")
            var total = 0
            for (g in gamesList) {
                total += GameProgressStore.getTotalStars(context, g)
            }
            total
        } catch (_: Exception) {
            20
        }
    }

    fun addStars(context: Context, amount: Int): Int {
        if (amount <= 0) return getStarBalance(context)
        val current = getStarBalance(context)
        val updated = current + amount
        prefs(context).edit().putInt(KEY_STAR_BALANCE, updated).apply()
        // Also reward ThemeStore points
        ThemeStore.addPoints(context, amount * 5)
        return updated
    }

    fun spendStars(context: Context, amount: Int): Boolean {
        val current = getStarBalance(context)
        if (current < amount) return false
        val updated = current - amount
        prefs(context).edit().putInt(KEY_STAR_BALANCE, updated).apply()
        return true
    }

    // ───────────────────────── Hints Economy ─────────────────────────

    fun getHintCount(context: Context): Int {
        return prefs(context).getInt(KEY_HINT_COUNT, 5) // default 5 hints
    }

    fun useHint(context: Context): Boolean {
        val count = getHintCount(context)
        if (count <= 0) return false
        prefs(context).edit().putInt(KEY_HINT_COUNT, count - 1).apply()
        return true
    }

    fun buyHintRefill(context: Context, hintAmount: Int = 1, costPerHint: Int = HINT_REFILL_COST): Boolean {
        val totalCost = hintAmount * costPerHint
        if (spendStars(context, totalCost)) {
            val currentHints = getHintCount(context)
            prefs(context).edit().putInt(KEY_HINT_COUNT, currentHints + hintAmount).apply()
            return true
        }
        return false
    }

    // ───────────────────────── Cosmetic Frames ─────────────────────────

    fun getPurchasedFrames(context: Context): Set<String> {
        val set = prefs(context).getStringSet(KEY_PURCHASED_FRAMES, null)
        return set ?: setOf(FRAME_CLASSIC)
    }

    fun isFramePurchased(context: Context, frameId: String): Boolean {
        if (frameId == FRAME_CLASSIC) return true
        return getPurchasedFrames(context).contains(frameId)
    }

    fun buyFrame(context: Context, frameId: String): Boolean {
        if (isFramePurchased(context, frameId)) return true
        val frame = ALL_FRAMES.firstOrNull { it.id == frameId } ?: return false
        if (spendStars(context, frame.costStars)) {
            val current = getPurchasedFrames(context).toMutableSet()
            current.add(frameId)
            prefs(context).edit().putStringSet(KEY_PURCHASED_FRAMES, current).apply()
            equipFrame(context, frameId)
            return true
        }
        return false
    }

    fun getEquippedFrameId(context: Context): String {
        return prefs(context).getString(KEY_EQUIPPED_FRAME, FRAME_CLASSIC) ?: FRAME_CLASSIC
    }

    fun getEquippedFrame(context: Context): CardFrame {
        val id = getEquippedFrameId(context)
        return ALL_FRAMES.firstOrNull { it.id == id } ?: ALL_FRAMES.first()
    }

    fun equipFrame(context: Context, frameId: String) {
        if (isFramePurchased(context, frameId)) {
            prefs(context).edit().putString(KEY_EQUIPPED_FRAME, frameId).apply()
        }
    }
}

/**
 * Modifier extension to apply the user's equipped cosmetic card frame.
 */
fun Modifier.noorEquippedCardFrame(context: Context, cornerRadius: Dp = 18.dp): Modifier {
    val frame = StarEconomyStore.getEquippedFrame(context)
    return this
        .noorGlassCard(cornerRadius = cornerRadius)
        .border(2.dp, frame.borderBrush, RoundedCornerShape(cornerRadius))
}

/**
 * Star & Cosmetic Frame Shop Dialog / Sheet Component.
 */
@Composable
fun StarShopDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var starBalance by remember { mutableIntStateOf(StarEconomyStore.getStarBalance(context)) }
    var hintCount by remember { mutableIntStateOf(StarEconomyStore.getHintCount(context)) }
    var equippedFrameId by remember { mutableStateOf(StarEconomyStore.getEquippedFrameId(context)) }
    var purchasedFrames by remember { mutableStateOf(StarEconomyStore.getPurchasedFrames(context)) }
    var toastMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("إغلاق", color = Gold, fontWeight = FontWeight.Bold)
            }
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("متجر النجوم والإطارات 🌟", color = Gold, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = NavyCard,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Gold)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Gold, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(starBalance.toArabicDigits(), color = Gold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (toastMessage != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Gold.copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Gold)
                    ) {
                        Text(
                            text = toastMessage!!,
                            color = TextMain,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(8.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Section 1: Hint Refills
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White.copy(alpha = 0.05f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("تعبئة التلميحات (الهَمَسَات)", color = TextMain, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("رصيدك الحالي: ${hintCount.toArabicDigits()} تلميح", color = GoldSoft, fontSize = 11.sp)
                        }
                        Button(
                            onClick = {
                                if (StarEconomyStore.buyHintRefill(context, 1)) {
                                    starBalance = StarEconomyStore.getStarBalance(context)
                                    hintCount = StarEconomyStore.getHintCount(context)
                                    toastMessage = "تم شراء تلميح بنجاح! 💡"
                                } else {
                                    toastMessage = "رصيد النجوم غير كافٍ! تحتاج ١٠ نجوم."
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Gold),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("شراء (١٠ 🌟)", color = Navy, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Text("إطارات البطاقات التجميلية:", color = Gold, fontSize = 14.sp, fontWeight = FontWeight.Bold)

                // Section 2: Frames list
                StarEconomyStore.ALL_FRAMES.forEach { frame ->
                    val isPurchased = purchasedFrames.contains(frame.id)
                    val isEquipped = equippedFrameId == frame.id

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White.copy(alpha = 0.05f),
                        border = androidx.compose.foundation.BorderStroke(
                            if (isEquipped) 2.dp else 1.dp,
                            if (isEquipped) Gold else Color.White.copy(alpha = 0.15f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(frame.glowColor.copy(alpha = 0.2f))
                                        .border(1.dp, frame.borderBrush, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(frame.icon, contentDescription = null, tint = Gold, modifier = Modifier.size(20.dp))
                                }
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Text(frame.nameAr, color = TextMain, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text(frame.descAr, color = TextSecondaryLinen, fontSize = 10.sp)
                                }
                            }

                            if (isEquipped) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Gold,
                                    modifier = Modifier.padding(start = 4.dp)
                                ) {
                                    Text("مُفعّل", color = Navy, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                }
                            } else if (isPurchased) {
                                Button(
                                    onClick = {
                                        StarEconomyStore.equipFrame(context, frame.id)
                                        equippedFrameId = frame.id
                                        toastMessage = "تم تفعيل ${frame.nameAr}!"
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = NavyCard),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Gold),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("تفعيل", color = Gold, fontSize = 11.sp)
                                }
                            } else {
                                Button(
                                    onClick = {
                                        if (StarEconomyStore.buyFrame(context, frame.id)) {
                                            starBalance = StarEconomyStore.getStarBalance(context)
                                            purchasedFrames = StarEconomyStore.getPurchasedFrames(context)
                                            equippedFrameId = StarEconomyStore.getEquippedFrameId(context)
                                            toastMessage = "تم فتح ${frame.nameAr} بنجاح! 👑"
                                        } else {
                                            toastMessage = "نجوم غير كافية! يلزمك ${frame.costStars} نجمة."
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Gold),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("فتح (${frame.costStars} 🌟)", color = Navy, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        },
        containerColor = NavyCard,
        shape = RoundedCornerShape(20.dp)
    )
}
