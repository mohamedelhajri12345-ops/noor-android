package com.elhajri.noor.mychild
import com.elhajri.noor.settings.t

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.audio.SfxManager
import com.elhajri.noor.ui.AmiriFamily
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.Navy
import com.elhajri.noor.settings.NoorLocale
import com.elhajri.noor.settings.toArabicDigits
import com.elhajri.noor.settings.t
import com.elhajri.noor.ui.NavyCard
import com.elhajri.noor.ui.TextMain
import com.elhajri.noor.ui.TextSecondaryLinen
import com.elhajri.noor.ui.noorGlassCard

private val AVATAR_OPTIONS = listOf(
    "🦁", "🐯", "🐻", "🦄", "🚀", "👑", "🌟", "🎈",
    "⚽", "🌸", "🐥", "🎨", "🐱", "🦊", "🤖", "🦸"
)

private val JUZ_30_SURAHS = listOf(
    ChildMemorization(1, "الفاتحة", "Al-Fatiha", 7),
    ChildMemorization(78, "النبأ", "An-Naba", 40),
    ChildMemorization(79, "النازعات", "An-Nazi'at", 46),
    ChildMemorization(80, "عبس", "Abasa", 42),
    ChildMemorization(81, "التكوير", "At-Takwir", 29),
    ChildMemorization(82, "الانفطار", "Al-Infitar", 19),
    ChildMemorization(83, "المطففين", "Al-Mutaffifin", 36),
    ChildMemorization(84, "الانشقاق", "Al-Inshiqaq", 25),
    ChildMemorization(85, "البروج", "Al-Buruj", 22),
    ChildMemorization(86, "الطارق", "At-Tariq", 17),
    ChildMemorization(87, "الأعلى", "Al-A'la", 19),
    ChildMemorization(88, "الغاشية", "Al-Ghashiyah", 26),
    ChildMemorization(89, "الفجر", "Al-Fajr", 30),
    ChildMemorization(90, "البلد", "Al-Balad", 20),
    ChildMemorization(91, "الشمس", "Ash-Shams", 15),
    ChildMemorization(92, "الليل", "Al-Lail", 21),
    ChildMemorization(93, "الضحى", "Ad-Duha", 11),
    ChildMemorization(94, "الشرح", "Ash-Sharh", 8),
    ChildMemorization(95, "التين", "At-Tin", 8),
    ChildMemorization(96, "العلق", "Al-Alaq", 19),
    ChildMemorization(97, "القدر", "Al-Qadr", 5),
    ChildMemorization(98, "البينة", "Al-Bayyinah", 8),
    ChildMemorization(99, "الزلزلة", "Az-Zalzalah", 8),
    ChildMemorization(100, "العاديات", "Al-Adiyat", 11),
    ChildMemorization(101, "القارعة", "Al-Qari'ah", 11),
    ChildMemorization(102, "التكاثر", "At-Takathur", 8),
    ChildMemorization(103, "العصر", "Al-Asr", 3),
    ChildMemorization(104, "الهمزة", "Al-Humazah", 9),
    ChildMemorization(105, "الفيل", "Al-Fil", 5),
    ChildMemorization(106, "قريش", "Quraish", 4),
    ChildMemorization(107, "الماعون", "Al-Ma'un", 7),
    ChildMemorization(108, "الكوثر", "Al-Kawthar", 3),
    ChildMemorization(109, "الكافرون", "Al-Kafirun", 6),
    ChildMemorization(110, "النصر", "An-Nasr", 3),
    ChildMemorization(111, "المسد", "Al-Masad", 5),
    ChildMemorization(112, "الإخلاص", "Al-Ikhlas", 4),
    ChildMemorization(113, "الفلق", "Al-Falaq", 5),
    ChildMemorization(114, "الناس", "An-Nas", 6)
)

enum class ChildTab(val titleAr: String, val icon: ImageVector) {
    PARENTS(t("لوحة الوالدين", "Parents Dashboard"), Icons.Filled.FamilyRestroom),
    SAFE_MODE(t("وضع الطفل الآمن", "Safe Kids Mode"), Icons.Filled.Shield),
    MEMORIZATION(t("متابعة الحفظ", "Memorization Tracker"), Icons.Filled.Star),
    BEDTIME_STORY(t("قصة قبل النوم", "Bedtime Story"), Icons.Filled.NightsStay),
    FAMILY_QUIZ(t("تحدي عائلي", "Family Challenge"), Icons.Filled.SportsEsports)
}

