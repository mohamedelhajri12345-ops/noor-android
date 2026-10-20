import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
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

private const val GAME_NAME = "JuzOrderGame"

private val JUZ_STAGES = listOf(
    GameOrderingStage(
        id = 1,
        title = "بداية جزء عمّ (الجزء ٣٠)",
        description = "رتب السور المباركة الأولى من جزء عمّ بالترتيب التسلسلي في المصحف",
        steps = listOf("سورة النبأ", "سورة النازعات", "سورة عبس", "سورة التكوير", "سورة الانفطار")
    ),
    GameOrderingStage(
        id = 2,
        title = "وسط جزء عمّ (الجزء ٣٠)",
        description = "رتب الترتيب الصحيح للسور في منتصف جزء عمّ",
        steps = listOf("سورة المطففين", "سورة الانشقاق", "سورة البروج", "سورة الطارق", "سورة الأعلى")
    ),
    GameOrderingStage(
        id = 3,
        title = "أواخر جزء عمّ (قصار السور)",
        description = "رتب السور الكريمة المتتابعة من سورة الضحى حتى القدر",
        steps = listOf("سورة الضحى", "سورة الشرح", "سورة التين", "سورة العلق", "سورة القدر")
    ),
    GameOrderingStage(
        id = 4,
        title = "خاتمة المصحف الشريف",
        description = "رتب السور السبع الأخيرة المسك في ختام القرآن الكريم",
        steps = listOf("سورة الكوثر", "سورة الكافرون", "سورة النصر", "سورة المسد", "سورة الإخلاص", "سورة الفلق", "سورة الناس")
    ),
    GameOrderingStage(
        id = 5,
        title = "سور جزء تبارك (الجزء ٢٩)",
        description = "رتب السور الست الأولى من جزء تبارك",
        steps = listOf("سورة الملك", "سورة القلم", "سورة الحاقة", "سورة المعارج", "سورة نوح", "سورة الجن")
    ),
    GameOrderingStage(
        id = 6,
        title = "تكملة جزء تبارك",
        description = "رتب السور المباركة الأخيرة في جزء تبارك",
        steps = listOf("سورة المزمل", "سورة المدثر", "سورة القيامة", "سورة الإنسان", "سورة المرسلات")
    ),
    GameOrderingStage(
        id = 7,
        title = "سور جزء قد سمع / المجادلة (الجزء ٢٨)",
        description = "رتب السور الأولى الخمس من جزء المجادلة",
        steps = listOf("سورة المجادلة", "سورة الحشر", "سورة الممتحنة", "سورة الصف", "سورة الجمعة")
    ),
    GameOrderingStage(
        id = 8,
        title = "تكملة جزء المجادلة",
        description = "رتب السور الأخيرة الأربعة في جزء المجادلة",
        steps = listOf("سورة المنافقون", "سورة التغابن", "سورة الطلاق", "سورة التحريم")
    ),
    GameOrderingStage(
        id = 9,
        title = "سور جزء الذاريات (الجزء ٢٧)",
        description = "رتب السور العظيمة الأولى في جزء الذاريات",
        steps = listOf("سورة الذاريات", "سورة الطور", "سورة النجم", "سورة القمر", "سورة الرحمن", "سورة الواقعة", "سورة الحديد")
    ),
    GameOrderingStage(
        id = 10,
        title = "سور جزء قد أفلح (الجزء ١٨)",
        description = "رتب السور الثلاث في جزء قد أفلح",
        steps = listOf("سورة المؤمنون", "سورة النور", "سورة الفرقان")
    ),
    GameOrderingStage(
        id = 11,
        title = "سور جزء الأحقاف (الجزء ٢٦)",
        description = "رتب السور الكريمة الخمس المكونة لجزء الأحقاف",
        steps = listOf("سورة الأحقاف", "سورة محمد", "سورة الفتح", "سورة الحجرات", "سورة ق")
    ),
    GameOrderingStage(
        id = 12,
        title = "أوائل سور المصحف (السبع الطوال)",
        description = "رتب السور الفاضلة الأولى في المصحف الشريف",
        steps = listOf("سورة الفاتحة", "سورة البقرة", "سورة آل عمران", "سورة النساء", "سورة المائدة")
    ),
    GameOrderingStage(
        id = 13,
        title = "سور الأنعام والأعراف والأنفال",
        description = "رتب السور الممتدة من الجزء السابع حتى العاشر",
        steps = listOf("سورة الأنعام", "سورة الأعراف", "سورة الأنفال", "سورة التوبة", "سورة يونس")
    ),
    GameOrderingStage(
        id = 14,
        title = "سور يوسف والإسراء والكهف",
        description = "رتب السور الشريفة من الجزء الثاني عشر إلى السادس عشر",
        steps = listOf("سورة يوسف", "سورة الإسراء", "سورة الكهف", "سورة مريم", "سورة طه")
    ),
    GameOrderingStage(
        id = 15,
        title = "سور الحواميم المسبعة",
        description = "رتب سور الحواميم المبتدئة بـ (حم) في المصحف الشريف",
        steps = listOf("سورة غافر", "سورة فصلت", "سورة الشورى", "سورة الزخرف", "سورة الدخان", "سورة الجاثية")
    ),
    GameOrderingStage(
        id = 16,
        title = "ترتيب السور حسب النزول (أوائل ما نزل)",
        description = "رتب السور القرآنية حسب أسبقية النزول في مكة المكرمة",
        steps = listOf("سورة العلق (أول ما نزل)", "سورة القلم", "سورة المزمل", "سورة المدثر", "سورة الفاتحة")
    ),
    GameOrderingStage(
        id = 17,
        title = "ترتيب السور حسب النزول (أواخر ما نزل)",
        description = "رتب السور القرآنية التي كانت من أواخر ما نزل في المدينة",
        steps = listOf("سورة النصر", "سورة التوبة", "سورة المائدة")
    ),
    GameOrderingStage(
        id = 18,
        title = "ترتيب السور حسب عدد الآيات (من الأطول للأقصر)",
        description = "رتب السور التالية تنازلياً بحسب كثرة عدد آياتها",
        steps = listOf("سورة البقرة (٢٨٦ آية)", "سورة الشعراء (٢٢٧ آية)", "سورة الأعراف (٢٠٦ آية)", "سورة آل عمران (٢٠٠ آية)", "سورة الصافات (١٨٢ آية)")
    ),
    GameOrderingStage(
        id = 19,
        title = "ترتيب قصار السور حسب عدد الآيات",
        description = "رتب السور التالية تنازلياً بحسب عدد آياتها",
        steps = listOf("سورة الضحى (١١ آية)", "سورة التين (٨ آيات)", "سورة القدر (٥ آيات)", "سورة قريش (٤ آيات)", "سورة الكوثر (٣ آيات)")
    ),
    GameOrderingStage(
        id = 20,
        title = "تحدي الأجزاء: من جزء تبارك إلى عمّ",
        description = "رتب السور التالية من الجزء التاسع والعشرين وحتى الثلاثين",
        steps = listOf("سورة الملك (جزء ٢٩)", "سورة الحاقة (جزء ٢٩)", "سورة النبأ (جزء ٣٠)", "سورة الأعلى (جزء ٣٠)", "سورة الإخلاص (جزء ٣٠)")
    )
)

