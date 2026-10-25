package com.elhajri.noor.games.engine

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import kotlin.math.*

/**
 * Lightweight physics and collision math helpers for Compose Canvas games.
 */
data class Vector22D(var x: Float = 0f, var y: Float = 0f) {
    fun length(): Float = sqrt(x * x + y * y)
    fun normalize(): Vector22D {
        val len = length()
        return if (len > 0f) Vector22D(x / len, y / len) else Vector22D(0f, 0f)
    }
    operator fun plus(other: Vector22D) = Vector22D(x + other.x, y + other.y)
    operator fun minus(other: Vector22D) = Vector22D(x - other.x, y - other.y)
    operator fun times(scalar: Float) = Vector22D(x * scalar, y * scalar)
    fun toOffset() = Offset(x, y)
}

data class AABBBox(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
) {
    val width: Float get() = right - left
    val height: Float get() = bottom - top
    val center: Offset get() = Offset((left + right) / 2f, (top + bottom) / 2f)

    fun intersects(other: AABBBox): Boolean {
        return left < other.right && right > other.left && top < other.bottom && bottom > other.top
    }

    fun contains(point: Offset): Boolean {
        return point.x in left..right && point.y in top..bottom
    }

    fun toRect(): Rect = Rect(left, top, right, bottom)
}

data class CircleBody(
    var center: Offset,
    var radius: Float,
    var velocity: Vector22D = Vector22D()
) {
    fun intersectsCircle(other: CircleBody): Boolean {
        val dx = center.x - other.center.x
        val dy = center.y - other.center.y
        val distSq = dx * dx + dy * dy
        val radSum = radius + other.radius
        return distSq <= radSum * radSum
    }

    fun intersectsBox(box: AABBBox): Boolean {
        val closestX = center.x.coerceIn(box.left, box.right)
        val closestY = center.y.coerceIn(box.top, box.bottom)
        val dx = center.x - closestX
        val dy = center.y - closestY
        return (dx * dx + dy * dy) <= (radius * radius)
    }
}

object PhysicsEngine {
    /**
     * Compute parabolic trajectory points for aiming arc cannons / slingshots.
     */
    fun computeTrajectory(
        startPos: Offset,
        velocity: Vector22D,
        gravity: Float = 980f, // pixels/s^2
        steps: Int = 20,
        timeStepSec: Float = 0.08f
    ): List<Offset> {
        val points = mutableListOf<Offset>()
        var currentX = startPos.x
        var currentY = startPos.y
        var vx = velocity.x
        var vy = velocity.y

        for (i in 0 until steps) {
            points.add(Offset(currentX, currentY))
            currentX += vx * timeStepSec
            currentY += vy * timeStepSec
            vy += gravity * timeStepSec
        }
        return points
    }

    /**
     * Reflection vector calculation when a ball bounces off a surface normal.
     */
    fun reflect(velocity: Vector22D, normal: Vector22D, restitution: Float = 0.85f): Vector22D {
        val dot = velocity.x * normal.x + velocity.y * normal.y
        return Vector22D(
            (velocity.x - 2 * dot * normal.x) * restitution,
            (velocity.y - 2 * dot * normal.y) * restitution
        )
    }
}