@Composable
fun MyChildRootScreen(
    onBack: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    var isSafeMode by remember { mutableStateOf(MyChildStore.isKidsSafeModeActive(context)) }
    var selectedTab by remember { mutableStateOf(ChildTab.PARENTS) }
    var activeProfile by remember { mutableStateOf(MyChildStore.getActiveProfile(context)) }

    // If safe mode is enabled, show the simplified Kids Home Screen
    if (isSafeMode) {
        SafeKidsHomeScreen(
            childProfile = activeProfile,
            onExitSafeMode = {
                MyChildStore.setKidsSafeModeActive(context, false)
                isSafeMode = false
                SfxManager.playSuccess(context)
                Toast.makeText(context, t("تم الخروج من وضع الطفل الآمن", "Exited Safe Kids Mode"), Toast.LENGTH_SHORT).show()
            },
            onNavigate = onNavigate
        )
        return
    }

    // Otherwise, Parents View with Tabs
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Navy)
            .padding(top = 8.dp)
    ) {
        // Top Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            IconButton(
                onClick = {
                    SfxManager.playTap(context)
                    onBack()
                }
            ) {
                Icon(Icons.Filled.ArrowBack, contentDescription = t("رجوع", "Back"), tint = Gold)
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = t("طفلي 👶", "My Child 👶"),
                    color = Gold,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = AmiriFamily
                )
                Text(
                    text = t("ملف الطفل، وضع آمن، متابعة الحفظ، وتحدي عائلي", "Child profile, safe mode, memorization tracker & family quiz"),
                    color = GoldSoft.copy(alpha = 0.7f),
                    fontSize = 11.sp
                )
            }

            // Quick Active Profile Avatar Chip
            Surface(
                color = Gold.copy(alpha = 0.15f),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Gold.copy(alpha = 0.4f)),
                modifier = Modifier.clickable {
                    selectedTab = ChildTab.PARENTS
                }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(text = activeProfile.avatar, fontSize = 16.sp)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = activeProfile.name,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Navigation Tabs Bar
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            contentPadding = PaddingValues(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(ChildTab.values()) { tab ->
                val isSelected = selectedTab == tab
                Surface(
                    color = if (isSelected) Gold else NavyCard,
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) Gold else Gold.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.clickable {
                        SfxManager.playTap(context)
                        selectedTab = tab
                    }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.titleAr,
                            tint = if (isSelected) Navy else Gold,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = tab.titleAr,
                            color = if (isSelected) Navy else Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Divider(color = Gold.copy(alpha = 0.15f), thickness = 1.dp)

        // Main Tab Content View
        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                ChildTab.PARENTS -> ParentsDashboardContent(
                    activeProfile = activeProfile,
                    onProfileUpdated = {
                        activeProfile = MyChildStore.getActiveProfile(context)
                    },
                    onActivateSafeMode = {
                        MyChildStore.setKidsSafeModeActive(context, true)
                        isSafeMode = true
                        SfxManager.playSuccess(context)
                    },
                    onNavigateToTab = { selectedTab = it }
                )
                ChildTab.SAFE_MODE -> SafeKidsHomeScreen(
                    childProfile = activeProfile,
                    onExitSafeMode = {
                        MyChildStore.setKidsSafeModeActive(context, false)
                        isSafeMode = false
                        SfxManager.playSuccess(context)
                    },
                    onNavigate = onNavigate
                )
                ChildTab.MEMORIZATION -> MemorizationTrackerContent(
                    childProfile = activeProfile,
                    onProfileUpdated = {
                        activeProfile = MyChildStore.getActiveProfile(context)
                    }
                )
                ChildTab.BEDTIME_STORY -> BedtimeStoryContent(
                    childProfile = activeProfile,
                    onProfileUpdated = {
                        activeProfile = MyChildStore.getActiveProfile(context)
                    }
                )
                ChildTab.FAMILY_QUIZ -> FamilyChallengeContent(
                    childProfile = activeProfile,
                    onProfileUpdated = {
                        activeProfile = MyChildStore.getActiveProfile(context)
                    }
                )
            }
        }
    }
}

