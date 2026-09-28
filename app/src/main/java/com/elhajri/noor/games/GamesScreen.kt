package com.elhajri.noor.games

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.EmojiEvents
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

private data class GameCard(val id: String, val name: String, val desc: String, val emoji: String)
private data class TFQuestion(val q: String, val a: Boolean)
private data class NumberQuestion(val q: String, val options: List<String>, val correct: Int)

@Composable
fun GamesScreen(onNavigate: (String) -> Unit) {
    var selectedLocalGame by remember { mutableStateOf<String?>(null) }

    when (selectedLocalGame) {
        "order" -> OrderProphetsGame(onBack = { selectedLocalGame = null })
        "truefalse" -> TrueFalseGame(onBack = { selectedLocalGame = null })
        "numbers" -> NumbersGame(onBack = { selectedLocalGame = null })
        else -> GamesHub(onSelect = { id ->
            when (id) {
                "names" -> onNavigate("names_game")
                "journey" -> onNavigate("journey_game")
                "quran_mem" -> onNavigate("mem_game")
                "quiz" -> onNavigate("quiz")
                else -> selectedLocalGame = id
            }
        })
    }
}

@Composable
private fun GamesHub(onSelect: (String) -> Unit) {
    val games = listOf(
        GameCard("order", "ترتيب الأنبياء", "رتّب الأنبياء حسب الترتيب الزمني", "🔢"),
        GameCard("truefalse", "صح أم خطأ", "١٠٠+ سؤال عن الدين", "✅"),
        GameCard("numbers", "اختبار الأرقام", "٥٠+ سؤال عن الأرقام الإسلامية", "🧮"),
        GameCard("quran_mem", "حفظ القرآن", "تابع رحلة حفظك للقرآن الكريم", "📖"),
        GameCard("names", "الأسماء الحسنى", "تعلّم واختبر أسماء الله الحسنى", "✨"),
        GameCard("journey", "رحلة الأنبياء", "١٥ مستوى عن قصص الأنبياء", "🗺️"),
        GameCard("quiz", "الاختبار الديني", "أسئلة إسلامية متنوعة", "🎯")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NoorGradients.ScreenBackground)
            .padding(16.dp)
    ) {
        Text(
            text = "الألعاب الإسلامية",
            style = MaterialTheme.typography.headlineMedium,
            color = Gold,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(games.size) { i ->
                val g = games[i]
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(g.id) },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF070B14)Card),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Gold.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(g.emoji, fontSize = 24.sp)
                            }
                            Column {
                                Text(
                                    text = g.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = g.desc,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = GoldSoft.copy(alpha = 0.8f)
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = null,
                            tint = GoldSoft
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderProphetsGame(onBack: () -> Unit) {
    val prophetsOrder = remember { listOf("آدم", "نوح", "إبراهيم", "موسى", "عيسى", "محمد") }
    var round by remember { mutableIntStateOf(0) }
    var shuffled by remember { mutableStateOf(prophetsOrder.shuffled()) }
    var selected by remember { mutableStateOf(listOf<String>()) }
    var score by remember { mutableIntStateOf(0) }
    var wrong by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(wrong) {
        if (wrong != null) {
            delay(800)
            wrong = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NoorGradients.ScreenBackground)
            .padding(16.dp)
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = Gold)
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("ترتيب الأنبياء", style = MaterialTheme.typography.headlineSmall, color = Gold)
            Spacer(Modifier.height(4.dp))
            Text(
                "اضغط بالترتيب الزمني · الجولة ${toArabicDigits(round + 1)}",
                style = MaterialTheme.typography.bodyMedium,
                color = GoldSoft
            )
            Spacer(Modifier.height(4.dp))
            Text("النتيجة: ${toArabicDigits(score)}", style = MaterialTheme.typography.titleMedium, color = Gold)
        }

        Spacer(Modifier.height(16.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF070B14)Card),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "التسلسل الصحيح: آدم ← نوح ← إبراهيم ← موسى ← عيسى ← محمد",
                    style = MaterialTheme.typography.bodySmall,
                    color = GoldSoft.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.height(240.dp)
                ) {
                    items(shuffled) { prophet ->
                        val isSelected = prophet in selected
                        val isWrong = prophet == wrong

                        val bgColor = when {
                            isSelected -> Gold.copy(alpha = 0.25f)
                            isWrong -> MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
                            else -> NavyLight
                        }

                        val textColor = when {
                            isSelected -> Gold
                            isWrong -> MaterialTheme.colorScheme.error
                            else -> Color.White
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .clickable(enabled = !isSelected) {
                                    val expectedIdx = selected.size
                                    if (prophet == prophetsOrder[expectedIdx]) {
                                        val newSelected = selected + prophet
                                        selected = newSelected
                                        if (newSelected.size == prophetsOrder.size) {
                                            score++
                                            round++
                                            shuffled = prophetsOrder.shuffled()
                                            selected = emptyList()
                                        }
                                    } else {
                                        wrong = prophet
                                    }
                                },
                            colors = CardDefaults.cardColors(containerColor = bgColor),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    if (isSelected) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Gold, modifier = Modifier.size(18.dp))
                                    }
                                    Text(prophet, style = MaterialTheme.typography.titleMedium, color = textColor)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (selected.size == prophetsOrder.size) {
            Spacer(Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF070B14)Light),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Gold, modifier = Modifier.size(32.dp))
                    Spacer(Modifier.height(4.dp))
                    Text("أحسنت! الترتيب صحيح", style = MaterialTheme.typography.titleMedium, color = Gold)
                }
            }
        }
    }
}

