package com.elhajri.noor.tasbih

enum class TasbihStyle(
    val titleAr: String,
    val titleEn: String,
    val descAr: String,
    val descEn: String,
    val tag: String
) {
    ELECTRONIC(
        "المسبحة الإلكترونية",
        "Electronic Tasbih",
        "عداد رقمي كلاسيكي متطور مع حلقة التقدّم والشعور بالملمس",
        "Classic digital counter with animated gauge and haptic ring",
        "counter"
    ),
    THREAD(
        "مسبحة الخيط",
        "Thread Tasbih",
        "انزلاق الخرز المستمر على الخيط بحركة فيزيائية انسيابية",
        "Physics-based sliding beads along a threaded string",
        "thread"
    ),
    ROTATING(
        "مسبحة الكرات الدوارة",
        "Rotating Golden Ring",
        "حلقة من الخرز الذهبي ثلاثي الأبعاد يدور مع كل ضغطة",
        "3D golden beads ring rotating smoothly on each tap",
        "rotate"
    ),
    GARDEN(
        "مسبحة البستان",
        "Night Garden Tasbih",
        "حديقة ليلية هادئة تُزهر فيها وردة نورانية مع كل تسبيحة",
        "Serene night garden growing a blooming flower per tasbih",
        "garden"
    ),
    LIGHT_ORB(
        "مسبحة النور",
        "Glowing Orb Tasbih",
        "كرة نورانية مشعة تكتمل هالتها وتضيء المدار عند إتمام الورد",
        "Glowing orb brightens per tap, completing a circle of light",
        "orb"
    );

    companion object {
        fun fromIndex(index: Int): TasbihStyle {
            val entries = entries
            return entries.getOrElse(index.coerceIn(0, entries.size - 1)) { ELECTRONIC }
        }
    }
}

data class DhikrItem(
    val text: String,
    val defaultTarget: Int = 33
)

val DEFAULT_DHIKR_LIST = listOf(
    DhikrItem("سبحان الله", 33),
    DhikrItem("الحمد لله", 33),
    DhikrItem("الله أكبر", 33),
    DhikrItem("أستغفر الله", 100),
    DhikrItem("لا إله إلا الله", 100),
    DhikrItem("اللهم صلِّ على محمد", 100)
)

fun toArabicNumber(number: Any): String {
    val map = mapOf('0' to '٠', '1' to '١', '2' to '٢', '3' to '٣', '4' to '٤', '5' to '٥', '6' to '٦', '7' to '٧', '8' to '٨', '9' to '٩')
    return number.toString().map { map[it] ?: it }.joinToString("")
}
