package com.elhajri.noor.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * تعريف الثيم — بنية بيانات واضحة تجعل إضافة ثيم جديد مسألة سطر واحد
 * داخل قائمة NoorThemes.all دون أي تعديل على بقية النظام.
 */
data class NoorThemeDef(
    val id: String,
    val name: String,
    val description: String,
    val price: Int,               // 0 = مجاني
    // الخلفية العامة (تدرّج رأسي من اللونين)
    val background: Color,
    val backgroundEnd: Color,
    // البطاقات والأسطح
    val surface: Color,
    val surfaceVariant: Color,
    // اللون الرئيسي (الذهب في الثيم الافتراضي)
    val accent: Color,
    val accentSoft: Color,
    val accentDeep: Color,
    // النصوص
    val textPrimary: Color,
    val textSecondary: Color,
    // ألوان الحالات
    val success: Color = Color(0xFF10B981),
    val error: Color = Color(0xFFEF4444),
    // الأنماط البصرية
    val cardRadius: Dp = 16.dp,
    val buttonRadius: Dp = 14.dp,
    val buttonGradient: List<Color> = listOf(accentSoft, accent, accentDeep)
)

/**
 * حالة الثيم النشط — واحدة لكل التطبيق، تقرأها الشاشات تفاعلياً
 * (أي تغيير هنا ينعكس فوراً على كل الواجهات دون إعادة إنشاء النشاط).
 */
object NoorThemeState {
    var active by mutableStateOf(NoorThemes.all.first())
        private set

    fun apply(def: NoorThemeDef) {
        active = def
    }

    fun applyById(id: String) {
        NoorThemes.byId(id)?.let { active = it }
    }
}

/**
 * كتالوج الثيمات — 21 ثيمة إسلامية (نور الأساسي مجاني + 20 ثيمة مدفوعة بالنقاط).
 * إضافة ثيمة جديدة = إضافة عنصر واحد هنا فقط.
 */
object NoorThemes {

    private fun theme(
        id: String, name: String, description: String, price: Int = ThemeStore.THEME_PRICE,
        bg: Long, bgEnd: Long, surface: Long, surfaceVariant: Long,
        accent: Long, accentSoft: Long, accentDeep: Long,
        text: Long, textSec: Long
    ) = NoorThemeDef(
        id, name, description, price,
        Color(bg), Color(bgEnd), Color(surface), Color(surfaceVariant),
        Color(accent), Color(accentSoft), Color(accentDeep),
        Color(text), Color(textSec)
    )

