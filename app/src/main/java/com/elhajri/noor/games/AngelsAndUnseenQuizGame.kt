package com.elhajri.noor.games

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

private const val GAME_NAME = "AngelsAndUnseenQuizGame"
private const val TOTAL_STAGES = 25

private data class UnseenQuestion(
    val id: Int,
    val question: String,
    val correctAnswer: String,
    val options: List<String>,
    val fact: String
)

private val UNSEEN_STAGES = listOf(
    UnseenQuestion(
        1,
        "من هو الملك الموكل بالوحي والنزول بالقرآن الكريم على الأنبياء عليهم السلام؟",
        "جبريل عليه السلام",
        listOf("جبريل عليه السلام", "ميكائيل عليه السلام", "إسرافيل عليه السلام", "ملك الموت"),
        "جبريل عليه السلام هو الروح الأمين والسفير بين الله تعالى وأنبيائه ورسله الكرام."
    ),
    UnseenQuestion(
        2,
        "من هو الملك الموكل بالقطر والنبات وإرزاق العباد بأمر الله؟",
        "ميكائيل عليه السلام",
        listOf("ميكائيل عليه السلام", "إسرافيل عليه السلام", "رضوان عليه السلام", "مالك عليه السلام"),
        "ميكائيل عليه السلام هو الملك المقرب الموكل بالأمطار والإنبات والأرزاق."
    ),
    UnseenQuestion(
        3,
        "من هو الملك الموكل بالنفخ في الصور يوم القيامة والبعث؟",
        "إسرافيل عليه السلام",
        listOf("إسرافيل عليه السلام", "جبريل عليه السلام", "منكر ونكير", "رقيب وعتيد"),
        "ينفخ إسرافيل عليه السلام في الصور النفخة الأولى للصعق والنفخة الثانية للبعث والنشور."
    ),
    UnseenQuestion(
        4,
        "ما اسم الملك الخازن والموكل بأبواب الجنة؟",
        "رضوان عليه السلام",
        listOf("رضوان عليه السلام", "مالك عليه السلام", "جبريل عليه السلام", "ميكائيل عليه السلام"),
        "رضوان عليه السلام هو خازن الجنة الذي يستقبل المؤمنين المتقين بالبشر والسلام."
    ),
    UnseenQuestion(
        5,
        "ما اسم الملك الخازن والموكل بجهنم والنار؟",
        "مالك عليه السلام",
        listOf("مالك عليه السلام", "رضوان عليه السلام", "إسرافيل عليه السلام", "منكر"),
        "مالك عليه السلام هو خازن النار، قال تعالى: ﴿وَنَادَوْا يَا مَالِكُ لِيَقْضِ عَلَيْنَا رَبُّكَ﴾."
    ),
    UnseenQuestion(
        6,
        "ما اسما الملكين الموكلين بسؤال العبد في قبره بعد دفنه؟",
        "منكر ونكير",
        listOf("منكر ونكير", "رقيب وعتيد", "هاروت وماروت", "جبريل وميكائيل"),
        "يسأل منكر ونكير العبد في القبر عن ربه ودينه والنبي الذي بُعث فيه."
    ),
    UnseenQuestion(
        7,
        "ما اسما الملكين الموكلين بكتابة حسنات العبد وسيئاته؟",
        "رقيب وعتيد",
        listOf("رقيب وعتيد", "منكر ونكير", "هاروت وماروت", "رضوان ومالك"),
        "قال تعالى: ﴿مَا يَلْفِظُ مِنْ قَوْلٍ إِلَّا لَدَيْهِ رَقِيبٌ عَتِيدٌ﴾."
    ),
    UnseenQuestion(
        8,
        "مما خُلق الملائكة الكرام عليهم السلام كما جاء في الحديث الصحيح؟",
        "من نور",
        listOf("من نور", "من نار السموم", "من طين", "من ماء"),
        "قال النبي ﷺ: «خُقلت الملائكة من نور، وخُلق الجان من مارج من نار» (رواه مسلم)."
    ),
    UnseenQuestion(
        9,
        "مما خُلقت الجان قبل خلق البشر كما أخبر القرآن والسنة؟",
        "من مارج من نار",
        listOf("من مارج من نار", "من نور", "من صلصال كالفخار", "من الريح"),
        "خُلق الجان من لهب النار الصافي الخالي من الدخان (مارج من نار)."
    ),
    UnseenQuestion(
        10,
        "ما هو البيت الموفور في السماء السابعة الذي تطوف به الملائكة؟",
        "البيت المعمور",
        listOf("البيت المعمور", "البيت العتيق", "المسجد الأقصى", "سدرة المنتهى"),
        "يدخل البيت المعمور كل يوم سبعون ألف ملك للتعبد والطواف لا يعودون إليه إلى يوم القيامة."
    ),
    UnseenQuestion(
        11,
        "ما اسم الشجرة العظيمة التي ينتهي عندها علم الخلائق في السماء السابعة؟",
        "سدرة المنتهى",
        listOf("سدرة المنتهى", "جبل القاف", "عين اليقين", "المقام المحمود"),
        "سدرة المنتهى شجرة مباركة عند عرش الرحمن انتهى إليها معراج النبي ﷺ."
    ),
    UnseenQuestion(
        12,
        "ما اسم الجسر المنصوب على ظهر جهنم يمر عليه الناس يوم القيامة؟",
        "الصراط",
        listOf("الصراط", "الميزان", "الحوض", "الأعراف"),
        "الصراط دقيق وحاد يمر عليه المؤمنون على قدر أعمالهم ونورهم يوم القيامة."
    ),
    UnseenQuestion(
        13,
        "ما اسم الحوض المبارك المورود للنبي ﷺ يوم القيامة؟",
        "حوض الكوثر",
        listOf("حوض الكوثر", "عين سلسبيل", "بئر زمزم", "نهر الجنة"),
        "من شرب من حوض النبي ﷺ شربةً هنيئة مباركة لا يظمأ بعدها أبداً."
    ),
    UnseenQuestion(
        14,
        "كيف حقيقة الميزان الذي توضع فيه الأعمال يوم القيامة؟",
        "ميزان حقيقي له كفتان ولسان تُوزن فيه الأعمال",
        listOf("ميزان حقيقي له كفتان ولسان تُوزن فيه الأعمال", "رمز معنوي فقط", "كتاب مكتوب", "نور يضيء"),
        "توضع في الميزان الصحائف والأعمال وتوزن بالحسنة والسيئة بفضل الله وعدله."
    ),
    UnseenQuestion(
        15,
        "ما هو المكان المرتفع بين الجنة والنار بينه القرآن الكريم؟",
        "الأعراف",
        listOf("الأعراف", "البرزخ", "الحجاب", "المنتهى"),
        "أصحاب الأعراف قوم استوت حسناتهم وسيئاتهم يوقفون بين الجنة والنار حتى يقضي الله فيهم."
    ),
    UnseenQuestion(
        16,
        "ما هي المرحلة الفاصلة بين موت الإنسان والبعث يوم القيامة؟",
        "الحياة البرزخية (البرزخ)",
        listOf("الحياة البرزخية (البرزخ)", "النشور", "الصعق", "الخُلد"),
        "البرزخ هو المرحلة التي يعيشها الإنسان في قبره من لحظة وفاته إلى يوم البعث."
    ),
    UnseenQuestion(
        17,
        "ما اسم النفخة الثانية التي ينفخها إسرافيل في الصور فيقوم الناس؟",
        "نفخة البعث والنشور",
        listOf("نفخة البعث والنشور", "نفخة الصعق", "نفخة الفزع", "نفخة التثبيت"),
        "النفخة الأولى للصعق والموت، والنفخة الثانية لإعادة الأرواح إلى أجسادها للبعث."
    ),
    UnseenQuestion(
        18,
        "ما هي أول وأكبر الشفاعات يوم القيامة لفصل القضاء وتختص بالنبي ﷺ؟",
        "الشفاعة العظمى (المقام المحمود)",
        listOf("الشفاعة العظمى (المقام المحمود)", "شفاعة الشهداء", "شفاعة القرآن", "شفاعة الأقارب"),
        "الشفاعة العظمى هي المقام المحمود الذي يطلبه الخلائق من النبي ﷺ لبدء الحساب."
    ),
    UnseenQuestion(
        19,
        "ما هي صفات الملائكة الكرام في طاعة الله تعالى كما جاء في القرآن؟",
        "لا يعصون الله ما أمرهم ويفعلون ما يؤمرون",
        listOf("لا يعصون الله ما أمرهم ويفعلون ما يؤمرون", "يأكلون ويشربون كالبشر", "يتناسلون ويولدون", "يصيبهم التعب والنعاس"),
        "الملائكة عباد مكرمون مطهرون لا يستكبرون عن عبادته ولا يعصون له أمراً."
    ),
    UnseenQuestion(
        20,
        "ما هو العرش العظيم بالنسبة لكافة المخلوقات؟",
        "أعظم المخلوقات وأعلاها وسقف الجنة",
        listOf("أعظم المخلوقات وأعلاها وسقف الجنة", "كوكب دُري في السماء", "سحابة بيضاء نورانية", "جبل من الياقوت"),
        "العرش هو أفق وسقف الفردوس الأعلى وأعظم مخلوقات الله تعالى قدرة وعظمة."
    ),
    UnseenQuestion(
        21,
        "ما هي حقيقة الكرسي المذكور في آية الكرسي المباركة؟",
        "موضع القدمين لله تعالى والعرش أعظم منه",
        listOf("موضع القدمين لله تعالى والعرش أعظم منه", "هو عين العرش العظيم", "سرير الملك في الجنة", "رمز العلم والقدرة"),
        "قال ابن عباس رضي الله عنهما: «الكرسي موضع القدمين والعرش لا يقدر قدره إلا الله»."
    ),
    UnseenQuestion(
        22,
        "من هم حملة العرش الكرام المذكورون في القرآن الكريم؟",
        "ثمانية أملاك من أعظم الملائكة خلقاً وقوة",
        listOf("ثمانية أملاك من أعظم الملائكة خلقاً وقوة", "أربعة من الأنبياء", "جميع ملائكة السماء السابعة", "سبعون ألف ملك"),
        "قال تعالى: ﴿وَيَحْمِلُ عَرْشَ رَبِّكَ فَوْقَهُمْ يَوْمَئِذٍ ثَمَانِيَةٌ﴾."
    ),
    UnseenQuestion(
        23,
        "ما هي جنة المأوى المذكورة في سورة النجم؟",
        "جنة في السماء السابعة عند سدرة المنتهى",
        listOf("جنة في السماء السابعة عند سدرة المنتهى", "روضة في القبر", "جبل في مكة", "نهر في الجنة"),
        "قال تعالى: ﴿عِنْدَ سِدْرَةِ الْمُنْتَهَى * عِنْدَهَا جَنَّةُ الْمَأْوَى﴾."
    ),
    UnseenQuestion(
        24,
        "ما هي صحف الأعمال التي تطير يوم القيامة؟",
        "سجلات مكتوب فيها كافة أعمال الإنسان حسناتها وسيئاتها",
        listOf("سجلات مكتوب فيها كافة أعمال الإنسان حسناتها وسيئاتها", "كتب الأنبياء القديمة", "أوراق الشجر بالجنة", "رسائل الملائكة للعباد"),
        "فآخذٌ كتابه بيمينه ومسرور بفضله، وآخذٌ كتابه بشماله أو من وراء ظهره."
    ),
    UnseenQuestion(
        25,
        "ما هو النعيم الأعظم الذي يتنعم به أهل الجنة ويغمر قلوبهم؟",
        "الرؤية والنظر إلى وجه الله الكريم سبحانه",
        listOf("الرؤية والنظر إلى وجه الله الكريم سبحانه", "الأكل والشراب في الجنة", "القصور والدور الذهبية", "الخلود بلا موت"),
        "قال تعالى: ﴿وُجُوهٌ يَوْمَئِذٍ نَاضِرَةٌ * إِلَى رَبِّهَا نَاظِرَةٌ﴾ وهو أعظم نعيم أهل الجنة."
    )
)

