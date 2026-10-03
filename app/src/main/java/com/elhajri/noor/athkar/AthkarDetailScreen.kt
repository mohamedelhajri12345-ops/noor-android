package com.elhajri.noor.athkar

import com.elhajri.noor.ui.themeScreenBackground
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.data.DataLoader
import com.elhajri.noor.data.Dhikr
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.Navy
import com.elhajri.noor.ui.NavyCard
import com.elhajri.noor.ui.NavyLight
import com.elhajri.noor.ui.TextMain
import kotlinx.coroutines.launch
import com.elhajri.noor.ui.NoorGradients

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

    LaunchedEffect(categoryId) {
        dhikrs.indices.forEach { i ->
            counts[i] = sp.getInt("athkar_count_${categoryId}_$i", 0)
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
                        listState.animateScrollToItem(nextUndoneIndex)
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .themeScreenBackground()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
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
                    contentDescription = "رجوع",
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
                    contentDescription = "إعادة",
                    tint = GoldSoft
                )
            }
        }

        LazyColumn(
            state = listState,
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(dhikrs) { index, item ->
                val currentCount = counts[index] ?: 0
                val isDone = currentCount >= item.count
                val progress = if (item.count > 0) (currentCount.toFloat() / item.count.toFloat()).coerceIn(0f, 1f) else 1f

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !isDone) { incrementDhikr(index) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDone) NavyCard.copy(alpha = 0.6f) else NavyCard
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
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

                                    drawArc(
                                        color = Gold.copy(alpha = 0.2f),
                                        startAngle = 0f,
                                        sweepAngle = 360f,
                                        useCenter = false,
                                        topLeft = topLeft,
                                        size = arcSize,
                                        style = Stroke(width = strokeWidth)
                                    )

                                    if (progress > 0f) {
                                        drawArc(
                                            color = Gold,
                                            startAngle = -90f,
                                            sweepAngle = progress * 360f,
                                            useCenter = false,
                                            topLeft = topLeft,
                                            size = arcSize,
                                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                        )
                                    }
                                }

                                if (isDone) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "تم",
                                        tint = Gold,
                                        modifier = Modifier.size(24.dp)
                                    )
                                } else {
                                    Text(
                                        text = "${toArabicNumber(currentCount)}/${toArabicNumber(item.count)}",
                                        color = Gold,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        if (item.count > 1 && !isDone) {
                            Spacer(modifier = Modifier.height(10.dp))
                            LinearProgressIndicator(
                                progress = progress,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(CircleShape),
                                color = Gold,
                                trackColor = NavyLight
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCelebrationDialog) {
        AlertDialog(
            onDismissRequest = { showCelebrationDialog = false },
            containerColor = NavyCard,
            icon = {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Gold,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "تقبّل الله طاعتكم",
                    color = Gold,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = "لقد أكملت جميع $title بنجاح.",
                    color = TextMain,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = { showCelebrationDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Navy),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("تم", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
