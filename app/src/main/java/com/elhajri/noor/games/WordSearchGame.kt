package com.elhajri.noor.games

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.theme.noorGlassCard
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.Navy
import com.elhajri.noor.ui.NavyCard
import com.elhajri.noor.ui.TextMain
import com.elhajri.noor.ui.TextSecondaryLinen
import com.elhajri.noor.ui.themeScreenBackground
import kotlinx.coroutines.delay

/**
 * Arabic Digits Converter
 */
private fun Int.toArabicDigits(): String {
    val arDigits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
    val str = this.toString()
    val builder = StringBuilder()
    for (ch in str) {
        if (ch in '0'..'9') builder.append(arDigits[ch - '0']) else builder.append(ch)
    }
    return builder.toString()
}

/**
 * Storage constants and SharedPreferences helpers for WordSearch Game progress
 */
private const val PREFS_NAME = "noor_games"
private const val KEY_HIGHEST_UNLOCKED = "word_search_highest_unlocked_v2"
private const val KEY_STARS_PREFIX = "word_search_stars_v2_"
private const val KEY_SCORE_PREFIX = "word_search_score_v2"

private fun getHighestUnlockedLevel(context: Context): Int {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return prefs.getInt(KEY_HIGHEST_UNLOCKED, 1).coerceIn(1, 25)
}

private fun getLevelStars(context: Context, levelNum: Int): Int {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return prefs.getInt("$KEY_STARS_PREFIX$levelNum", 0)
}

private fun saveLevelProgress(context: Context, levelNum: Int, stars: Int, scoreGained: Int) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val currentStars = prefs.getInt("$KEY_STARS_PREFIX$levelNum", 0)
    val editor = prefs.edit()
    if (stars > currentStars) {
        editor.putInt("$KEY_STARS_PREFIX$levelNum", stars)
    }
    val currentUnlocked = prefs.getInt(KEY_HIGHEST_UNLOCKED, 1)
    if (levelNum + 1 > currentUnlocked && levelNum < 25) {
        editor.putInt(KEY_HIGHEST_UNLOCKED, levelNum + 1)
    }
    val currentTotalScore = prefs.getInt(KEY_SCORE_PREFIX, 0)
    editor.putInt(KEY_SCORE_PREFIX, currentTotalScore + scoreGained)
    editor.apply()

    try {
        GameProgressStore.rewardPoints(context, "word_search", levelNum - 1, stars, scoreGained)
    } catch (_: Exception) {}
}

private fun getTotalStars(context: Context): Int {
    var total = 0
    for (i in 1..25) {
        total += getLevelStars(context, i)
    }
    return total
}

private fun getTotalScore(context: Context): Int {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return prefs.getInt(KEY_SCORE_PREFIX, 0)
}

/**
 * Data structures for 25 Islamic levels and Grid placement
 */
private data class WordSearchLevel(
    val id: Int,
    val title: String,
    val subtitle: String,
    val gridSize: Int,
    val words: List<String>
)

private data class PlacedWord(
    val word: String,
    val startRow: Int,
    val startCol: Int,
    val endRow: Int,
    val endCol: Int,
    val cells: List<Pair<Int, Int>>
)

private data class GridData(
    val size: Int,
    val grid: List<List<Char>>,
    val placedWords: List<PlacedWord>
)

/**
 * 25 Islamic Levels definition bank (4 to 5 fair themed words per level)
 */
