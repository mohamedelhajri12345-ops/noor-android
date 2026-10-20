package com.elhajri.noor.exams

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.games.toArabicDigits
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.NavyCard
import com.elhajri.noor.ui.TextMain
import com.elhajri.noor.ui.noorGlassCard
import com.elhajri.noor.ui.themeScreenBackground

@Composable
fun ExamMapScreen(
    onSelectLevel: (ExamTrack, ExamDifficulty, Int) -> Unit,
    onOpenCertificate: (ExamTrack, ExamDifficulty) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    var selectedTrack by remember { mutableStateOf(ExamTrack.KID) }
    var selectedDifficulty by remember { mutableStateOf(ExamDifficulty.EASY) }
    var showJumpDialog by remember { mutableStateOf(false) }

    val progress = remember(selectedTrack, selectedDifficulty) {
        ExamStorage.loadProgress(context, selectedTrack, selectedDifficulty)
    }

    val totalStars = progress.starsMap.values.sum()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .themeScreenBackground()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "رجوع",
                        tint = Gold
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "الاختبار الديني المعتمد",
                        color = Gold,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "مسار ${selectedTrack.titleAr} - ${selectedDifficulty.titleAr}",
                        color = GoldSoft,
                        fontSize = 12.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = NavyCard,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Gold.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Gold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = totalStars.toArabicDigits(),
                            color = Gold,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Track Switcher Chips (طفل / كبير)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ExamTrack.values().forEach { track ->
                    val isSelected = track == selectedTrack
                    Button(
                        onClick = { selectedTrack = track },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) Gold else NavyCard
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Gold),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "مسار ${track.titleAr}",
                            color = if (isSelected) Color.Black else Gold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Difficulty Tabs (سهل 1000 / متوسط 1000 / صعب 2000)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ExamDifficulty.values().forEach { diff ->
                    val isSelected = diff == selectedDifficulty
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedDifficulty = diff },
                        label = {
                            Text(
                                text = "${diff.titleAr} (${diff.totalLevels.toArabicDigits()})",
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Gold.copy(alpha = 0.3f),
                            selectedLabelColor = Gold,
                            containerColor = NavyCard,
                            labelColor = GoldSoft
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = GoldSoft,
                            selectedBorderColor = Gold
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Jump to level & Certificate Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { showJumpDialog = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Gold.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = Gold, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("انتقال لمستوى", color = Gold, fontSize = 13.sp)
                }

                Button(
                    onClick = { onOpenCertificate(selectedTrack, selectedDifficulty) },
                    modifier = Modifier.weight(1.2f),
                    colors = ButtonDefaults.buttonColors(containerColor = Gold),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CardMembership, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("عرض الشهادة", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Level Grid (Displays up to max levels, virtualized performance)
            val displayLevelsCount = minOf(selectedDifficulty.totalLevels, progress.unlockedLevel + 40)

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(List(displayLevelsCount) { it + 1 }) { _, levelNum ->
                    val isUnlocked = levelNum <= progress.unlockedLevel
                    val stars = progress.starsMap[levelNum] ?: 0

                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .then(
                                if (isUnlocked) {
                                    Modifier
                                        .noorGlassCard(cornerRadius = 18.dp)
                                        .border(1.5.dp, Gold, RoundedCornerShape(18.dp))
                                        .clickable { onSelectLevel(selectedTrack, selectedDifficulty, levelNum) }
                                } else {
                                    Modifier
                                        .noorGlassCard(cornerRadius = 18.dp)
                                        .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            if (isUnlocked) {
                                Text(
                                    text = levelNum.toArabicDigits(),
                                    color = Gold,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(horizontalArrangement = Arrangement.Center) {
                                    repeat(3) { idx ->
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = if (idx < stars) Gold else Color.Gray.copy(alpha = 0.5f),
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "مغلق",
                                    tint = GoldSoft.copy(alpha = 0.4f),
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = levelNum.toArabicDigits(),
                                    color = GoldSoft.copy(alpha = 0.4f),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Jump to Level Dialog
        if (showJumpDialog) {
            var inputLevelText by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showJumpDialog = false },
                title = { Text("الانتقال المباشر إلى مستوى", color = Gold, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text(
                            "ادخل رقم المستوى المراد الوصول إليه (1 حتى ${selectedDifficulty.totalLevels.toArabicDigits()}):",
                            color = TextMain,
                            fontSize = 13.sp
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = inputLevelText,
                            onValueChange = { inputLevelText = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Gold,
                                unfocusedBorderColor = GoldSoft,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val target = inputLevelText.toIntOrNull()
                            if (target != null && target in 1..selectedDifficulty.totalLevels) {
                                onSelectLevel(selectedTrack, selectedDifficulty, target)
                            }
                            showJumpDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Gold)
                    ) {
                        Text("انتقال", color = Color.Black)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showJumpDialog = false }) {
                        Text("إلغاء", color = GoldSoft)
                    }
                },
                containerColor = NavyCard
            )
        }
    }
}
