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

private const val GAME_NAME = "HijriMonthsGame"

private val HIJRI_STAGES = listOf(
    GameOrderingStage(
        id = 1,
        title = "الأشهر الهجرية الستة الأولى",
        description = "رتب الأشهر الستة الأولى من السنة الهجرية بالترتيب الدقيق",
        steps = listOf("محرم", "صفر", "ربيع الأول", "ربيع الآخر", "جمادى الأولى", "جمادى الآخرة")
    ),
    GameOrderingStage(
        id = 2,
        title = "الأشهر الهجرية الستة الأخيرة",
        description = "رتب الأشهر الستة الأخيرة من السنة الهجرية من رجب حتى ذي الحجة",
        steps = listOf("رجب", "شعبان", "رمضان", "شوال", "ذو القعدة", "ذو الحجة")
    ),
    GameOrderingStage(
        id = 3,
        title = "الأشهر الحرم الأربعة",
        description = "رتب الأشهر الحرم الأربعة في تقويم السنة الهجرية",
        steps = listOf("محرم (شهر الله المحرم)", "رجب (الفرد)", "ذو القعدة (أول المتتابعة)", "ذو الحجة (شهر الحج الأكبر)")
    ),
    GameOrderingStage(
        id = 4,
        title = "أشهر موسم العبادات والحج",
        description = "رتب الأشهر الهجرية المتتابعة لموسم الطاعات والحج",
        steps = listOf("رجب المبارك", "شعبان المعظم", "رمضان شهر الصيام", "شوال شهر عيد الفطر", "ذو القعدة شهر القعود للحج", "ذو الحجة شهر المناسك")
    ),
    GameOrderingStage(
        id = 5,
        title = "الأشهر الهجرية ذات الترتيب الزوجي",
        description = "رتب الأشهر الهجرية التي ترتيبها زوجي (٢، ٤، ٦، ٨، ١٠، ١٢)",
        steps = listOf("صفر (الشهر الثاني)", "ربيع الآخر (الشهر الرابع)", "جمادى الآخرة (الشهر السادس)", "شعبان (الشهر الثامن)", "شوال (الشهر العاشر)", "ذو الحجة (الشهر الثاني عشر)")
    ),
    GameOrderingStage(
        id = 6,
        title = "الأشهر الهجرية ذات الترتيب الفردي",
        description = "رتب الأشهر الهجرية التي ترتيبها فردي (١، ٣، ٥، ٧، ٩، ١١)",
        steps = listOf("محرم (الشهر الأول)", "ربيع الأول (الشهر الثالث)", "جمادى الأولى (الشهر الخامس)", "رجب (الشهر السابع)", "رمضان (الشهر التاسع)", "ذو القعدة (الشهر الحادي عشر)")
    ),
    GameOrderingStage(
        id = 7,
        title = "أشهر الربيعين والجماديين",
        description = "رتب أشهر الربيعين ثم الجماديين حسب تسلسل التقويم الهجري",
        steps = listOf("ربيع الأول", "ربيع الآخر", "جمادى الأولى", "جمادى الآخرة")
    ),
    GameOrderingStage(
        id = 8,
        title = "أشهر المناسبات الإسلامية الكبرى",
        description = "رتب الأشهر حسب مناسباتها العظيمة في السيرة والشريعة",
        steps = listOf("محرم (يوم عاشوراء والنجاة)", "ربيع الأول (مولد وهجرة النبي ﷺ)", "رجب (ذكرى الإسراء والمعراج)", "رمضان (نزول القرآن وصوم الفريضة)", "شوال (عيد الفطر السعيد)", "ذو الحجة (عرفة وعيد الأضحى)")
    ),
    GameOrderingStage(
        id = 9,
        title = "أشهر الصيام والتحضير والعيد",
        description = "رتب الأشهر المحيطة بشهر رمضان المبارك",
        steps = listOf("رجب (شهر الغراس)", "شعبان (شهر السقيا ورفع الأعمال)", "رمضان (شهر الحصاد وصيام الفريضة)", "شوال (شهر الجائزة وعيد الفطر)", "ذو القعدة (شهر الاستعداد للحج)")
    ),
    GameOrderingStage(
        id = 10,
        title = "ترتيب النصف الأول من السنة الهجرية",
        description = "رتب كافة أشهر النصف الأول من السنة الهجرية (١ - ٦)",
        steps = listOf("محرم", "صفر", "ربيع الأول", "ربيع الآخر", "جمادى الأولى", "جمادى الآخرة")
    ),
    GameOrderingStage(
        id = 11,
        title = "الأشهر الحرم الثلاثة المتتابعة",
        description = "رتب الأشهر الحرم الثلاثة التي تأتي متتابعة نهاية وبداية العام",
        steps = listOf("ذو القعدة (الشهر ١١)", "ذو الحجة (الشهر ١٢)", "محرم (الشهر ١)")
    ),
    GameOrderingStage(
        id = 12,
        title = "أشهر الثلث الأخير من السنة الهجرية",
        description = "رتب الأشهر الأربعة الأخيرة من السنة الهجرية",
        steps = listOf("رمضان", "شوال", "ذو القعدة", "ذو الحجة")
    ),
    GameOrderingStage(
        id = 13,
        title = "من ربيع الأول إلى شعبان",
        description = "رتب الأشهر الهجرية الممتدة من ربيع الأول حتى شعبان",
        steps = listOf("ربيع الأول", "ربيع الآخر", "جمادى الأولى", "جمادى الآخرة", "رجب", "شعبان")
    ),
    GameOrderingStage(
        id = 14,
        title = "أشهر التجهيز والعبادات الرمضانية",
        description = "رتب الأشهر الهجرية الأربعة المباركة الملازمة لرمضان",
        steps = listOf("رجب الأصم", "شعبان المعظم", "رمضان المبارك", "شوال المكرم")
    ),
    GameOrderingStage(
        id = 15,
        title = "من جمادى الأولى إلى شوال",
        description = "رتب الأشهر الهجرية المتتابعة من الشهر الخامس إلى العاشر",
        steps = listOf("جمادى الأولى", "جمادى الآخرة", "رجب", "شعبان", "رمضان", "شوال")
    ),
    GameOrderingStage(
        id = 16,
        title = "أشهر العبادات في الحرم المكي",
        description = "رتب الأشهر المرتبطة بأعظم المناسك في مكة المكرمة",
        steps = listOf("رجب (عمرة رجب المسنونة)", "رمضان (عمرة رمضان تعدل حجة)", "شوال (من أشهر الحج)", "ذو القعدة (من أشهر الحج والحرم)", "ذو الحجة (أيام الحج الأكبر)")
    ),
    GameOrderingStage(
        id = 17,
        title = "أربعة أشهر متتالية بعد رجب",
        description = "رتب الأشهر الأربعة المتتالية ابتداءً من شعبان",
        steps = listOf("شعبان", "رمضان", "شوال", "ذو القعدة")
    ),
    GameOrderingStage(
        id = 18,
        title = "أشهر البداية والنهاية والوسط",
        description = "رتب أشهر محطات التقويم الهجري الرئيسية",
        steps = listOf("محرم (فاتحة العام الهجري)", "جمادى الآخرة (انتصاف العام الهجري)", "رمضان (سيد الشهور)", "ذو الحجة (ختام العام الهجري)")
    ),
    GameOrderingStage(
        id = 19,
        title = "الأشهر التسعة الأولى بالترتيب",
        description = "رتب الأشهر الهجرية التسعة الأولى من محرم إلى رمضان",
        steps = listOf("محرم", "صفر", "ربيع الأول", "ربيع الآخر", "جمادى الأولى", "جمادى الآخرة", "رجب", "شعبان", "رمضان")
    ),
    GameOrderingStage(
        id = 20,
        title = "التحدي الختامي: السنة الهجرية الكاملة",
        description = "رتب كافة الأشهر الهجرية الـ ١٢ بالترتيب التسلسلي الدقيق",
        steps = listOf("محرم", "صفر", "ربيع الأول", "ربيع الآخر", "جمادى الأولى", "جمادى الآخرة", "رجب", "شعبان", "رمضان", "شوال", "ذو القعدة", "ذو الحجة")
    )
)

