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

private const val GAME_NAME = "ZakatCalcGame"
private const val TOTAL_STAGES = 25

private data class ZakatScenario(
    val id: Int,
    val title: String,
    val details: String,
    val correctAnswer: String,
    val options: List<String>,
    val explanation: String
)

private val ZAKAT_STAGES = listOf(
    ZakatScenario(
        1,
        "زكاة النقود والمدخرات البسيطة",
        "يمتلك شخص مبلغ 100,000 درهم مدخرة في البنك وحال عليها الحول الكامل (عام هجري). كم مقدار الزكاة الواجبة؟ (نسبة الزكاة 2.5%)",
        "2,500 درهم",
        listOf("2,500 درهم", "5,000 درهم", "1,000 درهم", "10,000 درهم"),
        "الزكاة الواجبة في الأموال النقدية التي بلغت النصاب هي 2.5% (المبلغ ÷ 40). 100,000 ÷ 40 = 2,500 درهم."
    ),
    ZakatScenario(
        2,
        "زكاة الذهب المدخر",
        "رجل يمتلك 200 غرام من الذهب الخالص 24 قيراط كمقتنى ادخاري ومضى عليه عام هجري. كم مقدار الزكاة الواجبة بالغرامات؟ (نصاب الذهب 85 غراماً)",
        "5 غرامات",
        listOf("5 غرامات", "10 غرامات", "2.5 غرام", "20 غراماً"),
        "نصاب الذهب 85 غراماً، وبما أنه يملك 200 غرام فالزكاة هي 2.5% من الوزن. 200 × 2.5% = 5 غرامات ذهب."
    ),
    ZakatScenario(
        3,
        "زكاة المحاصيل المَسْقِيّة بماء المطر",
        "مزارع حصد 2,000 كغم من القمح بقعة أرض تُسقى بماء المطر دون كلفة ولا آلات. كم مقدار زكاتها؟ (المقدار الواجب 10%)",
        "200 كغم",
        listOf("200 كغم", "100 كغم", "50 كغم", "400 كغم"),
        "المزروعات التي تسقى بماء المطر دون كلفة يكون مقدار زكاتها العُشر (10%). 2,000 × 10% = 200 كغم."
    ),
    ZakatScenario(
        4,
        "زكاة عروض التجارة",
        "تاجر يملك بضائع ومخزوناً تجارياً بقيمة 40,000 دولار وحال عليها الحول. كم مقدار الزكاة الواجبة؟",
        "1,000 دولار",
        listOf("1,000 دولار", "2,000 دولار", "400 دولار", "2,500 دولار"),
        "تُقوَّم عروض التجارة بسعر السوق الحالي عند تمام الحول وتُخرج منها نسبة 2.5%. 40,000 × 2.5% = 1,000 دولار."
    ),
    ZakatScenario(
        5,
        "حساب زكاة السيولة المالية",
        "شخص لديه مدخرات بقيمة 20,000 ريال مر عليها سنة كاملة. كم زكاة هذا المال؟",
        "500 ريال",
        listOf("500 ريال", "1,000 ريال", "250 ريالاً", "2,000 ريال"),
        "الزكاة الواجبة = المبلغ ÷ 40. 20,000 ÷ 40 = 500 ريال."
    ),
    ZakatScenario(
        6,
        "زكاة الفضة",
        "شخص يمتلك 1,000 غرام من الفضة النقية المدخرة وتجاوزت النصاب (595 غراماً). كم زكاة الفضة الواجبة؟",
        "25 غراماً",
        listOf("25 غراماً", "50 غراماً", "10 غرامات", "15 غراماً"),
        "نصاب الفضة 595 غراماً، والنسبة الواجبة 2.5%. 1,000 × 2.5% = 25 غرام فضة."
    ),
    ZakatScenario(
        7,
        "زكاة الثمار المَسْقِيّة بالآلات والكلفة",
        "مزرعة تمور حصدت 3,000 كغم من التمر باستعمال آلات الري والمضخات المكلفة. كم الزكاة الواجبة؟ (المقدار 5%)",
        "150 كغم",
        listOf("150 كغم", "300 كغم", "75 كغم", "600 كغم"),
        "المزروعات المَسْقِيّة بكلفة وآلات يكون مقدار زكاتها نصف العُشر (5%). 3,000 × 5% = 150 كغم."
    ),
    ZakatScenario(
        8,
        "زكاة الإبل والبقر (الأنعام)",
        "مزارع يملك 30 بقرة سائمة تُرى في المراعي ومضى عليها حول كامل. ما هي الزكاة الواجبة في 30 من البقر؟",
        "تبيع (عجل أتم سنة)",
        listOf("تبيع (عجل أتم سنة)", "مسنة (بقرة أتمت سنتين)", "شاة واحدة", "لا زكاة فيها"),
        "في كل 30 من البقر تبيع أو تبيعة (عجل أو عجلة أتمت سنة ودخلت في الثانية)."
    ),
    ZakatScenario(
        9,
        "زكاة الغنم والماشية",
        "مربّي مواشي لديه 40 شاة سائمة مر عليها الحول الهجري. ما الواجب في نصاب الغنم (40 - 120)؟",
        "شاة واحدة",
        listOf("شاة واحدة", "شاتان", "شاة عن كل 10", "لا زكاة فيها"),
        "نصاب الغنم يبدأ من 40 شاة، وفيها شاة واحدة فقط حتى تبلغ 121 شاة."
    ),
    ZakatScenario(
        10,
        "مال لم يبلغ النصاب",
        "شخص يمتلك مبلغ 1,000 ريال فقط ولم يبلغ قيمة نصاب الذهب (85غ) ولا الفضة (595غ). كم زكاته؟",
        "لا تجب فيه الزكاة (لم يبلغ النصاب)",
        listOf("لا تجب فيه الزكاة (لم يبلغ النصاب)", "25 ريالاً", "100 ريال", "10 ريالات"),
        "من شروط وجوب الزكاة بلوغ النِصاب الشرعي، والمال الذي لم يبلغ النصاب لا زكاة فيه."
    ),
    ZakatScenario(
        11,
        "زكاة المكتسبات والرواتب المدخرة",
        "موظف يدخر من راتبه شهرياً حتى تجمع لديه 60,000 درهم وحال الحول على أول الدفعات. كم زكاتها؟",
        "1,500 درهم",
        listOf("1,500 درهم", "3,000 درهم", "600 درهم", "6,000 درهم"),
        "60,000 ÷ 40 = 1,500 درهم (نسبة 2.5% على كامل المبلغ المدخر حال الحول)."
    ),
    ZakatScenario(
        12,
        "زكاة الذهب المستعمل والزينة",
        "امرأة تملك ذهباً للزينة الشخصية المعتادة وزنه 100 غرام. ما حكم ورأي الجمهور في زكاة حلي الزينة المباحة؟",
        "جمهور الفقهاء: لا زكاة في حلي الزينة الاستعمالي المباح",
        listOf("جمهور الفقهاء: لا زكاة في حلي الزينة الاستعمالي المباح", "تجب فيه الزكاة حتماً 2.5%", "تجب فيه زكاة 10%", "يُحرم لبسه"),
        "جمهور العلماء (المالكية والشافعية والحنابلة) على عدم زكاة حلي الزينة المباح إلا إذا اتخذ للادخار."
    ),
    ZakatScenario(
        13,
        "زكاة الأسهم الاستثمارية والشركات",
        "مستثمر يملك أسهماً في شركة تجارية بقيمة سوقية 200,000 ريال مر عليها الحول، والشركة تتاجر وتبيع. كم الزكاة؟",
        "5,000 ريال",
        listOf("5,000 ريال", "10,000 ريال", "2,000 ريال", "25,000 ريال"),
        "الأسهم في الشركات التجارية تعامل معاملة عروض التجارة وتزكى بنسبة 2.5% من القيمة السوقية (200,000 × 2.5% = 5,000)."
    ),
    ZakatScenario(
        14,
        "زكاة العقارات المؤجرة",
        "شخص يمتلك عمارة سكنية بقيمة 2,000,000 ريال مخصصة للإيجار، وبلغ الصافي السنوي من الإيجارات 100,000 ريال وحال الحول. كيف تزكى؟",
        "تُزكى الإيرادات المدخرة فقط (2.5% من الإيجار الصافي = 2,500)",
        listOf("تُزكى الإيرادات المدخرة فقط (2.5% من الإيجار الصافي = 2,500)", "تُزكى أصل العمارة بـ 2.5% (50,000)", "تُزكى أصل العمارة بـ 10%", "لا زكاة في الإيجار ولا العقار"),
        "الأصول الثابتة كالعمائر والمباني المخصصة للإيجار لا زكاة في أصل قيمتها، وإنما تزكى أرباحها وإيراداتها إذا بلغت النصاب وحال عليها الحول."
    ),
    ZakatScenario(
        15,
        "زكاة الدين المرجو الأداء",
        "أقرضت صديقاً موسراً أميناً مبلغ 30,000 ريال ويستطيع رده في أي وقت، ومضى عام. كيف تزكى؟",
        "تزكى سنوياً كأنها في حوزتك (750 ريالاً)",
        listOf("تزكى سنوياً كأنها في حوزتك (750 ريالاً)", "لا تزكى حتى تقبضها", "تزكى بـ 10%", "تسقط زكاتها للطرفين"),
        "الدين على مليء (موسر قادر على السداد) يعتبر كالمال الحاضر فيزكيه صاحبه كل عام."
    ),
    ZakatScenario(
        16,
        "زكاة الدين الميؤوس منه (المعسر)",
        "أقرضت شخصاً معسراً 50,000 ريال وتعذر سداده لسنوات، ثم سدده فجأة بعد 5 سنوات. كيف تزكى؟",
        "تُزكى لسنة واحدة فقط عند قبضه (1,250 ريالاً)",
        listOf("تُزكى لسنة واحدة فقط عند قبضه (1,250 ريالاً)", "تُزكى عن الـ 5 سنوات كاملة", "لا تزكى أبداً", "تُزكى بـ 50%"),
        "الدين على المعسر أو المماطل إذا قُبض يُزكى لسنة واحدة فقط على الراجح من أقوال أهل العلم."
    ),
    ZakatScenario(
        17,
        "زكاة الشركاء في التجارة",
        "شريكان في محل تجاري، رأس المال والبضائع بقيمة 80,000 درهم مناصفة بينهما. كم زكاة كل شريك بعد تمام الحول؟",
        "1,000 درهم لكل شريك (إجمالي المحل 2,000)",
        listOf("1,000 درهم لكل شريك (إجمالي المحل 2,000)", "2,000 درهم لكل شريك", "500 درهم لكل شريك", "4,000 درهم إجمالي"),
        "إجمالي زكاة المحل 80,000 ÷ 40 = 2,000 درهم، فيخص كل شريك بحسب حصته النصف (1,000 درهم)."
    ),
    ZakatScenario(
        18,
        "زكاة الركاز والكنوز القديمة",
        "عثر شخص في أرضه على ركاز (دفين وكنز جاهلي قديم) قيمته 50,000 ريال. كم المقدار الواجب إخراجه فوراً؟",
        "الخُمس 20% (10,000 ريال) فور العثور عليه دون اشتراط حول",
        listOf("الخُمس 20% (10,000 ريال) فور العثور عليه دون اشتراط حول", "2.5% فقط بعد حول", "10% بعد حول", "لا زكاة فيه"),
        "قال النبي ﷺ: «وفي الركاز الخُمس» (20%) وتخرج فوراً بدون اشتراط مضي حول."
    ),
    ZakatScenario(
        19,
        "زكاة العسل الطبيعي",
        "نحّال أنتج من مناحله 500 كغم من العسل الخالص وتجاوز النصاب. كم مقدار زكاته عند من أوجبها؟ (الخُمس أو العُشر)",
        "العُشر 10% (50 كغم) عند أصحاب هذا القول",
        listOf("العُشر 10% (50 كغم) عند أصحاب هذا القول", "2.5% (12.5 كغم)", "20% (100 كغم)", "لا زكاة في العسل باتفاق"),
        "ذهب بعض الأئمة كأحمد وأبي حنيفة إلى وجوب الزكاة في العسل بـ 10% إذا بلغ النصاب (ما يعادل 160 رطلاً)."
    ),
    ZakatScenario(
        20,
        "زكاة الأرض المعدة للتجارة والبيع",
        "اشتريت قطعة أرض بقيمة 500,000 ريال بنية بيعها والتجارة بها عند ارتفاع الأسعار، وحال الحول. كم زكاتها؟",
        "12,500 ريال (2.5% من القيمة الحالية عند تمام الحول)",
        listOf("12,500 ريال (2.5% من القيمة الحالية عند تمام الحول)", "لا زكاة فيها حتى تُباع", "25,000 ريال", "50,000 ريال"),
        "الأراضي المعدة للبيع والتجارة عروض تجارة تزكى بسعرها وقت تمام الحول بنسبة 2.5%."
    ),
    ZakatScenario(
        21,
        "زكاة الأرض السكنية (البناء والمأوى)",
        "شخص اشترى قطعة أرض بقيمة 800,000 ريال بنية إقامة منزل لسكنه الخاص وأسرته. كم زكاتها؟",
        "لا زكاة فيها (لأنها للقنية والسكن الخاص)",
        listOf("لا زكاة فيها (لأنها للقنية والسكن الخاص)", "20,000 ريال (2.5%)", "80,000 ريال", "10,000 ريال"),
        "الأراضي والعقارات والسيارات المخصصة للاستعمال الشخصي والقنية لا تجب فيها الزكاة."
    ),
    ZakatScenario(
        22,
        "حساب زكاة الفطر للأسرة",
        "رب أسرة مكونة من 6 أفراد (الأب والأم و4 أبناء). كم مقدار زكاة الفطر الواجبة عليهم صاعاً ووزناً تقريبياً؟",
        "6 أصوع (حوالي 15 إلى 18 كغم أرز/قمح)",
        listOf("6 أصوع (حوالي 15 إلى 18 كغم أرز/قمح)", "3 أصوع (7.5 كغم)", "12 صاعاً (30 كغم)", "صاع واحد للجميع"),
        "تجب زكاة الفطر صاعاً نبوياً (حوالي 2.5 إلى 3 كغم) عن كل فرد مسلم صغيراً أو كبيراً."
    ),
    ZakatScenario(
        23,
        "تعجيل إخراج الزكاة قبل وقتها",
        "شخص لديه مال وجبت زكاته في رمضان، ولكن نزل بقرابته الفقراء حاجة شديدة في رجب فدفع لهم الزكاة مقدماً. ما حكم ذلك؟",
        "يجوز تعجيل الزكاة قبل تمام الحول للحاجة والمصلحة",
        listOf("يجوز تعجيل الزكاة قبل تمام الحول للحاجة والمصلحة", "لا يجوز وتبطل الزكاة", "تُعتبر صدقة تطوع ويلزمه إعادتها", "تستوجب الكفارة"),
        "يجوز تعجيل إخراج الزكاة لسنة أو سنتين للحاجة والظروف الطارئة كما فعل النبي ﷺ مع العباس رضي الله عنه."
    ),
    ZakatScenario(
        24,
        "خصم الديون من المال الزكوي",
        "تاجر يملك بضائع وسيليلة بقيمة 100,000 ريال وعليها ديون حالة للغير بقيمة 30,000 ريال. كيف يحسب وعاء الزكاة على القول بخصم الدين؟",
        "يخصم الدين ويخرج الزكاة عن الصافي 70,000 ريال (1,750 ريالاً)",
        listOf("يخصم الدين ويخرج الزكاة عن الصافي 70,000 ريال (1,750 ريالاً)", "يخرج الزكاة عن 100,000 كاملة دون خصم", "تسقط عنه الزكاة تماماً", "يضاعف الزكاة لتغطية الدين"),
        "عند المانعين من الزكاة في قدر الدين تُخصم الديون الملازمة الحالة فيكون الوعاء الزكوي 70,000 وزكاتها 1,750 ريالاً."
    ),
    ZakatScenario(
        25,
        "التحدي الشامل: حساب زكاة ثروة متكاملة",
        "رجل يملك 120,000 ريال نقداً + ذهب ادخاري بـ 40,000 ريال + بضائع تجارية بـ 40,000 ريال، وحال عليها الحول (الإجمالي = 200,000 ريال). كم زكاة ثروته؟",
        "5,000 ريال (إجمالي الثروة 200,000 ÷ 40)",
        listOf("5,000 ريال (إجمالي الثروة 200,000 ÷ 40)", "2,500 ريال", "10,000 ريال", "20,000 ريال"),
        "تُضم الأموال الزكوية النقدية والذهب وعروض التجارة لبعضها للوصول للوعاء وتزكى بنسبة 2.5% (200,000 ÷ 40 = 5,000 ريال)."
    )
)

