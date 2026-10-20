package com.elhajri.noor.theme

import com.elhajri.noor.settings.NoorLocale
import com.elhajri.noor.settings.t

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.elhajri.noor.R

/**
 * تعريف الثيم — بنية بيانات واضحة تجعل إضافة ثيم جديد مسألة سطر واحد
 * داخل قائمة NoorThemes.all دون أي تعديل على بقية النظام.
 */
data class NoorThemeDef(
    val id: String,
    val nameAr: String,
    val nameEn: String,
    val descAr: String,
    val descEn: String,
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
    // خلفية مرسومة بالكود فقط (تدرّجات وإضاءة) — لا صور ويب نهائياً
    val backgroundRes: Int = 0,
    // الأنماط البصرية
    val cardRadius: Dp = 16.dp,
    val buttonRadius: Dp = 14.dp,
    val buttonGradient: List<Color> = listOf(accentSoft, accent, accentDeep),
    // الهوية البصرية للبصمة المخصصة (Motif Identity Fields)
    val motifType: ThemeMotifType = ThemeMotifType.NOOR_FLAGSHIP,
    val motifColor: Color = accent,
    val cardStyle: NoorCardStyle = NoorCardStyle(radius = cardRadius),
    val headerStyle: NoorHeaderStyle = NoorHeaderStyle()
) {
    val name: String get() = if (NoorLocale.isEnglish) nameEn else nameAr
    val description: String get() = if (NoorLocale.isEnglish) descEn else descAr
}

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
 * كل ثيمة تمتلك بصمتها البصرية الفريدة (Visual Signature Motif) ومعلمات أسلوبها الخاص.
 */
object NoorThemes {

    private fun theme(
        id: String, nameAr: String, nameEn: String, descAr: String, descEn: String, price: Int = ThemeStore.THEME_PRICE,
        bg: Long, bgEnd: Long, surface: Long, surfaceVariant: Long,
        accent: Long, accentSoft: Long, accentDeep: Long,
        text: Long, textSec: Long,
        motifType: ThemeMotifType = ThemeMotifType.NOOR_FLAGSHIP,
        motifColor: Color? = null,
        cardStyle: NoorCardStyle? = null,
        headerStyle: NoorHeaderStyle = NoorHeaderStyle()
    ): NoorThemeDef {
        val accentCol = Color(accent)
        return NoorThemeDef(
            id, nameAr, nameEn, descAr, descEn, price,
            Color(bg), Color(bgEnd), Color(surface), Color(surfaceVariant),
            accentCol, Color(accentSoft), Color(accentDeep),
            Color(text), Color(textSec),
            motifType = motifType,
            motifColor = motifColor ?: accentCol,
            cardStyle = cardStyle ?: NoorCardStyle(glowColor = accentCol),
            headerStyle = headerStyle
        )
    }