@Composable
fun AngelsAndUnseenQuizGame(onBack: () -> Unit) {
    val context = LocalContext.current
    var progressData by remember {
        mutableStateOf(StageProgressStore.loadProgress(context, GAME_NAME, TOTAL_STAGES))
    }
    var selectedStageIdx by remember { mutableStateOf<Int?>(null) }

    if (selectedStageIdx == null) {
        StageMapScreen(
            gameTitle = "عالم الملائكة والغيب",
            gameSubtitle = "٢٥ مرحلة لاستكشاف حقائق الإيمان والغيب",
            totalStages = TOTAL_STAGES,
            progressData = progressData,
            onSelectStage = { selectedStageIdx = it },
            onBack = onBack
        )
    } else {
        val stageIdx = selectedStageIdx!!
        val currentStage = UNSEEN_STAGES[stageIdx]

        AngelsAndUnseenPlayScreen(
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
private fun AngelsAndUnseenPlayScreen(
    stage: UnseenQuestion,
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

    fun handleOptionClick(opt: String) {
        if (isAnswered) return
        selectedOption = opt
        isAnswered = true

        val isCorrect = (opt == stage.correctAnswer)
        if (isCorrect) {
            val stars = when (mistakesCount) {
                0 -> 3
                1 -> 2
                else -> 1
            }
            earnedStars = stars
            onCompleteStage(stars)
            showWinDialog = true
        } else {
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
                        text = "عالم الملائكة والغيب",
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

            // Question Card
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
                        text = "سؤال عقيدة وإيمان بالغيب:",
                        color = TextSecondaryLinen,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = stage.question,
                        color = TextMain,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        lineHeight = 28.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

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
                        text = "إجابة غير صحيحة، حاول مجدداً واختر الحقيقة الشرعية!",
                        color = Color(0xFFFF8A80),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Fact Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .noorGlassCard(cornerRadius = 16.dp)
                    .padding(14.dp)
            ) {
                Column {
                    Text(
                        text = "✨ حقيقة وفائدة إيمانية:",
                        color = Gold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stage.fact,
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
                stageTitle = "المرحلة ${stageNumber.toArabicDigits()}",
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
