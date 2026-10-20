package com.elhajri.noor.games

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
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
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

private const val GAME_NAME = "DeedsBalanceGame"
private const val TOTAL_STAGES = 20

private data class DeedComparison(
    val id: Int,
    val deedA: String,
    val deedB: String,
    val isDeedAGreater: Boolean,
    val evidence: String
)

private val ALL_DEEDS_COMPARISONS = listOf(
    // Stage 1 (Items 1-5)
    DeedComparison(1, "قول: سبحان الله وبحمده 100 مرة", "السكوت وعدم الذكر", true, "تُحَطُّ خَطَايَاهُ وَإِنْ كَانَتْ مِثْلَ زَبَدِ الْبَحْرِ (البخاري)"),
    DeedComparison(2, "صلاة الفجر في جماعة", "قيام نصف الليل فرادى", true, "مَنْ صَلَّى الصُّبْحَ فِي جَمَاعَةٍ فَكَأَنَّمَا صَلَّى اللَّيلَ كُلَّهُ (مسلم)"),
    DeedComparison(3, "قول الكلمات الأربع: سبحان الله والحمد لله ولا إله إلا الله والله أكبر", "الإنفاق بملء الأرض ذهباً", true, "لَأَنْ أَقُولَهَا أَحَبُّ إِلَيَّ مِمَّا طَلَعَتْ عَلَيْهِ الشَّمْسُ (مسلم)"),
    DeedComparison(4, "أداء عمرة في شهر رمضان المبارك", "أداء عمرة في غير رمضان", true, "عُمْرَةً فِي رَمَضَانَ تَقْضِي حَجَّةً مَعِي (البخاري ومسلم)"),
    DeedComparison(5, "صيام يوم عرفة لغير الحاج", "صيام يوم عاشوراء", true, "يُكَفِّرُ السَّنَةَ الْمَاضِيَةَ وَالْبَاقِيَةَ (مسلم)"),

    // Stage 2 (Items 6-10)
    DeedComparison(6, "حسن الخلق واللطف بالناس", "كثرة الصيام والصلاة النافلة مع سوء الخلق", true, "إِنَّ الْمُؤْمِنَ لَيُدْرِكُ بِحُسْنِ خُلُقِهِ دَرَجَةَ الصَّائِمِ الْقَائِمِ (أبو داود)"),
    DeedComparison(7, "صلاة العشاء والفجر في جماعة", "قيام الليل كاملاً بدون صلاة الفجر جماعة", true, "مَنْ صَلَّى العِشَاءَ فِي جَمَاعَةٍ فَكَأَنَّمَا قَامَ نِصْفَ اللَّيلِ... (مسلم)"),
    DeedComparison(8, "الكلمة الطيبة والابتسامة في وجه أخيك", "العبوس والاعتزال مع الصمت", true, "تَبَسُّمُكَ فِي وَجْهِ أَخِيكَ لَكَ صَدَقَةٌ (الترمذي)"),
    DeedComparison(9, "كلمتان خفيفتان: سبحان الله وبحمده سبحان الله العظيم", "التفكير الدنيوي العابر", true, "خَفِيفَتَانِ عَلَى اللِّسَانِ ثَقِيلَتَانِ فِي الْمِيزَانِ (البخاري)"),
    DeedComparison(10, "صلاة ركعتي الفجر (سنة الفجر)", "الدنيا وما فيها من أموال وزينة", true, "رَكْعَتَا الْفَجْرِ خَيْرٌ مِنَ الدُّنْيَا وَمَا فِيهَا (مسلم)"),

    // Stage 3 (Items 11-15)
    DeedComparison(11, "إسباغ الوضوء على المكاره وكثرة الخطى للمساجد", "الجلوس في المنزل انتظاراً للصلاة", true, "أَلا أَدُلُّكُمْ عَلَى مَا يَمْحُو اللَّهُ بِهِ الْخَطَايَا وَيَرْفَعُ بِهِ الدَّرَجَاتِ (مسلم)"),
    DeedComparison(12, "دعاء المسلم لأخيه بظهر الغيب", "التمني بالخير دون دعاء", true, "دَعْوَةُ الْمَرْءِ الْمُسْلِمِ لأَخِيهِ بِظَهْرِ الْغَيْبِ مُسْتَجَابَةٌ (مسلم)"),
    DeedComparison(13, "الصدقة الجارية والعلم النافع", "الصدقة المقطوعة غير الجارية", true, "إِذَا مَاتَ الإِنْسَانُ انْقَطَعَ عَمَلُهُ إِلا مِنْ ثَلاثٍ... (مسلم)"),
    DeedComparison(14, "كفالة اليتيم والإحسان إليه", "تقديم معونة عابرة لمرة واحدة", true, "أَنَا وَكَافِلُ الْيَتِيمِ فِي الْجَنَّةِ هَكَذَا وَأَشَارَ بِالسَّبَّابَةِ وَالْوُسْطَى (البخاري)"),
    DeedComparison(15, "عيادة المريض وزيارته", "السؤال عن صحته بالهاتف فقط", true, "مَنْ عَادَ مَرِيضًا لَمْ يَزَلْ فِي خُرْفَةِ الْجَنَّةِ حَتَّى يَرْجِعَ (مسلم)"),

    // Stage 4 (Items 16-20)
    DeedComparison(16, "الصلاة على النبي ﷺ مرة واحدة", "ترك الصلاة عليه عند ذكر اسمه", true, "مَنْ صَلَّى عَلَيَّ وَاحِدَةً صَلَّى اللَّهُ عَلَيْهِ عَشْرًا (مسلم)"),
    DeedComparison(17, "قراءة سورة الإخلاص ثلاث مرات", "قراءة صفحة واحدة من سور أخرى", true, "قُلْ هُوَ اللَّهُ أَحَدٌ تَعْدِلُ ثُلُثَ الْقُرْآنِ (البخاري)"),
    DeedComparison(18, "صيام ستة أيام من شوال بعد رمضان", "صيام شهر متفرق من السنة", true, "مَنْ صَامَ رَمَضَانَ ثُمَّ أَتْبَعَهُ سِتًّا مِنْ شَوَّالٍ كَانَ كَصِيَامِ الدَّهْرِ (مسلم)"),
    DeedComparison(19, "إماطة الأذى والضرر عن الطريق", "المرور بالأذى دون رفعه", true, "إِمَاطَةُ الأَذَى عَنِ الطَّرِيقِ صَدَقَةٌ (البخاري ومسلم)"),
    DeedComparison(20, "الاستغفار للمؤمنين والمؤمنات أجمعين", "الاستغفار لنفسك فقط", true, "مَنِ اسْتَغْفَرَ لِلْمُؤْمِنِينَ وَالْمُؤْمِنَاتِ كَتَبَ اللَّهُ لَهُ بِكُلِّ مُؤْمِنٍ وَمُؤْمِنَةٍ حَسَنَةً (الطبراني)"),

    // Stage 5 (Items 21-25)
    DeedComparison(21, "صلاة الجمعة بتبكير واغتسال", "التأخر لآخر خطبة الجمعة", true, "مَنِ اغْتَسَلَ يَوْمَ الْجُمُعَةِ وَغَسَّلَ وَبَكَّرَ وَابْتَكَرَ... كَانَ لَهُ بِكُلِّ خُطْوَةٍ عَمَلُ سَنَةٍ (أبو داود)"),
    DeedComparison(22, "سُقيا الماء للظمآن", "تقديم أي طعام آخر", true, "أَفْضَلُ الصَّدَقَةِ سَقْيُ الْمَاءِ (أبو داود)"),
    DeedComparison(23, "الرباط يوماً وليلة في سبيل الله", "صيام شهر وقوامه دون رباط", true, "رِبَاطُ يَوْمٍ وَلَيْلَةٍ خَيْرٌ مِنْ صِيَامِ شَهْرٍ وَقِيَامِهِ (مسلم)"),
    DeedComparison(24, "صلاة الضحى ركعتين", "التنفل العابر في غير وقتها", true, "يُصْبِحُ عَلَى كُلِّ سُلاَمَى مِنْ أَحَدِكُمْ صَدَقَةٌ... وَيُجْزِئُ مِنْ ذَلِكَ رَكْعَتَانِ يَرْكَعُهُمَا مِنَ الضُّحَى (مسلم)"),
    DeedComparison(25, "الإصلاح بين اثنين متخاصمين", "درجة نافلة الصوم والصلاة دون إصلاح", true, "أَلا أُخْبِرُكُمْ بِأَفْضَلَ مِنْ دَرَجَةِ الصِّيَامِ وَالصَّلاةِ وَالصَّدَقَةِ؟ صَلاحُ ذَاتِ الْبَيْنِ (أبو داود)"),

    // Stage 6 (Items 26-30)
    DeedComparison(26, "قراءة أيت الكرسي دبر كل صلاة مكتوبة", "القراءة في أوقات الفراغ العادية", true, "مَنْ قَرَأَ آيَةَ الْكُرْسِيِّ دُبُرَ كُلِّ صَلاةٍ مَكْتُوبَةٍ لَمْ يَمْنَعْهُ مِنْ دُخُولِ الْجَنَّةِ إِلا أَنْ يَمُوتَ (النسائي)"),
    DeedComparison(27, "صلاة أربع ركعات قبل الظهر وأربع بعدها", "ترك السنن الرواتب", true, "مَنْ حَافَظَ عَلَى أَرْبَعِ رَكَعَاتٍ قَبْلَ الظُّهْرِ وَأَرْبَعٍ بَعْدَهَا حَرَّمَهُ اللَّهُ عَلَى النَّارِ (الترمذي)"),
    DeedComparison(28, "عيادة المريض في الصباح (يصلي عليه 70 ألف ملك حتى يمسي)", "مكالمته بالهاتف فقط", true, "مَا مِنْ مُسْلِمٍ يَعُودُ مُسْلِمًا غُدْوَةً إِلا صَلَّى عَلَيْهِ سَبْعُونَ أَلْفَ مَلَكٍ حَتَّى يُمْسِيَ (الترمذي)"),
    DeedComparison(29, "قول: لا إله إلا الله وحده لا شريك له... 100 مرة", "الذكر العابر", true, "كَانَتْ لَهُ عَدْلَ عَشْرِ رِقَابٍ وَكُتِبَتْ لَهُ مِائَةُ حَسَنَةٍ (البخاري)"),
    DeedComparison(30, "صلاة الجنازة واتباعها حتى تدفن (قيراطان)", "حضور الجنازة فقط دون الاتباع للدفن (قيراط واحد)", true, "مَنْ شَهِدَ الْجَنَازَةَ حَتَّى يُصَلَّى عَلَيْهَا فَلَهُ قِيرَاطٌ، وَمَنْ شَهِدَ حَتَّى تُدْفَنَ كَانَ لَهُ قِيرَاطَانِ (البخاري)"),

    // Stage 7 (Items 31-35)
    DeedComparison(31, "صلاة المرأة في بيتها وبصلاتها المكتوبة", "صلاتها في المسجد من غير حرج", true, "وَصَلاتُهَا فِي بَيْتِهَا خَيْرٌ لَهَا (أبو داود)"),
    DeedComparison(32, "الإنفاق على الأهل والعيال باحتساب", "الإنفاق في أوجه الخير الخارجية مع تقصير الأهل", true, "دِينَارٌ أَنْفَقْتَهُ فِي سَبِيلِ اللَّهِ... وَدِينَارٌ أَنْفَقْتَهُ عَلَى أَهْلِكَ أَعْظَمُهَا أَجْرًا الَّذِي أَنْفَقْتَهُ عَلَى أَهْلِكَ (مسلم)"),
    DeedComparison(33, "قول: رضيت بالله رباً وبالإسلام ديناً وبمحمد ﷺ نبياً حين يصبح وحين يمسي", "عدم أذكار الصباح والمساء", true, "كَانَ حَقًّا عَلَى اللَّهِ أَنْ يُرْضِيَهُ يَوْمَ الْقِيَامَةِ (أبو داود)"),
    DeedComparison(34, "صلة الرحم القريبة وإن قطعتك", "صلة من يصلك فقط من الأقارب", true, "لَيْسَ الْوَاصِلُ بِالْمُكَافِئِ، وَلَكِنَّ الْوَاصِلَ الَّذِي إِذَا قُطِعَتْ رَحِمُهُ وَصَلَهَا (البخاري)"),
    DeedComparison(35, "صيام الأيام البيض (13 و 14 و 15 من كل شهر هجري)", "صيام يوم واحد متفرق", true, "صِيَامُ ثَلاثَةِ أَيَّامٍ مِنْ كُلِّ شَهْرٍ صِيَامُ الدَّهْرِ كُلِّهِ (البخاري)"),

    // Stage 8 (Items 36-40)
    DeedComparison(36, "إسباغ الوضوء في الحر والبرد الشديد", "الوضوء السريع المختصر", true, "أَلا أَدُلُّكُمْ عَلَى مَا يَمْحُو اللَّهُ بِهِ الْخَطَايَا... إِسْبَاغُ الْوُضُوءِ عَلَى الْمَكَارِهِ (مسلم)"),
    DeedComparison(37, "كتمان الغيظ والقدرة على الانتقام", "الانتقام والانتصار للنفس", true, "مَنْ كَظَمَ غَيْظًا وَهُوَ قَادِرٌ عَلَى أَنْ يُنْفِذَهُ دَعَاهُ اللَّهُ عَلَى رُؤُوسِ الْخَلائِقِ (الترمذي)"),
    DeedComparison(38, "قراءة الخواتيم من سورة البقرة في ليلة", "ترك قراءتها قبل النوم", true, "مَنْ قَرَأَ بِالآيَتَيْنِ مِنْ آخِرِ سُورَةِ الْبَقَرَةِ فِي لَيْلَةٍ كَفَتَاهُ (البخاري)"),
    DeedComparison(39, "التجاوز عن المعسر وإسقاط الدين عنه", "المطالبة بالدين بالقسوة", true, "مَنْ أَنْظَرَ مُعْسِرًا أَوْ وَضَعَ عَنْهُ أَظَلَّهُ اللَّهُ فِي ظِلِّهِ (مسلم)"),
    DeedComparison(40, "صلاة العشاء في جماعة (قيام نصف ليلة)", "القيام المنفرد وتفويت جماعة العشاء", true, "مَنْ صَلَّى الْعِشَاءَ فِي جَمَاعَةٍ فَكَأَنَّمَا قَامَ نِصْفَ اللَّيْلِ (مسلم)"),

    // Stage 9 (Items 41-45)
    DeedComparison(41, "قيام الليل بسورة تبارك والسور المنجية", "النوم دون قيام ليل", true, "سُورَةٌ مِنَ الْقُرْآنِ ثَلاثُونَ آيَةً شَفَعَتْ لِرَجُلٍ حَتَّى غُفِرَ لَهُ (الترمذي)"),
    DeedComparison(42, "الاستغفار بالأسحار قبل الفجر", "الاستغفار في أوقات النهار العادية", true, "وَبِالأَسْحَارِ هُمْ يَسْتَغْفِرُونَ (القرآن الكريم)"),
    DeedComparison(43, "الذكر بعد صلاة الفجر حتى طلوع الشمس ثم صلاة ركعتين", "الخروج فوراً بعد الفجر للنوم", true, "كَانَتْ لَهُ كَأَجْرِ حَجَّةٍ وَعُمْرَةٍ تَامَّةٍ تَامَّةٍ تَامَّةٍ (الترمذي)"),
    DeedComparison(44, "الصدقة من كسب طيب حلال ولو بتمرة", "الصدقة بالمال الحرام والمكاسب الخبيثة", true, "إِنَّ اللَّهَ طَيِّبٌ لا يَقْبَلُ إِلا طَيِّبًا (مسلم)"),
    DeedComparison(45, "زيارة إخوانك في الله لغير مصلحة دنيوية", "الزيارة لأجل التجارة والمصلحة الخاصة", true, "أَنَّ رَجُلاً زَارَ أَخًا لَهُ فِي قَرْيَةٍ أُخْرَى... فَأَرْسَلَ اللَّهُ عَلَى مَدْرَجَتِهِ مَلَكًا (مسلم)"),

    // Stage 10 (Items 46-50)
    DeedComparison(46, "الحلم والتأني عند اتخاذ القرارات", "العجلة والاندفاع غضباً", true, "إِنَّ فِيَكَ خَصْلَتَيْنِ يُحِبُّهُمَا اللَّهُ: الْحِلْمُ وَالأَنَاةُ (مسلم)"),
    DeedComparison(47, "ستر عورة المسلم وعدم نشر مقاطعه المسيئة", "نشرها وتداولها في برامج التواصل", true, "مَنْ سَتَرَ مُسْلِمًا سَتَرَهُ اللَّهُ فِي الدُّنْيَا وَالآخِرَةِ (مسلم)"),
    DeedComparison(48, "إطعام الطعام وتفشي السلام", "الانعزال عن الناس والبخل بالطعام", true, "أَيُّ الإِسْلامِ خَيْرٌ؟ قَالَ: تُطْعِمُ الطَّعَامَ وَتَقْرَأُ السَّلامَ عَلَى مَنْ عَرَفْتَ وَمَنْ لَمْ تَعْرِفْ (البخاري)"),
    DeedComparison(49, "البكاء من خشية الله في الخلوة", "التباكي رياءً أمام الناس", true, "سَبْعَةٌ يُظِلُّهُمُ اللَّهُ فِي ظِلِّهِ... وَرَجُلٌ ذَكَرَ اللَّهَ خَالِيًا فَفَاضَتْ عَيْنَاهُ (البخاري)"),
    DeedComparison(50, "صلاة 12 ركعة سنة راتبة يومياً", "الاقتصار على الفرائض فقط", true, "مَنْ صَلَّى فِي يَوْمٍ وَلَيْلَةٍ ثِنْتَيْ عَشْرَةَ رَكْعَةً بُنِيَ لَهُ بَيْتٌ فِي الْجَنَّةِ (مسلم)"),

    // Stage 11 (Items 51-55)
    DeedComparison(51, "دعاء دخول السوق: لا إله إلا الله وحده لا شريك له...", "الغفلة في الأسواق والتسوق دون أذكار", true, "كَتَبَ اللَّهُ لَهُ أَلْفَ أَلْفِ حَسَنَةٍ وَمَحَا عَنْهُ أَلْفَ أَلْفِ سَيِّئَةٍ (الترمذي)"),
    DeedComparison(52, "ترديد أذان المؤذن ثم الصلاة على النبي ﷺ وسؤال الوسيلة", "المرور بالأذان دون ترديد", true, "حَلَّتْ لَهُ شَفَاعَتِي يَوْمَ الْقِيَامَةِ (البخاري)"),
    DeedComparison(53, "قراءة سورة الكهف يوم الجمعة", "ترك قراءتها يوم الجمعة", true, "أَضَاءَ لَهُ مِنَ النُّورِ مَا بَيْنَ الْجُمُعَتَيْنِ (الحاكم)"),
    DeedComparison(54, "صيام تسع ذي الحجة لغير الحاج", "ترك صيامها لغير عذر", true, "مَا مِنْ أَيَّامٍ الْعَمَلُ الصَّالِحُ فِيهَا أَحَبُّ إِلَى اللَّهِ مِنْ هَذِهِ الأَيَّامِ (البخاري)"),
    DeedComparison(55, "الإحسان إلى الجار بالهدية والقول الطيب", "إيذاء الجار بالصوت المرتفع أو القمامة", true, "وَاللَّهِ لا يُؤْمِنُ مَنْ لا يَأْمَنُ جَارُهُ بَوَائِقَهُ (البخاري)"),

    // Stage 12 (Items 56-60)
    DeedComparison(56, "شكر الله والتحدث بنعمه تواضعاً", "الكفران بالنعمة الشديدة", true, "وَأَمَّا بِنِعْمَةِ رَبِّكَ فَحَدِّثْ (القرآن الكريم)"),
    DeedComparison(57, "طلب العلم الشرعي النافع", "الاشتغال بالفضول والعلوم الضارة", true, "طَلَبُ الْعِلْمِ فَرِيضَةٌ عَلَى كُلِّ مُسْلِمٍ (ابن ماجه)"),
    DeedComparison(58, "كفالة الأرملة والمسكين وسعي عليهما", "التغافل عن الأرامل والمساكين", true, "السَّاعِي عَلَى الأَرْمَلَةِ وَالْمِسْكِينِ كَالْمُجَاهِدِ فِي سَبِيلِ اللَّهِ (البخاري)"),
    DeedComparison(59, "المحافظة على الوضوء في كل وقت", "الوضوء عند الصلاة فقط", true, "استَقِيمُوا وَلَنْ تُحْصُوا، وَاعْلَمُوا أَنَّ خَيْرَ أَعْمَالِكُمُ الصَّلاةُ وَلا يُحَافِظُ عَلَى الْوُضُوءِ إِلا مُؤْمِنٌ (ابن ماجه)"),
    DeedComparison(60, "الدعاء للوالدين الأحياء والأموات", "نسيانهما والدعاء لنفسك فقط", true, "أَوْ وَلَدٌ صَالِحٌ يَدْعُو لَهُ (مسلم)"),

    // Stage 13 (Items 61-65)
    DeedComparison(61, "المسارعة للتوبة من الذنب فور وقوعه", "الإصرار والتسويف في التوبة", true, "إِنَّ اللَّهَ يُحِبُّ التَّوَّابِينَ وَيُحِبُّ الْمُتَطَهِّرِينَ (القرآن الكريم)"),
    DeedComparison(62, "البدء بالسلام بطلاقة وجه", "انتظار السلام من الآخرين أولاً", true, "أَوْلَى النَّاسِ بِاللَّهِ مَنْ بَدَأَهُمْ بِالسَّلامِ (أبو داود)"),
    DeedComparison(63, "الرفق بالحيوان والدواب وعدم تحميلها ما لا تطيق", "القسوة عليها وضربها", true, "دَخَلَتِ امْرَأَةٌ النَّارَ فِي هِرَّةٍ حَبَسَتْهَا... (البخاري)"),
    DeedComparison(64, "إعادة الأمانات إلى أصحابها كاملة", "الخيانة والمماطلة فيها", true, "إِنَّ اللَّهَ يَأْمُرُكُمْ أَنْ تُؤَدُّوا الأَمَانَاتِ إِلَى أَهْلِهَا (القرآن الكريم)"),
    DeedComparison(65, "الحياء والخجل من المعاصي والآثام", "المجاهرة بالمعصية دون حياء", true, "الْحَيَاءُ لا يَأْتِي إِلا بِخَيْرٍ (البخاري)"),

    // Stage 14 (Items 66-70)
    DeedComparison(66, "صلاة العيدين وشهد الخطبة", "النوم والتفريط في صلاة العيد", true, "سُنَّةٌ مُؤَكَّدَةٌ وَشِعَارٌ إِسْلامِيٌّ عَظِيمٌ"),
    DeedComparison(67, "التسمية عند الأكل والشرب والحمد بعدها", "الأكل دون تسمية ولا حمد", true, "إِذَا أَكَلَ أَحَدُكُمْ فَلْيَذْكُرِ اسْمَ اللَّهِ تَعَالَى (أبو داود)"),
    DeedComparison(68, "السواك عند كل صلاة ووضوء", "ترك السواك والتغافل عنه", true, "السِّوَاكُ مَطْهَرَةٌ لِلْفَمِ مَرْضَاةٌ لِلرَّبِّ (النسائي)"),
    DeedComparison(69, "المشي للصلاة بالسكينة والوقار", "الركض والهرولة المزعجة للصلاة", true, "إِذَا أَتَيْتُمُ الصَّلاةَ فَعَلَيْكُمْ بِالسَّكِينَةِ وَالْوَقَارِ (البخاري)"),
    DeedComparison(70, "الصدقة الخفية التي لا تعلم شماله ما تنفق يمينه", "إعلان الصدقة رياءً ومفاخرة", true, "سَبْعَةٌ يُظِلُّهُمُ اللَّهُ... وَرَجُلٌ تَصَدَّقَ بِصَدَقَةٍ فَأَخْفَاهَا (البخاري)"),

    // Stage 15 (Items 71-75)
    DeedComparison(71, "قول: الحمد لله الذي بنعمته تتم الصالحات عند السراء", "التفاخر بالنفس وتناسي شكر الله", true, "كَانَ النبي ﷺ إِذَا رَأَى مَا يُحِبُّ قَالَ: الْحَمْدُ لِلَّهِ الَّذِي بِنِعْمَتِهِ تَتِمُّ الصَّالِحَاتُ (ابن ماجه)"),
    DeedComparison(72, "تلاوة القرآن بتدبر وتفهم لمعانيه", "القراءة السريعة دون تدبر ولا وعي", true, "كِتَابٌ أَنْزَلْنَاهُ إِلَيْكَ مُبَارَكٌ لِيَدَّبَّرُوا آيَاتِهِ (القرآن الكريم)"),
    DeedComparison(73, "إكرام الضيف بطلاقة الوجه والقرى الطيب", "إظهار الثقل والتضجر من الضيوف", true, "مَنْ كَانَ يُؤْمِنُ بِاللَّهِ وَالْيَوْمِ الآخِرِ فَلْيُكْرِمْ ضَيْفَهُ (البخاري)"),
    DeedComparison(74, "النصح للمسلمين بالسر بأسلوب أخوي", "الفضيحة والتوبيخ على الملأ", true, "الْمُؤْمِنُ يَسْتُرُ وَيَنْصَحُ، وَالْفَاجِرُ يَهْتِكُ وَيُعَيِّرُ (فضيل بن عياض)"),
    DeedComparison(75, "الدعاء عند المصيبة: إنا لله وإنا إليه راجعون اللهم أجرني في مصيبتي وأخلف لي خيراً منها", "الجزع وشق الثياب والسخط", true, "إِلا أَعْقَبَهُ اللَّهُ خَيْرًا مِنْهَا (مسلم)"),

    // Stage 16 (Items 76-80)
    DeedComparison(76, "الوفاء بالنذر والعهود", "إخلاف العهد والتهاون بالنذور", true, "يُوفُونَ بِالنَّذْرِ وَيَخَافُونَ يَوْمًا كَانَ شَرُّهُ مُسْتَطِيرًا (القرآن الكريم)"),
    DeedComparison(77, "ترك المراء والجدال العقيم وإن كنت محقاً", "الإصرار على الجدال والمخاصمة", true, "أَنَا زَعِيمٌ بِبَيْتٍ فِي رَبَضِ الْجَنَّةِ لِمَنْ تَرَكَ الْمِرَاءَ وَإِنْ كَانَ مُحِقًّا (أبو داود)"),
    DeedComparison(78, "حفظ الأيمان وعدم الحلف إلا بالله صدقاً", "الحلف بغير الله أو الحلف الكاذب", true, "مَنْ كَانَ حَالِفًا فَلْيَحْلِفْ بِاللَّهِ أَوْ لِيَصْمُتْ (البخاري)"),
    DeedComparison(79, "صلاة العيد ومشاركة المسلمين فرحتهم", "الاعتزال والتخلف دون عذر", true, "مِنْ شَعَائِرِ الإِسْلامِ الظَّاهِرَةِ"),
    DeedComparison(80, "الاستخارة عند الإقدام على الأمور المهمة", "الاستبداد بالرأي والتسرع دون استخارة", true, "كَانَ رَسُولُ اللَّهِ ﷺ يُعَلِّمُنَا الاسْتِخَارَةَ فِي الأُمُورِ كُلِّهَا (البخاري)"),

    // Stage 17 (Items 81-85)
    DeedComparison(81, "الصبر عند الصدمة الأولى للمصيبة", "التسخط ثم الصبر الاضطراري لاحقاً", true, "إِنَّمَا الصَّبْرُ عِنْدَ الصَّدْمَةِ الأُولَى (البخاري)"),
    DeedComparison(82, "المصادقة والتآخي في الله ولله", "المصادقة لأجل المنافع الدنيوية المؤقتة", true, "الْمَرْءُ عَلَى دِينِ خَلِيلِهِ فَلْيَنْظُرْ أَحَدُكُمْ مَنْ يُخَالِلُ (أبو داود)"),
    DeedComparison(83, "إتقان العمل والمهنة بأمانة", "الغش والتساهل وإضاعة الوقت", true, "إِنَّ اللَّهَ يُحِبُّ إِذَا عَمِلَ أَحَدُكُمْ عَمَلاً أَنْ يُتْقِنَهُ (البيهقي)"),
    DeedComparison(84, "غض البصر عن المحرمات", "إطلاق البصر وتتبع العورات", true, "قُلْ لِلْمُؤْمِنِينَ يَغُضُّوا مِنْ أَبْصَارِهِمْ (القرآن الكريم)"),
    DeedComparison(85, "التواضع للحق وقبوله ممن جاء به", "الكبر والترفع عن قبول الحق", true, "الْكِبْرُ بَطَرُ الْحَقِّ وَغَمْطُ النَّاسِ (مسلم)"),

    // Stage 18 (Items 86-90)
    DeedComparison(86, "المحافظة على صلاة الوتر قبل النوم", "ترك صلاة الوتر دائماً", true, "إِنَّ اللَّهَ وِتْرٌ يُحِبُّ الْوِتْرَ فَأَوْتِرُوا يَا أَهْلَ الْقُرْآنِ (أبو داود)"),
    DeedComparison(87, "بر الوالدين بالدعاء والخدمة بعد وفاتهما", "قطيعتهما في حياتهما وبعد موتهما", true, "الصَّلاةُ عَلَيْهِمَا وَالاِسْتِغْفَارُ لَهُمَا وَإِنْفَاذُ عَهْدِهِمَا (أبو داود)"),
    DeedComparison(88, "الدعاء بـ: اللهم آتنا في الدنيا حسنة وفي الآخرة حسنة وقنا عذاب النار", "الدعاء للدنيا فقط", true, "كَانَ أَكْثَرُ دُعَاءِ النَّبِيِّ ﷺ هَذِهِ الدَّعْوَةَ (البخاري)"),
    DeedComparison(89, "التنفيس عن كرب المسلمين المعسرين", "التضييق عليهم واستغلال حاجتهم", true, "مَنْ نَفَّسَ عَنْ مُؤْمِنٍ كُرْبَةً مِنْ كُرَبِ الدُّنْيَا نَفَّسَ اللَّهُ عَنْهُ كُرْبَةً (مسلم)"),
    DeedComparison(90, "الاستغفار 100 مرة في اليوم والتوبة", "الغفلة عن التوبة اليومية", true, "إِنِّي لأَسْتَغْفِرُ اللَّهَ وَأَتُوبُ إِلَيْهِ فِي الْيَوْمِ أَكْثَرَ مِنْ سَبْعِينَ مَرَّةً (البخاري)"),

    // Stage 19 (Items 91-95)
    DeedComparison(91, "شُكر الناس على إحسانهم ومساعدتهم", "جحود معروفهم وعدم الشكر", true, "مَنْ لا يَشْكُرِ النَّاسَ لا يَشْكُرِ اللَّهَ (الترمذي)"),
    DeedComparison(92, "قراءة الأذكار عقب الصلوات المفروضة", "القيام فوراً والانصراف دون ذكر", true, "أَفْضَلُ الأَعْمَالِ الذِّكْرُ بَعْدَ الصَّلاةِ"),
    DeedComparison(93, "إفشاء السلام بين المسلمين معارف وغير معارف", "السلام على المعارف فقط", true, "تُطْعِمُ الطَّعَامَ وَتَقْرَأُ السَّلامَ عَلَى مَنْ عَرَفْتَ وَمَنْ لَمْ تَعْرِفْ (البخاري)"),
    DeedComparison(94, "العمل اليدوي والكسب الحلال الشريف", "مسألة الناس والاستعطاء مع القدرة", true, "مَا أَكَلَ أَحَدٌ طَعَامًا قَطُّ خَيْرًا مِنْ أَنْ يَأْكُلَ مِنْ عَمَلِ يَدِهِ (البخاري)"),
    DeedComparison(95, "طهارة القلب وسلامة الصدر من الغل والحقد", "حمل الضغينة والأحقاد على المسلمين", true, "يَدْخُلُ عَلَيْكُمُ الآنَ رَجُلٌ مِنْ أَهْلِ الْجَنَّةِ... لا أَجِدُ فِي نَفْسِي لأَحَدٍ مِنَ الْمُسْلِمِينَ غِلاّ (أحمد)"),

    // Stage 20 (Items 96-100)
    DeedComparison(96, "المحافظة على صلاة العصر في وقتها", "تأخيرها حتى تصفر الشمس تفريطاً", true, "مَنْ تَرَكَ صَلاةَ الْعَصْرِ حَبِطَ عَمَلُهُ (البخاري)"),
    DeedComparison(97, "الإنفاق في السراء والضراء", "الإنفاق في السراء والبخل في الضراء", true, "الَّذِينَ يُنْفِقُونَ فِي السَّرَّاءِ وَالضَّرَّاءِ وَالْكَاظِمِينَ الْغَيْظَ (القرآن الكريم)"),
    DeedComparison(98, "المسارعة للخيرات وعدم التسويف", "تأجيل الأعمال الصالحة والغفلة", true, "وَسَارِعُوا إِلَى مَغْفِرَةٍ مِنْ رَبِّكُمْ وَجَنَّةٍ عَرْضُهَا السَّمَاوَاتُ وَالأَرْضُ (القرآن الكريم)"),
    DeedComparison(99, "سؤال الله الجنة والاستعاذة به من النار", "التغافل عن طلب الجنة والنجاة من النار", true, "مَنْ سَأَلَ اللَّهَ الْجَنَّةَ ثَلاثَ مَرَّاتٍ قَالَتِ الْجَنَّةُ: اللَّهُمَّ أَدْخِلْهُ الْجَنَّةَ (الترمذي)"),
    DeedComparison(100, "خُلق الخشوع والافتكار في آيات الله وخلقه", "السهو والتشتت الشديد دون تفكر", true, "الَّذِينَ يَذْكُرُونَ اللَّهَ قِيَامًا وَقُعُودًا وَعَلَى جُنُوبِهِمْ وَيَتَفَكَّرُونَ فِي خَلْقِ السَّمَاوَاتِ وَالأَرْضِ (القرآن الكريم)")
)