private val WORD_SEARCH_LEVELS = listOf(
    WordSearchLevel(1, "أسماء الله الحسنى", "ابحث عن أسماء الله الجليلة", 6, listOf("الرحمن", "الرحيم", "الملك", "القدوس")),
    WordSearchLevel(2, "سور قصار", "سور مباركة من القرآن", 6, listOf("الفلق", "الناس", "الكوثر", "النصر", "المسد")),
    WordSearchLevel(3, "أنبياء الله", "أولوا العزم وأنبياء الكرام", 6, listOf("محمد", "موسى", "عيسى", "نوح", "صالح")),
    WordSearchLevel(4, "أذكار اليوم", "كلمات الذكر المستحبة", 7, listOf("سبحان", "الحمد", "التكبير", "التوحيد")),
    WordSearchLevel(5, "أشهر هجرية", "أشهر التقويم الهجري", 7, listOf("رمضان", "محرم", "شوال", "شعبان")),
    WordSearchLevel(6, "الصحابة الكرام", "خلفاء وأبطال الإسلام", 7, listOf("أبوبكر", "عمر", "عثمان", "علي", "بلال")),
    WordSearchLevel(7, "أركان الإسلام", "دعائم الدين الخمس", 7, listOf("الشهادة", "الصلاة", "الزكاة", "الصوم", "الحج")),
    WordSearchLevel(8, "أركان الإيمان", "أركان العقيدة الإسلامية", 7, listOf("الله", "الملائكة", "الكتب", "الرسل", "القدر")),
    WordSearchLevel(9, "غزوات نبوية", "معارك السيرة الخالدة", 7, listOf("بدر", "أحد", "الخندق", "حنين", "خيبر")),
    WordSearchLevel(10, "أمهات المؤمنين", "زوجات النبي الكريم ﷺ", 8, listOf("خديجة", "عائشة", "حفصة", "زينب", "سودة")),
    WordSearchLevel(11, "صلوات مفروضة", "الصلوات الخمس اليومية", 8, listOf("الفجر", "الظهر", "العصر", "المغرب", "العشاء")),
    WordSearchLevel(12, "أخلاق إسلامية", "قيم وشيم المسلم", 8, listOf("الصدق", "الأمانة", "الرحمة", "الصبر", "التواضع")),
    WordSearchLevel(13, "معالم مكة", "أماكن مقدسة بالبيت الحرام", 8, listOf("الكعبة", "الصفا", "المروة", "عرفات")),
    WordSearchLevel(14, "نعيم الجنة", "من أنهار وبساتين الجنة", 8, listOf("سلسبيل", "تسنيم", "الكوثر", "الفردوس")),
    WordSearchLevel(15, "الكتب السماوية", "الكتب المنزلة على الرسل", 8, listOf("القرآن", "التوراة", "الإنجيل", "الزبور")),
    WordSearchLevel(16, "أنبياء ورسل", "أنبياء ذكروا في القرآن", 8, listOf("يوسف", "يونس", "سليمان", "داوود", "يعقوب")),
    WordSearchLevel(17, "سنن وآداب", "سنن اليوم والليلة", 8, listOf("السواك", "الوضوء", "السلام", "الدعاء", "الشكر")),
    WordSearchLevel(18, "صفات الله", "أسماء وصفات حسنى", 8, listOf("الخالق", "الرازق", "الغفور", "الحكيم")),
    WordSearchLevel(19, "ملائكة المقربون", "ملائكة الوحي والعرش", 9, listOf("جبريل", "ميكائيل", "إسرافيل", "مالك", "رضوان")),
    WordSearchLevel(20, "ألقاب الصحابة", "ألقاب شريفة للصحابة", 9, listOf("الصديق", "الفاروق", "الحواري", "أمين", "ترجمان")),
    WordSearchLevel(21, "خلفاء راشدون", "الأئمة المهديون", 9, listOf("أبوبكر", "عمر", "عثمان", "علي")),
    WordSearchLevel(22, "عبادات وطاعات", "قربات وطاعات جليلة", 9, listOf("القيام", "التهجد", "الاعتكاف", "الصدقة", "العمرة")),
    WordSearchLevel(23, "مصطلحات قرآنية", "أسماء وأوصاف القرآن", 9, listOf("التنزيل", "الفرقان", "الذكر", "الهدى", "النور")),
    WordSearchLevel(24, "ثمرات الإيمان", "سكينة وطمأنينة القلب", 9, listOf("الطمأنينة", "السكينة", "الفلاح", "الرشاد", "البركة")),
    WordSearchLevel(25, "ختام القرآن", "أسماء شريفة للكتاب العزيز", 9, listOf("المصحف", "الكتاب", "الشفاء", "المجيد", "البشير"))
)

/**
 * Procedural word search grid generator
 */
