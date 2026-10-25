package com.elhajri.noor.games

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class InteractiveGame(
    val id: String,
    val titleAr: String,
    val titleEn: String,
    val category: String, // One of: "ألعاب ذكاء", "مهارة", "كلمات", "متعة", "أطفال", "تربوية"
    val route: String,
    val icon: ImageVector,
    val accentColor: Color,
    val descriptionAr: String = ""
)

object InteractiveGameCategories {
    const val ALL = "الكل"
    const val INTELLIGENCE = "ألعاب ذكاء"
    const val SKILL = "مهارة"
    const val WORDS = "كلمات"
    const val FUN = "متعة"
    const val KIDS = "أطفال"
    const val EDUCATIONAL = "تربوية"

    val list = listOf(ALL, INTELLIGENCE, SKILL, WORDS, FUN, KIDS, EDUCATIONAL)
}

object GameFavoritesStore {
    private const val PREFS_NAME = "noor_game_favorites_v1"

    fun isFavorite(context: Context, gameId: String): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(gameId, false)
    }

    fun toggleFavorite(context: Context, gameId: String): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val current = prefs.getBoolean(gameId, false)
        val updated = !current
        prefs.edit().putBoolean(gameId, updated).apply()
        return updated
    }
}

object InteractiveGameRegistry {
    val games: List<InteractiveGame> = listOf(
        InteractiveGame(
            id = "game_01_dome_merge",
            titleAr = "دمج المآذن",
            titleEn = "Dome Merge 2048",
            category = InteractiveGameCategories.INTELLIGENCE,
            route = "game_01_dome_merge",
            icon = Icons.Default.AccountBalance,
            accentColor = Color(0xFFFFD700),
            descriptionAr = "دمج القباب والمآذن لبناء المعالم الإسلامية العريقة"
        ),
        InteractiveGame(
            id = "game_02_zellige_match3",
            titleAr = "نقوش الزليج",
            titleEn = "Zellige Match-3",
            category = InteractiveGameCategories.INTELLIGENCE,
            route = "game_02_zellige_match3",
            icon = Icons.Default.GridOn,
            accentColor = Color(0xFF00E676),
            descriptionAr = "مطابقة النقوش الفسيفسائية الهندسية الأندلسية"
        ),
        InteractiveGame(
            id = "game_09_mosque_jigsaw",
            titleAr = "تركيب المسجد",
            titleEn = "Mosque Jigsaw",
            category = InteractiveGameCategories.INTELLIGENCE,
            route = "game_09_mosque_jigsaw",
            icon = Icons.Default.Extension,
            accentColor = Color(0xFF80D8FF),
            descriptionAr = "تجميع قطع الجغسو المعمارية لأشهر المساجد"
        ),
        InteractiveGame(
            id = "game_10_aqueduct_pipes",
            titleAr = "قنوات الماء",
            titleEn = "Aqueduct Pipes",
            category = InteractiveGameCategories.INTELLIGENCE,
            route = "game_10_aqueduct_pipes",
            icon = Icons.Default.Build,
            accentColor = Color(0xFF64FFDA),
            descriptionAr = "توصيل أنابيب شبكات السقاية والري التاريخية"
        ),
        InteractiveGame(
            id = "game_11_minaret_stacker",
            titleAr = "برج المئذنة",
            titleEn = "Minaret Stacker",
            category = InteractiveGameCategories.SKILL,
            route = "game_11_minaret_stacker",
            icon = Icons.Default.Layers,
            accentColor = Color(0xFFFFAB00),
            descriptionAr = "توقيت الدقة لإسقاط طبقات المئذنة وبنائها"
        ),
        InteractiveGame(
            id = "game_12_crescent_cannon",
            titleAr = "مدفع الهلال",
            titleEn = "Crescent Cannon",
            category = InteractiveGameCategories.SKILL,
            route = "game_12_crescent_cannon",
            icon = Icons.Default.SportsBasketball,
            accentColor = Color(0xFFFF5252),
            descriptionAr = "تعديل زوايا الإطلاق والتنشين نحو النجوم والهلال"
        ),
        InteractiveGame(
            id = "game_22_calligraphy_trace",
            titleAr = "خطاط النور",
            titleEn = "Calligraphy Trace",
            category = InteractiveGameCategories.WORDS,
            route = "game_22_calligraphy_trace",
            icon = Icons.Default.Edit,
            accentColor = Color(0xFFFFD700),
            descriptionAr = "تخطيط وتتبع الخط العربي ورسم الحروف الإسلامية"
        ),
        InteractiveGame(
            id = "game_30_lantern_pop",
            titleAr = "فوانيس رمضان",
            titleEn = "Lantern Pop",
            category = InteractiveGameCategories.FUN,
            route = "game_30_lantern_pop",
            icon = Icons.Default.Lightbulb,
            accentColor = Color(0xFFFFC400),
            descriptionAr = "فرقعة الفوانيس المضيئة الصاعدة بلمسات سريعة"
        ),
        InteractiveGame(
            id = "game_31_oasis_merger",
            titleAr = "واحة السلام",
            titleEn = "Oasis Merge Board",
            category = InteractiveGameCategories.FUN,
            route = "game_31_oasis_merger",
            icon = Icons.Default.Spa,
            accentColor = Color(0xFF00E676),
            descriptionAr = "دمج العناصر وتطوير النخيل والعيون في الواحة"
        ),
        InteractiveGame(
            id = "game_lantern_memory",
            titleAr = "ذاكرة الفوانيس",
            titleEn = "Lantern Memory",
            category = InteractiveGameCategories.FUN,
            route = "game_lantern_memory",
            icon = Icons.Default.FlashOn,
            accentColor = Color(0xFFB388FF),
            descriptionAr = "حفظ وإعادة تسلسل أضواء فوانيس رمضان"
        ),
        InteractiveGame(
            id = "game_39_memory_cards",
            titleAr = "بطاقات الذاكرة",
            titleEn = "Memory Cards",
            category = InteractiveGameCategories.KIDS,
            route = "game_39_memory_cards",
            icon = Icons.Default.Style,
            accentColor = Color(0xFFFF80AB),
            descriptionAr = "قلب ومطابقة بطاقات الرسوم الإسلامية اللطيفة"
        ),
        InteractiveGame(
            id = "game_kindness_tap",
            titleAr = "شجرة الحسنات",
            titleEn = "Kindness Tap",
            category = InteractiveGameCategories.EDUCATIONAL,
            route = "game_kindness_tap",
            icon = Icons.Default.Favorite,
            accentColor = Color(0xFF76FF03),
            descriptionAr = "جمع ثمار الخير وتنمية شجرة الصدقات والحسنات"
        )
    )
}