    private val base = listOf(

        // ─────────── 00 — الثيم الافتراضي المجاني ───────────
        theme(
            "noor_default", "نور الأساسي", "NOOR Default", "الهوية المعتمدة — أخضر ليلي زيتوني وذهب دافئ", "Official Theme — Night Olive Green & Warm Gold", price = 0,
            bg = 0xFF10220F, bgEnd = 0xFF081209, surface = 0xFF13291B, surfaceVariant = 0xFF1A3323,
            accent = 0xFFE9C46A, accentSoft = 0xFFF6E2A0, accentDeep = 0xFFA57C31,
            text = 0xFFEDE9DD, textSec = 0xFFB9C4B0,
            motifType = ThemeMotifType.NOOR_FLAGSHIP,
            cardStyle = NoorCardStyle(radius = 16.dp, borderWidth = 1.dp, borderAlpha = 0.25f, glowColor = Color(0xFFE9C46A))
        ),

        // ─────────── 01 — نور الفجر ───────────
        theme(
            "fajr_noor", "نور الفجر", "Fajr Light", "سماء الفجر الهادئة — أزرق فجري داكن وذهب خفيف لبداية يوم جديد", "Serene Dawn Sky — Dark Dawn Blue & Subtle Gold",
            bg = 0xFF0C1826, bgEnd = 0xFF081220, surface = 0xFF142436, surfaceVariant = 0xFF1C3046,
            accent = 0xFFD9BC66, accentSoft = 0xFFF0DCA0, accentDeep = 0xFFB08D2F,
            text = 0xFFF6FBFF, textSec = 0xFFBFD2E4,
            motifType = ThemeMotifType.FAJR_DAWN,
            cardStyle = NoorCardStyle(radius = 16.dp, borderWidth = 1.dp, borderAlpha = 0.18f, glowColor = Color(0xFFD9BC66))
        ),

        // ─────────── 02 — زمرد القرآن ───────────
        theme(
            "emerald_quran", "زمرد القرآن", "Qur'an Emerald", "كلاسيكي إسلامي فاخر — زمردي عميق وذهب على أخضر داكن", "Luxurious Islamic Classic — Deep Emerald & Gold",
            bg = 0xFF062A1E, bgEnd = 0xFF04211A, surface = 0xFF0B3D2E, surfaceVariant = 0xFF0E4A38,
            accent = 0xFFD4AF35, accentSoft = 0xFFF0D77A, accentDeep = 0xFF9E7C2E,
            text = 0xFFF2FAF5, textSec = 0xFFBFD6C9,
            motifType = ThemeMotifType.EMERALD_DOME,
            cardStyle = NoorCardStyle(radius = 18.dp, borderWidth = 1.2.dp, borderAlpha = 0.30f, glowColor = Color(0xFFD4AF35), isFramed = true)
        ),

        // ─────────── 03 — المسجد الذهبي ───────────
        theme(
            "golden_mosque", "المسجد الذهبي", "Golden Mosque", "فخامة ملكية — ذهب راقٍ على بني داكن وأسود ناعم", "Royal Elegance — Fine Gold on Dark Brown",
            bg = 0xFF16100A, bgEnd = 0xFF100B07, surface = 0xFF231A10, surfaceVariant = 0xFF2C2115,
            accent = 0xFFE3B94D, accentSoft = 0xFFF6D98A, accentDeep = 0xFFA87F2B,
            text = 0xFFFBF5E9, textSec = 0xFFD6C4A5,
            motifType = ThemeMotifType.GOLDEN_MOSQUE,
            cardStyle = NoorCardStyle(radius = 14.dp, borderWidth = 1.5.dp, borderAlpha = 0.35f, glowColor = Color(0xFFE3B94D), isFramed = true)
        ),

        // ─────────── 04 — ليل مكة ───────────
        theme(
            "makkah_night", "ليل مكة", "Makkah Night", "ليل روحاني فاخر — كحلي عميق وذهب متوهج", "Spiritual Night — Deep Navy & Glowing Gold",
            bg = 0xFF070F2A, bgEnd = 0xFF050A1E, surface = 0xFF0C1734, surfaceVariant = 0xFF11204A,
            accent = 0xFFE0B94F, accentSoft = 0xFFF7DB86, accentDeep = 0xFFA5842D,
            text = 0xFFF4F6FC, textSec = 0xFFB9C2DC,
            motifType = ThemeMotifType.MAKKAH_NIGHT,
            cardStyle = NoorCardStyle(radius = 16.dp, borderWidth = 1.dp, borderAlpha = 0.25f, glowColor = Color(0xFFE0B94F))
        ),

        // ─────────── 05 — المدينة الهادئة ───────────
        theme(
            "madinah_calm", "المدينة الهادئة", "Serene Madinah", "دفء تراثي — أخضر زيتوني وبيج المسجد النبوي", "Heritage Warmth — Olive Green & Madinah Beige",
            bg = 0xFF141B10, bgEnd = 0xFF101609, surface = 0xFF1F2A17, surfaceVariant = 0xFF28341D,
            accent = 0xFFD9B25F, accentSoft = 0xFFEFD394, accentDeep = 0xFFA08140,
            text = 0xFFF7F3E8, textSec = 0xFFD0C7B0,
            motifType = ThemeMotifType.MADINAH_CALM,
            cardStyle = NoorCardStyle(radius = 20.dp, borderWidth = 1.dp, borderAlpha = 0.20f, glowColor = Color(0xFFD9B25F))
        ),

        // ─────────── 06 — الكعبة الملكية ───────────
        theme(
            "royal_kaaba", "الكعبة الملكية", "Royal Ka'bah", "Premium إسلامي — أسود فاخر بكسوة ذهبية", "Royal Islamic — Premium Black with Golden Kiswah",
            bg = 0xFF0A0A0C, bgEnd = 0xFF070708, surface = 0xFF141416, surfaceVariant = 0xFF1B1B1E,
            accent = 0xFFDAB84C, accentSoft = 0xFFF3DE8C, accentDeep = 0xFF9C7D2F,
            text = 0xFFF5F4F2, textSec = 0xFFC2C0BB,
            motifType = ThemeMotifType.ROYAL_KAABA,
            cardStyle = NoorCardStyle(radius = 12.dp, borderWidth = 2.dp, borderAlpha = 0.40f, glowColor = Color(0xFFDAB84C), isFramed = true)
        ),

        // ─────────── 07 — السماء الزرقاء ───────────
        theme(
            "blue_sky", "السماء الزرقاء", "Blue Sky", "ليلي عميق — أزرق ملكي داكن صافٍ وخط واضح", "Deep Night — Clear Royal Dark Blue",
            bg = 0xFF0A1A33, bgEnd = 0xFF071226, surface = 0xFF122540, surfaceVariant = 0xFF1B3460,
            accent = 0xFF4E8AE6, accentSoft = 0xFF87B7F2, accentDeep = 0xFF1D5FBF,
            text = 0xFFF2F8FF, textSec = 0xFFB8D0EC,
            motifType = ThemeMotifType.BLUE_SKY,
            cardStyle = NoorCardStyle(radius = 18.dp, borderWidth = 1.dp, borderAlpha = 0.30f, glowColor = Color(0xFF4E8AE6))
        ),

        // ─────────── 08 — الهلال الفضي ───────────
        theme(
            "silver_crescent", "الهلال الفضي", "Silver Crescent", "ليلي أنيق Minimal — فضي هادئ على كحلي ليلي", "Minimalist Night — Calm Silver on Navy",
            bg = 0xFF0B1020, bgEnd = 0xFF080C18, surface = 0xFF131A30, surfaceVariant = 0xFF18213E,
            accent = 0xFFC9D4E4, accentSoft = 0xFFE6ECF5, accentDeep = 0xFF8E9DB3,
            text = 0xFFEFF3F9, textSec = 0xFFB4BFCE,
            motifType = ThemeMotifType.SILVER_CRESCENT,
            cardStyle = NoorCardStyle(radius = 16.dp, borderWidth = 1.dp, borderAlpha = 0.22f, glowColor = Color(0xFFC9D4E4))
        ),

        // ─────────── 09 — الصحراء الذهبية ───────────
        theme(
            "golden_desert", "الصحراء الذهبية", "Golden Desert", "تراث عربي — رمال ذهبية وبيج دافئ وبني عميق", "Arabian Heritage — Golden Sands & Deep Brown",
            bg = 0xFF1E160C, bgEnd = 0xFF171009, surface = 0xFF2A2013, surfaceVariant = 0xFF33281A,
            accent = 0xFFE2BC6A, accentSoft = 0xFFF4DCA6, accentDeep = 0xFFAA8340,
            text = 0xFFFAF2E4, textSec = 0xFFD9C8AC,
            motifType = ThemeMotifType.GOLDEN_DESERT,
            cardStyle = NoorCardStyle(radius = 16.dp, borderWidth = 1.dp, borderAlpha = 0.25f, glowColor = Color(0xFFE2BC6A))
        ),

        // ─────────── 10 — الزيتون ───────────
        theme(
            "olive", "الزيتون", "Olive Branch", "طبيعي مريح للعين — زيتوني داكن على كريمي هادئ", "Eye-Comfort Natural — Dark Olive & Soft Cream",
            bg = 0xFF101508, bgEnd = 0xFF0C1006, surface = 0xFF1A2110, surfaceVariant = 0xFF222A16,
            accent = 0xFFC9B45A, accentSoft = 0xFFE5D68E, accentDeep = 0xFF93823D,
            text = 0xFFF6F3E6, textSec = 0xFFCDC8B2,
            motifType = ThemeMotifType.OLIVE_GARDEN,
            cardStyle = NoorCardStyle(radius = 18.dp, borderWidth = 1.dp, borderAlpha = 0.20f, glowColor = Color(0xFFC9B45A))
        ),

        // ─────────── 11 — الفيروز الإسلامي ───────────
        theme(
            "islamic_turquoise", "الفيروز الإسلامي", "Islamic Turquoise", "إسلامي حديث مشرق — تركواز على فيروزي وأبيض", "Bright Modern Islamic — Turquoise & Gold",
            bg = 0xFF05302E, bgEnd = 0xFF042624, surface = 0xFF094540, surfaceVariant = 0xFF0B524C,
            accent = 0xFFD9B54A, accentSoft = 0xFFF2DC8E, accentDeep = 0xFFA28430,
            text = 0xFFEFFBF9, textSec = 0xFFB5DBD6,
            motifType = ThemeMotifType.TURQUOISE_ARCH,
            cardStyle = NoorCardStyle(radius = 16.dp, borderWidth = 1.2.dp, borderAlpha = 0.30f, glowColor = Color(0xFFD9B54A), isFramed = true)
        ),

        // ─────────── 12 — البنفسج الروحاني ───────────
        theme(
            "spiritual_violet", "البنفسج الروحاني", "Spiritual Violet", "روحاني فاخر — بنفسجي عميق بذهب فاتح", "Spiritual Luxe — Deep Violet with Light Gold",
            bg = 0xFF170E2A, bgEnd = 0xFF110A20, surface = 0xFF221741, surfaceVariant = 0xFF2A1C4E,
            accent = 0xFFE4C565, accentSoft = 0xFFF6E4A3, accentDeep = 0xFFAB8A3C,
            text = 0xFFF6F2FB, textSec = 0xFFC9BEDF,
            motifType = ThemeMotifType.SPIRITUAL_VIOLET,
            cardStyle = NoorCardStyle(radius = 20.dp, borderWidth = 1.dp, borderAlpha = 0.28f, glowColor = Color(0xFFE4C565))
        ),

        // ─────────── 13 — زمرد الليل ───────────
        theme(
            "night_emerald", "زمرد الليل", "Night Emerald", "Dark Premium — زمردي داكن على أسود بذهب", "Dark Premium — Deep Emerald on Black with Gold",
            bg = 0xFF041410, bgEnd = 0xFF020C09, surface = 0xFF0A211A, surfaceVariant = 0xFF0D2921,
            accent = 0xFFD6B345, accentSoft = 0xFFF1D980, accentDeep = 0xFF9F8030,
            text = 0xFFF0FAF6, textSec = 0xFFB4CFC4,
            motifType = ThemeMotifType.NIGHT_EMERALD,
            cardStyle = NoorCardStyle(radius = 16.dp, borderWidth = 1.dp, borderAlpha = 0.25f, glowColor = Color(0xFFD6B345))
        ),

        // ─────────── 14 — الفجر الوردي ───────────
        theme(
            "pink_dawn", "الفجر الوردي", "Pink Dawn", "نعومة هادئة راقية — وردي فجر مع بنفسجي فاتح وسماوي", "Soft Serenity — Dawn Pink & Light Violet",
            bg = 0xFF241420, bgEnd = 0xFF1C1019, surface = 0xFF33202D, surfaceVariant = 0xFF3C2635,
            accent = 0xFFE5B896, accentSoft = 0xFFF6D9C4, accentDeep = 0xFFB07E5F,
            text = 0xFFFDF3F0, textSec = 0xFFE0C1C2,
            motifType = ThemeMotifType.PINK_DAWN,
            cardStyle = NoorCardStyle(radius = 20.dp, borderWidth = 1.dp, borderAlpha = 0.18f, glowColor = Color(0xFFE5B896))
        ),

        // ─────────── 15 — البحر الإسلامي ───────────
        theme(
            "islamic_sea", "البحر الإسلامي", "Islamic Sea", "منعش وهادئ — أزرق بحري عميق بتركوازي وذهب", "Calm Ocean — Deep Sea Blue with Gold",
            bg = 0xFF052338, bgEnd = 0xFF041B2C, surface = 0xFF0A3350, surfaceVariant = 0xFF0C3D5E,
            accent = 0xFFDDB654, accentSoft = 0xFFF5DC92, accentDeep = 0xFFA58434,
            text = 0xFFEFF7FC, textSec = 0xFFB3CFE0,
            motifType = ThemeMotifType.ISLAMIC_SEA,
            cardStyle = NoorCardStyle(radius = 16.dp, borderWidth = 1.dp, borderAlpha = 0.22f, glowColor = Color(0xFFDDB654))
        ),

        // ─────────── 16 — المخطوطات ───────────
        theme(
            "manuscripts", "المخطوطات", "Manuscripts", "تراث مخطوطات فخم — ورق عتيق وبني ذهبي وأخضر داكن", "Manuscript Heritage — Aged Parchment & Gold Brown",
            bg = 0xFF171009, bgEnd = 0xFF120B06, surface = 0xFF241A0E, surfaceVariant = 0xFF2C2112,
            accent = 0xFFC99B4A, accentSoft = 0xFFE4C683, accentDeep = 0xFF96702F,
            text = 0xFFFAF0DC, textSec = 0xFFD8C6A4,
            motifType = ThemeMotifType.MANUSCRIPTS,
            cardStyle = NoorCardStyle(radius = 14.dp, borderWidth = 1.5.dp, borderAlpha = 0.35f, glowColor = Color(0xFFC99B4A), isFramed = true)
        ),

        // ─────────── 17 — قبة الزمرد ───────────
        theme(
            "emerald_dome", "قبة الزمرد", "Emerald Dome", "كلاسيكي فاخر — قبة زمردية بذهب وأبيض دافئ", "Classic Luxe — Emerald Dome with Gold",
            bg = 0xFF063D2C, bgEnd = 0xFF052E22, surface = 0xFF0A523C, surfaceVariant = 0xFF0C5E46,
            accent = 0xFFE3C15C, accentSoft = 0xFFF5DE9B, accentDeep = 0xFFAC8B38,
            text = 0xFFF1FBF7, textSec = 0xFFBCE0D1,
            motifType = ThemeMotifType.EMERALD_GEM,
            cardStyle = NoorCardStyle(radius = 18.dp, borderWidth = 1.2.dp, borderAlpha = 0.30f, glowColor = Color(0xFFE3C15C), isFramed = true)
        ),

        // ─────────── 18 — رمضان ───────────
        theme(
            "ramadan", "رمضان", "Ramadan", "روحاني احتفالي متزن — فانوس ليلي بنفسجي بأزرق وذهب", "Festive Spiritual — Night Violet, Blue & Gold",
            bg = 0xFF0B1230, bgEnd = 0xFF1A0F3D, surface = 0xFF131C49, surfaceVariant = 0xFF182355,
            accent = 0xFFE7C05C, accentSoft = 0xFFF8E29B, accentDeep = 0xFFAE8E3A,
            text = 0xFFF3F1FC, textSec = 0xFFC0BCE0,
            motifType = ThemeMotifType.RAMADAN_LANTERN,
            cardStyle = NoorCardStyle(radius = 18.dp, borderWidth = 1.5.dp, borderAlpha = 0.35f, glowColor = Color(0xFFE7C05C), isFramed = true)
        ),

        // ─────────── 19 — الهلال الليلي ───────────
        theme(
            "night_crescent", "الهلال الليلي", "Night Crescent", "Dark Mode فاخر — أسود كحلي بفضي وذهب بسيط", "Luxe Dark Mode — Navy Black with Silver & Gold",
            bg = 0xFF06080D, bgEnd = 0xFF04050A, surface = 0xFF0D1220, surfaceVariant = 0xFF111726,
            accent = 0xFFC9CCD6, accentSoft = 0xFFE8EAF0, accentDeep = 0xFF9AA0AE,
            text = 0xFFEFF1F6, textSec = 0xFFB2B7C4,
            motifType = ThemeMotifType.NIGHT_CRESCENT,
            cardStyle = NoorCardStyle(radius = 16.dp, borderWidth = 1.dp, borderAlpha = 0.20f, glowColor = Color(0xFFC9CCD6))
        ),

        // ─────────── 20 — نور Premium ───────────
        theme(
            "noor_premium", "نور Premium", "NOOR Premium", "الثيم المميز — كحلي فاخر بذهب وأبيض ولمسة زمرد", "Signature Theme — Luxe Navy, Gold & Emerald Touch",
            bg = 0xFF0A1226, bgEnd = 0xFF070D1B, surface = 0xFF101B38, surfaceVariant = 0xFF152347,
            accent = 0xFFE5C158, accentSoft = 0xFFF8E29E, accentDeep = 0xFFAC8C3E,
            text = 0xFFFAFAF7, textSec = 0xFFC7CDE0,
            motifType = ThemeMotifType.NOOR_PREMIUM,
            cardStyle = NoorCardStyle(radius = 20.dp, borderWidth = 2.dp, borderAlpha = 0.40f, glowColor = Color(0xFFE5C158), isFramed = true)
        )
    )

    val all: List<NoorThemeDef> = base

    /** أيقونة تعريفية لكل ثيم — شخصية بصرية خاصة لكل ثيمة في المتجر */
    val icons = mapOf(
        "noor_default" to "✨",
        "fajr_noor" to "🌅",
        "emerald_quran" to "📗",
        "golden_mosque" to "🕌",
        "makkah_night" to "🕋",
        "madinah_calm" to "🌙",
        "royal_kaaba" to "🕋",
        "blue_sky" to "☁️",
        "silver_crescent" to "🌙",
        "golden_desert" to "🏜️",
        "olive" to "🫒",
        "islamic_turquoise" to "🔷",
        "spiritual_violet" to "💜",
        "night_emerald" to "🌿",
        "pink_dawn" to "🌸",
        "islamic_sea" to "🌊",
        "manuscripts" to "📜",
        "emerald_dome" to "🏛️",
        "ramadan" to "🏮",
        "night_crescent" to "🌛",
        "noor_premium" to "👑"
    )

    fun byId(id: String): NoorThemeDef? = all.find { it.id == id }
}
