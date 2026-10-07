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
import com.elhajri.noor.auth.RegisterScreen
import com.elhajri.noor.auth.ResetPasswordScreen
import com.elhajri.noor.calendar.CalendarScreen
import com.elhajri.noor.community.CommunityScreen
import com.elhajri.noor.donation.DonationScreen
import com.elhajri.noor.favorites.FavoritesScreen
import com.elhajri.noor.games.NoorGameHubScreen
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
import com.elhajri.noor.quran.TrackerScreen
import com.elhajri.noor.quiz.QuizScreen
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        com.elhajri.noor.auth.Base44Auth.init(applicationContext)
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
                val savedCity = com.elhajri.noor.data.Prefs.getCity(this@MainActivity)
                kotlinx.coroutines.runBlocking {
                    com.elhajri.noor.prayer.PrayerRepository.getTimings(
                        this@MainActivity,
                        lat = savedCity?.lat ?: 21.4225,
                        lng = savedCity?.lng ?: 39.8262,
                        country = savedCity?.country ?: ""
                    )
                }
                com.elhajri.noor.notification.AdhanScheduler.scheduleNextAdhan(this@MainActivity)
            } catch (_: Exception) {}
        }.start()
        setContent {
            NoorTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
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
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val mainRoutes = remember { bottomBarTabs.map { it.route }.toSet() }
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

    // التنقل الموثوق لشريط التنقل السفلي — يعمل بدقة 100% في كل الأوقات وعلى كل الأجهزة
    val onTabSelected: (String) -> Unit = { targetRoute ->
        val activeRoute = navController.currentDestination?.route
        if (activeRoute != targetRoute) {
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
                    onSettings = { navigateSafe("settings") }
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
                composable("quran") { com.elhajri.noor.quran.QuranScreen(onSurahClick = { surah -> navController.navigate("reader/${surah.number}/${surah.name}") }) }
                composable("athkar") { AthkarScreen(onOpenCategory = { cat, title -> navController.navigate("athkarDetail/$cat/$title") }) }
                composable("more") { MoreScreen(onNavigate = { navigateSafe(it) }) }

                composable("qibla") { QiblaScreen() }
                composable("tasbih") { TasbihScreen() }
                composable("names") { NamesOfAllahScreen() }
                composable("quiz") { QuizScreen() }
                composable("stories") { StoriesScreen(onOpenStory = { id -> navController.navigate("story/$id") }) }
                composable("settings") { SettingsScreen(onOpenPrivacy = { navController.navigate("privacy") }) }
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
                    onRegister = { navController.navigate("register") },
                    onForgot = { navController.navigate("forgot") }
                ) }
                composable("register") { RegisterScreen(
                    onSuccess = { navController.navigate("verifyOtp") },
                    onLogin = { navController.popBackStack() }
                ) }
                composable("verifyOtp") { com.elhajri.noor.auth.VerifyOtpScreen(
                    onVerified = {
                        navController.popBackStack("login", inclusive = true)
                        navController.navigate("community")
                    },
                    onBack = { navController.popBackStack() }
                ) }
                composable("forgot") { ForgotPasswordScreen(onBack = { navController.popBackStack() }) }
                composable("reset") { ResetPasswordScreen(onBack = { navController.popBackStack() }) }

                composable("journal") { JournalScreen() }
                composable("calendar") { CalendarScreen() }
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
                composable("library") { com.elhajri.noor.quran.LibraryScreen(onBack = { navController.popBackStack() }) }
                composable("tracker") { TrackerScreen() }
                composable("games") { NoorGameHubScreen(onBack = { navController.popBackStack() }, onNavigate = { navController.navigate(it) }) }
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
                composable("hajj") { HajjGuideScreen(onBack = { navController.popBackStack() }) }
                composable("zakat") { ZakatScreen() }
            }
        }
        if (showSplash) {
            NoorSplash(onFinished = { showSplash = false })
        }
    }
}
