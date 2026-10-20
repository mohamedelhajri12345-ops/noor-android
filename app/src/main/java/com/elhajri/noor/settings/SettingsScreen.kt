package com.elhajri.noor.settings

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
        Text("الإعدادات", color = Gold, fontSize = 20.sp, fontWeight = FontWeight.Bold, fontFamily = AmiriFamily)
        Spacer(Modifier.height(16.dp))

        // ============ البيانات ============
        SettingsSection(icon = Icons.Filled.Wifi, title = "البيانات") {
            SettingsRow(label = "وضع توفير البيانات", desc = "يقلل جودة الصوت ويحفظ الردود محلياً (يوفر ~٦٠٪)") {
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

        // ============ المدينة والموقع ============
        SettingsSection(icon = Icons.Filled.LocationOn, title = t("المدينة والموقع", "City & Location")) {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("التحديد التلقائي للموقع", color = TextMain, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("انتقال تلقائي حسب الموقع الجغرافي (GPS)", color = TextMain.copy(alpha = 0.5f), fontSize = 11.sp)
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
                                Text("(تلقائي)", color = Gold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Text(city.country, color = GoldSoft, fontSize = 11.sp)
                    }
                    Text("تغيير", color = Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // ============ القرآن الكريم — القارئ الافتراضي ============
        SettingsSection(icon = Icons.Filled.VolumeUp, title = "القرآن الكريم") {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text("القارئ الافتراضي", color = TextMain.copy(alpha = 0.5f), fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("alafasy" to "مشاري العفاسي", "dossari" to "ياسر الدوسري").forEach { (id, name) ->
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

        // ============ جودة الصوت ============
        SettingsSection(icon = Icons.Filled.Equalizer, title = "جودة الصوت") {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text("جودة التلاوة والأناشيد", color = TextMain.copy(alpha = 0.5f), fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("normal" to "عادية (٦٤kbps)", "high" to "عالية (١٢٨kbps)").forEach { (id, name) ->
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
        SettingsSection(icon = Icons.Filled.Tune, title = "حجم الخط") {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text("حجم نص التطبيق", color = TextMain.copy(alpha = 0.5f), fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        "small" to "صغير", "medium" to "متوسط",
                        "large" to "كبير", "xlarge" to "كبير جداً"
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
        SettingsSection(icon = Icons.Filled.Info, title = "حول التطبيق") {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text("نور", color = TextMain, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text("تطبيق الإسلام الشامل", color = TextMain.copy(alpha = 0.5f), fontSize = 12.sp)
                Text("القرآن · الأذكار · الصلاة · القبلة · القصص · الألعاب · الذكاء الاصطناعي", color = TextMain.copy(alpha = 0.5f), fontSize = 12.sp)
                Text("النسخة " + context.packageManager.getPackageInfo(context.packageName, 0).versionName.toString(), color = TextMain.copy(alpha = 0.5f), fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    "سياسة الخصوصية ←",
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
            Text("إعادة الإعدادات الافتراضية", color = Color(0xFFEF4444), fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(24.dp))
    }

    // ═══ اختيار المدينة ═══
    if (showCityPicker) {
        AlertDialog(
            onDismissRequest = { showCityPicker = false },
            title = { Text("اختر مدينتك أو التحديد التلقائي", color = Gold, fontWeight = FontWeight.Bold) },
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
                                "الموقع التلقائي (GPS)",
                                color = if (isAutoLocation) Navy else Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "انتقال تلقائي للمواقيت حسب موقعك",
                                color = if (isAutoLocation) Navy.copy(alpha = 0.7f) else GoldSoft.copy(alpha = 0.7f),
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = citySearch,
                        onValueChange = { citySearch = it },
                        placeholder = { Text("ابحث عن مدينة (300+ مدينة عالمية)...", fontSize = 12.sp) },
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
                TextButton(onClick = { showCityPicker = false }) { Text("إغلاق", color = GoldSoft) }
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


