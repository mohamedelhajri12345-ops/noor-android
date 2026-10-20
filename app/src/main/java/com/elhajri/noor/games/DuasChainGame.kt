package com.elhajri.noor.games
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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

private const val GAME_NAME = "DuasChainGame"
private const val TOTAL_STAGES = 25

private data class DuaStage(
    val id: Int,
    val title: String,
    val firstHalf: String,
    val correctCompletion: String,
    val options: List<String>,
    val explanation: String
)

private val DUA_STAGES = listOf(
    DuaStage(
        id = 1,
        title = "دعاء دخول المسجد",
        firstHalf = "اللَّهُمَّ افْتَحْ لِي...",
        correctCompletion = "...أَبْوَابَ رَحْمَتِكَ",
        options = listOf("...أَبْوَابَ رَحْمَتِكَ", "...فَضْلَكَ وَرَحْمَتَكَ", "...أَبْوَابَ الجَنَّةِ", "...رِزْقَكَ المَبْسُوطَ"),
        explanation = "يُستحب عند دخول المسجد تقديم الرجل اليمنى والصلاة على النبي ﷺ ثم قول هذا الدعاء."
    ),
    DuaStage(
        id = 2,
        title = "دعاء الخروج من المسجد",
        firstHalf = "اللَّهُمَّ إِنِّي أَسْأَلُكَ مِنْ...",
        correctCompletion = "...فَضْلِكَ وَرَحْمَتِكَ",
        options = listOf("...فَضْلِكَ وَرَحْمَتِكَ", "...أَبْوَابِ رَحْمَتِكَ", "...نَعِيمِ الجَنَّةِ", "...عَظِيمِ مَغْفِرَتِكَ"),
        explanation = "يُستحب عند الخروج تقديم الرجل اليسرى وسؤال الله من فضله الكبير."
    ),
    DuaStage(
        id = 3,
        title = "دعاء قبل الطعام",
        firstHalf = "بِسْمِ اللَّهِ، اللَّهُمَّ بَارِكْ لَنَا فِيمَا رَزَقْتَنَا...",
        correctCompletion = "...وَقِنَا عَذَابَ النَّارِ",
        options = listOf("...وَقِنَا عَذَابَ النَّارِ", "...وَارْزُقْنَا خَيْراً مِنْهُ", "...وَاجْعَلْهُ شِفَاءً لَنَا", "...وَأَطْعِمْنَا مِنْ طَعَامِ الجَنَّةِ"),
        explanation = "التسمية قبل الطعام تمنع مشاركة الشيطان وتجلب البركة في الرزق."
    ),
    DuaStage(
        id = 4,
        title = "دعاء الاستيقاظ من النوم",
        firstHalf = "الْحَمْدُ لِلَّهِ الَّذِي أَحْيَانَا بَعْدَ مَا أَمَاتَنَا...",
        correctCompletion = "...وَإِلَيْهِ النُّشُورُ",
        options = listOf("...وَإِلَيْهِ النُّشُورُ", "...وَبِهِ نَسْتَعِينُ", "...وَإِلَيْهِ المَصِيرُ", "...وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ"),
        explanation = "النوم موتة صغرى، والحمد عند الاستيقاظ اعتراف بنعمة إعادة الروح."
    ),
    DuaStage(
        id = 5,
        title = "دعاء السفر",
        firstHalf = "سُبْحَانَ الَّذِي سَخَّرَ لَنَا هَذَا...",
        correctCompletion = "...وَمَا كُنَّا لَهُ مُقْرِنِينَ",
        options = listOf("...وَمَا كُنَّا لَهُ مُقْرِنِينَ", "...وَمَا كُنَّا لَهُ ظَالِمِينَ", "...وَإِنَّا إِلَى المَصِيرِ", "...وَهُوَ العَلِيُّ العَظِيمُ"),
        explanation = "دعاء المسافر يذكره بقدرة الله وعجزه لولا تسخير الله له الوسائل."
    ),
    DuaStage(
        id = 6,
        title = "دعاء الكرب والضيق",
        firstHalf = "لا إِلَهَ إِلا اللَّهُ الْعَظِيمُ الْحَلِيمُ، لا إِلَهَ إِلا اللَّهُ...",
        correctCompletion = "...رَبُّ الْعَرْشِ الْعَظِيمِ",
        options = listOf("...رَبُّ الْعَرْشِ الْعَظِيمِ", "...رَبُّ السَّمَاوَاتِ وَالأَرْضِ", "...العَلِيُّ القَدِيرُ", "...الرَّحْمَنُ الرَّحِيمُ"),
        explanation = "دعاء المكروب يتضمن توحيد الله والثناء عليه بعظمته ورحمته."
    ),
    DuaStage(
        id = 7,
        title = "دعاء إفطار الصائم",
        firstHalf = "ذَهَبَ الظَّمَأُ وَابْتَلَّتِ الْعُرُوقُ...",
        correctCompletion = "...وَثَبَتَ الأَجْرُ إِنْ شَاءَ اللَّهُ",
        options = listOf("...وَثَبَتَ الأَجْرُ إِنْ شَاءَ اللَّهُ", "...وَتَقَبَّلَ اللَّهُ طَاعَتَنَا", "...وَغَفَرَ اللَّهُ ذُنُوبَنَا", "...وَرُزِقْنَا خَيْراً كَثِيراً"),
        explanation = "دعاء نبينا ﷺ عند إفطاره يبشر الصائم بظفر الأجر بعد مشقة الصوم."
    ),
    DuaStage(
        id = 8,
        title = "دعاء النوم",
        firstHalf = "بِاسْمِكَ رَبِّي وَضَعْتُ جَنْبِي وَبِكَ...",
        correctCompletion = "...أَرْفَعُهُ",
        options = listOf("...أَرْفَعُهُ", "...أَسْتَقِيمُ", "...أَسْتَعِينُ", "...أَسْتَغْفِرُك"),
        explanation = "تسليم النفس لله قبل النوم، فإن أمسكها رحِمها وإن أرسلها حَفِظها."
    ),
    DuaStage(
        id = 9,
        title = "دعاء دخول المنزل",
        firstHalf = "بِسْمِ اللَّهِ وَلَجْنَا، وَبِسْمِ اللَّهِ خَرَجْنَا...",
        correctCompletion = "...وَعَلَى اللَّهِ رَبِّنَا تَوَكَّلْنَا",
        options = listOf("...وَعَلَى اللَّهِ رَبِّنَا تَوَكَّلْنَا", "...وَإِلَيْهِ رَغِبْنَا", "...وَبِهِ المَصِيرُ", "...وَلَهُ الحَمْدُ ثَانِياً"),
        explanation = "ذكر الله عند دخول البيت يطرد الشياطين ويعمّ البيت والاهل بالسلام والبركة."
    ),
    DuaStage(
        10,
        title = "دعاء الخروج من المنزل",
        firstHalf = "بِسْمِ اللَّهِ، تَوَكَّلْتُ عَلَى اللَّهِ...",
        correctCompletion = "...وَلا حَوْلَ وَلا قُوَّةَ إِلا بِاللَّهِ",
        options = listOf("...وَلا حَوْلَ وَلا قُوَّةَ إِلا بِاللَّهِ", "...وَهُوَ نِعْمَ المَوْلَى", "...وَإِلَيْهِ المَبْعَثُ", "...وَالحَمْدُ لِلَّهِ رَبِّ العَالَمِينَ"),
        explanation = "من قال هذا عند خروجه قيل له: كُفِيتَ وَوُقِيتَ وَهُدِيتَ، وتنحى عنه الشيطان."
    ),
    DuaStage(
        11,
        title = "دعاء الفراغ من الوضوء",
        firstHalf = "أَشْهَدُ أَنْ لا إِلَهَ إِلا اللَّهُ وَحْدَهُ لا شَرِيكَ لَهُ...",
        correctCompletion = "...وَأَشْهَدُ أَنَّ مُحَمَّداً عَبْدُهُ وَرَسُولُهُ",
        options = listOf("...وَأَشْهَدُ أَنَّ مُحَمَّداً عَبْدُهُ وَرَسُولُهُ", "...وَأَنَّ الجَنَّةَ حَقٌّ", "...وَأَسْأَلُهُ التَّوْبَةَ وَالمَغْفِرَةَ", "...وَلَهُ الحَمْدُ فِى الأُولَى وَالآخِرَةِ"),
        explanation = "من قال هذا الدعاء بعد الوضوء فُتحت له أبواب الجنة الثمانية يدخل من أيها شاء."
    ),
    DuaStage(
        12,
        title = "دعاء لبس الثوب الجديد",
        firstHalf = "الْحَمْدُ لِلَّهِ الَّذِي كَسَانِي هَذَا الثَّوْبَ وَرَزَقَنِيهِ...",
        correctCompletion = "...مِنْ غَيْرِ حَوْلٍ مِنِّي وَلا قُوَّةٍ",
        options = listOf("...مِنْ غَيْرِ حَوْلٍ مِنِّي وَلا قُوَّةٍ", "...وَجَعَلَهُ خَيْراً وَبَرَكَةً", "...وَبَارَكَ لِي فِيهِ", "...وَحَفِظَنِي مِنْ كُلِّ سُوءٍ"),
        explanation = "شكر الله على نعمة الكسوة يتوج العبد بالتواضع ونسبة الفضل لوليه."
    ),
    DuaStage(
        13,
        title = "دعاء دخول الخلاء",
        firstHalf = "اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنَ...",
        correctCompletion = "...الْخُبُثِ وَالْخَبَائِثِ",
        options = listOf("...الْخُبُثِ وَالْخَبَائِثِ", "...الشَّيْطَانِ وَالسِّحْرِ", "...السُّوءِ وَالضُّرِّ", "...الفَقْرِ وَالدَّيْنِ"),
        explanation = "الاستعاذة بالرب قبل دخول أماكن الخلاء لحماية المسلم من أذى الشياطين."
    ),
    DuaStage(
        14,
        title = "دعاء الخروج من الخلاء",
        firstHalf = "الْحَمْدُ لِلَّهِ الَّذِي أَذْهَبَ عَنِّي الأَذَى...",
        correctCompletion = "...وَعَافَانِي",
        options = listOf("...وَعَافَانِي", "...وَغَفَرَ لِي", "...وَرَزَقَنِي", "...وَهَدَانِي"),
        explanation = "يقول الخارج أيضاً: (غُفْرَانَكَ)، شكراً لله على تيسير خروج الأذى وعافية البدن."
    ),
    DuaStage(
        15,
        title = "دعاء ركوب الدابة والمواصلات",
        firstHalf = "بِسْمِ اللَّهِ، الْحَمْدُ لِلَّهِ، سُبْحَانَ الَّذِي سَخَّرَ لَنَا هَذَا وَمَا كُنَّا لَهُ مُقْرِنِينَ...",
        correctCompletion = "...وَإِنَّا إِلَى رَبِّنَا لَمُنْقَلِبُونَ",
        options = listOf("...وَإِنَّا إِلَى رَبِّنَا لَمُنْقَلِبُونَ", "...وَإِلَيْهِ المَصِيرُ", "...وَلَهُ الحَمْدُ المَجِيدُ", "...وَهُوَ العَلِيُّ العَظِيمُ"),
        explanation = "شكر المنعم عند استخدام وسائل النقل واستحضار رحلة الرجوع الأخيرة للآخرة."
    ),
    DuaStage(
        16,
        title = "دعاء نزول المطر",
        firstHalf = "اللَّهُمَّ صَيِّباً...",
        correctCompletion = "...نَافِعاً",
        options = listOf("...نَافِعاً", "...مُبَارَكاً", "...هَنِيئاً", "...غَزِيراً"),
        explanation = "نزول المطر وقت استجابة للدعاء، ويسأل المسلم ربه أن يجعله نافعاً غير ضار."
    ),
    DuaStage(
        17,
        title = "دعاء بعد صلاة الفجر",
        firstHalf = "اللَّهُمَّ إِنِّي أَسْأَلُكَ عِلْماً نَافِعاً، وَرِزْقاً طَيِّباً...",
        correctCompletion = "...وَعَمَلاً مُتَقَبَّلاً",
        options = listOf("...وَعَمَلاً مُتَقَبَّلاً", "...وَعَفْواً شَامِلاً", "...وَعُمْراً طَوِيلاً", "...وَصِحَّةً فِي البَدَنِ"),
        explanation = "من أدعية النبي ﷺ الصباحية اليومية بعد انقضاء صلاة الفجر."
    ),
    DuaStage(
        18,
        title = "سيد الاستغفار",
        firstHalf = "اللَّهُمَّ أَنْتَ رَبِّي لا إِلَهَ إِلا أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ، وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ، أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ...",
        correctCompletion = "...أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ وَأَبُوءُ بِذَنْبِي",
        options = listOf("...أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ وَأَبُوءُ بِذَنْبِي", "...وَأَسْأَلُكَ العَفْوَ وَالمَغْفِرَةَ", "...فَاغْفِرْ لِي ذُنُوبِي كُلَّهَا", "...فَإِنَّهُ لا يَغْفِرُ الذُّنُوبَ إِلا أَنْتَ"),
        explanation = "من قال سيد الاستغفار موقناً به ومات في يومه أو ليلته دخل الجنة."
    ),
    DuaStage(
        19,
        title = "دعاء عيادة المريض",
        firstHalf = "لا بَأْسَ...",
        correctCompletion = "...طَهُورٌ إِنْ شَاءَ اللَّهُ",
        options = listOf("...طَهُورٌ إِنْ شَاءَ اللَّهُ", "...شِفَاءٌ وَعَافِيَةٌ", "...رَحْمَةٌ وَمَغْفِرَةٌ", "...أَجْرٌ وَعَافِيَةٌ"),
        explanation = "الدعاء للمريض يبث الأمل والنور في نفسه ويبشره بطهارة الذنوب."
    ),
    DuaStage(
        20,
        title = "دعاء رؤية الهلال",
        firstHalf = "اللَّهُمَّ أَهِلَّهُ عَلَيْنَا بِالأَمْنِ وَالإِيمَانِ، وَالسَّلامَةِ وَالإِسْلامِ...",
        correctCompletion = "...رَبِّي وَرَبُّكَ اللَّهُ",
        options = listOf("...رَبِّي وَرَبُّكَ اللَّهُ", "...وَبَارِكْ لَنَا فِيهِ", "...وَارْزُقْنَا خَيْراً كَثِيراً", "...وَبَلِّغْنَا صِيَامَهُ"),
        explanation = "استقبال بدايات الأشهر الهجرية بسؤال الله السلامة والتسليم لخالقه سبحانه."
    ),
    DuaStage(
        21,
        title = "دعاء سماع الأذان",
        firstHalf = "اللَّهُمَّ رَبَّ هَذِهِ الدَّعْوَةِ التَّامَّةِ وَالصَّلاةِ القَائِمَةِ، آتِ مُحَمَّداً الوَسِيلَةَ وَالَفَضِيلَةَ...",
        correctCompletion = "...وَابْعَثْهُ مَقَاماً مَحْمُوداً الَّذِي وَعَدْتَهُ",
        options = listOf("...وَابْعَثْهُ مَقَاماً مَحْمُوداً الَّذِي وَعَدْتَهُ", "...وَارْزُقْنَا شَفَاعَتَهُ يَوْمَ القِيَامَةِ", "...وَاجْعَلْنَا مِنْ أَهْلِ طَاعَتِهِ", "...وَاغْفِرْ لَنَا وَلِوَالِدَيْنَا"),
        explanation = "من قال هذا الدعاء بعد إجابة المؤذن حلت له شفاعة النبي ﷺ يوم القيامة."
    ),
    DuaStage(
        22,
        title = "دعاء سجود التلاوة",
        firstHalf = "سَجَدَ وَجْهِيَ لِلَّذِي خَلَقَهُ وَشَقَّ سَمْعَهُ وَبَصَرَهُ...",
        correctCompletion = "...بِحَوْلِهِ وَقُوَّتِهِ",
        options = listOf("...بِحَوْلِهِ وَقُوَّتِهِ", "...بِعِزَّتِهِ وَجَلالِهِ", "...بِرَحْمَتِهِ وَفَضْلِهِ", "...بِقُدْرَتِهِ وَعَظَمَتِهِ"),
        explanation = "الخضوع والتسبيح عند المرور بآيات السجود في كتاب الله عز وجل."
    ),
    DuaStage(
        23,
        title = "دعاء الوقاية عند الجماع",
        firstHalf = "بِسْمِ اللَّهِ، اللَّهُمَّ جَنِّبْنَا الشَّيْطَانَ...",
        correctCompletion = "...وَجَنِّبِ الشَّيْطَانَ مَا رَزَقْتَنَا",
        options = listOf("...وَجَنِّبِ الشَّيْطَانَ مَا رَزَقْتَنَا", "...وَبَارِكْ لَنَا فِي أَهْلِنَا", "...وَاحْفَظْنَا مِنْ كُلِّ سُوءٍ", "...وَارْزُقْنَا الذُّرِّيَّةَ الصَّالِحَةَ"),
        explanation = "حصن مبارك للذرية، فإن قُدر بينهما ولد لم يضره الشيطان أبدًا."
    ),
    DuaStage(
        24,
        title = "دعاء التعزية للمصاب",
        firstHalf = "إِنَّ لِلَّهِ مَا أَخَذَ، وَلَهُ مَا أَعْطَى، وَكُلٌّ عِنْدَهُ بِأَجَلٍ مُسَمًّى...",
        correctCompletion = "...فَالْصَبِرْ وَلْتَحْتَسِبْ",
        options = listOf("...فَالْصَبِرْ وَلْتَحْتَسِبْ", "...وَأَعْظَمَ اللَّهُ أَجْرَكَ", "...وَاغْفِرْ لِمَيِّتِكُمَا", "...وَأَحْسَنَ اللَّهُ عَزَاءَكُمَا"),
        explanation = "تسلية أهل الميت وربط قلوبهم بالصبر والاحتساب ورجاء الثواب."
    ),
    DuaStage(
        25,
        title = "دعاء كفارة المجلس",
        firstHalf = "سُبْحَانَكَ اللَّهُمَّ وَبِحَمْدِكَ، أَشْهَدُ أَنْ لا إِلَهَ إِلا أَنْتَ...",
        correctCompletion = "...أَسْتَغْفِرُكَ وَأَتُوبُ إِلَيْكَ",
        options = listOf("...أَسْتَغْفِرُكَ وَأَتُوبُ إِلَيْكَ", "...وَاغْفِرْ لِي خَطِيئَتِي", "...وَتَقَبَّلْ مِنَّا المَجْلِسَ", "...وَاجْعَلْنَا مِنَ المَقْبُولِينَ"),
        explanation = "يمحو ما كان في المجلس من اللغو والزلل ويختم المجلس بالاستغفار."
    )
)

