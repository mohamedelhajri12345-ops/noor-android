package com.elhajri.noor.quran

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.ui.Gold

/**
 * رأس صفحة القرآن — طبق الأصل عن الموقع: صف أيقونات دائرية خفيفة على جانب،
 * شعار "نور" مع العنوان الفرعي على الجانب الآخر، وهلال ذهبي في دائرة مُعبّأة.
 */
@Composable
fun QuranPageHeader(
    onSettings: () -> Unit = {},
    onAssistant: () -> Unit = {},
    onFavorites: () -> Unit = {},
    onSearch: () -> Unit = {},
    onBrightness: () -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                HeaderIcon(onClick = onBrightness) {
                    Icon(Icons.Filled.WbSunny, contentDescription = "السطوع", tint = Gold, modifier = Modifier.size(16.dp))
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End) {
                    Text("نور", color = Gold, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    Text("تطبيق الإسلام الشامل", color = Gold.copy(alpha = 0.6f), fontSize = 10.sp)
                }
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(Gold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.NightsStay, contentDescription = "الوضع الليلي", tint = Color(0xFF0D2B1F), modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun HeaderIcon(onClick: () -> Unit, filled: Boolean = false, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .padding(horizontal = 3.dp)
            .size(32.dp)
            .background(
                if (filled) Color(0xFF1F8A4C) else Color.White.copy(alpha = 0.04f),
                CircleShape
            )
            .border(1.dp, Gold.copy(alpha = if (filled) 0f else 0.3f), CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

/** صف "الرجوع ›" أسفل الهيدر مباشرة — طبق الأصل عن الموقع */
@Composable
fun QuranBackRow(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onBack() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("الرجوع", color = Gold.copy(alpha = 0.85f), fontSize = 14.sp)
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Gold.copy(alpha = 0.85f), modifier = Modifier.size(18.dp))
    }
}
