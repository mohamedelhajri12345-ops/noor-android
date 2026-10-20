import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
package com.elhajri.noor.games

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
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
import com.elhajri.noor.ui.TextMain
import com.elhajri.noor.ui.TextSecondaryLinen
import com.elhajri.noor.ui.noorGlassCard
import com.elhajri.noor.ui.themeScreenBackground
import kotlinx.coroutines.delay

private const val GAME_NAME = "WuduStepsGame"

private val WUDU_STAGES = listOf(
    GameOrderingStage(
        id = 1,
        title = "فرائض الوضوء الستة",
        description = "رتب أركان الوضوء الستة المفروضة بالترتيب الشرعي الصحيح",
        steps = listOf(
            "النية وغسل الوجه كاملاً",
            "غسل اليدين مع المرفقين",
            "مسح جميع الرأس ومنه الأذنان",
            "غسل الرجلين مع الكعبين",
            "الترتيب بين الأعضاء",
            "الموالاة بين أفعال الوضوء"
        )
    ),
    GameOrderingStage(
        id = 2,
        title = "خطوات الغسل التام من الجنابة",
        description = "رتب صفة غسل النبي ﷺ التام من الجنابة بالترتيب الصحيح",
        steps = listOf(
            "النية وغسل اليدين ثلاثاً",
            "غسل الفرج وما لوث البدن",
            "الوضوء الكامل كوضوء الصلاة",
            "صب الماء على الرأس ثلاثاً مع تخلليل الشعر",
            "إفاضة الماء على الشق الأيمن ثم الأيسر",
            "غسل سائر الجسد وتعميمه بالماء",
            "غسل القدمين وتنحي المكان"
        )
    ),
    GameOrderingStage(
        id = 3,
        title = "كيفية التيمم الشرعي",
        description = "رتب خطوات التيمم بالتراب الطاهر عند عذر الماء",
        steps = listOf(
            "النية والتسمية بسم الله",
            "ضرب الكفين على الصعيد الطاهر ضربة واحدة",
            "النفخ في الكفين لتخفيف التراب",
            "مسح الوجه بالكفين",
            "مسح ظاهر الكفين ببعضهما"
        )
    ),
    GameOrderingStage(
        id = 4,
        title = "ترتيب نواقض الوضوء حسب القوة",
        description = "رتب المبطلات للطهارة من الحدث الأصغر إلى الأكبر",
        steps = listOf(
            "الخارج من السبيلين (بول أو غائط)",
            "خروج الريح بيقين",
            "زوال العقل بالنوم العميق أو الإغماء",
            "مس الفرج باليد مباشرة دون حائل",
            "أكل لحم الإبل (الجزور)",
            "الردة عن الإسلام والعياذ بالله"
        )
    ),
    GameOrderingStage(
        id = 5,
        title = "خطوات الوضوء الكاملة مع السنن",
        description = "رتب أفعال الوضوء الشاملة السنن والفرائض من النية للختام",
        steps = listOf(
            "النية والتسمية بسم الله",
            "غسل الكفين ثلاث مرات",
            "المضمضة والاستنشاق والاستنثار ثلاثاً",
            "غسل الوجه كاملاً مع تخلليل اللحية",
            "غسل اليدين إلى المرفقين ثلاثاً بدءاً باليمنى",
            "مسح الرأس والأذنين مرة واحدة",
            "غسل الرجلين إلى الكعبين ثلاثاً بدءاً باليمنى",
            "قول الذكر المأثور: أشهد أن لا إله إلا الله..."
        )
    ),
    GameOrderingStage(
        id = 6,
        title = "غسل الجمعة والمناسبات",
        description = "رتب سنن وآداب الغسل والتطيب ليوم الجمعة",
        steps = listOf(
            "النية والاستعداد للجمعة",
            "إزالة الأذى وغسل الفرج",
            "الوضوء الكامل وتحضير الطيب",
            "إفاضة الماء وتعميم الجسد بالغسل",
            "لبس أحسن الثياب والتطيب",
            "التبكير للمسجد والسكينة"
        )
    ),
    GameOrderingStage(
        id = 7,
        title = "أحكام التيمم في السفر والمرض",
        description = "رتب مراحل الاستدلال والترخيص بالتيمم للمسافر والمريض",
        steps = listOf(
            "البحث عن الماء عند القدرة",
            "التحقق من العذر (فقد الماء أو خوف الضرر)",
            "قصد الصعيد الطاهر (التراب أو الحجر)",
            "النية والتيمم بالضرب والمسح",
            "أداء الصلاة المكتوبة في وقتها"
        )
    ),
    GameOrderingStage(
        id = 8,
        title = "مراتب نواقض وطهارة الخلاء",
        description = "رتب آداب دخول الخلاء والتطهر بالترتيب الشرعي",
        steps = listOf(
            "دعاء الدخول (اللهم إني أعوذ بك من الخبث والخبائث)",
            "تقديم القدم اليسرى عند الدخول",
            "قضاء الحاجة دون استقبال القبلة أو استدبارها",
            "الاستنجاء بالماء أو الاستجمار بطاهر",
            "تقديم القدم اليمنى عند الخروج وقول غفرانك"
        )
    ),
    GameOrderingStage(
        id = 9,
        title = "سنن الوضوء المستحبة",
        description = "رتب سنن الوضوء المؤكدة غير الفرائض",
        steps = listOf(
            "السواك عند المضمضة",
            "غسل الكفين ثلاثاً قبل الوضوء",
            "البدء بالتيامن (الأيمن قبل الأيسر)",
            "تثليث الغسل ثلاثاً ثلاثاً",
            "تخلليل أصل اللحية الكثيفة وأصابع اليدين والرجلين",
            "دعاء ما بعد الوضوء وركعتا السنة"
        )
    ),
    GameOrderingStage(
        id = 10,
        title = "الغسل المجزئ بدون زيادة",
        description = "رتب الحد الأدنى المجزئ لرفع الحدث الأكبر",
        steps = listOf(
            "نية رفع الحدث الأكبر",
            "التسمية بسم الله",
            "المضمضة والاستنشاق",
            "عموم الماء على سائر الجسد والشعر"
        )
    ),
    GameOrderingStage(
        id = 11,
        title = "صفة أفعال التيمم الدقيقة",
        description = "رتب أفعال التيمم بالتفصيل الدقيق من السلسلة النبوية",
        steps = listOf(
            "النية لرفع الاستباحة للصلاة",
            "وضع الكفين على التراب الطاهر",
            "مسح الوجه ببطون الأصابع والراحة",
            "مسح كف اليمين بكف الشمال واليسار باليمين"
        )
    ),
    GameOrderingStage(
        id = 12,
        title = "شروط الصلاة المتوقفة على الطهارة",
        description = "رتب الشروط الخمسة للطهارة لإقامة الصلاة",
        steps = listOf(
            "دخول وقت الصلاة",
            "طهارة الحدث الأصغر بالوضوء",
            "طهارة الحدث الأكبر بالغسل",
            "طهارة البدن والثوب والمكان من الخبث",
            "ستر العورة واستقبال القبلة"
        )
    ),
    GameOrderingStage(
        id = 13,
        title = "المسح على الخفين والجوربين",
        description = "رتب خطوات وأحكام رخصة المسح على الخفين",
        steps = listOf(
            "لبس الخفين أو الجوربين على طهارة كاملة بالماء",
            "حدوث الحدث الأصغر في مدة المسح",
            "بل اليدين بالماء النظيف",
            "مسح أعلى الخفين مرة واحدة من الأصابع إلى الساق",
            "احتساب المدة (يوم وليلة للمقيم وثلاثة أيام للمسافر)"
        )
    ),
    GameOrderingStage(
        id = 14,
        title = "ترتيب غسل الميت وتكفينه",
        description = "رتب خطوات غسل الميت وتجهيزه للصلاة",
        steps = listOf(
            "ستر عورة الميت وتجريده لطيفاً",
            "وضوؤه كوضوء الصلاة دون إدخال الماء لفمه",
            "غسل رأسه ولحيته برغوة السدر",
            "غسل شقه الأيمن ثم الأيسر بالماء والسدر",
            "جعل الكافور في الغسلة الأخيرة",
            "تنشيفه وتكفينه في الأثواب البيضاء"
        )
    ),
    GameOrderingStage(
        id = 15,
        title = "الطهور مع الجبيرة والضماد",
        description = "رتب كيفية الطهارة مع وجود كسر أو جرح معصوب",
        steps = listOf(
            "غسل الأعضاء الصحيحة السليمة بالماء",
            "المسح بالماء على الجبيرة أو الضماد إن لم يضر",
            "التيمم عن العضو الذي يتعذر غسله ومسحه",
            "الموالاة بين الغسل والتيمم في وقت الوضوء",
            "الصلاة بالسكينة والطهارة المقبولة"
        )
    ),
    GameOrderingStage(
        id = 16,
        title = "مبطلات المسح على الخفين",
        description = "رتب الأمور المبطلة لرخصة المسح على الجوربين",
        steps = listOf(
            "نزع الخفين أو أحدهما من القدم",
            "انقضاء مدة المسح الشرعية",
            "وجوب الغسل بالحدث الأكبر",
            "تخلل الشك في ابتداء وقت المسح"
        )
    ),
    GameOrderingStage(
        id = 17,
        title = "الاستنجاء والاستجمار ثم الوضوء",
        description = "رتب تسلسل التطهر من قضاء الحاجة إلى إتمام الوضوء",
        steps = listOf(
            "الاستجمار بثلاثة أحجار طاهرة أو الاستنجاء بالماء",
            "نظافة اليد بالماء والصابون",
            "النية للوضوء والتسمية",
            "غسل أعضاء الوضوء مرتبة",
            "إتمام الوضوء بالذكر المستحب"
        )
    ),
    GameOrderingStage(
        id = 18,
        title = "آداب وأحكام الخلاء والغسل",
        description = "رتب آداب النظافة والستر في الخلاء والغسل",
        steps = listOf(
            "الاستتار عن أعين الناس",
            "عدم الكلام أثناء قضاء الحاجة",
            "عدم إدخال ما فيه ذكر الله الخلاء",
            "استعمال اليد اليسرى في الاستنجاء",
            "غسل اليدين جيدا بالماء النظيف"
        )
    ),
    GameOrderingStage(
        id = 19,
        title = "موجبات الغسل الستة",
        description = "رتب الأحداث الموجبة للغسل الأكبر",
        steps = listOf(
            "خروج المني بدفق ولذة",
            "التقاء الخِتانين (الجماع)",
            "إسلام الكافر",
            "انقطاع دم الحيض للمرأة",
            "انقطاع دم النفاس",
            "الموت (فيجب غسل الميت)"
        )
    ),
    GameOrderingStage(
        id = 20,
        title = "التحدي الختامي: طهارة النبي ﷺ الكاملة",
        description = "رتب الترتيب الشامل الجامع لصفة الطهارة النبوية الكاملة",
        steps = listOf(
            "النية والتسمية بسم الله",
            "غسل الكفين والاستنجاء باليسار",
            "المضمضة والاستنشاق والغسل للوجه واليدين",
            "مسح الرأس والأذنين وغسل الرجلين ثلاثاً",
            "إفاضة الماء وتعميم البدن بالغسل إن كان جنابة",
            "المسح على الجوربين أو التيمم عند العذر الشرعي",
            "الذكر المأثور والثناء على الله بعد تمام الطهارة",
            "القيام إلى الصلاة بقلب خاشع وطاهر"
        )
    )
)

