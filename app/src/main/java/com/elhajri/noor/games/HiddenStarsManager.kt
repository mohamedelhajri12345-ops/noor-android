package com.elhajri.noor.games

import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.Navy

/**
 * HiddenStarsManager: Manages secret bonus stars hidden inside game stages and levels.
 * Explorers can find secret stars via corner icons or multi-tap sequences to earn +5 bonus stars.
 */
object HiddenStarsManager {

    private const val PREFS_NAME = "noor_hidden_stars_v1"
    private const val BONUS_STARS_PER_SECRET = 5

    private fun prefs(context: Context): SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private fun makeKey(gameId: String, stageIdx: Int, starId: String): String =
        "hidden_${gameId}_${stageIdx}_$starId"

    fun isStarClaimed(context: Context, gameId: String, stageIdx: Int, starId: String): Boolean {
        return prefs(context).getBoolean(makeKey(gameId, stageIdx, starId), false)
    }

    /**
     * Claim secret hidden star if not already claimed.
     * Returns true if newly claimed (+5 stars credited), false if already claimed.
     */
    fun claimHiddenStar(
        context: Context,
        gameId: String,
        stageIdx: Int,
        starId: String,
        bonusAmount: Int = BONUS_STARS_PER_SECRET
    ): Boolean {
        val key = makeKey(gameId, stageIdx, starId)
        if (prefs(context).getBoolean(key, false)) return false

        prefs(context).edit().putBoolean(key, true).apply()
        StarEconomyStore.addStars(context, bonusAmount)
        return true
    }
}

/**
 * Interactive Composable for secret corner hidden stars inside game screens.
 */
@Composable
fun HiddenStarSparkleIcon(
    gameId: String,
    stageIdx: Int = 0,
    starId: String = "secret_1",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var isClaimed by remember { mutableStateOf(HiddenStarsManager.isStarClaimed(context, gameId, stageIdx, starId)) }

    val infiniteTransition = rememberInfiniteTransition(label = "SparkleAnimation")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Scale"
    )

    if (!isClaimed) {
        Box(
            modifier = modifier
                .scale(scale)
                .size(28.dp)
                .clip(CircleShape)
                .background(Gold.copy(alpha = 0.25f))
                .border(1.dp, Gold, CircleShape)
                .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    if (HiddenStarsManager.claimHiddenStar(context, gameId, stageIdx, starId)) {
                        isClaimed = true
                        Toast
                            .makeText(
                                context,
                                "✨ رائع! عثرت على نجمة مخفية سرية! (+٥ نجوم 🌟)",
                                Toast.LENGTH_SHORT
                            )
                            .show()
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = "نجمة سرية مخفية",
                tint = Gold,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/**
 * Secret Multi-Tap Sequence Component wrapper.
 * Unlocks a secret bonus star when user rapidly taps an element (e.g. game title) N times.
 */
@Composable
fun SecretTapSequenceWrapper(
    gameId: String,
    stageIdx: Int = 0,
    starId: String = "tap_seq",
    requiredTaps: Int = 3,
    content: @Composable (onTap: () -> Unit) -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var tapCount by remember { mutableIntStateOf(0) }
    var lastTapTime by remember { mutableLongStateOf(0L) }

    val onTap: () -> Unit = {
        val now = System.currentTimeMillis()
        if (now - lastTapTime < 1200L) {
            tapCount++
        } else {
            tapCount = 1
        }
        lastTapTime = now

        if (tapCount >= requiredTaps) {
            tapCount = 0
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            if (HiddenStarsManager.claimHiddenStar(context, gameId, stageIdx, starId)) {
                Toast.makeText(
                    context,
                    "👑 كشفت الشفرة السرية! (+٥ نجوم بونوس 🌟)",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    content(onTap)
}