private fun generateWordSearchGrid(gridSize: Int, words: List<String>, levelId: Int): GridData {
    val rng = java.util.Random(levelId.toLong() * 31L + 17L)
    val grid = Array(gridSize) { CharArray(gridSize) { ' ' } }
    val placed = mutableListOf<PlacedWord>()

    val directions = listOf(
        Pair(0, 1),
        Pair(1, 0),
        Pair(0, -1),
        Pair(-1, 0),
        Pair(1, 1),
        Pair(1, -1),
        Pair(-1, 1),
        Pair(-1, -1)
    )

    for (word in words) {
        val wordChars = word.toCharArray()
        val len = wordChars.size
        var placedSuccessfully = false

        val shuffledDirs = directions.shuffled(rng)
        for (dir in shuffledDirs) {
            if (placedSuccessfully) break
            val (dr, dc) = dir

            val minR = if (dr < 0) len - 1 else 0
            val maxR = if (dr > 0) gridSize - len else gridSize - 1
            val minC = if (dc < 0) len - 1 else 0
            val maxC = if (dc > 0) gridSize - len else gridSize - 1

            if (minR > maxR || minC > maxC) continue

            val candidates = mutableListOf<Pair<Int, Int>>()
            for (r in minR..maxR) {
                for (c in minC..maxC) {
                    candidates.add(Pair(r, c))
                }
            }
            candidates.shuffle(rng)

            for ((r, c) in candidates) {
                var fits = true
                for (i in 0 until len) {
                    val currR = r + i * dr
                    val currC = c + i * dc
                    val existingChar = grid[currR][currC]
                    if (existingChar != ' ' && existingChar != wordChars[i]) {
                        fits = false
                        break
                    }
                }

                if (fits) {
                    val cellsList = mutableListOf<Pair<Int, Int>>()
                    for (i in 0 until len) {
                        val currR = r + i * dr
                        val currC = c + i * dc
                        grid[currR][currC] = wordChars[i]
                        cellsList.add(Pair(currR, currC))
                    }
                    val endR = r + (len - 1) * dr
                    val endC = c + (len - 1) * dc
                    placed.add(PlacedWord(word, r, c, endR, endC, cellsList))
                    placedSuccessfully = true
                    break
                }
            }
        }

        if (!placedSuccessfully) {
            for (r in 0 until gridSize) {
                if (placedSuccessfully) break
                for (c in 0..(gridSize - len)) {
                    var fits = true
                    for (i in 0 until len) {
                        val existingChar = grid[r][c + i]
                        if (existingChar != ' ' && existingChar != wordChars[i]) {
                            fits = false
                            break
                        }
                    }
                    if (fits) {
                        val cellsList = mutableListOf<Pair<Int, Int>>()
                        for (i in 0 until len) {
                            grid[r][c + i] = wordChars[i]
                            cellsList.add(Pair(r, c + i))
                        }
                        placed.add(PlacedWord(word, r, c, r, c + len - 1, cellsList))
                        placedSuccessfully = true
                        break
                    }
                }
            }
        }
    }

    val fillerLetters = listOf('ا', 'ب', 'ت', 'ث', 'ج', 'ح', 'خ', 'د', 'ذ', 'ر', 'ز', 'س', 'ش', 'ص', 'ض', 'ط', 'ظ', 'ع', 'غ', 'ف', 'ق', 'ك', 'ل', 'م', 'ن', 'ه', 'و', 'ي')
    val finalGrid = List(gridSize) { r ->
        List(gridSize) { c ->
            if (grid[r][c] == ' ') {
                fillerLetters[rng.nextInt(fillerLetters.size)]
            } else {
                grid[r][c]
            }
        }
    }

    return GridData(gridSize, finalGrid, placed)
}

/**
 * Screen state enum
 */
private enum class GameScreen {
    LEVEL_MAP,
    GAMEPLAY,
    LEVEL_WIN,
    LEVEL_FAILED
}

/**
 * Particle structure for Level Win celebration
 */
private data class ConfettiParticle(
    val x: Float,
    var y: Float,
    val speed: Float,
    val radius: Float,
    val color: Color,
    val alpha: Float
)

@Composable
private fun ConfettiEffect(modifier: Modifier = Modifier) {
    val particles = remember {
        val colors = listOf(Gold, GoldSoft, Color(0xFFFFD700), Color(0xFFFFF8DC), Color(0xFFFFA500))
        val list = mutableListOf<ConfettiParticle>()
        val rng = java.util.Random()
        repeat(45) {
            list.add(
                ConfettiParticle(
                    x = rng.nextFloat(),
                    y = rng.nextFloat() * -0.6f,
                    speed = 0.006f + rng.nextFloat() * 0.012f,
                    radius = 4f + rng.nextFloat() * 8f,
                    color = colors[rng.nextInt(colors.size)],
                    alpha = 0.7f + rng.nextFloat() * 0.3f
                )
            )
        }
        list
    }

    var tick by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(16L)
            particles.forEach { p ->
                p.y += p.speed
                if (p.y > 1.2f) {
                    p.y = -0.1f
                }
            }
            tick++
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        particles.forEach { p ->
            drawCircle(
                color = p.color.copy(alpha = p.alpha),
                radius = p.radius,
                center = Offset(p.x * w, p.y * h)
            )
        }
    }
}

