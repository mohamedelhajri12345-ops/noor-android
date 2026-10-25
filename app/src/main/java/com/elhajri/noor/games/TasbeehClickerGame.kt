package com.elhajri.noor.games

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.audio.SfxManager
import com.elhajri.noor.games.engine.*
import com.elhajri.noor.settings.t
import com.elhajri.noor.theme.NoorThemeState
import com.elhajri.noor.ui.TextMain
import com.elhajri.noor.ui.noorGlassCard

data class GardenPlant(
    val typeName: String,
    val color: Color,
    val position: Offset,
    val radius: Float
)

@Composable
fun TasbeehClickerGame(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val theme = NoorThemeState.active
    val engineState = rememberGameEngineState(initialLevel = 1, targetScore = 200)

    val athkarList = remember { listOf("سبحان الله", "الحمد لله", "لا إله إلا الله", "الله أكبر") }
    var currentAthkarIdx by remember { mutableIntStateOf(0) }

    val gardenPlants = remember { mutableStateListOf<GardenPlant>() }
    val particles = remember { ParticleSystem() }

    fun initGame() {
        gardenPlants.clear()
        currentAthkarIdx = 0
        engineState.startGame()
    }

    LaunchedEffect(Unit) {
        initGame()
    }

    fun handleBeadTap(centerOffset: Offset) {
        if (engineState.playState != GamePlayState.PLAYING) return

        engineState.addScore(1)
        currentAthkarIdx = (currentAthkarIdx + 1) % athkarList.size
        triggerGameHaptic(context, GameHapticType.LIGHT)
        SfxManager.playTap(context)
        particles.spawnBurst(centerOffset, count = 10)

        if (engineState.score >= engineState.targetScore) {
            engineState.completeLevel(context, 3)
        }
    }

    fun buyPlant(cost: Int, name: String, color: Color, radius: Float) {
        if (engineState.score >= cost) {
            engineState.addScore(-cost)
            val px = 80f + (gardenPlants.size * 60f) % 320f
            val py = 120f + (gardenPlants.size * 40f) % 240f
            gardenPlants.add(GardenPlant(name, color, Offset(px, py), radius))
            SfxManager.playSuccess(context)
            triggerGameHaptic(context, GameHapticType.MEDIUM)
        }
    }

    CanvasGameLoop(engineState = engineState) { deltaSec ->
        particles.update(deltaSec)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            GameHeaderBar(
                title = t("حديقة التسبيح", "Tasbeeh Garden"),
                level = engineState.level,
                score = engineState.score,
                lives = engineState.lives,
                onBack = onBack,
                onPause = { engineState.pauseGame() }
            )

            Spacer(Modifier.height(8.dp))

            // Garden Visualizer Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(horizontal = 16.dp)
                    .noorGlassCard(cornerRadius = 20.dp)
                    .border(2.dp, theme.accent.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Background grass gradient
                    drawRect(color = Color(0xFF0F2B1D).copy(alpha = 0.6f))

                    // Draw plants
                    gardenPlants.forEach { plant ->
                        drawCircle(
                            color = plant.color,
                            radius = plant.radius,
                            center = plant.position
                        )
                        drawCircle(
                            color = Color.White.copy(alpha = 0.7f),
                            radius = plant.radius * 0.4f,
                            center = plant.position
                        )
                    }

                    particles.draw(this)
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                ) {
                    Text(
                        text = "🌴 أشجار الحديقة: ${gardenPlants.size}",
                        color = theme.accent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Center Tasbeeh Bead Button
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(theme.accent)
                    .border(4.dp, Color(0xFFFFD700), CircleShape)
                    .clickable {
                        handleBeadTap(Offset(200f, 200f))
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = athkarList[currentAthkarIdx],
                        color = Color.Black,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "اضغط للتسبيح",
                        color = Color.Black.copy(alpha = 0.7f),
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Shop / Build Row
            Text(
                text = t("متجر غرس الجنة 🌿", "Paradise Garden Shop 🌿"),
                color = TextMain,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Plant Option 1: Palm (10 pts)
                Button(
                    onClick = { buyPlant(10, "نخلة", Color(0xFF00E676), 22f) },
                    enabled = engineState.score >= 10,
                    colors = ButtonDefaults.buttonColors(containerColor = theme.accent),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("🌴 نخلة (10)", fontSize = 11.sp, color = Color.Black)
                }

                Spacer(Modifier.width(8.dp))

                // Plant Option 2: Olive (25 pts)
                Button(
                    onClick = { buyPlant(25, "زيتونة", Color(0xFF76FF03), 26f) },
                    enabled = engineState.score >= 25,
                    colors = ButtonDefaults.buttonColors(containerColor = theme.accent),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("🫒 زيتونة (25)", fontSize = 11.sp, color = Color.Black)
                }

                Spacer(Modifier.width(8.dp))

                // Plant Option 3: Fountain (50 pts)
                Button(
                    onClick = { buyPlant(50, "عين ماء", Color(0xFF80D8FF), 30f) },
                    enabled = engineState.score >= 50,
                    colors = ButtonDefaults.buttonColors(containerColor = theme.accent),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("⛲ نافورة (50)", fontSize = 11.sp, color = Color.Black)
                }
            }
        }

        when (engineState.playState) {
            GamePlayState.PAUSED -> PauseOverlay(
                onResume = { engineState.resumeGame() },
                onRestart = { initGame() },
                onQuit = onBack
            )
            GamePlayState.WON -> GameWinOverlay(
                score = engineState.score,
                stars = engineState.starsEarned,
                onRestart = { initGame() },
                onQuit = onBack
            )
            GamePlayState.LOST -> GameLoseOverlay(
                score = engineState.score,
                onRestart = { initGame() },
                onQuit = onBack
            )
            else -> {}
        }
    }
}
