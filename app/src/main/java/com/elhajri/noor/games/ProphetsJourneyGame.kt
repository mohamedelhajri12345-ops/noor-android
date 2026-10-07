package com.elhajri.noor.games

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.EmojiEvents
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
import com.elhajri.noor.ui.noorGlassCard
import com.elhajri.noor.ui.themeScreenBackground
import kotlinx.coroutines.delay

private fun Int.toArabicDigits(): String {
    val arDigits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
    val str = this.toString()
    val builder = StringBuilder()
    for (ch in str) {
        if (ch in '0'..'9') builder.append(arDigits[ch - '0']) else builder.append(ch)
    }
    return builder.toString()
}

// One station of the prophetic journey: story + questions
private data class JourneyStation(
    val prophet: String,
    val intro: String,
    val questions: List<JourneyQuestion>
)

private data class JourneyQuestion(
    val q: String,
    val options: List<String>,
    val correct: Int
)

private val JOURNEY = listOf(
    JourneyStation(
        "آدم عليه السلام", "أبو البشر أول من وطئ الأرض، علّمه الله الأسماء كلها وأسجد له ملائكته تكريماً.",
        listOf(
            JourneyQuestion("من هو أول نبي من بني آدم؟", listOf("نوح", "آدم", "إدريس", "شيث"), 1),
            JourneyQuestion("علّم الله آدم...", listOf("الزراعة", "الأسماء كلها", "الكتابة فقط", "الصنعة"), 1),
            JourneyQuestion("من أغوى آدم وزوجته في الجنة؟", listOf("إبليس", "هاروت", "ماروت", "الشيطان الأكبر"), 0),
            JourneyQuestion("ما اسم زوج آدم التي خلقت منه؟", listOf("حواء", "سارة", "نوج", "هاجر"), 0),
            JourneyQuestion("أنزل الله على آدم من السماء ماذا؟", listOf("كتاباً", "لوحاً", "كلمات/وحياً", "صحفاً"), 2)
        )
    ),
    JourneyStation(
        "نوح عليه السلام", "أول رسول أرسل إلى قومه عبدة الأصنام، دعاهم ألف سنة إلا خمسين عاماً ثم غرقهم الطوفان.",
        listOf(
            JourneyQuestion("كم لبث نوح يدعو قومه؟", listOf("٥٠٠ سنة", "٩٥٠ سنة", "١٠٠٠ سنة", "٤٠ سنة"), 1),
            JourneyQuestion("بمَ أمره الله أن يصنع؟", listOf("سفينة", "بيتاً", "قصراً", "بيت المقدس"), 0),
            JourneyQuestion("ماذا صنع قومه حين دعاهم؟", listOf("آمنوا", "جعلوا أصابعهم في آذانهم", "سافروا", "هاجروا"), 1),
            JourneyQuestion("من نجا مع نوح في السفينة؟", listOf("قومه كلهم", "المؤمنون وأهله", "الملائكة", "الجن"), 1),
            JourneyQuestion("كان قوم نوح يعبدون...", listOf("الأصنام (وداً وسواعاً)", "الله وحده", "الشمس", "النار"), 0)
        )
    ),
    JourneyStation(
        "إبراهيم عليه السلام", "خليل الرحمن حطّم الأصنام ونبذ النار فكانت برداً وسلاماً، وبنى الكعبة مع ابنه إسماعيل.",
        listOf(
            JourneyQuestion("ما لقب إبراهيم؟", listOf("خليل الله", "كليم الله", "روح الله", "نبي الله"), 0),
            JourneyQuestion("ماذا حدث للنار التي ألقى فيها؟", listOf("خمدت", "صارت برداً وسلاماً", "أضاءت", "لم تصل إليه"), 1),
            JourneyQuestion("من ابنه الذي أمر بذبحه؟", listOf("إسحاق", "إسماعيل", "يعقوب", "يوسف"), 1),
            JourneyQuestion("من شاركه بناء الكعبة؟", listOf("إسماعيل", "إسحاق", "لوط", "يعقوب"), 0),
            JourneyQuestion("آية إبراهيم في الطير كانت...", listOf("طيوراً تُدعى فتأتيه", "حماماً يهاجر", "نعاماً", "غراباً"), 0)
        )
    ),
    JourneyStation(
        "لوط عليه السلام", "ابن أخي إبراهيم أُرسل إلى سدوم أهل الفاحشة، فأهلكهم الله وحُفظت قراه للعبرة.",
        listOf(
            JourneyQuestion("إلى أي قوم أُرسل لوط؟", listOf("قوم سدوم", "قوم عاد", "قوم ثمود", "أهل نينوى"), 0),
            JourneyQuestion("ما الفاحشة التي أتى قومه؟", listOf("الزنا", "الإتيان الذكران", "السرقة", "الكفر فقط"), 1),
            JourneyQuestion("من آمن معه من قومه؟", listOf("كل أهله", "إلا امرأته", "لا أحد", "قبيلة كاملة"), 1),
            JourneyQuestion("ماذا أمطر الله عليهم؟", listOf("حجارة سجّيل", "ثلجاً", "ريحاً صرصراً", "طوفاناً"), 0),
            JourneyQuestion("لماذا نجّى الله لوطاً؟", listOf("لصهره", "لأنه آمن", "لإيمانه وأمره بالمعروف", "لغناه"), 2)
        )
    ),
    JourneyStation(
        "إسماعيل عليه السلام", "الصبي الذي صبر على المذبح وسقته زمزم، وأبو العرب وآل النبي ﷺ.",
        listOf(
            JourneyQuestion("من أم إسماعيل؟", listOf("هاجر", "سارة", "مريم", "آسية"), 0),
            JourneyQuestion("ماذا فجر الله لإسماعيل وأمه؟", listOf("بئر زمزم", "نهر النيل", "عيناً في دمشق", "مطراً"), 0),
            JourneyQuestion("بأي صفة وصفه القرآن؟", listOf("صادق الوعد", "حليم", "قوي", "كريم"), 0),
            JourneyQuestion("من نسل إسماعيل جاء...", listOf("النبي ﷺ", "داود", "سليمان", "موسى"), 0),
            JourneyQuestion("سكن إسماعيل وأمه...", listOf("مكة", "فلسطين", "اليمن", "الشام"), 0)
        )
    ),
    JourneyStation(
        "إسحاق عليه السلام", "البشرة التي جاءت إبراهيم بعد الكبر، نبيٌّ من الصالحين أوتي الحكم والعلم.",
        listOf(
            JourneyQuestion("من بشّر بإسحاق؟", listOf("الملائكة", "إسماعيل", "نوح", "الجن"), 0),
            JourneyQuestion("إسحاق هو ابن...", listOf("إبراهيم وسارة", "إبراهيم وهاجر", "لوط", "يعقوب"), 0),
            JourneyQuestion("من ابن إسحاق النبي؟", listOf("يعقوب", "يوسف", "أيوب", "شعيب"), 0),
            JourneyQuestion("وصفه الله بأنه...", listOf("نبي من الصالحين", "خليل", "كليم", "نذير"), 0),
            JourneyQuestion("أُرسل إلى قوم...", listOf("الشام أهلها", "قوم عاد", "أهل مكة", "أهل نينوى"), 0)
        )
    ),
    JourneyStation(
        "يعقوب عليه السلام", "إسرائيل أبو الأسباط صبر على فقد يوسف حتى ابيضّت عيناه من الحزن ثم اجتمعت العائلة.",
        listOf(
            JourneyQuestion("لقب يعقوب هو...", listOf("إسرائيل", "الطبيب", "الصديق", "النجار"), 0),
            JourneyQuestion("من ابنه النبي الجميل؟", listOf("يوسف", "يهوذا", "روبين", "لاوي"), 0),
            JourneyQuestion("لماذا ابيضّت عيناه؟", listOf("من الحزن على يوسف", "من كبر السن فقط", "مرض", "حسد"), 0),
            JourneyQuestion("أسباط بني إسرائيل عددهم...", listOf("١٢", "٧", "١٠", "٤"), 0),
            JourneyQuestion("عند من استقر يعقوب آخر عمره؟", listOf("مصر عند يوسف", "كنعان", "مكة", "بابل"), 0)
        )
    ),
    JourneyStation(
        "يوسف عليه السلام", "الصديق رؤياه أحد عشر كوكباً، وجرّ به الجُبّ والسجن ثم صار على خزائن مصر.",
        listOf(
            JourneyQuestion("رأى يوسف في منامه...", listOf("أحد عشر كوكباً والشمس والقمر", "بحراً", "جبلاً", "سفينة"), 0),
            JourneyQuestion("أين ألقاه إخوته؟", listOf("في بئر", "في النهر", "في الصحراء", "في غابة"), 0),
            JourneyQuestion("من اشتراه في مصر؟", listOf("العزيز", "الملك", "تاجر", "جندي"), 0),
            JourneyQuestion("كم لبث في السجن؟", listOf("سبع سنين", "عشر سنين", "ثلاث سنين", "سنة"), 0),
            JourneyQuestion("ماذا قال في خواتيم سيرته؟", listOf("اقضني إليك طيباً وألحقني بالصالحين", "رب اغفر لي", "الحمد لله", "ربّ ارحمني"), 0)
        )
    ),
    JourneyStation(
        "شعيب عليه عليه السلام", "خطيب الأنبياء أُرسل إلى مدين ينهاهم عن نقص الكيل والميزان فعاقبهم يوم الظلة.",
        listOf(
            JourneyQuestion("إلى أي قوم أُرسل شعيب؟", listOf("أصحاب مدين", "أصحاب الأيكة", "قوم نوح", "ثمود"), 0),
            JourneyQuestion("بمَ أمر قومه؟", listOf("أوفوا الكيل والميزان", "الصلاة فقط", "الصيام", "الحج"), 0),
            JourneyQuestion("لقب شعيب هو...", listOf("خطيب الأنبياء", "كليم الله", "الصديق", "النجار"), 0),
            JourneyQuestion("ما العقاب الذي أخذهم؟", listOf("يوم الظلة (عذاب يوم الظلة)", "طوفان", "ريح صرصر", "صيحة"), 0),
            JourneyQuestion("من صهر شعيب؟", listOf("موسى", "هارون", "يوشع", "إلياس"), 0)
        )
    ),
    JourneyStation(
        "موسى عليه السلام", "كليم الله أُلقي في اليم فالتقطه آل فرعون، وفلق البحر وتلقى التوراة.",
        listOf(
            JourneyQuestion("من ربّى موسى؟", listOf("آل فرعون", "قومه", "الملائكة", "شعيب"), 0),
            JourneyQuestion("بمَ كلمه الله؟", listOf("تكليماً", "كتاباً", "رؤيا", "ملكاً"), 0),
            JourneyQuestion("ماذا ضرب البحر بعصاه؟", listOf("فانفلق اثني عشر سبيلًا", "فجمد", "فغاض", "فحصر"), 0),
            JourneyQuestion("ما الكتاب الذي أُنزل عليه؟", listOf("التوراة", "الإنجيل", "الزبور", "الصحف"), 0),
            JourneyQuestion("من نبيٌّ سار معه لطلب العلم؟", listOf("الخضر", "هارون", "يوشع", "إلياس"), 0)
        )
    ),
    JourneyStation(
        "هارون عليه السلام", "شقيق موسى وزيره وأخوه في الرسالة، نبيٌّ قويٌّ في جانب الله.",
        listOf(
            JourneyQuestion("ما طلب موسى من ربه لأخيه؟", listOf("اجعله وزيراً", "اجعله خليفة", "اجعله نبياً فقط", "اغفر له"), 0),
            JourneyQuestion("على من سخط هارون حين عبدوا العجل؟", listOf("قومه", "موسى", "الله", "الملائكة"), 0),
            JourneyQuestion("هارون كان...", listOf("أخا موسى الأكبر", "ابن موسى", "عمّ موسى", "ابن عمه"), 0),
            JourneyQuestion("لماذا لم يمنعهم عن العجل قهراً؟", listOf("خاف تفريق القوم", "كان مسافراً", "لم يعلم", "أمر بتركه"), 0),
            JourneyQuestion("وصف القرآن هارون بأنه...", listOf("نبي كريم من المسلمين", "كليم", "خليل", "رسول من الملائكة"), 0)
        )
    ),
    JourneyStation(
        "داود عليه السلام", "قتل جالوت وملُك وأُوتي الزبور وأوتي منطق الطير وشدّ الملك.",
        listOf(
            JourneyQuestion("من قتل داود جالوت؟", listOf("داود", "طالوت", "موسى", "يهوذا"), 0),
            JourneyQuestion("ما الكتاب الذي أوتيه؟", listOf("الزبور", "التوراة", "الإنجيل", "الصحف"), 0),
            JourneyQuestion("أوتي داود...", listOf("منطق الطير", "فلق البحر", "رؤيا", "علم الغيب"), 0),
            JourneyQuestion("من ابنه النبي الملك؟", listOf("سليمان", "أشعياء", "إرميا", "زكريا"), 0),
            JourneyQuestion("داود كان ملكاً و...", listOf("نبياً", "قاضياً فقط", "تاجراً", "كاهناً"), 0)
        )
    ),
    JourneyStation(
        "سليمان عليه السلام", "ملك لا ينبغي لأحد من بعده، يسّر له الريح وعرف منطق الطير والنمل.",
        listOf(
            JourneyQuestion("ما سخّر الله لسليمان؟", listOf("الريح", "البحر", "النار", "الجبال"), 0),
            JourneyQuestion("من قال: يا نمل ادخلوا مساكنكم؟", listOf("النملة", "الهدهد", "الطير", "الجن"), 0),
            JourneyQuestion("من الطير الذي أخبره بسبأ؟", listOf("الهدهد", "النسر", "الحمام", "الغراب"), 0),
            JourneyQuestion("من جاء من الجن يعمل بين يديه؟", listOf("عفريت", "إبليس", "ملك", "شيطان البحر"), 0),
            JourneyQuestion("علّمه الله منطق...", listOf("الطير", "السباع", "الأنسام", "الريح"), 0)
        )
    ),
    JourneyStation(
        "عيسى عليه السلام", "كلمة الله وروح منه، تكلّم في المهد وأبرأ الأكمه والأبرص بإذن الله.",
        listOf(
            JourneyQuestion("ما معجزة عيسى في المهد؟", listOf("تكلم وهو رضيع", "مشي وهو حديث", "قرأ الكتب", "صنع طيراً"), 0),
            JourneyQuestion("من أمه؟", listOf("مريم", "آسية", "هاجر", "سارة"), 0),
            JourneyQuestion("ماذا كان يصنع من الطين بإذن الله؟", listOf("طيراً فيطير", "أصناماً", "أواني", "تماثيل"), 0),
            JourneyQuestion("أُرسل إلى...", listOf("بني إسرائيل", "قوم نوح", "أهل مكة", "أهل نينوى"), 0),
            JourneyQuestion("ما الكتاب الذي أوتيه؟", listOf("الإنجيل", "الزبور", "التوراة", "القرآن"), 0)
        )
    ),
    JourneyStation(
        "محمد ﷺ", "خاتم النبيين رحمة للعالمين، أوتي القرآن وجعل الله أمته خير أمة أُخرجت للناس.",
        listOf(
            JourneyQuestion("من هو خاتم النبيين؟", listOf("محمد ﷺ", "عيسى", "موسى", "إبراهيم"), 0),
            JourneyQuestion("ما الكتاب الذي أُنزل عليه؟", listOf("القرآن الكريم", "الإنجيل", "الزبور", "الصحف"), 0),
            JourneyQuestion("أول ما نزل عليه كان...", listOf("اقرأ باسم ربك", "الحمد لله", "يا أيها المدثر", "قل هو الله أحد"), 0),
            JourneyQuestion("أُرسل إلى...", listOf("الثقلين جميعاً", "بني إسرائيل", "قوم مكة فقط", "العرب فقط"), 0),
            JourneyQuestion("وصفه الله بأنه...", listOf("رحمة للعالمين", "كليم", "خليل فقط", "نجار"), 0)
        )
    )
)

