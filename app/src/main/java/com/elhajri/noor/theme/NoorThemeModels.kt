package com.elhajri.noor.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * أنواع البصمات البصرية للثيمات (Theme Visual Signatures)
 * كل ثيمة لها بصمة رسم كود فريدة مرسومة بالـ Canvas دون أي صور خارجية.
 */
enum class ThemeMotifType {
    NOOR_FLAGSHIP,      // 00 — نور الأساسي: هالة زيتونية ذهبية مع هلال دقيق وشعاع ضوء
    FAJR_DAWN,          // 01 — نور الفجر: أشعة فجر سماوية متصاعدة وأفق دافئ
    EMERALD_DOME,       // 02 — زمرد القرآن: قبة مسجد زمردية أنيقة مع توهج ناعم
    GOLDEN_MOSQUE,      // 03 — المسجد الذهبي: ظلال مآذن وقبة ذهبية فاخرة
    MAKKAH_NIGHT,       // 04 — ليل مكة: أفق الكعبة المشرفة تحت سماء كحلية بنجوم متلألئة
    MADINAH_CALM,       // 05 — المدينة الهادئة: منحنى القبة الخضراء مع هالة هادئة
    ROYAL_KAABA,        // 06 — الكعبة الملكية: مجسم الكعبة الملكي بحزام كسوة ذهبي متوهج
    BLUE_SKY,           // 07 — السماء الزرقاء: غيوم انسيابية وأشعة ناصعة
    SILVER_CRESCENT,    // 08 — الهلال الفضي: هلال فضي ناعم مع نجوم دقيقة
    GOLDEN_DESERT,      // 09 — الصحراء الذهبية: كثبان رملية انسيابية وأفق دافئ
    OLIVE_GARDEN,       // 10 — الزيتون: أغصان وأوراق زيتون انسيابية
    TURQUOISE_ARCH,     // 11 — الفيروز الإسلامي: أقواس أندلسية فيروذية متوهجة
    SPIRITUAL_VIOLET,   // 12 — البنفسج الروحاني: سماء بنفسجية مع إشعاع نجمي ثماني
    NIGHT_EMERALD,      // 13 — زمرد الليل: عناقيد نجوم على سماء زمردية ليليّة
    PINK_DAWN,          // 14 — الفجر الوردي: تموجات ضوئية وردية لأفق الفجر
    ISLAMIC_SEA,        // 15 — البحر الإسلامي: أمواج بحرية انسيابية بتألق ذهبي
    MANUSCRIPTS,        // 16 — المخطوطات: زخارف زوايا المخطوطات الأندلسية العتيقة
    EMERALD_GEM,        // 17 — قبة الزمرد: انعكاسات بلورية زمردية فاخرة
    RAMADAN_LANTERN,    // 18 — رمضان: فانوس رمضان متوهج بشعاع ضوء وهلال ذهبي
    NIGHT_CRESCENT,     // 19 — الهلال الليلي: هلال بارز متوهج في سماء ليليّة حلكة
    NOOR_PREMIUM        // 20 — نور Premium: أشعة ملكية متداخلة وهالة تاجية ذهبية
}

/**
 * معلمات معاملة البطاقات للثيمة (Card Styles).
 * تمكّن العائلات المختلفة من إظهار شخصيات بصرية متفاوتة (انحناء الزوايا، قوة الحدود، التوهج).
 */
data class NoorCardStyle(
    val radius: Dp = 16.dp,
    val borderWidth: Dp = 1.dp,
    val borderColor: Color = Color.Unspecified,
    val borderAlpha: Float = 0.2f,
    val elevation: Dp = 2.dp,
    val glowColor: Color? = null,
    val isFramed: Boolean = false
)

/**
 * معلمات تخصيص الهيدر وشريط العنوان (Header Styles).
 */
data class NoorHeaderStyle(
    val showAccentRay: Boolean = true,
    val glowIntensity: Float = 0.3f,
    val badgeStyle: String = "CLASSIC"
)
