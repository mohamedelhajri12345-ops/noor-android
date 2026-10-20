package com.elhajri.noor.games
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.elhajri.noor.ui.TextMain
import com.elhajri.noor.ui.TextSecondaryLinen
import com.elhajri.noor.ui.noorGlassCard
import com.elhajri.noor.ui.themeScreenBackground

private const val GAME_NAME = "WisdomPearlsGame"
private const val TOTAL_STAGES = 25

private data class HadithPearl(
    val id: Int,
    val text: String,
    val questionPrompt: String,
    val correctAnswer: String,
    val options: List<String>,
    val source: String,
    val explanation: String
)

private val HADITH_STAGES = listOf(
    HadithPearl(
        id = 1,
        text = "«إنما الأعمال بالنيات، وإنما لكل امرئ ما نوى...»",
        questionPrompt = "من هو راوي هذا الحديث الشريف من الصحابة؟",
        correctAnswer = "عمر بن الخطاب رضي الله عنه",
        options = listOf("عمر بن الخطاب رضي الله عنه", "أبو هريرة رضي الله عنه", "علي بن أبي طالب رضي الله عنه", "عبد الله بن عمر رضي الله عنهما"),
        source = "متفق عليه (البخاري ومسلم)",
        explanation = "حديث جليل عليه مدار الإسلام، يبين أن صلاح الأعمال وقبولها مرتبط بنية العبد الإخلاص لله."
    ),
    HadithPearl(
        id = 2,
        text = "«الدين النصيحة: لله ولكتبه ولرسوله ولأئمة المسلمين وعامتهم»",
        questionPrompt = "من هو الصحابي الجليل راوي هذا الحديث؟",
        correctAnswer = "تميم الداري رضي الله عنه",
        options = listOf("تميم الداري رضي الله عنه", "أبو ذر الغفاري رضي الله عنه", "سلمان الفارسي رضي الله عنه", "معاذ بن جبل رضي الله عنه"),
        source = "رواه مسلم",
        explanation = "يبين النبي ﷺ أن عماد الدين وجوهره هو النصح الخالص لله والصدق مع خلقه."
    ),
    HadithPearl(
        id = 3,
        text = "«لا يؤمن أحدكم حتى يحب لأخيه ما يحب لنفسه»",
        questionPrompt = "من الصحابي خادم النبي ﷺ الذي روى هذا الحديث؟",
        correctAnswer = "أنس بن مالك رضي الله عنه",
        options = listOf("أنس بن مالك رضي الله عنه", "عبد الله بن مسعود رضي الله عنه", "جابر بن عبد الله رضي الله عنه", "أبو سعيد الخدري رضي الله عنه"),
        source = "متفق عليه",
        explanation = "من كمال الإيمان سلامة الصدر ومحبة الخير لكافة المسلمين كما يحبه لنفسه."
    ),
    HadithPearl(
        4,
        text = "«من كان يؤمن بالله واليوم الآخر فليقل خيراً أو ليصمت»",
        questionPrompt = "من هو الصحابي الجليل راوي الحديث النبوي؟",
        correctAnswer = "أبو هريرة رضي الله عنه",
        options = listOf("أبو هريرة رضي الله عنه", "عائشة أم المؤمنين رضي الله عنها", "عثمان بن عفان رضي الله عنه", "سعد بن أبي وقاص رضي الله عنه"),
        source = "متفق عليه",
        explanation = "حفظ اللسان واختيار الكلام الطيب أو الصمت من أعظم أسباب النجاة وكمال الإيمان."
    ),
    HadithPearl(
        5,
        text = "«من سلك طريقاً يطلب فيه علماً سلك الله به طريقاً إلى الجنة»",
        questionPrompt = "من راوي الحديث من صحابة رسول الله ﷺ؟",
        correctAnswer = "أبو الدرداء رضي الله عنه",
        options = listOf("أبو الدرداء رضي الله عنه", "أبو موسى الأشعري رضي الله عنه", "ابن عباس رضي الله عنهما", "أبي بن كعب رضي الله عنه"),
        source = "رواه الترمذي وأبو داود",
        explanation = "يبين الشرف العظيم لطلب العلم الشرعي وأنه طريق لتيسير دخول جنات النعيم."
    ),
    HadithPearl(
        6,
        text = "«اتق الله حيثما كنت، وأتبع السيئة الحسنة تمحها، وخالق الناس بخلق حسن»",
        questionPrompt = "من هما الصحابيان اللذان رويا هذه الوصية النبوية؟",
        correctAnswer = "أبو ذر ومعاذ بن جبل رضي الله عنهما",
        options = listOf("أبو ذر ومعاذ بن جبل رضي الله عنهما", "ابن مسعود وعمار رضي الله عنهما", "أبو هريرة وأنس رضي الله عنهما", "الزبير وطلحة رضي الله عنهما"),
        source = "رواه الترمذي",
        explanation = "وصية جامعة لحقوق الله بتقواه، وحقوق النفس بالتوبة، وحقوق العباد بالمكارم."
    ),
    HadithPearl(
        7,
        text = "«المسلم من سلم المسلمون من لسانه ويده»",
        questionPrompt = "من راوي الحديث الشريف؟",
        correctAnswer = "عبد الله بن عمرو رضي الله عنهما",
        options = listOf("عبد الله بن عمرو رضي الله عنهما", "أبو هريرة رضي الله عنه", "خالد بن الوليد رضي الله عنه", "البراء بن عازب رضي الله عنه"),
        source = "متفق عليه",
        explanation = "حقيقة الإسلام العملي تبدو في عدم إيذاء الناس بالأقوال السيئة أو الأفعال الضارة."
    ),
    HadithPearl(
        8,
        text = "«الطهور شطر الإيمان، والحمد لله تملأ الميزان»",
        questionPrompt = "من هو راوي الحديث الجليل في فضل الطهارة والذكر؟",
        correctAnswer = "أبو مالك الأشعري رضي الله عنه",
        options = listOf("أبو مالك الأشعري رضي الله عنه", "حذيفة بن اليمان رضي الله عنه", "بلال بن رباح رضي الله عنه", "عمرو بن العاص رضي الله عنه"),
        source = "رواه مسلم",
        explanation = "فضل الطهارة الظاهرة والباطنة وعظيم ثواب ذكر الله والتسبيح في الميزان."
    ),
    HadithPearl(
        9,
        text = "«احفظ الله يحفظك، احفظ الله تجده تجاهك...»",
        questionPrompt = "من راوي هذه الوصية العظيمة لابن عم النبي ﷺ وهو غلام؟",
        correctAnswer = "عبد الله بن عباس رضي الله عنهما",
        options = listOf("عبد الله بن عباس رضي الله عنهما", "عبد الله بن عمر رضي الله عنهما", "عبد الله بن الزبير رضي الله عنهما", "عبد الله بن عمرو رضي الله عنهما"),
        source = "رواه الترمذي",
        explanation = "وصية نبوية عظيمة في التوكل على الله وحفظ حدوده والاستعانة به وحده."
    ),
    HadithPearl(
        10,
        text = "«أن رجلاً قال للنبي ﷺ: أوصني، قال: لا تغضب، فردد مراراً قال: لا تغضب»",
        questionPrompt = "من هو راوي حديث الوصية بالابتعاد عن الغضب؟",
        correctAnswer = "أبو هريرة رضي الله عنه",
        options = listOf("أبو هريرة رضي الله عنه", "المقداد بن الأسود رضي الله عنه", "حسان بن ثابت رضي الله عنه", "صهيب الرومي رضي الله عنه"),
        source = "رواه البخاري",
        explanation = "السيطرة على النفس عند الغضب تجمع أسباب الخير وتدفع نزغات الشيطان."
    ),
    HadithPearl(
        11,
        text = "«من أحدث في أمرنا هذا ما ليس منه فهو رد»",
        questionPrompt = "من راوية هذا الحديث العظيم من أمهات المؤمنين؟",
        correctAnswer = "عائشة أم المؤمنين رضي الله عنها",
        options = listOf("عائشة أم المؤمنين رضي الله عنها", "حفصة أم المؤمنين رضي الله عنها", "أم سلمة رضي الله عنها", "جويرية أم المؤمنين رضي الله عنها"),
        source = "متفق عليه",
        explanation = "ميزان الأعمال الظاهرة ووجوب الاتباع والتأسّي بسنة النبي ﷺ."
    ),
    HadithPearl(
        12,
        text = "«الحلال بيّن والحرام بيّن وبينهما أمور مشتبهات لا يعلمهن كثير من الناس...»",
        questionPrompt = "من هو الصحابي الجليل راوي الحديث النبوي الشريف؟",
        correctAnswer = "النعمان بن بشير رضي الله عنهما",
        options = listOf("النعمان بن بشير رضي الله عنهما", "سعد بن عبادة رضي الله عنه", "عبادة بن الصامت رضي الله عنه", "النعمان بن مقرن رضي الله عنه"),
        source = "متفق عليه",
        explanation = "أصل عظيم في الورع واجتناب الشبهات صيانة للدين والعرض."
    ),
    HadithPearl(
        13,
        text = "«من صلّى البردين دخل الجنة»",
        questionPrompt = "ما هما الصدقتان والصلاتان المقصودتان بـ (البردين)؟",
        correctAnswer = "صلاة الفجر وصلاة العصر",
        options = listOf("صلاة الفجر وصلاة العصر", "صلاة الظهر وصلاة العصر", "صلاة المغرب وصلاة العشاء", "صلاة الجمعة وصلاة العيد"),
        source = "متفق عليه (البخاري ومسلم)",
        explanation = "البردَان هما الفجر والعصر لأنهما تقعان في بردي النهار، وفيهما حث عظيمة على المحافظة عليهما."
    ),
    HadithPearl(
        14,
        text = "«سبعة يظلهم الله في ظله يوم لا ظل إلا ظله...»",
        questionPrompt = "من هو أول السبعة المذكورين في الحديث الشريف؟",
        correctAnswer = "إمام عادل",
        options = listOf("إمام عادل", "شاب نشأ في عبادة الله", "رجل قلبه معلق بالمساجد", "رجل تصدق بصدقة فأخفاها"),
        source = "متفق عليه",
        explanation = "بدأ الحديث بالإمام العادل لعظم نفعه الشامل لكافة الأمة ورعيته."
    ),
    HadithPearl(
        15,
        text = "«كلمتان خفيفتان على اللسان ثقيلتان في الميزان حبيبتان إلى الرحمن...»",
        questionPrompt = "ما هما الكلمتان المباركتان المذكورتان في ختام صحيح البخاري؟",
        correctAnswer = "سبحان الله وبحمده، سبحان الله العظيم",
        options = listOf("سبحان الله وبحمده، سبحان الله العظيم", "لا حول ولا قوة إلا بالله", "استغفر الله العظيم وأتوب إليه", "الحمد لله رب العالمين"),
        source = "متفق عليه",
        explanation = "ذكر خفيف وسهل على اللسان لكنه عظيم القدر والثواب عند الله عز وجل."
    ),
    HadithPearl(
        16,
        text = "«من غشّنا فليس منّا»",
        questionPrompt = "في أي مناسبة قال النبي ﷺ هذا الحديث الشريف؟",
        correctAnswer = "عندما مرّ على صبرة طعام بالغة بللاً في السوق",
        options = listOf("عندما مرّ على صبرة طعام بالغة بللاً في السوق", "في إحدى الغزوات", "عند كتابة صلح الحديبية", "في خطبة الجمعة"),
        source = "رواه مسلم",
        explanation = "النصيحة بالأمانة والصدق في المعاملات التجارية والنهي الصارم عن الخداع والتدليس."
    ),
    HadithPearl(
        17,
        text = "«خيركم من تعلم القرآن وعلمه»",
        questionPrompt = "من الصحابي راوي هذا الحديث الشريف الخالد؟",
        correctAnswer = "عثمان بن عفان رضي الله عنه",
        options = listOf("عثمان بن عفان رضي الله عنه", "علي بن أبي طالب رضي الله عنه", "عبد الله بن مسعود رضي الله عنه", "أبي بن كعب رضي الله عنه"),
        source = "رواه البخاري",
        explanation = "فضل العناية بكتاب الله تلاوة وتدبراً وتعليماً للناس."
    ),
    HadithPearl(
        18,
        text = "«المؤمن القوي خير وأحب إلى الله من المؤمن الضعيف وفي كل خير...»",
        questionPrompt = "ما هي الوصية التي تلت هذه العبارة في الحديث؟",
        correctAnswer = "احرص على ما ينفعك واستعن بالله ولا تعجز",
        options = listOf("احرص على ما ينفعك واستعن بالله ولا تعجز", "واتق الله حيثما كنت", "وأتبع السيئة الحسنة", "ولا تغضب ولَك الجنة"),
        source = "رواه مسلم",
        explanation = "الحث على الإيجابية والأخذ بالأسباب مع التوكل الكامل على الله."
    ),
    HadithPearl(
        19,
        text = "«من استطاع منكم الباءة فليتزوج...»",
        questionPrompt = "ما الموصى به لمن لم يستطع الباءة في الحديث؟",
        correctAnswer = "فعليه بالصوم فإنه له وجاء",
        options = listOf("فعليه بالصوم فإنه له وجاء", "فعليه بالاستغفار والقيام", "فعليه بالدعاء والصدقة", "فعليه بطلب العلم والجهاد"),
        source = "متفق عليه",
        explanation = "الصوم يكسر الشهوة ويهذب النفس ويحفظ العفة والأخلاق."
    ),
    HadithPearl(
        20,
        text = "«أيعجز أحدكم أن يكسب كل يوم ألف حسنة؟»",
        questionPrompt = "كيف تكسب ألف حسنة كما بّين النبي ﷺ؟",
        correctAnswer = "يسبح مائة تسبيحة فيكتب له ألف حسنة أو يحط عنه ألف خطيئة",
        options = listOf("يسبح مائة تسبيحة فيكتب له ألف حسنة أو يحط عنه ألف خطيئة", "يصلي أربع ركعات في الضحى", "يقرأ سورة الإخلاص ثلاث مرات", "يتصدق بدرهم واحد"),
        source = "رواه مسلم",
        explanation = "فضل التسبيح وعظيم مضاعفة الحسنات من كرم الله تعالى."
    ),
    HadithPearl(
        21,
        text = "«لا يدخل الجنة قاطع...»",
        questionPrompt = "ما المقصود بـ (قاطع) في الحديث الشريف؟",
        correctAnswer = "قاطع رحم",
        options = listOf("قاطع رحم", "قاطع طريق", "قاطع صلاة", "قاطع عهد"),
        source = "متفق عليه",
        explanation = "عظم إثم قطع الأرحام وأهمية صلة الأقارب والإحسان إليهم."
    ),
    HadithPearl(
        22,
        text = "«تبسمك في وجه أخيك لك...»",
        questionPrompt = "بماذا وصف النبي ﷺ التبسم في وجه الأخ المسلم؟",
        correctAnswer = "صدقة",
        options = listOf("صدقة", "حسن خلق", "نور في الوجه", "بركة في العمر"),
        source = "رواه الترمذي",
        explanation = "الطلاقة وبشاشة الوجه من محاسن الإسلام وجلب المحبة بين المسلمين."
    ),
    HadithPearl(
        23,
        text = "«أحب الأعمال إلى الله أدومها وإن...»",
        questionPrompt = "أكمل الحديث النبوي الشريف:",
        correctAnswer = "...قلَّ",
        options = listOf("...قلَّ", "...صَعُبَ", "...خَفِيَ", "...تَأَخَّرَ"),
        source = "متفق عليه",
        explanation = "الاستمرارية والمداومة على الطاعات وإن كانت قليلة أحب إلى الله من الانقطاع."
    ),
    HadithPearl(
        24,
        text = "«من بنى مسجداً يبتغي به وجه الله...»",
        questionPrompt = "ما هو الجزاء العظيم لمن بنى مسجداً لله؟",
        correctAnswer = "بنى الله له مثله في الجنة",
        options = listOf("بنى الله له مثله في الجنة", "غفر الله له ما تقدم من ذنبه", "كتب الله له أجر كل من صلى فيه", "رفع الله درجاته في الدنيا والآخرة"),
        source = "متفق عليه",
        explanation = "عظم أجر عمارة المساجد وبنائها ابتغاء وجه الله الكريم."
    ),
    HadithPearl(
        25,
        text = "«أنا وكافل اليتيم في الجنة هكذا...»",
        questionPrompt = "كيف أشار النبي ﷺ بإصبعيه الشريفتين عند بيان منزلة كافل اليتيم؟",
        correctAnswer = "أشار بالسبابة والوسطى وفرّج بينهما شيئاً",
        options = listOf("أشار بالسبابة والوسطى وفرّج بينهما شيئاً", "ضمّ كفيه معاً", "رفع إبهامه بابتسامة", "أشار بإبهامه والسبابة"),
        source = "رواه البخاري",
        explanation = "منزلة رفيعة وقرب مجاور للنبي ﷺ في الجنة لمن يعطف على الأيتام ويكفلهم."
    )
)

