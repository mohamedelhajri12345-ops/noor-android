package com.elhajri.noor.tasbih
import com.elhajri.noor.settings.t

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.ui.AmiriFamily
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.Navy
import com.elhajri.noor.ui.NavyCard
import com.elhajri.noor.ui.NavyLight
import com.elhajri.noor.ui.TextMain
import com.elhajri.noor.theme.noorGlassCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasbihScreen(onBack: (() -> Unit)? = null) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    val soundPlayer = remember { TasbihSoundPlayer(context) }
    DisposableEffect(Unit) {
        onDispose {
            soundPlayer.release()
        }
    }

    var isMuted by remember { mutableStateOf(false) }

    // State
    var selectedStyleIndex by remember { mutableIntStateOf(TasbihPrefs.getSelectedStyle(context)) }
    var showStylePicker by remember { mutableStateOf(false) }

    var dhikrList by remember {
        mutableStateOf(
            DEFAULT_DHIKR_LIST.map { it.text } + TasbihPrefs.getCustomDhikrList(context)
        )
    }

    var selectedDhikr by remember { mutableStateOf(TasbihPrefs.getSelectedDhikr(context)) }
    var target by remember { mutableIntStateOf(TasbihPrefs.getSelectedTarget(context)) }

    var count by remember { mutableIntStateOf(TasbihPrefs.getDhikrCount(context, selectedDhikr)) }
    var dailyTotal by remember { mutableIntStateOf(TasbihPrefs.getDailyCount(context)) }
    var allTimeTotal by remember { mutableIntStateOf(TasbihPrefs.getTotalAllTime(context)) }

    var showCelebration by remember { mutableStateOf(false) }
    var showCustomDialog by remember { mutableStateOf(false) }
    var customInputText by remember { mutableStateOf("") }

    val currentStyle = TasbihStyle.fromIndex(selectedStyleIndex)

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
                    it.vibrate(VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    it.vibrate(35)
                }
            }
        } catch (_: Exception) {}
    }

    fun handleTap() {
        count++
        dailyTotal++
        allTimeTotal++

        TasbihPrefs.incrementCounters(context, selectedDhikr, 1)

        triggerVibration()
        if (!isMuted) {
            soundPlayer.playTick()
        }

        if (count > 0 && count % target == 0) {
            if (!isMuted) {
                soundPlayer.playSuccess()
            }
            showCelebration = true
        }
    }

    fun resetCount() {
        count = 0
    }

    if (showStylePicker) {
        TasbihStylePickerScreen(
            onSelectStyle = { newIndex ->
                selectedStyleIndex = newIndex
                TasbihPrefs.saveSelectedStyle(context, newIndex)
                showStylePicker = false
            },
            onBack = { showStylePicker = false }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        currentStyle.titleAr,
                        color = Gold,
                        fontWeight = FontWeight.Bold,
                        fontFamily = AmiriFamily,
                        fontSize = 19.sp
                    )
                },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "رجوع", tint = Gold)
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { isMuted = !isMuted }) {
                        Icon(
                            imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                            contentDescription = "الصوت",
                            tint = Gold
                        )
                    }
                    IconButton(onClick = { showStylePicker = true }) {
                        Icon(Icons.Default.Palette, contentDescription = "تغيير النمط", tint = Gold)
                    }
                    IconButton(onClick = { resetCount() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "إعادة ضبط", tint = GoldSoft)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyCard)
            )
        },
        containerColor = Color(0xFF070B14)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Dhikr Presets Horizontal Chips Row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                itemsIndexed(dhikrList) { idx, dhikr ->
                    val isSelected = dhikr == selectedDhikr
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) Gold else NavyCard)
                            .border(0.5.dp, Gold.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                            .clickable {
                                selectedDhikr = dhikr
                                TasbihPrefs.saveSelectedDhikr(context, dhikr)
                                count = TasbihPrefs.getDhikrCount(context, dhikr)
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = dhikr,
                            color = if (isSelected) Navy else TextMain,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }

                item {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(NavyLight)
                            .clickable { showCustomDialog = true }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Gold, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("ذكر خاص", color = Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Target Selector Chips (33 / 99 / 100)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("الهدف: ", color = GoldSoft, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.width(8.dp))

                listOf(33, 99, 100).forEach { tgt ->
                    val isSelected = target == tgt
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) Gold.copy(alpha = 0.25f) else Color.Transparent)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) Gold else GoldSoft.copy(alpha = 0.3f),
                                shape = CircleShape
                            )
                            .clickable {
                                target = tgt
                                TasbihPrefs.saveSelectedTarget(context, tgt)
                            }
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = toArabicNumber(tgt),
                            color = if (isSelected) Gold else GoldSoft,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            // Interactive Style Canvas View according to selectedStyleIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                val rounds = if (target > 0) count / target else 0

                when (TasbihStyle.fromIndex(selectedStyleIndex)) {
                    TasbihStyle.ELECTRONIC -> ElectronicTasbihView(
                        dhikrText = selectedDhikr,
                        count = count,
                        target = target,
                        rounds = rounds,
                        onTap = { handleTap() }
                    )
                    TasbihStyle.THREAD -> ThreadTasbihView(
                        dhikrText = selectedDhikr,
                        count = count,
                        target = target,
                        onTap = { handleTap() }
                    )
                    TasbihStyle.ROTATING -> RotatingBeadsTasbihView(
                        dhikrText = selectedDhikr,
                        count = count,
                        target = target,
                        onTap = { handleTap() }
                    )
                    TasbihStyle.GARDEN -> NightGardenTasbihView(
                        dhikrText = selectedDhikr,
                        count = count,
                        target = target,
                        onTap = { handleTap() }
                    )
                    TasbihStyle.LIGHT_ORB -> LightOrbTasbihView(
                        dhikrText = selectedDhikr,
                        count = count,
                        target = target,
                        onTap = { handleTap() }
                    )
                }
            }
        }
    }

    // Celebration Dialog on completing target
    if (showCelebration) {
        TasbihCelebrationDialog(
            dhikrText = selectedDhikr,
            target = target,
            dailyTotal = dailyTotal,
            allTimeTotal = allTimeTotal,
            onDismiss = { showCelebration = false },
            onReset = { resetCount() }
        )
    }

    // Add Custom Dhikr Dialog
    if (showCustomDialog) {
        AlertDialog(
            onDismissRequest = { showCustomDialog = false },
            title = { Text("إضافة ذكر جديد", color = Gold, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("أدخل نص الذكر الذي ترغب في تسبيحه:", color = Color.White, fontSize = 13.sp)
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = customInputText,
                        onValueChange = { customInputText = it },
                        placeholder = { Text("مثال: سبحان الله وبحمده") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Gold,
                            unfocusedBorderColor = NavyLight,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = customInputText.trim()
                        if (trimmed.isNotEmpty()) {
                            TasbihPrefs.addCustomDhikr(context, trimmed)
                            dhikrList = dhikrList + trimmed
                            selectedDhikr = trimmed
                            TasbihPrefs.saveSelectedDhikr(context, trimmed)
                            count = TasbihPrefs.getDhikrCount(context, trimmed)
                            customInputText = ""
                        }
                        showCustomDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Navy)
                ) {
                    Text("إضافة", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomDialog = false }) {
                    Text("إلغاء", color = GoldSoft)
                }
            },
            containerColor = NavyCard
        )
    }
}