@Composable
fun HijriMonthsGame(onBack: () -> Unit) {
    val context = LocalContext.current
    var progressData by remember {
        mutableStateOf(StageProgressStore.loadProgress(context, GAME_NAME, HIJRI_STAGES.size))
    }
    var selectedStageIdx by remember { mutableStateOf<Int?>(null) }

    if (selectedStageIdx == null) {
        StageMapScreen(
            gameTitle = "ترتيب الأشهر الهجرية",
            gameSubtitle = "٢٠ مرحلة لتحدي ترتيب الأشهر والمناسبات الهجرية",
            totalStages = HIJRI_STAGES.size,
            progressData = progressData,
            onSelectStage = { selectedStageIdx = it },
            onBack = onBack
        )
    } else {
        val stageIdx = selectedStageIdx!!
        val currentStage = HIJRI_STAGES[stageIdx]

        HijriMonthsOrderingPlayScreen(
            stage = currentStage,
            stageNumber = stageIdx + 1,
            totalStages = HIJRI_STAGES.size,
            onBackToMap = {
                progressData = StageProgressStore.loadProgress(context, GAME_NAME, HIJRI_STAGES.size)
                selectedStageIdx = null
            },
            onCompleteStage = { stars ->
                StageProgressStore.saveProgress(context, GAME_NAME, stageIdx, stars, HIJRI_STAGES.size)
                progressData = StageProgressStore.loadProgress(context, GAME_NAME, HIJRI_STAGES.size)
            },
            onNextStage = {
                if (stageIdx + 1 < HIJRI_STAGES.size) {
                    selectedStageIdx = stageIdx + 1
                } else {
                    selectedStageIdx = null
                }
            }
        )
    }
}

@Composable
private fun HijriMonthsOrderingPlayScreen(
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
