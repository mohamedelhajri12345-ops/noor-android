package com.elhajri.noor.games

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
import androidx.compose.material.icons.filled.Check
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

private const val GAME_NAME = "FastingRulesGame"
private const val TOTAL_STAGES = 20

private data class FastingRule(
    val id: Int,
    val ruleStatement: String,
    val isTrue: Boolean,
    val evidence: String
)

private data class FastingStage(
    val stageId: Int,
    val title: String,
    val rules: List<FastingRule>
)

private val FASTING_STAGES = listOf(
    FastingStage(
        stageId = 1,
        title = "المرحلة ١: المفطرات والنواقص الأساسية",
        rules = listOf(
            FastingRule(1, "الأكل والشرب نسياناً في نهار رمضان لا يبطل الصيام ولا يوجب القضاء.", true, "قال النبي ﷺ: «من نسي وهو صائم فأكل أو شرب فليتم صومه، فإنما أطعمه الله وسقاه»."),
            FastingRule(2, "القيء العمد في نهار رمضان يفسد الصوم ويوجب القضاء.", true, "قال النبي ﷺ: «من ذرعه القيء فلا قضاء عليه، ومن استقاء عمداً فليقضِ»."),
            FastingRule(3, "ابتلاع الريق الطاهر المعتاد يفسد الصيام ويوجب القضاء.", false, "ابتلاع الريق طاهر طبيعي مما يشق الاحتراز منه ولا يبطل الصيام باتفاق الفقهاء."),
            FastingRule(4, "الجماع في نهار رمضان من أعظم المفطرات ويوجب القضاء والكفارة المغلظة.", true, "الكفارة المغلظة هي عتق رقبة أو صيام شهرين متتابعين أو إطعام ستين مسكيناً."),
            FastingRule(5, "نزول دم الحيض والنفاس يبطل الصوم فوراً ولو قبل غروب الشمس بلحظات.", true, "يبطل الصوم فور رؤية الدم وتفطر المرأة ويجب عليها قضاء اليوم لاحقاً."),
            FastingRule(6, "حقن التغذية المغذية في الوريد التي تقوم مقام الطعام والشراب لا تفطر الصائم.", false, "الحقن المغذية المشتملة على السوائل والمغذيات تفطر لأنها تشارك الطعام معنوياً.")
        )
    ),
    FastingStage(
        stageId = 2,
        title = "المرحلة ٢: قطرات ومستحضرات العلاج",
        rules = listOf(
            FastingRule(7, "استعمال قطرة العين أو الأذن غير المبطلة للصوم عند عامة الفقهاء لأن العين ليست منفذاً جوفياً.", true, "قطرة العين لا تعد طعاماً ولا شراباً ولا تصل إلى الجوف كمنفذ معتاد فلا يفسد بها الصوم."),
            FastingRule(8, "استعمال معجون الأسنان في نهار رمضان يبطل الصيام فوراً حتى لو لم ينزل منه شيء للحلق.", false, "لا يبطل الصيام بمجرد تنظيف الأسنان بالمعجون ما لم يبتلع منه الصائم شيئاً متعمداً إلى جوفه."),
            FastingRule(9, "قطرة الأنف إذا وصلت للحلق وتجاوزت الخياشيم تعد مفطرة للحديث الشريف.", true, "قال النبي ﷺ: «وبالغ في الاستنشاق إلا أن تكون صائماً» لئلا ينفذ الماء إلى الحلق."),
            FastingRule(10, "بخاخ الربو (الفنتولين) لمرضى الضيق التنفسي يفسد الصوم حتماً.", false, "بخاخ الربو رذاذ يتجه للرئتين لفتح المجاري التنفسية ولا ينزل للمعدة كطعام."),
            FastingRule(11, "استعمال الإبر العلاجية العضلية والوريدية غير المغذية لا يفطر الصائم.", true, "الإبر العلاجية غير المغذية لا تقوم مقام الأكل والشراب."),
            FastingRule(12, "أخذ العينات لسحب الدم للتحليل الطبي يبطل الصيام بالكلية.", false, "سحب كمية يسيرة من الدم للتحليل لا يؤثر على صحة الصوم ولا يفطر.")
        )
    ),
    FastingStage(
        stageId = 3,
        title = "المرحلة ٣: الطهارة والغسل ونية الصوم",
        rules = listOf(
            FastingRule(13, "نية صيام الفريضة تجب تبييتها من الليل قبل طلوع الفجر.", true, "قال النبي ﷺ: «من لم يجمع الصيام قبل الفجر فلا صيام له»."),
            FastingRule(14, "تأخير غسل الجنابة إلى ما بعد طلوع الفجر يؤثر على صحة صوم ذلك اليوم.", false, "كان النبي ﷺ يصبح جنباً من غير احتلام ثم يغتسل ويتم صومه صحيحاً."),
            FastingRule(15, "الاغتسال والتبرد بالماء البارد للصائم جائز ولا يفسد الصيام.", true, "كان النبي ﷺ يصب على رأسه الماء وهو صائم من شدة الحر أو العطش."),
            FastingRule(16, "احتلام الصائم أثناء نومه في نهار رمضان يفسد صومه ويوجب عليه القضاء.", false, "الاحتلام أمر خارج عن إرادة النائم ولا يفسد الصيام باتفاق علماء المسلمين."),
            FastingRule(17, "المضمضة والاستنشاق في الوضوء ممنوعة تماماً على الصائم.", false, "المضمضة والاستنشاق من سنن الوضوء للصائم مع النهي عن المبالغة فقط."),
            FastingRule(18, "السواك مستحب ومسنون للصائم في كل وقت طوال نهار رمضان.", true, "السواك مطهرة للفم مرضاة للرب وهو مسنون للصائم في أول النهار وآخره.")
        )
    ),
    FastingStage(
        stageId = 4,
        title = "المرحلة ٤: سنن وآداب الصيام",
        rules = listOf(
            FastingRule(19, "تأخير السحور وتعجيل الفطر عند غروب الشمس من السنن المؤكدة في الصيام.", true, "قال النبي ﷺ: «لا يزال الناس بخير ما عجّلوا الفطر وأخّروا السحور»."),
            FastingRule(20, "السحور بركة ومستحب في الصيام ولو بجرعة ماء.", true, "قال النبي ﷺ: «تسحروا فإن في السحور بركة»."),
            FastingRule(21, "يُستحب الإفطار على رطبات أو تمرات أو ماء قبل صلاة المغرب.", true, "كان النبي ﷺ يفطر على رطبات فإن لم يجد فتمرات فإن لم يجد حسا حسوات من ماء."),
            FastingRule(22, "الدعاء عند الإفطار مستجاب ولا يُرد للصائم.", true, "قال النبي ﷺ: «ثلاث لا ترد دعوتهم: الصائم حتى يفطر...»."),
            FastingRule(23, "ترك الفحش والغيبة واللغو من كمال الصيام الشريف.", true, "قال النبي ﷺ: «من لم يدع قول الزور والعمل به فليس لله حاجة في أن يدع طعامه وشرابه»."),
            FastingRule(24, "إذا سابّ الصائمَ أحدٌ أو قاتله فليقل: إني صائم.", true, "صيانة للصوم وتخلقاً بأخلاق القرآن والهدى النبوي.")
        )
    ),
    FastingStage(
        stageId = 5,
        title = "المرحلة ٥: الأعذار والرخص الشرعية",
        rules = listOf(
            FastingRule(25, "المريض الذي يرجى برؤه يجوز له الفطر ويلزمه القضاء بعد شفائه.", true, "قال تعالى: ﴿فَمَنْ كَانَ مِنْكُمْ مَرِيضًا أَوْ عَلَى سَفَرٍ فَعِدَّةٌ مِنْ أَيَّامٍ أُخَرَ﴾."),
            FastingRule(26, "المسافر سفراً مباحاً يبلغ مسافة القصر يجوز له الفطر والقضاء.", true, "الرخصة ثابتة بنص القرآن الكريم والسنة النبوية الشريفة."),
            FastingRule(27, "الحامل والمرضع إذا خافتا على صحتهما أو جنينيهما جاز لهما الفطر والقضاء.", true, "رخص الشارع للحامل والمرضع بالفطر عند الخوف والقضاء لاحقاً."),
            FastingRule(28, "الكبير العاجز عن الصوم عجزاً مستمراً يلزمه الصوم ولو كلفه مشقة شديدة.", false, "الكبير العاجز يطعم عن كل يوم مسكيناً ولا يكلف الصيام."),
            FastingRule(29, "المريض مرضاً مزامناً لا يرجى شفاؤه يلزمه القضاء بالصوم لاحقاً.", false, "المريض مرضاً لا يرجى شفاؤه يفطر ويطعم عن كل يوم مسكيناً ولا صوم عليه."),
            FastingRule(30, "من أغمي عليه طوال النهار من الفجر إلى المغرب لا يصح صومه ويلزمه القضاء.", true, "يشترط الإدراك في جزء من النهار على الأقل لصحة الصوم.")
        )
    ),
    FastingStage(
        stageId = 6,
        title = "المرحلة ٦: تذوق الطعام والروائح والطهارة",
        rules = listOf(
            FastingRule(31, "تذوق الطعام بطرف اللسان للحاجة ومَجِّه دون ابتلاعه يُبطل الصيام.", false, "يجوز تذوق الطعام للحاجة كضبط الملح بشرط مجّه وعدم ابتلاع أي جزء منه."),
            FastingRule(32, "شم البخور وقصده باستنشاقه عمداً حتى يدخل الجوف يبطل الصوم عند العلماء.", true, "دخان البخور ذو جرم ويتكيف به الجوف فيكره استنشاقه عمداً للصائم."),
            FastingRule(33, "استعمال العطور والطيب والروائح الزكية لا يفسد الصيام.", true, "الطيب والروائح الزكية لا تدخل الجوف كطعام ولا تبطل الصيام."),
            FastingRule(34, "شج الرأس أو جرح البدن الخارجي لا يفطر الصائم.", true, "الجروح والدم النازل من غير العروق الرئيسية لا يفسد الصوم."),
            FastingRule(35, "الحجامة في نهار رمضان تبطل الصوم حتماً عند الجمهور.", false, "جمهور الأئمة (مالك والشافعي وأبو حنيفة) على أن الحجامة لا تفطر الصائم ورُخص فيها."),
            FastingRule(36, "الرعاف ودم الأنف الصادر بغير اختيار لا يبطل الصوم.", true, "الدم الخارج بغير اختيار الصائم كالرعاف لا يؤثر على صحة الصوم.")
        )
    ),
    FastingStage(
        stageId = 7,
        title = "المرحلة ٧: أحكام قضاء رمضان",
        rules = listOf(
            FastingRule(37, "يجب التتابع في قضاء أيام رمضان ولا يجوز تفريقها.", false, "يجوز تفريق أيام القضاء أو متابعتها بحسب استطاعة المسلم."),
            FastingRule(38, "يمتد وقت قضاء رمضان إلى رمضان القادم.", true, "كانت عائشة رضي الله عنها تقضي ما عليها من رمضان في شعبان."),
            FastingRule(39, "من أخر قضاء رمضان حتى دخل رمضان الآخر بلا عذر لزمه القضاء وإطعام مسكين عن كل يوم.", true, "افتات وتأخير بلا عذر يوجب القضاء والإطعام عند كثير من الصحابة والفقهاء."),
            FastingRule(40, "من مات وعليه صيام فرض صام عنه وليه.", true, "قال النبي ﷺ: «من مات وعليه صيام صام عنه وليه»."),
            FastingRule(41, "يجوز نفل التطوع قبل صيام قضاء رمضان لمن اتسع وقته.", true, "القضاء موسع والتطوع جائز خاصة في الأيام الفاضلة كعشر ذي الحجة."),
            FastingRule(42, "لا يجوز صيام يوم الشك (30 شعبان) بنية الاحتياط لرمضان.", true, "قال النبي ﷺ: «من صام اليوم الذي يشك فيه فقد عصى أبا القاسم ﷺ».")
        )
    ),
    FastingStage(
        stageId = 8,
        title = "المرحلة ٨: صيام التطوع والأيام الفاضلة",
        rules = listOf(
            FastingRule(43, "صيام ستة أيام من شوال بعد رمضان يعادل صيام الدهر.", true, "قال النبي ﷺ: «من صام رمضان ثم أتبعه ستاً من شوال كان كصيام الدهر»."),
            FastingRule(44, "صيام يوم عرفة يكفر السنة الماضية والباقية لغير الحاج.", true, "صيام يوم عرفة من أفضل صيام التطوع ويختص بغير الحاج في عرفات."),
            FastingRule(45, "صيام يوم عاشوراء يكفر السنة الماضية.", true, "أخبر النبي ﷺ أن احتساب صيام عاشوراء عند الله يكفر السنة التي قبله."),
            FastingRule(46, "أفضل الصيام بعد رمضان صيام شهر الله المحرم.", true, "قال النبي ﷺ: «أفضل الصيام بعد رمضان شهر الله المحرم»."),
            FastingRule(47, "يُسن صيام الاثنين والخميس من كل أسبوع.", true, "تُعرض الأعمال على الله يومي الاثنين والخميس وكان النبي ﷺ يحب أن يُعرض عمله وهو صائم."),
            FastingRule(48, "صيام ثلاثة أيام من كل شهر (الأيام البيض 13 و14 و15) صيام الدهر.", true, "الحسنة بعشر أمثالها فثلاثة أيام من كل شهر تعدل صيام الشهر كاملاً.")
        )
    ),
    FastingStage(
        stageId = 9,
        title = "المرحلة ٩: الأوقات والمشاهد المحرمة في الصوم",
        rules = listOf(
            FastingRule(49, "يُحرم صيام يومي عيد الفطر وعيد الأضحى.", true, "نهى النبي ﷺ عن صيام هذين اليومين حسمًا ومشاركة للفرح والضيافة الإلهية."),
            FastingRule(50, "يُحرم صيام أيام التشريق الثلاثة (11 و12 و13 ذي الحجة) إلا للحاج المتمتع المقرن الذي لم يجد الهدي.", true, "أيام التشريق أيام أكل وشرب وذكر لله تعالى."),
            FastingRule(51, "إفراد يوم الجمعة بالصوم مكروه أو محرم إلا أن يصوم يوماً قبله أو يوماً بعده.", true, "نهى النبي ﷺ عن تخصيص يوم الجمعة بصيام دون غيره من الأيام."),
            FastingRule(52, "صيام صوم الدائم (صيام الدهر كله بدون إفطار) مسنون ومستحب.", false, "قال النبي ﷺ: «لا صام من صام الأبد» ونهى عن صيام الدهر دائماً."),
            FastingRule(53, "صوم الوصال (وصل يومين بالصوم دون إفطار بينهما) نهى عنه النبي ﷺ.", true, "نهى النبي ﷺ عن الوصال رحمة بالأمة وشفقة عليهم."),
            FastingRule(54, "يجوز نية صوم التطوع في النهار قبل الزوال لمن لم يطعم شيئاً.", true, "ثبت عن النبي ﷺ أنه دخل على عائشة فقال: «هل عندكم شيء؟» قالوا: لا، قال: «فإني إذن صائم».")
        )
    ),
    FastingStage(
        stageId = 10,
        title = "المرحلة ١٠: الاعتكاف وليلة القدر",
        rules = listOf(
            FastingRule(55, "الاعتكاف لزوم المسجد لطاعة الله في العشر الأواخر من رمضان لزوم سنة مؤكدة.", true, "كان النبي ﷺ يعتكف العشر الأواخر من رمضان حتى توفاه الله."),
            FastingRule(56, "يشترط في الاعتكاف الشرعي أن يكون داخل المسجد.", true, "قال تعالى: ﴿وَأَنْتُمْ عَاكِفُونَ فِي الْمَسَاجِدِ﴾."),
            FastingRule(57, "الخروج من المسجد لغير حاجة ضرورية يبطل الاعتكاف.", true, "الخروج لغير الأكل والشرب أو قضاء الحاجة ينقض الاعتكاف."),
            FastingRule(58, "تحري ليلة القدر يكون في الأوتار من العشر الأواخر من رمضان.", true, "قال النبي ﷺ: «تحروا ليلة القدر في الوتر من العشر الأواخر من رمضان»."),
            FastingRule(59, "قيام ليلة القدر إيماناً واحتساباً يغفر ما تقدم من الذنوب.", true, "قال النبي ﷺ: «من قام ليلة القدر إيماناً واحتساباً غُفر له ما تقدم من ذنبه»."),
            FastingRule(60, "الدعاء المأثور في ليلة القدر: (اللَّهُمَّ إِنَّكَ عَفُوٌّ تُحِبُّ الْعَفْوَ فَاعْفُ عَنِّي).", true, "علم النبي ﷺ أم المؤمنين عائشة رضي الله عنها هذا الدعاء المبارك.")
        )
    ),
    FastingStage(
        stageId = 11,
        title = "المرحلة ١١: مفطرات العصر والتطبيقات الحديثة",
        rules = listOf(
            FastingRule(61, "استنشاق الغازات المخدرة في العمليات الجراحية التي تستوعب كل النهار يبطل الصوم.", true, "فقدان التمييز والإدراك طوال النهار يبطل الصوم ويلزم القضاء."),
            FastingRule(62, "غسيل الكلية الاصطناعي للمريض يفسد الصوم ويوجب القضاء.", true, "غسيل الكلية يتضمن تزويد الدم بالسوائل والمغذيات كالجلوكوز."),
            FastingRule(63, "منظار المعدة الذي لا يرافقه إدخال سوائل أو مواد مغذية لا يفطر الصائم عند جمهور المعاصرين.", true, "دخول جسم جاف للمعدة لا يعد طعاماً ولا شراباً."),
            FastingRule(64, "استعمال الأوكسجين الخالص للمريض عبر القناع لا يفطر الصائم.", true, "الأوكسجين هواء ونفس ضروري للحياة ولا يعد أطلاقا طعاماً أو شراباً."),
            FastingRule(65, "الأقراص الدوائية التي توضع تحت اللسان لعلاج أزمات القلب وتتمتص دون ابتلاع لا تفطر.", true, "امتصاص المادة الدوائية عبر الأوعية تحت اللسان دون نزولها للحلق لا يفطر."),
            FastingRule(66, "معجون معالجة اللثة الموضعي الذي يستعمل ولا يبتلع لا يفسد الصوم.", true, "العلاج الموضعي للفم والأسنان لا يضر الصوم ما لم يبتلع الجوف منه شيئاً.")
        )
    ),
    FastingStage(
        stageId = 12,
        title = "المرحلة ١٢: المسافر وأحكام السفر في رمضان",
        rules = listOf(
            FastingRule(67, "المسافر إذا أنشأ سفره في النهار بعد الفجر لا يجوز له الفطر إلا إذا جاوز بيوت مدينته.", true, "يشترط لمباشرة رخصة السفر مفارقة البنيان والمدار السكني."),
            FastingRule(68, "إذا شق الصوم على المسافر مشقة شديدة فالأفضل في حقه الفطر.", true, "قال النبي ﷺ لما رأى زحاماً ورجلاً قد ظُلل عليه: «ليس من البر الصيام في السفر»."),
            FastingRule(69, "إذا لم يجد المسافر أي مشقة وكان الصوم أسهل له فالصوم في حقه أفضل.", true, "صام النبي ﷺ في السفر واختاره الأئمة عند عدم المشقة مراعاة لشرف الوقت."),
            FastingRule(70, "سائقو الشاحنات وسيارات الأجرة المستمرون في السفر يجوز لهم الفطر والقضاء في أوقات الراحة.", true, "عموم رخصة السفر شاملة لكل من انطبقت عليه الشروط مسافةً وقصداً."),
            FastingRule(71, "إذا قدم المسافر مفطراً إلى مدينته في نهار رمضان كُره له التظاهر بالأكل مراعاة لحرمة الشهر.", true, "احترام حرمة الوقت والشهر وتجنب إثارة الشبهات بين الناس."),
            FastingRule(72, "تبييت نية الفطر للمسافر في الليل شرط لاستعمال الرخصة من أول النهار.", true, "النية تميز العبادة واستعمال الرخصة الشرعية.")
        )
    ),
    FastingStage(
        stageId = 13,
        title = "المرحلة ١٣: صوم الصبيان والشباب",
        rules = listOf(
            FastingRule(73, "الصبي غير البالغ لا يجب عليه الصوم شرعاً لكن يُؤمر به ليعتاده إذا أطاقه.", true, "كان الصحابة يُصومون صبيانهم ويجعلون لهم اللعبة من العهن ليتلهوا بها."),
            FastingRule(74, "علامات التكليف ووجوب الصوم هي البلوغ بإحدى العلامات الشرعية (كإكمال 15 سنة أو الاحتلام).", true, "البلوغ شرط للوجوب وتكليف العبادات الشرعية."),
            FastingRule(75, "إذا بلغ الصبي أو أسلم الكافر في أثناء نهار رمضان لزمه الإمساك بقية اليوم.", true, "إمساك حرمة للوقت واستجابة لسبب الوجوب عند طروء التكليف."),
            FastingRule(76, "صوم الصبي المميز صحيح ويثاب عليه ووالداه.", true, "تكتب له الحسنات وتدخر لوالديه في ميزانهما."),
            FastingRule(77, "تأذى الصبي الصغير بالصوم أذى شديداً يُمنع منه رفقاً ببدنه.", true, "رفع الحرج والمحافظة على صحة الأبدان أصل في الشريعة."),
            FastingRule(78, "ليس في صيام النصف من النهار للصغار (صوم العصافير) بطلان لتربيتهم التدريجية.", true, "التدرج في تدريب الصبيان على الصيام وسيلة تربوية محمودة.")
        )
    ),
    FastingStage(
        stageId = 14,
        title = "المرحلة ١٤: النية وتعدد نيات الصوم",
        rules = listOf(
            FastingRule(79, "تكفي نية واحدة لجميع أيام شهر رمضان في أول ليلة عند بعض المالكية ما لم يقطعها بسفر أو مرض.", true, "لأن شهر رمضان عبادة واحدة متصلة والأحوط تجديدها كل ليلة."),
            FastingRule(80, "يجوز الجمع بين نية صيام القضاء ونية صيام الست من شوال في نية واحدة كاملة الأجر.", false, "المقصود بصيام شوال إتباع صيام رمضان الكامل بالست، فالأفضل إفراد القضاء ثم صيام الست."),
            FastingRule(81, "من نوى قطع الصوم وتردّد في نهار رمضان بطل صومه عند جمع من الفقهاء.", true, "لأن النية جازمة في العبادة والتردد ينفي الجزم بها."),
            FastingRule(82, "تعيين النية للصوم الواجب (كرمضان والنذر والقضاء) شرط لا بد منه.", true, "النية تميز فروض الصيام عن بعضها وعن النفل."),
            FastingRule(83, "التلفظ بالنية باللسان جشراً واجب في الصلاة والصوم.", false, "النية محلها القلب والتلفظ بها بدعة لم تثبت عن النبي ﷺ ولا صحابته."),
            FastingRule(84, "من قام للتسحر في الليل يعد ذلك نية جازمة للصيام.", true, "القيام للسحور والأكل بنية الصوم هو عين النية القطبية في القلب.")
        )
    ),
    FastingStage(
        stageId = 15,
        title = "المرحلة ١٥: الفدية والكفارات في الصيام",
        rules = listOf(
            FastingRule(85, "مقدار الفدية عن كل يوم عجز عنه المسلم إطعام مسكين مد من طعام (حوالي نصف صاع / 1.5 كغم).", true, "إطعام مسكين وجبة مشبعة أو نصف صاع من قوت البلد."),
            FastingRule(86, "يجوز دفع الفدية مالاً ونقداً فوراً للفقراء عند بعض الأئمة كأبي حنيفة للحاجة.", true, "تيسيراً على الفقراء وقضاءً لحوائجه الضرورية."),
            FastingRule(87, "من جامع في نهار رمضان معذوراً بنسيان لا تجب عليه الكفارة المغلظة عند جمهور الفقهاء.", true, "النسيان يرفع الإثم والتكليف والكفارة تجب على التعمد."),
            FastingRule(88, "كفارة الجماع مرتبة: عتق رقبة، فإن لم يجد فصيام شهرين متتابعين، فإن لم يستطع فإطعام 60 مسكيناً.", true, "الترتيب جاء صريحاً في حديث الأعرابي في صحيح البخاري ومسلم."),
            FastingRule(89, "تكرار الجماع في اليوم الواحد من رمضان يوجب كفارات متعددة لكل جماع كفارة مستقلة.", false, "إذا كان في يوم واحد ولم يكفر عن الأول تكفيه كفارة واحدة عن اليوم عند جمع من الفقهاء."),
            FastingRule(90, "تجب الفدية على من أخر قضاء رمضان حتى مات قبل استطاعته القضاء.", false, "من مات قبل استطاعة القضاء لا شيء عليه ولا فدية لعدم فرط تقصيره.")
        )
    ),
    FastingStage(
        stageId = 16,
        title = "المرحلة ١٦: المفطرات المعنوية وأثر الذنوب",
        rules = listOf(
            FastingRule(91, "المعاصي كالغيبة والنميمة تفطر الصائم وتوجب عليه القضاء عند الأئمة الأربعة.", false, "المعاصي تجرح أجر الصوم وتنقص ثوابه لكنها لا تبطل أصله ولا توجب القضاء عند الجمهور."),
            FastingRule(92, "النظرة المحرمة بشهوة بغير خروج مني تنقص الأجر ولا تبطل الصوم.", true, "المحرمات تنقص ثواب العبادة وتنافي تقوى الصوم."),
            FastingRule(93, "الاستماع إلى الغناء والملاهي المحرمة يفسد الصيام في الظاهر.", false, "لا يفسد الصوم بالمعاصي السمعية لكن يبخس صاحبه ثوابه الأوفر."),
            FastingRule(94, "الصوم المقبول هو ما جمع بين إمساك البطن والفرج وإمساك الجوارح عن الحرام.", true, "قال جابر رضي الله عنه: (إذا صمت فليصم سمعك وبصرك ولسانك عن الكذب والماثم)."),
            FastingRule(95, "الشهادة الزور في نهار رمضان تحبط أجر الصيام بالكلية.", true, "قال النبي ﷺ: «من لم يدع قول الزور والعمل به فليس لله حاجة في أن يدع طعامه وشرابه»."),
            FastingRule(96, "الغضب الشديد والشتم ينقص ثواب الصائم فيجب عليه ضبط النفس.", true, "الصوم جُنة ووقاية وحصن للأخلاق والهدوء.")
        )
    ),
    FastingStage(
        stageId = 17,
        title = "المرحلة ١٧: المضمضة والاستنشاق والغبار",
        rules = listOf(
            FastingRule(97, "دخول الغبار أو دقيق الطحين أو تطاير البخار إلى الجوف بغير اختيار لا يفطر.", true, "لأنه مما يعسر ويشق الاحتراز منه في معيشة الناس."),
            FastingRule(98, "ابتلاع ما بين الأسنان من بقايا الطعام اليسيرة التي تعجز عن مجّها لا يفطر.", true, "الشيء اليسير المستهلك مع الريق لا يضر الصوم."),
            FastingRule(99, "إذا ابتلع الصائم ما بين أسنانه وكان كبيراً يستطيع قذفه عمداً بطل صومه.", true, "لأنه بمثابة أكل طعام جديد متطوع بابتلاعه."),
            FastingRule(100, "دخول الذباب أو البعوض للحلق بغير اختيار الصائم لا يفسد الصوم.", true, "لأنه طارئ بغير قصد ولا إرادة فلا يضر."),
            FastingRule(101, "تذوق معجون الطبخ ومجّه للحاجة مكروه بغير سبب ومباح عند الحاجة.", true, "يجوز عند الحاجة مع الحذر الشديد من ابتلاع شيء منه."),
            FastingRule(102, "البالوع وابتلاع النخامة من الحلق يفضل قذفها وعدم ابتلاعها إذا وصلت للفم.", true, "خروجاً من الخلاف وإبقاءً للعبادة على طهارتها وكمالها.")
        )
    ),
    FastingStage(
        stageId = 18,
        title = "المرحلة ١٨: أحكام الإفطار ورؤية الهلال",
        rules = listOf(
            FastingRule(103, "يثبت دخول شهر رمضان برؤية هلاله أو بإكمال شعبان ثلاثين يوماً.", true, "قال النبي ﷺ: «صوموا لرؤيته وأفطروا لرؤيته، فإن غُم عليكم فأكملوا عدة شعبان ثلاثين»."),
            FastingRule(104, "شهادة رجل واحد عدل تكفي لإثبات دخول شهر رمضان.", true, "قبل النبي ﷺ شهادة أعرابي واحد برؤية الهلال وأمر الناس بالصيام."),
            FastingRule(105, "لا بد من شهادة رجلين عدلين لإثبات خروج رمضان ودخول شهر شوال.", true, "احتياطاً للعبادة وعدم الاستعجال في الفطر."),
            FastingRule(106, "من أفطر يظن أن الشمس قد غربت وتبين أنها لم تغرب يلزمه القضاء عند جمهور العلماء.", true, "لأن الأصل بقاء النهار حتى يتيقن غروب الشمس."),
            FastingRule(107, "من أكل يظن أن الفجر لم يطلع وتبين طلوعه يلزمه القضاء عند أكثر أهل العلم.", true, "لأن الواجب التثبت من طلوع الفجر الصادق."),
            FastingRule(108, "اعتماد الحساب الفلكي كبديل كامل عن الرؤية الشرعية بالعين أو المناظير محل منع عند جمهور المجامع الفقهية.", true, "لأن الشريعة علقت الحكم بالرؤية البصرية أو إكمال العدة.")
        )
    ),
    FastingStage(
        stageId = 19,
        title = "المرحلة ١٩: الصوم في المسافات وأحكام الطائرة",
        rules = listOf(
            FastingRule(109, "الصائم على متن الطائرة يفطر عند غروب الشمس برؤيتها في الأفق في مستواه الجوي لا بتوقيت الأرض أسفله.", true, "العبرة برؤية غروب قرص الشمس بالعين للركاب بالجو."),
            FastingRule(110, "إذا سافر الصائم إلى بلد يختلف غروبه عن بلده أتم صومه بحسب غروب البلد الذي هو فيه.", true, "الصائم يتبع زمان ومكان وجوده الحالي."),
            FastingRule(111, "إذا انتقل الصائم لبلد صاموا 31 يوماً يصوم معهم احتفاظاً بوحدة المسلمين.", true, "الصوم يوم تصومون والفطر يوم تفطرون."),
            FastingRule(112, "إذا انتقل لبلد صاموا 28 يوماً فقط وأفطروا لزمه الفطر معهم وقضاء يوم واحد لاحقاً.", true, "لأن الشهر لا ينقص عن 29 يوماً في الشريعة."),
            FastingRule(113, "الصلاة والصوم يتبعان التوقيت المحلي للموقع الذي يتواجد فيه المسلم.", true, "ضوابط الشرع مرتبطة بالطلوع والغروب المشاهد بقعة بقعة."),
            FastingRule(114, "من كان في مناطق القطب حيث يستمر النهار أشهراً يقدر وقت صومه بأقرب بلد معتدل.", true, "تقدير الأوقات عند اضطراب العلامات الطبيعية أصل فقهي ثابت.")
        )
    ),
    FastingStage(
        stageId = 20,
        title = "المرحلة ٢٠: التحدي الختامي والأحكام الجامِعة",
        rules = listOf(
            FastingRule(115, "الصوم ركن من أركان الإسلام الخمسة التي بُني عليها الدين.", true, "قال النبي ﷺ: «بُني الإسلام على خمس... وصوم رمضان»."),
            FastingRule(116, "من أنكر وجوب صيام رمضان كفر وخرج من الملة لإجمال الأمة وعلم الدين بالضرورة.", true, "جحد فرائض الإسلام المعلومة بالضرورة مخرج من الملة."),
            FastingRule(117, "من أفطر يوماً في رمضان متعمداً بلا عذر فقد أتى كبيرة عظيمة من الكبائر.", true, "انتهاك حرمة الشهر الحرام متعمداً جرم كبير تجب منه التوبة النصوح."),
            FastingRule(118, "الصوم خالص لله تعالى وهو يجزي به جيل الأجر بلا حساب.", true, "في الحديث القدسي: «كل عمل ابن آدم له إلا الصوم فإنه لي وأنا أجزي به»."),
            FastingRule(119, "للصائم فرحتان: فرحة عند فطره، وفرحة عند لقاء ربه.", true, "فرحة الفطر بنعمة التمام، وفرحة اللقاء بعظيم الثواب والرضوان."),
            FastingRule(120, "خلوف فم الصائم أطيب عند الله من ريح المسك يوم القيامة.", true, "أثر العبادة الطاهرة مقرب ومحبوب عند الله عز وجل.")
        )
    )
)