@Composable
fun WuduStepsGame(onBack: () -> Unit) {
    val context = LocalContext.current
    var progressData by remember {
        mutableStateOf(StageProgressStore.loadProgress(context, GAME_NAME, WUDU_STAGES.size))
    }
    var selectedStageIdx by remember { mutableStateOf<Int?>(null) }

    if (selectedStageIdx == null) {
        StageMapScreen(
            gameTitle = "ترتيب الطهارة والوضوء",
            gameSubtitle = "٢٠ مرحلة لتحدي الوضوء والغسل والتيمم",
            totalStages = WUDU_STAGES.size,
            progressData = progressData,
            onSelectStage = { selectedStageIdx = it },
            onBack = onBack
        )
    } else {
        val stageIdx = selectedStageIdx!!
        val currentStage = WUDU_STAGES[stageIdx]

        WuduOrderingPlayScreen(
            stage = currentStage,
            stageNumber = stageIdx + 1,
            totalStages = WUDU_STAGES.size,
            onBackToMap = {
                progressData = StageProgressStore.loadProgress(context, GAME_NAME, WUDU_STAGES.size)
                selectedStageIdx = null
            },
            onCompleteStage = { stars ->
                StageProgressStore.saveProgress(context, GAME_NAME, stageIdx, stars, WUDU_STAGES.size)
                progressData = StageProgressStore.loadProgress(context, GAME_NAME, WUDU_STAGES.size)
            },
            onNextStage = {
                if (stageIdx + 1 < WUDU_STAGES.size) {
                    selectedStageIdx = stageIdx + 1
                } else {
                    selectedStageIdx = null
                }
            }
        )
    }
}

