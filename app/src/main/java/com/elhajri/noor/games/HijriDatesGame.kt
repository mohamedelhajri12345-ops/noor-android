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

private const val GAME_NAME = "HijriDatesGame"
private const val TOTAL_STAGES = 25

private data class HijriEvent(
    val id: Int,
    val title: String,
    val context: String,
    val correctDate: String,
    val options: List<String>,
    val fact: String
)

private val HIJRI_EVENTS = listOf(
    HijriEvent(
        1,
        "غزوة بدر الكبرى (يوم الفرقان)",
        "أول معركة فاصلة بين المسلمين وقريش وانتصار الفئة القليلة بإذن الله",
        "17 رمضان 2 هـ",
        listOf("17 رمضان 2 هـ", "1 شوال 3 هـ", "10 محرم 1 هـ", "27 رجب 2 هـ"),
        "حدثت معركة بدر في 17 رمضان سنة 2 هـ واستُشهد فيها 14 صحابياً رضوان الله عليهم."
    ),
    HijriEvent(
        2,
        "فتح مكة المكرمة",
        "دخول النبي ﷺ فاتحاً ومُطهراً للكعبة المشرفة دون قتال",
        "20 رمضان 8 هـ",
        listOf("20 رمضان 8 هـ", "10 ذو الحجة 8 هـ", "15 شعبان 7 هـ", "12 ربيع الأول 9 هـ"),
        "دخل النبي ﷺ مكة في 20 رمضان سنة 8 هـ وعفا عن أهلها قائلاً: اذهبوا فأنتم الطلقاء."
    ),
    HijriEvent(
        3,
        "يوم عرفة المبارك",
        "ركن الحج الأكبر ووقوف المسلمين بمشعر عرفات المبارك",
        "9 ذو الحجة",
        listOf("9 ذو الحجة", "10 ذو الحجة", "8 ذو الحجة", "1 ذو الحجة"),
        "يوم عرفة هو اليوم التاسع من شهر ذي الحجة وصيامه لغير الحاج يُكفر سنتين."
    ),
    HijriEvent(
        4,
        "يوم عاشوراء",
        "اليوم الذي نجى الله فيه نبي الله موسى عليه السلام وقومه من فرعون",
        "10 محرم",
        listOf("10 محرم", "1 محرم", "15 محرم", "9 محرم"),
        "صام النبي ﷺ يوم العاشر من محرم وأمر المسلمين بصيامه شُكراً لله تعالى."
    ),
    HijriEvent(
        5,
        "الهجرة النبوية إلى المدينة المنورة",
        "وصول النبي ﷺ وصاحبه أبي بكر إلى قباء وتأسيس دولة الإسلام",
        "ربيع الأول 1 هـ",
        listOf("ربيع الأول 1 هـ", "رمضان 1 هـ", "محرم 1 هـ", "ذو القعدة 2 هـ"),
        "وصل النبي ﷺ إلى المدينة في ربيع الأول سنة 1 هـ، واختير شهر محرم بدايةً للتقويم."
    ),
    HijriEvent(
        6,
        "معركة الخندق (الأحزاب)",
        "تحزب القبائل وحصار المدينة المنورة وحفر الخندق حولها",
        "شوال 5 هـ",
        listOf("شوال 5 هـ", "صفر 4 هـ", "رجب 6 هـ", "شعبان 3 هـ"),
        "حُفر الخندق بمشورة سلمان الفارسي رضي الله عنه وانتهت بريح صرصر أرسلها الله."
    ),
    HijriEvent(
        7,
        "صلح الحديبية الخالد",
        "المعاهدة والميثاق بين النبي ﷺ وقريش والتمهيد لانتشار الإسلام",
        "ذو القعدة 6 هـ",
        listOf("ذو القعدة 6 هـ", "رمضان 6 هـ", "شعبان 5 هـ", "محرم 7 هـ"),
        "سُمي صلح الحديبية بالفتح المبين في القرآن الكريم: ﴿إِنَّا فَتَحْنَا لَكَ فَتْحًا مُبِينًا﴾."
    ),
    HijriEvent(
        8,
        "غزوة أحد",
        "المواجهة عند جبل أحد واستشهاد سيد الشهداء حمزة بن عبد المطلب",
        "شوال 3 هـ",
        listOf("شوال 3 هـ", "رمضان 2 هـ", "ربيع الثاني 4 هـ", "ذو الحجة 3 هـ"),
        "وقعت غزوة أحد في منتصف شهر شوال سنة 3 هـ وكان فيها درس عظيم في طاعة الرسول."
    ),
    HijriEvent(
        9,
        "حجة الوداع وخطبة عرفة",
        "الحجة الوحيدة التي أداها النبي ﷺ وخطبته الخالدة في حقوق الإنسان",
        "ذو الحجة 10 هـ",
        listOf("ذو الحجة 10 هـ", "ذو القعدة 9 هـ", "شوال 10 هـ", "ربيع الأول 11 هـ"),
        "أدى النبي ﷺ حجة الوداع في ذي الحجة سنة 10 هـ ونزلت فيها: ﴿الْيَوْمَ أَكْمَلْتُ لَكُمْ دِينَكُمْ﴾."
    ),
    HijriEvent(
        10,
        "عيد الفطر المبارك",
        "يوم الفرح والاحتفال بتمام صيام شهر رمضان المبارك",
        "1 شوال",
        listOf("1 شوال", "30 رمضان", "10 ذو الحجة", "1 محرم"),
        "شرع الله عيد الفطر في اليوم الأول من شهر شوال ويُحرم صوم هذا اليوم."
    ),
    HijriEvent(
        11,
        "غزوة خيبر وزوال حصونها",
        "فتح حصون خيبر والقضاء على التآمر والغدر في الجزيرة",
        "محرم 7 هـ",
        listOf("محرم 7 هـ", "رمضان 6 هـ", "ربيع الأول 8 هـ", "شوال 5 هـ"),
        "فتحت خيبر في سنة 7 هـ وأعطى النبي ﷺ الراية لعلي بن أبي طالب رضي الله عنه."
    ),
    HijriEvent(
        12,
        "غزوة تبوك (غزوة العسرة)",
        "آخر غزوة قادها النبي ﷺ بنفسه لمواجهة الروم في الشمال",
        "رجب 9 هـ",
        listOf("رجب 9 هـ", "شعبان 8 هـ", "رمضان 10 هـ", "ذو القعدة 7 هـ"),
        "خرج المسلمون في حر شديد ومسافة بعيدة فسميت غزوة العسرة وتبرع الصحابة بأموالهم."
    ),
    HijriEvent(
        13,
        "تحويل القبلة إلى الكعبة المشرفة",
        "التحول من التوجه في الصلاة نحو بيت المقدس إلى البيت الحرام",
        "نصف شعبان 2 هـ",
        listOf("نصف شعبان 2 هـ", "17 رمضان 1 هـ", "1 محرم 3 هـ", "10 ذو الحجة 2 هـ"),
        "تحولت القبلة في السنة الثانية للهجرة بعد أن صلى المسلمون نحو بيت المقدس 16 أو 17 شهراً."
    ),
    HijriEvent(
        14,
        "غزوة حنين وإرساء التوكل",
        "المواجهة ضد هوازن وثقيف بعد فتح مكة وثبات النبي ﷺ",
        "شوال 8 هـ",
        listOf("شوال 8 هـ", "رمضان 8 هـ", "ربيع الأول 9 هـ", "ذو القعدة 7 هـ"),
        "وقعت حنين في شوال 8 هـ وتجلى فيها درس عدم الاغترار بالكثرة والاعتماد على الله."
    ),
    HijriEvent(
        15,
        "وفاة النبي الكريم ﷺ",
        "انتقال الرفيق الأعلى وسيد الخلق ﷺ إلى جوار ربه",
        "12 ربيع الأول 11 هـ",
        listOf("12 ربيع الأول 11 هـ", "1 رمضان 10 هـ", "10 ذو الحجة 11 هـ", "27 رجب 12 هـ"),
        "توفي النبي ﷺ يوم الاثنين 12 ربيع الأول سنة 11 هـ بعد أن أدى الأمانة وبلغ الرسالة."
    ),
    HijriEvent(
        16,
        "هدم الأصنام حول الكعبة",
        "تطهير البيت الحرام وإزالة 360 صنماً بعد الفتح",
        "رمضان 8 هـ",
        listOf("رمضان 8 هـ", "شوال 9 هـ", "ذو الحجة 7 هـ", "محرم 10 هـ"),
        "طهر النبي ﷺ الكعبة وهو يطعن الأصنام بقضيبه ويقول: ﴿وَقُلْ جَاءَ الْحَقُّ وَزَهَقَ الْبَاطِلُ﴾."
    ),
    HijriEvent(
        17,
        "عام الوفود للباب النبوي",
        "توافد القبائل العربية من كافة الأنحاء لإعلان إسلامها بالمدينة",
        "السنة 9 هـ",
        listOf("السنة 9 هـ", "السنة 5 هـ", "السنة 7 هـ", "السنة 11 هـ"),
        "سُميت السنة التاسعة للهجرة بعام الوفود لكثرة القبائل التي جاءت تسلم بين يدي الرسول."
    ),
    HijriEvent(
        18,
        "حادثة الإسراء والمعراج",
        "الرحلة النبوية المباركة من المسجد الحرام إلى الأقصى ثم إلى السوات العلى",
        "27 رجب (قبل الهجرة)",
        listOf("27 رجب (قبل الهجرة)", "17 رمضان (بعد الهجرة)", "10 ذو الحجة", "15 شعبان"),
        "أُسري بالنبي ﷺ وفرضت في تلك الليلة العظيمة صلاة الفريضة الخمس."
    ),
    HijriEvent(
        19,
        "غزوة مؤتة وبسالة القادة",
        "المواجهة الأولى ضد جيوش الروم واستشهاد القادة الثلاثة",
        "جمادى الأولى 8 هـ",
        listOf("جمادى الأولى 8 هـ", "رجب 7 هـ", "صفر 6 هـ", "رمضان 9 هـ"),
        "استشهد فيها زيد بن حارثة وجعفر بن أبي طالب وعبد الله بن رواحة ثم قاد خالد بن الوليد الانسحاب."
    ),
    HijriEvent(
        20,
        "فتح بيت المقدس في عهد عمر",
        "تسلم أمير المؤمنين عمر بن الخطاب رضي الله عنه مفاتيح القدس",
        "السنة 15 هـ",
        listOf("السنة 15 هـ", "السنة 20 هـ", "السنة 10 هـ", "السنة 25 هـ"),
        "دخل عمر بن الخطاب القدس فاتحاً وكتب لأهلها العهدة العمرية الخالدة في الأمان والحرية."
    ),
    HijriEvent(
        21,
        "جمع القرآن الكريم في مصحف واحد",
        "جمع القرآن في عهد أبي بكر الصديق بمشورة عمر وإشراف زيد بن ثابت",
        "السنة 12 هـ",
        listOf("السنة 12 هـ", "السنة 18 هـ", "السنة 25 هـ", "السنة 30 هـ"),
        "جُمع المصحف الشريف بعد معركة اليمامة واستشهاد جمع من القراء والحفاظ."
    ),
    HijriEvent(
        22,
        "معركة اليرموك الفاصلة",
        "انتصار جيش المسلمين العظيم على الامبراطورية البيزنطية في الشام",
        "رجب 13 هـ",
        listOf("رجب 13 هـ", "رمضان 15 هـ", "شوال 10 هـ", "محرم 18 هـ"),
        "قاد المسلمين فيها خالد بن الوليد وأنهت الوجود البيزنطي في بلاد الشام."
    ),
    HijriEvent(
        23,
        "معركة القادسية في العراق",
        "انتصار المسلمين بقيادة سعد بن أبي وقاص على الفارسية",
        "شعبان 15 هـ",
        listOf("شعبان 15 هـ", "محرم 12 هـ", "ذو القعدة 20 هـ", "ربيع الأول 14 هـ"),
        "كانت القادسية معركة فاصلة فتحت العراق والدائن أمام نشر نور الإسلام."
    ),
    HijriEvent(
        24,
        "وثيقة وصحيفة المدينة المنورة",
        "إقرار أول دستور مدني ينظم التعايش بين المهاجرين والأنصار واليهود",
        "السنة 1 هـ",
        listOf("السنة 1 هـ", "السنة 3 هـ", "السنة 5 هـ", "السنة 7 هـ"),
        "كتب النبي ﷺ الصحيفة فور وصوله للمدينة لتنظيم الحقوق والواجبات والمواطنة."
    ),
    HijriEvent(
        25,
        "اعتماد التأريخ بالتقويم الهجري",
        "اتخاذ الهجرة النبوية بداية للتقويم الإسلامي في عهد عمر بن الخطاب",
        "السنة 17 هـ",
        listOf("السنة 17 هـ", "السنة 10 هـ", "السنة 23 هـ", "السنة 30 هـ"),
        "جمع عمر الصحابة واستقر رأيهم على التأريخ بالهجرة النبوية ابتداءً من شهر محرم."
    )
)

