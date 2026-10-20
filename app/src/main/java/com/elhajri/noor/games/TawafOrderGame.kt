import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
package com.elhajri.noor.games

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.EmojiEvents
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

private const val GAME_NAME = "TawafOrderGame"

private val TAWAF_STAGES = listOf(
    GameOrderingStage(
        id = 1,
        title = "مراحل مناسك العمرة",
        description = "رتب خطوات العمرة المباركة بالترتيب الصحيح من الميقات إلى التحلل",
        steps = listOf(
            "الإحرام من الميقات والتلبية",
            "النية ودخول المسجد الحرام",
            "استلام الحجر الأسود والبدء بالتقبيل أو الإشارة",
            "الطواف ٧ أشواط حول الكعبة المشرفة",
            "صلاة ركعتين خلف مقام إبراهيم",
            "الشرب من ماء زمزم المبارك",
            "السعي بين الصفا والمروة ٧ أشواط"
        )
    ),
    GameOrderingStage(
        id = 2,
        title = "آداب وأحكام الطواف",
        description = "رتب الأحكام والآداب المتعلقة بطواف الكعبة المشرفة",
        steps = listOf(
            "الطهارة والنية للطواف",
            "محاذاة الحجر الأسود والتكبير",
            "جعل الكعبة المشرفة عن اليسار",
            "الرمل في الأشواط الثلاثة الأولى للرجال",
            "استلام الركن اليماني دون تقبيل",
            "الدعاء بين الركن اليماني والحجر الأسود",
            "إتمام الشوط السابع بركعتي الطواف"
        )
    ),
    GameOrderingStage(
        id = 3,
        title = "مراحل السعي بين الصفا والمروة",
        description = "رتب خطوات السعي بين المشعرين الشريفين",
        steps = listOf(
            "الخروج إلى الصفا بعد الطواف وزمزم",
            "الصعود على الصفا واستقبال القبلة",
            "الدعاء والذكر عند الصفا",
            "المشي نحو المروة والهرولة بين العلمين الأخضرين",
            "الوصول إلى المروة والدعاء عندها",
            "إكمال الشوط السابع بالانتهاء عند المروة",
            "الحلق أو التقصير بعد إتمام السعي"
        )
    ),
    GameOrderingStage(
        id = 4,
        title = "ترتيب أعمال يوم النحر (١٠ ذو الحجة)",
        description = "رتب أعمال يوم العيد الأكبر للحاج بالترتيب الشرعي",
        steps = listOf(
            "التوجه من مزدلفة إلى منى قبل طلوع الشمس",
            "رمي جمرة العقبة الكبرى بسبع حصيات",
            "ذبح الهدي أو النحر للمتمتع والمقرن",
            "الحلق أو التقصير والتحلل الأصغر",
            "التوجه إلى مكة لطواف الإفاضة",
            "السعي بين الصفا والمروة للحج"
        )
    ),
    GameOrderingStage(
        id = 5,
        title = "ترتيب أعمال يوم عرفة (٩ ذو الحجة)",
        description = "رتب أحداث ركن الحج الأكبر في مشعر عرفات المبارك",
        steps = listOf(
            "التوجه من منى إلى نمرة بعد طلوع الشمس",
            "الاستماع لخطبة عرفة وصلاة الظهر والعصر جمعاً وقصراً",
            "الوقوف بعرفة والاستكثار من الدعاء والذكر",
            "غروب الشمس والتوجه إلى مزدلفة بالسكينة",
            "صلاة المغرب والعشاء جمعاً وقصراً بمزدلفة",
            "المبيت بمزدلفة وجمع الحصى للرجم"
        )
    ),
    GameOrderingStage(
        id = 6,
        title = "أحكام أدوات وطهارة الحاج",
        description = "رتب خطوات الطهارة والتحضير للإحرام",
        steps = listOf(
            "الاستنجاء وإزالة النجاسة",
            "غسل اليدين والتمضمض والاستنشاق",
            "غسل الوجه واليدين إلى المرفقين",
            "مسح الرأس والأذنين وغسل الرجلين",
            "لبس ثياب الإحرام الطاهرة والتطيب في البدن"
        )
    ),
    GameOrderingStage(
        id = 7,
        title = "أذكار وأدعية أشواط الطواف",
        description = "رتب الأذكار المسنونة في طواف الكعبة المشرفة",
        steps = listOf(
            "التكبير عند الحجر الأسود في بدء كل شوط",
            "الذكر والدعاء والتضرع أثناء الطواف",
            "قول ربنا آتنا في الدنيا حسنة وفي الآخرة حسنة وقنا عذاب النار بين الركنين",
            "صلاة ركعتين خلف المقام بقراءة الكافرون والإخلاص",
            "الدعاء عند الشرب من زمزم"
        )
    ),
    GameOrderingStage(
        id = 8,
        title = "ترتيب مناسك الحج المفرد",
        description = "رتب مناسك حج الإفراد من الميقات حتى طواف الوداع",
        steps = listOf(
            "الإحرام بالحج فقط من الميقات",
            "طواف القدوم عند الوصول لمكة",
            "السعي للحج بعد طواف القدوم",
            "التوجه إلى منى في يوم التروية",
            "الوقوف بعرفة والمبيت بمزدلفة",
            "رمي الجمار والنحر والحلق وطواف الإفاضة"
        )
    ),
    GameOrderingStage(
        id = 9,
        title = "ترتيب مناسك حج التمتع",
        description = "رتب مراحل الجمع بين العمرة والحج في التمتع",
        steps = listOf(
            "الإحرام بالعمرة في أشهر الحج",
            "أداء طواف وسعي العمرة والحلق والتحلل",
            "الإحرام بالحج يوم التروية من مكة",
            "أداء مناسك الحج في عرفة ومزدلفة ومنى",
            "ذبح الهدي الواجب على المتمتع",
            "طواف الإفاضة والسعي وطواف الوداع"
        )
    ),
    GameOrderingStage(
        id = 10,
        title = "ترتيب محطات رامي الجمار في أيام التشريق",
        description = "رتب رمي الجمرات الثلاث بعد زوال الشمس",
        steps = listOf(
            "الزوال ودخول وقت الرمي بعد الظهيرة",
            "رمي الجمرة الصغرى بسبع حصيات ثم الدعاء",
            "رمي الجمرة الوسطى بسبع حصيات ثم الدعاء",
            "رمي الجمرة الكبرى العقبة بسبع حصيات",
            "التعجل في اليوم الثاني عشر أو التأخر لليوم الثالث عشر"
        )
    ),
    GameOrderingStage(
        id = 11,
        title = "أركان الحج الأربعة بالترتيب",
        description = "رتب أركان الحج التي لا يصح الحج إلا بها جميعاً",
        steps = listOf(
            "الإحرام (نية الدخول في النسك)",
            "الوقوف بعرفة",
            "طواف الإفاضة",
            "السعي بين الصفا والمروة"
        )
    ),
    GameOrderingStage(
        id = 12,
        title = "واجبات الحج بالترتيب الشرعي",
        description = "رتب واجبات الحج التي يجبر تركها بدم",
        steps = listOf(
            "الإحرام من الميقات المعتبر",
            "الوقوف بعرفة إلى غروب الشمس",
            "المبيت بمزدلفة ليلة النحر",
            "المبيت بمنى ليالي أيام التشريق",
            "رمي الجمار مرتبة",
            "الحلق أو التقصير وطواف الوداع"
        )
    ),
    GameOrderingStage(
        id = 13,
        title = "خطوات الإحرام وتلبيته",
        description = "رتب سنن وخطوات عقد النية والإحرام بالنسك",
        steps = listOf(
            "قص الأظافر وتقليم الشارب والغسل",
            "التجرد من المخيط للرجال ولبس الإزار والرداء",
            "صلاة ركعتين سنتين أو الفريضة",
            "نية النسك (لبيك اللهم عمرة / حجة)",
            "رفع الصوت بالتلبية الشرعية"
        )
    ),
    GameOrderingStage(
        id = 14,
        title = "سنن وآداب دخول المسجد الحرام",
        description = "رتب آداب المستحب اتباعها عند دخول الحرم المكي",
        steps = listOf(
            "تقديم القدم اليمنى عند الدخول",
            "قول دعاء دخول المسجد",
            "خشوع القلب ونظر الكعبة المشرفة",
            "التوجه مباشرة لبدء الطواف دون تحية مسجد",
            "التكبير والتهليل عند رؤية البيت"
        )
    ),
    GameOrderingStage(
        id = 15,
        title = "ترتيب طواف الوداع وآدابه",
        description = "رتب أفعال طواف الوداع لآخر عهد بالبيت الحرام",
        steps = listOf(
            "إنهاء جميع أعمال الحج والعمرة والتبضع",
            "النية لطواف الوداع للغريب عن مكة",
            "الطواف سبعة أشواط دون سعي",
            "صلاة ركعتين خلف المقام",
            "الخروج من الحرم مع الدعاء بالقبول"
        )
    ),
    GameOrderingStage(
        id = 16,
        title = "مراحل المبيت بمزدلفة",
        description = "رتب خطوات ليلة المزدلفة المباركة بعد الإفاضة",
        steps = listOf(
            "الوصول من عرفة بعد الغروب بالسكينة",
            "صلاة المغرب والعشاء جمع تأخير وقصراً",
            "المبيت والاستراحة حتى الفجر",
            "صلاة الفجر في أول وقتها بالمشعر الحرام",
            "جمع حصى الجمار والتوجّه إلى منى"
        )
    ),
    GameOrderingStage(
        id = 17,
        title = "أحكام التحلل من الإحرام",
        description = "رتب مراتب التحلل الأصغر والأكبر للحاج",
        steps = listOf(
            "رمي جمرة العقبة يوم النحر",
            "الحلق أو التقصير للشعر",
            "التحلل الأصغر (يباح كل شيء عدا النساء)",
            "أداء طواف الإفاضة والسعي",
            "التحلل الأكبر الكامل"
        )
    ),
    GameOrderingStage(
        id = 18,
        title = "سنن زيارة المسجد النبوي الشريف",
        description = "رتب الآداب والزيارات المستحبة بالمدينة المنورة",
        steps = listOf(
            "السفر بنية زيارة المسجد النبوي والصلاة فيه",
            "دخول المسجد والصلاة في الروضة الشريفة",
            "السلام على الرسول ﷺ عند القبر الشريف",
            "السلام على أبي بكر الصديق ثم عمر بن الخطاب",
            "زيارة مسجد قباء والبقيع وشهداء أحد"
        )
    ),
    GameOrderingStage(
        id = 19,
        title = "ترتيب أفعال يوم التروية (٨ ذو الحجة)",
        description = "رتب انطلاق الحجاج إلى مشعر منى في بداية الحج",
        steps = listOf(
            "الإحرام بالحج للمتمتع من مكانه",
            "التوجه إلى منى قبل الظهيرة",
            "صلاة الصلوات الخمس قصراً بلا جمع",
            "المبيت بمنى ليلة التاسع",
            "التوجّه صباحاً إلى عرفات"
        )
    ),
    GameOrderingStage(
        id = 20,
        title = "الترتيب الشامل الخالد لمناسك الحج",
        description = "التحدي الذهبي الختامي: رتب تسلسل مناسك الحج الكبرى من البداية للختام",
        steps = listOf(
            "الإحرام يوم التروية والمبيت بمنى",
            "الوقوف بعرفة والافاضة لمزدلفة",
            "المبيت بمزدلفة ورمي جمرة العقبة بالنحر",
            "الحلق والتحلل وطواف الإفاضة والسعي",
            "المبيت بمنى ورمي الجمار أيام التشريق",
            "طواف الوداع وختام المناسك المباركة"
        )
    )
)

