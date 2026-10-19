package com.elhajri.noor.games

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

private const val GAME_NAME = "IsraJourneyGame"

private val ISRA_STAGES = listOf(
    GameOrderingStage(
        id = 1,
        title = "بداية رحلة الإسراء من مكة المكرمة",
        description = "رتب أحداث البداية المباركة لرحلة الإسراء والمعراج من البيت الحرام",
        steps = listOf(
            "المبيت بالحجر عند الكعبة المشرفة",
            "شق الصدر الشريف وغسله بماء زمزم",
            "ملء القلب بالحكمة والإيمان",
            "إحضار البراق وركوبه للانطلاق"
        )
    ),
    GameOrderingStage(
        id = 2,
        title = "محطات الطريق إلى بيت المقدس",
        description = "رتب محطات التوقف والصلاة المباركة أثناء سيرة البراق ليلاً",
        steps = listOf(
            "الانطلاق بالبراق من المسجد الحرام",
            "الصلاة بيثرب (المدينة المنورة)",
            "الصلاة بمدين (شجرة نبي الله موسى)",
            "الصلاة بطور سيناء (حيث كلم الله موسى)",
            "الصلاة ببيت لحم (مولد عيسى بن مريم)"
        )
    ),
    GameOrderingStage(
        id = 3,
        title = "الوصول إلى بيت المقدس",
        description = "رتب الأحداث العظيمة فور الوصول للمسجد الأقصى المبارك",
        steps = listOf(
            "ربط البراق بالحلقة عند باب المسجد الأقصى",
            "دخول المسجد الأقصى المبارك",
            "اجتماع الأنبياء والمرسلين عليهم السلام",
            "إمامة النبي ﷺ بالأنبياء في الصلاة"
        )
    ),
    GameOrderingStage(
        id = 4,
        title = "اختيار الفطرة في بيت المقدس",
        description = "رتب موقف الاختيار بين الإناءين وتأييد الفطرة",
        steps = listOf(
            "تقديم إناء الخمر وإناء اللبن للنبي ﷺ",
            "اختيار النبي ﷺ لإناء اللبن المبارك",
            "قول جبريل: هديت للفطرة وهديت أمتك",
            "بدء العروج من بيت المقدس إلى السماوات"
        )
    ),
    GameOrderingStage(
        id = 5,
        title = "السماء الأولى (سماء الدنيا)",
        description = "رتب أحداث الاستفتاح واللقاء في السماء الأولى",
        steps = listOf(
            "استفتاح جبريل عليه السلام لفتح الباب",
            "الترحيب بالنبي ﷺ وفتح باب السماء",
            "لقاء أب البشر آدم عليه السلام والسلّام عليه",
            "عرض أرواح السعداء والأشقياء على آدم"
        )
    ),
    GameOrderingStage(
        id = 6,
        title = "السماء الثانية",
        description = "رتب أحداث العروج واللقاء في السماء الثانية",
        steps = listOf(
            "الارتقاء إلى السماء الثانية واستفتاح الباب",
            "لقاء ابني الخالة عيسى بن مريم ويحيى بن زكريا",
            "ترحيب النبيين بعرس رسول الله ﷺ",
            "الدعاء والثناء بالخير والبركة"
        )
    ),
    GameOrderingStage(
        id = 7,
        title = "السماء الثالثة",
        description = "رتب محطة السماء الثالثة ورؤية الجمال والجمال الإلهي",
        steps = listOf(
            "الوصول إلى السماء الثالثة واستفتاح الباب",
            "لقاء نبي الله يوسف عليه السلام",
            "رؤية حسن يوسف الذي أُعطي شطر الحسن",
            "الترحيب والدعاء بالنبي الصالح والأخ الصالح"
        )
    ),
    GameOrderingStage(
        id = 8,
        title = "السماء الرابعة",
        description = "رتب أحداث العروج واللقاء الشريف في السماء الرابعة",
        steps = listOf(
            "العروج إلى السماء الرابعة واستفتاح بابها",
            "لقاء نبي الله إدريس عليه السلام",
            "استحضار قوله تعالى: ورفعناه مكاناً علياً",
            "الترحيب بالنبي المبارك والدعاء له"
        )
    ),
    GameOrderingStage(
        id = 9,
        title = "السماء الخامسة",
        description = "رتب محطة السماء الخامسة ولقاء المحبوب في قومه",
        steps = listOf(
            "الوصول إلى السماء الخامسة واستفتاح الباب",
            "لقاء نبي الله هارون عليه السلام",
            "ترحيب هارون بالنبي ﷺ بين قومه",
            "الدعاء للنبي والأمة بالخير والرحمة"
        )
    ),
    GameOrderingStage(
        id = 10,
        title = "السماء السادسة",
        description = "رتب أحداث اللقاء بكليم الله موسى عليه السلام",
        steps = listOf(
            "العروج إلى السماء السادسة واستفتاح الباب",
            "لقاء كليم الله موسى عليه السلام والسلّام عليه",
            "ترحيب موسى بالنبي الكريم ﷺ",
            "بكاء موسى لكثرة من يدخل الجنة من أمة محمد"
        )
    ),
    GameOrderingStage(
        id = 11,
        title = "السماء السابعة",
        description = "رتب أحداث اللقاء بأبي الأنبياء خليل الله إبراهيم",
        steps = listOf(
            "الارتقاء إلى السماء السابعة المباركة",
            "لقاء خليل الله إبراهيم عليه السلام",
            "رؤية إبراهيم مسنداً ظهره إلى البيت المعمور",
            "إقراء إبراهيم السلام لأمة محمد والأمر بغراس الجنة"
        )
    ),
    GameOrderingStage(
        id = 12,
        title = "مشاهدة البيت المعمور",
        description = "رتب مشاهد العظمة في البيت المعمور فوق السماء السابعة",
        steps = listOf(
            "مشاهدة البيت المعمور بحذاء الكعبة في السماء",
            "دخول سبعون ألف ملك كل يوم للعبادة",
            "عدم عودة الملائكة إليه إلى يوم القيامة",
            "معاينة كثرة وجلال ملائكة الرحمن"
        )
    ),
    GameOrderingStage(
        id = 13,
        title = "الوصول إلى سدرة المنتهى",
        description = "رتب وصف مشاهد سدرة المنتهى الشريفة",
        steps = listOf(
            "الوصول إلى سدرة المنتهى العظيمة",
            "رؤية أوراقها كآذان الفيلة وثمارها كالقلال",
            "غشية ألوان وأشجار ونور لا يستطيع أحد وصفه",
            "وقوف جبريل عندها وتقدم النبي ﷺ بمفرده"
        )
    ),
    GameOrderingStage(
        id = 14,
        title = "عرض الأنهار الأربعة",
        description = "رتب مشاهد الأنهار التي رآها النبي ﷺ عند سدرة المنتهى",
        steps = listOf(
            "رؤية أربعة أنهار عند أصل سدرة المنتهى",
            "نهران باطنان يجرين في الجنة النعيم",
            "نهران ظاهران يجرين في الأرض (النيل والفرات)",
            "رؤية نهر الكوثر المعطى لرسول الله ﷺ"
        )
    ),
    GameOrderingStage(
        id = 15,
        title = "المقام الأعلى وفرض الصلاة",
        description = "رتب لحظات القرب والتكليف الإلهي العظيم بالصلوات",
        steps = listOf(
            "تجاوز سدرة المنتهى وسماع صريف الأقلام",
            "مناجاة الرب جل جلاله دون حجاب",
            "فرض خمسين صلاة في اليوم والليلة على الأمة"
        )
    ),
    GameOrderingStage(
        id = 16,
        title = "مراجعة نبي الله موسى للتخفيف",
        description = "رتب رحلة التردد والتخفيف في عدد الصلوات",
        steps = listOf(
            "النزول إلى موسى عليه السلام في السماء السادسة",
            "نصيحة موسى: أمتك لا تطيق ذلك فارجع لربك",
            "التردد بين الرب عز وجل وموسى وسؤال التخفيف",
            "استقرار الفريضة على ٥ صلوات بأجر ٥٠ صلاة"
        )
    ),
    GameOrderingStage(
        id = 17,
        title = "رؤية مشاهد الجنة والنعيم",
        description = "رتب مشاهد الكرامة والنعيم المقيم التي شاهدها النبي",
        steps = listOf(
            "معاينة الجنة وحياضها وقصورها",
            "رؤية تربة المسك واللؤلؤ والمحار",
            "سماع دف نعلين لبلال بن رباح في الجنة"
        )
    ),
    GameOrderingStage(
        id = 18,
        title = "رؤية مشاهد النار والوعيد",
        description = "رتب مشاهد العظة والعبرة التي رآها النبي ﷺ في النار",
        steps = listOf(
            "معاينة النار وأهوالها وعذابها",
            "رؤية مالك خازن النار لا يبتسم أبداً",
            "رؤية عذاب أكلة أموال اليتامى والمغتابين"
        )
    ),
    GameOrderingStage(
        id = 19,
        title = "العودة إلى مكة والتحديث بالخبر",
        description = "رتب أحداث الصباح وتحديث أهل مكة بالمعجزة",
        steps = listOf(
            "الهبوط بالبراق إلى المسجد الحرام قبل الفجر",
            "إخبار قريش برحلة الإسراء والمعراج العظيمة",
            "تكذيب كفار قريش ومطالبتهم بوصف بيت المقدس",
            "تجلي بيت المقدس للنبي ﷺ ووصفه باباً باباً"
        )
    ),
    GameOrderingStage(
        id = 20,
        title = "موقف الصديق وتصديق القافلة",
        description = "رتب التحدي الأخير وتأييد صدق النبي ﷺ بالدلائل والبراهين",
        steps = listOf(
            "إخبار أبي بكر بالخبر وقوله: إن كان قال فقد صدق",
            "تسمية النبي لأبي بكر بلقب (الصديق)",
            "الإخبار بقافلة قريش وتحديد موعد وصولها",
            "وصول القافلة لمكة كما وصفها النبي ﷺ تماماً"
        )
    )
)

