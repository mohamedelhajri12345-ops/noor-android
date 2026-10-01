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
import androidx.compose.material.icons.filled.Languages
import androidx.compose.material.icons.filled.RotateCcw
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Volume2
import androidx.compose.material.icons.filled.Wifi
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
import com.elhajri.noor.ui.Navy
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
        SettingsSection(icon = Icons.Filled.Languages, title = "اللغة") {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text("اختر لغة التطبيق", color = TextMain.copy(alpha = 0.5f), fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("ar" to "العربية", "en" to "English").forEach { (code, name) ->
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
                                    NoorSettings.setLang(context, code)
                                }
                                .padding(vertical = 10.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // ============ القرآن الكريم — القارئ الافتراضي ============
        SettingsSection(icon = Icons.Filled.Volume2, title = "القرآن الكريم") {
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
                Text("النسخة 5.0", color = TextMain.copy(alpha = 0.5f), fontSize = 12.sp)
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
            Icon(Icons.Filled.RotateCcw, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text("إعادة الإعدادات الافتراضية", color = Color(0xFFEF4444), fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(24.dp))
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
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(label, color = TextMain, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(desc, color = TextMain.copy(alpha = 0.45f), fontSize = 11.sp, lineHeight = 15.sp)
        }
        Spacer(Modifier.width(12.dp))
        control()
    }
}

/** مفتاح تبديل ذهبي — كما Toggle بالموقع */
@Composable
private fun GoldToggle(on: Boolean, onToggle: (Boolean) -> Unit) {
    Box(
        modifier = Modifier
            .width(46.dp)
            .height(26.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(if (on) Gold else NavyLight.copy(alpha = 0.8f))
            .clickable { onToggle(!on) }
            .border(1.dp, if (on) Gold.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.08f), RoundedCornerShape(13.dp)),
        contentAlignment = if (on) Alignment.CenterStart else Alignment.CenterEnd
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 3.dp)
                .size(20.dp)
                .clip(CircleShape)
                .background(if (on) Navy else Color.White.copy(alpha = 0.7f))
        )
    }
}
