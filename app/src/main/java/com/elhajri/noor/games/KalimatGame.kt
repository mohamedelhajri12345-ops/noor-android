package com.elhajri.noor.games

import android.content.Context
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
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
import com.elhajri.noor.ui.TextMain
import com.elhajri.noor.ui.TextSecondaryLinen
import com.elhajri.noor.ui.noorGlassCard
import com.elhajri.noor.ui.themeScreenBackground
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * KalimatGame.kt — "كلمة من الذكر"
 * Redesigned 25-level Islamic Arabic word guessing game with curated levels,
 * word length hints, meaning hints, category badges, fixed pre-shown diacritics,
 * 6 glowing lantern allowances with shake animations, reveal whispers (همسة),
 * and gold confetti win celebrations. Progress saved in SharedPreferences("noor_games").
 */

private data class KalimaLevel(
    val id: Int,
    val word: String,
    val vocalized: String,
    val category: String,
    val meaning: String,
    val diacritics: List<String>
)

private val kalimatLevels = listOf(
    KalimaLevel(
        id = 1,
        word = "الرحيم",
        vocalized = "الرَّحِيمُ",
        category = "اسم من أسماء الله الحسنى",
        meaning = "ذو الرحمة الواسعة الشاملة لجميع خلقه والخاصة بالمؤمنين",
        diacritics = listOf("", "ْ", "َّ", "ِ", "", "ُ")
    ),
    KalimaLevel(
        id = 2,
        word = "الكعبة",
        vocalized = "الْكَعْبَةُ",
        category = "معلم إسلامي",
        meaning = "قبلة المسلمين وبيته الحرام المبارك في مكة المكرمة",
        diacritics = listOf("", "ْ", "َ", "ْ", "َ", "ُ")
    ),
    KalimaLevel(
        id = 3,
        word = "الكهف",
        vocalized = "الْكَهْفُ",
        category = "سورة قرآنية",
        meaning = "سورة قرآنية مباركة تُقرأ يوم الجمعة وتُعصم من الفتن",
        diacritics = listOf("", "ْ", "َ", "ْ", "ُ")
    ),
    KalimaLevel(
        id = 4,
        word = "إبراهيم",
        vocalized = "إِبْرَاهِيمُ",
        category = "نبي ورسول",
        meaning = "خليل الله وأبو الأنبياء عليه السلام وباني البيت الحرام",
        diacritics = listOf("ِ", "ْ", "َ", "", "ِ", "", "ُ")
    ),
    KalimaLevel(
        id = 5,
        word = "القدوس",
        vocalized = "الْقُدُّوسُ",
        category = "اسم من أسماء الله الحسنى",
        meaning = "المنزه عن كل نقص وعيب، الطاهر المنزه العلي",
        diacritics = listOf("", "ْ", "ُ", "ُّ", "", "ُ")
    ),
    KalimaLevel(
        id = 6,
        word = "الفرقان",
        vocalized = "الْفُرْقَانُ",
        category = "اسم للقرآن الكريم",
        meaning = "الكتاب المبارك الذي يفرق الله به بين الحق والباطل",
        diacritics = listOf("", "ْ", "ُ", "ْ", "َ", "", "ُ")
    ),
    KalimaLevel(
        id = 7,
        word = "الزمزم",
        vocalized = "الزَّمْزَمُ",
        category = "معلم إسلامي",
        meaning = "الماء المبارك بمكة المكرمة الذي نبع لإسماعيل عليه السلام",
        diacritics = listOf("", "ْ", "َّ", "ْ", "َ", "ُ")
    ),
    KalimaLevel(
        id = 8,
        word = "البر",
        vocalized = "الْبِرُّ",
        category = "خلق إسلامي",
        meaning = "جماع الخير والطاعة وحسن الخلق والإحسان إلى الناس",
        diacritics = listOf("", "ْ", "ِ", "ُّ")
    ),
    KalimaLevel(
        id = 9,
        word = "الرحمن",
        vocalized = "الرَّحْمَنُ",
        category = "اسم من أسماء الله الحسنى",
        meaning = "الذي وسعت رحمته كل شيء في السماوات والأرض",
        diacritics = listOf("", "ْ", "َّ", "ْ", "َ", "ُ")
    ),
    KalimaLevel(
        id = 10,
        word = "محمد",
        vocalized = "مُحَمَّدٌ",
        category = "نبي ورسول",
        meaning = "خاتم الأنبياء والمرسلين والرحمة المهداة للعالمين ﷺ",
        diacritics = listOf("ُ", "َ", "َّ", "ٌ")
    ),
    KalimaLevel(
        id = 11,
        word = "السلام",
        vocalized = "السَّلَامُ",
        category = "اسم من أسماء الله الحسنى",
        meaning = "السالم من كل عيب والنقص والمفيض بالأمان والسكينة على خلقه",
        diacritics = listOf("", "ْ", "َّ", "َ", "", "ُ")
    ),
    KalimaLevel(
        id = 12,
        word = "الفاتحة",
        vocalized = "الْفَاتِحَةُ",
        category = "سورة قرآنية",
        meaning = "أعظم سورة في القرآن الكريم وتسمى أم الكتاب ورقية المريض",
        diacritics = listOf("", "ْ", "َ", "", "ِ", "َ", "ُ")
    ),
    KalimaLevel(
        id = 13,
        word = "الأقصى",
        vocalized = "الأَقْصَى",
        category = "معلم إسلامي",
        meaning = "المسجد المبارك في فلسطين أولى القبلتين ومسرى النبي ﷺ",
        diacritics = listOf("", "ْ", "َ", "ْ", "َ", "ى")
    ),
    KalimaLevel(
        id = 14,
        word = "يوسف",
        vocalized = "يُوسُفُ",
        category = "نبي ورسول",
        meaning = "نبي الله الصديق صاحب الأحلام وتفسير الرؤى عليه السلام",
        diacritics = listOf("ُ", "", "ُ", "ُ")
    ),
    KalimaLevel(
        id = 15,
        word = "السميع",
        vocalized = "السَّمِيعُ",
        category = "اسم من أسماء الله الحسنى",
        meaning = "الذي يسمع كل أصوات المخلوقات ودعاء الداعين بلا خفاء",
        diacritics = listOf("", "ْ", "َّ", "ِ", "", "ُ")
    ),
    KalimaLevel(
        id = 16,
        word = "التقوى",
        vocalized = "التَّقْوَى",
        category = "مفهوم إسلامي",
        meaning = "امتثال أوامر الله واجتناب نواهيه خشية ورجاءً",
        diacritics = listOf("", "ْ", "َّ", "ْ", "َ", "ى")
    ),
    KalimaLevel(
        id = 17,
        word = "الكوثر",
        vocalized = "الْكَوْثَرُ",
        category = "سورة قرآنية",
        meaning = "نهر الخيرات العظيمة المُعطى للنبي ﷺ وأقصر سورة بالقرآن",
        diacritics = listOf("", "ْ", "َ", "ْ", "َ", "ُ")
    ),
    KalimaLevel(
        id = 18,
        word = "موسى",
        vocalized = "مُوسَى",
        category = "نبي ورسول",
        meaning = "كليم الله الذي أيده الله بالمعجزات العظيمة وآتاه التوراة",
        diacritics = listOf("ُ", "", "َ", "ى")
    ),
    KalimaLevel(
        id = 19,
        word = "الرزاق",
        vocalized = "الرَّزَّاقُ",
        category = "اسم من أسماء الله الحسنى",
        meaning = "المتكفل بأرزاق العباد والخلائق أجمعين واسع الفضل",
        diacritics = listOf("", "ْ", "َّ", "َّ", "", "ُ")
    ),
    KalimaLevel(
        id = 20,
        word = "الروضة",
        vocalized = "الرَّوْضَةُ",
        category = "معلم إسلامي",
        meaning = "المكان الشريف في المسجد النبوي بين البيت والمنبر المبارك",
        diacritics = listOf("", "ْ", "َّ", "ْ", "َ", "ُ")
    ),
    KalimaLevel(
        id = 21,
        word = "الإخلاص",
        vocalized = "الإِخْلَاصُ",
        category = "سورة قرآنية",
        meaning = "سورة التوحيد الخالص التي تعدل ثلث القرآن الكريم",
        diacritics = listOf("", "ْ", "ِ", "ْ", "َ", "", "ُ")
    ),
    KalimaLevel(
        id = 22,
        word = "الغفور",
        vocalized = "الْغَفُورُ",
        category = "اسم من أسماء الله الحسنى",
        meaning = "كثير المغفرة والستر لذنوب عباده التائبين النادمين",
        diacritics = listOf("", "ْ", "َ", "ُ", "", "ُ")
    ),
    KalimaLevel(
        id = 23,
        word = "المدينة",
        vocalized = "الْمَدِينَةُ",
        category = "معلم إسلامي",
        meaning = "طيبة الطيبة دار الهجرة النبوية ومثوى الرسول الكريم ﷺ",
        diacritics = listOf("", "ْ", "َ", "ِ", "", "َ", "ُ")
    ),
    KalimaLevel(
        id = 24,
        word = "اللطيف",
        vocalized = "اللَّطِيفُ",
        category = "اسم من أسماء الله الحسنى",
        meaning = "المحسن إلى عباده في خفاء ورفق ويوصل إليهم منافعهم بدقة",
        diacritics = listOf("", "ْ", "َّ", "ِ", "", "ُ")
    ),
    KalimaLevel(
        id = 25,
        word = "القرآن",
        vocalized = "الْقُرْآنُ",
        category = "كتاب الله الخالد",
        meaning = "كلام الله المعجز المنزل على نبينا محمد ﷺ المتعبد بتلاوته",
        diacritics = listOf("", "ْ", "ُ", "ْ", "آ", "ُ")
    )
)

