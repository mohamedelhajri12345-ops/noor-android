package com.elhajri.noor.ui

import com.elhajri.noor.R

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Animated splash screen, faithful to the web app's Splash:
 * floating crescent with a pulsing golden glow, shimmering title,
 * tagline, and closing prayer phrase.
 */
@Composable
fun NoorSplash(onFinished: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(120)
        visible = true
        delay(2600)
        onFinished()
    }

    val fadeIn by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(700, easing = FastOutSlowInEasing),
        label = "fadeIn"
    )
    val rise by animateFloatAsState(
        targetValue = if (visible) 0f else 40f,
        animationSpec = tween(700, easing = FastOutSlowInEasing),
        label = "rise"
    )

    val transition = rememberInfiniteTransition(label = "splash")
    // crescent gently floats up and down
    val floatY by transition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(tween(1600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "floatY"
    )
    // golden glow pulses
    val glow by transition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1250, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "glow"
    )
    // shimmering title: alpha oscillation across gold tones
    val shimmer by transition.animateFloat(
        initialValue = 0.75f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "shimmer"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF131C31), Navy, Color(0xFF05080F))
                )
            )
    ) {
        // الخلفية الجديدة: سماء الليل والمسجد — نفس خلفية التطبيق
        Image(
            painter = androidx.compose.ui.res.painterResource(R.drawable.theme_bg_makkah_night),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop
        )
        Box(
            modifier = Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    0f to Navy.copy(alpha = 0.55f),
                    1f to Color(0xFF05080F).copy(alpha = 0.85f)
                )
            )
        )
        Column(
            modifier = Modifier.align(Alignment.Center).fillMaxSize().padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
        ) {
            // Floating crescent with glow halo
            Box(
                modifier = Modifier
                    .graphicsLayer { translationY = floatY.dp.toPx() }
                    .alpha(fadeIn)
            ) {
                // glow halo
                Canvas(modifier = Modifier.size(190.dp)) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Gold.copy(alpha = 0.35f * glow), Color.Transparent),
                            radius = size.minDimension / 2f
                        ),
                        radius = size.minDimension / 2f
                    )
                }
                // أيقونة التطبيق الرسمية — نفس أيقونة المشغّل الحالية
                Image(
                    painter = painterResource(R.mipmap.ic_launcher),
                    contentDescription = "أيقونة التطبيق",
                    modifier = Modifier
                        .size(112.dp)
                        .align(Alignment.Center)
                        .clip(RoundedCornerShape(28.dp))
                )
            }

            Spacer(Modifier.height(36.dp))

            // App title with shimmer
            Text(
                text = "القرآن الكريم",
                color = Gold.copy(alpha = shimmer),
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = AmiriFamily,
                modifier = Modifier
                    .alpha(fadeIn)
                    .graphicsLayer { translationY = rise.dp.toPx() }
            )
            Spacer(Modifier.height(14.dp))
            Text(
                text = "نور قلبك.. يقينك.. عبادتك",
                color = GoldSoft.copy(alpha = 0.7f),
                fontSize = 14.sp,
                modifier = Modifier
                    .alpha(if (visible) 1f else 0f)
                    .graphicsLayer { translationY = rise.dp.toPx() }
            )
        }

        // Closing prayer phrase at the bottom
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
