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
import androidx.compose.material.icons.filled.Translate
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
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.elhajri.noor.ui.Gold

/**
 * رأس صفحة القرآن — طبق الأصل عن الموقع: صف أيقونات دائرية خفيفة على جانب،
 * أيقونة التطبيق الرسمية مع «القرآن الكريم» — بلا هلال نهائياً كما هو معتمد.
 */
@Composable
fun QuranPageHeader(
    onSettings: () -> Unit = {},
    onAssistant: () -> Unit = {},
    onFavorites: () -> Unit = {},
    onSearch: () -> Unit = {},
    onBrightness: () -> Unit = {},
    onTranslation: () -> Unit = {},
    showTranslationActive: Boolean = false
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
                HeaderIcon(onClick = onTranslation, filled = showTranslationActive) {
                    Icon(Icons.Filled.Translate, contentDescription = "الترجمة الإنجليزية", tint = Gold, modifier = Modifier.size(16.dp))
                }
                HeaderIcon(onClick = onBrightness) {
                    Icon(Icons.Filled.WbSunny, contentDescription = "السطوع", tint = Gold, modifier = Modifier.size(16.dp))
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End) {
                    Text("القرآن الكريم", color = Gold, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                }
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(8.dp))
                AsyncImage(
                    model = com.elhajri.noor.R.mipmap.ic_launcher,
                    contentDescription = "القرآن الكريم",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(34.dp).background(Gold.copy(alpha = 0.14f), CircleShape)
                )
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