@Composable
fun DeedsBalanceGame(onBack: () -> Unit) {
    val context = LocalContext.current
    var progressData by remember {
        mutableStateOf(StageProgressStore.loadProgress(context, GAME_NAME, TOTAL_STAGES))
    }
    var selectedStageIdx by remember { mutableStateOf<Int?>(null) }

    if (selectedStageIdx == null) {
        StageMapScreen(
            gameTitle = "ميزان الحسنات",
            gameSubtitle = "٢٠ مرحلة لموازنة أجور الأعمال ومضاعفاتها",
            totalStages = TOTAL_STAGES,
            progressData = progressData,
            onSelectStage = { selectedStageIdx = it },
            onBack = onBack
        )
    } else {
        val stageIdx = selectedStageIdx!!
        DeedsBalancePlayScreen(
            stageIdx = stageIdx,
            totalStages = TOTAL_STAGES,
            onStageComplete = { stars ->
                StageProgressStore.saveProgress(context, GAME_NAME, stageIdx, stars, TOTAL_STAGES)
                progressData = StageProgressStore.loadProgress(context, GAME_NAME, TOTAL_STAGES)
            },
            onNextStage = {
                if (stageIdx + 1 < TOTAL_STAGES) {
                    selectedStageIdx = stageIdx + 1
                } else {
                    selectedStageIdx = null
                }
            },
            onBackToMap = { selectedStageIdx = null }
        )
    }
}

