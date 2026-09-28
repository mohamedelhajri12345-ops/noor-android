package com.elhajri.noor.games

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
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
import com.elhajri.noor.data.Surah
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.Navy
import com.elhajri.noor.ui.NavyCard
import com.elhajri.noor.ui.NavyLight
import kotlinx.coroutines.delay
import org.json.JSONObject
import com.elhajri.noor.ui.NoorGradients

private fun toArabicDigits(number: Any): String {
    val str = number.toString()
    val arabicDigits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
    return str.map { ch ->
        if (ch in '0'..'9') arabicDigits[ch - '0'] else ch
    }.joinToString("")
}

private sealed class GameQuestion {
    data class TrueFalse(val q: String, val answer: Boolean) : GameQuestion()
    data class MultipleChoice(val q: String, val options: List<String>, val correct: Int) : GameQuestion()
}

@Composable
fun QuranMemorizationScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var activeTab by remember { mutableIntStateOf(0) } // 0: Tracker, 1: Quiz Game

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NoorGradients.ScreenBackground)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "رجوع",
                    tint = Gold
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "حفظ القرآن الكريم",
                style = MaterialTheme.typography.titleLarge,
                color = Gold,
                fontWeight = FontWeight.Bold
            )
        }

        // Tab Selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { activeTab = 0 },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeTab == 0) Gold else NavyCard,
                    contentColor = if (activeTab == 0) Navy else GoldSoft
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("جدول الحفظ", fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = { activeTab = 1 },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeTab == 1) Gold else NavyCard,
                    contentColor = if (activeTab == 1) Navy else GoldSoft
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("اختبار الحفظ والآيات", fontWeight = FontWeight.Bold)
            }
        }

        if (activeTab == 0) {
            MemorizationTrackerView(context = context)
        } else {
            QuizGameView(context = context)
        }
    }
}

@Composable
private fun MemorizationTrackerView(context: Context) {
    val surahs = remember { DataLoader.surahs(context) }
    val sp = remember { context.getSharedPreferences("nur_quran_memorization", Context.MODE_PRIVATE) }

    var progressMap by remember {
        mutableStateOf(
            surahs.associate { s ->
                s.number to (sp.getString(s.number.toString(), "not_started") ?: "not_started")
            }
        )
    }

    fun cycleStatus(number: Int) {
        val current = progressMap[number] ?: "not_started"
        val next = when (current) {
            "not_started" -> "in_progress"
            "in_progress" -> "memorized"
            else -> "not_started"
        }
        sp.edit().putString(number.toString(), next).apply()
        progressMap = progressMap.toMutableMap().apply { put(number, next) }
    }

    val memorizedCount = progressMap.values.count { it == "memorized" }
    val inProgressCount = progressMap.values.count { it == "in_progress" }
    val notStartedCount = surahs.size - memorizedCount - inProgressCount
    val percentage = if (surahs.isNotEmpty()) Math.round((memorizedCount.toFloat() / surahs.size) * 100) else 0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            // Header stats
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF070B14)Card),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("التقدم الكلي", style = MaterialTheme.typography.titleMedium, color = Color.White)
                        Text("${toArabicDigits(percentage)}٪", style = MaterialTheme.typography.titleMedium, color = Gold, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { percentage / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(CircleShape),
                        color = Gold,
                        trackColor = NavyLight,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatBox("محفوظة", memorizedCount, Gold, Modifier.weight(1f))
                        StatBox("قيد الحفظ", inProgressCount, GoldSoft, Modifier.weight(1f))
                        StatBox("لم تبدأ", notStartedCount, Color.Gray, Modifier.weight(1f))
                    }
                }
            }
        }

        item {
            Text(
                text = "اضغط على السورة لتغيير حالة الحفظ (لم تبدأ ← قيد الحفظ ← محفوظة)",
                style = MaterialTheme.typography.bodySmall,
                color = GoldSoft.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )
        }

        items(surahs) { surah ->
            val status = progressMap[surah.number] ?: "not_started"
            SurahStatusCard(surah = surah, status = status, onClick = { cycleStatus(surah.number) })
        }
    }
}

@Composable
private fun StatBox(label: String, count: Int, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF070B14)Light),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(toArabicDigits(count), style = MaterialTheme.typography.titleLarge, color = color, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.bodySmall, color = GoldSoft.copy(alpha = 0.8f))
        }
    }
}

