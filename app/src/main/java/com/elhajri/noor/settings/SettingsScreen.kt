package com.elhajri.noor.settings
import com.elhajri.noor.settings.t
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableIntStateOf
import com.elhajri.noor.notification.AdhanScheduler
import com.elhajri.noor.notification.AdhanSettingsStore
import com.elhajri.noor.notification.AdhanSoundOptions

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.ui.AmiriFamily
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.Navy
import com.elhajri.noor.ui.NavyCard
import com.elhajri.noor.ui.NavyLight
import com.elhajri.noor.ui.TextMain

/** حامل مقياس الخط — تُحدّثه شاشة الإعدادات فينعكس فوراً على التطبيق كله */
object FontScaleHolder {
    var scale by androidx.compose.runtime.mutableFloatStateOf(1.0f)
}

/**
 * الإعدادات — طبق الأصل عن صفحة الإعدادات في الموقع:
 * نفس الأقسام والخيارات والمفاتيح، وتُطبَّق فعلياً على المحتوى
 * الأصيل وعلى صفحات الويب عبر حقن localStorage.
 */
@Composable
fun SettingsScreen(onOpenPrivacy: () -> Unit = {}) {
    val context = LocalContext.current

    var dataSaver by remember { mutableStateOf(NoorSettings.getDataSaver(context)) }
    var lang by remember { mutableStateOf(NoorSettings.getLang(context)) }
    var reciter by remember { mutableStateOf(NoorSettings.getReciter(context)) }
    var audioQuality by remember { mutableStateOf(NoorSettings.getAudioQuality(context)) }
    var sfxEnabled by remember { mutableStateOf(com.elhajri.noor.audio.SfxManager.isEnabled(context)) }
    var fontSize by remember { mutableStateOf(NoorSettings.getFontSize(context)) }
    var city by remember { mutableStateOf(com.elhajri.noor.data.Prefs.getCity(context) ?: com.elhajri.noor.data.City("مكة المكرمة", 21.4225, 39.8262, "السعودية", "Asia/Riyadh")) }
    var isAutoLocation by remember { mutableStateOf(com.elhajri.noor.data.Prefs.isAutoLocation(context)) }
    var showCityPicker by remember { mutableStateOf(false) }
    var citySearch by remember { mutableStateOf("") }
    val allCities = remember { com.elhajri.noor.data.DataLoader.cities(context) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Navy)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(t("الإعدادات", "Settings"), color = Gold, fontSize = 20.sp, fontWeight = FontWeight.Bold, fontFamily = AmiriFamily)
        Spacer(Modifier.height(16.dp))

        // ============ البيانات ============
        SettingsSection(icon = Icons.Filled.Wifi, title = t("البيانات", "Data & Storage")) {
            SettingsRow(label = t("وضع توفير البيانات", "Data Saver Mode"), desc = t("يقلل جودة الصوت ويحفظ الردود محلياً (يوفر ~٦٠٪)", "Reduces audio quality and saves responses locally (saves ~60%)")) {
                GoldToggle(
                    on = dataSaver,
                    onToggle = {
                        dataSaver = it
                        NoorSettings.setDataSaver(context, it)
                    }
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // ============ اللغة ============
        SettingsSection(icon = Icons.Filled.Language, title = t("اللغة", "Language")) {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text(t("اختر لغة التطبيق", "Choose app language"), color = TextMain.copy(alpha = 0.5f), fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("ar" to t("العربية", "العربية"), "en" to "English").forEach { (code, name) ->
                        Text(
                            name,
                            color = if (lang == code) Navy else TextMain,
                            fontSize = 13.sp,
                            fontWeight = if (lang == code) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (lang == code) Gold else NavyLight.copy(alpha = 0.5f))
                                .clickable {
                                    lang = code
                                    com.elhajri.noor.settings.NoorLocale.set(context, code)
                                }
                                .padding(vertical = 10.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // ============ تحدي اليوم والإشعارات ============
        SettingsSection(icon = Icons.Filled.NotificationsActive, title = t("تنبيهات تحدي اليوم", "Daily Challenge Reminders")) {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                com.elhajri.noor.games.DailyChallengeNotificationSettingsCard()
            }
        }

        Spacer(Modifier.height(12.dp))

        // ============ المدينة والموقع ============
        SettingsSection(icon = Icons.Filled.LocationOn, title = t("المدينة والموقع", "City & Location")) {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(t("التحديد التلقائي للموقع", "Auto Location Detection"), color = TextMain, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(t("انتقال تلقائي حسب الموقع الجغرافي (GPS)", "Auto switch prayer times based on GPS location"), color = TextMain.copy(alpha = 0.5f), fontSize = 11.sp)
                    }
                    GoldToggle(
                        on = isAutoLocation,
                        onToggle = { enabled ->
                            isAutoLocation = enabled
                            com.elhajri.noor.data.Prefs.setAutoLocation(context, enabled)
                            if (enabled) {
                                com.elhajri.noor.prayer.NoorLocationManager.updateAutoLocation(context, force = true) { newCity ->
                                    if (newCity != null) {
                                        city = newCity
                                    }
                                }
                            }
                        }
                    )
                }

                Spacer(Modifier.height(12.dp))

                Text(t("مدينة الصلاة والقبلة", "Prayer times & Qibla city"), color = TextMain.copy(alpha = 0.5f), fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(NavyLight.copy(alpha = 0.5f))
                        .clickable { showCityPicker = true }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isAutoLocation) Icons.Filled.MyLocation else Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = Gold,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(city.name, color = TextMain, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            if (isAutoLocation) {
                                Spacer(Modifier.width(6.dp))
                                Text(t("(تلقائي)", "(Auto)"), color = Gold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Text(city.country, color = GoldSoft, fontSize = 11.sp)
                    }
                    Text(t("تغيير", "Change"), color = Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // ============ تخصيص الأذان والتنبيهات ============
        AdhanCustomizationSection()

        Spacer(Modifier.height(12.dp))

        // ============ القرآن الكريم — القارئ الافتراضي ============
        SettingsSection(icon = Icons.Filled.VolumeUp, title = t("القرآن الكريم", "Holy Qur'an")) {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text(t("القارئ الافتراضي", "Default Reciter"), color = TextMain.copy(alpha = 0.5f), fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("alafasy" to t("مشاري العفاسي", "Mishary Alafasy"), "dossari" to t("ياسر الدوسري", "Yasser Al-Dosari")).forEach { (id, name) ->
                        Text(
                            name,
                            color = if (reciter == id) Navy else TextMain,
                            fontSize = 12.sp,
                            fontWeight = if (reciter == id) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (reciter == id) Gold else NavyLight.copy(alpha = 0.5f))
                                .clickable {
                                    reciter = id
                                    NoorSettings.setReciter(context, id)
                                }
                                .padding(vertical = 10.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // ============ تأثيرات الصوت ============
        SettingsSection(icon = Icons.Filled.VolumeUp, title = t("تأثيرات الصوت", "Sound Effects")) {
            SettingsRow(
                label = t("تأثيرات الصوت والتفاعل", "Sound Effects"),
                desc = t("أصوات أزرار وتفاعلات هادئة وأصوات إنجاز للألعاب", "Gentle button clicks and achievement sounds")
            ) {
                GoldToggle(
                    on = sfxEnabled,
                    onToggle = { enabled ->
                        sfxEnabled = enabled
                        com.elhajri.noor.audio.SfxManager.setEnabled(context, enabled)
                        if (enabled) {
                            com.elhajri.noor.audio.SfxManager.playTap(context)
                        }
                    }
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // ============ جودة الصوت ============
        SettingsSection(icon = Icons.Filled.Equalizer, title = t("جودة الصوت", "Audio Quality")) {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text(t("جودة التلاوة والأناشيد", "Recitation & Nasheed Quality"), color = TextMain.copy(alpha = 0.5f), fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("normal" to t("عادية (٦٤kbps)", "Standard (64 kbps)"), "high" to t("عالية (١٢٨kbps)", "High (128 kbps)")).forEach { (id, name) ->
                        Text(
                            name,
                            color = if (audioQuality == id) Navy else TextMain,
                            fontSize = 12.sp,
                            fontWeight = if (audioQuality == id) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (audioQuality == id) Gold else NavyLight.copy(alpha = 0.5f))
                                .clickable {
                                    audioQuality = id
                                    NoorSettings.setAudioQuality(context, id)
                                }
                                .padding(vertical = 10.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // ============ حجم الخط ============
        SettingsSection(icon = Icons.Filled.Tune, title = t("حجم الخط", "Font Size")) {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text(t("حجم نص التطبيق", "App Text Size"), color = TextMain.copy(alpha = 0.5f), fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        "small" to t("صغير", "Small"), "medium" to t("متوسط", "Medium"),
                        "large" to t("كبير", "Large"), "xlarge" to t("كبير جداً", "Extra Large")
                    ).forEach { (id, name) ->
                        Text(
                            name,
                            color = if (fontSize == id) Navy else TextMain,
                            fontSize = 11.sp,
                            fontWeight = if (fontSize == id) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (fontSize == id) Gold else NavyLight.copy(alpha = 0.5f))
                                .clickable {
                                    fontSize = id
                                    NoorSettings.setFontSize(context, id)
                                    FontScaleHolder.scale = NoorSettings.fontScale(context)
                                }
                                .padding(vertical = 10.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // ============ حول التطبيق ============
        SettingsSection(icon = Icons.Filled.Info, title = t("حول التطبيق", "About App")) {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text(t("نور", "NOOR"), color = TextMain, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(t("تطبيق الإسلام الشامل", "Comprehensive Islamic App"), color = TextMain.copy(alpha = 0.5f), fontSize = 12.sp)
                Text(t("القرآن · الأذكار · الصلاة · القبلة · القصص · الألعاب · الذكاء الاصطناعي", "Qur'an · Adhkar · Prayer · Qibla · Stories · Games · Smart AI"), color = TextMain.copy(alpha = 0.5f), fontSize = 12.sp)
                Text(t("النسخة ", "Version ") + context.packageManager.getPackageInfo(context.packageName, 0).versionName.toString(), color = TextMain.copy(alpha = 0.5f), fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    t("سياسة الخصوصية ←", "Privacy Policy ←"),
                    color = Gold, fontSize = 12.sp,
                    modifier = Modifier.clickable { onOpenPrivacy() }
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // ============ إعادة الإعدادات الافتراضية ============
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(NavyLight.copy(alpha = 0.45f))
                .clickable {
                    NoorSettings.reset(context)
                    dataSaver = false; lang = "ar"; reciter = "alafasy"
                    audioQuality = "high"; fontSize = "medium"
                    FontScaleHolder.scale = 1.0f
                }
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Refresh, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text(t("إعادة الإعدادات الافتراضية", "Reset to Default Settings"), color = Color(0xFFEF4444), fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(24.dp))
    }

    // ═══ اختيار المدينة ═══
    if (showCityPicker) {
        AlertDialog(
            onDismissRequest = { showCityPicker = false },
            title = { Text(t("اختر مدينتك أو التحديد التلقائي", "Select City or Auto Detect"), color = Gold, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isAutoLocation) Gold else NavyLight)
                            .clickable {
                                isAutoLocation = true
                                com.elhajri.noor.data.Prefs.setAutoLocation(context, true)
                                com.elhajri.noor.prayer.NoorLocationManager.updateAutoLocation(context, force = true) { newCity ->
                                    if (newCity != null) {
                                        city = newCity
                                    }
                                    showCityPicker = false
                                }
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.MyLocation,
                            contentDescription = null,
                            tint = if (isAutoLocation) Navy else Gold,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                t("الموقع التلقائي (GPS)", "Auto Location (GPS)"),
                                color = if (isAutoLocation) Navy else Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                t("انتقال تلقائي للمواقيت حسب موقعك", "Auto-switch prayer times based on your location"),
                                color = if (isAutoLocation) Navy.copy(alpha = 0.7f) else GoldSoft.copy(alpha = 0.7f),
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = citySearch,
                        onValueChange = { citySearch = it },
                        placeholder = { Text(t("ابحث عن مدينة (300+ مدينة عالمية)...", "Search city (300+ world cities)..."), fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Gold,
                            focusedBorderColor = Gold,
                            unfocusedBorderColor = NavyLight
                        )
                    )
                    Column(Modifier.verticalScroll(rememberScrollState()).height(300.dp)) {
                        val q = citySearch.trim()
                        val filtered = if (q.isBlank()) allCities else allCities.filter { it.name.contains(q) || it.country.contains(q) }
                        filtered.forEach { c ->
                            val selected = !isAutoLocation && c.name == city.name && c.country == city.country
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (selected) Gold else Color.Transparent)
                                    .clickable {
                                        isAutoLocation = false
                                        com.elhajri.noor.data.Prefs.setAutoLocation(context, false)
                                        city = c
                                        com.elhajri.noor.data.Prefs.setCity(context, c)
                                        com.elhajri.noor.notification.AdhanScheduler.scheduleNextAdhan(context)
                                        showCityPicker = false
                                    }
                                    .padding(horizontal = 10.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.LocationOn, contentDescription = null, tint = if (selected) Navy else Gold, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(c.name, color = if (selected) Navy else Color.White, fontSize = 14.sp)
                                Spacer(Modifier.width(6.dp))
                                Text(c.country, color = if (selected) Navy.copy(alpha = 0.7f) else GoldSoft.copy(alpha = 0.6f), fontSize = 11.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCityPicker = false }) { Text(t("إغلاق", "Close"), color = GoldSoft) }
            },
            containerColor = NavyCard
        )
    }
}

/** قسم إعدادات — بطاقة بأيقونة ذهبية وعنوان، كما في الموقع */
@Composable
private fun SettingsSection(icon: ImageVector, title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(NavyLight.copy(alpha = 0.5f))
            .border(1.dp, Gold.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(32.dp).clip(RoundedCornerShape(10.dp)).background(Gold.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Gold, modifier = Modifier.size(16.dp))
            }
            Spacer(Modifier.width(10.dp))
            Text(title, color = TextMain, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        content()
    }
}

/** صف إعدادات — تسمية ووصف مع عنصر تحكم */
@Composable
private fun SettingsRow(label: String, desc: String, control: @Composable () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = TextMain, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            if (desc.isNotEmpty()) {
                Text(desc, color = TextMain.copy(alpha = 0.5f), fontSize = 11.sp)
            }
        }
        Spacer(Modifier.width(12.dp))
        control()
    }
}

/** زر تبديل ذهبي أنيق متناسق مع تصميم التطبيق */
@Composable
private fun GoldToggle(on: Boolean, onToggle: (Boolean) -> Unit) {
    Box(
        modifier = Modifier
            .width(44.dp)
            .height(24.dp)
            .clip(CircleShape)
            .background(if (on) Gold else NavyLight)
            .clickable { onToggle(!on) }
            .padding(3.dp),
        contentAlignment = if (on) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .background(if (on) Navy else TextMain.copy(alpha = 0.4f))
        )
    }
}



@Composable
private fun AdhanCustomizationSection() {
    val context = LocalContext.current

    var masterEnabled by remember { mutableStateOf(AdhanSettingsStore.isAdhanEnabled(context)) }
    var selectedSound by remember { mutableStateOf(AdhanSettingsStore.getSelectedAdhanSound(context)) }
    var preReminderEnabled by remember { mutableStateOf(AdhanSettingsStore.isPreAdhanReminderEnabled(context)) }
    var preMinutes by remember { mutableIntStateOf(AdhanSettingsStore.getPreAdhanMinutes(context)) }

    var fajrEnabled by remember { mutableStateOf(AdhanSettingsStore.isPrayerEnabled(context, t("الفجر", "Fajr"))) }
    var dhuhrEnabled by remember { mutableStateOf(AdhanSettingsStore.isPrayerEnabled(context, t("الظهر", "Dhuhr"))) }
    var asrEnabled by remember { mutableStateOf(AdhanSettingsStore.isPrayerEnabled(context, t("العصر", "Asr"))) }
    var maghribEnabled by remember { mutableStateOf(AdhanSettingsStore.isPrayerEnabled(context, t("المغرب", "Maghrib"))) }
    var ishaEnabled by remember { mutableStateOf(AdhanSettingsStore.isPrayerEnabled(context, t("العشاء", "Isha"))) }

    var previewPlayer by remember { mutableStateOf<android.media.MediaPlayer?>(null) }
    var previewSoundId by remember { mutableStateOf<String?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            try {
                previewPlayer?.stop()
                previewPlayer?.release()
            } catch (_: Exception) {}
            previewPlayer = null
        }
    }

    SettingsSection(
        icon = Icons.Filled.NotificationsActive,
        title = t("تخصيص الأذان والتنبيهات", "Adhan Customization & Reminders")
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            // 1. Master Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(t("تفعيل إشعارات الأذان", "Enable Adhan Notifications"), color = TextMain, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(t("تشغيل الأذان الصوتي والتنبيهات عند دخول وقت الصلاة", "Play audio Adhan and alert at prayer times"), color = TextMain.copy(alpha = 0.5f), fontSize = 11.sp)
                }
                GoldToggle(
                    on = masterEnabled,
                    onToggle = { enabled ->
                        masterEnabled = enabled
                        AdhanSettingsStore.setAdhanEnabled(context, enabled)
                        if (enabled) {
                            AdhanScheduler.scheduleNextAdhan(context)
                        } else {
                            AdhanScheduler.cancel(context)
                        }
                    }
                )
            }

            if (masterEnabled) {
                Spacer(Modifier.height(14.dp))
                HorizontalDivider(color = Gold.copy(alpha = 0.15f))
                Spacer(Modifier.height(14.dp))

                // 2. Adhan Sound Picker
                Text(t("صوت الأذان المفضل", "Preferred Adhan Audio"), color = Gold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))

                AdhanSoundOptions.options.forEach { opt ->
                    val isSelected = selectedSound == opt.id
                    val isPreviewing = previewSoundId == opt.id

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                1.dp,
                                if (isSelected) Gold else Gold.copy(alpha = 0.15f),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                selectedSound = opt.id
                                AdhanSettingsStore.setSelectedAdhanSound(context, opt.id)
                                AdhanScheduler.scheduleNextAdhan(context)
                            },
                        color = if (isSelected) NavyCard else NavyLight.copy(alpha = 0.3f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = {
                                            selectedSound = opt.id
                                            AdhanSettingsStore.setSelectedAdhanSound(context, opt.id)
                                            AdhanScheduler.scheduleNextAdhan(context)
                                        },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = Gold,
                                            unselectedColor = TextMain.copy(alpha = 0.4f)
                                        )
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(opt.nameAr, color = TextMain, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                                Text(
                                    opt.descriptionAr,
                                    color = TextMain.copy(alpha = 0.6f),
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(start = 32.dp)
                                )
                            }

                            // Audio Preview button
                            TextButton(
                                onClick = {
                                    try {
                                        previewPlayer?.stop()
                                        previewPlayer?.release()
                                    } catch (_: Exception) {}
                                    previewPlayer = null

                                    if (isPreviewing) {
                                        previewSoundId = null
                                    } else {
                                        previewSoundId = opt.id
                                        try {
                                            val mp = android.media.MediaPlayer.create(context, opt.resId)
                                            mp?.setOnCompletionListener {
                                                previewSoundId = null
                                            }
                                            mp?.start()
                                            previewPlayer = mp
                                        } catch (_: Exception) {
                                            previewSoundId = null
                                        }
                                    }
                                },
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = if (isPreviewing) Gold else GoldSoft
                                )
                            ) {
                                Icon(
                                    imageVector = if (isPreviewing) Icons.Default.Stop else Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(if (isPreviewing) "إيقاف" else "استماع", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))
                HorizontalDivider(color = Gold.copy(alpha = 0.15f))
                Spacer(Modifier.height(14.dp))

                // 3. Per-Prayer Toggles
                Text("أذان الصلوات الخمس", color = Gold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text("تفعيل أو إيقاف الأذان لكل صلاة بشكل منفصل", color = TextMain.copy(alpha = 0.5f), fontSize = 11.sp)
                Spacer(Modifier.height(10.dp))

                val prayers = listOf(
                    Triple(t("الفجر", "Fajr"), fajrEnabled) { v: Boolean ->
                        fajrEnabled = v
                        AdhanSettingsStore.setPrayerEnabled(context, t("الفجر", "Fajr"), v)
                    },
                    Triple(t("الظهر", "Dhuhr"), dhuhrEnabled) { v: Boolean ->
                        dhuhrEnabled = v
                        AdhanSettingsStore.setPrayerEnabled(context, t("الظهر", "Dhuhr"), v)
                    },
                    Triple(t("العصر", "Asr"), asrEnabled) { v: Boolean ->
                        asrEnabled = v
                        AdhanSettingsStore.setPrayerEnabled(context, t("العصر", "Asr"), v)
                    },
                    Triple(t("المغرب", "Maghrib"), maghribEnabled) { v: Boolean ->
                        maghribEnabled = v
                        AdhanSettingsStore.setPrayerEnabled(context, t("المغرب", "Maghrib"), v)
                    },
                    Triple(t("العشاء", "Isha"), ishaEnabled) { v: Boolean ->
                        ishaEnabled = v
                        AdhanSettingsStore.setPrayerEnabled(context, t("العشاء", "Isha"), v)
                    }
                )

                prayers.forEach { (pName, pState, pToggle) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("صلاة $pName", color = TextMain, fontSize = 13.sp)
                        GoldToggle(
                            on = pState,
                            onToggle = { enabled ->
                                pToggle(enabled)
                                AdhanScheduler.scheduleNextAdhan(context)
                            }
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))
                HorizontalDivider(color = Gold.copy(alpha = 0.15f))
                Spacer(Modifier.height(14.dp))

                // 4. Pre-Adhan Reminder
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("التذكير المسبق قبل الأذان", color = Gold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("إرسال تنبيه مؤدب قبل حلول وقت الصلاة للاستعداد", color = TextMain.copy(alpha = 0.5f), fontSize = 11.sp)
                    }
                    GoldToggle(
                        on = preReminderEnabled,
                        onToggle = { enabled ->
                            preReminderEnabled = enabled
                            AdhanSettingsStore.setPreAdhanReminderEnabled(context, enabled)
                            AdhanScheduler.scheduleNextAdhan(context)
                        }
                    )
                }

                if (preReminderEnabled) {
                    Spacer(Modifier.height(10.dp))
                    Text("الوقت قبل الأذان (بالدقائق):", color = TextMain, fontSize = 12.sp)
                    Spacer(Modifier.height(6.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf(5, 10, 15, 20, 30).forEach { mins ->
                            val isSelected = preMinutes == mins
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) Gold else NavyLight.copy(alpha = 0.4f))
                                    .border(1.dp, if (isSelected) Gold else Gold.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                                    .clickable {
                                        preMinutes = mins
                                        AdhanSettingsStore.setPreAdhanMinutes(context, mins)
                                        AdhanScheduler.scheduleNextAdhan(context)
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    if (NoorLocale.isEnglish) "$mins m" else "$mins د",
                                    color = if (isSelected) Navy else TextMain,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Live Arabic Notification Preview Box
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, Gold.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                        color = NavyCard
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = Gold,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("معاينة نص التنبيه المسبق:", color = Gold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.height(6.dp))
                            Text("🕌 اقترب موعد صلاة الظهر", color = TextMain, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(
                                t("باقي $preMinutes دقائق على أذان صلاة الظهر — استعد للصلاة والوضوء", "$preMinutes minutes remaining until Dhuhr Adhan — prepare for prayer and Wudu"),
                                color = TextMain.copy(alpha = 0.8f),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}