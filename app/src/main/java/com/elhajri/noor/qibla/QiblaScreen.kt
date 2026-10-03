package com.elhajri.noor.qibla

import com.elhajri.noor.ui.themeScreenBackground
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.data.City
import com.elhajri.noor.data.Prefs
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.Navy
import com.elhajri.noor.ui.NavyCard
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan
import com.elhajri.noor.ui.NoorGradients

private const val MECCA_LAT = 21.4225
private const val MECCA_LNG = 39.8262

private fun calculateQiblaBearing(lat: Double, lng: Double): Float {
    val phi1 = Math.toRadians(lat)
    val phi2 = Math.toRadians(MECCA_LAT)
    val deltaLambda = Math.toRadians(MECCA_LNG - lng)
    val y = sin(deltaLambda)
    val x = cos(phi1) * tan(phi2) - sin(phi1) * cos(deltaLambda)
    val theta = Math.toDegrees(atan2(y, x))
    return ((theta + 360) % 360).toFloat()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QiblaScreen() {
    val context = LocalContext.current
    val city = remember { Prefs.getCity(context) ?: City("مكة المكرمة", 21.4225, 39.8262, "السعودية") }
    val qiblaBearing = remember(city) { calculateQiblaBearing(city.lat, city.lng) }

    var azimuth by remember { mutableStateOf(0f) }

    DisposableEffect(context) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        val gravity = FloatArray(3)
        val geomagnetic = FloatArray(3)
        var hasGravity = false
        var hasGeomagnetic = false

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                    System.arraycopy(event.values, 0, gravity, 0, 3)
                    hasGravity = true
                }
                if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                    System.arraycopy(event.values, 0, geomagnetic, 0, 3)
                    hasGeomagnetic = true
                }

                if (hasGravity && hasGeomagnetic) {
                    val R = FloatArray(9)
                    val I = FloatArray(9)
                    if (SensorManager.getRotationMatrix(R, I, gravity, geomagnetic)) {
                        val orientation = FloatArray(3)
                        SensorManager.getOrientation(R, orientation)
                        val azDeg = Math.toDegrees(orientation[0].toDouble()).toFloat()
                        azimuth = (azDeg + 360) % 360
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_GAME)
        sensorManager.registerListener(listener, magnetometer, SensorManager.SENSOR_DELAY_GAME)

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    val animatedAzimuth by animateFloatAsState(
        targetValue = azimuth,
        animationSpec = tween(durationMillis = 200),
        label = "azimuthAnim"
    )

    val needleRotation = (qiblaBearing - animatedAzimuth + 360) % 360
    val isAligned = abs((qiblaBearing - animatedAzimuth + 360) % 360) < 5f || abs((animatedAzimuth - qiblaBearing + 360) % 360) < 5f

    Scaffold(
        containerColor = Color(0xFF070B14)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .themeScreenBackground()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "اتجاه القبلة",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Gold,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = NavyCard,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Gold.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = null,
                            tint = Gold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${city.name}، ${city.country}",
                            color = GoldSoft,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .size(280.dp)
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val radius = size.minDimension / 2f - 10f

                    drawCircle(
                        color = Gold.copy(alpha = 0.3f),
                        radius = radius,
                        center = center,
                        style = Stroke(width = 4f)
                    )

                    drawCircle(
                        color = Gold.copy(alpha = 0.1f),
                        radius = radius - 12f,
                        center = center,
                        style = Stroke(width = 2f)
                    )

                    rotate(degrees = -animatedAzimuth, pivot = center) {
                        for (i in 0 until 360 step 30) {
                            val angleRad = Math.toRadians(i.toDouble())
                            val innerR = if (i % 90 == 0) radius - 20f else radius - 12f
                            val outerR = radius - 4f

                            val startX = center.x + (innerR * sin(angleRad)).toFloat()
                            val startY = center.y - (innerR * cos(angleRad)).toFloat()
                            val endX = center.x + (outerR * sin(angleRad)).toFloat()
                            val endY = center.y - (outerR * cos(angleRad)).toFloat()

                            drawLine(
                                color = if (i == 0) Color.Red else Gold.copy(alpha = 0.5f),
                                start = Offset(startX, startY),
                                end = Offset(endX, endY),
                                strokeWidth = if (i % 90 == 0) 3f else 1.5f
                            )
                        }
                    }

                    rotate(degrees = needleRotation, pivot = center) {
                        val needlePath = Path().apply {
                            moveTo(center.x, center.y - radius + 25f)
                            lineTo(center.x - 12f, center.y)
                            lineTo(center.x + 12f, center.y)
                            close()
                        }

                        val needleColor = if (isAligned) Color(0xFF4CAF50) else Gold

                        drawPath(
                            path = needlePath,
                            color = needleColor
                        )

                        val bottomNeedle = Path().apply {
                            moveTo(center.x, center.y + radius - 25f)
                            lineTo(center.x - 8f, center.y)
                            lineTo(center.x + 8f, center.y)
                            close()
                        }

                        drawPath(
                            path = bottomNeedle,
                            color = GoldSoft.copy(alpha = 0.4f)
                        )
                    }

                    val centerBoxSize = 28f
                    drawRect(
                        color = if (isAligned) Color(0xFF4CAF50) else Gold,
                        topLeft = Offset(center.x - centerBoxSize / 2, center.y - centerBoxSize / 2),
                        size = androidx.compose.ui.geometry.Size(centerBoxSize, centerBoxSize)
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = NavyCard),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isAligned) Color(0xFF4CAF50) else Gold.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${qiblaBearing.toInt()}° من الشمال",
                        color = Gold,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (isAligned) "أنت باتجاه القبلة تماماً 🕋" else "ضع الجهاز أفقياً ودر باتجاه السهم الذهبي",
                        color = if (isAligned) Color(0xFF4CAF50) else GoldSoft.copy(alpha = 0.8f),
                        fontSize = 13.sp,
                        fontWeight = if (isAligned) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}