@Composable
fun WisdomPearlsGame(onBack: () -> Unit) {
    val context = LocalContext.current
    var progressData by remember {
        mutableStateOf(StageProgressStore.loadProgress(context, GAME_NAME, TOTAL_STAGES))
    }
    var selectedStageIdx by remember { mutableStateOf<Int?>(null) }

    if (selectedStageIdx == null) {
        StageMapScreen(
            gameTitle = "لؤلؤة الحكمة",
            gameSubtitle = "٢٥ مرحلة لدراسة الأحاديث والدرر النبوية",
            totalStages = TOTAL_STAGES,
            progressData = progressData,
            onSelectStage = { selectedStageIdx = it },
            onBack = onBack
        )
    } else {
        val stageIdx = selectedStageIdx!!
        val currentStage = HADITH_STAGES[stageIdx]

        WisdomPearlsPlayScreen(
            stage = currentStage,
            stageNumber = stageIdx + 1,
            totalStages = TOTAL_STAGES,
            onBackToMap = {
                progressData = StageProgressStore.loadProgress(context, GAME_NAME, TOTAL_STAGES)
                selectedStageIdx = null
            },
            onCompleteStage = { stars ->
                StageProgressStore.saveProgress(context, GAME_NAME, stageIdx, stars, TOTAL_STAGES)
                progressData = StageProgressStore.loadProgress(context, GAME_NAME, TOTAL_STAGES)
            },
            onNextStage = {
                if (stageIdx + 1 < TOTAL_STAGES) {
                    selectedStageIdx = stageIdx + 1
                } else {
                    selectedStageIdx = null
                }
            }
        )
    }
}

