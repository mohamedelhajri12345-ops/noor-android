package com.elhajri.noor.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * كتالوج الثيمات الـ20 المستقبليّة (2026/2027 Futuristic Islamic Theme Store).
 * جميع الثيمات الـ20 مصممة بهوية إسلامية مستقبلية مع ألوان غنية وبصمات رسم كود.
 */
object NoorThemes {

    private fun theme(
        id: String,
        nameAr: String, nameEn: String,
        descAr: String, descEn: String,
        price: Int = ThemeStore.THEME_PRICE,
        bg: Long, bgEnd: Long, surface: Long, surfaceVariant: Long,
        accent: Long, accentSoft: Long, accentDeep: Long,
        text: Long, textSec: Long,
        motifType: ThemeMotifType
    ): NoorThemeDef {
        val accentCol = Color(accent)
        return NoorThemeDef(
            id = id,
            nameAr = nameAr,
            nameEn = nameEn,
            descAr = descAr,
            descEn = descEn,
            price = price,
            background = Color(bg),
            backgroundEnd = Color(bgEnd),
            surface = Color(surface),
            surfaceVariant = Color(surfaceVariant),
            accent = accentCol,
            accentSoft = Color(accentSoft),
            accentDeep = Color(accentDeep),
            textPrimary = Color(text),
            textSecondary = Color(textSec),
            motifType = motifType,
            motifColor = accentCol,
            cardStyle = NoorCardStyle(radius = 18.dp, glowColor = accentCol)
        )
    }

