package com.elhajri.noor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.elhajri.noor.ai.AIAssistantScreen
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
import com.elhajri.noor.games.GamesScreen
import com.elhajri.noor.games.NamesGameScreen
import com.elhajri.noor.games.ProphetsJourneyScreen
import com.elhajri.noor.games.QuranMemorizationScreen
import com.elhajri.noor.hajj.HajjGuideScreen
import com.elhajri.noor.home.HomeScreen
import com.elhajri.noor.journal.JournalScreen
import com.elhajri.noor.more.MoreScreen
import com.elhajri.noor.names.NamesOfAllahScreen
import com.elhajri.noor.notification.NotificationCenterScreen
import com.elhajri.noor.prayer.PrayerScreen
import com.elhajri.noor.privacy.PrivacyPolicyScreen
import com.elhajri.noor.qibla.QiblaScreen
import com.elhajri.noor.quran.LibraryScreen
import com.elhajri.noor.quran.QuranReaderScreen
import com.elhajri.noor.quran.QuranScreen
import com.elhajri.noor.quran.TrackerScreen
import com.elhajri.noor.quiz.QuizScreen
import com.elhajri.noor.settings.SettingsScreen
import com.elhajri.noor.stories.StoriesScreen
import com.elhajri.noor.stories.StoryDetailScreen
import com.elhajri.noor.tasbih.TasbihScreen
import com.elhajri.noor.zakat.ZakatScreen
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.NavyCard
import com.elhajri.noor.ui.NoorTheme
import com.elhajri.noor.ui.NoorTopBar
import com.elhajri.noor.ui.NoorSplash
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.elhajri.noor.audio.SoundEffects

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SoundEffects.init(this)
        com.elhajri.noor.audio.NoorAudioController.appContext = applicationContext
        setContent {
            NoorTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    NoorApp()
                }
            }
        }
    }
}

private data class Tab(val route: String, val label: String, val icon: ImageVector)

private val tabs = listOf(
    Tab("home", "الرئيسية", Icons.Filled.Home),
    Tab("prayer", "الصلاة", Icons.Filled.Schedule),
    Tab("quran", "المصحف", Icons.Filled.MenuBook),
    Tab("athkar", "الأذكار", Icons.Filled.Favorite),
    Tab("more", "المزيد", Icons.Filled.MoreHoriz)
)

@Composable
fun NoorApp() {
    var showSplash by remember { mutableStateOf(true) }
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val showBar = currentRoute in tabs.map { it.route }

    Box(modifier = Modifier.fillMaxSize()) {
    Scaffold(
        topBar = {
            if (showBar) NoorTopBar(
                onDonate = { navController.navigate("donation") },
                onAssistant = { navController.navigate("ai") },
                onSettings = { navController.navigate("settings") }
            )
        },
        bottomBar = {
            if (showBar) {
                NavigationBar(
                containerColor = NavyCard,
                modifier = Modifier.height(58.dp),
                tonalElevation = 0.dp
            ) {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = {
                                SoundEffects.click()
                                navController.navigate(tab.route) {
                                    popUpTo("home") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Gold,
                                selectedTextColor = Gold,
                                unselectedIconColor = GoldSoft.copy(alpha = 0.5f),
                                indicatorColor = Gold.copy(alpha = 0.15f)
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(padding)
        ) {
            composable("home") { HomeScreen(onNavigate = { navController.navigate(it) }) }
            composable("prayer") { PrayerScreen() }
            composable("quran") { QuranScreen(onSurahClick = { surah -> navController.navigate("reader/${surah.number}/${surah.name}") }) }
            composable("athkar") { AthkarScreen(onOpenCategory = { cat, title -> navController.navigate("athkarDetail/$cat/$title") }) }
            composable("more") { MoreScreen(onNavigate = { navController.navigate(it) }) }

            composable("qibla") { QiblaScreen() }
            composable("tasbih") { TasbihScreen() }
            composable("names") { NamesOfAllahScreen() }
            composable("quiz") { QuizScreen() }
            composable("stories") { StoriesScreen(onOpenStory = { id -> navController.navigate("story/$id") }) }
            composable("settings") { SettingsScreen() }

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
                onSuccess = { navController.navigate("home") { popUpTo("login") { inclusive = true } } },
                onRegister = { navController.navigate("register") },
                onForgot = { navController.navigate("forgot") }
            ) }
            composable("register") { RegisterScreen(
                onSuccess = { navController.navigate("home") { popUpTo("register") { inclusive = true } } },
                onLogin = { navController.popBackStack() }
            ) }
            composable("forgot") { ForgotPasswordScreen(onBack = { navController.popBackStack() }) }
            composable("reset") { ResetPasswordScreen(onBack = { navController.popBackStack() }) }

            composable("journal") { JournalScreen() }
            composable("calendar") { CalendarScreen() }
            composable("privacy") { PrivacyPolicyScreen(onBack = { navController.popBackStack() }) }
            composable("community") { CommunityScreen(onBack = { navController.popBackStack() }) }
            composable("ai") { AIAssistantScreen(onBack = { navController.popBackStack() }) }
            composable("favorites") { FavoritesScreen(
                onOpenSurah = { surah -> navController.navigate("reader/${surah.number}/${surah.name}") },
                onOpenStory = { id -> navController.navigate("story/$id") }
            ) }
            composable("donation") { DonationScreen(onBack = { navController.popBackStack() }) }
            composable("notifications") { NotificationCenterScreen(onBack = { navController.popBackStack() }) }
            composable("library") { LibraryScreen(onBack = { navController.popBackStack() }) }
            composable("tracker") { TrackerScreen() }
            composable("games") { GamesScreen(onNavigate = { navController.navigate(it) }) }
            composable("names_game") { NamesGameScreen(onBack = { navController.popBackStack() }) }
            composable("journey_game") { ProphetsJourneyScreen(onBack = { navController.popBackStack() }) }
            composable("mem_game") { QuranMemorizationScreen(onBack = { navController.popBackStack() }) }
            composable("hajj") { HajjGuideScreen(onBack = { navController.popBackStack() }) }
            composable("zakat") { ZakatScreen() }
        }
    }
    if (showSplash) {
        NoorSplash(onFinished = { showSplash = false })
    }
    }
}
