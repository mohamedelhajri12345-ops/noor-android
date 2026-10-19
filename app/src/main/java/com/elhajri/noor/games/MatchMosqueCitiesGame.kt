package com.elhajri.noor.games

import androidx.compose.runtime.Composable

@Composable
fun MatchMosqueCitiesGameScreen(onBack: () -> Unit) {
    val items = listOf(
        Pair("المسجد الحرام", "مكة المكرمة"),
        Pair("المسجد النبوي", "المدينة المنورة"),
        Pair("المسجد الأقصى", "القدس الشريف"),
        Pair("جامع الأموي", "دمشق"),
        Pair("جامع الأزهر", "القاهرة"),
        Pair("مسجد قباء", "المدينة المنورة"),
        Pair("جامع الزيتونة", "تونس"),
        Pair("جامع القرويين", "فاس"),
        Pair("مسجد الشيخ زايد", "أبوظبي"),
        Pair("جامع سلطان أحمد (المسجد الأزرق)", "إسطنبول"),
        Pair("مسجد الحسن الثاني", "الدار البيضاء"),
        Pair("جامع عقبة بن نافع", "القيروان"),
        Pair("جامع السلطان قابوس", "مسقط"),
        Pair("مسجد الغازي خسرو بك", "سراييفو")
    )

    MemoryMatchEngine(
        gameTitle = "مطابقة المساجد ومدنها",
        items = items,
        frontLabel = "المسجد المبارك",
        backLabel = "المدينة / البلد",
        onBack = onBack
    )
}