    val all: List<NoorThemeDef> = listOf(

        // 00 — نور السيبراني 2027 (الثيم الافتراضي الرئيسي المجاني)
        theme(
            id = "noor_cyber_2027",
            nameAr = "نور السيبراني 2027",
            nameEn = "Cyber Noor 2027",
            descAr = "الهوية المستقبلية المعتمدة — أوبسيديان أخضر ليلي مع ذهب كوانتمي ناصع",
            descEn = "Flagship 2027 Vision — Night Obsidian Green & Quantum Cyber Gold",
            price = 0,
            bg = 0xFF0A1810, bgEnd = 0xFF040C07,
            surface = 0xFF122E1A, surfaceVariant = 0xFF193D23,
            accent = 0xFFFFD700, accentSoft = 0xFFFFE875, accentDeep = 0xFFC29B00,
            text = 0xFFF7FBF6, textSec = 0xFFBCE3B5,
            motifType = ThemeMotifType.CYBER_NOOR_CORE
        ),

        // 01 — الزمرد الكوانتي
        theme(
            id = "quantum_emerald",
            nameAr = "الزمرد الكوانتي",
            nameEn = "Quantum Emerald",
            descAr = "شبكة زمردية ليزرية مع إشراق مآذن نيونية",
            descEn = "Quantum Laser Emerald Grid with Neon Minaret Radiance",
            bg = 0xFF041F18, bgEnd = 0xFF02120F,
            surface = 0xFF0A3326, surfaceVariant = 0xFF0F4538,
            accent = 0xFF00E676, accentSoft = 0xFF66FFA6, accentDeep = 0xFF00A352,
            text = 0xFFF0FDF8, textSec = 0xFFA7F3D0,
            motifType = ThemeMotifType.QUANTUM_EMERALD_GRID
        ),

        // 02 — الكعبة الأوبسيدية 2027
        theme(
            id = "obsidian_kaaba",
            nameAr = "الكعبة الأوبسيدية 2027",
            nameEn = "Obsidian Kaaba 2027",
            descAr = "أسود أوبسيدي فاخر بحزام كسوة ذهبي ليزري ناصع",
            descEn = "Deep Obsidian Black with Laser Golden Kiswah Belt",
            bg = 0xFF0A0B10, bgEnd = 0xFF030406,
            surface = 0xFF181A24, surfaceVariant = 0xFF222533,
            accent = 0xFFF5C542, accentSoft = 0xFFFFE082, accentDeep = 0xFFC49514,
            text = 0xFFFAFAFD, textSec = 0xFFC9CBDC,
            motifType = ThemeMotifType.OBSIDIAN_KAABA_NEON
        ),

        // 03 — الكوفي النيون
        theme(
            id = "neon_kufic_gold",
            nameAr = "الكوفي النيون",
            nameEn = "Neon Kufic Gold",
            descAr = "خط كوفي هندسي متوهج بذهب دافئ على خلفية داكنة",
            descEn = "Geometric Kufic Geometry glowing in Warm Gold",
            bg = 0xFF17120B, bgEnd = 0xFF0D0905,
            surface = 0xFF281E13, surfaceVariant = 0xFF36281B,
            accent = 0xFFFFB300, accentSoft = 0xFFFFD54F, accentDeep = 0xFFC78100,
            text = 0xFFFFFDF5, textSec = 0xFFE6CFAD,
            motifType = ThemeMotifType.NEON_KUFIC_GOLD
        ),

        // 04 — البنفسج الفلكي
        theme(
            id = "celestial_madinah",
            nameAr = "البنفسج الفلكي",
            nameEn = "Celestial Madinah Violet",
            descAr = "سماء بنفسجية فلكية مع هالة الروضة الشريفة",
            descEn = "Deep Celestial Violet with Rawdah Glowing Aura",
            bg = 0xFF130A24, bgEnd = 0xFF0A0414,
            surface = 0xFF22143A, surfaceVariant = 0xFF2F1D52,
            accent = 0xFFB388FF, accentSoft = 0xFFD1C4E9, accentDeep = 0xFF7C4DFF,
            text = 0xFFFBF8FF, textSec = 0xFFD1C2F0,
            motifType = ThemeMotifType.CELESTIAL_MADINAH_VIOLET
        ),

        // 05 — الذهب الفتوري الوردي
        theme(
            id = "diwani_cyber_rose",
            nameAr = "الذهب الفتوري الوردي",
            nameEn = "Diwani Cyber Rose",
            descAr = "انسيابية خط ديواني نيون بحواف ذهب وردي ملكي",
            descEn = "Fluid Diwani Neon Calligraphy in Royal Rose Gold",
            bg = 0xFF1C0D18, bgEnd = 0xFF0F050D,
            surface = 0xFF2D1627, surfaceVariant = 0xFF3D1F35,
            accent = 0xFFFF80AB, accentSoft = 0xFFFF4081, accentDeep = 0xFFC51162,
            text = 0xFFFFF5F9, textSec = 0xFFF8BBD0,
            motifType = ThemeMotifType.DIWANI_CYBER_ROSE
        ),

        // 06 — الزليج الهولوغرامي
        theme(
            id = "holographic_zellige",
            nameAr = "الزليج الهولوغرامي",
            nameEn = "Holographic Zellige",
            descAr = "زخارف أندلسية ثمانية بطيف ضوئي متوهج",
            descEn = "Spectral 8-Pointed Andalusian Holographic Tessellation",
            bg = 0xFF0D1B2A, bgEnd = 0xFF060D14,
            surface = 0xFF1B2A4A, surfaceVariant = 0xFF243861,
            accent = 0xFF80D8FF, accentSoft = 0xFFB388FF, accentDeep = 0xFF0091EA,
            text = 0xFFF4FAFF, textSec = 0xFFB3E5FC,
            motifType = ThemeMotifType.HOLOGRAPHIC_ZELLIGE
        ),

        // 07 — البلور الأندلسي
        theme(
            id = "andalusian_crystal",
            nameAr = "البلور الأندلسي",
            nameEn = "Andalusian Crystal Glass",
            descAr = "زجاج الكريستال الأندلسي مع انكسارات الضوء الفاخرة",
            descEn = "Frosted Andalusian Crystal Glass with Prism Radiance",
            bg = 0xFF091E22, bgEnd = 0xFF030D10,
            surface = 0xFF123136, surfaceVariant = 0xFF18424A,
            accent = 0xFF64FFDA, accentSoft = 0xFFA7FFEB, accentDeep = 0xFF00BFA5,
            text = 0xFFF2FCFB, textSec = 0xFFB2DFDB,
            motifType = ThemeMotifType.ANDALUSIAN_CRYSTAL_GLASS
        ),

        // 08 — فجر السولار
        theme(
            id = "solar_fajr",
            nameAr = "فجر السولار",
            nameEn = "Solar Fajr Glowing",
            descAr = "أشعة فجر شمسية بلازمية ببصمة ضوئية متصاعدة",
            descEn = "Solar Plasma Dawn Beams with Radiating Aura",
            bg = 0xFF241006, bgEnd = 0xFF120702,
            surface = 0xFF3B1B0B, surfaceVariant = 0xFF4F240F,
            accent = 0xFFFF6D00, accentSoft = 0xFFFFAB40, accentDeep = 0xFFDD2C00,
            text = 0xFFFFF8F5, textSec = 0xFFFFCCBC,
            motifType = ThemeMotifType.SOLAR_FAJR_GLOWING
        ),

        // 09 — الهلال السحيق
        theme(
            id = "deep_space_crescent",
            nameAr = "الهلال السحيق",
            nameEn = "Deep Space Crescent",
            descAr = "سوداوية مجرة سحيقة يتوسطها هلال فلكي متوهج",
            descEn = "Deep Cosmic Void centered by Glowing Cosmic Crescent",
            bg = 0xFF070B19, bgEnd = 0xFF02040B,
            surface = 0xFF111730, surfaceVariant = 0xFF192042,
            accent = 0xFF82B1FF, accentSoft = 0xFFB9D5FF, accentDeep = 0xFF2979FF,
            text = 0xFFF5F8FF, textSec = 0xFFC6DAFE,
            motifType = ThemeMotifType.DEEP_SPACE_CRESCENT
        ),

        // 10 — الزفير السماوي
        theme(
            id = "celestial_sapphire",
            nameAr = "الزفير السماوي",
            nameEn = "Celestial Sapphire",
            descAr = "كحلي زفيري ملكي بأشكال بلورية ثمانية متوهجة",
            descEn = "Royal Sapphire Cobalt with Octagonal Crystal Mesh",
            bg = 0xFF0A1428, bgEnd = 0xFF030814,
            surface = 0xFF12223F, surfaceVariant = 0xFF1A2F54,
            accent = 0xFF448AFF, accentSoft = 0xFF82B1FF, accentDeep = 0xFF2962FF,
            text = 0xFFF5F8FF, textSec = 0xFFBBDEFB,
            motifType = ThemeMotifType.CELESTIAL_SAPPHIRE
        ),

        // 11 — الروبي الحمراء
        theme(
            id = "alhambra_ruby",
            nameAr = "الروبي الحمراء",
            nameEn = "Alhambra Ruby Pulse",
            descAr = "عنابي مخملي أندلسي بنبضات ذهبية راقية",
            descEn = "Velvet Andalusian Crimson Ruby with Golden Pulses",
            bg = 0xFF21060E, bgEnd = 0xFF120207,
            surface = 0xFF360C19, surfaceVariant = 0xFF4A1224,
            accent = 0xFFFF5252, accentSoft = 0xFFFF8A80, accentDeep = 0xFFD50000,
            text = 0xFFFFF5F5, textSec = 0xFFFFCDD2,
            motifType = ThemeMotifType.ALHAMBRA_RUBY_PULSE
        ),

        // 12 — الشفق المئذني
        theme(
            id = "aurora_minaret",
            nameAr = "الشفق المئذني",
            nameEn = "Aurora Minaret",
            descAr = "ستارة شفق قطبي أخضر وأزرق خلف ظلال المآذن",
            descEn = "Ethereal Aurora Borealis Waves behind Minaret Silhouettes",
            bg = 0xFF051D1A, bgEnd = 0xFF010E0C,
            surface = 0xFF0D302B, surfaceVariant = 0xFF13423B,
            accent = 0xFF1DE9B6, accentSoft = 0xFFA7FFEB, accentDeep = 0xFF00BFA5,
            text = 0xFFF0FDFB, textSec = 0xFFB2DFDB,
            motifType = ThemeMotifType.AURORA_MINARET
        ),

        // 13 — أفق الصحراء الذهبي
        theme(
            id = "amber_desert",
            nameAr = "أفق الصحراء الذهبي",
            nameEn = "Amber Desert Horizon",
            descAr = "كثبان عنبرية سيبرانية بأفق دافئ ناصع الإضاءة",
            descEn = "Cyber Amber Dunes with Warm Glowing Horizon Line",
            bg = 0xFF1E1408, bgEnd = 0xFF100B03,
            surface = 0xFF302110, surfaceVariant = 0xFF402D18,
            accent = 0xFFFFAB00, accentSoft = 0xFFFFD740, accentDeep = 0xFFFF6D00,
            text = 0xFFFFFDF5, textSec = 0xFFFFECB3,
            motifType = ThemeMotifType.AMBER_DESERT_HORIZON
        ),

        // 14 — المصحف النجمي
        theme(
            id = "starlight_mushaf",
            nameAr = "المصحف النجمي",
            nameEn = "Starlight Mushaf",
            descAr = "نور مصحف كحلي يتلألأ بجزيئات نجمية دقيقة",
            descEn = "Midnight Navy Mushaf shimmering with Starlight Particles",
            bg = 0xFF081220, bgEnd = 0xFF030912,
            surface = 0xFF122036, surfaceVariant = 0xFF1A2C4A,
            accent = 0xFFE0E0E0, accentSoft = 0xFFFFFFFF, accentDeep = 0xFF9E9E9E,
            text = 0xFFFFFFFF, textSec = 0xFFCFD8DC,
            motifType = ThemeMotifType.STARLIGHT_MUSHAF
        ),

        // 15 — الزيتون التيتانيوم
        theme(
            id = "titanium_olive",
            nameAr = "الزيتون التيتانيوم",
            nameEn = "Titanium Olive",
            descAr = "معدن التيتانيوم الداكن مع خطوط نور زيتونية متوهجة",
            descEn = "Dark Metallic Titanium with Glowing Olive Energy Lines",
            bg = 0xFF111812, bgEnd = 0xFF080C08,
            surface = 0xFF1D261E, surfaceVariant = 0xFF263328,
            accent = 0xFF76FF03, accentSoft = 0xFFB2FF59, accentDeep = 0xFF64DD17,
            text = 0xFFF7FCF5, textSec = 0xFFDCEDC8,
            motifType = ThemeMotifType.TITANIUM_OLIVE
        ),

        // 16 — الفانوس الهولوغرامي
        theme(
            id = "holographic_lantern",
            nameAr = "الفانوس الهولوغرامي",
            nameEn = "Holographic Lantern",
            descAr = "فانوس رمضاني ثلاثي الأبعاد بهالة بلازما حية",
            descEn = "3D Wireframe Ramadan Lantern with Radiant Plasma Core",
            bg = 0xFF1C1305, bgEnd = 0xFF0F0A02,
            surface = 0xFF2D200B, surfaceVariant = 0xFF3D2E12,
            accent = 0xFFFFC400, accentSoft = 0xFFFFE57F, accentDeep = 0xFFFFAB00,
            text = 0xFFFFFDF5, textSec = 0xFFFFECB3,
            motifType = ThemeMotifType.HOLOGRAPHIC_LANTERN
        ),

        // 17 — السمت الفيروزي
        theme(
            id = "zenith_turquoise",
            nameAr = "السمت الفيروزي",
            nameEn = "Zenith Turquoise",
            descAr = "فيروزي إسلامي ناصع بأقواس أندلسية مستقبلية",
            descEn = "Vibrant Islamic Turquoise with Futuristic Andalusian Arches",
            bg = 0xFF041C24, bgEnd = 0xFF010E12,
            surface = 0xFF0A2E3B, surfaceVariant = 0xFF0F3E50,
            accent = 0xFF18FFFF, accentSoft = 0xFF84FFFF, accentDeep = 0xFF00E5FF,
            text = 0xFFF0FFFF, textSec = 0xFFB2EBF2,
            motifType = ThemeMotifType.ZENITH_TURQUOISE
        ),

        // 18 — الانديجو اللانهائي
        theme(
            id = "infinite_indigo_tasbih",
            nameAr = "الانديجو اللانهائي",
            nameEn = "Infinite Indigo Tasbih",
            descAr = "حبات تسبيح نيونية متداخلة في أعمق درجات الانديجو",
            descEn = "Overlapping Neon Tasbih Bead Rings in Deepest Indigo",
            bg = 0xFF0B0E28, bgEnd = 0xFF040614,
            surface = 0xFF161A40, surfaceVariant = 0xFF1E2457,
            accent = 0xFF536DFE, accentSoft = 0xFF8C9EFF, accentDeep = 0xFF3D5AFE,
            text = 0xFFF5F6FF, textSec = 0xFFC5CAE9,
            motifType = ThemeMotifType.INFINITE_INDIGO_TASBIH
        ),

        // 19 — القبلة الكمومية
        theme(
            id = "quantum_qibla_emerald",
            nameAr = "القبلة الكمومية",
            nameEn = "Quantum Qibla Emerald",
            descAr = "بوصلة قبلة اتجاهية ليزرية بنبض كوانتمي زمردي",
            descEn = "Directional Laser Qibla Compass with Quantum Emerald Pulses",
            bg = 0xFF021B14, bgEnd = 0xFF010D0A,
            surface = 0xFF072E22, surfaceVariant = 0xFF0B3E30,
            accent = 0xFF00BFA5, accentSoft = 0xFF64FFDA, accentDeep = 0xFF00897B,
            text = 0xFFF0FDF9, textSec = 0xFFA7F3D0,
            motifType = ThemeMotifType.QUANTUM_QIBLA_EMERALD
        )
    )

    fun byId(id: String): NoorThemeDef? = all.find { it.id == id }
}
