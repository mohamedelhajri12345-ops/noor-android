package com.elhajri.noor.quran

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import com.elhajri.noor.ui.Gold

/** دائرة رقم الآية الذهبية — طبق الأصل عن الموقع (دائرة داكنة بحدّ ذهبي رفيع ورقم ذهبي) */
@Composable
fun AyahBadge(number: String, size: androidx.compose.ui.unit.Dp = 20.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .background(Color(0xFF123A28), CircleShape)
            .border(1.dp, Gold.copy(alpha = 0.55f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(number, color = Gold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}
