package com.elhajri.noor.quran

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.ui.Gold

/**
 * رأس صفحة القارئ كما هو معتمد:
 * زر الرجوع على اليسار (مكان الأيقونات سابقاً)،
 * وأيقونتا الترجمة والسطوع على اليمين (مكان «القرآن الكريم» والأيقونة — حُذفتا نهائياً).
 */
@Composable
fun QuranPageHeader(
    onBack: () -> Unit = {},
    onBrightness: () -> Unit = {},
    onTranslation: () -> Unit = {},
    showTranslationActive: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: back button
        HeaderIcon(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = Gold, modifier = Modifier.size(16.dp))
        }

        // Right: translation + brightness icons
        Row(verticalAlignment = Alignment.CenterVertically) {
            HeaderIcon(onClick = onTranslation, filled = showTranslationActive) {
                Icon(Icons.Filled.Translate, contentDescription = "الترجمة الإنجليزية", tint = Gold, modifier = Modifier.size(16.dp))
            }
            HeaderIcon(onClick = onBrightness) {
                Icon(Icons.Filled.WbSunny, contentDescription = "السطوع", tint = Gold, modifier = Modifier.size(16.dp))
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
