package com.elhajri.noor.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    var city by remember { mutableStateOf(Prefs.getCity(context)) }
    var showCityPicker by remember { mutableStateOf(false) }
    var adhan by remember { mutableStateOf(Prefs.getAdhanEnabled(context)) }
    var search by remember { mutableStateOf("") }
    val cities = remember { DataLoader.cities(context).filter { it.name.isNotEmpty() }.distinctBy { it.name + it.country } }

    Column(modifier = Modifier.fillMaxSize().background(NoorGradients.ScreenBackground).padding(16.dp)) {
        Text("الإعدادات", color = Gold, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF070B14)Card), shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().clickable { showCityPicker = !showCityPicker }) {
            Column(Modifier.padding(16.dp)) {
                Text("الموقع لتحديد مواقيت الصلاة", color = GoldSoft, fontSize = 14.sp)
                Spacer(Modifier.height(4.dp))
                Text(city?.let { "${it.name}، ${it.country}" } ?: "اضغط لاختيار مدينتك", color = androidx.compose.ui.graphics.Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(10.dp))

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text("تنبيه الأذان", color = androidx.compose.ui.graphics.Color.White, fontSize = 16.sp, modifier = Modifier.weight(1f))
            Switch(checked = adhan, onCheckedChange = { adhan = it; Prefs.setAdhanEnabled(context, it) }, colors = SwitchDefaults.colors(checkedTrackColor = Gold))
        }

        if (showCityPicker) {
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = search, onValueChange = { search = it },
                placeholder = { Text("ابحث عن مدينة...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(8.dp))
            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                val filtered = cities.filter { it.name.contains(search) || it.country.contains(search) }
                items(filtered.size) { i ->
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF070B14)Card), shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().clickable {
                            Prefs.setCity(context, filtered[i])
                            city = filtered[i]
                            showCityPicker = false
                        }) {
                            Text("${filtered[i].name}، ${filtered[i].country}", color = GoldSoft, fontSize = 15.sp, modifier = Modifier.padding(12.dp))
                        }
                }
            }
        } else {
            Spacer(Modifier.weight(1f))
            Text("NOOR — نسخة أندرويد الأصلية", color = GoldSoft.copy(alpha = 0.5f), fontSize = 12.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
        }
    }
}