// Keyboard letters in 3 rows (28 letters)
private val row1Letters = listOf("ض", "ص", "ث", "ق", "ف", "غ", "ع", "ه", "خ", "ح")
private val row2Letters = listOf("ج", "ش", "س", "ي", "ب", "ل", "ا", "ت", "ن", "م")
private val row3Letters = listOf("ك", "ط", "د", "ذ", "ر", "ز", "و", "ء")

private fun normalizeArabicChar(c: Char): Char {
    return when (c) {
        'أ', 'إ', 'آ', 'ٱ' -> 'ا'
        'ى' -> 'ي'
        'ة' -> 'ه'
        'ؤ', 'ئ' -> 'ء'
        else -> c
    }
}

/**
 * Storage manager using SharedPreferences('noor_games')
 */
private object KalimatStorage {
    private const val PREFS_NAME = "noor_games"
    private const val KEY_UNLOCKED = "kalimat_unlocked_level"
    private const val KEY_STARS = "kalimat_stars_lvl_"
    private const val KEY_SCORE = "kalimat_total_score"
    private const val KEY_STREAK = "kalimat_streak"

    fun getUnlockedLevel(context: Context): Int {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_UNLOCKED, 0)
    }

    fun saveUnlockedLevel(context: Context, levelIdx: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val current = prefs.getInt(KEY_UNLOCKED, 0)
        if (levelIdx > current) {
            prefs.edit().putInt(KEY_UNLOCKED, levelIdx).apply()
        }
    }

    fun getLevelStars(context: Context, levelIdx: Int): Int {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt("$KEY_STARS", 0)
    }

    fun saveLevelStars(context: Context, levelIdx: Int, stars: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val current = prefs.getInt("$KEY_STARS", 0)
        if (stars > current) {
            prefs.edit().putInt("$KEY_STARS", stars).apply()
        }
    }

    fun getTotalStars(context: Context, totalLevels: Int): Int {
        var sum = 0
        for (i in 0 until totalLevels) {
            sum += getLevelStars(context, i)
        }
        return sum
    }

    fun getTotalScore(context: Context): Int {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_SCORE, 0)
    }

    fun addScore(context: Context, points: Int): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val newScore = prefs.getInt(KEY_SCORE, 0) + points
        prefs.edit().putInt(KEY_SCORE, newScore).apply()
        return newScore
    }

    fun getStreak(context: Context): Int {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_STREAK, 0)
    }

    fun incrementStreak(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val newStreak = prefs.getInt(KEY_STREAK, 0) + 1
        prefs.edit().putInt(KEY_STREAK, newStreak).apply()
        return newStreak
    }

    fun resetStreak(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putInt(KEY_STREAK, 0).apply()
    }
}

