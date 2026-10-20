package com.elhajri.noor.mychild

import android.content.Context
import org.json.JSONObject
import java.io.InputStreamReader
import kotlin.random.Random

object FamilyQuizEngine {
    private val fallbackQuestions = listOf(
        FamilyQuizQuestion(1, "ما هو الكتاب السماوي الذي أنزل على سيدنا محمد ﷺ؟", listOf("القرآن الكريم", "التوراة", "الإنجيل", "الزبور"), 0, "القرآن الكريم هو كلام الله الخاتم المتعبد بتلاوته."),
        FamilyQuizQuestion(2, "كم عدد سور القرآن الكريم؟", listOf("١١٤ سورة", "١٠٠ سورة", "١٢٠ سورة", "٩٩ سورة"), 0, "القرآن الكريم يحتوي على ١١٤ سورة مباركة."),
        FamilyQuizQuestion(3, "من هو أول الأنبياء وعلي أب البشر؟", listOf("آدم عليه السلام", "نوح عليه السلام", "إبراهيم عليه السلام", "موسى عليه السلام"), 0, "سيدنا آدم عليه السلام هو أول خلقه الله وأول الأنبياء."),
        FamilyQuizQuestion(4, "ما هي أول سورة في المصحف الشريف؟", listOf("الفاتحة", "البقرة", "الإخلاص", "الناس"), 0, "سورة الفاتحة هي فاتحة الكتاب والسبع المثاني."),
        FamilyQuizQuestion(5, "في أي شهر يصوم المسلمون؟", listOf("شهر رمضان", "شعبان", "محرم", "شوال"), 0, "شهر رمضان الفضيل هو شهر الصيام والقرآن."),
        FamilyQuizQuestion(6, "كم عدد صلوات الفريضة في اليوم والليل؟", listOf("٥ صلوات", "٣ صلوات", "٧ صلوات", "٤ صلوات"), 0, "فرض الله تعالى على المسلمين خمس صلوات في اليوم والليل."),
        FamilyQuizQuestion(7, "ما هي قبلة المسلمين في الصلاة؟", listOf("الكعبة المشرفة", "المسجد الأقصى", "المسجد النبوي", "جبل عرفات"), 0, "الكعبة المشرفة بكة المكرّمة هي قبلة المسلمين."),
        FamilyQuizQuestion(8, "ما هي الكلمة التي نقولها عند بدء تناول الطعام؟", listOf("بسم الله", "الحمد لله", "أستغفر الله", "سبحان الله"), 0, "نقول 'بسم الله' عند بداية الأكل والشرب بركةً وتسمية."),
        FamilyQuizQuestion(9, "ما هي أعظم آية في القرآن الكريم؟", listOf("آية الكرسي", "آية الدين", "آية الشفاء", "آية النور"), 0, "آية الكرسي في سورة البقرة هي أعظم آية في القرآن الكريم."),
        FamilyQuizQuestion(10, "من هو خاتم الأنبياء والمرسلين؟", listOf("محمد ﷺ", "عيسى عليه السلام", "موسى عليه السلام", "إبراهيم عليه السلام"), 0, "سيدنا محمد ﷺ هو خاتم الأنبياء والمرسلين أجمعين."),
        FamilyQuizQuestion(11, "ما اسم النبي الذي التقمه الحوت في البحر؟", listOf("يونس عليه السلام", "يوسف عليه السلام", "أيوب عليه السلام", "سليمان عليه السلام"), 0, "سيدنا يونس عليه السلام ناداه في الظلمات فنجاه الله."),
        FamilyQuizQuestion(12, "ما هي السورة الملقبة بأم القرآن؟", listOf("الفاتحة", "يس", "الرحمن", "الملك"), 0, "سورة الفاتحة تسمى أم القرآن الشريف."),
        FamilyQuizQuestion(13, "ماذا نقول عندما نرى شيئاً يعجبنا؟", listOf("ما شاء الله", "لا إله إلا الله", "الحمد لله", "أستغفر الله"), 0, "نقول 'ما شاء الله تبارك الله' عند إعجابنا بالشيء."),
        FamilyQuizQuestion(14, "كم عدد أركان الإسلام؟", listOf("٥ أركان", "٦ أركان", "٤ أركان", "٧ أركان"), 0, "بُني الإسلام على خمس: الشهادتان، الصلاة، الزكاة، الصوم، والحج."),
        FamilyQuizQuestion(15, "ما اسم الغار الذي نزل فيه الوحي على النبي ﷺ؟", listOf("غار حراء", "غار ثور", "غار أُحد", "غار الرحمة"), 0, "نزل جبريل عليه السلام بالوحي لأول مرة في غار حراء بمكة."),
        FamilyQuizQuestion(16, "ما هي أقصر سورة في القرآن الكريم؟", listOf("الكوثر", "الإخلاص", "الفلق", "النصر"), 0, "سورة الكوثر هي أقصر سورة وتتكون من ٣ آيات."),
        FamilyQuizQuestion(17, "ماذا يُسمى دعاء الذهاب إلى النوم؟", listOf("أذكار النوم", "دعاء السفر", "دعاء الركوب", "دعاء الاستخارة"), 0, "نقوم بقراءة أذكار النوم طلباً للحفظ والسكينة."),
        FamilyQuizQuestion(18, "من هو الصحابي الجليل الملقب بالصديق؟", listOf("أبو بكر الصديق رضي الله عنه", "عمر بن الخطاب", "عثمان بن عفان", "علي بن أبي طالب"), 0, "أبو بكر الصديق هو أول الخلفاء الراشدين وصاحب النبي في الهجرة."),
        FamilyQuizQuestion(19, "ما اسم الناقة المباركة التي هاجر عليها النبي ﷺ؟", listOf("القصواء", "الشهباء", "البغلة", "العضباء"), 0, "ناقة النبي ﷺ الكريمة كان اسمها القصواء."),
        FamilyQuizQuestion(20, "ما هي الجائزة العظمى التي يتمناها المؤمن في الجنة؟", listOf("رؤية وجه الله الكريم", "الأنهار", "القصور", "الفواكه"), 0, "أعظم نعيم في الجنة هو رؤية وجه الله عز وجل.")
    )

