package com.elhajri.noor.games

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
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
import com.elhajri.noor.data.DataLoader
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
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

// Quiz question built at runtime from the 99 names dataset
private data class NamesQuizQuestion(
    val prompt: String,
    val options: List<String>,
    val correct: Int
)

@Composable
fun NamesOfAllahGameScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    ForcePortraitOrientation()

    val names = remember { DataLoader.namesOfAllah(context) }

    val totalLevels = 15

    var progressData by remember { mutableStateOf(NewGameProgress.load(context, "game_names_allah")) }
    var mode by remember { mutableStateOf<String?>(null) } // null = mode chooser, "atlas" = encyclopedia, "quiz" = level map, "play" = in level
    var currentLevel by remember { mutableStateOf<Int?>(null) }
    var totalScore by remember { mutableStateOf(progressData.score) }
    var highestUnlocked by remember { mutableStateOf(progressData.level) }
    var learnedNames by remember { mutableStateOf(progressData.customData) }

    fun persistLearned() {
        NewGameProgress.save(context, "game_names_allah", highestUnlocked, totalScore, progressData.streak, progressData.stars, learnedNames)
    }

    Box(Modifier.fillMaxSize().themeScreenBackground()) {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            // Header
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = {
                    when {
                        mode == "play" -> { mode = "quiz"; currentLevel = null }
                        mode != null -> mode = null
                        else -> onBack()
                    }
                }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = Gold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("الأسماء الحسنى", color = Gold, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("٩٩ اسماً — دليل واختبارات إتقان", color = GoldSoft, fontSize = 11.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Gold, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(totalScore.toArabicDigits(), color = Gold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(12.dp))

            when (mode) {
                null -> ModeChooser(
                    onAtlas = { mode = "atlas" },
                    onQuiz = { mode = "quiz" },
                    learnedCount = learnedNames.split(",").count { it.isNotBlank() },
                    total = names.size
                )
                "atlas" -> NamesAtlas(
                    names = names,
                    learnedNames = learnedNames,
                    onToggle = { name ->
                        val set = learnedNames.split(",").filter { it.isNotBlank() }.toMutableSet()
                        if (set.contains(name)) set.remove(name) else set.add(name)
                        learnedNames = set.joinToString(",")
                        persistLearned()
                    }
                )
                "quiz" -> LevelMap(
                    totalLevels = totalLevels,
                    highestUnlocked = highestUnlocked,
                    stars = progressData.stars,
                    onStart = { lvl -> currentLevel = lvl; mode = "play" }
                )
                "play" -> currentLevel?.let { lvl ->
                    NamesQuiz(
                        names = names,
                        level = lvl,
                        totalLevels = totalLevels,
                        score = totalScore,
                        onLevelDone = { earned, stars ->
                            totalScore += earned
                            if (lvl >= highestUnlocked && stars >= 2) {
                                highestUnlocked = (lvl + 1).coerceAtMost(totalLevels)
                            }
                            val newStars = stars + if (stars >= 2) 1 else 0
                            NewGameProgress.save(
                                context, "game_names_allah",
                                highestUnlocked, totalScore,
                                progressData.streak + 1, newStars, learnedNames
                            )
                            progressData = NewGameProgress.load(context, "game_names_allah")
                        },
                        onExit = { mode = "quiz"; currentLevel = null }
                    )
                }
            }
        }
    }
}

