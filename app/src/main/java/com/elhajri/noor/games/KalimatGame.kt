package com.elhajri.noor.games

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.Navy
import com.elhajri.noor.ui.NavyCard

/**
 * "كلمة من الذكر" — لغز إيماني متعدد المستويات بأسلوب Wordle.
 * تخمين أسماء الله الحسنى والكلمات القرآنية بتدرج الصعوبة (20 مستوى).
 */
private data class KalimaLevel(
    val levelNum: Int,
    val word: String,
    val meaning: String,
    val dalil: String
)

private val kalimatLevels = listOf(
    // 4 حروف - سهل (1 - 5)
    KalimaLevel(1, "الحق", "الذي لا يرتاب في وجوده ولا في إلهيته", "فَتَعَالَى اللَّهُ الْمَلِكُ الْحَقُّ"),
    KalimaLevel(2, "العلي", "الذي له العلو المطلق في ذاته وصفاته", "وَهُوَ الْعَلِيُّ الْعَظِيمُ"),
    KalimaLevel(3, "الصمد", "المقصود في الحوائج كلها ولا يُستغنى عنه", "اللَّهُ الصَّمَدُ"),
    KalimaLevel(4, "الأول", "الذي ليس قبله شيء والآخر الذي ليس بعده شيء", "هُوَ الْأَوَّلُ وَالْآخِرُ"),
    KalimaLevel(5, "البر", "كثير الإحسان واللطف بعباده", "إِنَّهُ هُوَ الْبَرُّ الرَّحِيمُ"),

    // 5 حروف - متوسط (6 - 10)
    KalimaLevel(6, "الوهاب", "كثير العطاء بلا مقابل ولا عوض", "إِنَّكَ أَنتَ الْوَهَّابُ"),
    KalimaLevel(7, "العليم", "المحيط علمه بكل شيء ظاهراً وباطناً", "وَهُوَ الْعَلِيمُ الْحَكِيمُ"),
    KalimaLevel(8, "الكريم", "الذي يعطي بلا سؤال ويغفر الذنوب", "يَا أَيُّهَا الْإِنسَانُ مَا غَرَّكَ بِرَبِّكَ الْكَرِيمِ"),
    KalimaLevel(9, "الودود", "يحب عباده الصالحين ويحبونه", "وَهُوَ الْغَفُورُ الْوَدُودُ"),
    KalimaLevel(10, "الحفيظ", "يحفظ عباده وأعمالهم من الضياع", "إِنَّ رَبِّي عَلَىٰ كُلِّ شَيْءٍ حَفِيظٌ"),

    // 6 حروف - متقدم (11 - 15)
    KalimaLevel(11, "الرزاق", "المتكفل بأرزاق الخلائق أجمعين", "إِنَّ اللَّهَ هُوَ الرَّزَّاقُ ذُو الْقُوَّةِ الْمَتِينُ"),
    KalimaLevel(12, "الفتاح", "يفتح أبواب الرحمة والرزق والفرَج", "وَهُوَ الْفَتَّاحُ الْعَلِيمُ"),
    KalimaLevel(13, "الحكيم", "يضع الأمور في مواضعها الصحيحة بحكمة", "وَكَانَ اللَّهُ عَلِيمًا حَكِيمًا"),
    KalimaLevel(14, "الخبير", "الذي يعلم دقائق الأمور وبواطنها", "وَهُوَ الْحَكِيمُ الْخَبِيرُ"),
    KalimaLevel(15, "اللطيف", "يصل لطفه إلى عباده من حيث لا يشعرون", "اللَّهُ لَطِيفٌ بِعِبَادِهِ"),

    // 7+ حروف - خبير (16 - 20)
    KalimaLevel(16, "السلام", "السالم من كل نقص وأمان عباده", "السَّلَامُ الْمُؤْمِنُ الْمُهَيْمِنُ"),
    KalimaLevel(17, "العزيز", "القوي الغالب الذي لا يُقهر", "وَهُوَ الْعَزِيزُ الْحَكِيمُ"),
    KalimaLevel(18, "الجبار", "يجبر الكسير ويقهر العتاة", "الْجَبَّارُ الْمُتَكَبِّرُ"),
    KalimaLevel(19, "الرؤوف", "المتصف بعظيم الرحمة والعطف", "وَأَنَّ اللَّهَ رَؤُوفٌ رَّحِيمٌ"),
    KalimaLevel(20, "المتكبر", "المتعالي عن السوء والنقص والخلق", "الْعَزِيزُ الْجَبَّارُ الْمُتَكَبِّرُ")
)

