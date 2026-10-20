package com.elhajri.noor.exams

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Timer
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
import com.elhajri.noor.games.toArabicDigits
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.NavyCard
import com.elhajri.noor.ui.TextMain
import com.elhajri.noor.ui.noorGlassCard
import com.elhajri.noor.ui.themeScreenBackground
import kotlinx.coroutines.delay

@Composable
fun ExamPlayScreen(
    track: ExamTrack,
    difficulty: ExamDifficulty,
    levelNumber: Int,
    onFinishExam: (ExamResult) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val questions = remember {
        ExamQuestionBank.generateLevelQuestions(context, track, difficulty, levelNumber)
    }

    if (questions.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().themeScreenBackground(), contentAlignment = Alignment.Center) {
            Text("جارٍ تحميل أسئلة الاختبار...", color = Gold, fontSize = 18.sp)
        }
        return
    }

    var currentQuestionIdx by remember { mutableIntStateOf(0) }
    var selectedOptionIdx by remember { mutableStateOf<Int?>(null) }
    var isAnswerSubmitted by remember { mutableStateOf(false) }

    val userAnswers = remember { mutableStateListOf<ExamQuestionAnswer>() }

    // Timer per question (15s for Kids, 12s for Adults)
    val maxTimeSeconds = if (track == ExamTrack.KID) 15 else 12
    var remainingSeconds by remember(currentQuestionIdx) { mutableIntStateOf(maxTimeSeconds) }
    var totalTimeSpentSeconds by remember { mutableIntStateOf(0) }

    // Timer loop
    LaunchedEffect(currentQuestionIdx, isAnswerSubmitted) {
        if (!isAnswerSubmitted) {
            remainingSeconds = maxTimeSeconds
            while (remainingSeconds > 0 && !isAnswerSubmitted) {
                delay(1000L)
                remainingSeconds--
                totalTimeSpentSeconds++
            }
            if (remainingSeconds == 0 && !isAnswerSubmitted) {
                // Auto-submit timeout
                isAnswerSubmitted = true
                val currentQ = questions[currentQuestionIdx]
                userAnswers.add(
                    ExamQuestionAnswer(
                        question = currentQ,
                        selectedIndex = null,
                        isCorrect = false,
                        timeSpentSeconds = maxTimeSeconds
                    )
                )
            }
        }
    }

    val currentQ = questions[currentQuestionIdx]

    fun submitAnswer(chosenIdx: Int) {
        if (isAnswerSubmitted) return
        selectedOptionIdx = chosenIdx
        isAnswerSubmitted = true

        val isCorrect = chosenIdx == currentQ.correct
        userAnswers.add(
            ExamQuestionAnswer(
                question = currentQ,
                selectedIndex = chosenIdx,
                isCorrect = isCorrect,
                timeSpentSeconds = maxTimeSeconds - remainingSeconds
            )
        )
    }

    fun nextQuestion() {
        if (currentQuestionIdx < questions.size - 1) {
            currentQuestionIdx++
            selectedOptionIdx = null
            isAnswerSubmitted = false
        } else {
            // Exam finished! Calculate result
            val correctCount = userAnswers.count { it.isCorrect }
            val totalQ = questions.size
            val percentage = (correctCount * 100) / totalQ
            val passed = percentage >= 70
            val starsEarned = when {
                percentage >= 90 -> 3
                percentage >= 80 -> 2
                percentage >= 70 -> 1
                else -> 0
            }

            ExamStorage.saveLevelResult(
                context = context,
                track = track,
                diff = difficulty,
                levelNumber = levelNumber,
                percentage = percentage,
                passed = passed,
                starsEarned = starsEarned
            )

            val result = ExamResult(
                track = track,
                difficulty = difficulty,
                levelNumber = levelNumber,
                totalQuestions = totalQ,
                correctAnswers = correctCount,
                percentage = percentage,
                passed = passed,
                starsEarned = starsEarned,
                timeTakenSeconds = totalTimeSpentSeconds,
                answers = userAnswers.toList()
            )

            onFinishExam(result)
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "إنهاء الاختبار",
                        tint = Gold
                    )
                }
                Text(
                    text = "المستوى ${levelNumber.toArabicDigits()} (${difficulty.titleAr} - ${track.titleAr})",
                    color = Gold,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
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
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = if (remainingSeconds <= 3) Color.Red else Gold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${remainingSeconds.toArabicDigits()} ث",
                            color = if (remainingSeconds <= 3) Color.Red else Gold,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress Indicators Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "السؤال ${(currentQuestionIdx + 1).toArabicDigits()} من ${questions.size.toArabicDigits()}",
                    color = GoldSoft,
                    fontSize = 13.sp
                )
                Text(
                    text = "الفئة: ${getCategoryLabelAr(currentQ.category)}",
                    color = Gold,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { (currentQuestionIdx + 1).toFloat() / questions.size },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = Gold,
                trackColor = NavyCard
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Question Text Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .noorGlassCard(cornerRadius = 20.dp)
                    .border(1.dp, Gold.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = currentQ.q,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    lineHeight = 30.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 4 Options Grid
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                currentQ.options.forEachIndexed { idx, optionText ->
                    val isSelected = selectedOptionIdx == idx
                    val isCorrect = idx == currentQ.correct

                    val buttonBg by animateColorAsState(
                        targetValue = when {
                            isAnswerSubmitted && isCorrect -> Color(0xFF1B5E20) // Green
                            isAnswerSubmitted && isSelected && !isCorrect -> Color(0xFFB71C1C) // Red
                            isSelected -> Gold.copy(alpha = 0.3f)
                            else -> NavyCard
                        },
                        label = "optionBg"
                    )

                    val borderTint by animateColorAsState(
                        targetValue = when {
                            isAnswerSubmitted && isCorrect -> Color.Green
                            isAnswerSubmitted && isSelected && !isCorrect -> Color.Red
                            isSelected -> Gold
                            else -> Gold.copy(alpha = 0.2f)
                        },
                        label = "borderTint"
                    )

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable(enabled = !isAnswerSubmitted) {
                                submitAnswer(idx)
                            },
                        color = buttonBg,
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, borderTint),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Gold.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = getOptionSymbolAr(idx),
                                        color = Gold,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = optionText,
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            if (isAnswerSubmitted) {
                                if (isCorrect) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.Green)
                                } else if (isSelected) {
                                    Icon(Icons.Default.Close, contentDescription = null, tint = Color.Red)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Explanation & Next Button if answered
            if (isAnswerSubmitted) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .noorGlassCard(cornerRadius = 14.dp)
                        .padding(14.dp)
                ) {
                    Column {
                        Text(
                            text = if (selectedOptionIdx == currentQ.correct) "إجابة صحيحة! أحسنت." else "إجابة خاطئة!",
                            color = if (selectedOptionIdx == currentQ.correct) Color.Green else Color.Red,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = currentQ.explanation,
                            color = GoldSoft,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { nextQuestion() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Gold),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = if (currentQuestionIdx < questions.size - 1) "السؤال التالي" else "عرض النتيجة والتحليل",
                        color = Color.Black,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

fun getOptionSymbolAr(idx: Int): String {
    return when (idx) {
        0 -> "أ"
        1 -> "ب"
        2 -> "ج"
        3 -> "د"
        else -> "•"
    }
}

fun getCategoryLabelAr(cat: String): String {
    return when (cat.lowercase()) {
        "quran" -> "القرآن الكريم"
        "seerah" -> "السيرة النبوية"
        "prophets" -> "قصص الأنبياء"
        "fiqh" -> "الفقه الإسلامي"
        "aqeedah" -> "العقيدة الإسلامية"
        "adab" -> "الآداب والأخلاق"
        "history" -> "التاريخ الإسلامي"
        "adhkar" -> "الأذكار والأدعية"
        else -> "علوم إسلامية"
    }
}
