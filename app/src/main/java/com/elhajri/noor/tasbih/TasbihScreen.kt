package com.elhajri.noor.tasbih

import com.elhajri.noor.ui.themeScreenBackground
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.Navy
import com.elhajri.noor.ui.NavyCard
import com.elhajri.noor.ui.NavyLight
import com.elhajri.noor.ui.TextMain
import com.elhajri.noor.ui.NoorGradients
import androidx.compose.ui.graphics.Color
import com.elhajri.noor.theme.noorGlassCard

private fun toArabicNumber(number: Any): String {
    val map = mapOf('0' to '٠', '1' to '١', '2' to '٢', '3' to '٣', '4' to '٤', '5' to '٥', '6' to '٦', '7' to '٧', '8' to '٨', '9' to '٩')
    return number.toString().map { map[it] ?: it }.joinToString("")
}

@Composable
fun TasbihScreen() {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val sp = remember { context.getSharedPreferences("noor_prefs", Context.MODE_PRIVATE) }

    val presets = remember { DataLoader.tasbihPresets(context) }
    var selectedIdx by remember { mutableIntStateOf(0) }
    val preset = presets.getOrElse(selectedIdx) { presets[0] }

    var count by remember(selectedIdx) { mutableIntStateOf(0) }
    var total by remember { mutableIntStateOf(sp.getInt("tasbih_total", 0)) }
    var showCelebration by remember { mutableStateOf(false) }

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
                    it.vibrate(VibrationEffect.createOneShot(45, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    it.vibrate(45)
                }
            }
        } catch (_: Exception) {}
    }

    fun increment() {
        val nextCount = count + 1
        val nextTotal = total + 1
        count = nextCount
        total = nextTotal
        sp.edit().putInt("tasbih_total", nextTotal).apply()

        triggerVibration()
        if (nextCount > 0 && nextCount % preset.target == 0) {
            showCelebration = true
        } else {
        }
    }

    fun reset() {
        count = 0
    }

    val progress = if (preset.target > 0) ((count % preset.target).toFloat() / preset.target.toFloat()).coerceIn(0f, 1f) else 0f
    val rounds = if (preset.target > 0) count / preset.target else 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .themeScreenBackground()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "المسبحة الإلكترونية",
            color = Gold,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Presets chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
        ) {
            itemsIndexed(presets) { idx, p ->
                val isSelected = idx == selectedIdx
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) Gold else NavyCard)
                        .clickable {
                            selectedIdx = idx
                            count = 0
                        }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = p.text,
                        color = if (isSelected) Navy else TextMain,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        // Main Counter Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp).noorGlassCard(),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = preset.text,
                    color = Gold,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // Big Circular Counter Canvas
                Box(
                    modifier = Modifier
                        .size(190.dp)
                        .clickable { increment() },
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 10.dp.toPx()
                        val radius = (size.minDimension - strokeWidth) / 2
                        val topLeft = Offset((size.width - radius * 2) / 2, (size.height - radius * 2) / 2)
                        val arcSize = Size(radius * 2, radius * 2)

                        drawArc(
                            color = NavyLight,
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

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = toArabicNumber(count),
                            color = TextMain,
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "من ${toArabicNumber(preset.target)}",
                            color = GoldSoft,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "الجولات: ${toArabicNumber(rounds)}  ·  المجموع اليومي: ${toArabicNumber(total)}",
                    color = GoldSoft.copy(alpha = 0.8f),
                    fontSize = 13.sp
                )
            }
        }

        // Action Buttons
        Button(
            onClick = { increment() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Navy),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "اضغط للعدّ",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = { reset() },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldSoft),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "إعادة العدّ",
                fontSize = 14.sp
            )
        }
    }

    if (showCelebration) {
        AlertDialog(
            onDismissRequest = { showCelebration = false },
            containerColor = NavyCard,
            icon = {
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = Gold,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "أحسنت!",
                    color = Gold,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = "أتممت هدف ${preset.text} (${toArabicNumber(preset.target)} مرة)",
                    color = TextMain,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = { showCelebration = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Navy),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("متابعة", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