@Composable
private fun ModeChooser(onAtlas: () -> Unit, onQuiz: () -> Unit, learnedCount: Int, total: Int) {
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Encyclopedia mode card
        Row(
            Modifier
                .fillMaxWidth()
                .noorGlassCard(cornerRadius = 18.dp)
                .clickable { onAtlas() }
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.MenuBook, contentDescription = null, tint = Gold, modifier = Modifier.size(34.dp))
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text("دليل الأسماء", color = Gold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("تصفّح ٩٩ اسماً وتعلّم معانيها", color = GoldSoft, fontSize = 11.sp)
            }
            Text(
                "${learnedCount.toArabicDigits()} / ${total.toArabicDigits()}",
                color = GoldSoft, fontSize = 11.sp
            )
        }
        // Quiz mode card
        Row(
            Modifier
                .fillMaxWidth()
                .noorGlassCard(cornerRadius = 18.dp)
                .clickable { onQuiz() }
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Star, contentDescription = null, tint = Gold, modifier = Modifier.size(34.dp))
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text("اختبار الإتقان", color = Gold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("١٥ مستوى — اربط الاسم بمعناه", color = GoldSoft, fontSize = 11.sp)
            }
            Icon(Icons.Default.Lock, contentDescription = null, tint = GoldSoft, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun NamesAtlas(names: List<com.elhajri.noor.data.NameOfAllah>, learnedNames: String, onToggle: (String) -> Unit) {
    val learned = remember(learnedNames) { learnedNames.split(",").filter { it.isNotBlank() }.toSet() }
    var search by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf<com.elhajri.noor.data.NameOfAllah?>(null) }

    Column {
        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            placeholder = { Text("ابحث عن اسم", fontSize = 12.sp) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(Modifier.height(10.dp))

        selected?.let { sel ->
            Column(
                Modifier
                    .fillMaxWidth()
                    .noorGlassCard(cornerRadius = 16.dp)
                    .padding(14.dp)
            ) {
                Text(sel.name, color = Gold, fontSize = 22.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(6.dp))
                Text(sel.meaning, color = Color.White, fontSize = 13.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            }
            Spacer(Modifier.height(10.dp))
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            itemsIndexed(names.filter { it.name.contains(search) }) { _, n ->
                val isLearned = learned.contains(n.name)
                Column(
                    Modifier
                        .fillMaxWidth()
                        .noorGlassCard(cornerRadius = 12.dp)
                        .clickable {
                            selected = n
                            onToggle(n.name)
                        }
                        .padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(n.name, color = if (isLearned) Gold else Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    if (isLearned) {
                        Spacer(Modifier.height(4.dp))
                        Icon(Icons.Default.Check, contentDescription = null, tint = Gold, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun LevelMap(totalLevels: Int, highestUnlocked: Int, stars: Int, onStart: (Int) -> Unit) {
    Text(
        "اختر المستوى",
        color = GoldSoft, fontSize = 13.sp,
        modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(10.dp))
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        itemsIndexed((1..totalLevels).toList()) { idx, lvl ->
            val unlocked = lvl <= highestUnlocked
            Column(
                Modifier
                    .fillMaxWidth()
                    .noorGlassCard(cornerRadius = 12.dp)
                    .clickable { if (unlocked) onStart(lvl) }
                    .padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (unlocked) {
                    Text(lvl.toArabicDigits(), color = Gold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                } else {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = GoldSoft.copy(alpha = 0.5f), modifier = Modifier.size(18.dp))
                }
                Row {
                    repeat(3) { s ->
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = if (unlocked && s < ((stars / 15).coerceAtMost(3))) Gold else GoldSoft.copy(alpha = 0.25f),
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NamesQuiz(
    names: List<com.elhajri.noor.data.NameOfAllah>,
    level: Int,
    totalLevels: Int,
    score: Int,
    onLevelDone: (earnedScore: Int, stars: Int) -> Unit,
    onExit: () -> Unit
) {
    // Build deterministic questions for this level from the 99 names
    val questions = remember(level) {
        val startIndex = ((level - 1) * 6) % (names.size - 6)
        val slice = names.subList(startIndex, startIndex + 6)
        slice.map { target ->
            val others = names.filter { it.name != target.name }.shuffled(kotlin.random.Random(level * 31 + target.name.hashCode()))
                .take(3).map { it.meaning }
            val options = (others + target.meaning).shuffled(kotlin.random.Random(level * 17 + target.name.hashCode()))
            NamesQuizQuestion(
                prompt = target.name,
                options = options,
                correct = options.indexOf(target.meaning)
            )
        }
    }

    var qIndex by remember { mutableStateOf(0) }
    var correctCount by remember { mutableStateOf(0) }
    var selected by remember { mutableStateOf<Int?>(null) }
    var showResult by remember { mutableStateOf(false) }
    var finished by remember { mutableStateOf(false) }

    if (finished) {
        val stars = when {
            correctCount >= 6 -> 3
            correctCount >= 5 -> 2
            correctCount >= 4 -> 1
            else -> 0
        }
        val earned = correctCount * 20 * level
        Column(
            Modifier.fillMaxSize().noorGlassCard(cornerRadius = 18.dp).padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Gold, modifier = Modifier.size(48.dp))
            Spacer(Modifier.height(10.dp))
            Text("اكتمل المستوى ${level.toArabicDigits()}", color = Gold, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("أجبت ${correctCount.toArabicDigits()} من ٦", color = GoldSoft, fontSize = 13.sp)
            repeat(3) { s ->
                Icon(
                    Icons.Default.Star, contentDescription = null,
                    tint = if (s < stars) Gold else GoldSoft.copy(alpha = 0.25f),
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(Modifier.height(14.dp))
            Button(
                onClick = {
                    onLevelDone(earned, stars)
                    onExit()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Gold.copy(alpha = 0.2f))
            ) { Text("متابعة", color = Gold) }
        }
        return
    }

    val q = questions[qIndex]
    Column(Modifier.fillMaxSize()) {
        // Progress line
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("المستوى ${level.toArabicDigits()}", color = GoldSoft, fontSize = 11.sp)
            Spacer(Modifier.weight(1f))
            Text("${(qIndex + 1).toArabicDigits()} / ٦", color = GoldSoft, fontSize = 11.sp)
        }
        LinearProgressIndicator(
            progress = { (qIndex + 1f) / questions.size },
            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
            color = Gold,
            trackColor = GoldSoft.copy(alpha = 0.15f)
        )
        Spacer(Modifier.height(14.dp))

        Text(q.prompt, color = Gold, fontSize = 26.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Text("ما معنى هذا الاسم؟", color = GoldSoft, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(18.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            q.options.forEachIndexed { i, opt ->
                val isCorrect = i == q.correct
                val bg = when {
                    selected == i && isCorrect -> Color(0xFF1B4332)
                    selected == i && !isCorrect -> Color(0xFF5C1F2A)
                    else -> Color.Transparent
                }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .noorGlassCard(cornerRadius = 14.dp)
                        .clickable {
                            if (selected == null) {
                                selected = i
                                if (isCorrect) correctCount++
                                showResult = true
                            }
                        }
                        .padding(14.dp)
                        .background(bg, RoundedCornerShape(14.dp)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(opt, color = Color.White, fontSize = 14.sp, modifier = Modifier.weight(1f))
                    if (selected != null && isCorrect) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Gold, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }

    LaunchedEffect(showResult) {
        if (showResult) {
            delay(1200)
            showResult = false
            selected = null
            if (qIndex < questions.size - 1) qIndex++ else finished = true
        }
    }
}
