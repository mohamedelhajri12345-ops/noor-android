package com.elhajri.noor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

import com.elhajri.noor.athkar.AthkarDetailScreen
import com.elhajri.noor.athkar.AthkarScreen
import com.elhajri.noor.auth.ForgotPasswordScreen
import com.elhajri.noor.auth.LoginScreen
import com.elhajri.noor.auth.ResetPasswordScreen
import com.elhajri.noor.calendar.CalendarScreen
import com.elhajri.noor.community.CommunityScreen
import com.elhajri.noor.donation.DonationScreen
import com.elhajri.noor.favorites.FavoritesScreen
import com.elhajri.noor.games.NoorGameHubScreen
import com.elhajri.noor.games.WordSearchGame
import com.elhajri.noor.games.TawafOrderGame
import com.elhajri.noor.games.WuduStepsGame
import com.elhajri.noor.games.PrayerOrderGame
import com.elhajri.noor.games.DuasChainGame
import com.elhajri.noor.games.DeedsBalanceGame
import com.elhajri.noor.games.HijriMonthsGame
import com.elhajri.noor.games.JuzOrderGame
import com.elhajri.noor.games.LanternMemoryGame
import com.elhajri.noor.games.IsraJourneyGame
import com.elhajri.noor.games.WisdomPearlsGame
import com.elhajri.noor.games.ZakatCalcGame
import com.elhajri.noor.games.HijriDatesGame
import com.elhajri.noor.games.AngelsAndUnseenQuizGame
import com.elhajri.noor.games.FastingRulesGame
import com.elhajri.noor.games.KindnessTapGame
import com.elhajri.noor.games.DoctrineQuizGameScreen
import com.elhajri.noor.games.FiqhQuizGameScreen
import com.elhajri.noor.games.SirahQuizGameScreen
import com.elhajri.noor.games.CompanionsQuizGameScreen
import com.elhajri.noor.games.RamadanQuizGameScreen
import com.elhajri.noor.games.HajjQuizGameScreen
import com.elhajri.noor.games.MatchNamesMeaningGameScreen
import com.elhajri.noor.games.MatchSurahTopicsGameScreen
import com.elhajri.noor.games.MatchWorshipGameScreen
import com.elhajri.noor.games.MatchAthkarTimesGameScreen
import com.elhajri.noor.games.MatchMosqueCitiesGameScreen
import com.elhajri.noor.games.MatchIslamicMonthsGameScreen
import com.elhajri.noor.games.ScrambleProphetsGameScreen
import com.elhajri.noor.games.ScrambleSurahsGameScreen
import com.elhajri.noor.games.ScrambleNamesAllahGameScreen
import com.elhajri.noor.games.ScrambleRamadanGameScreen
import com.elhajri.noor.games.ProphetsQuizGameScreen
import com.elhajri.noor.games.QuranStoriesQuizGameScreen
import com.elhajri.noor.games.MannersQuizGameScreen
import com.elhajri.noor.games.PrayerQuizGameScreen
import com.elhajri.noor.games.HadithQuizGameScreen
import com.elhajri.noor.games.AthkarQuizGameScreen
import com.elhajri.noor.games.KalimatGameScreen
import com.elhajri.noor.games.TriviaLadderGameScreen
import com.elhajri.noor.games.AyatChainGameScreen
import com.elhajri.noor.games.NamesMatchGameScreen
import com.elhajri.noor.games.TimelineGameScreen
import com.elhajri.noor.games.TartilMirrorGameScreen
import com.elhajri.noor.games.ProphetsOrderGameScreen
import com.elhajri.noor.games.TrueFalseGameScreen
import com.elhajri.noor.games.NumbersQuizGameScreen
import com.elhajri.noor.games.QuranMemorizationGameScreen
import com.elhajri.noor.games.NamesOfAllahGameScreen
import com.elhajri.noor.games.ProphetsJourneyGameScreen
import com.elhajri.noor.hajj.HajjGuideScreen
import com.elhajri.noor.home.HomeScreen
import com.elhajri.noor.journal.JournalScreen
import com.elhajri.noor.more.MoreScreen
import com.elhajri.noor.names.NamesOfAllahScreen
import com.elhajri.noor.nav.NoorBottomBar
import com.elhajri.noor.nav.bottomBarTabs
import com.elhajri.noor.notification.NotificationCenterScreen
import com.elhajri.noor.prayer.PrayerScreen
import com.elhajri.noor.privacy.PrivacyPolicyScreen
import com.elhajri.noor.qibla.QiblaScreen
import com.elhajri.noor.quran.QuranReaderScreen
import com.elhajri.noor.analytics.AnalyticsDashboardScreen
import com.elhajri.noor.exams.ExamsRootScreen
import com.elhajri.noor.settings.SettingsScreen
import com.elhajri.noor.stories.StoriesScreen
import com.elhajri.noor.stories.StoryDetailScreen
import com.elhajri.noor.tasbih.TasbihScreen
import com.elhajri.noor.ui.NoorPlayerBar
import com.elhajri.noor.ui.NoorSplash
import com.elhajri.noor.ui.NoorTheme
import com.elhajri.noor.ui.NoorTopBar
import com.elhajri.noor.zakat.ZakatScreen

