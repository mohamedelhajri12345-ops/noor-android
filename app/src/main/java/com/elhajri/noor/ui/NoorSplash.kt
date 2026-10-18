package com.elhajri.noor.ui

import com.elhajri.noor.R

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/** The exact deep blue of the launcher icon background. */
private val IconDeepBlue = Color(0xFF001040)
private val IconDeepBlueDark = Color(0xFF000826)

/**
 * Clean static splash: the official app icon on the icon's own deep-blue
 * background. No glow, no floating animation — just a soft fade-in.
 */
@Composable
fun NoorSplash(onFinished: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(150)
        visible = true
        delay(1500)
        onFinished()
    }

    val fadeIn by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "fadeIn"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(IconDeepBlue, IconDeepBlueDark))
            )
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
        ) {
            // App icon, completely static
            Image(
                painter = painterResource(R.mipmap.ic_launcher),
                contentDescription = "أيقونة التطبيق",
                modifier = Modifier
                    .size(120.dp)
                    .alpha(fadeIn)
                    .clip(RoundedCornerShape(28.dp))
            )

            Spacer(Modifier.height(28.dp))

            Text(
                text = "القرآن الكريم",
                color = Color.White.copy(alpha = 0.92f),
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = AmiriFamily,
                modifier = Modifier.alpha(fadeIn)
            )
        }

        Text(
            text = "نسأل الله أن يجعل هذا العمل خالصاً لوجهه الكريم",
            color = Color.White.copy(alpha = 0.45f),
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp, start = 32.dp, end = 32.dp)
                .alpha(fadeIn)
        )
    }
}
