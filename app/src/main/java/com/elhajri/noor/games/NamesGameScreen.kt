package com.elhajri.noor.games

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Psychology
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
import com.elhajri.noor.data.DataLoader
import com.elhajri.noor.data.NameOfAllah
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.Navy
import com.elhajri.noor.ui.NavyCard
import com.elhajri.noor.ui.NavyLight
import kotlinx.coroutines.delay
import org.json.JSONArray
import com.elhajri.noor.ui.NoorGradients

private const val PREFS_KEY = "nur_names_learned"

private fun toArabicDigits(number: Any): String {
    val str = number.toString()
    val arabicDigits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
    return str.map { ch ->
        if (ch in '0'..'9') arabicDigits[ch - '0'] else ch
    }.joinToString("")
}

private fun loadLearnedSet(context: Context): Set<Int> {
    val sp = context.getSharedPreferences("noor_prefs", Context.MODE_PRIVATE)
    val jsonStr = sp.getString(PREFS_KEY, "[]") ?: "[]"
    val set = mutableSetOf<Int>()
    try {
        val arr = JSONArray(jsonStr)
        for (i in 0 until arr.length()) {
            set.add(arr.getInt(i))
        }
    } catch (_: Exception) {}
    return set
}

private fun saveLearnedSet(context: Context, learned: Set<Int>) {
    val sp = context.getSharedPreferences("noor_prefs", Context.MODE_PRIVATE)
    val arr = JSONArray()
    learned.forEach { arr.put(it) }
    sp.edit().putString(PREFS_KEY, arr.toString()).apply()
}

@Composable
fun NamesGameScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val namesList = remember { DataLoader.namesOfAllah(context) }
    var mode by remember { mutableStateOf("browse") } // "browse" or "quiz"
    var learnedSet by remember { mutableStateOf(loadLearnedSet(context)) }

    val toggleLearned = { idx: Int ->
        val updated = if (idx in learnedSet) learnedSet - idx else learnedSet + idx
        learnedSet = updated
        saveLearnedSet(context, updated)
    }

    if (mode == "quiz") {
        NamesQuizMode(
            namesList = namesList,
            onBack = { mode = "browse" }
        )
    } else {
        NamesBrowseMode(
            namesList = namesList,
            learnedSet = learnedSet,
            onToggleLearned = toggleLearned,
            onStartQuiz = { mode = "quiz" },
            onBack = onBack
        )
    }
}

@Composable
private fun NamesBrowseMode(
    namesList: List<NameOfAllah>,
    learnedSet: Set<Int>,
    onToggleLearned: (Int) -> Unit,
    onStartQuiz: () -> Unit,
    onBack: () -> Unit
) {
    val percentage = if (namesList.isNotEmpty()) (learnedSet.size * 100) / namesList.size else 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NoorGradients.ScreenBackground)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = Gold)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("الأسماء الحسنى", style = MaterialTheme.typography.titleLarge, color = Gold, fontWeight = FontWeight.Bold)
                Text("${toArabicDigits(namesList.size)} اسم من أسماء الله الحسنى", style = MaterialTheme.typography.bodySmall, color = GoldSoft)
            }
            Spacer(Modifier.width(48.dp))
        }

        Spacer(Modifier.height(12.dp))

        // Progress card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF070B14)Card),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("الأسماء المتعلمة", style = MaterialTheme.typography.titleSmall, color = Color.White)
                    Text("${toArabicDigits(learnedSet.size)}/${toArabicDigits(namesList.size)}", style = MaterialTheme.typography.titleSmall, color = Gold)
                }
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { percentage / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = Gold,
                    trackColor = NavyLight,
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onStartQuiz,
                    colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Navy),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Psychology, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("ابدأ اختبار الأسماء", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            itemsIndexed(namesList) { i, item ->
                val isLearned = i in learnedSet
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isLearned) NavyCard else NavyLight
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Gold.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(toArabicDigits(i + 1), style = MaterialTheme.typography.titleSmall, color = Gold)
                            }
                            Column {
                                Text(item.name, style = MaterialTheme.typography.titleMedium, color = Gold, fontWeight = FontWeight.Bold)
                                Text(item.meaning, style = MaterialTheme.typography.bodySmall, color = GoldSoft.copy(alpha = 0.8f))
                            }
                        }

                        IconButton(
                            onClick = { onToggleLearned(i) },
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    if (isLearned) Gold.copy(alpha = 0.2f) else Navy,
                                    CircleShape
                                )
                        ) {
                            if (isLearned) {
                                Icon(Icons.Default.Check, contentDescription = "تعلمتها", tint = Gold, modifier = Modifier.size(20.dp))
                            } else {
                                Icon(Icons.Default.AutoAwesome, contentDescription = "لم أتعلمها", tint = GoldSoft.copy(alpha = 0.5f), modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NamesQuizMode(
    namesList: List<NameOfAllah>,
    onBack: () -> Unit
) {
    val pool = remember { namesList.shuffled().take(20) }
    var idx by remember { mutableIntStateOf(0) }
    var selectedName by remember { mutableStateOf<String?>(null) }
    var score by remember { mutableIntStateOf(0) }
    var finished by remember { mutableStateOf(false) }

    val currentQ = pool.getOrNull(idx)

    val currentOptions = remember(idx) {
        if (currentQ != null) {
            val wrong = namesList.filter { it.name != currentQ.name }.shuffled().take(3)
            (listOf(currentQ) + wrong).shuffled()
        } else emptyList()
    }

    LaunchedEffect(selectedName) {
        if (selectedName != null) {
            delay(1200)
            if (idx + 1 >= pool.size) {
                finished = true
            } else {
                idx++
                selectedName = null
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NoorGradients.ScreenBackground)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = Gold)
            }
            Text("اختبار الأسماء الحسنى", style = MaterialTheme.typography.titleLarge, color = Gold)
            Spacer(Modifier.width(48.dp))
        }

        if (finished) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Gold, modifier = Modifier.size(64.dp))
                Spacer(Modifier.height(16.dp))
                Text("انتهى الاختبار!", style = MaterialTheme.typography.headlineMedium, color = Gold)
                Spacer(Modifier.height(8.dp))
                Text("${toArabicDigits(score)}/${toArabicDigits(pool.size)}", style = MaterialTheme.typography.headlineLarge, color = Color.White)
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Navy),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("رجوع للقائمة", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
        } else if (currentQ != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("السؤال ${toArabicDigits(idx + 1)}/${toArabicDigits(pool.size)}", style = MaterialTheme.typography.bodyMedium, color = GoldSoft)
                Text("النتيجة: ${toArabicDigits(score)}", style = MaterialTheme.typography.bodyMedium, color = Gold)
            }

            Spacer(Modifier.height(16.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF070B14)Card),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("ما معنى اسم الله:", style = MaterialTheme.typography.bodyMedium, color = GoldSoft)
                    Spacer(Modifier.height(8.dp))
                    Text(currentQ.name, style = MaterialTheme.typography.headlineLarge, color = Gold, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                currentOptions.forEach { opt ->
                    val isCorrect = opt.name == currentQ.name
                    val isSelected = opt.name == selectedName

                    val bgColor = when {
                        selectedName != null && isCorrect -> Gold.copy(alpha = 0.25f)
                        isSelected && !isCorrect -> MaterialTheme.colorScheme.error.copy(alpha = 0.25f)
                        else -> NavyCard
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = selectedName == null) {
                                selectedName = opt.name
                                if (isCorrect) score++
                            },
                        colors = CardDefaults.cardColors(containerColor = bgColor),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = opt.meaning,
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