class MainActivity : ComponentActivity() {

    var pendingSharedVideoId by mutableStateOf<String?>(null)
        private set

    fun clearSharedVideoId() {
        pendingSharedVideoId = null
    }

    private fun checkShareIntent(intent: android.content.Intent?) {
        if (intent == null || intent.action != android.content.Intent.ACTION_SEND) return
        if (intent.type != "text/plain") return
        val text = intent.getStringExtra(android.content.Intent.EXTRA_TEXT) ?: return

        val patterns = listOf(
            Regex("""(?:v=|/v/|/embed/|youtu\.be/|/shorts/)([a-zA-Z0-9_-]{11})"""),
            Regex("""^[a-zA-Z0-9_-]{11}$""")
        )
        for (pattern in patterns) {
            val match = pattern.find(text)
            if (match != null) {
                pendingSharedVideoId = match.groupValues[1]
                break
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        handleAuthRedirect(intent)
        checkShareIntent(intent)
    }

    /** Google sign-in deep link removed (broken flow) — kept as no-op for safety. */
    private fun handleAuthRedirect(intent: android.content.Intent?) {
        // no-op: Google OAuth login was removed by the owner's decision (it did not work).
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkShareIntent(intent)
        com.elhajri.noor.auth.Base44Auth.init(applicationContext)
        com.elhajri.noor.settings.NoorLocale.init(applicationContext)
        com.elhajri.noor.audio.player.QuranPlayerManager.ensure(applicationContext)
        com.elhajri.noor.audio.player.NasheedPlayerManager.ensure(applicationContext)

        // Android 13+: ask for the notification permission (needed for adhan alerts)
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            androidx.core.app.ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                3001
            )
        }
        // schedule the next adhan alert as soon as the app opens (timings fetch caches itself)
        Thread {
            try {
                // Use the user's SAVED city (not default Mecca) so the cache and adhan match the location
                if (com.elhajri.noor.data.Prefs.isAutoLocation(this@MainActivity)) {
                    com.elhajri.noor.prayer.NoorLocationManager.updateAutoLocation(this@MainActivity)
                }
                val savedCity = com.elhajri.noor.data.Prefs.getCity(this@MainActivity)
                kotlinx.coroutines.runBlocking {
                    com.elhajri.noor.prayer.PrayerRepository.getTimings(
                        this@MainActivity,
                        lat = savedCity?.lat ?: 21.4225,
                        lng = savedCity?.lng ?: 39.8262,
                        country = savedCity?.country ?: "",
                        timezone = savedCity?.timezone ?: ""
                    )
                }
                com.elhajri.noor.notification.AdhanScheduler.scheduleNextAdhan(this@MainActivity)
                com.elhajri.noor.notification.NoorReminders.schedule(this@MainActivity)
                com.elhajri.noor.games.DailyChallengeNotificationManager.scheduleNotification(this@MainActivity)
            } catch (_: Exception) {}
        }.start()
        handleAuthRedirect(intent)

        setContent {
            NoorTheme {
                CompositionLocalProvider(
                    LocalLayoutDirection provides (if (com.elhajri.noor.settings.NoorLocale.isEnglish) LayoutDirection.Ltr else LayoutDirection.Rtl),
                    com.elhajri.noor.settings.LocalStrings provides (if (com.elhajri.noor.settings.NoorLocale.isEnglish) com.elhajri.noor.settings.NoorStringsEn else com.elhajri.noor.settings.NoorStringsAr)
                ) {
                    // حجم الخط من الإعدادات — يتغير فوراً عند اختياره
                    val density = androidx.compose.ui.platform.LocalDensity.current
                    val fontScale = com.elhajri.noor.settings.FontScaleHolder.scale
                    androidx.compose.runtime.CompositionLocalProvider(
                        androidx.compose.ui.platform.LocalDensity provides
                            androidx.compose.ui.unit.Density(density.density, density.fontScale * fontScale)
                    ) {
                        NoorApp()
                    }
                }
            }
        }
    }
}

