package com.elhajri.noor.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * عنوان القسم — طبق الأصل عن الموقع: نص فقط في الوسط (بلا صورة ولا صندوق أسود)،
 * يستبدل البانر القديم الذي كان يظهر كخانة سوداء فوق كل صفحة.
 */
@Composable
fun NoorSectionTitle(
    title: String,
    subtitle: String = "",
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, color = Gold, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        if (subtitle.isNotBlank()) {
            Text(subtitle, color = TextMain.copy(alpha = 0.55f), fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
        }
    }
}