@Composable
fun ProphetsJourneyGameScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    ForcePortraitOrientation()

    val totalLevels = JOURNEY.size

    var progressData by remember { mutableStateOf(NewGameProgress.load(context, "game_prophets_journey")) }
    var currentLevel by remember { mutableStateOf<Int?>(null) }
    var totalScore by remember { mutableStateOf(progressData.score) }
    var highestUnlocked by remember { mutableStateOf(progressData.level) }
    var totalStars by remember { mutableStateOf(progressData.stars) }

    Box(Modifier.fillMaxSize().themeScreenBackground()) {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            // Header
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = {
                    if (currentLevel != null) currentLevel = null else onBack()
                }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = Gold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("رحلة الأنبياء", color = Gold, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("١٥ محطة في سير المرسلين", color = GoldSoft, fontSize = 11.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Gold, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(totalScore.toArabicDigits(), color = Gold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(12.dp))

            if (currentLevel == null) {
                // Level map — each station is a level
                Text(
                    "اختر المحطة",
                    color = GoldSoft, fontSize = 13.sp,
                    modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(10.dp))
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    itemsIndexed(JOURNEY) { idx, station ->
                        val lvl = idx + 1
                        val unlocked = lvl <= highestUnlocked
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .noorGlassCard(cornerRadius = 12.dp)
                                .clickable { if (unlocked) currentLevel = lvl }
                                .padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (unlocked) {
                                Text(lvl.toArabicDigits(), color = Gold, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                Text(station.prophet, color = Color.White, fontSize = 10.sp, maxLines = 1)
                            } else {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = GoldSoft.copy(alpha = 0.5f), modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            } else {
                val lvl = currentLevel!!
                val station = JOURNEY[lvl - 1]

                var qIndex by remember(lvl) { mutableStateOf(0) }
                var introShown by remember(lvl) { mutableStateOf(false) }
                var correctCount by remember(lvl) { mutableStateOf(0) }
                var selected by remember(lvl) { mutableStateOf<Int?>(null) }
                var finished by remember(lvl) { mutableStateOf(false) }

                if (finished) {
                    val stars = when {
                        correctCount >= 5 -> 3
                        correctCount >= 4 -> 2
                        correctCount >= 3 -> 1
                        else -> 0
                    }
                    val earned = correctCount * 25 * lvl
                    Column(
                        Modifier.fillMaxSize().noorGlassCard(cornerRadius = 18.dp).padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Gold, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(10.dp))
                        Text("اكتملت المحطة ${lvl.toArabicDigits()}", color = Gold, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(station.prophet, color = GoldSoft, fontSize = 13.sp)
                        Text("أجبت ${correctCount.toArabicDigits()} من ٥", color = GoldSoft, fontSize = 12.sp)
                        Row {
                            repeat(3) { s ->
                                Icon(
                                    Icons.Default.Star, contentDescription = null,
                                    tint = if (s < stars) Gold else GoldSoft.copy(alpha = 0.25f),
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        Spacer(Modifier.height(14.dp))
                        Button(
                            onClick = {
                                totalScore += earned
                                if (lvl >= highestUnlocked && stars >= 2) {
                                    highestUnlocked = (lvl + 1).coerceAtMost(totalLevels)
                                }
                                if (stars >= 1) totalStars += 1
                                NewGameProgress.save(
                                    context, "game_prophets_journey",
                                    highestUnlocked, totalScore,
                                    progressData.streak + 1, totalStars, ""
                                )
                                progressData = NewGameProgress.load(context, "game_prophets_journey")
                                currentLevel = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Gold.copy(alpha = 0.2f))
                        ) { Text("متابعة الرحلة", color = Gold) }
                    }
                } else if (!introShown) {
                    // Story intro screen
                    Column(
                        Modifier.fillMaxSize().noorGlassCard(cornerRadius = 18.dp).padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("المحطة ${lvl.toArabicDigits()}", color = GoldSoft, fontSize = 12.sp)
                        Spacer(Modifier.height(8.dp))
                        Text(station.prophet, color = Gold, fontSize = 22.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(12.dp))
                        Text(station.intro, color = Color.White, fontSize = 14.sp, textAlign = TextAlign.Center, lineHeight = 24.sp)
                        Spacer(Modifier.height(18.dp))
                        Button(
                            onClick = { introShown = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Gold.copy(alpha = 0.2f))
                        ) { Text("ابدأ الأسئلة", color = Gold) }
                    }
                } else {
                    val q = station.questions[qIndex]
                    Column(Modifier.fillMaxSize()) {
                        // Progress line
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text(station.prophet, color = GoldSoft, fontSize = 11.sp)
                            Spacer(Modifier.weight(1f))
                            Text("${(qIndex + 1).toArabicDigits()} / ٥", color = GoldSoft, fontSize = 11.sp)
                        }
                        LinearProgressIndicator(
                            progress = { (qIndex + 1f) / station.questions.size },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            color = Gold,
                            trackColor = GoldSoft.copy(alpha = 0.15f)
                        )
                        Spacer(Modifier.height(14.dp))

                        Text(q.q, color = Gold, fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(18.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            q.options.forEachIndexed { i, opt ->
                                val isCorrect = i == q.correct
                                val bg = when {
                                    selected == i && isCorrect -> Color(0xFF1B4332)
                                    selected == i && !isCorrect -> Color(0xFF5C1F2A)
                                    else -> Color.Transparent
                                }
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .noorGlassCard(cornerRadius = 14.dp)
                                        .clickable {
                                            if (selected == null) {
                                                selected = i
                                                if (isCorrect) correctCount++
                                            }
                                        }
                                        .padding(14.dp)
                                        .background(bg, RoundedCornerShape(14.dp)),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(opt, color = Color.White, fontSize = 14.sp, modifier = Modifier.weight(1f))
                                    if (selected != null && isCorrect) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Gold, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }

                    LaunchedEffect(selected) {
                        if (selected != null) {
                            delay(1100)
                            selected = null
                            if (qIndex < station.questions.size - 1) qIndex++ else finished = true
                        }
                    }
                }
            }
        }
    }
}