/**
 * Shake offset helper for wrong tap pairs
 */
@Composable
private fun rememberShakeOffset(isShaking: Boolean): Float {
    var offset by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(isShaking) {
        if (isShaking) {
            val shakeSeq = listOf(-10f, 10f, -7f, 7f, -4f, 4f, 0f)
            for (dx in shakeSeq) {
                offset = dx
                delay(35L)
            }
        } else {
            offset = 0f
        }
    }
    return offset
}

/**
 * Main WordSearch Game Composable
 */
@Composable
fun WordSearchGame(onBack: () -> Unit) {
    val context = LocalContext.current

    var currentScreen by remember { mutableStateOf(GameScreen.LEVEL_MAP) }
    var selectedLevelId by remember { mutableIntStateOf(1) }

    var unlockedLevel by remember { mutableIntStateOf(getHighestUnlockedLevel(context)) }
    var totalStars by remember { mutableIntStateOf(getTotalStars(context)) }
    var totalScore by remember { mutableIntStateOf(getTotalScore(context)) }

    var gridData by remember { mutableStateOf<GridData?>(null) }
    var foundWords by remember { mutableStateOf<Set<String>>(emptySet()) }
    var selectedFirstCell by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var shakingCells by remember { mutableStateOf<Set<Pair<Int, Int>>>(emptySet()) }

    var remainingTime by remember { mutableIntStateOf(90) }
    var isTimerRunning by remember { mutableStateOf(false) }
    var starsEarned by remember { mutableIntStateOf(0) }
    var timeTaken by remember { mutableIntStateOf(0) }

    fun refreshProgress() {
        unlockedLevel = getHighestUnlockedLevel(context)
        totalStars = getTotalStars(context)
        totalScore = getTotalScore(context)
    }

    fun startLevel(levelId: Int) {
        val lvl = WORD_SEARCH_LEVELS.firstOrNull { it.id == levelId } ?: WORD_SEARCH_LEVELS.first()
        selectedLevelId = lvl.id
        gridData = generateWordSearchGrid(lvl.gridSize, lvl.words, lvl.id)
        foundWords = emptySet()
        selectedFirstCell = null
        shakingCells = emptySet()
        remainingTime = 90
        isTimerRunning = true
        currentScreen = GameScreen.GAMEPLAY
    }

    // Timer loop
    LaunchedEffect(currentScreen, isTimerRunning, remainingTime) {
        if (currentScreen == GameScreen.GAMEPLAY && isTimerRunning && remainingTime > 0) {
            delay(1000L)
            remainingTime -= 1
            if (remainingTime <= 0) {
                isTimerRunning = false
                currentScreen = GameScreen.LEVEL_FAILED
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .themeScreenBackground()
    ) {
        when (currentScreen) {
            GameScreen.LEVEL_MAP -> {
                LevelMapScreen(
                    onBack = onBack,
                    unlockedLevel = unlockedLevel,
                    totalStars = totalStars,
                    totalScore = totalScore,
                    onSelectLevel = { levelId -> startLevel(levelId) }
                )
            }

            GameScreen.GAMEPLAY, GameScreen.LEVEL_WIN, GameScreen.LEVEL_FAILED -> {
                val currentLevel = WORD_SEARCH_LEVELS.first { it.id == selectedLevelId }
                val currentGrid = gridData ?: generateWordSearchGrid(currentLevel.gridSize, currentLevel.words, currentLevel.id)

                val foundCells = remember(foundWords, currentGrid) {
                    val set = mutableSetOf<Pair<Int, Int>>()
                    currentGrid.placedWords.forEach { pw ->
                        if (foundWords.contains(pw.word)) {
                            set.addAll(pw.cells)
                        }
                    }
                    set
                }

                fun handleCellTap(r: Int, c: Int) {
                    val tapped = Pair(r, c)
                    if (selectedFirstCell == null) {
                        selectedFirstCell = tapped
                    } else if (selectedFirstCell == tapped) {
                        selectedFirstCell = null
                    } else {
                        val firstCell = selectedFirstCell!!
                        val matchWord = currentGrid.placedWords.firstOrNull { pw ->
                            !foundWords.contains(pw.word) && (
                                (pw.startRow == firstCell.first && pw.startCol == firstCell.second && pw.endRow == r && pw.endCol == c) ||
                                (pw.startRow == r && pw.startCol == c && pw.endRow == firstCell.first && pw.endCol == firstCell.second)
                            )
                        }

                        if (matchWord != null) {
                            val updated = foundWords + matchWord.word
                            foundWords = updated
                            selectedFirstCell = null

                            if (updated.size == currentLevel.words.size) {
                                isTimerRunning = false
                                timeTaken = 90 - remainingTime
                                starsEarned = when {
                                    timeTaken < 40 -> 3
                                    timeTaken < 70 -> 2
                                    else -> 1
                                }
                                val scoreGained = (starsEarned * 50) + (remainingTime * 2)
                                saveLevelProgress(context, currentLevel.id, starsEarned, scoreGained)
                                refreshProgress()
                                currentScreen = GameScreen.LEVEL_WIN
                            }
                        } else {
                            shakingCells = setOf(firstCell, tapped)
                            selectedFirstCell = null
                        }
                    }
                }

                // Shake reset effect
                LaunchedEffect(shakingCells) {
                    if (shakingCells.isNotEmpty()) {
                        delay(350L)
                        shakingCells = emptySet()
                    }
                }

                GameplayScreen(
                    level = currentLevel,
                    gridData = currentGrid,
                    foundWords = foundWords,
                    foundCells = foundCells,
                    selectedFirstCell = selectedFirstCell,
                    shakingCells = shakingCells,
                    remainingTime = remainingTime,
                    onCellTap = { r, c -> handleCellTap(r, c) },
                    onBackToMap = {
                        isTimerRunning = false
                        refreshProgress()
                        currentScreen = GameScreen.LEVEL_MAP
                    },
                    onRestart = { startLevel(currentLevel.id) }
                )

                if (currentScreen == GameScreen.LEVEL_WIN) {
                    ConfettiEffect()
                    LevelWinDialog(
                        level = currentLevel,
                        stars = starsEarned,
                        timeTaken = timeTaken,
                        foundWords = foundWords.toList(),
                        onNextLevel = {
                            if (currentLevel.id < 25) {
                                startLevel(currentLevel.id + 1)
                            } else {
                                refreshProgress()
                                currentScreen = GameScreen.LEVEL_MAP
                            }
                        },
                        onMap = {
                            refreshProgress()
                            currentScreen = GameScreen.LEVEL_MAP
                        }
                    )
                }

                if (currentScreen == GameScreen.LEVEL_FAILED) {
                    LevelFailedDialog(
                        level = currentLevel,
                        onRetry = { startLevel(currentLevel.id) },
                        onMap = {
                            refreshProgress()
                            currentScreen = GameScreen.LEVEL_MAP
                        }
                    )
                }
            }
        }
    }
}

/**
 * Level Map Screen Composable
 */
@Composable
private fun LevelMapScreen(
    onBack: () -> Unit,
    unlockedLevel: Int,
    totalStars: Int,
    totalScore: Int,
    onSelectLevel: (Int) -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "رجوع",
                    tint = Gold
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "صيد الكلمات",
                    color = Gold,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "٢٥ مرحلة إسلامية شيقة",
                    color = TextSecondaryLinen,
                    fontSize = 12.sp
                )
            }
        }

        // Summary Glass Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .noorGlassCard(cornerRadius = 18.dp)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("النجوم", color = TextSecondaryLinen, fontSize = 12.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Gold, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("${totalStars.toArabicDigits()} / ٧٥", color = Gold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Box(modifier = Modifier.width(1.dp).height(32.dp).background(Gold.copy(alpha = 0.3f)))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("المراحل", color = TextSecondaryLinen, fontSize = 12.sp)
                    Text("${unlockedLevel.toArabicDigits()} / ٢٥", color = Gold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                Box(modifier = Modifier.width(1.dp).height(32.dp).background(Gold.copy(alpha = 0.3f)))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("النقاط", color = TextSecondaryLinen, fontSize = 12.sp)
                    Text("${totalScore.toArabicDigits()}", color = Gold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Level Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(WORD_SEARCH_LEVELS) { lvl ->
                val isUnlocked = lvl.id <= unlockedLevel
                val stars = getLevelStars(context, lvl.id)

                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .noorGlassCard(cornerRadius = 16.dp)
                        .border(
                            width = if (isUnlocked) 1.5.dp else 0.5.dp,
                            color = if (isUnlocked) Gold.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable(enabled = isUnlocked) { onSelectLevel(lvl.id) }
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        if (isUnlocked) {
                            Text(
                                text = lvl.id.toArabicDigits(),
                                color = Gold,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = lvl.title,
                                color = TextMain,
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                            Spacer(Modifier.height(4.dp))
                            Row {
                                repeat(3) { index ->
                                    Icon(
                                        Icons.Default.Star,
                                        contentDescription = null,
                                        tint = if (index < stars) Gold else Color.Gray.copy(alpha = 0.4f),
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        } else {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = "مقفل",
                                tint = TextSecondaryLinen.copy(alpha = 0.5f),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "المرحلة ${lvl.id.toArabicDigits()}",
                                color = TextSecondaryLinen.copy(alpha = 0.6f),
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Gameplay Screen Composable
 */
@Composable
private fun GameplayScreen(
    level: WordSearchLevel,
    gridData: GridData,
    foundWords: Set<String>,
    foundCells: Set<Pair<Int, Int>>,
    selectedFirstCell: Pair<Int, Int>?,
    shakingCells: Set<Pair<Int, Int>>,
    remainingTime: Int,
    onCellTap: (Int, Int) -> Unit,
    onBackToMap: () -> Unit,
    onRestart: () -> Unit
) {
    val gridSize = gridData.size
    val fontSizeSp = when (gridSize) {
        6 -> 20.sp
        7 -> 18.sp
        8 -> 16.sp
        else -> 14.sp
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBackToMap) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = Gold)
            }

            // Level Badge
            Box(
                modifier = Modifier
                    .noorGlassCard(cornerRadius = 14.dp)
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "المرحلة ${level.id.toArabicDigits()}",
                    color = Gold,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Timer Badge
            Box(
                modifier = Modifier
                    .noorGlassCard(cornerRadius = 14.dp)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Timer,
                        contentDescription = null,
                        tint = if (remainingTime <= 15) Color(0xFFEF5350) else Gold,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "${remainingTime.toArabicDigits()}ث",
                        color = if (remainingTime <= 15) Color(0xFFEF5350) else TextMain,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            IconButton(onClick = onRestart) {
                Icon(Icons.Default.Refresh, contentDescription = "إعادة", tint = Gold)
            }
        }

        Spacer(Modifier.height(8.dp))

        // Level Title & Subtitle Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .noorGlassCard(cornerRadius = 16.dp)
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = level.title,
                    color = Gold,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = level.subtitle,
                    color = TextSecondaryLinen,
                    fontSize = 12.sp
                )
                Spacer(Modifier.height(8.dp))

                // Progress Indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "الكلمات المكتشفة: ${foundWords.size.toArabicDigits()} / ${level.words.size.toArabicDigits()}",
                        color = TextMain,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { foundWords.size.toFloat() / level.words.size.toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = Gold,
                    trackColor = Gold.copy(alpha = 0.2f)
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        // Target Words Chips Row
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(level.words) { word ->
                val isFound = foundWords.contains(word)
                Box(
                    modifier = Modifier
                        .noorGlassCard(cornerRadius = 12.dp)
                        .background(if (isFound) Gold.copy(alpha = 0.25f) else Color.Transparent)
                        .border(
                            width = 1.dp,
                            color = if (isFound) Gold else Gold.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isFound) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Gold, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                        }
                        Text(
                            text = word,
                            color = if (isFound) Gold else TextMain,
                            fontSize = 13.sp,
                            fontWeight = if (isFound) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Instruction Text
        Text(
            text = "انقر على الحرف الأول ثم الحرف الأخير للكلمة 🎯",
            color = GoldSoft,
            fontSize = 12.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        // TAP-PAIR Grid Container with Canvas Overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .noorGlassCard(cornerRadius = 18.dp)
                    .padding(8.dp)
            ) {
                // Canvas Overlay for Gold Connecting Lines
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cw = size.width / gridSize
                    val ch = size.height / gridSize

                    gridData.placedWords.forEach { pw ->
                        if (foundWords.contains(pw.word)) {
                            val x1 = pw.startCol * cw + cw / 2f
                            val y1 = pw.startRow * ch + ch / 2f
                            val x2 = pw.endCol * cw + cw / 2f
                            val y2 = pw.endRow * ch + ch / 2f

                            // Glow line behind
                            drawLine(
                                color = GoldSoft.copy(alpha = 0.45f),
                                start = Offset(x1, y1),
                                end = Offset(x2, y2),
                                strokeWidth = 14f,
                                cap = StrokeCap.Round
                            )
                            // Core Gold line
                            drawLine(
                                color = Gold,
                                start = Offset(x1, y1),
                                end = Offset(x2, y2),
                                strokeWidth = 6f,
                                cap = StrokeCap.Round
                            )
                        }
                    }
                }

                // Interactive Grid Cells
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (r in 0 until gridSize) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            for (c in 0 until gridSize) {
                                val letter = gridData.grid[r][c]
                                val cellPos = Pair(r, c)
                                val isSelected = (selectedFirstCell == cellPos)
                                val isShaking = shakingCells.contains(cellPos)
                                val isFound = foundCells.contains(cellPos)

                                val shakeDx = rememberShakeOffset(isShaking)

                                val cardBg = when {
                                    isFound -> Gold
                                    isSelected -> Gold.copy(alpha = 0.35f)
                                    isShaking -> Color(0xFFEF5350).copy(alpha = 0.35f)
                                    else -> NavyCard
                                }

                                val textColor = when {
                                    isFound -> Navy
                                    isSelected -> Gold
                                    isShaking -> Color.White
                                    else -> TextMain
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .offset(x = shakeDx.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(cardBg)
                                        .border(
                                            width = if (isFound || isSelected) 2.dp else 1.dp,
                                            color = when {
                                                isFound -> GoldSoft
                                                isSelected -> Gold
                                                isShaking -> Color(0xFFEF5350)
                                                else -> Gold.copy(alpha = 0.2f)
                                            },
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable { onCellTap(r, c) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = letter.toString(),
                                        color = textColor,
                                        fontSize = fontSizeSp,
                                        fontWeight = if (isFound || isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Level Win Celebration Dialog Overlay
 */
@Composable
private fun LevelWinDialog(
    level: WordSearchLevel,
    stars: Int,
    timeTaken: Int,
    foundWords: List<String>,
    onNextLevel: () -> Unit,
    onMap: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .noorGlassCard(cornerRadius = 24.dp)
                .border(1.5.dp, Gold, RoundedCornerShape(24.dp))
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = Gold,
                    modifier = Modifier.size(54.dp)
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "ما شاء الله! اكتملت المرحلة!",
                    color = Gold,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = level.title,
                    color = TextSecondaryLinen,
                    fontSize = 14.sp
                )

                Spacer(Modifier.height(12.dp))

                // Stars Row
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    repeat(3) { index ->
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = if (index < stars) Gold else Color.Gray.copy(alpha = 0.4f),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "الوقت المستغرق: ${timeTaken.toArabicDigits()} ثانية",
                    color = TextMain,
                    fontSize = 13.sp
                )

                Spacer(Modifier.height(12.dp))

                // Found Words list summary
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .noorGlassCard(cornerRadius = 14.dp)
                        .padding(10.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("الكلمات المستخرجة:", color = GoldSoft, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = foundWords.joinToString("  •  "),
                            color = TextMain,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onMap,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyCard),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("خارطة المراحل", color = TextMain, fontSize = 13.sp)
                    }

                    Button(
                        onClick = onNextLevel,
                        modifier = Modifier
                            .weight(1.2f)
                            .height(46.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Gold),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = if (level.id < 25) "المرحلة التالية" else "إنهاء Game",
                            color = Navy,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Level Failed Dialog Overlay
 */
@Composable
private fun LevelFailedDialog(
    level: WordSearchLevel,
    onRetry: () -> Unit,
    onMap: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .noorGlassCard(cornerRadius = 24.dp)
                .border(1.5.dp, Color(0xFFEF5350), RoundedCornerShape(24.dp))
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.Timer,
                    contentDescription = null,
                    tint = Color(0xFFEF5350),
                    modifier = Modifier.size(54.dp)
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "انتهى الوقت!",
                    color = Color(0xFFEF5350),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "حاول مرة أخرى لاكتشاف جميع كلمات ${level.title}.",
                    color = TextSecondaryLinen,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onMap,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyCard),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("الخارطة", color = TextMain, fontSize = 13.sp)
                    }

                    Button(
                        onClick = onRetry,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Gold),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("إعادة المحاولة", color = Navy, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