@Composable
fun DuasChainGame(onBack: () -> Unit) {
    val context = LocalContext.current
    var progressData by remember {
        mutableStateOf(StageProgressStore.loadProgress(context, GAME_NAME, TOTAL_STAGES))
    }
    var selectedStageIdx by remember { mutableStateOf<Int?>(null) }

    if (selectedStageIdx == null) {
        StageMapScreen(
            gameTitle = "سلسلة الأدعية",
            gameSubtitle = "٢٥ مرحلة لإكمال الأذكار والأدعية المأثورة",
            totalStages = TOTAL_STAGES,
            progressData = progressData,
            onSelectStage = { selectedStageIdx = it },
            onBack = onBack
        )
    } else {
        val stageIdx = selectedStageIdx!!
        val currentStage = DUA_STAGES[stageIdx]

        DuasChainPlayScreen(
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
private fun DuasChainPlayScreen(
    stage: DuaStage,
    stageNumber: Int,
    totalStages: Int,
    onBackToMap: () -> Unit,
    onCompleteStage: (Int) -> Unit,
    onNextStage: () -> Unit
) {
    val context = LocalContext.current
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

        val isCorrect = (opt == stage.correctCompletion)
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
                        text = "سلسلة الأدعية",
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
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dua Card
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
                        text = "أكمل الدعاء الشريف التالي:",
                        color = TextSecondaryLinen,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stage.firstHalf,
                        color = TextMain,
                        fontSize = 18.sp,
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
                    val isCorrectOpt = (optionText == stage.correctCompletion)

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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = optionText,
                                color = TextMain,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            if (isAnswered && selectedOption != stage.correctCompletion) {
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .noorGlassCard(cornerRadius = 14.dp)
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "إجابة غير صحيحة، حاول مجدداً واختر التكملة الدقيقة!",
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
                        text = "💡 فضل وحكمة الدعاء:",
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
