package com.elhajri.noor.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.data.DataLoader
import com.elhajri.noor.data.Prefs
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.Navy
import com.elhajri.noor.ui.NavyCard
import com.elhajri.noor.ui.NoorGradients

/**
 * شاشة الإعدادات — بنية أقسام على نمط التطبيقات الإسلامية العالمية:
 * 1) الصوت والأذان   2) عرض القرآن   3) الموقع ومواقيت الصلاة
 * كل قسم داخل كرت زجاجي بحد ذهبي رقيق (هوية الأبنوس والذهب).
 */
@Composable
fun SettingsScreen() {
    val context = LocalContext.current

    var adhan by remember { mutableStateOf(Prefs.getAdhanEnabled(context)) }
    var adhanSound by remember { mutableStateOf(Prefs.getAdhanSound(context)) }
    var adhanVibrate by remember { mutableStateOf(Prefs.getAdhanVibrate(context)) }
    var fontSize by remember { mutableStateOf(Prefs.getReaderFontSize(context)) }
    var city by remember { mutableStateOf(Prefs.getCity(context)) }
    var showCityPicker by remember { mutableStateOf(false) }
    var search by remember { mutableStateOf("") }
    val cities = remember { DataLoader.cities(context).filter { it.name.isNotEmpty() }.distinctBy { it.name + it.country } }
    val currentReciter = remember { DataLoader.reciters(context).find { it.id == Prefs.getReciter(context) }?.name ?: "مشاري العفاسي" }

    Column(modifier = Modifier.fillMaxSize().background(NoorGradients.ScreenBackground).padding(16.dp)) {
        Text("الإعدادات", color = Gold, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("النسخة 4.0", color = GoldSoft, fontSize = 12.sp)
        Spacer(Modifier.height(16.dp))

        Column(modifier = Modifier
            .weight(1f)
            .verticalScroll(rememberScrollState())) {

            // ================= 1) الصوت والأذان =================
            SectionTitle("الصوت والأذان")
            SettingsCard {
                Column(Modifier.padding(14.dp)) {
                    SettingRow("تنبيه الأذان", adhan, { adhan = it; Prefs.setAdhanEnabled(context, it) })
                    SettingDivider()
                    SettingRow("صوت الأذان", adhanSound, { adhanSound = it; Prefs.setAdhanSound(context, it) })
                    SettingDivider()
                    SettingRow("الاهتزاز مع الأذان", adhanVibrate, { adhanVibrate = it; Prefs.setAdhanVibrate(context, it) })
                    SettingDivider()
                    Column(Modifier.padding(vertical = 6.dp)) {
                        Text("القارئ الافتراضي للتلاوات", color = GoldSoft, fontSize = 13.sp)
                        Text(currentReciter, color = androidx.compose.ui.graphics.Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("يمكن تغييره من قائمة القراء داخل شاشة السورة", color = GoldSoft.copy(alpha = 0.7f), fontSize = 11.sp)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))

            // ================= 2) عرض القرآن =================
            SectionTitle("عرض القرآن")
            SettingsCard {
                Column(Modifier.padding(14.dp)) {
                    Text("حجم خط المصحف", color = GoldSoft, fontSize = 13.sp)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        listOf("S" to "صغير", "M" to "متوسط", "L" to "كبير").forEach { (key, label) ->
                            val selected = fontSize == key
                            FilterChip(
                                selected = selected,
                                onClick = { fontSize = key; Prefs.setReaderFontSize(context, key) },
                                label = { Text(label, color = if (selected) Navy else GoldSoft, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = Navy,
                                    selectedContainerColor = Gold
                                ),
                            )
                        }
                    }
                    Text("التغيير يظهر فوراً في شاشة السورة", color = GoldSoft.copy(alpha = 0.7f), fontSize = 11.sp, modifier = Modifier.padding(top = 6.dp))
                }
            }
            Spacer(Modifier.height(16.dp))

            // ================= 3) الموقع ومواقيت الصلاة =================
            SectionTitle("الموقع ومواقيت الصلاة")
            SettingsCard(modifier = Modifier.clickable { showCityPicker = !showCityPicker }) {
                Column(Modifier.padding(14.dp)) {
                    Text("الموقع لتحديد مواقيت الصلاة", color = GoldSoft, fontSize = 13.sp)
                    Text(city?.let { "${it.name}، ${it.country}" } ?: "اضغط لاختيار مدينتك",
                        color = androidx.compose.ui.graphics.Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (showCityPicker) {
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = search, onValueChange = { search = it },
                    placeholder = { Text("ابحث عن مدينة...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Gold, unfocusedBorderColor = GoldSoft.copy(alpha = 0.4f),
                        focusedTextColor = androidx.compose.ui.graphics.Color.White,
                        unfocusedTextColor = androidx.compose.ui.graphics.Color.White,
                        cursorColor = Gold
                    )
                )
                Spacer(Modifier.height(8.dp))
                LazyColumn(modifier = Modifier.height(260.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    val filtered = cities.filter { it.name.contains(search) || it.country.contains(search) }
                    items(filtered.size) { i ->
                        Card(colors = CardDefaults.cardColors(containerColor = NavyCard), shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().clickable {
                                Prefs.setCity(context, filtered[i])
                                city = filtered[i]
                                showCityPicker = false
                            }) {
                            Text("${filtered[i].name}، ${filtered[i].country}", color = GoldSoft, fontSize = 15.sp, modifier = Modifier.padding(12.dp))
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Text("القرآن الكريم — الإصدار 2.0", color = GoldSoft.copy(alpha = 0.5f), fontSize = 12.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(title, color = Gold, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
}

@Composable
private fun SettingsCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = NavyCard), shape = RoundedCornerShape(14.dp),
        modifier = modifier.fillMaxWidth()) { content() }
}

@Composable
private fun SettingRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(label, color = androidx.compose.ui.graphics.Color.White, fontSize = 15.sp, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChange, colors = SwitchDefaults.colors(checkedTrackColor = Gold))
    }
}

@Composable
private fun SettingDivider() {
    HorizontalDivider(color = GoldSoft.copy(alpha = 0.15f), thickness = 0.5.dp)
}