@Composable
fun FastingRulesGame(onBack: () -> Unit) {
    val context = LocalContext.current
    var progressData by remember {
        mutableStateOf(StageProgressStore.loadProgress(context, GAME_NAME, TOTAL_STAGES))
    }
    var selectedStageIdx by remember { mutableStateOf<Int?>(null) }

    if (selectedStageIdx == null) {
        StageMapScreen(
            gameTitle = "فقه الصيام أحكام ومعانٍ",
            gameSubtitle = "٢٠ مرحلة (٦ أحكام في كل مرحلة) لإتقان فقه الصوم",
            totalStages = TOTAL_STAGES,
            progressData = progressData,
            onSelectStage = { selectedStageIdx = it },
            onBack = onBack
        )
    } else {
        val stageIdx = selectedStageIdx!!
        val currentStage = FASTING_STAGES[stageIdx]

        FastingRulesPlayScreen(
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
private fun FastingRulesPlayScreen(
    stage: FastingStage,
    stageNumber: Int,
    totalStages: Int,
    onBackToMap: () -> Unit,
    onCompleteStage: (Int) -> Unit,
    onNextStage: () -> Unit
) {
    var currentRuleIdx by remember(stage) { mutableIntStateOf(0) }
    var correctCount by remember(stage) { mutableIntStateOf(0) }
    var selectedAnswer by remember(stage, currentRuleIdx) { mutableStateOf<Boolean?>(null) }
    var isAnswered by remember(stage, currentRuleIdx) { mutableStateOf(false) }
    var showWinDialog by remember(stage) { mutableStateOf(false) }
    var earnedStars by remember(stage) { mutableIntStateOf(3) }

    val currentRule = stage.rules[currentRuleIdx]

    fun handleAnswer(ans: Boolean) {
        if (isAnswered) return
        selectedAnswer = ans
        isAnswered = true

        if (ans == currentRule.isTrue) {
            correctCount += 1
        }
    }

    fun handleNextRule() {
        if (currentRuleIdx + 1 < stage.rules.size) {
            currentRuleIdx += 1
            selectedAnswer = null
            isAnswered = false
        } else {
            val stars = when {
                correctCount == 6 -> 3
                correctCount >= 4 -> 2
                else -> 1
            }
            earnedStars = stars
            onCompleteStage(stars)
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
                        text = "فقه الصيام",
                        color = Gold,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "المرحلة ${stageNumber.toArabicDigits()} من ${totalStages.toArabicDigits()} (الحكم ${(currentRuleIdx + 1).toArabicDigits()} من ٦)",
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
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Gold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "صحيح: ${correctCount.toArabicDigits()}/٦",
                            color = Gold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stage Title
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .noorGlassCard(cornerRadius = 16.dp)
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stage.title,
                    color = Gold,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Rule Card
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
                        text = "هل العبارة التالية صحيحة أم خاطئة شرعاً؟",
                        color = TextSecondaryLinen,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = currentRule.ruleStatement,
                        color = TextMain,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        lineHeight = 28.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // True / False Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // True Button
                val isTrueCorrect = (currentRule.isTrue == true)
                val trueBg = when {
                    isAnswered && isTrueCorrect -> Color(0xFF2E7D32).copy(alpha = 0.85f)
                    isAnswered && selectedAnswer == true -> Color(0xFFC62828).copy(alpha = 0.85f)
                    else -> NavyCard
                }
                val trueBorder = when {
                    isAnswered && isTrueCorrect -> Color(0xFF81C784)
                    isAnswered && selectedAnswer == true -> Color(0xFFE57373)
                    else -> Color(0xFF4CAF50).copy(alpha = 0.5f)
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(trueBg)
                        .border(1.5.dp, trueBorder, RoundedCornerShape(16.dp))
                        .clickable(enabled = !isAnswered) { handleAnswer(true) }
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color(0xFF81C784),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "صحيحة (صح)",
                            color = TextMain,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // False Button
                val isFalseCorrect = (currentRule.isTrue == false)
                val falseBg = when {
                    isAnswered && isFalseCorrect -> Color(0xFF2E7D32).copy(alpha = 0.85f)
                    isAnswered && selectedAnswer == false -> Color(0xFFC62828).copy(alpha = 0.85f)
                    else -> NavyCard
                }
                val falseBorder = when {
                    isAnswered && isFalseCorrect -> Color(0xFF81C784)
                    isAnswered && selectedAnswer == false -> Color(0xFFE57373)
                    else -> Color(0xFFF44336).copy(alpha = 0.5f)
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(falseBg)
                        .border(1.5.dp, falseBorder, RoundedCornerShape(16.dp))
                        .clickable(enabled = !isAnswered) { handleAnswer(false) }
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = Color(0xFFE57373),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "خاطئة (خطأ)",
                            color = TextMain,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (isAnswered) {
                Spacer(modifier = Modifier.height(20.dp))

                // Evidence & Explanation Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .noorGlassCard(cornerRadius = 16.dp)
                        .padding(14.dp)
                ) {
                    Column {
                        Text(
                            text = "📖 الدليل والتوجيه الفقهي:",
                            color = Gold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = currentRule.evidence,
                            color = TextSecondaryLinen,
                            fontSize = 13.sp,
                            lineHeight = 20.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { handleNextRule() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Gold)
                ) {
                    Text(
                        text = if (currentRuleIdx + 1 < stage.rules.size) "الحكم التالي ➔" else "إنهاء المرحلة",
                        color = Navy,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
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
                    currentRuleIdx = 0
                    correctCount = 0
                    selectedAnswer = null
                    isAnswered = false
                },
                onStageMap = onBackToMap
            )
        }
    }
}