    fun getQuizQuestions(context: Context, count: Int = 5): List<FamilyQuizQuestion> {
        val loaded = mutableListOf<FamilyQuizQuestion>()

        try {
            val isStream = context.assets.open("data/examQuestions.json")
            val reader = InputStreamReader(isStream, "UTF-8")
            val jsonText = reader.readText()
            reader.close()
            isStream.close()

            val rootObj = JSONObject(jsonText)
            val array = rootObj.optJSONArray("questions")
            if (array != null) {
                val easyItems = mutableListOf<JSONObject>()
                for (i in 0 until array.length()) {
                    val item = array.getJSONObject(i)
                    val diff = item.optString("difficulty", "easy")
                    if (diff.lowercase() == "easy" || diff.lowercase() == "kid") {
                        easyItems.add(item)
                    }
                }

                easyItems.shuffle()
                val selected = easyItems.take(count)
                for ((idx, obj) in selected.withIndex()) {
                    val q = obj.optString("q", "")
                    val optionsArr = obj.optJSONArray("options")
                    val options = mutableListOf<String>()
                    if (optionsArr != null) {
                        for (j in 0 until optionsArr.length()) {
                            options.add(optionsArr.getString(j))
                        }
                    }
                    val correct = obj.optInt("correct", 0)
                    val explanation = obj.optString("explanation", "إجابة صحيحة وفق المعارف الإسلامية.")
                    if (q.isNotBlank() && options.size >= 2) {
                        loaded.add(FamilyQuizQuestion(idx + 1, q, options, correct, explanation))
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (loaded.size >= count) {
            return loaded.take(count)
        }

        // Fallback to built-in bank
        val shuffledFallback = fallbackQuestions.shuffled()
        return shuffledFallback.take(count)
    }
}
