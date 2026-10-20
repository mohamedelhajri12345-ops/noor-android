package com.elhajri.noor.exams

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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

@Composable
fun ExamResultScreen(
    result: ExamResult,
    onRetake: () -> Unit,
    onNextLevel: () -> Unit,
    onOpenCertificate: () -> Unit,
    onBackToMap: () -> Unit
) {
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
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBackToMap) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "العودة للخريطة",
                        tint = Gold
                    )
                }
                Text(
                    text = "تحليل نتيجة الاختبار",
                    color = Gold,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(48.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Result Hero Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .noorGlassCard(cornerRadius = 24.dp)
                    .border(
                        2.dp,
                        if (result.passed) Gold else Color.Red.copy(alpha = 0.6f),
                        RoundedCornerShape(24.dp)
                    )
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = if (result.passed) Icons.Default.EmojiEvents else Icons.Default.Refresh,
                        contentDescription = null,
                        tint = if (result.passed) Gold else Color.Red,
                        modifier = Modifier.size(56.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (result.passed) "مبارك! لقد اجتزت الاختبار بنجاح" else "لم تجتز الاختبار هذه المرة",
                        color = if (result.passed) Gold else Color.Red,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "المستوى ${result.levelNumber.toArabicDigits()} (${result.difficulty.titleAr} - ${result.track.titleAr})",
                        color = GoldSoft,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Percentage Badge
                    Text(
                        text = "%${result.percentage.toArabicDigits()}",
                        color = Color.White,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    // Earned Stars Row
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        repeat(3) { starIdx ->
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = if (starIdx < result.starsEarned) Gold else Color.Gray.copy(alpha = 0.4f),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Stat Metrics Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = "الإجابات الصحيحة",
                    value = "${result.correctAnswers.toArabicDigits()} / ${result.totalQuestions.toArabicDigits()}",
                    color = Color.Green,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "الوقت المستغرق",
                    value = "${(result.timeTakenSeconds / 60).toArabicDigits()} د ${(result.timeTakenSeconds % 60).toArabicDigits()} ث",
                    color = Gold,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Category Breakdown Section
            Text(
                text = "الأداء حسب الفئات والمواضيع:",
                color = Gold,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            val categoryAnswersMap = result.answers.groupBy { it.question.category }
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                categoryAnswersMap.forEach { (catKey, qList) ->
                    val correctInCat = qList.count { it.isCorrect }
                    val totalInCat = qList.size
                    val catRatio = (correctInCat.toFloat() / totalInCat).coerceIn(0f, 1f)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .noorGlassCard(cornerRadius = 14.dp)
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = getCategoryLabelAr(catKey),
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${correctInCat.toArabicDigits()} من ${totalInCat.toArabicDigits()}",
                                    color = GoldSoft,
                                    fontSize = 13.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { catRatio },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(CircleShape),
                                color = if (catRatio >= 0.7f) Color.Green else Gold,
                                trackColor = NavyCard
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Question by Question Review List
            Text(
                text = "مراجعة جميع الأسئلة والإجابات:",
                color = Gold,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                result.answers.forEachIndexed { idx, ans ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .noorGlassCard(cornerRadius = 16.dp)
                            .border(
                                1.dp,
                                if (ans.isCorrect) Color.Green.copy(alpha = 0.5f) else Color.Red.copy(alpha = 0.5f),
                                RoundedCornerShape(16.dp)
                            )
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "سؤال ${(idx + 1).toArabicDigits()}",
                                    color = Gold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Icon(
                                    imageVector = if (ans.isCorrect) Icons.Default.CheckCircle else Icons.Default.Close,
                                    contentDescription = null,
                                    tint = if (ans.isCorrect) Color.Green else Color.Red,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = ans.question.q,
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // User Answer
                            val userOptText = if (ans.selectedIndex != null && ans.selectedIndex in 0..3) {
                                ans.question.options[ans.selectedIndex]
                            } else {
                                "لم يتم الاختيار (انتهى الوقت)"
                            }
                            Text(
                                text = "إجابتك: $userOptText",
                                color = if (ans.isCorrect) Color.Green else Color.Red,
                                fontSize = 13.sp
                            )

                            // Correct Answer if wrong
                            if (!ans.isCorrect) {
                                Text(
                                    text = "الإجابة الصحيحة: ${ans.question.options[ans.question.correct]}",
                                    color = GoldSoft,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Explanation
                            Text(
                                text = "الشرح والتعليم: ${ans.question.explanation}",
                                color = TextMain.copy(alpha = 0.8f),
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Action Buttons
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (result.passed) {
                    Button(
                        onClick = onNextLevel,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Gold),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("المستوى التالي", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = onOpenCertificate,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Gold),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("عرض الشهادة المعتمدة", color = Gold, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onRetake,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Gold.copy(alpha = 0.2f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GoldSoft),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("إعادة المحاولة", color = GoldSoft, fontSize = 14.sp)
                    }

                    Button(
                        onClick = onBackToMap,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Gold.copy(alpha = 0.2f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GoldSoft),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("خريطة المستويات", color = GoldSoft, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricCard(title: String, value: String, color: Color, modifier: Modifier) {
    Box(
        modifier = modifier
            .noorGlassCard(cornerRadius = 16.dp)
            .padding(14.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = title, color = GoldSoft, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, color = color, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}
