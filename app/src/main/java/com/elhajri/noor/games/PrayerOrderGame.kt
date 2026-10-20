package com.elhajri.noor.games
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

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

private const val GAME_NAME = "PrayerOrderGame"

private val PRAYER_STAGES = listOf(
    GameOrderingStage(
        id = 1,
        title = "أركان صلاة الفجر (ركعتان)",
        description = "رتب أركان وصلاح خطوات صلاة الفجر بالترتيب الشرعي الصحيح",
        steps = listOf(
            "تكبيرة الإحرام والقيام",
            "قراءة الفاتحة والركوع والرفع منه",
            "السجود الأول والجلسة بين السجدتين والسجود الثاني",
            "القيام للركعة الثانية وقراءة الفاتحة",
            "الركوع والسجود والجلوس للتشهد الأخير",
            "التسليمتان عن اليمين والشمال"
        )
    ),
    GameOrderingStage(
        id = 2,
        title = "ترتيب أركان الصلاة الرئيسية",
        description = "رتب الأركان العظام للصلاة من البداية إلى الختام",
        steps = listOf(
            "القيام مع القدرة وتكبيرة الإحرام",
            "قراءة سورة الفاتحة في كل ركعة",
            "الركوع والاعتدال منه بطمأنينة",
            "السجود على الأعضاء السبعة والجلسة بين السجدتين",
            "التشهد الأخير والصلاة على النبي والتسليم"
        )
    ),
    GameOrderingStage(
        id = 3,
        title = "سنن الصلاة القولية والفعلية",
        description = "رتب السنن المسنونة المستحبة في الصلاة حسب مواضعها",
        steps = listOf(
            "دعاء الاستفتاح بعد تكبيرة الإحرام",
            "رفع اليدين عند التكبير والركوع والرفع",
            "الاستعاذة والبسملة وتأمين الفاتحة",
            "قراءة سورة بعد الفاتحة في الركعتين الأوليين",
            "التسبحات في الركوع والسجود",
            "الصلاة الإبراهيمية بعد التشهد الأخير"
        )
    ),
    GameOrderingStage(
        id = 4,
        title = "صفة الركعة الأولى من الصلاة",
        description = "رتب أعمال الركعة الأولى بالتفصيل والترتيب الدقيق",
        steps = listOf(
            "النية بالقلب وتكبيرة الإحرام",
            "دعاء الاستفتاح وقراءة الفاتحة وسورة",
            "التكبير للركوع وتعظيم الرب سبحانه",
            "الرفع من الركوع وقول سمع الله لمن حمده",
            "التكبير للسجود والتسبيح في السجود الأول",
            "الرفع من السجود والجلوس والدعاء بين السجدتين",
            "السجود الثاني ثم القيام للركعة الثانية"
        )
    ),
    GameOrderingStage(
        id = 5,
        title = "صفة الصلاة الثنائية (الفجر، الجمعة، العيد)",
        description = "رتب أركان وصفة الصلاة ذات الركعتين",
        steps = listOf(
            "أداء الركعة الأولى كاملة بأركانها وسننها",
            "القيام للركعة الثانية بقراءة الفاتحة وسورة",
            "الركوع والرفع والسجودان في الركعة الثانية",
            "الجلوس للتشهد الأخير والصلاة الإبراهيمية",
            "التسليم عن اليمين ثم عن اليسار"
        )
    ),
    GameOrderingStage(
        id = 6,
        title = "صفة الصلاة الثلاثية (صلاة المغرب)",
        description = "رتب أركان وركعات صلاة المغرب الثلاث",
        steps = listOf(
            "أداء الركعتين الأوليين بالفاتحة وسورة جهراً",
            "الجلوس للتشهد الأول بعد الركعة الثانية",
            "القيام للركعة الثالثة بقراءة الفاتحة سراً",
            "الركوع والسجود في الركعة الثالثة",
            "الجلوس للتشهد الأخير والتسليم"
        )
    ),
    GameOrderingStage(
        id = 7,
        title = "صفة الصلاة الرباعية (الظهر، العصر، العشاء)",
        description = "رتب تسلسل الركعات الأربع للصلاة الرباعية",
        steps = listOf(
            "الركعتان الأولى والثانية بالفاتحة وسورة",
            "الجلوس للتشهد الأول",
            "القيام للركعة الثالثة بالفاتحة فقط",
            "أداء الركعة الرابعة بالفاتحة فقط والركوع والسجود",
            "الجلوس للتشهد الأخير والصلاة الإبراهيمية والتسليم"
        )
    ),
    GameOrderingStage(
        id = 8,
        title = "ترتيب شروط صحة الصلاة قبل الدخول فيها",
        description = "رتب شروط الصلاة الواجب توفرها قبل تكبيرة الإحرام",
        steps = listOf(
            "طهارة الحدث الأصغر والأكبر (الوضوء)",
            "طهارة الثوب والبدن والمكان من النجاسة",
            "ستر العورة باللباس الساتر الشرعي",
            "دخول وقت الصلاة المفروضة",
            "استقبال القبلة الشريفة والنية بالقلب"
        )
    ),
    GameOrderingStage(
        id = 9,
        title = "مبطلات ونواقض الصلاة",
        description = "رتب الأمور المبطلة للصلاة حسب شدتها وتكرارها",
        steps = listOf(
            "الأكل أو الشرب عمداً أثناء الصلاة",
            "الكلام العمد بغير أذكار الصلاة",
            "الضحك أو القهقهة بصوت مرتفع",
            "انتقاض الطهارة بحدث أصغر أو أكبر",
            "انكشاف العورة المغلظة عمداً",
            "الانحراف الكلي عن القبلة الشريفة"
        )
    ),
    GameOrderingStage(
        id = 10,
        title = "ترتيب الصلوات الخمس المفروضة وعدد ركعاتها",
        description = "رتب الصلوات الخمس حسب أوقاتها الشرعية من الفجر إلى العشاء",
        steps = listOf(
            "صلاة الفجر (ركعتان جهريتان)",
            "صلاة الظهر (أربع ركعات سرية)",
            "صلاة العصر (أربع ركعات سرية)",
            "صلاة المغرب (ثلاث ركعات: ثنتان جهر وواحدة سر)",
            "صلاة العشاء (أربع ركعات: ثنتان جهر وثنتان سر)"
        )
    ),
    GameOrderingStage(
        id = 11,
        title = "ترتيب السنن الراتبة اليومية (١٢ ركعة)",
        description = "رتب السنن الراتبة المؤكدة الموزعة على الصلوات المفروضة",
        steps = listOf(
            "ركعتان قبل صلاة الفجر",
            "أربع ركعات قبل صلاة الظهر (بتسليمتين)",
            "ركعتان بعد صلاة الظهر",
            "ركعتان بعد صلاة المغرب",
            "ركعتان بعد صلاة العشاء"
        )
    ),
    GameOrderingStage(
        id = 12,
        title = "أفعال سجود السهو",
        description = "رتب خطوات التعامل مع السهو أو الشك في الصلاة",
        steps = listOf(
            "الشعور بالزيادة أو النقص أو الشك",
            "إتمام الصلاة والبناء على اليقين (الأقل)",
            "السجود سجدتين قبل التسليم أو بعده",
            "القول: سبحان من لا ينام ولا يسهو",
            "التسليم بعد سجود السهو"
        )
    ),
    GameOrderingStage(
        id = 13,
        title = "أحكام وسنن صلاة الجمعة",
        description = "رتب آداب وأعمال يوم الجمعة وصلاؤها المباركة",
        steps = listOf(
            "الغسل والتطيب والتبكير للمسجد الجامع",
            "صلاة تحية المسجد عند الدخول",
            "الاستماع لخطبة الجمعة بالإنصات التام",
            "أداء ركعتين جهريتين خلف الإمام",
            "صلاة السنة البعدية في البيت أو المسجد"
        )
    ),
    GameOrderingStage(
        id = 14,
        title = "كيفية صلاة الجنازة (٤ تكبيرات)",
        description = "رتب التكبارت الأربع والدعاء في صلاة الجنازة",
        steps = listOf(
            "التكبيرة الأولى وقراءة سورة الفاتحة",
            "التكبيرة الثانية والصلاة الإبراهيمية",
            "التكبيرة الثالثة والدعاء للميت بالرحمة",
            "التكبيرة الرابعة والدعاء لعموم المسلمين",
            "تسليمة واحدة عن اليمين"
        )
    ),
    GameOrderingStage(
        id = 15,
        title = "صفة صلاة الوتر وصلاة الليل",
        description = "رتب خطوات قيام الليل وختامه بركعة الوتر المباركة",
        steps = listOf(
            "صلاة ركعتين ركعتين في قيام الليل",
            "الشروع في ركعة الوتر الفردية",
            "قراءة الفاتحة وسورة الإخلاص",
            "دعاء القنوت في الركوع أو بعده",
            "الجلوس للتشهد الأخير والتسليم"
        )
    ),
    GameOrderingStage(
        id = 16,
        title = "أحكام صلاة المسافر (الجمع والقصر)",
        description = "رتب رخص المسافر في القصر والجمع بين الصلوات",
        steps = listOf(
            "الخروج للسفر وتجاوز بنيان البلدة",
            "قصر الصلاة الرباعية إلى ركعتين",
            "جمع الظهر مع العصر تقديم أو تأخير",
            "جمع المغرب مع العشاء تقديم أو تأخير",
            "ترك السنن الراتبة عدا الفجر والوتر"
        )
    ),
    GameOrderingStage(
        id = 17,
        title = "أحكام صلاة الجماعة ومتابعة الإمام",
        description = "رتب واجبات وآداب صلاة الجماعة خلف الإمام",
        steps = listOf(
            "نداء الأذان والإقامة للصلاة",
            "اصطفاف المأمومين واستواء الصفوف",
            "متابعة الإمام فوراً دون مسابقة أو تخلف",
            "إتمام الصلاة والتسليم مع الإمام"
        )
    ),
    GameOrderingStage(
        id = 18,
        title = "أذكار ما بعد الصلاة المفروضة",
        description = "رتب تسلسل الأذكار المستحبة عقب التسليم من الصلاة",
        steps = listOf(
            "الاستغفار ثلاثاً وقول: اللهم أنت السلام",
            "تسبيح الله ٣٣ مرة (سبحان الله)",
            "تحميد الله ٣٣ مرة (الحمد لله)",
            "تكبير الله ٣٣ مرة (الله أكبر)",
            "تمام المائة: لا إله إلا الله وحده لا شريك له",
            "قراءة آية الكرسي والمعوذتين"
        )
    ),
    GameOrderingStage(
        id = 19,
        title = "واجبات الصلاة الثمانية",
        description = "رتب الواجبات الشرعية في الصلاة التي تجبر بسجود السهو",
        steps = listOf(
            "جميع التكبيرات غير تكبيرة الإحرام",
            "قول: سمع الله لمن حمده للإمام والمنفرد",
            "قول: ربنا ولك الحمد للجميع",
            "قول: سبحان ربي العظيم في الركوع",
            "قول: سبحان ربي الأعلى في السجود",
            "قول: رب اغفر لي بين السجدتين",
            "التشهد الأول والجلوس له"
        )
    ),
    GameOrderingStage(
        id = 20,
        title = "التحدي الشامل: من الأذان إلى الانتهاء",
        description = "رتب رحلة الصلاة الكاملة من البداية إلى الأذكار الأخيرة",
        steps = listOf(
            "إجابة الأذان والدعاء بعده",
            "إسباغ الوضوء وستر العورة واستقبال القبلة",
            "إقامة الصلاة وتكبيرة الإحرام",
            "أداء الأركان والواجبات بطمأنينة ودعاء",
            "التشهد الأخير والتسليمتان",
            "الأذكار المسنونة بعد الانتهاء من الصلاة"
        )
    )
)