@Composable
private fun TrueFalseGame(onBack: () -> Unit) {
    val context = LocalContext.current
    val questions = remember {
        try {
            val jsonText = context.assets.open("data/gameQuestions.json").bufferedReader().use { it.readText() }
            val arr = JSONObject(jsonText).getJSONArray("trueFalseQuestions")
            List(arr.length()) { i ->
                val o = arr.getJSONObject(i)
                TFQuestion(o.getString("q"), o.getBoolean("a"))
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    var idx by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var answered by remember { mutableStateOf<Boolean?>(null) }
    var finished by remember { mutableStateOf(false) }

    LaunchedEffect(answered) {
        if (answered != null) {
            delay(1200)
            if (idx + 1 >= questions.size) {
                finished = true
            } else {
                idx++
                answered = null
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
            Text("صح أم خطأ", style = MaterialTheme.typography.titleLarge, color = Gold)
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
                Text("انتهت اللعبة!", style = MaterialTheme.typography.headlineMedium, color = Gold)
                Spacer(Modifier.height(8.dp))
                Text(
                    "${toArabicDigits(score)}/${toArabicDigits(questions.size)}",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White
                )
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = {
                        idx = 0
                        score = 0
                        answered = null
                        finished = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Navy),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("إعادة اللعب", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
        } else if (questions.isNotEmpty()) {
            val q = questions[idx]

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "السؤال ${toArabicDigits(idx + 1)}/${toArabicDigits(questions.size)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GoldSoft
                )
                Text(
                    "النتيجة: ${toArabicDigits(score)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gold
                )
            }

            Spacer(Modifier.height(16.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF070B14)Card),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = q.q,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // TRUE button
                val trueBg = when {
                    answered != null && q.a -> Gold.copy(alpha = 0.3f)
                    answered == true && !q.a -> MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
                    else -> NavyCard
                }
                Button(
                    onClick = {
                        if (answered == null) {
                            answered = true
                            if (q.a) score++
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = trueBg),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp)
                ) {
                    Text("صح", style = MaterialTheme.typography.headlineSmall, color = Color.White)
                }

                // FALSE button
                val falseBg = when {
                    answered != null && !q.a -> Gold.copy(alpha = 0.3f)
                    answered == false && q.a -> MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
                    else -> NavyCard
                }
                Button(
                    onClick = {
                        if (answered == null) {
                            answered = false
                            if (!q.a) score++
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = falseBg),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp)
                ) {
                    Text("خطأ", style = MaterialTheme.typography.headlineSmall, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun NumbersGame(onBack: () -> Unit) {
    val context = LocalContext.current
    val allQuestions = remember {
        try {
            val jsonText = context.assets.open("data/gameQuestions.json").bufferedReader().use { it.readText() }
            val arr = JSONObject(jsonText).getJSONArray("numberQuestions")
            List(arr.length()) { i ->
                val o = arr.getJSONObject(i)
                val optsArr = o.getJSONArray("options")
                NumberQuestion(
                    o.getString("q"),
                    List(optsArr.length()) { j -> optsArr.getString(j) },
                    o.getInt("correct")
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    var questions by remember { mutableStateOf(allQuestions.shuffled()) }
    var idx by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var selectedOption by remember { mutableStateOf<Int?>(null) }
    var finished by remember { mutableStateOf(false) }

    LaunchedEffect(selectedOption) {
        if (selectedOption != null) {
            delay(1200)
            if (idx + 1 >= questions.size) {
                finished = true
            } else {
                idx++
                selectedOption = null
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
            Text("اختبار الأرقام", style = MaterialTheme.typography.titleLarge, color = Gold)
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
                Text("انتهت اللعبة!", style = MaterialTheme.typography.headlineMedium, color = Gold)
                Spacer(Modifier.height(8.dp))
                Text(
                    "${toArabicDigits(score)}/${toArabicDigits(questions.size)}",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White
                )
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = {
                        questions = allQuestions.shuffled()
                        idx = 0
                        score = 0
                        selectedOption = null
                        finished = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Navy),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("إعادة اللعب", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
        } else if (questions.isNotEmpty()) {
            val q = questions[idx]

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "السؤال ${toArabicDigits(idx + 1)}/${toArabicDigits(questions.size)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GoldSoft
                )
                Text(
                    "النتيجة: ${toArabicDigits(score)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gold
                )
            }

            Spacer(Modifier.height(16.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF070B14)Card),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = q.q,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                q.options.forEachIndexed { i, opt ->
                    val isCorrect = i == q.correct
                    val isSelected = i == selectedOption

                    val cardBg = when {
                        selectedOption != null && isCorrect -> Gold.copy(alpha = 0.25f)
                        isSelected && !isCorrect -> MaterialTheme.colorScheme.error.copy(alpha = 0.25f)
                        else -> NavyCard
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = selectedOption == null) {
                                selectedOption = i
                                if (i == q.correct) score++
                            },
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(opt, style = MaterialTheme.typography.titleMedium, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
