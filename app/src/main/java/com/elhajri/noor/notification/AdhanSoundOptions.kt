package com.elhajri.noor.notification

import com.elhajri.noor.R

data class AdhanAudioOption(
    val id: String,
    val nameAr: String,
    val nameEn: String,
    val descriptionAr: String,
    val resId: Int
)

object AdhanSoundOptions {
    val options = listOf(
        AdhanAudioOption(
            id = "makkah",
            nameAr = "أذان مكة المكرمة",
            nameEn = "Makkah Adhan",
            descriptionAr = "الأذان المكي الشهير والنداء الخاشع من المسجد الحرام",
            resId = R.raw.adhan_makkah
        ),
        AdhanAudioOption(
            id = "madinah",
            nameAr = "أذان المدينة المنورة",
            nameEn = "Madinah Adhan",
            descriptionAr = "الأذان المدني العذب الندِي من المسجد النبوي الشريف",
            resId = R.raw.adhan_madinah
        ),
        AdhanAudioOption(
            id = "fajr",
            nameAr = "أذان الفجر (الصلاة خير من النوم)",
            nameEn = "Fajr Adhan",
            descriptionAr = "أذان صلاة الفجر بصوت ندي مع تثويب الفجر المبارك",
            resId = R.raw.adhan_fajr
        ),
        AdhanAudioOption(
            id = "takbeer",
            nameAr = "تكبيرات الأذان (تنبيه هادئ)",
            nameEn = "Adhan Takbeer",
            descriptionAr = "تكبيرات الأذان المختصرة لتنبيه مقتضب وهادئ",
            resId = R.raw.adhan_takbeer
        )
    )

    fun getResId(id: String): Int {
        return options.firstOrNull { it.id == id }?.resId ?: R.raw.adhan_makkah
    }

    fun getOption(id: String): AdhanAudioOption {
        return options.firstOrNull { it.id == id } ?: options.first()
    }
}
