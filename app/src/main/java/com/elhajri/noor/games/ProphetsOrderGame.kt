package com.elhajri.noor.games

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.Navy
import com.elhajri.noor.ui.NavyCard
import com.elhajri.noor.ui.noorGlassCard
import com.elhajri.noor.ui.themeScreenBackground
import kotlinx.coroutines.delay

private fun Int.toArabicDigits(): String {
    val arDigits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
    val str = this.toString()
    val builder = StringBuilder()
    for (ch in str) {
        if (ch in '0'..'9') builder.append(arDigits[ch - '0']) else builder.append(ch)
    }
    return builder.toString()
}

// 25 prophets in chronological order
val ALL_PROPHETS = listOf(
    "آدم", "إدريس", "نوح", "هود", "صالح",
    "إبراهيم", "لوط", "إسماعيل", "إسحاق", "يعقوب",
    "يوسف", "شعيب", "أيوب", "ذو الكفل", "موسى",
    "هارون", "داود", "سليمان", "إلياس", "اليسع",
    "يونس", "زكريا", "يحيى", "عيسى", "محمد ﷺ"
)

@Composable
fun ProphetsOrderGameScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    // Lock orientation to portrait
    DisposableEffect(Unit) {
        val activity = context as? Activity
        val orig = activity?.requestedOrientation
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        onDispose {
            orig?.let { activity.requestedOrientation = it }
        }
    }

    var progressData by remember { mutableStateOf(NewGameProgress.load(context, "game_prophets_order")) }
    var currentLevel by remember { mutableStateOf<Int?>(null) } // null = level select screen
    var totalScore by remember { mutableStateOf(progressData.score) }
    var highestUnlocked by remember { mutableStateOf(progressData.level) }

    Box(Modifier.fillMaxSize().themeScreenBackground()) {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            // Header bar
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = {
                    if (currentLevel != null) currentLevel = null else onBack()
                }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = Gold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("ترتيب الأنبياء", color = Gold, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("التسلسل الزمني لبواسل التوحيد", color = GoldSoft, fontSize = 12.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Gold, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(totalScore.toArabicDigits(), color = Gold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(12.dp))

            if (currentLevel == null) {
                // Level Selector (25 levels)
                Text("اختر المستوى:", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(25) { idx ->
                        val lvlNum = idx + 1
                        val isUnlocked = lvlNum <= highestUnlocked
                        val isCompleted = lvlNum < highestUnlocked

                        Box(
                            Modifier
                                .height(72.dp)
                                .noorGlassCard(cornerRadius = 14.dp)
                                .clickable(enabled = isUnlocked) { currentLevel = lvlNum },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                if (isUnlocked) {
                                    Text("المستوى", color = GoldSoft, fontSize = 9.sp)
                                    Text(lvlNum.toArabicDigits(), color = Gold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    if (isCompleted) {
                                        Row {
                                            Icon(Icons.Default.Star, contentDescription = null, tint = Gold, modifier = Modifier.size(10.dp))
                                            Icon(Icons.Default.Star, contentDescription = null, tint = Gold, modifier = Modifier.size(10.dp))
                                            Icon(Icons.Default.Star, contentDescription = null, tint = Gold, modifier = Modifier.size(10.dp))
                                        }
                                    }
                                } else {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                                    Text(lvlNum.toArabicDigits(), color = Color.Gray, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            } else {
                // Active Level Gameplay
                val lvl = currentLevel!!
                // Determine target prophets subset for this level
                val count = (3 + (lvl / 2)).coerceAtMost(10)
                val startIdx = ((lvl - 1) % (ALL_PROPHETS.size - count + 1)).coerceAtLeast(0)
                val targetProphets = remember(lvl) { ALL_PROPHETS.subList(startIdx, startIdx + count) }

                var shuffled by remember(lvl) { mutableStateOf(targetProphets.shuffled()) }
                var selected by remember(lvl) { mutableStateOf(listOf<String>()) }
                var wrongProphet by remember(lvl) { mutableStateOf<String?>(null) }
                var isSuccess by remember(lvl) { mutableStateOf(false) }

                LaunchedEffect(wrongProphet) {
                    if (wrongProphet != null) {
                        delay(700)
                        wrongProphet = null
                    }
                }

                Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Card(
                        Modifier.fillMaxWidth().noorGlassCard(cornerRadius = 16.dp).padding(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("المستوى ${lvl.toArabicDigits()}", color = Gold, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text("رتب الأنبياء الـ ${count.toArabicDigits()} بالترتيب الزمني الصحيح", color = GoldSoft, fontSize = 12.sp)
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Selected Timeline Box
                    Text("التسلسل المختار:", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(6.dp))
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .heightIn(min = 60.dp)
                            .noorGlassCard(cornerRadius = 12.dp)
                            .padding(8.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (selected.isEmpty()) {
                            Text("اضغط على الأنبياء من الأسفل لترتيبهم...", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                        } else {
                            Row(Modifier.horizontalScroll(androidx.compose.foundation.rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                selected.forEachIndexed { i, p ->
                                    Box(
                                        Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Gold.copy(alpha = 0.2f))
                                            .border(1.dp, Gold, RoundedCornerShape(10.dp))
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text("${(i + 1).toArabicDigits()}. $p", color = Gold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // Shuffled Buttons Grid
                    Text("الخيارات المتاحة:", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        itemsIndexed(shuffled) { _, prophet ->
                            val isSel = selected.contains(prophet)
                            val isWr = wrongProphet == prophet

                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        when {
                                            isSel -> Gold.copy(alpha = 0.25f)
                                            isWr -> Color.Red.copy(alpha = 0.35f)
                                            else -> NavyCard.copy(alpha = 0.8f)
                                        }
                                    )
                                    .border(
                                        1.dp,
                                        when {
                                            isSel -> Gold
                                            isWr -> Color.Red
                                            else -> Gold.copy(alpha = 0.3f)
                                        },
                                        RoundedCornerShape(14.dp)
                                    )
                                    .clickable(enabled = !isSel && !isSuccess) {
                                        val expectedIdx = selected.size
                                        if (prophet == targetProphets[expectedIdx]) {
                                            val nextSelected = selected + prophet
                                            selected = nextSelected
                                            if (nextSelected.size == targetProphets.size) {
                                                isSuccess = true
                                                val scoreEarned = count * 20
                                                totalScore += scoreEarned
                                                if (lvl >= highestUnlocked) highestUnlocked = lvl + 1
                                                NewGameProgress.save(
                                                    context,
                                                    gameId = "game_prophets_order",
                                                    level = highestUnlocked,
                                                    score = totalScore,
                                                    stars = highestUnlocked * 3
                                                )
                                            }
                                        } else {
                                            wrongProphet = prophet
                                        }
                                    }
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                    if (isSel) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Gold, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                    }
                                    Text(prophet, color = if (isSel) Gold else Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Button(
                            onClick = {
                                selected = emptyList()
                                shuffled = targetProphets.shuffled()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Gold.copy(alpha = 0.15f))
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = Gold)
                            Spacer(Modifier.width(4.dp))
                            Text("إعادة الترتيب", color = Gold, fontSize = 12.sp)
                        }
                    }

                    if (isSuccess) {
                        Spacer(Modifier.height(12.dp))
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .noorGlassCard(cornerRadius = 18.dp)
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Gold, modifier = Modifier.size(40.dp))
                                Spacer(Modifier.height(6.dp))
                                Text("ممتاز! الترتيب الصحيح مكتمل!", color = Gold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(10.dp))
                                Button(
                                    onClick = {
                                        if (lvl < 25) currentLevel = lvl + 1 else currentLevel = null
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Gold)
                                ) {
                                    Text("المستوى التالي", color = Navy, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