@Composable
fun JuzOrderGame(onBack: () -> Unit) {
    val context = LocalContext.current
    var progressData by remember {
        mutableStateOf(StageProgressStore.loadProgress(context, GAME_NAME, JUZ_STAGES.size))
    }
    var selectedStageIdx by remember { mutableStateOf<Int?>(null) }

    if (selectedStageIdx == null) {
        StageMapScreen(
            gameTitle = "ترتيب الأجزاء والسور",
            gameSubtitle = "٢٠ مرحلة لتحدي ترتيب سور القرآن حسب الأجزاء والنزول",
            totalStages = JUZ_STAGES.size,
            progressData = progressData,
            onSelectStage = { selectedStageIdx = it },
            onBack = onBack
        )
    } else {
        val stageIdx = selectedStageIdx!!
        val currentStage = JUZ_STAGES[stageIdx]

        JuzOrderingPlayScreen(
            stage = currentStage,
            stageNumber = stageIdx + 1,
            totalStages = JUZ_STAGES.size,
            onBackToMap = {
                progressData = StageProgressStore.loadProgress(context, GAME_NAME, JUZ_STAGES.size)
                selectedStageIdx = null
            },
            onCompleteStage = { stars ->
                StageProgressStore.saveProgress(context, GAME_NAME, stageIdx, stars, JUZ_STAGES.size)
                progressData = StageProgressStore.loadProgress(context, GAME_NAME, JUZ_STAGES.size)
            },
            onNextStage = {
                if (stageIdx + 1 < JUZ_STAGES.size) {
                    selectedStageIdx = stageIdx + 1
                } else {
                    selectedStageIdx = null
                }
            }
        )
    }
}

@Composable
private fun JuzOrderingPlayScreen(
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
            feedbackMessage = "الترتيب غير صحيح، تفقد السور الملونة باللون الأحمر"
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