@Composable
fun NoorApp() {
    var showSplash by remember { mutableStateOf(true) }
    val navController = rememberNavController()
    val appCtx = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(Unit) {
        com.elhajri.noor.ads.InterstitialAds.preload(appCtx)
        com.elhajri.noor.analytics.AnalyticsTracker.trackSessionStart(appCtx)
        while (true) {
            kotlinx.coroutines.delay(60000L)
            com.elhajri.noor.analytics.AnalyticsTracker.trackUsageMinutes(appCtx, 1)
        }
    }
    val sharedVideoId = (appCtx as? MainActivity)?.pendingSharedVideoId
    LaunchedEffect(sharedVideoId) {
        if (!sharedVideoId.isNullOrEmpty()) {
            navController.navigate("videoPlayer/$sharedVideoId") {
                launchSingleTop = true
            }
            (appCtx as? MainActivity)?.clearSharedVideoId()
        }
    }
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val mainRoutes = remember { bottomBarTabs().map { it.route }.toSet() }
    val showBar = currentRoute in mainRoutes

    // التنقل الآمن للشاشات الفرعية — يمنع فتح نفس الشاشة مرتين مع تجاوب فوري
    val navigateSafe: (String) -> Unit = { route ->
        val activeRoute = navController.currentDestination?.route
        if (activeRoute != route) {
            try {
                navController.navigate(route) {
                    launchSingleTop = true
                }
            } catch (_: Exception) {}
        }
    }

    // الإعلان البيني المهذب: يظهر فقط عند العودة للرئيسية من قسم الترفيه
    // (الألعاب أو الفيديوهات) — أي في نهاية جلسة طبيعية، وبحد أقصى مرة كل
    // ربع ساعة (التحقق داخل InterstitialAds) ولا يظهر أبداً أثناء أي تشغيل.
    val scope = rememberCoroutineScope()
    val appContext = androidx.compose.ui.platform.LocalContext.current
    val politeInterstitial: (String?) -> Unit = { fromRoute ->
        val mediaActive = com.elhajri.noor.audio.player.NasheedPlayerManager.state.value.isPlaying ||
                com.elhajri.noor.audio.player.QuranPlayerManager.state.value.isPlaying
        if ((fromRoute == "games" || fromRoute == "videos") && !mediaActive) {
            scope.launch {
                delay(900) // let the navigation settle first — ad appears on the calm home screen
                (appContext as? android.app.Activity)?.let { activity ->
                    com.elhajri.noor.ads.InterstitialAds.maybeShow(activity)
                }
            }
        }
    }

    // التنقل الموثوق لشريط التنقل السفلي — يعمل بدقة 100% في كل الأوقات وعلى كل الأجهزة
    val onTabSelected: (String) -> Unit = { targetRoute ->
        val activeRoute = navController.currentDestination?.route
        if (activeRoute != targetRoute) {
            politeInterstitial(activeRoute)
            try {
                val startDestId = navController.graph.findStartDestination().id
                navController.navigate(targetRoute) {
                    popUpTo(startDestId) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            } catch (_: Exception) {
                navController.navigate(targetRoute) {
                    launchSingleTop = true
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                if (showBar) NoorTopBar(
                    onCommunity = { navigateSafe("community") },
                    onDonate = { navigateSafe("donation") },
                    onAssistant = { navigateSafe("ai") },
                    onSettings = { navigateSafe("settings") },
                    onProfile = { navigateSafe("profile") }
                )
            },
            bottomBar = {
                if (showBar) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // player bar (mini) — directly above the bottom bar, like the web
                        NoorPlayerBar()
                        // 2026 Modern Glass Bottom Bar with animated active indicator & theme-aware icons
                        NoorBottomBar(
                            currentRoute = currentRoute,
                            onTabSelected = onTabSelected
                        )
                    }
                }
            }
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = "home",
                modifier = Modifier.padding(padding),
                // انتقال ناعم موحّد: ظهور بتلاشٍ ورفعٍ خفيف — هوية بصرية راقية
                enterTransition = {
                    androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(240)) +
                        androidx.compose.animation.slideInVertically(
                            animationSpec = androidx.compose.animation.core.tween(240)
                        ) { it / 24 }
                },
                exitTransition = { androidx.compose.animation.fadeOut(androidx.compose.animation.core.tween(180)) },
                popEnterTransition = { androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(240)) },
                popExitTransition = {
                    androidx.compose.animation.fadeOut(androidx.compose.animation.core.tween(180)) +
                        androidx.compose.animation.slideOutVertically(
                            animationSpec = androidx.compose.animation.core.tween(200)
                        ) { it / 24 }
                }
            ) {
                composable("home") { HomeScreen(onNavigate = { navigateSafe(it) }) }
                composable("prayer") { PrayerScreen() }
                composable("quran") { com.elhajri.noor.analytics.AnalyticsTracker.trackFeatureOpen(appCtx, "quran"); com.elhajri.noor.quran.QuranScreen(onSurahClick = { surah -> navController.navigate("reader/${surah.number}/${surah.name}") }) }
                composable("athkar") { AthkarScreen(onOpenCategory = { cat, title -> navController.navigate("athkarDetail/$cat/$title") }) }
                composable("more") { MoreScreen(onNavigate = { navigateSafe(it) }) }

                composable("qibla") { QiblaScreen() }
                composable("tasbih") { com.elhajri.noor.analytics.AnalyticsTracker.trackFeatureOpen(appCtx, "tasbih"); TasbihScreen(onBack = { navController.popBackStack() }) }
                composable("names") { NamesOfAllahScreen() }
                composable("quiz") { com.elhajri.noor.analytics.AnalyticsTracker.trackFeatureOpen(appCtx, "exam"); ExamsRootScreen(onBack = { navController.popBackStack() }) }
                composable("exams") { com.elhajri.noor.analytics.AnalyticsTracker.trackFeatureOpen(appCtx, "exam"); ExamsRootScreen(onBack = { navController.popBackStack() }) }
                composable("stories") { StoriesScreen(onOpenStory = { id -> navController.navigate("story/$id") }) }
                composable("profile") { com.elhajri.noor.analytics.AnalyticsTracker.trackFeatureOpen(appCtx, "profile"); com.elhajri.noor.profile.ProfileScreen(onBack = { navController.popBackStack() }, onNavigate = { navigateSafe(it) }) }
                composable("settings") { com.elhajri.noor.analytics.AnalyticsTracker.trackFeatureOpen(appCtx, "settings"); SettingsScreen(onOpenPrivacy = { navController.navigate("privacy") }) }
                composable("themes") { com.elhajri.noor.theme.ThemeStoreScreen(onOpenEarnPoints = { navController.navigate("earn") }) }
                composable("earn") { com.elhajri.noor.theme.EarnPointsScreen(onBack = { navController.popBackStack() }) }

                composable("reader/{number}/{name}") { entry ->
                    val number = entry.arguments?.getString("number")?.toIntOrNull() ?: 1
                    val name = entry.arguments?.getString("name") ?: ""
                    QuranReaderScreen(surahNumber = number, surahName = name, onBack = { navController.popBackStack() })
                }
                composable("athkarDetail/{cat}/{title}") { entry ->
                    val cat = entry.arguments?.getString("cat") ?: "morning"
                    val title = entry.arguments?.getString("title") ?: ""
                    AthkarDetailScreen(categoryId = cat, title = title, onBack = { navController.popBackStack() })
                }
                composable("story/{id}") { entry ->
                    val id = entry.arguments?.getString("id")?.toIntOrNull() ?: 1
                    StoryDetailScreen(storyId = id, onBack = { navController.popBackStack() })
                }

                composable("login") { LoginScreen(
                    onSuccess = { navController.popBackStack() },
                    onForgot = { navController.navigate("forgot") },
                    onNavigateToRegister = { navController.navigate("register") }
                ) }
                composable("register") { com.elhajri.noor.auth.RegisterScreen(
                    onNavigateToOtp = { navController.navigate("otp") },
                    onBack = { navController.popBackStack() }
                ) }
                composable("otp") { com.elhajri.noor.auth.OtpScreen(
                    onSuccess = { navController.popBackStack("login", inclusive = true) },
                    onBack = { navController.popBackStack() }
                ) }
                composable("forgot") { ForgotPasswordScreen(onBack = { navController.popBackStack() }) }
                composable("reset") { ResetPasswordScreen(onBack = { navController.popBackStack() }) }

                composable("journal") { JournalScreen() }
                composable("calendar") { com.elhajri.noor.analytics.AnalyticsTracker.trackFeatureOpen(appCtx, "calendar"); CalendarScreen() }
                composable("hijri_calendar") { com.elhajri.noor.analytics.AnalyticsTracker.trackFeatureOpen(appCtx, "calendar"); CalendarScreen() }
                composable("privacy") { PrivacyPolicyScreen(onBack = { navController.popBackStack() }) }
                composable("community") { CommunityScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToLogin = { navController.navigate("login") }
                ) }
                composable("ai") { com.elhajri.noor.ai.AiChatScreen(onBack = { navController.popBackStack() }) }
                composable("favorites") { FavoritesScreen(
                    onOpenSurah = { surah -> navController.navigate("reader/${surah.number}/${surah.name}") },
                    onOpenStory = { id -> navController.navigate("story/$id") }
                ) }
                composable("donation") { DonationScreen(onBack = { navController.popBackStack() }) }
                composable("notifications") { NotificationCenterScreen(onBack = { navController.popBackStack() }) }
                composable("library") {
                    com.elhajri.noor.analytics.AnalyticsTracker.trackFeatureOpen(appCtx, "library")
                    com.elhajri.noor.library.LibraryScreen(
                        onBack = { navController.popBackStack() },
                        onOpenReader = { bookId -> navController.navigate("book_reader/$bookId") }
                    )
                }
                composable("nasheeds") {
                    com.elhajri.noor.analytics.AnalyticsTracker.trackFeatureOpen(appCtx, "nasheeds")
                    com.elhajri.noor.nasheeds.NasheedsScreen(onBack = { navController.popBackStack() })
                }
                composable("book_reader/{bookId}") { entry ->
                    val bookId = entry.arguments?.getString("bookId") ?: ""
                    com.elhajri.noor.library.InAppBookReaderScreen(bookId = bookId, onBack = { navController.popBackStack() })
                }
                composable("downloads") { com.elhajri.noor.analytics.AnalyticsTracker.trackFeatureOpen(appCtx, "downloads"); com.elhajri.noor.offline.DownloadsScreen(onBack = { navController.popBackStack() }, onOpenBookReader = { bookId -> navController.navigate("book_reader/$bookId") }) }
                composable("tracker") { com.elhajri.noor.analytics.AnalyticsTracker.trackFeatureOpen(appCtx, "tracker"); AnalyticsDashboardScreen(onBack = { navController.popBackStack() }) }
                composable("mychild") { com.elhajri.noor.analytics.AnalyticsTracker.trackFeatureOpen(appCtx, "mychild"); com.elhajri.noor.mychild.MyChildRootScreen(onBack = { navController.popBackStack() }, onNavigate = { route -> navController.navigate(route) }) }
                composable("podcasts") { com.elhajri.noor.analytics.AnalyticsTracker.trackFeatureOpen(appCtx, "podcasts"); com.elhajri.noor.audio.sections.PodcastsScreen(onBack = { navController.popBackStack() }) }
                composable("ruqyah") { com.elhajri.noor.analytics.AnalyticsTracker.trackFeatureOpen(appCtx, "ruqyah"); com.elhajri.noor.audio.sections.RuqyahScreen(onBack = { navController.popBackStack() }) }
                composable("analytics") { com.elhajri.noor.analytics.AnalyticsTracker.trackFeatureOpen(appCtx, "analytics"); AnalyticsDashboardScreen(onBack = { navController.popBackStack() }) }
                composable("games") { NoorGameHubScreen(onBack = { navController.popBackStack() }, onNavigate = { navController.navigate(it) }) }
                composable("videos") { com.elhajri.noor.analytics.AnalyticsTracker.trackFeatureOpen(appCtx, "videos"); com.elhajri.noor.videos.VideosScreen(onBack = { navController.popBackStack() }, onOpenVideo = { videoId -> navController.navigate("videoPlayer/$videoId") }) }
                composable("videoPlayer/{videoId}") { entry ->
                    val videoId = entry.arguments?.getString("videoId") ?: ""
                    com.elhajri.noor.videos.VideoPlayerScreen(initialVideoId = videoId, onBack = { navController.popBackStack() })
                }
                composable("game_kalimat") { KalimatGameScreen(onBack = { navController.popBackStack() }) }
                composable("game_trivia") { TriviaLadderGameScreen(onBack = { navController.popBackStack() }) }
                composable("game_ayat") { AyatChainGameScreen(onBack = { navController.popBackStack() }) }
                composable("game_match") { NamesMatchGameScreen(onBack = { navController.popBackStack() }) }
                composable("game_timeline") { TimelineGameScreen(onBack = { navController.popBackStack() }) }
                composable("game_tartil") { TartilMirrorGameScreen(onBack = { navController.popBackStack() }) }
                composable("game_prophets_order") { ProphetsOrderGameScreen(onBack = { navController.popBackStack() }) }
                composable("game_true_false") { TrueFalseGameScreen(onBack = { navController.popBackStack() }) }
                composable("game_numbers_quiz") { NumbersQuizGameScreen(onBack = { navController.popBackStack() }) }
                composable("game_quran_mem") { QuranMemorizationGameScreen(onBack = { navController.popBackStack() }) }
                composable("game_names_allah") { NamesOfAllahGameScreen(onBack = { navController.popBackStack() }) }
                composable("game_prophets_journey") { ProphetsJourneyGameScreen(onBack = { navController.popBackStack() }) }
                // ===== New interactive games (batch 1) =====
                composable("game_word_search") { WordSearchGame(onBack = { navController.popBackStack() }) }
                composable("game_tawaf") { TawafOrderGame(onBack = { navController.popBackStack() }) }
                composable("game_wudu") { WuduStepsGame(onBack = { navController.popBackStack() }) }
                composable("game_prayer_order") { PrayerOrderGame(onBack = { navController.popBackStack() }) }
                composable("game_duas_chain") { DuasChainGame(onBack = { navController.popBackStack() }) }
                composable("game_deeds_balance") { DeedsBalanceGame(onBack = { navController.popBackStack() }) }
                composable("game_hijri_months") { HijriMonthsGame(onBack = { navController.popBackStack() }) }
                composable("game_juz_order") { JuzOrderGame(onBack = { navController.popBackStack() }) }
                composable("game_lantern_memory") { LanternMemoryGame(onBack = { navController.popBackStack() }) }
                composable("game_isra") { IsraJourneyGame(onBack = { navController.popBackStack() }) }
                composable("game_wisdom_pearls") { WisdomPearlsGame(onBack = { navController.popBackStack() }) }
                composable("game_zakat_calc") { ZakatCalcGame(onBack = { navController.popBackStack() }) }
                composable("game_hijri_dates") { HijriDatesGame(onBack = { navController.popBackStack() }) }
                composable("game_angels_unseen") { AngelsAndUnseenQuizGame(onBack = { navController.popBackStack() }) }
                composable("game_fasting_rules") { FastingRulesGame(onBack = { navController.popBackStack() }) }
                composable("game_kindness_tap") { KindnessTapGame(onBack = { navController.popBackStack() }) }
                // ===== Batch 3: engine-based games =====
                composable("game_quiz_doctrine") { DoctrineQuizGameScreen(onBack = { navController.popBackStack() }) }
                composable("game_quiz_fiqh") { FiqhQuizGameScreen(onBack = { navController.popBackStack() }) }
                composable("game_quiz_sirah") { SirahQuizGameScreen(onBack = { navController.popBackStack() }) }
                composable("game_quiz_companions") { CompanionsQuizGameScreen(onBack = { navController.popBackStack() }) }
                composable("game_quiz_ramadan") { RamadanQuizGameScreen(onBack = { navController.popBackStack() }) }
                composable("game_quiz_hajj") { HajjQuizGameScreen(onBack = { navController.popBackStack() }) }
                composable("game_match_names_meaning") { MatchNamesMeaningGameScreen(onBack = { navController.popBackStack() }) }
                composable("game_match_surah_topics") { MatchSurahTopicsGameScreen(onBack = { navController.popBackStack() }) }
                composable("game_match_worship") { MatchWorshipGameScreen(onBack = { navController.popBackStack() }) }
                composable("game_match_athkar_times") { MatchAthkarTimesGameScreen(onBack = { navController.popBackStack() }) }
                composable("game_match_mosque_cities") { MatchMosqueCitiesGameScreen(onBack = { navController.popBackStack() }) }
                composable("game_match_islamic_months") { MatchIslamicMonthsGameScreen(onBack = { navController.popBackStack() }) }
                composable("game_scramble_prophets") { ScrambleProphetsGameScreen(onBack = { navController.popBackStack() }) }
                composable("game_scramble_surahs") { ScrambleSurahsGameScreen(onBack = { navController.popBackStack() }) }
                composable("game_scramble_names_allah") { ScrambleNamesAllahGameScreen(onBack = { navController.popBackStack() }) }
                composable("game_scramble_ramadan") { ScrambleRamadanGameScreen(onBack = { navController.popBackStack() }) }
                composable("game_quiz_prophets") { ProphetsQuizGameScreen(onBack = { navController.popBackStack() }) }
                composable("game_quiz_quran_stories") { QuranStoriesQuizGameScreen(onBack = { navController.popBackStack() }) }
                composable("game_quiz_manners") { MannersQuizGameScreen(onBack = { navController.popBackStack() }) }
                composable("game_quiz_prayer") { PrayerQuizGameScreen(onBack = { navController.popBackStack() }) }
                composable("game_quiz_athkar") { AthkarQuizGameScreen(onBack = { navController.popBackStack() }) }
                composable("game_quiz_hadith") { HadithQuizGameScreen(onBack = { navController.popBackStack() }) }
                composable("hajj") { HajjGuideScreen(onBack = { navController.popBackStack() }) }
                composable("zakat") { ZakatScreen() }
            }
        }
        if (showSplash) {
            NoorSplash(onFinished = { showSplash = false })
        }
    }
}