@Composable
private fun DeedsBalancePlayScreen(
    stageIdx: Int,
    totalStages: Int,
    onStageComplete: (Int) -> Unit,
    onNextStage: () -> Unit,
    onBackToMap: () -> Unit
) {
    val stageNumber = stageIdx + 1
    val stageComparisons = remember(stageIdx) {
        val start = stageIdx * 5
        ALL_DEEDS_COMPARISONS.subList(start, start + 5)
    }

    var roundInStageIdx by remember(stageIdx) { mutableIntStateOf(0) }
    var selectedSide by remember(stageIdx) { mutableStateOf<Boolean?>(null) } // true = Deed A, false = Deed B
    var isAnswered by remember(stageIdx) { mutableStateOf(false) }
    var correctCount by remember(stageIdx) { mutableIntStateOf(0) }
    var showWinDialog by remember(stageIdx) { mutableStateOf(false) }
    var earnedStars by remember(stageIdx) { mutableIntStateOf(3) }
    val haptics = LocalHapticFeedback.current

    val activeRound = stageComparisons[roundInStageIdx]

    fun handleSelect(isDeedA: Boolean) {
        if (isAnswered) return
        selectedSide = isDeedA
        isAnswered = true

        val isCorrect = isDeedA == activeRound.isDeedAGreater
        if (isCorrect) {
            correctCount += 1
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        } else {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    fun nextComparison() {
        if (roundInStageIdx + 1 < 5) {
            roundInStageIdx += 1
            selectedSide = null
            isAnswered = false
        } else {
            val stars = when (correctCount) {
                5 -> 3
                3, 4 -> 2
                else -> 1
            }
            earnedStars = stars
            onStageComplete(stars)
            showWinDialog = true
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
                        text = "ميزان الحسنات",
                        color = Gold,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "المرحلة ${stageNumber.toArabicDigits()} من ${totalStages.toArabicDigits()} (مقارنة ${(roundInStageIdx + 1).toArabicDigits()} / ٥)",
                        color = GoldSoft,
                        fontSize = 12.sp
                    )
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = NavyCard,
                    border = BorderStroke(1.dp, Gold.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Scale,
                            contentDescription = null,
                            tint = Gold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${correctCount.toArabicDigits()} / ٥",
                            color = Gold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Subtitle Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .noorGlassCard(cornerRadius = 14.dp)
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "اختر العمل الأثقل والأعظم أجراً وثواباً في الميزان:",
                    color = TextMain,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Comparison Cards (Deed A vs Deed B)
            DeedCard(
                title = "العمل الأول (أ)",
                deedText = activeRound.deedA,
                isCorrectTarget = activeRound.isDeedAGreater,
                isSelected = selectedSide == true,
                isAnswered = isAnswered,
                onClick = { handleSelect(true) }
            )

            Spacer(modifier = Modifier.height(14.dp))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = Gold,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "أو",
                            color = Navy,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            DeedCard(
                title = "العمل الثاني (ب)",
                deedText = activeRound.deedB,
                isCorrectTarget = !activeRound.isDeedAGreater,
                isSelected = selectedSide == false,
                isAnswered = isAnswered,
                onClick = { handleSelect(false) }
            )

            if (isAnswered) {
                Spacer(modifier = Modifier.height(20.dp))

                // Evidence Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .noorGlassCard(cornerRadius = 16.dp)
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = "الدليل والشاهد الشرعي:",
                            color = Gold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = activeRound.evidence,
                            color = TextSecondaryLinen,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { nextComparison() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Gold),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = if (roundInStageIdx + 1 < 5) "المقارنة التالية" else "إنهاء المرحلة",
                        color = Navy,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (showWinDialog) {
            WinCelebrationOverlay(
                stageNumber = stageNumber,
                totalStages = totalStages,
                stars = earnedStars,
                stageTitle = "المرحلة ${stageNumber.toArabicDigits()}",
                onNextStage = {
                    showWinDialog = false
                    onNextStage()
                },
                onReplayStage = {
                    showWinDialog = false
                    roundInStageIdx = 0
                    selectedSide = null
                    isAnswered = false
                    correctCount = 0
                },
                onStageMap = onBackToMap
            )
        }
    }
}

@Composable
private fun DeedCard(
    title: String,
    deedText: String,
    isCorrectTarget: Boolean,
    isSelected: Boolean,
    isAnswered: Boolean,
    onClick: () -> Unit
) {
    val cardBg = when {
        !isAnswered -> NavyCard
        isCorrectTarget -> Color(0xFF2E7D32)
        isSelected -> Color(0xFFC62828)
        else -> NavyCard.copy(alpha = 0.5f)
    }

    val borderCol = when {
        !isAnswered -> Gold.copy(alpha = 0.3f)
        isCorrectTarget -> Color.Green
        isSelected -> Color.Red
        else -> Color.Transparent
    }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = cardBg,
        border = BorderStroke(1.5.dp, borderCol),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isAnswered, onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = GoldSoft,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                if (isAnswered) {
                    if (isCorrectTarget) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "الأثقل أجراً", color = Color.Green, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color.Green, modifier = Modifier.size(18.dp))
                        }
                    } else if (isSelected) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = Color.Red, modifier = Modifier.size(18.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = deedText,
                color = TextMain,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 20.sp
            )
        }
    }
}