@Composable
private fun SurahStatusCard(surah: Surah, status: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = when (status) {
                "memorized" -> Gold.copy(alpha = 0.15f)
                "in_progress" -> GoldSoft.copy(alpha = 0.1f)
                else -> NavyCard
            }
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
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
                        .clip(RoundedCornerShape(10.dp))
                        .background(Gold.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        toArabicDigits(surah.number),
                        style = MaterialTheme.typography.labelLarge,
                        color = Gold,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column {
                    Text(
                        "سورة ${surah.name}",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${surah.type} · ${toArabicDigits(surah.ayahs)} آية",
                        style = MaterialTheme.typography.bodySmall,
                        color = GoldSoft.copy(alpha = 0.7f)
                    )
                }
            }

            when (status) {
                "memorized" -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("محفوظة", style = MaterialTheme.typography.bodySmall, color = Gold, fontWeight = FontWeight.Bold)
                        Icon(Icons.Default.Check, contentDescription = null, tint = Gold, modifier = Modifier.size(20.dp))
                    }
                }
                "in_progress" -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("قيد الحفظ", style = MaterialTheme.typography.bodySmall, color = GoldSoft)
                        Icon(Icons.Default.Schedule, contentDescription = null, tint = GoldSoft, modifier = Modifier.size(20.dp))
                    }
                }
                else -> {
                    Icon(Icons.Default.RadioButtonUnchecked, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
private fun QuizGameView(context: Context) {
    val questions = remember { loadQuestionsFromAssets(context) }
    var currentIdx by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var selectedAnswer by remember { mutableStateOf<Any?>(null) }
    var isFinished by remember { mutableStateOf(false) }

    if (questions.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("لا توجد أسئلة متاح حالياً", color = GoldSoft)
        }
        return
    }

    if (isFinished) {
        val percentage = Math.round((score.toFloat() / questions.size) * 100)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = null,
                tint = Gold,
                modifier = Modifier.size(72.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("انتهت اللعبة!", style = MaterialTheme.typography.headlineMedium, color = Gold, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "${toArabicDigits(score)} / ${toArabicDigits(questions.size)}",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text("نسبة الإجابات الصحيحة: ${toArabicDigits(percentage)}٪", style = MaterialTheme.typography.bodyLarge, color = GoldSoft)
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    currentIdx = 0
                    score = 0
                    selectedAnswer = null
                    isFinished = false
                },
                colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Navy),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("إعادة الاختبار", fontWeight = FontWeight.Bold)
            }
        }
        return
    }

    val q = questions[currentIdx]
    val isAnswered = selectedAnswer != null

    fun handleAnswer(userChoice: Any) {
        if (isAnswered) return
        selectedAnswer = userChoice
        val isCorrect = when (q) {
            is GameQuestion.TrueFalse -> userChoice == q.answer
            is GameQuestion.MultipleChoice -> userChoice == q.correct
        }
        if (isCorrect) score++
    }

    LaunchedEffect(selectedAnswer) {
        if (selectedAnswer != null) {
            delay(1200)
            if (currentIdx + 1 < questions.size) {
                currentIdx++
                selectedAnswer = null
            } else {
                isFinished = true
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "السؤال ${toArabicDigits(currentIdx + 1)} من ${toArabicDigits(questions.size)}",
                style = MaterialTheme.typography.bodyMedium,
                color = GoldSoft
            )
            Text(
                "النتيجة: ${toArabicDigits(score)}",
                style = MaterialTheme.typography.titleMedium,
                color = Gold,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = { (currentIdx + 1).toFloat() / questions.size },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape),
            color = Gold,
            trackColor = NavyLight
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF070B14)Card),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = when (q) {
                        is GameQuestion.TrueFalse -> q.q
                        is GameQuestion.MultipleChoice -> q.q
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                when (q) {
                    is GameQuestion.TrueFalse -> {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TrueFalseOptionButton(
                                label = "صح",
                                isSelected = selectedAnswer == true,
                                isCorrect = q.answer == true,
                                isAnswered = isAnswered,
                                onClick = { handleAnswer(true) },
                                modifier = Modifier.weight(1f)
                            )
                            TrueFalseOptionButton(
                                label = "خطأ",
                                isSelected = selectedAnswer == false,
                                isCorrect = q.answer == false,
                                isAnswered = isAnswered,
                                onClick = { handleAnswer(false) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    is GameQuestion.MultipleChoice -> {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            q.options.forEachIndexed { index, option ->
                                MultipleChoiceOptionCard(
                                    text = option,
                                    isSelected = selectedAnswer == index,
                                    isCorrect = index == q.correct,
                                    isAnswered = isAnswered,
                                    onClick = { handleAnswer(index) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TrueFalseOptionButton(
    label: String,
    isSelected: Boolean,
    isCorrect: Boolean,
    isAnswered: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = when {
        isAnswered && isCorrect -> Color(0xFF2E7D32)
        isAnswered && isSelected && !isCorrect -> Color(0xFFC62828)
        else -> NavyLight
    }

    Card(
        modifier = modifier
            .height(56.dp)
            .clickable(enabled = !isAnswered, onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(14.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(label, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun MultipleChoiceOptionCard(
    text: String,
    isSelected: Boolean,
    isCorrect: Boolean,
    isAnswered: Boolean,
    onClick: () -> Unit
) {
    val containerColor = when {
        isAnswered && isCorrect -> Color(0xFF2E7D32)
        isAnswered && isSelected && !isCorrect -> Color(0xFFC62828)
        else -> NavyLight
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isAnswered, onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text, style = MaterialTheme.typography.bodyLarge, color = Color.White, modifier = Modifier.weight(1f))
            if (isAnswered) {
                if (isCorrect) Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                else if (isSelected) Icon(Icons.Default.Close, contentDescription = null, tint = Color.White)
            }
        }
    }
}

private fun loadQuestionsFromAssets(context: Context): List<GameQuestion> {
    val list = mutableListOf<GameQuestion>()
    try {
        val jsonStr = context.assets.open("data/gameQuestions.json").bufferedReader().use { it.readText() }
        val root = JSONObject(jsonStr)

        if (root.has("trueFalseQuestions")) {
            val tfArr = root.getJSONArray("trueFalseQuestions")
            for (i in 0 until tfArr.length()) {
                val obj = tfArr.getJSONObject(i)
                list.add(GameQuestion.TrueFalse(obj.getString("q"), obj.getBoolean("a")))
            }
        }

        if (root.has("numberQuestions")) {
            val numArr = root.getJSONArray("numberQuestions")
            for (i in 0 until numArr.length()) {
                val obj = numArr.getJSONObject(i)
                val optsArr = obj.getJSONArray("options")
                val opts = List(optsArr.length()) { j -> optsArr.getString(j) }
                list.add(GameQuestion.MultipleChoice(obj.getString("q"), opts, obj.getInt("correct")))
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return list.shuffled().take(20)
}
