package com.elhajri.noor.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.elhajri.noor.settings.NoorLocale

/**
 * أنواع البصمات البصرية المستقبلية للثيمات الـ20 (Futuristic Islamic Visual Signatures 2026/2027)
 * رسم كود كلي بالـ Canvas تفاعلي وعالي السرعة (60/120 FPS).
 */
enum class ThemeMotifType {
    CYBER_NOOR_CORE,          // 00 — نور السيبراني 2027: هالة ليزرية ذهبية كوانتمية وشعاع شعاعي
    QUANTUM_EMERALD_GRID,     // 01 — الزمرد الكوانتي: شبكة هندسية زمردية مع قبة ضوئية متوهجة
    OBSIDIAN_KAABA_NEON,      // 02 — الكعبة الأوبسيدية 2027: مجسم كعبة أوبسيدي بحزام نيون ذهبي
    NEON_KUFIC_GOLD,          // 03 — الكوفي النيون: شبكة خط كوفي ثمانية متوهجة بذهب دافئ
    CELESTIAL_MADINAH_VIOLET, // 04 — البنفسج الفلكي: منحنى قبة المدينة ببنفسج فلكي وهالة شفقية
    DIWANI_CYBER_ROSE,        // 05 — الذهب الفتوري الوردي: منحنيات ديوانية سيبرانية بذهب وردي
    HOLOGRAPHIC_ZELLIGE,      // 06 — الزليج الهولوغرامي: نجمة ثمانية متداخلة بألوان هولوغرافية
    ANDALUSIAN_CRYSTAL_GLASS, // 07 — البلور الأندلسي: قوس أندلسي زجاجي كريستالي مع انكسارات ضوء
    SOLAR_FAJR_GLOWING,       // 08 — فجر السولار: هالة بلازما شمسية فجرية بأشعة طاقة متصاعدة
    DEEP_SPACE_CRESCENT,      // 09 — الهلال السحيق: هلال فلكي متوهج في مجرة كحلية سحيقة
    CELESTIAL_SAPPHIRE,       // 10 — الزفير السماوي: شبكة كريستالية بكحلي زفييري متوهج
    ALHAMBRA_RUBY_PULSE,      // 11 — الروبي الحمراء: مخمل أندلسي روبي بنبضات هندسية ذهبية
    AURORA_MINARET,           // 12 — الشفق المئذني: ستارة شفق قطبي خلف مآذن مستقبليّة
    AMBER_DESERT_HORIZON,     // 13 — أفق الصحراء الذهبي: أمواج رملية سيبرانية بأفق عنبري متوهج
    STARLIGHT_MUSHAF,         // 14 — المصحف النجمي: هندسة مصحف مفتوح مع جزيئات نور متلألئة
    TITANIUM_OLIVE,           // 15 — الزيتون التيتانيوم: زيتون تيتانيومي داكن بخطوط نور زمردية
    HOLOGRAPHIC_LANTERN,      // 16 — الفانوس الهولوغرامي: فانوس ثلاثي الأبعاد بلوري بقلب بلازما
    ZENITH_TURQUOISE,         // 17 — السمت الفيروزي: قوس حدوة حصان فيروزي بشبكة طاقة
    INFINITE_INDIGO_TASBIH,   // 18 — الانديجو اللانهائي: حلقات حبات تسبيح نيونية متداخلة
    QUANTUM_QIBLA_EMERALD     // 19 — القبلة الكمومية: بوصلة قبلة متجهة كوانتمية بحلقات نبض
}

/**
 * معلمات تخصيص البطاقات للثيمات الـ20 المستقبليّة.
 */
data class NoorCardStyle(
    val radius: Dp = 18.dp,
    val borderWidth: Dp = 1.dp,
    val borderColor: Color = Color.Unspecified,
    val borderAlpha: Float = 0.30f,
    val elevation: Dp = 4.dp,
    val glowColor: Color? = null,
    val isFramed: Boolean = true
)

/**
 * معلمات أسلوب الهيدر والرأس.
 */
data class NoorHeaderStyle(
    val showAccentRay: Boolean = true,
    val glowIntensity: Float = 0.4f,
    val badgeStyle: String = "FUTURISTIC_2027"
)

/**
 * تعريف الثيم الموحد للثيمات الـ20 المستقبليّة (Futuristic Theme Definition).
 */
data class NoorThemeDef(
    val id: String,
    val nameAr: String,
    val nameEn: String,
    val descAr: String,
    val descEn: String,
    val price: Int = 0,
    val background: Color,
    val backgroundEnd: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val accent: Color,
    val accentSoft: Color,
    val accentDeep: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val success: Color = Color(0xFF10B981),
    val error: Color = Color(0xFFEF4444),
    val backgroundRes: Int = 0,
    val cardRadius: Dp = 18.dp,
    val buttonRadius: Dp = 14.dp,
    val buttonGradient: List<Color> = listOf(accentSoft, accent, accentDeep),
    val motifType: ThemeMotifType = ThemeMotifType.CYBER_NOOR_CORE,
    val motifColor: Color = accent,
    val cardStyle: NoorCardStyle = NoorCardStyle(radius = cardRadius),
    val headerStyle: NoorHeaderStyle = NoorHeaderStyle()
) {
    val name: String get() = if (NoorLocale.isEnglish) nameEn else nameAr
    val description: String get() = if (NoorLocale.isEnglish) descEn else descAr
}

/**
 * حالة الثيم التفاعلية النشطة في التطبيق.
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