@Composable
fun TawafOrderGame(onBack: () -> Unit) {
    val context = LocalContext.current
    var progressData by remember {
        mutableStateOf(StageProgressStore.loadProgress(context, GAME_NAME, TAWAF_STAGES.size))
    }
    var selectedStageIdx by remember { mutableStateOf<Int?>(null) }

    if (selectedStageIdx == null) {
        StageMapScreen(
            gameTitle = "ترتيب الطواف والمناسك",
            gameSubtitle = "٢٠ مرحلة لتحدي ترتيب المناسك والآداب",
            totalStages = TAWAF_STAGES.size,
            progressData = progressData,
            onSelectStage = { selectedStageIdx = it },
            onBack = onBack
        )
    } else {
        val stageIdx = selectedStageIdx!!
        val currentStage = TAWAF_STAGES[stageIdx]

        TawafOrderingPlayScreen(
            stage = currentStage,
            stageNumber = stageIdx + 1,
            totalStages = TAWAF_STAGES.size,
            onBackToMap = {
                progressData = StageProgressStore.loadProgress(context, GAME_NAME, TAWAF_STAGES.size)
                selectedStageIdx = null
            },
            onCompleteStage = { stars ->
                StageProgressStore.saveProgress(context, GAME_NAME, stageIdx, stars, TAWAF_STAGES.size)
                progressData = StageProgressStore.loadProgress(context, GAME_NAME, TAWAF_STAGES.size)
            },
            onNextStage = {
                if (stageIdx + 1 < TAWAF_STAGES.size) {
                    selectedStageIdx = stageIdx + 1
                } else {
                    selectedStageIdx = null
                }
            }
        )
    }
}

@Composable
private fun TawafOrderingPlayScreen(
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