// ==========================================
// 1. PARENTS DASHBOARD & PROFILES CONTENT
// ==========================================
@Composable
fun ParentsDashboardContent(
    activeProfile: ChildProfile,
    onProfileUpdated: () -> Unit,
    onActivateSafeMode: () -> Unit,
    onNavigateToTab: (ChildTab) -> Unit
) {
    val context = LocalContext.current
    var profiles by remember { mutableStateOf(MyChildStore.getProfiles(context)) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showPinDialog by remember { mutableStateOf(false) }
    var showRewardAddDialog by remember { mutableStateOf(false) }

    var screenTimeMinutes by remember {
        mutableStateOf(MyChildStore.getTodayScreenTimeMinutes(context, activeProfile.id))
    }
    var memorizationMap by remember {
        mutableStateOf(MyChildStore.getMemorizationMap(context, activeProfile.id))
    }
    var rewardsList by remember {
        mutableStateOf(MyChildStore.getParentRewards(context, activeProfile.id))
    }

    val memorizedCount = memorizationMap.values.count { it == 3 }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Child Profile Switcher Carousel ---
        item {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = t("ملف أطفالي 👶", "My Children Profiles 👶"),
                        color = Gold,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    TextButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Filled.Add, contentDescription = t("إضافة", "Add"), tint = Gold, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("إضافة طفل", color = Gold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    items(profiles) { profile ->
                        val isSelected = profile.id == activeProfile.id
                        Surface(
                            color = if (isSelected) Gold.copy(alpha = 0.25f) else NavyCard,
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (isSelected) Gold else Gold.copy(alpha = 0.2f)
                            ),
                            modifier = Modifier
                                .clickable {
                                    MyChildStore.setActiveProfileId(context, profile.id)
                                    onProfileUpdated()
                                }
                                .padding(2.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Text(profile.avatar, fontSize = 22.sp)
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Text(
                                        profile.name,
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        t("${profile.age} سنوات | ⭐️ ${profile.totalStarsEarned}", "${profile.age} yrs | ⭐️ ${profile.totalStarsEarned}"),
                                        color = GoldSoft,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- Active Child Summary Metrics Card ---
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .noorGlassCard(cornerRadius = 20.dp)
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(activeProfile.avatar, fontSize = 32.sp)
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = t("لوحة الوالدين لـ ${activeProfile.name}", "Parent Dashboard for ${activeProfile.name}"),
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = AmiriFamily
                        )
                        Text(
                            text = t("ملخص إنجاز ونشاط اليوم", "Today summary of achievements and activity"),
                            color = GoldSoft.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                    }

                    // Safe Mode Button
                    Button(
                        onClick = {
                            showPinDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Gold),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.Shield, contentDescription = t("وضع آمن", "Safe Mode"), tint = Navy, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("وضع الطفل الآمن", color = Navy, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MetricBox(
                        icon = Icons.Filled.Timer,
                        label = t("وقت الشاشة اليوم", "Daily Screen Time"),
                        value = t("$screenTimeMinutes دقيقة", "$screenTimeMinutes min"),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    MetricBox(
                        icon = Icons.Filled.Star,
                        label = t("رصيد النجوم", "Star Balance"),
                        value = "${activeProfile.totalStarsEarned} ⭐️",
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    MetricBox(
                        icon = Icons.Filled.MenuBook,
                        label = t("السور المحفوظة", "Memorized Surahs"),
                        value = t("$memorizedCount من ٣٨", "$memorizedCount of 38"),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // --- Motivation & Parent Rewards Section ---
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .noorGlassCard(cornerRadius = 20.dp)
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.CardGiftcard, contentDescription = t("مكافآت", "Rewards"), tint = Gold, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("بطاقات التحفيز ومكافآت الوالدين 🎁", color = Gold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    TextButton(onClick = {
                        MyChildStore.addStarsToChild(context, activeProfile.id, 5)
                        onProfileUpdated()
                        SfxManager.playSuccess(context)
                        Toast.makeText(context, t("تم إهداء ٥ نجوم تشجيعية لطفلك! 🌟", "Gifted 5 encouragement stars to your child! 🌟"), Toast.LENGTH_SHORT).show()
                    }) {
                        Text("إهداء ٥ نجوم ⭐️", color = Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(Modifier.height(10.dp))

                rewardsList.forEach { reward ->
                    Surface(
                        color = NavyCard,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(reward.iconEmoji, fontSize = 24.sp)
                            Spacer(Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    reward.titleAr,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    t("التكلفة: ${reward.starCost} نجمة", "Cost: ${reward.starCost} stars"),
                                    color = GoldSoft,
                                    fontSize = 11.sp
                                )
                            }

                            if (reward.isRedeemed) {
                                Surface(
                                    color = Gold.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("تم الاستبدال 🎉", color = Gold, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                }
                            } else {
                                Button(
                                    onClick = {
                                        if (MyChildStore.redeemReward(context, activeProfile.id, reward.id)) {
                                            rewardsList = MyChildStore.getParentRewards(context, activeProfile.id)
                                            onProfileUpdated()
                                            SfxManager.playSuccess(context)
                                            Toast.makeText(context, t("مبروك! تم استبدال المكافأة 🎉", "Congratulations! Reward redeemed 🎉"), Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, t("رصيد النجوم غير كافٍ بعد", "Insufficient star balance"), Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    enabled = activeProfile.totalStarsEarned >= reward.starCost,
                                    colors = ButtonDefaults.buttonColors(containerColor = Gold),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("استبدال", color = Navy, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { showRewardAddDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Gold.copy(alpha = 0.5f))
                ) {
                    Icon(Icons.Filled.Add, contentDescription = t("إضافة مكافأة", "Add Reward"), tint = Gold)
                    Spacer(Modifier.width(6.dp))
                    Text("إضافة هدية أو مكافأة خاصة", color = Gold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // --- Quick Section Launchers ---
        item {
            Text("أقسام ركن الطفل 🚀", color = Gold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                QuickLauncherCard(
                    title = t("متابعة الحفظ", "Memorization Tracker"),
                    desc = t("سور الجزء ٣٠", "Juz 30 Surahs"),
                    icon = Icons.Filled.Star,
                    onClick = { onNavigateToTab(ChildTab.MEMORIZATION) },
                    modifier = Modifier.weight(1f)
                )
                QuickLauncherCard(
                    title = t("قصة قبل النوم", "Bedtime Story"),
                    desc = t("قصص الأنبياء", "Prophets Stories"),
                    icon = Icons.Filled.NightsStay,
                    onClick = { onNavigateToTab(ChildTab.BEDTIME_STORY) },
                    modifier = Modifier.weight(1f)
                )
                QuickLauncherCard(
                    title = t("تحدي عائلي", "Family Challenge"),
                    desc = t("مسابقة الوالدين", "Parent Quiz"),
                    icon = Icons.Filled.SportsEsports,
                    onClick = { onNavigateToTab(ChildTab.FAMILY_QUIZ) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    // --- Dialogs ---
    if (showAddDialog) {
        AddChildDialog(
            onDismiss = { showAddDialog = false },
            onSave = { name, age, avatar ->
                val newProfile = ChildProfile(
                    id = java.util.UUID.randomUUID().toString(),
                    name = name,
                    age = age,
                    avatar = avatar
                )
                MyChildStore.saveChildProfile(context, newProfile)
                profiles = MyChildStore.getProfiles(context)
                onProfileUpdated()
                showAddDialog = false
                SfxManager.playSuccess(context)
            }
        )
    }

    if (showPinDialog) {
        PinVerificationDialog(
            title = t("تأكيد رمز الحماية الوالدية", "Parental PIN Confirmation"),
            desc = t("أدخل رمز PIN لتفعيل وضع الطفل الآمن (الافتراضي 1234)", "Enter 4-digit PIN to enable Safe Kids Mode (default 1234)"),
            onDismiss = { showPinDialog = false },
            onVerified = {
                showPinDialog = false
                onActivateSafeMode()
            }
        )
    }

    if (showRewardAddDialog) {
        AddRewardDialog(
            onDismiss = { showRewardAddDialog = false },
            onAdd = { title, cost, emoji ->
                MyChildStore.addParentReward(context, activeProfile.id, title, cost, emoji)
                rewardsList = MyChildStore.getParentRewards(context, activeProfile.id)
                showRewardAddDialog = false
                SfxManager.playSuccess(context)
            }
        )
    }
}

// Helper Metric Box
@Composable
private fun MetricBox(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = NavyCard,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Gold.copy(alpha = 0.2f)),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(10.dp)
        ) {
            Icon(icon, contentDescription = label, tint = Gold, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(4.dp))
            Text(value, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(label, color = GoldSoft.copy(alpha = 0.7f), fontSize = 10.sp, textAlign = TextAlign.Center)
        }
    }
}

// Helper Quick Launcher Card
@Composable
private fun QuickLauncherCard(
    title: String,
    desc: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = NavyCard,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Gold.copy(alpha = 0.3f)),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(12.dp)
        ) {
            Icon(icon, contentDescription = title, tint = Gold, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(6.dp))
            Text(title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Text(desc, color = GoldSoft.copy(alpha = 0.7f), fontSize = 10.sp, textAlign = TextAlign.Center)
        }
    }
}

// ==========================================
// 2. SAFE KIDS HOME SCREEN (وضع الطفل الآمن)
// ==========================================
@Composable
fun SafeKidsHomeScreen(
    childProfile: ChildProfile,
    onExitSafeMode: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    var activeSubTab by remember { mutableStateOf(0) } // 0 = Games, 1 = Stories, 2 = Nasheeds
    var showExitPinDialog by remember { mutableStateOf(false) }

    // Track screen time every minute
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(60000)
            MyChildStore.addScreenTime(context, childProfile.id, 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Navy)
    ) {
        // Safe Mode Kids Top Bar
        Surface(
            color = Gold.copy(alpha = 0.15f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(childProfile.avatar, fontSize = 28.sp)
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        t("مرحباً يا ${childProfile.name}! 🌟", "Welcome ${childProfile.name}! 🌟"),
                        color = Gold,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = AmiriFamily
                    )
                    Text("وضع الطفل الآمن 🛡️", color = GoldSoft, fontSize = 11.sp)
                }

                // Parent Exit Gate
                Button(
                    onClick = { showExitPinDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Gold),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.Lock, contentDescription = t("خروج الوالدين", "Parent Exit"), tint = Navy, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("خروج 🔒", color = Navy, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Sub-tabs bar: Games, Stories, Nasheeds
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val tabs = listOf(t("🎮 الألعاب", "🎮 Games"), t("📚 القصص", "📚 Stories"), t("🎵 الأناشيد", "🎵 Nasheeds"))
            tabs.forEachIndexed { idx, label ->
                val isSelected = activeSubTab == idx
                Surface(
                    color = if (isSelected) Gold else NavyCard,
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Gold),
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            SfxManager.playTap(context)
                            activeSubTab = idx
                        }
                ) {
                    Text(
                        label,
                        color = if (isSelected) Navy else Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            }
        }

        // Sub Tab Content
        Box(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
            when (activeSubTab) {
                0 -> KidsGamesGrid(onNavigate = onNavigate)
                1 -> KidsStoriesGrid()
                2 -> KidsNasheedsGrid()
            }
        }
    }

    if (showExitPinDialog) {
        PinVerificationDialog(
            title = t("رمز خروج الوالدين", "Parent Exit PIN"),
            desc = t("أدخل رمز PIN المكون من ٤ أرقام للخروج من وضع الطفل الآمن", "Enter 4-digit PIN to exit Safe Kids Mode"),
            onDismiss = { showExitPinDialog = false },
            onVerified = {
                showExitPinDialog = false
                onExitSafeMode()
            }
        )
    }
}

// Sub Content: Kids Games Grid
@Composable
private fun KidsGamesGrid(onNavigate: (String) -> Unit) {
    val kidsGames = listOf(
        Pair(t("فوانيس الذاكرة 🏮", "Lantern Memory 🏮"), "game_lantern_memory"),
        Pair(t("كلمة من الذكر 🔤", "Words of Dhikr 🔤"), "game_kalimat"),
        Pair(t("البحث عن الكلمات 🔍", "Word Search 🔍"), "game_word_search"),
        Pair(t("خطوات الوضوء 🧼", "Wudu Steps 🧼"), "game_wudu"),
        Pair(t("ترتيب الصلاة 🕌", "Prayer Order 🕌"), "game_prayer_order"),
        Pair(t("ترتيب الأنبياء 📜", "Prophets Order 📜"), "game_prophets_order")
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(kidsGames) { (title, route) ->
            Surface(
                color = NavyCard,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Gold.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigate(route) }
                    .padding(4.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(Icons.Filled.SportsEsports, contentDescription = title, tint = Gold, modifier = Modifier.size(36.dp))
                    Spacer(Modifier.height(8.dp))
                    Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

// Sub Content: Kids Stories Grid
@Composable
private fun KidsStoriesGrid() {
    val context = LocalContext.current
    val stories = remember { BedtimeStoryManager.loadBedtimeStories(context) }
    var selectedStory by remember { mutableStateOf<BedtimeStoryItem?>(null) }

    if (selectedStory != null) {
        BedtimeStoryReaderDialog(
            story = selectedStory!!,
            onDismiss = { selectedStory = null }
        )
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(stories) { story ->
            Surface(
                color = NavyCard,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Gold.copy(alpha = 0.3f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        SfxManager.playTap(context)
                        selectedStory = story
                    }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(12.dp)
                ) {
                    Icon(Icons.Filled.MenuBook, contentDescription = story.title, tint = Gold, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(story.title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text(story.summary, color = GoldSoft.copy(alpha = 0.7f), fontSize = 11.sp, maxLines = 2)
                    }
                    Icon(Icons.Filled.ChevronLeft, contentDescription = t("قراءة", "Read"), tint = Gold)
                }
            }
        }
    }
}

// Sub Content: Kids Nasheeds Grid
@Composable
private fun KidsNasheedsGrid() {
    val kidsNasheeds = listOf(
        t("يا نبي سلام عليك 🌸", "Ya Nabi Salam Alaika 🌸"),
        t("المصطفى ﷺ 🌟", "Al-Mustafa ﷺ 🌟"),
        t("سبحان الله وبحمده 🍃", "Subhan Allah wa Bihamdihi 🍃"),
        t("اشكر الله على النعم 🤲", "Thank Allah for Blessings 🤲"),
        t("إن شاء الله 🎈", "In Sha Allah 🎈")
    )

    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(kidsNasheeds) { title ->
            Surface(
                color = NavyCard,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Gold.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(14.dp)
                ) {
                    Icon(Icons.Filled.MusicNote, contentDescription = title, tint = Gold, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Icon(Icons.Filled.PlayArrow, contentDescription = t("تشغيل", "Play"), tint = Gold)
                }
            }
        }
    }
}

// ==========================================
// 3. MEMORIZATION TRACKER CONTENT (متابعة الحفظ)
// ==========================================
@Composable
fun MemorizationTrackerContent(
    childProfile: ChildProfile,
    onProfileUpdated: () -> Unit
) {
    val context = LocalContext.current
    var memorizationMap by remember {
        mutableStateOf(MyChildStore.getMemorizationMap(context, childProfile.id))
    }

    val totalSurahs = JUZ_30_SURAHS.size
    val memorizedCount = JUZ_30_SURAHS.count { (memorizationMap[it.surahNumber] ?: 0) == 3 }
    val progressFraction = memorizedCount.toFloat() / totalSurahs

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Stats Banner
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .noorGlassCard(cornerRadius = 20.dp)
                .padding(16.dp)
        ) {
            Text(
                t("متابعة حفظ القرآن الكريم لـ ${childProfile.name} 📖", "Quran Memorization Tracker for ${childProfile.name} 📖"),
                color = Gold,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = AmiriFamily
            )
            Spacer(Modifier.height(6.dp))
            Text(
                t("سور الجزء ٣٠ و الفاتحة | حَفِظَ $memorizedCount من $totalSurahs سورة", "Juz 30 & Al-Fatiha | Memorized $memorizedCount of $totalSurahs surahs"),
                color = Color.White,
                fontSize = 13.sp
            )
            Spacer(Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = progressFraction,
                color = Gold,
                trackColor = Gold.copy(alpha = 0.2f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape)
            )
        }

        Spacer(Modifier.height(12.dp))

        // Surah List
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(JUZ_30_SURAHS) { surah ->
                val stars = memorizationMap[surah.surahNumber] ?: 0
                Surface(
                    color = NavyCard,
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Gold.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Surface(
                            color = Gold.copy(alpha = 0.15f),
                            shape = CircleShape,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("${surah.surahNumber}", color = Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(if (NoorLocale.isEnglish) surah.surahNameEn else surah.surahNameAr, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = AmiriFamily)
                            Text("${surah.versesCount} آيات", color = GoldSoft.copy(alpha = 0.7f), fontSize = 11.sp)
                        }

                        // 3 Star Toggle Row
                        Row {
                            for (s in 1..3) {
                                val isFilled = s <= stars
                                IconButton(
                                    onClick = {
                                        val newStars = if (stars == s) s - 1 else s
                                        MyChildStore.setSurahMemorizationStars(context, childProfile.id, surah.surahNumber, newStars)
                                        memorizationMap = MyChildStore.getMemorizationMap(context, childProfile.id)
                                        onProfileUpdated()
                                        SfxManager.playSuccess(context)
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        if (isFilled) Icons.Filled.Star else Icons.Filled.StarBorder,
                                        contentDescription = t("نجمة $s", "Star $s"),
                                        tint = if (isFilled) Gold else Gold.copy(alpha = 0.3f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 4. BEDTIME STORY CONTENT (قصة قبل النوم)
// ==========================================
@Composable
fun BedtimeStoryContent(
    childProfile: ChildProfile,
    onProfileUpdated: () -> Unit
) {
    val context = LocalContext.current
    val todayStory = remember { BedtimeStoryManager.getTodayStory(context) }
    var showReader by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Today Bedtime Story Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .noorGlassCard(cornerRadius = 20.dp)
                .padding(18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.NightsStay, contentDescription = t("قصة قبل النوم", "Bedtime Story"), tint = Gold, modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(10.dp))
                Text("قصة قبل النوم لـ ${childProfile.name} 🌙", color = Gold, fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = AmiriFamily)
            }

            Spacer(Modifier.height(12.dp))

            Text(todayStory.title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text(todayStory.summary, color = GoldSoft, fontSize = 13.sp)

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    showReader = true
                    SfxManager.playTap(context)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Gold),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.MenuBook, contentDescription = t("قراءة القصة", "Read Story"), tint = Navy)
                Spacer(Modifier.width(6.dp))
                Text("اقرأ قصة الليلة الآن 📖", color = Navy, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Daily Bedtime Notification Settings Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .noorGlassCard(cornerRadius = 20.dp)
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.NotificationsActive, contentDescription = t("التنبيهات", "Notifications"), tint = Gold, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("تنبيه قصة قبل النوم اليومي ⏰", color = Gold, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(8.dp))
            Text("يتلقى الوالدان تنبيهاً لطيّفاً في الموعد المختار لقراءة قصة قبل النوم مع الطفل.", color = GoldSoft.copy(alpha = 0.7f), fontSize = 12.sp)

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = {
                    BedtimeStoryManager.scheduleBedtimeNotification(context, childProfile)
                    SfxManager.playSuccess(context)
                    Toast.makeText(context, t("تم ضبط تنبيه قصة قبل النوم الساعة 8:00 مساءً 🌙", "Bedtime story reminder set for 8:00 PM 🌙"), Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = NavyCard),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Gold)
            ) {
                Text("تفعيل التنبيه اليومي 🔔 (الساعة ٨:٠٠ م)", color = Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showReader) {
        BedtimeStoryReaderDialog(
            story = todayStory,
            onDismiss = {
                showReader = false
                MyChildStore.addStarsToChild(context, childProfile.id, 3)
                onProfileUpdated()
                SfxManager.playSuccess(context)
                Toast.makeText(context, t("قرأتم القصة معاً! تم منح طفلك +٣ نجوم 🌟", "Read story together! Awarded +3 stars 🌟"), Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
private fun BedtimeStoryReaderDialog(
    story: BedtimeStoryItem,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Gold),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("قرأنا القصة اليوم! 🌟 (+٣ نجوم)", color = Navy, fontWeight = FontWeight.Bold)
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.NightsStay, contentDescription = story.title, tint = Gold)
                Spacer(Modifier.width(8.dp))
                Text(story.title, color = Gold, fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = AmiriFamily)
            }
        },
        text = {
            LazyColumn(modifier = Modifier.heightIn(max = 350.dp)) {
                item {
                    Text(
                        story.content,
                        color = Color.White,
                        fontSize = 15.sp,
                        lineHeight = 24.sp,
                        fontFamily = AmiriFamily
                    )
                }
            }
        },
        containerColor = NavyCard,
        shape = RoundedCornerShape(20.dp)
    )
}

// ==========================================
// 5. FAMILY CHALLENGE CONTENT (تحدي عائلي)
// ==========================================
@Composable
fun FamilyChallengeContent(
    childProfile: ChildProfile,
    onProfileUpdated: () -> Unit
) {
    val context = LocalContext.current
    var isGameStarted by remember { mutableStateOf(false) }
    var questions by remember { mutableStateOf(emptyList<FamilyQuizQuestion>()) }
    var currentRoundIdx by remember { mutableIntStateOf(0) }
    var parentScore by remember { mutableIntStateOf(0) }
    var childScore by remember { mutableIntStateOf(0) }
    var isGameOver by remember { mutableStateOf(false) }

    if (!isGameStarted) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .noorGlassCard(cornerRadius = 24.dp)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Filled.SportsEsports, contentDescription = t("تحدي عائلي", "Family Challenge"), tint = Gold, modifier = Modifier.size(56.dp))
                Spacer(Modifier.height(12.dp))
                Text("التحدي العائلي ⚔️", color = Gold, fontSize = 24.sp, fontWeight = FontWeight.Bold, fontFamily = AmiriFamily)
                Spacer(Modifier.height(6.dp))
                Text("الوالد/الوالدة ضد ${childProfile.name}", color = Color.White, fontSize = 16.sp)
                Text("مسابقة ثقافية إسلامية ممتعة في ٥ جولات", color = GoldSoft, fontSize = 12.sp)

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = {
                        questions = FamilyQuizEngine.getQuizQuestions(context, 5)
                        currentRoundIdx = 0
                        parentScore = 0
                        childScore = 0
                        isGameOver = false
                        isGameStarted = true
                        SfxManager.playTap(context)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Gold),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("بدء التحدي الآن 🚀", color = Navy, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        return
    }

    if (isGameOver) {
        val winnerName = if (childScore > parentScore) childProfile.name else if (parentScore > childScore) t("الوالد/الوالدة 👑", "Parent 👑") else t("تعادل رائع 🤝", "Great Draw 🤝")
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .noorGlassCard(cornerRadius = 24.dp)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🏆 بطل التحدي العائلي 🏆", color = Gold, fontSize = 22.sp, fontWeight = FontWeight.Bold, fontFamily = AmiriFamily)
                Spacer(Modifier.height(12.dp))
                Text(winnerName, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MetricBox(icon = Icons.Filled.Person, label = t("الوالد/الوالدة", "Parent"), value = "${toArabicDigits(parentScore)} ${t("نقطة", "pts")}")
                    MetricBox(icon = Icons.Filled.Face, label = childProfile.name, value = "${toArabicDigits(childScore)} ${t("نقطة", "pts")}")
                }

                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = {
                        MyChildStore.addStarsToChild(context, childProfile.id, 5)
                        onProfileUpdated()
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, t("فزنا في التحدي العائلي بتطبيق نور! 🏆 النتيجة: الوالد $parentScore - ${childProfile.name} $childScore", "We won the Family Challenge in NOOR! 🏆 Result: Parent $parentScore - ${childProfile.name} $childScore"))
                        }
                        context.startActivity(Intent.createChooser(shareIntent, t("مشاركة نتيجة التحدي", "Share Challenge Result")))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Gold),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Share, contentDescription = t("مشاركة", "Share"), tint = Navy)
                    Spacer(Modifier.width(6.dp))
                    Text("مشاركة بطاقة الفوز وإهداء +٥ نجوم 🌟", color = Navy, fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(10.dp))

                TextButton(onClick = { isGameStarted = false }) {
                    Text("العودة للرئيسية", color = Gold)
                }
            }
        }
        return
    }

    // Play Battle Round
    val currentQuestion = questions.getOrNull(currentRoundIdx) ?: return
    val isChildTurn = currentRoundIdx % 2 == 1
    val turnLabel = if (isChildTurn) t("دور ${childProfile.name} 🧒", "${childProfile.name}'s turn 🧒") else t("دور الوالد/الوالدة 👑", "Parent's turn 👑")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Score Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(color = NavyCard, shape = RoundedCornerShape(12.dp)) {
                Text("الوالد: $parentScore", color = Gold, modifier = Modifier.padding(10.dp), fontWeight = FontWeight.Bold)
            }
            Text("الجولة ${currentRoundIdx + 1} من ٥", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Surface(color = NavyCard, shape = RoundedCornerShape(12.dp)) {
                Text("${childProfile.name}: $childScore", color = Gold, modifier = Modifier.padding(10.dp), fontWeight = FontWeight.Bold)
            }
        }

        // Question Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .noorGlassCard(cornerRadius = 20.dp)
                .padding(18.dp)
        ) {
            Text(turnLabel, color = Gold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(currentQuestion.question, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold, fontFamily = AmiriFamily)

            Spacer(Modifier.height(16.dp))

            currentQuestion.options.forEachIndexed { optionIdx, optionText ->
                Button(
                    onClick = {
                        val isCorrect = optionIdx == currentQuestion.correctIndex
                        if (isCorrect) {
                            if (isChildTurn) childScore += 10 else parentScore += 10
                            SfxManager.playSuccess(context)
                            Toast.makeText(context, t("إجابة صحيحة! 👏", "Correct answer! 👏"), Toast.LENGTH_SHORT).show()
                        } else {
                            SfxManager.playWrong(context)
                            Toast.makeText(context, t("إجابة خاطئة", "Incorrect answer"), Toast.LENGTH_SHORT).show()
                        }

                        if (currentRoundIdx + 1 < questions.size) {
                            currentRoundIdx++
                        } else {
                            isGameOver = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyCard),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Gold.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Text(optionText, color = Color.White, fontSize = 14.sp)
                }
            }
        }
    }
}

// --- Common Dialogs ---
@Composable
private fun AddChildDialog(
    onDismiss: () -> Unit,
    onSave: (String, Int, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var age by remember { mutableIntStateOf(7) }
    var selectedAvatar by remember { mutableStateOf("🦁") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) onSave(name.trim(), age, selectedAvatar)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Gold)
            ) {
                Text("حفظ الملف", color = Navy, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء", color = Gold) }
        },
        title = { Text("إضافة طفل جديد 👶", color = Gold, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم الطفل", color = GoldSoft) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Gold,
                        unfocusedBorderColor = GoldSoft.copy(alpha = 0.5f),
                        focusedLabelColor = Gold
                    )
                )

                Text("العمر: $age سنوات", color = Color.White)
                Slider(
                    value = age.toFloat(),
                    onValueChange = { age = it.toInt() },
                    valueRange = 3f..15f,
                    steps = 12
                )

                Text("اختر رمزي الخيالي:", color = Gold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(AVATAR_OPTIONS) { emoji ->
                        Surface(
                            color = if (selectedAvatar == emoji) Gold.copy(alpha = 0.3f) else NavyCard,
                            shape = CircleShape,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Gold),
                            modifier = Modifier.clickable { selectedAvatar = emoji }
                        ) {
                            Text(emoji, fontSize = 22.sp, modifier = Modifier.padding(8.dp))
                        }
                    }
                }
            }
        },
        containerColor = NavyCard
    )
}

@Composable
private fun PinVerificationDialog(
    title: String,
    desc: String,
    onDismiss: () -> Unit,
    onVerified: () -> Unit
) {
    val context = LocalContext.current
    var pin by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    if (MyChildStore.verifyParentPin(context, pin)) {
                        onVerified()
                    } else {
                        Toast.makeText(context, t("رمز PIN غير صحيح", "Incorrect PIN"), Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Gold)
            ) {
                Text("تأكيد", color = Navy, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء", color = Gold) }
        },
        title = { Text(title, color = Gold, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(desc, color = Color.White, fontSize = 12.sp)
                OutlinedTextField(
                    value = pin,
                    onValueChange = { if (it.length <= 4) pin = it },
                    label = { Text("رمز PIN (٤ أرقام)", color = GoldSoft) },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Gold,
                        unfocusedBorderColor = GoldSoft.copy(alpha = 0.5f)
                    )
                )
            }
        },
        containerColor = NavyCard
    )
}

@Composable
private fun AddRewardDialog(
    onDismiss: () -> Unit,
    onAdd: (String, Int, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var cost by remember { mutableIntStateOf(20) }
    var emoji by remember { mutableStateOf("🎁") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) onAdd(title.trim(), cost, emoji)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Gold)
            ) {
                Text("إضافة المكافأة", color = Navy, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء", color = Gold) } },
        title = { Text("إضافة هدية أو مكافأة 🎁", color = Gold, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("اسم الهادءة / المكافأة", color = GoldSoft) },
                    singleLine = true
                )
                Text("التكلفة بالنجوم: $cost نجمة", color = Color.White)
                Slider(
                    value = cost.toFloat(),
                    onValueChange = { cost = it.toInt() },
                    valueRange = 5f..100f,
                    steps = 19
                )
            }
        },
        containerColor = NavyCard
    )
}
