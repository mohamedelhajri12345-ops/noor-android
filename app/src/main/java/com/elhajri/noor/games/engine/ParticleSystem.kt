package com.elhajri.noor.games.engine

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class GameParticle(
    var position: Offset,
    var velocity: Vector22D,
    var color: Color,
    var size: Float,
    var alpha: Float = 1.0f,
    var maxLifeSec: Float = 1.0f,
    var ageSec: Float = 0.0f
)

class ParticleSystem {
    private val particles = mutableListOf<GameParticle>()

    fun spawnBurst(
        origin: Offset,
        count: Int = 20,
        colors: List<Color> = listOf(Gold, GoldSoft, Color.White, Color(0xFF00E676)),
        minSpeed: Float = 100f,
        maxSpeed: Float = 400f
    ) {
        for (i in 0 until count) {
            val angle = Random.nextFloat() * 2 * Math.PI.toFloat()
            val speed = minSpeed + Random.nextFloat() * (maxSpeed - minSpeed)
            val vx = cos(angle.toDouble()).toFloat() * speed
            val vy = sin(angle.toDouble()).toFloat() * speed
            val color = colors[Random.nextInt(colors.size)]
            val size = 4f + Random.nextFloat() * 8f
            val life = 0.5f + Random.nextFloat() * 0.8f

            particles.add(
                GameParticle(
                    position = origin,
                    velocity = Vector22D(vx, vy),
                    color = color,
                    size = size,
                    maxLifeSec = life
                )
            )
        }
    }

    fun update(deltaSec: Float, gravityY: Float = 200f) {
        val iterator = particles.iterator()
        while (iterator.hasNext()) {
            val p = iterator.next()
            p.ageSec += deltaSec
            if (p.ageSec >= p.maxLifeSec) {
                iterator.remove()
                continue
            }
            p.position = Offset(
                p.position.x + p.velocity.x * deltaSec,
                p.position.y + p.velocity.y * deltaSec
            )
            p.velocity.y += gravityY * deltaSec
            p.alpha = (1.0f - (p.ageSec / p.maxLifeSec)).coerceIn(0f, 1f)
        }
    }

    fun draw(drawScope: DrawScope) {
        particles.forEach { p ->
            drawScope.drawCircle(
                color = p.color.copy(alpha = p.alpha),
                radius = p.size,
                center = p.position
            )
        }
    }

    fun clear() {
        particles.clear()
    }
}