    val all = listOf(

        // ─────────── 00 — الثيم الافتراضي المجاني ───────────
        theme(
            "noor_default", "نور الأساسي", "الثيم الافتراضي الفاخر — أبنوس مخملي وذهب إمبراطوري", price = 0,
            bg = 0xFF0A0E14, bgEnd = 0xFF0A0E14, surface = 0xFF15181E, surfaceVariant = 0xFF1A1F29,
            accent = 0xFFD4AF35, accentSoft = 0xFFF7D56E, accentDeep = 0xFF9E7C2E,
            text = 0xFFFAF8F5, textSec = 0xFFC5BAA8
        ),

        // ─────────── 01 — نور الفجر ───────────
        theme(
            "fajr_noor", "نور الفجر", "سماء الفجر الهادئة — سماوي نقي وذهب خفيف لبداية يوم جديد",
            bg = 0xFFEAF2FB, bgEnd = 0xFFDCE9F7, surface = 0xFFFDFEFE, surfaceVariant = 0xFFEAF2FB,
            accent = 0xFFB08D2F, accentSoft = 0xFFD9BC66, accentDeep = 0xFF8A6C20,
            text = 0xFF1E2A3A, textSec = 0xFF5B6B7E
        ),

        // ─────────── 02 — زمرد القرآن ───────────
        theme(
            "emerald_quran", "زمرد القرآن", "كلاسيكي إسلامي فاخر — زمردي عميق وذهب على أخضر داكن",
            bg = 0xFF062A1E, bgEnd = 0xFF04211A, surface = 0xFF0B3D2E, surfaceVariant = 0xFF0E4A38,
            accent = 0xFFD4AF35, accentSoft = 0xFFF0D77A, accentDeep = 0xFF9E7C2E,
            text = 0xFFF2FAF5, textSec = 0xFFBFD6C9
        ),

        // ─────────── 03 — المسجد الذهبي ───────────
        theme(
            "golden_mosque", "المسجد الذهبي", "فخامة ملكية — ذهب راقٍ على بني داكن وأسود ناعم",
            bg = 0xFF16100A, bgEnd = 0xFF100B07, surface = 0xFF231A10, surfaceVariant = 0xFF2C2115,
            accent = 0xFFE3B94D, accentSoft = 0xFFF6D98A, accentDeep = 0xFFA87F2B,
            text = 0xFFFBF5E9, textSec = 0xFFD6C4A5
        ),

        // ─────────── 04 — ليل مكة ───────────
        theme(
            "makkah_night", "ليل مكة", "ليل روحاني فاخر — كحلي عميق وذهب متوهج",
            bg = 0xFF070F2A, bgEnd = 0xFF050A1E, surface = 0xFF0C1734, surfaceVariant = 0xFF11204A,
            accent = 0xFFE0B94F, accentSoft = 0xFFF7DB86, accentDeep = 0xFFA5842D,
            text = 0xFFF4F6FC, textSec = 0xFFB9C2DC
        ),

        // ─────────── 05 — المدينة الهادئة ───────────
        theme(
            "madinah_calm", "المدينة الهادئة", "دفء تراثي — أخضر زيتوني وبيج المسجد النبوي",
            bg = 0xFF141B10, bgEnd = 0xFF101609, surface = 0xFF1F2A17, surfaceVariant = 0xFF28341D,
            accent = 0xFFD9B25F, accentSoft = 0xFFEFD394, accentDeep = 0xFFA08140,
            text = 0xFFF7F3E8, textSec = 0xFFD0C7B0
        ),

        // ─────────── 06 — الكعبة الملكية ───────────
        theme(
            "royal_kaaba", "الكعبة الملكية", "Premium إسلامي — أسود فاخر بكسوة ذهبية",
            bg = 0xFF0A0A0C, bgEnd = 0xFF070708, surface = 0xFF141416, surfaceVariant = 0xFF1B1B1E,
            accent = 0xFFDAB84C, accentSoft = 0xFFF3DE8C, accentDeep = 0xFF9C7D2F,
            text = 0xFFF5F4F2, textSec = 0xFFC2C0BB
        ),

        // ─────────── 07 — السماء الزرقاء ───────────
        theme(
            "blue_sky", "السماء الزرقاء", "مشرق وعصري — أزرق ملكي على سماوي وأبيض",
            bg = 0xFFE8F3FE, bgEnd = 0xFFD9EBFC, surface = 0xFFFEFEFF, surfaceVariant = 0xFFE3F0FD,
            accent = 0xFF1D5FBF, accentSoft = 0xFF4E8Ae6, accentDeep = 0xFF15418A,
            text = 0xFF13233A, textSec = 0xFF4A607A
        ),

        // ─────────── 08 — الهلال الفضي ───────────
        theme(
            "silver_crescent", "الهلال الفضي", "ليلي أنيق Minimal — فضي هادئ على كحلي ليلي",
            bg = 0xFF0B1020, bgEnd = 0xFF080C18, surface = 0xFF131A30, surfaceVariant = 0xFF18213E,
            accent = 0xFFC9D4E4, accentSoft = 0xFFE6ECF5, accentDeep = 0xFF8E9DB3,
            text = 0xFFEFF3F9, textSec = 0xFFB4BFCE
        ),

        // ─────────── 09 — الصحراء الذهبية ───────────
        theme(
            "golden_desert", "الصحراء الذهبية", "تراث عربي — رمال ذهبية وبيج دافئ وبني عميق",
            bg = 0xFF1E160C, bgEnd = 0xFF171009, surface = 0xFF2A2013, surfaceVariant = 0xFF33281A,
            accent = 0xFFE2BC6A, accentSoft = 0xFFF4DCA6, accentDeep = 0xFFAA8340,
            text = 0xFFFAF2E4, textSec = 0xFFD9C8AC
        ),

        // ─────────── 10 — الزيتون ───────────
        theme(
            "olive", "الزيتون", "طبيعي مريح للعين — زيتوني داكن على كريمي هادئ",
            bg = 0xFF101508, bgEnd = 0xFF0C1006, surface = 0xFF1A2110, surfaceVariant = 0xFF222A16,
            accent = 0xFFC9B45A, accentSoft = 0xFFE5D68E, accentDeep = 0xFF93823D,
            text = 0xFFF6F3E6, textSec = 0xFFCDC8B2
        ),

        // ─────────── 11 — الفيروز الإسلامي ───────────
        theme(
            "islamic_turquoise", "الفيروز الإسلامي", "إسلامي حديث مشرق — تركواز على فيروزي وأبيض",
            bg = 0xFF05302E, bgEnd = 0xFF042624, surface = 0xFF094540, surfaceVariant = 0xFF0B524C,
            accent = 0xFFD9B54A, accentSoft = 0xFFF2DC8E, accentDeep = 0xFFA28430,
            text = 0xFFEFFBF9, textSec = 0xFFB5DBD6
        ),

        // ─────────── 12 — البنفسج الروحاني ───────────
        theme(
            "spiritual_violet", "البنفسج الروحاني", "روحاني فاخر — بنفسجي عميق بذهب فاتح",
            bg = 0xFF170E2A, bgEnd = 0xFF110A20, surface = 0xFF221741, surfaceVariant = 0xFF2A1C4E,
            accent = 0xFFE4C565, accentSoft = 0xFFF6E4A3, accentDeep = 0xFFAB8A3C,
            text = 0xFFF6F2FB, textSec = 0xFFC9BEDF
        ),

        // ─────────── 13 — زمرد الليل ───────────
        theme(
            "night_emerald", "زمرد الليل", "Dark Premium — زمردي داكن على أسود بذهب",
            bg = 0xFF041410, bgEnd = 0xFF020C09, surface = 0xFF0A211A, surfaceVariant = 0xFF0D2921,
            accent = 0xFFD6B345, accentSoft = 0xFFF1D980, accentDeep = 0xFF9F8030,
            text = 0xFFF0FAF6, textSec = 0xFFB4CFC4
        ),

        // ─────────── 14 — الفجر الوردي ───────────
        theme(
            "pink_dawn", "الفجر الوردي", "نعومة هادئة راقية — وردي فجر مع بنفسجي فاتح وسماوي",
            bg = 0xFF241420, bgEnd = 0xFF1C1019, surface = 0xFF33202D, surfaceVariant = 0xFF3C2635,
            accent = 0xFFE5B896, accentSoft = 0xFFF6D9C4, accentDeep = 0xFFB07E5F,
            text = 0xFFFDF3F0, textSec = 0xFFE0C1C2
        ),

        // ─────────── 15 — البحر الإسلامي ───────────
        theme(
            "islamic_sea", "البحر الإسلامي", "منعش وهادئ — أزرق بحري عميق بتركوازي وذهب",
            bg = 0xFF052338, bgEnd = 0xFF041B2C, surface = 0xFF0A3350, surfaceVariant = 0xFF0C3D5E,
            accent = 0xFFDDB654, accentSoft = 0xFFF5DC92, accentDeep = 0xFFA58434,
            text = 0xFFEFF7FC, textSec = 0xFFB3CFE0
        ),

        // ─────────── 16 — المخطوطات ───────────
        theme(
            "manuscripts", "المخطوطات", "تراث مخطوطات فخم — ورق عتيق وبني ذهبي وأخضر داكن",
            bg = 0xFF171009, bgEnd = 0xFF120B06, surface = 0xFF241A0E, surfaceVariant = 0xFF2C2112,
            accent = 0xFFC99B4A, accentSoft = 0xFFE4C683, accentDeep = 0xFF96702F,
            text = 0xFFFAF0DC, textSec = 0xFFD8C6A4
        ),

        // ─────────── 17 — قبة الزمرد ───────────
        theme(
            "emerald_dome", "قبة الزمرد", "كلاسيكي فاخر — قبة زمردية بذهب وأبيض دافئ",
            bg = 0xFF063D2C, bgEnd = 0xFF052E22, surface = 0xFF0A523C, surfaceVariant = 0xFF0C5E46,
            accent = 0xFFE3C15C, accentSoft = 0xFFF5DE9B, accentDeep = 0xFFAC8B38,
            text = 0xFFF1FBF7, textSec = 0xFFBCE0D1
        ),

        // ─────────── 18 — رمضان ───────────
        theme(
            "ramadan", "رمضان", "روحاني احتفالي متزن — فانوس ليلي بنفسجي بأزرق وذهب",
            bg = 0xFF0B1230, bgEnd = 0xFF1A0F3D, surface = 0xFF131C49, surfaceVariant = 0xFF182355,
            accent = 0xFFE7C05C, accentSoft = 0xFFF8E29B, accentDeep = 0xFFAE8E3A,
            text = 0xFFF3F1FC, textSec = 0xFFC0BCE0
        ),

        // ─────────── 19 — الهلال الليلي ───────────
        theme(
            "night_crescent", "الهلال الليلي", "Dark Mode فاخر — أسود كحلي بفضي وذهب بسيط",
            bg = 0xFF06080D, bgEnd = 0xFF04050A, surface = 0xFF0D1220, surfaceVariant = 0xFF111726,
            accent = 0xFFC9CCD6, accentSoft = 0xFFE8EAF0, accentDeep = 0xFF9AA0AE,
            text = 0xFFEFF1F6, textSec = 0xFFB2B7C4
        ),

        // ─────────── 20 — نور Premium ───────────
        theme(
            "noor_premium", "نور Premium", "الثيم المميز — كحلي فاخر بذهب وأبيض ولمسة زمرد",
            bg = 0xFF0A1226, bgEnd = 0xFF070D1B, surface = 0xFF101B38, surfaceVariant = 0xFF152347,
            accent = 0xFFE5C158, accentSoft = 0xFFF8E29E, accentDeep = 0xFFAC8C3E,
            text = 0xFFFAFAF7, textSec = 0xFFC7CDE0
        )
    )

    fun byId(id: String): NoorThemeDef? = all.find { it.id == id }
}
