package com.elhajri.noor.games

import com.elhajri.noor.settings.t

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.Navy
import com.elhajri.noor.ui.TextMain
import com.elhajri.noor.ui.TextSecondaryLinen
import com.elhajri.noor.ui.noorGlassCard
import kotlin.random.Random

/**
 * Modifier extension to apply a wrong-answer horizontal shake effect.
 */
fun Modifier.wrongAnswerShake(shakeTrigger: Long): Modifier = this.composed {
    val context = LocalContext.current
    val shakeOffset = remember { Animatable(0f) }
    LaunchedEffect(shakeTrigger) {
        if (shakeTrigger > 0L) {
            com.elhajri.noor.audio.SfxManager.playWrong(context)
            shakeOffset.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = 400
                    0f at 0
                    -16f at 50
                    16f at 100
                    -12f at 150
                    12f at 200
                    -8f at 250
                    8f at 300
                    -4f at 350
                    0f at 400
                }
            )
        }
    }
    this.offset(x = shakeOffset.value.dp)
}

/**
 * Particle data class for gentle confetti overlay in GameFx.
 */
private data class GameFxConfettiParticle(
    val xRatio: Float,
    val initialY: Float,
    val speed: Float,
    val size: Float,
    val color: Color
)

/**
 * Canvas-based gentle confetti and star burst overlay.
 */
@Composable
fun GentleConfettiOverlay(modifier: Modifier = Modifier) {
    val particles = remember {
        val colors = listOf(
            Gold,
            GoldSoft,
            Color(0xFF8ECFC4),
            Color(0xFF6FBF8B),
            Color(0xFFD4B85C),
            Color(0xFFFFFFFF)
        )
        List(40) {
            GameFxConfettiParticle(
                xRatio = Random.nextFloat(),
                initialY = Random.nextFloat(),
                speed = 0.2f + Random.nextFloat() * 0.4f,
                size = 4f + Random.nextFloat() * 8f,
                color = colors[Random.nextInt(colors.size)]
            )
        }
    }

    val transition = rememberInfiniteTransition(label = "ConfettiTransition")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ConfettiProgress"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        particles.forEach { p ->
            val y = ((p.initialY + progress * p.speed) % 1f) * height
            val x = p.xRatio * width + Math.sin((progress * 2 * Math.PI + p.initialY * 10).toDouble()).toFloat() * 15f
            drawCircle(
                color = p.color.copy(alpha = 0.85f),
                radius = p.size,
                center = Offset(x, y)
            )
        }
    }
}

/**
 * Shared Win Celebration composable with star burst, gentle confetti overlay,
 * one-tap WhatsApp achievement card sharing, and dark-green glass + gold design.
 */
@Composable
fun WinCelebrationOverlay(
    stageNumber: Int,
    totalStages: Int,
    stars: Int,
    stageTitle: String = "",
    onNextStage: (() -> Unit)? = null,
    onReplayStage: () -> Unit,
    onStageMap: () -> Unit
) {
    val context = LocalContext.current
    val starScale1 = remember { Animatable(0f) }
    val starScale2 = remember { Animatable(0f) }
    val starScale3 = remember { Animatable(0f) }

    LaunchedEffect(stars) {
        if (stars > 0) {
            com.elhajri.noor.audio.SfxManager.playCelebration(context)
            // Reward XP and stars
            XpRankStore.rewardLevelCompletion(context, stageTitle.ifBlank { "game_level" }, stageNumber, stars)
            // Check daily challenge completion
            DailyChallengeManager.completeDailyChallenge(context)
        }
        starScale1.snapTo(0f)
        starScale2.snapTo(0f)
        starScale3.snapTo(0f)

        if (stars >= 1) {
            starScale1.animateTo(1.3f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
            starScale1.animateTo(1.0f, tween(100))
        }
        if (stars >= 2) {
            starScale2.animateTo(1.3f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
            starScale2.animateTo(1.0f, tween(100))
        }
        if (stars >= 3) {
            starScale3.animateTo(1.3f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
            starScale3.animateTo(1.0f, tween(100))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF041810).copy(alpha = 0.82f)),
        contentAlignment = Alignment.Center
    ) {
        GentleConfettiOverlay()

        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .noorGlassCard(cornerRadius = 24.dp)
                .border(1.5.dp, Gold.copy(alpha = 0.6f), RoundedCornerShape(24.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = t("🎉 أحسنت! إنجاز مبارك", "🎉 Well done! Blessed achievement"),
                color = Gold,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            if (stageNumber > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = t(t(t("أتممت المرحلة ${stageNumber.toArabicDigits()}", "Completed Stage ${stageNumber.toString()}"), "Completed Stage ${stageNumber.toString()}"), "Completed Stage ${stageNumber.toString()}") + if (totalStages > 0) t(t(t(" من ${totalStages.toArabicDigits()}", " of ${totalStages.toString()}"), " of ${totalStages.toString()}"), " of ${totalStages.toString()}") else "",
                    color = TextMain,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
            }

            if (stageTitle.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stageTitle,
                    color = TextSecondaryLinen,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Gold Star Burst Row
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val scales = listOf(starScale1.value, starScale2.value, starScale3.value)
                for (s in 1..3) {
                    val isActive = s <= stars
                    Box(
                        modifier = Modifier
                            .scale(if (isActive) scales[s - 1] else 1.0f)
                            .padding(6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isActive) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Gold.copy(alpha = 0.2f))
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (isActive) Gold else GoldSoft.copy(alpha = 0.25f),
                            modifier = Modifier.size(42.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 1-Tap Achievement Share Button
            Button(
                onClick = {
                    AchievementCardGenerator.shareAchievementCard(
                        context = context,
                        gameTitle = stageTitle.ifBlank { t(t(t("ألعاب نور الإيمانية", "NOOR Faith Games"), "NOOR Faith Games"), "NOOR Faith Games") },
                        levelNumber = stageNumber,
                        starsEarned = stars,
                        scoreGained = 50,
                        xpGained = 20 + (stars * 5)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)), // WhatsApp Green
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = t("مشاركة الإنجاز إلى واتساب 📲", "Share Achievement to WhatsApp 📲"),
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Navigation Actions
            if (onNextStage != null && (stageNumber < totalStages || totalStages == 0)) {
                Button(
                    onClick = onNextStage,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Gold),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = t("المرحلة التالية", "Next Level"),
                        color = Navy,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onReplayStage,
                    modifier = Modifier.weight(1f),
                    border = BorderStroke(1.dp, Gold),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = Gold,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = t("إعادة", "Replay"),
                        color = Gold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = onStageMap,
                    modifier = Modifier.weight(1f),
                    border = BorderStroke(1.dp, GoldSoft),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = t("المراحل", "Levels Map"),
                        color = GoldSoft,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

/**
 * Animated Flame Streak counter header element.
 */
@Composable
fun StreakCounterFlameHeader(
    streak: Int,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "FlameTransition")
    val flameScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "FlameScale"
    )

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1B3022))
            .border(1.dp, Gold.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.LocalFireDepartment,
            contentDescription = t(t(t("سلسلة الإنجاز", "Streak of Achievement"), "Streak of Achievement"), "Streak of Achievement"),
            tint = Color(0xFFFF9800),
            modifier = Modifier
                .size(20.dp)
                .scale(flameScale)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "🔥 ${streak.toArabicDigits()}",
            color = Gold,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