@Composable
fun KalimatGameScreen(onBack: () -> Unit) {
    ForcePortraitOrientation()

    val context = LocalContext.current
    var currentLevelIdx by remember { mutableIntStateOf(0) }
    var inPlay by remember { mutableStateOf(false) }
    var refreshKey by remember { mutableIntStateOf(0) }

    val unlockedLevel = remember(refreshKey) { KalimatStorage.getUnlockedLevel(context) }
    val totalStars = remember(refreshKey) { KalimatStorage.getTotalStars(context, kalimatLevels.size) }
    val totalScore = remember(refreshKey) { KalimatStorage.getTotalScore(context) }
    val streak = remember(refreshKey) { KalimatStorage.getStreak(context) }

    Box(
        Modifier
            .fillMaxSize()
            .themeScreenBackground()
    ) {
        Column(Modifier.fillMaxSize()) {
            // Header Top Bar
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    if (inPlay) {
                        inPlay = false
                        refreshKey++
                    } else {
                        onBack()
                    }
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "رجوع",
                        tint = Gold
                    )
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "كلمة من الذكر",
                        color = Gold,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "المرحلة ${currentLevelIdx + 1} / ${kalimatLevels.size} | ⭐ $totalStars | 🌙 $totalScore | 🔥 $streak أعياد",
                        color = GoldSoft,
                        fontSize = 11.sp
                    )
                }
            }

            if (!inPlay) {
                // Level Map View (25 Levels)
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .noorGlassCard(cornerRadius = 18.dp)
                            .padding(14.dp)
                    ) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "خريطة المراحل الإيمانية",
                                    color = Gold,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "خمن الكلمة الإيمانية عبر ٢٥ مرحلة شيقة",
                                    color = TextSecondaryLinen,
                                    fontSize = 12.sp
                                )
                            }
                            Surface(
                                color = Gold.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.border(1.dp, Gold.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            ) {
                                Text(
                                    text = "🔥 $streak",
                                    color = Gold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        itemsIndexed(kalimatLevels) { index, lvl ->
                            val isUnlocked = index <= unlockedLevel
                            val starsEarned = KalimatStorage.getLevelStars(context, index)

                            Box(
                                Modifier
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isUnlocked) NavyCard else NavyCard.copy(alpha = 0.3f))
                                    .border(
                                        1.dp,
                                        if (starsEarned > 0) Gold else if (isUnlocked) Gold.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.08f),
                                        RoundedCornerShape(16.dp)
                                    )
                                    .clickable(enabled = isUnlocked) {
                                        currentLevelIdx = index
                                        inPlay = true
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    if (isUnlocked) {
                                        Text(
                                            text = "${lvl.id}",
                                            color = TextMain,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(Modifier.height(2.dp))
                                        Row {
                                            for (s in 1..3) {
                                                Icon(
                                                    imageVector = Icons.Default.Star,
                                                    contentDescription = null,
                                                    tint = if (s <= starsEarned) Gold else Color.White.copy(alpha = 0.2f),
                                                    modifier = Modifier.size(10.dp)
                                                )
                                            }
                                        }
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = "مقفل",
                                            tint = GoldSoft.copy(alpha = 0.35f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Game Play Screen
                KalimatPlayScreen(
                    level = kalimatLevels[currentLevelIdx],
                    onLevelComplete = { remainingLanterns ->
                        val stars = when {
                            remainingLanterns == 3 -> 3
                            remainingLanterns == 2 -> 2
                            else -> 1
                        }
                        val pts = 100 + (remainingLanterns * 20)
                        KalimatStorage.saveUnlockedLevel(context, currentLevelIdx + 1)
                        KalimatStorage.saveLevelStars(context, currentLevelIdx, stars)
                        KalimatStorage.addScore(context, pts)
                        KalimatStorage.incrementStreak(context)
                        GameProgressStore.rewardPoints(context, GameProgressStore.GAME_KALIMAT, currentLevelIdx, stars, pts)
                        refreshKey++
                    },
                    onNextLevel = {
                        if (currentLevelIdx < kalimatLevels.size - 1) {
                            currentLevelIdx++
                        } else {
                            inPlay = false
                        }
                        refreshKey++
                    },
                    onBackToMap = {
                        inPlay = false
                        refreshKey++
                    }
                )
            }
        }
    }
}

@Composable
private fun KalimatPlayScreen(
    level: KalimaLevel,
    onLevelComplete: (remainingLanterns: Int) -> Unit,
    onNextLevel: () -> Unit,
    onBackToMap: () -> Unit
) {
    val context = LocalContext.current
    val wordChars = remember(level) { level.word.toCharArray() }

    val revealedIndices = remember(level) { mutableStateMapOf<Int, Boolean>() }
    val guessedLetters = remember(level) { mutableStateMapOf<String, Boolean>() }
    var remainingLanterns by remember(level) { mutableIntStateOf(3) }
    var hintCount by remember(level) { mutableIntStateOf(0) }
    var lastHintTime by remember(level) { mutableLongStateOf(0L) }
    var hintToastMessage by remember(level) { mutableStateOf<String?>(null) }

    var showWinDialog by remember(level) { mutableStateOf(false) }
    var showFailDialog by remember(level) { mutableStateOf(false) }

    var shakeOffset by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(shakeOffset) {
        if (shakeOffset != 0f) {
            delay(300)
            shakeOffset = 0f
        }
    }

    fun handleLetterInput(letterStr: String) {
        if (showWinDialog || showFailDialog || remainingLanterns <= 0) return

        val normChar = normalizeArabicChar(letterStr[0])
        val matches = wordChars.indices.filter { normalizeArabicChar(wordChars[it]) == normChar }

        if (matches.isNotEmpty()) {
            guessedLetters[letterStr] = true
            matches.forEach { revealedIndices[it] = true }

            if (revealedIndices.size == wordChars.size) {
                showWinDialog = true
                onLevelComplete(remainingLanterns)
            }
        } else {
            guessedLetters[letterStr] = false
            remainingLanterns--
            shakeOffset = 10f

            if (remainingLanterns <= 0) {
                KalimatStorage.resetStreak(context)
                showFailDialog = true
            }
        }
    }

    fun useWhisperHint() {
        if (showWinDialog || showFailDialog || remainingLanterns <= 0) return
        if (hintCount >= 2) {
            hintToastMessage = "الحد الأقصى للتلميحات في هذه المرحلة هو ٢"
            return
        }
        val now = System.currentTimeMillis()
        if (now - lastHintTime < 15000L && lastHintTime != 0L) {
            val remainingSecs = ((15000L - (now - lastHintTime)) / 1000L) + 1
            hintToastMessage = "انتظر $remainingSecs ثوانٍ لاستخدام التلميح التالي"
            return
        }

        val unrevealed = wordChars.indices.filter { it !in revealedIndices }
        if (unrevealed.isEmpty()) return

        hintCount++
        lastHintTime = now
        hintToastMessage = null

        val pickIdx = unrevealed.random()
        val targetNorm = normalizeArabicChar(wordChars[pickIdx])

        wordChars.indices.filter { normalizeArabicChar(wordChars[it]) == targetNorm }.forEach {
            revealedIndices[it] = true
        }

        if (revealedIndices.size == wordChars.size) {
            showWinDialog = true
            onLevelComplete(remainingLanterns)
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 6.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Level Badge & Title
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Gold.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.border(1.dp, Gold.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                ) {
                    Text(
                        text = "المرحلة ${level.id}",
                        color = Gold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }

                Surface(
                    color = NavyCard,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.border(1.dp, GoldSoft.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                ) {
                    Text(
                        text = level.category,
                        color = TextMain,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            // Lanterns Allowance Row (6 Glowing Lanterns with Shake)
            Box(
                Modifier
                    .fillMaxWidth()
                    .offset(x = shakeOffset.dp)
                    .noorGlassCard(cornerRadius = 18.dp)
                    .padding(vertical = 8.dp, horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "الفوانيس المتبقية: $remainingLanterns / 3",
                        color = GoldSoft,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 1..3) {
                            LanternIcon(isActive = i <= remainingLanterns)
                        }
                    }
                }
            }

            // Meaning Hint Card ('معنى')
            Box(
                Modifier
                    .fillMaxWidth()
                    .noorGlassCard(cornerRadius = 18.dp)
                    .padding(14.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = Gold,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "المعنى الإيماني:",
                            color = Gold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = level.meaning,
                        color = TextMain,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Start,
                        lineHeight = 20.sp
                    )
                }
            }

            // Word Length Hint + Letter Tiles Row
            Box(
                Modifier
                    .fillMaxWidth()
                    .noorGlassCard(cornerRadius = 18.dp)
                    .padding(vertical = 12.dp, horizontal = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "الكلمة تتكون من ${wordChars.size} حروف",
                        color = GoldSoft,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(Modifier.height(10.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        wordChars.forEachIndexed { idx, ch ->
                            val isRevealed = idx in revealedIndices
                            val diacritic = level.diacritics.getOrNull(idx) ?: ""

                            Box(
                                Modifier
                                    .size(width = 42.dp, height = 52.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isRevealed) Gold.copy(alpha = 0.25f) else NavyCard)
                                    .border(
                                        1.5.dp,
                                        if (isRevealed) Gold else GoldSoft.copy(alpha = 0.35f),
                                        RoundedCornerShape(12.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    if (isRevealed) {
                                        Text(
                                            text = "$ch$diacritic",
                                            color = Gold,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    } else {
                                        Text(
                                            text = diacritic.ifEmpty { " " },
                                            color = GoldSoft,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Box(
                                            Modifier
                                                .width(18.dp)
                                                .height(3.dp)
                                                .background(Gold.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Action Button: Whisper Hint ('همسة')
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = { useWhisperHint() },
                    enabled = remainingLanterns > 0 && revealedIndices.size < wordChars.size,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Gold.copy(alpha = 0.2f),
                        contentColor = Gold,
                        disabledContainerColor = Color.White.copy(alpha = 0.05f),
                        disabledContentColor = Color.White.copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.border(1.dp, Gold.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = Gold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "همسة (كشف حرف)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // On-screen 28-letter Arabic Keyboard
            Box(
                Modifier
                    .fillMaxWidth()
                    .noorGlassCard(cornerRadius = 18.dp)
                    .padding(8.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    KeyboardRow(
                        letters = row1Letters,
                        guessedLetters = guessedLetters,
                        onLetterSelect = { handleLetterInput(it) }
                    )
                    KeyboardRow(
                        letters = row2Letters,
                        guessedLetters = guessedLetters,
                        onLetterSelect = { handleLetterInput(it) }
                    )
                    KeyboardRow(
                        letters = row3Letters,
                        guessedLetters = guessedLetters,
                        onLetterSelect = { handleLetterInput(it) }
                    )
                }
            }
        }

        // Gold Confetti Effect on Win
        if (showWinDialog) {
            GoldConfettiEffect(active = true)
        }

        // Win Screen Overlay
        if (showWinDialog) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Navy.copy(alpha = 0.85f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(0.9f)
                        .noorGlassCard(cornerRadius = 24.dp)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (level.id == 25) "مبارك! أكملت جميع المراحل 🏆✨" else "أحسنت! إجابة صحيحة 🎉",
                            color = Gold,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(12.dp))

                        Text(
                            text = level.vocalized,
                            color = Gold,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(8.dp))

                        Surface(
                            color = Gold.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.border(1.dp, Gold.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        ) {
                            Text(
                                text = level.category,
                                color = GoldSoft,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        Text(
                            text = level.meaning,
                            color = TextMain,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )

                        Spacer(Modifier.height(16.dp))

                        // Stars Display
                        val starsCount = when {
                            remainingLanterns >= 5 -> 3
                            remainingLanterns >= 3 -> 2
                            else -> 1
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            for (s in 1..3) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = if (s <= starsCount) Gold else Color.Gray.copy(alpha = 0.4f),
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        Button(
                            onClick = {
                                showWinDialog = false
                                if (level.id == 25) {
                                    onBackToMap()
                                } else {
                                    onNextLevel()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Navy),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text(
                                text = if (level.id == 25) "إنهاء اللعبة وتصفح الخريطة 🌟" else "المرحلة التالية ➔",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Fail Overlay
        if (showFailDialog) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Navy.copy(alpha = 0.85f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(0.85f)
                        .noorGlassCard(cornerRadius = 22.dp)
                        .padding(22.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "انتهت الفوانيس! 🌙",
                            color = Gold,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "الكلمة هي: ${level.vocalized}",
                            color = TextMain,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = {
                                showFailDialog = false
                                revealedIndices.clear()
                                guessedLetters.clear()
                                remainingLanterns = 6
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Navy),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "إعادة المحاولة",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
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
private fun LanternIcon(isActive: Boolean) {
    val alphaAnim by animateFloatAsState(
        targetValue = if (isActive) 1f else 0.25f,
        animationSpec = tween(300),
        label = "lanternAlpha"
    )

    Box(
        Modifier
            .size(32.dp, 40.dp)
            .graphicsLayer { alpha = alphaAnim },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Ring
            drawCircle(
                color = if (isActive) Gold else Color.Gray,
                radius = w * 0.12f,
                center = Offset(w * 0.5f, h * 0.12f)
            )

            // Lantern Path
            val path = Path().apply {
                moveTo(w * 0.30f, h * 0.22f)
                lineTo(w * 0.70f, h * 0.22f)
                lineTo(w * 0.85f, h * 0.55f)
                lineTo(w * 0.70f, h * 0.88f)
                lineTo(w * 0.30f, h * 0.88f)
                lineTo(w * 0.15f, h * 0.55f)
                close()
            }

            drawPath(
                path = path,
                brush = Brush.verticalGradient(
                    colors = if (isActive) {
                        listOf(GoldSoft, Gold, Color(0xFFD4AF37))
                    } else {
                        listOf(Color(0xFF4A4A4A), Color(0xFF2A2A2A))
                    }
                )
            )

            if (isActive) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.9f),
                    radius = w * 0.14f,
                    center = Offset(w * 0.5f, h * 0.55f)
                )
            }
        }
    }
}

@Composable
private fun KeyboardRow(
    letters: List<String>,
    guessedLetters: Map<String, Boolean>,
    onLetterSelect: (String) -> Unit
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally)
    ) {
        letters.forEach { ch ->
            val isGuessed = guessedLetters.containsKey(ch)
            val isCorrect = guessedLetters[ch] == true

            val (bgColor, textColor, borderColor) = when {
                isGuessed && isCorrect -> Triple(Gold, Navy, Gold)
                isGuessed && !isCorrect -> Triple(Navy.copy(alpha = 0.3f), Color.White.copy(alpha = 0.25f), Color.Transparent)
                else -> Triple(NavyCard.copy(alpha = 0.7f), Color.White, GoldSoft.copy(alpha = 0.3f))
            }

            Box(
                Modifier
                    .weight(1f, fill = false)
                    .widthIn(min = 28.dp, max = 36.dp)
                    .height(42.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(bgColor)
                    .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                    .clickable(enabled = !isGuessed) { onLetterSelect(ch) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = ch,
                    color = textColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private data class Particle(
    val x: Float,
    val y: Float,
    val size: Float,
    val color: Color,
    val vx: Float,
    val vy: Float
)

@Composable
private fun GoldConfettiEffect(
    modifier: Modifier = Modifier,
    active: Boolean
) {
    if (!active) return

    val particles = remember {
        val colors = listOf(Gold, GoldSoft, Color(0xFFFFD700), Color(0xFFFFA500), Color(0xFFFFF8DC))
        List(35) {
            val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
            val speed = Random.nextFloat() * 6f + 3f
            Particle(
                x = 0.5f,
                y = 0.4f,
                size = Random.nextFloat() * 10f + 5f,
                color = colors.random(),
                vx = cos(angle) * speed,
                vy = sin(angle) * speed - 4f
            )
        }
    }

    var progress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(active) {
        val anim = Animatable(0f)
        anim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1600, easing = LinearOutSlowInEasing)
        ) {
            progress = value
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val alpha = (1f - progress).coerceIn(0f, 1f)

        particles.forEach { p ->
            val px = (p.x * w) + (p.vx * progress * 35f)
            val py = (p.y * h) + (p.vy * progress * 35f) + (0.5f * 9.8f * progress * progress * 40f)
            drawCircle(
                color = p.color.copy(alpha = alpha),
                radius = p.size * (1f - progress * 0.3f),
                center = Offset(px, py)
            )
        }
    }
}