@Composable
private fun WisdomPearlsPlayScreen(
    stage: HadithPearl,
    stageNumber: Int,
    totalStages: Int,
    onBackToMap: () -> Unit,
    onCompleteStage: (Int) -> Unit,
    onNextStage: () -> Unit
) {
    val shuffledOptions = remember(stage) { stage.options.shuffled() }

    var selectedOption by remember(stage) { mutableStateOf<String?>(null) }
    var isAnswered by remember(stage) { mutableStateOf(false) }
    var mistakesCount by remember(stage) { mutableIntStateOf(0) }
    var showWinDialog by remember(stage) { mutableStateOf(false) }
    var earnedStars by remember(stage) { mutableIntStateOf(3) }
    val haptics = LocalHapticFeedback.current
    var shakeTrigger by remember(stage) { mutableLongStateOf(0L) }

    fun handleOptionClick(opt: String) {
        if (isAnswered) return
        selectedOption = opt
        isAnswered = true

        val isCorrect = (opt == stage.correctAnswer)
        if (isCorrect) {
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            val stars = when (mistakesCount) {
                0 -> 3
                1 -> 2
                else -> 1
            }
            earnedStars = stars
            onCompleteStage(stars)
            showWinDialog = true
        } else {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            shakeTrigger = System.currentTimeMillis()
            mistakesCount += 1
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
                .verticalScroll(rememberScrollState())
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBackToMap) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "المراحل",
                        tint = Gold
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "لؤلؤة الحكمة",
                        color = Gold,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "المرحلة ${stageNumber.toArabicDigits()} من ${totalStages.toArabicDigits()}",
                        color = GoldSoft,
                        fontSize = 12.sp
                    )
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = NavyCard,
                    border = BorderStroke(1.dp, Gold.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Gold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "أخطاء: ${mistakesCount.toArabicDigits()}",
                            color = Gold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Hadith Text Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .noorGlassCard(cornerRadius = 20.dp)
                    .border(1.dp, Gold.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stage.text,
                        color = TextMain,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        lineHeight = 28.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "المصدر: ${stage.source}",
                        color = GoldSoft,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Question Prompt
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .noorGlassCard(cornerRadius = 14.dp)
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stage.questionPrompt,
                    color = Gold,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Options List
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                shuffledOptions.forEach { optionText ->
                    val isSelected = (selectedOption == optionText)
                    val isCorrectOpt = (optionText == stage.correctAnswer)

                    val bgColor = when {
                        isAnswered && isCorrectOpt -> Color(0xFF2E7D32).copy(alpha = 0.85f)
                        isAnswered && isSelected -> Color(0xFFC62828).copy(alpha = 0.85f)
                        else -> NavyCard
                    }

                    val borderColor = when {
                        isAnswered && isCorrectOpt -> Color(0xFF81C784)
                        isAnswered && isSelected -> Color(0xFFE57373)
                        else -> Gold.copy(alpha = 0.3f)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(bgColor)
                            .border(1.5.dp, borderColor, RoundedCornerShape(14.dp))
                            .clickable(enabled = !isAnswered) { handleOptionClick(optionText) }
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = optionText,
                            color = TextMain,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            if (isAnswered && selectedOption != stage.correctAnswer) {
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .noorGlassCard(cornerRadius = 14.dp)
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "إجابة غير صحيحة، حاول مجدداً واقتبس اللؤلؤة الصحيحة!",
                        color = Color(0xFFFF8A80),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Explanation Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .noorGlassCard(cornerRadius = 16.dp)
                    .padding(14.dp)
            ) {
                Column {
                    Text(
                        text = "💎 حكمة وفقه الحديث:",
                        color = Gold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stage.explanation,
                        color = TextSecondaryLinen,
                        fontSize = 13.sp,
                        lineHeight = 20.sp
                    )
                }
            }
        }

        if (showWinDialog) {
            StageWinDialog(
                stageNumber = stageNumber,
                totalStages = totalStages,
                stars = earnedStars,
                stageTitle = "حديث ${stageNumber.toArabicDigits()}",
                onNextStage = {
                    showWinDialog = false
                    onNextStage()
                },
                onReplayStage = {
                    showWinDialog = false
                    selectedOption = null
                    isAnswered = false
                    mistakesCount = 0
                },
                onStageMap = onBackToMap
            )
        }
    }
}
