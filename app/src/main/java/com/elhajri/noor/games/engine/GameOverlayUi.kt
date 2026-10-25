package com.elhajri.noor.games.engine
import com.elhajri.noor.settings.toArabicDigits

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.audio.SfxManager
import com.elhajri.noor.settings.t
import com.elhajri.noor.settings.toArabicDigits
import com.elhajri.noor.theme.NoorThemeState
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.TextMain
import com.elhajri.noor.ui.TextSecondaryLinen
import com.elhajri.noor.ui.noorGlassCard

/**
 * Standard top header bar for NOOR Canvas interactive games.
 */
@Composable
fun GameHeaderBar(
    title: String,
    level: Int,
    score: Int,
    lives: Int = -1,
    timeRemainingMs: Long = -1L,
    onBack: () -> Unit,
    onPause: () -> Unit
) {
    val theme = NoorThemeState.active
    val context = LocalContext.current

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .noorGlassCard(cornerRadius = 16.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = {
                    SfxManager.playTap(context)
                    onBack()
                }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = theme.accent
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = title,
                    color = theme.textPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                if (level > 0) {
                    Text(
                        text = t("المرحلة ${level.toArabicDigits()}", "Level $level"),
                        color = theme.accent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Score Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(theme.accent.copy(alpha = 0.15f))
                        .border(1.dp, theme.accent.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = theme.accent, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = score.toArabicDigits(),
                            color = theme.textPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Lives or Timer
                if (lives >= 0) {
                    Spacer(Modifier.width(8.dp))
                    Row {
                        repeat(lives.coerceAtMost(5)) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = Color(0xFFFF5252),
                                modifier = Modifier.size(16.dp).padding(horizontal = 1.dp)
                            )
                        }
                    }
                } else if (timeRemainingMs >= 0L) {
                    Spacer(Modifier.width(8.dp))
                    val sec = (timeRemainingMs / 1000).toInt()
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF37474F))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${sec.toArabicDigits()}s",
                            color = if (sec <= 5) Color(0xFFFF5252) else theme.textPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        SfxManager.playTap(context)
                        onPause()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Pause,
                        contentDescription = "Pause",
                        tint = theme.textPrimary
                    )
                }
            }
        }
    }
}

/**
 * Pause Overlay
 */
@Composable
fun PauseOverlay(
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onQuit: () -> Unit
) {
    val theme = NoorThemeState.active
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .noorGlassCard(cornerRadius = 20.dp)
                .border(1.dp, theme.accent.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = t("اللعبة متوقفة مؤقتاً", "Game Paused"),
                color = theme.accent,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(20.dp))

            Button(
                onClick = onResume,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = theme.accent),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
                Spacer(Modifier.width(8.dp))
                Text(t("استئناف", "Resume"), color = Color.Black, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(10.dp))
            OutlinedButton(
                onClick = onRestart,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, tint = theme.textPrimary)
                Spacer(Modifier.width(8.dp))
                Text(t("إعادة المحاولة", "Restart"), color = theme.textPrimary)
            }

            Spacer(Modifier.height(10.dp))
            TextButton(
                onClick = onQuit,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(t("الخروج إلى القائمة", "Quit to Hub"), color = theme.textSecondary)
            }
        }
    }
}

/**
 * Game Win Overlay
 */
@Composable
fun GameWinOverlay(
    score: Int,
    stars: Int,
    onNextLevel: (() -> Unit)? = null,
    onRestart: () -> Unit,
    onQuit: () -> Unit
) {
    val theme = NoorThemeState.active

    val starScale1 = remember { Animatable(0f) }
    val starScale2 = remember { Animatable(0f) }
    val starScale3 = remember { Animatable(0f) }

    LaunchedEffect(stars) {
        if (stars >= 1) starScale1.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
        if (stars >= 2) starScale2.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
        if (stars >= 3) starScale3.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .noorGlassCard(cornerRadius = 24.dp)
                .border(1.5.dp, theme.accent.copy(alpha = 0.6f), RoundedCornerShape(24.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = t("🎉 أحسنت! انتصار مبارك", "🎉 Level Completed! Blessed Victory"),
                color = theme.accent,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(12.dp))

            // Stars row
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val scales = listOf(starScale1.value, starScale2.value, starScale3.value)
                for (s in 1..3) {
                    val active = s <= stars
                    Box(
                        modifier = Modifier
                            .scale(if (active) scales[s - 1] else 1.0f)
                            .padding(6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (active) theme.accent else Color.Gray.copy(alpha = 0.3f),
                            modifier = Modifier.size(44.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Text(
                text = t("النقاط المحققة: ${score.toArabicDigits()}", "Score Earned: $score"),
                color = theme.textPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(20.dp))

            if (onNextLevel != null) {
                Button(
                    onClick = onNextLevel,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = theme.accent),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(t("المرحلة التالية ➔", "Next Level ➔"), color = Color.Black, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(10.dp))
            }

            OutlinedButton(
                onClick = onRestart,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(t("لعب مرة أخرى", "Replay Level"), color = theme.textPrimary)
            }

            Spacer(Modifier.height(8.dp))
            TextButton(
                onClick = onQuit,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(t("العودة للألعاب", "Return to Hub"), color = theme.textSecondary)
            }
        }
    }
}

/**
 * Game Lose Overlay
 */
@Composable
fun GameLoseOverlay(
    score: Int,
    onRestart: () -> Unit,
    onQuit: () -> Unit
) {
    val theme = NoorThemeState.active
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .noorGlassCard(cornerRadius = 24.dp)
                .border(1.dp, Color(0xFFFF5252).copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.SentimentDissatisfied,
                contentDescription = null,
                tint = Color(0xFFFF5252),
                modifier = Modifier.size(54.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = t("حاول مرة أخرى!", "Try Again!"),
                color = Color(0xFFFF5252),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = t("النقاط: ${score.toArabicDigits()}", "Score: $score"),
                color = theme.textSecondary,
                fontSize = 14.sp
            )

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = onRestart,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = theme.accent),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.Black)
                Spacer(Modifier.width(8.dp))
                Text(t("إعادة المحاولة", "Try Again"), color = Color.Black, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(8.dp))
            TextButton(
                onClick = onQuit,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(t("العودة للألعاب", "Return to Hub"), color = theme.textSecondary)
            }
        }
    }
}