@Composable
fun PrayerOrderGame(onBack: () -> Unit) {
    val context = LocalContext.current
    var progressData by remember {
        mutableStateOf(StageProgressStore.loadProgress(context, GAME_NAME, PRAYER_STAGES.size))
    }
    var selectedStageIdx by remember { mutableStateOf<Int?>(null) }

    if (selectedStageIdx == null) {
        StageMapScreen(
            gameTitle = "ترتيب أركان وصفة الصلاة",
            gameSubtitle = "٢٠ مرحلة لترتيب الصلوات الخمس والأركان والسنن والنواقض",
            totalStages = PRAYER_STAGES.size,
            progressData = progressData,
            onSelectStage = { selectedStageIdx = it },
            onBack = onBack
        )
    } else {
        val stageIdx = selectedStageIdx!!
        val currentStage = PRAYER_STAGES[stageIdx]

        PrayerOrderingPlayScreen(
            stage = currentStage,
            stageNumber = stageIdx + 1,
            totalStages = PRAYER_STAGES.size,
            onBackToMap = {
                progressData = StageProgressStore.loadProgress(context, GAME_NAME, PRAYER_STAGES.size)
                selectedStageIdx = null
            },
            onCompleteStage = { stars ->
                StageProgressStore.saveProgress(context, GAME_NAME, stageIdx, stars, PRAYER_STAGES.size)
                progressData = StageProgressStore.loadProgress(context, GAME_NAME, PRAYER_STAGES.size)
            },
            onNextStage = {
                if (stageIdx + 1 < PRAYER_STAGES.size) {
                    selectedStageIdx = stageIdx + 1
                } else {
                    selectedStageIdx = null
                }
            }
        )
    }
}

@Composable
private fun PrayerOrderingPlayScreen(
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
            feedbackMessage = "الترتيب غير صحيح، تفقد الخطوات الملونة باللون الأحمر"
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

            // Draggable / Reorderable List
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
                modifier = Modifier.wrongAnswerShake(shakeTrigger)
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
