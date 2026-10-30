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
            id = "game_08_tetris_geometry",
            titleAr = "بناء الأسوار",
            titleEn = "Wall Brick Tetris",
            category = InteractiveGameCategories.INTELLIGENCE,
            route = "game_08_tetris_geometry",
            icon = Icons.Default.ViewCompact,
            accentColor = Color(0xFFFFAB00),
            descriptionAr = "رصف الأحجار المنحوتة لإكمال أسوار المدينة"
        ),
        InteractiveGame(
            id = "game_07_pipe_irrigation",
            titleAr = "قنوات السقيا",
            titleEn = "Oasis Aqueduct Pipes",
            category = InteractiveGameCategories.INTELLIGENCE,
            route = "game_07_pipe_irrigation",
            icon = Icons.Default.Build,
            accentColor = Color(0xFF64FFDA),
            descriptionAr = "تدوير القنوات لتوصيل المياه وسقيا النخيل"
        ),
        InteractiveGame(
            id = "game_06_rubik_arabesque",
            titleAr = "مكعب المقرنصات",
            titleEn = "Muqarnas Sliding Grid",
            category = InteractiveGameCategories.INTELLIGENCE,
            route = "game_06_rubik_arabesque",
            icon = Icons.Default.Grid4x4,
            accentColor = Color(0xFF80D8FF),
            descriptionAr = "تحريك الصفوف والأعمدة لترتيب نقش المقرنصات"
        ),
        InteractiveGame(
            id = "game_05_tangram_star",
            titleAr = "تنجرام النجمة الإسلامية",
            titleEn = "Islamic Star Tangram",
            category = InteractiveGameCategories.INTELLIGENCE,
            route = "game_05_tangram_star",
            icon = Icons.Default.Star,
            accentColor = Color(0xFF00E676),
            descriptionAr = "تركيب الأشكال الهندسية داخل النجمة الإسلامية"
        ),
        InteractiveGame(
            id = "game_04_kaaba_unblock",
            titleAr = "مسار القافلة",
            titleEn = "Caravan Path Unblock",
            category = InteractiveGameCategories.INTELLIGENCE,
            route = "game_04_kaaba_unblock",
            icon = Icons.Default.DirectionsCar,
            accentColor = Color(0xFFFFD700),
            descriptionAr = "تحريك الخشبيات لفتح المسار أمام قافلة الجمال"
        ),
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
            id = "game_48_tasbeeh_clicker",
            titleAr = "حديقة التسبيح",
            titleEn = "Tasbeeh Garden Clicker",
            category = InteractiveGameCategories.EDUCATIONAL,
            route = "game_48_tasbeeh_clicker",
            icon = Icons.Default.FilterVintage,
            accentColor = Color(0xFF64FFDA),
            descriptionAr = "التسبيح والذكر لجمع الحسنات وغرس الأشجار في الجنة"
        ),
        InteractiveGame(
            id = "game_52_caravan_snake",
            titleAr = "قافلة الخير",
            titleEn = "Caravan Snake",
            category = InteractiveGameCategories.EDUCATIONAL,
            route = "game_52_caravan_snake",
            icon = Icons.Default.DirectionsRun,
            accentColor = Color(0xFFFFAB00),
            descriptionAr = "توجيه القافلة بلمسات السحب وتفادي العوائق"
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
,
        InteractiveGame(
            id = "game_13_slingshot_dates",
            titleAr = "مقلاع التمر",
            titleEn = "Date Slingshot",
            category = InteractiveGameCategories.SKILL,
            route = "game_13_slingshot_dates",
            icon = Icons.Default.SportsBasketball,
            accentColor = Color(0xFFFFD700),
            descriptionAr = "إطلاق التمر بالمقلاع لإصابة السلال والأهداف"
        )
        ,
        InteractiveGame(
            id = "game_20_archery_range",
            titleAr = "رماية السهام",
            titleEn = "Desert Archery Range",
            category = InteractiveGameCategories.SKILL,
            route = "game_20_archery_range",
            icon = Icons.Default.SportsEsports,
            accentColor = Color(0xFFFFD700),
            descriptionAr = "Archery precision target shooting. Pull back bowstring, move aim sight over target rings, "
        )
        ,
        InteractiveGame(
            id = "game_32_bubble_crescent",
            titleAr = "فقاعات النور",
            titleEn = "Crescent Bubble Popper",
            category = InteractiveGameCategories.FUN,
            route = "game_32_bubble_crescent",
            icon = Icons.Default.Extension,
            accentColor = Color(0xFF00E676),
            descriptionAr = "Bubble shooter arcade. Aim a bottom crescent cannon at a hanging cluster of colored geomet"
        )
        ,
        InteractiveGame(
            id = "game_29_calligraphy_puzzle",
            titleAr = "ترتيب المخطوطات",
            titleEn = "Calligraphy Unscramble",
            category = InteractiveGameCategories.WORDS,
            route = "game_29_calligraphy_puzzle",
            icon = Icons.Default.AutoAwesome,
            accentColor = Color(0xFF80D8FF),
            descriptionAr = "Horizontal strip unscramble. Drag and swap horizontal sliced image strips of a classical A"
        )
        ,
        InteractiveGame(
            id = "game_55_camel_balance_tilt",
            titleAr = "توازن القافلة",
            titleEn = "Camel Jug Balance",
            category = InteractiveGameCategories.EDUCATIONAL,
            route = "game_55_camel_balance_tilt",
            icon = Icons.Default.Star,
            accentColor = Color(0xFF64FFDA),
            descriptionAr = "Balance slider / tilt equilibrium simulator. Drag a balance bar or tilt device to keep a t"
        )
        ,
        InteractiveGame(
            id = "game_49_charity_sorting",
            titleAr = "صندوق الصدقات",
            titleEn = "Charity Basket Sort",
            category = InteractiveGameCategories.EDUCATIONAL,
            route = "game_49_charity_sorting",
            icon = Icons.Default.EmojiEvents,
            accentColor = Color(0xFFB388FF),
            descriptionAr = "Falling item sorting. Items (bread, winter coat, books, toys) fall from top; tap left/righ"
        )
        ,
        InteractiveGame(
            id = "game_54_chime_ear_train",
            titleAr = "أنغام المنار",
            titleEn = "Minaret Melodic Chimes",
            category = InteractiveGameCategories.EDUCATIONAL,
            route = "game_54_chime_ear_train",
            icon = Icons.Default.Casino,
            accentColor = Color(0xFFFFAB00),
            descriptionAr = "Audio pitch match & chime sequence. Listen to 3 played pentatonic chime notes from minaret"
        )
        ,
        InteractiveGame(
            id = "game_46_connect_constellation",
            titleAr = "نجوم السماء",
            titleEn = "Constellation Connect",
            category = InteractiveGameCategories.KIDS,
            route = "game_46_connect_constellation",
            icon = Icons.Default.Games,
            accentColor = Color(0xFFFF5252),
            descriptionAr = "Connect-the-dots star tracer. Drag finger from star 1 to star 2, 3... in numbered order to"
        )
        ,
        InteractiveGame(
            id = "game_17_crescent_pinning",
            titleAr = "دائرة الهلال",
            titleEn = "Crescent Pin Spinner",
            category = InteractiveGameCategories.SKILL,
            route = "game_17_crescent_pinning",
            icon = Icons.Default.Spa,
            accentColor = Color(0xFF1DE9B6),
            descriptionAr = "Rotating wheel pin insertion (aa style). Tap the screen to shoot numbered pins into a cent"
        )
        ,
        InteractiveGame(
            id = "game_26_crossword_blocks",
            titleAr = "شبكة الكلمات",
            titleEn = "Mini Arabic Crossword",
            category = InteractiveGameCategories.WORDS,
            route = "game_26_crossword_blocks",
            icon = Icons.Default.SportsBasketball,
            accentColor = Color(0xFF82B1FF),
            descriptionAr = "Block fitting crossword grid. Drag multi-letter horizontal or vertical word blocks onto a "
        )
        ,
        InteractiveGame(
            id = "game_36_cut_rope_lantern",
            titleAr = "مصباح الأمل",
            titleEn = "Pendant Rope Cut",
            category = InteractiveGameCategories.FUN,
            route = "game_36_cut_rope_lantern",
            icon = Icons.Default.Rocket,
            accentColor = Color(0xFF76FF03),
            descriptionAr = "Physics rope cutting puzzle (Cut the Rope style). Swipe finger across ropes supporting a s"
        )
        ,
        InteractiveGame(
            id = "game_19_date_slicer",
            titleAr = "حصاد الثمار",
            titleEn = "Fruit Harvest Slicer",
            category = InteractiveGameCategories.SKILL,
            route = "game_19_date_slicer",
            icon = Icons.Default.Bolt,
            accentColor = Color(0xFF18FFFF),
            descriptionAr = "Swipe slicing game (Fruit Ninja style). Swipe finger across flying dates, olives, and pome"
        )
        ,
        InteractiveGame(
            id = "game_15_dome_breaker",
            titleAr = "كاسر الأشكال",
            titleEn = "Dome Brick Breaker",
            category = InteractiveGameCategories.SKILL,
            route = "game_15_dome_breaker",
            icon = Icons.Default.ColorLens,
            accentColor = Color(0xFF536DFE),
            descriptionAr = "Breakout arcade game. Drag a bottom paddle left/right to bounce a crescent orb and destroy"
        )
        ,
        InteractiveGame(
            id = "game_45_duff_rhythm",
            titleAr = "إيقاع الدف",
            titleEn = "Duff Drum Beat",
            category = InteractiveGameCategories.KIDS,
            route = "game_45_duff_rhythm",
            icon = Icons.Default.MusicNote,
            accentColor = Color(0xFF00BFA5),
            descriptionAr = "Single-touch frame drum rhythm pulse. Tap the central Duff drum visualizer exactly when ex"
        )
        ,
        InteractiveGame(
            id = "game_18_flying_dove",
            titleAr = "حمامة السلام",
            titleEn = "Peace Dove Flight",
            category = InteractiveGameCategories.SKILL,
            route = "game_18_flying_dove",
            icon = Icons.Default.Gesture,
            accentColor = Color(0xFFFF80AB),
            descriptionAr = "Flappy altitude control game. Tap the screen to give a peace dove an upward flap impulse a"
        )
        ,
        InteractiveGame(
            id = "game_50_good_deeds_tree",
            titleAr = "شجرة الحسنات",
            titleEn = "Virtue Tree Leaf Catcher",
            category = InteractiveGameCategories.EDUCATIONAL,
            route = "game_50_good_deeds_tree",
            icon = Icons.Default.Lightbulb,
            accentColor = Color(0xFFE0E0E0),
            descriptionAr = "Moving catcher arcade game. Slide a wicker basket left/right at bottom of screen to catch "
        )
        ,
        InteractiveGame(
            id = "game_41_hidden_mosque",
            titleAr = "البحث عن المعالم",
            titleEn = "Hidden Mosque Objects",
            category = InteractiveGameCategories.KIDS,
            route = "game_41_hidden_mosque",
            icon = Icons.Default.Pets,
            accentColor = Color(0xFFFFD700),
            descriptionAr = "Hidden object discovery. Explore a detailed warm illustration of a mosque library/courtyar"
        )
        ,
        InteractiveGame(
            id = "game_25_letter_shift",
            titleAr = "سلسلة الحروف",
            titleEn = "Letter Shift Ladder",
            category = InteractiveGameCategories.WORDS,
            route = "game_25_letter_shift",
            icon = Icons.Default.WaterDrop,
            accentColor = Color(0xFF00E676),
            descriptionAr = "Combination lock letter reels. Tap up/down arrows on vertical letter columns to shift lett"
        )
        ,
        InteractiveGame(
            id = "game_09_medina_maze",
            titleAr = "متاهة المدينة",
            titleEn = "Medina Labyrinth",
            category = InteractiveGameCategories.FUN,
            route = "game_09_medina_maze",
            icon = Icons.Default.Terrain,
            accentColor = Color(0xFF80D8FF),
            descriptionAr = "2D Maze pathfinding. Drag or guide a glowing traveler orb through complex vector maze corr"
        )
        ,
        InteractiveGame(
            id = "game_53_oasis_bridge",
            titleAr = "جسر المحبة",
            titleEn = "Oasis Bridge Architect",
            category = InteractiveGameCategories.EDUCATIONAL,
            route = "game_53_oasis_bridge",
            icon = Icons.Default.Lock,
            accentColor = Color(0xFF64FFDA),
            descriptionAr = "Physics bridge building sandbox. Drag wooden beams and ropes onto structural anchor points"
        )
        ,
        InteractiveGame(
            id = "game_24_petal_word_wheel",
            titleAr = "زهرة الحروف",
            titleEn = "Letter Petal Wheel",
            category = InteractiveGameCategories.WORDS,
            route = "game_24_petal_word_wheel",
            icon = Icons.Default.Favorite,
            accentColor = Color(0xFFB388FF),
            descriptionAr = "Circular letter wheel word builder (Wordscapes style). Drag across letters arranged on a c"
        )
        ,
        InteractiveGame(
            id = "game_16_piano_anachid",
            titleAr = "نغمات الروح",
            titleEn = "Anachid Rhythm Tiles",
            category = InteractiveGameCategories.SKILL,
            route = "game_16_piano_anachid",
            icon = Icons.Default.Visibility,
            accentColor = Color(0xFFFFAB00),
            descriptionAr = "Falling lane rhythm tap (Piano Tiles style). Tap glowing light tiles as they descend along"
        )
        ,
        InteractiveGame(
            id = "game_34_pinball_minaret",
            titleAr = "مغناطيس المنار",
            titleEn = "Minaret Steel Pinball",
            category = InteractiveGameCategories.FUN,
            route = "game_34_pinball_minaret",
            icon = Icons.Default.Camera,
            accentColor = Color(0xFFFF5252),
            descriptionAr = "Pinball physics arcade. Drag back a spring plunger to launch a steel ball into an illumina"
        )
        ,
        InteractiveGame(
            id = "game_27_rising_word_lanterns",
            titleAr = "فوانيس الحروف",
            titleEn = "Rising Word Lanterns",
            category = InteractiveGameCategories.WORDS,
            route = "game_27_rising_word_lanterns",
            icon = Icons.Default.DirectionsRun,
            accentColor = Color(0xFF1DE9B6),
            descriptionAr = "Floating letter balloon pop. Tap rising sky lanterns carrying Arabic letters in the correc"
        )
        ,
        InteractiveGame(
            id = "game_10_scales_of_deeds",
            titleAr = "ميزان التعادل",
            titleEn = "Equilibrium Scale",
            category = InteractiveGameCategories.FUN,
            route = "game_10_scales_of_deeds",
            icon = Icons.Default.Map,
            accentColor = Color(0xFF82B1FF),
            descriptionAr = "Physics balance scale puzzle. Drag weighted brass geometric objects onto dual scale pans t"
        )
        ,
        InteractiveGame(
            id = "game_23_scrambled_blocks",
            titleAr = "مكعبات الحروف",
            titleEn = "Scrambled Letter Blocks",
            category = InteractiveGameCategories.WORDS,
            route = "game_23_scrambled_blocks",
            icon = Icons.Default.Timeline,
            accentColor = Color(0xFF76FF03),
            descriptionAr = "Anagram block unscrambler. Drag scrambled Arabic wooden letter blocks from the bottom tray"
        )
        ,
        InteractiveGame(
            id = "game_47_scratch_reveal",
            titleAr = "كشف المخطوطة",
            titleEn = "Gold Manuscript Scratch",
            category = InteractiveGameCategories.KIDS,
            route = "game_47_scratch_reveal",
            icon = Icons.Default.Widgets,
            accentColor = Color(0xFF18FFFF),
            descriptionAr = "Scratch canvas image reveal. Swipe finger back and forth across a dusty screen canvas to b"
        )
        ,
        InteractiveGame(
            id = "game_44_shape_silhouette",
            titleAr = "ظلال المعالم",
            titleEn = "Minaret Silhouette Match",
            category = InteractiveGameCategories.KIDS,
            route = "game_44_shape_silhouette",
            icon = Icons.Default.Brush,
            accentColor = Color(0xFF536DFE),
            descriptionAr = "Shape silhouette drag & snap puzzle. Drag colorful architectural pieces (crescent, minaret"
        )
        ,
        InteractiveGame(
            id = "game_14_silk_runner",
            titleAr = "عداء طريق الحرير",
            titleEn = "Silk Road Runner",
            category = InteractiveGameCategories.SKILL,
            route = "game_14_silk_runner",
            icon = Icons.Default.Build,
            accentColor = Color(0xFF00BFA5),
            descriptionAr = "Endless 2D side-scrolling runner. Guide a merchant running through desert dunes; swipe up "
        )
        ,
        InteractiveGame(
            id = "game_42_simon_lanterns",
            titleAr = "أنوار الفوانيس",
            titleEn = "Simon Lantern Sequence",
            category = InteractiveGameCategories.KIDS,
            route = "game_42_simon_lanterns",
            icon = Icons.Default.SportsEsports,
            accentColor = Color(0xFFFF80AB),
            descriptionAr = "Simon Says light & sound memory. Watch 4 colorful hanging lanterns flash and chime in sequ"
        )
        ,
        InteractiveGame(
            id = "game_35_spiral_zuma",
            titleAr = "قطار القافلة",
            titleEn = "Caravan Zuma Stream",
            category = InteractiveGameCategories.FUN,
            route = "game_35_spiral_zuma",
            icon = Icons.Default.Extension,
            accentColor = Color(0xFFE0E0E0),
            descriptionAr = "Rolling chain marble shooter (Zuma style). Aim a central date cannon at a rolling spiral c"
        )
        ,
        InteractiveGame(
            id = "game_40_spot_difference",
            titleAr = "افحص الصورة",
            titleEn = "Spot the Difference",
            category = InteractiveGameCategories.KIDS,
            route = "game_40_spot_difference",
            icon = Icons.Default.AutoAwesome,
            accentColor = Color(0xFFFFD700),
            descriptionAr = "Side-by-side visual difference search. Compare two adjacent cartoon illustrations of mosqu"
        )
        ,
        InteractiveGame(
            id = "game_51_water_saver",
            titleAr = "ترشيد الماء",
            titleEn = "Oasis Water Saver",
            category = InteractiveGameCategories.EDUCATIONAL,
            route = "game_51_water_saver",
            icon = Icons.Default.Star,
            accentColor = Color(0xFF00E676),
            descriptionAr = "Timed valve control puzzle. Tap leaky pipe joints and turn valve handles on a timed water "
        )
        ,
        InteractiveGame(
            id = "game_43_whack_seeds",
            titleAr = "بستان الخير",
            titleEn = "Good Seed Whack",
            category = InteractiveGameCategories.KIDS,
            route = "game_43_whack_seeds",
            icon = Icons.Default.EmojiEvents,
            accentColor = Color(0xFF80D8FF),
            descriptionAr = "Whack-a-mole reaction tap. Tap bright sprouting dates and flowers as they pop up randomly "
        )
        ,
        InteractiveGame(
            id = "game_28_word_chain",
            titleAr = "وصلة الحروف",
            titleEn = "Word Chain Link",
            category = InteractiveGameCategories.WORDS,
            route = "game_28_word_chain",
            icon = Icons.Default.Casino,
            accentColor = Color(0xFF64FFDA),
            descriptionAr = "Word connection chain puzzle. Drag connecting lines between word nodes where the last lett"
        )
        ,
        InteractiveGame(
            id = "game_21_word_matrix",
            titleAr = "مسار الكلمات",
            titleEn = "Quranic Word Matrix",
            category = InteractiveGameCategories.WORDS,
            route = "game_21_word_matrix",
            icon = Icons.Default.Games,
            accentColor = Color(0xFFB388FF),
            descriptionAr = "Word search line connector. Drag finger across adjacent letter tiles on a 5x5 grid in any "
        )
        ,
        InteractiveGame(
            id = "game_33_zellige_color_number",
            titleAr = "تلوين الزليج",
            titleEn = "Zellige Color-by-Number",
            category = InteractiveGameCategories.FUN,
            route = "game_33_zellige_color_number",
            icon = Icons.Default.Spa,
            accentColor = Color(0xFFFFAB00),
            descriptionAr = "Paint fill by number canvas. Tap a numbered color swatch from the bottom palette, then tap"
        )
        ,
        InteractiveGame(
            id = "game_38_zigzag_caravan",
            titleAr = "طريق الجبل",
            titleEn = "ZigZag Caravan Trail",
            category = InteractiveGameCategories.FUN,
            route = "game_38_zigzag_caravan",
            icon = Icons.Default.SportsBasketball,
            accentColor = Color(0xFFFF5252),
            descriptionAr = "Direction switcher runner (Tap Tap Dash style). A camel walks forward automatically along "
        )
    )
}
