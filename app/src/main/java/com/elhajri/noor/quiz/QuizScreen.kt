package com.elhajri.noor.quiz

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
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
import com.elhajri.noor.data.QuizQuestion
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.Navy
import com.elhajri.noor.ui.NavyCard
import com.elhajri.noor.ui.NavyLight
import kotlinx.coroutines.delay

private fun toArabicDigits(number: Any): String {
    val str = number.toString()
    val arabicDigits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
    return str.map { ch ->
        if (ch in '0'..'9') arabicDigits[ch - '0'] else ch
    }.joinToString("")
}

@Composable
fun QuizScreen() {
    val context = LocalContext.current
    val allQuestions = remember { DataLoader.quizQuestions(context) }

    var isStarted by remember { mutableStateOf(false) }
    var questions by remember { mutableStateOf<List<QuizQuestion>>(emptyList()) }
    var currentIdx by remember { mutableIntStateOf(0) }
    var selectedAnswer by remember { mutableStateOf<Int?>(null) }
    var score by remember { mutableIntStateOf(0) }
    var isFinished by remember { mutableStateOf(false) }

    fun startQuiz() {
        questions = allQuestions.shuffled().take(10)
        currentIdx = 0
        selectedAnswer = null
        score = 0
        isFinished = false
        isStarted = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Navy)
            .padding(16.dp)
    ) {
        if (!isStarted) {
            // Start Screen
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Gold.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        tint = Gold,
                        modifier = Modifier.size(44.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "الاختبار الديني",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Gold,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "اختبر معلوماتك الدينية\n١٠ أسئلة عشوائية من بنك أسئلة ضخم",
                    style = MaterialTheme.typography.bodyLarge,
                    color = GoldSoft,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "أكثر من ${toArabicDigits(allQuestions.size.coerceAtLeast(250))} سؤال متاح",
                    style = MaterialTheme.typography.bodySmall,
                    color = Gold
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { startQuiz() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Navy),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("ابدأ الاختبار", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
        } else if (isFinished) {
            // Finished Result Screen
            val percentage = if (questions.isNotEmpty()) Math.round((score.toFloat() / questions.size) * 100) else 0

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Gold.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = Gold,
                        modifier = Modifier.size(44.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "انتهى الاختبار!",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Gold,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "${toArabicDigits(score)}/${toArabicDigits(questions.size)}",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 36.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "نسبة الإجابات الصحيحة: ${toArabicDigits(percentage)}٪",
                    style = MaterialTheme.typography.bodyLarge,
                    color = GoldSoft
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { startQuiz() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Navy),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("إعادة الاختبار", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            // Question Play Screen
            val q = questions.getOrNull(currentIdx)
            if (q != null) {
                val isAnswered = selectedAnswer != null

                LaunchedEffect(selectedAnswer) {
                    if (selectedAnswer != null) {
                        delay(1200)
                        if (currentIdx + 1 >= questions.size) {
                            isFinished = true
                        } else {
                            currentIdx++
                            selectedAnswer = null
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "السؤال ${toArabicDigits(currentIdx + 1)}/${toArabicDigits(questions.size)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GoldSoft
                    )
                    Text(
                        text = "النتيجة: ${toArabicDigits(score)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = Gold,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { (currentIdx).toFloat() / questions.size },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape),
                    color = Gold,
                    trackColor = NavyLight
                )

                Spacer(modifier = Modifier.height(20.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = NavyCard),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = q.q,
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            q.options.forEachIndexed { i, opt ->
                                val isCorrect = i == q.correct
                                val isSelected = i == selectedAnswer

                                val containerColor = when {
                                    isAnswered -> {
                                        if (isCorrect) Color(0xFF2E7D32)
                                        else if (isSelected) Color(0xFFC62828)
                                        else NavyLight.copy(alpha = 0.5f)
                                    }
                                    else -> NavyLight
                                }

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(enabled = !isAnswered) {
                                            selectedAnswer = i
                                            if (i == q.correct) {
                                                score++
                                                com.elhajri.noor.audio.
                                            } else {
                                                com.elhajri.noor.audio.
                                            }
                                        },
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
                                        Text(
                                            text = opt,
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = Color.White,
                                            modifier = Modifier.weight(1f)
                                        )

                                        if (isAnswered) {
                                            if (isCorrect) {
                                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                                            } else if (isSelected) {
                                                Icon(Icons.Default.Close, contentDescription = null, tint = Color.White)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (isAnswered && q.ref.isNotBlank()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = q.ref,
                                style = MaterialTheme.typography.bodySmall,
                                color = GoldSoft.copy(alpha = 0.8f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}