@Composable
fun IsraJourneyGame(onBack: () -> Unit) {
    val context = LocalContext.current
    var progressData by remember {
        mutableStateOf(StageProgressStore.loadProgress(context, GAME_NAME, ISRA_STAGES.size))
    }
    var selectedStageIdx by remember { mutableStateOf<Int?>(null) }

    if (selectedStageIdx == null) {
        StageMapScreen(
            gameTitle = "رحلة الإسراء والمعراج",
            gameSubtitle = "٢٠ مرحلة لترتيب محطات ورحلة الإسراء والمعراج مفصلة",
            totalStages = ISRA_STAGES.size,
            progressData = progressData,
            onSelectStage = { selectedStageIdx = it },
            onBack = onBack
        )
    } else {
        val stageIdx = selectedStageIdx!!
        val currentStage = ISRA_STAGES[stageIdx]

        IsraOrderingPlayScreen(
            stage = currentStage,
            stageNumber = stageIdx + 1,
            totalStages = ISRA_STAGES.size,
            onBackToMap = {
                progressData = StageProgressStore.loadProgress(context, GAME_NAME, ISRA_STAGES.size)
                selectedStageIdx = null
            },
            onCompleteStage = { stars ->
                StageProgressStore.saveProgress(context, GAME_NAME, stageIdx, stars, ISRA_STAGES.size)
                progressData = StageProgressStore.loadProgress(context, GAME_NAME, ISRA_STAGES.size)
            },
            onNextStage = {
                if (stageIdx + 1 < ISRA_STAGES.size) {
                    selectedStageIdx = stageIdx + 1
                } else {
                    selectedStageIdx = null
                }
            }
        )
    }
}

@Composable
private fun IsraOrderingPlayScreen(
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
            itemStatuses = List(stage.steps.size) { null }
        }
    }

    fun checkOrder() {
        val target = stage.steps
        val statuses = currentList.mapIndexed { idx, item -> item == target[idx] }
        itemStatuses = statuses

        val allCorrect = statuses.all { it }
        if (allCorrect) {
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
            feedbackMessage = "الترتيب غير صحيح، تفقد المحطات الملونة باللون الأحمر"
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
