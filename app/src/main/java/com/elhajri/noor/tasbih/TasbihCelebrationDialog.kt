package com.elhajri.noor.tasbih

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.elhajri.noor.ui.AmiriFamily
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.Navy
import com.elhajri.noor.ui.NavyCard
import kotlin.random.Random

private data class CelebrationParticle(
    var x: Float,
    var y: Float,
    var speedY: Float,
    var speedX: Float,
    var radius: Float,
    var color: Color,
    var alpha: Float
)

@Composable
fun TasbihCelebrationDialog(
    dhikrText: String,
    target: Int,
    dailyTotal: Int,
    allTimeTotal: Int,
    onDismiss: () -> Unit,
    onReset: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            NavyCard,
                            Color(0xFF0F1A2C),
                            Color(0xFF09101C)
                        )
                    )
                )
                .border(
                    width = 2.dp,
                    brush = Brush.linearGradient(
                        listOf(Gold, GoldSoft, Gold)
                    ),
                    shape = RoundedCornerShape(28.dp)
                )
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            val infiniteTransition = rememberInfiniteTransition(label = "particles")
            val particleProgress by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "particleProgress"
            )

            val particles = remember {
                List(30) {
                    CelebrationParticle(
                        x = Random.nextFloat(),
                        y = Random.nextFloat(),
                        speedY = Random.nextFloat() * 0.006f + 0.002f,
                        speedX = (Random.nextFloat() - 0.5f) * 0.003f,
                        radius = Random.nextFloat() * 5f + 3f,
                        color = if (Random.nextBoolean()) Gold else Color(0xFFFFD54F),
                        alpha = Random.nextFloat() * 0.7f + 0.3f
                    )
                }
            }

            Canvas(modifier = Modifier.matchParentSize()) {
                particles.forEach { p ->
                    p.y = (p.y + p.speedY) % 1f
                    p.x = (p.x + p.speedX + 1f) % 1f
                    drawCircle(
                        color = p.color.copy(alpha = (p.alpha * (1f - p.y)).coerceIn(0.1f, 1f)),
                        radius = p.radius,
                        center = Offset(p.x * size.width, p.y * size.height)
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Gold.copy(alpha = 0.18f))
                        .border(1.5.dp, Gold, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Gold,
                        modifier = Modifier.size(38.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "مبارك! تقبّل الله طاعتك",
                    color = Gold,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = AmiriFamily,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "أتممت الورد المبارك ($target تسبيحة)\n« $dhikrText »",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black.copy(alpha = 0.35f))
                        .border(0.5.dp, Gold.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("مجموع اليوم", color = GoldSoft.copy(alpha = 0.8f), fontSize = 11.sp)
                        Text(toArabicNumber(dailyTotal), color = Gold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(30.dp)
                            .background(Gold.copy(alpha = 0.2f))
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("المجموع الكلي", color = GoldSoft.copy(alpha = 0.8f), fontSize = 11.sp)
                        Text(toArabicNumber(allTimeTotal), color = Gold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            onReset()
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Navy),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("بدء جولة جديدة", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldSoft),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("استمرار", fontSize = 14.sp)
                    }
                }
            }
        }
    }
}
