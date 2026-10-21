package com.elhajri.noor.athkar
import com.elhajri.noor.settings.t

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.audio.player.AthkarPlayerManager
import com.elhajri.noor.data.DataLoader
import com.elhajri.noor.data.Dhikr
import com.elhajri.noor.theme.noorGlassCard
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.Navy
import com.elhajri.noor.ui.NavyCard
import com.elhajri.noor.ui.NavyLight
import com.elhajri.noor.ui.TextMain
import com.elhajri.noor.ui.themeScreenBackground
import kotlinx.coroutines.launch

@Composable
fun AthkarDetailScreen(
    categoryId: String,
    title: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val sp = remember { context.getSharedPreferences("noor_prefs", Context.MODE_PRIVATE) }
    val dhikrs = remember(categoryId) { DataLoader.athkar(context, categoryId) }

    val counts = remember { mutableStateMapOf<Int, Int>() }
    var showCelebrationDialog by remember { mutableStateOf(false) }

    val playerState by AthkarPlayerManager.athkarState.collectAsState()

    val isCategoryActive = playerState.categoryId == categoryId
    val activeIndex = if (isCategoryActive) playerState.activeIndex else -1

    LaunchedEffect(categoryId) {
        dhikrs.indices.forEach { i ->
            counts[i] = sp.getInt("athkar_count_${categoryId}_$i", 0)
        }
    }

    // Auto-scroll list to active playing dhikr during sequential playback
    LaunchedEffect(activeIndex) {
        if (activeIndex in dhikrs.indices) {
            coroutineScope.launch {
                listState.animateScrollToItem(activeIndex + 1)
            }
        }
    }

    fun triggerVibration() {
        try {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            vibrator?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    it.vibrate(VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    it.vibrate(40)
                }
            }
        } catch (_: Exception) {}
    }

    fun resetAll() {
        dhikrs.indices.forEach { i ->
            counts[i] = 0
            sp.edit().remove("athkar_count_${categoryId}_$i").apply()
        }
        sp.edit().remove("athkar_completed_$categoryId").apply()
        Toast.makeText(context, "تمت إعادة ضبط الأذكار", Toast.LENGTH_SHORT).show()
    }

    fun incrementDhikr(index: Int) {
        val dhikr = dhikrs[index]
        val current = counts[index] ?: 0
        if (current < dhikr.count) {
            val next = current + 1
            counts[index] = next
            sp.edit().putInt("athkar_count_${categoryId}_$index", next).apply()
            triggerVibration()

            val isAllCompleted = dhikrs.indices.all { idx ->
                val c = if (idx == index) next else (counts[idx] ?: 0)
                c >= dhikrs[idx].count
            }

            if (isAllCompleted) {
                sp.edit().putBoolean("athkar_completed_$categoryId", true).apply()
                showCelebrationDialog = true
            } else if (next >= dhikr.count) {
                val nextUndoneIndex = dhikrs.indices.firstOrNull { idx ->
                    val c = if (idx == index) next else (counts[idx] ?: 0)
                    c < dhikrs[idx].count
                }
                if (nextUndoneIndex != null) {
                    coroutineScope.launch {
                        listState.animateScrollToItem(nextUndoneIndex + 1)
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .themeScreenBackground()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(NavyCard)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = t("رجوع", "Back"),
                        tint = Gold
                    )
                }

                Text(
                    text = title,
                    color = Gold,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = { resetAll() },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(NavyCard)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = t("إعادة الضبط", "Reset"),
                        tint = GoldSoft
                    )
                }
            }

            LazyColumn(
                state = listState,
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(bottom = if (isCategoryActive) 130.dp else 24.dp)
            ) {
                // Sequential Reading Mode Hero Card (t("قراءة متتابعة", "Sequential Audio"))
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .noorGlassCard(),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        shape = RoundedCornerShape(18.dp)
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
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Headphones,
                                        contentDescription = null,
                                        tint = Gold,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = t("وضع القراءة المتتابعة", "Sequential Audio Mode"),
                                        color = Gold,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                if (isCategoryActive && playerState.isPlaying) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Gold.copy(alpha = 0.2f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = t("جاري التشغيل...", "Playing..."),
                                            color = Gold,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = t("استمع وتتبع الأذكار بصوت خاشع ومؤثر مع الانتقال التلقائي بين الأذكار", "Listen and follow adhkar with auto-advance"),
                                color = GoldSoft.copy(alpha = 0.8f),
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                // Previous Dhikr
                                IconButton(
                                    onClick = { AthkarPlayerManager.previousDhikr(context) },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(NavyCard)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SkipNext, // RTL arrow direction
                                        contentDescription = t("الذكر السابق", "Previous Dhikr"),
                                        tint = Gold
                                    )
                                }

                                // Play / Pause Button
                                Button(
                                    onClick = {
                                        if (isCategoryActive) {
                                            AthkarPlayerManager.togglePlayPause(context)
                                        } else {
                                            AthkarPlayerManager.startCategory(
                                                context = context,
                                                categoryId = categoryId,
                                                categoryTitle = title,
                                                dhikrs = dhikrs,
                                                startIndex = 0
                                            )
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Navy),
                                    shape = RoundedCornerShape(14.dp),
                                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isCategoryActive && playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = if (isCategoryActive && playerState.isPlaying) t("إيقاف مؤقت", "Pause") else t("ابدأ الاستماع", "Start Listening"),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isCategoryActive && playerState.isPlaying) t("إيقاف مؤقت", "Pause") else t("تشغيل متتابع", "Sequential Play"),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }

                                // Next Dhikr
                                IconButton(
                                    onClick = { AthkarPlayerManager.nextDhikr(context) },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(NavyCard)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SkipPrevious, // RTL arrow direction
                                        contentDescription = t("الذكر التالي", "Next Dhikr"),
                                        tint = Gold
                                    )
                                }
                            }
                        }
                    }
                }

                // Dhikrs List
                itemsIndexed(dhikrs) { index, item ->
                    val currentCount = counts[index] ?: 0
                    val isDone = currentCount >= item.count
                    val isCurrentPlaying = isCategoryActive && playerState.activeIndex == index && playerState.isPlaying

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (isCategoryActive && playerState.activeIndex == index) {
                                    AthkarPlayerManager.togglePlayPause(context)
                                } else {
                                    AthkarPlayerManager.startCategory(
                                        context = context,
                                        categoryId = categoryId,
                                        categoryTitle = title,
                                        dhikrs = dhikrs,
                                        startIndex = index
                                    )
                                }
                            }
                            .noorGlassCard(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCurrentPlaying) Gold.copy(alpha = 0.12f) else Color.Transparent
                        ),
                        shape = RoundedCornerShape(18.dp)
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
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Gold.copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "ذكر ${toArabicNumber(index + 1)} من ${toArabicNumber(dhikrs.size)}",
                                        color = Gold,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                if (isCurrentPlaying) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.VolumeUp,
                                            contentDescription = t("مستمر", "Continuous"),
                                            tint = Gold,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = t("جاري القراءة", "Reading"),
                                            color = Gold,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = item.text,
                                color = if (isDone) TextMain.copy(alpha = 0.6f) else TextMain,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 28.sp
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = item.ref,
                                    color = GoldSoft.copy(alpha = 0.7f),
                                    fontSize = 12.sp,
                                    modifier = Modifier.weight(1f)
                                )

                                // Circular Counter / Repeat Indicator
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clickable(enabled = !isDone) { incrementDhikr(index) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        val strokeWidth = 4.dp.toPx()
                                        val radius = (size.minDimension - strokeWidth) / 2
                                        val topLeft = Offset((size.width - radius * 2) / 2, (size.height - radius * 2) / 2)
                                        val arcSize = Size(radius * 2, radius * 2)

                                        val progress = if (item.count > 0) (currentCount.toFloat() / item.count.toFloat()).coerceIn(0f, 1f) else 1f

                                        drawArc(
                                            color = Gold.copy(alpha = 0.2f),
                                            startAngle = 0f,
                                            sweepAngle = 360f,
                                            useCenter = false,
                                            topLeft = topLeft,
                                            size = arcSize,
                                            style = Stroke(width = strokeWidth)
                                        )

                                        drawArc(
                                            color = Gold,
                                            startAngle = -90f,
                                            sweepAngle = 360f * progress,
                                            useCenter = false,
                                            topLeft = topLeft,
                                            size = arcSize,
                                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                        )
                                    }

                                    if (isDone) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = t("مكتمل", "Completed"),
                                            tint = Gold,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    } else {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = toArabicNumber(currentCount),
                                                color = Gold,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "/${toArabicNumber(item.count)}",
                                                color = GoldSoft.copy(alpha = 0.7f),
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Floating Bottom Audio Player Bar during Active Playback
        if (isCategoryActive) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(12.dp)
                    .noorGlassCard(),
                color = Color.Transparent,
                shape = RoundedCornerShape(18.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = playerState.currentDhikrText.take(35) + if (playerState.currentDhikrText.length > 35) "..." else "",
                            color = TextMain,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "الذكر ${toArabicNumber(playerState.activeIndex + 1)} من ${toArabicNumber(dhikrs.size)} • ${playerState.currentRef}",
                            color = GoldSoft,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = { AthkarPlayerManager.togglePlayPause(context) },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Gold)
                        ) {
                            Icon(
                                imageVector = if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (playerState.isPlaying) t("إيقاف مؤقت", "Pause") else "تشغيل",
                                tint = Navy,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        IconButton(
                            onClick = { AthkarPlayerManager.stopAudioMode(context) },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(NavyCard)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = t("إنهاء", "Finish"),
                                tint = GoldSoft,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // Completion Celebration Dialog
        AnimatedVisibility(
            visible = showCelebrationDialog,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            AlertDialog(
                onDismissRequest = { showCelebrationDialog = false },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Gold
                        )
                        Text(
                            text = t("تقبل الله طاعتكم", "May Allah accept your worship"),
                            color = Gold,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                text = {
                    Text(
                        text = "أحسنت! أتممت قراءة $title بنجاح. كتب الله أجركم وجعلكم من الذاكرين الله كثيراً والذاكرات.",
                        color = TextMain,
                        fontSize = 14.sp,
                        lineHeight = 22.sp
                    )
                },
                confirmButton = {
                    TextButton(onClick = { showCelebrationDialog = false }) {
                        Text(text = "تم", color = Gold, fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = Navy,
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}