// صفوف لوحة المفاتيح العربية
private val row1Letters = "ضصثقفغعهخحجد".map { it.toString() }
private val row2Letters = "شسيبلاتنمكط".map { it.toString() }
private val row3Letters = listOf("ئ", "ء", "ؤ", "ر", "ى", "ة", "و", "ز", "ظ")

enum class LetterState { UNGUESSED, NOT_IN_WORD, IN_WORD_WRONG_SPOT, CORRECT_SPOT }

@Composable
fun KalimatGameScreen(onBack: () -> Unit) {
    ForcePortraitOrientation()

    val context = LocalContext.current
    var currentLevelIdx by remember { mutableIntStateOf(0) }
    var inPlay by remember { mutableStateOf(false) }

    // Refresh state key to update stats after finishing a level
    var refreshKey by remember { mutableIntStateOf(0) }

    val highestLevel = remember(refreshKey) { GameProgressStore.getHighestLevel(context, GameProgressStore.GAME_KALIMAT) }
    val totalStars = remember(refreshKey) { GameProgressStore.getTotalStars(context, GameProgressStore.GAME_KALIMAT, kalimatLevels.size) }
    val totalScore = remember(refreshKey) { GameProgressStore.getTotalScore(context, GameProgressStore.GAME_KALIMAT) }
    val streak = remember(refreshKey) { GameProgressStore.getStreak(context, GameProgressStore.GAME_KALIMAT) }

    Box(Modifier.fillMaxSize().background(Navy)) {
        Column(Modifier.fillMaxSize()) {
            // Header
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    if (inPlay) inPlay = false else onBack()
                }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = Gold)
                }
                Column(Modifier.weight(1f)) {
                    Text("كلمة من الذكر", color = Gold, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("المستوى ${currentLevelIdx + 1} / ${kalimatLevels.size} | ⭐ $totalStars | 🌙 $totalScore | 🔥 $streak أعياد", color = GoldSoft, fontSize = 11.sp)
                }
            }

            if (!inPlay) {
                // خريطة المراحل
                Text(
                    "المراحل الإيمانية",
                    color = Gold, fontSize = 15.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    contentPadding = PaddingValues(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    itemsIndexed(kalimatLevels) { index, lvl ->
                        val isUnlocked = index <= highestLevel
                        val starsEarned = GameProgressStore.getLevelStars(context, GameProgressStore.GAME_KALIMAT, index)

                        Box(
                            Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isUnlocked) NavyCard else NavyCard.copy(alpha = 0.4f))
                                .border(
                                    1.dp,
                                    if (starsEarned > 0) Gold else if (isUnlocked) Gold.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.1f),
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable(enabled = isUnlocked) {
                                    currentLevelIdx = index
                                    inPlay = true
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                if (isUnlocked) {
                                    Text("${lvl.levelNum}", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.height(2.dp))
                                    Row {
                                        for (s in 1..3) {
                                            Icon(
                                                Icons.Default.Star,
                                                contentDescription = null,
                                                tint = if (s <= starsEarned) Gold else Color.White.copy(alpha = 0.2f),
                                                modifier = Modifier.size(11.dp)
                                            )
                                        }
                                    }
                                } else {
                                    Icon(Icons.Default.Lock, contentDescription = "مقفل", tint = GoldSoft.copy(alpha = 0.4f), modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            } else {
                KalimatPlayScreen(
                    level = kalimatLevels[currentLevelIdx],
                    onLevelComplete = { triesUsed ->
                        val stars = when {
                            triesUsed <= 3 -> 3
                            triesUsed <= 5 -> 2
                            else -> 1
                        }
                        val pts = (6 - triesUsed + 1) * 20
                        GameProgressStore.rewardPoints(context, GameProgressStore.GAME_KALIMAT, currentLevelIdx, stars, pts)
                        refreshKey++
                    },
                    onNextLevel = {
                        if (currentLevelIdx + 1 < kalimatLevels.size) {
                            currentLevelIdx++
                        } else {
                            inPlay = false
                        }
                    },
                    onExit = { inPlay = false }
                )
            }
        }
    }
}

@Composable
private fun KalimatPlayScreen(
    level: KalimaLevel,
    onLevelComplete: (triesUsed: Int) -> Unit,
    onNextLevel: () -> Unit,
    onExit: () -> Unit
) {
    val target = level.word
    val len = target.length
    val maxTries = 6

    var guesses by remember(level) { mutableStateOf(listOf<String>()) }
    var current by remember(level) { mutableStateOf("") }
    var won by remember(level) { mutableStateOf(false) }
    var lost by remember(level) { mutableStateOf(false) }

    fun submit() {
        if (current.length != len) return
        val newGuesses = guesses + current
        guesses = newGuesses
        if (current == target) {
            won = true
            onLevelComplete(newGuesses.size)
        } else if (newGuesses.size >= maxTries) {
            lost = true
        }
        current = ""
    }

    Column(
        Modifier.fillMaxSize().padding(horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("المستوى ${level.levelNum}: كلمة من ${len} حروف", color = Gold, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))

        // شبكة التخمين
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            for (r in 0 until maxTries) {
                val rowWord = when {
                    r < guesses.size -> guesses[r]
                    r == guesses.size && !won && !lost -> current
                    else -> ""
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (c in 0 until len) {
                        val ch = rowWord.getOrNull(c)?.toString() ?: ""
                        val (bg, fg) = when {
                            r < guesses.size -> {
                                val t = target
                                when {
                                    t.getOrNull(c)?.toString() == ch -> Color(0xFFD4AF37) to Navy
                                    ch.isNotEmpty() && t.contains(ch) -> Color(0xFFF3E5AB) to Navy
                                    ch.isNotEmpty() -> NavyCard.copy(alpha = 0.5f) to Color.White.copy(alpha = 0.35f)
                                    else -> NavyCard to Color.Transparent
                                }
                            }
                            else -> NavyCard to if (ch.isEmpty()) Color.Transparent else Color.White
                        }
                        val flip by animateFloatAsState(
                            targetValue = if (r < guesses.size) 1f else 0f,
                            animationSpec = tween(350), label = "flip"
                        )
                        Box(
                            Modifier
                                .size(44.dp)
                                .graphicsLayer { rotationX = (1f - flip) * 90f }
                                .clip(RoundedCornerShape(10.dp))
                                .background(bg)
                                .border(1.dp, Gold.copy(alpha = 0.35f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(ch, color = fg, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(Modifier.height(5.dp))
            }
        }

        Spacer(Modifier.weight(1f))

        if (won || lost) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(NavyCard)
                    .border(1.dp, Gold, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Text(
                    if (won) "ما شاء الله! أحسنت 🌙" else "الكلمة هي: ${target}",
                    color = Gold, fontSize = 17.sp, fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(6.dp))
                Text(level.meaning, color = Color.White, fontSize = 13.sp, textAlign = TextAlign.Center)
                Text("﴿${level.dalil}﴾", color = GoldSoft, fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 4.dp))

                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Gold.copy(alpha = 0.2f))
                            .border(1.dp, Gold, RoundedCornerShape(10.dp))
                            .clickable { onExit() }
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                        Text("قائمة المراحل", color = Gold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    if (won) {
                        Box(
                            Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Gold)
                                .clickable { onNextLevel() }
                                .padding(horizontal = 24.dp, vertical = 8.dp)
                        ) {
                            Text("المستوى التالي", color = Navy, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        } else {
            // لوحة المفاتيح العربية الاحترافية
            BeautifulArabicKeyboard(
                onLetter = { if (current.length < len) current += it },
                onDelete = { if (current.isNotEmpty()) current = current.dropLast(1) },
                onEnter = { submit() },
                target = target,
                guesses = guesses
            )
        }
        Spacer(Modifier.height(10.dp))
    }
}

/**
 * Beautiful Custom Arabic Keyboard matching the app theme:
 * Glass keys, subtle 3D depth, ripple press, 3-row layout + control bar, professional spacing.
 */
@Composable
private fun BeautifulArabicKeyboard(
    onLetter: (String) -> Unit,
    onDelete: () -> Unit,
    onEnter: () -> Unit,
    target: String,
    guesses: List<String>
) {
    fun letterState(ch: String): LetterState {
        if (guesses.isEmpty()) return LetterState.UNGUESSED
        val inTarget = target.contains(ch)
        val inCorrectSpot = guesses.any { g ->
            g.mapIndexedNotNull { i, c ->
                if (c.toString() == ch && target.getOrNull(i)?.toString() == ch) ch else null
            }.isNotEmpty()
        }
        val inAnyGuess = guesses.any { it.contains(ch) }

        return when {
            inCorrectSpot -> LetterState.CORRECT_SPOT
            inTarget && inAnyGuess -> LetterState.IN_WORD_WRONG_SPOT
            inAnyGuess -> LetterState.NOT_IN_WORD
            else -> LetterState.UNGUESSED
        }
    }

    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(NavyCard.copy(alpha = 0.6f))
            .border(1.dp, Gold.copy(alpha = 0.25f), RoundedCornerShape(18.dp))
            .padding(vertical = 10.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Row 1
        KeyboardRow(letters = row1Letters, letterStateProvider = { letterState(it) }, onLetter = onLetter)
        // Row 2
        KeyboardRow(letters = row2Letters, letterStateProvider = { letterState(it) }, onLetter = onLetter)
        // Row 3
        KeyboardRow(letters = row3Letters, letterStateProvider = { letterState(it) }, onLetter = onLetter)

        // Bottom Controls Bar (Backspace + Guess Button)
        Row(
            Modifier
                .fillMaxWidth()
                .padding(start = 6.dp, end = 6.dp, top = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Delete Key (Glass Style with 3D border)
            Box(
                Modifier
                    .weight(1f)
                    .height(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Navy.copy(alpha = 0.7f))
                    .border(1.dp, Gold.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                    .clickable { onDelete() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Backspace, contentDescription = "حذف", tint = Gold, modifier = Modifier.size(20.dp))
            }

            // Guess Key (Gold Glass Button)
            Box(
                Modifier
                    .weight(2f)
                    .height(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Gold)
                    .clickable { onEnter() },
                contentAlignment = Alignment.Center
            ) {
                Text("تخمين الكلمة 🌙", color = Navy, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun KeyboardRow(
    letters: List<String>,
    letterStateProvider: (String) -> LetterState,
    onLetter: (String) -> Unit
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterHorizontally)
    ) {
        letters.forEach { ch ->
            val state = letterStateProvider(ch)
            val (bgColor, textColor, borderColor) = when (state) {
                LetterState.CORRECT_SPOT -> Triple(Gold, Navy, Gold)
                LetterState.IN_WORD_WRONG_SPOT -> Triple(Color(0xFFF3E5AB), Navy, Gold)
                LetterState.NOT_IN_WORD -> Triple(Navy.copy(alpha = 0.4f), Color.White.copy(alpha = 0.25f), Color.Transparent)
                LetterState.UNGUESSED -> Triple(NavyCard.copy(alpha = 0.6f), Color.White, Gold.copy(alpha = 0.2f))
            }

            Box(
                Modifier
                    .weight(1f, fill = false)
                    .widthIn(min = 26.dp, max = 34.dp)
                    .height(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(bgColor)
                    .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = rememberRipple(bounded = true, color = Gold),
                        onClick = { onLetter(ch) }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(ch, color = textColor, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