@Composable
fun HijriDatesGame(onBack: () -> Unit) {
    val context = LocalContext.current
    var progressData by remember {
        mutableStateOf(StageProgressStore.loadProgress(context, GAME_NAME, TOTAL_STAGES))
    }
    var selectedStageIdx by remember { mutableStateOf<Int?>(null) }

    if (selectedStageIdx == null) {
        StageMapScreen(
            gameTitle = "لغز التواريخ الهجرية",
            gameSubtitle = "٢٥ مرحلة لاستكشاف الأحداث والتواريخ الهجرية",
            totalStages = TOTAL_STAGES,
            progressData = progressData,
            onSelectStage = { selectedStageIdx = it },
            onBack = onBack
        )
    } else {
        val stageIdx = selectedStageIdx!!
        val currentStage = HIJRI_EVENTS[stageIdx]

        HijriDatesPlayScreen(
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
private fun HijriDatesPlayScreen(
    stage: HijriEvent,
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

        val isCorrect = (opt == stage.correctDate)
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
                        text = "التواريخ الهجرية",
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

            // Event Context Card
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
                        text = "حدد التاريخ والتوقيت الهجري الصحيح:",
                        color = TextSecondaryLinen,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = stage.context,
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
                    val isCorrectOpt = (optionText == stage.correctDate)

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

            if (isAnswered && selectedOption != stage.correctDate) {
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .noorGlassCard(cornerRadius = 14.dp)
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "تاريخ غير دقيق، حاول مجدداً وطابق الحدث بالتاريخ الصحيح!",
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
                        text = "📜 إضاءة تاريخية:",
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
