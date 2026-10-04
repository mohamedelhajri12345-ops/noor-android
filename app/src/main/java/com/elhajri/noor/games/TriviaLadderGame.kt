package com.elhajri.noor.games

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.Navy
import com.elhajri.noor.ui.NavyCard
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * "ميدان المعرفة" — سلم ذهبي من ١٠ أسئلة متدرجة الصعوبة، بعدّاد ١٥ ثانية،
 * وثلاث وسائل مساعدة: إشعاع الحكمة (حذف خيارين)، نور التفكير (وقت إضافي)،
 * وتبديل السؤال. مضاعف نقاط لكل إجابات متتالية سريعة.
 */
@Composable
fun TriviaLadderGameScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val allQuestions = remember { DataLoader.quizQuestions(context).shuffled(Random(System.currentTimeMillis() % 1000)) }
    var ladder by remember { mutableStateOf(allQuestions.take(10)) }

    var step by remember { mutableIntStateOf(0) }
    var eliminated by remember { mutableStateOf(setOf<Int>()) }
    var timeLeft by remember { mutableIntStateOf(15) }
    var combo by remember { mutableIntStateOf(1) }
    var score by remember { mutableIntStateOf(0) }
    var state by remember { mutableStateOf("playing") } // playing | answered | done | fail
    var pickedIdx by remember { mutableStateOf<Int?>(null) }
    var helpsUsed by remember { mutableStateOf(setOf<String>()) }
    var wrongFlash by remember { mutableStateOf<Int?>(null) }

    val q = ladder.getOrNull(step)

    LaunchedEffect(step, state) {
        if (state == "playing" && q != null) {
            timeLeft = 15
            while (timeLeft > 0 && state == "playing") {
                delay(1000)
                if (state == "playing") timeLeft--
            }
            if (state == "playing" && timeLeft <= 0) { state = "fail" }
        }
    }

    fun answer(i: Int) {
        if (state != "playing" || i in eliminated) return
        pickedIdx = i
        if (i == q!!.correct) {
            state = "answered"
            score += 100 * combo
            combo = if (combo < 3) combo + 1 else 3
        } else {
            wrongFlash = i
            state = "fail"
        }
    }

    fun useHelp(h: String) {
        if (h in helpsUsed || state != "playing") return
        helpsUsed = helpsUsed + h
        when (h) {
            "wisdom" -> {
                val wrongs = q!!.options.indices.filter { it != q.correct }.shuffled().take(2)
                eliminated = wrongs.toSet()
            }
            "light" -> timeLeft += 15
            "swap" -> {
                val pool = allQuestions.filterNot { it in ladder }
                if (pool.isEmpty()) return
                val replacement = pool.random()
                ladder = ladder.toMutableList().also { it[step] = replacement }
                eliminated = emptySet()
                timeLeft = 15
            }
        }
    }

    Box(Modifier.fillMaxSize().background(Navy)) {
        Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            Row(Modifier.fillMaxWidth().padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = Gold) }
                Column(Modifier.weight(1f)) {
                    Text("ميدان المعرفة", color = Gold, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("السلم الذهبي — ١٠ أسئلة", color = GoldSoft, fontSize = 11.sp)
                }
                Text("$score", color = Gold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            // درجات السلم
            Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                for (s in 0 until 10) {
                    Box(
                        Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (s < step) Gold else Gold.copy(alpha = 0.15f))
                    )
                }
            }

            if (q == null || state == "done") {
                Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Text("🎉", fontSize = 40.sp)
                    Text("اكتمل السلم الذهبي!", color = Gold, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("نتيجتك: $score نقطة — كنز حسنات", color = GoldSoft, fontSize = 13.sp, modifier = Modifier.padding(top = 6.dp))
                    Spacer(Modifier.height(16.dp))
                    Box(
                        Modifier.clip(RoundedCornerShape(12.dp)).background(Gold).clickable { onBack() }.padding(horizontal = 30.dp, vertical = 10.dp)
                    ) { Text("حسناً", color = Navy, fontWeight = FontWeight.Bold, fontSize = 14.sp) }
                }
            } else {
                Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                    // عداد الوقت
                    Box(Modifier.size(40.dp).clip(CircleShape).border(2.dp, if (timeLeft <= 5) Color(0xFFEF4444) else Gold, CircleShape), contentAlignment = Alignment.Center) {
                        Text("$timeLeft", color = Gold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(Modifier.width(10.dp))
                    Text("مضاعف ×$combo", color = GoldSoft, fontSize = 12.sp)
                    Spacer(Modifier.weight(1f))
                    // وسائل المساعدة
                    HelpIcon("wisdom", Icons.Default.AutoAwesome, "حذف خيارين", helpsUsed, enabled = state == "playing") { useHelp("wisdom") }
                    HelpIcon("light", Icons.Default.Lightbulb, "وقت إضافي", helpsUsed, enabled = state == "playing") { useHelp("light") }
                    HelpIcon("swap", Icons.Default.Refresh, "تبديل السؤال", helpsUsed, enabled = state == "playing") { useHelp("swap") }
                }

                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(NavyCard)
                        .border(1.dp, Gold.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        .padding(18.dp)
                ) {
                    Text(q.q, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    if (q.ref.isNotBlank()) {
                        Text("﴿${q.ref}﴾", color = GoldSoft.copy(alpha = 0.7f), fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 6.dp))
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    q.options.forEachIndexed { i, opt ->
                        val isCorrect = i == q.correct
                        val isPicked = pickedIdx == i
                        val bg = when {
                            state == "answered" && isCorrect -> Gold
                            state == "fail" && isCorrect -> Gold.copy(alpha = 0.6f)
                            wrongFlash == i -> Color(0xFF7F1D1D).copy(alpha = 0.5f)
                            i in eliminated -> Color.Transparent
                            else -> NavyCard
                        }
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(bg)
                                .border(1.dp, if (bg == Color.Transparent) Color.Transparent else Gold.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                .clickable(enabled = state == "playing" && i !in eliminated) { answer(i) }
                                .padding(14.dp)
                        ) {
                            Text(
                                opt,
                                color = when {
                                    bg == Gold -> Navy
                                    bg == Color.Transparent -> Color.White.copy(alpha = 0.15f)
                                    else -> Color.White
                                },
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                if (state == "answered") {
                    Spacer(Modifier.height(10.dp))
                    Box(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Gold).clickable {
                            pickedIdx = null
                            eliminated = emptySet()
                            if (step + 1 >= ladder.size) state = "done" else { step++; state = "playing" }
                        }.padding(vertical = 10.dp)
                    ) {
                        Text("الدرجة التالية ↑", color = Navy, fontWeight = FontWeight.Bold, fontSize = 14.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    }
                }
                if (state == "fail") {
                    Spacer(Modifier.height(10.dp))
                    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("انتهت المحاولة — النتيجة: $score", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                        Spacer(Modifier.height(8.dp))
                        Box(
                            Modifier.clip(RoundedCornerShape(12.dp)).border(1.dp, Gold, RoundedCornerShape(12.dp)).clickable { onBack() }.padding(horizontal = 30.dp, vertical = 8.dp)
                        ) { Text("خروج", color = Gold, fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                    }
                }
            }
        }
    }
}

@Composable
private fun HelpIcon(id: String, icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, used: Set<String>, enabled: Boolean, onClick: () -> Unit) {
    val usedUp = id in used
    IconButton(onClick = onClick, enabled = enabled && !usedUp) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = label, tint = if (usedUp) Color.White.copy(alpha = 0.2f) else Gold, modifier = Modifier.size(20.dp))
            Text(label, color = if (usedUp) Color.White.copy(alpha = 0.2f) else GoldSoft, fontSize = 8.sp)
        }
    }
}