@Composable
private fun WuduOrderingPlayScreen(
    stage: GameOrderingStage,
    stageNumber: Int,
    totalStages: Int,
    onBackToMap: () -> Unit,
    onCompleteStage: (Int) -> Unit,
    onNextStage: () -> Unit
) {
    var currentList by remember(stage) {
        var shuf = stage.steps.shuffled()
        while (shuf == stage.steps && stage.steps.size > 1) {
            shuf = stage.steps.shuffled()
        }
        mutableStateOf(shuf)
    }
    var itemStatuses by remember(stage) { mutableStateOf<List<Boolean?>>(List(stage.steps.size) { null }) }
    var feedbackMessage by remember(stage) { mutableStateOf<String?>(null) }
    var mistakesCount by remember(stage) { mutableIntStateOf(0) }
    var showWinDialog by remember(stage) { mutableStateOf(false) }
    var earnedStars by remember(stage) { mutableIntStateOf(3) }
    val haptics = LocalHapticFeedback.current
    var shakeTrigger by remember(stage) { mutableLongStateOf(0L) }

    LaunchedEffect(feedbackMessage) {
        if (feedbackMessage != null) {
            delay(1800)
            feedbackMessage = null
        }
    }

    fun moveItemUp(index: Int) {
        if (index > 0) {
            val newList = currentList.toMutableList()
            val tmp = newList[index]
            newList[index] = newList[index - 1]
            newList[index - 1] = tmp
            currentList = newList
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            itemStatuses = List(stage.steps.size) { null }
        }
    }

    fun moveItemDown(index: Int) {
        if (index < currentList.size - 1) {
            val newList = currentList.toMutableList()
            val tmp = newList[index]
            newList[index] = newList[index + 1]
            newList[index + 1] = tmp
            currentList = newList
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            itemStatuses = List(stage.steps.size) { null }
        }
    }

    fun checkOrder() {
        val target = stage.steps
        val statuses = currentList.mapIndexed { idx, item -> item == target[idx] }
        itemStatuses = statuses

        val allCorrect = statuses.all { it }
        if (allCorrect) {
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
            feedbackMessage = "الترتيب غير صحيح، تفقد العناصر الملونة باللون الأحمر"
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

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = NavyCard,
                    border = BorderStroke(1.dp, Gold.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "المرحلة ${stageNumber.toArabicDigits()} من ${totalStages.toArabicDigits()}",
                        color = Gold,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                IconButton(onClick = {
                    var shuf = stage.steps.shuffled()
                    while (shuf == stage.steps && stage.steps.size > 1) {
                        shuf = stage.steps.shuffled()
                    }
                    currentList = shuf
                    itemStatuses = List(stage.steps.size) { null }
                    mistakesCount = 0
                }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "إعادة",
                        tint = Gold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Stage Title & Info Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .noorGlassCard(cornerRadius = 18.dp)
                    .padding(14.dp)
            ) {
                Column {
                    Text(
                        text = stage.title,
                        color = Gold,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stage.description,
                        color = TextSecondaryLinen,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Feedback Banner
            AnimatedVisibility(visible = feedbackMessage != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFD32F2F).copy(alpha = 0.85f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Text(
                        text = feedbackMessage ?: "",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            // Draggable/Reorderable List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(currentList) { idx, item ->
                    val status = itemStatuses.getOrNull(idx)
                    val cardBorder = when (status) {
                        true -> BorderStroke(2.dp, Color(0xFF4CAF50))
                        false -> BorderStroke(2.dp, Color(0xFFE53935))
                        null -> BorderStroke(1.dp, Gold.copy(alpha = 0.3f))
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = NavyCard,
                        border = cardBorder,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Step Number Badge
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Gold.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = (idx + 1).toArabicDigits(),
                                    color = Gold,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Text(
                                text = item,
                                color = TextMain,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )

                            Row {
                                IconButton(
                                    onClick = { moveItemUp(idx) },
                                    enabled = idx > 0,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowUpward,
                                        contentDescription = "أعلى",
                                        tint = if (idx > 0) Gold else GoldSoft.copy(alpha = 0.2f)
                                    )
                                }

                                IconButton(
                                    onClick = { moveItemDown(idx) },
                                    enabled = idx < currentList.size - 1,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDownward,
                                        contentDescription = "أسفل",
                                        tint = if (idx < currentList.size - 1) Gold else GoldSoft.copy(alpha = 0.2f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = { checkOrder() },
                modifier = Modifier.wrongAnswerShake(shakeTrigger),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Gold),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Navy
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "تحقق من الترتيب",
                    color = Navy,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
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
                    var shuf = stage.steps.shuffled()
                    while (shuf == stage.steps && stage.steps.size > 1) {
                        shuf = stage.steps.shuffled()
                    }
                    currentList = shuf
                    itemStatuses = List(stage.steps.size) { null }
                    mistakesCount = 0
                },
                onStageMap = onBackToMap
            )
        }
    }
}
