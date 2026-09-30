package com.elhajri.noor.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

/**
 * بانر علوي احترافي بصورة إسلامية حقيقية — يُستعمل في رأس كل شاشة رئيسية
 * (الأذكار، القرآن، الأناشيد، قصص الأنبياء، مواقيت الصلاة...) لإعطاء
 * التطبيق مظهراً موحّداً وفاخراً على مستوى تطبيقات عالمية.
 */
object NoorHeroImages {
    const val LANTERNS = "https://upload.wikimedia.org/wikipedia/commons/thumb/0/04/Ramadan_Lantern_in_the_Rain_Near_the_Ur_Ziggurat.jpg/1280px-Ramadan_Lantern_in_the_Rain_Near_the_Ur_Ziggurat.jpg"
    const val QURAN_REHAL = "https://upload.wikimedia.org/wikipedia/commons/thumb/8/85/The_Holy_Qur%27an_placed_on_a_Rehal_at_the_Abuja_National_Mosque.jpg/1280px-The_Holy_Qur%27an_placed_on_a_Rehal_at_the_Abuja_National_Mosque.jpg"
    const val MOSQUE_NIGHT = "https://upload.wikimedia.org/wikipedia/commons/thumb/3/3b/Night_Lights_In_Esfahan_%28110640535%29.jpeg/1280px-Night_Lights_In_Esfahan_%28110640535%29.jpeg"
    const val MOSQUE_DAY = "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c6/Mosque_Sidi_Mtir_in_Mahdia.jpg/1280px-Mosque_Sidi_Mtir_in_Mahdia.jpg"
    const val MINARET = "https://upload.wikimedia.org/wikipedia/commons/thumb/7/7e/Minaret_of_the_Alkaff_Mosque%2C_Bedok_North.jpg/1280px-Minaret_of_the_Alkaff_Mosque%2C_Bedok_North.jpg"
}

@Composable
fun NoorHeroBanner(
    imageUrl: String,
    title: String,
    subtitle: String = "",
    height: androidx.compose.ui.unit.Dp = 128.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(20.dp))
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Black.copy(alpha = 0.15f), Color.Black.copy(alpha = 0.72f))
                    )
                )
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Text(title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            if (subtitle.isNotBlank()) {
                Text(subtitle, color = Gold, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
            }
        }
    }
}