@Composable
fun ZakatCalcGame(onBack: () -> Unit) {
    val context = LocalContext.current
    var progressData by remember {
        mutableStateOf(StageProgressStore.loadProgress(context, GAME_NAME, TOTAL_STAGES))
    }
    var selectedStageIdx by remember { mutableStateOf<Int?>(null) }

    if (selectedStageIdx == null) {
        StageMapScreen(
            gameTitle = "عداد الزكاة",
            gameSubtitle = "٢٥ مرحلة لتطبيقات وحساب أحكام الزكاة",
            totalStages = TOTAL_STAGES,
            progressData = progressData,
            onSelectStage = { selectedStageIdx = it },
            onBack = onBack
        )
    } else {
        val stageIdx = selectedStageIdx!!
        val currentStage = ZAKAT_STAGES[stageIdx]

        ZakatCalcPlayScreen(
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
private fun ZakatCalcPlayScreen(
    stage: ZakatScenario,
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
                        text = "عداد الزكاة",
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

            // Title Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .noorGlassCard(cornerRadius = 16.dp)
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stage.title,
                    color = Gold,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Scenario Details Card
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
                        text = "مسألة المرحلة الحسابية:",
                        color = TextSecondaryLinen,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = stage.details,
                        color = TextMain,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        lineHeight = 26.sp
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
                        text = "حساب غير دقيق، حاول مجدداً وطبق المعادلة الشرعية الصحيحة!",
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
                        text = "📐 القاعدة وحساب الزكاة:",
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
                stageTitle = stage.title,
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
